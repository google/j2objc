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
 * A declaration fragment contains the declaration and optional initialization
 * of a single variable.
 */
public class VariableDeclarationFragment extends VariableDeclaration {

  public VariableDeclarationFragment(org.eclipse.jdt.core.dom.VariableDeclarationFragment jdtNode) {
    super(jdtNode);
  }

  public VariableDeclarationFragment(VariableDeclarationFragment other) {
    super(other);
  }

  public VariableDeclarationFragment(IVariableBinding variableBinding, Expression initializer) {
    super(variableBinding, initializer);
  }

  @Override
  public Kind getKind() {
    return Kind.VARIABLE_DECLARATION_FRAGMENT;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      name.accept(visitor);
      initializer.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public VariableDeclarationFragment copy() {
    return new VariableDeclarationFragment(this);
  }
}
