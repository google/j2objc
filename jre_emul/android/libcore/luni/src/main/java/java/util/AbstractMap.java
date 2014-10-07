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

import java.io.Serializable;

/**
 * A base class for {@code Map} implementations.
 *
 * <p>Subclasses that permit new mappings to be added must override {@link
 * #put}.
 *
 * <p>The default implementations of many methods are inefficient for large
 * maps. For example in the default implementation, each call to {@link #get}
 * performs a linear iteration of the entry set. Subclasses should override such
 * methods to improve their performance.
 *
 * @since 1.2
 */
public abstract class AbstractMap<K, V> implements Map<K, V> {
    // Lazily-initialized key set (for implementing {@link #keySet}).
    Set<K> keySet;

    // Lazily-initialized values collection (for implementing {@link #values}).
    Collection<V> valuesCollection;

    /**
     * An immutable key-value mapping. Despite the name, this class is non-final
     * and its subclasses may be mutable.
     *
     * @since 1.6
     */
    public static class SimpleImmutableEntry<K, V>
            implements Map.Entry<K, V>, Serializable {
        private static final long serialVersionUID = 7138329143949025153L;

        private final K key;
        private final V value;

        public SimpleImmutableEntry(K theKey, V theValue) {
            key = theKey;
            value = theValue;
        }

        /**
         * Constructs an instance with the key and value of {@code copyFrom}.
         */
        public SimpleImmutableEntry(Map.Entry<? extends K, ? extends V> copyFrom) {
            key = copyFrom.getKey();
            value = copyFrom.getValue();
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        /**
         * This base implementation throws {@code UnsupportedOperationException}
         * always.
         */
        public V setValue(V object) {
            throw new UnsupportedOperationException();
        }

        @Override public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof Map.Entry) {
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) object;
                return (key == null ? entry.getKey() == null : key.equals(entry
                        .getKey()))
                        && (value == null ? entry.getValue() == null : value
                                .equals(entry.getValue()));
            }
            return false;
        }

        @Override public int hashCode() {
            return (key == null ? 0 : key.hashCode())
                    ^ (value == null ? 0 : value.hashCode());
        }

        @Override public String toString() {
            return key + "=" + value;
        }
    }

    /**
     * A key-value mapping with mutable values.
     *
     * @since 1.6
     */
    public static class SimpleEntry<K, V>
            implements Map.Entry<K, V>, Serializable {
        private static final long serialVersionUID = -8499721149061103585L;

        private final K key;
        private V value;

        public SimpleEntry(K theKey, V theValue) {
            key = theKey;
            value = theValue;
        }

        /**
         * Constructs an instance with the key and value of {@code copyFrom}.
         */
        public SimpleEntry(Map.Entry<? extends K, ? extends V> copyFrom) {
            key = copyFrom.getKey();
            value = copyFrom.getValue();
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(V object) {
            V result = value;
            value = object;
            return result;
        }

        @Override public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof Map.Entry) {
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) object;
                return (key == null ? entry.getKey() == null : key.equals(entry
                        .getKey()))
                        && (value == null ? entry.getValue() == null : value
                                .equals(entry.getValue()));
            }
            return false;
        }

        @Override public int hashCode() {
            return (key == null ? 0 : key.hashCode())
                    ^ (value == null ? 0 : value.hashCode());
        }

        @Override public String toString() {
            return key + "=" + value;
        }
    }

    protected AbstractMap() {
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation calls {@code entrySet().clear()}.
     */
    public void clear() {
        entrySet().clear();
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation iterates its key set, looking for a key that
     * {@code key} equals.
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
     * {@inheritDoc}
     *
     * <p>This implementation iterates its entry set, looking for an entry with
     * a value that {@code value} equals.
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

    public abstract Set<Map.Entry<K, V>> entrySet();

    /**
     * {@inheritDoc}
     *
     * <p>This implementation first checks the structure of {@code object}. If
     * it is not a map or of a different size, this returns false. Otherwise it
     * iterates its own entry set, looking up each entry's key in {@code
     * object}. If any value does not equal the other map's value for the same
     * key, this returns false. Otherwise it returns true.
     */
    @Override public boolean equals(Object object) {
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
     * {@inheritDoc}
     *
     * <p>This implementation iterates its entry set, looking for an entry with
     * a key that {@code key} equals.
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
     * {@inheritDoc}
     *
     * <p>This implementation iterates its entry set, summing the hashcodes of
     * its entries.
     */
    @Override public int hashCode() {
        int result = 0;
        Iterator<Map.Entry<K, V>> it = entrySet().iterator();
        while (it.hasNext()) {
            result += it.next().hashCode();
        }
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation compares {@code size()} to 0.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation returns a view that calls through this to map. Its
     * iterator transforms this map's entry set iterator to return keys.
     */
    public Set<K> keySet() {
        if (keySet == null) {
            @WeakOuter
            class AbstractMapKeySet extends AbstractSet<K> {
                @Override public boolean contains(Object object) {
                    return containsKey(object);
                }

                @Override public int size() {
                    return AbstractMap.this.size();
                }

                @Override public Iterator<K> iterator() {
                    return new Iterator<K>() {
                        Iterator<Map.Entry<K, V>> setIterator = entrySet().iterator();

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
            }
            keySet = new AbstractMapKeySet();
        }
        return keySet;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This base implementation throws {@code UnsupportedOperationException}.
     */
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation iterates through {@code map}'s entry set, calling
     * {@code put()} for each.
     */
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation iterates its entry set, removing the entry with
     * a key that {@code key} equals.
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
     * {@inheritDoc}
     *
     * <p>This implementation returns its entry set's size.
     */
    public int size() {
        return entrySet().size();
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation composes a string by iterating its entry set. If
     * this map contains itself as a key or a value, the string "(this Map)"
     * will appear in its place.
     */
    @Override public String toString() {
        if (isEmpty()) {
            return "{}";
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
                buffer.append("(this Map)");
            }
            buffer.append('=');
            Object value = entry.getValue();
            if (value != this) {
                buffer.append(value);
            } else {
                buffer.append("(this Map)");
            }
            if (it.hasNext()) {
                buffer.append(", ");
            }
        }
        buffer.append('}');
        return buffer.toString();
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation returns a view that calls through this to map. Its
     * iterator transforms this map's entry set iterator to return values.
     */
    public Collection<V> values() {
        if (valuesCollection == null) {
            @WeakOuter
            class AbstractMapValuesCollection extends AbstractCollection<V> {
                @Override public int size() {
                    return AbstractMap.this.size();
                }

                @Override public boolean contains(Object object) {
                    return containsValue(object);
                }

                @Override public Iterator<V> iterator() {
                    return new Iterator<V>() {
                        Iterator<Map.Entry<K, V>> setIterator = entrySet().iterator();

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
            }
            valuesCollection = new AbstractMapValuesCollection();
        }
        return valuesCollection;
    }

    @SuppressWarnings("unchecked")
    @Override protected Object clone() throws CloneNotSupportedException {
        AbstractMap<K, V> result = (AbstractMap<K, V>) super.clone();
        result.keySet = null;
        result.valuesCollection = null;
        return result;
    }
}
