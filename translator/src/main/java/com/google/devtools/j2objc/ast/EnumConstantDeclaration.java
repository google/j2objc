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
import org.eclipse.jdt.core.dom.IVariableBinding;

import java.util.List;

/**
 * Node for an enum constant.
 */
public class EnumConstantDeclaration extends BodyDeclaration {

  private IVariableBinding variableBinding = null;
  private IMethodBinding methodBinding = null;
  private final ChildLink<SimpleName> name = ChildLink.create(SimpleName.class, this);
  private final ChildList<Expression> arguments = ChildList.create(Expression.class, this);
  private final ChildLink<AnonymousClassDeclaration> anonymousClassDeclaration =
      ChildLink.create(AnonymousClassDeclaration.class, this);

  public EnumConstantDeclaration(org.eclipse.jdt.core.dom.EnumConstantDeclaration jdtNode) {
    super(jdtNode);
    variableBinding = jdtNode.resolveVariable();
    methodBinding = jdtNode.resolveConstructorBinding();
    name.set((SimpleName) TreeConverter.convert(jdtNode.getName()));
    for (Object argument : jdtNode.arguments()) {
      arguments.add((Expression) TreeConverter.convert(argument));
    }
    anonymousClassDeclaration.set((AnonymousClassDeclaration)
        TreeConverter.convert(jdtNode.getAnonymousClassDeclaration()));
  }

  public EnumConstantDeclaration(EnumConstantDeclaration other) {
    super(other);
    variableBinding = other.getVariableBinding();
    methodBinding = other.getMethodBinding();
    name.copyFrom(other.getName());
    arguments.copyFrom(other.getArguments());
    anonymousClassDeclaration.copyFrom(other.getAnonymousClassDeclaration());
  }

  @Override
  public Kind getKind() {
    return Kind.ENUM_CONSTANT_DECLARATION;
  }

  public IVariableBinding getVariableBinding() {
    return variableBinding;
  }

  public IMethodBinding getMethodBinding() {
    return methodBinding;
  }

  public void setMethodBinding(IMethodBinding newMethodBinding) {
    methodBinding = newMethodBinding;
  }

  public SimpleName getName() {
    return name.get();
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
      javadoc.accept(visitor);
      annotations.accept(visitor);
      name.accept(visitor);
      arguments.accept(visitor);
      anonymousClassDeclaration.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public EnumConstantDeclaration copy() {
    return new EnumConstantDeclaration(this);
  }
}
