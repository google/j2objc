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

import com.google.devtools.j2objc.jdt.BindingConverter;
import com.google.devtools.j2objc.types.LambdaTypeBinding;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Lambda expression AST node type (added in JLS8, section 15.27).
 */
public class LambdaExpression extends Expression {

  private TypeMirror typeMirror;
  // Unique type binding that can be used as a key.
  private final LambdaTypeBinding lambdaTypeBinding;
  private ChildList<VariableDeclaration> parameters = ChildList.create(VariableDeclaration.class,
      this);
  protected ChildLink<TreeNode> body = ChildLink.create(TreeNode.class, this);
  // The unique name that is used as a key when dynamically creating the lambda type.
  private String uniqueName = null;
  private boolean isCapturing = false;

  public LambdaExpression() {
    // Generate a type binding which is unique to the lambda, as resolveTypeBinding gives us a
    // generic raw type of the implemented class.
    lambdaTypeBinding = new LambdaTypeBinding("LambdaExpression:" + this.getLineNumber());
  }

  public LambdaExpression(LambdaExpression other) {
    super(other);
    typeMirror = other.getTypeMirror();
    lambdaTypeBinding = other.getLambdaTypeBinding();
    parameters.copyFrom(other.getParameters());
    body.copyFrom(other.getBody());
    uniqueName = other.getUniqueName();
    isCapturing = other.isCapturing();
  }

  public LambdaExpression(String name, ITypeBinding typeBinding) {
    typeMirror = BindingConverter.getType(typeBinding);
    lambdaTypeBinding = new LambdaTypeBinding(name);
  }

  @Override
  public Kind getKind() {
    return Kind.LAMBDA_EXPRESSION;
  }

  public String getUniqueName() {
    return uniqueName;
  }

  public LambdaExpression setUniqueName(String newUniqueName) {
    lambdaTypeBinding.setName(newUniqueName);
    uniqueName = newUniqueName;
    return this;
  }

  @Override
  public TypeMirror getTypeMirror() {
    return typeMirror;
  }

  public LambdaExpression setTypeMirror(TypeMirror t) {
    typeMirror = t;
    return this;
  }

  public LambdaTypeBinding getLambdaTypeBinding() {
    return lambdaTypeBinding;
  }

  public TypeElement getLambdaType() {
    return BindingConverter.getTypeElement(lambdaTypeBinding);
  }

  public TypeMirror getLambdaTypeMirror() {
    return BindingConverter.getType(lambdaTypeBinding);
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

  public LambdaExpression setIsCapturing(boolean isCapturing) {
    this.isCapturing = isCapturing;
    return this;
  }

  public LambdaExpression addParameter(VariableDeclaration param) {
    parameters.add(param);
    return this;
  }
}
