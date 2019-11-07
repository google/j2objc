/*
 * ConditionalExpression.java
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
import com.strobel.util.TypeUtils;

/**
 * @author Mike Strobel
 */
public class ConditionalExpression extends Expression {
    private final Expression _test;
    private final Expression _ifTrue;

    ConditionalExpression(final Expression test, final Expression ifTrue) {
        _test = test;
        _ifTrue = ifTrue;
    }

    public final Expression getTest() {
        return _test;
    }

    public final Expression getIfTrue() {
        return _ifTrue;
    }

    public Expression getIfFalse() {
        return Expression.empty();
    }

    @Override
    public Type<?> getType() {
        return _ifTrue.getType();
    }

    @Override
    public ExpressionType getNodeType() {
        return ExpressionType.Conditional;
    }

    @Override
    protected Expression accept(final ExpressionVisitor visitor) {
        return visitor.visitConditional(this);
    }

    static ConditionalExpression make(
        final Expression test,
        final Expression ifTrue,
        final Expression ifFalse,
        final Type type) {

        if (!TypeUtils.areEquivalent(ifTrue.getType(), type) || !TypeUtils.areEquivalent(ifFalse.getType(), type)) {
            return new FullConditionalExpressionWithType(test, ifTrue, ifFalse, type);
        }
        if (ifFalse instanceof DefaultValueExpression && ifFalse.getType() == PrimitiveTypes.Void) {
            return new ConditionalExpression(test, ifTrue);
        }
        return new FullConditionalExpression(test, ifTrue, ifFalse);
    }

    public ConditionalExpression update(Expression test, Expression ifTrue, Expression ifFalse) {
        if (test == getTest() && ifTrue == getIfTrue() && ifFalse == getIfFalse()) {
            return this;
        }
        return Expression.condition(test, ifTrue, ifFalse, getType());
    }
}

class FullConditionalExpression extends ConditionalExpression {
    private final Expression _ifFalse;

    FullConditionalExpression(final Expression test, final Expression ifTrue, final Expression ifFalse) {
        super(test, ifTrue);
        _ifFalse = ifFalse;
    }

    @Override
    public Expression getIfFalse() {
        return _ifFalse;
    }
}

class FullConditionalExpressionWithType extends FullConditionalExpression {
    private final Type _type;

    FullConditionalExpressionWithType(
        final Expression test,
        final Expression ifTrue,
        final Expression ifFalse,
        final Type type) {

        super(test, ifTrue, ifFalse);
        _type = type;
    }

    @Override
    public Type<?> getType() {
        return _type;
    }
}
