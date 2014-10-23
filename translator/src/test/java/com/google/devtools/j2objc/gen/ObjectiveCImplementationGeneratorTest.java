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

/**
 * Tests for {@link ObjectiveCImplementationGenerator}.
 *
 * @author Tom Ball
 */
public class ObjectiveCImplementationGeneratorTest extends GenerationTest {

  @Override
  protected void tearDown() throws Exception {
    Options.resetDeprecatedDeclarations();
    Options.resetDocComments();
    Options.resetMemoryManagementOption();
    Options.setStripReflection(false);
    super.tearDown();
  }

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
    assertTranslation(translation, "[Example foo];");
  }

  public void testSameClassStaticMethodPackageInvocation() throws IOException {
    String translation = translateSourceFile(
        "package unit.test; public class Example { static void foo() {} void test() { foo(); }}",
        "Example", "unit/test/Example.m");
    assertTranslation(translation, "[UnitTestExample foo];");
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
    assertTranslation(translation, "JavaUtilDate * Example_today_;");
    assertFalse(translation.contains("initialize"));
  }

  public void testStaticVariableWithInitInitialization() throws IOException {
    String translation = translateSourceFile(
        "public class Example { public static java.util.Date today = new java.util.Date();}",
        "Example", "Example.m");
    assertTranslation(translation, "JavaUtilDate * Example_today_;");
    assertTranslation(translation, "+ (void)initialize {");
    assertTranslation(translation,
        "JreStrongAssignAndConsume(&Example_today_, nil, [[JavaUtilDate alloc] init]);");
  }

  public void testStaticVariableWithNonInitInitialization() throws IOException {
    String translation = translateSourceFile(
        "public class Example { "
        + "  public static java.util.logging.Logger logger ="
        + "      java.util.logging.Logger.getLogger(\"Test\");}",
        "Example", "Example.m");
    assertTranslation(translation,
        "JreStrongAssign(&Example_logger_, nil, "
        + "[JavaUtilLoggingLogger getLoggerWithNSString:@\"Test\"]);");
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
    assertTranslation(translation, "return [Example load__];");
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
        "ColorEnum_RED = [[ColorEnum alloc] initWithNSString:@\"RED\" withInt:0];");
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
        "ColorEnum_RED = [[ColorEnum alloc] "
        + "initWithInt:(jint) 0xff0000 withNSString:@\"RED\" withInt:0];");
    assertTranslation(translation,
        "ColorEnum_WHITE = [[ColorEnum alloc] "
        + "initWithInt:(jint) 0xffffff withNSString:@\"WHITE\" withInt:1];");
    assertTranslation(translation,
        "ColorEnum_BLUE = [[ColorEnum alloc] "
        + "initWithInt:(jint) 0x0000ff withNSString:@\"BLUE\" withInt:2];");
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
    assertTranslation(translation, "FooBar_set_fieldFoo_(self, nil);");
    assertTranslation(translation, "id fieldFoo_;");
    assertTranslation(translation, "id fieldJar_;");
    assertTranslation(translation, "int newFieldBar_;");
    assertTranslation(translation, "id fieldFoo_;");
    assertTranslation(translation, "__weak id fieldJar_;");
    assertTranslation(translation, "int newFieldBar_;");
    translation = getTranslatedFile("FooBar.h");
    assertTranslation(translation, "J2OBJC_STATIC_FIELD_GETTER(FooBar, fieldPhi_, jint)");
    assertTranslation(translation, "J2OBJC_STATIC_FIELD_REF_GETTER(FooBar, fieldPhi_, jint)");
  }

  public void testEmptyInterfaceGenerationNoMetadata() throws IOException {
    Options.setStripReflection(true);
    String translation = translateSourceFile(
        "package foo; public interface Compatible {}",
        "Compatible", "foo/Compatible.m");
    assertTranslation(translation, "void FooCompatible_unused() {}");
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
        "JreStrongAssignAndConsume(&FooCompatible_FOO_, nil, [[NSObject alloc] init]);");
  }

  public void testAnnotationGeneration() throws IOException {
    String translation = translateSourceFile(
        "package foo; import java.lang.annotation.*; @Retention(RetentionPolicy.RUNTIME) "
        + "public @interface Compatible { boolean fooable() default false; }",
        "Compatible", "foo/Compatible.m");
    assertTranslation(translation, "@implementation FooCompatible");
    assertTranslation(translation, "@synthesize fooable;");

    // Verify constructor generated.
    assertTranslation(translation, "- (instancetype)initWithFooable:(jboolean)fooable_");
    assertTranslation(translation, "fooable = fooable_;");

    // Verify default value accessor.
    assertTranslatedLines(translation,
        "+ (jboolean)fooableDefault {",
        "return NO;");

    assertTranslatedLines(translation,
        "- (IOSClass *)annotationType {",
        "return [IOSClass classWithProtocol:@protocol(FooCompatible)];");
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

  public void testBadNativeCodeBlock() throws IOException {
    // Bad native code blocks should just be ignored comments.
    String translation = translateSourceFile(
        "public class Example { native void test() /* -[ ]-*/; }",
        "Example", "Example.h");

    // Instead, a native method declaration should be created.
    assertTranslation(translation, "@interface Example (NativeMethods)\n- (void)test;");
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
        "initWithTest_TypeEnum:Test_TypeEnum_get_STRING() "
        + "withNSString:@\"STRING\" withInt:2");
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
    assertTranslation(translation, "- (instancetype)initWithInt:(jint)i {");
    assertTranslation(translation, "- (instancetype)initTestWithInt:(jint)i {");
    assertTranslation(translation, "[self initTestWithInt:42]");
  }

  public void testInnerConstructorGeneratedForNonStaticInnerClass() throws IOException {
    String translation = translateSourceFile(
        "public class Test {"
        + "  class Inner {"
        + "    public Inner() { this(42); }"
        + "    public Inner(int i) {} } }",
        "Test", "Test.m");
    assertTranslation(translation, "- (instancetype)initTest_InnerWithTest:(Test *)");
    assertTranslation(translation, "[self initTest_InnerWithTest:");
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
    assertTranslation(translation, "+ (void)foo {\n"
        + "  @synchronized([IOSClass classWithClass:[Test class]]) {");
  }

  public void testNoGenMethodStubs() throws IOException {
    String translation = translateSourceFile(
        "public class Example { native void method(int i); }",
        "Example", "Example.m");

    // Verify no stub is generated by default in impl file.
    String methodHeader = "- (void)methodWithInt:(jint)i";
    assertFalse(translation.contains(methodHeader));

    // Verify method declaration is still generated.
    translation = getTranslatedFile("Example.h");
    assertTranslation(translation, methodHeader);
  }

  public void testGenMethodStubs() throws IOException {
    Options.setGenerateNativeStubs(true);
    try {
      String translation = translateSourceFile(
          "public class Example { native void method(int i); }",
          "Example", "Example.m");
      assertTranslation(translation,
          "- (void)methodWithInt:(jint)i {\n  @throw \"method method not implemented\";\n}");
      translation = getTranslatedFile("Example.h");
      assertTranslation(translation, "- (void)methodWithInt:(jint)i");
    } finally {
      Options.setGenerateNativeStubs(false);  // Restore default value.
    }
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
        + "count:1 type:[IOSClass classWithProtocol:@protocol(JavaLangAnnotationAnnotation)]];");
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
        + "count:1 type:[IOSClass classWithProtocol:@protocol(JavaLangAnnotationAnnotation)]];");
  }

  public void testConstructorAnnotationNoParameters() throws IOException {
    String translation = translateSourceFile(
        "public class Test { @Deprecated Test() {} }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "+ (IOSObjectArray *)__annotations_Test {",
        "return [IOSObjectArray arrayWithObjects:(id[]) "
        + "{ [[[JavaLangDeprecated alloc] init] autorelease] } "
        + "count:1 type:[IOSClass classWithProtocol:@protocol(JavaLangAnnotationAnnotation)]];");
  }

  public void testConstructorAnnotationWithParameter() throws IOException {
    String translation = translateSourceFile(
        "public class Test { @Deprecated Test(int i) {} }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "+ (IOSObjectArray *)__annotations_TestWithInt_ {",
        "return [IOSObjectArray arrayWithObjects:(id[]) "
        + "{ [[[JavaLangDeprecated alloc] init] autorelease] } "
        + "count:1 type:[IOSClass classWithProtocol:@protocol(JavaLangAnnotationAnnotation)]];");
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
        + "count:1 type:[IOSClass classWithProtocol:@protocol(JavaLangAnnotationAnnotation)]];");
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
        + "count:1 type:[IOSClass classWithProtocol:@protocol(JavaLangAnnotationAnnotation)]];");
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
        "abstract class Test { "
        + " Object test1() { return null; }"  // package-private
        + " private char test2() { return 'a'; }"
        + " protected void test3() { }"
        + " final long test4() { return 0L; }"
        + " synchronized boolean test5() { return false; }"
        + " String test6(String s, Object... args) { return null; }"
        + " native void test7() /*-[ exit(0); ]-*/; "
        + " abstract void test8() throws InterruptedException, Error; "
        + "}",
        "Test", "Test.m");
    assertTranslation(translation, "{ \"test1\", NULL, \"Ljava.lang.Object;\", 0x0, NULL },");
    assertTranslation(translation, "{ \"test2\", NULL, \"C\", 0x2, NULL },");
    assertTranslation(translation, "{ \"test3\", NULL, \"V\", 0x4, NULL },");
    assertTranslation(translation, "{ \"test4\", NULL, \"J\", 0x10, NULL },");
    assertTranslation(translation, "{ \"test5\", NULL, \"Z\", 0x20, NULL },");
    assertTranslation(translation, "{ \"test6WithNSString:withNSObjectArray:\", "
        + "\"test6\", \"Ljava.lang.String;\", 0x80, NULL }");
    assertTranslation(translation, "{ \"test7\", NULL, \"V\", 0x100, NULL },");
    assertTranslation(translation, "{ \"test8\", NULL, \"V\", 0x400, "
        + "\"Ljava.lang.InterruptedException;Ljava.lang.Error;\" },");
  }

  public void testAnnotationWithField() throws IOException {
    String translation = translateSourceFile(
        "@interface Test { String FOO = \"foo\"; int I = 5; }", "Test", "Test.h");
    assertTranslation(translation, "#define Test_I 5");
    assertTranslation(translation, "@interface Test : NSObject < Test >");
    assertTranslation(translation, "FOUNDATION_EXPORT NSString *Test_FOO_;");
    assertTranslation(translation, "J2OBJC_STATIC_FIELD_GETTER(Test, FOO_, NSString *)");
    translation = getTranslatedFile("Test.m");
    assertTranslation(translation, "NSString * Test_FOO_ = @\"foo\";");
  }

  public void testPackageInfoAnnotationAndDoc() throws IOException {
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
    assertTranslation(translation, "[IOSClass classWithProtocol:@protocol(JavaLangCharSequence)]");
  }
}
