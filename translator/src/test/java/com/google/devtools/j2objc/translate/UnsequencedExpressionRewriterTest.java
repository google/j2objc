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
import java.io.IOException;

/**
 * Unit tests for {@link UnsequencedExpressionRewriter}.
 *
 * @author Keith Stanger
 */
public class UnsequencedExpressionRewriterTest extends GenerationTest {

  @Override
  protected void setUp() throws IOException {
    super.setUp();
    options.enableExtractUnsequencedModifications();
  }

  public void testUnsequencedPrefixExpression() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test(int i) { int j = ++i - ++i; } }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "int32_t unseq$1 = ++i;",
        "int32_t j = unseq$1 - ++i;");
  }

  public void testUnsequencedAssignmentExpression() throws IOException {
    String translation = translateSourceFile(
        "class Test { int test(int[] data, int i) { return data[i += 2] + i; } }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "int32_t unseq$1 = i += 2;",
        "return IOSIntArray_Get(nil_chk(data), unseq$1) + i;");
  }

  public void testUnsequencedConditionalInfixExpression() throws IOException {
    String translation = translateSourceFile(
        "class Test { boolean test(int i) { "
        + "return i == 0 || i == 1 || ++i + i == 2 || i++ + i == 3 || i == 4; } }",
        "Test", "Test.m");
    assertTranslatedLines(
        translation,
        "bool unseq$1;",
        "if (!(unseq$1 = (i == 0 || i == 1))) {",
        "  int32_t unseq$2 = ++i;",
        "  if (!(unseq$1 = (unseq$2 + i == 2))) {",
        "    int32_t unseq$3 = i++;",
        "    unseq$1 = (unseq$1 || unseq$3 + i == 3 || i == 4);",
        "  }",
        "}",
        "return unseq$1;");
  }

  public void testUnsequencedConditionalExpression() throws IOException {
    String translation = translateSourceFile(
        "class Test {"
        + " boolean test(int i) {  return i == 0 ? i++ + i == 0 || i++ + i == 0 : ++i == 1; }"
        + " boolean test2(int i) { return i == 0 ? ++i == 1 : i++ + i == 0 || i++ + i == 0; } }",
        "Test", "Test.m");
    assertTranslatedLines(
        translation,
        "- (bool)testWithInt:(int32_t)i {",
        "  bool unseq$1;",
        "  if (i == 0) {",
        "    int32_t unseq$2 = i++;",
        "    bool unseq$3;",
        "    if (!(unseq$3 = (unseq$2 + i == 0))) {",
        "      int32_t unseq$4 = i++;",
        "      unseq$3 = (unseq$3 || unseq$4 + i == 0);",
        "    }",
        "    unseq$1 = unseq$3;",
        "  }",
        "  else {",
        "    unseq$1 = (++i == 1);",
        "  }",
        "  return unseq$1;",
        "}");
    assertTranslatedLines(
        translation,
        "- (bool)test2WithInt:(int32_t)i {",
        "  bool unseq$1;",
        "  if (i == 0) {",
        "    unseq$1 = (++i == 1);",
        "  }",
        "  else {",
        "    int32_t unseq$2 = i++;",
        "    bool unseq$3;",
        "    if (!(unseq$3 = (unseq$2 + i == 0))) {",
        "      int32_t unseq$4 = i++;",
        "      unseq$3 = (unseq$3 || unseq$4 + i == 0);",
        "    }",
        "    unseq$1 = unseq$3;",
        "  }",
        "  return unseq$1;",
        "}");
  }

  public void testWhileLoop() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test(int i) { while (i + i++ < 10) {} } }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "while (true) {",
        "  int32_t unseq$1 = i;",
        "  if (!(unseq$1 + i++ < 10)) break;");
  }

  // https://github.com/google/j2objc/issues/1487
  public void testMethodConditionalParameter() throws IOException {
    String translation =
        translateSourceFile(
            "class Test { \n"
                + "int test(Object o) { \n"
                + "int i = 42; \n"
                + "int j = -1; \n"
                // Depending on parameter expr execution order, either cmp(42, 84),
                // cmp(42, -2) or cmp(42, fn.applyAsInt(j, 2)) is executed.
                + "return cmp(j = i, (o == null) ? j : j * 2); }\n"
                + "int cmp(int a, int b) { return 0; }\n}",
            "Test",
            "Test.m");
    assertTranslatedLines(
        translation,
        "int32_t unseq$1 = j = i;",
        "return [self cmpWithInt:unseq$1 withInt:(o == nil) ? j : j * 2];");
  }

  // https://github.com/google/j2objc/issues/1487
  public void testConditionalsInsideNonConditionalInfixExpression() throws IOException {
    String translation =
        translateSourceFile(
            "class Test { \n"
                + "int test(byte[] input, byte[] tail, int tailLen, int p, int t) { \n"
                + "return (((tailLen > 1 ? tail[t++] : input[p++]) & 0xff) << 10) |\n"
                + "     (((tailLen > 0 ? tail[t++] : input[p++]) & 0xff) << 2);\n}\n}",
            "Test",
            "Test.m");
    assertTranslatedLines(
        translation,
        "  int8_t unseq$1;",
        "if (tailLen > 1) {",
        "int32_t unseq$2 = t++;",
        "unseq$1 = IOSByteArray_Get(nil_chk(tail), unseq$2);",
        "}",
        "else {",
        "int32_t unseq$3 = p++;",
        "unseq$1 = IOSByteArray_Get(nil_chk(input), unseq$3);",
        "}",
        "return (JreLShift32(((unseq$1) & (int32_t) 0xff), 10)) "
            + "| (JreLShift32(((tailLen > 0 ? IOSByteArray_Get(nil_chk(tail), t++) "
            + ": IOSByteArray_Get(nil_chk(input), p++)) & (int32_t) 0xff), 2));");
  }

  public void testVariableDeclarationStatementIsSplit() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test() { int i = 0, j = i++ + i, k = j, l = --k - k, m = 1; } }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "int32_t i = 0;",
        "int32_t unseq$1 = i++;",
        "int32_t j = unseq$1 + i;",
        "int32_t k = j;",
        "int32_t unseq$2 = --k;",
        "int32_t l = unseq$2 - k;",
        "int32_t m = 1;");
  }

  public void testAssertStatement() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test(int i) { assert i++ + i++ == 0 : \"foo\" + i++ + i++; } }",
        "Test", "Test.m");
    assertTranslatedLines(
        translation,
        "int32_t unseq$1 = i++;",
        "bool unseq$2 = unseq$1 + i++ == 0;",
        "int32_t unseq$3 = i++;",
        "JreAssert(unseq$2, JreStrcat(\"$II\", @\"foo\", unseq$3, i++));");
  }

  public void testForInitStatements() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test() { int i = 0, j = 0, k = 0; "
        + "for (i = i++ + i++, j = i++ + i++, k = i++ + i++;;) { } } }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "int32_t i = 0;",
        "int32_t j = 0;",
        "int32_t k = 0;",
        "int32_t unseq$1 = i++;",
        "int32_t unseq$2 = i++;",
        "i = unseq$1 + unseq$2;",
        "int32_t unseq$3 = i++;",
        "j = unseq$3 + i++;",
        "int32_t unseq$4 = i++;",
        "for (k = unseq$4 + i++; ; ) {",
        "}");
  }

  public void testForInitWithDeclaration() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test() { int k = 0; "
        + "for (int i = k++ + k++, j = i++ + i++;;) { } } }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "int32_t k = 0;",
        "int32_t unseq$1 = k++;",
        "int32_t i = unseq$1 + k++;",
        "int32_t unseq$2 = i++;",
        "for (int32_t j = unseq$2 + i++; ; ) {",
        "}");
  }

  public void testIfConditionAndUpdaters() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test() { int k = 0; "
        + "for (int i = k++ + k++; i++ + i++ < 10; i++, k = i++ + i++) { "
        + "  String s = \"foo\" + i; } } }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "int32_t k = 0;",
        "int32_t unseq$1 = k++;",
        "for (int32_t i = unseq$1 + k++; ; ) {",
        "  int32_t unseq$2 = i++;",
        "  if (!(unseq$2 + i++ < 10)) break;",
        "  NSString *s = JreStrcat(\"$I\", @\"foo\", i);",
        "  i++;",
        "  int32_t unseq$3 = i++;",
        "  k = unseq$3 + i++;",
        "}");
  }

  public void testIfStatement() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test(int i) { "
        + "if (i++ + i++ == 0) {} else if (i++ + i++ == 1) {} else {} } }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "int32_t unseq$1 = i++;",
        "if (unseq$1 + i++ == 0) {",
        "}",
        "else {",
        "  int32_t unseq$2 = i++;",
        "  if (unseq$2 + i++ == 1) {",
        "  }",
        "  else {",
        "  }",
        "}");
  }

  public void testAssignToArray() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test(int[] arr, int i) { arr[i] = i++; } }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "int32_t unseq$1 = i;",
        "*IOSIntArray_GetRef(nil_chk(arr), unseq$1) = i++;");
  }

  // Make sure that a conditional access remains conditional. Even if the access
  // is not a modification, it might have been modified by the condition.
  public void testConditionalAccess() throws IOException {
    String translation = translateSourceFile(
        "class Test { boolean foo(int i, int j) { return i < j; }"
        + " boolean test1(boolean b, int i) { return b || foo(i, i++); }"
        + " boolean test2(boolean b, int i) { return b ? foo(i, i++) : false; } }",
        "Test", "Test.m");
    // test1
    assertTranslatedLines(
        translation,
        "bool unseq$1;",
        "if (!(unseq$1 = b)) {",
        "  int32_t unseq$2 = i;",
        "  unseq$1 = (unseq$1 || [self fooWithInt:unseq$2 withInt:i++]);",
        "}",
        "return unseq$1;");
    // test2
    assertTranslatedLines(
        translation,
        "bool unseq$1;",
        "if (b) {",
        "  int32_t unseq$2 = i;",
        "  unseq$1 = [self fooWithInt:unseq$2 withInt:i++];",
        "}",
        "else {",
        "  unseq$1 = false;",
        "}",
        "return unseq$1;");
  }

  // Instance variables do not appear to produce any unsequenced errors.
  // Regression test for Issue #748.
  public void testInstanceVarIsNotUnsequenced() throws IOException {
    String translation = translateSourceFile(
        "class Test { int i; void test() { this.i = this.i + this.i++; } }", "Test", "Test.m");
    assertInTranslation(translation, "self->i_ = self->i_ + self->i_++;");
  }
}
