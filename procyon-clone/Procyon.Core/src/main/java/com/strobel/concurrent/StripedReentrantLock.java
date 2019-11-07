/*
 * StripedReentrantLock.java
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

import java.util.concurrent.locks.ReentrantLock;

public final class StripedReentrantLock extends StripedLock<ReentrantLock> {
    private final static StripedReentrantLock INSTANCE = new StripedReentrantLock();

    public static StripedReentrantLock instance() {
        return INSTANCE;
    }

    public StripedReentrantLock() {
        super(ReentrantLock.class);
    }

    @NotNull
    @Override
    protected final ReentrantLock createLock() {
        return new ReentrantLock();
    }

    @Override
    public final void lock(final int index) {
        locks[index].lock();
    }

    @Override
    public final void unlock(final int index) {
        locks[index].unlock();
    }
}
