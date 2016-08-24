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
 * Node type for a type cast.
 */
public class CastExpression extends Expression {

  private ChildLink<Type> type = ChildLink.create(Type.class, this);
  private ChildLink<Expression> expression = ChildLink.create(Expression.class, this);

  public CastExpression(CastExpression other) {
    super(other);
    type.copyFrom(other.getType());
    expression.copyFrom(other.getExpression());
  }

  public CastExpression(TypeMirror typeMirror, Expression expression) {
    type.set(Type.newType(typeMirror));
    this.expression.set(expression);
  }
  
  public CastExpression() {}

  @Override
  public Kind getKind() {
    return Kind.CAST_EXPRESSION;
  }

  @Override
  public TypeMirror getTypeMirror() {
    Type typeNode = type.get();
    return typeNode != null ? typeNode.getTypeMirror() : null;
  }

  public Type getType() {
    return type.get();
  }

  public CastExpression setType(Type newType) {
    type.set(newType);
    return this;
  }

  public Expression getExpression() {
    return expression.get();
  }

  public CastExpression setExpression(Expression newExpression) {
    expression.set(newExpression);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      type.accept(visitor);
      expression.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public CastExpression copy() {
    return new CastExpression(this);
  }
}
