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
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.TypeDeclaration;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.io.IOException;
import java.util.List;

/**
 * Unit tests for {@link InitializationNormalization} phase.
 *
 * @author Tom Ball
 */
public class InitializationNormalizerTest extends GenerationTest {
  // TODO(tball): update bug id in comments to public issue numbers when
  // issue tracking is sync'd.

  InitializationNormalizer instance;

  @Override
  protected void setUp() throws IOException {
    super.setUp();
    instance = new InitializationNormalizer();
  }

  private TypeDeclaration translateClassBody(String testSource) {
    String source = "public class Test { " + testSource + " }";
    CompilationUnit unit = translateType("Test", source);
    List<AbstractTypeDeclaration> types = unit.getTypes();
    assertEquals(1, types.size());
    assertTrue(types.get(0) instanceof TypeDeclaration);
    return (TypeDeclaration) types.get(0);
  }

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
    assertTranslation(translation, "if (self = [self initTestWithBoolean:YES]) {");
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
        "[IOSObjectArray newArrayWithObjects:(id[]){ [[[Distance_SimplexVertex alloc] "
        + "initWithDistance:outer$] autorelease] } count:1 type:Distance_SimplexVertex_class_()]");
  }

  public void testStaticVarInitialization() throws IOException {
    String translation = translateSourceFile(
        "class Test { static java.util.Date date = new java.util.Date(); }", "Test", "Test.m");
    // test that initializer was stripped from the declaration
    assertTranslation(translation, "JavaUtilDate * Test_date_;");
    // test that initializer was moved to new initialize method
    assertTranslatedLines(translation,
        "+ (void)initialize {",
        "if (self == [Test class]) {",
        "JreStrongAssignAndConsume(&Test_date_, nil, [[JavaUtilDate alloc] init]);");
  }

  public void testFieldInitializer() throws IOException {
    String translation = translateSourceFile(
        "class Test { java.util.Date date = new java.util.Date(); }", "Test", "Test.m");
    // Test that a default constructor was created and the initializer statement
    // moved to the constructor.
    assertTranslatedLines(translation,
        "- (instancetype)init {",
        "if (self = [super init]) {",
        "Test_setAndConsume_date_(self, [[JavaUtilDate alloc] init]);",
        "}",
        "return self;",
        "}");
  }

  public void testInitializationBlock() throws IOException {
    String translation = translateSourceFile(
        "class Test { java.util.Date date; { date = new java.util.Date(); } }", "Test", "Test.m");
    // Test that a default constructor was created and the initializer statement
    // moved to the constructor.
    assertTranslatedLines(translation,
        "- (instancetype)init {",
        "if (self = [super init]) {",
        "{",
        "Test_setAndConsume_date_(self, [[JavaUtilDate alloc] init]);",
        "}",
        "}",
        "return self;",
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
        "[((JavaIoPrintStream *) nil_chk(JavaLangSystem_get_out_())) "
            + "printlnWithNSString:@\"foo\"];");
  }

  public void testInitializerMovedToDesignatedConstructor() throws IOException {
    String translation = translateSourceFile(
        "class Test { java.util.Date date; { date = new java.util.Date(); } "
        + "public Test() { this(2); } public Test(int i) { System.out.println(i); } }",
        "Test", "Test.m");
    // test that default constructor was untouched, since it calls self()
    assertTranslatedLines(translation,
        "- (instancetype)init {", "return [self initTestWithInt:2];", "}");
    // test that initializer statement was added to second constructor
    assertTranslatedLines(translation,
        "- (instancetype)initTestWithInt:(jint)i {",
        "if (self = [super init]) {",
        "{",
        "Test_setAndConsume_date_(self, [[JavaUtilDate alloc] init]);",
        "}",
        "[((JavaIoPrintStream *) nil_chk(JavaLangSystem_get_out_())) printlnWithInt:i];",
        "}",
        "return self;",
        "}");
  }

  public void testInitializerMovedToEmptyConstructor() {
    TypeDeclaration clazz = translateClassBody(
        "java.util.Date date = new java.util.Date(); public Test() {}");
    List<BodyDeclaration> classMembers = clazz.getBodyDeclarations();
    assertEquals(4, classMembers.size());  // dealloc() was also added to release date

    // Test that the constructor had super() and initialization statements added.
    BodyDeclaration decl = classMembers.get(1);
    MethodDeclaration method = (MethodDeclaration) decl;
    IMethodBinding binding = method.getMethodBinding();
    assertTrue(binding.isConstructor());
    assertEquals(Modifier.PUBLIC, method.getModifiers());
    assertTrue(method.getParameters().isEmpty());
    List<Statement> generatedStatements = method.getBody().getStatements();
    assertEquals(2, generatedStatements.size());
    assertTrue(generatedStatements.get(0) instanceof SuperConstructorInvocation);
    SuperConstructorInvocation superInvoke =
        (SuperConstructorInvocation) generatedStatements.get(0);
    assertTrue(superInvoke.getArguments().isEmpty());
    assertTrue(generatedStatements.get(1) instanceof ExpressionStatement);
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
    String source = "class Test { static final String foo = \"\\uffff\"; }";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation, "NSString * Test_foo_;");
    assertTranslation(translation,
        "JreStrongAssign(&Test_foo_, nil, [NSString stringWithCharacters:(jchar[]) { "
        + "(int) 0xffff } length:1]);");
  }

  public void testStringConcatWithInvalidCppCharacters() throws IOException {
    String source = "class Test { static final String foo = \"hello\" + \"\\uffff\"; }";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation, "NSString * Test_foo_;");
    assertTranslation(translation,
        "JreStrongAssign(&Test_foo_, nil, JreStrcat(\"$$\", @\"hello\", "
        + "[NSString stringWithCharacters:(jchar[]) { (int) 0xffff } length:1]));");
  }

  public void testInitializersPlacedAfterOuterAssignments() throws IOException {
    String source = "class Test { "
         + "  int outerVar = 1; "
         + "  class Inner { int innerVar = outerVar; void test() { outerVar++; } } }";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation, "Test_Inner_set_this$0_(self, outer$);");
    assertTranslation(translation, "innerVar_ = outer$->outerVar_;");
    assertTrue(translation.indexOf("Test_Inner_set_this$0_(self, outer$);")
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
    String setInit = "JreStrongAssignAndConsume(&Test_iSet_, nil, [[JavaUtilHashSet alloc] init])";
    String setAdd = "[Test_iSet_ addWithId:JavaLangInteger_valueOfWithInt_(Test_I)]";
    String setSize = "Test_iSetSize_ = [Test_iSet_ size]";
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
    assertTranslation(translation, "NSString * Test_FOO_ = @\"bar\";");
  }
}
