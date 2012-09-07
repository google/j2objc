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

import org.eclipse.jdt.core.dom.Statement;

import java.io.IOException;
import java.util.List;

/**
 * Tests for {@link StatementGenerator}.
 *
 * @author Tom Ball
 */
public class StatementGeneratorTest extends GenerationTest {
  // TODO(user): update bug id in comments to public issue numbers when
  // issue tracking is sync'd.

  // Verify that return statements output correctly for reserved words.
  public void testReturnReservedWord() throws IOException {
    String translation = translateSourceFile(
        "public class Test { enum Type { TYPE_BOOL; } Type test() { return Type.TYPE_BOOL; }}",
        "Test", "Test.m");
    assertTranslation(translation, "return [Test_TypeEnum TYPE_BOOL_];");
  }

  // Verify that super.method(), where method is static, sends the
  // class the message, not super.  Objective-C
  public void testStaticSuperInvocation() throws IOException {
    String translation = translateSourceFile(
        "public class A { static class Base { static void test() {} } " +
        "static class Foo extends Base { void test2() { super.test(); } }}", "A", "A.m");
    assertTranslation(translation, "[[super class] test];");
  }

  public void testNilCheckArrayLength() throws IOException {
    String translation = translateSourceFile(
      "public class A {" +
      "  int length(char[] s) { return s.length; } void test() { length(null);}}",
      "A", "A.m");
    assertTranslation(translation, "return (int) [((IOSCharArray *) NIL_CHK(s)) count];");
  }

  // Verify that both a class and interface type invoke getClass() correctly.
  public void testGetClass() throws IOException {
    String translation = translateSourceFile(
      "import java.util.*; public class A {" +
      "  void test(ArrayList one, List two) { " +
      "    Class<?> classOne = one.getClass();" +
      "    Class<?> classTwo = two.getClass(); }}",
      "A", "A.m");
    assertTranslation(translation, "[((JavaUtilArrayList *) NIL_CHK(one)) getClass]");
    assertTranslation(translation, "[(id<JavaObject>) ((id<JavaUtilList>) NIL_CHK(two)) getClass]");
  }

  public void testVariableDeclarationsInSwitchStatement() throws IOException {
    String translation = translateSourceFile(
      "public class A { public void doSomething(int i) { switch (i) { " +
      "case 1: int j = i * 2; log(j); break; " +
      "case 2: log(i); break; " +
      "case 3: log(i); int k = i; break; }}" +
      "private void log(int i) {}}",
      "A", "A.m");
    assertTranslation(translation, "case 1: {\n");
    assertTranslation(translation, "case 2:\n");
    assertTranslation(translation, "case 3: {\n");
  }

  public void testClosingBraceInSwitchStatement() throws IOException {
    String source = "switch (1) { case 1: int i = 1; break; }";
    List<Statement> stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertTranslation(result, "}\n}");
  }

  public void testEnumConstantsInSwitchStatement() throws IOException {
    String translation = translateSourceFile(
      "public class A { static enum B { ONE, TWO }" +
      "public static void doSomething(B b) { switch (b) { case ONE: break; case TWO: break; }}}",
      "A", "A.m");
    assertTranslation(translation, "switch ([b ordinal]) {");
    assertTranslation(translation, "case A_B_ONE:");
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
    assertEquals("JavaLangThrowable *t = (JavaLangThrowable *) e;", result);
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
    assertEquals("[NIL_CHK(o) description];", result);
    result = generateStatement(stmts.get(2));
    assertEquals("[self description];", result);
  }

  public void testSuperToStringRenaming() throws IOException {
    String translation = translateSourceFile(
      "public class Example { public String toString() { return super.toString(); } }",
      "Example", "Example.m");
    assertTranslation(translation, "return (NSString *) [super description];");
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
    assertFalse(translation.contains("static int Example_Bar_FOO_ = 1;"));
    assertTranslation(translation, "+ (int)FOO {");
    assertTranslation(translation, "return Example_Bar_FOO;");
    translation = getTranslatedFile("Example.h");
    assertTranslation(translation, "#define Example_Bar_FOO 1");
  }

  public void testAccessExternalStringConstant() throws IOException {
    String translation = translateSourceFile(
      "public class Example { static class Bar { public static final String FOO=\"Mumble\"; } "
      + "String foo; { foo = Bar.FOO; } }",
      "Example", "Example.m");
    assertTranslation(translation, "foo_ = [[Example_Bar FOO] retain]");
    assertTranslation(translation, "static NSString * Example_Bar_FOO_ = @\"Mumble\";");
    assertTranslation(translation, "+ (NSString *)FOO {");
    assertTranslation(translation, "return Example_Bar_FOO_;");
  }

  public void testMultipleVariableDeclarations() throws IOException {
    String source = "Object one, two;";
    List<Statement> stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("NSObject *one, *two;", result);
  }

  public void testStaticBooleanFields() throws IOException {
    String translation = translateSourceFile(
        "public class Example { Boolean b1 = Boolean.TRUE; Boolean b2 = Boolean.FALSE; }",
        "Example", "Example.m");
    assertTranslation(translation, "b1_ = [[JavaLangBoolean getTRUE] retain]");
    assertTranslation(translation, "b2_ = [[JavaLangBoolean getFALSE] retain]");
  }

  public void testStringConcatenation() throws IOException {
    String translation = translateSourceFile(
      "public class Example<K,V> { void test() { String s = \"hello, \" + \"world\"; }}",
      "Example", "Example.m");
    assertTranslation(translation, "NSString *s = @\"hello, world\"");
  }

  public void testStringConcatenationTypes() throws IOException {
    String translation = translateSourceFile(
      "public class Example<K,V> { Object obj; boolean b; char c; double d; float f; int i; " +
      "long l; short s; String str; public String toString() { " +
      "return \"obj=\" + obj + \" b=\" + b + \" c=\" + c + \" d=\" + d + \" f=\" + f" +
      " + \" i=\" + i + \" l=\" + l + \" s=\" + s; }}",
      "Example", "Example.m");
    assertTranslation(translation,
        "return [NSString stringWithFormat:@\"obj=%@ b=%@ c=%c d=%f f=%f i=%d l=%qi s=%d\", " +
        "obj_, [JavaLangBoolean toStringWithBOOL:b_], c_, d_, f_, i_, l_, s_];");
  }

  public void testStringConcatenationWithLiterals() throws IOException {
    String translation = translateSourceFile(
      "public class Example<K,V> { public String toString() { " +
      "return \"literals: \" + true + \", \" + 'c' + \", \" + 1.0d + \", \" + 3.14 + \", \"" +
      " + 42 + \", \" + 123L + \", \" + 1; }}",
      "Example", "Example.m");
    assertTranslation(translation, "return @\"literals: true, 'c', 1.0d, 3.14, 42, 123L, 1\";");
  }

  public void testStringConcatenationEscaping() throws IOException {
    String translation = translateSourceFile(
      "public class Example<K,V> { String s = \"hello, \" + 50 + \"% of the world\\n\"; }",
      "Example", "Example.m");
    //TODO(user): should copy the string below, not retain it.
    assertTranslation(translation, "s_ = [@\"hello, 50% of the world\\n\" retain]");
  }

  public void testVarargsMethodInvocation() throws IOException {
    String translation = translateSourceFile("public class Example { "
      + "public void call() { foo(null); bar(\"\", null, null); }"
      + "void foo(Object ... args) { }"
      + "void bar(String firstArg, Object ... varArgs) { }}",
      "Example", "Example.m");
    assertTranslation(translation, "[self fooWithNSObjectArray:");
    assertTranslation(translation,
        "[IOSObjectArray arrayWithType:[IOSClass classWithClass:[NSObject class]] " +
        "count:2, nil, nil ]");
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
        "[IOSObjectArray arrayWithType:[IOSClass classWithClass:[NSObject class]] count:1, " +
        "[JavaLangInteger valueOfWithInt:1] ]]");
  }

  public void testStaticInnerSubclassAccessingOuterStaticVar() throws IOException {
    String translation = translateSourceFile(
      "public class Test { public static final Object FOO = new Object(); " +
      "static class Inner { Object test() { return FOO; }}}",
      "Test", "Test.m");
    assertTranslation(translation, "return [Test FOO];");
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

  public void testInnerInnerClassFieldAccess() throws IOException {
    String translation = translateSourceFile(
      "public class Test { static class One {} static class Two extends Test { " +
      "Integer i; Two(Integer i) { this.i = i; } int getI() { return i.intValue(); }}}",
      "Test", "Test.m");
    assertTranslation(translation, "- (id)initWithJavaLangInteger:(JavaLangInteger *)i {");
    assertTranslation(translation, "return [((JavaLangInteger *) NIL_CHK(i_)) intValue];");
  }

  public void testInnerClassSuperConstructor() throws IOException {
    String translation = translateSourceFile(
      "public class Test { static class One { int i; One(int i) { this.i = i; }} " +
      "static class Two extends One { Two(int i) { super(i); }}}",
      "Test", "Test.m");
    assertTranslation(translation, "- (id)initWithInt:(int)i");
    assertTranslation(translation, "return [super initWithInt:i];");
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
        "[((id<JavaUtilIterator>) NIL_CHK(iterator_)) next]) getKey];");
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
    assertTranslation(translation, "return [((id<JavaUtilIterator>) NIL_CHK(it_)) hasNext];");
    assertTranslation(translation, "return [((id<JavaUtilIterator>) NIL_CHK(it_)) next];");
    assertFalse(translation.contains("Test *this$0;"));
  }

  public void testPrintlnToNSLog() throws IOException {
    String translation = translateSourceFile(
        "public class Hello { public static void main(String[] args) { " +
        "System.out.println(\"Hello, world!\"); }}",
        "Hello", "Hello.m");
    assertTranslation(translation, "NSLog(@\"%@\", @\"Hello, world!\");");
  }

  public void testPrintlnToNSLogInAnonymousClass() throws IOException {
    String translation = translateSourceFile(
        "public class Hello { public static void main(String[] args) { " +
        "Object o = new Object() { public String toString() { return \"Hello, world!\"; }};" +
        "System.out.println(o); }}",
        "Hello", "Hello.m");
    assertTranslation(translation, "NSLog(@\"%@\", o);");
    assertFalse(translation.contains("toString"));
    assertTranslation(translation, "- (NSString *)description");
    translation = getTranslatedFile("Hello.h");
    assertFalse(translation.contains("toString"));
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
        "- (id)initWithJavaUtilCollection:(id<JavaUtilCollection>)outer$0;");
  }

  public void testEnumInEqualsTest() throws IOException {
    String translation = translateSourceFile(
        "public class Test { enum TicTacToe { X, Y } " +
        "boolean isX(TicTacToe ttt) { return ttt == TicTacToe.X; } }",
        "Test", "Test.m");
    assertTranslation(translation, "return ttt == [Test_TicTacToeEnum X];");
  }

  public void testArrayLocalVariable() throws IOException {
    String source = "char[] array = new char[1];";
    List<Statement> stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals(
        "IOSCharArray *array = [[[IOSCharArray alloc] initWithLength:1] autorelease];", result);

    source = "char array[] = new char[1];";
    stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    result = generateStatement(stmts.get(0));
    assertEquals(
        "IOSCharArray *array = [[[IOSCharArray alloc] initWithLength:1] autorelease];", result);
  }

  public void testArrayParameterLengthUse() throws IOException {
    String translation = translateSourceFile(
        "public class Test { void test(char[] foo, char bar[]) { " +
        "sync(foo.length, bar.length); } void sync(int a, int b) {} }",
        "Test", "Test.m");
    assertTranslation(translation,
        "[self syncWithInt:(int) [((IOSCharArray *) NIL_CHK(foo)) count] withInt:(int) " +
        "[((IOSCharArray *) NIL_CHK(bar)) count]];");
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
        "[IOSCharArray arrayWithCharacters:(unichar[]){ '4', '5' } count:2];", result);
  }

  /**
   * Verify that static array initializers are rewritten as method calls.
   */
  public void testStaticArrayInitializer() throws IOException {
    String translation = translateSourceFile(
        "public class Test { static int[] a = { 1, 2, 3 }; static char b[] = { '4', '5' }; }",
        "Test", "Test.m");
    assertTranslation(translation,
        "a_ = [[IOSIntArray arrayWithInts:(int[]){ 1, 2, 3 } count:3] retain];");
    assertTranslation(translation,
        "b_ = [[IOSCharArray arrayWithCharacters:(unichar[]){ '4', '5' } count:2] retain];");
  }

  public void testLocalArrayCreation() throws IOException {
    String translation = translateSourceFile(
      "public class Example { char[] test() { int high = 0xD800, low = 0xDC00; " +
            "return new char[] { (char) high, (char) low }; } }",
      "Example", "Example.m");
    assertTranslation(translation, "return [IOSCharArray " +
        "arrayWithCharacters:(unichar[]){ (unichar) high, (unichar) low } count:2];");
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
    assertTrue(result.contains("{\nint n__"));
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
    assertEquals("IOSClass *mySuperClass = [NIL_CHK(myClass) getSuperclass];", result);
    result = generateStatement(stmts.get(2));
    assertEquals("IOSClass *enumClass = [IOSClass classWithClass:[JavaLangEnum class]];", result);
  }

  public void testCastInConstructorChain() throws IOException {
    String source = "int i = new Object().hashCode();";
    List<Statement> stmts = translateStatements(source);
    assertEquals(1, stmts.size());
    String result = generateStatement(stmts.get(0));
    assertEquals("int i = [((NSObject *) [[[NSObject alloc] init] autorelease]) hash];", result);
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
    assertTranslation(translation, "map_ = [[JavaUtilHashMap alloc] init]");
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
    assertTranslation(translation, "fooWithJavaLangCharacterArray:");
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
    // Verify that super properties aren't referenced as "super.self.i".
    String translation = translateSourceFile(
      "public class A { int i; class B extends A { int test() { return super.i; }}}",
      "A", "A.m");
    assertTranslation(translation, "super.i");
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

  public void testCastGenericReturnType() throws IOException {
    String translation = translateSourceFile(
      "class Test { " +
      "  static class A<E extends A> { E other; public E getOther() { return other; } } " +
      "  static class B extends A<B> { B other = getOther(); } }",
      "Test", "Test.h");
    // Test_B's "other" needs a trailing underscore, since there is an "other"
    // field in its superclass.
    assertTranslation(translation, "@property (nonatomic, retain) Test_B *other_;");
    translation = getTranslatedFile("Test.m");
    assertTranslation(translation, "other__ = [((Test_B *) [self getOther]) retain]");
  }

  public void testArrayInstanceOfTranslation() throws IOException {
    String source = "Object args = new String[0]; if (args instanceof String[]) {}";
    List<Statement> stmts = translateStatements(source);
    assertEquals(2, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertEquals(
        "if ([[(IOSArray *) args elementType] isEqual:[NSString class]]) {\n}", result);
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
    assertEquals("[((IOSIntArray *) NIL_CHK(array)) replaceIntAtIndex:offset " +
        "withInt:[array intAtIndex:offset] + 23];", result);
  }

  public void testFPModuloOperator() throws IOException {
    String source = "float a = 4.2f; a %= 2.1f;";
    List<Statement> stmts = translateStatements(source);
    assertEquals(2, stmts.size());
    String result = generateStatement(stmts.get(1));
    assertEquals("a = fmod(a, 2.1f);", result);
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
    assertTranslation(translation, "[Example setTodayWithJavaUtilDate:now];");
  }

  // b/5872533: reserved method name not renamed correctly in super invocation.
  public void testSuperReservedName() throws IOException {
    addSourceFile("public class A { A() {} public void init(int a) { }}", "A.java");
    addSourceFile(
        "public class B extends A { B() {} public void init(int b) { super.init(b); }}", "B.java");
    String translation = translateSourceFile("A", "A.h");
    assertTranslation(translation, "- (id)init;");
    assertTranslation(translation, "- (void)init__WithInt:(int)a;");
    translation = translateSourceFile("B", "B.m");
    assertTranslation(translation, "return (self = [super init]);");
    assertTranslation(translation, "[super init__WithInt:b];");
  }

  // b/5872710: generic return type needs to be cast if chaining invocations.
  public void testTypeVariableCast() throws IOException {
    String translation = translateSourceFile(
      "import java.util.ArrayList; public class Test {" +
      "  int length; static ArrayList<String> strings = new ArrayList<String>();" +
      "  public static void main(String[] args) { int n = strings.get(1).length(); }}",
      "Test", "Test.m");
    assertTranslation(translation, "[((NSString *) " +
      "[((JavaUtilArrayList *) NIL_CHK([Test strings])) getWithInt:1]) length];");
  }

  // b/5872667: verify asserts in main() methods use NSCAssert.
  public void testAssertInMain() throws IOException {
    String translation = translateSourceFile(
        "public class Test { public static void main(String[] args) {" +
        "  assert (false) : \"false!!\"; }}",
        "Test", "Test.m");
    assertTranslation(translation, "NSCAssert((NO), @\"false!!\");");
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
        "[(IOSObjectArray *) [((IOSObjectArray *) NIL_CHK([Test a])) objectAtIndex:0] " +
        "replaceObjectAtIndex:0 withObject:@\"42\"];");
    assertTranslation(translation,
        "[(IOSObjectArray *) [((IOSObjectArray *) NIL_CHK([Test a])) objectAtIndex:0] count]");
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
        "type:[IOSClass classWithClass:[IOSIntArray class]]];");
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
        "type:[IOSClass classWithClass:[IOSObjectArray class]]];");
  }

  public void testVarargsMethodInvocationZeroLengthArray() throws IOException {
    String translation = translateSourceFile(
        "public class Example { " +
        " public void call() { foo(new Object[0]); bar(new Object[0]); } " +
        " public void foo(Object ... args) { } " +
        " public void bar(Object[] ... args) { } }",
        "Example", "Example.h");
    assertTranslation(translation, "- (void)fooWithNSObjectArray:(IOSObjectArray *)args");
    assertTranslation(translation, "- (void)barWithNSObjectArray:(IOSObjectArray *)args");
    translation = getTranslatedFile("Example.m");

    // Should be equivalent to foo(new Object[0]).
    assertTranslation(translation,
        "[self fooWithNSObjectArray:[[[IOSObjectArray alloc] " +
        "initWithLength:0 type:[IOSClass classWithClass:[NSObject class]]] autorelease]]");

    // Should be equivalent to bar(new Object[] { new Object[0] }).
    assertTranslation(translation,
        "[self barWithNSObjectArray:[IOSObjectArray arrayWithType:[IOSClass " +
        "classWithClass:[NSObject class]] count:1, [[[IOSObjectArray alloc] " +
        "initWithLength:0 type:[IOSClass classWithClass:[NSObject class]]] autorelease] ]]");
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
        "[IOSObjectArray arrayWithType:[IOSClass classWithClass:[IOSClass class]] count:0 ]];");
    assertTranslation(translation,
        "c2 = [[IOSClass classWithClass:[Test class]] getConstructor:" +
        "[IOSObjectArray arrayWithType:[IOSClass classWithClass:[IOSClass class]] count:1, " +
        "[IOSClass classWithClass:[NSString class]] ]];");
    assertTranslation(translation,
        "c3 = [[IOSClass classWithClass:[Test class]] getConstructor:" +
        "[IOSObjectArray arrayWithType:[IOSClass classWithClass:[IOSClass class]] count:2, " +
        "[IOSClass classWithClass:[NSString class]], [JavaLangByte TYPE] ]];");

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
        "arrayWithType:[IOSClass classWithClass:[IOSClass class]] count:1, " +
        "[IOSClass classWithClass:[NSObject class]] ]];");
  }

  public void testGetVarargsWithLeadingParameterNoArgs() throws IOException {
    String translation = translateSourceFile(
        "public class Test {" +
        "  void test() throws Exception { " +
        "    getClass().getMethod(\"hashCode\", new Class[0]); }}",
        "Test", "Test.m");
    assertTranslation(translation,
        "[[self getClass] getMethod:@\"hashCode\" parameterTypes:[[[IOSObjectArray alloc] " +
        "initWithLength:0 type:[IOSClass classWithClass:[IOSClass class]]] autorelease]];");
  }

  public void testTypeVariableWithBoundCast() throws IOException {
    String translation = translateSourceFile(
      "import java.util.ArrayList; public class Test {" +
      "  public static class Foo<T extends Foo.Bar> {" +
      "    public static class Bar { } " +
      "    public T foo() { return null; } } " +
      "  public static class BarD extends Foo.Bar { } " +
      "  public void bar() { Foo<BarD> f = new Foo<BarD>(); BarD b = f.foo(); } }",
      "Test", "Test.m");
    assertTranslation(translation, "(Test_BarD *) [((Test_Foo *) NIL_CHK(f)) foo]");
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
      assertTranslation(translation, "NSLog(@\"%@\", [HelloWorld staticString]);");
  }

  public void testThisCallInInnerConstructor() throws IOException {
    String translation = translateSourceFile(
        "public class Test {" +
        "  class Inner {" +
        "    public Inner() { }" +
        "    public Inner(int foo) { this(); int i = foo; }}}",
        "Test", "Test.m");
    assertTranslation(translation, "self = [self initWithTest:outer$1]");
  }

  public void testDoubleModulo() throws IOException {
    String translation = translateSourceFile(
      "public class A { " +
      "  double doubleMod(double one, double two) { return one % two; }" +
      "  float floatMod(float three, float four) { return three % four; }}",
      "A", "A.m");
    assertTranslation(translation, "return fmod(one, two);");
    assertTranslation(translation, "return fmodf(three, four);");
  }

  // Verify that an external string can be used in string concatenation,
  // for a parameter to a translated method.
  public void testConcatPublicStaticString() throws IOException {
    String translation = translateSourceFile(
      "class B { public static final String separator = \"/\"; } "  +
      "public class A { String prefix(Object o) { return new String(o + B.separator); }}",
      "A", "A.m");
    assertTranslation(translation,
        "[NSString stringWithString:[NSString stringWithFormat:@\"%@%@\", o, [B separator]]];");
  }

  public void testStringConcatWithBoolean() throws IOException {
    String translation = translateSourceFile(
      "public class A { String test(boolean b) { return \"foo: \" + b; }}",
      "A", "A.m");
    assertTranslation(translation,
        "return [NSString stringWithFormat:@\"foo: %@\", [JavaLangBoolean toStringWithBOOL:b]];");
  }

  public void testStringConcatWithChar() throws IOException {
    String translation = translateSourceFile(
      "public class A { String test(char c) { return \"foo: \" + c; }}",
      "A", "A.m");
    assertTranslation(translation, "return [NSString stringWithFormat:@\"foo: %c\", c];");
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
    // \177 (0x7f) is the highest legal octal value in C99.
    assertTranslation(translation, "s1_ = [@\"\\177\" retain]");

    // \200-\377 (0x80-0xFF) is the illegal octal value range in C99.
    assertFalse(translation.contains("s2_ = [@\"\\200\" retain]"));
    assertFalse(translation.contains("s3_ = [@\"\\377\" retain]"));
    assertTranslation(translation,
        "[NSString stringWithCharacters:(unichar[]) { (int) 0x80 } length:1]");
    assertTranslation(translation,
        "[NSString stringWithCharacters:(unichar[]) { (int) 0xff } length:1]");
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
    assertTranslation(translation, "fooWithUnichar:'\\'']");
    assertTranslation(translation, "fooWithUnichar:'\\\\']");
  }

  public void testStaticVarAccessFromInnerClass() throws IOException {
    String translation = translateSourceFile("public class Test { public static String foo; " +
        "interface Assigner { void assign(String s); } static { " +
        "new Assigner() { public void assign(String s) { foo = s; }}; }}",
        "Test", "Test.m");
    assertTranslation(translation, "[Test setFooWithNSString:s];");
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
        "    NSAutoreleasePool *pool__ = [[NSAutoreleasePool alloc] init];\n" +
        "    {\n    }\n" +
        "    [pool__ release];\n" +
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
    assertTranslation(translation, "NSAutoreleasePool *pool__ = [[NSAutoreleasePool alloc] init]");
    assertTranslation(translation, "[pool__ release]");
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
        "withInt:(int) (((unsigned int) [array intAtIndex:0]) >> 2)");
    assertTranslation(translation,
        "withInt:(int) (((unsigned int) [array intAtIndex:i - 1]) >> 3)");
    assertTranslation(translation, "withInt:[array intAtIndex:1] >> 4");
    assertTranslation(translation, "withInt:[array intAtIndex:2] << 5");
  }
}
