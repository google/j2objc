/*
 * WildcardType.java
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

import javax.lang.model.type.TypeKind;

/**
 * @author Mike Strobel
 */
@SuppressWarnings("unchecked")
class WildcardType<T> extends Type<T> {
    private final Type<T> _extendsBound;
    private final Type _superBound;
    private Class<T> _erasedClass;

    WildcardType(final Type<T> extendsBound, final Type superBound) {
        _extendsBound = extendsBound != null ? extendsBound : (Type<T>)Types.Object;
        _superBound = superBound != null ? superBound : Type.Bottom;
    }

    @Override
    public StringBuilder appendSignature(final StringBuilder sb) {
        if (_superBound != Bottom) {
            return _superBound.appendSignature(sb.append('-'));
        }
        if (_extendsBound != Types.Object) {
            return _extendsBound.appendSignature(sb.append('+'));
        }
        return sb.append('*');
    }

    @Override
    public StringBuilder appendBriefDescription(final StringBuilder sb) {
        if (_superBound != Bottom) {
            sb.append("? super ");
            if (_superBound.isGenericParameter()) {
                return sb.append(_superBound.getFullName());
            }
            return _superBound.appendErasedDescription(sb);
        }

        if (_extendsBound == Types.Object) {
            return sb.append("?");
        }

        sb.append("? extends ");

        if (_extendsBound.isGenericParameter()) {
            return sb.append(_extendsBound.getFullName());
        }

        return _extendsBound.appendErasedDescription(sb);
    }

    @Override
    public StringBuilder appendSimpleDescription(final StringBuilder sb) {
        if (_superBound != Bottom) {
            sb.append("? super ");
            if (_superBound.isGenericParameter()) {
                return sb.append(_superBound.getName());
            }
            return _superBound.appendSimpleDescription(sb);
        }

        if (_extendsBound == Types.Object) {
            return sb.append("?");
        }

        sb.append("? extends ");

        if (_extendsBound.isGenericParameter()) {
            return sb.append(_extendsBound.getName());
        }

        return _extendsBound.appendSimpleDescription(sb);
    }

    @Override
    public StringBuilder appendErasedDescription(final StringBuilder sb) {
        return appendBriefDescription(sb);
    }

    @Override
    public StringBuilder appendDescription(final StringBuilder sb) {
        return appendBriefDescription(sb);
    }

    private Class<T> resolveErasedClass() {
        return _extendsBound.getErasedClass();
    }

    @Override
    public Type<?> getExtendsBound() {
        return _extendsBound;
    }

    @Override
    public Type<?> getSuperBound() {
        return _superBound;
    }

    @Override
    public boolean isWildcardType() {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object accept(final TypeVisitor visitor, final Object parameter) {
        return visitor.visitWildcardType(this, parameter);
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.WILDCARD;
    }

    @Override
    public Class<T> getErasedClass() {
        if (_erasedClass == null) {
            synchronized (CACHE_LOCK) {
                if (_erasedClass == null) {
                    _erasedClass = resolveErasedClass();
                }
            }
        }
        return _erasedClass;
    }

    @Override
    public MemberType getMemberType() {
        return MemberType.TypeInfo;
    }

    @Override
    public Type getDeclaringType() {
        return null;
    }

    @Override
    public int getModifiers() {
        return Flags.PUBLIC;
    }

    @Override
    public boolean isEquivalentTo(final Type<?> other) {
        if (other == this) {
            return true;
        }

        if (other instanceof WildcardType<?>) {
            final WildcardType that = (WildcardType)other;

            return _extendsBound.isEquivalentTo(that._extendsBound) &&
                   _superBound.isEquivalentTo(that._superBound);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = _extendsBound.hashCode();
        result = 31 * result + _superBound.hashCode();
        return result;
    }
}
