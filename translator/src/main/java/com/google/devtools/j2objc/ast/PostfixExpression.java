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
import java.util.Map;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Postfix expression node type.
 */
public class PostfixExpression extends Expression {

  /**
   * Postfix operators.
   */
  public static enum Operator {
    INCREMENT("++"),
    DECREMENT("--");

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

    public static Operator parse(String op) {
      Operator result = stringLookup.get(op);
      assert result != null;
      return result;
    }
  }

  private Operator operator = null;
  private ChildLink<Expression> operand = ChildLink.create(Expression.class, this);

  public PostfixExpression() {}

  public PostfixExpression(PostfixExpression other) {
    super(other);
    operator = other.getOperator();
    operand.copyFrom(other.getOperand());
  }

  public PostfixExpression(VariableElement var, Operator op) {
    operator = op;
    operand.set(new SimpleName(var));
  }

  @Override
  public Kind getKind() {
    return Kind.POSTFIX_EXPRESSION;
  }

  @Override
  public TypeMirror getTypeMirror() {
    Expression operandNode = operand.get();
    return operand != null ? operandNode.getTypeMirror() : null;
  }

  public Operator getOperator() {
    return operator;
  }

  public PostfixExpression setOperator(Operator newOp) {
    operator = newOp;
    return this;
  }

  public Expression getOperand() {
    return operand.get();
  }

  public PostfixExpression setOperand(Expression newOperand) {
    operand.set(newOperand);
    return this;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      operand.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public PostfixExpression copy() {
    return new PostfixExpression(this);
  }
}
