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


/**
 * SortedSet is a Set which iterates over its elements in a sorted order. The
 * order is determined either by the elements natural ordering, or by a
 * {@link Comparator} which is passed into a concrete implementation at
 * construction time. All elements in this set must be mutually comparable. The
 * ordering in this set must be consistent with {@code equals} of its elements.
 *
 * @see Comparator
 * @see Comparable
 */
public interface SortedSet<E> extends Set<E> {

    /**
     * Returns the comparator used to compare elements in this {@code SortedSet}.
     *
     * @return a comparator or null if the natural ordering is used.
     */
    public Comparator<? super E> comparator();

    /**
     * Returns the first element in this {@code SortedSet}. The first element
     * is the lowest element.
     *
     * @return the first element.
     * @throws NoSuchElementException
     *             when this {@code SortedSet} is empty.
     */
    public E first();

    /**
     * Returns a {@code SortedSet} of the specified portion of this
     * {@code SortedSet} which contains elements less than the end element. The
     * returned {@code SortedSet} is backed by this {@code SortedSet} so changes
     * to one set are reflected by the other.
     *
     * @param end
     *            the end element.
     * @return a subset where the elements are less than {@code end}.
     * @throws ClassCastException
     *             when the class of the end element is inappropriate for this
     *             SubSet.
     * @throws NullPointerException
     *             when the end element is null and this {@code SortedSet} does
     *             not support null elements.
     */
    public SortedSet<E> headSet(E end);

    /**
     * Returns the last element in this {@code SortedSet}. The last element is
     * the highest element.
     *
     * @return the last element.
     * @throws NoSuchElementException
     *             when this {@code SortedSet} is empty.
     */
    public E last();

    /**
     * Returns a {@code SortedSet} of the specified portion of this
     * {@code SortedSet} which contains elements greater or equal to the start
     * element but less than the end element. The returned {@code SortedSet} is
     * backed by this SortedMap so changes to one set are reflected by the
     * other.
     *
     * @param start
     *            the start element.
     * @param end
     *            the end element.
     * @return a subset where the elements are greater or equal to {@code start}
     *         and less than {@code end}.
     * @throws ClassCastException
     *             when the class of the start or end element is inappropriate
     *             for this SubSet.
     * @throws NullPointerException
     *             when the start or end element is null and this
     *             {@code SortedSet} does not support null elements.
     * @throws IllegalArgumentException
     *             when the start element is greater than the end element.
     */
    public SortedSet<E> subSet(E start, E end);

    /**
     * Returns a {@code SortedSet} of the specified portion of this
     * {@code SortedSet} which contains elements greater or equal to the start
     * element. The returned {@code SortedSet} is backed by this
     * {@code SortedSet} so changes to one set are reflected by the other.
     *
     * @param start
     *            the start element.
     * @return a subset where the elements are greater or equal to {@code start} .
     * @throws ClassCastException
     *             when the class of the start element is inappropriate for this
     *             SubSet.
     * @throws NullPointerException
     *             when the start element is null and this {@code SortedSet}
     *             does not support null elements.
     */
    public SortedSet<E> tailSet(E start);
}
