/*
 * ReadOnlyList.java
 *
 * Copyright (c) 2012 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.core;

import com.strobel.annotations.NotNull;
import com.strobel.util.EmptyArrayCache;

import java.lang.reflect.Array;
import java.util.*;

/**
 * @author Mike Strobel
 */
public class ReadOnlyList<T> implements IReadOnlyList<T>, List<T>, RandomAccess {
    private final static ReadOnlyList<?> EMPTY = new ReadOnlyList<Object>();
    private final static ReadOnlyCollectionIterator<?> EMPTY_ITERATOR = new ReadOnlyCollectionIterator<>(EMPTY);

    @SuppressWarnings("unchecked")
    public static <T> ReadOnlyList<T> emptyList() {
        return (ReadOnlyList<T>)EMPTY;
    }

    private final int _offset;
    private final int _length;
    private final T[] _elements;

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public ReadOnlyList(final T... elements) {
        VerifyArgument.notNull(elements, "elements");

        _offset = 0;
        _length = elements.length;
        _elements = (T[])Arrays.copyOf(elements, elements.length, elements.getClass());
    }

    @SuppressWarnings("unchecked")
    public ReadOnlyList(final Class<? extends T> elementType, final Collection<? extends T> elements) {
        VerifyArgument.notNull(elementType, "elementType");
        VerifyArgument.notNull(elements, "elements");

        _offset = 0;
        _length = elements.size();
        _elements = elements.toArray((T[])Array.newInstance(elementType, _length));
    }

    @SuppressWarnings("unchecked")
    public ReadOnlyList(final T[] elements, final int offset, final int length) {
        VerifyArgument.notNull(elements, "elements");

        _elements = (T[])Arrays.copyOf(elements, elements.length, elements.getClass());

        subListRangeCheck(offset, offset + length, _elements.length);

        _offset = offset;
        _length = length;
    }
    
    protected ReadOnlyList<T> newInstance() {
        return new ReadOnlyList<>(_elements, _offset, _length);
    }

    private ReadOnlyList(final ReadOnlyList<T> baseList, final int offset, final int length) {
        VerifyArgument.notNull(baseList, "baseList");

        final T[] elements = baseList._elements;

        subListRangeCheck(offset, offset + length, elements.length);

        _elements = elements;
        _offset = offset;
        _length = length;
    }

    protected final int getOffset() {
        return _offset;
    }

    protected final T[] getElements() {
        return _elements;
    }

    @Override
    public final int size() {
        return _length;
    }

    @Override
    public final boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsAll(final Iterable<? extends T> c) {
        VerifyArgument.notNull(c, "c");
        for (final T element : c) {
            if (!ArrayUtilities.contains(_elements, element)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public final boolean contains(final Object o) {
        return indexOf(o) != -1;
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public final Iterator<T> iterator() {
        if (isEmpty()) {
            return (Iterator<T>) EMPTY_ITERATOR;
        }
        return new ReadOnlyCollectionIterator(this);
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public final T[] toArray() {
        if (_length == 0) {
            return EmptyArrayCache.fromArrayType(_elements.getClass());
        }
        return (T[])Arrays.copyOfRange(_elements, _offset, _offset + _length, _elements.getClass());
    }

    @NotNull
    @Override
    @SuppressWarnings({"unchecked", "SuspiciousSystemArraycopy"})
    public final <U> U[] toArray(@NotNull final U[] a) {
        final int length = _length;

        if (a.length < length) {
            return (U[])Arrays.copyOfRange(_elements, _offset, _offset + _length, _elements.getClass());
        }

        System.arraycopy(_elements, _offset, a, 0, length);

        if (a.length > length) {
            a[length] = null;
        }

        return a;
    }

    @Override
    public final boolean add(final T T) {
        throw Error.unmodifiableCollection();
    }

    @Override
    public final boolean remove(final Object o) {
        throw Error.unmodifiableCollection();
    }

    @Override
    public final boolean containsAll(@NotNull final Collection<?> c) {
        for (final Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public final boolean addAll(@NotNull final Collection<? extends T> c) {
        throw Error.unmodifiableCollection();
    }

    @Override
    public final boolean addAll(final int index, @NotNull final Collection<? extends T> c) {
        throw Error.unmodifiableCollection();
    }

    @Override
    public final boolean removeAll(@NotNull final Collection<?> c) {
        throw Error.unmodifiableCollection();
    }

    @Override
    public final boolean retainAll(@NotNull final Collection<?> c) {
        throw Error.unmodifiableCollection();
    }

    @Override
    public final void clear() {
        throw Error.unmodifiableCollection();
    }

    @Override
    public final T get(final int index) {
        return _elements[_offset + index];
    }

    @Override
    public final T set(final int index, final T element) {
        throw Error.unmodifiableCollection();
    }

    @Override
    public final void add(final int index, final T element) {
        throw Error.unmodifiableCollection();
    }

    @Override
    public final T remove(final int index) {
        throw Error.unmodifiableCollection();
    }

    @Override
    public int hashCode() {
        int hash = 0;
        
        for (int i = _offset, n = _offset + _length; i < n ; i++) {
            final T element = _elements[i];
            
            if (element != null) {
                hash = hash * 31 + element.hashCode();
            }
        }
        
        return hash;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof ReadOnlyList)) {
            return false;
        }
        
        final ReadOnlyList<T> other = (ReadOnlyList<T>) obj;
        
        return Arrays.equals(_elements, other._elements);
    }

    @Override
    public final int indexOf(final Object o) {
        final T[] elements = _elements;
        final int start = _offset;
        final int end = start + _length;

        if (o == null) {
            for (int i = start; i < end; i++) {
                if (elements[i] == null) {
                    return i;
                }
            }
        }
        else {
            for (int i = start; i < end; i++) {
                if (o.equals(elements[i])) {
                    return i;
                }
            }
        }

        return -1;
    }

    @Override
    public final int lastIndexOf(final Object o) {
        final T[] elements = _elements;
        final int start = _offset;
        final int end = start + _length;

        if (o == null) {
            for (int i = end - 1; i >= start; i--) {
                if (elements[i] == null) {
                    return i;
                }
            }
        }
        else {
            for (int i = end - 1; i >= start; i--) {
                if (o.equals(elements[i])) {
                    return i;
                }
            }
        }

        return -1;
    }

    @Override
    public String toString() {
        final Iterator<T> it = iterator();

        if (!it.hasNext()) {
            return "[]";
        }

        final StringBuilder sb = new StringBuilder();

        sb.append('[');

        for (; ; ) {
            final T e = it.next();

            sb.append(e == this ? "(this Collection)" : e);

            if (!it.hasNext()) {
                return sb.append(']').toString();
            }

            sb.append(',').append(' ');
        }
    }

    @NotNull
    @Override
    public final ListIterator<T> listIterator() {
        return new ReadOnlyCollectionIterator<>(this);
    }

    @NotNull
    @Override
    public final ListIterator<T> listIterator(final int index) {
        return new ReadOnlyCollectionIterator<>(this, index);
    }

    protected static void subListRangeCheck(final int fromIndex, final int toIndex, final int size) {
        if (fromIndex < 0) {
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        }
        if (toIndex > size) {
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        }
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
        }
    }

    @NotNull
    @Override
    public ReadOnlyList<T> subList(final int fromIndex, final int toIndex) {
        subListRangeCheck(fromIndex, toIndex, size());
        return new ReadOnlyList<>(this, _offset + fromIndex, _offset + toIndex);
    }

    private static class ReadOnlyCollectionIterator<T> implements ListIterator<T> {
        private final ReadOnlyList<T> _list;
        private int _position = -1;

        ReadOnlyCollectionIterator(final ReadOnlyList<T> list) {
            this._list = list;
        }

        ReadOnlyCollectionIterator(final ReadOnlyList<T> list, final int startPosition) {
            if (startPosition < -1 || startPosition >= list.size()) {
                throw new IllegalArgumentException();
            }
            _position = startPosition;
            _list = list;
        }

        @Override
        public final boolean hasNext() {
            return _position + 1 < _list.size();
        }

        @Override
        public final T next() {
            if (!hasNext()) {
                throw new IllegalStateException();
            }
            return _list.get(++_position);
        }

        @Override
        public final boolean hasPrevious() {
            return _position > 0;
        }

        @Override
        public final T previous() {
            if (!hasPrevious()) {
                throw new IllegalStateException();
            }
            return _list.get(--_position);
        }

        @Override
        public final int nextIndex() {
            return _position + 1;
        }

        @Override
        public final int previousIndex() {
            return _position + 1;
        }

        @Override
        public final void remove() {
            throw Error.unmodifiableCollection();
        }

        @Override
        public final void set(final T T) {
            throw Error.unmodifiableCollection();
        }

        @Override
        public final void add(@NotNull final T T) {
            throw Error.unmodifiableCollection();
        }
    }
}
