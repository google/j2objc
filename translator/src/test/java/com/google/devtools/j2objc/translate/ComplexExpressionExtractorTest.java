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
 * Unit tests for {@link ComplexExpressionExtractor}.
 *
 * @author Keith Stanger
 */
public class ComplexExpressionExtractorTest extends GenerationTest {

  @Override
  protected void setUp() throws IOException {
    super.setUp();
    ComplexExpressionExtractor.setMaxDepth(3);
  }

  @Override
  protected void tearDown() throws Exception {
    ComplexExpressionExtractor.resetMaxDepth();
    super.tearDown();
  }

  public void testChainedMethod() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test() { StringBuilder sb = new StringBuilder(); "
        + "sb.append(\"a\").append(\"b\").append(\"c\").append(\"d\").append(\"e\").append(\"f\")"
        + ".append(\"g\"); } }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "JavaLangStringBuilder *complex$1 = [((JavaLangStringBuilder *) "
            + "nil_chk([sb appendWithNSString:@\"a\"])) appendWithNSString:@\"b\"];",
        "JavaLangStringBuilder *complex$2 = nil_chk([((JavaLangStringBuilder *) "
            + "nil_chk(complex$1)) appendWithNSString:@\"c\"]);",
        "JavaLangStringBuilder *complex$3 = [((JavaLangStringBuilder *) "
            + "nil_chk([complex$2 appendWithNSString:@\"d\"])) appendWithNSString:@\"e\"];",
        "JavaLangStringBuilder *complex$4 = nil_chk([((JavaLangStringBuilder *) "
            + "nil_chk(complex$3)) appendWithNSString:@\"f\"]);",
        "[complex$4 appendWithNSString:@\"g\"];");
  }

  public void testComplexExpressionWithinStaticInit() throws IOException {
    String translation = translateSourceFile(
        "class Test { static String s = new StringBuilder().append('a').append('b').toString(); }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "JavaLangStringBuilder *complex$1 = nil_chk([((JavaLangStringBuilder *) "
          + "[new_JavaLangStringBuilder_init() autorelease]) appendWithChar:'a']);",
        "NSString *complex$2 = [((JavaLangStringBuilder *) nil_chk([complex$1 "
          + "appendWithChar:'b'])) description];",
        "JreStrongAssign(&Test_s_, complex$2);");
  }

  public void testLongExpression() throws IOException {
    String translation = translateSourceFile(
        "class Test { int test() { return 1 + 2 - 3 + 4 - 5 + 6 - 7 + 8 - 9; } }",
        "Test", "Test.m");
    assertTranslation(translation, "int complex$1 = 1 + 2 - 3 + 4;");
    assertTranslation(translation, "int complex$2 = complex$1 - 5 + 6 - 7;");
    assertTranslation(translation, "return complex$2 + 8 - 9;");
  }

  public void testAssignCompareExpression() throws IOException {
    String translation = translateSourceFile(
        "class Test { boolean b; void test(int i) { if (b = i == 0) {} } }",
        "Test", "Test.m");
    assertTranslation(translation, "if ((b_ = (i == 0))) {");
  }

  public void testIfAssignExpression() throws IOException {
    String translation = translateSourceFile(
        "class Test { boolean foo; void test(boolean b) { if (foo = b) {} } }",
        "Test", "Test.m");
    assertTranslation(translation, "if ((foo_ = b)) {");
  }

  public void testWhileAssignExpression() throws IOException {
    String translation = translateSourceFile(
        "class Test { boolean foo; void test(boolean b) { while (foo = b) {} } }",
        "Test", "Test.m");
    assertTranslation(translation, "while ((foo_ = b)) {");
  }

  public void testDoAssignExpression() throws IOException {
    String translation = translateSourceFile(
        "class Test { boolean foo; void test(boolean b) { do {} while (foo = b); } }",
        "Test", "Test.m");
    assertTranslation(translation, "while ((foo_ = b));");
  }

  // Verify that multiple parentheses are removed from equality (==) expressions.
  // This avoids clang's -Wparentheses-equality warning.
  public void testDoubleParentheses() throws IOException {
    String translation = translateSourceFile(
        "class Test { int foo; int bar;"
        + "  boolean test(int b) { return (((((foo == b))))); } "
        + "  int test2(int b) { if ((bar == b)) { return 42; } else { return 666; }}}",
        "Test", "Test.m");
    assertTranslation(translation, "return (foo_ == b);");
    assertTranslation(translation, "if (bar_ == b) {");
  }
}
