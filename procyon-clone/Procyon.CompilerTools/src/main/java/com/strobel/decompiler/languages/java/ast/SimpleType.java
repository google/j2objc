/*
 * SimpleType.java
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

import com.strobel.decompiler.languages.TextLocation;
import com.strobel.decompiler.patterns.INode;
import com.strobel.decompiler.patterns.Match;

public class SimpleType extends AstType {
    public SimpleType(final String identifier) {
        this(identifier, EMPTY_TYPES);
    }

    public SimpleType(final Identifier identifier) {
        setIdentifierToken(identifier);
    }

    public SimpleType(final String identifier, final TextLocation location) {
        setChildByRole(Roles.IDENTIFIER, Identifier.create(identifier, location));
    }

    public SimpleType(final String identifier, final Iterable<AstType> typeArguments) {
        setIdentifier(identifier);

        if (typeArguments != null) {
            for (final AstType typeArgument : typeArguments) {
                addChild(typeArgument, Roles.TYPE_ARGUMENT);
            }
        }
    }

    public SimpleType(final String identifier, final AstType... typeArguments) {
        setIdentifier(identifier);

        if (typeArguments != null) {
            for (final AstType typeArgument : typeArguments) {
                addChild(typeArgument, Roles.TYPE_ARGUMENT);
            }
        }
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
        return visitor.visitSimpleType(this, data);
    }

    @Override
    public String toString() {
        final AstNodeCollection<AstType> typeArguments = getTypeArguments();

        if (typeArguments.isEmpty()) {
            return getIdentifier();
        }

        final StringBuilder sb = new StringBuilder(getIdentifier()).append('<');

        boolean first = true;

        for (final AstType typeArgument : typeArguments) {
            if (!first) {
                sb.append(", ");
            }

            first = false;
            sb.append(typeArgument);
        }

        return sb.append('>').toString();
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof SimpleType) {
            final SimpleType otherType = (SimpleType) other;

            return !other.isNull() &&
                   matchString(getIdentifier(), otherType.getIdentifier()) &&
                   getTypeArguments().matches(otherType.getTypeArguments(), match);
        }

        return false;
    }

//    @Override
//    public TypeReference toTypeReference() {
//        final TypeReference typeReference = super.toTypeReference();
//        final AstNodeCollection<AstType> astTypeArguments = getTypeArguments();
//
//        if (astTypeArguments.isEmpty() || !typeReference.isGenericType()) {
//            return typeReference;
//        }
//
//        final TypeReference[] typeArguments = new TypeReference[astTypeArguments.size()];
//
//        int i = 0;
//
//        for (final AstType astTypeArgument : astTypeArguments) {
//            typeArguments[i++] = astTypeArgument.toTypeReference();
//        }
//
//        return typeReference.makeGenericType(typeArguments);
//    }
}
