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
 * A map that has its keys ordered. The sorting is according to either the
 * natural ordering of its keys or the ordering given by a specified comparator.
 */
public interface SortedMap<K,V> extends Map<K,V> {

    /**
     * Returns the comparator used to compare keys in this sorted map, or null if the natural
     * ordering is in use.
     */
    public Comparator<? super K> comparator();

    /**
     * Returns the least key in this sorted map.
     *
     * @throws NoSuchElementException
     *                if this sorted map is empty.
     */
    public K firstKey();

    /**
     * Returns a sorted map over a range of this sorted map with all keys that
     * are less than the specified {@code endKey}. Changes to the returned
     * sorted map are reflected in this sorted map and vice versa.
     * <p>
     * Note: The returned map will not allow an insertion of a key outside the
     * specified range.
     *
     * @param endKey
     *            the high boundary of the range specified.
     * @return a sorted map where the keys are less than {@code endKey}.
     * @throws ClassCastException
     *             if the class of the end key is inappropriate for this sorted
     *             map.
     * @throws NullPointerException
     *             if the end key is {@code null} and this sorted map does not
     *             support {@code null} keys.
     * @throws IllegalArgumentException
     *             if this map is itself a sorted map over a range of another
     *             map and the specified key is outside of its range.
     */
    public SortedMap<K,V> headMap(K endKey);

    /**
     * Returns the greatest key in this sorted map.
     *
     * @throws NoSuchElementException
     *                if this sorted map is empty.
     */
    public K lastKey();

    /**
     * Returns a sorted map over a range of this sorted map with all keys
     * greater than or equal to the specified {@code startKey} and less than the
     * specified {@code endKey}. Changes to the returned sorted map are
     * reflected in this sorted map and vice versa.
     * <p>
     * Note: The returned map will not allow an insertion of a key outside the
     * specified range.
     *
     * @param startKey
     *            the low boundary of the range (inclusive).
     * @param endKey
     *            the high boundary of the range (exclusive),
     * @return a sorted map with the key from the specified range.
     * @throws ClassCastException
     *             if the class of the start or end key is inappropriate for
     *             this sorted map.
     * @throws NullPointerException
     *             if the start or end key is {@code null} and this sorted map
     *             does not support {@code null} keys.
     * @throws IllegalArgumentException
     *             if the start key is greater than the end key, or if this map
     *             is itself a sorted map over a range of another sorted map and
     *             the specified range is outside of its range.
     */
    public SortedMap<K,V> subMap(K startKey, K endKey);

    /**
     * Returns a sorted map over a range of this sorted map with all keys that
     * are greater than or equal to the specified {@code startKey}. Changes to
     * the returned sorted map are reflected in this sorted map and vice versa.
     * <p>
     * Note: The returned map will not allow an insertion of a key outside the
     * specified range.
     *
     * @param startKey
     *            the low boundary of the range specified.
     * @return a sorted map where the keys are greater or equal to
     *         {@code startKey}.
     * @throws ClassCastException
     *             if the class of the start key is inappropriate for this
     *             sorted map.
     * @throws NullPointerException
     *             if the start key is {@code null} and this sorted map does not
     *             support {@code null} keys.
     * @throws IllegalArgumentException
     *             if this map itself a sorted map over a range of another map
     *             and the specified key is outside of its range.
     */
    public SortedMap<K,V> tailMap(K startKey);
}
