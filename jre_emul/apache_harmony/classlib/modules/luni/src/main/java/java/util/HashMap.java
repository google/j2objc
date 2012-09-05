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

import java.io.Serializable;

/**
 * HashMap is an implementation of Map. All optional operations (adding and
 * removing) are supported. Keys and values can be any objects.
 */
public class HashMap<K, V> extends AbstractMap<K, V> implements Map<K, V>,
        Cloneable, Serializable {

    private static final long serialVersionUID = 362498820763181265L;

    /*
     * Actual count of entries
     */
    transient int elementCount;

    /*
     * The internal data structure to hold Entries
     */
    transient Entry<K, V>[] elementData;

    /*
     * modification count, to keep track of structural modifications between the
     * HashMap and the iterator
     */
    transient int modCount = 0;

    /*
     * default size that an HashMap created using the default constructor would
     * have.
     */
    private static final int DEFAULT_SIZE = 16;

    /*
     * maximum ratio of (stored elements)/(storage size) which does not lead to
     * rehash
     */
    final float loadFactor;

    /*
     * maximum number of elements that can be put in this map before having to
     * rehash
     */
    int threshold;

    static class Entry<K, V> extends MapEntry<K, V> {
        final int origKeyHash;

        Entry<K, V> next;

        Entry(K theKey, int hash) {
            super(theKey, null);
            this.origKeyHash = hash;
        }

        Entry(K theKey, V theValue) {
            super(theKey, theValue);
            origKeyHash = (theKey == null ? 0 : computeHashCode(theKey));
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object clone() {
            Entry<K, V> entry = (Entry<K, V>) super.clone();
            if (next != null) {
                entry.next = (Entry<K, V>) next.clone();
            }
            return entry;
        }
    }

    private static class AbstractMapIterator<K, V>  {
        private int position = 0;
        int expectedModCount;
        Entry<K, V> futureEntry;
        Entry<K, V> currentEntry;
        Entry<K, V> prevEntry;

        final HashMap<K, V> associatedMap;

        AbstractMapIterator(HashMap<K, V> hm) {
            associatedMap = hm;
            expectedModCount = hm.modCount;
            futureEntry = null;
        }

        public boolean hasNext() {
            if (futureEntry != null) {
                return true;
            }
            while (position < associatedMap.elementData.length) {
                if (associatedMap.elementData[position] == null) {
                    position++;
                } else {
                    return true;
                }
            }
            return false;
        }

        final void checkConcurrentMod() throws ConcurrentModificationException {
            if (expectedModCount != associatedMap.modCount) {
                throw new ConcurrentModificationException();
            }
        }

        final void makeNext() {
            checkConcurrentMod();
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            if (futureEntry == null) {
                currentEntry = associatedMap.elementData[position++];
                futureEntry = currentEntry.next;
                prevEntry = null;
            } else {
                if(currentEntry!=null){
                    prevEntry = currentEntry;
                }
                currentEntry = futureEntry;
                futureEntry = futureEntry.next;
            }
        }

        public final void remove() {
            checkConcurrentMod();
            if (currentEntry==null) {
                throw new IllegalStateException();
            }
            if(prevEntry==null){
                int index = currentEntry.origKeyHash & (associatedMap.elementData.length - 1);
                associatedMap.elementData[index] = ((Entry<K, V>) associatedMap.elementData[index]).next;
            } else {
                prevEntry.next = currentEntry.next;
            }
            currentEntry = null;
            expectedModCount++;
            associatedMap.modCount++;
            associatedMap.elementCount--;

        }
    }


    private static class EntryIterator <K, V> extends AbstractMapIterator<K, V> implements Iterator<Map.Entry<K, V>> {

        EntryIterator (HashMap<K, V> map) {
            super(map);
        }

        public Map.Entry<K, V> next() {
            makeNext();
            return currentEntry;
        }
    }

    private static class KeyIterator <K, V> extends AbstractMapIterator<K, V> implements Iterator<K> {

        KeyIterator (HashMap<K, V> map) {
            super(map);
        }

        public K next() {
            makeNext();
            return currentEntry.key;
        }
    }

    private static class ValueIterator <K, V> extends AbstractMapIterator<K, V> implements Iterator<V> {

        ValueIterator (HashMap<K, V> map) {
            super(map);
        }

        public V next() {
            makeNext();
            return currentEntry.value;
        }
    }

    static class HashMapEntrySet<KT, VT> extends AbstractSet<Map.Entry<KT, VT>> {
        private final HashMap<KT, VT> associatedMap;

        public HashMapEntrySet(HashMap<KT, VT> hm) {
            associatedMap = hm;
        }

        HashMap<KT, VT> hashMap() {
            return associatedMap;
        }

        @Override
        public int size() {
            return associatedMap.elementCount;
        }

        @Override
        public void clear() {
            associatedMap.clear();
        }

        @Override
        public boolean remove(Object object) {
            if (object instanceof Map.Entry) {
                Map.Entry<?, ?> oEntry = (Map.Entry<?, ?>) object;
                Entry<KT,VT> entry = associatedMap.getEntry(oEntry.getKey());
                if(valuesEq(entry, oEntry)) {
                    associatedMap.removeEntry(entry);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean contains(Object object) {
            if (object instanceof Map.Entry) {
                Map.Entry<?, ?> oEntry = (Map.Entry<?, ?>) object;
                Entry<KT, VT> entry = associatedMap.getEntry(oEntry.getKey());
                return valuesEq(entry, oEntry);
            }
            return false;
        }

        private static boolean valuesEq(Entry entry, Map.Entry<?, ?> oEntry) {
            return (entry != null) &&
                                   ((entry.value == null) ?
                                    (oEntry.getValue() == null) :
                                    (areEqualValues(entry.value, oEntry.getValue())));
        }

        @Override
        public Iterator<Map.Entry<KT, VT>> iterator() {
            return new EntryIterator<KT,VT> (associatedMap);
        }
    }

    /**
     * Create a new element array
     *
     * @param s
     * @return Reference to the element array
     */
    @SuppressWarnings("unchecked")
    Entry<K, V>[] newElementArray(int s) {
        return new Entry[s];
    }

    /**
     * Constructs a new empty {@code HashMap} instance.
     */
    public HashMap() {
        this(DEFAULT_SIZE);
    }

    /**
     * Constructs a new {@code HashMap} instance with the specified capacity.
     *
     * @param capacity
     *            the initial capacity of this hash map.
     * @throws IllegalArgumentException
     *                when the capacity is less than zero.
     */
    public HashMap(int capacity) {
        this(capacity, 0.75f);  // default load factor of 0.75
        }

    /**
     * Calculates the capacity of storage required for storing given number of
     * elements
     * 
     * @param x
     *            number of elements
     * @return storage size
     */
    private static final int calculateCapacity(int x) {
        if(x >= 1 << 30){
            return 1 << 30;
        }
        if(x == 0){
            return 16;
        }
        x = x -1;
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        x |= x >> 16;
        return x + 1;
    }

    /**
     * Constructs a new {@code HashMap} instance with the specified capacity and
     * load factor.
     *
     * @param capacity
     *            the initial capacity of this hash map.
     * @param loadFactor
     *            the initial load factor.
     * @throws IllegalArgumentException
     *                when the capacity is less than zero or the load factor is
     *                less or equal to zero.
     */
    public HashMap(int capacity, float loadFactor) {
        if (capacity >= 0 && loadFactor > 0) {
            capacity = calculateCapacity(capacity);
            elementCount = 0;
            elementData = newElementArray(capacity);
            this.loadFactor = loadFactor;
            computeThreshold();
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Constructs a new {@code HashMap} instance containing the mappings from
     * the specified map.
     *
     * @param map
     *            the mappings to add.
     */
    public HashMap(Map<? extends K, ? extends V> map) {
        this(calculateCapacity(map.size()));
        putAllImpl(map);
    }

    /**
     * Removes all mappings from this hash map, leaving it empty.
     *
     * @see #isEmpty
     * @see #size
     */
    @Override
    public void clear() {
        if (elementCount > 0) {
            elementCount = 0;
            Arrays.fill(elementData, null);
            modCount++;
        }
    }

    /**
     * Returns a shallow copy of this map.
     *
     * @return a shallow copy of this map.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object clone() {
        try {
            HashMap<K, V> map = (HashMap<K, V>) super.clone();
            map.elementCount = 0;
            map.elementData = newElementArray(elementData.length);
            map.putAll(this);
            
            return map;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Computes the threshold for rehashing
     */
    private void computeThreshold() {
        threshold = (int) (elementData.length * loadFactor);
    }

    /**
     * Returns whether this map contains the specified key.
     *
     * @param key
     *            the key to search for.
     * @return {@code true} if this map contains the specified key,
     *         {@code false} otherwise.
     */
    @Override
    public boolean containsKey(Object key) {
        Entry<K, V> m = getEntry(key);
        return m != null;
    }

    /**
     * Returns whether this map contains the specified value.
     *
     * @param value
     *            the value to search for.
     * @return {@code true} if this map contains the specified value,
     *         {@code false} otherwise.
     */
    @Override
    public boolean containsValue(Object value) {
        if (value != null) {
            for (int i = 0; i < elementData.length; i++) {
                Entry<K, V> entry = elementData[i];
                while (entry != null) {
                    if (areEqualValues(value, entry.value)) {
                        return true;
                    }
                    entry = entry.next;
                }
            }
        } else {
            for (int i = 0; i < elementData.length; i++) {
                Entry<K, V> entry = elementData[i];
                while (entry != null) {
                    if (entry.value == null) {
                        return true;
                    }
                    entry = entry.next;
                }
            }
        }
        return false;
    }

    /**
     * Returns a set containing all of the mappings in this map. Each mapping is
     * an instance of {@link Map.Entry}. As the set is backed by this map,
     * changes in one will be reflected in the other.
     *
     * @return a set of the mappings.
     */
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return new HashMapEntrySet<K, V>(this);
    }

    /**
     * Returns the value of the mapping with the specified key.
     *
     * @param key
     *            the key.
     * @return the value of the mapping with the specified key, or {@code null}
     *         if no mapping for the specified key is found.
     */
    @Override
    public V get(Object key) {
        Entry<K, V> m = getEntry(key);
        if (m != null) {
            return m.value;
        }
        return null;
    }

    final Entry<K, V> getEntry(Object key) {
        Entry<K, V> m;
        if (key == null) {
            m = findNullKeyEntry();
        } else {
            int hash = computeHashCode(key);
            int index = hash & (elementData.length - 1);
            m = findNonNullKeyEntry(key, index, hash);
        }
        return m;
    }

    final Entry<K,V> findNonNullKeyEntry(Object key, int index, int keyHash) {
        Entry<K,V> m = elementData[index];
        while (m != null
                && (m.origKeyHash != keyHash || !areEqualKeys(key, m.key))) {
            m = m.next;
        }
        return m;
    }

    final Entry<K,V> findNullKeyEntry() {
        Entry<K,V> m = elementData[0];
        while (m != null && m.key != null)
            m = m.next;
        return m;
    }

    /**
     * Returns whether this map is empty.
     *
     * @return {@code true} if this map has no elements, {@code false}
     *         otherwise.
     * @see #size()
     */
    @Override
    public boolean isEmpty() {
        return elementCount == 0;
    }

    /**
     * Returns a set of the keys contained in this map. The set is backed by
     * this map so changes to one are reflected by the other. The set does not
     * support adding.
     *
     * @return a set of the keys.
     */
    @Override
    public Set<K> keySet() {
        if (keySet == null) {
            keySet = new AbstractSet<K>() {
                @Override
                public boolean contains(Object object) {
                    return containsKey(object);
                }

                @Override
                public int size() {
                    return HashMap.this.size();
                }

                @Override
                public void clear() {
                    HashMap.this.clear();
                }

                @Override
                public boolean remove(Object key) {
                    Entry<K, V> entry = HashMap.this.removeEntry(key);
                    return entry != null;
                }

                @Override
                public Iterator<K> iterator() {
                    return new KeyIterator<K,V> (HashMap.this);
                }
            };
        }
        return keySet;
    }

    /**
     * Maps the specified key to the specified value.
     *
     * @param key
     *            the key.
     * @param value
     *            the value.
     * @return the value of any previous mapping with the specified key or
     *         {@code null} if there was no such mapping.
     */
    @Override
    public V put(K key, V value) {
        return putImpl(key, value);
    }

    V putImpl(K key, V value) {
        Entry<K,V> entry;
        if(key == null) {
            entry = findNullKeyEntry();
            if (entry == null) {
                modCount++;
                entry = createHashedEntry(null, 0, 0);
                if (++elementCount > threshold) {
                    rehash();
                }
            }
        } else {
            int hash = computeHashCode(key);
            int index = hash & (elementData.length - 1);
            entry = findNonNullKeyEntry(key, index, hash);
            if (entry == null) {
                modCount++;
                entry = createHashedEntry(key, index, hash);
                if (++elementCount > threshold) {
                    rehash();
                }
            }
        }

        V result = entry.value;
        entry.value = value;
        return result;
    }

    Entry<K, V> createEntry(K key, int index, V value) {
        Entry<K, V> entry = new Entry<K, V>(key, value);
        entry.next = elementData[index];
        elementData[index] = entry;
        return entry;
    }

    Entry<K,V> createHashedEntry(K key, int index, int hash) {
        Entry<K,V> entry = new Entry<K,V>(key,hash);
        entry.next = elementData[index];
        elementData[index] = entry;
        return entry;
    }

    /**
     * Copies all the mappings in the specified map to this map. These mappings
     * will replace all mappings that this map had for any of the keys currently
     * in the given map.
     *
     * @param map
     *            the map to copy mappings from.
     * @throws NullPointerException
     *             if {@code map} is {@code null}.
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        if (!map.isEmpty()) {
            putAllImpl(map);
        }
    }

    private void putAllImpl(Map<? extends K, ? extends V> map) {
        int capacity = elementCount + map.size();
        if (capacity > threshold) {
            rehash(capacity);
        }
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            putImpl(entry.getKey(), entry.getValue());
        }
    }

    void rehash(int capacity) {
        int length = calculateCapacity((capacity == 0 ? 1 : capacity << 1));

        Entry<K, V>[] newData = newElementArray(length);
        for (int i = 0; i < elementData.length; i++) {
            Entry<K, V> entry = elementData[i];
            elementData[i] = null;
            while (entry != null) {
                int index = entry.origKeyHash & (length - 1);
                Entry<K, V> next = entry.next;
                entry.next = newData[index];
                newData[index] = entry;
                entry = next;
            }
        }
        elementData = newData;
        computeThreshold();
    }

    void rehash() {
        rehash(elementData.length);
    }

    /**
     * Removes the mapping with the specified key from this map.
     *
     * @param key
     *            the key of the mapping to remove.
     * @return the value of the removed mapping or {@code null} if no mapping
     *         for the specified key was found.
     */
    @Override
    public V remove(Object key) {
        Entry<K, V> entry = removeEntry(key);
        if (entry != null) {
            return entry.value;
        }
        return null;
    }

    /*
     * Remove the given entry from the hashmap.
     * Assumes that the entry is in the map.
     */
    final void removeEntry(Entry<K, V> entry) {
        int index = entry.origKeyHash & (elementData.length - 1);
        Entry<K, V> m = elementData[index];
        if (m == entry) {
            elementData[index] = entry.next;
        } else {
            while (m.next != entry) {
                m = m.next;
            }
            m.next = entry.next;

        }
        modCount++;
        elementCount--;
    }

    final Entry<K, V> removeEntry(Object key) {
        int index = 0;
        Entry<K, V> entry;
        Entry<K, V> last = null;
        if (key != null) {
            int hash = computeHashCode(key);
            index = hash & (elementData.length - 1);
            entry = elementData[index];
            while (entry != null && !(entry.origKeyHash == hash && areEqualKeys(key, entry.key))) {
                last = entry;
                entry = entry.next;
            }
        } else {
            entry = elementData[0];
            while (entry != null && entry.key != null) {
                last = entry;
                entry = entry.next;
            }
        }
        if (entry == null) {
            return null;
        }
        if (last == null) {
            elementData[index] = entry.next;
        } else {
            last.next = entry.next;
        }
        modCount++;
        elementCount--;
        return entry;
    }

    /**
     * Returns the number of elements in this map.
     *
     * @return the number of elements in this map.
     */
    @Override
    public int size() {
        return elementCount;
    }

    /**
     * Returns a collection of the values contained in this map. The collection
     * is backed by this map so changes to one are reflected by the other. The
     * collection supports remove, removeAll, retainAll and clear operations,
     * and it does not support add or addAll operations.
     * <p>
     * This method returns a collection which is the subclass of
     * AbstractCollection. The iterator method of this subclass returns a
     * "wrapper object" over the iterator of map's entrySet(). The {@code size}
     * method wraps the map's size method and the {@code contains} method wraps
     * the map's containsValue method.
     * <p>
     * The collection is created when this method is called for the first time
     * and returned in response to all subsequent calls. This method may return
     * different collections when multiple concurrent calls occur, since no
     * synchronization is performed.
     *
     * @return a collection of the values contained in this map.
     */
    @Override
    public Collection<V> values() {
        if (valuesCollection == null) {
            valuesCollection = new AbstractCollection<V>() {
                @Override
                public boolean contains(Object object) {
                    return containsValue(object);
                }

                @Override
                public int size() {
                    return HashMap.this.size();
                }

                @Override
                public void clear() {
                    HashMap.this.clear();
                }

                @Override
                public Iterator<V> iterator() {
                    return new ValueIterator<K,V> (HashMap.this);
                }
            };
        }
        return valuesCollection;
    }

    /*
     * Contract-related functionality 
     */
    static int computeHashCode(Object key) {
        return key.hashCode();
}

    static boolean areEqualKeys(Object key1, Object key2) {
        return (key1 == key2) || key1.equals(key2);
    }
    
    static boolean areEqualValues(Object value1, Object value2) {
        return (value1 == value2) || value1.equals(value2);
    }
    
    
}
