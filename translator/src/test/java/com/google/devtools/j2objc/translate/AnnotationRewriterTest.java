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
 * Tests for {@link AnnotationRewriter}.
 *
 * @author Keith Stanger
 */
public class AnnotationRewriterTest extends GenerationTest {

  public void testEnumPropertyValue() throws IOException {
    String translation = translateSourceFile(
        "import java.lang.annotation.*;\n"
        + "@Retention(RetentionPolicy.RUNTIME) @interface A {}", "A", "A.m");
    // Make sure we get a "JreLoadEnum" instead of "JreEnum" macro.
    assertTranslation(translation,
        "create_JavaLangAnnotationRetention("
        + "JreLoadEnum(JavaLangAnnotationRetentionPolicy, RUNTIME))");
  }

  public void testDeallocMethodAdded() throws IOException {
    String translation = translateSourceFile(
        "import java.lang.annotation.*;\n"
        + "@Retention(RetentionPolicy.RUNTIME) @interface Test { String value(); }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "- (void)dealloc {",
        "  RELEASE_(value_);",
        "  [super dealloc];");
  }

  public void testCreateDescriptionMethod() throws IOException {
    String translation = translateSourceFile(
        "import java.lang.annotation.*;\n"
        + "@Retention(RetentionPolicy.RUNTIME)\n"
        + "public @interface A {\n"
        + "  String my_str() default \"dummy\";\n"
        + "  boolean my_bool() default false;\n"
        + "  byte my_byte() default 10;\n"
        + "  char my_char() default 'a';\n"
        + "  double my_double() default 1.0;\n"
        + "  float my_float() default 2.0f;\n"
        + "  int my_int() default 3;\n"
        + "  long my_long() default 4L;\n"
        + "  short my_short() default 5;\n"
        + "}\n", "A", "A.m");
    assertTranslation(translation,
        "return [NSString stringWithFormat:@\"@A(my_str=%@, my_bool=%d, my_byte=%d, my_char=%c,"
        + " my_double=%lf, my_float=%f, my_int=%d, my_long=%lld, my_short=%hd)\","
        + " my_str_, my_bool_, my_byte_, my_char_, my_double_, my_float_, my_int_, my_long_,"
        + " my_short_];");

    translation = translateSourceFile(
        "import java.lang.annotation.*;\n"
        + "@Retention(RetentionPolicy.RUNTIME)\n"
        + "public @interface B {\n"
        + "}\n", "B", "B.m");
    assertTranslation(translation, "return @\"@B()\";");
  }
}
