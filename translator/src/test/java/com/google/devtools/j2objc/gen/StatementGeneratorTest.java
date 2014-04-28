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
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.Options.MemoryManagementOption;

import org.eclipse.jdt.core.dom.Statement;

import java.io.IOException;
import java.util.List;

/**
 * Tests for {@link StatementGenerator}.
 *
 * @author Tom Ball
 */
public class StatementGeneratorTest extends GenerationTest {

  @Override
  protected void tearDown() throws Exception {
    Options.resetMemoryManagementOption();
    super.tearDown();
  }

  // Verify that return statements output correctly for reserved words.
  public void testReturnReservedWord() throws IOException {
    String translation = translateSourceFile(
        "public class Test { enum Type { TYPE_BOOL; } Type test() { return Type.TYPE_BOOL; }}",
        "Test", "Test.m");
    assertTranslation(translation, "return Test_TypeEnum_get_TYPE_BOOL_();");
  }

  // Verify that super.method(), where method is static, sends the
  // class the message, not super.  Objective-C
  public void testStaticSuperInvocation() throws IOException {
    String translation = translateSourceFile(
        "public class A { static class Base { static void test() {} } " +
        "static class Foo extends Base { void test2() { super.test(); } }}", "A", "A.m");
    assertTranslation(translation, "[[super class] test];");
  }

  // Verify that both a class and interface type invoke getClass() correctly.
  public void testGetClass() throws IOException {
    String translation = translateSourceFile(
      "import java.util.*; public class A {" +
      "  void test(ArrayList one, List two) { " +
      "    Class<?> classOne = one.getClass();" +
      "    Class<?> classTwo = two.getClass(); }}",
      "A", "A.m");
    assertTranslation(translation, "[((JavaUtilArrayList *) nil_chk(one)) getClass]");
    assertTranslation(translation, "[((id<JavaUtilList>) nil_chk(two)) getClass]");
  }

  public void testEnumConstantsInSwitchStatement() throws IOException {
    String translation = translateSourceFile(
      "public class A { static enum EnumType { ONE, TWO }" +
      "public static void doSomething(EnumType e) {" +
      " switch (e) { case ONE: break; case TWO: break; }}}",
      "A", "A.m");
    assertTranslation(translation, "switch ([e ordinal]) {");
    assertTranslation(translation, "case A_EnumType_ONE:");
  }

  public void testEnumConstantReferences() throws IOException {
    String translation = translateSourceFile(
      "public class A { static enum B { ONE, TWO; " +
      "public static B doSomething(boolean b) { return b ? ONE : TWO; }}}",
      "A", "A.m");
    assertTranslation(translation, "return b ? A_BEnum_ONE : A_BEnum_TWO;");
  }

  public void testInnerClassFQN() throws IOException {
    String translation = translateSourceFile(
      "package com.example.foo; " +
      "public class Foo { static class Inner { public static void doSomething() {} }}" +
      "class Bar { public static void mumber() { Foo.Inner.doSomething(); }}",
      "Foo", "com/example/foo/Foo.m");
    assertTranslation(translation, "[ComExampleFooFoo_Inner doSomething];");
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
    assertEquals("[[[JavaLangException alloc] initWithNSString:@\"test\"] autorelease];", result);
  }

  public void testParameterTranslation() throws IOException {
    String source = "Throwable cause = new Throwable(); new Exception(cause);";
    List<Statement> stmts = translateStatements(source);
    assertEquals(2, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertEquals(
        "[[[JavaLangException alloc] initWithJavaLangThrowable:cause] autorelease];", result);
  }

  public void testCastTranslation() throws IOException {
    String source = "Exception e = new Exception(); Throwable t = (Throwable) e;";
    List<Statement> stmts = translateStatements(source);
    assertEquals(2, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertEquals("JavaLangThrowable *t = " +
        "(JavaLangThrowable *) check_class_cast(e, [JavaLangThrowable class]);", result);
  }

  public void testInterfaceCastTranslation() throws IOException {
    String source = "java.util.ArrayList al = new java.util.ArrayList(); " +
        "java.util.List l = (java.util.List) al;";
    List<Statement> stmts = translateStatements(source);
    assertEquals(2, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertEquals("id<JavaUtilList> l = " +
        "(id<JavaUtilList>) check_protocol_cast(al, @protocol(JavaUtilList));", result);
  }

  public void testCatchTranslation() throws IOException {
    String source = "try { ; } catch (Exception e) {}";
    List<Statement> stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("@try {\n;\n}\n @catch (JavaLangException *e) {\n}", result);
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
    assertTranslation(translation, "Example_set_foo_(self, Example_Bar_get_FOO_())");
    assertTranslation(translation, "NSString * Example_Bar_FOO_ = @\"Mumble\";");
    translation = getTranslatedFile("Example.h");
    assertTranslation(translation, "FOUNDATION_EXPORT NSString *Example_Bar_FOO_;");
    assertTranslation(translation, "J2OBJC_STATIC_FIELD_GETTER(Example_Bar, FOO_, NSString *)");
  }

  public void testMultipleVariableDeclarations() throws IOException {
    String source = "String one, two;";
    List<Statement> stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("NSString *one, *two;", result);
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
        "Example_set_b1_(self, JavaLangBoolean_get_TRUE__())");
    assertTranslation(translation,
        "Example_set_b2_(self, JavaLangBoolean_get_FALSE__())");
  }

  public void testStringConcatenation() throws IOException {
    String translation = translateSourceFile(
      "public class Example<K,V> { void test() { String s = \"hello, \" + \"world\"; }}",
      "Example", "Example.m");
    assertTranslation(translation, "NSString *s = @\"hello, world\"");
  }

  public void testStringConcatenation2() throws IOException {
    String source = "class A { " +
        "private static final String A = \"bob\"; " +
        "private static final char SPACE = ' '; " +
        "private static final double ANSWER = 22.0 / 2; " +
        "private static final boolean B = false; " +
        "private static final String C = " +
        "\"hello \" + A + ' ' + 3 + SPACE + true + ' ' + ANSWER + ' ' + B; }";
    String translation = translateSourceFile(source, "A", "A.m");
    assertTranslation(translation, "\"hello bob 3 true 11.0 false\"");
  }

  public void testStringConcatenationTypes() throws IOException {
    String translation = translateSourceFile(
      "public class Example<K,V> { Object obj; boolean b; char c; double d; float f; int i; " +
      "long l; short s; String str; public String toString() { " +
      "return \"obj=\" + obj + \" b=\" + b + \" c=\" + c + \" d=\" + d + \" f=\" + f" +
      " + \" i=\" + i + \" l=\" + l + \" s=\" + s; }}",
      "Example", "Example.m");
    assertTranslation(translation,
        "return [NSString stringWithFormat:@\"obj=%@ b=%@ c=%C d=%f f=%f i=%d l=%lld s=%d\", " +
        "obj_, [JavaLangBoolean toStringWithBoolean:b_], c_, d_, f_, i_, l_, s_];");
  }

  public void testStringConcatenationWithLiterals() throws IOException {
    String translation = translateSourceFile(
      "public class Example<K,V> { public String toString() { " +
      "return \"literals: \" + true + \", \" + 'c' + \", \" + 1.0d + \", \" + 3.14 + \", \"" +
      " + 42 + \", \" + 123L + \", \" + 1; }}",
      "Example", "Example.m");
    assertTranslation(translation, "return @\"literals: true, c, 1.0d, 3.14, 42, 123L, 1\";");
  }

  public void testStringConcatenationEscaping() throws IOException {
    String translation = translateSourceFile(
      "public class Example<K,V> { String s = \"hello, \" + 50 + \"% of the world\\n\"; }",
      "Example", "Example.m");
    //TODO(pankaj): should copy the string below, not retain it.
    assertTranslation(translation,
        "Example_set_s_(self, @\"hello, 50% of the world\\n\")");
  }

  public void testStringConcatenationMethodInvocation() throws IOException {
    String translation = translateSourceFile(
        "public class Test { " +
        "  String getStr() { return \"str\"; } " +
        "  int getInt() { return 42; } " +
        "  void test() { " +
        "    String a = \"foo\" + getStr() + \"bar\" + getInt() + \"baz\"; } }",
        "Test", "Test.m");
    assertTranslation(translation,
        "[NSString stringWithFormat:@\"foo%@bar%dbaz\", [self getStr], [self getInt]]");
  }

  public void testIntCastInStringConcatenation() throws IOException {
    String translation = translateSourceFile(
        "public class Test { void test() { " +
        "  String a = \"abc\"; " +
        "  String b = \"foo\" + a.hashCode() + \"bar\" + a.length() + \"baz\"; } }",
        "Test", "Test.m");
    assertTranslation(translation,
        "[NSString stringWithFormat:@\"foo%dbar%dbaz\", ((int) [a hash]), ((int) [a length])]");
  }

  public void testVarargsMethodInvocation() throws IOException {
    String translation = translateSourceFile("public class Example { "
      + "public void call() { foo(null); bar(\"\", null, null); }"
      + "void foo(Object ... args) { }"
      + "void bar(String firstArg, Object ... varArgs) { }}",
      "Example", "Example.m");
    assertTranslation(translation, "[self fooWithNSObjectArray:");
    assertTranslation(translation,
        "[IOSObjectArray arrayWithObjects:(id[]){ nil, nil } count:2 "
        + "type:[IOSClass classWithClass:[NSObject class]]]");
    assertTranslation(translation, "[self barWithNSString:");
    assertTranslation(translation, "withNSObjectArray:");
  }

  public void testVarargsMethodInvocationSingleArg() throws IOException {
    String translation = translateSourceFile("public class Example { "
      + "public void call() { foo(1); }"
      + "void foo(Object ... args) { }}",
      "Example", "Example.m");
    assertTranslation(translation,
        "[self fooWithNSObjectArray:" +
        "[IOSObjectArray arrayWithObjects:(id[]){ [JavaLangInteger valueOfWithInt:1] } count:1 " +
        "type:[IOSClass classWithClass:[NSObject class]]]];");
  }

  public void testVarargsMethodInvocationPrimitiveArgs() throws IOException {
    String translation = translateSourceFile(
        "class Test { void call() { foo(1); } void foo(int... i) {} }", "Test", "Test.m");
    assertTranslation(translation,
        "[self fooWithIntArray:[IOSIntArray arrayWithInts:(int[]){ 1 } count:1]];");
  }

  public void testStaticInnerSubclassAccessingOuterStaticVar() throws IOException {
    String translation = translateSourceFile(
      "public class Test { public static final Object FOO = new Object(); " +
      "static class Inner { Object test() { return FOO; }}}",
      "Test", "Test.m");
    assertTranslation(translation, "return Test_get_FOO_();");
  }

  public void testReservedIdentifierReference() throws IOException {
    String translation = translateSourceFile(
      "public class Test { public int test(int id) { return id; }}",
      "Test", "Test.m");
    assertTranslation(translation, "- (int)testWithInt:(int)id_");
    assertTranslation(translation, "return id_;");
  }

  public void testReservedTypeQualifierReference() throws IOException {
    String translation = translateSourceFile(
      "public class Test { public int test(int in, int out) { return in + out; }}",
      "Test", "Test.m");
    assertTranslation(translation, "- (int)testWithInt:(int)inArg");
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
    assertTranslation(translation, "Test_set_i_(self, otherI);");
    assertTranslation(translation, "j_ = otherJ;");
    assertTranslation(translation, "Test_set_i_(self, nil);");
  }

  public void testInnerInnerClassFieldAccess() throws IOException {
    String translation = translateSourceFile(
      "public class Test { static class One {} static class Two extends Test { " +
      "Integer i; Two(Integer i) { this.i = i; } int getI() { return i.intValue(); }}}",
      "Test", "Test.m");
    assertTranslation(translation, "- (id)initWithJavaLangInteger:(JavaLangInteger *)i {");
    assertTranslation(translation, "return [((JavaLangInteger *) nil_chk(i_)) intValue];");
  }

  public void testInnerClassSuperConstructor() throws IOException {
    String translation = translateSourceFile(
      "public class Test { static class One { int i; One(int i) { this.i = i; }} " +
      "static class Two extends One { Two(int i) { super(i); }}}",
      "Test", "Test.m");
    assertTranslation(translation, "- (id)initWithInt:(int)i");
    assertTranslation(translation, "[super initWithInt:i]");
  }

  public void testStaticInnerClassSuperFieldAccess() throws IOException {
    String translation = translateSourceFile(
      "public class Test { protected int foo; " +
      "static class One extends Test { int i; One() { i = foo; } int test() { return i; }}}",
      "Test", "Test.m");
    assertTranslation(translation, "- (id)init {");
    assertTranslation(translation, "i_ = foo_;");
    assertTranslation(translation, "return i_;");
  }

  public void testMethodInvocationOfReturnedInterface() throws IOException {
    String translation = translateSourceFile(
      "import java.util.*; public class Test <K,V> { " +
      "Iterator<Map.Entry<K,V>> iterator; " +
      "K test() { return iterator.next().getKey(); }}",
      "Test", "Test.m");
    assertTranslation(translation, "return [((id<JavaUtilMap_Entry>) " +
        "nil_chk([((id<JavaUtilIterator>) nil_chk(iterator_)) next])) getKey];");
  }

  public void testAnonymousClassInInnerStatic() throws IOException {
    String translation = translateSourceFile(
        "import java.util.*; public class Test { " +
        "static <T> Enumeration<T> enumeration(Collection<T> collection) {" +
        "final Collection<T> c = collection; " +
        "return new Enumeration<T>() { " +
        "Iterator<T> it = c.iterator(); " +
        "public boolean hasMoreElements() { return it.hasNext(); } " +
        "public T nextElement() { return it.next(); } }; }}",
        "Test", "Test.m");
    assertTranslation(translation, "return [((id<JavaUtilIterator>) nil_chk(it_)) hasNext];");
    assertTranslation(translation, "return [((id<JavaUtilIterator>) nil_chk(it_)) next];");
    assertFalse(translation.contains("Test *this$0;"));
  }

  public void testGenericMethodWithAnonymousReturn() throws IOException {
    String translation = translateSourceFile(
        "import java.util.*; public class Test { " +
        "public static <T> Enumeration<T> enumeration(final Collection<T> collection) {" +
        "return new Enumeration<T>() {" +
        "  Iterator<T> it = collection.iterator();" +
        "  public boolean hasMoreElements() { return it.hasNext(); }" +
        "  public T nextElement() { return it.next(); }}; }}",
        "Test", "Test.m");
    assertTranslation(translation,
        "return [[[Test_$1 alloc] initWithJavaUtilCollection:collection] autorelease];");
    translation = getTranslatedFile("Test.h");
    assertTranslation(translation,
        "- (id)initWithJavaUtilCollection:(id<JavaUtilCollection>)capture$0;");
  }

  public void testEnumInEqualsTest() throws IOException {
    String translation = translateSourceFile(
        "public class Test { enum TicTacToe { X, Y } " +
        "boolean isX(TicTacToe ttt) { return ttt == TicTacToe.X; } }",
        "Test", "Test.m");
    assertTranslation(translation, "return ttt == Test_TicTacToeEnum_get_X();");
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
        "public class Test { void test(char[] foo, char bar[]) { " +
        "sync(foo.length, bar.length); } void sync(int a, int b) {} }",
        "Test", "Test.m");
    assertTranslation(translation,
        "[self syncWithInt:(int) [((IOSCharArray *) nil_chk(foo)) count] withInt:(int) " +
        "[((IOSCharArray *) nil_chk(bar)) count]];");
  }

  public void testLongLiteral() throws IOException {
    String translation = translateSourceFile("public class Test { "
        + "public static void testLong() { long l1 = 1L; }}", "Test", "Test.m");
    assertTranslation(translation, "long long int l1 = 1LL");
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
    assertEquals("IOSIntArray *a = [IOSIntArray arrayWithInts:(int[]){ 1, 2, 3 } count:3];",
        result);
    result = generateStatement(stmts.get(1));
    assertEquals("IOSCharArray *b = " +
        "[IOSCharArray arrayWithChars:(unichar[]){ '4', '5' } count:2];", result);
  }

  /**
   * Verify that static array initializers are rewritten as method calls.
   */
  public void testStaticArrayInitializer() throws IOException {
    String translation = translateSourceFile(
        "public class Test { static int[] a = { 1, 2, 3 }; static char b[] = { '4', '5' }; }",
        "Test", "Test.m");
    assertTranslation(translation,
        "JreOperatorRetainedAssign(&Test_a_, nil, " +
        "[IOSIntArray arrayWithInts:(int[]){ 1, 2, 3 } count:3]);");
    assertTranslation(translation,
        "JreOperatorRetainedAssign(&Test_b_, nil, " +
        "[IOSCharArray arrayWithChars:(unichar[]){ '4', '5' } count:2]);");
  }

  public void testLocalArrayCreation() throws IOException {
    String translation = translateSourceFile(
      "public class Example { char[] test() { int high = 0xD800, low = 0xDC00; " +
            "return new char[] { (char) high, (char) low }; } }",
      "Example", "Example.m");
    assertTranslation(translation, "return [IOSCharArray " +
        "arrayWithChars:(unichar[]){ (unichar) high, (unichar) low } count:2];");
  }

  // Regression test: "case:" was output instead of "case".
  public void testSwitchCaseStatement() throws IOException {
    String source = "int c = 1; " +
        "switch (c) { case 1: c = 0; break; default: break; }";
    List<Statement> stmts = translateStatements(source);
    assertEquals(2, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertTrue(result.contains("case 1:"));
  }

  public void testEnhancedForStatement() throws IOException {
    String source = "String[] strings = {\"test1\", \"test2\"};" +
        "for (String string : strings) { }";
    List<Statement> stmts = translateStatements(source);
    assertEquals(2, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertTranslatedLines(result,
        "{",
          "IOSObjectArray *a__ = strings;",
          "NSString * const *b__ = a__->buffer_;",
          "NSString * const *e__ = b__ + a__->size_;",
          "while (b__ < e__) {",
            "NSString *string = (*b__++);",
          "}",
        "}");
  }

  public void testEnhancedForStatementInSwitchStatement() throws IOException {
    String source = "int test = 5; int[] myInts = new int[10]; " +
        "switch (test) { case 0: break; default: " +
        "for (int i : myInts) {} break; }";
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
              "int const *b__ = a__->buffer_;",
              "int const *e__ = b__ + a__->size_;",
              "while (b__ < e__) {",
                "int i = (*b__++);",
              "}",
            "}",
            "break;",
        "}");
  }

  public void testSwitchStatementWithExpression() throws IOException {
    String translation = translateSourceFile("public class Example { " +
        "static enum Test { ONE, TWO } " +
        "Test foo() { return Test.ONE; } " +
        "void bar() { switch (foo()) { case ONE: break; case TWO: break; }}}",
        "Example", "Example.m");
    assertTranslation(translation, "switch ([[self foo] ordinal])");
  }

  public void testClassVariable() throws IOException {
    String source = "Class<?> myClass = getClass();" +
        "Class<?> mySuperClass = myClass.getSuperclass();" +
        "Class<?> enumClass = Enum.class;";
    List<Statement> stmts = translateStatements(source);
    assertEquals(3, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("IOSClass *myClass = [self getClass];", result);
    result = generateStatement(stmts.get(1));
    assertEquals("IOSClass *mySuperClass = [myClass getSuperclass];", result);
    result = generateStatement(stmts.get(2));
    assertEquals("IOSClass *enumClass = [IOSClass classWithClass:[JavaLangEnum class]];", result);
  }

  public void testCastInConstructorChain() throws IOException {
    String source = "int i = new Throwable().hashCode();";
    List<Statement> stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("int i = ((int) [((JavaLangThrowable *) " +
    		"[[[JavaLangThrowable alloc] init] autorelease]) hash]);", result);
  }

  public void testInnerClassCreation() throws IOException {
    String translation = translateSourceFile(
      "public class A { int x; class Inner { int y; Inner(int i) { y = i + x; }}" +
      "public Inner test() { return this.new Inner(3); }}",
      "A", "A.m");
    assertTranslation(translation,
        "return [[[A_Inner alloc] initWithA:self withInt:3] autorelease];");
  }

  public void testNewFieldNotRetained() throws IOException {
    String translation = translateSourceFile(
      "import java.util.*; public class A { Map map; A() { map = new HashMap(); }}",
      "A", "A.m");
    assertTranslation(translation,
        "A_set_map_(self, [[[JavaUtilHashMap alloc] init] autorelease])");
  }

  public void testStringAddOperator() throws IOException {
    String translation = translateSourceFile(
      "import java.util.*; public class A { String myString;" +
      "  A() { myString = \"Foo\"; myString += \"Bar\"; }}",
      "A", "A.m");
    assertTranslation(translation,
        "A_set_myString_(self, [NSString stringWithFormat:@\"%@Bar\", myString_]);");
  }

  public void testPrimitiveConstantInSwitchCase() throws IOException {
    String translation = translateSourceFile(
      "public class A { public static final char PREFIX = 'p';" +
      "public boolean doSomething(char c) { switch (c) { " +
      "case PREFIX: return true; " +
      "default: return false; }}}",
      "A", "A.h");
    assertTranslation(translation, "#define A_PREFIX 'p'");
    translation = getTranslatedFile("A.m");
    assertTranslation(translation, "case A_PREFIX:");
  }

  public void testInterfaceStaticVarReference() throws IOException {
    String translation = translateSourceFile(
      "public class Test { " +
      "  public interface I { " +
      "    void foo(); public static final int FOO = 1; } " +
      "  public class Bar implements I { public void foo() { int i = I.FOO; } } }",
      "Test", "Test.m");
    assertTranslation(translation, "int i = Test_I_FOO;");
  }

  public void testMethodWithPrimitiveArrayParameter() throws IOException {
    String translation = translateSourceFile(
      "public class Test { " +
      "  public void foo(char[] chars) { } }",
      "Test", "Test.m");
    assertTranslation(translation, "fooWithCharArray:");
  }

  public void testMethodWithGenericArrayParameter() throws IOException {
    String translation = translateSourceFile(
      "public class Test<T> { " +
      "  T[] tArray; " +
      "  public void foo(T[] ts) { } " +
      "  public void bar(Test<? extends T>[] tLists) { } " +
      "  public void foo() { foo(tArray); } " +
      "  public class Inner<S extends Test<T>> { " +
      "    public void baz(S[] ss) { } } }",
      "Test", "Test.m");
    assertTranslation(translation, "- (void)fooWithNSObjectArray:");
    assertTranslation(translation, "- (void)barWithTestArray:");
    assertTranslation(translation, "- (void)foo {");
    assertTranslation(translation, "[self fooWithNSObjectArray:");
    assertTranslation(translation, "- (void)bazWithTestArray:");
  }

  public void testGenericMethodWithfGenericArrayParameter() throws IOException {
    String translation = translateSourceFile(
      "public class Test { " +
      "  public <T> void foo(T[] ts) { } " +
      "  public void foo() { foo(new String[1]); } }",
      "Test", "Test.m");
    assertTranslation(translation, "[self fooWithNSObjectArray:");
  }

  public void testJreDoubleNegativeInfinity() throws IOException {
    String translation = translateSourceFile(
        "public class Test { " +
        "  public void foo() { Double d = Double.NEGATIVE_INFINITY; } }",
        "Test", "Test.m");
    assertTranslation(translation,
        "[JavaLangDouble valueOfWithDouble:JavaLangDouble_NEGATIVE_INFINITY]");
  }

  public void testInvokeMethodInConcreteImplOfGenericInterface() throws IOException {
    String translation = translateSourceFile(
      "public class Test { " +
      "  public interface Foo<T> { void foo(T t); } " +
      "  public class FooImpl implements Foo<Test> { " +
      "    public void foo(Test t) { } " +
      "    public void bar() { foo(new Test()); } } }",
      "Test", "Test.m");
    assertTranslation(translation, "[self fooWithId:");
  }

  public void testNewStringWithArrayInAnonymousClass() throws IOException {
    String translation = translateSourceFile(
      "public class Test { " +
      "  public Runnable foo() { " +
      "    String s1 = new String(new char[10]); " +
      "    return new Runnable() { " +
      "      public void run() { String s = new String(new char[10]); } }; } }",
      "Test", "Test.m");
    assertTranslation(translation, "s = [NSString stringWith");
  }

  public void testMostNegativeIntegers() throws IOException {
    String translation = translateSourceFile(
      "public class Test { " +
      "  int min_int = 0x80000000; " +
      "  long min_long = 0x8000000000000000L; }",
      "Test", "Test.m");
    assertTranslation(translation, "-0x7fffffff - 1");
    assertTranslation(translation, "-0x7fffffffffffffffLL - 1");
  }

  public void testInnerNewStatement() throws IOException {
    String translation = translateSourceFile(
      "class A { class B {} static B test() { return new A().new B(); }}",
      "A", "A.m");
    assertTranslation(translation,
        "[[[A_B alloc] initWithA:[[[A alloc] init] autorelease]] autorelease]");
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
    assertEquals(result, "float f = JavaLangFloat_NaN;");
    result = generateStatement(stmts.get(1)).trim();
    assertEquals(result, "double d = JavaLangDouble_POSITIVE_INFINITY;");
  }

  public void testInstanceStaticConstants() throws IOException {
    String translation = translateSourceFile(
        "public class Test { Foo f; void test() { int i = f.DEFAULT; Object lock = f.LOCK; }} " +
        "class Foo { public static final int DEFAULT = 1; " +
        "public static final Object LOCK = null; }", "Test", "Test.m");
    assertTranslation(translation, "int i = Foo_DEFAULT;");
    assertTranslation(translation, "id lock = Foo_get_LOCK_();");
  }

  public void testCastGenericReturnType() throws IOException {
    String translation = translateSourceFile(
      "class Test { " +
      "  static class A<E extends A> { E other; public E getOther() { return other; } } " +
      "  static class B extends A<B> { B other = getOther(); } }",
      "Test", "Test.h");
    // Test_B's "other" needs a trailing underscore, since there is an "other"
    // field in its superclass.
    assertTranslation(translation, "Test_B *other_B_;");
    translation = getTranslatedFile("Test.m");
    assertTranslation(translation, "Test_B_set_other_B_(self, [self getOther])");
  }

  public void testArrayInstanceOfTranslation() throws IOException {
    String source = "Object args = new String[0]; if (args instanceof String[]) {}";
    List<Statement> stmts = translateStatements(source);
    assertEquals(2, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertEquals(
        "if ([[IOSObjectArray iosClassWithType:[IOSClass classWithClass:[NSString class]]] " +
        "isInstance:args]) {\n}", result);
  }

  public void testInterfaceArrayInstanceOfTranslation() throws IOException {
    String source = "Object args = new Readable[0]; if (args instanceof Readable[]) {}";
    List<Statement> stmts = translateStatements(source);
    assertEquals(2, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertEquals(
        "if ([[IOSObjectArray iosClassWithType:[IOSClass classWithProtocol:" +
        "@protocol(JavaLangReadable)]] isInstance:args]) {\n}", result);
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
    assertEquals("IOSObjectArray *a = [IOSObjectArray " +
        "arrayWithObjects:(id[]){ @\"one\", @\"two\", @\"three\" } " +
        "count:3 type:[IOSClass classWithClass:[NSString class]]];", result);

    source = "Comparable[] a = { \"one\", \"two\", \"three\" };";
    stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    result = generateStatement(stmts.get(0));
    assertEquals("IOSObjectArray *a = [IOSObjectArray " +
        "arrayWithObjects:(id[]){ @\"one\", @\"two\", @\"three\" } " +
        "count:3 type:[IOSClass classWithProtocol:@protocol(JavaLangComparable)]];", result);
  }

  public void testArrayPlusAssign() throws IOException {
    String source = "int[] array = new int[] { 1, 2, 3 }; int offset = 1; array[offset] += 23;";
    List<Statement> stmts = translateStatements(source);
    assertEquals(3, stmts.size());
    String result = generateStatement(stmts.get(2));
    assertEquals("(*IOSIntArray_GetRef(array, offset)) += 23;", result);
  }

  public void testRegisterVariableName() throws IOException {
    String source = "int register = 42;";
    List<Statement> stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("int register_ = 42;", result);
  }

  public void testStaticVariableSetterReference() throws IOException {
    String translation = translateSourceFile(
        "public class Example { public static java.util.Date today; }" +
        "class Test { void test(java.util.Date now) { Example.today = now; }}",
        "Example", "Example.m");
    assertTranslation(translation, "Example_set_today_(now);");
  }

  // b/5872533: reserved method name not renamed correctly in super invocation.
  public void testSuperReservedName() throws IOException {
    addSourceFile("public class A { A() {} public void init(int a) { }}", "A.java");
    addSourceFile(
        "public class B extends A { B() {} public void init(int b) { super.init(b); }}", "B.java");
    String translation = translateSourceFile("A", "A.h");
    assertTranslation(translation, "- (id)init;");
    assertTranslation(translation, "- (void)init__WithInt:(int)a");
    translation = translateSourceFile("B", "B.m");
    assertTranslation(translation, "return JreMemDebugAdd([super init]);");
    assertTranslation(translation, "[super init__WithInt:b];");
  }

  // b/5872710: generic return type needs to be cast if chaining invocations.
  public void testTypeVariableCast() throws IOException {
    String translation = translateSourceFile(
      "import java.util.ArrayList; public class Test {" +
      "  int length; static ArrayList<String> strings = new ArrayList<String>();" +
      "  public static void main(String[] args) { int n = strings.get(1).length(); }}",
      "Test", "Test.m");
    assertTranslation(translation, "((int) [((NSString *) " +
      "nil_chk([((JavaUtilArrayList *) nil_chk(Test_strings_)) getWithInt:1])) length]);");
  }

  // b/5872757: verify multi-dimensional array has cast before each
  // secondary reference.
  public void testMultiDimArrayCast() throws IOException {
    String translation = translateSourceFile(
      "public class Test {" +
      "  static String[][] a = new String[1][1];" +
      "  public static void main(String[] args) { " +
      "    a[0][0] = \"42\"; System.out.println(a[0].length); }}",
      "Test", "Test.m");
    assertTranslation(translation,
        "IOSObjectArray_Set(nil_chk(IOSObjectArray_Get(nil_chk(Test_a_), 0)), 0, @\"42\");");
    assertTranslation(translation,
        "[((IOSObjectArray *) nil_chk(IOSObjectArray_Get(Test_a_, 0))) count]");
  }

  public void testMultiDimArray() throws IOException {
    String source = "int[][] a = new int[][] { null, { 0, 2 }, { 2, 2 }};";
    List<Statement> stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    String translation = generateStatement(stmts.get(0));
    assertTranslation(translation,
        "IOSObjectArray *a = [IOSObjectArray arrayWithObjects:(id[]){ nil, " +
        "[IOSIntArray arrayWithInts:(int[]){ 0, 2 } count:2], " +
        "[IOSIntArray arrayWithInts:(int[]){ 2, 2 } count:2] } count:3 " +
        "type:[IOSIntArray iosClass]];");
  }

  public void testObjectMultiDimArray() throws IOException {
    String source = "class Test { Integer i = new Integer(1); Integer j = new Integer(2);" +
        "void test() { Integer[][] a = new Integer[][] { null, { i, j }, { j, i }}; }}";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation,
        "IOSObjectArray *a = [IOSObjectArray arrayWithObjects:(id[]){ nil, " +
        "[IOSObjectArray arrayWithObjects:(id[]){ i_, j_ } count:2 " +
        "type:[IOSClass classWithClass:[JavaLangInteger class]]], " +
        "[IOSObjectArray arrayWithObjects:(id[]){ j_, i_ } count:2 " +
        "type:[IOSClass classWithClass:[JavaLangInteger class]]] } count:3 " +
        "type:[IOSObjectArray iosClassWithType:" +
        "[IOSClass classWithClass:[JavaLangInteger class]]]];");
  }

  public void testVarargsMethodInvocationZeroLengthArray() throws IOException {
    String translation = translateSourceFile(
        "public class Example { " +
        " public void call() { foo(new Object[0]); bar(new Object[0]); } " +
        " public void foo(Object ... args) { } " +
        " public void bar(Object[] ... args) { } }",
        "Example", "Example.h");
    assertTranslation(translation, "- (void)fooWithNSObjectArray:(IOSObjectArray *)args");
    assertTranslation(translation, "- (void)barWithNSObjectArray2:(IOSObjectArray *)args");
    translation = getTranslatedFile("Example.m");

    // Should be equivalent to foo(new Object[0]).
    assertTranslation(translation,
        "[self fooWithNSObjectArray:[IOSObjectArray " +
        "arrayWithLength:0 type:[IOSClass classWithClass:[NSObject class]]]]");

    // Should be equivalent to bar(new Object[] { new Object[0] }).
    assertTranslation(translation,
        "[self barWithNSObjectArray2:[IOSObjectArray arrayWithObjects:" +
        "(id[]){ [IOSObjectArray arrayWithLength:0 type:" +
        "[IOSClass classWithClass:[NSObject class]]] } count:1 " +
        "type:[IOSObjectArray iosClassWithType:" +
        "[IOSClass classWithClass:[NSObject class]]]]];");
  }

  public void testVarargsIOSMethodInvocation() throws IOException {
    String translation = translateSourceFile(
        "import java.lang.reflect.Constructor; public class Test { " +
        " public void test() throws Exception { " +
        "  Constructor c1 = Test.class.getConstructor();" +
        "  Constructor c2 = Test.class.getConstructor(String.class);" +
        "  Constructor c3 = Test.class.getConstructor(String.class, Byte.TYPE);" +
        "  Class[] types = new Class[] { Object.class, Exception.class };" +
        "  Constructor c4 = Test.class.getConstructor(types); }}",
        "Test", "Test.m");
    assertTranslation(translation,
        "c1 = [[IOSClass classWithClass:[Test class]] getConstructor:" +
        "[IOSObjectArray arrayWithLength:0 type:" +
        "[IOSClass classWithClass:[IOSClass class]]]];");
    assertTranslation(translation,
        "c2 = [[IOSClass classWithClass:[Test class]] getConstructor:" +
        "[IOSObjectArray arrayWithObjects:(id[]){ [IOSClass classWithClass:[NSString class]] } " +
        "count:1 type:[IOSClass classWithClass:[IOSClass class]]]];");
    assertTranslation(translation,
        "c3 = [[IOSClass classWithClass:[Test class]] getConstructor:" +
        "[IOSObjectArray arrayWithObjects:(id[]){ [IOSClass classWithClass:[NSString class]], " +
        "JavaLangByte_get_TYPE_() } count:2 type:[IOSClass classWithClass:[IOSClass class]]]];");

    // Array contents should be expanded.
    assertTranslation(translation,
        "c4 = [[IOSClass classWithClass:[Test class]] getConstructor:types];");
  }

  public void testGetVarargsWithLeadingParameter() throws IOException {
    String translation = translateSourceFile(
        "public class Test {" +
        "  void test() throws Exception { " +
        "    getClass().getMethod(\"equals\", Object.class); }}",
        "Test", "Test.m");
    assertTranslation(translation,
        "[[self getClass] getMethod:@\"equals\" parameterTypes:[IOSObjectArray " +
        "arrayWithObjects:(id[]){ [IOSClass classWithClass:[NSObject class]] } count:1 " +
        "type:[IOSClass classWithClass:[IOSClass class]]]];");
  }

  public void testGetVarargsWithLeadingParameterNoArgs() throws IOException {
    String translation = translateSourceFile(
        "public class Test {" +
        "  void test() throws Exception { " +
        "    getClass().getMethod(\"hashCode\", new Class[0]); }}",
        "Test", "Test.m");
    assertTranslation(translation,
        "[[self getClass] getMethod:@\"hashCode\" parameterTypes:[IOSObjectArray " +
        "arrayWithLength:0 type:[IOSClass classWithClass:[IOSClass class]]]];");
  }

  public void testTypeVariableWithBoundCast() throws IOException {
    String translation = translateSourceFile(
      "import java.util.ArrayList; public class Test {" +
      "  public static class Foo<T extends Foo.Bar> {" +
      "    public static class Bar { } " +
      "    public T foo() { return null; } } " +
      "  public static class BarD extends Foo.Bar { } " +
      "  public void bar(Foo<BarD> f) { BarD b = f.foo(); } }",
      "Test", "Test.m");
    assertTranslation(translation, "[((Test_Foo *) nil_chk(f)) foo]");
  }

  // b/5934474: verify that static variables are always referenced by
  // their accessors in functions, since their class may not have loaded.
  public void testFunctionReferencesStaticVariable() throws IOException {
    String translation = translateSourceFile(
        "public class HelloWorld {" +
        "  static String staticString = \"hello world\";" +
        "  public static void main(String[] args) {" +
        "    System.out.println(staticString);" +
        "  }}",
        "HelloWorld", "HelloWorld.m");
      assertTranslation(translation, "printlnWithNSString:HelloWorld_staticString_];");
  }

  public void testThisCallInEnumConstructor() throws IOException {
    String translation = translateSourceFile(
        "public enum Test {" +
        "  A, B(1);" +
        "  private int i;" +
        "  private Test(int i) { this.i = i; } " +
        "  private Test() { this(0); }}",
        "Test", "Test.m");
    assertTranslation(translation,
        "[self initTestEnumWithInt:0 withNSString:__name withInt:__ordinal]");
  }

  public void testThisCallInInnerConstructor() throws IOException {
    String translation = translateSourceFile(
        "public class Test {" +
        "  class Inner {" +
        "    public Inner() { }" +
        "    public Inner(int foo) { this(); int i = foo; }}}",
        "Test", "Test.m");
    assertTranslation(translation, "self = [self initTest_InnerWithTest:outer$]");
  }

  // Verify that an external string can be used in string concatenation,
  // for a parameter to a translated method.
  public void testConcatPublicStaticString() throws IOException {
    String translation = translateSourceFile(
      "class B { public static final String separator = \"/\"; } "  +
      "public class A { String prefix(Object o) { return new String(o + B.separator); }}",
      "A", "A.m");
    assertTranslation(translation,
        "[NSString stringWithString:" +
        "[NSString stringWithFormat:@\"%@%@\", o, B_get_separator_()]];");
  }

  public void testStringConcatWithBoolean() throws IOException {
    String translation = translateSourceFile(
      "public class A { String test(boolean b) { return \"foo: \" + b; }}",
      "A", "A.m");
    assertTranslation(translation,
        "return [NSString stringWithFormat:@\"foo: %@\", " +
        "[JavaLangBoolean toStringWithBoolean:b]];");
  }

  public void testStringConcatWithChar() throws IOException {
    String translation = translateSourceFile(
      "public class A { String test(char c) { return \"foo: \" + c; }}",
      "A", "A.m");
    assertTranslation(translation, "return [NSString stringWithFormat:@\"foo: %C\", c];");
  }

  // Verify that double quote character constants are concatenated correctly.
  public void testConcatDoubleQuoteChar() throws IOException {
    String translation = translateSourceFile(
        "public class Test { " +
        "static final char QUOTE = '\"'; static final String TEST = QUOTE + \"\"; }",
        "Test", "Test.m");
    assertTranslation(translation, "Test_TEST_ = @\"\\\"\";");
  }

  /**
   * Verify that when a the last switch case is empty (no statement),
   * an empty statement is added.  Java doesn't require an empty statement
   * here, while C does.
   */
  public void testEmptyLastCaseStatement() throws IOException {
    String translation = translateSourceFile(
      "public class A {" +
      "  int test(int i) { " +
      "    switch (i) { case 1: return 1; case 2: return 2; default: } return i; }}",
      "A", "A.m");
    assertTranslation(translation, "default:\n    ;\n  }");
  }

  public void testBuildStringFromChars() {
    String s = "a\uffffz";
    String result = StatementGenerator.buildStringFromChars(s);
    assertEquals(result,
        "[NSString stringWithCharacters:(unichar[]) " +
        "{ (int) 0x61, (int) 0xffff, (int) 0x7a } length:3]");
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
      "public class A {" +
      "public static final char APOSTROPHE = '\\''; " +
      "public static final char BACKSLASH = '\\\\'; " +
      "void foo(char c) {} void test() { foo('\\''); foo('\\\\'); }}",
      "A", "A.h");
    assertTranslation(translation, "#define A_APOSTROPHE '\\''");
    assertTranslation(translation, "#define A_BACKSLASH '\\\\'");
    translation = getTranslatedFile("A.m");
    assertTranslation(translation, "fooWithChar:'\\'']");
    assertTranslation(translation, "fooWithChar:'\\\\']");
  }

  public void testStaticVarAccessFromInnerClass() throws IOException {
    String translation = translateSourceFile("public class Test { public static String foo; " +
        "interface Assigner { void assign(String s); } static { " +
        "new Assigner() { public void assign(String s) { foo = s; }}; }}",
        "Test", "Test.m");
    assertTranslation(translation, "Test_set_foo_(s);");
  }

  public void testNoAutoreleasePoolForStatement() throws IOException {
    String translation = translateSourceFile(
        "public class Test {" +
        "  public void foo() {" +
        "    for (int i = 0; i < 10; i++) {" +
        "    }" +
        "  }" +
        "}",
        "Test", "Test.m");
    assertTranslation(translation, "  for (int i = 0; i < 10; i++) {\n" +
        "  }");
  }

  public void testAutoreleasePoolForStatement() throws IOException {
    String translation = translateSourceFile(
        "import com.google.j2objc.annotations.AutoreleasePool;" +
        "public class Test {" +
        "  public void foo() {" +
        "    for (@AutoreleasePool int i = 0; i < 10; i++) {" +
        "    }" +
        "  }" +
        "}",
        "Test", "Test.m");
    assertTranslation(translation, "  for (int i = 0; i < 10; i++) {\n" +
        "    @autoreleasepool {\n    }\n" +
        "  }");
  }

  public void testAutoreleasePoolEnhancedForStatement() throws IOException {
    String translation = translateSourceFile(
        "import com.google.j2objc.annotations.AutoreleasePool;" +
        "public class Test {" +
        "  public void foo(String[] strings) {" +
        "    for (@AutoreleasePool String s : strings) {" +
        "    }" +
        "  }" +
        "}",
        "Test", "Test.m");
    assertTranslation(translation, "@autoreleasepool");
  }

  public void testARCAutoreleasePoolForStatement() throws IOException {
    Options.setMemoryManagementOption(MemoryManagementOption.ARC);
    String translation = translateSourceFile(
        "import com.google.j2objc.annotations.AutoreleasePool;" +
        "public class Test {" +
        "  public void foo() {" +
        "    for (@AutoreleasePool int i = 0; i < 10; i++) {" +
        "    }" +
        "  }" +
        "}",
        "Test", "Test.m");
    assertTranslation(translation, "  for (int i = 0; i < 10; i++) {\n" +
        "    @autoreleasepool {\n" +
        "    }\n" +
        "  }");
  }

  public void testARCAutoreleasePoolEnhancedForStatement() throws IOException {
    Options.setMemoryManagementOption(MemoryManagementOption.ARC);
    String translation = translateSourceFile(
        "import com.google.j2objc.annotations.AutoreleasePool;" +
        "public class Test {" +
        "  public void foo(String[] strings) {" +
        "    for (@AutoreleasePool String s : strings) {" +
        "    }" +
        "  }" +
        "}",
        "Test", "Test.m");
    assertTranslation(translation, "@autoreleasepool {");
  }

  public void testShiftAssignArrayElement() throws IOException {
    String translation = translateSourceFile(
        "public class Test { void test(int[] array) { " +
        "int i = 2;" +
        "array[0] >>>= 2; " +
        "array[i - 1] >>>= 3; " +
        "array[1] >>= 4;" +
        "array[2] <<= 5;}}",
        "Test", "Test.m");
    assertTranslation(translation,
        "URShiftAssignInt(&(*IOSIntArray_GetRef(nil_chk(array), 0)), 2)");
    assertTranslation(translation, "URShiftAssignInt(&(*IOSIntArray_GetRef(array, i - 1)), 3)");
    assertTranslation(translation, "(*IOSIntArray_GetRef(array, 1)) >>= 4");
    assertTranslation(translation, "(*IOSIntArray_GetRef(array, 2)) <<= 5");
  }

  public void testAssertWithoutDescription() throws IOException {
    String translation = translateSourceFile(
        "public class Test { void test() {\n" +
        "int a = 5;\nint b = 6;\nassert a < b;\n}\n}\n",
        "Test", "Test.m");
    assertTranslation(translation,
        "NSAssert(a < b, @\"Test.java:4 condition failed: assert a < b;\")");
  }

  public void testAssertWithDescription() throws IOException {
    String translation = translateSourceFile(
        "public class Test { void test() { " +
        "int a = 5; int b = 6; assert a < b : \"a should be lower than b\";}}",
        "Test", "Test.m");
    assertTranslation(translation,
      "NSAssert(a < b, @\"a should be lower than b\")");
  }

  public void testAssertWithDynamicDescription() throws IOException {
    String translation = translateSourceFile(
        "public class Test { void test() { " +
        "int a = 5; int b = 6; assert a < b : a + \" should be lower than \" + b;}}",
        "Test", "Test.m");
    assertTranslation(translation,
      "NSAssert(a < b, [[NSString stringWithFormat:@\"%d should be lower than %d\" " +
      "J2OBJC_COMMA() a J2OBJC_COMMA() b] description])");
  }

  // Verify that a Unicode escape sequence is preserved with string
  // concatenation.
  public void testUnicodeStringConcat() throws IOException {
    String translation = translateSourceFile(
        "class Test { static final String NAME = \"\\u4e2d\\u56fd\";" +
        " static final String CAPTION = \"China's name is \";" +
        " static final String TEST = CAPTION + NAME; }", "Test", "Test.m");
    assertTranslation(translation, "Test_TEST_ = @\"China's name is \\u4e2d\\u56fd\"");
  }

  public void testPartialArrayCreation2D() throws IOException {
    String translation = translateSourceFile(
        "class Test { void foo() { char[][] c = new char[3][]; } }", "Test", "Test.m");
    assertTranslation(translation, "#include \"IOSObjectArray.h\"");
    assertTranslation(translation, "#include \"IOSPrimitiveArray.h\"");
    assertTranslation(translation,
        "IOSObjectArray *c = [IOSObjectArray arrayWithLength:3 type:[IOSCharArray iosClass]]");
  }

  public void testPartialArrayCreation3D() throws IOException {
    String translation = translateSourceFile(
        "class Test { void foo() { char[][][] c = new char[3][][]; } }", "Test", "Test.m");
    assertTranslation(translation, "#include \"IOSObjectArray.h\"");
    assertTranslation(translation,
        "IOSObjectArray *c = [IOSObjectArray arrayWithLength:3 type:" +
        "[IOSCharArray iosClassWithDimensions:2]]");
  }

  public void testUnsignedRightShift() throws IOException {
    String translation = translateSourceFile(
        "public class Test { void test(int a, long b, char c, byte d, short e) { " +
        "long r; r = a >>> 1; r = b >>> 2; r = c >>> 3; r = d >>> 4; r = e >>> 5; }}",
        "Test", "Test.m");
    assertTranslation(translation, "r = (int) (((unsigned int) a) >> 1);");
    assertTranslation(translation, "r = (long long) (((unsigned long long) b) >> 2);");
    assertTranslation(translation, "r = c >> 3;");
    assertTranslation(translation, "r = (char) (((unsigned char) d) >> 4);");
    assertTranslation(translation, "r = (short) (((unsigned short) e) >> 5);");
  }

  public void testUnsignedRightShiftAssign() throws IOException {
    String translation = translateSourceFile(
        "public class Test { void test(int a, long b, char c, byte d, short e) { " +
        "a >>>= 1; b >>>= 2; c >>>= 3; d >>>= 4; e >>>= 5; }}",
        "Test", "Test.m");
    assertTranslation(translation, "URShiftAssignInt(&a, 1);");
    assertTranslation(translation, "URShiftAssignLong(&b, 2);");
    assertTranslation(translation, "c >>= 3;");
    assertTranslation(translation, "URShiftAssignByte(&d, 4);");
    assertTranslation(translation, "URShiftAssignShort(&e, 5);");
  }

  public void testUnsignedShiftRightAssignCharArray() throws IOException {
    String translation = translateSourceFile(
        "public class Test { void test(char[] array) { " +
        "array[0] >>>= 2; }}",
        "Test", "Test.m");
    assertTranslation(translation, "(*IOSCharArray_GetRef(nil_chk(array), 0)) >>= 2");
  }

  public void testDoubleQuoteConcatenation() throws IOException {
    String translation = translateSourceFile(
      "public class Test { String test(String s) { return '\"' + s + '\"'; }}",
      "Test", "Test.m");
    assertTranslation(translation, "return [NSString stringWithFormat:@\"\\\"%@\\\"\", s];");
  }

  public void testIntConcatenation() throws IOException {
    String translation = translateSourceFile(
      "public class Test { void check(boolean expr, String fmt, Object... args) {} " +
      "void test(int i, int j) { check(true, \"%d-%d\", i, j); }}",
      "Test", "Test.m");
    assertTranslation(translation,
      "[self checkWithBoolean:YES withNSString:@\"%d-%d\" " +
      "withNSObjectArray:[IOSObjectArray arrayWithObjects:(id[]){ " +
      "[JavaLangInteger valueOfWithInt:i], [JavaLangInteger valueOfWithInt:j] } count:2 " +
      "type:[IOSClass classWithClass:[NSObject class]]]];");
  }

  // Verify that a string == comparison is converted to compare invocation.
  public void testStringComparison() throws IOException {
    String translation = translateSourceFile(
      "public class Test { void check(String s, Object o) { " +
      "boolean b1 = s == null; boolean b2 = \"foo\" == s; boolean b3 = o == \"bar\"; " +
      "boolean b4 = \"baz\" != s; boolean b5 = null != \"abc\"; }}",
      "Test", "Test.m");
    // Assert that non-string compare isn't converted.
    assertTranslation(translation, "BOOL b1 = s == nil;");
    // Assert string equate is converted,
    assertTranslation(translation, "BOOL b2 = [@\"foo\" isEqual:s];");
    // Order is reversed when literal is on the right.
    assertTranslation(translation, "BOOL b3 = [@\"bar\" isEqual:o];");
    // Not equals is converted.
    assertTranslation(translation, "BOOL b4 = ![@\"baz\" isEqual:s];");
    // Comparing null with string literal.
    assertTranslation(translation, "BOOL b5 = ![@\"abc\" isEqual:nil];");
  }

  public void testBinaryLiterals() throws IOException {
    String translation = translateSourceFile(
        "public class A { " +
        "  byte aByte = (byte)0b00100001; short aShort = (short)0b1010000101000101;" +
        "  int anInt1 = 0b10100001010001011010000101000101; " +
        "  int anInt2 = 0b101; int anInt3 = 0B101;" +  // b can be lower or upper case.
        "  long aLong = 0b1010000101000101101000010100010110100001010001011010000101000101L; }",
        "A", "A.m");
    assertTranslation(translation, "aByte_ = (char) 0b00100001;");
    assertTranslation(translation, "aShort_ = (short int) 0b1010000101000101;");
    assertTranslation(translation, "anInt1_ = 0b10100001010001011010000101000101;");
    assertTranslation(translation, "anInt2_ = 0b101;");
    assertTranslation(translation, "anInt3_ = 0B101;");
    assertTranslation(translation,
        "aLong_ = 0b1010000101000101101000010100010110100001010001011010000101000101LL;");
  }

  public void testUnderscoresInNumericLiterals() throws IOException {
    String translation = translateSourceFile(
        "public class A { " +
        "  long creditCardNumber = 1234_5678_9012_3456L; " +
        "  long socialSecurityNumber = 999_99_9999L; " +
        "  float pi =  3.14_15F; " +
        "  long hexBytes = 0xFF_EC_DE_5E; " +
        "  long hexWords = 0xCAFE_BABE; " +
        "  long maxLong = 0x7fff_ffff_ffff_ffffL; " +
        "  byte nybbles = 0b0010_0101; " +
        "  long bytes = 0b11010010_01101001_10010100_10010010; }", "A", "A.m");
    assertTranslation(translation, "creditCardNumber_ = 1234567890123456LL;");
    assertTranslation(translation, "socialSecurityNumber_ = 999999999LL;");
    assertTranslation(translation, "pi_ = 3.1415f;");
    assertTranslation(translation, "hexBytes_ = (int) 0xFFECDE5E;");
    assertTranslation(translation, "hexWords_ = (int) 0xCAFEBABE;");
    assertTranslation(translation, "maxLong_ = (long long) 0x7fffffffffffffffLL;");
    assertTranslation(translation, "nybbles_ = 0b00100101;");
    assertTranslation(translation, "bytes_ = 0b11010010011010011001010010010010;");
  }

  // Verify that the null literal is concatenated as "null" in strings.
  public void testNullConcatenation() throws IOException {
    String translation = translateSourceFile(
      "public class Test { String test(String s) { return \"the nil value is \" + null; }}",
      "Test", "Test.m");
    assertTranslation(translation,
        "return [NSString stringWithFormat:@\"the nil value is %@\", @\"null\"];");
  }

  public void testTypeVariableWithBoundsIsCast() throws IOException {
    String translation = translateSourceFile(
        "class Test<E> { interface A<T> { void foo(); } class B { int foo() { return 1; } } " +
        "<T extends A<? super E>> void test(T t) { t.foo(); } " +
        "<T extends B> void test2(T t) { t.foo(); } }", "Test", "Test.m");
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
        "import java.util.*; public class Test { " +
        "  static class FirstException extends Exception {} " +
        "  static class SecondException extends Exception {} " +
        "  public void rethrowException(String exceptionName) " +
        "      throws FirstException, SecondException { " +
        "    try { " +
        "      if (exceptionName.equals(\"First\")) { throw new FirstException(); } " +
        "      else { throw new SecondException(); }" +
        "    } catch (FirstException|SecondException e) { throw e; }}}",
        "Test", "Test.m");
    assertTranslation(translation, "@catch (Test_FirstException e) {\n    @throw e;\n  }");
    assertTranslation(translation, "@catch (Test_SecondException e) {\n    @throw e;\n  }");
  }

  public void testDifferentTypesInConditionalExpression() throws IOException {
    String translation = translateSourceFile(
        "class Test { String test(Runnable r) { return \"foo\" + (r != null ? r : \"bar\"); } }",
        "Test", "Test.m");
    assertTranslation(translation, "(r != nil ? ((id) r) : @\"bar\")");
  }

  // Verify that when a method invocation returns an object that is ignored,
  // it is cast to (void) to avoid a clang warning when compiling with ARC.
  public void testVoidedUnusedInvocationReturn() throws IOException {
    Options.setMemoryManagementOption(MemoryManagementOption.ARC);
    String translation = translateSourceFile(
        "class Test { void test() {" +
        "  StringBuilder sb = new StringBuilder();" +
        "  sb.append(\"hello, world\");" +
        "  new Throwable(); }}",
        "Test", "Test.m");
    assertTranslation(translation, "(void) [sb appendWithNSString:@\"hello, world\"];");
    assertTranslation(translation, "(void) [[JavaLangThrowable alloc] init]");
  }

  // Verify Java 7's switch statements with strings.
  public void testStringSwitchStatement() throws IOException {
    String translation = translateSourceFile(
        "public class Test { int test(String s) { " +
        "  switch(s) {" +
        "    case \"foo\": return 42;" +
        "    case \"bar\": return 666;" +
        "    default: return -1;" +
        "  }}}",
        "Test", "Test.m");
    assertTranslation(translation, "case 0:\n      return 42;");
    assertTranslation(translation, "case 1:\n      return 666;");
    assertTranslation(translation, "default:\n      return -1;");
    assertTranslation(translation,
        "NSArray *__caseValues = [NSArray arrayWithObjects:@\"foo\", @\"bar\", nil];");
    assertTranslation(translation,
        "NSUInteger __index = [__caseValues indexOfObject:s];");
    assertTranslation(translation, "switch (__index)");
  }

  // Verify Java 7 try-with-resources translation.
  public void testTryWithResourceNoCatchOrFinally() throws IOException {
    // Only runs on Java 7, due to AutoCloseable dependency.
    String javaVersion = System.getProperty("java.version");
    if (javaVersion.startsWith("1.7")) {
      String translation = translateSourceFile(
          "import java.io.*; public class Test { String test(String path) throws IOException { " +
          "  try (BufferedReader br = new BufferedReader(new FileReader(path))) {" +
          "    return br.readLine(); } }}",
          "Test", "Test.m");
      assertTranslation(translation,
          "JavaIoBufferedReader *br = [[[JavaIoBufferedReader alloc] initWithJavaIoReader:" +
          "[[[JavaIoFileReader alloc] initWithNSString:path] autorelease]] autorelease];");
      assertTranslation(translation,
          "@try {\n      return [br readLine];\n    }");
      assertTranslation(translation, "@finally {");
      assertTranslation(translation, "@try {\n        [br close];\n      }");
      assertTranslation(translation, "@catch (JavaLangThrowable *e) {");
      assertTranslation(translation, "if (__mainException) {");
      assertTranslation(translation, "[__mainException addSuppressedWithJavaLangThrowable:e];");
      assertTranslation(translation, "} else {\n          __mainException = e;\n        }");
      assertTranslation(translation,
          "if (__mainException) {\n        @throw __mainException;\n      }");
    }
  }

  public void testGenericResultIsCastForChainedMethodCall() throws IOException {
    String translation = translateSourceFile(
        "abstract class Test<T extends Test.Foo> { " +
        "abstract T getObj(); static class Foo { void foo() { } } " +
        "static void test(Test<Foo> t) { t.getObj().foo(); } }", "Test", "Test.m");
    assertTranslation(translation, "[((Test_Foo *) nil_chk([((Test *) nil_chk(t)) getObj])) foo]");
  }

  public void testCastResultWhenInterfaceDeclaresMoreGenericType() throws IOException {
    String translation = translateSourceFile(
        "class Test { " +
        "interface I1 { A foo(); } " +
        "interface I2 { B foo(); } " +
        "static class A { } static class B extends A { } " +
        "static abstract class C { abstract B foo(); } " +
        "static abstract class D extends C implements I1, I2 { } " +
        "B test(D d) { return d.foo(); } }", "Test", "Test.h");
    // Check that protocols are declared in the same order.
    assertTranslation(translation, "@interface Test_D : Test_C < Test_I1, Test_I2 >");
    translation = getTranslatedFile("Test.m");
    // Check that the result of d.foo() is cast because the compiler will think
    // it returns a Test_A type.
    assertTranslation(translation, "return ((Test_B *) [((Test_D *) nil_chk(d)) foo]);");
  }

  public void testStaticMethodCalledOnObject() throws IOException {
    String translation = translateSourceFile(
        "class Test { static void foo() {} void test(Test t) { t.foo(); } }", "Test", "Test.m");
    assertTranslation(translation, "[Test foo];");
  }

  public void testAnnotationVariableDeclaration() throws IOException {
    String translation = translateSourceFile(
        "public class Test { void test() { " +
        "Deprecated annotation = null; }}",
        "Test", "Test.m");
    assertTranslation(translation, "id<JavaLangDeprecated> annotation = ");
  }

  public void testAnnotationTypeLiteral() throws IOException {
    String translation = translateSourceFile(
        "@Deprecated public class Test { " +
        "  Deprecated deprecated() { " +
        "    return Test.class.getAnnotation(Deprecated.class); }}",
        "Test", "Test.m");
    assertTranslation(translation, "[IOSClass classWithProtocol:@protocol(JavaLangDeprecated)]]");
  }

  public void testEnumThisCallWithNoArguments() throws IOException {
    String translation = translateSourceFile(
        "enum Test { A, B; Test() {} Test(int i) { this(); } }", "Test", "Test.m");
    assertTranslation(translation, "[super initWithNSString:__name withInt:__ordinal]");
    assertOccurrences(translation, "[self initTestEnumWithNSString:__name withInt:__ordinal]", 2);
  }

  public void testForStatementWithMultipleInitializers() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test() { " +
        "for (String s1 = null, s2 = null;;) {}}}", "Test", "Test.m");
    // C requires that each var have its own pointer.
    assertTranslation(translation, "for (NSString *s1 = nil, *s2 = nil; ; )");
  }

  public void testQualifiedSuperMethodInvocation() throws IOException {
    String translation = translateSourceFile(
        "class Test { double foo(int i) { return 1.2; } " +
        "static class Inner extends Test { Runnable test() { return new Runnable() { " +
        "public void run() { Inner.super.foo(1); } }; } } }", "Test", "Test.m");
    assertTranslation(translation,
        "((double (*)(id, SEL, ...))[Test instanceMethodForSelector:@selector(fooWithInt:)])" +
        "(this$0_, @selector(fooWithInt:), 1);");
  }

  // Verify that constant variables are directly referenced when expression is "self".
  public void testSelfStaticVarAccess() throws IOException {
    String translation = translateSourceFile(
        "public class Test { enum Type { TYPE_BOOL; } Type test() { return Type.TYPE_BOOL; }}",
        "Test", "Test.m");
    assertTranslation(translation, "return Test_TypeEnum_get_TYPE_BOOL_();");
  }

  public void testMakeQuotedStringHang() throws IOException {
    // Test hangs if bug makeQuotedString() isn't fixed.
    translateSourceFile(
        "public class Test { void test(String s) { assert !\"null\\foo\\nbar\".equals(s); }}",
        "Test", "Test.m");
  }

  // Verify that the type of superclass field's type variable is cast properly.
  public void testSuperTypeVariable() throws IOException {
    addSourceFile("import java.util.List; class TestList <T extends List> { " +
        "  protected final T testField; TestList(T field) { testField = field; }}",
        "TestList.java");
    addSourceFile("import java.util.ArrayList; class TestArrayList extends TestList<ArrayList> { " +
        "  TestArrayList(ArrayList list) { super(list); }}",
        "TestArrayList.java");
    String translation = translateSourceFile(
        "import java.util.ArrayList; class Test extends TestArrayList { " +
        "  Test(ArrayList list) { super(list); } " +
        "  private class Inner {" +
        "    void test() { testField.ensureCapacity(42); }}}",
        "Test", "Test.m");
    assertTranslation(translation,
        "[((JavaUtilArrayList *) nil_chk(this$0_->testField_)) ensureCapacityWithInt:42];");
  }

  public void testNoTrigraphs() throws IOException {
    String translation = translateSourceFile(
        // C trigraph list from http://en.wikipedia.org/wiki/Digraphs_and_trigraphs#C.
        "class Test { static final String S1 = \"??=??/??'??(??)??!??<??>??-\"; " +
        // S2 has char sequences that start with ?? but aren't trigraphs.
        " static final String S2 = \"??@??$??%??&??*??A??z??1??.\"; }",
        "Test", "Test.m");
    assertTranslation(translation,
        "S1_ = @\"?\" \"?=?\" \"?/?\" \"?'?\" \"?(?\" \"?)?\" \"?!?\" \"?<?\" \"?>?\" \"?-\";");
    assertTranslation(translation, "S2_ = @\"??@??$??%??&??*??A??z??1??.\";");
  }

  // Verify that String.length() and Object.hashCode() return values are cast when used.
  public void testStringLengthCompare() throws IOException {
    String translation = translateSourceFile(
        "public class Test { boolean test(String s) { return -2 < \"1\".length(); }" +
        "  void test2(Object o) { o.hashCode(); }}",
        "Test", "Test.m");
    // Verify referenced return value is cast.
    assertTranslation(translation, "return -2 < ((int) [@\"1\" length]);");
    // Verify unused return value isn't.
    assertTranslation(translation, "[nil_chk(o) hash];");
  }

  public void testDerivedTypeVariableInvocation() throws IOException {
    String translation = translateSourceFile(
        "public class Test {" +
        "  static class Base <T extends BaseFoo> {" +
        "    protected T foo;" +
        "    public Base(T foo) {" +
        "      this.foo = foo;" +
        "    }" +
        "  }" +
        "  static class BaseFoo {" +
        "    void baseMethod() {}" +
        "  }" +
        "  static class Derived extends Base<DerivedFoo> {" +
        "    public Derived(DerivedFoo foo) {" +
        "      super(foo);" +
        "    }" +
        "    void test() {" +
        "      foo.baseMethod();" +
        "      foo.derivedMethod();" +
        "    }" +
        "  }" +
        "  static class DerivedFoo extends BaseFoo {" +
        "    void derivedMethod() {}" +
        "  }" +
        "}", "Test", "Test.m");
    // Verify foo.derivedMethod() has cast of appropriate type variable.
    assertTranslation(translation, "[((Test_DerivedFoo *) foo_) derivedMethod];");
  }
}
