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

package com.google.devtools.j2objc.translate;

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.SwitchStatement;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.VariableDeclarationStatement;

import java.io.IOException;
import java.util.List;

/**
 * Unit tests for {@link SwitchRewriter}.
 *
 * @author Tom Ball, Keith Stanger
 */
public class SwitchRewriterTest extends GenerationTest {

  public void testVariableDeclarationsInSwitchStatement() throws IOException {
    String translation = translateSourceFile(
      "public class A { public void doSomething(int i) { switch (i) { "
      + "case 1: int j = i * 2; log(j); break; "
      + "case 2: log(i); break; "
      + "case 3: log(i); int k = i, l = 42; break; }}"
      + "private void log(int i) {}}",
      "A", "A.m");
    assertTranslation(translation, "jint j;");
    if (options.isJDT()) {
      assertTranslation(translation, "jint k, l;");
    } else {
      assertTranslation(translation, "jint k;");
      assertTranslation(translation, "jint l;");
    }
    assertTranslation(translation, "case 1:");
    assertTrue(translation.indexOf("jint j;") < translation.indexOf("case 1:"));
    if (options.isJDT()) {
      assertTrue(translation.indexOf("jint k, l;") < translation.indexOf("case 1:"));
      assertTrue(translation.indexOf("jint j;") < translation.indexOf("jint k, l;"));
    } else {
      assertTrue(translation.indexOf("jint k;") < translation.indexOf("case 1:"));
      assertTrue(translation.indexOf("jint l;") < translation.indexOf("case 1:"));
      assertTrue(translation.indexOf("jint j;") < translation.indexOf("jint k;"));
      assertTrue(translation.indexOf("jint k;") < translation.indexOf("jint l;"));
    }
    assertTranslation(translation, "j = i * 2;");
    assertTranslation(translation, "k = i;");
    assertTranslation(translation, "l = 42;");
    assertTrue(translation.indexOf("k = i") < translation.indexOf("l = 42"));
  }

  public void testVariableDeclarationsInSwitchStatement2() throws IOException {
    CompilationUnit unit = translateType("A",
        "public class A { public void doSomething(int i) { switch (i) { "
        + "case 1: int j = i * 2; log(j); break; "
        + "case 2: log(i); break; "
        + "case 3: log(i); int k = i, l = 42; break; }}"
        + "private void log(int i) {}}");
    TypeDeclaration testType = (TypeDeclaration) unit.getTypes().get(0);
    // First MethodDeclaration is the implicit default constructor.
    MethodDeclaration method = TreeUtil.getMethodDeclarationsList(testType).get(1);
    List<Statement> stmts = method.getBody().getStatements();
    assertEquals(1, stmts.size());
    Block block = (Block) stmts.get(0);
    stmts = block.getStatements();
    if (options.isJDT()) {
      assertEquals(3, stmts.size());
      assertTrue(stmts.get(0) instanceof VariableDeclarationStatement);
      assertTrue(stmts.get(1) instanceof VariableDeclarationStatement);
      assertTrue(stmts.get(2) instanceof SwitchStatement);
    } else {
      assertEquals(4, stmts.size());
      assertTrue(stmts.get(0) instanceof VariableDeclarationStatement);
      assertTrue(stmts.get(1) instanceof VariableDeclarationStatement);
      assertTrue(stmts.get(2) instanceof VariableDeclarationStatement);
      assertTrue(stmts.get(3) instanceof SwitchStatement);
    }
  }

  public void testMultipleSwitchVariables() throws IOException {
    String translation = translateSourceFile(
      "public class A { public void doSomething(int n) { switch (n) { "
      + "case 1: int i; int j = 2; }}"
      + "private void log(int i) {}}",
      "A", "A.m");
    int index = translation.indexOf("jint i;");
    assertTrue(index >= 0 && index < translation.indexOf("switch"));
    index = translation.indexOf("jint j;");
    assertTrue(index >= 0 && index < translation.indexOf("switch"));
    assertOccurrences(translation, "jint i;", 1);
    assertFalse(translation.contains("jint j = 2;"));
  }

  public void testEnumConstantsInSwitchStatement() throws IOException {
    String translation = translateSourceFile(
        "public class A { static enum EnumType { ONE, TWO }"
        + "public static void doSomething(EnumType e) {"
        + " switch (e) { case ONE: break; case TWO: break; }}}",
        "A", "A.m");
    assertTranslation(translation, "switch ([e ordinal]) {");
    assertTranslation(translation, "case A_EnumType_Enum_ONE:");
  }

  public void testPrimitiveConstantInSwitchCase() throws IOException {
    String translation = translateSourceFile(
        "public class A { public static final char PREFIX = 'p';"
        + "public boolean doSomething(char c) { switch (c) { "
        + "case PREFIX: return true; "
        + "default: return false; }}}",
        "A", "A.h");
    assertTranslation(translation, "#define A_PREFIX 'p'");
    translation = getTranslatedFile("A.m");
    assertTranslation(translation, "case A_PREFIX:");
  }

  // Verify Java 7's switch statements with strings.
  public void testStringSwitchStatement() throws IOException {
    addSourceFile("class Foo { static final String TEST = \"test1\"; }", "Foo.java");
    addSourceFile("interface Bar { String TEST = \"test2\"; }", "Bar.java");
    addSourcesToSourcepaths();
    String translation = translateSourceFile(
        "public class Test { "
        + "static final String constant = \"mumble\";"
        + "int test(String s) { "
        + "  switch(s) {"
        + "    case \"foo\": return 42;"
        + "    case \"bar\": return 666;"
        + "    case constant: return -1;"
        + "    case Foo.TEST: return -2;"
        + "    case Bar.TEST: return -3;"
        + "    default: return -1;"
        + "  }}}",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "switch (JreIndexOfStr(s, (id[]){ @\"foo\", "
            + "@\"bar\", Test_constant, Foo_TEST, Bar_TEST }, 5)) {",
        "  case 0:",
        "  return 42;",
        "  case 1:",
        "  return 666;",
        "  case 2:",
        "  return -1;",
        "  case 3:",
        "  return -2;",
        "  case 4:",
        "  return -3;",
        "  default:",
        "  return -1;",
        "}");
  }

  /**
   * Verify that when a the last switch case is empty (no statement),
   * an empty statement is added.  Java doesn't require an empty statement
   * here, while C does.
   */
  public void testEmptyLastCaseStatement() throws IOException {
    String translation = translateSourceFile(
        "public class A {"
        + "  int test(int i) { "
        + "    switch (i) { case 1: return 1; case 2: return 2; default: } return i; }}",
        "A", "A.m");
    assertTranslatedLines(translation, "default:", ";", "}");
  }

  public void testLocalFinalPrimitiveCaseValue() throws IOException {
    String translation = translateSourceFile(
        "public class Test { char test(int i) { final int ONE = 1, TWO = 2; "
        + "switch (i) { case ONE: return 'a'; case TWO: return 'b'; default: return 'z'; } } }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "switch (i) {",
        "  case 1:",
        "  return 'a';",
        "  case 2:",
        "  return 'b';",
        "  default:",
        "  return 'z';",
        "}");
  }

  public void testEmptySwitchStatement() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test(int i) { switch (i) { } } }", "Test", "Test.m");
    assertTranslatedLines(translation, "switch (i) {", "}");
  }
}
