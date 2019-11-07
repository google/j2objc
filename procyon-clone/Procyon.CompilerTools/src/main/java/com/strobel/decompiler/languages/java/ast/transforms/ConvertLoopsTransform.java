/*
 * PatternStatementTransform.java
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

import com.strobel.assembler.metadata.BuiltinTypes;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.core.CollectionUtilities;
import com.strobel.core.Predicate;
import com.strobel.core.StringUtilities;
import com.strobel.core.StrongBox;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.ast.DefaultMap;
import com.strobel.decompiler.ast.Variable;
import com.strobel.decompiler.languages.java.analysis.ControlFlowEdge;
import com.strobel.decompiler.languages.java.analysis.ControlFlowEdgeType;
import com.strobel.decompiler.languages.java.analysis.ControlFlowGraphBuilder;
import com.strobel.decompiler.languages.java.analysis.ControlFlowNode;
import com.strobel.decompiler.languages.java.analysis.ControlFlowNodeType;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.decompiler.patterns.*;

import javax.lang.model.element.Modifier;
import java.util.*;

import static com.strobel.core.CollectionUtilities.*;
import static com.strobel.decompiler.languages.java.analysis.Correlator.areCorrelated;

public final class ConvertLoopsTransform extends ContextTrackingVisitor<AstNode> {
    public ConvertLoopsTransform(final DecompilerContext context) {
        super(context);
    }

    // <editor-fold defaultstate="collapsed" desc="Visitor Overrides">

    @Override
    protected AstNode visitChildren(final AstNode node, final Void data) {
        AstNode next;

        for (AstNode child = node.getFirstChild(); child != null; child = next) {
            next = child.getNextSibling();

            final AstNode childResult = child.acceptVisitor(this, data);

            if (childResult != null && childResult != child) {
                next = childResult;
            }
        }

        return node;
    }

    @Override
    public AstNode visitExpressionStatement(final ExpressionStatement node, final Void data) {
        final AstNode n = super.visitExpressionStatement(node, data);

        if (!context.getSettings().getDisableForEachTransforms() && n instanceof ExpressionStatement) {
            final AstNode result = transformForEach((ExpressionStatement) n);

            if (result != null) {
                return result.acceptVisitor(this, data);
            }
        }

        return n;
    }

    @Override
    public AstNode visitWhileStatement(final WhileStatement node, final Void data) {
//        super.visitWhileStatement(node, data);

        final ForStatement forLoop = transformFor(node);

        if (forLoop != null) {
            if (!context.getSettings().getDisableForEachTransforms()) {
                final AstNode forEachInArray = transformForEachInArray(forLoop);

                if (forEachInArray != null) {
                    return forEachInArray.acceptVisitor(this, data);
                }
            }
            return forLoop.acceptVisitor(this, data);
        }

        final DoWhileStatement doWhile = transformDoWhile(node);

        if (doWhile != null) {
            return doWhile.acceptVisitor(this, data);
        }

        return visitChildren(transformContinueOuter(node), data);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="For Loop Transform">

    @SuppressWarnings("ConstantConditions")
    public final ForStatement transformFor(final WhileStatement node) {
        final Expression condition = node.getCondition();

        if (condition == null || condition.isNull() || condition instanceof PrimitiveExpression) {
            return null;
        }

        if (!(node.getEmbeddedStatement() instanceof BlockStatement)) {
            return null;
        }

        final BlockStatement body = (BlockStatement) node.getEmbeddedStatement();
        final ControlFlowGraphBuilder graphBuilder = new ControlFlowGraphBuilder();
        final List<ControlFlowNode> nodes = graphBuilder.buildControlFlowGraph(node, new JavaResolver(context));

        if (nodes.size() < 2) {
            return null;
        }

        final ControlFlowNode conditionNode = firstOrDefault(
            nodes,
            new Predicate<ControlFlowNode>() {
                @Override
                public boolean test(final ControlFlowNode n) {
                    return n.getType() == ControlFlowNodeType.LoopCondition;
                }
            }
        );

        if (conditionNode == null) {
            return null;
        }

        final List<ControlFlowNode> bodyNodes = new ArrayList<>();

        for (final ControlFlowEdge edge : conditionNode.getIncoming()) {
            final ControlFlowNode from = edge.getFrom();
            final Statement statement = from.getPreviousStatement();

            if (statement != null && body.isAncestorOf(statement, node)) {
                bodyNodes.add(from);
            }
        }

        if (bodyNodes.size() != 1) {
            return null;
        }

        final Set<Statement> incoming = new LinkedHashSet<>();
        final Set<ControlFlowEdge> visited = new HashSet<>();
        final ArrayDeque<ControlFlowEdge> agenda = new ArrayDeque<>();

        agenda.addAll(conditionNode.getIncoming());
        visited.addAll(conditionNode.getIncoming());

        while (!agenda.isEmpty()) {
            final ControlFlowEdge edge = agenda.removeFirst();
            final ControlFlowNode from = edge.getFrom();

            if (from == null) {
                continue;
            }

            if (edge.getType() == ControlFlowEdgeType.Jump) {
                final Statement jump = from.getNextStatement();
                if (jump.getPreviousStatement() != null) {
                    incoming.add(jump.getPreviousStatement());
                }
                else {
                    incoming.add(jump);
                }
                continue;
            }

            final Statement previousStatement = from.getPreviousStatement();

            if (previousStatement == null) {
                continue;
            }

            if (from.getType() == ControlFlowNodeType.EndNode) {
                if (previousStatement instanceof TryCatchStatement) {
                    incoming.add(previousStatement);
                    continue;
                }

                if (previousStatement instanceof BlockStatement || hasNestedBlocks(previousStatement)) {
                    for (final ControlFlowEdge e : from.getIncoming()) {
                        if (visited.add(e)) {
                            agenda.addLast(e);
                        }
                    }
                }
                else {
                    incoming.add(previousStatement);
                }
            }
        }

        if (incoming.isEmpty()) {
            return null;
        }

        final Statement[] iteratorSites = incoming.toArray(new Statement[incoming.size()]);
        final List<Statement> iterators = new ArrayList<>();
        final Set<Statement> iteratorCopies = new HashSet<>();

        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        final Map<Statement, List<Statement>> iteratorCopyMap = new DefaultMap<>(CollectionUtilities.<Statement>listFactory());

    collectIterators:
        while (true) {
            final Statement s = iteratorSites[0];

            if (s != null && !s.isNull() && s.isEmbeddable() && isSimpleIterator(s)/* && areCorrelated(condition, s)*/) {
                for (int i = 1; i < iteratorSites.length; i++) {
                    final Statement o = iteratorSites[i];

                    if (o == null || !s.matches(o)) {
                        break collectIterators;
                    }
                }

                iterators.add(s);

                for (int i = 0; i < iteratorSites.length; i++) {
                    iteratorCopies.add(iteratorSites[i]);
                    iteratorCopyMap.get(s).add(iteratorSites[i]);
                    iteratorSites[i] = iteratorSites[i].getPreviousStatement();
                }
            }
            else {
                break;
            }
        }

        //
        // We built up our iterator candidate list from the tail end, so reverse it.
        //

        Collections.reverse(iterators);

        //
        // Remove all leading iterator candidates which do not actually correlate with our loop
        // condition.  Stop when we find one that does.
        //

        while (!iterators.isEmpty()) {
            final Statement iterator = first(iterators);

            if (areCorrelated(condition, iterator)) {
                break;
            }

            for (final Statement copy : iteratorCopyMap.get(iterator)) {
                iteratorCopies.remove(copy);
            }

            iterators.remove(0);
        }

        if (iterators.isEmpty()) {
            //
            // We don't want to create any 'for' loops without iterator statements.
            //
            return null;
        }

        //
        // Build up our initializer list.  Only consider preceding assignments which correlate with
        // variables in our loop condition and iterator statements.
        //

        final ForStatement forLoop = new ForStatement(node.getOffset());
        final Stack<Statement> initializers = new Stack<>();

        for (Statement s = node.getPreviousStatement(); s instanceof ExpressionStatement; s = s.getPreviousStatement()) {
            final Statement fs = s;
            final Expression e = ((ExpressionStatement) s).getExpression();
            final Expression left;

            final boolean canExtract =
                e instanceof AssignmentExpression &&
                (left = e.getChildByRole(AssignmentExpression.LEFT_ROLE)) instanceof IdentifierExpression &&
                (areCorrelated(condition, s) ||
                 any(
                     iterators,
                     new Predicate<Statement>() {
                         @Override
                         public boolean test(final Statement i) {
                             return (i instanceof ExpressionStatement &&
                                     areCorrelated(((ExpressionStatement) i).getExpression(), fs)) ||
                                    areCorrelated(left, i);
                         }
                     }
                 ));

            if (canExtract) {
                initializers.add(s);
            }
            else {
                break;
            }
        }

        if (initializers.isEmpty()) {
            //
            // Don't transform a 'while' loop into a 'for' loop with no initializers.
            //
            return null;
        }

        condition.remove();
        body.remove();

        forLoop.setCondition(condition);

        if (body instanceof BlockStatement) {
            for (final Statement copy : iteratorCopies) {
                copy.remove();
            }
            forLoop.setEmbeddedStatement(body);
        }

        forLoop.getIterators().addAll(iterators);

        while (!initializers.isEmpty()) {
            final Statement initializer = initializers.pop();
            initializer.remove();
            forLoop.getInitializers().add(initializer);
        }

        node.replaceWith(forLoop);

        final Statement firstInlinableInitializer = canInlineInitializerDeclarations(forLoop);

        if (firstInlinableInitializer != null) {
            final BlockStatement parent = (BlockStatement) forLoop.getParent();
            final VariableDeclarationStatement newDeclaration = new VariableDeclarationStatement();
            final List<Statement> forInitializers = new ArrayList<>(forLoop.getInitializers());
            final int firstInlinableInitializerIndex = forInitializers.indexOf(firstInlinableInitializer);

            forLoop.getInitializers().clear();
            forLoop.getInitializers().add(newDeclaration);

            for (int i = 0; i < forInitializers.size(); i++) {
                final Statement initializer = forInitializers.get(i);

                if (i < firstInlinableInitializerIndex) {
                    parent.insertChildBefore(forLoop, initializer, BlockStatement.STATEMENT_ROLE);
                    continue;
                }

                final AssignmentExpression assignment = (AssignmentExpression) ((ExpressionStatement) initializer).getExpression();
                final IdentifierExpression variable = (IdentifierExpression) assignment.getLeft();
                final String variableName = variable.getIdentifier();
                final VariableDeclarationStatement declaration = findVariableDeclaration(forLoop, variableName);
                final Expression initValue = assignment.getRight();

                initValue.remove();
                newDeclaration.getVariables().add(new VariableInitializer(variableName, initValue));

                final AstType newDeclarationType = newDeclaration.getType();

                if (newDeclarationType == null || newDeclarationType.isNull()) {
                    newDeclaration.setType(declaration.getType().clone());
                }
            }
        }

        return forLoop;
    }

    private static boolean hasNestedBlocks(final AstNode node) {
        return AstNode.isLoop(node) ||
               node instanceof TryCatchStatement ||
               node instanceof CatchClause ||
               node instanceof LabeledStatement ||
               node instanceof SynchronizedStatement ||
               node instanceof IfElseStatement ||
               node instanceof SwitchSection;
    }

    private static boolean isSimpleIterator(final Statement statement) {
        if (!(statement instanceof ExpressionStatement)) {
            return false;
        }

        final Expression e = ((ExpressionStatement) statement).getExpression();

        if (e instanceof AssignmentExpression) {
            return true;
        }

        if (e instanceof UnaryOperatorExpression) {
            switch (((UnaryOperatorExpression) e).getOperator()) {
                case INCREMENT:
                case DECREMENT:
                case POST_INCREMENT:
                case POST_DECREMENT:
                    return true;

                default:
                    return false;
            }
        }

        return false;
    }

    private Statement canInlineInitializerDeclarations(final ForStatement forLoop) {
        TypeReference variableType = null;

        final BlockStatement tempOuter = new BlockStatement();
        final BlockStatement temp = new BlockStatement();
        final Statement[] initializers = forLoop.getInitializers().toArray(new Statement[forLoop.getInitializers().size()]);
        final Set<String> variableNames = new HashSet<>();

        Statement firstInlinableInitializer = null;

        forLoop.getParent().insertChildBefore(forLoop, tempOuter, BlockStatement.STATEMENT_ROLE);
        forLoop.remove();

        for (final Statement initializer : initializers) {
            initializer.remove();
            temp.getStatements().add(initializer);
        }

        temp.getStatements().add(forLoop);
        tempOuter.getStatements().add(temp);

        try {
            for (final Statement initializer : initializers) {
                final AssignmentExpression assignment = (AssignmentExpression) ((ExpressionStatement) initializer).getExpression();
                final IdentifierExpression variable = (IdentifierExpression) assignment.getLeft();
                final String variableName = variable.getIdentifier();
                final VariableDeclarationStatement declaration = findVariableDeclaration(forLoop, variableName);

                if (declaration == null) {
                    firstInlinableInitializer = null;
                    continue;
                }

                final Variable underlyingVariable = declaration.getUserData(Keys.VARIABLE);

                if (underlyingVariable == null || underlyingVariable.isParameter()) {
                    firstInlinableInitializer = null;
                    continue;
                }

                if (!variableNames.add(underlyingVariable.getName())) {
                    firstInlinableInitializer = null;
                    continue;
                }

                if (variableType == null) {
                    variableType = underlyingVariable.getType();
                }
                else if (!variableType.equals(underlyingVariable.getType())) {
                    variableType = underlyingVariable.getType();
                    firstInlinableInitializer = null;
                }

                if (!(declaration.getParent() instanceof BlockStatement)) {
                    firstInlinableInitializer = null;
                    continue;
                }

                final Statement declarationPoint = canMoveVariableDeclarationIntoStatement(context, declaration, forLoop);

                if (declarationPoint != tempOuter) {
                    variableType = null;
                    firstInlinableInitializer = null;
                }
                else if (firstInlinableInitializer == null) {
                    firstInlinableInitializer = initializer;
                }
            }

            return firstInlinableInitializer;
        }
        finally {
            forLoop.remove();
            tempOuter.replaceWith(forLoop);

            for (final Statement initializer : initializers) {
                initializer.remove();
                forLoop.getInitializers().add(initializer);
            }
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="For Each Loop Transform (Arrays)">

    private final static ExpressionStatement ARRAY_INIT_PATTERN;
    private final static ForStatement FOR_ARRAY_PATTERN_1;
    private final static ForStatement FOR_ARRAY_PATTERN_2;
    private final static ForStatement FOR_ARRAY_PATTERN_3;

    static {
        ARRAY_INIT_PATTERN = new ExpressionStatement(
            new AssignmentExpression(
                new NamedNode("array", new IdentifierExpression(Expression.MYSTERY_OFFSET, Pattern.ANY_STRING)).toExpression(),
                new AnyNode("initializer").toExpression()
            )
        );

        final ForStatement forArrayPattern1 = new ForStatement(Expression.MYSTERY_OFFSET);
        final VariableDeclarationStatement declaration1 = new VariableDeclarationStatement();
        final SimpleType variableType1 = new SimpleType("int");

        variableType1.putUserData(Keys.TYPE_REFERENCE, BuiltinTypes.Integer);

        declaration1.setType(variableType1);

        declaration1.getVariables().add(
            new VariableInitializer(
                Pattern.ANY_STRING,
                new NamedNode("array", new IdentifierExpression(Expression.MYSTERY_OFFSET, Pattern.ANY_STRING)).toExpression().member("length")
            )
        );

        declaration1.getVariables().add(
            new VariableInitializer(
                Pattern.ANY_STRING,
                new PrimitiveExpression(Expression.MYSTERY_OFFSET, 0)
            )
        );

        forArrayPattern1.getInitializers().add(
            new NamedNode("declaration", declaration1).toStatement()
        );

        forArrayPattern1.setCondition(
            new BinaryOperatorExpression(
                new NamedNode("index", new IdentifierExpression(Expression.MYSTERY_OFFSET, Pattern.ANY_STRING)).toExpression(),
                BinaryOperatorType.LESS_THAN,
                new NamedNode("length", new IdentifierExpression(Expression.MYSTERY_OFFSET, Pattern.ANY_STRING)).toExpression()
            )
        );

        forArrayPattern1.getIterators().add(
            new ExpressionStatement(
                new UnaryOperatorExpression(
                    UnaryOperatorType.INCREMENT,
                    new BackReference("index").toExpression()
                )
            )
        );

        final BlockStatement embeddedStatement1 = new BlockStatement();

        embeddedStatement1.add(
            new ExpressionStatement(
                new AssignmentChain(
                    new NamedNode("item", new IdentifierExpression(Expression.MYSTERY_OFFSET, Pattern.ANY_STRING)).toExpression(),
                    new IndexerExpression(
                        Expression.MYSTERY_OFFSET,
                        new BackReference("array").toExpression(),
                        new BackReference("index").toExpression()
                    )
                ).toExpression()
            )
        );

        embeddedStatement1.add(
            new Repeat(
                new AnyNode("statement")
            ).toStatement()
        );

        forArrayPattern1.setEmbeddedStatement(embeddedStatement1);

        FOR_ARRAY_PATTERN_1 = forArrayPattern1;

        final ForStatement forArrayPattern2 = new ForStatement(Expression.MYSTERY_OFFSET);
        final VariableDeclarationStatement declaration2 = new VariableDeclarationStatement();
        final SimpleType variableType2 = new SimpleType("int");

        variableType2.putUserData(Keys.TYPE_REFERENCE, BuiltinTypes.Integer);

        declaration2.setType(variableType2);

        declaration2.getVariables().add(
            new VariableInitializer(
                Pattern.ANY_STRING,
                new PrimitiveExpression(Expression.MYSTERY_OFFSET, 0)
            )
        );

        forArrayPattern2.getInitializers().add(
            new NamedNode("declaration", declaration2).toStatement()
        );

        forArrayPattern2.setCondition(
            new BinaryOperatorExpression(
                new NamedNode("index", new IdentifierExpression(Expression.MYSTERY_OFFSET, Pattern.ANY_STRING)).toExpression(),
                BinaryOperatorType.LESS_THAN,
                new NamedNode("length", new IdentifierExpression(Expression.MYSTERY_OFFSET, Pattern.ANY_STRING)).toExpression()
            )
        );

        forArrayPattern2.getIterators().add(
            new ExpressionStatement(
                new UnaryOperatorExpression(
                    UnaryOperatorType.INCREMENT,
                    new BackReference("index").toExpression()
                )
            )
        );

        final BlockStatement embeddedStatement2 = new BlockStatement();

        embeddedStatement2.add(
            new ExpressionStatement(
                new AssignmentChain(
                    new NamedNode("item", new IdentifierExpression(Expression.MYSTERY_OFFSET, Pattern.ANY_STRING)).toExpression(),
                    new IndexerExpression(
                        Expression.MYSTERY_OFFSET,
                        new NamedNode("array", new IdentifierExpression(Expression.MYSTERY_OFFSET, Pattern.ANY_STRING)).toExpression(),
                        new BackReference("index").toExpression()
                    )
                ).toExpression()
            )
        );

        embeddedStatement2.add(
            new Repeat(
                new AnyNode("statement")
            ).toStatement()
        );

        forArrayPattern2.setEmbeddedStatement(embeddedStatement2);

        FOR_ARRAY_PATTERN_2 = forArrayPattern2;

        final ForStatement altForArrayPattern = new ForStatement(Expression.MYSTERY_OFFSET);

        altForArrayPattern.getInitializers().add(
            new ExpressionStatement(
                new AssignmentExpression(
                    new NamedNode("length", new IdentifierExpression(Expression.MYSTERY_OFFSET, Pattern.ANY_STRING)).toExpression(),
                    AssignmentOperatorType.ASSIGN,
                    new NamedNode("array", new IdentifierExpression(Expression.MYSTERY_OFFSET, Pattern.ANY_STRING)).toExpression().member("length")
                )
            )
        );

        altForArrayPattern.getInitializers().add(
            new ExpressionStatement(
                new AssignmentExpression(
                    new NamedNode("index", new IdentifierExpression(Expression.MYSTERY_OFFSET, Pattern.ANY_STRING)).toExpression(),
                    AssignmentOperatorType.ASSIGN,
                    new PrimitiveExpression(Expression.MYSTERY_OFFSET, 0)
                )
            )
        );

        altForArrayPattern.setCondition(
            new BinaryOperatorExpression(
                new BackReference("index").toExpression(),
                BinaryOperatorType.LESS_THAN,
                new BackReference("length").toExpression()
            )
        );

        altForArrayPattern.getIterators().add(
            new ExpressionStatement(
                new UnaryOperatorExpression(
                    UnaryOperatorType.INCREMENT,
                    new BackReference("index").toExpression()
                )
            )
        );

        final BlockStatement altEmbeddedStatement = new BlockStatement();

        altEmbeddedStatement.add(
            new ExpressionStatement(
                new AssignmentChain(
                    new NamedNode("item", new IdentifierExpression(Expression.MYSTERY_OFFSET, Pattern.ANY_STRING)).toExpression(),
                    new IndexerExpression(
                        Expression.MYSTERY_OFFSET,
                        new BackReference("array").toExpression(),
                        new BackReference("index").toExpression()
                    )
                ).toExpression()
            )
        );

        altEmbeddedStatement.add(
            new Repeat(
                new AnyNode("statement")
            ).toStatement()
        );

        altForArrayPattern.setEmbeddedStatement(altEmbeddedStatement);

        FOR_ARRAY_PATTERN_3 = altForArrayPattern;
    }

    public final ForEachStatement transformForEachInArray(final ForStatement loop) {
        Match m = FOR_ARRAY_PATTERN_1.match(loop);

        if (!m.success()) {
            m = FOR_ARRAY_PATTERN_2.match(loop);

            if (!m.success()) {
                m = FOR_ARRAY_PATTERN_3.match(loop);

                if (!m.success()) {
                    return null;
                }
            }
        }

        final IdentifierExpression array = first(m.<IdentifierExpression>get("array"));
        final IdentifierExpression item = last(m.<IdentifierExpression>get("item"));
        final IdentifierExpression index = first(m.<IdentifierExpression>get("index"));

        //
        // Find the declaration of the item variable.  Because we look only outside the loop,
        // we won't make the mistake of moving a captured variable across the loop boundary.
        //

        final VariableDeclarationStatement itemDeclaration = findVariableDeclaration(loop, item.getIdentifier());

        if (itemDeclaration == null || !(itemDeclaration.getParent() instanceof BlockStatement)) {
            return null;
        }

        //
        // Now verify that we can move the variable declaration in front of the loop.
        //

        final Statement declarationPoint = canMoveVariableDeclarationIntoStatement(context, itemDeclaration, loop);

        //
        // We ignore the return value because we don't care whether we can move the variable into the loop
        // (that is possible only with non-captured variables).  We just care that we can move it in front
        // of the loop.
        //

        if (declarationPoint != loop) {
            return null;
        }

        final BlockStatement loopBody = (BlockStatement) loop.getEmbeddedStatement();
        final Statement secondStatement = getOrDefault(loopBody.getStatements(), 1);

        if (secondStatement != null && !secondStatement.isNull()) {
            final DefiniteAssignmentAnalysis analysis = new DefiniteAssignmentAnalysis(context, loopBody);

            analysis.setAnalyzedRange(secondStatement, loopBody);
            analysis.analyze(array.getIdentifier(), DefiniteAssignmentStatus.DEFINITELY_NOT_ASSIGNED);

            if (analysis.getStatusAfter(loopBody) != DefiniteAssignmentStatus.DEFINITELY_NOT_ASSIGNED) {
                //
                // We can't transform into a for-each loop because the array variable is reassigned.
                //
                return null;
            }

            analysis.analyze(index.getIdentifier(), DefiniteAssignmentStatus.DEFINITELY_NOT_ASSIGNED);

            if (analysis.getStatusAfter(loopBody) != DefiniteAssignmentStatus.DEFINITELY_NOT_ASSIGNED) {
                //
                // We can't eliminate the index variable because it's reassigned in the loop.
                //
                return null;
            }

            if (!analysis.getUnassignedVariableUses().isEmpty()) {
                //
                // We can't eliminate the index variable because it's used in the loop.
                //
                return null;
            }
        }

        final ForEachStatement forEach = new ForEachStatement(loop.getOffset());

        forEach.setVariableType(itemDeclaration.getType().clone());
        forEach.setVariableName(item.getIdentifier());

        forEach.putUserData(
            Keys.VARIABLE,
            itemDeclaration.getVariables().firstOrNullObject().getUserData(Keys.VARIABLE)
        );

        final BlockStatement body = new BlockStatement();
        final BlockStatement parent = (BlockStatement) loop.getParent();

        forEach.setEmbeddedStatement(body);
        parent.getStatements().insertBefore(loop, forEach);

        loop.remove();
        body.add(loop);
        loop.remove();
        body.add(loop);

        //
        // Now create the correct body for the foreach statement.
        //

        array.remove();

        forEach.setInExpression(array);

        final AstNodeCollection<Statement> bodyStatements = body.getStatements();

        bodyStatements.clear();

        final AstNode itemParent = item.getParent();

        if (itemParent.getParent() instanceof AssignmentExpression &&
            ((AssignmentExpression) itemParent.getParent()).getRight() == itemParent) {

            final Statement itemStatement = firstOrDefault(itemParent.getParent().getAncestors(Statement.class));

            item.remove();
            itemParent.replaceWith(item);

            if (itemStatement != null) {
                itemStatement.remove();
                bodyStatements.add(itemStatement);
            }
        }

        for (final Statement statement : m.<Statement>get("statement")) {
            statement.remove();
            bodyStatements.add(statement);
        }

        Statement previous = forEach.getPreviousStatement();

        while (previous instanceof LabelStatement) {
            previous = previous.getPreviousStatement();
        }

        if (previous != null) {
            final Match m2 = ARRAY_INIT_PATTERN.match(previous);

            if (m2.success()) {
                final Expression initializer = m2.<Expression>get("initializer").iterator().next();
                final IdentifierExpression array2 = m2.<IdentifierExpression>get("array").iterator().next();

                if (StringUtilities.equals(array2.getIdentifier(), array.getIdentifier())) {
                    final BlockStatement tempOuter = new BlockStatement();
                    final BlockStatement temp = new BlockStatement();

                    boolean restorePrevious = true;

                    parent.insertChildBefore(forEach, tempOuter, BlockStatement.STATEMENT_ROLE);
                    previous.remove();
                    forEach.remove();
                    temp.add(previous);
                    temp.add(forEach);
                    tempOuter.add(temp);

                    try {
                        final VariableDeclarationStatement arrayDeclaration = findVariableDeclaration(forEach, array.getIdentifier());

                        if (arrayDeclaration != null && arrayDeclaration.getParent() instanceof BlockStatement) {
                            final Statement arrayDeclarationPoint = canMoveVariableDeclarationIntoStatement(context, arrayDeclaration, forEach);

                            if (arrayDeclarationPoint == tempOuter) {
                                initializer.remove();
                                array.replaceWith(initializer);
                                restorePrevious = false;
                            }
                        }
                    }
                    finally {
                        previous.remove();
                        forEach.remove();

                        if (restorePrevious) {
                            parent.insertChildBefore(tempOuter, previous, BlockStatement.STATEMENT_ROLE);
                        }

                        parent.insertChildBefore(tempOuter, forEach, BlockStatement.STATEMENT_ROLE);
                        tempOuter.remove();
                    }
                }
            }
        }

        if (body.getStatements().isEmpty()) {
            forEach.addVariableModifier(Modifier.FINAL);
        }
        else {
            final DefiniteAssignmentAnalysis analysis = new DefiniteAssignmentAnalysis(context, body);
            final Statement firstStatement = firstOrDefault(body.getStatements());
            final Statement lastStatement = lastOrDefault(body.getStatements());

            analysis.setAnalyzedRange(firstStatement, lastStatement);
            analysis.analyze(item.getIdentifier(), DefiniteAssignmentStatus.DEFINITELY_NOT_ASSIGNED);

            if (!analysis.isPotentiallyAssigned()) {
                forEach.addVariableModifier(Modifier.FINAL);
            }
        }

        return forEach;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="For Each Loop Transform (Iterables)">

    private final static ExpressionStatement GET_ITERATOR_PATTERN;
    private final static WhileStatement FOR_EACH_PATTERN;

    static {
        GET_ITERATOR_PATTERN = new ExpressionStatement(
            new AssignmentExpression(
                new NamedNode("left", new AnyNode()).toExpression(),
                new AnyNode("collection").toExpression().invoke("iterator")
            )
        );

        final WhileStatement forEachPattern = new WhileStatement(Expression.MYSTERY_OFFSET);

        forEachPattern.setCondition(
            new InvocationExpression(
                Expression.MYSTERY_OFFSET,
                new MemberReferenceExpression(
                    Expression.MYSTERY_OFFSET,
                    new NamedNode("iterator", new IdentifierExpression(Expression.MYSTERY_OFFSET, Pattern.ANY_STRING)).toExpression(),
                    "hasNext"
                )
            )
        );

        final BlockStatement embeddedStatement = new BlockStatement();

        embeddedStatement.add(
            new NamedNode(
                "next",
                new ExpressionStatement(
                    new AssignmentChain(
                        new NamedNode("item", new IdentifierExpression(Expression.MYSTERY_OFFSET, Pattern.ANY_STRING)),
                        new Choice(
                            new InvocationExpression(
                                Expression.MYSTERY_OFFSET,
                                new MemberReferenceExpression(
                                    Expression.MYSTERY_OFFSET,
                                    new BackReference("iterator").toExpression(),
                                    "next"
                                )
                            ),
                            new CastExpression(
                                new AnyNode("castType").toType(),
                                new InvocationExpression(
                                    Expression.MYSTERY_OFFSET,
                                    new MemberReferenceExpression(
                                        Expression.MYSTERY_OFFSET,
                                        new BackReference("iterator").toExpression(),
                                        "next"
                                    )
                                )
                            )
                        )
                    ).toExpression()
//                    new AssignmentExpression(
//                        new NamedNode("item", new IdentifierExpression( Expression.MYSTERY_OFFSET, Pattern.ANY_STRING)).toExpression(),
//                        AssignmentOperatorType.ASSIGN,
//                        new Choice(
//                            new InvocationExpression(
//                                Expression.MYSTERY_OFFSET,
//                                new MemberReferenceExpression(
//                                    Expression.MYSTERY_OFFSET,
//                                    new BackReference("iterator").toExpression(),
//                                    "next"
//                                )
//                            ),
//                            new CastExpression(
//                                new AnyNode("castType").toType(),
//                                new InvocationExpression(
//                                    Expression.MYSTERY_OFFSET,
//                                    new MemberReferenceExpression(
//                                        Expression.MYSTERY_OFFSET,
//                                        new BackReference("iterator").toExpression(),
//                                        "next"
//                                    )
//                                )
//                            )
//                        ).toExpression()
//                    )
                )
            ).toStatement()
        );

        embeddedStatement.add(
            new Repeat(
                new AnyNode("statement")
            ).toStatement()
        );

        forEachPattern.setEmbeddedStatement(embeddedStatement);

        FOR_EACH_PATTERN = forEachPattern;
    }

    public final ForEachStatement transformForEach(final ExpressionStatement node) {
        final Match m1 = GET_ITERATOR_PATTERN.match(node);

        if (!m1.success()) {
            return null;
        }

        AstNode next = node.getNextSibling();

        while (next instanceof LabelStatement) {
            next = next.getNextSibling();
        }

        final Match m2 = FOR_EACH_PATTERN.match(next);

        if (!m2.success()) {
            return null;
        }

        final IdentifierExpression iterator = m2.<IdentifierExpression>get("iterator").iterator().next();
        final IdentifierExpression item = lastOrDefault(m2.<IdentifierExpression>get("item"));
        final WhileStatement loop = (WhileStatement) next;

        //
        // Ensure that the GET_ITERATOR_PATTERN and FOR_EACH_PATTERN reference the same iterator variable.
        //

        if (!iterator.matches(m1.get("left").iterator().next())) {
            return null;
        }

        final VariableDeclarationStatement iteratorDeclaration = findVariableDeclaration(loop, iterator.getIdentifier());

        if (iteratorDeclaration == null || !(iteratorDeclaration.getParent() instanceof BlockStatement)) {
            return null;
        }

        //
        // Find the declaration of the item variable.  Because we look only outside the loop,
        // we won't make the mistake of moving a captured variable across the loop boundary.
        //

        final VariableDeclarationStatement itemDeclaration = findVariableDeclaration(loop, item.getIdentifier());

        if (itemDeclaration == null || !(itemDeclaration.getParent() instanceof BlockStatement)) {
            return null;
        }

        //
        // Now verify that we can move the variable declaration in front of the loop.
        //

        Statement declarationPoint = canMoveVariableDeclarationIntoStatement(context, itemDeclaration, loop);

        //
        // We ignore the return value because we don't care whether we can move the variable into the loop
        // (that is possible only with non-captured variables).  We just care that we can move it in front
        // of the loop.
        //

        if (declarationPoint != loop) {
            return null;
        }

        final BlockStatement loopBody = (BlockStatement) loop.getEmbeddedStatement();
        final Statement secondStatement = getOrDefault(loopBody.getStatements(), 1);

        if (secondStatement != null && !secondStatement.isNull()) {
            final DefiniteAssignmentAnalysis analysis = new DefiniteAssignmentAnalysis(context, loopBody);

            analysis.setAnalyzedRange(secondStatement, loopBody);
            analysis.analyze(iterator.getIdentifier(), DefiniteAssignmentStatus.DEFINITELY_NOT_ASSIGNED);

            if (!analysis.getUnassignedVariableUses().isEmpty()) {
                //
                // We can't eliminate the iterator variable because it's used in the loop.
                //
                return null;
            }
        }

        final ForEachStatement forEach = new ForEachStatement(node.getOffset());

        forEach.setVariableType(itemDeclaration.getType().clone());
        forEach.setVariableName(item.getIdentifier());

        forEach.putUserData(
            Keys.VARIABLE,
            itemDeclaration.getVariables().firstOrNullObject().getUserData(Keys.VARIABLE)
        );

        final BlockStatement body = new BlockStatement();

        forEach.setEmbeddedStatement(body);
        ((BlockStatement) node.getParent()).getStatements().insertBefore(loop, forEach);

        node.remove();
        body.add(node);
        loop.remove();
        body.add(loop);

        //
        // Now that we moved the whole while statement into the foreach loop, verify that we can
        // move the iterator into the foreach loop.
        //

        declarationPoint = canMoveVariableDeclarationIntoStatement(context, iteratorDeclaration, forEach);

        if (declarationPoint != forEach) {
            //
            // We can't move the iterator variable after all; undo our changes.
            //
            node.remove();
            ((BlockStatement) forEach.getParent()).getStatements().insertBefore(forEach, node);
            forEach.replaceWith(loop);
            return null;
        }

        //
        // Now create the correct body for the foreach statement.
        //

        final Expression collection = m1.<Expression>get("collection").iterator().next();

        collection.remove();

        if (collection instanceof SuperReferenceExpression) {
            final ThisReferenceExpression self = new ThisReferenceExpression(collection.getOffset());
            self.putUserData(Keys.TYPE_REFERENCE, collection.getUserData(Keys.TYPE_REFERENCE));
            self.putUserData(Keys.VARIABLE, collection.getUserData(Keys.VARIABLE));
            forEach.setInExpression(self);
        }
        else {
            forEach.setInExpression(collection);
        }

        final AstNodeCollection<Statement> bodyStatements = body.getStatements();

        bodyStatements.clear();

        final AstNode itemParent = item.getParent();

        if (itemParent.getParent() instanceof AssignmentExpression &&
            ((AssignmentExpression) itemParent.getParent()).getRight() == itemParent) {

            final Statement itemStatement = firstOrDefault(itemParent.getParent().getAncestors(Statement.class));

            item.remove();
            itemParent.replaceWith(item);

            if (itemStatement != null) {
                itemStatement.remove();
                bodyStatements.add(itemStatement);
            }
        }

        for (final Statement statement : m2.<Statement>get("statement")) {
            statement.remove();
            bodyStatements.add(statement);
        }

//        iteratorDeclaration.remove();

        final Statement firstStatement = firstOrDefault(body.getStatements());
        final Statement lastStatement = lastOrDefault(body.getStatements());

        if (firstStatement != null && lastStatement != null) {
            final DefiniteAssignmentAnalysis analysis = new DefiniteAssignmentAnalysis(context, body);

            analysis.setAnalyzedRange(firstStatement, lastStatement);
            analysis.analyze(item.getIdentifier(), DefiniteAssignmentStatus.DEFINITELY_NOT_ASSIGNED);

            if (!analysis.isPotentiallyAssigned()) {
                forEach.addVariableModifier(Modifier.FINAL);
            }
        }

        return forEach;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Do While Loop Transform">

    private final static WhileStatement DO_WHILE_PATTERN;

    static {
        final WhileStatement doWhile = new WhileStatement(Expression.MYSTERY_OFFSET);

        doWhile.setCondition(new PrimitiveExpression(Expression.MYSTERY_OFFSET, true));

        doWhile.setEmbeddedStatement(
            new Choice(
                new BlockStatement(
                    new Repeat(new AnyNode("statement")).toStatement(),
                    new IfElseStatement(
                        Expression.MYSTERY_OFFSET,
                        new AnyNode("breakCondition").toExpression(),
                        new BlockStatement(new BreakStatement(Expression.MYSTERY_OFFSET))
                    )
                ),
                new BlockStatement(
                    new Repeat(new AnyNode("statement")).toStatement(),
                    new IfElseStatement(
                        Expression.MYSTERY_OFFSET,
                        new AnyNode("continueCondition").toExpression(),
                        new BlockStatement(new NamedNode("continueStatement", new ContinueStatement(Expression.MYSTERY_OFFSET)).toStatement())
                    ),
                    new NamedNode("breakStatement", new BreakStatement(Expression.MYSTERY_OFFSET)).toStatement()
                )
            ).toBlockStatement()
        );

        DO_WHILE_PATTERN = doWhile;
    }

    public final DoWhileStatement transformDoWhile(final WhileStatement loop) {
        final Match m = DO_WHILE_PATTERN.match(loop);

        if (!m.success() || !canConvertWhileToDoWhile(loop, firstOrDefault(m.<ContinueStatement>get("continueStatement")))) {
            return null;
        }

        final DoWhileStatement doWhile = new DoWhileStatement(loop.getOffset());

        Expression condition = firstOrDefault(m.<Expression>get("continueCondition"));

        final boolean hasContinueCondition = condition != null;

        if (hasContinueCondition) {
            condition.remove();
            first(m.<Statement>get("breakStatement")).remove();
        }
        else {
            condition = firstOrDefault(m.<Expression>get("breakCondition"));
            condition.remove();

            if (condition instanceof UnaryOperatorExpression &&
                ((UnaryOperatorExpression) condition).getOperator() == UnaryOperatorType.NOT) {

                condition = ((UnaryOperatorExpression) condition).getExpression();
                condition.remove();
            }
            else {
                condition = new UnaryOperatorExpression(UnaryOperatorType.NOT, condition);
            }
        }

        doWhile.setCondition(condition);

        final BlockStatement block = (BlockStatement) loop.getEmbeddedStatement();

        lastOrDefault(block.getStatements()).remove();
        block.remove();

        doWhile.setEmbeddedStatement(block);

        loop.replaceWith(doWhile);

        //
        // We may have to extract variable definitions out of the loop if they were used
        // in the condition.
        //

        for (final Statement statement : block.getStatements()) {
            if (statement instanceof VariableDeclarationStatement) {
                final VariableDeclarationStatement declaration = (VariableDeclarationStatement) statement;
                final VariableInitializer v = firstOrDefault(declaration.getVariables());

                for (final AstNode node : condition.getDescendantsAndSelf()) {
                    if (node instanceof IdentifierExpression &&
                        StringUtilities.equals(v.getName(), ((IdentifierExpression) node).getIdentifier())) {

                        final Expression initializer = v.getInitializer();

                        initializer.remove();

                        final AssignmentExpression assignment = new AssignmentExpression(
                            new IdentifierExpression(statement.getOffset(), v.getName()),
                            initializer
                        );

                        assignment.putUserData(Keys.MEMBER_REFERENCE, initializer.getUserData(Keys.MEMBER_REFERENCE));
                        assignment.putUserData(Keys.VARIABLE, initializer.getUserData(Keys.VARIABLE));

                        v.putUserData(Keys.MEMBER_REFERENCE, null);
                        v.putUserData(Keys.VARIABLE, null);

                        assignment.putUserData(Keys.MEMBER_REFERENCE, declaration.getUserData(Keys.MEMBER_REFERENCE));
                        assignment.putUserData(Keys.VARIABLE, declaration.getUserData(Keys.VARIABLE));

                        declaration.replaceWith(new ExpressionStatement(assignment));

                        declaration.putUserData(Keys.MEMBER_REFERENCE, null);
                        declaration.putUserData(Keys.VARIABLE, null);

                        doWhile.getParent().insertChildBefore(doWhile, declaration, BlockStatement.STATEMENT_ROLE);
                    }
                }
            }
        }

        return doWhile;
    }

    private boolean canConvertWhileToDoWhile(final WhileStatement loop, final ContinueStatement continueStatement) {
        final List<ContinueStatement> continueStatements = new ArrayList<>();

        for (final AstNode node : loop.getDescendantsAndSelf()) {
            if (node instanceof ContinueStatement) {
                continueStatements.add((ContinueStatement) node);
            }
        }

        if (continueStatements.isEmpty()) {
            return true;
        }

        for (final ContinueStatement cs : continueStatements) {
            final String label = cs.getLabel();

            if (StringUtilities.isNullOrEmpty(label) && cs != continueStatement) {
                return false;
            }

            final Statement previousStatement = loop.getPreviousStatement();

            if (previousStatement instanceof LabelStatement) {
                return !StringUtilities.equals(
                    ((LabelStatement) previousStatement).getLabel(),
                    label
                );
            }

            if (loop.getParent() instanceof LabeledStatement) {
                return !StringUtilities.equals(
                    ((LabeledStatement) loop.getParent()).getLabel(),
                    label
                );
            }
        }

        return true;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Continue Outer Loop Transforms">

    private final static WhileStatement CONTINUE_OUTER_PATTERN;

    static {
        final WhileStatement continueOuter = new WhileStatement(Expression.MYSTERY_OFFSET);

        continueOuter.setCondition(new AnyNode().toExpression());

        continueOuter.setEmbeddedStatement(
            new BlockStatement(
                new NamedNode("label", new LabelStatement(Expression.MYSTERY_OFFSET, Pattern.ANY_STRING)).toStatement(),
                new Repeat(new AnyNode("statement")).toStatement()
            )
        );

        CONTINUE_OUTER_PATTERN = continueOuter;
    }

    public final WhileStatement transformContinueOuter(final WhileStatement loop) {
        final Match m = CONTINUE_OUTER_PATTERN.match(loop);

        if (!m.success()) {
            return loop;
        }

        final LabelStatement label = (LabelStatement) m.get("label").iterator().next();

        label.remove();
        loop.getParent().insertChildBefore(loop, label, BlockStatement.STATEMENT_ROLE);

        return loop;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Helper Methods">

    static VariableDeclarationStatement findVariableDeclaration(final AstNode node, final String identifier) {
        AstNode current = node;
        while (current != null) {
            while (current.getPreviousSibling() != null) {
                current = current.getPreviousSibling();
                if (current instanceof VariableDeclarationStatement) {
                    final VariableDeclarationStatement variableDeclaration = (VariableDeclarationStatement) current;
                    final Variable variable = variableDeclaration.getUserData(Keys.VARIABLE);

                    if (variable != null && StringUtilities.equals(variable.getName(), identifier)) {
                        return variableDeclaration;
                    }

                    if (variableDeclaration.getVariables().size() == 1 &&
                        StringUtilities.equals(variableDeclaration.getVariables().firstOrNullObject().getName(), identifier)) {

                        return variableDeclaration;
                    }
                }
            }
            current = current.getParent();
        }
        return null;
    }

    static Statement canMoveVariableDeclarationIntoStatement(
        final DecompilerContext context,
        final VariableDeclarationStatement declaration,
        final Statement targetStatement) {

        if (declaration == null) {
            return null;
        }

        final BlockStatement parent = (BlockStatement) declaration.getParent();

        //noinspection AssertWithSideEffects
        assert CollectionUtilities.contains(targetStatement.getAncestors(), parent);

        //
        // Find all blocks between targetStatement and declaration's parent block.
        //
        final ArrayList<BlockStatement> blocks = new ArrayList<>();

        for (final AstNode block : targetStatement.getAncestors()) {
            if (block == parent) {
                break;
            }

            if (block instanceof BlockStatement) {
                blocks.add((BlockStatement) block);
            }
        }

        //
        // Also handle the declaration's parent block itself.
        //
        blocks.add(parent);

        //
        // Go from parent blocks to child blocks.
        //
        Collections.reverse(blocks);

        final StrongBox<Statement> declarationPoint = new StrongBox<>();
        final DefiniteAssignmentAnalysis analysis = new DefiniteAssignmentAnalysis(context, blocks.get(0));

        Statement result = null;

        for (final BlockStatement block : blocks) {
            if (!DeclareVariablesTransform.findDeclarationPoint(analysis, declaration, block, declarationPoint, null)) {
                break;
            }
            result = declarationPoint.get();
        }

        return result;
    }

    // </editor-fold>
}
