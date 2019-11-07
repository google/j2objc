/*
 * BlockExpression.java
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

import com.strobel.core.VerifyArgument;
import com.strobel.reflection.Type;
import com.strobel.util.ContractUtils;

/**
 * @author Mike Strobel
 */
public class BlockExpression extends Expression {

    public final ExpressionList<? extends Expression> getExpressions() {
        return getOrMakeExpressions();
    }

    public final ParameterExpressionList getVariables() {
        return getOrMakeVariables();
    }

    public final Expression getResult() {
        final int expressionCount = getExpressionCount();
        assert expressionCount > 0;
        return getExpression(expressionCount - 1);
    }

    @Override
    public final ExpressionType getNodeType() {
        return ExpressionType.Block;
    }

    @Override
    public Type<?> getType() {
        final int expressionCount = getExpressionCount();
        assert expressionCount > 0;
        return getExpression(expressionCount - 1).getType();
    }

    @Override
    protected Expression accept(final ExpressionVisitor visitor) {
        return visitor.visitBlock(this);
    }

    Expression getExpression(final int index) {
        throw ContractUtils.unreachable();
    }

    int getExpressionCount() {
        return 0;
    }

    ExpressionList<? extends Expression> getOrMakeExpressions() {
        throw ContractUtils.unreachable();
    }

    ParameterExpression getVariable(final int index) {
        throw ContractUtils.unreachable();
    }

    int getVariableCount() {
        return 0;
    }

    ParameterExpressionList getOrMakeVariables() {
        return ParameterExpressionList.empty();
    }

    BlockExpression rewrite(final ParameterExpressionList variables, final Expression[] args) {
        throw ContractUtils.unreachable();
    }

    @SuppressWarnings("unchecked")
    static ExpressionList<? extends Expression> returnReadOnlyExpressions(
        final BlockExpression provider,
        final Object expressionOrCollection) {

        if (expressionOrCollection instanceof Expression) {
            return new BlockExpressionList(provider, (Expression)expressionOrCollection);
        }
        // Return what is not guaranteed to be a readonly expressionOrCollection
        return (ExpressionList<? extends Expression>)expressionOrCollection;
    }
}

final class Block2 extends BlockExpression {
    private Object _arg0;
    private final Expression _arg1;

    Block2(final Expression arg0, final Expression arg1) {
        _arg0 = arg0;
        _arg1 = arg1;
    }

    @Override
    final Expression getExpression(final int index) {
        switch (index) {
            case 0:
                return returnObject(Expression.class, _arg0);
            case 1:
                return _arg1;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    final int getExpressionCount() {
        return 2;
    }

    @SuppressWarnings("unchecked")
    @Override
    final ExpressionList<? extends Expression> getOrMakeExpressions() {
        return (ExpressionList<? extends Expression>)(_arg0 = returnReadOnlyExpressions(this, _arg0));
    }

    @Override
    BlockExpression rewrite(final ParameterExpressionList variables, final Expression[] args) {
        assert args.length == 2;
        assert variables == null || variables.size() == 0;

        return new Block2(args[0], args[1]);
    }
}

final class Block3 extends BlockExpression {
    private Object _arg0;
    private final Expression _arg1;
    private final Expression _arg2;

    Block3(final Expression arg0, final Expression arg1, final Expression arg2) {
        _arg0 = arg0;
        _arg1 = arg1;
        _arg2 = arg2;
    }

    @Override
    final Expression getExpression(final int index) {
        switch (index) {
            case 0:
                return returnObject(Expression.class, _arg0);
            case 1:
                return _arg1;
            case 2:
                return _arg2;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    final int getExpressionCount() {
        return 3;
    }

    @SuppressWarnings("unchecked")
    @Override
    final ExpressionList<? extends Expression> getOrMakeExpressions() {
        return (ExpressionList<? extends Expression>)(_arg0 = returnReadOnlyExpressions(this, _arg0));
    }

    @Override
    BlockExpression rewrite(final ParameterExpressionList variables, final Expression[] args) {
        assert args.length == 3;
        assert variables == null || variables.size() == 0;

        return new Block3(args[0], args[1], args[2]);
    }
}

final class Block4 extends BlockExpression {
    private Object _arg0;
    private final Expression _arg1;
    private final Expression _arg2;
    private final Expression _arg3;

    Block4(final Expression arg0, final Expression arg1, final Expression arg2, final Expression arg3) {
        _arg0 = arg0;
        _arg1 = arg1;
        _arg2 = arg2;
        _arg3 = arg3;
    }

    @Override
    final Expression getExpression(final int index) {
        switch (index) {
            case 0:
                return returnObject(Expression.class, _arg0);
            case 1:
                return _arg1;
            case 2:
                return _arg2;
            case 3:
                return _arg3;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    final int getExpressionCount() {
        return 4;
    }

    @SuppressWarnings("unchecked")
    @Override
    final ExpressionList<? extends Expression> getOrMakeExpressions() {
        return (ExpressionList<? extends Expression>)(_arg0 = returnReadOnlyExpressions(this, _arg0));
    }

    @Override
    BlockExpression rewrite(final ParameterExpressionList variables, final Expression[] args) {
        assert args.length == 4;
        assert variables == null || variables.size() == 0;

        return new Block4(args[0], args[1], args[2], args[3]);
    }
}

final class Block5 extends BlockExpression {
    private Object _arg0;
    private final Expression _arg1;
    private final Expression _arg2;
    private final Expression _arg3;
    private final Expression _arg4;

    Block5(final Expression arg0, final Expression arg1, final Expression arg2, final Expression arg3, final Expression arg4) {
        _arg0 = arg0;
        _arg1 = arg1;
        _arg2 = arg2;
        _arg3 = arg3;
        _arg4 = arg4;
    }

    @Override
    final Expression getExpression(final int index) {
        switch (index) {
            case 0:
                return returnObject(Expression.class, _arg0);
            case 1:
                return _arg1;
            case 2:
                return _arg2;
            case 3:
                return _arg3;
            case 4:
                return _arg4;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    final int getExpressionCount() {
        return 5;
    }

    @SuppressWarnings("unchecked")
    @Override
    final ExpressionList<? extends Expression> getOrMakeExpressions() {
        return (ExpressionList<? extends Expression>)(_arg0 = returnReadOnlyExpressions(this, _arg0));
    }

    @Override
    BlockExpression rewrite(final ParameterExpressionList variables, final Expression[] args) {
        assert args.length == 5;
        assert variables == null || variables.size() == 0;

        return new Block5(args[0], args[1], args[2], args[3], args[4]);
    }
}

class BlockN extends BlockExpression {
    private final ExpressionList<? extends Expression> _expressions;

    BlockN(final ExpressionList<? extends Expression> expressions) {
        VerifyArgument.notEmpty(expressions, "expressions");
        _expressions = expressions;
    }

    @Override
    final int getExpressionCount() {
        return _expressions.size();
    }

    @Override
    final Expression getExpression(final int index) {
        return _expressions.get(index);
    }

    @Override
    final ExpressionList<? extends Expression> getOrMakeExpressions() {
        return _expressions;
    }

    @Override
    BlockExpression rewrite(final ParameterExpressionList variables, final Expression[] args) {
        assert variables == null || variables.size() == 0;

        return new BlockN(arrayToList(args));
    }
}

class ScopeExpression extends BlockExpression {
    private final ParameterExpressionList _variables;

    ScopeExpression(final ParameterExpressionList variables) {
        _variables = variables;
    }

    @Override
    final ParameterExpression getVariable(final int index) {
        return _variables.get(index);
    }

    @Override
    final int getVariableCount() {
        return _variables.size();
    }

    @Override
    final ParameterExpressionList getOrMakeVariables() {
        return _variables;
    }

    // Used for rewrite of the nodes to either reuse existing set of variables if not rewritten.
    final ParameterExpressionList reuseOrValidateVariables(final ParameterExpressionList variables) {
        if (variables != null && variables != getVariables()) {
            // Need to validate the new variables (i.e. uniqueness)
            validateVariables(variables, "variables");
            return variables;
        }
        else {
            return getVariables();
        }
    }
}

final class Scope1 extends ScopeExpression {
    private Object _body;

    Scope1(final ParameterExpressionList variables, final Expression body) {
        super(variables);
        _body = body;
    }

    @Override
    final Expression getExpression(final int index) {
        return returnObject(Expression.class, _body);
    }

    @Override
    final int getExpressionCount() {
        return 1;
    }

    @SuppressWarnings("unchecked")
    @Override
    final ExpressionList<? extends Expression> getOrMakeExpressions() {
        return (ExpressionList<? extends Expression>)(_body = returnReadOnlyExpressions(this, _body));
    }

    final BlockExpression rewrite(final ParameterExpressionList variables, final Expression[] args) {
        assert args.length == 1;
        assert variables == null || variables.size() == getVariableCount();

        return new Scope1(reuseOrValidateVariables(variables), args[0]);
    }
}

class ScopeN extends ScopeExpression {
    private final ExpressionList<? extends Expression> _body;

    ScopeN(final ParameterExpressionList variables, final ExpressionList<? extends Expression> body) {
        super(variables);
        _body = body;
    }

    @Override
    final Expression getExpression(final int index) {
        return _body.get(index);
    }

    @Override
    final int getExpressionCount() {
        return _body.size();
    }

    @Override
    final ExpressionList<? extends Expression> getOrMakeExpressions() {
        return _body;
    }

    BlockExpression rewrite(final ParameterExpressionList variables, final Expression[] args) {
        assert args != null && args.length == getExpressionCount() || args == null && getExpressionCount() == 0;
        assert variables == null || variables.size() == getVariableCount();

        return new ScopeN(reuseOrValidateVariables(variables), new ExpressionList<>(args));
    }
}

final class ScopeWithType extends ScopeN {
    private final Type _type;

    ScopeWithType(final ParameterExpressionList variables, final ExpressionList<? extends Expression> expressions, final Type type) {
        super(variables, expressions);
        _type = type;
    }

    @Override
    public final Type<?> getType() {
        return _type;
    }

    final BlockExpression rewrite(final ParameterExpressionList variables, final Expression[] args) {
        assert args != null && args.length == getExpressionCount() || args == null && getExpressionCount() == 0;
        assert variables == null || variables.size() == getVariableCount();

        return new ScopeWithType(reuseOrValidateVariables(variables), new ExpressionList<>(args), _type);
    }
}

final class BlockExpressionList extends ExpressionList<Expression> {
    private final BlockExpression _block;
    private final Expression _arg0;

    BlockExpressionList(final BlockExpression block, final Expression arg0) {
        _block = block;
        _arg0 = arg0;
    }

    @Override
    public int size() {
        return _block.getExpressionCount();
    }

    @Override
    protected ExpressionList<Expression> newInstance(final Expression[] expressions) {
        throw ContractUtils.unsupported();
    }

    @Override
    public Expression get(final int index) {
        if (index == 0) {
            return _arg0;
        }
        return _block.getExpression(index);
    }

    @Override
    public int indexOf(final Expression item) {
        if (_arg0 == item)
            return 0;

        for (int i = 1, n = _block.getExpressionCount(); i < n; i++) {
            if (_block.getExpression(i) == item) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public ExpressionList<Expression> remove(final int index) {
        throw ContractUtils.unsupported();
    }

    @Override
    public int lastIndexOf(final Expression expression) {
        for (int i = _block.getExpressionCount() - 1; i >= 1; i--) {
            if (_block.getExpression(i) == expression) {
                return i;
            }
        }

        if (expression == _arg0) {
            return 0;
        }

        return -1;
    }

    @Override
    public ExpressionList<Expression> getRange(final int fromIndex, final int toIndex) {
        throw ContractUtils.unsupported();
    }

    @Override
    public ExpressionList<Expression> add(final int index, final Expression expression) {
        throw ContractUtils.unsupported();
    }

    @Override
    public ExpressionList<Expression> replace(final int index, final Expression expression) {
        throw ContractUtils.unsupported();
    }

    @Override
    public ExpressionList<Expression> retainAll(final ExpressionList<? extends Expression> c) {
        throw ContractUtils.unsupported();
    }

    @Override
    public ExpressionList<Expression> removeAll(final ExpressionList<? extends Expression> c) {
        throw ContractUtils.unsupported();
    }

    @Override
    public ExpressionList<Expression> addAll(final ExpressionList<Expression> c) {
        throw ContractUtils.unsupported();
    }

    @Override
    public ExpressionList<Expression> addAll(final int index, final ExpressionList<Expression> c) {
        throw ContractUtils.unsupported();
    }

    @Override
    public ExpressionList<Expression> remove(final Expression expression) {
        throw ContractUtils.unsupported();
    }

    @Override
    public ExpressionList<Expression> add(final Expression expression) {
        throw ContractUtils.unsupported();
    }
}