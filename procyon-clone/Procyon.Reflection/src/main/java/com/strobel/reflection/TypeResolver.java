/*
 * TypeResolver.java
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

import java.io.Serializable;
import java.lang.reflect.*;

abstract class RawMember {
    private final Type _declaringType;

    protected RawMember(final Type context) {
        _declaringType = context;
    }

    public final Type getDeclaringType() {
        return _declaringType;
    }

    public abstract Member getRawMember();

    public String getName() {
        return getRawMember().getName();
    }

    public boolean isStatic() {
        return Modifier.isStatic(getModifiers());
    }

    public boolean isFinal() {
        return Modifier.isFinal(getModifiers());
    }

    public boolean isPrivate() {
        return Modifier.isPrivate(getModifiers());
    }

    public boolean isProtected() {
        return Modifier.isProtected(getModifiers());
    }

    public boolean isPublic() {
        return Modifier.isPublic(getModifiers());
    }

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();

    @Override
    public String toString() {
        return getName();
    }

    protected final int getModifiers() { return getRawMember().getModifiers(); }
}

final class RawField extends RawMember {
    private final Field _field;
    private final int _hashCode;

    public RawField(final Type context, final Field field) {
        super(context);
        _field = field;
        _hashCode = (_field == null ? 0 : _field.hashCode());
    }

    public Field getRawMember() {
        return _field;
    }

    public boolean isTransient() {
        return Modifier.isTransient(getModifiers());
    }

    public boolean isVolatile() {
        return Modifier.isVolatile(getModifiers());
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || o.getClass() != getClass()) {
            return false;
        }
        final RawField other = (RawField)o;
        return (other._field == _field);
    }

    @Override
    public int hashCode() {
        return _hashCode;
    }
}

final class RawConstructor extends RawMember {
    private final Constructor<?> _constructor;
    private final int _hashCode;

    public RawConstructor(final Type context, final Constructor<?> constructor) {
        super(context);
        _constructor = constructor;
        _hashCode = (_constructor == null ? 0 : _constructor.hashCode());
    }

    public MethodKey createKey() {
        final String name = "<init>";
        final Class<?>[] argTypes = _constructor.getParameterTypes();
        return new MethodKey(name, argTypes);
    }

    @Override
    public Constructor<?> getRawMember() {
        return _constructor;
    }

    @Override
    public int hashCode() {
        return _hashCode;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || o.getClass() != getClass()) {
            return false;
        }
        final RawConstructor other = (RawConstructor)o;
        return (other._constructor == _constructor);
    }
}

final class RawMethod extends RawMember {
    private final Method _method;
    private final int _hashCode;

    public RawMethod(final Type context, final Method method) {
        super(context);
        _method = method;
        _hashCode = (_method == null ? 0 : _method.hashCode());
    }

    public Method getRawMember() {
        return _method;
    }

    public boolean isAbstract() {
        return Modifier.isAbstract(getModifiers());
    }

    public boolean isStrict() {
        return Modifier.isStrict(getModifiers());
    }

    public boolean isNative() {
        return Modifier.isNative(getModifiers());
    }

    public boolean isSynchronized() {
        return Modifier.isSynchronized(getModifiers());
    }

    public MethodKey createKey() {
        final String name = _method.getName();
        final Class<?>[] argTypes = _method.getParameterTypes();
        return new MethodKey(name, argTypes);
    }

    @Override
    public int hashCode() {
        return _hashCode;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || o.getClass() != getClass()) {
            return false;
        }
        final RawMethod other = (RawMethod)o;
        return (other._method == _method);
    }
}

@SuppressWarnings("serial")
final class MethodKey implements Serializable {
    private static final Class<?>[] NO_CLASSES = new Class[0];
    private final String _name;
    private final Class<?>[] _argumentTypes;
    private final int _hashCode;

    public MethodKey(final String name) {
        _name = name;
        _argumentTypes = NO_CLASSES;
        _hashCode = name.hashCode();
    }

    public MethodKey(final String name, final Class<?>[] argTypes) {
        _name = name;
        _argumentTypes = argTypes;
        _hashCode = name.hashCode() + argTypes.length;
    }

    /**
     * Equality means name is the same and argument type erasures as well.
     */
    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || o.getClass() != getClass()) {
            return false;
        }
        final MethodKey other = (MethodKey)o;
        final Class<?>[] otherArgs = other._argumentTypes;
        final int argCount = _argumentTypes.length;
        if (otherArgs.length != argCount) {
            return false;
        }
        for (int i = 0; i < argCount; ++i) {
            if (otherArgs[i] != _argumentTypes[i]) {
                return false;
            }
        }
        return _name.equals(other._name);
    }

    @Override
    public int hashCode() { return _hashCode; }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(_name);
        sb.append('(');
        for (int i = 0, len = _argumentTypes.length; i < len; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(_argumentTypes[i].getName());
        }
        sb.append(')');
        return sb.toString();
    }
}

