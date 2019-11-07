/*
 * Error.java
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

import static java.lang.String.format;

/**
 * @author Mike Strobel
 */
final class Error {
    private Error() {}

    static IllegalStateException unmodifiableCollection() {
        return new IllegalStateException("Collection is read only.");
    }

    static IllegalArgumentException sequenceHasNoElements() {
        return new IllegalArgumentException("Sequence has no elements.");
    }

    static IllegalArgumentException sequenceHasMultipleElements() {
        return new IllegalArgumentException("Sequence contains more than one element.");
    }

    static IllegalArgumentException couldNotConvertFromNull() {
        return new IllegalArgumentException("Could not convert from 'null'.");
    }

    static IllegalArgumentException couldNotConvertFromType(final Class<?> sourceType) {
        return new IllegalArgumentException(
            format("Could not convert from type '%s'.", sourceType.getName())
        );
    }

    static IllegalArgumentException couldNotConvertNullValue(final Class<?> targetType) {
        return new IllegalArgumentException(
            format(
                "Could not convert 'null' to an instance of '%s'.",
                targetType.getName()
            )
        );
    }

    static IllegalArgumentException couldNotConvertValue(final Class<?> sourceType, final Class<?> targetType) {
        return new IllegalArgumentException(
            format(
                "Could not convert a value of type '%s' to an instance of '%s'.",
                sourceType.getName(),
                targetType.getName()
            )
        );
    }

    static IndexOutOfBoundsException indexOutOfRange(final int index) {
        return new IndexOutOfBoundsException(
            format("Index is out of range: %d", index)
        );
    }
}
