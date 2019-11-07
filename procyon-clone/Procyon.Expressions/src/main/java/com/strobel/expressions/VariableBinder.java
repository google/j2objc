/*
 * VariableBinder.java
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

import com.strobel.core.MutableInteger;
import com.strobel.reflection.emit.CodeGenerator;
import com.strobel.util.ContractUtils;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Stack;

/**
 * @author strobelm
 */
final class VariableBinder extends ExpressionVisitor {
    private final AnalyzedTree _tree = new AnalyzedTree();
    private final Stack<CompilerScope> _scopes = new Stack<>();
    private final Stack<BoundConstants> _constants = new Stack<>();

    private boolean _inQuote;
    
    static AnalyzedTree bind(final LambdaExpression<?> lambda) {
        final VariableBinder binder = new VariableBinder();
        binder.visit(lambda);
        return binder._tree;
    }

    private VariableBinder() {}

    @Override
    protected Expression visitConstant(final ConstantExpression node) {
        // If we're in Quote, we can ignore constants completely.
        if (_inQuote) {
            return node;
        }

        // Constants that can be emitted into bytecode don't need to be stored on
        // the delegate.
        if (CodeGenerator.canEmitConstant(node.getValue(), node.getType())) {
            return node;
        }

        _constants.peek().addReference(node.getValue(), node.getType());

//        for (int i = _constants.size() - 1; i >= 0; i--) {
//            _constants.get(i).addReference(node.getValue(), node.getType());
//        }

        return node;
    }

    @Override
    protected Expression visitUnary(final UnaryExpression node) {
        if (node.getNodeType() == ExpressionType.Quote) {
            final boolean wasInQuote = _inQuote;
            _inQuote = true;
            visit(node.getOperand());
            _inQuote = wasInQuote;
        }
        else {
            visit(node.getOperand());
        }
        
        return node;
    }

    @Override
    public <T> LambdaExpression<T> visitLambda(final LambdaExpression<T> node) {
        final CompilerScope scope = new CompilerScope(node, true);
        final BoundConstants constants = new BoundConstants();

        _tree.scopes.put(node, scope);
        _tree.constants.put(node, constants);

        _scopes.push(scope);
        _constants.push(constants);

        visit(mergeScopes(node));

        _constants.pop();
        _scopes.pop();

        return node;
    }

    @Override
    protected Expression visitInvocation(final InvocationExpression node) {
        final Expression e = node.getExpression();
        // Optimization: inline code for literal lambdas directly.
        if (e instanceof LambdaExpression) {
            final LambdaExpression lambda = (LambdaExpression) e;
            final CompilerScope scope = new CompilerScope(lambda, false);

            // Visit the lambda, but treat it more like a scope.
            _tree.scopes.put(lambda, scope);
            _scopes.push(scope);

            visit(mergeScopes(lambda));

            _scopes.pop();

            // Visit the invocation arguments.
            visit(node.getArguments());

            return node;
        }

        return super.visitInvocation(node);
    }

    @Override
    protected Expression visitBlock(final BlockExpression node) {
        if (node.getVariables().isEmpty()) {
            visit(node.getExpressions());
            return node;
        }

        final CompilerScope scope = new CompilerScope(node, false);

        _tree.scopes.put(node, scope);
        _scopes.push(scope);

        visit(mergeScopes(node));

        _scopes.pop();
        return node;
    }

    @Override
    protected CatchBlock visitCatchBlock(final CatchBlock node) {
        if (node.getVariable() == null) {
           visit(node.getBody());
            return node;
        }
        
        final CompilerScope scope = new CompilerScope(node, false);

        _tree.scopes.put(node, scope);
        _scopes.push(scope);
        
        visit(node.getBody());
        
        _scopes.pop();
        
        return node;
    }

    @Override
    protected Expression visitParameter(final ParameterExpression node) {
        if (node instanceof SelfExpression || node instanceof SuperExpression) {
            return node;
        }

        reference(node, VariableStorageKind.Local);

        //
        // Track reference count so we can emit it in a more optimal way if
        // it is used a lot.
        //
        CompilerScope referenceScope = null;

        for (int i = _scopes.size() - 1; i >= 0; i--) {
            final CompilerScope scope = _scopes.get(i);
            //
            // There are two times we care about references:
            //
            //   1. When we enter a lambda, we want to cache frequently
            //      used variables
            //
            //   2. When we enter a scope with closed-over variables, we
            //      want to cache it immediately when we allocate the
            //      closure slot for it
            //
            if (scope.isMethod || scope.definitions.containsKey(node)) {
                referenceScope = scope;
                break;
            }
        }

        assert referenceScope != null
            : "referenceScope != null";

        incrementReferenceCount(node, referenceScope);

        return node;
    }

    private void incrementReferenceCount(final ParameterExpression node, final CompilerScope scope) {
        if (scope.referenceCount == null) {
            scope.referenceCount = new LinkedHashMap<>();
        }

        final MutableInteger refCount = scope.referenceCount.get(node);

        if (refCount == null) {
            scope.referenceCount.put(node, new MutableInteger(1));
        }
        else {
            refCount.increment();
        }
    }

    @Override
    protected Expression visitRuntimeVariables(final RuntimeVariablesExpression node) {
        for (final ParameterExpression v : node.getVariables()) {
            reference(v, VariableStorageKind.Hoisted);
        }
        return node;
    }

    private ExpressionList<? extends Expression> mergeScopes(final Expression node) {
        ExpressionList<? extends Expression> body;

        if (node instanceof LambdaExpression<?>) {
            body = new ExpressionList<>(((LambdaExpression<?>)node).getBody());
        }
        else {
            body = ((BlockExpression)node).getExpressions();
        }

        final CompilerScope currentScope = _scopes.peek();

        // A block body is mergeable if the body only contains one single block
        // node containing variables and the child block has the same type as the
        // parent block. 
        while (body.size() == 1 &&
               body.get(0).getNodeType() == ExpressionType.Block) {

            final BlockExpression block = (BlockExpression)body.get(0);
            final ParameterExpressionList blockVariables = block.getVariables();

            if (!blockVariables.isEmpty()) {
                // Make sure none of the variables are shadowed.  If any are,
                // we can't merge it. 
                for (final ParameterExpression v : blockVariables) {
                    if (currentScope.definitions.containsKey(v)) {
                        return body;
                    }
                }

                // Otherwise, merge it .
                if (currentScope.mergedScopes == null) {
                    currentScope.mergedScopes = new LinkedHashSet<>();
                }

                currentScope.mergedScopes.add(block);

                for (final ParameterExpression v : blockVariables) {
                    currentScope.definitions.put(v, VariableStorageKind.Local);
                }
            }

            body = block.getExpressions();
        }

        return body;
    }

    private void reference(final ParameterExpression node, final VariableStorageKind storage) {
        CompilerScope definition = null;

        VariableStorageKind storageKind = storage;

        for (int i = _scopes.size() - 1; i >= 0; i--) {
            final CompilerScope scope = _scopes.get(i);

            if (scope.definitions.containsKey(node)) {
                definition = scope;
                break;
            }

            scope.needsClosure = true;

            if (scope.isMethod) {
                storageKind = VariableStorageKind.Hoisted;
            }
        }

        if (definition == null) {
            throw Error.undefinedVariable(node.getName(), node.getType(), getCurrentLambdaName());
        }

        if (storageKind == VariableStorageKind.Hoisted) {
            definition.definitions.put(node, VariableStorageKind.Hoisted);
        }
    }

    private String getCurrentLambdaName() {
        for (int i = _scopes.size() - 1; i >= 0; i--) {
            final CompilerScope scope = _scopes.get(i);
            if (scope.node instanceof LambdaExpression<?>) {
                return ((LambdaExpression)scope.node).getName();
            }
        }
        throw ContractUtils.unreachable();
    }
}
