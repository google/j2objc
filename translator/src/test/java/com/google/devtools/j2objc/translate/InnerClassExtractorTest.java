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

package com.google.devtools.j2objc.translate;

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.Options.MemoryManagementOption;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;

import java.io.IOException;
import java.util.List;

/**
 * Unit tests for {@link InnerClassExtractor}.
 *
 * @author Tom Ball
 */
public class InnerClassExtractorTest extends GenerationTest {

  @Override
  protected void setUp() throws IOException {
    super.setUp();
    // Reference counting by default, change for ARC-specific tests.
    Options.setMemoryManagementOption(MemoryManagementOption.REFERENCE_COUNTING);
  }

  protected List<AbstractTypeDeclaration> translateClassBody(String testSource) {
    String source = "public class Test { " + testSource + " }";
    CompilationUnit unit = translateType("Test", source);
    return unit.getTypes();
  }

  public void testSimpleInnerClass() throws IOException {
    String source = "public class A { class B { int test() { return o.hashCode(); }} Object o; }";
    String translation = translateSourceFile(source, "A", "A.h");
    assertTranslation(translation, "- (instancetype)initWithA:(A *)outer$;");
    translation = getTranslatedFile("A.m");
    assertTranslation(translation, "A *this$0_;");
    assertTranslation(translation, "[nil_chk(this$0_->o_) hash]");
    assertTranslation(translation, "A_B_set_this$0_(self, outer$);");
  }

  public void testWeakSimpleInnerClass() throws IOException {
    String source = "import com.google.j2objc.annotations.WeakOuter; "
        + "public class A { @WeakOuter class B { int test() { return o.hashCode(); }} Object o; }";
    String translation = translateSourceFile(source, "A", "A.m");
    assertTranslation(translation, "__weak A *this$0_;");
    assertTranslation(translation, "this$0_ = outer$;");
  }

  public void testWeakArcSimpleInnerClass() throws IOException {
    Options.setMemoryManagementOption(MemoryManagementOption.ARC);
    String source = "import com.google.j2objc.annotations.WeakOuter; "
        + "public class A { Object o;"
        + "  @WeakOuter class B { int test() { return o.hashCode(); } Object o2; }}";
    String translation = translateSourceFile(source, "A", "A.h");
    assertTranslation(translation, "id o_;");
    assertTranslation(translation, "id o2_;");
    translation = getTranslatedFile("A.m");
    assertTranslation(translation, "__weak A *this$0_;");
  }

  public void testInnerInnerClass() throws IOException {
    String source = "public class A { class B { "
        + "class C {int test() { return o.hashCode(); }}} Object o; }";
    String translation = translateSourceFile(source, "A", "A.h");
    assertTranslation(translation, "- (instancetype)initWithA:(A *)outer$;");
    assertTranslation(translation, "- (instancetype)initWithA_B:(A_B *)outer$;");
    translation = getTranslatedFile("A.m");
    assertTranslation(translation, "A *this$0_;");
    assertTranslation(translation, "A_B *this$0_;");
    assertTranslation(translation, "[nil_chk(this$0_->this$0_->o_) hash]");
  }

  public void testWeakInnerInnerClass() throws IOException {
    String source = "public class A { class B { "
        + "@com.google.j2objc.annotations.WeakOuter class C {"
        + "  int test() { return o.hashCode(); }}} Object o; }";
    String translation = translateSourceFile(source, "A", "A.m");
    assertTranslation(translation, "A *this$0_;");
    assertTranslation(translation, "__weak A_B *this$0_;");
    assertTranslation(translation, "[nil_chk(this$0_->this$0_->o_) hash]");
  }

  public void testInnerMethodAnonymousClass() throws IOException {
    String source = "public class A {"
        + "  abstract class C { public abstract void foo(); }"
        + "  class B { "
        + "    public void foo(final int j) {"
        + "      C r = new C() {"
        + "        public void foo() { int hash = j + o.hashCode(); }"
        + "      };"
        + "    }"
        + "  }"
        + "  Object o;"
        + "}";
    String translation = translateSourceFile(source, "A", "A.h");
    assertTranslation(translation, "- (instancetype)initWithA:(A *)outer$;");
    translation = getTranslatedFile("A.m");
    assertTranslatedLines(translation,
        "- (instancetype)initWithA_B:(A_B *)outer$",
        "withInt:(jint)capture$0;");
    assertTranslation(translation, "A *this$0_;");
    assertTranslation(translation, "A_B *this$1_;");
    assertTranslation(translation, "jint val$j_;");
    assertTranslatedLines(translation,
        "void A_B_$1_initWithA_B_withInt_(A_B_$1 *self, A_B *outer$, jint capture$0) {",
        "  A_B_$1_set_this$1_(self, outer$);",
        "  self->val$j_ = capture$0;",
        "  A_C_initWithA_(self, outer$->this$0_);",
        "}");
    assertTranslation(translation, "[nil_chk(this$1_->this$0_->o_) hash]");
  }

  /**
   * Verify that a static inner class is extracted.
   */
  public void testStaticInnerClass() throws IOException {
    String translation = translateSourceFile(
        "class Test { static class Foo { int i; Foo() { this(0); } Foo(int i) { this.i = i; } } }",
        "Test", "Test.h");
    assertTranslatedLines(translation,
        "@interface Test_Foo : NSObject {",
        "@public",
        "jint i_;",
        "}");

    translation = getTranslatedFile("Test.m");
    assertTranslatedLines(translation,
        "void Test_Foo_init(Test_Foo *self) {",
        "  Test_Foo_initWithInt_(self, 0);",
        "}");
    assertTranslatedLines(translation,
        "void Test_Foo_initWithInt_(Test_Foo *self, jint i) {",
        "  NSObject_init(self);",
        "  self->i_ = i;",
        "}");
  }

  /**
   * Verify that an inner class is moved to the compilation unit's types list.
   */
  public void testInnerClassExtracted() {
    List<AbstractTypeDeclaration> types = translateClassBody("class Foo { }");
    assertEquals(2, types.size());
    assertEquals("Test", types.get(0).getName().getIdentifier());
    assertEquals("Foo", types.get(1).getName().getIdentifier());
  }

  /**
   * Regression test: verify that references to class members of a type with
   * an inner class aren't disturbed.
   */
  public void testStaticMethodInvokingStaticMethodWithInnerClass() throws IOException {
    String translation = translateSourceFile(
        "class Test { public static int test(Object object) { return 0; }"
        + "public static int test(Object object, Object foo) {"
        + "  if (foo == null) { return Test.test(object); } return 1; } "
        + "private class Inner {} }", "Test", "Test.m");
    assertTranslation(translation, "return Test_testWithId_(object);");
  }

  public void testInnerClassInvokingExplicitOuterMethod() throws IOException {
    String translation = translateSourceFile(
        "class Test { public int size() { return 0; } "
        + "class Inner { int size() { return Test.this.size(); } } }", "Test", "Test.m");
    assertTranslation(translation, "Test_Inner_set_this$0_(self, outer$);");
    assertTranslation(translation, "return [this$0_ size];");
  }

  public void testInnerClassInvokingOuterMethod() throws IOException {
    String translation = translateSourceFile(
        "class Test { public int size() { return 0; } "
        + "class Inner { int getCount() { return size(); } } }", "Test", "Test.m");
    assertTranslation(translation, "return [this$0_ size];");
  }

  public void testInnerSubclassInvokingOuterMethod() throws IOException {
    String translation = translateSourceFile(
        "class Test { public int size() { return 0; } public void add(int n) {} class Inner {} "
        + "class Innermost { void test() { Test.this.add(size()); } } }", "Test", "Test.m");
    assertTranslation(translation, "Test_Innermost_set_this$0_(self, outer$);");
    assertTranslation(translation, "[this$0_ addWithInt:[this$0_ size]];");
  }

  public void testInnerClassDefaultInitialization() throws IOException {
    String translation = translateSourceFile(
        "class Test { Inner inner = new Inner(true); public int size() { return 0; }"
        + "class Inner { Inner(boolean b) {} int size() { return Test.this.size(); } } }",
        "Test", "Test.m");
    assertTranslation(translation, "Test_setAndConsume_inner_(self, "
        + "new_Test_Inner_initWithTest_withBoolean_(self, YES));");
    assertTranslation(translation, "Test_Inner_set_this$0_(self, outer$);");
  }

  public void testOuterClassAccessOuterVars() throws IOException {
    String translation = translateSourceFile(
        "class Test { int elementCount;"
        + "public Test() { elementCount = 0; }"
        + "private class Iterator { public void remove() { elementCount--; } } }",
        "Test", "Test.m");
    assertTranslatedLines(translation, "elementCount_ = 0;");
    assertTranslatedLines(translation,
        "- (void)remove {",
        "this$0_->elementCount_--;");
  }

  public void testOuterInterfaceMethodReference() throws IOException {
    String source = "class Test { "
        + "  interface Foo { void foo(); } "
        + "  abstract class Bar implements Foo { "
        + "    class Inner { Inner() { foo(); } } "
        + "    class Inner2 extends Inner { void bar() { foo(); } } "
        + "    Inner makeInner() { return new Inner(); } }"
        + "  public void test() { "
        + "    Bar bar = new Bar() { public void foo() { } };"
        + "    Bar.Inner inner = bar.new Inner(); } }";

    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation, "- (void)bar {\n  [this$1_ foo]");
    assertTranslation(translation,
        "- (Test_Bar_Inner *)makeInner {\n"
        + "  return [new_Test_Bar_Inner_initWithTest_Bar_(self) autorelease]");
    assertTranslation(translation, "[new_Test_Bar_Inner_initWithTest_Bar_(bar) autorelease];");
  }

  public void testMultipleThisReferences() throws IOException {
    String source =
        "class A { private int x = 0; "
        + "  interface Foo { void doSomething(); } "
        + "  class Inner { private int x = 1; "
        + "    public void blah() { "
        + "      new Foo() { public void doSomething() { "
        + "      Inner.this.x = 2; A.this.x = 3; }}; }}}";
    CompilationUnit unit = translateType("A", source);
    List<AbstractTypeDeclaration> types = unit.getTypes();
    assertEquals(4, types.size());

    String translation = translateSourceFile(source, "A", "A.m");
    // Anonymous class constructor in Inner.blah()
    assertTranslation(translation, "new_A_Inner_$1_initWithA_Inner_(self)");
    // A.Inner.x referred to in anonymous Foo
    assertTranslation(translation, "this$0_->x_ = 2");
    // A.x referred to in anonymous Foo
    assertTranslation(translation, "this$0_->this$0_->x_ = 3");
    // A.Inner init in anonymous Foo's constructor
    assertTranslation(translation, "A_Inner_set_this$0_(self, outer$)");
  }

  /**
   * This test differs from the last one only in the addition of another
   * 'this' reference before the anonymous class creation.
   */
  public void testMultipleThisReferencesWithPreviousReference() throws IOException {
    String source =
        "class A { private int x = 0; "
        + "  interface Foo { void doSomething(); } "
        + "  class Inner { private int x = 1; "
        + "    public void blah() { "
        + "      A.this.x = 2; "
        + "      new Foo() { public void doSomething() { "
        + "      Inner.this.x = 3; A.this.x = 4; }}; }}}";
    CompilationUnit unit = translateType("A", source);
    List<AbstractTypeDeclaration> types = unit.getTypes();
    assertEquals(4, types.size());

    String translation = translateSourceFile(source, "A", "A.m");
    // Anonymous class constructor in Inner.blah()
    assertTranslation(translation, "new_A_Inner_$1_initWithA_Inner_(self)");
    // A.x referred to in A.Inner.
    assertTranslation(translation, "this$0_->x_ = 2");
    // A.Inner.x referred to in anonymous Foo.
    assertTranslation(translation, "this$0_->x_ = 3");
    // A.x referred to in anonymous Foo
    assertTranslation(translation, "this$0_->this$0_->x_ = 4");
    // A.Inner init in anonymous Foo's constructor
    assertTranslation(translation, "A_Inner_set_this$0_(self, outer$)");
  }

  public void testOuterMethodReference() throws IOException {
    String source = "class Test { "
        + "  interface Foo { void foo(); } "
        + "  class Inner { "
        + "    void bar() { "
        + "      final int x = 0; final int y = 0; "
        + "      Foo foo = new Foo() { "
        + "        public void foo() { if (x ==0) mumble(y); } }; } }"
        + "  private void mumble(int y) { } }";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation, "Test_mumbleWithInt_(this$0_->this$0_, 0)");
  }

  public void testInnerSubClassOfGenericClassInner() throws IOException {
    String source = "class Test { "
        + "class A<E extends A<E>.Inner> { public class Inner { } } "
        + "class B extends A<B.BInner> { public class BInner extends A<B.BInner>.Inner { } } }";
    String translation = translateSourceFile(source, "Test", "Test.h");

    assertTranslation(translation, "@interface Test_B_BInner : Test_A_Inner");
  }

  public void testGenericInnerSubClassOfGenericClassGenericInner() throws IOException {
    String source = "class Test<E> { "
        + "class A<E> { } class B<E> extends A<E> { B(int i) { } } }";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation,
        "- (instancetype)initWithTest:(Test *)outer$\n"
        + "                     withInt:(jint)i");
  }

  public void testInnerSubClassOfOtherInnerWithOuterRefs() throws IOException {
    String source = "class Test { "
        + "class A { "
        + "  public void foo() { } "
        + "  public class Inner { void test() { foo(); } } } "
        + "class B extends A { "
        + "  public class BInner extends A.Inner { void test() { foo(); } } } "
        + "    public static void main(String[] args) { B b = new Test().new B(); }}";
    String translation = translateSourceFile(source, "Test", "Test.m");

    // Check that outer fields are added to A.Inner and B.BInner.
    assertTranslation(translation, "@interface Test_A_Inner () {\n @public\n  Test_A *this$0_;");
    assertTranslation(translation, "@interface Test_B_BInner () {\n @public\n  Test_B *this$1_;");

    // Check that B has a constructor that correctly calls constructor of A
    // with right outer.
    assertTranslatedLines(translation,
        "void Test_B_initWithTest_(Test_B *self, Test *outer$) {",
        "  Test_A_initWithTest_(self, outer$);",
        "}");
  }

  public void testInnerClassQualifiedAndUnqualfiedOuterReferences() throws IOException {
    String source = "class Test { "
        + "  public int i = 0; "
        + "  class Inner { "
        + "    void foo(int i) { Test.this.i = i; } "
        + "    void bar() { int j = i; } } }";
    String translation = translateSourceFile(source, "Test", "Test.m");

    assertTranslation(translation, "- (void)fooWithInt:(jint)i {\n  this$0_->i_ =");
    assertTranslation(translation, "- (void)bar {\n  jint j = this$0_->i_");
  }

  public void testInnerClassExtendsAnotherInner() throws IOException {
    String translation = translateSourceFile(
        "class Test { "
        + "  Integer i = 1; "
        + "  class Inner1 { } "
        + "  class Inner2 extends Inner1 { "
        + "    int j = 1; "
        + "    public int foo() { return i + j; } } }",
        "Test", "Test.m");
    assertTranslation(translation, "Test *this$1");  // Inner2's outer reference.
    assertTranslation(translation, "[((JavaLangInteger *) nil_chk(this$1_->i_)) intValue] + j_");
  }

  public void testInnerClassInstantiatesAnotherInner() throws IOException {
    String translation = translateSourceFile(
        "class Test { "
        + "  Integer i = 1; "
        + "  class Inner1 { public int foo() { return i + 1; } } "
        + "  class Inner2 { Inner1 inner1 = new Inner1(); } }",
        "Test", "Test.m");
    assertTranslation(translation, "new_Test_Inner1_initWithTest_(outer$)");

    translation = getTranslatedFile("Test.h");
    assertTranslation(translation,
        "@interface Test_Inner2 : NSObject {\n"
        + " @public\n"
        + "  Test_Inner1 *inner1_;");
  }

  public void testInnerClassWithInnerSuperClass() throws IOException {
    String translation = translateSourceFile(
        "class Test { "
        + "  class Inner1 { public Inner1(int n) { } } "
        + "  class Inner2 extends Inner1 { public Inner2(int n, long l) { super(n); } } }",
        "Test", "Test.m");
    assertTranslation(translation, "Test_Inner1_initWithTest_withInt_(self, outer$, n);");
  }

  public void testInnerSubClassOfOtherInnerWithOuterRefsExtraction() throws IOException {
    String source = "public class Test { "
        + "int i; "
        + "class A { "
        + "  private void foo() { i++; } "
        + "  public class Inner { Inner() { foo(); } } } "
        + "class B extends A { "
        + "  public class BInner extends A.Inner { } } "
        + "public static void main(String[] args) { B b = new Test().new B(); }}";
    String translation = translateSourceFile(source, "Test", "Test.m");

    // Verify that B's translation has the Test field declared.
    assertTranslation(translation, "Test *this$0_;");

    // Verify that A has a Test field (this$0).
    assertTranslatedLines(translation,
        "@interface Test_A () {",
        "@public",
        "Test *this$0_;",
        "}");

    // Verify that B does not have a Test field.
    assertNotInTranslation(translation, "@interface Test_B ()");

    // Verify that main method creates a new instanceof B associated with
    // a new instance of Test.
    assertTranslatedLines(translation,
        "void Test_mainWithNSStringArray_(IOSObjectArray *args) {",
        "Test_initialize();",
        "Test_B *b = [new_Test_B_initWithTest_([new_Test_init() autorelease]) autorelease];");

    // Verify that BInner's constructor takes a B instance and correctly calls
    // the super constructor.
    assertTranslatedLines(translation,
        "void Test_B_BInner_initWithTest_B_(Test_B_BInner *self, Test_B *outer$) {",
        "  Test_A_Inner_initWithTest_A_(self, outer$);",
        "}");
  }

  // Identical sample code to above test, except the order of B and A is switched.
  public void testInnerSubClassOfOtherInnerWithOuterRefsExtraction2() throws IOException {
    String source = "public class Test { "
        + "int i; "
        + "class B extends A { "
        + "  public class BInner extends A.Inner { } } "
        + "class A { "
        + "  private void foo() { i++; } "
        + "  public class Inner { Inner() { foo(); } } } "
        + "public static void main(String[] args) { B b = new Test().new B(); }}";

    // Verify that B's translation has the Test field declared.
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation, "Test *this$0_;");

    // Verify that A has a Test field (this$0).
    assertTranslatedLines(translation,
        "@interface Test_A () {",
        "@public",
        "Test *this$0_;",
        "}");

    // Verify that B does not have a Test field.
    assertNotInTranslation(translation, "@interface Test_B () {");

    // Verify that main method creates a new instanceof B associated with
    // a new instance of Test.
    assertTranslatedLines(translation,
        "void Test_mainWithNSStringArray_(IOSObjectArray *args) {",
        "Test_initialize();",
        "Test_B *b = [new_Test_B_initWithTest_([new_Test_init() autorelease]) autorelease];");

    // Verify that BInner's constructor takes a B instance and correctly calls
    // the super constructor.
    assertTranslatedLines(translation,
        "void Test_B_BInner_initWithTest_B_(Test_B_BInner *self, Test_B *outer$) {",
        "  Test_A_Inner_initWithTest_A_(self, outer$);",
        "}");
  }

  // Identical sample code to above test, except A is a generic class.
  public void testInnerSubClassOfOtherInnerWithOuterRefsWithGenerics() throws IOException {
    String source = "public class Test { "
        + "class B extends A<B.BInner> { "
        + "  public class BInner extends A.Inner { BInner() { super(null); } } } "
        + "class A<T extends A<T>.Inner> { "
        + "  private void foo() { } "
        + "  public class Inner { Inner(T t) { foo(); } } } "
        + "public static void main(String[] args) { B b = new Test().new B(); }}";
    String translation = translateSourceFile(source, "Test", "Test.m");

    // Make sure that the call to super(null) in B.BInner's constructor
    // is translated with the right keyword for the generic second parameter.
    assertTranslation(translation,
        "Test_A_Inner_initWithTest_A_withTest_A_Inner_(self, outer$, nil);");
  }

  public void testStaticImportReferenceInInnerClass() throws IOException {
    String translation = translateSourceFile(
        "import static java.lang.Character.isDigit; public class Test { class Inner { "
        + "  public void foo() { boolean b = isDigit('c'); } } }",
        "Test", "Test.m");
    assertTranslation(translation, "JavaLangCharacter_isDigitWithChar_('c')");
  }

  public void testStaticReferenceInInnerClass() throws IOException {
    String translation = translateSourceFile(
        "public class Test { public static void foo() { } class Inner { "
        + "  public void bar() { foo(); } } }",
        "Test", "Test.m");
    assertTranslation(translation, "Test_foo()");
  }

  public void testMethodInnerClass() throws IOException {
    String source = "public class A { void foo() { class MyRunnable implements Runnable {"
        + "public void run() {} }}}";
    String translation = translateSourceFile(source, "A", "A.m");
    assertTranslation(translation, "@interface A_1MyRunnable : NSObject < JavaLangRunnable >");
    assertNotInTranslation(translation, "A *this");
  }

  public void testInnerClassConstructor() throws IOException {
    String source = "public class A { class B { Object test() { return new B(); }}}";
    String translation = translateSourceFile(source, "A", "A.m");
    assertTranslation(translation, "return [new_A_B_initWithA_(this$0_) autorelease];");
  }

  public void testMethodInnerClassWithSameName() throws IOException {
    String source = "public class A { class MyClass {} void foo() { class MyClass {}}}";
    String translation = translateSourceFile(source, "A", "A.h");
    assertTranslation(translation, "@interface A_MyClass");
    translation = getTranslatedFile("A.m");
    assertTranslation(translation, "@interface A_1MyClass");
  }

  public void testOuterThisReferenceInInner() throws IOException {
    String translation = translateSourceFile(
        "class Test { "
        + "  class Inner { Inner(int i) { } Inner foo() { return new Inner(1); } } "
        + "  public Inner bar() { return new Inner(2); } }",
        "Test", "Test.m");
    assertTranslation(translation, "new_Test_Inner_initWithTest_withInt_(this$0_, 1)");
    assertTranslation(translation, "new_Test_Inner_initWithTest_withInt_(self, 2)");
  }

  public void testInnerThisReferenceInInnerAsFieldAccess() throws IOException {
    String translation = translateSourceFile(
        "class Test { "
        + "  class Inner { int i = 0; Inner() { Inner.this.i = 1; } } }",
        "Test", "Test.m");
    assertTranslation(translation, "self->i_ = 1");
  }

  public void testInnerThisReferenceInInnerAsThisExpression() throws IOException {
    String translation = translateSourceFile(
        "class Test { "
        + "  static void foo(Inner i) { } "
        + "  class Inner { Inner() { foo(Inner.this); } } }",
        "Test", "Test.m");
    assertTranslation(translation, "Test_fooWithTest_Inner_(self)");
  }

  // Verify that an anonymous class in a static initializer does not reference
  // instance.
  public void testNoOuterInStaticInitializer() throws IOException {
    String source = "import java.util.*; "
        + "public class A { static { foo(new Enumeration() { "
        + "    public boolean hasMoreElements() { return false; }"
        + "    public Object nextElement() { return null; }}); }"
        + "  public static void foo(Object o) { } }";
    String translation = translateSourceFile(source, "A", "A.h");
    assertNotInTranslation(translation, "this$0_");
    translation = getTranslatedFile("A.m");
    assertNotInTranslation(translation, "this$0_");
    assertTranslation(translation, "A_fooWithId_([new_A_$1_init() autorelease])");
  }

  // Verify that an anonymous class assigned to a static field does not
  // reference instance.
  public void testNoOuterWhenAssignedToStaticField() throws IOException {
    String source = "import java.util.*; "
        + "public class A { static Enumeration test = new Enumeration() { "
        + "    public boolean hasMoreElements() { return false; }"
        + "    public Object nextElement() { return null; }}; }";
    String translation = translateSourceFile(source, "A", "A.h");
    assertFalse(translation.contains("this$0_"));
    translation = getTranslatedFile("A.m");
    assertFalse(translation.contains("this$0_"));
    assertTranslation(translation,
        "JreStrongAssignAndConsume(&A_test_, nil, new_A_$1_init());");
  }

  // Verify that an anonymous class in a static method does not reference
  // instance.
  public void testNoOuterWhenInStaticMethod() throws IOException {
    String source = "import java.util.*; "
        + "public class A { static Enumeration test(Collection collection) { "
        + "  final Collection c = collection; "
        + "  return new Enumeration() { "
        + "    Iterator it = c.iterator(); "
        + "    public boolean hasMoreElements() { return it.hasNext(); }"
        + "    public Object nextElement() { return it.next(); }}; }}";
    String translation = translateSourceFile(source, "A", "A.m");
    assertFalse(translation.contains("this$0_"));
    assertTranslation(translation,
        "- (instancetype)initWithJavaUtilCollection:(id<JavaUtilCollection>)capture$0;");
    assertTranslation(translation, "id<JavaUtilCollection> val$c_;");
    assertFalse(translation.contains("this$0_"));
    assertTranslation(translation,
        "return [new_A_$1_initWithJavaUtilCollection_(c) autorelease];");
    assertTranslation(translation,
        "- (instancetype)initWithJavaUtilCollection:(id<JavaUtilCollection>)capture$0 {");
  }

  public void testInnerAccessingOuterArrayLength() throws IOException {
    String source = "public class A<E> { transient E[] elements; "
        + "private class B implements java.util.Iterator<E> { "
        + "public boolean hasNext() { return elements.length > 0; } "
        + "public E next() { return null; }"
        + "public void remove() {} }}";
    String translation = translateSourceFile(source, "A", "A.m");
    assertTranslation(translation, "- (instancetype)initWithA:(A *)outer$;");
    assertTranslation(translation, "A *this$0_;");
    assertTranslation(translation, "((IOSObjectArray *) nil_chk(this$0_->elements_))->size_");
  }

  public void testCreateInnerClassOfSuperclass() throws IOException {
    String source = "class B {\n"
        + "  class C {}\n"
        + "}\n"
        + "class A extends B {\n"
        + "  void foo() { new C(); }\n"
        + "}\n";
    String translation = translateSourceFile(source, "A", "A.m");
    assertTranslation(translation, "new_B_C_initWithB_(self)");
  }

  public void testCallInnerConstructorOfParameterizedOuterClass() throws IOException {
    String outerSource = "abstract class Outer<T> { class Inner { public Inner(T t) {} }}";
    String callerSource =
        "class A extends Outer<String> { public void foo() { new Inner(\"test\"); } }";
    addSourceFile(outerSource, "Outer.java");
    addSourceFile(callerSource, "A.java");
    String translation = translateSourceFile("A", "A.m");
    assertTranslation(translation,
        "[new_Outer_Inner_initWithOuter_withId_(self, @\"test\") autorelease];");
  }

  public void testNoOuterFieldAssignmentWhenCallingOtherConstructor() throws IOException {
    String source = "class Outer { class Inner { Inner(int i) {} Inner() { this(42); } } }";
    String translation = translateSourceFile(source, "Outer", "Outer.m");
    assertTranslatedLines(translation,
        "void Outer_Inner_initWithOuter_(Outer_Inner *self, Outer *outer$) {",
        "  Outer_Inner_initWithOuter_withInt_(self, outer$, 42);",
        "}");
  }

  public void testListArgsInEnumConstantDeclaration() throws IOException {
    String source =
        "class Outer { "
        + "  enum Inner { "
        + "    A(new String[] { \"1\", \"2\", \"3\" }), "
        + "    B(new String[] { \"4\", \"5\", \"6\" }); "
        + "    Inner(String[] values) {} "
        + "  } "
        + "}";
    String translation = translateSourceFile(source, "Outer", "Outer.m");
    assertTranslation(translation, "[IOSObjectArray arrayWithObjects:(id[]){ "
        + "@\"1\", @\"2\", @\"3\" } count:3 type:NSString_class_()]");
    assertTranslation(translation, "[IOSObjectArray arrayWithObjects:(id[]){ "
        + "@\"4\", @\"5\", @\"6\" } count:3 type:NSString_class_()]");
  }

  public void testInnerClassVarargsConstructor() throws IOException {
    String translation = translateSourceFile(
        "class Test { class Inner { Inner(int... i) {} } void test() { new Inner(1, 2, 3); } }",
        "Test", "Test.m");
    assertTranslation(translation,
        "new_Test_Inner_initWithTest_withIntArray_(self, "
        + "[IOSIntArray arrayWithInts:(jint[]){ 1, 2, 3 } count:3])");
  }

  public void testInnerClassConstructedInSuperConstructorInvocation() throws IOException {
    String translation = translateSourceFile(
        "class Outer { "
        + "  class Inner1 { } "
        + "  class Inner2Super { Inner2Super(Inner1 i) { } } "
        + "  class Inner2 extends Inner2Super { "
        + "    Inner2() { "
        + "      super(new Inner1()); "
        + "    } "
        + "  } "
        + "}", "Outer", "Outer.m");

    assertTranslation(translation, "new_Outer_Inner1_initWithOuter_(outer$)");
  }

  public void testOuterReferenceInSuperConstructorInvocation() throws IOException {
    String translation = translateSourceFile(
        "class Outer { "
        + "  int foo; "
        + "  class Inner1 { Inner1(int i) { } } "
        + "  class Inner2 extends Inner1 { "
        + "    Inner2() { "
        + "      super(foo); "
        + "    } "
        + "  } "
        + "}", "Outer", "Outer.m");

    assertTranslation(translation,
        "Outer_Inner1_initWithOuter_withInt_(self, outer$, outer$->foo_);");
  }

  public void testOuterThisReferenceInSuperConstructorInvocation() throws IOException {
    String translation = translateSourceFile(
        "class Outer { "
        + "  int foo; "
        + "  class Outer1 { "
        + "    int foo; "
        + "    class Inner1 { Inner1(int i) { } } "
        + "    class Inner2 extends Inner1 { "
        + "      Inner2() { "
        + "        super(Outer.this.foo); "
        + "      } "
        + "    } "
        + "  } "
        + "}", "Outer", "Outer.m");

    assertTranslation(translation,
        "Outer_Outer1_Inner1_initWithOuter_Outer1_withInt_(self, outer$, outer$->this$0_->foo_);");
  }

  public void testAnonymousClassWithinTypeDeclarationStatement() throws IOException {
    String translation = translateSourceFile(
        "class Test { Runnable foo() { class MyRunnable implements Runnable { "
        + "public void run() { Runnable r = new Runnable() { public void run() {} }; } } "
        + "return new MyRunnable(); } }", "Test", "Test.m");
    assertOccurrences(translation, "@interface Test_1MyRunnable_$1", 1);
  }

  public void testOuterInitializedBeforeSuperInit() throws IOException {
    String translation = translateSourceFile(
        "class Test { int i; class Inner { void test() { i++; } } }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "void Test_Inner_initWithTest_(Test_Inner *self, Test *outer$) {",
        "  Test_Inner_set_this$0_(self, outer$);",
        "  NSObject_init(self);",
        "}");
  }

  public void testInnerClassOuterStackReference() throws IOException {
    String translation = translateSourceFile(
        "public class A { "
        + "  void test() { "
        + "    final Object obj = new Object(); "
        + "    class TestThread extends Thread { "
        + "      public void run() { "
        + "        System.out.println(obj); }}}}",
        "A", "A.m");
    assertTranslation(translation, "printlnWithId:val$obj_");
    assertTranslation(translation, "id val$obj_;");
  }

  public void testLocalClassWithCaptureVariables() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test(final String s) { "
        + "  class Inner { "
        + "    Inner() { this(0); } "
        + "    Inner(int i) { } "
        + "    void foo() { s.toString(); } "
        + "  } "
        + "  new Inner(); } }",
        "Test", "Test.m");
    assertTranslation(translation, "new_Test_1Inner_initWithNSString_(s)");
    assertTranslatedLines(translation,
        "void Test_1Inner_initWithNSString_(Test_1Inner *self, NSString *capture$0) {",
        "  Test_1Inner_initWithNSString_withInt_(self, capture$0, 0);",
        "}");
    assertTranslatedLines(translation,
        "void Test_1Inner_initWithNSString_withInt_("
          + "Test_1Inner *self, NSString *capture$0, jint i) {",
        "  Test_1Inner_set_val$s_(self, capture$0);",
        "  NSObject_init(self);",
        "}");
  }

  public void testWeakStaticClass() throws IOException {
    String source = "import com.google.j2objc.annotations.WeakOuter; "
        + "public class A { @WeakOuter static class B {}}";
    String translation = translateSourceFile(source, "A", "A.h");
    assertWarningCount(1);
    assertErrorCount(0);
    assertNotInTranslation(translation, "__weak");
  }

  public void testInnerClassWithVarargsAndCaptureVariables() throws IOException {
    String translation = translateSourceFile(
        "class Test { int test(final int i, Object o) { class Inner { Inner(Object... o) {} "
        + "int foo() { return i; } } return new Inner(o).foo(); } }", "Test", "Test.m");
    assertTranslation(translation,
        "new_Test_1Inner_initWithInt_withNSObjectArray_(i, "
        + "[IOSObjectArray arrayWithObjects:(id[]){ o } count:1 type:NSObject_class_()])");
    assertTranslation(translation,
        "void Test_1Inner_initWithInt_withNSObjectArray_("
        + "Test_1Inner *self, jint capture$0, IOSObjectArray *o)");
  }
}
