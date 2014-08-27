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

import com.google.common.collect.Maps;

import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.List;
import java.util.Map;

/**
 * Infix expression node type.
 */
public class InfixExpression extends Expression {

  /**
   * Infix operators.
   */
  public static enum Operator {
    TIMES("*"),
    DIVIDE("/"),
    REMAINDER("%"),
    PLUS("+"),
    MINUS("-"),
    LEFT_SHIFT("<<"),
    RIGHT_SHIFT_SIGNED(">>"),
    RIGHT_SHIFT_UNSIGNED(">>>"),
    LESS("<"),
    GREATER(">"),
    LESS_EQUALS("<="),
    GREATER_EQUALS(">="),
    EQUALS("=="),
    NOT_EQUALS("!="),
    XOR("^"),
    AND("&"),
    OR("|"),
    CONDITIONAL_AND("&&"),
    CONDITIONAL_OR("||");

    private final String opString;
    private static Map<String, Operator> stringLookup = Maps.newHashMap();

    static {
      for (Operator operator : Operator.values()) {
        stringLookup.put(operator.toString(), operator);
      }
    }

    private Operator(String opString) {
      this.opString = opString;
    }

    @Override
    public String toString() {
      return opString;
    }

    public static Operator fromJdtOperator(
        org.eclipse.jdt.core.dom.InfixExpression.Operator jdtOperator) {
      Operator result = stringLookup.get(jdtOperator.toString());
      assert result != null;
      return result;
    }
  }

  private Operator operator = null;
  private ChildLink<Expression> leftOperand = ChildLink.create(Expression.class, this);
  private ChildLink<Expression> rightOperand = ChildLink.create(Expression.class, this);
  private ChildList<Expression> extendedOperands = ChildList.create(Expression.class, this);

  public InfixExpression(org.eclipse.jdt.core.dom.InfixExpression jdtNode) {
    super(jdtNode);
    operator = Operator.fromJdtOperator(jdtNode.getOperator());
    leftOperand.set((Expression) TreeConverter.convert(jdtNode.getLeftOperand()));
    rightOperand.set((Expression) TreeConverter.convert(jdtNode.getRightOperand()));
    for (Object operand : jdtNode.extendedOperands()) {
      extendedOperands.add((Expression) TreeConverter.convert(operand));
    }
  }

  public InfixExpression(InfixExpression other) {
    super(other);
    operator = other.getOperator();
    leftOperand.copyFrom(other.getLeftOperand());
    rightOperand.copyFrom(other.getRightOperand());
    extendedOperands.copyFrom(other.getExtendedOperands());
  }

  public InfixExpression(
      ITypeBinding typeBinding, Operator operator, Expression leftOperand,
      Expression rightOperand) {
    super(typeBinding);
    this.operator = operator;
    this.leftOperand.set(leftOperand);
    this.rightOperand.set(rightOperand);
  }

  @Override
  public Kind getKind() {
    return Kind.INFIX_EXPRESSION;
  }

  public Operator getOperator() {
    return operator;
  }

  public Expression getLeftOperand() {
    return leftOperand.get();
  }

  public Expression getRightOperand() {
    return rightOperand.get();
  }

  public List<Expression> getExtendedOperands() {
    return extendedOperands;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      leftOperand.accept(visitor);
      rightOperand.accept(visitor);
      extendedOperands.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public InfixExpression copy() {
    return new InfixExpression(this);
  }
}
