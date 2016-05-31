/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.util;

import com.google.j2objc.annotations.WeakOuter;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;

/*-[
#include "JreRetainedWith.h"
]-*/

/**
 * Hashtable is a synchronized implementation of {@link Map}. All optional operations are supported.
 *
 * <p>Neither keys nor values can be null. (Use {@code HashMap} or {@code LinkedHashMap} if you
 * need null keys or values.)
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @see HashMap
 */
public class Hashtable<K, V> extends Dictionary<K, V>
        implements Map<K, V>, Cloneable, Serializable {
    /**
     * Min capacity (other than zero) for a Hashtable. Must be a power of two
     * greater than 1 (and less than 1 << 30).
     */
    private static final int MINIMUM_CAPACITY = 4;

    /**
     * Max capacity for a Hashtable. Must be a power of two >= MINIMUM_CAPACITY.
     */
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * An empty table shared by all zero-capacity maps (typically from default
     * constructor). It is never written to, and replaced on first put. Its size
     * is set to half the minimum, so that the first resize will create a
     * minimum-sized table.
     */
    private static final Entry[] EMPTY_TABLE
            = new HashtableEntry[MINIMUM_CAPACITY >>> 1];

    /**
     * The default load factor. Note that this implementation ignores the
     * load factor, but cannot do away with it entirely because it's
     * mentioned in the API.
     *
     * <p>Note that this constant has no impact on the behavior of the program,
     * but it is emitted as part of the serialized form. The load factor of
     * .75 is hardwired into the program, which uses cheap shifts in place of
     * expensive division.
     */
    private static final float DEFAULT_LOAD_FACTOR = .75F;

    /**
     * The hash table.
     */
    private transient HashtableEntry<K, V>[] table;

    /**
     * The number of mappings in this hash map.
     */
    private transient int size;

    /**
     * Incremented by "structural modifications" to allow (best effort)
     * detection of concurrent modification.
     */
    private transient int modCount;

    /**
     * The table is rehashed when its size exceeds this threshold.
     * The value of this field is generally .75 * capacity, except when
     * the capacity is zero, as described in the EMPTY_TABLE declaration
     * above.
     */
    private transient int threshold;

    // Views - lazily initialized
    private transient Set<K> keySet;
    private transient Set<Entry<K, V>> entrySet;
    private transient Collection<V> values;

    /**
     * Constructs a new {@code Hashtable} using the default capacity and load
     * factor.
     */
    @SuppressWarnings("unchecked")
    public Hashtable() {
        table = (HashtableEntry<K, V>[]) EMPTY_TABLE;
        threshold = -1; // Forces first put invocation to replace EMPTY_TABLE
    }

    /**
     * Constructs a new {@code Hashtable} using the specified capacity and the
     * default load factor.
     *
     * @param capacity
     *            the initial capacity.
     */
    public Hashtable(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity: " + capacity);
        }

        if (capacity == 0) {
            @SuppressWarnings("unchecked")
            HashtableEntry<K, V>[] tab = (HashtableEntry<K, V>[]) EMPTY_TABLE;
            table = tab;
            threshold = -1; // Forces first put() to replace EMPTY_TABLE
            return;
        }

        if (capacity < MINIMUM_CAPACITY) {
            capacity = MINIMUM_CAPACITY;
        } else if (capacity > MAXIMUM_CAPACITY) {
            capacity = MAXIMUM_CAPACITY;
        } else {
            capacity = Collections.roundUpToPowerOfTwo(capacity);
        }
        makeTable(capacity);
    }

    /**
     * Constructs a new {@code Hashtable} using the specified capacity and load
     * factor.
     *
     * @param capacity
     *            the initial capacity.
     * @param loadFactor
     *            the initial load factor.
     */
    public Hashtable(int capacity, float loadFactor) {
        this(capacity);

        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Load factor: " + loadFactor);
        }

        /*
         * Note that this implementation ignores loadFactor; it always uses
         * a load factor of 3/4. This simplifies the code and generally
         * improves performance.
         */
    }

    /**
     * Constructs a new instance of {@code Hashtable} containing the mappings
     * from the specified map.
     *
     * @param map
     *            the mappings to add.
     */
    public Hashtable(Map<? extends K, ? extends V> map) {
        this(capacityForInitSize(map.size()));
        constructorPutAll(map);
    }

    /**
     * Inserts all of the elements of map into this Hashtable in a manner
     * suitable for use by constructors and pseudo-constructors (i.e., clone,
     * readObject).
     */
    private void constructorPutAll(Map<? extends K, ? extends V> map) {
        if (table == EMPTY_TABLE) {
            doubleCapacity(); // Don't do unchecked puts to a shared table.
        }
        for (Entry<? extends K, ? extends V> e : map.entrySet()) {
            constructorPut(e.getKey(), e.getValue());
        }
    }

    /**
     * Returns an appropriate capacity for the specified initial size. Does
     * not round the result up to a power of two; the caller must do this!
     * The returned value will be between 0 and MAXIMUM_CAPACITY (inclusive).
     */
    private static int capacityForInitSize(int size) {
        int result = (size >> 1) + size; // Multiply by 3/2 to allow for growth

        // boolean expr is equivalent to result >= 0 && result<MAXIMUM_CAPACITY
        return (result & ~(MAXIMUM_CAPACITY-1))==0 ? result : MAXIMUM_CAPACITY;
    }

    /**
     * Returns a new {@code Hashtable} with the same key/value pairs, capacity
     * and load factor.
     *
     * @return a shallow copy of this {@code Hashtable}.
     * @see java.lang.Cloneable
     */
    @SuppressWarnings("unchecked")
    @Override public synchronized Object clone() {
        /*
         * This could be made more efficient. It unnecessarily hashes all of
         * the elements in the map.
         */
        Hashtable<K, V> result;
        try {
            result = (Hashtable<K, V>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }

        // Restore clone to empty state, retaining our capacity and threshold
        result.makeTable(table.length);
        result.size = 0;
        result.keySet = null;
        result.entrySet = null;
        result.values = null;

        result.constructorPutAll(this);
        return result;
    }

    /**
     * Returns true if this {@code Hashtable} has no key/value pairs.
     *
     * @return {@code true} if this {@code Hashtable} has no key/value pairs,
     *         {@code false} otherwise.
     * @see #size
     */
    public synchronized boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the number of key/value pairs in this {@code Hashtable}.
     *
     * @return the number of key/value pairs in this {@code Hashtable}.
     * @see #elements
     * @see #keys
     */
    public synchronized int size() {
        return size;
    }

    /**
     * Returns the value associated with the specified key in this
     * {@code Hashtable}.
     *
     * @param key
     *            the key of the value returned.
     * @return the value associated with the specified key, or {@code null} if
     *         the specified key does not exist.
     * @see #put
     */
    public synchronized V get(Object key) {
        int hash = Collections.secondaryHash(key);
        HashtableEntry<K, V>[] tab = table;
        for (HashtableEntry<K, V> e = tab[hash & (tab.length - 1)];
                e != null; e = e.next) {
            K eKey = e.key;
            if (eKey == key || (e.hash == hash && key.equals(eKey))) {
                return e.value;
            }
        }
        return null;
    }

    /**
     * Returns true if this {@code Hashtable} contains the specified object as a
     * key of one of the key/value pairs.
     *
     * @param key
     *            the object to look for as a key in this {@code Hashtable}.
     * @return {@code true} if object is a key in this {@code Hashtable},
     *         {@code false} otherwise.
     * @see #contains
     * @see java.lang.Object#equals
     */
    public synchronized boolean containsKey(Object key) {
        int hash = Collections.secondaryHash(key);
        HashtableEntry<K, V>[] tab = table;
        for (HashtableEntry<K, V> e = tab[hash & (tab.length - 1)];
                e != null; e = e.next) {
            K eKey = e.key;
            if (eKey == key || (e.hash == hash && key.equals(eKey))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Searches this {@code Hashtable} for the specified value.
     *
     * @param value
     *            the object to search for.
     * @return {@code true} if {@code value} is a value of this
     *         {@code Hashtable}, {@code false} otherwise.
     */
    public synchronized boolean containsValue(Object value) {
        if (value == null) {
            throw new NullPointerException("value == null");
        }

        HashtableEntry[] tab = table;
        int len = tab.length;

        for (int i = 0; i < len; i++) {
            for (HashtableEntry e = tab[i]; e != null; e = e.next) {
                if (value.equals(e.value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if this {@code Hashtable} contains the specified object as
     * the value of at least one of the key/value pairs.
     *
     * @param value
     *            the object to look for as a value in this {@code Hashtable}.
     * @return {@code true} if object is a value in this {@code Hashtable},
     *         {@code false} otherwise.
     * @see #containsKey
     * @see java.lang.Object#equals
     */
    public boolean contains(Object value) {
        return containsValue(value);
    }

    /**
     * Associate the specified value with the specified key in this
     * {@code Hashtable}. If the key already exists, the old value is replaced.
     * The key and value cannot be null.
     *
     * @param key
     *            the key to add.
     * @param value
     *            the value to add.
     * @return the old value associated with the specified key, or {@code null}
     *         if the key did not exist.
     * @see #elements
     * @see #get
     * @see #keys
     * @see java.lang.Object#equals
     */
    public synchronized V put(K key, V value) {
        if (key == null) {
            throw new NullPointerException("key == null");
        } else if (value == null) {
            throw new NullPointerException("value == null");
        }
        int hash = Collections.secondaryHash(key);
        HashtableEntry<K, V>[] tab = table;
        int index = hash & (tab.length - 1);
        HashtableEntry<K, V> first = tab[index];
        for (HashtableEntry<K, V> e = first; e != null; e = e.next) {
            if (e.hash == hash && key.equals(e.key)) {
                V oldValue = e.value;
                e.value = value;
                return oldValue;
            }
        }

        // No entry for key is present; create one
        modCount++;
        if (size++ > threshold) {
            rehash();  // Does nothing!!
            tab = doubleCapacity();
            index = hash & (tab.length - 1);
            first = tab[index];
        }
        tab[index] = new HashtableEntry<K, V>(key, value, hash, first);
        return null;
    }

    /**
     * This method is just like put, except that it doesn't do things that
     * are inappropriate or unnecessary for constructors and pseudo-constructors
     * (i.e., clone, readObject). In particular, this method does not check to
     * ensure that capacity is sufficient, and does not increment modCount.
     */
    private void constructorPut(K key, V value) {
        if (key == null) {
            throw new NullPointerException("key == null");
        } else if (value == null) {
            throw new NullPointerException("value == null");
        }
        int hash = Collections.secondaryHash(key);
        HashtableEntry<K, V>[] tab = table;
        int index = hash & (tab.length - 1);
        HashtableEntry<K, V> first = tab[index];
        for (HashtableEntry<K, V> e = first; e != null; e = e.next) {
            if (e.hash == hash && key.equals(e.key)) {
                e.value = value;
                return;
            }
        }

        // No entry for key is present; create one
        tab[index] = new HashtableEntry<K, V>(key, value, hash, first);
        size++;
    }

    /**
     * Copies every mapping to this {@code Hashtable} from the specified map.
     *
     * @param map
     *            the map to copy mappings from.
     */
    public synchronized void putAll(Map<? extends K, ? extends V> map) {
        ensureCapacity(map.size());
        for (Entry<? extends K, ? extends V> e : map.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    /**
     * Ensures that the hash table has sufficient capacity to store the
     * specified number of mappings, with room to grow. If not, it increases the
     * capacity as appropriate. Like doubleCapacity, this method moves existing
     * entries to new buckets as appropriate. Unlike doubleCapacity, this method
     * can grow the table by factors of 2^n for n > 1. Hopefully, a single call
     * to this method will be faster than multiple calls to doubleCapacity.
     *
     *  <p>This method is called only by putAll.
     */
    private void ensureCapacity(int numMappings) {
        int newCapacity = Collections.roundUpToPowerOfTwo(capacityForInitSize(numMappings));
        HashtableEntry<K, V>[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (newCapacity <= oldCapacity) {
            return;
        }

        rehash();  // Does nothing!!

        if (newCapacity == oldCapacity * 2) {
            doubleCapacity();
            return;
        }

        // We're growing by at least 4x, rehash in the obvious way
        HashtableEntry<K, V>[] newTable = makeTable(newCapacity);
        if (size != 0) {
            int newMask = newCapacity - 1;
            for (int i = 0; i < oldCapacity; i++) {
                for (HashtableEntry<K, V> e = oldTable[i]; e != null;) {
                    HashtableEntry<K, V> oldNext = e.next;
                    int newIndex = e.hash & newMask;
                    HashtableEntry<K, V> newNext = newTable[newIndex];
                    newTable[newIndex] = e;
                    e.next = newNext;
                    e = oldNext;
                }
            }
        }
    }

    /**
     * Increases the capacity of this {@code Hashtable}. This method is called
     * when the size of this {@code Hashtable} exceeds the load factor.
     */
    protected void rehash() {
        /*
         * This method has no testable semantics, other than that it gets
         * called from time to time.
         */
    }

    /**
     * Allocate a table of the given capacity and set the threshold accordingly.
     * @param newCapacity must be a power of two
     */
    private HashtableEntry<K, V>[] makeTable(int newCapacity) {
        @SuppressWarnings("unchecked") HashtableEntry<K, V>[] newTable
                = (HashtableEntry<K, V>[]) new HashtableEntry[newCapacity];
        table = newTable;
        threshold = (newCapacity >> 1) + (newCapacity >> 2); // 3/4 capacity
        return newTable;
    }

    /**
     * Doubles the capacity of the hash table. Existing entries are placed in
     * the correct bucket on the enlarged table. If the current capacity is,
     * MAXIMUM_CAPACITY, this method is a no-op. Returns the table, which
     * will be new unless we were already at MAXIMUM_CAPACITY.
     */
    private HashtableEntry<K, V>[] doubleCapacity() {
        HashtableEntry<K, V>[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            return oldTable;
        }
        int newCapacity = oldCapacity * 2;
        HashtableEntry<K, V>[] newTable = makeTable(newCapacity);
        if (size == 0) {
            return newTable;
        }

        for (int j = 0; j < oldCapacity; j++) {
            /*
             * Rehash the bucket using the minimum number of field writes.
             * This is the most subtle and delicate code in the class.
             */
            HashtableEntry<K, V> e = oldTable[j];
            if (e == null) {
                continue;
            }
            int highBit = e.hash & oldCapacity;
            HashtableEntry<K, V> broken = null;
            newTable[j | highBit] = e;
            for (HashtableEntry<K,V> n = e.next; n != null; e = n, n = n.next) {
                int nextHighBit = n.hash & oldCapacity;
                if (nextHighBit != highBit) {
                    if (broken == null)
                        newTable[j | nextHighBit] = n;
                    else
                        broken.next = n;
                    broken = e;
                    highBit = nextHighBit;
                }
            }
            if (broken != null)
                broken.next = null;
        }
        return newTable;
    }

    /**
     * Removes the key/value pair with the specified key from this
     * {@code Hashtable}.
     *
     * @param key
     *            the key to remove.
     * @return the value associated with the specified key, or {@code null} if
     *         the specified key did not exist.
     * @see #get
     * @see #put
     */
    public synchronized V remove(Object key) {
        int hash = Collections.secondaryHash(key);
        HashtableEntry<K, V>[] tab = table;
        int index = hash & (tab.length - 1);
        for (HashtableEntry<K, V> e = tab[index], prev = null;
                e != null; prev = e, e = e.next) {
            if (e.hash == hash && key.equals(e.key)) {
                if (prev == null) {
                    tab[index] = e.next;
                } else {
                    prev.next = e.next;
                }
                modCount++;
                size--;
                return e.value;
            }
        }
        return null;
    }

    /**
     * Removes all key/value pairs from this {@code Hashtable}, leaving the
     * size zero and the capacity unchanged.
     *
     * @see #isEmpty
     * @see #size
     */
    public synchronized void clear() {
        if (size != 0) {
            Arrays.fill(table, null);
            modCount++;
            size = 0;
        }
    }

    /**
     * Returns a set of the keys contained in this {@code Hashtable}. The set
     * is backed by this {@code Hashtable} so changes to one are reflected by
     * the other. The set does not support adding.
     *
     * @return a set of the keys.
     */
    public synchronized Set<K> keySet() {
        Set<K> ks = keySet;
        return (ks != null) ? ks : (keySet = new KeySet());
    }

    /**
     * Returns a collection of the values contained in this {@code Hashtable}.
     * The collection is backed by this {@code Hashtable} so changes to one are
     * reflected by the other. The collection does not support adding.
     *
     * @return a collection of the values.
     */
    public synchronized Collection<V> values() {
        Collection<V> vs = values;
        return (vs != null) ? vs : (values = new Values());
    }

    /**
     * Returns a set of the mappings contained in this {@code Hashtable}. Each
     * element in the set is a {@link Map.Entry}. The set is backed by this
     * {@code Hashtable} so changes to one are reflected by the other. The set
     * does not support adding.
     *
     * @return a set of the mappings.
     */
    public synchronized Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> es = entrySet;
        return (es != null) ? es : (entrySet = new EntrySet());
    }


    /**
     * Returns an enumeration on the keys of this {@code Hashtable} instance.
     * The results of the enumeration may be affected if the contents of this
     * {@code Hashtable} are modified.
     *
     * @return an enumeration of the keys of this {@code Hashtable}.
     * @see #elements
     * @see #size
     * @see Enumeration
     */
    public synchronized Enumeration<K> keys() {
        return new KeyEnumeration();
    }

    /**
     * Returns an enumeration on the values of this {@code Hashtable}. The
     * results of the Enumeration may be affected if the contents of this
     * {@code Hashtable} are modified.
     *
     * @return an enumeration of the values of this {@code Hashtable}.
     * @see #keys
     * @see #size
     * @see Enumeration
     */
    public synchronized Enumeration<V> elements() {
        return new ValueEnumeration();
    }

    /**
     * Note: technically the methods of this class should synchronize the
     * backing map.  However, this would require them to have a reference
     * to it, which would cause considerable bloat.  Moreover, the RI
     * behaves the same way.
     */
    private static class HashtableEntry<K, V> implements Entry<K, V> {
        final K key;
        V value;
        final int hash;
        HashtableEntry<K, V> next;

        HashtableEntry(K key, V value, int hash, HashtableEntry<K, V> next) {
            this.key = key;
            this.value = value;
            this.hash = hash;
            this.next = next;
        }

        public final K getKey() {
            return key;
        }

        public final V getValue() {
            return value;
        }

        public final V setValue(V value) {
            if (value == null) {
                throw new NullPointerException("value == null");
            }
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        @Override public final boolean equals(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry<?, ?> e = (Entry<?, ?>) o;
            return key.equals(e.getKey()) && value.equals(e.getValue());
        }

        @Override public final int hashCode() {
            return key.hashCode() ^ value.hashCode();
        }

        @Override public final String toString() {
            return key + "=" + value;
        }
    }

    private abstract class HashIterator {
        int nextIndex;
        HashtableEntry<K, V> nextEntry;
        HashtableEntry<K, V> lastEntryReturned;
        int expectedModCount = modCount;

        HashIterator() {
            HashtableEntry<K, V>[] tab = table;
            HashtableEntry<K, V> next = null;
            while (next == null && nextIndex < tab.length) {
                next = tab[nextIndex++];
            }
            nextEntry = next;
        }

        public boolean hasNext() {
            return nextEntry != null;
        }

        HashtableEntry<K, V> nextEntry() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (nextEntry == null)
                throw new NoSuchElementException();

            HashtableEntry<K, V> entryToReturn = nextEntry;
            HashtableEntry<K, V>[] tab = table;
            HashtableEntry<K, V> next = entryToReturn.next;
            while (next == null && nextIndex < tab.length) {
                next = tab[nextIndex++];
            }
            nextEntry = next;
            return lastEntryReturned = entryToReturn;
        }

        HashtableEntry<K, V> nextEntryNotFailFast() {
            if (nextEntry == null)
                throw new NoSuchElementException();

            HashtableEntry<K, V> entryToReturn = nextEntry;
            HashtableEntry<K, V>[] tab = table;
            HashtableEntry<K, V> next = entryToReturn.next;
            while (next == null && nextIndex < tab.length) {
                next = tab[nextIndex++];
            }
            nextEntry = next;
            return lastEntryReturned = entryToReturn;
        }

        public void remove() {
            if (lastEntryReturned == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            Hashtable.this.remove(lastEntryReturned.key);
            lastEntryReturned = null;
            expectedModCount = modCount;
        }
    }

    private final class KeyIterator extends HashIterator
            implements Iterator<K> {
        public K next() { return nextEntry().key; }
    }

    private final class ValueIterator extends HashIterator
            implements Iterator<V> {
        public V next() { return nextEntry().value; }
    }

    private final class EntryIterator extends HashIterator
            implements Iterator<Entry<K, V>> {
        public Entry<K, V> next() { return nextEntry(); }
    }

    private final class KeyEnumeration extends HashIterator
            implements Enumeration<K> {
        public boolean hasMoreElements() { return hasNext(); }
        public K nextElement() { return nextEntryNotFailFast().key; }
    }

    private final class ValueEnumeration extends HashIterator
            implements Enumeration<V> {
        public boolean hasMoreElements() { return hasNext(); }
        public V nextElement() { return nextEntryNotFailFast().value; }
    }

    /**
     * Returns true if this map contains the specified mapping.
     */
    private synchronized boolean containsMapping(Object key, Object value) {
        int hash = Collections.secondaryHash(key);
        HashtableEntry<K, V>[] tab = table;
        int index = hash & (tab.length - 1);
        for (HashtableEntry<K, V> e = tab[index]; e != null; e = e.next) {
            if (e.hash == hash && e.key.equals(key)) {
                return e.value.equals(value);
            }
        }
        return false; // No entry for key
    }

    /**
     * Removes the mapping from key to value and returns true if this mapping
     * exists; otherwise, returns does nothing and returns false.
     */
    private synchronized boolean removeMapping(Object key, Object value) {
        int hash = Collections.secondaryHash(key);
        HashtableEntry<K, V>[] tab = table;
        int index = hash & (tab.length - 1);
        for (HashtableEntry<K, V> e = tab[index], prev = null;
                e != null; prev = e, e = e.next) {
            if (e.hash == hash && e.key.equals(key)) {
                if (!e.value.equals(value)) {
                    return false;  // Map has wrong value for key
                }
                if (prev == null) {
                    tab[index] = e.next;
                } else {
                    prev.next = e.next;
                }
                modCount++;
                size--;
                return true;
            }
        }
        return false; // No entry for key
    }

    /**
     * Compares this {@code Hashtable} with the specified object and indicates
     * if they are equal. In order to be equal, {@code object} must be an
     * instance of Map and contain the same key/value pairs.
     *
     * @param object
     *            the object to compare with this object.
     * @return {@code true} if the specified object is equal to this Map,
     *         {@code false} otherwise.
     * @see #hashCode
     */
    @Override public synchronized boolean equals(Object object) {
        return (object instanceof Map) &&
                entrySet().equals(((Map<?, ?>)object).entrySet());
    }

    @Override public synchronized int hashCode() {
        int result = 0;
        for (Entry<K, V> e : entrySet()) {
            K key = e.getKey();
            V value = e.getValue();
            if (key == this || value == this) {
                continue;
            }
            result += (key != null ? key.hashCode() : 0)
                    ^ (value != null ? value.hashCode() : 0);
        }
        return result;
    }

    /**
     * A rough estimate of the number of characters per entry, for use
     * when creating a string buffer in the toString method.
     */
    private static final int CHARS_PER_ENTRY = 15;

    /**
     * Returns the string representation of this {@code Hashtable}.
     *
     * @return the string representation of this {@code Hashtable}.
     */
    @Override public synchronized String toString() {
        StringBuilder result = new StringBuilder(CHARS_PER_ENTRY * size);
        result.append('{');
        Iterator<Entry<K, V>> i = entrySet().iterator();
        boolean hasMore = i.hasNext();
        while (hasMore) {
            Entry<K, V> entry = i.next();

            K key = entry.getKey();
            result.append(key == this ? "(this Map)" : key.toString());

            result.append('=');

            V value = entry.getValue();
            result.append(value == this ? "(this Map)" : value.toString());

            if (hasMore = i.hasNext()) {
                result.append(", ");
            }
        }

        result.append('}');
        return result.toString();
    }

    @WeakOuter
    private final class KeySet extends AbstractSet<K> {
        public Iterator<K> iterator() {
            return new KeyIterator();
        }
        public int size() {
            return Hashtable.this.size();
        }
        public boolean contains(Object o) {
            return containsKey(o);
        }
        public boolean remove(Object o) {
            synchronized (Hashtable.this) {
                int oldSize = size;
                Hashtable.this.remove(o);
                return size != oldSize;
            }
        }
        public void clear() {
            Hashtable.this.clear();
        }
        public boolean removeAll(Collection<?> collection) {
            synchronized (Hashtable.this) {
                return super.removeAll(collection);
            }
        }
        public boolean retainAll(Collection<?> collection) {
            synchronized (Hashtable.this) {
                return super.retainAll(collection);
            }
        }
        public boolean containsAll(Collection<?> collection) {
            synchronized (Hashtable.this) {
                return super.containsAll(collection);
            }
        }
        public boolean equals(Object object) {
            synchronized (Hashtable.this) {
                return super.equals(object);
            }
        }
        public int hashCode() {
            synchronized (Hashtable.this) {
                return super.hashCode();
            }
        }
        public String toString() {
            synchronized (Hashtable.this) {
                return super.toString();
            }
        }
        public Object[] toArray() {
            synchronized (Hashtable.this) {
                return super.toArray();
            }
        }
        public <T> T[] toArray(T[] a) {
            synchronized (Hashtable.this) {
                return super.toArray(a);
            }
        }

        /*-[ RETAINED_WITH_CHILD(this$0_) ]-*/
    }

    @WeakOuter
    private final class Values extends AbstractCollection<V> {
        public Iterator<V> iterator() {
            return new ValueIterator();
        }
        public int size() {
            return Hashtable.this.size();
        }
        public boolean contains(Object o) {
            return containsValue(o);
        }
        public void clear() {
            Hashtable.this.clear();
        }
        public boolean containsAll(Collection<?> collection) {
            synchronized (Hashtable.this) {
                return super.containsAll(collection);
            }
        }
        public String toString() {
            synchronized (Hashtable.this) {
                return super.toString();
            }
        }
        public Object[] toArray() {
            synchronized (Hashtable.this) {
                return super.toArray();
            }
        }
        public <T> T[] toArray(T[] a) {
            synchronized (Hashtable.this) {
                return super.toArray(a);
            }
        }

        /*-[ RETAINED_WITH_CHILD(this$0_) ]-*/
    }

    @WeakOuter
    private final class EntrySet extends AbstractSet<Entry<K, V>> {
        public Iterator<Entry<K, V>> iterator() {
            return new EntryIterator();
        }
        public boolean contains(Object o) {
            if (!(o instanceof Entry))
                return false;
            Entry<?, ?> e = (Entry<?, ?>) o;
            return containsMapping(e.getKey(), e.getValue());
        }
        public boolean remove(Object o) {
            if (!(o instanceof Entry))
                return false;
            Entry<?, ?> e = (Entry<?, ?>)o;
            return removeMapping(e.getKey(), e.getValue());
        }
        public int size() {
            return Hashtable.this.size();
        }
        public void clear() {
            Hashtable.this.clear();
        }
        public boolean removeAll(Collection<?> collection) {
            synchronized (Hashtable.this) {
                return super.removeAll(collection);
            }
        }
        public boolean retainAll(Collection<?> collection) {
            synchronized (Hashtable.this) {
                return super.retainAll(collection);
            }
        }
        public boolean containsAll(Collection<?> collection) {
            synchronized (Hashtable.this) {
                return super.containsAll(collection);
            }
        }
        public boolean equals(Object object) {
            synchronized (Hashtable.this) {
                return super.equals(object);
            }
        }
        public int hashCode() {
            return Hashtable.this.hashCode();
        }
        public String toString() {
            synchronized (Hashtable.this) {
                return super.toString();
            }
        }
        public Object[] toArray() {
            synchronized (Hashtable.this) {
                return super.toArray();
            }
        }
        public <T> T[] toArray(T[] a) {
            synchronized (Hashtable.this) {
                return super.toArray(a);
            }
        }

        /*-[ RETAINED_WITH_CHILD(this$0_) ]-*/
    }

    private static final long serialVersionUID = 1421746759512286392L;

    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("threshold", int.class),
        new ObjectStreamField("loadFactor", float.class),
    };

    private synchronized void writeObject(ObjectOutputStream stream)
            throws IOException {
        // Emulate loadFactor field for other implementations to read
        ObjectOutputStream.PutField fields = stream.putFields();
        fields.put("threshold",  (int) (DEFAULT_LOAD_FACTOR * table.length));
        fields.put("loadFactor", DEFAULT_LOAD_FACTOR);
        stream.writeFields();

        stream.writeInt(table.length); // Capacity
        stream.writeInt(size);
        for (Entry<K, V> e : entrySet()) {
            stream.writeObject(e.getKey());
            stream.writeObject(e.getValue());
        }
    }

    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();
        int capacity = stream.readInt();
        if (capacity < 0) {
            throw new InvalidObjectException("Capacity: " + capacity);
        }
        if (capacity < MINIMUM_CAPACITY) {
            capacity = MINIMUM_CAPACITY;
        } else if (capacity > MAXIMUM_CAPACITY) {
            capacity = MAXIMUM_CAPACITY;
        } else {
            capacity = Collections.roundUpToPowerOfTwo(capacity);
        }
        makeTable(capacity);

        int size = stream.readInt();
        if (size < 0) {
            throw new InvalidObjectException("Size: " + size);
        }

        for (int i = 0; i < size; i++) {
            @SuppressWarnings("unchecked") K key = (K) stream.readObject();
            @SuppressWarnings("unchecked") V val = (V) stream.readObject();
            constructorPut(key, val);
        }
    }
}
