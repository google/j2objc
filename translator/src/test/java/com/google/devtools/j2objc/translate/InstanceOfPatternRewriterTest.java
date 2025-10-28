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
 * Unit tests for {@link InstanceOfPatternRewriter}.
 *
 * @author Roberto Lublinerman
 */
public class InstanceOfPatternRewriterTest extends GenerationTest {

  public void testConditionExpression() throws IOException {
    String translation =
        translateSourceFile(
            "class Test {"
                + " void test() { Object o = \"Hello\"; int i = o instanceof String s ?"
                + " s.length() : 0; }}",
            "Test",
            "Test.m");
    assertTranslatedLines(
        translation,
        "NSString *s$pattern$0;",
        "id o = @\"Hello\";",
        "int32_t i = (s$pattern$0 = [o isKindOfClass:[NSString class]] ? (NSString"
            + " *) o : nil, !JreStringEqualsEquals(s$pattern$0, nil)) ? [s$pattern$0"
            + " java_length] : 0;");
  }

  public void testVariableNaming() throws IOException {
    String translation =
        translateSourceFile(
            "class Test {"
                + " void test() { "
                + "    Object o = \"Hello\"; "
                + "    if (o instanceof String s) { int i = s.length(); }"
                + "    if (!(o instanceof String s)) { return; }"
                + "    int j = s.length(); }}",
            "Test",
            "Test.m");
    assertTranslatedLines(
        translation,
        "NSString *s$pattern$1;",
        "NSString *s$pattern$0;",
        "id o = @\"Hello\";",
        "if ((s$pattern$0 = [o isKindOfClass:[NSString class]] ? (NSString *) o"
            + " : nil, !JreStringEqualsEquals(s$pattern$0, nil))) {",
        "    int32_t i = [s$pattern$0 java_length];",
        "}",
        "if (!((s$pattern$1 = [o isKindOfClass:[NSString class]] ? (NSString *)"
            + " o : nil, !JreStringEqualsEquals(s$pattern$1, nil)))) {",
        "return;",
        "}",
        "int32_t j = [s$pattern$1 java_length];");
  }

  public void testPotentialSideEffects() throws IOException {
    String translation =
        translateSourceFile(
            "import java.util.List;"
                + "class Test {"
                + " void test(List<Object> l) { "
                + "    if (l.get(0) instanceof String s) { int i = s.length(); }}}",
            "Test",
            "Test.m");
    assertTranslatedLines(
        translation,
        "id tmp$instanceof$0;",
        "NSString *s$pattern$0;",
        "if ((tmp$instanceof$0 = [((id<JavaUtilList>) nil_chk(l)) getWithInt:0], s$pattern$0 = "
            + "[tmp$instanceof$0 isKindOfClass:[NSString class]] ? (NSString *) tmp$instanceof$0"
            + " : nil, !JreStringEqualsEquals(s$pattern$0, nil))) {",
        "    int32_t i = [s$pattern$0 java_length];",
        "}");
  }

  public void testIssue2580() throws IOException {
    String translation =
        translateSourceFile(
            "class Test {"
                + " void test() { "
                + "    Object x = null ; "
                + "    Object y = \"Hello\"; "
                + "    if (x != null && y instanceof String s) { int i = s.length(); }"
                + "    if (x != null && (y instanceof String s)) { int i = s.length(); }"
                + "}}",
            "Test",
            "Test.m");
    assertTranslatedLines(
        translation,
        "NSString *s$pattern$1;",
        "NSString *s$pattern$0;",
        "id x = nil;",
        "id y = @\"Hello\";",
        "if (x != nil && (s$pattern$0 = [y isKindOfClass:[NSString class]] ?"
            + " (NSString *) y : nil, !JreStringEqualsEquals(s$pattern$0, nil))) {",
        "int32_t i = [((NSString *) nil_chk(s$pattern$0)) java_length];",
        "}",
        "if (x != nil && ((s$pattern$1 = [y isKindOfClass:[NSString class]] ?"
            + " (NSString *) y : nil, !JreStringEqualsEquals(s$pattern$1, nil)))) {",
        " int32_t i = [((NSString *) nil_chk(s$pattern$1)) java_length];",
        "}");
  }
}
