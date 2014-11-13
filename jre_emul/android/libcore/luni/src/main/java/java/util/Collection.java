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
 * {@code Collection} is the root of the collection hierarchy. It defines operations on
 * data collections and the behavior that they will have in all implementations
 * of {@code Collection}s.
 *
 * All direct or indirect implementations of {@code Collection} should implement at
 * least two constructors. One with no parameters which creates an empty
 * collection and one with a parameter of type {@code Collection}. This second
 * constructor can be used to create a collection of different type as the
 * initial collection but with the same elements. Implementations of {@code Collection}
 * cannot be forced to implement these two constructors but at least all
 * implementations under {@code java.util} do.
 *
 * Methods that change the content of a collection throw an
 * {@code UnsupportedOperationException} if the underlying collection does not
 * support that operation, though it's not mandatory to throw such an {@code Exception}
 * in cases where the requested operation would not change the collection. In
 * these cases it's up to the implementation whether it throws an
 * {@code UnsupportedOperationException} or not.
 *
 * Methods marked with (optional) can throw an
 * {@code UnsupportedOperationException} if the underlying collection doesn't
 * support that method.
 */
public interface Collection<E> extends Iterable<E> {

    /**
     * Attempts to add {@code object} to the contents of this
     * {@code Collection} (optional).
     *
     * After this method finishes successfully it is guaranteed that the object
     * is contained in the collection.
     *
     * If the collection was modified it returns {@code true}, {@code false} if
     * no changes were made.
     *
     * An implementation of {@code Collection} may narrow the set of accepted
     * objects, but it has to specify this in the documentation. If the object
     * to be added does not meet this restriction, then an
     * {@code IllegalArgumentException} is thrown.
     *
     * If a collection does not yet contain an object that is to be added and
     * adding the object fails, this method <i>must</i> throw an appropriate
     * unchecked Exception. Returning false is not permitted in this case
     * because it would violate the postcondition that the element will be part
     * of the collection after this method finishes.
     *
     * @param object
     *            the object to add.
     * @return {@code true} if this {@code Collection} is
     *         modified, {@code false} otherwise.
     *
     * @throws UnsupportedOperationException
     *                if adding to this {@code Collection} is not supported.
     * @throws ClassCastException
     *                if the class of the object is inappropriate for this
     *                collection.
     * @throws IllegalArgumentException
     *                if the object cannot be added to this {@code Collection}.
     * @throws NullPointerException
     *                if null elements cannot be added to the {@code Collection}.
     */
    public boolean add(E object);

    /**
     * Attempts to add all of the objects contained in {@code Collection}
     * to the contents of this {@code Collection} (optional). If the passed {@code Collection}
     * is changed during the process of adding elements to this {@code Collection}, the
     * behavior is not defined.
     *
     * @param collection
     *            the {@code Collection} of objects.
     * @return {@code true} if this {@code Collection} is modified, {@code false}
     *         otherwise.
     * @throws UnsupportedOperationException
     *                if adding to this {@code Collection} is not supported.
     * @throws ClassCastException
     *                if the class of an object is inappropriate for this
     *                {@code Collection}.
     * @throws IllegalArgumentException
     *                if an object cannot be added to this {@code Collection}.
     * @throws NullPointerException
     *                if {@code collection} is {@code null}, or if it
     *                contains {@code null} elements and this {@code Collection} does
     *                not support such elements.
     */
    public boolean addAll(Collection<? extends E> collection);

    /**
     * Removes all elements from this {@code Collection}, leaving it empty (optional).
     *
     * @throws UnsupportedOperationException
     *                if removing from this {@code Collection} is not supported.
     *
     * @see #isEmpty
     * @see #size
     */
    public void clear();

    /**
     * Tests whether this {@code Collection} contains the specified object. Returns
     * {@code true} if and only if at least one element {@code elem} in this
     * {@code Collection} meets following requirement:
     * {@code (object==null ? elem==null : object.equals(elem))}.
     *
     * @param object
     *            the object to search for.
     * @return {@code true} if object is an element of this {@code Collection},
     *         {@code false} otherwise.
     * @throws ClassCastException
     *                if the object to look for isn't of the correct
     *                type.
     * @throws NullPointerException
     *                if the object to look for is {@code null} and this
     *                {@code Collection} doesn't support {@code null} elements.
     */
    public boolean contains(Object object);

    /**
     * Tests whether this {@code Collection} contains all objects contained in the
     * specified {@code Collection}. If an element {@code elem} is contained several
     * times in the specified {@code Collection}, the method returns {@code true} even
     * if {@code elem} is contained only once in this {@code Collection}.
     *
     * @param collection
     *            the collection of objects.
     * @return {@code true} if all objects in the specified {@code Collection} are
     *         elements of this {@code Collection}, {@code false} otherwise.
     * @throws ClassCastException
     *                if one or more elements of {@code collection} isn't of the
     *                correct type.
     * @throws NullPointerException
     *                if {@code collection} contains at least one {@code null}
     *                element and this {@code Collection} doesn't support {@code null}
     *                elements.
     * @throws NullPointerException
     *                if {@code collection} is {@code null}.
     */
    public boolean containsAll(Collection<?> collection);

    /**
     * Compares the argument to the receiver, and returns true if they represent
     * the <em>same</em> object using a class specific comparison.
     *
     * @param object
     *            the object to compare with this object.
     * @return {@code true} if the object is the same as this object and
     *         {@code false} if it is different from this object.
     * @see #hashCode
     */
    public boolean equals(Object object);

    /**
     * Returns an integer hash code for the receiver. Objects which are equal
     * return the same value for this method.
     *
     * @return the receiver's hash.
     *
     * @see #equals
     */
    public int hashCode();

    /**
     * Returns if this {@code Collection} contains no elements.
     *
     * @return {@code true} if this {@code Collection} has no elements, {@code false}
     *         otherwise.
     *
     * @see #size
     */
    public boolean isEmpty();

    /**
     * Returns an instance of {@link Iterator} that may be used to access the
     * objects contained by this {@code Collection}. The order in which the elements are
     * returned by the iterator is not defined. Only if the instance of the
     * {@code Collection} has a defined order the elements are returned in that order.
     *
     * @return an iterator for accessing the {@code Collection} contents.
     */
    public Iterator<E> iterator();

    /**
     * Removes one instance of the specified object from this {@code Collection} if one
     * is contained (optional). The element {@code elem} that is removed
     * complies with {@code (object==null ? elem==null : object.equals(elem)}.
     *
     * @param object
     *            the object to remove.
     * @return {@code true} if this {@code Collection} is modified, {@code false}
     *         otherwise.
     * @throws UnsupportedOperationException
     *                if removing from this {@code Collection} is not supported.
     * @throws ClassCastException
     *                if the object passed is not of the correct type.
     * @throws NullPointerException
     *                if {@code object} is {@code null} and this {@code Collection}
     *                doesn't support {@code null} elements.
     */
    public boolean remove(Object object);

    /**
     * Removes all occurrences in this {@code Collection} of each object in the
     * specified {@code Collection} (optional). After this method returns none of the
     * elements in the passed {@code Collection} can be found in this {@code Collection}
     * anymore.
     *
     * @param collection
     *            the collection of objects to remove.
     * @return {@code true} if this {@code Collection} is modified, {@code false}
     *         otherwise.
     *
     * @throws UnsupportedOperationException
     *                if removing from this {@code Collection} is not supported.
     * @throws ClassCastException
     *                if one or more elements of {@code collection}
     *                isn't of the correct type.
     * @throws NullPointerException
     *                if {@code collection} contains at least one
     *                {@code null} element and this {@code Collection} doesn't support
     *                {@code null} elements.
     * @throws NullPointerException
     *                if {@code collection} is {@code null}.
     */
    public boolean removeAll(Collection<?> collection);

    /**
     * Removes all objects from this {@code Collection} that are not also found in the
     * {@code Collection} passed (optional). After this method returns this {@code Collection}
     * will only contain elements that also can be found in the {@code Collection}
     * passed to this method.
     *
     * @param collection
     *            the collection of objects to retain.
     * @return {@code true} if this {@code Collection} is modified, {@code false}
     *         otherwise.
     * @throws UnsupportedOperationException
     *                if removing from this {@code Collection} is not supported.
     * @throws ClassCastException
     *                if one or more elements of {@code collection}
     *                isn't of the correct type.
     * @throws NullPointerException
     *                if {@code collection} contains at least one
     *                {@code null} element and this {@code Collection} doesn't support
     *                {@code null} elements.
     * @throws NullPointerException
     *                if {@code collection} is {@code null}.
     */
    public boolean retainAll(Collection<?> collection);

    /**
     * Returns a count of how many objects this {@code Collection} contains.
     *
     * @return how many objects this {@code Collection} contains, or Integer.MAX_VALUE
     *         if there are more than Integer.MAX_VALUE elements in this
     *         {@code Collection}.
     */
    public int size();

    /**
     * Returns a new array containing all elements contained in this {@code Collection}.
     *
     * If the implementation has ordered elements it will return the element
     * array in the same order as an iterator would return them.
     *
     * The array returned does not reflect any changes of the {@code Collection}. A new
     * array is created even if the underlying data structure is already an
     * array.
     *
     * @return an array of the elements from this {@code Collection}.
     */
    public Object[] toArray();

    /**
     * Returns an array containing all elements contained in this {@code Collection}. If
     * the specified array is large enough to hold the elements, the specified
     * array is used, otherwise an array of the same type is created. If the
     * specified array is used and is larger than this {@code Collection}, the array
     * element following the {@code Collection} elements is set to null.
     *
     * If the implementation has ordered elements it will return the element
     * array in the same order as an iterator would return them.
     *
     * {@code toArray(new Object[0])} behaves exactly the same way as
     * {@code toArray()} does.
     *
     * @param array
     *            the array.
     * @return an array of the elements from this {@code Collection}.
     *
     * @throws ArrayStoreException
     *                if the type of an element in this {@code Collection} cannot be
     *                stored in the type of the specified array.
     */
    public <T> T[] toArray(T[] array);
}
