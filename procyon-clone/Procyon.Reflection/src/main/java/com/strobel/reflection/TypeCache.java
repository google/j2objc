/*
 * TypeCache.java
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

import com.strobel.core.Comparer;
import com.strobel.core.HashUtilities;
import com.strobel.util.TypeUtils;

import java.util.LinkedHashMap;

/**
 * @author strobelm
 */
@SuppressWarnings("unchecked")
final class TypeCache {
    private final LinkedHashMap<Key, Type<?>> _map = new LinkedHashMap<>();
    private final LinkedHashMap<String, Type<?>> _definitionMap = new LinkedHashMap<>();
    private final LinkedHashMap<Type<?>, Type<?>> _arrayMap = new LinkedHashMap<>();

    public Key key(final Type<?> type) {
        return key(type, TypeList.empty());
    }

    public Key key(final Type<?> type, final TypeList typeArguments) {
        return new Key(type.isGenericType() ? type.getGenericTypeDefinition() : type, typeArguments);
    }

    public Type find(final Key key) {
        return _map.get(key);
    }

    public <T> Type<T[]> getArrayType(final Type<T> elementType) {
        Type<T[]> arrayType = (Type<T[]>) _arrayMap.get(elementType);

        if (arrayType != null) {
            return arrayType;
        }

        arrayType = elementType.createArrayType();
        add(arrayType);

        return arrayType;
    }

    public <T> Type<T> getGenericType(final Type<T> type, final TypeList typeArguments) {
        final Key key = key(
            type.isGenericType() ? type.getGenericTypeDefinition() : type,
            typeArguments
        );

        Type genericType = _map.get(key);

        if (genericType == null) {
            genericType = new GenericType(
                type.getGenericTypeDefinition(),
                typeArguments
            );

            final Type existing = _map.put(key, genericType);

            if (existing != null) {
                return existing;
            }
        }

        return genericType;
    }

    public <T> Type<T> find(final Class<T> clazz) {
        return (Type<T>) _definitionMap.get(TypeUtils.getInternalName(clazz));
    }

    public int size() {
        return _map.size();
    }

    public void put(final Key key, final Type type) {
        final String descriptor = key.descriptor;

        if (!_definitionMap.containsKey(descriptor)) {
            if (type.isGenericType() && !type.isGenericTypeDefinition()) {
                _definitionMap.put(descriptor, type.getGenericTypeDefinition());
            }
            else {
                _definitionMap.put(descriptor, type);
            }
        }

        if (type.isPrimitive() && !_definitionMap.containsKey(type.getName())) {
            _definitionMap.put(type.getName(), type);
        }

        _map.put(key, type);

        if (type.isArray()) {
            final Type elementType = type.getElementType();
            if (!_arrayMap.containsKey(elementType)) {
                _arrayMap.put(elementType, type);
            }
        }
    }

    public void add(final Type type) {
        final TypeList typeArguments;

        if (type.isGenericType()) {
            typeArguments = type.getTypeBindings().getBoundTypes();
        }
        else {
            typeArguments = TypeList.empty();
        }

        put(key(type, typeArguments), type);
    }

    final static class Key {
        private final String descriptor;
        private final TypeList typeArguments;
        private final int hashCode;

        public Key(final Type<?> simpleType) {
            this(simpleType, null);
        }

        public Key(final Type<?> type, final TypeList typeArguments) {
            this.descriptor = type.getInternalName();
            this.typeArguments = typeArguments;

            int h = this.descriptor.hashCode();

            if (typeArguments != null && !typeArguments.isEmpty()) {
                for (final Type<?> argument : typeArguments) {
                    h = HashUtilities.combineHashCodes(h, argument.hashCode());
                }
            }

            this.hashCode = h;
        }

        @Override
        public final int hashCode() {
            return this.hashCode;
        }

        @Override
        public final boolean equals(final Object o) {
            if (o == this) {
                return true;
            }

            if (o == null || o.getClass() != getClass()) {
                return false;
            }

            final Key other = (Key) o;

            if (!this.descriptor.equals(other.descriptor)) {
                return false;
            }

            final TypeList typeArguments = this.typeArguments;
            final TypeList otherArguments = other.typeArguments;

            if (typeArguments == null || typeArguments.isEmpty()) {
                return otherArguments == null || otherArguments.isEmpty();
            }

            if (otherArguments == null || otherArguments.size() != typeArguments.size()) {
                return false;
            }

            for (int i = 0, n = typeArguments.size(); i < n; ++i) {
                final Type argument = typeArguments.get(i);
                final Type otherArgument = otherArguments.get(i);

                if (!Comparer.equals(argument, otherArgument)) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public final String toString() {
            return "Key{" +
                   "descriptor=" + descriptor +
                   ", typeArguments=" + typeArguments +
                   ", hashCode=" + hashCode +
                   '}';
        }
    }
}
