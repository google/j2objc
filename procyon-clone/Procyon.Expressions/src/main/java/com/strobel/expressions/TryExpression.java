/*
 * TryExpression.java
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

import com.strobel.core.ReadOnlyList;
import com.strobel.reflection.Type;

/**
 * @author Mike Strobel
 */
public final class TryExpression extends Expression {
    private final Type _type;
    private final Expression _body;
    private final ReadOnlyList<CatchBlock> _handlers;
    private final Expression _finallyBlock;

    TryExpression(
        final Type type,
        final Expression body,
        final ReadOnlyList<CatchBlock> handlers,
        final Expression finallyBlock) {

        _type = type;
        _body = body;
        _handlers = handlers;
        _finallyBlock = finallyBlock;
    }

    public final Expression getBody() {
        return _body;
    }

    public final ReadOnlyList<CatchBlock> getHandlers() {
        return _handlers;
    }

    public final Expression getFinallyBlock() {
        return _finallyBlock;
    }

    @Override
    public final ExpressionType getNodeType() {
        return ExpressionType.Try;
    }

    @Override
    public final Type<?> getType() {
        return _type;
    }

    @Override
    protected final Expression accept(final ExpressionVisitor visitor) {
        return visitor.visitTry(this);
    }

    public final TryExpression update(
        final Expression body,
        final ReadOnlyList<CatchBlock> handlers,
        final Expression finallyBlock) {

        if (body == _body && handlers == _handlers && finallyBlock == _finallyBlock) {
            return this;
        }

        return makeTry(getType(), body, handlers, finallyBlock);
    }
}
