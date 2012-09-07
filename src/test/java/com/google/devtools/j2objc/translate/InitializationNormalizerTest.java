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
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.io.IOException;
import java.util.List;

/**
 * Unit tests for {@link InitializationNormalization} phase.
 *
 * @author Tom Ball
 */
@SuppressWarnings("unchecked")  // JDT lists are raw, but still safely typed.
public class InitializationNormalizerTest extends GenerationTest {
  // TODO(user): update bug id in comments to public issue numbers when
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
    List<?> types = unit.types();
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
    String source = "class Test {" +
    	"boolean b1; boolean b2;" +
        "Test() { this(true); b2 = true; }" +
        "Test(boolean b) { b1 = b; }}";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation, "if ((self = [self initWithBOOL:YES])) {");
  }

  /**
   * Regression test (b/5822974): translation fails with an
   * ArrayIndexOutOfBoundsException in JDT, due to a syntax error in the
   * vertices initializer after initialization normalization.
   * @throws IOException
   */
  public void testFieldArrayInitializer() throws IOException {
    String source = "public class Distance {" +
        "private class SimplexVertex {}" +
        "private class Simplex {" +
        "  public final SimplexVertex vertices[] = {" +
        "    new SimplexVertex() " +
        "  }; }}";
    String translation = translateSourceFile(source, "Distance", "Distance.m");
    assertTranslation(translation,
        "[IOSObjectArray arrayWithObjects:(id[]){ [[Distance_SimplexVertex alloc] " +
        "initWithDistance:this$0_] } " +
        "count:1 type:[IOSClass classWithClass:[Distance_SimplexVertex class]]]");
  }

  public void testStaticVarInitialization() {
    TypeDeclaration clazz =
        translateClassBody("static java.util.Date date = new java.util.Date();");
    List<BodyDeclaration> classMembers = clazz.bodyDeclarations();
    assertEquals(4, classMembers.size()); // added two accessors and initialize method

    // test that initializer was stripped from the declaration
    BodyDeclaration decl = classMembers.get(0);
    assertTrue(decl instanceof FieldDeclaration);
    List<VariableDeclarationFragment> varFragments = ((FieldDeclaration) decl).fragments();
    assertEquals(1, varFragments.size());
    VariableDeclarationFragment field = varFragments.get(0);
    assertTrue(field.getInitializer() == null);

    // test that a read accessor was added
    decl = classMembers.get(1);
    assertTrue(decl instanceof MethodDeclaration);
    MethodDeclaration method = (MethodDeclaration) decl;
    assertEquals("date", method.getName().getIdentifier());
    assertEquals(Modifier.STATIC, method.getModifiers());
    assertTrue(method.parameters().isEmpty());
    List<Statement> generatedStatements = method.getBody().statements();
    assertEquals(1, generatedStatements.size());
    assertTrue(generatedStatements.get(0) instanceof ReturnStatement);
    assertEquals("return date_;\n", generatedStatements.get(0).toString());

    // test that a write accessor was added
    decl = classMembers.get(2);
    assertTrue(decl instanceof MethodDeclaration);
    method = (MethodDeclaration) decl;
    assertEquals("setDate", method.getName().getIdentifier());
    assertEquals(Modifier.STATIC, method.getModifiers());
    assertEquals(1, method.parameters().size());
    SingleVariableDeclaration param = (SingleVariableDeclaration) method.parameters().get(0);
    assertEquals("date", param.getName().getIdentifier());
    generatedStatements = method.getBody().statements();
    assertEquals(1, generatedStatements.size());
    assertTrue(generatedStatements.get(0) instanceof ExpressionStatement);
    ExpressionStatement stmt = (ExpressionStatement) generatedStatements.get(0);
    assertTrue(stmt.getExpression() instanceof Assignment);
    Assignment assign = (Assignment) stmt.getExpression();
    assertEquals("Test_date_", NameTable.getName(Types.getBinding(assign.getLeftHandSide())));
    assertEquals("date", assign.getRightHandSide().toString());

    // test that initializer was moved to new initialize method
    decl = classMembers.get(3);
    assertTrue(decl instanceof MethodDeclaration);
    method = (MethodDeclaration) decl;
    assertEquals(NameTable.CLINIT_NAME, method.getName().getIdentifier());
    assertEquals(Modifier.PUBLIC | Modifier.STATIC, method.getModifiers());
    assertTrue(method.parameters().isEmpty());
    generatedStatements = method.getBody().statements();
    assertEquals(1, generatedStatements.size());
    assertTrue(generatedStatements.get(0) instanceof ExpressionStatement);
    stmt = (ExpressionStatement) generatedStatements.get(0);
    assertTrue(stmt.getExpression() instanceof Assignment);
    assign = (Assignment) stmt.getExpression();
    assertEquals("Test_date_", NameTable.getName(Types.getBinding(assign.getLeftHandSide())));
    assertEquals("new java.util.Date()", assign.getRightHandSide().toString());
  }

  public void testFieldInitializer() {
    TypeDeclaration clazz = translateClassBody("java.util.Date date = new java.util.Date();");
    List<BodyDeclaration> classMembers = clazz.bodyDeclarations();
    assertEquals(3, classMembers.size());  // dealloc() was also added to release date

    // test that a default constructor was created
    BodyDeclaration decl = classMembers.get(1);
    assertTrue(decl instanceof MethodDeclaration);
    MethodDeclaration method = (MethodDeclaration) decl;
    assertEquals(InitializationNormalizer.INIT_NAME, method.getName().getIdentifier());
    assertEquals(Modifier.PUBLIC, method.getModifiers());
    assertTrue(method.parameters().isEmpty());
    List<Statement> generatedStatements = method.getBody().statements();
    assertEquals(2, generatedStatements.size());
    assertTrue(generatedStatements.get(0) instanceof SuperConstructorInvocation);
    SuperConstructorInvocation superInvoke =
        (SuperConstructorInvocation) generatedStatements.get(0);
    assertTrue(superInvoke.arguments().isEmpty());

    // test that initializer statement was moved to constructor
    assertTrue(generatedStatements.get(1) instanceof ExpressionStatement);
    ExpressionStatement stmt = (ExpressionStatement) generatedStatements.get(1);
    assertTrue(stmt.getExpression() instanceof Assignment);
    Assignment assign = (Assignment) stmt.getExpression();
    assertEquals("date", assign.getLeftHandSide().toString());
    assertEquals("new java.util.Date()", assign.getRightHandSide().toString());
  }

  public void testInitializationBlock() {
    TypeDeclaration clazz =
        translateClassBody("java.util.Date date; { date = new java.util.Date(); }");
    List<BodyDeclaration> classMembers = clazz.bodyDeclarations();
    assertEquals(3, classMembers.size());  // dealloc() was also added to release date

    // test that a default constructor was created
    BodyDeclaration decl = classMembers.get(1);
    assertTrue(decl instanceof MethodDeclaration);
    MethodDeclaration method = (MethodDeclaration) decl;
    assertTrue(method.isConstructor());
    assertEquals(InitializationNormalizer.INIT_NAME, method.getName().getIdentifier());
    assertEquals(Modifier.PUBLIC, method.getModifiers());
    assertTrue(method.parameters().isEmpty());
    List<Statement> generatedStatements = method.getBody().statements();
    assertEquals(2, generatedStatements.size());
    assertTrue(generatedStatements.get(0) instanceof SuperConstructorInvocation);
    SuperConstructorInvocation superInvoke =
        (SuperConstructorInvocation) generatedStatements.get(0);
    assertTrue(superInvoke.arguments().isEmpty());

    // test that initializer statement was moved to constructor
    assertTrue(generatedStatements.get(1) instanceof ExpressionStatement);
    ExpressionStatement stmt = (ExpressionStatement) generatedStatements.get(1);
    assertTrue(stmt.getExpression() instanceof Assignment);
    Assignment assign = (Assignment) stmt.getExpression();
    assertEquals("date", assign.getLeftHandSide().toString());
    assertEquals("new java.util.Date()", assign.getRightHandSide().toString());
  }

  public void testStaticInitializerBlock() {
    TypeDeclaration clazz = translateClassBody("static { System.out.println(\"foo\"); }");
    List<BodyDeclaration> classMembers = clazz.bodyDeclarations();
    assertEquals(1, classMembers.size());

    // test that a static initialize() method was created
    BodyDeclaration decl = classMembers.get(0);
    assertTrue(decl instanceof MethodDeclaration);
    MethodDeclaration method = (MethodDeclaration) decl;
    assertEquals(NameTable.CLINIT_NAME, method.getName().getIdentifier());
    assertEquals(Modifier.PUBLIC | Modifier.STATIC, method.getModifiers());
    assertTrue(method.parameters().isEmpty());

    // test that the method body consists of the block's statement
    List<Statement> generatedStatements = method.getBody().statements();
    assertEquals(1, generatedStatements.size());
    assertTrue(generatedStatements.get(0) instanceof ExpressionStatement);
    ExpressionStatement stmt = (ExpressionStatement) generatedStatements.get(0);
    assertEquals("NSLog(\"%@\",\"foo\")", stmt.getExpression().toString());
  }

  public void testIsDesignatedConstructor() {
    TypeDeclaration clazz = translateClassBody(
        "Test() { this(42); } Test(int i) {} Test(int i, byte b) { System.out.print(b); }");
    List<BodyDeclaration> classMembers = clazz.bodyDeclarations();
    assertEquals(3, classMembers.size());

    BodyDeclaration decl = classMembers.get(0);
    assertTrue(decl instanceof MethodDeclaration);
    assertFalse(instance.isDesignatedConstructor((MethodDeclaration) decl));

    decl = classMembers.get(1);
    assertTrue(decl instanceof MethodDeclaration);
    assertTrue(instance.isDesignatedConstructor((MethodDeclaration) decl));

    decl = classMembers.get(2);
    assertTrue(decl instanceof MethodDeclaration);
    assertTrue(instance.isDesignatedConstructor((MethodDeclaration) decl));
  }

  public void testInitializerMovedToDesignatedConstructor() {
    TypeDeclaration clazz = translateClassBody(
      "java.util.Date date; { date = new java.util.Date(); } "
      + "public Test() { this(2); } public Test(int i) { System.out.println(i); }");
    List<BodyDeclaration> classMembers = clazz.bodyDeclarations();
    assertEquals(4, classMembers.size());  // dealloc() was also added to release date

    // test that default constructor was untouched, since it calls self()
    BodyDeclaration decl = classMembers.get(1);
    assertTrue(decl instanceof MethodDeclaration);
    MethodDeclaration method = (MethodDeclaration) decl;
    assertTrue(method.isConstructor());
    assertEquals(Modifier.PUBLIC, method.getModifiers());
    assertTrue(method.parameters().isEmpty());
    List<Statement> generatedStatements = method.getBody().statements();
    assertEquals(1, generatedStatements.size());
    assertTrue(generatedStatements.get(0) instanceof ConstructorInvocation);

    // test that initializer statement was added to second constructor
    decl = classMembers.get(2);
    assertTrue(decl instanceof MethodDeclaration);
    method = (MethodDeclaration) decl;
    assertTrue(method.isConstructor());
    assertEquals(Modifier.PUBLIC, method.getModifiers());
    assertEquals(1, method.parameters().size());
    generatedStatements = method.getBody().statements();
    assertEquals(3, generatedStatements.size());
    assertTrue(generatedStatements.get(0) instanceof SuperConstructorInvocation);
    assertTrue(generatedStatements.get(1) instanceof ExpressionStatement);
    Expression expr = ((ExpressionStatement) generatedStatements.get(1)).getExpression();
    assertTrue(expr instanceof Assignment);
    assertTrue(generatedStatements.get(2) instanceof ExpressionStatement);
    expr = ((ExpressionStatement) generatedStatements.get(2)).getExpression();
    assertTrue(expr instanceof MethodInvocation);
  }

  public void testInitializerMovedToEmptyConstructor() {
    TypeDeclaration clazz = translateClassBody(
        "java.util.Date date = new java.util.Date(); public Test() {}");
    List<BodyDeclaration> classMembers = clazz.bodyDeclarations();
    assertEquals(3, classMembers.size());  // dealloc() was also added to release date

    // Test that the constructor had super() and initialization statements added.
    BodyDeclaration decl = classMembers.get(1);
    MethodDeclaration method = (MethodDeclaration) decl;
    IMethodBinding binding = method.resolveBinding();
    assertTrue(binding.isConstructor());
    assertEquals(Modifier.PUBLIC, method.getModifiers());
    assertTrue(method.parameters().isEmpty());
    List<Statement> generatedStatements = method.getBody().statements();
    assertEquals(2, generatedStatements.size());
    assertTrue(generatedStatements.get(0) instanceof SuperConstructorInvocation);
    SuperConstructorInvocation superInvoke =
        (SuperConstructorInvocation) generatedStatements.get(0);
    assertTrue(superInvoke.arguments().isEmpty());
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
}
