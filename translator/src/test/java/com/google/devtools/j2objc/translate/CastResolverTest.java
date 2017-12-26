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
import com.google.devtools.j2objc.ast.Statement;

import java.io.IOException;
import java.util.List;

/**
 * Unit tests for {@link CastResolver}.
 *
 * @author Keith Stanger
 */
public class CastResolverTest extends GenerationTest {

  public void testFieldOfGenericParameter() throws IOException {
    String translation = translateSourceFile(
        "class Test { int foo; static class Other<T extends Test> {"
        + " int test(T t) { return t.foo + t.foo; } } }", "Test", "Test.m");
    assertTranslation(translation, "return ((Test *) nil_chk(t))->foo_ + t->foo_;");
  }

  public void testIntCastInStringConcatenation() throws IOException {
    String translation = translateSourceFile(
        "public class Test { void test() { "
        + "  String a = \"abc\"; "
        + "  String b = \"foo\" + a.hashCode() + \"bar\" + a.hashCode() + \"baz\"; } }",
        "Test", "Test.m");
    assertTranslation(translation,
        "JreStrcat(\"$I$I$\", @\"foo\", ((jint) [a hash]), @\"bar\", ((jint) [a hash]),"
          + " @\"baz\")");
  }

  public void testCastInConstructorChain() throws IOException {
    String source = "int i = new Throwable().hashCode();";
    List<Statement> stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("jint i = ((jint) [create_JavaLangThrowable_init() hash]);", result);
  }

  // b/5872710: generic return type needs to be cast if chaining invocations.
  public void testTypeVariableCast() throws IOException {
    String translation = translateSourceFile(
      "import java.util.ArrayList; public class Test {"
      + "  int length; static ArrayList<String> strings = new ArrayList<String>();"
      + "  public static void main(String[] args) { int n = strings.get(1).hashCode(); }}",
      "Test", "Test.m");
    assertTranslation(translation, "((jint) [((NSString *) "
      + "nil_chk([((JavaUtilArrayList *) nil_chk(Test_strings)) getWithInt:1])) hash]);");
  }

  // Verify that Object.hashCode() return value is cast when used.
  public void testStringLengthCompare() throws IOException {
    String translation = translateSourceFile(
        "public class Test { boolean test(String s) { return -2 < \"1\".length(); }"
        + "  void test2(Object o) { o.hashCode(); }"
        + "  int test3() { return super.hashCode(); } }",
        "Test", "Test.m");
    // Verify referenced return value is cast.
    assertTranslation(translation, "return -2 < [@\"1\" java_length];");
    // Verify unused return value isn't.
    assertTranslation(translation, "[nil_chk(o) hash];");
    // Verify that super call to hashCode() is cast.
    assertTranslation(translation, "return ((jint) [super hash]);");
  }

  public void testDerivedTypeVariableInvocation() throws IOException {
    String translation = translateSourceFile(
        "public class Test {"
        + "  static class Base <T extends BaseFoo> {"
        + "    protected T foo;"
        + "    public Base(T foo) {"
        + "      this.foo = foo;"
        + "    }"
        + "  }"
        + "  static class BaseFoo {"
        + "    void baseMethod() {}"
        + "  }"
        + "  static class Derived extends Base<DerivedFoo> {"
        + "    public Derived(DerivedFoo foo) {"
        + "      super(foo);"
        + "    }"
        + "    int test() {"
        + "      foo.baseMethod();"
        + "      foo.derivedMethod();"
        + "      return foo.myInt;"
        + "    }"
        + "  }"
        + "  static class DerivedFoo extends BaseFoo {"
        + "    int myInt;"
        + "    void derivedMethod() {}"
        + "  }"
        + "}", "Test", "Test.m");
    // Verify foo.derivedMethod() has cast of appropriate type variable.
    assertTranslation(translation, "[((Test_DerivedFoo *) nil_chk(foo_)) derivedMethod];");
    // Verify that a cast can be added to a QualifiedName node.
    assertTranslation(translation, "return ((Test_DerivedFoo *) nil_chk(foo_))->myInt_;");
  }

  public void testCapturedType() throws IOException {
    String translation = translateSourceFile(
        "class Test {"
        + " interface Bar { void bar(); }"
        + " static class Foo<T extends Bar> { T get() { return null; } }"
        + " void test(Foo<?> foo) { foo.get().bar(); } }", "Test", "Test.m");
    assertTranslation(translation,
        "[((id<Test_Bar>) nil_chk([((Test_Foo *) nil_chk(foo)) get])) bar];");
  }

  public void testChainedFieldLookup() throws IOException {
    String translation = translateSourceFile(
        "class Test {"
        + " static class Foo { Bar bar; }"
        + " static class Bar { int baz; }"
        + " static class GenericImpl<T> { T foo; }"
        + " static class Impl extends GenericImpl<Foo> {"
        + " int test() {"
        // Need to call "foo.bar.baz" twice so that the second expression is
        // free of nil_chk's.
        + " int i = foo.bar.baz;"
        + " return foo.bar.baz; } } }", "Test", "Test.m");
    // This is actually a regression for a NPE in the translator, but we may as
    // well check the output.
    assertTranslation(translation, "return ((Test_Foo *) foo_)->bar_->baz_;");
  }

  public void testCastOfParenthesizedExpression() throws IOException {
    String translation = translateSourceFile(
        "class Test { static class Node { int key; } static Node next;"
        + " Node getNext() { return null; }"
        + " int test() { return (next = getNext()).key; } }", "Test", "Test.m");
    assertTranslation(translation,
        "return ((Test_Node *) (JreStrongAssign(&Test_next, [self getNext])))->key_;");
  }

  public void testCastOfInferredWildcardType() throws IOException {
    String translation = translateSourceFile(
        "class Test { <T> T genericMethod(T a, T b) { return null; }"
        + " interface I { void foo(); } static class C {}"
        + " static class Bar extends C implements I { public void foo() {} }"
        + " static class Baz extends C implements I { public void foo() {} }"
        + " I test() { genericMethod(new Bar(), new Baz()).foo();"
        + " return genericMethod(new Bar(), new Baz()); } }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "- (id<Test_I>)test {",
        // Type cast must contain both "Test_C" and "Test_I".
        "  [((Test_C<Test_I> *) nil_chk([self genericMethodWithId:create_Test_Bar_init()"
          + " withId:create_Test_Baz_init()])) foo];",
        // No need for a cast because genericMethodWithId:withId: is declared to return "id".
        "  return [self genericMethodWithId:create_Test_Bar_init() withId:create_Test_Baz_init()];",
        "}");
  }

  public void testAccessOfFieldFromSubclassWithMoreSpecificTypeVariable() throws IOException {
    String translation = translateSourceFile(
        "class Test<T extends I2> {"
        // This method won't be functionized.
        + "void test1(T i) {}"
        // The private method will be functionized.
        + "private void test2(T i) {}"
        + "void test3(B<T> b) { test1(b.foo); test2(b.foo); } }"
        + "interface I1 {}"
        + "interface I2 extends I1 {}"
        + "class A<T extends I1> { T foo; }"
        + "class B<T extends I2> extends A<T> {}", "Test", "Test.m");
    // Test that access of "foo" from subclass B is cast to id<I2>.
    assertTranslation(translation, "[self test1WithI2:((id<I2>) ((B *) nil_chk(b))->foo_)];");
    assertTranslation(translation, "Test_test2WithI2_(self, ((id<I2>) b->foo_));");
  }

  /**
   * Clang reports an incompatible pointer comparison when comparing two
   * objects with different interface types. That's potentially wrong both
   * in Java and Objective-C, since a single class can implement both interfaces.
   */
  public void testInterfaceComparisons() throws IOException {
    String translation = translateSourceFile(
        "class Test { "
        + " interface Foo {}"
        + " interface Bar {}"
        + " static class Mumble implements Foo, Bar {}"
        + " static boolean test(Foo f, Bar b) {"
        + "   return f == b;"
        + " }"
        + " static boolean test2(Bar b, Foo f) {"
        + "   return b != f;"
        + " }"
        + " static boolean test3(byte[] buffer, int offset) {"
        + "   return buffer[offset] != 'Z';"  // Verify primitive types don't get (id) casts.
        + " }"
        + " public static void main(String... args) {"
        + "   Mumble m = new Mumble();"
        + "   test(m, m);"
        + "   test2(m, m);"
        + " }"
        + "}", "Test", "Test.m");
    // Wrong: clang will report a compare-distinct-pointer-types warning.
    assertNotInTranslation(translation, "return f == b;");
    assertNotInTranslation(translation, "return b != f;");
    // Right: weaker right-hand type, since Java compiler already type-checked.
    assertTranslation(translation, "return f == (id) b;");
    assertTranslation(translation, "return b != (id) f;");
    assertTranslation(translation, "IOSByteArray_Get(nil_chk(buffer), offset) != 'Z';");
  }

  public void testGenericArrayCast() throws IOException {
    String translation = translateSourceFile(
        "class Test<E> { E[] test(Object[] o) { E[] e = (E[]) new Object[0]; return (E[])o; } }",
        "Test", "Test.m");
    // No need to check either cast because the erasure of E[] is Object[].
    assertTranslation(translation,
        "IOSObjectArray *e = [IOSObjectArray arrayWithLength:0 type:NSObject_class_()];");
    assertTranslation(translation, "return o;");
    translation = translateSourceFile(
        "class Test<E extends String> { E[] test(Object[] o) { return (E[])o; } }",
        "Test", "Test.m");
    // Need to check the cast because the erasure of E[] is String[].
    assertTranslation(translation,
        "return (IOSObjectArray *) cast_check(o, IOSClass_arrayType(NSString_class_(), 1));");
  }

  public void testAssignmentCast() throws IOException {
    String translation = translateSourceFile(
        "class Test implements java.io.Serializable {"
        + " static class A<T extends java.io.Serializable> { T foo; }"
        + " void test (A<Test> a, Test t) { if (a != null) { t = a.foo; } } }", "Test", "Test.m");
    assertTranslation(translation, "t = ((Test *) a->foo_);");
  }

  public void testCastInConditionalExpression() throws IOException {
    String translation = translateSourceFile(
        "class Test implements java.io.Serializable {"
        + " static class A<T extends java.io.Serializable> { T foo; }"
        + " Test test (A<Test> a1, A<Test> a2, boolean b) {"
        + " if (a1 == null || a2 == null) return null; return b ? a1.foo : a2.foo; } }",
        "Test", "Test.m");
    assertTranslation(translation, "return b ? ((Test *) a1->foo_) : ((Test *) a2->foo_);");
  }

  public void testCastLocallyParameterizedType() throws IOException {
    addSourceFile("interface Foo<A> { A foo(); }", "Foo.java");
    addSourceFile("interface Bar<B extends Number> extends Foo<B> {}", "Bar.java");
    String translation = translateSourceFile(
        "class Test { Integer test(Bar<Integer> bar) { return bar.foo(); } }", "Test", "Test.m");
    // Needs the JavaLangInteger cast.
    assertTranslation(translation, "return ((JavaLangInteger *) [((id<Bar>) nil_chk(bar)) foo]);");
  }

  public void testCastInSuperFieldAccess() throws IOException {
    addSourceFile("class A <T> { T foo; }", "A.java");
    String translation = translateSourceFile("class Test extends A<String> {"
        + " int fooLength() { return super.foo.length(); } }", "Test", "Test.m");
    assertTranslation(translation, "(NSString *) nil_chk(foo_)");
  }
}
