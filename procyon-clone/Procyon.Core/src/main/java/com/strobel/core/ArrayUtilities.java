/*
 * ArrayUtilities.java
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

package com.strobel.core;

import com.strobel.collections.Cache;
import com.strobel.util.ContractUtils;
import com.strobel.util.EmptyArrayCache;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Mike Strobel
 */
@SuppressWarnings("unchecked")
public final class ArrayUtilities {
    private final static Cache<Class<?>, Class<?>> GLOBAL_ARRAY_TYPE_CACHE = Cache.createTopLevelCache();
    private final static Cache<Class<?>, Class<?>> ARRAY_TYPE_CACHE = Cache.createThreadLocalCache(GLOBAL_ARRAY_TYPE_CACHE);

    private ArrayUtilities() {
        throw ContractUtils.unreachable();
    }

    public static boolean isArray(final Object value) {
        return value != null &&
               value.getClass().isArray();
    }

    public static <T> T[] create(final Class<T> elementType, final int length) {
        return (T[])Array.newInstance(elementType, length);
    }

    public static Object createAny(final Class<?> elementType, final int length) {
        return Array.newInstance(elementType, length);
    }

    public static int[] range(final int start, final int count) {
        VerifyArgument.isNonNegative(count, "count");

        if (count == 0) {
            return EmptyArrayCache.EMPTY_INT_ARRAY;
        }

        final int[] array = new int[count];

        for (int i = 0, j = start; i < array.length; i++) {
            array[i] = j++;
        }

        return array;
    }

    public static Object copyOf(final Object array, final int newLength) {
        return copyOf(VerifyArgument.notNull(array, "array"), newLength, array.getClass());
    }

    public static Object copyOf(final Object array, final int newLength, final Class<?> newType) {
        final Object copy = newType == Object[].class
                            ? new Object[newLength]
                            : Array.newInstance(newType.getComponentType(), newLength);

        //noinspection SuspiciousSystemArraycopy
        System.arraycopy(array, 0, copy, 0, Math.min(Array.getLength(array), newLength));

        return copy;
    }

    public static Object copyOfRange(final Object array, final int from, final int to) {
        return copyOfRange(VerifyArgument.notNull(array, "array"), from, to, array.getClass());
    }

    public static Object copyOfRange(final Object array, final int from, final int to, final Class<?> newType) {
        final int newLength = to - from;

        if (newLength < 0) {
            throw new IllegalArgumentException(from + " > " + to);
        }

        final Object copy = newType == Object[].class
                            ? new Object[newLength]
                            : Array.newInstance(newType.getComponentType(), newLength);

        //noinspection SuspiciousSystemArraycopy
        System.arraycopy(
            array,
            from,
            copy,
            0,
            Math.min(Array.getLength(array) - from, newLength)
        );
        return copy;
    }

    public static <T> Class<T[]> makeArrayType(final Class<T> elementType) {
        final Class<?> arrayType = ARRAY_TYPE_CACHE.get(elementType);

        if (arrayType != null) {
            return (Class<T[]>)arrayType;
        }

        return (Class<T[]>)ARRAY_TYPE_CACHE.cache(
            elementType,
            Array.newInstance(elementType, 0).getClass()
        );
    }

    public static <T> T[] copy(final T[] source, final T[] target) {
        VerifyArgument.notNull(source, "source");
        return copy(source, 0, target, 0, source.length);
    }

    public static <T> T[] copy(final T[] source, final int offset, final T[] target, final int targetOffset, final int length) {
        VerifyArgument.notNull(source, "source");
        VerifyArgument.validElementRange(source.length, offset, offset + length);

        final T[] actualTarget;
        final int requiredLength = targetOffset + length;

        if (target == null) {
            if (targetOffset == 0) {
                return Arrays.copyOf(source, source.length);
            }
            actualTarget = (T[])Array.newInstance(source.getClass().getComponentType(), requiredLength);
        }
        else if (requiredLength > target.length) {
            if (targetOffset == 0) {
                actualTarget = (T[])Array.newInstance(target.getClass().getComponentType(), length);
            }
            else {
                actualTarget = Arrays.copyOf(target, requiredLength);
            }
        }
        else {
            actualTarget = target;
        }

        System.arraycopy(source, offset, actualTarget, targetOffset, length);

        return actualTarget;
    }

    public static <T> boolean rangeEquals(final T[] first, final T[] second, final int offset, final int length) {
        VerifyArgument.notNull(first, "first");
        VerifyArgument.notNull(second, "second");

        final int end = offset + length;

        if (offset < 0 || end < offset || end > first.length || end > second.length) {
            return false;
        }

        if (first == second) {
            return true;
        }

        for (int i = offset; i < end; i++) {
            if (!Comparer.equals(first[i], second[i])) {
                return false;
            }
        }

        return true;
    }

    public static <T> boolean contains(final T[] array, final T value) {
        VerifyArgument.notNull(array, "array");
        return indexOf(array, value) != -1;
    }

    public static <T> int indexOf(final T[] array, final T value) {
        VerifyArgument.notNull(array, "array");
        if (value == null) {
            for (int i = 0, arrayLength = array.length; i < arrayLength; i++) {
                if (array[i] == null) {
                    return i;
                }
            }
        }
        else {
            for (int i = 0, arrayLength = array.length; i < arrayLength; i++) {
                if (value.equals(array[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static <T> int lastIndexOf(final T[] array, final T value) {
        VerifyArgument.notNull(array, "array");
        if (value == null) {
            for (int i = array.length - 1; i >= 0; i--) {
                if (array[i] == null) {
                    return i;
                }
            }
        }
        else {
            for (int i = array.length - 1; i >= 0; i--) {
                if (value.equals(array[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static <T> T[] insert(final T[] array, final int index, final T value) {
        VerifyArgument.notNull(array, "array");
        VerifyArgument.inRange(0, array.length, index, "index");

        final T[] newArray = (T[])Array.newInstance(
            array.getClass().getComponentType(),
            array.length + 1
        );

        System.arraycopy(array, 0, newArray, 0, index);

        final int remaining = array.length - index;

        if (remaining > 0) {
            System.arraycopy(array, index, newArray, index + 1, remaining);
        }

        newArray[index] = value;

        return newArray;
    }

    @SafeVarargs
    public static <T> T[] insert(final T[] array, final int index, final T... values) {
        VerifyArgument.notNull(array, "array");
        VerifyArgument.inRange(0, array.length, index, "index");

        if (values == null || values.length == 0) {
            return array;
        }

        VerifyArgument.elementsOfType(array.getClass().getComponentType(), values, "values");

        final int newItemCount = values.length;

        final T[] newArray = (T[])Array.newInstance(
            array.getClass().getComponentType(),
            array.length + newItemCount
        );

        System.arraycopy(array, 0, newArray, 0, index);

        final int remaining = array.length - index;

        if (remaining > 0) {
            System.arraycopy(array, index, newArray, index + newItemCount, remaining);
        }

        System.arraycopy(values, 0, newArray, index, newItemCount);

        return newArray;
    }

    public static <T> T[] append(final T[] array, final T value) {
        if (array == null) {
            if (value == null) {
                throw new IllegalArgumentException("At least one value must be specified if 'array' is null.");
            }
            final T[] newArray = (T[])Array.newInstance(value.getClass(), 1);
            newArray[0] = value;
            return newArray;
        }
        return insert(array, VerifyArgument.notNull(array, "array").length, value);
    }

    @SafeVarargs
    public static <T> T[] append(final T[] array, final T... values) {
        if (array == null) {
            if (values == null || values.length == 0) {
                throw new IllegalArgumentException("At least one value must be specified if 'array' is null.");
            }
            final T[] newArray = (T[])Array.newInstance(values.getClass().getComponentType(), values.length);
            System.arraycopy(values, 0, newArray, 0, values.length);
            return newArray;
        }
        return insert(array, VerifyArgument.notNull(array, "array").length, values);
    }

    public static <T> T[] prepend(final T[] array, final T value) {
        if (array == null) {
            if (value == null) {
                throw new IllegalArgumentException("At least one value must be specified if 'array' is null.");
            }
            final T[] newArray = (T[])Array.newInstance(value.getClass(), 1);
            newArray[0] = value;
            return newArray;
        }
        return insert(array, 0, value);
    }

    @SafeVarargs
    public static <T> T[] prepend(final T[] array, final T... values) {
        if (array == null) {
            if (values == null || values.length == 0) {
                throw new IllegalArgumentException("At least one value must be specified if 'array' is null.");
            }
            final T[] newArray = (T[])Array.newInstance(values.getClass().getComponentType(), values.length);
            System.arraycopy(values, 0, newArray, 0, values.length);
            return newArray;
        }
        return insert(array, 0, values);
    }

    public static <T> T[] remove(final T[] array, final int index) {
        VerifyArgument.notNull(array, "array");
        VerifyArgument.inRange(0, array.length - 1, index, "index");

        if (array.length == 1) {
            return EmptyArrayCache.fromArrayType(array.getClass());
        }

        final T[] newArray = (T[])Array.newInstance(
            array.getClass().getComponentType(),
            array.length - 1
        );

        System.arraycopy(array, 0, newArray, 0, index);

        final int remaining = array.length - index - 1;

        if (remaining > 0) {
            System.arraycopy(array, index + 1, newArray, index, remaining);
        }

        return newArray;
    }

    public static <T> boolean isNullOrEmpty(final T[] array) {
        return array == null || array.length == 0;
    }

    @SafeVarargs
    public static <T> T[] removeAll(final T[] array, final T... values) {
        VerifyArgument.notNull(array, "array");

        if (isNullOrEmpty(array)) {
            return array;
        }

        final int count = values.length;

        int matchCount = 0;

        final int[] matchIndices = new int[count];

        for (int i = 0; i < count; i++) {
            final T value = values[i];
            final int index = indexOf(array, value);

            if (index == -1) {
                matchIndices[i] = Integer.MAX_VALUE;
                continue;
            }

            matchIndices[i] = index;
            ++matchCount;
        }

        if (matchCount == 0) {
            return array;
        }

        Arrays.sort(matchIndices);

        final T[] newArray = (T[])Array.newInstance(
            array.getClass().getComponentType(),
            array.length - matchCount
        );

        int sourcePosition = 0;

        for (int i = 0; i < matchCount; i++) {
            final int matchIndex = matchIndices[i];

            if (matchIndex == Integer.MAX_VALUE) {
                break;
            }

            System.arraycopy(array, sourcePosition, newArray, sourcePosition - i, matchIndex);

            sourcePosition = matchIndex + 1;
        }

        final int remaining = array.length - sourcePosition;

        if (remaining > 0) {
            System.arraycopy(array, sourcePosition, newArray, newArray.length - remaining, remaining);
        }

        return newArray;
    }

    public static <T> T[] removeFirst(final T[] array, final T value) {
        final int index = indexOf(VerifyArgument.notNull(array, "array"), value);

        if (index == -1) {
            return array;
        }

        return remove(array, index);
    }

    public static <T> T[] removeLast(final T[] array, final T value) {
        final int index = lastIndexOf(VerifyArgument.notNull(array, "array"), value);

        if (index == -1) {
            return array;
        }

        return remove(array, index);
    }

    @SafeVarargs
    public static <T> T[] retainAll(final T[] array, final T... values) {
        VerifyArgument.notNull(array, "array");

        if (isNullOrEmpty(values)) {
            return array;
        }

        final int count = values.length;

        int matchCount = 0;

        final int[] matchIndices = new int[count];

        for (int i = 0; i < count; i++) {
            final T value = values[i];
            final int index = indexOf(array, value);

            if (index == -1) {
                matchIndices[i] = Integer.MAX_VALUE;
                continue;
            }

            matchIndices[i] = index;
            ++matchCount;
        }

        if (matchCount == 0) {
            return EmptyArrayCache.fromArrayType(array.getClass());
        }

        Arrays.sort(matchIndices);

        final T[] newArray = (T[])Array.newInstance(
            array.getClass().getComponentType(),
            matchCount
        );

        for (int i = 0, j = 0; i < count; i++) {
            final int matchIndex = matchIndices[i];

            if (matchIndex == Integer.MAX_VALUE) {
                continue;
            }

            newArray[j++] = array[matchIndex];
        }

        return newArray;
    }

    @SafeVarargs
    public static <T> T[] union(final T[] array, final T... values) {
        VerifyArgument.notNull(array, "array");

        if (isNullOrEmpty(values)) {
            return array;
        }

        final int count = values.length;

        int matchCount = 0;

        final int[] matchIndices = new int[count];

        for (int i = 0; i < count; i++) {
            final T value = values[i];
            final int index = indexOf(array, value);

            if (index == -1) {
                matchIndices[i] = Integer.MAX_VALUE;
                continue;
            }

            matchIndices[i] = index;
            ++matchCount;
        }

        if (matchCount == 0) {
            return append(array, values);
        }

        final T[] newArray = Arrays.copyOf(array, array.length + values.length - matchCount);

        for (int i = 0, j = array.length; i < count; i++) {
            final int matchIndex = matchIndices[i];

            if (matchIndex == Integer.MAX_VALUE) {
                newArray[j++] = values[i];
            }
        }

        return newArray;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // PRIMITIVE ARRAY SPECIALIZATIONS OF SEARCH FUNCTIONS                        //
    ////////////////////////////////////////////////////////////////////////////////

    public static boolean isNullOrEmpty(final boolean[] array) {
        return array == null || array.length == 0;
    }

    public static boolean[] copy(final boolean[] source, final boolean[] target) {
        VerifyArgument.notNull(source, "source");
        return copy(source, 0, target, 0, source.length);
    }

    public static boolean[] copy(final boolean[] source, final int offset, final boolean[] target, final int targetOffset, final int length) {
        VerifyArgument.notNull(source, "source");
        VerifyArgument.validElementRange(source.length, offset, offset + length);
        VerifyArgument.isNonNegative(targetOffset, "targetOffset");

        final boolean[] actualTarget;
        final int requiredLength = targetOffset + length;

        if (target == null) {
            if (targetOffset == 0) {
                return Arrays.copyOfRange(source, offset, offset + length);
            }
            actualTarget = new boolean[requiredLength];
        }
        else if (requiredLength > target.length) {
            actualTarget = new boolean[requiredLength];
            if (targetOffset != 0) {
                System.arraycopy(target, 0, actualTarget, 0, targetOffset);
            }
        }
        else {
            actualTarget = target;
        }

        System.arraycopy(source, offset, actualTarget, targetOffset, length);

        return actualTarget;
    }

    public static boolean rangeEquals(final boolean[] first, final boolean[] second, final int offset, final int length) {
        VerifyArgument.notNull(first, "first");
        VerifyArgument.notNull(second, "second");

        final int end = offset + length;

        if (offset < 0 || end < offset || end > first.length || end > second.length) {
            return false;
        }

        if (first == second) {
            return true;
        }

        for (int i = offset; i < end; i++) {
            if (first[i] != second[i]) {
                return false;
            }
        }

        return true;
    }

    public static boolean contains(final boolean[] array, final boolean value) {
        VerifyArgument.notNull(array, "array");
        return indexOf(array, value) != -1;
    }

    public static int indexOf(final boolean[] array, final boolean value) {
        VerifyArgument.notNull(array, "array");
        for (int i = 0, arrayLength = array.length; i < arrayLength; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    public static int lastIndexOf(final boolean[] array, final boolean value) {
        VerifyArgument.notNull(array, "array");
        for (int i = array.length - 1; i >= 0; i--) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isNullOrEmpty(final char[] array) {
        return array == null || array.length == 0;
    }

    public static char[] copy(final char[] source, final char[] target) {
        VerifyArgument.notNull(source, "source");
        return copy(source, 0, target, 0, source.length);
    }

    public static char[] copy(final char[] source, final int offset, final char[] target, final int targetOffset, final int length) {
        VerifyArgument.notNull(source, "source");
        VerifyArgument.validElementRange(source.length, offset, offset + length);
        VerifyArgument.isNonNegative(targetOffset, "targetOffset");

        final char[] actualTarget;
        final int requiredLength = targetOffset + length;

        if (target == null) {
            if (targetOffset == 0) {
                return Arrays.copyOfRange(source, offset, offset + length);
            }
            actualTarget = new char[requiredLength];
        }
        else if (requiredLength > target.length) {
            actualTarget = new char[requiredLength];
            if (targetOffset != 0) {
                System.arraycopy(target, 0, actualTarget, 0, targetOffset);
            }
        }
        else {
            actualTarget = target;
        }

        System.arraycopy(source, offset, actualTarget, targetOffset, length);

        return actualTarget;
    }

    public static boolean rangeEquals(final char[] first, final char[] second, final int offset, final int length) {
        VerifyArgument.notNull(first, "first");
        VerifyArgument.notNull(second, "second");

        final int end = offset + length;

        if (offset < 0 || end < offset || end > first.length || end > second.length) {
            return false;
        }

        if (first == second) {
            return true;
        }

        for (int i = offset; i < end; i++) {
            if (first[i] != second[i]) {
                return false;
            }
        }

        return true;
    }

    public static boolean contains(final char[] array, final char value) {
        VerifyArgument.notNull(array, "array");
        return indexOf(array, value) != -1;
    }

    public static int indexOf(final char[] array, final char value) {
        VerifyArgument.notNull(array, "array");
        for (int i = 0, arrayLength = array.length; i < arrayLength; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    public static int lastIndexOf(final char[] array, final char value) {
        VerifyArgument.notNull(array, "array");
        for (int i = array.length - 1; i >= 0; i--) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isNullOrEmpty(final byte[] array) {
        return array == null || array.length == 0;
    }

    public static byte[] copy(final byte[] source, final byte[] target) {
        VerifyArgument.notNull(source, "source");
        return copy(source, 0, target, 0, source.length);
    }

    public static byte[] copy(final byte[] source, final int offset, final byte[] target, final int targetOffset, final int length) {
        VerifyArgument.notNull(source, "source");
        VerifyArgument.validElementRange(source.length, offset, offset + length);
        VerifyArgument.isNonNegative(targetOffset, "targetOffset");

        final byte[] actualTarget;
        final int requiredLength = targetOffset + length;

        if (target == null) {
            if (targetOffset == 0) {
                return Arrays.copyOfRange(source, offset, offset + length);
            }
            actualTarget = new byte[requiredLength];
        }
        else if (requiredLength > target.length) {
            actualTarget = new byte[requiredLength];
            if (targetOffset != 0) {
                System.arraycopy(target, 0, actualTarget, 0, targetOffset);
            }
        }
        else {
            actualTarget = target;
        }

        System.arraycopy(source, offset, actualTarget, targetOffset, length);

        return actualTarget;
    }

    public static boolean rangeEquals(final byte[] first, final byte[] second, final int offset, final int length) {
        VerifyArgument.notNull(first, "first");
        VerifyArgument.notNull(second, "second");

        final int end = offset + length;

        if (offset < 0 || end < offset || end > first.length || end > second.length) {
            return false;
        }

        if (first == second) {
            return true;
        }

        for (int i = offset; i < end; i++) {
            if (first[i] != second[i]) {
                return false;
            }
        }

        return true;
    }

    public static boolean contains(final byte[] array, final byte value) {
        VerifyArgument.notNull(array, "array");
        return indexOf(array, value) != -1;
    }

    public static int indexOf(final byte[] array, final byte value) {
        VerifyArgument.notNull(array, "array");
        for (int i = 0, arrayLength = array.length; i < arrayLength; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    public static int lastIndexOf(final byte[] array, final byte value) {
        VerifyArgument.notNull(array, "array");
        for (int i = array.length - 1; i >= 0; i--) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isNullOrEmpty(final short[] array) {
        return array == null || array.length == 0;
    }

    public static short[] copy(final short[] source, final short[] target) {
        VerifyArgument.notNull(source, "source");
        return copy(source, 0, target, 0, source.length);
    }

    public static short[] copy(final short[] source, final int offset, final short[] target, final int targetOffset, final int length) {
        VerifyArgument.notNull(source, "source");
        VerifyArgument.validElementRange(source.length, offset, offset + length);
        VerifyArgument.isNonNegative(targetOffset, "targetOffset");

        final short[] actualTarget;
        final int requiredLength = targetOffset + length;

        if (target == null) {
            if (targetOffset == 0) {
                return Arrays.copyOfRange(source, offset, offset + length);
            }
            actualTarget = new short[requiredLength];
        }
        else if (requiredLength > target.length) {
            actualTarget = new short[requiredLength];
            if (targetOffset != 0) {
                System.arraycopy(target, 0, actualTarget, 0, targetOffset);
            }
        }
        else {
            actualTarget = target;
        }

        System.arraycopy(source, offset, actualTarget, targetOffset, length);

        return actualTarget;
    }

    public static boolean rangeEquals(final short[] first, final short[] second, final int offset, final int length) {
        VerifyArgument.notNull(first, "first");
        VerifyArgument.notNull(second, "second");

        final int end = offset + length;

        if (offset < 0 || end < offset || end > first.length || end > second.length) {
            return false;
        }

        if (first == second) {
            return true;
        }

        for (int i = offset; i < end; i++) {
            if (first[i] != second[i]) {
                return false;
            }
        }

        return true;
    }

    public static boolean contains(final short[] array, final short value) {
        VerifyArgument.notNull(array, "array");
        return indexOf(array, value) != -1;
    }

    public static int indexOf(final short[] array, final short value) {
        VerifyArgument.notNull(array, "array");
        for (int i = 0, arrayLength = array.length; i < arrayLength; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    public static int lastIndexOf(final short[] array, final short value) {
        VerifyArgument.notNull(array, "array");
        for (int i = array.length - 1; i >= 0; i--) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isNullOrEmpty(final int[] array) {
        return array == null || array.length == 0;
    }

    public static int[] copy(final int[] source, final int[] target) {
        VerifyArgument.notNull(source, "source");
        return copy(source, 0, target, 0, source.length);
    }

    public static int[] copy(final int[] source, final int offset, final int[] target, final int targetOffset, final int length) {
        VerifyArgument.notNull(source, "source");
        VerifyArgument.validElementRange(source.length, offset, offset + length);
        VerifyArgument.isNonNegative(targetOffset, "targetOffset");

        final int[] actualTarget;
        final int requiredLength = targetOffset + length;

        if (target == null) {
            if (targetOffset == 0) {
                return Arrays.copyOfRange(source, offset, offset + length);
            }
            actualTarget = new int[requiredLength];
        }
        else if (requiredLength > target.length) {
            actualTarget = new int[requiredLength];
            if (targetOffset != 0) {
                System.arraycopy(target, 0, actualTarget, 0, targetOffset);
            }
        }
        else {
            actualTarget = target;
        }

        System.arraycopy(source, offset, actualTarget, targetOffset, length);

        return actualTarget;
    }

    public static boolean rangeEquals(final int[] first, final int[] second, final int offset, final int length) {
        VerifyArgument.notNull(first, "first");
        VerifyArgument.notNull(second, "second");

        final int end = offset + length;

        if (offset < 0 || end < offset || end > first.length || end > second.length) {
            return false;
        }

        if (first == second) {
            return true;
        }

        for (int i = offset; i < end; i++) {
            if (first[i] != second[i]) {
                return false;
            }
        }

        return true;
    }

    public static boolean contains(final int[] array, final int value) {
        VerifyArgument.notNull(array, "array");
        return indexOf(array, value) != -1;
    }

    public static int indexOf(final int[] array, final int value) {
        VerifyArgument.notNull(array, "array");
        for (int i = 0, arrayLength = array.length; i < arrayLength; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    public static int lastIndexOf(final int[] array, final int value) {
        VerifyArgument.notNull(array, "array");
        for (int i = array.length - 1; i >= 0; i--) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isNullOrEmpty(final long[] array) {
        return array == null || array.length == 0;
    }

    public static long[] copy(final long[] source, final long[] target) {
        VerifyArgument.notNull(source, "source");
        return copy(source, 0, target, 0, source.length);
    }

    public static long[] copy(final long[] source, final int offset, final long[] target, final int targetOffset, final int length) {
        VerifyArgument.notNull(source, "source");
        VerifyArgument.validElementRange(source.length, offset, offset + length);
        VerifyArgument.isNonNegative(targetOffset, "targetOffset");

        final long[] actualTarget;
        final int requiredLength = targetOffset + length;

        if (target == null) {
            if (targetOffset == 0) {
                return Arrays.copyOfRange(source, offset, offset + length);
            }
            actualTarget = new long[requiredLength];
        }
        else if (requiredLength > target.length) {
            actualTarget = new long[requiredLength];
            if (targetOffset != 0) {
                System.arraycopy(target, 0, actualTarget, 0, targetOffset);
            }
        }
        else {
            actualTarget = target;
        }

        System.arraycopy(source, offset, actualTarget, targetOffset, length);

        return actualTarget;
    }

    public static boolean rangeEquals(final long[] first, final long[] second, final int offset, final int length) {
        VerifyArgument.notNull(first, "first");
        VerifyArgument.notNull(second, "second");

        final int end = offset + length;

        if (offset < 0 || end < offset || end > first.length || end > second.length) {
            return false;
        }

        if (first == second) {
            return true;
        }

        for (int i = offset; i < end; i++) {
            if (first[i] != second[i]) {
                return false;
            }
        }

        return true;
    }

    public static boolean contains(final long[] array, final long value) {
        VerifyArgument.notNull(array, "array");
        return indexOf(array, value) != -1;
    }

    public static int indexOf(final long[] array, final long value) {
        VerifyArgument.notNull(array, "array");
        for (int i = 0, arrayLength = array.length; i < arrayLength; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    public static int lastIndexOf(final long[] array, final long value) {
        VerifyArgument.notNull(array, "array");
        for (int i = array.length - 1; i >= 0; i--) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isNullOrEmpty(final float[] array) {
        return array == null || array.length == 0;
    }

    public static float[] copy(final float[] source, final float[] target) {
        VerifyArgument.notNull(source, "source");
        return copy(source, 0, target, 0, source.length);
    }

    public static float[] copy(final float[] source, final int offset, final float[] target, final int targetOffset, final int length) {
        VerifyArgument.notNull(source, "source");
        VerifyArgument.validElementRange(source.length, offset, offset + length);
        VerifyArgument.isNonNegative(targetOffset, "targetOffset");

        final float[] actualTarget;
        final int requiredLength = targetOffset + length;

        if (target == null) {
            if (targetOffset == 0) {
                return Arrays.copyOfRange(source, offset, offset + length);
            }
            actualTarget = new float[requiredLength];
        }
        else if (requiredLength > target.length) {
            actualTarget = new float[requiredLength];
            if (targetOffset != 0) {
                System.arraycopy(target, 0, actualTarget, 0, targetOffset);
            }
        }
        else {
            actualTarget = target;
        }

        System.arraycopy(source, offset, actualTarget, targetOffset, length);

        return actualTarget;
    }

    public static boolean rangeEquals(final float[] first, final float[] second, final int offset, final int length) {
        VerifyArgument.notNull(first, "first");
        VerifyArgument.notNull(second, "second");

        final int end = offset + length;

        if (offset < 0 || end < offset || end > first.length || end > second.length) {
            return false;
        }

        if (first == second) {
            return true;
        }

        for (int i = offset; i < end; i++) {
            if (first[i] != second[i]) {
                return false;
            }
        }

        return true;
    }

    public static boolean contains(final float[] array, final float value) {
        VerifyArgument.notNull(array, "array");
        return indexOf(array, value) != -1;
    }

    public static int indexOf(final float[] array, final float value) {
        VerifyArgument.notNull(array, "array");
        for (int i = 0, arrayLength = array.length; i < arrayLength; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    public static int lastIndexOf(final float[] array, final float value) {
        VerifyArgument.notNull(array, "array");
        for (int i = array.length - 1; i >= 0; i--) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isNullOrEmpty(final double[] array) {
        return array == null || array.length == 0;
    }

    public static double[] copy(final double[] source, final double[] target) {
        VerifyArgument.notNull(source, "source");
        return copy(source, 0, target, 0, source.length);
    }

    public static double[] copy(final double[] source, final int offset, final double[] target, final int targetOffset, final int length) {
        VerifyArgument.notNull(source, "source");
        VerifyArgument.validElementRange(source.length, offset, offset + length);
        VerifyArgument.isNonNegative(targetOffset, "targetOffset");

        final double[] actualTarget;
        final int requiredLength = targetOffset + length;

        if (target == null) {
            if (targetOffset == 0) {
                return Arrays.copyOfRange(source, offset, offset + length);
            }
            actualTarget = new double[requiredLength];
        }
        else if (requiredLength > target.length) {
            actualTarget = new double[requiredLength];
            if (targetOffset != 0) {
                System.arraycopy(target, 0, actualTarget, 0, targetOffset);
            }
        }
        else {
            actualTarget = target;
        }

        System.arraycopy(source, offset, actualTarget, targetOffset, length);

        return actualTarget;
    }

    public static boolean rangeEquals(final double[] first, final double[] second, final int offset, final int length) {
        VerifyArgument.notNull(first, "first");
        VerifyArgument.notNull(second, "second");

        final int end = offset + length;

        if (offset < 0 || end < offset || end > first.length || end > second.length) {
            return false;
        }

        if (first == second) {
            return true;
        }

        for (int i = offset; i < end; i++) {
            if (first[i] != second[i]) {
                return false;
            }
        }

        return true;
    }

    public static boolean contains(final double[] array, final double value) {
        VerifyArgument.notNull(array, "array");
        return indexOf(array, value) != -1;
    }

    public static int indexOf(final double[] array, final double value) {
        VerifyArgument.notNull(array, "array");
        for (int i = 0, arrayLength = array.length; i < arrayLength; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    public static int lastIndexOf(final double[] array, final double value) {
        VerifyArgument.notNull(array, "array");
        for (int i = array.length - 1; i >= 0; i--) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // PRIMITIVE ARRAY SPECIALIZATIONS OF MANIPULATION FUNCTIONS                  //
    ////////////////////////////////////////////////////////////////////////////////

    public static boolean[] append(final boolean[] array, final boolean value) {
        if (isNullOrEmpty(array)) {
            return new boolean[] { value };
        }
        return insert(array, VerifyArgument.notNull(array, "array").length, value);
    }

    public static boolean[] append(final boolean[] array, final boolean... values) {
        if (isNullOrEmpty(array)) {
            if (isNullOrEmpty(values)) {
                return values;
            }
            return Arrays.copyOf(values, values.length);
        }
        return insert(array, VerifyArgument.notNull(array, "array").length, values);
    }

    public static boolean[] prepend(final boolean[] array, final boolean value) {
        if (isNullOrEmpty(array)) {
            return new boolean[] { value };
        }
        return insert(array, 0, value);
    }

    public static boolean[] prepend(final boolean[] array, final boolean... values) {
        if (isNullOrEmpty(array)) {
            if (isNullOrEmpty(values)) {
                return values;
            }
            return Arrays.copyOf(values, values.length);
        }
        return insert(array, 0, values);
    }

    public static boolean[] remove(final boolean[] array, final int index) {
        VerifyArgument.notNull(array, "array");
        VerifyArgument.inRange(0, array.length - 1, index, "index");

        if (array.length == 1) {
            return EmptyArrayCache.EMPTY_BOOLEAN_ARRAY;
        }

        final boolean[] newArray = new boolean[array.length - 1];

        System.arraycopy(array, 0, newArray, 0, index);

        final int remaining = array.length - index - 1;

        if (remaining > 0) {
            System.arraycopy(array, index + 1, newArray, index, remaining);
        }

        return newArray;
    }

    public static boolean[] insert(final boolean[] array, final int index, final boolean value) {
        VerifyArgument.notNull(array, "array");
        VerifyArgument.inRange(0, array.length, index, "index");

        final boolean[] newArray = new boolean[array.length + 1];

        System.arraycopy(array, 0, newArray, 0, index);

        final int remaining = array.length - index;

        if (remaining > 0) {
            System.arraycopy(array, index, newArray, index + 1, remaining);
        }

        newArray[index] = value;

        return newArray;
    }

    public static boolean[] insert(final boolean[] array, final int index, final boolean... values) {
        VerifyArgument.notNull(array, "array");
        VerifyArgument.inRange(0, array.length, index, "index");

        if (values == null || values.length == 0) {
            return array;
        }

        final int newItemCount = values.length;
        final boolean[] newArray = new boolean[array.length + newItemCount];

        System.arraycopy(array, 0, newArray, 0, index);

        final int remaining = array.length - index;

        if (remaining > 0) {
            System.arraycopy(array, index, newArray, index + newItemCount, remaining);
        }

        System.arraycopy(values, 0, newArray, index, newItemCount);

        return newArray;
    }

    public static char[] append(final char[] array, final char value) {
        if (isNullOrEmpty(array)) {
            return new char[] { value };
        }
        return insert(array, VerifyArgument.notNull(array, "array").length, value);
    }

    public static char[] append(final char[] array, final char... values) {
        if (isNullOrEmpty(array)) {
            if (isNullOrEmpty(values)) {
                return values;
            }
            return Arrays.copyOf(values, values.length);
        }
        return insert(array, VerifyArgument.notNull(array, "array").length, values);
    }

    public static char[] prepend(final char[] array, final char value) {
        if (isNullOrEmpty(array)) {
            return new char[] { value };
        }
        return insert(array, 0, value);
    }

    public static char[] prepend(final char[] array, final char... values) {
        if (isNullOrEmpty(array)) {
            if (isNullOrEmpty(values)) {
                return values;
            }
            return Arrays.copyOf(values, values.length);
        }
        return insert(array, 0, values);
    }

    public static char[] remove(final char[] array, final int index) {
        VerifyArgument.notNull(array, "array");
        VerifyArgument.inRange(0, array.length - 1, index, "index");

        if (array.length == 1) {
            return EmptyArrayCache.EMPTY_CHAR_ARRAY;
        }

        final char[] newArray = new char[array.length - 1];

        System.arraycopy(array, 0, newArray, 0, index);

        final int remaining = array.length - index - 1;

        if (remaining > 0) {
            System.arraycopy(array, index + 1, newArray, index, remaining);
        }

        return newArray;
    }

    public static char[] insert(final char[] array, final int index, final char value) {
        VerifyArgument.notNull(array, "array");
        VerifyArgument.inRange(0, array.length, index, "index");

        final char[] newArray = new char[array.length + 1];

        System.arraycopy(array, 0, newArray, 0, index);

        final int remaining = array.length - index;

        if (remaining > 0) {
            System.arraycopy(array, index, newArray, index + 1, remaining);
        }

        newArray[index] = value;

        return newArray;
    }

    public static char[] insert(final char[] array, final int index, final char... values) {
        VerifyArgument.notNull(array, "array");
        VerifyArgument.inRange(0, array.length, index, "index");

        if (values == null || values.length == 0) {
            return array;
        }

        final int newItemCount = values.length;
        final char[] newArray = new char[array.length + newItemCount];

        System.arraycopy(array, 0, newArray, 0, index);

        final int remaining = array.length - index;

        if (remaining > 0) {
            System.arraycopy(array, index, newArray, index + newItemCount, remaining);
        }

        System.arraycopy(values, 0, newArray, index, newItemCount);

        return newArray;
    }

    public static byte[] append(final byte[] array, final byte value) {
        if (isNullOrEmpty(array)) {
            return new byte[] { value };
        }
        return insert(array, VerifyArgument.notNull(array, "array").length, value);
    }

    public static byte[] append(final byte[] array, final byte... values) {
        if (isNullOrEmpty(array)) {
            if (isNullOrEmpty(values)) {
                return values;
            }
            return Arrays.copyOf(values, values.length);
        }
        return insert(array, VerifyArgument.notNull(array, "array").length, values);
    }

    public static byte[] prepend(final byte[] array, final byte value) {
        if (isNullOrEmpty(array)) {
            return new byte[] { value };
        }
        return insert(array, 0, value);
    }

    public static byte[] prepend(final byte[] array, final byte... values) {
        if (isNullOrEmpty(array)) {
            if (isNullOrEmpty(values)) {
                return values;
            }
            return Arrays.copyOf(values, values.length);
        }
        return insert(array, 0, values);
    }

    public static byte[] remove(final byte[] array, final int index) {
        VerifyArgument.notNull(array, "array");
        VerifyArgument.inRange(0, array.length - 1, index, "index");

        if (array.length == 1) {
            return EmptyArrayCache.EMPTY_BYTE_ARRAY;
        }

        final byte[] newArray = new byte[array.length - 1];

        System.arraycopy(array, 0, newArray, 0, index);

        final int remaining = array.length - index - 1;

        if (remaining > 0) {
            System.arraycopy(array, index + 1, newArray, index, remaining);
        }

        return newArray;
    }

    public static byte[] insert(final byte[] array, final int index, final byte value) {
        VerifyArgument.notNull(array, "array");
        VerifyArgument.inRange(0, array.length, index, "index");

        final byte[] newArray = new byte[array.length + 1];

        System.arraycopy(array, 0, newArray, 0, index);

        final int remaining = array.length - index;

        if (remaining > 0) {
            System.arraycopy(array, index, newArray, index + 1, remaining);
        }

        newArray[index] = value;

        return newArray;
    }

    public static byte[] insert(final byte[] array, final int index, final byte... values) {
        VerifyArgument.notNull(array, "array");
        VerifyArgument.inRange(0, array.length, index, "index");

        if (values == null || values.length == 0) {
            return array;
        }

        final int newItemCount = values.length;
        final byte[] newArray = new byte[array.length + newItemCount];

        System.arraycopy(array, 0, newArray, 0, index);

        final int remaining = array.length - index;

        if (remaining > 0) {
            System.arraycopy(array, index, newArray, index + newItemCount, remaining);
        }

        System.arraycopy(values, 0, newArray, index, newItemCount);

        return newArray;
    }

    public static short[] append(final short[] array, final short value) {
        if (isNullOrEmpty(array)) {
            return new short[] { value };
        }
        return insert(array, VerifyArgument.notNull(array, "array").length, value);
    }

    public static short[] append(final short[] array, final short... values) {
        if (isNullOrEmpty(array)) {
            if (isNullOrEmpty(values)) {
                return values;
            }
            return Arrays.copyOf(values, values.length);
        }
        return insert(array, VerifyArgument.notNull(array, "array").length, values);
    }

    public static short[] prepend(final short[] array, final short value) {
        if (isNullOrEmpty(array)) {
            return new short[] { value };
        }
        return insert(array, 0, value);
    }

    public static short[] prepend(final short[] array, final short... values) {
        if (isNullOrEmpty(array)) {
            if (isNullOrEmpty(values)) {
                return values;
            }
            return Arrays.copyOf(values, values.length);
        }
        return insert(array, 0, values);
    }

    public static short[] remove(final short[] array, final int index) {
        VerifyArgument.notNull(array, "array");
        VerifyArgument.inRange(0, array.length - 1, index, "index");

        if (array.length == 1) {
            return EmptyArrayCache.EMPTY_SHORT_ARRAY;
        }

        final short[] newArray = new short[array.length - 1];

        System.arraycopy(array, 0, newArray, 0, index);

        final int remaining = array.length - index - 1;

        if (remaining > 0) {
            System.arraycopy(array, index + 1, newArray, index, remaining);
        }

        return newArray;
    }

    public static short[] insert(final short[] array, final int index, final short value) {
        VerifyArgument.notNull(array, "array");
        VerifyArgument.inRange(0, array.length, index, "index");

        final short[] newArray = new short[array.length + 1];

        System.arraycopy(array, 0, newArray, 0, index);

        final int remaining = array.length - index;

        if (remaining > 0) {
            System.arraycopy(array, index, newArray, index + 1, remaining);
        }

        newArray[index] = value;

        return newArray;
    }

    public static short[] insert(final short[] array, final int index, final short... values) {
        VerifyArgument.notNull(array, "array");
        VerifyArgument.inRange(0, array.length, index, "index");

        if (values == null || values.length == 0) {
            return array;
        }

        final int newItemCount = values.length;
        final short[] newArray = new short[array.length + newItemCount];

        System.arraycopy(array, 0, newArray, 0, index);

        final int remaining = array.length - index;

        if (remaining > 0) {
            System.arraycopy(array, index, newArray, index + newItemCount, remaining);
        }

        System.arraycopy(values, 0, newArray, index, newItemCount);

        return newArray;
    }

    public static int[] append(final int[] array, final int value) {
        if (isNullOrEmpty(array)) {
            return new int[] { value };
        }
        return insert(array, VerifyArgument.notNull(array, "array").length, value);
    }

    public static int[] append(final int[] array, final int... values) {
        if (isNullOrEmpty(array)) {
            if (isNullOrEmpty(values)) {
                return values;
            }
            return Arrays.copyOf(values, values.length);
        }
        return insert(array, VerifyArgument.notNull(array, "array").length, values);
    }

    public static int[] prepend(final int[] array, final int value) {
        if (isNullOrEmpty(array)) {
            return new int[] { value };
        }
        return insert(array, 0, value);
    }

    public static int[] prepend(final int[] array, final int... values) {
        if (isNullOrEmpty(array)) {
            if (isNullOrEmpty(values)) {
                return values;
            }
            return Arrays.copyOf(values, values.length);
        }
        return insert(array, 0, values);
    }

    public static int[] remove(final int[] array, final int index) {
        VerifyArgument.notNull(array, "array");
        VerifyArgument.inRange(0, array.length - 1, index, "index");

        if (array.length == 1) {
            return EmptyArrayCache.EMPTY_INT_ARRAY;
        }

        final int[] newArray = new int[array.length - 1];

        System.arraycopy(array, 0, newArray, 0, index);

        final int remaining = array.length - index - 1;

        if (remaining > 0) {
            System.arraycopy(array, index + 1, newArray, index, remaining);
        }

        return newArray;
    }

    public static int[] insert(final int[] array, final int index, final int value) {
        VerifyArgument.notNull(array, "array");
        VerifyArgument.inRange(0, array.length, index, "index");

        final int[] newArray = new int[array.length + 1];

        System.arraycopy(array, 0, newArray, 0, index);

        final int remaining = array.length - index;

        if (remaining > 0) {
            System.arraycopy(array, index, newArray, index + 1, remaining);
        }

        newArray[index] = value;

        return newArray;
    }

    public static int[] insert(final int[] array, final int index, final int... values) {
        VerifyArgument.notNull(array, "array");
        VerifyArgument.inRange(0, array.length, index, "index");

        if (values == null || values.length == 0) {
            return array;
        }

        final int newItemCount = values.length;
        final int[] newArray = new int[array.length + newItemCount];

        System.arraycopy(array, 0, newArray, 0, index);

        final int remaining = array.length - index;

        if (remaining > 0) {
            System.arraycopy(array, index, newArray, index + newItemCount, remaining);
        }

        System.arraycopy(values, 0, newArray, index, newItemCount);

        return newArray;
    }

    public static long[] append(final long[] array, final long value) {
        if (isNullOrEmpty(array)) {
            return new long[] { value };
        }
        return insert(array, VerifyArgument.notNull(array, "array").length, value);
    }

    public static long[] append(final long[] array, final long... values) {
        if (isNullOrEmpty(array)) {
            if (isNullOrEmpty(values)) {
                return values;
            }
            return Arrays.copyOf(values, values.length);
        }
        return insert(array, VerifyArgument.notNull(array, "array").length, values);
    }

    public static long[] prepend(final long[] array, final long value) {
        if (isNullOrEmpty(array)) {
            return new long[] { value };
        }
        return insert(array, 0, value);
    }

    public static long[] prepend(final long[] array, final long... values) {
        if (isNullOrEmpty(array)) {
            if (isNullOrEmpty(values)) {
                return values;
            }
            return Arrays.copyOf(values, values.length);
        }
        return insert(array, 0, values);
    }

    public static long[] remove(final long[] array, final int index) {
        VerifyArgument.notNull(array, "array");
        VerifyArgument.inRange(0, array.length - 1, index, "index");

        if (array.length == 1) {
            return EmptyArrayCache.EMPTY_LONG_ARRAY;
        }

        final long[] newArray = new long[array.length - 1];

        System.arraycopy(array, 0, newArray, 0, index);

        final int remaining = array.length - index - 1;

        if (remaining > 0) {
            System.arraycopy(array, index + 1, newArray, index, remaining);
        }

        return newArray;
    }

    public static long[] insert(final long[] array, final int index, final long value) {
        VerifyArgument.notNull(array, "array");
        VerifyArgument.inRange(0, array.length, index, "index");

        final long[] newArray = new long[array.length + 1];

        System.arraycopy(array, 0, newArray, 0, index);

        final int remaining = array.length - index;

        if (remaining > 0) {
            System.arraycopy(array, index, newArray, index + 1, remaining);
        }

        newArray[index] = value;

        return newArray;
    }

    public static long[] insert(final long[] array, final int index, final long... values) {
        VerifyArgument.notNull(array, "array");
        VerifyArgument.inRange(0, array.length, index, "index");

        if (values == null || values.length == 0) {
            return array;
        }

        final int newItemCount = values.length;
        final long[] newArray = new long[array.length + newItemCount];

        System.arraycopy(array, 0, newArray, 0, index);

        final int remaining = array.length - index;

        if (remaining > 0) {
            System.arraycopy(array, index, newArray, index + newItemCount, remaining);
        }

        System.arraycopy(values, 0, newArray, index, newItemCount);

        return newArray;
    }

    public static float[] append(final float[] array, final float value) {
        if (isNullOrEmpty(array)) {
            return new float[] { value };
        }
        return insert(array, VerifyArgument.notNull(array, "array").length, value);
    }

    public static float[] append(final float[] array, final float... values) {
        if (isNullOrEmpty(array)) {
            if (isNullOrEmpty(values)) {
                return values;
            }
            return Arrays.copyOf(values, values.length);
        }
        return insert(array, VerifyArgument.notNull(array, "array").length, values);
    }

    public static float[] prepend(final float[] array, final float value) {
        if (isNullOrEmpty(array)) {
            return new float[] { value };
        }
        return insert(array, 0, value);
    }

    public static float[] prepend(final float[] array, final float... values) {
        if (isNullOrEmpty(array)) {
            if (isNullOrEmpty(values)) {
                return values;
            }
            return Arrays.copyOf(values, values.length);
        }
        return insert(array, 0, values);
    }

    public static float[] remove(final float[] array, final int index) {
        VerifyArgument.notNull(array, "array");
        VerifyArgument.inRange(0, array.length - 1, index, "index");

        if (array.length == 1) {
            return EmptyArrayCache.EMPTY_FLOAT_ARRAY;
        }

        final float[] newArray = new float[array.length - 1];

        System.arraycopy(array, 0, newArray, 0, index);

        final int remaining = array.length - index - 1;

        if (remaining > 0) {
            System.arraycopy(array, index + 1, newArray, index, remaining);
        }

        return newArray;
    }

    public static float[] insert(final float[] array, final int index, final float value) {
        VerifyArgument.notNull(array, "array");
        VerifyArgument.inRange(0, array.length, index, "index");

        final float[] newArray = new float[array.length + 1];

        System.arraycopy(array, 0, newArray, 0, index);

        final int remaining = array.length - index;

        if (remaining > 0) {
            System.arraycopy(array, index, newArray, index + 1, remaining);
        }

        newArray[index] = value;

        return newArray;
    }

    public static float[] insert(final float[] array, final int index, final float... values) {
        VerifyArgument.notNull(array, "array");
        VerifyArgument.inRange(0, array.length, index, "index");

        if (values == null || values.length == 0) {
            return array;
        }

        final int newItemCount = values.length;
        final float[] newArray = new float[array.length + newItemCount];

        System.arraycopy(array, 0, newArray, 0, index);

        final int remaining = array.length - index;

        if (remaining > 0) {
            System.arraycopy(array, index, newArray, index + newItemCount, remaining);
        }

        System.arraycopy(values, 0, newArray, index, newItemCount);

        return newArray;
    }

    public static double[] append(final double[] array, final double value) {
        if (isNullOrEmpty(array)) {
            return new double[] { value };
        }
        return insert(array, VerifyArgument.notNull(array, "array").length, value);
    }

    public static double[] append(final double[] array, final double... values) {
        if (isNullOrEmpty(array)) {
            if (isNullOrEmpty(values)) {
                return values;
            }
            return Arrays.copyOf(values, values.length);
        }
        return insert(array, VerifyArgument.notNull(array, "array").length, values);
    }

    public static double[] prepend(final double[] array, final double value) {
        if (isNullOrEmpty(array)) {
            return new double[] { value };
        }
        return insert(array, 0, value);
    }

    public static double[] prepend(final double[] array, final double... values) {
        if (isNullOrEmpty(array)) {
            if (isNullOrEmpty(values)) {
                return values;
            }
            return Arrays.copyOf(values, values.length);
        }
        return insert(array, 0, values);
    }

    public static double[] remove(final double[] array, final int index) {
        VerifyArgument.notNull(array, "array");
        VerifyArgument.inRange(0, array.length - 1, index, "index");

        if (array.length == 1) {
            return EmptyArrayCache.EMPTY_DOUBLE_ARRAY;
        }

        final double[] newArray = new double[array.length - 1];

        System.arraycopy(array, 0, newArray, 0, index);

        final int remaining = array.length - index - 1;

        if (remaining > 0) {
            System.arraycopy(array, index + 1, newArray, index, remaining);
        }

        return newArray;
    }

    public static double[] insert(final double[] array, final int index, final double value) {
        VerifyArgument.notNull(array, "array");
        VerifyArgument.inRange(0, array.length, index, "index");

        final double[] newArray = new double[array.length + 1];

        System.arraycopy(array, 0, newArray, 0, index);

        final int remaining = array.length - index;

        if (remaining > 0) {
            System.arraycopy(array, index, newArray, index + 1, remaining);
        }

        newArray[index] = value;

        return newArray;
    }

    public static double[] insert(final double[] array, final int index, final double... values) {
        VerifyArgument.notNull(array, "array");
        VerifyArgument.inRange(0, array.length, index, "index");

        if (values == null || values.length == 0) {
            return array;
        }

        final int newItemCount = values.length;
        final double[] newArray = new double[array.length + newItemCount];

        System.arraycopy(array, 0, newArray, 0, index);

        final int remaining = array.length - index;

        if (remaining > 0) {
            System.arraycopy(array, index, newArray, index + newItemCount, remaining);
        }

        System.arraycopy(values, 0, newArray, index, newItemCount);

        return newArray;
    }

    @SafeVarargs
    public static <T> List<T> asUnmodifiableList(final T... items) {
        return new UnmodifiableArrayList<>(items);
    }

    private final static class UnmodifiableArrayList<T> extends AbstractList<T> {
        private final T[] _array;

        private UnmodifiableArrayList(final T[] array) {
            _array = VerifyArgument.notNull(array, "array");
        }

        @Override
        public T get(final int index) {
            return _array[index];
        }

        @Override
        public int size() {
            return _array.length;
        }
    }
}