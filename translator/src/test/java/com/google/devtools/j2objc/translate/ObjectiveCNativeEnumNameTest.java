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

public class ObjectiveCNativeEnumNameTest extends GenerationTest {

  @Override
  public void setUp() throws IOException {
    super.setUp();
  }

  public void testNameRequired() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.ObjectiveCNativeEnumName; "
            + "@ObjectiveCNativeEnumName(\"\")"
            + "public enum Color { RED, WHITE, BLUE };",
        "Color.java");
    @SuppressWarnings("unused")
    String testHeader = translateSourceFile("Color", "Color.h");
    assertError("ObjectiveCNativeEnumName must specify a name.");
  }

  public void testRenaming() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.ObjectiveCNativeEnumName; "
            + "@ObjectiveCNativeEnumName(\"RenamedColor\")"
            + "public enum Color { RED, WHITE, BLUE };",
        "Color.java");
    String testHeader = translateSourceFile("Color", "Color.h");

    assertTranslation(testHeader, "typedef NS_ENUM(jint, RenamedColor) {");
    assertTranslation(testHeader, "RenamedColorRed NS_SWIFT_NAME(red) = 0");
    assertTranslation(testHeader, "RenamedColorWhite NS_SWIFT_NAME(white) = 1");
    assertTranslation(testHeader, "RenamedColorBlue NS_SWIFT_NAME(blue) = 2");
    assertTranslation(testHeader, "#define Color_ORDINAL RenamedColor");
    assertTranslation(testHeader, "@interface Color : JavaLangEnum");
    assertTranslation(testHeader, "+ (Color *)fromNSEnum:(RenamedColor)value;");
  }
}
