/*
 * Freezable.java
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

/**
 * @author Mike Strobel
 */
public abstract class Freezable implements IFreezable {
    private boolean _isFrozen;

    @Override
    public boolean canFreeze() {
        return !isFrozen();
    }

    @Override
    public final boolean isFrozen() {
        return _isFrozen;
    }

    @Override
    public final void freeze()
        throws IllegalStateException {
        if (!canFreeze()) {
            throw new IllegalStateException(
                "Object cannot be frozen.  Be sure to check canFreeze() before calling " +
                "freeze(), or use the tryFreeze() method instead."
            );
        }

        freezeCore();

        _isFrozen = true;
    }

    protected void freezeCore() {}

    protected final void verifyNotFrozen() {
        if (isFrozen()) {
            throw new IllegalStateException("Frozen object cannot be modified.");
        }
    }

    protected final void verifyFrozen() {
        if (!isFrozen()) {
            throw new IllegalStateException(
                "Object must be frozen before performing this operation."
            );
        }
    }

    @Override
    public final boolean tryFreeze() {
        if (!canFreeze()) {
            return false;
        }

        try {
            freeze();
            return true;
        }
        catch (final Throwable t) {
            return false;
        }
    }

    @Override
    public final void freezeIfUnfrozen() throws IllegalStateException {
        if (isFrozen()) {
            return;
        }
        freeze();
    }
}
