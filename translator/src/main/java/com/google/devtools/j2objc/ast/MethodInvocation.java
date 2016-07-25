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
import com.google.devtools.j2objc.jdt.TreeConverter;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Method invocation node type.
 */
public class MethodInvocation extends Expression {

  private ExecutableElement method = null;
  // The context-specific known type of this expression.
  private TypeMirror typeMirror = null;
  private ChildLink<Expression> expression = ChildLink.create(Expression.class, this);
  private ChildLink<SimpleName> name = ChildLink.create(SimpleName.class, this);
  private ChildList<Expression> arguments = ChildList.create(Expression.class, this);

  public MethodInvocation(org.eclipse.jdt.core.dom.MethodInvocation jdtNode) {
    super(jdtNode);
    IMethodBinding methodBinding = BindingConverter.wrapBinding(jdtNode.resolveMethodBinding());
    method = BindingConverter.getExecutableElement(methodBinding);
    ITypeBinding typeBinding = BindingConverter.wrapBinding(jdtNode.resolveTypeBinding());
    typeMirror = BindingConverter.getType(typeBinding);
    expression.set((Expression) TreeConverter.convert(jdtNode.getExpression()));
    name.set((SimpleName) TreeConverter.convert(jdtNode.getName()));
    for (Object argument : jdtNode.arguments()) {
      arguments.add((Expression) TreeConverter.convert(argument));
    }
  }

  public MethodInvocation(MethodInvocation other) {
    super(other);
    method = other.getExecutableElement();
    typeMirror = other.getTypeMirror();
    expression.copyFrom(other.getExpression());
    name.copyFrom(other.getName());
    arguments.copyFrom(other.getArguments());
  }

  public MethodInvocation(IMethodBinding binding, ITypeBinding typeBinding, Expression expression) {
    method = BindingConverter.getExecutableElement(binding);
    typeMirror = BindingConverter.getType(typeBinding);
    this.expression.set(expression);
    name.set(new SimpleName(binding));
  }

  public MethodInvocation(IMethodBinding binding, Expression expression) {
    this(binding, binding.getReturnType(), expression);
  }

  @Override
  public Kind getKind() {
    return Kind.METHOD_INVOCATION;
  }

  public IMethodBinding getMethodBinding() {
    return (IMethodBinding) BindingConverter.unwrapElement(method);
  }

  public void setMethodBinding(IMethodBinding newMethodBinding) {
    method = BindingConverter.getExecutableElement(newMethodBinding);
  }

  public ExecutableElement getExecutableElement() {
    return method;
  }

  @Override
  public TypeMirror getTypeMirror() {
    return typeMirror;
  }

  public void setTypeBinding(ITypeBinding newTypeBinding) {
    typeMirror = BindingConverter.getType(newTypeBinding);
  }

  public Expression getExpression() {
    return expression.get();
  }

  public void setExpression(Expression newExpression) {
    expression.set(newExpression);
  }

  public SimpleName getName() {
    return name.get();
  }

  public void setName(SimpleName newName) {
    name.set(newName);
  }

  public List<Expression> getArguments() {
    return arguments;
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

  public MethodInvocation addArgument(Expression arg) {
    arguments.add(arg);
    return this;
  }
}
