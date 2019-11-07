/*
 * ConcurrentWeakIntObjectHashMap.java
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
import com.strobel.core.Comparer;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * @author strobelm
 */
public final class ConcurrentWeakIntObjectHashMap<V> extends ConcurrentRefValueIntObjectHashMap<V> {
    @Override
    @SuppressWarnings("unchecked")
    protected final IntReference<V> createReference(final int key, @NotNull final V value, final ReferenceQueue<V> queue) {
        return new ConcurrentWeakIntObjectHashMap.WeakIntReference<>(key, value, queue);
    }

    private final static class WeakIntReference<V> extends WeakReference<V> implements IntReference<V> {
        private final int _hash;
        private final int _key;

        WeakIntReference(final int key, final V referent, final ReferenceQueue<? super V> q) {
            super(referent, q);
            _key = key;
            _hash = referent.hashCode();
        }

        @Override
        public final int key() {
            return _key;
        }

        @Override
        public final int hashCode() {
            return _hash;
        }

        @Override
        @SuppressWarnings("unchecked")
        public final boolean equals(final Object obj) {
            if (!(obj instanceof ConcurrentWeakIntObjectHashMap.WeakIntReference<?>)) {
                return false;
            }

            final ConcurrentWeakIntObjectHashMap.WeakIntReference<V> other = (ConcurrentWeakIntObjectHashMap.WeakIntReference<V>)obj;

            return other._hash == _hash &&
                   Comparer.equals(other.get(), get());
        }
    }
}
