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

package com.google.devtools.j2objc.ast;

import com.google.devtools.j2objc.GenerationTest;
import java.io.IOException;

/**
 * Tests for {@link Annotation}.
 */
public class AnnotationTest extends GenerationTest {

  // Issue 470: ClassCastException converting annotation with simple members.
  public void testAnnotationTypeMemberConversion() throws IOException {
    String headers = "import java.lang.annotation.*; "
        + "import static java.lang.annotation.ElementType.*; "
        + "import static java.lang.annotation.RetentionPolicy.*; ";
    addSourceFile(headers
        + "@Retention(RUNTIME) @Target({TYPE, METHOD, FIELD}) public @interface Simple { "
        + "String value() default \"default_value\"; }", "Simple.java");
    String translation = translateSourceFile(headers
        + "@Retention(RUNTIME) @Target({TYPE, METHOD, FIELD}) public @interface Complex { "
        + "Simple member() default @Simple; }", "Complex", "Complex.m");
    assertTranslatedLines(translation,
        "+ (id<Simple>)memberDefault {", "return create_Simple(@\"default_value\");", "}");
  }

  // Issue 471: ClassCastException converting annotation with array members.
  public void testAnnotationTypeArrayMemberConversion() throws IOException {
    String headers = "import java.lang.annotation.*; "
        + "import static java.lang.annotation.ElementType.*; "
        + "import static java.lang.annotation.RetentionPolicy.*; ";
    addSourceFile(headers
        + "@Retention(RUNTIME) @Target({TYPE, METHOD, FIELD}) public @interface Simple { "
        + "String value() default \"default_value\"; }", "Simple.java");
    String translation = translateSourceFile(headers
        + "@Retention(RUNTIME) @Target({FIELD, METHOD}) public @interface Elements { "
        + "Simple[] value(); }", "Elements", "Elements.h");
    assertTranslation(translation, "@property (readonly) IOSObjectArray *value;");
  }
}
