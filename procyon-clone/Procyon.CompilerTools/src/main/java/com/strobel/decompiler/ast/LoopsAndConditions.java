/*
 * LoopsAndConditions.java
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
import com.strobel.assembler.flowanalysis.ControlFlowNode;
import com.strobel.assembler.flowanalysis.ControlFlowNodeType;
import com.strobel.assembler.flowanalysis.JumpType;
import com.strobel.assembler.metadata.SwitchInfo;
import com.strobel.core.ArrayUtilities;
import com.strobel.core.Pair;
import com.strobel.core.Predicate;
import com.strobel.core.StrongBox;
import com.strobel.decompiler.DecompilerContext;

import java.util.*;

import static com.strobel.core.CollectionUtilities.*;
import static com.strobel.decompiler.ast.AstOptimizer.*;
import static com.strobel.decompiler.ast.PatternMatching.*;

final class LoopsAndConditions {
    private final Map<Label, ControlFlowNode> labelsToNodes = new IdentityHashMap<>();
    @SuppressWarnings({ "FieldCanBeLocal", "UnusedDeclaration" })
    private final DecompilerContext context;

    private int _nextLabelIndex;

    LoopsAndConditions(final DecompilerContext context) {
        this.context = context;
    }

    public final void findConditions(final Block block) {
        final List<Node> body = block.getBody();

        if (body.isEmpty() || block.getEntryGoto() == null) {
            return;
        }

        final ControlFlowGraph graph = buildGraph(body, (Label) block.getEntryGoto().getOperand());

        graph.computeDominance();
        graph.computeDominanceFrontier();

        final Set<ControlFlowNode> cfNodes = new LinkedHashSet<>();
        final List<ControlFlowNode> graphNodes = graph.getNodes();

        for (int i = 3; i < graphNodes.size(); i++) {
            cfNodes.add(graphNodes.get(i));
        }

        final List<Node> newBody = findConditions(cfNodes, graph.getEntryPoint());

        block.getBody().clear();
        block.getBody().addAll(newBody);
    }

    public final void findLoops(final Block block) {
        final List<Node> body = block.getBody();

        if (body.isEmpty() || block.getEntryGoto() == null) {
            return;
        }

        final ControlFlowGraph graph = buildGraph(body, (Label) block.getEntryGoto().getOperand());

        graph.computeDominance();
        graph.computeDominanceFrontier();

        final Set<ControlFlowNode> cfNodes = new LinkedHashSet<>();
        final List<ControlFlowNode> graphNodes = graph.getNodes();

        for (int i = 3; i < graphNodes.size(); i++) {
            cfNodes.add(graphNodes.get(i));
        }

        final List<Node> newBody = findLoops(cfNodes, graph.getEntryPoint(), false);

        block.getBody().clear();
        block.getBody().addAll(newBody);
    }

    private ControlFlowGraph buildGraph(final List<Node> nodes, final Label entryLabel) {
        int index = 0;

        final List<ControlFlowNode> cfNodes = new ArrayList<>();

        final ControlFlowNode entryPoint = new ControlFlowNode(index++, 0, ControlFlowNodeType.EntryPoint);
        final ControlFlowNode regularExit = new ControlFlowNode(index++, -1, ControlFlowNodeType.RegularExit);
        final ControlFlowNode exceptionalExit = new ControlFlowNode(index++, -1, ControlFlowNodeType.ExceptionalExit);

        cfNodes.add(entryPoint);
        cfNodes.add(regularExit);
        cfNodes.add(exceptionalExit);

        //
        // Create graph nodes.
        //

        labelsToNodes.clear();

        final Map<Node, ControlFlowNode> astNodesToControlFlowNodes = new IdentityHashMap<>();

        for (final Node node : nodes) {
            final ControlFlowNode cfNode = new ControlFlowNode(index++, -1, ControlFlowNodeType.Normal);

            cfNodes.add(cfNode);
            astNodesToControlFlowNodes.put(node, cfNode);
            cfNode.setUserData(node);

            //
            // Find all contained labels.
            //
            for (final Label label : node.getSelfAndChildrenRecursive(Label.class)) {
                labelsToNodes.put(label, cfNode);
            }
        }

        final ControlFlowNode entryNode = labelsToNodes.get(entryLabel);
        final ControlFlowEdge entryEdge = new ControlFlowEdge(entryPoint, entryNode, JumpType.Normal);

        entryPoint.getOutgoing().add(entryEdge);
        entryNode.getIncoming().add(entryEdge);

        //
        // Create edges.
        //

        for (final Node node : nodes) {
            final ControlFlowNode source = astNodesToControlFlowNodes.get(node);

            //
            // Find all branches.
            //

            for (final Expression e : node.getSelfAndChildrenRecursive(Expression.class)) {
                if (!e.isBranch()) {
                    continue;
                }

                for (final Label target : e.getBranchTargets()) {
                    final ControlFlowNode destination = labelsToNodes.get(target);

                    if (destination != null &&
                        (destination != source || canBeSelfContainedLoop((BasicBlock) node, e, target))) {

                        final ControlFlowEdge edge = new ControlFlowEdge(source, destination, JumpType.Normal);

                        if (!source.getOutgoing().contains(edge)) {
                            source.getOutgoing().add(edge);
                        }

                        if (!destination.getIncoming().contains(edge)) {
                            destination.getIncoming().add(edge);
                        }
                    }
                }
            }
        }

        return new ControlFlowGraph(cfNodes.toArray(new ControlFlowNode[cfNodes.size()]));
    }

    private boolean canBeSelfContainedLoop(final BasicBlock node, final Expression branch, final Label target) {
        final List<Node> nodeBody = node.getBody();

        if (target == null || nodeBody.isEmpty()) {
            return false;
        }

        if (target == nodeBody.get(0)) {
            return true;
        }

        final Node secondNode = getOrDefault(nodeBody, 1);

        if (secondNode instanceof TryCatchBlock) {
            final Node next = getOrDefault(nodeBody, 2);

            if (next != branch) {
                return false;
            }

            final TryCatchBlock tryCatch = (TryCatchBlock) secondNode;
            final Block tryBlock = tryCatch.getTryBlock();

            final Predicate<Expression> labelMatch = new Predicate<Expression>() {
                @Override
                public boolean test(final Expression e) {
                    return e != tryBlock.getEntryGoto() && e.getBranchTargets().contains(target);
                }
            };

/*
            if (tryBlock != null) {
                final Node firstInTryBody = firstOrDefault(tryBlock.getBody());

                if (!(firstInTryBody instanceof BasicBlock &&
                      target == firstOrDefault(((BasicBlock) firstInTryBody).getBody()))) {

                    return false;
                }

                final boolean branchInTry = any(tryBlock.getSelfAndChildrenRecursive(Expression.class), labelMatch);

                if (branchInTry) {
                    return false;
                }
            }
*/

            for (final CatchBlock catchBlock : tryCatch.getCatchBlocks()) {
                if (any(catchBlock.getSelfAndChildrenRecursive(Expression.class), labelMatch)) {
                    return true;
                }
            }

            if (tryCatch.getFinallyBlock() != null &&
                any(tryCatch.getFinallyBlock().getSelfAndChildrenRecursive(Expression.class), labelMatch)) {

                return true;
            }

            return true;
        }

        return false;
    }

    @SuppressWarnings("ConstantConditions")
    private List<Node> findLoops(final Set<ControlFlowNode> scopeNodes, final ControlFlowNode entryPoint, final boolean excludeEntryPoint) {
        final List<Node> result = new ArrayList<>();
        final StrongBox<Label[]> switchLabels = new StrongBox<>();
        final Set<ControlFlowNode> scope = new LinkedHashSet<>(scopeNodes);
        final ArrayDeque<ControlFlowNode> agenda = new ArrayDeque<>();

        agenda.addLast(entryPoint);

        while (!agenda.isEmpty()) {
            final ControlFlowNode node = agenda.pollFirst();

            //
            // If the node is a loop header...
            //
            if (scope.contains(node) &&
                node.getDominanceFrontier().contains(node) &&
                (node != entryPoint || !excludeEntryPoint)) {

                final Set<ControlFlowNode> loopContents = findLoopContents(scope, node);

                //
                // If the first or last expression is a loop condition...
                //
                final BasicBlock basicBlock = (BasicBlock) node.getUserData();
                final StrongBox<Expression> condition = new StrongBox<>();
                final StrongBox<Label> trueLabel = new StrongBox<>();
                final StrongBox<Label> falseLabel = new StrongBox<>();

                final ControlFlowNode lastInLoop = lastOrDefault(loopContents);
                final BasicBlock lastBlock = (BasicBlock) lastInLoop.getUserData();

                //
                // Check for an infinite loop.
                //

                if (loopContents.size() == 1 &&
                    matchSimpleBreak(basicBlock, trueLabel) &&
                    trueLabel.get() == first(basicBlock.getBody())) {

                    final Loop emptyLoop = new Loop();

                    emptyLoop.setBody(new Block());

                    final BasicBlock block = new BasicBlock();
                    final List<Node> blockBody = block.getBody();

                    blockBody.add(basicBlock.getBody().get(0));
                    blockBody.add(emptyLoop);

                    result.add(block);
                    scope.remove(lastInLoop);
                    continue;
                }

                //
                // Check for a conditional loop.
                //

                for (int pass = 0; pass < 2; pass++) {
                    final boolean isPostCondition = pass == 1;

                    final boolean foundCondition = isPostCondition ? matchLastAndBreak(lastBlock, AstCode.IfTrue, trueLabel, condition, falseLabel)
                                                                   : matchSingleAndBreak(basicBlock, AstCode.IfTrue, trueLabel, condition, falseLabel);

                    //
                    // It has to be just IfTrue; any preceding code would introduce a goto.
                    //
                    if (!foundCondition) {
                        continue;
                    }

                    final ControlFlowNode trueTarget = labelsToNodes.get(trueLabel.get());
                    final ControlFlowNode falseTarget = labelsToNodes.get(falseLabel.get());

                    //
                    // If one point inside the loop and the other outside...
                    //

                    if ((!loopContents.contains(falseTarget) || loopContents.contains(trueTarget)) &&
                        (!loopContents.contains(trueTarget) || loopContents.contains(falseTarget))) {

                        continue;
                    }

                    final boolean flipped = loopContents.contains(falseTarget) || falseTarget == node;

                    //
                    // If false means enter the loop, negate the condition.
                    //
                    if (flipped) {
                        final Label temp = trueLabel.get();

                        trueLabel.set(falseLabel.get());
                        falseLabel.set(temp);
                        condition.set(AstOptimizer.simplifyLogicalNot(new Expression(AstCode.LogicalNot, null, condition.get().getOffset(), condition.get())));
                    }

                    final boolean canWriteConditionalLoop;

                    if (isPostCondition) {
                        final Expression continueGoto;

                        if (flipped) {
                            continueGoto = (Expression) last(lastBlock.getBody());
                        }
                        else {
                            continueGoto = (Expression) lastBlock.getBody().get(lastBlock.getBody().size() - 2);
                        }

                        canWriteConditionalLoop = countJumps(loopContents, trueLabel.get(), continueGoto) == 0;
                    }
                    else {
                        canWriteConditionalLoop = true;
                    }

                    if (canWriteConditionalLoop) {
                        removeOrThrow(loopContents, node);
                        removeOrThrow(scope, node);

                        final ControlFlowNode postLoopTarget = labelsToNodes.get(falseLabel.get());

                        if (postLoopTarget != null) {
                            //
                            // Pull more nodes into the loop.
                            //
                            final Set<ControlFlowNode> postLoopContents = findDominatedNodes(scope, postLoopTarget);
                            final LinkedHashSet<ControlFlowNode> pullIn = new LinkedHashSet<>(scope);

                            pullIn.removeAll(postLoopContents);

                            for (final ControlFlowNode n : pullIn) {
                                if (node.dominates(n)) {
                                    loopContents.add(n);
                                }
                            }
                        }

                        //
                        // Use loop to implement the IfTrue.
                        //
                        final BasicBlock block;
                        final List<Node> basicBlockBody;
                        final Label loopLabel;

                        if (isPostCondition) {
                            block = new BasicBlock();
                            basicBlockBody = block.getBody();

                            removeTail(lastBlock.getBody(), AstCode.IfTrue, AstCode.Goto);

                            if (lastBlock.getBody().size() > 1) {
                                lastBlock.getBody().add(new Expression(AstCode.Goto, trueLabel.get(), Expression.MYSTERY_OFFSET));
                                loopLabel = new Label("Loop_" + _nextLabelIndex++);
                            }
                            else {
                                scope.remove(lastInLoop);
                                loopContents.remove(lastInLoop);
                                loopLabel = (Label) lastBlock.getBody().get(0);
                            }

                            basicBlockBody.add(loopLabel);
                        }
                        else {
                            block = basicBlock;
                            basicBlockBody = block.getBody();
                            removeTail(basicBlockBody, AstCode.IfTrue, AstCode.Goto);
                        }

                        final Loop loop = new Loop();
                        final Block bodyBlock = new Block();

                        loop.setCondition(condition.get());
                        loop.setBody(bodyBlock);

                        if (isPostCondition) {
                            loop.setLoopType(LoopType.PostCondition);
                            bodyBlock.getBody().add(basicBlock);
                        }

                        bodyBlock.setEntryGoto(new Expression(AstCode.Goto, trueLabel.get(), Expression.MYSTERY_OFFSET));
                        bodyBlock.getBody().addAll(findLoops(loopContents, node, isPostCondition));

                        basicBlockBody.add(loop);

                        if (isPostCondition) {
                            basicBlockBody.add(new Expression(AstCode.Goto, falseLabel.get(), Expression.MYSTERY_OFFSET));
                        }
                        else {
                            basicBlockBody.add(new Expression(AstCode.Goto, falseLabel.get(), Expression.MYSTERY_OFFSET));
                        }

                        result.add(block);
                        scope.removeAll(loopContents);

                        break;
                    }
                }

                //
                // Fallback method: while (true) { ... }
                //
                if (scope.contains(node)) {
                    final BasicBlock block = new BasicBlock();
                    final List<Node> blockBody = block.getBody();
                    final Loop loop = new Loop();
                    final Block bodyBlock = new Block();

                    loop.setBody(bodyBlock);

                    final LoopExitInfo exitInfo = findLoopExitInfo(loopContents);

                    if (exitInfo.exitLabel != null) {
                        final ControlFlowNode postLoopTarget = labelsToNodes.get(exitInfo.exitLabel);

                        if (postLoopTarget.getIncoming().size() == 1) {
                            //
                            // See if our only exit comes from an inner switch's default label.  If so, pull it in
                            // to the loop if there are no other references.
                            //

                            final ControlFlowNode predecessor = firstOrDefault(postLoopTarget.getPredecessors());

                            if (predecessor != null && loopContents.contains(predecessor)) {
                                final BasicBlock b = (BasicBlock) predecessor.getUserData();

                                if (matchLast(b, AstCode.Switch, switchLabels, condition) &&
                                    !ArrayUtilities.isNullOrEmpty(switchLabels.get()) &&
                                    exitInfo.exitLabel == switchLabels.get()[0]) {

                                    final Set<ControlFlowNode> defaultContents = findDominatedNodes(scope, postLoopTarget);

                                    for (final ControlFlowNode n : defaultContents) {
                                        if (scope.contains(n) && node.dominates(n)) {
                                            loopContents.add(n);
                                        }
                                    }
                                }
                            }
                        }

                        if (!loopContents.contains(postLoopTarget)) {
                            //
                            // Pull more nodes into the loop.
                            //
                            final Set<ControlFlowNode> postLoopContents = findDominatedNodes(scope, postLoopTarget);
                            final LinkedHashSet<ControlFlowNode> pullIn = new LinkedHashSet<>(scope);

                            pullIn.removeAll(postLoopContents);

                            for (final ControlFlowNode n : pullIn) {
                                if (n.getBlockIndex() < postLoopTarget.getBlockIndex() && scope.contains(n) && node.dominates(n)) {
                                    loopContents.add(n);
                                }
                            }
                        }
                    }
                    else if (exitInfo.additionalNodes.size() == 1) {
                        final ControlFlowNode postLoopTarget = first(exitInfo.additionalNodes);
                        final BasicBlock postLoopBlock = (BasicBlock) postLoopTarget.getUserData();
                        final Node postLoopBlockHead = firstOrDefault(postLoopBlock.getBody());

                        //
                        // See if our only exit comes from an inner switch's default label.  If so, pull it in
                        // to the loop if there are no other references.
                        //

                        final ControlFlowNode predecessor = single(postLoopTarget.getPredecessors());

                        if (postLoopBlockHead instanceof Label &&
                            loopContents.contains(predecessor)) {

                            final BasicBlock b = (BasicBlock) predecessor.getUserData();

                            if (matchLast(b, AstCode.Switch, switchLabels, condition) &&
                                !ArrayUtilities.isNullOrEmpty(switchLabels.get()) &&
                                postLoopBlockHead == switchLabels.get()[0]) {

                                final Set<ControlFlowNode> defaultContents = findDominatedNodes(scope, postLoopTarget);

                                for (final ControlFlowNode n : defaultContents) {
                                    if (scope.contains(n) && node.dominates(n)) {
                                        loopContents.add(n);
                                    }
                                }
                            }
                        }
                    }
                    else if (exitInfo.additionalNodes.size() > 1) {
                        final Set<ControlFlowNode> auxNodes = new LinkedHashSet<>();

                        //
                        // Pull more nodes into the loop, but only if we have more than one external jump.
                        // See ExceptionTestFinally19f for a good example of why we require more than one.
                        //

                        for (final ControlFlowNode n : exitInfo.additionalNodes) {
                            if (scope.contains(n) && node.dominates(n)) {
                                auxNodes.addAll(findDominatedNodes(scope, n));
                            }
                        }

                        final List<ControlFlowNode> sortedNodes = toList(auxNodes);

                        Collections.sort(sortedNodes);

                        loopContents.addAll(sortedNodes);
                    }

                    bodyBlock.setEntryGoto(new Expression(AstCode.Goto, basicBlock.getBody().get(0), Expression.MYSTERY_OFFSET));
                    bodyBlock.getBody().addAll(findLoops(loopContents, node, true));

                    blockBody.add(new Label("Loop_" + _nextLabelIndex++));
                    blockBody.add(loop);

                    result.add(block);
                    scope.removeAll(loopContents);
                }
            }

            //
            // Using the dominator tree should ensure we find the widest loop first.
            //
            for (final ControlFlowNode child : node.getDominatorTreeChildren()) {
                agenda.addLast(child);
            }
        }

        //
        // Add whatever is left.
        //

        for (final ControlFlowNode node : scope) {
            result.add((Node) node.getUserData());
        }

        scope.clear();

        return result;
    }

    private LoopExitInfo findLoopExitInfo(final Set<ControlFlowNode> contents) {
        final LoopExitInfo exitInfo = new LoopExitInfo();

        boolean noCommonExit = false;

        for (final ControlFlowNode node : contents) {
            final BasicBlock basicBlock = (BasicBlock) node.getUserData();

            for (final Expression e : basicBlock.getSelfAndChildrenRecursive(Expression.class)) {
                for (final Label target : e.getBranchTargets()) {
                    final ControlFlowNode targetNode = labelsToNodes.get(target);

                    if (targetNode == null || contents.contains(targetNode)) {
                        continue;
                    }

                    if (targetNode.getIncoming().size() == 1) {
                        exitInfo.additionalNodes.add(targetNode);
                    }
                    else if (exitInfo.exitLabel == null) {
                        exitInfo.exitLabel = target;
                    }
                    else if (exitInfo.exitLabel != target) {
                        noCommonExit = true;
                    }
                }
            }
        }

        if (noCommonExit) {
            exitInfo.exitLabel = null;
        }

        return exitInfo;
    }

    private final static class LoopExitInfo {
        Label exitLabel;
        final Set<ControlFlowNode> additionalNodes = new LinkedHashSet<>();
    }

    private int countJumps(final Set<ControlFlowNode> nodes, final Label target, final Expression ignore) {
        int jumpCount = 0;

        for (final ControlFlowNode node : nodes) {
            final BasicBlock basicBlock = (BasicBlock) node.getUserData();

            for (final Expression e : basicBlock.getSelfAndChildrenRecursive(Expression.class)) {
                if (e != ignore && e.getBranchTargets().contains(target)) {
                    ++jumpCount;
                }
            }
        }

        return jumpCount;
    }

    private static Set<ControlFlowNode> findLoopContents(final Set<ControlFlowNode> scope, final ControlFlowNode head) {
        final Set<ControlFlowNode> viaBackEdges = new LinkedHashSet<>();

        for (final ControlFlowNode predecessor : head.getPredecessors()) {
            if (head.dominates(predecessor)) {
                viaBackEdges.add(predecessor);
            }
        }

        final Set<ControlFlowNode> agenda = new LinkedHashSet<>(viaBackEdges);
        final Set<ControlFlowNode> result = new LinkedHashSet<>();

        while (!agenda.isEmpty()) {
            final ControlFlowNode addNode = agenda.iterator().next();

            agenda.remove(addNode);

            if (scope.contains(addNode) && head.dominates(addNode) && result.add(addNode)) {
                for (final ControlFlowNode predecessor : addNode.getPredecessors()) {
                    agenda.add(predecessor);
                }
            }
        }

        if (scope.contains(head)) {
            result.add(head);
        }

        if (result.size() <= 1) {
            return result;
        }

        final List<ControlFlowNode> sortedResult = new ArrayList<>(result);

        Collections.sort(
            sortedResult,
            new Comparator<ControlFlowNode>() {
                @Override
                public int compare(@NotNull final ControlFlowNode o1, @NotNull final ControlFlowNode o2) {
                    return Integer.compare(o1.getBlockIndex(), o2.getBlockIndex());
                }
            }
        );

        result.clear();
        result.addAll(sortedResult);

        return result;
    }

    @SuppressWarnings("ConstantConditions")
    private List<Node> findConditions(final Set<ControlFlowNode> scopeNodes, final ControlFlowNode entryNode) {
        final List<Node> result = new ArrayList<>();
        final Set<ControlFlowNode> scope = new HashSet<>(scopeNodes);
        final Stack<ControlFlowNode> agenda = new Stack<>();

        agenda.push(entryNode);

        while (!agenda.isEmpty()) {
            final ControlFlowNode node = agenda.pop();

            if (node == null) {
                continue;
            }

            //
            // Find a block that represents a simple condition.
            //

            if (scope.contains(node)) {
                final BasicBlock block = (BasicBlock) node.getUserData();
                final List<Node> blockBody = block.getBody();

                final StrongBox<Label[]> caseLabels = new StrongBox<>();
                final StrongBox<Expression> switchArgument = new StrongBox<>();
                final StrongBox<Label> tempTarget = new StrongBox<>();

                if (matchLast(block, AstCode.Switch, caseLabels, switchArgument)) {
                    final Expression switchExpression = (Expression) blockBody.get(blockBody.size() - 1);

                    //
                    // Replace the switch code with a Switch node.
                    //

                    final Switch switchNode = new Switch();

                    switchNode.setCondition(switchArgument.get());
                    removeTail(blockBody, AstCode.Switch);
                    blockBody.add(switchNode);
                    result.add(block);

                    //
                    // Replace the item so it isn't picked up as content.
                    //

                    removeOrThrow(scope, node);

                    //
                    // Pull in code of cases.
                    //

                    final Label[] labels = caseLabels.get();
                    final SwitchInfo switchInfo = switchExpression.getUserData(AstKeys.SWITCH_INFO);
                    final int lowValue = switchInfo.getLowValue();
                    final int[] keys = switchInfo.getKeys();
                    final Label defaultLabel = labels[0];
                    final ControlFlowNode defaultTarget = labelsToNodes.get(defaultLabel);

                    boolean defaultFollowsSwitch = false;

                    for (int i = 1; i < labels.length; i++) {
                        final Label caseLabel = labels[i];

                        if (caseLabel == defaultLabel) {
                            continue;
                        }

                        //
                        // Find or create a new case block.
                        //

                        CaseBlock caseBlock = null;

                        for (final CaseBlock cb : switchNode.getCaseBlocks()) {
                            if (cb.getEntryGoto().getOperand() == caseLabel) {
                                caseBlock = cb;
                                break;
                            }
                        }

                        if (caseBlock == null) {
                            caseBlock = new CaseBlock();

                            caseBlock.setEntryGoto(new Expression(AstCode.Goto, caseLabel, Expression.MYSTERY_OFFSET));

                            final ControlFlowNode caseTarget = labelsToNodes.get(caseLabel);
                            final List<Node> caseBody = caseBlock.getBody();

                            switchNode.getCaseBlocks().add(caseBlock);

                            if (caseTarget != null) {
                                if (caseTarget.getDominanceFrontier().contains(defaultTarget)) {
                                    defaultFollowsSwitch = true;
                                }

                                final Set<ControlFlowNode> content = findDominatedNodes(scope, caseTarget);

                                scope.removeAll(content);
                                caseBody.addAll(findConditions(content, caseTarget));
                            }
                            else {
                                final BasicBlock explicitGoto = new BasicBlock();

                                explicitGoto.getBody().add(new Label("SwitchGoto_" + _nextLabelIndex++));
                                explicitGoto.getBody().add(new Expression(AstCode.Goto, caseLabel, Expression.MYSTERY_OFFSET));

                                caseBody.add(explicitGoto);
                            }

                            if (caseBody.isEmpty() ||
                                !matchLast((BasicBlock) caseBody.get(caseBody.size() - 1), AstCode.Goto, tempTarget) ||
                                !ArrayUtilities.contains(labels, tempTarget.get())) {

                                //
                                // Add explicit break that should not be used by default, but which might be used
                                // by goto removal.
                                //

                                final BasicBlock explicitBreak = new BasicBlock();

                                explicitBreak.getBody().add(new Label("SwitchBreak_" + _nextLabelIndex++));
                                explicitBreak.getBody().add(new Expression(AstCode.LoopOrSwitchBreak, null, Expression.MYSTERY_OFFSET));

                                caseBody.add(explicitBreak);
                            }
                        }

                        if (switchInfo.hasKeys()) {
                            caseBlock.getValues().add(keys[i - 1]);
                        }
                        else {
                            caseBlock.getValues().add(lowValue + i - 1);
                        }
                    }

                    if (!defaultFollowsSwitch) {
                        final CaseBlock defaultBlock = new CaseBlock();

                        defaultBlock.setEntryGoto(new Expression(AstCode.Goto, defaultLabel, Expression.MYSTERY_OFFSET));

                        switchNode.getCaseBlocks().add(defaultBlock);

                        final Set<ControlFlowNode> content = findDominatedNodes(scope, defaultTarget);

                        scope.removeAll(content);
                        defaultBlock.getBody().addAll(findConditions(content, defaultTarget));

                        //
                        // Add explicit break that should not be used by default, but which might be used
                        // by goto removal.
                        //

                        final BasicBlock explicitBreak = new BasicBlock();

                        explicitBreak.getBody().add(new Label("SwitchBreak_" + _nextLabelIndex++));
                        explicitBreak.getBody().add(new Expression(AstCode.LoopOrSwitchBreak, null, Expression.MYSTERY_OFFSET));

                        defaultBlock.getBody().add(explicitBreak);
                    }

                    reorderCaseBlocks(switchNode);
                }

                //
                // Two-way branch...
                //
                final StrongBox<Expression> condition = new StrongBox<>();
                final StrongBox<Label> trueLabel = new StrongBox<>();
                final StrongBox<Label> falseLabel = new StrongBox<>();

                if (matchLastAndBreak(block, AstCode.IfTrue, trueLabel, condition, falseLabel)) {
                    //
                    // Flip bodies since that seems to be the Java compiler tradition.
                    //

                    final Label temp = trueLabel.get();

                    trueLabel.set(falseLabel.get());
                    falseLabel.set(temp);
                    condition.set(AstOptimizer.simplifyLogicalNot(new Expression(AstCode.LogicalNot, null, condition.get().getOffset(), condition.get())));

                    //
                    // Convert IfTrue expression to Condition.
                    //

                    final Condition conditionNode = new Condition();
                    final Block trueBlock = new Block();
                    final Block falseBlock = new Block();

                    conditionNode.setCondition(condition.get());
                    conditionNode.setTrueBlock(trueBlock);
                    conditionNode.setFalseBlock(falseBlock);

                    removeTail(blockBody, AstCode.IfTrue, AstCode.Goto);
                    blockBody.add(conditionNode);
                    result.add(block);

                    //
                    // Remove the item immediately so it isn't picked up as content.
                    //
                    removeOrThrow(scope, node);

                    //
                    // Assuming the `if` block isn't empty, pull in the child blocks.
                    //
                    if (falseLabel.get() != trueLabel.get()) {
                        trueBlock.setEntryGoto(new Expression(AstCode.Goto, trueLabel.get(), Expression.MYSTERY_OFFSET));
                        falseBlock.setEntryGoto(new Expression(AstCode.Goto, falseLabel.get(), Expression.MYSTERY_OFFSET));

                        final ControlFlowNode trueTarget = labelsToNodes.get(trueLabel.get());
                        final ControlFlowNode falseTarget = labelsToNodes.get(falseLabel.get());

                        //
                        // Pull in the conditional code.
                        //

                        if (trueTarget != null && hasSingleEdgeEnteringBlock(trueTarget)) {
                            final Set<ControlFlowNode> content = findDominatedNodes(scope, trueTarget);
                            scope.removeAll(content);
                            conditionNode.getTrueBlock().getBody().addAll(findConditions(content, trueTarget));
                        }

                        if (falseTarget != null && hasSingleEdgeEnteringBlock(falseTarget)) {
                            final Set<ControlFlowNode> content = findDominatedNodes(scope, falseTarget);
                            scope.removeAll(content);
                            conditionNode.getFalseBlock().getBody().addAll(findConditions(content, falseTarget));
                        }
                    }
                }

                //
                // Add the node now so that we have good ordering.
                //
                if (scope.contains(node)) {
                    result.add((Node) node.getUserData());
                    scope.remove(node);
                }
            }

            //
            // Depth-first traversal of dominator tree.
            //

            final List<ControlFlowNode> dominatorTreeChildren = node.getDominatorTreeChildren();

            for (int i = dominatorTreeChildren.size() - 1; i >= 0; i--) {
                agenda.push(dominatorTreeChildren.get(i));
            }
        }

        //
        // Add whatever is left.
        //
        for (final ControlFlowNode node : scope) {
            result.add((Node) node.getUserData());
        }

        return result;
    }

    private void reorderCaseBlocks(final Switch switchNode) {
        Collections.sort(
            switchNode.getCaseBlocks(),
            new Comparator<CaseBlock>() {
                @Override
                public int compare(@NotNull final CaseBlock o1, @NotNull final CaseBlock o2) {
                    final Label l1 = (Label) o1.getEntryGoto().getOperand();
                    final Label l2 = (Label) o2.getEntryGoto().getOperand();

                    return Integer.compare(l1.getOffset(), l2.getOffset());
                }
            }
        );

        final List<CaseBlock> caseBlocks = switchNode.getCaseBlocks();
        final Map<Label, Pair<CaseBlock, Integer>> caseLookup = new IdentityHashMap<>();

        for (int i = 0; i < caseBlocks.size(); i++) {
            final CaseBlock block = caseBlocks.get(i);
            caseLookup.put((Label) block.getEntryGoto().getOperand(), Pair.create(block, i));
        }

        final StrongBox<Label> label = new StrongBox<>();
        final Set<CaseBlock> movedBlocks = new HashSet<>();

        for (int i = 0; i < caseBlocks.size(); i++) {
            final CaseBlock block = caseBlocks.get(i);
            final List<Node> caseBody = block.getBody();

            Node lastInCase = lastOrDefault(caseBody);

            if (lastInCase instanceof BasicBlock) {
                lastInCase = lastOrDefault(((BasicBlock) lastInCase).getBody());
            }
            else if (lastInCase instanceof Block) {
                lastInCase = lastOrDefault(((Block) lastInCase).getBody());
            }

            if (matchGetOperand(lastInCase, AstCode.Goto, label)) {
                final Pair<CaseBlock, Integer> caseInfo = caseLookup.get(label.get());

                if (caseInfo == null) {
                    continue;
                }

                //
                // We have a switch section that should fall through to another section.  Make sure
                // we are positioned immediately before the fall through target.
                //

                final int targetIndex = caseInfo.getSecond();

                if (targetIndex == i + 1 || movedBlocks.contains(block)) {
                    continue;
                }

                caseBlocks.remove(i);
                caseBlocks.add(targetIndex, block);
                movedBlocks.add(block);

                if (targetIndex > i) {
                    --i;
                }
            }
        }
    }

    private static boolean hasSingleEdgeEnteringBlock(final ControlFlowNode node) {
        int count = 0;

        for (final ControlFlowEdge edge : node.getIncoming()) {
            if (!node.dominates(edge.getSource())) {
                if (++count > 1) {
                    return false;
                }
            }
        }

        return count == 1;
    }

    private static Set<ControlFlowNode> findDominatedNodes(final Set<ControlFlowNode> scope, final ControlFlowNode head) {
        final Set<ControlFlowNode> agenda = new LinkedHashSet<>();
        final Set<ControlFlowNode> result = new LinkedHashSet<>();

        agenda.add(head);

        while (!agenda.isEmpty()) {
            final ControlFlowNode addNode = agenda.iterator().next();

            agenda.remove(addNode);

            if (scope.contains(addNode) && head.dominates(addNode) && result.add(addNode)) {
                for (final ControlFlowNode successor : addNode.getSuccessors()) {
                    agenda.add(successor);
                }
            }
        }

        return result;
    }
}
