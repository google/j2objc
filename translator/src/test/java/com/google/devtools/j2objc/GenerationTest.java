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
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.TreeConverter;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.gen.SourceBuilder;
import com.google.devtools.j2objc.gen.StatementGenerator;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.JdtParser;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TimeTracker;

import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.List;
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

  static {
    // Prevents errors and warnings from being printed to the console.
    ErrorUtil.setTestMode();
  }

  @Override
  protected void setUp() throws IOException {
    tempDir = createTempDir();
    Options.load(new String[] {
      "-d", tempDir.getAbsolutePath(),
      "--mem-debug", // Run tests with memory debugging by default.
      "--hide-private-members" // Future default, run tests with it now.
    });
    parser = initializeParser(tempDir);
  }

  @Override
  protected void tearDown() throws Exception {
    deleteTempDir(tempDir);
    ErrorUtil.reset();
  }

  private static JdtParser initializeParser(File tempDir) {
    JdtParser parser = new JdtParser();
    parser.addClasspathEntries(getComGoogleDevtoolsJ2objcPath());
    parser.addSourcepathEntry(tempDir.getAbsolutePath());
    return parser;
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
   * @param name the name of the public type being declared
   * @param source the source code
   * @return the translated compilation unit
   */
  protected CompilationUnit translateType(String name, String source) {
    org.eclipse.jdt.core.dom.CompilationUnit unit = compileType(name, source);
    NameTable.initialize();
    Types.initialize(unit);
    CompilationUnit newUnit = TreeConverter.convertCompilationUnit(unit, name + ".java", source);
    TranslationProcessor.applyMutations(newUnit, TimeTracker.noop());
    return newUnit;
  }

  /**
   * Compiles Java source, as contained in a source file.
   *
   * @param name the name of the public type being declared
   * @param source the source code
   * @return the parsed compilation unit
   */
  protected org.eclipse.jdt.core.dom.CompilationUnit compileType(String name, String source) {
    int errors = ErrorUtil.errorCount();
    parser.setEnableDocComments(Options.docCommentsEnabled());
    org.eclipse.jdt.core.dom.CompilationUnit unit = parser.parse(name, source);
    assertEquals(errors, ErrorUtil.errorCount());
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
    return StatementGenerator.generate(statement, false, SourceBuilder.BEGINNING_OF_FILE).trim();
  }

  /**
   * Returns a newly-created temporary directory.
   */
  protected File createTempDir() throws IOException {
    File tempDir = File.createTempFile("testout", ".tmp");
    tempDir.delete();
    tempDir.mkdir();
    return tempDir;
  }

  /**
   * Recursively delete specified directory.
   */
  protected void deleteTempDir(File dir) {
    // TODO(cpovirk): try Directories.deleteRecursively if a c.g.c.unix dep is OK
    if (dir.exists()) {
      for (File f : dir.listFiles()) {
        if (f.isDirectory()) {
          deleteTempDir(f);
        } else {
          f.delete();
        }
      }
      dir.delete();
    }
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
          if (i == 0) {
            return false;
          }
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
    String source = getTranslatedFile(typeName + ".java");
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
    CompilationUnit unit = translateType(typeName, source);
    TranslationProcessor.generateObjectiveCSource(unit, TimeTracker.noop());
    return getTranslatedFile(fileName);
  }

  protected void addSourceFile(String source, String fileName) throws IOException {
    File file = new File(tempDir, fileName);
    file.getParentFile().mkdirs();
    Files.write(source, file, Charset.defaultCharset());
  }

  /**
   * Return the contents of a previously translated file, made by a call to
   * {@link #translateMethod} above.
   */
  protected String getTranslatedFile(String fileName) throws IOException {
    File f = new File(tempDir, fileName);
    assertTrue(fileName + " not generated", f.exists());
    return Files.toString(f, Charset.defaultCharset());
  }

  /**
   * Asserts that the correct number of warnings were reported during the
   * last translation.
   */
  protected void assertWarningCount(int expectedCount) {
    assertEquals(expectedCount, ErrorUtil.warningCount());
  }

  /**
   * Asserts that the correct number of errors were reported during the
   * last translation.
   */
  protected void assertErrorCount(int expectedCount) {
    assertEquals(expectedCount, ErrorUtil.errorCount());
  }
}
