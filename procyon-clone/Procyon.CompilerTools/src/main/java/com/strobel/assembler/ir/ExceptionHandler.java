/*
 * ExceptionHandler.java
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

import com.strobel.assembler.metadata.TypeReference;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.DecompilerHelpers;
import com.strobel.decompiler.PlainTextOutput;

public final class ExceptionHandler implements Comparable<ExceptionHandler> {
    private final InstructionBlock _tryBlock;
    private final InstructionBlock _handlerBlock;
    private final ExceptionHandlerType _handlerType;
    private final TypeReference _catchType;

    private ExceptionHandler(
        final InstructionBlock tryBlock,
        final InstructionBlock handlerBlock,
        final ExceptionHandlerType handlerType,
        final TypeReference catchType) {

        _tryBlock = tryBlock;
        _handlerBlock = handlerBlock;
        _handlerType = handlerType;
        _catchType = catchType;
    }

    public static ExceptionHandler createCatch(
        final InstructionBlock tryBlock,
        final InstructionBlock handlerBlock,
        final TypeReference catchType) {

        VerifyArgument.notNull(tryBlock, "tryBlock");
        VerifyArgument.notNull(handlerBlock, "handlerBlock");
        VerifyArgument.notNull(catchType, "catchType");

        return new ExceptionHandler(
            tryBlock,
            handlerBlock,
            ExceptionHandlerType.Catch,
            catchType
        );
    }

    public static ExceptionHandler createFinally(
        final InstructionBlock tryBlock,
        final InstructionBlock handlerBlock) {

        VerifyArgument.notNull(tryBlock, "tryBlock");
        VerifyArgument.notNull(handlerBlock, "handlerBlock");

        return new ExceptionHandler(
            tryBlock,
            handlerBlock,
            ExceptionHandlerType.Finally,
            null
        );
    }

    public final boolean isFinally() {
        return _handlerType == ExceptionHandlerType.Finally;
    }

    public final boolean isCatch() {
        return _handlerType == ExceptionHandlerType.Catch;
    }

    public final InstructionBlock getTryBlock() {
        return _tryBlock;
    }

    public final InstructionBlock getHandlerBlock() {
        return _handlerBlock;
    }

    public final ExceptionHandlerType getHandlerType() {
        return _handlerType;
    }

    public final TypeReference getCatchType() {
        return _catchType;
    }

    @Override
    public final String toString() {
        final PlainTextOutput output = new PlainTextOutput();
        DecompilerHelpers.writeExceptionHandler(output, this);
        return output.toString();
    }

    @Override
    public int compareTo(final ExceptionHandler o) {
        if (o == null) {
            return 1;
        }

        int result;

        final InstructionBlock h1 = _handlerBlock;
        final InstructionBlock h2 = o._handlerBlock;

        result = h1.getFirstInstruction().compareTo(h2.getFirstInstruction());

        if (result != 0) {
            return result;
        }

        final InstructionBlock t1 = _tryBlock;
        final InstructionBlock t2 = o._tryBlock;

        result = t1.getFirstInstruction().compareTo(t2.getFirstInstruction());

        if (result != 0) {
            return result;
        }

        result = t2.getLastInstruction().compareTo(t1.getLastInstruction());

        if (result != 0) {
            return result;
        }

        return h2.getLastInstruction().compareTo(h1.getLastInstruction());
    }
}

