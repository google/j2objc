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
import javax.lang.model.type.TypeMirror;

/**
 * An expression that allows statements to be used where expressions are expected. There are emitted
 * as block literals that are immediately executed, i.e. ^{statement;}()) in Objective-C.
 */
public class EmbeddedStatementExpression extends Expression {
  private final ChildLink<Statement> statement = ChildLink.create(Statement.class, this);
  private TypeMirror typeMirror;

  public EmbeddedStatementExpression(EmbeddedStatementExpression other) {
    super(other);
    statement.set(other.getStatement().copy());
    typeMirror = other.getTypeMirror();
  }

  public EmbeddedStatementExpression() {}

  @Override
  public Kind getKind() {
    return Kind.EMBEDDED_STATEMENT_EXPRESSION;
  }

  @Override
  public TypeMirror getTypeMirror() {
    return typeMirror;
  }

  @CanIgnoreReturnValue
  public EmbeddedStatementExpression setTypeMirror(TypeMirror typeMirror) {
    this.typeMirror = typeMirror;
    return this;
  }

  public Statement getStatement() {
    return statement.get();
  }

  @CanIgnoreReturnValue
  public EmbeddedStatementExpression setStatement(Statement statement) {
    this.statement.set(statement);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      statement.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public EmbeddedStatementExpression copy() {
    return new EmbeddedStatementExpression(this);
  }
}
