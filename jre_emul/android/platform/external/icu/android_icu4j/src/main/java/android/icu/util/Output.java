/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2011-2016, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.util;

/**
 * Simple struct-like class for output parameters.
 * @param <T> The type of the parameter.
 */
public class Output<T> {
    /**
     * The value field
     */
    public T value;

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return value == null ? "null" : value.toString();
    }

    /**
     * Constructs an empty <code>Output</code>
     */
    public Output() {

    }

    /**
     * Constructs an <code>Output</code> with the given value.
     * @param value the initial value
     */
    public Output(T value) {
        this.value = value;
    }
}
