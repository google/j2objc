/*
 * SwitchStatement.java
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

public class SwitchStatement extends Statement {
    public final static TokenRole SWITCH_KEYWORD_ROLE = new TokenRole("switch", TokenRole.FLAG_KEYWORD);
    public final static Role<SwitchSection> SWITCH_SECTION_ROLE = new Role<>("SwitchSection", SwitchSection.class);

    public SwitchStatement(final Expression testExpression) {
        super( testExpression.getOffset());
        setExpression(testExpression);
    }

    public final JavaTokenNode getReturnToken() {
        return getChildByRole(SWITCH_KEYWORD_ROLE);
    }

    public final Expression getExpression() {
        return getChildByRole(Roles.EXPRESSION);
    }

    public final void setExpression(final Expression value) {
        setChildByRole(Roles.EXPRESSION, value);
    }

    public final JavaTokenNode getLeftParenthesisToken() {
        return getChildByRole(Roles.LEFT_PARENTHESIS);
    }

    public final JavaTokenNode getRightParenthesisToken() {
        return getChildByRole(Roles.RIGHT_PARENTHESIS);
    }

    public final JavaTokenNode getLeftBraceToken() {
        return getChildByRole(Roles.LEFT_BRACE);
    }

    public final AstNodeCollection<SwitchSection> getSwitchSections() {
        return getChildrenByRole(SWITCH_SECTION_ROLE);
    }

    public final JavaTokenNode getRightBraceToken() {
        return getChildByRole(Roles.RIGHT_BRACE);
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitSwitchStatement(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof SwitchStatement) {
            final SwitchStatement otherStatement = (SwitchStatement) other;

            return !otherStatement.isNull() &&
                   getExpression().matches(otherStatement.getExpression(), match) &&
                   getSwitchSections().matches(otherStatement.getSwitchSections(), match);
        }

        return false;
    }
}
