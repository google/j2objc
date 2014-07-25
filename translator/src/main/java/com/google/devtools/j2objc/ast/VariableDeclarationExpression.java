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

import java.util.List;

/**
 * Collection of variable declaration fragments. Mainly used as the initializer
 * of a ForStatement.
 */
public class VariableDeclarationExpression extends Expression {

  private ChildList<VariableDeclarationFragment> fragments = ChildList.create(this);

  public VariableDeclarationExpression(
      org.eclipse.jdt.core.dom.VariableDeclarationExpression jdtNode) {
    super(jdtNode);
    for (Object fragment : jdtNode.fragments()) {
      fragments.add((VariableDeclarationFragment) TreeConverter.convert(fragment));
    }
  }

  public VariableDeclarationExpression(VariableDeclarationExpression other) {
    super(other);
    fragments.copyFrom(other.getFragments());
  }

  public List<VariableDeclarationFragment> getFragments() {
    return fragments;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      fragments.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public VariableDeclarationExpression copy() {
    return new VariableDeclarationExpression(this);
  }
}
