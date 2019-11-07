/*
 * ExceptionTableEntry.java
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

package com.strobel.assembler.ir.attributes;

import com.strobel.assembler.metadata.TypeReference;

/**
 * @author Mike Strobel
 */
public final class ExceptionTableEntry {
    private final int _startOffset;
    private final int _endOffset;
    private final int _handlerOffset;
    private final TypeReference _catchType;

    public ExceptionTableEntry(final int startOffset, final int endOffset, final int handlerOffset, final TypeReference catchType) {
        _startOffset = startOffset;
        _endOffset = endOffset;
        _handlerOffset = handlerOffset;
        _catchType = catchType;
    }

    public int getStartOffset() {
        return _startOffset;
    }

    public int getEndOffset() {
        return _endOffset;
    }

    public int getHandlerOffset() {
        return _handlerOffset;
    }

    public TypeReference getCatchType() {
        return _catchType;
    }

    @Override
    public String toString() {
        return "Handler{" +
               "From=" + _startOffset +
               ", To=" + _endOffset +
               ", Target=" + _handlerOffset +
               ", Type=" + _catchType +
               '}';
    }
}
