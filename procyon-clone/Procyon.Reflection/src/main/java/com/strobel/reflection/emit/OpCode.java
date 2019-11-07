/*
 * OpCode.java
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

import static com.strobel.reflection.emit.OperandType.*;

/**
 * @author strobelm
 */
public enum OpCode {
    NOP(0x0),
    ACONST_NULL(0x1),
    ICONST_M1(0x2),
    ICONST_0(0x3),
    ICONST_1(0x4),
    ICONST_2(0x5),
    ICONST_3(0x6),
    ICONST_4(0x7),
    ICONST_5(0x8),
    LCONST_0(0x9),
    LCONST_1(0xa),
    FCONST_0(0xb),
    FCONST_1(0xc),
    FCONST_2(0xd),
    DCONST_0(0xe),
    DCONST_1(0xf),
    BIPUSH(0x10, Byte),
    SIPUSH(0x11, Short),
    LDC(0x12, CPRef),
    LDC_W(0x13, CPRefWide),
    LDC2_W(0x14, CPRefWide),
    ILOAD(0x15, Local),
    LLOAD(0x16, Local),
    FLOAD(0x17, Local),
    DLOAD(0x18, Local),
    ALOAD(0x19, Local),
    ILOAD_0(0x1a),
    ILOAD_1(0x1b),
    ILOAD_2(0x1c),
    ILOAD_3(0x1d),
    LLOAD_0(0x1e),
    LLOAD_1(0x1f),
    LLOAD_2(0x20),
    LLOAD_3(0x21),
    FLOAD_0(0x22),
    FLOAD_1(0x23),
    FLOAD_2(0x24),
    FLOAD_3(0x25),
    DLOAD_0(0x26),
    DLOAD_1(0x27),
    DLOAD_2(0x28),
    DLOAD_3(0x29),
    ALOAD_0(0x2a),
    ALOAD_1(0x2b),
    ALOAD_2(0x2c),
    ALOAD_3(0x2d),
    IALOAD(0x2e),
    LALOAD(0x2f),
    FALOAD(0x30),
    DALOAD(0x31),
    AALOAD(0x32),
    BALOAD(0x33),
    CALOAD(0x34),
    SALOAD(0x35),
    ISTORE(0x36, Local),
    LSTORE(0x37, Local),
    FSTORE(0x38, Local),
    DSTORE(0x39, Local),
    ASTORE(0x3a, Local),
    ISTORE_0(0x3b),
    ISTORE_1(0x3c),
    ISTORE_2(0x3d),
    ISTORE_3(0x3e),
    LSTORE_0(0x3f),
    LSTORE_1(0x40),
    LSTORE_2(0x41),
    LSTORE_3(0x42),
    FSTORE_0(0x43),
    FSTORE_1(0x44),
    FSTORE_2(0x45),
    FSTORE_3(0x46),
    DSTORE_0(0x47),
    DSTORE_1(0x48),
    DSTORE_2(0x49),
    DSTORE_3(0x4a),
    ASTORE_0(0x4b),
    ASTORE_1(0x4c),
    ASTORE_2(0x4d),
    ASTORE_3(0x4e),
    IASTORE(0x4f),
    LASTORE(0x50),
    FASTORE(0x51),
    DASTORE(0x52),
    AASTORE(0x53),
    BASTORE(0x54),
    CASTORE(0x55),
    SASTORE(0x56),
    POP(0x57),
    POP2(0x58),
    DUP(0x59),
    DUP_X1(0x5a),
    DUP_X2(0x5b),
    DUP2(0x5c),
    DUP2_X1(0x5d),
    DUP2_X2(0x5e),
    SWAP(0x5f),
    IADD(0x60),
    LADD(0x61),
    FADD(0x62),
    DADD(0x63),
    ISUB(0x64),
    LSUB(0x65),
    FSUB(0x66),
    DSUB(0x67),
    IMUL(0x68),
    LMUL(0x69),
    FMUL(0x6a),
    DMUL(0x6b),
    IDIV(0x6c),
    LDIV(0x6d),
    FDIV(0x6e),
    DDIV(0x6f),
    IREM(0x70),
    LREM(0x71),
    FREM(0x72),
    DREM(0x73),
    INEG(0x74),
    LNEG(0x75),
    FNEG(0x76),
    DNEG(0x77),
    ISHL(0x78),
    LSHL(0x79),
    ISHR(0x7a),
    LSHR(0x7b),
    IUSHR(0x7c),
    LUSHR(0x7d),
    IAND(0x7e),
    LAND(0x7f),
    IOR(0x80),
    LOR(0x81),
    IXOR(0x82),
    LXOR(0x83),
    IINC(0x84, LocalByte),
    I2L(0x85),
    I2F(0x86),
    I2D(0x87),
    L2I(0x88),
    L2F(0x89),
    L2D(0x8a),
    F2I(0x8b),
    F2L(0x8c),
    F2D(0x8d),
    D2I(0x8e),
    D2L(0x8f),
    D2F(0x90),
    I2B(0x91),
    I2C(0x92),
    I2S(0x93),
    LCMP(0x94),
    FCMPL(0x95),
    FCMPG(0x96),
    DCMPL(0x97),
    DCMPG(0x98),
    IFEQ(0x99, Branch),
    IFNE(0x9a, Branch),
    IFLT(0x9b, Branch),
    IFGE(0x9c, Branch),
    IFGT(0x9d, Branch),
    IFLE(0x9e, Branch),
    IF_ICMPEQ(0x9f, Branch),
    IF_ICMPNE(0xa0, Branch),
    IF_ICMPLT(0xa1, Branch),
    IF_ICMPGE(0xa2, Branch),
    IF_ICMPGT(0xa3, Branch),
    IF_ICMPLE(0xa4, Branch),
    IF_ACMPEQ(0xa5, Branch),
    IF_ACMPNE(0xa6, Branch),
    GOTO(0xa7, Branch),
    JSR(0xa8, Branch),
    RET(0xa9, Local),
    TABLESWITCH(0xaa, Dynamic),
    LOOKUPSWITCH(0xab, Dynamic),
    IRETURN(0xac),
    LRETURN(0xad),
    FRETURN(0xae),
    DRETURN(0xaf),
    ARETURN(0xb0),
    RETURN(0xb1),
    GETSTATIC(0xb2, CPRefWide),
    PUTSTATIC(0xb3, CPRefWide),
    GETFIELD(0xb4, CPRefWide),
    PUTFIELD(0xb5, CPRefWide),
    INVOKEVIRTUAL(0xb6, CPRefWide),
    INVOKESPECIAL(0xb7, CPRefWide),
    INVOKESTATIC(0xb8, CPRefWide),
    INVOKEINTERFACE(0xb9, CPRefWideUByteZero),
    INVOKEDYNAMIC(0xba, CPRefWideUByteZero),
    NEW(0xbb, CPRefWide),
    NEWARRAY(0xbc, Type),
    ANEWARRAY(0xbd, CPRefWide),
    ARRAYLENGTH(0xbe),
    ATHROW(0xbf),
    CHECKCAST(0xc0, CPRefWide),
    INSTANCEOF(0xc1, CPRefWide),
    MONITORENTER(0xc2),
    MONITOREXIT(0xc3),

    // wide 0xc4
    MULTIANEWARRAY(0xc5, CPRefWideUByte),
    IFNULL(0xc6, Branch),
    IFNONNULL(0xc7, Branch),
    GOTO_W(0xc8, BranchW),
    JSR_W(0xc9, BranchW),

    BREAKPOINT(0xca, NoOperands),

    // wide opcodes
    ILOAD_W(0xc415, WideCPRefWide),
    LLOAD_W(0xc416, WideCPRefWide),
    FLOAD_W(0xc417, WideCPRefWide),
    DLOAD_W(0xc418, WideCPRefWide),
    ALOAD_W(0xc419, WideCPRefWide),
    ISTORE_W(0xc436, WideCPRefWide),
    LSTORE_W(0xc437, WideCPRefWide),
    FSTORE_W(0xc438, WideCPRefWide),
    DSTORE_W(0xc439, WideCPRefWide),
    ASTORE_W(0xc43a, WideCPRefWide),
    IINC_W(0xc484, WideCPRefWideShort),
    RET_W(0xc4a9, WideCPRefWide);

    private OpCode(final int code) {
        this(code, NoOperands);
    }

    private OpCode(final int code, final OperandType operandType) {
        this._code = code;
        this._operandType = operandType;
    }

    public int getCode() {
        return _code;
    }

    public boolean isWide() {
        return ((_code >> 8) & WIDE) == WIDE;
    }
    
    public OperandType getOperandType() {
        return _operandType;
    }

    public int getSize() {
        return ((_code >> 8) == 0xc4) ? 2 : 1;
    }

    public int getSizeWithOperands() {
        return _operandType.length;
    }

    public int getStackChange() {
        return stackChange[_code & 0xFF];
    }

    public OpCode negate() {
        if (this == IFNULL) {
            return IFNONNULL;
        }
        else if (this == IFNONNULL) {
            return IFNULL;
        }
        else {
            return get(((_code + 1) ^ 1) - 1);
        }
    }

    private final int _code;
    private final OperandType _operandType;

    /**
     * Get the OpCode for a simple standard 1-byte opcode.
     */
    public static OpCode get(final int code) {
        return getOpcodeBlock(code >> 8)[code & 0xff];
    }

    private static OpCode[] getOpcodeBlock(final int prefix) {
        switch (prefix) {
            case STANDARD:
                return _standardOpCodes;
            case WIDE:
                return _wideOpCodes;
            default:
                return null;
        }
    }

    /**
     * The byte prefix for the wide instructions.
     */
    public static final int STANDARD = 0x00;
    public static final int WIDE = 0xc4;

    private static OpCode[] _standardOpCodes = new OpCode[256];
    private static OpCode[] _wideOpCodes = new OpCode[256];

    static {
        for (final OpCode o : values()) {
            getOpcodeBlock(o._code >> 8)[o._code & 0xff] = o;
        }
    }

    public boolean endsUnconditionalJumpBlock() {
        switch (this) {
            case GOTO:
            case JSR:
            case RET:
                return true;

            case IRETURN:
            case LRETURN:
            case FRETURN:
            case DRETURN:
            case ARETURN:
            case RETURN:
                return true;

            case ATHROW:
                return true;

            case GOTO_W:
            case JSR_W:
                return true;

            case RET_W:
                return true;
        }

        return false;
    }

    private static final byte[] stackChange = {
        /* NOP */ 0,
        /* ACONST_NULL */ 1,
        /* ICONST_M1 */ 1,
        /* ICONST_0 */ 1,
        /* ICONST_1 */ 1,
        /* ICONST_2 */ 1,
        /* ICONST_3 */ 1,
        /* ICONST_4 */ 1,
        /* ICONST_5 */ 1,
        /* LCONST_0 */ 2,
        /* LCONST_1 */ 2,
        /* FCONST_0 */ 1,
        /* FCONST_1 */ 1,
        /* FCONST_2 */ 1,
        /* DCONST_0 */ 2,
        /* DCONST_1 */ 2,
        /* BIPUSH */ 1,
        /* SIPUSH */ 1,
        /* LDC */ 1,
        /* LDC_W */ 1,
        /* LDC2_W */ 2,
        /* ILOAD */ 1,
        /* LLOAD */ 2,
        /* FLOAD */ 1,
        /* DLOAD */ 2,
        /* ALOAD */ 1,
        /* ILOAD_0 */ 1,
        /* ILOAD_1 */ 1,
        /* ILOAD_2 */ 1,
        /* ILOAD_3 */ 1,
        /* LLOAD_0 */ 2,
        /* LLOAD_1 */ 2,
        /* LLOAD_2 */ 2,
        /* LLOAD_3 */ 2,
        /* FLOAD_0 */ 1,
        /* FLOAD_1 */ 1,
        /* FLOAD_2 */ 1,
        /* FLOAD_3 */ 1,
        /* DLOAD_0 */ 2,
        /* DLOAD_1 */ 2,
        /* DLOAD_2 */ 2,
        /* DLOAD_3 */ 2,
        /* ALOAD_0 */ 1,
        /* ALOAD_1 */ 1,
        /* ALOAD_2 */ 1,
        /* ALOAD_3 */ 1,
        /* IALOAD */ -1,
        /* LALOAD */ 0,
        /* FALOAD */ -1,
        /* DALOAD */ 0,
        /* AALOAD */ -1,
        /* BALOAD */ -1,
        /* CALOAD */ -1,
        /* SALOAD */ -1,
        /* ISTORE */ -1,
        /* LSTORE */ -2,
        /* FSTORE */ -1,
        /* DSTORE */ -2,
        /* ASTORE */ -1,
        /* ISTORE_0 */ -1,
        /* ISTORE_1 */ -1,
        /* ISTORE_2 */ -1,
        /* ISTORE_3 */ -1,
        /* LSTORE_0 */ -2,
        /* LSTORE_1 */ -2,
        /* LSTORE_2 */ -2,
        /* LSTORE_3 */ -2,
        /* FSTORE_0 */ -1,
        /* FSTORE_1 */ -1,
        /* FSTORE_2 */ -1,
        /* FSTORE_3 */ -1,
        /* DSTORE_0 */ -2,
        /* DSTORE_1 */ -2,
        /* DSTORE_2 */ -2,
        /* DSTORE_3 */ -2,
        /* ASTORE_0 */ -1,
        /* ASTORE_1 */ -1,
        /* ASTORE_2 */ -1,
        /* ASTORE_3 */ -1,
        /* IASTORE */ -3,
        /* LASTORE */ -4,
        /* FASTORE */ -3,
        /* DASTORE */ -4,
        /* AASTORE */ -3,
        /* BASTORE */ -3,
        /* CASTORE */ -3,
        /* SASTORE */ -3,
        /* POP */ -1,
        /* POP2 */ -2,
        /* DUP */ 1,
        /* DUP_X1 */ 1,
        /* DUP_X2 */ 1,
        /* DUP2 */ 2,
        /* DUP2_X1 */ 2,
        /* DUP2_X2 */ 2,
        /* SWAP */ 0,
        /* IADD */ -1,
        /* LADD */ -2,
        /* FADD */ -1,
        /* DADD */ -2,
        /* ISUB */ -1,
        /* LSUB */ -2,
        /* FSUB */ -1,
        /* DSUB */ -2,
        /* IMUL */ -1,
        /* LMUL */ -2,
        /* FMUL */ -1,
        /* DMUL */ -2,
        /* IDIV */ -1,
        /* LDIV */ -2,
        /* FDIV */ -1,
        /* DDIV */ -2,
        /* IREM */ -1,
        /* LREM */ -2,
        /* FREM */ -1,
        /* DREM */ -2,
        /* INEG */ 0,
        /* LNEG */ 0,
        /* FNEG */ 0,
        /* DNEG */ 0,
        /* ISHL */ -1,
        /* LSHL */ -1,
        /* ISHR */ -1,
        /* LSHR */ -1,
        /* IUSHR */ -1,
        /* LUSHR */ -1,
        /* IAND */ -1,
        /* LAND */ -2,
        /* IOR */ -1,
        /* LOR */ -2,
        /* IXOR */ -1,
        /* LXOR */ -2,
        /* IINC */ 0,
        /* I2L */ 1,
        /* I2F */ 0,
        /* I2D */ 1,
        /* L2I */ -1,
        /* L2F */ -1,
        /* L2D */ 0,
        /* F2I */ 0,
        /* F2L */ 1,
        /* F2D */ 1,
        /* D2I */ -1,
        /* D2L */ 0,
        /* D2F */ -1,
        /* I2B */ 0,
        /* I2C */ 0,
        /* I2S */ 0,
        /* LCMP */ -3,
        /* FCMPL */ -1,
        /* FCMPG */ -1,
        /* DCMPL */ -3,
        /* DCMPG */ -3,
        /* IFEQ */ -1,
        /* IFNE */ -1,
        /* IFLT */ -1,
        /* IFGE */ -1,
        /* IFGT */ -1,
        /* IFLE */ -1,
        /* IF_ICMPEQ */ -2,
        /* IF_ICMPNE */ -2,
        /* IF_ICMPLT */ -2,
        /* IF_ICMPGE */ -2,
        /* IF_ICMPGT */ -2,
        /* IF_ICMPLE */ -2,
        /* IF_ACMPEQ */ -2,
        /* IF_ACMPNE */ -2,
        /* GOTO */ 0,
        /* JSR */ 1,
        /* RET */ 0,
        /* TABLESWITCH */ -1,
        /* LOOKUPSWITCH */ -1,
        /* IRETURN */ -1,
        /* LRETURN */ -2,
        /* FRETURN */ -1,
        /* DRETURN */ -2,
        /* ARETURN */ -1,
        /* RETURN */ 0,
        /* GETSTATIC */ 0,              // <-+-- pops 'this' (unless static)
        /* PUTSTATIC */ 0,              //   |   but needs to account for
        /* GETFIELD */ 1,               //   |   field type
        /* PUTFIELD */ -1,              // <-+
        /* INVOKEVIRTUAL */ -1,         // <-+-- pops 'this' (unless static)
        /* INVOKESPECIAL */ -1,         //   |   but needs to account for
        /* INVOKESTATIC */ 0,           //   |   parameter and return types
        /* INVOKEINTERFACE */ -1,       // <-+
        /* XXXUNUSEDXXX */ 0,
        /* NEW */ 1,
        /* NEWARRAY */ 0,
        /* ANEWARRAY */ 0,
        /* ARRAYLENGTH */ 0,
        /* ATHROW */ -1,
        /* CHECKCAST */ 0,
        /* INSTANCEOF */ 0,
        /* MONITORENTER */ -1,
        /* MONITOREXIT */ -1,
        /* WIDE */ 0,
        /* MULTIANEWARRAY */ 1,
        /* IFNULL */ -1,
        /* IFNONNULL */ -1,
        /* GOTO_W */ 0,
        /* JSR_W */ 1,
        /* BREAKPOINT */ 0,
        /* LDC_QUICK */ 1,
        /* LDC_W_QUICK */ 1,
        /* LDC2_W_QUICK */ 2,
        /* GETFIELD_QUICK */ 0,
        /* PUTFIELD_QUICK */ 0,
        /* GETFIELD2_QUICK */ 0,
        /* PUTFIELD2_QUICK */ 0,
        /* GETSTATIC_QUICK */ 0,
        /* PUTSTATIC_QUICK */ 0,
        /* GETSTATIC2_QUICK */ 0,
        /* PUTSTATIC2_QUICK */ 0,
        /* INVOKEVIRTUAL_QUICK */ 0,
        /* INVOKENONVIRTUAL_QUICK */ 0,
        /* INVOKESUPER_QUICK */ 0,
        /* INVOKESTATIC_QUICK */ 0,
        /* INVOKEINTERFACE_QUICK */ 0,
        /* INVOKEVIRTUALOBJECT_QUICK */ 0,
        /* XXXUNUSEDXXX */ 0,
        /* NEW_QUICK */ 1,
        /* ANEWARRAY_QUICK */ 1,
        /* MULTIANEWARRAY_QUICK */ 1,
        /* CHECKCAST_QUICK */ -1,
        /* INSTANCEOF_QUICK */ 0,
        /* INVOKEVIRTUAL_QUICK_W */ 0,
        /* GETFIELD_QUICK_W */ 0,
        /* PUTFIELD_QUICK_W */ 0,
        /* XXXUNUSEDXXX */ 0,
        /* XXXUNUSEDXXX */ 0,
        /* XXXUNUSEDXXX */ 0,
        /* XXXUNUSEDXXX */ 0,
        /* XXXUNUSEDXXX */ 0,
        /* XXXUNUSEDXXX */ 0,
        /* XXXUNUSEDXXX */ 0,
        /* XXXUNUSEDXXX */ 0,
        /* XXXUNUSEDXXX */ 0,
        /* XXXUNUSEDXXX */ 0,
        /* XXXUNUSEDXXX */ 0,
        /* XXXUNUSEDXXX */ 0,
        /* XXXUNUSEDXXX */ 0,
        /* XXXUNUSEDXXX */ 0,
        /* XXXUNUSEDXXX */ 0,
        /* XXXUNUSEDXXX */ 0,
        /* XXXUNUSEDXXX */ 0,
        /* XXXUNUSEDXXX */ 0,
        /* XXXUNUSEDXXX */ 0,
        /* XXXUNUSEDXXX */ 0,
        /* XXXUNUSEDXXX */ 0,
        /* XXXUNUSEDXXX */ 0,
        /* XXXUNUSEDXXX */ 0,
        /* XXXUNUSEDXXX */ 0,
        /* XXXUNUSEDXXX */ 0,
        /* IMPDEP1 */ 0,
        /* IMPDEP2 */ 0
    };
}
