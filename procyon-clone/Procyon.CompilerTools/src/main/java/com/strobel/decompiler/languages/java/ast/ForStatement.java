/*
 * ForStatement.java
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

public class ForStatement extends Statement {
    public final static TokenRole FOR_KEYWORD_ROLE = new TokenRole("for", TokenRole.FLAG_KEYWORD);
    public final static Role<Statement> INITIALIZER_ROLE = new Role<>("Initializer", Statement.class, Statement.NULL);
    public final static Role<Statement> ITERATOR_ROLE = new Role<>("Iterator", Statement.class, Statement.NULL);

    public ForStatement( int offset) {
        super( offset);
    }
    
    public final JavaTokenNode getForToken() {
        return getChildByRole(FOR_KEYWORD_ROLE);
    }

    public final Statement getEmbeddedStatement() {
        return getChildByRole(Roles.EMBEDDED_STATEMENT);
    }

    public final void setEmbeddedStatement(final Statement value) {
        setChildByRole(Roles.EMBEDDED_STATEMENT, value);
    }

    public final Expression getCondition() {
        return getChildByRole(Roles.CONDITION);
    }

    public final void setCondition(final Expression value) {
        setChildByRole(Roles.CONDITION, value);
    }

    public final JavaTokenNode getLeftParenthesisToken() {
        return getChildByRole(Roles.LEFT_PARENTHESIS);
    }

    public final JavaTokenNode getRightParenthesisToken() {
        return getChildByRole(Roles.RIGHT_PARENTHESIS);
    }

    public final AstNodeCollection<Statement> getInitializers() {
        return getChildrenByRole(INITIALIZER_ROLE);
    }

    public final AstNodeCollection<Statement> getIterators() {
        return getChildrenByRole(ITERATOR_ROLE);
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitForStatement(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof ForStatement) {
            final ForStatement otherStatement = (ForStatement) other;

            return !other.isNull() &&
                   getInitializers().matches(otherStatement.getInitializers(), match) &&
                   getCondition().matches(otherStatement.getCondition(), match) &&
                   getIterators().matches(otherStatement.getIterators(), match) &&
                   getEmbeddedStatement().matches(otherStatement.getEmbeddedStatement(), match);
        }

        return false;
    }
}
