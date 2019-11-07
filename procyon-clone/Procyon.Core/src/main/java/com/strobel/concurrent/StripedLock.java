/*
 * StripedLock.java
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

package com.strobel.concurrent;

import com.strobel.annotations.NotNull;

import java.lang.reflect.Array;

public abstract class StripedLock<T> {
    private static final int LOCK_COUNT = 256;

    @SuppressWarnings("ProtectedField")
    protected final T[] locks;
    private int _lockAllocationCounter;

    @SuppressWarnings({ "unchecked", "OverridableMethodCallDuringObjectConstruction" })
    protected StripedLock(final Class<T> lockType) {
        locks = (T[]) Array.newInstance(lockType, LOCK_COUNT);

        for (int i = 0; i < locks.length; i++) {
            locks[i] = createLock();
        }
    }

    @NotNull
    public T allocateLock() {
        return locks[allocateLockIndex()];
    }

    public int allocateLockIndex() {
        return (_lockAllocationCounter = (_lockAllocationCounter + 1) % LOCK_COUNT);
    }

    @NotNull
    protected abstract T createLock();

    public abstract void lock(final int index);
    public abstract void unlock(final int index);
}

