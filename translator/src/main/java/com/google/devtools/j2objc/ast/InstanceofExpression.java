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
 * Instanceof expression node type.
 */
public class InstanceofExpression extends Expression {

  private ChildLink<Expression> leftOperand = ChildLink.create(this);
  private ChildLink<Type> rightOperand = ChildLink.create(this);

  public InstanceofExpression(org.eclipse.jdt.core.dom.InstanceofExpression jdtNode) {
    super(jdtNode);
    leftOperand.set((Expression) TreeConverter.convert(jdtNode.getLeftOperand()));
    rightOperand.set((Type) TreeConverter.convert(jdtNode.getRightOperand()));
  }

  public InstanceofExpression(InstanceofExpression other) {
    super(other);
    leftOperand.copyFrom(other.getLeftOperand());
    rightOperand.copyFrom(other.getRightOperand());
  }

  public Expression getLeftOperand() {
    return leftOperand.get();
  }

  public Type getRightOperand() {
    return rightOperand.get();
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
