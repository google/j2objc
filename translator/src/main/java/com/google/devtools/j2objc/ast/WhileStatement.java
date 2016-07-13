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
 * While statement node type.
 */
public class WhileStatement extends Statement {

  private ChildLink<Expression> expression = ChildLink.create(Expression.class, this);
  private ChildLink<Statement> body = ChildLink.create(Statement.class, this);

  public WhileStatement(org.eclipse.jdt.core.dom.WhileStatement jdtNode) {
    super(jdtNode);
    expression.set((Expression) TreeConverter.convert(jdtNode.getExpression()));
    body.set((Statement) TreeConverter.convert(jdtNode.getBody()));
  }

  public WhileStatement(WhileStatement other) {
    super(other);
    expression.copyFrom(other.getExpression());
    body.copyFrom(other.getBody());
  }

  public WhileStatement() {}

  @Override
  public Kind getKind() {
    return Kind.WHILE_STATEMENT;
  }

  public Expression getExpression() {
    return expression.get();
  }

  public void setExpression(Expression newExpression) {
    expression.set(newExpression);
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
      expression.accept(visitor);
      body.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public WhileStatement copy() {
    return new WhileStatement(this);
  }
}
