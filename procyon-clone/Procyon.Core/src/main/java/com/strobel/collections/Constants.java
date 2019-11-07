/*
 * Constants.java
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

package com.strobel.collections;

/**
 * @author strobelm
 */
final class Constants {
    /**
     * The default value that represents for {@code int} types.
     */
    public static final int DEFAULT_INT_NO_ENTRY_VALUE;

    static {
        final int value;
        final String property = System.getProperty("gnu.trove.no_entry.int", "0");

        if ("MAX_VALUE".equalsIgnoreCase(property)) {
            value = Integer.MAX_VALUE;
        }
        else if ("MIN_VALUE".equalsIgnoreCase(property)) {
            value = Integer.MIN_VALUE;
        }
        else {
            value = Integer.valueOf(property);
        }

        DEFAULT_INT_NO_ENTRY_VALUE = value;
    }
}
