/*
 * AstBuilder.java
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

package com.strobel.decompiler.ast;

import com.strobel.annotations.NotNull;
import com.strobel.assembler.flowanalysis.ControlFlowEdge;
import com.strobel.assembler.flowanalysis.ControlFlowGraph;
import com.strobel.assembler.flowanalysis.ControlFlowGraphBuilder;
import com.strobel.assembler.flowanalysis.ControlFlowNode;
import com.strobel.assembler.flowanalysis.ControlFlowNodeType;
import com.strobel.assembler.flowanalysis.JumpType;
import com.strobel.assembler.ir.*;
import com.strobel.assembler.metadata.*;
import com.strobel.core.*;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.ITextOutput;
import com.strobel.decompiler.InstructionHelper;
import com.strobel.decompiler.PlainTextOutput;
import com.strobel.functions.Function;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.strobel.core.CollectionUtilities.*;
import static com.strobel.decompiler.ast.PatternMatching.*;
import static java.lang.String.format;

public final class AstBuilder {
    private final static Logger LOG = Logger.getLogger(AstBuilder.class.getSimpleName());
    private final static AstCode[] CODES = AstCode.values();
    private final static StackSlot[] EMPTY_STACK = new StackSlot[0];
    private final static ByteCode[] EMPTY_DEFINITIONS = new ByteCode[0];

    private final Map<ExceptionHandler, ByteCode> _loadExceptions = new LinkedHashMap<>();
    private final Set<Instruction> _removed = new LinkedHashSet<>();
    private Map<Instruction, Instruction> _originalInstructionMap;
    private ControlFlowGraph _cfg;
    private InstructionCollection _instructions;
    private List<ExceptionHandler> _exceptionHandlers;
    private MethodBody _body;
    private boolean _optimize;
    private DecompilerContext _context;
    private CoreMetadataFactory _factory;

    public static List<Node> build(final MethodBody body, final boolean optimize, final DecompilerContext context) {
        final AstBuilder builder = new AstBuilder();

        builder._body = VerifyArgument.notNull(body, "body");
        builder._optimize = optimize;
        builder._context = VerifyArgument.notNull(context, "context");

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(
                format(
                    "Beginning bytecode AST construction for %s:%s...",
                    body.getMethod().getFullName(),
                    body.getMethod().getSignature()
                )
            );
        }

        if (body.getInstructions().isEmpty()) {
            return Collections.emptyList();
        }

        builder._instructions = copyInstructions(body.getInstructions());

        final InstructionCollection oldInstructions = body.getInstructions();
        final InstructionCollection newInstructions = builder._instructions;

        builder._originalInstructionMap = new IdentityHashMap<>();

        for (int i = 0; i < newInstructions.size(); i++) {
            builder._originalInstructionMap.put(newInstructions.get(i), oldInstructions.get(i));
        }

        builder._exceptionHandlers = remapHandlers(body.getExceptionHandlers(), builder._instructions);

        Collections.sort(builder._exceptionHandlers);

        builder.removeGetClassCallsForInvokeDynamic();
        builder.pruneExceptionHandlers();
        builder.inlineSubroutines();

        FinallyInlining.run(builder._body, builder._instructions, builder._exceptionHandlers, builder._removed);

        builder._cfg = ControlFlowGraphBuilder.build(builder._instructions, builder._exceptionHandlers);
        builder._cfg.computeDominance();
        builder._cfg.computeDominanceFrontier();

        LOG.fine("Performing stack analysis...");

        final List<ByteCode> byteCode = builder.performStackAnalysis();

        LOG.fine("Creating bytecode AST...");

        @SuppressWarnings("UnnecessaryLocalVariable")
        final List<Node> ast = builder.convertToAst(
            byteCode,
            new LinkedHashSet<>(builder._exceptionHandlers),
            0,
            new MutableInteger(byteCode.size())
        );

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(
                format(
                    "Finished bytecode AST construction for %s:%s.",
                    body.getMethod().getFullName(),
                    body.getMethod().getSignature()
                )
            );
        }

        return ast;
    }

    private static boolean isGetClassInvocation(final Instruction p) {
        return p != null &&
               p.getOpCode() == OpCode.INVOKEVIRTUAL &&
               p.<MethodReference>getOperand(0).getParameters().isEmpty() &&
               StringUtilities.equals(p.<MethodReference>getOperand(0).getName(), "getClass");
    }

    private void removeGetClassCallsForInvokeDynamic() {
        for (final Instruction i : _instructions) {
            if (i.getOpCode() != OpCode.INVOKEDYNAMIC) {
                continue;
            }

            final Instruction p1 = i.getPrevious();

            if (p1 == null || p1.getOpCode() != OpCode.POP) {
                continue;
            }

            final Instruction p2 = p1.getPrevious();

            if (p2 == null || !isGetClassInvocation(p2)) {
                continue;
            }

            final Instruction p3 = p2.getPrevious();

            if (p3 == null || p3.getOpCode() != OpCode.DUP) {
                continue;
            }

            p1.setOpCode(OpCode.NOP);
            p1.setOperand(null);

            p2.setOpCode(OpCode.NOP);
            p2.setOperand(null);

            p3.setOpCode(OpCode.NOP);
            p3.setOperand(null);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void inlineSubroutines() {
        LOG.fine("Inlining subroutines...");

        final List<SubroutineInfo> subroutines = findSubroutines();

        if (subroutines.isEmpty()) {
            return;
        }

        final List<ExceptionHandler> handlers = _exceptionHandlers;
        final Set<ExceptionHandler> originalHandlers = new HashSet<>(handlers);
        final List<SubroutineInfo> inlinedSubroutines = new ArrayList<>();
        final Set<Instruction> instructionsToKeep = new HashSet<>();

        for (final SubroutineInfo subroutine : subroutines) {
            if (callsOtherSubroutine(subroutine, subroutines)) {
                continue;
            }

            boolean fullyInlined = true;

            for (final Instruction reference : subroutine.liveReferences) {
                fullyInlined &= inlineSubroutine(subroutine, reference);
            }

            for (final Instruction p : subroutine.deadReferences) {
                p.setOpCode(OpCode.NOP);
                p.setOperand(null);
                _removed.add(p);
            }

            if (fullyInlined) {
                inlinedSubroutines.add(subroutine);
            }
            else {
                for (final ControlFlowNode node : subroutine.contents) {
                    for (Instruction p = node.getStart();
                         p != null && p.getOffset() < node.getStart().getEndOffset();
                         p = p.getNext()) {

                        instructionsToKeep.add(p);
                    }
                }
            }
        }

        //
        // NOP-out the original subroutine instructions only after all subroutines have been processed.
        // Note that there might be overlapping subroutines, and it's possible that some ranges may still
        // be live code if not all subroutines were successfully inlined at all jump sites.
        //
        for (final SubroutineInfo subroutine : inlinedSubroutines) {
            for (Instruction p = subroutine.start;
                 p != null && p.getOffset() < subroutine.end.getEndOffset();
                 p = p.getNext()) {

                if (instructionsToKeep.contains(p)) {
                    continue;
                }

                p.setOpCode(OpCode.NOP);
                p.setOperand(null);

                _removed.add(p);
            }

            for (final ExceptionHandler handler : subroutine.containedHandlers) {
                if (originalHandlers.contains(handler)) {
                    handlers.remove(handler);
                }
            }
        }
    }

    private boolean inlineSubroutine(final SubroutineInfo subroutine, final Instruction reference) {
        if (!subroutine.start.getOpCode().isStore() && subroutine.start.getOpCode() != OpCode.POP) {
            return false;
        }

        final InstructionCollection instructions = _instructions;
        final Map<Instruction, Instruction> originalInstructionMap = _originalInstructionMap;
        final boolean nonEmpty = subroutine.start != subroutine.end && subroutine.start.getNext() != subroutine.end;

        if (nonEmpty) {
            final int jumpIndex = instructions.indexOf(reference);
            final List<Instruction> originalContents = new ArrayList<>();

            for (final ControlFlowNode node : subroutine.contents) {
                for (Instruction p = node.getStart();
                     p != null && p.getOffset() < node.getEnd().getEndOffset();
                     p = p.getNext()) {

                    originalContents.add(p);
                }
            }

            final Map<Instruction, Instruction> remappedJumps = new IdentityHashMap<>();
            final List<Instruction> contents = copyInstructions(originalContents);

            for (int i = 0, n = originalContents.size(); i < n; i++) {
                remappedJumps.put(originalContents.get(i), contents.get(i));
                originalInstructionMap.put(contents.get(i), mappedInstruction(originalInstructionMap, originalContents.get(i)));
            }

            final Instruction newStart = mappedInstruction(remappedJumps, subroutine.start);

            final Instruction newEnd = reference.getNext() != null ? reference.getNext()
                                                                   : mappedInstruction(remappedJumps, subroutine.end).getPrevious();

            for (final ControlFlowNode exitNode : subroutine.exitNodes) {
                final Instruction newExit = mappedInstruction(remappedJumps, exitNode.getEnd());

                if (newExit != null) {
                    newExit.setOpCode(OpCode.GOTO);
                    newExit.setOperand(newEnd);
                    remappedJumps.put(newExit, newEnd);
                }
            }

            newStart.setOpCode(OpCode.NOP);
            newStart.setOperand(null);

            instructions.addAll(jumpIndex, toList(contents));

            if (newStart != first(contents)) {
                instructions.add(jumpIndex, new Instruction(OpCode.GOTO, newStart));
            }

            instructions.remove(reference);
            instructions.recomputeOffsets();

            remappedJumps.put(reference, first(contents));
            remappedJumps.put(subroutine.end, newEnd);
            remappedJumps.put(subroutine.start, newStart);

            remapJumps(Collections.singletonMap(reference, newStart));
            remapHandlersForInlinedSubroutine(reference, first(contents), last(contents));
            duplicateHandlersForInlinedSubroutine(subroutine, remappedJumps);
        }
        else {
            reference.setOpCode(OpCode.NOP);
            reference.setOperand(OpCode.NOP);
        }

        return true;
    }

    @SuppressWarnings("ConstantConditions")
    private void remapHandlersForInlinedSubroutine(
        final Instruction jump,
        final Instruction start,
        final Instruction end) {

        final List<ExceptionHandler> handlers = _exceptionHandlers;

        for (int i = 0; i < handlers.size(); i++) {
            final ExceptionHandler handler = handlers.get(i);

            final InstructionBlock oldTry = handler.getTryBlock();
            final InstructionBlock oldHandler = handler.getHandlerBlock();

            final InstructionBlock newTryBlock;
            final InstructionBlock newHandlerBlock;

            if (oldTry.getFirstInstruction() == jump || oldTry.getLastInstruction() == jump) {
                newTryBlock = new InstructionBlock(
                    oldTry.getFirstInstruction() == jump ? start : oldTry.getFirstInstruction(),
                    oldTry.getLastInstruction() == jump ? end : oldTry.getLastInstruction()
                );
            }
            else {
                newTryBlock = oldTry;
            }

            if (oldHandler.getFirstInstruction() == jump || oldHandler.getLastInstruction() == jump) {
                newHandlerBlock = new InstructionBlock(
                    oldHandler.getFirstInstruction() == jump ? start : oldHandler.getFirstInstruction(),
                    oldHandler.getLastInstruction() == jump ? end : oldHandler.getLastInstruction()
                );
            }
            else {
                newHandlerBlock = oldHandler;
            }

            if (newTryBlock != oldTry || newHandlerBlock != oldHandler) {
                if (handler.isCatch()) {
                    handlers.set(
                        i,
                        ExceptionHandler.createCatch(newTryBlock, newHandlerBlock, handler.getCatchType())
                    );
                }
                else {
                    handlers.set(
                        i,
                        ExceptionHandler.createFinally(newTryBlock, newHandlerBlock)
                    );
                }
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void duplicateHandlersForInlinedSubroutine(final SubroutineInfo subroutine, final Map<Instruction, Instruction> oldToNew) {
        final List<ExceptionHandler> handlers = _exceptionHandlers;

        for (final ExceptionHandler handler : subroutine.containedHandlers) {
            final InstructionBlock oldTry = handler.getTryBlock();
            final InstructionBlock oldHandler = handler.getHandlerBlock();

            final InstructionBlock newTryBlock;
            final InstructionBlock newHandlerBlock;

            final Instruction newTryStart = mappedInstruction(oldToNew, oldTry.getFirstInstruction());
            final Instruction newTryEnd = mappedInstruction(oldToNew, oldTry.getLastInstruction());

            final Instruction newHandlerStart = mappedInstruction(oldToNew, oldHandler.getFirstInstruction());
            final Instruction newHandlerEnd = mappedInstruction(oldToNew, oldHandler.getLastInstruction());

            if (newTryStart != null || newTryEnd != null) {
                newTryBlock = new InstructionBlock(
                    newTryStart != null ? newTryStart : oldTry.getFirstInstruction(),
                    newTryEnd != null ? newTryEnd : oldTry.getLastInstruction()
                );
            }
            else {
                newTryBlock = oldTry;
            }

            if (newHandlerStart != null || newHandlerEnd != null) {
                newHandlerBlock = new InstructionBlock(
                    newHandlerStart != null ? newHandlerStart : oldHandler.getFirstInstruction(),
                    newHandlerEnd != null ? newHandlerEnd : oldHandler.getLastInstruction()
                );
            }
            else {
                newHandlerBlock = oldHandler;
            }

            if (newTryBlock != oldTry || newHandlerBlock != oldHandler) {
                handlers.add(
                    handler.isCatch() ? ExceptionHandler.createCatch(newTryBlock, newHandlerBlock, handler.getCatchType())
                                      : ExceptionHandler.createFinally(newTryBlock, newHandlerBlock)
                );
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void remapJumps(final Map<Instruction, Instruction> remappedJumps) {
        for (final Instruction instruction : _instructions) {
            if (instruction.hasLabel()) {
                instruction.getLabel().setIndex(instruction.getOffset());
            }

            if (instruction.getOperandCount() == 0) {
                continue;
            }

            final Object operand = instruction.getOperand(0);

            if (operand instanceof Instruction) {
                final Instruction oldTarget = (Instruction) operand;
                final Instruction newTarget = mappedInstruction(remappedJumps, oldTarget);

                if (newTarget != null) {
                    if (newTarget == instruction) {
                        instruction.setOpCode(OpCode.NOP);
                        instruction.setOperand(null);
                    }
                    else {
                        instruction.setOperand(newTarget);

                        if (!newTarget.hasLabel()) {
                            newTarget.setLabel(new com.strobel.assembler.metadata.Label(newTarget.getOffset()));
                        }
                    }
                }
            }
            else if (operand instanceof SwitchInfo) {
                final SwitchInfo oldOperand = (SwitchInfo) operand;

                final Instruction oldDefault = oldOperand.getDefaultTarget();
                final Instruction newDefault = mappedInstruction(remappedJumps, oldDefault);

                if (newDefault != null && !newDefault.hasLabel()) {
                    newDefault.setLabel(new com.strobel.assembler.metadata.Label(newDefault.getOffset()));
                }

                final Instruction[] oldTargets = oldOperand.getTargets();

                Instruction[] newTargets = null;

                for (int i = 0; i < oldTargets.length; i++) {
                    final Instruction newTarget = mappedInstruction(remappedJumps, oldTargets[i]);

                    if (newTarget != null) {
                        if (newTargets == null) {
                            newTargets = Arrays.copyOf(oldTargets, oldTargets.length);
                        }

                        newTargets[i] = newTarget;

                        if (!newTarget.hasLabel()) {
                            newTarget.setLabel(new com.strobel.assembler.metadata.Label(newTarget.getOffset()));
                        }
                    }
                }

                if (newDefault != null || newTargets != null) {
                    final SwitchInfo newOperand = new SwitchInfo(
                        oldOperand.getKeys(),
                        newDefault != null ? newDefault : oldDefault,
                        newTargets != null ? newTargets : oldTargets
                    );

                    instruction.setOperand(newOperand);
                }
            }
        }
    }

    private boolean callsOtherSubroutine(final SubroutineInfo subroutine, final List<SubroutineInfo> subroutines) {
        return any(
            subroutines,
            new Predicate<SubroutineInfo>() {
                @Override
                public boolean test(final SubroutineInfo info) {
                    return info != subroutine &&
                           any(
                               info.liveReferences,
                               new Predicate<Instruction>() {
                                   @Override
                                   public boolean test(final Instruction p) {
                                       return p.getOffset() >= subroutine.start.getOffset() &&
                                              p.getOffset() < subroutine.end.getEndOffset();
                                   }
                               }
                           ) &&
                           !subroutine.contents.containsAll(info.contents);
                }
            }
        );
    }

    private List<SubroutineInfo> findSubroutines() {
        final InstructionCollection instructions = _instructions;

        if (instructions.isEmpty()) {
            return Collections.emptyList();
        }

        Map<ExceptionHandler, Pair<Set<ControlFlowNode>, Set<ControlFlowNode>>> handlerContents = null;
        Map<Instruction, SubroutineInfo> subroutineMap = null;
        ControlFlowGraph cfg = null;

        for (Instruction p = first(instructions);
             p != null;
             p = p.getNext()) {

            if (!p.getOpCode().isJumpToSubroutine()) {
                continue;
            }

            final boolean isLive = !_removed.contains(p);

            if (cfg == null) {
                cfg = ControlFlowGraphBuilder.build(instructions, _exceptionHandlers);
                cfg.computeDominance();
                cfg.computeDominanceFrontier();

                subroutineMap = new IdentityHashMap<>();
                handlerContents = new IdentityHashMap<>();

                for (final ExceptionHandler handler : _exceptionHandlers) {
                    final InstructionBlock tryBlock = handler.getTryBlock();
                    final InstructionBlock handlerBlock = handler.getHandlerBlock();

                    final Set<ControlFlowNode> tryNodes = findDominatedNodes(
                        cfg,
                        findNode(cfg, tryBlock.getFirstInstruction()),
                        true,
                        Collections.<ControlFlowNode>emptySet()
                    );

                    final Set<ControlFlowNode> handlerNodes = findDominatedNodes(
                        cfg,
                        findNode(cfg, handlerBlock.getFirstInstruction()),
                        true,
                        Collections.<ControlFlowNode>emptySet()
                    );

                    handlerContents.put(handler, Pair.create(tryNodes, handlerNodes));
                }
            }

            final Instruction target = p.getOperand(0);

            if (_removed.contains(target)) {
                continue;
            }

            SubroutineInfo info = subroutineMap.get(target);

            if (info == null) {
                final ControlFlowNode start = findNode(cfg, target);

                final List<ControlFlowNode> contents = toList(
                    findDominatedNodes(
                        cfg,
                        start,
                        true,
                        Collections.<ControlFlowNode>emptySet()
                    )
                );

                Collections.sort(contents);

                subroutineMap.put(target, info = new SubroutineInfo(start, contents, cfg));

                for (final ExceptionHandler handler : _exceptionHandlers) {
                    final Pair<Set<ControlFlowNode>, Set<ControlFlowNode>> pair = handlerContents.get(handler);

                    if (contents.containsAll(pair.getFirst()) && contents.containsAll(pair.getSecond())) {
                        info.containedHandlers.add(handler);
                    }
                }
            }

            if (isLive) {
                info.liveReferences.add(p);
            }
            else {
                info.deadReferences.add(p);
            }
        }

        if (subroutineMap == null) {
            return Collections.emptyList();
        }

        final List<SubroutineInfo> subroutines = toList(subroutineMap.values());

        Collections.sort(
            subroutines,
            new Comparator<SubroutineInfo>() {
                @Override
                public int compare(@NotNull final SubroutineInfo o1, @NotNull final SubroutineInfo o2) {
                    if (o1.contents.containsAll(o2.contents)) {
                        return 1;
                    }
                    if (o2.contents.containsAll(o1.contents)) {
                        return -1;
                    }
                    return Integer.compare(o2.start.getOffset(), o1.start.getOffset());
                }
            }
        );

        return subroutines;
    }

    private final static class SubroutineInfo {
        final Instruction start;
        final Instruction end;
        final List<Instruction> liveReferences = new ArrayList<>();
        final List<Instruction> deadReferences = new ArrayList<>();
        final List<ControlFlowNode> contents;
        final ControlFlowNode entryNode;
        final List<ControlFlowNode> exitNodes = new ArrayList<>();
        final List<ExceptionHandler> containedHandlers = new ArrayList<>();
        final ControlFlowGraph cfg;

        public SubroutineInfo(final ControlFlowNode entryNode, final List<ControlFlowNode> contents, final ControlFlowGraph cfg) {
            this.start = entryNode.getStart();
            this.end = last(contents).getEnd();
            this.entryNode = entryNode;
            this.contents = contents;
            this.cfg = cfg;

            for (final ControlFlowNode node : contents) {
                if (node.getNodeType() == ControlFlowNodeType.Normal &&
                    node.getEnd().getOpCode().isReturnFromSubroutine()) {

                    this.exitNodes.add(node);
                }
            }
        }
    }

    private final static class HandlerInfo {
        final ExceptionHandler handler;
        final ControlFlowNode handlerNode;
        final ControlFlowNode head;
        final ControlFlowNode tail;
        final List<ControlFlowNode> tryNodes;
        final List<ControlFlowNode> handlerNodes;

        HandlerInfo(
            final ExceptionHandler handler,
            final ControlFlowNode handlerNode,
            final ControlFlowNode head,
            final ControlFlowNode tail,
            final List<ControlFlowNode> tryNodes,
            final List<ControlFlowNode> handlerNodes) {

            this.handler = handler;
            this.handlerNode = handlerNode;
            this.head = head;
            this.tail = tail;
            this.tryNodes = tryNodes;
            this.handlerNodes = handlerNodes;
        }
    }

    private static ControlFlowNode findNode(final ControlFlowGraph cfg, final Instruction instruction) {
        final int offset = instruction.getOffset();

        for (final ControlFlowNode node : cfg.getNodes()) {
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

    private static Set<ControlFlowNode> findDominatedNodes(
        final ControlFlowGraph cfg,
        final ControlFlowNode head,
        final boolean diveIntoHandlers,
        final Set<ControlFlowNode> terminals) {

        final Set<ControlFlowNode> visited = new LinkedHashSet<>();
        final ArrayDeque<ControlFlowNode> agenda = new ArrayDeque<>();
        final Set<ControlFlowNode> result = new LinkedHashSet<>();

        agenda.add(head);
        visited.add(head);

        while (!agenda.isEmpty()) {
            ControlFlowNode addNode = agenda.removeFirst();

            if (terminals.contains(addNode)) {
                continue;
            }

            if (diveIntoHandlers && addNode.getExceptionHandler() != null) {
                addNode = findNode(cfg, addNode.getExceptionHandler().getHandlerBlock().getFirstInstruction());
            }
            else if (diveIntoHandlers && addNode.getNodeType() == ControlFlowNodeType.EndFinally) {
                agenda.addAll(addNode.getDominatorTreeChildren());
                continue;
            }

            if (addNode == null || addNode.getNodeType() != ControlFlowNodeType.Normal) {
                continue;
            }

            if (!head.dominates(addNode) &&
                !shouldIncludeExceptionalExit(cfg, head, addNode)) {

                continue;
            }

            if (!result.add(addNode)) {
                continue;
            }

            for (final ControlFlowNode successor : addNode.getSuccessors()) {
                if (visited.add(successor)) {
                    agenda.add(successor);
                }
            }
        }

        return result;
    }

    private static boolean shouldIncludeExceptionalExit(
        final ControlFlowGraph cfg,
        final ControlFlowNode head,
        final ControlFlowNode node) {

        if (node.getNodeType() != ControlFlowNodeType.Normal) {
            return false;
        }

        if (!node.getDominanceFrontier().contains(cfg.getExceptionalExit()) &&
            !node.dominates(cfg.getExceptionalExit())) {

            final ControlFlowNode innermostHandlerNode = findInnermostExceptionHandlerNode(cfg, node.getEnd().getOffset(), false);

            if (innermostHandlerNode == null || !node.getDominanceFrontier().contains(innermostHandlerNode)) {
                return false;
            }
        }

        return head.getNodeType() == ControlFlowNodeType.Normal &&
               node.getNodeType() == ControlFlowNodeType.Normal &&
               node.getStart().getNext() == node.getEnd() &&
               head.getStart().getOpCode().isStore() &&
               node.getStart().getOpCode().isLoad() &&
               node.getEnd().getOpCode() == OpCode.ATHROW &&
               InstructionHelper.getLoadOrStoreSlot(head.getStart()) == InstructionHelper.getLoadOrStoreSlot(node.getStart());
    }

    private static ControlFlowNode findInnermostExceptionHandlerNode(
        final ControlFlowGraph cfg,
        final int offsetInTryBlock,
        final boolean finallyOnly) {
        ExceptionHandler result = null;
        ControlFlowNode resultNode = null;

        final List<ControlFlowNode> nodes = cfg.getNodes();

        for (int i = nodes.size() - 1; i >= 0; i--) {
            final ControlFlowNode node = nodes.get(i);
            final ExceptionHandler handler = node.getExceptionHandler();

            if (handler == null) {
                break;
            }

            if (finallyOnly && handler.isCatch()) {
                continue;
            }

            final InstructionBlock tryBlock = handler.getTryBlock();

            if (tryBlock.getFirstInstruction().getOffset() <= offsetInTryBlock &&
                offsetInTryBlock < tryBlock.getLastInstruction().getEndOffset() &&
                (result == null ||
                 tryBlock.getFirstInstruction().getOffset() > result.getTryBlock().getFirstInstruction().getOffset())) {

                result = handler;
                resultNode = node;
            }
        }

        return resultNode != null ? resultNode
                                  : cfg.getExceptionalExit();
    }

    private static boolean opCodesMatch(
        final Instruction tail1,
        final Instruction tail2,
        final int count,
        final Function<Instruction, Instruction> previous) {
        int i = 0;

        if (tail1 == null || tail2 == null) {
            return false;
        }

        for (Instruction p1 = tail1, p2 = tail2;
             p1 != null && p2 != null && i < count;
             p1 = previous.apply(p1), p2 = previous.apply(p2), i++) {

            final OpCode c1 = p1.getOpCode();
            final OpCode c2 = p2.getOpCode();

            if (c1.isLoad()) {
                if (!c2.isLoad() || c2.getStackBehaviorPush() != c1.getStackBehaviorPush()) {
                    return false;
                }
            }
            else if (c1.isStore()) {
                if (!c2.isStore() || c2.getStackBehaviorPop() != c1.getStackBehaviorPop()) {
                    return false;
                }
            }
            else if (c1 != p2.getOpCode()) {
                return false;
            }

            switch (c1.getOperandType()) {
                case TypeReferenceU1:
                    if (!Objects.equals(p1.getOperand(1), p2.getOperand(1))) {
                        return false;
                    }
                    // fall through
                case PrimitiveTypeCode:
                case TypeReference: {
                    if (!Objects.equals(p1.getOperand(0), p2.getOperand(0))) {
                        return false;
                    }
                    break;
                }

                case MethodReference:
                case FieldReference: {
                    final MemberReference m1 = p1.getOperand(0);
                    final MemberReference m2 = p2.getOperand(0);

                    if (!StringUtilities.equals(m1.getFullName(), m2.getFullName()) ||
                        !StringUtilities.equals(m1.getErasedSignature(), m2.getErasedSignature())) {

                        return false;
                    }

                    break;
                }

                case I1:
                case I2:
                case I8:
                case Constant:
                case WideConstant: {
                    if (!Objects.equals(p1.getOperand(0), p2.getOperand(0))) {
                        return false;
                    }
                    break;
                }

                case LocalI1:
                case LocalI2: {
                    if (!Objects.equals(p1.getOperand(1), p2.getOperand(1))) {
                        return false;
                    }
                    break;
                }
            }
        }

        return i == count;
    }

    private static Map<Instruction, ControlFlowNode> createNodeMap(final ControlFlowGraph cfg) {
        final Map<Instruction, ControlFlowNode> nodeMap = new IdentityHashMap<>();

        for (final ControlFlowNode node : cfg.getNodes()) {
            if (node.getNodeType() != ControlFlowNodeType.Normal) {
                continue;
            }

            for (Instruction p = node.getStart();
                 p != null && p.getOffset() < node.getEnd().getEndOffset();
                 p = p.getNext()) {

                nodeMap.put(p, node);
            }
        }

        return nodeMap;
    }

    private static List<ExceptionHandler> remapHandlers(final List<ExceptionHandler> handlers, final InstructionCollection instructions) {
        final List<ExceptionHandler> newHandlers = new ArrayList<>();

        for (final ExceptionHandler handler : handlers) {
            final InstructionBlock oldTry = handler.getTryBlock();
            final InstructionBlock oldHandler = handler.getHandlerBlock();

            final InstructionBlock newTry = new InstructionBlock(
                instructions.atOffset(oldTry.getFirstInstruction().getOffset()),
                instructions.atOffset(oldTry.getLastInstruction().getOffset())
            );

            final InstructionBlock newHandler = new InstructionBlock(
                instructions.atOffset(oldHandler.getFirstInstruction().getOffset()),
                instructions.atOffset(oldHandler.getLastInstruction().getOffset())
            );

            if (handler.isCatch()) {
                newHandlers.add(
                    ExceptionHandler.createCatch(
                        newTry,
                        newHandler,
                        handler.getCatchType()
                    )
                );
            }
            else {
                newHandlers.add(
                    ExceptionHandler.createFinally(
                        newTry,
                        newHandler
                    )
                );
            }
        }

        return newHandlers;
    }

    private static InstructionCollection copyInstructions(final List<Instruction> instructions) {
        final InstructionCollection instructionsCopy = new InstructionCollection();
        final Map<Instruction, Instruction> oldToNew = new IdentityHashMap<>();

        for (final Instruction instruction : instructions) {
            final Instruction copy = new Instruction(instruction.getOffset(), instruction.getOpCode());

            if (instruction.getOperandCount() > 1) {
                final Object[] operands = new Object[instruction.getOperandCount()];

                for (int i = 0; i < operands.length; i++) {
                    operands[i] = instruction.getOperand(i);
                }

                copy.setOperand(operands);
            }
            else {
                copy.setOperand(instruction.getOperand(0));
            }

            copy.setLabel(instruction.getLabel());

            instructionsCopy.add(copy);
            oldToNew.put(instruction, copy);
        }

        for (final Instruction instruction : instructionsCopy) {
            if (!instruction.hasOperand()) {
                continue;
            }

            final Object operand = instruction.getOperand(0);

            if (operand instanceof Instruction) {
                instruction.setOperand(mappedInstruction(oldToNew, (Instruction) operand));
            }
            else if (operand instanceof SwitchInfo) {
                final SwitchInfo oldOperand = (SwitchInfo) operand;

                final Instruction oldDefault = oldOperand.getDefaultTarget();
                final Instruction newDefault = mappedInstruction(oldToNew, oldDefault);

                final Instruction[] oldTargets = oldOperand.getTargets();
                final Instruction[] newTargets = new Instruction[oldTargets.length];

                for (int i = 0; i < newTargets.length; i++) {
                    newTargets[i] = mappedInstruction(oldToNew, oldTargets[i]);
                }

                final SwitchInfo newOperand = new SwitchInfo(oldOperand.getKeys(), newDefault, newTargets);

                newOperand.setLowValue(oldOperand.getLowValue());
                newOperand.setHighValue(oldOperand.getHighValue());

                instruction.setOperand(newOperand);
            }
        }

        instructionsCopy.recomputeOffsets();

        return instructionsCopy;
    }

    @SuppressWarnings("ConstantConditions")
    private void pruneExceptionHandlers() {
        LOG.fine("Pruning exception handlers...");

        final List<ExceptionHandler> handlers = _exceptionHandlers;

        if (handlers.isEmpty()) {
            return;
        }

        removeSelfHandlingFinallyHandlers();
        removeEmptyCatchBlockBodies();
        trimAggressiveFinallyBlocks();
        trimAggressiveCatchBlocks();
        closeTryHandlerGaps();
//        extendHandlers();
        mergeSharedHandlers();
        alignFinallyBlocksWithSiblingCatchBlocks();
        ensureDesiredProtectedRanges();

        for (int i = 0; i < handlers.size(); i++) {
            final ExceptionHandler handler = handlers.get(i);

            if (!handler.isFinally()) {
                continue;
            }

            final InstructionBlock tryBlock = handler.getTryBlock();
            final List<ExceptionHandler> siblings = findHandlers(tryBlock, handlers);

            for (int j = 0; j < siblings.size(); j++) {
                final ExceptionHandler sibling = siblings.get(j);

                if (sibling.isCatch() && j < siblings.size() - 1) {
                    final ExceptionHandler nextSibling = siblings.get(j + 1);

                    if (sibling.getHandlerBlock().getLastInstruction() !=
                        nextSibling.getHandlerBlock().getFirstInstruction().getPrevious()) {

                        final int index = handlers.indexOf(sibling);

                        handlers.set(
                            index,
                            ExceptionHandler.createCatch(
                                sibling.getTryBlock(),
                                new InstructionBlock(
                                    sibling.getHandlerBlock().getFirstInstruction(),
                                    nextSibling.getHandlerBlock().getFirstInstruction().getPrevious()
                                ),
                                sibling.getCatchType()
                            )
                        );

                        siblings.set(j, handlers.get(j));
                    }
                }
            }
        }

    outer:
        for (int i = 0; i < handlers.size(); i++) {
            final ExceptionHandler handler = handlers.get(i);

            if (!handler.isFinally()) {
                continue;
            }

            final InstructionBlock tryBlock = handler.getTryBlock();
            final List<ExceptionHandler> siblings = findHandlers(tryBlock, handlers);

            for (final ExceptionHandler sibling : siblings) {
                if (sibling == handler || sibling.isFinally()) {
                    continue;
                }

                for (int j = 0; j < handlers.size(); j++) {
                    final ExceptionHandler e = handlers.get(j);

                    if (e == handler || e == sibling || !e.isFinally()) {
                        continue;
                    }

                    if (e.getTryBlock().getFirstInstruction() == sibling.getHandlerBlock().getFirstInstruction() &&
                        e.getHandlerBlock().equals(handler.getHandlerBlock())) {

                        handlers.remove(j);

                        final int removeIndex = j--;

                        if (removeIndex < i) {
                            --i;
                            continue outer;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < handlers.size(); i++) {
            final ExceptionHandler handler = handlers.get(i);

            if (!handler.isFinally()) {
                continue;
            }

            final InstructionBlock tryBlock = handler.getTryBlock();
            final InstructionBlock handlerBlock = handler.getHandlerBlock();

            for (int j = 0; j < handlers.size(); j++) {
                final ExceptionHandler other = handlers.get(j);

                if (other != handler &&
                    other.isFinally() &&
                    other.getHandlerBlock().equals(handlerBlock) &&
                    tryBlock.contains(other.getTryBlock()) &&
                    tryBlock.getLastInstruction() == other.getTryBlock().getLastInstruction()) {

                    handlers.remove(j);

                    if (j < i) {
                        --i;
                        break;
                    }

                    --j;
                }
            }
        }

        for (int i = 0; i < handlers.size(); i++) {
            final ExceptionHandler handler = handlers.get(i);
            final InstructionBlock tryBlock = handler.getTryBlock();
            final ExceptionHandler firstHandler = findFirstHandler(tryBlock, handlers);
            final InstructionBlock firstHandlerBlock = firstHandler.getHandlerBlock();
            final Instruction firstAfterTry = tryBlock.getLastInstruction().getNext();
            final Instruction firstInHandler = firstHandlerBlock.getFirstInstruction();
            final Instruction lastBeforeHandler = firstInHandler.getPrevious();

            if (firstAfterTry != firstInHandler &&
                firstAfterTry != null &&
                lastBeforeHandler != null) {

                InstructionBlock newTryBlock = null;

                final FlowControl flowControl = lastBeforeHandler.getOpCode().getFlowControl();

                if (flowControl == FlowControl.Branch ||
                    flowControl == FlowControl.Return && lastBeforeHandler.getOpCode() == OpCode.RETURN) {

                    if (lastBeforeHandler == firstAfterTry) {
                        newTryBlock = new InstructionBlock(tryBlock.getFirstInstruction(), lastBeforeHandler);
                    }
                }
                else if (flowControl == FlowControl.Throw ||
                         flowControl == FlowControl.Return && lastBeforeHandler.getOpCode() != OpCode.RETURN) {

                    if (lastBeforeHandler.getPrevious() == firstAfterTry) {
                        newTryBlock = new InstructionBlock(tryBlock.getFirstInstruction(), lastBeforeHandler);
                    }
                }

                if (newTryBlock != null) {
                    final List<ExceptionHandler> siblings = findHandlers(tryBlock, handlers);

                    for (int j = 0; j < siblings.size(); j++) {
                        final ExceptionHandler sibling = siblings.get(j);
                        final int index = handlers.indexOf(sibling);

                        if (sibling.isCatch()) {
                            handlers.set(
                                index,
                                ExceptionHandler.createCatch(
                                    newTryBlock,
                                    sibling.getHandlerBlock(),
                                    sibling.getCatchType()
                                )
                            );
                        }
                        else {
                            handlers.set(
                                index,
                                ExceptionHandler.createFinally(
                                    newTryBlock,
                                    sibling.getHandlerBlock()
                                )
                            );
                        }
                    }
                }
            }
        }

        //
        // Look for finally blocks which duplicate an outer catch.
        //

        for (int i = 0; i < handlers.size(); i++) {
            final ExceptionHandler handler = handlers.get(i);
            final InstructionBlock tryBlock = handler.getTryBlock();
            final InstructionBlock handlerBlock = handler.getHandlerBlock();

            if (!handler.isFinally()) {
                continue;
            }

            final ExceptionHandler innermostHandler = findInnermostExceptionHandler(
                tryBlock.getFirstInstruction().getOffset(),
                handler
            );

            if (innermostHandler == null ||
                innermostHandler == handler ||
                innermostHandler.isFinally()) {

                continue;
            }

            for (int j = 0; j < handlers.size(); j++) {
                final ExceptionHandler sibling = handlers.get(j);

                if (sibling != handler &&
                    sibling != innermostHandler &&
                    sibling.getTryBlock().equals(handlerBlock) &&
                    sibling.getHandlerBlock().equals(innermostHandler.getHandlerBlock())) {

                    handlers.remove(j);

                    if (j < i) {
                        --i;
                        break;
                    }

                    --j;
                }
            }
        }
    }

    private void removeEmptyCatchBlockBodies() {
        final List<ExceptionHandler> handlers = _exceptionHandlers;

        for (int i = 0; i < handlers.size(); i++) {
            final ExceptionHandler handler = handlers.get(i);

            if (!handler.isCatch()) {
                continue;
            }

            final InstructionBlock catchBlock = handler.getHandlerBlock();
            final Instruction start = catchBlock.getFirstInstruction();
            final Instruction end = catchBlock.getLastInstruction();

            if (start != end || !start.getOpCode().isStore()) {
                continue;
            }

//            final InstructionBlock tryBlock = handler.getTryBlock();
//
//            for (int j = 0; j < handlers.size(); j++) {
//                if (i == j) {
//                    continue;
//                }
//
//                final ExceptionHandler other = handlers.get(j);
//                final InstructionBlock finallyBlock = other.getHandlerBlock();
//
//                if (other.isFinally() &&
//                    finallyBlock.contains(tryBlock) &&
//                    finallyBlock.contains(catchBlock)) {
//
//                    final Instruction endFinally = finallyBlock.getLastInstruction();
//
//                    if (endFinally != null &&
//                        endFinally.getOpCode().isThrow() &&
//                        endFinally.getPrevious() != null &&
//                        endFinally.getPrevious().getOpCode().isLoad() &&
//                        endFinally.getPrevious().getPrevious() == end &&
//                        (InstructionHelper.getLoadOrStoreSlot(endFinally.getPrevious()) !=
//                         InstructionHelper.getLoadOrStoreSlot(end))) {
//
//                        end.setOpCode(OpCode.POP);
//                        end.setOperand(null);
//                        _removed.add(end);
//                        break;
//                    }
//                }
//            }

            end.setOpCode(OpCode.POP);
            end.setOperand(null);
            _removed.add(end);
        }
    }

    private void ensureDesiredProtectedRanges() {
        final List<ExceptionHandler> handlers = _exceptionHandlers;

        for (int i = 0; i < handlers.size(); i++) {
            final ExceptionHandler handler = handlers.get(i);
            final InstructionBlock tryBlock = handler.getTryBlock();
            final List<ExceptionHandler> siblings = findHandlers(tryBlock, handlers);
            final ExceptionHandler firstSibling = first(siblings);
            final InstructionBlock firstHandler = firstSibling.getHandlerBlock();
            final Instruction desiredEndTry = firstHandler.getFirstInstruction().getPrevious();

            for (int j = 0; j < siblings.size(); j++) {
                ExceptionHandler sibling = siblings.get(j);

                if (handler.getTryBlock().getLastInstruction() != desiredEndTry) {
                    final int index = handlers.indexOf(sibling);

                    if (sibling.isCatch()) {
                        handlers.set(
                            index,
                            ExceptionHandler.createCatch(
                                new InstructionBlock(
                                    tryBlock.getFirstInstruction(),
                                    desiredEndTry
                                ),
                                sibling.getHandlerBlock(),
                                sibling.getCatchType()
                            )
                        );
                    }
                    else {
                        handlers.set(
                            index,
                            ExceptionHandler.createFinally(
                                new InstructionBlock(
                                    tryBlock.getFirstInstruction(),
                                    desiredEndTry
                                ),
                                sibling.getHandlerBlock()
                            )
                        );
                    }

                    sibling = handlers.get(index);
                    siblings.set(j, sibling);
                }
            }
        }
    }

    private void alignFinallyBlocksWithSiblingCatchBlocks() {
        final List<ExceptionHandler> handlers = _exceptionHandlers;

    outer:
        for (int i = 0; i < handlers.size(); i++) {
            final ExceptionHandler handler = handlers.get(i);

            if (handler.isCatch()) {
                continue;
            }

            final InstructionBlock tryBlock = handler.getTryBlock();
            final InstructionBlock handlerBlock = handler.getHandlerBlock();

            for (int j = 0; j < handlers.size(); j++) {
                if (i == j) {
                    continue;
                }

                final ExceptionHandler other = handlers.get(j);
                final InstructionBlock otherTry = other.getTryBlock();
                final InstructionBlock otherHandler = other.getHandlerBlock();

                if (other.isCatch() &&
                    otherHandler.getLastInstruction().getNext() == handlerBlock.getFirstInstruction() &&
                    otherTry.getFirstInstruction() == tryBlock.getFirstInstruction() &&
                    otherTry.getLastInstruction().getOffset() < tryBlock.getLastInstruction().getOffset() &&
                    tryBlock.getLastInstruction().getEndOffset() > otherHandler.getFirstInstruction().getOffset()) {

                    handlers.set(
                        i,
                        ExceptionHandler.createFinally(
                            new InstructionBlock(
                                tryBlock.getFirstInstruction(),
                                otherHandler.getFirstInstruction().getPrevious()
                            ),
                            handlerBlock
                        )
                    );

                    --i;
                    continue outer;
                }
            }
        }
    }

    private void mergeSharedHandlers() {
        final List<ExceptionHandler> handlers = _exceptionHandlers;

        for (int i = 0; i < handlers.size(); i++) {
            final ExceptionHandler handler = handlers.get(i);
            final List<ExceptionHandler> duplicates = findDuplicateHandlers(handler, handlers);

            for (int j = 0; j < duplicates.size() - 1; j++) {
                final ExceptionHandler h1 = duplicates.get(j);
                final ExceptionHandler h2 = duplicates.get(1 + j);

                final InstructionBlock try1 = h1.getTryBlock();
                final InstructionBlock try2 = h2.getTryBlock();

                final Instruction head = try1.getLastInstruction().getNext();
                final Instruction tail = try2.getFirstInstruction().getPrevious();

                final int i1 = handlers.indexOf(h1);
                final int i2 = handlers.indexOf(h2);

                if (head != tail) {
//                    if (tail.getOpCode().isUnconditionalBranch()) {
//                        switch (tail.getOpCode()) {
//                            case GOTO:
//                            case GOTO_W:
//                            case RETURN:
//                                tail = tail.getPrevious();
//                                break;
//
//                            case IRETURN:
//                            case LRETURN:
//                            case FRETURN:
//                            case DRETURN:
//                            case ARETURN:
//                                tail = tail.getPrevious().getPrevious();
//                                break;
//                        }
//                    }
//
//                    if (!areAllRemoved(head, tail)) {
//                        continue;
//                    }

                    if (h1.isCatch()) {
                        handlers.set(
                            i1,
                            ExceptionHandler.createCatch(
                                new InstructionBlock(try1.getFirstInstruction(), try2.getLastInstruction()),
                                h1.getHandlerBlock(),
                                h1.getCatchType()
                            )
                        );
                    }
                    else {
                        handlers.set(
                            i1,
                            ExceptionHandler.createFinally(
                                new InstructionBlock(try1.getFirstInstruction(), try2.getLastInstruction()),
                                h1.getHandlerBlock()
                            )
                        );
                    }

                    duplicates.set(j, handlers.get(i1));
                    duplicates.remove(j + 1);
                    handlers.remove(i2);

                    if (i2 <= i) {
                        --i;
                    }

                    --j;
                }
            }
        }
    }

    private void trimAggressiveCatchBlocks() {
        final List<ExceptionHandler> handlers = _exceptionHandlers;

    outer:
        for (int i = 0; i < handlers.size(); i++) {
            final ExceptionHandler handler = handlers.get(i);
            final InstructionBlock tryBlock = handler.getTryBlock();
            final InstructionBlock handlerBlock = handler.getHandlerBlock();

            if (!handler.isCatch()) {
                continue;
            }

            for (int j = 0; j < handlers.size(); j++) {
                if (i == j) {
                    continue;
                }

                final ExceptionHandler other = handlers.get(j);

                if (!other.isFinally()) {
                    continue;
                }

                final InstructionBlock otherTry = other.getTryBlock();
                final InstructionBlock otherHandler = other.getHandlerBlock();

                if (handlerBlock.getFirstInstruction().getOffset() < otherHandler.getFirstInstruction().getOffset() &&
                    handlerBlock.intersects(otherHandler) &&
                    !(handlerBlock.contains(otherTry) && handlerBlock.contains(otherHandler)) &&
                    !otherTry.contains(tryBlock)) {

                    handlers.set(
                        i--,
                        ExceptionHandler.createCatch(
                            tryBlock,
                            new InstructionBlock(
                                handlerBlock.getFirstInstruction(),
                                otherHandler.getFirstInstruction().getPrevious()
                            ),
                            handler.getCatchType()
                        )
                    );

                    continue outer;
                }
            }
        }
    }

    private void removeSelfHandlingFinallyHandlers() {
        final List<ExceptionHandler> handlers = _exceptionHandlers;

        //
        // Remove self-handling finally blocks.
        //

        for (int i = 0; i < handlers.size(); i++) {
            final ExceptionHandler handler = handlers.get(i);
            final InstructionBlock tryBlock = handler.getTryBlock();
            final InstructionBlock handlerBlock = handler.getHandlerBlock();

            if (handler.isFinally() &&
                handlerBlock.getFirstInstruction() == tryBlock.getFirstInstruction() &&
                tryBlock.getLastInstruction().getOffset() < handlerBlock.getLastInstruction().getEndOffset()) {

                handlers.remove(i--);
            }
        }
    }

    private void trimAggressiveFinallyBlocks() {
        final List<ExceptionHandler> handlers = _exceptionHandlers;

    outer:
        for (int i = 0; i < handlers.size(); i++) {
            final ExceptionHandler handler = handlers.get(i);
            final InstructionBlock tryBlock = handler.getTryBlock();
            final InstructionBlock handlerBlock = handler.getHandlerBlock();

            if (!handler.isFinally()) {
                continue;
            }

            for (int j = 0; j < handlers.size(); j++) {
                if (i == j) {
                    continue;
                }

                final ExceptionHandler other = handlers.get(j);

                if (!other.isCatch()) {
                    continue;
                }

                final InstructionBlock otherTry = other.getTryBlock();
                final InstructionBlock otherHandler = other.getHandlerBlock();

                if (tryBlock.getFirstInstruction() == otherTry.getFirstInstruction() &&
                    tryBlock.getLastInstruction() == otherHandler.getFirstInstruction()) {

                    handlers.set(
                        i--,
                        ExceptionHandler.createFinally(
                            new InstructionBlock(
                                tryBlock.getFirstInstruction(),
                                otherHandler.getFirstInstruction().getPrevious()
                            ),
                            handlerBlock
                        )
                    );

                    continue outer;
                }
            }
        }
    }

    private static ControlFlowNode findHandlerNode(final ControlFlowGraph cfg, final ExceptionHandler handler) {
        final List<ControlFlowNode> nodes = cfg.getNodes();

        for (int i = nodes.size() - 1; i >= 0; i--) {
            final ControlFlowNode node = nodes.get(i);

            if (node.getExceptionHandler() == handler) {
                return node;
            }
        }

        return null;
    }

    private ExceptionHandler findInnermostExceptionHandler(final int offsetInTryBlock, final ExceptionHandler exclude) {
        ExceptionHandler result = null;

        for (final ExceptionHandler handler : _exceptionHandlers) {
            if (handler == exclude) {
                continue;
            }

            final InstructionBlock tryBlock = handler.getTryBlock();

            if (tryBlock.getFirstInstruction().getOffset() <= offsetInTryBlock &&
                offsetInTryBlock < tryBlock.getLastInstruction().getEndOffset() &&
                (result == null ||
                 tryBlock.getFirstInstruction().getOffset() > result.getTryBlock().getFirstInstruction().getOffset())) {

                result = handler;
            }
        }

        return result;
    }

    private void closeTryHandlerGaps() {
        //
        // Java does this retarded thing where a try block gets split along exit branches,
        // but with the split parts sharing the same handler.  We can't represent this in
        // out AST, so just merge the parts back together.
        //

        final List<ExceptionHandler> handlers = _exceptionHandlers;

        for (int i = 0; i < handlers.size() - 1; i++) {
            final ExceptionHandler current = handlers.get(i);
            final ExceptionHandler next = handlers.get(i + 1);

            if (current.getHandlerBlock().equals(next.getHandlerBlock())) {
                final Instruction lastInCurrent = current.getTryBlock().getLastInstruction();
                final Instruction firstInNext = next.getTryBlock().getFirstInstruction();
                final Instruction branchInBetween = firstInNext.getPrevious();

                final Instruction beforeBranch;

                if (branchInBetween != null) {
                    beforeBranch = branchInBetween.getPrevious();
                }
                else {
                    beforeBranch = null;
                }

                if (branchInBetween != null &&
                    branchInBetween.getOpCode().isBranch() &&
                    (lastInCurrent == beforeBranch || lastInCurrent == branchInBetween)) {

                    final ExceptionHandler newHandler;

                    if (current.isFinally()) {
                        newHandler = ExceptionHandler.createFinally(
                            new InstructionBlock(
                                current.getTryBlock().getFirstInstruction(),
                                next.getTryBlock().getLastInstruction()
                            ),
                            new InstructionBlock(
                                current.getHandlerBlock().getFirstInstruction(),
                                current.getHandlerBlock().getLastInstruction()
                            )
                        );
                    }
                    else {
                        newHandler = ExceptionHandler.createCatch(
                            new InstructionBlock(
                                current.getTryBlock().getFirstInstruction(),
                                next.getTryBlock().getLastInstruction()
                            ),
                            new InstructionBlock(
                                current.getHandlerBlock().getFirstInstruction(),
                                current.getHandlerBlock().getLastInstruction()
                            ),
                            current.getCatchType()
                        );
                    }

                    handlers.set(i, newHandler);
                    handlers.remove(i + 1);
                    --i;
                }
            }
        }
    }

//    private void extendHandlers() {
//        final List<ExceptionHandler> handlers = _exceptionHandlers;
//
//    outer:
//        for (int i = 0; i < handlers.size(); i++) {
//            final ExceptionHandler handler = handlers.get(i);
//            final InstructionBlock tryBlock = handler.getTryBlock();
//            final InstructionBlock handlerBlock = handler.getHandlerBlock();
//
//            for (int j = 0; j < handlers.size(); j++) {
//                if (i == j) {
//                    continue;
//                }
//
//                final ExceptionHandler other = handlers.get(j);
//                final InstructionBlock otherHandler = other.getHandlerBlock();
//
//                if (handlerBlock.intersects(otherHandler) &&
//                    !handlerBlock.contains(otherHandler) &&
//                    handlerBlock.getFirstInstruction().getOffset() <= otherHandler.getFirstInstruction().getOffset()) {
//
//                    if (handler.isCatch()) {
//                        handlers.set(
//                            i--,
//                            ExceptionHandler.createCatch(
//                                tryBlock,
//                                new InstructionBlock(
//                                    handlerBlock.getFirstInstruction(),
//                                    otherHandler.getLastInstruction()
//                                ),
//                                handler.getCatchType()
//                            )
//                        );
//                    }
//                    else {
//                        handlers.set(
//                            i--,
//                            ExceptionHandler.createFinally(
//                                tryBlock,
//                                new InstructionBlock(
//                                    handlerBlock.getFirstInstruction(),
//                                    otherHandler.getLastInstruction()
//                                )
//                            )
//                        );
//                    }
//
//                    continue outer;
//                }
//            }
//        }
//    }

    private static ExceptionHandler findFirstHandler(final InstructionBlock tryBlock, final Collection<ExceptionHandler> handlers) {
        ExceptionHandler result = null;

        for (final ExceptionHandler handler : handlers) {
            if (handler.getTryBlock().equals(tryBlock) &&
                (result == null ||
                 handler.getHandlerBlock().getFirstInstruction().getOffset() < result.getHandlerBlock().getFirstInstruction().getOffset())) {

                result = handler;
            }
        }

        return result;
    }

    private static List<ExceptionHandler> findHandlers(final InstructionBlock tryBlock, final Collection<ExceptionHandler> handlers) {
        List<ExceptionHandler> result = null;

        for (final ExceptionHandler handler : handlers) {
            if (handler.getTryBlock().equals(tryBlock)) {
                if (result == null) {
                    result = new ArrayList<>();
                }

                result.add(handler);
            }
        }

        if (result == null) {
            return Collections.emptyList();
        }

        Collections.sort(
            result,
            new Comparator<ExceptionHandler>() {
                @Override
                public int compare(@NotNull final ExceptionHandler o1, @NotNull final ExceptionHandler o2) {
                    return Integer.compare(
                        o1.getHandlerBlock().getFirstInstruction().getOffset(),
                        o2.getHandlerBlock().getFirstInstruction().getOffset()
                    );
                }
            }
        );

        return result;
    }

    private static List<ExceptionHandler> findDuplicateHandlers(final ExceptionHandler handler, final Collection<ExceptionHandler> handlers) {
        final List<ExceptionHandler> result = new ArrayList<>();

        for (final ExceptionHandler other : handlers) {
            if (other.getHandlerBlock().equals(handler.getHandlerBlock())) {
                if (handler.isFinally()) {
                    if (other.isFinally()) {
                        result.add(other);
                    }
                }
                else if (other.isCatch() &&
                         MetadataHelper.isSameType(other.getCatchType(), handler.getCatchType())) {
                    result.add(other);
                }
            }
        }

        Collections.sort(
            result,
            new Comparator<ExceptionHandler>() {
                @Override
                public int compare(@NotNull final ExceptionHandler o1, @NotNull final ExceptionHandler o2) {
                    return Integer.compare(
                        o1.getTryBlock().getFirstInstruction().getOffset(),
                        o2.getTryBlock().getFirstInstruction().getOffset()
                    );
                }
            }
        );

        return result;
    }

    @SuppressWarnings("ConstantConditions")
    private List<ByteCode> performStackAnalysis() {
        final Set<ByteCode> handlerStarts = new HashSet<>();
        final Map<Instruction, ByteCode> byteCodeMap = new LinkedHashMap<>();
        final Map<Instruction, ControlFlowNode> nodeMap = new IdentityHashMap<>();
        final InstructionCollection instructions = _instructions;
        final List<ExceptionHandler> exceptionHandlers = new ArrayList<>();
        final List<ControlFlowNode> successors = new ArrayList<>();

        for (final ControlFlowNode node : _cfg.getNodes()) {
            if (node.getExceptionHandler() != null) {
                exceptionHandlers.add(node.getExceptionHandler());
            }

            if (node.getNodeType() != ControlFlowNodeType.Normal) {
                continue;
            }

            for (Instruction p = node.getStart();
                 p != null && p.getOffset() < node.getEnd().getEndOffset();
                 p = p.getNext()) {

                nodeMap.put(p, node);
            }
        }

        _exceptionHandlers.retainAll(exceptionHandlers);

        final List<ByteCode> body = new ArrayList<>(instructions.size());
        final StackMappingVisitor stackMapper = new StackMappingVisitor();
        final InstructionVisitor instructionVisitor = stackMapper.visitBody(_body);
        final StrongBox<AstCode> codeBox = new StrongBox<>();
        final StrongBox<Object> operandBox = new StrongBox<>();

        _factory = CoreMetadataFactory.make(_context.getCurrentType(), _context.getCurrentMethod());

        for (final Instruction instruction : instructions) {
            final OpCode opCode = instruction.getOpCode();

            AstCode code = CODES[opCode.ordinal()];
            Object operand = instruction.hasOperand() ? instruction.getOperand(0) : null;

            final Object secondOperand = instruction.getOperandCount() > 1 ? instruction.getOperand(1) : null;

            codeBox.set(code);
            operandBox.set(operand);

            final int offset = mappedInstruction(_originalInstructionMap, instruction).getOffset();

            if (AstCode.expandMacro(codeBox, operandBox, _body, offset)) {
                code = codeBox.get();
                operand = operandBox.get();
            }

            final ByteCode byteCode = new ByteCode();

            byteCode.instruction = instruction;
            byteCode.offset = instruction.getOffset();
            byteCode.endOffset = instruction.getEndOffset();
            byteCode.code = code;
            byteCode.operand = operand;
            byteCode.secondOperand = secondOperand;
            byteCode.popCount = InstructionHelper.getPopDelta(instruction, _body);
            byteCode.pushCount = InstructionHelper.getPushDelta(instruction, _body);

            byteCodeMap.put(instruction, byteCode);
            body.add(byteCode);
        }

        for (int i = 0, n = body.size() - 1; i < n; i++) {
            final ByteCode next = body.get(i + 1);
            final ByteCode current = body.get(i);

            current.next = next;
            next.previous = current;
        }

        final ArrayDeque<ByteCode> agenda = new ArrayDeque<>();
        final ArrayDeque<ByteCode> handlerAgenda = new ArrayDeque<>();
        final int variableCount = _body.getMaxLocals();
        final VariableSlot[] unknownVariables = VariableSlot.makeUnknownState(variableCount);
        final MethodReference method = _body.getMethod();
        final List<ParameterDefinition> parameters = method.getParameters();
        final boolean hasThis = _body.hasThis();

        if (hasThis) {
            if (method.isConstructor()) {
                unknownVariables[0] = new VariableSlot(FrameValue.UNINITIALIZED_THIS, EMPTY_DEFINITIONS);
            }
            else {
                unknownVariables[0] = new VariableSlot(FrameValue.makeReference(_context.getCurrentType()), EMPTY_DEFINITIONS);
            }
        }

        final ByteCode[] definitions = new ByteCode[] { new ByteCode() };

        for (int i = 0; i < parameters.size(); i++) {
            final ParameterDefinition parameter = parameters.get(i);
            final TypeReference parameterType = parameter.getParameterType();
            final int slot = parameter.getSlot();

            switch (parameterType.getSimpleType()) {
                case Boolean:
                case Byte:
                case Character:
                case Short:
                case Integer:
                    unknownVariables[slot] = new VariableSlot(FrameValue.INTEGER, definitions);
                    break;
                case Long:
                    unknownVariables[slot] = new VariableSlot(FrameValue.LONG, definitions);
                    unknownVariables[slot + 1] = new VariableSlot(FrameValue.TOP, definitions);
                    break;
                case Float:
                    unknownVariables[slot] = new VariableSlot(FrameValue.FLOAT, definitions);
                    break;
                case Double:
                    unknownVariables[slot] = new VariableSlot(FrameValue.DOUBLE, definitions);
                    unknownVariables[slot + 1] = new VariableSlot(FrameValue.TOP, definitions);
                    break;
                default:
                    unknownVariables[slot] = new VariableSlot(FrameValue.makeReference(parameterType), definitions);
                    break;
            }
        }

        for (final ExceptionHandler handler : exceptionHandlers) {
            final ByteCode handlerStart = byteCodeMap.get(handler.getHandlerBlock().getFirstInstruction());

            handlerStarts.add(handlerStart);

            handlerStart.stackBefore = EMPTY_STACK;
            handlerStart.variablesBefore = VariableSlot.cloneVariableState(unknownVariables);

            final ByteCode loadException = new ByteCode();
            final TypeReference catchType;

            if (handler.isFinally()) {
                catchType = _factory.makeNamedType("java.lang.Throwable");
            }
            else {
                catchType = handler.getCatchType();
            }

            loadException.code = AstCode.LoadException;
            loadException.operand = catchType;
            loadException.popCount = 0;
            loadException.pushCount = 1;

            _loadExceptions.put(handler, loadException);

            handlerStart.stackBefore = new StackSlot[] {
                new StackSlot(
                    FrameValue.makeReference(catchType),
                    new ByteCode[] { loadException }
                )
            };

            handlerAgenda.addLast(handlerStart);
        }

        body.get(0).stackBefore = EMPTY_STACK;
        body.get(0).variablesBefore = unknownVariables;

        agenda.addFirst(body.get(0));

        //
        // Process agenda.
        //
        while (!(agenda.isEmpty() && handlerAgenda.isEmpty())) {
            final ByteCode byteCode = agenda.isEmpty() ? handlerAgenda.removeFirst() : agenda.removeFirst();

            //
            // Calculate new stack.
            //

            stackMapper.visitFrame(byteCode.getFrameBefore());
            instructionVisitor.visit(byteCode.instruction);

            final StackSlot[] newStack = createModifiedStack(byteCode, stackMapper);

            //
            // Calculate new variable state.
            //

            final VariableSlot[] newVariableState = VariableSlot.cloneVariableState(byteCode.variablesBefore);
            final Map<Instruction, TypeReference> initializations = stackMapper.getInitializations();

            for (int i = 0; i < newVariableState.length; i++) {
                final VariableSlot slot = newVariableState[i];

                if (slot.isUninitialized()) {
                    final Object parameter = slot.value.getParameter();

                    if (parameter instanceof Instruction) {
                        final Instruction instruction = (Instruction) parameter;
                        final TypeReference initializedType = initializations.get(instruction);

                        if (initializedType != null) {
                            newVariableState[i] = new VariableSlot(
                                FrameValue.makeReference(initializedType),
                                slot.definitions
                            );
                        }
                    }
                }
            }

            if (byteCode.isVariableDefinition()) {
                final int slot = ((VariableReference) byteCode.operand).getSlot();

                newVariableState[slot] = new VariableSlot(
                    stackMapper.getLocalValue(slot),
                    new ByteCode[] { byteCode }
                );

                if (newVariableState[slot].value.getType().isDoubleWord()) {
                    newVariableState[slot + 1] = new VariableSlot(
                        stackMapper.getLocalValue(slot + 1),
                        new ByteCode[] { byteCode }
                    );
                }
            }

            //
            // Find all successors.
            //
            final ArrayList<ByteCode> branchTargets = new ArrayList<>();
            final ControlFlowNode node = nodeMap.get(byteCode.instruction);

            successors.clear();

            //
            // Add normal control first.
            //

            if (byteCode.instruction != node.getEnd()) {
                branchTargets.add(byteCode.next);
            }
            else {
                if (!byteCode.instruction.getOpCode().isUnconditionalBranch()) {
                    branchTargets.add(byteCode.next);
                }

                for (final ControlFlowNode successor : node.getSuccessors()) {
                    if (successor.getNodeType() == ControlFlowNodeType.Normal) {
                        successors.add(successor);
                    }
                    else if (successor.getNodeType() == ControlFlowNodeType.EndFinally) {
                        for (final ControlFlowNode s : successor.getSuccessors()) {
                            successors.add(s);
                        }
                    }
                }
            }

            //
            // Then add the exceptional control flow.
            //

            for (final ControlFlowNode successor : node.getSuccessors()) {
                if (successor.getExceptionHandler() != null) {
                    successors.add(
                        nodeMap.get(
                            successor.getExceptionHandler().getHandlerBlock().getFirstInstruction()
                        )
                    );
                }
            }

            for (final ControlFlowNode successor : successors) {
                if (successor.getNodeType() != ControlFlowNodeType.Normal) {
                    continue;
                }

                final Instruction targetInstruction = successor.getStart();
                final ByteCode target = byteCodeMap.get(targetInstruction);

                if (target.label == null) {
                    target.label = new Label();
                    target.label.setOffset(target.offset);
                    target.label.setName(target.makeLabelName());
                }

                branchTargets.add(target);
            }

            //
            // Apply the state to successors.
            //
            for (final ByteCode branchTarget : branchTargets) {
                final boolean isSubroutineJump = byteCode.code == AstCode.Jsr &&
                                                 byteCode.instruction.getOperand(0) == branchTarget.instruction;

                final StackSlot[] effectiveStack;

                if (isSubroutineJump) {
                    effectiveStack = ArrayUtilities.append(
                        newStack,
                        new StackSlot(
                            FrameValue.makeAddress(byteCode.next.instruction),
                            new ByteCode[] { byteCode }
                        )
                    );
                }
                else {
                    effectiveStack = newStack;
                }

                if (branchTarget.stackBefore == null && branchTarget.variablesBefore == null) {
//                    if (branchTargets.size() == 1) {
//                        branchTarget.stackBefore = effectiveStack;
//                        branchTarget.variablesBefore = newVariableState;
//                    }
//                    else {
                    //
                    // Do not share data for several bytecodes.
                    //
                    branchTarget.stackBefore = StackSlot.modifyStack(effectiveStack, 0, null);
                    branchTarget.variablesBefore = VariableSlot.cloneVariableState(newVariableState);
//                    }

                    agenda.push(branchTarget);
                }
                else {
                    final boolean isHandlerStart = handlerStarts.contains(branchTarget);

                    if (branchTarget.stackBefore.length != effectiveStack.length && !isHandlerStart && !isSubroutineJump) {
                        throw new IllegalStateException(
                            "Inconsistent stack size at " + branchTarget.name()
                            + " (coming from " + byteCode.name() + ")."
                        );
                    }

                    //
                    // Be careful not to change our new data; it might be reused for several branch targets.
                    // In general, be careful that two bytecodes never share data structures.
                    //

                    boolean modified = false;

                    final int stackSize = newStack.length;

                    final Frame outputFrame = createFrame(effectiveStack, newVariableState);
                    @SuppressWarnings("UnnecessaryLocalVariable")
                    final Frame inputFrame = outputFrame; //createFrame(byteCode.stackBefore, byteCode.variablesBefore);

                    final Frame nextFrame = createFrame(
                        branchTarget.stackBefore.length > stackSize ? Arrays.copyOfRange(branchTarget.stackBefore, 0, stackSize)
                                                                    : branchTarget.stackBefore,
                        branchTarget.variablesBefore
                    );

                    final Frame mergedFrame = Frame.merge(inputFrame, outputFrame, nextFrame, initializations);

                    final List<FrameValue> stack = mergedFrame.getStackValues();
                    final List<FrameValue> locals = mergedFrame.getLocalValues();

                    if (!isHandlerStart) {
                        final StackSlot[] oldStack = branchTarget.stackBefore;

                        final int oldStart = oldStack != null && oldStack.length > stackSize ? oldStack.length - 1
                                                                                             : stackSize - 1;

                        //
                        // Merge stacks; modify the target.
                        //
                        for (int i = stack.size() - 1, j = oldStart;
                             i >= 0 && j >= 0;
                             i--, j--) {

                            final FrameValue oldValue = oldStack[j].value;
                            final FrameValue newValue = stack.get(i);

                            final ByteCode[] oldDefinitions = oldStack[j].definitions;
                            final ByteCode[] newDefinitions = ArrayUtilities.union(oldDefinitions, effectiveStack[i].definitions);

                            if (!Comparer.equals(newValue, oldValue) || newDefinitions.length > oldDefinitions.length) {
                                oldStack[j] = new StackSlot(newValue, newDefinitions);
                                modified = true;
                            }
                        }
                    }

                    //
                    // Merge variables; modify the target;
                    //
                    for (int i = 0, n = locals.size(); i < n; i++) {
                        final VariableSlot oldSlot = branchTarget.variablesBefore[i];
                        final VariableSlot newSlot = newVariableState[i];

                        final FrameValue oldLocal = oldSlot.value;
                        final FrameValue newLocal = locals.get(i);

                        final ByteCode[] oldDefinitions = oldSlot.definitions;
                        final ByteCode[] newDefinitions = ArrayUtilities.union(oldSlot.definitions, newSlot.definitions);

                        if (!Comparer.equals(oldLocal, newLocal) || newDefinitions.length > oldDefinitions.length) {
                            branchTarget.variablesBefore[i] = new VariableSlot(newLocal, newDefinitions);
                            modified = true;
                        }
                    }

                    if (modified) {
                        agenda.addLast(branchTarget);
                    }
                }
            }
        }

        //
        // Occasionally, compilers or obfuscators may generate unreachable code (which might be intentionally invalid).
        // It should be safe to simply remove it.
        //

        ArrayList<ByteCode> unreachable = null;

        for (final ByteCode byteCode : body) {
            if (byteCode.stackBefore == null) {
                if (unreachable == null) {
                    unreachable = new ArrayList<>();
                }

                unreachable.add(byteCode);
            }
        }

        if (unreachable != null) {
            body.removeAll(unreachable);
        }

        //
        // Generate temporary variables to replace stack values.
        //
        for (final ByteCode byteCode : body) {
            final int popCount = byteCode.popCount != -1 ? byteCode.popCount : byteCode.stackBefore.length;

            int argumentIndex = 0;

            for (int i = byteCode.stackBefore.length - popCount; i < byteCode.stackBefore.length; i++) {
                final Variable tempVariable = new Variable();

                tempVariable.setName(format("stack_%1$02X_%2$d", byteCode.offset, argumentIndex));
                tempVariable.setGenerated(true);

                final FrameValue value = byteCode.stackBefore[i].value;

                switch (value.getType()) {
                    case Integer:
                        tempVariable.setType(BuiltinTypes.Integer);
                        break;
                    case Float:
                        tempVariable.setType(BuiltinTypes.Float);
                        break;
                    case Long:
                        tempVariable.setType(BuiltinTypes.Long);
                        break;
                    case Double:
                        tempVariable.setType(BuiltinTypes.Double);
                        break;
                    case UninitializedThis:
                        tempVariable.setType(_context.getCurrentType());
                        break;
                    case Reference:
                        TypeReference refType = (TypeReference) value.getParameter();
                        if (refType.isWildcardType()) {
                            refType = refType.hasSuperBound() ? refType.getSuperBound() : refType.getExtendsBound();
                        }
                        tempVariable.setType(refType);
                        break;
                }

                byteCode.stackBefore[i] = new StackSlot(value, byteCode.stackBefore[i].definitions, tempVariable);

                for (final ByteCode pushedBy : byteCode.stackBefore[i].definitions) {
                    if (pushedBy.storeTo == null) {
                        pushedBy.storeTo = new ArrayList<>();
                    }

                    pushedBy.storeTo.add(tempVariable);
                }

                argumentIndex++;
            }
        }

        //
        // Try to use a single temporary variable instead of several, if possible (especially useful for DUP).
        // This has to be done after all temporary variables are assigned so we know about all loads.
        //
        for (final ByteCode byteCode : body) {
            if (byteCode.storeTo != null && byteCode.storeTo.size() > 1) {
                final List<Variable> localVariables = byteCode.storeTo;

                //
                // For each of the variables, find the location where it is loaded; there should be exactly one.
                //
                List<StackSlot> loadedBy = null;

                for (final Variable local : localVariables) {
                inner:
                    for (final ByteCode bc : body) {
                        for (final StackSlot s : bc.stackBefore) {
                            if (s.loadFrom == local) {
                                if (loadedBy == null) {
                                    loadedBy = new ArrayList<>();
                                }

                                loadedBy.add(s);
                                break inner;
                            }
                        }
                    }
                }

                if (loadedBy == null) {
                    continue;
                }

                //
                // We know that all the temp variables have a single load; now make sure they have a single store.
                //
                boolean singleStore = true;
                TypeReference type = null;

                for (final StackSlot slot : loadedBy) {
                    if (slot.definitions.length != 1) {
                        singleStore = false;
                        break;
                    }
                    else if (slot.definitions[0] != byteCode) {
                        singleStore = false;
                        break;
                    }
                    else if (type == null) {
                        switch (slot.value.getType()) {
                            case Integer:
                                type = BuiltinTypes.Integer;
                                break;
                            case Float:
                                type = BuiltinTypes.Float;
                                break;
                            case Long:
                                type = BuiltinTypes.Long;
                                break;
                            case Double:
                                type = BuiltinTypes.Double;
                                break;
                            case Reference:
                                type = (TypeReference) slot.value.getParameter();
                                if (type.isWildcardType()) {
                                    type = type.hasSuperBound() ? type.getSuperBound() : type.getExtendsBound();
                                }
                                break;
                        }
                    }
                }

                if (!singleStore) {
                    continue;
                }

                //
                // We can now reduce everything into a single variable.
                //
                final Variable tempVariable = new Variable();

                tempVariable.setName(format("expr_%1$02X", byteCode.offset));
                tempVariable.setGenerated(true);
                tempVariable.setType(type);

                byteCode.storeTo = Collections.singletonList(tempVariable);

                for (final ByteCode bc : body) {
                    for (int i = 0; i < bc.stackBefore.length; i++) {
                        //
                        // Is it one of the variables we merged?
                        //
                        if (localVariables.contains(bc.stackBefore[i].loadFrom)) {
                            //
                            // Replace with the new temp variable.
                            //
                            bc.stackBefore[i] = new StackSlot(bc.stackBefore[i].value, bc.stackBefore[i].definitions, tempVariable);
                        }
                    }
                }
            }
        }

        //
        // Split and convert the normal local variables.
        //
        convertLocalVariables(definitions, body);

        //
        // Convert branch targets to labels.
        //
        for (final ByteCode byteCode : body) {
            if (byteCode.operand instanceof Instruction[]) {
                final Instruction[] branchTargets = (Instruction[]) byteCode.operand;
                final Label[] newOperand = new Label[branchTargets.length];

                for (int i = 0; i < branchTargets.length; i++) {
                    newOperand[i] = byteCodeMap.get(branchTargets[i]).label;
                }

                byteCode.operand = newOperand;
            }
            else if (byteCode.operand instanceof Instruction) {
                //noinspection SuspiciousMethodCalls
                byteCode.operand = byteCodeMap.get(byteCode.operand).label;
            }
            else if (byteCode.operand instanceof SwitchInfo) {
                final SwitchInfo switchInfo = (SwitchInfo) byteCode.operand;
                final Instruction[] branchTargets = ArrayUtilities.prepend(switchInfo.getTargets(), switchInfo.getDefaultTarget());
                final Label[] newOperand = new Label[branchTargets.length];

                for (int i = 0; i < branchTargets.length; i++) {
                    newOperand[i] = byteCodeMap.get(branchTargets[i]).label;
                }

                byteCode.operand = newOperand;
            }
        }

        return body;
    }

    private static Instruction mappedInstruction(final Map<Instruction, Instruction> map, final Instruction instruction) {
        Instruction current = instruction;
        Instruction newInstruction;

        while ((newInstruction = map.get(current)) != null) {
            if (newInstruction == current) {
                return current;
            }

            current = newInstruction;
        }

        return current;
    }

    private static StackSlot[] createModifiedStack(final ByteCode byteCode, final StackMappingVisitor stackMapper) {
        final Map<Instruction, TypeReference> initializations = stackMapper.getInitializations();
        final StackSlot[] oldStack = byteCode.stackBefore.clone();

        for (int i = 0; i < oldStack.length; i++) {
            if (oldStack[i].value.getParameter() instanceof Instruction) {
                final TypeReference initializedType = initializations.get(oldStack[i].value.getParameter());

                if (initializedType != null) {
                    oldStack[i] = new StackSlot(
                        FrameValue.makeReference(initializedType),
                        oldStack[i].definitions,
                        oldStack[i].loadFrom
                    );
                }
            }
        }

        if (byteCode.popCount == 0 && byteCode.pushCount == 0) {
            return oldStack;
        }

        switch (byteCode.code) {
            case Dup:
                return ArrayUtilities.append(
                    oldStack,
                    new StackSlot(stackMapper.getStackValue(0), oldStack[oldStack.length - 1].definitions)
                );

            case DupX1:
                return ArrayUtilities.insert(
                    oldStack,
                    oldStack.length - 2,
                    new StackSlot(stackMapper.getStackValue(0), oldStack[oldStack.length - 1].definitions)
                );

            case DupX2:
                return ArrayUtilities.insert(
                    oldStack,
                    oldStack.length - 3,
                    new StackSlot(stackMapper.getStackValue(0), oldStack[oldStack.length - 1].definitions)
                );

            case Dup2:
                return ArrayUtilities.append(
                    oldStack,
                    new StackSlot(stackMapper.getStackValue(1), oldStack[oldStack.length - 2].definitions),
                    new StackSlot(stackMapper.getStackValue(0), oldStack[oldStack.length - 1].definitions)
                );

            case Dup2X1:
                return ArrayUtilities.insert(
                    oldStack,
                    oldStack.length - 3,
                    new StackSlot(stackMapper.getStackValue(1), oldStack[oldStack.length - 2].definitions),
                    new StackSlot(stackMapper.getStackValue(0), oldStack[oldStack.length - 1].definitions)
                );

            case Dup2X2:
                return ArrayUtilities.insert(
                    oldStack,
                    oldStack.length - 4,
                    new StackSlot(stackMapper.getStackValue(1), oldStack[oldStack.length - 2].definitions),
                    new StackSlot(stackMapper.getStackValue(0), oldStack[oldStack.length - 1].definitions)
                );

            case Swap:
                final StackSlot[] newStack = new StackSlot[oldStack.length];

                ArrayUtilities.copy(oldStack, newStack);

                final StackSlot temp = newStack[oldStack.length - 1];

                newStack[oldStack.length - 1] = newStack[oldStack.length - 2];
                newStack[oldStack.length - 2] = temp;

                return newStack;

            default:
                final FrameValue[] pushValues = new FrameValue[byteCode.pushCount];

                for (int i = 0; i < byteCode.pushCount; i++) {
                    pushValues[pushValues.length - i - 1] = stackMapper.getStackValue(i);
                }

                return StackSlot.modifyStack(
                    oldStack,
                    byteCode.popCount != -1 ? byteCode.popCount : oldStack.length,
                    byteCode,
                    pushValues
                );
        }
    }

    private final static class VariableInfo {
        final int slot;
        final Variable variable;
        final List<ByteCode> definitions;
        final List<ByteCode> references;

        Range lifetime;

        VariableInfo(final int slot, final Variable variable, final List<ByteCode> definitions, final List<ByteCode> references) {
            this.slot = slot;
            this.variable = variable;
            this.definitions = definitions;
            this.references = references;
        }

        void recomputeLifetime() {
            int start = Integer.MAX_VALUE;
            int end = Integer.MIN_VALUE;

            for (final ByteCode d : definitions) {
                start = Math.min(d.offset, start);
                end = Math.max(d.offset, end);
            }

            for (final ByteCode r : references) {
                start = Math.min(r.offset, start);
                end = Math.max(r.offset, end);
            }

            lifetime = new Range(start, end);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void convertLocalVariables(final ByteCode[] parameterDefinitions, final List<ByteCode> body) {
        final MethodDefinition method = _context.getCurrentMethod();
        final List<ParameterDefinition> parameters = method.getParameters();
        final VariableDefinitionCollection variables = _body.getVariables();
        final ParameterDefinition[] parameterMap = new ParameterDefinition[_body.getMaxLocals()];

        final boolean hasThis = _body.hasThis();

        if (hasThis) {
            parameterMap[0] = _body.getThisParameter();
        }

        for (final ParameterDefinition parameter : parameters) {
            parameterMap[parameter.getSlot()] = parameter;
        }

        final Set<Pair<Integer, JvmType>> undefinedSlots = new HashSet<>();
        final List<VariableReference> varReferences = new ArrayList<>();
        final Map<String, VariableDefinition> lookup = makeVariableLookup(variables);

        for (final VariableDefinition variableDefinition : variables) {
            varReferences.add(variableDefinition);
        }

        for (final ByteCode b : body) {
            if (b.operand instanceof VariableReference && !(b.operand instanceof VariableDefinition)) {
                final VariableReference reference = (VariableReference) b.operand;

                if (undefinedSlots.add(Pair.create(reference.getSlot(), getStackType(reference.getVariableType())))) {
                    varReferences.add(reference);
                }
            }
        }

        for (final VariableReference vRef : varReferences) {
            //
            // Find all definitions of and references to this variable.
            //

            final int slot = vRef.getSlot();

            final List<ByteCode> definitions = new ArrayList<>();
            final List<ByteCode> references = new ArrayList<>();

            final VariableDefinition vDef = vRef instanceof VariableDefinition ? lookup.get(key((VariableDefinition) vRef))
                                                                               : null;

            for (final ByteCode b : body) {
                if (vDef != null) {
                    if (b.operand instanceof VariableDefinition &&
                        lookup.get(key((VariableDefinition) b.operand)) == vDef) {

                        if (b.isVariableDefinition()) {
                            definitions.add(b);
                        }
                        else {
                            references.add(b);
                        }
                    }
                }
                else if (b.operand instanceof VariableReference &&
                         variablesMatch(vRef, (VariableReference) b.operand)) {

                    if (b.isVariableDefinition()) {
                        definitions.add(b);
                    }
                    else {
                        references.add(b);
                    }
                }
            }

            List<VariableInfo> newVariables;
//            boolean fromUnknownDefinition = false;
//
//            if (_optimize) {
//                for (final ByteCode b : references) {
//                    if (b.variablesBefore[slot].isUninitialized()) {
//                        fromUnknownDefinition = true;
//                        break;
//                    }
//                }
//            }

            final ParameterDefinition parameter = parameterMap[slot];

            /*if (parameter != null) {
                final Variable variable = new Variable();

                variable.setName(
                    StringUtilities.isNullOrEmpty(parameter.getName()) ? "p" + parameter.getPosition()
                                                                       : parameter.getName()
                );

                variable.setType(parameter.getParameterType());
                variable.setOriginalParameter(parameter);

                final VariableInfo variableInfo = new VariableInfo(variable, parameterDefinitions, references);

                newVariables = Collections.singletonList(variableInfo);
            }
            else*/
            if (!_optimize/* || fromUnknownDefinition*/) {
                newVariables = processVariableUnoptimized(method, slot, definitions, references, vDef);
            }
            else {
                newVariables = new ArrayList<>();

                boolean parameterVariableAdded = false;
                VariableInfo parameterVariable = null;

                if (parameter != null) {
                    final Variable variable = new Variable();

                    variable.setName(
                        StringUtilities.isNullOrEmpty(parameter.getName()) ? "p" + parameter.getPosition()
                                                                           : parameter.getName()
                    );

                    variable.setType(parameter.getParameterType());
                    variable.setOriginalParameter(parameter);
                    variable.setOriginalVariable(vDef);

                    parameterVariable = new VariableInfo(
                        slot,
                        variable,
                        new ArrayList<ByteCode>(),
                        new ArrayList<ByteCode>()
                    );

                    Collections.addAll(parameterVariable.definitions, parameterDefinitions);
                }

                for (final ByteCode b : definitions) {
                    final FrameValue stackValue;
                    final TypeReference variableType;

                    if (b.code == AstCode.Inc) {
                        stackValue = FrameValue.INTEGER;
                    }
                    else {
                        stackValue = b.stackBefore[b.stackBefore.length - b.popCount].value;
                    }

                    if (vDef != null && vDef.isFromMetadata()) {
                        variableType = vDef.getVariableType();
                    }
                    else {
                        switch (stackValue.getType()) {
                            case Integer:
                                variableType = BuiltinTypes.Integer;
                                break;
                            case Float:
                                variableType = BuiltinTypes.Float;
                                break;
                            case Long:
                                variableType = BuiltinTypes.Long;
                                break;
                            case Double:
                                variableType = BuiltinTypes.Double;
                                break;
                            case UninitializedThis:
                                variableType = _context.getCurrentType();
                                break;
                            case Reference:
                                variableType = (TypeReference) stackValue.getParameter();
                                break;
                            case Address:
                                variableType = BuiltinTypes.Integer;
                                break;
                            case Null:
                                variableType = BuiltinTypes.Null;
                                break;
                            default:
                                if (vDef != null) {
                                    variableType = vDef.getVariableType();
                                }
                                else {
                                    variableType = BuiltinTypes.Object;
                                }
                                break;
                        }
                    }

                    if (parameterVariable != null) {
                        final boolean useParameter;

                        if (variableType.isPrimitive() || parameterVariable.variable.getType().isPrimitive()) {
                            useParameter = variableType.getSimpleType() == parameterVariable.variable.getType().getSimpleType();
                        }
                        else {
                            useParameter = MetadataHelper.isSameType(variableType, parameterVariable.variable.getType());
                        }

                        if (useParameter) {
                            if (!parameterVariableAdded) {
                                newVariables.add(parameterVariable);
                                parameterVariableAdded = true;
                            }

                            parameterVariable.definitions.add(b);
                            continue;
                        }
                    }

                    final Variable variable = new Variable();

                    if (vDef != null && !StringUtilities.isNullOrEmpty(vDef.getName())) {
                        variable.setName(vDef.getName());
                    }
                    else {
                        variable.setName(format("var_%1$d_%2$02X", slot, b.offset));
                    }

                    variable.setType(variableType);

                    if (vDef == null) {
                        variable.setOriginalVariable(new VariableDefinition(slot, variable.getName(), method, variable.getType()));
                    }
                    else {
                        variable.setOriginalVariable(vDef);
                    }

                    variable.setGenerated(false);

                    final VariableInfo variableInfo = new VariableInfo(
                        slot,
                        variable,
                        new ArrayList<ByteCode>(),
                        new ArrayList<ByteCode>()
                    );

                    variableInfo.definitions.add(b);
                    newVariables.add(variableInfo);
                }

                //
                // Add loads to the data structure; merge variables if necessary.
                //
                for (final ByteCode ref : references) {
                    final ByteCode[] refDefinitions = ref.variablesBefore[slot].definitions;

                    if (refDefinitions.length == 0 && parameterVariable != null) {
                        parameterVariable.references.add(ref);

                        if (!parameterVariableAdded) {
                            newVariables.add(parameterVariable);
                            parameterVariableAdded = true;
                        }
                    }
                    else if (refDefinitions.length == 1) {
                        VariableInfo newVariable = null;

                        for (final VariableInfo v : newVariables) {
                            if (v.definitions.contains(refDefinitions[0])) {
                                newVariable = v;
                                break;
                            }
                        }

                        if (newVariable == null && parameterVariable != null) {
                            newVariable = parameterVariable;

                            if (!parameterVariableAdded) {
                                newVariables.add(parameterVariable);
                                parameterVariableAdded = true;
                            }
                        }

                        assert newVariable != null;

                        newVariable.references.add(ref);
                    }
                    else {
                        final ArrayList<VariableInfo> mergeVariables = new ArrayList<>();

                        for (final VariableInfo v : newVariables) {
                            boolean hasIntersection = false;

                        outer:
                            for (final ByteCode b1 : v.definitions) {
                                for (final ByteCode b2 : refDefinitions) {
                                    if (b1 == b2) {
                                        hasIntersection = true;
                                        break outer;
                                    }
                                }
                            }

                            if (hasIntersection) {
                                mergeVariables.add(v);
                            }
                        }

//                        if (!mergeVariables.isEmpty()) {
//                            for (final VariableInfo v : newVariables) {
//                                if (!mergeVariables.contains(v) &&
//                                    MetadataHelper.isSameType(mergeVariables.get(0).variable.getType(), v.variable.getType())) {
//
//                                    mergeVariables.add(v);
//                                }
//                            }
//                        }

                        final ArrayList<ByteCode> mergedDefinitions = new ArrayList<>();
                        final ArrayList<ByteCode> mergedReferences = new ArrayList<>();

                        if (parameterVariable != null &&
                            (mergeVariables.isEmpty() ||
                             (!mergeVariables.contains(parameterVariable) &&
                              ArrayUtilities.contains(refDefinitions, parameterDefinitions[0])))) {

                            mergeVariables.add(parameterVariable);
                            parameterVariableAdded = true;
                        }

                        for (final VariableInfo v : mergeVariables) {
                            mergedDefinitions.addAll(v.definitions);
                            mergedReferences.addAll(v.references);
                        }

                        if (mergeVariables.isEmpty()) {
                            //
                            // TODO: Figure out why this happens.
                            //
                            newVariables = processVariableUnoptimized(method, slot, definitions, references, vDef);
                        }
                        else {
                            final VariableInfo mergedVariable = new VariableInfo(
                                slot,
                                mergeVariables.get(0).variable,
                                mergedDefinitions,
                                mergedReferences
                            );

                            if (parameterVariable != null && mergeVariables.contains(parameterVariable)) {
                                parameterVariable = mergedVariable;
                                parameterVariable.variable.setOriginalParameter(parameter);
                                parameterVariableAdded = true;
                            }

                            mergedVariable.variable.setType(mergeVariableType(mergeVariables));
                            mergedVariable.references.add(ref);

                            newVariables.removeAll(mergeVariables);
                            newVariables.add(mergedVariable);
                        }
                    }
                }
            }

            if (_context.getSettings().getMergeVariables()) {
                //
                // Experiment: attempt to reduce the number of disjoint variables in the absence of debug info by
                // merging primitive variables with adjacent lifespans and matching types.
                //

                for (final VariableInfo variable : newVariables) {
                    variable.recomputeLifetime();
                }

                Collections.sort(
                    newVariables,
                    new Comparator<VariableInfo>() {
                        @Override
                        public int compare(@NotNull final VariableInfo o1, @NotNull final VariableInfo o2) {
                            return o1.lifetime.compareTo(o2.lifetime);
                        }
                    }
                );

            outer:
                for (int j = 0; j < newVariables.size() - 1; j++) {
                    final VariableInfo prev = newVariables.get(j);

                    if (!prev.variable.getType().isPrimitive() ||
                        prev.variable.getOriginalVariable() != null && prev.variable.getOriginalVariable().isFromMetadata()) {

                        continue;
                    }

                    for (int k = j + 1; k < newVariables.size(); k++) {
                        final VariableInfo next = newVariables.get(k);

                        if (next.variable.getOriginalVariable().isFromMetadata() ||
                            !MetadataHelper.isSameType(prev.variable.getType(), next.variable.getType()) ||
                            mightBeBoolean(prev) != mightBeBoolean(next)) {

                            continue outer;
                        }

                        prev.definitions.addAll(next.definitions);
                        prev.references.addAll(next.references);

                        newVariables.remove(k--);

                        prev.lifetime.setStart(Math.min(prev.lifetime.getStart(), next.lifetime.getStart()));
                        prev.lifetime.setEnd(Math.max(prev.lifetime.getEnd(), next.lifetime.getEnd()));
                    }
                }
            }

            //
            // Set bytecode operands.
            //
            for (final VariableInfo newVariable : newVariables) {
                if (newVariable.variable.getType() == BuiltinTypes.Null) {
                    newVariable.variable.setType(BuiltinTypes.Null);
                }

                for (final ByteCode definition : newVariable.definitions) {
                    definition.operand = newVariable.variable;
                }

                for (final ByteCode reference : newVariable.references) {
                    reference.operand = newVariable.variable;
                }
            }
        }
    }

    private List<VariableInfo> processVariableUnoptimized(
        final MethodDefinition method,
        final int slot,
        final List<ByteCode> definitions,
        final List<ByteCode> references,
        final VariableDefinition vDef) {

        final Variable variable = new Variable();

        if (vDef != null) {
            variable.setType(vDef.getVariableType());

            variable.setName(
                StringUtilities.isNullOrEmpty(vDef.getName()) ? "var_" + slot
                                                              : vDef.getName()
            );
        }
        else {
            variable.setName("var_" + slot);

            for (final ByteCode b : definitions) {
                final FrameValue stackValue = b.stackBefore[b.stackBefore.length - b.popCount].value;

                if (stackValue != FrameValue.UNINITIALIZED &&
                    stackValue != FrameValue.UNINITIALIZED_THIS) {

                    final TypeReference variableType;

                    switch (stackValue.getType()) {
                        case Integer:
                            variableType = BuiltinTypes.Integer;
                            break;
                        case Float:
                            variableType = BuiltinTypes.Float;
                            break;
                        case Long:
                            variableType = BuiltinTypes.Long;
                            break;
                        case Double:
                            variableType = BuiltinTypes.Double;
                            break;
                        case Uninitialized:
                            if (stackValue.getParameter() instanceof Instruction &&
                                ((Instruction) stackValue.getParameter()).getOpCode() == OpCode.NEW) {

                                variableType = ((Instruction) stackValue.getParameter()).getOperand(0);
                            }
                            else if (vDef != null) {
                                variableType = vDef.getVariableType();
                            }
                            else {
                                variableType = BuiltinTypes.Object;
                            }
                            break;
                        case UninitializedThis:
                            variableType = _context.getCurrentType();
                            break;
                        case Reference:
                            variableType = (TypeReference) stackValue.getParameter();
                            break;
                        case Address:
                            variableType = BuiltinTypes.Integer;
                            break;
                        case Null:
                            variableType = BuiltinTypes.Null;
                            break;
                        default:
                            if (vDef != null) {
                                variableType = vDef.getVariableType();
                            }
                            else {
                                variableType = BuiltinTypes.Object;
                            }
                            break;
                    }

                    variable.setType(variableType);
                    break;
                }
            }

            if (variable.getType() == null) {
                variable.setType(BuiltinTypes.Object);
            }
        }

        if (vDef == null) {
            variable.setOriginalVariable(new VariableDefinition(slot, variable.getName(), method, variable.getType()));
        }
        else {
            variable.setOriginalVariable(vDef);
        }

        variable.setGenerated(false);

        final VariableInfo variableInfo = new VariableInfo(slot, variable, definitions, references);

        return Collections.singletonList(variableInfo);
    }

    private boolean mightBeBoolean(final VariableInfo info) {
        //
        // Perform (limited) analysis to determine if a variable might be a boolean, in which
        // case we may not merge it with another variable which is suspected to not be a boolean.
        //

        final TypeReference type = info.variable.getType();

        if (type == BuiltinTypes.Boolean) {
            return true;
        }

        if (type != BuiltinTypes.Integer) {
            return false;
        }

        for (final ByteCode b : info.definitions) {
            if (b.code != AstCode.Store || b.stackBefore.length < 1) {
                return false;
            }

            final StackSlot value = b.stackBefore[b.stackBefore.length - 1];

            for (final ByteCode d : value.definitions) {
                switch (d.code) {
                    case LdC: {
                        if (!Objects.equals(d.operand, 0) && !Objects.equals(d.operand, 1)) {
                            return false;
                        }
                        break;
                    }

                    case GetField:
                    case GetStatic: {
                        if (((FieldReference) d.operand).getFieldType() != BuiltinTypes.Boolean) {
                            return false;
                        }
                        break;
                    }

                    case LoadElement: {
                        if (d.instruction.getOpCode() != OpCode.BALOAD) {
                            return false;
                        }
                        break;
                    }

                    case InvokeVirtual:
                    case InvokeSpecial:
                    case InvokeStatic:
                    case InvokeInterface: {
                        if (((MethodReference) d.operand).getReturnType() != BuiltinTypes.Boolean) {
                            return false;
                        }
                        break;
                    }

                    default: {
                        return false;
                    }
                }
            }
        }

        for (final ByteCode r : info.references) {
            if (r.code == AstCode.Inc) {
                return false;
            }
        }

        return true;
    }

    private TypeReference mergeVariableType(final List<VariableInfo> info) {
        TypeReference result = first(info).variable.getType();

        for (int i = 0; i < info.size(); i++) {
            final VariableInfo variableInfo = info.get(i);
            final TypeReference t = variableInfo.variable.getType();

            if (result == BuiltinTypes.Null) {
                result = t;
            }
            else if (t == BuiltinTypes.Null) {
                //noinspection UnnecessaryContinue
                continue;
            }
            else {
                result = MetadataHelper.findCommonSuperType(result, t);
            }
        }

        return result != null ? result : BuiltinTypes.Object;
    }

    private JvmType getStackType(final TypeReference type) {
        final JvmType t = type.getSimpleType();

        switch (t) {
            case Boolean:
            case Byte:
            case Character:
            case Short:
            case Integer:
                return JvmType.Integer;

            case Long:
            case Float:
            case Double:
                return t;

            default:
                return JvmType.Object;
        }
    }

    private boolean variablesMatch(final VariableReference v1, final VariableReference v2) {
        if (v1.getSlot() == v2.getSlot()) {
            final JvmType t1 = getStackType(v1.getVariableType());
            final JvmType t2 = getStackType(v2.getVariableType());

            return t1 == t2;
        }
        return false;
    }

    private static Map<String, VariableDefinition> makeVariableLookup(final VariableDefinitionCollection variables) {
        final Map<String, VariableDefinition> lookup = new HashMap<>();

        for (final VariableDefinition variable : variables) {
            final String key = key(variable);

            if (lookup.containsKey(key)) {
                continue;
            }

            lookup.put(key, variable);
        }

        return lookup;
    }

    private static String key(final VariableDefinition variable) {
        final StringBuilder sb = new StringBuilder().append(variable.getSlot()).append(':');

        if (variable.hasName()) {
            sb.append(variable.getName());
        }
        else {
            sb.append("#unnamed_")
              .append(variable.getScopeStart())
              .append('_')
              .append(variable.getScopeEnd());
        }

        return sb.append(':')
                 .append(variable.getVariableType().getSignature())
                 .toString();
    }

    @SuppressWarnings("ConstantConditions")
    private List<Node> convertToAst(
        final List<ByteCode> body,
        final Set<ExceptionHandler> exceptionHandlers,
        final int startIndex,
        final MutableInteger endIndex) {

        final ArrayList<Node> ast = new ArrayList<>();

        int headStartIndex = startIndex;
        int tailStartIndex = startIndex;

        final MutableInteger tempIndex = new MutableInteger();

        while (!exceptionHandlers.isEmpty()) {
            final TryCatchBlock tryCatchBlock = new TryCatchBlock();
            final int minTryStart = body.get(headStartIndex).offset;

            //
            // Find the first and widest scope;
            //

            int tryStart = Integer.MAX_VALUE;
            int tryEnd = -1;
            int firstHandlerStart = -1;

            headStartIndex = tailStartIndex;

            for (final ExceptionHandler handler : exceptionHandlers) {
                final int start = handler.getTryBlock().getFirstInstruction().getOffset();

                if (start < tryStart && start >= minTryStart) {
                    tryStart = start;
                }
            }

            for (final ExceptionHandler handler : exceptionHandlers) {
                final int start = handler.getTryBlock().getFirstInstruction().getOffset();

                if (start == tryStart) {
                    final Instruction lastInstruction = handler.getTryBlock().getLastInstruction();
                    final int end = lastInstruction.getEndOffset();

                    if (end > tryEnd) {
                        tryEnd = end;

                        final int handlerStart = handler.getHandlerBlock().getFirstInstruction().getOffset();

                        if (firstHandlerStart < 0 || handlerStart < firstHandlerStart) {
                            firstHandlerStart = handlerStart;
                        }
                    }
                }
            }

            final ArrayList<ExceptionHandler> handlers = new ArrayList<>();

            for (final ExceptionHandler handler : exceptionHandlers) {
                final int start = handler.getTryBlock().getFirstInstruction().getOffset();
                final int end = handler.getTryBlock().getLastInstruction().getEndOffset();

                if (start == tryStart && end == tryEnd) {
                    handlers.add(handler);
                }
            }

            Collections.sort(
                handlers,
                new Comparator<ExceptionHandler>() {
                    @Override
                    public int compare(@NotNull final ExceptionHandler o1, @NotNull final ExceptionHandler o2) {
                        return Integer.compare(
                            o1.getTryBlock().getFirstInstruction().getOffset(),
                            o2.getTryBlock().getFirstInstruction().getOffset()
                        );
                    }
                }
            );

            //
            // Remember that any part of the body might have been removed due to unreachability.
            //

            //
            // Cut all instructions up to the try block.
            //
            int tryStartIndex = 0;

            while (tryStartIndex < body.size() &&
                   body.get(tryStartIndex).offset < tryStart) {

                tryStartIndex++;
            }

            if (headStartIndex < tryStartIndex) {
                ast.addAll(convertToAst(body.subList(headStartIndex, tryStartIndex)));
            }

            //
            // Cut the try block.
            //
            {
                final Set<ExceptionHandler> nestedHandlers = new LinkedHashSet<>();

                for (final ExceptionHandler eh : exceptionHandlers) {
                    final int ts = eh.getTryBlock().getFirstInstruction().getOffset();
                    final int te = eh.getTryBlock().getLastInstruction().getEndOffset();

                    if (tryStart < ts && te <= tryEnd || tryStart <= ts && te < tryEnd) {
                        nestedHandlers.add(eh);
                    }
                }

                exceptionHandlers.removeAll(nestedHandlers);

                int tryEndIndex = 0;

                while (tryEndIndex < body.size() && body.get(tryEndIndex).offset < tryEnd) {
                    tryEndIndex++;
                }

                final Block tryBlock = new Block();
/*
                for (int i = 0; i < tryEndIndex; i++) {
                    body.remove(0);
                }
*/

                tempIndex.setValue(tryEndIndex);

                final List<Node> tryAst = convertToAst(body, nestedHandlers, tryStartIndex, tempIndex);

                if (tempIndex.getValue() > tailStartIndex) {
                    tailStartIndex = tempIndex.getValue();
                }

                final Node lastInTry = lastOrDefault(tryAst, NOT_A_LABEL_OR_NOP);

                if (lastInTry == null || !lastInTry.isUnconditionalControlFlow()) {
                    tryAst.add(new Expression(AstCode.Leave, null, Expression.MYSTERY_OFFSET));
                }

                tryBlock.getBody().addAll(tryAst);
                tryCatchBlock.setTryBlock(tryBlock);
                tailStartIndex = Math.max(tryEndIndex, tailStartIndex);
            }

            //
            // Cut from the end of the try to the beginning of the first handler.
            //

/*
            while (!body.isEmpty() && body.get(0).offset < firstHandlerStart) {
                body.remove(0);
            }
*/

            //
            // Cut all handlers.
            //
        HandlerLoop:
            for (int i = 0, n = handlers.size(); i < n; i++) {
                final ExceptionHandler eh = handlers.get(i);
                final TypeReference catchType = eh.getCatchType();
                final InstructionBlock handlerBlock = eh.getHandlerBlock();

                final int handlerStart = handlerBlock.getFirstInstruction().getOffset();

                final int handlerEnd = handlerBlock.getLastInstruction() != null
                                       ? handlerBlock.getLastInstruction().getEndOffset()
                                       : _body.getCodeSize();

                int handlersStartIndex = tailStartIndex;

                while (handlersStartIndex < body.size() &&
                       body.get(handlersStartIndex).offset < handlerStart) {

                    handlersStartIndex++;
                }

                int handlersEndIndex = handlersStartIndex;

                while (handlersEndIndex < body.size() &&
                       body.get(handlersEndIndex).offset < handlerEnd) {

                    handlersEndIndex++;
                }

                tailStartIndex = Math.max(tailStartIndex, handlersEndIndex);

                if (eh.isCatch()) {
                    //
                    // See if we share a block with another handler; if so, add our catch type and move on.
                    //
                    for (final CatchBlock catchBlock : tryCatchBlock.getCatchBlocks()) {
                        final Expression firstExpression = firstOrDefault(
                            catchBlock.getSelfAndChildrenRecursive(Expression.class),
                            new Predicate<Expression>() {
                                @Override
                                public boolean test(final Expression e) {
                                    return !e.getRanges().isEmpty();
                                }
                            }
                        );

                        if (firstExpression == null) {
                            continue;
                        }

                        final int otherHandlerStart = firstExpression.getRanges().get(0).getStart();

                        if (otherHandlerStart == handlerStart) {
                            catchBlock.getCaughtTypes().add(catchType);

                            final TypeReference commonCatchType = MetadataHelper.findCommonSuperType(
                                catchBlock.getExceptionType(),
                                catchType
                            );

                            catchBlock.setExceptionType(commonCatchType);

                            if (catchBlock.getExceptionVariable() == null) {
                                updateExceptionVariable(catchBlock, eh);
                            }

                            continue HandlerLoop;
                        }
                    }
                }

                final Set<ExceptionHandler> nestedHandlers = new LinkedHashSet<>();

                for (final ExceptionHandler e : exceptionHandlers) {
                    final int ts = e.getTryBlock().getFirstInstruction().getOffset();
                    final int te = e.getTryBlock().getLastInstruction().getOffset();

                    if (ts == tryStart && te == tryEnd || e == eh) {
                        continue;
                    }

                    if (handlerStart <= ts && te < handlerEnd) {
                        nestedHandlers.add(e);

                        final int nestedEndIndex = firstIndexWhere(
                            body,
                            new Predicate<ByteCode>() {
                                @Override
                                public boolean test(final ByteCode code) {
                                    return code.instruction == e.getHandlerBlock().getLastInstruction();
                                }
                            }
                        );

                        if (nestedEndIndex > handlersEndIndex) {
                            handlersEndIndex = nestedEndIndex;
                        }
                    }
                }

                tailStartIndex = Math.max(tailStartIndex, handlersEndIndex);
                exceptionHandlers.removeAll(nestedHandlers);

                tempIndex.setValue(handlersEndIndex);

                final List<Node> handlerAst = convertToAst(body, nestedHandlers, handlersStartIndex, tempIndex);
                final Node lastInHandler = lastOrDefault(handlerAst, NOT_A_LABEL_OR_NOP);

                if (tempIndex.getValue() > tailStartIndex) {
                    tailStartIndex = tempIndex.getValue();
                }

                if (lastInHandler == null || !lastInHandler.isUnconditionalControlFlow()) {
                    handlerAst.add(new Expression(eh.isCatch() ? AstCode.Leave : AstCode.EndFinally, null, Expression.MYSTERY_OFFSET));
                }

                if (eh.isCatch()) {
                    final CatchBlock catchBlock = new CatchBlock();

                    catchBlock.setExceptionType(catchType);
                    catchBlock.getCaughtTypes().add(catchType);
                    catchBlock.getBody().addAll(handlerAst);

                    updateExceptionVariable(catchBlock, eh);

                    tryCatchBlock.getCatchBlocks().add(catchBlock);
                }
                else if (eh.isFinally()) {
                    final ByteCode loadException = _loadExceptions.get(eh);
                    final Block finallyBlock = new Block();

                    finallyBlock.getBody().addAll(handlerAst);
                    tryCatchBlock.setFinallyBlock(finallyBlock);

                    final Variable exceptionTemp = new Variable();

                    exceptionTemp.setName(format("ex_%1$02X", handlerStart));
                    exceptionTemp.setGenerated(true);

                    if (loadException == null || loadException.storeTo == null) {
                        final Expression finallyStart = firstOrDefault(finallyBlock.getSelfAndChildrenRecursive(Expression.class));

                        if (match(finallyStart, AstCode.Store)) {
                            finallyStart.getArguments().set(
                                0,
                                new Expression(AstCode.Load, exceptionTemp, Expression.MYSTERY_OFFSET)
                            );
                        }
                    }
                    else {
                        for (final Variable storeTo : loadException.storeTo) {
                            finallyBlock.getBody().add(
                                0,
                                new Expression(AstCode.Store, storeTo, Expression.MYSTERY_OFFSET, new Expression(AstCode.Load, exceptionTemp, Expression.MYSTERY_OFFSET))
                            );
                        }
                    }

                    finallyBlock.getBody().add(
                        0,
                        new Expression(
                            AstCode.Store,
                            exceptionTemp,
                            Expression.MYSTERY_OFFSET,
                            new Expression(
                                AstCode.LoadException,
                                _factory.makeNamedType("java.lang.Throwable"),
                                Expression.MYSTERY_OFFSET
                            )
                        )
                    );
                }
            }

            exceptionHandlers.removeAll(handlers);

            final Expression first;
            final Expression last;

            first = firstOrDefault(tryCatchBlock.getTryBlock().getSelfAndChildrenRecursive(Expression.class));

            if (!tryCatchBlock.getCatchBlocks().isEmpty()) {
                final CatchBlock lastCatch = lastOrDefault(tryCatchBlock.getCatchBlocks());
                if (lastCatch == null) {
                    last = null;
                }
                else {
                    last = lastOrDefault(lastCatch.getSelfAndChildrenRecursive(Expression.class));
                }
            }
            else {
                final Block finallyBlock = tryCatchBlock.getFinallyBlock();
                if (finallyBlock == null) {
                    last = null;
                }
                else {
                    last = lastOrDefault(finallyBlock.getSelfAndChildrenRecursive(Expression.class));
                }
            }

            if (first == null && last == null) {
                //
                // Ignore empty handlers.  These can crop up due to finally blocks which handle themselves.
                //
                continue;
            }

            ast.add(tryCatchBlock);
        }

        if (tailStartIndex < endIndex.getValue()) {
            ast.addAll(convertToAst(body.subList(tailStartIndex, endIndex.getValue())));
        }
        else {
            endIndex.setValue(tailStartIndex);
        }

        return ast;
    }

    private void updateExceptionVariable(final CatchBlock catchBlock, final ExceptionHandler handler) {
        final ByteCode loadException = _loadExceptions.get(handler);
        final int handlerStart = handler.getHandlerBlock().getFirstInstruction().getOffset();

        if (loadException.storeTo == null || loadException.storeTo.size() != 1) {
            final Variable exceptionTemp = new Variable();

            exceptionTemp.setName(format("ex_%1$02X", handlerStart));
            exceptionTemp.setGenerated(true);
            exceptionTemp.setType(catchBlock.getExceptionType());

            catchBlock.setExceptionVariable(exceptionTemp);

            if (loadException.storeTo != null) {
                for (final Variable storeTo : loadException.storeTo) {
                    catchBlock.getBody().add(
                        0,
                        new Expression(
                            AstCode.Store,
                            storeTo,
                            Expression.MYSTERY_OFFSET,
                            new Expression(AstCode.Load, exceptionTemp, Expression.MYSTERY_OFFSET)
                        )
                    );
                }
            }

            return;
        }

        final Node firstNode = firstOrDefault(
            skipWhile(
                catchBlock.getBody(),
                Predicates.<Node>instanceOf(Label.class)
            )
        );

        final StrongBox<Expression> popArgument;

        if (firstNode != null &&
            matchGetArgument(firstNode, AstCode.Pop, popArgument = new StrongBox<>()) &&
            matchLoad(popArgument.value, first(loadException.storeTo))) {

            //
            // The exception is just popped; optimize it away.
            //
            final Variable exceptionVariable = new Variable();

            exceptionVariable.setName(format("ex_%1$02X", handlerStart));
            exceptionVariable.setGenerated(true);

            catchBlock.setExceptionVariable(exceptionVariable);

            return;
        }

        catchBlock.setExceptionVariable(loadException.storeTo.get(0));
    }

    @SuppressWarnings("ConstantConditions")
    private List<Node> convertToAst(final List<ByteCode> body) {
        final ArrayList<Node> ast = new ArrayList<>();

        //
        // Convert stack-based bytecode to bytecode AST.
        //
        for (final ByteCode byteCode : body) {
            final Instruction originalInstruction = mappedInstruction(_originalInstructionMap, byteCode.instruction);
            final Range codeRange = new Range(originalInstruction.getOffset(), originalInstruction.getEndOffset());

            if (byteCode.stackBefore == null /*|| _removed.contains(byteCode.instruction)*/) {
                //
                // Unreachable code.
                //
                continue;
            }

            //
            // Include the instruction's label, if it has one.
            //
            if (byteCode.label != null) {
                ast.add(byteCode.label);
            }

            switch (byteCode.code) {
                case Dup:
                case DupX1:
                case DupX2:
                case Dup2:
                case Dup2X1:
                case Dup2X2:
                case Swap:
                    continue;
            }

            final Expression expression;

            if (_removed.contains(byteCode.instruction)) {
                expression = new Expression(AstCode.Nop, null, Expression.MYSTERY_OFFSET);
                ast.add(expression);
                continue;
            }

            expression = new Expression(byteCode.code, byteCode.operand, byteCode.offset);

            if (byteCode.code == AstCode.Inc) {
                assert byteCode.secondOperand instanceof Integer;

                expression.setCode(AstCode.Inc);
                expression.getArguments().add(new Expression(AstCode.LdC, byteCode.secondOperand, byteCode.offset));
            }
            else if (byteCode.code == AstCode.Switch) {
                expression.putUserData(AstKeys.SWITCH_INFO, byteCode.instruction.<SwitchInfo>getOperand(0));
            }

            expression.getRanges().add(codeRange);

            //
            // Reference arguments using temporary variables.
            //

            final int popCount = byteCode.popCount != -1 ? byteCode.popCount
                                                         : byteCode.stackBefore.length;

            for (int i = byteCode.stackBefore.length - popCount; i < byteCode.stackBefore.length; i++) {
                final StackSlot slot = byteCode.stackBefore[i];

                if (slot.value.getType().isDoubleWord()) {
                    i++;
                }

                expression.getArguments().add(new Expression(AstCode.Load, slot.loadFrom, byteCode.offset));
            }

            //
            // Store the result to temporary variables, if needed.
            //
            if (byteCode.storeTo == null || byteCode.storeTo.isEmpty()) {
                ast.add(expression);
            }
            else if (byteCode.storeTo.size() == 1) {
                ast.add(new Expression(AstCode.Store, byteCode.storeTo.get(0), expression.getOffset(), expression));
            }
            else {
                final Variable tempVariable = new Variable();

                tempVariable.setName(format("expr_%1$02X", byteCode.offset));
                tempVariable.setGenerated(true);

                ast.add(new Expression(AstCode.Store, tempVariable, expression.getOffset(), expression));

                for (int i = byteCode.storeTo.size() - 1; i >= 0; i--) {
                    ast.add(
                        new Expression(
                            AstCode.Store,
                            byteCode.storeTo.get(i),
                            Expression.MYSTERY_OFFSET,
                            new Expression(AstCode.Load, tempVariable, byteCode.offset)
                        )
                    );
                }
            }
        }

        return ast;
    }

    // <editor-fold defaultstate="collapsed" desc="StackSlot Class">

    private final static class StackSlot {
        final FrameValue value;
        final ByteCode[] definitions;
        final Variable loadFrom;

        public StackSlot(final FrameValue value, final ByteCode[] definitions) {
            this.value = VerifyArgument.notNull(value, "value");
            this.definitions = VerifyArgument.notNull(definitions, "definitions");
            this.loadFrom = null;
        }

        public StackSlot(final FrameValue value, final ByteCode[] definitions, final Variable loadFrom) {
            this.value = VerifyArgument.notNull(value, "value");
            this.definitions = VerifyArgument.notNull(definitions, "definitions");
            this.loadFrom = loadFrom;
        }

        public static StackSlot[] modifyStack(
            final StackSlot[] stack,
            final int popCount,
            final ByteCode pushDefinition,
            final FrameValue... pushTypes) {

            VerifyArgument.notNull(stack, "stack");
            VerifyArgument.isNonNegative(popCount, "popCount");
            VerifyArgument.noNullElements(pushTypes, "pushTypes");

            final StackSlot[] newStack = new StackSlot[stack.length - popCount + pushTypes.length];

            System.arraycopy(stack, 0, newStack, 0, stack.length - popCount);

            for (int i = stack.length - popCount, j = 0; i < newStack.length; i++, j++) {
                newStack[i] = new StackSlot(pushTypes[j], new ByteCode[] { pushDefinition });
            }

            return newStack;
        }

        @Override
        public String toString() {
            return "StackSlot(" + value + ')';
        }

        @Override
        @SuppressWarnings("CloneDoesntCallSuperClone")
        protected final StackSlot clone() {
            return new StackSlot(value, definitions.clone(), loadFrom);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="VariableSlot Class">

    private final static class VariableSlot {
        final static VariableSlot UNKNOWN_INSTANCE = new VariableSlot(FrameValue.EMPTY, EMPTY_DEFINITIONS);

        final ByteCode[] definitions;
        final FrameValue value;

        public VariableSlot(final FrameValue value, final ByteCode[] definitions) {
            this.value = VerifyArgument.notNull(value, "value");
            this.definitions = VerifyArgument.notNull(definitions, "definitions");
        }

        public static VariableSlot[] cloneVariableState(final VariableSlot[] state) {
            return state.clone();
        }

        public static VariableSlot[] makeUnknownState(final int variableCount) {
            final VariableSlot[] unknownVariableState = new VariableSlot[variableCount];

            for (int i = 0; i < variableCount; i++) {
                unknownVariableState[i] = UNKNOWN_INSTANCE;
            }

            return unknownVariableState;
        }

        public final boolean isUninitialized() {
            return value == FrameValue.UNINITIALIZED || value == FrameValue.UNINITIALIZED_THIS;
        }

        @Override
        @SuppressWarnings("CloneDoesntCallSuperClone")
        protected final VariableSlot clone() {
            return new VariableSlot(value, definitions.clone());
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ByteCode Class">

    private final static class ByteCode {
        Label label;
        Instruction instruction;
        String name;
        int offset; // NOTE: If you change 'offset', clear out 'name'.
        int endOffset;
        AstCode code;
        Object operand;
        Object secondOperand;
        int popCount = -1;
        int pushCount;
        ByteCode next;
        ByteCode previous;
        FrameValue type;
        StackSlot[] stackBefore;
        VariableSlot[] variablesBefore;
        List<Variable> storeTo;

        public final String name() {
            if (name == null) {
                name = format("#%1$04d", offset);
            }
            return name;
        }

        public final String makeLabelName() {
            return format("Label_%1$04d", offset);
        }

        public final Frame getFrameBefore() {
            return createFrame(stackBefore, variablesBefore);
        }

        public final boolean isVariableDefinition() {
            return code == AstCode.Store/* ||
                   code == AstCode.Inc*/;
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public final String toString() {
            final StringBuilder sb = new StringBuilder();

            //
            // Label
            //
            sb.append(name()).append(':');

            if (label != null) {
                sb.append('*');
            }

            //
            // Name
            //
            sb.append(' ');
            sb.append(code.getName());

            if (operand != null) {
                sb.append(' ');

                if (operand instanceof Instruction) {
                    sb.append(format("#%1$04d", ((Instruction) operand).getOffset()));
                }
                else if (operand instanceof Instruction[]) {
                    for (final Instruction instruction : (Instruction[]) operand) {
                        sb.append(format("#%1$04d", instruction.getOffset()));
                        sb.append(' ');
                    }
                }
                else if (operand instanceof Label) {
                    sb.append(((Label) operand).getName());
                }
                else if (operand instanceof Label[]) {
                    for (final Label l : (Label[]) operand) {
                        sb.append(l.getName());
                        sb.append(' ');
                    }
                }
                else if (operand instanceof VariableReference) {
                    final VariableReference variable = (VariableReference) operand;

                    if (variable.hasName()) {
                        sb.append(variable.getName());
                    }
                    else {
                        sb.append("$").append(String.valueOf(variable.getSlot()));
                    }
                }
                else {
                    sb.append(operand);
                }
            }

            if (stackBefore != null) {
                sb.append(" StackBefore={");

                for (int i = 0; i < stackBefore.length; i++) {
                    if (i != 0) {
                        sb.append(',');
                    }

                    final StackSlot slot = stackBefore[i];
                    final ByteCode[] definitions = slot.definitions;

                    for (int j = 0; j < definitions.length; j++) {
                        if (j != 0) {
                            sb.append('|');
                        }
                        sb.append(format("#%1$04d", definitions[j].offset));
                    }
                }

                sb.append('}');
            }

            if (storeTo != null && !storeTo.isEmpty()) {
                sb.append(" StoreTo={");

                for (int i = 0; i < storeTo.size(); i++) {
                    if (i != 0) {
                        sb.append(',');
                    }
                    sb.append(storeTo.get(i).getName());
                }

                sb.append('}');
            }

            if (variablesBefore != null) {
                sb.append(" VariablesBefore={");

                for (int i = 0; i < variablesBefore.length; i++) {
                    if (i != 0) {
                        sb.append(',');
                    }

                    final VariableSlot slot = variablesBefore[i];

                    if (slot.isUninitialized()) {
                        sb.append('?');
                    }
                    else {
                        final ByteCode[] definitions = slot.definitions;
                        for (int j = 0; j < definitions.length; j++) {
                            if (j != 0) {
                                sb.append('|');
                            }
                            sb.append(format("#%1$04d", definitions[j].offset));
                        }
                    }
                }

                sb.append('}');
            }

            return sb.toString();
        }
    }

    private static Frame createFrame(final StackSlot[] stack, final VariableSlot[] locals) {
        final FrameValue[] stackValues;
        final FrameValue[] variableValues;

        if (stack.length == 0) {
            stackValues = FrameValue.EMPTY_VALUES;
        }
        else {
            stackValues = new FrameValue[stack.length];

            for (int i = 0; i < stack.length; i++) {
                stackValues[i] = stack[i].value;
            }
        }
        if (locals.length == 0) {
            variableValues = FrameValue.EMPTY_VALUES;
        }
        else {
            variableValues = new FrameValue[locals.length];

            for (int i = 0; i < locals.length; i++) {
                variableValues[i] = locals[i].value;
            }
        }

        return new Frame(FrameType.New, variableValues, stackValues);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Predicates">

    private final static Predicate<Node> NOT_A_LABEL_OR_NOP = new Predicate<Node>() {
        @Override
        public boolean test(final Node node) {
            return !(node instanceof Label || match(node, AstCode.Nop));
        }
    };

    // </editor-fold>

    private final static class FinallyInlining {
        private final MethodBody _body;
        private final InstructionCollection _instructions;
        private final List<ExceptionHandler> _exceptionHandlers;
        private final Set<Instruction> _removed;
        private final Function<Instruction, Instruction> _previous;
        private final ControlFlowGraph _cfg;

        private final Map<Instruction, ControlFlowNode> _nodeMap;
        private final Map<ExceptionHandler, HandlerInfo> _handlerMap = new IdentityHashMap<>();
        private final Set<ControlFlowNode> _processedNodes = new LinkedHashSet<>();
        private final Set<ControlFlowNode> _allFinallyNodes = new LinkedHashSet<>();

        private FinallyInlining(
            final MethodBody body,
            final InstructionCollection instructions,
            final List<ExceptionHandler> handlers,
            final Set<Instruction> removedInstructions) {

            _body = body;
            _instructions = instructions;
            _exceptionHandlers = handlers;
            _removed = removedInstructions;
            _previous = new Function<Instruction, Instruction>() {
                @Override
                public Instruction apply(final Instruction i) {
                    return previous(i);
                }
            };

            preProcess();

            _cfg = ControlFlowGraphBuilder.build(instructions, handlers);
            _cfg.computeDominance();
            _cfg.computeDominanceFrontier();
            _nodeMap = createNodeMap(_cfg);

            final Set<ControlFlowNode> terminals = new HashSet<>();

            for (int i = 0; i < handlers.size(); i++) {
                final ExceptionHandler handler = handlers.get(i);
                final InstructionBlock handlerBlock = handler.getHandlerBlock();
                final ControlFlowNode handlerNode = findHandlerNode(_cfg, handler);
                final ControlFlowNode head = _nodeMap.get(handlerBlock.getFirstInstruction());
                final ControlFlowNode tryHead = _nodeMap.get(handler.getTryBlock().getFirstInstruction());

                terminals.clear();

                for (int j = 0; j < handlers.size(); j++) {
                    final ExceptionHandler otherHandler = handlers.get(j);

                    if (otherHandler.getTryBlock().equals(handler.getTryBlock())) {
                        terminals.add(findHandlerNode(_cfg, otherHandler));
                    }
                }

                final List<ControlFlowNode> tryNodes = new ArrayList<>(
                    findDominatedNodes(
                        _cfg,
                        tryHead,
                        true,
                        terminals
                    )
                );

                terminals.remove(handlerNode);

                if (handler.isFinally() && handlerNode != null) {
                    terminals.add(handlerNode.getEndFinallyNode());
                }

                final List<ControlFlowNode> handlerNodes = new ArrayList<>(
                    findDominatedNodes(
                        _cfg,
                        head,
                        true,
                        terminals
                    )
                );

                Collections.sort(tryNodes);
                Collections.sort(handlerNodes);

                final ControlFlowNode tail = last(handlerNodes);

                final HandlerInfo handlerInfo = new HandlerInfo(
                    handler,
                    handlerNode,
                    head,
                    tail,
                    tryNodes,
                    handlerNodes
                );

                _handlerMap.put(handler, handlerInfo);

                if (handler.isFinally()) {
                    _allFinallyNodes.addAll(handlerNodes);
                }

//                dumpHandlerNodes(handler, tryNodes, handlerNodes);
            }
        }

        @SuppressWarnings("UnusedDeclaration")
        private static void dumpHandlerNodes(
            final ExceptionHandler handler,
            final List<ControlFlowNode> tryNodes,
            final List<ControlFlowNode> handlerNodes) {

            final ITextOutput output = new PlainTextOutput();

            output.writeLine(handler.toString());
            output.writeLine("Try Nodes:");
            output.indent();

            for (final ControlFlowNode node : tryNodes) {
                output.writeLine(node.toString());
            }

            output.unindent();
            output.writeLine("Handler Nodes:");
            output.indent();

            for (final ControlFlowNode node : handlerNodes) {
                output.writeLine(node.toString());
            }

            output.unindent();
            output.writeLine();

            System.out.println(output);
        }

        static void run(
            final MethodBody body,
            final InstructionCollection instructions,
            final List<ExceptionHandler> handlers,
            final Set<Instruction> removedInstructions) {

            Collections.reverse(handlers);

            try {
                LOG.fine("Removing inlined `finally` code...");

                final FinallyInlining inlining = new FinallyInlining(body, instructions, handlers, removedInstructions);

                inlining.runCore();
            }
            finally {
                Collections.reverse(handlers);
            }
        }

        private void runCore() {
            final List<ExceptionHandler> handlers = _exceptionHandlers;

            if (handlers.isEmpty()) {
                return;
            }

            final List<ExceptionHandler> originalHandlers = toList(_exceptionHandlers);
            final List<ExceptionHandler> sortedHandlers = toList(originalHandlers);

            Collections.sort(
                sortedHandlers,
                new Comparator<ExceptionHandler>() {
                    @Override
                    public int compare(@NotNull final ExceptionHandler o1, @NotNull final ExceptionHandler o2) {
                        if (o1.getHandlerBlock().contains(o2.getHandlerBlock())) {
                            return -1;
                        }

                        if (o2.getHandlerBlock().contains(o1.getHandlerBlock())) {
                            return 1;
                        }

                        return Integer.compare(
                            originalHandlers.indexOf(o1),
                            originalHandlers.indexOf(o2)
                        );
                    }
                }
            );

            for (final ExceptionHandler handler : sortedHandlers) {
                if (handler.isFinally()) {
                    processFinally(handler);
                }
            }
        }

        private void processFinally(final ExceptionHandler handler) {
            final HandlerInfo handlerInfo = _handlerMap.get(handler);

            Instruction first = handlerInfo.head.getStart();
            Instruction last = handlerInfo.handler.getHandlerBlock().getLastInstruction();

            if (last.getOpCode() == OpCode.ENDFINALLY) {
                first = first.getNext();
                last = previous(last);
            }
            else {
                if (first.getOpCode().isStore() || first.getOpCode() == OpCode.POP) {
                    first = first.getNext();
                }
            }

            if (first == null || last == null) {
                return;
            }

            int instructionCount = 0;

            for (Instruction p = last; p != null && p.getOffset() >= first.getOffset(); p = previous(p)) {
                ++instructionCount;
            }

            if (instructionCount == 0 ||
                instructionCount == 1 && !_removed.contains(last) && last.getOpCode().isUnconditionalBranch()) {

                return;
            }

            final Set<ControlFlowNode> toProcess = collectNodes(handlerInfo);
            final Set<ControlFlowNode> forbiddenNodes = new LinkedHashSet<>(_allFinallyNodes);

            forbiddenNodes.removeAll(handlerInfo.tryNodes);

            _processedNodes.clear();

            processNodes(handlerInfo, first, last, instructionCount, toProcess, forbiddenNodes);
        }

        @SuppressWarnings("ConstantConditions")
        private void processNodes(
            final HandlerInfo handlerInfo,
            final Instruction first,
            final Instruction last,
            final int instructionCount,
            final Set<ControlFlowNode> toProcess,
            final Set<ControlFlowNode> forbiddenNodes) {

            final ExceptionHandler handler = handlerInfo.handler;
            final ControlFlowNode tryHead = _nodeMap.get(handler.getTryBlock().getFirstInstruction());
            final ControlFlowNode finallyTail = _nodeMap.get(handler.getHandlerBlock().getLastInstruction());
            final List<Pair<Instruction, Instruction>> startingPoints = new ArrayList<>();

        nextNode:
            for (ControlFlowNode node : toProcess) {
                final ExceptionHandler nodeHandler = node.getExceptionHandler();

                if (node.getNodeType() == ControlFlowNodeType.EndFinally) {
                    continue;
                }

                if (nodeHandler != null) {
                    node = _nodeMap.get(nodeHandler.getHandlerBlock().getLastInstruction());
                }

                if (_processedNodes.contains(node) || forbiddenNodes.contains(node)) {
                    continue;
                }

                Instruction tail = node.getEnd();
                boolean isLeave = false;
                boolean tryNext = false;
                boolean tryPrevious = false;

                if (finallyTail.getEnd().getOpCode().isReturn() ||
                    finallyTail.getEnd().getOpCode().isThrow()) {

                    isLeave = true;
                }

                if (last.getOpCode() == OpCode.GOTO || last.getOpCode() == OpCode.GOTO_W) {
                    tryNext = true;
                }

                if (tail.getOpCode().isUnconditionalBranch()) {
                    switch (tail.getOpCode()) {
                        case GOTO:
                        case GOTO_W:
                            tryPrevious = true;
                            break;

                        case RETURN:
                            tail = previous(tail);
                            tryPrevious = true;
                            break;

                        case IRETURN:
                        case LRETURN:
                        case FRETURN:
                        case DRETURN:
                        case ARETURN:
                            if (finallyTail.getEnd().getOpCode().getFlowControl() != FlowControl.Return) {
                                tail = previous(tail);
                            }
                            tryPrevious = true;
                            break;

                        case ATHROW:
                            tryNext = true;
                            tryPrevious = true;
                            break;
                    }
                }

                if (tail == null) {
                    continue;
                }

                startingPoints.add(Pair.create(last, tail));

                if (tryPrevious) {
                    startingPoints.add(Pair.create(last, previous(tail)));
                }

                if (tryNext) {
                    startingPoints.add(Pair.create(last, tail.getNext()));
                }

                boolean matchFound = false;

                for (final Pair<Instruction, Instruction> startingPoint : startingPoints) {
                    if (forbiddenNodes.contains(_nodeMap.get(startingPoint.getSecond()))) {
                        continue;
                    }

                    if (opCodesMatch(startingPoint.getFirst(), startingPoint.getSecond(), instructionCount, _previous)) {
                        tail = startingPoint.getSecond();
                        matchFound = true;
                        break;
                    }
                }

                startingPoints.clear();

                if (!matchFound) {
                    if (last.getOpCode() == OpCode.JSR) {
                        //
                        // If we failed to match against the last instruction in our 'try' block, see if our
                        // subroutine jump follows the finally block instead.  This pattern has been seen
                        // in the wild.
                        //
                        final Instruction lastInTry = handlerInfo.handler.getTryBlock().getLastInstruction();

                        if (tail == lastInTry &&
                            (lastInTry.getOpCode() == OpCode.GOTO || lastInTry.getOpCode() == OpCode.GOTO_W)) {

                            final Instruction target = lastInTry.getOperand(0);

                            if (target.getOpCode() == OpCode.JSR &&
                                target.getOperand(0) == last.getOperand(0)) {

                                target.setOpCode(OpCode.NOP);
                                target.setOperand(null);
                            }
                        }
                    }

                    continue;
                }

                if (tail.getOffset() - tryHead.getOffset() == last.getOffset() - first.getOffset() &&
                    handlerInfo.tryNodes.contains(node)) {

                    //
                    // If the try block exactly matches the finally, don't remove it.
                    //
                    continue;
                }

                for (int i = 0; i < instructionCount; i++) {
                    _removed.add(tail);
                    tail = previous(tail);
                    if (tail == null) {
                        continue nextNode;
                    }
                }

                if (isLeave) {
                    if (tail != null &&
                        tail.getOpCode().isStore() &&
                        !_body.getMethod().getReturnType().isVoid()) {

                        final Instruction load = InstructionHelper.reverseLoadOrStore(tail);
                        final Instruction returnSite = node.getEnd();
                        final Instruction loadSite = returnSite.getPrevious();

                        loadSite.setOpCode(load.getOpCode());

                        if (load.getOperandCount() == 1) {
                            loadSite.setOperand(load.getOperand(0));
                        }

                        switch (load.getOpCode().name().charAt(0)) {
                            case 'I':
                                returnSite.setOpCode(OpCode.IRETURN);
                                break;
                            case 'L':
                                returnSite.setOpCode(OpCode.LRETURN);
                                break;
                            case 'F':
                                returnSite.setOpCode(OpCode.FRETURN);
                                break;
                            case 'D':
                                returnSite.setOpCode(OpCode.DRETURN);
                                break;
                            case 'A':
                                returnSite.setOpCode(OpCode.ARETURN);
                                break;
                        }

                        returnSite.setOperand(null);

                        _removed.remove(loadSite);
                        _removed.remove(returnSite);
                    }
                    else {
                        _removed.add(node.getEnd());
                    }
                }

                _processedNodes.add(node);
            }
        }

        @SuppressWarnings("ConstantConditions")
        private Set<ControlFlowNode> collectNodes(final HandlerInfo handlerInfo) {
            final ControlFlowGraph cfg = _cfg;
            final List<ControlFlowNode> successors = new ArrayList<>();
            final Set<ControlFlowNode> toProcess = new LinkedHashSet<>();
            final ControlFlowNode endFinallyNode = handlerInfo.handlerNode.getEndFinallyNode();
            final Set<ControlFlowNode> exitOnlySuccessors = new LinkedHashSet<>();
            final InstructionBlock tryBlock = handlerInfo.handler.getTryBlock();

            if (endFinallyNode != null) {
                successors.add(handlerInfo.handlerNode);
            }

            for (final ControlFlowNode exit : cfg.getRegularExit().getPredecessors()) {
                if (exit.getNodeType() == ControlFlowNodeType.Normal &&
                    tryBlock.contains(exit.getEnd())) {

                    toProcess.add(exit);
                }
            }

            for (final ControlFlowNode exit : cfg.getExceptionalExit().getPredecessors()) {
                if (exit.getNodeType() == ControlFlowNodeType.Normal &&
                    tryBlock.contains(exit.getEnd())) {

                    toProcess.add(exit);
                }
            }

            for (int i = 0; i < successors.size(); i++) {
                final ControlFlowNode successor = successors.get(i);

                for (final ControlFlowEdge edge : successor.getIncoming()) {
                    if (edge.getSource() == successor) {
                        continue;
                    }

                    if (edge.getType() == JumpType.Normal &&
                        edge.getSource().getNodeType() == ControlFlowNodeType.Normal &&
                        !exitOnlySuccessors.contains(successor)) {

                        toProcess.add(edge.getSource());
                    }
                    else if (edge.getType() == JumpType.JumpToExceptionHandler &&
                             edge.getSource().getNodeType() == ControlFlowNodeType.Normal &&
                             (edge.getSource().getEnd().getOpCode().isThrow() ||
                              edge.getSource().getEnd().getOpCode().isReturn())) {

                        toProcess.add(edge.getSource());

                        if (exitOnlySuccessors.contains(successor)) {
                            exitOnlySuccessors.add(edge.getSource());
                        }
                    }
                    else if (edge.getSource().getNodeType() == ControlFlowNodeType.CatchHandler) {
                        final ControlFlowNode endCatch = findNode(
                            cfg,
                            edge.getSource().getExceptionHandler().getHandlerBlock().getLastInstruction()
                        );

                        if (handlerInfo.handler.getTryBlock().contains(endCatch.getEnd())) {
                            toProcess.add(endCatch);
                        }
                    }
                    else if (edge.getSource().getNodeType() == ControlFlowNodeType.FinallyHandler) {
                        successors.add(edge.getSource());
                        exitOnlySuccessors.add(edge.getSource());
                    }
                    else if (edge.getSource().getNodeType() == ControlFlowNodeType.EndFinally) {
                        successors.add(edge.getSource());

                        final HandlerInfo precedingFinally = firstOrDefault(
                            _handlerMap.values(),
                            new Predicate<HandlerInfo>() {
                                @Override
                                public boolean test(final HandlerInfo o) {
                                    return o.handlerNode.getEndFinallyNode() == edge.getSource();
                                }
                            }
                        );

                        if (precedingFinally != null) {
                            successors.add(precedingFinally.handlerNode);
                            exitOnlySuccessors.remove(precedingFinally.handlerNode);
                        }
                    }
                }
            }

            List<ControlFlowNode> finallyNodes = null;

            for (final ControlFlowNode node : toProcess) {
                if (_allFinallyNodes.contains(node)) {
                    if (finallyNodes == null) {
                        finallyNodes = new ArrayList<>();
                    }
                    finallyNodes.add(node);
                }
            }

            if (finallyNodes != null) {
                toProcess.removeAll(finallyNodes);
                toProcess.addAll(finallyNodes);
            }

            return toProcess;
        }

        private void preProcess() {
            final InstructionCollection instructions = _instructions;
            final List<ExceptionHandler> handlers = _exceptionHandlers;
            final ControlFlowGraph cfg = ControlFlowGraphBuilder.build(instructions, handlers);

            cfg.computeDominance();
            cfg.computeDominanceFrontier();

            for (int i = 0; i < handlers.size(); i++) {
                final ExceptionHandler handler = handlers.get(i);

                if (handler.isFinally()) {
                    final InstructionBlock handlerBlock = handler.getHandlerBlock();
                    final ControlFlowNode finallyHead = findNode(cfg, handler.getHandlerBlock().getFirstInstruction());

                    final List<ControlFlowNode> finallyNodes = toList(
                        findDominatedNodes(
                            cfg,
                            finallyHead,
                            true,
                            Collections.<ControlFlowNode>emptySet()
                        )
                    );

                    Collections.sort(finallyNodes);

                    final Instruction first = handlerBlock.getFirstInstruction();

                    Instruction last = last(finallyNodes).getEnd();
                    Instruction nextToLast = last.getPrevious();

                    boolean firstPass = true;

                    while (true) {
                        if (first.getOpCode().isStore() &&
                            last.getOpCode() == OpCode.ATHROW &&
                            nextToLast.getOpCode().isLoad() &&
                            InstructionHelper.getLoadOrStoreSlot(first) == InstructionHelper.getLoadOrStoreSlot(nextToLast)) {

                            nextToLast.setOpCode(OpCode.NOP);
                            nextToLast.setOperand(null);

                            _removed.add(nextToLast);

                            last.setOpCode(OpCode.ENDFINALLY);
                            last.setOperand(null);

                            break;
                        }

                        if (firstPass = !firstPass) {
                            break;
                        }

                        last = handlerBlock.getLastInstruction();
                        nextToLast = last.getPrevious();
                    }
                }
            }
        }

        private Instruction previous(final Instruction i) {
            Instruction p = i.getPrevious();

            while (p != null && _removed.contains(p)) {
                p = p.getPrevious();
            }

            return p;
        }
    }
}
