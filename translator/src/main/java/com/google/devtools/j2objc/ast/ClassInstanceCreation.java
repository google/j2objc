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
 * Node type for constructing a new instance. (e.g. "new Foo()")
 */
public class ClassInstanceCreation extends Expression {

  private IMethodBinding methodBinding = null;
  // Indicates that this expression leaves the created object with a retain
  // count of 1. (i.e. does not call autorelease)
  private boolean hasRetainedResult = false;
  private ChildLink<Expression> expression = ChildLink.create(Expression.class, this);
  private ChildLink<Type> type = ChildLink.create(Type.class, this);
  private ChildList<Expression> arguments = ChildList.create(Expression.class, this);
  private ChildLink<AnonymousClassDeclaration> anonymousClassDeclaration =
      ChildLink.create(AnonymousClassDeclaration.class, this);

  public ClassInstanceCreation(org.eclipse.jdt.core.dom.ClassInstanceCreation jdtNode) {
    super(jdtNode);
    methodBinding = jdtNode.resolveConstructorBinding();
    expression.set((Expression) TreeConverter.convert(jdtNode.getExpression()));
    type.set((Type) TreeConverter.convert(jdtNode.getType()));
    for (Object argument : jdtNode.arguments()) {
      arguments.add((Expression) TreeConverter.convert(argument));
    }
    anonymousClassDeclaration.set(
        (AnonymousClassDeclaration) TreeConverter.convert(jdtNode.getAnonymousClassDeclaration()));
  }

  public ClassInstanceCreation(ClassInstanceCreation other) {
    super(other);
    methodBinding = other.getMethodBinding();
    hasRetainedResult = other.hasRetainedResult();
    expression.copyFrom(other.getExpression());
    type.copyFrom(other.getType());
    arguments.copyFrom(other.getArguments());
    anonymousClassDeclaration.copyFrom(other.getAnonymousClassDeclaration());
  }

  public ClassInstanceCreation(IMethodBinding methodBinding, Type type) {
    this.methodBinding = methodBinding;
    this.type.set(type);
  }

  public ClassInstanceCreation(IMethodBinding methodBinding) {
    this.methodBinding = methodBinding;
    type.set(Type.newType(methodBinding.getDeclaringClass()));
  }

  @Override
  public Kind getKind() {
    return Kind.CLASS_INSTANCE_CREATION;
  }

  public IMethodBinding getMethodBinding() {
    return methodBinding;
  }

  public void setMethodBinding(IMethodBinding newMethodBinding) {
    methodBinding = newMethodBinding;
  }

  @Override
  public ITypeBinding getTypeBinding() {
    return methodBinding != null ? methodBinding.getDeclaringClass() : null;
  }

  public boolean hasRetainedResult() {
    return hasRetainedResult;
  }

  public void setHasRetainedResult(boolean hasRetainedResult) {
    this.hasRetainedResult = hasRetainedResult;
  }

  public Expression getExpression() {
    return expression.get();
  }

  public void setExpression(Expression newExpression) {
    expression.set(newExpression);
  }

  public Type getType() {
    return type.get();
  }

  public void setType(Type newType) {
    type.set(newType);
  }

  public List<Expression> getArguments() {
    return arguments;
  }

  public AnonymousClassDeclaration getAnonymousClassDeclaration() {
    return anonymousClassDeclaration.get();
  }

  public void setAnonymousClassDeclaration(AnonymousClassDeclaration newAnonymousClassDeclaration) {
    anonymousClassDeclaration.set(newAnonymousClassDeclaration);
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      expression.accept(visitor);
      type.accept(visitor);
      arguments.accept(visitor);
      anonymousClassDeclaration.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public ClassInstanceCreation copy() {
    return new ClassInstanceCreation(this);
  }
}
