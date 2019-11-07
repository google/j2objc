/*
 * ForEachExpression.java
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
import com.strobel.reflection.Types;

/**
 * @author Mike Strobel
 */
public final class ForEachExpression extends Expression {
    private final ParameterExpression _variable;
    private final Expression _sequence;
    private final Expression _body;

    private final LabelTarget _breakTarget;
    private final LabelTarget _continueTarget;

    ForEachExpression(
        final ParameterExpression variable,
        final Expression sequence,
        final Expression body,
        final LabelTarget breakTarget,
        final LabelTarget continueTarget) {

        _variable = variable;
        _sequence = sequence;
        _body = body;
        _breakTarget = breakTarget;
        _continueTarget = continueTarget;
    }

    public ParameterExpression getVariable() {
        return _variable;
    }

    public Expression getSequence() {
        return _sequence;
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

    public ForEachExpression update(
        final ParameterExpression variable,
        final Expression sequence,
        final Expression body,
        final LabelTarget breakTarget,
        final LabelTarget continueTarget) {

        if (variable == _variable &&
            sequence == _sequence &&
            body == _body &&
            breakTarget == _breakTarget &&
            continueTarget == _continueTarget) {

            return this;
        }

        return forEach(
            variable,
            sequence,
            body,
            breakTarget,
            continueTarget
        );
    }

    @Override
    protected Expression accept(final ExpressionVisitor visitor) {
        return visitor.visitForEach(this);
    }

    @Override
    protected Expression visitChildren(final ExpressionVisitor visitor) {
        return update(
            (ParameterExpression)visitor.visit(_variable),
            visitor.visit(_sequence),
            visitor.visit(_body),
            visitor.visitLabelTarget(_breakTarget),
            visitor.visitLabelTarget(_continueTarget)
        );
    }

    @Override
    public Expression reduce() {
        if (_sequence.getType().isArray()) {
            return reduceForArray();
        }
        return reduceForIterable();
    }

    private Expression reduceForArray() {
        final ParameterExpression array = variable(_sequence.getType(), "$array");
        final ParameterExpression index = variable(PrimitiveTypes.Integer, "$i");
        final ParameterExpression length = variable(PrimitiveTypes.Integer, "$length");

        final ParameterExpressionList variables = new ParameterExpressionList(array, index, length);

        final LabelTarget breakTarget = _breakTarget != null ? _breakTarget : label();
        final LabelTarget continueTarget = _continueTarget != null ? _continueTarget : label("update");

        return block(
            variables,
            assign(array, _sequence),
            assign(length, arrayLength(array)),
            assign(index, constant(0)),
            loop(
                block(
                    new ParameterExpressionList(_variable),
                    ifThen(
                        not(lessThan(index, length)),
                        makeBreak(breakTarget)
                    ),
                    assign(_variable, convert(arrayIndex(array, index), _variable.getType())),
                    _body,
                    label(continueTarget),
                    preIncrementAssign(index)
                )
            ),
            label(breakTarget)
        );
    }

    private Expression reduceForIterable() {
        final Type iterableType;
        final Type iteratorType;
        final Type<?> argument = tryGetGenericEnumerableArgument();

        if (argument == null) {
            iterableType = Types.Iterable.getErasedType();
            iteratorType = Types.Iterator.getErasedType();
        }
        else {
            iterableType = Types.Iterable.makeGenericType(argument);
            iteratorType = Types.Iterator.makeGenericType(argument);
        }

        final ParameterExpression iterator = variable(iteratorType, "iterator");

        final LabelTarget innerContinueTarget = label();
        final LabelTarget innerBreakTarget = label();
        final LabelTarget breakTarget = _breakTarget != null ? _breakTarget : label();
        final LabelTarget continueTarget = _continueTarget != null ? _continueTarget : label("update");

        return block(
            new ParameterExpressionList(iterator),
            assign(
                iterator,
                call(
                    convert(_sequence, iterableType),
                    iterableType.getMethod("iterator")
                )
            ),
            block(
                new ParameterExpressionList(_variable),
                makeGoto(continueTarget),
                loop(
                    block(
                        assign(
                            _variable,
                            convert(
                                call(iterator, iteratorType.getMethod("next")),
                                _variable.getType()
                            )
                        ),
                        _body,
                        label(continueTarget),
                        condition(
                            call(iterator, iteratorType.getMethod("hasNext")),
                            makeContinue(innerContinueTarget),
                            makeBreak(innerBreakTarget)
                        )
                    ),
                    innerBreakTarget,
                    innerContinueTarget
                ),
                label(breakTarget)
            )
        );
    }

    private Type<?> tryGetGenericEnumerableArgument() {
        for (final Type ifType : _sequence.getType().getInterfaces()) {
            if (!ifType.isGenericType()) {
                continue;
            }

            if (ifType.getGenericTypeDefinition() != Types.Iterable) {
                continue;
            }

            final Type<?> typeArgument = ifType.getTypeArguments().get(0);

            if (_variable.getType().isAssignableFrom(typeArgument)) {
                return typeArgument;
            }
        }

        return null;
    }
}
