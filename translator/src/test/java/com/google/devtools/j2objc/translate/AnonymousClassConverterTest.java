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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.NameTable;
import java.io.IOException;
import java.util.List;

/**
 * Unit tests for {@link AnonymousClassConverter}.
 *
 * @author Tom Ball
 */
public class AnonymousClassConverterTest extends GenerationTest {

  protected List<TypeDeclaration> translateClassBody(String testSource) {
    String source = "public class Test { " + testSource + " }";
    CompilationUnit unit = translateType("Test", source);
    return Lists.newArrayList(Iterables.filter(unit.getTypes(), TypeDeclaration.class));
  }

  public void testAnonymousClassNaming() throws IOException {
    String source = "import java.util.*; public class Test { "
        + "Set keySet() { return new AbstractSet() { "
        + "  public int size() { return 0; }"
        + "  public Iterator iterator() { return new Iterator() {"
        + "    public boolean hasNext() { return false; } "
        + "    public Object next() { return null; }"
        + "    public void remove() {}};}};}"
        + "Collection values() { return new AbstractCollection() {"
        + "  public int size() { return 0; }"
        + "  public Iterator iterator() { return new Iterator() {"
        + "    public boolean hasNext() { return false; } "
        + "    public Object next() { return null; }"
        + "    public void remove() {}};}};}"
        + "}";
    String impl = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(impl, "@interface Test_1_1 : NSObject < JavaUtilIterator >");
    assertTranslation(impl, "@interface Test_1 : JavaUtilAbstractSet");
    assertTranslation(impl, "@interface Test_2_1 : NSObject < JavaUtilIterator >");
    assertTranslation(impl, "@interface Test_2 : JavaUtilAbstractCollection");
  }

  public void testFinalArrayInnerAccess() throws IOException {
    String source = "public class Test { void foo() { "
        + "final boolean[] bar = new boolean[1];"
        + "Runnable r = new Runnable() { public void run() { bar[0] = true; }}; }}";
    String impl = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(impl, "IOSBooleanArray *val$bar_;");
    assertTranslation(impl,
        "- (instancetype)initWithBooleanArray:(IOSBooleanArray *)capture$0;");
    assertTranslation(impl, "IOSBooleanArray *bar = [IOSBooleanArray arrayWithLength:1];");
    assertTranslation(impl, "create_Test_1_initWithBooleanArray_(bar)");
    assertTranslation(impl, "*IOSBooleanArray_GetRef(nil_chk(val$bar_), 0) = true;");
  }

  /**
   * Verify that an anonymous class is moved to the compilation unit's types list.
   */
  public void testAnonymousClassExtracted() {
    List<TypeDeclaration> types = translateClassBody(
        "Object test() { return new java.util.Enumeration<Object>() { "
        + "public boolean hasMoreElements() { return false; } "
        + "public Object nextElement() { return null; } }; }");
    assertEquals(2, types.size());

    TypeDeclaration type = types.get(1);
    ElementUtil elementUtil = TreeUtil.getCompilationUnit(type).getEnv().elementUtil();
    assertEquals("Test$1", elementUtil.getBinaryName(type.getTypeElement()));
  }

  /**
   * Regression test: verify that a class passed in the constructor of an
   * anonymous class is converted.
   */
  public void testAnonymousClassWithTypeArgParameter() throws IOException {
    String translation = translateSourceFile(
        "class Test { public Test(Class c) {} static Test t = "
        + "new Test(Test.class) { @Override public int hashCode() { return 1; } }; }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "+ (void)initialize {",
        "if (self == [Test class]) {",
        "JreStrongAssignAndConsume(&Test_t, new_Test_1_initWithIOSClass_(Test_class_()));");
  }

  public void testFinalParameter() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test(final Object test) {"
        + "  Runnable r = new Runnable() {"
        + "    public void run() {"
        + "      System.out.println(test.toString());"
        + "    } }; } }", "Test", "Test.m");
    assertTranslation(translation,
        "id<JavaLangRunnable> r = create_Test_1_initWithId_(test);");
    assertTranslatedLines(translation,
        "void Test_1_initWithId_(Test_1 *self, id capture$0) {",
        "  JreStrongAssign(&self->val$test_, capture$0);",
        "  NSObject_init(self);",
        "}");
    assertTranslation(translation, "[nil_chk(val$test_) description]");
  }

  public void testFinalLocalVariable() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test() {"
        + "  final Object foo = new Object();"
        + "  Runnable r = new Runnable() {"
        + "    public void run() {"
        + "      System.out.println(foo.toString());"
        + "    } }; } }", "Test", "Test.m");
    assertTranslation(translation,
        "id<JavaLangRunnable> r = create_Test_1_initWithId_(foo);");
    assertTranslatedLines(translation,
        "void Test_1_initWithId_(Test_1 *self, id capture$0) {",
        "  JreStrongAssign(&self->val$foo_, capture$0);",
        "  NSObject_init(self);",
        "}");
    assertTranslation(translation, "[nil_chk(val$foo_) description]");
  }

  public void testAnonymousClassInvokingOuterMethod() throws IOException {
    String translation = translateSourceFile(
        "class Test { public int getCount() { return 0; } "
        + "Object test() { return new Object() { "
        + "int getCount() { return Test.this.getCount(); } }; } }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "- (jint)getCount {",
        "return [this$0_ getCount];");
  }

  public void testAnonymousClassAsInitializer() throws IOException {
    String translation = translateSourceFile(
        "import java.util.*; public class Test {"
        + "private static final Enumeration<?> EMPTY_ENUMERATION = new Enumeration<Object>() {"
        + "  public boolean hasMoreElements() { return false; }"
        + "  public Object nextElement() { throw new NoSuchElementException(); }}; }",
        "Test", "Test.m");
    assertTranslation(translation, "id<JavaUtilEnumeration> Test_EMPTY_ENUMERATION;");
    assertTranslatedLines(translation,
        "+ (void)initialize {",
        "if (self == [Test class]) {",
        "JreStrongAssignAndConsume(&Test_EMPTY_ENUMERATION, new_Test_1_init());");
  }

  public void testFinalParameterAccess() throws IOException {
    String source = "class Test { Object bar; void foo(final Object bar_) {"
        + "Runnable r = new Runnable() { public void run() { log(1, bar_); }};"
        + "log(2, bar_); }"
        + "private void log(int i, Object o) {}}";
    String translation = translateSourceFile(source, "Test", "Test.m");

    // Test.foo(): since the bar_ parameter shadows a field, the parameter
    // gets renamed to bar_Arg.
    assertTranslation(translation, "- (void)fooWithId:(id)bar_Arg {");
    assertTranslation(translation, "Test_logWithInt_withId_(self, 2, bar_Arg);");

    // Test_$: since bar_ is an unshadowed field, the parameter name is
    // unchanged.
    assertTranslation(translation, "Test_logWithInt_withId_(this$0_, 1, val$bar__);");
    assertTranslation(translation, "JreStrongAssign(&self->val$bar__, capture$0);");
  }

  public void testExternalReferenceAsQualifier() throws IOException {
    String translation = translateSourceFile(
      "class Test {"
      + "  class Foo { int i = 0; } "
      + "  void bar() { "
      + "    final Foo foo = new Foo(); "
      + "    Runnable run = new Runnable() { public void run() { int j = foo.i; } }; } }",
      "Test", "Test.m");

    assertTranslation(translation, "int j = ((Test_Foo *) nil_chk(val$foo_))->i_");
  }

  public void testMultipleReferencesToSameVar() throws IOException {
    String translation = translateSourceFile(
      "class Test {"
      + "  void bar() { "
      + "    final Integer i = new Integer(0); "
      + "    Runnable run = new Runnable() { public void run() { int j = i + i; } }; } }",
      "Test", "Test.m");

    assertTranslation(translation, "initWithJavaLangInteger:(JavaLangInteger *)capture$0 {");
  }

  public void testFinalVarInEnhancedForStatement() throws IOException {
    String source = "public class Test { "
        + "void foo(java.util.List<String> strings) { "
        + "  for (final String s : strings) { "
        + "    Runnable r = new Runnable() {"
        + "      public void run() {"
        + "        System.out.println(s);"
        + "      }"
        + "    }; "
        + "  }}}";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation,
        "[((JavaIoPrintStream *) nil_chk(JreLoadStatic(JavaLangSystem, out))) "
        + "printlnWithNSString:val$s_];");
  }

  public void testMethodVarInNestedAnonymousClass() throws IOException {
    String source = "class Test { "
        + "  void bar() { "
        + "    Runnable r1 = new Runnable() { "
        + "      public void run() { "
        + "        final Integer i = 1; "
        + "        Runnable r2 = new Runnable() { "
        + "          public void run() { int j = i + 1; } }; } }; } }";

    // Verify method var in r1.run() isn't mistakenly made a field in r1.
    CompilationUnit unit = translateType("Test", source);
    NameTable nameTable = unit.getEnv().nameTable();
    List<AbstractTypeDeclaration> types = unit.getTypes();
    AbstractTypeDeclaration r1 = types.get(1);
    assertEquals("Test_1", nameTable.getFullName(r1.getTypeElement()));
    for (VariableDeclarationFragment var : TreeUtil.getAllFields(r1)) {
      if (ElementUtil.getName(var.getVariableElement()).equals("val$i")) {
        fail("found field that shouldn't be declared");
      }
    }

    // Method var in r1.run() becomes a field in r2.
    AbstractTypeDeclaration r2 = types.get(2);
    assertEquals("Test_1_1", nameTable.getFullName(r2.getTypeElement()));
    boolean found = false;
    for (VariableDeclarationFragment var : TreeUtil.getAllFields(r2)) {
      if (ElementUtil.getName(var.getVariableElement()).equals("val$i")) {
        found = true;
      }
    }
    assertTrue("required field not found", found);

    // Verify constructor takes both outer field and var.
    String translation = generateFromUnit(unit, "Test.m");
    assertTranslation(translation, "r2 = create_Test_1_1_initWithJavaLangInteger_(i)");
  }

  public void testMethodVarInAnonymousClass() throws IOException {
    String source = "class Test { "
        + "  boolean debug;"
        + "  void foo() { "
        + "    if (true) {"
        + "      if (debug) {"
        + "        final Integer i = 1;"
        + "        Runnable r = new Runnable() { "
        + "          public void run() { int j = i + 1; } }; }}}}";

    // Verify method var in r1.run() isn't mistakenly made a field in r1.
    CompilationUnit unit = translateType("Test", source);
    NameTable nameTable = unit.getEnv().nameTable();
    List<AbstractTypeDeclaration> types = unit.getTypes();
    AbstractTypeDeclaration r1 = types.get(1);
    assertEquals("Test_1", nameTable.getFullName(r1.getTypeElement()));
    boolean found = false;
    for (VariableDeclarationFragment var : TreeUtil.getAllFields(r1)) {
      if (ElementUtil.getName(var.getVariableElement()).equals("val$i")) {
        found = true;
      }
    }
    assertTrue("required field not found", found);

    // Verify method var is passed to constructor.
    String translation = generateFromUnit(unit, "Test.m");
    assertTranslation(translation, "r = create_Test_1_initWithJavaLangInteger_(i)");
  }

  public void testMethodVarInSwitch() throws IOException {
    String source = "class Test { "
        + "  enum E { ONE, TWO };"
        + "  void foo(E e) { "
        + "    switch (e) {"
        + "      case ONE: {"
        + "        final Integer i = 1;"
        + "        Runnable r = new Runnable() { "
        + "          public void run() { int j = i + 1; } }; }}}}";

    // Verify method var in r1.run() isn't mistakenly made a field in r1.
    CompilationUnit unit = translateType("Test", source);
    NameTable nameTable = unit.getEnv().nameTable();
    List<AbstractTypeDeclaration> types = unit.getTypes();
    AbstractTypeDeclaration r1 = types.get(2);
    assertEquals("Test_1", nameTable.getFullName(r1.getTypeElement()));
    boolean found = false;
    for (VariableDeclarationFragment var : TreeUtil.getAllFields(r1)) {
      if (ElementUtil.getName(var.getVariableElement()).equals("val$i")) {
        found = true;
      }
    }
    assertTrue("required field not found", found);

    // Verify method var is passed to constructor.
    String translation = generateFromUnit(unit, "Test.m");
    assertTranslation(translation, "r = create_Test_1_initWithJavaLangInteger_(i)");
  }

  public void testAnonymousClassField() throws IOException {
    String source = "class Test { "
        + "  void bar() { "
        + "    Runnable r1 = new Runnable() { "
        + "      int j = 0; "
        + "      public void run() { "
        + "        final Integer i = 1; "
        + "        Runnable r2 = new Runnable() { "
        + "          public void run() { j = i + 1; } }; } }; } }";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation,
        "this$0_->j_ = [((JavaLangInteger *) nil_chk(val$i_)) intValue] + 1;");
  }

  public void testEnumConstantAnonymousClassNaming() throws IOException {
    String source = "public enum Test { "
        + "UP { public boolean isUp() { return true; }},"
        + "DOWN { public boolean isUp() { return false; }};"
        + "public abstract boolean isUp(); }";
    String impl = translateSourceFile(source, "Test", "Test.m");

    assertTranslation(impl, "@interface Test_1 : Test");
    assertTranslation(impl, "@interface Test_2 : Test");

    assertTranslation(impl, "Test_initWithNSString_withInt_(self, __name, __ordinal);");
    assertTranslation(impl, "Test_1_initWithNSString_withInt_(e, @\"UP\", 0);");
    assertTranslation(impl, "Test_2_initWithNSString_withInt_(e, @\"DOWN\", 1);");
  }

  public void testTwoOutersInAnonymousSubClassOfInner() throws IOException {
    String translation = translateSourceFile("class Test { "
        + "  class B { class Inner { Inner(int i) { } } } "
        + "  class A {"
        + "    B outerB;"
        + "    public B.Inner foo(final B b) {"
        + "      return b.new Inner(1) { public boolean bar() { return b.equals(outerB); } }; } } "
        + "}",
        "Test", "Test.m");
    assertTranslation(translation,
        "create_Test_A_1_initWithTest_A_withTest_B_withTest_B_withInt_(self, b, b, 1)");
    String param0 = options.isJDT() ? "param0" : "i";
    String superOuter = options.isJDT() ? "superOuter$" : "x0";
    assertTranslatedLines(translation,
        "void Test_A_1_initWithTest_A_withTest_B_withTest_B_withInt_("
          + "Test_A_1 *self, Test_A *outer$, Test_B *capture$0, Test_B *" + superOuter + ", "
          + "jint " + param0 + ") {",
        "  JreStrongAssign(&self->this$1_, outer$);",
        "  JreStrongAssign(&self->val$b_, capture$0);",
        "  Test_B_Inner_initWithTest_B_withInt_(self, nil_chk(" + superOuter + "), " + param0
          + ");",
        "}");
  }

  public void testAnonymousClassInStaticBlock() throws IOException {
    String translation = translateSourceFile("class Test { "
        + "  static class A {"
        + "    static abstract class Inner { Inner(int i) { } abstract int foo(); } } "
        + "  static A.Inner inner = new A.Inner(1) { int foo() { return 2; } }; }",
        "Test", "Test.m");
    // This is probably not the right output - but it compiles and works.
    assertTranslation(translation, "new_Test_1_initWithInt_(1)");
  }

  public void testAnonymousClassObjectParameter() throws IOException {
    String translation = translateSourceFile("class Test {"
        + "  abstract static class A { "
        + "    A(Object o) { } "
        + "    abstract void foo();"
        + "  } "
        + "  void bar(Object o) { A a = new A(o) { void foo() { } }; } }",
        "Test", "Test.h");
    assertTranslation(translation, "- (instancetype)initWithId:");
  }

  public void testEnumWithParametersAndInnerClasses() throws IOException {
    String impl = translateSourceFile(
      "public enum Color { "
      + "RED(42) { public int getRGB() { return 0xF00; }}, "
      + "GREEN(-1) { public int getRGB() { return 0x0F0; }}, "
      + "BLUE(666) { public int getRGB() { return 0x00F; }};"
      + "Color(int n) {} public int getRGB() { return 0; }}",
      "Color", "Color.m");

    // Verify Color constructor.
    assertTranslatedLines(impl,
        "void Color_initWithInt_withNSString_withInt_("
          + "Color *self, jint n, NSString *__name, jint __ordinal) {",
        "  JavaLangEnum_initWithNSString_withInt_(self, __name, __ordinal);",
        "}");

    String param0 = options.isJDT() ? "param0" : "n";
    // Verify Color_1 constructor.
    assertTranslatedLines(impl,
        "void Color_1_initWithInt_withNSString_withInt_("
          + "Color_1 *self, jint " + param0 + ", NSString *__name, jint __ordinal) {",
        "  Color_initWithInt_withNSString_withInt_(self, " + param0 + ", __name, __ordinal);",
        "}");

    // Verify constant initialization.
    assertTranslation(impl, "Color_1_initWithInt_withNSString_withInt_(e, 42, @\"RED\", 0)");
  }

  public void testEnumWithInnerEnum() throws IOException {
    String impl = translateSourceFile(
      "public enum OuterValue {\n"
      + "  VALUE1, VALUE2, VALUE3;\n"
      + "  public enum InnerValue {\n"
      + "    VALUE1, VALUE2, VALUE3;\n"
      + "  }\n"
      + "}\n",
      "OuterValue", "OuterValue.m");

    // Verify OuterValue constant initialization.
    assertTranslation(impl, "OuterValue_initWithNSString_withInt_(e, names[i], i);");

    // Verify InnerValue constant initialization.
    assertTranslation(impl, "OuterValue_InnerValue_initWithNSString_withInt_(e, names[i], i);");
  }

  // Tests a field initialized with an anonymous class and multiple
  // constructors. Field initialization is moved to the constructors,
  // duplicating the initialization statement, but we do not want to duplicate
  // the implementation.
  public void testAnonymousClassNotDuplicated() throws IOException {
    String impl = translateSourceFile(
        "public class A { "
        + "  interface I { public int getInt(); } "
        + "  private I my_i = new I() { public int getInt() { return 42; } }; "
        + "  A() {} "
        + "  A(String foo) {} }",
        "A", "A.m");
    assertOccurrences(impl, "@implementation A_1", 1);
    assertOccurrences(impl, "JreStrongAssignAndConsume(&self->my_i_, new_A_1_init());", 2);
  }

  public void testNestedAnonymousClasses() throws IOException {
    String impl = translateSourceFile(
        "class Test { void test(final int i) { Runnable r = new Runnable() { "
        + "public void run() { Runnable r2 = new Runnable() { public void run() { "
        + "int i2 = i; } }; } }; } }", "Test", "Test.m");
    assertTranslation(impl, "int i2 = this$0_->val$i_;");
  }

  // Verify that an anonymous class can be defined with a null constructor
  // parameter.
  public void testDefaultConstructorWithNullParameter() throws IOException {
    translateSourceFile(
      "class Test {"
      + "  static Test instance = new Test(null) {};"
      + "  protected Test(String s) {} }",
      "Test", "Test.m");
    // The test is successful if the above doesn't throw an NPE.
  }

  public void testAnonymousClassWithGenericConstructor() throws IOException {
    String translation = translateSourceFile(
        "class Test<T> { Test(T t) {} void test() { new Test<String>(\"foo\") {}; } }",
        "Test", "Test.m");
    String param0 = options.isJDT() ? "param0" : "t";
    assertTranslation(translation, "create_Test_1_initWithNSString_(@\"foo\")");
    assertTranslation(translation, "- (instancetype)initWithNSString:(NSString *)" + param0 + " {");
    assertTranslatedLines(translation,
        "void Test_1_initWithNSString_(Test_1 *self, NSString *" + param0 + ") {",
        "  Test_initWithId_(self, " + param0 + ");",
        "}");
  }

  public void testAnonymousClassWithVarargsConstructor() throws IOException {
    String translation = translateSourceFile(
        "class Test { Test(String fmt, Object... args) {} "
        + "  void test() { new Test(\"%s %s\", \"1\", \"2\") {}; } "
        + "  void test2() { new Test(\"foo\") {}; } }",
        "Test", "Test.m");
    // check the invocations.
    assertTranslation(translation,
        "create_Test_1_initWithNSString_withNSObjectArray_(@\"%s %s\", "
        + "[IOSObjectArray arrayWithObjects:(id[]){ @\"1\", @\"2\" } count:2 "
        + "type:NSObject_class_()]);");
    assertTranslation(translation,
        "create_Test_2_initWithNSString_withNSObjectArray_(@\"foo\", "
        + "[IOSObjectArray arrayWithLength:0 type:NSObject_class_()]);");
    String param0 = options.isJDT() ? "param0" : "fmt";
    String param1 = options.isJDT() ? "param1" : "args";
    // check the generated constructors.
    assertTranslatedLines(translation,
        "void Test_1_initWithNSString_withNSObjectArray_("
          + "Test_1 *self, NSString *" + param0 + ", IOSObjectArray *" + param1 + ") {",
        "  Test_initWithNSString_withNSObjectArray_(self, " + param0 + ", " + param1 + ");",
        "}");
    assertTranslatedLines(translation,
        "void Test_2_initWithNSString_withNSObjectArray_("
          + "Test_2 *self, NSString *" + param0 + ", IOSObjectArray *" + param1 + ") {",
        "  Test_initWithNSString_withNSObjectArray_(self, " + param0 + ", " + param1 + ");",
        "}");
  }

  public void testAnonymousClassWithinLambdaWithSuperOuterParam() throws IOException {
    String translation = translateSourceFile(
        "class Test { interface I { A get(); } class A {} "
        + "static class B { String s;  I test(Test t, int i) { "
        + "return () -> t.new A() { public String toString() { return s + i; } }; } } }",
        "Test", "Test.m");
    String superOuter = options.isJDT() ? "superOuter$" : "x0";
    assertTranslation(translation,
        "static Test_B_1 *create_Test_B_1_initWithTest_B_withInt_withTest_("
        + "Test_B *outer$, jint capture$0, Test *" + superOuter + ");");
    assertTranslation(translation,
        "return create_Test_B_1_initWithTest_B_withInt_withTest_(this$0_, val$i_, val$t_);");
    // The super outer must be nil_chk'ed in the anonymous constructor.
    assertTranslation(translation, "Test_A_initWithTest_(self, nil_chk(" + superOuter + "));");
  }

  public void testSuperclassHasCapturedVariables() throws IOException {
    String translation = translateSourceFile(
        "class Test { static Object test(int i) { class Local { int foo() { return i; } } "
        + "return new Local() { int bar() { return i; } }; } }", "Test", "Test.m");
    // Test that the anonymous class captures i and passes it to Local's constructor.
    assertTranslatedLines(translation,
        "void Test_1_initWithInt_(Test_1 *self, jint capture$0) {",
        "  self->val1$i_ = capture$0;",
        "  Test_1Local_initWithInt_(self, capture$0);",
        "}");
  }

  public void testGenericConstructorCalledByAnonymousClass() throws IOException {
    if (!options.isJDT()) {
      // JDT fails with "could not find constructor".
      String translation = translateSourceFile(
          "class Test { <T> Test(T t) {} Test create() { return new Test(\"foo\") {}; } }",
          "Test", "Test.m");
      assertTranslatedLines(translation,
          "void Test_1_initWithId_(Test_1 *self, id t) {",
          "  Test_initWithId_(self, t);",
          "}");
    }
  }
}
