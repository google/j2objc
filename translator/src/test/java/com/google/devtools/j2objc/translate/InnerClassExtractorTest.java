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

import com.google.common.collect.Maps;
import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.Options.MemoryManagementOption;
import com.google.devtools.j2objc.gen.SourceBuilder;
import com.google.devtools.j2objc.gen.SourcePosition;
import com.google.devtools.j2objc.gen.StatementGenerator;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ASTUtil;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for {@link InnerClassExtractor}.
 *
 * @author Tom Ball
 */
public class InnerClassExtractorTest extends GenerationTest {
  // TODO(user): update bug id in comments to public issue numbers when
  // issue tracking is sync'd.

  @Override
  protected void setUp() throws IOException {
    super.setUp();
    // Reference counting by default, change for ARC-specific tests.
    Options.setMemoryManagementOption(MemoryManagementOption.REFERENCE_COUNTING);
  }

  protected List<AbstractTypeDeclaration> translateClassBody(String testSource) {
    String source = "public class Test { " + testSource + " }";
    CompilationUnit unit = translateType("Test", source);
    return ASTUtil.getTypes(unit);
  }

  public void testSimpleInnerClass() throws IOException {
    String source = "public class A { class B { int test() { return o.hashCode(); }} Object o; }";
    String translation = translateSourceFile(source, "A", "A.h");
    assertTranslation(translation, "A *this$0_;");
    assertTranslation(translation, "- (id)initWithA:(A *)outer$;");
    translation = getTranslatedFile("A.m");
    assertTranslation(translation, "[nil_chk(this$0_->o_) hash]");
    assertTranslation(translation, "A_B_set_this$0_(self, outer$);");
  }

  public void testWeakSimpleInnerClass() throws IOException {
    String source = "import com.google.j2objc.annotations.WeakOuter; " +
        "public class A { @WeakOuter class B { int test() { return o.hashCode(); }} Object o; }";
    String translation = translateSourceFile(source, "A", "A.h");
    assertTranslation(translation, "__weak A *this$0_;");
    translation = getTranslatedFile("A.m");
    assertTranslation(translation, "this$0_ = outer$;");
  }

  public void testWeakArcSimpleInnerClass() throws IOException {
    Options.setMemoryManagementOption(MemoryManagementOption.ARC);
    String source = "import com.google.j2objc.annotations.WeakOuter; " +
        "public class A { Object o;" +
        "  @WeakOuter class B { int test() { return o.hashCode(); } Object o2; }}";
    String translation = translateSourceFile(source, "A", "A.h");
    assertTranslation(translation, "id o_;");
    assertTranslation(translation, "id o2_;");
    assertTranslation(translation, "__weak A *this$0_;");
  }

  public void testInnerInnerClass() throws IOException {
    String source = "public class A { class B { " +
        "class C {int test() { return o.hashCode(); }}} Object o; }";
    String translation = translateSourceFile(source, "A", "A.h");
    assertTranslation(translation, "A *this$0_;");
    assertTranslation(translation, "- (id)initWithA:(A *)outer$;");
    assertTranslation(translation, "A_B *this$0_;");
    assertTranslation(translation, "- (id)initWithA_B:(A_B *)outer$;");
    translation = getTranslatedFile("A.m");
    assertTranslation(translation, "[nil_chk(this$0_->this$0_->o_) hash]");
  }

  public void testWeakInnerInnerClass() throws IOException {
    String source = "public class A { class B { " +
        "@com.google.j2objc.annotations.WeakOuter class C {" +
        "  int test() { return o.hashCode(); }}} Object o; }";
    String translation = translateSourceFile(source, "A", "A.h");
    assertTranslation(translation, "A *this$0_;");
    assertTranslation(translation, "__weak A_B *this$0_;");
    translation = getTranslatedFile("A.m");
    assertTranslation(translation, "[nil_chk(this$0_->this$0_->o_) hash]");
  }

  public void testInnerMethodAnonymousClass() throws IOException {
    String source = "public class A {" +
        "  abstract class C { public abstract void foo(); }" +
        "  class B { " +
        "    public void foo(final int j) {" +
        "      C r = new C() {" +
        "        public void foo() { int hash = j + o.hashCode(); }" +
        "      };" +
        "    }" +
        "  }" +
        "  Object o;" +
        "}";
    String translation = translateSourceFile(source, "A", "A.h");
    assertTranslation(translation, "A *this$0_;");
    assertTranslation(translation, "- (id)initWithA:(A *)outer$;");
    assertTranslation(translation, "A_B *this$1_;");
    assertTranslation(translation, "int val$j_;");
    assertTranslation(translation,
        "- (id)initWithA_B:(A_B *)outer$\n" +
        "          withInt:(int)capture$0;");
    translation = getTranslatedFile("A.m");
    assertTranslation(translation, "[super initWithA:outer$->this$0_]");
    assertTranslation(translation, "[nil_chk(this$1_->this$0_->o_) hash]");
  }

  /**
   * Verify that a static inner class is extracted.
   */
  public void testStaticInnerClass() {
    List<AbstractTypeDeclaration> types = translateClassBody(
        "static class Foo { int i; Foo() { this(0); } Foo(int i) { this.i = i; } }");
    assertEquals(2, types.size());
    List<BodyDeclaration> classMembers = ASTUtil.getBodyDeclarations(types.get(0));
    assertTrue(classMembers.size() == 1);
    AbstractTypeDeclaration innerClass = types.get(1);
    assertEquals(4, innerClass.bodyDeclarations().size());
    List<BodyDeclaration> members = ASTUtil.getBodyDeclarations(innerClass);

    FieldDeclaration field = (FieldDeclaration) members.get(0);
    assertEquals("int", field.getType().toString());

    MethodDeclaration method = (MethodDeclaration) members.get(1);
    assertTrue(method.isConstructor());
    assertTrue(method.parameters().isEmpty());
    assertEquals(1, method.getBody().statements().size());
    ConstructorInvocation stmt = (ConstructorInvocation) method.getBody().statements().get(0);
    assertEquals(1, stmt.arguments().size());


    method = (MethodDeclaration) members.get(2);
    assertTrue(method.isConstructor());
    assertEquals(1, method.parameters().size());
    assertEquals(2, method.getBody().statements().size());
    ExpressionStatement expr = (ExpressionStatement) method.getBody().statements().get(1);
    assertTrue(expr.getExpression() instanceof Assignment);
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
  public void testStaticMethodInvokingStaticMethodWithInnerClass() {
    List<AbstractTypeDeclaration> types = translateClassBody(
        "public static int test(Object object) { return 0; }" +
        "public static int test(Object object, Object foo) {" +
        "  if (foo == null) { return Test.test(object); } return 1; } " +
        "private class Inner {}");
    assertEquals(2, types.size());

    final int[] testsFound = { 0 };
    types.get(0).accept(new ASTVisitor() {
      @Override
      public void endVisit(MethodInvocation node) {
        // Verify that Test.test() wasn't translated to this$0.test().
        assertEquals("Test", node.getExpression().toString());
        ++testsFound[0];
      }
    });
    assertEquals(1, testsFound[0]);
  }

  public void testInnerClassInvokingExplicitOuterMethod() throws IOException {
    String translation = translateSourceFile(
      "class Test { public int size() { return 0; } " +
      "class Inner { int size() { return Test.this.size(); } } }", "Test", "Test.m");
    assertTranslation(translation, "Test_Inner_set_this$0_(self, outer$);");
    assertTranslation(translation, "return [this$0_ size];");
  }

  public void testInnerClassInvokingOuterMethod() {
    List<AbstractTypeDeclaration> types = translateClassBody(
      "public int size() { return 0; } " +
      "class Inner { int getCount() { return size(); }}");
    assertEquals(2, types.size());

    final int[] testsFound = { 0 };
    types.get(1).accept(new ASTVisitor() {
      @Override
      public void endVisit(ReturnStatement node) {
        assertEquals("return this$0.size();", node.toString().trim());
        ++testsFound[0];
      }
    });
    assertEquals(1, testsFound[0]);
  }

  public void testInnerSubclassInvokingOuterMethod() throws IOException {
    String translation = translateSourceFile(
      "class Test { public int size() { return 0; } public void add(int n) {} class Inner {} " +
      "class Innermost { void test() { Test.this.add(size()); } } }", "Test", "Test.m");
    assertTranslation(translation, "Test_Innermost_set_this$0_(self, outer$);");
    assertTranslation(translation, "[this$0_ addWithInt:[this$0_ size]];");
  }

  public void testInnerClassDefaultInitialization() throws IOException {
    String translation = translateSourceFile(
        "class Test { Inner inner = new Inner(true); public int size() { return 0; }" +
        "class Inner { Inner(boolean b) {} int size() { return Test.this.size(); } } }",
        "Test", "Test.m");
    assertTranslation(translation, "Test_set_inner_(self, " +
        "[[[Test_Inner alloc] initWithTest:self withBOOL:YES] autorelease]);");
    assertTranslation(translation, "Test_Inner_set_this$0_(self, outer$);");
  }

  public void testOuterClassAccessOuterVars() {
    List<AbstractTypeDeclaration> types = translateClassBody(
      "int elementCount;" +
      "public Test() { " +
      "  elementCount = 0; }" +
      "private class Iterator {" +
      "  public void remove() {" +
      "    elementCount--;" +
      "  }}");
    assertEquals(2, types.size());

    final int[] testsFound = { 0 };
    types.get(0).accept(new ASTVisitor() {
      @Override
      public void endVisit(MethodDeclaration node) {
        if (node.isConstructor()) {
          // No this$0 qualifier should have been added.
          assertEquals("elementCount=0;", node.getBody().statements().get(1).toString().trim());
          ++testsFound[0];
        }
      };
    });
    assertEquals(1, testsFound[0]);
  }

  public void testOuterInterfaceMethodReference() throws IOException {
    String source = "class Test { " +
        "  interface Foo { void foo(); } " +
        "  abstract class Bar implements Foo { " +
        "    class Inner { Inner() { foo(); } } " +
        "    class Inner2 extends Inner { void bar() { foo(); } } " +
        "    Inner makeInner() { return new Inner(); } }" +
        "  public void test() { " +
        "    Bar bar = new Bar() { public void foo() { } };" +
        "    Bar.Inner inner = bar.new Inner(); } }";

    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation, "- (void)bar {\n  [this$1_ foo]");
    assertTranslation(translation,
        "- (Test_Bar_Inner *)makeInner {\n" +
        "  return [[[Test_Bar_Inner alloc] initWithTest_Bar:self] autorelease]");
    assertTranslation(translation, "[[[Test_Bar_Inner alloc] initWithTest_Bar:bar] autorelease];");
  }

  public void testMultipleThisReferences() throws IOException {
    String source =
        "class A { private int x = 0; " +
        "  interface Foo { void doSomething(); } " +
        "  class Inner { private int x = 1; " +
        "    public void blah() { " +
        "      new Foo() { public void doSomething() { " +
        "      Inner.this.x = 2; A.this.x = 3; }}; }}}";
    CompilationUnit unit = translateType("A", source);
    List<AbstractTypeDeclaration> types = ASTUtil.getTypes(unit);
    assertEquals(4, types.size());

    String translation = translateSourceFile(source, "A", "A.m");
    // Anonymous class constructor in Inner.blah()
    assertTranslation(translation, "[[A_Inner_$1 alloc] initWithA_Inner:self]");
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
        "class A { private int x = 0; " +
        "  interface Foo { void doSomething(); } " +
        "  class Inner { private int x = 1; " +
        "    public void blah() { " +
        "      A.this.x = 2; " +
        "      new Foo() { public void doSomething() { " +
        "      Inner.this.x = 3; A.this.x = 4; }}; }}}";
    CompilationUnit unit = translateType("A", source);
    List<AbstractTypeDeclaration> types = ASTUtil.getTypes(unit);
    assertEquals(4, types.size());

    String translation = translateSourceFile(source, "A", "A.m");
    // Anonymous class constructor in Inner.blah()
    assertTranslation(translation, "[[A_Inner_$1 alloc] initWithA_Inner:self]");
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
    String source = "class Test { " +
        "  interface Foo { void foo(); } " +
        "  class Inner { " +
        "    void bar() { " +
        "      final int x = 0; final int y = 0; " +
        "      Foo foo = new Foo() { " +
        "        public void foo() { if (x ==0) mumble(y); } }; } }" +
        "  private void mumble(int y) { } }";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation, "[this$0_->this$0_ mumbleWithInt:0]");
  }

  public void testInnerSubClassOfGenericClassInner() throws IOException {
    String source = "class Test { " +
        "class A<E extends A<E>.Inner> { public class Inner { } } " +
        "class B extends A<B.BInner> { public class BInner extends A<B.BInner>.Inner { } } }";
    String translation = translateSourceFile(source, "Test", "Test.h");

    assertTranslation(translation, "@interface Test_B_BInner : Test_A_Inner");
  }

  public void testGenericInnerSubClassOfGenericClassGenericInner() throws IOException {
    String source = "class Test<E> { " +
        "class A<E> { } class B<E> extends A<E> { B(int i) { } } }";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation,
        "- (id)initWithTest:(Test *)outer$\n" +
        "           withInt:(int)i");
  }

  public void testInnerSubClassOfOtherInnerWithOuterRefs() throws IOException {
    String source = "class Test { " +
        "class A { " +
        "  public void foo() { } " +
        "  public class Inner { void test() { foo(); } } } " +
        "class B extends A { " +
        "  public class BInner extends A.Inner { void test() { foo(); } } } " +
        "    public static void main(String[] args) { B b = new Test().new B(); }}";
    String headerTranslation = translateSourceFile(source, "Test", "Test.h");

    // Check that outer fields are added to A.Inner and B.BInner.
    assertTranslation(headerTranslation,
        "@interface Test_A_Inner : NSObject {\n @public\n  Test_A *this$0_");
    assertTranslation(headerTranslation,
        "@interface Test_B_BInner : Test_A_Inner {\n @public\n  Test_B *this$1_");

    // Check that B has a constructor that correctly calls constructor of A
    // with right outer.
    String sourceTranslation = getTranslatedFile("Test.m");
    assertTranslation(sourceTranslation, "initWithTest_B:(Test_B *)outer$");
    assertTranslation(sourceTranslation, "[super initWithTest_A:outer$]");
  }

  public void testInnerClassQualifiedAndUnqualfiedOuterReferences() throws IOException {
    String source = "class Test { " +
        "  public int i = 0; " +
        "  class Inner { " +
        "    void foo(int i) { Test.this.i = i; } " +
        "    void bar() { int j = i; } } }";
    String translation = translateSourceFile(source, "Test", "Test.m");

    assertTranslation(translation, "- (void)fooWithInt:(int)i {\n  this$0_->i_ =");
    assertTranslation(translation, "- (void)bar {\n  int j = this$0_->i_");
  }

  public void testInnerClassExtendsAnotherInner() throws IOException {
    String translation = translateSourceFile(
        "class Test { " +
        "  Integer i = 1; " +
        "  class Inner1 { } " +
        "  class Inner2 extends Inner1 { " +
        "    int j = 1; " +
        "    public int foo() { return i + j; } } }",
        "Test", "Test.h");
    assertTranslation(translation, "Test *this$1");  // Inner2's outer reference.

    translation = getTranslatedFile("Test.m");
    assertTranslation(translation, "[((JavaLangInteger *) nil_chk(this$1_->i_)) intValue] + j_");
  }

  public void testInnerClassInstantiatesAnotherInner() throws IOException {
    String translation = translateSourceFile(
        "class Test { " +
        "  Integer i = 1; " +
        "  class Inner1 { public int foo() { return i + 1; } } " +
        "  class Inner2 { Inner1 inner1 = new Inner1(); } }",
        "Test", "Test.m");
    assertTranslation(translation, "[[Test_Inner1 alloc] initWithTest:outer$]");

    translation = getTranslatedFile("Test.h");
    assertTranslation(translation,
        "@interface Test_Inner2 : NSObject {\n" +
        " @public\n" +
        "  Test_Inner1 *inner1_;");
  }

  public void testInnerClassWithInnerSuperClass() throws IOException {
    String translation = translateSourceFile(
        "class Test { " +
        "  class Inner1 { public Inner1(int n) { } } " +
        "  class Inner2 extends Inner1 { public Inner2(int n, long l) { super(n); } } }",
        "Test", "Test.m");
    assertTranslation(translation, "[super initWithTest:outer$ withInt:n]");
  }

  public void testInnerSubClassOfOtherInnerWithOuterRefsExtraction() throws IOException {
    String source = "public class Test { " +
        "int i; " +
        "class A { " +
        "  private void foo() { i++; } " +
        "  public class Inner { Inner() { foo(); } } } " +
        "class B extends A { " +
        "  public class BInner extends A.Inner { } } " +
        "public static void main(String[] args) { B b = new Test().new B(); }}";
    CompilationUnit unit = translateType("Test", source);
    List<AbstractTypeDeclaration> types = ASTUtil.getTypes(unit);
    assertEquals(5, types.size());
    Map<String, AbstractTypeDeclaration> typesByName = Maps.newHashMap();
    for (AbstractTypeDeclaration type : types) {
      typesByName.put(NameTable.getFullName(type), type);
    }

    // Verify that main method creates a new instanceof B associated with
    // a new instance of Test.
    List<BodyDeclaration> classMembers = ASTUtil.getBodyDeclarations(typesByName.get("Test"));
    assertEquals(4, classMembers.size());
    MethodDeclaration method = (MethodDeclaration) classMembers.get(1);
    assertEquals("main", method.getName().getIdentifier());
    VariableDeclarationStatement field =
        (VariableDeclarationStatement) method.getBody().statements().get(0);
    assertEquals("Test_B", NameTable.getFullName(Types.getTypeBinding(field.getType())));
    String result = StatementGenerator.generate(field, Collections.<IVariableBinding>emptySet(),
        false, new SourcePosition(null, SourceBuilder.BEGINNING_OF_FILE, null)).trim();
    assertEquals("Test_B *b = " +
        "[[[Test_B alloc] initWithTest:[[[Test alloc] init] autorelease]] autorelease];", result);

    // Verify that A has a Test field (this$0).
    AbstractTypeDeclaration classA = typesByName.get("Test_A");
    assertNotNull(classA);
    classMembers = ASTUtil.getBodyDeclarations(classA);
    assertEquals(4, classMembers.size());  // Test field, init, foo, dealloc.

    // Verify that B has a Test field (this$0).
    AbstractTypeDeclaration classB = typesByName.get("Test_B");
    assertNotNull(classB);
    classMembers = ASTUtil.getBodyDeclarations(classB);
    assertEquals(1, classMembers.size());  // init

    // Verify that B has a constructor that takes a Test instance.
    method = (MethodDeclaration) classMembers.get(0);
    assertTrue(method.isConstructor());
    assertEquals(1, method.parameters().size());
    SingleVariableDeclaration param = (SingleVariableDeclaration) method.parameters().get(0);
    assertEquals("Test", param.getType().toString());

    // Verify that B's translation has the Test field declared.
    String translation = translateSourceFile(source, "Test", "Test.h");
    assertTranslation(translation, "Test *this$0_;");
    translation = getTranslatedFile("Test.m");
    assertTranslation(translation, "- (id)initWithTest_B:(Test_B *)outer$ {");
    assertTranslation(translation, "[super initWithTest_A:outer$]");
  }

  // Identical sample code to above test, except the order of B and A is switched.
  public void testInnerSubClassOfOtherInnerWithOuterRefsExtraction2() throws IOException {
    String source = "public class Test { " +
        "int i; " +
        "class B extends A { " +
        "  public class BInner extends A.Inner { } } " +
        "class A { " +
        "  private void foo() { i++; } " +
        "  public class Inner { Inner() { foo(); } } } " +
        "public static void main(String[] args) { B b = new Test().new B(); }}";
    CompilationUnit unit = translateType("Test", source);
    List<AbstractTypeDeclaration> types = ASTUtil.getTypes(unit);
    assertEquals(5, types.size());
    Map<String, AbstractTypeDeclaration> typesByName = Maps.newHashMap();
    for (AbstractTypeDeclaration type : types) {
      typesByName.put(NameTable.getFullName(type), type);
    }

    // Verify that main method creates a new instanceof B associated with
    // a new instance of Test.
    List<BodyDeclaration> classMembers = ASTUtil.getBodyDeclarations(typesByName.get("Test"));
    assertEquals(4, classMembers.size());
    MethodDeclaration method = (MethodDeclaration) classMembers.get(1);
    assertEquals("main", method.getName().getIdentifier());
    VariableDeclarationStatement field =
        (VariableDeclarationStatement) method.getBody().statements().get(0);
    assertEquals("Test_B", NameTable.getFullName(Types.getTypeBinding(field.getType())));
    String result = StatementGenerator.generate(field, Collections.<IVariableBinding>emptySet(),
        false, new SourcePosition(null, SourceBuilder.BEGINNING_OF_FILE, null)).trim();
    assertEquals("Test_B *b = " +
        "[[[Test_B alloc] initWithTest:[[[Test alloc] init] autorelease]] autorelease];", result);

    // Verify that A has a Test field (this$0).
    AbstractTypeDeclaration classA = typesByName.get("Test_A");
    assertNotNull(classA);
    classMembers = ASTUtil.getBodyDeclarations(classA);
    assertEquals(4, classMembers.size());  // Test field, init, foo, dealloc.

    // Verify that B has a Test field (this$0).
    AbstractTypeDeclaration classB = typesByName.get("Test_B");
    assertNotNull(classB);
    classMembers = ASTUtil.getBodyDeclarations(classB);
    assertEquals(1, classMembers.size());  // init

    // Verify that B has a constructor that takes a Test instance.
    method = (MethodDeclaration) classMembers.get(0);
    assertTrue(method.isConstructor());
    assertEquals(1, method.parameters().size());
    SingleVariableDeclaration param = (SingleVariableDeclaration) method.parameters().get(0);
    assertEquals("Test", param.getType().toString());

    // Verify that B's translation has the Test field declared.
    String translation = translateSourceFile(source, "Test", "Test.h");
    assertTranslation(translation, "Test *this$0_;");
    translation = getTranslatedFile("Test.m");
    assertTranslation(translation, "- (id)initWithTest_B:(Test_B *)outer$ {");
    assertTranslation(translation, "[super initWithTest_A:outer$]");
  }

  // Identical sample code to above test, except A is a generic class.
  public void testInnerSubClassOfOtherInnerWithOuterRefsWithGenerics() throws IOException {
    String source = "public class Test { " +
        "class B extends A<B.BInner> { " +
        "  public class BInner extends A.Inner { BInner() { super(null); } } } " +
        "class A<T extends A<T>.Inner> { " +
        "  private void foo() { } " +
        "  public class Inner { Inner(T t) { foo(); } } } " +
        "public static void main(String[] args) { B b = new Test().new B(); }}";
    String translation = translateSourceFile(source, "Test", "Test.m");

    // Make sure that the call to super(null) in B.BInner's constructor
    // is translated with the right keyword for the generic second parameter.
    assertTranslation(translation, "[super initWithTest_A:outer$ withId:nil");
  }

  public void testStaticImportReferenceInInnerClass() throws IOException {
    String translation = translateSourceFile(
        "import static java.lang.Character.isDigit; public class Test { class Inner { " +
        "  public void foo() { boolean b = isDigit('c'); } } }",
        "Test", "Test.m");
    assertTranslation(translation, "[JavaLangCharacter isDigit");
  }

  public void testStaticReferenceInInnerClass() throws IOException {
    String translation = translateSourceFile(
        "public class Test { public static void foo() { } class Inner { " +
        "  public void bar() { foo(); } } }",
        "Test", "Test.m");
    assertTranslation(translation, "[Test foo]");
  }

  public void testMethodInnerClass() throws IOException {
    String source = "public class A { void foo() { class MyRunnable implements Runnable {" +
        "public void run() {} }}}";
    String translation = translateSourceFile(source, "A", "A.h");
    assertTranslation(translation, "@interface A_foo_MyRunnable : NSObject < JavaLangRunnable >");
    assertNotInTranslation(translation, "A *this");
    assertTranslation(translation, "- (id)initWithA:(A *)outer$;");
  }

  public void testInnerClassConstructor() throws IOException {
    String source = "public class A { class B { " +
        "Object test() { return new B(); }}}";
    String translation = translateSourceFile(source, "A", "A.m");
    assertTranslation(translation, "return [[[A_B alloc] initWithA:this$0_] autorelease];");
  }

  public void testMethodInnerClassWithSameName() throws IOException {
    String source = "public class A { class MyClass {} void foo() { class MyClass {}}}";
    String translation = translateSourceFile(source, "A", "A.h");
    assertTranslation(translation, "@interface A_MyClass");
    assertTranslation(translation, "@interface A_foo_MyClass");
  }

  public void testOuterThisReferenceInInner() throws IOException {
    String translation = translateSourceFile(
        "class Test { " +
        "  class Inner { Inner(int i) { } Inner foo() { return new Inner(1); } } " +
        "  public Inner bar() { return new Inner(2); } }",
        "Test", "Test.m");
    assertTranslation(translation, "[[Test_Inner alloc] initWithTest:this$0_ withInt:1]");
    assertTranslation(translation, "[[Test_Inner alloc] initWithTest:self withInt:2]");
  }

  public void testInnerThisReferenceInInnerAsFieldAccess() throws IOException {
    String translation = translateSourceFile(
        "class Test { " +
        "  class Inner { int i = 0; Inner() { Inner.this.i = 1; } } }",
        "Test", "Test.m");
    assertTranslation(translation, "self->i_ = 1");
  }

  public void testInnerThisReferenceInInnerAsThisExpression() throws IOException {
    String translation = translateSourceFile(
        "class Test { " +
        "  static void foo(Inner i) { } " +
        "  class Inner { Inner() { foo(Inner.this); } } }",
        "Test", "Test.m");
    assertTranslation(translation, "[Test fooWithTest_Inner:self]");
  }

  // Verify that an anonymous class in a static initializer does not reference
  // instance.
  public void testNoOuterInStaticInitializer() throws IOException {
    String source = "import java.util.*; " +
        "public class A { static { foo(new Enumeration() { " +
        "    public boolean hasMoreElements() { return false; }" +
        "    public Object nextElement() { return null; }}); }" +
        "  public static void foo(Object o) { } }";
    String translation = translateSourceFile(source, "A", "A.h");
    assertFalse(translation.contains("this$0_"));
    translation = getTranslatedFile("A.m");
    assertFalse(translation.contains("this$0_"));
    assertTranslation(translation, "fooWithId:[[[A_$1 alloc] init]");
  }

  // Verify that an anonymous class assigned to a static field does not
  // reference instance.
  public void testNoOuterWhenAssignedToStaticField() throws IOException {
    String source = "import java.util.*; " +
        "public class A { static Enumeration test = new Enumeration() { " +
        "    public boolean hasMoreElements() { return false; }" +
        "    public Object nextElement() { return null; }}; }";
    String translation = translateSourceFile(source, "A", "A.h");
    assertFalse(translation.contains("this$0_"));
    translation = getTranslatedFile("A.m");
    assertFalse(translation.contains("this$0_"));
    assertTranslation(translation,
        "JreOperatorRetainedAssign(&A_test_, nil, [[[A_$1 alloc] init] autorelease]);");
  }

  // Verify that an anonymous class in a static method does not reference
  // instance.
  public void testNoOuterWhenInStaticMethod() throws IOException {
    String source = "import java.util.*; " +
        "public class A { static Enumeration test(Collection collection) { " +
        "  final Collection c = collection; " +
        "  return new Enumeration() { " +
        "    Iterator it = c.iterator(); " +
        "    public boolean hasMoreElements() { return it.hasNext(); }" +
        "    public Object nextElement() { return it.next(); }}; }}";
    String translation = translateSourceFile(source, "A", "A.h");
    assertFalse(translation.contains("this$0_"));
    assertTranslation(translation, "id<JavaUtilCollection> val$c_;");
    assertTranslation(translation,
        "- (id)initWithJavaUtilCollection:(id<JavaUtilCollection>)capture$0;");
    translation = getTranslatedFile("A.m");
    assertFalse(translation.contains("this$0_"));
    assertTranslation(translation,
        "return [[[A_$1 alloc] initWithJavaUtilCollection:c] autorelease];");
    assertTranslation(translation,
        "- (id)initWithJavaUtilCollection:(id<JavaUtilCollection>)capture$0 {");
  }

  public void testInnerAccessingOuterArrayLength() throws IOException {
    String source = "public class A<E> { transient E[] elements; " +
        "private class B implements java.util.Iterator<E> { " +
        "public boolean hasNext() { return elements.length > 0; } " +
        "public E next() { return null; }" +
        "public void remove() {} }}";
    String translation = translateSourceFile(source, "A", "A.h");
    assertTranslation(translation, "A *this$0_;");
    assertTranslation(translation, "- (id)initWithA:(A *)outer$;");
    translation = getTranslatedFile("A.m");
    assertTranslation(translation, "[((IOSObjectArray *) nil_chk(this$0_->elements_)) count]");
  }

  public void testCreateInnerClassOfSuperclass() throws IOException {
    String source = "class B {\n" +
        "  class C {}\n" +
        "}\n" +
        "class A extends B {\n" +
        "  void foo() { new C(); }\n" +
        "}\n";
    String translation = translateSourceFile(source, "A", "A.m");
    assertTranslation(translation, "[[B_C alloc] initWithB:self]");
  }

  public void testCallInnerConstructorOfParameterizedOuterClass() throws IOException {
    String outerSource = "abstract class Outer<T> { " +
        "class Inner { public Inner(T t) {} }}";
    String callerSource = "class A extends Outer<String> { public void foo() { " +
        "new Inner(\"test\"); } }";
    addSourceFile(outerSource, "Outer.java");
    addSourceFile(callerSource, "A.java");
    String translation = translateSourceFile("A", "A.m");
    assertTranslation(translation,
        "[[[Outer_Inner alloc] initWithOuter:self withId:@\"test\"] autorelease];");
  }

  public void testNoOuterFieldAssignmentWhenCallingOtherConstructor() throws IOException {
    String source = "class Outer { class Inner { " +
        "Inner(int i) {} Inner() { this(42); } } }";
    String translation = translateSourceFile(source, "Outer", "Outer.m");
    assertTranslation(translation, "- (id)initWithOuter:(Outer *)outer$ {\n" +
        "  return JreMemDebugAdd([self initOuter_InnerWithOuter:outer$ withInt:42]);\n}");
  }

  public void testListArgsInEnumConstantDeclaration() throws IOException {
    String source =
        "class Outer { " +
        "  enum Inner { " +
        "    A(new String[] { \"1\", \"2\", \"3\" }), " +
        "    B(new String[] { \"4\", \"5\", \"6\" }); " +
        "    Inner(String[] values) {} " +
        "  } " +
        "}";
    String translation = translateSourceFile(source, "Outer", "Outer.m");
    assertTranslation(translation, "[IOSObjectArray arrayWithObjects:(id[]){ " +
        "@\"1\", @\"2\", @\"3\" } count:3 type:[IOSClass classWithClass:[NSString class]]]");
    assertTranslation(translation, "[IOSObjectArray arrayWithObjects:(id[]){ " +
        "@\"4\", @\"5\", @\"6\" } count:3 type:[IOSClass classWithClass:[NSString class]]]");
  }

  public void testInnerClassVarargsConstructor() throws IOException {
    String translation = translateSourceFile(
        "class Test { class Inner { Inner(int... i) {} } void test() { new Inner(1, 2, 3); } }",
        "Test", "Test.m");
    assertTranslation(translation,
        "[[Test_Inner alloc] initWithTest:self withIntArray:" +
        "[IOSIntArray arrayWithInts:(int[]){ 1, 2, 3 } count:3]]");
  }

  public void testInnerClassConstructedInSuperConstructorInvocation() throws IOException {
    String translation = translateSourceFile(
        "class Outer { " +
        "  class Inner1 { } " +
        "  class Inner2Super { Inner2Super(Inner1 i) { } } " +
        "  class Inner2 extends Inner2Super { " +
        "    Inner2() { " +
        "      super(new Inner1()); " +
        "    } " +
        "  } " +
        "}", "Outer", "Outer.m");

    assertTranslation(translation, "[[Outer_Inner1 alloc] initWithOuter:outer$]");
  }

  public void testOuterReferenceInSuperConstructorInvocation() throws IOException {
    String translation = translateSourceFile(
        "class Outer { " +
        "  int foo; " +
        "  class Inner1 { Inner1(int i) { } } " +
        "  class Inner2 extends Inner1 { " +
        "    Inner2() { " +
        "      super(foo); " +
        "    } " +
        "  } " +
        "}", "Outer", "Outer.m");

    assertTranslation(translation, "[super initWithOuter:outer$ withInt:outer$->foo_]");
  }

  public void testOuterThisReferenceInSuperConstructorInvocation() throws IOException {
    String translation = translateSourceFile(
        "class Outer { " +
        "  int foo; " +
        "  class Outer1 { " +
        "    int foo; " +
        "    class Inner1 { Inner1(int i) { } } " +
        "    class Inner2 extends Inner1 { " +
        "      Inner2() { " +
        "        super(Outer.this.foo); " +
        "      } " +
        "    } " +
        "  } " +
        "}", "Outer", "Outer.m");

    assertTranslation(translation,
        "[super initWithOuter_Outer1:outer$ withInt:outer$->this$0_->foo_]");
  }

  public void testAnonymousClassWithinTypeDeclarationStatement() throws IOException {
    String translation = translateSourceFile(
        "class Test { Runnable foo() { class MyRunnable implements Runnable { " +
        "public void run() { Runnable r = new Runnable() { public void run() {} }; } } " +
        "return new MyRunnable(); } }", "Test", "Test.h");
    assertOccurrences(translation, "@interface Test_foo_MyRunnable_$1", 1);
  }
}
