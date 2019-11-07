/*
 * LoopExpression.java
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

/**
 * @author Mike Strobel
 */
public final class LoopExpression extends Expression {
    private final Expression _body;
    private final LabelTarget _breakTarget;
    private final LabelTarget _continueTarget;

    LoopExpression(final Expression body, final LabelTarget breakTarget, final LabelTarget continueTarget) {
        _body = body;
        _breakTarget = breakTarget;
        _continueTarget = continueTarget;
    }

    public final Expression getBody() {
        return _body;
    }

    public final LabelTarget getBreakTarget() {
        return _breakTarget;
    }

    public final LabelTarget getContinueTarget() {
        return _continueTarget;
    }

    @Override
    public final ExpressionType getNodeType() {
        return ExpressionType.Loop;
    }

    @Override
    public final Type<?> getType() {
        return _breakTarget == null ? PrimitiveTypes.Void : _breakTarget.getType();
    }

    @Override
    protected final Expression accept(final ExpressionVisitor visitor) {
        return visitor.visitLoop(this);
    }

    public final LoopExpression update(final LabelTarget breakLabel, final LabelTarget continueLabel, final Expression body) {
        if (breakLabel == _breakTarget && continueLabel == _continueTarget && body == _body) {
            return this;
        }
        return loop(body, breakLabel, continueLabel);
    }
}
