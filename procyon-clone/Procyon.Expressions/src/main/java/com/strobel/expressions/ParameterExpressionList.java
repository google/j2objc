/*
 * ParameterExpressionList.java
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
import com.strobel.reflection.TypeList;

/**
 * @author Mike Strobel
 */
public class ParameterExpressionList extends ExpressionList<ParameterExpression> {

    private final static ParameterExpressionList EMPTY = new ParameterExpressionList();

    @SuppressWarnings("unchecked")
    public static ParameterExpressionList empty() {
        return EMPTY;
    }

    public ParameterExpressionList(final ParameterExpression... expressions) {
        super(expressions);
    }

    @Override
    protected ParameterExpressionList newInstance(final ParameterExpression[] expressions) {
        return new ParameterExpressionList(expressions);
    }

    @Override
    public ParameterExpressionList add(final ParameterExpression expression) {
        return (ParameterExpressionList)super.add(expression);
    }

    @Override
    public ParameterExpressionList remove(final ParameterExpression expression) {
        return (ParameterExpressionList)super.remove(expression);
    }

    @Override
    public ParameterExpressionList addAll(final int index, final ExpressionList<ParameterExpression> c) {
        return (ParameterExpressionList)super.addAll(index, c);
    }

    public ParameterExpressionList removeAll(final ParameterExpressionList c) {
        return (ParameterExpressionList)super.removeAll(c);
    }

    public ParameterExpressionList retainAll(final ParameterExpressionList c) {
        return (ParameterExpressionList)super.retainAll(c);
    }

    @Override
    public ParameterExpressionList addAll(final ExpressionList<ParameterExpression> c) {
        return (ParameterExpressionList)super.addAll(c);
    }

    @Override
    public ParameterExpressionList replace(final int index, final ParameterExpression expression) {
        return (ParameterExpressionList)super.replace(index, expression);
    }

    @Override
    public ParameterExpressionList add(final int index, final ParameterExpression expression) {
        return (ParameterExpressionList)super.add(index, expression);
    }

    @Override
    public ParameterExpressionList remove(final int index) {
        return (ParameterExpressionList)super.remove(index);
    }

    @Override
    public ParameterExpressionList getRange(final int fromIndex, final int toIndex) {
        return (ParameterExpressionList)super.getRange(fromIndex, toIndex);
    }

    public TypeList getParameterTypes() {
        final int parameterCount = size();
        final Type<?>[] types = new Type<?>[parameterCount];

        for (int i = 0; i < parameterCount; i++) {
            types[i] = get(i).getType();
        }

        return Type.list(types);
    }
}
