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

import com.google.devtools.j2objc.types.ExecutablePair;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

/**
 * Node type for constructing a new instance. (e.g. "new Foo()")
 */
public class ClassInstanceCreation extends Expression {

  private ExecutablePair method = ExecutablePair.NULL;
  private TypeMirror varargsType = null;
  // Indicates that this expression leaves the created object with a retain
  // count of 1. (i.e. does not call autorelease)
  private boolean hasRetainedResult = false;
  private final ChildLink<Expression> expression = ChildLink.create(Expression.class, this);
  private final ChildList<Expression> captureArgs = ChildList.create(Expression.class, this);
  private final ChildLink<Type> type = ChildLink.create(Type.class, this);
  private final ChildList<Expression> arguments = ChildList.create(Expression.class, this);
  private final ChildLink<TypeDeclaration> anonymousClassDeclaration =
      ChildLink.create(TypeDeclaration.class, this);

  public ClassInstanceCreation() {}

  public ClassInstanceCreation(ClassInstanceCreation other) {
    super(other);
    method = other.getExecutablePair();
    varargsType = other.getVarargsType();
    hasRetainedResult = other.hasRetainedResult();
    expression.copyFrom(other.getExpression());
    captureArgs.copyFrom(other.getCaptureArgs());
    type.copyFrom(other.getType());
    arguments.copyFrom(other.getArguments());
    anonymousClassDeclaration.copyFrom(other.getAnonymousClassDeclaration());
  }

  public ClassInstanceCreation(ExecutablePair method, TypeMirror type) {
    this.method = method;
    this.type.set(Type.newType(type));
  }

  public ClassInstanceCreation(ExecutablePair method) {
    this(method, method.element().getEnclosingElement().asType());
  }

  @Override
  public Kind getKind() {
    return Kind.CLASS_INSTANCE_CREATION;
  }

  public ExecutablePair getExecutablePair() {
    return method;
  }

  public ClassInstanceCreation setExecutablePair(ExecutablePair newMethod) {
    method = newMethod;
    return this;
  }

  public ExecutableElement getExecutableElement() {
    return method.element();
  }

  public ExecutableType getExecutableType() {
    return method.type();
  }

  @Override
  public TypeMirror getTypeMirror() {
    Type typeNode = type.get();
    return typeNode != null ? typeNode.getTypeMirror() : null;
  }

  public TypeMirror getVarargsType() {
    return varargsType;
  }

  public ClassInstanceCreation setVarargsType(TypeMirror type) {
    varargsType = type;
    return this;
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

  public ClassInstanceCreation setExpression(Expression newExpression) {
    expression.set(newExpression);
    return this;
  }

  public List<Expression> getCaptureArgs() {
    return captureArgs;
  }

  public Type getType() {
    return type.get();
  }

  public ClassInstanceCreation setType(Type newType) {
    type.set(newType);
    return this;
  }

  public ClassInstanceCreation addArgument(Expression arg) {
    arguments.add(arg);
    return this;
  }

  public ClassInstanceCreation addArgument(int index, Expression arg) {
    arguments.add(index, arg);
    return this;
  }

  public Expression getArgument(int index) {
    return arguments.get(index);
  }

  public List<Expression> getArguments() {
    return arguments;
  }

  public TypeDeclaration getAnonymousClassDeclaration() {
    return anonymousClassDeclaration.get();
  }

  public ClassInstanceCreation setAnonymousClassDeclaration(
      TypeDeclaration newAnonymousClassDeclaration) {
    anonymousClassDeclaration.set(newAnonymousClassDeclaration);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      expression.accept(visitor);
      captureArgs.accept(visitor);
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
