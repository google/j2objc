/*
 * ForExpression.java
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

public final class ForExpression extends Expression {
    private final ParameterExpression _variable;
    private final Expression _initializer;
    private final Expression _test;
    private final Expression _step;
    private final Expression _body;

    private final LabelTarget _breakTarget;
    private final LabelTarget _continueTarget;

    ForExpression(
        final ParameterExpression variable,
        final Expression initializer,
        final Expression test,
        final Expression step,
        final Expression body,
        final LabelTarget breakTarget,
        final LabelTarget continueTarget) {

        _variable = variable;
        _initializer = initializer;
        _test = test;
        _step = step;
        _body = body;
        _breakTarget = breakTarget;
        _continueTarget = continueTarget;
    }

    public ParameterExpression getVariable() {
        return _variable;
    }

    public Expression getInitializer() {
        return _initializer;
    }

    public Expression getTest() {
        return _test;
    }

    public Expression getStep() {
        return _step;
    }

    public Expression getBody() {
        return _body;
    }

    public LabelTarget getBreakTarget() {
        return _breakTarget;
    }

    public LabelTarget getContinueTarget() {
        return _continueTarget;
    }

    @Override
    public ExpressionType getNodeType() {
        return ExpressionType.Extension;
    }

    @Override
    public Type<?> getType() {
        if (_breakTarget != null) {
            return _breakTarget.getType();
        }
        return PrimitiveTypes.Void;
    }

    @Override
    public boolean canReduce() {
        return true;
    }

    public ForExpression update(
        final ParameterExpression variable,
        final Expression initializer,
        final Expression test,
        final Expression step,
        final Expression body,
        final LabelTarget breakTarget,
        final LabelTarget continueTarget) {

        if (variable == _variable &&
            initializer == _initializer &&
            body == _body &&
            test == _test &&
            step == _step &&
            breakTarget == _breakTarget &&
            continueTarget == _continueTarget) {

            return this;
        }

        return makeFor(
            variable,
            initializer,
            test,
            step,
            body,
            breakTarget,
            continueTarget
        );
    }

    @Override
    protected Expression accept(final ExpressionVisitor visitor) {
        return visitor.visitFor(this);
    }

    @Override
    protected Expression visitChildren(final ExpressionVisitor visitor) {
        return update(
            (ParameterExpression)visitor.visit(_variable),
            visitor.visit(_initializer),
            visitor.visit(_test),
            visitor.visit(_step),
            visitor.visit(_body),
            visitor.visitLabelTarget(_breakTarget),
            visitor.visitLabelTarget(_continueTarget)
        );
    }

    @Override
    public Expression reduce() {
        final LabelTarget breakTarget = _breakTarget != null ? _breakTarget : label();
        final LabelTarget continueTarget = _continueTarget != null ? _continueTarget : label();

        return block(
            new ParameterExpressionList(_variable),
            assign(_variable, _initializer),
            loop(
                block(
                    ifThen(not(_test), makeBreak(breakTarget)),
                    _body,
                    label(continueTarget),
                    _step
                )
            ),
            label(breakTarget)
        );
    }
}
