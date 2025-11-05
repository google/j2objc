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
        "NSString *s = nil;",
        "id o = @\"Hello\";",
        "int32_t i = (s = [o isKindOfClass:[NSString class]] ? (NSString"
            + " *) o : nil, !JreStringEqualsEquals(s, nil)) ? [s"
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
        "NSString *s = nil;",
        "NSString *s_1 = nil;",
        "id o = @\"Hello\";",
        "if ((s_1 = [o isKindOfClass:[NSString class]] ? (NSString *) o"
            + " : nil, !JreStringEqualsEquals(s_1, nil))) {",
        "    int32_t i = [s_1 java_length];",
        "}",
        "if (!((s = [o isKindOfClass:[NSString class]] ? (NSString *)"
            + " o : nil, !JreStringEqualsEquals(s, nil)))) {",
        "return;",
        "}",
        "int32_t j = [s java_length];");
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
        "id tmp;",
        "NSString *s = nil;",
        "if ((tmp = [((id<JavaUtilList>) nil_chk(l)) getWithInt:0], s = "
            + "[tmp isKindOfClass:[NSString class]] ? (NSString *) tmp"
            + " : nil, !JreStringEqualsEquals(s, nil))) {",
        "    int32_t i = [s java_length];",
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
        "NSString *s = nil;",
        "NSString *s_1 = nil;",
        "id x = nil;",
        "id y = @\"Hello\";",
        "if (x != nil && (s_1 = [y isKindOfClass:[NSString class]] ?"
            + " (NSString *) y : nil, !JreStringEqualsEquals(s_1, nil))) {",
        "int32_t i = [((NSString *) nil_chk(s_1)) java_length];",
        "}",
        "if (x != nil && ((s = [y isKindOfClass:[NSString class]] ?"
            + " (NSString *) y : nil, !JreStringEqualsEquals(s, nil)))) {",
        "int32_t i = [((NSString *) nil_chk(s)) java_length];",
        "}");
  }

  public void testNegatedInstanceOfPattern() throws IOException {
    String source =
        """
        class Test {
          void test(Object o) {
            if (!(o instanceof String s)) {
              throw new IllegalArgumentException();
            }
            System.out.println(s.repeat(5));
          }
        }
        """;
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslatedLines(
        translation,
        // "s"  must be declared outside the scope of the if statement below since it will be
        // accessed outside.
        "NSString *s = nil;",
        "if (!((s = [o isKindOfClass:[NSString class]] ? (NSString"
            + " *) o : nil, !JreStringEqualsEquals(s, nil)))) {",
        "@throw create_JavaLangIllegalArgumentException_init();",
        "}",
        "[((JavaIoPrintStream *) nil_chk(JreLoadStatic(JavaLangSystem, out)))"
            + " printlnWithNSString:[s java_repeat:5]];");
  }
}
