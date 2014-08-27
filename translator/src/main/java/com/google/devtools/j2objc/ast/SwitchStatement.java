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
 * Switch statement node type.
 */
public class SwitchStatement extends Statement {

  private ChildLink<Expression> expression = ChildLink.create(Expression.class, this);
  private ChildList<Statement> statements = ChildList.create(Statement.class, this);

  public SwitchStatement(org.eclipse.jdt.core.dom.SwitchStatement jdtNode) {
    super(jdtNode);
    expression.set((Expression) TreeConverter.convert(jdtNode.getExpression()));
    for (Object statement : jdtNode.statements()) {
      statements.add((Statement) TreeConverter.convert(statement));
    }
  }

  public SwitchStatement(SwitchStatement other) {
    super(other);
    expression.copyFrom(other.getExpression());
    statements.copyFrom(other.getStatements());
  }

  @Override
  public Kind getKind() {
    return Kind.SWITCH_STATEMENT;
  }

  public Expression getExpression() {
    return expression.get();
  }

  public void setExpression(Expression newExpression) {
    expression.set(newExpression);
  }

  public List<Statement> getStatements() {
    return statements;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      expression.accept(visitor);
      statements.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public SwitchStatement copy() {
    return new SwitchStatement(this);
  }
}
