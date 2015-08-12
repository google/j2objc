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

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.List;

/**
 * Lambda expression AST node type (added in JLS8, section 15.27).
 */
public class LambdaExpression extends Expression {

  private final ITypeBinding resolvedTypeBinding;
  // Unique type binding that can be used as a key.
  private final ITypeBinding generatedTypeBinding;
  private final IMethodBinding methodBinding;
  private ChildList<VariableDeclaration> parameters = ChildList.create(VariableDeclaration.class,
      this);
  protected ChildLink<TreeNode> body = ChildLink.create(TreeNode.class, this);
  private boolean isCapturing = false;

  public LambdaExpression(org.eclipse.jdt.core.dom.LambdaExpression jdtNode) {
    super(jdtNode);
    resolvedTypeBinding = jdtNode.resolveTypeBinding();
    methodBinding = jdtNode.resolveMethodBinding();
    for (Object x : jdtNode.parameters()) {
      parameters.add((VariableDeclaration) TreeConverter.convert(x));
    }
    // Lambda bodies can either be a block or an expression, which forces a common root of TreeNode.
    body.set(TreeConverter.convert(jdtNode.getBody()));
    // Generate a type binding which is unique to the lambda, as resolveTypeBinding gives us a
    // generic raw type of the implemented class.
    String name = jdtNode.resolveMethodBinding().getName();
    IPackageBinding packageBinding = methodBinding.getDeclaringClass().getPackage();
    this.generatedTypeBinding = new LambdaTypeBinding(name, packageBinding,
        resolvedTypeBinding.getFunctionalInterfaceMethod());
  }

  public LambdaExpression(LambdaExpression other) {
    super(other);
    generatedTypeBinding = other.getTypeBinding();
    resolvedTypeBinding = other.functionalTypeBinding();
    methodBinding = other.getMethodBinding();
    parameters.copyFrom(other.getParameters());
    body.copyFrom(other.getBody());
    isCapturing = other.isCapturing();
  }

  @Override
  public Kind getKind() {
    return Kind.LAMBDA_EXPRESSION;
  }

  public ITypeBinding functionalTypeBinding() {
    return resolvedTypeBinding;
  }

  @Override
  public ITypeBinding getTypeBinding() {
    return generatedTypeBinding;
  }

  public IMethodBinding getMethodBinding() {
    return methodBinding;
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
