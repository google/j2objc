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

import com.google.devtools.j2objc.jdt.TreeConverter;

/**
 * Node type for a break statement.
 */
public class BreakStatement extends Statement {

  private ChildLink<SimpleName> label = ChildLink.create(SimpleName.class, this);

  public BreakStatement(org.eclipse.jdt.core.dom.BreakStatement jdtNode) {
    super(jdtNode);
    label.set((SimpleName) TreeConverter.convert(jdtNode.getLabel()));
  }

  public BreakStatement(BreakStatement other) {
    super(other);
    label.copyFrom(other.getLabel());
  }

  public BreakStatement() {}

  @Override
  public Kind getKind() {
    return Kind.BREAK_STATEMENT;
  }

  public SimpleName getLabel() {
    return label.get();
  }

  public void setLabel(SimpleName newLabel) {
    label.set(newLabel);
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      label.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public BreakStatement copy() {
    return new BreakStatement(this);
  }
}
