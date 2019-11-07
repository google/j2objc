/*
 * ConcurrentIntObjectMap.java
 *
 * Copyright (c) 2013 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.collections.concurrent;

import com.strobel.annotations.NotNull;
import com.strobel.annotations.Nullable;

public interface ConcurrentIntObjectMap<V> {
    @NotNull
    V addOrGet(final int key, @NotNull final V value);

    boolean remove(final int key, @NotNull final V value);
    boolean replace(final int key, @NotNull final V oldValue, @NotNull final V newValue);

    @Nullable
    V put(final int key, @NotNull final V value);
    V putIfAbsent(final int key, @NotNull final V value);

    @Nullable
    V get(final int key);

    @Nullable
    V remove(final int key);

    int size();
    boolean isEmpty();
    boolean contains(final int key);
    void clear();

    @NotNull
    int[] keys();

    @NotNull
    Iterable<IntObjectEntry<V>> entries();
}
