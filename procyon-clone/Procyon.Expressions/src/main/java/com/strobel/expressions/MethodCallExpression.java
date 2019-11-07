/*
 * MethodCallExpression.java
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

import com.strobel.reflection.MethodInfo;
import com.strobel.reflection.Type;
import com.strobel.util.ContractUtils;

/**
 * @author Mike Strobel
 */
public class MethodCallExpression extends Expression implements IArgumentProvider {
    private final MethodInfo _method;

    MethodCallExpression(final MethodInfo method) {
        _method = method;
    }

    public final MethodInfo getMethod() {
        return _method;
    }

    public Expression getTarget() {
        return null;
    }

    @Override
    public ExpressionType getNodeType() {
        return ExpressionType.Call;
    }

    @Override
    public Type<?> getType() {
        return _method.getReturnType();
    }

    public final ExpressionList<? extends Expression> getArguments() {
        return getOrMakeArguments();
    }

    ExpressionList<? extends Expression> getOrMakeArguments() {
        throw ContractUtils.unreachable();
    }

    @Override
    public int getArgumentCount() {
        throw ContractUtils.unreachable();
    }

    @Override
    public Expression getArgument(final int index) {
        throw ContractUtils.unreachable();
    }

    MethodCallExpression rewrite(final Expression target, final ExpressionList<? extends Expression> arguments) {
        throw ContractUtils.unreachable();
    }

    public MethodCallExpression update(final Expression target, final ExpressionList<? extends Expression> arguments) {
        if (target == getTarget() && arguments == getArguments()) return this;
        return call(target, getMethod(), getArguments());
    }

    @Override
    protected Expression accept(final ExpressionVisitor visitor) {
        return visitor.visitMethodCall(this);
    }
}

final class MethodCallExpressionN extends MethodCallExpression {
    private final ExpressionList<? extends Expression> _arguments;

    MethodCallExpressionN(final MethodInfo method, final ExpressionList<? extends Expression> arguments) {
        super(method);
        _arguments = arguments;
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
    final ExpressionList<? extends Expression> getOrMakeArguments() {
        return _arguments;
    }

    @Override
    final MethodCallExpression rewrite(final Expression target, final ExpressionList<? extends Expression> arguments) {
        assert target == null;
        assert arguments == null || arguments.size() == _arguments.size();

        return call(getMethod(), arguments != null ? arguments : _arguments);
    }
}

final class InstanceMethodCallExpressionN extends MethodCallExpression {
    private final Expression _target;
    private final ExpressionList<? extends Expression> _arguments;

    InstanceMethodCallExpressionN(final MethodInfo method, final Expression target, final ExpressionList<? extends Expression> arguments) {
        super(method);
        _target = target;
        _arguments = arguments;
    }

    @Override
    public final Expression getTarget() {
        return _target;
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
    final ExpressionList<? extends Expression> getOrMakeArguments() {
        return _arguments;
    }

    @Override
    final MethodCallExpression rewrite(final Expression target, final ExpressionList<? extends Expression> arguments) {
        assert target != null;
        assert arguments == null || arguments.size() == _arguments.size();

        return call(target, getMethod(), arguments != null ? arguments : _arguments);
    }
}