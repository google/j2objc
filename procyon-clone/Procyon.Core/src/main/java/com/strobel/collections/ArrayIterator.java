/*
 * ArrayIterator.java
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

import com.strobel.core.VerifyArgument;
import com.strobel.util.ContractUtils;

import java.util.Iterator;

/**
 * @author strobelm
 */
public final class ArrayIterator<E> implements Iterator<E> {
    private final E[] _elements;
    private int _index;

    public ArrayIterator(final E[] elements) {
        _elements = VerifyArgument.notNull(elements, "elements");
    }

    @Override
    public boolean hasNext() {
        return _index < _elements.length;
    }

    @Override
    public E next() {
        return _elements[_index++];
    }

    @Override
    public void remove() {
        throw ContractUtils.unsupported();
    }
}
