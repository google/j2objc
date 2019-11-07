/*
 * ListBuffer.java
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

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A class for constructing lists by appending elements. Modelled after
 * java.lang.StringBuffer.
 * <p></p>
 * <p><b>This is NOT part of any supported API.
 * If you write code that depends on this, you do so at your own risk.
 * This code and its internal interfaces are subject to change or
 * deletion without notice.</b>
 */
@SuppressWarnings({"PublicField", "PackageVisibleField"})
public class ListBuffer<A> extends AbstractQueue<A> {

    public static <T> ListBuffer<T> lb() {
        return new ListBuffer<>();
    }

    public static <T> ListBuffer<T> of(final T x) {
        final ListBuffer<T> lb = new ListBuffer<>();
        lb.add(x);
        return lb;
    }

    /**
     * The list of elements of this buffer.
     */
    public ImmutableList<A> elements;

    /**
     * A pointer pointing to the last, sentinel element of `elements'.
     */
    public ImmutableList<A> last;

    /**
     * The number of element in this buffer.
     */
    public int count;

    /**
     * Has a list been created from this buffer yet?
     */
    public boolean shared;

    /**
     * Create a new initially empty list buffer.
     */
    public ListBuffer() {
        clear();
    }

    public final void clear() {
        this.elements = new ImmutableList<>(null, null);
        this.last = this.elements;
        count = 0;
        shared = false;
    }

    /**
     * Return the number of elements in this buffer.
     */
    public int length() {
        return count;
    }

    public int size() {
        return count;
    }

    /**
     * Is buffer empty?
     */
    public boolean isEmpty() {
        return count == 0;
    }

    /**
     * Is buffer not empty?
     */
    public boolean nonEmpty() {
        return count != 0;
    }

    /**
     * Copy list and sets last.
     */
    private void copy() {
        ImmutableList<A> p = elements = new ImmutableList<>(elements.head, elements.tail);
        while (true) {
            ImmutableList<A> tail = p.tail;
            if (tail == null) {
                break;
            }
            tail = new ImmutableList<>(tail.head, tail.tail);
            p.setTail(tail);
            p = tail;
        }
        last = p;
        shared = false;
    }

    /**
     * Prepend an element to buffer.
     */
    public ListBuffer<A> prepend(final A x) {
        elements = elements.prepend(x);
        count++;
        return this;
    }

    /**
     * Append an element to buffer.
     */
    public ListBuffer<A> append(final A x) {
        x.getClass(); // null check
        if (shared) {
            copy();
        }
        last.head = x;
        last.setTail(new ImmutableList<A>(null, null));
        last = last.tail;
        count++;
        return this;
    }

    /**
     * Append all elements in a list to buffer.
     */
    public ListBuffer<A> appendList(ImmutableList<A> xs) {
        while (xs.nonEmpty()) {
            append(xs.head);
            xs = xs.tail;
        }
        return this;
    }

    /**
     * Append all elements in a list to buffer.
     */
    public ListBuffer<A> appendList(final ListBuffer<A> xs) {
        return appendList(xs.toList());
    }

    /**
     * Append all elements in an array to buffer.
     */
    public ListBuffer<A> appendArray(final A[] xs) {
        for (final A x : xs) {
            append(x);
        }
        return this;
    }

    /**
     * Convert buffer to a list of all its elements.
     */
    public ImmutableList<A> toList() {
        shared = true;
        return elements;
    }

    /**
     * Does the list contain the specified element?
     */
    public boolean contains(final Object x) {
        return elements.contains(x);
    }

    /**
     * Convert buffer to an array
     */
    @NotNull
    @SuppressWarnings("SuspiciousToArrayCall")
    public <T> T[] toArray(final T[] vec) {
        return elements.toArray(vec);
    }

    @NotNull
    public Object[] toArray() {
        return toArray(new Object[size()]);
    }

    /**
     * The first element in this buffer.
     */
    public A first() {
        return elements.head;
    }

    /**
     * Return first element in this buffer and remove
     */
    public A next() {
        final A x = elements.head;
        if (elements != last) {
            elements = elements.tail;
            count--;
        }
        return x;
    }

    /**
     * An enumeration of all elements in this buffer.
     */
    @NotNull
    public Iterator<A> iterator() {
        return new Iterator<A>() {
            ImmutableList<A> elements = ListBuffer.this.elements;

            public boolean hasNext() {
                return elements != last;
            }

            public A next() {
                if (elements == last) {
                    throw new NoSuchElementException();
                }
                final A elem = elements.head;
                elements = elements.tail;
                return elem;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public boolean add(final A a) {
        append(a);
        return true;
    }

    public boolean remove(final Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean containsAll(final Collection<?> c) {
        for (final Object x : c) {
            if (!contains(x)) {
                return false;
            }
        }
        return true;
    }

    public boolean addAll(final Collection<? extends A> c) {
        for (final A a : c) {
            append(a);
        }
        return true;
    }

    public boolean removeAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public boolean offer(final A a) {
        append(a);
        return true;
    }

    public A poll() {
        return next();
    }

    public A peek() {
        return first();
    }
}
