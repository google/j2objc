/*
 * SelfExpression.java
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
final class SelfExpression extends ParameterExpression {
    private final Type<?> _type;

    SelfExpression(final Type<?> type) {
        super("this");
        _type = type;
    }

    @Override
    public Type<?> getType() {
        return _type;
    }
}

/**
 * @author Mike Strobel
 */
final class SuperExpression extends ParameterExpression {
    private final Type<?> _type;

    SuperExpression(final Type<?> type) {
        super("super");
        _type = type;
    }

    @Override
    public Type<?> getType() {
        return _type;
    }
}
