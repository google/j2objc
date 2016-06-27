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
package com.google.devtools.j2objc.ast;

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.util.SourceVersion;

import java.io.IOException;

/**
 * Unit tests for {@link MethodReference}.
 *
 * @author Seth Kirby
 */
public class MethodReferenceTest extends GenerationTest {
  @Override
  protected void loadOptions() throws IOException {
    super.loadOptions();
    Options.setSourceVersion(SourceVersion.JAVA_8);
  }

  // Test the creation of explicit blocks for lambdas with expression bodies.
  public void testCreationReferenceBlockWrapper() throws IOException {
    String creationReferenceHeader =
        "class I { I() { } I(int x) { } I(int x, I j, String s, Object o) { } }\n"
        + "interface FunInt<T> { T apply(int x); }"
        + "interface FunInt4<T> { T apply(int x, I j, String s, Object o); }"
        + "interface Call<T> { T call(); }";

    String noArgumentTranslation = translateSourceFile(
        creationReferenceHeader + "class Test { Call<I> iInit = I::new; }",
        "Test", "Test.m");
    assertTranslatedSegments(noArgumentTranslation,
        "I *Test$$Lambda$1_impl(LambdaBase *self_, SEL _cmd) {",
        "return create_I_init();",
        "instance = CreateNonCapturing(",
        "\"Test$$Lambda$1\""
        );
    String oneArgumentTranslation = translateSourceFile(
        creationReferenceHeader + "class Test { FunInt<I> iInit2 = I::new; }", "Test", "Test.m");
    assertTranslatedSegments(oneArgumentTranslation,
        "I *Test$$Lambda$1_impl(LambdaBase *self_, SEL _cmd, jint a) {",
        "return create_I_initWithInt_(a);",
        "instance = CreateNonCapturing(",
        "\"Test$$Lambda$1\""
        );
    String mixedArgumentTranslation = translateSourceFile(
        creationReferenceHeader + "class Test { FunInt4<I> iInit3 = I::new; }", "Test", "Test.m");
    assertTranslatedSegments(mixedArgumentTranslation,
        "I *Test$$Lambda$1_impl(LambdaBase *self_, SEL _cmd, jint a, I *b, NSString *c, id d) {",
        "return create_I_initWithInt_withI_withNSString_withId_(a, b, c, d);",
        "instance = CreateNonCapturing(",
        "\"Test$$Lambda$1\""
        );
  }

  // Test that expression method references resolve correctly for static and non-static methods.
  public void testExpressionReferenceStaticResolution() throws IOException {
    String expressionReferenceHeader = "class Q { static Object o(Object x) { return x; }\n"
        + "    Object o2(Object x) { return x; }}" + "interface F<T, R> { R f(T t); }";
    String staticTranslation = translateSourceFile(
        expressionReferenceHeader + "class Test { F fun = Q::o; }",
        "Test", "Test.m");
    assertTranslatedSegments(staticTranslation, "return Q_oWithId_(a);", "CreateNonCapturing",
        "Test$$Lambda$1", "@selector(fWithId:)");
    String instanceTranslation = translateSourceFile(
        expressionReferenceHeader + "class Test { F fun = new Q()::o2; }",
        "Test", "Test.m");
    assertTranslatedSegments(instanceTranslation, "return [__emt$0 o2WithId:a];",
        "CreatePossiblyCapturingClass", "Test$$Lambda$1", "@selector(fWithId:)");
    assertNotInTranslation(instanceTranslation, "return [create_Q_init() o2WithId:a];");
    String staticInstanceTranslation = translateSourceFile(
        expressionReferenceHeader + "class Test { static F fun = new Q()::o2; }",
        "Test", "Test.m");
    assertTranslatedSegments(staticInstanceTranslation, "+ (void)initialize {",
        "self == [Test class])", "Q *__emt$0;", "J2OBJC_SET_INITIALIZED(Test)");
    assertNotInTranslation(staticInstanceTranslation, "return [create_Q_init() o2WithId:a];");
  }

  public void testTypeReference() throws IOException {
    // TODO(kirbs): Find and test more examples of type method references. Most of the examples that
    // Eclipse is using are coming from special compiler flags to parse ExpressionMethodReferences
    // as TypeMethodReferences. Using these specially compiled constructs breaks us currently, but
    // I'm not sure if that is because of the special compilation, or an actual issue on our side.
    // Thankfully, this should be a small use case anyway.
    String typeReferenceHeader = "interface H { Object copy(int[] i); }";
    String translation = translateSourceFile(
        typeReferenceHeader + "class Test { H h = int[]::clone; }", "Test", "Test.m");
    assertTranslatedSegments(translation,
        "id Test$$Lambda$1_impl(LambdaBase *self_, SEL _cmd, IOSIntArray *a) {",
        "return [((IOSIntArray *) nil_chk(a)) clone];",
        "CreateNonCapturing", "@selector(copy__WithIntArray:)");
  }

  public void testReferenceToInstanceMethodOfType() throws IOException {
    String source = "import java.util.Comparator;"
        + "class Test { void f() { Comparator<String> s = String::compareTo; } }";

    String impl = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(impl, "return [((NSString *) nil_chk(a)) compareToWithId:b];");
  }

  public void testReferenceToInstanceMethodOfGenericType() throws IOException {
    String source = "interface BiConsumer<T,U> { void accept(T t, U u); } "
        + "interface Collection<E> { boolean add(E x); } "
        + "class Test {"
        + "  <T> void f(Collection<T> c, T o) {"
        + "    BiConsumer<Collection<T>, T> bc = Collection<T>::add;"
        + "  }"
        + "}";

    String impl = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(impl, "[((id<Collection>) nil_chk(a)) addWithId:b];");
    assertNotInTranslation(impl, "return [((id<Collection>) nil_chk(a)) addWithId:b];");
  }

  public void testReferenceToInstanceMethodOfGenericTypeWithReturnType() throws IOException {
    String source = "interface BiConsumer<T,U> { boolean accept(T t, U u); } "
        + "interface Collection<E> { boolean add(E x); } "
        + "class Test {"
        + "  <T> void f(Collection<T> c, T o) {"
        + "    BiConsumer<Collection<T>, T> bc = Collection<T>::add;"
        + "  }"
        + "}";

    String impl = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(impl, "return [((id<Collection>) nil_chk(a)) addWithId:b];");
  }

  public void testVarArgs() throws IOException {
    String varArgsHeader = "interface I { void foo(int a1, String a2, String a3); }"
        + "interface I2 { void foo(int a1, String a2, String a3, String a4); }"
        + "class Y { static void m(int a1, String... rest) { } }";
    String translation = translateSourceFile(
        varArgsHeader + "class Test { I i = Y::m; I2 i2 = Y::m; }",
        "Test", "Test.m");
    assertTranslatedSegments(translation,
        "void Test$$Lambda$2_impl(LambdaBase *self_, SEL _cmd, jint a, NSString *b, NSString *c, "
            + "NSString *d) {",
        "Y_mWithInt_withNSStringArray_(a, [IOSObjectArray arrayWithObjects:(id[]){ b, c, d } "
            + "count:3 type:NSString_class_()]);",
        "instance = CreateNonCapturing(",
        "\"Test$$Lambda$2\", 1, (Protocol *[]){ @protocol(I2)},",
        "1,(SEL []){ @selector(fooWithInt:withNSString:withNSString:withNSString:)}, "
            + "(IMP []){ (IMP)&Test$$Lambda$2_impl}, (const char *[]){ desc1.types});",
        "void Test$$Lambda$1_impl(LambdaBase *self_, SEL _cmd, jint a, NSString *b, NSString *c) {",
        "Y_mWithInt_withNSStringArray_(a, [IOSObjectArray arrayWithObjects:(id[]){ b, c } count:2 "
            + "type:NSString_class_()]);",
        "instance = CreateNonCapturing(",
        "\"Test$$Lambda$1\", 1, (Protocol *[]){ @protocol(I)},",
        "1,(SEL []){ @selector(fooWithInt:withNSString:withNSString:)}, "
            + "(IMP []){ (IMP)&Test$$Lambda$1_impl}, (const char *[]){ desc1.types});"
    );
  }

  public void testReferenceToInstanceMethodOfTypeWithVarArgs() throws IOException {
    String p = "interface P<T> { void f(T t); }";
    String q = "interface Q<T> { void f(T t, String a, String b); }";
    String r = "interface R<T> { void f(T t, String... rest); }";
    String x1 = p + "class X { void g(String... rest) {} void h() { P<X> ff = X::g; } }";
    String x2 = q + "class X { void g(String... rest) {} void h() { Q<X> ff = X::g; } }";
    String x3 = r + "class X { void g(String... rest) {} void h() { R<X> ff = X::g; } }";
    String impl1 = translateSourceFile(x1, "X", "X.m");
    String impl2 = translateSourceFile(x2, "X", "X.m");
    String impl3 = translateSourceFile(x3, "X", "X.m");

    // Pass an empty array to the referenced method.
    assertTranslation(impl1, "void X$$Lambda$1_impl(LambdaBase *self_, SEL _cmd, X *a) {");
    assertTranslation(impl1, "[((X *) nil_chk(a)) "
        + "gWithNSStringArray:[IOSObjectArray arrayWithLength:0 type:NSString_class_()]];");

    // Pass an array of the arguments b and c to the referenced method.
    assertTranslation(impl2, "void X$$Lambda$1_impl(LambdaBase *self_, SEL _cmd, X *a, "
        + "NSString *b, NSString *c) {");
    assertTranslation(impl2, "[((X *) nil_chk(a)) "
        + "gWithNSStringArray:"
        + "[IOSObjectArray arrayWithObjects:(id[]){ b, c } count:2 type:NSString_class_()]];");

    // Pass the varargs array to the referenced method.
    assertTranslation(impl3, "void X$$Lambda$1_impl(LambdaBase *self_, SEL _cmd, X *a, "
        + "IOSObjectArray *b) {");
    assertTranslation(impl3, "[((X *) nil_chk(a)) gWithNSStringArray:b];");
  }

  public void testArgumentBoxingAndUnboxing() throws IOException {
    String header = "interface IntFun { void apply(int a); }\n"
        + "interface IntegerFun { void apply(Integer a); }";
    String translation = translateSourceFile(header
        + "class Test { static void foo(Integer x) {}; static void bar(int x) {};"
        + "IntFun f = Test::foo; IntegerFun f2 = Test::bar; }", "Test", "Test.m");
    assertTranslatedSegments(translation,
        "void Test$$Lambda$2_impl(LambdaBase *self_, SEL _cmd, JavaLangInteger *a) {",
        "Test_barWithInt_([((JavaLangInteger *) nil_chk(a)) intValue]);",
        "void Test$$Lambda$1_impl(LambdaBase *self_, SEL _cmd, jint a) {",
        "Test_fooWithJavaLangInteger_(JavaLangInteger_valueOfWithInt_(a));"
        );
  }

  public void testReturnBoxingAndUnboxing() throws IOException {
    String header = "interface Fun { Integer a(); } interface Fun2 { int a(); }";
    String translation = translateSourceFile(header
        + "class Test { int size() { return 42; } Integer size2() { return 43; }"
        + "Fun f = this::size; Fun2 f2 = this::size2; }",
        "Test", "Test.m");
    assertTranslatedSegments(translation,
        "jint Test$$Lambda$2_impl(LambdaBase *self_, SEL _cmd) {",
        "return [((JavaLangInteger *) nil_chk([this$0_ size2])) intValue];",
        "JavaLangInteger *Test$$Lambda$1_impl(LambdaBase *self_, SEL _cmd) {",
        "return JavaLangInteger_valueOfWithInt_([this$0_ size]);"
        );
  }

  // Creation references can be initialized only for side effects, and have a void return.
  public void testCreationReferenceVoidReturn() throws IOException {
    String header = "interface V { void f(); }";
    String translation = translateSourceFile(header + "class Test { V v = Test::new; }", "Test",
        "Test.m");
    assertTranslatedLines(translation, "void Test$$Lambda$1_impl(LambdaBase *self_, SEL _cmd) {",
        "create_Test_init();");
  }

  public void testCreationReferenceNonVoidReturn() throws IOException {
    String header = "interface V { Object f(); }";
    String translation = translateSourceFile(header + "class Test { V v = Test::new; }", "Test",
        "Test.m");
    assertTranslatedLines(translation, "id Test$$Lambda$1_impl(LambdaBase *self_, SEL _cmd) {",
         "return create_Test_init();");
  }

  public void testArrayCreationReference() throws IOException {
    String translation = translateSourceFile("import java.util.function.Supplier;"
        + "interface IntFunction<R> {"
        + "  R apply(int value);"
        + "}"
        + "class Test {"
        + "  IntFunction<int[]> i = int[]::new;"
        + "}", "Test", "Test.m");
    assertNotInTranslation(translation, "return create_IntFunction_initWithIntArray_");
    assertTranslatedLines(translation,
        "IOSIntArray *Test$$Lambda$1_impl(LambdaBase *self_, SEL _cmd, jint a) {",
        "return [IOSIntArray arrayWithLength:a];");
  }

  public void testCreationReferenceOfLocalCapturingType() throws IOException {
    String translation = translateSourceFile(
        "interface Supplier<T> { T get(); }"
        + "class Test { static Supplier<Runnable> test(Runnable r) {"
        + "class Runner implements Runnable { public void run() { r.run(); } }"
        + "return Runner::new; } }", "Test", "Test.m");
    assertTranslatedSegments(translation,
        "return create_Test_1Runner_initWithJavaLangRunnable_(r);",
        "cls = CreatePossiblyCapturingClass(");
  }

  public void testQualifiedSuperMethodReference() throws IOException {
    String translation = translateSourceFile(
        "interface I { void bar(); }"
        + "class Test { void foo() {} static class TestSub extends Test { void foo() {}"
        + "class Inner { I test() { return TestSub.super::foo; } } } }",
        "Test", "Test.m");
    assertTranslatedSegments(translation,
        "static void (*Test_TestSub_super$_foo)(id, SEL);",
        "void Test_TestSub_Inner$$Lambda$1_impl(LambdaBase *self_, SEL _cmd) {",
        "Test_TestSub_super$_foo(this$0_->this$0_, @selector(foo));", "}",
        "cls = CreatePossiblyCapturingClass("
        );
  }

  public void testMultipleMethodReferencesNilChecks() throws IOException {
    String translation = translateSourceFile(
        "interface Foo { void f(Test t); }"
        + "class Test { void foo() {} void test() {"
        + " Foo f1 = Test::foo; Foo f2 = Test::foo; } }", "Test", "Test.m");
    // Both lambdas must perform a nil_chk on their local variable "a".
    assertOccurrences(translation, "nil_chk(a)", 2);
  }

  public void testCapturingExpressionMethodReferences() throws IOException {
    String translation = translateSourceFile(
        "interface Supplier { int get(); }"
        + "class Holder { private int num; public Holder(int i) {num = i;} int get() {return num;}}"
        + "class Test { public void run() { Holder h = new Holder(1); Supplier s = h::get; } }",
        "Test", "Test.m");
    assertTranslation(translation, "CreatePossiblyCapturingClass");
    assertNotInTranslation(translation, "CreateNonCapturing");
  }
}
