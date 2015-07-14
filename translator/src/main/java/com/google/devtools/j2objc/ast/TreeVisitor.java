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

package com.google.devtools.j2objc.ast;

import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.NameTable;

/**
 * Base visitor class for the J2ObjC tree.
 */
public class TreeVisitor {

  protected CompilationUnit unit = null;
  protected Types typeEnv = null;
  protected NameTable nameTable = null;

  /**
   * Executes this visitor on a specified node.  This entry point should
   * be used instead of visit(), so that certain state can be initialized.
   *
   * @param node the top-level node to visit.
   */
  public void run(TreeNode node) {
    unit = TreeUtil.getCompilationUnit(node);
    typeEnv = unit.getTypeEnv();
    nameTable = unit.getNameTable();
    node.accept(this);
    unit = null;
    typeEnv = null;
    nameTable = null;
  }

  public boolean preVisit(TreeNode node) {
    return true;
  }

  public void postVisit(TreeNode node) {}

  public boolean visit(AnnotationTypeDeclaration node) {
    return true;
  }

  public void endVisit(AnnotationTypeDeclaration node) {}

  public boolean visit(AnnotationTypeMemberDeclaration node) {
    return true;
  }

  public void endVisit(AnnotationTypeMemberDeclaration node) {}

  public boolean visit(AnonymousClassDeclaration node) {
    return true;
  }

  public void endVisit(AnonymousClassDeclaration node) {}

  public boolean visit(ArrayAccess node) {
    return true;
  }

  public void endVisit(ArrayAccess node) {}

  public boolean visit(ArrayCreation node) {
    return true;
  }

  public void endVisit(ArrayCreation node) {}

  public boolean visit(ArrayInitializer node) {
    return true;
  }

  public void endVisit(ArrayInitializer node) {}

  public boolean visit(ArrayType node) {
    return true;
  }

  public void endVisit(ArrayType node) {}

  public boolean visit(AssertStatement node) {
    return true;
  }

  public void endVisit(AssertStatement node) {}

  public boolean visit(Assignment node) {
    return true;
  }

  public void endVisit(Assignment node) {}

  public boolean visit(Block node) {
    return true;
  }

  public void endVisit(Block node) {}

  public boolean visit(BlockComment node) {
    return true;
  }

  public void endVisit(BlockComment node) {}

  public boolean visit(BooleanLiteral node) {
    return true;
  }

  public void endVisit(BooleanLiteral node) {}

  public boolean visit(BreakStatement node) {
    return true;
  }

  public void endVisit(BreakStatement node) {}

  public boolean visit(CStringLiteral node) {
    return true;
  }

  public void endVisit(CStringLiteral node) {}

  public boolean visit(CastExpression node) {
    return true;
  }

  public void endVisit(CastExpression node) {}

  public boolean visit(CatchClause node) {
    return true;
  }

  public void endVisit(CatchClause node) {}

  public boolean visit(CharacterLiteral node) {
    return true;
  }

  public void endVisit(CharacterLiteral node) {}

  public boolean visit(ClassInstanceCreation node) {
    return true;
  }

  public void endVisit(ClassInstanceCreation node) {}

  public boolean visit(CommaExpression node) {
    return true;
  }

  public void endVisit(CommaExpression node) {}

  public boolean visit(CompilationUnit node) {
    return true;
  }

  public void endVisit(CompilationUnit node) {}

  public boolean visit(ConditionalExpression node) {
    return true;
  }

  public void endVisit(ConditionalExpression node) {}

  public boolean visit(ConstructorInvocation node) {
    return true;
  }

  public void endVisit(ConstructorInvocation node) {}

  public boolean visit(ContinueStatement node) {
    return true;
  }

  public void endVisit(ContinueStatement node) {}

  public boolean visit(CreationReference node) {
    return true;
  }

  public void endVisit(CreationReference node) {}

  public boolean visit(DoStatement node) {
    return true;
  }

  public void endVisit(DoStatement node) {}

  public boolean visit(Dimension node) {
    return true;
  }

  public void endVisit(Dimension node) {}

  public boolean visit(EmptyStatement node) {
    return true;
  }

  public void endVisit(EmptyStatement node) {}

  public boolean visit(EnhancedForStatement node) {
    return true;
  }

  public void endVisit(EnhancedForStatement node) {}

  public boolean visit(EnumConstantDeclaration node) {
    return true;
  }

  public void endVisit(EnumConstantDeclaration node) {}

  public boolean visit(EnumDeclaration node) {
    return true;
  }

  public void endVisit(EnumDeclaration node) {}

  public boolean visit(ExpressionMethodReference node) {
    return true;
  }

  public void endVisit(ExpressionMethodReference node) {}

  public boolean visit(ExpressionStatement node) {
    return true;
  }

  public void endVisit(ExpressionStatement node) {}

  public boolean visit(FieldAccess node) {
    return true;
  }

  public void endVisit(FieldAccess node) {}

  public boolean visit(FieldDeclaration node) {
    return true;
  }

  public void endVisit(FieldDeclaration node) {}

  public boolean visit(ForStatement node) {
    return true;
  }

  public void endVisit(ForStatement node) {}

  public boolean visit(FunctionDeclaration node) {
    return true;
  }

  public void endVisit(FunctionDeclaration node) {}

  public boolean visit(FunctionInvocation node) {
    return true;
  }

  public void endVisit(FunctionInvocation node) {}

  public boolean visit(IfStatement node) {
    return true;
  }

  public void endVisit(IfStatement node) {}

  public boolean visit(InfixExpression node) {
    return true;
  }

  public void endVisit(InfixExpression node) {}

  public boolean visit(Initializer node) {
    return true;
  }

  public void endVisit(Initializer node) {}

  public boolean visit(InstanceofExpression node) {
    return true;
  }

  public void endVisit(InstanceofExpression node) {}

  public boolean visit(IntersectionType node) {
    return true;
  }

  public void endVisit(IntersectionType node) {}

  public boolean visit(Javadoc node) {
    // By default don't visit javadoc nodes because they aren't code.
    // This is consistent with JDT's base visitor class.
    return false;
  }

  public void endVisit(Javadoc node) {}

  public boolean visit(LabeledStatement node) {
    return true;
  }

  public void endVisit(LabeledStatement node) {}

  public boolean visit(LambdaExpression node) {
    return true;
  }

  public void endVisit(LambdaExpression node) {}

  public boolean visit(LineComment node) {
    return true;
  }

  public void endVisit(LineComment node) {}

  public boolean visit(MarkerAnnotation node) {
    return true;
  }

  public void endVisit(MarkerAnnotation node) {}

  public boolean visit(MemberValuePair node) {
    return true;
  }

  public void endVisit(MemberValuePair node) {}

  public boolean visit(MethodDeclaration node) {
    return true;
  }

  public void endVisit(MethodDeclaration node) {}

  public boolean visit(MethodInvocation node) {
    return true;
  }

  public void endVisit(MethodInvocation node) {}

  public boolean visit(NameQualifiedType node) {
    return true;
  }

  public void endVisit(NameQualifiedType node) {}

  public boolean visit(NativeDeclaration node) {
    return true;
  }

  public void endVisit(NativeDeclaration node) {}

  public boolean visit(NativeExpression node) {
    return true;
  }

  public void endVisit(NativeExpression node) {}

  public boolean visit(NativeStatement node) {
    return true;
  }

  public void endVisit(NativeStatement node) {}

  public boolean visit(NormalAnnotation node) {
    return true;
  }

  public void endVisit(NormalAnnotation node) {}

  public boolean visit(NullLiteral node) {
    return true;
  }

  public void endVisit(NullLiteral node) {}

  public boolean visit(NumberLiteral node) {
    return true;
  }

  public void endVisit(NumberLiteral node) {}

  public boolean visit(PackageDeclaration node) {
    return true;
  }

  public void endVisit(PackageDeclaration node) {}

  public boolean visit(ParameterizedType node) {
    return true;
  }

  public void endVisit(ParameterizedType node) {}

  public boolean visit(ParenthesizedExpression node) {
    return true;
  }

  public void endVisit(ParenthesizedExpression node) {}

  public boolean visit(PostfixExpression node) {
    return true;
  }

  public void endVisit(PostfixExpression node) {}

  public boolean visit(PrefixExpression node) {
    return true;
  }

  public void endVisit(PrefixExpression node) {}

  public boolean visit(PrimitiveType node) {
    return true;
  }

  public void endVisit(PrimitiveType node) {}

  public boolean visit(PropertyAnnotation node) {
    return true;
  }

  public void endVisit(PropertyAnnotation node) {}

  public boolean visit(QualifiedName node) {
    return true;
  }

  public void endVisit(QualifiedName node) {}

  public boolean visit(QualifiedType node) {
    return true;
  }

  public void endVisit(QualifiedType node) {}

  public boolean visit(ReturnStatement node) {
    return true;
  }

  public void endVisit(ReturnStatement node) {}

  public boolean visit(SimpleName node) {
    return true;
  }

  public void endVisit(SimpleName node) {}

  public boolean visit(SimpleType node) {
    return true;
  }

  public void endVisit(SimpleType node) {}

  public boolean visit(SingleMemberAnnotation node) {
    return true;
  }

  public void endVisit(SingleMemberAnnotation node) {}

  public boolean visit(SingleVariableDeclaration node) {
    return true;
  }

  public void endVisit(SingleVariableDeclaration node) {}

  public boolean visit(StringLiteral node) {
    return true;
  }

  public void endVisit(StringLiteral node) {}

  public boolean visit(SuperConstructorInvocation node) {
    return true;
  }

  public void endVisit(SuperConstructorInvocation node) {}

  public boolean visit(SuperMethodInvocation node) {
    return true;
  }

  public void endVisit(SuperMethodInvocation node) {}

  public boolean visit(SuperMethodReference node) {
    return true;
  }

  public void endVisit(SuperMethodReference node) {}

  public boolean visit(SuperFieldAccess node) {
    return true;
  }

  public void endVisit(SuperFieldAccess node) {}

  public boolean visit(SwitchCase node) {
    return true;
  }

  public void endVisit(SwitchCase node) {}

  public boolean visit(SwitchStatement node) {
    return true;
  }

  public void endVisit(SwitchStatement node) {}

  public boolean visit(SynchronizedStatement node) {
    return true;
  }

  public void endVisit(SynchronizedStatement node) {}

  public boolean visit(TagElement node) {
    return true;
  }

  public void endVisit(TagElement node) {}

  public boolean visit(TextElement node) {
    return true;
  }

  public void endVisit(TextElement node) {}

  public boolean visit(ThisExpression node) {
    return true;
  }

  public void endVisit(ThisExpression node) {}

  public boolean visit(ThrowStatement node) {
    return true;
  }

  public void endVisit(ThrowStatement node) {}

  public boolean visit(TryStatement node) {
    return true;
  }

  public void endVisit(TryStatement node) {}

  public boolean visit(TypeDeclaration node) {
    return true;
  }

  public void endVisit(TypeDeclaration node) {}

  public boolean visit(TypeDeclarationStatement node) {
    return true;
  }

  public void endVisit(TypeDeclarationStatement node) {}

  public boolean visit(TypeLiteral node) {
    return true;
  }

  public void endVisit(TypeLiteral node) {}

  public boolean visit(TypeMethodReference node) {
    return true;
  }

  public void endVisit(TypeMethodReference node) {}

  public boolean visit(UnionType node) {
    return true;
  }

  public void endVisit(UnionType node) {}

  public boolean visit(VariableDeclarationExpression node) {
    return true;
  }

  public void endVisit(VariableDeclarationExpression node) {}

  public boolean visit(VariableDeclarationFragment node) {
    return true;
  }

  public void endVisit(VariableDeclarationFragment node) {}

  public boolean visit(VariableDeclarationStatement node) {
    return true;
  }

  public void endVisit(VariableDeclarationStatement node) {}

  public boolean visit(WhileStatement node) {
    return true;
  }

  public void endVisit(WhileStatement node) {}
}
