/*
 * StackMapFrame.java
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

package com.strobel.assembler.ir;

import com.strobel.core.VerifyArgument;

public final class StackMapFrame {
    private final Frame _frame;
    private final Instruction _startInstruction;

    public StackMapFrame(final Frame frame, final Instruction startInstruction) {
        _frame = VerifyArgument.notNull(frame, "frame");
        _startInstruction = VerifyArgument.notNull(startInstruction, "startInstruction");
    }

    public final Frame getFrame() {
        return _frame;
    }

    public final Instruction getStartInstruction() {
        return _startInstruction;
    }

    @Override
    public final String toString() {
        return String.format("#%1$04d: %2$s", _startInstruction.getOffset(), _frame);
    }
}
