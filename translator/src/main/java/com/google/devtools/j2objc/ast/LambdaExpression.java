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

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.List;

/**
 * Lambda expression AST node type (added in JLS8, section 15.27).
 */
public class LambdaExpression extends Expression {

  private final ITypeBinding typeBinding;
  private final IMethodBinding methodBinding;
  private ChildList<VariableDeclaration> parameters = ChildList.create(VariableDeclaration.class,
      this);
  protected ChildLink<TreeNode> body = ChildLink.create(TreeNode.class, this);
  // TODO(kirbs) Remove when no longer needed for debugging.
  public boolean fromAnonClass = false;

  public LambdaExpression(org.eclipse.jdt.core.dom.LambdaExpression jdtNode) {
    super(jdtNode);
    typeBinding = jdtNode.resolveTypeBinding();
    methodBinding = jdtNode.resolveMethodBinding();
    for (Object x : jdtNode.parameters()) {
      parameters.add((VariableDeclaration) TreeConverter.convert(x));
    }
    // Lambda bodies can either be a block or an expression, which forces a common root of TreeNode.
    body.set(TreeConverter.convert(jdtNode.getBody()));
  }

  public LambdaExpression(LambdaExpression other) {
    super(other);
    typeBinding = other.getTypeBinding();
    methodBinding = other.getMethodBinding();
    parameters.copyFrom(other.parameters());
    body.copyFrom(other.getBody());
  }

  // Added for conversion of Anonymous Classes to Lambda Expressions.
  public LambdaExpression(ITypeBinding typeBinding, IMethodBinding methodBinding,
      List<VariableDeclarationFragment> parameters, Block body, boolean fromAnonClass) {
    this.typeBinding = typeBinding;
    this.methodBinding = methodBinding;
    this.parameters.addAll(parameters);
    this.body.set(body);
    this.fromAnonClass = fromAnonClass;
  }

  // Added for conversion of Anonymous Classes to Lambda Expressions.
  public LambdaExpression(ITypeBinding typeBinding, IMethodBinding methodBinding,
      boolean fromAnonClass) {
    this.typeBinding = typeBinding;
    this.methodBinding = methodBinding;
    this.fromAnonClass = fromAnonClass;
  }

  @Override
  public Kind getKind() {
    return Kind.LAMBDA_EXPRESSION;
  }

  @Override
  public ITypeBinding getTypeBinding() {
    return typeBinding;
  }

  public IMethodBinding getMethodBinding() {
    return methodBinding;
  }

  public List<VariableDeclaration> parameters() {
    return parameters;
  }

  public TreeNode getBody() {
    return body.get();
  }

  public void setBody(TreeNode newBody) {
    body.set(newBody);
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      body.accept(visitor);
    }
    visitor.endVisit(this);
  }

  // For retrieving functionalInterfaceMethod of Anonymous Classes.
  // TODO(kirbs): Handle this more naturally.
  public IMethodBinding getFunctionalInterfaceMethod() {
    if (typeBinding.getFunctionalInterfaceMethod() != null) {
      return typeBinding.getFunctionalInterfaceMethod();
    }
    return methodBinding;
  }

  @Override
  public LambdaExpression copy() {
    return new LambdaExpression(this);
  }
}
