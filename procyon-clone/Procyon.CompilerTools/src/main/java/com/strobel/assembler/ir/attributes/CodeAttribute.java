/*
 * CodeAttribute.java
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

import com.strobel.assembler.metadata.Buffer;
import com.strobel.core.ArrayUtilities;
import com.strobel.core.VerifyArgument;

import java.util.List;

/**
 * @author Mike Strobel
 */
public final class CodeAttribute extends SourceAttribute {
    private final int _maxStack;
    private final int _maxLocals;
    private final int _codeSize;
    private final int _codeOffset;
    private final Buffer _code;
    private final List<ExceptionTableEntry> _exceptionTableEntriesView;
    private final List<SourceAttribute> _attributesView;

    public CodeAttribute(
        final int size,
        final int maxStack,
        final int maxLocals,
        final int codeOffset,
        final int codeSize,
        final Buffer buffer,
        final ExceptionTableEntry[] exceptionTableEntries,
        final SourceAttribute[] attributes) {

        super(AttributeNames.Code, size);

        VerifyArgument.notNull(buffer, "buffer");
        VerifyArgument.notNull(exceptionTableEntries, "exceptionTableEntries");
        VerifyArgument.notNull(attributes, "attributes");

        _codeOffset = 0;
        _maxStack = maxStack;
        _maxLocals = maxLocals;
        _codeSize = codeSize;

        final Buffer code = new Buffer(codeSize);

        System.arraycopy(
            buffer.array(),
            codeOffset,
            code.array(),
            0,
            codeSize
        );

        _code = code;
        _attributesView = ArrayUtilities.asUnmodifiableList(attributes.clone());
        _exceptionTableEntriesView = ArrayUtilities.asUnmodifiableList(exceptionTableEntries.clone());
    }

    public CodeAttribute(
        final int size,
        final int codeOffset,
        final int codeSize,
        final int maxStack,
        final int maxLocals,
        final ExceptionTableEntry[] exceptionTableEntries,
        final SourceAttribute[] attributes) {

        super(AttributeNames.Code, size);

        VerifyArgument.notNull(attributes, "attributes");
        VerifyArgument.notNull(exceptionTableEntries, "exceptionTableEntries");

        _maxStack = maxStack;
        _maxLocals = maxLocals;
        _codeOffset = codeOffset;
        _codeSize = codeSize;
        _code = null;
        _attributesView = ArrayUtilities.asUnmodifiableList(attributes.clone());
        _exceptionTableEntriesView = ArrayUtilities.asUnmodifiableList(exceptionTableEntries.clone());
    }

    public int getMaxStack() {
        return _maxStack;
    }

    public int getMaxLocals() {
        return _maxLocals;
    }

    public int getCodeSize() {
        return _codeSize;
    }

    public boolean hasCode() {
        return _code != null;
    }

    public Buffer getCode() {
        return _code;
    }

    public List<ExceptionTableEntry> getExceptionTableEntries() {
        return _exceptionTableEntriesView;
    }

    public List<SourceAttribute> getAttributes() {
        return _attributesView;
    }

    public int getCodeOffset() {
        return _codeOffset;
    }
}
