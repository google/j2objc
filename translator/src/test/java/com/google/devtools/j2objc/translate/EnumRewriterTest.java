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
import com.google.devtools.j2objc.Options.MemoryManagementOption;
import java.io.IOException;

/**
 * Unit tests for {@link EnumRewriter}.
 *
 * @author Keith Stanger
 */
public class EnumRewriterTest extends GenerationTest {

  public void testGenericEnumConstructor() throws IOException {
    String translation = translateSourceFile(
        "enum Test { A(\"foo\"); private <T> Test(T t) {} }", "Test", "Test.m");
    assertTranslation(translation,
        "void Test_initWithId_withNSString_withInt_("
          + "Test *self, id t, NSString *__name, jint __ordinal) {");
    assertTranslatedLines(translation,
        "((void) (JreEnum(Test, A) = e = "
          + "objc_constructInstance(self, (void *)ptr)), ptr += objSize);",
        "Test_initWithId_withNSString_withInt_(e, @\"foo\", @\"A\", 0);");
  }

  public void testToNsEnumConversion() throws Exception {
    String translation = translateSourceFile("enum Test { A }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "- (Test_Enum)toNSEnum {",
        "  return (Test_Enum)[self ordinal];",
        "}");
  }

  public void testEmptyEnum() throws Exception {
    String header = translateSourceFile("enum Test {}", "Test", "Test.h");
    assertNotInTranslation(header, "Test_Enum");
    String source = getTranslatedFile("Test.m");
    assertNotInTranslation(source, "Test_Enum");
  }

  public void testSimpleEnumAllocationCode() throws Exception {
    String translation = translateSourceFile(
        "enum Test { A, B, C, D, E }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "size_t objSize = class_getInstanceSize(self);",
        "size_t allocSize = 5 * objSize;",
        "uintptr_t ptr = (uintptr_t)calloc(allocSize, 1);",
        "id e;",
        "for (jint i = 0; i < 5; i++) {",
        "((void)(Test_values_[i] = e = "
          + "objc_constructInstance(self, (void *)ptr)), ptr += objSize);",
        "Test_initWithNSString_withInt_(e, JreEnumConstantName(Test_class_(), i), i);",
        "}");
  }

  public void testSimpleEnumArc() throws Exception {
    options.setMemoryManagementOption(MemoryManagementOption.ARC);
    String translation = translateSourceFile(
        "enum Test { A, B, C }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "JreEnum(Test, A) = "
            + "new_Test_initWithNSString_withInt_(JreEnumConstantName(Test_class_(), 0), 0);",
        "JreEnum(Test, B) = "
            + "new_Test_initWithNSString_withInt_(JreEnumConstantName(Test_class_(), 1), 1);",
        "JreEnum(Test, C) = "
            + "new_Test_initWithNSString_withInt_(JreEnumConstantName(Test_class_(), 2), 2);",
        "J2OBJC_SET_INITIALIZED(Test)");
  }

  public void testSimpleEnumArcStripEnumConstants() throws Exception {
    options.setMemoryManagementOption(MemoryManagementOption.ARC);
    options.setStripEnumConstants(true);
    String translation = translateSourceFile(
        "enum Test { A, B, C }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "JreEnum(Test, A) = "
            + "new_Test_initWithNSString_withInt_(@\"JAVA_LANG_ENUM_NAME_STRIPPED\", 0);",
        "JreEnum(Test, B) = "
            + "new_Test_initWithNSString_withInt_(@\"JAVA_LANG_ENUM_NAME_STRIPPED\", 1);",
        "JreEnum(Test, C) = "
            + "new_Test_initWithNSString_withInt_(@\"JAVA_LANG_ENUM_NAME_STRIPPED\", 2);",
        "J2OBJC_SET_INITIALIZED(Test)");
  }

  // Verify normal reflection stripping doesn't affect enum names.
  public void testSimpleEnumReflectionStripped() throws Exception {
    options.setStripReflection(true);
    String translation = translateSourceFile(
        "enum Test { A, B, C, D, E }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "size_t objSize = class_getInstanceSize(self);",
        "size_t allocSize = 5 * objSize;",
        "uintptr_t ptr = (uintptr_t)calloc(allocSize, 1);",
        "id e;",
        "id names[] = {",
        "@\"A\", @\"B\", @\"C\", @\"D\", @\"E\",",
        "};",
        "for (jint i = 0; i < 5; i++) {",
        "((void)(Test_values_[i] = e = "
          + "objc_constructInstance(self, (void *)ptr)), ptr += objSize);",
        "Test_initWithNSString_withInt_(e, names[i], i);",
        "}");
  }

  public void testStrippedEnumName() throws Exception {
    options.setStripEnumConstants(true);
    String translation = translateSourceFile(
        "enum Test { APPLE, ORANGE, PEAR }", "Test", "Test.m");
    assertNotInTranslation(translation, "\"APPLE\"");
    assertNotInTranslation(translation, "\"ORANGE\"");
    assertNotInTranslation(translation, "\"PEAR\"");
  }

  public void testEnumAllocationCode() throws Exception {
    String translation = translateSourceFile(
        "enum Test { A, B { public String toString() { return \"foo\"; } }, C}", "Test", "Test.m");
    assertTranslatedLines(translation,
        "size_t objSize = class_getInstanceSize(self);",
        "size_t allocSize = 2 * objSize;",
        "size_t objSize_B = class_getInstanceSize([Test_1 class]);",
        "allocSize += objSize_B;",
        "uintptr_t ptr = (uintptr_t)calloc(allocSize, 1);",
        "id e;",
        "((void) (JreEnum(Test, A) = e = "
          + "objc_constructInstance(self, (void *)ptr)), ptr += objSize);",
        "Test_initWithNSString_withInt_(e, @\"A\", 0);",
        "((void) (JreEnum(Test, B) = e = objc_constructInstance([Test_1 class],"
          + " (void *)ptr)), ptr += objSize_B);",
        "Test_1_initWithNSString_withInt_(e, @\"B\", 1);",
        "((void) (JreEnum(Test, C) = e = "
          + "objc_constructInstance(self, (void *)ptr)), ptr += objSize);",
        "Test_initWithNSString_withInt_(e, @\"C\", 2);");
  }

  public void testValueOfMethod() throws IOException {
    String translation = translateSourceFile("enum Test { A, B, C }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "Test *Test_valueOfWithNSString_(NSString *name) {",
        "Test_initialize();",
        "for (int i = 0; i < 3; i++) {",
        "Test *e = Test_values_[i];",
        "if ([name isEqual:[e name]]) {",
        "return e;",
        "}",
        "}",
        "@throw create_JavaLangIllegalArgumentException_initWithNSString_(name);");
  }

  public void testStrippedValueOfMethod() throws IOException {
    options.setStripEnumConstants(true);
    String translation = translateSourceFile("enum Test { A, B, C }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "Test *Test_valueOfWithNSString_(NSString *name) {",
        "Test_initialize();",
        "@throw create_JavaLangError_initWithNSString_(@\"Enum.valueOf(String) "
        + "called on Test enum with stripped constant names\");");
  }
}
