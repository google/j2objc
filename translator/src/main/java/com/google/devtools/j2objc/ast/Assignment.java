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
 * Node type for an assignment.
 */
public class Assignment extends Expression {

  /**
   * Assignment operators.
   */
  public static enum Operator {
    ASSIGN("=", "Assign"),
    PLUS_ASSIGN("+=", "PlusAssign"),
    MINUS_ASSIGN("-=", "MinusAssign"),
    TIMES_ASSIGN("*=", "TimesAssign"),
    DIVIDE_ASSIGN("/=", "DivideAssign"),
    BIT_AND_ASSIGN("&=", "BitAndAssign"),
    BIT_OR_ASSIGN("|=", "BitOrAssign"),
    BIT_XOR_ASSIGN("^=", "BitXorAssign"),
    REMAINDER_ASSIGN("%=", "ModAssign"),
    LEFT_SHIFT_ASSIGN("<<=", "LShiftAssign"),
    RIGHT_SHIFT_SIGNED_ASSIGN(">>=", "RShiftAssign"),
    RIGHT_SHIFT_UNSIGNED_ASSIGN(">>>=", "URShiftAssign");

    private final String opString;
    private final String name;
    private static Map<String, Operator> stringLookup = Maps.newHashMap();

    static {
      for (Operator operator : Operator.values()) {
        stringLookup.put(operator.toString(), operator);
      }
    }

    private Operator(String opString, String name) {
      this.opString = opString;
      this.name = name;
    }

    public String getName() {
      return name;
    }

    @Override
    public String toString() {
      return opString;
    }

    public static Operator fromJdtOperator(
        org.eclipse.jdt.core.dom.Assignment.Operator jdtOperator) {
      Operator result = stringLookup.get(jdtOperator.toString());
      assert result != null;
      return result;
    }
  }

  private Operator operator;
  private ChildLink<Expression> leftHandSide = ChildLink.create(Expression.class, this);
  private ChildLink<Expression> rightHandSide = ChildLink.create(Expression.class, this);

  public Assignment(org.eclipse.jdt.core.dom.Assignment jdtNode) {
    super(jdtNode);
    operator = Operator.fromJdtOperator(jdtNode.getOperator());
    leftHandSide.set((Expression) TreeConverter.convert(jdtNode.getLeftHandSide()));
    rightHandSide.set((Expression) TreeConverter.convert(jdtNode.getRightHandSide()));
  }

  public Assignment(Assignment other) {
    super(other);
    operator = other.getOperator();
    leftHandSide.copyFrom(other.getLeftHandSide());
    rightHandSide.copyFrom(other.getRightHandSide());
  }

  public Assignment(Expression lhs, Expression rhs) {
    operator = Operator.ASSIGN;
    leftHandSide.set(lhs);
    rightHandSide.set(rhs);
  }

  @Override
  public Kind getKind() {
    return Kind.ASSIGNMENT;
  }

  @Override
  public ITypeBinding getTypeBinding() {
    Expression leftHandSideNode = leftHandSide.get();
    return leftHandSideNode != null ? leftHandSideNode.getTypeBinding() : null;
  }

  public Operator getOperator() {
    return operator;
  }

  public void setOperator(Operator newOperator) {
    operator = newOperator;
  }

  public Expression getLeftHandSide() {
    return leftHandSide.get();
  }

  public void setLeftHandSide(Expression newLeftHandSide) {
    leftHandSide.set(newLeftHandSide);
  }

  public Expression getRightHandSide() {
    return rightHandSide.get();
  }

  public void setRightHandSide(Expression newRightHandSide) {
    rightHandSide.set(newRightHandSide);
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      leftHandSide.accept(visitor);
      rightHandSide.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public Assignment copy() {
    return new Assignment(this);
  }
}
