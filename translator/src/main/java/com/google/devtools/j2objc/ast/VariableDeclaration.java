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

import org.eclipse.jdt.core.dom.IVariableBinding;

/**
 * Node type for the declaration of a single local variable.
 */
public abstract class VariableDeclaration extends TreeNode {

  private IVariableBinding variableBinding;
  private int extraDimensions = 0;
  protected ChildLink<SimpleName> name = ChildLink.create(SimpleName.class, this);
  protected ChildLink<Expression> initializer = ChildLink.create(Expression.class, this);

  public VariableDeclaration(org.eclipse.jdt.core.dom.VariableDeclaration jdtNode) {
    super(jdtNode);
    variableBinding = jdtNode.resolveBinding();
    extraDimensions = jdtNode.getExtraDimensions();
    name.set((SimpleName) TreeConverter.convert(jdtNode.getName()));
    initializer.set((Expression) TreeConverter.convert(jdtNode.getInitializer()));
  }

  public VariableDeclaration(VariableDeclaration other) {
    super(other);
    variableBinding = other.getVariableBinding();
    extraDimensions = other.getExtraDimensions();
    name.copyFrom(other.getName());
    initializer.copyFrom(other.getInitializer());
  }

  public VariableDeclaration(IVariableBinding variableBinding, Expression initializer) {
    super();
    this.variableBinding = variableBinding;
    name.set(new SimpleName(variableBinding));
    this.initializer.set(initializer);
  }

  public IVariableBinding getVariableBinding() {
    return variableBinding;
  }

  public void setVariableBinding(IVariableBinding newVariableBinding) {
    variableBinding = newVariableBinding;
  }

  public int getExtraDimensions() {
    return extraDimensions;
  }

  public void setExtraDimensions(int newExtraDimensions) {
    extraDimensions = newExtraDimensions;
  }

  public SimpleName getName() {
    return name.get();
  }

  public Expression getInitializer() {
    return initializer.get();
  }

  public void setInitializer(Expression newInitializer) {
    initializer.set(newInitializer);
  }
}
