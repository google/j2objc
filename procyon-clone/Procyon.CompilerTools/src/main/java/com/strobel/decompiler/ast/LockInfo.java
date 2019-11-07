/*
 * LockInfo.java
 *
 * Copyright (c) 2013 Mike Strobel
 *
 * This source code is based on Mono.Cecil from Jb Evain, Copyright (c) Jb Evain;
 * and ILSpy/ICSharpCode from SharpDevelop, Copyright (c) AlphaSierraPapa.
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.decompiler.ast;

import com.strobel.core.VerifyArgument;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class LockInfo {
    public final Label leadingLabel;
    public final Expression lockInit;
    public final Expression lockStore;
    public final Expression lockStoreCopy;
    public final Expression lockAcquire;

    public final Variable lock;
    public final Variable lockCopy;

    public final int operationCount;
    public final boolean isSimpleAcquire;

    public final List<Variable> getLockVariables() {
        if (this.lockCopy == null)
            return Collections.singletonList(this.lock);

        return Arrays.asList(this.lock, this.lockCopy);
    }

    LockInfo(
        final Label leadingLabel,
        final Expression lockAcquire) {

        this(
            leadingLabel,
            null,
            null,
            null,
            lockAcquire
        );
    }

    LockInfo(
        final Label leadingLabel,
        final Expression lockInit,
        final Expression lockStore,
        final Expression lockStoreCopy,
        final Expression lockAcquire) {

        this.leadingLabel = leadingLabel;
        this.lockInit = lockInit;
        this.lockStore = lockStore;
        this.lockStoreCopy = lockStoreCopy;
        this.lockAcquire = VerifyArgument.notNull(lockAcquire, "lockAcquire");

        this.lock = (Variable) lockAcquire.getArguments().get(0).getOperand();

        if (lockStoreCopy != null) {
            this.lockCopy = (Variable) lockStoreCopy.getOperand();
        }
        else {
            this.lockCopy = null;
        }

        this.isSimpleAcquire = lockInit == null &&
                               lockStore == null &&
                               lockStoreCopy == null;

        this.operationCount = (leadingLabel != null ? 1 : 0) +
                              (lockStore != null ? 1 : 0) +
                              (lockStoreCopy != null ? 1 : 0) +
                              1;
    }
}
