/*
 * BinaryExpression.java
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

import com.strobel.reflection.*;
import com.strobel.util.TypeUtils;

/**
 * @author Mike Strobel
 */
public class BinaryExpression extends Expression {
    private final Expression _left;
    private final Expression _right;

    BinaryExpression(final Expression left, final Expression right) {
        _left = left;
        _right = right;
    }

    public final Expression getRight() {
        return _right;
    }

    public final Expression getLeft() {
        return _left;
    }

    public MethodInfo getMethod() {
        return null;
    }

    public LambdaExpression<?> getConversion() {
        return null;
    }

    @Override
    public boolean canReduce() {
        return isOpAssignment(getNodeType());
    }

    @Override
    protected Expression accept(final ExpressionVisitor visitor) {
        return visitor.visitBinary(this);
    }

    @Override
    public Expression reduce() {
        // Only reduce OpAssignment expressions.
        if (isOpAssignment(getNodeType())) {
            switch (_left.getNodeType()) {
                case MemberAccess:
                    return reduceMember();
                default:
                    return reduceVariable();
            }
        }
        return this;
    }

    static Expression create(
        final ExpressionType nodeType,
        final Expression left,
        final Expression right,
        final Type type,
        final MethodInfo method,
        final LambdaExpression<?> conversion) {

        if (nodeType == ExpressionType.Assign) {
            assert method == null &&
                   TypeUtils.hasIdentityPrimitiveOrBoxingConversion(type, left.getType());

            return new AssignBinaryExpression(left, right);
        }

        if (conversion != null) {
            assert method == null &&
                   TypeUtils.hasIdentityPrimitiveOrBoxingConversion(type, right.getType()) &&
                   nodeType == ExpressionType.Coalesce;

            return new CoalesceConversionBinaryExpression(left, right, conversion);
        }

        if (method != null) {
            return new MethodBinaryExpression(nodeType, left, right, type, method);
        }

        if (TypeUtils.hasIdentityPrimitiveOrBoxingConversion(type, PrimitiveTypes.Boolean)) {
            return new LogicalBinaryExpression(nodeType, left, right);
        }

        return new SimpleBinaryExpression(nodeType, left, right, type);
    }

    public BinaryExpression update(final Expression left, final LambdaExpression<?> conversion, final Expression right) {
        if (left == getLeft() && right == getRight() && conversion == getConversion()) {
            return this;
        }
        if (isReferenceComparison()) {
            if (getNodeType() == ExpressionType.Equal) {
                return Expression.referenceEqual(left, right);
            }
            else {
                return Expression.referenceNotEqual(left, right);
            }
        }

        return Expression.makeBinary(getNodeType(), left, right, getMethod(), conversion);
    }

    boolean isReferenceComparison() {
        final Type left = _left.getType();
        final Type right = _right.getType();
        final MethodInfo method = getMethod();
        final ExpressionType kind = getNodeType();

        return (kind == ExpressionType.Equal || kind == ExpressionType.NotEqual) &&
               method == null &&
               !left.isPrimitive() &&
               !right.isPrimitive();
    }

    private Expression reduceVariable() {
        // v (op)= r => v = v (op) r
        final ExpressionType op = getBinaryOpFromAssignmentOp(getNodeType());

        Expression r = Expression.makeBinary(op, _left, _right, getMethod());

        final LambdaExpression<?> conversion = getConversion();

        if (conversion != null) {
            r = Expression.invoke(conversion, r);
        }

        return Expression.assign(_left, r);
    }

    private Expression reduceMember() {
        final MemberExpression member = (MemberExpression)_left;

        if (member.getTarget() == null) {
            // Static member; reduce the same as variable
            return reduceVariable();
        }
        else {
            // left.b (op)= r => temp1 = left; temp2 = temp1.b (op) r;  temp1.b = temp2; temp2
            final ParameterExpression temp1 = variable(member.getTarget().getType(), "temp1");

            // 1. temp1 = left
            final Expression temp1EqualsLeft = Expression.assign(temp1, member.getTarget());

            // 2. temp2 = temp1.b (op) r
            final ExpressionType op = getBinaryOpFromAssignmentOp(getNodeType());

            Expression temp2EqualsTemp1Member = Expression.makeBinary(
                op,
                Expression.makeMemberAccess(temp1, member.getMember()),
                _right,
                getMethod()
            );

            final LambdaExpression<?> conversion = getConversion();

            if (conversion != null) {
                temp2EqualsTemp1Member = Expression.invoke(conversion, temp2EqualsTemp1Member);
            }

            final ParameterExpression temp2 = variable(temp2EqualsTemp1Member.getType(), "temp2");
            temp2EqualsTemp1Member = Expression.assign(temp2, temp2EqualsTemp1Member);

            // 3. temp1.b = temp2

            final Expression temp1MemberEqualsTemp2 = Expression.assign(
                Expression.makeMemberAccess(temp1, member.getMember()),
                temp2
            );

            return Expression.block(
                new ParameterExpression[]{temp1, temp2},
                temp1EqualsLeft,
                temp2EqualsTemp1Member,
                temp1MemberEqualsTemp2,
                temp2
            );
        }
    }

    private static boolean isOpAssignment(final ExpressionType operation) {
        switch (operation) {
            case AddAssign:
            case SubtractAssign:
            case MultiplyAssign:
            case DivideAssign:
            case ModuloAssign:
            case AndAssign:
            case OrAssign:
            case RightShiftAssign:
            case LeftShiftAssign:
            case ExclusiveOrAssign:
                return true;
        }
        return false;
    }

    private static ExpressionType getBinaryOpFromAssignmentOp(final ExpressionType operator) {
        assert isOpAssignment(operator);

        switch (operator) {
            case AddAssign:
                return ExpressionType.Add;
            case SubtractAssign:
                return ExpressionType.Subtract;
            case MultiplyAssign:
                return ExpressionType.Multiply;
            case DivideAssign:
                return ExpressionType.Divide;
            case ModuloAssign:
                return ExpressionType.Modulo;
            case AndAssign:
                return ExpressionType.And;
            case OrAssign:
                return ExpressionType.Or;
            case RightShiftAssign:
                return ExpressionType.RightShift;
            case LeftShiftAssign:
                return ExpressionType.LeftShift;
            case ExclusiveOrAssign:
                return ExpressionType.ExclusiveOr;
            default:
                throw Error.invalidOperator(operator);
        }
    }
}

/**
 * Optimized representation of simple logical expressions:
 * {@code && || == != > < >= <=}
 */
class LogicalBinaryExpression extends BinaryExpression {
    private final ExpressionType _operator;

    public LogicalBinaryExpression(final ExpressionType operator, final Expression left, final Expression right) {
        super(left, right);
        _operator = operator;
    }

    @Override
    public final Type<?> getType() {
        return PrimitiveTypes.Boolean;
    }

    @Override
    public final ExpressionType getNodeType() {
        return _operator;
    }
}

final class CompareMethodBasedLogicalBinaryExpression extends BinaryExpression {
    private final ExpressionType _operator;
    private final MethodInfo _method;

    public CompareMethodBasedLogicalBinaryExpression(
        final ExpressionType operator,
        final Expression left,
        final Expression right,
        final MethodInfo method) {

        super(left, right);

        _operator = operator;
        _method = method;
    }

    @Override
    public final Type<?> getType() {
        return PrimitiveTypes.Boolean;
    }

    @Override
    public final ExpressionType getNodeType() {
        return _operator;
    }

    @Override
    public final MethodInfo getMethod() {
        return _method;
    }

    @Override
    public final Expression reduce() {
        if (canReduce()) {
            switch (_operator) {
                case GreaterThan:
                    return greaterThan(call(_method, getLeft(), getRight()), constant(0));
                case GreaterThanOrEqual:
                    return greaterThanOrEqual(call(_method, getLeft(), getRight()), constant(0));
                case LessThan:
                    return lessThan(call(_method, getLeft(), getRight()), constant(0));
                case LessThanOrEqual:
                    return lessThanOrEqual(call(_method, getLeft(), getRight()), constant(0));
            }
        }
        return this;
    }

    @Override
    public final boolean canReduce() {
        return _method.getReturnType() == PrimitiveTypes.Integer;
    }
}

final class EqualsMethodBasedLogicalBinaryExpression extends BinaryExpression {
    private final ExpressionType _operator;
    private final MethodInfo _method;

    public EqualsMethodBasedLogicalBinaryExpression(
        final ExpressionType operator,
        final Expression left,
        final Expression right,
        final MethodInfo method) {

        super(left, right);

        _operator = operator;

        if (method != null) {
            _method = method;
        }
        else {
            final MethodInfo equalsMethod = Types.Comparer.getMethod(
                operator == ExpressionType.Equal ? "equals" : "notEqual",
                BindingFlags.PublicStatic,
                Types.Object,
                Types.Object
            );

            if (TypeUtils.areEquivalent(left.getType(), right.getType())) {
                _method = equalsMethod.makeGenericMethod(left.getType());
            }
            else {
                _method = equalsMethod.makeGenericMethod(Types.Object);
            }
        }
    }

    @Override
    public final Type<?> getType() {
        return PrimitiveTypes.Boolean;
    }

    @Override
    public final ExpressionType getNodeType() {
        return _operator;
    }

    @Override
    public MethodInfo getMethod() {
        return _method;
    }
}

/**
 * Optimized assignment node; only holds onto children.
 */
final class AssignBinaryExpression extends BinaryExpression {
    public AssignBinaryExpression(final Expression left, final Expression right) {
        super(left, right);
    }

    @Override
    public final Type<?> getType() {
        return getLeft().getType();
    }

    @Override
    public final ExpressionType getNodeType() {
        return ExpressionType.Assign;
    }
}

/**
 * Coalesce with conversion.  This is not a frequently used node, but
 * rather we want to save every other BinaryExpression from holding onto the
 * null conversion lambda.
 */
final class CoalesceConversionBinaryExpression extends BinaryExpression {
    private final LambdaExpression<?> _conversion;

    CoalesceConversionBinaryExpression(final Expression left, final Expression right, final LambdaExpression<?> conversion) {
        super(left, right);
        _conversion = conversion;
    }

    @Override
    public final LambdaExpression<?> getConversion() {
        return _conversion;
    }

    @Override
    public final Type<?> getType() {
        return getRight().getType();
    }

    @Override
    public final ExpressionType getNodeType() {
        return ExpressionType.Coalesce;
    }
}

final class OpAssignMethodConversionBinaryExpression extends MethodBinaryExpression {
    private final LambdaExpression<?> _conversion;

    OpAssignMethodConversionBinaryExpression(
        final ExpressionType nodeType,
        final Expression left,
        final Expression right,
        final Type type,
        final MethodInfo method,
        final LambdaExpression<?> conversion) {

        super(nodeType, left, right, type, method);

        _conversion = conversion;
    }

    @Override
    public final LambdaExpression<?> getConversion() {
        return _conversion;
    }
}

class SimpleBinaryExpression extends BinaryExpression {
    private final ExpressionType _nodeType;
    private final Type _type;

    SimpleBinaryExpression(
        final ExpressionType nodeType,
        final Expression left,
        final Expression right,
        final Type type) {

        super(left, right);

        _nodeType = nodeType;
        _type = type;
    }

    @Override
    public final ExpressionType getNodeType() {
        return _nodeType;
    }

    @Override
    public final Type<?> getType() {
        return _type;
    }
}

class MethodBinaryExpression extends SimpleBinaryExpression {
    private final MethodInfo _method;

    public MethodBinaryExpression(
        final ExpressionType operator,
        final Expression left,
        final Expression right,
        final Type type,
        final MethodInfo method) {

        super(operator, left, right, type);
        _method = method;
    }

    @Override
    public final MethodInfo getMethod() {
        return _method;
    }
}

