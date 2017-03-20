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
 * Node type for a method invocation on the super class. (e.g. "super.foo()")
 */
public class SuperMethodInvocation extends Expression {

  private ExecutablePair method = ExecutablePair.NULL;
  private TypeMirror varargsType = null;
  private final ChildLink<Name> qualifier = ChildLink.create(Name.class, this);
  // Resolved by OuterReferenceResolver.
  private final ChildLink<Expression> receiver = ChildLink.create(Expression.class, this);
  private final ChildList<Expression> arguments = ChildList.create(Expression.class, this);

  public SuperMethodInvocation() {}

  public SuperMethodInvocation(SuperMethodInvocation other) {
    super(other);
    method = other.getExecutablePair();
    varargsType = other.getVarargsType();
    qualifier.copyFrom(other.getQualifier());
    receiver.copyFrom(other.getReceiver());
    arguments.copyFrom(other.getArguments());
  }

  public SuperMethodInvocation(ExecutablePair method) {
    this.method = method;
  }

  @Override
  public Kind getKind() {
    return Kind.SUPER_METHOD_INVOCATION;
  }

  public ExecutablePair getExecutablePair() {
    return method;
  }

  public SuperMethodInvocation setExecutablePair(ExecutablePair newMethod) {
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
    return getExecutableType().getReturnType();
  }

  public TypeMirror getVarargsType() {
    return varargsType;
  }

  public SuperMethodInvocation setVarargsType(TypeMirror type) {
    varargsType = type;
    return this;
  }

  public Name getQualifier() {
    return qualifier.get();
  }

  public SuperMethodInvocation setQualifier(Name newQualifier) {
    qualifier.set(newQualifier);
    return this;
  }

  public Expression getReceiver() {
    return receiver.get();
  }

  public SuperMethodInvocation setReceiver(Expression newReceiver) {
    receiver.set(newReceiver);
    return this;
  }

  public List<Expression> getArguments() {
    return arguments;
  }

  public SuperMethodInvocation addArgument(Expression arg) {
    arguments.add(arg);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      qualifier.accept(visitor);
      receiver.accept(visitor);
      arguments.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public SuperMethodInvocation copy() {
    return new SuperMethodInvocation(this);
  }
}
