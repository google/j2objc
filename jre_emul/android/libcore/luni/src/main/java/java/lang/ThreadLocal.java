/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.lang;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implements a thread-local storage, that is, a variable for which each thread
 * has its own value. All threads share the same {@code ThreadLocal} object,
 * but each sees a different value when accessing it, and changes made by one
 * thread do not affect the other threads. The implementation supports
 * {@code null} values.
 *
 * @see java.lang.Thread
 * @author Bob Lee
 */
public class ThreadLocal<T> {

    /* Thanks to Josh Bloch and Doug Lea for code reviews and impl advice. */

    /**
     * Creates a new thread-local variable.
     */
    public ThreadLocal() {}

    /**
     * Returns the value of this variable for the current thread. If an entry
     * doesn't yet exist for this variable on this thread, this method will
     * create an entry, populating the value with the result of
     * {@link #initialValue()}.
     *
     * @return the current value of the variable for the calling thread.
     */
    @SuppressWarnings("unchecked")
    public T get() {
        // Optimized for the fast path.
        Thread currentThread = Thread.currentThread();
        Values values = values(currentThread);
        if (values != null) {
            Object[] table = values.table;
            int index = hash & values.mask;
            if (this.reference == table[index]) {
                return (T) table[index + 1];
            }
        } else {
            values = initializeValues(currentThread);
        }

        return (T) values.getAfterMiss(this);
    }

    /**
     * Provides the initial value of this variable for the current thread.
     * The default implementation returns {@code null}.
     *
     * @return the initial value of the variable.
     */
    protected T initialValue() {
        return null;
    }

    /**
     * Sets the value of this variable for the current thread. If set to
     * {@code null}, the value will be set to null and the underlying entry will
     * still be present.
     *
     * @param value the new value of the variable for the caller thread.
     */
    public void set(T value) {
        Thread currentThread = Thread.currentThread();
        Values values = values(currentThread);
        if (values == null) {
            values = initializeValues(currentThread);
        }
        values.put(this, value);
    }

    /**
     * Removes the entry for this variable in the current thread. If this call
     * is followed by a {@link #get()} before a {@link #set},
     * {@code #get()} will call {@link #initialValue()} and create a new
     * entry with the resulting value.
     *
     * @since 1.5
     */
    public void remove() {
        Thread currentThread = Thread.currentThread();
        Values values = values(currentThread);
        if (values != null) {
            values.remove(this);
        }
    }

    /**
     * Creates Values instance for this thread and variable type.
     */
    Values initializeValues(Thread current) {
        return current.localValues = new Values();
    }

    /**
     * Gets Values instance for this thread and variable type.
     */
    Values values(Thread current) {
        return current.localValues;
    }

    /** Weak reference to this thread local instance. */
    private final Reference<ThreadLocal<T>> reference
            = new WeakReference<ThreadLocal<T>>(this);

    /** Hash counter. */
    private static AtomicInteger hashCounter = new AtomicInteger(0);

    /**
     * Internal hash. We deliberately don't bother with #hashCode().
     * Hashes must be even. This ensures that the result of
     * (hash & (table.length - 1)) points to a key and not a value.
     *
     * We increment by Doug Lea's Magic Number(TM) (*2 since keys are in
     * every other bucket) to help prevent clustering.
     */
    private final int hash = hashCounter.getAndAdd(0x61c88647 * 2);

    /**
     * Per-thread map of ThreadLocal instances to values.
     */
    static class Values {

        /**
         * Size must always be a power of 2.
         */
        private static final int INITIAL_SIZE = 16;

        /**
         * Placeholder for deleted entries.
         */
        private static final Object TOMBSTONE = new Object();

        /**
         * Map entries. Contains alternating keys (ThreadLocal) and values.
         * The length is always a power of 2.
         */
        private Object[] table;

        /** Used to turn hashes into indices. */
        private int mask;

        /** Number of live entries. */
        private int size;

        /** Number of tombstones. */
        private int tombstones;

        /** Maximum number of live entries and tombstones. */
        private int maximumLoad;

        /** Points to the next cell to clean up. */
        private int clean;

        /**
         * Constructs a new, empty instance.
         */
        Values() {
            initializeTable(INITIAL_SIZE);
            this.size = 0;
            this.tombstones = 0;
        }

        /**
         * Used for InheritableThreadLocals.
         */
        Values(Values fromParent) {
            this.table = fromParent.table.clone();
            this.mask = fromParent.mask;
            this.size = fromParent.size;
            this.tombstones = fromParent.tombstones;
            this.maximumLoad = fromParent.maximumLoad;
            this.clean = fromParent.clean;
            inheritValues(fromParent);
        }

        /**
         * Inherits values from a parent thread.
         */
        @SuppressWarnings({"unchecked"})
        private void inheritValues(Values fromParent) {
            // Transfer values from parent to child thread.
            Object[] table = this.table;
            for (int i = table.length - 2; i >= 0; i -= 2) {
                Object k = table[i];

                if (k == null || k == TOMBSTONE) {
                    // Skip this entry.
                    continue;
                }

                // The table can only contain null, tombstones and references.
                Reference<InheritableThreadLocal<?>> reference
                        = (Reference<InheritableThreadLocal<?>>) k;
                // Raw type enables us to pass in an Object below.
                InheritableThreadLocal key = reference.get();
                if (key != null) {
                    // Replace value with filtered value.
                    // We should just let exceptions bubble out and tank
                    // the thread creation
                    table[i + 1] = key.childValue(fromParent.table[i + 1]);
                } else {
                    // The key was reclaimed.
                    table[i] = TOMBSTONE;
                    table[i + 1] = null;
                    fromParent.table[i] = TOMBSTONE;
                    fromParent.table[i + 1] = null;

                    tombstones++;
                    fromParent.tombstones++;

                    size--;
                    fromParent.size--;
                }
            }
        }

        /**
         * Creates a new, empty table with the given capacity.
         */
        private void initializeTable(int capacity) {
            this.table = new Object[capacity * 2];
            this.mask = table.length - 1;
            this.clean = 0;
            this.maximumLoad = capacity * 2 / 3; // 2/3
        }

        /**
         * Cleans up after garbage-collected thread locals.
         */
        private void cleanUp() {
            if (rehash()) {
                // If we rehashed, we needn't clean up (clean up happens as
                // a side effect).
                return;
            }

            if (size == 0) {
                // No live entries == nothing to clean.
                return;
            }

            // Clean log(table.length) entries picking up where we left off
            // last time.
            int index = clean;
            Object[] table = this.table;
            for (int counter = table.length; counter > 0; counter >>= 1,
                    index = next(index)) {
                Object k = table[index];

                if (k == TOMBSTONE || k == null) {
                    continue; // on to next entry
                }

                // The table can only contain null, tombstones and references.
                @SuppressWarnings("unchecked")
                Reference<ThreadLocal<?>> reference
                        = (Reference<ThreadLocal<?>>) k;
                if (reference.get() == null) {
                    // This thread local was reclaimed by the garbage collector.
                    table[index] = TOMBSTONE;
                    table[index + 1] = null;
                    tombstones++;
                    size--;
                }
            }

            // Point cursor to next index.
            clean = index;
        }

        /**
         * Rehashes the table, expanding or contracting it as necessary.
         * Gets rid of tombstones. Returns true if a rehash occurred.
         * We must rehash every time we fill a null slot; we depend on the
         * presence of null slots to end searches (otherwise, we'll infinitely
         * loop).
         */
        private boolean rehash() {
            if (tombstones + size < maximumLoad) {
                return false;
            }

            int capacity = table.length >> 1;

            // Default to the same capacity. This will create a table of the
            // same size and move over the live entries, analogous to a
            // garbage collection. This should only happen if you churn a
            // bunch of thread local garbage (removing and reinserting
            // the same thread locals over and over will overwrite tombstones
            // and not fill up the table).
            int newCapacity = capacity;

            if (size > (capacity >> 1)) {
                // More than 1/2 filled w/ live entries.
                // Double size.
                newCapacity = capacity * 2;
            }

            Object[] oldTable = this.table;

            // Allocate new table.
            initializeTable(newCapacity);

            // We won't have any tombstones after this.
            this.tombstones = 0;

            // If we have no live entries, we can quit here.
            if (size == 0) {
                return true;
            }

            // Move over entries.
            for (int i = oldTable.length - 2; i >= 0; i -= 2) {
                Object k = oldTable[i];
                if (k == null || k == TOMBSTONE) {
                    // Skip this entry.
                    continue;
                }

                // The table can only contain null, tombstones and references.
                @SuppressWarnings("unchecked")
                Reference<ThreadLocal<?>> reference
                        = (Reference<ThreadLocal<?>>) k;
                ThreadLocal<?> key = reference.get();
                if (key != null) {
                    // Entry is still live. Move it over.
                    add(key, oldTable[i + 1]);
                } else {
                    // The key was reclaimed.
                    size--;
                }
            }

            return true;
        }

        /**
         * Adds an entry during rehashing. Compared to put(), this method
         * doesn't have to clean up, check for existing entries, account for
         * tombstones, etc.
         */
        void add(ThreadLocal<?> key, Object value) {
            for (int index = key.hash & mask;; index = next(index)) {
                Object k = table[index];
                if (k == null) {
                    table[index] = key.reference;
                    table[index + 1] = value;
                    return;
                }
            }
        }

        /**
         * Sets entry for given ThreadLocal to given value, creating an
         * entry if necessary.
         */
        void put(ThreadLocal<?> key, Object value) {
            cleanUp();

            // Keep track of first tombstone. That's where we want to go back
            // and add an entry if necessary.
            int firstTombstone = -1;

            for (int index = key.hash & mask;; index = next(index)) {
                Object k = table[index];

                if (k == key.reference) {
                    // Replace existing entry.
                    table[index + 1] = value;
                    return;
                }

                if (k == null) {
                    if (firstTombstone == -1) {
                        // Fill in null slot.
                        table[index] = key.reference;
                        table[index + 1] = value;
                        size++;
                        return;
                    }

                    // Go back and replace first tombstone.
                    table[firstTombstone] = key.reference;
                    table[firstTombstone + 1] = value;
                    tombstones--;
                    size++;
                    return;
                }

                // Remember first tombstone.
                if (firstTombstone == -1 && k == TOMBSTONE) {
                    firstTombstone = index;
                }
            }
        }

        /**
         * Gets value for given ThreadLocal after not finding it in the first
         * slot.
         */
        Object getAfterMiss(ThreadLocal<?> key) {
            Object[] table = this.table;
            int index = key.hash & mask;

            // If the first slot is empty, the search is over.
            if (table[index] == null) {
                Object value = key.initialValue();

                // If the table is still the same and the slot is still empty...
                if (this.table == table && table[index] == null) {
                    table[index] = key.reference;
                    table[index + 1] = value;
                    size++;

                    cleanUp();
                    return value;
                }

                // The table changed during initialValue().
                put(key, value);
                return value;
            }

            // Keep track of first tombstone. That's where we want to go back
            // and add an entry if necessary.
            int firstTombstone = -1;

            // Continue search.
            for (index = next(index);; index = next(index)) {
                Object reference = table[index];
                if (reference == key.reference) {
                    return table[index + 1];
                }

                // If no entry was found...
                if (reference == null) {
                    Object value = key.initialValue();

                    // If the table is still the same...
                    if (this.table == table) {
                        // If we passed a tombstone and that slot still
                        // contains a tombstone...
                        if (firstTombstone > -1
                                && table[firstTombstone] == TOMBSTONE) {
                            table[firstTombstone] = key.reference;
                            table[firstTombstone + 1] = value;
                            tombstones--;
                            size++;

                            // No need to clean up here. We aren't filling
                            // in a null slot.
                            return value;
                        }

                        // If this slot is still empty...
                        if (table[index] == null) {
                            table[index] = key.reference;
                            table[index + 1] = value;
                            size++;

                            cleanUp();
                            return value;
                        }
                    }

                    // The table changed during initialValue().
                    put(key, value);
                    return value;
                }

                if (firstTombstone == -1 && reference == TOMBSTONE) {
                    // Keep track of this tombstone so we can overwrite it.
                    firstTombstone = index;
                }
            }
        }

        /**
         * Removes entry for the given ThreadLocal.
         */
        void remove(ThreadLocal<?> key) {
            cleanUp();

            for (int index = key.hash & mask;; index = next(index)) {
                Object reference = table[index];

                if (reference == key.reference) {
                    // Success!
                    table[index] = TOMBSTONE;
                    table[index + 1] = null;
                    tombstones++;
                    size--;
                    return;
                }

                if (reference == null) {
                    // No entry found.
                    return;
                }
            }
        }

        /**
         * Gets the next index. If we're at the end of the table, we wrap back
         * around to 0.
         */
        private int next(int index) {
            return (index + 2) & mask;
        }
    }
}
