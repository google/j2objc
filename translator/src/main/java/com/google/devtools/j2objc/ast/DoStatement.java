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

/**
 * Do statement node type.
 */
public class DoStatement extends Statement {

  private ChildLink<Expression> expression = ChildLink.create(Expression.class, this);
  private ChildLink<Statement> body = ChildLink.create(Statement.class, this);

  public DoStatement(org.eclipse.jdt.core.dom.DoStatement jdtNode) {
    super(jdtNode);
    expression.set((Expression) TreeConverter.convert(jdtNode.getExpression()));
    body.set((Statement) TreeConverter.convert(jdtNode.getBody()));
  }

  public DoStatement(DoStatement other) {
    super(other);
    expression.copyFrom(other.getExpression());
    body.copyFrom(other.getBody());
  }

  @Override
  public Kind getKind() {
    return Kind.DO_STATEMENT;
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

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      expression.accept(visitor);
      body.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public DoStatement copy() {
    return new DoStatement(this);
  }
}
