/*
 * Instruction.java
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

import com.strobel.assembler.metadata.DynamicCallSite;
import com.strobel.assembler.metadata.FieldReference;
import com.strobel.assembler.metadata.Label;
import com.strobel.assembler.metadata.MethodReference;
import com.strobel.assembler.metadata.SwitchInfo;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.assembler.metadata.VariableReference;
import com.strobel.core.ArrayUtilities;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.DecompilerHelpers;
import com.strobel.decompiler.PlainTextOutput;
import com.strobel.util.ContractUtils;

import java.lang.reflect.Array;

/**
 * User: Mike Strobel
 * Date: 1/6/13
 * Time: 1:31 AM
 */
public final class Instruction implements Comparable<Instruction> {
    private int _offset = -1;
    private OpCode _opCode;
    private Object _operand;
    private Label _label;

    private Instruction _previous;
    private Instruction _next;

    public Instruction(final int offset, final OpCode opCode) {
        _offset = offset;
        _opCode = opCode;
    }

    public Instruction(final OpCode opCode) {
        _opCode = opCode;
        _operand = null;
    }

    public Instruction(final OpCode opCode, final Object operand) {
        _opCode = opCode;
        _operand = operand;
    }

    public Instruction(final OpCode opCode, final Object... operands) {
        _opCode = opCode;
        _operand = VerifyArgument.notNull(operands, "operands");
    }

    public boolean hasOffset() {
        return _offset >= 0;
    }

    public boolean hasOperand() {
        return _operand != null;
    }

    public int getOffset() {
        return _offset;
    }

    public void setOffset(final int offset) {
        _offset = offset;
    }

    public int getEndOffset() {
        return _offset + getSize();
    }

    public OpCode getOpCode() {
        return _opCode;
    }

    public void setOpCode(final OpCode opCode) {
        _opCode = opCode;
    }

    public int getOperandCount() {
        final Object operand = _operand;

        if (operand == null) {
            return 0;
        }

        if (ArrayUtilities.isArray(operand)) {
            return Array.getLength(operand);
        }

        return 1;
    }

    @SuppressWarnings("unchecked")
    public <T> T getOperand(final int index) {
        final Object operand = _operand;

        if (ArrayUtilities.isArray(operand)) {
            VerifyArgument.inRange(0, Array.getLength(operand) - 1, index, "index");
            return (T) Array.get(operand, index);
        }
        else {
            VerifyArgument.inRange(0, 0, index, "index");
            return (T) operand;
        }
    }

    public void setOperand(final Object operand) {
        _operand = operand;
    }

    public boolean hasLabel() {
        return _label != null;
    }

    public Label getLabel() {
        return _label;
    }

    public void setLabel(final Label label) {
        _label = label;
    }

    public Instruction getPrevious() {
        return _previous;
    }

    public void setPrevious(final Instruction previous) {
        _previous = previous;
    }

    public Instruction getNext() {
        return _next;
    }

    public void setNext(final Instruction next) {
        _next = next;
    }

    @Override
    @SuppressWarnings("CloneDoesntCallSuperClone")
    public Instruction clone() {
        final Instruction copy = new Instruction(_opCode, (Object) null);

        copy._offset = _offset;
        copy._label = _label != null ? new Label(_label.getIndex()) : null;

        if (ArrayUtilities.isArray(_operand)) {
            copy._operand = ((Object[]) _operand).clone();
        }
        else {
            copy._operand = _operand;
        }

        return copy;
    }

    @Override
    public String toString() {
        final PlainTextOutput output = new PlainTextOutput();
        DecompilerHelpers.writeInstruction(output, this);
        return output.toString();
    }

    // <editor-fold defaultstate="collapsed" desc="Size Calculation">

    public int getSize() {
        final int opCodeSize = _opCode.getSize();
        final OperandType operandType = _opCode.getOperandType();

        switch (operandType) {
            case None:
                return opCodeSize;

            case PrimitiveTypeCode:
            case TypeReference:
            case TypeReferenceU1:
                return opCodeSize + operandType.getBaseSize();

            case DynamicCallSite: {
                return opCodeSize + operandType.getBaseSize();
            }

            case MethodReference:
                switch (_opCode) {
                    case INVOKEVIRTUAL:
                    case INVOKESPECIAL:
                    case INVOKESTATIC:
                        return opCodeSize + operandType.getBaseSize();
                    case INVOKEINTERFACE:
                        return opCodeSize + operandType.getBaseSize() + 2;
                }
                break;

            case FieldReference:
            case BranchTarget:
            case BranchTargetWide:
            case I1:
            case I2:
            case I8:
            case Constant:
            case WideConstant:
                return opCodeSize + operandType.getBaseSize();

            case Switch:
                final Instruction[] targets = ((SwitchInfo) _operand).getTargets();
                final int relativeOffset = _offset + opCodeSize;
                final int padding = _offset >= 0 ? (4 - (relativeOffset % 4)) % 4 : 0;
                switch (_opCode) {
                    case TABLESWITCH:
                        // op + padding + default + low + high + targets
                        return opCodeSize + padding + (3 * 4) + (targets.length * 4);
                    case LOOKUPSWITCH:
                        // op + padding + default + number of pairs + pairs
                        return opCodeSize + padding + (4 * 2) + (targets.length * 8);
                }
                break;

            case Local:
                return opCodeSize + (_opCode.isWide() ? 2 : 1);

            case LocalI1:
            case LocalI2:
                return opCodeSize + operandType.getBaseSize();
        }

        throw ContractUtils.unreachable();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Factory Methods">

    public static Instruction create(final OpCode opCode) {
        VerifyArgument.notNull(opCode, "opCode");

        if (opCode.getOperandType() != OperandType.None) {
            throw new IllegalArgumentException(String.format("Invalid operand for OpCode %s.", opCode));
        }

        return new Instruction(opCode);
    }

    public static Instruction create(final OpCode opCode, final Instruction target) {
        VerifyArgument.notNull(opCode, "opCode");
        VerifyArgument.notNull(target, "target");

        if (opCode.getOperandType() != OperandType.BranchTarget &&
            opCode.getOperandType() != OperandType.BranchTargetWide) {

            throw new IllegalArgumentException(String.format("Invalid operand for OpCode %s.", opCode));
        }

        return new Instruction(opCode, target);
    }

    public static Instruction create(final OpCode opCode, final SwitchInfo switchInfo) {
        VerifyArgument.notNull(opCode, "opCode");
        VerifyArgument.notNull(switchInfo, "switchInfo");

        if (opCode.getOperandType() != OperandType.Switch) {
            throw new IllegalArgumentException(String.format("Invalid operand for OpCode %s.", opCode));
        }

        return new Instruction(opCode, switchInfo);
    }

    public static Instruction create(final OpCode opCode, final int value) {
        VerifyArgument.notNull(opCode, "opCode");

        if (!checkOperand(opCode.getOperandType(), value)) {
            throw new IllegalArgumentException(String.format("Invalid operand for OpCode %s.", opCode));
        }

        return new Instruction(opCode, value);
    }

    public static Instruction create(final OpCode opCode, final short value) {
        VerifyArgument.notNull(opCode, "opCode");

        if (!checkOperand(opCode.getOperandType(), value)) {
            throw new IllegalArgumentException(String.format("Invalid operand for OpCode %s.", opCode));
        }

        return new Instruction(opCode, value);
    }

    public static Instruction create(final OpCode opCode, final float value) {
        VerifyArgument.notNull(opCode, "opCode");

        if (opCode.getOperandType() != OperandType.Constant && opCode.getOperandType() != OperandType.WideConstant) {
            throw new IllegalArgumentException(String.format("Invalid operand for OpCode %s.", opCode));
        }

        return new Instruction(opCode, value);
    }

    public static Instruction create(final OpCode opCode, final double value) {
        VerifyArgument.notNull(opCode, "opCode");

        if (opCode.getOperandType() != OperandType.WideConstant) {
            throw new IllegalArgumentException(String.format("Invalid operand for OpCode %s.", opCode));
        }

        return new Instruction(opCode, value);
    }

    public static Instruction create(final OpCode opCode, final long value) {
        VerifyArgument.notNull(opCode, "opCode");

        if (opCode.getOperandType() != OperandType.I8 && opCode.getOperandType() != OperandType.WideConstant) {
            throw new IllegalArgumentException(String.format("Invalid operand for OpCode %s.", opCode));
        }

        return new Instruction(opCode, value);
    }

    public static Instruction create(final OpCode opCode, final VariableReference variable) {
        VerifyArgument.notNull(opCode, "opCode");

        if (opCode.getOperandType() != OperandType.Local) {
            throw new IllegalArgumentException(String.format("Invalid operand for OpCode %s.", opCode));
        }

        return new Instruction(opCode, variable);
    }

    public static Instruction create(final OpCode opCode, final VariableReference variable, final int operand) {
        VerifyArgument.notNull(opCode, "opCode");
        VerifyArgument.notNull(variable, "variable");

        if (!checkOperand(opCode.getOperandType(), operand)) {
            throw new IllegalArgumentException(String.format("Invalid operand for OpCode %s.", opCode));
        }

        return new Instruction(opCode, variable, operand);
    }

    public static Instruction create(final OpCode opCode, final TypeReference type) {
        VerifyArgument.notNull(opCode, "opCode");
        VerifyArgument.notNull(type, "type");

        if (!checkOperand(opCode.getOperandType(), type)) {
            throw new IllegalArgumentException(String.format("Invalid operand for OpCode %s.", opCode));
        }

        return new Instruction(opCode, type);
    }

    public static Instruction create(final OpCode opCode, final TypeReference type, final int operand) {
        VerifyArgument.notNull(opCode, "opCode");

        if (!checkOperand(opCode.getOperandType(), type) || !checkOperand(opCode.getOperandType(), operand)) {
            throw new IllegalArgumentException(String.format("Invalid operand for OpCode %s.", opCode));
        }

        return new Instruction(opCode, type, operand);
    }

    public static Instruction create(final OpCode opCode, final MethodReference method) {
        VerifyArgument.notNull(opCode, "opCode");

        if (!checkOperand(opCode.getOperandType(), method)) {
            throw new IllegalArgumentException(String.format("Invalid operand for OpCode %s.", opCode));
        }

        return new Instruction(opCode, method);
    }

    public static Instruction create(final OpCode opCode, final DynamicCallSite callSite) {
        VerifyArgument.notNull(opCode, "opCode");

        if (!checkOperand(opCode.getOperandType(), callSite)) {
            throw new IllegalArgumentException(String.format("Invalid operand for OpCode %s.", opCode));
        }

        return new Instruction(opCode, callSite);
    }

    public static Instruction create(final OpCode opCode, final FieldReference field) {
        VerifyArgument.notNull(opCode, "opCode");

        if (!checkOperand(opCode.getOperandType(), field)) {
            throw new IllegalArgumentException(String.format("Invalid operand for OpCode %s.", opCode));
        }

        return new Instruction(opCode, field);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Operand Checks">

    private static final int U1_MIN_VALUE = 0x00;
    private static final int U1_MAX_VALUE = 0xFF;
    @SuppressWarnings("UnusedDeclaration")
    private static final int U2_MIN_VALUE = 0x0000;
    @SuppressWarnings("UnusedDeclaration")
    private static final int U2_MAX_VALUE = 0xFFFF;

    private static boolean checkOperand(final OperandType operandType, final int value) {
        switch (operandType) {
            case I1:
            case LocalI1:
                return value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE;
            case I2:
            case LocalI2:
                return value >= Short.MIN_VALUE && value <= Short.MAX_VALUE;
            case TypeReferenceU1:
                return value >= U1_MIN_VALUE && value <= U1_MAX_VALUE;
            default:
                return false;
        }
    }

    private static boolean checkOperand(final OperandType operandType, final TypeReference type) {
        VerifyArgument.notNull(type, "type");

        switch (operandType) {
            case PrimitiveTypeCode:
                return type.getSimpleType().isPrimitive();
            case TypeReference:
            case TypeReferenceU1:
                return true;
            default:
                return false;
        }
    }

    private static boolean checkOperand(final OperandType operandType, final DynamicCallSite callSite) {
        VerifyArgument.notNull(callSite, "callSite");

        switch (operandType) {
            case DynamicCallSite:
                return true;
            default:
                return false;
        }
    }

    private static boolean checkOperand(final OperandType operandType, final MethodReference method) {
        VerifyArgument.notNull(method, "method");

        switch (operandType) {
            case MethodReference:
                return true;
            default:
                return false;
        }
    }

    private static boolean checkOperand(final OperandType operandType, final FieldReference field) {
        VerifyArgument.notNull(field, "field");

        switch (operandType) {
            case FieldReference:
                return true;
            default:
                return false;
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Visitor Acceptor">

    public void accept(final InstructionVisitor visitor) {
        if (hasLabel()) {
            visitor.visitLabel(_label);
        }

        switch (_opCode.getOperandType()) {
            case None:
                visitor.visit(_opCode);
                break;

            case PrimitiveTypeCode:
            case TypeReference:
            case TypeReferenceU1:
                visitor.visitType(_opCode, (TypeReference) getOperand(0));
                break;

            case DynamicCallSite:
                visitor.visitDynamicCallSite(_opCode, (DynamicCallSite) _operand);
                break;

            case MethodReference:
                visitor.visitMethod(_opCode, (MethodReference) _operand);
                break;

            case FieldReference:
                visitor.visitField(_opCode, (FieldReference) _operand);
                break;

            case BranchTarget:
            case BranchTargetWide:
                visitor.visitBranch(_opCode, (Instruction) _operand);
                break;

            case I1:
            case I2:
                visitor.visitConstant(_opCode, ((Number) _operand).intValue());
                break;

            case I8:
                visitor.visitConstant(_opCode, ((Number) _operand).longValue());
                break;

            case Constant:
            case WideConstant:
                if (_operand instanceof String) {
                    visitor.visitConstant(_opCode, (String) _operand);
                }
                else if (_operand instanceof TypeReference) {
                    visitor.visitConstant(_opCode, (TypeReference) _operand);
                }
                else {
                    final Number number = (Number) _operand;

                    if (_operand instanceof Long) {
                        visitor.visitConstant(_opCode, number.longValue());
                    }
                    else if (_operand instanceof Float) {
                        visitor.visitConstant(_opCode, number.floatValue());
                    }
                    else if (_operand instanceof Double) {
                        visitor.visitConstant(_opCode, number.doubleValue());
                    }
                    else {
                        visitor.visitConstant(_opCode, number.intValue());
                    }
                }
                break;

            case Switch:
                visitor.visitSwitch(_opCode, (SwitchInfo) _operand);
                break;

            case Local:
                visitor.visitVariable(_opCode, (VariableReference) _operand);
                break;

            case LocalI1:
            case LocalI2:
                visitor.visitVariable(
                    _opCode,
                    this.<VariableReference>getOperand(0),
                    this.<Number>getOperand(1).intValue()
                );
                break;
        }
    }

    @Override
    public final int compareTo(final Instruction o) {
        if (o == null) {
            return 1;
        }

        return Integer.compare(_offset, o._offset);
    }

    // </editor-fold>
}
