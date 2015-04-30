/*
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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.ast.AnonymousClassDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.InfixExpression;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.PostfixExpression;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeNode.Kind;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.util.BindingUtil;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import java.util.List;

/**
 * Unit tests for {@link OuterReferenceResolver}.
 *
 * @author Keith Stanger
 */
public class OuterReferenceResolverTest extends GenerationTest {

  private ListMultimap<Kind, TreeNode> nodesByType = ArrayListMultimap.create();

  @Override
  protected void tearDown() throws Exception {
    nodesByType.clear();
    OuterReferenceResolver.cleanup();
  }

  public void testOuterVarAccess() {
    resolveSource("Test", "class Test { int i; class Inner { void test() { i++; } } }");

    TypeDeclaration innerNode = (TypeDeclaration) nodesByType.get(Kind.TYPE_DECLARATION).get(1);
    assertTrue(OuterReferenceResolver.needsOuterReference(innerNode.getTypeBinding()));

    PostfixExpression increment =
        (PostfixExpression) nodesByType.get(Kind.POSTFIX_EXPRESSION).get(0);
    List<IVariableBinding> path = OuterReferenceResolver.getPath(increment.getOperand());
    assertNotNull(path);
    assertEquals(2, path.size());
    assertEquals("Test", path.get(0).getType().getName());
  }

  public void testInheritedOuterMethod() {
    resolveSource("Test",
        "class Test { class A { void foo() {} } class B extends A { "
        + "class Inner { void test() { foo(); } } } }");

    TypeDeclaration aNode = (TypeDeclaration) nodesByType.get(Kind.TYPE_DECLARATION).get(1);
    TypeDeclaration bNode = (TypeDeclaration) nodesByType.get(Kind.TYPE_DECLARATION).get(2);
    TypeDeclaration innerNode = (TypeDeclaration) nodesByType.get(Kind.TYPE_DECLARATION).get(3);
    assertFalse(OuterReferenceResolver.needsOuterReference(aNode.getTypeBinding()));
    assertFalse(OuterReferenceResolver.needsOuterReference(bNode.getTypeBinding()));
    assertTrue(OuterReferenceResolver.needsOuterReference(innerNode.getTypeBinding()));

    // B will need an outer reference to Test so it can initialize its
    // superclass A.
    List<IVariableBinding> bPath = OuterReferenceResolver.getPath(bNode);
    assertNotNull(bPath);
    assertEquals(1, bPath.size());
    assertEquals(OuterReferenceResolver.OUTER_PARAMETER, bPath.get(0));

    // foo() call will need to get to B's scope to call the inherited method.
    MethodInvocation fooCall = (MethodInvocation) nodesByType.get(Kind.METHOD_INVOCATION).get(0);
    List<IVariableBinding> fooPath = OuterReferenceResolver.getPath(fooCall);
    assertNotNull(fooPath);
    assertEquals(1, fooPath.size());
    assertEquals("B", fooPath.get(0).getType().getName());
  }

  public void testCapturedLocalVariable() {
    resolveSource("Test",
        "class Test { void test(final int i) { Runnable r = new Runnable() { "
        + "public void run() { int i2 = i + 1; } }; } }");

    AnonymousClassDeclaration runnableNode =
        (AnonymousClassDeclaration) nodesByType.get(Kind.ANONYMOUS_CLASS_DECLARATION).get(0);
    ITypeBinding runnableBinding = runnableNode.getTypeBinding();
    assertFalse(OuterReferenceResolver.needsOuterReference(runnableBinding));
    List<IVariableBinding> capturedVars = OuterReferenceResolver.getCapturedVars(runnableBinding);
    List<IVariableBinding> innerFields = OuterReferenceResolver.getInnerFields(runnableBinding);
    assertEquals(1, capturedVars.size());
    assertEquals(1, innerFields.size());
    assertEquals("i", capturedVars.get(0).getName());
    assertEquals("val$i", innerFields.get(0).getName());

    InfixExpression addition = (InfixExpression) nodesByType.get(Kind.INFIX_EXPRESSION).get(0);
    List<IVariableBinding> iPath = OuterReferenceResolver.getPath(addition.getLeftOperand());
    assertNotNull(iPath);
    assertEquals(1, iPath.size());
    assertEquals("val$i", iPath.get(0).getName());
  }

  public void testCapturedWeakLocalVariable() {
    resolveSource("Test",
        "import com.google.j2objc.annotations.Weak;"
        + "class Test { void test(@Weak final int i) { Runnable r = new Runnable() { "
        + "public void run() { int i2 = i + 1; } }; } }");

    AnonymousClassDeclaration runnableNode =
        (AnonymousClassDeclaration) nodesByType.get(Kind.ANONYMOUS_CLASS_DECLARATION).get(0);
    ITypeBinding runnableBinding = runnableNode.getTypeBinding();
    List<IVariableBinding> innerFields = OuterReferenceResolver.getInnerFields(runnableBinding);
    assertEquals(1, innerFields.size());
    assertTrue(BindingUtil.isWeakReference(innerFields.get(0)));
  }

  public void testAnonymousClassInheritsLocalClassInStaticMethod() {
    resolveSource("Test",
        "class Test { static void test() { class LocalClass {}; new LocalClass() {}; } }");

    AnonymousClassDeclaration decl =
        (AnonymousClassDeclaration) nodesByType.get(Kind.ANONYMOUS_CLASS_DECLARATION).get(0);
    ITypeBinding type = decl.getTypeBinding();
    assertFalse(OuterReferenceResolver.needsOuterParam(type));
  }

  private void resolveSource(String name, String source) {
    CompilationUnit unit = compileType(name, source);
    OuterReferenceResolver.resolve(unit);
    findTypeDeclarations(unit);
  }

  private void findTypeDeclarations(CompilationUnit unit) {
    unit.accept(new TreeVisitor() {
      @Override
      public boolean preVisit(TreeNode node) {
        nodesByType.put(node.getKind(), node);
        return true;
      }
    });
  }
}
