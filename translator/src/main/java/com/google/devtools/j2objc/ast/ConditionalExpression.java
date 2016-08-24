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

import javax.lang.model.type.TypeMirror;

/**
 * Conditional expression node type. (e.g. "useFoo ? foo : bar")
 */
public class ConditionalExpression extends Expression {

  private TypeMirror typeMirror = null;

  private ChildLink<Expression> expression = ChildLink.create(Expression.class, this);
  private ChildLink<Expression> thenExpression = ChildLink.create(Expression.class, this);
  private ChildLink<Expression> elseExpression = ChildLink.create(Expression.class, this);
  
  public ConditionalExpression() {}

  public ConditionalExpression(ConditionalExpression other) {
    super(other);
    typeMirror = other.getTypeMirror();
    expression.copyFrom(other.getExpression());
    thenExpression.copyFrom(other.getThenExpression());
    elseExpression.copyFrom(other.getElseExpression());
  }

  @Override
  public Kind getKind() {
    return Kind.CONDITIONAL_EXPRESSION;
  }

  @Override
  public TypeMirror getTypeMirror() {
    return typeMirror;
  }
  
  public ConditionalExpression setTypeMirror(TypeMirror newType) {
    typeMirror = newType;
    return this;
  }

  public Expression getExpression() {
    return expression.get();
  }

  public ConditionalExpression setExpression(Expression newExpression) {
    expression.set(newExpression);
    return this;
  }

  public Expression getThenExpression() {
    return thenExpression.get();
  }

  public ConditionalExpression setThenExpression(Expression newThenExpression) {
    thenExpression.set(newThenExpression);
    return this;
  }

  public Expression getElseExpression() {
    return elseExpression.get();
  }

  public ConditionalExpression setElseExpression(Expression newElseExpression) {
    elseExpression.set(newElseExpression);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      expression.accept(visitor);
      thenExpression.accept(visitor);
      elseExpression.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public ConditionalExpression copy() {
    return new ConditionalExpression(this);
  }
}
