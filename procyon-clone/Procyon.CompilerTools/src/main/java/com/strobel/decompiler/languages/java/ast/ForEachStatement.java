/*
 * ForEachStatement.java
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

import javax.lang.model.element.Modifier;
import java.util.List;

public class ForEachStatement extends Statement {
    public final static TokenRole FOR_KEYWORD_ROLE = ForStatement.FOR_KEYWORD_ROLE;
    public final static TokenRole COLON_ROLE = new TokenRole(":", TokenRole.FLAG_OPERATOR);

    public ForEachStatement( int offset) {
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

    public final AstType getVariableType() {
        return getChildByRole(Roles.TYPE);
    }

    public final void setVariableType(final AstType value) {
        setChildByRole(Roles.TYPE, value);
    }

    public final String getVariableName() {
        return getChildByRole(Roles.IDENTIFIER).getName();
    }

    public final void setVariableName(final String value) {
        setChildByRole(Roles.IDENTIFIER, Identifier.create(value));
    }

    public final Identifier getVariableNameToken() {
        return getChildByRole(Roles.IDENTIFIER);
    }

    public final void setVariableNameToken(final Identifier value) {
        setChildByRole(Roles.IDENTIFIER, value);
    }

    public final List<Modifier> getVariableModifiers() {
        return EntityDeclaration.getModifiers(this);
    }

    public final void addVariableModifier(final Modifier modifier) {
        EntityDeclaration.addModifier(this, modifier);
    }

    public final void removeVariableModifier(final Modifier modifier) {
        EntityDeclaration.removeModifier(this, modifier);
    }

    public final void setVariableModifiers(final List<Modifier> modifiers) {
        EntityDeclaration.setModifiers(this, modifiers);
    }

    public final JavaTokenNode getLeftParenthesisToken() {
        return getChildByRole(Roles.LEFT_PARENTHESIS);
    }

    public final JavaTokenNode getRightParenthesisToken() {
        return getChildByRole(Roles.RIGHT_PARENTHESIS);
    }

    public final Expression getInExpression() {
        return getChildByRole(Roles.EXPRESSION);
    }

    public final void setInExpression(final Expression value) {
        setChildByRole(Roles.EXPRESSION, value);
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitForEachStatement(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof ForEachStatement) {
            final ForEachStatement otherStatement = (ForEachStatement) other;

            return !other.isNull() &&
                   getVariableType().matches(otherStatement.getVariableType(), match) &&
                   matchString(getVariableName(), otherStatement.getVariableName()) &&
                   getInExpression().matches(otherStatement.getInExpression(), match) &&
                   getEmbeddedStatement().matches(otherStatement.getEmbeddedStatement(), match);
        }

        return false;
    }
}
