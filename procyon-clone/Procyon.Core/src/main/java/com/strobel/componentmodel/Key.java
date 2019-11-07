/*
 * Key.java
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

package com.strobel.componentmodel;

import com.strobel.annotations.NotNull;
import com.strobel.annotations.Nullable;
import com.strobel.collections.concurrent.ConcurrentWeakIntObjectHashMap;
import com.strobel.core.VerifyArgument;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Key<T> {
    private final static AtomicInteger _keyCounter = new AtomicInteger();
    private final static ConcurrentWeakIntObjectHashMap<Key<?>> _allKeys = new ConcurrentWeakIntObjectHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> Key<T> getKeyByIndex(final int index) {
        return (Key<T>)_allKeys.get(index);
    }

    public static <T> Key<T> create(@NotNull final String name) {
        return new Key<>(name);
    }

    private final int _index = _keyCounter.getAndIncrement();

    @NotNull
    private final String _name;

    public Key(@NotNull final String name) {
        _name = VerifyArgument.notNull(name, "name");
    }

    @Override
    public final int hashCode() {
        return _index;
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public final boolean equals(final Object obj) {
        return obj == this;
    }

    @Override
    public String toString() {
        return "Key(" + _name + ")";
    }

    @Nullable
    public T get(@Nullable final UserDataStore store) {
        return store == null ? null
                             : store.getUserData(this);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public T get(@Nullable final Map<Key<?>, ?> store) {
        return store == null ? null
                             : (T)store.get(this);
    }

    @Nullable
    public T get(@Nullable final UserDataStore store, @Nullable final T defaultValue) {
        final T value = get(store);
        return value != null ? value
                             : defaultValue;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public T get(@Nullable final Map<Key<?>, ?> store, @Nullable final T defaultValue) {
        final T value = get(store);
        return value != null ? value
                             : defaultValue;
    }

    public boolean isPresent(@Nullable final UserDataStore store) {
        return get(store) != null;
    }

    public void set(@Nullable final UserDataStore store, @Nullable final T value) {
        if (store != null) {
            store.putUserData(this, value);
        }
    }

    public void set(@Nullable final Map<Key<?>, Object> store, @Nullable final T value) {
        if (store != null) {
            store.put(this, value);
        }
    }
}
