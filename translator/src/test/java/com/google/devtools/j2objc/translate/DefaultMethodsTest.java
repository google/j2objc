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
import com.google.devtools.j2objc.util.SourceVersion;

import java.io.IOException;

/**
 * Tests Java 8 default method support.
 *
 * @author Lukhnos Liu
 */
public class DefaultMethodsTest extends GenerationTest {
  @Override
  protected void loadOptions() throws IOException {
    super.loadOptions();
    Options.setSourceVersion(SourceVersion.JAVA_8);
  }

  public void testDefaultMethodFunctionalization() throws IOException {
    String source = "interface Foo { void f(); default void g() { f(); } }";
    String header = translateSourceFile(source, "Test", "Test.h");
    String impl =  getTranslatedFile("Test.m");

    assertTranslation(header, "- (void)f;");
    assertTranslation(header, "- (void)g;");
    assertTranslation(header, "@interface Foo : NSObject < Foo >");
    assertTranslation(header, "void Foo_g(id<Foo> self);");
    assertTranslatedLines(impl, "- (void)g {", "Foo_g(self);", "}");
    assertTranslatedLines(impl, "void Foo_g(id<Foo> self) {", "[self f];", "}");
  }

  public void testDefaultMethodFunctionalizationWithReflectionsStripped() throws IOException {
    Options.setStripReflection(true);

    String source = "interface Foo { void f(); default void g() { f(); } }";
    String header = translateSourceFile(source, "Test", "Test.h");
    String impl =  getTranslatedFile("Test.m");

    // Even when reflection info is stripped, the companion class should still be generated.
    assertTranslation(header, "- (void)f;");
    assertTranslation(header, "- (void)g;");
    assertTranslation(header, "@interface Foo : NSObject < Foo >");
    assertTranslation(header, "void Foo_g(id<Foo> self);");
    assertTranslatedLines(impl, "- (void)g {", "Foo_g(self);", "}");
    assertTranslatedLines(impl, "void Foo_g(id<Foo> self) {", "[self f];", "}");
  }

  public void testCompanionClassDefaultMethodImplementation() throws IOException {
    String source = "interface A { void f(); default void a() { f(); } }"
        + "interface B extends A { default void f() {} }"
        + "interface C extends B { void f(); }"
        + "interface D { default void d() {} }"
        + "interface E extends A, C, D {}";

    String header = translateSourceFile(source, "Test", "Test.h");
    String impl =  getTranslatedFile("Test.m");

    assertTranslation(header, "@interface E : NSObject < E >");
    assertOccurrences(impl, "A_a(self)", 4); // From -[A a], -[B a], -[C a], and -[E a].
    assertOccurrences(impl, "B_f(self)", 1); // From -[B f].
    assertOccurrences(impl, "D_d(self)", 2); // From -[D d] and -[E d].
  }

  public void testSuperDefaultMethodInvocation() throws IOException {
    String translation = translateSourceFile(
        "interface Foo { default int f(int y) { return y + 1; } }"
        + "class Bar implements Foo {"
        + "  public Bar(int x) { int i = Foo.super.f(x); }"
        + "  public int f(int y) { return Foo.super.f(y) + 1; }"
        + "}", "Test", "Test.m");

    assertTranslation(translation, "jint i = Foo_fWithInt_(self, x);");
    assertTranslation(translation, "return Foo_fWithInt_(self, y) + 1;");
  }

  public void testBasicDefaultMethodUsage() throws IOException {
    String source = "  interface A {"
        + "  default void f() {}"
        + "  default int g() { return 0; }"
        + "  void p();"
        + "  static void q() {}"
        + "  default Object r(int x, A b) {"
        + "    f(); p(); q();"
        + "    return x + b.g();"
        + "  }"
        + "}"
        + "class B implements A {"
        + "  @Override public void p() {}"
        + "}";
    String header = translateSourceFile(source, "Test", "Test.h");
    String impl = getTranslatedFile("Test.m");

    assertTranslation(header, "void A_f(id<A> self)");
    assertTranslation(header, "jint A_g(id<A> self)");
    assertTranslation(header, "void A_q()");
    assertTranslation(header, "id A_rWithInt_withA_(id<A> self, jint x, id<A> b)");

    // This is an illegal value for JVM's access_flags field and should never show up in metadata.
    assertNotInTranslation(impl, "0x10001");

    assertTranslatedLines(impl, "- (void)f {", "A_f(self);", "}");
    assertTranslatedLines(impl, "- (jint)g {", "return A_g(self);", "}");
    assertTranslatedLines(impl, "- (void)p {", "}");
    assertTranslatedLines(impl, "- (id)rWithInt:(jint)arg0", "withA:(id<A>)arg1 {",
        "return A_rWithInt_withA_(self, arg0, arg1);", "}");
  }

  public void testEnumSupport() throws Exception {
    String source = "interface I { default void f() {} }"
        + "enum E implements I { FOO, BAR }";
    String header = translateSourceFile(source, "Test", "Test.h");
    String impl = getTranslatedFile("Test.m");

    assertOccurrences(header, "- (void)f;", 1); // Declared in I but not E.
    assertTranslatedLines(impl, "- (void)f {", "I_f(self);", "}");
  }

  public void testDefaultMethodWithLambda() throws IOException {
    String source = "interface A {"
        + "  String op(String a, String b);"
        + "  default String underscorePrefix(String a) { return op(\"_\", a); }"
        + "}"
        + "interface Unrelated { default boolean unrelated() { return false; } }"
        + "class B {"
        + "  void f() { g((x, y) -> x + y); }"
        + "  String g(A a) { return a.underscorePrefix(\"foo\"); }"
        + "  boolean other() { return ((A & Unrelated) (a,b) -> a).unrelated(); }"
        + "}";
    String header = translateSourceFile(source, "Test", "Test.h");
    String impl = getTranslatedFile("Test.m");

    assertTranslation(header,
        "NSString *A_underscorePrefixWithNSString_(id<A> self, NSString *a)");
    assertTranslatedLines(impl,
        "NSString *A_underscorePrefixWithNSString_(id<A> self, NSString *a) {",
        "  return [self opWithNSString:@\"_\" withNSString:a];",
        "}");

    // Make sure we base the non-capturing lambda on interface A's companion class that has the
    // default method shim.
    assertTranslatedLines(impl,
        "- (NSString *)underscorePrefixWithNSString:(NSString *)arg0 {",
        "  return A_underscorePrefixWithNSString_(self, arg0);",
        "}",
        "",
        "- (jboolean)unrelated {",
        "  return Unrelated_unrelated(self);",
        "}");
  }

  public void testDefaultMethodsInInterfaceExtensions() throws IOException {
    String source = "interface A { default void f() {} }"
        + "interface B extends A { default void f() {} }"
        + "class C implements B {}";
    String impl = translateSourceFile(source, "Test", "Test.m");

    assertTranslatedLines(impl, "- (void)f {",
        "B_f(self);",
        "}");
  }

  public void testRedeclaringAbstractInterfaceMethods() throws IOException {
    String source = "interface A { default void f() {}  }"
        + "interface B extends A { void f(); }";
    String impl = translateSourceFile(source, "Test", "Test.m");

    assertTranslation(impl, "void A_f(id<A> self)");
    assertNotInTranslation(impl, "void B_f(id<A> self)");
  }

  public void testAbstractClassImplementations() throws IOException {
    String source = "interface A { default void f() {} default void g() {} }"
        + "interface B extends A { default void f() {} void g(); }"
        + "abstract class C implements B {}";
    String impl = translateSourceFile(source, "Test", "Test.m");

    assertTranslation(impl, "void A_f(id<A> self)");
    assertTranslation(impl, "void A_g(id<A> self)");
    assertTranslation(impl, "void B_f(id<B> self)");
    assertNotInTranslation(impl, "void B_g(id<A> self)");
    assertTranslatedLines(impl, "- (void)f {", "B_f(self);", "}");
    assertOccurrences(impl, "A_g(self);", 1); // From @implementation A.
    assertNotInTranslation(impl, "B_g(self);");
  }

  public void testUniqueShimImplementation() throws IOException {
    String source = "interface A { default void f() {} }"
        + "interface B extends A { default void g() {} }"
        + "class P implements A {}"
        + "class Q extends P implements B {}";
    String header = translateSourceFile(source, "Test", "Test.h");
    String impl = getTranslatedFile("Test.m");

    assertOccurrences(header, "- (void)f;", 1); // Declared once by A.
    assertOccurrences(impl, "A_f(self);", 3); // Called by -[A f], -[B f], and -[P f].
    assertOccurrences(impl, "B_g(self);", 2); // Called by -[B g] and -[Q g].
  }

  public void testConcreteMethodPrecedence() throws Exception {
    String source = "interface A { default void f() {} }"
        + "class P { public void f() {} }"
        + "class Q extends P implements A {}";
    String header = translateSourceFile(source, "Test", "Test.h");
    String impl = getTranslatedFile("Test.m");

    assertOccurrences(header, "- (void)f;", 2); // Declared once by A and another by P.
    assertOccurrences(impl, "A_f(self);", 1); // Used only once in @implementation A.
  }

  public void testAnonymousClass() throws IOException {
    String source = "interface A {"
        + "  default String f(String x) { return x + x; } "
        + "}"
        + "class B {"
        + "  String h(String y) {"
        + "    return new A() {}.f(y);"
        + "  }"
        + "}";
    String impl = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(impl, "- (NSString *)fWithNSString:(NSString *)arg0 {");
    assertTranslation(impl, "return A_fWithNSString_(self, arg0);");
  }

  public void testNestedAndInnerClasses() throws Exception {
    String source = "class A {"
        + "  interface P { default void f() {} }"
        + "  static class B implements P {"
        + "    class C implements P {}"
        + "  }"
        + "  class D implements P {}"
        + "}";
    String header = translateSourceFile(source, "Test", "Test.h");
    String impl = getTranslatedFile("Test.m");
    assertTranslation(header, "void A_P_f(id<A_P> self);");

    // This is called by the shims -[A_P f], -[A_B f], -[A_B_C f], and -[A_D f].
    assertOccurrences(impl, "A_P_f(self);", 4);
  }

  public void testFunctionizedMethodRenaming() throws Exception {
    String source = "interface P {"
            + "  default void f() {}"
            + "  static Object f = new Object();"
            + "}";
    String header = translateSourceFile(source, "Test", "Test.h");
    String impl = getTranslatedFile("Test.m");

    assertTranslation(header, "P_get_f_()");
    assertTranslation(header, "P_f_");
    assertTranslation(header, "void P_f(id<P> self)");
    assertTranslation(impl, "id P_f_;");
    assertTranslation(impl, "void P_f(id<P> self)");
  }

  public void testNoReconsideringSuperclasses() throws Exception {
    String source = "interface P { default void f() {} }"
        + "class A implements P {}"
        + "class B extends A {}"
        + "class C extends B implements P {}";
    String header = translateSourceFile(source, "Test", "Test.h");
    String impl = getTranslatedFile("Test.m");
    assertOccurrences(header, "- (void)f;", 1); // From P, but not A and C
    assertOccurrences(impl, "P_f(self);", 2); // From -[P f] and -[A f], but not from B or C.
  }

  public void testInterfaceTraversalOrder() throws Exception {
    String source = "interface A { default void f() {} }"
        + "interface B extends A { void f(); }"
        + "interface C extends B { default void f() {} }"
        + "interface D extends C { void f(); }"
        + "interface E extends D { default void f() {} }"
        + "interface F extends E { void f(); }"
        + "interface G extends F { default void f() {} }"
        + "class H implements G {}";
    String impl = translateSourceFile(source, "Test", "Test.m");

    // The shim should call the default method in the interface that's closest to H.
    assertTranslatedLines(impl, "- (void)f {", "G_f(self);", "}");
  }

  public void testInterfaceTraversalOrderWithDuplicateImplements() throws Exception {
    String source = "interface A { default void f() {} }"
        + "interface B extends A { }"
        + "interface C extends B { default void f() {} }"
        + "class D implements A, C {}";
    String impl = translateSourceFile(source, "Test", "Test.m");
    assertOccurrences(impl, "A_f(self);", 2); // From -[A f] and -[B f]
    assertOccurrences(impl, "C_f(self);", 2); // From -[C f] and -[D f]
  }

  public void testGenericDefaultMethods() throws Exception {
    String source = "interface A<T> { default T f() { return null; } }"
        + "class P implements A<String> {}"
        + "class Q implements A<Integer> { public Integer f() { return 0; } }"
        + "abstract class R implements A<Long> { public abstract Long f(); }";
    String header = translateSourceFile(source, "Test", "Test.h");
    String impl = getTranslatedFile("Test.m");
    assertTranslation(header, "- (id)f");
    assertTranslation(header, "id A_f(id<A> self)");
    assertTranslation(header, "- (JavaLangInteger *)f;"); // From Q

    // From R; abstract method is still declared and will be implemented with throwing an exception.
    assertTranslation(header, "- (JavaLangLong *)f;");

    // From A<T>
    assertTranslatedLines(impl, "id A_f(id<A> self) {", "return nil;", "}");

    // From P
    assertTranslatedLines(impl, "- (NSString *)f {", "return A_f(self);", "}");

    // From Q
    assertTranslatedLines(impl,
        "- (JavaLangInteger *)f {",
        "return JavaLangInteger_valueOfWithInt_(0);",
        "}");

    // From R
    assertTranslatedLines(impl,
        "- (JavaLangLong *)f {",
        "// can't call an abstract method",
        "[self doesNotRecognizeSelector:_cmd];",
        "return 0;",
        "}");
  }

  public void testDefaultMethodWithGenericTypeParameters() throws IOException {
    String source = "interface A<T> {}"
        + "interface B { default void f(A<String> x) {} }"
        + "class P implements B {}"
        + "class Q implements B { @Override public void f(A<String> x) {} }";
    String header = translateSourceFile(source, "Test", "Test.h");
    String impl = getTranslatedFile("Test.m");

    assertTranslation(header, "void B_fWithA_(id<B> self, id<A> x);");
    assertOccurrences(header, "- (void)fWithA:(id<A>)x;", 2); // From B and Q
    // From P
    assertTranslatedLines(impl, "- (void)fWithA:(id<A>)arg0 {", "B_fWithA_(self, arg0);", "}");
  }

  public void testNarrowedReturnType() throws IOException {
    String source = "interface A { default Object f() { return null; } }"
        + "class B implements A { public String f() { return \"\"; } }";
    String header = translateSourceFile(source, "Test", "Test.h");
    String impl = getTranslatedFile("Test.m");

    // No shim should be generated as -[B f].
    assertOccurrences(header, "- (id)f", 1);
    assertTranslation(header, "- (NSString *)f");

    // From the default method of A.
    assertTranslatedLines(impl, "id A_f(id<A> self) {", "return nil;", "}");

    // From @implementation A.
    assertTranslatedLines(impl, "- (id)f {", "return A_f(self);", "}");
    assertOccurrences(impl, "return A_f(self)", 1);

    // From @implementation B.
    assertTranslatedLines(impl, "- (NSString *)f {", "return @\"\";", "}");
  }

  public void testIncompleteImplPragmasInCompanionClasses() throws IOException {
    addSourceFile("interface A { void f(); } ", "A.java");
    addSourceFile("interface B extends A {}", "B.java");
    addSourceFile("interface C extends A { default void g() {} }", "C.java");
    addSourceFile("interface D extends C { }", "D.java");
    String implA = translateSourceFile("A", "A.m");
    String implB = translateSourceFile("B", "B.m");
    String implC = translateSourceFile("C", "C.m");
    String implD = translateSourceFile("D", "D.m");
    String ignoreProtocol = "#pragma clang diagnostic ignored \"-Wprotocol\"";
    assertNotInTranslation(implA, ignoreProtocol);
    assertNotInTranslation(implB, ignoreProtocol);
    assertTranslation(implC, ignoreProtocol);
    assertTranslation(implD, ignoreProtocol);
  }

  public void testIncompleteImplPragmasInCompanionClassesWithNoReflection() throws IOException {
    Options.setStripReflection(true);

    addSourceFile("interface A { void f(); } ", "A.java");
    addSourceFile("interface B extends A {}", "B.java");
    addSourceFile("interface C extends A { default void g() {} }", "C.java");
    addSourceFile("interface D extends C { }", "D.java");
    String implA = translateSourceFile("A", "A.m");
    String implB = translateSourceFile("B", "B.m");
    String implC = translateSourceFile("C", "C.m");
    String implD = translateSourceFile("D", "D.m");
    String ignoreProtocol = "#pragma clang diagnostic ignored \"-Wprotocol\"";
    assertNotInTranslation(implA, ignoreProtocol);
    assertNotInTranslation(implB, ignoreProtocol);
    assertTranslation(implC, ignoreProtocol);
    assertTranslation(implD, ignoreProtocol);
  }

  public void testAccessingOuterType() throws IOException {
    String source = "interface A { default Class<?> type() { return getClass(); } }";
    String impl = translateSourceFile(source, "Test", "Test.m");
    assertTranslatedLines(impl, "IOSClass *A_type(id<A> self) {", "return [self java_getClass];", "}");
  }

  public void testDefaultMethodWithMultipleSelectors() throws IOException {
    addSourceFile("interface A <T> { void foo(T t); }", "A.java");
    addSourceFile("interface B { void foo(String s); }", "B.java");
    addSourceFile("interface C extends A<String>, B { default void foo(String s) {} }", "C.java");
    addSourceFile("class D implements C {}", "D.java");
    String headerC = translateSourceFile("C", "C.h");
    String implD = translateSourceFile("D", "D.m");
    assertTranslation(headerC, "- (void)fooWithId:(NSString *)s;");
    assertTranslatedLines(implD,
        "- (void)fooWithId:(NSString *)arg0 {", "C_fooWithNSString_(self, arg0);", "}");
    assertTranslatedLines(implD,
        "- (void)fooWithNSString:(NSString *)arg0 {", "[self fooWithId:arg0];", "}");
  }

  // Regression test simplified from java.util.stream.Node.
  public void testNestedInterfaces() throws IOException {
    String translation = translateSourceFile(
        "interface Node<T> { "
        + "default Node<T> getChild(int i) {"
        + "  throw new IndexOutOfBoundsException();"
        + "}"
        + "interface OfPrimitive<T, T_NODE extends OfPrimitive<T, T_NODE>> extends Node<T> {"
        + "  default T_NODE getChild(int i) {"
        + "    throw new IndexOutOfBoundsException();"
        + "  }"
        + "}"
        + "interface OfInt extends OfPrimitive<Integer, OfInt> {}}", "Node", "Node.m");
    assertTranslatedLines(translation,
        "return ((id<Node_OfInt>) Node_OfPrimitive_getChildWithInt_(self, arg0));");
  }

  // Regression test simplified from java.util.stream.ReduceOps, where @interface for
  // private interface wasn't generated.
  public void testPrivateNestedInterfaceWithDefaultMethod() throws IOException {
    addSourceFile("interface Sink<T> { default void test(long size) {}}", "Sink.java");
    String translation = translateSourceFile("class Test {"
        + "private interface AccumulatingSink<T> extends Sink<T> {}}", "Test", "Test.m");
    assertTranslatedLines(translation,
        "@interface Test_AccumulatingSink : NSObject < Test_AccumulatingSink >");
  }

  public void testExtraSelectorsFromMultipleOverrides() throws IOException {
    addSourceFile("interface I { int foo(String t); }", "I.java");
    addSourceFile("class A<T> { int foo(T t) {} }", "A.java");
    String translation = translateSourceFile(
        "class B extends A<String> implements I { public int foo(String t) { return 7; } }",
        "B", "B.h");
    assertTranslation(translation, "- (jint)fooWithId:(NSString *)t;");

    translation = getTranslatedFile("B.m");
    assertTranslatedLines(translation,
        "- (jint)fooWithId:(NSString *)t {",
        "  return 7;",
        "}");
    assertTranslatedLines(translation,
        "- (jint)fooWithNSString:(NSString *)arg0 {",
        "  return [self fooWithId:arg0];",
        "}");
  }

  public void testClassInheritsOverridingMethodsWithDifferentSelectors() throws IOException {
    addSourceFile("class A extends B implements C<B> {}", "A.java");
    addSourceFile("class B extends D { public void foo(B b) { } }", "B.java");
    addSourceFile("interface C<T extends D> { public void foo(T d); }", "C.java");
    addSourceFile("class D {}", "D.java");
    String aImpl = translateSourceFile("A", "A.m");
    assertTranslatedLines(aImpl,
        "- (void)fooWithD:(B *)arg0 {",
        "  [self fooWithB:arg0];",
        "}");
  }
}
