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
import com.google.devtools.j2objc.Options;

import java.io.IOException;

/**
 * Tests for {@link Functionizer}.
 *
 * @author Tom Ball
 */
public class FunctionizerTest extends GenerationTest {

  @Override
  protected void setUp() throws IOException {
    super.setUp();
    Options.setFinalMethodsAsFunctions(true);
  }

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
        "return A_strWithNSString_withIOSClass_(self, msg, [self getClass]);");
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
        "return [self strWithNSString:msg withIOSClass:[self getClass]];");
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
        "A_set_hello_(self, @\"hi\");",
        "return self->hello_;");
  }

  // Verify there isn't any super method invocations in functions.
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
        "- (NSString *)test1 {",
        "return [super hello];");
    assertTranslatedLines(translation,
        "- (NSString *)test2 {",
        "return [super shout];");
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
    translation = getTranslatedFile("A.m");
    assertTranslatedLines(translation,
        "- (NSString *)test {",
        "return A_strWithInt_(self, 0);");
    assertTranslatedLines(translation,
        "NSString *A_strWithInt_(A *self, jint i) {",
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
        "int A_str(A *self) {",
        "return 0;");
    assertTranslatedLines(translation,
        "- (jint)test1 {",
        "return A_str(this$0_);");
    assertTranslatedLines(translation,
        "- (jint)test2 {",
        "return A_str(this$0_);");
    assertTranslatedLines(translation,
        "- (jint)test3 {",
        "return this$0_->outerN_;");
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
        "- (void)testWithA_B:(A_B *)b {",
        "A_B_test(nil_chk(b));");
  }

  // Verify annotation parameters are ignored.
  public void testAnnotationParameters() throws IOException {
    String translation = translateSourceFile(
        "import java.lang.annotation.*; @Target({ElementType.METHOD}) public @interface Test {}",
        "Test", "Test.m");
    assertNotInTranslation(translation, "self");
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
        "return A_strWithNSString_withIOSClass_(msg, [self getClass]);");
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
        "return A_strWithNSString_withIOSClass_(msg, [self getClass]);");
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
        + "[IOSCharArray arrayWithChars:(jchar[]){ 'a', 'b', 'c' } count:3]);");
  }

  public void testAssertInFunction() throws IOException {
    String translation = translateSourceFile(
        "class A { void use(String s) { test(s); } "
        + "  private void test(String msg) { assert msg != null : \"null msg\"; }}",
        "A", "A.m");
    assertTranslation(translation, "NSCAssert");
    assertNotInTranslation(translation, "NSAssert");
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
    assertTranslation(translation, "@synchronized([self getClass])");
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
    assertTranslation(translation, "A_set_o_(self, o)");
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
    assertTranslatedLines(translation, "id A_foo() {", "A_initialize();", "return A_o_;", "}");
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
    assertTranslatedLines(translation, "AEnum *AEnum_valueOfWithNSString_(NSString *name) {",
        "AEnum_initialize();", "for (int i = 0; i < 2; i++) {");
    // Verify static class function calls class init.
    assertTranslatedLines(translation,
        "id AEnum_foo() {", "AEnum_initialize();", "return AEnum_o_;", "}");
    // Verify class method doesn't call class init.
    assertTranslatedLines(translation, "- (void)test {", "AEnum_foo();", "}");
    // Verify non-static class function doesn't call class init.
    assertTranslatedLines(translation, "void AEnum_test2(AEnum *self) {", "}");
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
    assertTranslatedLines(translation, "- (void)bar {", "TestEnum_foo();");
    assertTranslation(translation, "static void TestEnum_foo();");
    assertTranslation(translation, "void TestEnum_foo() {");
  }

  public void testNativeMethodsWithoutOcni() throws IOException {
    String translation = translateSourceFile(
        "class Test { public native void foo(); public native static void bar(); }",
        "Test", "Test.h");

    // Public declaration for "foo" instance method, within "NativeMethods" category.
    assertTranslation(translation, "- (void)foo;");
    // Public declaration for "bar". both the class method and c-function.
    assertTranslation(translation, "+ (void)bar;");
    assertTranslation(translation, "FOUNDATION_EXPORT void Test_bar();");

    translation = getTranslatedFile("Test.m");
    // Implementation for "foo" is functionized.
    assertTranslation(translation, "void Test_foo(Test *self);");
    assertTranslatedLines(translation, "- (void)foo {", "Test_foo(self);", "}");
    // class method wrapper for "bar".
    assertTranslatedLines(translation, "+ (void)bar {", "Test_bar();", "}");
    // No implementation of the c-function for "bar".
    assertNotInTranslation(translation, "void Test_bar()");
  }

  public void testExtraSelectorsFromMultipleOverrides() throws IOException {
    addSourceFile("interface I { int foo(String t); }", "I.java");
    addSourceFile("class A<T> { int foo(T t) {} }", "A.java");
    String translation = translateSourceFile(
        "class B extends A<String> implements I { public int foo(String t) { return 7; } }",
        "B", "B.h");
    assertTranslation(translation, "- (jint)fooWithNSString:(NSString *)t;");
    assertTranslation(translation, "- (jint)fooWithId:(NSString *)t;");

    translation = getTranslatedFile("B.m");
    assertTranslatedLines(translation,
        "- (jint)fooWithNSString:(NSString *)t {",
        "  return B_fooWithNSString_(self, t);",
        "}");
    assertTranslatedLines(translation,
        "- (jint)fooWithId:(NSString *)t {",
        "  return B_fooWithNSString_(self, t);",
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

  public void testFunctionizedConstructors() throws IOException {
    String translation = translateSourceFile(
        "class Test { String s; "
        + "Test() { this(\"foo\"); } "
        + "private Test(String s) { this.s = s; } }", "Test", "Test.h");
    // Functionized constructor.
    assertTranslation(translation, "FOUNDATION_EXPORT void Test_init(Test *self);");
    // Allocating constructor.
    assertTranslation(translation, "FOUNDATION_EXPORT Test *new_Test_init() NS_RETURNS_RETAINED;");
    translation = getTranslatedFile("Test.m");
    // Declarations for the private constructor.
    assertTranslation(translation,
        "__attribute__((unused)) static void Test_initWithNSString_(Test *self, NSString *s);");
    assertTranslation(translation,
        "__attribute__((unused)) static Test *new_Test_initWithNSString_(NSString *s) "
        + "NS_RETURNS_RETAINED;");
    // Implementations.
    assertTranslatedLines(translation,
        "void Test_init(Test *self) {",
        "  Test_initWithNSString_(self, @\"foo\");",
        "}");
    assertTranslatedLines(translation,
        "Test *new_Test_init() {",
        "  Test *self = [Test alloc];",
        "  Test_init(self);",
        "  return self;",
        "}");
    assertTranslatedLines(translation,
        "void Test_initWithNSString_(Test *self, NSString *s) {",
        "  NSObject_init(self);",
        "  Test_set_s_(self, s);",
        "}");
    assertTranslatedLines(translation,
        "Test *new_Test_initWithNSString_(NSString *s) {",
        "  Test *self = [Test alloc];",
        "  Test_initWithNSString_(self, s);",
        "  return self;",
        "}");
  }

  public void testNoAllocatingConstructorsForAbstractClass() throws IOException {
    String translation = translateSourceFile("abstract class Test {}", "Test", "Test.h");
    assertTranslation(translation, "FOUNDATION_EXPORT void Test_init(Test *self);");
    assertNotInTranslation(translation, "new_Test_init");
    translation = getTranslatedFile("Test.m");
    assertNotInTranslation(translation, "new_Test_init");
  }
}
