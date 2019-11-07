/*
 * DefiniteAssignmentAnalysis.java
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

package com.strobel.decompiler.languages.java.ast;

import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.languages.java.analysis.ControlFlowEdge;
import com.strobel.decompiler.languages.java.analysis.ControlFlowEdgeType;
import com.strobel.decompiler.languages.java.analysis.ControlFlowGraphBuilder;
import com.strobel.decompiler.languages.java.analysis.ControlFlowNode;
import com.strobel.decompiler.languages.java.analysis.ControlFlowNodeType;
import com.strobel.decompiler.semantics.ResolveResult;
import com.strobel.functions.Function;
import com.strobel.util.ContractUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.strobel.core.CollectionUtilities.contains;

public class DefiniteAssignmentAnalysis {
    private final DefiniteAssignmentVisitor visitor = new DefiniteAssignmentVisitor();
    private final ArrayList<DefiniteAssignmentNode> allNodes = new ArrayList<>();
    private final LinkedHashMap<Statement, DefiniteAssignmentNode> beginNodeMap = new LinkedHashMap<>();
    private final LinkedHashMap<Statement, DefiniteAssignmentNode> endNodeMap = new LinkedHashMap<>();
    private final LinkedHashMap<Statement, DefiniteAssignmentNode> conditionNodeMap = new LinkedHashMap<>();
    private final LinkedHashMap<ControlFlowEdge, DefiniteAssignmentStatus> edgeStatus = new LinkedHashMap<>();

    private final ArrayList<IdentifierExpression> unassignedVariableUses = new ArrayList<>();
    private final List<IdentifierExpression> unassignedVariableUsesView = Collections.unmodifiableList(unassignedVariableUses);
    private final ArrayDeque<DefiniteAssignmentNode> nodesWithModifiedInput = new ArrayDeque<>();

    private final Function<AstNode, ResolveResult> resolver;

    private String variableName;

    private int analyzedRangeStart;
    private int analyzedRangeEnd;

    public DefiniteAssignmentAnalysis(final DecompilerContext context, final Statement rootStatement) {
        this(rootStatement, new JavaResolver(context));
    }

    public DefiniteAssignmentAnalysis(final Statement rootStatement, final Function<AstNode, ResolveResult> resolver) {
        VerifyArgument.notNull(rootStatement, "rootStatement");
        VerifyArgument.notNull(resolver, "resolver");

        this.resolver = resolver;

        final DerivedControlFlowGraphBuilder builder = new DerivedControlFlowGraphBuilder();

        builder.setEvaluateOnlyPrimitiveConstants(true);

        for (final ControlFlowNode node : builder.buildControlFlowGraph(rootStatement, resolver)) {
            allNodes.add((DefiniteAssignmentNode) node);
        }

        for (int i = 0; i < allNodes.size(); i++) {
            final DefiniteAssignmentNode node = allNodes.get(i);

            node.setIndex(i);

            if (node.getType() == ControlFlowNodeType.StartNode ||
                node.getType() == ControlFlowNodeType.BetweenStatements) {

                //
                // Anonymous methods have separate control flow graphs, but we also need to analyze those.
                // Iterate backwards so that anonymous methods are inserted in the correct order.
                //
                for (AstNode child = node.getNextStatement().getLastChild(); child != null; child = child.getPreviousSibling()) {
                    insertAnonymousMethods(i + 1, child, builder);
                }
            }

            if (node.getType() == ControlFlowNodeType.StartNode ||
                node.getType() == ControlFlowNodeType.BetweenStatements) {

                beginNodeMap.put(node.getNextStatement(), node);
            }

            if (node.getType() == ControlFlowNodeType.BetweenStatements ||
                node.getType() == ControlFlowNodeType.EndNode) {

                endNodeMap.put(node.getPreviousStatement(), node);
            }

            if (node.getType() == ControlFlowNodeType.LoopCondition) {
                conditionNodeMap.put(node.getNextStatement(), node);
            }
        }

        this.analyzedRangeStart = 0;
        this.analyzedRangeEnd = allNodes.size() - 1;
    }

    private void insertAnonymousMethods(
        final int insertPosition,
        final AstNode node,
        final ControlFlowGraphBuilder builder) {

        //
        // Ignore any statements, as those have their own ControlFlowNode and get handled separately.
        //
        if (node instanceof Statement) {
            return;
        }

        if (node instanceof LambdaExpression){
            final LambdaExpression lambda = (LambdaExpression) node;

            if (lambda.getBody() instanceof Statement) {
                @SuppressWarnings("unchecked")
                final List<? extends DefiniteAssignmentNode> nodes = (List) builder.buildControlFlowGraph(
                    (Statement) lambda.getBody(),
                    resolver
                );

                allNodes.addAll(insertPosition, nodes);

                return;
            }
        }

        //
        // Descend into child expressions.  Iterate backwards so that anonymous methods
        // are inserted in the correct order.
        //
        for (AstNode child = node.getLastChild(); child != null; child = child.getPreviousSibling()) {
            insertAnonymousMethods(insertPosition, child, builder);
        }
    }
    public List<IdentifierExpression> getUnassignedVariableUses() {
        return unassignedVariableUsesView;
    }

    public void setAnalyzedRange(final Statement start, final Statement end) {
        setAnalyzedRange(start, end, true, true);
    }

    public void setAnalyzedRange(final Statement start, final Statement end, final boolean startInclusive, final boolean endInclusive) {
        final Map<Statement, DefiniteAssignmentNode> startMap = startInclusive ? beginNodeMap : endNodeMap;
        final Map<Statement, DefiniteAssignmentNode> endMap = endInclusive ? endNodeMap : beginNodeMap;

        assert startMap.containsKey(start) && endMap.containsKey(end);

        final int startIndex = startMap.get(start).getIndex();
        final int endIndex = endMap.get(end).getIndex();

        if (startIndex > endIndex) {
            throw new IllegalStateException("The start statement must lexically precede the end statement.");
        }

        this.analyzedRangeStart = startIndex;
        this.analyzedRangeEnd = endIndex;
    }

    public void analyze(final String variable) {
        analyze(variable, DefiniteAssignmentStatus.POTENTIALLY_ASSIGNED);
    }

    public void analyze(final String variable, final DefiniteAssignmentStatus initialStatus) {
        this.variableName = variable;

        try {
            unassignedVariableUses.clear();

            for (final DefiniteAssignmentNode node : allNodes) {
                node.setNodeStatus(DefiniteAssignmentStatus.CODE_UNREACHABLE);

                for (final ControlFlowEdge edge : node.getOutgoing()) {
                    edgeStatus.put(edge, DefiniteAssignmentStatus.CODE_UNREACHABLE);
                }
            }

            changeNodeStatus(allNodes.get(analyzedRangeStart), initialStatus);

            while (!nodesWithModifiedInput.isEmpty()) {
                final DefiniteAssignmentNode node = nodesWithModifiedInput.poll();

                DefiniteAssignmentStatus inputStatus = DefiniteAssignmentStatus.CODE_UNREACHABLE;

                for (final ControlFlowEdge edge : node.getIncoming()) {
                    inputStatus = mergeStatus(inputStatus, edgeStatus.get(edge));
                }

                changeNodeStatus(node, inputStatus);
            }
        }
        finally {
            this.variableName = null;
        }
    }

    public boolean isPotentiallyAssigned() {
        for (final DefiniteAssignmentNode node : allNodes) {
            final DefiniteAssignmentStatus status = node.getNodeStatus();

            if (status == null)
                return true;

            switch (status) {
                case POTENTIALLY_ASSIGNED:
                case DEFINITELY_ASSIGNED:
                case ASSIGNED_AFTER_TRUE_EXPRESSION:
                case ASSIGNED_AFTER_FALSE_EXPRESSION:
                    return true;
            }
        }

        return false;
    }

    public DefiniteAssignmentStatus getStatusBefore(final Statement statement) {
        return beginNodeMap.get(statement).getNodeStatus();
    }

    public DefiniteAssignmentStatus getStatusAfter(final Statement statement) {
        return endNodeMap.get(statement).getNodeStatus();
    }

    public DefiniteAssignmentStatus getBeforeLoopCondition(final Statement statement) {
        return conditionNodeMap.get(statement).getNodeStatus();
    }

    private DefiniteAssignmentStatus cleanSpecialValues(final DefiniteAssignmentStatus status) {
        if (status == null) {
            return null;
        }

        switch (status) {
            case ASSIGNED_AFTER_TRUE_EXPRESSION:
            case ASSIGNED_AFTER_FALSE_EXPRESSION:
                return DefiniteAssignmentStatus.POTENTIALLY_ASSIGNED;

            default:
                return status;
        }
    }

    private DefiniteAssignmentStatus mergeStatus(final DefiniteAssignmentStatus a, final DefiniteAssignmentStatus b) {
        if (a == b) {
            return a;
        }

        if (a == DefiniteAssignmentStatus.CODE_UNREACHABLE) {
            return b;
        }

        if (b == DefiniteAssignmentStatus.CODE_UNREACHABLE) {
            return a;
        }

        return DefiniteAssignmentStatus.POTENTIALLY_ASSIGNED;
    }

    private void changeNodeStatus(final DefiniteAssignmentNode node, final DefiniteAssignmentStatus inputStatus) {
        if (node.getNodeStatus() == inputStatus) {
            return;
        }

        node.setNodeStatus(inputStatus);

        DefiniteAssignmentStatus outputStatus;

        switch (node.getType()) {
            case StartNode:
            case BetweenStatements: {
                if (!(node.getNextStatement() instanceof IfElseStatement)) {
                    if (inputStatus == DefiniteAssignmentStatus.DEFINITELY_ASSIGNED) {
                        outputStatus = DefiniteAssignmentStatus.DEFINITELY_ASSIGNED;
                    }
                    else {
                        outputStatus = cleanSpecialValues(node.getNextStatement().acceptVisitor(visitor, inputStatus));
                    }
                    break;
                }

                //
                // Fall through to LoopCondition if next statement is If/Else...
                //
            }

            case LoopCondition: {
                if (node.getNextStatement() instanceof ForEachStatement) {
                    final ForEachStatement forEach = (ForEachStatement) node.getNextStatement();

                    outputStatus = cleanSpecialValues(forEach.getInExpression().acceptVisitor(visitor, inputStatus));

                    if (StringUtilities.equals(forEach.getVariableName(), variableName)) {
                        outputStatus = DefiniteAssignmentStatus.DEFINITELY_ASSIGNED;
                    }

                    break;
                }

                assert node.getNextStatement() instanceof IfElseStatement ||
                       node.getNextStatement() instanceof WhileStatement ||
                       node.getNextStatement() instanceof DoWhileStatement ||
                       node.getNextStatement() instanceof ForStatement;

                final Expression condition = node.getNextStatement().getChildByRole(Roles.CONDITION);

                if (condition.isNull()) {
                    outputStatus = inputStatus;
                }
                else {
                    outputStatus = condition.acceptVisitor(visitor, inputStatus);
                }

                for (final ControlFlowEdge edge : node.getOutgoing()) {
                    if (edge.getType() == ControlFlowEdgeType.ConditionTrue &&
                        outputStatus == DefiniteAssignmentStatus.ASSIGNED_AFTER_TRUE_EXPRESSION) {

                        changeEdgeStatus(edge, DefiniteAssignmentStatus.DEFINITELY_ASSIGNED);
                    }
                    else if (edge.getType() == ControlFlowEdgeType.ConditionFalse &&
                             outputStatus == DefiniteAssignmentStatus.ASSIGNED_AFTER_FALSE_EXPRESSION) {

                        changeEdgeStatus(edge, DefiniteAssignmentStatus.DEFINITELY_ASSIGNED);
                    }
                    else {
                        changeEdgeStatus(edge, cleanSpecialValues(outputStatus));
                    }
                }

                return;
            }

            case EndNode: {
                outputStatus = inputStatus;

                if (node.getPreviousStatement().getRole() == TryCatchStatement.FINALLY_BLOCK_ROLE &&
                    (outputStatus == DefiniteAssignmentStatus.DEFINITELY_ASSIGNED ||
                     outputStatus == DefiniteAssignmentStatus.POTENTIALLY_ASSIGNED)) {

                    final TryCatchStatement tryFinally = (TryCatchStatement) node.getPreviousStatement().getParent();

                    for (final DefiniteAssignmentNode n : allNodes) {
                        for (final ControlFlowEdge edge : n.getOutgoing()) {
                            if (edge.isLeavingTryFinally() && contains(edge.getTryFinallyStatements(), tryFinally)) {
                                final DefiniteAssignmentStatus s = edgeStatus.get(edge);

                                if (s == DefiniteAssignmentStatus.POTENTIALLY_ASSIGNED) {
                                    changeEdgeStatus(edge, outputStatus);
                                }
                            }
                        }
                    }
                }

                break;
            }

            default: {
                throw ContractUtils.unreachable();
            }
        }

        for (final ControlFlowEdge edge : node.getOutgoing()) {
            changeEdgeStatus(edge, outputStatus);
        }
    }

    private void changeEdgeStatus(final ControlFlowEdge edge, final DefiniteAssignmentStatus newStatus) {
        final DefiniteAssignmentStatus oldStatus = edgeStatus.get(edge);

        if (oldStatus == newStatus) {
            return;
        }

        //
        // Ensure that status cannot change after it is definitely assigned..
        //

        if (oldStatus == DefiniteAssignmentStatus.DEFINITELY_ASSIGNED) {
            return;
        }

        //
        // Ensure that status cannot change back to unreachable after it was once reachable.
        //

        if (newStatus == DefiniteAssignmentStatus.CODE_UNREACHABLE ||
            newStatus == DefiniteAssignmentStatus.ASSIGNED_AFTER_FALSE_EXPRESSION ||
            newStatus == DefiniteAssignmentStatus.ASSIGNED_AFTER_TRUE_EXPRESSION) {

            throw new IllegalStateException("Illegal edge output status:" + newStatus);
        }

        edgeStatus.put(edge, newStatus);

        final DefiniteAssignmentNode targetNode = (DefiniteAssignmentNode) edge.getTo();

        if (analyzedRangeStart <= targetNode.getIndex() && targetNode.getIndex() <= analyzedRangeEnd) {
            nodesWithModifiedInput.add(targetNode);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Constant Evaluation">

    protected ResolveResult evaluateConstant(final Expression e) {
        return resolver.apply(e);
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

    // <editor-fold defaultstate="collapsed" desc="Visitor">

    final class DefiniteAssignmentVisitor extends DepthFirstAstVisitor<DefiniteAssignmentStatus, DefiniteAssignmentStatus> {
        @Override
        protected DefiniteAssignmentStatus visitChildren(final AstNode node, final DefiniteAssignmentStatus data) {
            //
            // Special values are valid as output only.
            //
            assert data == cleanSpecialValues(data);

            DefiniteAssignmentStatus status = data;

            for (AstNode child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                assert !(child instanceof Statement);

                if (child instanceof TypeDeclaration) {
                    //
                    // Ignore the content of anonymous local types.
                    //
                    continue;
                }

                status = child.acceptVisitor(this, status);
                status = cleanSpecialValues(status);
            }

            return status;
        }

        @Override
        public DefiniteAssignmentStatus visitLabeledStatement(final LabeledStatement node, final DefiniteAssignmentStatus data) {
            return node.getStatement().acceptVisitor(this, data);
        }

        @Override
        public DefiniteAssignmentStatus visitBlockStatement(final BlockStatement node, final DefiniteAssignmentStatus data) {
            return data;
        }

        @Override
        public DefiniteAssignmentStatus visitTypeDeclaration(final TypeDeclaration node, final DefiniteAssignmentStatus data) {
            return data;
        }

        @Override
        public DefiniteAssignmentStatus visitLocalTypeDeclarationStatement(final LocalTypeDeclarationStatement node, final DefiniteAssignmentStatus data) {
            return data;
        }

        @Override
        public DefiniteAssignmentStatus visitVariableInitializer(final VariableInitializer node, final DefiniteAssignmentStatus data) {
            if (node.getInitializer().isNull()) {
                return data;
            }

            final DefiniteAssignmentStatus status = node.getInitializer().acceptVisitor(this, data);

            if (StringUtilities.equals(variableName, node.getName())) {
                return DefiniteAssignmentStatus.DEFINITELY_ASSIGNED;
            }

            return status;
        }

        @Override
        public DefiniteAssignmentStatus visitSwitchStatement(final SwitchStatement node, final DefiniteAssignmentStatus data) {
            return node.getExpression().acceptVisitor(this, data);
        }

        @Override
        public DefiniteAssignmentStatus visitDoWhileStatement(final DoWhileStatement node, final DefiniteAssignmentStatus data) {
            return data;
        }

        @Override
        public DefiniteAssignmentStatus visitWhileStatement(final WhileStatement node, final DefiniteAssignmentStatus data) {
            return data;
        }

        @Override
        public DefiniteAssignmentStatus visitForStatement(final ForStatement node, final DefiniteAssignmentStatus data) {
            return data;
        }

        @Override
        public DefiniteAssignmentStatus visitTryCatchStatement(final TryCatchStatement node, final DefiniteAssignmentStatus data) {
            return data;
        }

        @Override
        public DefiniteAssignmentStatus visitForEachStatement(final ForEachStatement node, final DefiniteAssignmentStatus data) {
            return data;
        }

        @Override
        public DefiniteAssignmentStatus visitSynchronizedStatement(final SynchronizedStatement node, final DefiniteAssignmentStatus data) {
            return node.getExpression().acceptVisitor(this, data);
        }

        @Override
        public DefiniteAssignmentStatus visitAssignmentExpression(final AssignmentExpression node, final DefiniteAssignmentStatus data) {
            if (node.getOperator() == AssignmentOperatorType.ASSIGN) {
                return handleAssignment(node.getLeft(), node.getRight(), data);
            }
            else {
                return visitChildren(node, data);
            }
        }

        @Override
        public DefiniteAssignmentStatus visitLambdaExpression(final LambdaExpression node, final DefiniteAssignmentStatus data) {
            if (node.getBody() instanceof Statement) {
                changeNodeStatus(beginNodeMap.get(node.getBody()), data);
            }
            else {
                node.getBody().acceptVisitor(this, data);
            }
            return data;
        }

        final DefiniteAssignmentStatus handleAssignment(final Expression left, final Expression right, final DefiniteAssignmentStatus initialStatus) {
            if (left instanceof IdentifierExpression) {
                final IdentifierExpression identifier = (IdentifierExpression) left;

                if (StringUtilities.equals(variableName, identifier.getIdentifier())) {
                    if (right != null) {
                        right.acceptVisitor(this, initialStatus);
                    }

                    return DefiniteAssignmentStatus.DEFINITELY_ASSIGNED;
                }
            }

            DefiniteAssignmentStatus status = left.acceptVisitor(this, initialStatus);

            if (right != null) {
                status = right.acceptVisitor(this, status);
            }

            return cleanSpecialValues(status);
        }

        @Override
        public DefiniteAssignmentStatus visitParenthesizedExpression(final ParenthesizedExpression node, final DefiniteAssignmentStatus data) {
            return node.getExpression().acceptVisitor(visitor, data);
        }

        @Override
        public DefiniteAssignmentStatus visitBinaryOperatorExpression(final BinaryOperatorExpression node, final DefiniteAssignmentStatus data) {
            final BinaryOperatorType operator = node.getOperator();

            if (operator == BinaryOperatorType.LOGICAL_AND) {
                //
                // Handle constant left side of && operator.
                //
                final Boolean condition = evaluateCondition(node.getLeft());

                if (Boolean.TRUE.equals(condition)) {
                    return node.getRight().acceptVisitor(this, data);
                }

                if (Boolean.FALSE.equals(condition)) {
                    return data;
                }

                final DefiniteAssignmentStatus afterLeft = node.getLeft().acceptVisitor(this, data);
                final DefiniteAssignmentStatus beforeRight;

                if (afterLeft == DefiniteAssignmentStatus.ASSIGNED_AFTER_TRUE_EXPRESSION) {
                    beforeRight = DefiniteAssignmentStatus.DEFINITELY_ASSIGNED;
                }
                else if (afterLeft == DefiniteAssignmentStatus.ASSIGNED_AFTER_FALSE_EXPRESSION) {
                    beforeRight = DefiniteAssignmentStatus.POTENTIALLY_ASSIGNED;
                }
                else {
                    beforeRight = afterLeft;
                }

                final DefiniteAssignmentStatus afterRight = node.getRight().acceptVisitor(this, beforeRight);

                if (afterLeft == DefiniteAssignmentStatus.DEFINITELY_ASSIGNED) {
                    return DefiniteAssignmentStatus.DEFINITELY_ASSIGNED;
                }

                if (afterRight == DefiniteAssignmentStatus.DEFINITELY_ASSIGNED &&
                    afterLeft == DefiniteAssignmentStatus.ASSIGNED_AFTER_FALSE_EXPRESSION) {

                    return DefiniteAssignmentStatus.DEFINITELY_ASSIGNED;
                }

                if (afterRight == DefiniteAssignmentStatus.DEFINITELY_ASSIGNED ||
                    afterRight == DefiniteAssignmentStatus.ASSIGNED_AFTER_TRUE_EXPRESSION) {

                    return DefiniteAssignmentStatus.ASSIGNED_AFTER_TRUE_EXPRESSION;
                }

                if (afterLeft == DefiniteAssignmentStatus.ASSIGNED_AFTER_FALSE_EXPRESSION &&
                    afterRight == DefiniteAssignmentStatus.ASSIGNED_AFTER_FALSE_EXPRESSION) {

                    return DefiniteAssignmentStatus.ASSIGNED_AFTER_FALSE_EXPRESSION;
                }

                if (afterLeft == DefiniteAssignmentStatus.DEFINITELY_NOT_ASSIGNED &&
                    afterRight == DefiniteAssignmentStatus.DEFINITELY_NOT_ASSIGNED) {

                    return DefiniteAssignmentStatus.DEFINITELY_NOT_ASSIGNED;
                }

                return DefiniteAssignmentStatus.POTENTIALLY_ASSIGNED;
            }

            if (operator == BinaryOperatorType.LOGICAL_OR) {
                //
                // Handle constant left side of && operator.
                //
                final Boolean condition = evaluateCondition(node.getLeft());

                if (Boolean.FALSE.equals(condition)) {
                    return node.getRight().acceptVisitor(this, data);
                }

                if (Boolean.TRUE.equals(condition)) {
                    return data;
                }

                final DefiniteAssignmentStatus afterLeft = node.getLeft().acceptVisitor(this, data);
                final DefiniteAssignmentStatus beforeRight;

                if (afterLeft == DefiniteAssignmentStatus.ASSIGNED_AFTER_TRUE_EXPRESSION) {
                    beforeRight = DefiniteAssignmentStatus.POTENTIALLY_ASSIGNED;
                }
                else if (afterLeft == DefiniteAssignmentStatus.ASSIGNED_AFTER_FALSE_EXPRESSION) {
                    beforeRight = DefiniteAssignmentStatus.DEFINITELY_ASSIGNED;
                }
                else {
                    beforeRight = afterLeft;
                }

                final DefiniteAssignmentStatus afterRight = node.getRight().acceptVisitor(this, beforeRight);

                if (afterLeft == DefiniteAssignmentStatus.DEFINITELY_ASSIGNED) {
                    return DefiniteAssignmentStatus.DEFINITELY_ASSIGNED;
                }

                if (afterRight == DefiniteAssignmentStatus.DEFINITELY_ASSIGNED &&
                    afterLeft == DefiniteAssignmentStatus.ASSIGNED_AFTER_TRUE_EXPRESSION) {

                    return DefiniteAssignmentStatus.DEFINITELY_ASSIGNED;
                }

                if (afterRight == DefiniteAssignmentStatus.DEFINITELY_ASSIGNED ||
                    afterRight == DefiniteAssignmentStatus.ASSIGNED_AFTER_FALSE_EXPRESSION) {

                    return DefiniteAssignmentStatus.ASSIGNED_AFTER_FALSE_EXPRESSION;
                }

                if (afterLeft == DefiniteAssignmentStatus.ASSIGNED_AFTER_TRUE_EXPRESSION &&
                    afterRight == DefiniteAssignmentStatus.ASSIGNED_AFTER_TRUE_EXPRESSION) {

                    return DefiniteAssignmentStatus.ASSIGNED_AFTER_TRUE_EXPRESSION;
                }

                if (afterLeft == DefiniteAssignmentStatus.DEFINITELY_NOT_ASSIGNED &&
                    afterRight == DefiniteAssignmentStatus.DEFINITELY_NOT_ASSIGNED) {

                    return DefiniteAssignmentStatus.DEFINITELY_NOT_ASSIGNED;
                }

                return DefiniteAssignmentStatus.POTENTIALLY_ASSIGNED;
            }

            return visitChildren(node, data);
        }

        @Override
        public DefiniteAssignmentStatus visitUnaryOperatorExpression(final UnaryOperatorExpression node, final DefiniteAssignmentStatus data) {
            if (node.getOperator() == UnaryOperatorType.NOT) {
                final DefiniteAssignmentStatus status = node.getExpression().acceptVisitor(this, data);

                if (status == DefiniteAssignmentStatus.ASSIGNED_AFTER_FALSE_EXPRESSION) {
                    return DefiniteAssignmentStatus.ASSIGNED_AFTER_TRUE_EXPRESSION;
                }
                else if (status == DefiniteAssignmentStatus.ASSIGNED_AFTER_TRUE_EXPRESSION) {
                    return DefiniteAssignmentStatus.ASSIGNED_AFTER_FALSE_EXPRESSION;
                }
                else {
                    return status;
                }
            }

            return visitChildren(node, data);
        }

        @Override
        public DefiniteAssignmentStatus visitConditionalExpression(final ConditionalExpression node, final DefiniteAssignmentStatus data) {
            final Boolean condition = evaluateCondition(node.getCondition());

            if (Boolean.TRUE.equals(condition)) {
                return node.getTrueExpression().acceptVisitor(this, data);
            }

            if (Boolean.FALSE.equals(condition)) {
                return node.getFalseExpression().acceptVisitor(this, data);
            }

            final DefiniteAssignmentStatus afterCondition = node.getCondition().acceptVisitor(this, data);
            final DefiniteAssignmentStatus beforeTrue;
            final DefiniteAssignmentStatus beforeFalse;

            if (afterCondition == DefiniteAssignmentStatus.ASSIGNED_AFTER_TRUE_EXPRESSION) {
                beforeTrue = DefiniteAssignmentStatus.DEFINITELY_ASSIGNED;
                beforeFalse = DefiniteAssignmentStatus.DEFINITELY_ASSIGNED;
            }
            else if (afterCondition == DefiniteAssignmentStatus.ASSIGNED_AFTER_FALSE_EXPRESSION) {
                beforeTrue = DefiniteAssignmentStatus.POTENTIALLY_ASSIGNED;
                beforeFalse = DefiniteAssignmentStatus.DEFINITELY_ASSIGNED;
            }
            else {
                beforeTrue = afterCondition;
                beforeFalse = afterCondition;
            }

            final DefiniteAssignmentStatus afterTrue = node.getTrueExpression().acceptVisitor(this, beforeTrue);
            final DefiniteAssignmentStatus afterFalse = node.getTrueExpression().acceptVisitor(this, beforeFalse);

            return mergeStatus(cleanSpecialValues(afterTrue), cleanSpecialValues(afterFalse));
        }

        @Override
        public DefiniteAssignmentStatus visitIdentifierExpression(final IdentifierExpression node, final DefiniteAssignmentStatus data) {
            if (data != DefiniteAssignmentStatus.DEFINITELY_ASSIGNED &&
                StringUtilities.equals(node.getIdentifier(), variableName) &&
                node.getTypeArguments().isEmpty()) {

                unassignedVariableUses.add(node);
            }

            return data;
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="DefiniteAssignmentNode Class">

    final class DefiniteAssignmentNode extends ControlFlowNode {
        private int _index;
        private DefiniteAssignmentStatus _nodeStatus;

        public DefiniteAssignmentNode(
            final Statement previousStatement,
            final Statement nextStatement,
            final ControlFlowNodeType type) {

            super(previousStatement, nextStatement, type);
        }

        public int getIndex() {
            return _index;
        }

        public void setIndex(final int index) {
            this._index = index;
        }

        public DefiniteAssignmentStatus getNodeStatus() {
            return _nodeStatus;
        }

        public void setNodeStatus(final DefiniteAssignmentStatus nodeStatus) {
            this._nodeStatus = nodeStatus;
        }

        @Override
        public String toString() {
            return "[" + _index + "] " + _nodeStatus;
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="DerivedControlFlowGraphBuilder Class">

    final class DerivedControlFlowGraphBuilder extends ControlFlowGraphBuilder {
        @Override
        protected ControlFlowNode createNode(
            final Statement previousStatement,
            final Statement nextStatement,
            final ControlFlowNodeType type) {

            return new DefiniteAssignmentNode(previousStatement, nextStatement, type);
        }
    }

    // </editor-fold>
}
