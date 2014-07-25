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
 * Node for accessing a field via "super" keyword.
 */
public class SuperFieldAccess extends Expression {

  private IVariableBinding variableBinding = null;
  private ChildLink<SimpleName> name = ChildLink.create(this);

  public SuperFieldAccess(org.eclipse.jdt.core.dom.SuperFieldAccess jdtNode) {
    super(jdtNode);
    variableBinding = Types.getVariableBinding(jdtNode);
    name.set((SimpleName) TreeConverter.convert(jdtNode.getName()));
  }

  public SuperFieldAccess(SuperFieldAccess other) {
    super(other);
    variableBinding = other.getVariableBinding();
    name.copyFrom(other.getName());
  }

  public IVariableBinding getVariableBinding() {
    return variableBinding;
  }

  public SimpleName getName() {
    return name.get();
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      name.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public SuperFieldAccess copy() {
    return new SuperFieldAccess(this);
  }
}
