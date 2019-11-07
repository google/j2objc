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

package com.strobel.decompiler.languages.java.analysis;

import com.strobel.core.Comparer;
import com.strobel.core.StringUtilities;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.decompiler.semantics.ResolveResult;
import com.strobel.functions.Function;
import com.strobel.util.ContractUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class ControlFlowGraphBuilder {
    private Statement rootStatement;
    private Function<AstNode, ResolveResult> resolver;
    private ArrayList<ControlFlowNode> nodes;
    private HashMap<String, ControlFlowNode> labels;
    private ArrayList<ControlFlowNode> gotoStatements;

    protected ControlFlowNode createNode(
        final Statement previousStatement,
        final Statement nextStatement,
        final ControlFlowNodeType type) {

        return new ControlFlowNode(previousStatement, nextStatement, type);
    }

    protected ControlFlowNode createStartNode(final Statement statement) {
        final ControlFlowNode node = createNode(null, statement, ControlFlowNodeType.StartNode);
        nodes.add(node);
        return node;
    }

    protected ControlFlowNode createSpecialNode(final Statement statement, final ControlFlowNodeType type) {
        return createSpecialNode(statement, type, true);
    }

    protected ControlFlowNode createSpecialNode(final Statement statement, final ControlFlowNodeType type, final boolean addNodeToList) {
        final ControlFlowNode node = createNode(null, statement, type);

        if (addNodeToList) {
            nodes.add(node);
        }

        return node;
    }

    protected ControlFlowNode createEndNode(final Statement statement) {
        return createEndNode(statement, true);
    }

    protected ControlFlowNode createEndNode(final Statement statement, final boolean addNodeToList) {
        Statement nextStatement = null;

        if (statement == rootStatement) {
            nextStatement = null;
        }
        else {
            //
            // Find the next statement in the same role.
            //
            AstNode next = statement;

            do {
                next = next.getNextSibling();
            }
            while (next != null && next.getRole() != statement.getRole());

            if (next instanceof Statement) {
                nextStatement = (Statement) next;
            }
        }

        final ControlFlowNodeType type = nextStatement != null ? ControlFlowNodeType.BetweenStatements
                                                               : ControlFlowNodeType.EndNode;

        final ControlFlowNode node = createNode(statement, nextStatement, type);

        if (addNodeToList) {
            nodes.add(node);
        }

        return node;
    }

    protected ControlFlowEdge createEdge(
        final ControlFlowNode from,
        final ControlFlowNode to,
        final ControlFlowEdgeType type) {

        return new ControlFlowEdge(from, to, type);
    }

    public List<ControlFlowNode> buildControlFlowGraph(
        final Statement statement,
        final Function<AstNode, ResolveResult> resolver) {

        final NodeCreationVisitor nodeCreationVisitor = new NodeCreationVisitor();

        try {
            this.nodes = new ArrayList<>();
            this.labels = new HashMap<>();
            this.gotoStatements = new ArrayList<>();
            this.rootStatement = statement;
            this.resolver = resolver;

            final ControlFlowNode entryPoint = createStartNode(statement);

            statement.acceptVisitor(nodeCreationVisitor, entryPoint);

            // Resolve goto statements:
            for (final ControlFlowNode gotoStatement : gotoStatements) {
                final String label;
                final ControlFlowNode labelNode;

                if (gotoStatement.getNextStatement() instanceof BreakStatement) {
                    label = ((BreakStatement) gotoStatement.getNextStatement()).getLabel();
                }
                else if (gotoStatement.getNextStatement() instanceof ContinueStatement) {
                    label = ((ContinueStatement) gotoStatement.getNextStatement()).getLabel();
                }
                else {
                    label = ((GotoStatement) gotoStatement.getNextStatement()).getLabel();
                }

                labelNode = labels.get(label);

                if (labelNode != null) {
                    nodeCreationVisitor.connect(gotoStatement, labelNode, ControlFlowEdgeType.Jump);
                }
            }

            annotateLeaveEdgesWithTryFinallyBlocks();

            return nodes;
        }
        finally {
            this.nodes = null;
            this.labels = null;
            this.gotoStatements = null;
            this.rootStatement = null;
            this.resolver = null;
        }
    }

    final void annotateLeaveEdgesWithTryFinallyBlocks() {
        for (final ControlFlowNode n : nodes) {
            for (final ControlFlowEdge edge : n.getOutgoing()) {
                if (edge.getType() != ControlFlowEdgeType.Jump) {
                    //
                    // Only jumps are candidates for leaving try-finally blocks.  Regular edges leaving
                    // try or catch blocks are already annotated by the visitor.
                    //
                    continue;
                }

                final Statement gotoStatement = edge.getFrom().getNextStatement();

                assert gotoStatement instanceof GotoStatement ||
                       gotoStatement instanceof BreakStatement ||
                       gotoStatement instanceof ContinueStatement;

                final Statement targetStatement = edge.getTo().getPreviousStatement() != null ? edge.getTo().getPreviousStatement()
                                                                                              : edge.getTo().getNextStatement();

                if (gotoStatement.getParent() == targetStatement.getParent()) {
                    continue;
                }

                final Set<TryCatchStatement> targetParentTryCatch = new LinkedHashSet<>();

                for (final AstNode ancestor : targetStatement.getAncestors()) {
                    if (ancestor instanceof TryCatchStatement) {
                        targetParentTryCatch.add((TryCatchStatement) ancestor);
                    }
                }

                for (AstNode node = gotoStatement.getParent(); node != null; node = node.getParent()) {
                    if (node instanceof TryCatchStatement) {
                        final TryCatchStatement leftTryCatch = (TryCatchStatement) node;

                        if (targetParentTryCatch.contains(leftTryCatch)) {
                            break;
                        }

                        if (!leftTryCatch.getFinallyBlock().isNull()) {
                            edge.AddJumpOutOfTryFinally(leftTryCatch);
                        }
                    }
                }
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Constant Evaluation">

    private boolean _evaluateOnlyPrimitiveConstants;

    public final boolean isEvaluateOnlyPrimitiveConstants() {
        return _evaluateOnlyPrimitiveConstants;
    }

    public final void setEvaluateOnlyPrimitiveConstants(final boolean evaluateOnlyPrimitiveConstants) {
        _evaluateOnlyPrimitiveConstants = evaluateOnlyPrimitiveConstants;
    }

    protected ResolveResult evaluateConstant(final Expression e) {
        if (_evaluateOnlyPrimitiveConstants) {
            if (!(e instanceof PrimitiveExpression || e instanceof NullReferenceExpression)) {
                return null;
            }
        }
        return resolver.apply(e);
    }

    private boolean areEqualConstants(final ResolveResult c1, final ResolveResult c2) {
        //noinspection SimplifiableIfStatement
        if (c1 == null || c2 == null || !c1.isCompileTimeConstant() || !c2.isCompileTimeConstant()) {
            return false;
        }

        return Comparer.equals(c1.getConstantValue(), c2.getConstantValue());
    }

    protected Boolean evaluateCondition(final Expression e) {
        final ResolveResult result = evaluateConstant(e);

        if (result != null && result.isCompileTimeConstant()) {
            final Object constantValue = result.getConstantValue();

            if (constantValue instanceof Boolean) {
                return (Boolean) constantValue;
            }

            return null;
        }

        return null;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="NodeCreationVisitor Class">

    final class NodeCreationVisitor extends DepthFirstAstVisitor<ControlFlowNode, ControlFlowNode> {
        final Stack<ControlFlowNode> breakTargets = new Stack<>();
        final Stack<ControlFlowNode> continueTargets = new Stack<>();
        final Stack<ControlFlowNode> gotoTargets = new Stack<>();

        final ControlFlowEdge connect(final ControlFlowNode from, final ControlFlowNode to) {
            return connect(from, to, ControlFlowEdgeType.Normal);
        }

        final ControlFlowEdge connect(final ControlFlowNode from, final ControlFlowNode to, final ControlFlowEdgeType type) {
            final ControlFlowEdge edge = ControlFlowGraphBuilder.this.createEdge(from, to, type);
            from.getOutgoing().add(edge);
            to.getIncoming().add(edge);
            return edge;
        }

        final ControlFlowNode createConnectedEndNode(final Statement statement, final ControlFlowNode from) {
            final ControlFlowNode newNode = ControlFlowGraphBuilder.this.createEndNode(statement);
            connect(from, newNode);
            return newNode;
        }

        final ControlFlowNode handleStatementList(final AstNodeCollection<Statement> statements, final ControlFlowNode source) {
            ControlFlowNode childNode = null;

            for (final Statement statement : statements) {
                if (childNode == null) {
                    childNode = createStartNode(statement);

                    if (source != null) {
                        connect(source, childNode);
                    }
                }

                assert childNode.getNextStatement() == statement;
                childNode = statement.acceptVisitor(this, childNode);
                assert childNode.getPreviousStatement() == statement;
            }

            return childNode != null ? childNode : source;
        }

        @Override
        protected ControlFlowNode visitChildren(final AstNode node, final ControlFlowNode data) {
            throw ContractUtils.unreachable();
        }

        @Override
        public ControlFlowNode visitBlockStatement(final BlockStatement node, final ControlFlowNode data) {
            final ControlFlowNode childNode = handleStatementList(node.getStatements(), data);
            return createConnectedEndNode(node, childNode);
        }

        @Override
        public ControlFlowNode visitEmptyStatement(final EmptyStatement node, final ControlFlowNode data) {
            return createConnectedEndNode(node, data);
        }

        @Override
        public ControlFlowNode visitLabelStatement(final LabelStatement node, final ControlFlowNode data) {
            final ControlFlowNode end = createConnectedEndNode(node, data);
            labels.put(node.getLabel(), end);
            return end;
        }

        @Override
        public ControlFlowNode visitLabeledStatement(final LabeledStatement node, final ControlFlowNode data) {
            final ControlFlowNode end = createConnectedEndNode(node, data);
            labels.put(node.getLabel(), end);
            connect(end, node.getStatement().acceptVisitor(this, data));
            return end;
        }

        @Override
        public ControlFlowNode visitVariableDeclaration(final VariableDeclarationStatement node, final ControlFlowNode data) {
            return createConnectedEndNode(node, data);
        }

        @Override
        public ControlFlowNode visitExpressionStatement(final ExpressionStatement node, final ControlFlowNode data) {
            return createConnectedEndNode(node, data);
        }

        @Override
        public ControlFlowNode visitIfElseStatement(final IfElseStatement node, final ControlFlowNode data) {
            final Boolean condition = evaluateCondition(node.getCondition());
            final ControlFlowNode trueBegin = createStartNode(node.getTrueStatement());

            if (!Boolean.FALSE.equals(condition)) {
                connect(data, trueBegin, ControlFlowEdgeType.ConditionTrue);
            }

            final ControlFlowNode trueEnd = node.getTrueStatement().acceptVisitor(this, trueBegin);
            final ControlFlowNode falseEnd;

            if (node.getFalseStatement().isNull()) {
                falseEnd = null;
            }
            else {
                final ControlFlowNode falseBegin = createStartNode(node.getFalseStatement());

                if (!Boolean.TRUE.equals(condition)) {
                    connect(data, falseBegin, ControlFlowEdgeType.ConditionFalse);
                }

                falseEnd = node.getFalseStatement().acceptVisitor(this, falseBegin);
            }

            final ControlFlowNode end = createEndNode(node);

            if (trueEnd != null) {
                connect(trueEnd, end);
            }

            if (falseEnd != null) {
                connect(falseEnd, end);
            }
            else if (!Boolean.TRUE.equals(condition)) {
                connect(data, end, ControlFlowEdgeType.ConditionFalse);
            }

            return end;
        }

        @Override
        public ControlFlowNode visitAssertStatement(final AssertStatement node, final ControlFlowNode data) {
            return createConnectedEndNode(node, data);
        }

        @Override
        public ControlFlowNode visitSwitchStatement(final SwitchStatement node, final ControlFlowNode data) {
            final ResolveResult constant = evaluateConstant(node.getExpression());

            SwitchSection defaultSection = null;
            SwitchSection sectionMatchedByConstant = null;

            for (final SwitchSection section : node.getSwitchSections()) {
                for (final CaseLabel label : section.getCaseLabels()) {
                    if (label.getExpression().isNull()) {
                        defaultSection = section;
                    }
                    else if (constant != null && constant.isCompileTimeConstant()) {
                        final ResolveResult labelConstant = evaluateConstant(label.getExpression());

                        if (areEqualConstants(constant, labelConstant)) {
                            sectionMatchedByConstant = section;
                        }
                    }
                }
            }

            if (constant != null && constant.isCompileTimeConstant() && sectionMatchedByConstant == null) {
                sectionMatchedByConstant = defaultSection;
            }

            final ControlFlowNode end = createEndNode(node, false);

            breakTargets.push(end);

            for (final SwitchSection section : node.getSwitchSections()) {
                assert section != null;

                if (constant == null || !constant.isCompileTimeConstant() || section == sectionMatchedByConstant) {
                    handleStatementList(section.getStatements(), data);
                }
                else {
                    //
                    // Section is unreachable; pass null to handleStatementList().
                    //
                    handleStatementList(section.getStatements(), null);
                }
            }

            breakTargets.pop();

            if (defaultSection == null || sectionMatchedByConstant == null) {
                connect(data, end);
            }

            nodes.add(end);

            return end;
        }

        @Override
        public ControlFlowNode visitWhileStatement(final WhileStatement node, final ControlFlowNode data) {
            final ControlFlowNode end = createEndNode(node, false);
            final ControlFlowNode conditionNode = createSpecialNode(node, ControlFlowNodeType.LoopCondition);

            breakTargets.push(end);
            continueTargets.push(conditionNode);

            connect(data, conditionNode);

            final Boolean condition = evaluateCondition(node.getCondition());
            final ControlFlowNode bodyStart = createStartNode(node.getEmbeddedStatement());

            if (!Boolean.FALSE.equals(condition)) {
                connect(conditionNode, bodyStart, ControlFlowEdgeType.ConditionTrue);
            }

            final ControlFlowNode bodyEnd = node.getEmbeddedStatement().acceptVisitor(this, bodyStart);

            connect(bodyEnd, conditionNode);

            if (!Boolean.TRUE.equals(condition)) {
                connect(conditionNode, end, ControlFlowEdgeType.ConditionFalse);
            }

            breakTargets.pop();
            continueTargets.pop();
            nodes.add(end);

            return end;
        }

        @Override
        public ControlFlowNode visitDoWhileStatement(final DoWhileStatement node, final ControlFlowNode data) {
            final ControlFlowNode end = createEndNode(node, false);
            final ControlFlowNode conditionNode = createSpecialNode(node, ControlFlowNodeType.LoopCondition, false);

            breakTargets.push(end);
            continueTargets.push(conditionNode);

            final ControlFlowNode bodyStart = createStartNode(node.getEmbeddedStatement());

            connect(data, bodyStart);

            final ControlFlowNode bodyEnd = node.getEmbeddedStatement().acceptVisitor(this, bodyStart);

            connect(bodyEnd, conditionNode);

            final Boolean condition = evaluateCondition(node.getCondition());

            if (!Boolean.FALSE.equals(condition)) {
                connect(conditionNode, bodyStart, ControlFlowEdgeType.ConditionTrue);
            }

            if (!Boolean.TRUE.equals(condition)) {
                connect(conditionNode, end, ControlFlowEdgeType.ConditionFalse);
            }

            breakTargets.pop();
            continueTargets.pop();
            nodes.add(conditionNode);
            nodes.add(end);

            return end;
        }

        @Override
        public ControlFlowNode visitForStatement(final ForStatement node, final ControlFlowNode data) {
            final ControlFlowNode newData = handleStatementList(node.getInitializers(), data);
            final ControlFlowNode end = createEndNode(node, false);
            final ControlFlowNode conditionNode = createSpecialNode(node, ControlFlowNodeType.LoopCondition);

            connect(newData, conditionNode);

            final int iteratorStartNodeId = nodes.size();

            final ControlFlowNode iteratorEnd = handleStatementList(node.getIterators(), null);
            final ControlFlowNode iteratorStart;

            if (iteratorEnd != null) {
                iteratorStart = nodes.get(iteratorStartNodeId);
            }
            else {
                iteratorStart = conditionNode;
            }

            breakTargets.push(end);
            continueTargets.push(iteratorStart);

            final ControlFlowNode bodyStart = createStartNode(node.getEmbeddedStatement());
            final ControlFlowNode bodyEnd = node.getEmbeddedStatement().acceptVisitor(this, bodyStart);

            if (bodyEnd != null) {
                connect(bodyEnd, iteratorStart);
            }

            breakTargets.pop();
            continueTargets.pop();

            final Boolean condition = node.getCondition().isNull() ? Boolean.TRUE
                                                                   : evaluateCondition(node.getCondition());

            if (!Boolean.FALSE.equals(condition)) {
                connect(conditionNode, bodyStart, ControlFlowEdgeType.ConditionTrue);
            }

            if (!Boolean.TRUE.equals(condition)) {
                connect(conditionNode, end, ControlFlowEdgeType.ConditionFalse);
            }

            nodes.add(end);

            return end;
        }

        final ControlFlowNode handleEmbeddedStatement(final Statement embeddedStatement, final ControlFlowNode source) {
            if (embeddedStatement == null || embeddedStatement.isNull()) {
                return source;
            }

            final ControlFlowNode bodyStart = createStartNode(embeddedStatement);

            if (source != null) {
                connect(source, bodyStart);
            }

            return embeddedStatement.acceptVisitor(this, bodyStart);
        }

        @Override
        public ControlFlowNode visitForEachStatement(final ForEachStatement node, final ControlFlowNode data) {
            final ControlFlowNode end = createEndNode(node, false);
            final ControlFlowNode conditionNode = createSpecialNode(node, ControlFlowNodeType.LoopCondition);

            connect(data, conditionNode);

            breakTargets.push(end);
            continueTargets.push(conditionNode);

            final ControlFlowNode bodyEnd = handleEmbeddedStatement(node.getEmbeddedStatement(), conditionNode);

            connect(bodyEnd, conditionNode);

            breakTargets.pop();
            continueTargets.pop();

            connect(conditionNode, end);
            nodes.add(end);

            return end;
        }

        @Override
        public ControlFlowNode visitGotoStatement(final GotoStatement node, final ControlFlowNode data) {
            gotoStatements.add(data);
            return createEndNode(node);
        }

        @Override
        public ControlFlowNode visitBreakStatement(final BreakStatement node, final ControlFlowNode data) {
            if (!StringUtilities.isNullOrEmpty(node.getLabel())) {
                gotoStatements.add(data);
                return createEndNode(node);
            }

            if (!breakTargets.isEmpty()) {
                connect(data, breakTargets.peek(), ControlFlowEdgeType.Jump);
            }

            return createEndNode(node);
        }

        @Override
        public ControlFlowNode visitContinueStatement(final ContinueStatement node, final ControlFlowNode data) {
            if (!StringUtilities.isNullOrEmpty(node.getLabel())) {
                gotoStatements.add(data);
                return createEndNode(node);
            }

            if (!continueTargets.isEmpty()) {
                connect(data, continueTargets.peek(), ControlFlowEdgeType.Jump);
            }

            return createEndNode(node);
        }

        @Override
        public ControlFlowNode visitReturnStatement(final ReturnStatement node, final ControlFlowNode data) {
            return createEndNode(node);
        }

        @Override
        public ControlFlowNode visitThrowStatement(final ThrowStatement node, final ControlFlowNode data) {
            return createEndNode(node);
        }

        @Override
        public ControlFlowNode visitTryCatchStatement(final TryCatchStatement node, final ControlFlowNode data) {
            final boolean hasFinally = !node.getFinallyBlock().isNull();
            final ControlFlowNode end = createEndNode(node, false);

            ControlFlowEdge edge = connect(handleEmbeddedStatement(node.getTryBlock(), data), end);

            if (hasFinally) {
                edge.AddJumpOutOfTryFinally(node);
            }

            for (final CatchClause cc : node.getCatchClauses()) {
                edge = connect(handleEmbeddedStatement(cc.getBody(), data), end);

                if (hasFinally) {
                    edge.AddJumpOutOfTryFinally(node);
                }
            }

            if (hasFinally) {
                handleEmbeddedStatement(node.getFinallyBlock(), data);
            }

            nodes.add(end);

            return end;
        }

        @Override
        public ControlFlowNode visitSynchronizedStatement(final SynchronizedStatement node, final ControlFlowNode data) {
            final ControlFlowNode bodyEnd = handleEmbeddedStatement(node.getEmbeddedStatement(), data);
            return createConnectedEndNode(node, bodyEnd);
        }
    }

    // </editor-fold>
}
