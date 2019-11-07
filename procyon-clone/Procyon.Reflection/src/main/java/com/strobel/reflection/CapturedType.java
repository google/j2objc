/*
 * CapturedType.java
 *
 * Copyright (c) 2012 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.reflection;

import com.strobel.core.VerifyArgument;

import javax.lang.model.type.TypeKind;

/**
 * @author Mike Strobel
 */
final class CapturedType<T> extends Type<T> implements ICapturedType {
    final static String CapturedName = "<captured wildcard>";

    private final Type<?> _wildcard;
    private final Type<?> _declaringType;
    private final Type<?> _upperBound;
    private final Type<?> _lowerBound;

    CapturedType(final Type<?> declaringType, final Type<?> upperBound, final Type<?> lowerBound, final Type<?> wildcard) {
        if (!wildcard.isWildcardType()) {
            throw new IllegalArgumentException("Argument 'wildcard' must be a wildcard type.");
        }

        _wildcard = VerifyArgument.notNull(wildcard, "wildcard");
        _upperBound = upperBound != null ? upperBound : Types.Object;
        _lowerBound = lowerBound != null ? lowerBound : Type.Bottom;
        _declaringType = declaringType;
    }

    @Override
    public StringBuilder appendDescription(final StringBuilder sb) {
        return sb.append(CapturedName);
    }

    @Override
    public StringBuilder appendBriefDescription(final StringBuilder sb) {
        return sb.append(CapturedName);
    }

    @Override
    public StringBuilder appendSimpleDescription(final StringBuilder sb) {
        return sb.append(CapturedName);
    }

    @Override
    public Type<?> getExtendsBound() {
        return _upperBound;
    }

    @Override
    public Type<?> getSuperBound() {
        return _lowerBound;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<T> getErasedClass() {
        return (Class<T>) resolveErasedClass();
    }

    @Override
    public Type<?> getWildcard() {
        return _wildcard;
    }

    @Override
    public MemberType getMemberType() {
        return MemberType.TypeInfo;
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.WILDCARD;
    }

    @Override
    public <P, R> R accept(final TypeVisitor<P, R> visitor, final P parameter) {
        return visitor.visitCapturedType(this, parameter);
    }

    @Override
    public boolean isWildcardType() {
        return true;
    }

    @Override
    public Type getDeclaringType() {
        return _declaringType;
    }

    @Override
    public int getModifiers() {
        return _wildcard.getModifiers();
    }

    private Class<?> resolveErasedClass() {
        if (_upperBound != Types.Object) {
            return _upperBound.getErasedClass();
        }

        return Object.class;
    }
}
