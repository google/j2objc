/*
 * Predicate.java
 *
 * Copyright (c) 2013 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.core;

/**
 * Determines if the input object matches some criteria. *
 *
 * @param <T> the type of input objects provided to {@code test}.
 */
public interface Predicate<T> {

    /**
     * Return {@code true} if the input object matches some criteria.
     *
     * @param t the input object.
     *
     * @return {@code true} if the input object matched some criteria.
     */
    boolean test(T t);
}
