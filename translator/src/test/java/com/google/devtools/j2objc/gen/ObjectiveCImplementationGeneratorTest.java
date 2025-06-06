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

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.Options.MemoryManagementOption;
import java.io.IOException;
import javax.tools.ToolProvider;

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

  public void testOuterObjectVariableAccess() throws IOException {
    String translation =
        translateSourceFile(
            "public class Example { "
                + "java.util.Date foo; "
                + "class Inner { java.util.Date test() { return foo; }}}",
            "Example",
            "Example.m");
    assertTranslation(translation, "return this$0_->foo_;");
  }

  public void testOuterObjectVariableAccessStrictFieldLoad() throws IOException {
    options.setStrictFieldLoad(true);
    String translation =
        translateSourceFile(
            "public class Example { "
                + "java.util.Date foo; "
                + "class Inner { java.util.Date test() { return foo; }}}",
            "Example",
            "Example.m");
    assertTranslation(
        translation,
        "return JreStrictFieldStrongLoad(&((Example *)"
            + " JreStrictFieldStrongLoad(&this$0_))->foo_);");
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
    options.getHeaderMap().setMappingFiles("testMappings.j2objc");
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
    assertTranslation(translation, "JavaUtilDate *Example_today;");
    assertFalse(translation.contains("initialize"));
  }

  public void testStaticVariableWithInitInitialization() throws IOException {
    String translation =
        translateSourceFile(
            "public class Example { public static java.util.Date today = new java.util.Date();}",
            "Example",
            "Example.m");
    assertTranslation(translation, "JavaUtilDate *Example_today;");
    assertTranslation(translation, "+ (void)initialize {");
    assertTranslation(translation,
        "JreStrongAssignAndConsume(&Example_today, new_JavaUtilDate_init());");
  }

  public void testStaticVariableWithInitInitializationStrictFieldAssign() throws IOException {
    options.setStrictFieldAssign(true);
    String translation =
        translateSourceFile(
            "public class Example { public static java.util.Date today = new java.util.Date();}",
            "Example",
            "Example.m");
    assertTranslation(translation, "JavaUtilDate *Example_today;");
    assertTranslation(translation, "+ (void)initialize {");
    assertTranslation(
        translation, "JreStrictFieldStrongAssign(&Example_today, create_JavaUtilDate_init());");
  }

  public void testStaticVariableWithNonInitInitialization() throws IOException {
    String translation = translateSourceFile(
        "public class Example { "
        + "  public static java.util.logging.Logger logger ="
        + "      java.util.logging.Logger.getLogger(\"Test\");}",
        "Example", "Example.m");
    assertTranslation(translation,
        "JreStrongAssign(&Example_logger, "
        + "JavaUtilLoggingLogger_getLoggerWithNSString_(@\"Test\"));");
  }

  public void testStaticVariableInGenericInnerClass() throws IOException {
    String translation = translateSourceFile(
        "public class Example { "
        + "  static class Inner<T> { "
        + "    static int foo = 1; "
        + "    final int myFoo = foo++; }}",
        "Example", "Example.m");
    assertTranslation(translation, "int32_t Example_Inner_foo = 1");
    assertTranslation(translation, "myFoo_ = Example_Inner_foo++");
  }

  public void testStaticVariableWithGenericTypeCast() throws IOException {
    String translation = translateSourceFile(
        "public class Example<B> { "
        + "  public <B> Example<B> foo() { return (Example<B>) FOO; } "
        + "  public static final Example<?> FOO = new Example(); }",
        "Example", "Example.m");
    // The erasure of FOO matches the erasure of the return type of foo() so no cast necessary.
    assertTranslation(translation, "return Example_FOO");
  }

  public void testStaticVariableInOtherVariable() throws IOException {
    String translation = translateSourceFile("public class Example { "
        + "void test() { Bar.FOO=2; } } class Bar { public static int FOO=1; }",
       "Example", "Example.m");
    assertTranslation(translation, "int32_t Bar_FOO = 1;");
    assertTranslation(translation, "*JreLoadStaticRef(Bar, FOO) = 2;");
  }

  public void testNSObjectMessageRename() throws IOException {
    String translation = translateSourceFile(
        "public class Example { int load() { return 1; } int test() { return load(); }}",
        "Example", "Example.m");
    assertTranslation(translation, "- (int32_t)load__ {");
    assertTranslation(translation, "return [self load__];");
  }

  public void testNSObjectMessageQualifiedNameRename() throws IOException {
    String translation = translateSourceFile(
        "public class Example { int load() { return 1; } int test() { return this.load(); }}",
        "Example", "Example.m");
    assertTranslation(translation, "return [self load__];");
  }

  public void testNSObjectMessageSuperRename() throws IOException {
    addSourceFile("public class SuperClass { int load() { return 1; }}", "SuperClass.java");
    String translation = translateSourceFile(
        "class Example extends SuperClass { int load() { return super.load(); }}",
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
    assertTranslation(translation, "Color *Color_values_[3];");
    assertTranslation(translation, "@implementation Color");
    assertTranslation(translation, "for (int32_t i = 0; i < 3; i++) {");
    assertTranslation(translation, "Color *e = Color_values_[i];");
    assertTranslation(translation,
        "Color_initWithNSString_withInt_(e, JreEnumConstantName(Color_class_(), i), i);");

    // Check that correct enum names are in metadata.
    assertTranslation(translation, "{ \"RED\", \"LColor;\"");
    assertTranslation(translation, "{ \"WHITE\", \"LColor;\"");
    assertTranslation(translation, "{ \"BLUE\", \"LColor;\"");
  }

  public void testEnumWithParameters() throws IOException {
    String sourceContent =
        "public enum Color { RED(0xff0000), WHITE(0xffffff), BLUE(0x0000ff); "
        + "private int rgb; private int newValue;"
        + "private Color(int rgb) { this.rgb = rgb; } "
        + "public int getRgb() { return rgb; }}";
    String translation = translateSourceFile(sourceContent, "Color", "Color.m");
    assertTranslation(translation, "@implementation Color");
    assertTranslation(translation,
        "Color_initWithInt_withNSString_withInt_(e, (int32_t) 0xff0000, @\"RED\", 0);");
    assertTranslation(translation,
        "Color_initWithInt_withNSString_withInt_(e, (int32_t) 0xffffff, @\"WHITE\", 1);");
    assertTranslation(translation,
        "Color_initWithInt_withNSString_withInt_(e, (int32_t) 0x0000ff, @\"BLUE\", 2);");
    assertTranslation(translation, "- (int32_t)getRgb {");
    assertTranslation(translation, "return rgb_;");
    assertTranslation(translation, "int32_t newValue_;");
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
    assertTranslation(translation, "int32_t FooBar_fieldPhi;");
    assertTranslation(translation, "RELEASE_(fieldFoo_);");
    assertTranslation(translation, "id fieldFoo_;");
    assertTranslation(translation, "id fieldJar_;");
    assertTranslation(translation, "int32_t newFieldBar_;");
    assertTranslation(translation, "id fieldFoo_;");
    assertTranslation(translation, "WEAK_ id fieldJar_;");
    assertTranslation(translation, "int32_t newFieldBar_;");
    assertTranslation(translation, "J2OBJC_STATIC_FIELD_PRIMITIVE(FooBar, fieldPhi, int32_t)");
  }

  public void testClassFieldStrictFieldAssign() throws IOException {
    options.setStrictFieldAssign(true);
    String sourceContent =
        "import com.google.j2objc.annotations.Weak;"
            + "public class FooBar {"
            + "private static int fieldPhi;"
            + "private Object fieldFoo;"
            + "@Weak private Object fieldJar;"
            + "private int newFieldBar;"
            + "}";
    String translation = translateSourceFile(sourceContent, "FooBar", "FooBar.m");
    assertTranslation(translation, "int32_t FooBar_fieldPhi;");
    assertTranslation(translation, "JreStrictFieldStrongRelease(&fieldFoo_);");
    assertTranslation(translation, "id fieldFoo_;");
    assertTranslation(translation, "id fieldJar_;");
    assertTranslation(translation, "int32_t newFieldBar_;");
    assertTranslation(translation, "id fieldFoo_;");
    assertTranslation(translation, "WEAK_ id fieldJar_;");
    assertTranslation(translation, "int32_t newFieldBar_;");
    assertTranslation(translation, "J2OBJC_STATIC_FIELD_PRIMITIVE(FooBar, fieldPhi, int32_t)");
  }

  public void testEmptyInterfaceGenerationNoMetadata() throws IOException {
    options.setStripReflection(true);
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
    assertTranslation(translation, "id FooCompatible_FOO;");
    assertTranslation(translation,
        "JreStrongAssignAndConsume(&FooCompatible_FOO, new_NSObject_init());");
  }

  public void testAnnotationGeneration() throws IOException {
    String translation = translateSourceFile(
        "package foo; import java.lang.annotation.*; @Retention(RetentionPolicy.RUNTIME) "
        + "public @interface Compatible { boolean fooable() default false; }",
        "Compatible", "foo/Compatible.m");
    assertTranslation(translation, "@implementation FooCompatible");
    assertTranslation(translation, "@synthesize fooable = fooable_;");

    // Verify constructor generated.
    assertTranslation(translation, "id<FooCompatible> create_FooCompatible(bool fooable) {");
    assertTranslation(translation, "fooable_ = fooable;");

    // Verify default value accessor.
    assertTranslatedLines(translation, "+ (bool)fooableDefault {", "return false;");

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
    assertNotInTranslation(translation, "#include \"Bar.h\"");
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

    assertTranslation(translation, "int32_t Example_foo = 42;");
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
        "Test_Field_1_initWithTest_Type_withNSString_withInt_("
        + "e, JreLoadEnum(Test_Type, STRING), @\"STRING\", 2);");
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
    options.setMemoryManagementOption(MemoryManagementOption.ARC);
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
        "- (instancetype)initWithInt:(int32_t)i {",
        "  Test_initWithInt_(self, i);",
        "  return self;",
        "}");
    assertTranslatedLines(translation,
        "void Test_init(Test *self) {",
        "  Test_initWithInt_(self, 42);",
        "}");
    assertTranslatedLines(translation,
        "void Test_initWithInt_(Test *self, int32_t i) {",
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
        "                     withInt:(int32_t)i {",
        "  Test_Inner_initWithTest_withInt_(self, outer$, i);",
        "  return self;",
        "}");
    assertTranslatedLines(translation,
        "void Test_Inner_initWithTest_(Test_Inner *self, Test *outer$) {",
        "  Test_Inner_initWithTest_withInt_(self, outer$, 42);",
        "}");
    assertTranslatedLines(translation,
        "void Test_Inner_initWithTest_withInt_(Test_Inner *self, Test *outer$, int32_t i) {",
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
    options.enableDeprecatedDeclarations();

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
    assertNotInTranslation(translation,
        "#pragma GCC diagnostic ignored \"-Wdeprecated-declarations\"");
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
    String innerType = "@implementation Test_Inner";
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
        "return JreDefaultFastEnumeration(self, state, stackbuf);",
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

  public void testAnnotationWithField() throws IOException {
    String translation = translateSourceFile(
        "@interface Test { String FOO = \"foo\"; int I = 5; }", "Test", "Test.h");
    assertTranslation(translation, "#define Test_I 5");
    assertTranslation(translation, "FOUNDATION_EXPORT NSString *Test_FOO;");
    assertTranslation(translation, "J2OBJC_STATIC_FIELD_OBJ_FINAL(Test, FOO, NSString *)");
    assertTranslation(translation, "@interface Test : NSObject");
    translation = getTranslatedFile("Test.m");
    assertTranslation(translation, "NSString *Test_FOO = @\"foo\";");
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
    options.setDocCommentsEnabled(true);
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
    assertTranslatedLines(translation, "/*!", "@brief A package doc-comment.", "*/");
    translation = getTranslatedFile("foo/bar/mumble/package-info.m");
    assertTranslation(translation, "@implementation FooBarMumblepackage_info");
    assertTranslation(translation, "IOSObjectArray *FooBarMumblepackage_info__Annotations$0() {");
    assertTranslation(translation, "create_FooAnnotationsTest()");
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
    assertNotInTranslation(translation, "/**");
    translation = getTranslatedFile("foo/bar/mumble/package-info.m");
    assertTranslation(translation, "@implementation FooBarMumblepackage_info");
    assertTranslation(translation, "IOSObjectArray *FooBarMumblepackage_info__Annotations$0() {");
    assertTranslation(translation, "create_FooAnnotationsTest()");
  }

  public void testPackageInfoDocNoAnnotation() throws IOException {
    addSourcesToSourcepaths();
    options.setDocCommentsEnabled(true);
    String translation = translateSourceFile(
        "/** A package doc-comment. */\n"
        + "package foo.bar.mumble;",
        "package-info", "foo/bar/mumble/package-info.h");
    assertTranslatedLines(translation, "/*!", "@brief A package doc-comment.", "*/");
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
    assertTranslation(translation, "@compatibility_alias FooBarMumbleTest FBMTest;");
    translation = getTranslatedFile("foo/bar/mumble/Test.m");
    assertTranslation(translation, "@implementation FBMTest");
    assertTranslation(translation,
        "J2OBJC_NAME_MAPPING(FBMTest, \"foo.bar.mumble\", \"FBM\")");
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
    assertTranslation(translation, "@compatibility_alias FooBarMumbleTest FBMTest;");
    translation = getTranslatedFile("foo/bar/mumble/Test.m");
    assertTranslation(translation, "@implementation FBMTest");
    assertTranslation(translation,
        "J2OBJC_NAME_MAPPING(FBMTest, \"foo.bar.mumble\", \"FBM\")");
    assertNotInTranslation(translation, "FooBarMumbleTest");
  }

  public void testPackageInfoOnClasspath() throws IOException {
    addSourceFile(
        "@ObjectiveCName(\"FBM\")\n"
        + "package foo.bar.mumble;\n"
        + "import com.google.j2objc.annotations.ObjectiveCName;",
        "src/foo/bar/mumble/package-info.java");

    ToolProvider.getSystemJavaCompiler().run(
        null, null, System.err,
        tempDir.getAbsolutePath() + "/src/foo/bar/mumble/package-info.java");
    options.fileUtil().getClassPathEntries().add(tempDir.getAbsolutePath() + "/src/");
    String translation = translateSourceFile("package foo.bar.mumble;\n"
        + "public class Test {}",
        "foo.bar.mumble.Test", "foo/bar/mumble/Test.h");
    assertTranslation(translation, "@interface FBMTest");
    assertTranslation(translation, "@compatibility_alias FooBarMumbleTest FBMTest;");
    translation = getTranslatedFile("foo/bar/mumble/Test.m");
    assertTranslation(translation, "@implementation FBMTest");
    assertTranslation(translation,
        "J2OBJC_NAME_MAPPING(FBMTest, \"foo.bar.mumble\", \"FBM\")");
    assertNotInTranslation(translation, "FooBarMumbleTest");
  }

  public void testPackageInfoPrefixMethod() throws IOException {
    String translation = translateSourceFile(
        "@ObjectiveCName(\"FBM\")\npackage foo.bar.mumble;\n"
        + "import com.google.j2objc.annotations.ObjectiveCName;",
        "foo.bar.mumble.package-info", "foo/bar/mumble/package-info.m");
    assertTranslatedLines(translation,
        "+ (NSString *)__prefix {",
        "  return @\"FBM\";",
        "}");
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

  public void testReservedWordAsAnnotationPropertyName() throws IOException {
    String translation = translateSourceFile(
        "package foo; "
            + "import java.lang.annotation.*; "
            + "@Retention(RetentionPolicy.RUNTIME) "
            + "public @interface Bar { "
            + "  String in() default \"XYZ\"; "
            + "  String namespace() default \"\"; "
            + "} "
            + "class Test { Bar ann; String namespace() { return ann.namespace(); }}",
        "Bar", "foo/Bar.m");
    assertTranslation(translation, "@synthesize in = in_;");
    assertTranslation(translation, "@synthesize namespace__ = namespace___;");
    assertTranslation(translation,
        "id<FooBar> create_FooBar(NSString *inArg, NSString *namespace__) {");
    assertTranslation(translation, "self->in_ = RETAIN_(inArg);");
    assertTranslation(translation, "self->namespace___ = RETAIN_(namespace__);");
    assertTranslation(translation, "+ (NSString *)inDefault {");
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
        "+ (id<A_InnerAnn>)testDefault {", "return create_A_InnerAnn();", "}");
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
        "return create_A_InnerAnn(@\"bar\", 5);",
        "}");
  }

  public void testAnnotationsAsAnnotationValues() throws IOException {
    String translation = translateSourceFile(
        "import java.lang.annotation.*; "
        + "public class A {"
        + "@Retention(RetentionPolicy.RUNTIME) @interface Outer { Inner innerAnnotation(); }"
        + "@Retention(RetentionPolicy.RUNTIME) @interface Inner { String name(); }"
        + "@Outer(innerAnnotation=@Inner(name=\"Bar\")) class Foo {}}",
        "A", "A.m");
    assertTranslation(translation, "create_A_Outer(create_A_Inner(@\"Bar\"))");
  }

  public void testForwardDeclarationForPrivateAbstractDeclaration() throws IOException {
    // We need a forward declaration of JavaLangInteger for the type narrowing declaration of get()
    // in the private class B.
    String translation = translateSourceFile(
        "class Test { static class A <T> { T get() { return null; } }"
        + "private static class B extends A<Integer> { } }", "Test", "Test.m");
    assertTranslation(translation, "@class JavaLangInteger;");
  }

  public void testNameMappingStripped() throws IOException {
    options.setStripNameMapping(true);
    String translation = translateSourceFile(
        "@com.google.j2objc.annotations.ObjectiveCName(\"NSTest\") public class Test {}",
        "Test", "Test.m");
    assertNotInTranslation(translation, "J2OBJC_NAME_MAPPING");
  }

  public void testNameMappingNotStripped() throws IOException {
    options.setStripReflection(true);
    options.setStripNameMapping(false);
    String translation = translateSourceFile(
        "@com.google.j2objc.annotations.ObjectiveCName(\"NSTest\") public class Test {}",
        "Test", "Test.m");
    assertTranslation(translation, "J2OBJC_NAME_MAPPING(NSTest, \"Test\", \"NSTest\")");
  }

  public void testNameMappingNestedClass() throws IOException {
    String hFile =
        translateSourceFile(
            "package foo; "
                + "import com.google.j2objc.annotations.ObjectiveCName; "
                + "public class Outer { "
                + "@ObjectiveCName(\"Renamed\") public class Inner {} "
                + "}",
            "foo.Outer",
            "foo/Outer.h");
    String mFile = getTranslatedFile("foo/Outer.m");
    // Validate Outer class.
    assertTranslation(hFile, "@interface FooOuter");
    assertTranslation(mFile, "@implementation FooOuter");
    // Validate Inner class.
    assertTranslation(hFile, "@interface Renamed");
    assertTranslation(mFile, "@implementation Renamed");
    assertTranslation(
        mFile, "J2OBJC_NAME_MAPPING(Renamed, \"foo.Outer$Inner\", \"Renamed\")");
    assertNotInTranslation(mFile, "FooOuter_Inner");
    // Make sure that the only class mapping is the one for the inner class.
    assertOccurrences(mFile, "J2OBJC_NAME_MAPPING", 1);
  }

  /**
   * The following scenarios should not generate name mappings: package-info files, lambdas and
   * anonymous classes.
   */
  public void testCasesWithoutNameMapping() throws IOException {
    addSourceFile(
        "@ObjectiveCName(\"FBM\")\n"
            + "package foo.bar.mumble;\n"
            + "import com.google.j2objc.annotations.ObjectiveCName;",
        "foo/bar/mumble/package-info.java");
    String mFile =
        translateSourceFile(
            "package foo.bar.mumble; "
                + "public class Test { "
                + "  Runnable r = () -> {}; "  // lambda
                + "  Runnable s = new Runnable() { "  // anonymous
                + "    @Override public void run() {} "
                + "  }; "
                + "  public void method() { "
                + "    Runnable unused = new Runnable() {"  // local anonymous
                + "      @Override public void run() {} "
                + "    };"
                + "  } "
                + "}",
            "foo.bar.mumble.Test",
            "foo/bar/mumble/Test.m");
    assertTranslation(
        mFile, "J2OBJC_NAME_MAPPING(FBMTest, \"foo.bar.mumble\", \"FBM\")");
    assertOccurrences(mFile, "J2OBJC_NAME_MAPPING", 1);
    // The ObjectiveCName annotation affects classes in the package but not the package itself.
    String translation =
        translateSourceFile("foo.bar.mumble.package-info", "foo/bar/mumble/package-info.m");
    assertTranslation(translation, "FooBarMumblepackage_info");
    assertNotInTranslation(translation, "FBMpackage_info");
    assertNotInTranslation(translation, "J2OBJC_NAME_MAPPING");
  }

  //  Verifies that the error test and message lines are generated for ARC when
  //  using ARC as Memory Management Option
  public void testErrorTestAndMessageLinesGeneratedWithARC() throws IOException {
    options.setMemoryManagementOption(Options.MemoryManagementOption.ARC);
    String translation =
        translateSourceFile(
            "package foo.bar; public class A { public A(int i) {} A() {} }",
            "foo.bar.A",
            "foo/bar/A.m");

    assertTranslation(
        translation,
        "#if !__has_feature(objc_arc)\n"
            + "#error \"foo/bar/A must be compiled with ARC (-fobjc-arc)\"\n"
            + "#endif");
  }

  //  Verifies that the error test and message lines are generated for Reference Counting
  //  when using Reference Counting as Memory Management Option
  public void testErrorTestAndMessageLinesGeneratedWithReferenceCounting() throws IOException {
    options.setMemoryManagementOption(Options.MemoryManagementOption.REFERENCE_COUNTING);
    String translation =
        translateSourceFile(
            "package foo.bar; public class A { public A(int i) {} A() {} }",
            "foo.bar.A",
            "foo/bar/A.m");

    assertTranslation(
        translation,
        "#if __has_feature(objc_arc)\n"
            + "#error \"foo/bar/A must not be compiled with ARC (-fobjc-arc)\"\n"
            + "#endif");
  }

  //  Verifies that the error test and message lines are generated for Reference Counting
  //  when no Memory Management Option is set
  public void testErrorTestAndMessageLinesGeneratedDefaultMemoryManagement() throws IOException {
    String translation =
        translateSourceFile(
            "package foo.bar; public class A { public A(int i) {} A() {} }",
            "foo.bar.A",
            "foo/bar/A.m");

    assertTranslation(
        translation,
        "#if __has_feature(objc_arc)\n"
            + "#error \"foo/bar/A must not be compiled with ARC (-fobjc-arc)\"\n"
            + "#endif");
  }

  public void testWeakReferenceCheckIsPrinted() throws IOException {
    String translation =
        translateSourceFile(String.join("\n",
            "package foo.bar;",
    "import com.google.j2objc.annotations.Weak;",
    "public class A { @Weak A a; A() {} }"),
            "foo.bar.A",
            "foo/bar/A.m");
    assertTranslation(
        translation,
        "#if !__has_feature(objc_arc_weak)\n"
            + "#error \"foo/bar/A must be compiled with weak references support (-fobjc-weak)\"\n"
            + "#endif");
  }

  public void testStrictFieldBuildFlagsGeneration() throws IOException {
    String translation = translateSourceFile("public class Example {}", "Example", "Example.m");

    assertNotInTranslation(translation, "J2OBJC_STRICT_FIELD_ASSIGN");
    assertNotInTranslation(translation, "J2OBJC_STRICT_FIELD_LOAD");

    options.setStrictFieldAssign(true);
    translation = translateSourceFile("public class Example {}", "Example", "Example.m");

    assertTranslation(translation, "J2OBJC_STRICT_FIELD_ASSIGN 1");
    assertNotInTranslation(translation, "J2OBJC_STRICT_FIELD_LOAD");

    options.setStrictFieldLoad(true);
    translation = translateSourceFile("public class Example {}", "Example", "Example.m");

    assertTranslation(translation, "J2OBJC_STRICT_FIELD_ASSIGN 1");
    assertTranslation(translation, "J2OBJC_STRICT_FIELD_LOAD 1");

    options.setStrictFieldAssign(false);
    translation = translateSourceFile("public class Example {}", "Example", "Example.m");

    assertNotInTranslation(translation, "J2OBJC_STRICT_FIELD_ASSIGN");
    assertNotInTranslation(translation, "J2OBJC_STRICT_FIELD_LOAD");
  }

  public void testRetainedAutoreleasedReturnBuildFlagsGeneration() throws IOException {
    String translation = translateSourceFile("public class Example {}", "Example", "Example.m");

    assertNotInTranslation(translation, "J2OBJC_RETAIN_AUTORELEASE_RETURNS");
    assertNotInTranslation(translation, "J2OBJC_ARC_AUTORELEASE_RETURNS");

    options.setRetainAutoreleaseReturns(true);
    translation = translateSourceFile("public class Example {}", "Example", "Example.m");

    assertTranslation(translation, "J2OBJC_RETAIN_AUTORELEASE_RETURNS 1");
    assertNotInTranslation(translation, "J2OBJC_STRICT_FIELD_LOAD");

    options.setARCAutoreleaseReturns(true);
    translation = translateSourceFile("public class Example {}", "Example", "Example.m");

    assertTranslation(translation, "J2OBJC_RETAIN_AUTORELEASE_RETURNS 1");
    assertTranslation(translation, "J2OBJC_ARC_AUTORELEASE_RETURNS 1");

    options.setRetainAutoreleaseReturns(false);
    translation = translateSourceFile("public class Example {}", "Example", "Example.m");

    assertNotInTranslation(translation, "J2OBJC_RETAIN_AUTORELEASE_RETURNS");
    assertNotInTranslation(translation, "J2OBJC_ARC_AUTORELEASE_RETURNS");
  }
}
