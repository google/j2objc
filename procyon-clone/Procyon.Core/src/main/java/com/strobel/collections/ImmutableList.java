/*
 * ImmutableList.java
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

package com.strobel.collections;

import com.strobel.annotations.NotNull;
import com.strobel.core.StringUtilities;

import java.lang.reflect.Array;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * A class for generic linked lists. Links are supposed to be
 * immutable, the only exception being the incremental construction of
 * lists via ListBuffers.  List is the main container class in
 * GJC. Most data structures and algorithms in GJC use lists rather
 * than arrays.
 * <p></p>
 * <p>Lists are always trailed by a sentinel element, whose head and tail
 * are both null.
 * <p></p>
 * <p><b>This is NOT part of any supported API.
 * If you write code that depends on this, you do so at your own risk.
 * This code and its internal interfaces are subject to change or
 * deletion without notice.</b>
 */
@SuppressWarnings("PublicField")
public class ImmutableList<A> extends AbstractCollection<A> implements java.util.List<A> {

    /**
     * The first element of the list, supposed to be immutable.
     */
    public A head;

    /**
     * The remainder of the list except for its first element, supposed
     * to be immutable.
     */
    //@Deprecated
    public ImmutableList<A> tail;

    /**
     * Construct a list given its head and tail.
     */
    ImmutableList(final A head, final ImmutableList<A> tail) {
        this.tail = tail;
        this.head = head;
    }

    /**
     * Construct an empty list.
     */
    @SuppressWarnings("unchecked")
    public static <A> ImmutableList<A> empty() {
        return (ImmutableList<A>)EMPTY_LIST;
    }

    private static final ImmutableList<?> EMPTY_LIST = new ImmutableList<Object>(null, null) {
        public ImmutableList<Object> setTail(final ImmutableList<Object> tail) {
            throw new UnsupportedOperationException();
        }

        public boolean isEmpty() {
            return true;
        }
    };

    /**
     * Construct a list consisting of given element.
     */
    public static <A> ImmutableList<A> of(final A x1) {
        return new ImmutableList<>(x1, ImmutableList.<A>empty());
    }

    /**
     * Construct a list consisting of given elements.
     */
    @SafeVarargs
    public static <A> ImmutableList<A> of(final A x1, final A... rest) {
        return new ImmutableList<>(x1, from(rest));
    }

    /**
     * Construct a list consisting of given elements.
     */
    public static <A> ImmutableList<A> of(final A x1, final A x2) {
        return new ImmutableList<>(x1, of(x2));
    }

    /**
     * Construct a list consisting of given elements.
     */
    public static <A> ImmutableList<A> of(final A x1, final A x2, final A x3) {
        return new ImmutableList<>(x1, of(x2, x3));
    }

    /**
     * Construct a list consisting of given elements.
     */
    @SuppressWarnings({"varargs", "unchecked"})
    public static <A> ImmutableList<A> of(final A x1, final A x2, final A x3, final A... rest) {
        return new ImmutableList<>(x1, new ImmutableList<>(x2, new ImmutableList<>(x3, from(rest))));
    }

    /**
     * Construct a list consisting all elements of given array.
     * @param array an array; if {@code null} return an empty list
     */
    public static <A> ImmutableList<A> from(final A[] array) {
        ImmutableList<A> xs = empty();
        if (array != null) {
            for (int i = array.length - 1; i >= 0; i--) {
                xs = new ImmutableList<>(array[i], xs);
            }
        }
        return xs;
    }

    /**
     * Construct a list consisting of a given number of identical elements.
     * @param len  The number of elements in the list.
     * @param init The value of each element.
     */
    @Deprecated
    public static <A> ImmutableList<A> fill(final int len, final A init) {
        ImmutableList<A> l = empty();
        for (int i = 0; i < len; i++) {
            l = new ImmutableList<>(init, l);
        }
        return l;
    }

    /**
     * Does list have no elements?
     */
    @Override
    public boolean isEmpty() {
        return tail == null;
    }

    /**
     * Does list have elements?
     */
    //@Deprecated
    public boolean nonEmpty() {
        return tail != null;
    }

    /**
     * Return the number of elements in this list.
     */
    //@Deprecated
    public int length() {
        ImmutableList<A> l = this;
        int len = 0;
        while (l.tail != null) {
            l = l.tail;
            len++;
        }
        return len;
    }

    @Override
    public int size() {
        return length();
    }

    public ImmutableList<A> setTail(final ImmutableList<A> tail) {
        this.tail = tail;
        return tail;
    }

    /**
     * Prepend given element to front of list, forming and returning
     * a new list.
     */
    public ImmutableList<A> prepend(final A x) {
        return new ImmutableList<>(x, this);
    }

    /**
     * Prepend given list of elements to front of list, forming and returning
     * a new list.
     */
    public ImmutableList<A> prependList(final ImmutableList<A> xs) {
        if (this.isEmpty()) {
            return xs;
        }
        
        if (xs.isEmpty()) {
            return this;
        }
        
        if (xs.tail.isEmpty()) {
            return prepend(xs.head);
        }
        
        // Return this.prependList(xs.tail).prepend(xs.head);
        ImmutableList<A> result = this;
        ImmutableList<A> rev = xs.reverse();
        
        assert rev != xs;
        
        // since xs.reverse() returned a new list, we can reuse the
        // individual List objects, instead of allocating new ones.
        while (rev.nonEmpty()) {
            final ImmutableList<A> h = rev;
            rev = rev.tail;
            h.setTail(result);
            result = h;
        }
        return result;
    }

    /**
     * Reverse list.
     * If the list is empty or a singleton, then the same list is returned.
     * Otherwise a new list is formed.
     */
    public ImmutableList<A> reverse() {
        // if it is empty or a singleton, return itself
        if (isEmpty() || tail.isEmpty()) {
            return this;
        }

        ImmutableList<A> rev = empty();
        for (ImmutableList<A> l = this; l.nonEmpty(); l = l.tail) {
            rev = new ImmutableList<>(l.head, rev);
        }
        return rev;
    }

    /**
     * Append given element at length, forming and returning
     * a new list.
     */
    public ImmutableList<A> append(final A x) {
        return of(x).prependList(this);
    }

    /**
     * Append given list at length, forming and returning
     * a new list.
     */
    public ImmutableList<A> appendList(final ImmutableList<A> x) {
        return x.prependList(this);
    }

    /**
     * Append given list buffer at length, forming and returning a new
     * list.
     */
    public ImmutableList<A> appendList(final ListBuffer<A> x) {
        return appendList(x.toList());
    }

    /**
     * Copy successive elements of this list into given vector until
     * list is exhausted or end of vector is reached.
     */
    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(@NotNull final T[] vec) {
        int i = 0;
        ImmutableList<A> l = this;
        while (l.nonEmpty() && i < vec.length) {
            vec[i] = (T)l.head;
            l = l.tail;
            i++;
        }
        if (l.isEmpty()) {
            if (i < vec.length) {
                vec[i] = null;
            }
            return vec;
        }

        return toArray((T[])Array.newInstance(vec.getClass().getComponentType(), size()));
    }

    @NotNull
    public Object[] toArray() {
        return toArray(new Object[size()]);
    }

    /**
     * Form a string listing all elements with given separator character.
     */
    public String toString(final String sep) {
        if (isEmpty()) {
            return StringUtilities.EMPTY;
        }
        else {
            final StringBuilder buffer = new StringBuilder();
            buffer.append(head);
            for (ImmutableList<A> l = tail; l.nonEmpty(); l = l.tail) {
                buffer.append(sep);
                buffer.append(l.head);
            }
            return buffer.toString();
        }
    }

    /**
     * Form a string listing all elements with comma as the separator character.
     */
    @Override
    public String toString() {
        return toString(",");
    }

    /**
     * Compute a hash code, overrides Object
     * @see java.util.List#hashCode
     */
    @Override
    public int hashCode() {
        ImmutableList<A> l = this;
        int h = 1;
        while (l.tail != null) {
            h = h * 31 + (l.head == null ? 0 : l.head.hashCode());
            l = l.tail;
        }
        return h;
    }

    /**
     * Is this list the same as other list?
     * @see java.util.List#equals
     */
    @Override
    public boolean equals(final Object other) {
        if (other instanceof ImmutableList<?>) {
            return equals(this, (ImmutableList<?>)other);
        }
        if (other instanceof java.util.List<?>) {
            ImmutableList<A> t = this;
            final Iterator<?> it = ((java.util.List<?>)other).iterator();
            while (t.tail != null && it.hasNext()) {
                final Object o = it.next();
                if (!(t.head == null ? o == null : t.head.equals(o))) {
                    return false;
                }
                t = t.tail;
            }
            return (t.isEmpty() && !it.hasNext());
        }
        return false;
    }

    /**
     * Are the two lists the same?
     */
    public static boolean equals(ImmutableList<?> xs, ImmutableList<?> ys) {
        while (xs.tail != null && ys.tail != null) {
            if (xs.head == null) {
                if (ys.head != null) {
                    return false;
                }
            }
            else {
                if (!xs.head.equals(ys.head)) {
                    return false;
                }
            }
            xs = xs.tail;
            ys = ys.tail;
        }
        return xs.tail == null && ys.tail == null;
    }

    /**
     * Does the list contain the specified element?
     */
    @Override
    public boolean contains(final Object x) {
        ImmutableList<A> l = this;
        while (l.tail != null) {
            if (x == null) {
                if (l.head == null) {
                    return true;
                }
            }
            else {
                if (l.head.equals(x)) {
                    return true;
                }
            }
            l = l.tail;
        }
        return false;
    }

    /**
     * The last element in the list, if any, or null.
     */
    public A last() {
        A last = null;
        ImmutableList<A> t = this;
        while (t.tail != null) {
            last = t.head;
            t = t.tail;
        }
        return last;
    }

    @SuppressWarnings("unchecked")
    public static <T> ImmutableList<T> convert(final Class<T> type, final ImmutableList<?> list) {
        if (list == null) {
            return null;
        }
        for (final Object o : list) {
            type.cast(o);
        }
        return (ImmutableList<T>)list;
    }

    private static final Iterator<?> EMPTY_ITERATOR = new Iterator<Object>() {
        public boolean hasNext() {
            return false;
        }

        public Object next() {
            throw new java.util.NoSuchElementException();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    };

    @SuppressWarnings("unchecked")
    private static <A> Iterator<A> emptyIterator() {
        return (Iterator<A>)EMPTY_ITERATOR;
    }

    @NotNull
    @Override
    public Iterator<A> iterator() {
        if (tail == null) {
            return emptyIterator();
        }
        return new Iterator<A>() {
            private ImmutableList<A> _elements = ImmutableList.this;

            public boolean hasNext() {
                return _elements.tail != null;
            }

            public A next() {
                if (_elements.tail == null) {
                    throw new NoSuchElementException();
                }
                final A result = _elements.head;
                _elements = _elements.tail;
                return result;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public A get(final int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }

        ImmutableList<A> l = this;

        int i = index;

        while (i-- > 0 && !l.isEmpty()) {
            l = l.tail;
        }

        if (l.isEmpty()) {
            throw new IndexOutOfBoundsException(
                "Index: " + index + ", " +
                "Size: " + size()
            );
        }

        return l.head;
    }

    public boolean addAll(final int index, @NotNull final Collection<? extends A> c) {
        if (c.isEmpty()) {
            return false;
        }
        throw new UnsupportedOperationException();
    }

    public A set(final int index, final A element) {
        throw new UnsupportedOperationException();
    }

    public void add(final int index, final A element) {
        throw new UnsupportedOperationException();
    }

    public A remove(final int index) {
        throw new UnsupportedOperationException();
    }

    public int indexOf(final Object o) {
        int i = 0;
        for (ImmutableList<A> l = this; l.tail != null; l = l.tail, i++) {
            if (l.head == null ? o == null : l.head.equals(o)) {
                return i;
            }
        }
        return -1;
    }

    public int lastIndexOf(final Object o) {
        int last = -1;
        int i = 0;
        for (ImmutableList<A> l = this; l.tail != null; l = l.tail, i++) {
            if (l.head == null ? o == null : l.head.equals(o)) {
                last = i;
            }
        }
        return last;
    }

    @NotNull
    public ListIterator<A> listIterator() {
        return Collections.unmodifiableList(new ArrayList<>(this)).listIterator();
    }

    @NotNull
    public ListIterator<A> listIterator(final int index) {
        return Collections.unmodifiableList(new ArrayList<>(this)).listIterator(index);
    }

    @NotNull
    public java.util.List<A> subList(final int fromIndex, final int toIndex) {
        if (fromIndex < 0 || toIndex > size() || fromIndex > toIndex) {
            throw new IllegalArgumentException();
        }

        final ArrayList<A> a = new ArrayList<>(toIndex - fromIndex);
        int i = 0;
        for (ImmutableList<A> l = this; l.tail != null; l = l.tail, i++) {
            if (i == toIndex) {
                break;
            }
            if (i >= fromIndex) {
                a.add(l.head);
            }
        }

        return Collections.unmodifiableList(a);
    }
}
