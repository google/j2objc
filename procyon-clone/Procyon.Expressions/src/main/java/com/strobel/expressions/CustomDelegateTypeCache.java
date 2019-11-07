/*
 * CustomDelegateTypeCache.java
 *
 * Copyright (c) 2012 Mike Strobel
 *
 * This source code is based on the Dynamic Language Runtime from Microsoft,
 *   Copyright (c) Microsoft Corporation.
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.expressions;

import com.strobel.collections.Cache;
import com.strobel.core.ArrayUtilities;
import com.strobel.core.VerifyArgument;
import com.strobel.reflection.Type;
import com.strobel.reflection.TypeList;
import com.strobel.reflection.emit.GenericParameterBuilder;
import com.strobel.reflection.emit.TypeBuilder;

import javax.lang.model.type.TypeKind;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * @author strobelm
 */
final class CustomDelegateTypeCache {
    // <editor-fold defaultstate="collapsed" desc="CacheKey Class">

    private final static class CacheKey {
        private final TypeKind[] _types;

        CacheKey(final Type<?> returnType, final TypeList parameterTypes) {
            VerifyArgument.notNull(returnType, "returnType");
            VerifyArgument.notNull(parameterTypes, "parameterTypes");

            _types = new TypeKind[parameterTypes.size() + 1];
            _types[0] = coalesceKind(returnType);

            for (int i = 0, n = parameterTypes.size(); i < n; i++) {
                _types[i + 1] = coalesceKind(parameterTypes.get(i));
            }
        }

        private TypeKind coalesceKind(final Type<?> type) {
            final TypeKind kind = type.getKind();
            return kind.ordinal() <= TypeKind.VOID.ordinal() ? kind : TypeKind.DECLARED;
        }

        @Override
        public boolean equals(final Object o) {
            return this == o ||
                   o instanceof CacheKey && Arrays.equals(_types, ((CacheKey) o)._types);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(_types);
        }
    }

    // </editor-fold>
    
    private final static Cache<CacheKey, Type<?>> TypeCache = Cache.createSatelliteCache();
    
    static synchronized Type<?> get(final Type<?> returnType, final TypeList parameterTypes) {
        final CacheKey key = new CacheKey(returnType, parameterTypes);
        
        Type<?> delegateType = TypeCache.get(key);
        
        if (delegateType == null) {
            delegateType = TypeCache.cache(key, createDelegateType(returnType, parameterTypes));
        }

        if (!delegateType.isGenericTypeDefinition()) {
            return delegateType;
        }
        
        final TypeList genericParameters = delegateType.getGenericTypeParameters();
        final Type<?>[] typeArguments = new Type<?>[genericParameters.size()];

        int t = 0;

        if (!returnType.isPrimitive()) {
            typeArguments[t++] = returnType;
        }

        for (int i = 0, n = parameterTypes.size(); i < n; i++) {
            final Type<?> parameterType = parameterTypes.get(i);
            if (!parameterType.isPrimitive()) {
                typeArguments[t++] = parameterType;
            }
        }

        return delegateType.makeGenericType(typeArguments);
    }

    private static Type<?> createDelegateType(final Type<?> returnType, final TypeList parameterTypes) {
        final TypeBuilder<?> typeBuilder = new TypeBuilder<>(
            generateName(returnType, parameterTypes),
            Modifier.PUBLIC | Modifier.ABSTRACT | Modifier.INTERFACE,
            null,
            TypeList.empty()
        );

        String[] genericParameterNames;

        int t = 0;

        if (returnType.isPrimitive()) {
            genericParameterNames = null;
        }
        else {
            genericParameterNames = new String[] { "T" + t++ };
        }

        for (int i = 0, n = parameterTypes.size(); i < n; i++) {
            final Type<?> type = parameterTypes.get(i);
            
            if (!type.isPrimitive()) {
                genericParameterNames = ArrayUtilities.append(
                    genericParameterNames,
                    "T" + t++
                );
            }
        }

        final Type<?> genericReturnType;
        final TypeList genericParameterTypes;

        if (t == 0) {
            genericReturnType = returnType;
            genericParameterTypes = parameterTypes;
        }
        else {
            final GenericParameterBuilder<?>[] genericParameters = typeBuilder.defineGenericParameters(
                genericParameterNames
            );

            final Type<?>[] genericParameterTypeArray = new Type<?>[parameterTypes.size()];

            t = 0;

            if (returnType.isPrimitive()) {
                genericReturnType = returnType;
            }
            else {
                genericReturnType = genericParameters[t++];
            }

            for (int i = 0, n = parameterTypes.size(); i < n; i++) {
                final Type<?> parameterType = parameterTypes.get(i);

                if (parameterType.isPrimitive()) {
                    genericParameterTypeArray[i] = parameterType;
                }
                else {
                    genericParameterTypeArray[i] = genericParameters[t++];
                }
            }

            genericParameterTypes = Type.list(genericParameterTypeArray);
        }

        typeBuilder.defineMethod(
            "invoke",
            Modifier.PUBLIC | Modifier.ABSTRACT,
            genericReturnType,
            genericParameterTypes
        );

        return typeBuilder.createType();
    }

    private static String generateName(final Type<?> returnType, final TypeList parameterTypes) {
        final StringBuilder sb = new StringBuilder();
        
        sb.append(CustomDelegateTypeCache.class.getPackage().getName());
        sb.append(".GeneratedDelegate");
        sb.append(getCharacterCode(returnType));

        for (int i = 0, n = parameterTypes.size(); i < n; i++) {
            sb.append(getCharacterCode(parameterTypes.get(i)));
        }

        return sb.toString();
    }
    
    private static char getCharacterCode(final Type<?> type) {
        return type.isPrimitive() ? type.getSignature().charAt(0) : 'T';
    }
}
