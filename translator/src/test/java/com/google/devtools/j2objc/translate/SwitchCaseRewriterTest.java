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
 * Unit tests for {@link SwitchCaseRewriter}.
 */
public class SwitchCaseRewriterTest extends GenerationTest {

  @SuppressWarnings("StringConcatToTextBlock")
  public void testSwitchCaseWithInstanceOf() throws IOException {
    String source =
        """
        class Test {
            static String test(Object obj) {
                return switch (obj) {
                    case String s -> "It's a String: " + s;
                    case Integer i -> "It's an Integer: " + i;
                    default -> "It's something else.";
                };
            }
        }
        """;
    String translation = translateSourceFile(source, "Test", "Test.m");

    // In the translated code, the type-pattern switch statement should be converted to
    // a series of if-else-if statements that check the type of the object.
    assertTranslatedLines(translation,
        "if ([obj isKindOfClass:[NSString class]]) {",
        "  return JreStrcat(\"$$\", @\"It's a String: \", ((NSString *) obj));",
        "}",
        "else if ([obj isKindOfClass:[JavaLangInteger class]]) {",
        "  return JreStrcat(\"$@\", @\"It's an Integer: \", ((JavaLangInteger *) obj));",
        "}",
        "else {",
        "  return @\"It's something else.\";",
        "}");
  }

  @SuppressWarnings("StringConcatToTextBlock")
  public void testSwitchCaseWithOverlappingGuards() throws IOException {
    String source =
"""
        class Test {
          void checkObject(Object obj) {
            switch (obj) {
              // The > 25 test must still be tested before > 5.
              case String s when s.length() > 25 -> log("Long string");
              case String s when s.length() > 5 -> log("Medium string");
              // Test without guard.
              case String s -> log("Short string");
              // Test with different pattern, no guard.
              case Integer i -> log("An integer: " + i);
              default -> log("Something else");
            }
          }
          private static void log(String s) {
            System.out.println(s);
          }
        }
""";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslatedLines(
        translation,
        "- (void)checkObjectWithId:(id)obj {",
        "  if ([obj isKindOfClass:[NSString class]] && "
            + "[((NSString *) obj) java_length] > 25) {",
        "    Test_logWithNSString_(@\"Long string\");",
        "  }",
        "  else if ([obj isKindOfClass:[NSString class]] && "
            + "[((NSString *) obj) java_length] > 5) {",
        "    Test_logWithNSString_(@\"Medium string\");",
        "  }",
        "  else if ([obj isKindOfClass:[NSString class]]) {",
        "    Test_logWithNSString_(@\"Short string\");",
        "  }",
        "  else if ([obj isKindOfClass:[JavaLangInteger class]]) {",
        "    Test_logWithNSString_(JreStrcat(\"$@\", "
            + "@\"An integer: \", ((JavaLangInteger *) obj)));",
        "  }",
        "  else {",
        "    Test_logWithNSString_(@\"Something else\");",
        "  }",
        "}");
  }

  // Verify a switch expression case can have one or more statements without a block.
  @SuppressWarnings("StringConcatToTextBlock")
  public void testSwitchCaseWithStatements() throws IOException {
    String source =
        """
        class Test {
          String typeGuardIfTrueSwitchExpression(Object o) {
            Object o2 = "";
            return switch (o) {
               case Integer i when i == 0 && i < 1 && o2 instanceof String s: o = s + String.valueOf(i); yield "true";
               case Integer i when i == 0 || i > 1: o = String.valueOf(i); yield "second";
               case Object x: yield "any";
            };
          }
        }
        """;
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslatedLines(
        translation,
        "NSString *s;",
        "id o2 = @\"\";",
        "if ([o isKindOfClass:[JavaLangInteger class]] && [((JavaLangInteger *)"
            + " o) intValue] == 0 && [((JavaLangInteger *) o)"
            + " intValue] < 1 && (s = [o2 isKindOfClass:[NSString class]] ? (NSString *) o2 :"
            + " nil, !JreStringEqualsEquals(s, nil))) {",
        "  return @\"true\";",
        "}",
        "else if ([o isKindOfClass:[JavaLangInteger class]] && [((JavaLangInteger *)"
            + " o) intValue] == 0 || [((JavaLangInteger *) o)"
            + " intValue] > 1) {",
        "  return @\"second\";",
        "}",
        "else if ([o isKindOfClass:[NSObject class]]) {",
        "  return @\"any\";",
        "}");
  }

  @SuppressWarnings("StringConcatToTextBlock")
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
        // "s" (which is renamed to s$pattern$0) must be declared outside the scope of the
        // if statement below since it will be access outside.
        "NSString *s;",
        "if (!((s = [o isKindOfClass:[NSString class]] ? (NSString"
            + " *) o : nil, !JreStringEqualsEquals(s, nil)))) {",
        "@throw create_JavaLangIllegalArgumentException_init();",
        "}",
        "[((JavaIoPrintStream *) nil_chk(JreLoadStatic(JavaLangSystem, out)))"
            + " printlnWithNSString:[s java_repeat:5]];");
  }
}
