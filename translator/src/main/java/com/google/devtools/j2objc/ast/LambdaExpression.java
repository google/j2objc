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

import com.google.common.collect.Lists;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.List;

public class LambdaExpression extends Expression {
  private final ITypeBinding typeBinding;
  private final IMethodBinding methodBinding;
  private List<VariableDeclarationFragment> parameters;
  protected ChildLink<TreeNode> body = ChildLink.create(TreeNode.class, this);

  public LambdaExpression(org.eclipse.jdt.core.dom.LambdaExpression jdtNode) {
    super(jdtNode);
    typeBinding = jdtNode.resolveTypeBinding();
    methodBinding = jdtNode.resolveMethodBinding();
    parameters = Lists.newArrayListWithExpectedSize(jdtNode.parameters().size());
    for (Object x : jdtNode.parameters()) {
      parameters.add(new VariableDeclarationFragment(
          (org.eclipse.jdt.core.dom.VariableDeclarationFragment) x));
    }
    // Lambda bodies can either be a block or an expression, which forces a common root of TreeNode.
    body.set(TreeConverter.convert(jdtNode.getBody()));
    // System.out.println(this);
  }

  public LambdaExpression(LambdaExpression other) {
    super(other);
    typeBinding = other.getTypeBinding();
    methodBinding = other.getMethodBinding();
    parameters = other.parameters();
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
      body.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public LambdaExpression copy() {
    return new LambdaExpression(this);
  }
}
