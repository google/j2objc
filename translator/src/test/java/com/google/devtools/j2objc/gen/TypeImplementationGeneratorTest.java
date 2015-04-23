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

package com.google.devtools.j2objc.gen;

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.Options;

import java.io.IOException;

/**
 * Tests for {@link TypeImplementationGenerator}.
 *
 * @author Keith Stanger
 */
public class TypeImplementationGeneratorTest extends GenerationTest {

  public void testFieldAnnotationMethodForAnnotationType() throws IOException {
    String translation = translateSourceFile(
        "import java.lang.annotation.*; @Retention(RetentionPolicy.RUNTIME) "
        + "@interface A { @Deprecated int I = 5; }", "A", "A.m");
    assertTranslatedLines(translation,
        "+ (IOSObjectArray *)__annotations_I_ {",
        "  return [IOSObjectArray arrayWithObjects:(id[]) { "
          + "[[[JavaLangDeprecated alloc] init] autorelease] } count:1 "
          + "type:JavaLangAnnotationAnnotation_class_()];",
        "}");
  }

  public void testFieldAnnotationMethodForInterfaceType() throws IOException {
    String translation = translateSourceFile(
        "interface I { @Deprecated int I = 5; }", "I", "I.m");
    assertTranslatedLines(translation,
        "+ (IOSObjectArray *)__annotations_I_ {",
        "  return [IOSObjectArray arrayWithObjects:(id[]) { "
          + "[[[JavaLangDeprecated alloc] init] autorelease] } count:1 "
          + "type:JavaLangAnnotationAnnotation_class_()];",
        "}");
  }

  public void testFunctionLineNumbers() throws IOException {
    Options.setEmitLineDirectives(true);
    String translation = translateSourceFile("class A {\n\n"
        + "  static void test() {\n"
        + "    System.out.println(A.class);\n"
        + "  }}", "A", "A.m");
    assertTranslatedLines(translation,
        "#line 3", "void A_test() {", "A_initialize();", "", "#line 4",
        "[((JavaIoPrintStream *) nil_chk(JavaLangSystem_get_out_())) printlnWithId:A_class_()];");
  }
}
