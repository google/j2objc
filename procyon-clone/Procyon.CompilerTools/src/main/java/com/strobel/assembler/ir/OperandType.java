/*
 * OperandType.java
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

public enum OperandType {
    /**
     * Opcode is not followed by any operands.
     */
    None(0),
    /**
     * Opcode is followed by a primitive type code.
     */
    PrimitiveTypeCode(1),
    /**
     * Opcode is followed by a type reference.
     */
    TypeReference(2),
    /**
     * Opcode is followed by a type reference and an unsigned byte.
     */
    TypeReferenceU1(3),
    /**
     * Opcode is followed by a method reference.
     */
    DynamicCallSite(4),
    /**
     * Opcode is followed by a method reference.
     */
    MethodReference(2),
    /**
     * Opcode is followed by a field reference.
     */
    FieldReference(2),
    /**
     * Opcode is followed by a 2-byte branch offset.
     */
    BranchTarget(2),
    /**
     * Opcode is followed by a 4-byte branch offset.
     */
    BranchTargetWide(4),
    /**
     * Opcode is followed by a signed byte.
     */
    I1(1),
    /**
     * Opcode is followed by a signed short integer.
     */
    I2(2),
    /**
     * Opcode is followed by a signed long integer.
     */
    I8(8),
    /**
     * Opcode is followed by an unsigned byte.
     */
    Constant(1),
    /**
     * Opcode is followed by an unsigned short integer.
     */
    WideConstant(2),
    /**
     * Opcode is followed by variable number of operands, depending
     * on the instruction.
     */
    Switch(-1),
    /**
     * Opcode is followed by a 1-byte reference to a local variable.
     */
    Local(1),
    /**
     * Opcode is followed by a 1-byte reference to a local variable
     * and a signed byte value.
     */
    LocalI1(2),
    /**
     * Opcode is followed by a 2-byte reference to a local variable
     * and a signed short integer.
     */
    LocalI2(4);

    private final int size;

    OperandType(final int size) {
        this.size = size;
    }

    public final int getBaseSize() {
        return this.size;
    }
}
