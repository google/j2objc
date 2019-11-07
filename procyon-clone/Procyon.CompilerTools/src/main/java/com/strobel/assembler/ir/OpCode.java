/*
 * OpCode.java
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

public enum OpCode {
    NOP(0x00, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.Pop0, StackBehavior.Push0),
    ACONST_NULL(0x01, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.Pop0, StackBehavior.PushA),
    ICONST_M1(0x02, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI4),
    ICONST_0(0x03, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI4),
    ICONST_1(0x04, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI4),
    ICONST_2(0x05, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI4),
    ICONST_3(0x06, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI4),
    ICONST_4(0x07, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI4),
    ICONST_5(0x08, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI4),
    LCONST_0(0x09, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI8),
    LCONST_1(0x0A, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI8),
    FCONST_0(0x0B, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushR4),
    FCONST_1(0x0C, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushR4),
    FCONST_2(0x0D, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushR4),
    DCONST_0(0x0E, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushR8),
    DCONST_1(0x0F, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushR8),
    BIPUSH(0x10, FlowControl.Next, OpCodeType.Primitive, OperandType.I1, StackBehavior.Pop0, StackBehavior.PushI4),
    SIPUSH(0x11, FlowControl.Next, OpCodeType.Primitive, OperandType.I2, StackBehavior.Pop0, StackBehavior.PushI4),
    LDC(0x12, FlowControl.Next, OpCodeType.Primitive, OperandType.Constant, StackBehavior.Pop0, StackBehavior.Push1),
    LDC_W(0x13, FlowControl.Next, OpCodeType.Primitive, OperandType.WideConstant, StackBehavior.Pop0, StackBehavior.Push1),
    LDC2_W(0x14, FlowControl.Next, OpCodeType.Primitive, OperandType.WideConstant, StackBehavior.Pop0, StackBehavior.Push2),
    ILOAD(0x15, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.Pop0, StackBehavior.PushI4),
    LLOAD(0x16, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.Pop0, StackBehavior.PushI8),
    FLOAD(0x17, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.Pop0, StackBehavior.PushR4),
    DLOAD(0x18, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.Pop0, StackBehavior.PushR8),
    ALOAD(0x19, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.Pop0, StackBehavior.PushA),
    ILOAD_0(0x1A, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI4),
    ILOAD_1(0x1B, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI4),
    ILOAD_2(0x1C, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI4),
    ILOAD_3(0x1D, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI4),
    LLOAD_0(0x1E, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI8),
    LLOAD_1(0x1F, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI8),
    LLOAD_2(0x20, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI8),
    LLOAD_3(0x21, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushI8),
    FLOAD_0(0x22, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushR4),
    FLOAD_1(0x23, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushR4),
    FLOAD_2(0x24, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushR4),
    FLOAD_3(0x25, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushR4),
    DLOAD_0(0x26, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushR8),
    DLOAD_1(0x27, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushR8),
    DLOAD_2(0x28, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushR8),
    DLOAD_3(0x29, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushR8),
    ALOAD_0(0x2A, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushA),
    ALOAD_1(0x2B, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushA),
    ALOAD_2(0x2C, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushA),
    ALOAD_3(0x2D, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.Pop0, StackBehavior.PushA),
    IALOAD(0x2E, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopI4_PopA, StackBehavior.PushI4),
    LALOAD(0x2F, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopI4_PopA, StackBehavior.PushI8),
    FALOAD(0x30, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopI4_PopA, StackBehavior.PushR4),
    DALOAD(0x31, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopI4_PopA, StackBehavior.PushR8),
    AALOAD(0x32, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopI4_PopA, StackBehavior.PushA),
    BALOAD(0x33, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopI4_PopA, StackBehavior.PushI4),
    CALOAD(0x34, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopI4_PopA, StackBehavior.PushI4),
    SALOAD(0x35, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopI4_PopA, StackBehavior.PushI4),
    ISTORE(0x36, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.PopI4, StackBehavior.Push0),
    LSTORE(0x37, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.PopI8, StackBehavior.Push0),
    FSTORE(0x38, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.PopR4, StackBehavior.Push0),
    DSTORE(0x39, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.PopR8, StackBehavior.Push0),
    ASTORE(0x3A, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.PopA, StackBehavior.Push0),
    ISTORE_0(0x3B, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopI4, StackBehavior.Push0),
    ISTORE_1(0x3C, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopI4, StackBehavior.Push0),
    ISTORE_2(0x3D, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopI4, StackBehavior.Push0),
    ISTORE_3(0x3E, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopI4, StackBehavior.Push0),
    LSTORE_0(0x3F, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopI8, StackBehavior.Push0),
    LSTORE_1(0x40, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopI8, StackBehavior.Push0),
    LSTORE_2(0x41, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopI8, StackBehavior.Push0),
    LSTORE_3(0x42, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopI8, StackBehavior.Push0),
    FSTORE_0(0x43, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopR4, StackBehavior.Push0),
    FSTORE_1(0x44, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopR4, StackBehavior.Push0),
    FSTORE_2(0x45, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopR4, StackBehavior.Push0),
    FSTORE_3(0x46, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopR4, StackBehavior.Push0),
    DSTORE_0(0x47, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopR8, StackBehavior.Push0),
    DSTORE_1(0x48, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopR8, StackBehavior.Push0),
    DSTORE_2(0x49, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopR8, StackBehavior.Push0),
    DSTORE_3(0x4A, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopR8, StackBehavior.Push0),
    ASTORE_0(0x4B, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopA, StackBehavior.Push0),
    ASTORE_1(0x4C, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopA, StackBehavior.Push0),
    ASTORE_2(0x4D, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopA, StackBehavior.Push0),
    ASTORE_3(0x4E, FlowControl.Next, OpCodeType.Macro, OperandType.None, StackBehavior.PopA, StackBehavior.Push0),
    IASTORE(0x4F, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopI4_PopI4_PopA, StackBehavior.Push0),
    LASTORE(0x50, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopI8_PopI4_PopA, StackBehavior.Push0),
    FASTORE(0x51, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopR4_PopI4_PopA, StackBehavior.Push0),
    DASTORE(0x52, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopR8_PopI4_PopA, StackBehavior.Push0),
    AASTORE(0x53, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopA_PopI4_PopA, StackBehavior.Push0),
    BASTORE(0x54, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopI4_PopI4_PopA, StackBehavior.Push0),
    CASTORE(0x55, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopI4_PopI4_PopA, StackBehavior.Push0),
    SASTORE(0x56, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopI4_PopI4_PopA, StackBehavior.Push0),
    POP(0x57, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.Pop1, StackBehavior.Push0),
    POP2(0x58, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.Pop2, StackBehavior.Push0),
    DUP(0x59, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.Pop1, StackBehavior.Push1_Push1),
    DUP_X1(0x5A, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.Pop1_Pop1, StackBehavior.Push1_Push1_Push1),
    DUP_X2(0x5B, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.Pop2_Pop1, StackBehavior.Push1_Push2_Push1),
    DUP2(0x5C, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.Pop2, StackBehavior.Push2_Push2),
    DUP2_X1(0x5D, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.Pop1_Pop2, StackBehavior.Push2_Push1_Push2),
    DUP2_X2(0x5E, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.Pop2_Pop2, StackBehavior.Push2_Push2_Push2),
    SWAP(0x5F, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.Pop1_Pop1, StackBehavior.Push1_Push1),
    IADD(0x60, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4_PopI4, StackBehavior.PushI4),
    LADD(0x61, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI8_PopI8, StackBehavior.PushI8),
    FADD(0x62, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR4_PopR4, StackBehavior.PushR4),
    DADD(0x63, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR8_PopR8, StackBehavior.PushR8),
    ISUB(0x64, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4_PopI4, StackBehavior.PushI4),
    LSUB(0x65, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI8_PopI8, StackBehavior.PushI8),
    FSUB(0x66, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR4_PopR4, StackBehavior.PushR4),
    DSUB(0x67, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR8_PopR8, StackBehavior.PushR8),
    IMUL(0x68, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4_PopI4, StackBehavior.PushI4),
    LMUL(0x69, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI8_PopI8, StackBehavior.PushI8),
    FMUL(0x6A, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR4_PopR4, StackBehavior.PushR4),
    DMUL(0x6B, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR8_PopR8, StackBehavior.PushR8),
    IDIV(0x6C, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4_PopI4, StackBehavior.PushI4),
    LDIV(0x6D, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI8_PopI8, StackBehavior.PushI8),
    FDIV(0x6E, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR4_PopR4, StackBehavior.PushR4),
    DDIV(0x6F, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR8_PopR8, StackBehavior.PushR8),
    IREM(0x70, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4_PopI4, StackBehavior.PushI4),
    LREM(0x71, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI8_PopI8, StackBehavior.PushI8),
    FREM(0x72, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR4_PopR4, StackBehavior.PushR4),
    DREM(0x73, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR8_PopR8, StackBehavior.PushR8),
    INEG(0x74, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4, StackBehavior.PushI4),
    LNEG(0x75, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI8, StackBehavior.PushI8),
    FNEG(0x76, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR4, StackBehavior.PushR4),
    DNEG(0x77, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR8, StackBehavior.PushR8),
    ISHL(0x78, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4_PopI4, StackBehavior.PushI4),
    LSHL(0x79, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4_PopI8, StackBehavior.PushI8),
    ISHR(0x7A, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4_PopI4, StackBehavior.PushI4),
    LSHR(0x7B, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4_PopI8, StackBehavior.PushI8),
    IUSHR(0x7C, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4_PopI4, StackBehavior.PushI4),
    LUSHR(0x7D, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4_PopI8, StackBehavior.PushI8),
    IAND(0x7E, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4_PopI4, StackBehavior.PushI4),
    LAND(0x7F, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI8_PopI8, StackBehavior.PushI8),
    IOR(0x80, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4_PopI4, StackBehavior.PushI4),
    LOR(0x81, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI8_PopI8, StackBehavior.PushI8),
    IXOR(0x82, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4_PopI4, StackBehavior.PushI4),
    LXOR(0x83, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI8_PopI8, StackBehavior.PushI8),
    IINC(0x84, FlowControl.Next, OpCodeType.Primitive, OperandType.LocalI1, StackBehavior.Pop0, StackBehavior.Push0),
    I2L(0x85, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4, StackBehavior.PushI8),
    I2F(0x86, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4, StackBehavior.PushR4),
    I2D(0x87, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4, StackBehavior.PushR8),
    L2I(0x88, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI8, StackBehavior.PushI4),
    L2F(0x89, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI8, StackBehavior.PushR4),
    L2D(0x8A, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI8, StackBehavior.PushR8),
    F2I(0x8B, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR4, StackBehavior.PushI4),
    F2L(0x8C, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR4, StackBehavior.PushI8),
    F2D(0x8D, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR4, StackBehavior.PushR8),
    D2I(0x8E, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR8, StackBehavior.PushI4),
    D2L(0x8F, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR8, StackBehavior.PushI8),
    D2F(0x90, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR8, StackBehavior.PushR4),
    I2B(0x91, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4, StackBehavior.PushI4),
    I2C(0x92, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4, StackBehavior.PushI4),
    I2S(0x93, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4, StackBehavior.PushI4),
    LCMP(0x94, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI8_PopI8, StackBehavior.PushI4),
    FCMPL(0x95, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR4_PopR4, StackBehavior.PushI4),
    FCMPG(0x96, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR4_PopR4, StackBehavior.PushI4),
    DCMPL(0x97, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR8_PopR8, StackBehavior.PushI4),
    DCMPG(0x98, FlowControl.Next, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR8_PopR8, StackBehavior.PushI4),
    IFEQ(0x99, FlowControl.ConditionalBranch, OpCodeType.Primitive, OperandType.BranchTarget, StackBehavior.PopI4, StackBehavior.Push0),
    IFNE(0x9A, FlowControl.ConditionalBranch, OpCodeType.Primitive, OperandType.BranchTarget, StackBehavior.PopI4, StackBehavior.Push0),
    IFLT(0x9B, FlowControl.ConditionalBranch, OpCodeType.Primitive, OperandType.BranchTarget, StackBehavior.PopI4, StackBehavior.Push0),
    IFGE(0x9C, FlowControl.ConditionalBranch, OpCodeType.Primitive, OperandType.BranchTarget, StackBehavior.PopI4, StackBehavior.Push0),
    IFGT(0x9D, FlowControl.ConditionalBranch, OpCodeType.Primitive, OperandType.BranchTarget, StackBehavior.PopI4, StackBehavior.Push0),
    IFLE(0x9E, FlowControl.ConditionalBranch, OpCodeType.Primitive, OperandType.BranchTarget, StackBehavior.PopI4, StackBehavior.Push0),
    IF_ICMPEQ(0x9F, FlowControl.ConditionalBranch, OpCodeType.Macro, OperandType.BranchTarget, StackBehavior.PopI4_PopI4, StackBehavior.Push0),
    IF_ICMPNE(0xA0, FlowControl.ConditionalBranch, OpCodeType.Macro, OperandType.BranchTarget, StackBehavior.PopI4_PopI4, StackBehavior.Push0),
    IF_ICMPLT(0xA1, FlowControl.ConditionalBranch, OpCodeType.Macro, OperandType.BranchTarget, StackBehavior.PopI4_PopI4, StackBehavior.Push0),
    IF_ICMPGE(0xA2, FlowControl.ConditionalBranch, OpCodeType.Macro, OperandType.BranchTarget, StackBehavior.PopI4_PopI4, StackBehavior.Push0),
    IF_ICMPGT(0xA3, FlowControl.ConditionalBranch, OpCodeType.Macro, OperandType.BranchTarget, StackBehavior.PopI4_PopI4, StackBehavior.Push0),
    IF_ICMPLE(0xA4, FlowControl.ConditionalBranch, OpCodeType.Macro, OperandType.BranchTarget, StackBehavior.PopI4_PopI4, StackBehavior.Push0),
    IF_ACMPEQ(0xA5, FlowControl.ConditionalBranch, OpCodeType.Macro, OperandType.BranchTarget, StackBehavior.PopA_PopA, StackBehavior.Push0),
    IF_ACMPNE(0xA6, FlowControl.ConditionalBranch, OpCodeType.Macro, OperandType.BranchTarget, StackBehavior.PopA_PopA, StackBehavior.Push0),
    GOTO(0xA7, FlowControl.Branch, OpCodeType.Primitive, OperandType.BranchTarget, StackBehavior.Pop0, StackBehavior.Push0),
    JSR(0xA8, FlowControl.Branch, OpCodeType.Primitive, OperandType.BranchTarget, StackBehavior.Pop0, StackBehavior.PushAddress),
    RET(0xA9, FlowControl.Branch, OpCodeType.Primitive, OperandType.Local, StackBehavior.Pop0, StackBehavior.Push0),
    TABLESWITCH(0xAA, FlowControl.Branch, OpCodeType.Primitive, OperandType.Switch, StackBehavior.PopI4, StackBehavior.Push0),
    LOOKUPSWITCH(0xAB, FlowControl.Branch, OpCodeType.Primitive, OperandType.Switch, StackBehavior.PopI4, StackBehavior.Push0),
    IRETURN(0xAC, FlowControl.Return, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI4, StackBehavior.Push0),
    LRETURN(0xAD, FlowControl.Return, OpCodeType.Primitive, OperandType.None, StackBehavior.PopI8, StackBehavior.Push0),
    FRETURN(0xAE, FlowControl.Return, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR4, StackBehavior.Push0),
    DRETURN(0xAF, FlowControl.Return, OpCodeType.Primitive, OperandType.None, StackBehavior.PopR8, StackBehavior.Push0),
    ARETURN(0xB0, FlowControl.Return, OpCodeType.Primitive, OperandType.None, StackBehavior.PopA, StackBehavior.Push0),
    RETURN(0xB1, FlowControl.Return, OpCodeType.Primitive, OperandType.None, StackBehavior.Pop0, StackBehavior.Push0),
    GETSTATIC(0xB2, FlowControl.Next, OpCodeType.ObjectModel, OperandType.FieldReference, StackBehavior.Pop0, StackBehavior.Push1),
    PUTSTATIC(0xB3, FlowControl.Next, OpCodeType.ObjectModel, OperandType.FieldReference, StackBehavior.Pop1, StackBehavior.Push0),
    GETFIELD(0xB4, FlowControl.Next, OpCodeType.ObjectModel, OperandType.FieldReference, StackBehavior.PopA, StackBehavior.Push1),
    PUTFIELD(0xB5, FlowControl.Next, OpCodeType.ObjectModel, OperandType.FieldReference, StackBehavior.Pop1_PopA, StackBehavior.Push0),
    INVOKEVIRTUAL(0xB6, FlowControl.Call, OpCodeType.ObjectModel, OperandType.MethodReference, StackBehavior.VarPop, StackBehavior.VarPush),
    INVOKESPECIAL(0xB7, FlowControl.Call, OpCodeType.ObjectModel, OperandType.MethodReference, StackBehavior.VarPop, StackBehavior.VarPush),
    INVOKESTATIC(0xB8, FlowControl.Call, OpCodeType.Primitive, OperandType.MethodReference, StackBehavior.VarPop, StackBehavior.VarPush),
    INVOKEINTERFACE(0xB9, FlowControl.Call, OpCodeType.ObjectModel, OperandType.MethodReference, StackBehavior.VarPop, StackBehavior.VarPush),
    INVOKEDYNAMIC(0xBA, FlowControl.Call, OpCodeType.ObjectModel, OperandType.DynamicCallSite, StackBehavior.VarPop, StackBehavior.VarPush),
    NEW(0xBB, FlowControl.Next, OpCodeType.ObjectModel, OperandType.TypeReference, StackBehavior.Pop0, StackBehavior.PushA),
    NEWARRAY(0xBC, FlowControl.Next, OpCodeType.ObjectModel, OperandType.PrimitiveTypeCode, StackBehavior.PopI4, StackBehavior.PushA),
    ANEWARRAY(0xBD, FlowControl.Next, OpCodeType.ObjectModel, OperandType.TypeReference, StackBehavior.PopI4, StackBehavior.PushA),
    ARRAYLENGTH(0xBE, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopA, StackBehavior.PushI4),
    ATHROW(0xBF, FlowControl.Throw, OpCodeType.ObjectModel, OperandType.None, StackBehavior.VarPop, StackBehavior.Push0),
    CHECKCAST(0xC0, FlowControl.Next, OpCodeType.ObjectModel, OperandType.TypeReference, StackBehavior.PopA, StackBehavior.PushA),
    INSTANCEOF(0xC1, FlowControl.Next, OpCodeType.ObjectModel, OperandType.TypeReference, StackBehavior.PopA, StackBehavior.PushI4),
    MONITORENTER(0xC2, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopA, StackBehavior.Push0),
    MONITOREXIT(0xC3, FlowControl.Next, OpCodeType.ObjectModel, OperandType.None, StackBehavior.PopA, StackBehavior.Push0),
    MULTIANEWARRAY(0xC5, FlowControl.Next, OpCodeType.ObjectModel, OperandType.TypeReferenceU1, StackBehavior.VarPop, StackBehavior.PushA),
    IFNULL(0xC6, FlowControl.ConditionalBranch, OpCodeType.Primitive, OperandType.BranchTarget, StackBehavior.PopA, StackBehavior.Push0),
    IFNONNULL(0xC7, FlowControl.ConditionalBranch, OpCodeType.Primitive, OperandType.BranchTarget, StackBehavior.PopA, StackBehavior.Push0),
    GOTO_W(0xC8, FlowControl.Branch, OpCodeType.Primitive, OperandType.BranchTargetWide, StackBehavior.Pop0, StackBehavior.Push0),
    JSR_W(0xC9, FlowControl.Branch, OpCodeType.Primitive, OperandType.BranchTargetWide, StackBehavior.Pop0, StackBehavior.PushAddress),
    BREAKPOINT(0xCA, FlowControl.Breakpoint, OpCodeType.Primitive, OperandType.None, StackBehavior.Pop0, StackBehavior.Push0),
    ILOAD_W(0xC415, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.Pop0, StackBehavior.PushI4),
    LLOAD_W(0xC416, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.Pop0, StackBehavior.PushI8),
    FLOAD_W(0xC417, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.Pop0, StackBehavior.PushR4),
    DLOAD_W(0xC418, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.Pop0, StackBehavior.PushR8),
    ALOAD_W(0xC419, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.Pop0, StackBehavior.PushA),
    ISTORE_W(0xC436, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.PopI4, StackBehavior.Push0),
    LSTORE_W(0xC437, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.PopI8, StackBehavior.Push0),
    FSTORE_W(0xC438, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.PopR4, StackBehavior.Push0),
    DSTORE_W(0xC439, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.PopR8, StackBehavior.Push0),
    ASTORE_W(0xC43A, FlowControl.Next, OpCodeType.Primitive, OperandType.Local, StackBehavior.PopA, StackBehavior.Push0),
    IINC_W(0xC484, FlowControl.Next, OpCodeType.Primitive, OperandType.LocalI2, StackBehavior.Pop0, StackBehavior.Push0),
    RET_W(0xC4A9, FlowControl.Branch, OpCodeType.Primitive, OperandType.Local, StackBehavior.Pop0, StackBehavior.Push0),
    LEAVE(0xFE, FlowControl.Branch, OpCodeType.Primitive, OperandType.None, StackBehavior.Pop0, StackBehavior.Push0),
    ENDFINALLY(0xFF, FlowControl.Branch, OpCodeType.Primitive, OperandType.None, StackBehavior.Pop0, StackBehavior.Push0);

    private OpCode(
        final int code,
        final FlowControl flowControl,
        final OpCodeType opCodeType,
        final OperandType operandType,
        final StackBehavior stackBehaviorPop,
        final StackBehavior stackBehaviorPush) {

        _code = code;
        _flowControl = flowControl;
        _opCodeType = opCodeType;
        _operandType = operandType;
        _stackBehaviorPop = stackBehaviorPop;
        _stackBehaviorPush = stackBehaviorPush;
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

    public FlowControl getFlowControl() {
        return _flowControl;
    }

    public OpCodeType getOpCodeType() {
        return _opCodeType;
    }

    public StackBehavior getStackBehaviorPop() {
        return _stackBehaviorPop;
    }

    public StackBehavior getStackBehaviorPush() {
        return _stackBehaviorPush;
    }

    public boolean hasVariableStackBehavior() {
        return _stackBehaviorPop == StackBehavior.VarPop ||
               _stackBehaviorPush == StackBehavior.VarPush;
    }

    public boolean isReturn() {
        return _flowControl == FlowControl.Return;
    }

    public boolean isThrow() {
        return _flowControl == FlowControl.Throw;
    }

    public boolean isInvoke() {
        switch (this) {
            case INVOKEVIRTUAL:
            case INVOKESPECIAL:
            case INVOKESTATIC:
            case INVOKEINTERFACE:
            case INVOKEDYNAMIC:
                return true;
            default:
                return false;
        }
    }

    public boolean isJumpToSubroutine() {
        switch (this) {
            case JSR:
            case JSR_W:
                return true;
            default:
                return false;
        }
    }

    public boolean isReturnFromSubroutine() {
        switch (this) {
            case RET:
            case RET_W:
                return true;
            default:
                return false;
        }
    }

    public boolean isLeave() {
        switch (this) {
            case JSR:
            case JSR_W:
            case LEAVE:
            case ENDFINALLY:
                return true;
            default:
                return false;
        }
    }

    public boolean isBranch() {
        switch (_flowControl) {
            case Branch:
            case ConditionalBranch:
            case Return:
            case Throw:
                return true;
            default:
                return false;
        }
    }

    public boolean isGoto() {
        switch (this) {
            case GOTO:
            case GOTO_W:
                return true;
            default:
                return false;
        }
    }

    public boolean isUnconditionalBranch() {
        switch (_flowControl) {
            case Branch:
            case Return:
            case Throw:
                return true;
            default:
                return false;
        }
    }

    public boolean isMoveInstruction() {
        return isLoad() || isStore();
    }

    public boolean isLoad() {
        switch (this) {
            case ILOAD:
            case LLOAD:
            case FLOAD:
            case DLOAD:
            case ALOAD:
            case ILOAD_0:
            case ILOAD_1:
            case ILOAD_2:
            case ILOAD_3:
            case LLOAD_0:
            case LLOAD_1:
            case LLOAD_2:
            case LLOAD_3:
            case FLOAD_0:
            case FLOAD_1:
            case FLOAD_2:
            case FLOAD_3:
            case DLOAD_0:
            case DLOAD_1:
            case DLOAD_2:
            case DLOAD_3:
            case ALOAD_0:
            case ALOAD_1:
            case ALOAD_2:
            case ALOAD_3:
                return true;

            case ILOAD_W:
            case LLOAD_W:
            case FLOAD_W:
            case DLOAD_W:
            case ALOAD_W:
                return true;

            case RET:
            case RET_W:
                return true;

            default:
                return false;
        }
    }

    public boolean isStore() {
        switch (this) {
            case ISTORE:
            case LSTORE:
            case FSTORE:
            case DSTORE:
            case ASTORE:
            case ISTORE_0:
            case ISTORE_1:
            case ISTORE_2:
            case ISTORE_3:
            case LSTORE_0:
            case LSTORE_1:
            case LSTORE_2:
            case LSTORE_3:
            case FSTORE_0:
            case FSTORE_1:
            case FSTORE_2:
            case FSTORE_3:
            case DSTORE_0:
            case DSTORE_1:
            case DSTORE_2:
            case DSTORE_3:
            case ASTORE_0:
            case ASTORE_1:
            case ASTORE_2:
            case ASTORE_3:
                return true;

            case ISTORE_W:
            case LSTORE_W:
            case FSTORE_W:
            case DSTORE_W:
            case ASTORE_W:
                return true;

            default:
                return false;
        }
    }

    public boolean isArrayLoad() {
        switch (this) {
            case IALOAD:
            case LALOAD:
            case FALOAD:
            case DALOAD:
            case AALOAD:
                return true;

            default:
                return false;
        }
    }

    public boolean isArrayStore() {
        switch (this) {
            case IASTORE:
            case LASTORE:
            case FASTORE:
            case DASTORE:
            case AASTORE:
                return true;

            default:
                return false;
        }
    }

    public int getSize() {
        return ((_code >> 8) == 0xC4) ? 2 : 1;
    }

    public int getStackChange() {
        return stackChange[_code & 0xFF];
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

    public boolean canThrow() {
        if (_opCodeType == OpCodeType.ObjectModel) {
            return this != INSTANCEOF;
        }

        switch (this) {
            case IDIV:
            case LDIV:
                return true;

            case IREM:
            case LREM:
                return true;

            default:
                return false;
        }
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
    private final FlowControl _flowControl;
    private final OpCodeType _opCodeType;
    private final OperandType _operandType;
    private final StackBehavior _stackBehaviorPop;
    private final StackBehavior _stackBehaviorPush;

    /**
     * Get the OpCode for a simple standard 1-byte opcode.
     */
    public static OpCode get(final int code) {
        return getOpcodeBlock(code >> 8)[code & 0xff];
    }

    private static OpCode[] getOpcodeBlock(final int prefix) {
        switch (prefix) {
            case STANDARD:
                return standardOpCodes;
            case WIDE:
                return wideOpCodes;
            default:
                return null;
        }
    }

    /**
     * The byte prefix for the wide instructions.
     */
    public static final int STANDARD = 0x00;
    public static final int WIDE = 0xC4;

    private static final OpCode[] standardOpCodes = new OpCode[256];
    private static final OpCode[] wideOpCodes = new OpCode[256];

    static {
        for (final OpCode o : values()) {
            getOpcodeBlock(o._code >> 8)[o._code & 0xff] = o;
        }
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
        /* GETSTATIC */ 1,
        /* PUTSTATIC */ -1,
        /* GETFIELD */ 1,
        /* PUTFIELD */ -1,
        /* INVOKEVIRTUAL */ -1,         // pops 'this' (unless static)
        /* INVOKESPECIAL */ -1,         // but needs to account for
        /* INVOKESTATIC */ 0,           // parameters and return type
        /* INVOKEINTERFACE */ -1,       //
        /* INVOKEDYNAMIC */ -1,
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
