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
 * Tests for {@link Functionizer}.
 *
 * @author Tom Ball
 */
public class FunctionizerTest extends GenerationTest {

  public void testPrivateInstanceMethodNoArgs() throws IOException {
    String translation = translateSourceFile(
        "class A { String test(String msg) { return str(); } "
        + "  private String str() { return toString(); }}",
        "A", "A.h");
    String functionHeader = "NSString *A_str(A *self)";
    assertNotInTranslation(translation, functionHeader);
    translation = getTranslatedFile("A.m");
    assertTranslation(translation, "static " + functionHeader + ";");
    assertTranslation(translation, functionHeader + " {");
    assertTranslation(translation, "return A_str(self);");
    assertTranslation(translation, "return [self description];");
  }

  // Verify one function calls another with the instance parameter.
  public void testPrivateToPrivate() throws IOException {
    String translation = translateSourceFile(
        "class A { private String test(String msg) { return str(); } "
        + "  private String str() { return toString(); }}",
        "A", "A.m");
    assertTranslation(translation, "return A_str(self);");
    assertTranslation(translation, "return [self description];");
  }

  public void testPrivateInstanceMethod() throws IOException {
    String translation = translateSourceFile(
        "class A { String test(String msg) { return str(msg, getClass()); } "
        + "  private String str(String msg, Class<?> cls) { return msg + cls; }}",
        "A", "A.h");
    String functionHeader =
        "NSString *A_strWithNSString_withIOSClass_(A *self, NSString *msg, IOSClass *cls)";
    assertNotInTranslation(translation, functionHeader);
    translation = getTranslatedFile("A.m");
    assertTranslation(translation, "static " + functionHeader + ";");
    assertTranslatedLines(translation, functionHeader + " {",
        "return JreStrcat(\"$@\", msg, cls);");
    assertTranslation(translation,
        "return A_strWithNSString_withIOSClass_(self, msg, [self java_getClass]);");
  }

  // Verify non-private instance method is generated normally.
  public void testNonPrivateInstanceMethod() throws IOException {
    String translation = translateSourceFile(
        "class A { String test(String msg) { return str(msg, getClass()); } "
        + "  String str(String msg, Class<?> cls) { return msg + cls; }}",
        "A", "A.m");
    assertTranslatedLines(translation,
        "- (NSString *)strWithNSString:(NSString *)msg",
        "withIOSClass:(IOSClass *)cls {");
    assertTranslation(translation,
        "return [self strWithNSString:msg withIOSClass:[self java_getClass]];");
  }

  // Verify instance field access in function.
  public void testFieldAccessInFunction() throws IOException {
    String translation = translateSourceFile(
        "class A { String hello = \"hello\";"
        + "  String test() { return str(); } "
        + "  private String str() { return hello; }}",
        "A", "A.m");
    assertTranslatedLines(translation,
        "- (NSString *)test {",
        "return A_str(self);");
    assertTranslatedLines(translation,
        "NSString *A_str(A *self) {",
        "return self->hello_;");
  }

  // Verify super field access in function.
  public void testSuperFieldAccessInFunction() throws IOException {
    String translation = translateSourceFile(
        "class A { String hello = \"hello\";"
        + "  static class B extends A { void use() { str(); }"
        + "  private String str() { super.hello = \"hi\"; return super.hello; }}}",
        "A", "A.m");
    assertTranslatedLines(translation,
        "NSString *A_B_str(A_B *self) {",
        "JreStrongAssign(&self->hello_, @\"hi\");",
        "return self->hello_;");
  }

  // Verify a method with super invocations can be funcitonized if those super invocations can also
  // be functionized.
  public void testSuperMethodInvocationInFunction() throws IOException {
    String translation = translateSourceFile(
        "class A { "
        + "  private String hello() { return \"hello\"; } "
        + "  public String shout() { return \"HELLO\"; } "
        + "  void use() { hello(); } "
        + "  static class B extends A { "
        + "  private String test1() { return super.hello(); } "
        + "  private String test2() { return super.shout(); }"
        + "  void use() { test1(); test2(); }}}",
        "A", "A.m");
    assertTranslatedLines(translation,
        "NSString *A_B_test1(A_B *self) {",
        "return A_hello(self);");
    assertTranslatedLines(translation,
        "NSString *A_B_test2(A_B *self) {",
        "return A_shout(self);");
  }

  // Verify functions can call other functions, correctly passing the instance variable.
  // Also tests that overloaded functions work.
  public void testFunctionCallingFunction() throws IOException {
    String translation = translateSourceFile(
        "class A { String hello = \"hello\";"
        + "  String test() { return str(0); } "
        + "  private String str(int i) { return str(); }"
        + "  private String str() { return hello; } }",
        "A", "A.m");
    assertTranslatedLines(translation,
        "- (NSString *)test {",
        "return A_strWithInt_(self, 0);");
    assertTranslatedLines(translation,
        "NSString *A_strWithInt_(A *self, int32_t i) {",
        "return A_str(self);");
    assertTranslatedLines(translation,
        "NSString *A_str(A *self) {",
        "return self->hello_;");
  }

  // Verify this expressions are changed to self parameters in functions.
  public void testThisParameters() throws IOException {
    String translation = translateSourceFile(
        "class A { private void test(java.util.List list) { list.add(this); }}",
        "A", "A.m");
    assertTranslatedLines(translation, "[((id<JavaUtilList>) nil_chk(list)) addWithId:self];");
  }

  // Verify that a call to a private method in an outer class is converted correctly.
  public void testOuterCall() throws IOException {
    String translation = translateSourceFile(
        "class A { int outerN = str(); private int str() { return 0; }"
        + "  class B { "
        + "    private int test1() { return str(); } "
        + "    private int test2() { return A.this.str(); }"
        + "    private int test3() { return A.this.outerN; }}}",
        "A", "A.m");
    assertTranslatedLines(translation,
        "int32_t A_str(A *self) {",
        "return 0;");
    assertTranslatedLines(translation,
        "int32_t A_B_test1(A_B *self) {",
        "return A_str(self->this$0_);");
    assertTranslatedLines(translation,
        "int32_t A_B_test2(A_B *self) {",
        "return A_str(self->this$0_);");
    assertTranslatedLines(translation,
        "int32_t A_B_test3(A_B *self) {",
        "return self->this$0_->outerN_;");
  }

  // Verify that a call to a private method in an outer class is converted correctly.
  public void testInnerOuterCall() throws IOException {
    String translation = translateSourceFile(
        "class A { private int str() { return 0; }"
        + "  class B { "
        + "    private int test() { return str(); }}"
        + "  class C { "
        + "    private void test(B b) { b.test(); }}}",
        "A", "A.m");
    assertTranslatedLines(translation,
        "void A_C_testWithA_B_(A_C *self, A_B *b) {",
        "A_B_test(nil_chk(b));");
  }

  // Verify function declaration is in .m file, not the header.
  public void testPrivateStaticMethod() throws IOException {
    String translation = translateSourceFile(
        "class A { String test(String msg) { return str(msg, getClass()); } "
        + "  private static String str(String msg, Class<?> cls) { return msg + cls; }}",
        "A", "A.h");
    String functionHeader =
        "NSString *A_strWithNSString_withIOSClass_(NSString *msg, IOSClass *cls)";
    assertNotInTranslation(translation, functionHeader + ';');
    translation = getTranslatedFile("A.m");
    // Check new function.
    assertTranslatedLines(translation, functionHeader + " {",
        "A_initialize();",
        "return JreStrcat(\"$@\", msg, cls);");
    // Check wrapper.
    assertTranslatedLines(translation,
        "+ (NSString *)strWithNSString:(NSString *)msg",
        "withIOSClass:(IOSClass *)cls {",
        "return A_strWithNSString_withIOSClass_(msg, cls);");
    // Check invocation.
    assertTranslatedLines(translation,
        "- (NSString *)testWithNSString:(NSString *)msg {",
        "return A_strWithNSString_withIOSClass_(msg, [self java_getClass]);");
  }

  // Verify function declaration is in the header.
  public void testStaticMethod() throws IOException {
    String translation = translateSourceFile(
        "class A { String test(String msg) { return str(msg, getClass()); } "
        + "  static String str(String msg, Class<?> cls) { return msg + cls; }}",
        "A", "A.h");
    String functionHeader =
        "NSString *A_strWithNSString_withIOSClass_(NSString *msg, IOSClass *cls)";
    assertTranslation(translation, functionHeader + ';');
    translation = getTranslatedFile("A.m");
    // Check new function.
    assertTranslatedLines(translation, functionHeader + " {",
        "A_initialize();",
        "return JreStrcat(\"$@\", msg, cls);");
    // Check wrapper.
    assertTranslatedLines(translation,
        "+ (NSString *)strWithNSString:(NSString *)msg",
        "withIOSClass:(IOSClass *)cls {",
        "return A_strWithNSString_withIOSClass_(msg, cls);");
    // Check invocation.
    assertTranslatedLines(translation,
        "- (NSString *)testWithNSString:(NSString *)msg {",
        "return A_strWithNSString_withIOSClass_(msg, [self java_getClass]);");
  }

  public void testFunctionParameter() throws IOException {
    String translation = translateSourceFile(
        "class A { private String test(String msg) { return echo(str(msg)); } "
        + "  private String echo(String msg) { return msg; } "
        + "  private String str(String msg) { return msg; }}",
        "A", "A.m");
    assertTranslatedLines(translation, "A_echoWithNSString_(self, A_strWithNSString_(self, msg))");
  }

  public void testStaticVarargsMethod() throws IOException {
    String translation = translateSourceFile(
        "class A { String test(String msg) { return strchars('a', 'b', 'c'); } "
        + "  private static String strchars(char... args) { return String.valueOf(args); }}",
        "A", "A.h");
    String functionHeader = "NSString *A_strcharsWithCharArray_(IOSCharArray *args)";
    assertNotInTranslation(translation, functionHeader + ';');
    translation = getTranslatedFile("A.m");
    assertTranslation(translation, functionHeader + " {");
    assertTranslation(translation, "return A_strcharsWithCharArray_("
        + "[IOSCharArray arrayWithChars:(unichar[]){ 'a', 'b', 'c' } count:3]);");
  }

  public void testSynchronizedFunction() throws IOException {
    String translation = translateSourceFile(
        "class A { void test() { str(); } "
        + "private synchronized String str() { return toString(); }}",
        "A", "A.m");
    assertTranslation(translation, "@synchronized(self)");
    assertOccurrences(translation, "@synchronized", 1);
    translation = translateSourceFile(
        "class A { void test() { str(); } "
        + "  private String str() { synchronized(this) { return toString(); }}}",
        "A", "A.m");
    assertTranslation(translation, "@synchronized(self)");
    translation = translateSourceFile(
        "class A { void test() { str(); } "
        + "  private static synchronized String str() { return \"abc\"; }}",
        "A", "A.m");
    assertTranslation(translation, "@synchronized(A_class_())");
    assertOccurrences(translation, "@synchronized", 1);
    translation = translateSourceFile(
        "class A { void test() { str(); } "
        + "  private String str() { synchronized(this.getClass()) { return \"abc\"; }}}",
        "A", "A.m");
    assertTranslation(translation, "@synchronized([self java_getClass])");
    translation = translateSourceFile(
        "class A { void test() { str(); } "
        + "  private static String str() { synchronized(A.class) { return \"abc\"; }}}",
        "A", "A.m");
    assertTranslation(translation, "@synchronized(A_class_())");
  }

  public void testSetter() throws IOException {
    String translation = translateSourceFile(
        "class A { Object o; private void setO(Object o) { this.o = o; }}",
        "A", "A.m");
    assertTranslation(translation, "JreStrongAssign(&self->o_, o)");
  }

  public void testClassInitializerCalledFromFunction() throws IOException {
    String translation = translateSourceFile(
        "class A { static Object o = new Object(); "
        + "  private static Object foo() { return o; }"
        + "  void test() { A.foo(); }"
        + "  private void test2() {}"
        + "  void use() { test2(); }}",
        "A", "A.m");
    // Verify static class function calls class init.
    assertTranslatedLines(translation, "id A_foo() {", "A_initialize();", "return A_o;", "}");
    // Verify class method doesn't call class init.
    assertTranslatedLines(translation, "- (void)test {", "A_foo();", "}");
    // Verify non-static class function doesn't call class init.
    assertTranslatedLines(translation, "void A_test2(A *self) {", "}");
  }

  public void testClassInitializerCalledFromEnumFunctions() throws IOException {
    String translation = translateSourceFile(
        "enum A { A, B; static Object o = new Object(); "
        + "  private static Object foo() { return o; }"
        + "  void test() { A.foo(); }"
        + "  private void test2() {}"
        + "  void use() { test2(); }}",
        "A", "A.m");
    // Verify valueOf function calls class init.
    assertTranslatedLines(translation, "A *A_valueOfWithNSString_(NSString *name) {",
        "A_initialize();", "for (int i = 0; i < 2; i++) {");
    // Verify static class function calls class init.
    assertTranslatedLines(translation,
        "id A_foo() {", "A_initialize();", "return A_o;", "}");
    // Verify class method doesn't call class init.
    assertTranslatedLines(translation, "- (void)test {", "A_foo();", "}");
    // Verify non-static class function doesn't call class init.
    assertTranslatedLines(translation, "void A_test2(A *self) {", "}");
  }

  public void testPrivateNativeMethod() throws IOException {
    String translation = translateSourceFile(
        "class A { Object o; void use() { setO(null); } "
        + "  private native void setO(Object o) /*-[ self->o_ = o; ]-*/; }",
        "A", "A.m");
    assertTranslation(translation, "static void A_setOWithId_(A *self, id o);");
    assertTranslatedLines(translation, "void A_setOWithId_(A *self, id o) {", "self->o_ = o;", "}");
    assertTranslatedLines(translation,
        "- (void)setOWithId:(id)o {", "A_setOWithId_(self, o);", "}");
  }

  public void testGenericMethod() throws IOException {
    String translation = translateSourceFile(
        "class Test { private static <T> void foo(T t) {} static void bar() { foo(\"test\"); } }",
        "Test", "Test.m");
    assertTranslation(translation, "Test_fooWithId_(@\"test\");");
  }

  public void testProtectedMethodInPrivateClass() throws IOException {
    String translation = translateSourceFile(
        "class Test { private static class A { protected void foo() {} void bar() { foo(); } } "
        + "private static class B extends A { protected void foo() {} } }",
        "Test", "Test.m");
    assertNotInTranslation(translation, "Test_A_foo");
  }

  public void testPrivateMethodCalledFromAnonymousEnum() throws IOException {
    String translation = translateSourceFile(
        "enum Test { A { void bar() { foo(); } }; private static void foo() {} }",
        "Test", "Test.m");
    assertTranslatedLines(translation, "- (void)bar {", "Test_foo();");
    assertTranslation(translation, "static void Test_foo(void);");
    assertTranslation(translation, "void Test_foo() {");
  }

  public void testNativeMethodsWithoutOcni() throws IOException {
    String translation = translateSourceFile(
        "class Test { public native void foo(); public native static void bar(); }",
        "Test", "Test.h");

    // Public declaration for "foo" instance method, within "NativeMethods" category.
    assertTranslation(translation, "- (void)foo;");
    // Public declaration for "bar". both the class method and c-function.
    assertTranslation(translation, "+ (void)bar;");
    assertTranslation(translation, "FOUNDATION_EXPORT void Test_bar(void);");

    translation = getTranslatedFile("Test.m");
    // Implementation for "foo" is functionized.
    assertTranslation(translation, "void Test_foo(Test *self);");
    assertTranslatedLines(translation, "- (void)foo {", "Test_foo(self);", "}");
    // class method wrapper for "bar".
    assertTranslatedLines(translation, "+ (void)bar {", "Test_bar();", "}");
    // JNI external function declarations
    assertTranslation(translation, "JNIEXPORT void Java_Test_foo(JNIEnv *_env_, jobject self);");
    assertTranslation(translation, "JNIEXPORT void Java_Test_bar(JNIEnv *_env_, jclass _cls_);");
    // JNI wrapper functions
    assertTranslatedLines(translation,
        "void Test_foo(Test *self) {", "Java_Test_foo(&J2ObjC_JNIEnv, self);", "}");
    assertTranslatedLines(translation,
        "void Test_bar() {", "Java_Test_bar(&J2ObjC_JNIEnv, Test_class_());", "}");
  }

  public void testOverloadedNativeMethodsWithoutOcni() throws IOException {
    String translation = translateSourceFile(
        "class Test { public native void foo(int i); public native static void foo(String s); }",
        "Test", "Test.h");

    // Public declaration for "foo" instance method, within "NativeMethods" category.
    assertTranslation(translation, "- (void)fooWithInt:(int32_t)i;");
    // Public declaration for "bar". both the class method and c-function.
    assertTranslation(translation, "+ (void)fooWithNSString:(NSString *)s;");
    assertTranslation(translation, "FOUNDATION_EXPORT void Test_fooWithNSString_(NSString *s);");

    translation = getTranslatedFile("Test.m");
    // Implementation for "foo" is functionized.
    assertTranslation(translation, "void Test_fooWithInt_(Test *self, int32_t i);");
    assertTranslatedLines(translation,
        "- (void)fooWithInt:(int32_t)i {", "Test_fooWithInt_(self, i);", "}");
    // class method wrapper for "bar".
    assertTranslatedLines(translation,
        "+ (void)fooWithNSString:(NSString *)s {", "Test_fooWithNSString_(s);", "}");
    // JNI external function declarations
    assertTranslation(translation,
        "JNIEXPORT void Java_Test_foo__I(JNIEnv *_env_, jobject self, int32_t i);");
    assertTranslation(translation,
        "JNIEXPORT void Java_Test_foo__Ljava_lang_String_2("
        + "JNIEnv *_env_, jclass _cls_, jstring s);");
    // JNI wrapper functions
    assertTranslatedLines(translation,
        "void Test_fooWithInt_(Test *self, int32_t i) {",
        "Java_Test_foo__I(&J2ObjC_JNIEnv, self, i);",
        "}");
    assertTranslatedLines(translation,
        "void Test_fooWithNSString_(NSString *s) {",
        "Java_Test_foo__Ljava_lang_String_2(&J2ObjC_JNIEnv, Test_class_(), s);",
        "}");
  }

  // Verify that static methods called via a super invokation are correctly
  // functionized.
  public void testStaticSuperInvocation() throws IOException {
    String translation = translateSourceFile(
        "public class A { static class Base { static void test() {} } "
        + "static class Foo extends Base { void test2() { super.test(); } }}", "A", "A.m");
    assertTranslatedLines(translation,
        "- (void)test2 {",
        "  A_Base_test();",
        "}");
  }

  public void testSuperInvocationFromConstructor() throws IOException {
    String translation = translateSourceFile(
        "class Test { Test() { super.toString(); } }", "Test", "Test.m");
    assertTranslation(translation, "Test_super$_description(self, @selector(description));");
  }

  public String innerTestFunctionizedConstructors() throws IOException {
    String translation = translateSourceFile(
        "class Test { int i; "
        + "Test() { this(0); } "
        + "private Test(int i) { this.i = i; } }", "Test", "Test.h");
    // Functionized constructor.
    assertTranslation(translation, "FOUNDATION_EXPORT void Test_init(Test *self);");
    // Retaining allocating constructor.
    assertTranslation(translation,
        "FOUNDATION_EXPORT Test *new_Test_init(void) NS_RETURNS_RETAINED;");
    // Releasing allocating constructor.
    assertTranslation(translation, "FOUNDATION_EXPORT Test *create_Test_init(void);");
    translation = getTranslatedFile("Test.m");
    // Declarations for the private constructor.
    assertTranslation(translation,
        "__attribute__((unused)) static void Test_initWithInt_(Test *self, int32_t i);");
    assertTranslation(translation,
        "__attribute__((unused)) static Test *new_Test_initWithInt_(int32_t i) NS_RETURNS_RETAINED;");
    assertTranslation(translation,
        "__attribute__((unused)) static Test *create_Test_initWithInt_(int32_t i);");
    // Implementations.
    assertTranslatedLines(translation,
        "void Test_init(Test *self) {",
        "  Test_initWithInt_(self, 0);",
        "}");
    assertTranslatedLines(translation,
        "Test *new_Test_init() {",
        "  J2OBJC_NEW_IMPL(Test, init)",
        "}");
    assertTranslatedLines(translation,
        "void Test_initWithInt_(Test *self, int32_t i) {",
        "  NSObject_init(self);",
        "  self->i_ = i;",
        "}");
    assertTranslatedLines(translation,
        "Test *new_Test_initWithInt_(int32_t i) {",
        "  J2OBJC_NEW_IMPL(Test, initWithInt_, i)",
        "}");
    assertTranslatedLines(translation,
        "Test *create_Test_init() {",
        "  J2OBJC_CREATE_IMPL(Test, init)",
        "}");
    assertTranslatedLines(translation,
        "Test *create_Test_initWithInt_(int32_t i) {",
        "  J2OBJC_CREATE_IMPL(Test, initWithInt_, i)",
        "}");
    return translation;
  }

  public void testFunctionizedConstructors() throws IOException {
    innerTestFunctionizedConstructors();
  }

  public void testFunctionizedConstructorsARC() throws IOException {
    options.setMemoryManagementOption(MemoryManagementOption.ARC);
    innerTestFunctionizedConstructors();
  }

  public void testFunctionizedConstructorWithNullMarked() throws IOException {
    addSourceFile(
        "@NullMarked" + "package foo.bar;" + "import org.jspecify.annotations.NullMarked;",
        "foo/bar/package-info.java");
    String source =
        "package foo.bar; import javax.annotation.*;"
            + "public class Test {"
            + "  @Nullable private final String nullableStr;"
            + "  private final String nonNullStr;"
            + "  Test(@Nullable String nullableStr, String nonNullStr) {"
            + "    this.nullableStr = nullableStr; this.nonNullStr = nonNullStr;"
            + "   }"
            + "}";
    options.setNullMarked(true);
    options.setNullability(true);
    String translation = translateSourceFile(source, "foo.bar.Test", "foo/bar/Test.h");
    assertTranslatedLines(
        translation, "NS_ASSUME_NONNULL_BEGIN", "@interface FooBarTest : NSObject");
    assertTranslation(
        translation,
        "FOUNDATION_EXPORT void FooBarTest_initWithNSString_withNSString_(FooBarTest *self,"
            + " NSString *_Nullable nullableStr, NSString *nonNullStr);");
    assertTranslation(
        translation,
        "FOUNDATION_EXPORT FooBarTest *new_FooBarTest_initWithNSString_withNSString_(NSString"
            + " *_Nullable nullableStr, NSString *nonNullStr) NS_RETURNS_RETAINED;");
    assertTranslation(
        translation,
        "FOUNDATION_EXPORT FooBarTest *create_FooBarTest_initWithNSString_withNSString_(NSString"
            + " *_Nullable nullableStr, NSString *nonNullStr);");
    assertTranslation(translation, "NS_ASSUME_NONNULL_END");
  }

  public void testNoAllocatingConstructorsForAbstractClass() throws IOException {
    String translation = translateSourceFile("abstract class Test {}", "Test", "Test.h");
    assertTranslation(translation, "FOUNDATION_EXPORT void Test_init(Test *self);");
    assertNotInTranslation(translation, "new_Test_init");
    assertNotInTranslation(translation, "create_Test_init");
    translation = getTranslatedFile("Test.m");
    assertNotInTranslation(translation, "new_Test_init");
    assertNotInTranslation(translation, "create_Test_init");
  }

  public void testWrapperAndReflectionStripping() throws IOException {
    addSourceFile(
        "class Test { public Test() {} public static void foo() {} private static void bar() {} }",
        "Test.java");
    String initSig = "- (instancetype)init";
    String initDisallowedSig = "- (instancetype)init NS_UNAVAILABLE";
    String fooSig = "+ (void)foo";
    String barSig = "+ (void)bar";

    // No reflection or wrapper stripping.
    String header = translateSourceFile("Test", "Test.h");
    String source = getTranslatedFile("Test.m");
    assertTranslation(header, initSig);
    assertTranslation(header, fooSig);
    assertNotInTranslation(header, barSig);
    assertTranslation(source, initSig);
    assertTranslation(source, fooSig);
    assertTranslation(source, barSig);

    // Reflection stripped: Private static method wrapper is removed.
    options.setStripReflection(true);
    header = translateSourceFile("Test", "Test.h");
    source = getTranslatedFile("Test.m");
    assertTranslation(header, initSig);
    assertTranslation(header, fooSig);
    assertNotInTranslation(header, barSig);
    assertTranslation(source, initSig);
    assertTranslation(source, fooSig);
    assertNotInTranslation(source, barSig);

    // Reflection not stripped, wrapper methods stripped: no declarations in the header.
    options.setStripReflection(false);
    options.setEmitWrapperMethods(false);
    header = translateSourceFile("Test", "Test.h");
    source = getTranslatedFile("Test.m");
    assertTranslation(header, initDisallowedSig);
    assertNotInTranslation(header, fooSig);
    assertNotInTranslation(header, barSig);
    assertTranslation(source, initSig);
    assertTranslation(source, fooSig);
    assertTranslation(source, barSig);

    // Both reflection and wrapper methods stripped: no declarations or implementations.
    options.setStripReflection(true);
    header = translateSourceFile("Test", "Test.h");
    source = getTranslatedFile("Test.m");
    assertTranslation(header, initDisallowedSig);
    assertNotInTranslation(header, fooSig);
    assertNotInTranslation(header, barSig);
    assertNotInTranslation(source, initSig);
    assertNotInTranslation(source, fooSig);
    assertNotInTranslation(source, barSig);
  }

  public void testDisallowedConstructorsWithNoWrapperMethods() throws IOException {
    options.setDisallowInheritedConstructors(true);
    options.setEmitWrapperMethods(false);
    String translation = translateSourceFile("class A {}", "A", "A.h");
    assertTranslation(translation, "- (instancetype)init NS_UNAVAILABLE;");
  }

  public void testDisallowedConstructorInAbstractClass() throws IOException {
    String translation = translateSourceFile(
        "public abstract class A { public A(String unused) {} }", "A", "A.h");
    // init is inherited from NSObject.
    assertTranslation(translation, "- (instancetype)init NS_UNAVAILABLE;");
  }

  // Even when the private method contains a super invocation, it must be functionized. (b/63163887)
  public void testPrivateInstanceMethodIsFunctionized() throws IOException {
    String translation = translateSourceFile(
        "class Test { private void foo() { super.toString(); } }", "Test", "Test.m");
    assertTranslatedLines(translation,
         "void Test_foo(Test *self) {",
         "  Test_super$_description(self, @selector(description));",
         "}");
  }

  public void testDefaultMethod() throws IOException {
    String translation = translateSourceFile(
        "interface A { default String test(String msg) { return msg.toUpperCase(); }}",
        "A", "A.h");
    // Protocol method declaration.
    assertTranslation(translation, "- (NSString *)testWithNSString:(NSString *)msg;");
    // Default method's function declaration.
    assertTranslation(translation, "NSString *A_testWithNSString_(id<A> self, NSString *msg);");

    translation = getTranslatedFile("A.m");
    // Check default method function.
    assertTranslatedLines(translation,
        "NSString *A_testWithNSString_(id<A> self, NSString *msg) {",
        "A_initialize();",  // Issue 1009: this initialize call was missing.
        "return [((NSString *) nil_chk(msg)) uppercaseString];");
  }

  public void testEnumValuesMethodIsNotRemoved() throws IOException {
    // Preconditions:
    // 1) Reflection stripped and no wrapper methods.
    // 2) Private enum.
    // 3) No explicit reference to values method.
    options.setStripReflection(true);
    options.setEmitWrapperMethods(false);
    String translation = translateSourceFile(
        "public class Test { "
            + "  private enum AnEnum { "
            + "    A, B, C; "
            + "  } "
            + "  public String test() { "
            + "    return AnEnum.B.toString(); "
            + "  } "
            + "} ",
        "Test", "Test.m");
    assertTranslation(translation, "+ (IOSObjectArray *)values {");
  }
}
