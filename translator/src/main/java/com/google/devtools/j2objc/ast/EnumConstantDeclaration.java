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
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

/**
 * Node for an enum constant.
 */
public class EnumConstantDeclaration extends BodyDeclaration {

  private VariableElement variableElement = null;
  private ExecutablePair method = ExecutablePair.NULL;
  private TypeMirror varargsType = null;
  private final ChildList<Expression> arguments = ChildList.create(Expression.class, this);
  private final ChildLink<TypeDeclaration> anonymousClassDeclaration =
      ChildLink.create(TypeDeclaration.class, this);

  public EnumConstantDeclaration() {}

  public EnumConstantDeclaration(EnumConstantDeclaration other) {
    super(other);
    variableElement = other.getVariableElement();
    method = other.getExecutablePair();
    varargsType = other.getVarargsType();
    arguments.copyFrom(other.getArguments());
    anonymousClassDeclaration.copyFrom(other.getAnonymousClassDeclaration());
  }

  public EnumConstantDeclaration(VariableElement variableElement) {
    super(variableElement);
    this.variableElement = variableElement;
  }

  @Override
  public Kind getKind() {
    return Kind.ENUM_CONSTANT_DECLARATION;
  }

  public VariableElement getVariableElement() {
    return variableElement;
  }

  public EnumConstantDeclaration setVariableElement(VariableElement element) {
    variableElement = element;
    return this;
  }

  public ExecutablePair getExecutablePair() {
    return method;
  }

  public EnumConstantDeclaration setExecutablePair(ExecutablePair newMethod) {
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

  public EnumConstantDeclaration setVarargsType(TypeMirror type) {
    varargsType = type;
    return this;
  }

  public List<Expression> getArguments() {
    return arguments;
  }

  public void addArgument(Expression arg) {
    arguments.add(arg);
  }

  public TypeDeclaration getAnonymousClassDeclaration() {
    return anonymousClassDeclaration.get();
  }

  public EnumConstantDeclaration setAnonymousClassDeclaration(
      TypeDeclaration newAnonymousClassDeclaration) {
    anonymousClassDeclaration.set(newAnonymousClassDeclaration);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      javadoc.accept(visitor);
      annotations.accept(visitor);
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
