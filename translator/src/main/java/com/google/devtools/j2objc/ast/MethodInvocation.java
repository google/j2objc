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
import com.google.devtools.j2objc.types.ExecutablePair;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

/**
 * Method invocation node type.
 */
public class MethodInvocation extends Expression {

  private ExecutablePair method = ExecutablePair.NULL;
  // The context-specific known type of this expression.
  private TypeMirror typeMirror = null;
  private TypeMirror varargsType = null;
  private final ChildLink<Expression> expression = ChildLink.create(Expression.class, this);
  private final ChildList<Expression> arguments = ChildList.create(Expression.class, this);

  public MethodInvocation() {}

  public MethodInvocation(MethodInvocation other) {
    super(other);
    method = other.getExecutablePair();
    typeMirror = other.getTypeMirror();
    varargsType = other.getVarargsType();
    expression.copyFrom(other.getExpression());
    arguments.copyFrom(other.getArguments());
  }

  public MethodInvocation(ExecutablePair method, TypeMirror typeMirror, Expression expression) {
    this.method = method;
    this.typeMirror = typeMirror;
    this.expression.set(expression);
  }

  public MethodInvocation(ExecutablePair method, Expression expression) {
    this(method, method.type().getReturnType(), expression);
  }

  @Override
  public Kind getKind() {
    return Kind.METHOD_INVOCATION;
  }

  public ExecutablePair getExecutablePair() {
    return method;
  }

  public MethodInvocation setExecutablePair(ExecutablePair newMethod) {
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
    return typeMirror;
  }

  public MethodInvocation setTypeMirror(TypeMirror newMirror) {
    typeMirror = newMirror;
    return this;
  }

  public TypeMirror getVarargsType() {
    return varargsType;
  }

  public MethodInvocation setVarargsType(TypeMirror type) {
    varargsType = type;
    return this;
  }

  public Expression getExpression() {
    return expression.get();
  }

  public MethodInvocation setExpression(Expression newExpression) {
    expression.set(newExpression);
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
    Preconditions.checkNotNull(typeMirror);
  }
}
