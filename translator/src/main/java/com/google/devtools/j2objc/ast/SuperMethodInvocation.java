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
import com.google.devtools.j2objc.jdt.TreeConverter;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import org.eclipse.jdt.core.dom.IMethodBinding;

/**
 * Node type for a method invocation on the super class. (e.g. "super.foo()")
 */
public class SuperMethodInvocation extends Expression {

  private ExecutableElement method = null;
  private ChildLink<Name> qualifier = ChildLink.create(Name.class, this);
  private ChildLink<SimpleName> name = ChildLink.create(SimpleName.class, this);
  private ChildList<Expression> arguments = ChildList.create(Expression.class, this);

  public SuperMethodInvocation(org.eclipse.jdt.core.dom.SuperMethodInvocation jdtNode) {
    super(jdtNode);
    IMethodBinding methodBinding = BindingConverter.wrapBinding(jdtNode.resolveMethodBinding());
    method = BindingConverter.getExecutableElement(methodBinding);
    qualifier.set((Name) TreeConverter.convert(jdtNode.getQualifier()));
    name.set((SimpleName) TreeConverter.convert(jdtNode.getName()));
    for (Object argument : jdtNode.arguments()) {
      arguments.add((Expression) TreeConverter.convert(argument));
    }
  }

  public SuperMethodInvocation(SuperMethodInvocation other) {
    super(other);
    method = other.getExecutableElement();
    qualifier.copyFrom(other.getQualifier());
    name.copyFrom(other.getName());
    arguments.copyFrom(other.getArguments());
  }

  public SuperMethodInvocation(IMethodBinding methodBinding) {
    method = BindingConverter.getExecutableElement(methodBinding);
    name.set(new SimpleName(methodBinding));
  }

  @Override
  public Kind getKind() {
    return Kind.SUPER_METHOD_INVOCATION;
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
    return method.getReturnType();
  }

  public Name getQualifier() {
    return qualifier.get();
  }

  public void setQualifier(Name newQualifier) {
    qualifier.set(newQualifier);
  }

  public SimpleName getName() {
    return name.get();
  }

  public List<Expression> getArguments() {
    return arguments;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      qualifier.accept(visitor);
      arguments.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public SuperMethodInvocation copy() {
    return new SuperMethodInvocation(this);
  }

  public SuperMethodInvocation addArgument(Expression arg) {
    arguments.add(arg);
    return this;
  }
}
