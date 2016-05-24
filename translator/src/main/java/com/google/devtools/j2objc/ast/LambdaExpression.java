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

import com.google.devtools.j2objc.types.LambdaTypeBinding;

import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.List;

/**
 * Lambda expression AST node type (added in JLS8, section 15.27).
 */
public class LambdaExpression extends Expression {

  private final ITypeBinding typeBinding;
  // Unique type binding that can be used as a key.
  private final ITypeBinding lambdaTypeBinding;
  private ChildList<VariableDeclaration> parameters = ChildList.create(VariableDeclaration.class,
      this);
  protected ChildLink<TreeNode> body = ChildLink.create(TreeNode.class, this);
  // The unique name that is used as a key when dynamically creating the lambda type.
  private String uniqueName = null;
  private boolean isCapturing = false;

  public LambdaExpression(org.eclipse.jdt.core.dom.LambdaExpression jdtNode) {
    super(jdtNode);
    typeBinding = jdtNode.resolveTypeBinding();
    for (Object x : jdtNode.parameters()) {
      parameters.add((VariableDeclaration) TreeConverter.convert(x));
    }
    // Lambda bodies can either be a block or an expression, which forces a common root of TreeNode.
    body.set(TreeConverter.convert(jdtNode.getBody()));
    // Generate a type binding which is unique to the lambda, as resolveTypeBinding gives us a
    // generic raw type of the implemented class.
    lambdaTypeBinding = new LambdaTypeBinding("LambdaExpression:" + this.getLineNumber());
  }

  public LambdaExpression(LambdaExpression other) {
    super(other);
    typeBinding = other.getTypeBinding();
    lambdaTypeBinding = other.getLambdaTypeBinding();
    parameters.copyFrom(other.getParameters());
    body.copyFrom(other.getBody());
    uniqueName = other.getUniqueName();
    isCapturing = other.isCapturing();
  }

  public LambdaExpression(String name, ITypeBinding typeBinding) {
    this.typeBinding = typeBinding;
    lambdaTypeBinding = new LambdaTypeBinding(name);
  }

  @Override
  public Kind getKind() {
    return Kind.LAMBDA_EXPRESSION;
  }

  public String getUniqueName() {
    return uniqueName;
  }

  public void setUniqueName(String newUniqueName) {
    uniqueName = newUniqueName;
  }

  @Override
  public ITypeBinding getTypeBinding() {
    return typeBinding;
  }

  public ITypeBinding getLambdaTypeBinding() {
    return lambdaTypeBinding;
  }

  public List<VariableDeclaration> getParameters() {
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
      parameters.accept(visitor);
      body.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public LambdaExpression copy() {
    return new LambdaExpression(this);
  }

  public boolean isCapturing() {
    return isCapturing;
  }

  public void setIsCapturing(boolean isCapturing) {
    this.isCapturing = isCapturing;
  }
}
