/*
 * UnaryOperatorExpression.java
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

public class UnaryOperatorExpression extends Expression {
    public final static TokenRole NOT_ROLE = new TokenRole("!");
    public final static TokenRole BITWISE_NOT_ROLE = new TokenRole("~");
    public final static TokenRole MINUS_ROLE = new TokenRole("-");
    public final static TokenRole PLUS_ROLE = new TokenRole("+");
    public final static TokenRole INCREMENT_ROLE = new TokenRole("++");
    public final static TokenRole DECREMENT_ROLE = new TokenRole("--");
    public final static TokenRole DEREFERENCE_ROLE = new TokenRole("*");
    public final static TokenRole ADDRESS_OF_ROLE = new TokenRole("&");

    private UnaryOperatorType _operator;

    public UnaryOperatorExpression(final UnaryOperatorType operator, final Expression expression) {
        super( expression.getOffset());
        setOperator(operator);
        setExpression(expression);
    }

    public final UnaryOperatorType getOperator() {
        return _operator;
    }

    public final void setOperator(final UnaryOperatorType operator) {
        verifyNotFrozen();
        _operator = operator;
    }

    public final JavaTokenNode getOperatorToken() {
        return getChildByRole(getOperatorRole(getOperator()));
    }

    public final Expression getExpression() {
        return getChildByRole(Roles.EXPRESSION);
    }

    public final void setExpression(final Expression value) {
        setChildByRole(Roles.EXPRESSION, value);
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitUnaryOperatorExpression(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof UnaryOperatorExpression) {
            final UnaryOperatorExpression otherOperator = (UnaryOperatorExpression) other;

            return !otherOperator.isNull() &&
                   (otherOperator._operator == _operator ||
                    _operator == UnaryOperatorType.ANY ||
                    otherOperator._operator == UnaryOperatorType.ANY) &&
                   getExpression().matches(otherOperator.getExpression(), match);
        }

        return false;
    }

    public static TokenRole getOperatorRole(final UnaryOperatorType operator) {
        switch (operator) {
            case NOT:
                return NOT_ROLE;

            case BITWISE_NOT:
                return BITWISE_NOT_ROLE;

            case MINUS:
                return MINUS_ROLE;

            case PLUS:
                return PLUS_ROLE;

            case INCREMENT:
                return INCREMENT_ROLE;

            case DECREMENT:
                return DECREMENT_ROLE;

            case POST_INCREMENT:
                return INCREMENT_ROLE;

            case POST_DECREMENT:
                return DECREMENT_ROLE;
        }

        throw new IllegalArgumentException("Invalid value for UnaryOperatorType.");
    }
}