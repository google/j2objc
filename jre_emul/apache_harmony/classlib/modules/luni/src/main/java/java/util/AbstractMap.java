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
 * This class is an abstract implementation of the {@code Map} interface. This
 * implementation does not support adding. A subclass must implement the
 * abstract method entrySet().
 * 
 * @since 1.2
 */
public abstract class AbstractMap<K, V> implements Map<K, V> {

    // Lazily initialized key set.
    Set<K> keySet;

    Collection<V> valuesCollection;

    /**
     * Constructs a new instance of this {@code AbstractMap}.
     */
    protected AbstractMap() {
        super();
    }

    /**
     * Removes all elements from this map, leaving it empty.
     * 
     * @throws UnsupportedOperationException
     *                if removing from this map is not supported.
     * @see #isEmpty()
     * @see #size()
     */
    public void clear() {
        entrySet().clear();
    }

    /**
     * Returns whether this map contains the specified key.
     * 
     * @param key
     *            the key to search for.
     * @return {@code true} if this map contains the specified key,
     *         {@code false} otherwise.
     */
    public boolean containsKey(Object key) {
        Iterator<Map.Entry<K, V>> it = entrySet().iterator();
        if (key != null) {
            while (it.hasNext()) {
                if (key.equals(it.next().getKey())) {
                    return true;
                }
            }
        } else {
            while (it.hasNext()) {
                if (it.next().getKey() == null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns whether this map contains the specified value.
     * 
     * @param value
     *            the value to search for.
     * @return {@code true} if this map contains the specified value,
     *         {@code false} otherwise.
     */
    public boolean containsValue(Object value) {
        Iterator<Map.Entry<K, V>> it = entrySet().iterator();
        if (value != null) {
            while (it.hasNext()) {
                if (value.equals(it.next().getValue())) {
                    return true;
                }
            }
        } else {
            while (it.hasNext()) {
                if (it.next().getValue() == null) {
                    return true;
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
    public abstract Set<Map.Entry<K, V>> entrySet();

    /**
     * Compares the specified object to this instance, and returns {@code true}
     * if the specified object is a map and both maps contain the same mappings.
     * 
     * @param object
     *            the object to compare with this object.
     * @return boolean {@code true} if the object is the same as this object,
     *         and {@code false} if it is different from this object.
     * @see #hashCode()
     * @see #entrySet()
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) object;
            if (size() != map.size()) {
                return false;
            }

            try {
                for (Entry<K, V> entry : entrySet()) {
                    K key = entry.getKey();
                    V mine = entry.getValue();
                    Object theirs = map.get(key);
                    if (mine == null) {
                        if (theirs != null || !map.containsKey(key)) {
                            return false;
                        }
                    } else if (!mine.equals(theirs)) {
                        return false;
                    }
                }
            } catch (NullPointerException ignored) {
                return false;
            } catch (ClassCastException ignored) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the value of the mapping with the specified key.
     * 
     * @param key
     *            the key.
     * @return the value of the mapping with the specified key, or {@code null}
     *         if no mapping for the specified key is found.
     */
    public V get(Object key) {
        Iterator<Map.Entry<K, V>> it = entrySet().iterator();
        if (key != null) {
            while (it.hasNext()) {
                Map.Entry<K, V> entry = it.next();
                if (key.equals(entry.getKey())) {
                    return entry.getValue();
                }
            }
        } else {
            while (it.hasNext()) {
                Map.Entry<K, V> entry = it.next();
                if (entry.getKey() == null) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Returns the hash code for this object. Objects which are equal must
     * return the same value for this method.
     * 
     * @return the hash code of this object.
     * @see #equals(Object)
     */
    @Override
    public int hashCode() {
        int result = 0;
        Iterator<Map.Entry<K, V>> it = entrySet().iterator();
        while (it.hasNext()) {
            result += it.next().hashCode();
        }
        return result;
    }

    /**
     * Returns whether this map is empty.
     * 
     * @return {@code true} if this map has no elements, {@code false}
     *         otherwise.
     * @see #size()
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns a set of the keys contained in this map. The set is backed by
     * this map so changes to one are reflected by the other. The returned set
     * does not support adding.
     * 
     * @return a set of the keys.
     */
    public Set<K> keySet() {
        if (keySet == null) {
            keySet = new AbstractSet<K>() {
                @Override
                public boolean contains(Object object) {
                    return containsKey(object);
                }

                @Override
                public int size() {
                    return AbstractMap.this.size();
                }

                @Override
                public Iterator<K> iterator() {
                    return new Iterator<K>() {
                        Iterator<Map.Entry<K, V>> setIterator = entrySet()
                                .iterator();

                        public boolean hasNext() {
                            return setIterator.hasNext();
                        }

                        public K next() {
                            return setIterator.next().getKey();
                        }

                        public void remove() {
                            setIterator.remove();
                        }
                    };
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
     *         {@code null} if there was no mapping.
     * @throws UnsupportedOperationException
     *                if adding to this map is not supported.
     * @throws ClassCastException
     *                if the class of the key or value is inappropriate for this
     *                map.
     * @throws IllegalArgumentException
     *                if the key or value cannot be added to this map.
     * @throws NullPointerException
     *                if the key or value is {@code null} and this Map does not
     *                support {@code null} keys or values.
     */
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Copies every mapping in the specified map to this map.
     * 
     * @param map
     *            the map to copy mappings from.
     * @throws UnsupportedOperationException
     *                if adding to this map is not supported.
     * @throws ClassCastException
     *                if the class of a key or value is inappropriate for this
     *                map.
     * @throws IllegalArgumentException
     *                if a key or value cannot be added to this map.
     * @throws NullPointerException
     *                if a key or value is {@code null} and this map does not
     *                support {@code null} keys or values.
     */
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Removes a mapping with the specified key from this Map.
     * 
     * @param key
     *            the key of the mapping to remove.
     * @return the value of the removed mapping or {@code null} if no mapping
     *         for the specified key was found.
     * @throws UnsupportedOperationException
     *                if removing from this map is not supported.
     */
    public V remove(Object key) {
        Iterator<Map.Entry<K, V>> it = entrySet().iterator();
        if (key != null) {
            while (it.hasNext()) {
                Map.Entry<K, V> entry = it.next();
                if (key.equals(entry.getKey())) {
                    it.remove();
                    return entry.getValue();
                }
            }
        } else {
            while (it.hasNext()) {
                Map.Entry<K, V> entry = it.next();
                if (entry.getKey() == null) {
                    it.remove();
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Returns the number of elements in this map.
     * 
     * @return the number of elements in this map.
     */
    public int size() {
        return entrySet().size();
    }

    /**
     * Returns the string representation of this map.
     * 
     * @return the string representation of this map.
     */
    @Override
    public String toString() {
        if (isEmpty()) {
            return "{}"; //$NON-NLS-1$
        }

        StringBuilder buffer = new StringBuilder(size() * 28);
        buffer.append('{');
        Iterator<Map.Entry<K, V>> it = entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<K, V> entry = it.next();
            Object key = entry.getKey();
            if (key != this) {
                buffer.append(key);
            } else {
                buffer.append("(this Map)"); //$NON-NLS-1$
            }
            buffer.append('=');
            Object value = entry.getValue();
            if (value != this) {
                buffer.append(value);
            } else {
                buffer.append("(this Map)"); //$NON-NLS-1$
            }
            if (it.hasNext()) {
                buffer.append(", "); //$NON-NLS-1$
            }
        }
        buffer.append('}');
        return buffer.toString();
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
     * different collections when multiple concurrent calls occur to this
     * method, since no synchronization is performed.
     *
     * @return a collection of the values contained in this map.
     */
    public Collection<V> values() {
        if (valuesCollection == null) {
            valuesCollection = new AbstractCollection<V>() {
                @Override
                public int size() {
                    return AbstractMap.this.size();
                }

                @Override
                public boolean contains(Object object) {
                    return containsValue(object);
                }

                @Override
                public Iterator<V> iterator() {
                    return new Iterator<V>() {
                        Iterator<Map.Entry<K, V>> setIterator = entrySet()
                                .iterator();

                        public boolean hasNext() {
                            return setIterator.hasNext();
                        }

                        public V next() {
                            return setIterator.next().getValue();
                        }

                        public void remove() {
                            setIterator.remove();
                        }
                    };
                }
            };
        }
        return valuesCollection;
    }

    /**
     * Returns a new instance of the same class as this instance, whose slots
     * have been filled in with the values of the slots of this instance.
     *
     * @return a shallow copy of this object.
     * @throws CloneNotSupportedException
     *                if the receiver's class does not implement the interface
     *                {@code Cloneable}.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Object clone() throws CloneNotSupportedException {
        AbstractMap<K, V> result = (AbstractMap<K, V>) super.clone();
        result.keySet = null;
        result.valuesCollection = null;
        return result;
    }
}
