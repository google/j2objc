/*
 * OperandType.java
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

package com.strobel.reflection.emit;

public enum OperandType {
    /**
     * Opcode is not followed by any operands.
     */
    NoOperands(1),
    /**
     * Opcode is followed by a byte indicating a type.
     */
    Type(2),
    /**
     * Opcode is followed by a 2-byte branch offset.
     */
    Branch(3),
    /**
     * Opcode is followed by a 4-byte branch offset.
     */
    BranchW(5),
    /**
     * Opcode is followed by a signed byte value.
     */
    Byte(2),
    /**
     * Opcode is followed by a 1-byte index into the constant pool.
     */
    CPRef(2),
    /**
     * Opcode is followed by a 2-byte index into the constant pool.
     */
    CPRefWide(3),
    /**
     * Opcode is followed by a 2-byte index into the constant pool,
     * an unsigned byte value.
     */
    CPRefWideUByte(4),
    /**
     * Opcode is followed by a 2-byte index into the constant pool.,
     * an unsigned byte value, and a zero byte.
     */
    CPRefWideUByteZero(5),
    /**
     * Opcode is followed by variable number of operands, depending
     * on the instruction.
     */
    Dynamic(-1),
    /**
     * Opcode is followed by a 1-byte reference to a local variable.
     */
    Local(2),
    /**
     * Opcode is followed by a 1-byte reference to a local variable,
     * and a signed byte value.
     */
    LocalByte(3),
    /**
     * Opcode is followed by a signed short value.
     */
    Short(3),
    /**
     * Wide opcode is not followed by any operands.
     */
    WideNoOperands(2),
    /**
     * Wide opcode is followed by a 2-byte index into the constant pool.
     */
    WideCPRefWide(4),
    /**
     * Wide opcode is followed by a 2-byte index into the constant pool,
     * and a signed short value.
     */
    WideCPRefWideShort(6),
    /**
     * Opcode was not recognized.
     */
    Unknown(1);

    @SuppressWarnings("PackageVisibleField")
    final int length;

    OperandType(final int length) {
        this.length = length;
    }
}
