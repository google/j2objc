/*
 * GotoExpression.java
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
public final class GotoExpression extends Expression {
    private final GotoExpressionKind _kind;
    private final Expression _value;
    private final LabelTarget _target;
    private final Type _type;

    GotoExpression(final GotoExpressionKind kind, final LabelTarget target, final Expression value, final Type type) {
        _kind = kind;
        _target = target;
        _value = value;
        _type = type;
    }

    public final GotoExpressionKind getKind() {
        return _kind;
    }

    public final Expression getValue() {
        return _value;
    }

    public final LabelTarget getTarget() {
        return _target;
    }

    @Override
    public final ExpressionType getNodeType() {
        return ExpressionType.Goto;
    }

    @Override
    public final Type<?> getType() {
        return _type;
    }

    @Override
    protected final Expression accept(final ExpressionVisitor visitor) {
        return visitor.visitGoto(this);
    }

    public final GotoExpression update(final LabelTarget target, final Expression value) {
        if (target == _target && value == _value) {
            return this;
        }
        return makeGoto(_kind, target, value, _type);
    }
}
