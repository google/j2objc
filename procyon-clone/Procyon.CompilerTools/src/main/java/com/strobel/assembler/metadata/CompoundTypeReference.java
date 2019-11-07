/*
 * CompoundTypeReference.java
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

import java.util.List;

/**
 * @author Mike Strobel
 */
public final class CompoundTypeReference extends TypeReference {
    private final TypeReference _baseType;
    private final List<TypeReference> _interfaces;

    public CompoundTypeReference(final TypeReference baseType, final List<TypeReference> interfaces) {
        _baseType = baseType;
        _interfaces = interfaces;
    }

    public final TypeReference getBaseType() {
        return _baseType;
    }

    public final List<TypeReference> getInterfaces() {
        return _interfaces;
    }

    @Override
    public TypeReference getDeclaringType() {
        return null;
    }

    @Override
    public String getSimpleName() {
        if (_baseType != null) {
            return _baseType.getSimpleName();
        }
        return _interfaces.get(0).getSimpleName();
    }

    @Override
    public boolean containsGenericParameters() {
        final TypeReference baseType = getBaseType();

        if (baseType != null && baseType.containsGenericParameters()) {
            return true;
        }

        for (final TypeReference t : _interfaces) {
            if (t.containsGenericParameters()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getName() {
        if (_baseType != null) {
            return _baseType.getName();
        }
        return _interfaces.get(0).getName();
    }

    @Override
    public String getFullName() {
        if (_baseType != null) {
            return _baseType.getFullName();
        }
        return _interfaces.get(0).getFullName();
    }

    @Override
    public String getInternalName() {
        if (_baseType != null) {
            return _baseType.getInternalName();
        }
        return _interfaces.get(0).getInternalName();
    }

    @Override
    public final <R, P> R accept(final TypeMetadataVisitor<P, R> visitor, final P parameter) {
        return visitor.visitCompoundType(this, parameter);
    }

    @Override
    public StringBuilder appendBriefDescription(final StringBuilder sb) {
        final TypeReference baseType = _baseType;
        final List<TypeReference> interfaces = _interfaces;

        StringBuilder s = sb;

        if (baseType != null) {
            s = baseType.appendBriefDescription(s);
            if (!interfaces.isEmpty()) {
                s.append(" & ");
            }
        }

        for (int i = 0, n = interfaces.size(); i < n; i++) {
            if (i != 0) {
                s.append(" & ");
            }
            s = interfaces.get(i).appendBriefDescription(s);
        }

        return s;
    }

    @Override
    public StringBuilder appendSimpleDescription(final StringBuilder sb) {
        final TypeReference baseType = _baseType;
        final List<TypeReference> interfaces = _interfaces;

        StringBuilder s = sb;

        if (baseType != null) {
            s = baseType.appendSimpleDescription(s);
            if (!interfaces.isEmpty()) {
                s.append(" & ");
            }
        }

        for (int i = 0, n = interfaces.size(); i < n; i++) {
            if (i != 0) {
                s.append(" & ");
            }
            s = interfaces.get(i).appendSimpleDescription(s);
        }

        return s;
    }

    @Override
    public StringBuilder appendErasedDescription(final StringBuilder sb) {
        final TypeReference baseType = _baseType;
        final List<TypeReference> interfaces = _interfaces;

        StringBuilder s = sb;

        if (baseType != null) {
            s = baseType.appendErasedDescription(s);
            if (!interfaces.isEmpty()) {
                s.append(" & ");
            }
        }

        for (int i = 0, n = interfaces.size(); i < n; i++) {
            if (i != 0) {
                s.append(" & ");
            }
            s = interfaces.get(i).appendErasedDescription(s);
        }

        return s;
    }

    @Override
    public StringBuilder appendDescription(final StringBuilder sb) {
        return appendBriefDescription(sb);
    }

    @Override
    public StringBuilder appendSignature(final StringBuilder sb) {
        StringBuilder s = sb;

        if (_baseType != null) {
            s = _baseType.appendSignature(s);
        }

        if (_interfaces.isEmpty()) {
            return s;
        }

        for (final TypeReference interfaceType : _interfaces) {
            s.append(':');
            s = interfaceType.appendSignature(s);
        }

        return s;
    }

    @Override
    public StringBuilder appendErasedSignature(final StringBuilder sb) {
        if (_baseType != null) {
            return _baseType.appendErasedSignature(sb);
        }

        if (!_interfaces.isEmpty()) {
            return _interfaces.get(0).appendErasedSignature(sb);
        }

        return BuiltinTypes.Object.appendErasedSignature(sb);
    }
}
