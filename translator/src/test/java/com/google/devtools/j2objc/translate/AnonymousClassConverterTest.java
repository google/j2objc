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
import com.google.devtools.j2objc.gen.ObjectiveCImplementationGenerator;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
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
    assertTranslation(header,
        "- (id)initWithBooleanArray:(IOSBooleanArray *)capture$0;");
    assertTranslation(impl, "IOSBooleanArray *bar = [IOSBooleanArray arrayWithLength:1];");
    assertTranslation(impl, "[[Test_$1 alloc] initWithBooleanArray:bar]");
    assertTranslation(impl, "(*IOSBooleanArray_GetRef(nil_chk(val$bar_), 0)) = YES;");
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
  public void testAnonymousClassWithTypeArgParameter() throws IOException {
    String translation = translateSourceFile(
        "class Test { public Test(Class c) {} static Test t = " +
        "new Test(Test.class) { @Override public int hashCode() { return 1; } }; }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "+ (void)initialize {",
        "if (self == [Test class]) {",
        "JreOperatorRetainedAssign(&Test_t_, nil, [[[Test_$1 alloc] " +
            "initWithIOSClass:[IOSClass classWithClass:[Test class]]] autorelease]);");
  }

  public void testFinalParameter() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test(final Object test) {" +
        "  Runnable r = new Runnable() {" +
        "    public void run() {" +
        "      System.out.println(test.toString());" +
        "    } }; } }", "Test", "Test.m");
    assertTranslation(translation,
        "id<JavaLangRunnable> r = [[[Test_$1 alloc] initWithId:test] autorelease];");
    assertTranslatedLines(translation,
        "- (id)initWithId:(id)capture$0 {",
        "Test_$1_set_val$test_(self, capture$0);",
        "return JreMemDebugAdd([super init]);",
        "}");
    assertTranslation(translation, "[nil_chk(val$test_) description]");
  }

  public void testFinalLocalVariable() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test() {" +
        "  final Object foo = new Object();" +
        "  Runnable r = new Runnable() {" +
        "    public void run() {" +
        "      System.out.println(foo.toString());" +
        "    } }; } }", "Test", "Test.m");
    assertTranslation(translation,
        "id<JavaLangRunnable> r = [[[Test_$1 alloc] initWithId:foo] autorelease];");
    assertTranslatedLines(translation,
        "- (id)initWithId:(id)capture$0 {",
        "Test_$1_set_val$foo_(self, capture$0);",
        "return JreMemDebugAdd([super init]);",
        "}");
    assertTranslation(translation, "[nil_chk(val$foo_) description]");
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

  public void testAnonymousClassAsInitializer() throws IOException {
    String translation = translateSourceFile(
        "import java.util.*; public class Test {" +
        "private static final Enumeration<?> EMPTY_ENUMERATION = new Enumeration<Object>() {" +
        "  public boolean hasMoreElements() { return false; }" +
        "  public Object nextElement() { throw new NoSuchElementException(); }}; }",
        "Test", "Test.m");
    assertTranslation(translation, "id<JavaUtilEnumeration> Test_EMPTY_ENUMERATION_;");
    assertTranslatedLines(translation,
        "+ (void)initialize {",
        "if (self == [Test class]) {",
        "JreOperatorRetainedAssign(&Test_EMPTY_ENUMERATION_, nil, " +
            "[[[Test_$1 alloc] init] autorelease]);");
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
    assertTranslation(translation, "Test_$1_set_val$bar__(self, capture$0);");
  }

  public void testExternalReferenceAsQualifier() throws IOException {
    String translation = translateSourceFile(
      "class Test {" +
      "  class Foo { int i = 0; } " +
      "  void bar() { " +
      "    final Foo foo = new Foo(); " +
      "    Runnable run = new Runnable() { public void run() { int j = foo.i; } }; } }",
      "Test", "Test.m");

    assertTranslation(translation, "int j = ((Test_Foo *) nil_chk(val$foo_))->i_");
  }

  public void testMultipleReferencesToSameVar() throws IOException {
    String translation = translateSourceFile(
      "class Test {" +
      "  void bar() { " +
      "    final Integer i = new Integer(0); " +
      "    Runnable run = new Runnable() { public void run() { int j = i + i; } }; } }",
      "Test", "Test.m");

    assertTranslation(translation, "initWithJavaLangInteger:(JavaLangInteger *)capture$0 {");
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
    assertTranslation(translation,
        "[((JavaIoPrintStream *) nil_chk(JavaLangSystem_get_out_())) printlnWithNSString:val$s_];");
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
    ObjectiveCImplementationGenerator.generate("Test.java", unit, source);
    String translation = getTranslatedFile("Test.m");
    assertTranslation(translation,
        "r2 = [[[Test_$1_$1 alloc] initWithJavaLangInteger:i] autorelease]");
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

    // Verify method var is passed to constructor.
    ObjectiveCImplementationGenerator.generate("Test.java", unit, source);
    String translation = getTranslatedFile("Test.m");
    assertTranslation(translation, "r = [[[Test_$1 alloc] initWithJavaLangInteger:i] autorelease]");
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

    // Verify method var is passed to constructor.
    ObjectiveCImplementationGenerator.generate("Test.java", unit, source);
    String translation = getTranslatedFile("Test.m");
    assertTranslation(translation,
        "r = [[[Test_$1 alloc] initWithJavaLangInteger:i] autorelease]");
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
        "this$0_->j_ = [((JavaLangInteger *) nil_chk(val$i_)) intValue] + 1;");
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
        "- (id)initWithNSString:(NSString *)__name withInt:(int)__ordinal");

    assertTranslation(impl, "[super initWithNSString:__name withInt:__ordinal]");
    assertTranslation(impl,
        "TestEnum_UP = [[TestEnum_$1 alloc] initWithNSString:@\"UP\" withInt:0];");
    assertTranslation(impl,
        "TestEnum_DOWN = [[TestEnum_$2 alloc] initWithNSString:@\"DOWN\" withInt:1];");
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
        "[super initWithTest_B:superOuter$ withInt:arg$0]");
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
    assertTranslation(translation, "- (id)initWithId:");
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
        "     withNSString:(NSString *)__name\n" +
        "          withInt:(int)__ordinal {\n" +
        "  return JreMemDebugAdd([super initWithNSString:__name withInt:__ordinal]);\n}");

    // Verify ColorEnum_$1 constructor.
    assertTranslation(impl,
        "- (id)initWithInt:(int)arg$0\n" +
        "     withNSString:(NSString *)__name\n" +
        "          withInt:(int)__ordinal {\n" +
        "  return JreMemDebugAdd([super initWithInt:arg$0 " +
        "withNSString:__name withInt:__ordinal]);\n}");

    // Verify constant initialization.
    assertTranslation(impl,
        "[[ColorEnum_$1 alloc] initWithInt:42 withNSString:@\"RED\" withInt:0]");
  }

  public void testEnumWithInnerEnum() throws IOException {
    String impl = translateSourceFile(
      "public enum OuterValue {\n" +
      "  VALUE1, VALUE2, VALUE3;\n" +
      "  public enum InnerValue {\n" +
      "    VALUE1, VALUE2, VALUE3;\n" +
      "  }\n" +
      "}\n",
      "OuterValue", "OuterValue.m");

    // Verify OuterValue constant initialization.
    assertTranslation(impl,
        "[[OuterValueEnum alloc] initWithNSString:@\"VALUE1\" withInt:0]");

    // Verify InnerValue constant initialization.
    assertTranslation(impl,
        "[[OuterValueEnum_InnerValueEnum alloc] initWithNSString:@\"VALUE1\" withInt:0]");
  }

  // Tests a field initialized with an anonymous class and multiple
  // constructors. Field initialization is moved to the constructors,
  // duplicating the initialization statement, but we do not want to duplicate
  // the implementation.
  public void testAnonymousClassNotDuplicated() throws IOException {
    String impl = translateSourceFile(
        "public class A { " +
        "  interface I { public int getInt(); } " +
        "  private I my_i = new I() { public int getInt() { return 42; } }; " +
        "  A() {} " +
        "  A(String foo) {} }",
        "A", "A.m");
    assertOccurrences(impl, "@implementation A_$1", 1);
    assertOccurrences(impl, "[[A_$1 alloc] init]", 2);
  }

  public void testNestedAnonymousClasses() throws IOException {
    String impl = translateSourceFile(
        "class Test { void test(final int i) { Runnable r = new Runnable() { " +
        "public void run() { Runnable r2 = new Runnable() { public void run() { " +
        "int i2 = i; } }; } }; } }", "Test", "Test.m");
    assertTranslation(impl, "int i2 = this$0_->val$i_;");
  }

  // Verify that an anonymous class can be defined with a null constructor
  // parameter.
  public void testDefaultConstructorWithNullParameter() throws IOException {
    translateSourceFile(
      "class Test {" +
      "  static Test instance = new Test(null) {};" +
      "  protected Test(String s) {} }",
      "Test", "Test.m");
    // The test is successful if the above doesn't throw an NPE.
  }

  public void testAnonymousClassWithGenericConstructor() throws IOException {
    String translation = translateSourceFile(
        "class Test<T> { Test(T t) {} void test() { new Test<String>(\"foo\") {}; } }",
        "Test", "Test.m");
    assertTranslation(translation, "[[Test_$1 alloc] initWithNSString:@\"foo\"]");
    assertTranslation(translation, "- (id)initWithNSString:(NSString *)arg$0 {");
    assertTranslation(translation, "[super initWithId:arg$0]");
  }

  public void testAnonymousClassWithVarargsConstructor() throws IOException {
    String translation = translateSourceFile(
        "class Test { Test(String fmt, Object... args) {} " +
        "  void test() { new Test(\"%s %s\", \"1\", \"2\") {}; } }",
        "Test", "Test.m");
    assertTranslation(translation,
        "[super initWithNSString:arg$0 withNSObjectArray:" +
        "[IOSObjectArray arrayWithObjects:(id[]){ arg$1, arg$2 } count:2 " +
        "type:[IOSClass classWithClass:[NSObject class]]]]");
  }
}
