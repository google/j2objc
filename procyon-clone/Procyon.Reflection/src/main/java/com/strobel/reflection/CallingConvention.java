/*
 * CallingConvention.java
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

import static com.strobel.reflection.FlagUtilities.all;

/**
 * @author Mike Strobel
 */
public enum CallingConvention {
    Standard(1),
    VarArgs(2),
    Any(VarArgs.mask | Standard.mask);

    private final int mask;

    CallingConvention(final int mask) {
        this.mask = mask;
    }

    static CallingConvention fromMethodModifiers(final int modifiers) {
        if (all(modifiers, Type.VARARGS_MODIFIER)) {
            return VarArgs;
        }
        return Standard;
    }
}
