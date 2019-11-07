/*
 * FrameValue.java
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
import com.strobel.core.Comparer;
import com.strobel.core.VerifyArgument;

public final class FrameValue {
    public final static FrameValue[] EMPTY_VALUES = new FrameValue[0];

    public final static FrameValue EMPTY = new FrameValue(FrameValueType.Empty);
    public final static FrameValue OUT_OF_SCOPE = new FrameValue(FrameValueType.Top);
    public final static FrameValue TOP = new FrameValue(FrameValueType.Top);
    public final static FrameValue INTEGER = new FrameValue(FrameValueType.Integer);
    public final static FrameValue FLOAT = new FrameValue(FrameValueType.Float);
    public final static FrameValue LONG = new FrameValue(FrameValueType.Long);
    public final static FrameValue DOUBLE = new FrameValue(FrameValueType.Double);
    public final static FrameValue NULL = new FrameValue(FrameValueType.Null);
    public final static FrameValue UNINITIALIZED_THIS = new FrameValue(FrameValueType.UninitializedThis);
    public final static FrameValue UNINITIALIZED = new FrameValue(FrameValueType.Uninitialized);

    private final FrameValueType _type;
    private final Object _parameter;

    private FrameValue(final FrameValueType type) {
        _type = type;
        _parameter = null;
    }

    private FrameValue(final FrameValueType type, final Object parameter) {
        _type = type;
        _parameter = parameter;
    }

    public final FrameValueType getType() {
        return _type;
    }

    public final Object getParameter() {
        return _parameter;
    }

    public final boolean isUninitialized() {
        return _type == FrameValueType.Uninitialized ||
               _type == FrameValueType.UninitializedThis;
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o instanceof FrameValue) {
            final FrameValue that = (FrameValue) o;
            return that._type == _type &&
                   Comparer.equals(that._parameter, _parameter);
        }

        return false;
    }

    @Override
    public final int hashCode() {
        int result = _type.hashCode();
        result = 31 * result + (_parameter != null ? _parameter.hashCode() : 0);
        return result;
    }

    @Override
    public final String toString() {
        if (_type == FrameValueType.Reference) {
            return String.format("%s(%s)", _type, ((TypeReference)_parameter).getSignature());
        }
        return _type.name();
    }

    // <editor-fold defaultstate="collapsed" desc="Factory Methods">

    public static FrameValue makeReference(final TypeReference type) {
        return new FrameValue(FrameValueType.Reference, VerifyArgument.notNull(type, "type"));
    }

    public static FrameValue makeAddress(final Instruction target) {
        return new FrameValue(FrameValueType.Address, VerifyArgument.notNull(target, "target"));
    }

    public static FrameValue makeUninitializedReference(final Instruction newInstruction) {
        VerifyArgument.notNull(newInstruction, "newInstruction");

        if (newInstruction.getOpCode() != OpCode.NEW) {
            throw new IllegalArgumentException("Parameter must be a NEW instruction.");
        }

        return new FrameValue(FrameValueType.Uninitialized, newInstruction);
    }

    // </editor-fold>
}
