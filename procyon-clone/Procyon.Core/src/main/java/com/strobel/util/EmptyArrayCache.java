/*
 * EmptyArrayCache.java
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

package com.strobel.util;

import com.strobel.collections.Cache;
import com.strobel.core.VerifyArgument;

import java.lang.reflect.Array;

/**
 * @author Mike Strobel
 */
public final class EmptyArrayCache {
    public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];
    public static final char[] EMPTY_CHAR_ARRAY = new char[0];
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    public static final short[] EMPTY_SHORT_ARRAY = new short[0];
    public static final int[] EMPTY_INT_ARRAY = new int[0];
    public static final long[] EMPTY_LONG_ARRAY = new long[0];
    public static final float[] EMPTY_FLOAT_ARRAY = new float[0];
    public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

    private final static Cache<Class<?>, Object> GLOBAL_CACHE = Cache.createTopLevelCache();
    private final static Cache<Class<?>, Object> THREAD_LOCAL_CACHE = Cache.createThreadLocalCache(GLOBAL_CACHE);

    static {
        //
        // Values stored in thread-local cache automatically propagate up to global cache.
        //
        THREAD_LOCAL_CACHE.cache(boolean.class, EMPTY_BOOLEAN_ARRAY);
        THREAD_LOCAL_CACHE.cache(char.class, EMPTY_CHAR_ARRAY);
        THREAD_LOCAL_CACHE.cache(byte.class, EMPTY_BYTE_ARRAY);
        THREAD_LOCAL_CACHE.cache(short.class, EMPTY_SHORT_ARRAY);
        THREAD_LOCAL_CACHE.cache(int.class, EMPTY_INT_ARRAY);
        THREAD_LOCAL_CACHE.cache(long.class, EMPTY_LONG_ARRAY);
        THREAD_LOCAL_CACHE.cache(float.class, EMPTY_FLOAT_ARRAY);
        THREAD_LOCAL_CACHE.cache(double.class, EMPTY_DOUBLE_ARRAY);
        THREAD_LOCAL_CACHE.cache(String.class, EMPTY_STRING_ARRAY);
        THREAD_LOCAL_CACHE.cache(Object.class, EMPTY_OBJECT_ARRAY);
        THREAD_LOCAL_CACHE.cache(Class.class, EMPTY_CLASS_ARRAY);
    }

    private EmptyArrayCache() {
        throw ContractUtils.unreachable();
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] fromElementType(final Class<T> elementType) {
        VerifyArgument.notNull(elementType, "elementType");

        final T[] cachedArray = (T[])THREAD_LOCAL_CACHE.get(elementType);

        if (cachedArray != null) {
            return cachedArray;
        }

        return (T[])THREAD_LOCAL_CACHE.cache(elementType, Array.newInstance(elementType, 0));
    }

    public static Object fromElementOrPrimitiveType(final Class<?> elementType) {
        VerifyArgument.notNull(elementType, "elementType");

        final Object cachedArray = THREAD_LOCAL_CACHE.get(elementType);

        if (cachedArray != null) {
            return cachedArray;
        }

        return THREAD_LOCAL_CACHE.cache(elementType, Array.newInstance(elementType, 0));
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromArrayType(final Class<? extends Object[]> arrayType) {
        return (T)fromElementType(
            VerifyArgument.notNull(arrayType, "arrayType").getComponentType()
        );
    }
}
