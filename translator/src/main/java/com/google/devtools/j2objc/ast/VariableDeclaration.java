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

import com.google.devtools.j2objc.types.Types;

import org.eclipse.jdt.core.dom.IVariableBinding;

/**
 * Node type for the declaration of a single local variable.
 */
public abstract class VariableDeclaration extends TreeNode {

  private IVariableBinding binding;
  private int extraDimensions = 0;
  protected ChildLink<SimpleName> name = ChildLink.create(this);
  protected ChildLink<Expression> initializer = ChildLink.create(this);

  public VariableDeclaration(org.eclipse.jdt.core.dom.VariableDeclaration jdtNode) {
    super(jdtNode);
    binding = Types.getVariableBinding(jdtNode);
    extraDimensions = jdtNode.getExtraDimensions();
    name.set((SimpleName) TreeConverter.convert(jdtNode.getName()));
    initializer.set((Expression) TreeConverter.convert(jdtNode.getInitializer()));
  }

  public VariableDeclaration(VariableDeclaration other) {
    super(other);
    binding = other.getVariableBinding();
    extraDimensions = other.getExtraDimensions();
    name.copyFrom(other.getName());
    initializer.copyFrom(other.getInitializer());
  }

  public IVariableBinding getVariableBinding() {
    return binding;
  }

  public int getExtraDimensions() {
    return extraDimensions;
  }

  public SimpleName getName() {
    return name.get();
  }

  public Expression getInitializer() {
    return initializer.get();
  }
}
