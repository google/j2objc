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

import org.eclipse.jdt.core.dom.Statement;

import java.io.IOException;
import java.util.List;

/**
 * Unit tests for {@link Autoboxer} class.
 *
 * @author Tom Ball
 */
public class AutoboxerTest extends GenerationTest {

  public void testDoNotBoxIntInVarargMethod() throws IOException {
    String source = "public class Test { Test(String s) {} " +
        "int one(String s, int i) { return two(new Test(s), i, 1, 2); }" +
        "int two(Test t, int i, Integer ... args) { return 0; } }";
    String translation = translateSourceFile(source, "Test", "Test.m");

    // i should not be boxed since its argument is explicitly declared,
    // but 1 and 2 should be because they are passed as varargs.
    assertTranslation(translation, "twoWithTest:[[[Test alloc] initWithNSString:s] autorelease] " +
        "withInt:i withJavaLangIntegerArray:" +
        "[IOSObjectArray arrayWithType:[IOSClass classWithClass:[JavaLangInteger class]] count:2,"+
        " [JavaLangInteger valueOfWithInt:1], [JavaLangInteger valueOfWithInt:2] ]];");
  }

  public void testUnboxReturn() throws IOException {
    String source = "public class Test { Integer value; int intValue() { return value; }}";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation, "return [((JavaLangInteger *) NIL_CHK(value_)) intValue];");
  }

  public void testBooleanAssignment() throws IOException {
    String source = "boolean b = true; Boolean foo = Boolean.FALSE; b = foo;";
    List<Statement> stmts = translateStatements(source);
    String result = generateStatement(stmts.get(2));
    assertEquals("b = [((JavaLangBoolean *) NIL_CHK(foo)) booleanValue];", result);

    source = "boolean b = true; Boolean foo = Boolean.FALSE; foo = b;";
    stmts = translateStatements(source);
    result = generateStatement(stmts.get(2));
    assertEquals("foo = [JavaLangBoolean valueOfWithBOOL:b];", result);
  }

  public void testByteAssignment() throws IOException {
    String source = "byte b = 5; Byte foo = Byte.valueOf((byte) 3); b = foo;";
    List<Statement> stmts = translateStatements(source);
    String result = generateStatement(stmts.get(2));
    assertEquals("b = [((JavaLangByte *) NIL_CHK(foo)) byteValue];", result);

    source = "byte b = 5; Byte foo = Byte.valueOf((byte) 3); foo = b;";
    stmts = translateStatements(source);
    result = generateStatement(stmts.get(2));
    assertEquals("foo = [JavaLangByte valueOfWithChar:b];", result);
  }

  public void testCharAssignment() throws IOException {
    String source = "char c = 'a'; Character foo = Character.valueOf('b'); c = foo;";
    List<Statement> stmts = translateStatements(source);
    String result = generateStatement(stmts.get(2));
    assertEquals("c = [((JavaLangCharacter *) NIL_CHK(foo)) charValue];", result);

    source = "char c = 'a'; Character foo = Character.valueOf('b'); foo = c;";
    stmts = translateStatements(source);
    result = generateStatement(stmts.get(2));
    assertEquals("foo = [JavaLangCharacter valueOfWithUnichar:c];", result);
  }

  public void testShortAssignment() throws IOException {
    String source = "short s = 5; Short foo = Short.valueOf((short) 3); s = foo;";
    List<Statement> stmts = translateStatements(source);
    String result = generateStatement(stmts.get(2));
    assertEquals("s = [((JavaLangShort *) NIL_CHK(foo)) shortValue];", result);

    source = "short s = 5; Short foo = Short.valueOf((short) 3); foo = s;";
    stmts = translateStatements(source);
    result = generateStatement(stmts.get(2));
    assertEquals("foo = [JavaLangShort valueOfWithShortInt:s];", result);
  }

  public void testIntAssignment() throws IOException {
    String source = "int i = 5; Integer foo = Integer.valueOf(3); i = foo;";
    List<Statement> stmts = translateStatements(source);
    String result = generateStatement(stmts.get(2));
    assertEquals("i = [((JavaLangInteger *) NIL_CHK(foo)) intValue];", result);

    source = "int i = 5; Integer foo = Integer.valueOf(3); foo = i;";
    stmts = translateStatements(source);
    result = generateStatement(stmts.get(2));
    assertEquals("foo = [JavaLangInteger valueOfWithInt:i];", result);
  }

  public void testLongAssignment() throws IOException {
    String source = "long l = 5; Long foo = Long.valueOf(3L); l = foo;";
    List<Statement> stmts = translateStatements(source);
    String result = generateStatement(stmts.get(2));
    assertEquals("l = [((JavaLangLong *) NIL_CHK(foo)) longLongValue];", result);

    source = "long l = 5; Long foo = Long.valueOf(3L); foo = l;";
    stmts = translateStatements(source);
    result = generateStatement(stmts.get(2));
    assertEquals("foo = [JavaLangLong valueOfWithLongInt:l];", result);
  }

  public void testFloatAssignment() throws IOException {
    String source = "float f = 5.0f; Float foo = Float.valueOf(3.0f); f = foo;";
    List<Statement> stmts = translateStatements(source);
    String result = generateStatement(stmts.get(2));
    assertEquals("f = [((JavaLangFloat *) NIL_CHK(foo)) floatValue];", result);

    source = "float f = 5.0f; Float foo = Float.valueOf(3.0f); foo = f;";
    stmts = translateStatements(source);
    result = generateStatement(stmts.get(2));
    assertEquals("foo = [JavaLangFloat valueOfWithFloat:f];", result);
  }

  public void testDoubleAssignment() throws IOException {
    String source = "double d = 5.0; Double foo = Double.valueOf(3.0); d = foo;";
    List<Statement> stmts = translateStatements(source);
    String result = generateStatement(stmts.get(2));
    assertEquals("d = [((JavaLangDouble *) NIL_CHK(foo)) doubleValue];", result);

    source = "double d = 5.0; Double foo = Double.valueOf(3.0); foo = d;";
    stmts = translateStatements(source);
    result = generateStatement(stmts.get(2));
    assertEquals("foo = [JavaLangDouble valueOfWithDouble:d];", result);
  }

  public void testInfixLeftOperand() throws IOException {
    String source = "Integer test = new Integer(5); int result = test + 3;";
    List<Statement> stmts = translateStatements(source);
    String result = generateStatement(stmts.get(1));
    assertEquals("int result = [((JavaLangInteger *) NIL_CHK(test)) intValue] + 3;", result);
  }

  public void testInfixRightOperand() throws IOException {
    String source = "Integer test = new Integer(5); int result = 3 + test;";
    List<Statement> stmts = translateStatements(source);
    String result = generateStatement(stmts.get(1));
    assertEquals("int result = 3 + [((JavaLangInteger *) NIL_CHK(test)) intValue];", result);
  }

  public void testInfixBothOperands() throws IOException {
    String source = "Integer foo = new Integer(5); Integer bar = new Integer(3);" +
        "int result = foo + bar;";
    List<Statement> stmts = translateStatements(source);
    String result = generateStatement(stmts.get(2));
    assertEquals("int result = [((JavaLangInteger *) NIL_CHK(foo)) intValue] + " +
    	"[((JavaLangInteger *) NIL_CHK(bar)) intValue];", result);
  }

  public void testInfixNeitherOperand() throws IOException {
    // Make sure primitive expressions aren't modified.
    String source = "int result = 3 + 5;";
    List<Statement> stmts = translateStatements(source);
    String result = generateStatement(stmts.get(0));
    assertEquals("int result = 3 + 5;", result);
  }

  public void testVariableDeclaration() throws IOException {
    String source = "Integer test = 3;";
    List<Statement> stmts = translateStatements(source);
    String result = generateStatement(stmts.get(0));
    assertEquals("JavaLangInteger *test = [JavaLangInteger valueOfWithInt:3];", result);
  }

  public void testMethodArgs() throws IOException {
    String source = "public class Test { void foo(Integer i) {} " +
        "void test() { int i = 3; foo(i); }}";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation,
        "[self fooWithJavaLangInteger:[JavaLangInteger valueOfWithInt:i]];");
  }

  public void testConditionalExpression() throws IOException {
    String translation = translateSourceFile(
        "public class Test { " +
        "void test() { Boolean b = true ? false : null; } }",
        "Test", "Test.m");
    assertTranslation(translation, "[JavaLangBoolean valueOfWithBOOL:NO]");
  }

  public void testReturnWithConditional() throws IOException {
    String translation = translateSourceFile(
        "public class Test { " +
        "boolean test() { Boolean b = null; return b != null ? b : false; } }",
        "Test", "Test.m");
    assertTranslation(translation,
        "b != nil ? [((JavaLangBoolean *) NIL_CHK(b)) booleanValue] : NO");
  }

  public void testArrayInitializerNotBoxed() throws IOException {
    // Verify that an array with an initializer that has elements of the same
    // type isn't boxed.
    String translation = translateSourceFile(
      "public class Test { public int values[] = new int[] { 1, 2, 3 }; }",
      "Test", "Test.m");
    assertTranslation(translation, "[IOSIntArray arrayWithInts:(int[]){ 1, 2, 3 } count:3]");
    translation = translateSourceFile(
      "public class Test { private Integer i = 1; private Integer j = 2; private Integer k = 3;" +
      "  public Integer values[] = new Integer[] { i, j, k }; }",
      "Test", "Test.m");
    assertTranslation(translation,
        "[IOSObjectArray arrayWithObjects:(id[]){ i_, j_, k_ } count:3 " +
        "type:[IOSClass classWithClass:[JavaLangInteger class]]]");
  }

  public void testArrayInitializerBoxed() throws IOException {
    // Verify that an Integer array with an initializer that has int elements
    // is boxed.
    String translation = translateSourceFile(
      "public class Test { private Integer i = 1; " +
      "  public Integer values[] = new Integer[] { 1, 2, i }; }",
      "Test", "Test.m");
    assertTranslation(translation,
        "[IOSObjectArray arrayWithObjects:(id[]){ [JavaLangInteger valueOfWithInt:1], " +
        "[JavaLangInteger valueOfWithInt:2], i_ } count:3 " +
        "type:[IOSClass classWithClass:[JavaLangInteger class]]]");
  }

  public void testArrayInitializerUnboxed() throws IOException {
    // Verify that an int array with an initializer that has Integer elements
    // is unboxed.
    String translation = translateSourceFile(
      "public class Test { private Integer i = 1; private Integer j = 2;" +
      "  public int values[] = new int[] { i, j, 3 }; }",
      "Test", "Test.m");
    assertTranslation(translation,
        "[IOSIntArray arrayWithInts:(int[]){ [((JavaLangInteger *) NIL_CHK(i_)) intValue], " +
        "[((JavaLangInteger *) NIL_CHK(j_)) intValue], 3 } count:3]");
  }

  public void testBoxedTypeLiteral() throws IOException {
    String source = "public class Test { Class c = int.class; }";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation,
        "c_ = [[IOSClass classWithClass:[JavaLangInteger class]] retain]");
  }

  public void testBoxedLhsOperatorAssignment() throws IOException {
    String source = "public class Test { Integer i = 1; void foo() { i *= 2; } }";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation,
        "i_ = [[JavaLangInteger valueOfWithInt:[((JavaLangInteger *) NIL_CHK(i_)) intValue] * 2]");
  }

  public void testBoxedEnumConstructorArgs() throws IOException {
    String source = "public enum Test { INT(0), BOOLEAN(false); Test(Object o) {}}";
    String translation = translateSourceFile(source, "Test", "Test.m");

    // i should not be boxed since its argument is explicitly declared,
    // but 1 and 2 should be because they are passed as varargs.
    assertTranslation(translation,
        "[[TestEnum alloc] initWithId:[JavaLangInteger valueOfWithInt:0] " +
        "withNSString:@\"Test_INT\" withInt:0]");
    assertTranslation(translation,
        "[[TestEnum alloc] initWithId:[JavaLangBoolean valueOfWithBOOL:NO] " +
        "withNSString:@\"Test_BOOLEAN\" withInt:1]");
  }
}
