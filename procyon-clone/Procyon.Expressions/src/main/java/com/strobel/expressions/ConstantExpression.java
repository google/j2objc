/*
 * ConstantExpression.java
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

import com.strobel.reflection.Type;
import com.strobel.reflection.Types;
import com.strobel.util.TypeUtils;

/**
 * Represents an expression that has a constant value.
 *
 * @author Mike Strobel
 */
public class ConstantExpression extends Expression {
    private final Object _value;

    ConstantExpression(final Object value) {
        _value = value;
    }

    public final Object getValue() {
        return _value;
    }

    @Override
    public Type<?> getType() {
        if (_value == null) {
            return Types.Object;
        }
        return Type.getType(_value);
    }

    @Override
    public ExpressionType getNodeType() {
        return ExpressionType.Constant;
    }

    @Override
    protected Expression accept(final ExpressionVisitor visitor) {
        return visitor.visitConstant(this);
    }

    static ConstantExpression make(final Object value, final Type type) {
        if (type.isPrimitive()) {
            return new PrimitiveConstantExpression(value);
        }

        if (value == null && type == Types.Object ||
            value != null && Type.of(value.getClass()) == type) {

            return new ConstantExpression(value);
        }

        return new TypedConstantExpression(value, type);
    }
}

class PrimitiveConstantExpression extends ConstantExpression {
    PrimitiveConstantExpression(final Object value) {
        super(value);
    }

    @Override
    public final Type<?> getType() {
        return TypeUtils.getUnderlyingPrimitiveOrSelf(super.getType());
    }
}

class TypedConstantExpression extends ConstantExpression {
    private final Type _type;

    TypedConstantExpression(final Object value, final Type type) {
        super(value);
        _type = type;
    }

    @Override
    public final Type<?> getType() {
        return _type;
    }
}