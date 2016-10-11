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

import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Lambda expression AST node type (added in JLS8, section 15.27).
 */
public class LambdaExpression extends FunctionalExpression {

  private ChildList<VariableDeclaration> parameters = ChildList.create(VariableDeclaration.class,
      this);
  protected ChildLink<TreeNode> body = ChildLink.create(TreeNode.class, this);

  public LambdaExpression() {
  }

  public LambdaExpression(LambdaExpression other) {
    super(other);
    parameters.copyFrom(other.getParameters());
    body.copyFrom(other.getBody());
  }

  @Override
  public Kind getKind() {
    return Kind.LAMBDA_EXPRESSION;
  }

  public List<VariableDeclaration> getParameters() {
    return parameters;
  }

  public TreeNode getBody() {
    return body.get();
  }

  public LambdaExpression setBody(TreeNode newBody) {
    body.set(newBody);
    return this;
  }

  @Override
  public LambdaExpression setTypeMirror(TypeMirror t) {
    return (LambdaExpression) super.setTypeMirror(t);
  }

  @Override
  public LambdaExpression setTypeElement(TypeElement e) {
    return (LambdaExpression) super.setTypeElement(e);
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      lambdaOuterArg.accept(visitor);
      lambdaCaptureArgs.accept(visitor);
      parameters.accept(visitor);
      body.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public LambdaExpression copy() {
    return new LambdaExpression(this);
  }

  public LambdaExpression addParameter(VariableDeclaration param) {
    parameters.add(param);
    return this;
  }
}
