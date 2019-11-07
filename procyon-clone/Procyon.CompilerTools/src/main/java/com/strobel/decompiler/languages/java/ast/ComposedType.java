/*
 * ComposedType.java
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

import com.strobel.assembler.metadata.TypeReference;
import com.strobel.decompiler.patterns.INode;
import com.strobel.decompiler.patterns.Match;
import com.strobel.decompiler.patterns.Role;

import static com.strobel.core.CollectionUtilities.firstOrDefault;

public class ComposedType extends AstType {
    public final static Role<ArraySpecifier> ARRAY_SPECIFIER_ROLE = new Role<>("ArraySpecifier", ArraySpecifier.class);

    public ComposedType() {
    }

    public ComposedType(final AstType baseType) {
        setBaseType(baseType);
    }

    public final AstType getBaseType() {
        return getChildByRole(Roles.BASE_TYPE);
    }

    public final void setBaseType(final AstType value) {
        setChildByRole(Roles.BASE_TYPE, value);
    }

    public final AstNodeCollection<ArraySpecifier> getArraySpecifiers() {
        return getChildrenByRole(ARRAY_SPECIFIER_ROLE);
    }

    @Override
    public TypeReference toTypeReference() {
        TypeReference typeReference = getBaseType().toTypeReference();

        for (ArraySpecifier specifier = getArraySpecifiers().firstOrNullObject();
             specifier != null;
             specifier = specifier.getNextSibling(ARRAY_SPECIFIER_ROLE)) {

            typeReference = typeReference.makeArrayType();
        }

        return typeReference;
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitComposedType(this, data);
    }

    @Override
    public AstType makeArrayType() {
        insertChildBefore(firstOrDefault(getArraySpecifiers()), new ArraySpecifier(), ARRAY_SPECIFIER_ROLE);

        final TypeReference typeReference = getUserData(Keys.TYPE_REFERENCE);

        if (typeReference != null) {
            putUserData(Keys.TYPE_REFERENCE, typeReference.makeArrayType());
        }

        return this;
    }

    @Override
    public String toString() {
        final AstNodeCollection<ArraySpecifier> arraySpecifiers = getArraySpecifiers();
        final StringBuilder sb = new StringBuilder();

        sb.append(getBaseType());

        for (final ArraySpecifier arraySpecifier : arraySpecifiers) {
            sb.append(arraySpecifier);
        }

        return sb.toString();
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof ComposedType &&
               getArraySpecifiers().matches(((ComposedType) other).getArraySpecifiers(), match);
    }
}

