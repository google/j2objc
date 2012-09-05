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
 * LinkedHashSet is a variant of HashSet. Its entries are kept in a
 * doubly-linked list. The iteration order is the order in which entries were
 * inserted.
 * <p>
 * Null elements are allowed, and all the optional Set operations are supported.
 * <p>
 * Like HashSet, LinkedHashSet is not thread safe, so access by multiple threads
 * must be synchronized by an external mechanism such as
 * {@link Collections#synchronizedSet(Set)}.
 *
 * @since 1.4
 */
public class LinkedHashSet<E> extends HashSet<E> implements Set<E>, Cloneable,
        Serializable {

    private static final long serialVersionUID = -2851667679971038690L;

    /**
     * Constructs a new empty instance of {@code LinkedHashSet}.
     */
    public LinkedHashSet() {
        super(new LinkedHashMap<E, HashSet<E>>());
    }

    /**
     * Constructs a new instance of {@code LinkedHashSet} with the specified
     * capacity.
     * 
     * @param capacity
     *            the initial capacity of this {@code LinkedHashSet}.
     */
    public LinkedHashSet(int capacity) {
        super(new LinkedHashMap<E, HashSet<E>>(capacity));
    }

    /**
     * Constructs a new instance of {@code LinkedHashSet} with the specified
     * capacity and load factor.
     * 
     * @param capacity
     *            the initial capacity.
     * @param loadFactor
     *            the initial load factor.
     */
    public LinkedHashSet(int capacity, float loadFactor) {
        super(new LinkedHashMap<E, HashSet<E>>(capacity, loadFactor));
    }

    /**
     * Constructs a new instance of {@code LinkedHashSet} containing the unique
     * elements in the specified collection.
     * 
     * @param collection
     *            the collection of elements to add.
     */
    public LinkedHashSet(Collection<? extends E> collection) {
        super(new LinkedHashMap<E, HashSet<E>>(collection.size() < 6 ? 11
                : collection.size() * 2));
        for (E e : collection) {
            add(e);
        }
    }

    /* overrides method in HashMap */
    @Override
    HashMap<E, HashSet<E>> createBackingMap(int capacity, float loadFactor) {
        return new LinkedHashMap<E, HashSet<E>>(capacity, loadFactor);
    }
}
