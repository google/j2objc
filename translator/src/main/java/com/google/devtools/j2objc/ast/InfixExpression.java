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

import com.google.common.collect.Lists;
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

  // In theory the type binding can be resolved from the operator and operands
  // but we'll keep it simple for now.
  private ITypeBinding typeBinding = null;
  private Operator operator = null;
  private ChildList<Expression> operands = ChildList.create(Expression.class, this);

  public InfixExpression(org.eclipse.jdt.core.dom.InfixExpression jdtNode) {
    super(jdtNode);
    typeBinding = jdtNode.resolveTypeBinding();
    operator = Operator.fromJdtOperator(jdtNode.getOperator());

    // The JDT parser apparently does not always take advantage of extended
    // operands, resulting in potentially very deep trees that can overflow the
    // stack. This code traverses the subtree non-recursively and merges all
    // children that have the same operator into this node using extended
    // operands.
    List<StackState> stack = Lists.newArrayList();
    stack.add(new StackState(jdtNode));
    while (!stack.isEmpty()) {
      StackState currentState = stack.get(stack.size() - 1);
      org.eclipse.jdt.core.dom.Expression child = currentState.nextChild();
      if (child == null) {
        stack.remove(stack.size() - 1);
        continue;
      }
      if (child instanceof org.eclipse.jdt.core.dom.InfixExpression) {
        org.eclipse.jdt.core.dom.InfixExpression infixChild =
            (org.eclipse.jdt.core.dom.InfixExpression) child;
        if (infixChild.getOperator().equals(jdtNode.getOperator())) {
          stack.add(new StackState(infixChild));
          continue;
        }
      }
      operands.add((Expression) TreeConverter.convert(child));
    }
  }

  private static class StackState {
    private final org.eclipse.jdt.core.dom.InfixExpression expression;
    private int nextChild = -2;

    private StackState(org.eclipse.jdt.core.dom.InfixExpression expr) {
      expression = expr;
    }

    private org.eclipse.jdt.core.dom.Expression nextChild() {
      int childIdx = nextChild++;
      if (childIdx == -2) {
        return expression.getLeftOperand();
      } else if (childIdx == -1) {
        return expression.getRightOperand();
      } else if (childIdx < expression.extendedOperands().size()) {
        return (org.eclipse.jdt.core.dom.Expression) expression.extendedOperands().get(childIdx);
      } else {
        return null;
      }
    }
  }

  public InfixExpression(InfixExpression other) {
    super(other);
    typeBinding = other.getTypeBinding();
    operator = other.getOperator();
    operands.copyFrom(other.getOperands());
  }

  public InfixExpression(
      ITypeBinding typeBinding, Operator operator, Expression... operands) {
    this.typeBinding = typeBinding;
    this.operator = operator;
    for (Expression operand : operands) {
      this.operands.add(operand);
    }
  }

  @Override
  public Kind getKind() {
    return Kind.INFIX_EXPRESSION;
  }

  @Override
  public ITypeBinding getTypeBinding() {
    return typeBinding;
  }

  public void setTypeBinding(ITypeBinding newTypeBinding) {
    typeBinding = newTypeBinding;
  }

  public Operator getOperator() {
    return operator;
  }

  public List<Expression> getOperands() {
    return operands;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      operands.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public InfixExpression copy() {
    return new InfixExpression(this);
  }
}
