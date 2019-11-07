/*
 * ConcurrentIntObjectHashMap.java
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
import com.strobel.concurrent.StripedReentrantLock;
import com.strobel.core.VerifyArgument;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;

@SuppressWarnings({ "unchecked", "ProtectedField" })
public class ConcurrentIntObjectHashMap<V> implements ConcurrentIntObjectMap<V> {
    protected static final int DEFAULT_INITIAL_CAPACITY = 16;
    protected static final int MAXIMUM_CAPACITY = 1 << 30;
    protected static final float DEFAULT_LOAD_FACTOR = 0.75f;

    public ConcurrentIntObjectHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public ConcurrentIntObjectHashMap(final int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public ConcurrentIntObjectHashMap(final int initialCapacity, final float loadFactor) {
        final int capacity = computeInitialCapacity(initialCapacity, loadFactor);
        setTable(new IntHashEntry<?>[capacity]);
        _loadFactor = loadFactor;
    }

    // <editor-fold defaultstate="collapsed" desc="Lock Management">

    private static final StripedReentrantLock STRIPED_REENTRANT_LOCK = StripedReentrantLock.instance();

    private final byte _lockIndex = (byte)STRIPED_REENTRANT_LOCK.allocateLockIndex();

    private void lock() {
        STRIPED_REENTRANT_LOCK.lock(_lockIndex);
    }

    private void unlock() {
        STRIPED_REENTRANT_LOCK.unlock(_lockIndex);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Internal State">

    protected volatile IntHashEntry<V>[] table;
    protected volatile int count;
    protected int modCount;

    private final float _loadFactor;

    private int threshold() {
        return (int)(table.length * _loadFactor);
    }

    private void setTable(final IntHashEntry<?>[] newTable) {
        table = (IntHashEntry<V>[])newTable;
    }

    private static int computeInitialCapacity(final int initialCapacity, final float loadFactor) {
        VerifyArgument.isNonNegative(initialCapacity, "initialCapacity");
        VerifyArgument.isPositive(loadFactor, "loadFactor");

        final int desiredCapacity = Math.min(initialCapacity, MAXIMUM_CAPACITY);

        int capacity = 1;

        while (capacity < desiredCapacity) {
            capacity <<= 1;
        }

        return capacity;
    }

    private IntHashEntry<V> getFirst(final int hash) {
        final IntHashEntry<?>[] t = table;
        return (IntHashEntry<V>)t[hash & (t.length - 1)];
    }

    /**
     * Read the value of an entry under lock.  Called if the value field appears
     * to be {@code null}.
     */
    private V readValueUnderLock(final IntHashEntry<V> entry) {
        lock();

        try {
            return entry.value;
        }
        finally {
            unlock();
        }
    }

    private void rehash() {
        final IntHashEntry<?>[] oldTable = table;
        final int oldCapacity = oldTable.length;

        if (oldCapacity >= MAXIMUM_CAPACITY) {
            return;
        }

        final int newCapacity = oldCapacity << 1;
        final IntHashEntry<V>[] newTable = (IntHashEntry<V>[])new IntHashEntry<?>[newCapacity];
        final int sizeMask = newCapacity - 1;

        for (final IntHashEntry oldEntry : oldTable) {
            if (oldEntry == null) {
                continue;
            }

            final IntHashEntry<V> next = oldEntry.next;
            final int index = oldEntry.key & sizeMask;

            if (next == null) {
                //
                // Single node on list.
                //
                newTable[index] = oldEntry;
            }
            else {
                //
                // Reuse trailing consecutive elements at same slot.
                //

                IntHashEntry<V> lastRun = oldEntry;
                int lastIndex = index;

                for (IntHashEntry<V> last = next;
                     last != null;
                     last = last.next) {

                    final int k = last.key & sizeMask;

                    if (k != lastIndex) {
                        lastIndex = k;
                        lastRun = last;
                    }
                }

                newTable[lastIndex] = lastRun;

                for (IntHashEntry<V> p = oldEntry; p != lastRun; p = p.next) {
                    final int currentIndex = p.key & sizeMask;
                    final IntHashEntry<V> current = newTable[currentIndex];

                    newTable[currentIndex] = new IntHashEntry<>(p.key, current, p.value);
                }
            }
        }

        setTable(newTable);
    }

    protected V put(final int key, @NotNull final V value, final boolean onlyIfAbsent) {
        lock();

        try {
            int c = count;

            if (c++ > threshold()) {
                rehash();
            }

            final IntHashEntry<V>[] t = table;
            final int index = key & (table.length - 1);
            final IntHashEntry<V> first = t[index];

            IntHashEntry<V> entry = first;

            while (entry != null && entry.key != key) {
                entry = entry.next;
            }

            final V oldValue;

            if (entry != null) {
                oldValue = entry.value;

                if (!onlyIfAbsent) {
                    entry.value = value;
                }
            }
            else {
                oldValue = null;
                ++modCount;
                t[index] = new IntHashEntry(key, first, value);
                count = c;
            }

            return oldValue;
        }
        finally {
            unlock();
        }
    }

    protected V removeCore(final int key, @Nullable final V value) {
        lock();

        try {
            final int newCount = count - 1;
            final IntHashEntry<V>[] t = table;
            final int index = key & (table.length - 1);

            IntHashEntry<V> entry = t[index];

            while (entry != null && entry.key != key) {
                entry = entry.next;
            }

            if (entry != null) {
                final V oldValue = entry.value;

                if (value == null || value.equals(oldValue)) {
                    ++modCount;

                    IntHashEntry<V> newFirst = t[index];

                    for (IntHashEntry<V> p = newFirst; p != entry; p = p.next) {
                        newFirst = new IntHashEntry<>(p.key, newFirst, p.value);
                    }

                    t[index] = newFirst;
                    count = newCount;

                    return oldValue;
                }
            }

            return null;
        }
        finally {
            unlock();
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ConcurrentIntObjectMap Implementation">

    @NotNull
    @Override
    public V addOrGet(final int key, @NotNull final V value) {
        final V previous = putIfAbsent(key, value);

        return previous != null ? previous
                                : value;
    }

    @Override
    public boolean remove(final int key, @NotNull final V value) {
        return removeCore(key, value) != null;
    }

    @Override
    public boolean replace(final int key, @NotNull final V oldValue, @NotNull final V newValue) {
        VerifyArgument.notNull(oldValue, "oldValue");
        VerifyArgument.notNull(newValue, "newValue");

        lock();

        try {
            IntHashEntry<V> entry = getFirst(key);

            while (entry != null && entry.key != key) {
                entry = entry.next;
            }

            if (entry != null && oldValue.equals(entry.value)) {
                entry.value = newValue;
                return true;
            }

            return false;
        }
        finally {
            unlock();
        }
    }

    @Override
    public V put(final int key, @NotNull final V value) {
        return put(key, value, false);
    }

    @Override
    public V putIfAbsent(final int key, @NotNull final V value) {
        return put(key, value, true);
    }

    @Override
    public V get(final int key) {
        if (count != 0) {   // read-volatile
            IntHashEntry<V> entry = getFirst(key);

            while (entry != null) {
                if (entry.key == key) {
                    final V value = entry.value;

                    return value != null ? value
                                         : readValueUnderLock(entry);
                }
                entry = entry.next;
            }
        }
        return null;
    }

    @Override
    public V remove(final int key) {
        return removeCore(key, null);
    }

    @Override
    public int size() {
        return count;
    }

    @Override
    public boolean isEmpty() {
        return count == 0;
    }

    @Override
    public boolean contains(final int key) {
        if (count != 0) {   // read-volatile
            IntHashEntry<V> entry = getFirst(key);

            while (entry != null) {
                if (entry.key == key) {
                    return true;
                }
                entry = entry.next;
            }
        }
        return false;
    }

    @Override
    public void clear() {
        if (count != 0) {   // read-volatile
            lock();

            try {
                final IntHashEntry<?>[] t = table;

                for (int i = 0; i < t.length; i++) {
                    t[i] = null;
                }

                ++modCount;
                count = 0;
            }
            finally {
                unlock();
            }
        }
    }

    @NotNull
    @Override
    public int[] keys() {
        final IntHashEntry<?>[] t = table;
        final int c = Math.min(count, t.length);

        int[] keys = new int[c];
        int k = 0;

        for (int i = 0; i < t.length; i++, k++) {
            if (k >= keys.length) {
                keys = Arrays.copyOf(keys, keys.length * 2);
            }
            keys[k] = t[i].key;
        }

        if (k < keys.length) {
            return Arrays.copyOfRange(keys, 0, k);
        }

        return keys;
    }

    @NotNull
    @Override
    public Iterable<IntObjectEntry<V>> entries() {
        return new Iterable<IntObjectEntry<V>>() {
            @Override
            public Iterator<IntObjectEntry<V>> iterator() {
                return new Iterator<IntObjectEntry<V>>() {
                    private final HashIterator hashIterator = new HashIterator();

                    @Override
                    public final boolean hasNext() {
                        return hashIterator.hasNext();
                    }

                    @Override
                    public final IntObjectEntry<V> next() {
                        final IntHashEntry<V> e = hashIterator.nextEntry();
                        return new SimpleEntry<>(e.key, e.value);
                    }

                    @Override
                    public final void remove() {
                        hashIterator.remove();
                    }
                };
            }
        };
    }

    @NotNull
    public Iterable<V> elements() {
        return new Iterable<V>() {
            @Override
            public Iterator<V> iterator() {
                return new ValueIterator();
            }
        };
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Iterators">

    private class HashIterator {
        private int _nextTableIndex = table.length - 1;
        private IntHashEntry<V> _nextEntry;
        private IntHashEntry<V> _lastReturned;

        private HashIterator() {
            advance();
        }

        private void advance() {
            if (_nextEntry != null && (_nextEntry = _nextEntry.next) != null) {
                return;
            }

            while (_nextTableIndex >= 0) {
                if ((_nextEntry = table[_nextTableIndex--]) != null) {
                    return;
                }
            }
        }

        public final boolean hasMoreElements() {
            return _nextEntry != null;
        }

        public final boolean hasNext() {
            return _nextEntry != null;
        }

        protected final IntHashEntry<V> nextEntry() {
            if (_nextEntry == null) {
                throw new IllegalStateException();
            }

            _lastReturned = _nextEntry;
            advance();
            return _lastReturned;
        }

        public final void remove() {
            if (_lastReturned == null) {
                throw new IllegalStateException();
            }
            ConcurrentIntObjectHashMap.this.remove(_lastReturned.key);
            _lastReturned = null;
        }
    }

    private final class ValueIterator extends HashIterator implements Iterator<V>, Enumeration<V> {
        @Override
        public V nextElement() {
            return nextEntry().value;
        }

        @Override
        public V next() {
            return nextEntry().value;
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="SimpleEntry Class">

    private final static class SimpleEntry<V> implements IntObjectEntry<V> {
        private final int _key;
        private final V _value;

        private SimpleEntry(final int key, final V value) {
            _key = key;
            _value = value;
        }

        public final int key() {
            return _key;
        }

        @NotNull
        public final V value() {
            return _value;
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="IntHashEntry Class">

    private final static class IntHashEntry<V> {
        final int key;
        final IntHashEntry<V> next;

        @NotNull
        volatile V value;

        private IntHashEntry(final int key, final IntHashEntry<V> next, @NotNull final V value) {
            this.key = key;
            this.next = next;
            this.value = value;
        }
    }

    // </editor-fold>
}
