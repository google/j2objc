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

import com.google.common.collect.Lists;
import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.util.HeaderMap;
import com.google.devtools.j2objc.util.SourceVersion;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

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
    assertTranslation(translation, "@interface MypackageExample");
    // enum declaration
    assertTranslation(translation, "typedef NS_ENUM(NSUInteger, MypackageAbcd_Enum) {");
    assertTranslation(translation, "@interface MypackageAbcd");
    assertTranslation(translation, "@interface MypackageMyClass");
    assertTranslation(translation, "MypackageMyClass *myclass_;");
  }

  public void testTypeNameTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class Example {}", "Example", "Example.h");
    assertTranslation(translation, "@interface Example ");
  }

  public void testUnicodeHeaderGuardTranslation() throws IOException {
    Options.setSegmentedHeaders(false);
    // Non-letters should be replaced
    String translation = translateSourceFile(
        "public class ¢ents {}", "¢ents", "¢ents.h");
    assertTranslation(translation, "#ifndef _ents_H");

    // Unicode letters outside the Basic Latin range should not be replaced
    translation = translateSourceFile(
        "public class こんにちは {}", "こんにちは", "こんにちは.h");
    assertTranslation(translation, "#ifndef こんにちは_H");

    // Egyptian heiroglyph letters outside UCS-2, requiring two UTF-16 chars
    translation = translateSourceFile(
        "public class egyptian\uD80C\uDC00 {}", "egyptian\uD80C\uDC00", "egyptian\uD80C\uDC00.h");
    assertTranslation(translation, "#ifndef Egyptian\uD80C\uDC00_H");
  }

  public void testDeprecatedTypeNameTranslation() throws IOException {
    Options.enableDeprecatedDeclarations();
    String translation = translateSourceFile(
        "public @Deprecated class Example {}", "Example", "Example.h");
    assertTranslation(translation, "__attribute__((deprecated))\n@interface Example ");
  }

  public void testDeprecatedTypeNameTranslationIsTurnedOff() throws IOException {
    String translation = translateSourceFile(
        "public @Deprecated class Example {}", "Example", "Example.h");
    assertFalse(translation.contains("__attribute__((deprecated))"));
  }

  public void testFullyQualifiedDeprecatedTypeNameTranslation() throws IOException {
    Options.enableDeprecatedDeclarations();
    String translation = translateSourceFile(
        "public @java.lang.Deprecated class Example {}", "Example", "Example.h");
    assertTranslation(translation, "__attribute__((deprecated))\n@interface Example ");
  }

  public void testPackageTypeNameTranslation() throws IOException {
    String translation = translateSourceFile(
        "package unit.test; public class Example {}", "Example", "unit/test/Example.h");
    assertTranslation(translation, "@interface UnitTestExample ");
  }

  public void testPackageTypeNameTranslationWithInnerClass() throws IOException {
    String translation = translateSourceFile(
        "package unit.test; public class Example { class Inner {}}",
        "Example", "unit/test/Example.h");
    assertTranslation(translation, "@interface UnitTestExample ");
    assertTranslation(translation, "Example_Inner");
    assertTranslation(translation, "@interface UnitTestExample_Inner ");
  }

  public void testSuperclassTypeTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class MyException extends Exception {}", "MyException", "MyException.h");
    assertTranslation(translation, "@interface MyException : JavaLangException");
  }

  public void testImplementsTypeTranslation() throws IOException {
    String translation = translateSourceFile(
        "import java.io.Serializable; public class Example implements Serializable {}",
        "Example", "Example.h");
    assertTranslation(translation, "@interface Example : NSObject < JavaIoSerializable >");
  }

  public void testImportTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class MyException extends Exception { MyException(RuntimeException t) {super(t);}}",
        "MyException", "MyException.h");
    assertTranslation(translation, "@class JavaLangRuntimeException;");
    assertTranslation(translation, "#include \"java/lang/Exception.h\"");
  }

  public void testHeaderFileMapping() throws IOException {
    Options.setHeaderMappingFiles(Lists.newArrayList("testMappings.j2objc"));
    addSourceFile("package unit.mapping.custom; public class Test { }",
        "unit/mapping/custom/Test.java");
    loadHeaderMappings();
    String translation = translateSourceFile(
        "import unit.mapping.custom.Test; "
            + "public class MyTest extends Test { MyTest() {}}",
        "MyTest", "MyTest.h");
    assertTranslation(translation, "#include \"my/mapping/custom/Test.h\"");
  }

  public void testHeaderDefaultFileMapping() throws IOException {
    addSourceFile("package unit.mapping; public class Test { }", "unit/mapping/Test.java");
    loadHeaderMappings();
    String translation = translateSourceFile(
        "import unit.mapping.Test; "
            + "public class MyTest extends Test { MyTest() {}}",
        "MyTest", "MyTest.h");
    assertTranslation(translation, "#include \"my/mapping/Test.h\"");
  }

  public void testNoHeaderMapping() throws IOException {
    // Should be able to turn off header mappings by passing empty collection
    Options.setHeaderMappingFiles(Collections.<String>emptyList());
    addSourceFile("package unit.mapping; public class Test { }", "unit/mapping/Test.java");
    loadHeaderMappings();
    String translation = translateSourceFile(
        "import unit.mapping.Test; "
            + "public class MyTest extends Test { MyTest() {}}",
        "MyTest", "MyTest.h");
    assertTranslation(translation, "#include \"unit/mapping/Test.h\"");
  }

  public void testOutputHeaderFileMapping() throws IOException {
    Options.setHeaderMappingFiles(Lists.newArrayList("testMappings.j2objc"));
    Options.setOutputStyle(Options.OutputStyleOption.SOURCE);
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
    assertTranslation(translation, "#include \"unit/test/Dummy.h\"");

    HeaderMap headerMap = Options.getHeaderMap();
    assertEquals(headerMap.get("unit.test.Dummy"), "unit/test/Dummy.h");
    assertEquals(headerMap.get("unit.test.AnotherDummy"), "unit/test/AnotherDummy.h");
    assertEquals(headerMap.get("unit.mapping.custom.Test"), "my/mapping/custom/Test.h");
    assertEquals(headerMap.get("unit.mapping.custom.AnotherTest"), "my/mapping/custom/Test.h");
  }

  public void testOutputHeaderFileMappingWithMultipleClassesInOneHeader() throws IOException {
    Options.setHeaderMappingFiles(Lists.newArrayList("testMappings.j2objc"));
    Options.setOutputStyle(Options.OutputStyleOption.SOURCE);
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
    assertTranslation(translationForDummy, "#include \"my/mapping/custom/Test.h\"");
    assertTranslation(translationForAnotherDummy, "#include \"my/mapping/custom/Test.h\"");

    HeaderMap headerMap = Options.getHeaderMap();
    assertEquals(headerMap.get("unit.test.Dummy"), "unit/test/Dummy.h");
    assertEquals(headerMap.get("unit.test.AnotherDummy"), "unit/test/AnotherDummy.h");
    assertEquals(headerMap.get("unit.mapping.custom.Test"), "my/mapping/custom/Test.h");
    assertEquals(headerMap.get("unit.mapping.custom.AnotherTest"), "my/mapping/custom/Test.h");
  }

  public void testCombinedGeneration() throws IOException {
    Options.setSegmentedHeaders(false);
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
    assertTranslation(header, "#ifndef UnitFoo_H");
    assertTranslation(header, "#define UnitFoo_H");
    assertTranslation(header, "@interface UnitTest");
    assertTranslation(header, "- (instancetype)init;");
    assertTranslation(header, "- (void)Dummy;");
    assertTranslation(header, "J2OBJC_EMPTY_STATIC_INIT(UnitTest)");
    assertTranslation(header, "J2OBJC_TYPE_LITERAL_HEADER(UnitTest)");
    assertTranslation(header, "@interface UnitAnotherTest : UnitTest");
    assertTranslation(header, "- (void)AnotherDummy;");
    assertTranslation(header, "J2OBJC_EMPTY_STATIC_INIT(UnitAnotherTest)");
    assertTranslation(header, "J2OBJC_TYPE_LITERAL_HEADER(UnitAnotherTest)");
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
    assert header.indexOf("@interface UnitTest") < header.indexOf("@interface UnitAnotherTest");
  }

  public void testCombinedJarHeaderMapping() throws IOException {
    File outputHeaderMappingFile = new File(tempDir, "mappings.j2objc");
    Options.setOutputHeaderMappingFile(outputHeaderMappingFile);
    Options.setOutputStyle(Options.OutputStyleOption.SOURCE);
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

    HeaderMap headerMap = Options.getHeaderMap();
    assertEquals("unit/Foo.h", headerMap.get("unit.Test"));
    assertEquals("unit/Foo.h", headerMap.get("unit.AnotherTest"));
    assertTranslation(header2, "#include \"unit/Foo.h\"");
    assertEquals("unit2/Foo.h", headerMap.get("unit2.AnotherTest"));
    assertEquals("unit2/Foo.h", headerMap.get("unit2.YetAnotherTest"));
  }

  public void testForwardDeclarationTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class MyException extends Exception { MyException(RuntimeException t) {super(t);}}",
        "MyException", "MyException.h");
    assertTranslation(translation, "@class JavaLangRuntimeException;");
  }

  public void testInstanceVariableTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class Example { Exception testException; }",
        "Example", "Example.h");
    assertTranslation(translation, "JavaLangException *testException_;");
  }

  public void testInterfaceTranslation() throws IOException {
    String translation = translateSourceFile(
        "package unit.test; public interface Example {}",
        "Example", "unit/test/Example.h");
    assertTranslation(translation, "@protocol UnitTestExample");
  }

  public void testDeprecatedInterfaceTranslation() throws IOException {
    Options.enableDeprecatedDeclarations();
    String translation = translateSourceFile(
      "package unit.test; public @Deprecated interface Example {}",
      "Example", "unit/test/Example.h");
    assertTranslation(translation, "__attribute__((deprecated))\n@protocol UnitTestExample");
  }

  public void testInterfaceWithMethodTranslation() throws IOException {
    String translation = translateSourceFile(
        "package unit.test; public interface Example { Example getExample(); }",
        "Example", "unit/test/Example.h");
    assertTranslation(translation, "(id<UnitTestExample>)getExample;");
  }

  public void testInterfaceWithDeprecatedMethodTranslation() throws IOException {
    Options.enableDeprecatedDeclarations();
    String translation = translateSourceFile(
        "package unit.test; public interface Example { @Deprecated Example getExample(); }",
        "Example", "unit/test/Example.h");
    assertTranslation(translation,
        "- (id<UnitTestExample>)getExample __attribute__((deprecated));");
  }

  public void testSuperInterfaceTranslation() throws IOException {
    String translation = translateSourceFile(
        "package unit.test; public interface Example extends Bar {} interface Bar {}",
        "Example", "unit/test/Example.h");
    assertTranslation(translation,
        "@protocol UnitTestExample < UnitTestBar, JavaObject >");
  }

  public void testConstTranslation() throws IOException {
    String translation = translateSourceFile(
        "package unit.test; public class Example { public static final int FOO=1; }",
        "Example", "unit/test/Example.h");
    assertTranslation(translation, "#define UnitTestExample_FOO 1");
    assertFalse(translation.contains("initialize"));
  }

  public void testStaticVariableTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class Example { public static java.util.Date today; }",
        "Example", "Example.h");
    assertTranslatedLines(translation,
        "inline JavaUtilDate *Example_get_today();",
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
        "inline JavaUtilDate *Example_get_today();",
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
    assertTranslation(translation, "- (void)init__ OBJC_METHOD_FAMILY_NONE;");
    assertTranslation(translation, "- (void)_init OBJC_METHOD_FAMILY_NONE;");
  }

  public void testInitializeMessageTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class Example { void initialize() {} }", "Example", "Example.h");
    assertTranslation(translation, "- (void)initialize__ OBJC_METHOD_FAMILY_NONE;");
  }

  public void testToStringRenaming() throws IOException {
    String translation = translateSourceFile(
      "public class Example { public String toString() { return super.toString(); } }",
      "Example", "Example.h");
    assertTranslation(translation, "- (NSString *)description;");
  }

  public void testMultipleObjectDeclaration() throws IOException {
    String translation = translateSourceFile(
      "public class Example { String one, two, three; }",
      "Example", "Example.h");
    assertTranslation(translation, "NSString *one_, *two_, *three_;");
  }

  public void testMultiplePrimitiveDeclaration() throws IOException {
    String translation = translateSourceFile(
      "public class Example { int one, two, three; }",
      "Example", "Example.h");
    assertTranslation(translation, "int one_, two_, three_;");
  }

  public void testMultipleInterfaceDeclaration() throws IOException {
    String translation = translateSourceFile(
      "public class Example { Comparable one, two, three; }",
      "Example", "Example.h");
    assertTranslation(translation, "id<JavaLangComparable> one_, two_, three_;");
  }

  public void testMultipleClassDeclaration() throws IOException {
    String translation = translateSourceFile(
      "public class Example { Class<?> one, two, three; }",
      "Example", "Example.h");
    assertTranslation(translation, "IOSClass *one_, *two_, *three_;");
  }

  public void testInnerClassDeclaration() throws IOException {
    String translation = translateSourceFile(
      "public class Example { class Inner {} }",
      "Example", "Example.h");
    assertTranslation(translation, "@interface Example_Inner : NSObject");
    assertNotInTranslation(translation, "Example *this");
    assertTranslation(translation, "- (instancetype)initWithExample:(Example *)outer$;");
  }

  public void testInnerClassDeclarationWithOuterReference() throws IOException {
    String translation = translateSourceFile(
      "public class Example { int i; class Inner { void test() { int j = i; } } }",
      "Example", "Example.h");
    assertTranslation(translation, "@interface Example_Inner : NSObject");
    assertTranslation(translation, "- (instancetype)initWithExample:(Example *)outer$;");
    translation = getTranslatedFile("Example.m");
    assertTranslation(translation, "Example *this$0_;");
  }

  public void testEnum() throws IOException {
    String translation = translateSourceFile(
      "public enum Color { RED, WHITE, BLUE }",
      "Color", "Color.h");
    assertTranslatedLines(translation,
        "typedef NS_ENUM(NSUInteger, Color_Enum) {",
        "  Color_Enum_RED = 0,",
        "  Color_Enum_WHITE = 1,",
        "  Color_Enum_BLUE = 2,",
        "};");
    assertTranslation(translation, "@interface Color : JavaLangEnum < NSCopying >");
    assertTranslation(translation, "+ (IOSObjectArray *)values;");
    assertTranslation(translation, "+ (Color *)valueOfWithNSString:(NSString *)name;");
    assertTranslation(translation, "FOUNDATION_EXPORT Color *Color_values_[];");
    assertTranslatedLines(translation,
        "inline Color *Color_get_RED();",
        "J2OBJC_ENUM_CONSTANT(Color, RED)");
    assertTranslatedLines(translation,
        "inline Color *Color_get_WHITE();",
        "J2OBJC_ENUM_CONSTANT(Color, WHITE)");
    assertTranslatedLines(translation,
        "inline Color *Color_get_BLUE();",
        "J2OBJC_ENUM_CONSTANT(Color, BLUE)");
  }

  public void testEnumWithParameters() throws IOException {
    String translation = translateSourceFile(
        "public enum Color { RED(0xff0000), WHITE(0xffffff), BLUE(0x0000ff); "
        + "private int rgb; private Color(int rgb) { this.rgb = rgb; } "
        + "public int getRgb() { return rgb; }}",
        "Color", "Color.h");
    assertTranslation(translation, "@interface Color : JavaLangEnum");
    translation = getTranslatedFile("Color.m");
    assertTranslation(translation, "int rgb_;");
    assertTranslatedLines(translation,
        "void Color_initWithInt_withNSString_withInt_("
        + "Color *self, jint rgb, NSString *__name, jint __ordinal) {");
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
    assertTranslation(translation, "@interface Color : JavaLangEnum");
    translation = getTranslatedFile("Color.m");
    assertTranslation(translation, "jboolean primary_;");
    assertTranslation(translation,
        "Color_initWithInt_withBoolean_withNSString_withInt_("
        + "self, rgb, true, __name, __ordinal);");
    assertTranslatedLines(translation,
        "void Color_initWithInt_withBoolean_withNSString_withInt_("
          + "Color *self, jint rgb, jboolean primary, NSString *__name, jint __ordinal) {",
        "  JavaLangEnum_initWithNSString_withInt_(self, __name, __ordinal);",
        "  self->rgb_ = rgb;",
        "  self->primary_ = primary;",
        "}");
    assertTranslatedLines(translation,
        "void Color_initWithInt_withNSString_withInt_("
          + "Color *self, jint rgb, NSString *__name, jint __ordinal) {",
        "  Color_initWithInt_withBoolean_withNSString_withInt_("
          + "self, rgb, true, __name, __ordinal);",
        "}");
    assertTranslation(translation,
        "Color_initWithInt_withBoolean_withNSString_withInt_("
          + "e, (jint) 0xffffff, false, @\"WHITE\", 1);");
    assertTranslation(translation,
        "Color_initWithInt_withNSString_withInt_(e, (jint) 0x0000ff, @\"BLUE\", 2);");
  }

  // Verify that the "init" enum constant name is properly declared: b/27352834
  public void testInitEnumConstant() throws IOException {
    String translation = translateSourceFile(
      "public enum Test { init }", "Test", "Test.h");
    assertTranslation(translation, "Test_Enum_init_ = 0,");
    assertNotInTranslation(translation, "Test_Enum_init ");
  }

  public void testArrayFieldDeclaration() throws IOException {
    String translation = translateSourceFile(
      "public class Example { char[] before; char after[]; }",
      "Example", "Example.h");
    assertTranslation(translation, "IOSCharArray *before_;");
    assertTranslation(translation, "IOSCharArray *after_;");
  }

  public void testForwardDeclarationOfInnerType() throws IOException {
    String translation = translateSourceFile(
        "public class Example { Foo foo; class Foo {} }", "Example", "Example.h");
    // Test that Foo is forward declared because Example contains a field of
    // type Foo and Foo is declared after Example.
    assertTranslation(translation, "@class Example_Foo;");
  }

  public void testAnnotationGeneration() throws IOException {
    String translation = translateSourceFile(
      "package foo; import java.lang.annotation.*; @Retention(RetentionPolicy.RUNTIME) "
      + "public @interface Compatible { boolean fooable() default false; }",
      "Compatible", "foo/Compatible.h");

    // Test that the annotation was declared as a protocol and a value class.
    assertTranslation(translation, "@protocol FooCompatible < JavaLangAnnotationAnnotation >");
    assertTranslation(translation, "@interface FooCompatible : NSObject < FooCompatible >");

    // Verify that the value is defined as a property instead of a method.
    assertTranslation(translation, "@public\n  jboolean fooable_;");
    assertTranslation(translation, "@property (readonly) jboolean fooable;");

    // Check that constructor was created with the property as parameter.
    assertTranslation(translation,
        "FOUNDATION_EXPORT id<FooCompatible> create_FooCompatible(jboolean fooable);");

    translation = getTranslatedFile("foo/Compatible.m");
    // Verify default value accessor is generated for property.
    assertTranslation(translation, "+ (jboolean)fooableDefault {");
  }

  public void testCharacterEdgeValues() throws IOException {
    String translation = translateSourceFile(
      "public class Test { "
      + "  public static final char MIN = 0; "
      + "  public static final char MAX = '\uffff'; "
      + "}", "Test", "Test.h");
    assertTranslation(translation, "x00");
    assertTranslation(translation, "0xffff");
  }

  public void testEnumNaming() throws IOException {
    String translation = translateSourceFile(
        "public enum MyEnum { ONE, TWO, THREE }",
        "MyEnum", "MyEnum.h");
    assertTranslation(translation, "typedef NS_ENUM(NSUInteger, MyEnum_Enum) {");
    assertTranslation(translation, "@interface MyEnum : JavaLangEnum");
    assertTranslation(translation, "FOUNDATION_EXPORT MyEnum *MyEnum_values_[];");
    assertTranslation(translation, "inline MyEnum *MyEnum_get_ONE();");
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
    assertTranslation(translation, "NSCopying");
  }

  // Verify that an empty Java enum doesn't define an empty C enum,
  // which is illegal.
  public void testEmptyEnum() throws IOException {
    String header = translateSourceFile("public class A { enum Foo {} }", "A", "A.h");
    String impl = getTranslatedFile("A.m");

    // Verify there's no C enum.
    assertFalse(header.contains("typedef enum {\n} A_Foo;"));

    // Verify there's still a Java enum type.
    assertTranslation(header, "@interface A_Foo : JavaLangEnum");
    assertTranslation(impl, "@implementation A_Foo");
  }

  public void testEnumWithInterfaces() throws IOException {
    String translation = translateSourceFile(
        "public class A { interface I {} "
        + "enum Foo implements I, Runnable, Cloneable { "
        + "A, B, C; public void run() {}}}", "A", "A.h");
    assertTranslation(translation,
        "@interface A_Foo : JavaLangEnum < NSCopying, A_I, JavaLangRunnable >");
    assertTranslation(translation, "#include \"java/lang/Runnable.h\"");
  }

  public void testExternalNativeMethod() throws IOException {
    String translation = translateSourceFile(
        "package foo; class Example { native void external(String s); "
        + "  void test(String str) { external(str); }}", "Example", "foo/Example.h");

    // Verify external() and test() are in main interface.
    assertTranslation(translation, "- (void)externalWithNSString:(NSString *)s;");
    assertTranslation(translation, "- (void)testWithNSString:(NSString *)str;");

    // Verify category method is invoked.
    translation = getTranslatedFile("foo/Example.m");
    assertTranslation(translation, "@implementation FooExample\n");
    assertTranslation(translation,
        "void FooExample_externalWithNSString_(FooExample *self, NSString *s);");
    assertTranslatedLines(translation,
        "- (void)externalWithNSString:(NSString *)s {",
        "  FooExample_externalWithNSString_(self, s);",
        "}");
    assertTranslation(translation, "[self externalWithNSString:str];");
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
        "__unsafe_unretained FooBar_Internal *fieldBar_;",
        "FooBar_Internal *fieldFoo_;");
  }

  public void testAddIgnoreDeprecationWarningsPragmaIfDeprecatedDeclarationsIsEnabled()
      throws IOException {
    Options.enableDeprecatedDeclarations();

    String sourceContent = "class Test {}";
    String translation = translateSourceFile(sourceContent, "FooBar", "FooBar.h");

    assertTranslation(translation, "#pragma clang diagnostic push");
    assertTranslation(translation, "#pragma GCC diagnostic ignored \"-Wdeprecated-declarations\"");
    assertTranslation(translation, "#pragma clang diagnostic pop");
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
    assertTranslation(translation, "@protocol Test_Initialize < JavaLangAnnotationAnnotation >");
    assertTranslation(translation, "@interface Test_Initialize : NSObject < Test_Initialize >");
  }

  public void testFieldSetterGeneration() throws IOException {
    String translation = translateSourceFile(
        "import com.google.j2objc.annotations.Weak;"
        + "class Test { Object o; @Weak String s; static Integer i; }", "Test", "Test.h");
    assertTranslation(translation, "J2OBJC_FIELD_SETTER(Test, o_, id)");
    // Make sure the @Weak and static fields don't generate setters.
    assertOccurrences(translation, "J2OBJC_FIELD_SETTER", 1);
  }

  public void testEnumWithNameAndOrdinalParameters() throws IOException {
    String translation = translateSourceFile(
      "public enum Test { FOO(\"foo\", 3), BAR(\"bar\", 5); "
      + "private String name; private int ordinal; "
      + "private Test(String name, int ordinal) { this.name = name; this.ordinal = ordinal; }"
      + "public String getName() { return name; }}",
      "Test", "Test.h");
    assertTranslation(translation, "@interface Test : JavaLangEnum");
    translation = getTranslatedFile("Test.m");
    assertTranslation(translation, "NSString *name_Test_;");
    assertTranslation(translation, "int ordinal_Test_;");
    assertTranslation(translation,
        "void Test_initWithNSString_withInt_withNSString_withInt_("
        + "Test *self, NSString *name, jint ordinal, NSString *__name, jint __ordinal) {");
  }

  public void testDeprecatedEnumType() throws IOException {
    Options.enableDeprecatedDeclarations();
    String translation = translateSourceFile(
        "@Deprecated public enum Test { A, B }", "Test", "Test.h");
    assertTranslation(translation, "__attribute__((deprecated))\n@interface Test");
  }

  public void testLongConstants() throws IOException {
    String translation = translateSourceFile(
        "class Test { static final long FOO = 123; }", "Test", "Test.h");
    assertTranslation(translation, "123LL");
  }

  public void testCustomWeakAnnotations() throws IOException {
    String translation = translateSourceFile(
        "class Test { @interface Weak {} @interface WeakOuter {}"
        + " void foo() {}"
        + " @WeakOuter public class Inner { void bar() { foo(); } }"
        + " @Weak public Object obj; }", "Test", "Test.h");
    assertTranslation(translation, "__unsafe_unretained id obj_;");
    translation = getTranslatedFile("Test.m");
    assertTranslation(translation, "__unsafe_unretained Test *this$0_;");
  }

  public void testReservedWordAsAnnotationPropertyName() throws IOException {
    String translation = translateSourceFile(
        "package foo; import java.lang.annotation.*; @Retention(RetentionPolicy.RUNTIME) "
        + "public @interface Bar { String namespace() default \"\"; }",
        "Bar", "foo/Bar.h");
    assertTranslation(translation, "@property (readonly) NSString *namespace__;");
    assertTranslatedLines(translation,
        "@interface FooBar : NSObject < FooBar > {", "@public", "NSString *namespace___;", "}");
    assertTranslation(translation,
        "FOUNDATION_EXPORT id<FooBar> create_FooBar(NSString *namespace__);");
    translation = getTranslatedFile("foo/Bar.m");
    assertTranslation(translation, "+ (NSString *)namespace__Default {");
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
        "- (instancetype)initWithInt:(jint)i;",
        "",
        "#pragma mark Protected",
        "",
        "- (void)gnuWithNSString:(NSString *)s",
                        "withInt:(jint)i",
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
    Options.setSegmentedHeaders(false);
    addSourceFile("class Foo { static class Bar { } }", "Foo.java");
    String translation = translateSourceFile(
        "class Test extends Foo { Foo.Bar bar; }", "Test", "Test.h");
    assertTranslation(translation, "#include \"Foo.h\"");
    // Forward declaration for Foo_Bar is not needed because we've included Foo.h.
    assertNotInTranslation(translation, "@class Foo_Bar");
  }

  // Verify that the default constructor is disallowed if the class has a non-default
  // constructor.
  public void testDefaultConstructorDisallowed() throws IOException {
    Options.setDisallowInheritedConstructors(true);
    String translation = translateSourceFile("class Test { Test(int i) {} }", "Test", "Test.h");
    assertTranslation(translation, "- (instancetype)initWithInt:(jint)i;");
    assertTranslation(translation, "- (instancetype)init NS_UNAVAILABLE;");
  }

  // Verify that inherited constructors are disallowed. Exception has four constructors,
  // so a subclass that only implements one should have the other three disallowed.
  public void testConstructorsDisallowed() throws IOException {
    Options.setDisallowInheritedConstructors(true);
    String translation = translateSourceFile(
        "class Test extends Exception { Test(String s) { super(s); } }", "Test", "Test.h");
    assertTranslation(translation, "- (instancetype)initWithNSString:(NSString *)s;");
    assertTranslation(translation, "- (instancetype)init NS_UNAVAILABLE;");
    assertTranslation(translation,
        "- (instancetype)initWithNSException:(NSException *)arg0 NS_UNAVAILABLE;");
    assertTranslatedLines(translation,
        "- (instancetype)initWithNSString:(NSString *)arg0",
        "withNSException:(NSException *)arg1 NS_UNAVAILABLE;");
    assertTranslatedLines(translation,
        "- (instancetype)initWithNSString:(NSString *)arg0",
        "withNSException:(NSException *)arg1",
        "withBoolean:(jboolean)arg2",
        "withBoolean:(jboolean)arg3 NS_UNAVAILABLE;");
  }

  public void testStaticInterfaceMethodDeclaredInCompanionClass() throws IOException {
    Options.setSourceVersion(SourceVersion.JAVA_8);
    createParser();

    String source = "interface Foo { static void f() {} }"
        + "class Bar implements Foo { void g() { Foo.f(); } }";
    String header = translateSourceFile(source, "Test", "Test.h");
    String impl =  getTranslatedFile("Test.m");

    assertTranslation(header, "@protocol Foo < JavaObject >");
    assertTranslatedSegments(header, "@interface Foo : NSObject", "+ (void)f;", "@end");
    // Should only have one occurrence from the companion class.
    assertOccurrences(header, "+ (void)f;", 1);

    // The companion class of Foo still has the class method +[Foo f].
    assertTranslatedLines(impl, "+ (void)f {", "Foo_f();", "}");
  }
}
