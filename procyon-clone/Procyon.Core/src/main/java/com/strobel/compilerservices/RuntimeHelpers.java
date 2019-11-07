/*
 * RuntimeHelpers.java
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

package com.strobel.compilerservices;

import com.strobel.core.VerifyArgument;
import com.strobel.util.ContractUtils;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @author strobelm
 */
public final class RuntimeHelpers {
    private RuntimeHelpers() {
        throw ContractUtils.unreachable();
    }

    public static void ensureClassInitialized(final Class<?> clazz) {
        getUnsafeInstance().ensureClassInitialized(VerifyArgument.notNull(clazz, "clazz"));
    }

    // <editor-fold defaultstate="collapsed" desc="Unsafe Access">

    private static Unsafe _unsafe;

    private static Unsafe getUnsafeInstance() {
        if (_unsafe != null) {
            return _unsafe;
        }

        try {
            _unsafe = Unsafe.getUnsafe();
        }
        catch (Throwable ignored) {
        }

        try {
            final Field instanceField = Unsafe.class.getDeclaredField("theUnsafe");
            instanceField.setAccessible(true);
            _unsafe = (Unsafe) instanceField.get(Unsafe.class);
        }
        catch (Throwable t) {
            throw new IllegalStateException(
                String.format(
                    "Could not load an instance of the %s class.",
                    Unsafe.class.getName()
                )
            );
        }

        return _unsafe;
    }

    // </editor-fold>
}
