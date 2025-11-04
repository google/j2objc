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

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.List;

/** Switch statement node type. */
public class SwitchStatement extends Statement implements SwitchConstruct {

  private ChildLink<Expression> expression = ChildLink.create(Expression.class, this);
  private ChildList<Statement> statements = ChildList.create(Statement.class, this);

  public SwitchStatement() {}

  public SwitchStatement(SwitchStatement other) {
    super(other);
    expression.copyFrom(other.getExpression());
    statements.copyFrom(other.getStatements());
  }

  public SwitchStatement(SwitchExpression other) {
    expression.copyFrom(other.getExpression());
    statements.copyFrom(other.getStatements());
  }

  @Override
  public Kind getKind() {
    return Kind.SWITCH_STATEMENT;
  }

  @Override
  public Expression getExpression() {
    return expression.get();
  }

  @Override
  @CanIgnoreReturnValue
  public SwitchStatement setExpression(Expression newExpression) {
    expression.set(newExpression);
    return this;
  }

  @Override
  public List<Statement> getStatements() {
    return statements;
  }

  @Override
  @CanIgnoreReturnValue
  public SwitchStatement addStatement(Statement stmt) {
    statements.add(stmt);
    return this;
  }

  @CanIgnoreReturnValue
  public SwitchStatement copyStatements(List<Statement> stmts) {
    statements.copyFrom(stmts);
    return this;
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
