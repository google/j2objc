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
 * Wrapper for another statement node adding a label.
 */
public class LabeledStatement extends Statement {

  private ChildLink<SimpleName> label = ChildLink.create(SimpleName.class, this);
  private ChildLink<Statement> body = ChildLink.create(Statement.class, this);

  public LabeledStatement(org.eclipse.jdt.core.dom.LabeledStatement jdtNode) {
    super(jdtNode);
    label.set((SimpleName) TreeConverter.convert(jdtNode.getLabel()));
    body.set((Statement) TreeConverter.convert(jdtNode.getBody()));
  }

  public LabeledStatement(LabeledStatement other) {
    super(other);
    label.copyFrom(other.getLabel());
    body.copyFrom(other.getBody());
  }

  public LabeledStatement(String label) {
    this.label.set(new SimpleName(label));
  }

  @Override
  public Kind getKind() {
    return Kind.LABELED_STATEMENT;
  }

  public SimpleName getLabel() {
    return label.get();
  }

  public void setLabel(SimpleName newLabel) {
    label.set(newLabel);
  }

  public Statement getBody() {
    return body.get();
  }

  public void setBody(Statement newBody) {
    body.set(newBody);
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      label.accept(visitor);
      body.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public LabeledStatement copy() {
    return new LabeledStatement(this);
  }
}
