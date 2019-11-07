/*
 * ParameterExpression.java
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
import com.strobel.reflection.Types;

import java.util.Date;

/**
 * Represents a named parameter expression.
 *
 * @author Mike Strobel
 */
public class ParameterExpression extends Expression {
    private final String _name;

    ParameterExpression(final String name) {
        _name = name;
    }

    public final String getName() {
        return _name;
    }

    @Override
    public Type<?> getType() {
        return Types.Object;
    }

    @Override
    public final ExpressionType getNodeType() {
        return ExpressionType.Parameter;
    }

    @Override
    protected Expression accept(final ExpressionVisitor visitor) {
        return visitor.visitParameter(this);
    }

    static ParameterExpression make(final Type type, final String name) {
        if (!type.isEnum()) {
            if (type.isPrimitive()) {
                if (type == PrimitiveTypes.Boolean) {
                    return new PrimitiveParameterExpression(PrimitiveTypes.Boolean, name);
                }
                if (type == PrimitiveTypes.Byte) {
                    return new PrimitiveParameterExpression(PrimitiveTypes.Byte, name);
                }
                if (type == PrimitiveTypes.Character) {
                    return new PrimitiveParameterExpression(PrimitiveTypes.Character, name);
                }
                if (type == PrimitiveTypes.Float) {
                    return new PrimitiveParameterExpression(PrimitiveTypes.Float, name);
                }
                if (type == PrimitiveTypes.Double) {
                    return new PrimitiveParameterExpression(PrimitiveTypes.Double, name);
                }
                if (type == PrimitiveTypes.Short) {
                    return new PrimitiveParameterExpression(PrimitiveTypes.Short, name);
                }
                if (type == PrimitiveTypes.Integer) {
                    return new PrimitiveParameterExpression(PrimitiveTypes.Integer, name);
                }
                if (type == PrimitiveTypes.Long) {
                    return new PrimitiveParameterExpression(PrimitiveTypes.Long, name);
                }
                if (type == PrimitiveTypes.Float) {
                    return new PrimitiveParameterExpression(PrimitiveTypes.Float, name);
                }
            }
            else if (type == Types.String) {
                return new PrimitiveParameterExpression(Type.of(String.class), name);
            }
            else if (type == Types.Date) {
                return new PrimitiveParameterExpression(Type.of(Date.class), name);
            }
            else if (type == Types.Object) {
                return new PrimitiveParameterExpression(Types.Object, name);
            }
            else if (type.isArray() && type.getElementType() == Types.Object) {
                return new PrimitiveParameterExpression(type, name);
            }
            else if (Type.of(Throwable.class).isAssignableFrom(type)) {
                return new PrimitiveParameterExpression(type, name);
            }
        }

        return new TypedParameterExpression(type, name);
    }
}

final class TypedParameterExpression extends ParameterExpression {
    private final Type _type;

    TypedParameterExpression(final Type type, final String name) {
        super(name);
        _type = type;
    }

    @Override
    public final Type<?> getType() {
        return _type;
    }
}

final class PrimitiveParameterExpression extends ParameterExpression {
    private final Type _type;

    PrimitiveParameterExpression(final Type type, final String name) {
        super(name);
        _type = type;
    }

    @Override
    public final Type<?> getType() {
        return _type;
    }
}
