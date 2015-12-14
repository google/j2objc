/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements. See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
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

import com.google.j2objc.annotations.Weak;
import com.google.j2objc.annotations.WeakOuter;

/**
 * LinkedHashMap is an implementation of {@link Map} that guarantees iteration order.
 * All optional operations are supported.
 *
 * <p>All elements are permitted as keys or values, including null.
 *
 * <p>Entries are kept in a doubly-linked list. The iteration order is, by default, the
 * order in which keys were inserted. Reinserting an already-present key doesn't change the
 * order. If the three argument constructor is used, and {@code accessOrder} is specified as
 * {@code true}, the iteration will be in the order that entries were accessed.
 * The access order is affected by {@code put}, {@code get}, and {@code putAll} operations,
 * but not by operations on the collection views.
 *
 * <p>Note: the implementation of {@code LinkedHashMap} is not synchronized.
 * If one thread of several threads accessing an instance modifies the map
 * structurally, access to the map needs to be synchronized. For
 * insertion-ordered instances a structural modification is an operation that
 * removes or adds an entry. Access-ordered instances also are structurally
 * modified by {@code put}, {@code get}, and {@code putAll} since these methods
 * change the order of the entries. Changes in the value of an entry are not structural changes.
 *
 * <p>The {@code Iterator} created by calling the {@code iterator} method
 * may throw a {@code ConcurrentModificationException} if the map is structurally
 * changed while an iterator is used to iterate over the elements. Only the
 * {@code remove} method that is provided by the iterator allows for removal of
 * elements during iteration. It is not possible to guarantee that this
 * mechanism works in all cases of unsynchronized concurrent modification. It
 * should only be used for debugging purposes.
 */
public class LinkedHashMap<K, V> extends HashMap<K, V> {

    /**
     * A dummy entry in the circular linked list of entries in the map.
     * The first real entry is header.nxt, and the last is header.prv.
     * If the map is empty, header.nxt == header && header.prv == header.
     */
    transient LinkedEntry<K, V> header;

    /**
     * True if access ordered, false if insertion ordered.
     */
    private final boolean accessOrder;

    /**
     * Constructs a new empty {@code LinkedHashMap} instance.
     */
    public LinkedHashMap() {
        init();
        accessOrder = false;
    }

    /**
     * Constructs a new {@code LinkedHashMap} instance with the specified
     * capacity.
     *
     * @param initialCapacity
     *            the initial capacity of this map.
     * @throws IllegalArgumentException
     *                when the capacity is less than zero.
     */
    public LinkedHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs a new {@code LinkedHashMap} instance with the specified
     * capacity and load factor.
     *
     * @param initialCapacity
     *            the initial capacity of this map.
     * @param loadFactor
     *            the initial load factor.
     * @throws IllegalArgumentException
     *             when the capacity is less than zero or the load factor is
     *             less or equal to zero.
     */
    public LinkedHashMap(int initialCapacity, float loadFactor) {
        this(initialCapacity, loadFactor, false);
    }

    /**
     * Constructs a new {@code LinkedHashMap} instance with the specified
     * capacity, load factor and a flag specifying the ordering behavior.
     *
     * @param initialCapacity
     *            the initial capacity of this hash map.
     * @param loadFactor
     *            the initial load factor.
     * @param accessOrder
     *            {@code true} if the ordering should be done based on the last
     *            access (from least-recently accessed to most-recently
     *            accessed), and {@code false} if the ordering should be the
     *            order in which the entries were inserted.
     * @throws IllegalArgumentException
     *             when the capacity is less than zero or the load factor is
     *             less or equal to zero.
     */
    public LinkedHashMap(
            int initialCapacity, float loadFactor, boolean accessOrder) {
        super(initialCapacity, loadFactor);
        init();
        this.accessOrder = accessOrder;
    }

    /**
     * Constructs a new {@code LinkedHashMap} instance containing the mappings
     * from the specified map. The order of the elements is preserved.
     *
     * @param map
     *            the mappings to add.
     */
    public LinkedHashMap(Map<? extends K, ? extends V> map) {
        this(capacityForInitSize(map.size()));
        constructorPutAll(map);
    }

    @Override void init() {
        header = new LinkedEntry<K, V>();
    }

    /**
     * LinkedEntry adds nxt/prv double-links to plain HashMapEntry.
     */
    static class LinkedEntry<K, V> extends HashMapEntry<K, V> {
        @Weak LinkedEntry<K, V> nxt;
        @Weak LinkedEntry<K, V> prv;

        /** Create the header entry */
        LinkedEntry() {
            super(null, null, 0, null);
            nxt = prv = this;
        }

        /** Create a normal entry */
        LinkedEntry(K key, V value, int hash, HashMapEntry<K, V> next,
                    LinkedEntry<K, V> nxt, LinkedEntry<K, V> prv) {
            super(key, value, hash, next);
            this.nxt = nxt;
            this.prv = prv;
        }
    }

    /**
     * Returns the eldest entry in the map, or {@code null} if the map is empty.
     * @hide
     */
    public Entry<K, V> eldest() {
        LinkedEntry<K, V> eldest = header.nxt;
        return eldest != header ? eldest : null;
    }

    /**
     * Evicts eldest entry if instructed, creates a new entry and links it in
     * as head of linked list. This method should call constructorNewEntry
     * (instead of duplicating code) if the performance of your VM permits.
     *
     * <p>It may seem strange that this method is tasked with adding the entry
     * to the hash table (which is properly the province of our superclass).
     * The alternative of passing the "next" link in to this method and
     * returning the newly created element does not work! If we remove an
     * (eldest) entry that happens to be the first entry in the same bucket
     * as the newly created entry, the "next" link would become invalid, and
     * the resulting hash table corrupt.
     *
     * Modified to avoid extra retain/autorelease calls.
     */
    @Override native void addNewEntry(K key, V value, int hash, int index) /*-[
      JavaUtilLinkedHashMap_LinkedEntry *header = self->header_;
      JavaUtilLinkedHashMap_LinkedEntry *eldest = header->nxt_;
      if (eldest != header && [self removeEldestEntryWithJavaUtilMap_Entry:eldest]) {
        [self removeWithId:eldest->key_];
      }
      JavaUtilLinkedHashMap_LinkedEntry *oldTail = header->prv_;
      JavaUtilLinkedHashMap_LinkedEntry *newTail = [[JavaUtilLinkedHashMap_LinkedEntry alloc]
          initWithId:key withId:value withInt:hash_
          withJavaUtilHashMap_HashMapEntry:nil
          withJavaUtilLinkedHashMap_LinkedEntry:header
          withJavaUtilLinkedHashMap_LinkedEntry:oldTail];
      newTail->next_ = self->table_->buffer_[index];
      self->table_->buffer_[index] = oldTail->nxt_ = header->prv_ = newTail;
    ]-*/;

    @Override native void addNewEntryForNullKey(V value) /*-[
      JavaUtilLinkedHashMap_LinkedEntry *header = self->header_;
      JavaUtilLinkedHashMap_LinkedEntry *eldest = header->nxt_;
      if (eldest != header && [self removeEldestEntryWithJavaUtilMap_Entry:eldest]) {
        [self removeWithId:eldest->key_];
      }
      JavaUtilLinkedHashMap_LinkedEntry *oldTail = header->prv_;
      JavaUtilLinkedHashMap_LinkedEntry *newTail = [[JavaUtilLinkedHashMap_LinkedEntry alloc]
          initWithId:nil withId:value withInt:0 withJavaUtilHashMap_HashMapEntry:nil
          withJavaUtilLinkedHashMap_LinkedEntry:header
          withJavaUtilLinkedHashMap_LinkedEntry:oldTail];
      JavaUtilHashMap_setAndConsume_entryForNullKey_(self, oldTail->nxt_ = header->prv_ = newTail);
    ]-*/;

    /**
     * As above, but without eviction.
     *
     * This native method has a modified contract from the original version.
     * It must return a retained entry object. And it must avoid retaining the
     * "first" parameter when setting it to the "next" field of the new entry.
     */
    @Override native HashMapEntry<K, V> constructorNewRetainedEntry(
            K key, V value, int hash, HashMapEntry<K, V> next) /*-[
      JavaUtilLinkedHashMap_LinkedEntry *header = self->header_;
      JavaUtilLinkedHashMap_LinkedEntry *oldTail = header->prv_;
      JavaUtilLinkedHashMap_LinkedEntry *newTail = [[JavaUtilLinkedHashMap_LinkedEntry alloc]
          initWithId:key withId:value withInt:hash_ withJavaUtilHashMap_HashMapEntry:nil
          withJavaUtilLinkedHashMap_LinkedEntry:header
          withJavaUtilLinkedHashMap_LinkedEntry:oldTail];
      newTail->next_ = next;
      return oldTail->nxt_ = header->prv_ = newTail;
    ]-*/;

    /**
     * Returns the value of the mapping with the specified key.
     *
     * @param key
     *            the key.
     * @return the value of the mapping with the specified key, or {@code null}
     *         if no mapping for the specified key is found.
     */
    @Override public V get(Object key) {
        /*
         * This method is overridden to eliminate the need for a polymorphic
         * invocation in superclass at the expense of code duplication.
         */
        if (key == null) {
            HashMapEntry<K, V> e = entryForNullKey;
            if (e == null)
                return null;
            if (accessOrder)
                makeTail((LinkedEntry<K, V>) e);
            return e.value;
        }

        int hash = Collections.secondaryHash(key);
        HashMapEntry<K, V>[] tab = table;
        for (HashMapEntry<K, V> e = tab[hash & (tab.length - 1)];
                e != null; e = e.next) {
            K eKey = e.key;
            if (eKey == key || (e.hash == hash && key.equals(eKey))) {
                if (accessOrder)
                    makeTail((LinkedEntry<K, V>) e);
                return e.value;
            }
        }
        return null;
    }

    /**
     * Relinks the given entry to the tail of the list. Under access ordering,
     * this method is invoked whenever the value of a  pre-existing entry is
     * read by Map.get or modified by Map.put.
     */
    private void makeTail(LinkedEntry<K, V> e) {
        // Unlink e
        e.prv.nxt = e.nxt;
        e.nxt.prv = e.prv;

        // Relink e as tail
        LinkedEntry<K, V> header = this.header;
        LinkedEntry<K, V> oldTail = header.prv;
        e.nxt = header;
        e.prv = oldTail;
        oldTail.nxt = header.prv = e;
        modCount++;
    }

    @Override void preModify(HashMapEntry<K, V> e) {
        if (accessOrder) {
            makeTail((LinkedEntry<K, V>) e);
        }
    }

    @Override void postRemove(HashMapEntry<K, V> e) {
        LinkedEntry<K, V> le = (LinkedEntry<K, V>) e;
        le.prv.nxt = le.nxt;
        le.nxt.prv = le.prv;
        le.nxt = le.prv = null; // Help the GC (for performance)
    }

    /**
     * This override is done for LinkedHashMap performance: iteration is cheaper
     * via LinkedHashMap nxt links.
     */
    @Override public boolean containsValue(Object value) {
        if (value == null) {
            for (LinkedEntry<K, V> header = this.header, e = header.nxt;
                    e != header; e = e.nxt) {
                if (e.value == null) {
                    return true;
                }
            }
            return false;
        }

        // value is non-null
        for (LinkedEntry<K, V> header = this.header, e = header.nxt;
                e != header; e = e.nxt) {
            if (value.equals(e.value)) {
                return true;
            }
        }
        return false;
    }

    public void clear() {
        super.clear();

        // Clear all links to help GC
        LinkedEntry<K, V> header = this.header;
        for (LinkedEntry<K, V> e = header.nxt; e != header; ) {
            LinkedEntry<K, V> nxt = e.nxt;
            e.nxt = e.prv = null;
            e = nxt;
        }

        header.nxt = header.prv = header;
    }

    @WeakOuter
    private abstract class LinkedHashIterator<T> implements Iterator<T> {
        LinkedEntry<K, V> next = header.nxt;
        LinkedEntry<K, V> lastReturned = null;
        int expectedModCount = modCount;

        public final boolean hasNext() {
            return next != header;
        }

        final LinkedEntry<K, V> nextEntry() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            LinkedEntry<K, V> e = next;
            if (e == header)
                throw new NoSuchElementException();
            next = e.nxt;
            return lastReturned = e;
        }

        public final void remove() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (lastReturned == null)
                throw new IllegalStateException();
            LinkedHashMap.this.remove(lastReturned.key);
            lastReturned = null;
            expectedModCount = modCount;
        }
    }

    private final class KeyIterator extends LinkedHashIterator<K> {
        public final K next() { return nextEntry().key; }
    }

    private final class ValueIterator extends LinkedHashIterator<V> {
        public final V next() { return nextEntry().value; }
    }

    private final class EntryIterator
            extends LinkedHashIterator<Map.Entry<K, V>> {
        public final Map.Entry<K, V> next() { return nextEntry(); }
    }

    // Override view iterator methods to generate correct iteration order
    @Override Iterator<K> newKeyIterator() {
        return new KeyIterator();
    }
    @Override Iterator<V> newValueIterator() {
        return new ValueIterator();
    }
    @Override Iterator<Map.Entry<K, V>> newEntryIterator() {
        return new EntryIterator();
    }

    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return false;
    }

    private static final long serialVersionUID = 3801124242820219131L;

    /*-[
    - (NSUInteger)enumerateEntriesWithState:(NSFastEnumerationState *)state
                                    objects:(__unsafe_unretained id *)stackbuf
                                      count:(NSUInteger)len {
      __unsafe_unretained JavaUtilLinkedHashMap_LinkedEntry *entry;
      if (state->state == 0) {
        state->state = 1;
        state->mutationsPtr = (unsigned long *) &modCount_;
        entry = header_->nxt_;
      } else {
        entry = (JavaUtilLinkedHashMap_LinkedEntry *) state->extra[0];
      }
      state->itemsPtr = stackbuf;
      NSUInteger objCount = 0;
      while (entry != header_ && objCount < len) {
        *stackbuf++ = entry;
        objCount++;
        entry = entry->nxt_;
      }
      state->extra[0] = (unsigned long) entry;
      return objCount;
    }
    ]-*/
}
