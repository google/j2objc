/*
 * FlagUtilities.java
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

package com.strobel.reflection;

/**
 * @author Mike Strobel
 */
final class FlagUtilities {
    private FlagUtilities() {}

    static boolean all(final int flags, final int mask) {
        return (flags & mask) == mask;
    }

    static boolean any(final int flags, final int mask) {
        return (flags & mask) != 0;
    }
}
