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

import com.google.common.base.Preconditions;
import com.google.devtools.j2objc.jdt.BindingConverter;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Method invocation node type.
 */
public class MethodInvocation extends Expression {

  private ExecutableElement method = null;
  private ExecutableType methodType = null;
  // The context-specific known type of this expression.
  private TypeMirror typeMirror = null;
  private ChildLink<Expression> expression = ChildLink.create(Expression.class, this);
  private ChildLink<SimpleName> name = ChildLink.create(SimpleName.class, this);
  private ChildList<Expression> arguments = ChildList.create(Expression.class, this);

  public MethodInvocation() {}

  public MethodInvocation(MethodInvocation other) {
    super(other);
    method = other.getExecutableElement();
    methodType = other.getExecutableType();
    typeMirror = other.getTypeMirror();
    expression.copyFrom(other.getExpression());
    name.copyFrom(other.getName());
    arguments.copyFrom(other.getArguments());
  }

  public MethodInvocation(IMethodBinding binding, ITypeBinding typeBinding, Expression expression) {
    method = BindingConverter.getExecutableElement(binding);
    methodType = BindingConverter.getType(binding);
    typeMirror = BindingConverter.getType(typeBinding);
    this.expression.set(expression);
    name.set(new SimpleName(binding));
  }

  public MethodInvocation(IMethodBinding binding, Expression expression) {
    this(binding, binding.getReturnType(), expression);
  }

  public MethodInvocation(
      ExecutableElement method, ExecutableType methodType, Expression expression) {
    this.method = method;
    this.methodType = methodType;
    typeMirror = methodType.getReturnType();
    this.expression.set(expression);
    name.set(new SimpleName(BindingConverter.unwrapElement(method)));
  }

  @Override
  public Kind getKind() {
    return Kind.METHOD_INVOCATION;
  }

  public IMethodBinding getMethodBinding() {
    return (IMethodBinding) BindingConverter.unwrapTypeMirrorIntoBinding(methodType);
  }

  public void setMethodBinding(IMethodBinding newMethodBinding) {
    method = BindingConverter.getExecutableElement(newMethodBinding);
    methodType = BindingConverter.getType(newMethodBinding);
  }

  public ExecutableElement getExecutableElement() {
    return method;
  }

  public MethodInvocation setExecutableElement(ExecutableElement newElement) {
    method = newElement;
    return this;
  }

  public ExecutableType getExecutableType() {
    return methodType;
  }

  public MethodInvocation setExecutableType(ExecutableType newType) {
    methodType = newType;
    return this;
  }

  @Override
  public TypeMirror getTypeMirror() {
    return typeMirror;
  }

  public MethodInvocation setTypeMirror(TypeMirror newMirror) {
    typeMirror = newMirror;
    return this;
  }

  public Expression getExpression() {
    return expression.get();
  }

  public MethodInvocation setExpression(Expression newExpression) {
    expression.set(newExpression);
    return this;
  }

  public SimpleName getName() {
    return name.get();
  }

  public MethodInvocation setName(SimpleName newName) {
    name.set(newName);
    return this;
  }

  public List<Expression> getArguments() {
    return arguments;
  }

  public MethodInvocation addArgument(Expression arg) {
    arguments.add(arg);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      expression.accept(visitor);
      name.accept(visitor);
      arguments.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public MethodInvocation copy() {
    return new MethodInvocation(this);
  }

  @Override
  public void validateInner() {
    super.validateInner();
    Preconditions.checkNotNull(method);
    Preconditions.checkNotNull(name.get());
  }
}
