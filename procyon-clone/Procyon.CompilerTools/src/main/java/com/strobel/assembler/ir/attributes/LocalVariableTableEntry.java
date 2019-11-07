/*
 * LocalVariableTableEntry.java
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
import com.strobel.core.VerifyArgument;

/**
 * @author Mike Strobel
 */
public final class LocalVariableTableEntry {
    private final int _index;
    private final String _name;
    private final TypeReference _type;
    private final String _originalSignature;
    private final int _scopeOffset;
    private final int _scopeLength;
    private final boolean _isBadType;

    public LocalVariableTableEntry(
        final int index,
        final String name,
        final TypeReference type,
        final String originalSignature,
        final int scopeOffset,
        final int scopeLength,
        final boolean isBadType) {

        _index = VerifyArgument.isNonNegative(index, "index");
        _name = VerifyArgument.notNull(name, "name");
        _type = VerifyArgument.notNull(type, "type");
        _originalSignature = VerifyArgument.notNull(originalSignature, "originalSignature");
        _scopeOffset = VerifyArgument.isNonNegative(scopeOffset, "scopeOffset");
        _scopeLength = VerifyArgument.isNonNegative(scopeLength, "scopeLength");
        _isBadType = isBadType;
    }

    public int getIndex() {
        return _index;
    }

    public String getName() {
        return _name;
    }

    public TypeReference getType() {
        return _type;
    }

    public int getScopeOffset() {
        return _scopeOffset;
    }

    public int getScopeLength() {
        return _scopeLength;
    }

    public String getOriginalSignature() {
        return _originalSignature;
    }

    public boolean isBadType() {
        return _isBadType;
    }

    @Override
    public String toString() {
        return "LocalVariableTableEntry{" +
               "Index=" + _index +
               ", Name='" + _name + '\'' +
               ", Type=" + _type +
               ", OriginalSignature=" + _originalSignature +
               ", ScopeOffset=" + _scopeOffset +
               ", ScopeLength=" + _scopeLength +
               ", IsBadType=" + _isBadType +
               '}';
    }
}

