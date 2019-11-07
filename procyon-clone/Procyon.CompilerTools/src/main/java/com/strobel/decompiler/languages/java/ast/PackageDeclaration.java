/*
 * PackageDeclaration.java
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

import com.strobel.core.ArrayUtilities;
import com.strobel.core.StringUtilities;
import com.strobel.decompiler.patterns.INode;
import com.strobel.decompiler.patterns.Match;

public class PackageDeclaration extends AstNode {
    public PackageDeclaration() {
    }

    public PackageDeclaration(final String name) {
        setName(name);
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.UNKNOWN;
    }

    public final JavaTokenNode getPackageToken() {
        return getChildByRole(Roles.PACKAGE_KEYWORD);
    }

    public final JavaTokenNode getSemicolonToken() {
        return getChildByRole(Roles.SEMICOLON);
    }

    public final AstNodeCollection<Identifier> getIdentifiers() {
        return getChildrenByRole(Roles.IDENTIFIER);
    }

    public final String getName() {
        final StringBuilder sb = new StringBuilder();

        for (final Identifier identifier : getIdentifiers()) {
            if (sb.length() > 0) {
                sb.append('.');
            }

            sb.append(identifier.getName());
        }

        return sb.toString();
    }

    public final void setName(final String name) {
        if (name == null) {
            getChildrenByRole(Roles.IDENTIFIER).clear();
            return;
        }

        final String[] parts = name.split("\\.");
        final Identifier[] identifiers = new Identifier[parts.length];

        for (int i = 0; i < identifiers.length; i++) {
            identifiers[i] = Identifier.create(parts[i]);
        }

        getChildrenByRole(Roles.IDENTIFIER).replaceWith(ArrayUtilities.asUnmodifiableList(identifiers));
    }

    public static String BuildQualifiedName(final String name1, final String name2) {
        if (StringUtilities.isNullOrEmpty(name1)) {
            return name2;
        }

        if (StringUtilities.isNullOrEmpty(name2)) {
            return name1;
        }

        return name1 + "." + name2;
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitPackageDeclaration(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof PackageDeclaration &&
               !other.isNull() &&
               matchString(getName(), ((PackageDeclaration) other).getName());
    }

    // <editor-fold defaultstate="collapsed" desc="Null PackageDeclaration">

    public final static PackageDeclaration NULL = new NullPackageDeclaration();

    private static final class NullPackageDeclaration extends PackageDeclaration {
        @Override
        public final boolean isNull() {
            return true;
        }

        @Override
        public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
            return null;
        }

        @Override
        public boolean matches(final INode other, final Match match) {
            return other == null || other.isNull();
        }
    }

    // </editor-fold>
}
