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
import javax.lang.model.element.VariableElement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

/**
 * Node for an enum constant.
 */
public class EnumConstantDeclaration extends BodyDeclaration {

  private VariableElement variableElement = null;
  private ExecutableElement method = null;
  private final ChildLink<SimpleName> name = ChildLink.create(SimpleName.class, this);
  private final ChildList<Expression> arguments = ChildList.create(Expression.class, this);
  private final ChildLink<AnonymousClassDeclaration> anonymousClassDeclaration =
      ChildLink.create(AnonymousClassDeclaration.class, this);

  public EnumConstantDeclaration() {}

  public EnumConstantDeclaration(EnumConstantDeclaration other) {
    super(other);
    variableElement = other.getVariableElement();
    method = other.getExecutableElement();
    name.copyFrom(other.getName());
    arguments.copyFrom(other.getArguments());
    anonymousClassDeclaration.copyFrom(other.getAnonymousClassDeclaration());
  }

  @Override
  public Kind getKind() {
    return Kind.ENUM_CONSTANT_DECLARATION;
  }

  public IVariableBinding getVariableBinding() {
    return BindingConverter.unwrapVariableElement(variableElement);
  }

  public VariableElement getVariableElement() {
    return variableElement;
  }

  public EnumConstantDeclaration setVariableElement(VariableElement element) {
    variableElement = element;
    return this;
  }

  // TODO(tball): remove when javac migration is complete.
  public IMethodBinding getMethodBinding() {
    return (IMethodBinding) BindingConverter.unwrapElement(method);
  }

  public ExecutableElement getExecutableElement() {
    return method;
  }

  public EnumConstantDeclaration setExecutableElement(ExecutableElement newMethod) {
    method = newMethod;
    return this;
  }

  public SimpleName getName() {
    return name.get();
  }

  public EnumConstantDeclaration setName(SimpleName newName) {
    name.set(newName);
    return this;
  }

  public List<Expression> getArguments() {
    return arguments;
  }

  public void addArgument(Expression arg) {
    arguments.add(arg);
  }

  public AnonymousClassDeclaration getAnonymousClassDeclaration() {
    return anonymousClassDeclaration.get();
  }

  public EnumConstantDeclaration setAnonymousClassDeclaration(
      AnonymousClassDeclaration newAnonymousClassDeclaration) {
    anonymousClassDeclaration.set(newAnonymousClassDeclaration);
    return this;
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
