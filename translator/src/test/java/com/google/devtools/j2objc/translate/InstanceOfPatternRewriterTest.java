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
            """
            class Test {
              void test() {
               Object o = "Hello";
               int i = o instanceof String s ? s.length() : 0;
              }
            }
            """,
            "Test",
            "Test.m");
    assertTranslatedLines(
        translation,
        """
        NSString *s = nil;
        id o = @"Hello";
        int32_t i = [o isKindOfClass:[NSString class]] && (s = (NSString *) o, true) ? [((NSString *) nil_chk(s)) java_length] : 0;
        """);
  }

  public void testVariableNaming() throws IOException {
    String translation =
        translateSourceFile(
            """
            class Test {
              void test() {
                Object o = "Hello";
                if (o instanceof String s) {
                  int i = s.length();
                }
                if (!(o instanceof String s)) {
                  return;
                }
                int j = s.length();
              }
            }
            """,
            "Test",
            "Test.m");
    assertTranslatedLines(
        translation,
        """
        NSString *s = nil;
        NSString *s_1 = nil;
        id o = @"Hello";
        if ([o isKindOfClass:[NSString class]] && (s_1 = (NSString *) o, true)) {
          int32_t i = [((NSString *) nil_chk(s_1)) java_length];
        }
        if (!([o isKindOfClass:[NSString class]] && (s = (NSString *) o, true))) {
          return;
        }
        int32_t j = [((NSString *) nil_chk(s)) java_length];
        """);
  }

  public void testPotentialSideEffects() throws IOException {
    String translation =
        translateSourceFile(
            """
            import java.util.List;
            class Test {
              void test(List<Object> l) {
                if (l.get(0) instanceof String s) {
                  int i = s.length();
                }
              }
            }
            """,
            "Test",
            "Test.m");
    assertTranslatedLines(
        translation,
        """
        id tmp = [((id<JavaUtilList>) nil_chk(l)) getWithInt:0];
        NSString *s = nil;
        if ([tmp isKindOfClass:[NSString class]] && (s = (NSString *) tmp, true)) {
          int32_t i = [((NSString *) nil_chk(s)) java_length];
        }
        """);
  }

  public void testIssue2580() throws IOException {
    String translation =
        translateSourceFile(
            """
            class Test {
              void test() {
                Object x = null ;
                Object y = "Hello";
                if (x != null && y instanceof String s) {
                 int i = s.length();
                }
                if (x != null && (y instanceof String s)) {
                 int i = s.length();
               }
              }
            }
            """,
            "Test",
            "Test.m");
    assertTranslatedLines(
        translation,
        """
        NSString *s = nil;
        NSString *s_1 = nil;
        id x = nil;
        id y = @"Hello";
        if (x != nil && [y isKindOfClass:[NSString class]] && (s_1 = (NSString *) y, true)) {
          int32_t i = [((NSString *) nil_chk(s_1)) java_length];
        }
        if (x != nil && ([y isKindOfClass:[NSString class]] && (s = (NSString *) y, true))) {
          int32_t i = [((NSString *) nil_chk(s)) java_length];
        }
        """);
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
        """
        NSString *s = nil;
        if (!([o isKindOfClass:[NSString class]] && (s = (NSString *) o, true))) {
        @throw create_JavaLangIllegalArgumentException_init();
        }
        [((JavaIoPrintStream *) nil_chk(JreLoadStatic(JavaLangSystem, out))) printlnWithNSString:[((NSString *) nil_chk(s)) java_repeat:5]];
        """);
  }

  public void testRecordPatterns() throws IOException {
    String translation =
        translateSourceFile(
            """
            class Test {
              record A(int i, String s) {}
              record B(Object a1, int i, A a2) {}
              void test() {
                Object o = new B(new A(1, ""), 3, new A(2, "bye"));
                if (o instanceof B(A(var i1, String s),int i2, var a)) {
                }
              }
            }
            """,
            "Test",
            "Test.m");
    assertTranslatedLines(
        translation,
        """
        Test_B *rec = nil;
        id comp = nil;
        Test_A *rec_1 = nil;
        int32_t i1 = 0;
        NSString *comp_1 = nil;
        NSString *s = nil;
        int32_t i2 = 0;
        Test_A *comp_2 = nil;
        Test_A *a = nil;
        id o = create_Test_B_initWithId_withInt_withTest_A_(create_Test_A_initWithInt_withNSString_(1, @""), 3, create_Test_A_initWithInt_withNSString_(2, @"bye"));
        if ([o isKindOfClass:[Test_B class]] && (rec = (Test_B *) o, true)\
         && [comp = [((Test_B *) nil_chk(rec)) a1] isKindOfClass:[Test_A class]] && (rec_1 = (Test_A *) comp, true)\
         && (i1 = (int32_t) [((Test_A *) nil_chk(rec_1)) i], true)\
         && [comp_1 = [((Test_A *) nil_chk(rec_1)) s] isKindOfClass:[NSString class]] && (s = comp_1, true)\
         && (i2 = (int32_t) [((Test_B *) nil_chk(rec)) i], true)\
         && [comp_2 = [((Test_B *) nil_chk(rec)) a2] isKindOfClass:[Test_A class]] && (a = comp_2, true)) {
        """);
  }
}
