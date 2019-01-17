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
import javax.lang.model.type.TypeMirror;

/**
 * Node type for an assignment.
 */
public final class Assignment extends Expression {

  /**
   * Assignment operators.
   */
  public enum Operator {
    ASSIGN("=", "Assign", Tree.Kind.ASSIGNMENT),
    PLUS_ASSIGN("+=", "PlusAssign", Tree.Kind.PLUS_ASSIGNMENT),
    MINUS_ASSIGN("-=", "MinusAssign", Tree.Kind.MINUS_ASSIGNMENT),
    TIMES_ASSIGN("*=", "TimesAssign", Tree.Kind.MULTIPLY_ASSIGNMENT),
    DIVIDE_ASSIGN("/=", "DivideAssign", Tree.Kind.DIVIDE_ASSIGNMENT),
    BIT_AND_ASSIGN("&=", "BitAndAssign", Tree.Kind.AND_ASSIGNMENT),
    BIT_OR_ASSIGN("|=", "BitOrAssign", Tree.Kind.OR_ASSIGNMENT),
    BIT_XOR_ASSIGN("^=", "BitXorAssign", Tree.Kind.XOR_ASSIGNMENT),
    REMAINDER_ASSIGN("%=", "ModAssign", Tree.Kind.REMAINDER_ASSIGNMENT),
    LEFT_SHIFT_ASSIGN("<<=", "LShiftAssign", Tree.Kind.LEFT_SHIFT_ASSIGNMENT),
    RIGHT_SHIFT_SIGNED_ASSIGN(">>=", "RShiftAssign", Tree.Kind.RIGHT_SHIFT_ASSIGNMENT),
    RIGHT_SHIFT_UNSIGNED_ASSIGN(">>>=", "URShiftAssign", Tree.Kind.UNSIGNED_RIGHT_SHIFT_ASSIGNMENT);

    private final String opString;
    private final String name;
    private final Tree.Kind javacKind;
    private static final EnumMap<Tree.Kind, Operator> javacKindLookup =
        new EnumMap<>(Tree.Kind.class);

    static {
      for (Operator operator : Operator.values()) {
        javacKindLookup.put(operator.javacKind, operator);
      }
    }

    Operator(String opString, String name, Tree.Kind javacKind) {
      this.opString = opString;
      this.name = name;
      this.javacKind = javacKind;
    }

    public String getName() {
      return name;
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

  private Operator operator;
  private ChildLink<Expression> leftHandSide = ChildLink.create(Expression.class, this);
  private ChildLink<Expression> rightHandSide = ChildLink.create(Expression.class, this);

  public Assignment() {}

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
  public TypeMirror getTypeMirror() {
    Expression leftHandSideNode = leftHandSide.get();
    return leftHandSideNode != null ? leftHandSideNode.getTypeMirror() : null;
  }

  public Operator getOperator() {
    return operator;
  }

  public Assignment setOperator(Operator newOperator) {
    operator = newOperator;
    return this;
  }

  public Expression getLeftHandSide() {
    return leftHandSide.get();
  }

  public Assignment setLeftHandSide(Expression newLeftHandSide) {
    leftHandSide.set(newLeftHandSide);
    return this;
  }

  public Expression getRightHandSide() {
    return rightHandSide.get();
  }

  public Assignment setRightHandSide(Expression newRightHandSide) {
    rightHandSide.set(newRightHandSide);
    return this;
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
