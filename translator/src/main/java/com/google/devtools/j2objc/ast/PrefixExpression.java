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
import javax.lang.model.type.TypeMirror;

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
    POSITIVE("+"),
    NEGATIVE("-"),
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
      // javac uses "+++" and "---" to differentiate from infix operators.
      stringLookup.put("+++", POSITIVE);
      stringLookup.put("---", NEGATIVE);
    }

    private Operator(String opString) {
      this.opString = opString;
    }

    @Override
    public String toString() {
      return opString;
    }

    public static Operator parse(String newOp) {
      Operator result = stringLookup.get(newOp);
      assert result != null;
      return result;
    }
  }

  private TypeMirror typeMirror = null;
  private Operator operator = null;
  private ChildLink<Expression> operand = ChildLink.create(Expression.class, this);

  public PrefixExpression() {}

  public PrefixExpression(PrefixExpression other) {
    super(other);
    typeMirror = other.getTypeMirror();
    operator = other.getOperator();
    operand.copyFrom(other.getOperand());
  }

  public PrefixExpression(TypeMirror typeMirror, Operator operator, Expression operand) {
    this.typeMirror = typeMirror;
    this.operator = operator;
    this.operand.set(operand);
  }

  @Override
  public Kind getKind() {
    return Kind.PREFIX_EXPRESSION;
  }

  @Override
  public TypeMirror getTypeMirror() {
    return typeMirror;
  }

  public PrefixExpression setTypeMirror(TypeMirror newMirror) {
    typeMirror = newMirror;
    return this;
  }

  public Operator getOperator() {
    return operator;
  }

  public PrefixExpression setOperator(Operator newOp) {
    operator = newOp;
    return this;
  }

  public Expression getOperand() {
    return operand.get();
  }

  public PrefixExpression setOperand(Expression newOperand) {
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
  public PrefixExpression copy() {
    return new PrefixExpression(this);
  }
}
