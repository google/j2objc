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

package com.google.devtools.j2objc.gen;

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.Options;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Unit tests for CPP line directive generation, used to support debugging.
 *
 * @author Tom Ball
 */
public class LineDirectivesTest extends GenerationTest {

  @Override
  protected void setUp() throws IOException {
    super.setUp();
    Options.setEmitLineDirectives(true);
  }

  public void testNoHeaderNumbering() throws IOException {
    String translation = translateSourceFile(
        "import java.util.*;\n\n public class A {\n\n"
        + "  // Method one\n"
        + "  void one() {}\n\n"
        + "  // Method two\n"
        + "  void two() {}\n}\n",
        "A", "A.h");
    assertFalse(translation.contains("#line"));
  }

  public void testMethodNumbering() throws IOException {
    String translation = translateSourceFile(
        "import java.util.*;\n\n public class A {\n\n"
        + "  // Method one\n"
        + "  void one() {}\n\n"
        + "  // Method two\n"
        + "  void two() {}\n"
        + "  void three() {}}\n",
        "A", "A.m");
    assertTranslation(translation, "#line 1 \"A.java\"");
    assertTranslation(translation, "#line 3\n@implementation A");
    assertTranslation(translation, "#line 6\n- (void)one");
    // Lines match up between one() and two() so no need for the directive.
    assertNotInTranslation(translation, "#line 9\n- (void)two");
    assertTranslation(translation, "#line 10\n- (void)three");
  }

  public void testStatementNumbering() throws IOException {
    String translation = translateSourceFile(
        "public class A {\n"
        + "  String test() {\n"
        + "    // some comment\n"
        + "    int i = 0;\n\n"
        + "    // another comment\n"
        + "    return Integer.toString(i);\n"
        + "  }}\n",
        "A", "A.m");
    assertTranslation(translation, "#line 1 \"A.java\"");
    assertTranslation(translation, "#line 2\n- (NSString *)test");
    assertTranslation(translation, "#line 4\n  jint i = 0;");
    assertTranslation(translation, "#line 7\n  return JavaLangInteger_toStringWithInt_(i);");
  }

  public void testForIfWhileStatementsWithoutBlocks() throws IOException {
    String translation = translateSourceFile(
        "public class Test {\n"
        + "  void test(int n) {\n"
        + "    for (int i = 0; i < 10; i++)\n"
        + "      if ((n % 2) == 0)\n"
        + "        n += i;\n"
        + "    \n"
        + "    for (int j = 0; j < 100; j++)\n"
        + "      for (int k = 0; k < 1000; k++)\n"
        + "        n += j + k;\n"
        + "    \n"
        + "    while (n > 0)\n"
        + "      n--;"
        + "  }}",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "for (jint i = 0; i < 10; i++)",
        "#line 4",
        "if ((n % 2) == 0)",
        "#line 5",
        "n += i;",
        "",
        "#line 7",
        "for (jint j = 0; j < 100; j++)",
        "#line 8",
        "for (jint k = 0; k < 1000; k++)",
        "#line 9",
        "n += j + k;",
        "",
        "#line 11",
        "while (n > 0)",
        "#line 12",
        "n--;");
  }

  public void testCombinedFileLineDirectives() throws IOException {
    // An example where some types will be reordered
    addSourceFile(
        "package unit;\n"
        + "public class Test {\n"
        + "    public String Dummy(int i) {\n"
        + "        System.out.println(\"Hello world!\");\n"
        + "        return Integer.toString(i);\n"
        + "    }\n"
        + "}\n"
        + "\n"
        + "class NotReordered {"
        + "    public String DummyTwo(int i) {\n"
        + "        int j = i + 1;\n"
        + "        return Integer.toString(j);\n"
        + "    }"
        + "}\n",
        "unit/Test.java");
    addSourceFile(
        "package unit;\n"
        + "public class TestDependent {\n"
        + "    public Test Dummy() {\n"
        + "        return new Test();\n"
        + "    }\n"
        + "    public AnotherTest AnotherDummy() {\n"
        + "        return new AnotherTest();\n"
        + "    }\n"
        + "}\n",
        "unit/TestDependent.java");
    addSourceFile(
        "package unit;\n"
        + "public class AnotherTest extends Test {\n"
        + "    public void AnotherDummy() {\n"
        + "    }\n"
        + "}\n"
        + "\n"
        + "class AlsoNotReordered {"
        + "    public void DummyTwo() {\n"
        + "    }"
        + "}\n",
        "unit/AnotherTest.java");

    String translation = translateCombinedFiles(
        "unit/Foo", ".m",
        "unit/TestDependent.java", "unit/AnotherTest.java", "unit/Test.java");
    assertDirectivePreceedsLine(
        translation, "TestDependent.java", "@implementation UnitTestDependent");
    assertDirectivePreceedsLine(
        translation, "Test.java", "@implementation UnitTest");
    assertDirectivePreceedsLine(
        translation, "AnotherTest.java", "@implementation UnitAnotherTest");
    assertDirectivePreceedsLine(
        translation, "AnotherTest.java", "@implementation UnitAlsoNotReordered");
    assertDirectivePreceedsLine(
        translation, "Test.java", "@implementation UnitNotReordered");
    // make sure lines get re-synced when files are interwoven
    assertTranslatedLines(translation,
        "#line 3",
        "- (NSString *)DummyWithInt:(jint)i {");
    assertTranslatedLines(translation,
        "#line 9",
        "- (NSString *)DummyTwoWithInt:(jint)i {");
  }

  private static final Pattern LINE_DIRECTIVE_PATTERN = Pattern.compile(
      "\\n#line \\d+ \"(\\S*)\"\\n");

  /**
   * Asserts that the directive "#line some_line "some_prefix/$filename""
   * most immediately preceding the given line matches the given filename;
   * that is, the line directives should say we are on the given filename
   * and not some other filename.
   */
  private void assertDirectivePreceedsLine(String translation, String filename, String line) {
    int lineIndex = translation.indexOf(line + "\n");
    assertTrue(lineIndex > 0);
    // Find the last instance of a #line directive before lineIndex
    Matcher m = LINE_DIRECTIVE_PATTERN.matcher(translation.substring(0, lineIndex));
    String foundLineDirectiveFilename = "";
    while (m.find()) {
      foundLineDirectiveFilename = m.group(1);
    }
    assertTrue(foundLineDirectiveFilename.endsWith(filename));
  }
}
