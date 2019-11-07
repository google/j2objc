/*
 * MutableInteger.java
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

import com.strobel.functions.Function;
import com.strobel.functions.Supplier;

/**
 * @author strobelm
 */
public final class MutableInteger {
    public final static Supplier<MutableInteger> SUPPLIER = new Supplier<MutableInteger>() {
        @Override
        public MutableInteger get() {
            return new MutableInteger();
        }
    };

    private int _value;

    public MutableInteger() {}

    public MutableInteger(final int value) {
        _value = value;
    }

    public int getValue() {
        return _value;
    }

    public void setValue(final int value) {
        _value = value;
    }
    
    public MutableInteger increment() {
        ++_value;
        return this;
    }

    public MutableInteger decrement() {
        --_value;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final MutableInteger that = (MutableInteger)o;

        return _value == that._value;
    }

    @Override
    public int hashCode() {
        return _value;
    }
}
