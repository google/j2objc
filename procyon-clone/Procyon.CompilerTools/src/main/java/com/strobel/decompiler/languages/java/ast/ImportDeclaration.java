/*
 * ImportDeclaration.java
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

import com.strobel.assembler.metadata.PackageReference;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.patterns.INode;
import com.strobel.decompiler.patterns.Match;
import com.strobel.decompiler.patterns.Role;

public class ImportDeclaration extends AstNode {
    public final static TokenRole IMPORT_KEYWORD_RULE = new TokenRole("import", TokenRole.FLAG_KEYWORD);

    public ImportDeclaration() {
    }

    public ImportDeclaration(final String packageOrTypeName) {
        setImport(packageOrTypeName);
    }

    public ImportDeclaration(final PackageReference pkg) {
        setImport(VerifyArgument.notNull(pkg, "pkg").getFullName() + ".*");
        putUserData(Keys.PACKAGE_REFERENCE, pkg);
    }

    public ImportDeclaration(final TypeReference type) {
        setImport(VerifyArgument.notNull(type, "pkg").getFullName() + ".*");
        putUserData(Keys.TYPE_REFERENCE, type);
    }

    public ImportDeclaration(final AstType type) {
        final TypeReference typeReference = VerifyArgument.notNull(type, "type").toTypeReference();

        if (typeReference != null) {
            setImport(typeReference.getFullName());
            putUserData(Keys.TYPE_REFERENCE, typeReference);
        }
        else {
            setImport(type.toString());
        }
    }

    public final String getImport() {
        return getChildByRole(Roles.IDENTIFIER).getName();
    }

    public final void setImport(final String value) {
        setChildByRole(Roles.IDENTIFIER, Identifier.create(value));
    }

    public final Identifier getImportIdentifier() {
        return getChildByRole(Roles.IDENTIFIER);
    }

    public final void setImportIdentifier(final Identifier value) {
        setChildByRole(Roles.IDENTIFIER, value);
    }

    public final JavaTokenNode getImportToken() {
        return getChildByRole(IMPORT_KEYWORD_RULE);
    }

    public final JavaTokenNode getSemicolonToken() {
        return getChildByRole(Roles.SEMICOLON);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Role<? extends ImportDeclaration> getRole() {
        return (Role<? extends ImportDeclaration>) super.getRole();
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.UNKNOWN;
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitImportDeclaration(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof ImportDeclaration &&
               getImportIdentifier().matches(((ImportDeclaration) other).getImportIdentifier(), match);
    }

    // <editor-fold defaultstate="collapsed" desc="Null ImportDeclaration">

    public final static ImportDeclaration NULL = new NullImportDeclaration();

    private static final class NullImportDeclaration extends ImportDeclaration {
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

