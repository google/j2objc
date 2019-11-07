/*
 * InsertParenthesesVisitor.java
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

import com.strobel.functions.Function;
import com.strobel.util.ContractUtils;

public final class InsertParenthesesVisitor extends DepthFirstAstVisitor<Void, Void> {
    private final static int PRIMARY = 16;
    private final static int CAST = 15;
    private final static int UNARY = 14;
    private static final int MULTIPLICATIVE = 13;
    private static final int ADDITIVE = 12;
    private static final int SHIFT = 11;
    private final static int RELATIONAL_AND_TYPE_TESTING = 10;
    private final static int EQUALITY = 9;
    private static final int BITWISE_AND = 8;
    private static final int EXCLUSIVE_OR = 7;
    private static final int BITWISE_OR = 6;
    private static final int LOGICAL_AND = 5;
    private static final int LOGICAL_OR = 4;
    private final static int CONDITIONAL = 2;
    private final static int ASSIGNMENT = 1;

    private final static Function<AstNode, AstNode> PARENTHESIZE_FUNCTION = new Function<AstNode, AstNode>() {
        @Override
        public AstNode apply(final AstNode input) {
            return new ParenthesizedExpression((Expression) input);
        }
    };

    private boolean _insertParenthesesForReadability = true;

    public final boolean getInsertParenthesesForReadability() {
        return _insertParenthesesForReadability;
    }

    public final void setInsertParenthesesForReadability(final boolean insertParenthesesForReadability) {
        _insertParenthesesForReadability = insertParenthesesForReadability;
    }

    private static int getPrecedence(final Expression e) {
        if (e instanceof UnaryOperatorExpression) {
            final UnaryOperatorExpression unary = (UnaryOperatorExpression) e;

            if (unary.getOperator() == UnaryOperatorType.POST_DECREMENT ||
                unary.getOperator() == UnaryOperatorType.POST_INCREMENT) {

                return PRIMARY;
            }

            return UNARY;
        }

        if (e instanceof CastExpression) {
            return CAST;
        }

        if (e instanceof BinaryOperatorExpression) {
            final BinaryOperatorExpression binary = (BinaryOperatorExpression) e;

            switch (binary.getOperator()) {
                case MULTIPLY:
                case DIVIDE:
                case MODULUS:
                    return MULTIPLICATIVE;

                case ADD:
                case SUBTRACT:
                    return ADDITIVE;

                case SHIFT_LEFT:
                case SHIFT_RIGHT:
                case UNSIGNED_SHIFT_RIGHT:
                    return SHIFT;

                case GREATER_THAN:
                case GREATER_THAN_OR_EQUAL:
                case LESS_THAN:
                case LESS_THAN_OR_EQUAL:
                    return RELATIONAL_AND_TYPE_TESTING;

                case EQUALITY:
                case INEQUALITY:
                    return EQUALITY;

                case BITWISE_AND:
                    return BITWISE_AND;
                case EXCLUSIVE_OR:
                    return EXCLUSIVE_OR;
                case BITWISE_OR:
                    return BITWISE_OR;
                case LOGICAL_AND:
                    return LOGICAL_AND;
                case LOGICAL_OR:
                    return LOGICAL_OR;

                default:
                    throw ContractUtils.unsupported();
            }
        }

        if (e instanceof InstanceOfExpression) {
            return RELATIONAL_AND_TYPE_TESTING;
        }

        if (e instanceof ConditionalExpression) {
            return CONDITIONAL;
        }

        if (e instanceof AssignmentExpression || e instanceof LambdaExpression) {
            return ASSIGNMENT;
        }

        return PRIMARY;
    }

    private static BinaryOperatorType getBinaryOperatorType(final Expression e) {
        if (e instanceof BinaryOperatorExpression) {
            return ((BinaryOperatorExpression) e).getOperator();
        }
        return null;
    }

    private static void parenthesizeIfRequired(final Expression expression, final int minimumPrecedence) {
        if (getPrecedence(expression) < minimumPrecedence) {
            parenthesize(expression);
        }
    }

    private static void parenthesize(final Expression expression) {
        expression.replaceWith(PARENTHESIZE_FUNCTION);
    }

    private static boolean canTypeBeMisinterpretedAsExpression(final AstType type) {
        return type instanceof SimpleType;
    }

    @Override
    public Void visitMemberReferenceExpression(final MemberReferenceExpression node, final Void data) {
        parenthesizeIfRequired(node.getTarget(), PRIMARY);
        return super.visitMemberReferenceExpression(node, data);
    }

    @Override
    public Void visitInvocationExpression(final InvocationExpression node, final Void data) {
        parenthesizeIfRequired(node.getTarget(), PRIMARY);
        return super.visitInvocationExpression(node, data);
    }

    @Override
    public Void visitIndexerExpression(final IndexerExpression node, final Void data) {
        parenthesizeIfRequired(node.getTarget(), PRIMARY);

        if (node.getTarget() instanceof ArrayCreationExpression) {
            final ArrayCreationExpression arrayCreation = (ArrayCreationExpression) node.getTarget();

            if (_insertParenthesesForReadability || arrayCreation.getInitializer().isNull()) {
                // require parentheses for "(new int[1])[0]"
                parenthesize(arrayCreation);
            }
        }

        return super.visitIndexerExpression(node, data);
    }

    @Override
    public Void visitUnaryOperatorExpression(final UnaryOperatorExpression node, final Void data) {
        final Expression child = node.getExpression();

        parenthesizeIfRequired(child, getPrecedence(node));

        if (_insertParenthesesForReadability && child instanceof UnaryOperatorExpression) {
            parenthesize(child);
        }

        return super.visitUnaryOperatorExpression(node, data);
    }

    @Override
    public Void visitCastExpression(final CastExpression node, final Void data) {
        final Expression child = node.getExpression();

        parenthesizeIfRequired(child, UNARY);

        if (child instanceof UnaryOperatorExpression) {
            final UnaryOperatorExpression childUnary = (UnaryOperatorExpression) child;

            if (childUnary.getOperator() != UnaryOperatorType.BITWISE_NOT &&
                childUnary.getOperator() != UnaryOperatorType.NOT) {

                if (canTypeBeMisinterpretedAsExpression(node.getType())) {
                    parenthesize(child);
                }
            }
        }

        if (child instanceof PrimitiveExpression) {
            final PrimitiveExpression primitive = (PrimitiveExpression) child;
            final Object primitiveValue = primitive.getValue();

            if (primitiveValue instanceof Number) {
                final Number number = (Number) primitiveValue;

                if (primitiveValue instanceof Float || primitiveValue instanceof Double) {
                    if (number.doubleValue() < 0d) {
                        parenthesize(child);
                    }
                }
                else if (number.longValue() < 0L) {
                    parenthesize(child);
                }
            }
        }

        return super.visitCastExpression(node, data);
    }

    @Override
    public Void visitBinaryOperatorExpression(final BinaryOperatorExpression node, final Void data) {
        final int precedence = getPrecedence(node);

        if (_insertParenthesesForReadability && precedence < EQUALITY) {
            if (getBinaryOperatorType(node.getLeft()) == node.getOperator()) {
                parenthesizeIfRequired(node.getLeft(), precedence);
            }
            else {
                parenthesizeIfRequired(node.getLeft(), EQUALITY);
            }
            parenthesizeIfRequired(node.getRight(), EQUALITY);
        }
        else {
            parenthesizeIfRequired(node.getLeft(), precedence);
            parenthesizeIfRequired(node.getRight(), precedence + 1);
        }

        return super.visitBinaryOperatorExpression(node, data);
    }

    @Override
    public Void visitInstanceOfExpression(final InstanceOfExpression node, final Void data) {
        if (_insertParenthesesForReadability) {
            parenthesizeIfRequired(node.getExpression(), PRIMARY);
        }
        else {
            parenthesizeIfRequired(node.getExpression(), RELATIONAL_AND_TYPE_TESTING);
        }
        return super.visitInstanceOfExpression(node, data);
    }

    @Override
    public Void visitConditionalExpression(final ConditionalExpression node, final Void data) {
        //
        // Associativity here is a bit tricky:
        //
        //     (a ? b : c ? d : e) == (a ? b : (c ? d : e))
        //     (a ? b ? c : d : e) == (a ? (b ? c : d) : e)
        //
        // Only ((a ? b : c) ? d : e) strictly needs the additional parentheses.
        //

        if (_insertParenthesesForReadability) {
            // Precedence of ?: can be confusing; so always put parentheses in nice-looking mode.
            parenthesizeIfRequired(node.getCondition(), PRIMARY);
            parenthesizeIfRequired(node.getTrueExpression(), PRIMARY);
            parenthesizeIfRequired(node.getFalseExpression(), PRIMARY);
        }
        else {
            parenthesizeIfRequired(node.getCondition(), CONDITIONAL + 1);
            parenthesizeIfRequired(node.getTrueExpression(), CONDITIONAL);
            parenthesizeIfRequired(node.getFalseExpression(), CONDITIONAL);
        }

        return super.visitConditionalExpression(node, data);
    }

    @Override
    public Void visitAssignmentExpression(final AssignmentExpression node, final Void data) {
        parenthesizeIfRequired(node.getLeft(), ASSIGNMENT + 1);

        if (_insertParenthesesForReadability) {
            parenthesizeIfRequired(node.getRight(), RELATIONAL_AND_TYPE_TESTING + 1);
        }
        else {
            parenthesizeIfRequired(node.getRight(), ASSIGNMENT);
        }

        return super.visitAssignmentExpression(node, data);
    }
}
