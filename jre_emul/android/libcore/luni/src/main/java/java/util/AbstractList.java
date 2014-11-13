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
 * {@code AbstractList} is an abstract implementation of the {@code List} interface, optimized
 * for a backing store which supports random access. This implementation does
 * not support adding or replacing. A subclass must implement the abstract
 * methods {@code get()} and {@code size()}, and to create a
 * modifiable {@code List} it's necessary to override the {@code add()} method that
 * currently throws an {@code UnsupportedOperationException}.
 *
 * @since 1.2
 */
public abstract class AbstractList<E> extends AbstractCollection<E> implements List<E> {

    /**
     * A counter for changes to the list.
     */
    protected transient int modCount;

    private class SimpleListIterator implements Iterator<E> {
        int pos = -1;

        int expectedModCount;

        int lastPosition = -1;

        SimpleListIterator() {
            expectedModCount = modCount;
        }

        public boolean hasNext() {
            return pos + 1 < size();
        }

        public E next() {
            if (expectedModCount == modCount) {
                try {
                    E result = get(pos + 1);
                    lastPosition = ++pos;
                    return result;
                } catch (IndexOutOfBoundsException e) {
                    throw new NoSuchElementException();
                }
            }
            throw new ConcurrentModificationException();
        }

        public void remove() {
            if (this.lastPosition == -1) {
                throw new IllegalStateException();
            }

            if (expectedModCount != modCount) {
                throw new ConcurrentModificationException();
            }

            try {
                AbstractList.this.remove(lastPosition);
            } catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }

            expectedModCount = modCount;
            if (pos == lastPosition) {
                pos--;
            }
            lastPosition = -1;
        }
    }

    private final class FullListIterator extends SimpleListIterator implements ListIterator<E> {
        FullListIterator(int start) {
            if (start >= 0 && start <= size()) {
                pos = start - 1;
            } else {
                throw new IndexOutOfBoundsException();
            }
        }

        public void add(E object) {
            if (expectedModCount == modCount) {
                try {
                    AbstractList.this.add(pos + 1, object);
                } catch (IndexOutOfBoundsException e) {
                    throw new NoSuchElementException();
                }
                pos++;
                lastPosition = -1;
                if (modCount != expectedModCount) {
                    expectedModCount = modCount;
                }
            } else {
                throw new ConcurrentModificationException();
            }
        }

        public boolean hasPrevious() {
            return pos >= 0;
        }

        public int nextIndex() {
            return pos + 1;
        }

        public E previous() {
            if (expectedModCount == modCount) {
                try {
                    E result = get(pos);
                    lastPosition = pos;
                    pos--;
                    return result;
                } catch (IndexOutOfBoundsException e) {
                    throw new NoSuchElementException();
                }
            }
            throw new ConcurrentModificationException();
        }

        public int previousIndex() {
            return pos;
        }

        public void set(E object) {
            if (expectedModCount == modCount) {
                try {
                    AbstractList.this.set(lastPosition, object);
                } catch (IndexOutOfBoundsException e) {
                    throw new IllegalStateException();
                }
            } else {
                throw new ConcurrentModificationException();
            }
        }
    }

    private static final class SubAbstractListRandomAccess<E> extends
            SubAbstractList<E> implements RandomAccess {
        SubAbstractListRandomAccess(AbstractList<E> list, int start, int end) {
            super(list, start, end);
        }
    }

    private static class SubAbstractList<E> extends AbstractList<E> {
        private final AbstractList<E> fullList;

        private int offset;

        private int size;

        private static final class SubAbstractListIterator<E> implements
                ListIterator<E> {
            private final SubAbstractList<E> subList;

            private final ListIterator<E> iterator;

            private int start;

            private int end;

            SubAbstractListIterator(ListIterator<E> it,
                    SubAbstractList<E> list, int offset, int length) {
                iterator = it;
                subList = list;
                start = offset;
                end = start + length;
            }

            public void add(E object) {
                iterator.add(object);
                subList.sizeChanged(true);
                end++;
            }

            public boolean hasNext() {
                return iterator.nextIndex() < end;
            }

            public boolean hasPrevious() {
                return iterator.previousIndex() >= start;
            }

            public E next() {
                if (iterator.nextIndex() < end) {
                    return iterator.next();
                }
                throw new NoSuchElementException();
            }

            public int nextIndex() {
                return iterator.nextIndex() - start;
            }

            public E previous() {
                if (iterator.previousIndex() >= start) {
                    return iterator.previous();
                }
                throw new NoSuchElementException();
            }

            public int previousIndex() {
                int previous = iterator.previousIndex();
                if (previous >= start) {
                    return previous - start;
                }
                return -1;
            }

            public void remove() {
                iterator.remove();
                subList.sizeChanged(false);
                end--;
            }

            public void set(E object) {
                iterator.set(object);
            }
        }

        SubAbstractList(AbstractList<E> list, int start, int end) {
            fullList = list;
            modCount = fullList.modCount;
            offset = start;
            size = end - start;
        }

        @Override
        public void add(int location, E object) {
            if (modCount == fullList.modCount) {
                if (location >= 0 && location <= size) {
                    fullList.add(location + offset, object);
                    size++;
                    modCount = fullList.modCount;
                } else {
                    throw new IndexOutOfBoundsException();
                }
            } else {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public boolean addAll(int location, Collection<? extends E> collection) {
            if (modCount == fullList.modCount) {
                if (location >= 0 && location <= size) {
                    boolean result = fullList.addAll(location + offset,
                            collection);
                    if (result) {
                        size += collection.size();
                        modCount = fullList.modCount;
                    }
                    return result;
                }
                throw new IndexOutOfBoundsException();
            }
            throw new ConcurrentModificationException();
        }

        @Override
        public boolean addAll(Collection<? extends E> collection) {
            if (modCount == fullList.modCount) {
                boolean result = fullList.addAll(offset + size, collection);
                if (result) {
                    size += collection.size();
                    modCount = fullList.modCount;
                }
                return result;
            }
            throw new ConcurrentModificationException();
        }

        @Override
        public E get(int location) {
            if (modCount == fullList.modCount) {
                if (location >= 0 && location < size) {
                    return fullList.get(location + offset);
                }
                throw new IndexOutOfBoundsException();
            }
            throw new ConcurrentModificationException();
        }

        @Override
        public Iterator<E> iterator() {
            return listIterator(0);
        }

        @Override
        public ListIterator<E> listIterator(int location) {
            if (modCount == fullList.modCount) {
                if (location >= 0 && location <= size) {
                    return new SubAbstractListIterator<E>(fullList
                            .listIterator(location + offset), this, offset,
                            size);
                }
                throw new IndexOutOfBoundsException();
            }
            throw new ConcurrentModificationException();
        }

        @Override
        public E remove(int location) {
            if (modCount == fullList.modCount) {
                if (location >= 0 && location < size) {
                    E result = fullList.remove(location + offset);
                    size--;
                    modCount = fullList.modCount;
                    return result;
                }
                throw new IndexOutOfBoundsException();
            }
            throw new ConcurrentModificationException();
        }

        @Override
        protected void removeRange(int start, int end) {
            if (start != end) {
                if (modCount == fullList.modCount) {
                    fullList.removeRange(start + offset, end + offset);
                    size -= end - start;
                    modCount = fullList.modCount;
                } else {
                    throw new ConcurrentModificationException();
                }
            }
        }

        @Override
        public E set(int location, E object) {
            if (modCount == fullList.modCount) {
                if (location >= 0 && location < size) {
                    return fullList.set(location + offset, object);
                }
                throw new IndexOutOfBoundsException();
            }
            throw new ConcurrentModificationException();
        }

        @Override
        public int size() {
            if (modCount == fullList.modCount) {
                return size;
            }
            throw new ConcurrentModificationException();
        }

        void sizeChanged(boolean increment) {
            if (increment) {
                size++;
            } else {
                size--;
            }
            modCount = fullList.modCount;
        }
    }

    /**
     * Constructs a new instance of this AbstractList.
     */
    protected AbstractList() {
    }

    /**
     * Inserts the specified object into this List at the specified location.
     * The object is inserted before any previous element at the specified
     * location. If the location is equal to the size of this List, the object
     * is added at the end.
     * <p>
     * Concrete implementations that would like to support the add functionality
     * must override this method.
     *
     * @param location
     *            the index at which to insert.
     * @param object
     *            the object to add.
     *
     * @throws UnsupportedOperationException
     *                if adding to this List is not supported.
     * @throws ClassCastException
     *                if the class of the object is inappropriate for this
     *                List
     * @throws IllegalArgumentException
     *                if the object cannot be added to this List
     * @throws IndexOutOfBoundsException
     *                if {@code location < 0 || location > size()}
     */
    public void add(int location, E object) {
        throw new UnsupportedOperationException();
    }

    /**
     * Adds the specified object at the end of this List.
     *
     *
     * @param object
     *            the object to add
     * @return true
     *
     * @throws UnsupportedOperationException
     *                if adding to this List is not supported
     * @throws ClassCastException
     *                if the class of the object is inappropriate for this
     *                List
     * @throws IllegalArgumentException
     *                if the object cannot be added to this List
     */
    @Override
    public boolean add(E object) {
        add(size(), object);
        return true;
    }

    /**
     * Inserts the objects in the specified Collection at the specified location
     * in this List. The objects are added in the order they are returned from
     * the collection's iterator.
     *
     * @param location
     *            the index at which to insert.
     * @param collection
     *            the Collection of objects
     * @return {@code true} if this List is modified, {@code false} otherwise.
     * @throws UnsupportedOperationException
     *             if adding to this list is not supported.
     * @throws ClassCastException
     *             if the class of an object is inappropriate for this list.
     * @throws IllegalArgumentException
     *             if an object cannot be added to this list.
     * @throws IndexOutOfBoundsException
     *             if {@code location < 0 || location > size()}
     */
    public boolean addAll(int location, Collection<? extends E> collection) {
        Iterator<? extends E> it = collection.iterator();
        while (it.hasNext()) {
            add(location++, it.next());
        }
        return !collection.isEmpty();
    }

    /**
     * Removes all elements from this list, leaving it empty.
     *
     * @throws UnsupportedOperationException
     *             if removing from this list is not supported.
     * @see List#isEmpty
     * @see List#size
     */
    @Override
    public void clear() {
        removeRange(0, size());
    }

    /**
     * Compares the specified object to this list and return true if they are
     * equal. Two lists are equal when they both contain the same objects in the
     * same order.
     *
     * @param object
     *            the object to compare to this object.
     * @return {@code true} if the specified object is equal to this list,
     *         {@code false} otherwise.
     * @see #hashCode
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof List) {
            List<?> list = (List<?>) object;
            if (list.size() != size()) {
                return false;
            }

            Iterator<?> it1 = iterator(), it2 = list.iterator();
            while (it1.hasNext()) {
                Object e1 = it1.next(), e2 = it2.next();
                if (!(e1 == null ? e2 == null : e1.equals(e2))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the element at the specified location in this list.
     *
     * @param location
     *            the index of the element to return.
     * @return the element at the specified index.
     * @throws IndexOutOfBoundsException
     *             if {@code location < 0 || location >= size()}
     */
    public abstract E get(int location);

    /**
     * Returns the hash code of this list. The hash code is calculated by taking
     * each element's hashcode into account.
     *
     * @return the hash code.
     * @see #equals
     * @see List#hashCode()
     */
    @Override
    public int hashCode() {
        int result = 1;
        Iterator<?> it = iterator();
        while (it.hasNext()) {
            Object object = it.next();
            result = (31 * result) + (object == null ? 0 : object.hashCode());
        }
        return result;
    }

    /**
     * Searches this list for the specified object and returns the index of the
     * first occurrence.
     *
     * @param object
     *            the object to search for.
     * @return the index of the first occurrence of the object, or -1 if it was
     *         not found.
     */
    public int indexOf(Object object) {
        ListIterator<?> it = listIterator();
        if (object != null) {
            while (it.hasNext()) {
                if (object.equals(it.next())) {
                    return it.previousIndex();
                }
            }
        } else {
            while (it.hasNext()) {
                if (it.next() == null) {
                    return it.previousIndex();
                }
            }
        }
        return -1;
    }

    /**
     * Returns an iterator on the elements of this list. The elements are
     * iterated in the same order as they occur in the list.
     *
     * @return an iterator on the elements of this list.
     * @see Iterator
     */
    @Override
    public Iterator<E> iterator() {
        return new SimpleListIterator();
    }

    /**
     * Searches this list for the specified object and returns the index of the
     * last occurrence.
     *
     * @param object
     *            the object to search for.
     * @return the index of the last occurrence of the object, or -1 if the
     *         object was not found.
     */
    public int lastIndexOf(Object object) {
        ListIterator<?> it = listIterator(size());
        if (object != null) {
            while (it.hasPrevious()) {
                if (object.equals(it.previous())) {
                    return it.nextIndex();
                }
            }
        } else {
            while (it.hasPrevious()) {
                if (it.previous() == null) {
                    return it.nextIndex();
                }
            }
        }
        return -1;
    }

    /**
     * Returns a ListIterator on the elements of this list. The elements are
     * iterated in the same order that they occur in the list.
     *
     * @return a ListIterator on the elements of this list
     * @see ListIterator
     */
    public ListIterator<E> listIterator() {
        return listIterator(0);
    }

    /**
     * Returns a list iterator on the elements of this list. The elements are
     * iterated in the same order as they occur in the list. The iteration
     * starts at the specified location.
     *
     * @param location
     *            the index at which to start the iteration.
     * @return a ListIterator on the elements of this list.
     * @throws IndexOutOfBoundsException
     *             if {@code location < 0 || location > size()}
     * @see ListIterator
     */
    public ListIterator<E> listIterator(int location) {
        return new FullListIterator(location);
    }

    /**
     * Removes the object at the specified location from this list.
     *
     * @param location
     *            the index of the object to remove.
     * @return the removed object.
     * @throws UnsupportedOperationException
     *             if removing from this list is not supported.
     * @throws IndexOutOfBoundsException
     *             if {@code location < 0 || location >= size()}
     */
    public E remove(int location) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes the objects in the specified range from the start to the end
     * index minus one.
     *
     * @param start
     *            the index at which to start removing.
     * @param end
     *            the index after the last element to remove.
     * @throws UnsupportedOperationException
     *             if removing from this list is not supported.
     * @throws IndexOutOfBoundsException
     *             if {@code start < 0} or {@code start >= size()}.
     */
    protected void removeRange(int start, int end) {
        Iterator<?> it = listIterator(start);
        for (int i = start; i < end; i++) {
            it.next();
            it.remove();
        }
    }

    /**
     * Replaces the element at the specified location in this list with the
     * specified object.
     *
     * @param location
     *            the index at which to put the specified object.
     * @param object
     *            the object to add.
     * @return the previous element at the index.
     * @throws UnsupportedOperationException
     *             if replacing elements in this list is not supported.
     * @throws ClassCastException
     *             if the class of an object is inappropriate for this list.
     * @throws IllegalArgumentException
     *             if an object cannot be added to this list.
     * @throws IndexOutOfBoundsException
     *             if {@code location < 0 || location >= size()}
     */
    public E set(int location, E object) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a part of consecutive elements of this list as a view. The
     * returned view will be of zero length if start equals end. Any change that
     * occurs in the returned subList will be reflected to the original list,
     * and vice-versa. All the supported optional operations by the original
     * list will also be supported by this subList.
     * <p>
     * This method can be used as a handy method to do some operations on a sub
     * range of the original list, for example
     * {@code list.subList(from, to).clear();}
     * <p>
     * If the original list is modified in other ways than through the returned
     * subList, the behavior of the returned subList becomes undefined.
     * <p>
     * The returned subList is a subclass of AbstractList. The subclass stores
     * offset, size of itself, and modCount of the original list. If the
     * original list implements RandomAccess interface, the returned subList
     * also implements RandomAccess interface.
     * <p>
     * The subList's set(int, Object), get(int), add(int, Object), remove(int),
     * addAll(int, Collection) and removeRange(int, int) methods first check the
     * bounds, adjust offsets and then call the corresponding methods of the
     * original AbstractList. addAll(Collection c) method of the returned
     * subList calls the original addAll(offset + size, c).
     * <p>
     * The listIterator(int) method of the subList wraps the original list
     * iterator. The iterator() method of the subList invokes the original
     * listIterator() method, and the size() method merely returns the size of
     * the subList.
     * <p>
     * All methods will throw a ConcurrentModificationException if the modCount
     * of the original list is not equal to the expected value.
     *
     * @param start
     *            start index of the subList (inclusive).
     * @param end
     *            end index of the subList, (exclusive).
     * @return a subList view of this list starting from {@code start}
     *         (inclusive), and ending with {@code end} (exclusive)
     * @throws IndexOutOfBoundsException
     *             if (start < 0 || end > size())
     * @throws IllegalArgumentException
     *             if (start > end)
     */
    public List<E> subList(int start, int end) {
        if (start >= 0 && end <= size()) {
            if (start <= end) {
                if (this instanceof RandomAccess) {
                    return new SubAbstractListRandomAccess<E>(this, start, end);
                }
                return new SubAbstractList<E>(this, start, end);
            }
            throw new IllegalArgumentException();
        }
        throw new IndexOutOfBoundsException();
    }
}
