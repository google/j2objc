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
 * <strong>Note: Do not use this class since it is obsolete. Please use the
 * {@link Map} interface for new implementations.</strong>
 * <p>
 * Dictionary is an abstract class which is the superclass of all classes that
 * associate keys with values, such as {@code Hashtable}.
 *
 * @see Hashtable
 * @since 1.0
 */
public abstract class Dictionary<K, V> {
    /**
     * Constructs a new instance of this class.
     */
    public Dictionary() {
    }

    /**
     * Returns an enumeration on the elements of this dictionary.
     *
     * @return an enumeration of the values of this dictionary.
     * @see #keys
     * @see #size
     * @see Enumeration
     */
    public abstract Enumeration<V> elements();

    /**
     * Returns the value which is associated with {@code key}.
     *
     * @param key
     *            the key of the value returned.
     * @return the value associated with {@code key}, or {@code null} if the
     *         specified key does not exist.
     * @see #put
     */
    public abstract V get(Object key);

    /**
     * Returns true if this dictionary has no key/value pairs.
     *
     * @return {@code true} if this dictionary has no key/value pairs,
     *         {@code false} otherwise.
     * @see #size
     */
    public abstract boolean isEmpty();

    /**
     * Returns an enumeration on the keys of this dictionary.
     *
     * @return an enumeration of the keys of this dictionary.
     * @see #elements
     * @see #size
     * @see Enumeration
     */
    public abstract Enumeration<K> keys();

    /**
     * Associate {@code key} with {@code value} in this dictionary. If {@code
     * key} exists in the dictionary before this call, the old value in the
     * dictionary is replaced by {@code value}.
     *
     * @param key
     *            the key to add.
     * @param value
     *            the value to add.
     * @return the old value previously associated with {@code key} or {@code
     *         null} if {@code key} is new to the dictionary.
     * @see #elements
     * @see #get
     * @see #keys
     */
    public abstract V put(K key, V value);

    /**
     * Removes the key/value pair with the specified {@code key} from this
     * dictionary.
     *
     * @param key
     *            the key to remove.
     * @return the associated value before the deletion or {@code null} if
     *         {@code key} was not known to this dictionary.
     * @see #get
     * @see #put
     */
    public abstract V remove(Object key);

    /**
     * Returns the number of key/value pairs in this dictionary.
     *
     * @return the number of key/value pairs in this dictionary.
     * @see #elements
     * @see #keys
     */
    public abstract int size();
}
