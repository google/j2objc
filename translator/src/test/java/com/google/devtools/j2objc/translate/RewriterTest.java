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

package com.google.devtools.j2objc.translate;

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.EmptyStatement;
import com.google.devtools.j2objc.ast.ForStatement;
import com.google.devtools.j2objc.ast.IfStatement;
import com.google.devtools.j2objc.ast.LabeledStatement;
import com.google.devtools.j2objc.ast.Statement;

import java.io.IOException;
import java.util.List;

/**
 * Unit tests for {@link Rewriter}.
 *
 * @author Tom Ball
 */
public class RewriterTest extends GenerationTest {

  public void testContinueAndBreakUsingSameLabel() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test() { "
        + "int i = 0; outer: for (; i < 10; i++) { "
        + "for (int j = 0; j < 10; j++) { "
        + "int n = i + j; "
        + "if (n == 5) continue outer; "
        + "else break outer; } } } }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "jint i = 0;",
        "for (; i < 10; i++) {",
        "{",
        "for (jint j = 0; j < 10; j++) {",
        "jint n = i + j;",
        "if (n == 5) goto continue_outer;",
        "else goto break_outer;",
        "}",
        "}",
        "continue_outer: ;",
        "}",
        "break_outer: ;");
  }

  public void testLabeledContinue() throws IOException {
    List<Statement> stmts = translateStatements(
        "int i = 0; outer: for (; i < 10; i++) { "
        + "for (int j = 0; j < 10; j++) { continue outer; }}");
    assertEquals(2, stmts.size());
    Statement s = stmts.get(1);
    assertTrue(s instanceof ForStatement);  // not LabeledStatement
    ForStatement fs = (ForStatement) s;
    Statement forStmt = fs.getBody();
    assertTrue(forStmt instanceof Block);
    stmts = ((Block) forStmt).getStatements();
    assertEquals(2, stmts.size());
    Statement lastStmt = stmts.get(1);
    assertTrue(lastStmt instanceof LabeledStatement);
    assertTrue(((LabeledStatement) lastStmt).getBody() instanceof EmptyStatement);
  }

  public void testLabeledBreak() throws IOException {
    List<Statement> stmts = translateStatements(
        "int i = 0; outer: for (; i < 10; i++) { "
        + "for (int j = 0; j < 10; j++) { break outer; }}");
    assertEquals(3, stmts.size());
    Statement s = stmts.get(1);
    assertTrue(s instanceof ForStatement);  // not LabeledStatement
    ForStatement fs = (ForStatement) s;
    Statement forStmt = fs.getBody();
    assertTrue(forStmt instanceof Block);
    assertEquals(1, ((Block) forStmt).getStatements().size());
    Statement lastStmt = stmts.get(2);
    assertTrue(lastStmt instanceof LabeledStatement);
    assertTrue(((LabeledStatement) lastStmt).getBody() instanceof EmptyStatement);
  }

  public void testLabeledBreakWithNonBlockParent() throws IOException {
    List<Statement> stmts = translateStatements(
        "int i = 0; if (i == 0) outer: for (; i < 10; i++) { "
        + "for (int j = 0; j < 10; j++) { break outer; }}");
    assertEquals(2, stmts.size());
    Statement s = stmts.get(1);
    assertTrue(s instanceof IfStatement);
    s = ((IfStatement) s).getThenStatement();
    assertTrue(s instanceof Block);
    stmts = ((Block) s).getStatements();
    assertEquals(2, stmts.size());
    s = stmts.get(0);
    assertTrue(s instanceof ForStatement);  // not LabeledStatement
    ForStatement fs = (ForStatement) s;
    Statement forStmt = fs.getBody();
    assertTrue(forStmt instanceof Block);
    assertEquals(1, ((Block) forStmt).getStatements().size());
    Statement labelStmt = stmts.get(1);
    assertTrue(labelStmt instanceof LabeledStatement);
    assertTrue(((LabeledStatement) labelStmt).getBody() instanceof EmptyStatement);
  }

  public void testLabeledBreakOnSwitchStatement() throws IOException {
    String translation = translateSourceFile(
        "class Test { void main(int i) { outer: switch(i) { case 1: break outer; } } }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "switch (i) {",
        "case 1:",
        "goto break_outer;",
        "}",
        "break_outer: ;");
  }

  /**
   * Verify that array initializers are rewritten as method calls.
   */
  public void testArrayInitializerRewrite() throws IOException {
    String translation = translateSourceFile(
        "public class Test { void test() { int[] a = { 1, 2, 3 }; char b[] = { '4', '5' }; } }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "IOSIntArray *a = [IOSIntArray arrayWithInts:(jint[]){ 1, 2, 3 } count:3];",
        "IOSCharArray *b = [IOSCharArray arrayWithChars:(jchar[]){ '4', '5' } count:2];");
  }

  /**
   * Verify that static array initializers are rewritten as method calls.
   */
  public void testStaticArrayInitializerRewrite() throws IOException {
    String translation = translateSourceFile(
        "public class Test { static int[] a = { 1, 2, 3 }; static char b[] = { '4', '5' }; }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "+ (void)initialize {",
        "if (self == [Test class]) {",
        "JreStrongAssignAndConsume(&Test_a, "
            + "[IOSIntArray newArrayWithInts:(jint[]){ 1, 2, 3 } count:3]);",
        "JreStrongAssignAndConsume(&Test_b, "
            + "[IOSCharArray newArrayWithChars:(jchar[]){ '4', '5' } count:2]);");
  }

  public void testNonStaticMultiDimArrayInitializer() throws IOException {
    String translation = translateSourceFile(
        "class Test { int[][] a = { { 1, 2, 3 } }; }", "Test", "Test.m");
    assertTranslation(translation,
        "[IOSObjectArray newArrayWithObjects:(id[]){"
        + " [IOSIntArray arrayWithInts:(jint[]){ 1, 2, 3 } count:3] } count:1"
        + " type:IOSClass_intArray(1)]");
  }

  public void testArrayCreationInConstructorInvocation() throws IOException {
    String translation = translateSourceFile(
        "class Test { Test(int[] i) {} Test() { this(new int[] {}); } }", "Test", "Test.m");
    assertTranslation(translation,
        "Test_initWithIntArray_(self, [IOSIntArray arrayWithInts:(jint[]){  } count:0]);");
  }

  public void testInterfaceFieldsAreStaticFinal() throws IOException {
    String source = "interface Test { String foo = \"bar\"; }";
    String translation = translateSourceFile(source, "Test", "Test.h");
    assertTranslation(translation, "J2OBJC_STATIC_FIELD_OBJ_FINAL(Test, foo, NSString *)");
  }

  // Regression test: the wrong method name used for "f.group()" translation.
  public void testPrintlnWithMethodInvocation() throws IOException {
    String source = "public class A { "
        + "String group() { return \"foo\"; } "
        + "void test() { A a = new A(); System.out.println(a.group()); }}";
    String translation = translateSourceFile(source, "A", "A.m");
    assertTranslation(translation, "printlnWithNSString:[a group]];");
  }

  public void testStaticArrayInitializerMove() throws IOException {
    String source = "class Test { static final double[] EVERY_SIXTEENTH_FACTORIAL = "
        + "{ 0x1.0p0, 0x1.30777758p44, 0x1.956ad0aae33a4p117, 0x1.ee69a78d72cb6p202, "
        + "0x1.fe478ee34844ap295, 0x1.c619094edabffp394, 0x1.3638dd7bd6347p498, "
        + "0x1.7cac197cfe503p605, 0x1.1e5dfc140e1e5p716, 0x1.8ce85fadb707ep829, "
        + "0x1.95d5f3d928edep945 }; }";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation, "{ 1.0, 2.0922789888E13, 2.631308369336935E35, "
        + "1.2413915592536073E61, 1.2688693218588417E89, 7.156945704626381E118, "
        + "9.916779348709496E149, 1.974506857221074E182, 3.856204823625804E215, "
        + "5.5502938327393044E249, 4.7147236359920616E284 }");
  }

  public void testTypeCheckInCompareToMethod() throws IOException {
    String translation = translateSourceFile(
        "class Test implements Comparable<Test> { int i; "
        + "  public int compareTo(Test t) { return i - t.i; } }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "- (jint)compareToWithId:(Test *)t {",
        "cast_chk(t, [Test class]);");
  }

  public void testAdditionWithinStringConcatenation() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test() { String s = 1 + 2.3f + \"foo\"; } }", "Test", "Test.m");
    assertTranslation(translation, "NSString *s = JreStrcat(\"F$\", 1 + 2.3f, @\"foo\");");
  }

  public void testMethodCollisionWithSuperclassField() throws IOException {
    addSourceFile("class A { protected int i; }", "A.java");
    String translation = translateSourceFile(
        "class B extends A { int i() { return i; } }", "B", "B.m");
    assertTranslation(translation, "return i_;");
  }

  public void testMultipleLabelsWithSameName() throws IOException {
    String translation = translateSourceFile(
        "public class Test { void test() { "
        + "  outer: for (int r = 0; r < 10; r++) {"
        + "    for (int s = 0; s < 10; s++) {"
        + "      break outer; }}"
        + "  outer: for (int t = 0; t < 10; t++) {"
        + "    for (int u = 0; u < 10; u++) {"
        + "      break outer; }}"
        + "  outer: for (int v = 0; v < 10; v++) {"
        + "    for (int w = 0; w < 10; w++) {"
        + "      break outer;"
        + "}}}}", "Test", "Test.m");
    assertTranslation(translation, "break_outer:");
    assertTranslation(translation, "goto break_outer;");
    assertTranslation(translation, "break_outer_2:");
    assertTranslation(translation, "goto break_outer_2;");
    assertTranslation(translation, "break_outer_3:");
    assertTranslation(translation, "goto break_outer_3;");
  }

  public void testExtraDimensionsInFieldDeclaration() throws IOException {
    String translation = translateSourceFile(
        "class Test { int i1, i2[], i3[][], i4[][][], i5[][], i6; }", "Test", "Test.h");
    if (options.isJDT()) {
      assertTranslatedLines(translation,
          "jint i1_, i6_;",
          "IOSIntArray *i2_;",
          "IOSObjectArray *i3_, *i5_;",
          "IOSObjectArray *i4_;");
    } else {
      assertTranslatedLines(translation,
          "jint i1_;",
          "IOSIntArray *i2_;",
          "IOSObjectArray *i3_;",
          "IOSObjectArray *i4_;",
          "IOSObjectArray *i5_;",
          "jint i6_;");
    }
  }

  public void testExtraDimensionsInVariableDeclarationStatement() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test() { char c1[][], c2[], c3, c4, c5[][]; } }", "Test", "Test.m");
    if (options.isJDT()) {
      assertTranslatedLines(translation,
          "IOSObjectArray *c1, *c5;",
          "IOSCharArray *c2;",
          "jchar c3, c4;");
    } else {
      assertTranslatedLines(translation,
          "IOSObjectArray *c1;",
          "IOSCharArray *c2;",
          "jchar c3;",
          "jchar c4;",
          "IOSObjectArray *c5;");
    }
  }

  // Objective-C requires that && tests be surrounded by parens when mixed with || tests.
  public void testLogicalPrecedence() throws IOException {
    String translation = translateSourceFile(
        "class Test { "
        + "boolean test1(boolean a, boolean b) {"
        + "  return a && b; }"
        + "boolean test2(boolean c, boolean d) {"
        + "  return c || d; }"
        + "boolean test3(boolean e, boolean f, boolean g, boolean h, boolean i) { "
        + "  return e && f || g && h || i; }"
        + "boolean test4(boolean j, boolean k, boolean l, boolean m, boolean n) {"
        + "  return j || k || l && m && n; }}",
        "Test", "Test.m");
    assertTranslation(translation, "return a && b;");
    assertTranslation(translation, "return c || d;");
    assertTranslatedLines(translation, "return (e && f) || (g && h) || i;");
    assertTranslatedLines(translation, "return j || k || (l && m && n);");

    translation = translateSourceFile(
        "class Test { int i; @Override public boolean equals(Object object) { "
        + "return (object == this) || (object instanceof Test) && (i == ((Test) object).i); } }",
        "Test", "Test.m");
    assertTranslatedLines(translation, "(object == self) || "
        + "(([object isKindOfClass:[Test class]]) && (i_ == ((Test *) nil_chk(((Test *) "
        + "cast_chk(object, [Test class]))))->i_));");
  }

  // Objective-C requires that bit-wise and tests be surrounded by parens when mixed with or tests.
  public void testBitPrecedence() throws IOException {
    String translation = translateSourceFile(
        "class Test { "
        + "int test1(int a, int b) {"
        + "  return a & b; }"
        + "int test2(int c, int d) {"
        + "  return c | d; }"
        + "int test3(int e, int f, int g, int h, int i) { "
        + "  return e & f | g & h | i; }"
        + "int test4(int j, int k, int l, int m, int n) {"
        + "  return j | k | l & m & n; }}",
        "Test", "Test.m");
    assertTranslation(translation, "return a & b;");
    assertTranslation(translation, "return c | d;");
    assertTranslatedLines(translation, "return (e & f) | (g & h) | i;");
    assertTranslatedLines(translation, "return j | k | (l & m & n);");
  }

  // C compiler requires that tests using & or | as boolean test have parentheses around
  // infix operands.
  public void testLowerPrecedence() throws IOException {
    String translation = translateSourceFile(
        "class Test { "
        + "boolean test1(int o, int p, int q) {"
        + "  return o < 0 | (o == 0 & p > q); } "
        + "boolean test2(int r) {"
        + "  return r < 0 & !isPowerOfTwo(r); } "
        + "boolean isPowerOfTwo(int i) { return false; }}",
        "Test", "Test.m");
    assertTranslatedLines(translation, "return (o < 0) | ((o == 0) & (p > q));");
    assertTranslatedLines(translation, "return (r < 0) & ![self isPowerOfTwoWithInt:r];");
  }

  public void testInitializeRenamed() throws IOException {
    String translation = translateSourceFile(
        "class Test { "
        + "  public static void initialize() {}}",
        "Test", "Test.m");
    assertTranslation(translation, "+ (void)initialize__ {");
  }

  // Verify minimal try-with-resources translation.
  public void testTryWithResourceNoCatchOrFinally() throws IOException {
    String translation = translateSourceFile(
        "import java.io.*; public class Test { String test(String path) throws IOException { "
        + "  try (BufferedReader br = new BufferedReader(new FileReader(path))) {"
        + "    return br.readLine(); } }}",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "JavaIoBufferedReader *br = create_JavaIoBufferedReader_initWithJavaIoReader_("
        + "create_JavaIoFileReader_initWithNSString_(path));",
        "JavaLangThrowable *__primaryException1 = nil;",
        "@try {",
        "  return [br readLine];",
        "}",
        "@catch (JavaLangThrowable *e) {",
        "  __primaryException1 = e;",
        "  @throw e;",
        "}",
        "@finally {",
        "  if (br != nil) {",
        "    if (__primaryException1 != nil) {",
        "      @try {",
        "        [br close];",
        "      }",
        "      @catch (JavaLangThrowable *e) {",
        "        [__primaryException1 addSuppressedWithJavaLangThrowable:e];",
        "      }",
        "    }",
        "    else {",
        "      [br close];",
        "    }",
        "  }",
        "}");
  }

  // Verify try-with-resources translation with multiple resources.
  public void testTryWithMultipleResourceNoCatchOrFinally() throws IOException {
    String translation = translateSourceFile(
        "import java.io.*; public class Test { String test(String path) throws IOException { "
        + "  try (BufferedReader br = new BufferedReader(new FileReader(path));"
        + "       BufferedReader br2 = new BufferedReader(new FileReader(path))) {"
        + "    return br.readLine(); } }}",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "JavaIoBufferedReader *br = create_JavaIoBufferedReader_initWithJavaIoReader_("
            + "create_JavaIoFileReader_initWithNSString_(path));",
        "JavaLangThrowable *__primaryException2 = nil;",
        "@try {",
        " JavaIoBufferedReader *br2 = create_JavaIoBufferedReader_initWithJavaIoReader_("
            + "create_JavaIoFileReader_initWithNSString_(path));",
        " JavaLangThrowable *__primaryException1 = nil;",
        " @try {",
        "  return [br readLine];",
        " }",
        " @catch (JavaLangThrowable *e) {",
        "  __primaryException1 = e;",
        "  @throw e;",
        " }",
        " @finally {",
        "  if (br2 != nil) {",
        "    if (__primaryException1 != nil) {",
        "      @try {",
        "        [br2 close];",
        "      }",
        "      @catch (JavaLangThrowable *e) {",
        "        [__primaryException1 addSuppressedWithJavaLangThrowable:e];",
        "      }",
        "    }",
        "    else {",
        "      [br2 close];",
        "    }",
        "  }",
        " }",
        "}",
        "@catch (JavaLangThrowable *e) {",
        " __primaryException2 = e;",
        " @throw e;",
        "}",
        "@finally {",
        " if (br != nil) {",
        "  if (__primaryException2 != nil) {",
        "   @try {",
        "    [br close];",
        "   }",
        "   @catch (JavaLangThrowable *e) {",
        "    [__primaryException2 addSuppressedWithJavaLangThrowable:e];",
        "   }",
        "  }",
        "  else {",
        "   [br close];",
        "  }",
        " }",
        "}");
  }

  // Verify try-with-resources translation is inside of try block with catch clause outside.
  public void testTryWithResourceAndCatch() throws IOException {
    String translation = translateSourceFile(
        "import java.io.*; public class Test { String test(String path) throws IOException { "
        + "  try (BufferedReader br = new BufferedReader(new FileReader(path))) {"
        + "    return br.readLine(); "
        + "  } catch (IOException e) {"
        + "    System.out.println(e);"
        + "    throw e;"
        + "  } }}",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "@try {",
        " JavaIoBufferedReader *br = create_JavaIoBufferedReader_initWithJavaIoReader_("
        + "create_JavaIoFileReader_initWithNSString_(path));",
        " JavaLangThrowable *__primaryException1 = nil;",
        " @try {",
        "  return [br readLine];",
        " }",
        " @catch (JavaLangThrowable *e) {",
        "  __primaryException1 = e;",
        "  @throw e;",
        " }",
        " @finally {",
        "  if (br != nil) {",
        "   if (__primaryException1 != nil) {",
        "    @try {",
        "     [br close];",
        "    }",
        "    @catch (JavaLangThrowable *e) {",
        "     [__primaryException1 addSuppressedWithJavaLangThrowable:e];",
        "    }",
        "   }",
        "   else {",
        "    [br close];",
        "   }",
        "  }",
        " }",
        "}",
        "@catch (JavaIoIOException *e) {",
        " [((JavaIoPrintStream *) nil_chk(JreLoadStatic(JavaLangSystem, out))) printlnWithId:e];",
        " @throw e;",
        "}");
  }
}
