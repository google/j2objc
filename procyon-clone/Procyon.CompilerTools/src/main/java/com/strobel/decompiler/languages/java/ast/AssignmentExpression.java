/*
 * AssignmentExpression.java
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

public class AssignmentExpression extends Expression {
    public final static Role<Expression> LEFT_ROLE = BinaryOperatorExpression.LEFT_ROLE;
    public final static Role<Expression> RIGHT_ROLE = BinaryOperatorExpression.RIGHT_ROLE;

    public final static TokenRole ASSIGN_ROLE = new TokenRole("=", TokenRole.FLAG_OPERATOR);
    public final static TokenRole ADD_ROLE = new TokenRole("+=", TokenRole.FLAG_OPERATOR);
    public final static TokenRole SUBTRACT_ROLE = new TokenRole("-=", TokenRole.FLAG_OPERATOR);
    public final static TokenRole MULTIPLY_ROLE = new TokenRole("*=", TokenRole.FLAG_OPERATOR);
    public final static TokenRole DIVIDE_ROLE = new TokenRole("/=", TokenRole.FLAG_OPERATOR);
    public final static TokenRole MODULUS_ROLE = new TokenRole("%=", TokenRole.FLAG_OPERATOR);
    public final static TokenRole SHIFT_LEFT_ROLE = new TokenRole("<<=", TokenRole.FLAG_OPERATOR);
    public final static TokenRole SHIFT_RIGHT_ROLE = new TokenRole(">>=", TokenRole.FLAG_OPERATOR);
    public final static TokenRole UNSIGNED_SHIFT_RIGHT_ROLE = new TokenRole(">>>=", TokenRole.FLAG_OPERATOR);
    public final static TokenRole BITWISE_AND_ROLE = new TokenRole("&=", TokenRole.FLAG_OPERATOR);
    public final static TokenRole BITWISE_OR_ROLE = new TokenRole("|=", TokenRole.FLAG_OPERATOR);
    public final static TokenRole EXCLUSIVE_OR_ROLE = new TokenRole("^=", TokenRole.FLAG_OPERATOR);
    public final static TokenRole ANY_ROLE = new TokenRole("(assign)", TokenRole.FLAG_OPERATOR);

    private AssignmentOperatorType _operator;

    public AssignmentExpression(final Expression left, final Expression right) {
        super( left.getOffset());
        setLeft(left);
        setOperator(AssignmentOperatorType.ASSIGN);
        setRight(right);
    }

    public AssignmentExpression(final Expression left, final AssignmentOperatorType operator, final Expression right) {
        super( left.getOffset());
        setLeft(left);
        setOperator(operator);
        setRight(right);
    }

    public final AssignmentOperatorType getOperator() {
        return _operator;
    }

    public final void setOperator(final AssignmentOperatorType operator) {
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
        return visitor.visitAssignmentExpression(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof AssignmentExpression) {
            final AssignmentExpression otherExpression = (AssignmentExpression) other;

            return !otherExpression.isNull() &&
                   (otherExpression._operator == _operator ||
                    _operator == AssignmentOperatorType.ANY ||
                    otherExpression._operator == AssignmentOperatorType.ANY) &&
                   getLeft().matches(otherExpression.getLeft(), match) &&
                   getRight().matches(otherExpression.getRight(), match);
        }

        return false;
    }

    public static TokenRole getOperatorRole(final AssignmentOperatorType operator) {
        switch (operator) {
            case ASSIGN:
                return ASSIGN_ROLE;
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
            case BITWISE_AND:
                return BITWISE_AND_ROLE;
            case BITWISE_OR:
                return BITWISE_OR_ROLE;
            case EXCLUSIVE_OR:
                return EXCLUSIVE_OR_ROLE;
            case ANY:
                return ANY_ROLE;
        }

        throw new IllegalArgumentException("Invalid value for AssignmentOperatorType");
    }

    public static BinaryOperatorType getCorrespondingBinaryOperator(final AssignmentOperatorType operator) {
        switch (operator) {
            case ASSIGN:
                return null;
            case ADD:
                return BinaryOperatorType.ADD;
            case SUBTRACT:
                return BinaryOperatorType.SUBTRACT;
            case MULTIPLY:
                return BinaryOperatorType.MULTIPLY;
            case DIVIDE:
                return BinaryOperatorType.DIVIDE;
            case MODULUS:
                return BinaryOperatorType.MODULUS;
            case SHIFT_LEFT:
                return BinaryOperatorType.SHIFT_LEFT;
            case SHIFT_RIGHT:
                return BinaryOperatorType.SHIFT_RIGHT;
            case UNSIGNED_SHIFT_RIGHT:
                return BinaryOperatorType.UNSIGNED_SHIFT_RIGHT;
            case BITWISE_AND:
                return BinaryOperatorType.BITWISE_AND;
            case BITWISE_OR:
                return BinaryOperatorType.BITWISE_OR;
            case EXCLUSIVE_OR:
                return BinaryOperatorType.EXCLUSIVE_OR;
            case ANY:
                return BinaryOperatorType.ANY;
            default:
                return null;
        }
    }

    public static AssignmentOperatorType getCorrespondingAssignmentOperator(final BinaryOperatorType operator) {
        switch (operator) {
            case ADD:
                return AssignmentOperatorType.ADD;
            case SUBTRACT:
                return AssignmentOperatorType.SUBTRACT;
            case MULTIPLY:
                return AssignmentOperatorType.MULTIPLY;
            case DIVIDE:
                return AssignmentOperatorType.DIVIDE;
            case MODULUS:
                return AssignmentOperatorType.MODULUS;
            case SHIFT_LEFT:
                return AssignmentOperatorType.SHIFT_LEFT;
            case SHIFT_RIGHT:
                return AssignmentOperatorType.SHIFT_RIGHT;
            case UNSIGNED_SHIFT_RIGHT:
                return AssignmentOperatorType.UNSIGNED_SHIFT_RIGHT;
            case BITWISE_AND:
                return AssignmentOperatorType.BITWISE_AND;
            case BITWISE_OR:
                return AssignmentOperatorType.BITWISE_OR;
            case EXCLUSIVE_OR:
                return AssignmentOperatorType.EXCLUSIVE_OR;
            case ANY:
                return AssignmentOperatorType.ANY;
            default:
                return null;
        }
    }
}

