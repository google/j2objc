/*
 * ArrayType.java
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

package com.strobel.assembler.metadata;

import com.strobel.core.VerifyArgument;

/**
 * @author Mike Strobel
 */
public final class ArrayType extends TypeReference {
    private final TypeReference _elementType;

    private String _internalName;
    private String _fullName;
    private String _simpleName;

    ArrayType(final TypeReference elementType) {
        _elementType = VerifyArgument.notNull(elementType, "elementType");

        setName(elementType.getName() + "[]");
    }

    @Override
    public boolean containsGenericParameters() {
        return _elementType.containsGenericParameters();
    }

    @Override
    public String getPackageName() {
        return _elementType.getPackageName();
    }

    public String getSimpleName() {
        if (_simpleName == null) {
            _simpleName = _elementType.getSimpleName() + "[]";
        }
        return _simpleName;
    }

    public String getFullName() {
        if (_fullName == null) {
            _fullName = _elementType.getFullName() + "[]";
        }
        return _fullName;
    }

    public String getInternalName() {
        if (_internalName == null) {
            _internalName = "[" + _elementType.getInternalName();
        }
        return _internalName;
    }

    @Override
    public final boolean isArray() {
        return true;
    }

    @Override
    public final TypeReference getElementType() {
        return _elementType;
    }

    @Override
    public final <R, P> R accept(final TypeMetadataVisitor<P, R> visitor, final P parameter) {
        return visitor.visitArrayType(this, parameter);
    }

    @Override
    public final TypeReference getUnderlyingType() {
        return _elementType.getUnderlyingType();
    }

    @Override
    public final StringBuilder appendSignature(final StringBuilder sb) {
        sb.append('[');
        return _elementType.appendSignature(sb);
    }

    @Override
    public final StringBuilder appendErasedSignature(final StringBuilder sb) {
        return _elementType.appendErasedSignature(sb.append('['));
    }

    public final StringBuilder appendBriefDescription(final StringBuilder sb) {
        return _elementType.appendBriefDescription(sb).append("[]");
    }

    public final StringBuilder appendSimpleDescription(final StringBuilder sb) {
        return _elementType.appendSimpleDescription(sb).append("[]");
    }

    public final StringBuilder appendDescription(final StringBuilder sb) {
        return appendBriefDescription(sb);
    }

    public static ArrayType create(final TypeReference elementType) {
        return new ArrayType(elementType);
    }

    @Override
    public final TypeDefinition resolve() {
        final TypeDefinition resolvedElementType = _elementType.resolve();

        if (resolvedElementType != null) {
            return resolvedElementType;
        }

        return super.resolve();
    }
}
