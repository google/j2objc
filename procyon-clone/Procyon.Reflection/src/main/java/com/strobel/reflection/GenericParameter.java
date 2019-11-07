/*
 * GenericParameter.java
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

import com.strobel.annotations.NotNull;
import com.strobel.core.Comparer;
import com.strobel.core.HashUtilities;
import com.strobel.core.VerifyArgument;
import com.strobel.reflection.emit.GenericParameterBuilder;

import javax.lang.model.type.TypeKind;
import java.lang.annotation.Annotation;
import java.lang.reflect.TypeVariable;

/**
 * @author Mike Strobel
 */
class GenericParameter<T> extends Type<T> {
    private final String _name;
    private final int _position;
    private Type _upperBound;
    private Type _lowerBound;
    private MethodInfo _declaringMethod;
    private Type _declaringType;
    private Class<T> _erasedClass;
    private TypeVariable<?> _typeVariable;

    GenericParameter(final String name, final TypeVariable<?> typeVariable, final int position) {
        _typeVariable = typeVariable;
        _name = VerifyArgument.notNull(name, "name");
        _declaringType = null;
        _upperBound = Types.Object;
        _lowerBound = Bottom;
        _position = position;
    }

    GenericParameter(final String name, final Type declaringType, final Type upperBound, final int position) {
        _name = VerifyArgument.notNull(name, "name");
        _declaringType = VerifyArgument.notNull(declaringType, "declaringType");
        _upperBound = upperBound != null ? upperBound : Types.Object;
        _lowerBound = Bottom;
        _position = position;
    }

    GenericParameter(final String name, final MethodInfo declaringMethod, final Type upperBound, final int position) {
        _name = VerifyArgument.notNull(name, "name");
        _declaringType = null;
        _declaringMethod = VerifyArgument.notNull(declaringMethod, "declaringMethod");
        _upperBound = upperBound != null ? upperBound : Types.Object;
        _lowerBound = Bottom;
        _position = position;
    }

    protected GenericParameter(final String name, final Type declaringType, final Type upperBound, final Type lowerBound, final int position) {
        _name = VerifyArgument.notNull(name, "name");
        _declaringType = VerifyArgument.notNull(declaringType, "declaringType");
        _upperBound = upperBound != null ? upperBound : Types.Object;
        _lowerBound = lowerBound != null ? lowerBound : Type.Bottom;
        _position = position;
    }

    protected GenericParameter(final String name, final MethodInfo declaringMethod, final Type upperBound, final Type lowerBound, final int position) {
        _name = VerifyArgument.notNull(name, "name");
        _declaringType = null;
        _declaringMethod = VerifyArgument.notNull(declaringMethod, "declaringMethod");
        _upperBound = upperBound != null ? upperBound : Types.Object;
        _lowerBound = lowerBound != null ? lowerBound : Type.Bottom;
        _position = position;
    }

    final void setUpperBound(final Type upperBound) {
        _upperBound = upperBound;
    }

    final void setLowerBound(final Type lowerBound) {
        _lowerBound = lowerBound;
    }

    @Override
    public TypeList getExplicitInterfaces() {
        return TypeList.empty();
    }

    private TypeVariable<?> resolveTypeVariable() {
        final TypeVariable[] parameters;

        if (_declaringMethod != null) {
            parameters = _declaringMethod.getRawMethod().getTypeParameters();
        }
        else {
            parameters = _declaringType.getErasedClass().getTypeParameters();
        }

        for (final TypeVariable typeVariable : parameters) {
            if (_name.equals(typeVariable.getName())) {
                return typeVariable;
            }
        }

        throw Error.couldNotResolveType(_name);
    }

    private Class<?> resolveErasedClass() {
        if (_upperBound != Types.Object) {
            return _upperBound.getErasedClass();
        }

        return Object.class;
    }

    public TypeVariable<?> getRawTypeVariable() {
        if (_typeVariable == null) {
            synchronized (CACHE_LOCK) {
                if (_typeVariable == null) {
                    _typeVariable = resolveTypeVariable();
                }
            }
        }
        return _typeVariable;
    }

    @Override
    public MemberType getMemberType() {
        return MemberType.TypeInfo;
    }

    @Override
    public String getFullName() {
        return _name;
    }

    @Override
    public StringBuilder appendBriefDescription(final StringBuilder sb) {
        sb.append(getFullName());

        final Type<?> upperBound = getExtendsBound();

        if (upperBound != null && upperBound != Types.Object) {
            sb.append(" extends ");
            if (upperBound.isGenericParameter() || upperBound == getDeclaringType()) {
                return sb.append(upperBound.getFullName());
            }
            return upperBound.appendErasedDescription(sb);
        }

        return sb;
    }

    @Override
    public StringBuilder appendSimpleDescription(final StringBuilder sb) {
        return sb.append(getName());
    }

    @Override
    public StringBuilder appendErasedDescription(final StringBuilder sb) {
        return getExtendsBound().appendErasedDescription(sb);
    }

    @Override
    public StringBuilder appendErasedSignature(final StringBuilder sb) {
        return getExtendsBound().appendErasedSignature(sb);
    }

    @Override
    public StringBuilder appendDescription(final StringBuilder sb) {
        return appendBriefDescription(sb);
    }

    @Override
    protected final StringBuilder _appendClassName(final StringBuilder sb, final boolean fullName, final boolean dottedName) {
        return sb.append(_name);
    }

    @Override
    public Type getDeclaringType() {
        return _declaringType;
    }

    @Override
    public MethodInfo getDeclaringMethod() {
        return _declaringMethod;
    }

    public void setDeclaringMethod(final MethodInfo declaringMethod) {
        _declaringMethod = declaringMethod;
    }

    public void setDeclaringType(final Type declaringType) {
        _declaringType = declaringType;
    }

    @Override
    public boolean isGenericParameter() {
        return true;
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.TYPEVAR;
    }

    @Override
    public Type<?> getSuperBound() {
        return _lowerBound;
    }

    @Override
    public Type<?> getExtendsBound() {
        return _upperBound;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<T> getErasedClass() {
        if (_erasedClass == null) {
            synchronized (CACHE_LOCK) {
                if (_erasedClass == null) {
                    _erasedClass = (Class<T>) resolveErasedClass();
                }
            }
        }
        return _erasedClass;
    }

    @Override
    public int getGenericParameterPosition() {
        return _position;
    }

    @Override
    public int getModifiers() {
        return 0;
    }

    @Override
    public boolean isAnnotationPresent(final Class<? extends Annotation> annotationClass) {
        return false;
    }

    @Override
    public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
        return null;
    }

    @NotNull
    @Override
    public Annotation[] getAnnotations() {
        return new Annotation[0];
    }

    @NotNull
    @Override
    public Annotation[] getDeclaredAnnotations() {
        return new Annotation[0];
    }

    @Override
    public String toString() {
        return getFullName();
    }

    @Override
    public <P, R> R accept(final TypeVisitor<P, R> visitor, final P parameter) {
        return visitor.visitTypeParameter(this, parameter);
    }

    @Override
    public int hashCode() {
        return HashUtilities.hashCode(_typeVariable);
    }

    @Override
    public boolean isEquivalentTo(final Type<?> member) {
        if (member == this) {
            return true;
        }

        if (member == null) {
            return false;
        }

        if (member instanceof GenericParameter<?>) {
            if (member instanceof CapturedType<?>) {
                return false;
            }
            final GenericParameter<?> other = (GenericParameter<?>) member;
            return other._position == _position &&
                   Comparer.equals(other.getRawTypeVariable(), _typeVariable);
        }

        if (member instanceof GenericParameterBuilder<?>) {
            return member.isEquivalentTo(this);
        }

        return false;
    }
}
