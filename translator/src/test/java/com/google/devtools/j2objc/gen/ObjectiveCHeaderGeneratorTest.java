/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
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

import static com.google.common.truth.Truth.assertThat;

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.util.HeaderMap;
import java.io.File;
import java.io.IOException;

/**
 * Tests for {@link ObjectiveCHeaderGenerator}.
 *
 * @author Tom Ball
 */
public class ObjectiveCHeaderGeneratorTest extends GenerationTest {

  public void testInnerEnumWithPackage() throws IOException {
    String translation = translateSourceFile(
        "package mypackage;"
        + "public class Example { MyClass myclass = new MyClass(); }"
        + "enum Abcd { A, B, C; }"
        + "class MyClass {}", "Example", "mypackage/Example.h");
    assertInTranslation(translation, "@interface MypackageExample");
    // enum declaration
    assertInTranslation(translation, "typedef NS_ENUM(int32_t, MypackageAbcd_Enum) {");
    assertInTranslation(translation, "@interface MypackageAbcd");
    assertInTranslation(translation, "@interface MypackageMyClass");
    assertInTranslation(translation, "MypackageMyClass *myclass_;");
  }

  public void testTypeNameTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class Example {}", "Example", "Example.h");
    assertInTranslation(translation, "@interface Example ");
  }

  public void testUnicodeHeaderGuardTranslation() throws IOException {
    options.setSegmentedHeaders(false);
    // Non-letters should be replaced
    String translation = translateSourceFile(
        "public class ¢ents {}", "¢ents", "¢ents.h");
    assertInTranslation(translation, "#ifndef _ents_H");

    // Unicode letters outside the Basic Latin range should not be replaced
    translation = translateSourceFile(
        "public class こんにちは {}", "こんにちは", "こんにちは.h");
    assertInTranslation(translation, "#ifndef こんにちは_H");

    // Egyptian heiroglyph letters outside UCS-2, requiring two UTF-16 chars
    translation = translateSourceFile(
        "public class egyptian\uD80C\uDC00 {}", "egyptian\uD80C\uDC00", "egyptian\uD80C\uDC00.h");
    assertInTranslation(translation, "#ifndef Egyptian\uD80C\uDC00_H");
  }

  public void testDeprecatedTypeNameTranslation() throws IOException {
    options.enableDeprecatedDeclarations();
    String translation = translateSourceFile(
        "public @Deprecated class Example {}", "Example", "Example.h");
    assertInTranslation(translation, "__attribute__((deprecated))\n@interface Example ");
  }

  public void testDeprecatedTypeNameTranslationIsTurnedOff() throws IOException {
    String translation = translateSourceFile(
        "public @Deprecated class Example {}", "Example", "Example.h");
    assertFalse(translation.contains("__attribute__((deprecated))"));
  }

  public void testFullyQualifiedDeprecatedTypeNameTranslation() throws IOException {
    options.enableDeprecatedDeclarations();
    String translation = translateSourceFile(
        "public @java.lang.Deprecated class Example {}", "Example", "Example.h");
    assertInTranslation(translation, "__attribute__((deprecated))\n@interface Example ");
  }

  public void testPackageTypeNameTranslation() throws IOException {
    String translation = translateSourceFile(
        "package unit.test; public class Example {}", "Example", "unit/test/Example.h");
    assertInTranslation(translation, "@interface UnitTestExample ");
  }

  public void testPackageTypeNameTranslationWithInnerClass() throws IOException {
    String translation = translateSourceFile(
        "package unit.test; public class Example { class Inner {}}",
        "Example", "unit/test/Example.h");
    assertInTranslation(translation, "@interface UnitTestExample ");
    assertInTranslation(translation, "Example_Inner");
    assertInTranslation(translation, "@interface UnitTestExample_Inner ");
  }

  public void testSuperclassTypeTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class MyException extends Exception {}", "MyException", "MyException.h");
    assertInTranslation(translation, "@interface MyException : JavaLangException");
  }

  public void testImplementsTypeTranslation() throws IOException {
    String translation = translateSourceFile(
        "import java.io.Serializable; public class Example implements Serializable {}",
        "Example", "Example.h");
    assertInTranslation(translation, "@interface Example : NSObject < JavaIoSerializable >");
  }

  public void testImportTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class MyException extends Exception { MyException(RuntimeException t) {super(t);}}",
        "MyException", "MyException.h");
    assertInTranslation(translation, "@class JavaLangRuntimeException;");
    assertInTranslation(translation, "#include \"java/lang/Exception.h\"");
  }

  public void testHeaderFileMapping() throws IOException {
    options.getHeaderMap().setMappingFiles("testMappings.j2objc");
    addSourceFile("package unit.mapping.custom; public class Test { }",
        "unit/mapping/custom/Test.java");
    loadHeaderMappings();
    String translation = translateSourceFile(
        "import unit.mapping.custom.Test; "
            + "public class MyTest extends Test { MyTest() {}}",
        "MyTest", "MyTest.h");
    assertInTranslation(translation, "#include \"my/mapping/custom/Test.h\"");
  }

  public void testHeaderDefaultFileMapping() throws IOException {
    addSourceFile("package unit.mapping; public class Test { }", "unit/mapping/Test.java");
    loadHeaderMappings();
    String translation = translateSourceFile(
        "import unit.mapping.Test; "
            + "public class MyTest extends Test { MyTest() {}}",
        "MyTest", "MyTest.h");
    assertInTranslation(translation, "#include \"my/mapping/Test.h\"");
  }

  public void testNoHeaderMapping() throws IOException {
    // Should be able to turn off header mappings by passing empty list.
    options.getHeaderMap().setMappingFiles("");
    addSourceFile("package unit.mapping; public class Test { }", "unit/mapping/Test.java");
    loadHeaderMappings();
    String translation = translateSourceFile(
        "import unit.mapping.Test; "
            + "public class MyTest extends Test { MyTest() {}}",
        "MyTest", "MyTest.h");
    assertInTranslation(translation, "#include \"unit/mapping/Test.h\"");
  }

  public void testOutputHeaderFileMapping() throws IOException {
    options.getHeaderMap().setMappingFiles("testMappings.j2objc");
    options.getHeaderMap().setOutputStyle(HeaderMap.OutputStyleOption.SOURCE);
    addSourceFile("package unit.test; public class Dummy {}", "unit/test/Dummy.java");
    addSourceFile(
        "package unit.test;"
        + "public class AnotherDummy extends Dummy { "
        + "    public AnotherDummy() {}"
        + "}", "unit/test/AnotherDummy.java");

    preprocessFiles("unit/test/Dummy.java", "unit/test/AnotherDummy.java");
    loadHeaderMappings();

    String translation = translateSourceFile(getTranslatedFile("unit/test/AnotherDummy.java"),
        "AnotherDummy", "AnotherDummy.h");
    assertInTranslation(translation, "#include \"unit/test/Dummy.h\"");

    HeaderMap headerMap = options.getHeaderMap();
    assertEquals("unit/test/Dummy.h", headerMap.getMapped("unit.test.Dummy"));
    assertEquals("unit/test/AnotherDummy.h", headerMap.getMapped("unit.test.AnotherDummy"));
    assertEquals("my/mapping/custom/Test.h", headerMap.getMapped("unit.mapping.custom.Test"));
    assertEquals("my/mapping/custom/Test.h",
                 headerMap.getMapped("unit.mapping.custom.AnotherTest"));
  }

  public void testOutputHeaderFileMappingWithMultipleClassesInOneHeader() throws IOException {
    options.getHeaderMap().setMappingFiles("testMappings.j2objc");
    options.getHeaderMap().setOutputStyle(HeaderMap.OutputStyleOption.SOURCE);
    addSourceFile("package unit.mapping.custom; public class Test { }",
        "unit/mapping/custom/Test.java");
    addSourceFile("package unit.mapping.custom; public class AnotherTest { }",
        "unit/mapping/custom/AnotherTest.java");
    addSourceFile(
        "package unit.test;"
            + "import unit.mapping.custom.Test;"
            + "public class Dummy extends Test { "
            + "    public Dummy() {}"
            + "}", "unit/test/Dummy.java");
    addSourceFile(
        "package unit.test;"
            + "import unit.mapping.custom.AnotherTest;"
            + "public class AnotherDummy extends AnotherTest { "
            + "    public AnotherDummy() {}"
            + "}", "unit/test/AnotherDummy.java");

    preprocessFiles("unit/test/Dummy.java", "unit/test/AnotherDummy.java");
    loadHeaderMappings();

    String translationForDummy = translateSourceFile(getTranslatedFile("unit/test/Dummy.java"),
        "Dummy", "Dummy.h");
    String translationForAnotherDummy = translateSourceFile(
        getTranslatedFile("unit/test/AnotherDummy.java"), "AnotherDummy", "AnotherDummy.h");
    assertInTranslation(translationForDummy, "#include \"my/mapping/custom/Test.h\"");
    assertInTranslation(translationForAnotherDummy, "#include \"my/mapping/custom/Test.h\"");

    HeaderMap headerMap = options.getHeaderMap();
    assertEquals("unit/test/Dummy.h", headerMap.getMapped("unit.test.Dummy"));
    assertEquals("unit/test/AnotherDummy.h", headerMap.getMapped("unit.test.AnotherDummy"));
    assertEquals("my/mapping/custom/Test.h", headerMap.getMapped("unit.mapping.custom.Test"));
    assertEquals("my/mapping/custom/Test.h",
                 headerMap.getMapped("unit.mapping.custom.AnotherTest"));
  }

  public void testCombinedGeneration() throws IOException {
    options.setSegmentedHeaders(false);
    addSourceFile("package unit; public class Test {"
            + "    public void Dummy() {}"
            + "}",
        "unit/Test.java");
    addSourceFile("package unit; public class AnotherTest extends Test {"
            + "    public void AnotherDummy() {}"
            + "}",
        "unit/AnotherTest.java");

    String header = translateCombinedFiles(
        "unit/Foo", ".h", "unit/Test.java", "unit/AnotherTest.java");
    assertInTranslation(header, "#ifndef UnitFoo_H");
    assertInTranslation(header, "#define UnitFoo_H");
    assertInTranslation(header, "@interface UnitTest");
    assertInTranslation(header, "- (instancetype)init;");
    assertInTranslation(header, "- (void)Dummy;");
    assertInTranslation(header, "J2OBJC_EMPTY_STATIC_INIT(UnitTest)");
    assertInTranslation(header, "J2OBJC_TYPE_LITERAL_HEADER(UnitTest)");
    assertInTranslation(header, "@interface UnitAnotherTest : UnitTest");
    assertInTranslation(header, "- (void)AnotherDummy;");
    assertInTranslation(header, "J2OBJC_EMPTY_STATIC_INIT(UnitAnotherTest)");
    assertInTranslation(header, "J2OBJC_TYPE_LITERAL_HEADER(UnitAnotherTest)");
    assertNotInTranslation(header, "@class UnitTest");
    assertNotInTranslation(header, "@class UnitAnotherTest");
  }

  public void testCombinedGenerationOrdering() throws IOException {
    addSourceFile("package unit; public class Test {"
            + "    public void Dummy() {}"
            + "}",
        "unit/Test.java");
    // Test that necessary forward declarations aren't eliminated.
    addSourceFile("package unit; public class TestDependent {"
            + "    public Test Dummy() {"
            + "        return null;"
            + "    }"
            + "    public AnotherTest AnotherDummy() {"
            + "        return null;"
            + "    }"
            + "}",
        "unit/TestDependent.java");
    addSourceFile("package unit; public class AnotherTest extends Test {"
            + "    public void AnotherDummy() {}"
            + "}",
        "unit/AnotherTest.java");

    String header = translateCombinedFiles(
        "unit/Foo", ".h",
        "unit/TestDependent.java", "unit/AnotherTest.java", "unit/Test.java");
    assertThat(header.indexOf("@interface UnitTest")
        < header.indexOf("@interface UnitAnotherTest")).isTrue();
  }

  public void testCombinedJarHeaderMapping() throws IOException {
    File outputHeaderMappingFile = new File(tempDir, "mappings.j2objc");
    options.getHeaderMap().setOutputMappingFile(outputHeaderMappingFile);
    options.getHeaderMap().setOutputStyle(HeaderMap.OutputStyleOption.SOURCE);
    addSourceFile("package unit; public class Test { }",
        "unit/Test.java");
    addSourceFile("package unit; public class AnotherTest extends Test { }",
        "unit/AnotherTest.java");
    addSourceFile("package unit2;"
        + "import unit.Test;"
        + "public class AnotherTest extends Test { }",
        "unit2/AnotherTest.java");
    addSourceFile("package unit2;"
        + "import unit.AnotherTest;"
        + "public class YetAnotherTest extends AnotherTest { }",
        "unit2/YetAnotherTest.java");

    translateCombinedFiles("unit/Foo", ".h", "unit/Test.java", "unit/AnotherTest.java");
    String header2 = translateCombinedFiles(
        "unit2/Foo", ".h", "unit2/AnotherTest.java", "unit2/YetAnotherTest.java");

    HeaderMap headerMap = options.getHeaderMap();
    assertEquals("unit/Foo.h", headerMap.getMapped("unit.Test"));
    assertEquals("unit/Foo.h", headerMap.getMapped("unit.AnotherTest"));
    assertInTranslation(header2, "#include \"unit/Foo.h\"");
    assertEquals("unit2/Foo.h", headerMap.getMapped("unit2.AnotherTest"));
    assertEquals("unit2/Foo.h", headerMap.getMapped("unit2.YetAnotherTest"));
  }

  public void testForwardDeclarationTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class MyException extends Exception { MyException(RuntimeException t) {super(t);}}",
        "MyException", "MyException.h");
    assertInTranslation(translation, "@class JavaLangRuntimeException;");
  }

  public void testInstanceVariableTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class Example { Exception testException; }",
        "Example", "Example.h");
    assertInTranslation(translation, "JavaLangException *testException_;");
  }

  public void testInterfaceTranslation() throws IOException {
    String translation = translateSourceFile(
        "package unit.test; public interface Example {}",
        "Example", "unit/test/Example.h");
    assertInTranslation(translation, "@protocol UnitTestExample");
  }

  public void testDeprecatedInterfaceTranslation() throws IOException {
    options.enableDeprecatedDeclarations();
    String translation = translateSourceFile(
      "package unit.test; public @Deprecated interface Example {}",
      "Example", "unit/test/Example.h");
    assertInTranslation(translation, "__attribute__((deprecated))\n@protocol UnitTestExample");
  }

  public void testInterfaceWithMethodTranslation() throws IOException {
    String translation = translateSourceFile(
        "package unit.test; public interface Example { Example getExample(); }",
        "Example", "unit/test/Example.h");
    assertInTranslation(translation, "(id<UnitTestExample>)getExample;");
  }

  public void testInterfaceWithDeprecatedMethodTranslation() throws IOException {
    options.enableDeprecatedDeclarations();
    String translation = translateSourceFile(
        "package unit.test; public interface Example { @Deprecated Example getExample(); }",
        "Example", "unit/test/Example.h");
    assertInTranslation(
        translation, "- (id<UnitTestExample>)getExample __attribute__((deprecated));");
  }

  public void testSuperInterfaceTranslation() throws IOException {
    String translation = translateSourceFile(
        "package unit.test; public interface Example extends Bar {} interface Bar {}",
        "Example", "unit/test/Example.h");
    assertInTranslation(translation, "@protocol UnitTestExample < UnitTestBar, JavaObject >");
  }

  public void testNativeSuperInterfaceTranslation() throws IOException {
    // Allow overwriting the system java.lang.Iterable with our own.
    options.addPlatformModuleSystemOptions("--patch-module", "java.base=" + tempDir);
    // Allow java.base to see com.google.j2objc.
    options.addPlatformModuleSystemOptions("--add-reads", "java.base=ALL-UNNAMED");

    // Translate the file in the temp directory (i.e. avoid in-memory copy) because the temp
    // directory is already configured as a patch-module location.
    String filename = "java/lang/Iterable";
    String path = addSourceFile("package java.lang;"
            + "import com.google.j2objc.NSFastEnumeration;"
            + "public interface Iterable<T> extends NSFastEnumeration { String foo(); }",
        filename + ".java");
    String translation = translateSourceFileNoInMemory(path, filename + ".h");
    assertInTranslation(
        translation, "@protocol JavaLangIterable < NSFastEnumeration, JavaObject >");
  }

  public void testConstTranslation() throws IOException {
    String translation = translateSourceFile(
        "package unit.test; public class Example { public static final int FOO=1; }",
        "Example", "unit/test/Example.h");
    assertInTranslation(translation, "#define UnitTestExample_FOO 1");
    assertFalse(translation.contains("initialize"));
  }

  public void testStaticVariableTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class Example { public static java.util.Date today; }",
        "Example", "Example.h");
    assertTranslatedLines(translation,
        "inline JavaUtilDate *Example_get_today(void);",
        "inline JavaUtilDate *Example_set_today(JavaUtilDate *value);",
        "/*! INTERNAL ONLY - Use accessor function from above. */",
        "FOUNDATION_EXPORT JavaUtilDate *Example_today;",
        "J2OBJC_STATIC_FIELD_OBJ(Example, today, JavaUtilDate *)");
    assertFalse(translation.contains("initialize"));
    assertFalse(translation.contains("dealloc"));
  }

  public void testStaticVariableWithInitTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class Example { public static java.util.Date today = new java.util.Date(); }",
        "Example", "Example.h");
    assertTranslatedLines(translation,
        "inline JavaUtilDate *Example_get_today(void);",
        "inline JavaUtilDate *Example_set_today(JavaUtilDate *value);",
        "/*! INTERNAL ONLY - Use accessor function from above. */",
        "FOUNDATION_EXPORT JavaUtilDate *Example_today;",
        "J2OBJC_STATIC_FIELD_OBJ(Example, today, JavaUtilDate *)");
    assertFalse(translation.contains("initialize;"));
    assertFalse(translation.contains("dealloc"));
  }

  public void testInitMessageTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class Example { void init() {} void _init() {}}", "Example", "Example.h");
    assertInTranslation(translation, "- (void)init__ OBJC_METHOD_FAMILY_NONE;");
    assertInTranslation(translation, "- (void)_init OBJC_METHOD_FAMILY_NONE;");
  }

  public void testInitializeMessageTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class Example { void initialize() {} }", "Example", "Example.h");
    assertInTranslation(translation, "- (void)initialize__ OBJC_METHOD_FAMILY_NONE;");
  }

  public void testToStringRenaming() throws IOException {
    String translation = translateSourceFile(
      "public class Example { public String toString() { return super.toString(); } }",
      "Example", "Example.h");
    assertInTranslation(translation, "- (NSString *)description;");
  }

  public void testMultipleObjectDeclaration() throws IOException {
    String translation = translateSourceFile(
      "public class Example { String one, two, three; }",
      "Example", "Example.h");
    assertTranslatedLines(translation,
        "NSString *one_;", "NSString *two_;", "NSString *three_;");
  }

  public void testMultiplePrimitiveDeclaration() throws IOException {
    String translation = translateSourceFile(
      "public class Example { int one, two, three; }",
      "Example", "Example.h");
    assertTranslatedLines(translation,
        "int32_t one_;", "int32_t two_;", "int32_t three_;");
  }

  public void testMultipleInterfaceDeclaration() throws IOException {
    String translation = translateSourceFile(
      "public class Example { Comparable one, two, three; }",
      "Example", "Example.h");
    assertTranslatedLines(translation, "id<JavaLangComparable> one_;",
        "id<JavaLangComparable> two_;", "id<JavaLangComparable> three_;");
  }

  public void testMultipleClassDeclaration() throws IOException {
    String translation = translateSourceFile(
      "public class Example { Class<?> one, two, three; }",
      "Example", "Example.h");
    assertTranslatedLines(translation,
        "IOSClass *one_;", "IOSClass *two_;", "IOSClass *three_;");
  }

  public void testInnerClassDeclaration() throws IOException {
    String translation = translateSourceFile(
      "public class Example { class Inner {} }",
      "Example", "Example.h");
    assertInTranslation(translation, "@interface Example_Inner : NSObject");
    assertNotInTranslation(translation, "Example *this");
    assertInTranslation(translation, "- (instancetype)initWithExample:(Example *)outer$;");
  }

  public void testInnerClassDeclarationWithOuterReference() throws IOException {
    String translation = translateSourceFile(
      "public class Example { int i; class Inner { void test() { int j = i; } } }",
      "Example", "Example.h");
    assertInTranslation(translation, "@interface Example_Inner : NSObject");
    assertInTranslation(translation, "- (instancetype)initWithExample:(Example *)outer$;");
    translation = getTranslatedFile("Example.m");
    assertInTranslation(translation, "Example *this$0_;");
  }

  public void testEnum() throws IOException {
    String translation = translateSourceFile(
      "public enum Color { RED, WHITE, BLUE }",
      "Color", "Color.h");
    assertTranslatedLines(
        translation,
        "typedef NS_ENUM(int32_t, Color_Enum) {",
        "  Color_Enum_RED NS_SWIFT_NAME(red) = 0,",
        "  Color_Enum_WHITE NS_SWIFT_NAME(white) = 1,",
        "  Color_Enum_BLUE NS_SWIFT_NAME(blue) = 2,",
        "};");
    assertInTranslation(translation, "@interface Color : JavaLangEnum");
    assertInTranslation(translation, "+ (IOSObjectArray *)values;");
    assertInTranslation(translation, "@property(readonly) Color_Enum enumValue;");
    assertInTranslation(translation, "+ (Color *)valueOfWithNSString:(NSString *)name;");
    assertInTranslation(translation, "FOUNDATION_EXPORT Color *Color_values_[];");
    assertTranslatedLines(
        translation, "inline Color *Color_get_RED(void);", "J2OBJC_ENUM_CONSTANT(Color, RED)");
    assertTranslatedLines(
        translation, "inline Color *Color_get_WHITE(void);", "J2OBJC_ENUM_CONSTANT(Color, WHITE)");
    assertTranslatedLines(
        translation, "inline Color *Color_get_BLUE(void);", "J2OBJC_ENUM_CONSTANT(Color, BLUE)");
  }

  public void testEnumNoSwiftEnums() throws IOException {
    options.setSwiftEnums(false);
    String translation =
        translateSourceFile("public enum Color { RED, WHITE, BLUE }", "Color", "Color.h");
    assertTranslatedLines(
        translation,
        "typedef NS_ENUM(int32_t, Color_Enum) {",
        "  Color_Enum_RED = 0,",
        "  Color_Enum_WHITE = 1,",
        "  Color_Enum_BLUE = 2,",
        "};");
    assertInTranslation(translation, "@interface Color : JavaLangEnum");
    assertInTranslation(translation, "+ (IOSObjectArray *)values;");
    assertInTranslation(translation, "+ (Color *)valueOfWithNSString:(NSString *)name;");
    assertInTranslation(translation, "FOUNDATION_EXPORT Color *Color_values_[];");
    assertTranslatedLines(translation,
        "inline Color *Color_get_RED(void);",
        "J2OBJC_ENUM_CONSTANT(Color, RED)");
    assertTranslatedLines(translation,
        "inline Color *Color_get_WHITE(void);",
        "J2OBJC_ENUM_CONSTANT(Color, WHITE)");
    assertTranslatedLines(translation,
        "inline Color *Color_get_BLUE(void);",
        "J2OBJC_ENUM_CONSTANT(Color, BLUE)");
  }

  public void testEnumIsImplicitlyNonnullWhenNullMarked() throws IOException {
    options.setNullMarked(true);
    options.setNullability(true);
    String translation =
        translateSourceFile("public enum Color { RED, WHITE, BLUE }", "Color", "Color.h");
    assertTranslatedLines(
        translation, "NS_ASSUME_NONNULL_BEGIN", "@interface Color : JavaLangEnum");
    assertTranslatedLines(
        translation,
        "typedef NS_ENUM(int32_t, Color_Enum) {",
        "  Color_Enum_RED NS_SWIFT_NAME(red) = 0,",
        "  Color_Enum_WHITE NS_SWIFT_NAME(white) = 1,",
        "  Color_Enum_BLUE NS_SWIFT_NAME(blue) = 2,",
        "};");
    assertInTranslation(translation, "FOUNDATION_EXPORT Color *_Nonnull Color_values_[];");
    assertTranslatedLines(
        translation, "J2OBJC_TYPE_LITERAL_HEADER(Color)", "", "NS_ASSUME_NONNULL_END");
  }

  public void testEnumWithParameters() throws IOException {
    String translation = translateSourceFile(
        "public enum Color { RED(0xff0000), WHITE(0xffffff), BLUE(0x0000ff); "
        + "private int rgb; private Color(int rgb) { this.rgb = rgb; } "
        + "public int getRgb() { return rgb; }}",
        "Color", "Color.h");
    assertInTranslation(translation, "@interface Color : JavaLangEnum");
    translation = getTranslatedFile("Color.m");
    assertInTranslation(translation, "int32_t rgb_;");
    assertTranslatedLines(translation,
        "void Color_initWithInt_withNSString_withInt_("
        + "Color *self, int32_t rgb, NSString *__name, int32_t __ordinal) {");
  }

  public void testEnumWithMultipleConstructors() throws IOException {
    String translation = translateSourceFile(
      "public enum Color { RED(0xff0000), WHITE(0xffffff, false), BLUE(0x0000ff); "
      + "private int rgb; private boolean primary;"
      + "private Color(int rgb, boolean primary) { this.rgb = rgb; this.primary = primary; } "
      + "private Color(int rgb) { this(rgb, true); } "
      + "public int getRgb() { return rgb; }"
      + "public boolean isPrimaryColor() { return primary; }}",
      "Color", "Color.h");
    assertInTranslation(translation, "@interface Color : JavaLangEnum");
    translation = getTranslatedFile("Color.m");
    assertInTranslation(translation, "bool primary_;");
    assertInTranslation(
        translation,
        "Color_initWithInt_withBoolean_withNSString_withInt_("
            + "self, rgb, true, __name, __ordinal);");
    assertTranslatedLines(
        translation,
        "void Color_initWithInt_withBoolean_withNSString_withInt_("
            + "Color *self, int32_t rgb, bool primary, NSString *__name, int32_t __ordinal) {",
        "  JavaLangEnum_initWithNSString_withInt_(self, __name, __ordinal);",
        "  self->rgb_ = rgb;",
        "  self->primary_ = primary;",
        "}");
    assertTranslatedLines(translation,
        "void Color_initWithInt_withNSString_withInt_("
          + "Color *self, int32_t rgb, NSString *__name, int32_t __ordinal) {",
        "  Color_initWithInt_withBoolean_withNSString_withInt_("
          + "self, rgb, true, __name, __ordinal);",
        "}");
    assertInTranslation(
        translation,
        "Color_initWithInt_withBoolean_withNSString_withInt_("
            + "e, (int32_t) 0xffffff, false, @\"WHITE\", 1);");
    assertInTranslation(
        translation,
        "Color_initWithInt_withNSString_withInt_(e, (int32_t) 0x0000ff, @\"BLUE\", 2);");
  }

  public void testEnumInitialization() throws IOException {
    String sourceContent = "  package com.foo.bar;" + "" + "public enum Color { RED, WHITE, BLUE }";
    String translation = translateSourceFile(sourceContent, "Color", "com/foo/bar/Color.h");
    assertTranslatedLines(
        translation, "- (nullable instancetype)initWithColor:(ComFooBarColor_Enum)value;");
    translation = getTranslatedFile("com/foo/bar/Color.m");
    assertTranslatedLines(
        translation,
        "- (nullable instancetype)initWithColor:(ComFooBarColor_Enum)value {",
        "  return RETAIN_(ComFooBarColor_fromOrdinal((ComFooBarColor_ORDINAL)value));",
        "}");
  }

  public void testArrayFieldDeclaration() throws IOException {
    String translation = translateSourceFile(
      "public class Example { char[] before; char after[]; }",
      "Example", "Example.h");
    assertInTranslation(translation, "IOSCharArray *before_;");
    assertInTranslation(translation, "IOSCharArray *after_;");
  }

  public void testForwardDeclarationOfInnerType() throws IOException {
    String translation = translateSourceFile(
        "public class Example { Foo foo; class Foo {} }", "Example", "Example.h");
    // Test that Foo is forward declared because Example contains a field of
    // type Foo and Foo is declared after Example.
    assertInTranslation(translation, "@class Example_Foo;");
  }

  public void testAnnotationGeneration() throws IOException {
    String translation = translateSourceFile(
      "package foo; import java.lang.annotation.*; @Retention(RetentionPolicy.RUNTIME) "
      + "public @interface Compatible { boolean fooable() default false; }",
      "Compatible", "foo/Compatible.h");

    // Test that the annotation was declared as a protocol and a value class.
    assertInTranslation(translation, "@protocol FooCompatible < JavaLangAnnotationAnnotation >");
    assertInTranslation(translation, "@interface FooCompatible : NSObject < FooCompatible >");

    // Verify that the value is defined as a property instead of a method.
    assertInTranslation(translation, "@public\n  bool fooable_;");
    assertInTranslation(translation, "@property (readonly) bool fooable;");

    // Check that constructor was created with the property as parameter.
    assertInTranslation(
        translation, "FOUNDATION_EXPORT id<FooCompatible> create_FooCompatible(bool fooable);");

    translation = getTranslatedFile("foo/Compatible.m");
    // Verify default value accessor is generated for property.
    assertInTranslation(translation, "+ (bool)fooableDefault {");
  }

  public void testCharacterEdgeValues() throws IOException {
    String translation = translateSourceFile(
      "public class Test { "
      + "  public static final char MIN = 0; "
      + "  public static final char MAX = '\uffff'; "
      + "}", "Test", "Test.h");
    assertInTranslation(translation, "x00");
    assertInTranslation(translation, "0xffff");
  }

  public void testEnumNaming() throws IOException {
    String translation = translateSourceFile(
        "public enum MyEnum { ONE, TWO, THREE }",
        "MyEnum", "MyEnum.h");
    assertInTranslation(translation, "typedef NS_ENUM(int32_t, MyEnum_Enum) {");
    assertInTranslation(translation, "@interface MyEnum : JavaLangEnum");
    assertInTranslation(translation, "FOUNDATION_EXPORT MyEnum *MyEnum_values_[];");
    assertInTranslation(translation, "inline MyEnum *MyEnum_get_ONE(void);");
  }

  public void testNoImportForMappedTypes() throws IOException {
    String translation = translateSourceFile(
        "public class Test extends Object implements Cloneable { "
        + "  public String toString() { return \"\"; }"
        + "  public Class<?> myClass() { return getClass(); }}",
        "Test", "Test.h");
    assertFalse(translation.contains("#include \"java/lang/Class.h\""));
    assertFalse(translation.contains("#include \"java/lang/Cloneable.h\""));
    assertFalse(translation.contains("#include \"java/lang/Object.h\""));
    assertFalse(translation.contains("#include \"java/lang/String.h\""));
    assertFalse(translation.contains("#include \"Class.h\""));
    assertFalse(translation.contains("#include \"NSCopying.h\""));
    assertFalse(translation.contains("#include \"NSObject.h\""));
    assertFalse(translation.contains("#include \"NSString.h\""));
    assertInTranslation(translation, "NSCopying");
  }

  // Verify that an empty Java enum doesn't define an empty C enum,
  // which is illegal.
  public void testEmptyEnum() throws IOException {
    String header = translateSourceFile("public class A { enum Foo {} }", "A", "A.h");
    String impl = getTranslatedFile("A.m");

    // Verify there's no C enum.
    assertFalse(header.contains("typedef enum {\n} A_Foo;"));

    // Verify there's still a Java enum type.
    assertInTranslation(header, "@interface A_Foo : JavaLangEnum");
    assertInTranslation(impl, "@implementation A_Foo");
  }

  public void testEnumWithInterfaces() throws IOException {
    String translation = translateSourceFile(
        "public class A { interface I {} "
        + "enum Foo implements I, Runnable, Cloneable { "
        + "A, B, C; public void run() {}}}", "A", "A.h");
    assertInTranslation(
        translation, "@interface A_Foo : JavaLangEnum < A_I, JavaLangRunnable, NSCopying >");
    assertInTranslation(translation, "#include \"java/lang/Runnable.h\"");
  }

  public void testExternalNativeMethod() throws IOException {
    String translation = translateSourceFile(
        "package foo; class Example { native void external(String s); "
        + "  void test(String str) { external(str); }}", "Example", "foo/Example.h");

    // Verify external() and test() are in main interface.
    assertInTranslation(translation, "- (void)externalWithNSString:(NSString *)s;");
    assertInTranslation(translation, "- (void)testWithNSString:(NSString *)str;");

    // Verify category method is invoked.
    translation = getTranslatedFile("foo/Example.m");
    assertInTranslation(translation, "@implementation FooExample\n");
    assertInTranslation(
        translation, "void FooExample_externalWithNSString_(FooExample *self, NSString *s);");
    assertTranslatedLines(translation,
        "- (void)externalWithNSString:(NSString *)s {",
        "  FooExample_externalWithNSString_(self, s);",
        "}");
    assertInTranslation(translation, "[self externalWithNSString:str];");
  }

  public void testPropertiesOfTypeWeakOuter() throws IOException {
    String sourceContent =
        "  import com.google.j2objc.annotations.Weak;"
        + "import com.google.j2objc.annotations.WeakOuter;"
        + "public class FooBar {"
        + "  @Weak private Internal fieldBar;"
        + "  private Internal fieldFoo;"
        + "  @WeakOuter"
        + "  private class Internal {"
        + "  }"
        + "}";
    String translation = translateSourceFile(sourceContent, "FooBar", "FooBar.m");
    assertTranslatedLines(translation,
        "WEAK_ FooBar_Internal *fieldBar_;",
        "FooBar_Internal *fieldFoo_;");
  }

  public void testPropertiesOfGetTypes() throws IOException {
    String sourceContent =
        "  import com.google.j2objc.annotations.Property;"
            + "public class FooBar {"
            + "  private String fieldFoo = \"test\";"
            + "  "
            + "  @Property"
            + "  public String getFooField() {"
            + "     return fieldFoo;"
            + "  }"
            + "}";
    String translation = translateSourceFile(sourceContent, "FooBar", "FooBar.h");
    assertTranslatedLines(
        translation, "@property (nonatomic, getter=getFooField, readonly) NSString * fooField;");
  }

  public void testPropertiesOfGetTypesWithSetters() throws IOException {
    String sourceContent =
        "  import com.google.j2objc.annotations.Property;"
            + "public class FooBar {"
            + "  private String fieldFoo = \"test\";"
            + "  "
            + "  @Property"
            + "  public String getFooField() {"
            + "     return fieldFoo;"
            + "  }"
            + "  "
            + "  public void setFooField(String fooField) {"
            + "     this.fieldFoo = fooField;"
            + "  }"
            + "}";
    String translation = translateSourceFile(sourceContent, "FooBar", "FooBar.h");
    assertTranslatedLines(
        translation,
        "@property (nonatomic, getter=getFooField, setter=setFooFieldWithNSString:) NSString *"
            + " fooField;");
  }

  public void testPropertyAnnotationSuppression() throws IOException {
    String sourceContent =
        "  import com.google.j2objc.annotations.Property;"
            + "@Property "
            + "public class FooBar {"
            + "  "
            + "  public String getFieldFoo() {"
            + "     return \"\";"
            + "  }"
            + "  "
            + "  public void setFieldFoo(String fooField) {"
            + ""
            + "  }"
            + " "
            + "  @Property.Suppress "
            + "  public String getBar() {"
            + "     return \"\";"
            + "  }"
            + "  "
            + "}";
    String translation = translateSourceFile(sourceContent, "FooBar", "FooBar.h");
    assertNotInTranslation(
        translation, "@property (nonatomic, getter=getBar, readonly) NSString * bar;");
  }

  public void testPropertiesStaticMethods() throws IOException {
    String sourceContent =
        "  import com.google.j2objc.annotations.Property;"
            + "@Property "
            + "public class FooBar {"
            + "  "
            + "  public static String getFieldFoo() {"
            + "     return \"\";"
            + "  }"
            + "}";
    String translation = translateSourceFile(sourceContent, "FooBar", "FooBar.h");
    assertTranslatedLines(
        translation,
        "@property (class, nonatomic, getter=getFieldFoo, readonly) NSString * fieldFoo;");
  }

  public void testPropertiesOfGetTypesDuplicateNames() throws IOException {
    String sourceContent =
        "  import com.google.j2objc.annotations.Property;"
            + "@Property "
            + "public class FooBar {"
            + "  @Property"
            + "  public String fieldFoo = \"test\";"
            + "  "
            + "  "
            + "  public String getFieldFoo() {"
            + "     return fieldFoo;"
            + "  }"
            + "  "
            + "  public void setFieldFoo(String fooField) {"
            + "     this.fieldFoo = fooField;"
            + "  }"
            + "}";
    String translation = translateSourceFile(sourceContent, "FooBar", "FooBar.h");
    assertTranslatedLines(
        translation,
        "@property (copy, nonatomic, getter=getFieldFoo, setter=setFieldFooWithNSString:) NSString"
            + " *fieldFoo;");
  }

  public void testPropertiesOfGetTypesWithSettersNullable() throws IOException {
    options.setNullMarked(true);
    options.setNullability(true);
    addSourceFile(
        "@NullMarked package foo.bar;" + "import org.jspecify.annotations.NullMarked;",
        "foo/bar/package-info.java");

    String sourceContent =
        "package foo.bar; "
            + "import com.google.j2objc.annotations.Property;"
            + "import javax.annotation.*;"
            + "public class FooBar {"
            + "  private String fieldFoo = \"test\";"
            + "  "
            + "  @Property"
            + "  @Nullable"
            + "  public String getFooField() {"
            + "     return fieldFoo;"
            + "  }"
            + "  "
            + "  public void setFooField(@Nullable String fooField) {"
            + "    this.fieldFoo = fooField;"
            + "  }"
            + "}";
    String translation = translateSourceFile(sourceContent, "foo.bar.FooBar", "foo/bar/FooBar.h");
    assertTranslatedLines(
        translation,
        "@property (nonatomic, getter=getFooField, setter=setFooFieldWithNSString:, nullable)"
            + " NSString * fooField;");
  }

  public void testPropertyAnnotationIgnoresVoidMethod() throws IOException {
    String sourceContent =
        "  import com.google.j2objc.annotations.Property;"
            + "@Property "
            + "public class FooBar {"
            + "  public void doFoo() {}"
            + "  "
            + "}";
    String translation = translateSourceFile(sourceContent, "FooBar", "FooBar.h");
    assertNotInTranslation(translation, "@property");
  }

  public void testPropertiesOfClassTypes() throws IOException {
    String sourceContent =
        "  import com.google.j2objc.annotations.Property;"
            + "@Property "
            + "public class FooBar {"
            + "  private String fieldFoo = \"test\";"
            + "  "
            + "  public String getFooField() {"
            + "     return fieldFoo;"
            + "  }"
            + "  "
            + "  public void setFooField(String fooField) {"
            + "     this.fieldFoo = fooField;"
            + "  }"
            + "}";
    String translation = translateSourceFile(sourceContent, "FooBar", "FooBar.h");
    assertTranslatedLines(
        translation,
        "@property (nonatomic, getter=getFooField, setter=setFooFieldWithNSString:)"
            + " NSString * fooField;");
  }

  public void testAddIgnoreDeprecationWarningsPragmaIfDeprecatedDeclarationsIsEnabled()
      throws IOException {
    options.enableDeprecatedDeclarations();

    String sourceContent = "class Test {}";
    String translation = translateSourceFile(sourceContent, "FooBar", "FooBar.h");

    assertInTranslation(translation, "#pragma clang diagnostic push");
    assertInTranslation(
        translation, "#pragma GCC diagnostic ignored \"-Wdeprecated-declarations\"");
    assertInTranslation(translation, "#pragma clang diagnostic pop");
  }

  public void testDoNotAddIgnoreDeprecationWarningsPragmaIfDeprecatedDeclarationsIsDisabled()
      throws IOException {
    String sourceContent = "class Test {}";
    String translation = translateSourceFile(sourceContent, "FooBar", "FooBar.h");

    assertNotInTranslation(translation, "#pragma clang diagnostic push");
    assertNotInTranslation(translation,
        "#pragma GCC diagnostic ignored \"-Wdeprecated-declarations\"");
    assertNotInTranslation(translation, "#pragma clang diagnostic pop");
  }

  public void testInnerAnnotationGeneration() throws IOException {
    String source = "import java.lang.annotation.*; public abstract class Test { "
        + "@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD) "
        + "public @interface Initialize {}}";
    String translation = translateSourceFile(source, "Test", "Test.h");
    assertInTranslation(translation, "@protocol Test_Initialize < JavaLangAnnotationAnnotation >");
    assertInTranslation(translation, "@interface Test_Initialize : NSObject < Test_Initialize >");
  }

  public void testFieldSetterGeneration() throws IOException {
    String translation = translateSourceFile(
        "import com.google.j2objc.annotations.Weak;"
        + "class Test { Object o; @Weak String s; static Integer i; }", "Test", "Test.h");
    assertInTranslation(translation, "J2OBJC_FIELD_SETTER(Test, o_, id)");
    // Make sure the @Weak and static fields don't generate setters.
    assertOccurrences(translation, "J2OBJC_FIELD_SETTER", 1);
  }

  public void testFieldSetterGenerationWithNullMarked() throws IOException {
    options.setNullMarked(true);
    options.setNullability(true);
    addSourceFile(
        "@NullMarked package foo.bar;" + "import org.jspecify.annotations.NullMarked;",
        "foo/bar/package-info.java");
    String translation =
        translateSourceFile(
            "package foo.bar;import javax.annotation.*;"
                + "class Test { @Nullable String a; String b; @Nullable Object c;}",
            "foo.bar.Test",
            "foo/bar/Test.h");
    assertInTranslation(translation, "J2OBJC_FIELD_SETTER(FooBarTest, a_, NSString *_Nullable)");
    assertInTranslation(translation, "J2OBJC_FIELD_SETTER(FooBarTest, b_, NSString *)");
    assertInTranslation(translation, "J2OBJC_FIELD_SETTER(FooBarTest, c_, id _Nullable)");
  }

  public void testStaticFieldObject() throws IOException {
    String translation =
        translateSourceFile(
            "class Test { public static final String str = \"str\"; }", "Test", "Test.h");
    assertInTranslation(translation, "J2OBJC_STATIC_FIELD_OBJ_FINAL(Test, str, NSString *)");
  }

  public void testStaticFieldObjectWithNullMarked() throws IOException {
    options.setNullMarked(true);
    options.setNullability(true);
    addSourceFile(
        "@NullMarked package foo.bar;" + "import org.jspecify.annotations.NullMarked;",
        "foo/bar/package-info.java");
    String translation =
        translateSourceFile(
            "package foo.bar; import javax.annotation.*;"
                + "class Test { public static final String a = \"a\";"
                + "@Nullable public static final String b = null; }",
            "foo.bar.Test",
            "foo/bar/Test.h");
    assertInTranslation(translation, "inline NSString *FooBarTest_get_a(void);");
    assertInTranslation(translation, "FOUNDATION_EXPORT NSString *FooBarTest_a;");
    assertInTranslation(translation, "J2OBJC_STATIC_FIELD_OBJ_FINAL(FooBarTest, a, NSString *)");

    assertInTranslation(translation, "inline NSString *_Nullable FooBarTest_get_b(void);");
    assertInTranslation(translation, "FOUNDATION_EXPORT NSString *_Nullable FooBarTest_b;");
    assertInTranslation(
        translation, "J2OBJC_STATIC_FIELD_OBJ_FINAL(FooBarTest, b, NSString *_Nullable)");
  }

  public void testEnumWithNameAndOrdinalParameters() throws IOException {
    String translation = translateSourceFile(
      "public enum Test { FOO(\"foo\", 3), BAR(\"bar\", 5); "
      + "private String name; private int ordinal; "
      + "private Test(String name, int ordinal) { this.name = name; this.ordinal = ordinal; }"
      + "public String getName() { return name; }}",
      "Test", "Test.h");
    assertInTranslation(translation, "@interface Test : JavaLangEnum");
    translation = getTranslatedFile("Test.m");
    assertInTranslation(translation, "NSString *name_Test_;");
    assertInTranslation(translation, "int32_t ordinal_Test_;");
    assertInTranslation(
        translation,
        "void Test_initWithNSString_withInt_withNSString_withInt_(Test *self, NSString *name,"
            + " int32_t ordinal, NSString *__name, int32_t __ordinal) {");
  }

  public void testDeprecatedEnumType() throws IOException {
    options.enableDeprecatedDeclarations();
    String translation = translateSourceFile(
        "@Deprecated public enum Test { A, B }", "Test", "Test.h");
    assertInTranslation(translation, "__attribute__((deprecated))\n@interface Test");
  }

  public void testLongConstants() throws IOException {
    String translation = translateSourceFile(
        "class Test { static final long FOO = 123; }", "Test", "Test.h");
    assertInTranslation(translation, "123LL");
  }

  public void testCustomWeakAnnotations() throws IOException {
    String translation = translateSourceFile(
        "class Test { @interface Weak {} @interface WeakOuter {}"
        + " void foo() {}"
        + " @WeakOuter public class Inner { void bar() { foo(); } }"
        + " @Weak public Object obj; }", "Test", "Test.h");
    assertInTranslation(translation, "WEAK_ id obj_;");
    translation = getTranslatedFile("Test.m");
    assertInTranslation(translation, "WEAK_ Test *this$0_;");
  }

  public void testReservedWordAsAnnotationPropertyName() throws IOException {
    String translation = translateSourceFile(
        "package foo; import java.lang.annotation.*; @Retention(RetentionPolicy.RUNTIME) "
        + "public @interface Bar { String namespace() default \"\"; }",
        "Bar", "foo/Bar.h");
    assertInTranslation(translation, "@property (readonly) NSString *namespace__;");
    assertTranslatedLines(translation,
        "@interface FooBar : NSObject < FooBar > {", "@public", "NSString *namespace___;", "}");
    assertInTranslation(
        translation, "FOUNDATION_EXPORT id<FooBar> create_FooBar(NSString *namespace__);");
    translation = getTranslatedFile("foo/Bar.m");
    assertInTranslation(translation, "+ (NSString *)namespace__Default {");
  }

  public void testSwiftNamePackageAnnotation() throws IOException {
    addSourceFile(
        "@SwiftName "
            + "package com.foo.bar;"
            + ""
            + "import com.google.j2objc.annotations.SwiftName;",
        "com/foo/bar/package-info.java");

    String sourceContent =
        "  package com.foo.bar;"
            + ""
            + "public abstract class FooBar {"
            + ""
            + "  public void setFooField(String fooField) {"
            + " "
            + "  }"
            + "}";
    String translation = translateSourceFile(sourceContent, "FooBar", "com/foo/bar/FooBar.h");
    assertInTranslation(translation, "NS_SWIFT_NAME(FooBar)");
    assertInTranslation(translation, "NS_SWIFT_NAME(set(fooField:))");
  }

  public void testSwiftNameClassAnnotation() throws IOException {
    String sourceContent =
        "  package com.foo.bar;"
            + "import com.google.j2objc.annotations.SwiftName;"
            + ""
            + "@SwiftName "
            + "public abstract class FooBar {"
            + "  public void setFooField(String fooField) {}"
            + "}";
    String translation = translateSourceFile(sourceContent, "FooBar", "com/foo/bar/FooBar.h");
    assertInTranslation(translation, "NS_SWIFT_NAME(FooBar)");
  }

  public void testSwiftNameClassAnnotationOverride() throws IOException {
    String sourceContent =
        "  package com.foo.bar;"
            + "import com.google.j2objc.annotations.SwiftName;"
            + ""
            + "@SwiftName(\"MyFooBar\") "
            + "public abstract class FooBar {"
            + "  public void setFooField(String fooField) {}"
            + "  public static void builderWithExpectedSize(int expectedSize){}"
            + "}";
    String translation = translateSourceFile(sourceContent, "FooBar", "com/foo/bar/FooBar.h");
    assertInTranslation(translation, "NS_SWIFT_NAME(MyFooBar)");
  }

  public void testSwiftNameMethodAnnotationOverride() throws IOException {
    String sourceContent =
        "  package com.foo.bar;"
            + "import com.google.j2objc.annotations.SwiftName;"
            + ""
            + "public abstract class FooBar {"
            + "  @SwiftName(\"set(name:)\") "
            + "  public void setFooField(String fooField) {}"
            + "  @SwiftName(\"builder(size:)\") "
            + "  public static void builderWithExpectedSize(int expectedSize){}"
            + "}";
    String translation = translateSourceFile(sourceContent, "FooBar", "com/foo/bar/FooBar.h");
    assertInTranslation(translation, "NS_SWIFT_NAME(set(name:))");
    assertInTranslation(translation, "NS_SWIFT_NAME(builder(size:))");
  }

  public void testSwiftNameMethodAnnotationWithObjCName() throws IOException {
    String sourceContent =
        "  package com.foo.bar;"
            + "import com.google.j2objc.annotations.SwiftName;"
            + "import com.google.j2objc.annotations.ObjectiveCName;"
            + ""
            + "public abstract class FooBar {"
            + "  @SwiftName(\"swiftSet(name:)\") "
            + "  @ObjectiveCName(\"objcSetFooField:\") "
            + "  public void setFooField(String fooField) {}"
            + ""
            + "  @ObjectiveCName(\"objcBuilderWithSize:\") "
            + "  @SwiftName(\"swiftBuilder(size:)\") "
            + "  public static void builderWithExpectedSize(int expectedSize){}"
            + ""
            + "  @SwiftName(\"swiftDoAction()\") "
            + "  @ObjectiveCName(\"objcDoAction\") "
            + "  public void doAction(){}"
            + "}";
    String translation = translateSourceFile(sourceContent, "FooBar", "com/foo/bar/FooBar.h");
    assertInTranslation(
        translation,
        "- (void)objcSetFooField:(NSString *)fooField NS_SWIFT_NAME(swiftSet(name:));");
    assertInTranslation(
        translation,
        "+ (void)objcBuilderWithSize:(int32_t)expectedSize NS_SWIFT_NAME(swiftBuilder(size:));");
    assertInTranslation(translation, "- (void)objcDoAction NS_SWIFT_NAME(swiftDoAction());");
  }

  public void testSwiftNamePackageAnnotationWithObjCNameMethods() throws IOException {
    addSourceFile(
        "@SwiftName "
            + "package com.foo.bar;"
            + ""
            + "import com.google.j2objc.annotations.SwiftName;",
        "com/foo/bar/package-info.java");

    String sourceContent =
        "  package com.foo.bar;"
            + "import com.google.j2objc.annotations.SwiftName;"
            + "import com.google.j2objc.annotations.ObjectiveCName;"
            + ""
            + "public abstract class FooBar {"
            + "  @ObjectiveCName(\"objcSetFooField:\") "
            + "  public void setFooField(String fooField) {}"
            + ""
            + "  @ObjectiveCName(\"objcBuilderWithSize:\") "
            + "  public static void builderWithExpectedSize(int expectedSize){}"
            + ""
            + "  @ObjectiveCName(\"objcDoAction\") "
            + "  public void doAction(){}"
            + "}";
    String translation = translateSourceFile(sourceContent, "FooBar", "com/foo/bar/FooBar.h");
    assertInTranslation(
        translation, "- (void)objcSetFooField:(NSString *)fooField NS_SWIFT_NAME(set(fooField:));");
    assertInTranslation(
        translation,
        "+ (void)objcBuilderWithSize:(int32_t)expectedSize NS_SWIFT_NAME(builder(expectedSize:));");
    assertInTranslation(translation, "- (void)objcDoAction;");
  }

  public void testSwiftNameAnnotationWithNestedTypes() throws IOException {
    addSourceFile(
        "@SwiftName "
            + "package com.foo.bar;"
            + ""
            + "import com.google.j2objc.annotations.SwiftName;",
        "com/foo/bar/package-info.java");

    String sourceContent =
        "  package com.foo.bar;"
            + ""
            + "public abstract class FooBar {"
            + ""
            + "  public static final class NestedBar {}"
            + "  public void setFooField(String fooField) {"
            + " "
            + "  }"
            + "   public static void builderWithExpectedSize(int expectedSize){}"
            + "}";
    String translation = translateSourceFile(sourceContent, "FooBar", "com/foo/bar/FooBar.h");
    assertTranslatedLines(translation, "NS_SWIFT_NAME(FooBar.NestedBar)");
  }

  public void testSwiftNamePackageAnnotationEnumSubtype() throws IOException {
    addSourceFile(
        "@SwiftName "
            + "package com.foo.bar;"
            + ""
            + "import com.google.j2objc.annotations.SwiftName;",
        "com/foo/bar/package-info.java");

    String sourceContent =
        "  package com.foo.bar;"
            + ""
            + "public abstract class FooBar {"
            + "  public enum MyEnum { ONE, TWO, THREE } "
            + "  public void setFooField(String fooField) {"
            + " "
            + "  }"
            + "}";
    String translation = translateSourceFile(sourceContent, "FooBar", "com/foo/bar/FooBar.h");
    assertInTranslation(translation, "NS_SWIFT_NAME(FooBar.MyEnumClass)");
    assertInTranslation(translation, "NS_SWIFT_NAME(FooBar.MyEnum)");
  }

  public void testSwiftNameInheritedInterface() throws IOException {
    options.setStripReflection(true);
    options.setEmitWrapperMethods(false);
    options.setAsObjCGenericDecl(true);
    addSourceFile(
        "@SwiftName " + "package foo;" + "" + "import com.google.j2objc.annotations.SwiftName;",
        "foo/package-info.java");

    addSourceFile(
        "@SwiftName " + "package bar;" + "" + "import com.google.j2objc.annotations.SwiftName;",
        "bar/package-info.java");

    addSourceFile(
        "  package foo;"
            + ""
            + "public interface FooBar<E> {"
            + "  public E getElementByIndex(int index); "
            + "}",
        "foo/FooBar.java");

    addSourceFile(
        "  package bar;"
            + "import foo.FooBar; "
            + ""
            + "public interface BarFoo extends FooBar<String> {"
            + " "
            + "}",
        "bar/BarFoo.java");

    String fooHeader = translateCombinedFiles("com/Foo", ".h", "foo/FooBar.java");
    String barHeader = translateCombinedFiles("com/Bar", ".h", "bar/BarFoo.java");
    assertTranslatedLines(
        barHeader,
        "- (NSString *)getElementByIndexWithInt:(int32_t)index"
            + " NS_SWIFT_NAME(getElementByInt(index:));");
    assertTranslatedLines(
        fooHeader,
        "- (id)getElementByIndexWithInt:(int32_t)index NS_SWIFT_NAME(getElementBy(index:));");
  }

  public void testSwiftNameAbnormalArgumentNames() throws IOException {
    addSourceFile(
        "@SwiftName " + "package foo;" + "" + "import com.google.j2objc.annotations.SwiftName;",
        "foo/package-info.java");

    addSourceFile(
        "  package foo;"
            + ""
            + "public interface FooBar {"
            + "  public String fooBar(String fooBar);"
            + "  public String setSomeValue(String value);"
            + "  public String setSomeArgument(String arg0);"
            + "}",
        "foo/FooBar.java");

    String fooHeader = translateCombinedFiles("com/Foo", ".h", "foo/FooBar.java");
    assertTranslatedLines(
        fooHeader,
        "- (NSString *)fooBarWithNSString:(NSString *)fooBar NS_SWIFT_NAME(fooBar(_:));");
    assertTranslatedLines(
        fooHeader,
        "- (NSString *)setSomeValueWithNSString:(NSString *)value NS_SWIFT_NAME(setSome(_:));");
    assertTranslatedLines(
        fooHeader,
        "- (NSString *)setSomeArgumentWithNSString:(NSString *)arg0"
            + " NS_SWIFT_NAME(setSomeArgument(_:));");
  }

  public void testSwiftNameAnnotationWithStaticFunctions() throws IOException {
    options.setEmitWrapperMethods(false);

    addSourceFile(
        "@SwiftName "
            + "package com.foo.bar;"
            + ""
            + "import com.google.j2objc.annotations.SwiftName;",
        "com/foo/bar/package-info.java");

    String sourceContent =
        "  package com.foo.bar;"
            + ""
            + "public class FooBar {"
            + "  FooBar() {}"
            + "  public static final class NestedBar {}"
            + "  public void setFooField(String fooField) {"
            + " "
            + "  }"
            + "  public static String builderWithExpectedSize(int expectedSize){ return \"\"; }"
            + "  public static String builderWithName(String name){ return \"\"; }"
            + "}";
    String translation = translateSourceFile(sourceContent, "FooBar", "com/foo/bar/FooBar.h");
    assertInTranslation(translation, "NS_SWIFT_NAME(FooBar.init())");
    assertInTranslation(translation, "NS_SWIFT_NAME(FooBar.builder(expectedSize:))");
    assertInTranslation(translation, "NS_SWIFT_NAME(FooBar.builder(name:))");
  }

  public void testSwiftNameAnnotationWithStaticFunctionsWithWapperMethods() throws IOException {
    options.setEmitWrapperMethods(true);

    addSourceFile(
        "@SwiftName "
            + "package com.foo.bar;"
            + ""
            + "import com.google.j2objc.annotations.SwiftName;",
        "com/foo/bar/package-info.java");

    String sourceContent =
        "  package com.foo.bar;"
            + ""
            + "public abstract class FooBar {"
            + "  FooBar() {}"
            + "  public static final class NestedBar {}"
            + "  public void setFooField(String fooField) {"
            + " "
            + "  }"
            + "   public static void builderWithExpectedSize(int expectedSize){}"
            + "}";
    String translation = translateSourceFile(sourceContent, "FooBar", "com/foo/bar/FooBar.h");
    assertInTranslation(translation, "NS_SWIFT_NAME(builder(expectedSize:))");
  }

  public void testSwiftNameAnnotationOverrideConstructor() throws IOException {
    String sourceContent =
        "  package com.foo.bar;"
            + "import com.google.j2objc.annotations.SwiftName;"
            + ""
            + "public abstract class FooBar {"
            + "  @SwiftName(\"init(size:)\") "
            + "  FooBar(int expectedSize) {}"
            + "  public void setFooField(String fooField) {}"
            + "  public static void builderWithExpectedSize(int expectedSize){}"
            + "}";
    String translation = translateSourceFile(sourceContent, "FooBar", "com/foo/bar/FooBar.h");
    assertInTranslation(translation, "NS_SWIFT_NAME(init(size:))");
  }

  public void testSwiftNameAnnotationOverrideConstructorNoWrapperMethods() throws IOException {
    options.setEmitWrapperMethods(false);
    String sourceContent =
        "  package com.foo.bar;"
            + "import com.google.j2objc.annotations.SwiftName;"
            + ""
            + "public class FooBar {"
            + "  @SwiftName(\"init(size:)\") "
            + "  public FooBar(int expectedSize) {}"
            + "  public void setFooField(String fooField) {}"
            + "  public static void builderWithExpectedSize(int expectedSize){}"
            + "}";
    String translation = translateSourceFile(sourceContent, "FooBar", "com/foo/bar/FooBar.h");
    assertInTranslation(translation, "NS_SWIFT_NAME(ComFooBarFooBar.init(expectedSize:))");
  }

  public void testSwiftNameEnumAnnotation() throws IOException {
    options.setSwiftNaming(true);

    String sourceContent =
        "  package com.foo.bar;"
            + "import com.google.j2objc.annotations.SwiftName;"
            + ""
            + "@SwiftName "
            + "public enum Color { RED, WHITE, BLUE }";
    String translation = translateSourceFile(sourceContent, "Color", "com/foo/bar/Color.h");
    assertInTranslation(translation, "NS_SWIFT_NAME(Color)");
    assertInTranslation(translation, "NS_SWIFT_NAME(ColorClass)");
  }

  public void testSwiftNameInterfaceSubtype() throws IOException {
    addSourceFile(
        "@SwiftName "
            + "package com.foo.bar;"
            + ""
            + "import com.google.j2objc.annotations.SwiftName;",
        "com/foo/bar/package-info.java");

    String sourceContent =
        "  package com.foo.bar;"
            + ""
            + "public interface FooBar {"
            + "  public enum MyEnum { ONE, TWO, THREE } "
            + "  public void setFooField(String fooField);"
            + "  public final class NestedBar {}"
            + "}";
    String translation = translateSourceFile(sourceContent, "FooBar", "com/foo/bar/FooBar.h");
    assertInTranslation(translation, "NS_SWIFT_NAME(MyEnumClass)");
    assertInTranslation(translation, "NS_SWIFT_NAME(MyEnum)");
    assertInTranslation(translation, "NS_SWIFT_NAME(NestedBar)");
  }

  public void testMethodSorting() throws IOException {
    String translation = translateSourceFile("class A {"
        + "protected void gnu(String s, int i, Runnable r) {}"
        + "public A(int i) {}"
        + "private void zebra() {}"
        + "void yak() {}"
        + "A() {} }", "A", "A.h");
    assertTranslatedLines(translation,
        "#pragma mark Public",
        "",
        "- (instancetype)initWithInt:(int32_t)i;",
        "",
        "#pragma mark Protected",
        "",
        "- (void)gnuWithNSString:(NSString *)s",
                        "withInt:(int32_t)i",
           "withJavaLangRunnable:(id<JavaLangRunnable>)r;",
        "",
        "#pragma mark Package-Private",
        "",
        "- (instancetype)init;",
        "",
        "- (void)yak;");
    assertNotInTranslation(translation, "zebra");  // No zebra() since it's private.
  }

  // Verify that when a class is referenced in the same source file, a header
  // isn't included for it.
  public void testPackagePrivateBaseClass() throws IOException {
    String translation = translateSourceFile(
        "package bar; public class Test extends Foo {} "
        + "abstract class Foo {}", "Test", "bar/Test.h");
    assertNotInTranslation(translation, "#include \"Foo.h\"");
  }

  public void testNoForwardDeclarationWhenIncluded() throws IOException {
    options.setSegmentedHeaders(false);
    addSourceFile("class Foo { static class Bar { } }", "Foo.java");
    String translation = translateSourceFile(
        "class Test extends Foo { Foo.Bar bar; }", "Test", "Test.h");
    assertInTranslation(translation, "#include \"Foo.h\"");
    // Forward declaration for Foo_Bar is not needed because we've included Foo.h.
    assertNotInTranslation(translation, "@class Foo_Bar");
  }

  // Verify that the default constructor is disallowed if the class has a non-default
  // constructor.
  public void testDefaultConstructorDisallowed() throws IOException {
    options.setDisallowInheritedConstructors(true);
    String translation = translateSourceFile("class Test { Test(int i) {} }", "Test", "Test.h");
    assertInTranslation(translation, "- (instancetype)initWithInt:(int32_t)i;");
    assertInTranslation(translation, "- (instancetype)init NS_UNAVAILABLE;");
  }

  // Verify that inherited constructors are disallowed. Exception has four constructors,
  // so a subclass that only implements one should have the other three disallowed.
  public void testConstructorsDisallowed() throws IOException {
    options.setDisallowInheritedConstructors(true);
    String translation = translateSourceFile(
        "class Test extends Exception { Test(String s) { super(s); } }", "Test", "Test.h");
    assertInTranslation(translation, "- (instancetype)initWithNSString:(NSString *)s;");
    assertInTranslation(translation, "- (instancetype)init NS_UNAVAILABLE;");
    assertInTranslation(
        translation,
        "- (instancetype)initWithJavaLangThrowable:(JavaLangThrowable *)arg0 NS_UNAVAILABLE;");
    assertTranslatedLines(translation,
        "- (instancetype)initWithNSString:(NSString *)arg0",
        "withJavaLangThrowable:(JavaLangThrowable *)arg1 NS_UNAVAILABLE;");
    assertTranslatedLines(
        translation,
        "- (instancetype)initWithNSString:(NSString *)arg0",
        "withJavaLangThrowable:(JavaLangThrowable *)arg1",
        "withBoolean:(bool)arg2",
        "withBoolean:(bool)arg3 NS_UNAVAILABLE;");
  }

  public void testStaticInterfaceMethodDeclaredInCompanionClass() throws IOException {
    String source = "interface Foo { static void f() {} }"
        + "class Bar implements Foo { void g() { Foo.f(); } }";
    String header = translateSourceFile(source, "Test", "Test.h");
    String impl =  getTranslatedFile("Test.m");

    assertInTranslation(header, "@protocol Foo < JavaObject >");
    assertTranslatedSegments(header, "@interface Foo : NSObject", "+ (void)f;", "@end");
    // Should only have one occurrence from the companion class.
    assertOccurrences(header, "+ (void)f;", 1);

    // The companion class of Foo still has the class method +[Foo f].
    assertTranslatedLines(impl, "+ (void)f {", "Foo_f();", "}");
  }

  // Verifies that properly encoded Kythe metadata and associated pragmas are generated when
  // using the Kythe mapping flag.
  public void testKytheMetadataMappings() throws IOException {
    options.setEmitKytheMappings(true);
    String translation =
        translateSourceFile("class A {" + "public A(int i) {}" + "A() {} }", "A", "A.h");
    String kytheMetadata = extractKytheMetadata(translation);

    assertInTranslation(translation, "#ifdef KYTHE_IS_RUNNING");
    assertInTranslation(
        translation, "#pragma kythe_inline_metadata" + " \"This file contains Kythe metadata.\"");
    assertInTranslation(translation, "/* This file contains Kythe metadata.");

    assertInTranslation(kytheMetadata, "kythe0");
    assertInTranslation(kytheMetadata, "{\"type\":\"anchor_anchor\"");
  }

  // Verifies that properly encoded Kythe metadata and associated pragmas are generated when
  // using the Kythe mapping flag with a source jar.
  public void testKytheMetadataMappingsWithSourceJar() throws Exception {
    options.setEmitKytheMappings(true);
    addJarFile(
        "some/path/test.jar", "foo/Test.java", "package foo; class Test { public void test() {}}");
    runPipeline("some/path/test.jar");

    String translation = getTranslatedFile("foo/Test.h");
    assertInTranslation(translation, "#ifdef KYTHE_IS_RUNNING");
    assertInTranslation(
        translation, "#pragma kythe_inline_metadata" + " \"This file contains Kythe metadata.\"");
    assertInTranslation(translation, "/* This file contains Kythe metadata.");

    // Verify metadata paths don't contain jar URL paths ...
    String kytheMetadata = extractKytheMetadata(translation);
    assertNotInTranslation(kytheMetadata, "jar:file:some/path/test.jar");

    // ... just the source file entry in that jar file.
    assertInTranslation(kytheMetadata, "\"path\":\"foo/Test.java\"");
  }

  public void testSeparateHeaderGeneration() throws IOException {
    options.setSeparateHeaders(true);
    String source =
        String.join(
            "\n",
            "package foo;",
            "public class Test implements Runnable {",
            "  private int n;",
            "  public void run() {}",
            "  static class Bar implements AutoCloseable {",
            "    public void close() throws Exception {}",
            "  }",
            "  static enum Mumble { A, B, C; }",
            "  class Inner {",
            "    int answerToEverything() { return n; }",
            "  }",
            "}");
    String mainHeader = translateSourceFile(source, "Test", "foo/Test.h");
    assertInTranslation(mainHeader, "#import \"java/lang/Runnable.h\"");
    assertInTranslation(mainHeader, "- (instancetype)init;");
    assertNotInTranslation(mainHeader, "Bar");
    assertNotInTranslation(mainHeader, "Mumble");
    assertNotInTranslation(mainHeader, "Inner");

    String barHeader = getTranslatedFile("foo/Test_Bar.h");
    assertInTranslation(barHeader, "#import \"java/lang/AutoCloseable.h\"");
    assertInTranslation(barHeader, "- (void)close;");
    assertNotInTranslation(barHeader, "Mumble");
    assertNotInTranslation(barHeader, "Inner");

    String mumbleHeader = getTranslatedFile("foo/Test_Mumble.h");
    assertInTranslation(mumbleHeader, "#import \"java/lang/Enum.h\"");
    assertInTranslation(mumbleHeader, "J2OBJC_ENUM_CONSTANT(FooTest_Mumble, A)");
    assertNotInTranslation(mumbleHeader, "Bar");
    assertNotInTranslation(mumbleHeader, "Inner");

    String innerHeader = getTranslatedFile("foo/Test_Inner.h");
    assertInTranslation(innerHeader, "@class FooTest;");
    assertInTranslation(innerHeader, "- (int32_t)answerToEverything;");
    assertNotInTranslation(innerHeader, "Bar");
    assertNotInTranslation(innerHeader, "Mumble");

    String implFile = getTranslatedFile("foo/Test.m");
    assertInTranslation(implFile, "#import \"foo/Test_Bar.h\"");
    assertInTranslation(implFile, "#import \"foo/Test_Mumble.h\"");
    assertInTranslation(implFile, "#import \"foo/Test_Inner.h\"");
  }

  // Verify circular includes example at j2objc.org translates correctly.
  public void testCircularIncludes() throws IOException {
    options.setSeparateHeaders(true);
    addSourceFile("class Foo extends Bar {}", "Foo.java");
    addSourceFile("class Bar {\n"
            + "  static class Baz extends Foo {}\n"
            + "}", "Bar.java");
    runPipeline("Foo.java", "Bar.java");
    String translation = getTranslatedFile("Foo.h");
    assertInTranslation(translation, "#import \"Bar.h\"");
    assertInTranslation(translation, "@interface Foo : Bar");
    assertNotInTranslation(translation, "Bar_Baz.h");

    translation = getTranslatedFile("Bar.h");
    assertInTranslation(translation, "@interface Bar : NSObject");
    assertNotInTranslation(translation, "Foo");
    assertNotInTranslation(translation, "Bar_Baz");

    translation = getTranslatedFile("Bar_Baz.h");
    assertInTranslation(translation, "#import \"Foo.h\"");
    assertInTranslation(translation, "@interface Bar_Baz : Foo");

    translation = getTranslatedFile("Foo.m");
    assertInTranslation(translation, "#import \"Bar.h\"");
    assertInTranslation(translation, "#import \"Foo.h\"");

    translation = getTranslatedFile("Bar.m");
    assertInTranslation(translation, "#import \"Bar.h\"");
    assertInTranslation(translation, "#import \"Bar_Baz.h\"");
    assertInTranslation(translation, "#import \"Foo.h\"");
  }

  public void testSeparateHeaderGenerationOfAnnotationTypeWithInnerTypes() throws IOException {
    options.setSeparateHeaders(true);
    String source =
        String.join(
            "\n",
            "package foo;",
            "import java.lang.annotation.*;",
            "@Retention(RetentionPolicy.CLASS)",
            "public @interface ObjectiveCAdapterMethod {",
            "    public enum Adaptation {",
            "        EXCEPTIONS_AS_ERRORS, RETURN_NATIVE_BOOLS; }",
            "    Adaptation[] adaptations() default {};",
            "}");
    String mainHeader =
        translateSourceFile(source, "ObjectiveCAdapterMethod", "foo/ObjectiveCAdapterMethod.h");
    assertInTranslation(
        mainHeader, "@protocol FooObjectiveCAdapterMethod < JavaLangAnnotationAnnotation >");
    assertInTranslation(
        mainHeader,
        "@interface FooObjectiveCAdapterMethod : NSObject < FooObjectiveCAdapterMethod >");
    String enumHeader = getTranslatedFile("foo/ObjectiveCAdapterMethod_Adaptation.h");
    assertInTranslation(
        enumHeader, "@interface FooObjectiveCAdapterMethod_Adaptation : JavaLangEnum");
    String implFile = getTranslatedFile("foo/ObjectiveCAdapterMethod.m");
    assertInTranslation(implFile, "ObjectiveCAdapterMethod_Adaptation_Enum");
  }
}
