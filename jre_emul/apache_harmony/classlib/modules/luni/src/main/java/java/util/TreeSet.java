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
 * TreeSet is an implementation of SortedSet. All optional operations (adding
 * and removing) are supported. The elements can be any objects which are
 * comparable to each other either using their natural order or a specified
 * Comparator.
 * 
 * @since 1.2
 */
public class TreeSet<E> extends AbstractSet<E> implements SortedSet<E>,
        Cloneable, Serializable {

    private static final long serialVersionUID = -2479143000061671589L;

    /** Keys are this set's elements. Values are always Boolean.TRUE */
    private transient SortedMap<E, Object> backingMap;

    private TreeSet(SortedMap<E, Object> map) {
        this.backingMap = map;
    }

    /**
     * Constructs a new empty instance of {@code TreeSet} which uses natural
     * ordering.
     */
    public TreeSet() {
        backingMap = new TreeMap<E, Object>();
    }

    /**
     * Constructs a new instance of {@code TreeSet} which uses natural ordering
     * and containing the unique elements in the specified collection.
     * 
     * @param collection
     *            the collection of elements to add.
     * @throws ClassCastException
     *                when an element in the collection does not implement the
     *                Comparable interface, or the elements in the collection
     *                cannot be compared.
     */
    public TreeSet(Collection<? extends E> collection) {
        this();
        addAll(collection);
    }

    /**
     * Constructs a new empty instance of {@code TreeSet} which uses the
     * specified comparator.
     * 
     * @param comparator
     *            the comparator to use.
     */
    public TreeSet(Comparator<? super E> comparator) {
        backingMap = new TreeMap<E, Object>(comparator);
    }

    /**
     * Constructs a new instance of {@code TreeSet} containing the elements of
     * the specified SortedSet and using the same Comparator.
     * 
     * @param set
     *            the SortedSet of elements to add.
     */
    public TreeSet(SortedSet<E> set) {
        this(set.comparator());
        Iterator<E> it = set.iterator();
        while (it.hasNext()) {
            add(it.next());
        }
    }

    /**
     * Adds the specified object to this {@code TreeSet}.
     * 
     * @param object
     *            the object to add.
     * @return {@code true} when this {@code TreeSet} did not already contain
     *         the object, {@code false} otherwise.
     * @throws ClassCastException
     *             when the object cannot be compared with the elements in this
     *             {@code TreeSet}.
     * @throws NullPointerException
     *             when the object is null and the comparator cannot handle
     *             null.
     */
    @Override
    public boolean add(E object) {
        return backingMap.put(object, Boolean.TRUE) == null;
    }

    /**
     * Adds the objects in the specified collection to this {@code TreeSet}.
     * 
     * @param collection
     *            the collection of objects to add.
     * @return {@code true} if this {@code TreeSet} was modified, {@code false}
     *         otherwise.
     * @throws ClassCastException
     *             when an object in the collection cannot be compared with the
     *             elements in this {@code TreeSet}.
     * @throws NullPointerException
     *             when an object in the collection is null and the comparator
     *             cannot handle null.
     */
    @Override
    public boolean addAll(Collection<? extends E> collection) {
        return super.addAll(collection);
    }

    /**
     * Removes all elements from this {@code TreeSet}, leaving it empty.
     * 
     * @see #isEmpty
     * @see #size
     */
    @Override
    public void clear() {
        backingMap.clear();
    }

    /**
     * Returns a new {@code TreeSet} with the same elements, size and comparator
     * as this {@code TreeSet}.
     * 
     * @return a shallow copy of this {@code TreeSet}.
     * @see java.lang.Cloneable
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        try {
            TreeSet<E> clone = (TreeSet<E>) super.clone();
            if (backingMap instanceof TreeMap) {
                clone.backingMap = (SortedMap<E, Object>) ((TreeMap<E, Object>) backingMap)
                        .clone();
            } else {
                clone.backingMap = new TreeMap<E, Object>(backingMap);
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Returns the comparator used to compare elements in this {@code TreeSet}.
     * 
     * @return a Comparator or null if the natural ordering is used
     */
    public Comparator<? super E> comparator() {
        return backingMap.comparator();
    }

    /**
     * Searches this {@code TreeSet} for the specified object.
     * 
     * @param object
     *            the object to search for.
     * @return {@code true} if {@code object} is an element of this
     *         {@code TreeSet}, {@code false} otherwise.
     * @throws ClassCastException
     *             when the object cannot be compared with the elements in this
     *             {@code TreeSet}.
     * @throws NullPointerException
     *             when the object is null and the comparator cannot handle
     *             null.
     */
    @Override
    public boolean contains(Object object) {
        return backingMap.containsKey(object);
    }

    /**
     * Returns the first element in this {@code TreeSet}.
     * 
     * @return the first element.
     * @throws NoSuchElementException
     *             when this {@code TreeSet} is empty.
     */
    public E first() {
        return backingMap.firstKey();
    }

    /**
     * Returns a SortedSet of the specified portion of this {@code TreeSet}
     * which contains elements which are all less than the end element. The
     * returned SortedSet is backed by this {@code TreeSet} so changes to one
     * are reflected by the other.
     * 
     * @param end
     *            the end element.
     * @return a subset where the elements are less than {@code end}
     * @throws ClassCastException
     *             when the end object cannot be compared with the elements in
     *             this {@code TreeSet}.
     * @throws NullPointerException
     *             when the end object is null and the comparator cannot handle
     *             null.
     */
    @SuppressWarnings("unchecked")
    public SortedSet<E> headSet(E end) {
        // Check for errors
        Comparator<? super E> c = backingMap.comparator();
        if (c == null) {
            ((Comparable<E>) end).compareTo(end);
        } else {
            c.compare(end, end);
        }
        return new TreeSet<E>(backingMap.headMap(end));
    }

    /**
     * Returns true if this {@code TreeSet} has no element, otherwise false.
     * 
     * @return true if this {@code TreeSet} has no element.
     * @see #size
     */
    @Override
    public boolean isEmpty() {
        return backingMap.isEmpty();
    }

    /**
     * Returns an Iterator on the elements of this {@code TreeSet}.
     * 
     * @return an Iterator on the elements of this {@code TreeSet}.
     * @see Iterator
     */
    @Override
    public Iterator<E> iterator() {
        return backingMap.keySet().iterator();
    }

    /**
     * Returns the last element in this {@code TreeSet}. The last element is
     * the highest element.
     * 
     * @return the last element.
     * @throws NoSuchElementException
     *             when this {@code TreeSet} is empty.
     */
    public E last() {
        return backingMap.lastKey();
    }

    /**
     * Removes an occurrence of the specified object from this {@code TreeSet}.
     * 
     * @param object
     *            the object to remove.
     * @return {@code true} if this {@code TreeSet} was modified, {@code false}
     *         otherwise.
     * @throws ClassCastException
     *             when the object cannot be compared with the elements in this
     *             {@code TreeSet}.
     * @throws NullPointerException
     *             when the object is null and the comparator cannot handle
     *             null.
     */
    @Override
    public boolean remove(Object object) {
        return backingMap.remove(object) != null;
    }

    /**
     * Returns the number of elements in this {@code TreeSet}.
     * 
     * @return the number of elements in this {@code TreeSet}.
     */
    @Override
    public int size() {
        return backingMap.size();
    }

    /**
     * Returns a SortedSet of the specified portion of this {@code TreeSet}
     * which contains elements greater or equal to the start element but less
     * than the end element. The returned SortedSet is backed by this
     * {@code TreeSet} so changes to one are reflected by the other.
     * 
     * @param start
     *            the start element.
     * @param end
     *            the end element (exclusive).
     * @return a subset where the elements are greater or equal to {@code start}
     *         and less than {@code end}
     * @throws ClassCastException
     *             when the start or end object cannot be compared with the
     *             elements in this {@code TreeSet}.
     * @throws NullPointerException
     *             when the start or end object is null and the comparator
     *             cannot handle null.
     */
    @SuppressWarnings("unchecked")
    public SortedSet<E> subSet(E start, E end) {
        Comparator<? super E> c = backingMap.comparator();
        if (c == null) {
            if (((Comparable<E>) start).compareTo(end) <= 0) {
                return new TreeSet<E>(backingMap.subMap(start, end));
            }
        } else {
            if (c.compare(start, end) <= 0) {
                return new TreeSet<E>(backingMap.subMap(start, end));
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * Returns a SortedSet of the specified portion of this {@code TreeSet}
     * which contains elements greater or equal to the start element. The
     * returned SortedSet is backed by this {@code TreeSet} so changes to one
     * are reflected by the other.
     * 
     * @param start
     *            the start element.
     * @return a subset where the elements are greater or equal to {@code start}
     * @throws ClassCastException
     *             when the start object cannot be compared with the elements in
     *             this {@code TreeSet}.
     * @throws NullPointerException
     *             when the start object is null and the comparator cannot
     *             handle null.
     */
    @SuppressWarnings("unchecked")
    public SortedSet<E> tailSet(E start) {
        // Check for errors
        Comparator<? super E> c = backingMap.comparator();
        if (c == null) {
            ((Comparable<E>) start).compareTo(start);
        } else {
            c.compare(start, start);
        }
        return new TreeSet<E>(backingMap.tailMap(start));
    }
}
