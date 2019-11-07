/*
 * ConditionalExpression.java
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

public class ConditionalExpression extends Expression {
    public final static Role<Expression> CONDITION_ROLE = Roles.CONDITION;
    public final static TokenRole QUESTION_MARK_ROLE = new TokenRole("?", TokenRole.FLAG_OPERATOR);
    public final static Role<Expression> TRUE_ROLE = new Role<>("True", Expression.class, Expression.NULL);
    public final static TokenRole COLON_ROLE = new TokenRole(":", TokenRole.FLAG_OPERATOR);
    public final static Role<Expression> FALSE_ROLE = new Role<>("False", Expression.class, Expression.NULL);

    public ConditionalExpression(final Expression condition, final Expression trueExpression, final Expression falseExpression) {
        super( condition.getOffset());
        addChild(condition, CONDITION_ROLE);
        addChild(trueExpression, TRUE_ROLE);
        addChild(falseExpression, FALSE_ROLE);
    }

    public final JavaTokenNode getQuestionMark() {
        return getChildByRole(QUESTION_MARK_ROLE);
    }

    public final JavaTokenNode getColonToken() {
        return getChildByRole(COLON_ROLE);
    }

    public final Expression getCondition() {
        return getChildByRole(CONDITION_ROLE);
    }

    public final void setCondition(final Expression value) {
        setChildByRole(CONDITION_ROLE, value);
    }

    public final Expression getTrueExpression() {
        return getChildByRole(TRUE_ROLE);
    }

    public final void setTrueExpression(final Expression value) {
        setChildByRole(TRUE_ROLE, value);
    }

    public final Expression getFalseExpression() {
        return getChildByRole(FALSE_ROLE);
    }

    public final void setFalseExpression(final Expression value) {
        setChildByRole(FALSE_ROLE, value);
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitConditionalExpression(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof ConditionalExpression) {
            final ConditionalExpression otherCondition = (ConditionalExpression) other;

            return !other.isNull() &&
                   getCondition().matches(otherCondition.getCondition(), match) &&
                   getTrueExpression().matches(otherCondition.getTrueExpression(), match) &&
                   getFalseExpression().matches(otherCondition.getFalseExpression(), match);
        }

        return false;
    }
}
