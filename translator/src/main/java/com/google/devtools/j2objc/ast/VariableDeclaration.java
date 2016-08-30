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
import javax.lang.model.element.VariableElement;
import org.eclipse.jdt.core.dom.IVariableBinding;

/**
 * Node type for the declaration of a single local variable.
 */
public abstract class VariableDeclaration extends TreeNode {

  private VariableElement variableElement;
  private int extraDimensions = 0;
  protected ChildLink<SimpleName> name = ChildLink.create(SimpleName.class, this);
  protected ChildLink<Expression> initializer = ChildLink.create(Expression.class, this);

  public VariableDeclaration() {}

  public VariableDeclaration(VariableDeclaration other) {
    super(other);
    variableElement = other.getVariableElement();
    extraDimensions = other.getExtraDimensions();
    name.copyFrom(other.getName());
    initializer.copyFrom(other.getInitializer());
  }

  // TODO(tball): remove when javac migration is complete.
  public VariableDeclaration(IVariableBinding variableBinding, Expression initializer) {
    this((VariableElement) BindingConverter.getElement(variableBinding), initializer);
  }

  public VariableDeclaration(VariableElement variableElement, Expression initializer) {
    super();
    this.variableElement = variableElement;
    name.set(new SimpleName(variableElement));
    this.initializer.set(initializer);
  }

  public IVariableBinding getVariableBinding() {
    return (IVariableBinding) BindingConverter.unwrapElement(variableElement);
  }

  public VariableElement getVariableElement() {
    return variableElement;
  }

  public VariableDeclaration setVariableElement(VariableElement newElement) {
    variableElement = newElement;
    return this;
  }

  public void setVariableBinding(IVariableBinding newVariableBinding) {
    variableElement = (VariableElement) BindingConverter.getElement(newVariableBinding);
  }

  public int getExtraDimensions() {
    return extraDimensions;
  }

  public VariableDeclaration setExtraDimensions(int newExtraDimensions) {
    extraDimensions = newExtraDimensions;
    return this;
  }

  public SimpleName getName() {
    return name.get();
  }

  public VariableDeclaration setName(SimpleName newName) {
    name.set(newName);
    return this;
  }

  public Expression getInitializer() {
    return initializer.get();
  }

  public VariableDeclaration setInitializer(Expression newInitializer) {
    initializer.set(newInitializer);
    return this;
  }
}
