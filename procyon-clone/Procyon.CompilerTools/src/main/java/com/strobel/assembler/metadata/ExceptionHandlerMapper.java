/*
 * ExceptionHandlerMapper.java
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

import com.strobel.annotations.NotNull;
import com.strobel.assembler.Collection;
import com.strobel.assembler.flowanalysis.ControlFlowEdge;
import com.strobel.assembler.flowanalysis.ControlFlowGraph;
import com.strobel.assembler.flowanalysis.ControlFlowNode;
import com.strobel.assembler.flowanalysis.ControlFlowNodeType;
import com.strobel.assembler.flowanalysis.JumpType;
import com.strobel.assembler.ir.ExceptionHandler;
import com.strobel.assembler.ir.FlowControl;
import com.strobel.assembler.ir.Instruction;
import com.strobel.assembler.ir.InstructionBlock;
import com.strobel.assembler.ir.InstructionCollection;
import com.strobel.assembler.ir.OpCode;
import com.strobel.assembler.ir.OperandType;
import com.strobel.assembler.ir.attributes.ExceptionTableEntry;
import com.strobel.core.Comparer;
import com.strobel.core.Predicate;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.InstructionHelper;

import java.util.*;

import static com.strobel.core.CollectionUtilities.*;
import static java.lang.String.format;

@SuppressWarnings("ConstantConditions")
public final class ExceptionHandlerMapper {
    public static List<ExceptionHandler> run(final InstructionCollection instructions, final List<ExceptionTableEntry> tableEntries) {
        VerifyArgument.notNull(instructions, "instructions");
        VerifyArgument.notNull(tableEntries, "tableEntries");

        final ExceptionHandlerMapper builder = new ExceptionHandlerMapper(instructions, tableEntries);
        final ControlFlowGraph cfg = builder.build();

        final List<ExceptionHandler> handlers = new ArrayList<>();
        final Map<ExceptionTableEntry, ControlFlowNode> handlerStartNodes = new IdentityHashMap<>();

        for (final ExceptionTableEntry entry : builder._tableEntries) {
            final Instruction handlerStart = instructions.atOffset(entry.getHandlerOffset());
            final ControlFlowNode handlerStartNode = builder.findNode(handlerStart);

            if (handlerStartNode == null) {
                throw new IllegalStateException(
                    format(
                        "Could not find entry node for handler at offset %d.",
                        handlerStart.getOffset()
                    )
                );
            }

            if (handlerStartNode.getIncoming().isEmpty()) {
                builder.createEdge(cfg.getEntryPoint(), handlerStartNode, JumpType.Normal);
            }

            handlerStartNodes.put(entry, handlerStartNode);
        }

        cfg.computeDominance();
        cfg.computeDominanceFrontier();

        for (final ExceptionTableEntry entry : builder._tableEntries) {
            final ControlFlowNode handlerStart = handlerStartNodes.get(entry);
            final List<ControlFlowNode> dominatedNodes = new ArrayList<>();

            for (final ControlFlowNode node : findDominatedNodes(cfg, handlerStart)) {
                if (node.getNodeType() == ControlFlowNodeType.Normal) {
                    dominatedNodes.add(node);
                }
            }

            Collections.sort(
                dominatedNodes,
                new Comparator<ControlFlowNode>() {
                    @Override
                    public int compare(@NotNull final ControlFlowNode o1, @NotNull final ControlFlowNode o2) {
                        return Integer.compare(o1.getBlockIndex(), o2.getBlockIndex());
                    }
                }
            );

            for (int i = 1; i < dominatedNodes.size(); i++) {
                final ControlFlowNode prev = dominatedNodes.get(i - 1);
                final ControlFlowNode node = dominatedNodes.get(i);

                if (node.getBlockIndex() != prev.getBlockIndex() + 1) {
                    for (int j = i; j < dominatedNodes.size(); j++) {
                        dominatedNodes.remove(i);
                        break;
                    }
                }
            }

            final Instruction lastInstruction = instructions.get(instructions.size() - 1);

            final InstructionBlock tryBlock;

            if (entry.getEndOffset() == lastInstruction.getEndOffset()) {
                tryBlock = new InstructionBlock(
                    instructions.atOffset(entry.getStartOffset()),
                    lastInstruction
                );
            }
            else {
                tryBlock = new InstructionBlock(
                    instructions.atOffset(entry.getStartOffset()),
                    instructions.atOffset(entry.getEndOffset()).getPrevious()
                );
            }

            if (entry.getCatchType() == null) {
                handlers.add(
                    ExceptionHandler.createFinally(
                        tryBlock,
                        new InstructionBlock(handlerStart.getStart(), lastOrDefault(dominatedNodes).getEnd())
                    )
                );
            }
            else {
                handlers.add(
                    ExceptionHandler.createCatch(
                        tryBlock,
                        new InstructionBlock(handlerStart.getStart(), lastOrDefault(dominatedNodes).getEnd()),
                        entry.getCatchType()
                    )
                );
            }
        }

//        Collections.sort(handlers);
//        ControlFlowGraphBuilder.build(instructions, handlers).export(new File("w:/dump/try"));

        return handlers;
    }

    private ControlFlowNode findNode(final Instruction instruction) {
        if (instruction == null) {
            return null;
        }

        return firstOrDefault(
            _nodes,
            new Predicate<ControlFlowNode>() {
                @Override
                public boolean test(final ControlFlowNode node) {
                    return node.getNodeType() == ControlFlowNodeType.Normal &&
                           instruction.getOffset() >= node.getStart().getOffset() &&
                           instruction.getOffset() < node.getEnd().getEndOffset();
                }
            }
        );
    }

    private static Set<ControlFlowNode> findDominatedNodes(final ControlFlowGraph cfg, final ControlFlowNode head) {
        final Set<ControlFlowNode> agenda = new LinkedHashSet<>();
        final Set<ControlFlowNode> result = new LinkedHashSet<>();

        agenda.add(head);

        while (!agenda.isEmpty()) {
            final ControlFlowNode addNode = agenda.iterator().next();

            agenda.remove(addNode);

            if (!head.dominates(addNode) &&
                !shouldIncludeExceptionalExit(cfg, head, addNode)) {

                continue;
            }

            if (!result.add(addNode)) {
                continue;
            }

            for (final ControlFlowNode successor : addNode.getSuccessors()) {
                agenda.add(successor);
            }
        }

        return result;
    }

    private static boolean shouldIncludeExceptionalExit(final ControlFlowGraph cfg, final ControlFlowNode head, final ControlFlowNode node) {
        if (node.getNodeType() != ControlFlowNodeType.Normal) {
            return false;
        }

        if (!node.getDominanceFrontier().contains(cfg.getExceptionalExit()) &&
            !node.dominates(cfg.getExceptionalExit())) {

            final ControlFlowNode innermostHandlerNode = findInnermostExceptionHandlerNode(cfg, node.getStart().getOffset());

            if (innermostHandlerNode == null || !node.getDominanceFrontier().contains(innermostHandlerNode)) {
                return false;
            }
        }

        if (node.getStart().getNext() != node.getEnd()) {
            return false;
        }

        if (head.getStart().getOpCode().isStore() &&
            node.getStart().getOpCode().isLoad() &&
            node.getEnd().getOpCode() == OpCode.ATHROW) {

            return InstructionHelper.getLoadOrStoreSlot(head.getStart()) ==
                   InstructionHelper.getLoadOrStoreSlot(node.getStart());
        }

        return false;
    }

    private final InstructionCollection _instructions;
    private final List<ExceptionTableEntry> _tableEntries;
    private final List<ExceptionHandler> _handlerPlaceholders;
    private final List<ControlFlowNode> _nodes = new Collection<>();
    private final int[] _offsets;
    private final boolean[] _hasIncomingJumps;
    private final ControlFlowNode _entryPoint;
    private final ControlFlowNode _regularExit;
    private final ControlFlowNode _exceptionalExit;

    private int _nextBlockId;
    boolean copyFinallyBlocks = false;

    private ExceptionHandlerMapper(final InstructionCollection instructions, final List<ExceptionTableEntry> tableEntries) {
        _instructions = VerifyArgument.notNull(instructions, "instructions");
        _tableEntries = VerifyArgument.notNull(tableEntries, "tableEntries");
        _handlerPlaceholders = createHandlerPlaceholders();

        _offsets = new int[instructions.size()];
        _hasIncomingJumps = new boolean[instructions.size()];

        for (int i = 0; i < instructions.size(); i++) {
            _offsets[i] = instructions.get(i).getOffset();
        }

        _entryPoint = new ControlFlowNode(_nextBlockId++, 0, ControlFlowNodeType.EntryPoint);
        _regularExit = new ControlFlowNode(_nextBlockId++, -1, ControlFlowNodeType.RegularExit);
        _exceptionalExit = new ControlFlowNode(_nextBlockId++, -2, ControlFlowNodeType.ExceptionalExit);

        _nodes.add(_entryPoint);
        _nodes.add(_regularExit);
        _nodes.add(_exceptionalExit);
    }

    private ControlFlowGraph build() {
        calculateIncomingJumps();
        createNodes();
        createRegularControlFlow();
        createExceptionalControlFlow();

        return new ControlFlowGraph(_nodes.toArray(new ControlFlowNode[_nodes.size()]));
    }

    private boolean isHandlerStart(final Instruction instruction) {
        for (final ExceptionTableEntry entry : _tableEntries) {
            if (entry.getHandlerOffset() == instruction.getOffset()) {
                return true;
            }
        }
        return false;
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

        for (final ExceptionTableEntry entry : _tableEntries) {
            _hasIncomingJumps[getInstructionIndex(_instructions.atOffset(entry.getHandlerOffset()))] = true;
        }
    }

    private void createNodes() {
        //
        // Step 2a: Find basic blocks and create nodes for them.
        //

        final InstructionCollection instructions = _instructions;

        for (int i = 0, n = instructions.size(); i < n; i++) {
            final Instruction blockStart = instructions.get(i);
            final ExceptionHandler blockStartExceptionHandler = findInnermostExceptionHandler(blockStart.getOffset());

            //
            // See how big we can make that block...
            //
            for (; i + 1 < n; i++) {
                final Instruction instruction = instructions.get(i);
                final OpCode opCode = instruction.getOpCode();

                if (opCode.isBranch() && !opCode.isJumpToSubroutine() /*|| opCode.canThrow()*/ || _hasIncomingJumps[i + 1]) {
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

            final ControlFlowNode node = new ControlFlowNode(_nodes.size(), blockStart, instructions.get(i));

            node.setUserData(blockStartExceptionHandler);

            _nodes.add(node);
        }

        //
        // Step 2b: Create special nodes for exception handling constructs.
        //

        for (final ExceptionHandler handler : _handlerPlaceholders) {
            final int index = _nodes.size();
            _nodes.add(new ControlFlowNode(index, handler, null));
        }
    }

    private void createRegularControlFlow() {
        //
        // Step 3: Create edges for the normal control flow (assuming no exceptions thrown).
        //

        final InstructionCollection instructions = _instructions;

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

                if (next != null && !isHandlerStart(next)) {
                    createEdge(node, next, JumpType.Normal);
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

                    createEdge(node, instruction.<Instruction>getOperand(0), JumpType.Normal);
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
            // Create edges for return instructions.
            //
            if (endOpCode.getFlowControl() == FlowControl.Return) {
                createEdge(node, _regularExit, JumpType.Normal);
            }
//            else if (endOpCode.getFlowControl() == FlowControl.Throw) {
//                createEdge(node, _exceptionalExit, JumpType.JumpToExceptionHandler);
//            }
        }
    }

    private void createExceptionalControlFlow() {
        //
        // Step 4: Create edges for the exceptional control flow.
        //

        for (final ControlFlowNode node : _nodes) {
            if (node.getNodeType() == ControlFlowNodeType.Normal) {
                final Instruction end = node.getEnd();
                final ExceptionHandler innermostHandler = findInnermostExceptionHandler(node.getEnd().getOffset());

                if (innermostHandler != null) {
                    for (final ExceptionHandler other : _handlerPlaceholders) {
                        if (other.getTryBlock().equals(innermostHandler.getTryBlock())) {
                            final ControlFlowNode handlerNode = firstOrDefault(
                                _nodes,
                                new Predicate<ControlFlowNode>() {
                                    @Override
                                    public boolean test(final ControlFlowNode node) {
                                        return node.getExceptionHandler() == other;
                                    }
                                }
                            );

                            if (node != handlerNode) {
                                createEdge(node, handlerNode, JumpType.JumpToExceptionHandler);
                            }
                        }
                    }
                }
                else if (end.getOpCode() == OpCode.ATHROW) {
                    createEdge(node, _exceptionalExit, JumpType.JumpToExceptionHandler);
                }
            }

            final ExceptionHandler exceptionHandler = node.getExceptionHandler();

            if (exceptionHandler != null) {
                final ControlFlowNode parentHandler = findParentExceptionHandlerNode(node);

                if (parentHandler.getNodeType() != ControlFlowNodeType.ExceptionalExit) {
                    for (final ExceptionHandler other : _handlerPlaceholders) {
                        if (Comparer.equals(other.getTryBlock(), parentHandler.getExceptionHandler().getTryBlock())) {
                            final ControlFlowNode handlerNode = firstOrDefault(
                                _nodes,
                                new Predicate<ControlFlowNode>() {
                                    @Override
                                    public boolean test(final ControlFlowNode node) {
                                        return node.getExceptionHandler() == other;
                                    }
                                }
                            );

                            if (handlerNode != node) {
                                createEdge(node, handlerNode, JumpType.JumpToExceptionHandler);
                            }
                        }
                    }
                }

                createEdge(
                    node,
                    exceptionHandler.getHandlerBlock().getFirstInstruction(),
                    JumpType.Normal
                );
            }
        }
    }

    private static ControlFlowNode findInnermostExceptionHandlerNode(final ControlFlowGraph cfg, final int offsetInTryBlock) {
        ExceptionHandler result = null;
        ControlFlowNode resultNode = null;

        final List<ControlFlowNode> nodes = cfg.getNodes();

        for (int i = nodes.size() - 1; i >= 0; i--) {
            final ControlFlowNode node = nodes.get(i);
            final ExceptionHandler handler = node.getExceptionHandler();

            if (handler == null) {
                break;
            }

            final InstructionBlock tryBlock = handler.getTryBlock();

            if (tryBlock.getFirstInstruction().getOffset() <= offsetInTryBlock &&
                offsetInTryBlock < tryBlock.getLastInstruction().getEndOffset() &&
                isNarrower(handler, result)) {

                result = handler;
                resultNode = node;
            }
        }

        return resultNode;
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

    private ExceptionHandler findInnermostExceptionHandler(final int offsetInTryBlock) {
        ExceptionHandler result = null;

        for (final ExceptionHandler handler : _handlerPlaceholders) {
            final InstructionBlock tryBlock = handler.getTryBlock();

            if (tryBlock.getFirstInstruction().getOffset() <= offsetInTryBlock &&
                offsetInTryBlock < tryBlock.getLastInstruction().getEndOffset() &&
                (result == null || isNarrower(handler, result))) {

                result = handler;
            }
        }

        return result;
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

    private int getInstructionIndex(final Instruction instruction) {
        final int index = Arrays.binarySearch(_offsets, instruction.getOffset());
        assert index >= 0;
        return index;
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

        fromNode.getOutgoing().add(edge);
        toNode.getIncoming().add(edge);

        return edge;
    }

    private List<ExceptionHandler> createHandlerPlaceholders() {
        final ArrayList<ExceptionHandler> handlers = new ArrayList<>();

        for (final ExceptionTableEntry entry : _tableEntries) {
            final ExceptionHandler handler;
            final Instruction afterTry = _instructions.tryGetAtOffset(entry.getEndOffset());

            if (entry.getCatchType() == null) {
                handler = ExceptionHandler.createFinally(
                    new InstructionBlock(
                        _instructions.atOffset(entry.getStartOffset()),
                        afterTry != null ? afterTry.getPrevious() : last(_instructions)
                    ),
                    new InstructionBlock(
                        _instructions.atOffset(entry.getHandlerOffset()),
                        _instructions.atOffset(entry.getHandlerOffset())
                    )
                );
            }
            else {
                handler = ExceptionHandler.createCatch(
                    new InstructionBlock(
                        _instructions.atOffset(entry.getStartOffset()),
                        afterTry != null ? afterTry.getPrevious() : last(_instructions)
                    ),
                    new InstructionBlock(
                        _instructions.atOffset(entry.getHandlerOffset()),
                        _instructions.atOffset(entry.getHandlerOffset())
                    ),
                    entry.getCatchType()
                );
            }

            handlers.add(handler);
        }

        return handlers;
    }
}

