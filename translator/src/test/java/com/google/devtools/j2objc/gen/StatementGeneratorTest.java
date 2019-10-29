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

package com.google.devtools.j2objc.gen;

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.Options.MemoryManagementOption;
import com.google.devtools.j2objc.ast.Statement;
import java.io.IOException;
import java.util.List;

/**
 * Tests for {@link StatementGenerator}.
 *
 * @author Tom Ball
 */
public class StatementGeneratorTest extends GenerationTest {

  // Verify that return statements output correctly for reserved words.
  public void testReturnReservedWord() throws IOException {
    String translation = translateSourceFile(
        "public class Test { static final String BOOL = \"bool\"; String test() { return BOOL; }}",
        "Test", "Test.m");
    assertTranslation(translation, "return Test_BOOL;");
  }

  // Verify that both a class and interface type invoke getClass() correctly.
  public void testGetClass() throws IOException {
    String translation = translateSourceFile(
        "import java.util.*; public class A {"
        + "  void test(ArrayList one, List two) { "
        + "    Class<?> classOne = one.getClass();"
        + "    Class<?> classTwo = two.getClass(); }}",
        "A", "A.m");
    assertTranslation(translation, "[((JavaUtilArrayList *) nil_chk(one)) java_getClass]");
    assertTranslation(translation, "[((id<JavaUtilList>) nil_chk(two)) java_getClass]");
  }

  public void testEnumConstantReferences() throws IOException {
    String translation = translateSourceFile(
        "public class A { static enum B { ONE, TWO; "
        + "public static B doSomething(boolean b) { return b ? ONE : TWO; }}}",
        "A", "A.m");
    assertTranslation(translation, "return b ? JreEnum(A_B, ONE) : JreEnum(A_B, TWO);");
  }

  public void testInnerClassFQN() throws IOException {
    String translation = translateSourceFile(
        "package com.example.foo; "
        + "public class Foo { static class Inner { public static void doSomething() {} }}"
        + "class Bar { public static void mumber() { Foo.Inner.doSomething(); }}",
        "Foo", "com/example/foo/Foo.m");
    assertTranslation(translation, "ComExampleFooFoo_Inner_doSomething();");
  }

  public void testLocalVariableTranslation() throws IOException {
    String source = "Exception e;";
    List<Statement> stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("JavaLangException *e;", result);
  }

  public void testClassCreationTranslation() throws IOException {
    String source = "new Exception(\"test\");";
    List<Statement> stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("create_JavaLangException_initWithNSString_(@\"test\");", result);
  }

  public void testParameterTranslation() throws IOException {
    String source = "Throwable cause = new Throwable(); new Exception(cause);";
    List<Statement> stmts = translateStatements(source);
    assertEquals(2, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertEquals("create_JavaLangException_initWithJavaLangThrowable_(cause);", result);
  }

  public void testCastTranslation() throws IOException {
    String source = "Object o = new Object(); Throwable t = (Throwable) o; int[] i = (int[]) o;";
    List<Statement> stmts = translateStatements(source);
    assertEquals(3, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertEquals("JavaLangThrowable *t = "
        + "(JavaLangThrowable *) cast_chk(o, [JavaLangThrowable class]);", result);
    result = generateStatement(stmts.get(2));
    assertEquals("IOSIntArray *i = "
        + "(IOSIntArray *) cast_chk(o, [IOSIntArray class]);", result);
  }

  public void testInterfaceCastTranslation() throws IOException {
    String source = "java.util.Collection al = new java.util.ArrayList(); "
        + "java.util.List l = (java.util.List) al;";
    List<Statement> stmts = translateStatements(source);
    assertEquals(2, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertEquals("id<JavaUtilList> l = "
        + "(id<JavaUtilList>) cast_check(al, JavaUtilList_class_());", result);
  }

  public void testCatchTranslation() throws IOException {
    String source = "try { ; } catch (Exception e) {}";
    List<Statement> stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("@try {\n;\n}\n@catch (JavaLangException *e) {\n}", result);
  }

  public void testInstanceOfTranslation() throws IOException {
    String source = "Exception e = new Exception(); if (e instanceof Throwable) {}";
    List<Statement> stmts = translateStatements(source);
    assertEquals(2, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertEquals("if ([e isKindOfClass:[JavaLangThrowable class]]) {\n}", result);
  }

  public void testFullyQualifiedTypeTranslation() throws IOException {
    String source = "java.lang.Exception e = null;";
    List<Statement> stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("JavaLangException *e = nil;", result);
  }

  public void testToStringRenaming() throws IOException {
    String source = "Object o = new Object(); o.toString(); toString();";
    List<Statement> stmts = translateStatements(source);
    assertEquals(3, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertEquals("[o description];", result);
    result = generateStatement(stmts.get(2));
    assertEquals("[self description];", result);
  }

  public void testSuperToStringRenaming() throws IOException {
    String translation = translateSourceFile(
      "public class Example { public String toString() { return super.toString(); } }",
      "Example", "Example.m");
    assertTranslation(translation, "return [super description];");
  }

  public void testAccessPublicConstant() throws IOException {
    String translation = translateSourceFile(
      "public class Example { public static final int FOO=1; int foo; { foo = FOO; } }",
      "Example", "Example.m");
    assertTranslation(translation, "foo_ = Example_FOO;");
  }

  public void testAccessPublicConstant2() throws IOException {
    String translation = translateSourceFile(
      "public class Example { public static final int FOO=1;"
      + "int test() { int foo = FOO; return foo;} }",
      "Example", "Example.m");
    assertTranslation(translation, "foo = Example_FOO;");
  }

  public void testAccessPrivateConstant() throws IOException {
    String translation = translateSourceFile(
      "public class Example { private static final int FOO=1;"
      + "int test() { int foo = FOO; return foo;} }",
      "Example", "Example.m");
    assertTranslation(translation, "foo = Example_FOO;");
  }

  public void testAccessExternalConstant() throws IOException {
    String translation = translateSourceFile(
      "public class Example { static class Bar { public static final int FOO=1; } "
      + "int foo; { foo = Bar.FOO; } }",
      "Example", "Example.m");
    assertTranslation(translation, "foo_ = Example_Bar_FOO;");
    assertFalse(translation.contains("int Example_Bar_FOO_ = 1;"));
    translation = getTranslatedFile("Example.h");
    assertTranslation(translation, "#define Example_Bar_FOO 1");
  }

  public void testAccessExternalStringConstant() throws IOException {
    String translation = translateSourceFile(
        "public class Example { static class Bar { public static final String FOO=\"Mumble\"; } "
        + "String foo; { foo = Bar.FOO; } }",
        "Example", "Example.m");
    assertTranslation(translation,
        "JreStrongAssign(&self->foo_, Example_Bar_FOO)");
    assertTranslation(translation, "NSString *Example_Bar_FOO = @\"Mumble\";");
    translation = getTranslatedFile("Example.h");
    assertTranslation(translation, "FOUNDATION_EXPORT NSString *Example_Bar_FOO;");
    assertTranslation(translation, "J2OBJC_STATIC_FIELD_OBJ_FINAL(Example_Bar, FOO, NSString *)");
  }

  public void testObjectDeclaration() throws IOException {
    List<Statement> stmts = translateStatements("Object o;");
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("id o;", result);
  }

  public void testStaticBooleanFields() throws IOException {
    String translation = translateSourceFile(
        "public class Example { Boolean b1 = Boolean.TRUE; Boolean b2 = Boolean.FALSE; }",
        "Example", "Example.m");
    assertTranslation(translation,
        "JreStrongAssign(&self->b1_, JreLoadStatic(JavaLangBoolean, TRUE))");
    assertTranslation(translation,
        "JreStrongAssign(&self->b2_, JreLoadStatic(JavaLangBoolean, FALSE))");
  }

  public void testStringConcatenation() throws IOException {
    String translation = translateSourceFile(
        "public class Example<K,V> { void test() { String s = \"hello, \" + \"world\"; }}",
        "Example", "Example.m");
    assertTranslation(translation, "NSString *s = @\"hello, world\"");
  }

  public void testStringConcatenation2() throws IOException {
    String source = "class A { "
        + "private static final String A = \"bob\"; "
        + "private static final char SPACE = ' '; "
        + "private static final double ANSWER = 22.0 / 2; "
        + "private static final boolean B = false; "
        + "private static final String C = "
        + "\"hello \" + A + ' ' + 3 + SPACE + true + ' ' + ANSWER + ' ' + B; }";
    String translation = translateSourceFile(source, "A", "A.m");
    assertTranslation(translation, "\"hello bob 3 true 11.0 false\"");
  }

  public void testStringConcatenationTypes() throws IOException {
    String translation = translateSourceFile(
        "public class Example<K,V> { Object obj; boolean b; char c; double d; float f; int i; "
        + "long l; short s; String str; public String toString() { "
        + "return \"obj=\" + obj + \" b=\" + b + \" c=\" + c + \" d=\" + d + \" f=\" + f"
        + " + \" i=\" + i + \" l=\" + l + \" s=\" + s; }}",
        "Example", "Example.m");
    assertTranslation(translation,
        "return JreStrcat(\"$@$Z$C$D$F$I$J$S\", @\"obj=\", obj_, @\" b=\", b_, @\" c=\", c_,"
          + " @\" d=\", d_, @\" f=\", f_, @\" i=\", i_, @\" l=\", l_, @\" s=\", s_);");
  }

  public void testStringConcatenationWithLiterals() throws IOException {
    String translation = translateSourceFile(
        "public class Example<K,V> { public String toString() { "
        + "return \"literals: \" + true + \", \" + 'c' + \", \" + 1.0d + \", \" + 3.14 + \", \""
        + " + 42 + \", \" + 123L + \", \" + 1; }}",
        "Example", "Example.m");
    assertTranslation(translation, "return @\"literals: true, c, 1.0, 3.14, 42, 123, 1\";");
  }

  public void testStringConcatenationEscaping() throws IOException {
    String translation = translateSourceFile(
        "public class Example<K,V> { String s = \"hello, \" + 50 + \"% of the world\\n\"; }",
        "Example", "Example.m");
    assertTranslation(translation,
        "JreStrongAssign(&self->s_, @\"hello, 50% of the world\\n\");");
  }

  public void testStringConcatenationMethodInvocation() throws IOException {
    String translation = translateSourceFile(
        "public class Test { "
        + "  String getStr() { return \"str\"; } "
        + "  int getInt() { return 42; } "
        + "  void test() { "
        + "    String a = \"foo\" + getStr() + \"bar\" + getInt() + \"baz\"; } }",
        "Test", "Test.m");
    assertTranslation(translation,
        "JreStrcat(\"$$$I$\", @\"foo\", [self getStr], @\"bar\", [self getInt], @\"baz\")");
  }

  public void testVarargsMethodInvocation() throws IOException {
    String translation = translateSourceFile("public class Example { "
        + "public void call() { foo(null); bar(\"\", null, null); }"
        + "void foo(Object ... args) { }"
        + "void bar(String firstArg, Object ... varArgs) { }}",
        "Example", "Example.m");
    assertTranslation(translation, "[self fooWithNSObjectArray:");
    assertTranslation(translation,
        "[IOSObjectArray arrayWithObjects:(id[]){ nil, nil } count:2 type:NSObject_class_()]");
    assertTranslation(translation, "[self barWithNSString:");
    assertTranslation(translation, "withNSObjectArray:");
  }

  public void testVarargsMethodInvocationSingleArg() throws IOException {
    String translation = translateSourceFile("public class Example { "
        + "public void call() { foo(1); }"
        + "void foo(Object ... args) { }}",
        "Example", "Example.m");
    assertTranslation(translation,
        "[self fooWithNSObjectArray:"
        + "[IOSObjectArray arrayWithObjects:(id[]){ JavaLangInteger_valueOfWithInt_(1) } count:1 "
        + "type:NSObject_class_()]];");
  }

  public void testVarargsMethodInvocationPrimitiveArgs() throws IOException {
    String translation = translateSourceFile(
        "class Test { void call() { foo(1); } void foo(int... i) {} }", "Test", "Test.m");
    assertTranslation(translation,
        "[self fooWithIntArray:[IOSIntArray arrayWithInts:(jint[]){ 1 } count:1]];");
  }

  public void testStaticInnerSubclassAccessingOuterStaticVar() throws IOException {
    String translation = translateSourceFile(
        "public class Test { public static final Object FOO = new Object(); "
        + "static class Inner { Object test() { return FOO; }}}",
        "Test", "Test.m");
    assertTranslation(translation, "return JreLoadStatic(Test, FOO);");
  }

  public void testReservedIdentifierReference() throws IOException {
    String translation = translateSourceFile(
        "public class Test { public int test(int id) { return id; }}",
        "Test", "Test.m");
    assertTranslation(translation, "- (jint)testWithInt:(jint)id_");
    assertTranslation(translation, "return id_;");
  }

  public void testReservedTypeQualifierReference() throws IOException {
    String translation = translateSourceFile(
        "public class Test { public int test(int in, int out) { return in + out; }}",
        "Test", "Test.m");
    assertTranslation(translation, "- (jint)testWithInt:(jint)inArg");
    assertTranslation(translation, "return inArg + outArg;");
  }

  public void testFieldAccess() throws IOException {
    String translation = translateSourceFile(
        "import com.google.j2objc.annotations.Weak;"
        + "public class Test { "
        + "  Object i;"
        + "  @Weak Object j;"
        + "  Test(Object otherI, Object otherJ) {"
        + "    i = otherI;"
        + "    j = otherJ;"
        + "  }"
        + "}",
        "Test", "Test.m");
    assertTranslation(translation, "JreStrongAssign(&self->i_, otherI);");
    assertTranslation(translation, "j_ = otherJ;");
    assertTranslation(translation, "RELEASE_(i_);");
  }

  public void testStaticFinalFieldAccessWithParenthesizedExpression() throws IOException {
    String translation = translateSourceFile(
        "public class Test { "
            + "  private static final int t = 7; "
            + "  static int test() { "
            + "    Object o = new Test(); "
            + "    return ((Test)o).t; "
            + "  } "
            + "}",
        "Test", "Test.m");
    assertTranslation(translation, "return Test_t;");
  }

  public void testInnerInnerClassFieldAccess() throws IOException {
    String translation = translateSourceFile(
        "public class Test { static class One {} static class Two extends Test { "
        + "Integer i; Two(Integer i) { this.i = i; } int getI() { return i.intValue(); }}}",
        "Test", "Test.m");
    assertTranslation(translation,
        "- (instancetype)initWithJavaLangInteger:(JavaLangInteger *)i {");
    assertTranslation(translation, "return [((JavaLangInteger *) nil_chk(i_)) intValue];");
  }

  public void testInnerClassSuperConstructor() throws IOException {
    String translation = translateSourceFile(
        "public class Test { static class One { int i; One(int i) { this.i = i; }} "
        + "static class Two extends One { Two(int i) { super(i); }}}",
        "Test", "Test.m");
    assertTranslation(translation, "- (instancetype)initWithInt:(jint)i");
    assertTranslatedLines(translation,
        "void Test_Two_initWithInt_(Test_Two *self, jint i) {",
        "  Test_One_initWithInt_(self, i);",
        "}");
  }

  public void testStaticInnerClassSuperFieldAccess() throws IOException {
    String translation = translateSourceFile(
        "public class Test { protected int foo; "
        + "static class One extends Test { int i; One() { i = foo; } int test() { return i; }}}",
        "Test", "Test.m");
    assertTranslation(translation, "- (instancetype)init {");
    assertTranslation(translation, "self->i_ = self->foo_;");
    assertTranslation(translation, "return i_;");
  }

  public void testMethodInvocationOfReturnedInterface() throws IOException {
    String translation = translateSourceFile(
        "import java.util.*; public class Test <K,V> { "
        + "Iterator<Map.Entry<K,V>> iterator; "
        + "K test() { return iterator.next().getKey(); }}",
        "Test", "Test.m");
    assertTranslation(translation, "return [((id<JavaUtilMap_Entry>) "
        + "nil_chk([((id<JavaUtilIterator>) nil_chk(iterator_)) next])) getKey];");
  }

  public void testAnonymousClassInInnerStatic() throws IOException {
    String translation = translateSourceFile(
        "import java.util.*; public class Test { "
        + "static <T> Enumeration<T> enumeration(Collection<T> collection) {"
        + "final Collection<T> c = collection; "
        + "return new Enumeration<T>() { "
        + "Iterator<T> it = c.iterator(); "
        + "public boolean hasMoreElements() { return it.hasNext(); } "
        + "public T nextElement() { return it.next(); } }; }}",
        "Test", "Test.m");
    assertTranslation(translation, "return [((id<JavaUtilIterator>) nil_chk(it_)) hasNext];");
    assertTranslation(translation, "return [((id<JavaUtilIterator>) nil_chk(it_)) next];");
    assertFalse(translation.contains("Test *this$0;"));
  }

  public void testGenericMethodWithAnonymousReturn() throws IOException {
    String translation = translateSourceFile(
        "import java.util.*; public class Test { "
        + "public static <T> Enumeration<T> enumeration(final Collection<T> collection) {"
        + "return new Enumeration<T>() {"
        + "  Iterator<T> it = collection.iterator();"
        + "  public boolean hasMoreElements() { return it.hasNext(); }"
        + "  public T nextElement() { return it.next(); }}; }}",
        "Test", "Test.m");
    assertTranslation(translation,
        "return create_Test_1_initWithJavaUtilCollection_(collection);");
    assertTranslation(translation,
        "- (instancetype)initWithJavaUtilCollection:(id<JavaUtilCollection>)capture$0;");
    assertTranslation(translation,
        "__attribute__((unused)) static Test_1 *new_Test_1_initWithJavaUtilCollection_("
        + "id<JavaUtilCollection> capture$0) NS_RETURNS_RETAINED;");
  }

  public void testEnumInEqualsTest() throws IOException {
    String translation = translateSourceFile(
        "public class Test { enum TicTacToe { X, Y } "
        + "boolean isX(TicTacToe ttt) { return ttt == TicTacToe.X; } }",
        "Test", "Test.m");
    assertTranslation(translation, "return ttt == JreLoadEnum(Test_TicTacToe, X);");
  }

  public void testArrayLocalVariable() throws IOException {
    String source = "char[] array = new char[1];";
    List<Statement> stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSCharArray *array = [IOSCharArray arrayWithLength:1];", result);

    source = "char array[] = new char[1];";
    stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    result = generateStatement(stmts.get(0));
    assertEquals("IOSCharArray *array = [IOSCharArray arrayWithLength:1];", result);
  }

  public void testArrayParameterLengthUse() throws IOException {
    String translation = translateSourceFile(
        "public class Test { void test(char[] foo, char bar[]) { "
        + "sync(foo.length, bar.length); } void sync(int a, int b) {} }",
        "Test", "Test.m");
    assertTranslation(translation,
        "[self syncWithInt:((IOSCharArray *) nil_chk(foo))->size_ withInt:"
        + "((IOSCharArray *) nil_chk(bar))->size_];");
  }

  public void testLongLiteral() throws IOException {
    String translation = translateSourceFile("public class Test { "
        + "public static void testLong() { long l1 = 1L; }}", "Test", "Test.m");
    assertTranslation(translation, "jlong l1 = 1LL");
  }

  public void testStringLiteralEscaping() throws IOException {
    String source = "String s = \"\\u1234\\u2345\\n\";";
    List<Statement> stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("NSString *s = @\"\\u1234\\u2345\\n\";", result);
  }

  public void testStringLiteralEscapingInStringConcatenation() throws IOException {
    String source = "String s = \"\\u1234\" + \"Hi\" + \"\\u2345\\n\";";
    List<Statement> stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("NSString *s = @\"\\u1234Hi\\u2345\\n\";", result);
  }

  /**
   * Verify that Unicode escape sequences that aren't legal C++ Unicode are
   * converted to C hexadecimal escape sequences.  This works because all
   * illegal sequences are less than 0xA0.
   */
  public void testStringLiteralWithInvalidCppUnicode() throws IOException {
    String source = "String s = \"\\u0093\\u0048\\u0069\\u0094\\n\";";
    List<Statement> stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("NSString *s = @\"\\xc2\\x93Hi\\xc2\\x94\\n\";", result);
  }

  public void testArrayInitializer() {
    String source = "int[] a = { 1, 2, 3 }; char b[] = { '4', '5' };";
    List<Statement> stmts = translateStatements(source);
    assertEquals(2, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSIntArray *a = [IOSIntArray arrayWithInts:(jint[]){ 1, 2, 3 } count:3];",
        result);
    result = generateStatement(stmts.get(1));
    assertEquals("IOSCharArray *b = "
        + "[IOSCharArray arrayWithChars:(jchar[]){ '4', '5' } count:2];", result);
  }

  /**
   * Verify that static array initializers are rewritten as method calls.
   */
  public void testStaticArrayInitializer() throws IOException {
    String translation = translateSourceFile(
        "public class Test { static int[] a = { 1, 2, 3 }; static char b[] = { '4', '5' }; }",
        "Test", "Test.m");
    assertTranslation(translation,
        "JreStrongAssignAndConsume(&Test_a, "
        + "[IOSIntArray newArrayWithInts:(jint[]){ 1, 2, 3 } count:3]);");
    assertTranslation(translation,
        "JreStrongAssignAndConsume(&Test_b, "
        + "[IOSCharArray newArrayWithChars:(jchar[]){ '4', '5' } count:2]);");
  }

  public void testLocalArrayCreation() throws IOException {
    String translation = translateSourceFile(
        "public class Example { char[] test() { int high = 0xD800, low = 0xDC00; "
        + "return new char[] { (char) high, (char) low }; } }",
        "Example", "Example.m");
    assertTranslation(translation, "return [IOSCharArray "
        + "arrayWithChars:(jchar[]){ (jchar) high, (jchar) low } count:2];");
  }

  // Regression test: "case:" was output instead of "case".
  public void testSwitchCaseStatement() throws IOException {
    String source = "int c = 1; "
        + "switch (c) { case 1: c = 0; break; default: break; }";
    List<Statement> stmts = translateStatements(source);
    assertEquals(2, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertTrue(result.contains("case 1:"));
  }

  public void testEnhancedForStatement() throws IOException {
    String source = "String[] strings = {\"test1\", \"test2\"};"
        + "for (String string : strings) { }";
    List<Statement> stmts = translateStatements(source);
    assertEquals(2, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertTranslatedLines(result,
        "{",
          "IOSObjectArray *a__ = strings;",
          "NSString * const *b__ = a__->buffer_;",
          "NSString * const *e__ = b__ + a__->size_;",
          "while (b__ < e__) {",
            "NSString *string = *b__++;",
          "}",
        "}");
  }

  public void testEnhancedForStatementInSwitchStatement() throws IOException {
    String source = "int test = 5; int[] myInts = new int[10]; "
        + "switch (test) { case 0: break; default: "
        + "for (int i : myInts) {} break; }";
    List<Statement> stmts = translateStatements(source);
    assertEquals(3, stmts.size());
    String result = generateStatement(stmts.get(2));
    assertTranslatedLines(result,
        "switch (test) {",
          "case 0:",
            "break;",
          "default:",
            "{",
              "IOSIntArray *a__ = myInts;",
              "jint const *b__ = a__->buffer_;",
              "jint const *e__ = b__ + a__->size_;",
              "while (b__ < e__) {",
                "jint i = *b__++;",
              "}",
            "}",
            "break;",
        "}");
  }

  public void testSwitchStatementWithExpression() throws IOException {
    String translation = translateSourceFile("public class Example { "
        + "static enum Test { ONE, TWO } "
        + "Test foo() { return Test.ONE; } "
        + "void bar() { switch (foo()) { case ONE: break; case TWO: break; }}}",
        "Example", "Example.m");
    assertTranslation(translation, "switch ([[self foo] ordinal])");
  }

  public void testClassVariable() throws IOException {
    String source = "Class<?> myClass = getClass();"
        + "Class<?> mySuperClass = myClass.getSuperclass();"
        + "Class<?> enumClass = Enum.class;";
    List<Statement> stmts = translateStatements(source);
    assertEquals(3, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSClass *myClass = [self java_getClass];", result);
    result = generateStatement(stmts.get(1));
    assertEquals("IOSClass *mySuperClass = [myClass getSuperclass];", result);
    result = generateStatement(stmts.get(2));
    assertEquals("IOSClass *enumClass = JavaLangEnum_class_();", result);
  }

  public void testInnerClassCreation() throws IOException {
    String translation = translateSourceFile(
        "public class A { int x; class Inner { int y; Inner(int i) { y = i + x; }}"
        + "public Inner test() { return this.new Inner(3); }}",
        "A", "A.m");
    assertTranslation(translation, "return create_A_Inner_initWithA_withInt_(self, 3);");
  }

  public void testNewFieldNotRetained() throws IOException {
    String translation = translateSourceFile(
        "import java.util.*; public class A { Map map; A() { map = new HashMap(); }}",
        "A", "A.m");
    assertTranslation(translation,
        "JreStrongAssignAndConsume(&self->map_, new_JavaUtilHashMap_init())");
  }

  public void testStringAddOperator() throws IOException {
    String translation = translateSourceFile(
        "import java.util.*; public class A { String myString;"
        + "  A() { myString = \"Foo\"; myString += \"Bar\"; }}",
        "A", "A.m");
    assertTranslation(translation, "JreStrAppendStrong(&self->myString_, \"$\", @\"Bar\");");
  }

  public void testInterfaceStaticVarReference() throws IOException {
    String translation = translateSourceFile(
        "public class Test { "
        + "  public interface I { "
        + "    void foo(); public static final int FOO = 1; } "
        + "  public class Bar implements I { public void foo() { int i = I.FOO; } } }",
        "Test", "Test.m");
    assertTranslation(translation, "int i = Test_I_FOO;");
  }

  public void testMethodWithPrimitiveArrayParameter() throws IOException {
    String translation = translateSourceFile(
        "public class Test { "
        + "  public void foo(char[] chars) { } }",
        "Test", "Test.m");
    assertTranslation(translation, "fooWithCharArray:");
  }

  public void testMethodWithGenericArrayParameter() throws IOException {
    String translation = translateSourceFile(
        "public class Test<T> { "
        + "  T[] tArray; "
        + "  public void foo(T[] ts) { } "
        + "  public void bar(Test<? extends T>[] tLists) { } "
        + "  public void foo() { foo(tArray); } "
        + "  public class Inner<S extends Test<T>> { "
        + "    public void baz(S[] ss) { } } }",
        "Test", "Test.m");
    assertTranslation(translation, "- (void)fooWithNSObjectArray:");
    assertTranslation(translation, "- (void)barWithTestArray:");
    assertTranslation(translation, "- (void)foo {");
    assertTranslation(translation, "[self fooWithNSObjectArray:");
    assertTranslation(translation, "- (void)bazWithTestArray:");
  }

  public void testGenericMethodWithfGenericArrayParameter() throws IOException {
    String translation = translateSourceFile(
        "public class Test { "
        + "  public <T> void foo(T[] ts) { } "
        + "  public void foo() { foo(new String[1]); } }",
        "Test", "Test.m");
    assertTranslation(translation, "[self fooWithNSObjectArray:");
  }

  public void testJreDoubleNegativeInfinity() throws IOException {
    String translation = translateSourceFile(
        "public class Test { "
        + "  public void foo() { Double d = Double.NEGATIVE_INFINITY; } }",
        "Test", "Test.m");
    assertTranslation(translation,
        "JavaLangDouble_valueOfWithDouble_(JavaLangDouble_NEGATIVE_INFINITY)");
  }

  public void testInvokeMethodInConcreteImplOfGenericInterface() throws IOException {
    String translation = translateSourceFile(
        "public class Test { "
        + "  public interface Foo<T> { void foo(T t); } "
        + "  public class FooImpl implements Foo<Test> { "
        + "    public void foo(Test t) { } "
        + "    public void bar() { foo(new Test()); } } }",
        "Test", "Test.m");
    assertTranslation(translation, "[self fooWithId:");
  }

  public void testNewStringWithArrayInAnonymousClass() throws IOException {
    String translation = translateSourceFile(
        "public class Test { "
        + "  public Runnable foo() { "
        + "    String s1 = new String(new char[10]); "
        + "    return new Runnable() { "
        + "      public void run() { String s = new String(new char[10]); } }; } }",
        "Test", "Test.m");
    assertTranslation(translation, "s = [NSString java_stringWith");
  }

  public void testMostNegativeIntegers() throws IOException {
    String translation = translateSourceFile(
        "public class Test { "
        + "  int min_int = 0x80000000; "
        + "  long min_long = 0x8000000000000000L; }",
        "Test", "Test.m");
    assertTranslation(translation, "-0x7fffffff - 1");
    assertTranslation(translation, "-0x7fffffffffffffffLL - 1");
  }

  public void testInnerNewStatement() throws IOException {
    String translation = translateSourceFile(
        "class A { class B {} static B test() { return new A().new B(); }}",
        "A", "A.m");
    assertTranslation(translation, "create_A_B_initWithA_(create_A_init())");
  }

  public void testSuperFieldAccess() throws IOException {
    String translation = translateSourceFile(
        "public class A { int i; class B extends A { int i; int test() { return super.i + i; }}}",
        "A", "A.m");
    assertTranslation(translation, "return i_ + i_B_;");
  }

  public void testStaticConstants() throws IOException {
    String source = "float f = Float.NaN; double d = Double.POSITIVE_INFINITY;";
    List<Statement> stmts = translateStatements(source);
    assertEquals(2, stmts.size());
    String result = generateStatement(stmts.get(0)).trim();
    assertEquals("jfloat f = JavaLangFloat_NaN;", result);
    result = generateStatement(stmts.get(1)).trim();
    assertEquals("jdouble d = JavaLangDouble_POSITIVE_INFINITY;", result);
  }

  public void testInstanceStaticConstants() throws IOException {
    String translation = translateSourceFile(
        "public class Test { Foo f; void test() { int i = f.DEFAULT; Object lock = f.LOCK; }} "
        + "class Foo { public static final int DEFAULT = 1; "
        + "public static final Object LOCK = null; }", "Test", "Test.m");
    assertTranslation(translation, "int i = Foo_DEFAULT;");
    assertTranslation(translation, "id lock = JreLoadStatic(Foo, LOCK);");
  }

  public void testCastGenericReturnType() throws IOException {
    String translation = translateSourceFile(
      "class Test { "
      + "  static class A<E extends A> { E other; public E getOther() { return other; } } "
      + "  static class B extends A<B> { B other = getOther(); } }",
      "Test", "Test.h");
    // Test_B's "other" needs a trailing underscore, since there is an "other"
    // field in its superclass.
    assertTranslation(translation, "Test_B *other_B_;");
    translation = getTranslatedFile("Test.m");
    assertTranslation(translation, "JreStrongAssign(&self->other_B_, [self getOther])");
  }

  public void testArrayInstanceOfTranslation() throws IOException {
    String source = "Object args = new String[0]; if (args instanceof String[]) {}";
    List<Statement> stmts = translateStatements(source);
    assertEquals(2, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertEquals(
        "if ([IOSClass_arrayType(NSString_class_(), 1) isInstance:args]) {\n}", result);
  }

  public void testInterfaceArrayInstanceOfTranslation() throws IOException {
    String source = "Object args = new Readable[0]; if (args instanceof Readable[]) {}";
    List<Statement> stmts = translateStatements(source);
    assertEquals(2, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertEquals(
        "if ([IOSClass_arrayType(JavaLangReadable_class_(), 1) isInstance:args]) {\n}",
        result);
  }

  public void testPrimitiveArrayInstanceOfTranslation() throws IOException {
    String source = "Object args = new int[0]; if (args instanceof int[]) {}";
    List<Statement> stmts = translateStatements(source);
    assertEquals(2, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertEquals("if ([args isKindOfClass:[IOSIntArray class]]) {\n}", result);
  }

  public void testObjectArrayInitializer() throws IOException {
    String source = "String[] a = { \"one\", \"two\", \"three\" };";
    List<Statement> stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSObjectArray *a = [IOSObjectArray "
        + "arrayWithObjects:(id[]){ @\"one\", @\"two\", @\"three\" } "
        + "count:3 type:NSString_class_()];", result);

    source = "Comparable[] a = { \"one\", \"two\", \"three\" };";
    stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    result = generateStatement(stmts.get(0));
    assertEquals("IOSObjectArray *a = [IOSObjectArray "
        + "arrayWithObjects:(id[]){ @\"one\", @\"two\", @\"three\" } "
        + "count:3 type:JavaLangComparable_class_()];", result);
  }

  public void testArrayPlusAssign() throws IOException {
    String source = "int[] array = new int[] { 1, 2, 3 }; int offset = 1; array[offset] += 23;";
    List<Statement> stmts = translateStatements(source);
    assertEquals(3, stmts.size());
    String result = generateStatement(stmts.get(2));
    assertEquals("*IOSIntArray_GetRef(array, offset) += 23;", result);
  }

  public void testRegisterVariableName() throws IOException {
    String source = "int register = 42;";
    List<Statement> stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("jint register_ = 42;", result);
  }

  public void testStaticVariableSetterReference() throws IOException {
    String translation = translateSourceFile(
        "public class Example { public static java.util.Date today; }"
        + "class Test { void test(java.util.Date now) { Example.today = now; }}",
        "Example", "Example.m");
    assertTranslation(translation, "JreStrongAssign(JreLoadStaticRef(Example, today), now);");
  }

  // b/5872533: reserved method name not renamed correctly in super invocation.
  public void testSuperReservedName() throws IOException {
    addSourceFile("public class A { A() {} public void init(int a) { }}", "A.java");
    addSourceFile(
        "public class B extends A { B() {} public void init(int b) { super.init(b); }}", "B.java");
    String translation = translateSourceFile("A", "A.h");
    assertTranslation(translation, "- (instancetype)init;");
    assertTranslation(translation, "- (void)init__WithInt:(jint)a");
    translation = translateSourceFile("B", "B.m");
    assertTranslation(translation, "A_init(self);");
    assertTranslation(translation, "[super init__WithInt:b];");
  }

  // b/5872757: verify multi-dimensional array has cast before each
  // secondary reference.
  public void testMultiDimArrayCast() throws IOException {
    String translation = translateSourceFile(
      "public class Test {"
      + "  static String[][] a = new String[1][1];"
      + "  public static void main(String[] args) { "
      + "    a[0][0] = \"42\"; System.out.println(a[0].length); }}",
      "Test", "Test.m");
    assertTranslation(translation,
        "IOSObjectArray_Set(nil_chk(IOSObjectArray_Get(nil_chk(Test_a), 0)), 0, @\"42\");");
    assertTranslation(translation,
        "((IOSObjectArray *) nil_chk(IOSObjectArray_Get(Test_a, 0)))->size_");
  }

  public void testMultiDimArray() throws IOException {
    String source = "int[][] a = new int[][] { null, { 0, 2 }, { 2, 2 }};";
    List<Statement> stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    String translation = generateStatement(stmts.get(0));
    assertTranslation(translation,
        "IOSObjectArray *a = [IOSObjectArray arrayWithObjects:(id[]){ nil, "
        + "[IOSIntArray arrayWithInts:(jint[]){ 0, 2 } count:2], "
        + "[IOSIntArray arrayWithInts:(jint[]){ 2, 2 } count:2] } count:3 "
        + "type:IOSClass_intArray(1)];");
  }

  public void testObjectMultiDimArray() throws IOException {
    String source = "class Test { Integer i = new Integer(1); Integer j = new Integer(2);"
        + "void test() { Integer[][] a = new Integer[][] { null, { i, j }, { j, i }}; }}";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation,
        "IOSObjectArray *a = [IOSObjectArray arrayWithObjects:(id[]){ nil, "
        + "[IOSObjectArray arrayWithObjects:(id[]){ i_, j_ } count:2 "
        + "type:JavaLangInteger_class_()], "
        + "[IOSObjectArray arrayWithObjects:(id[]){ j_, i_ } count:2 "
        + "type:JavaLangInteger_class_()] } count:3 "
        + "type:IOSClass_arrayType(JavaLangInteger_class_(), 1)];");
  }

  public void testVarargsMethodInvocationZeroLengthArray() throws IOException {
    String translation = translateSourceFile(
        "public class Example { "
        + " public void call() { foo(new Object[0]); bar(new Object[0]); } "
        + " public void foo(Object ... args) { } "
        + " public void bar(Object[] ... args) { } }",
        "Example", "Example.h");
    assertTranslation(translation, "- (void)fooWithNSObjectArray:(IOSObjectArray *)args");
    assertTranslation(translation, "- (void)barWithNSObjectArray2:(IOSObjectArray *)args");
    translation = getTranslatedFile("Example.m");

    // Should be equivalent to foo(new Object[0]).
    assertTranslation(translation,
        "[self fooWithNSObjectArray:[IOSObjectArray arrayWithLength:0 type:NSObject_class_()]]");

    // Should be equivalent to bar(new Object[] { new Object[0] }).
    assertTranslation(translation,
        "[self barWithNSObjectArray2:[IOSObjectArray arrayWithObjects:"
        + "(id[]){ [IOSObjectArray arrayWithLength:0 type:NSObject_class_()] } count:1 "
        + "type:IOSClass_arrayType(NSObject_class_(), 1)]];");
  }

  public void testVarargsIOSMethodInvocation() throws IOException {
    String translation = translateSourceFile(
        "import java.lang.reflect.Constructor; public class Test { "
        + " public void test() throws Exception { "
        + "  Constructor c1 = Test.class.getConstructor();"
        + "  Constructor c2 = Test.class.getConstructor(String.class);"
        + "  Constructor c3 = Test.class.getConstructor(String.class, Byte.TYPE);"
        + "  Class[] types = new Class[] { Object.class, Exception.class };"
        + "  Constructor c4 = Test.class.getConstructor(types); }}",
        "Test", "Test.m");
    assertTranslation(translation,
        "c1 = [Test_class_() getConstructor:"
        + "[IOSObjectArray arrayWithLength:0 type:IOSClass_class_()]];");
    assertTranslation(translation,
        "c2 = [Test_class_() getConstructor:[IOSObjectArray "
        + "arrayWithObjects:(id[]){ NSString_class_() } count:1 type:IOSClass_class_()]];");
    assertTranslation(translation,
        "c3 = [Test_class_() getConstructor:[IOSObjectArray arrayWithObjects:"
        + "(id[]){ NSString_class_(), JreLoadStatic(JavaLangByte, TYPE) } count:2 "
        + "type:IOSClass_class_()]];");

    // Array contents should be expanded.
    assertTranslation(translation, "c4 = [Test_class_() getConstructor:types];");
  }

  public void testGetVarargsWithLeadingParameter() throws IOException {
    String translation = translateSourceFile(
        "public class Test {"
        + "  void test() throws Exception { "
        + "    getClass().getMethod(\"equals\", Object.class); }}",
        "Test", "Test.m");
    assertTranslation(translation,
        "[[self java_getClass] getMethod:@\"equals\" parameterTypes:[IOSObjectArray "
        + "arrayWithObjects:(id[]){ NSObject_class_() } count:1 type:IOSClass_class_()]];");
  }

  public void testGetVarargsWithLeadingParameterNoArgs() throws IOException {
    String translation = translateSourceFile(
        "public class Test {"
        + "  void test() throws Exception { "
        + "    getClass().getMethod(\"hashCode\", new Class[0]); }}",
        "Test", "Test.m");
    assertTranslation(translation,
        "[[self java_getClass] getMethod:@\"hashCode\" parameterTypes:[IOSObjectArray "
        + "arrayWithLength:0 type:IOSClass_class_()]];");
  }

  public void testTypeVariableWithBoundCast() throws IOException {
    String translation = translateSourceFile(
        "import java.util.ArrayList; public class Test {"
        + "  public static class Foo<T extends Foo.Bar> {"
        + "    public static class Bar { } "
        + "    public T foo() { return null; } } "
        + "  public static class BarD extends Foo.Bar { } "
        + "  public void bar(Foo<BarD> f) { BarD b = f.foo(); } }",
        "Test", "Test.m");
    assertTranslation(translation, "[((Test_Foo *) nil_chk(f)) foo]");
  }

  // b/5934474: verify that static variables are always referenced by
  // their accessors in functions, since their class may not have loaded.
  public void testFunctionReferencesStaticVariable() throws IOException {
    String translation = translateSourceFile(
        "public class HelloWorld {"
        + "  static String staticString = \"hello world\";"
        + "  public static void main(String[] args) {"
        + "    System.out.println(staticString);"
        + "  }}",
        "HelloWorld", "HelloWorld.m");
    assertTranslation(translation, "printlnWithNSString:HelloWorld_staticString];");
  }

  public void testThisCallInEnumConstructor() throws IOException {
    String translation = translateSourceFile(
        "public enum Test {"
        + "  A, B(1);"
        + "  private int i;"
        + "  private Test(int i) { this.i = i; } "
        + "  private Test() { this(0); }}",
        "Test", "Test.m");
    assertTranslation(translation,
        "Test_initWithInt_withNSString_withInt_(self, 0, __name, __ordinal);");
  }

  public void testThisCallInInnerConstructor() throws IOException {
    String translation = translateSourceFile(
        "public class Test {"
        + "  class Inner {"
        + "    public Inner() { }"
        + "    public Inner(int foo) { this(); int i = foo; }}}",
        "Test", "Test.m");
    assertTranslation(translation, "Test_Inner_initWithTest_(self, outer$);");
  }

  // Verify that an external string can be used in string concatenation,
  // for a parameter to a translated method.
  public void testConcatPublicStaticString() throws IOException {
    String translation = translateSourceFile(
        "class B { public static final String separator = \"/\"; } "
        + "public class A { String prefix(Object o) { return new String(o + B.separator); }}",
        "A", "A.m");
    assertTranslation(translation,
        "[NSString stringWithString:JreStrcat(\"@$\", o, B_separator)]");
  }

  public void testStringConcatWithBoolean() throws IOException {
    String translation = translateSourceFile(
        "public class A { String test(boolean b) { return \"foo: \" + b; }}",
        "A", "A.m");
    assertTranslation(translation, "return JreStrcat(\"$Z\", @\"foo: \", b);");
  }

  public void testStringConcatWithChar() throws IOException {
    String translation = translateSourceFile(
        "public class A { String test(char c) { return \"foo: \" + c; }}",
        "A", "A.m");
    assertTranslation(translation, "return JreStrcat(\"$C\", @\"foo: \", c);");
  }

  // Verify that double quote character constants are concatenated correctly.
  public void testConcatDoubleQuoteChar() throws IOException {
    String translation = translateSourceFile(
        "public class Test { "
        + "static final char QUOTE = '\"'; static final String TEST = QUOTE + \"\"; }",
        "Test", "Test.m");
    assertTranslation(translation, "Test_TEST = @\"\\\"\";");
  }

  // Verify that return statements in constructors return self.
  public void testConstructorReturn() throws IOException {
    String translation = translateSourceFile(
        "public class A { public A() { return; }}", "A", "A.m");
    assertTranslation(translation, "return self;");
  }

  public void testNonAsciiOctalEscapeInString() throws IOException {
    String translation = translateSourceFile(
      "public class A { String s1 = \"\\177\"; String s2 = \"\\200\"; String s3 = \"\\377\"; }",
      "A", "A.m");
    assertTranslation(translation, "@\"\\x7f\"");
    assertTranslation(translation, "@\"\\xc2\\x80\"");
    assertTranslation(translation, "@\"\\u00ff\"");
  }

  public void testCharLiteralsAreEscaped() throws IOException {
    String translation = translateSourceFile(
        "public class A {"
        + "public static final char APOSTROPHE = '\\''; "
        + "public static final char BACKSLASH = '\\\\'; "
        + "void foo(char c) {} void test() { foo('\\''); foo('\\\\'); }}",
        "A", "A.h");
    assertTranslation(translation, "#define A_APOSTROPHE '\\''");
    assertTranslation(translation, "#define A_BACKSLASH '\\\\'");
    translation = getTranslatedFile("A.m");
    assertTranslation(translation, "fooWithChar:'\\'']");
    assertTranslation(translation, "fooWithChar:'\\\\']");
  }

  public void testStaticVarAccessFromInnerClass() throws IOException {
    String translation = translateSourceFile("public class Test { public static String foo; "
        + "interface Assigner { void assign(String s); } static { "
        + "new Assigner() { public void assign(String s) { foo = s; }}; }}",
        "Test", "Test.m");
    assertTranslation(translation, "JreStrongAssign(JreLoadStaticRef(Test, foo), s);");
  }

  public void testNoAutoreleasePoolForStatement() throws IOException {
    String translation = translateSourceFile(
        "public class Test {"
        + "  public void foo() {"
        + "    for (int i = 0; i < 10; i++) {"
        + "    }"
        + "  }"
        + "}",
        "Test", "Test.m");
    assertTranslation(translation, "  for (jint i = 0; i < 10; i++) {\n  }");
  }

  public void testAutoreleasePoolForStatement() throws IOException {
    String translation = translateSourceFile(
        "import com.google.j2objc.annotations.AutoreleasePool;"
        + "public class Test {"
        + "  public void foo() {"
        + "    for (@AutoreleasePool int i = 0; i < 10; i++) {"
        + "    }"
        + "  }"
        + "}",
        "Test", "Test.m");
    assertTranslation(translation, "  for (jint i = 0; i < 10; i++) {\n"
        + "    @autoreleasepool {\n    }\n"
        + "  }");
  }

  public void testAutoreleasePoolEnhancedForStatement() throws IOException {
    String translation = translateSourceFile(
        "import com.google.j2objc.annotations.AutoreleasePool;"
        + "public class Test {"
        + "  public void foo(String[] strings) {"
        + "    for (@AutoreleasePool String s : strings) {"
        + "    }"
        + "  }"
        + "}",
        "Test", "Test.m");
    assertTranslation(translation, "@autoreleasepool");
  }

  public void testARCAutoreleasePoolForStatement() throws IOException {
    options.setMemoryManagementOption(MemoryManagementOption.ARC);
    String translation = translateSourceFile(
        "import com.google.j2objc.annotations.AutoreleasePool;"
        + "public class Test {"
        + "  public void foo() {"
        + "    for (@AutoreleasePool int i = 0; i < 10; i++) {"
        + "    }"
        + "  }"
        + "}",
        "Test", "Test.m");
    assertTranslation(translation, "  for (jint i = 0; i < 10; i++) {\n"
        + "    @autoreleasepool {\n"
        + "    }\n"
        + "  }");
  }

  public void testARCAutoreleasePoolEnhancedForStatement() throws IOException {
    options.setMemoryManagementOption(MemoryManagementOption.ARC);
    String translation = translateSourceFile(
        "import com.google.j2objc.annotations.AutoreleasePool;"
        + "public class Test {"
        + "  public void foo(String[] strings) {"
        + "    for (@AutoreleasePool String s : strings) {"
        + "    }"
        + "  }"
        + "}",
        "Test", "Test.m");
    assertTranslation(translation, "@autoreleasepool {");
  }

  public void testShiftAssignArrayElement() throws IOException {
    String translation = translateSourceFile(
        "public class Test { void test(int[] array) { "
        + "int i = 2;"
        + "array[0] >>>= 2; "
        + "array[i - 1] >>>= 3; "
        + "array[1] >>= 4;"
        + "array[2] <<= 5;}}",
        "Test", "Test.m");
    assertTranslation(translation, "JreURShiftAssignInt(IOSIntArray_GetRef(nil_chk(array), 0), 2)");
    assertTranslation(translation, "JreURShiftAssignInt(IOSIntArray_GetRef(array, i - 1), 3)");
    assertTranslation(translation, "JreRShiftAssignInt(IOSIntArray_GetRef(array, 1), 4)");
    assertTranslation(translation, "JreLShiftAssignInt(IOSIntArray_GetRef(array, 2), 5)");
  }

  public void testAssertWithoutDescription() throws IOException {
    String translation = translateSourceFile(
        "public class Test { void test() {\n"
        + "int a = 5;\nint b = 6;\nassert a < b;\n}\n}\n",
        "Test", "Test.m");
    assertTranslation(translation,
        "JreAssert(a < b, @\"Test.java:4 condition failed: assert a < b;\")");
  }

  public void testAssertWithDescription() throws IOException {
    String translation = translateSourceFile(
        "public class Test { void test() { "
        + "int a = 5; int b = 6; assert a < b : \"a should be lower than b\";}}",
        "Test", "Test.m");
    assertTranslation(translation,
        "JreAssert(a < b, @\"a should be lower than b\")");
  }

  public void testAssertWithDynamicDescription() throws IOException {
    String translation = translateSourceFile(
        "public class Test { void test() { "
        + "int a = 5; int b = 6; assert a < b : a + \" should be lower than \" + b;}}",
        "Test", "Test.m");
    assertTranslation(translation,
        "JreAssert(a < b, JreStrcat(\"I$I\", a, @\" should be lower than \", b));");
  }

  // Verify that a Unicode escape sequence is preserved with string
  // concatenation.
  public void testUnicodeStringConcat() throws IOException {
    String translation = translateSourceFile(
        "class Test { static final String NAME = \"\\u4e2d\\u56fd\";"
        + " static final String CAPTION = \"China's name is \";"
        + " static final String TEST = CAPTION + NAME; }", "Test", "Test.m");
    assertTranslation(translation, "Test_TEST = @\"China's name is \\u4e2d\\u56fd\"");
  }

  public void testPartialArrayCreation2D() throws IOException {
    String translation = translateSourceFile(
        "class Test { void foo() { char[][] c = new char[3][]; } }", "Test", "Test.m");
    assertTranslation(translation, "#include \"IOSObjectArray.h\"");
    assertTranslation(translation, "#include \"IOSClass.h\"");
    assertTranslation(translation,
        "IOSObjectArray *c = [IOSObjectArray arrayWithLength:3 type:IOSClass_charArray(1)]");
  }

  public void testPartialArrayCreation3D() throws IOException {
    String translation = translateSourceFile(
        "class Test { void foo() { char[][][] c = new char[3][][]; } }", "Test", "Test.m");
    assertTranslation(translation, "#include \"IOSObjectArray.h\"");
    assertTranslation(translation,
        "IOSObjectArray *c = [IOSObjectArray arrayWithLength:3 type:IOSClass_charArray(2)]");
  }

  public void testUnsignedRightShift() throws IOException {
    String translation = translateSourceFile(
        "public class Test { void test(int a, long b, char c, byte d, short e) { "
        + "long r; r = a >>> 1; r = b >>> 2; r = c >>> 3; r = d >>> 4; r = e >>> 5; }}",
        "Test", "Test.m");
    assertTranslation(translation, "r = JreURShift32(a, 1);");
    assertTranslation(translation, "r = JreURShift64(b, 2);");
    assertTranslation(translation, "r = JreURShift32(c, 3);");
    assertTranslation(translation, "r = JreURShift32(d, 4);");
    assertTranslation(translation, "r = JreURShift32(e, 5);");
  }

  public void testUnsignedRightShiftAssign() throws IOException {
    String translation = translateSourceFile(
        "public class Test { void test(int a, long b, char c, byte d, short e) { "
        + "a >>>= 1; b >>>= 2; c >>>= 3; d >>>= 4; e >>>= 5; }}",
        "Test", "Test.m");
    assertTranslation(translation, "JreURShiftAssignInt(&a, 1);");
    assertTranslation(translation, "JreURShiftAssignLong(&b, 2);");
    assertTranslation(translation, "JreURShiftAssignChar(&c, 3);");
    assertTranslation(translation, "JreURShiftAssignByte(&d, 4);");
    assertTranslation(translation, "JreURShiftAssignShort(&e, 5);");
  }

  public void testUnsignedShiftRightAssignCharArray() throws IOException {
    String translation = translateSourceFile(
        "public class Test { void test(char[] array) { "
        + "array[0] >>>= 2; }}",
        "Test", "Test.m");
    assertTranslation(translation,
        "JreURShiftAssignChar(IOSCharArray_GetRef(nil_chk(array), 0), 2)");
  }

  public void testDoubleQuoteConcatenation() throws IOException {
    String translation = translateSourceFile(
        "public class Test { String test(String s) { return '\"' + s + '\"'; }}",
        "Test", "Test.m");
    assertTranslation(translation, "return JreStrcat(\"C$C\", '\"', s, '\"');");
  }

  public void testIntConcatenation() throws IOException {
    String translation = translateSourceFile(
        "public class Test { void check(boolean expr, String fmt, Object... args) {} "
        + "void test(int i, int j) { check(true, \"%d-%d\", i, j); }}",
        "Test", "Test.m");
    assertTranslation(translation,
        "[self checkWithBoolean:true withNSString:@\"%d-%d\" "
        + "withNSObjectArray:[IOSObjectArray arrayWithObjects:(id[]){ "
        + "JavaLangInteger_valueOfWithInt_(i), JavaLangInteger_valueOfWithInt_(j) } count:2 "
        + "type:NSObject_class_()]];");
  }

  // Verify that a string == comparison is converted to compare invocation.
  public void testStringComparison() throws IOException {
    String translation = translateSourceFile(
      "public class Test { void check(String s, Object o) { "
      + "boolean b1 = s == null; boolean b2 = \"foo\" == s; boolean b3 = o == \"bar\"; "
      + "boolean b4 = \"baz\" != s; boolean b5 = null != \"abc\"; }}",
      "Test", "Test.m");
    // Assert that non-string compare isn't converted.
    assertTranslation(translation, "jboolean b1 = s == nil;");
    // Assert string equate is converted,
    assertTranslation(translation, "jboolean b2 = [@\"foo\" isEqual:s];");
    // Order is reversed when literal is on the right.
    assertTranslation(translation, "jboolean b3 = [@\"bar\" isEqual:o];");
    // Not equals is converted.
    assertTranslation(translation, "jboolean b4 = ![@\"baz\" isEqual:s];");
    // Comparing null with string literal.
    assertTranslation(translation, "jboolean b5 = ![@\"abc\" isEqual:nil];");
  }

  public void testBinaryLiterals() throws IOException {
    String translation = translateSourceFile(
        "public class A { "
        + "  byte aByte = (byte)0b00100001; short aShort = (short)0b1010000101000101;"
        + "  int anInt1 = 0b10100001010001011010000101000101; "
        + "  int anInt2 = 0b101; int anInt3 = 0B101;"  // b can be lower or upper case.
        + "  long aLong = 0b1010000101000101101000010100010110100001010001011010000101000101L; }",
        "A", "A.m");
    assertTranslation(translation, "aByte_ = (jbyte) 0b00100001;");
    assertTranslation(translation, "aShort_ = (jshort) 0b1010000101000101;");
    assertTranslation(translation, "anInt1_ = 0b10100001010001011010000101000101;");
    assertTranslation(translation, "anInt2_ = 0b101;");
    assertTranslation(translation, "anInt3_ = 0B101;");
    assertTranslation(translation,
        "aLong_ = 0b1010000101000101101000010100010110100001010001011010000101000101LL;");
  }

  public void testUnderscoresInNumericLiterals() throws IOException {
    String translation = translateSourceFile(
        "public class A { "
        + "  long creditCardNumber = 1234_5678_9012_3456L; "
        + "  long socialSecurityNumber = 999_99_9999L; "
        + "  float pi =  3.14_15F; "
        + "  long hexBytes = 0xFF_EC_DE_5E; "
        + "  long hexWords = 0xCAFE_BABE; "
        + "  long maxLong = 0x7fff_ffff_ffff_ffffL; "
        + "  byte nybbles = 0b0010_0101; "
        + "  long bytes = 0b11010010_01101001_10010100_10010010; }", "A", "A.m");
    assertTranslation(translation, "creditCardNumber_ = 1234567890123456LL;");
    assertTranslation(translation, "socialSecurityNumber_ = 999999999LL;");
    assertTranslation(translation, "pi_ = 3.1415f;");
    assertTranslation(translation, "hexBytes_ = (jint) 0xFFECDE5E;");
    assertTranslation(translation, "hexWords_ = (jint) 0xCAFEBABE;");
    assertTranslation(translation, "maxLong_ = (jlong) 0x7fffffffffffffffLL;");
    assertTranslation(translation, "nybbles_ = 0b00100101;");
    assertTranslation(translation, "bytes_ = 0b11010010011010011001010010010010;");
  }

  // Verify that the null literal is concatenated as "null" in strings.
  public void testNullConcatenation() throws IOException {
    String translation = translateSourceFile(
        "public class Test { String test(String s) { return \"the nil value is \" + null; }}",
        "Test", "Test.m");
    assertTranslation(translation,
        "return JreStrcat(\"$@\", @\"the nil value is \", nil);");
  }

  public void testTypeVariableWithBoundsIsCast() throws IOException {
    String translation = translateSourceFile(
        "class Test<E> { interface A<T> { void foo(); } class B { int foo() { return 1; } } "
        + "<T extends A<? super E>> void test(T t) { t.foo(); } "
        + "<T extends B> void test2(T t) { t.foo(); } }", "Test", "Test.m");
    assertTranslation(translation, "[((id<Test_A>) nil_chk(t)) foo];");
    assertTranslation(translation, "[((Test_B *) nil_chk(t)) foo];");
  }

  public void testTypeVariableWithMultipleBounds() throws IOException {
    String translation = translateSourceFile(
        "class Test<T extends String & Runnable & Cloneable> { T t; }", "Test", "Test.h");
    assertTranslation(translation, "NSString<JavaLangRunnable, NSCopying> *t");
  }

  public void testMultiCatch() throws IOException {
    String translation = translateSourceFile(
        "import java.util.*; public class Test { "
        + "  static class FirstException extends Exception {} "
        + "  static class SecondException extends Exception {} "
        + "  public void rethrowException(String exceptionName) "
        + "      throws FirstException, SecondException { "
        + "    try { "
        + "      if (exceptionName.equals(\"First\")) { throw new FirstException(); } "
        + "      else { throw new SecondException(); }"
        + "    } catch (FirstException|SecondException e) { throw e; }}}",
        "Test", "Test.m");
    assertTranslation(translation,
        "@catch (Test_FirstException *e) {\n    @throw e;\n  }");
    assertTranslation(translation,
        "@catch (Test_SecondException *e) {\n    @throw e;\n  }");
    assertNotInTranslation(translation,
        "@catch (JavaLangException *e) {\n    @throw e;\n  }");
  }

  public void testLambdaCapturesMultiCatchExceptionParameter() throws IOException {
    String translation = translateSourceFile(
        "import java.util.function.Supplier; "
            + "public class Test { "
            + "  public void test() { "
            + "    try { "
            + "      \"\".charAt(10); "
            + "    } catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) { "
            + "      Supplier<String> s = () -> e.getMessage(); "
            + "      System.out.println(s.get()); "
            + "    } "
            + "  } "
            + "} ",
        "Test", "Test.m");
    // Note that the type of the captured parameter is the least upper bound of
    // (Array | String) IndexOutOfBoundsException.
    assertTranslation(translation, "JavaLangIndexOutOfBoundsException *capture$0");
  }

  public void testDifferentTypesInConditionalExpression() throws IOException {
    String translation = translateSourceFile(
        "class Test { String test(Runnable r) { return \"foo\" + (r != null ? r : \"bar\"); } }",
        "Test", "Test.m");
    assertTranslation(translation, "(r != nil ? r : (id) @\"bar\")");
  }

  // Verify that when a method invocation returns an object that is ignored,
  // it is cast to (void) to avoid a clang warning when compiling with ARC.
  public void testVoidedUnusedInvocationReturn() throws IOException {
    options.setMemoryManagementOption(MemoryManagementOption.ARC);
    String translation = translateSourceFile(
        "class Test { void test() {"
        + "  StringBuilder sb = new StringBuilder();"
        + "  sb.append(\"hello, world\");"
        + "  new Throwable(); }}",
        "Test", "Test.m");
    assertTranslation(translation, "(void) [sb appendWithNSString:@\"hello, world\"];");
    assertTranslation(translation, "(void) new_JavaLangThrowable_init();");
  }

  // Verify that multiple resources are closed in reverse order from opening.
  public void testTryMultiResourcesNoCatchOrFinally() throws IOException {
    String translation = translateSourceFile(
        "import java.io.*; public class Test { "
        + "static class Resource implements AutoCloseable { "
        + "  public void close() throws Exception {}} "
        + "void test() throws Exception { "
        + "  try (Resource r1 = new Resource();"
        + "       Resource r2 = new Resource();"
        + "       Resource r3 = new Resource()) {"
        + "  }}}",
        "Test", "Test.m");
    assertTranslatedSegments(translation,
        "Test_Resource *r1", "Test_Resource *r2", "Test_Resource *r3");
    assertTranslatedSegments(translation, "[r3 close]", "[r2 close]", "[r1 close]");
  }

  public void testGenericResultIsCastForChainedMethodCall() throws IOException {
    String translation = translateSourceFile(
        "abstract class Test<T extends Test.Foo> { "
        + "abstract T getObj(); static class Foo { void foo() { } } "
        + "static void test(Test<Foo> t) { t.getObj().foo(); } }", "Test", "Test.m");
    assertTranslation(translation, "[((Test_Foo *) nil_chk([((Test *) nil_chk(t)) getObj])) foo]");
  }

  public void testCastResultWhenInterfaceDeclaresMoreGenericType() throws IOException {
    String translation = translateSourceFile(
        "class Test { "
        + "interface I1 { A foo(); } "
        + "interface I2 { B foo(); } "
        + "static class A { } static class B extends A { } "
        + "static abstract class C { abstract B foo(); } "
        + "static abstract class D extends C implements I1, I2 { } "
        + "B test(D d) { return d.foo(); } }", "Test", "Test.h");
    // Check that protocols are declared in the same order.
    assertTranslation(translation, "@interface Test_D : Test_C < Test_I1, Test_I2 >");
    // A "foo" declaration is added to class "D" to override the less specific
    // return type inherited from "I1".
    assertOccurrences(translation, "- (Test_B *)foo;", 3);
    translation = getTranslatedFile("Test.m");
    // Check that the result of d.foo() is not cast.
    assertTranslation(translation, "return [((Test_D *) nil_chk(d)) foo];");
  }

  public void testStaticMethodCalledOnObject() throws IOException {
    String translation = translateSourceFile(
        "class Test { static void foo() {} void test(Test t) { t.foo(); } }", "Test", "Test.m");
    assertTranslation(translation, "Test_foo();");
  }

  public void testAnnotationVariableDeclaration() throws IOException {
    String translation = translateSourceFile(
        "public class Test { void test() { "
        + "Deprecated annotation = null; }}",
        "Test", "Test.m");
    assertTranslation(translation, "id<JavaLangDeprecated> annotation = ");
  }

  public void testAnnotationTypeLiteral() throws IOException {
    String translation = translateSourceFile(
        "@Deprecated public class Test { "
        + "  Deprecated deprecated() { "
        + "    return Test.class.getAnnotation(Deprecated.class); }}",
        "Test", "Test.m");
    assertTranslation(translation, "JavaLangDeprecated_class_()");
  }

  public void testEnumThisCallWithNoArguments() throws IOException {
    String translation = translateSourceFile(
        "enum Test { A, B; Test() {} Test(int i) { this(); } }", "Test", "Test.m");
    assertTranslation(translation,
        "JavaLangEnum_initWithNSString_withInt_(self, __name, __ordinal);");
    // Called from the "this()" call.
    assertOccurrences(translation,
        "Test_initWithNSString_withInt_(self, __name, __ordinal);", 1);
  }

  public void testForStatementWithMultipleInitializers() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test() { "
        + "for (String s1 = null, s2 = null;;) {}}}", "Test", "Test.m");
    // C requires that each var have its own pointer.
    assertTranslation(translation, "for (NSString *s1 = nil, *s2 = nil; ; )");
  }

  // Verify that constant variables are directly referenced when expression is "self".
  public void testSelfStaticVarAccess() throws IOException {
    String translation = translateSourceFile(
        "public class Test { enum Type { TYPE_BOOL; } Type test() { return Type.TYPE_BOOL; }}",
        "Test", "Test.m");
    assertTranslation(translation, "return JreLoadEnum(Test_Type, TYPE_BOOL);");
  }

  public void testMakeQuotedStringHang() throws IOException {
    // Test hangs if bug makeQuotedString() isn't fixed.
    translateSourceFile(
        "public class Test { void test(String s) { assert !\"null\\foo\\nbar\".equals(s); }}",
        "Test", "Test.m");
  }

  // Verify that the type of superclass field's type variable is cast properly.
  public void testSuperTypeVariable() throws IOException {
    addSourceFile("import java.util.List; class TestList <T extends List> { "
        + "  protected final T testField; TestList(T field) { testField = field; }}",
        "TestList.java");
    addSourceFile("import java.util.ArrayList; class TestArrayList extends TestList<ArrayList> { "
        + "  TestArrayList(ArrayList list) { super(list); }}",
        "TestArrayList.java");
    String translation = translateSourceFile(
        "import java.util.ArrayList; class Test extends TestArrayList { "
        + "  Test(ArrayList list) { super(list); } "
        + "  private class Inner {"
        + "    void test() { testField.ensureCapacity(42); }}}",
        "Test", "Test.m");
    assertTranslation(translation,
        "[((JavaUtilArrayList *) nil_chk(this$0_->testField_)) ensureCapacityWithInt:42];");
  }

  public void testNoTrigraphs() throws IOException {
    String translation = translateSourceFile(
        // C trigraph list from http://en.wikipedia.org/wiki/Digraphs_and_trigraphs#C.
        "class Test { static final String S1 = \"??=??/??'??(??)??!??<??>??-\"; "
        // S2 has char sequences that start with ?? but aren't trigraphs.
        + " static final String S2 = \"??@??$??%??&??*??A??z??1??.\"; }",
        "Test", "Test.m");
    assertTranslation(translation,
        "S1 = @\"?\" \"?=?\" \"?/?\" \"?'?\" \"?(?\" \"?)?\" \"?!?\" \"?<?\" \"?>?\" \"?-\";");
    assertTranslation(translation, "S2 = @\"??@??$??%??&??*??A??z??1??.\";");
  }

  // Verify that casting from a floating point primitive to an integral primitive
  // uses the right cast macro.
  public void testFloatingPointCasts() throws IOException {
    String translation = translateSourceFile(
        "public class Test { "
        + "  byte testByte(float f) { return (byte) f; }"
        + "  char testChar(float f) { return (char) f; }"
        + "  short testShort(float f) { return (short) f; }"
        + "  int testInt(float f) { return (int) f; }"
        + "  long testLong(float f) { return (long) f; }"
        + "  byte testByte(double d) { return (byte) d; }"
        + "  char testChar(double d) { return (char) d; }"
        + "  short testShort(double d) { return (short) d; }"
        + "  int testInt(double d) { return (int) d; }"
        + "  long testLong(double d) { return (long) d; }}",
        "Test", "Test.m");
    // Verify referenced return value is cast.
    assertTranslatedLines(translation,
        "- (jbyte)testByteWithFloat:(jfloat)f {", "return (jbyte) JreFpToInt(f);");
    assertTranslatedLines(translation,
        "- (jchar)testCharWithFloat:(jfloat)f {", "return JreFpToChar(f);");
    assertTranslatedLines(translation,
        "- (jshort)testShortWithFloat:(jfloat)f {", "return (jshort) JreFpToInt(f);");
    assertTranslatedLines(translation,
        "- (jint)testIntWithFloat:(jfloat)f {", "return JreFpToInt(f);");
    assertTranslatedLines(translation,
        "- (jlong)testLongWithFloat:(jfloat)f {", "return JreFpToLong(f);");
    assertTranslatedLines(translation,
        "- (jbyte)testByteWithDouble:(jdouble)d {", "return (jbyte) JreFpToInt(d);");
    assertTranslatedLines(translation,
        "- (jchar)testCharWithDouble:(jdouble)d {", "return JreFpToChar(d);");
    assertTranslatedLines(translation,
        "- (jshort)testShortWithDouble:(jdouble)d {", "return (jshort) JreFpToInt(d);");
    assertTranslatedLines(translation,
        "- (jint)testIntWithDouble:(jdouble)d {", "return JreFpToInt(d);");
    assertTranslatedLines(translation,
        "- (jlong)testLongWithDouble:(jdouble)d {", "return JreFpToLong(d);");
  }

  // Verify that string constants used in switch statements can be generated after functionizing.
  public void testFunctionalizedStringStringStatement() throws IOException {
    String source = "class A { "
        + "private static final String STR = \"\"; "
        + "private void f(String s) { switch(s) { case STR: return; } } "
        + "public void g() { f(\"\"); } }";
    // Assertion was thrown in StatementGenerator.getStringConstant(), due to the QualifiedName
    // node not having a constant value.
    translateSourceFile(source, "A", "A.m");
  }

  public void testSuppressedUnusedVariable() throws IOException {
    String translation = translateSourceFile(
        "class Test {"
        + "void test() { "
        + "@SuppressWarnings(\"unused\") int foo; }}", "Test", "Test.m");
    assertTranslation(translation, "__unused jint foo;");
  }

  public void testSuppressedUnusedVariableFromMethod() throws IOException {
    String translation = translateSourceFile(
        "class Test {"
        + "@SuppressWarnings(\"unused\") void test() { "
        + "int foo; }}", "Test", "Test.m");
    assertTranslation(translation, "__unused jint foo;");
  }

  public void testSuppressedUnusedVariableFromClass() throws IOException {
    String translation = translateSourceFile(
        "@SuppressWarnings(\"unused\") class Test {"
        + "void test() { "
        + "  int foo; }}", "Test", "Test.m");
    assertTranslation(translation, "__unused jint foo;");
  }

  public void testSuppressedUnusedVariableByName() throws IOException {
    String translation =
        translateSourceFile("class Test { void test() { int unusedFoo; }}", "Test", "Test.m");
    assertTranslation(translation, "__unused jint unusedFoo;");
  }

  // Verify that empty statements line offset to owning statement is preserved.
  public void testEmptyStatementFormatting() throws IOException {
    String translation = translateSourceFile(
        "class Test {\n"
        + "  void foo(int a, int b) {\n"
        + "    if (a < b) ;\n"  // Empty statement on same line as if statement.
        + "  }\n"
        + "  void bar(int c, int d) {\n"
        + "    if (c < d)\n"
        + "      ;\n"           // Empty statement on different line than if statement.
        + "  }}", "Test", "Test.m");
    assertTranslation(translation, "if (a < b) ;");
    assertTranslatedLines(translation, "if (c < d)", ";");
  }

  public void testVarLocalVariables() throws IOException {
    if (!onJava10OrAbove()) {
      return;
    }
    String translation = translateSourceFile(String.join("\n",
        "import java.util.ArrayList;",
        "import java.util.stream.Stream;",
        "class Test {",
        "  Stream test() {",
        "    var list = new ArrayList<String>();",
        "    var stream = list.stream();",
        "    return stream;",
        "  }",
        "}"), "Test", "Test.m");
    // Verify correct type inference.
    assertTranslation(translation, "JavaUtilArrayList *list = create_JavaUtilArrayList_init();");
    assertTranslation(translation, "id<JavaUtilStreamStream> stream = [list stream];");
  }

  public void testVarLambdaExpressionParameter() throws IOException {
    if (!onJava11OrAbove()) {
      return;
    }
    String translation = translateSourceFile(String.join("\n",
        "import java.util.function.Function;",
        "class Test {",
        "  int test(String input) {",
        "    Function<String, Integer> f = (var s) -> s.length();",
        "    return f.apply(input);",
        "  }",
        "}"), "Test", "Test.m");
    assertTranslation(translation,
        "@interface Test_$Lambda$1 : NSObject < JavaUtilFunctionFunction >");
  }
}
