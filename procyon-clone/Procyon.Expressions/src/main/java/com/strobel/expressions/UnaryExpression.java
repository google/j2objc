/*
 * UnaryExpression.java
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

import com.strobel.reflection.MethodInfo;
import com.strobel.reflection.Type;

/**
 * Represents an expression that has a unary operator.
 *
 * @author Mike Strobel
 */
public final class UnaryExpression extends Expression {
    private final Expression _operand;
    private final MethodInfo _method;
    private final ExpressionType _nodeType;
    private final Type _type;

    UnaryExpression(final ExpressionType nodeType, final Expression operand, final Type type, final MethodInfo method) {
        _nodeType = nodeType;
        _operand = operand;
        _type = type;
        _method = method;
    }

    /**
     * Gets the implementing method for the unary operation.
     *
     * @return an {@link Expression} that represents the operand of the unary operation.
     */
    public final Expression getOperand() {
        return _operand;
    }

    /**
     * Gets the implementing method for the unary operation.
     *
     * @return the {@link MethodInfo} that represents the implementing method.
     */
    public final MethodInfo getMethod() {
        return _method;
    }

    @Override
    public final Type<?> getType() {
        return _type;
    }

    @Override
    public final ExpressionType getNodeType() {
        return _nodeType;
    }

    @Override
    protected final Expression accept(final ExpressionVisitor visitor) {
        return visitor.visitUnary(this);
    }

    @Override
    public final boolean canReduce() {
        switch (_nodeType) {
            case UnaryPlus:
                return _method == null;

            case PreIncrementAssign:
            case PreDecrementAssign:
            case PostIncrementAssign:
            case PostDecrementAssign:
                return true;
        }
        return false;
    }

    @Override
    public final Expression reduce() {
        if (canReduce()) {
            if (_nodeType == ExpressionType.UnaryPlus) {
                return _operand;
            }

            switch (_operand.getNodeType()) {
                case MemberAccess:
                    return reduceMember();
                default:
                    return reduceVariable();
            }
        }
        return this;
    }

    private Expression reduceVariable() {
        if (isPrefix()) {
            // (op) var => var = op(var)
            return assign(_operand, functionalOp(_operand));
        }

        // var (op) => temp = var; var = op(var); temp

        final ParameterExpression temp = parameter(_operand.getType());

        return block(
            _type,
            new ParameterExpression[] { temp },
            assign(temp, _operand),
            assign(_operand, functionalOp(temp)),
            temp
        );
    }

    private Expression reduceMember() {
        MemberExpression member = (MemberExpression) _operand;
        if (member.getTarget() == null) {
            // Static member; reduce the same as variable.
            return reduceVariable();
        }
        else {
            final ParameterExpression temp1 = parameter(member.getTarget().getType(), null);
            final Expression initTemp1 = assign(temp1, member.getTarget());

            member = makeMemberAccess(temp1, member.getMember());

            if (isPrefix()) {
                // (op) value.member => temp1 = value; temp1.member = op(temp1.member)
                return block(
                    new ParameterExpression[] { temp1 },
                    initTemp1,
                    assign(member, functionalOp(member))
                );
            }

            // value.member (op) => temp1 = value; temp2 = temp1.member; temp1.member = op(temp2); temp2

            final ParameterExpression temp2 = parameter(member.getType(), null);

            return block(
                new ParameterExpression[] { temp1, temp2 },
                initTemp1,
                assign(temp2, member),
                assign(member, functionalOp(temp2)),
                temp2
            );
        }
    }

    private boolean isPrefix() {
        return _nodeType == ExpressionType.PreIncrementAssign || _nodeType == ExpressionType.PreDecrementAssign;
    }

    private UnaryExpression functionalOp(final Expression operand) {
        final ExpressionType functional;
        if (_nodeType == ExpressionType.PreIncrementAssign || _nodeType == ExpressionType.PostIncrementAssign) {
            functional = ExpressionType.Increment;
        }
        else {
            functional = ExpressionType.Decrement;
        }
        return new UnaryExpression(functional, operand, operand.getType(), _method);
    }

    public final UnaryExpression update(final Expression operand) {
        if (operand == _operand) {
            return this;
        }
        return makeUnary(_nodeType, operand, _type, _method);
    }
}
