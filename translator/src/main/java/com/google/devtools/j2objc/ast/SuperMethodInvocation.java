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
 * Node type for a method invocation on the super class. (e.g. "super.foo()")
 */
public class SuperMethodInvocation extends Expression {

  private IMethodBinding methodBinding = null;
  private ChildLink<Name> qualifier = ChildLink.create(Name.class, this);
  private ChildLink<SimpleName> name = ChildLink.create(SimpleName.class, this);
  private ChildList<Expression> arguments = ChildList.create(Expression.class, this);

  public SuperMethodInvocation(org.eclipse.jdt.core.dom.SuperMethodInvocation jdtNode) {
    super(jdtNode);
    methodBinding = jdtNode.resolveMethodBinding();
    qualifier.set((Name) TreeConverter.convert(jdtNode.getQualifier()));
    name.set((SimpleName) TreeConverter.convert(jdtNode.getName()));
    for (Object argument : jdtNode.arguments()) {
      arguments.add((Expression) TreeConverter.convert(argument));
    }
  }

  public SuperMethodInvocation(SuperMethodInvocation other) {
    super(other);
    methodBinding = other.getMethodBinding();
    qualifier.copyFrom(other.getQualifier());
    name.copyFrom(other.getName());
    arguments.copyFrom(other.getArguments());
  }

  public SuperMethodInvocation(IMethodBinding methodBinding) {
    this.methodBinding = methodBinding;
    name.set(new SimpleName(methodBinding));
  }

  @Override
  public Kind getKind() {
    return Kind.SUPER_METHOD_INVOCATION;
  }

  public IMethodBinding getMethodBinding() {
    return methodBinding;
  }

  public void setMethodBinding(IMethodBinding newMethodBinding) {
    methodBinding = newMethodBinding;
  }

  @Override
  public ITypeBinding getTypeBinding() {
    return methodBinding != null ? methodBinding.getReturnType() : null;
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
}
