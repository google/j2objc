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
import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.DebugASTDump;
import com.google.devtools.j2objc.gen.GenerationUnit;
import com.google.devtools.j2objc.gen.ObjectiveCHeaderGenerator;
import com.google.devtools.j2objc.gen.ObjectiveCImplementationGenerator;
import com.google.devtools.j2objc.gen.ObjectiveCMultiHeaderGenerator;
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
import com.google.devtools.j2objc.translate.ExternalAnnotationInjector;
import com.google.devtools.j2objc.translate.Functionizer;
import com.google.devtools.j2objc.translate.GwtConverter;
import com.google.devtools.j2objc.translate.InitializationNormalizer;
import com.google.devtools.j2objc.translate.InnerClassExtractor;
import com.google.devtools.j2objc.translate.JavaCloneWriter;
import com.google.devtools.j2objc.translate.JavaToIOSMethodTranslator;
import com.google.devtools.j2objc.translate.LabelRewriter;
import com.google.devtools.j2objc.translate.LambdaRewriter;
import com.google.devtools.j2objc.translate.LambdaTypeElementAdder;
import com.google.devtools.j2objc.translate.LogSiteInjector;
import com.google.devtools.j2objc.translate.MetadataWriter;
import com.google.devtools.j2objc.translate.NilCheckResolver;
import com.google.devtools.j2objc.translate.NumberMethodRewriter;
import com.google.devtools.j2objc.translate.ObjectiveCAdapterMethodAnnotation;
import com.google.devtools.j2objc.translate.ObjectiveCNativeProtocolAnnotation;
import com.google.devtools.j2objc.translate.OcniExtractor;
import com.google.devtools.j2objc.translate.OperatorRewriter;
import com.google.devtools.j2objc.translate.OuterReferenceResolver;
import com.google.devtools.j2objc.translate.PackageInfoRewriter;
import com.google.devtools.j2objc.translate.PrivateDeclarationResolver;
import com.google.devtools.j2objc.translate.RecordExpander;
import com.google.devtools.j2objc.translate.ReflectionCodeDetector;
import com.google.devtools.j2objc.translate.Rewriter;
import com.google.devtools.j2objc.translate.SerializationStripper;
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
import com.google.devtools.j2objc.util.ExternalAnnotations;
import com.google.devtools.j2objc.util.Parser;
import com.google.devtools.j2objc.util.TimeTracker;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
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
  private final List<GenerationUnit> outputs = new ArrayList<>();
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
      applyMutations(unit, deadCodeMap, options.externalAnnotations(), ticker);
      ticker.tick("Tree mutations");
      ticker.printResults(System.out);

      GenerationUnit genUnit = input.getGenerationUnit();
      genUnit.addCompilationUnit(unit);
      outputs.add(genUnit);

      // Add out-of-date dependencies to translation list.
      if (closureQueue != null) {
        checkDependencies(unit);
      }
    }
    processedCount++;
  }

  @Override
  protected void processOutputs(Iterable<ProcessingContext> outputs) {
    HashMultimap<String, String> headerIncludesMap = HashMultimap.create();
    for (ProcessingContext output : outputs) {
      generateObjectiveCSource(output.getGenerationUnit(), headerIncludesMap::put);
    }
    checkNoCycles(headerIncludesMap.asMap());
  }

  /**
   * Translates a parsed source file, modifying the compilation unit by substituting core Java type
   * and method references with iOS equivalents. For example, <code>java.lang.Object</code> maps to
   * <code>NSObject</code>, and <code>java.lang.String</code> to <code>NSString</code>. The source
   * is also modified to add support for iOS memory management, extract inner classes, etc.
   */
  public static void applyMutations(
      CompilationUnit unit,
      CodeReferenceMap deadCodeMap,
      ExternalAnnotations externalAnnotations,
      TimeTracker ticker) {
    ticker.push();

    // Before: OuterReferenceResolver - OuterReferenceResolver needs the bindings fixed.
    new LambdaTypeElementAdder(unit).run();
    ticker.tick("LambdaTypeElementAdder");

    DeadCodeEliminator deadCodeEliminator = new DeadCodeEliminator(unit, deadCodeMap);
    if (deadCodeMap != null) {
      deadCodeEliminator.run();
      ticker.tick("DeadCodeEliminator");
    }

    if (unit.getEnv().options().stripReflection()
        && unit.getEnv().options().stripReflectionErrors()) {
      new ReflectionCodeDetector(unit).run();
      ticker.tick("ReflectionCodeDetector");
    }

    LogSiteInjector logSiteInjector = new LogSiteInjector(unit);
    if (logSiteInjector.isEnabled()) {
      logSiteInjector.run();
      ticker.tick("CallSiteInjector");
    }

    new ExternalAnnotationInjector(unit, externalAnnotations).run();
    ticker.tick("ExternalAnnotationInjector");

    new OuterReferenceResolver(unit).run();
    ticker.tick("OuterReferenceResolver");

    new RecordExpander(unit).run();
    ticker.tick("RecordExpander");

    // Update code that has GWT references.
    new GwtConverter(unit).run();
    ticker.tick("GwtConverter");

    // Remove serialization related members if needed.
    new SerializationStripper(unit).run();
    ticker.tick("SerializationStripper");

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
    //   top-level and functionizing to have occurred.
    new PrivateDeclarationResolver(unit).run();
    ticker.tick("PrivateDeclarationResolver");

    // Add native protocols after all prior translation. Occurs before
    // adapter methods that may reference those protocols.
    new ObjectiveCNativeProtocolAnnotation(unit).run();
    ticker.tick("ObjectiveCNativeProtocolAnnotation");

    // After all methods are resolved and functionized, add adapter methods to
    // use their native types as annotated. Done last as the generated methods
    // do not need other processing above.
    new ObjectiveCAdapterMethodAnnotation(unit).run();
    ticker.tick("ObjectiveCAdapterMethodAnnotation");

    if (deadCodeMap != null) {
      deadCodeEliminator.removeDeadClasses();
      ticker.tick("removeDeadClasses");
    }

    // Make sure we still have a valid AST.
    unit.validate();

    ticker.pop();
  }

  @VisibleForTesting
  public static void generateObjectiveCSource(
      GenerationUnit unit, BiConsumer<String, String> headerIncludeCollector) {
    assert unit.getOutputPath() != null;
    assert unit.isFullyParsed();
    TimeTracker ticker = TimeTracker.getTicker(unit.getSourceName(), unit.options().timingLevel());
    logger.fine("Generating " + unit.getOutputPath());
    logger.finest("writing source file(s) to "
        + unit.options().fileUtil().getOutputDirectory().getAbsolutePath());
    logger.finest("writing header file(s) to "
        + unit.options().fileUtil().getHeaderOutputDirectory().getAbsolutePath());
    ticker.push();

    // write header(s)
    if (unit.options().generateSeparateHeaders()) {
      ObjectiveCMultiHeaderGenerator.generate(unit);
    } else if (unit.options().generateSegmentedHeaders()) {
      ObjectiveCSegmentedHeaderGenerator.generate(unit);
    } else {
      // Only need to populate headerIncludesMap in this case, since segmented or separate headers
      // cannot produce include cycles.
      ObjectiveCHeaderGenerator.generate(unit, headerIncludeCollector);
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

  @VisibleForTesting
  static void checkNoCycles(Map<String, Collection<String>> headerIncludesMap) {
    List<String> cycle = findCycle(headerIncludesMap);
    if (!cycle.isEmpty()) {
      throw new IllegalArgumentException(
          "This target contains an include cycle, but segmented headers are disabled. Enable"
              + " segmented headers and try again. Cycle:\n"
              + Joiner.on("\n").join(cycle));
    }
  }

  /**
   * Looks for a cycle in the map. If a cycle is found, the path argument is populated with the path
   * that found the cycle (in reverse order).
   */
  private static List<String> findCycle(Map<String, Collection<String>> headerIncludesMap) {
    HashSet<String> finished = new HashSet<>();
    HashSet<String> visited = new HashSet<>();
    ArrayList<String> path = new ArrayList<>();
    for (String header : headerIncludesMap.keySet()) {
      if (findCycle(headerIncludesMap, header, finished, visited, path)) {
        String cycleHead = path.get(0);
        return path.subList(0, path.lastIndexOf(cycleHead) + 1);
      }
    }
    return new ArrayList<>();
  }

  /**
   * Looks for a cycle in the map, starting at currentPath. If a cycle is found, the path argument
   * is populated with the path that found the cycle (in reverse order).
   */
  private static boolean findCycle(
      Map<String, Collection<String>> headerIncludesMap,
      String currentPath,
      Set<String> finished,
      Set<String> visited,
      List<String> path) {
    if (finished.contains(currentPath)) {
      return false;
    }
    if (visited.contains(currentPath)) {
      path.add(currentPath);
      return true;
    }
    visited.add(currentPath);
    Collection<String> includes = headerIncludesMap.get(currentPath);
    if (includes != null) {
      for (String include : includes) {
        if (findCycle(headerIncludesMap, include, finished, visited, path)) {
          path.add(currentPath);
          return true;
        }
      }
    }
    finished.add(currentPath);
    return false;
  }
}
