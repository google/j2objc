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
import com.google.devtools.j2objc.ast.Statement;

import java.io.IOException;
import java.util.List;

/**
 * Unit tests for {@link Autoboxer} class.
 *
 * @author Tom Ball
 */
public class AutoboxerTest extends GenerationTest {

  public void testDoNotBoxIntInVarargMethod() throws IOException {
    String source = "public class Test { Test(String s) {} "
        + "int one(String s, int i) { return two(new Test(s), i, 1, 2); }"
        + "int two(Test t, int i, Integer ... args) { return 0; } }";
    String translation = translateSourceFile(source, "Test", "Test.m");

    // i should not be boxed since its argument is explicitly declared,
    // but 1 and 2 should be because they are passed as varargs.
    assertTranslation(translation, "twoWithTest:[new_Test_initWithNSString_(s) autorelease] "
        + "withInt:i withJavaLangIntegerArray:"
        + "[IOSObjectArray arrayWithObjects:(id[]){ JavaLangInteger_valueOfWithInt_(1), "
        + "JavaLangInteger_valueOfWithInt_(2) } count:2 type:JavaLangInteger_class_()]];");
  }

  public void testUnboxReturn() throws IOException {
    String source = "public class Test { Integer value; int intValue() { return value; }}";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation, "return [((JavaLangInteger *) nil_chk(value_)) intValue];");
  }

  public void testBooleanAssignment() throws IOException {
    String source = "boolean b = true; Boolean foo = Boolean.FALSE; b = foo;";
    List<Statement> stmts = translateStatements(source);
    String result = generateStatement(stmts.get(2));
    assertEquals("b = [((JavaLangBoolean *) nil_chk(foo)) booleanValue];", result);

    source = "boolean b = true; Boolean foo = Boolean.FALSE; foo = b;";
    stmts = translateStatements(source);
    result = generateStatement(stmts.get(2));
    assertEquals("foo = JavaLangBoolean_valueOfWithBoolean_(b);", result);
  }

  public void testByteAssignment() throws IOException {
    String source = "byte b = 5; Byte foo = Byte.valueOf((byte) 3); b = foo;";
    List<Statement> stmts = translateStatements(source);
    String result = generateStatement(stmts.get(2));
    assertEquals("b = [foo charValue];", result);

    source = "byte b = 5; Byte foo = Byte.valueOf((byte) 3); foo = b;";
    stmts = translateStatements(source);
    result = generateStatement(stmts.get(2));
    assertEquals("foo = JavaLangByte_valueOfWithByte_(b);", result);
  }

  public void testCharAssignment() throws IOException {
    String source = "char c = 'a'; Character foo = Character.valueOf('b'); c = foo;";
    List<Statement> stmts = translateStatements(source);
    String result = generateStatement(stmts.get(2));
    assertEquals("c = [foo charValue];", result);

    source = "char c = 'a'; Character foo = Character.valueOf('b'); foo = c;";
    stmts = translateStatements(source);
    result = generateStatement(stmts.get(2));
    assertEquals("foo = JavaLangCharacter_valueOfWithChar_(c);", result);
  }

  public void testShortAssignment() throws IOException {
    String source = "short s = 5; Short foo = Short.valueOf((short) 3); s = foo;";
    List<Statement> stmts = translateStatements(source);
    String result = generateStatement(stmts.get(2));
    assertEquals("s = [foo shortValue];", result);

    source = "short s = 5; Short foo = Short.valueOf((short) 3); foo = s;";
    stmts = translateStatements(source);
    result = generateStatement(stmts.get(2));
    assertEquals("foo = JavaLangShort_valueOfWithShort_(s);", result);
  }

  public void testIntAssignment() throws IOException {
    String source = "int i = 5; Integer foo = Integer.valueOf(3); i = foo;";
    List<Statement> stmts = translateStatements(source);
    String result = generateStatement(stmts.get(2));
    assertEquals("i = [foo intValue];", result);

    source = "int i = 5; Integer foo = Integer.valueOf(3); foo = i;";
    stmts = translateStatements(source);
    result = generateStatement(stmts.get(2));
    assertEquals("foo = JavaLangInteger_valueOfWithInt_(i);", result);
  }

  public void testLongAssignment() throws IOException {
    String source = "long l = 5; Long foo = Long.valueOf(3L); l = foo;";
    List<Statement> stmts = translateStatements(source);
    String result = generateStatement(stmts.get(2));
    assertEquals("l = [foo longLongValue];", result);

    source = "long l = 5; Long foo = Long.valueOf(3L); foo = l;";
    stmts = translateStatements(source);
    result = generateStatement(stmts.get(2));
    assertEquals("foo = JavaLangLong_valueOfWithLong_(l);", result);
  }

  public void testFloatAssignment() throws IOException {
    String source = "float f = 5.0f; Float foo = Float.valueOf(3.0f); f = foo;";
    List<Statement> stmts = translateStatements(source);
    String result = generateStatement(stmts.get(2));
    assertEquals("f = [foo floatValue];", result);

    source = "float f = 5.0f; Float foo = Float.valueOf(3.0f); foo = f;";
    stmts = translateStatements(source);
    result = generateStatement(stmts.get(2));
    assertEquals("foo = JavaLangFloat_valueOfWithFloat_(f);", result);
  }

  public void testDoubleAssignment() throws IOException {
    String source = "double d = 5.0; Double foo = Double.valueOf(3.0); d = foo;";
    List<Statement> stmts = translateStatements(source);
    String result = generateStatement(stmts.get(2));
    assertEquals("d = [foo doubleValue];", result);

    source = "double d = 5.0; Double foo = Double.valueOf(3.0); foo = d;";
    stmts = translateStatements(source);
    result = generateStatement(stmts.get(2));
    assertEquals("foo = JavaLangDouble_valueOfWithDouble_(d);", result);
  }

  public void testInfixLeftOperand() throws IOException {
    String source = "Integer test = new Integer(5); int result = test + 3;";
    List<Statement> stmts = translateStatements(source);
    String result = generateStatement(stmts.get(1));
    assertEquals("jint result = [test intValue] + 3;", result);
  }

  public void testInfixRightOperand() throws IOException {
    String source = "Integer test = new Integer(5); int result = 3 + test;";
    List<Statement> stmts = translateStatements(source);
    String result = generateStatement(stmts.get(1));
    assertEquals("jint result = 3 + [test intValue];", result);
  }

  public void testInfixBothOperands() throws IOException {
    String source = "Integer foo = new Integer(5); Integer bar = new Integer(3);"
        + "int result = foo + bar;";
    List<Statement> stmts = translateStatements(source);
    String result = generateStatement(stmts.get(2));
    assertEquals("jint result = [foo intValue] + [bar intValue];", result);
  }

  public void testInfixNeitherOperand() throws IOException {
    // Make sure primitive expressions aren't modified.
    String source = "int result = 3 + 5;";
    List<Statement> stmts = translateStatements(source);
    String result = generateStatement(stmts.get(0));
    assertEquals("jint result = 3 + 5;", result);
  }

  public void testVariableDeclaration() throws IOException {
    String source = "Integer test = 3;";
    List<Statement> stmts = translateStatements(source);
    String result = generateStatement(stmts.get(0));
    assertEquals("JavaLangInteger *test = JavaLangInteger_valueOfWithInt_(3);", result);
  }

  public void testMethodArgs() throws IOException {
    String source = "public class Test { void foo(Integer i) {} "
        + "void test() { int i = 3; foo(i); }}";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation,
        "[self fooWithJavaLangInteger:JavaLangInteger_valueOfWithInt_(i)];");
  }

  public void testConditionalExpression() throws IOException {
    String translation = translateSourceFile(
        "public class Test { "
        + "void test() { Boolean b = true ? false : null; } }",
        "Test", "Test.m");
    assertTranslation(translation, "JavaLangBoolean_valueOfWithBoolean_(NO)");
  }

  public void testReturnWithConditional() throws IOException {
    String translation = translateSourceFile(
        "public class Test { "
        + "boolean test() { Boolean b = null; return b != null ? b : false; } }",
        "Test", "Test.m");
    assertTranslation(translation, "b != nil ? [b booleanValue] : NO");
  }

  public void testConditionalOnBoxedValue() throws IOException {
    String translation = translateSourceFile(
        "public class Test { int test(Boolean b) { return b ? 1 : 2; } }", "Test", "Test.m");
    assertTranslation(translation,
        "return [((JavaLangBoolean *) nil_chk(b)) booleanValue] ? 1 : 2;");
  }

  public void testArrayInitializerNotBoxed() throws IOException {
    // Verify that an array with an initializer that has elements of the same
    // type isn't boxed.
    String translation = translateSourceFile(
        "public class Test { public int values[] = new int[] { 1, 2, 3 }; }",
        "Test", "Test.m");
    assertTranslation(translation, "[IOSIntArray newArrayWithInts:(jint[]){ 1, 2, 3 } count:3]");
    translation = translateSourceFile(
        "public class Test { private Integer i = 1; private Integer j = 2; private Integer k = 3;"
        + "  public Integer values[] = new Integer[] { i, j, k }; }",
        "Test", "Test.m");
    assertTranslation(translation,
        "[IOSObjectArray newArrayWithObjects:(id[]){ self->i_, self->j_, self->k_ } count:3 "
        + "type:JavaLangInteger_class_()]");
  }

  public void testArrayInitializerBoxed() throws IOException {
    // Verify that an Integer array with an initializer that has int elements
    // is boxed.
    String translation = translateSourceFile(
        "public class Test { private Integer i = 1; "
        + "  public void test() { Integer values[] = new Integer[] { 1, 2, i }; }}",
        "Test", "Test.m");
    assertTranslation(translation,
        "[IOSObjectArray arrayWithObjects:(id[]){ JavaLangInteger_valueOfWithInt_(1), "
        + "JavaLangInteger_valueOfWithInt_(2), i_ } count:3 type:JavaLangInteger_class_()]");
  }

  public void testArrayInitializerUnboxed() throws IOException {
    // Verify that an int array with an initializer that has Integer elements
    // is unboxed.
    String translation = translateSourceFile(
        "public class Test { private Integer i = 1; private Integer j = 2;"
        + "  public void test() { int values[] = new int[] { i, j, 3 }; }}",
        "Test", "Test.m");
    assertTranslation(translation,
        "[IOSIntArray arrayWithInts:(jint[]){ [((JavaLangInteger *) nil_chk(i_)) intValue], "
        + "[((JavaLangInteger *) nil_chk(j_)) intValue], 3 } count:3]");
  }

  public void testFieldArrayInitializerBoxed() throws IOException {
    // Verify that an Integer array with an initializer that has int elements
    // is boxed.
    String translation = translateSourceFile(
        "public class Test { private Integer i = 1; "
        + "  public Integer values[] = new Integer[] { 1, 2, i }; }",
        "Test", "Test.m");
    assertTranslation(translation,
        "[IOSObjectArray newArrayWithObjects:(id[]){ JavaLangInteger_valueOfWithInt_(1), "
        + "JavaLangInteger_valueOfWithInt_(2), self->i_ } count:3 type:JavaLangInteger_class_()]");
  }

  public void testFieldArrayInitializerUnboxed() throws IOException {
    // Verify that an int array with an initializer that has Integer elements
    // is unboxed.
    String translation = translateSourceFile(
        "public class Test { private Integer i = 1; private Integer j = 2;"
        + "  public int values[] = new int[] { i, j, 3 }; }",
        "Test", "Test.m");
    assertTranslation(translation,
        "[IOSIntArray newArrayWithInts:(jint[]){ "
        + "[self->i_ intValue], [self->j_ intValue], 3 } count:3]");
  }

  public void testBoxedTypeLiteral() throws IOException {
    String source = "public class Test { Class c = int.class; }";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation, "Test_set_c_(self, [IOSClass intClass]);");
  }

  public void testBoxedLhsOperatorAssignment() throws IOException {
    String source = "public class Test { Integer i = 1; void foo() { i *= 2; } }";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation, "BoxedTimesAssignStrongInt(&i_, 2);");
  }

  public void testBoxedEnumConstructorArgs() throws IOException {
    String source = "public enum Test { INT(0), BOOLEAN(false); Test(Object o) {}}";
    String translation = translateSourceFile(source, "Test", "Test.m");

    assertTranslation(translation,
        "new_TestEnum_initWithId_withNSString_withInt_("
        + "JavaLangInteger_valueOfWithInt_(0), @\"INT\", 0)");
    assertTranslation(translation,
        "new_TestEnum_initWithId_withNSString_withInt_("
        + "JavaLangBoolean_valueOfWithBoolean_(NO), @\"BOOLEAN\", 1)");
  }

  public void testBoxedBoolInIf() throws IOException {
    String source = "public class Test { Boolean b = false; void foo() { if (b) foo(); } }";
    String translation = translateSourceFile(source, "Test", "Test.m");

    assertTranslation(translation, "if ([((JavaLangBoolean *) nil_chk(b_)) booleanValue])");
  }

  public void testBoxedBoolInWhile() throws IOException {
    String source = "public class Test { Boolean b = false; void foo() { while (b) foo(); } }";
    String translation = translateSourceFile(source, "Test", "Test.m");

    assertTranslation(translation, "while ([((JavaLangBoolean *) nil_chk(b_)) booleanValue])");
  }

  public void testBoxedBoolInDoWhile() throws IOException {
    String source = "public class Test { "
        + "  Boolean b = false; void foo() { do { foo(); } while (b); } }";
    String translation = translateSourceFile(source, "Test", "Test.m");

    assertTranslation(translation, "while ([((JavaLangBoolean *) nil_chk(b_)) booleanValue])");
  }

  public void testBoxedBoolNegatedInWhile() throws IOException {
    String source = "public class Test { Boolean b = false; void foo() { while (!b) foo(); } }";
    String translation = translateSourceFile(source, "Test", "Test.m");

    assertTranslation(translation, "while (![((JavaLangBoolean *) nil_chk(b_)) booleanValue])");
  }

  public void testAutoboxCast() throws IOException {
    String source = "public class Test { double doubleValue; "
        + "public int hashCode() { return ((Double) doubleValue).hashCode(); } }";
    String translation = translateSourceFile(source, "Test", "Test.m");

    assertTranslation(translation, "[JavaLangDouble_valueOfWithDouble_(doubleValue_) hash]");
  }

  public void testAutoboxArrayIndex() throws IOException {
    String source =
        "public class Test { "
        + "  Integer index() { return 1; }"
        + "  void test() {"
        + "    int[] array = new int[3];"
        + "    array[index()] = 2; }}";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation,
        "*IOSIntArray_GetRef(array, [((JavaLangInteger *) nil_chk([self index])) intValue]) = 2;");
  }

  public void testPrefixExpression() throws IOException {
    String source =
        "public class Test { void test() { "
        + "  Integer iMinutes = new Integer(1); "
        + "  Double iSeconds = new Double(0);"
        + "  iMinutes = -iMinutes; iSeconds = -iSeconds; }}";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation,
        "iMinutes = JavaLangInteger_valueOfWithInt_(-[iMinutes intValue]);");
    assertTranslation(translation,
        "iSeconds = JavaLangDouble_valueOfWithDouble_(-[iSeconds doubleValue]);");
  }

  public void testStringConcatenation() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test() { Boolean b = Boolean.TRUE; Integer i = new Integer(3); "
        + "String s = b + \"foo\" + i; } }", "Test", "Test.m");
    assertTranslation(translation, "NSString *s = JreStrcat(\"@$@\", b, @\"foo\", i)");
  }

  public void testExtendedOperandsAreUnboxed() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test() { Integer i1 = new Integer(2); Integer i2 = new Integer(3); "
        + "int i3 = 1 + 2 + i1 + i2; } }", "Test", "Test.m");
    assertTranslation(translation, "int i3 = 1 + 2 + [i1 intValue] + [i2 intValue]");
  }

  public void testUnboxOfSwitchStatementExpression() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test() {"
        + " Integer i = 3;"
        + " switch (i) { case 1: case 2: case 3: } } }", "Test", "Test.m");
    assertTranslation(translation, "switch ([i intValue]) {");
  }

  public void testInvokeSuperMethodAutoboxing() throws IOException {
    String translation = translateSourceFile("class Base { "
        + "public void print(Object o) { System.out.println(o); }}"
        + "public class Test extends Base {"
        + "@Override public void print(Object o) { super.print(123.456f); }}", "Test", "Test.m");
    assertTranslation(translation,
        "[super printWithId:JavaLangFloat_valueOfWithFloat_(123.456f)];");
  }

  public void testAssignIntLiteralToNonIntBoxedType() throws Exception {
    String translation = translateSourceFile(
        "class Test { void test() { Byte b = 3; Short s; s = 4; } }", "Test", "Test.m");
    assertTranslation(translation, "JavaLangByte *b = JavaLangByte_valueOfWithByte_(3);");
    assertTranslation(translation, "s = JavaLangShort_valueOfWithShort_(4);");
  }

  public void testBoxedIncrementAndDecrement() throws Exception {
    String translation = translateSourceFile(
        "class Test { void test() { Integer i = 1; i++; Byte b = 2; b--; Character c = 'a'; ++c; "
        + "Double d = 3.0; --d; } }", "Test", "Test.m");
    assertTranslation(translation, "PostIncrInt(&i);");
    assertTranslation(translation, "PostDecrByte(&b);");
    assertTranslation(translation, "PreIncrChar(&c);");
    assertTranslation(translation, "PreDecrDouble(&d);");
  }

  // Verify that passing a new Double to a method that takes a double is unboxed.
  public void testUnboxedDoubleParameter() throws Exception {
    String translation = translateSourceFile(
        "class Test { void takesDouble(double d) {} void test() { takesDouble(new Double(1.2)); }}",
        "Test", "Test.m");
    assertTranslation(translation,
        "[self takesDoubleWithDouble:[((JavaLangDouble *) [new_JavaLangDouble_initWithDouble_(1.2) "
        + "autorelease]) doubleValue]];");
  }

  public void testWildcardBoxType() throws IOException {
    String translation = translateSourceFile(
        "class Test { interface Entry<T> { T getValue(); } "
        + "void test(Entry<? extends Long> entry) { long l = entry.getValue(); } }",
        "Test", "Test.m");
    assertTranslation(translation,
        "jlong l = [((JavaLangLong *) nil_chk([((id<Test_Entry>) nil_chk(entry_)) "
        + "getValue])) longLongValue];");
  }

  public void testAssignmentWithCase() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test() { Integer i; i = (Integer) 12; } }", "Test", "Test.m");
    assertTranslation(translation, "i = JavaLangInteger_valueOfWithInt_(12);");
  }

  public void testAssertMessage() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test(int i) { assert i == 0 : i; }}", "Test", "Test.m");
    assertTranslation(translation,
        "NSAssert(i == 0, [JavaLangInteger_valueOfWithInt_(i) description]);");
  }

  public void testNonWrapperObjectTypeCastToPrimitive() throws IOException {
    String translation = translateSourceFile(
        "class Test { int test(Object o) { return (int) o; } "
        + "int test2(Integer i) { return (int) i; } }", "Test", "Test.m");
    assertTranslation(translation,
        "return [((JavaLangInteger *) nil_chk((JavaLangInteger *) "
        + "check_class_cast(o, [JavaLangInteger class]))) intValue];");
    // Make sure we don't unnecessarily add a cast check if the object type
    // matches the primitive cast type.
    assertTranslation(translation,
        "return [((JavaLangInteger *) nil_chk(i)) intValue];");
  }

  public void testBoxedOperators() throws IOException {
    // TODO(kstanger): Fix compound assign on array access.
    String translation = translateSourceFile(
        "class Test { Integer si; Long sl; Float sf; Double sd;"
        + " Integer[] ai; Long[] al; Float[] af; Double ad;"
        + " void test(Integer wi, Long wl, Float wf, Double wd) {"
        + " si++; wi++; ++sl; ++wl; sf--; wf--; --sd; --wd;"
        + " si += 5; wi += 5; sl &= 6l; wl &= 6l;"
        + " si <<= 2; wi <<= 2; sl >>>= 3; wl >>>= 3;"
        + " ai[0]++; --al[1]; } }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "BoxedPostIncrStrongInt(&si_);",
        "BoxedPostIncrInt(&wi);",
        "BoxedPreIncrStrongLong(&sl_);",
        "BoxedPreIncrLong(&wl);",
        "BoxedPostDecrStrongFloat(&sf_);",
        "BoxedPostDecrFloat(&wf);",
        "BoxedPreDecrStrongDouble(&sd_);",
        "BoxedPreDecrDouble(&wd);",
        "BoxedPlusAssignStrongInt(&si_, 5);",
        "BoxedPlusAssignInt(&wi, 5);",
        "BoxedBitAndAssignStrongLong(&sl_, 6l);",
        "BoxedBitAndAssignLong(&wl, 6l);",
        "BoxedLShiftAssignStrongInt(&si_, 2);",
        "BoxedLShiftAssignInt(&wi, 2);",
        "BoxedURShiftAssignStrongLong(&sl_, 3);",
        "BoxedURShiftAssignLong(&wl, 3);",
        "BoxedPostIncrArrayInt(IOSObjectArray_GetRef(nil_chk(ai_), 0));",
        "BoxedPreDecrArrayLong(IOSObjectArray_GetRef(nil_chk(al_), 1));");
  }
}
