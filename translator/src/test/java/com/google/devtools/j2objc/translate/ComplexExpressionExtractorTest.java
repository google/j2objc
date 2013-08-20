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
        "class Test { void test() { StringBuilder sb = new StringBuilder(); " +
        "sb.append(\"a\").append(\"b\").append(\"c\").append(\"d\").append(\"e\").append(\"f\")" +
        ".append(\"f\"); } }", "Test", "Test.m");
    assertTranslation(translation, "JavaLangStringBuilder *complex$1 = " +
        "[((JavaLangStringBuilder *) nil_chk([((JavaLangStringBuilder *) " +
        "nil_chk([sb appendWithNSString:@\"a\"])) " +
        "appendWithNSString:@\"b\"])) appendWithNSString:@\"c\"];");
    assertTranslation(translation, "JavaLangStringBuilder *complex$2 = " +
        "[((JavaLangStringBuilder *) nil_chk([((JavaLangStringBuilder *) " +
        "nil_chk([((JavaLangStringBuilder *) nil_chk(complex$1)) appendWithNSString:@\"d\"])) " +
        "appendWithNSString:@\"e\"])) appendWithNSString:@\"f\"];");
    assertTranslation(translation,
        "[((JavaLangStringBuilder *) nil_chk(complex$2)) appendWithNSString:@\"f\"];");
  }

  public void testLongExpression() throws IOException {
    String translation = translateSourceFile(
        "class Test { int test() { return 1 + 2 - 3 + 4 - 5 + 6 - 7 + 8 - 9; } }",
        "Test", "Test.m");
    assertTranslation(translation, "int complex$1 = 1 + 2 - 3 + 4;");
    assertTranslation(translation, "int complex$2 = complex$1 - 5 + 6 - 7;");
    assertTranslation(translation, "return complex$2 + 8 - 9;");
  }
}
