/*
 * SimplifyAssignmentsTransform.java
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

import com.strobel.assembler.metadata.CommonTypeReferences;
import com.strobel.assembler.metadata.MetadataResolver;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.decompiler.languages.java.utilities.RedundantCastUtility;
import com.strobel.decompiler.semantics.ResolveResult;
import com.strobel.functions.Function;

public class SimplifyAssignmentsTransform extends ContextTrackingVisitor<AstNode> implements IAstTransform {
    private final static Function<AstNode, AstNode> NEGATE_FUNCTION = new Function<AstNode, AstNode>() {
        @Override
        public AstNode apply(final AstNode n) {
            if (n instanceof UnaryOperatorExpression) {
                final UnaryOperatorExpression unary = (UnaryOperatorExpression) n;

                if (unary.getOperator() == UnaryOperatorType.NOT) {
                    final Expression operand = unary.getExpression();
                    operand.remove();
                    return operand;
                }
            }
            return new UnaryOperatorExpression(UnaryOperatorType.NOT, (Expression) n);
        }
    };

    private final JavaResolver _resolver;

    public SimplifyAssignmentsTransform(final DecompilerContext context) {
        super(context);
        _resolver = new JavaResolver(context);
    }

    private final static PrimitiveExpression TRUE_CONSTANT = new PrimitiveExpression(Expression.MYSTERY_OFFSET, true);
    private final static PrimitiveExpression FALSE_CONSTANT = new PrimitiveExpression(Expression.MYSTERY_OFFSET, false);

    @Override
    public AstNode visitConditionalExpression(final ConditionalExpression node, final Void data) {
        final Expression condition = node.getCondition();
        final Expression trueExpression = node.getTrueExpression();
        final Expression falseExpression = node.getFalseExpression();

        if (TRUE_CONSTANT.matches(trueExpression) &&
            FALSE_CONSTANT.matches(falseExpression)) {

            condition.remove();
            trueExpression.remove();
            falseExpression.remove();

            node.replaceWith(condition);

            return condition.acceptVisitor(this, data);
        }
        else if (TRUE_CONSTANT.matches(trueExpression) &&
                 FALSE_CONSTANT.matches(falseExpression)) {

            condition.remove();
            trueExpression.remove();
            falseExpression.remove();

            final Expression negatedCondition = new UnaryOperatorExpression(UnaryOperatorType.NOT, condition);

            node.replaceWith(negatedCondition);

            return negatedCondition.acceptVisitor(this, data);
        }

        return super.visitConditionalExpression(node, data);
    }

    @Override
    public AstNode visitBinaryOperatorExpression(final BinaryOperatorExpression node, final Void data) {
        final BinaryOperatorType operator = node.getOperator();

        if (operator == BinaryOperatorType.EQUALITY ||
            operator == BinaryOperatorType.INEQUALITY) {

            final Expression left = node.getLeft();
            final Expression right = node.getRight();

            if (TRUE_CONSTANT.matches(left) || FALSE_CONSTANT.matches(left)) {
                if (TRUE_CONSTANT.matches(right) || FALSE_CONSTANT.matches(right)) {
                    return new PrimitiveExpression( node.getOffset(),
                        (TRUE_CONSTANT.matches(left) == TRUE_CONSTANT.matches(right)) ^
                        operator == BinaryOperatorType.INEQUALITY
                    );
                }

                final boolean negate = FALSE_CONSTANT.matches(left) ^ operator == BinaryOperatorType.INEQUALITY;

                right.remove();

                final Expression replacement = negate ? new UnaryOperatorExpression(UnaryOperatorType.NOT, right)
                                                      : right;

                node.replaceWith(replacement);

                return replacement.acceptVisitor(this, data);
            }
            else if (TRUE_CONSTANT.matches(right) || FALSE_CONSTANT.matches(right)) {
                final boolean negate = FALSE_CONSTANT.matches(right) ^ operator == BinaryOperatorType.INEQUALITY;

                left.remove();

                final Expression replacement = negate ? new UnaryOperatorExpression(UnaryOperatorType.NOT, left)
                                                      : left;

                node.replaceWith(replacement);

                return replacement.acceptVisitor(this, data);
            }
        }

        return super.visitBinaryOperatorExpression(node, data);
    }

    @Override
    public AstNode visitUnaryOperatorExpression(final UnaryOperatorExpression node, final Void p) {
        if (node.getOperator() == UnaryOperatorType.NOT &&
            node.getExpression() instanceof BinaryOperatorExpression) {

            final BinaryOperatorExpression binary = (BinaryOperatorExpression) node.getExpression();

            boolean successful = true;

            switch (binary.getOperator()) {
                case EQUALITY:
                    binary.setOperator(BinaryOperatorType.INEQUALITY);
                    break;

                case INEQUALITY:
                    binary.setOperator(BinaryOperatorType.EQUALITY);
                    break;

                case GREATER_THAN:
                    binary.setOperator(BinaryOperatorType.LESS_THAN_OR_EQUAL);
                    break;

                case GREATER_THAN_OR_EQUAL:
                    binary.setOperator(BinaryOperatorType.LESS_THAN);
                    break;

                case LESS_THAN:
                    binary.setOperator(BinaryOperatorType.GREATER_THAN_OR_EQUAL);
                    break;

                case LESS_THAN_OR_EQUAL:
                    binary.setOperator(BinaryOperatorType.GREATER_THAN);
                    break;

                default:
                    successful = false;
                    break;
            }

            if (successful) {
                node.replaceWith(binary);
                return binary.acceptVisitor(this, p);
            }

            successful = true;

            switch (binary.getOperator()) {
                case LOGICAL_AND:
                    binary.setOperator(BinaryOperatorType.LOGICAL_OR);
                    break;

                case LOGICAL_OR:
                    binary.setOperator(BinaryOperatorType.LOGICAL_AND);
                    break;

                default:
                    successful = false;
                    break;
            }

            if (successful) {
                binary.getLeft().replaceWith(NEGATE_FUNCTION);
                binary.getRight().replaceWith(NEGATE_FUNCTION);
                node.replaceWith(binary);
                return binary.acceptVisitor(this, p);
            }
        }

        return super.visitUnaryOperatorExpression(node, p);
    }

    @Override
    public AstNode visitAssignmentExpression(final AssignmentExpression node, final Void data) {
        final Expression left = node.getLeft();
        final Expression right = node.getRight();

        if (node.getOperator() == AssignmentOperatorType.ASSIGN) {
            if (right instanceof CastExpression) {
                //
                // T t; t = (T)(t + n) => t += n
                //

                final CastExpression castExpression = (CastExpression) right;
                final TypeReference castType = castExpression.getType().toTypeReference();
                final Expression castedValue = castExpression.getExpression();

                if (castType != null &&
                    castType.isPrimitive() &&
                    castedValue instanceof BinaryOperatorExpression) {

                    final ResolveResult leftResult = _resolver.apply(left);

                    if (leftResult != null &&
                        MetadataResolver.areEquivalent(castType, leftResult.getType()) &&
                        tryRewriteBinaryAsAssignment(node, left, castedValue)) {

                        final Expression newValue = castExpression.getExpression();

                        newValue.remove();
                        right.replaceWith(newValue);

                        return newValue.acceptVisitor(this, data);
                    }
                }
            }

            if (tryRewriteBinaryAsAssignment(node, left, right)) {
                return left.getParent().acceptVisitor(this, data);
            }
        }
        else if (tryRewriteBinaryAsUnary(node, left, right)) {
            return left.getParent().acceptVisitor(this, data);
        }

        return super.visitAssignmentExpression(node, data);
    }

    private boolean tryRewriteBinaryAsAssignment(final AssignmentExpression node, final Expression left, final Expression right) {
        if (right instanceof BinaryOperatorExpression) {
            final BinaryOperatorExpression binary = (BinaryOperatorExpression) right;
            final Expression innerLeft = binary.getLeft();
            final Expression innerRight = binary.getRight();
            final BinaryOperatorType binaryOp = binary.getOperator();

            if (innerLeft.matches(left)) {
                final AssignmentOperatorType assignOp = AssignmentExpression.getCorrespondingAssignmentOperator(binaryOp);

                if (assignOp != null) {
                    innerRight.remove();
                    right.replaceWith(innerRight);
                    node.setOperator(assignOp);

                    //
                    // t = t (op) n => t (op)= n
                    //

                    tryRewriteBinaryAsUnary(node, node.getLeft(), node.getRight());
                    return true;
                }
            }
            else if (binaryOp.isCommutative() && innerRight.matches(left)) {
                final ResolveResult leftResult = _resolver.apply(left);
                final ResolveResult innerLeftResult = _resolver.apply(innerLeft);

                //
                // The transform from `t = n + t` to `t += n` is legal for numeric types, but the
                // commutative property does not hold for string concatenation (i.e., the result
                // depends on the order of the operands).  If the assignment target does not match
                // the left operand, and either operand is a string, then do not apply a compound
                // assignment transform.
                //

                if (leftResult == null || leftResult.getType() == null ||
                    innerLeftResult == null || innerLeftResult.getType() == null) {

                    return false;
                }

                if (CommonTypeReferences.String.isEquivalentTo(leftResult.getType()) ||
                    CommonTypeReferences.String.isEquivalentTo(innerLeftResult.getType())) {

                    return false;
                }

                final AssignmentOperatorType assignOp = AssignmentExpression.getCorrespondingAssignmentOperator(binaryOp);

                //
                // t = n (op) t => t (op)= n
                //

                innerLeft.remove();
                right.replaceWith(innerLeft);
                node.setOperator(assignOp);
                return true;
            }
        }

        return false;
    }

    private boolean tryRewriteBinaryAsUnary(final AssignmentExpression node, final Expression left, final Expression right) {
        final AssignmentOperatorType op = node.getOperator();

        if (op == AssignmentOperatorType.ADD ||
            op == AssignmentOperatorType.SUBTRACT) {

            Expression innerRight = right;

            while (innerRight instanceof CastExpression &&
                   RedundantCastUtility.isCastRedundant(_resolver, (CastExpression) innerRight)) {

                innerRight = ((CastExpression) innerRight).getExpression();
            }

            if (!(innerRight instanceof PrimitiveExpression)) {
                return false;
            }

            final Object value = ((PrimitiveExpression) innerRight).getValue();

            long delta = 0L;

            if (value instanceof Number) {
                final Number n = (Number) value;

                if (value instanceof Float || value instanceof Double) {
                    final double d = n.doubleValue();

                    if (Math.abs(d) == 1d) {
                        delta = (long) d;
                    }
                }
                else {
                    delta = n.longValue();
                }
            }
            else if (value instanceof Character) {
                delta = (Character) value;
            }

            if (Math.abs(delta) == 1L) {
                final UnaryOperatorType unaryOp;
                final boolean increment = (delta == 1L) ^ (op == AssignmentOperatorType.SUBTRACT);

                //
                // (t += +1) => ++t
                // (t += -1) => --t
                // (t -= +1) => --t
                // (t -= -1) => ++t
                //

                unaryOp = increment ? UnaryOperatorType.INCREMENT
                                    : UnaryOperatorType.DECREMENT;

                left.remove();
                node.replaceWith(new UnaryOperatorExpression(unaryOp, left));

                return true;
            }
        }

        return false;
    }
}
