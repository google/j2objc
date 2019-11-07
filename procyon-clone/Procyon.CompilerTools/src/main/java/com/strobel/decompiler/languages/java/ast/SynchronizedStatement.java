/*
 * SynchronizedStatement.java
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

public class SynchronizedStatement extends Statement {
    public final static TokenRole SYNCHRONIZED_KEYWORD_ROLE = new TokenRole("synchronized", TokenRole.FLAG_KEYWORD);

    public SynchronizedStatement( int offset) {
        super( offset);
    }
    
    public final Statement getEmbeddedStatement() {
        return getChildByRole(Roles.EMBEDDED_STATEMENT);
    }

    public final void setEmbeddedStatement(final Statement value) {
        setChildByRole(Roles.EMBEDDED_STATEMENT, value);
    }

    public final Expression getExpression() {
        return getChildByRole(Roles.EXPRESSION);
    }

    public final void setExpression(final Expression value) {
        setChildByRole(Roles.EXPRESSION, value);
    }

    public final JavaTokenNode getSynchronizedToken() {
        return getChildByRole(SYNCHRONIZED_KEYWORD_ROLE);
    }

    public final JavaTokenNode getLeftParenthesisToken() {
        return getChildByRole(Roles.LEFT_PARENTHESIS);
    }

    public final JavaTokenNode getRightParenthesisToken() {
        return getChildByRole(Roles.RIGHT_PARENTHESIS);
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitSynchronizedStatement(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof SynchronizedStatement) {
            final SynchronizedStatement otherStatement = (SynchronizedStatement) other;

            return !otherStatement.isNull() &&
                   getExpression().matches(otherStatement.getExpression(), match) &&
                   getEmbeddedStatement().matches(otherStatement.getEmbeddedStatement(), match);
        }

        return false;
    }
}
