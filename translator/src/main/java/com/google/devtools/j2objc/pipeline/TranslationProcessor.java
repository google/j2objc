/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devtools.j2objc.pipeline;

import com.google.common.annotations.VisibleForTesting;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.DebugASTDump;
import com.google.devtools.j2objc.gen.GenerationUnit;
import com.google.devtools.j2objc.gen.ObjectiveCHeaderGenerator;
import com.google.devtools.j2objc.gen.ObjectiveCImplementationGenerator;
import com.google.devtools.j2objc.gen.ObjectiveCSegmentedHeaderGenerator;
import com.google.devtools.j2objc.translate.AbstractMethodRewriter;
import com.google.devtools.j2objc.translate.AnnotationRewriter;
import com.google.devtools.j2objc.translate.ArrayRewriter;
import com.google.devtools.j2objc.translate.Autoboxer;
import com.google.devtools.j2objc.translate.CastResolver;
import com.google.devtools.j2objc.translate.ComplexExpressionExtractor;
import com.google.devtools.j2objc.translate.ConstantBranchPruner;
import com.google.devtools.j2objc.translate.DeadCodeEliminator;
import com.google.devtools.j2objc.translate.DefaultMethodShimGenerator;
import com.google.devtools.j2objc.translate.DestructorGenerator;
import com.google.devtools.j2objc.translate.EnhancedForRewriter;
import com.google.devtools.j2objc.translate.EnumRewriter;
import com.google.devtools.j2objc.translate.Functionizer;
import com.google.devtools.j2objc.translate.GwtConverter;
import com.google.devtools.j2objc.translate.InitializationNormalizer;
import com.google.devtools.j2objc.translate.InnerClassExtractor;
import com.google.devtools.j2objc.translate.JavaCloneWriter;
import com.google.devtools.j2objc.translate.JavaToIOSMethodTranslator;
import com.google.devtools.j2objc.translate.LabelRewriter;
import com.google.devtools.j2objc.translate.LambdaRewriter;
import com.google.devtools.j2objc.translate.LambdaTypeElementAdder;
import com.google.devtools.j2objc.translate.MetadataWriter;
import com.google.devtools.j2objc.translate.NilCheckResolver;
import com.google.devtools.j2objc.translate.NumberMethodRewriter;
import com.google.devtools.j2objc.translate.OcniExtractor;
import com.google.devtools.j2objc.translate.OperatorRewriter;
import com.google.devtools.j2objc.translate.OuterReferenceResolver;
import com.google.devtools.j2objc.translate.PackageInfoRewriter;
import com.google.devtools.j2objc.translate.PrivateDeclarationResolver;
import com.google.devtools.j2objc.translate.Rewriter;
import com.google.devtools.j2objc.translate.StaticVarRewriter;
import com.google.devtools.j2objc.translate.SuperMethodInvocationRewriter;
import com.google.devtools.j2objc.translate.SwitchRewriter;
import com.google.devtools.j2objc.translate.UnsequencedExpressionRewriter;
import com.google.devtools.j2objc.translate.VarargsRewriter;
import com.google.devtools.j2objc.translate.VariableRenamer;
import com.google.devtools.j2objc.types.HeaderImportCollector;
import com.google.devtools.j2objc.types.ImplementationImportCollector;
import com.google.devtools.j2objc.types.Import;
import com.google.devtools.j2objc.util.CodeReferenceMap;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.Parser;
import com.google.devtools.j2objc.util.TimeTracker;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Processes source files by translating each source into an Objective-C header
 * and an Objective-C source file.
 *
 * @author Tom Ball, Keith Stanger, Mike Thvedt
 */
public class TranslationProcessor extends FileProcessor {

  private static final Logger logger = Logger.getLogger(TranslationProcessor.class.getName());

  private final CodeReferenceMap deadCodeMap;

  private int processedCount = 0;

  public TranslationProcessor(Parser parser, CodeReferenceMap deadCodeMap) {
    super(parser);
    this.deadCodeMap = deadCodeMap;
  }

  @Override
  protected void processConvertedTree(ProcessingContext input, CompilationUnit unit) {
    String unitName = input.getOriginalSourcePath();
    if (logger.isLoggable(Level.INFO)) {
      System.out.println("translating " + unitName);
    }
    TimeTracker ticker = TimeTracker.getTicker(unitName, options.timingLevel());
    if (options.dumpAST()) {
      // Dump compilation unit to an .ast output file instead of translating.
      DebugASTDump.dumpUnit(unit);
    } else {
      applyMutations(unit, deadCodeMap, ticker);
      ticker.tick("Tree mutations");
      ticker.printResults(System.out);

      GenerationUnit genUnit = input.getGenerationUnit();
      genUnit.addCompilationUnit(unit);

      // Add out-of-date dependencies to translation list.
      if (closureQueue != null) {
        checkDependencies(unit);
      }

      if (genUnit.isFullyParsed()) {
        generateObjectiveCSource(genUnit);
      }
    }
    processedCount++;
  }

  /**
   * Translates a parsed source file, modifying the compilation unit by
   * substituting core Java type and method references with iOS equivalents.
   * For example, <code>java.lang.Object</code> maps to <code>NSObject</code>,
   * and <code>java.lang.String</code> to <code>NSString</code>. The source is
   * also modified to add support for iOS memory management, extract inner
   * classes, etc.
   */
  public static void applyMutations(CompilationUnit unit, CodeReferenceMap deadCodeMap,
      TimeTracker ticker) {
    ticker.push();

    // Before: OuterReferenceResolver - OuterReferenceResolver needs the bindings fixed.
    new LambdaTypeElementAdder(unit).run();
    ticker.tick("LambdaTypeElementAdder");

    if (deadCodeMap != null) {
      new DeadCodeEliminator(unit, deadCodeMap).run();
      ticker.tick("DeadCodeEliminator");
    }

    new OuterReferenceResolver(unit).run();
    ticker.tick("OuterReferenceResolver");

    // Update code that has GWT references.
    new GwtConverter(unit).run();
    ticker.tick("GwtConverter");

    // Add default equals/hashCode methods to Number subclasses, if necessary.
    new NumberMethodRewriter(unit).run();
    ticker.tick("NumberMethodRewriter");

    // Before: Rewriter - Pruning unreachable statements must happen before
    //   rewriting labeled break statements.
    // Before: InnerClassExtractor - Removes unreachable local classes.
    new ConstantBranchPruner(unit).run();
    ticker.tick("ConstantBranchPruner");

    // Modify AST to be more compatible with Objective C
    new Rewriter(unit).run();
    ticker.tick("Rewriter");

    // Add abstract method stubs.
    new AbstractMethodRewriter(unit, deadCodeMap).run();
    ticker.tick("AbstractMethodRewriter");

    new VariableRenamer(unit).run();
    ticker.tick("VariableRenamer");

    // Rewrite enhanced for loops into correct C code.
    new EnhancedForRewriter(unit).run();
    ticker.tick("EnhancedForRewriter");

    // Before: Autoboxer - Must generate implementations so autoboxing can be applied to result.
    new LambdaRewriter(unit).run();
    ticker.tick("LambdaRewriter");

    // Add auto-boxing conversions.
    new Autoboxer(unit).run();
    ticker.tick("Autoboxer");

    new InnerClassExtractor(unit).run();
    ticker.tick("InnerClassExtractor");

    // Generate method shims for classes implementing interfaces that have default methods
    new DefaultMethodShimGenerator(unit, deadCodeMap).run();
    ticker.tick("DefaultMethodShimGenerator");

    // Normalize init statements
    new InitializationNormalizer(unit).run();
    ticker.tick("InitializationNormalizer");

    // Adds nil_chk calls wherever an expression is dereferenced.
    // After: InnerClassExtractor - Cannot handle local classes.
    // After: InitializationNormalizer
    // Before: LabelRewriter - Control flow analysis requires original Java
    //   labels.
    new NilCheckResolver(unit).run();
    ticker.tick("NilCheckResolver");

    // Rewrites expressions that would cause unsequenced compile errors.
    if (unit.getEnv().options().extractUnsequencedModifications()) {
      new UnsequencedExpressionRewriter(unit).run();
      ticker.tick("UnsequencedExpressionRewriter");
    }

    // Rewrites labeled break and continue statements.
    unit.accept(new LabelRewriter());
    ticker.tick("LabelRewriter");

    // Before: ArrayRewriter - Adds ArrayCreation nodes.
    // Before: Functionizer - Can't rewrite function arguments.
    new VarargsRewriter(unit).run();
    ticker.tick("VarargsRewriter");

    new JavaCloneWriter(unit).run();
    ticker.tick("JavaCloneWriter");

    new OcniExtractor(unit, deadCodeMap).run();
    ticker.tick("OcniExtractor");

    // Before: AnnotationRewriter - Needs AnnotationRewriter to add the
    //   annotation metadata to the generated package-info type.
    PackageInfoRewriter.run(unit);
    ticker.tick("PackageInfoRewriter");

    // Before: DestructorGenerator - Annotation types need a destructor to
    //   release the added fields.
    new AnnotationRewriter(unit).run();
    ticker.tick("AnnotationRewriter");

    // Before: Functionizer - Edits constructor invocations before they are
    //   functionized.
    new EnumRewriter(unit).run();
    ticker.tick("EnumRewriter");

    // Add dealloc/finalize method(s), if necessary.  This is done
    // after inner class extraction, so that each class releases
    // only its own instance variables.
    new DestructorGenerator(unit).run();
    ticker.tick("DestructorGenerator");

    // Before: StaticVarRewriter - Generates static variable access expressions.
    new MetadataWriter(unit, deadCodeMap).run();
    ticker.tick("MetadataWriter");

    // Before: Functionizer - Needs to rewrite some ClassInstanceCreation nodes
    //   before Functionizer does.
    // Before: StaticVarRewriter, OperatorRewriter - Doesn't know how to handle
    //   the hasRetainedResult flag on ClassInstanceCreation nodes.
    new JavaToIOSMethodTranslator(unit).run();
    ticker.tick("JavaToIOSMethodTranslator");

    // After: OcniExtractor - So that native methods can be correctly
    //   functionized.
    new Functionizer(unit).run();
    ticker.tick("Functionizer");

    // After: Functionizer - Edits the qualifier on SuperMethodInvocation nodes.
    new SuperMethodInvocationRewriter(unit).run();
    ticker.tick("SuperMethodInvocationRewriter");

    new OperatorRewriter(unit).run();
    ticker.tick("OperatorRewriter");

    // After: OperatorRewriter - Static load rewriting needs to happen after
    //   operator rewriting.
    new StaticVarRewriter(unit).run();
    ticker.tick("StaticVarRewriter");

    // After: StaticVarRewriter, OperatorRewriter - They set the
    //   hasRetainedResult on ArrayCreation nodes.
    new ArrayRewriter(unit).run();
    ticker.tick("ArrayRewriter");

    new SwitchRewriter(unit).run();
    ticker.tick("SwitchRewriter");

    // Breaks up deeply nested expressions such as chained method calls.
    // Should be one of the last translations because other mutations will
    // affect how deep the expressions are.
    unit.accept(new ComplexExpressionExtractor());
    ticker.tick("ComplexExpressionExtractor");

    // Should be one of the last translations because methods and functions
    // added in other phases may need added casts.
    new CastResolver(unit).run();
    ticker.tick("CastResolver");

    // After: InnerClassExtractor, Functionizer - Expects all types to be
    //   top-level and functionizing to have occured.
    new PrivateDeclarationResolver(unit).run();
    ticker.tick("PrivateDeclarationResolver");

    if (deadCodeMap != null) {
      DeadCodeEliminator.removeDeadClasses(unit, deadCodeMap);
      ticker.tick("removeDeadClasses");
    }

    // Make sure we still have a valid AST.
    unit.validate();

    ticker.pop();
  }

  @VisibleForTesting
  public static void generateObjectiveCSource(GenerationUnit unit) {
    assert unit.getOutputPath() != null;
    assert unit.isFullyParsed();
    TimeTracker ticker = TimeTracker.getTicker(unit.getSourceName(), unit.options().timingLevel());
    logger.fine("Generating " + unit.getOutputPath());
    logger.finest("writing output file(s) to "
        + unit.options().fileUtil().getOutputDirectory().getAbsolutePath());
    ticker.push();

    // write header
    if (unit.options().generateSegmentedHeaders()) {
      ObjectiveCSegmentedHeaderGenerator.generate(unit);
    } else {
      ObjectiveCHeaderGenerator.generate(unit);
    }
    ticker.tick("Header generation");

    // write implementation file
    ObjectiveCImplementationGenerator.generate(unit);
    ticker.tick("Implementation generation");

    unit.finished();
    ticker.pop();
    ticker.tick("Source generation");
    ticker.printResults(System.out);
  }

  @Override
  protected void handleError(ProcessingContext input) {
    // Causes the generation unit to release any trees it was holding.
    input.getGenerationUnit().failed();
  }

  public void postProcess() {
    if (logger.isLoggable(Level.INFO)) {
      int nFiles = processedCount;
      System.out.println(String.format(
          "Translated %d %s: %d errors, %d warnings",
          nFiles, nFiles == 1 ? "file" : "files", ErrorUtil.errorCount(),
          ErrorUtil.warningCount()));
    }
  }

  private void checkDependencies(CompilationUnit unit) {
    HeaderImportCollector hdrCollector =
        new HeaderImportCollector(unit, HeaderImportCollector.Filter.INCLUDE_ALL);
    hdrCollector.run();
    ImplementationImportCollector implCollector = new ImplementationImportCollector(unit);
    implCollector.run();
    Set<Import> imports = hdrCollector.getForwardDeclarations();
    imports.addAll(hdrCollector.getSuperTypes());
    imports.addAll(implCollector.getImports());
    for (Import imp : imports) {
      String qualifiedName = imp.getJavaQualifiedName();
      if (qualifiedName != null) {
        closureQueue.addName(qualifiedName);
      }
    }
  }
}
