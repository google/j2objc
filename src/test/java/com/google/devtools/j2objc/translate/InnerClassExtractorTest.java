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
import com.google.devtools.j2objc.gen.SourceBuilder;
import com.google.devtools.j2objc.gen.StatementGenerator;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link InnerClassExtractor}.
 *
 * @author Tom Ball
 */
@SuppressWarnings("unchecked")  // JDT lists are raw, but still safely typed.
public class InnerClassExtractorTest extends GenerationTest {
  // TODO(user): update bug id in comments to public issue numbers when
  // issue tracking is sync'd.

  @Override
  protected void setUp() throws IOException {
    super.setUp();
    // Reference counting by default, change for ARC-specific tests.
    Options.setMemoryManagementOption(MemoryManagementOption.REFERENCE_COUNTING);
  }

  protected List<TypeDeclaration> translateClassBody(String testSource) {
    String source = "public class Test { " + testSource + " }";
    CompilationUnit unit = translateType("Test", source);
    return unit.types();
  }

  public void testSimpleInnerClass() throws IOException {
    String source = "public class A { class B { int test() { return o.hashCode(); }} Object o; }";
    String translation = translateSourceFile(source, "A", "A.h");
    assertTranslation(translation, "A *this$0_;");
    assertTranslation(translation, "@property (nonatomic, retain) A *this$0;");
    assertTranslation(translation, "- (id)initWithA:(A *)outer$0;");
    translation = getTranslatedFile("A.m");
    assertTranslation(translation, "[NIL_CHK(this$0_.o) hash]");
    assertTranslation(translation, "([this$0_ autorelease], this$0_ = [outer$0 retain]);");
  }

  public void testWeakSimpleInnerClass() throws IOException {
    String source = "import com.google.j2objc.annotations.WeakOuter; " +
    	"public class A { @WeakOuter class B { int test() { return o.hashCode(); }} Object o; }";
    String translation = translateSourceFile(source, "A", "A.h");
    assertTranslation(translation, "@property (nonatomic, assign) A *this$0;");
    translation = getTranslatedFile("A.m");
    assertTranslation(translation, "this$0_ = outer$0;");
  }

  public void testWeakArcSimpleInnerClass() throws IOException {
    Options.setMemoryManagementOption(MemoryManagementOption.ARC);
    String source = "import com.google.j2objc.annotations.WeakOuter; " +
        "public class A { Object o;" +
        "  @WeakOuter class B { int test() { return o.hashCode(); } Object o2; }}";
    String translation = translateSourceFile(source, "A", "A.h");
    assertTranslation(translation, "@property (nonatomic, strong) id o;");
    assertTranslation(translation, "@property (nonatomic, strong) id o2;");
    assertTranslation(translation, "@property (nonatomic, weak) A *this$0;");
  }

  public void testInnerInnerClass() throws IOException {
    String source = "public class A { class B { " +
        "class C {int test() { return o.hashCode(); }}} Object o; }";
    String translation = translateSourceFile(source, "A", "A.h");
    assertTranslation(translation, "A *this$0_;");
    assertTranslation(translation, "@property (nonatomic, retain) A *this$0;");
    assertTranslation(translation, "- (id)initWithA:(A *)outer$0;");
    assertTranslation(translation, "A_B *this$0_;");
    assertTranslation(translation, "@property (nonatomic, retain) A_B *this$0;");
    assertTranslation(translation, "- (id)initWithA_B:(A_B *)outer$0;");
    translation = getTranslatedFile("A.m");
    assertTranslation(translation, "[NIL_CHK(this$0_.this$0.o) hash]");
  }

  public void testWeakInnerInnerClass() throws IOException {
    String source = "public class A { class B { " +
        "@com.google.j2objc.annotations.WeakOuter class C {" +
        "  int test() { return o.hashCode(); }}} Object o; }";
    String translation = translateSourceFile(source, "A", "A.h");
    assertTranslation(translation, "@property (nonatomic, retain) A *this$0;");
    assertTranslation(translation, "@property (nonatomic, assign) A_B *this$0;");
    translation = getTranslatedFile("A.m");
    assertTranslation(translation, "[NIL_CHK(this$0_.this$0.o) hash]");
  }

  /** TODO(user): test fails due to b/6265412
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
    assertTranslation(translation, "@property (nonatomic, retain) A *this$0;");
    assertTranslation(translation, "- (id)initWithA:(A *)outer$0;");
    assertTranslation(translation, "A_B *this$1_;");
    assertTranslation(translation, "int val$j_;");
    assertTranslation(translation, "@property (nonatomic, retain) A_B *this$1;");
    assertTranslation(translation, "@property (nonatomic, assign) int val$j;");
    assertTranslation(translation,
        "- (id)initWithA_B:(A_B *)outer$1\n" +
        "          withInt:(int)outer$0;");
    translation = getTranslatedFile("A.m");
    assertTranslation(translation, "[super initWithA:outer$1.this$0]");
    assertTranslation(translation, "[NIL_CHK(this$1_.this$0.o) hash]");
  }
  */

  /**
   * Verify that a static inner class is extracted.
   */
  public void testStaticInnerClass() {
    List<TypeDeclaration> types = translateClassBody(
        "static class Foo { int i; Foo() { this(0); } Foo(int i) { this.i = i; } }");
    assertEquals(2, types.size());
    List<BodyDeclaration> classMembers = types.get(0).bodyDeclarations();
    assertTrue(classMembers.isEmpty());
    TypeDeclaration innerClass = types.get(1);
    assertEquals(3, innerClass.bodyDeclarations().size());
    List<?> members = innerClass.bodyDeclarations();

    FieldDeclaration field = (FieldDeclaration) members.get(0);
    assertEquals(field.getAST().resolveWellKnownType("int"), field.getType().resolveBinding());

    MethodDeclaration method = (MethodDeclaration) members.get(1);
    assertTrue(method.isConstructor());
    assertTrue(method.parameters().isEmpty());
    assertEquals(1, method.getBody().statements().size());
    ConstructorInvocation stmt = (ConstructorInvocation) method.getBody().statements().get(0);
    assertEquals(1, stmt.arguments().size());


    method = (MethodDeclaration) members.get(2);
    assertTrue(method.isConstructor());
    assertEquals(1, method.parameters().size());
    assertEquals(1, method.getBody().statements().size());
    ExpressionStatement expr = (ExpressionStatement) method.getBody().statements().get(0);
    assertTrue(expr.getExpression() instanceof Assignment);
  }

  /**
   * Verify that an inner class is moved to the compilation unit's types list.
   */
  public void testInnerClassExtracted() {
    List<TypeDeclaration> types = translateClassBody("class Foo { }");
    assertEquals(2, types.size());
    assertEquals("Test", types.get(0).getName().getIdentifier());
    assertEquals("Foo", types.get(1).getName().getIdentifier());
  }

  /**
   * Regression test: verify that references to class members of a type with
   * an inner class aren't disturbed.
   */
  public void testStaticMethodInvokingStaticMethodWithInnerClass() {
    List<TypeDeclaration> types = translateClassBody(
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

  public void testInnerClassInvokingExplicitOuterMethod() {
    List<TypeDeclaration> types = translateClassBody(
      "public int size() { return 0; } " +
      "class Inner { int size() { return Test.this.size(); }}");
    assertEquals(2, types.size());

    final int[] testsFound = { 0 };
    types.get(1).accept(new ASTVisitor() {
      @Override
      public void endVisit(MethodDeclaration node) {
        if (node.isConstructor()) {
          assertEquals("final Test outer$0", node.parameters().get(0).toString());
          assertEquals("this$0=outer$0;", node.getBody().statements().get(0).toString().trim());
          ++testsFound[0];
        } else if (node.getName().getIdentifier().equals("dealloc")) {
          assertEquals("this$0=null;", node.getBody().statements().get(0).toString().trim());
          ++testsFound[0];
        }
      };

      @Override
      public void endVisit(ReturnStatement node) {
        assertEquals("return this$0.size();", node.toString().trim());
        ++testsFound[0];
      }
    });
    assertEquals(3, testsFound[0]);
  }

  public void testInnerClassInvokingOuterMethod() {
    List<TypeDeclaration> types = translateClassBody(
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

  public void testInnerSubclassInvokingOuterMethod() {
    List<TypeDeclaration> types = translateClassBody(
      "public int size() { return 0; } public void add(int n) {} class Inner {} " +
      "class Innermost { void test() { Test.this.add(size()); }}");
    assertEquals(3, types.size());

    final int[] testsFound = { 0 };
    types.get(2).accept(new ASTVisitor() {
      @Override
      public void endVisit(MethodDeclaration node) {
        if (node.isConstructor()) {
          assertEquals("this$0=outer$0;", node.getBody().statements().get(0).toString().trim());
          ++testsFound[0];
        } else if (node.getName().getIdentifier().equals("test")) {
          assertEquals("this$0.add(this$0.size());",
              node.getBody().statements().get(0).toString().trim());
          ++testsFound[0];
        }
      }
    });
    assertEquals(2, testsFound[0]);
  }

  public void testInnerClassDefaultInitialization() {
    List<TypeDeclaration> types = translateClassBody(
      "Inner inner = new Inner(true); public int size() { return 0; }" +
      "class Inner { Inner(boolean b) {} int size() { return Test.this.size(); }}");
    assertEquals(2, types.size());

    final int[] testsFound = { 0 };
    types.get(0).accept(new ASTVisitor() {
      @Override
      public void endVisit(MethodDeclaration node) {
        if (node.isConstructor()) {
          assertEquals("inner=new Inner(this,true);",
              node.getBody().statements().get(1).toString().trim());
          ++testsFound[0];
        }
      };
    });
    types.get(1).accept(new ASTVisitor() {
      @Override
      public void endVisit(MethodDeclaration node) {
        if (node.isConstructor()) {
          String result = node.getBody().statements().get(0).toString().trim();
          assertTrue(result.matches("this\\$0=outer\\$[0-9];"));
          ++testsFound[0];
        }
      };
    });
   assertEquals(2, testsFound[0]);
  }

  public void testOuterClassAccessOuterVars() {
    List<TypeDeclaration> types = translateClassBody(
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
          assertEquals("elementCount=0;", node.getBody().statements().get(0).toString().trim());
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
        "    Inner newInner() { return new Inner(); } }" +
        "  public void test() { " +
        "    Bar bar = new Bar() { public void foo() { } };" +
        "    Bar.Inner inner = bar.new Inner(); } }";

    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation, "- (void)bar {\n  [this$1_.this$0 foo]");
    assertTranslation(translation,
        "- (Test_Bar_Inner *)newInner {\n" +
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
    List<TypeDeclaration> types = unit.types();
    assertEquals(4, types.size());

    String translation = translateSourceFile(source, "A", "A.m");
    // Anonymous class constructor in Inner.blah()
    assertTranslation(translation, "[[A_Inner_$1 alloc] initWithA_Inner:self]");
    // A.Inner.x referred to in anonymous Foo
    assertTranslation(translation, "this$0.x = 2");
    // A.x referred to in anonymous Foo
    assertTranslation(translation, "this$0_.this$0.x = 3");
    // A.Inner init in anonymous Foo's constructor
    assertTranslation(translation, "this$0_ = [outer$0 retain]");
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
    List<TypeDeclaration> types = unit.types();
    assertEquals(4, types.size());

    String translation = translateSourceFile(source, "A", "A.m");
    // Anonymous class constructor in Inner.blah()
    assertTranslation(translation, "[[A_Inner_$1 alloc] initWithA_Inner:self]");
    // A.Inner.x referred to in anonymous Foo
    assertTranslation(translation, "self.this$0.x = 3");
    // A.x referred to in anonymous Foo
    assertTranslation(translation, "this$0_.this$0.x = 4");
    // A.Inner init in anonymous Foo's constructor
    assertTranslation(translation, "this$0_ = [outer$0 retain]");
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
    assertTranslation(translation, "[this$0_.this$0 mumbleWithInt:0]");
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
        "- (id)initWithTest:(Test *)outer$1\n" +
        "           withInt:(int)i");
  }

  public void testInnerSubClassOfOtherInnerWithOuterRefs() throws IOException {
    String source = "class Test { " +
        "class A { " +
        "  private void foo() { } " +
        "  public class Inner { Inner() { foo(); } } } " +
        "class B extends A { " +
        "  public class BInner extends A.Inner { } } " +
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
    assertTranslation(sourceTranslation, "initWithTest_B:(Test_B *)outer$0");
    assertTranslation(sourceTranslation, "[super initWithTest_A:outer$0]");
  }

  public void testInnerClassQualifiedAndUnqualfiedOuterReferences() throws IOException {
    String source = "class Test { " +
        "  public int i = 0; " +
        "  class Inner { " +
        "    void foo(int i) { Test.this.i = i; } " +
        "    void bar() { int j = i; } } }";
    String translation = translateSourceFile(source, "Test", "Test.m");

    assertTranslation(translation, "- (void)fooWithInt:(int)i {\n  self.this$0.i =");
    assertTranslation(translation, "- (void)bar {\n  int j = this$0_.i");
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
    assertTranslation(translation, "Test *this$0");

    translation = getTranslatedFile("Test.m");
    assertTranslation(translation, "[((JavaLangInteger *) NIL_CHK(this$1_.i)) intValue] + j_");
  }

  public void testInnerClassInstantiatesAnotherInner() throws IOException {
    String translation = translateSourceFile(
        "class Test { " +
        "  Integer i = 1; " +
        "  class Inner1 { public int foo() { return i + 1; } } " +
        "  class Inner2 { Inner1 inner1 = new Inner1(); } }",
        "Test", "Test.m");
    assertTranslation(translation, "[[Test_Inner1 alloc] initWithTest:this$0_]");

    translation = getTranslatedFile("Test.h");
    assertTranslation(translation,
        "@interface Test_Inner2 : NSObject {\n" +
        " @public\n" +
        "  Test *this$0_;\n" +
        "  Test_Inner1 *inner1_;");
  }

  public void testInnerClassWithInnerSuperClass() throws IOException {
    String translation = translateSourceFile(
        "class Test { " +
        "  class Inner1 { public Inner1(int n) { } } " +
        "  class Inner2 extends Inner1 { public Inner2(int n, long l) { super(n); } } }",
        "Test", "Test.m");
    assertTranslation(translation, "self = [super initWithTest:outer$2 withInt:n]");
  }

  public void testInnerSubClassOfOtherInnerWithOuterRefsExtraction() throws IOException {
    String source = "public class Test { " +
        "class A { " +
        "  private void foo() { } " +
        "  public class Inner { Inner() { foo(); } } } " +
        "class B extends A { " +
        "  public class BInner extends A.Inner { } } " +
        "public static void main(String[] args) { B b = new Test().new B(); }}";
    CompilationUnit unit = translateType("Test", source);
    List<TypeDeclaration> types = unit.types();
    assertEquals(5, types.size());

    // Verify that main method creates a new instanceof B associated with
    // a new instance of Test.
    List<BodyDeclaration> classMembers = types.get(0).bodyDeclarations();
    assertEquals(1, classMembers.size());
    MethodDeclaration method = (MethodDeclaration) classMembers.get(0);
    assertEquals("main", method.getName().getIdentifier());
    VariableDeclarationStatement field =
        (VariableDeclarationStatement) method.getBody().statements().get(0);
    assertEquals("Test_B", NameTable.javaTypeToObjC(field.getType(), false));
    String result = StatementGenerator.generate(field, Collections.EMPTY_SET, false,
        SourceBuilder.BEGINNING_OF_FILE).trim();
    assertEquals("Test_B *b = " +
        "[[[Test_B alloc] initWithTest:[[[Test alloc] init] autorelease]] autorelease];", result);

    // Verify that A has a Test field (this$0).
    TypeDeclaration classA = types.get(1);
    assertEquals("Test_A", NameTable.getFullName(classA));
    classMembers = classA.bodyDeclarations();
    assertEquals(4, classMembers.size());  // Test field, init, foo, dealloc.

    // Verify that B has a Test field (this$0).
    TypeDeclaration classB = types.get(3);
    assertEquals("Test_B", NameTable.getFullName(classB));
    classMembers = classB.bodyDeclarations();
    assertEquals(3, classMembers.size());  // Test field, init, dealloc.

    // Verify that B has a constructor that takes a Test instance.
    method = (MethodDeclaration) classMembers.get(1);
    assertTrue(method.isConstructor());
    assertEquals(1, method.parameters().size());
    SingleVariableDeclaration param = (SingleVariableDeclaration) method.parameters().get(0);
    assertEquals("Test", param.getType().toString());

    // Verify that B's translation has the Test field declared.
    String translation = translateSourceFile(source, "Test", "Test.h");
    assertTranslation(translation, "@property (nonatomic, retain) Test *this$");
    translation = getTranslatedFile("Test.m");
    assertTranslation(translation,
        "- (id)initWithTest_B:(Test_B *)outer$0 {\n" +
        "  if ((self = [super initWithTest_A:outer$0");
  }

  // Identical sample code to above test, except the order of B and A is switched.
  public void testInnerSubClassOfOtherInnerWithOuterRefsExtraction2() throws IOException {
    String source = "public class Test { " +
        "class B extends A { " +
        "  public class BInner extends A.Inner { } } " +
        "class A { " +
        "  private void foo() { } " +
        "  public class Inner { Inner() { foo(); } } } " +
        "public static void main(String[] args) { B b = new Test().new B(); }}";
    CompilationUnit unit = translateType("Test", source);
    List<TypeDeclaration> types = unit.types();
    assertEquals(5, types.size());

    // Verify that main method creates a new instanceof B associated with
    // a new instance of Test.
    List<BodyDeclaration> classMembers = types.get(0).bodyDeclarations();
    assertEquals(1, classMembers.size());
    MethodDeclaration method = (MethodDeclaration) classMembers.get(0);
    assertEquals("main", method.getName().getIdentifier());
    VariableDeclarationStatement field =
        (VariableDeclarationStatement) method.getBody().statements().get(0);
    assertEquals("Test_B", NameTable.javaTypeToObjC(field.getType(), false));
    String result = StatementGenerator.generate(field, Collections.EMPTY_SET, false,
        SourceBuilder.BEGINNING_OF_FILE).trim();
    assertEquals("Test_B *b = " +
        "[[[Test_B alloc] initWithTest:[[[Test alloc] init] autorelease]] autorelease];", result);

    // Verify that A has a Test field (this$0).
    TypeDeclaration classA = types.get(3);
    assertEquals("Test_A", NameTable.getFullName(classA));
    classMembers = classA.bodyDeclarations();
    assertEquals(4, classMembers.size());  // Test field, init, foo, dealloc.

    // Verify that B has a Test field (this$0).
    TypeDeclaration classB = types.get(1);
    assertEquals("Test_B", NameTable.getFullName(classB));
    classMembers = classB.bodyDeclarations();
    assertEquals(3, classMembers.size());  // Test field, init, dealloc.

    // Verify that B has a constructor that takes a Test instance.
    method = (MethodDeclaration) classMembers.get(1);
    assertTrue(method.isConstructor());
    assertEquals(1, method.parameters().size());
    SingleVariableDeclaration param = (SingleVariableDeclaration) method.parameters().get(0);
    assertEquals("Test", param.getType().toString());

    // Verify that B's translation has the Test field declared.
    String translation = translateSourceFile(source, "Test", "Test.h");
    assertTranslation(translation, "@property (nonatomic, retain) Test *this$");
    translation = getTranslatedFile("Test.m");
    assertTranslation(translation,
        "- (id)initWithTest_B:(Test_B *)outer$0 {\n" +
        "  if ((self = [super initWithTest_A:outer$0");
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
    assertTranslation(translation, "[super initWithTest_A:outer$0 withId:nil");
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
    assertTranslation(translation, "A *this$0_;");
    assertTranslation(translation, "@property (nonatomic, retain) A *this$0;");
    assertTranslation(translation, "- (id)initWithA:(A *)outer$0;");
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
    assertTranslation(translation, "self.i = 1");
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
    assertTranslation(translation, "A_test_ = [[A_$1 alloc] init];");
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
        "- (id)initWithJavaUtilCollection:(id<JavaUtilCollection>)outer$0;");
    translation = getTranslatedFile("A.m");
    assertFalse(translation.contains("this$0_"));
    assertTranslation(translation,
        "return [[[A_$1 alloc] initWithJavaUtilCollection:c] autorelease];");
    assertTranslation(translation,
        "- (id)initWithJavaUtilCollection:(id<JavaUtilCollection>)outer$0 {");
  }

  public void testInnerAccessingOuterArrayLength() throws IOException {
    String source = "public class A<E> { transient E[] elements; " +
        "private class B implements java.util.Iterator<E> { " +
        "public boolean hasNext() { return elements.length > 0; } " +
        "public E next() { return null; }" +
        "public void remove() {} }}";
    String translation = translateSourceFile(source, "A", "A.h");
    assertTranslation(translation, "A *this$0_;");
    assertTranslation(translation, "@property (nonatomic, retain) A *this$0;");
    assertTranslation(translation, "- (id)initWithA:(A *)outer$0;");
    translation = getTranslatedFile("A.m");
    assertTranslation(translation, "[((IOSObjectArray *) NIL_CHK(this$0_.elements)) count]");
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
}
