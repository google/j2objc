/*
 * ContractUtils.java
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

package com.strobel.util;

/**
 * @author Mike Strobel
 */
public final class ContractUtils {
    private ContractUtils() {}

    public static IllegalStateException unreachable() {
        return new IllegalStateException("Code supposed to be unreachable");
    }

    public static UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException("The requested operation is not supported.");
    }
}
