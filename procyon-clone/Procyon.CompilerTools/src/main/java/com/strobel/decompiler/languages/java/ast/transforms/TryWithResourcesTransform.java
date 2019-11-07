/*
 * TryWithResourcesTransform.java
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

import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.decompiler.patterns.AnyNode;
import com.strobel.decompiler.patterns.INode;
import com.strobel.decompiler.patterns.IdentifierExpressionBackReference;
import com.strobel.decompiler.patterns.Match;
import com.strobel.decompiler.patterns.NamedNode;
import com.strobel.decompiler.patterns.Pattern;
import com.strobel.decompiler.semantics.ResolveResult;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

import static com.strobel.core.CollectionUtilities.*;
import static com.strobel.decompiler.languages.java.ast.transforms.ConvertLoopsTransform.*;

public class TryWithResourcesTransform extends ContextTrackingVisitor<Void> {
    private final static INode RESOURCE_INIT_PATTERN;
    private final static INode CLEAR_SAVED_EXCEPTION_PATTERN;

    static {
        final Expression resource = new NamedNode(
            "resource",
            new IdentifierExpression(Expression.MYSTERY_OFFSET, Pattern.ANY_STRING)
        ).toExpression();

        final Expression savedException = new NamedNode(
            "savedException",
            new IdentifierExpression(Expression.MYSTERY_OFFSET, Pattern.ANY_STRING)
        ).toExpression();

        RESOURCE_INIT_PATTERN = new ExpressionStatement(
            new AssignmentExpression(
                resource,
                AssignmentOperatorType.ASSIGN,
                new AnyNode("resourceInitializer").toExpression()
            )
        );

        CLEAR_SAVED_EXCEPTION_PATTERN = new ExpressionStatement(
            new AssignmentExpression(
                savedException,
                AssignmentOperatorType.ASSIGN,
                new NullReferenceExpression(Expression.MYSTERY_OFFSET)
            )
        );
    }

    private final TryCatchStatement _tryPattern;
    private final AstBuilder _astBuilder;
    private final JavaResolver _resolver;

    public TryWithResourcesTransform(final DecompilerContext context) {
        super(context);

        _astBuilder = context.getUserData(Keys.AST_BUILDER);

        if (_astBuilder == null) {
            _tryPattern = null;
            _resolver = null;

            return;
        }

        _resolver = new JavaResolver(context);

        final TryCatchStatement tryPattern = new TryCatchStatement(Expression.MYSTERY_OFFSET);

        tryPattern.setTryBlock(new AnyNode("tryContent").toBlockStatement());

        final CatchClause catchClause = new CatchClause(
            new BlockStatement(
                new ExpressionStatement(
                    new AssignmentExpression(
                        new IdentifierExpressionBackReference("savedException").toExpression(),
                        new NamedNode("caughtException", new IdentifierExpression(Expression.MYSTERY_OFFSET, Pattern.ANY_STRING)).toExpression()
                    )
                ),
                new ThrowStatement(new IdentifierExpressionBackReference("caughtException").toExpression())
            )
        );

        catchClause.setVariableName(Pattern.ANY_STRING);
        catchClause.getExceptionTypes().add(new SimpleType("Throwable"));

        tryPattern.getCatchClauses().add(catchClause);

        final TryCatchStatement disposeTry = new TryCatchStatement(Expression.MYSTERY_OFFSET);

        disposeTry.setTryBlock(
            new BlockStatement(
                new ExpressionStatement(
                    new IdentifierExpressionBackReference("resource").toExpression().invoke("close")
                )
            )
        );

        final CatchClause disposeCatch = new CatchClause(
            new BlockStatement(
                new ExpressionStatement(
                    new IdentifierExpressionBackReference("savedException").toExpression().invoke(
                        "addSuppressed",
                        new NamedNode("caughtOnClose", new IdentifierExpression(Expression.MYSTERY_OFFSET, Pattern.ANY_STRING)).toExpression()
                    )
                )
            )
        );

        disposeCatch.setVariableName(Pattern.ANY_STRING);
        disposeCatch.getExceptionTypes().add(new SimpleType("Throwable"));

        disposeTry.getCatchClauses().add(disposeCatch);

        tryPattern.setFinallyBlock(
            new BlockStatement(
                new IfElseStatement( Expression.MYSTERY_OFFSET,
                    new BinaryOperatorExpression(
                        new IdentifierExpressionBackReference("resource").toExpression(),
                        BinaryOperatorType.INEQUALITY,
                        new NullReferenceExpression(Expression.MYSTERY_OFFSET)
                    ),
                    new BlockStatement(
                        new IfElseStatement( Expression.MYSTERY_OFFSET,
                            new BinaryOperatorExpression(
                                new IdentifierExpressionBackReference("savedException").toExpression(),
                                BinaryOperatorType.INEQUALITY,
                                new NullReferenceExpression(Expression.MYSTERY_OFFSET)
                            ),
                            new BlockStatement(
                                disposeTry
                            ),
                            new BlockStatement(
                                new ExpressionStatement(
                                    new IdentifierExpressionBackReference("resource").toExpression().invoke("close")
                                )
                            )
                        )
                    )
                )
            )
        );

        _tryPattern = tryPattern;
    }

    @Override
    public void run(final AstNode compilationUnit) {
        if (_tryPattern == null) {
            return;
        }

        super.run(compilationUnit);

        new MergeResourceTryStatementsVisitor(context).run(compilationUnit);
    }

    @Override
    public Void visitTryCatchStatement(final TryCatchStatement node, final Void data) {
        super.visitTryCatchStatement(node, data);

        if (!(node.getParent() instanceof BlockStatement)) {
            return null;
        }

        final BlockStatement parent = (BlockStatement) node.getParent();

        final Statement p = node.getPreviousSibling(BlockStatement.STATEMENT_ROLE);
        final Statement pp = p != null ? p.getPreviousSibling(BlockStatement.STATEMENT_ROLE) : null;

        if (pp == null) {
            return null;
        }

        final Statement initializeResource = pp;
        final Statement clearCaughtException = p;

        final Match m = Match.createNew();

        if (RESOURCE_INIT_PATTERN.matches(initializeResource, m) &&
            CLEAR_SAVED_EXCEPTION_PATTERN.matches(clearCaughtException, m) &&
            _tryPattern.matches(node, m)) {

            final IdentifierExpression resource = first(m.<IdentifierExpression>get("resource"));
            final ResolveResult resourceResult = _resolver.apply(resource);

            if (resourceResult == null || resourceResult.getType() == null) {
                return null;
            }

            final BlockStatement tryContent = first(m.<BlockStatement>get("tryContent"));
            final Expression resourceInitializer = first(m.<Expression>get("resourceInitializer"));
            final IdentifierExpression caughtException = first(m.<IdentifierExpression>get("caughtException"));
            final IdentifierExpression caughtOnClose = first(m.<IdentifierExpression>get("caughtOnClose"));
            final CatchClause caughtParent = first(caughtException.getAncestors(CatchClause.class));
            final CatchClause caughtOnCloseParent = first(caughtOnClose.getAncestors(CatchClause.class));

            if (caughtParent == null ||
                caughtOnCloseParent == null ||
                !Pattern.matchString(caughtException.getIdentifier(), caughtParent.getVariableName()) ||
                !Pattern.matchString(caughtOnClose.getIdentifier(), caughtOnCloseParent.getVariableName())) {

                return null;
            }

            //
            // Find the declaration of the resource variable.
            //

            final VariableDeclarationStatement resourceDeclaration = findVariableDeclaration(
                node,
                resource.getIdentifier()
            );

            if (resourceDeclaration == null || !(resourceDeclaration.getParent() instanceof BlockStatement)) {
                return null;
            }

            final BlockStatement outerTemp = new BlockStatement();
            final BlockStatement temp = new BlockStatement();

            initializeResource.remove();
            clearCaughtException.remove();

            node.replaceWith(outerTemp);

            temp.add(initializeResource);
            temp.add(clearCaughtException);
            temp.add(node);

            outerTemp.add(temp);

            //
            // Now verify that we can move the variable declaration into the 'try'.
            //

            final Statement declarationPoint = canMoveVariableDeclarationIntoStatement(
                context,
                resourceDeclaration,
                node
            );

            node.remove();
            outerTemp.replaceWith(node);

            if (declarationPoint != outerTemp) {
                //
                // We cannot move the declaration into the 'try'; abort.
                //

                initializeResource.remove();
                clearCaughtException.remove();

                parent.insertChildBefore(node, initializeResource, BlockStatement.STATEMENT_ROLE);
                parent.insertChildBefore(node, clearCaughtException, BlockStatement.STATEMENT_ROLE);

                return null;
            }

            tryContent.remove();
            resource.remove();
            resourceInitializer.remove();

            final VariableDeclarationStatement newResourceDeclaration = new VariableDeclarationStatement(
                _astBuilder.convertType(resourceResult.getType()),
                resource.getIdentifier(),
                resourceInitializer
            );

            final Statement firstStatement = firstOrDefault(tryContent.getStatements());
            final Statement lastStatement = lastOrDefault(tryContent.getStatements());

            if (firstStatement != null) {
                final DefiniteAssignmentAnalysis analysis = new DefiniteAssignmentAnalysis(context, tryContent);

                analysis.setAnalyzedRange(firstStatement, lastStatement);
                analysis.analyze(resource.getIdentifier(), DefiniteAssignmentStatus.DEFINITELY_NOT_ASSIGNED);

                if (!analysis.isPotentiallyAssigned()) {
                    newResourceDeclaration.addModifier(Modifier.FINAL);
                }
            }
            else {
                newResourceDeclaration.addModifier(Modifier.FINAL);
            }

            node.setTryBlock(tryContent);
            node.getResources().add(newResourceDeclaration);

            node.getCatchClauses().clear();
            node.setFinallyBlock(null);
        }

        return null;
    }

    private final static class MergeResourceTryStatementsVisitor extends ContextTrackingVisitor<Void> {
        MergeResourceTryStatementsVisitor(final DecompilerContext context) {
            super(context);
        }

        @Override
        public Void visitTryCatchStatement(final TryCatchStatement node, final Void data) {
            super.visitTryCatchStatement(node, data);

            if (node.getResources().isEmpty()) {
                return null;
            }

            final List<VariableDeclarationStatement> resources = new ArrayList<>();

            TryCatchStatement current = node;

            while (current.getCatchClauses().isEmpty() &&
                   current.getFinallyBlock().isNull()) {

                final AstNode parent = current.getParent();

                if (parent instanceof BlockStatement &&
                    parent.getParent() instanceof TryCatchStatement) {

                    final TryCatchStatement parentTry = (TryCatchStatement) parent.getParent();

                    if (parentTry.getTryBlock().getStatements().hasSingleElement()) {
                        if (!current.getResources().isEmpty()) {
                            resources.addAll(0, current.getResources());
                        }

                        current = parentTry;
                        continue;
                    }
                }

                break;
            }

            final BlockStatement tryContent = node.getTryBlock();

            if (current != node) {
                for (final VariableDeclarationStatement resource : resources) {
                    resource.remove();
                    current.getResources().add(resource);
                }

                tryContent.remove();
                current.setTryBlock(tryContent);
            }

            return null;
        }
    }
}
