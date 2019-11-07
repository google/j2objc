/*
 * GotoRemoval.java
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
import com.strobel.core.CollectionUtilities;
import com.strobel.core.StrongBox;
import com.strobel.core.VerifyArgument;
import com.strobel.util.ContractUtils;

import java.util.*;

import static com.strobel.assembler.metadata.Flags.testAny;
import static com.strobel.core.CollectionUtilities.*;
import static com.strobel.decompiler.ast.PatternMatching.*;

@SuppressWarnings("ConstantConditions")
final class GotoRemoval {
    final static int OPTION_MERGE_ADJACENT_LABELS = 0x01;
    final static int OPTION_REMOVE_REDUNDANT_RETURNS = 0x02;

    final Map<Node, Label> labels = new IdentityHashMap<>();
    final Map<Label, Node> labelLookup = new IdentityHashMap<>();
    final Map<Node, Node> parentLookup = new IdentityHashMap<>();
    final Map<Node, Node> nextSibling = new IdentityHashMap<>();

    final int options;

    GotoRemoval() {
        this(0);
    }

    GotoRemoval(final int options) {
        this.options = options;
    }

    public final void removeGotos(final Block method) {
        traverseGraph(method);
        removeGotosCore(method);
    }

    private void removeGotosCore(final Block method) {
        transformLeaveStatements(method);

        boolean modified;

        do {
            modified = false;

            for (final Expression e : method.getSelfAndChildrenRecursive(Expression.class)) {
                if (e.getCode() == AstCode.Goto) {
                    modified |= trySimplifyGoto(e);
                }
            }
        }
        while (modified);

        removeRedundantCodeCore(method);
    }

    private void traverseGraph(final Block method) {
        labels.clear();
        labelLookup.clear();
        parentLookup.clear();
        nextSibling.clear();

        parentLookup.put(method, Node.NULL);

        for (final Node node : method.getSelfAndChildrenRecursive(Node.class)) {
            Node previousChild = null;

            for (final Node child : node.getChildren()) {
                if (parentLookup.containsKey(child)) {
                    throw Error.expressionLinkedFromMultipleLocations(child);
                }

                parentLookup.put(child, node);

                if (previousChild != null) {
                    if (previousChild instanceof Label) {
                        labels.put(child, (Label) previousChild);
                        labelLookup.put((Label) previousChild, child);
                    }
                    nextSibling.put(previousChild, child);
                }

                previousChild = child;
            }

            if (previousChild != null) {
                nextSibling.put(previousChild, Node.NULL);
            }
        }
    }

    private boolean trySimplifyGoto(final Expression gotoExpression) {
        assert gotoExpression.getCode() == AstCode.Goto;
        assert gotoExpression.getOperand() instanceof Label;

        final Node target = enter(gotoExpression, new LinkedHashSet<Node>());

        if (target == null) {
            return false;
        }

        //
        // The goto expression is marked as visited because we do not want to iterate over
        // nodes which we plan to modify.
        //
        // The simulated path always has to start in the same try block in order for the
        // same finally blocks to be executed.
        //

        final Set<Node> visitedNodes = new LinkedHashSet<>();

        visitedNodes.add(gotoExpression);

        final Node exitTo = exit(gotoExpression, visitedNodes);
        final boolean isRedundant = target == exitTo;

        if (isRedundant) {
            final Node parent = parentLookup.get(gotoExpression);

            //
            // For now, only remove redundant goto expressions that are unlikely to be of the form
            // `if (x) continue`.  This is an aesthetic choice.
            //

            if (!(parent instanceof Block &&
                  ((Block) parent).getBody().size() == 1 &&
                  parentLookup.get(parent) instanceof Condition)) {

                gotoExpression.setCode(AstCode.Nop);
                gotoExpression.setOperand(null);

                if (target instanceof Expression) {
                    ((Expression) target).getRanges().addAll(gotoExpression.getRanges());
                }

                gotoExpression.getRanges().clear();
                return true;
            }
        }

        visitedNodes.clear();
        visitedNodes.add(gotoExpression);

        for (final TryCatchBlock tryCatchBlock : getParents(gotoExpression, TryCatchBlock.class)) {
            final Block finallyBlock = tryCatchBlock.getFinallyBlock();

            if (finallyBlock == null) {
                continue;
            }

            if (target == enter(finallyBlock, visitedNodes)) {
                gotoExpression.setCode(AstCode.Nop);
                gotoExpression.setOperand(null);
                gotoExpression.getRanges().clear();
                return true;
            }
        }

        visitedNodes.clear();
        visitedNodes.add(gotoExpression);

        //
        // Look for single-level `continue` statements first.
        //

        Loop continueBlock = null;

        for (final Node parent : getParents(gotoExpression)) {
            if (parent instanceof Loop) {
                final Node enter = enter(parent, visitedNodes);

                if (target == enter) {
                    continueBlock = (Loop) parent;
                    break;
                }

                if (enter instanceof TryCatchBlock) {
                    final Node firstChild = firstOrDefault(enter.getChildren());

                    if (firstChild != null) {
                        visitedNodes.clear();
                        if (enter(firstChild, visitedNodes) == target) {
                            continueBlock = (Loop) parent;
                            break;
                        }
                    }
                }

                break;
            }
        }

        if (continueBlock != null) {
            gotoExpression.setCode(AstCode.LoopContinue);
            gotoExpression.setOperand(null);
            return true;
        }

        //
        // Remove redundant goto statements that are NOT conditional continue statements.
        //

        if (isRedundant) {
            gotoExpression.setCode(AstCode.Nop);
            gotoExpression.setOperand(null);

            if (target instanceof Expression) {
                ((Expression) target).getRanges().addAll(gotoExpression.getRanges());
            }

            gotoExpression.getRanges().clear();
            return true;
        }

        visitedNodes.clear();
        visitedNodes.add(gotoExpression);

        //
        // Now look for loop/switch break statements.
        //

        int loopDepth = 0;
        int switchDepth = 0;
        Node breakBlock = null;

        for (final Node parent : getParents(gotoExpression)) {
            if (parent instanceof Loop) {
                ++loopDepth;

                final Node exit = exit(parent, visitedNodes);

                if (target == exit) {
                    breakBlock = parent;
                    break;
                }

                if (exit instanceof TryCatchBlock) {
                    final Node firstChild = firstOrDefault(exit.getChildren());

                    if (firstChild != null) {
                        visitedNodes.clear();
                        if (enter(firstChild, visitedNodes) == target) {
                            breakBlock = parent;
                            break;
                        }
                    }
                }
            }
            else if (parent instanceof Switch) {
                ++switchDepth;

                final Node exit = exit(parent, visitedNodes);

                if (target == exit) {
                    breakBlock = parent;
                    break;
                }
            }
        }

        if (breakBlock != null) {
            gotoExpression.setCode(AstCode.LoopOrSwitchBreak);
            gotoExpression.setOperand((loopDepth + switchDepth) > 1 ? gotoExpression.getOperand() : null);
            return true;
        }

        visitedNodes.clear();
        visitedNodes.add(gotoExpression);

        //
        // Now look for outer loop continue statements.
        //

        loopDepth = 0;

        for (final Node parent : getParents(gotoExpression)) {
            if (parent instanceof Loop) {
                ++loopDepth;

                final Node enter = enter(parent, visitedNodes);

                if (target == enter) {
                    continueBlock = (Loop) parent;
                    break;
                }

                if (enter instanceof TryCatchBlock) {
                    final Node firstChild = firstOrDefault(enter.getChildren());

                    if (firstChild != null) {
                        visitedNodes.clear();
                        if (enter(firstChild, visitedNodes) == target) {
                            continueBlock = (Loop) parent;
                            break;
                        }
                    }
                }
            }
        }

        if (continueBlock != null) {
            gotoExpression.setCode(AstCode.LoopContinue);
            gotoExpression.setOperand(loopDepth > 1 ? gotoExpression.getOperand() : null);
            return true;
        }

        //
        // Lastly, try to duplicate return/throw statements at the target site.
        //

        return tryInlineReturn(gotoExpression, target, AstCode.Return) ||
               tryInlineReturn(gotoExpression, target, AstCode.AThrow);
    }

    private boolean tryInlineReturn(final Expression gotoExpression, final Node target, final AstCode code) {
        final List<Expression> expressions = new ArrayList<>();

        if (matchGetArguments(target, code, expressions) &&
            (expressions.isEmpty() ||
             expressions.size() == 1/* && Inlining.hasNoSideEffect(expressions.get(0))*/)) {

            gotoExpression.setCode(code);
            gotoExpression.setOperand(null);
            gotoExpression.getArguments().clear();

            if (!expressions.isEmpty()) {
                gotoExpression.getArguments().add(expressions.get(0).clone());
            }

            return true;
        }

        final StrongBox<Variable> v = new StrongBox<>();
        final StrongBox<Variable> v2 = new StrongBox<>();

        Node next = nextSibling.get(target);

        while (next instanceof Label) {
            next = nextSibling.get(next);
        }

        if (matchGetArguments(target, AstCode.Store, v, expressions) &&
            expressions.size() == 1 &&
            /*Inlining.hasNoSideEffect(expressions.get(0)) &&*/
            matchGetArguments(next, code, expressions) &&
            expressions.size() == 1 &&
            matchGetOperand(expressions.get(0), AstCode.Load, v2) &&
            v2.get() == v.get()) {

            gotoExpression.setCode(code);
            gotoExpression.setOperand(null);
            gotoExpression.getArguments().clear();
            gotoExpression.getArguments().add(((Expression) target).getArguments().get(0).clone());

            return true;
        }

        return false;
    }

    private Iterable<Node> getParents(final Node node) {
        return getParents(node, Node.class);
    }

    private <T extends Node> Iterable<T> getParents(final Node node, final Class<T> parentType) {
        return new Iterable<T>() {
            @NotNull
            @Override
            public final Iterator<T> iterator() {
                return new Iterator<T>() {
                    T current = updateCurrent(node);

                    @SuppressWarnings("unchecked")
                    private T updateCurrent(Node node) {
                        while (node != null && node != Node.NULL) {
                            node = parentLookup.get(node);

                            if (parentType.isInstance(node)) {
                                return (T) node;
                            }
                        }

                        return null;
                    }

                    @Override
                    public final boolean hasNext() {
                        return current != null;
                    }

                    @Override
                    public final T next() {
                        final T next = current;

                        if (next == null) {
                            throw new NoSuchElementException();
                        }

                        current = updateCurrent(next);
                        return next;
                    }

                    @Override
                    public final void remove() {
                        throw ContractUtils.unsupported();
                    }
                };
            }
        };
    }

    private Node enter(final Node node, final Set<Node> visitedNodes) {
        VerifyArgument.notNull(node, "node");
        VerifyArgument.notNull(visitedNodes, "visitedNodes");

        if (!visitedNodes.add(node)) {
            //
            // Infinite loop.
            //
            return null;
        }

        if (node instanceof Label) {
            return exit(node, visitedNodes);
        }

        if (node instanceof Expression) {
            final Expression e = (Expression) node;

            switch (e.getCode()) {
                case Goto: {
                    final Label target = (Label) e.getOperand();

                    //
                    // Early exit -- same try block.
                    //
                    if (firstOrDefault(getParents(e, TryCatchBlock.class)) ==
                        firstOrDefault(getParents(target, TryCatchBlock.class))) {

                        return enter(target, visitedNodes);
                    }

                    //
                    // Make sure we are not entering a try block.
                    //
                    final List<TryCatchBlock> sourceTryBlocks = toList(getParents(e, TryCatchBlock.class));
                    final List<TryCatchBlock> targetTryBlocks = toList(getParents(target, TryCatchBlock.class));

                    Collections.reverse(sourceTryBlocks);
                    Collections.reverse(targetTryBlocks);

                    //
                    // Skip blocks we are already in.
                    //
                    int i = 0;

                    while (i < sourceTryBlocks.size() &&
                           i < targetTryBlocks.size() &&
                           sourceTryBlocks.get(i) == targetTryBlocks.get(i)) {
                        i++;
                    }

                    if (i == targetTryBlocks.size()) {
                        return enter(target, visitedNodes);
                    }

                    final TryCatchBlock targetTryBlock = targetTryBlocks.get(i);

                    //
                    // Check that the goto points to the start.
                    //
                    TryCatchBlock current = targetTryBlock;

                    while (current != null) {
                        final List<Node> body = current.getTryBlock().getBody();

                        current = null;

                        for (final Node n : body) {
                            if (n instanceof Label) {
                                if (n == target) {
                                    return targetTryBlock;
                                }
                            }
                            else if (!match(n, AstCode.Nop)) {
                                current = n instanceof TryCatchBlock ? (TryCatchBlock) n : null;
                                break;
                            }
                        }
                    }

                    return null;
                }

                default: {
                    return e;
                }
            }
        }

        if (node instanceof Block) {
            final Block block = (Block) node;

            if (block.getEntryGoto() != null) {
                return enter(block.getEntryGoto(), visitedNodes);
            }

            if (block.getBody().isEmpty()) {
                return exit(block, visitedNodes);
            }

            return enter(block.getBody().get(0), visitedNodes);
        }

        if (node instanceof Condition) {
            return ((Condition) node).getCondition();
        }

        if (node instanceof Loop) {
            final Loop loop = (Loop) node;

            if (loop.getLoopType() == LoopType.PreCondition && loop.getCondition() != null) {
                return loop.getCondition();
            }

            return enter(loop.getBody(), visitedNodes);
        }

        if (node instanceof TryCatchBlock) {
            return node;
        }

        if (node instanceof Switch) {
            return ((Switch) node).getCondition();
        }

        throw Error.unsupportedNode(node);
    }

    private Node exit(final Node node, final Set<Node> visitedNodes) {
        VerifyArgument.notNull(node, "node");
        VerifyArgument.notNull(visitedNodes, "visitedNodes");

        final Node parent = parentLookup.get(node);

        if (parent == null || parent == Node.NULL) {
            //
            // Exited main body.
            //
            return null;
        }

        if (parent instanceof Block) {
            final Node nextNode = nextSibling.get(node);

            if (nextNode != null && nextNode != Node.NULL) {
                return enter(nextNode, visitedNodes);
            }

            if (parent instanceof CaseBlock) {
                final Node nextCase = nextSibling.get(parent);

                if (nextCase != null && nextCase != Node.NULL) {
                    return enter(nextCase, visitedNodes);
                }
            }

            return exit(parent, visitedNodes);
        }

        if (parent instanceof Condition) {
            return exit(parent, visitedNodes);
        }

        if (parent instanceof TryCatchBlock) {
            //
            // Finally blocks are completely ignored.  We rely on the fact that try blocks
            // cannot be entered.
            //
            return exit(parent, visitedNodes);
        }

        if (parent instanceof Switch) {
            //
            // Implicit exit from switch is not allowed.
            //
            return null;
        }

        if (parent instanceof Loop) {
            return enter(parent, visitedNodes);
        }

        throw Error.unsupportedNode(parent);
    }

    @SuppressWarnings("ConstantConditions")
    private void transformLeaveStatements(final Block method) {
        final StrongBox<Label> target = new StrongBox<>();
        final Set<Node> visitedNodes = new LinkedHashSet<>();

    outer:
        for (final Expression e : method.getSelfAndChildrenRecursive(Expression.class)) {
            if (matchGetOperand(e, AstCode.Goto, target)) {
                visitedNodes.clear();

                final Node exit = exit(e, new HashSet<Node>());

                if (exit != null && matchLeaveHandler(exit)) {
                    final Node parent = parentLookup.get(e);
                    final Node grandParent = parent != null ? parentLookup.get(parent) : null;

                    if (parent instanceof Block &&
                        (grandParent instanceof CatchBlock ||
                         grandParent instanceof TryCatchBlock) &&
                        e == last(((Block) parent).getBody())) {

                        if (grandParent instanceof TryCatchBlock &&
                            parent == ((TryCatchBlock) grandParent).getFinallyBlock()) {

                            e.setCode(AstCode.EndFinally);
                        }
                        else {
                            e.setCode(AstCode.Leave);
                        }

                        e.setOperand(null);
                    }
                }
            }
        }
    }

    public static void removeRedundantCode(final Block method) {
        removeRedundantCode(method, 0);
    }

    @SuppressWarnings("ConstantConditions")
    public static void removeRedundantCode(final Block method, final int options) {
        final GotoRemoval gotoRemoval = new GotoRemoval(options);

        gotoRemoval.traverseGraph(method);
        gotoRemoval.removeRedundantCodeCore(method);
    }

    private void removeRedundantCodeCore(final Block method) {
        //
        // Remove dead labels and NOPs.
        //

        final Set<Label> liveLabels = new LinkedHashSet<>();
        final StrongBox<Label> target = new StrongBox<>();

        final Set<Expression> returns = new LinkedHashSet<>();

        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        final Map<Label, List<Expression>> jumps = new DefaultMap<>(CollectionUtilities.<Expression>listFactory());

        List<TryCatchBlock> tryCatchBlocks = null;

    outer:
        for (final Expression e : method.getSelfAndChildrenRecursive(Expression.class)) {
            if (matchEmptyReturn(e)) {
                returns.add(e);
            }

            if (e.isBranch()) {
                if (matchGetOperand(e, AstCode.Goto, target)) {
                    if (tryCatchBlocks == null) {
                        tryCatchBlocks = method.getSelfAndChildrenRecursive(TryCatchBlock.class);
                    }

                    //
                    // See if the goto is an explicit jump to an outer finally.  If so, remove it.
                    //
                    for (final TryCatchBlock tryCatchBlock : tryCatchBlocks) {
                        final Block finallyBlock = tryCatchBlock.getFinallyBlock();

                        if (finallyBlock != null) {
                            final Node firstInBody = firstOrDefault(finallyBlock.getBody());

                            if (firstInBody == target.get()) {
                                e.setCode(AstCode.Leave);
                                e.setOperand(null);
                                continue outer;
                            }
                        }
                        else if (tryCatchBlock.getCatchBlocks().size() == 1) {
                            final Node firstInBody = firstOrDefault(first(tryCatchBlock.getCatchBlocks()).getBody());

                            if (firstInBody == target.get()) {
                                e.setCode(AstCode.Leave);
                                e.setOperand(null);
                                continue outer;
                            }
                        }
                    }
                }

                final List<Label> branchTargets = e.getBranchTargets();

                for (final Label label : branchTargets) {
                    jumps.get(label).add(e);
                }

                liveLabels.addAll(branchTargets);
            }
        }

        final boolean mergeAdjacentLabels = testAny(options, OPTION_MERGE_ADJACENT_LABELS);

        for (final Block block : method.getSelfAndChildrenRecursive(Block.class)) {
            final List<Node> body = block.getBody();

            for (int i = 0; i < body.size(); i++) {
                final Node n = body.get(i);

                //noinspection SuspiciousMethodCalls
                if (match(n, AstCode.Nop) ||
                    match(n, AstCode.Leave) ||
                    match(n, AstCode.EndFinally) ||
                    n instanceof Label && !liveLabels.contains(n)) {

                    body.remove(i--);
                }

                if (mergeAdjacentLabels &&
                    n instanceof Label &&
                    i < body.size() - 1 &&
                    body.get(i + 1) instanceof Label) {

                    final Label newLabel = (Label) n;
                    final Label oldLabel = (Label) body.remove(i + 1);
                    final List<Expression> oldLabelJumps = jumps.get(oldLabel);

                    for (final Expression jump : oldLabelJumps) {
                        if (jump.getOperand() instanceof Label) {
                            jump.setOperand(n);
                        }
                        else {
                            final Label[] branchTargets = (Label[]) jump.getOperand();

                            for (int j = 0; j < branchTargets.length; j++) {
                                if (branchTargets[j] == oldLabel) {
                                    branchTargets[j] = newLabel;
                                }
                            }
                        }
                    }
                }
            }
        }

        //
        // Remove redundant continue statements.
        //

        for (final Loop loop : method.getSelfAndChildrenRecursive(Loop.class)) {
            final Block body = loop.getBody();
            final Node lastInLoop = lastOrDefault(body.getBody());

            if (lastInLoop == null) {
                continue;
            }

            if (match(lastInLoop, AstCode.LoopContinue)) {
                final Expression last = (Expression) last(body.getBody());

                if (last.getOperand() == null) {
                    body.getBody().remove(last);
                }
            }
            else if (lastInLoop instanceof Condition) {
                final Condition condition = (Condition) lastInLoop;
                final Block falseBlock = condition.getFalseBlock();

                if (matchSingle(falseBlock, AstCode.LoopContinue, target) &&
                    target.get() == null) {

                    falseBlock.getBody().clear();
                }
            }
        }

        //
        // Remove redundant break at end of case.  Remove empty case blocks.
        //

        for (final Switch switchNode : method.getSelfAndChildrenRecursive(Switch.class)) {
            CaseBlock defaultCase = null;

            final List<CaseBlock> caseBlocks = switchNode.getCaseBlocks();

            for (final CaseBlock caseBlock : caseBlocks) {
                assert caseBlock.getEntryGoto() == null;

                if (caseBlock.getValues().isEmpty()) {
                    defaultCase = caseBlock;
                }

                final List<Node> caseBody = caseBlock.getBody();
                final int size = caseBody.size();

                if (size >= 2) {
                    if (caseBody.get(size - 2).isUnconditionalControlFlow() &&
                        match(caseBody.get(size - 1), AstCode.LoopOrSwitchBreak)) {

                        caseBody.remove(size - 1);
                    }
                }
            }

            if (defaultCase == null ||
                defaultCase.getBody().size() == 1 && match(firstOrDefault(defaultCase.getBody()), AstCode.LoopOrSwitchBreak)) {

                for (int i = 0; i < caseBlocks.size(); i++) {
                    final List<Node> body = caseBlocks.get(i).getBody();

                    if (body.size() == 1 &&
                        matchGetOperand(firstOrDefault(body), AstCode.LoopOrSwitchBreak, target) &&
                        target.get() == null) {

                        caseBlocks.remove(i--);
                    }
                }
            }
        }

        //
        // Remove redundant return at end of method.
        //

        final List<Node> methodBody = method.getBody();
        final Node lastStatement = lastOrDefault(methodBody);

        if (match(lastStatement, AstCode.Return) &&
            ((Expression) lastStatement).getArguments().isEmpty()) {

            methodBody.remove(methodBody.size() - 1);
            //noinspection SuspiciousMethodCalls
            returns.remove(lastStatement);
        }

        //
        // Remove unreachable return/throw statements.
        //

        boolean modified = false;

        for (final Block block : method.getSelfAndChildrenRecursive(Block.class)) {
            final List<Node> blockBody = block.getBody();

            for (int i = 0; i < blockBody.size() - 1; i++) {
                final Node node = blockBody.get(i);

                if (node.isUnconditionalControlFlow() &&
                    (match(blockBody.get(i + 1), AstCode.Return) ||
                     match(blockBody.get(i + 1), AstCode.AThrow))) {

                    modified = true;
                    blockBody.remove(i-- + 1);

                    //noinspection SuspiciousMethodCalls
                    returns.remove(blockBody.get(i + 1));
                }
            }
        }

        if (testAny(options, OPTION_REMOVE_REDUNDANT_RETURNS)) {
            //
            // Remove redundant empty returns deeper within the tree, e.g.,:
            //
            // { try { f(); return; } catch { g(); return; } return; } => { try { f(); } catch { g(); } return; }
            //

            for (final Expression r : returns) {
                final Node immediateParent = parentLookup.get(r);

                Node current = r;
                Node parent = immediateParent;

                boolean firstBlock = true;
                boolean isRedundant = true;

                while (parent != null && parent != Node.NULL) {
                    if (parent instanceof BasicBlock || parent instanceof Block) {
                        final List<Node> body = parent instanceof BasicBlock ? ((BasicBlock) parent).getBody()
                                                                             : ((Block) parent).getBody();

                        if (firstBlock) {
                            final Node grandparent = parentLookup.get(parent);

                            if (grandparent instanceof Condition) {
                                final Condition c = (Condition) grandparent;

                                if (c.getTrueBlock().getBody().size() == 1 &&
                                    r == last(c.getTrueBlock().getBody()) &&
                                    (matchNullOrEmpty(c.getFalseBlock()) || matchEmptyReturn(c.getFalseBlock()))) {

                                    //
                                    // Don't convert `if (condition) { return; }` to `if (condition) {}` for aesthetic reasons.
                                    //

                                    isRedundant = false;
                                    break;
                                }
                            }

                            firstBlock = false;
                        }

                        final Node last = last(body);

                        if (last != current) {
                            if (matchEmptyReturn(last) &&
                                body.size() > 1 &&
                                body.get(body.size() - 2) == current) {

                                //
                                // Given { { { f(); return; } return; } ... }, the outer return is enough to render the
                                // inner return redundant, so we need not walk any further up the tree.
                                //

                                break;
                            }

                            isRedundant = false;
                            break;
                        }
                    }

                    current = parent;
                    parent = parentLookup.get(current);
                }

                if (isRedundant) {
                    if (immediateParent instanceof Block) {
                        ((Block) immediateParent).getBody().remove(r);
                    }
                    else if (immediateParent instanceof BasicBlock) {
                        ((BasicBlock) immediateParent).getBody().remove(r);
                    }
                }
            }
        }

        if (modified) {
            //
            // More removals might be possible.
            //
            removeGotosCore(method);
        }
    }
}
