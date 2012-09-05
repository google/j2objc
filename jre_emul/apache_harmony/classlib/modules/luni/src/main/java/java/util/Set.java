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
 * A {@code Set} is a data structure which does not allow duplicate elements.
 *
 * @since 1.2
 */
public interface Set<E> extends Collection<E> {
    
    /**
     * Adds the specified object to this set. The set is not modified if it
     * already contains the object.
     * 
     * @param object
     *            the object to add.
     * @return {@code true} if this set is modified, {@code false} otherwise.
     * @throws UnsupportedOperationException
     *             when adding to this set is not supported.
     * @throws ClassCastException
     *             when the class of the object is inappropriate for this set.
     * @throws IllegalArgumentException
     *             when the object cannot be added to this set.
     */
    public boolean add(E object);

    /**
     * Adds the objects in the specified collection which do not exist yet in
     * this set.
     * 
     * @param collection
     *            the collection of objects.
     * @return {@code true} if this set is modified, {@code false} otherwise.
     * @throws UnsupportedOperationException
     *             when adding to this set is not supported.
     * @throws ClassCastException
     *             when the class of an object is inappropriate for this set.
     * @throws IllegalArgumentException
     *             when an object cannot be added to this set.
     */
    public boolean addAll(Collection<? extends E> collection);

    /**
     * Removes all elements from this set, leaving it empty.
     * 
     * @throws UnsupportedOperationException
     *             when removing from this set is not supported.
     * @see #isEmpty
     * @see #size
     */
    public void clear();

    /**
     * Searches this set for the specified object.
     * 
     * @param object
     *            the object to search for.
     * @return {@code true} if object is an element of this set, {@code false}
     *         otherwise.
     */
    public boolean contains(Object object);

    /**
     * Searches this set for all objects in the specified collection.
     * 
     * @param collection
     *            the collection of objects.
     * @return {@code true} if all objects in the specified collection are
     *         elements of this set, {@code false} otherwise.
     */
    public boolean containsAll(Collection<?> collection);

    /**
     * Returns true if this set has no elements.
     * 
     * @return {@code true} if this set has no elements, {@code false}
     *         otherwise.
     * @see #size
     */
    public boolean isEmpty();

    /**
     * Returns an iterator on the elements of this set. The elements are
     * unordered.
     * 
     * @return an iterator on the elements of this set.
     * @see Iterator
     */
    public Iterator<E> iterator();

    /**
     * Removes the specified object from this set.
     * 
     * @param object
     *            the object to remove.
     * @return {@code true} if this set was modified, {@code false} otherwise.
     * @throws UnsupportedOperationException
     *             when removing from this set is not supported.
     */
    public boolean remove(Object object);

    /**
     * Removes all objects in the specified collection from this set.
     * 
     * @param collection
     *            the collection of objects to remove.
     * @return {@code true} if this set was modified, {@code false} otherwise.
     * @throws UnsupportedOperationException
     *             when removing from this set is not supported.
     */
    public boolean removeAll(Collection<?> collection);

    /**
     * Removes all objects from this set that are not contained in the specified
     * collection.
     * 
     * @param collection
     *            the collection of objects to retain.
     * @return {@code true} if this set was modified, {@code false} otherwise.
     * @throws UnsupportedOperationException
     *             when removing from this set is not supported.
     */
    public boolean retainAll(Collection<?> collection);

    /**
     * Returns the number of elements in this set.
     * 
     * @return the number of elements in this set.
     */
    public int size();
}
