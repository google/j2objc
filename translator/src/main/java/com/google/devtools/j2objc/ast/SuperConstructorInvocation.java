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
 * Node for a super constructor invocation. (i.e. "super(...);")
 */
public class SuperConstructorInvocation extends Statement {

  private ExecutablePair method = ExecutablePair.NULL;
  private TypeMirror varargsType = null;
  private final ChildLink<Expression> expression = ChildLink.create(Expression.class, this);
  private final ChildList<Expression> arguments = ChildList.create(Expression.class, this);

  public SuperConstructorInvocation() {}

  public SuperConstructorInvocation(SuperConstructorInvocation other) {
    super(other);
    method = other.getExecutablePair();
    varargsType = other.getVarargsType();
    expression.copyFrom(other.getExpression());
    arguments.copyFrom(other.getArguments());
  }

  public SuperConstructorInvocation(ExecutablePair method) {
    this.method = method;
  }

  @Override
  public Kind getKind() {
    return Kind.SUPER_CONSTRUCTOR_INVOCATION;
  }

  public ExecutablePair getExecutablePair() {
    return method;
  }

  public SuperConstructorInvocation setExecutablePair(ExecutablePair newMethod) {
    method = newMethod;
    return this;
  }

  public ExecutableElement getExecutableElement() {
    return method.element();
  }

  public ExecutableType getExecutableType() {
    return method.type();
  }

  public TypeMirror getVarargsType() {
    return varargsType;
  }

  public SuperConstructorInvocation setVarargsType(TypeMirror type) {
    varargsType = type;
    return this;
  }

  public Expression getExpression() {
    return expression.get();
  }

  public SuperConstructorInvocation setExpression(Expression newExpression) {
    expression.set(newExpression);
    return this;
  }

  public List<Expression> getArguments() {
    return arguments;
  }

  public SuperConstructorInvocation addArgument(Expression arg) {
    arguments.add(arg);
    return this;
  }

  public SuperConstructorInvocation addArgument(int index, Expression arg) {
    arguments.add(index, arg);
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
  public SuperConstructorInvocation copy() {
    return new SuperConstructorInvocation(this);
  }
}
