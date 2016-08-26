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
 * Instanceof expression node type.
 */
public class InstanceofExpression extends Expression {

  private TypeMirror typeMirror;
  private ChildLink<Expression> leftOperand = ChildLink.create(Expression.class, this);
  private ChildLink<Type> rightOperand = ChildLink.create(Type.class, this);

  public InstanceofExpression() {}

  public InstanceofExpression(InstanceofExpression other) {
    super(other);
    typeMirror = other.getTypeMirror();
    leftOperand.copyFrom(other.getLeftOperand());
    rightOperand.copyFrom(other.getRightOperand());
  }

  @Override
  public Kind getKind() {
    return Kind.INSTANCEOF_EXPRESSION;
  }

  @Override
  public TypeMirror getTypeMirror() {
    return typeMirror;
  }

  public InstanceofExpression setTypeMirror(TypeMirror type) {
    typeMirror = type;
    return this;
  }

  public Expression getLeftOperand() {
    return leftOperand.get();
  }

  public InstanceofExpression setLeftOperand(Expression operand) {
    leftOperand.set(operand);
    return this;
  }

  public Type getRightOperand() {
    return rightOperand.get();
  }

  public InstanceofExpression setRightOperand(Type operand) {
    rightOperand.set(operand);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      leftOperand.accept(visitor);
      rightOperand.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public InstanceofExpression copy() {
    return new InstanceofExpression(this);
  }
}
