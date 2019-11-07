/*
 * AstOptimizer.java
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

import com.strobel.assembler.metadata.*;
import com.strobel.core.*;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.functions.Function;
import com.strobel.functions.Supplier;
import com.strobel.functions.Suppliers;
import com.strobel.util.ContractUtils;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.strobel.core.CollectionUtilities.*;
import static com.strobel.decompiler.ast.PatternMatching.*;

@SuppressWarnings("ConstantConditions")
public final class AstOptimizer {
    private final static Logger LOG = Logger.getLogger(AstOptimizer.class.getSimpleName());

    private int _nextLabelIndex;

    public static void optimize(final DecompilerContext context, final Block method) {
        optimize(context, method, AstOptimizationStep.None);
    }

    public static void optimize(final DecompilerContext context, final Block method, final AstOptimizationStep abortBeforeStep) {
        VerifyArgument.notNull(context, "context");
        VerifyArgument.notNull(method, "method");

        LOG.fine("Beginning bytecode AST optimization...");

        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.RemoveRedundantCode)) {
            return;
        }

        final AstOptimizer optimizer = new AstOptimizer();

        removeRedundantCode(method, context.getSettings());

        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.ReduceBranchInstructionSet)) {
            return;
        }

        introducePreIncrementOptimization(context, method);

        for (final Block block : method.getSelfAndChildrenRecursive(Block.class)) {
            reduceBranchInstructionSet(block);
        }

        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.InlineVariables)) {
            return;
        }

        final Inlining inliningPhase1 = new Inlining(context, method);

        while (inliningPhase1.inlineAllVariables()) {
            inliningPhase1.analyzeMethod();
        }

        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.CopyPropagation)) {
            return;
        }

        inliningPhase1.copyPropagation();

        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.RewriteFinallyBlocks)) {
            return;
        }

        rewriteFinallyBlocks(method);

        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.SplitToMovableBlocks)) {
            return;
        }

        for (final Block block : method.getSelfAndChildrenRecursive(Block.class)) {
            optimizer.splitToMovableBlocks(block);
        }

        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.RemoveUnreachableBlocks)) {
            return;
        }

        removeUnreachableBlocks(method);

        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.TypeInference)) {
            return;
        }

        TypeAnalysis.run(context, method);

        boolean done = false;

        LOG.fine("Performing block-level bytecode AST optimizations (enable FINER for more detail)...");

        int blockNumber = 0;

        for (final Block block : method.getSelfAndChildrenRecursive(Block.class)) {
            boolean modified;
            int blockRound = 0;

            ++blockNumber;

            do {
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("Optimizing block #" + blockNumber + ", round " + ++blockRound + "...");
                }

                modified = false;

                if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.RemoveInnerClassInitSecurityChecks)) {
                    done = true;
                    break;
                }

                modified |= runOptimization(block, new RemoveInnerClassInitSecurityChecksOptimization(context, method));

                if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.PreProcessShortCircuitAssignments)) {
                    done = true;
                    break;
                }

                modified |= runOptimization(block, new PreProcessShortCircuitAssignmentsOptimization(context, method));

                if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.SimplifyShortCircuit)) {
                    done = true;
                    break;
                }

                modified |= runOptimization(block, new SimplifyShortCircuitOptimization(context, method));

                if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.JoinBranchConditions)) {
                    done = true;
                    break;
                }

                modified |= runOptimization(block, new JoinBranchConditionsOptimization(context, method));

                if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.SimplifyTernaryOperator)) {
                    done = true;
                    break;
                }

                modified |= runOptimization(block, new SimplifyTernaryOperatorOptimization(context, method));
                modified |= runOptimization(block, new SimplifyTernaryOperatorRoundTwoOptimization(context, method));

                if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.JoinBasicBlocks)) {
                    done = true;
                    break;
                }

                modified |= runOptimization(block, new JoinBasicBlocksOptimization(context, method));

                if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.SimplifyLogicalNot)) {
                    done = true;
                    break;
                }

                modified |= runOptimization(block, new SimplifyLogicalNotOptimization(context, method));

                if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.TransformObjectInitializers)) {
                    done = true;
                    break;
                }

                modified |= runOptimization(block, new TransformObjectInitializersOptimization(context, method));

                if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.TransformArrayInitializers)) {
                    done = true;
                    break;
                }

                modified |= new Inlining(context, method, true).inlineAllInBlock(block);
                modified |= runOptimization(block, new TransformArrayInitializersOptimization(context, method));

                if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.IntroducePostIncrement)) {
                    done = true;
                    break;
                }

                modified |= runOptimization(block, new IntroducePostIncrementOptimization(context, method));

                if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.InlineConditionalAssignments)) {
                    done = true;
                    break;
                }

                modified |= runOptimization(block, new InlineConditionalAssignmentsOptimization(context, method));

                if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.MakeAssignmentExpressions)) {
                    done = true;
                    break;
                }

                modified |= runOptimization(block, new MakeAssignmentExpressionsOptimization(context, method));

                if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.InlineLambdas)) {
                    return;
                }

                modified |= runOptimization(block, new InlineLambdasOptimization(context, method));

                if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.InlineVariables2)) {
                    done = true;
                    break;
                }

                modified |= new Inlining(context, method, true).inlineAllInBlock(block);
                new Inlining(context, method).copyPropagation();

                if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.MergeDisparateObjectInitializations)) {
                    done = true;
                    break;
                }

                modified |= mergeDisparateObjectInitializations(context, block);
            }
            while (modified);
        }

        if (done) {
            return;
        }

        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.FindLoops)) {
            return;
        }

        for (final Block block : method.getSelfAndChildrenRecursive(Block.class)) {
            new LoopsAndConditions(context).findLoops(block);
        }

        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.FindConditions)) {
            return;
        }

        for (final Block block : method.getSelfAndChildrenRecursive(Block.class)) {
            new LoopsAndConditions(context).findConditions(block);
        }

        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.FlattenNestedMovableBlocks)) {
            return;
        }

        flattenBasicBlocks(method);

        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.RemoveRedundantCode2)) {
            return;
        }

        removeRedundantCode(method, context.getSettings());

        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.GotoRemoval)) {
            return;
        }

        new GotoRemoval().removeGotos(method);

        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.DuplicateReturns)) {
            return;
        }

        duplicateReturnStatements(method);

        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.ReduceIfNesting)) {
            return;
        }

        reduceIfNesting(method);

        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.GotoRemoval2)) {
            return;
        }

        new GotoRemoval().removeGotos(method);

        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.ReduceComparisonInstructionSet)) {
            return;
        }

        for (final Expression e : method.getChildrenAndSelfRecursive(Expression.class)) {
            reduceComparisonInstructionSet(e);
        }

        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.RecombineVariables)) {
            return;
        }

        recombineVariables(method);

        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.RemoveRedundantCode3)) {
            return;
        }

        GotoRemoval.removeRedundantCode(
            method,
            GotoRemoval.OPTION_MERGE_ADJACENT_LABELS |
            GotoRemoval.OPTION_REMOVE_REDUNDANT_RETURNS
        );

        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.CleanUpTryBlocks)) {
            return;
        }

        cleanUpTryBlocks(method);

        //
        // This final inlining pass is necessary because the DuplicateReturns step and the
        // introduction of ternary operators may open up additional inlining possibilities.
        //

        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.InlineVariables3)) {
            return;
        }

        final Inlining inliningPhase3 = new Inlining(context, method, true);

        inliningPhase3.inlineAllVariables();

        if (!shouldPerformStep(abortBeforeStep, AstOptimizationStep.TypeInference2)) {
            return;
        }

        TypeAnalysis.reset(context, method);
        TypeAnalysis.run(context, method);

        LOG.fine("Finished bytecode AST optimization.");
    }

    private static boolean shouldPerformStep(final AstOptimizationStep abortBeforeStep, final AstOptimizationStep nextStep) {
        if (abortBeforeStep == nextStep) {
            return false;
        }

        if (nextStep.isBlockLevelOptimization()) {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("Performing block-level optimization: " + nextStep + ".");
            }
        }
        else {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Performing optimization: " + nextStep + ".");
            }
        }

        return true;
    }

    // <editor-fold defaultstate="collapsed" desc="RemoveUnreachableBlocks Step">

    private static void removeUnreachableBlocks(final Block method) {
        final BasicBlock entryBlock = firstOrDefault(ofType(method.getBody(), BasicBlock.class));

        if (entryBlock == null) {
            return;
        }

        final Set<Label> liveLabels = new LinkedHashSet<>();

        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        final Map<BasicBlock, List<Label>> embeddedLabels = new DefaultMap<>(
            new Supplier<List<Label>>() {
                @Override
                public List<Label> get() {
                    return new ArrayList<>();
                }
            }
        );

        for (final BasicBlock basicBlock : method.getChildrenAndSelfRecursive(BasicBlock.class)) {
            for (final Label label : basicBlock.getChildrenAndSelfRecursive(Label.class)) {
                embeddedLabels.get(basicBlock).add(label);
            }
        }

        for (final Expression e : method.getChildrenAndSelfRecursive(Expression.class)) {
            if (e.getOperand() instanceof Label) {
                liveLabels.add((Label) e.getOperand());
            }
            else if (e.getOperand() instanceof Label[]) {
                Collections.addAll(liveLabels, (Label[]) e.getOperand());
            }
        }

    outer:
        for (final BasicBlock basicBlock : method.getChildrenAndSelfRecursive(BasicBlock.class)) {
            final List<Node> body = basicBlock.getBody();
            final Label entryLabel = (Label) body.get(0);

            if (basicBlock != entryBlock && !liveLabels.contains(entryLabel)) {
                for (final Label label : embeddedLabels.get(basicBlock)) {
                    if (liveLabels.contains(label)) {
                        continue outer;
                    }
                }
                while (body.size() > 1) {
                    body.remove(body.size() - 1);
                }
            }
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="CleanUpTryBlocks Step">

    private static void cleanUpTryBlocks(final Block method) {
        for (final Block block : method.getChildrenAndSelfRecursive(Block.class)) {
            final List<Node> body = block.getBody();

            for (int i = 0; i < body.size(); i++) {
                if (body.get(i) instanceof TryCatchBlock) {
                    final TryCatchBlock tryCatch = (TryCatchBlock) body.get(i);

                    if (tryCatch.getTryBlock().getBody().isEmpty()) {
                        if (tryCatch.getFinallyBlock() == null || tryCatch.getFinallyBlock().getBody().isEmpty()) {
                            body.remove(i--);
                            continue;
                        }
                    }

                    if (tryCatch.getFinallyBlock() != null &&
                        tryCatch.getCatchBlocks().isEmpty()) {

                        if (tryCatch.getTryBlock().getBody().size() == 1 &&
                            tryCatch.getTryBlock().getBody().get(0) instanceof TryCatchBlock) {

                            final TryCatchBlock innerTryCatch = (TryCatchBlock) tryCatch.getTryBlock().getBody().get(0);

                            if (innerTryCatch.getFinallyBlock() == null) {
                                tryCatch.setTryBlock(innerTryCatch.getTryBlock());
                                tryCatch.getCatchBlocks().addAll(innerTryCatch.getCatchBlocks());
                            }
                        }
//                        else if (tryCatch.getFinallyBlock().getBody().isEmpty()) {
//                            body.remove(i);
//                            body.addAll(i, tryCatch.getTryBlock().getBody());
//                        }
                    }
                }
            }
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="RewriteFinallyBlocks Step">

    private static void rewriteFinallyBlocks(final Block method) {
        rewriteSynchronized(method);

        final List<Expression> a = new ArrayList<>();
        final StrongBox<Variable> v = new StrongBox<>();

        int endFinallyCount = 0;

        for (final TryCatchBlock tryCatch : method.getChildrenAndSelfRecursive(TryCatchBlock.class)) {
            final Block finallyBlock = tryCatch.getFinallyBlock();

            if (finallyBlock == null || finallyBlock.getBody().size() < 2) {
                continue;
            }

            final List<Node> body = finallyBlock.getBody();
            final List<Variable> exceptionCopies = new ArrayList<>();
            final Node lastInFinally = last(finallyBlock.getBody());

            if (matchGetArguments(body.get(0), AstCode.Store, v, a) &&
                match(a.get(0), AstCode.LoadException)) {

                body.remove(0);
                exceptionCopies.add(v.get());

                if (body.isEmpty() || !matchLoadStore(body.get(0), v.get(), v)) {
                    v.set(null);
                }
                else {
                    exceptionCopies.add(v.get());
                }

                final Label endFinallyLabel;

                if (body.size() > 1 && body.get(body.size() - 2) instanceof Label) {
                    endFinallyLabel = (Label) body.get(body.size() - 2);
                }
                else {
                    endFinallyLabel = new Label();
                    endFinallyLabel.setName("EndFinally_" + endFinallyCount++);

                    body.add(body.size() - 1, endFinallyLabel);
                }

                for (final Block b : finallyBlock.getSelfAndChildrenRecursive(Block.class)) {
                    final List<Node> blockBody = b.getBody();

                    for (int i = 0; i < blockBody.size(); i++) {
                        final Node node = blockBody.get(i);

                        if (node instanceof Expression) {
                            final Expression e = (Expression) node;

                            if (matchLoadStoreAny(node, exceptionCopies, v)) {
                                exceptionCopies.add(v.get());
                            }
                            else if (e != lastInFinally &&
                                     matchGetArguments(e, AstCode.AThrow, a) &&
                                     matchLoadAny(a.get(0), exceptionCopies)) {

                                e.setCode(AstCode.Goto);
                                e.setOperand(endFinallyLabel);
                                e.getArguments().clear();
                            }
                        }
                    }
                }

                if (body.size() >= 1 &&
                    matchGetArguments(body.get(body.size() - 1), AstCode.AThrow, a) &&
                    matchLoadAny(a.get(0), exceptionCopies)) {

                    body.set(body.size() - 1, new Expression(AstCode.EndFinally, null, Expression.MYSTERY_OFFSET));
                }
            }
        }
    }

    private static void rewriteSynchronized(final Block method) {
        final StrongBox<LockInfo> lockInfoBox = new StrongBox<>();

        for (final Block block : method.getSelfAndChildrenRecursive(Block.class)) {
            final List<Node> body = block.getBody();

            for (int i = 0; i < body.size() - 1; i++) {
                if (matchLock(body, i, lockInfoBox) &&
                    i + lockInfoBox.get().operationCount < body.size() &&
                    body.get(i + lockInfoBox.get().operationCount) instanceof TryCatchBlock) {

                    final TryCatchBlock tryCatch = (TryCatchBlock) body.get(i + lockInfoBox.get().operationCount);

                    if (tryCatch.isSynchronized()) {
                        continue;
                    }

                    final Block finallyBlock = tryCatch.getFinallyBlock();

                    if (finallyBlock != null) {
                        final List<Node> finallyBody = finallyBlock.getBody();
                        final LockInfo lockInfo = lockInfoBox.get();

                        if (finallyBody.size() == 3 &&
                            matchUnlock(finallyBody.get(1), lockInfo)) {

                            if (rewriteSynchronizedCore(tryCatch, lockInfo.operationCount)) {
                                tryCatch.setSynchronized(true);
                            }
                            else {
                                final StrongBox<Variable> v = new StrongBox<>();
                                final List<Variable> lockCopies = new ArrayList<>();

                                if (lockInfo.lockCopy != null) {
                                    lockCopies.add(lockInfo.lockCopy);
                                }

                                for (final Expression e : tryCatch.getChildrenAndSelfRecursive(Expression.class)) {
                                    if (matchLoadAny(e, lockCopies)) {
                                        e.setOperand(lockInfo.lock);
                                    }
                                    else if (matchLoadStore(e, lockInfo.lock, v) &&
                                             v.get() != lockInfo.lock) {

                                        lockCopies.add(v.get());
                                    }
                                }
                            }

                            inlineLockAccess(tryCatch, body, lockInfo);
                        }
                    }
                }
            }
        }
    }

    private static boolean rewriteSynchronizedCore(final TryCatchBlock tryCatch, final int depth) {
        final Block tryBlock = tryCatch.getTryBlock();
        final List<Node> tryBody = tryBlock.getBody();

        final LockInfo lockInfo;
        final StrongBox<LockInfo> lockInfoBox = new StrongBox<>();

    test:
        {
            switch (tryBody.size()) {
                case 0:
                    return false;
                case 1:
                    lockInfo = null;
                    break test;
            }

            if (matchLock(tryBody, 0, lockInfoBox)) {
                lockInfo = lockInfoBox.get();

                if (lockInfo.operationCount < tryBody.size() &&
                    tryBody.get(lockInfo.operationCount) instanceof TryCatchBlock) {

                    final TryCatchBlock nestedTry = (TryCatchBlock) tryBody.get(lockInfo.operationCount);
                    final Block finallyBlock = nestedTry.getFinallyBlock();

                    if (finallyBlock == null) {
                        return false;
                    }

                    final List<Node> finallyBody = finallyBlock.getBody();

                    if (finallyBody.size() == 3 &&
                        matchUnlock(finallyBody.get(1), lockInfo) &&
                        rewriteSynchronizedCore(nestedTry, depth + 1)) {

                        tryCatch.setSynchronized(true);
                        inlineLockAccess(tryCatch, tryBody, lockInfo);

                        return true;
                    }
                }
            }
            else {
                lockInfo = null;
            }

            break test;
        }

        final boolean skipTrailingBranch = matchUnconditionalBranch(tryBody.get(tryBody.size() - 1));

        if (tryBody.size() < (skipTrailingBranch ? depth + 1 : depth)) {
            return false;
        }

        final int removeTail = tryBody.size() - (skipTrailingBranch ? 1 : 0);
        final List<Node> monitorExitNodes;

        if (removeTail > 0 &&
            tryBody.get(removeTail - 1) instanceof TryCatchBlock) {

            final TryCatchBlock innerTry = (TryCatchBlock) tryBody.get(removeTail - 1);
            final List<Node> innerTryBody = innerTry.getTryBlock().getBody();

            if (matchLock(innerTryBody, 0, lockInfoBox) &&
                rewriteSynchronizedCore(innerTry, depth)) {

                inlineLockAccess(tryCatch, tryBody, lockInfo);
                tryCatch.setSynchronized(true);

                return true;
            }

            final boolean skipInnerTrailingBranch = matchUnconditionalBranch(innerTryBody.get(innerTryBody.size() - 1));

            if (innerTryBody.size() < (skipInnerTrailingBranch ? depth + 1 : depth)) {
                return false;
            }

            final int innerRemoveTail = innerTryBody.size() - (skipInnerTrailingBranch ? 1 : 0);

            monitorExitNodes = innerTryBody.subList(innerRemoveTail - depth, innerRemoveTail);
        }
        else {
            monitorExitNodes = tryBody.subList(removeTail - depth, removeTail);
        }

        final boolean removeAll = all(
            monitorExitNodes,
            new Predicate<Node>() {
                @Override
                public boolean test(final Node node) {
                    return match(node, AstCode.MonitorExit);
                }
            }
        );

        if (removeAll) {
            //
            // Remove the monitorexit instructions that we've already found.  Thank you, SubList.clear().
            //
            monitorExitNodes.clear();

            if (!tryCatch.getCatchBlocks().isEmpty()) {
                final TryCatchBlock newTryCatch = new TryCatchBlock();

                newTryCatch.setTryBlock(tryCatch.getTryBlock());
                newTryCatch.getCatchBlocks().addAll(tryCatch.getCatchBlocks());

                tryCatch.getCatchBlocks().clear();
                tryCatch.setTryBlock(new Block(newTryCatch));
            }

            inlineLockAccess(tryCatch, tryBody, lockInfo);

            tryCatch.setSynchronized(true);

            return true;
        }

        return false;
    }

    private static void inlineLockAccess(final Node owner, final List<Node> body, final LockInfo lockInfo) {
        if (lockInfo == null || lockInfo.lockInit == null) {
            return;
        }

        boolean lockCopyUsed = false;

        final StrongBox<Expression> a = new StrongBox<>();
        final List<Expression> lockAccesses = new ArrayList<>();
        final Set<Expression> lockAccessLoads = new HashSet<>();

        for (final Expression e : owner.getSelfAndChildrenRecursive(Expression.class)) {
            if (matchLoad(e, lockInfo.lock) && !lockAccessLoads.contains(e)) {

                //
                // The lock variable is used elsewhere; we can't remove it.
                //
                return;
            }

            if (lockInfo.lockCopy != null &&
                matchLoad(e, lockInfo.lockCopy) &&
                !lockAccessLoads.contains(e)) {

                lockCopyUsed = true;
            }
            else if ((matchGetArgument(e, AstCode.MonitorEnter, a) || matchGetArgument(e, AstCode.MonitorExit, a)) &&
                     (matchLoad(a.get(), lockInfo.lock) || lockInfo.lockCopy != null && matchLoad(a.get(), lockInfo.lockCopy))) {

                lockAccesses.add(e);
                lockAccessLoads.add(a.get());
            }
        }

        for (final Expression e : lockAccesses) {
            e.getArguments().set(0, lockInfo.lockInit.clone());
        }

        body.remove(lockInfo.lockStore);

        lockInfo.lockAcquire.getArguments().set(0, lockInfo.lockInit.clone());

        if (lockInfo.lockCopy != null && !lockCopyUsed) {
            body.remove(lockInfo.lockStoreCopy);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="RemoveRedundantCode Step">

    @SuppressWarnings({ "ConstantConditions", "StatementWithEmptyBody" })
    static void removeRedundantCode(final Block method, final DecompilerSettings settings) {
        final Map<Label, MutableInteger> labelReferenceCount = new IdentityHashMap<>();

        final List<Expression> branchExpressions = method.getSelfAndChildrenRecursive(
            Expression.class,
            new Predicate<Expression>() {
                @Override
                public boolean test(final Expression e) {
                    return e.isBranch();
                }
            }
        );

        for (final Expression e : branchExpressions) {
            for (final Label branchTarget : e.getBranchTargets()) {
                final MutableInteger referenceCount = labelReferenceCount.get(branchTarget);

                if (referenceCount == null) {
                    labelReferenceCount.put(branchTarget, new MutableInteger(1));
                }
                else {
                    referenceCount.increment();
                }
            }
        }

        for (final Block block : method.getSelfAndChildrenRecursive(Block.class)) {
            final List<Node> body = block.getBody();
            final List<Node> newBody = new ArrayList<>(body.size());

            for (int i = 0, n = body.size(); i < n; i++) {
                final Node node = body.get(i);
                final StrongBox<Label> target = new StrongBox<>();
                final List<Expression> args = new ArrayList<>();

                if (matchGetOperand(node, AstCode.Goto, target) &&
                    i + 1 < body.size() &&
                    body.get(i + 1) == target.get()) {

                    //
                    // Ignore the branch.
                    //
                    if (labelReferenceCount.get(target.get()).getValue() == 1) {
                        //
                        // Ignore the label as well.
                        //
                        i++;
                    }
                }
                else if (match(node, AstCode.Nop)) {
                    //
                    // Ignore NOP.
                    //
                }
                else if (match(node, AstCode.Load)) {
                    //
                    // Ignore empty load.
                    //
                }
                else if (matchGetArguments(node, AstCode.Pop, args)) {
                    final StrongBox<Variable> variable = new StrongBox<>();

                    if (!matchGetOperand(args.get(0), AstCode.Load, variable)) {
                        throw new IllegalStateException("Pop should just have Load at this stage.");
                    }

                    //
                    // Best effort to move bytecode range to previous statement.
                    //

                    final StrongBox<Variable> previousVariable = new StrongBox<>();
                    final StrongBox<Expression> previousExpression = new StrongBox<>();

                    if (i - 1 >= 0 &&
                        matchGetArgument(body.get(i - 1), AstCode.Store, previousVariable, previousExpression) &&
                        previousVariable.get() == variable.get()) {

                        previousExpression.get().getRanges().addAll(((Expression) node).getRanges());

                        //
                        // Ignore POP.
                        //
                    }
                }
                else if (matchGetArguments(node, AstCode.Pop2, args)) {
                    final StrongBox<Variable> v1 = new StrongBox<>();
                    final StrongBox<Variable> v2 = new StrongBox<>();

                    final StrongBox<Variable> pv1 = new StrongBox<>();
                    final StrongBox<Expression> pe1 = new StrongBox<>();

                    if (args.size() == 1) {
                        if (!matchGetOperand(args.get(0), AstCode.Load, v1)) {
                            throw new IllegalStateException("Pop2 should just have Load arguments at this stage.");
                        }

                        if (!v1.get().getType().getSimpleType().isDoubleWord()) {
                            throw new IllegalStateException("Pop2 instruction has only one single-word operand.");
                        }

                        //
                        // Best effort to move bytecode range to previous statement.
                        //

                        if (i - 1 >= 0 &&
                            matchGetArgument(body.get(i - 1), AstCode.Store, pv1, pe1) &&
                            pv1.get() == v1.get()) {

                            pe1.get().getRanges().addAll(((Expression) node).getRanges());

                            //
                            // Ignore POP2.
                            //
                        }
                    }
                    else {
                        if (!matchGetOperand(args.get(0), AstCode.Load, v1) ||
                            !matchGetOperand(args.get(1), AstCode.Load, v2)) {

                            throw new IllegalStateException("Pop2 should just have Load arguments at this stage.");
                        }

                        //
                        // Best effort to move bytecode range to previous statement.
                        //

                        final StrongBox<Variable> pv2 = new StrongBox<>();
                        final StrongBox<Expression> pe2 = new StrongBox<>();

                        if (i - 2 >= 0 &&
                            matchGetArgument(body.get(i - 2), AstCode.Store, pv1, pe1) &&
                            pv1.get() == v1.get() &&
                            matchGetArgument(body.get(i - 1), AstCode.Store, pv2, pe2) &&
                            pv2.get() == v2.get()) {

                            pe1.get().getRanges().addAll(((Expression) node).getRanges());
                            pe2.get().getRanges().addAll(((Expression) node).getRanges());

                            //
                            // Ignore POP2.
                            //
                        }
                    }
                }
                else if (node instanceof Label) {
                    final Label label = (Label) node;
                    final MutableInteger referenceCount = labelReferenceCount.get(label);

                    if (referenceCount != null && referenceCount.getValue() > 0) {
                        newBody.add(label);
                    }
                }
                else if (node instanceof TryCatchBlock) {
                    final TryCatchBlock tryCatch = (TryCatchBlock) node;

                    if (!isEmptyTryCatch(tryCatch)) {
                        newBody.add(node);
                    }
                }
                else if (match(node, AstCode.Switch) && !settings.getRetainPointlessSwitches()) {
                    final Expression e = (Expression) node;
                    final Label[] targets = (Label[]) e.getOperand();

                    if (targets.length == 1) {
                        final Expression test = e.getArguments().get(0);

                        e.setCode(AstCode.Goto);
                        e.setOperand(targets[0]);

                        if (Inlining.canBeExpressionStatement(test)) {
                            newBody.add(test);
                        }

                        e.getArguments().clear();
                    }

                    newBody.add(node);
                }
                else {
                    newBody.add(node);
                }
            }

            body.clear();
            body.addAll(newBody);
        }

        //
        // DUP removal.
        //

        for (final Expression e : method.getSelfAndChildrenRecursive(Expression.class)) {
            final List<Expression> arguments = e.getArguments();

            for (int i = 0, n = arguments.size(); i < n; i++) {
                final Expression argument = arguments.get(i);

                switch (argument.getCode()) {
                    case Dup:
                    case Dup2:
                    case DupX1:
                    case DupX2:
                    case Dup2X1:
                    case Dup2X2:
                        final Expression firstArgument = argument.getArguments().get(0);
                        firstArgument.getRanges().addAll(argument.getRanges());
                        arguments.set(i, firstArgument);
                        break;
                }
            }
        }

        cleanUpTryBlocks(method);
    }

    private static boolean isEmptyTryCatch(final TryCatchBlock tryCatch) {
        if (tryCatch.getFinallyBlock() != null && !tryCatch.getFinallyBlock().getBody().isEmpty()) {
            return false;
        }

        if (matchEmptyBlockOrLeave(tryCatch.getTryBlock())) {
            return true;
        }

        final List<Node> body = tryCatch.getTryBlock().getBody();
        final StrongBox<Label> label = new StrongBox<>();

        return body.size() == 3 &&
               matchGetOperand(body.get(0), AstCode.Goto, label) &&
               body.get(1) == label.get() &&
               match(body.get(2), AstCode.EndFinally);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="IntroducePreIncrementOptimization Step">

    private static void introducePreIncrementOptimization(final DecompilerContext context, final Block method) {
        final Inlining inlining = new Inlining(context, method);

        inlining.analyzeMethod();

        for (final Block block : method.getSelfAndChildrenRecursive(Block.class)) {
            final List<Node> body = block.getBody();
            final MutableInteger position = new MutableInteger();

            for (; position.getValue() < body.size() - 1; position.increment()) {
                if (!introducePreIncrementForVariables(body, position) &&
                    !introducePreIncrementForStaticFields(body, position, inlining)) {

                    introducePreIncrementForInstanceFields(body, position, inlining);
                }
            }
        }
    }

    private static boolean introducePreIncrementForVariables(final List<Node> body, final MutableInteger position) {
        final int i = position.getValue();

        if (i >= body.size() - 1) {
            return false;
        }

        final Node node = body.get(i);
        final Node next = body.get(i + 1);

        final StrongBox<Variable> v = new StrongBox<>();
        final StrongBox<Expression> t = new StrongBox<>();
        final StrongBox<Integer> d = new StrongBox<>();

        if (!(node instanceof Expression && next instanceof Expression)) {
            return false;
        }

        final Expression e = (Expression) node;
        final Expression n = (Expression) next;

        if (matchGetArgument(e, AstCode.Inc, v, t) &&
            matchGetOperand(t.get(), AstCode.LdC, Integer.class, d) &&
            Math.abs(d.get()) == 1 &&
            match(n, AstCode.Store) &&
            matchLoad(n.getArguments().get(0), v.get())) {

            n.getArguments().set(
                0,
                new Expression(AstCode.PreIncrement, d.get(), n.getArguments().get(0).getOffset(), n.getArguments().get(0))
            );

            body.remove(i);
            position.decrement();

            return true;
        }

        return false;
    }

    private static boolean introducePreIncrementForStaticFields(final List<Node> body, final MutableInteger position, final Inlining inlining) {
        final int i = position.getValue();

        if (i >= body.size() - 3) {
            return false;
        }

        final Node n1 = body.get(i);
        final Node n2 = body.get(i + 1);
        final Node n3 = body.get(i + 2);
        final Node n4 = body.get(i + 3);

        final StrongBox<Object> tAny = new StrongBox<>();
        final List<Expression> a = new ArrayList<>();

        if (!matchGetArguments(n1, AstCode.Store, tAny, a)) {
            return false;
        }

        final Variable t = (Variable) tAny.get();

        if (!matchGetOperand(a.get(0), AstCode.GetStatic, tAny)) {
            return false;
        }

        final Variable u;
        final FieldReference f = (FieldReference) tAny.get();

        if (!(matchGetArguments(n2, AstCode.Store, tAny, a) &&
              (u = (Variable) tAny.get()) != null &&
              matchGetOperand(a.get(0), AstCode.LdC, tAny) &&
              tAny.get() instanceof Integer &&
              Math.abs((int) tAny.get()) == 1)) {

            return false;
        }

        final Variable v;
        final int amount = (int) tAny.get();

        if (matchGetArguments(n3, AstCode.Store, tAny, a) &&
            inlining.loadCounts.get(v = (Variable) tAny.get()).getValue() > 1 &&
            matchGetArguments(a.get(0), AstCode.Add, a) &&
            matchLoad(a.get(0), t) &&
            matchLoad(a.get(1), u) &&
            matchGetArguments(n4, AstCode.PutStatic, tAny, a) &&
            tAny.get() instanceof FieldReference &&
            StringUtilities.equals(f.getFullName(), ((FieldReference) tAny.get()).getFullName()) &&
            matchLoad(a.get(0), v)) {

            ((Expression) n3).getArguments().set(
                0,
                new Expression(AstCode.PreIncrement, amount, ((Expression) n1).getArguments().get(0).getOffset(), ((Expression) n1).getArguments().get(0))
            );

            body.remove(i);
            body.remove(i);
            body.remove(i + 1);
            position.decrement();

            return true;
        }

        return false;
    }

    private static boolean introducePreIncrementForInstanceFields(final List<Node> body, final MutableInteger position, final Inlining inlining) {
        final int i = position.getValue();

        if (i < 1 || i >= body.size() - 3) {
            return false;
        }

        //
        // +-------------------------------+
        // | n = load(o)                   |
        // | t = getfield(f, load(n))      |
        // | u = ldc(1)                    |
        // | v = add(load(t), load(u))     |
        // | putfield(f, load(n), load(v)) |
        // +-------------------------------+
        //   |
        //   |    +--------------------------------------------+
        //   +--> | v = postincrement(1, getfield(f, load(n))) |
        //        +--------------------------------------------+
        //
        //   == OR ==
        //
        // +---------------------------+
        // | n = ?                     |
        // | t = loadelement(o, n)     |
        // | u = ldc(1)                |
        // | v = add(load(t), load(u)) |
        // | storeelement(o, n, v)     |
        // +---------------------------+
        //   |
        //   |    +-----------------------------------------+
        //   +--> | v = postincrement(1, loadelement(o, n)) |
        //        +-----------------------------------------+
        //

        if (!(body.get(i) instanceof Expression &&
              body.get(i - 1) instanceof Expression &&
              body.get(i + 1) instanceof Expression &&
              body.get(i + 2) instanceof Expression &&
              body.get(i + 3) instanceof Expression)) {

            return false;
        }

        final Expression e0 = (Expression) body.get(i - 1);
        final Expression e1 = (Expression) body.get(i);

        final List<Expression> a = new ArrayList<>();
        final StrongBox<Variable> tVar = new StrongBox<>();

        if (!matchGetArguments(e0, AstCode.Store, tVar, a)) {
            return false;
        }

        final Variable n = tVar.get();
        final StrongBox<Object> unused = new StrongBox<>();
        final boolean field;

        if (!matchGetArguments(e1, AstCode.Store, tVar, a) ||
            !((field = match(a.get(0), AstCode.GetField)) ? matchGetArguments(a.get(0), AstCode.GetField, unused, a)
                                                          : matchGetArguments(a.get(0), AstCode.LoadElement, a)) ||
            !matchLoad(a.get(field ? 0 : 1), n)) {

            return false;
        }

        final Variable t = tVar.get();
        final Variable o = field ? null : (Variable) a.get(0).getOperand();
        final FieldReference f = field ? (FieldReference) unused.get() : null;
        final Expression e2 = (Expression) body.get(i + 1);
        final StrongBox<Integer> amount = new StrongBox<>();

        if (!matchGetArguments(e2, AstCode.Store, tVar, a) ||
            !matchGetOperand(a.get(0), AstCode.LdC, Integer.class, amount) ||
            Math.abs(amount.get()) != 1) {

            return false;
        }

        final Variable u = tVar.get();

        //  v = add(load(t), load(u))
        //  putfield(field, load(n), load(v))

        final Expression e3 = (Expression) body.get(i + 2);

        if (!matchGetArguments(e3, AstCode.Store, tVar, a) ||
            tVar.get().isGenerated() && inlining.loadCounts.get(tVar.get()).getValue() <= 1 ||
            !matchGetArguments(a.get(0), AstCode.Add, a) ||
            !matchLoad(a.get(0), t) ||
            !matchLoad(a.get(1), u)) {

            return false;
        }

        final Variable v = tVar.get();
        final Expression e4 = (Expression) body.get(i + 3);

        if (!(field ? matchGetArguments(e4, AstCode.PutField, unused, a)
                    : matchGetArguments(e4, AstCode.StoreElement, a)) ||
            !(field ? StringUtilities.equals(f.getFullName(), ((FieldReference) unused.get()).getFullName())
                    : matchLoad(a.get(0), o)) ||
            !matchLoad(a.get(field ? 0 : 1), n) ||
            !matchLoad(a.get(field ? 1 : 2), v)) {

            return false;
        }

        final Expression newExpression = new Expression(
            AstCode.PreIncrement,
            amount.get(),
            e1.getArguments().get(0).getOffset(),
            e1.getArguments().get(0)
        );

        e3.getArguments().set(0, newExpression);

        body.remove(i);
        body.remove(i);
        body.remove(i + 1);

        position.decrement();

        return true;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ReduceBranchInstructionSet Step">

    private static void reduceBranchInstructionSet(final Block block) {
        final List<Node> body = block.getBody();

        for (int i = 0; i < body.size(); i++) {
            final Node node = body.get(i);

            if (!(node instanceof Expression)) {
                continue;
            }

            final Expression e = (Expression) node;
            final AstCode code;

            switch (e.getCode()) {
                case __TableSwitch:
                case __LookupSwitch:
                case Switch: {
                    e.getArguments().get(0).getRanges().addAll(e.getRanges());
                    e.getRanges().clear();
                    continue;
                }

                case __LCmp:
                case __FCmpL:
                case __FCmpG:
                case __DCmpL:
                case __DCmpG: {
                    if (i == body.size() - 1 || !(body.get(i + 1) instanceof Expression)) {
                        continue;
                    }

                    final Expression next = (Expression) body.get(i + 1);

                    switch (next.getCode()) {
                        case __IfEq:
                            code = AstCode.CmpEq;
                            break;
                        case __IfNe:
                            code = AstCode.CmpNe;
                            break;
                        case __IfLt:
                            code = AstCode.CmpLt;
                            break;
                        case __IfGe:
                            code = AstCode.CmpGe;
                            break;
                        case __IfGt:
                            code = AstCode.CmpGt;
                            break;
                        case __IfLe:
                            code = AstCode.CmpLe;
                            break;
                        default:
                            continue;
                    }

                    body.remove(i);
                    break;
                }

                case __IfEq:
                    e.getArguments().add(new Expression(AstCode.LdC, 0, e.getOffset()));
                    code = AstCode.CmpEq;
                    break;

                case __IfNe:
                    e.getArguments().add(new Expression(AstCode.LdC, 0, e.getOffset()));
                    code = AstCode.CmpNe;
                    break;

                case __IfLt:
                    e.getArguments().add(new Expression(AstCode.LdC, 0, e.getOffset()));
                    code = AstCode.CmpLt;
                    break;
                case __IfGe:
                    e.getArguments().add(new Expression(AstCode.LdC, 0, e.getOffset()));
                    code = AstCode.CmpGe;
                    break;
                case __IfGt:
                    e.getArguments().add(new Expression(AstCode.LdC, 0, e.getOffset()));
                    code = AstCode.CmpGt;
                    break;
                case __IfLe:
                    e.getArguments().add(new Expression(AstCode.LdC, 0, e.getOffset()));
                    code = AstCode.CmpLe;
                    break;

                case __IfICmpEq:
                    code = AstCode.CmpEq;
                    break;
                case __IfICmpNe:
                    code = AstCode.CmpNe;
                    break;
                case __IfICmpLt:
                    code = AstCode.CmpLt;
                    break;
                case __IfICmpGe:
                    code = AstCode.CmpGe;
                    break;
                case __IfICmpGt:
                    code = AstCode.CmpGt;
                    break;
                case __IfICmpLe:
                    code = AstCode.CmpLe;
                    break;
                case __IfACmpEq:
                    code = AstCode.CmpEq;
                    break;
                case __IfACmpNe:
                    code = AstCode.CmpNe;
                    break;

                case __IfNull:
                    e.getArguments().add(new Expression(AstCode.AConstNull, null, e.getOffset()));
                    code = AstCode.CmpEq;
                    break;
                case __IfNonNull:
                    e.getArguments().add(new Expression(AstCode.AConstNull, null, e.getOffset()));
                    code = AstCode.CmpNe;
                    break;

                default:
                    continue;
            }

            final Expression newExpression = new Expression(code, null, e.getOffset(), e.getArguments());

            body.set(i, new Expression(AstCode.IfTrue, e.getOperand(), newExpression.getOffset(), newExpression));
            newExpression.getRanges().addAll(e.getRanges());
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="RemoveInnerClassInitSecurityChecks Step">

    private final static class RemoveInnerClassInitSecurityChecksOptimization extends AbstractExpressionOptimization {
        protected RemoveInnerClassInitSecurityChecksOptimization(final DecompilerContext context, final Block method) {
            super(context, method);
        }

        @Override
        public boolean run(final List<Node> body, final Expression head, final int position) {
            final StrongBox<Expression> getClassArgument = new StrongBox<>();
            final StrongBox<Variable> getClassArgumentVariable = new StrongBox<>();
            final StrongBox<Variable> constructorTargetVariable = new StrongBox<>();
            final StrongBox<Variable> constructorArgumentVariable = new StrongBox<>();
            final StrongBox<MethodReference> constructor = new StrongBox<>();
            final StrongBox<MethodReference> getClassMethod = new StrongBox<>();
            final List<Expression> arguments = new ArrayList<>();

            if (position > 0) {
                final Node previous = body.get(position - 1);

                arguments.clear();

                if (matchGetArguments(head, AstCode.InvokeSpecial, constructor, arguments) &&
                    arguments.size() > 1 &&
                    matchGetOperand(arguments.get(0), AstCode.Load, constructorTargetVariable) &&
                    matchGetOperand(arguments.get(1), AstCode.Load, constructorArgumentVariable) &&
                    matchGetArgument(previous, AstCode.InvokeVirtual, getClassMethod, getClassArgument) &&
                    isGetClassMethod(getClassMethod.get()) &&
                    matchGetOperand(getClassArgument.get(), AstCode.Load, getClassArgumentVariable) &&
                    getClassArgumentVariable.get() == constructorArgumentVariable.get()) {

                    final TypeReference constructorTargetType = constructorTargetVariable.get().getType();
                    final TypeReference constructorArgumentType = constructorArgumentVariable.get().getType();

                    if (constructorTargetType != null && constructorArgumentType != null) {
                        final TypeDefinition resolvedConstructorTargetType = constructorTargetType.resolve();
                        final TypeDefinition resolvedConstructorArgumentType = constructorArgumentType.resolve();

                        if (resolvedConstructorTargetType != null &&
                            resolvedConstructorArgumentType != null &&
                            resolvedConstructorTargetType.isNested() &&
                            !resolvedConstructorTargetType.isStatic() &&
                            (!resolvedConstructorArgumentType.isNested() ||
                             isEnclosedBy(resolvedConstructorTargetType, resolvedConstructorArgumentType))) {

                            body.remove(position - 1);
                            return true;
                        }
                    }
                }
            }

            return false;
        }

        private static boolean isGetClassMethod(final MethodReference method) {
            return method.getParameters().isEmpty() &&
                   StringUtilities.equals(method.getName(), "getClass");
        }

        private static boolean isEnclosedBy(final TypeReference innerType, final TypeReference outerType) {
            if (innerType == null) {
                return false;
            }

            for (TypeReference current = innerType.getDeclaringType();
                 current != null;
                 current = current.getDeclaringType()) {

                if (MetadataResolver.areEquivalent(current, outerType)) {
                    return true;
                }
            }

            final TypeDefinition resolvedInnerType = innerType.resolve();

            return resolvedInnerType != null &&
                   isEnclosedBy(resolvedInnerType.getBaseType(), outerType);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ReduceComparisonInstructionSet Step">

    private static void reduceComparisonInstructionSet(final Expression expression) {
        final List<Expression> arguments = expression.getArguments();
        final Expression firstArgument = arguments.isEmpty() ? null : arguments.get(0);

        if (matchSimplifiableComparison(expression)) {
            arguments.clear();
            arguments.addAll(firstArgument.getArguments());
            expression.getRanges().addAll(firstArgument.getRanges());
        }

        if (matchReversibleComparison(expression)) {
            final AstCode reversedCode;

            switch (firstArgument.getCode()) {
                case CmpEq:
                    reversedCode = AstCode.CmpNe;
                    break;
                case CmpNe:
                    reversedCode = AstCode.CmpEq;
                    break;
                case CmpLt:
                    reversedCode = AstCode.CmpGe;
                    break;
                case CmpGe:
                    reversedCode = AstCode.CmpLt;
                    break;
                case CmpGt:
                    reversedCode = AstCode.CmpLe;
                    break;
                case CmpLe:
                    reversedCode = AstCode.CmpGt;
                    break;

                default:
                    throw ContractUtils.unreachable();
            }

            expression.setCode(reversedCode);
            expression.getRanges().addAll(firstArgument.getRanges());

            arguments.clear();
            arguments.addAll(firstArgument.getArguments());
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="SplitToMovableBlocks Step">

    private void splitToMovableBlocks(final Block block) {
        final List<Node> basicBlocks = new ArrayList<>();

        final List<Node> body = block.getBody();
        final Object firstNode = firstOrDefault(body);

        final Label entryLabel;

        if (firstNode instanceof Label) {
            entryLabel = (Label) firstNode;
        }
        else {
            entryLabel = new Label();
            entryLabel.setName("Block_" + (_nextLabelIndex++));
        }

        BasicBlock basicBlock = new BasicBlock();
        List<Node> basicBlockBody = basicBlock.getBody();

        basicBlocks.add(basicBlock);
        basicBlockBody.add(entryLabel);

        block.setEntryGoto(new Expression(AstCode.Goto, entryLabel, Expression.MYSTERY_OFFSET));

        if (!body.isEmpty()) {
            if (body.get(0) != entryLabel) {
                basicBlockBody.add(body.get(0));
            }

            for (int i = 1; i < body.size(); i++) {
                final Node lastNode = body.get(i - 1);
                final Node currentNode = body.get(i);

                //
                // Start a new basic block if necessary.
                //
                if (currentNode instanceof Label ||
                    currentNode instanceof TryCatchBlock ||
                    lastNode.isConditionalControlFlow() ||
                    lastNode.isUnconditionalControlFlow()) {

                    //
                    // Try to reuse the label.
                    //
                    final Label label = currentNode instanceof Label ? (Label) currentNode
                                                                     : new Label("Block_" + (_nextLabelIndex++));

                    //
                    // Terminate the last block.
                    //
                    if (!lastNode.isUnconditionalControlFlow()) {
                        basicBlockBody.add(new Expression(AstCode.Goto, label, Expression.MYSTERY_OFFSET));
                    }

                    //
                    // Start the new block.
                    //
                    basicBlock = new BasicBlock();
                    basicBlocks.add(basicBlock);
                    basicBlockBody = basicBlock.getBody();
                    basicBlockBody.add(label);

                    //
                    // Add the node to the basic block.
                    //
                    if (currentNode != label) {
                        basicBlockBody.add(currentNode);
                    }

                    if (currentNode instanceof TryCatchBlock) {
                        //
                        // If we have a TryCatchBlock with all nested blocks exiting to the same label,
                        // go ahead and insert an explicit jump to that label.  This prevents us from
                        // potentially adding a jump to the next node that would never actually be followed
                        // (possibly throwing a wrench in FindLoopsAndConditions later).
                        //
                        final Label exitLabel = checkExit(currentNode);

                        if (exitLabel != null) {
                            body.add(i + 1, new Expression(AstCode.Goto, exitLabel, Expression.MYSTERY_OFFSET));
                        }
                    }
                }
                else {
                    basicBlockBody.add(currentNode);
                }
            }
        }

        body.clear();
        body.addAll(basicBlocks);
    }

    private Label checkExit(final Node node) {
        if (node == null) {
            return null;
        }

        if (node instanceof BasicBlock) {
            return checkExit(lastOrDefault(((BasicBlock) node).getBody()));
        }

        if (node instanceof TryCatchBlock) {
            final TryCatchBlock tryCatch = (TryCatchBlock) node;
            final Label exitLabel = checkExit(lastOrDefault(tryCatch.getTryBlock().getBody()));

            if (exitLabel == null) {
                return null;
            }

            for (final CatchBlock catchBlock : tryCatch.getCatchBlocks()) {
                if (checkExit(lastOrDefault(catchBlock.getBody())) != exitLabel) {
                    return null;
                }
            }

            final Block finallyBlock = tryCatch.getFinallyBlock();

            if (finallyBlock != null && checkExit(lastOrDefault(finallyBlock.getBody())) != exitLabel) {
                return null;
            }

            return exitLabel;
        }

        if (node instanceof Expression) {
            final Expression expression = (Expression) node;
            final AstCode code = expression.getCode();

            if (code == AstCode.Goto) {
                return (Label) expression.getOperand();
            }
        }

        return null;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="SimplifyShortCircuit Step">

    private static final class SimplifyShortCircuitOptimization extends AbstractBasicBlockOptimization {
        public SimplifyShortCircuitOptimization(final DecompilerContext context, final Block method) {
            super(context, method);
        }

        @Override
        public final boolean run(final List<Node> body, final BasicBlock head, final int position) {
            assert body.contains(head);

            final StrongBox<Expression> condition = new StrongBox<>();
            final StrongBox<Label> trueLabel = new StrongBox<>();
            final StrongBox<Label> falseLabel = new StrongBox<>();

            final StrongBox<Expression> nextCondition = new StrongBox<>();
            final StrongBox<Label> nextTrueLabel = new StrongBox<>();
            final StrongBox<Label> nextFalseLabel = new StrongBox<>();

            if (matchLastAndBreak(head, AstCode.IfTrue, trueLabel, condition, falseLabel)) {
                for (int pass = 0; pass < 2; pass++) {
                    //
                    // On second pass, swap labels and negate expression of the first branch.
                    // It is slightly ugly, but much better than copy-pasting the whole block.
                    //

                    final Label nextLabel = pass == 0 ? trueLabel.get() : falseLabel.get();
                    final Label otherLabel = pass == 0 ? falseLabel.get() : trueLabel.get();
                    final boolean negate = pass == 1;

                    final BasicBlock next = labelToBasicBlock.get(nextLabel);

                    //
                    // Try to match short circuit operators, e.g.,:
                    //
                    // c = a || b
                    // c = a && b
                    //
                    if (body.contains(next) &&
                        next != head &&
                        labelGlobalRefCount.get(nextLabel).getValue() == 1 &&
                        matchSingleAndBreak(next, AstCode.IfTrue, nextTrueLabel, nextCondition, nextFalseLabel) &&
                        (otherLabel == nextFalseLabel.get() || otherLabel == nextTrueLabel.get())) {

                        //
                        // Create short circuit branch.
                        //
                        final Expression logicExpression;

                        if (otherLabel == nextFalseLabel.get()) {
                            logicExpression = makeLeftAssociativeShortCircuit(
                                AstCode.LogicalAnd,
                                negate ? new Expression(AstCode.LogicalNot, null, condition.get().getOffset(), condition.get()) : condition.get(),
                                nextCondition.get()
                            );
                        }
                        else {
                            logicExpression = makeLeftAssociativeShortCircuit(
                                AstCode.LogicalOr,
                                negate ? condition.get() : new Expression(AstCode.LogicalNot, null, condition.get().getOffset(), condition.get()),
                                nextCondition.get()
                            );
                        }

                        final List<Node> headBody = head.getBody();

                        removeTail(headBody, AstCode.IfTrue, AstCode.Goto);

                        headBody.add(new Expression(AstCode.IfTrue, nextTrueLabel.get(), logicExpression.getOffset(), logicExpression));
                        headBody.add(new Expression(AstCode.Goto, nextFalseLabel.get(), logicExpression.getOffset()));

                        labelGlobalRefCount.get(trueLabel.get()).decrement();
                        labelGlobalRefCount.get(falseLabel.get()).decrement();

                        //
                        // Remove the inlined branch from scope.
                        //
                        removeOrThrow(body, next);

                        return true;
                    }
                }
            }

            return false;
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="PreProcessShortCircuitAssignments Step">

    private static final class PreProcessShortCircuitAssignmentsOptimization extends AbstractBasicBlockOptimization {
        public PreProcessShortCircuitAssignmentsOptimization(final DecompilerContext context, final Block method) {
            super(context, method);
        }

        @Override
        public final boolean run(final List<Node> body, final BasicBlock head, final int position) {
            assert body.contains(head);

            final StrongBox<Expression> condition = new StrongBox<>();
            final StrongBox<Label> trueLabel = new StrongBox<>();
            final StrongBox<Label> falseLabel = new StrongBox<>();

            if (matchLastAndBreak(head, AstCode.IfTrue, trueLabel, condition, falseLabel)) {
                final StrongBox<Label> nextTrueLabel = new StrongBox<>();
                final StrongBox<Label> nextFalseLabel = new StrongBox<>();

                final StrongBox<Variable> sourceVariable = new StrongBox<>();
                final StrongBox<Expression> assignedValue = new StrongBox<>();
                final StrongBox<Expression> equivalentLoad = new StrongBox<>();

                final StrongBox<Expression> left = new StrongBox<>();
                final StrongBox<Expression> right = new StrongBox<>();

                boolean modified = false;

                for (int pass = 0; pass < 2; pass++) {
                    //
                    // On second pass, swap labels and negate expression of the first branch.
                    // It is slightly ugly, but much better than copy-pasting the whole block.
                    //

                    final Label nextLabel = pass == 0 ? trueLabel.get() : falseLabel.get();
                    final Label otherLabel = pass == 0 ? falseLabel.get() : trueLabel.get();

                    final BasicBlock next = labelToBasicBlock.get(nextLabel);
                    final BasicBlock other = labelToBasicBlock.get(otherLabel);

                    //
                    // Try to match short circuit operators, e.g.,:
                    //
                    // c = a || b
                    // c = a && b
                    //
                    if (body.contains(next) &&
                        next != head &&
                        labelGlobalRefCount.get(nextLabel).getValue() == 1 &&
                        matchLastAndBreak(next, AstCode.IfTrue, nextTrueLabel, condition, nextFalseLabel) &&
                        (otherLabel == nextFalseLabel.get() || otherLabel == nextTrueLabel.get())) {

                        final List<Node> nextBody = next.getBody();
                        final List<Node> otherBody = other.getBody();

                        while (nextBody.size() > 3 &&
                               matchAssignment(nextBody.get(nextBody.size() - 3), assignedValue, equivalentLoad) &&
                               matchLoad(assignedValue.value, sourceVariable) &&
                               matchComparison(condition.value, left, right)) {

                            if (matchLoad(left.value, sourceVariable.value)) {
                                condition.value.getArguments().set(0, (Expression) nextBody.get(nextBody.size() - 3));
                                nextBody.remove(nextBody.size() - 3);
                                modified = true;
                            }
                            else if (matchLoad(right.value, sourceVariable.value) && !containsMatch(left.value, equivalentLoad.value)) {
                                condition.value.getArguments().set(1, (Expression) nextBody.get(nextBody.size() - 3));
                                nextBody.remove(nextBody.size() - 3);
                                modified = true;
                            }
                            else {
                                break;
                            }
                        }

                        final boolean modifiedNext = modified;

                        modified = false;

                        while (matchAssignmentAndConditionalBreak(other, assignedValue, condition, trueLabel, falseLabel, equivalentLoad) &&
                               matchLoad(assignedValue.value, sourceVariable) &&
                               matchComparison(condition.value, left, right)) {

                            if (matchLoad(left.value, sourceVariable.value)) {
                                condition.value.getArguments().set(0, (Expression) otherBody.get(otherBody.size() - 3));
                                otherBody.remove(otherBody.size() - 3);
                                modified = true;
                            }
                            else if (matchLoad(right.value, sourceVariable.value) && !containsMatch(left.value, equivalentLoad.value)) {
                                condition.value.getArguments().set(1, (Expression) otherBody.get(otherBody.size() - 3));
                                otherBody.remove(otherBody.size() - 3);
                                modified = true;
                            }
                            else {
                                break;
                            }
                        }

                        final boolean modifiedOther = modified;

                        if (modifiedNext || modifiedOther) {
                            final Inlining inlining = new Inlining(context, method);

                            if (modifiedNext) {
                                inlining.inlineAllInBasicBlock(next);
                            }

                            if (modifiedOther) {
                                inlining.inlineAllInBasicBlock(other);
                            }

                            return true;
                        }

                        return false;
                    }
                }
            }

            return false;
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="InlineConditionalAssignments Step">

    private static final class InlineConditionalAssignmentsOptimization extends AbstractBasicBlockOptimization {
        public InlineConditionalAssignmentsOptimization(final DecompilerContext context, final Block method) {
            super(context, method);
        }

        @Override
        public final boolean run(final List<Node> body, final BasicBlock head, final int position) {
            assert body.contains(head);

            final StrongBox<Expression> condition = new StrongBox<>();
            final StrongBox<Label> trueLabel = new StrongBox<>();
            final StrongBox<Label> falseLabel = new StrongBox<>();

            if (matchLastAndBreak(head, AstCode.IfTrue, trueLabel, condition, falseLabel)) {
                final StrongBox<Variable> sourceVariable = new StrongBox<>();
                final StrongBox<Expression> assignedValue = new StrongBox<>();
                final StrongBox<Expression> equivalentLoad = new StrongBox<>();
                final StrongBox<Expression> left = new StrongBox<>();
                final StrongBox<Expression> right = new StrongBox<>();

                final Label thenLabel = trueLabel.value;
                final Label elseLabel = falseLabel.value;
                final BasicBlock thenSuccessor = labelToBasicBlock.get(thenLabel);
                final BasicBlock elseSuccessor = labelToBasicBlock.get(elseLabel);

                boolean modified = false;

                if (matchAssignmentAndConditionalBreak(elseSuccessor, assignedValue, condition, trueLabel, falseLabel, equivalentLoad) &&
                    matchLoad(assignedValue.value, sourceVariable) &&
                    matchComparison(condition.value, left, right)) {

                    final List<Node> b = elseSuccessor.getBody();

                    if (matchLoad(left.value, sourceVariable.value)) {
                        condition.value.getArguments().set(0, (Expression) b.get(b.size() - 3));
                        b.remove(b.size() - 3);
                        modified = true;
                    }
                    else if (matchLoad(right.value, sourceVariable.value) && !containsMatch(left.value, equivalentLoad.value)) {
                        condition.value.getArguments().set(1, (Expression) b.get(b.size() - 3));
                        b.remove(b.size() - 3);
                        modified = true;
                    }
                }

                if (matchAssignmentAndConditionalBreak(thenSuccessor, assignedValue, condition, trueLabel, falseLabel, equivalentLoad) &&
                    matchLoad(assignedValue.value, sourceVariable) &&
                    matchComparison(condition.value, left, right)) {

                    final List<Node> b = thenSuccessor.getBody();

                    if (matchLoad(left.value, sourceVariable.value)) {
                        condition.value.getArguments().set(0, (Expression) b.get(b.size() - 3));
                        b.remove(b.size() - 3);
                        modified = true;
                    }
                    else if (matchLoad(right.value, sourceVariable.value) && !containsMatch(left.value, equivalentLoad.value)) {
                        condition.value.getArguments().set(1, (Expression) b.get(b.size() - 3));
                        b.remove(b.size() - 3);
                        modified = true;
                    }
                }

                return modified;
            }

            return false;
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="JoinBasicBlocks Step">

    private final static class JoinBasicBlocksOptimization extends AbstractBasicBlockOptimization {
        protected JoinBasicBlocksOptimization(final DecompilerContext context, final Block method) {
            super(context, method);
        }

        @Override
        public final boolean run(final List<Node> body, final BasicBlock head, final int position) {
            final StrongBox<Label> nextLabel = new StrongBox<>();
            final List<Node> headBody = head.getBody();
            final BasicBlock nextBlock;

            final Node secondToLast = CollectionUtilities.getOrDefault(headBody, headBody.size() - 2);

            if (secondToLast != null &&
                !secondToLast.isConditionalControlFlow() &&
                matchGetOperand(headBody.get(headBody.size() - 1), AstCode.Goto, nextLabel) &&
                labelGlobalRefCount.get(nextLabel.get()).getValue() == 1 &
                (nextBlock = labelToBasicBlock.get(nextLabel.get())) != null &&
                nextBlock != EMPTY_BLOCK &&
                body.contains(nextBlock) &&
                nextBlock.getBody().get(0) == nextLabel.get() &&
                !CollectionUtilities.any(nextBlock.getBody(), Predicates.instanceOf(BasicBlock.class))) {

                final Node secondInNext = getOrDefault(nextBlock.getBody(), 1);

                if (secondInNext instanceof TryCatchBlock) {
                    final Block tryBlock = ((TryCatchBlock) secondInNext).getTryBlock();
                    final Node firstInTry = firstOrDefault(tryBlock.getBody());

                    if (firstInTry instanceof BasicBlock) {
                        final Node firstInTryBody = firstOrDefault(((BasicBlock) firstInTry).getBody());

                        if (firstInTryBody instanceof Label &&
                            labelGlobalRefCount.get(firstInTryBody).getValue() > 1) {

                            return false;
                        }
                    }
                }

                removeTail(headBody, AstCode.Goto);
                nextBlock.getBody().remove(0);
                headBody.addAll(nextBlock.getBody());
                removeOrThrow(body, nextBlock);
                return true;
            }

            return false;
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="SimplifyTernaryOperator Step">

    private final static class SimplifyTernaryOperatorOptimization extends AbstractBasicBlockOptimization {
        protected SimplifyTernaryOperatorOptimization(final DecompilerContext context, final Block method) {
            super(context, method);
        }

        @Override
        public final boolean run(final List<Node> body, final BasicBlock head, final int position) {
            final StrongBox<Expression> condition = new StrongBox<>();
            final StrongBox<Label> trueLabel = new StrongBox<>();
            final StrongBox<Label> falseLabel = new StrongBox<>();
            final StrongBox<Variable> trueVariable = new StrongBox<>();
            final StrongBox<Expression> trueExpression = new StrongBox<>();
            final StrongBox<Label> trueFall = new StrongBox<>();
            final StrongBox<Variable> falseVariable = new StrongBox<>();
            final StrongBox<Expression> falseExpression = new StrongBox<>();
            final StrongBox<Label> falseFall = new StrongBox<>();

            final StrongBox<Object> unused = new StrongBox<>();

            if (matchLastAndBreak(head, AstCode.IfTrue, trueLabel, condition, falseLabel) &&
                labelGlobalRefCount.get(trueLabel.value).getValue() == 1 &&
                labelGlobalRefCount.get(falseLabel.value).getValue() == 1 &&
                body.contains(labelToBasicBlock.get(trueLabel.value)) &&
                body.contains(labelToBasicBlock.get(falseLabel.value))) {

                if (((matchSingleAndBreak(labelToBasicBlock.get(trueLabel.value), AstCode.Store, trueVariable, trueExpression, trueFall) &&
                      matchSingleAndBreak(labelToBasicBlock.get(falseLabel.value), AstCode.Store, falseVariable, falseExpression, falseFall) &&
                      trueVariable.value == falseVariable.value &&
                      trueFall.value == falseFall.value) ||
                     (matchSingle(labelToBasicBlock.get(trueLabel.value), AstCode.Return, unused, trueExpression) &&
                      matchSingle(labelToBasicBlock.get(falseLabel.value), AstCode.Return, unused, falseExpression)))) {

                    final boolean isStore = trueVariable.value != null;
                    final AstCode opCode = isStore ? AstCode.Store : AstCode.Return;
                    final TypeReference returnType = isStore ? trueVariable.value.getType() : context.getCurrentMethod().getReturnType();

                    final boolean returnTypeIsBoolean = TypeAnalysis.isBoolean(returnType);

                    final StrongBox<Boolean> leftBooleanValue = new StrongBox<>();
                    final StrongBox<Boolean> rightBooleanValue = new StrongBox<>();

                    final Expression newExpression;

                    // a ? true:false  is equivalent to  a
                    // a ? false:true  is equivalent to  !a
                    // a ? true : b    is equivalent to  a || b
                    // a ? b : true    is equivalent to  !a || b
                    // a ? b : false   is equivalent to  a && b
                    // a ? false : b   is equivalent to  !a && b

                    if (returnTypeIsBoolean &&
                        matchBooleanConstant(trueExpression.value, leftBooleanValue) &&
                        matchBooleanConstant(falseExpression.value, rightBooleanValue) &&
                        (leftBooleanValue.value && !rightBooleanValue.value ||
                         !leftBooleanValue.value && rightBooleanValue.value)) {

                        //
                        // It can be expressed as a trivial expression.
                        //
                        if (leftBooleanValue.value) {
                            newExpression = condition.value;
                        }
                        else {
                            newExpression = new Expression(AstCode.LogicalNot, null, condition.value.getOffset(), condition.value);
                            newExpression.setInferredType(BuiltinTypes.Boolean);
                        }
                    }
                    else if ((returnTypeIsBoolean || TypeAnalysis.isBoolean(falseExpression.value.getInferredType())) &&
                             matchBooleanConstant(trueExpression.value, leftBooleanValue)) {

                        //
                        // It can be expressed as a logical expression.
                        //
                        if (leftBooleanValue.value) {
                            newExpression = makeLeftAssociativeShortCircuit(
                                AstCode.LogicalOr,
                                condition.value,
                                falseExpression.value
                            );
                        }
                        else {
                            newExpression = makeLeftAssociativeShortCircuit(
                                AstCode.LogicalAnd,
                                new Expression(AstCode.LogicalNot, null, condition.value.getOffset(), condition.value),
                                falseExpression.value
                            );
                        }
                    }
                    else if ((returnTypeIsBoolean || TypeAnalysis.isBoolean(trueExpression.value.getInferredType())) &&
                             matchBooleanConstant(falseExpression.value, rightBooleanValue)) {

                        //
                        // It can be expressed as a logical expression.
                        //
                        if (rightBooleanValue.value) {
                            newExpression = makeLeftAssociativeShortCircuit(
                                AstCode.LogicalOr,
                                new Expression(AstCode.LogicalNot, null, condition.value.getOffset(), condition.value),
                                trueExpression.value
                            );
                        }
                        else {
                            newExpression = makeLeftAssociativeShortCircuit(
                                AstCode.LogicalAnd,
                                condition.value,
                                trueExpression.value
                            );
                        }
                    }
                    else {
                        //
                        // Ternary operator tends to create long, complicated return statements.
                        //
                        if (opCode == AstCode.Return) {
                            return false;
                        }

                        //
                        // Only simplify generated variables.
                        //
                        if (opCode == AstCode.Store && !trueVariable.value.isGenerated()) {
                            return false;
                        }

                        //
                        // Create ternary expression.
                        //
                        // Default behavior seems to be to invert the condition.  Try to reverse it.
                        //

                        if (simplifyLogicalNotArgument(condition.value)) {
                            newExpression = new Expression(
                                AstCode.TernaryOp,
                                null,
                                condition.value.getOffset(),
                                condition.value,
                                falseExpression.value,
                                trueExpression.value
                            );
                        }
                        else {
                            newExpression = new Expression(
                                AstCode.TernaryOp,
                                null,
                                condition.value.getOffset(),
                                condition.value,
                                trueExpression.value,
                                falseExpression.value
                            );
                        }
                    }

                    final List<Node> headBody = head.getBody();

                    removeTail(headBody, AstCode.IfTrue, AstCode.Goto);
                    headBody.add(new Expression(opCode, trueVariable.value, newExpression.getOffset(), newExpression));

                    if (isStore) {
                        headBody.add(new Expression(AstCode.Goto, trueFall.value, trueFall.value.getOffset()));
                    }

                    //
                    // Remove the old basic blocks.
                    //

                    removeOrThrow(body, labelToBasicBlock.get(trueLabel.value));
                    removeOrThrow(body, labelToBasicBlock.get(falseLabel.value));

                    return true;
                }
                else {
                    final StrongBox<Label> innerTrue = new StrongBox<>();
                    final StrongBox<Label> innerFalse = new StrongBox<>();
                    final StrongBox<Label> trueBreak = new StrongBox<>();
                    final StrongBox<Label> falseBreak = new StrongBox<>();
                    final StrongBox<Label> intermediateJump = new StrongBox<>();

                    if (matchSingleAndBreak(labelToBasicBlock.get(trueLabel.value), AstCode.IfTrue, innerTrue, trueExpression, trueFall) &&
                        matchSingleAndBreak(labelToBasicBlock.get(falseLabel.value), AstCode.IfTrue, unused, falseExpression, falseFall) &&
                        unused.value == innerTrue.value &&
                        matchLast(labelToBasicBlock.get(falseFall.value), AstCode.Goto, innerFalse)) {

                        final StrongBox<Expression> innerTrueExpression = new StrongBox<>();
                        final StrongBox<Expression> innerFalseExpression = new StrongBox<>();

                        //
                        // (a ? b : c) ? d : e
                        //
                        if (labelGlobalRefCount.get(innerTrue.value).getValue() == 2 &&
                            labelGlobalRefCount.get(innerFalse.value).getValue() == 2 &&
                            matchSingleAndBreak(labelToBasicBlock.get(innerTrue.value), AstCode.Store, trueVariable, innerTrueExpression, trueBreak) &&
                            matchSingleAndBreak(labelToBasicBlock.get(innerFalse.value), AstCode.Store, falseVariable, innerFalseExpression, falseBreak) &&
                            trueVariable.value == falseVariable.value &&
                            trueFall.value == innerFalse.value &&
                            trueBreak.value == falseBreak.value) {

                            final Expression newCondition;
                            final Expression newExpression;

                            final boolean negateInner = simplifyLogicalNotArgument(trueExpression.value);

                            if (negateInner && !simplifyLogicalNotArgument(falseExpression.value)) {
                                final Expression newFalseExpression = new Expression(AstCode.LogicalNot, null, falseExpression.value.getOffset(), falseExpression.value);
                                newFalseExpression.getRanges().addAll(falseExpression.value.getRanges());
                                falseExpression.set(newFalseExpression);
                            }

                            if (simplifyLogicalNotArgument(condition.value)) {
                                newCondition = new Expression(
                                    AstCode.TernaryOp,
                                    null,
                                    condition.value.getOffset(),
                                    condition.value,
                                    falseExpression.value,
                                    trueExpression.value
                                );
                            }
                            else {
                                newCondition = new Expression(
                                    AstCode.TernaryOp,
                                    null,
                                    condition.value.getOffset(),
                                    condition.value,
                                    trueExpression.value,
                                    falseExpression.value
                                );
                            }

                            if (negateInner) {
                                newExpression = new Expression(
                                    AstCode.TernaryOp,
                                    null,
                                    newCondition.getOffset(),
                                    newCondition,
                                    innerFalseExpression.value,
                                    innerTrueExpression.value
                                );
                            }
                            else {
                                newExpression = new Expression(
                                    AstCode.TernaryOp,
                                    null,
                                    newCondition.getOffset(),
                                    newCondition,
                                    innerTrueExpression.value,
                                    innerFalseExpression.value
                                );
                            }

                            final List<Node> headBody = head.getBody();

                            removeTail(headBody, AstCode.IfTrue, AstCode.Goto);

                            headBody.add(new Expression(AstCode.Store, trueVariable.value, newExpression.getOffset(), newExpression));
                            headBody.add(new Expression(AstCode.Goto, trueBreak.value, trueBreak.value.getOffset()));

                            //
                            // Remove the old basic blocks.
                            //

                            removeOrThrow(body, labelToBasicBlock.get(trueLabel.value));
                            removeOrThrow(body, labelToBasicBlock.get(falseLabel.value));
                            removeOrThrow(body, labelToBasicBlock.get(falseFall.value));
                            removeOrThrow(body, labelToBasicBlock.get(innerTrue.value));
                            removeOrThrow(body, labelToBasicBlock.get(innerFalse.value));

                            return true;
                        }

                        //
                        // (a ? b : c)
                        //
                        if (matchSingleAndBreak(labelToBasicBlock.get(innerTrue.value), AstCode.Store, trueVariable, innerTrueExpression, trueBreak) &&
                            (matchSingleAndBreak(labelToBasicBlock.get(falseFall.value), AstCode.Store, falseVariable, innerFalseExpression, falseBreak) ||
                             (matchSimpleBreak(labelToBasicBlock.get(falseFall.value), intermediateJump) &&
                              matchSingleAndBreak(
                                  labelToBasicBlock.get(intermediateJump.value),
                                  AstCode.Store,
                                  falseVariable,
                                  innerFalseExpression,
                                  falseBreak
                              ))) &&
                            trueVariable.value == falseVariable.value &&
                            trueBreak.value == falseBreak.value) {

                            final List<Expression> arguments = condition.value.getArguments();
                            final Expression oldCondition = condition.value.clone();

                            condition.value.setCode(AstCode.TernaryOp);
                            arguments.clear();

                            Collections.addAll(
                                arguments,
                                simplifyLogicalNot(oldCondition),
                                simplifyLogicalNot(trueExpression.value),
                                simplifyLogicalNot(falseExpression.value)
                            );

                            final List<Node> headBody = head.getBody();

                            ((Expression) headBody.get(headBody.size() - 2)).setOperand(innerTrue.value);

                            if (matchSimpleBreak(labelToBasicBlock.get(falseFall.value), intermediateJump)) {
                                if (labelGlobalRefCount.get(falseFall.value).getValue() == 1) {
                                    removeOrThrow(body, labelToBasicBlock.get(falseFall.value));
                                }
                                ((Expression) headBody.get(headBody.size() - 1)).setOperand(intermediateJump.value);
                            }
                            else {
                                ((Expression) headBody.get(headBody.size() - 1)).setOperand(falseFall.value);
                            }

                            if (labelGlobalRefCount.get(trueFall.value).getValue() == 1) {
                                removeOrThrow(body, labelToBasicBlock.get(trueFall.value));
                            }

                            removeOrThrow(body, labelToBasicBlock.get(trueLabel.value));
                            removeOrThrow(body, labelToBasicBlock.get(falseLabel.value));

                            return true;
                        }
                    }
                }
            }

            return false;
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="SimplifyTernaryOperatorRoundTwo Step">

    private final static class SimplifyTernaryOperatorRoundTwoOptimization extends AbstractExpressionOptimization {
        protected SimplifyTernaryOperatorRoundTwoOptimization(final DecompilerContext context, final Block method) {
            super(context, method);
        }

        @Override
        public boolean run(final List<Node> body, final Expression head, final int position) {
            final BooleanBox modified = new BooleanBox();
            final Expression simplified = simplify(head, modified);

            if (simplified != head) {
                body.set(position, simplified);
            }

            return modified.get();
        }

        private static Expression simplify(final Expression head, final BooleanBox modified) {
            if (match(head, AstCode.TernaryOp)) {
                return simplifyTernaryDirect(head);
            }

            final List<Expression> arguments = head.getArguments();

            for (int i = 0; i < arguments.size(); i++) {
                final Expression argument = arguments.get(i);
                final Expression simplified = simplify(argument, modified);

                if (simplified != argument) {
                    arguments.set(i, simplified);
                    modified.set(true);
                }
            }

            final AstCode opType = head.getCode();

            if (opType != AstCode.CmpEq && opType != AstCode.CmpNe) {
                return head;
            }

            final Boolean right = matchBooleanConstant(arguments.get(1));

            if (right == null) {
                return head;
            }

            final Expression ternary = arguments.get(0);

            if (ternary.getCode() != AstCode.TernaryOp) {
                return head;
            }

            final Boolean ifTrue = matchBooleanConstant(ternary.getArguments().get(1));
            final Boolean ifFalse = matchBooleanConstant(ternary.getArguments().get(2));

            if (ifTrue == null || ifFalse == null || ifTrue.equals(ifFalse)) {
                return head;
            }

            final boolean invert = !ifTrue.equals(right) ^ opType == AstCode.CmpNe;
            final Expression condition = ternary.getArguments().get(0);

            condition.getRanges().addAll(ternary.getRanges());
            modified.set(true);

            return invert ? new Expression(AstCode.LogicalNot, null, condition.getOffset(), condition)
                          : condition;
        }

        private static Expression simplifyTernaryDirect(final Expression head) {
            final List<Expression> a = new ArrayList<>();

            final StrongBox<Variable> v;
            final StrongBox<Expression> left;
            final StrongBox<Expression> right;

            //
            // c ? (v = a) : (v = b) => v = c ? a : b;
            //
            if (matchGetArguments(head, AstCode.TernaryOp, a)) {
                if (matchGetArgument(a.get(1), AstCode.Store, v = new StrongBox<>(), left = new StrongBox<>()) &&
                    matchStore(a.get(2), v.get(), right = new StrongBox<>())) {

                    final Expression condition = a.get(0);
                    final Expression leftValue = left.value;
                    final Expression rightValue = right.value;

                    final Expression newTernary = new Expression(
                        AstCode.TernaryOp,
                        null,
                        condition.getOffset(),
                        condition,
                        leftValue,
                        rightValue
                    );

                    head.setCode(AstCode.Store);
                    head.setOperand(v.get());
                    head.getArguments().clear();
                    head.getArguments().add(newTernary);

                    newTernary.getRanges().addAll(head.getRanges());

                    return head;
                }
                else {
                    final Boolean ifTrue = matchBooleanConstant(head.getArguments().get(1));
                    final Boolean ifFalse = matchBooleanConstant(head.getArguments().get(2));

                    if (ifTrue == null || ifFalse == null || ifTrue.equals(ifFalse)) {
                        return head;
                    }

                    final boolean invert = Boolean.FALSE.equals(ifTrue);
                    final Expression condition = head.getArguments().get(0);

                    condition.getRanges().addAll(head.getRanges());

                    return invert ? new Expression(AstCode.LogicalNot, null, condition.getOffset(), condition)
                                  : condition;
                }
            }

            return head;
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="SimplifyLogicalNot Step">

    private final static class SimplifyLogicalNotOptimization extends AbstractExpressionOptimization {
        protected SimplifyLogicalNotOptimization(final DecompilerContext context, final Block method) {
            super(context, method);
        }

        @Override
        public final boolean run(final List<Node> body, final Expression head, final int position) {
            final BooleanBox modified = new BooleanBox();
            final Expression simplified = simplifyLogicalNot(head, modified);

            assert simplified == null;

            return modified.get();
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="TransformObjectInitializers Step">

    private final static class TransformObjectInitializersOptimization extends AbstractExpressionOptimization {
        protected TransformObjectInitializersOptimization(final DecompilerContext context, final Block method) {
            super(context, method);
        }

        @Override
        public boolean run(final List<Node> body, final Expression head, final int position) {
            if (position >= body.size() - 1) {
                return false;
            }

            final StrongBox<Variable> v = new StrongBox<>();
            final StrongBox<Expression> newObject = new StrongBox<>();
            final StrongBox<TypeReference> objectType = new StrongBox<>();
            final StrongBox<MethodReference> constructor = new StrongBox<>();
            final List<Expression> arguments = new ArrayList<>();

            if (position < body.size() - 1 &&
                matchGetArgument(head, AstCode.Store, v, newObject) &&
                matchGetOperand(newObject.get(), AstCode.__New, objectType)) {

                final Node next = body.get(position + 1);

                if (matchGetArguments(next, AstCode.InvokeSpecial, constructor, arguments) &&
                    !arguments.isEmpty() &&
                    matchLoad(arguments.get(0), v.get())) {

                    final Expression initExpression = new Expression(AstCode.InitObject, constructor.get(), ((Expression) next).getOffset());

                    arguments.remove(0);
                    initExpression.getArguments().addAll(arguments);
                    initExpression.getRanges().addAll(((Expression) next).getRanges());
                    head.getArguments().set(0, initExpression);
                    body.remove(position + 1);

                    return true;
                }
            }

            if (matchGetArguments(head, AstCode.InvokeSpecial, constructor, arguments) &&
                constructor.get().isConstructor() &&
                !arguments.isEmpty() &&
                matchGetArgument(arguments.get(0), AstCode.Store, v, newObject) &&
                matchGetOperand(newObject.get(), AstCode.__New, objectType)) {

                final Expression initExpression = new Expression(AstCode.InitObject, constructor.get(), newObject.get().getOffset());

                arguments.remove(0);

                initExpression.getArguments().addAll(arguments);
                initExpression.getRanges().addAll(head.getRanges());

                body.set(position, new Expression(AstCode.Store, v.get(), initExpression.getOffset(), initExpression));

                return true;
            }

            return false;
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="MergeDisparateObjectInitializations Step">

    private static boolean mergeDisparateObjectInitializations(final DecompilerContext context, final Block method) {
        final Inlining inlining = new Inlining(context, method);
        final Map<Node, Node> parentLookup = new IdentityHashMap<>();
        final Map<Variable, Expression> newExpressions = new IdentityHashMap<>();

        final StrongBox<Variable> variable = new StrongBox<>();
        final StrongBox<MethodReference> ctor = new StrongBox<>();
        final List<Expression> args = new ArrayList<>();

        boolean anyChanged = false;

        parentLookup.put(method, Node.NULL);

        for (final Node node : method.getSelfAndChildrenRecursive(Node.class)) {
            if (matchStore(node, variable, args) &&
                match(single(args), AstCode.__New)) {

                newExpressions.put(variable.get(), (Expression) node);
            }

            for (final Node child : node.getChildren()) {
                if (parentLookup.containsKey(child)) {
                    throw Error.expressionLinkedFromMultipleLocations(child);
                }

                parentLookup.put(child, node);
            }
        }

        for (final Expression e : method.getSelfAndChildrenRecursive(Expression.class)) {
            if (matchGetArguments(e, AstCode.InvokeSpecial, ctor, args) &&
                ctor.get().isConstructor() &&
                args.size() > 0 &&
                matchLoad(first(args), variable)) {

                final Expression storeNew = newExpressions.get(variable.value);

                if (storeNew != null &&
                    Inlining.count(inlining.storeCounts, variable.value) == 1) {

                    final Node parent = parentLookup.get(storeNew);

                    if (parent instanceof Block || parent instanceof BasicBlock) {
                        final List<Node> body;

                        if (parent instanceof Block) {
                            body = ((Block) parent).getBody();
                        }
                        else {
                            body = ((BasicBlock) parent).getBody();
                        }

                        boolean moveInitToNew = false;

                        if (parentLookup.get(e) == parent) {
                            final int newIndex = body.indexOf(storeNew);
                            final int initIndex = body.indexOf(e);

                            if (initIndex > newIndex) {
                                for (int i = newIndex + 1; i < initIndex; i++) {
                                    if (references(body.get(i), variable.value)) {
                                        moveInitToNew = true;
                                        break;
                                    }
                                }
                            }
                        }

                        final Expression toRemove = moveInitToNew ? e : storeNew;
                        final Expression toRewrite = moveInitToNew ? storeNew : e;

                        final List<Expression> arguments = e.getArguments();
                        final Expression initExpression = new Expression(AstCode.InitObject, ctor.get(), storeNew.getOffset());

                        arguments.remove(0);

                        initExpression.getArguments().addAll(arguments);
                        initExpression.getRanges().addAll(e.getRanges());

                        body.remove(toRemove);

                        toRewrite.setCode(AstCode.Store);
                        toRewrite.setOperand(variable.value);
                        toRewrite.getArguments().clear();
                        toRewrite.getArguments().add(initExpression);

                        anyChanged = true;
                    }
                }
            }
        }

        return anyChanged;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="TransformArrayInitializers Step">

    private final static class TransformArrayInitializersOptimization extends AbstractExpressionOptimization {
        protected TransformArrayInitializersOptimization(final DecompilerContext context, final Block method) {
            super(context, method);
        }

        @Override
        public boolean run(final List<Node> body, final Expression head, final int position) {
            final StrongBox<Variable> v = new StrongBox<>();
            final StrongBox<Expression> newArray = new StrongBox<>();

            if (matchGetArgument(head, AstCode.Store, v, newArray) &&
                match(newArray.get(), AstCode.InitArray)) {

                return tryRefineArrayInitialization(body, head, position);
            }

            final StrongBox<Variable> v3 = new StrongBox<>();
            final StrongBox<TypeReference> elementType = new StrongBox<>();
            final StrongBox<Expression> lengthExpression = new StrongBox<>();
            final StrongBox<Number> arrayLength = new StrongBox<>();

            if (matchGetArgument(head, AstCode.Store, v, newArray) &&
                matchGetArgument(newArray.get(), AstCode.NewArray, elementType, lengthExpression) &&
                matchGetOperand(lengthExpression.get(), AstCode.LdC, Number.class, arrayLength) &&
                arrayLength.get().intValue() > 0) {

                final int actualArrayLength = arrayLength.get().intValue();

                final StrongBox<Number> arrayPosition = new StrongBox<>();
                final List<Expression> initializers = new ArrayList<>();

                int instructionsToRemove = 0;

                final boolean hasInnocuousStackAssignment;

                if (position < body.size() - 1) {
                    final StrongBox<Variable> stackVariable = new StrongBox<>();
                    final StrongBox<Expression> stackAssignment = new StrongBox<>();

                    hasInnocuousStackAssignment = matchStore(body.get(position + 1), stackVariable, stackAssignment) &&
                                                  matchLoad(stackAssignment.value, v.value) &&
                                                  stackVariable.value.isGeneratedStackVariable();
                }
                else {
                    hasInnocuousStackAssignment = false;
                }

                final int storeElementStart = hasInnocuousStackAssignment ? 2 : 1;

                for (int j = position + storeElementStart; j < body.size(); j++) {
                    final Node node = body.get(j);

                    if (!(node instanceof Expression)) {
                        continue;
                    }

                    final Expression next = (Expression) node;

                    if (next.getCode() == AstCode.StoreElement &&
                        matchGetOperand(next.getArguments().get(0), AstCode.Load, v3) &&
                        v3.get() == v.get() &&
                        matchGetOperand(next.getArguments().get(1), AstCode.LdC, Number.class, arrayPosition) &&
                        arrayPosition.get().intValue() >= initializers.size() &&
                        !next.getArguments().get(2).containsReferenceTo(v3.get())) {

                        while (initializers.size() < arrayPosition.get().intValue()) {
                            initializers.add(new Expression(AstCode.DefaultValue, elementType.get(), next.getOffset()));
                        }

                        initializers.add(next.getArguments().get(2));
                        instructionsToRemove++;
                    }
                    else {
                        break;
                    }
                }

                if (initializers.size() < actualArrayLength &&
                    initializers.size() >= actualArrayLength / 2) {

                    //
                    // Some compilers like Eclipse emit sparse array initializers.  If at least half
                    // of the elements in the array are initialized, emit an InitArray expression.
                    // I think this is a reasonable threshold.
                    //

                    while (initializers.size() < actualArrayLength) {
                        initializers.add(new Expression(AstCode.DefaultValue, elementType.get(), head.getOffset()));
                    }
                }

                if (initializers.size() == actualArrayLength) {
                    final TypeReference arrayType = elementType.get().makeArrayType();

                    head.getArguments().set(0, new Expression(AstCode.InitArray, arrayType, head.getOffset(), initializers));

                    for (int i = 0; i < instructionsToRemove; i++) {
                        body.remove(position + storeElementStart);
                    }

                    new Inlining(context, method).inlineIfPossible(body, new MutableInteger(position));
                    return true;
                }
            }

            return false;
        }

        private boolean tryRefineArrayInitialization(final List<Node> body, final Expression head, final int position) {
            final StrongBox<Variable> v = new StrongBox<>();
            final List<Expression> a = new ArrayList<>();
            final StrongBox<TypeReference> arrayType = new StrongBox<>();

            if (matchGetArguments(head, AstCode.Store, v, a) &&
                matchGetArguments(a.get(0), AstCode.InitArray, arrayType, a)) {

                final Expression initArray = head.getArguments().get(0);
                final List<Expression> initializers = initArray.getArguments();
                final int actualArrayLength = initializers.size();
                final StrongBox<Integer> arrayPosition = new StrongBox<>();

                for (int j = position + 1; j < body.size(); j++) {
                    final Node node = body.get(j);

                    if (matchGetArguments(node, AstCode.StoreElement, a) &&
                        matchLoad(a.get(0), v.get()) &&
                        !a.get(2).containsReferenceTo(v.get()) &&
                        matchGetOperand(a.get(1), AstCode.LdC, Integer.class, arrayPosition) &&
                        arrayPosition.get() >= 0 &&
                        arrayPosition.get() < actualArrayLength &&
                        match(initializers.get(arrayPosition.get()), AstCode.DefaultValue)) {

                        initializers.set(arrayPosition.get(), a.get(2));
                        body.remove(j--);
                    }
                    else {
                        break;
                    }
                }
            }

            return false;
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="MakeAssignmentExpressions Step">

    private final static class MakeAssignmentExpressionsOptimization extends AbstractExpressionOptimization {
        protected MakeAssignmentExpressionsOptimization(final DecompilerContext context, final Block method) {
            super(context, method);
        }

        @Override
        public boolean run(final List<Node> body, final Expression head, final int position) {
            //
            // ev = ...; store(v, ev) => ev = store(v, ...)
            //

            final StrongBox<Variable> ev = new StrongBox<>();
            final StrongBox<Expression> initializer = new StrongBox<>();

            final Node next = getOrDefault(body, position + 1);
            final StrongBox<Variable> v = new StrongBox<>();
            final StrongBox<Expression> storeArgument = new StrongBox<>();

            if (matchGetArgument(head, AstCode.Store, ev, initializer) &&
                !match(initializer.value, AstCode.__New)) {

                if (matchGetArgument(next, AstCode.Store, v, storeArgument) &&
                    matchLoad(storeArgument.get(), ev.get())) {

                    final Expression nextExpression = (Expression) next;
                    final Node store2 = getOrDefault(body, position + 2);

                    if (canConvertStoreToAssignment(store2, ev.get())) {
                        //
                        // e = ...; store(v1, e); anyStore(v2, e) => store(v1, anyStore(v2, ...)
                        //

                        final Inlining inlining = new Inlining(context, method);
                        final MutableInteger loadCounts = inlining.loadCounts.get(ev.get());
                        final MutableInteger storeCounts = inlining.storeCounts.get(ev.get());

                        if (loadCounts != null &&
                            loadCounts.getValue() == 2 &&
                            storeCounts != null &&
                            storeCounts.getValue() == 1) {

                            final Expression storeExpression = (Expression) store2;

                            body.remove(position + 2);  // remove store2
                            body.remove(position);      // remove ev = ...

                            nextExpression.getArguments().set(0, storeExpression);
                            storeExpression.getArguments().set(storeExpression.getArguments().size() - 1, initializer.get());

                            inlining.inlineIfPossible(body, new MutableInteger(position));

                            return true;
                        }
                    }

                    body.remove(position + 1);  // remove store

                    nextExpression.getArguments().set(0, initializer.get());
                    ((Expression) body.get(position)).getArguments().set(0, nextExpression);

                    return true;
                }

                if (match(next, AstCode.PutStatic)) {
                    final Expression nextExpression = (Expression) next;

                    //
                    // ev = ...; putstatic(f, ev) => ev = putstatic(f, ...)
                    //

                    if (matchLoad(nextExpression.getArguments().get(0), ev.get())) {
                        body.remove(position + 1);  // remove putstatic

                        nextExpression.getArguments().set(0, initializer.get());
                        ((Expression) body.get(position)).getArguments().set(0, nextExpression);

                        return true;
                    }
                }

                return false;
            }

            final StrongBox<Expression> equivalentLoad = new StrongBox<>();

            if (matchAssignment(head, initializer, equivalentLoad) &&
                next instanceof Expression) {

                if (equivalentLoad.get().getCode() == AstCode.GetField) {
                    final FieldReference field = (FieldReference) equivalentLoad.get().getOperand();
                    final FieldDefinition resolvedField = field != null ? field.resolve() : null;

                    if (resolvedField != null && resolvedField.isSynthetic()) {
                        return false;
                    }
                }

                final boolean isLoad = matchLoad(initializer.value, v);
                final ArrayDeque<Expression> agenda = new ArrayDeque<>();

                agenda.push((Expression) next);

            processNext:
                while (!agenda.isEmpty()) {
                    final Expression e = agenda.removeFirst();

                    if (e.getCode().isShortCircuiting() || e.getCode().isStore() || e.getCode().isFieldWrite()) {
                        break;
                    }

                    final List<Expression> arguments = e.getArguments();

                    for (int i = 0; i < arguments.size(); i++) {
                        final Expression a = arguments.get(i);

                        if (a.isEquivalentTo(equivalentLoad.value) ||
                            isLoad && matchLoad(a, v.get()) ||
                            (Inlining.hasNoSideEffect(initializer.get()) &&
                             a.isEquivalentTo(initializer.get()) &&
                             initializer.get().getInferredType() != null &&
                             MetadataHelper.isSameType(initializer.get().getInferredType(), a.getInferredType(), true))) {

                            arguments.set(i, head);
                            body.remove(position);
                            return true;
                        }

                        if (!Inlining.isSafeForInlineOver(a, head)) {
                            break processNext;
                        }

                        agenda.push(a);
                    }
                }
            }

            return false;
        }

        private boolean canConvertStoreToAssignment(final Node store, final Variable variable) {
            if (store instanceof Expression) {
                final Expression storeExpression = (Expression) store;

                switch (storeExpression.getCode()) {
                    case Store:
                    case PutStatic:
                    case PutField:
                    case StoreElement:
                        return matchLoad(lastOrDefault(storeExpression.getArguments()), variable);
                }
            }

            return false;
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="IntroducePostIncrement Step">

    private final static class IntroducePostIncrementOptimization extends AbstractExpressionOptimization {
        protected IntroducePostIncrementOptimization(final DecompilerContext context, final Block method) {
            super(context, method);
        }

        @Override
        public boolean run(final List<Node> body, final Expression head, final int position) {
            boolean modified = introducePostIncrementForVariables(body, head, position);

            assert body.get(position) == head;

            if (position > 0) {
                final Expression newExpression = introducePostIncrementForInstanceFields(head, body.get(position - 1));

                if (newExpression != null) {
                    modified = true;
                    body.remove(position);
                    new Inlining(context, method).inlineIfPossible(body, new MutableInteger(position - 1));
                }
            }

            return modified;
        }

        private boolean introducePostIncrementForVariables(final List<Node> body, final Expression e, final int position) {
            //
            // Works for local variables and static fields:
            //
            // e = load(i); inc(i, 1) => e = postincrement(i, 1)
            //   --or--
            // e = load(i); store(i, add(e, ldc(1)) => e = postincrement(i, 1)
            //

            final StrongBox<Variable> variable = new StrongBox<>();
            final StrongBox<Expression> initializer = new StrongBox<>();

            if (!matchGetArgument(e, AstCode.Store, variable, initializer) || !variable.get().isGenerated()) {
                return false;
            }

            final Node next = getOrDefault(body, position + 1);

            if (!(next instanceof Expression)) {
                return false;
            }

            final Expression nextExpression = (Expression) next;
            final AstCode loadCode = initializer.get().getCode();
            final AstCode storeCode = nextExpression.getCode();

            boolean recombineVariables = false;

            switch (loadCode) {
                case Load: {
                    if (storeCode != AstCode.Inc && storeCode != AstCode.Store) {
                        return false;
                    }

                    final Variable loadVariable = (Variable) initializer.get().getOperand();
                    final Variable storeVariable = (Variable) nextExpression.getOperand();

                    if (loadVariable != storeVariable) {
                        if (loadVariable.getOriginalVariable() != null &&
                            loadVariable.getOriginalVariable() == storeVariable.getOriginalVariable()) {

                            recombineVariables = true;
                        }
                        else {
                            return false;
                        }
                    }

                    break;
                }

                case GetStatic: {
                    if (storeCode != AstCode.PutStatic) {
                        return false;
                    }

                    final FieldReference initializerOperand = (FieldReference) initializer.get().getOperand();
                    final FieldReference nextOperand = (FieldReference) nextExpression.getOperand();

                    if (initializerOperand == null ||
                        nextOperand == null ||
                        !StringUtilities.equals(initializerOperand.getFullName(), nextOperand.getFullName())) {

                        return false;
                    }

                    break;
                }

                default: {
                    return false;
                }
            }

            final Expression add = storeCode == AstCode.Inc ? nextExpression : nextExpression.getArguments().get(0);
            final StrongBox<Number> incrementAmount = new StrongBox<>();
            final AstCode incrementCode = getIncrementCode(add, incrementAmount);

            if (incrementCode == AstCode.Nop || !(match(add, AstCode.Inc) || match(add.getArguments().get(0), AstCode.Load))) {
                return false;
            }

            if (recombineVariables) {
                replaceVariables(
                    method,
                    new Function<Variable, Variable>() {
                        @Override
                        public Variable apply(final Variable old) {
                            return old == nextExpression.getOperand() ? (Variable) initializer.get().getOperand() : old;
                        }
                    }
                );
            }

            e.getArguments().set(
                0,
                new Expression(incrementCode, incrementAmount.get(), initializer.get().getOffset(), initializer.get())
            );

            body.remove(position + 1);
            return true;
        }

        @SuppressWarnings("UnusedParameters")
        private Expression introducePostIncrementForInstanceFields(final Expression e, final Node previous) {
            //
            // t = getfield(field, load(p)); putfield(field, load(p), add(load(t), ldc(1)))
            //   => store(t, postincrement(1, load(field, load(p))))
            //
            // Also works for array elements:
            //
            // t = loadelement(T, load(p), load(i)); storeelement(T, load(p), load(i), add(load(t), ldc(1)))
            //   => store(t, postincrement(1, loadelement(load(p), load(i))))
            //

            if (!(previous instanceof Expression)) {
                return null;
            }

            final Expression p = (Expression) previous;
            final StrongBox<Variable> t = new StrongBox<>();
            final StrongBox<Expression> initialValue = new StrongBox<>();

            if (!matchGetArgument(p, AstCode.Store, t, initialValue) ||
                initialValue.get().getCode() != AstCode.GetField && initialValue.get().getCode() != AstCode.LoadElement) {

                return null;
            }

            final AstCode code = e.getCode();
            final Variable tempVariable = t.get();

            if (code != AstCode.PutField && code != AstCode.StoreElement) {
                return null;
            }

            //
            // Test that all arguments except the last are load (1 arg for fields, 2 args for arrays).
            //

            final List<Expression> arguments = e.getArguments();

            for (int i = 0, n = arguments.size() - 1; i < n; i++) {
                if (arguments.get(i).getCode() != AstCode.Load) {
                    return null;
                }
            }

            final StrongBox<Number> incrementAmount = new StrongBox<>();
            final Expression add = arguments.get(arguments.size() - 1);
            final AstCode incrementCode = getIncrementCode(add, incrementAmount);

            if (incrementCode == AstCode.Nop) {
                return null;
            }

            final List<Expression> addArguments = add.getArguments();

            if (!matchGetOperand(addArguments.get(0), AstCode.Load, t) || t.get() != tempVariable) {
                return null;
            }

            if (e.getCode() == AstCode.PutField) {
                if (initialValue.get().getCode() != AstCode.GetField) {
                    return null;
                }

                //
                // There might be two different FieldReference instances, so we compare the field's signatures:
                //
                final FieldReference getField = (FieldReference) initialValue.get().getOperand();
                final FieldReference setField = (FieldReference) e.getOperand();

                if (!StringUtilities.equals(getField.getFullName(), setField.getFullName())) {
                    return null;
                }
            }
            else if (initialValue.get().getCode() != AstCode.LoadElement) {
                return null;
            }

            final List<Expression> initialValueArguments = initialValue.get().getArguments();

            assert (arguments.size() - 1 == initialValueArguments.size());

            for (int i = 0, n = initialValueArguments.size(); i < n; i++) {
                if (!matchLoad(initialValueArguments.get(i), (Variable) arguments.get(i).getOperand())) {
                    return null;
                }
            }

            p.getArguments().set(0, new Expression(AstCode.PostIncrement, incrementAmount.get(), initialValue.get().getOffset(), initialValue.get()));

            return p;
        }

        private AstCode getIncrementCode(final Expression add, final StrongBox<Number> incrementAmount) {
            final AstCode incrementCode;
            final Expression amountArgument;
            final boolean decrement;

            switch (add.getCode()) {
                case Add: {
                    incrementCode = AstCode.PostIncrement;
                    amountArgument = add.getArguments().get(1);
                    decrement = false;
                    break;
                }

                case Sub: {
                    incrementCode = AstCode.PostIncrement;
                    amountArgument = add.getArguments().get(1);
                    decrement = true;
                    break;
                }

                case Inc: {
                    incrementCode = AstCode.PostIncrement;
                    amountArgument = add.getArguments().get(0);
                    decrement = false;
                    break;
                }

                default: {
                    return AstCode.Nop;
                }
            }

            if (matchGetOperand(amountArgument, AstCode.LdC, incrementAmount) &&
                !(incrementAmount.get() instanceof Float ||
                  incrementAmount.get() instanceof Double)) {

                if (incrementAmount.get().longValue() == 1L || incrementAmount.get().longValue() == -1L) {
                    incrementAmount.set(
                        decrement ? -incrementAmount.get().intValue()
                                  : incrementAmount.get().intValue()
                    );
                    return incrementCode;
                }
            }

            return AstCode.Nop;
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="FlattenBasicBlocks Step">

    @SuppressWarnings("StatementWithEmptyBody")
    private static void flattenBasicBlocks(final Node node) {
        if (node instanceof Block) {
            final Block block = (Block) node;
            final List<Node> flatBody = new ArrayList<>();

            for (final Node child : block.getChildren()) {
                flattenBasicBlocks(child);

                if (child instanceof BasicBlock) {
                    final BasicBlock childBasicBlock = (BasicBlock) child;
                    final Node firstChild = firstOrDefault(childBasicBlock.getBody());
                    final Node lastChild = lastOrDefault(childBasicBlock.getBody());

                    if (!(firstChild instanceof Label)) {
                        throw new IllegalStateException("Basic block must start with a label.");
                    }

                    if (lastChild instanceof Expression && !lastChild.isUnconditionalControlFlow()) {
                        throw new IllegalStateException("Basic block must end with an unconditional branch.");
                    }

                    flatBody.addAll(childBasicBlock.getBody());
                }
                else {
                    flatBody.add(child);
                }
            }

            block.setEntryGoto(null);
            block.getBody().clear();
            block.getBody().addAll(flatBody);
        }
        else if (node != null) {
            for (final Node child : node.getChildren()) {
                flattenBasicBlocks(child);
            }
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="DuplicateReturns Step">

    private static void duplicateReturnStatements(final Block method) {
        final List<Node> methodBody = method.getBody();
        final Map<Node, Node> nextSibling = new IdentityHashMap<>();
        final StrongBox<Object> constant = new StrongBox<>();
        final StrongBox<Variable> localVariable = new StrongBox<>();
        final StrongBox<Label> targetLabel = new StrongBox<>();
        final List<Expression> returnArguments = new ArrayList<>();

        //
        // Build navigation data.
        //
        for (final Block block : method.getSelfAndChildrenRecursive(Block.class)) {
            final List<Node> body = block.getBody();

            for (int i = 0; i < body.size() - 1; i++) {
                final Node current = body.get(i);

                if (current instanceof Label) {
                    nextSibling.put(current, body.get(i + 1));
                }
            }
        }

        //
        // Duplicate returns.
        //
        for (final Block block : method.getSelfAndChildrenRecursive(Block.class)) {
            final List<Node> body = block.getBody();

            for (int i = 0; i < body.size(); i++) {
                final Node node = body.get(i);

                if (matchGetOperand(node, AstCode.Goto, targetLabel)) {
                    //
                    // Skip extra labels.
                    //
                    while (nextSibling.get(targetLabel.get()) instanceof Label) {
                        targetLabel.accept((Label) nextSibling.get(targetLabel.get()));
                    }

                    //
                    // Inline return statement.
                    //
                    final Node target = nextSibling.get(targetLabel.get());

                    if (target != null &&
                        matchGetArguments(target, AstCode.Return, returnArguments)) {

                        if (returnArguments.isEmpty()) {
                            body.set(
                                i,
                                new Expression(AstCode.Return, null, Expression.MYSTERY_OFFSET)
                            );
                        }
                        else if (matchGetOperand(returnArguments.get(0), AstCode.Load, localVariable)) {
                            body.set(
                                i,
                                new Expression(AstCode.Return, null, Expression.MYSTERY_OFFSET, new Expression(AstCode.Load, localVariable.get(), Expression.MYSTERY_OFFSET))
                            );
                        }
                        else if (matchGetOperand(returnArguments.get(0), AstCode.LdC, constant)) {
                            body.set(
                                i,
                                new Expression(AstCode.Return, null, Expression.MYSTERY_OFFSET, new Expression(AstCode.LdC, constant.get(), Expression.MYSTERY_OFFSET))
                            );
                        }
                    }
                    else if (!methodBody.isEmpty() && methodBody.get(methodBody.size() - 1) == targetLabel.get()) {
                        //
                        // It exits the main method, so it is effectively a return.
                        //
                        body.set(i, new Expression(AstCode.Return, null, Expression.MYSTERY_OFFSET));
                    }
                }
            }
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ReduceIfNesting Step">

    private static void reduceIfNesting(final Node node) {
        if (node instanceof Block) {
            final Block block = (Block) node;
            final List<Node> blockBody = block.getBody();

            for (int i = 0; i < blockBody.size(); i++) {
                final Node n = blockBody.get(i);

                if (!(n instanceof Condition)) {
                    continue;
                }

                final Condition condition = (Condition) n;

                final Node trueEnd = lastOrDefault(condition.getTrueBlock().getBody());
                final Node falseEnd = lastOrDefault(condition.getFalseBlock().getBody());

                final boolean trueExits = trueEnd != null && trueEnd.isUnconditionalControlFlow();
                final boolean falseExits = falseEnd != null && falseEnd.isUnconditionalControlFlow();

                if (trueExits) {
                    //
                    // Move the false block after the condition.
                    //
                    blockBody.addAll(i + 1, condition.getFalseBlock().getChildren());
                    condition.setFalseBlock(new Block());
                }
                else if (falseExits) {
                    //
                    // Move the true block after the condition.
                    //
                    blockBody.addAll(i + 1, condition.getTrueBlock().getChildren());
                    condition.setTrueBlock(new Block());
                }

                //
                // Eliminate empty true block.
                //
                if (condition.getTrueBlock().getChildren().isEmpty() && !condition.getFalseBlock().getChildren().isEmpty()) {
                    final Block temp = condition.getTrueBlock();
                    final Expression conditionExpression = condition.getCondition();

                    condition.setTrueBlock(condition.getFalseBlock());
                    condition.setFalseBlock(temp);
                    condition.setCondition(simplifyLogicalNot(new Expression(AstCode.LogicalNot, null, conditionExpression.getOffset(), conditionExpression)));
                }
            }
        }

        for (final Node child : node.getChildren()) {
            if (child != null && !(child instanceof Expression)) {
                reduceIfNesting(child);
            }
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="RecombineVariables Step">

    private static void recombineVariables(final Block method) {
        final Map<VariableDefinition, Variable> map = new IdentityHashMap<>();

        replaceVariables(
            method,
            new Function<Variable, Variable>() {
                @Override
                public final Variable apply(final Variable v) {
                    final VariableDefinition originalVariable = v.getOriginalVariable();

                    if (originalVariable == null) {
                        return v;
                    }

                    Variable combinedVariable = map.get(originalVariable);

                    if (combinedVariable == null) {
                        map.put(originalVariable, v);
                        combinedVariable = v;
                    }

                    return combinedVariable;
                }
            }
        );
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="InlineLambdas Step">

    private final static class InlineLambdasOptimization extends AbstractExpressionOptimization {
        private final MutableInteger _lambdaCount = new MutableInteger();

        protected InlineLambdasOptimization(final DecompilerContext context, final Block method) {
            super(context, method);
        }

        @Override
        public boolean run(final List<Node> body, final Expression head, final int position) {
            final StrongBox<DynamicCallSite> c = new StrongBox<>();
            final List<Expression> a = new ArrayList<>();

            boolean modified = false;

            for (final Expression e : head.getChildrenAndSelfRecursive(Expression.class)) {
                if (matchGetArguments(e, AstCode.InvokeDynamic, c, a)) {
                    final Lambda lambda = tryInlineLambda(e, c.value);

                    if (lambda != null) {
                        modified = true;
                    }
                }
            }

            return modified;
        }

        private Lambda tryInlineLambda(final Expression site, final DynamicCallSite callSite) {
            final MethodReference bootstrapMethod = callSite.getBootstrapMethod();

            if ("java/lang/invoke/LambdaMetafactory".equals(bootstrapMethod.getDeclaringType().getInternalName()) &&
                (StringUtilities.equals("metafactory", bootstrapMethod.getName(), StringComparison.OrdinalIgnoreCase) ||
                 StringUtilities.equals("altMetafactory", bootstrapMethod.getName(), StringComparison.OrdinalIgnoreCase)) &&
                callSite.getBootstrapArguments().size() >= 3 &&
                callSite.getBootstrapArguments().get(1) instanceof MethodHandle) {

                final MethodHandle targetMethodHandle = (MethodHandle) callSite.getBootstrapArguments().get(1);
                final MethodReference targetMethod = targetMethodHandle.getMethod();
                final MethodDefinition resolvedMethod = targetMethod.resolve();

                if (resolvedMethod == null ||
                    resolvedMethod.getBody() == null ||
                    !resolvedMethod.isSynthetic()) {

                    return null;
                }

                final TypeReference functionType = callSite.getMethodType().getReturnType();

                final List<MethodReference> methods = MetadataHelper.findMethods(
                    functionType,
                    MetadataFilters.matchName(callSite.getMethodName())
                );

                MethodReference functionMethod = null;

                for (final MethodReference m : methods) {
                    final MethodDefinition r = m.resolve();

                    if (r != null && r.isAbstract() && !r.isStatic() && !r.isDefault()) {
                        functionMethod = r;
                        break;
                    }
                }

                if (functionMethod == null) {
                    return null;
                }

                final DecompilerContext innerContext = new DecompilerContext(context.getSettings());

                innerContext.setCurrentType(resolvedMethod.getDeclaringType());
                innerContext.setCurrentMethod(resolvedMethod);

                final MethodBody methodBody = resolvedMethod.getBody();
                final List<ParameterDefinition> parameters = resolvedMethod.getParameters();
                final Variable[] parameterMap = new Variable[methodBody.getMaxLocals()];

                final List<Node> nodes = new ArrayList<>();
                final Block body = new Block();
                final Lambda lambda = new Lambda(body, functionType);

                lambda.setMethod(functionMethod);
                lambda.setCallSite(callSite);

                final List<Variable> lambdaParameters = lambda.getParameters();

                if (resolvedMethod.hasThis()) {
                    final Variable variable = new Variable();

                    variable.setName("this");
                    variable.setType(context.getCurrentMethod().getDeclaringType());
                    variable.setOriginalParameter(context.getCurrentMethod().getBody().getThisParameter());

                    parameterMap[0] = variable;

                    lambdaParameters.add(variable);
                }

                for (final ParameterDefinition p : parameters) {
                    final Variable variable = new Variable();

                    variable.setName(p.getName());
                    variable.setType(p.getParameterType());
                    variable.setOriginalParameter(p);
                    variable.setLambdaParameter(true);

                    parameterMap[p.getSlot()] = variable;

                    lambdaParameters.add(variable);
                }

                final List<Expression> arguments = site.getArguments();

                for (int i = 0; i < arguments.size(); i++) {
                    final Variable v = lambdaParameters.get(0);

                    v.setOriginalParameter(null);
                    v.setGenerated(true);

                    final Expression argument = arguments.get(i).clone();

                    nodes.add(new Expression(AstCode.Store, v, argument.getOffset(), argument));

                    lambdaParameters.remove(0);
                }

                arguments.clear();
                nodes.addAll(AstBuilder.build(methodBody, true, innerContext));
                body.getBody().addAll(nodes);

                for (final Expression e : body.getSelfAndChildrenRecursive(Expression.class)) {
                    final Object operand = e.getOperand();

                    if (operand instanceof Variable) {
                        final Variable oldVariable = (Variable) operand;

                        if (oldVariable.isParameter() &&
                            oldVariable.getOriginalParameter().getMethod() == resolvedMethod) {

                            final Variable newVariable = parameterMap[oldVariable.getOriginalParameter().getSlot()];

                            if (newVariable != null) {
                                e.setOperand(newVariable);
                            }
                        }
                    }
                }

                AstOptimizer.optimize(innerContext, body, AstOptimizationStep.InlineVariables2);

                final int lambdaId = _lambdaCount.increment().getValue();
                final Set<Label> renamedLabels = new HashSet<>();

                for (final Node n : body.getSelfAndChildrenRecursive()) {
                    if (n instanceof Label) {
                        final Label label = (Label) n;
                        if (renamedLabels.add(label)) {
                            label.setName(label.getName() + "_" + lambdaId);
                        }
                        continue;
                    }

                    if (!(n instanceof Expression)) {
                        continue;
                    }

                    final Expression e = (Expression) n;
                    final Object operand = e.getOperand();

                    if (operand instanceof Label) {
                        final Label label = (Label) operand;
                        if (renamedLabels.add(label)) {
                            label.setName(label.getName() + "_" + lambdaId);
                        }
                    }
                    else if (operand instanceof Label[]) {
                        for (final Label label : (Label[]) operand) {
                            if (renamedLabels.add(label)) {
                                label.setName(label.getName() + "_" + lambdaId);
                            }
                        }
                    }

                    if (match(e, AstCode.Return)) {
                        e.putUserData(AstKeys.PARENT_LAMBDA_BINDING, site);
                    }
                }

                site.setCode(AstCode.Bind);
                site.setOperand(lambda);

                final List<Range> ranges = site.getRanges();

                for (final Expression e : lambda.getSelfAndChildrenRecursive(Expression.class)) {
                    ranges.addAll(e.getRanges());
                }

                return lambda;
            }

            return null;
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="JoinBranchConditions Step">

    private static final class JoinBranchConditionsOptimization extends AbstractBranchBlockOptimization {
        public JoinBranchConditionsOptimization(final DecompilerContext context, final Block method) {
            super(context, method);
        }

        @Override
        protected boolean run(
            final List<Node> body,
            final BasicBlock branchBlock,
            final Expression branchCondition,
            final Label thenLabel,
            final Label elseLabel,
            final boolean negate) {

            if (labelGlobalRefCount.get(elseLabel).getValue() != 1) {
                return false;
            }

            final BasicBlock elseBlock = labelToBasicBlock.get(elseLabel);

            if (matchSingleAndBreak(elseBlock, AstCode.IfTrue, label1, expression, label2)) {
                final Label elseThenLabel = label1.get();
                final Label elseElseLabel = label2.get();

                final Expression elseCondition = expression.get();

                return runCore(body, branchBlock, branchCondition, thenLabel, elseLabel, elseCondition, negate, elseThenLabel, elseElseLabel, false) ||
                       runCore(body, branchBlock, branchCondition, thenLabel, elseLabel, elseCondition, negate, elseElseLabel, elseThenLabel, true);
            }

            return false;
        }

        private boolean runCore(
            final List<Node> body,
            final BasicBlock branchBlock,
            final Expression branchCondition,
            final Label thenLabel,
            final Label elseLabel,
            final Expression elseCondition,
            final boolean negateFirst,
            final Label elseThenLabel,
            final Label elseElseLabel,
            final boolean negateSecond) {

            final BasicBlock thenBlock = labelToBasicBlock.get(thenLabel);
            final BasicBlock elseThenBlock = labelToBasicBlock.get(elseThenLabel);

            BasicBlock alsoRemove = null;
            Label alsoDecrement = null;

            if (elseThenBlock != thenBlock) {
                if (matchSimpleBreak(elseThenBlock, label1) &&
                    labelGlobalRefCount.get(label1.get()).getValue() <= 2) {

                    final BasicBlock intermediateBlock = labelToBasicBlock.get(label1.get());

                    if (intermediateBlock != thenBlock) {
                        return false;
                    }

                    alsoRemove = elseThenBlock;
                    alsoDecrement = label1.get();
                }
                else {
                    return false;
                }
            }

            final BasicBlock elseBlock = labelToBasicBlock.get(elseLabel);

            final Expression logicExpression = new Expression(
                AstCode.LogicalOr,
                null,
                Expression.MYSTERY_OFFSET,
                negateFirst ? simplifyLogicalNotArgument(branchCondition) ? branchCondition
                                                                          : new Expression(AstCode.LogicalNot, null, branchCondition.getOffset(), branchCondition)
                            : branchCondition,
                negateSecond ? simplifyLogicalNotArgument(elseCondition) ? elseCondition
                                                                         : new Expression(AstCode.LogicalNot, null, elseCondition.getOffset(), elseCondition)
                             : elseCondition
            );

            final List<Node> branchBody = branchBlock.getBody();

            removeTail(branchBody, AstCode.IfTrue, AstCode.Goto);

            branchBody.add(new Expression(AstCode.IfTrue, thenLabel, logicExpression.getOffset(), logicExpression));
            branchBody.add(new Expression(AstCode.Goto, elseElseLabel, Expression.MYSTERY_OFFSET));

            labelGlobalRefCount.get(elseLabel).decrement();
            labelGlobalRefCount.get(elseThenLabel).decrement();

            body.remove(elseBlock);

            if (alsoRemove != null) {
                body.remove(alsoRemove);
            }

            if (alsoDecrement != null) {
                labelGlobalRefCount.get(alsoDecrement).decrement();
            }

            return true;
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Optimization Helpers">

    private interface BasicBlockOptimization {
        boolean run(final List<Node> body, final BasicBlock head, final int position);
    }

    private interface ExpressionOptimization {
        boolean run(final List<Node> body, final Expression head, final int position);
    }

    @SuppressWarnings("ProtectedField")
    private static abstract class AbstractBasicBlockOptimization implements BasicBlockOptimization {
        protected final static BasicBlock EMPTY_BLOCK = new BasicBlock();

        protected final Map<Label, MutableInteger> labelGlobalRefCount = new DefaultMap<>(MutableInteger.SUPPLIER);
        protected final Map<Label, BasicBlock> labelToBasicBlock = new DefaultMap<>(Suppliers.forValue(EMPTY_BLOCK));

        protected final DecompilerContext context;
        protected final IMetadataResolver resolver;
        protected final Block method;

        protected AbstractBasicBlockOptimization(final DecompilerContext context, final Block method) {
            this.context = VerifyArgument.notNull(context, "context");
            this.resolver = context.getCurrentType().getResolver();
            this.method = VerifyArgument.notNull(method, "method");

            for (final Expression e : method.getSelfAndChildrenRecursive(Expression.class)) {
                if (e.isBranch()) {
                    for (final Label target : e.getBranchTargets()) {
                        labelGlobalRefCount.get(target).increment();
                    }
                }
            }

            for (final BasicBlock basicBlock : method.getSelfAndChildrenRecursive(BasicBlock.class)) {
                for (final Node child : basicBlock.getChildren()) {
                    if (child instanceof Label) {
                        labelToBasicBlock.put((Label) child, basicBlock);
                    }
                }
            }
        }
    }

    @SuppressWarnings("ProtectedField")
    private static abstract class AbstractExpressionOptimization implements ExpressionOptimization {
        protected final DecompilerContext context;
        protected final MetadataSystem metadataSystem;
        protected final Block method;

        protected AbstractExpressionOptimization(final DecompilerContext context, final Block method) {
            this.context = VerifyArgument.notNull(context, "context");
            this.metadataSystem = MetadataSystem.instance();
            this.method = VerifyArgument.notNull(method, "method");
        }
    }

    private static boolean runOptimization(final Block block, final BasicBlockOptimization optimization) {
        boolean modified = false;

        final List<Node> body = block.getBody();

        for (int i = body.size() - 1; i >= 0; i--) {
            if (i < body.size() && optimization.run(body, (BasicBlock) body.get(i), i)) {
                modified = true;
                ++i;
            }
        }

        return modified;
    }

    private static boolean runOptimization(final Block block, final ExpressionOptimization optimization) {
        boolean modified = false;

        for (final Node node : block.getBody()) {
            final BasicBlock basicBlock = (BasicBlock) node;
            final List<Node> body = basicBlock.getBody();

            for (int i = body.size() - 1; i >= 0; i--) {
                if (i >= body.size()) {
                    continue;
                }

                final Node n = body.get(i);

                if (n instanceof Expression && optimization.run(body, (Expression) n, i)) {
                    modified = true;
                    ++i;
                }
            }
        }

        return modified;
    }

    private static abstract class AbstractBranchBlockOptimization extends AbstractBasicBlockOptimization {
        protected final StrongBox<Expression> expression = new StrongBox<>();
        protected final StrongBox<Label> label1 = new StrongBox<>();
        protected final StrongBox<Label> label2 = new StrongBox<>();

        public AbstractBranchBlockOptimization(final DecompilerContext context, final Block method) {
            super(context, method);
        }

        @Override
        public final boolean run(final List<Node> body, final BasicBlock head, final int position) {
            if (matchLastAndBreak(head, AstCode.IfTrue, label1, expression, label2)) {
                final Label thenLabel = label1.get();
                final Label elseLabel = label2.get();

                final Expression condition = expression.get();

                return run(body, head, condition, thenLabel, elseLabel, false) ||
                       run(body, head, condition, elseLabel, thenLabel, true);
            }

            return false;
        }

        protected abstract boolean run(
            final List<Node> body,
            final BasicBlock branchBlock,
            final Expression branchCondition,
            final Label thenLabel,
            final Label elseLabel,
            final boolean negate);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Utility Methods">

    public static void replaceVariables(final Node node, final Function<Variable, Variable> mapping) {
        if (node instanceof Expression) {
            final Expression expression = (Expression) node;
            final Object operand = expression.getOperand();

            if (operand instanceof Variable) {
                expression.setOperand(mapping.apply((Variable) operand));
            }

            for (final Expression argument : expression.getArguments()) {
                replaceVariables(argument, mapping);
            }
        }
        else {
            if (node instanceof CatchBlock) {
                final CatchBlock catchBlock = (CatchBlock) node;
                final Variable exceptionVariable = catchBlock.getExceptionVariable();

                if (exceptionVariable != null) {
                    catchBlock.setExceptionVariable(mapping.apply(exceptionVariable));
                }
            }

            for (final Node child : node.getChildren()) {
                replaceVariables(child, mapping);
            }
        }
    }

    static <T> void removeOrThrow(final Collection<T> collection, final T item) {
        if (!collection.remove(item)) {
            throw new IllegalStateException("The item was not found in the collection.");
        }
    }

    static void removeTail(final List<Node> body, final AstCode... codes) {
        for (int i = 0; i < codes.length; i++) {
            if (((Expression) body.get(body.size() - codes.length + i)).getCode() != codes[i]) {
                throw new IllegalStateException("Tailing code does not match expected.");
            }
        }

        //noinspection UnusedDeclaration
        for (final AstCode code : codes) {
            body.remove(body.size() - 1);
        }
    }

    static Expression makeLeftAssociativeShortCircuit(final AstCode code, final Expression left, final Expression right) {
        //
        // Assuming that the inputs are already left-associative.
        //
        if (match(right, code)) {
            //
            // Find the leftmost logical expression.
            //
            Expression current = right;

            while (match(current.getArguments().get(0), code)) {
                current = current.getArguments().get(0);
            }

            final Expression newArgument = new Expression(code, null, left.getOffset(), left, current.getArguments().get(0));

            newArgument.setInferredType(BuiltinTypes.Boolean);
            current.getArguments().set(0, newArgument);

            return right;
        }
        else {
            final Expression newExpression = new Expression(code, null, left.getOffset(), left, right);
            newExpression.setInferredType(BuiltinTypes.Boolean);
            return newExpression;
        }
    }

    private final static BooleanBox SCRATCH_BOOLEAN_BOX = new BooleanBox();

    static Expression simplifyLogicalNot(final Expression expression) {
        final Expression result = simplifyLogicalNot(expression, SCRATCH_BOOLEAN_BOX);
        return result != null ? result : expression;
    }

    static Expression simplifyLogicalNot(final Expression expression, final BooleanBox modified) {
        Expression a;
        Expression e = expression;

        //
        // CmpEq(a, ldc, 0) becomes LogicalNot(a) if the inferred type for expression 'a' is boolean.
        //

        List<Expression> arguments = e.getArguments();

        final StrongBox<Boolean> b = new StrongBox<>();
        final Expression operand = arguments.isEmpty() ? null : arguments.get(0);

        if (e.getCode() == AstCode.CmpEq &&
            TypeAnalysis.isBoolean(operand.getInferredType()) &&
            matchBooleanConstant(a = arguments.get(1), b) &&
            Boolean.FALSE.equals(b.get())) {

            e.setCode(AstCode.LogicalNot);
            e.getRanges().addAll(a.getRanges());

            arguments.remove(1);
            modified.set(true);
        }

        Expression result = null;

        if (e.getCode() == AstCode.CmpNe &&
            TypeAnalysis.isBoolean(operand.getInferredType()) &&
            matchBooleanConstant(arguments.get(1), b) &&
            Boolean.FALSE.equals(b.get())) {

            modified.set(true);
            return e.getArguments().get(0);
        }

        if (e.getCode() == AstCode.TernaryOp) {
            final Expression condition = arguments.get(0);

            if (match(condition, AstCode.LogicalNot)) {
                final Expression temp = arguments.get(1);

                arguments.set(0, condition.getArguments().get(0));
                arguments.set(1, arguments.get(2));
                arguments.set(2, temp);
            }
        }

        while (e.getCode() == AstCode.LogicalNot) {
            a = operand;

            //
            // Remove double negation.
            //
            if (a.getCode() == AstCode.LogicalNot) {
                result = a.getArguments().get(0);
                result.getRanges().addAll(e.getRanges());
                result.getRanges().addAll(a.getRanges());
                e = result;
                arguments = e.getArguments();
            }
            else {
                if (simplifyLogicalNotArgument(a)) {
                    result = e = a;
                    arguments = e.getArguments();
                    modified.set(true);
                }
                break;
            }
        }

        for (int i = 0; i < arguments.size(); i++) {
            a = simplifyLogicalNot(arguments.get(i), modified);

            if (a != null) {
                arguments.set(i, a);
                modified.set(true);
            }
        }

        return result;
    }

    static boolean simplifyLogicalNotArgument(final Expression e) {
        if (!canSimplifyLogicalNotArgument(e)) {
            return false;
        }

        final List<Expression> arguments = e.getArguments();

        switch (e.getCode()) {
            case CmpEq:
            case CmpNe:
            case CmpLt:
            case CmpGe:
            case CmpGt:
            case CmpLe:
                e.setCode(e.getCode().reverse());
                return true;

            case LogicalNot:
                final Expression a = arguments.get(0);
                e.setCode(a.getCode());
                e.setOperand(a.getOperand());
                arguments.clear();
                arguments.addAll(a.getArguments());
                e.getRanges().addAll(a.getRanges());
                return true;

            case LogicalAnd:
            case LogicalOr:
                if (!simplifyLogicalNotArgument(arguments.get(0))) {
                    negate(arguments.get(0));
                }
                if (!simplifyLogicalNotArgument(arguments.get(1))) {
                    negate(arguments.get(1));
                }
                e.setCode(e.getCode().reverse());
                return true;

            case TernaryOp:
                simplifyLogicalNotArgument(arguments.get(1));
                simplifyLogicalNotArgument(arguments.get(2));
                return true;

            default:
                return TypeAnalysis.isBoolean(e.getInferredType()) &&
                       negate(e);
        }
    }

    private static boolean negate(final Expression e) {
        if (TypeAnalysis.isBoolean(e.getInferredType())) {
            final Expression copy = e.clone();
            e.setCode(AstCode.LogicalNot);
            e.setOperand(null);
            e.getArguments().clear();
            e.getArguments().add(copy);
            return true;
        }
        return false;
    }

    private static boolean canSimplifyLogicalNotArgument(final Expression e) {
        switch (e.getCode()) {
            case CmpEq:
            case CmpNe:
            case CmpLt:
            case CmpGe:
            case CmpGt:
            case CmpLe:
                return true;

            case LogicalNot:
                return true;

            case LogicalAnd:
            case LogicalOr:
                final List<Expression> arguments = e.getArguments();
                return canSimplifyLogicalNotArgument(arguments.get(0)) ||
                       canSimplifyLogicalNotArgument(arguments.get(1));

            case TernaryOp:
                return TypeAnalysis.isBoolean(e.getInferredType()) &&
                       canSimplifyLogicalNotArgument(e.getArguments().get(1)) &&
                       canSimplifyLogicalNotArgument(e.getArguments().get(2));

            default:
                return false;
        }
    }

    static boolean references(final Node node, final Variable v) {
        for (final Expression e : node.getSelfAndChildrenRecursive(Expression.class)) {
            if (matchLoad(e, v)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsMatch(final Node node, final Expression pattern) {
        for (final Expression e : node.getSelfAndChildrenRecursive(Expression.class)) {
            if (e.isEquivalentTo(pattern)) {
                return true;
            }
        }
        return false;
    }

    // </editor-fold>
}
