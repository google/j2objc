/*
 * MethodReader.java
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

package com.strobel.assembler.metadata;

import com.strobel.assembler.ir.ErrorOperand;
import com.strobel.assembler.ir.Instruction;
import com.strobel.assembler.ir.InstructionCollection;
import com.strobel.assembler.ir.OpCode;
import com.strobel.assembler.ir.OpCodeHelpers;
import com.strobel.assembler.ir.OperandType;
import com.strobel.assembler.ir.attributes.AttributeNames;
import com.strobel.assembler.ir.attributes.CodeAttribute;
import com.strobel.assembler.ir.attributes.ExceptionTableEntry;
import com.strobel.assembler.ir.attributes.LocalVariableTableAttribute;
import com.strobel.assembler.ir.attributes.LocalVariableTableEntry;
import com.strobel.assembler.ir.attributes.SourceAttribute;
import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;

import java.lang.reflect.Modifier;
import java.util.List;

import static java.lang.String.format;

public class MethodReader {
    private final MethodDefinition _methodDefinition;
    private final CodeAttribute _code;
    private final IMetadataScope _scope;
    private final MethodBody _methodBody;
    private final TypeReference _declaringType;
    private final int _modifiers;

    public MethodReader(final MethodDefinition methodDefinition, final IMetadataScope scope) {
        _methodDefinition = VerifyArgument.notNull(methodDefinition, "methodDefinition");
        _scope = VerifyArgument.notNull(scope, "scope");
        _declaringType = methodDefinition.getDeclaringType();
        _modifiers = methodDefinition.getModifiers();
        _code = SourceAttribute.find(AttributeNames.Code, methodDefinition.getSourceAttributes());
        _methodBody = new MethodBody(methodDefinition);
        _methodBody.setCodeSize(_code.getCode().size());
        _methodBody.setMaxStackSize(_code.getMaxStack());
        _methodBody.setMaxLocals(_code.getMaxLocals());
    }

    @SuppressWarnings("ConstantConditions")
    public MethodBody readBody() {
        try {
            return readBodyCore();
        }
        catch (final Throwable t) {
            throw new MethodBodyParseException(
                format(
                    "An error occurred while parsing the bytecode of method '%s:%s'.",
                    _methodDefinition.getFullName(),
                    _methodDefinition.getSignature()
                ),
                t);
        }
    }

    private MethodBody readBodyCore() {
        final Buffer b = _code.getCode();

        b.position(0);

        final InstructionCollection body = _methodBody.getInstructions();
        final VariableDefinitionCollection variables = _methodBody.getVariables();

        final LocalVariableTableAttribute localVariableTable = SourceAttribute.find(
            AttributeNames.LocalVariableTable,
            _code.getAttributes()
        );

        final LocalVariableTableAttribute localVariableTypeTable = SourceAttribute.find(
            AttributeNames.LocalVariableTypeTable,
            _code.getAttributes()
        );

        final boolean hasThis = !Modifier.isStatic(_modifiers);
        final List<ParameterDefinition> parameters = _methodDefinition.getParameters();

        if (hasThis) {
            final ParameterDefinition thisParameter = new ParameterDefinition(
                0,
                "this",
                _declaringType
            );

            final VariableDefinition thisVariable = new VariableDefinition(
                0,
                "this",
                _methodDefinition,
                _declaringType
            );

            thisVariable.setScopeStart(0);
            thisVariable.setScopeEnd(_code.getCodeSize());
            thisVariable.setFromMetadata(false);
            thisVariable.setParameter(thisParameter);

            variables.add(thisVariable);

            _methodBody.setThisParameter(thisParameter);
        }

        for (int i = 0; i < parameters.size(); i++) {
            final ParameterDefinition parameter = parameters.get(i);
            final int variableSlot = parameter.getSlot();

            final VariableDefinition variable = new VariableDefinition(
                variableSlot,
                parameter.getName(),
                _methodDefinition,
                parameter.getParameterType()
            );

            variable.setScopeStart(0);
            variable.setScopeEnd(_code.getCodeSize());
            variable.setTypeKnown(true);
            variable.setFromMetadata(false);
            variable.setParameter(parameter);

            variables.add(variable);
        }

        if (localVariableTable != null) {
            processLocalVariableTable(variables, localVariableTable, parameters);
        }

        if (localVariableTypeTable != null) {
            processLocalVariableTable(variables, localVariableTypeTable, parameters);
        }

        for (final VariableDefinition variable : variables) {
            if (!variable.isFromMetadata()) {
                variable.setScopeStart(-1);
                variable.setScopeEnd(-1);
            }
        }

        @SuppressWarnings("unchecked")
        final Fixup[] fixups = new Fixup[b.size()];

        while (b.position() < b.size()) {
            final int offset = b.position();

            int code = b.readUnsignedByte();

            if (code == OpCode.WIDE) {
                code = code << 8 | b.readUnsignedByte();
            }

            final OpCode op = OpCode.get(code);
            final Instruction instruction;

            switch (op.getOperandType()) {
                case None: {
                    if (op.isLoad() || op.isStore()) {
                        variables.reference(OpCodeHelpers.getLoadStoreMacroArgumentIndex(op), op, offset);
                    }
                    instruction = Instruction.create(op);
                    break;
                }

                case PrimitiveTypeCode: {
                    instruction = Instruction.create(op, BuiltinTypes.fromPrimitiveTypeCode(b.readUnsignedByte()));
                    break;
                }

                case TypeReference: {
                    final int typeToken = b.readUnsignedShort();
                    instruction = Instruction.create(op, _scope.lookupType(typeToken));
                    break;
                }

                case TypeReferenceU1: {
                    instruction = Instruction.create(op, _scope.lookupType(b.readUnsignedShort()), b.readUnsignedByte());
                    break;
                }

                case DynamicCallSite: {
                    instruction = Instruction.create(op, _scope.lookupDynamicCallSite(b.readUnsignedShort()));
                    b.readUnsignedByte();
                    b.readUnsignedByte();
                    break;
                }

                case MethodReference: {
                    instruction = Instruction.create(op, _scope.lookupMethod(b.readUnsignedShort()));

                    if (op == OpCode.INVOKEINTERFACE) {
                        b.readUnsignedByte();
                        b.readUnsignedByte();
                    }

                    break;
                }

                case FieldReference: {
                    instruction = Instruction.create(op, _scope.lookupField(b.readUnsignedShort()));
                    break;
                }

                case BranchTarget:
                case BranchTargetWide: {
                    final int targetOffset;

                    instruction = new Instruction(op);

                    if (op.isWide()) {
                        targetOffset = offset + _scope.<Integer>lookupConstant(b.readUnsignedShort());
                    }
                    else if (op.getOperandType() == OperandType.BranchTargetWide) {
                        targetOffset = offset + b.readInt();
                    }
                    else {
                        targetOffset = offset + (int) b.readShort();
                    }

                    if (targetOffset < offset) {
                        final Instruction target = body.atOffset(targetOffset);

                        if (!target.hasLabel()) {
                            target.setLabel(new Label(targetOffset));
                        }

                        instruction.setOperand(target);
                    }
                    else if (targetOffset == offset) {
                        instruction.setOperand(instruction);
                        instruction.setLabel(new Label(offset));
                    }
                    else if (targetOffset > b.size()) {
                        //
                        // Target is a label after the last instruction.  Insert a dummy NOP.
                        //
                        instruction.setOperand(new Instruction(targetOffset, OpCode.NOP));
                    }
                    else {
                        final Fixup oldFixup = fixups[targetOffset];
                        final Fixup newFixup = new Fixup() {
                            @Override
                            public void fix(final Instruction target) {
                                instruction.setOperand(target);
                            }
                        };

                        fixups[targetOffset] = oldFixup != null ? Fixup.combine(oldFixup, newFixup)
                                                                : newFixup;
                    }

                    break;
                }

                case I1: {
                    instruction = Instruction.create(op, b.readByte());
                    break;
                }

                case I2: {
                    instruction = Instruction.create(op, b.readShort());
                    break;
                }

                case I8: {
                    instruction = Instruction.create(op, b.readLong());
                    break;
                }

                case Constant: {
                    //noinspection RedundantTypeArguments
                    instruction = new Instruction(op, _scope.<Object>lookupConstant(b.readUnsignedByte()));
                    break;
                }

                case WideConstant: {
                    final int constantToken = b.readUnsignedShort();
                    //noinspection RedundantTypeArguments
                    instruction = new Instruction(op, _scope.<Object>lookupConstant(constantToken));
                    break;
                }

                case Switch: {
                    while (b.position() % 4 != 0) {
                        b.readByte();
                    }

                    final SwitchInfo switchInfo = new SwitchInfo();
                    final int defaultOffset = offset + b.readInt();

                    instruction = Instruction.create(op, switchInfo);

                    if (defaultOffset < offset) {
                        switchInfo.setDefaultTarget(body.atOffset(defaultOffset));
                    }
                    else if (defaultOffset == offset) {
                        switchInfo.setDefaultTarget(instruction);
                    }
                    else {
                        switchInfo.setDefaultTarget(new Instruction(defaultOffset, OpCode.NOP));

                        final Fixup oldFixup = fixups[defaultOffset];
                        final Fixup newFixup = new Fixup() {
                            @Override
                            public void fix(final Instruction target) {
                                switchInfo.setDefaultTarget(target);
                            }
                        };

                        fixups[defaultOffset] = oldFixup != null ? Fixup.combine(oldFixup, newFixup)
                                                                 : newFixup;
                    }

                    if (op == OpCode.TABLESWITCH) {
                        final int low = b.readInt();
                        final int high = b.readInt();
                        final Instruction[] targets = new Instruction[high - low + 1];

                        switchInfo.setLowValue(low);
                        switchInfo.setHighValue(high);

                        for (int i = 0; i < targets.length; i++) {
                            final int targetIndex = i;
                            final int targetOffset = offset + b.readInt();

                            if (targetOffset < offset) {
                                targets[targetIndex] = body.atOffset(targetOffset);
                            }
                            else if (targetOffset == offset) {
                                targets[targetIndex] = instruction;
                            }
                            else {
                                targets[targetIndex] = new Instruction(targetOffset, OpCode.NOP);

                                final Fixup oldFixup = fixups[targetOffset];
                                final Fixup newFixup = new Fixup() {
                                    @Override
                                    public void fix(final Instruction target) {
                                        targets[targetIndex] = target;
                                    }
                                };

                                fixups[targetOffset] = oldFixup != null ? Fixup.combine(oldFixup, newFixup)
                                                                        : newFixup;
                            }
                        }

                        switchInfo.setTargets(targets);
                    }
                    else {
                        final int pairCount = b.readInt();
                        final int[] keys = new int[pairCount];
                        final Instruction[] targets = new Instruction[pairCount];

                        for (int i = 0; i < pairCount; i++) {
                            final int targetIndex = i;

                            keys[targetIndex] = b.readInt();

                            final int targetOffset = offset + b.readInt();

                            if (targetOffset < offset) {
                                targets[targetIndex] = body.atOffset(targetOffset);
                            }
                            else if (targetOffset == offset) {
                                targets[targetIndex] = instruction;
                            }
                            else {
                                targets[targetIndex] = new Instruction(targetOffset, OpCode.NOP);

                                final Fixup oldFixup = fixups[targetOffset];
                                final Fixup newFixup = new Fixup() {
                                    @Override
                                    public void fix(final Instruction target) {
                                        targets[targetIndex] = target;
                                    }
                                };

                                fixups[targetOffset] = oldFixup != null ? Fixup.combine(oldFixup, newFixup)
                                                                        : newFixup;
                            }
                        }

                        switchInfo.setKeys(keys);
                        switchInfo.setTargets(targets);
                    }

                    break;
                }

                case Local: {
                    final int variableSlot;

                    if (op.isWide()) {
                        variableSlot = b.readUnsignedShort();
                    }
                    else {
                        variableSlot = b.readUnsignedByte();
                    }

                    final VariableReference variable = variables.reference(variableSlot, op, offset);

                    if (variableSlot < 0) {
                        instruction = new Instruction(op, new ErrorOperand("!!! BAD LOCAL: " + variableSlot + " !!!"));
                    }
                    else {
                        instruction = Instruction.create(op, variable);
                    }

                    break;
                }

                case LocalI1: {
                    final int variableSlot;
                    final int operand;

                    if (op.isWide()) {
                        variableSlot = b.readUnsignedShort();
                    }
                    else {
                        variableSlot = b.readUnsignedByte();
                    }

                    final VariableReference variable = variables.reference(variableSlot, op, offset);

                    operand = b.readByte();

                    if (variableSlot < 0) {
                        instruction = new Instruction(
                            op,
                            new ErrorOperand("!!! BAD LOCAL: " + variableSlot + " !!!"),
                            operand
                        );
                    }
                    else {
                        instruction = Instruction.create(op, variable, operand);
                    }

                    break;
                }

                case LocalI2: {
                    final int variableSlot;
                    final int operand;

                    if (op.isWide()) {
                        variableSlot = b.readUnsignedShort();
                    }
                    else {
                        variableSlot = b.readUnsignedByte();
                    }

                    final VariableReference variable = variables.reference(variableSlot, op, offset);

                    operand = b.readShort();

                    if (variableSlot < 0) {
                        instruction = new Instruction(
                            op,
                            new ErrorOperand("!!! BAD LOCAL: " + variableSlot + " !!!"),
                            operand
                        );
                    }
                    else {
                        instruction = Instruction.create(op, variable, operand);
                    }

                    break;
                }

                default: {
                    throw new IllegalStateException("Unrecognized opcode: " + code);
                }
            }

            instruction.setOffset(offset);
            body.add(instruction);

            final Fixup fixup = fixups[offset];

            if (fixup != null) {
                if (!instruction.hasLabel()) {
                    instruction.setLabel(new Label(offset));
                }
                fixup.fix(instruction);
            }
        }

        int labelCount = 0;

        for (int i = 0; i < body.size(); i++) {
            final Instruction instruction = body.get(i);
            final OpCode code = instruction.getOpCode();
            final Object operand = instruction.hasOperand() ? instruction.getOperand(0) : null;

            if (operand instanceof VariableDefinition) {
                final VariableDefinition currentVariable = (VariableDefinition) operand;
                final int effectiveOffset;

                if (code.isStore()) {
                    effectiveOffset = instruction.getOffset() + code.getSize() + code.getOperandType().getBaseSize();
                }
                else {
                    effectiveOffset = instruction.getOffset();
                }

                VariableDefinition actualVariable = variables.tryFind(currentVariable.getSlot(), effectiveOffset);

                if (actualVariable == null && code.isStore()) {
                    actualVariable = variables.find(
                        currentVariable.getSlot(),
                        effectiveOffset + code.getSize() + code.getOperandType().getBaseSize()
                    );
                }

                if (actualVariable != currentVariable) {
                    if (instruction.getOperandCount() > 1) {
                        final Object[] operands = new Object[instruction.getOperandCount()];

                        operands[0] = actualVariable;

                        for (int j = 1; j < operands.length; j++) {
                            operands[j] = instruction.getOperand(j);
                        }

                        instruction.setOperand(operands);
                    }
                    else {
                        instruction.setOperand(actualVariable);
                    }
                }
            }

            if (instruction.hasLabel()) {
                instruction.getLabel().setIndex(labelCount++);
            }
        }

        final List<ExceptionTableEntry> exceptionTable = _code.getExceptionTableEntries();

        if (!exceptionTable.isEmpty()) {
//            populateExceptionHandlerInfo(body, exceptionTable);
            _methodBody.getExceptionHandlers().addAll(ExceptionHandlerMapper.run(body, exceptionTable));
/*
            if (!_methodBody.getExceptionHandlers().isEmpty()) {
                final ControlFlowGraph cfg = ControlFlowGraphBuilder.build(_methodBody);
                cfg.computeDominance();
                cfg.computeDominanceFrontier();
                cfg.export(new File("/tmp/test"));
            }
*/
        }

        return _methodBody;
    }

    private void processLocalVariableTable(
        final VariableDefinitionCollection variables,
        final LocalVariableTableAttribute table,
        final List<ParameterDefinition> parameters) {

        for (final LocalVariableTableEntry entry : table.getEntries()) {
            final int slot = entry.getIndex();
            final int scopeStart = entry.getScopeOffset();
            final int scopeEnd = scopeStart + entry.getScopeLength();

            VariableDefinition variable = variables.tryFind(slot, scopeStart);

            if (variable == null) {
                variable = new VariableDefinition(
                    slot,
                    entry.getName(),
                    _methodDefinition,
                    entry.getType()
                );

                variables.add(variable);
            }
            else if (!StringUtilities.isNullOrEmpty(entry.getName())) {
                variable.setName(entry.getName());
            }

            if (!entry.isBadType()) {
                variable.setVariableType(entry.getType());
                variable.setTypeKnown(true);
            }

            variable.setFromMetadata(true);
            variable.setScopeStart(scopeStart);
            variable.setScopeEnd(scopeEnd);

            if (entry.getScopeOffset() == 0) {
                ParameterDefinition parameter = null;

                for (int j = 0; j < parameters.size(); j++) {
                    if (parameters.get(j).getSlot() == entry.getIndex()) {
                        parameter = parameters.get(j);
                        break;
                    }
                }

                if (parameter != null && !parameter.hasName()) {
                    parameter.setName(entry.getName());
                }
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Fixup Class">

    private abstract static class Fixup {
        public abstract void fix(final Instruction target);

        public static Fixup combine(final Fixup first, final Fixup second) {
            final Fixup[] fixups;

            if (first instanceof MultiFixup) {
                final MultiFixup m1 = (MultiFixup) first;

                if (second instanceof MultiFixup) {
                    final MultiFixup m2 = (MultiFixup) second;

                    fixups = new Fixup[m1._fixups.length + m2._fixups.length];

                    System.arraycopy(
                        m2._fixups,
                        0,
                        fixups,
                        m1._fixups.length,
                        m2._fixups.length
                    );
                }
                else {
                    fixups = new Fixup[m1._fixups.length + 1];
                    fixups[m1._fixups.length] = second;
                }

                System.arraycopy(
                    m1._fixups,
                    0,
                    fixups,
                    0,
                    m1._fixups.length
                );
            }
            else {
                if (second instanceof MultiFixup) {
                    final MultiFixup m2 = (MultiFixup) second;

                    fixups = new Fixup[1 + m2._fixups.length];

                    System.arraycopy(
                        m2._fixups,
                        0,
                        fixups,
                        1,
                        m2._fixups.length
                    );
                }
                else {
                    fixups = new Fixup[] { first, second };
                }
            }

            return new MultiFixup(fixups);
        }

        private final static class MultiFixup extends Fixup {
            private final Fixup[] _fixups;

            private MultiFixup(final Fixup... fixups) {
                _fixups = VerifyArgument.noNullElements(fixups, "fixups");
            }

            @Override
            public void fix(final Instruction target) {
                for (final Fixup fixup : _fixups) {
                    fixup.fix(target);
                }
            }
        }
    }

    // </editor-fold>
}
