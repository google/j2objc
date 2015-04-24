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

import java.util.Map;

/**
 * Prefix expression node type.
 */
public class PrefixExpression extends Expression {

  /**
   * Prefix operators.
   */
  public static enum Operator {
    INCREMENT("++"),
    DECREMENT("--"),
    PLUS("+"),
    MINUS("-"),
    COMPLEMENT("~"),
    NOT("!"),
    DEREFERENCE("*"),
    ADDRESS_OF("&");

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
        org.eclipse.jdt.core.dom.PrefixExpression.Operator jdtOperator) {
      Operator result = stringLookup.get(jdtOperator.toString());
      assert result != null;
      return result;
    }
  }

  private ITypeBinding typeBinding = null;
  private Operator operator = null;
  private ChildLink<Expression> operand = ChildLink.create(Expression.class, this);

  public PrefixExpression(org.eclipse.jdt.core.dom.PrefixExpression jdtNode) {
    super(jdtNode);
    typeBinding = jdtNode.resolveTypeBinding();
    operator = Operator.fromJdtOperator(jdtNode.getOperator());
    operand.set((Expression) TreeConverter.convert(jdtNode.getOperand()));
  }

  public PrefixExpression(PrefixExpression other) {
    super(other);
    typeBinding = other.getTypeBinding();
    operator = other.getOperator();
    operand.copyFrom(other.getOperand());
  }

  public PrefixExpression(ITypeBinding typeBinding, Operator operator, Expression operand) {
    this.typeBinding = typeBinding;
    this.operator = operator;
    this.operand.set(operand);
  }

  @Override
  public Kind getKind() {
    return Kind.PREFIX_EXPRESSION;
  }

  @Override
  public ITypeBinding getTypeBinding() {
    return typeBinding;
  }

  public Operator getOperator() {
    return operator;
  }

  public Expression getOperand() {
    return operand.get();
  }

  public void setOperand(Expression newOperand) {
    operand.set(newOperand);
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      operand.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public PrefixExpression copy() {
    return new PrefixExpression(this);
  }
}
