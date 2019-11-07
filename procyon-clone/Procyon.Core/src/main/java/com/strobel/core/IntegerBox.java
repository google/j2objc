/*
 * IntegerBox.java
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

@SuppressWarnings("PublicField")
public final class IntegerBox implements IStrongBox {
    public int value;

    public IntegerBox() {}

    public IntegerBox(final int value) {
        this.value = value;
    }

    @Override
    public Integer get() {
        return this.value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void set(final Object value) {
        this.value = (Integer) value;
    }
}
