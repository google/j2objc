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
 * Node type for an assert statement.
 */
public class AssertStatement extends Statement {

  private ChildLink<Expression> expression = ChildLink.create(Expression.class, this);
  private ChildLink<Expression> message = ChildLink.create(Expression.class, this);

  public AssertStatement(org.eclipse.jdt.core.dom.AssertStatement jdtNode) {
    super(jdtNode);
    expression.set((Expression) TreeConverter.convert(jdtNode.getExpression()));
    message.set((Expression) TreeConverter.convert(jdtNode.getMessage()));
  }

  public AssertStatement(AssertStatement other) {
    super(other);
    expression.copyFrom(other.getExpression());
    message.copyFrom(other.getMessage());
  }

  @Override
  public Kind getKind() {
    return Kind.ASSERT_STATEMENT;
  }

  public Expression getExpression() {
    return expression.get();
  }

  public void setExpression(Expression newExpression) {
    expression.set(newExpression);
  }

  public Expression getMessage() {
    return message.get();
  }

  public void setMessage(Expression newMessage) {
    message.set(newMessage);
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      expression.accept(visitor);
      message.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public AssertStatement copy() {
    return new AssertStatement(this);
  }
}
