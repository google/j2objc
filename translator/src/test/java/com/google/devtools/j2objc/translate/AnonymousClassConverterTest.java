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
    assertTranslation(impl, "@interface Test_$1_$1 : NSObject < JavaUtilIterator >");
    assertTranslation(impl, "@interface Test_$1 : JavaUtilAbstractSet");
    assertTranslation(impl, "@interface Test_$2_$1 : NSObject < JavaUtilIterator >");
    assertTranslation(impl, "@interface Test_$2 : JavaUtilAbstractCollection");
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
    assertTranslation(impl, "new_Test_$1_initWithBooleanArray_(bar)");
    assertTranslation(impl, "*IOSBooleanArray_GetRef(nil_chk(val$bar_), 0) = YES;");
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
    assertEquals("Test$1", type.getTypeBinding().getBinaryName());
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
        "JreStrongAssignAndConsume(&Test_t_, new_Test_$1_initWithIOSClass_(Test_class_()));");
  }

  public void testFinalParameter() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test(final Object test) {"
        + "  Runnable r = new Runnable() {"
        + "    public void run() {"
        + "      System.out.println(test.toString());"
        + "    } }; } }", "Test", "Test.m");
    assertTranslation(translation,
        "id<JavaLangRunnable> r = [new_Test_$1_initWithId_(test) autorelease];");
    assertTranslatedLines(translation,
        "void Test_$1_initWithId_(Test_$1 *self, id capture$0) {",
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
        "id<JavaLangRunnable> r = [new_Test_$1_initWithId_(foo) autorelease];");
    assertTranslatedLines(translation,
        "void Test_$1_initWithId_(Test_$1 *self, id capture$0) {",
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
    assertTranslation(translation, "id<JavaUtilEnumeration> Test_EMPTY_ENUMERATION_;");
    assertTranslatedLines(translation,
        "+ (void)initialize {",
        "if (self == [Test class]) {",
        "JreStrongAssignAndConsume(&Test_EMPTY_ENUMERATION_, new_Test_$1_init());");
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
        "[((JavaIoPrintStream *) nil_chk(JreLoadStatic(JavaLangSystem, out_))) "
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
    NameTable nameTable = unit.getNameTable();
    List<AbstractTypeDeclaration> types = unit.getTypes();
    AbstractTypeDeclaration r1 = types.get(1);
    assertEquals("Test_$1", nameTable.getFullName(r1.getTypeBinding()));
    for (VariableDeclarationFragment var : TreeUtil.getAllFields(r1)) {
      if (var.getName().getIdentifier().equals("val$i")) {
        fail("found field that shouldn't be declared");
      }
    }

    // Method var in r1.run() becomes a field in r2.
    AbstractTypeDeclaration r2 = types.get(2);
    assertEquals("Test_$1_$1", nameTable.getFullName(r2.getTypeBinding()));
    boolean found = false;
    for (VariableDeclarationFragment var : TreeUtil.getAllFields(r2)) {
      if (var.getName().getIdentifier().equals("val$i")) {
        found = true;
      }
    }
    assertTrue("required field not found", found);

    // Verify constructor takes both outer field and var.
    String translation = generateFromUnit(unit, "Test.m");
    assertTranslation(translation, "r2 = [new_Test_$1_$1_initWithJavaLangInteger_(i) autorelease]");
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
    NameTable nameTable = unit.getNameTable();
    List<AbstractTypeDeclaration> types = unit.getTypes();
    AbstractTypeDeclaration r1 = types.get(1);
    assertEquals("Test_$1", nameTable.getFullName(r1.getTypeBinding()));
    boolean found = false;
    for (VariableDeclarationFragment var : TreeUtil.getAllFields(r1)) {
      if (var.getName().getIdentifier().equals("val$i")) {
        found = true;
      }
    }
    assertTrue("required field not found", found);

    // Verify method var is passed to constructor.
    String translation = generateFromUnit(unit, "Test.m");
    assertTranslation(translation, "r = [new_Test_$1_initWithJavaLangInteger_(i) autorelease]");
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
    NameTable nameTable = unit.getNameTable();
    List<AbstractTypeDeclaration> types = unit.getTypes();
    AbstractTypeDeclaration r1 = types.get(2);
    assertEquals("Test_$1", nameTable.getFullName(r1.getTypeBinding()));
    boolean found = false;
    for (VariableDeclarationFragment var : TreeUtil.getAllFields(r1)) {
      if (var.getName().getIdentifier().equals("val$i")) {
        found = true;
      }
    }
    assertTrue("required field not found", found);

    // Verify method var is passed to constructor.
    String translation = generateFromUnit(unit, "Test.m");
    assertTranslation(translation, "r = [new_Test_$1_initWithJavaLangInteger_(i) autorelease]");
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

    assertTranslation(impl, "@interface Test_$1Enum : TestEnum");
    assertTranslation(impl, "@interface Test_$2Enum : TestEnum");
    assertTranslatedLines(impl,
        "- (instancetype)initWithNSString:(NSString *)__name",
        "withInt:(jint)__ordinal {");

    assertTranslation(impl, "TestEnum_initWithNSString_withInt_(self, __name, __ordinal);");
    assertTranslation(impl, "TestEnum_UP = new_Test_$1Enum_initWithNSString_withInt_(@\"UP\", 0);");
    assertTranslation(impl,
        "TestEnum_DOWN = new_Test_$2Enum_initWithNSString_withInt_(@\"DOWN\", 1);");
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
        "new_Test_A_$1_initWithTest_A_withTest_B_withTest_B_withInt_(self, b, b, 1)");
    assertTranslatedLines(translation,
        "void Test_A_$1_initWithTest_A_withTest_B_withTest_B_withInt_("
          + "Test_A_$1 *self, Test_A *outer$, Test_B *capture$0, Test_B *superOuter$, "
          + "jint arg$0) {",
        "  JreStrongAssign(&self->this$1_, outer$);",
        "  JreStrongAssign(&self->val$b_, capture$0);",
        "  Test_B_Inner_initWithTest_B_withInt_(self, superOuter$, arg$0);",
        "}");
  }

  public void testAnonymousClassInStaticBlock() throws IOException {
    String translation = translateSourceFile("class Test { "
        + "  static class A {"
        + "    static abstract class Inner { Inner(int i) { } abstract int foo(); } } "
        + "  static A.Inner inner = new A.Inner(1) { int foo() { return 2; } }; }",
        "Test", "Test.m");
    // This is probably not the right output - but it compiles and works.
    assertTranslation(translation, "new_Test_$1_initWithInt_(1)");
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

    // Verify ColorEnum constructor.
    assertTranslatedLines(impl,
        "void ColorEnum_initWithInt_withNSString_withInt_("
          + "ColorEnum *self, jint n, NSString *__name, jint __ordinal) {",
        "  JavaLangEnum_initWithNSString_withInt_(self, __name, __ordinal);",
        "}");

    // Verify ColorEnum_$1 constructor.
    assertTranslatedLines(impl,
        "void Color_$1Enum_initWithInt_withNSString_withInt_("
          + "Color_$1Enum *self, jint arg$0, NSString *__name, jint __ordinal) {",
        "  ColorEnum_initWithInt_withNSString_withInt_(self, arg$0, __name, __ordinal);",
        "}");

    // Verify constant initialization.
    assertTranslation(impl, "new_Color_$1Enum_initWithInt_withNSString_withInt_(42, @\"RED\", 0)");
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
    assertTranslation(impl, "new_OuterValueEnum_initWithNSString_withInt_(@\"VALUE1\", 0)");

    // Verify InnerValue constant initialization.
    assertTranslation(impl,
        "new_OuterValue_InnerValueEnum_initWithNSString_withInt_(@\"VALUE1\", 0)");
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
    assertOccurrences(impl, "@implementation A_$1", 1);
    assertOccurrences(impl, "JreStrongAssignAndConsume(&self->my_i_, new_A_$1_init());", 2);
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
    assertTranslation(translation, "new_Test_$1_initWithNSString_(@\"foo\")");
    assertTranslation(translation, "- (instancetype)initWithNSString:(NSString *)arg$0 {");
    assertTranslatedLines(translation,
        "void Test_$1_initWithNSString_(Test_$1 *self, NSString *arg$0) {",
        "  Test_initWithId_(self, arg$0);",
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
        "[new_Test_$1_initWithNSString_withNSObjectArray_(@\"%s %s\", "
        + "[IOSObjectArray arrayWithObjects:(id[]){ @\"1\", @\"2\" } count:2 "
        + "type:NSObject_class_()]) autorelease];");
    assertTranslation(translation,
        "[new_Test_$2_initWithNSString_withNSObjectArray_(@\"foo\", "
        + "[IOSObjectArray arrayWithLength:0 type:NSObject_class_()]) autorelease];");
    // check the generated constructors.
    assertTranslatedLines(translation,
        "void Test_$1_initWithNSString_withNSObjectArray_("
          + "Test_$1 *self, NSString *arg$0, IOSObjectArray *arg$1) {",
        "  Test_initWithNSString_withNSObjectArray_(self, arg$0, arg$1);",
        "}");
    assertTranslatedLines(translation,
        "void Test_$2_initWithNSString_withNSObjectArray_("
          + "Test_$2 *self, NSString *arg$0, IOSObjectArray *arg$1) {",
        "  Test_initWithNSString_withNSObjectArray_(self, arg$0, arg$1);",
        "}");
  }
}
