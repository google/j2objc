/*
 * LabelExpression.java
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
public final class LabelExpression extends Expression {
    private final Expression _defaultValue;
    private final LabelTarget _target;

    public LabelExpression(final LabelTarget target, final Expression defaultValue) {
        _target = target;
        _defaultValue = defaultValue;
    }

    public final Expression getDefaultValue() {
        return _defaultValue;
    }

    public final LabelTarget getTarget() {
        return _target;
    }

    @Override
    public final ExpressionType getNodeType() {
        return ExpressionType.Label;
    }

    @Override
    public final Type<?> getType() {
        return _target.getType();
    }

    @Override
    protected final Expression accept(final ExpressionVisitor visitor) {
        return visitor.visitLabel(this);
    }

    public final LabelExpression update(final LabelTarget target, final Expression defaultValue) {
        if (target == _target && defaultValue == _defaultValue) {
            return this;
        }
        return label(target, defaultValue);
    }
}
