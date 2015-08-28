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
        + "  String b = \"foo\" + a.hashCode() + \"bar\" + a.length() + \"baz\"; } }",
        "Test", "Test.m");
    assertTranslation(translation,
        "JreStrcat(\"$I$I$\", @\"foo\", ((jint) [a hash]), @\"bar\", ((jint) [a length]),"
          + " @\"baz\")");
  }

  public void testCastInConstructorChain() throws IOException {
    String source = "int i = new Throwable().hashCode();";
    List<Statement> stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("jint i = ((jint) [((JavaLangThrowable *) "
        + "[new_JavaLangThrowable_init() autorelease]) hash]);", result);
  }

  // b/5872710: generic return type needs to be cast if chaining invocations.
  public void testTypeVariableCast() throws IOException {
    String translation = translateSourceFile(
      "import java.util.ArrayList; public class Test {"
      + "  int length; static ArrayList<String> strings = new ArrayList<String>();"
      + "  public static void main(String[] args) { int n = strings.get(1).length(); }}",
      "Test", "Test.m");
    assertTranslation(translation, "((jint) [((NSString *) "
      + "nil_chk([((JavaUtilArrayList *) nil_chk(Test_strings_)) getWithInt:1])) length]);");
  }

  // Verify that String.length() and Object.hashCode() return values are cast when used.
  public void testStringLengthCompare() throws IOException {
    String translation = translateSourceFile(
        "public class Test { boolean test(String s) { return -2 < \"1\".length(); }"
        + "  void test2(Object o) { o.hashCode(); }"
        + "  int test3() { return super.hashCode(); } }",
        "Test", "Test.m");
    // Verify referenced return value is cast.
    assertTranslation(translation, "return -2 < ((jint) [@\"1\" length]);");
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
    assertTranslation(translation, "[((Test_DerivedFoo *) foo_) derivedMethod];");
    // Verify that a cast can be added to a QualifiedName node.
    assertTranslation(translation, "return ((Test_DerivedFoo *) foo_)->myInt_;");
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
        "return ((Test_Node *) (JreStrongAssign(&Test_next_, [self getNext])))->key_;");
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
        "  [((Test_C<Test_I> *) nil_chk([self genericMethodWithId:[new_Test_Bar_init() autorelease]"
          + " withId:[new_Test_Baz_init() autorelease]])) foo];",
        // No need for a cast because genericMethodWithId:withId: is declared to return "id".
        "  return [self genericMethodWithId:[new_Test_Bar_init() autorelease]"
          + " withId:[new_Test_Baz_init() autorelease]];",
        "}");
  }
}
