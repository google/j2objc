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

package com.google.devtools.j2objc;

import com.google.common.annotations.VisibleForTesting;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.gen.GenerationUnit;
import com.google.devtools.j2objc.gen.ObjectiveCHeaderGenerator;
import com.google.devtools.j2objc.gen.ObjectiveCImplementationGenerator;
import com.google.devtools.j2objc.gen.ObjectiveCSegmentedHeaderGenerator;
import com.google.devtools.j2objc.translate.AbstractMethodRewriter;
import com.google.devtools.j2objc.translate.AnonymousClassConverter;
import com.google.devtools.j2objc.translate.ArrayRewriter;
import com.google.devtools.j2objc.translate.Autoboxer;
import com.google.devtools.j2objc.translate.CastResolver;
import com.google.devtools.j2objc.translate.ComplexExpressionExtractor;
import com.google.devtools.j2objc.translate.ConstantBranchPruner;
import com.google.devtools.j2objc.translate.DeadCodeEliminator;
import com.google.devtools.j2objc.translate.DestructorGenerator;
import com.google.devtools.j2objc.translate.EnhancedForRewriter;
import com.google.devtools.j2objc.translate.EnumRewriter;
import com.google.devtools.j2objc.translate.Functionizer;
import com.google.devtools.j2objc.translate.GwtConverter;
import com.google.devtools.j2objc.translate.InitializationNormalizer;
import com.google.devtools.j2objc.translate.InnerClassExtractor;
import com.google.devtools.j2objc.translate.JavaCloneWriter;
import com.google.devtools.j2objc.translate.JavaToIOSMethodTranslator;
import com.google.devtools.j2objc.translate.NilCheckResolver;
import com.google.devtools.j2objc.translate.OcniExtractor;
import com.google.devtools.j2objc.translate.OperatorRewriter;
import com.google.devtools.j2objc.translate.OuterReferenceFixer;
import com.google.devtools.j2objc.translate.OuterReferenceResolver;
import com.google.devtools.j2objc.translate.PrivateDeclarationResolver;
import com.google.devtools.j2objc.translate.Rewriter;
import com.google.devtools.j2objc.translate.StaticVarRewriter;
import com.google.devtools.j2objc.translate.SuperMethodInvocationRewriter;
import com.google.devtools.j2objc.translate.UnsequencedExpressionRewriter;
import com.google.devtools.j2objc.translate.VarargsRewriter;
import com.google.devtools.j2objc.translate.VariableRenamer;
import com.google.devtools.j2objc.types.HeaderImportCollector;
import com.google.devtools.j2objc.types.IOSTypeBinding;
import com.google.devtools.j2objc.types.ImplementationImportCollector;
import com.google.devtools.j2objc.types.Import;
import com.google.devtools.j2objc.util.DeadCodeMap;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.FileUtil;
import com.google.devtools.j2objc.util.JdtParser;
import com.google.devtools.j2objc.util.TimeTracker;

import org.eclipse.jdt.core.dom.ITypeBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Processes source files by translating each source into an Objective-C header
 * and an Objective-C source file.
 *
 * @author Tom Ball, Keith Stanger, Mike Thvedt
 */
class TranslationProcessor extends FileProcessor {

  private static final Logger logger = Logger.getLogger(TranslationProcessor.class.getName());

  private final DeadCodeMap deadCodeMap;

  private int processedCount = 0;

  public TranslationProcessor(JdtParser parser, DeadCodeMap deadCodeMap) {
    super(parser);
    this.deadCodeMap = deadCodeMap;
  }

  @Override
  protected void processConvertedTree(ProcessingContext input, CompilationUnit unit) {
    GenerationUnit genUnit = input.getGenerationUnit();
    genUnit.addCompilationUnit(unit);

    if (genUnit.isFullyParsed()) {
      logger.finest("Processing compiled unit " + genUnit.getName()
          + " of size " + genUnit.getCompilationUnits().size());
      processCompiledGenerationUnit(genUnit);
    }
  }

  protected void processCompiledGenerationUnit(GenerationUnit unit) {
    assert unit.getOutputPath() != null;
    assert unit.isFullyParsed();
    TimeTracker ticker = getTicker(unit.getOutputPath());
    ticker.push();
    try {
      if (logger.isLoggable(Level.INFO)) {
        System.out.println("translating " + unit.getSourceName());
      }

      for (CompilationUnit compUnit : unit.getCompilationUnits()) {
        applyMutations(compUnit, deadCodeMap, ticker);
        ticker.tick("Tree mutations for " + compUnit.getMainTypeName());
        processedCount++;
      }

      logger.finest("writing output file(s) to " + Options.getOutputDirectory().getAbsolutePath());

      generateObjectiveCSource(unit, ticker);
      ticker.tick("Source generation");

      if (closureQueue != null) {
        // Add out-of-date dependencies to translation list.
        for (CompilationUnit compilationUnit : unit.getCompilationUnits()) {
          checkDependencies(compilationUnit);
        }
      }

      OuterReferenceResolver.cleanup();
    } finally {
      unit.finished();
      ticker.pop();
      ticker.tick("Total processing time");
      ticker.printResults(System.out);
    }
  }

  /**
   * Translates a parsed source file, modifying the compilation unit by
   * substituting core Java type and method references with iOS equivalents.
   * For example, <code>java.lang.Object</code> maps to <code>NSObject</code>,
   * and <code>java.lang.String</code> to <code>NSString</code>. The source is
   * also modified to add support for iOS memory management, extract inner
   * classes, etc.
   */
  public static void applyMutations(
      CompilationUnit unit, DeadCodeMap deadCodeMap, TimeTracker ticker) {
    ticker.push();

    if (deadCodeMap != null) {
      new DeadCodeEliminator(unit, deadCodeMap).run(unit);
      ticker.tick("DeadCodeEliminator");
    }

    OuterReferenceResolver.resolve(unit);
    ticker.tick("OuterReferenceResolver");

    // Update code that has GWT references.
    new GwtConverter().run(unit);
    ticker.tick("GwtConverter");

    // Modify AST to be more compatible with Objective C
    new Rewriter().run(unit);
    ticker.tick("Rewriter");

    // Add abstract method stubs.
    new AbstractMethodRewriter(unit).run(unit);
    ticker.tick("AbstractMethodRewriter");

    new VariableRenamer().run(unit);
    ticker.tick("VariableRenamer");

    // Rewrite enhanced for loops into correct C code.
    new EnhancedForRewriter().run(unit);
    ticker.tick("EnhancedForRewriter");

    // Add auto-boxing conversions.
    new Autoboxer().run(unit);
    ticker.tick("Autoboxer");

    // Extract inner and anonymous classes
    new AnonymousClassConverter().run(unit);
    ticker.tick("AnonymousClassConverter");
    new InnerClassExtractor(unit).run(unit);
    ticker.tick("InnerClassExtractor");

    // Normalize init statements
    new InitializationNormalizer().run(unit);
    ticker.tick("InitializationNormalizer");

    // Fix references to outer scope and captured variables.
    new OuterReferenceFixer().run(unit);
    ticker.tick("OuterReferenceFixer");

    // Rewrites expressions that would cause unsequenced compile errors.
    if (Options.extractUnsequencedModifications()) {
      new UnsequencedExpressionRewriter().run(unit);
      ticker.tick("UnsequencedExpressionRewriter");
    }

    // Adds nil_chk calls wherever an expression is dereferenced.
    new NilCheckResolver().run(unit);
    ticker.tick("NilCheckResolver");

    // Before: ArrayRewriter - Adds ArrayCreation nodes.
    // Before: Functionizer - Can't rewrite function arguments.
    new VarargsRewriter().run(unit);
    ticker.tick("VarargsRewriter");

    // Add dealloc/finalize method(s), if necessary.  This is done
    // after inner class extraction, so that each class releases
    // only its own instance variables.
    new DestructorGenerator().run(unit);
    ticker.tick("DestructorGenerator");

    new JavaCloneWriter().run(unit);
    ticker.tick("JavaCloneWriter");

    new ConstantBranchPruner().run(unit);
    ticker.tick("ConstantBranchPruner");

    new OcniExtractor(unit).run(unit);
    ticker.tick("OcniExtractor");

    // Before: Functionizer - Edits constructor invocations before they are
    //   functionized.
    new EnumRewriter().run(unit);
    ticker.tick("EnumRewriter");

    // Before: Functionizer - Needs to rewrite some ClassInstanceCreation nodes
    //   before Functionizer does.
    // Before: StaticVarRewriter, OperatorRewriter - Doesn't know how to handle
    //   the hasRetainedResult flag on ClassInstanceCreation nodes.
    new JavaToIOSMethodTranslator().run(unit);
    ticker.tick("JavaToIOSMethodTranslator");

    // After: OcniExtractor - So that native methods can be correctly
    //   functionized.
    new Functionizer().run(unit);
    ticker.tick("Functionizer");

    // After: OuterReferenceFixer, Functionizer - Those passes edit the
    //   qualifier on SuperMethodInvocation nodes.
    new SuperMethodInvocationRewriter().run(unit);
    ticker.tick("SuperMethodInvocationRewriter");

    new StaticVarRewriter().run(unit);
    ticker.tick("StaticVarRewriter");

    new OperatorRewriter().run(unit);
    ticker.tick("OperatorRewriter");

    // After: StaticVarRewriter, OperatorRewriter - They set the
    //   hasRetainedResult on ArrayCreation nodes.
    new ArrayRewriter().run(unit);
    ticker.tick("ArrayRewriter");

    // Breaks up deeply nested expressions such as chained method calls.
    // Should be one of the last translations because other mutations will
    // affect how deep the expressions are.
    new ComplexExpressionExtractor().run(unit);
    ticker.tick("ComplexExpressionExtractor");

    // Should be one of the last translations because methods and functions
    // added in other phases may need added casts.
    new CastResolver().run(unit);
    ticker.tick("CastResolver");

    // After: InnerClassExtractor, Functionizer - Expects all types to be
    //   top-level and functionizing to have occured.
    new PrivateDeclarationResolver().run(unit);
    ticker.tick("PrivateDeclarationResolver");

    // Make sure we still have a valid AST.
    unit.validate();

    ticker.pop();
  }

  @VisibleForTesting
  public static void generateObjectiveCSource(GenerationUnit unit, TimeTracker ticker) {
    ticker.push();

    // write header
    if (Options.generateSegmentedHeaders()) {
      ObjectiveCSegmentedHeaderGenerator.generate(unit);
    } else {
      ObjectiveCHeaderGenerator.generate(unit);
    }
    ticker.tick("Header generation");

    // write implementation file
    ObjectiveCImplementationGenerator.generate(unit);
    ticker.tick("Implementation generation");

    ticker.pop();
  }

  protected void handleError(ProcessingContext input) {
    // Causes the generation unit to release any trees it was holding.
    input.getGenerationUnit().failed();
  }

  public void postProcess() {
    printHeaderMappings();

    if (logger.isLoggable(Level.INFO)) {
      int nFiles = processedCount;
      System.out.println(String.format(
          "Translated %d %s: %d errors, %d warnings",
          nFiles, nFiles == 1 ? "file" : "files", ErrorUtil.errorCount(),
          ErrorUtil.warningCount()));
      if (Options.finalMethodsAsFunctions()) {
        System.out.println(String.format("Translated %d methods as functions",
            ErrorUtil.functionizedMethodCount()));
      }
    }
  }

  private void checkDependencies(CompilationUnit unit) {
    HeaderImportCollector hdrCollector =
        new HeaderImportCollector(HeaderImportCollector.Filter.INCLUDE_ALL);
    hdrCollector.collect(unit);
    ImplementationImportCollector implCollector = new ImplementationImportCollector();
    implCollector.collect(unit);
    Set<Import> imports = hdrCollector.getForwardDeclarations();
    imports.addAll(hdrCollector.getSuperTypes());
    imports.addAll(implCollector.getImports());
    for (Import imp : imports) {
      ITypeBinding type = imp.getMainType();
      // Ignore core types.
      if (!(type instanceof IOSTypeBinding)) {
        closureQueue.addName(type.getErasure().getQualifiedName());
      }
    }
  }

  private TimeTracker getTicker(String name) {
    if (logger.isLoggable(Level.FINEST)) {
      return TimeTracker.start(name);
    } else {
      return TimeTracker.noop();
    }
  }

  static void printHeaderMappings() {
    if (Options.getOutputHeaderMappingFile() != null) {
      Map<String, String> headerMappings = Options.getHeaderMappings();
      File outputMappingFile = Options.getOutputHeaderMappingFile();

      try {
        if (!outputMappingFile.exists()) {
          outputMappingFile.getParentFile().mkdirs();
          outputMappingFile.createNewFile();
        }
        PrintWriter writer = new PrintWriter(outputMappingFile);

        for (String className : headerMappings.keySet()) {
          writer.println(String.format("%s=%s", className, headerMappings.get(className)));
        }

        writer.close();
      } catch (IOException e) {
        ErrorUtil.error(e.getMessage());
      }
    }
  }

  static void loadHeaderMappings() {
    Map<String, String> headerMappings = Options.getHeaderMappings();

    List<String> headerMappingFiles = Options.getHeaderMappingFiles();
    List<Properties> headerMappingProps = new ArrayList<Properties>();

    try {
      if (headerMappingFiles == null) {
        try {
          headerMappingProps.add(FileUtil.loadProperties(Options.DEFAULT_HEADER_MAPPING_FILE));
        } catch (FileNotFoundException e) {
          // Don't fail if mappings aren't configured and the default mapping is absent.
        }
      } else {
        for (String resourceName : headerMappingFiles) {
          headerMappingProps.add(FileUtil.loadProperties(resourceName));
        }
      }
    } catch (IOException e) {
      ErrorUtil.error(e.getMessage());
    }

    for (Properties mappings : headerMappingProps) {
      Enumeration<?> keyIterator = mappings.propertyNames();
      while (keyIterator.hasMoreElements()) {
        String key = (String) keyIterator.nextElement();
        headerMappings.put(key, mappings.getProperty(key));
      }
    }
  }
}
