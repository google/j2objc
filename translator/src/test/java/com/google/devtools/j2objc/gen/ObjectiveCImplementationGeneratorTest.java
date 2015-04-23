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
import com.google.devtools.j2objc.Options.MemoryManagementOption;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tests for {@link ObjectiveCImplementationGenerator}.
 *
 * @author Tom Ball
 */
public class ObjectiveCImplementationGeneratorTest extends GenerationTest {

  public void testOuterVariableAccess() throws IOException {
    String translation = translateSourceFile(
        "public class Example { int foo; class Inner { int test() { return foo; }}}",
        "Example", "Example.m");
    assertTranslation(translation, "return this$0_->foo_;");
  }

  public void testTypeNameTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class Example {}", "Example", "Example.m");
    assertTranslation(translation, "#include \"Example.h\"");
  }

  public void testPackageTypeNameTranslation() throws IOException {
    String translation = translateSourceFile(
        "package unit.test; public class Example {}", "Example", "unit/test/Example.m");
    assertTranslation(translation, "#include \"unit/test/Example.h\"");
  }

  public void testHeaderFileMapping() throws IOException {
    Options.setHeaderMappingFiles(Lists.newArrayList("testMappings.j2objc"));
    loadHeaderMappings();
    addSourceFile("package unit.mapping.custom; public class Test { }",
        "unit/mapping/custom/Test.java");
    String translation = translateSourceFile(
        "import unit.mapping.custom.Test; "
            + "public class MyTest { MyTest(Test u) {}}",
        "MyTest", "MyTest.m");
    assertTranslation(translation, "#include \"my/mapping/custom/Test.h\"");
  }

  public void testPackageTypeNameTranslationWithInnerClass() throws IOException {
    String translation = translateSourceFile(
        "package unit.test; public class Example { class Inner {}}",
        "Example", "unit/test/Example.m");
    assertTranslation(translation, "#include \"unit/test/Example.h\"");
    assertFalse(translation.contains("#include \"unit/test/Example_Inner.h\""));
  }

  public void testSameClassMethodInvocation() throws IOException {
    String translation = translateSourceFile(
        "package unit.test; public class Example { void foo() {} void test() { foo(); }}",
        "Example", "unit/test/Example.m");
    assertTranslation(translation, "[self foo];");
  }

  public void testSameClassStaticMethodInvocation() throws IOException {
    String translation = translateSourceFile(
        "public class Example { static void foo() {} void test() { foo(); }}",
        "Example", "Example.m");
    assertTranslation(translation, "Example_foo();");
  }

  public void testSameClassStaticMethodPackageInvocation() throws IOException {
    String translation = translateSourceFile(
        "package unit.test; public class Example { static void foo() {} void test() { foo(); }}",
        "Example", "unit/test/Example.m");
    assertTranslation(translation, "UnitTestExample_foo();");
  }

  public void testConstStaticIntTranslation() throws IOException {
    String translation = translateSourceFile(
        "public class Example { public static final int FOO=1; int test() { return FOO; }}",
        "Example", "Example.h");
    assertTranslation(translation, "#define Example_FOO 1");
    translation = getTranslatedFile("Example.m");
    assertFalse(translation.contains("initialize"));
  }

  public void testConstVariableInOtherClassTranslation() throws IOException {
    String translation = translateSourceFile("public class Example { "
        + "int test() { return Bool.FOO; } } class Bool { public static final int FOO=1; }",
        "Example", "Example.m");
    assertTranslation(translation, "return Bool_FOO;");
  }

  public void testStaticVariableInitialization() throws IOException {
    String translation = translateSourceFile(
        "public class Example { public static java.util.Date today; }",
        "Example", "Example.m");
    assertTranslation(translation, "JavaUtilDate *Example_today_;");
    assertFalse(translation.contains("initialize"));
  }

  public void testStaticVariableWithInitInitialization() throws IOException {
    String translation = translateSourceFile(
        "public class Example { public static java.util.Date today = new java.util.Date();}",
        "Example", "Example.m");
    assertTranslation(translation, "JavaUtilDate *Example_today_;");
    assertTranslation(translation, "+ (void)initialize {");
    assertTranslation(translation,
        "JreStrongAssignAndConsume(&Example_today_, nil, new_JavaUtilDate_init());");
  }

  public void testStaticVariableWithNonInitInitialization() throws IOException {
    String translation = translateSourceFile(
        "public class Example { "
        + "  public static java.util.logging.Logger logger ="
        + "      java.util.logging.Logger.getLogger(\"Test\");}",
        "Example", "Example.m");
    assertTranslation(translation,
        "JreStrongAssign(&Example_logger_, nil, "
        + "JavaUtilLoggingLogger_getLoggerWithNSString_(@\"Test\"));");
  }

  public void testStaticVariableInGenericInnerClass() throws IOException {
    String translation = translateSourceFile(
        "public class Example { "
        + "  static class Inner<T> { "
        + "    static int foo = 1; "
        + "    final int myFoo = foo++; }}",
        "Example", "Example.m");
    assertTranslation(translation, "int Example_Inner_foo_ = 1");
    assertTranslation(translation, "myFoo_ = Example_Inner_foo_++");
  }

  public void testStaticVariableWithGenericTypeCast() throws IOException {
    String translation = translateSourceFile(
        "public class Example<B> { "
        + "  public <B> Example<B> foo() { return (Example<B>) FOO; } "
        + "  public static final Example<?> FOO = new Example(); }",
        "Example", "Example.m");
    assertTranslation(translation,
        "return (Example *) check_class_cast(Example_FOO_, [Example class])");
  }

  public void testStaticVariableInOtherVariable() throws IOException {
    String translation = translateSourceFile("public class Example { "
        + "void test() { Bar.FOO=2; } } class Bar { public static int FOO=1; }",
       "Example", "Example.m");
    assertTranslation(translation, "int Bar_FOO_ = 1;");
    assertTranslation(translation, "*Bar_getRef_FOO_() = 2;");
  }

  public void testNSObjectMessageRename() throws IOException {
    String translation = translateSourceFile(
        "public class Example { int load() { return 1; } int test() { return load(); }}",
        "Example", "Example.m");
    assertTranslation(translation, "- (jint)load__ {");
    assertTranslation(translation, "return [self load__];");
  }

  public void testNSObjectMessageQualifiedNameRename() throws IOException {
    String translation = translateSourceFile(
        "public class Example { int load() { return 1; } int test() { return this.load(); }}",
        "Example", "Example.m");
    assertTranslation(translation, "return [self load__];");
  }

  public void testNSObjectMessageSuperRename() throws IOException {
    String translation = translateSourceFile(
        "public class Example { int load() { return 1; }} "
        + "class SubClass extends Example { int load() { return super.load(); }}",
        "Example", "Example.m");
    assertTranslation(translation, "return [super load__];");
  }

  public void testNSObjectMessageStaticRename() throws IOException {
    String translation = translateSourceFile(
        "public class Example { static int load() { return 1; }} "
        + "class Other { int test() { return Example.load(); }}",
        "Example", "Example.m");
    assertTranslation(translation, "return Example_load__();");
  }

  public void testToStringRenaming() throws IOException {
    String translation = translateSourceFile(
      "public class Example { public String toString() { return super.toString(); } }",
      "Example", "Example.m");
    assertTranslation(translation, "- (NSString *)description {");
  }

  public void testInnerClassAccessToOuterMethods() throws IOException {
    String translation = translateSourceFile(
        "public class Example {"
        + "public int size() { return 0; } public void add(int n) {} "
        + "class Inner {} "
        + "class Innermost { void test() { Example.this.add(size()); }}}",
        "Example", "Example.m");
    assertTranslation(translation, "[this$0_ addWithInt:[this$0_ size]];");
  }

  public void testEnum() throws IOException {
    String translation = translateSourceFile(
      "public enum Color { RED, WHITE, BLUE }",
      "Color", "Color.m");
    assertTranslation(translation, "ColorEnum *ColorEnum_values_[3];");
    assertTranslation(translation, "@implementation ColorEnum");
    assertTranslation(translation,
        "ColorEnum_RED = new_ColorEnum_initWithNSString_withInt_(@\"RED\", 0);");
    assertTranslation(translation, "for (int i = 0; i < 3; i++) {");
    assertTranslation(translation, "ColorEnum *e = ColorEnum_values_[i];");
  }

  public void testEnumWithParameters() throws IOException {
    String sourceContent =
        "public enum Color { RED(0xff0000), WHITE(0xffffff), BLUE(0x0000ff); "
        + "private int rgb; private int newValue;"
        + "private Color(int rgb) { this.rgb = rgb; } "
        + "public int getRgb() { return rgb; }}";
    String translation = translateSourceFile(sourceContent, "Color", "Color.m");
    assertTranslation(translation, "@implementation ColorEnum");
    assertTranslation(translation,
        "ColorEnum_RED = new_ColorEnum_initWithInt_withNSString_withInt_("
        + "(jint) 0xff0000, @\"RED\", 0);");
    assertTranslation(translation,
        "ColorEnum_WHITE = new_ColorEnum_initWithInt_withNSString_withInt_("
        + "(jint) 0xffffff, @\"WHITE\", 1);");
    assertTranslation(translation,
        "ColorEnum_BLUE = new_ColorEnum_initWithInt_withNSString_withInt_("
        + "(jint) 0x0000ff, @\"BLUE\", 2);");
    assertTranslation(translation, "- (jint)getRgb {");
    assertTranslation(translation, "return rgb_;");
    assertTranslation(translation, "jint newValue_;");
  }

  public void testClassField() throws IOException {
    String sourceContent =
        "import com.google.j2objc.annotations.Weak;"
        + "public class FooBar {"
        + "private static int fieldPhi;"
        + "private Object fieldFoo;"
        + "@Weak private Object fieldJar;"
        + "private int newFieldBar;"
        + "}";
    String translation = translateSourceFile(sourceContent, "FooBar", "FooBar.m");
    assertTranslation(translation, "int FooBar_fieldPhi_;");
    assertTranslation(translation, "RELEASE_(fieldFoo_);");
    assertTranslation(translation, "id fieldFoo_;");
    assertTranslation(translation, "id fieldJar_;");
    assertTranslation(translation, "int newFieldBar_;");
    assertTranslation(translation, "id fieldFoo_;");
    assertTranslation(translation, "__weak id fieldJar_;");
    assertTranslation(translation, "int newFieldBar_;");
    assertTranslation(translation, "J2OBJC_STATIC_FIELD_GETTER(FooBar, fieldPhi_, jint)");
    assertTranslation(translation, "J2OBJC_STATIC_FIELD_REF_GETTER(FooBar, fieldPhi_, jint)");
  }

  public void testEmptyInterfaceGenerationNoMetadata() throws IOException {
    Options.setStripReflection(true);
    String translation = translateSourceFile(
        "package foo; public interface Compatible {}",
        "Compatible", "foo/Compatible.m");
    assertNotInTranslation(translation, "@interface");
  }

  public void testEmptyInterfaceGeneration() throws IOException {
    String translation = translateSourceFile(
        "package foo; public interface Compatible {}",
        "Compatible", "foo/Compatible.m");
    assertTranslation(translation, "@interface FooCompatible : NSObject");
    assertTranslation(translation, "@implementation FooCompatible");
    assertTranslation(translation, "+ (const J2ObjcClassInfo *)__metadata");
  }

  public void testInterfaceConstantGeneration() throws IOException {
    String translation = translateSourceFile(
        "package foo; public interface Compatible { "
        + "public static final Object FOO = new Object(); }",
        "Compatible", "foo/Compatible.m");
    assertTranslation(translation, "id FooCompatible_FOO_;");
    assertTranslation(translation,
        "JreStrongAssignAndConsume(&FooCompatible_FOO_, nil, new_NSObject_init());");
  }

  public void testAnnotationGeneration() throws IOException {
    String translation = translateSourceFile(
        "package foo; import java.lang.annotation.*; @Retention(RetentionPolicy.RUNTIME) "
        + "public @interface Compatible { boolean fooable() default false; }",
        "Compatible", "foo/Compatible.m");
    assertTranslation(translation, "@implementation FooCompatible");
    assertTranslation(translation, "@synthesize fooable = fooable_;");

    // Verify constructor generated.
    assertTranslation(translation, "- (instancetype)initWithFooable:(jboolean)fooable__ {");
    assertTranslation(translation, "fooable_ = fooable__;");

    // Verify default value accessor.
    assertTranslatedLines(translation,
        "+ (jboolean)fooableDefault {",
        "return NO;");

    assertTranslatedLines(translation,
        "- (IOSClass *)annotationType {",
        "return FooCompatible_class_();");
  }

  public void testMethodsWithTypeParameters() throws IOException {
    String translation = translateSourceFile(
        "public class Example implements Foo<String> { public void doFoo(String foo) {}} "
        + "interface Foo<T> { void doFoo(T foo); }",
        "Example", "Example.m");
    assertTranslation(translation, "- (void)doFooWithId:(NSString *)foo {");
  }

  public void testTopLevelClassesNotImported() throws IOException {
    String translation = translateSourceFile(
        "public class Example { Bar bar; } class Bar {}",
        "Example", "Example.m");
    assertFalse(translation.contains("#include \"Bar.h\""));
  }

  public void testEnumWithStaticVar() throws IOException {
    String translation = translateSourceFile(
        "public enum Example { ONE, TWO; public static int foo = 42; }",
        "Example", "Example.m");

    // Verify that there's only one initialize() method.
    String initializeSignature = "+ (void)initialize";
    int initializeOffset = translation.indexOf(initializeSignature);
    assertTrue(initializeOffset != -1);
    initializeOffset = translation.indexOf(initializeSignature,
        initializeOffset + initializeSignature.length());
    assertTrue(initializeOffset == -1);

    assertTranslation(translation, "int ExampleEnum_foo_ = 42;");
  }

  public void testNativeCodeBlock() throws IOException {
    String translation = translateSourceFile(
        "public class Example { native void test() /*-[ good native code block ]-*/; }",
        "Example", "Example.m");
    assertTranslation(translation, "good native code block");
  }

  public void testImportDerivedTypeInMethodParams() throws IOException {
    addSourceFile("abstract class Foo implements java.util.List { }", "Foo.java");
    addSourceFile("class Bar { Foo foo() { return null; } }", "Bar.java");
    String translation = translateSourceFile(
        "class Test { "
        + "  void baz(java.util.List list) { }"
        + "  void foobar() { baz(new Bar().foo()); } }",
        "Test", "Test.m");
    assertTranslation(translation, "#include \"Foo.h\"");
  }

  public void testImportDerivedTypeInConstructorParams() throws IOException {
    addSourceFile("abstract class Foo implements java.util.List { }", "Foo.java");
    addSourceFile("class Bar { Foo foo() { return null; } }", "Bar.java");
    addSourceFile("class Baz { Baz(java.util.List l) { } }", "Baz.java");
    String translation = translateSourceFile(
        "class Test { "
        + "  void foobar() { new Baz(new Bar().foo()); } }",
        "Test", "Test.m");
    assertTranslation(translation, "#include \"Foo.h\"");
  }

  public void testEnumWithEnumField() throws IOException {
    String header = translateSourceFile(
        "public class Test { "
        + "enum Type { BOOLEAN(false), INT(0), STRING(\"\"); "
        + "Type(Object value) { this.value = value; } private Object value; } "
        + "enum Field { BOOL(Type.BOOLEAN), INT32(Type.INT), "
        + "STRING(Type.STRING) { public boolean isPackable() { return false; }}; "
        + "Field(Type type) { this.type = type; } private Type type;"
        + "public boolean isPackable() { return true; }}}",
        "Test", "Test.h");
    String impl = getTranslatedFile("Test.m");
    assertFalse(header.contains("isPackableWithTest_TypeEnum"));
    assertFalse(impl.contains("\n  return NO;\n  [super initWithTest_TypeEnum:arg$0]}"));
    assertTranslation(impl,
        "Test_FieldEnum_STRING = new_Test_Field_$1Enum_initWithTest_TypeEnum_withNSString_withInt_("
        + "Test_TypeEnum_get_STRING(), @\"STRING\", 2);");
  }

  public void testAutoreleasePoolMethod() throws IOException {
    String translation = translateSourceFile(
        "import com.google.j2objc.annotations.AutoreleasePool;"
        + "public class Test {"
        + "  @AutoreleasePool\n"
        + "  public void foo() { }"
        + "}",
        "Test", "Test.m");
    assertTranslation(translation, "- (void)foo {\n"
        + "  @autoreleasepool {\n"
        + "  }\n"
        + "}");
  }

  public void testARCAutoreleasePoolMethod() throws IOException {
    Options.setMemoryManagementOption(MemoryManagementOption.ARC);
    String translation = translateSourceFile(
        "import com.google.j2objc.annotations.AutoreleasePool;"
        + "public class Test {"
        + "  @AutoreleasePool\n"
        + "  public void foo() { }"
        + "}",
        "Test", "Test.m");
    assertTranslation(translation, "- (void)foo {\n"
        + "  @autoreleasepool {\n"
        + "  }\n"
        + "}");
  }

  public void testAutoreleasePoolAnonymousClassMethod() throws IOException {
    String translation = translateSourceFile(
        "import com.google.j2objc.annotations.AutoreleasePool;"
        + "public class Test {"
        + "  interface Foo {"
        + "    void apply();"
        + "  }"
        + "  Foo foo() {"
        + "    return new Foo() {"
        + "      @AutoreleasePool\n"
        + "      public void apply() { }"
        + "    };"
        + "  }"
        + "}",
        "Test", "Test.m");
    assertTranslation(translation, "- (void)apply {\n"
        + "  @autoreleasepool {\n  }\n"
        + "}");
  }

  public void testInnerConstructorGenerated() throws IOException {
    String translation = translateSourceFile(
        "public class Test {"
        + "  public Test() { this(42); }"
        + "  public Test(int i) {} }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "- (instancetype)init {",
        "  Test_init(self);",
        "  return self;",
        "}");
    assertTranslatedLines(translation,
        "- (instancetype)initWithInt:(jint)i {",
        "  Test_initWithInt_(self, i);",
        "  return self;",
        "}");
    assertTranslatedLines(translation,
        "void Test_init(Test *self) {",
        "  Test_initWithInt_(self, 42);",
        "}");
    assertTranslatedLines(translation,
        "void Test_initWithInt_(Test *self, jint i) {",
        "  NSObject_init(self);",
        "}");

  }

  public void testInnerConstructorGeneratedForNonStaticInnerClass() throws IOException {
    String translation = translateSourceFile(
        "public class Test {"
        + "  class Inner {"
        + "    public Inner() { this(42); }"
        + "    public Inner(int i) {} } }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "- (instancetype)initWithTest:(Test *)outer$ {",
        "  Test_Inner_initWithTest_(self, outer$);",
        "  return self;",
        "}");
    assertTranslatedLines(translation,
        "- (instancetype)initWithTest:(Test *)outer$",
        "                     withInt:(jint)i {",
        "  Test_Inner_initWithTest_withInt_(self, outer$, i);",
        "  return self;",
        "}");
    assertTranslatedLines(translation,
        "void Test_Inner_initWithTest_(Test_Inner *self, Test *outer$) {",
        "  Test_Inner_initWithTest_withInt_(self, outer$, 42);",
        "}");
    assertTranslatedLines(translation,
        "void Test_Inner_initWithTest_withInt_(Test_Inner *self, Test *outer$, jint i) {",
        "  NSObject_init(self);",
        "}");
  }

  public void testSynchronizedMethod() throws IOException {
    String translation = translateSourceFile(
        "public class Test {"
        + "  public synchronized void foo() {} }",
        "Test", "Test.m");
    assertTranslation(translation, "- (void)foo {\n"
        + "  @synchronized(self) {");
  }

  public void testStaticSynchronizedMethod() throws IOException {
    String translation = translateSourceFile(
        "public class Test {"
        + "  public static synchronized void foo() {} }",
        "Test", "Test.m");
    assertTranslation(translation, "void Test_foo() {\n"
        + "  Test_initialize();\n"
        + "  @synchronized(Test_class_()) {");
  }

  // Verify that an interface that has a generated implementation file and an Object method
  // like toString() doesn't print a description method.
  public void testNoInterfaceToString() throws IOException {
    String translation = translateSourceFile(
        "interface Test { public static final Object FOO = new Object(); String toString(); }",
        "Test", "Test.m");
    assertNotInTranslation(translation, "- (NSString *)description {");
  }

  public void testAddIgnoreDeprecationWarningsPragmaIfDeprecatedDeclarationsIsEnabled()
      throws IOException {
    Options.enableDeprecatedDeclarations();

    String translation = translateSourceFile(
            "class Test { public static String foo; }", "Test", "Test.m");

    assertTranslation(translation, "#pragma clang diagnostic push");
    assertTranslation(translation, "#pragma GCC diagnostic ignored \"-Wdeprecated-declarations\"");
    assertTranslation(translation, "#pragma clang diagnostic pop");
  }

  public void testDoNotAddIgnoreDeprecationWarningsPragmaIfDeprecatedDeclarationsIsDisabled()
      throws IOException {
    String translation = translateSourceFile(
            "class Test { public static String foo; }", "Test", "Test.m");

    assertNotInTranslation(translation, "#pragma clang diagnostic push");
    assertNotInTranslation(translation,
        "#pragma GCC diagnostic ignored \"-Wdeprecated-declarations\"");
    assertNotInTranslation(translation, "#pragma clang diagnostic pop");
  }

  public void testMethodAnnotationNoParameters() throws IOException {
    String translation = translateSourceFile(
        "import org.junit.*;"
        + "public class Test { @After void foo() {} }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "+ (IOSObjectArray *)__annotations_foo {",
        "return [IOSObjectArray arrayWithObjects:(id[]) "
        + "{ [[[OrgJunitAfter alloc] init] autorelease] } "
        + "count:1 type:JavaLangAnnotationAnnotation_class_()];");
  }

  public void testMethodAnnotationWithParameter() throws IOException {
    String translation = translateSourceFile(
        "import org.junit.*;"
        + "public class Test { @After void foo(int i) {} }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "+ (IOSObjectArray *)__annotations_fooWithInt_ {",
        "return [IOSObjectArray arrayWithObjects:(id[]) "
        + "{ [[[OrgJunitAfter alloc] init] autorelease] } "
        + "count:1 type:JavaLangAnnotationAnnotation_class_()];");
  }

  public void testConstructorAnnotationNoParameters() throws IOException {
    String translation = translateSourceFile(
        "public class Test { @Deprecated Test() {} }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "+ (IOSObjectArray *)__annotations_init {",
        "return [IOSObjectArray arrayWithObjects:(id[]) "
        + "{ [[[JavaLangDeprecated alloc] init] autorelease] } "
        + "count:1 type:JavaLangAnnotationAnnotation_class_()];");
  }

  public void testConstructorAnnotationWithParameter() throws IOException {
    String translation = translateSourceFile(
        "public class Test { @Deprecated Test(int i) {} }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "+ (IOSObjectArray *)__annotations_initWithInt_ {",
        "return [IOSObjectArray arrayWithObjects:(id[]) "
        + "{ [[[JavaLangDeprecated alloc] init] autorelease] } "
        + "count:1 type:JavaLangAnnotationAnnotation_class_()];");
  }

  public void testTypeAnnotationDefaultParameter() throws IOException {
    String translation = translateSourceFile(
        "import org.junit.*;"
        + "@Ignore public class Test { void test() {} }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "+ (IOSObjectArray *)__annotations {",
        "return [IOSObjectArray arrayWithObjects:(id[]) "
        + "{ [[[OrgJunitIgnore alloc] initWithValue:@\"\"] autorelease] } "
        + "count:1 type:JavaLangAnnotationAnnotation_class_()];");
  }

  public void testTypeAnnotationWithParameter() throws IOException {
    String translation = translateSourceFile(
        "import org.junit.*;"
        + "@Ignore(\"some \\\"escaped\\n comment\") public class Test { void test() {} }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "+ (IOSObjectArray *)__annotations {",
        "return [IOSObjectArray arrayWithObjects:(id[]) "
        + "{ [[[OrgJunitIgnore alloc] initWithValue:"
        + "@\"some \\\"escaped\\n comment\"] autorelease] } "
        + "count:1 type:JavaLangAnnotationAnnotation_class_()];");
  }

  public void testFreeFormNativeCode() throws IOException {
    String translation = translateSourceFile(
        "class Test { void method1() {} /*-[ OCNI1 ]-*/ "
        + "enum Inner { A, B; /*-[ OCNI2 ]-*/ void method2() {}} "
        + " native void method3() /*-[ OCNI3 ]-*/; }", "Test", "Test.m");
    assertOccurrences(translation, "OCNI1", 1);
    assertOccurrences(translation, "OCNI2", 1);
    assertOccurrences(translation, "OCNI3", 1);
    String testType = "@implementation Test\n";
    String innerType = "@implementation Test_InnerEnum";
    String method1 = "- (void)method1";
    String method2 = "- (void)method2";
    String method3 = "- (void)method3";
    assertOccurrences(translation, testType, 1);
    assertOccurrences(translation, innerType, 1);
    assertOccurrences(translation, method1, 1);
    assertOccurrences(translation, method2, 1);
    assertOccurrences(translation, method3, 1);

    assertTrue(translation.indexOf(testType) < translation.indexOf(method1));
    assertTrue(translation.indexOf(method1) < translation.indexOf("OCNI1"));
    assertTrue(translation.indexOf("OCNI1") < translation.indexOf(method3));
    assertTrue(translation.indexOf(method3) < translation.indexOf("OCNI3"));
    assertTrue(translation.indexOf("OCNI3") < translation.indexOf(innerType));
    assertTrue(translation.indexOf(innerType) < translation.indexOf("OCNI2"));
    assertTrue(translation.indexOf("OCNI2") < translation.indexOf(method2));
  }

  public void testPrintsCountByEnumeratingWithState() throws IOException {
    String translation = translateSourceFile(
        "import java.util.Iterator; "
        + "abstract class Test implements Iterable { abstract public Iterator iterator(); }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "- (NSUInteger)countByEnumeratingWithState:(NSFastEnumerationState *)state "
        + "objects:(__unsafe_unretained id *)stackbuf count:(NSUInteger)len {",
        "return JreDefaultFastEnumeration(self, state, stackbuf, len);",
        "}");
  }

  public void testNoDuplicateCountByEnumeratingWithState() throws IOException {
    String translation = translateSourceFile(
        "import java.util.Iterator; "
        + "abstract class Test implements Iterable { abstract public Iterator iterator(); /*-[ "
        + "-(NSUInteger)countByEnumeratingWithState:(NSFastEnumerationState *)state "
        + "objects:(id *)stackbuf count:(NSUInteger)len { return 0; } ]-*/}",
        "Test", "Test.m");
    assertOccurrences(translation, "countByEnumeratingWithState", 1);
  }

  public void testSynchronizedNativeMethod() throws IOException {
    String translation = translateSourceFile(
        "class Test { public synchronized native void exit() /*-[ exit(0); ]-*/; }",
        "Test", "Test.m");
    assertTranslation(translation, "@synchronized(self)");
  }

  public void testMethodMetadata() throws IOException {
    String translation = translateSourceFile(
        // Separate methods are used so each only has one modifier.
        "abstract class Test<T> { "
        + " Object test1() { return null; }"  // package-private
        + " private char test2() { return 'a'; }"
        + " protected void test3() { }"
        + " final long test4() { return 0L; }"
        + " synchronized boolean test5() { return false; }"
        + " String test6(String s, Object... args) { return null; }"
        + " native void test7() /*-[ exit(0); ]-*/; "
        + " abstract void test8() throws InterruptedException, Error; "
        + " abstract T test9();"
        + " abstract void test10(int i, T t);"
        + " abstract <V,X> void test11(V one, X two, T three);"
        + "}",
        "Test", "Test.m");
    assertTranslation(translation, "{ \"test1\", NULL, \"Ljava.lang.Object;\", 0x0, NULL, NULL },");
    assertTranslation(translation, "{ \"test2\", NULL, \"C\", 0x2, NULL, NULL },");
    assertTranslation(translation, "{ \"test3\", NULL, \"V\", 0x4, NULL, NULL },");
    assertTranslation(translation, "{ \"test4\", NULL, \"J\", 0x10, NULL, NULL },");
    assertTranslation(translation, "{ \"test5\", NULL, \"Z\", 0x20, NULL, NULL },");
    assertTranslation(translation, "{ \"test6WithNSString:withNSObjectArray:\", "
        + "\"test6\", \"Ljava.lang.String;\", 0x80, NULL, NULL }");
    assertTranslation(translation, "{ \"test7\", NULL, \"V\", 0x100, NULL, NULL },");
    assertTranslation(translation, "{ \"test8\", NULL, \"V\", 0x400, "
        + "\"Ljava.lang.InterruptedException;Ljava.lang.Error;\", NULL },");
    assertTranslation(translation, "{ \"test9\", NULL, \"TT;\", 0x400, NULL, \"()TT;\" },");
    assertTranslation(translation,
        "{ \"test10WithInt:withId:\", \"test10\", \"V\", 0x400, NULL, \"(ITT;)V\" },");
    assertTranslation(translation,
        "{ \"test11WithId:withId:withId:\", \"test11\", \"V\", 0x400, NULL, "
        + "\"<V:Ljava/lang/Object;X:Ljava/lang/Object;>(TV;TX;TT;)V\" },");
  }

  public void testAnnotationWithField() throws IOException {
    String translation = translateSourceFile(
        "@interface Test { String FOO = \"foo\"; int I = 5; }", "Test", "Test.h");
    assertTranslation(translation, "#define Test_I 5");
    assertTranslation(translation, "FOUNDATION_EXPORT NSString *Test_FOO_;");
    assertTranslation(translation, "J2OBJC_STATIC_FIELD_GETTER(Test, FOO_, NSString *)");
    translation = getTranslatedFile("Test.m");
    assertTranslation(translation, "NSString *Test_FOO_ = @\"foo\";");
    assertTranslation(translation, "@interface Test : NSObject");
  }

  public void testCombinedGeneration() throws IOException {
    addSourceFile("package unit; public class Test { }", "unit/Test.java");
    addSourceFile("package unit; public class AnotherTest extends Test { }",
        "unit/AnotherTest.java");
    String translation = translateCombinedFiles(
        "unit/Foo", ".m", "unit/Test.java", "unit/AnotherTest.java");

    assertTranslation(translation, "source: unit/Foo.testfile");
    assertTranslation(translation, "#include \"unit/Foo.h\"");
    assertTranslation(translation, "#include \"J2ObjC_source.h\"");
    assertTranslation(translation, "@implementation UnitTest");
    assertTranslation(translation, "@implementation UnitAnotherTest");
  }

  public void testPackageInfoAnnotationAndDoc() throws IOException {
    addSourcesToSourcepaths();
    Options.setDocCommentsEnabled(true);
    addSourceFile("package foo.annotations;\n"
        + "import java.lang.annotation.*;\n"
        + "@Retention(RetentionPolicy.RUNTIME)\n"
        + "@Target(ElementType.PACKAGE)\n"
        + "public @interface Test {}",
        "foo/annotations/Test.java");
    String translation = translateSourceFile(
        "/** A package doc-comment. */\n"
        + "@Test\n"
        + "package foo.bar.mumble;\n"
        + "import foo.annotations.Test;",
        "package-info", "foo/bar/mumble/package-info.h");
    assertTranslation(translation, "_FooBarMumblepackage_info_H_");
    assertTranslatedLines(translation, "/**", "@brief A package doc-comment.", "*/");
    translation = getTranslatedFile("foo/bar/mumble/package-info.m");
    assertTranslation(translation, "@implementation FooBarMumblepackage_info");
    assertTranslation(translation, "+ (IOSObjectArray *)__annotations");
    assertTranslation(translation, "[FooAnnotationsTest alloc]");
  }

  public void testPackageInfoAnnotationNoDoc() throws IOException {
    addSourcesToSourcepaths();
    addSourceFile("package foo.annotations;\n"
        + "import java.lang.annotation.*;\n"
        + "@Retention(RetentionPolicy.RUNTIME)\n"
        + "@Target(ElementType.PACKAGE)\n"
        + "public @interface Test {}",
        "foo/annotations/Test.java");
    String translation = translateSourceFile(
        "@Test\n"
        + "package foo.bar.mumble;\n"
        + "import foo.annotations.Test;",
        "package-info", "foo/bar/mumble/package-info.h");
    assertTranslation(translation, "_FooBarMumblepackage_info_H_");
    assertNotInTranslation(translation, "/**");
    translation = getTranslatedFile("foo/bar/mumble/package-info.m");
    assertTranslation(translation, "@implementation FooBarMumblepackage_info");
    assertTranslation(translation, "+ (IOSObjectArray *)__annotations");
    assertTranslation(translation, "[FooAnnotationsTest alloc]");
  }

  public void testPackageInfoDocNoAnnotation() throws IOException {
    addSourcesToSourcepaths();
    Options.setDocCommentsEnabled(true);
    String translation = translateSourceFile(
        "/** A package doc-comment. */\n"
        + "package foo.bar.mumble;",
        "package-info", "foo/bar/mumble/package-info.h");
    assertTranslation(translation, "_FooBarMumblepackage_info_H_");
    assertTranslatedLines(translation, "/**", "@brief A package doc-comment.", "*/");
    translation = getTranslatedFile("foo/bar/mumble/package-info.m");
    assertNotInTranslation(translation, "@implementation FooBarMumblepackage_info");
    assertNotInTranslation(translation, "+ (IOSObjectArray *)__annotations");
  }

  public void testPackageInfoPrefixAnnotation() throws IOException {
    addSourcesToSourcepaths();
    addSourceFile(
        "@ObjectiveCName(\"FBM\")\n"
        + "package foo.bar.mumble;\n"
        + "import com.google.j2objc.annotations.ObjectiveCName;",
        "foo/bar/mumble/package-info.java");
    String translation = translateSourceFile("package foo.bar.mumble;\n"
            + "public class Test {}",
        "foo.bar.mumble.Test", "foo/bar/mumble/Test.h");
    assertTranslation(translation, "@interface FBMTest");
    assertTranslation(translation, "typedef FBMTest FooBarMumbleTest;");
    translation = getTranslatedFile("foo/bar/mumble/Test.m");
    assertTranslation(translation, "@implementation FBMTest");
    assertNotInTranslation(translation, "FooBarMumbleTest");
  }

  public void testPackageInfoPreprocessing() throws IOException {
    addSourceFile(
        "@ObjectiveCName(\"FBM\")\n"
        + "package foo.bar.mumble;\n"
        + "import com.google.j2objc.annotations.ObjectiveCName;",
        "foo/bar/mumble/package-info.java");
    preprocessFiles("foo/bar/mumble/package-info.java");
    String translation = translateSourceFile("package foo.bar.mumble;\n"
        + "public class Test {}",
        "foo.bar.mumble.Test", "foo/bar/mumble/Test.h");
    assertTranslation(translation, "@interface FBMTest");
    assertTranslation(translation, "typedef FBMTest FooBarMumbleTest;");
    translation = getTranslatedFile("foo/bar/mumble/Test.m");
    assertTranslation(translation, "@implementation FBMTest");
    assertNotInTranslation(translation, "FooBarMumbleTest");
  }

  public void testPackageInfoOnClasspath() throws IOException {
    addSourceFile(
        "@ObjectiveCName(\"FBM\")\n"
        + "package foo.bar.mumble;\n"
        + "import com.google.j2objc.annotations.ObjectiveCName;",
        "src/foo/bar/mumble/package-info.java");

    List<String> compileArgs = Lists.newArrayList();
    compileArgs.add("-classpath");
    compileArgs.add(System.getProperty("java.class.path"));
    compileArgs.add("-encoding");
    compileArgs.add(Options.getCharset().name());
    compileArgs.add("-source");
    compileArgs.add("1.6");
    compileArgs.add(tempDir.getAbsolutePath() + "/src/foo/bar/mumble/package-info.java");
    org.eclipse.jdt.internal.compiler.batch.Main batchCompiler =
        new org.eclipse.jdt.internal.compiler.batch.Main(
            new PrintWriter(System.out), new PrintWriter(System.err),
            false, Collections.emptyMap(), null);
    batchCompiler.compile(compileArgs.toArray(new String[0]));
    List<String> oldClassPathEntries = new ArrayList<String>(Options.getClassPathEntries());
    Options.getClassPathEntries().add(tempDir.getAbsolutePath() + "/src/");
    try {
      String translation = translateSourceFile("package foo.bar.mumble;\n"
          + "public class Test {}",
          "foo.bar.mumble.Test", "foo/bar/mumble/Test.h");
      assertTranslation(translation, "@interface FBMTest");
      assertTranslation(translation, "typedef FBMTest FooBarMumbleTest;");
      translation = getTranslatedFile("foo/bar/mumble/Test.m");
      assertTranslation(translation, "@implementation FBMTest");
      assertNotInTranslation(translation, "FooBarMumbleTest");
    } finally {
      Options.getClassPathEntries().clear();
      Options.getClassPathEntries().addAll(oldClassPathEntries);
    }
  }

  public void testInitializeNotInClassExtension() throws IOException {
    String translation = translateSourceFile(
        "class Test { static Integer i = new Integer(5); }", "Test", "Test.m");
    assertNotInTranslation(translation, "+ (void)initialize OBJC_METHOD_FAMILY_NONE;");
    assertOccurrences(translation, "+ (void)initialize", 1);
  }

  public void testInterfaceTypeLiteralAsAnnotationValue() throws IOException {
    addSourceFile(
        "import java.lang.annotation.*; @Retention(RetentionPolicy.RUNTIME)"
        + " @interface Foo { Class<?> value(); }", "Foo.java");
    String translation = translateSourceFile(
        "@Foo(CharSequence.class) class Test {}", "Test", "Test.m");
    assertTranslation(translation, "JavaLangCharSequence_class_()");
  }

  public void testMetadataHeaderGeneration() throws IOException {
    String translation = translateSourceFile("package foo; class Test {}", "Test", "foo/Test.m");
    assertTranslation(translation, "+ (const J2ObjcClassInfo *)__metadata");
    assertTranslation(translation, "static const J2ObjcClassInfo _FooTest = { "
        + Integer.toString(MetadataGenerator.METADATA_VERSION)
        + ", \"Test\", \"foo\"");
  }

  public void testReservedWordAsAnnotationPropertyName() throws IOException {
    String translation = translateSourceFile(
        "package foo; import java.lang.annotation.*; @Retention(RetentionPolicy.RUNTIME) "
        + "public @interface Bar { String namespace() default \"\"; } "
        + "class Test { Bar ann; String namespace() { return ann.namespace(); }}",
        "Bar", "foo/Bar.m");
    assertTranslation(translation, "@synthesize namespace__ = namespace___;");
    assertTranslation(translation,
        "- (instancetype)initWithNamespace__:(NSString *)namespace____ {");
    assertTranslation(translation, "self->namespace___ = RETAIN_(namespace____);");
    assertTranslation(translation, "+ (NSString *)namespace__Default {");
  }

  public void testAnnotationWithDefaultAnnotation() throws IOException {
    String translation = translateSourceFile(
        "import java.lang.annotation.*; public class A { "
        + "@Retention(RetentionPolicy.RUNTIME) "
        + "public @interface InnerAnn {} "
        + "@Retention(RetentionPolicy.RUNTIME) "
        + "public @interface OuterAnn { InnerAnn test() default @InnerAnn(); }}",
        "A", "A.m");
    assertTranslatedLines(translation,
        "+ (id<A_InnerAnn>)testDefault {", "return [[[A_InnerAnn alloc] init] autorelease];", "}");
  }

  public void testAnnotationWithDefaultAnnotationWithArguments() throws IOException {
    String translation = translateSourceFile(
        "import java.lang.annotation.*; public class A { "
        + "@Retention(RetentionPolicy.RUNTIME) "
        + "public @interface InnerAnn { String foo(); int num(); } "
        + "@Retention(RetentionPolicy.RUNTIME) "
        + "public @interface OuterAnn { InnerAnn test() default @InnerAnn(foo=\"bar\", num=5); }}",
        "A", "A.m");
    assertTranslatedLines(translation,
        "+ (id<A_InnerAnn>)testDefault {",
        "return [[[A_InnerAnn alloc] initWithFoo:@\"bar\" withNum:5] autorelease];",
        "}");
  }

  public void testAnnotationMetadata() throws IOException {
    String translation = translateSourceFile(
        "import java.lang.annotation.*; @Retention(RetentionPolicy.RUNTIME) @interface Test { "
        + " String foo() default \"bar\";"
        + " int num() default 5;"
        + "}",
        "Test", "Test.m");
    assertTranslation(translation,
        "{ \"fooDefault\", \"foo\", \"Ljava.lang.String;\", 0x100a, NULL, NULL },");
    assertTranslation(translation, "{ \"numDefault\", \"num\", \"I\", 0x100a, NULL, NULL },");
  }

  // Verify that a class with an annotation with a reserved name property is
  // created in the __annotations support method with that reserved name in the
  // constructor.
  public void testReservedWordAsAnnotationConstructorParameter() throws IOException {
    String translation = translateSourceFile(
        "package foo; import java.lang.annotation.*; @Retention(RetentionPolicy.RUNTIME) "
        + "public @interface Bar { String namespace() default \"\"; } "
        + "@Bar(namespace=\"mynames\") class Test {}",
        "Bar", "foo/Bar.m");
    assertTranslatedLines(translation, "+ (IOSObjectArray *)__annotations {",
        "return [IOSObjectArray arrayWithObjects:(id[]) "
        + "{ [[[FooBar alloc] initWithNamespace__:@\"mynames\"] autorelease] } "
        + "count:1 type:JavaLangAnnotationAnnotation_class_()];");
  }

  public void testInnerClassesMetadata() throws IOException {
    String translation = translateSourceFile(
        " class A {"
        + "class B {"
        + "  class InnerInner{}}"
        + "static class C {"
        + "  Runnable test() {"
        + "    return new Runnable() { public void run() {}};}}"
        + "interface D {}"
        + "@interface E {}"
        + "}"
        , "A", "A.m");
    assertTranslation(translation,
        "static const char *inner_classes[] = {\"LA$B;\", \"LA$C;\", \"LA$D;\", \"LA$E;\"};");
    assertTranslation(translation,
        "static const J2ObjcClassInfo _A = { 2, \"A\", NULL, NULL, 0x0, 1, methods, "
        + "0, NULL, 0, NULL, 4, inner_classes, NULL, NULL };");
  }

  public void testEnclosingMethodAndConstructor() throws IOException {
    String translation = translateSourceFile(
        "class A { A(String s) { class B {}} void test(int i, long l) { class C { class D {}}}}",
        "A", "A.m");
    assertTranslatedLines(translation,
        "static const J2ObjCEnclosingMethodInfo "
        + "enclosing_method = { \"A\", \"initWithNSString:\" };",
        "static const J2ObjcClassInfo _A_1B = { 2, \"B\", NULL, \"A\", 0x0, 1, methods, "
        + "0, NULL, 0, NULL, 0, NULL, &enclosing_method, NULL };");
    assertTranslatedLines(translation,
        "static const J2ObjCEnclosingMethodInfo "
        + "enclosing_method = { \"A\", \"testWithInt:withLong:\" };",
        "static const J2ObjcClassInfo _A_1C = { 2, \"C\", NULL, \"A\", 0x0, 1, methods, "
        + "0, NULL, 0, NULL, 1, inner_classes, &enclosing_method, NULL };");

    // Verify D is not enclosed by test(), as it's enclosed by C.
    assertTranslation(translation,
        "J2ObjcClassInfo _A_1C_D = { 2, \"D\", NULL, \"A$C\", 0x0, 1, methods, "
        + "0, NULL, 0, NULL, 0, NULL, NULL, NULL }");
  }

  public void testAnnotationsAsAnnotationValues() throws IOException {
    String translation = translateSourceFile(
        "import java.lang.annotation.*; "
        + "public class A {"
        + "@Retention(RetentionPolicy.RUNTIME) @interface Outer { Inner innerAnnotation(); }"
        + "@Retention(RetentionPolicy.RUNTIME) @interface Inner { String name(); }"
        + "@Outer(innerAnnotation=@Inner(name=\"Bar\")) class Foo {}}",
        "A", "A.m");
    assertTranslation(translation,
        "[[[A_Outer alloc] initWithInnerAnnotation:[[[A_Inner alloc] initWithName:@\"Bar\"]");
  }
}
