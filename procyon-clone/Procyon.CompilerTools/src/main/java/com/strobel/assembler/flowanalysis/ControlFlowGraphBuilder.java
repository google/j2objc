/*
 * ControlFlowGraphBuilder.java
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

package com.strobel.assembler.flowanalysis;

import com.strobel.assembler.Collection;
import com.strobel.assembler.ir.ExceptionHandler;
import com.strobel.assembler.ir.ExceptionHandlerType;
import com.strobel.assembler.ir.Instruction;
import com.strobel.assembler.ir.InstructionBlock;
import com.strobel.assembler.ir.OpCode;
import com.strobel.assembler.ir.OperandType;
import com.strobel.assembler.metadata.MethodBody;
import com.strobel.assembler.metadata.SwitchInfo;
import com.strobel.core.Comparer;
import com.strobel.core.Predicate;
import com.strobel.core.VerifyArgument;
import com.strobel.util.ContractUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static com.strobel.core.CollectionUtilities.*;

@SuppressWarnings("ConstantConditions")
public final class ControlFlowGraphBuilder {
    public static ControlFlowGraph build(final MethodBody methodBody) {
        VerifyArgument.notNull(methodBody, "methodBody");

        final ControlFlowGraphBuilder builder = new ControlFlowGraphBuilder(
            methodBody.getInstructions(),
            methodBody.getExceptionHandlers()
        );

        return builder.build();
    }

    public static ControlFlowGraph build(final List<Instruction> instructions, final List<ExceptionHandler> exceptionHandlers) {
        final ControlFlowGraphBuilder builder = new ControlFlowGraphBuilder(
            VerifyArgument.notNull(instructions, "instructions"),
            VerifyArgument.notNull(exceptionHandlers, "exceptionHandlers")
        );

        return builder.build();
    }

    private final List<Instruction> _instructions;
    private final List<ExceptionHandler> _exceptionHandlers;
    private final List<ControlFlowNode> _nodes = new Collection<>();
    private final int[] _offsets;
    private final boolean[] _hasIncomingJumps;
    private final ControlFlowNode _entryPoint;
    private final ControlFlowNode _regularExit;
    private final ControlFlowNode _exceptionalExit;

    private int _nextBlockId;
    boolean copyFinallyBlocks = false;

    private ControlFlowGraphBuilder(final List<Instruction> instructions, final List<ExceptionHandler> exceptionHandlers) {
        _instructions = VerifyArgument.notNull(instructions, "instructions");
        _exceptionHandlers = coalesceExceptionHandlers(VerifyArgument.notNull(exceptionHandlers, "exceptionHandlers"));

        _offsets = new int[instructions.size()];
        _hasIncomingJumps = new boolean[_offsets.length];

        for (int i = 0; i < instructions.size(); i++) {
            _offsets[i] = instructions.get(i).getOffset();
        }

        _entryPoint = new ControlFlowNode(_nextBlockId++, 0, ControlFlowNodeType.EntryPoint);
        _regularExit = new ControlFlowNode(_nextBlockId++, -1, ControlFlowNodeType.RegularExit);
        _exceptionalExit = new ControlFlowNode(_nextBlockId++, -1, ControlFlowNodeType.ExceptionalExit);

        _nodes.add(_entryPoint);
        _nodes.add(_regularExit);
        _nodes.add(_exceptionalExit);
    }

    public final ControlFlowGraph build() {
        calculateIncomingJumps();
        createNodes();
        createRegularControlFlow();
        createExceptionalControlFlow();

        if (copyFinallyBlocks) {
            copyFinallyBlocksIntoLeaveEdges();
        }
        else {
            transformLeaveEdges();
        }

        return new ControlFlowGraph(_nodes.toArray(new ControlFlowNode[_nodes.size()]));
    }

    private void calculateIncomingJumps() {
        //
        // Step 1: Determine which instructions are jump targets.
        //

        for (final Instruction instruction : _instructions) {
            final OpCode opCode = instruction.getOpCode();

            if (opCode.getOperandType() == OperandType.BranchTarget ||
                opCode.getOperandType() == OperandType.BranchTargetWide) {

                _hasIncomingJumps[getInstructionIndex(instruction.<Instruction>getOperand(0))] = true;
            }
            else if (opCode.getOperandType() == OperandType.Switch) {
                final SwitchInfo switchInfo = instruction.getOperand(0);

                _hasIncomingJumps[getInstructionIndex(switchInfo.getDefaultTarget())] = true;

                for (final Instruction target : switchInfo.getTargets()) {
                    _hasIncomingJumps[getInstructionIndex(target)] = true;
                }
            }
        }

        for (final ExceptionHandler handler : _exceptionHandlers) {
            _hasIncomingJumps[getInstructionIndex(handler.getHandlerBlock().getFirstInstruction())] = true;
        }
    }

    private void createNodes() {
        //
        // Step 2a: Find basic blocks and create nodes for them.
        //

        final List<Instruction> instructions = _instructions;

        for (int i = 0, n = instructions.size(); i < n; i++) {
            final Instruction blockStart = instructions.get(i);
            final ExceptionHandler blockStartExceptionHandler = findInnermostExceptionHandler(blockStart.getOffset());

            //
            // See how big we can make that block...
            //
            for (; i + 1 < n; i++) {
                final Instruction instruction = instructions.get(i);
                final OpCode opCode = instruction.getOpCode();

                if (opCode.isBranch() /*|| opCode.canThrow()*/ || _hasIncomingJumps[i + 1]) {
                    break;
                }

                final Instruction next = instruction.getNext();

                if (next != null) {
                    //
                    // Ensure that blocks never contain instructions from different try blocks.
                    //
                    final ExceptionHandler innermostExceptionHandler = findInnermostExceptionHandler(next.getOffset());

                    if (innermostExceptionHandler != blockStartExceptionHandler) {
                        break;
                    }
                }
            }

            _nodes.add(new ControlFlowNode(_nodes.size(), blockStart, instructions.get(i)));
        }

        //
        // Step 2b: Create special nodes for exception handling constructs.
        //

        for (final ExceptionHandler handler : _exceptionHandlers) {
            final int index = _nodes.size();
            final ControlFlowNode endFinallyNode;

            if (handler.getHandlerType() == ExceptionHandlerType.Finally) {
                endFinallyNode = new ControlFlowNode(
                    index,
                    handler.getHandlerBlock().getLastInstruction().getEndOffset(),
                    ControlFlowNodeType.EndFinally
                );
            }
            else {
                endFinallyNode = null;
            }

            _nodes.add(new ControlFlowNode(index, handler, endFinallyNode));
        }
    }

    private void createRegularControlFlow() {
        //
        // Step 3: Create edges for the normal control flow (assuming no exceptions thrown).
        //

        final List<Instruction> instructions = _instructions;

        createEdge(_entryPoint, instructions.get(0), JumpType.Normal);

        for (final ControlFlowNode node : _nodes) {
            final Instruction end = node.getEnd();

            if (end == null || end.getOffset() >= _instructions.get(_instructions.size() - 1).getEndOffset()) {
                continue;
            }

            final OpCode endOpCode = end.getOpCode();

            //
            // Create normal edges from one instruction to the next.
            //
            if (!endOpCode.isUnconditionalBranch() || endOpCode.isJumpToSubroutine()) {
                final Instruction next = end.getNext();

                if (next != null) {
                    final boolean isHandlerStart = any(
                        _exceptionHandlers,
                        new Predicate<ExceptionHandler>() {
                            @Override
                            public boolean test(final ExceptionHandler handler) {
                                return handler.getHandlerBlock().getFirstInstruction() == next;
                            }
                        }
                    );

                    if (!isHandlerStart) {
                        createEdge(node, next, JumpType.Normal);
                    }
                }
            }

            //
            // Create edges for branch instructions.
            //
            for (Instruction instruction = node.getStart();
                 instruction != null && instruction.getOffset() <= end.getOffset();
                 instruction = instruction.getNext()) {

                final OpCode opCode = instruction.getOpCode();

                if (opCode.getOperandType() == OperandType.BranchTarget ||
                    opCode.getOperandType() == OperandType.BranchTargetWide) {

                    createBranchControlFlow(node, instruction, instruction.<Instruction>getOperand(0));
                }
                else if (opCode.getOperandType() == OperandType.Switch) {
                    final SwitchInfo switchInfo = instruction.getOperand(0);

                    createEdge(node, switchInfo.getDefaultTarget(), JumpType.Normal);

                    for (final Instruction target : switchInfo.getTargets()) {
                        createEdge(node, target, JumpType.Normal);
                    }
                }
            }

            //
            // Create edges for return and leave instructions.
            //
            if (endOpCode == OpCode.ENDFINALLY) {
                final ControlFlowNode handlerBlock = findInnermostFinallyBlock(end.getOffset());

                if (handlerBlock.getEndFinallyNode() != null) {
                    createEdge(node, handlerBlock.getEndFinallyNode(), JumpType.Normal);
                }
            }
            else if (endOpCode == OpCode.LEAVE) {
                ControlFlowNode handlerBlock = findInnermostHandlerBlock(end.getOffset());

                if (handlerBlock != _exceptionalExit) {
                    if (handlerBlock.getEndFinallyNode() == null) {
                        handlerBlock = findInnermostFinallyHandlerNode(
                            handlerBlock.getExceptionHandler().getTryBlock().getLastInstruction().getOffset()
                        );
                    }

                    if (handlerBlock.getEndFinallyNode() != null) {
                        createEdge(node, handlerBlock.getEndFinallyNode(), JumpType.LeaveTry);
                    }
                }
            }
            else if (endOpCode.isReturn()) {
                createReturnControlFlow(node, end);
            }
        }
    }

    private void createExceptionalControlFlow() {
        //
        // Step 4: Create edges for the exceptional control flow.
        //

        for (final ControlFlowNode node : _nodes) {
            final Instruction end = node.getEnd();

            if (end != null &&
                end.getOffset() < _instructions.get(_instructions.size() - 1).getEndOffset()) {

                final ControlFlowNode innermostHandler = findInnermostExceptionHandlerNode(node.getEnd().getOffset());

                if (innermostHandler == _exceptionalExit) {
                    final ControlFlowNode handlerBlock = findInnermostHandlerBlock(node.getEnd().getOffset());

                    ControlFlowNode finallyBlock;

                    if (handlerBlock.getExceptionHandler() != null) {
                        finallyBlock = findInnermostFinallyHandlerNode(
                            handlerBlock.getExceptionHandler().getTryBlock().getLastInstruction().getOffset()
                        );

                        if (finallyBlock.getNodeType() == ControlFlowNodeType.FinallyHandler &&
                            finallyBlock.getExceptionHandler().getHandlerBlock().contains(end)) {

                            finallyBlock = _exceptionalExit;
                        }
                    }
                    else {
                        finallyBlock = _exceptionalExit;
                    }

                    createEdge(node, finallyBlock, JumpType.JumpToExceptionHandler);
                }
                else {
                    for (final ExceptionHandler handler : _exceptionHandlers) {
                        if (Comparer.equals(handler.getTryBlock(), innermostHandler.getExceptionHandler().getTryBlock())) {
                            final ControlFlowNode handlerNode = firstOrDefault(
                                _nodes,
                                new Predicate<ControlFlowNode>() {
                                    @Override
                                    public boolean test(final ControlFlowNode node) {
                                        return node.getExceptionHandler() == handler;
                                    }
                                }
                            );

                            createEdge(node, handlerNode, JumpType.JumpToExceptionHandler);
                        }
                    }

                    //
                    // If we're in a catch block, and we have an adjacent finally block, jump to it.
                    //

                    final ControlFlowNode handlerBlock = findInnermostHandlerBlock(node.getEnd().getOffset());

                    if (handlerBlock != innermostHandler &&
                        handlerBlock.getNodeType() == ControlFlowNodeType.CatchHandler) {

                        final ControlFlowNode finallyBlock = findInnermostFinallyHandlerNode(
                            handlerBlock.getExceptionHandler().getTryBlock().getLastInstruction().getOffset()
                        );

                        if (finallyBlock.getNodeType() == ControlFlowNodeType.FinallyHandler) {
                            createEdge(node, finallyBlock, JumpType.JumpToExceptionHandler);
                        }
                    }
                }
            }

            final ExceptionHandler exceptionHandler = node.getExceptionHandler();

            if (exceptionHandler != null) {
                if (exceptionHandler.isFinally()) {
                    final ControlFlowNode handlerBlock = findInnermostFinallyHandlerNode(
                        exceptionHandler.getHandlerBlock().getLastInstruction().getOffset()
                    );

                    if (handlerBlock.getNodeType() == ControlFlowNodeType.FinallyHandler && handlerBlock != node) {
                        createEdge(
                            node,
                            handlerBlock,
                            JumpType.JumpToExceptionHandler
                        );
                    }
                }
                else {
                    final ControlFlowNode adjacentFinally = findInnermostFinallyHandlerNode(
                        exceptionHandler.getTryBlock().getLastInstruction().getOffset()
                    );

                    createEdge(
                        node,
                        adjacentFinally != null ? adjacentFinally : findParentExceptionHandlerNode(node),
                        JumpType.JumpToExceptionHandler
                    );
                }

                createEdge(
                    node,
                    exceptionHandler.getHandlerBlock().getFirstInstruction(),
                    JumpType.Normal
                );
            }
        }
    }

    private void createBranchControlFlow(final ControlFlowNode node, final Instruction jump, final Instruction target) {
        final ControlFlowNode handlerNode = findInnermostHandlerBlock(jump.getOffset());
        final ControlFlowNode outerFinally = findInnermostHandlerBlock(jump.getOffset(), true);
        final ControlFlowNode targetHandlerNode = findInnermostHandlerBlock(target.getOffset());
        final ExceptionHandler handler = handlerNode.getExceptionHandler();

        if (jump.getOpCode().isJumpToSubroutine() ||
            targetHandlerNode == handlerNode ||
            (handler != null &&
             (handler.getTryBlock().contains(jump) ? handler.getTryBlock().contains(target)
                                                   : handler.getHandlerBlock().contains(target)))) {
            //
            // A jump within a handler is normal control flow.
            //
            createEdge(node, target, JumpType.Normal);
            return;
        }

        if (handlerNode.getNodeType() == ControlFlowNodeType.CatchHandler) {
            //
            // First look for an immediately adjacent finally handler.
            //
            ControlFlowNode finallyHandlerNode = findInnermostFinallyHandlerNode(handler.getTryBlock().getLastInstruction().getOffset());

            final ExceptionHandler finallyHandler = finallyHandlerNode.getExceptionHandler();
            final ExceptionHandler outerFinallyHandler = outerFinally.getExceptionHandler();

            if (finallyHandlerNode.getNodeType() != ControlFlowNodeType.FinallyHandler ||
                (outerFinally.getNodeType() == ControlFlowNodeType.FinallyHandler &&
                 finallyHandler.getTryBlock().contains(outerFinallyHandler.getHandlerBlock()))) {

                //
                // We don't have an adjacent finally handler, or our containing finally handler is
                // protected by the adjacent finally handler's try block.  Use the containing finally.
                //
                finallyHandlerNode = outerFinally;
            }

            if (finallyHandlerNode.getNodeType() == ControlFlowNodeType.FinallyHandler &&
                finallyHandlerNode != targetHandlerNode) {

                //
                // We are jumping out of a try or catch block by way of a finally block.
                //
                createEdge(node, target, JumpType.LeaveTry);
            }
            else {
                //
                // We are performing a regular jumping out of a try or catch block.
                //
                createEdge(node, target, JumpType.Normal);
            }

            return;
        }

        if (handlerNode.getNodeType() == ControlFlowNodeType.FinallyHandler) {
            if (handler.getTryBlock().contains(jump)) {
                //
                // We are jumping out of a try block by way of a finally block.
                //

                createEdge(node, target, JumpType.LeaveTry);
            }
            else {
                ControlFlowNode parentHandler = findParentExceptionHandlerNode(handlerNode);

                while (parentHandler != handlerNode &&
                       parentHandler.getNodeType() == ControlFlowNodeType.CatchHandler) {

                    parentHandler = findParentExceptionHandlerNode(parentHandler);
                }

                if (parentHandler.getNodeType() == ControlFlowNodeType.FinallyHandler &&
                    !parentHandler.getExceptionHandler().getTryBlock().contains(target)) {

                    createEdge(node, target, JumpType.LeaveTry);
                }
                else {
                    //
                    // We are jumping out of a finally block.
                    //
                    createEdge(node, handlerNode.getEndFinallyNode(), JumpType.Normal);
                    createEdge(handlerNode.getEndFinallyNode(), target, JumpType.Normal);
                }
            }

            return;
        }

        //
        // Last case is regular control flow.
        //
        createEdge(node, target, JumpType.Normal);
    }

    @SuppressWarnings("UnusedParameters")
    private void createReturnControlFlow(final ControlFlowNode node, final Instruction end) {
//        final ControlFlowNode handlerNode = findInnermostHandlerBlock(end.getOffset());
//        final ControlFlowNode finallyNode;
//
//        if (handlerNode.getNodeType() == ControlFlowNodeType.CatchHandler) {
//            finallyNode = findInnermostFinallyHandlerNode(
//                handlerNode.getExceptionHandler().getTryBlock().getLastInstruction().getOffset()
//            );
//        }
//        else if (handlerNode.getNodeType() == ControlFlowNodeType.FinallyHandler) {
//            finallyNode = handlerNode;
//        }
//        else {
//            finallyNode = _exceptionalExit;
//        }
//
//        if (finallyNode.getNodeType() == ControlFlowNodeType.FinallyHandler) {
//            createEdge(node, _regularExit, JumpType.LeaveTry);
//        }
//        else {
        createEdge(node, _regularExit, JumpType.Normal);
//        }
    }

    private void transformLeaveEdges() {
        //
        // Step 5: Replace LeaveTry edges with EndFinally edges.
        //

        for (int n = _nodes.size(), i = n - 1; i >= 0; i--) {
            final ControlFlowNode node = _nodes.get(i);
            final Instruction end = node.getEnd();

            if (end != null && !node.getOutgoing().isEmpty()) {
                for (final ControlFlowEdge edge : node.getOutgoing()) {
                    if (edge.getType() == JumpType.LeaveTry) {
                        assert end.getOpCode().isBranch();

                        final ControlFlowNode handlerBlock = findInnermostHandlerBlock(end.getOffset());
                        ControlFlowNode finallyBlock = findInnermostFinallyHandlerNode(end.getOffset());

                        if (handlerBlock != finallyBlock) {
                            final ExceptionHandler handler = handlerBlock.getExceptionHandler();
                            final ControlFlowNode adjacentFinally = findInnermostFinallyHandlerNode(handler.getTryBlock().getLastInstruction().getOffset());

                            if (finallyBlock.getNodeType() != ControlFlowNodeType.FinallyHandler || finallyBlock != adjacentFinally) {
                                finallyBlock = adjacentFinally;
                            }
                        }

                        final ControlFlowNode target = edge.getTarget();

                        target.getIncoming().remove(edge);
                        node.getOutgoing().remove(edge);

                        if (finallyBlock.getNodeType() == ControlFlowNodeType.ExceptionalExit) {
                            createEdge(node, finallyBlock, JumpType.Normal);
                            continue;
                        }

                        assert finallyBlock.getNodeType() == ControlFlowNodeType.FinallyHandler;

                        Instruction targetAddress = target.getStart();

                        if (targetAddress == null && target.getExceptionHandler() != null) {
                            targetAddress = target.getExceptionHandler().getHandlerBlock().getFirstInstruction();
                        }

                        if (finallyBlock.getExceptionHandler().getHandlerBlock().contains(end)) {
                            createEdge(node, finallyBlock.getEndFinallyNode(), JumpType.Normal);
                        }
                        else {
                            createEdge(node, finallyBlock, JumpType.Normal);
                        }

                        if (targetAddress != null) {
                            while (true) {
                                ControlFlowNode parentHandler = findParentExceptionHandlerNode(finallyBlock);

                                while (parentHandler.getNodeType() == ControlFlowNodeType.CatchHandler &&
                                       !parentHandler.getExceptionHandler().getTryBlock().contains(targetAddress)) {

                                    parentHandler = findInnermostFinallyHandlerNode(
                                        parentHandler.getExceptionHandler().getTryBlock().getLastInstruction().getOffset()
                                    );

                                    if (parentHandler == finallyBlock) {
                                        parentHandler = findParentExceptionHandlerNode(finallyBlock);
                                    }
                                }

                                if (parentHandler.getNodeType() != ControlFlowNodeType.FinallyHandler ||
                                    parentHandler.getExceptionHandler().getTryBlock().contains(targetAddress)) {

                                    break;
                                }

                                createEdge(finallyBlock.getEndFinallyNode(), parentHandler, JumpType.EndFinally);

                                finallyBlock = parentHandler;
                            }
                        }

                        if (finallyBlock != target) {
                            createEdge(finallyBlock.getEndFinallyNode(), target, JumpType.EndFinally);

                            createEdge(
                                findNode(finallyBlock.getExceptionHandler().getHandlerBlock().getLastInstruction()),
                                finallyBlock.getEndFinallyNode(),
                                JumpType.Normal
                            );
                        }
                    }
                }
            }
        }
    }

    private void copyFinallyBlocksIntoLeaveEdges() {
        //
        // Step 5b: Copy finally blocks into the LeaveTry edges.
        //

        for (int n = _nodes.size(), i = n - 1; i >= 0; i--) {
            final ControlFlowNode node = _nodes.get(i);
            final Instruction end = node.getEnd();

            if (end != null &&
                node.getOutgoing().size() == 1 &&
                node.getOutgoing().get(0).getType() == JumpType.LeaveTry) {

                assert end.getOpCode() == OpCode.GOTO ||
                       end.getOpCode() == OpCode.GOTO_W;

                final ControlFlowEdge edge = node.getOutgoing().get(0);
                final ControlFlowNode target = edge.getTarget();

                target.getIncoming().remove(edge);
                node.getOutgoing().clear();

                final ControlFlowNode handler = findInnermostExceptionHandlerNode(end.getEndOffset());

                assert handler.getNodeType() == ControlFlowNodeType.FinallyHandler;

                final ControlFlowNode copy = copyFinallySubGraph(handler, handler.getEndFinallyNode(), target);

                createEdge(node, copy, JumpType.Normal);
            }
        }
    }

    private ControlFlowNode copyFinallySubGraph(final ControlFlowNode start, final ControlFlowNode end, final ControlFlowNode newEnd) {
        return new CopyFinallySubGraphLogic(start, end, newEnd).copyFinallySubGraph();
    }

    private static boolean isNarrower(final ExceptionHandler handler, final ExceptionHandler anchor) {
        if (handler == null || anchor == null) {
            return false;
        }

        final Instruction tryStart = handler.getTryBlock().getFirstInstruction();
        final Instruction anchorTryStart = anchor.getTryBlock().getFirstInstruction();

        if (tryStart.getOffset() > anchorTryStart.getOffset()) {
            return true;
        }

        final Instruction tryEnd = handler.getTryBlock().getLastInstruction();
        final Instruction anchorTryEnd = anchor.getTryBlock().getLastInstruction();

        return tryStart.getOffset() == anchorTryStart.getOffset() &&
               tryEnd.getOffset() < anchorTryEnd.getOffset();
    }

    private static boolean isNarrower(final InstructionBlock block, final InstructionBlock anchor) {
        if (block == null || anchor == null) {
            return false;
        }

        final Instruction start = block.getFirstInstruction();
        final Instruction anchorStart = anchor.getFirstInstruction();
        final Instruction end = block.getLastInstruction();
        final Instruction anchorEnd = anchor.getLastInstruction();

        if (start.getOffset() > anchorStart.getOffset()) {
            return end.getOffset() < anchorEnd.getEndOffset();
        }

        return start.getOffset() == anchorStart.getOffset() &&
               end.getOffset() < anchorEnd.getOffset();
    }

    private ControlFlowNode findParentExceptionHandlerNode(final ControlFlowNode node) {
        assert node.getNodeType() == ControlFlowNodeType.CatchHandler ||
               node.getNodeType() == ControlFlowNodeType.FinallyHandler;

        ControlFlowNode result = null;
        ExceptionHandler resultHandler = null;

        final int offset = node.getExceptionHandler().getHandlerBlock().getFirstInstruction().getOffset();

        for (int i = 0, n = _nodes.size(); i < n; i++) {
            final ControlFlowNode currentNode = _nodes.get(i);
            final ExceptionHandler handler = currentNode.getExceptionHandler();

            if (handler != null &&
                handler.getTryBlock().getFirstInstruction().getOffset() <= offset &&
                offset < handler.getTryBlock().getLastInstruction().getEndOffset() &&
                (resultHandler == null || isNarrower(handler, resultHandler))) {

                result = currentNode;
                resultHandler = handler;
            }
        }

        return result != null ? result : _exceptionalExit;
    }

    private ControlFlowNode findInnermostExceptionHandlerNode(final int offset) {
        final ExceptionHandler handler = findInnermostExceptionHandler(offset);

        if (handler == null) {
            return _exceptionalExit;
        }

        for (final ControlFlowNode node : _nodes) {
            if (node.getExceptionHandler() == handler && node.getCopyFrom() == null) {
                return node;
            }
        }

        throw new IllegalStateException("Could not find node for exception handler!");
    }

    private ControlFlowNode findInnermostFinallyHandlerNode(final int offset) {
        final ExceptionHandler handler = findInnermostFinallyHandler(offset);

        if (handler == null) {
            return _exceptionalExit;
        }

        for (final ControlFlowNode node : _nodes) {
            if (node.getExceptionHandler() == handler && node.getCopyFrom() == null) {
                return node;
            }
        }

        throw new IllegalStateException("Could not find node for exception handler!");
    }

    private int getInstructionIndex(final Instruction instruction) {
        final int index = Arrays.binarySearch(_offsets, instruction.getOffset());
        assert index >= 0;
        return index;
    }

    private ControlFlowNode findNode(final Instruction instruction) {
        final int offset = instruction.getOffset();

        for (final ControlFlowNode node : _nodes) {
            if (node.getNodeType() != ControlFlowNodeType.Normal) {
                continue;
            }

            if (offset >= node.getStart().getOffset() &&
                offset < node.getEnd().getEndOffset()) {

                return node;
            }
        }

        return null;
    }

    private ExceptionHandler findInnermostExceptionHandler(final int offsetInTryBlock) {
        ExceptionHandler result = null;

        for (final ExceptionHandler handler : _exceptionHandlers) {
            final InstructionBlock tryBlock = handler.getTryBlock();

            if (tryBlock.getFirstInstruction().getOffset() <= offsetInTryBlock &&
                offsetInTryBlock < tryBlock.getLastInstruction().getEndOffset() &&
                (result == null || isNarrower(handler, result))) {

                result = handler;
            }
        }

        return result;
    }

    private ExceptionHandler findInnermostFinallyHandler(final int offsetInTryBlock) {
        ExceptionHandler result = null;

        for (final ExceptionHandler handler : _exceptionHandlers) {
            if (!handler.isFinally()) {
                continue;
            }

            final InstructionBlock tryBlock = handler.getTryBlock();

            if (tryBlock.getFirstInstruction().getOffset() <= offsetInTryBlock &&
                offsetInTryBlock < tryBlock.getLastInstruction().getEndOffset() &&
                (result == null || isNarrower(handler, result))) {

                result = handler;
            }
        }

        return result;
    }

    private ControlFlowNode findInnermostHandlerBlock(final int instructionOffset) {
        return findInnermostHandlerBlock(instructionOffset, false);
    }

    private ControlFlowNode findInnermostFinallyBlock(final int instructionOffset) {
        return findInnermostHandlerBlock(instructionOffset, true);
    }

    private ControlFlowNode findInnermostHandlerBlock(final int instructionOffset, final boolean finallyOnly) {
        ExceptionHandler result = null;
        InstructionBlock resultBlock = null;

        for (final ExceptionHandler handler : _exceptionHandlers) {
            if (finallyOnly && handler.isCatch()) {
                continue;
            }

            final InstructionBlock handlerBlock = handler.getHandlerBlock();

            if (handlerBlock.getFirstInstruction().getOffset() <= instructionOffset &&
                instructionOffset < handlerBlock.getLastInstruction().getEndOffset() &&
                (resultBlock == null || isNarrower(handler.getHandlerBlock(), resultBlock))) {

                result = handler;
                resultBlock = handlerBlock;
            }
        }

        final ControlFlowNode innerMost = finallyOnly ? findInnermostExceptionHandlerNode(instructionOffset)
                                                      : findInnermostFinallyHandlerNode(instructionOffset);

        final ExceptionHandler innerHandler = innerMost.getExceptionHandler();
        final InstructionBlock innerBlock = innerHandler != null ? innerHandler.getTryBlock() : null;

        if (innerBlock != null && (resultBlock == null || isNarrower(innerBlock, resultBlock))) {
            result = innerHandler;
        }

        if (result == null) {
            return _exceptionalExit;
        }

        for (final ControlFlowNode node : _nodes) {
            if (node.getExceptionHandler() == result && node.getCopyFrom() == null) {
                return node;
            }
        }

        throw new IllegalStateException("Could not find innermost handler block!");
    }

    private ControlFlowEdge createEdge(final ControlFlowNode fromNode, final Instruction toInstruction, final JumpType type) {
        ControlFlowNode target = null;

        for (final ControlFlowNode node : _nodes) {
            if (node.getStart() != null && node.getStart().getOffset() == toInstruction.getOffset()) {
                if (target != null) {
                    throw new IllegalStateException("Multiple edge targets detected!");
                }
                target = node;
            }
        }

        if (target != null) {
            return createEdge(fromNode, target, type);
        }

        throw new IllegalStateException("Could not find target node!");
    }

    private ControlFlowEdge createEdge(final ControlFlowNode fromNode, final ControlFlowNode toNode, final JumpType type) {
        final ControlFlowEdge edge = new ControlFlowEdge(fromNode, toNode, type);

        for (final ControlFlowEdge existingEdge : fromNode.getOutgoing()) {
            if (existingEdge.getSource() == fromNode &&
                existingEdge.getTarget() == toNode &&
                existingEdge.getType() == type) {

                return existingEdge;
            }
        }

        fromNode.getOutgoing().add(edge);
        toNode.getIncoming().add(edge);

        return edge;
    }

    private static List<ExceptionHandler> coalesceExceptionHandlers(final List<ExceptionHandler> handlers) {
        final ArrayList<ExceptionHandler> copy = new ArrayList<>(handlers);

//        for (int i = 0; i < copy.size(); i++) {
//            final ExceptionHandler handler = copy.get(i);
//
//            if (!handler.isCatch()) {
//                if (handler.getTryBlock().equals(handler.getHandlerBlock())) {
//                    copy.remove(i--);
//                }
//                else if (handler.getTryBlock().getFirstInstruction() == handler.getHandlerBlock().getFirstInstruction() &&
//                         handler.getTryBlock().getLastInstruction() == handler.getTryBlock().getFirstInstruction()) {
//                    copy.remove(i--);
//                }
//                continue;
//            }
//
//            final InstructionBlock tryBlock = handler.getTryBlock();
//            final InstructionBlock handlerBlock = handler.getHandlerBlock();
//
//            for (int j = i + 1; j < copy.size(); j++) {
//                final ExceptionHandler other = copy.get(j);
//
//                if (!other.isCatch()) {
//                    continue;
//                }
//
//                final InstructionBlock otherTry = other.getTryBlock();
//                final InstructionBlock otherHandler = other.getHandlerBlock();
//
//                if (otherTry.equals(tryBlock) && otherHandler.equals(handlerBlock)) {
//                    copy.set(
//                        i,
//                        ExceptionHandler.createCatch(
//                            tryBlock,
//                            handlerBlock,
//                            MetadataHelper.findCommonSuperType(handler.getCatchType(), other.getCatchType())
//                        )
//                    );
//
//                    copy.remove(j--);
//                }
//            }
//        }

        return copy;
    }

    private final class CopyFinallySubGraphLogic {
        final Map<ControlFlowNode, ControlFlowNode> oldToNew = new IdentityHashMap<>();
        final ControlFlowNode start;
        final ControlFlowNode end;
        final ControlFlowNode newEnd;

        CopyFinallySubGraphLogic(final ControlFlowNode start, final ControlFlowNode end, final ControlFlowNode newEnd) {
            this.start = start;
            this.end = end;
            this.newEnd = newEnd;
        }

        final ControlFlowNode copyFinallySubGraph() {
            for (final ControlFlowNode node : end.getPredecessors()) {
                collectNodes(node);
            }

            for (final ControlFlowNode old : oldToNew.keySet()) {
                reconstructEdges(old, oldToNew.get(old));
            }

            return getNew(start);
        }

        private void collectNodes(final ControlFlowNode node) {
            if (node == end || node == newEnd) {
                throw new IllegalStateException("Unexpected cycle involving finally constructs!");
            }

            if (oldToNew.containsKey(node)) {
                return;
            }

            final int newBlockIndex = _nodes.size();
            final ControlFlowNode copy;

            switch (node.getNodeType()) {
                case Normal:
                    copy = new ControlFlowNode(newBlockIndex, node.getStart(), node.getEnd());
                    break;

                case FinallyHandler:
                    copy = new ControlFlowNode(newBlockIndex, node.getExceptionHandler(), node.getEndFinallyNode());
                    break;

                default:
                    throw ContractUtils.unsupported();
            }

            copy.setCopyFrom(node);
            _nodes.add(copy);
            oldToNew.put(node, copy);

            if (node != start) {
                for (final ControlFlowNode predecessor : node.getPredecessors()) {
                    collectNodes(predecessor);
                }
            }
        }

        private void reconstructEdges(final ControlFlowNode oldNode, final ControlFlowNode newNode) {
            for (final ControlFlowEdge oldEdge : oldNode.getOutgoing()) {
                createEdge(newNode, getNew(oldEdge.getTarget()), oldEdge.getType());
            }
        }

        private ControlFlowNode getNew(final ControlFlowNode oldNode) {
            if (oldNode == end) {
                return newEnd;
            }

            final ControlFlowNode newNode = oldToNew.get(oldNode);

            return newNode != null ? newNode : oldNode;
        }
    }
}
