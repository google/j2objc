
/*
 * GenericParameter.java
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

import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;

import java.util.List;

public final class GenericParameter extends TypeDefinition {
    private int _position;
    private GenericParameterType _type = GenericParameterType.Type;
    private IGenericParameterProvider _owner;
    private TypeReference _extendsBound;

    public GenericParameter(final String name) {
        _extendsBound = BuiltinTypes.Object;
        setName(name != null ? name : StringUtilities.EMPTY);
    }

    public GenericParameter(final String name, final TypeReference extendsBound) {
        _extendsBound = VerifyArgument.notNull(extendsBound, "extendsBound");
        setName(name != null ? name : StringUtilities.EMPTY);
    }

    protected final void setPosition(final int position) {
        _position = position;
    }

    protected final void setOwner(final IGenericParameterProvider owner) {
        _owner = owner;

        _type = owner instanceof MethodReference ? GenericParameterType.Method
                                                 : GenericParameterType.Type;
    }

    protected final void setExtendsBound(final TypeReference extendsBound) {
        _extendsBound = extendsBound;
    }

    @Override
    public String getName() {
        final String name = super.getName();

        if (!StringUtilities.isNullOrEmpty(name)) {
            return name;
        }

        return "T" + _position;
    }

    @Override
    public String getFullName() {
        return getName();
    }

    @Override
    public String getInternalName() {
        return getName();
    }

    @Override
    public TypeReference getUnderlyingType() {
        final TypeReference extendsBound = getExtendsBound();
        return extendsBound != null ? extendsBound : BuiltinTypes.Object;
    }

    @Override
    public final <R, P> R accept(final TypeMetadataVisitor<P, R> visitor, final P parameter) {
        return visitor.visitGenericParameter(this, parameter);
    }

    @Override
    public boolean isUnbounded() {
        return !hasExtendsBound();
    }

    @Override
    public boolean isGenericParameter() {
        return true;
    }

    @Override
    public boolean containsGenericParameters() {
        return true;
    }

    @Override
    public TypeReference getDeclaringType() {
        final IGenericParameterProvider owner = _owner;

        if (owner instanceof TypeReference) {
            return (TypeReference) owner;
        }

        return null;
    }

    public int getPosition() {
        return _position;
    }

    public GenericParameterType getType() {
        return _type;
    }

    public IGenericParameterProvider getOwner() {
        return _owner;
    }

    @Override
    public boolean hasExtendsBound() {
        return _extendsBound != null &&
               !MetadataResolver.areEquivalent(_extendsBound, BuiltinTypes.Object);
    }

    @Override
    public TypeReference getExtendsBound() {
        return _extendsBound;
    }

    @Override
    public boolean hasAnnotations() {
        return !getAnnotations().isEmpty();
    }

    @Override
    public TypeDefinition resolve() {
        if (_owner instanceof TypeReference &&
            !(_owner instanceof TypeDefinition)) {
            final TypeDefinition resolvedOwner = ((TypeReference) _owner).resolve();

            if (resolvedOwner != null) {
                final List<GenericParameter> genericParameters = resolvedOwner.getGenericParameters();

                if (_position >= 0 &&
                    _position < genericParameters.size()) {

                    return genericParameters.get(_position);
                }
            }
        }
        return this;
    }

    // <editor-fold defaultstate="collapsed" desc="Name and Signature Formatting">

    @Override
    protected StringBuilder appendDescription(final StringBuilder sb) {
        sb.append(getFullName());

        final TypeReference upperBound = getExtendsBound();

        if (upperBound != null && !upperBound.equals(BuiltinTypes.Object)) {
            sb.append(" extends ");
            if (upperBound.isGenericParameter() || upperBound.equals(getDeclaringType())) {
                return sb.append(upperBound.getFullName());
            }
            return upperBound.appendErasedDescription(sb);
        }

        return sb;
    }

    @Override
    protected StringBuilder appendBriefDescription(final StringBuilder sb) {
        sb.append(getFullName());

        final TypeReference upperBound = getExtendsBound();

        if (upperBound != null && !upperBound.equals(BuiltinTypes.Object)) {
            sb.append(" extends ");
            if (upperBound.isGenericParameter() || upperBound.equals(getDeclaringType())) {
                return sb.append(upperBound.getName());
            }
            return upperBound.appendErasedDescription(sb);
        }

        return sb;
    }

    @Override
    protected StringBuilder appendErasedDescription(final StringBuilder sb) {
        return getExtendsBound().appendErasedDescription(sb);
    }

    @Override
    protected StringBuilder appendSignature(final StringBuilder sb) {
        return sb.append('T')
                 .append(getName())
                 .append(';');
    }

    @Override
    protected StringBuilder appendErasedSignature(final StringBuilder sb) {
        return getExtendsBound().appendErasedSignature(sb);
    }

    @Override
    protected StringBuilder appendSimpleDescription(final StringBuilder sb) {
        sb.append(getFullName());

        final TypeReference upperBound = getExtendsBound();

        if (upperBound != null && !upperBound.equals(BuiltinTypes.Object)) {
            sb.append(" extends ");
            if (upperBound.isGenericParameter() || upperBound.equals(getOwner())) {
                return sb.append(upperBound.getSimpleName());
            }
            return upperBound.appendSimpleDescription(sb);
        }

        return sb;
    }

    // </editor-fold>
}
