/*
 * IdentifierExpression.java
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

public class IdentifierExpression extends Expression {
    public IdentifierExpression( int offset, final String identifier) {
        super( offset);
        setIdentifier(identifier);
    }

    public IdentifierExpression( int offset, final Identifier identifier) {
        super( offset);
        setIdentifierToken(identifier);
    }

    public final String getIdentifier() {
        return getChildByRole(Roles.IDENTIFIER).getName();
    }

    public final void setIdentifier(final String value) {
        setChildByRole(Roles.IDENTIFIER, Identifier.create(value));
    }

    public final Identifier getIdentifierToken() {
        return getChildByRole(Roles.IDENTIFIER);
    }

    public final void setIdentifierToken(final Identifier value) {
        setChildByRole(Roles.IDENTIFIER, value);
    }

    public final AstNodeCollection<AstType> getTypeArguments() {
        return getChildrenByRole(Roles.TYPE_ARGUMENT);
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitIdentifierExpression(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof IdentifierExpression) {
            final IdentifierExpression otherIdentifier = (IdentifierExpression) other;

            return !otherIdentifier.isNull() &&
                   matchString(getIdentifier(), otherIdentifier.getIdentifier()) &&
                   getTypeArguments().matches(otherIdentifier.getTypeArguments(), match);
        }

        return false;
    }
}
