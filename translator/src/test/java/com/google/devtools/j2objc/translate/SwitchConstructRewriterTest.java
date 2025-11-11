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

/** Unit tests for {@link SwitchConstructRewriter}. */
public class SwitchConstructRewriterTest extends GenerationTest {

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
    assertTranslatedLines(
        translation,
        """
        return ^ NSString * (){
          {
            JavaLangInteger *i = nil;
            NSString *s = nil;
            int32_t selector = 0;
            if ([obj isKindOfClass:[NSString class]] && (s = (NSString *) obj, true)) selector = 1;
            else if ([obj isKindOfClass:[JavaLangInteger class]] && (i = (JavaLangInteger *) obj, true)) selector = 2;
            switch (selector) {
              case 1:
              return JreStrcat("$$", @"It's a String: ", s);
              case 2:
              return JreStrcat("$@", @"It's an Integer: ", i);
              default:
              return @"It's something else.";
            }
          }
        """);
  }

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
        """
        JavaLangInteger *i = nil;
        NSString *s = nil;
        NSString *s_1 = nil;
        NSString *s_2 = nil;
        int32_t selector = 0;
        if ([obj isKindOfClass:[NSString class]] && (s_2 = (NSString *) obj, true) && [((NSString *) nil_chk(s_2)) java_length] > 25) selector = 1;
        else if ([obj isKindOfClass:[NSString class]] && (s_1 = (NSString *) obj, true) && [((NSString *) nil_chk(s_1)) java_length] > 5) selector = 2;
        else if ([obj isKindOfClass:[NSString class]] && (s = (NSString *) obj, true)) selector = 3;
        else if ([obj isKindOfClass:[JavaLangInteger class]] && (i = (JavaLangInteger *) obj, true)) selector = 4;
        switch (selector) {
          case 1:
          {
            Test_logWithNSString_(@"Long string");
            break;
          }
          case 2:
          {
            Test_logWithNSString_(@"Medium string");
            break;
          }
          case 3:
          {
            Test_logWithNSString_(@"Short string");
            break;
          }
          case 4:
          {
            Test_logWithNSString_(JreStrcat("$@", @"An integer: ", i));
            break;
          }
          default:
          {
            Test_logWithNSString_(@"Something else");
            break;
          }
        }
        """);
  }

  // Verify a switch expression case can have one or more statements without a block.
  public void testSwitchbCaseWithStatements() throws IOException {
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
        """
        return ^ NSString * (){
          {
            id x = nil;
            JavaLangInteger *i = nil;
            NSString *s = nil;
            JavaLangInteger *i_1 = nil;
            int32_t selector = 0;
            if ([o isKindOfClass:[JavaLangInteger class]] && (i_1 = (JavaLangInteger *) o, true) && [((JavaLangInteger *) nil_chk(i_1)) intValue] == 0 && [i_1 intValue] < 1 && [o2 isKindOfClass:[NSString class]] && (s = (NSString *) o2, true)) selector = 1;
            else if ([o isKindOfClass:[JavaLangInteger class]] && (i = (JavaLangInteger *) o, true) && [((JavaLangInteger *) nil_chk(i)) intValue] == 0 || [i intValue] > 1) selector = 2;
            else if ([o isKindOfClass:[NSObject class]] && (x = o, true)) selector = 3;
            switch (selector) {
              case 1:
              o = JreStrcat("$$", s, NSString_java_valueOf_(i_1));
              return @"true";
              case 2:
              o = NSString_java_valueOf_(i);
              return @"second";
              case 3:
              return @"any";
              default:
              __builtin_unreachable();
            }
          }
        }();
        """);
  }
}
