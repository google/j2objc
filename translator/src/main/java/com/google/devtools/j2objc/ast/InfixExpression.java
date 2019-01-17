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

import com.sun.source.tree.Tree;
import java.util.EnumMap;
import java.util.List;
import javax.lang.model.type.TypeMirror;

/**
 * Infix expression node type.
 */
public class InfixExpression extends Expression {

  /**
   * Infix operators.
   */
  public enum Operator {
    TIMES("*", Tree.Kind.MULTIPLY),
    DIVIDE("/", Tree.Kind.DIVIDE),
    REMAINDER("%", Tree.Kind.REMAINDER),
    PLUS("+", Tree.Kind.PLUS),
    MINUS("-", Tree.Kind.MINUS),
    LEFT_SHIFT("<<", Tree.Kind.LEFT_SHIFT),
    RIGHT_SHIFT_SIGNED(">>", Tree.Kind.RIGHT_SHIFT),
    RIGHT_SHIFT_UNSIGNED(">>>", Tree.Kind.UNSIGNED_RIGHT_SHIFT),
    LESS("<", Tree.Kind.LESS_THAN),
    GREATER(">", Tree.Kind.GREATER_THAN),
    LESS_EQUALS("<=", Tree.Kind.LESS_THAN_EQUAL),
    GREATER_EQUALS(">=", Tree.Kind.GREATER_THAN_EQUAL),
    EQUALS("==", Tree.Kind.EQUAL_TO),
    NOT_EQUALS("!=", Tree.Kind.NOT_EQUAL_TO),
    XOR("^", Tree.Kind.XOR),
    AND("&", Tree.Kind.AND),
    OR("|", Tree.Kind.OR),
    CONDITIONAL_AND("&&", Tree.Kind.CONDITIONAL_AND),
    CONDITIONAL_OR("||", Tree.Kind.CONDITIONAL_OR);

    private final String opString;
    private final Tree.Kind javacKind;
    private static final EnumMap<Tree.Kind, Operator> javacKindLookup =
        new EnumMap<>(Tree.Kind.class);

    static {
      for (Operator operator : Operator.values()) {
        javacKindLookup.put(operator.javacKind, operator);
      }
    }

    Operator(String opString, Tree.Kind javacKind) {
      this.opString = opString;
      this.javacKind = javacKind;
    }

    @Override
    public String toString() {
      return opString;
    }

    public static Operator from(Tree.Kind kind) {
      Operator result = javacKindLookup.get(kind);
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
