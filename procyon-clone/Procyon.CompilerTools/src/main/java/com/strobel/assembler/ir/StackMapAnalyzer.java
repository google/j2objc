/*
 * StackMapAnalyzer.java
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

import com.strobel.assembler.metadata.*;
import com.strobel.core.ArrayUtilities;
import com.strobel.core.VerifyArgument;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class StackMapAnalyzer {

    @SuppressWarnings("ConstantConditions")
    public static List<StackMapFrame> computeStackMapTable(final MethodBody body) {
        VerifyArgument.notNull(body, "body");

        final InstructionCollection instructions = body.getInstructions();
        final List<ExceptionHandler> exceptionHandlers = body.getExceptionHandlers();

        if (instructions.isEmpty()) {
            return Collections.emptyList();
        }

        final StackMappingVisitor stackMappingVisitor = new StackMappingVisitor();
        final InstructionVisitor executor = stackMappingVisitor.visitBody(body);

        final Set<Instruction> agenda = new LinkedHashSet<>();
        final Map<Instruction, Frame> frames = new IdentityHashMap<>();
        final Set<Instruction> branchTargets = new LinkedHashSet<>();

        final IMetadataResolver resolver = body.getResolver();
        final TypeReference throwableType = resolver.lookupType("java/lang/Throwable");

        for (final ExceptionHandler handler : exceptionHandlers) {
            final Instruction handlerStart = handler.getHandlerBlock().getFirstInstruction();

            branchTargets.add(handlerStart);

            frames.put(
                handlerStart,
                new Frame(
                    FrameType.New,
                    FrameValue.EMPTY_VALUES,
                    new FrameValue[] {
                        FrameValue.makeReference(
                            handler.isCatch() ? handler.getCatchType()
                                              : throwableType
                        )
                    }
                )
            );
        }

        final ParameterDefinition thisParameter = body.getThisParameter();
        final boolean hasThis = thisParameter != null;

        if (hasThis) {
            stackMappingVisitor.set(0, thisParameter.getParameterType());
        }

        for (final ParameterDefinition parameter : body.getMethod().getParameters()) {
            stackMappingVisitor.set(parameter.getSlot(), parameter.getParameterType());
        }

        final Instruction firstInstruction = instructions.get(0);
        final Frame initialFrame = stackMappingVisitor.buildFrame();

        agenda.add(firstInstruction);
        frames.put(firstInstruction, initialFrame);

        while (!agenda.isEmpty()) {
            final Instruction instruction = agenda.iterator().next();
            final Frame inputFrame = frames.get(instruction);

            assert inputFrame != null;

            agenda.remove(instruction);
            stackMappingVisitor.visitFrame(inputFrame);
            executor.visit(instruction);

            final Frame outputFrame = stackMappingVisitor.buildFrame();
            final OpCode opCode = instruction.getOpCode();
            final OperandType operandType = opCode.getOperandType();

            if (!opCode.isUnconditionalBranch()) {
                final Instruction nextInstruction = instruction.getNext();

                if (nextInstruction != null) {
                    pruneLocals(stackMappingVisitor, nextInstruction, body.getVariables());

                    final boolean changed = updateFrame(
                        nextInstruction,
                        inputFrame,
                        stackMappingVisitor.buildFrame(),
                        stackMappingVisitor.getInitializations(),
                        frames
                    );

                    if (changed) {
                        agenda.add(nextInstruction);
                    }

                    stackMappingVisitor.visitFrame(outputFrame);
                }
            }

            if (operandType == OperandType.BranchTarget ||
                operandType == OperandType.BranchTargetWide) {

                final Instruction branchTarget = instruction.getOperand(0);

                assert branchTarget != null;

                pruneLocals(stackMappingVisitor, branchTarget, body.getVariables());

                final boolean changed = updateFrame(
                    branchTarget,
                    inputFrame,
                    stackMappingVisitor.buildFrame(),
                    stackMappingVisitor.getInitializations(),
                    frames
                );

                if (changed) {
                    agenda.add(branchTarget);
                }

                branchTargets.add(branchTarget);
                stackMappingVisitor.visitFrame(outputFrame);
            }
            else if (operandType == OperandType.Switch) {
                final SwitchInfo switchInfo = instruction.getOperand(0);
                final Instruction defaultTarget = switchInfo.getDefaultTarget();

                assert defaultTarget != null;

                pruneLocals(stackMappingVisitor, defaultTarget, body.getVariables());

                boolean changed = updateFrame(
                    defaultTarget,
                    inputFrame,
                    stackMappingVisitor.buildFrame(),
                    stackMappingVisitor.getInitializations(),
                    frames
                );

                if (changed) {
                    agenda.add(defaultTarget);
                }

                branchTargets.add(defaultTarget);
                stackMappingVisitor.visitFrame(outputFrame);

                for (final Instruction branchTarget : switchInfo.getTargets()) {
                    assert branchTarget != null;

                    pruneLocals(stackMappingVisitor, branchTarget, body.getVariables());

                    changed = updateFrame(
                        branchTarget,
                        inputFrame,
                        stackMappingVisitor.buildFrame(),
                        stackMappingVisitor.getInitializations(),
                        frames
                    );

                    if (changed) {
                        agenda.add(branchTarget);
                    }

                    branchTargets.add(branchTarget);
                    stackMappingVisitor.visitFrame(outputFrame);
                }
            }

            if (opCode.canThrow()) {
                final ExceptionHandler handler = findInnermostExceptionHandler(
                    exceptionHandlers,
                    instruction.getOffset()
                );

                if (handler != null) {
                    final Instruction handlerStart = handler.getHandlerBlock().getFirstInstruction();

                    while (stackMappingVisitor.getStackSize() > 0) {
                        stackMappingVisitor.pop();
                    }

                    if (handler.isCatch()) {
                        stackMappingVisitor.push(handler.getCatchType());
                    }
                    else {
                        stackMappingVisitor.push(throwableType);
                    }

                    pruneLocals(stackMappingVisitor, handlerStart, body.getVariables());

                    final boolean changed = updateFrame(
                        handlerStart,
                        inputFrame,
                        stackMappingVisitor.buildFrame(),
                        stackMappingVisitor.getInitializations(),
                        frames
                    );

                    if (changed) {
                        agenda.add(handlerStart);
                    }
                }
            }
        }

        final StackMapFrame[] framesInStackMap = new StackMapFrame[branchTargets.size()];

        int i = 0;

        for (final Instruction branchTarget : branchTargets) {
            framesInStackMap[i++] = new StackMapFrame(
                frames.get(branchTarget),
                branchTarget
            );
        }

        Arrays.sort(
            framesInStackMap,
            new Comparator<StackMapFrame>() {
                @Override
                public int compare(final StackMapFrame o1, final StackMapFrame o2) {
                    return Integer.compare(o1.getStartInstruction().getOffset(), o2.getStartInstruction().getOffset());
                }
            }
        );

        Frame lastFrame = initialFrame;

        for (i = 0; i < framesInStackMap.length; i++) {
            final StackMapFrame frame = framesInStackMap[i];

            final Frame deltaFrame = Frame.computeDelta(
                lastFrame,
                frame.getFrame()
            );

            framesInStackMap[i] = new StackMapFrame(deltaFrame, frame.getStartInstruction());
            lastFrame = frame.getFrame();
        }

        return ArrayUtilities.asUnmodifiableList(framesInStackMap);
    }

    private static boolean pruneLocals(
        final StackMappingVisitor stackMappingVisitor,
        final Instruction target,
        final VariableDefinitionCollection variables) {

        boolean changed = false;

        for (int i = 0, n = stackMappingVisitor.getLocalCount(); i < n; i++) {
            final VariableDefinition v = variables.tryFind(i, target.getOffset());

            if (v == null) {
                stackMappingVisitor.set(i, FrameValue.OUT_OF_SCOPE);
                changed = true;
            }
        }

        if (changed) {
            stackMappingVisitor.pruneLocals();
            return true;
        }

        return false;
    }

    private static boolean updateFrame(
        final Instruction instruction,
        final Frame inputFrame,
        final Frame outputFrame,
        final Map<Instruction, TypeReference> initializations,
        final Map<Instruction, Frame> frames) {

        final Frame oldFrame = frames.get(instruction);

        if (oldFrame != null) {
            assert oldFrame.getStackValues().size() == outputFrame.getStackValues().size();

            final Frame mergedFrame = Frame.merge(inputFrame, outputFrame, oldFrame, initializations);
            frames.put(instruction, mergedFrame);
            return mergedFrame != oldFrame;
        }
        else {
            frames.put(instruction, outputFrame);
            return true;
        }
    }

    private static ExceptionHandler findInnermostExceptionHandler(
        final List<ExceptionHandler> exceptionHandlers,
        final int offsetInTryBlock) {

        for (final ExceptionHandler handler : exceptionHandlers) {
            final InstructionBlock tryBlock = handler.getTryBlock();
            final InstructionBlock handlerBlock = handler.getHandlerBlock();

            if (tryBlock.getFirstInstruction().getOffset() <= offsetInTryBlock &&
                offsetInTryBlock < handlerBlock.getFirstInstruction().getOffset()) {

                return handler;
            }
        }

        return null;
    }
}
