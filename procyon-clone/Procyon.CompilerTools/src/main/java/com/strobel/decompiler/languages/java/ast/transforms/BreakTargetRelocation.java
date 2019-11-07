/*
 * BreakTargetRelocation.java
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

package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.core.Predicate;
import com.strobel.core.StringUtilities;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.functions.Function;

import java.util.*;

import static com.strobel.core.CollectionUtilities.*;

public final class BreakTargetRelocation extends ContextTrackingVisitor<Void> {
    public BreakTargetRelocation(final DecompilerContext context) {
        super(context);
    }

    private final static class LabelInfo {
        final String name;
        final List<GotoStatement> gotoStatements = new ArrayList<>();

        boolean labelIsLast;
        LabelStatement label;
        AstNode labelTarget;
        LabeledStatement newLabeledStatement;

        LabelInfo(final String name) {
            this.name = name;
        }

        LabelInfo(final LabelStatement label) {
            this.label = label;
            this.labelTarget = label.getNextSibling();
            this.name = label.getLabel();
        }
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public Void visitMethodDeclaration(final MethodDeclaration node, final Void p) {
        super.visitMethodDeclaration(node, p);

        runForMethod(node);

        return null;
    }

    @Override
    public Void visitConstructorDeclaration(final ConstructorDeclaration node, final Void p) {
        super.visitConstructorDeclaration(node, p);

        runForMethod(node);

        return null;
    }

    private void runForMethod(final AstNode node) {
        final Map<String, LabelInfo> labels = new LinkedHashMap<>();

        for (final AstNode n : node.getDescendantsAndSelf()) {
            if (n instanceof LabelStatement) {
                final LabelStatement label = (LabelStatement) n;
                final LabelInfo labelInfo = labels.get(label.getLabel());

                if (labelInfo == null) {
                    labels.put(label.getLabel(), new LabelInfo(label));
                }
                else {
                    labelInfo.label = label;
                    labelInfo.labelTarget = label.getNextSibling();
                    labelInfo.labelIsLast = true;
                }
            }
            else if (n instanceof GotoStatement) {
                final GotoStatement gotoStatement = (GotoStatement) n;

                LabelInfo labelInfo = labels.get(gotoStatement.getLabel());

                if (labelInfo == null) {
                    labels.put(gotoStatement.getLabel(), labelInfo = new LabelInfo(gotoStatement.getLabel()));
                }
                else {
                    labelInfo.labelIsLast = false;
                }

                labelInfo.gotoStatements.add(gotoStatement);
            }
        }

        for (final LabelInfo labelInfo : labels.values()) {
            run(labelInfo);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void run(final LabelInfo labelInfo) {
        assert labelInfo != null;

        final LabelStatement label = labelInfo.label;

        if (label == null || labelInfo.gotoStatements.isEmpty()) {
            return;
        }

        final List<Stack<AstNode>> paths = new ArrayList<>();

        for (final GotoStatement gotoStatement : labelInfo.gotoStatements) {
            paths.add(buildPath(gotoStatement));
        }

        paths.add(buildPath(label));

        final Statement commonAncestor = findLowestCommonAncestor(paths);

        if (commonAncestor instanceof SwitchStatement &&
            labelInfo.gotoStatements.size() == 1 &&
            label.getParent() instanceof BlockStatement &&
            label.getParent().getParent() instanceof SwitchSection &&
            label.getParent().getParent().getParent() == commonAncestor) {

            final GotoStatement s = labelInfo.gotoStatements.get(0);

            if (s.getParent() instanceof BlockStatement &&
                s.getParent().getParent() instanceof SwitchSection &&
                s.getParent().getParent().getParent() == commonAncestor) {

                //
                // We have a switch section that should fall through to another section.
                // Make sure the fall through target is positioned after the section with
                // the goto, then remove the goto and the target label.
                //

                final SwitchStatement parentSwitch = (SwitchStatement) commonAncestor;

                final SwitchSection targetSection = (SwitchSection) label.getParent().getParent();
                final BlockStatement fallThroughBlock = (BlockStatement) s.getParent();
                final SwitchSection fallThroughSection = (SwitchSection) fallThroughBlock.getParent();

                if (fallThroughSection.getNextSibling() != targetSection) {
                    fallThroughSection.remove();
                    parentSwitch.getSwitchSections().insertBefore(targetSection, fallThroughSection);
                }

                final BlockStatement parentBlock = (BlockStatement) label.getParent();

                s.remove();
                label.remove();

                if (fallThroughBlock.getStatements().isEmpty()) {
                    fallThroughBlock.remove();
                }

                if (parentBlock.getStatements().isEmpty()) {
                    parentBlock.remove();
                }

                return;
            }
        }

        paths.clear();

        for (final GotoStatement gotoStatement : labelInfo.gotoStatements) {
            paths.add(buildPath(gotoStatement));
        }

        paths.add(buildPath(label));

        final BlockStatement parent = findLowestCommonAncestorBlock(paths);

        if (parent == null) {
            return;
        }

        if (convertToContinue(parent, labelInfo, paths)) {
            return;
        }

        final Set<AstNode> remainingNodes = new LinkedHashSet<>();
        final LinkedList<AstNode> orderedNodes = new LinkedList<>();
        final AstNode startNode = paths.get(0).peek();

        assert startNode != null;

        for (final Stack<AstNode> path : paths) {
            if (path.isEmpty()) {
                return;
            }
            remainingNodes.add(path.peek());
        }

        AstNode current = startNode;

        while (lookAhead(current, remainingNodes)) {
            for (; current != null && !remainingNodes.isEmpty(); current = current.getNextSibling()) {
                if (current instanceof Statement) {
                    orderedNodes.addLast(current);
                }

                if (remainingNodes.remove(current)) {
                    break;
                }
            }
        }

        if (!remainingNodes.isEmpty()) {
            current = startNode.getPreviousSibling();

            while (lookBehind(current, remainingNodes)) {
                for (; current != null && !remainingNodes.isEmpty(); current = current.getPreviousSibling()) {
                    if (current instanceof Statement) {
                        orderedNodes.addFirst(current);
                    }

                    if (remainingNodes.remove(current)) {
                        break;
                    }
                }
            }
        }

        if (!remainingNodes.isEmpty()) {
            return;
        }

        final AstNode insertBefore = orderedNodes.getLast().getNextSibling();
        final AstNode insertAfter = orderedNodes.getFirst().getPreviousSibling();

        final BlockStatement newBlock = new BlockStatement();
        final AstNodeCollection<Statement> blockStatements = newBlock.getStatements();

        final AssessForLoopResult loopData = assessForLoop(commonAncestor, paths, label, labelInfo.gotoStatements);
        final boolean rewriteAsLoop = !loopData.continueStatements.isEmpty();

        for (final AstNode node : orderedNodes) {
            node.remove();
            blockStatements.add((Statement) node);
        }

        label.remove();

        final Statement insertedStatement;

        if (rewriteAsLoop) {
            final WhileStatement loop = new WhileStatement(new PrimitiveExpression( Expression.MYSTERY_OFFSET, true));

            loop.setEmbeddedStatement(newBlock);

            if (!AstNode.isUnconditionalBranch(lastOrDefault(newBlock.getStatements()))) {
                newBlock.getStatements().add(new BreakStatement(Expression.MYSTERY_OFFSET));
            }

            if (loopData.needsLabel) {
                final LabeledStatement labeledStatement = new LabeledStatement(label.getLabel(), loop);
                insertedStatement = labeledStatement;
                labelInfo.newLabeledStatement = labeledStatement;
            }
            else {
                insertedStatement = loop;
            }
        }
        else {
            if (newBlock.getStatements().hasSingleElement() && AstNode.isLoop(newBlock.getStatements().firstOrNullObject())) {
                final Statement loop = newBlock.getStatements().firstOrNullObject();

                loop.remove();

                final LabeledStatement labeledStatement = new LabeledStatement(label.getLabel(), loop);
                insertedStatement = labeledStatement;
                labelInfo.newLabeledStatement = labeledStatement;
            }
            else {
                final LabeledStatement labeledStatement = new LabeledStatement(label.getLabel(), newBlock);
                insertedStatement = labeledStatement;
                labelInfo.newLabeledStatement = labeledStatement;
            }
        }

        if (parent.getParent() instanceof LabelStatement) {
            AstNode insertionPoint = parent;

            while (insertionPoint != null && insertionPoint.getParent() instanceof LabelStatement) {
                insertionPoint = firstOrDefault(insertionPoint.getAncestors(BlockStatement.class));
            }

            if (insertionPoint == null) {
                return;
            }

            insertionPoint.addChild(insertedStatement, BlockStatement.STATEMENT_ROLE);
        }
        else if (insertBefore != null) {
            parent.insertChildBefore(insertBefore, insertedStatement, BlockStatement.STATEMENT_ROLE);
        }
        else if (insertAfter != null) {
            parent.insertChildAfter(insertAfter, insertedStatement, BlockStatement.STATEMENT_ROLE);
        }
        else {
            parent.getStatements().add(insertedStatement);
        }

        for (final GotoStatement gotoStatement : labelInfo.gotoStatements) {
            if (loopData.continueStatements.contains(gotoStatement)) {
                final ContinueStatement continueStatement = new ContinueStatement(Expression.MYSTERY_OFFSET);

                if (loopData.needsLabel) {
                    continueStatement.setLabel(gotoStatement.getLabel());
                }

                gotoStatement.replaceWith(continueStatement);
            }
            else {
                final BreakStatement breakStatement = new BreakStatement(Expression.MYSTERY_OFFSET);

                breakStatement.setLabel(gotoStatement.getLabel());

                gotoStatement.replaceWith(breakStatement);
            }
        }

        if (rewriteAsLoop && !loopData.preexistingContinueStatements.isEmpty()) {
            final AstNode existingLoop = firstOrDefault(
                insertedStatement.getAncestors(),
                new Predicate<AstNode>() {
                    @Override
                    public boolean test(final AstNode node) {
                        return AstNode.isLoop(node);
                    }
                }
            );

            if (existingLoop != null) {
                final String loopLabel = label.getLabel() + "_Outer";

                existingLoop.replaceWith(
                    new Function<AstNode, AstNode>() {
                        @Override
                        public AstNode apply(final AstNode input) {
                            return new LabeledStatement(loopLabel, (Statement) existingLoop);
                        }
                    }
                );

                for (final ContinueStatement statement : loopData.preexistingContinueStatements) {
                    statement.setLabel(loopLabel);
                }
            }
        }
    }

    private boolean convertToContinue(final BlockStatement parent, final LabelInfo labelInfo, final List<Stack<AstNode>> paths) {
        if (!AstNode.isLoop(parent.getParent())) {
            return false;
        }

        final AstNode loop = parent.getParent();
        final AstNode nextAfterLoop = loop.getNextNode();

        AstNode n = labelInfo.label;

        while (n.getNextSibling() == null) {
            n = n.getParent();
        }

        n = n.getNextSibling();

        final boolean isContinue = n == nextAfterLoop ||
                                   (loop instanceof ForStatement &&
                                    n.getRole() == ForStatement.ITERATOR_ROLE &&
                                    n.getParent() == loop);

        if (!isContinue) {
            return false;
        }

        boolean loopNeedsLabel = false;

        for (final AstNode node : loop.getDescendantsAndSelf()) {
            if (node instanceof ContinueStatement &&
                StringUtilities.equals(((ContinueStatement) node).getLabel(), labelInfo.name)) {

                loopNeedsLabel = true;
            }
            else if (node instanceof BreakStatement &&
                     StringUtilities.equals(((BreakStatement) node).getLabel(), labelInfo.name)) {

                loopNeedsLabel = true;
            }
        }

        for (final Stack<AstNode> path : paths) {
            final AstNode start = path.firstElement();

            boolean continueNeedsLabel = false;

            if (start instanceof GotoStatement) {
                for (AstNode node = start;
                     node != null && node != loop;
                     node = node.getParent()) {

                    if (AstNode.isLoop(node)) {
                        loopNeedsLabel = continueNeedsLabel = true;
                        break;
                    }
                }

                final int offset = ((GotoStatement) start).getOffset();

                if (continueNeedsLabel) {
                    start.replaceWith(new ContinueStatement(offset, labelInfo.name));
                }
                else {
                    start.replaceWith(new ContinueStatement(offset));
                }
            }
        }

        labelInfo.label.remove();

        if (loopNeedsLabel) {
            loop.replaceWith(
                new Function<AstNode, AstNode>() {
                    @Override
                    public AstNode apply(final AstNode input) {
                        return new LabeledStatement(labelInfo.name, (Statement) input);
                    }
                }
            );
        }

        return true;
    }

    private final static class AssessForLoopResult {
        final boolean needsLabel;
        final Set<GotoStatement> continueStatements;
        final Set<ContinueStatement> preexistingContinueStatements;

        private AssessForLoopResult(
            final boolean needsLabel,
            final Set<GotoStatement> continueStatements,
            final Set<ContinueStatement> preexistingContinueStatements) {

            this.needsLabel = needsLabel;
            this.continueStatements = continueStatements;
            this.preexistingContinueStatements = preexistingContinueStatements;
        }
    }

    private AssessForLoopResult assessForLoop(
        final AstNode commonAncestor,
        final List<Stack<AstNode>> paths,
        final LabelStatement label,
        final List<GotoStatement> statements) {

        final Set<GotoStatement> gotoStatements = new HashSet<>(statements);
        final Set<GotoStatement> continueStatements = new HashSet<>();
        final Set<ContinueStatement> preexistingContinueStatements = new HashSet<>();

        boolean labelSeen = false;
        boolean loopEncountered = false;

        for (final Stack<AstNode> path : paths) {
            if (firstOrDefault(path) == label) {
                continue;
            }

            loopEncountered = any(
                path,
                new Predicate<AstNode>() {
                    @Override
                    public boolean test(final AstNode node) {
                        return AstNode.isLoop(node);
                    }
                }
            );

            if (loopEncountered) {
                break;
            }
        }

        for (final AstNode node : commonAncestor.getDescendantsAndSelf()) {
            if (node == label) {
                labelSeen = true;
            }
            else if (labelSeen && node instanceof GotoStatement && gotoStatements.contains(node)) {
                continueStatements.add((GotoStatement) node);
            }
            else if (node instanceof ContinueStatement &&
                     StringUtilities.isNullOrEmpty(((ContinueStatement) node).getLabel())) {

                preexistingContinueStatements.add((ContinueStatement) node);
            }
        }

        return new AssessForLoopResult(loopEncountered, continueStatements, preexistingContinueStatements);
    }

    private static boolean lookAhead(final AstNode start, final Set<AstNode> targets) {
        for (AstNode current = start;
             current != null && !targets.isEmpty();
             current = current.getNextSibling()) {

            if (targets.contains(current)) {
                return true;
            }
        }
        return false;
    }

    private static boolean lookBehind(final AstNode start, final Set<AstNode> targets) {
        for (AstNode current = start;
             current != null && !targets.isEmpty();
             current = current.getPreviousSibling()) {

            if (targets.contains(current)) {
                return true;
            }
        }
        return false;
    }

    private BlockStatement findLowestCommonAncestorBlock(final List<Stack<AstNode>> paths) {
        if (paths.isEmpty()) {
            return null;
        }

        AstNode current = null;
        BlockStatement match = null;

        final Stack<AstNode> sinceLastMatch = new Stack<>();

    outer:
        while (true) {
            for (final Stack<AstNode> path : paths) {
                if (path.isEmpty()) {
                    break outer;
                }

                if (current == null) {
                    current = path.peek();
                }
                else if (path.peek() != current) {
                    break outer;
                }
            }

            for (final Stack<AstNode> path : paths) {
                path.pop();
            }

            if (current instanceof BlockStatement) {
                sinceLastMatch.clear();
                match = (BlockStatement) current;
            }
            else {
                sinceLastMatch.push(current);
            }

            current = null;
        }

        while (!sinceLastMatch.isEmpty()) {
            for (int i = 0, n = paths.size(); i < n; i++) {
                paths.get(i).push(sinceLastMatch.peek());
            }
            sinceLastMatch.pop();
        }

        return match;
    }

    private Statement findLowestCommonAncestor(final List<Stack<AstNode>> paths) {
        if (paths.isEmpty()) {
            return null;
        }

        AstNode current = null;
        Statement match = null;

        final Stack<AstNode> sinceLastMatch = new Stack<>();

    outer:
        while (true) {
            for (final Stack<AstNode> path : paths) {
                if (path.isEmpty()) {
                    break outer;
                }

                if (current == null) {
                    current = path.peek();
                }
                else if (path.peek() != current) {
                    break outer;
                }
            }

            for (final Stack<AstNode> path : paths) {
                path.pop();
            }

            if (current instanceof Statement) {
                sinceLastMatch.clear();
                match = (Statement) current;
            }
            else {
                sinceLastMatch.push(current);
            }

            current = null;
        }

        while (!sinceLastMatch.isEmpty()) {
            for (int i = 0, n = paths.size(); i < n; i++) {
                paths.get(i).push(sinceLastMatch.peek());
            }
            sinceLastMatch.pop();
        }

        return match;
    }

    private Stack<AstNode> buildPath(final AstNode node) {
        assert node != null;

        final Stack<AstNode> path = new Stack<>();

        path.push(node);

        for (AstNode current = node; current != null; current = current.getParent()) {
            path.push(current);

            if (current instanceof MethodDeclaration) {
                break;
            }
        }

        return path;
    }
}
