/*
 * CatchBlock.java
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
public final class CatchBlock {
    private final Type _test;
    private final ParameterExpression _variable;
    private final Expression _body;
    private final Expression _filter;

    CatchBlock(final Type test, final ParameterExpression variable, final Expression body, final Expression filter) {
        _test = test;
        _variable = variable;
        _body = body;
        _filter = filter;
    }

    public final Type getTest() {
        return _test;
    }

    public final ParameterExpression getVariable() {
        return _variable;
    }

    public final Expression getBody() {
        return _body;
    }

    public final Expression getFilter() {
        return _filter;
    }

    @Override
    public final String toString() {
        return ExpressionStringBuilder.catchBlockToString(this);
    }

    public final CatchBlock update(final ParameterExpression variable, final Expression filter, final Expression body) {
        if (variable == _variable && filter == _filter && body == _body) {
            return this;
        }
        return Expression.makeCatch(_test, variable, body, filter);
    }
}
