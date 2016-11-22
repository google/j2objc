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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.type.TypeMirror;

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
    private static Map<String, Operator> stringLookup = new HashMap<>();

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

    public static Operator parse(String op) {
      Operator result = stringLookup.get(op);
      assert result != null;
      return result;
    }
  }

  // In theory the type can be resolved from the operator and operands but we'll keep it simple for
  // now.
  private TypeMirror typeMirror = null;
  private Operator operator = null;
  private ChildList<Expression> operands = ChildList.create(Expression.class, this);

  public InfixExpression() {}

  public InfixExpression(InfixExpression other) {
    super(other);
    typeMirror = other.getTypeMirror();
    operator = other.getOperator();
    operands.copyFrom(other.getOperands());
  }

  public InfixExpression(TypeMirror typeMirror, Operator operator, Expression... operands) {
    this.typeMirror = typeMirror;
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
  public TypeMirror getTypeMirror() {
    return typeMirror;
  }

  public InfixExpression setTypeMirror(TypeMirror newType) {
    typeMirror = newType;
    return this;
  }

  public Operator getOperator() {
    return operator;
  }

  public InfixExpression setOperator(Operator newOp) {
    operator = newOp;
    return this;
  }

  public InfixExpression addOperand(Expression operand) {
    operands.add(operand);
    return this;
  }

  public void addOperand(int index, Expression operand) {
    operands.add(index, operand);
  }

  public Expression getOperand(int index) {
    return operands.get(index);
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
