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
  private ChildList<VariableDeclarationFragment> parameters = ChildList.create(
      VariableDeclarationFragment.class, this);
  protected ChildLink<TreeNode> body = ChildLink.create(TreeNode.class, this);

  public LambdaExpression(org.eclipse.jdt.core.dom.LambdaExpression jdtNode) {
    super(jdtNode);
    typeBinding = jdtNode.resolveTypeBinding();
    methodBinding = jdtNode.resolveMethodBinding();
    for (Object x : jdtNode.parameters()) {
      parameters.add((VariableDeclarationFragment) TreeConverter.convert(x));
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

  public List<VariableDeclarationFragment> parameters() {
    return parameters;
  }

  public TreeNode getBody() {
    return body.get();
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      parameters.accept(visitor);
      body.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public LambdaExpression copy() {
    return new LambdaExpression(this);
  }
}
