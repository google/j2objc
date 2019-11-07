/*
 * IfElseStatement.java
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

public class IfElseStatement extends Statement {
    public final static TokenRole IF_KEYWORD_ROLE = new TokenRole("if", TokenRole.FLAG_KEYWORD);
    public final static TokenRole ELSE_KEYWORD_ROLE = new TokenRole("else", TokenRole.FLAG_KEYWORD);
    public final static Role<Expression> CONDITION_ROLE = Roles.CONDITION;
    public final static Role<Statement> TRUE_ROLE = new Role<>("True", Statement.class, Statement.NULL);
    public final static Role<Statement> FALSE_ROLE = new Role<>("False", Statement.class, Statement.NULL);

    public IfElseStatement(final int offset, final Expression condition, final Statement trueStatement) {
        this(offset, condition, trueStatement, null);
    }

    public IfElseStatement(final int offset, final Expression condition, final Statement trueStatement, final Statement falseStatement) {
        super( offset);
        setCondition(condition);
        setTrueStatement(trueStatement);
        setFalseStatement(falseStatement);
    }

    public final JavaTokenNode getIfToken() {
        return getChildByRole(IF_KEYWORD_ROLE);
    }

    public final JavaTokenNode getElseToken() {
        return getChildByRole(IF_KEYWORD_ROLE);
    }

    public final JavaTokenNode getLeftParenthesisToken() {
        return getChildByRole(Roles.LEFT_PARENTHESIS);
    }

    public final JavaTokenNode getRightParenthesisToken() {
        return getChildByRole(Roles.RIGHT_PARENTHESIS);
    }

    public final Expression getCondition() {
        return getChildByRole(CONDITION_ROLE);
    }

    public final void setCondition(final Expression value) {
        setChildByRole(CONDITION_ROLE, value);
    }

    public final Statement getTrueStatement() {
        return getChildByRole(TRUE_ROLE);
    }

    public final void setTrueStatement(final Statement value) {
        setChildByRole(TRUE_ROLE, value);
    }

    public final Statement getFalseStatement() {
        return getChildByRole(FALSE_ROLE);
    }

    public final void setFalseStatement(final Statement value) {
        setChildByRole(FALSE_ROLE, value);
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitIfElseStatement(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof IfElseStatement) {
            final IfElseStatement otherStatement = (IfElseStatement) other;

            return !other.isNull() &&
                   getCondition().matches(otherStatement.getCondition(), match) &&
                   getTrueStatement().matches(otherStatement.getTrueStatement(), match) &&
                   getFalseStatement().matches(otherStatement.getFalseStatement(), match);
        }

        return false;
    }
}
