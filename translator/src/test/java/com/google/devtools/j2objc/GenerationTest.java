/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
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

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.SignatureASTPrinter;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.file.RegularInputFile;
import com.google.devtools.j2objc.gen.GenerationUnit;
import com.google.devtools.j2objc.gen.SourceBuilder;
import com.google.devtools.j2objc.gen.StatementGenerator;
import com.google.devtools.j2objc.pipeline.GenerationBatch;
import com.google.devtools.j2objc.pipeline.InputFilePreprocessor;
import com.google.devtools.j2objc.pipeline.ProcessingContext;
import com.google.devtools.j2objc.pipeline.TranslationProcessor;
import com.google.devtools.j2objc.util.CodeReferenceMap;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.FileUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.Parser;
import com.google.devtools.j2objc.util.SourceVersion;
import com.google.devtools.j2objc.util.TimeTracker;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import junit.framework.TestCase;

/**
 * Test support and assertions for j2objc code generation. The general design involves transpiling a
 * string containing source code from one or more Java source file snippets, then verifying that the
 * iOS code is generated as expected. Options (flags) may be set before transpilation. When a test
 * fails, the full generated .m or .h file is included in the failure message.
 *
 * @author Tom Ball
 */
public abstract class GenerationTest extends TestCase {

  protected File tempDir;
  protected Parser parser;
  protected Options options;
  private CodeReferenceMap deadCodeMap = null;
  private List<String> javacFlags = new ArrayList<>();

  static {
    // Prevents errors and warnings from being printed to the console.
    ErrorUtil.setTestMode();
    ClassLoader cl = GenerationTest.class.getClassLoader();
    cl.setPackageAssertionStatus("com.google.devtools.j2objc", true);
  }

  @Override
  protected void setUp() throws IOException {
    tempDir = FileUtil.createTempDir("testout");
    if (onJava11OrAbove()) {
      SourceVersion.setMaxSupportedVersion(SourceVersion.JAVA_11);
    }
    loadOptions();
    createParser();
  }

  @Override
  protected void tearDown() throws Exception {
    options = null;
    if (parser != null) {
      parser.close();
    }
    FileUtil.deleteTempDir(tempDir);
    ErrorUtil.reset();
  }

  protected void loadOptions() throws IOException {
    options = new Options();
    String tempPath = tempDir.getAbsolutePath();
    options.load(new String[]{
        "-d", tempPath,
        "-sourcepath", tempPath,
        "-classpath", tempPath,
        "-q", // Suppress console output.
        "-encoding", "UTF-8" // Translate strings correctly when encodings are nonstandard.
    });
  }

  protected void createParser() {
    parser = initializeParser(tempDir, options);
  }

  protected static Parser initializeParser(File tempDir, Options options) {
    Parser parser = Parser.newParser(options);
    parser.addClasspathEntries(getComGoogleDevtoolsJ2objcPath());
    parser.addSourcepathEntry(tempDir.getAbsolutePath());
    return parser;
  }

  protected void setDeadCodeMap(CodeReferenceMap deadCodeMap) {
    this.deadCodeMap = deadCodeMap;
  }

  protected void addSourcesToSourcepaths() throws IOException {
    options.fileUtil().getSourcePathEntries().add(tempDir.getCanonicalPath());
  }

  /**
   * Translate a string of Java statement(s) into a list of
   * JDT DOM Statements.  Although JDT has support for statement
   * parsing, it doesn't resolve them.  The statements are therefore
   * wrapped in a type declaration so they having bindings.
   */
  protected List<Statement> translateStatements(String stmts) {
    // Wrap statements in test class, so type resolution works.
    String source = "public class Test { void test() { " + stmts + "}}";
    CompilationUnit unit = translateType("Test", source);
    final List<Statement> statements = Lists.newArrayList();
    unit.accept(new TreeVisitor() {
      @Override
      public boolean visit(MethodDeclaration node) {
        if (ElementUtil.getName(node.getExecutableElement()).equals("test")) {
          statements.addAll(node.getBody().getStatements());
        }
        return false;
      }
    });
    return statements;
  }

  /**
   * Translates Java source, as contained in a source file.
   *
   * @param typeName the name of the public type being declared
   * @param source the source code
   * @return the translated compilation unit
   */
  protected CompilationUnit translateType(String typeName, String source) {
    CompilationUnit newUnit = compileType(typeName, source);
    TranslationProcessor.applyMutations(
        newUnit, deadCodeMap, options.externalAnnotations(), TimeTracker.noop());
    return newUnit;
  }

  protected String typeNameToSource(String name) {
    return name.replace('.', '/') + ".java";
  }

  protected CompilationUnit maybeCompileType(String name, String source) {
    String mainTypeName = name.substring(name.lastIndexOf('.') + 1);
    String path = typeNameToSource(name);
    parser.setEnableDocComments(options.docCommentsEnabled());
    return parser.parse(mainTypeName, path, source);
  }

  /**
   * Compiles Java source, as contained in a source file.
   *
   * @param name the name of the public type being declared
   * @param source the source code
   * @return the parsed compilation unit
   */
  protected CompilationUnit compileType(String name, String source) {
    int errors = ErrorUtil.errorCount();
    CompilationUnit unit = maybeCompileType(name, source);
    if (ErrorUtil.errorCount() > errors) {
      int newErrorCount = ErrorUtil.errorCount() - errors;
      String info = String.format(
          "%d test compilation error%s", newErrorCount, (newErrorCount == 1 ? "" : "s"));
      failWithMessages(info, ErrorUtil.getErrorMessages().subList(errors, ErrorUtil.errorCount()));
    }
    return unit;
  }

  /**
   * Compiles Java source to a JVM class file, then converts it to a CompilationUnit. The class
   * file is compiled with parameter name and debug attributes.
   *
   * @param name the name of the public type being declared
   * @param source the source code
   * @return the parsed compilation unit
   */
  protected CompilationUnit compileAsClassFile(String name, String source) throws IOException {
    return compileAsClassFile(name, source, javacFlags.toArray(new String[0]));
  }

  /**
   * Compiles Java source to a JVM class file, then converts it to a CompilationUnit.
   *
   * @param name the name of the public type being declared
   * @param source the source code
   * @param flags which javac flags to use
   * @return the parsed compilation unit
   */
  protected CompilationUnit compileAsClassFile(String name, String source,
      String... flags) throws IOException {
    assertTrue("Classfile translation not enabled", options.translateClassfiles());
    InputFile input = createClassFile(name, source, flags);
    if (input == null) {
      // Class file compilation failed.
      return null;
    }
    int errors = ErrorUtil.errorCount();
    CompilationUnit unit = parser.parse(input);
    if (ErrorUtil.errorCount() > errors) {
      int newErrorCount = ErrorUtil.errorCount() - errors;
      String info = String.format(
          "%d test compilation error%s", newErrorCount, (newErrorCount == 1 ? "" : "s"));
      failWithMessages(info, ErrorUtil.getErrorMessages().subList(errors, ErrorUtil.errorCount()));
    }
    return unit;
  }

  /**
   * Compiles Java source to a JVM class file.
   *
   * @param typeName the name of the type being declared
   * @param source the source code
   * @param flags which javac flags to use
   * @return the InputFile defining the class file
   */
  protected InputFile createClassFile(String typeName, String source, String... flags)
      throws IOException {
    String path = typeNameToSource(typeName);
    File srcFile = new File(tempDir, path);
    srcFile.getParentFile().mkdirs();
    try (BufferedWriter fw = Files.newWriter(srcFile, UTF_8)) {
      fw.write(source);
    }

    List<String> args = new ArrayList<>(Arrays.asList(flags));
    String tempPath = tempDir.getAbsolutePath();
    if (!args.contains("-d")) {
      args.add("-d");
      args.add(tempPath);
    }
    if (!args.contains("-classpath") && !args.contains("-cp")) {
      args.add("-classpath");
      List<String> classpath = getComGoogleDevtoolsJ2objcPath();
      classpath.add(0, tempPath);
      args.add(String.join(":", classpath));
    }
    if (options.emitLineDirectives() && !args.contains("-g")) {
      args.add("-g");
    }
    args.add(srcFile.getPath());

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    int numErrors = compiler.run(null, null, errOut, args.toArray(new String[0]));
    if (numErrors > 0) {
      String errMsg = errOut.toString();
      ErrorUtil.error(errMsg);
      failWithMessages("test compilation error" + (numErrors > 1 ? "s" : ""),
          Lists.newArrayList(errMsg));
      return null;
    }
    File classFile = new File(tempDir, path.replace(".java", ".class"));
    assertTrue(classFile.exists());

    return new RegularInputFile(classFile.getAbsolutePath(), typeName);
  }

  protected static List<String> getComGoogleDevtoolsJ2objcPath() {
    ClassLoader loader = GenerationTest.class.getClassLoader();
    List<String> classpath = Lists.newArrayList();

    if (loader instanceof URLClassLoader) {
      URL[] urls = ((URLClassLoader) GenerationTest.class.getClassLoader()).getURLs();
      String encoding = System.getProperty("file.encoding");
      for (int i = 0; i < urls.length; i++) {
        try {
          classpath.add(URLDecoder.decode(urls[i].getFile(), encoding));
        } catch (UnsupportedEncodingException e) {
          throw new AssertionError("System doesn't have the default encoding");
        }
      }
    } else {
      // Use the classpath for Java 9+.
      for (String path : Splitter.on(':').split(System.getProperty("java.class.path"))) {
        classpath.add(path);
      }
    }

    return classpath;
  }

  protected String generateStatement(Statement statement) {
    return StatementGenerator.generate(statement, SourceBuilder.BEGINNING_OF_FILE).trim();
  }

  /**
   * Asserts that translated source contains a specified string.
   */
  protected void assertTranslation(String translation, String expected) {
    if (!translation.contains(expected)) {
      fail("expected:\"" + expected + "\" in:\n" + translation);
    }
  }

  protected void assertNotInTranslation(String translation, String notExpected) {
    if (translation.contains(notExpected)) {
      fail("NOT expected:\"" + notExpected + "\" in:\n" + translation);
    }
  }

  /**
   * Asserts that translated source contains an ordered, consecutive list of lines
   * (each line's leading and trailing whitespace is ignored).
   */
  protected void assertTranslatedLines(String translation, String... expectedLines)
      throws IOException {
    int nLines = expectedLines.length;
    if (nLines < 2) {
      assertTranslation(translation, nLines == 1 ? expectedLines[0] : null);
      return;
    }
    int unmatchedLineIndex = unmatchedLineIndex(translation, expectedLines);
    if (unmatchedLineIndex != -1) {
      fail("unmatched:\n\"" + expectedLines[unmatchedLineIndex] + "\"\n" + "expected lines:\n\""
          + Joiner.on('\n').join(expectedLines) + "\"\nin:\n" + translation);
    }
  }

  private int unmatchedLineIndex(String s, String[] lines) throws IOException {
    int index = s.indexOf(lines[0]);
    if (index == -1) {
      return 0;
    }
    BufferedReader in = new BufferedReader(new StringReader(s.substring(index)));
    try {
      for (int i = 0; i < lines.length; i++) {
        String nextLine = in.readLine();
        if (nextLine == null) {
          return i;
        }
        if (!nextLine.trim().equals(lines[i].trim())) {
          // Check if there is a subsequent match.
          int subsequentMatch = unmatchedLineIndex(s.substring(index + 1), lines);
          if (subsequentMatch == -1) {
            return -1;
          }
          return Math.max(i, subsequentMatch);
        }
      }
      return -1;
    } finally {
      in.close();
    }
  }

  /**
   * Asserts that translated source contains a list of strings in order, but not necessarily
   * consecutive. Differs from assertTranslatedLines in that it doesn't match entire lines, and that
   * matches may occur anywhere forward in the string from the last match, not solely in the next
   * line.
   */
  protected void assertTranslatedSegments(String translation, String... expectedLines)
      throws IOException {
    int nLines = expectedLines.length;
    if (nLines < 2) {
      assertTranslation(translation, nLines == 1 ? expectedLines[0] : null);
      return;
    }
    String incorrectSegment = firstIncorrectSegment(translation, expectedLines);
    if (incorrectSegment != null) {
      fail("unmatched:\n\"" + incorrectSegment + "\"\n" + "expected segments:\n\""
          + Joiner.on('\n').join(expectedLines) + "\"\nin:\n" + translation);
    }
  }

  private String firstIncorrectSegment(String s, String[] lines) {
    int index = 0;
    for (int i = 0; i < lines.length; i++) {
      index = s.indexOf(lines[i], index);
      if (index == -1) {
        return lines[i];
      } else {
        index += lines[i].length();
      }
    }
    return null;
  }

  protected void assertOccurrences(String translation, String expected, int times) {
    Matcher matcher = Pattern.compile(Pattern.quote(expected)).matcher(translation);
    int count = 0;
    for (; matcher.find(); count++) {}
    if (count != times) {
      fail("expected:\"" + expected + "\" " + times + " times in:\n" + translation);
    }
  }

  /**
   * Verify that two AST nodes are equal, by comparing their toString() outputs.
   */
  protected void assertEqualASTs(TreeNode first, TreeNode second) {
    if (!first.toString().equals(second.toString())) {
      fail("unmatched:\n" + first + "vs:\n" + second);
    }
  }

  /**
   * Verify that two AST nodes are equal excepting MethodDeclaration bodies, by comparing their
   * signatures.
   */
  protected void assertEqualSignatures(TreeNode first, TreeNode second) {
    String firstStr = SignatureASTPrinter.toString(first);
    String secondStr = SignatureASTPrinter.toString(second);
    if (!firstStr.equals(secondStr)) {
      fail("unmatched:\n" + firstStr + "vs:\n" + secondStr);
    }
  }

  /**
   * Compiles Java source, as contained in a source file, and compares the generated Objective C
   * from the source and class files.
   *
   * @param type the public type being declared
   * @param source the source code
   */
  protected void assertEqualSrcClassfile(String type, String source) throws IOException {
    options.setEmitSourceHeaders(false);
    String fileRoot = type.replace('.', '/');
    CompilationUnit srcUnit = translateType(type, source);
    String srcHeader = generateFromUnit(srcUnit, fileRoot + ".h");
    String srcImpl = getTranslatedFile(fileRoot + ".m");

    Options.OutputLanguageOption language = options.getLanguage();
    options.setOutputLanguage(Options.OutputLanguageOption.TEST_OBJECTIVE_C);
    CompilationUnit classfileUnit = compileAsClassFile(fileRoot, source);
    TranslationProcessor.applyMutations(
        classfileUnit, deadCodeMap, options.externalAnnotations(), TimeTracker.noop());
    String clsHeader = generateFromUnit(classfileUnit, fileRoot + ".h2");
    String clsImpl = getTranslatedFile(fileRoot + ".m2");
    options.setOutputLanguage(language);
    if (!srcHeader.equals(clsHeader)) {
      fail("source and classfile headers differ:\n" + diff(fileRoot + ".h", fileRoot + ".h2"));
    }
    if (!srcImpl.equals(clsImpl)) {
      fail("source and classfile impls differ:\n" + diff(fileRoot + ".m", fileRoot + ".m2"));
    }
  }

  /**
   * Invoke diff on two generated files and return its report.
   */
  protected String diff(String file1, String file2) {
    File f1 = new File(tempDir, file1);
    File f2 = new File(tempDir, file2);
    ProcessBuilder pb = new ProcessBuilder("/usr/bin/diff",
        f1.getAbsolutePath(), f2.getAbsolutePath()).inheritIO();
    pb.redirectErrorStream(true);
    File log = new File(tempDir, "result.diff");
    pb.redirectOutput(ProcessBuilder.Redirect.to(log));
    try {
      Process p = pb.start();
      p.waitFor();
      String s = Files.asCharSource(log, options.fileUtil().getCharset()).read();
      return s;
    } catch (InterruptedException | IOException e) {
      return "failed diffing generated files: " + e;
    }
  }

  /**
   * Translate a Java method into a JDT DOM MethodDeclaration.  Although JDT
   * has support for parsing methods, it doesn't resolve them.  The statements
   * are therefore wrapped in a type declaration so they having bindings.
   */
  protected MethodDeclaration translateMethod(String method) {
    // Wrap statements in test class, so type resolution works.
    String source = "public class Test { " + method + " }";
    CompilationUnit unit = translateType("Test", source);
    final MethodDeclaration[] result = new MethodDeclaration[1];
    unit.accept(
        new TreeVisitor() {
          @Override
          public boolean visit(MethodDeclaration node) {
            String name = ElementUtil.getName(node.getExecutableElement());
            if (name.equals(NameTable.INIT_NAME)
                || name.equals(NameTable.FINALIZE_METHOD)
                || name.equals(NameTable.DEALLOC_METHOD)) {
              return false;
            }
            assertThat(result[0]).isNull();
            result[0] = node;
            return false;
          }
        });
    return result[0];
  }

  /**
   * Translate a Java source file contents, returning the contents of either
   * the generated header or implementation file.
   *
   * @param typeName the name of the main type defined by this source file
   * @param fileName the name of the file whose contents should be returned,
   *                 which is either the Obj-C header or implementation file
   */
  protected String translateSourceFile(String typeName, String fileName) throws IOException {
    String source = getTranslatedFile(typeNameToSource(typeName));
    return translateSourceFile(source, typeName, fileName);
  }

  /**
   * Translate a Java source file contents, returning the contents of either
   * the generated header or implementation file.
   *
   * @param source the source file contents
   * @param typeName the name of the main type defined by this source file
   * @param fileName the name of the file whose contents should be returned,
   *                 which is either the Obj-C header or implementation file
   */
  protected String translateSourceFile(String source, String typeName, String fileName)
      throws IOException {
    return generateFromUnit(translateType(typeName, source), fileName);
  }

  /**
   * Similar to the versions above, but it does not generate an in-memory file.
   *
   * @param inputFile the name of the file to be transpiled
   * @param outputFile the name of the file whose contents should be returned,
   *                   which is either the Obj-C header or implementation file
   */
  protected String translateSourceFileNoInMemory(String inputFile, String outputFile)
      throws IOException {
    Parser.Handler handler = (String path, CompilationUnit newUnit) -> {
      try {
        TranslationProcessor.applyMutations(
            newUnit, deadCodeMap, options.externalAnnotations(), TimeTracker.noop());
        generateFromUnit(newUnit, outputFile);
      } catch (IOException e) {
        // Ignore.
      }
    };
    parser.parseFiles(Arrays.asList(inputFile), handler, null);
    if (ErrorUtil.errorCount() > 0) {
      failWithMessages("Compilation errors:", ErrorUtil.getErrorMessages());
    }
    return getTranslatedFile(outputFile);
  }

  /**
   * Compile Java source and translate the resulting class file, returning the
   * contents of either the generated header or implementation file.
   *
   * @param source the Java source to compile
   * @param typeName the name of the main type defined by this source file
   * @param fileName the name of the file whose contents should be returned,
   *                 which is either the Obj-C header or implementation file
   */
  protected String compileAndTranslateSourceFile(String source, String typeName, String fileName)
      throws IOException {
    CompilationUnit newUnit = compileAsClassFile(typeName, source);
    TranslationProcessor.applyMutations(
        newUnit, deadCodeMap, options.externalAnnotations(), TimeTracker.noop());
    return generateFromUnit(newUnit, fileName);
  }

  protected String generateFromUnit(CompilationUnit unit, String filename) throws IOException {
    GenerationUnit genUnit = new GenerationUnit(unit.getSourceFilePath(), options);
    genUnit.incrementInputs();
    genUnit.addCompilationUnit(unit);
    TranslationProcessor.generateObjectiveCSource(genUnit, (headerFile, include) -> {});
    return getTranslatedFile(filename);
  }

  protected String translateCombinedFiles(String outputPath, String extension, String... sources)
      throws IOException {
    List<ProcessingContext> inputs = new ArrayList<>();
    GenerationUnit genUnit = GenerationUnit.newCombinedJarUnit(outputPath + ".testfile", options);
    for (String sourceFile : sources) {
      inputs.add(new ProcessingContext(
          new RegularInputFile(tempDir + "/" + sourceFile, sourceFile), genUnit));
    }
    parser.setEnableDocComments(options.docCommentsEnabled());
    new InputFilePreprocessor(parser).processInputs(inputs);
    new TranslationProcessor(parser, CodeReferenceMap.builder().build()).processInputs(inputs);
    return getTranslatedFile(outputPath + extension);
  }

  protected void runPipeline(String... files) {
    J2ObjC.run(Arrays.asList(files), options);
    assertNoErrors();
    assertNoWarnings();
  }

  protected void loadHeaderMappings() {
    options.getHeaderMap().loadMappings();
  }

  protected void preprocessFiles(String... fileNames) {
    GenerationBatch batch = new GenerationBatch(options);
    for (String fileName : fileNames) {
      batch.addSource(new RegularInputFile(
          tempDir.getPath() + File.separatorChar + fileName, fileName));
    }
    new InputFilePreprocessor(parser).processInputs(batch.getInputs());
  }

  protected String addSourceFile(String source, String fileName) throws IOException {
    File file = new File(tempDir, fileName);
    file.getParentFile().mkdirs();
    Files.asCharSink(file, options.fileUtil().getCharset()).write(source);
    return file.getPath();
  }

  /**
   * Removes a file from the tmp directory,
   */
  protected void removeFile(String relativePath) throws IOException {
    if (!new File(tempDir, relativePath).delete()) {
      throw new IOException("failed deleting " + relativePath);
    }
  }

  /**
   * Return the contents of a previously translated file, made by a call to
   * {@link #translateMethod} above.
   */
  protected String getTranslatedFile(String fileName) throws IOException {
    File f = new File(tempDir, fileName);
    assertTrue(fileName + " not generated", f.exists());
    return Files.asCharSource(f, options.fileUtil().getCharset()).read();
  }

  /**
   * When running Java tests in a build, there is no formal guarantee that these resources
   * be available as filesystem files. This copies a resource to a file in the temp dir,
   * and returns the new path.
   * The given resource name is relative to the class to which this method belongs
   * (which might be a subclass of GenerationTest, in a different package).
   */
  public String getResourceAsFile(String resourceName) throws IOException {
    URL url;
    try {
      url = getClass().getResource(resourceName).toURI().toURL();
    } catch (URISyntaxException e) {
      throw new IOException(e);
    }
    File file = new File(tempDir + "/resources/"
        + getClass().getPackage().getName().replace('.', File.separatorChar)
        + File.separatorChar + resourceName);
    file.getParentFile().mkdirs();
    OutputStream ostream = new FileOutputStream(file);
    Resources.copy(url, ostream);
    return file.getPath();
  }

  /** Asserts that a specific warning was reported during the last translation. */
  protected void assertWarning(String warning) {
    if (!ErrorUtil.getWarningMessages().contains(warning)) {
      failWithMessages(
          "Expected warning was not reported: \"" + warning + "\"", ErrorUtil.getWarningMessages());
    }
  }

  /**
   * Asserts that a specific warning was reported during the last translation. This assertion is
   * used when a warning has build-specific information, such as a failing file name.
   */
  protected void assertWarningRegex(String regex) {
    for (String message : ErrorUtil.getWarningMessages()) {
      if (message.matches(regex)) {
        return; // Warning found.
      }
      failWithMessages(
          "Expected warning was not reported: \"" + regex + "\"", ErrorUtil.getWarningMessages());
    }
  }

  /** Asserts that no warnings were reported during the last translation. */
  protected void assertNoWarnings() {
    // TODO(tball): remove when b/261217081 is fixed.
    int warningCount = ErrorUtil.warningCount();
    for (String message : ErrorUtil.getWarningMessages()) {
      if (message.startsWith("system modules path not set")) {
        --warningCount;
      }
    }
    if (warningCount > 0) {
      failWithMessages(
          String.format("No warnings were expected, but there were %d", warningCount),
          ErrorUtil.getWarningMessages());
    }
  }

  /** Asserts that a specific error was reported during the last translation. */
  protected void assertError(String error) {
    if (!ErrorUtil.getErrorMessages().contains(error)) {
      failWithMessages(
          "Expected error was not reported: \"" + error + "\"", ErrorUtil.getErrorMessages());
    }
  }

  /**
   * Asserts that a specific error was reported during the last translation. This assertion is used
   * when an error has build-specific information, such as a failing file name.
   */
  protected void assertErrorRegex(String regex) {
    for (String message : ErrorUtil.getErrorMessages()) {
      if (message.matches(regex)) {
        return; // Error found.
      }
      failWithMessages(
          "Expected error was not reported: \"" + regex + "\"", ErrorUtil.getErrorMessages());
    }
  }

  /** Asserts that no errors were reported during the last translation. */
  protected void assertNoErrors() {
    if (ErrorUtil.errorCount() > 0) {
      failWithMessages(
          String.format("No errors were expected, but there were %d", ErrorUtil.errorCount()),
          ErrorUtil.getErrorMessages());
    }
  }

  private void failWithMessages(String info, List<String> messages) {
    StringBuilder sb = new StringBuilder(info + "\n");
    for (String msg : messages) {
      sb.append(msg).append('\n');
    }
    fail(sb.toString());
  }

  protected String getTempDir() {
    return tempDir.getPath();
  }

  protected File getTempFile(String filename) {
    return new File(tempDir, filename);
  }

  protected void addJarFile(String jarFileName, String... sources) throws IOException {
    File jarFile = getTempFile(jarFileName);
    jarFile.getParentFile().mkdirs();
    options.fileUtil().appendSourcePath(jarFile.getPath());
    JarOutputStream jar = new JarOutputStream(new FileOutputStream(jarFile));
    try {
      for (int i = 0; i < sources.length; i += 2) {
        String name = sources[i];
        String source = sources[i + 1];
        JarEntry fooEntry = new JarEntry(name);
        jar.putNextEntry(fooEntry);
        jar.write(source.getBytes(Charset.defaultCharset()));
        jar.closeEntry();
      }
    } finally {
      jar.close();
    }
  }

  /**
   * Enables both javac and j2objc gidebugging support in a test.
   */
  protected void enableDebuggingSupport() {
    javacFlags.add("-parameters");
    javacFlags.add("-g");
  }

  protected boolean onJava9OrAbove() {
    return supportsClass("java.lang.Module");
  }

  protected boolean onJava11OrAbove() {
    return supportsClass("java.net.http.HttpClient");
  }

  protected boolean onJava16OrAbove() {
    return supportsClass("java.lang.Record");
  }

  protected boolean onJava17OrAbove() {
    return supportsClass("java.util.HexFormat");
  }

  protected boolean onJava21OrAbove() {
    return supportsClass("java.lang.MatchException");
  }

  private boolean supportsClass(String fullyQualifiedClassName) {
    try {
      Class.forName(fullyQualifiedClassName);
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  @FunctionalInterface
  protected interface TestLambda {
    void run() throws IOException;
  }

  protected void testOnJava9OrAbove(TestLambda test) throws IOException {
    if (onJava9OrAbove()) {
      SourceVersion.setMaxSupportedVersion(SourceVersion.JAVA_9);
      options.setSourceVersion(SourceVersion.JAVA_9);
      test.run();
    }
  }

  protected void testOnJava11OrAbove(TestLambda test) throws IOException {
    if (onJava11OrAbove()) {
      SourceVersion.setMaxSupportedVersion(SourceVersion.JAVA_11);
      options.setSourceVersion(SourceVersion.JAVA_11);
      test.run();
    }
  }

  protected void testOnJava16OrAbove(TestLambda test) throws IOException {
    if (onJava16OrAbove()) {
      SourceVersion.setMaxSupportedVersion(SourceVersion.JAVA_16);
      options.setSourceVersion(SourceVersion.JAVA_16);
      test.run();
    }
  }

  protected void testOnJava17OrAbove(TestLambda test) throws IOException {
    if (onJava17OrAbove()) {
      SourceVersion.setMaxSupportedVersion(SourceVersion.JAVA_17);
      options.setSourceVersion(SourceVersion.JAVA_17);
      test.run();
    }
  }

  protected void testOnJava21OrAbove(TestLambda test) throws IOException {
    if (onJava21OrAbove()) {
      SourceVersion.setMaxSupportedVersion(SourceVersion.JAVA_21);
      options.setSourceVersion(SourceVersion.JAVA_21);
      test.run();
    }
  }

  protected String extractKytheMetadata(String translation) {
    String openingDelimiter = "/* This file contains Kythe metadata.";
    String closingDelimiter = "*/";
    int openingDelimiterIndex = translation.indexOf(openingDelimiter);
    int closingDelimiterIndex = translation.indexOf(closingDelimiter);
    if (openingDelimiterIndex == -1) {
      return "";
    }

    String encodedMetadata =
        translation
            .substring(openingDelimiterIndex + openingDelimiter.length(), closingDelimiterIndex)
            .replace("\n", "")
            .trim();
    return new String(Base64.getDecoder().decode(encodedMetadata), UTF_8);
  }
}
