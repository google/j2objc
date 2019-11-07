/*
 * DefaultValueExpression.java
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

/**
 * @author Mike Strobel
 */
public final class DefaultValueExpression extends Expression {
    private final Type _type;

    DefaultValueExpression(final Type type) {
        _type = type;
    }

    @Override
    public ExpressionType getNodeType() {
        return ExpressionType.DefaultValue;
    }

    @Override
    public Type<?> getType() {
        return _type;
    }

    @Override
    protected Expression accept(final ExpressionVisitor visitor) {
        return visitor.visitDefaultValue(this);
    }
}
