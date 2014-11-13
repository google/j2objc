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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * HashSet is an implementation of a Set. All optional operations (adding and
 * removing) are supported. The elements can be any objects.
 */
public class HashSet<E> extends AbstractSet<E> implements Set<E>, Cloneable,
        Serializable {

    private static final long serialVersionUID = -5024744406713321676L;

    transient HashMap<E, Object> backingMap;

    private static final Object dummyKey = new Object();

    /**
     * Constructs a new empty instance of {@code HashSet}.
     */
    public HashSet() {
        this(new HashMap<E, Object>());
    }

    /**
     * Constructs a new instance of {@code HashSet} with the specified capacity.
     *
     * @param capacity
     *            the initial capacity of this {@code HashSet}.
     */
    public HashSet(int capacity) {
        this(new HashMap<E, Object>(capacity));
    }

    /**
     * Constructs a new instance of {@code HashSet} with the specified capacity
     * and load factor.
     *
     * @param capacity
     *            the initial capacity.
     * @param loadFactor
     *            the initial load factor.
     */
    public HashSet(int capacity, float loadFactor) {
        this(new HashMap<E, Object>(capacity, loadFactor));
    }

    /**
     * Constructs a new instance of {@code HashSet} containing the unique
     * elements in the specified collection.
     *
     * @param collection
     *            the collection of elements to add.
     */
    public HashSet(Collection<? extends E> collection) {
        this(new HashMap<E, Object>(collection.size() < 6 ? 11 : collection
                .size() * 2));
        for (E e : collection) {
            add(e);
        }
    }

    HashSet(HashMap<E, Object> backingMap) {
        this.backingMap = backingMap;
    }

    /**
     * Adds the specified object to this {@code HashSet} if not already present.
     *
     * @param object
     *            the object to add.
     * @return {@code true} when this {@code HashSet} did not already contain
     *         the object, {@code false} otherwise
     */
    @Override
    public boolean add(E object) {
        return backingMap.put(object, dummyKey) == null;
    }

    /**
     * Removes all elements from this {@code HashSet}, leaving it empty.
     *
     * @see #isEmpty
     * @see #size
     */
    @Override
    public void clear() {
        backingMap.clear();
    }

    /**
     * Returns a new {@code HashSet} with the same elements and size as this
     * {@code HashSet}.
     *
     * @return a shallow copy of this {@code HashSet}.
     * @see java.lang.Cloneable
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object clone() {
        try {
            HashSet<E> clone = (HashSet<E>) super.clone();
            clone.backingMap = (HashMap<E, Object>) backingMap.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Searches this {@code HashSet} for the specified object.
     *
     * @param object
     *            the object to search for.
     * @return {@code true} if {@code object} is an element of this
     *         {@code HashSet}, {@code false} otherwise.
     */
    @Override
    public boolean contains(Object object) {
        return backingMap.containsKey(object);
    }

    /**
     * Returns true if this {@code HashSet} has no elements, false otherwise.
     *
     * @return {@code true} if this {@code HashSet} has no elements,
     *         {@code false} otherwise.
     * @see #size
     */
    @Override
    public boolean isEmpty() {
        return backingMap.isEmpty();
    }

    /**
     * Returns an Iterator on the elements of this {@code HashSet}.
     *
     * @return an Iterator on the elements of this {@code HashSet}.
     * @see Iterator
     */
    @Override
    public Iterator<E> iterator() {
        return backingMap.keySet().iterator();
    }

    /**
     * Removes the specified object from this {@code HashSet}.
     *
     * @param object
     *            the object to remove.
     * @return {@code true} if the object was removed, {@code false} otherwise.
     */
    @Override
    public boolean remove(Object object) {
        return backingMap.remove(object) != null;
    }

    /**
     * Returns the number of elements in this {@code HashSet}.
     *
     * @return the number of elements in this {@code HashSet}.
     */
    @Override
    public int size() {
        return backingMap.size();
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeInt(backingMap.table.length);
        stream.writeFloat(HashMap.DEFAULT_LOAD_FACTOR);
        stream.writeInt(size());
        for (E e : this) {
            stream.writeObject(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();
        int length = stream.readInt();
        float loadFactor = stream.readFloat();
        backingMap = createBackingMap(length, loadFactor);
        int elementCount = stream.readInt();
        for (int i = elementCount; --i >= 0;) {
            E key = (E) stream.readObject();
            backingMap.put(key, this);
        }
    }

    HashMap<E, Object> createBackingMap(int capacity, float loadFactor) {
        return new HashMap<E, Object>(capacity, loadFactor);
    }

    /*-[
    - (NSUInteger)countByEnumeratingWithState:(NSFastEnumerationState *)state
                                      objects:(__unsafe_unretained id *)stackbuf
                                        count:(NSUInteger)len {
      // Note: HashMap's KeySet implementation must not use extra[4].
      __unsafe_unretained id keySet = (ARCBRIDGE id) (void *) state->extra[4];
      if (!keySet) {
        keySet = [backingMap_ keySet];
        state->extra[4] = (unsigned long) keySet;
      }
      return [keySet countByEnumeratingWithState:state objects:stackbuf count:len];
    }
    ]-*/
}
