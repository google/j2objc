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
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import org.eclipse.jdt.core.dom.IMethodBinding;

/**
 * Node type for a method invocation on the super class. (e.g. "super.foo()")
 */
public class SuperMethodInvocation extends Expression {

  private ExecutableElement method = null;
  private ExecutableType methodType = null;
  private ChildLink<Name> qualifier = ChildLink.create(Name.class, this);
  private ChildLink<SimpleName> name = ChildLink.create(SimpleName.class, this);
  // Resolved by OuterReferenceResolver.
  private ChildLink<Expression> receiver = ChildLink.create(Expression.class, this);
  private ChildList<Expression> arguments = ChildList.create(Expression.class, this);

  public SuperMethodInvocation() {}

  public SuperMethodInvocation(SuperMethodInvocation other) {
    super(other);
    method = other.getExecutableElement();
    methodType = other.getExecutableType();
    qualifier.copyFrom(other.getQualifier());
    name.copyFrom(other.getName());
    receiver.copyFrom(other.getReceiver());
    arguments.copyFrom(other.getArguments());
  }

  public SuperMethodInvocation(IMethodBinding methodBinding) {
    method = BindingConverter.getExecutableElement(methodBinding);
    methodType = BindingConverter.getType(methodBinding);
    name.set(new SimpleName(methodBinding));
  }

  public SuperMethodInvocation(ExecutableElement element) {
    method = element;
    methodType = (ExecutableType) element.asType();
    name.set(new SimpleName(element));
  }

  @Override
  public Kind getKind() {
    return Kind.SUPER_METHOD_INVOCATION;
  }

  public IMethodBinding getMethodBinding() {
    return (IMethodBinding) BindingConverter.unwrapTypeMirrorIntoBinding(getExecutableType());
  }

  public ExecutableElement getExecutableElement() {
    return method;
  }

  public SuperMethodInvocation setExecutableElement(ExecutableElement newElement) {
    method = newElement;
    return this;
  }

  public ExecutableType getExecutableType() {
    return methodType;
  }

  public SuperMethodInvocation setExecutableType(ExecutableType newType) {
    methodType = newType;
    return this;
  }

  @Override
  public TypeMirror getTypeMirror() {
    return getExecutableType().getReturnType();
  }

  public Name getQualifier() {
    return qualifier.get();
  }

  public SuperMethodInvocation setQualifier(Name newQualifier) {
    qualifier.set(newQualifier);
    return this;
  }

  public SimpleName getName() {
    return name.get();
  }

  public SuperMethodInvocation setName(SimpleName newName) {
    name.set(newName);
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
      name.accept(visitor);
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
