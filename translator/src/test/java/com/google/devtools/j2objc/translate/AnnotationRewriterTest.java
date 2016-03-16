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
}
