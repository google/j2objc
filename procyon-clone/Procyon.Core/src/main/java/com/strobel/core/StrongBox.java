/*
 * StrongBox.java
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

import com.strobel.functions.Block;

/**
 * @author Mike Strobel
 */
@SuppressWarnings("PublicField")
public final class StrongBox<T> implements IStrongBox, Block<T> {
    public T value;

    public StrongBox() {}

    public StrongBox(final T value) {
        this.value = value;
    }

    @Override
    public T get() {
        return this.value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void set(final Object value) {
        this.value = (T) value;
    }

    @Override
    public void accept(final T input) {
        this.value = input;
    }

    @Override
    public String toString() {
        return "StrongBox{" +
               "value=" + value +
               '}';
    }
}
