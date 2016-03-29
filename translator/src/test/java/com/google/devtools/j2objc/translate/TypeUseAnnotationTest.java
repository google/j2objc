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
import com.google.devtools.j2objc.util.SourceVersion;

import java.io.IOException;

/**
 * Tests Java 8 type annotations.
 *
 * @author Keith Stanger
 */
public class TypeUseAnnotationTest extends GenerationTest {

  @Override
  protected void loadOptions() throws IOException {
    super.loadOptions();
    Options.setSourceVersion(SourceVersion.JAVA_8);
  }

  // Regression for Issue #730.
  public void testAnnotatedStringType() throws IOException {
    Options.setSourceVersion(SourceVersion.JAVA_8);
    createParser();
    addSourceFile(
        "import java.lang.annotation.*;\n"
        + "@Target(ElementType.TYPE_USE) @public @interface A {}", "A.java");
    String translation = translateSourceFile(
        "class Test { @A String str; @A String foo() { return null; } }",
        "Test", "Test.m");
    assertNotInTranslation(translation, "java/lang/String.h");
    assertNotInTranslation(translation, "JavaLangString");
  }
}
