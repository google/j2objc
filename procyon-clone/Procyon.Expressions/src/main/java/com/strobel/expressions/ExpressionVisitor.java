/*
 * ExpressionVisitor.java
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

import com.strobel.core.ReadOnlyList;
import com.strobel.reflection.Type;

/**
 * @author Mike Strobel
 */
public abstract class ExpressionVisitor {

    public Expression visit(final Expression node) {
        if (node != null) {
            return node.accept(this);
        }
        return null;
    }

    protected Expression visitDefaultValue(final DefaultValueExpression node) {
        return node;
    }

    protected Expression visitExtension(final Expression node) {
        return node.visitChildren(this);
    }

    protected Expression visitLabel(final LabelExpression node) {
        return node.update(this.visitLabelTarget(node.getTarget()), visit(node.getDefaultValue()));
    }

    protected LabelTarget visitLabelTarget(final LabelTarget node) {
        return node;
    }

    protected Expression visitConcat(final ConcatExpression node) {
        return node.update(visit(node.getOperands()));
    }

    protected Expression visitGoto(final GotoExpression node) {
        return node.update(visitLabelTarget(node.getTarget()), visit(node.getValue()));
    }

    protected Expression visitLoop(final LoopExpression node) {
        return node.update(
            visitLabelTarget(node.getBreakTarget()),
            visitLabelTarget(node.getContinueTarget()),
            visit(node.getBody())
        );
    }

    protected Expression visitForEach(final ForEachExpression node) {
        return node.update(
            (ParameterExpression) visit(node.getVariable()),
            visit(node.getSequence()),
            visit(node.getBody()),
            visitLabelTarget(node.getBreakTarget()),
            visitLabelTarget(node.getContinueTarget())
        );
    }

    protected Expression visitFor(final ForExpression node) {
        return node.update(
            (ParameterExpression) visit(node.getVariable()),
            visit(node.getInitializer()),
            visit(node.getTest()),
            visit(node.getStep()),
            visit(node.getBody()),
            visitLabelTarget(node.getBreakTarget()),
            visitLabelTarget(node.getContinueTarget())
        );
    }

    protected Expression visitMember(final MemberExpression node) {
        return node.update(this.visit(node.getTarget()));
    }

    protected Expression visitConstant(final ConstantExpression node) {
        return node;
    }

    protected Expression visitParameter(final ParameterExpression node) {
        return node;
    }

    protected Expression visitUnary(final UnaryExpression node) {
        return validateUnary(node, node.update(visit(node.getOperand())));
    }

    protected Expression visitBinary(final BinaryExpression node) {
        // Walk children in evaluation order: left, conversion, right
        return validateBinary(
            node,
            node.update(
                visit(node.getLeft()),
                visitAndConvert(node.getConversion(), "visitBinary"),
                visit(node.getRight())
            )
        );
    }

    protected Expression visitTypeBinary(final TypeBinaryExpression node) {
        return node.update(visit(node.getOperand()));
    }

    protected Expression visitBlock(final BlockExpression node) {
        final int count = node.getExpressionCount();

        Expression[] nodes = null;

        for (int i = 0; i < count; i++) {
            final Expression oldNode = node.getExpression(i);
            final Expression newNode = visit(oldNode);

            if (newNode != oldNode) {
                if (nodes == null) {
                    nodes = new Expression[count];
                }
                nodes[i] = newNode;
            }
        }

        final ParameterExpressionList v = visitAndConvertList(
            node.getVariables(),
            "visitBlock"
        );

        if (v == node.getVariables() && nodes == null) {
            return node;
        }
        else if (nodes != null) {
            for (int i = 0; i < count; i++) {
                if (nodes[i] == null) {
                    nodes[i] = node.getExpression(i);
                }
            }
        }

        return node.rewrite(v, nodes);
    }

    protected Expression visitInvocation(final InvocationExpression node) {
        final Expression e = visit(node.getExpression());
        final ExpressionList<? extends Expression> a = visitArguments(node);

        if (e == node.getExpression() && a == null) {
            return node;
        }

        return node.rewrite((LambdaExpression)e, a);
    }

    protected Expression visitMethodCall(final MethodCallExpression node) {
        final Expression target = visit(node.getTarget());
        final ExpressionList<? extends Expression> arguments = visitArguments(node);

        if (target == node.getTarget() && arguments == null) {
            return node;
        }

        return node.rewrite(target, arguments);
    }

    protected Expression visitNew(final NewExpression node) {
        return node.update(visit(node.getArguments()));
    }

    protected Expression visitNewArray(final NewArrayExpression node) {
        return node.update(visit(node.getExpressions()));
    }

    protected <T> LambdaExpression<T> visitLambda(final LambdaExpression<T> node) {
        return node.update(visit(node.getBody()), visitAndConvertList(node.getParameters(), "visitLambda"));
    }

    protected Expression visitConditional(final ConditionalExpression node) {
        return node.update(visit(node.getTest()), visit(node.getIfTrue()), visit(node.getIfFalse()));
    }

    protected Expression visitRuntimeVariables(final RuntimeVariablesExpression node) {
        return node.update(visitAndConvertList(node.getVariables(), "visitRuntimeVariables"));
    }

    protected Expression visitTry(final TryExpression node) {
        return node.update(
            visit(node.getBody()),
            visit(
                node.getHandlers(),
                new ElementVisitor<CatchBlock>() {
                    @Override
                    public CatchBlock visit(final CatchBlock node) {
                        return visitCatchBlock(node);
                    }
                }
            ),
            visit(node.getFinallyBlock())
        );
    }

    protected CatchBlock visitCatchBlock(final CatchBlock node) {
        return node.update(
            visitAndConvert(node.getVariable(), "visitCatchBlock"),
            visit(node.getFilter()),
            visit(node.getBody())
        );
    }

    protected SwitchCase visitSwitchCase(final SwitchCase node) {
        return node.update(
            visit(node.getTestValues()),
            visit(node.getBody())
        );
    }
    
    protected Expression visitSwitch(final SwitchExpression node) {
        return validateSwitch(
            node,
            node.update(
                visit(node.getSwitchValue()),
                visit(
                    node.getCases(),
                    new ElementVisitor<SwitchCase>() {
                        @Override
                        public SwitchCase visit(final SwitchCase node) {
                            return visitSwitchCase(node);
                        }
                    }),
                visit(node.getDefaultBody()),
                node.getOptions()
            )
        );
    }

    protected static <T> ReadOnlyList<T> visit(final ReadOnlyList<T> nodes, final ElementVisitor<T> elementVisitor) {
        T[] newNodes = null;

        for (int i = 0, n = nodes.size(); i < n; i++) {
            final T node = nodes.get(i);
            final T newNode = elementVisitor.visit(node);

            if (newNode != node) {
                if (newNodes == null) {
                    newNodes = nodes.toArray();
                }
                newNodes[i] = newNode;
            }
        }
        if (newNodes == null) {
            return nodes;
        }
        return new ReadOnlyList<>(newNodes);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // HELPER METHODS                                                                                                     //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    final ExpressionList<? extends Expression> visit(final ExpressionList<? extends Expression> nodes) {
        Expression[] newNodes = null;

        for (int i = 0, n = nodes.size(); i < n; i++) {
            final Expression oldNode = nodes.get(i);
            final Expression node = visit(oldNode);

            if (newNodes != null) {
                newNodes[i] = node;
            }
            else if (node != oldNode) {
                newNodes = nodes.toArray();
                newNodes[i] = node;
            }
        }

        return newNodes == null ? nodes
                                : new ExpressionList<>(newNodes);
    }

    final ExpressionList<? extends Expression> visitArguments(final IArgumentProvider nodes) {
        Expression[] newNodes = null;
        for (int i = 0, n = nodes.getArgumentCount(); i < n; i++) {
            final Expression node = nodes.getArgument(i);
            final Expression newNode = visit(node);

            if (newNodes != null) {
                newNodes[i] = newNode;
            }
            else if (newNode != node) {
                newNodes = new Expression[n];
                for (int j = 0; j < i; j++) {
                    newNodes[j] = nodes.getArgument(j);
                }
                newNodes[i] = newNode;
            }
        }
        return newNodes == null ? null : new ExpressionList<>(newNodes);
    }

    @SuppressWarnings("unchecked")
    protected <T extends Expression> T visitAndConvert(final T node, final String callerName) {
        if (node == null) {
            return null;
        }

        final Expression newNode = visit(node);

        if (!node.getClass().isInstance(newNode)) {
            throw Error.mustRewriteToSameNode(callerName, node.getClass(), callerName);
        }

        return (T)newNode;
    }

    @SuppressWarnings("unchecked")
    protected <T extends Expression> ExpressionList<T> visitAndConvertList(final ExpressionList<T> nodes, final String callerName) {
        T[] newNodes = null;

        for (int i = 0, n = nodes.size(); i < n; i++) {
            final T oldNode = nodes.get(i);
            final T node = (T)visit(oldNode);

            if (node == null) {
                throw Error.mustRewriteToSameNode(callerName, oldNode.getClass(), callerName);
            }

            if (newNodes != null) {
                newNodes[i] = node;
            }
            else if (node != oldNode) {
                newNodes = nodes.toArray();
                newNodes[i] = node;
            }
        }

        if (newNodes == null) {
            return nodes;
        }

        return new ExpressionList<>(newNodes);
    }

    @SuppressWarnings("unchecked")
    protected ParameterExpressionList visitAndConvertList(final ParameterExpressionList nodes, final String callerName) {
        ParameterExpression[] newNodes = null;

        for (int i = 0, n = nodes.size(); i < n; i++) {
            final ParameterExpression oldNode = nodes.get(i);
            final ParameterExpression node = (ParameterExpression)visit(oldNode);

            if (node == null) {
                throw Error.mustRewriteToSameNode(callerName, oldNode.getClass(), callerName);
            }

            if (newNodes != null) {
                newNodes[i] = node;
            }
            else if (node != oldNode) {
                newNodes = nodes.toArray();
                newNodes[i] = node;
            }
        }

        if (newNodes == null) {
            return nodes;
        }

        return new ParameterExpressionList(newNodes);
    }

    private static UnaryExpression validateUnary(final UnaryExpression before, final UnaryExpression after) {
        if (before != after && before.getMethod() == null) {
            if (after.getMethod() != null) {
                throw Error.mustRewriteWithoutMethod(after.getMethod(), "visitUnary");
            }

            // rethrow has null operand
            if (before.getOperand() != null && after.getOperand() != null) {
                validateChildType(before.getOperand().getType(), after.getOperand().getType(), "visitUnary");
            }
        }
        return after;
    }

    private static BinaryExpression validateBinary(final BinaryExpression before, final BinaryExpression after) {
        if (before != after && before.getMethod() == null) {
            if (after.getMethod() != null) {
                throw Error.mustRewriteWithoutMethod(after.getMethod(), "VisitBinary");
            }

            validateChildType(before.getLeft().getType(), after.getLeft().getType(), "VisitBinary");
            validateChildType(before.getRight().getType(), after.getRight().getType(), "VisitBinary");
        }
        return after;
    }

    private static void validateChildType(final Type before, final Type after, final String methodName) {
        if (before.isPrimitive()) {
            if (before == after) {
                // types are the same value type
                return;
            }
        }
        else if (!after.isPrimitive()) {
            // both are reference types
            return;
        }

        // Otherwise, it's an invalid type change.
        throw Error.mustRewriteChildToSameType(before, after, methodName);
    }

    private static SwitchExpression validateSwitch(final SwitchExpression before, final SwitchExpression after) {
        // If we did not have a method, we don't want to bind to one,
        // it might not be the right thing.

        if (before.getComparison() == null && after.getComparison() != null) {
            throw Error.mustRewriteWithoutMethod(after.getComparison(), "visitSwitch");
        }

        return after;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DEPENDENT TYPES                                                                                                    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public interface ElementVisitor<T> {
        T visit(final T node);
    }
}
