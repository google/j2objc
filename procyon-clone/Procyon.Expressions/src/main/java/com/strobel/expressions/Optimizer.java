/*
 * Optimizer.java
 *
 * Copyright (c) 2012 Mike Strobel
 *
 * This source code is based on the Dynamic Language Runtime from Microsoft,
 *   Copyright (c) Microsoft Corporation.
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.expressions;

import com.strobel.reflection.PrimitiveTypes;
import com.strobel.reflection.Type;
import com.strobel.util.TypeUtils;

import static com.strobel.expressions.Expression.*;

/**
 * @author strobelm
 */
final class Optimizer extends ExpressionVisitor {
    private final static Optimizer OPTIMIZER = new Optimizer();
    
    static Expression optimize(final Expression node) {
        return OPTIMIZER.visit(node);
    }

    static <T> LambdaExpression<T> optimize(final LambdaExpression<T> node) {
        return OPTIMIZER.visitLambda(node);
    }

    @Override
    protected Expression visitBinary(final BinaryExpression node) {
        Expression reduced = reduceNullConstantComparison(node);

        if (reduced != null) {
            return visit(reduced);
        }
        
        reduced = reduceBooleanConstantComparison(node);

        if (reduced != null) {
            return visit(reduced);
        }

        return super.visitBinary(node);
    }
    
    @Override
    protected Expression visitUnary(final UnaryExpression node) {
        Expression reduced = reduceNullConstantCheck(node);

        if (reduced != null) {
            return visit(reduced);
        }

        reduced = reduceDoubleNot(node);

        if (reduced != null) {
            return visit(reduced);
        }

        return super.visitUnary(node);
    }

    private Expression reduceNullConstantCheck(final UnaryExpression node) {
        final Expression operand = node.getOperand();
        final ExpressionType nodeType = node.getNodeType();
        final ExpressionType operandNodeType = operand.getNodeType();

        if (nodeType == ExpressionType.IsNull) {
            if (ConstantCheck.isNull(operand)) {
                if (operandNodeType == ExpressionType.Parameter ||
                    operandNodeType == ExpressionType.Constant) {

                    return constant(Boolean.TRUE);
                }
                return block(operand, constant(Boolean.TRUE));
            }
        }
        else if (nodeType == ExpressionType.IsNotNull) {
            if (ConstantCheck.isNull(operand)) {
                if (operandNodeType == ExpressionType.Parameter ||
                    operandNodeType == ExpressionType.Constant) {

                    return constant(Boolean.FALSE);
                }
                return block(operand, constant(Boolean.FALSE));
            }
        }
        
        return null;
    }
    
    private Expression reduceDoubleNot(final UnaryExpression node) {
        final Type<?> type = node.getType();
        final Expression operand = node.getOperand();

        if (type != PrimitiveTypes.Boolean || operand.getType() != PrimitiveTypes.Boolean) {
            return null;
        }

        final ExpressionType nodeType = node.getNodeType();
        final ExpressionType operandNodeType = operand.getNodeType();
        
        if ((nodeType == ExpressionType.Not || nodeType == ExpressionType.IsFalse) &&
            (operandNodeType == ExpressionType.Not || operandNodeType == ExpressionType.IsFalse)) {
            
            return ((UnaryExpression)operand).getOperand();
        }

        return null;
    }

    private Expression reduceNullConstantComparison(final BinaryExpression node) {
        final Expression left = visit(node.getLeft());
        final Expression right = visit(node.getRight());

        if (node.getType() != PrimitiveTypes.Boolean) {
            return null;
        }

        final ExpressionType nodeType = node.getNodeType();

        switch (nodeType) {
            case Equal:
            case NotEqual:
            case ReferenceEqual:
            case ReferenceNotEqual:
                break;

            default:
                return null;
        }

        if (ConstantCheck.isNull(right)) {
            if (ConstantCheck.isNull(left)) {
                return constant(nodeType == ExpressionType.Equal || nodeType == ExpressionType.ReferenceEqual);
            }
            return nodeType == ExpressionType.Equal || nodeType == ExpressionType.ReferenceEqual
                   ? isNull(left)
                   : isNotNull(left);
        }

        if (ConstantCheck.isNull(left)) {
            return nodeType == ExpressionType.Equal || nodeType == ExpressionType.ReferenceEqual
                   ? isNull(right)
                   : isNotNull(right);
        }

        return null;
    }

    private Expression reduceBooleanConstantComparison(final BinaryExpression node) {
        final Expression left = visit(node.getLeft());
        final Expression right = visit(node.getRight());

        final ExpressionType nodeType = node.getNodeType();

        if (node.getType() != PrimitiveTypes.Boolean ||
            nodeType != ExpressionType.Equal && nodeType != ExpressionType.NotEqual) {
            return null;
        }

        if (ConstantCheck.isTrue(right)) {
            // true [op] true
            if (ConstantCheck.isTrue(left)) {
                return constant(nodeType == ExpressionType.Equal);
            }
            // false [op] true
            if (ConstantCheck.isFalse(left)) {
                return constant(nodeType == ExpressionType.NotEqual);
            }
            // expr [op] true
            if (TypeUtils.getUnderlyingPrimitiveOrSelf(left.getType()) == PrimitiveTypes.Boolean) {
                return nodeType == ExpressionType.Equal ? left : isFalse(left);
            }
            return null;
        }

        if (ConstantCheck.isFalse(right)) {
            // false [op] false
            if (ConstantCheck.isFalse(left)) {
                return constant(nodeType == ExpressionType.Equal);
            }
            // true [op] false
            if (ConstantCheck.isTrue(left)) {
                return constant(nodeType == ExpressionType.NotEqual);
            }
            // expr [op] false
            if (TypeUtils.getUnderlyingPrimitiveOrSelf(left.getType()) == PrimitiveTypes.Boolean) {
                return nodeType == ExpressionType.Equal ? isFalse(left) : left;
            }
            return null;
        }

        if (ConstantCheck.isTrue(left)) {
            // true [op] expr
            if (TypeUtils.getUnderlyingPrimitiveOrSelf(right.getType()) == PrimitiveTypes.Boolean) {
                return nodeType == ExpressionType.Equal ? right : isFalse(right);
            }
        }
        else if (ConstantCheck.isFalse(left)) {
            // false [op] expr
            if (TypeUtils.getUnderlyingPrimitiveOrSelf(right.getType()) == PrimitiveTypes.Boolean) {
                return nodeType == ExpressionType.NotEqual ? right : isFalse(right);
            }
        }

        return null;
    }
}
