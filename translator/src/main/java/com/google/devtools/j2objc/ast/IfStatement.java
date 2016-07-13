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
 * If statement node type.
 */
public class IfStatement extends Statement {

  private ChildLink<Expression> expression = ChildLink.create(Expression.class, this);
  private ChildLink<Statement> thenStatement = ChildLink.create(Statement.class, this);
  private ChildLink<Statement> elseStatement = ChildLink.create(Statement.class, this);

  public IfStatement(org.eclipse.jdt.core.dom.IfStatement jdtNode) {
    super(jdtNode);
    expression.set((Expression) TreeConverter.convert(jdtNode.getExpression()));
    thenStatement.set((Statement) TreeConverter.convert(jdtNode.getThenStatement()));
    elseStatement.set((Statement) TreeConverter.convert(jdtNode.getElseStatement()));
  }

  public IfStatement(IfStatement other) {
    super(other);
    expression.copyFrom(other.getExpression());
    thenStatement.copyFrom(other.getThenStatement());
    elseStatement.copyFrom(other.getElseStatement());
  }

  public IfStatement() {}

  @Override
  public Kind getKind() {
    return Kind.IF_STATEMENT;
  }

  public Expression getExpression() {
    return expression.get();
  }

  public void setExpression(Expression newExpression) {
    expression.set(newExpression);
  }

  public Statement getThenStatement() {
    return thenStatement.get();
  }

  public void setThenStatement(Statement newThenStatement) {
    thenStatement.set(newThenStatement);
  }

  public Statement getElseStatement() {
    return elseStatement.get();
  }

  public void setElseStatement(Statement newElseStatement) {
    elseStatement.set(newElseStatement);
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      expression.accept(visitor);
      thenStatement.accept(visitor);
      elseStatement.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public IfStatement copy() {
    return new IfStatement(this);
  }
}
