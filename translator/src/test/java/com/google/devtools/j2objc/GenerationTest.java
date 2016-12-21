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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.file.RegularInputFile;
import com.google.devtools.j2objc.gen.GenerationUnit;
import com.google.devtools.j2objc.gen.SourceBuilder;
import com.google.devtools.j2objc.gen.StatementGenerator;
import com.google.devtools.j2objc.pipeline.GenerationBatch;
import com.google.devtools.j2objc.pipeline.InputFilePreprocessor;
import com.google.devtools.j2objc.pipeline.ProcessingContext;
import com.google.devtools.j2objc.pipeline.TranslationProcessor;
import com.google.devtools.j2objc.util.CodeReferenceMap;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.FileUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.Parser;
import com.google.devtools.j2objc.util.TimeTracker;
import java.io.BufferedReader;
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
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestCase;

/**
 * Tests code generation. A string containing the source code for a list of Java
 * statements is parsed and translated, then iOS code is generated for one or
 * more of those statements for comparison in a specific generation test.
 *
 * @author Tom Ball
 */
public class GenerationTest extends TestCase {

  protected File tempDir;
  protected Parser parser;
  protected Options options;
  private CodeReferenceMap deadCodeMap = null;

  static {
    // Prevents errors and warnings from being printed to the console.
    ErrorUtil.setTestMode();
    ClassLoader cl = GenerationTest.class.getClassLoader();
    cl.setPackageAssertionStatus("com.google.devtools.j2objc", true);
  }

  @Override
  protected void setUp() throws IOException {
    tempDir = FileUtil.createTempDir("testout");
    loadOptions();
    createParser();
  }

  @Override
  protected void tearDown() throws Exception {
    options = null;
    FileUtil.deleteTempDir(tempDir);
    ErrorUtil.reset();
  }

  protected void loadOptions() throws IOException {
    options = new Options();

    options.load(new String[]{
        "-d", tempDir.getAbsolutePath(),
        "-sourcepath", tempDir.getAbsolutePath(),
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
        if (node.getName().getIdentifier().equals("test")) {
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
    TranslationProcessor.applyMutations(newUnit, deadCodeMap, TimeTracker.noop());
    return newUnit;
  }

  /**
   * Compiles Java source, as contained in a source file.
   *
   * @param name the name of the public type being declared
   * @param source the source code
   * @return the parsed compilation unit
   */
  protected CompilationUnit compileType(String name, String source) {
    String mainTypeName = name.substring(name.lastIndexOf('.') + 1);
    String path = name.replace('.', '/') + ".java";
    int errors = ErrorUtil.errorCount();
    parser.setEnableDocComments(options.docCommentsEnabled());
    CompilationUnit unit = parser.parse(mainTypeName, path, source);
    if (ErrorUtil.errorCount() > errors) {
      int newErrorCount = ErrorUtil.errorCount() - errors;
      String info = String.format(
          "%d test compilation error%s", newErrorCount, (newErrorCount == 1 ? "" : "s"));
      failWithMessages(info, ErrorUtil.getErrorMessages().subList(errors, ErrorUtil.errorCount()));
    }
    return unit;
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
   * Translate a Java method into a JDT DOM MethodDeclaration.  Although JDT
   * has support for parsing methods, it doesn't resolve them.  The statements
   * are therefore wrapped in a type declaration so they having bindings.
   */
  protected MethodDeclaration translateMethod(String method) {
    // Wrap statements in test class, so type resolution works.
    String source = "public class Test { " + method + " }";
    CompilationUnit unit = translateType("Test", source);
    final MethodDeclaration[] result = new MethodDeclaration[1];
    unit.accept(new TreeVisitor() {
      @Override
      public boolean visit(MethodDeclaration node) {
        String name = node.getName().getIdentifier();
        if (name.equals(NameTable.INIT_NAME)
            || name.equals(NameTable.FINALIZE_METHOD)
            || name.equals(NameTable.DEALLOC_METHOD)) {
          return false;
        }
        assert result[0] == null;
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
    String source = getTranslatedFile(typeName.replace('.', '/') + ".java");
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

  protected String generateFromUnit(CompilationUnit unit, String filename) throws IOException {
    GenerationUnit genUnit = new GenerationUnit(unit.getSourceFilePath(), 1, options);
    genUnit.addCompilationUnit(unit);
    TranslationProcessor.generateObjectiveCSource(genUnit);
    return getTranslatedFile(filename);
  }

  protected String translateCombinedFiles(String outputPath, String extension, String... sources)
      throws IOException {
    List<RegularInputFile> inputFiles = Lists.newArrayList();
    for (String sourceFile : sources) {
      inputFiles.add(new RegularInputFile(tempDir + "/" + sourceFile, sourceFile));
    }
    GenerationBatch batch = new GenerationBatch(options);
    batch.addCombinedJar(outputPath + ".testfile", inputFiles);
    List<ProcessingContext> inputs = batch.getInputs();
    parser.setEnableDocComments(options.docCommentsEnabled());
    new InputFilePreprocessor(parser).processInputs(inputs);
    new TranslationProcessor(parser, CodeReferenceMap.builder().build()).processInputs(inputs);
    return getTranslatedFile(outputPath + extension);
  }

  protected void runPipeline(String... files) {
    J2ObjC.run(Arrays.asList(files), options);
    assertErrorCount(0);
    assertWarningCount(0);
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
    Files.write(source, file, options.fileUtil().getCharset());
    return file.getPath();
  }

  /**
   * Return the contents of a previously translated file, made by a call to
   * {@link #translateMethod} above.
   */
  protected String getTranslatedFile(String fileName) throws IOException {
    File f = new File(tempDir, fileName);
    assertTrue(fileName + " not generated", f.exists());
    return Files.toString(f, options.fileUtil().getCharset());
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

  /**
   * Asserts that the correct number of warnings were reported during the
   * last translation.
   */
  protected void assertWarningCount(int expectedCount) {
    if (expectedCount != ErrorUtil.warningCount()) {
      failWithMessages(
          String.format("Wrong number of warnings. Expected:%d but was:%d",
                        expectedCount, ErrorUtil.warningCount()),
          ErrorUtil.getWarningMessages());
    }
  }

  /**
   * Asserts that the correct number of errors were reported during the
   * last translation.
   */
  protected void assertErrorCount(int expectedCount) {
    if (expectedCount != ErrorUtil.errorCount()) {
      failWithMessages(
          String.format("Wrong number of errors. Expected:%d but was:%d",
                        expectedCount, ErrorUtil.errorCount()),
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

  // Empty test so Bazel won't report a "no tests" error.
  public void testNothing() {}
}
