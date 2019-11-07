/*
 * AnonymousObjectCreationExpression.java
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

public class AnonymousObjectCreationExpression extends ObjectCreationExpression {

    public AnonymousObjectCreationExpression( int offset, final TypeDeclaration typeDeclaration, final AstType type) {
        super( offset, type);
        setTypeDeclaration(typeDeclaration);
    }

    public AnonymousObjectCreationExpression(
        int offset,
        final TypeDeclaration typeDeclaration,
        final AstType type,
        final Expression... arguments) {

        super( offset, type, arguments);
        setTypeDeclaration(typeDeclaration);
    }

    public AnonymousObjectCreationExpression(
        final int offset,
        final TypeDeclaration typeDeclaration,
        final AstType type,
        final Iterable<Expression> arguments) {

        super( offset, type, arguments);
        setTypeDeclaration(typeDeclaration);
    }

    public final TypeDeclaration getTypeDeclaration() {
        return getChildByRole(Roles.LOCAL_TYPE_DECLARATION);
    }

    public final void setTypeDeclaration(final TypeDeclaration value) {
        setChildByRole(Roles.LOCAL_TYPE_DECLARATION, value);
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitAnonymousObjectCreationExpression(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof AnonymousObjectCreationExpression) {
            final AnonymousObjectCreationExpression otherExpression = (AnonymousObjectCreationExpression) other;

            return super.matches(other, match) &&
                   getTypeDeclaration().matches(otherExpression.getTypeDeclaration(), match);
        }

        return false;
    }
}
