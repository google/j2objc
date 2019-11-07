/*
 * RuntimeVariablesExpression.java
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
public final class RuntimeVariablesExpression extends Expression {
    private final ParameterExpressionList _variables;

    RuntimeVariablesExpression(final ParameterExpressionList variables) {
        _variables = variables;
    }

    public final ParameterExpressionList getVariables() {
        return _variables;
    }

    @Override
    public final Type<?> getType() {
        return Type.of(IRuntimeVariables.class);
    }

    @Override
    public final ExpressionType getNodeType() {
        return ExpressionType.RuntimeVariables;
    }

    @Override
    protected Expression accept(final ExpressionVisitor visitor) {
        return visitor.visitRuntimeVariables(this);
    }

    public final RuntimeVariablesExpression update(final ParameterExpressionList variables) {
        if (variables == getVariables()) {
            return this;
        }
        return runtimeVariables(variables);
    }
}
