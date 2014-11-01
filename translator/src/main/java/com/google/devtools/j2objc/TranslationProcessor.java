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

import com.google.common.collect.Sets;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.TreeConverter;
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
import com.google.devtools.j2objc.translate.CopyAllFieldsWriter;
import com.google.devtools.j2objc.translate.DestructorGenerator;
import com.google.devtools.j2objc.translate.EnhancedForRewriter;
import com.google.devtools.j2objc.translate.EnumRewriter;
import com.google.devtools.j2objc.translate.Functionizer;
import com.google.devtools.j2objc.translate.GwtConverter;
import com.google.devtools.j2objc.translate.InitializationNormalizer;
import com.google.devtools.j2objc.translate.InnerClassExtractor;
import com.google.devtools.j2objc.translate.JavaToIOSMethodTranslator;
import com.google.devtools.j2objc.translate.NilCheckResolver;
import com.google.devtools.j2objc.translate.OcniExtractor;
import com.google.devtools.j2objc.translate.OperatorRewriter;
import com.google.devtools.j2objc.translate.OuterReferenceFixer;
import com.google.devtools.j2objc.translate.OuterReferenceResolver;
import com.google.devtools.j2objc.translate.Rewriter;
import com.google.devtools.j2objc.translate.StaticVarRewriter;
import com.google.devtools.j2objc.translate.TypeSorter;
import com.google.devtools.j2objc.translate.UnsequencedExpressionRewriter;
import com.google.devtools.j2objc.translate.VarargsRewriter;
import com.google.devtools.j2objc.translate.VariableRenamer;
import com.google.devtools.j2objc.types.HeaderImportCollector;
import com.google.devtools.j2objc.types.IOSTypeBinding;
import com.google.devtools.j2objc.types.ImplementationImportCollector;
import com.google.devtools.j2objc.types.Import;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.JdtParser;
import com.google.devtools.j2objc.util.TimeTracker;

import org.eclipse.jdt.core.dom.ITypeBinding;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Processes source files by translating each source into an Objective-C header
 * and an Objective-C source file.
 *
 * @author Tom Ball, Keith Stanger
 */
class TranslationProcessor extends FileProcessor {

  private static final Logger logger = Logger.getLogger(TranslationProcessor.class.getName());

  Queue<String> pendingFiles = new ArrayDeque<String>();
  // Relative paths of files that have been processed.
  Set<String> processedFiles = Sets.newHashSet();
  // Relative paths of files that have either been processed or added to pendingfiles.
  Set<String> seenFiles = Sets.newHashSet();

  public TranslationProcessor(JdtParser parser) {
    super(parser);
  }

  @Override
  public void processFiles(Iterable<String> files) {
    super.processFiles(files);
    if (Options.buildClosure()) {
      while (!pendingFiles.isEmpty()) {
        String file = pendingFiles.remove();
        if (!processedFiles.contains(file)) {
          processFile(file);
        }
      }
    }
  }

  @Override
  protected void processSource(String path, String source) {
    if (logger.isLoggable(Level.INFO)) {
      System.out.println("translating " + path);
    }
    super.processSource(path, source);
  }

  @Override
  protected void processUnit(
      String path, String source, org.eclipse.jdt.core.dom.CompilationUnit unit,
      TimeTracker ticker) {
    String relativePath = getRelativePath(path, unit);
    processedFiles.add(relativePath);
    seenFiles.add(relativePath);

    CompilationUnit newUnit = TreeConverter.convertCompilationUnit(unit, path, source);

    applyMutations(newUnit, ticker);
    ticker.tick("Tree mutations");

    if (unit.types().isEmpty() && !newUnit.getMainTypeName().endsWith("package_info")) {
      logger.finest("skipping dead file " + path);
      return;
    }

    logger.finest("writing output file(s) to " + Options.getOutputDirectory().getAbsolutePath());

    generateObjectiveCSource(newUnit, ticker);
    ticker.tick("Source generation");

    if (Options.buildClosure()) {
      // Add out-of-date dependencies to translation list.
      checkDependencies(newUnit);
    }

    OuterReferenceResolver.cleanup();
  }

  /**
   * Translates a parsed source file, modifying the compilation unit by
   * substituting core Java type and method references with iOS equivalents.
   * For example, <code>java.lang.Object</code> maps to <code>NSObject</code>,
   * and <code>java.lang.String</code> to <code>NSString</code>. The source is
   * also modified to add support for iOS memory management, extract inner
   * classes, etc.
   */
  public static void applyMutations(CompilationUnit unit, TimeTracker ticker) {
    ticker.push();

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

    // Reorders the types so that superclasses are declared before classes that
    // extend them.
    TypeSorter.sortTypes(unit);
    ticker.tick("TypeSorter");

    // Add dealloc/finalize method(s), if necessary.  This is done
    // after inner class extraction, so that each class releases
    // only its own instance variables.
    new DestructorGenerator().run(unit);
    ticker.tick("DestructorGenerator");

    new CopyAllFieldsWriter().run(unit);
    ticker.tick("CopyAllFieldsWriter");

    new ConstantBranchPruner().run(unit);
    ticker.tick("ConstantBranchPruner");

    new OcniExtractor(unit).run(unit);
    ticker.tick("OcniExtractor");

    // After: OcniExtractor - So that native methods can be correctly
    //   functionized.
    if (Options.finalMethodsAsFunctions()) {
      new Functionizer().run(unit);
      ticker.tick("Functionizer");
    }

    // After: Functionizer - Changes bindings on MethodDeclaration nodes.
    // Before: StaticVarRewriter, OperatorRewriter - Doesn't know how to handle
    //   the hasRetainedResult flag on ClassInstanceCreation nodes.
    Map<String, String> methodMappings = Options.getMethodMappings();
    if (methodMappings.isEmpty()) {
      // Method maps are loaded here so tests can call translate() directly.
      loadMappingFiles();
    }
    new JavaToIOSMethodTranslator(methodMappings).run(unit);
    ticker.tick("JavaToIOSMethodTranslator");

    new StaticVarRewriter().run(unit);
    ticker.tick("StaticVarRewriter");

    new OperatorRewriter().run(unit);
    ticker.tick("OperatorRewriter");

    // After: StaticVarRewriter, OperatorRewriter - They set the
    //   hasRetainedResult on ArrayCreation nodes.
    new ArrayRewriter().run(unit);
    ticker.tick("ArrayRewriter");

    new EnumRewriter().run(unit);
    ticker.tick("EnumRewriter");

    // Breaks up deeply nested expressions such as chained method calls.
    // Should be one of the last translations because other mutations will
    // affect how deep the expressions are.
    new ComplexExpressionExtractor().run(unit);
    ticker.tick("ComplexExpressionExtractor");

    // Should be one of the last translations because methods and functions
    // added in other phases may need added casts.
    new CastResolver().run(unit);
    ticker.tick("CastResolver");

    for (Plugin plugin : Options.getPlugins()) {
      plugin.processUnit(unit);
    }

    // Make sure we still have a valid AST.
    unit.validate();

    ticker.pop();
  }

  public static void generateObjectiveCSource(CompilationUnit unit, TimeTracker ticker) {
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

  public void postProcess() {
    for (Plugin plugin : Options.getPlugins()) {
      plugin.endProcessing(Options.getOutputDirectory());
    }
    if (logger.isLoggable(Level.INFO)) {
      int nFiles = processedFiles.size();
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
    HeaderImportCollector hdrCollector = new HeaderImportCollector();
    hdrCollector.collect(unit);
    ImplementationImportCollector implCollector = new ImplementationImportCollector();
    implCollector.collect(unit);
    Set<Import> imports = hdrCollector.getForwardDeclarations();
    imports.addAll(hdrCollector.getSuperTypes());
    imports.addAll(implCollector.getImports());
    for (Import imp : imports) {
      maybeAddToClosure(imp.getType());
    }
  }

  private void maybeAddToClosure(ITypeBinding type) {
    if (type instanceof IOSTypeBinding) {
      return;  // Ignore core types.
    }
    String typeName = type.getErasure().getQualifiedName();
    String sourceName = typeName.replace('.', File.separatorChar) + ".java";
    if (seenFiles.contains(sourceName)) {
      return;
    }
    seenFiles.add(sourceName);

    // Check if source file exists.
    String originalTypeName = typeName;
    File sourceFile = findSourceFile(sourceName);
    while (sourceFile == null && !sourceName.isEmpty()) {
      // Check if class exists on classpath.
      if (findClassFile(typeName)) {
        logger.finest("no source for " + typeName + ", class found");
        return;
      }
      int iDot = typeName.lastIndexOf('.');
      if (iDot == -1) {
        ErrorUtil.warning("could not find source path for " + originalTypeName);
        return;
      }
      typeName = typeName.substring(0, iDot);
      sourceName = typeName.replace('.', File.pathSeparatorChar) + ".java";
      if (seenFiles.contains(sourceName)) {
        return;
      }
      sourceFile = findSourceFile(sourceName);
    }

    // Check if the source file is older than the generated header file.
    File headerSource = new File(Options.getOutputDirectory(), sourceName.replace(".java", ".h"));
    if (headerSource.exists() && sourceFile.lastModified() < headerSource.lastModified()) {
      return;
    }
    pendingFiles.add(sourceName);
  }

  private File findSourceFile(String path) {
    for (String sourcePath : Options.getSourcePathEntries()) {
      File f = findFile(path, sourcePath);
      if (f != null) {
        return f;
      }
    }
    return null;
  }

  private boolean findClassFile(String typeName) {
    // Zip/jar files always use forward slashes.
    String path = typeName.replace('.', '/') + ".class";
    for (String classPath : Options.getClassPathEntries()) {
      File f = findFile(path, classPath);
      if (f != null) {
        return true;
      }
    }
    // See if it's a JRE class.
    try {
      Class.forName(typeName);
      return true;
    } catch (ClassNotFoundException e) {
      // Fall-through.
    }
    return false;
  }

  private File findFile(String path, String sourcePath) {
    File f = new File(sourcePath);
    if (f.isDirectory()) {
      File source = new File(f, path);
      if (source.exists()) {
        return source;
      }
    } else if (f.isFile() && sourcePath.endsWith(".jar")) {
      try {
        ZipFile zfile = new ZipFile(f);
        try {
          ZipEntry entry = zfile.getEntry(path);
          if (entry != null) {
            return f;
          }
        } finally {
          zfile.close();
        }
      } catch (IOException e) {
        ErrorUtil.warning(e.getMessage());
      }
    }
    return null;
  }

  private static void loadMappingFiles() {
    for (String resourceName : Options.getMappingFiles()) {
      Properties mappings = new Properties();
      try {
        File f = new File(resourceName);
        if (f.exists()) {
          FileReader reader = new FileReader(f);
          try {
            mappings.load(reader);
          } finally {
            reader.close();
          }
        } else {
          InputStream stream = J2ObjC.class.getResourceAsStream(resourceName);
          if (stream == null) {
            ErrorUtil.error(resourceName + " not found");
          } else {
            try {
              mappings.load(stream);
            } finally {
              stream.close();
            }
          }
        }
      } catch (IOException e) {
        throw new AssertionError(e);
      }

      Enumeration<?> keyIterator = mappings.propertyNames();
      while (keyIterator.hasMoreElements()) {
        String key = (String) keyIterator.nextElement();
        if (key.indexOf('(') > 0) {
          // All method mappings have parentheses characters, classes don't.
          String iosMethod = mappings.getProperty(key);
          Options.getMethodMappings().put(key, iosMethod);
        } else {
          String iosClass = mappings.getProperty(key);
          Options.getClassMappings().put(key, iosClass);
        }
      }
    }
  }
}
