/*
 * CompoundType.java
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
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;

/**
 * @author Mike Strobel
 */
final class CompoundType<T> extends Type<T> {
    private final TypeList _interfaces;
    private final Type<T> _baseType;

    CompoundType(final TypeList interfaces, final Type<T> baseType) {
        _baseType = VerifyArgument.notNull(baseType, "baseType");

        final Type[] sortedInterfaces = VerifyArgument.notNull(interfaces, "interfaces").toArray();

        Arrays.sort(
            sortedInterfaces,
            new Comparator<Type>() {
                @Override
                public int compare(final Type o1, final Type o2) {
                    return Integer.compare(Helper.rank(o1), Helper.rank(o2));
                }
            });

        _interfaces = interfaces;
    }

    @Override
    public <P, R> R accept(final TypeVisitor<P, R> visitor, final P parameter) {
        return visitor.visitTypeParameter(this, parameter);
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.TYPEVAR;
    }

    @Override
    public Type<? super T> getBaseType() {
        return _baseType;
    }

    @Override
    public TypeList getInterfaces() {
        return _interfaces;
    }

    @Override
    public Class<T> getErasedClass() {
        return _baseType.getErasedClass();
    }

    @Override
    public Type getDeclaringType() {
        return null;
    }

    @Override
    public int getModifiers() {
        return Modifier.PUBLIC | Modifier.ABSTRACT;
    }

    @Override
    public boolean isSynthetic() {
        return true;
    }

    @Override
    public boolean isCompoundType() {
        return true;
    }

    @Override
    public TypeList getExplicitInterfaces() {
        return _interfaces;
    }

    @Override
    public boolean isGenericParameter() {
        return false;
    }

    @Override
    public Type<?> getExtendsBound() {
        return _baseType;
    }

    @Override
    public StringBuilder appendBriefDescription(final StringBuilder sb) {
        final Type<T> baseType = _baseType;
        final TypeList interfaces = _interfaces;

        StringBuilder s = sb;

        if (baseType != Types.Object) {
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
        final Type<T> baseType = _baseType;
        final TypeList interfaces = _interfaces;

        StringBuilder s = sb;

        if (baseType != Types.Object) {
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
        final Type<T> baseType = _baseType;
        final TypeList interfaces = _interfaces;

        StringBuilder s = sb;

        if (baseType != Types.Object) {
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

        if (_baseType != null && _baseType != Types.Object)
            s = _baseType.appendSignature(s);

        if (_interfaces.isEmpty())
            return s;

        s.append(':');

        for (final Type interfaceType : _interfaces) {
            s = interfaceType.appendSignature(s);
        }

        return s;
    }

    @Override
    public StringBuilder appendErasedSignature(final StringBuilder sb) {
        return super.appendErasedSignature(sb);
    }
}
