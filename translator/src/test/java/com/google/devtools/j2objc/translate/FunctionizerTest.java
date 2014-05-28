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
    Options.enableFinalMethodsAsFunctions();
  }

  @Override
  protected void tearDown() throws Exception {
    Options.resetFinalMethodsAsFunctions();
    super.tearDown();
  }

  public void testPrivateInstanceMethodNoArgs() throws IOException {
    String translation = translateSourceFile(
      "class A { String test(String msg) { return str(); } " +
      "  private String str() { return toString(); }}",
      "A", "A.h");
    String functionHeader = "__attribute__ ((unused)) static NSString * A_str_(A * self_)";
    assertNotInTranslation(translation, functionHeader);
    translation = getTranslatedFile("A.m");
    assertTranslation(translation, functionHeader + ";");
    assertTranslation(translation, functionHeader + " {");
    assertTranslation(translation, "return A_str_(self);");
    assertTranslation(translation, "return [self_ description];");
  }

  // Verify one function calls another with the instance parameter.
  public void testPrivateToPrivate() throws IOException {
    String translation = translateSourceFile(
      "class A { private String test(String msg) { return str(); } " +
      "  private String str() { return toString(); }}",
      "A", "A.m");
    assertTranslation(translation, "return A_str_(self);");
    assertTranslation(translation, "return [self_ description];");
  }

  public void testPrivateInstanceMethod() throws IOException {
    String translation = translateSourceFile(
      "class A { String test(String msg) { return str(msg, getClass()); } " +
      "  private String str(String msg, Class<?> cls) { return msg + cls; }}",
      "A", "A.h");
    String functionHeader =
        "__attribute__ ((unused)) static " +
        "NSString * A_str_(A * self_, NSString * msg, IOSClass * cls)";
    assertNotInTranslation(translation, functionHeader);
    translation = getTranslatedFile("A.m");
    assertTranslation(translation, functionHeader + ";");
    assertTranslatedLines(translation, functionHeader + " {",
        "return [NSString stringWithFormat:@\"%@%@\", msg, cls];");
    assertTranslation(translation, "return A_str_(self, msg, [self getClass]);");
  }

  // Verify non-private instance method is generated normally.
  public void testNonPrivateInstanceMethod() throws IOException {
    String translation = translateSourceFile(
      "class A { String test(String msg) { return str(msg, getClass()); } " +
      "  String str(String msg, Class<?> cls) { return msg + cls; }}",
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
        "class A { String hello = \"hello\";" +
        "  String test() { return str(); } " +
        "  private String str() { return hello; }}",
        "A", "A.m");
      assertTranslatedLines(translation,
          "- (NSString *)test {",
          "return A_str_(self);");
      assertTranslatedLines(translation,
          "__attribute__ ((unused)) static NSString * A_str_(A * self_) {",
          "return self_->hello_;");
  }

  // Verify super field access in function.
  public void testSuperFieldAccessInFunction() throws IOException {
    String translation = translateSourceFile(
        "class A { String hello = \"hello\";" +
        "  static class B extends A { " +
        "  private String str() { super.hello = \"hi\"; return super.hello; }}}",
        "A", "A.m");
      assertTranslatedLines(translation,
          "__attribute__ ((unused)) static NSString * A_B_str_(A_B * self_) {",
          "A_set_hello_(self_, @\"hi\");",
          "return self_->hello_;");
  }

  // Verify super method invocation in function.
  public void testSuperMethodInvocationInFunction() throws IOException {
    String translation = translateSourceFile(
        "class A { " +
        "  private String hello() { return \"hello\"; } " +
        "  public String shout() { return \"HELLO\"; } " +
        "  static class B extends A { " +
        "  private String test1() { return super.hello(); } " +
        "  private String test2() { return super.shout(); }}}",
        "A", "A.m");
      assertTranslatedLines(translation,
          "__attribute__ ((unused)) static NSString * A_B_test1_(A_B * self_) {",
          "return A_hello_(self_);");
      assertTranslatedLines(translation,
          "__attribute__ ((unused)) static NSString * A_B_test2_(A_B * self_) {",
          "return [self_ shout];");
  }

  // Verify functions can call other functions, correctly passing the instance variable.
  // Also tests that overloaded functions work.
  public void testFunctionCallingFunction() throws IOException {
    String translation = translateSourceFile(
        "class A { String hello = \"hello\";" +
        "  String test() { return str(0); } " +
        "  private String str(int i) { return str(); }" +
        "  private String str() { return hello; } }",
        "A", "A.m");
    translation = getTranslatedFile("A.m");
    assertTranslatedLines(translation,
        "- (NSString *)test {",
        "return A_str_(self, 0);");
    assertTranslatedLines(translation,
        "__attribute__ ((unused)) static NSString * A_str_(A * self_, int i) {",
        "return A_str_2(self_);");
    assertTranslatedLines(translation,
        "__attribute__ ((unused)) static NSString * A_str_2(A * self_) {",
        "return self_->hello_;");
  }

  // Verify this expressions are changed to self parameters in functions.
  public void testThisParameters() throws IOException {
    String translation = translateSourceFile(
        "class A { private void test(java.util.List list) { list.add(this); }}",
        "A", "A.m");
    assertTranslatedLines(translation, "[((id<JavaUtilList>) nil_chk(list)) addWithId:self_];");
  }

  // Verify that a call to a private method in an outer class is converted correctly.
  public void testOuterCall() throws IOException {
    String translation = translateSourceFile(
        "class A { int outerN = 0; private int str() { return 0; }" +
        "  class B { " +
        "    private int test1() { return str(); } " +
        "    private int test2() { return A.this.str(); }" +
        "    private int test3() { return A.this.outerN; }}}",
        "A", "A.m");
    assertTranslatedLines(translation,
        "__attribute__ ((unused)) static int A_str_(A * self_) {",
        "return 0;");
    assertTranslatedLines(translation,
        "__attribute__ ((unused)) static int A_B_test1_(A_B * self_) {",
        "return A_str_(self_->this$0_);");
    assertTranslatedLines(translation,
        "__attribute__ ((unused)) static int A_B_test2_(A_B * self_) {",
        "return A_str_(self_->this$0_);");
    assertTranslatedLines(translation,
        "__attribute__ ((unused)) static int A_B_test3_(A_B * self_) {",
        "return self_->this$0_->outerN_;");
  }

  // Verify that a call to a private method in an outer class is converted correctly.
  public void testInnerOuterCall() throws IOException {
    String translation = translateSourceFile(
        "class A { private int str() { return 0; }" +
        "  class B { " +
        "    private int test() { return str(); }}" +
        "  class C { " +
        "    private void test(B b) { b.test(); }}}",
        "A", "A.m");
    assertTranslatedLines(translation,
        "__attribute__ ((unused)) static void A_C_test_(A_C * self_, A_B * b) {",
        "A_B_test_(nil_chk(b));");
  }

  // Verify annotation parameters are ignored.
  public void testAnnotationParameters() throws IOException {
    String translation = translateSourceFile("import java.lang.annotation.*; " +
    		"@Target({ElementType.METHOD}) public @interface Test {}", "Test", "Test.m");
    assertNotInTranslation(translation, "self_");
  }

  // Verify enum constants are ignored.
  public void testEnumConstants() throws IOException {
    String translation = translateSourceFile("public enum Test { A, B, C; }", "Test", "Test.m");
    assertNotInTranslation(translation, "self_");
  }

  public void testStaticMethod() throws IOException {
    String translation = translateSourceFile(
      "class A { String test(String msg) { return str(msg, getClass()); } " +
      "  private static String str(String msg, Class<?> cls) { return msg + cls; }}",
      "A", "A.h");
    String functionHeader = "NSString * A_str_(NSString * msg, IOSClass * cls)";
    assertNotInTranslation(translation, functionHeader + ';');
    translation = getTranslatedFile("A.m");
    // Check new function.
    assertTranslatedLines(translation, functionHeader + " {",
        "A_init();",
        "return [NSString stringWithFormat:@\"%@%@\", msg, cls];");
    // Check wrapper.
    assertTranslatedLines(translation,
        "+ (NSString *)strWithNSString:(NSString *)msg",
        "withIOSClass:(IOSClass *)cls {",
        "return A_str_(msg, cls);");
    // Check invocation.
    assertTranslatedLines(translation,
        "- (NSString *)testWithNSString:(NSString *)msg {",
        "return A_str_(msg, [self getClass]);");
  }

  public void testFunctionParameter() throws IOException {
    String translation = translateSourceFile(
      "class A { private String test(String msg) { return echo(str(msg)); } " +
      "  private String echo(String msg) { return msg; } " +
      "  private String str(String msg) { return msg; }}",
      "A", "A.m");
    assertTranslatedLines(translation, "A_echo_(self_, A_str_(self_, msg))");
  }

  public void testStaticVarargsMethod() throws IOException {
    String translation = translateSourceFile(
      "class A { String test(String msg) { return strchars('a', 'b', 'c'); } " +
      "  private static String strchars(char... args) { return String.valueOf(args); }}",
      "A", "A.h");
    String functionHeader = "NSString * A_strchars_(IOSCharArray * args)";
    assertNotInTranslation(translation, functionHeader + ';');
    translation = getTranslatedFile("A.m");
    assertTranslation(translation, functionHeader + " {");
    assertTranslation(translation, "return A_strchars_([" +
    		"IOSCharArray arrayWithChars:(unichar[]){ 'a', 'b', 'c' } count:3]);");
  }

  public void testAssertInFunction() throws IOException {
    String translation = translateSourceFile(
        "class A { private void test(String msg) { assert msg != null : \"null msg\"; }}",
        "A", "A.m");
    assertTranslation(translation, "NSCAssert");
    assertNotInTranslation(translation, "NSAssert");
  }

  public void testSynchronizedFunction() throws IOException {
    String translation = translateSourceFile(
      "class A { void test() { str(); } private synchronized String str() { return toString(); }}",
      "A", "A.m");
    assertTranslation(translation, "@synchronized (self_)");
    translation = translateSourceFile(
        "class A { void test() { str(); } " +
        "  private String str() { synchronized(this) { return toString(); }}}",
        "A", "A.m");
    assertTranslation(translation, "@synchronized (self_)");
    translation = translateSourceFile(
        "class A { void test() { str(); } " +
        "  private static synchronized String str() { return \"abc\"; }}",
        "A", "A.m");
    assertTranslation(translation, "@synchronized ([IOSClass classWithClass:[A class]])");
    translation = translateSourceFile(
        "class A { void test() { str(); } " +
        "  private String str() { synchronized(this.getClass()) { return \"abc\"; }}}",
        "A", "A.m");
    assertTranslation(translation, "@synchronized ([self_ getClass])");
    translation = translateSourceFile(
        "class A { void test() { str(); } " +
        "  private static String str() { synchronized(A.class) { return \"abc\"; }}}",
        "A", "A.m");
    assertTranslation(translation, "@synchronized ([IOSClass classWithClass:[A class]])");
  }

  public void testSetter() throws IOException {
    String translation = translateSourceFile(
        "class A { Object o; private void setO(Object o) { this.o = o; }}",
        "A", "A.m");
    assertTranslation(translation, "A_set_o_(self_, o)");
  }

  public void testClassInitializerCalledFromFunction() throws IOException {
    String translation = translateSourceFile(
        "class A { static Object o = new Object(); " +
        "  private static Object foo() { return o; }" +
        "  void test() { A.foo(); }" +
        "  private void test2() {}}",
        "A", "A.m");
    // Verify static class function calls class init.
    assertTranslatedLines(translation, "id A_foo_() {", "A_init();", "return A_o_;", "}");
    // Verify class method doesn't call class init.
    assertTranslatedLines(translation, "- (void)test {", "A_foo_();", "}");
    // Verify non-static class function doesn't call class init.
    assertTranslatedLines(translation,
        "__attribute__ ((unused)) static void A_test2_(A * self_) {", "}");
  }
}

