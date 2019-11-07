/*
 * IReadOnlyList.java
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

import java.util.ListIterator;
import java.util.RandomAccess;

/**
 * @author Mike Strobel
 */
public interface IReadOnlyList<T> extends Iterable<T>, RandomAccess {
    int size();

    <U extends T> int indexOf(U o);
    <U extends T> int lastIndexOf(U o);

    boolean isEmpty();
    <U extends T> boolean contains(U o);
    boolean containsAll(Iterable<? extends T> c);

    T get(int index);

    @NotNull
    T[] toArray();
    @NotNull
    <T> T[] toArray(T[] a);

    @NotNull
    ListIterator<T> listIterator();

    @NotNull
    ListIterator<T> listIterator(int index);
}
