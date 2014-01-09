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
import com.google.devtools.j2objc.Options;

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
    Options.enableExtractUnsequencedModifications();
  }

  @Override
  protected void tearDown() throws Exception {
    Options.resetExtractUnsequencedModifications();
    super.tearDown();
  }

  public void testUnsequencedPrefixExpression() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test(int i) { int j = ++i - ++i; } }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "int unseq$1 = ++i;",
        "int j = unseq$1 - ++i;");
  }

  public void testUnsequencedAssignmentExpression() throws IOException {
    String translation = translateSourceFile(
        "class Test { int test(int[] data, int i) { return data[i += 2] + i; } }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "int unseq$1 = i += 2;",
        "return IOSIntArray_Get(nil_chk(data), unseq$1) + i;");
  }

  public void testUnsequencedConditionalInfixExpression() throws IOException {
    String translation = translateSourceFile(
        "class Test { boolean test(int i) { " +
        "return i == 0 || i == 1 || ++i + i == 2 || i++ + i == 3 || i == 4; } }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "BOOL unseq$1;",
        "if (!(unseq$1 = i == 0 || i == 1)) {",
        "  int unseq$2 = ++i;",
        "  unseq$1 = unseq$1 || unseq$2 + i == 2;",
        "}",
        "BOOL unseq$3;",
        "if (!(unseq$3 = unseq$1)) {",
        "  int unseq$4 = i++;",
        "  unseq$3 = unseq$3 || unseq$4 + i == 3;",
        "}",
        "return unseq$3 || i == 4;");
  }

  public void testUnsequencedConditionalExpression() throws IOException {
    String translation = translateSourceFile(
        "class Test { boolean test(int i) { " +
        "return i == 0 ? i++ + i == 0 || i++ + i == 0 : ++i == 1; } }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "BOOL unseq$1;",
        "if (i == 0) {",
        "  int unseq$2 = i++;",
        "  BOOL unseq$3;",
        "  if (!(unseq$3 = unseq$2 + i == 0)) {",
        "    int unseq$4 = i++;",
        "    unseq$3 = unseq$3 || unseq$4 + i == 0;",
        "  }",
        "  unseq$1 = unseq$3;",
        "}",
        "else {",
        "  unseq$1 = ++i == 1;",
        "}",
        "return unseq$1;");
  }

  public void testWhileLoop() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test(int i) { while (i + i++ < 10) {} } }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "while (YES) {",
        "  int unseq$1 = i;",
        "  if (!(unseq$1 + i++ < 10)) break;");
  }

  public void testVariableDeclarationStatementIsSplit() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test() { int i = 0, j = i++ + i, k = j, l = --k - k, m = 1; } }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "int i = 0;",
        "int unseq$1 = i++;",
        "int j = unseq$1 + i, k = j;",
        "int unseq$2 = --k;",
        "int l = unseq$2 - k, m = 1;");
  }
}
