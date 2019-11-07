/*
 * Triple.java
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

/**
 * @author strobelm
 */
public final class Triple<TFirst, TSecond, TThird> implements Comparable<Triple<TFirst, TSecond, TThird>> {
    private static final int UninitializedHashCode = Integer.MIN_VALUE;

    private final static int FirstNullHash = 0x61E04917;
    private final static int SecondNullHash = 0x198ED6A3;
    private final static int ThirdNullHash = 0x40FC1877;

    private final TFirst _first;
    private final TSecond _second;
    private final TThird _third;

    private int _cachedHashCode = UninitializedHashCode;

    public Triple(final TFirst first, final TSecond second, final TThird third) {
        _first = first;
        _second = second;
        _third = third;
    }

    public final TFirst getFirst() {
        return _first;
    }

    public final TSecond getSecond() {
        return _second;
    }

    public final TThird getThird() {
        return _third;
    }

    public final boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Triple<?, ?, ?>)) {
            return false;
        }

        final Triple<?, ?, ?> other = (Triple<?, ?, ?>)obj;

        return Comparer.equals(_first, other._first) &&
               Comparer.equals(_second, other._second) &&
               Comparer.equals(_third, other._third);
    }

    public final boolean equals(final Triple<? extends TFirst, ? extends TSecond, ? extends TThird> other) {
        return other != null &&
               Comparer.equals(_first, other._first) &&
               Comparer.equals(_second, other._second) &&
               Comparer.equals(_third, other._third);
    }

    @Override
    public final int hashCode() {
        if (_cachedHashCode != Integer.MIN_VALUE) {
            return _cachedHashCode;
        }

        final int combinedHash = HashUtilities.combineHashCodes(
            _first == null ? FirstNullHash : _first.hashCode(),
            _second == null ? SecondNullHash : _second.hashCode(),
            _third == null ? ThirdNullHash : _third.hashCode()
        );

        _cachedHashCode = combinedHash;

        return combinedHash;
    }

    @Override
    public int compareTo(final Triple<TFirst, TSecond, TThird> o) {
        if (o == this) {
            return 0;
        }

        if (o == null) {
            return 1;
        }

        final int firstCompare = Comparer.compare(_first, o._first);

        if (firstCompare != 0) {
            return firstCompare;
        }

        final int secondCompare = Comparer.compare(_second, o._second);

        if (secondCompare != 0) {
            return secondCompare;
        }

        return Comparer.compare(_third, o._third);
    }

    @Override
    public final String toString() {
        return String.format("Triple[%s, %s, %s]", _first, _second, _third);
    }

    public static <TFirst, TSecond, TThird> Triple<TFirst, TSecond, TThird> create(
        final TFirst first,
        final TSecond second,
        final TThird third) {

        return new Triple<>(first, second, third);
    }
}
