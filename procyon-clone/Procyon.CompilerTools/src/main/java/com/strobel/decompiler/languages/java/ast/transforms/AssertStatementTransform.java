/*
 * AssertStatementTransform.java
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

import com.strobel.assembler.metadata.FieldDefinition;
import com.strobel.assembler.metadata.FieldReference;
import com.strobel.assembler.metadata.MemberReference;
import com.strobel.assembler.metadata.MetadataHelper;
import com.strobel.assembler.metadata.MetadataResolver;
import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.decompiler.patterns.*;
import com.strobel.functions.Function;

import static com.strobel.core.CollectionUtilities.*;

public class AssertStatementTransform extends ContextTrackingVisitor<Void> {
    public AssertStatementTransform(final DecompilerContext context) {
        super(context);
    }

    private final static IfElseStatement ASSERT_PATTERN;
    private final static AssignmentExpression ASSERTIONS_DISABLED_PATTERN;

    static {
        ASSERT_PATTERN = new IfElseStatement(
            Expression.MYSTERY_OFFSET,
            new Choice(
                new UnaryOperatorExpression(
                    UnaryOperatorType.NOT,
                    new Choice(
                        new BinaryOperatorExpression(
                            new LeftmostBinaryOperandNode(
                                new NamedNode(
                                    "assertionsDisabledCheck",
                                    new TypeReferenceExpression(Expression.MYSTERY_OFFSET, new SimpleType(Pattern.ANY_STRING)).member("$assertionsDisabled")
                                ),
                                BinaryOperatorType.LOGICAL_OR,
                                true
                            ).toExpression(),
                            BinaryOperatorType.LOGICAL_OR,
                            new AnyNode("condition").toExpression()
                        ),
                        new TypeReferenceExpression(Expression.MYSTERY_OFFSET, new SimpleType(Pattern.ANY_STRING)).member("$assertionsDisabled")
                    ).toExpression()
                ),
                new BinaryOperatorExpression(
                    new LeftmostBinaryOperandNode(
                        new UnaryOperatorExpression(
                            UnaryOperatorType.NOT,
                            new NamedNode(
                                "assertionsDisabledCheck",
                                new TypeReferenceExpression(Expression.MYSTERY_OFFSET, new SimpleType(Pattern.ANY_STRING)).member("$assertionsDisabled")
                            ).toExpression()
                        ),
                        BinaryOperatorType.LOGICAL_AND,
                        true
                    ).toExpression(),
                    BinaryOperatorType.LOGICAL_AND,
                    new AnyNode("invertedCondition").toExpression()
                )
            ).toExpression(),
            new BlockStatement(
                new ThrowStatement(
                    new ObjectCreationExpression(
                        Expression.MYSTERY_OFFSET,
                        new SimpleType("AssertionError"),
                        new OptionalNode(new AnyNode("message")).toExpression()
                    )
                )
            )
        );

        ASSERTIONS_DISABLED_PATTERN = new AssignmentExpression(
            new NamedNode(
                "$assertionsDisabled",
                new Choice(
                    new IdentifierExpression(Expression.MYSTERY_OFFSET, "$assertionsDisabled"),
                    new TypedNode(TypeReferenceExpression.class).toExpression().member("$assertionsDisabled")
                )
            ).toExpression(),
            new UnaryOperatorExpression(
                UnaryOperatorType.NOT,
                new InvocationExpression(
                    Expression.MYSTERY_OFFSET,
                    new MemberReferenceExpression(
                        Expression.MYSTERY_OFFSET,
                        new NamedNode(
                            "type",
                            new ClassOfExpression(
                                Expression.MYSTERY_OFFSET,
                                new SimpleType(Pattern.ANY_STRING)
                            )
                        ).toExpression(),
                        "desiredAssertionStatus"
                    )
                )
            )
        );
    }

    @Override
    public Void visitIfElseStatement(final IfElseStatement node, final Void data) {
        super.visitIfElseStatement(node, data);

        transformAssert(node);

        return null;
    }

    @Override
    public Void visitAssignmentExpression(final AssignmentExpression node, final Void data) {
        super.visitAssignmentExpression(node, data);

        removeAssertionsDisabledAssignment(node);

        return null;
    }

    private void removeAssertionsDisabledAssignment(final AssignmentExpression node) {
        if (context.getSettings().getShowSyntheticMembers()) {
            return;
        }

        final Match m = ASSERTIONS_DISABLED_PATTERN.match(node);

        if (!m.success()) {
            return;
        }

        final AstNode parent = node.getParent();

        if (!(parent instanceof ExpressionStatement &&
              parent.getParent() instanceof BlockStatement &&
              parent.getParent().getParent() instanceof MethodDeclaration)) {

            return;
        }

        final MethodDeclaration staticInitializer = (MethodDeclaration) parent.getParent().getParent();
        final MethodDefinition methodDefinition = staticInitializer.getUserData(Keys.METHOD_DEFINITION);

        if (methodDefinition == null || !methodDefinition.isTypeInitializer()) {
            return;
        }

        final Expression field = first(m.<IdentifierExpression>get("$assertionsDisabled"));
        final ClassOfExpression type = m.<ClassOfExpression>get("type").iterator().next();
        final MemberReference reference = field.getUserData(Keys.MEMBER_REFERENCE);

        if (!(reference instanceof FieldReference)) {
            return;
        }

        final FieldDefinition resolvedField = ((FieldReference) reference).resolve();

        if (resolvedField == null || !resolvedField.isSynthetic()) {
            return;
        }

        final TypeReference typeReference = type.getType().getUserData(Keys.TYPE_REFERENCE);

        if (typeReference != null &&
            (MetadataResolver.areEquivalent(context.getCurrentType(), typeReference) ||
             MetadataHelper.isEnclosedBy(context.getCurrentType(), typeReference))) {

            parent.remove();

            if (staticInitializer.getBody().getStatements().isEmpty()) {
                staticInitializer.remove();
            }
        }
    }

    private AssertStatement transformAssert(final IfElseStatement ifElse) {
        final Match m = ASSERT_PATTERN.match(ifElse);

        if (!m.success()) {
            return null;
        }

        final Expression assertionsDisabledCheck = firstOrDefault(m.<Expression>get("assertionsDisabledCheck"));

        Expression condition = firstOrDefault(m.<Expression>get("condition"));

        if (condition == null) {
            condition = firstOrDefault(m.<Expression>get("invertedCondition"));

            if (condition != null) {
                condition = condition.replaceWith(
                    new Function<AstNode, Expression>() {
                        @Override
                        public Expression apply(final AstNode input) {
                            return new UnaryOperatorExpression(UnaryOperatorType.NOT, (Expression) input);
                        }
                    }
                );
            }
        }

        if (condition != null &&
            assertionsDisabledCheck != null &&
            assertionsDisabledCheck.getParent() instanceof BinaryOperatorExpression &&
            assertionsDisabledCheck.getParent().getParent() instanceof BinaryOperatorExpression) {

            final BinaryOperatorExpression logicalOr = (BinaryOperatorExpression) assertionsDisabledCheck.getParent();
            final Expression right = logicalOr.getRight();

            right.remove();
            assertionsDisabledCheck.replaceWith(right);
            condition.remove();
            logicalOr.setRight(condition);
            condition = logicalOr;
        }

        final AssertStatement assertStatement = new AssertStatement(
            condition == null ? ifElse.getOffset() : condition.getOffset()
        );

        if (condition != null) {
            condition.remove();
            assertStatement.setCondition(condition);
        }
        else {
            assertStatement.setCondition(new PrimitiveExpression(Expression.MYSTERY_OFFSET, false));
        }

        if (m.has("message")) {
            Expression message = firstOrDefault(m.<Expression>get("message"));

            while (message instanceof CastExpression) {
                message = ((CastExpression) message).getExpression();
            }

            message.remove();
            assertStatement.setMessage(message);
        }

        ifElse.replaceWith(assertStatement);

        return assertStatement;
    }
}
