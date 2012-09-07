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
import com.google.devtools.j2objc.J2ObjC.Language;
import com.google.devtools.j2objc.gen.ObjectiveCImplementationGenerator;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.io.IOException;
import java.util.List;

/**
 * Unit tests for {@link AnonymousClassConverter}.
 *
 * @author Tom Ball
 */
@SuppressWarnings("unchecked")  // JDT lists are raw, but still safely typed.
public class AnonymousClassConverterTest extends GenerationTest {

  protected List<TypeDeclaration> translateClassBody(String testSource) {
    String source = "public class Test { " + testSource + " }";
    CompilationUnit unit = translateType("Test", source);
    return unit.types();
  }

  public void testAnonymousClassNaming() throws IOException {
    String source = "import java.util.*; public class Test { " +
        "Set keySet() { return new AbstractSet() { " +
        "  public int size() { return 0; }" +
        "  public Iterator iterator() { return new Iterator() {" +
        "    public boolean hasNext() { return false; } " +
        "    public Object next() { return null; }" +
        "    public void remove() {}};}};}" +
        "Collection values() { return new AbstractCollection() {" +
        "  public int size() { return 0; }" +
        "  public Iterator iterator() { return new Iterator() {" +
        "    public boolean hasNext() { return false; } " +
        "    public Object next() { return null; }" +
        "    public void remove() {}};}};}" +
        "}";
    String header = translateSourceFile(source, "Test", "Test.h");
    assertTranslation(header, "@interface Test_$1_$1 : NSObject < JavaUtilIterator >");
    assertTranslation(header, "@interface Test_$1 : JavaUtilAbstractSet");
    assertTranslation(header, "@interface Test_$2_$1 : NSObject < JavaUtilIterator >");
    assertTranslation(header, "@interface Test_$2 : JavaUtilAbstractCollection");
  }

  public void testFinalArrayInnerAccess() throws IOException {
    String source = "public class Test { void foo() { " +
        "final boolean[] bar = new boolean[1];" +
        "Runnable r = new Runnable() { public void run() { bar[0] = true; }}; }}";
    String header = translateSourceFile(source, "Test", "Test.h");
    String impl = getTranslatedFile("Test.m");
    assertTranslation(header, "IOSBooleanArray *val$bar_;");
    assertTranslation(header, "@property (nonatomic, retain) IOSBooleanArray *val$bar;");
    assertTranslation(header,
        "- (id)initWithTest:(Test *)outer$1\nwithJavaLangBooleanArray:(IOSBooleanArray *)outer$0;");
    assertTranslation(impl,
        "IOSBooleanArray *bar = [[[IOSBooleanArray alloc] initWithLength:1] autorelease];");
    assertTranslation(impl, "[[Test_$1 alloc] initWithTest:self withJavaLangBooleanArray:bar]");
    assertTranslation(impl,
        "[((IOSBooleanArray *) NIL_CHK(val$bar_)) replaceBooleanAtIndex:0 withBoolean:YES];");
  }

  /**
   * Verify that an anonymous class is moved to the compilation unit's types list.
   */
  public void testAnonymousClassExtracted() {
    List<TypeDeclaration> types = translateClassBody(
        "Object test() { return new java.util.Enumeration<Object>() { " +
        "public boolean hasMoreElements() { return false; } " +
        "public Object nextElement() { return null; } }; }");
    assertEquals(2, types.size());

    TypeDeclaration type = types.get(1);
    assertEquals("$1", type.getName().getIdentifier());

    type = types.get(0);
    assertEquals("Test", type.getName().getIdentifier());
    MethodDeclaration testMethod = (MethodDeclaration) type.bodyDeclarations().get(0);
    ReturnStatement stmt = (ReturnStatement) testMethod.getBody().statements().get(0);
    ClassInstanceCreation expr = (ClassInstanceCreation) stmt.getExpression();
    assertNull(expr.getAnonymousClassDeclaration());
  }

  /**
   * Regression test: verify that a class passed in the constructor of an
   * anonymous class is converted.
   */
  public void testAnonymousClassWithTypeArgParameter() {
    List<TypeDeclaration> types = translateClassBody(
        "public Test(Class c) {} static Test t = " +
        "new Test(Test.class) { @Override public int hashCode() { return 1; } };");
    assertEquals(2, types.size());

    TypeDeclaration type = types.get(0);
    List<BodyDeclaration> members = type.bodyDeclarations();
    for (BodyDeclaration member : members) {
      if (member instanceof MethodDeclaration) {
        MethodDeclaration m = (MethodDeclaration) member;
        if (m.getName().getIdentifier().equals("initialize")) {
          ExpressionStatement stmt = (ExpressionStatement) m.getBody().statements().get(0);
          Assignment assign = (Assignment) stmt.getExpression();
          ClassInstanceCreation create = (ClassInstanceCreation) assign.getRightHandSide();
          assertTrue(create.arguments().get(0) instanceof TypeLiteral);
        }
      }
    }
  }

  public void testFinalParameter() {
    List<TypeDeclaration> types = translateClassBody(
      "void test(final Object test) {" +
      "  Runnable r = new Runnable() {" +
      "    public void run() {" +
      "      System.out.println(test.toString());" +
      "    }};}");
    assertEquals(2, types.size());

    final int[] testsFound = { 0 };
    types.get(0).accept(new ASTVisitor() {
      @Override
      public void endVisit(ClassInstanceCreation node) {
        assertTrue(node.arguments().get(0).toString().equals("this"));
        assertTrue(node.arguments().get(1).toString().equals("test"));
        ++testsFound[0];
      };
    });
    types.get(1).accept(new ASTVisitor() {
      @Override
      public void endVisit(MethodDeclaration node) {
        if (node.isConstructor()) {
          assertEquals("final NSObject outer$0", node.parameters().get(1).toString());
          assertEquals("val$test=outer$0;", node.getBody().statements().get(1).toString().trim());
          ++testsFound[0];
        }
      };

      @Override
      public void endVisit(MethodInvocation node) {
        if (node.getName().getIdentifier().equals("NSLog")) {
          assertEquals("val$test.toString()", node.arguments().get(1).toString());
          ++testsFound[0];
        }
      };
    });
    assertEquals(3, testsFound[0]);
  }

  public void testFinalLocalVariable() {
    List<TypeDeclaration> types = translateClassBody(
      "void test() {" +
      "  final Object foo = new Object();" +
      "  Runnable r = new Runnable() {" +
      "    public void run() {" +
      "      System.out.println(foo.toString());" +
      "    }};}");
    assertEquals(2, types.size());

    final int[] testsFound = { 0 };
    types.get(0).accept(new ASTVisitor() {
      @Override
      public void endVisit(ClassInstanceCreation node) {
        if (Types.getTypeBinding(node).isAnonymous()) {
          assertEquals("this", node.arguments().get(0).toString());
          assertEquals("foo", node.arguments().get(1).toString());
          ++testsFound[0];
        }
      };
    });
    types.get(1).accept(new ASTVisitor() {
      @Override
      public void endVisit(MethodDeclaration node) {
        if (node.isConstructor()) {
          assertEquals("final NSObject outer$0", node.parameters().get(1).toString());
          assertEquals("val$foo=outer$0;", node.getBody().statements().get(1).toString().trim());
          ++testsFound[0];
        }
      };

      @Override
      public void endVisit(MethodInvocation node) {
        if (node.getName().getIdentifier().equals("NSLog")) {
          assertEquals("val$foo.toString()", node.arguments().get(1).toString());
          ++testsFound[0];
        }
      };
    });
    assertEquals(3, testsFound[0]);
  }

  public void testAnonymousClassInvokingOuterMethod() {
    List<TypeDeclaration> types = translateClassBody(
      "public int getCount() { return 0; } " +
      "Object test() { return new Object() { int getCount() { return Test.this.getCount(); }}; }");
    assertEquals(2, types.size());

    final int[] testsFound = { 0 };
    types.get(1).accept(new ASTVisitor() {
      @Override
      public void endVisit(ReturnStatement node) {
        assertEquals("return this$0.getCount();", node.toString().trim());
        ++testsFound[0];
      }
    });
    assertEquals(1, testsFound[0]);
  }

  public void testAnonymousClassAsInitializer() {
    String source =
        "import java.util.*; public class Test {" +
        "private static final Enumeration<?> EMPTY_ENUMERATION = new Enumeration<Object>() {" +
        "  public boolean hasMoreElements() { return false; }" +
        "  public Object nextElement() { throw new NoSuchElementException(); }}; }";
    CompilationUnit unit = translateType("Test", source);
    List<TypeDeclaration> types = unit.types();
    assertEquals(2, types.size());

    final int[] testsFound = { 0 };
    types.get(0).accept(new ASTVisitor() {
      @Override
      public void endVisit(Assignment node) {
        assertEquals("Test_EMPTY_ENUMERATION_=new $1()", node.toString().trim());
        ++testsFound[0];
      }
    });
    assertEquals(1, testsFound[0]);
  }

  public void testFinalParameterAccess() throws IOException {
    String source = "class Test { Object bar; void foo(final Object bar_) {" +
        "Runnable r = new Runnable() { public void run() { log(1, bar_); }};" +
        "log(2, bar_); }" +
        "private void log(int i, Object o) {}}";
    String translation = translateSourceFile(source, "Test", "Test.m");

    // Test.foo(): since the bar_ parameter shadows a field, the parameter
    // gets renamed to bar_Arg.
    assertTranslation(translation, "- (void)fooWithId:(id)bar_Arg {");
    assertTranslation(translation, "[self logWithInt:2 withId:bar_Arg];");

    // Test_$: since bar_ is an unshadowed field, the parameter name is
    // unchanged.
    assertTranslation(translation, "[this$0_ logWithInt:1 withId:val$bar__];");
    assertTranslation(translation, "val$bar__ = [outer$0 retain]");
  }

  public void testExternalReferenceAsQualifier() throws IOException {
    String translation = translateSourceFile(
      "class Test {" +
      "  class Foo { int i = 0; } " +
      "  void bar() { " +
      "    final Foo foo = new Foo(); " +
      "    Runnable run = new Runnable() { public void run() { int j = foo.i; } }; } }",
      "Test", "Test.m");

    assertTranslation(translation, "int j = ((Test_Foo *) NIL_CHK(val$foo_)).i");
  }

  public void testMultipleReferencesToSameVar() throws IOException {
    String translation = translateSourceFile(
      "class Test {" +
      "  void bar() { " +
      "    final Integer i = new Integer(0); " +
      "    Runnable run = new Runnable() { public void run() { int j = i + i; } }; } }",
      "Test", "Test.m");

    assertTranslation(translation, "initWithTest:(Test *)outer$1");
    assertTranslation(translation, "JavaLangInteger:(JavaLangInteger *)outer$0 {");
  }

  public void testFinalVarInEnhancedForStatement() throws IOException {
    String source = "public class Test { " +
        "void foo(java.util.List<String> strings) { " +
        "  for (final String s : strings) { " +
        "    Runnable r = new Runnable() {" +
        "      public void run() {" +
        "        System.out.println(s);" +
        "      }" +
        "    }; " +
        "  }}}";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation, "NSLog(@\"%@\", val$s_);");
  }

  public void testMethodVarInNestedAnonymousClass() throws IOException {
    String source = "class Test { " +
    "  void bar() { " +
    "    Runnable r1 = new Runnable() { " +
    "      public void run() { " +
    "        final Integer i = 1; " +
    "        Runnable r2 = new Runnable() { " +
    "          public void run() { int j = i + 1; } }; } }; } }";

    // Verify method var in r1.run() isn't mistakenly made a field in r1.
    CompilationUnit unit = translateType("Test", source);
    List<TypeDeclaration> types = unit.types();
    TypeDeclaration r1 = types.get(1);
    assertEquals("Test_$1", NameTable.getFullName(r1));
    for (FieldDeclaration field : r1.getFields()) {
      List<VariableDeclarationFragment> vars = field.fragments();
      for (VariableDeclaration var : vars) {
        if (var.getName().getIdentifier().equals("val$i")) {
          fail("found field that shouldn't be declared");
        }
      }
    }

    // Method var in r1.run() becomes a field in r2.
    TypeDeclaration r2 = types.get(2);
    assertEquals("Test_$1_$1", NameTable.getFullName(r2));
    boolean found = false;
    for (FieldDeclaration field : r2.getFields()) {
      List<VariableDeclarationFragment> vars = field.fragments();
      for (VariableDeclaration var : vars) {
        if (var.getName().getIdentifier().equals("val$i")) {
          found = true;
        }
      }
    }
    assertTrue("required field not found", found);

    // Verify constructor takes both outer field and var.
    ObjectiveCImplementationGenerator.generate("Test.java", Language.OBJECTIVE_C, unit, source);
    String translation = getTranslatedFile("Test.m");
    assertTranslation(translation,
        "r2 = [[[Test_$1_$1 alloc] " +
        "initWithTest_$1:self withJavaLangInteger:i] autorelease]");
  }

  public void testMethodVarInAnonymousClass() throws IOException {
    String source = "class Test { " +
    "  void foo() { " +
    "    if (true) {" +
    "      if (false) {" +
    "        final Integer i = 1;" +
    "        Runnable r = new Runnable() { " +
    "          public void run() { int j = i + 1; } }; }}}}";

    // Verify method var in r1.run() isn't mistakenly made a field in r1.
    CompilationUnit unit = translateType("Test", source);
    List<TypeDeclaration> types = unit.types();
    TypeDeclaration r1 = types.get(1);
    assertEquals("Test_$1", NameTable.getFullName(r1));
    boolean found = false;
    for (FieldDeclaration field : r1.getFields()) {
      List<VariableDeclarationFragment> vars = field.fragments();
      for (VariableDeclaration var : vars) {
        if (var.getName().getIdentifier().equals("val$i")) {
          found = true;
        }
      }
    }
    assertTrue("required field not found", found);

    // Verify constructor takes both outer field and var.
    ObjectiveCImplementationGenerator.generate("Test.java", Language.OBJECTIVE_C, unit, source);
    String translation = getTranslatedFile("Test.m");
    assertTranslation(translation,
        "r = [[[Test_$1 alloc] " +
        "initWithTest:self withJavaLangInteger:i] autorelease]");
  }

  public void testMethodVarInSwitch() throws IOException {
    String source = "class Test { " +
    "  enum E { ONE, TWO };" +
    "  void foo(E e) { " +
    "    switch (e) {" +
    "      case ONE: {" +
    "        final Integer i = 1;" +
    "        Runnable r = new Runnable() { " +
    "          public void run() { int j = i + 1; } }; }}}}";

    // Verify method var in r1.run() isn't mistakenly made a field in r1.
    CompilationUnit unit = translateType("Test", source);
    List<TypeDeclaration> types = unit.types();
    TypeDeclaration r1 = types.get(2);
    assertEquals("Test_$1", NameTable.getFullName(r1));
    boolean found = false;
    for (FieldDeclaration field : r1.getFields()) {
      List<VariableDeclarationFragment> vars = field.fragments();
      for (VariableDeclaration var : vars) {
        if (var.getName().getIdentifier().equals("val$i")) {
          found = true;
        }
      }
    }
    assertTrue("required field not found", found);

    // Verify constructor takes both outer field and var.
    ObjectiveCImplementationGenerator.generate("Test.java", Language.OBJECTIVE_C, unit, source);
    String translation = getTranslatedFile("Test.m");
    assertTranslation(translation,
        "r = [[[Test_$1 alloc] " +
        "initWithTest:self withJavaLangInteger:i] autorelease]");
  }

  public void testAnonymousClassField() throws IOException {
    String source = "class Test { " +
        "  void bar() { " +
        "    Runnable r1 = new Runnable() { " +
        "      int j = 0; " +
        "      public void run() { " +
        "        final Integer i = 1; " +
        "        Runnable r2 = new Runnable() { " +
        "          public void run() { j = i + 1; } }; } }; } }";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation,
        "this$0_.j = [((JavaLangInteger *) NIL_CHK(val$i_)) intValue] + 1;");
  }

  public void testEnumConstantAnonymousClassNaming() throws IOException {
    String source = "public enum Test { " +
        "UP { public boolean isUp() { return true; }}," +
        "DOWN { public boolean isUp() { return false; }};" +
        "public abstract boolean isUp(); }";
    String header = translateSourceFile(source, "Test", "Test.h");
    String impl = getTranslatedFile("Test.m");

    assertTranslation(header, "@interface TestEnum_$1 : TestEnum");
    assertTranslation(header, "@interface TestEnum_$2 : TestEnum");
    assertTranslation(header,
        "- (id)initWithTestEnum:(TestEnum *)outer$0\n" +
        "          withNSString:(NSString *)name\n" +
        "               withInt:(int)ordinal");

    assertTranslation(impl, "if ((self = [super initWithNSString:name withInt:ordinal]))");
    assertTranslation(impl,
        "TestEnum_UP = [[TestEnum_$1 alloc] initWithNSString:@\"Test_UP\" withInt:0];");
    assertTranslation(impl,
        "TestEnum_DOWN = [[TestEnum_$2 alloc] initWithNSString:@\"Test_DOWN\" withInt:1];");
  }

  public void testTwoOutersInAnonymousSubClassOfInner() throws IOException {
    String translation = translateSourceFile("class Test { " +
        "  class B { class Inner { Inner(int i) { } } } " +
        "  class A {" +
        "    B outerB;" +
        "    public B.Inner foo(final B b) {" +
        "      return b.new Inner(1) { public boolean bar() { return b.equals(outerB); } }; } } " +
        "}",
        "Test", "Test.m");
    assertTranslation(translation,
        "[[[Test_A_$1 alloc] initWithTest_A:self withTest_B:b withInt:1 withTest_B:b]");
    assertTranslation(translation,
        "[super initWithTest_B:outer$3 withInt:arg$0]");
  }

  public void testAnonymousClassInStaticBlock() throws IOException {
    String translation = translateSourceFile("class Test { " +
    		"  static class A {" +
    		"    static abstract class Inner { Inner(int i) { } abstract int foo(); } } " +
    		"  static A.Inner inner = new A.Inner(1) { int foo() { return 2; } }; }",
    		"Test", "Test.m");
    // This is probably not the right output - but it compiles and works.
    assertTranslation(translation, "[[Test_$1 alloc] initWithInt:1");
  }

  public void testAnonymousClassObjectParameter() throws IOException {
    String translation = translateSourceFile("class Test {" +
        "  abstract static class A { " +
        "    A(Object o) { } " +
        "    abstract void foo();" +
        "  } " +
        "  void bar(Object o) { A a = new A(o) { void foo() { } }; } }",
        "Test", "Test.h");
    assertTranslation(translation,
        "- (id)initWithTest:(Test *)outer$1\n" +
        "            withId:");
  }

  public void testEnumWithParametersAndInnerClasses() throws IOException {
    String impl = translateSourceFile(
      "public enum Color { " +
      "RED(42) { public int getRGB() { return 0xF00; }}, " +
      "GREEN(-1) { public int getRGB() { return 0x0F0; }}, " +
      "BLUE(666) { public int getRGB() { return 0x00F; }};" +
      "Color(int n) {} public int getRGB() { return 0; }}",
      "Color", "Color.m");

    // Verify ColorEnum constructor.
    assertTranslation(impl,
        "- (id)initWithInt:(int)n\n" +
        "     withNSString:(NSString *)name\n" +
        "          withInt:(int)ordinal {\n" +
        "  return [super initWithNSString:name withInt:ordinal];\n}");

    // Verify ColorEnum_$1 constructor.
    assertTranslation(impl,
        "- (id)initWithColorEnum:(ColorEnum *)outer$1\n" +
        "                withInt:(int)arg$0\n" +
        "           withNSString:(NSString *)name\n" +
        "                withInt:(int)ordinal {\n" +
        "  if ((self = [super initWithInt:arg$0 withNSString:name withInt:ordinal])) {\n" +
        "    ([this$0_ autorelease], this$0_ = [outer$1 retain]);\n" +
        "  }\n" +
        "  return self;\n}");

    // Verify constant initialization.
    assertTranslation(impl,
        "[[ColorEnum_$1 alloc] initWithInt:42 withNSString:@\"Color_RED\" withInt:0]");
  }
}
