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
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;

/**
 * Vector is an implementation of {@link List}, backed by an array and synchronized.
 * All optional operations including adding, removing, and replacing elements are supported.
 *
 * <p>All elements are permitted, including null.
 *
 * <p>This class is equivalent to {@link ArrayList} with synchronized operations. This has a
 * performance cost, and the synchronization is not necessarily meaningful to your application:
 * synchronizing each call to {@code get}, for example, is not equivalent to synchronizing on the
 * list and iterating over it (which is probably what you intended). If you do need very highly
 * concurrent access, you should also consider {@link java.util.concurrent.CopyOnWriteArrayList}.
 *
 * @param <E> The element type of this list.
 */
public class Vector<E> extends AbstractList<E> implements List<E>,
        RandomAccess, Cloneable, Serializable {

    private static final long serialVersionUID = -2767605614048989439L;

    /**
     * The number of elements or the size of the vector.
     */
    protected int elementCount;

    /**
     * The elements of the vector.
     */
    protected Object[] elementData;

    /**
     * How many elements should be added to the vector when it is detected that
     * it needs to grow to accommodate extra entries. If this value is zero or
     * negative the size will be doubled if an increase is needed.
     */
    protected int capacityIncrement;

    private static final int DEFAULT_SIZE = 10;

    /**
     * Constructs a new vector using the default capacity.
     */
    public Vector() {
        this(DEFAULT_SIZE, 0);
    }

    /**
     * Constructs a new vector using the specified capacity.
     *
     * @param capacity
     *            the initial capacity of the new vector.
     * @throws IllegalArgumentException
     *             if {@code capacity} is negative.
     */
    public Vector(int capacity) {
        this(capacity, 0);
    }

    /**
     * Constructs a new vector using the specified capacity and capacity
     * increment.
     *
     * @param capacity
     *            the initial capacity of the new vector.
     * @param capacityIncrement
     *            the amount to increase the capacity when this vector is full.
     * @throws IllegalArgumentException
     *             if {@code capacity} is negative.
     */
    public Vector(int capacity, int capacityIncrement) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity < 0: " + capacity);
        }
        elementData = newElementArray(capacity);
        elementCount = 0;
        this.capacityIncrement = capacityIncrement;
    }

    /**
     * Constructs a new instance of {@code Vector} containing the elements in
     * {@code collection}. The order of the elements in the new {@code Vector}
     * is dependent on the iteration order of the seed collection.
     *
     * @param collection
     *            the collection of elements to add.
     */
    public Vector(Collection<? extends E> collection) {
        this(collection.size(), 0);
        Iterator<? extends E> it = collection.iterator();
        while (it.hasNext()) {
            elementData[elementCount++] = it.next();
        }
    }

    @SuppressWarnings("unchecked")
    private E[] newElementArray(int size) {
        return (E[]) new Object[size];
    }

    /**
     * Adds the specified object into this vector at the specified location. The
     * object is inserted before any element with the same or a higher index
     * increasing their index by 1. If the location is equal to the size of this
     * vector, the object is added at the end.
     *
     * @param location
     *            the index at which to insert the element.
     * @param object
     *            the object to insert in this vector.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code location < 0 || location > size()}.
     * @see #addElement
     * @see #size
     */
    @Override
    public void add(int location, E object) {
        insertElementAt(object, location);
    }

    /**
     * Adds the specified object at the end of this vector.
     *
     * @param object
     *            the object to add to the vector.
     * @return {@code true}
     */
    @Override
    public synchronized boolean add(E object) {
        if (elementCount == elementData.length) {
            growByOne();
        }
        elementData[elementCount++] = object;
        modCount++;
        return true;
    }

    /**
     * Inserts the objects in the specified collection at the specified location
     * in this vector. The objects are inserted in the order in which they are
     * returned from the Collection iterator. The elements with an index equal
     * or higher than {@code location} have their index increased by the size of
     * the added collection.
     *
     * @param location
     *            the location to insert the objects.
     * @param collection
     *            the collection of objects.
     * @return {@code true} if this vector is modified, {@code false} otherwise.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code location < 0} or {@code location > size()}.
     */
    @Override
    public synchronized boolean addAll(int location, Collection<? extends E> collection) {
        if (location >= 0 && location <= elementCount) {
            int size = collection.size();
            if (size == 0) {
                return false;
            }
            int required = size - (elementData.length - elementCount);
            if (required > 0) {
                growBy(required);
            }
            int count = elementCount - location;
            if (count > 0) {
                System.arraycopy(elementData, location, elementData, location
                        + size, count);
            }
            Iterator<? extends E> it = collection.iterator();
            while (it.hasNext()) {
                elementData[location++] = it.next();
            }
            elementCount += size;
            modCount++;
            return true;
        }
        throw arrayIndexOutOfBoundsException(location, elementCount);
    }

    /**
     * Adds the objects in the specified collection to the end of this vector.
     *
     * @param collection
     *            the collection of objects.
     * @return {@code true} if this vector is modified, {@code false} otherwise.
     */
    @Override
    public synchronized boolean addAll(Collection<? extends E> collection) {
        return addAll(elementCount, collection);
    }

    /**
     * Adds the specified object at the end of this vector.
     *
     * @param object
     *            the object to add to the vector.
     */
    public synchronized void addElement(E object) {
        if (elementCount == elementData.length) {
            growByOne();
        }
        elementData[elementCount++] = object;
        modCount++;
    }

    /**
     * Returns the number of elements this vector can hold without growing.
     *
     * @return the capacity of this vector.
     * @see #ensureCapacity
     * @see #size
     */
    public synchronized int capacity() {
        return elementData.length;
    }

    /**
     * Removes all elements from this vector, leaving it empty.
     *
     * @see #isEmpty
     * @see #size
     */
    @Override
    public void clear() {
        removeAllElements();
    }

    /**
     * Returns a new vector with the same elements, size, capacity and capacity
     * increment as this vector.
     *
     * @return a shallow copy of this vector.
     * @see java.lang.Cloneable
     */
    @Override
    @SuppressWarnings("unchecked")
    public synchronized Object clone() {
        try {
            Vector<E> vector = (Vector<E>) super.clone();
            vector.elementData = elementData.clone();
            return vector;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Searches this vector for the specified object.
     *
     * @param object
     *            the object to look for in this vector.
     * @return {@code true} if object is an element of this vector,
     *         {@code false} otherwise.
     * @see #indexOf(Object)
     * @see #indexOf(Object, int)
     * @see java.lang.Object#equals
     */
    @Override
    public boolean contains(Object object) {
        return indexOf(object, 0) != -1;
    }

    /**
     * Searches this vector for all objects in the specified collection.
     *
     * @param collection
     *            the collection of objects.
     * @return {@code true} if all objects in the specified collection are
     *         elements of this vector, {@code false} otherwise.
     */
    @Override
    public synchronized boolean containsAll(Collection<?> collection) {
        return super.containsAll(collection);
    }

    /**
     * Attempts to copy elements contained by this {@code Vector} into the
     * corresponding elements of the supplied {@code Object} array.
     *
     * @param elements
     *            the {@code Object} array into which the elements of this
     *            vector are copied.
     * @throws IndexOutOfBoundsException
     *             if {@code elements} is not big enough.
     * @see #clone
     */
    public synchronized void copyInto(Object[] elements) {
        System.arraycopy(elementData, 0, elements, 0, elementCount);
    }

    /**
     * Returns the element at the specified location in this vector.
     *
     * @param location
     *            the index of the element to return in this vector.
     * @return the element at the specified location.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code location < 0 || location >= size()}.
     * @see #size
     */
    @SuppressWarnings("unchecked")
    public synchronized E elementAt(int location) {
        if (location < elementCount) {
            return (E) elementData[location];
        }
        throw arrayIndexOutOfBoundsException(location, elementCount);
    }

    /**
     * Returns an enumeration on the elements of this vector. The results of the
     * enumeration may be affected if the contents of this vector is modified.
     *
     * @return an enumeration of the elements of this vector.
     * @see #elementAt
     * @see Enumeration
     */
    public Enumeration<E> elements() {
        return new Enumeration<E>() {
            int pos = 0;

            public boolean hasMoreElements() {
                return pos < elementCount;
            }

            @SuppressWarnings("unchecked")
            public E nextElement() {
                synchronized (Vector.this) {
                    if (pos < elementCount) {
                        return (E) elementData[pos++];
                    }
                }
                throw new NoSuchElementException();
            }
        };
    }

    /**
     * Ensures that this vector can hold the specified number of elements
     * without growing.
     *
     * @param minimumCapacity
     *            the minimum number of elements that this vector will hold
     *            before growing.
     * @see #capacity
     */
    public synchronized void ensureCapacity(int minimumCapacity) {
        if (elementData.length < minimumCapacity) {
            int next = (capacityIncrement <= 0 ? elementData.length
                    : capacityIncrement)
                    + elementData.length;
            grow(minimumCapacity > next ? minimumCapacity : next);
        }
    }

    /**
     * Compares the specified object to this vector and returns if they are
     * equal. The object must be a List which contains the same objects in the
     * same order.
     *
     * @param object
     *            the object to compare with this object
     * @return {@code true} if the specified object is equal to this vector,
     *         {@code false} otherwise.
     * @see #hashCode
     */
    @Override
    public synchronized boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof List) {
            List<?> list = (List<?>) object;
            if (list.size() != elementCount) {
                return false;
            }

            int index = 0;
            Iterator<?> it = list.iterator();
            while (it.hasNext()) {
                Object e1 = elementData[index++], e2 = it.next();
                if (!(e1 == null ? e2 == null : e1.equals(e2))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the first element in this vector.
     *
     * @return the element at the first position.
     * @throws NoSuchElementException
     *                if this vector is empty.
     * @see #elementAt
     * @see #lastElement
     * @see #size
     */
    @SuppressWarnings("unchecked")
    public synchronized E firstElement() {
        if (elementCount > 0) {
            return (E) elementData[0];
        }
        throw new NoSuchElementException();
    }

    /**
     * Returns the element at the specified location in this vector.
     *
     * @param location
     *            the index of the element to return in this vector.
     * @return the element at the specified location.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code location < 0 || location >= size()}.
     * @see #size
     */
    @Override
    public E get(int location) {
        return elementAt(location);
    }

    private void grow(int newCapacity) {
        E[] newData = newElementArray(newCapacity);
        // Assumes elementCount is <= newCapacity
        // assert elementCount <= newCapacity;
        System.arraycopy(elementData, 0, newData, 0, elementCount);
        elementData = newData;
    }

    /**
     * JIT optimization
     */
    private void growByOne() {
        int adding = 0;
        if (capacityIncrement <= 0) {
            if ((adding = elementData.length) == 0) {
                adding = 1;
            }
        } else {
            adding = capacityIncrement;
        }

        E[] newData = newElementArray(elementData.length + adding);
        System.arraycopy(elementData, 0, newData, 0, elementCount);
        elementData = newData;
    }

    private void growBy(int required) {
        int adding = 0;
        if (capacityIncrement <= 0) {
            if ((adding = elementData.length) == 0) {
                adding = required;
            }
            while (adding < required) {
                adding += adding;
            }
        } else {
            adding = (required / capacityIncrement) * capacityIncrement;
            if (adding < required) {
                adding += capacityIncrement;
            }
        }
        E[] newData = newElementArray(elementData.length + adding);
        System.arraycopy(elementData, 0, newData, 0, elementCount);
        elementData = newData;
    }

    /**
     * Returns an integer hash code for the receiver. Objects which are equal
     * return the same value for this method.
     *
     * @return the receiver's hash.
     * @see #equals
     */
    @Override
    public synchronized int hashCode() {
        int result = 1;
        for (int i = 0; i < elementCount; i++) {
            result = (31 * result)
                    + (elementData[i] == null ? 0 : elementData[i].hashCode());
        }
        return result;
    }

    /**
     * Searches in this vector for the index of the specified object. The search
     * for the object starts at the beginning and moves towards the end of this
     * vector.
     *
     * @param object
     *            the object to find in this vector.
     * @return the index in this vector of the specified element, -1 if the
     *         element isn't found.
     * @see #contains
     * @see #lastIndexOf(Object)
     * @see #lastIndexOf(Object, int)
     */
    @Override
    public int indexOf(Object object) {
        return indexOf(object, 0);
    }

    /**
     * Searches in this vector for the index of the specified object. The search
     * for the object starts at the specified location and moves towards the end
     * of this vector.
     *
     * @param object
     *            the object to find in this vector.
     * @param location
     *            the index at which to start searching.
     * @return the index in this vector of the specified element, -1 if the
     *         element isn't found.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code location < 0}.
     * @see #contains
     * @see #lastIndexOf(Object)
     * @see #lastIndexOf(Object, int)
     */
    public synchronized int indexOf(Object object, int location) {
        if (object != null) {
            for (int i = location; i < elementCount; i++) {
                if (object.equals(elementData[i])) {
                    return i;
                }
            }
        } else {
            for (int i = location; i < elementCount; i++) {
                if (elementData[i] == null) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Inserts the specified object into this vector at the specified location.
     * This object is inserted before any previous element at the specified
     * location. All elements with an index equal or greater than
     * {@code location} have their index increased by 1. If the location is
     * equal to the size of this vector, the object is added at the end.
     *
     * @param object
     *            the object to insert in this vector.
     * @param location
     *            the index at which to insert the element.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code location < 0 || location > size()}.
     * @see #addElement
     * @see #size
     */
    public synchronized void insertElementAt(E object, int location) {
        if (location >= 0 && location <= elementCount) {
            if (elementCount == elementData.length) {
                growByOne();
            }
            int count = elementCount - location;
            if (count > 0) {
                System.arraycopy(elementData, location, elementData,
                        location + 1, count);
            }
            elementData[location] = object;
            elementCount++;
            modCount++;
        } else {
            throw arrayIndexOutOfBoundsException(location, elementCount);
        }
    }

    /**
     * Returns if this vector has no elements, a size of zero.
     *
     * @return {@code true} if this vector has no elements, {@code false}
     *         otherwise.
     * @see #size
     */
    @Override
    public synchronized boolean isEmpty() {
        return elementCount == 0;
    }

    /**
     * Returns the last element in this vector.
     *
     * @return the element at the last position.
     * @throws NoSuchElementException
     *                if this vector is empty.
     * @see #elementAt
     * @see #firstElement
     * @see #size
     */
    @SuppressWarnings("unchecked")
    public synchronized E lastElement() {
        try {
            return (E) elementData[elementCount - 1];
        } catch (IndexOutOfBoundsException e) {
            throw new NoSuchElementException();
        }
    }

    /**
     * Searches in this vector for the index of the specified object. The search
     * for the object starts at the end and moves towards the start of this
     * vector.
     *
     * @param object
     *            the object to find in this vector.
     * @return the index in this vector of the specified element, -1 if the
     *         element isn't found.
     * @see #contains
     * @see #indexOf(Object)
     * @see #indexOf(Object, int)
     */
    @Override
    public synchronized int lastIndexOf(Object object) {
        return lastIndexOf(object, elementCount - 1);
    }

    /**
     * Searches in this vector for the index of the specified object. The search
     * for the object starts at the specified location and moves towards the
     * start of this vector.
     *
     * @param object
     *            the object to find in this vector.
     * @param location
     *            the index at which to start searching.
     * @return the index in this vector of the specified element, -1 if the
     *         element isn't found.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code location >= size()}.
     * @see #contains
     * @see #indexOf(Object)
     * @see #indexOf(Object, int)
     */
    public synchronized int lastIndexOf(Object object, int location) {
        if (location < elementCount) {
            if (object != null) {
                for (int i = location; i >= 0; i--) {
                    if (object.equals(elementData[i])) {
                        return i;
                    }
                }
            } else {
                for (int i = location; i >= 0; i--) {
                    if (elementData[i] == null) {
                        return i;
                    }
                }
            }
            return -1;
        }
        throw arrayIndexOutOfBoundsException(location, elementCount);
    }

    /**
     * Removes the object at the specified location from this vector. All
     * elements with an index bigger than {@code location} have their index
     * decreased by 1.
     *
     * @param location
     *            the index of the object to remove.
     * @return the removed object.
     * @throws IndexOutOfBoundsException
     *                if {@code location < 0 || location >= size()}.
     */
    @SuppressWarnings("unchecked")
    @Override
    public synchronized E remove(int location) {
        if (location < elementCount) {
            E result = (E) elementData[location];
            elementCount--;
            int size = elementCount - location;
            if (size > 0) {
                System.arraycopy(elementData, location + 1, elementData,
                        location, size);
            }
            elementData[elementCount] = null;
            modCount++;
            return result;
        }
        throw arrayIndexOutOfBoundsException(location, elementCount);
    }

    /**
     * Removes the first occurrence, starting at the beginning and moving
     * towards the end, of the specified object from this vector. All elements
     * with an index bigger than the element that gets removed have their index
     * decreased by 1.
     *
     * @param object
     *            the object to remove from this vector.
     * @return {@code true} if the specified object was found, {@code false}
     *         otherwise.
     * @see #removeAllElements
     * @see #removeElementAt
     * @see #size
     */
    @Override
    public boolean remove(Object object) {
        return removeElement(object);
    }

    /**
     * Removes all occurrences in this vector of each object in the specified
     * Collection.
     *
     * @param collection
     *            the collection of objects to remove.
     * @return {@code true} if this vector is modified, {@code false} otherwise.
     * @see #remove(Object)
     * @see #contains(Object)
     */
    @Override
    public synchronized boolean removeAll(Collection<?> collection) {
        return super.removeAll(collection);
    }

    /**
     * Removes all elements from this vector, leaving the size zero and the
     * capacity unchanged.
     *
     * @see #isEmpty
     * @see #size
     */
    public synchronized void removeAllElements() {
        for (int i = 0; i < elementCount; i++) {
            elementData[i] = null;
        }
        modCount++;
        elementCount = 0;
    }

    /**
     * Removes the first occurrence, starting at the beginning and moving
     * towards the end, of the specified object from this vector. All elements
     * with an index bigger than the element that gets removed have their index
     * decreased by 1.
     *
     * @param object
     *            the object to remove from this vector.
     * @return {@code true} if the specified object was found, {@code false}
     *         otherwise.
     * @see #removeAllElements
     * @see #removeElementAt
     * @see #size
     */
    public synchronized boolean removeElement(Object object) {
        int index;
        if ((index = indexOf(object, 0)) == -1) {
            return false;
        }
        removeElementAt(index);
        return true;
    }

    /**
     * Removes the element found at index position {@code location} from
     * this {@code Vector}. All elements with an index bigger than
     * {@code location} have their index decreased by 1.
     *
     * @param location
     *            the index of the element to remove.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code location < 0 || location >= size()}.
     * @see #removeElement
     * @see #removeAllElements
     * @see #size
     */
    public synchronized void removeElementAt(int location) {
        if (location >= 0 && location < elementCount) {
            elementCount--;
            int size = elementCount - location;
            if (size > 0) {
                System.arraycopy(elementData, location + 1, elementData,
                        location, size);
            }
            elementData[elementCount] = null;
            modCount++;
        } else {
            throw arrayIndexOutOfBoundsException(location, elementCount);
        }
    }

    /**
     * Removes the objects in the specified range from the start to the, but not
     * including, end index. All elements with an index bigger than or equal to
     * {@code end} have their index decreased by {@code end - start}.
     *
     * @param start
     *            the index at which to start removing.
     * @param end
     *            the index one past the end of the range to remove.
     * @throws IndexOutOfBoundsException
     *                if {@code start < 0, start > end} or
     *                {@code end > size()}.
     */
    @Override
    protected void removeRange(int start, int end) {
        if (start >= 0 && start <= end && end <= elementCount) {
            if (start == end) {
                return;
            }
            if (end != elementCount) {
                System.arraycopy(elementData, end, elementData, start,
                        elementCount - end);
                int newCount = elementCount - (end - start);
                Arrays.fill(elementData, newCount, elementCount, null);
                elementCount = newCount;
            } else {
                Arrays.fill(elementData, start, elementCount, null);
                elementCount = start;
            }
            modCount++;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Removes all objects from this vector that are not contained in the
     * specified collection.
     *
     * @param collection
     *            the collection of objects to retain.
     * @return {@code true} if this vector is modified, {@code false} otherwise.
     * @see #remove(Object)
     */
    @Override
    public synchronized boolean retainAll(Collection<?> collection) {
        return super.retainAll(collection);
    }

    /**
     * Replaces the element at the specified location in this vector with the
     * specified object.
     *
     * @param location
     *            the index at which to put the specified object.
     * @param object
     *            the object to add to this vector.
     * @return the previous element at the location.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code location < 0 || location >= size()}.
     * @see #size
     */
    @SuppressWarnings("unchecked")
    @Override
    public synchronized E set(int location, E object) {
        if (location < elementCount) {
            E result = (E) elementData[location];
            elementData[location] = object;
            return result;
        }
        throw arrayIndexOutOfBoundsException(location, elementCount);
    }

    /**
     * Replaces the element at the specified location in this vector with the
     * specified object.
     *
     * @param object
     *            the object to add to this vector.
     * @param location
     *            the index at which to put the specified object.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code location < 0 || location >= size()}.
     * @see #size
     */
    public synchronized void setElementAt(E object, int location) {
        if (location < elementCount) {
            elementData[location] = object;
        } else {
            throw arrayIndexOutOfBoundsException(location, elementCount);
        }
    }

    /**
     * This method was extracted to encourage VM to inline callers.
     * TODO: when we have a VM that can actually inline, move the test in here too!
     */
    private static ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException(int index, int size) {
        throw new ArrayIndexOutOfBoundsException(size, index);
    }

    /**
     * Sets the size of this vector to the specified size. If there are more
     * than length elements in this vector, the elements at end are lost. If
     * there are less than length elements in the vector, the additional
     * elements contain null.
     *
     * @param length
     *            the new size of this vector.
     * @see #size
     */
    public synchronized void setSize(int length) {
        if (length == elementCount) {
            return;
        }
        ensureCapacity(length);
        if (elementCount > length) {
            Arrays.fill(elementData, length, elementCount, null);
        }
        elementCount = length;
        modCount++;
    }

    /**
     * Returns the number of elements in this vector.
     *
     * @return the number of elements in this vector.
     * @see #elementCount
     * @see #lastElement
     */
    @Override
    public synchronized int size() {
        return elementCount;
    }

    /**
     * Returns a List of the specified portion of this vector from the start
     * index to one less than the end index. The returned List is backed by this
     * vector so changes to one are reflected by the other.
     *
     * @param start
     *            the index at which to start the sublist.
     * @param end
     *            the index one past the end of the sublist.
     * @return a List of a portion of this vector.
     * @throws IndexOutOfBoundsException
     *                if {@code start < 0} or {@code end > size()}.
     * @throws IllegalArgumentException
     *                if {@code start > end}.
     */
    @Override
    public synchronized List<E> subList(int start, int end) {
        return new Collections.SynchronizedRandomAccessList<E>(super.subList(
                start, end), this);
    }

    /**
     * Returns a new array containing all elements contained in this vector.
     *
     * @return an array of the elements from this vector.
     */
    @Override
    public synchronized Object[] toArray() {
        Object[] result = new Object[elementCount];
        System.arraycopy(elementData, 0, result, 0, elementCount);
        return result;
    }

    /**
     * Returns an array containing all elements contained in this vector. If the
     * specified array is large enough to hold the elements, the specified array
     * is used, otherwise an array of the same type is created. If the specified
     * array is used and is larger than this vector, the array element following
     * the collection elements is set to null.
     *
     * @param contents
     *            the array to fill.
     * @return an array of the elements from this vector.
     * @throws ArrayStoreException
     *                if the type of an element in this vector cannot be
     *                stored in the type of the specified array.
     */
    @Override
    @SuppressWarnings("unchecked")
    public synchronized <T> T[] toArray(T[] contents) {
        if (elementCount > contents.length) {
            Class<?> ct = contents.getClass().getComponentType();
            contents = (T[]) Array.newInstance(ct, elementCount);
        }
        System.arraycopy(elementData, 0, contents, 0, elementCount);
        if (elementCount < contents.length) {
            contents[elementCount] = null;
        }
        return contents;
    }

    /**
     * Returns the string representation of this vector.
     *
     * @return the string representation of this vector.
     * @see #elements
     */
    @Override
    public synchronized String toString() {
        if (elementCount == 0) {
            return "[]";
        }
        int length = elementCount - 1;
        StringBuilder buffer = new StringBuilder(elementCount * 16);
        buffer.append('[');
        for (int i = 0; i < length; i++) {
            if (elementData[i] == this) {
                buffer.append("(this Collection)");
            } else {
                buffer.append(elementData[i]);
            }
            buffer.append(", ");
        }
        if (elementData[length] == this) {
            buffer.append("(this Collection)");
        } else {
            buffer.append(elementData[length]);
        }
        buffer.append(']');
        return buffer.toString();
    }

    /**
     * Sets the capacity of this vector to be the same as the size.
     *
     * @see #capacity
     * @see #ensureCapacity
     * @see #size
     */
    public synchronized void trimToSize() {
        if (elementData.length != elementCount) {
            grow(elementCount);
        }
    }

    private synchronized void writeObject(ObjectOutputStream stream)
            throws IOException {
        stream.defaultWriteObject();
    }
}
