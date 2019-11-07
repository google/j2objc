/*
 * SmartList.java
 *
 * Copyright (c) 2013 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.collections;

import com.strobel.annotations.NotNull;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A {@link List} that is optimised for the sizes of 0 and 1, in which cases no array is allocated.
 */
@SuppressWarnings({ "unchecked" })
public class SmartList<E> extends AbstractList<E> {
    //
    // Backing data for the list contents.  May be:
    //   1. `null` if _size == 0
    //   2. `E` if _size == 1
    //   3. `Object[]` if _size >= 2
    //
    private Object _data = null;

    private int _size = 0;

    public SmartList() {
    }

    public SmartList(final E element) {
        add(element);
    }

    public SmartList(@NotNull final java.util.Collection<? extends E> elements) {
        final int size = elements.size();

        if (size == 1) {
            final E element = elements instanceof List ? (E) ((List) elements).get(0)
                                                       : elements.iterator().next();
            add(element);
        }
        else if (size > 0) {
            _size = size;
            _data = elements.toArray(new Object[size]);
        }
    }

    public SmartList(@NotNull final E... elements) {
        if (elements.length == 1) {
            add(elements[0]);
        }
        else if (elements.length > 0) {
            _size = elements.length;
            _data = Arrays.copyOf(elements, _size);
        }
    }

    @Override
    public E get(final int index) {
        if (index < 0 || index >= _size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + _size);
        }

        if (_size == 1) {
            return (E) _data;
        }

        return (E) ((Object[]) _data)[index];
    }

    @Override
    public boolean add(final E e) {
        switch (_size) {
            case 0: {
                _data = e;
                break;
            }

            case 1: {
                final Object[] array = new Object[2];

                array[0] = _data;
                array[1] = e;

                _data = array;
                break;
            }

            default: {
                Object[] array = (Object[]) _data;
                final int oldCapacity = array.length;

                if (_size >= oldCapacity) {
                    //
                    // Resize the backing array.
                    //
                    int newCapacity = oldCapacity * 3 / 2 + 1;
                    final int minCapacity = _size + 1;

                    if (newCapacity < minCapacity) {
                        newCapacity = minCapacity;
                    }

                    final Object[] oldArray = array;

                    _data = array = new Object[newCapacity];

                    System.arraycopy(oldArray, 0, array, 0, oldCapacity);
                }

                array[_size] = e;
                break;
            }
        }

        _size++;
        modCount++;

        return true;
    }

    @Override
    public void add(final int index, final E e) {
        if (index < 0 || index > _size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + _size);
        }

        switch (_size) {
            case 0: {
                _data = e;
                break;
            }

            case 1: {
                if (index == 0) {
                    final Object[] array = new Object[2];
                    array[0] = e;
                    array[1] = _data;
                    _data = array;
                    break;
                }
                //
                // If index != 0, fall through to default.
                //
            }

            default: {
                final Object[] array = new Object[_size + 1];

                if (_size == 1) {
                    array[0] = _data; // index == 1
                }
                else {
                    final Object[] oldArray = (Object[]) _data;

                    System.arraycopy(oldArray, 0, array, 0, index);
                    System.arraycopy(oldArray, index, array, index + 1, _size - index);
                }

                array[index] = e;
                _data = array;
            }

        }

        _size++;
        modCount++;
    }

    @Override
    public int size() {
        return _size;
    }

    @Override
    public void clear() {
        _data = null;
        _size = 0;
        modCount++;
    }

    @Override
    public E set(final int index, final E element) {
        if (index < 0 || index >= _size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + _size);
        }

        final E oldValue;

        if (_size == 1) {
            oldValue = (E) _data;
            _data = element;
        }
        else {
            final Object[] array = (Object[]) _data;
            oldValue = (E) array[index];
            array[index] = element;
        }

        return oldValue;
    }

    @Override
    public E remove(final int index) {
        if (index < 0 || index >= _size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + _size);
        }

        final E oldValue;

        if (_size == 1) {
            oldValue = (E) _data;
            _data = null;
        }
        else {
            final Object[] array = (Object[]) _data;
            oldValue = (E) array[index];

            if (_size == 2) {
                _data = array[1 - index];
            }
            else {
                final int numMoved = _size - index - 1;

                if (numMoved > 0) {
                    System.arraycopy(array, index + 1, array, index, numMoved);
                }

                array[_size - 1] = null;
            }
        }

        _size--;
        modCount++;

        return oldValue;
    }

    @NotNull
    @Override
    public java.util.Iterator<E> iterator() {
        switch (_size) {
            case 0:
                return Collections.emptyIterator();
            case 1:
                return new SingletonIterator();
            default:
                return super.iterator();
        }
    }

    private final class SingletonIterator implements Iterator<E> {
        private boolean _visited;
        private final int _initialModCount;

        public SingletonIterator() {
            _initialModCount = modCount;
        }

        @Override
        public boolean hasNext() {
            return !_visited;
        }

        @Override
        public E next() {
            if (_visited) {
                throw new NoSuchElementException();
            }

            _visited = true;

            if (modCount != _initialModCount) {
                throw new ConcurrentModificationException("ModCount: " + modCount + "; expected: " + _initialModCount);
            }

            return (E) _data;
        }

        @Override
        public void remove() {
            if (modCount != _initialModCount) {
                throw new ConcurrentModificationException("ModCount: " + modCount + "; expected: " + _initialModCount);
            }

            clear();
        }
    }

    public void sort(@NotNull final java.util.Comparator<? super E> comparator) {
        if (_size >= 2) {
            Arrays.sort((E[]) _data, 0, _size, comparator);
        }
    }

    public int getModificationCount() {
        return modCount;
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull final T[] a) {
        if (_size == 1) {
            final int length = a.length;

            if (length != 0) {
                a[0] = (T) _data;

                if (length > 1) {
                    a[1] = null;
                }

                return a;
            }
        }

        //noinspection SuspiciousToArrayCall
        return super.toArray(a);
    }

    /**
     * Trims the capacity of this list to be the
     * list's current size.  An application can use this operation to minimize
     * the storage of a list instance.
     */
    public void trimToSize() {
        if (_size < 2) {
            return;
        }

        final Object[] array = (Object[]) _data;
        final int oldCapacity = array.length;

        if (_size < oldCapacity) {
            modCount++;
            _data = Arrays.copyOf(array, _size);
        }
    }
}
