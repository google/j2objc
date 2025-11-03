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
import javax.lang.model.type.TypeMirror;

/** Switch expression node type. */
@SuppressWarnings("CanIgnoreReturnValueSuggester")
public class SwitchExpression extends Expression {

  private final ChildLink<Expression> expression = ChildLink.create(Expression.class, this);
  private final ChildList<Statement> statements = ChildList.create(Statement.class, this);
  private TypeMirror typeMirror;

  public SwitchExpression() {}

  public SwitchExpression(SwitchExpression other) {
    super(other);
    expression.copyFrom(other.getExpression());
    statements.copyFrom(other.getStatements());
    typeMirror = other.getTypeMirror();
  }

  @Override
  public Kind getKind() {
    return Kind.SWITCH_EXPRESSION;
  }

  @Override
  public TypeMirror getTypeMirror() {
    return typeMirror;
  }

  public SwitchExpression setTypeMirror(TypeMirror newType) {
    typeMirror = newType;
    return this;
  }

  public Expression getExpression() {
    return expression.get();
  }

  public SwitchExpression setExpression(Expression newExpression) {
    expression.set(newExpression);
    return this;
  }

  public List<Statement> getStatements() {
    return statements;
  }

  public SwitchExpression addStatement(Statement stmt) {
    statements.add(stmt);
    return this;
  }

  public boolean hasDefaultCase() {
    return statements.stream()
        .anyMatch(stmt -> stmt instanceof SwitchCase switchCase && switchCase.isDefault());
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
  public SwitchExpression copy() {
    return new SwitchExpression(this);
  }
}
