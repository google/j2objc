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

import java.io.IOException;

/**
 * Unit tests for {@link OuterReferenceFixer}.
 *
 * @author Keith Stanger
 */
public class OuterReferenceFixerTest extends GenerationTest {

  public void testSuperConstructorExpression() throws IOException {
    addSourceFile("class A { class Inner { } }", "A.java");
    String translation = translateSourceFile(
        "class B extends A.Inner { B(A a) { a.super(); } }", "B", "B.m");
    assertTranslation(translation, "A_Inner_initWithA_(self, nil_chk(a));");
  }

  public void testLocalClassCaptureVariablesInsideGenericClass() throws IOException {
    String translation = translateSourceFile(
        "class Test<T> { void test() { final Object o = null; class Inner { "
        + "public void foo() { o.toString(); } } new Inner(); } }", "Test", "Test.m");
    assertTranslation(translation, "create_Test_1Inner_initWithId_(o)");
  }

  public void testRecursiveConstructionOfLocalClass() throws IOException {
    String translation = translateSourceFile(
        "public class Test { void test(final Object bar) { "
        + "class Foo { void foo() { bar.toString(); new Foo(); } } } }", "Test", "Test.m");
    assertTranslation(translation, "create_Test_1Foo_initWithId_(val$bar_)");
  }

  public void testLocalClassExtendsLocalClassCapturesVariables() throws IOException {
    String translation = translateSourceFile(
        "public class Test { void test(final int i, final int j) { "
        + "class A { int sum() { return i + j; } }; class B extends A {} } }", "Test", "Test.m");
    // Local class B must also capture the locals and pass them to A's constructor.
    assertTranslatedLines(translation,
        "void Test_1B_initWithInt_withInt_(Test_1B *self, jint capture$0, jint capture$1) {",
        "  Test_1A_initWithInt_withInt_(self, capture$0, capture$1);",
        "}");
  }
}
