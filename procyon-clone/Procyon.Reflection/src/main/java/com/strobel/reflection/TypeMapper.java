/*
 * TypeMapper.java
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

/**
 * @author Mike Strobel
 */
public abstract class TypeMapper<T> extends DefaultTypeVisitor<T, Type<?>> {
    @Override
    public Type<?> visitType(final Type<?> type, final T parameter) {
        return type;
    }

    public TypeList visit(final TypeList types, final T parameter) {
        Type<?>[] newTypes = null;

        for (int i = 0, n = types.size(); i < n; i++) {
            final Type oldType = types.get(i);
            final Type newType = visit(oldType, parameter);

            if (newType != oldType) {
                if (newTypes == null) {
                    newTypes = types.toArray();
                }
                newTypes[i] = newType;
            }
        }

        if (newTypes != null) {
            return Type.list(newTypes);
        }

        return types;
    }

    public TypeList visit(final TypeList types) {
        return visit(types, null);
    }
}
