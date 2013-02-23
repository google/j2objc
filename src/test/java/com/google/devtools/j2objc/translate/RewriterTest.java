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
import com.google.devtools.j2objc.J2ObjC;
import com.google.devtools.j2objc.types.Types;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Unit tests for {@link Rewriter}.
 *
 * @author Tom Ball
 */
@SuppressWarnings("unchecked")
public class RewriterTest extends GenerationTest {

  public void testContinueAndBreakUsingSameLabel() {
    List<Statement> stmts = translateStatements(
        "int i = 0; outer: for (; i < 10; i++) { " +
        "for (int j = 0; j < 10; j++) { " +
          "int n = i + j; " +
          "if (n == 5) continue outer; " +
          "else break outer; }}");
    assertEquals(3, stmts.size());
    Statement s = stmts.get(1);
    assertTrue(s instanceof ForStatement);  // not LabeledStatement
    ForStatement fs = (ForStatement) s;
    Statement forStmt = fs.getBody();
    assertTrue(forStmt instanceof Block);
    List<Statement> innerStmts = ((Block) forStmt).statements();
    assertEquals(2, innerStmts.size());
    Statement innerForLoop = innerStmts.get(0);
    assertTrue(innerForLoop instanceof ForStatement);
    Statement innerForBlock = ((ForStatement) innerForLoop).getBody();
    assertTrue(innerForBlock instanceof Block);
    List<Statement> innerStmts2 = ((Block) innerForBlock).statements();
    assertEquals(2, innerStmts2.size());
    Statement ifStmt = innerStmts2.get(1);
    assertTrue(ifStmt instanceof IfStatement);
    Statement continueStmt = ((IfStatement) ifStmt).getThenStatement();
    assertTrue(continueStmt instanceof ContinueStatement);
    assertEquals("continue_outer", ((ContinueStatement) continueStmt).getLabel().getIdentifier());
    Statement breakStmt = ((IfStatement) ifStmt).getElseStatement();
    assertTrue(breakStmt instanceof BreakStatement);
    assertEquals("break_outer", ((BreakStatement) breakStmt).getLabel().getIdentifier());
    Statement lastInnerStmt = innerStmts.get(1);
    assertTrue(lastInnerStmt instanceof LabeledStatement);
    LabeledStatement continueLabel = (LabeledStatement) lastInnerStmt;
    assertEquals("continue_outer", continueLabel.getLabel().getIdentifier());
    assertTrue(continueLabel.getBody() instanceof EmptyStatement);
    Statement lastStmt = stmts.get(2);
    assertTrue(lastStmt instanceof LabeledStatement);
    LabeledStatement breakLabel = (LabeledStatement) lastStmt;
    assertEquals("break_outer", breakLabel.getLabel().getIdentifier());
    assertTrue(breakLabel.getBody() instanceof EmptyStatement);
  }

  public void testLabeledContinue() throws IOException {
    List<Statement> stmts = translateStatements(
        "int i = 0; outer: for (; i < 10; i++) { " +
        "for (int j = 0; j < 10; j++) { continue outer; }}");
    assertEquals(2, stmts.size());
    Statement s = stmts.get(1);
    assertTrue(s instanceof ForStatement);  // not LabeledStatement
    ForStatement fs = (ForStatement) s;
    Statement forStmt = fs.getBody();
    assertTrue(forStmt instanceof Block);
    stmts = ((Block) forStmt).statements();
    assertEquals(2, stmts.size());
    Statement lastStmt = stmts.get(1);
    assertTrue(lastStmt instanceof LabeledStatement);
    assertTrue(((LabeledStatement) lastStmt).getBody() instanceof EmptyStatement);
  }

  public void testLabeledBreak() throws IOException {
    List<Statement> stmts = translateStatements(
        "int i = 0; outer: for (; i < 10; i++) { " +
        "for (int j = 0; j < 10; j++) { break outer; }}");
    assertEquals(3, stmts.size());
    Statement s = stmts.get(1);
    assertTrue(s instanceof ForStatement);  // not LabeledStatement
    ForStatement fs = (ForStatement) s;
    Statement forStmt = fs.getBody();
    assertTrue(forStmt instanceof Block);
    assertEquals(1, ((Block) forStmt).statements().size());
    Statement lastStmt = stmts.get(2);
    assertTrue(lastStmt instanceof LabeledStatement);
    assertTrue(((LabeledStatement) lastStmt).getBody() instanceof EmptyStatement);
  }

  public void testLabeledBreakWithNonBlockParent() throws IOException {
    List<Statement> stmts = translateStatements(
        "int i = 0; if (i == 0) outer: for (; i < 10; i++) { " +
        "for (int j = 0; j < 10; j++) { break outer; }}");
    assertEquals(2, stmts.size());
    Statement s = stmts.get(1);
    assertTrue(s instanceof IfStatement);
    s = ((IfStatement) s).getThenStatement();
    assertTrue(s instanceof Block);
    stmts = ((Block) s).statements();
    assertEquals(2, stmts.size());
    s = stmts.get(0);
    assertTrue(s instanceof ForStatement);  // not LabeledStatement
    ForStatement fs = (ForStatement) s;
    Statement forStmt = fs.getBody();
    assertTrue(forStmt instanceof Block);
    assertEquals(1, ((Block) forStmt).statements().size());
    Statement labelStmt = stmts.get(1);
    assertTrue(labelStmt instanceof LabeledStatement);
    assertTrue(((LabeledStatement) labelStmt).getBody() instanceof EmptyStatement);
  }

  public void testStaticReaderAdded() {
    String source = "class Test { private static int foo; }";
    assertEquals(1, methodCount(source, "foo", new String[0]));
  }

  public void testStaticReaderAddedWhenSameMethodNameExists() {
    String source = "class Test { private static int foo; void foo(String s) {}}";
    assertEquals(1, methodCount(source, "foo", new String[0]));
  }

  public void testStaticWriterAdded() {
    String source = "class Test { private static int foo; }";
    assertEquals(1, methodCount(source, "setFoo", new String[] { "int" }));
  }

  public void testStaticWriterAddedWhenSameMethodNameExists() {
    String source = "class Test { private static int foo; void setFoo(String s) {}}";
    assertEquals(1, methodCount(source, "setFoo", new String[] { "int" }));
  }

  /**
   * Verify that a static reader method is not added to a class that already
   * has one.
   */
  public void testExistingStaticReaderDetected() {
    String source =
        "class Test { private static int foo; public static int foo() { return foo; }}";
    assertEquals(1, methodCount(source, "foo", new String[0]));
  }

  /**
   * Verify that a static writer method is not added to a class that already
   * has one.
   */
  public void testExistingStaticWriterDetected() {
    String source = "class Test { private static int foo;" +
        "public static void setFoo(int newFoo) { foo = newFoo; }}";
    assertEquals(1, methodCount(source, "setFoo", new String[] { "int" }));
  }

  private int methodCount(String source, String methodName, String[] paramTypes) {
    CompilationUnit unit = translateType("Test", source);
    List<?> types = unit.types();
    assertEquals(1, types.size());
    TypeDeclaration testType = (TypeDeclaration) types.get(0);
    MethodDeclaration[] methods = testType.getMethods();
    int nFooMethods = 0;
    for (MethodDeclaration m : methods) {
      if (m.getName().getIdentifier().equals(methodName) &&
          parametersMatch(paramTypes, m.parameters())) {
        nFooMethods++;
      }
    }
    return nFooMethods;
  }

  private boolean parametersMatch(String[] paramTypes, List<?> parameters) {
    if (parameters.size() != paramTypes.length) {
      return false;
    }
    for (int i = 0; i < paramTypes.length; i++) {
      SingleVariableDeclaration param = (SingleVariableDeclaration) parameters.get(i);
      if (!param.getType().toString().equals(paramTypes[i])) {
        return false;
      }
    }
    return true;
  }

  /**
   * Verifies that abstract methods to implement an interface are added to an
   * abstract class.
   */
  public void testAbstractMethodsAdded() {
    String source =
        "import java.util.Iterator; public abstract class Test implements Iterator<Test> { "
            + "public boolean hasNext() { return true; } }";
    CompilationUnit unit = translateType("Test", source);
    List<?> types = unit.types();
    assertEquals(1, types.size());
    assertTrue(types.get(0) instanceof TypeDeclaration);
    TypeDeclaration testType = (TypeDeclaration) types.get(0);
    MethodDeclaration[] methods = testType.getMethods();
    assertEquals(4, methods.length);

    // verify added methods are abstract, and that existing method wasn't changed
    for (MethodDeclaration m : methods) {
      int modifiers = m.getModifiers();
      String name = m.getName().getIdentifier();
      if (name.equals("hasNext")) {
        assertFalse(Modifier.isAbstract(modifiers));
      } else if (name.equals(DestructorGenerator.FINALIZE_METHOD)
          || name.equals(DestructorGenerator.DEALLOC_METHOD)) {
        // it's ok.
      } else {
        // it's an added method
        assertTrue(Modifier.isAbstract(modifiers));
        assertEquals(0, m.parameters().size());
        if (name.equals("next")) {
          assertEquals(testType.resolveBinding(), Types.getBinding(m.getReturnType2()));
        } else if (name.equals("remove")) {
          ITypeBinding voidType = m.getAST().resolveWellKnownType("void");
          assertEquals(voidType, m.getReturnType2().resolveBinding());
        } else {
          fail("unknown method added: " + name);
        }
      }
    }
  }

  /**
   * List has a toArray() method that uses array types.
   */
  public void testAbstractMethodsAddedWithArrayType() {
    String source =
        "import java.util.List; public abstract class Test implements List<Object> { "
            + "public boolean isEmpty() { return true; } }";
    CompilationUnit unit = translateType("Test", source);
    List<?> types = unit.types();
    assertEquals(1, types.size());
    assertTrue(types.get(0) instanceof TypeDeclaration);
    TypeDeclaration testType = (TypeDeclaration) types.get(0);
    MethodDeclaration[] methods = testType.getMethods();
    assertEquals(26, methods.length);

    // verify added methods are abstract, and that existing method wasn't changed
    for (MethodDeclaration m : methods) {
      int modifiers = m.getModifiers();
      String name = m.getName().getIdentifier();
      if (name.equals("isEmpty")) {
        assertFalse(Modifier.isAbstract(modifiers));
      } else if (name.equals(DestructorGenerator.FINALIZE_METHOD)
          || name.equals(DestructorGenerator.DEALLOC_METHOD)) {
        // it's ok.
      } else {
        // it's an added method
        assertTrue(Modifier.isAbstract(modifiers));
        ITypeBinding returnType = Types.getTypeBinding(m.getReturnType2());
        if (name.equals("toArray")) {
          assertTrue(returnType.isArray());
          ITypeBinding componentType = returnType.getComponentType();
          assertEquals(Types.getNSObject(), componentType);
          if (!m.parameters().isEmpty()) {
            assertEquals(1, m.parameters().size());
            Object param = m.parameters().get(0);
            IBinding paramBinding = Types.getBinding(param);
            assertTrue(paramBinding instanceof IVariableBinding);
            ITypeBinding paramType = ((IVariableBinding) paramBinding).getType();
            assertTrue(paramType.isArray());
          }
        }
      }
    }
  }

  /**
   * Verify that super-interface methods are also added.
   */
  public void testAbstractClassGrandfatherInterface() {
    String source =
        "public class Test {" +
        "  public interface I1 { void foo(); } " +
        "  public interface I2 extends I1 { void bar(); } " +
        "  public abstract class Inner implements I2 { } }";
    CompilationUnit unit = translateType("Test", source);
    List<?> types = unit.types();
    assertEquals(4, types.size());
    assertTrue(types.get(3) instanceof TypeDeclaration);
    TypeDeclaration innerType = (TypeDeclaration) types.get(3);
    assertEquals("Inner", innerType.getName().toString());

    MethodDeclaration[] methods = innerType.getMethods();
    assertEquals(4, methods.length);
    String name0 = methods[0].getName().getIdentifier();
    assertTrue(name0.matches("foo|bar"));
    String name1 = methods[1].getName().getIdentifier();
    assertTrue(name1.matches("foo|bar"));
    assertNotSame(name0, name1);
  }

  /**
   * Verify that interface methods declaring methods implemented by
   * super-class have a forwarding method.
   */
  public void testInterfaceOfSuperclassMethod() {
    String source =
        "public class Test implements Equateable {} " +
        "interface Equateable { boolean equals(Object o); }";
    CompilationUnit unit = translateType("Test", source);
    assertEquals(2, unit.types().size());
    TypeDeclaration innerType = (TypeDeclaration) unit.types().get(0);
    assertEquals("Test", innerType.getName().toString());

    MethodDeclaration[] methods = innerType.getMethods();
    assertEquals(2, methods.length);
    MethodDeclaration equalsMethod = methods[0];
    assertEquals("isEqual", equalsMethod.getName().getIdentifier());
    assertEquals(1, equalsMethod.parameters().size());
    assertTrue(equalsMethod.parameters().get(0) instanceof SingleVariableDeclaration);
    List<Statement> stmts = equalsMethod.getBody().statements();
    assertEquals(1, stmts.size());
    Statement stmt = stmts.get(0);
    assertTrue(stmt instanceof ReturnStatement);
    assertTrue(((ReturnStatement) stmt).getExpression() instanceof SuperMethodInvocation);
  }

  /**
   * Verify that interface methods declaring methods implemented by
   * super-class have a forwarding method.
   */
  public void testInterfaceOfSuperclassMethodInAnonymousInner() {
    String source =
        "interface Equateable { boolean equals(Object o); }" +
        "public class Test { public void foo() { Equateable e = new Equateable() { }; } } ";
    CompilationUnit unit = translateType("Test", source);
    assertEquals(3, unit.types().size());
    TypeDeclaration innerType = (TypeDeclaration) unit.types().get(2);
    assertEquals("$1", innerType.getName().toString());

    MethodDeclaration[] methods = innerType.getMethods();
    assertEquals(3, methods.length); // isEqual, init, dealloc
    MethodDeclaration equalsMethod = methods[0];
    assertEquals("isEqual", equalsMethod.getName().getIdentifier());
    assertEquals(1, equalsMethod.parameters().size());
    assertTrue(equalsMethod.parameters().get(0) instanceof SingleVariableDeclaration);
    List<Statement> stmts = equalsMethod.getBody().statements();
    assertEquals(1, stmts.size());
    Statement stmt = stmts.get(0);
    assertTrue(stmt instanceof ReturnStatement);
    assertTrue(((ReturnStatement) stmt).getExpression() instanceof SuperMethodInvocation);
  }

  /**
   * Verify that array initializers are rewritten as method calls.
   */
  public void testArrayInitializerRewrite() {
    String source =
        "public class Test { void test() { int[] a = { 1, 2, 3 }; char b[] = { '4', '5' }; } }";
    CompilationUnit unit = translateType("Test", source);
    TypeDeclaration clazz = (TypeDeclaration) unit.types().get(0);
    MethodDeclaration md = (MethodDeclaration) clazz.bodyDeclarations().get(0);
    List<Statement> stmts = md.getBody().statements();
    assertEquals(2, stmts.size());

    assertEquals("IOSIntArray a=IOSIntArray.arrayWithInts({1,2,3},3);",
        stmts.get(0).toString().trim());
    assertEquals("IOSCharArray b[]=IOSCharArray.arrayWithCharacters({'4','5'},2);",
      stmts.get(1).toString().trim());
  }

  /**
   * Verify that static array initializers are rewritten as method calls.
   */
  public void testStaticArrayInitializerRewrite() {
    String source =
        "public class Test { static int[] a = { 1, 2, 3 }; static char b[] = { '4', '5' }; }";
    CompilationUnit unit = translateType("Test", source);
    TypeDeclaration clazz = (TypeDeclaration) unit.types().get(0);
    Iterator<BodyDeclaration> classMembers = clazz.bodyDeclarations().iterator();

    boolean foundInitStatements = false;
    while (classMembers.hasNext()) {
      BodyDeclaration member = classMembers.next();
      if (member instanceof MethodDeclaration) {
        MethodDeclaration md = (MethodDeclaration) member;
        if (md.getName().getIdentifier().equals("initialize")) {
          List<Statement> stmts = md.getBody().statements();
          assertEquals(2, stmts.size());
          foundInitStatements = true;

          List<Statement> blockStmts = ((Block) stmts.get(0)).statements();
          assertEquals("Test_a_=IOSIntArray.arrayWithInts({1,2,3},3);",
              blockStmts.get(0).toString().trim());
          blockStmts = ((Block) stmts.get(1)).statements();
          assertEquals("Test_b_=IOSCharArray.arrayWithCharacters({'4','5'},2);",
            blockStmts.get(0).toString().trim());
        }
      }
    }
    assertTrue(foundInitStatements);
  }

  public void testNonStaticMultiDimArrayInitializer() throws IOException {
    String translation = translateSourceFile(
        "class Test { int[][] a = { { 1, 2, 3 } }; }", "Test", "Test.m");
    assertTranslation(translation,
        "[IOSObjectArray arrayWithObjects:(id[]){" +
        " [IOSIntArray arrayWithInts:(int[]){ 1, 2, 3 } count:3] } count:1" +
        " type:[IOSClass classWithClass:[IOSIntArray class]]]");
  }

  public void testArrayCreationInConstructorInvocation() throws IOException {
    String translation = translateSourceFile(
        "class Test { Test(int[] i) {} Test() { this(new int[] {}); } }", "Test", "Test.m");
    assertTranslation(translation,
        "[self initTestWithJavaLangIntegerArray:[IOSIntArray arrayWithInts:(int[]){  } count:0]]");
  }

  /**
   * Verify serialization methods and fields are removed.
   */
  public void testSerializationRemoval() {
    String source = "import java.io.*; public class Test implements Serializable { " +
        "private int foo; " +

        "private static long serialVersionUID; " +
        "private void readObject(ObjectInputStream in) {} " +
        "private void writeObject(ObjectOutputStream out) {} " +
        "private void readObjectNoData() {} " +
        "private Object readResolve() { return null; } " +
        "private Object writeResolve() { return null;} " +

        "public Test() {} " +
        "private void testMethod() {} }";
    CompilationUnit unit = compileType("Test", source);
    TypeDeclaration type = (TypeDeclaration) unit.types().get(0);
    List<BodyDeclaration> members = type.bodyDeclarations();
    assertEquals(9, members.size());
    J2ObjC.initializeTranslation(unit);
    J2ObjC.translate(unit, source);
    assertEquals(4, members.size());
    FieldDeclaration f = (FieldDeclaration) members.get(0);
    VariableDeclarationFragment var = (VariableDeclarationFragment) f.fragments().get(0);
    assertEquals("foo", var.getName().getIdentifier());
    MethodDeclaration m = (MethodDeclaration) members.get(1);
    assertTrue(m.isConstructor());
    m = (MethodDeclaration) members.get(2);
    assertEquals("testMethod", m.getName().getIdentifier());
  }

  public void testStaticInitializersKeptInOrder() {
    String source =
        "public class Test { " +
        "  public static final int I = 1; " +
        "  public static final java.util.Set<Integer> iSet = new java.util.HashSet<Integer>(); " +
        "  static { iSet.add(I); } " +
        "  public static final int iSetSize = iSet.size(); }";
    CompilationUnit unit = compileType("Test", source);
    J2ObjC.initializeTranslation(unit);
    J2ObjC.translate(unit, source);
    List<BodyDeclaration> classMembers = ((TypeDeclaration) unit.types().get(0)).bodyDeclarations();
    assertEquals(8, classMembers.size()); // 3 fields + 3 getters + 1 clInit + 1 dealloc

    // Test that the clInit has the right statements in order.
    MethodDeclaration clInit = (MethodDeclaration) classMembers.get(6);
    assertEquals("initialize", clInit.getName().getIdentifier());
    assertEquals(Modifier.PUBLIC | Modifier.STATIC, clInit.getModifiers());
    List<Statement> statements = clInit.getBody().statements();
    assertEquals(3, statements.size());

    // Test_iSet_ = new ...
    Statement first = statements.get(0);
    assertTrue(first instanceof Block);
    Block b = (Block) first;
    first = (Statement) b.statements().get(0);
    assertTrue(first instanceof ExpressionStatement);
    Expression firstExpr = ((ExpressionStatement) first).getExpression();
    assertTrue(firstExpr instanceof Assignment);
    assertEquals("Test_iSet_",
        ((SimpleName) ((Assignment) firstExpr).getLeftHandSide()).getIdentifier());

    // iSet.add(...)
    Statement second = statements.get(1);
    assertTrue(second instanceof Block);
    b = (Block) second;
    second = (Statement) b.statements().get(0);
    assertTrue(second instanceof ExpressionStatement);
    Expression secondExpr = ((ExpressionStatement) second).getExpression();
    assertTrue(secondExpr instanceof MethodInvocation);

    // Test_iSetSize_ = ...
    Statement third = statements.get(2);
    assertTrue(third instanceof Block);
    b = (Block) third;
    third = (Statement) b.statements().get(0);
    assertTrue(third instanceof ExpressionStatement);
    Expression thirdExpr = ((ExpressionStatement) third).getExpression();
    assertTrue(thirdExpr instanceof Assignment);
    assertEquals("Test_iSetSize_",
        ((SimpleName) ((Assignment) thirdExpr).getLeftHandSide()).getIdentifier());
  }

  public void testRewriteSystemOut() throws IOException {
    String source = "class A {\n" +
        "  void foo() {\n" +
        "    System.out.println(\"foo\");\n" +
        "    System.out.println();\n" +
        "  }" +
        "}\n";
    String translation = translateSourceFile(source, "A", "A.m");
    assertTranslation(translation, "NSLog(@\"%@\", @\"foo\")");
    assertTranslation(translation, "NSLog(@\"\")");
  }

  public void testAddsAbstractMethodsToEnum() throws IOException {
    String interfaceSource = "interface I { public int foo(); }";
    String enumSource =
        "enum E implements I { " +
        "  A { public int foo() { return 42; } }," +
        "  B { public int foo() { return -1; } } }";
    addSourceFile(interfaceSource, "I.java");
    addSourceFile(enumSource, "E.java");
    String translation = translateSourceFile("E", "E.m");
    assertTranslation(translation, "- (int)foo {");
    assertTranslation(translation, "[self doesNotRecognizeSelector:_cmd];");
  }

  public void testInterfaceFieldsAreStaticFinal() throws IOException {
    String source = "interface Test { String foo = \"bar\"; }";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation, "+ (NSString *)foo {");
    assertFalse(translation.contains("setFoo"));
  }

  // Regression test: the wrong method name used for "f.group()" translation.
  public void testNSLogWithMethodInvocation() throws IOException {
    String source = "public class A { " +
        "String group() { return \"foo\"; } " +
        "void test() { A a = new A(); System.out.println(a.group()); }}";
    String translation = translateSourceFile(source, "A", "A.m");
    assertTranslation(translation, "NSLog(@\"%@\", [((A *) NIL_CHK(a)) group]);");
  }

  // Regression test: Must call "charValue" on boxed type returned from iterator.
  public void testEnhancedForWithBoxedType() throws IOException {
    String source = "import java.util.List;" +
        "public class A { " +
        "List<Character> chars; " +
        "void test() { for (char c : chars) {} } }";
    String translation = translateSourceFile(source, "A", "A.m");
    assertTranslation(translation,
        "unichar c = [((JavaLangCharacter *) [((id<JavaUtilIterator>) NIL_CHK(iter__)) next]) " +
        "charValue];");
  }

  public void testStaticArrayInitializerMove() throws IOException {
    String source = "class Test { static final double[] EVERY_SIXTEENTH_FACTORIAL = " +
        "{ 0x1.0p0, 0x1.30777758p44, 0x1.956ad0aae33a4p117, 0x1.ee69a78d72cb6p202, " +
        "0x1.fe478ee34844ap295, 0x1.c619094edabffp394, 0x1.3638dd7bd6347p498, " +
        "0x1.7cac197cfe503p605, 0x1.1e5dfc140e1e5p716, 0x1.8ce85fadb707ep829, " +
        "0x1.95d5f3d928edep945 }; }";
    String translation = translateSourceFile(source, "Test", "Test.m");
    assertTranslation(translation, "{ 1.0, 2.0922789888E13, 2.631308369336935E35, " +
        "1.2413915592536073E61, 1.2688693218588417E89, 7.156945704626381E118, " +
        "9.916779348709496E149, 1.974506857221074E182, 3.856204823625804E215, " +
        "5.5502938327393044E249, 4.7147236359920616E284 }");
  }

  public void testTypeCheckInCompareToMethod() throws IOException {
    String translation = translateSourceFile(
        "class Test implements Comparable<Test> { int i; " +
        "  public int compareTo(Test t) { return i - t.i; } }", "Test", "Test.m");
    assertTranslation(translation, "#import \"java/lang/ClassCastException.h\"");
    assertTranslation(translation, "if (t != nil && ![t isKindOfClass:[Test class]])");
    assertTranslation(translation,
        "@throw [[[JavaLangClassCastException alloc] init] autorelease]");
  }

  public void testAdditionWithinStringConcatenation() throws IOException {
    String translation = translateSourceFile(
        "class Test { void test() { String s = 1 + 2.3f + \"foo\"; } }", "Test", "test.m");
    assertTranslation(translation,
        "NSString *s = [NSString stringWithFormat:@\"%ffoo\", 1 + 2.3f]");
  }
}
