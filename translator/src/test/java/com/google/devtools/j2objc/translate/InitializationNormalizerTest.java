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

import java.io.IOException;

/**
 * Unit tests for {@link InitializationNormalizer} phase.
 *
 * @author Tom Ball
 */
public class InitializationNormalizerTest extends GenerationTest {

  /**
   * Verify that for a constructor that calls another constructor and has
   * other statements, the "this-constructor" statement is used to
   * initialize self, rather than a super constructor call.
   */
  public void testThisConstructorCallInlined() throws IOException {
    String source = "class Test {"
        + "boolean b1; boolean b2;"
        + "Test() { this(true); b2 = true; }"
        + "Test(boolean b) { b1 = b; }}";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslatedLines(translation,
        "void Test_init(Test *self) {",
        "  Test_initWithBoolean_(self, true);",
        "  self->b2_ = true;",
        "}");
  }

  /**
   * Regression test (b/5822974): translation fails with an
   * ArrayIndexOutOfBoundsException in JDT, due to a syntax error in the
   * vertices initializer after initialization normalization.
   * @throws IOException
   */
  public void testFieldArrayInitializer() throws IOException {
    String source = "public class Distance {"
        + "private class SimplexVertex {}"
        + "private class Simplex {"
        + "  public final SimplexVertex vertices[] = {"
        + "    new SimplexVertex() "
        + "  }; }}";
    String translation = translateSourceFile(source, "Distance", "Distance.m");
    assertTranslation(translation,
        "[IOSObjectArray newArrayWithObjects:(id[]){ "
        + "create_Distance_SimplexVertex_initWithDistance_(outer$) } "
        + "count:1 type:Distance_SimplexVertex_class_()]");
  }

  public void testStaticVarInitialization() throws IOException {
    String translation = translateSourceFile(
        "class Test { static java.util.Date date = new java.util.Date(); }", "Test", "Test.m");
    // test that initializer was stripped from the declaration
    assertTranslation(translation, "JavaUtilDate *Test_date;");
    // test that initializer was moved to new initialize method
    assertTranslatedLines(translation,
        "+ (void)initialize {",
        "if (self == [Test class]) {",
        "JreStrongAssignAndConsume(&Test_date, new_JavaUtilDate_init());");
  }

  public void testFieldInitializer() throws IOException {
    String translation = translateSourceFile(
        "class Test { java.util.Date date = new java.util.Date(); }", "Test", "Test.m");
    // Test that a default constructor was created and the initializer statement
    // moved to the constructor.
    assertTranslatedLines(translation,
        "void Test_init(Test *self) {",
        "  NSObject_init(self);",
        "  JreStrongAssignAndConsume(&self->date_, new_JavaUtilDate_init());",
        "}");
  }

  public void testInitializationBlock() throws IOException {
    String translation = translateSourceFile(
        "class Test { java.util.Date date; { date = new java.util.Date(); } }", "Test", "Test.m");
    // Test that a default constructor was created and the initializer statement
    // moved to the constructor.
    assertTranslatedLines(translation,
        "void Test_init(Test *self) {",
        "  NSObject_init(self);",
        "  {",
        "    JreStrongAssignAndConsume(&self->date_, new_JavaUtilDate_init());",
        "  }",
        "}");
  }

  public void testStaticInitializerBlock() throws IOException {
    String translation = translateSourceFile(
        "class Test { static { System.out.println(\"foo\"); } }", "Test", "Test.m");
    // test that a static initialize() method was created and that it contains
    // the block's statement.
    assertTranslatedLines(translation,
        "+ (void)initialize {",
        "if (self == [Test class]) {",
        "{",
        "[((JavaIoPrintStream *) nil_chk(JreLoadStatic(JavaLangSystem, out))) "
            + "printlnWithNSString:@\"foo\"];");
  }

  public void testInitializerMovedToDesignatedConstructor() throws IOException {
    String translation = translateSourceFile(
        "class Test { java.util.Date date; { date = new java.util.Date(); } "
        + "public Test() { this(2); } public Test(int i) { System.out.println(i); } }",
        "Test", "Test.m");
    // test that default constructor was untouched, since it calls self()
    assertTranslatedLines(translation,
        "void Test_init(Test *self) {",
        "  Test_initWithInt_(self, 2);",
        "}");
    // test that initializer statement was added to second constructor
    assertTranslatedLines(translation,
        "void Test_initWithInt_(Test *self, int32_t i) {",
        "  NSObject_init(self);",
        "  {",
        "    JreStrongAssignAndConsume(&self->date_, new_JavaUtilDate_init());",
        "  }",
        "  [((JavaIoPrintStream *) nil_chk(JreLoadStatic(JavaLangSystem, out))) "
          + "printlnWithInt:i];",
        "}");
  }

  public void testInitializerMovedToEmptyConstructor() throws IOException {
    String translation = translateSourceFile(
        "class Test { java.util.Date date = new java.util.Date(); public Test() {} }",
        "Test", "Test.m");
    assertTranslatedLines(translation,
        "void Test_init(Test *self) {",
        "  NSObject_init(self);",
        "  JreStrongAssignAndConsume(&self->date_, new_JavaUtilDate_init());",
        "}");
  }

  /**
   * Regression test (b/5861660): translation fails with an NPE when
   * an interface has a constant defined.
   */
  public void testInterfaceConstantsIgnored() throws IOException {
    String source = "public interface Mouse { int BUTTON_LEFT = 0; }";
    String translation = translateSourceFile(source, "Mouse", "Mouse.h");
    assertTranslation(translation, "#define Mouse_BUTTON_LEFT 0");
  }

  public void testStringWithInvalidCppCharacters() throws IOException {
    String source = "class Test { static final String foo = \"\\udfff\"; }";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation, "NSString *Test_foo;");
    assertTranslation(translation,
        "JreStrongAssign(&Test_foo, [NSString stringWithCharacters:(unichar[]) { "
        + "(int) 0xdfff } length:1]);");
  }

  public void testStringConcatWithInvalidCppCharacters() throws IOException {
    // Include a 1 between the strings so javac's parser doesn't combine them.
    String source = "class Test { static final String foo = \"hello\" + 1 + \"\\udfff\"; }";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation, "NSString *Test_foo;");
    assertTranslation(translation,
        "JreStrongAssign(&Test_foo, JreStrcat(\"$$\", @\"hello1\", "
        + "[NSString stringWithCharacters:(unichar[]) { (int) 0xdfff } length:1]));");
  }

  public void testInitializersPlacedAfterOuterAssignments() throws IOException {
    String source = "class Test { "
         + "  int outerVar = 1; "
         + "  class Inner { int innerVar = outerVar; void test() { outerVar++; } } }";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation, "JreStrongAssign(&self->this$0_, outer$);");
    assertTranslation(translation, "innerVar_ = outer$->outerVar_;");
    assertTrue(translation.indexOf("JreStrongAssign(&self->this$0_, outer$);")
               < translation.indexOf("innerVar_ = outer$->outerVar_;"));
  }

  public void testStaticInitializersKeptInOrder() throws IOException {
    String source =
        "public class Test { "
        + "  public static final int I = 1; "
        + "  public static final java.util.Set<Integer> iSet = new java.util.HashSet<Integer>(); "
        + "  static { iSet.add(I); } "
        + "  public static final int iSetSize = iSet.size(); }";
    String translation = translateSourceFile(source, "Test", "Test.m");
    String setInit = "JreStrongAssignAndConsume(&Test_iSet, new_JavaUtilHashSet_init())";
    String setAdd = "[Test_iSet addWithId:JavaLangInteger_valueOfWithInt_(Test_I)]";
    String setSize = "Test_iSetSize = [Test_iSet size]";
    assertTranslation(translation, setInit);
    assertTranslation(translation, setAdd);
    assertTranslation(translation, setSize);
    assertTrue(translation.indexOf(setInit) < translation.indexOf(setAdd));
    assertTrue(translation.indexOf(setAdd) < translation.indexOf(setSize));
  }

  public void testStaticFinalStringAssignedToStaticFinalString() throws IOException {
    String translation = translateSourceFile(
        "class Test { static final String FOO = Inner.BAR; "
        + "class Inner { static final String BAR = \"bar\"; } }", "Test", "Test.m");
    assertTranslation(translation, "NSString *Test_FOO = @\"bar\";");
  }

  public void testVarargConstructorCallFromSubclass() throws IOException {
    String translation = translateSourceFile(
        "class A { A(Object ... bars) {} static class B extends A {}}",
        "A", "A.m");
    assertNotInTranslation(translation, "A_init(self);");
    assertTranslation(translation, "A_initWithNSObjectArray_(self, "
        + "[IOSObjectArray arrayWithLength:0 type:NSObject_class_()]);");
  }

  /**
   * Verify that the java/lang/Object.java stub file can be translated.
   * InitializationNormalizer.normalizeMethod() threw an NPE because
   * Object's supertype is null.
   */
  public void testTranslateObject() throws IOException {
    // Allow overwriting the system java.lang.Object with our own.
    options.addPlatformModuleSystemOptions("--patch-module", "java.base=" + tempDir);

    // Translate the file in the temp directory (i.e. avoid in-memory copy) because the temp
    // directory is already configured as a patch-module location.
    String filename = "java/lang/Object";
    String path = addSourceFile("package java.lang;"
            + "public class Object {"
            + "  public Object() {}}",
        filename + ".java");
    String translation = translateSourceFileNoInMemory(path, filename + ".h");
    assertTranslation(translation, "@interface NSObject");
  }

  // Regression test for Issue #809.
  public void testConstantInitializedByConditionalOnConstants() throws IOException {
    String translation = translateSourceFile(
        "class Test { private static final int A = 1; private static final int B = 2; "
        + "private static final int C = A < B ? A : B; }", "Test", "Test.m");
    assertTranslation(translation, "#define Test_C 1");
  }

  // Verify that code in a double-brace is added to initialize method.
  public void testDoubleBraceInitialization() throws IOException {
    String translation = translateSourceFile(
        "import java.util.*; class Test { static final Map<String, String> test = "
        + "new HashMap<String, String>() {{ put(\"123\", \"123\"); }}; }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "void Test_1_init(Test_1 *self) {",
        "JavaUtilHashMap_init(self);",
        "{",
        "[self putWithId:@\"123\" withId:@\"123\"];",
        "}",
        "}");
  }

  public void testConstantStringInitializationOrder() throws IOException {
    String translation = translateSourceFile(
        "class Test { private static final int CODE_POINT = getCodePoint(); "
        + "private static final String FOO = \"\\uda6e\"; "
        + "static int getCodePoint() { return FOO.codePointAt(0); } }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "+ (void)initialize {",
        "  if (self == [Test class]) {",
        // Initialization of the constant FOO must come before initialization of non-constants.
        "    JreStrongAssign(&Test_FOO, [NSString stringWithCharacters:(unichar[]) { "
          + "(int) 0xda6e } length:1]);",
        "    Test_CODE_POINT = Test_getCodePoint();");
  }

  // Verify that a static variable initialized as null doesn't do that in the
  // initialize method, since static variables are always zeroed in C.
  public void testNullInitializationIsStripped() throws IOException {
    String translation = translateSourceFile(
        "class test { private static Object CLASS_LOCK = null; }", "Test", "Test.m");

    // Is the null initializer still in the field declaration?
    assertNotInTranslation(translation, "static id test_CLASS_LOCK = nil;");

    // Was it moved to class initialization?
    assertNotInTranslation(translation, "JreStrongAssign(&test_CLASS_LOCK, nil);");
  }
}
