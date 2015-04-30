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
import com.google.devtools.j2objc.ast.TreeConverter;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.file.RegularInputFile;
import com.google.devtools.j2objc.gen.GenerationUnit;
import com.google.devtools.j2objc.gen.SourceBuilder;
import com.google.devtools.j2objc.gen.StatementGenerator;
import com.google.devtools.j2objc.util.DeadCodeMap;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.FileUtil;
import com.google.devtools.j2objc.util.JdtParser;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TimeTracker;

import junit.framework.TestCase;

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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tests code generation. A string containing the source code for a list of Java
 * statements is parsed and translated, then iOS code is generated for one or
 * more of those statements for comparison in a specific generation test.
 *
 * @author Tom Ball
 */
public abstract class GenerationTest extends TestCase {

  protected File tempDir;
  private JdtParser parser;
  private DeadCodeMap deadCodeMap = null;

  static {
    // Prevents errors and warnings from being printed to the console.
    ErrorUtil.setTestMode();
  }

  @Override
  protected void setUp() throws IOException {
    tempDir = FileUtil.createTempDir("testout");
    Options.load(new String[]{
        "-d", tempDir.getAbsolutePath(),
        "-q", // Suppress console output.
        "--hide-private-members", // Future default, run tests with it now.
        "-encoding", "UTF-8" // Translate strings correctly when encodings are nonstandard.
    });
    parser = initializeParser(tempDir);
  }

  @Override
  protected void tearDown() throws Exception {
    Options.reset();
    FileUtil.deleteTempDir(tempDir);
    ErrorUtil.reset();
  }

  private static JdtParser initializeParser(File tempDir) {
    JdtParser parser = new JdtParser();
    parser.addClasspathEntries(getComGoogleDevtoolsJ2objcPath());
    parser.addSourcepathEntry(tempDir.getAbsolutePath());
    return parser;
  }

  protected void setDeadCodeMap(DeadCodeMap deadCodeMap) {
    this.deadCodeMap = deadCodeMap;
  }

  protected void addSourcesToSourcepaths() throws IOException {
    Options.getSourcePathEntries().add(tempDir.getCanonicalPath());
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
    parser.setEnableDocComments(Options.docCommentsEnabled());
    org.eclipse.jdt.core.dom.CompilationUnit unit = parser.parseWithBindings(path, source);
    if (ErrorUtil.errorCount() > errors) {
      int newErrorCount = ErrorUtil.errorCount() - errors;
      String info = String.format(
          "%d test compilation error%s", newErrorCount, (newErrorCount == 1 ? "" : "s"));
      failWithMessages(info, ErrorUtil.getErrorMessages().subList(errors, ErrorUtil.errorCount()));
    }
    return TreeConverter.convertCompilationUnit(
        unit, path, mainTypeName, source, NameTable.newFactory());
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
    return StatementGenerator.generate(statement, false, SourceBuilder.BEGINNING_OF_FILE).trim();
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
    if (!hasRegion(translation, expectedLines)) {
      fail("expected:\"" + Joiner.on('\n').join(expectedLines) + "\" in:\n" + translation);
    }
  }

  private boolean hasRegion(String s, String[] lines) throws IOException {
    int index = s.indexOf(lines[0]);
    if (index == -1) {
      return false;
    }
    BufferedReader in = new BufferedReader(new StringReader(s.substring(index)));
    try {
      for (int i = 0; i < lines.length; i++) {
        String nextLine = in.readLine();
        if (nextLine == null) {
          return false;
        }
        index += nextLine.length() + 1;  // Also skip trailing newline.
        if (!nextLine.trim().equals(lines[i].trim())) {
          // Check if there is a subsequent match.
          return hasRegion(s.substring(index), lines);
        }
      }
      return true;
    } finally {
      in.close();
    }
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
    GenerationUnit genUnit = new GenerationUnit(unit.getSourceFilePath(), 1);
    genUnit.addCompilationUnit(unit);
    TranslationProcessor.generateObjectiveCSource(genUnit, TimeTracker.noop());
    return getTranslatedFile(filename);
  }

  protected String translateCombinedFiles(String outputPath, String extension, String... sources)
      throws IOException {
    List<RegularInputFile> inputFiles = Lists.newArrayList();
    for (String sourceFile : sources) {
      inputFiles.add(new RegularInputFile(tempDir + "/" + sourceFile, sourceFile));
    }
    GenerationBatch batch = new GenerationBatch();
    batch.addCombinedJar(outputPath + ".testfile", inputFiles);
    List<ProcessingContext> inputs = batch.getInputs();
    parser.setEnableDocComments(Options.docCommentsEnabled());
    new InputFilePreprocessor(parser).processInputs(inputs);
    new TranslationProcessor(parser, DeadCodeMap.builder().build()).processInputs(inputs);
    return getTranslatedFile(outputPath + extension);
  }

  protected void loadHeaderMappings() {
    TranslationProcessor.loadHeaderMappings();
  }

  protected void preprocessFiles(String... fileNames) {
    GenerationBatch batch = new GenerationBatch();
    for (String fileName : fileNames) {
      batch.addSource(new RegularInputFile(
          tempDir.getPath() + File.separatorChar + fileName, fileName));
    }
    new InputFilePreprocessor(parser).processInputs(batch.getInputs());
  }

  protected Map<String, String> writeAndReloadHeaderMappings() throws IOException {
    File outputHeaderMappingFile = new File(tempDir.getPath() + "/mappings.j2objc");
    outputHeaderMappingFile.deleteOnExit();
    Options.setOutputHeaderMappingFile(outputHeaderMappingFile);
    TranslationProcessor.printHeaderMappings();
    Options.getHeaderMappings().clear();
    Options.setOutputHeaderMappingFile(null);
    Options.setHeaderMappingFiles(Lists.newArrayList(outputHeaderMappingFile.getAbsolutePath()));
    loadHeaderMappings();
    return Options.getHeaderMappings();
  }

  protected void addSourceFile(String source, String fileName) throws IOException {
    File file = new File(tempDir, fileName);
    file.getParentFile().mkdirs();
    Files.write(source, file, Options.getCharset());
  }

  /**
   * Return the contents of a previously translated file, made by a call to
   * {@link #translateMethod} above.
   */
  protected String getTranslatedFile(String fileName) throws IOException {
    File f = new File(tempDir, fileName);
    assertTrue(fileName + " not generated", f.exists());
    return Files.toString(f, Options.getCharset());
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
}
