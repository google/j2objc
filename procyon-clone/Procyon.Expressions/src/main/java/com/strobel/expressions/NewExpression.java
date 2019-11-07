/*
 * NewExpression.java
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

import com.strobel.reflection.ConstructorInfo;
import com.strobel.reflection.Type;

/**
 * @author Mike Strobel
 */
public final class NewExpression extends Expression implements IArgumentProvider {
    private final ConstructorInfo _constructor;
    private final ExpressionList<? extends Expression> _arguments;

    NewExpression(final ConstructorInfo constructor, final ExpressionList<? extends Expression> arguments) {
        _constructor = constructor;
        _arguments = arguments;
    }

    public final ConstructorInfo getConstructor() {
        return _constructor;
    }

    public final ExpressionList<? extends Expression> getArguments() {
        return _arguments;
    }

    @Override
    public final int getArgumentCount() {
        return _arguments.size();
    }

    @Override
    public final Expression getArgument(final int index) {
        return _arguments.get(index);
    }

    @Override
    public final ExpressionType getNodeType() {
        return ExpressionType.New;
    }

    @Override
    public final Type<?> getType() {
        return _constructor.getDeclaringType();
    }

    @Override
    protected final Expression accept(final ExpressionVisitor visitor) {
        return visitor.visitNew(this);
    }

    public final NewExpression update(final ExpressionList<? extends Expression> arguments) {
        if (arguments == _arguments) {
            return this;
        }
        return makeNew(_constructor, arguments);
    }
}
