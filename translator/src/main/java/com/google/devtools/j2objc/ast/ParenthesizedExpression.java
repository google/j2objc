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
 * Adds parentheses to a wrapped expression.
 */
public class ParenthesizedExpression extends Expression {

  private ChildLink<Expression> expression = ChildLink.create(Expression.class, this);

  public ParenthesizedExpression() {}

  public ParenthesizedExpression(ParenthesizedExpression other) {
    super(other);
    expression.copyFrom(other.getExpression());
  }

  public ParenthesizedExpression(Expression expression) {
    this.expression.set(expression);
  }

  // Static factory avoids conflict with the copy constructor
  public static ParenthesizedExpression parenthesize(Expression expression) {
    return new ParenthesizedExpression(expression);
  }

  /**
   * Wraps the given expression with a ParenthesizedExpression and replaces it
   * in the tree.
   */
  public static ParenthesizedExpression parenthesizeAndReplace(Expression expression) {
    ParenthesizedExpression newExpr = new ParenthesizedExpression();
    expression.replaceWith(newExpr);
    newExpr.setExpression(expression);
    return newExpr;
  }

  @Override
  public Kind getKind() {
    return Kind.PARENTHESIZED_EXPRESSION;
  }

  @Override
  public TypeMirror getTypeMirror() {
    Expression expressionNode = expression.get();
    return expressionNode != null ? expressionNode.getTypeMirror() : null;
  }

  public Expression getExpression() {
    return expression.get();
  }

  public ParenthesizedExpression setExpression(Expression newExpression) {
    expression.set(newExpression);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      expression.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public ParenthesizedExpression copy() {
    return new ParenthesizedExpression(this);
  }
}
