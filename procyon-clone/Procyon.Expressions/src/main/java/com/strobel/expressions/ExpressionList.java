/*
 * ExpressionList.java
 *
 * Copyright (c) 2012 Mike Strobel
 *
 * This source code is based on the Dynamic Language Runtime from Microsoft,
 *   Copyright (c) Microsoft Corporation.
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.expressions;

import com.strobel.core.ArrayUtilities;
import com.strobel.core.VerifyArgument;
import com.strobel.util.EmptyArrayCache;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.RandomAccess;

/**
 * @author Mike Strobel
 */
public class ExpressionList<T extends Expression> implements Iterable<T>, RandomAccess {
    @SuppressWarnings("unchecked")
    private final static ExpressionList EMPTY = new ExpressionList(EmptyArrayCache.fromElementType(Expression.class));

    private final T[] _expressions;

    @SuppressWarnings("unchecked")
    public static <T extends Expression> ExpressionList<T> empty() {
        return EMPTY;
    }

    @SafeVarargs
    public ExpressionList(final T... expressions) {
        _expressions = Arrays.copyOf(
            VerifyArgument.noNullElements(expressions, "expressions"),
            expressions.length);
    }

    protected ExpressionList<T> newInstance(final T[] expressions) {
        return new ExpressionList<>(expressions);
    }

    public int size() {
        return _expressions.length;
    }

    public boolean isEmpty() {
        return _expressions.length == 0;
    }

    public boolean contains(final T expression) {
        return indexOf(expression) != -1;
    }

    @Override
    public ListIterator<T> iterator() {
        return new ExpressionListIterator();
    }

    public ListIterator<T> iterator(final int index) {
        VerifyArgument.inRange(0, _expressions.length, index, "index");
        return new ExpressionListIterator(index);
    }

    @SuppressWarnings("unchecked")
    public T[] toArray() {
        final int size = size();

        if (size == 0) {
            return EmptyArrayCache.fromArrayType(_expressions.getClass());
        }

        final T[] array = (T[])Array.newInstance(
            _expressions.getClass().getComponentType(),
            size
        );

        for (int i = 0; i < size; i++) {
            array[i] = get(i);
        }

        return array;
    }

    @SuppressWarnings("unchecked")
    public <T> T[] toArray(final T[] a) {
        final int size = size();
        final T[] array = a != null && a.length >= size
                          ? a
                          : (T[])Array.newInstance(_expressions.getClass().getComponentType(), size);

        for (int i = 0; i < size; i++) {
            array[i] = (T)get(i);
        }

        return array;
    }

    public ExpressionList<T> add(final T expression) {
        return newInstance(ArrayUtilities.append(_expressions, expression));
    }

    public ExpressionList<T> remove(final T expression) {
        if (_expressions.length == 1 && _expressions[0].equals(expression)) {
            return empty();
        }

        return newInstance(ArrayUtilities.removeFirst(_expressions, expression));
    }

    @SafeVarargs
    public final ExpressionList<T> addAll(final int index, final T... expressions) {
        VerifyArgument.inRange(0, _expressions.length, index, "index");

        if (expressions == null || expressions.length == 0) {
            return this;
        }

        return newInstance(ArrayUtilities.insert(_expressions, index, expressions));
    }

    public ExpressionList<T> addAll(final int index, final ExpressionList<T> c) {
        VerifyArgument.inRange(0, _expressions.length, index, "index");

        if (c == null || c.size() == 0) {
            return this;
        }

        return newInstance(ArrayUtilities.insert(_expressions, index, c._expressions));
    }

    @SafeVarargs
    public final ExpressionList<T> addAll(final T... expressions) {
        if (expressions == null || expressions.length == 0) {
            return this;
        }

        return newInstance(ArrayUtilities.insert(_expressions, size(), expressions));
    }

    public ExpressionList<T> addAll(final ExpressionList<T> c) {
        if (c == null || c.size() == 0) {
            return this;
        }

        return newInstance(ArrayUtilities.insert(_expressions, size(), c._expressions));
    }

    @SafeVarargs
    public final ExpressionList<T> removeAll(final T... expressions) {
        if (expressions == null || expressions.length == 0) {
            return this;
        }

        return newInstance(ArrayUtilities.removeAll(_expressions, expressions));
    }

    public ExpressionList<T> removeAll(final ExpressionList<? extends T> c) {
        if (c == null || c.size() == 0) {
            return this;
        }

        return newInstance(ArrayUtilities.removeAll(_expressions, c._expressions));
    }

    @SafeVarargs
    public final ExpressionList<T> retainAll(final T... expressions) {
        if (expressions == null || expressions.length == 0) {
            return this;
        }

        return newInstance(ArrayUtilities.removeAll(_expressions, expressions));
    }

    public ExpressionList<T> retainAll(final ExpressionList<? extends T> c) {
        if (c == null || c.size() == 0) {
            return this;
        }

        return newInstance(ArrayUtilities.removeAll(_expressions, c._expressions));
    }

    public T get(final int index) {
        VerifyArgument.inRange(0, _expressions.length, index, "index");
        return _expressions[index];
    }

    public ExpressionList<T> replace(final int index, final T expression) {
        VerifyArgument.inRange(0, _expressions.length, index, "index");

        final T[] expressions = Arrays.copyOf(_expressions, _expressions.length);

        expressions[index] = expression;

        return newInstance(expressions);
    }

    public ExpressionList<T> add(final int index, final T expression) {
        VerifyArgument.inRange(0, _expressions.length, index, "index");

        return newInstance(ArrayUtilities.insert(_expressions, index, expression));
    }

    public ExpressionList<T> remove(final int index) {
        VerifyArgument.inRange(0, _expressions.length, index, "index");

        return newInstance(ArrayUtilities.remove(_expressions, index));
    }

    public int indexOf(final T expression) {
        return ArrayUtilities.indexOf(_expressions, expression);
    }

    public int lastIndexOf(final T expression) {
        return ArrayUtilities.lastIndexOf(_expressions, expression);
    }

    public ExpressionList<T> getRange(final int fromIndex, final int toIndex) {
        VerifyArgument.validElementRange(_expressions.length, fromIndex, toIndex);

        return newInstance(Arrays.copyOfRange(_expressions, fromIndex, toIndex));
    }

    private final class ExpressionListIterator implements ListIterator<T> {
        private int _position = -1;

        ExpressionListIterator() {}

        ExpressionListIterator(final int startPosition) {
            if (startPosition < -1 || startPosition >= size()) {
                throw new IllegalArgumentException();
            }
            _position = startPosition;
        }

        @Override
        public boolean hasNext() {
            return _position + 1 < size();
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new IllegalStateException();
            }
            return get(++_position);
        }

        @Override
        public boolean hasPrevious() {
            return _position > 0;
        }

        @Override
        public T previous() {
            if (!hasPrevious()) {
                throw new IllegalStateException();
            }
            return get(--_position);
        }

        @Override
        public int nextIndex() {
            return _position + 1;
        }

        @Override
        public int previousIndex() {
            return _position + 1;
        }

        @Override
        public void remove() {
            throw Error.unmodifiableCollection();
        }

        @Override
        public void set(final T expression) {
            throw Error.unmodifiableCollection();
        }

        @Override
        public void add(final T expression) {
            throw Error.unmodifiableCollection();
        }
    }
}
