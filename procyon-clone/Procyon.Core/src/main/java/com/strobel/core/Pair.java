/*
 * Pair.java
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
public final class Pair<TFirst, TSecond> implements Comparable<Pair<TFirst, TSecond>> {
    private static final int UninitializedHashCode = Integer.MIN_VALUE;

    private final static int FirstNullHash = 0x61E04917;
    private final static int SecondNullHash = 0x198ED6A3;

    private final TFirst _first;
    private final TSecond _second;

    private int _cachedHashCode = UninitializedHashCode;

    public Pair(final TFirst first, final TSecond second) {
        _first = first;
        _second = second;
    }

    public final TFirst getFirst() {
        return _first;
    }

    public final TSecond getSecond() {
        return _second;
    }

    public final boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Pair<?, ?>)) {
            return false;
        }

        final Pair<?, ?> other = (Pair<?, ?>)obj;

        return Comparer.equals(_first, other._first) &&
               Comparer.equals(_second, other._second);
    }

    public final boolean equals(final Pair<? extends TFirst, ? extends TSecond> other) {
        return other != null &&
               Comparer.equals(_first, other._first) &&
               Comparer.equals(_second, other._second);
    }

    @Override
    public final int hashCode() {
        if (_cachedHashCode != UninitializedHashCode) {
            return _cachedHashCode;
        }

        final int combinedHash = HashUtilities.combineHashCodes(
            _first == null ? FirstNullHash : _first.hashCode(),
            _second == null ? SecondNullHash : _second.hashCode()
        );

        _cachedHashCode = combinedHash;

        return combinedHash;
    }

    @Override
    public int compareTo(final Pair<TFirst, TSecond> o) {
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

        return Comparer.compare(_second, o._second);
    }

    @Override
    public final String toString() {
        return String.format("(%s; %s)", _first, _second);
    }

    public static <TFirst, TSecond> Pair<TFirst, TSecond> create(final TFirst first, final TSecond second) {
        return new Pair<>(first, second);
    }
}
