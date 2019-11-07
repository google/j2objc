/*
 * BinaryOperatorExpression.java
 *
 * Copyright (c) 2013 Mike Strobel
 *
 * This source code is based on Mono.Cecil from Jb Evain, Copyright (c) Jb Evain;
 * and ILSpy/ICSharpCode from SharpDevelop, Copyright (c) AlphaSierraPapa.
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.INode;
import com.strobel.decompiler.patterns.Match;
import com.strobel.decompiler.patterns.Role;

public class BinaryOperatorExpression extends Expression {
    public final static TokenRole BITWISE_AND_ROLE = new TokenRole("&", TokenRole.FLAG_OPERATOR);
    public final static TokenRole BITWISE_OR_ROLE = new TokenRole("|", TokenRole.FLAG_OPERATOR);
    public final static TokenRole LOGICAL_AND_ROLE = new TokenRole("&&", TokenRole.FLAG_OPERATOR);
    public final static TokenRole LOGICAL_OR_ROLE = new TokenRole("||", TokenRole.FLAG_OPERATOR);
    public final static TokenRole EXCLUSIVE_OR_ROLE = new TokenRole("^", TokenRole.FLAG_OPERATOR);
    public final static TokenRole GREATER_THAN_ROLE = new TokenRole(">", TokenRole.FLAG_OPERATOR);
    public final static TokenRole GREATER_THAN_OR_EQUAL_ROLE = new TokenRole(">=", TokenRole.FLAG_OPERATOR);
    public final static TokenRole EQUALITY_ROLE = new TokenRole("==", TokenRole.FLAG_OPERATOR);
    public final static TokenRole IN_EQUALITY_ROLE = new TokenRole("!=", TokenRole.FLAG_OPERATOR);
    public final static TokenRole LESS_THAN_ROLE = new TokenRole("<", TokenRole.FLAG_OPERATOR);
    public final static TokenRole LESS_THAN_OR_EQUAL_ROLE = new TokenRole("<=", TokenRole.FLAG_OPERATOR);
    public final static TokenRole ADD_ROLE = new TokenRole("+", TokenRole.FLAG_OPERATOR);
    public final static TokenRole SUBTRACT_ROLE = new TokenRole("-", TokenRole.FLAG_OPERATOR);
    public final static TokenRole MULTIPLY_ROLE = new TokenRole("*", TokenRole.FLAG_OPERATOR);
    public final static TokenRole DIVIDE_ROLE = new TokenRole("/", TokenRole.FLAG_OPERATOR);
    public final static TokenRole MODULUS_ROLE = new TokenRole("%", TokenRole.FLAG_OPERATOR);
    public final static TokenRole SHIFT_LEFT_ROLE = new TokenRole("<<", TokenRole.FLAG_OPERATOR);
    public final static TokenRole SHIFT_RIGHT_ROLE = new TokenRole(">>", TokenRole.FLAG_OPERATOR);
    public final static TokenRole UNSIGNED_SHIFT_RIGHT_ROLE = new TokenRole(">>>", TokenRole.FLAG_OPERATOR);
    public final static TokenRole ANY_ROLE = new TokenRole("(op)", TokenRole.FLAG_OPERATOR);

    public final static Role<Expression> LEFT_ROLE = new Role<>("Left", Expression.class, Expression.NULL);
    public final static Role<Expression> RIGHT_ROLE = new Role<>("Right", Expression.class, Expression.NULL);

    private BinaryOperatorType _operator;

    public BinaryOperatorExpression(final Expression left, final BinaryOperatorType operator, final Expression right) {
        super( left.getOffset());
        setLeft(left);
        setOperator(operator);
        setRight(right);
    }

    public final BinaryOperatorType getOperator() {
        return _operator;
    }

    public final void setOperator(final BinaryOperatorType operator) {
        verifyNotFrozen();
        _operator = operator;
    }

    public final JavaTokenNode getOperatorToken() {
        return getChildByRole(getOperatorRole(getOperator()));
    }

    public final Expression getLeft() {
        return getChildByRole(LEFT_ROLE);
    }

    public final void setLeft(final Expression value) {
        setChildByRole(LEFT_ROLE, value);
    }

    public final Expression getRight() {
        return getChildByRole(RIGHT_ROLE);
    }

    public final void setRight(final Expression value) {
        setChildByRole(RIGHT_ROLE, value);
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitBinaryOperatorExpression(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof BinaryOperatorExpression) {
            final BinaryOperatorExpression otherExpression = (BinaryOperatorExpression) other;

            return !otherExpression.isNull() &&
                   (otherExpression._operator == _operator ||
                    _operator == BinaryOperatorType.ANY ||
                    otherExpression._operator == BinaryOperatorType.ANY) &&
                   getLeft().matches(otherExpression.getLeft(), match) &&
                   getRight().matches(otherExpression.getRight(), match);
        }

        return false;
    }

    public static TokenRole getOperatorRole(final BinaryOperatorType operator) {
        switch (operator) {
            case BITWISE_AND:
                return BITWISE_AND_ROLE;

            case BITWISE_OR:
                return BITWISE_OR_ROLE;

            case LOGICAL_AND:
                return LOGICAL_AND_ROLE;

            case LOGICAL_OR:
                return LOGICAL_OR_ROLE;

            case EXCLUSIVE_OR:
                return EXCLUSIVE_OR_ROLE;

            case GREATER_THAN:
                return GREATER_THAN_ROLE;

            case GREATER_THAN_OR_EQUAL:
                return GREATER_THAN_OR_EQUAL_ROLE;

            case EQUALITY:
                return EQUALITY_ROLE;

            case INEQUALITY:
                return IN_EQUALITY_ROLE;

            case LESS_THAN:
                return LESS_THAN_ROLE;

            case LESS_THAN_OR_EQUAL:
                return LESS_THAN_OR_EQUAL_ROLE;

            case ADD:
                return ADD_ROLE;

            case SUBTRACT:
                return SUBTRACT_ROLE;

            case MULTIPLY:
                return MULTIPLY_ROLE;

            case DIVIDE:
                return DIVIDE_ROLE;

            case MODULUS:
                return MODULUS_ROLE;

            case SHIFT_LEFT:
                return SHIFT_LEFT_ROLE;

            case SHIFT_RIGHT:
                return SHIFT_RIGHT_ROLE;

            case UNSIGNED_SHIFT_RIGHT:
                return UNSIGNED_SHIFT_RIGHT_ROLE;

            case ANY:
                return ANY_ROLE;
        }

        throw new IllegalArgumentException("Invalid value for BinaryOperatorType.");
    }
}
