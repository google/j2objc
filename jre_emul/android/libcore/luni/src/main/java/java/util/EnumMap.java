/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.util;

import com.google.j2objc.annotations.Weak;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;

/*-[
#include "JreRetainedWith.h"
]-*/

/**
 * An {@code Map} specialized for use with {@code Enum} types as keys.
 */
public class EnumMap<K extends Enum<K>, V> extends AbstractMap<K, V> implements
        Serializable, Cloneable, Map<K, V> {

    // BEGIN android-changed
    // added implements Map<K, V> for apicheck
    // END android-changed

    private static final long serialVersionUID = 458661240069192865L;

    private Class<K> keyType;

    transient K[] keys;

    transient V[] values;

    transient boolean[] hasMapping;

    private transient int mappingsCount;

    transient int enumSize;

    private transient EnumMapEntrySet<K, V> entrySet = null;

    private static class Entry<KT extends Enum<KT>, VT> extends MapEntry<KT, VT> {
        private final EnumMap<KT, VT> enumMap;

        private final int ordinal;

        Entry(KT theKey, VT theValue, EnumMap<KT, VT> em) {
            super(theKey, theValue);
            enumMap = em;
            ordinal = theKey.ordinal();
        }

        @Override
        public boolean equals(Object object) {
            if (!enumMap.hasMapping[ordinal]) {
                return false;
            }
            boolean isEqual = false;
            if (object instanceof Map.Entry) {
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) object;
                Object enumKey = entry.getKey();
                if (key.equals(enumKey)) {
                    Object theValue = entry.getValue();
                    if (enumMap.values[ordinal] == null) {
                        isEqual = (theValue == null);
                    } else {
                        isEqual = enumMap.values[ordinal].equals(theValue);
                    }
                }
            }
            return isEqual;
        }

        @Override
        public int hashCode() {
            return (enumMap.keys[ordinal] == null ? 0 : enumMap.keys[ordinal].hashCode())
                    ^ (enumMap.values[ordinal] == null ? 0
                            : enumMap.values[ordinal].hashCode());
        }

        @Override
        public KT getKey() {
            checkEntryStatus();
            return enumMap.keys[ordinal];
        }

        @Override
        public VT getValue() {
            checkEntryStatus();
            return enumMap.values[ordinal];
        }

        @Override
        public VT setValue(VT value) {
            checkEntryStatus();
            return enumMap.put(enumMap.keys[ordinal], value);
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder(enumMap.keys[ordinal].toString());
            result.append("=");
            result.append(enumMap.values[ordinal] == null
                    ? "null" : enumMap.values[ordinal].toString());
            return result.toString();
        }

        private void checkEntryStatus() {
            if (!enumMap.hasMapping[ordinal]) {
                throw new IllegalStateException();
            }
        }
    }

    private static class EnumMapIterator<E, KT extends Enum<KT>, VT> implements Iterator<E> {
        int position = 0;

        int prePosition = -1;

        final EnumMap<KT, VT> enumMap;

        final MapEntry.Type<E, KT, VT> type;

        EnumMapIterator(MapEntry.Type<E, KT, VT> value, EnumMap<KT, VT> em) {
            enumMap = em;
            type = value;
        }

        public boolean hasNext() {
            int length = enumMap.enumSize;
            for (; position < length; position++) {
                if (enumMap.hasMapping[position]) {
                    break;
                }
            }
            return position != length;
        }

        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            prePosition = position++;
            return type.get(new MapEntry<KT, VT>(enumMap.keys[prePosition],
                    enumMap.values[prePosition]));
        }

        public void remove() {
            checkStatus();
            if (enumMap.hasMapping[prePosition]) {
                enumMap.remove(enumMap.keys[prePosition]);
            }
            prePosition = -1;
        }

        @Override
        public String toString() {
            if (prePosition == -1) {
                return super.toString();
            }
            return type.get(
                    new MapEntry<KT, VT>(enumMap.keys[prePosition],
                            enumMap.values[prePosition])).toString();
        }

        private void checkStatus() {
            if (prePosition == -1) {
                throw new IllegalStateException();
            }
        }
    }

    private static class EnumMapKeySet<KT extends Enum<KT>, VT> extends
            AbstractSet<KT> {
        @Weak
        private final EnumMap<KT, VT> enumMap;

        EnumMapKeySet(EnumMap<KT, VT> em) {
            enumMap = em;
        }

        @Override
        public void clear() {
            enumMap.clear();
        }

        @Override
        public boolean contains(Object object) {
            return enumMap.containsKey(object);
        }

        @Override
        public Iterator<KT> iterator() {
            return new EnumMapIterator<KT, KT, VT>(
                    new MapEntry.Type<KT, KT, VT>() {
                        public KT get(MapEntry<KT, VT> entry) {
                            return entry.key;
                        }
                    }, enumMap);
        }

        @Override
        public boolean remove(Object object) {
            if (contains(object)) {
                enumMap.remove(object);
                return true;
            }
            return false;
        }

        @Override
        public int size() {
            return enumMap.size();
        }

        /*-[ RETAINED_WITH_CHILD(enumMap_) ]-*/
    }

    private static class EnumMapValueCollection<KT extends Enum<KT>, VT>
            extends AbstractCollection<VT> {
        @Weak
        private final EnumMap<KT, VT> enumMap;

        EnumMapValueCollection(EnumMap<KT, VT> em) {
            enumMap = em;
        }

        @Override
        public void clear() {
            enumMap.clear();
        }

        @Override
        public boolean contains(Object object) {
            return enumMap.containsValue(object);
        }

        @Override
        public Iterator<VT> iterator() {
            return new EnumMapIterator<VT, KT, VT>(
                    new MapEntry.Type<VT, KT, VT>() {
                        public VT get(MapEntry<KT, VT> entry) {
                            return entry.value;
                        }
                    }, enumMap);
        }

        @Override
        public boolean remove(Object object) {
            if (object == null) {
                for (int i = 0; i < enumMap.enumSize; i++) {
                    if (enumMap.hasMapping[i] && enumMap.values[i] == null) {
                        enumMap.remove(enumMap.keys[i]);
                        return true;
                    }
                }
            } else {
                for (int i = 0; i < enumMap.enumSize; i++) {
                    if (enumMap.hasMapping[i]
                            && object.equals(enumMap.values[i])) {
                        enumMap.remove(enumMap.keys[i]);
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public int size() {
            return enumMap.size();
        }

        /*-[ RETAINED_WITH_CHILD(enumMap_) ]-*/
    }

    private static class EnumMapEntryIterator<E, KT extends Enum<KT>, VT>
            extends EnumMapIterator<E, KT, VT> {
        EnumMapEntryIterator(MapEntry.Type<E, KT, VT> value, EnumMap<KT, VT> em) {
            super(value, em);
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            prePosition = position++;
            return type.get(new EnumMap.Entry<KT, VT>(enumMap.keys[prePosition],
                    enumMap.values[prePosition], enumMap));
        }
    }

    private static class EnumMapEntrySet<KT extends Enum<KT>, VT> extends
            AbstractSet<Map.Entry<KT, VT>> {
        @Weak
        private final EnumMap<KT, VT> enumMap;

        EnumMapEntrySet(EnumMap<KT, VT> em) {
            enumMap = em;
        }

        @Override
        public void clear() {
            enumMap.clear();
        }

        @Override
        public boolean contains(Object object) {
            boolean isEqual = false;
            if (object instanceof Map.Entry) {
                Object enumKey = ((Map.Entry<?, ?>) object).getKey();
                Object enumValue = ((Map.Entry<?, ?>) object).getValue();
                if (enumMap.containsKey(enumKey)) {
                    VT value = enumMap.get(enumKey);
                    if (value == null) {
                        isEqual = enumValue == null;
                    } else {
                        isEqual = value.equals(enumValue);
                    }
                }
            }
            return isEqual;
        }

        @Override
        public Iterator<Map.Entry<KT, VT>> iterator() {
            return new EnumMapEntryIterator<Map.Entry<KT, VT>, KT, VT>(
                    new MapEntry.Type<Map.Entry<KT, VT>, KT, VT>() {
                        public Map.Entry<KT, VT> get(MapEntry<KT, VT> entry) {
                            return entry;
                        }
                    }, enumMap);
        }

        @Override
        public boolean remove(Object object) {
            if (contains(object)) {
                enumMap.remove(((Map.Entry<?, ?>) object).getKey());
                return true;
            }
            return false;
        }

        @Override
        public int size() {
            return enumMap.size();
        }

        @Override
        public Object[] toArray() {
            Object[] entryArray = new Object[enumMap.size()];
            return toArray(entryArray);
        }

        @Override
        public <T> T[] toArray(T[] array) {
            int size = enumMap.size();
            int index = 0;
            T[] entryArray = array;
            if (size > array.length) {
                Class<?> clazz = array.getClass().getComponentType();
                @SuppressWarnings("unchecked") T[] newArray = (T[]) Array.newInstance(clazz, size);
                entryArray = newArray;
            }
            Iterator<Map.Entry<KT, VT>> iter = iterator();
            for (; index < size; index++) {
                Map.Entry<KT, VT> entry = iter.next();
                @SuppressWarnings("unchecked") T newEntry =
                        (T) new MapEntry<KT, VT>(entry.getKey(), entry.getValue());
                entryArray[index] = newEntry;
            }
            if (index < array.length) {
                entryArray[index] = null;
            }
            return entryArray;
        }

        /*-[ RETAINED_WITH_CHILD(enumMap_) ]-*/
    }

    /**
     * Constructs an empty {@code EnumMap} using the given key type.
     *
     * @param keyType
     *            the class object giving the type of the keys used by this {@code EnumMap}.
     * @throws NullPointerException
     *             if {@code keyType} is {@code null}.
     */
    public EnumMap(Class<K> keyType) {
        initialization(keyType);
    }

    /**
     * Constructs an {@code EnumMap} using the same key type as the given {@code EnumMap} and
     * initially containing the same mappings.
     *
     * @param map
     *            the {@code EnumMap} from which this {@code EnumMap} is initialized.
     * @throws NullPointerException
     *             if {@code map} is {@code null}.
     */
    public EnumMap(EnumMap<K, ? extends V> map) {
        initialization(map);
    }

    /**
     * Constructs an {@code EnumMap} initialized from the given map. If the given map
     * is an {@code EnumMap} instance, this constructor behaves in the exactly the same
     * way as {@link EnumMap#EnumMap(EnumMap)}}. Otherwise, the given map
     * should contain at least one mapping.
     *
     * @param map
     *            the map from which this {@code EnumMap} is initialized.
     * @throws IllegalArgumentException
     *             if {@code map} is not an {@code EnumMap} instance and does not contain
     *             any mappings.
     * @throws NullPointerException
     *             if {@code map} is {@code null}.
     */
    public EnumMap(Map<K, ? extends V> map) {
        if (map instanceof EnumMap) {
            @SuppressWarnings("unchecked") EnumMap<K, ? extends V> enumMap =
                    (EnumMap<K, ? extends V>) map;
            initialization(enumMap);
        } else {
            if (map.isEmpty()) {
                throw new IllegalArgumentException("map is empty");
            }
            Iterator<K> iter = map.keySet().iterator();
            K enumKey = iter.next();
            // Confirm the key is actually an enum: Throw ClassCastException if not.
            Enum.class.cast(enumKey);
            Class<?> clazz = enumKey.getClass();
            if (!clazz.isEnum()) {
                // Each enum value can have its own subclass. In this case we want the abstract
                // super-class which has the values() method.
                clazz = clazz.getSuperclass();
            }
            @SuppressWarnings("unchecked") Class<K> enumClass = (Class<K>) clazz;
            initialization(enumClass);
            putAllImpl(map);
        }
    }

    /**
     * Removes all elements from this {@code EnumMap}, leaving it empty.
     *
     * @see #isEmpty()
     * @see #size()
     */
    @Override
    public void clear() {
        Arrays.fill(values, null);
        Arrays.fill(hasMapping, false);
        mappingsCount = 0;
    }

    /**
     * Returns a shallow copy of this {@code EnumMap}.
     *
     * @return a shallow copy of this {@code EnumMap}.
     */
    @Override
    public EnumMap<K, V> clone() {
        try {
            @SuppressWarnings("unchecked") EnumMap<K, V> enumMap = (EnumMap<K, V>) super.clone();
            enumMap.initialization(this);
            return enumMap;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns whether this {@code EnumMap} contains the specified key.
     *
     * @param key
     *            the key to search for.
     * @return {@code true} if this {@code EnumMap} contains the specified key,
     *         {@code false} otherwise.
     */
    @Override
    public boolean containsKey(Object key) {
        if (isValidKeyType(key)) {
            int keyOrdinal = ((Enum) key).ordinal();
            return hasMapping[keyOrdinal];
        }
        return false;
    }

    /**
     * Returns whether this {@code EnumMap} contains the specified value.
     *
     * @param value
     *            the value to search for.
     * @return {@code true} if this {@code EnumMap} contains the specified value,
     *         {@code false} otherwise.
     */
    @Override
    public boolean containsValue(Object value) {
        if (value == null) {
            for (int i = 0; i < enumSize; i++) {
                if (hasMapping[i] && values[i] == null) {
                    return true;
                }
            }
        } else {
            for (int i = 0; i < enumSize; i++) {
                if (hasMapping[i] && value.equals(values[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns a {@code Set} containing all of the mappings in this {@code EnumMap}. Each mapping is
     * an instance of {@link Map.Entry}. As the {@code Set} is backed by this {@code EnumMap},
     * changes in one will be reflected in the other.
     * <p>
     * The order of the entries in the set will be the order that the enum keys
     * were declared in.
     *
     * @return a {@code Set} of the mappings.
     */
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        if (entrySet == null) {
            entrySet = new EnumMapEntrySet<K, V>(this);
        }
        return entrySet;
    }

    /**
     * Compares the argument to the receiver, and returns {@code true} if the
     * specified {@code Object} is an {@code EnumMap} and both {@code EnumMap}s contain the same mappings.
     *
     * @param object
     *            the {@code Object} to compare with this {@code EnumMap}.
     * @return boolean {@code true} if {@code object} is the same as this {@code EnumMap},
     *         {@code false} otherwise.
     * @see #hashCode()
     * @see #entrySet()
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof EnumMap)) {
            return super.equals(object);
        }
        @SuppressWarnings("unchecked") EnumMap<K, V> enumMap = (EnumMap<K, V>) object;
        if (keyType != enumMap.keyType || size() != enumMap.size()) {
            return false;
        }
        return Arrays.equals(hasMapping, enumMap.hasMapping)
                && Arrays.equals(values, enumMap.values);
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
        if (!isValidKeyType(key)) {
            return null;
        }
        int keyOrdinal = ((Enum) key).ordinal();
        return values[keyOrdinal];
    }

    /**
     * Returns a set of the keys contained in this {@code EnumMap}. The {@code Set} is backed by
     * this {@code EnumMap} so changes to one are reflected in the other. The {@code Set} does not
     * support adding.
     * <p>
     * The order of the set will be the order that the enum keys were declared
     * in.
     *
     * @return a {@code Set} of the keys.
     */
    @Override
    public Set<K> keySet() {
        if (keySet == null) {
            keySet = new EnumMapKeySet<K, V>(this);
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
     *                if the key or value is {@code null} and this {@code EnumMap} does not
     *                support {@code null} keys or values.
     */
    @Override
    public V put(K key, V value) {
        return putImpl(key, value);
    }

    /**
     * Copies every mapping in the specified {@code Map} to this {@code EnumMap}.
     *
     * @param map
     *            the {@code Map} to copy mappings from.
     * @throws UnsupportedOperationException
     *                if adding to this {@code EnumMap} is not supported.
     * @throws ClassCastException
     *                if the class of a key or value is inappropriate for this
     *                {@code EnumMap}.
     * @throws IllegalArgumentException
     *                if a key or value cannot be added to this map.
     * @throws NullPointerException
     *                if a key or value is {@code null} and this {@code EnumMap} does not
     *                support {@code null} keys or values.
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        putAllImpl(map);
    }

    /**
     * Removes a mapping with the specified key from this {@code EnumMap}.
     *
     * @param key
     *            the key of the mapping to remove.
     * @return the value of the removed mapping or {@code null} if no mapping
     *         for the specified key was found.
     * @throws UnsupportedOperationException
     *                if removing from this {@code EnumMap} is not supported.
     */
    @Override
    public V remove(Object key) {
        if (!isValidKeyType(key)) {
            return null;
        }
        int keyOrdinal = ((Enum) key).ordinal();
        if (hasMapping[keyOrdinal]) {
            hasMapping[keyOrdinal] = false;
            mappingsCount--;
        }
        V oldValue = values[keyOrdinal];
        values[keyOrdinal] = null;
        return oldValue;
    }

    /**
     * Returns the number of elements in this {@code EnumMap}.
     *
     * @return the number of elements in this {@code EnumMap}.
     */
    @Override
    public int size() {
        return mappingsCount;
    }

    /**
     * Returns a {@code Collection} of the values contained in this {@code EnumMap}. The returned
     * {@code Collection} complies with the general rule specified in
     * {@link Map#values()}. The {@code Collection}'s {@code Iterator} will return the values
     * in the their corresponding keys' natural order (the {@code Enum} constants are
     * declared in this order).
     * <p>
     * The order of the values in the collection will be the order that their
     * corresponding enum keys were declared in.
     *
     * @return a collection of the values contained in this {@code EnumMap}.
     */
    @Override
    public Collection<V> values() {
        if (valuesCollection == null) {
            valuesCollection = new EnumMapValueCollection<K, V>(this);
        }
        return valuesCollection;
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();
        initialization(keyType);
        int elementCount = stream.readInt();
        K enumKey;
        V value;
        for (int i = elementCount; i > 0; i--) {
            enumKey = (K) stream.readObject();
            value = (V) stream.readObject();
            putImpl(enumKey, value);
        }
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeInt(mappingsCount);
        for (Map.Entry<K, V> entry : entrySet()) {
            stream.writeObject(entry.getKey());
            stream.writeObject(entry.getValue());
        }
    }

    private boolean isValidKeyType(Object key) {
        return key != null && keyType.isInstance(key);
    }

    private void initialization(EnumMap<K, ? extends V> enumMap) {
        keyType = enumMap.keyType;
        keys = enumMap.keys;
        enumSize = enumMap.enumSize;
        values = enumMap.values.clone();
        hasMapping = enumMap.hasMapping.clone();
        mappingsCount = enumMap.mappingsCount;
    }

    private void initialization(Class<K> type) {
        keyType = type;
        keys = Enum.getSharedConstants(keyType);
        enumSize = keys.length;
        // The value array is actually Object[] for speed of creation. It is treated as a V[]
        // because it is safe to do so and eliminates unchecked warning suppression throughout.
        @SuppressWarnings("unchecked") V[] valueArray = (V[]) new Object[enumSize];
        values = valueArray;
        hasMapping = new boolean[enumSize];
    }

    private void putAllImpl(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            putImpl(entry.getKey(), entry.getValue());
        }
    }

    private V putImpl(K key, V value) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        keyType.cast(key); // Called to throw ClassCastException.
        int keyOrdinal = key.ordinal();
        if (!hasMapping[keyOrdinal]) {
            hasMapping[keyOrdinal] = true;
            mappingsCount++;
        }
        V oldValue = values[keyOrdinal];
        values[keyOrdinal] = value;
        return oldValue;
    }

}
