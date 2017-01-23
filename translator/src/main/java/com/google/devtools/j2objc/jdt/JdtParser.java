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

package com.google.devtools.j2objc.jdt;

import com.google.common.collect.ImmutableMap;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.Options.LintOption;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.file.RegularInputFile;
import com.google.devtools.j2objc.pipeline.ProcessingContext;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.FileUtil;
import com.google.devtools.j2objc.util.Parser;
import com.google.devtools.j2objc.util.ParserEnvironment;
import com.google.devtools.j2objc.util.SourceVersion;
import com.google.devtools.j2objc.util.TranslationEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Adapts JDT's ASTParser to a more convenient interface for parsing source
 * strings into CompilationUnit trees.
 *
 * @author Tom Ball, Keith Stanger
 */
public class JdtParser extends Parser {

  private static final Logger logger = Logger.getLogger(JdtParser.class.getName());

  private Map<String, String> compilerOptions =
      initCompilerOptions(options.getSourceVersion(), options.lintOptions());

  private static Map<String, String> initCompilerOptions(
      SourceVersion sourceVersion, EnumSet<LintOption> lintOptions) {
    Map<String, String> compilerOptions = new HashMap<>();
    String version = sourceVersion.flag();
    compilerOptions.put(org.eclipse.jdt.core.JavaCore.COMPILER_SOURCE, version);
    compilerOptions.put(org.eclipse.jdt.core.JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, version);
    compilerOptions.put(org.eclipse.jdt.core.JavaCore.COMPILER_COMPLIANCE, version);

    // Turn on any specified lint warnings.
    for (Options.LintOption lintOption : lintOptions) {
      compilerOptions.put(lintOption.jdtFlag(), "warning");
    }

    // Turn off any warnings on by default but not requested.
    for (Options.LintOption lintOption : EnumSet.complementOf(lintOptions)) {
      compilerOptions.put(lintOption.jdtFlag(), "ignore");
    }
    return compilerOptions;
  }

  public JdtParser(Options options){
    super(options);
  }

  @Override
  public void addClasspathEntry(String entry) {
    if (isValidPathEntry(entry)) {
      classpathEntries.add(entry);
    }
  }

  @Override
  public void addSourcepathEntry(String entry) {
    if (isValidPathEntry(entry)) {
      sourcepathEntries.add(entry);
    }
  }

  @Override
  public void prependSourcepathEntry(String entry) {
    if (isValidPathEntry(entry)) {
      sourcepathEntries.add(0, entry);
    }
  }

  @Override
  public void setEnableDocComments(boolean enable) {
    // BodyDeclaration.getJavadoc() always returns null without this option enabled,
    // so by default no doc comments are generated.
    compilerOptions.put(
        org.eclipse.jdt.core.JavaCore.COMPILER_DOC_COMMENT_SUPPORT,
        enable ? "enabled" : "disabled");
  }

  @Override
  public Parser.ParseResult parseWithoutBindings(InputFile file, String source) {
    CompilationUnit unit = parse(file.getUnitName(), source, false);
    return new JdtParseResult(file, source, unit);
  }

  @Override
  public com.google.devtools.j2objc.ast.CompilationUnit parse(InputFile file) {
    String source = null;
    try {
      source = options.fileUtil().readFile(file);
      return parse(FileUtil.getMainTypeName(file), file.getUnitName(), source);
    } catch (IOException e) {
      ErrorUtil.error(e.getMessage());
      return null;
    }
  }

  @Override
  public com.google.devtools.j2objc.ast.CompilationUnit parse(String mainTypeName,
      String path, String source) {
    int errors = ErrorUtil.errorCount();
    org.eclipse.jdt.core.dom.CompilationUnit unit = parse(path, source, true);
    if (ErrorUtil.errorCount() > errors) {
      return null;
    }
    if (mainTypeName == null) {
      RegularInputFile file = new RegularInputFile(path);
      mainTypeName = FileUtil.getQualifiedMainTypeName(file, unit);
    }
    ParserEnvironment parserEnv = new JdtParserEnvironment(unit.getAST());
    TranslationEnvironment env = new TranslationEnvironment(options, parserEnv);
    return TreeConverter.convertCompilationUnit(env, unit, path, mainTypeName, source);
  }

  private CompilationUnit parse(String unitName, String source, boolean resolveBindings) {
    ASTParser parser = newASTParser(resolveBindings, options.getSourceVersion());
    parser.setUnitName(unitName);
    parser.setSource(source.toCharArray());
    CompilationUnit unit = (CompilationUnit) parser.createAST(null);
    if (checkCompilationErrors(unitName, unit)) {
      return unit;
    } else {
      return null;
    }
  }

  @Override
  public void parseFiles(Collection<String> paths, final Handler handler,
      SourceVersion sourceVersion) {
    ASTParser parser = newASTParser(true, sourceVersion);
    FileASTRequestor astRequestor = new FileASTRequestor() {
      @Override
      public void acceptAST(String sourceFilePath, CompilationUnit ast) {
        logger.fine("acceptAST: " + sourceFilePath);
        if (checkCompilationErrors(sourceFilePath, ast)) {
          RegularInputFile file = new RegularInputFile(sourceFilePath);
          try {
            String source = options.fileUtil().readFile(file);
            ParserEnvironment parserEnv = new JdtParserEnvironment(ast.getAST());
            TranslationEnvironment env = new TranslationEnvironment(options, parserEnv);
            com.google.devtools.j2objc.ast.CompilationUnit unit =
                TreeConverter.convertCompilationUnit(
                    env, ast, sourceFilePath, FileUtil.getMainTypeName(file), source);
            handler.handleParsedUnit(sourceFilePath, unit);
          } catch (IOException e) {
            ErrorUtil.error(
                "Error reading file " + file.getOriginalLocation() + ": " + e.getMessage());
          }
        }
      }
    };
    // JDT fails to resolve all secondary bindings unless there are the same
    // number of "binding key" strings as source files. It doesn't appear to
    // matter what the binding key strings should be (as long as they're non-
    // null), so the paths array is reused.
    String[] pathsArray = paths.toArray(new String[paths.size()]);
    parser.createASTs(pathsArray, getEncodings(pathsArray.length), pathsArray, astRequestor, null);
  }

  @Override
  public ProcessingResult processAnnotations(Iterable<String> fileArgs,
      List<ProcessingContext> inputs) {
    AnnotationPreProcessor preProcessor = new AnnotationPreProcessor(options);
    final List<ProcessingContext> generatedInputs = preProcessor.process(fileArgs, inputs);
    return new Parser.ProcessingResult() {
      @Override
      public File getSourceOutputDirectory() {
        return preProcessor.getTemporaryDirectory();
      }

      @Override
      public List<ProcessingContext> getGeneratedSources() {
        return generatedInputs;
      }
    };
  }

  @SuppressWarnings("deprecation")
  private ASTParser newASTParser(boolean resolveBindings, SourceVersion sourceVersion) {
    ASTParser parser;
    if (SourceVersion.java8Minimum(sourceVersion)) {
      parser = ASTParser.newParser(AST.JLS8);
    } else {
      parser = ASTParser.newParser(AST.JLS4); // Java 7
    }

    parser.setCompilerOptions(compilerOptions);
    parser.setResolveBindings(resolveBindings);
    parser.setEnvironment(
        toArray(classpathEntries), toArray(sourcepathEntries),
        getEncodings(sourcepathEntries.size()), includeRunningVMBootclasspath);
    return parser;
  }

  private String[] toArray(List<String> list) {
    return list.toArray(new String[list.size()]);
  }

  private boolean isValidPathEntry(String path) {
    // JDT requires that all path elements exist and can hold class files.
    File f = new File(path);
    return f.exists() && (f.isDirectory() || path.endsWith(".jar"));
  }

  private String[] getEncodings(int length) {
    String encoding = options.fileUtil().getFileEncoding();
    if (encoding == null) {
      return null;
    }
    String[] encodings = new String[length];
    Arrays.fill(encodings, encoding);
    return encodings;
  }

  private boolean checkCompilationErrors(String filename, CompilationUnit unit) {
    boolean hasErrors = false;
    for (IProblem problem : unit.getProblems()) {
      if (problem.isError()) {
        ErrorUtil.error(String.format(
            "%s:%s: %s", filename, problem.getSourceLineNumber(), problem.getMessage()));
        hasErrors = true;
      }
      if (problem.isWarning()) {
        ErrorUtil.warning(String.format(
            "%s:%s: warning: %s", filename, problem.getSourceLineNumber(), problem.getMessage()));
      }
    }
    return !hasErrors;
  }

  private static class JdtParserEnvironment implements ParserEnvironment {

    private final AST ast;
    private final Types types;
    private final Map<String, Element> knownTypes;

    JdtParserEnvironment(AST ast) {
      this.ast = ast;
      types = new JdtTypes(ast);

      ITypeBinding javaLangInteger = ast.resolveWellKnownType("java.lang.Integer");
      knownTypes = ImmutableMap.of(
          "java.lang.Number", BindingConverter.getTypeElement(javaLangInteger.getSuperclass()));
    }

    @Override
    public Element resolve(String name) {
      Element result = knownTypes.get(name);
      return result != null ? result : BindingConverter.getElement(ast.resolveWellKnownType(name));
    }

    @Override
    public Elements elementUtilities() {
      return JdtElements.INSTANCE;
    }

    @Override
    public Types typeUtilities() {
      return types;
    }

    @Override
    public void reset() {
      BindingConverter.reset();
    }
  }

  private static class JdtParseResult implements Parser.ParseResult {
    private final InputFile file;
    private String source;
    private final CompilationUnit unit;

    private JdtParseResult(InputFile file, String source, CompilationUnit unit) {
      super();
      this.file = file;
      this.source = source;
      this.unit = unit;
    }

    @Override
    public void stripIncompatibleSource() {
      source = JdtJ2ObjCIncompatibleStripper.strip(source, unit);
    }

    @Override
    public String getSource() {
      return source;
    }

    @Override
    public String mainTypeName() {
      String qualifiedName = FileUtil.getMainTypeName(file);
      org.eclipse.jdt.core.dom.PackageDeclaration packageDecl = unit.getPackage();
      if (packageDecl != null) {
        String packageName = packageDecl.getName().getFullyQualifiedName();
        qualifiedName = packageName + "." + qualifiedName;
      }
      return qualifiedName;
    }

    @Override
    public String toString() {
      return unit.toString();
    }
  }
}
