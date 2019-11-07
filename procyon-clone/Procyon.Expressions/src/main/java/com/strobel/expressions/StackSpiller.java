/*
 * StackSpiller.java
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
import com.strobel.core.StrongBox;
import com.strobel.core.VerifyArgument;
import com.strobel.reflection.Type;
import com.strobel.util.ContractUtils;
import com.strobel.util.TypeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Strobel
 */
@SuppressWarnings("UnusedParameters")
final class StackSpiller {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS AND UTILITY METHODS                                                                                //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final TempMaker _tm = new TempMaker();

    /**
     * Initial stack state. Normally empty, but when inlining the lambda we
     * might have a non-empty starting stack state.
     */
    private final Stack _startingStack;

    /**
     * Lambda rewrite result. We need this for inlined lambdas to figure out
     * whether we need to guarantee it an empty stack.
     */
    private RewriteAction _lambdaRewrite;

    private StackSpiller(final Stack stack) {
        _startingStack = stack;
    }

    private void verifyTemps() {
        _tm.verifyTemps();
    }

    private ParameterExpression makeTemp(final Type type) {
        return _tm.temp(type);
    }

    private int mark() {
        return _tm.mark();
    }

    private void free(final int mark) {
        _tm.free(mark);
    }

    private ParameterExpression toTemp(final Expression expression, final StrongBox<Expression> save) {
        final ParameterExpression temp = makeTemp(expression.getType());
        save.value = Expression.assign(temp, expression);
        return temp;
    }

    /**
     * Creates a special block that is marked as not allowing jumps in.  This should not
     * be used for rewriting BlockExpression itself, or anything else that supports jumping.
     * @param expressions the expressions within the block
     * @return the new block
     */
    private static Expression makeBlock(final ExpressionList<? extends Expression> expressions) {
        return new SpilledExpressionBlock(expressions);
    }

    private static void verifyRewrite(final Result result, final Expression node) {
        assert result.Node != null;

        // (result.Action == RewriteAction.None) if and only if (node == result.Node) 
        assert (result.Action == RewriteAction.None) ^ (node != result.Node)
               :"rewrite action does not match node object identity";

        // if the original node is an extension node, it should have been rewritten 
        assert result.Node.getNodeType() != ExpressionType.Extension : "extension nodes must be rewritten";

        // if we have Copy, then node type must match
        assert result.Action != RewriteAction.Copy || node.getNodeType() == result.Node.getNodeType() || node.canReduce()
               : "rewrite action does not match node object kind";

        // New type must be reference assignable to the old type 
        // (our rewrites preserve type exactly, but the rules for rewriting
        // an extension node are more lenient, see Expression.ReduceAndCheck()) 
        assert TypeUtils.areReferenceAssignable(node.getType(), result.Node.getType())
               : "rewritten object must be reference assignable to the original type";
    }

    private static <T extends Expression> T[] clone(final ExpressionList<T> original, final int max) {
        assert original != null;
        assert max < original.size();

        final T[] clone = original.toArray();
        
        for (int i = max; i < clone.length; i++) {
            clone[i] = null;
        }
        
        return clone;
    }

    private static <T> T[] clone(final ReadOnlyList<T> original, final int max) {
        assert original != null;
        assert max < original.size();

        final T[] clone = original.toArray();

        for (int i = max; i < clone.length; i++) {
            clone[i] = null;
        }

        return clone;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ENTRY POINT                                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static <T> LambdaExpression<T> analyzeLambda(final LambdaExpression<T> lambda) {
        return /*Optimizer.optimize(lambda)*/lambda.accept(new StackSpiller(Stack.Empty));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // REWRITE METHODS                                                                                                    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    <T> LambdaExpression<T> rewrite(final LambdaExpression<T> lambda) {
        verifyTemps();

        final Result body = rewriteExpressionFreeTemps(lambda.getBody(), _startingStack);

        _lambdaRewrite = body.getAction();

        verifyTemps();

        if (body.getAction() != RewriteAction.None) {
            // Create a new scope for temps
            // (none of these will be hoisted so there is no closure impact)
            Expression newBody = body.Node;
            if (!_tm.getTemps().isEmpty()) {
                newBody = Expression.block(_tm.getTempsList(), newBody);
            }

            // Clone the lambda, replacing the body & variables 
            return lambda.update(
                newBody,
                lambda.getParameters()
            );
        }

        return lambda;
    }

    private Result rewriteExpressionFreeTemps(final Expression expression, final Stack stack) {
        final int mark = mark();
        final Result result = rewriteExpression(expression, stack);
        free(mark);
        return result;
    }

    private Result rewriteExpression(final Expression node, final Stack stack) {
        if (node == null) {
            return new Result(RewriteAction.None, null);
        }

        if (node.canReduce()) {
            return rewriteReducibleExpression(node, stack);
        }

        final Result result;

        switch (node.getNodeType()) {
            case Add:
                result = rewriteBinaryExpression(node, stack);
                break;
            case And:
                result = rewriteBinaryExpression(node, stack);
                break;
            case AndAlso:
                result = rewriteLogicalBinaryExpression(node, stack);
                break;
            case ArrayLength:
                result = rewriteUnaryExpression(node, stack);
                break;
            case ArrayIndex:
                result = rewriteBinaryExpression(node, stack);
                break;
            case Call:
                result = rewriteMethodCallExpression(node, stack);
                break;
            case Coalesce:
                result = rewriteLogicalBinaryExpression(node, stack);
                break;
            case Conditional:
                result = rewriteConditionalExpression(node, stack);
                break;
            case Convert:
                result = rewriteUnaryExpression(node, stack);
                break;
            case ConvertChecked:
                result = rewriteUnaryExpression(node, stack);
                break;
            case Divide:
                result = rewriteBinaryExpression(node, stack);
                break;
            case Equal:
                result = rewriteBinaryExpression(node, stack);
                break;
            case ExclusiveOr:
                result = rewriteBinaryExpression(node, stack);
                break;
            case GreaterThan:
                result = rewriteBinaryExpression(node, stack);
                break;
            case GreaterThanOrEqual:
                result = rewriteBinaryExpression(node, stack);
                break;
            case Invoke:
                result = rewriteInvocationExpression(node, stack);
                break;
            case Lambda:
                result = rewriteLambdaExpression(node, stack);
                break;
            case LeftShift:
                result = rewriteBinaryExpression(node, stack);
                break;
            case LessThan:
                result = rewriteBinaryExpression(node, stack);
                break;
            case LessThanOrEqual:
                result = rewriteBinaryExpression(node, stack);
                break;
            case MemberAccess:
                result = rewriteMemberExpression(node, stack);
                break;
            case Modulo:
                result = rewriteBinaryExpression(node, stack);
                break;
            case Multiply:
                result = rewriteBinaryExpression(node, stack);
                break;
            case Negate:
                result = rewriteUnaryExpression(node, stack);
                break;
            case UnaryPlus:
                result = rewriteUnaryExpression(node, stack);
                break;
            case New:
                result = rewriteNewExpression(node, stack);
                break;
            case NewArrayInit:
                result = rewriteNewArrayExpression(node, stack);
                break;
            case NewArrayBounds:
                result = rewriteNewArrayExpression(node, stack);
                break;
            case Not:
                result = rewriteUnaryExpression(node, stack);
                break;
            case NotEqual:
                result = rewriteBinaryExpression(node, stack);
                break;
            case Or:
                result = rewriteBinaryExpression(node, stack);
                break;
            case OrElse:
                result = rewriteLogicalBinaryExpression(node, stack);
                break;
            case RightShift:
                result = rewriteBinaryExpression(node, stack);
                break;
            case UnsignedRightShift:
                result = rewriteBinaryExpression(node, stack);
                break;
            case Subtract:
                result = rewriteBinaryExpression(node, stack);
                break;
            case InstanceOf:
                result = rewriteTypeBinaryExpression(node, stack);
                break;
            case Assign:
                result = rewriteAssignBinaryExpression(node, stack);
                break;
            case Block:
                result = rewriteBlockExpression(node, stack);
                break;
            case Decrement:
                result = rewriteUnaryExpression(node, stack);
                break;
            case Extension:
                result = rewriteExtensionExpression(node, stack);
                break;
            case Goto:
                result = rewriteGotoExpression(node, stack);
                break;
            case Increment:
                result = rewriteUnaryExpression(node, stack);
                break;
            case Label:
                result = rewriteLabelExpression(node, stack);
                break;
            case Loop:
                result = rewriteLoopExpression(node, stack);
                break;
            case Switch:
                result = rewriteSwitchExpression(node, stack);
                break;
            case Throw:
                result = rewriteThrowUnaryExpression(node, stack);
                break;
            case Try:
                result = rewriteTryExpression(node, stack);
                break;
            case Unbox:
                result = rewriteUnaryExpression(node, stack);
                break;
            case TypeEqual:
                result = rewriteTypeBinaryExpression(node, stack);
                break;
            case OnesComplement:
                result = rewriteUnaryExpression(node, stack);
                break;
            case IsTrue:
                result = rewriteUnaryExpression(node, stack);
                break;
            case IsFalse:
                result = rewriteUnaryExpression(node, stack);
                break;
            case ReferenceEqual:
                result = rewriteBinaryExpression(node, stack);
                break;
            case ReferenceNotEqual:
                result = rewriteBinaryExpression(node, stack);
                break;
            case IsNull:
                result = rewriteUnaryExpression(node, stack);
                break;
            case IsNotNull:
                result = rewriteUnaryExpression(node, stack);
                break;
            case AddAssign:
            case AndAssign:
            case DivideAssign:
            case ExclusiveOrAssign:
            case LeftShiftAssign:
            case ModuloAssign:
            case MultiplyAssign:
            case OrAssign:
            case RightShiftAssign:
            case UnsignedRightShiftAssign:
            case SubtractAssign:
            case PreIncrementAssign:
            case PreDecrementAssign:
            case PostIncrementAssign:
            case PostDecrementAssign:
                result = rewriteReducibleExpression(node, stack);
                break;
            case Quote:
            case Parameter:
            case Constant:
            case RuntimeVariables:
            case DefaultValue:
                return new Result(RewriteAction.None, node);

            default:
                throw ContractUtils.unreachable();
        }

        verifyRewrite(result, node);

        return result;
    }

    private Result rewriteReducibleExpression(final Expression expr, final Stack stack) {
        final Result result = rewriteExpression(expr.reduce(), stack);

        // It's at least Copy because we reduced the node
        return new Result(result.Action.or(RewriteAction.Copy), result.Node);
    }

    private Result rewriteTryExpression(final Expression expr, final Stack stack) {
        final TryExpression node = (TryExpression) expr;

        // Try statement definitely needs an empty stack so its 
        // child nodes execute at empty stack.
        final Result body = rewriteExpression(node.getBody(), Stack.Empty);

        ReadOnlyList<CatchBlock> handlers = node.getHandlers();
        CatchBlock[] clone = null;
        RewriteAction action = body.Action;

        if (handlers != null) {
            for (int i = 0, n = handlers.size(); i < n; i++) {
                RewriteAction currentAction = body.Action;

                CatchBlock handler = handlers.get(i);
                Expression filter = handler.getFilter();

                if (handler.getFilter() != null) {
                    // Our code gen saves the incoming filter value and provides it as a
                    // variable so the stack is empty.
                    final Result rFault = rewriteExpression(handler.getFilter(), Stack.Empty);

                    action = action.or(rFault.Action);
                    currentAction = currentAction.or(rFault.Action);
                    filter = rFault.Node;
                }

                // Catch block starts with an empty stack (guaranteed by TryStatement)
                final Result rBody = rewriteExpression(handler.getBody(), Stack.Empty);

                action = action.or(rBody.Action);
                currentAction = currentAction.or(rBody.Action);

                if (currentAction != RewriteAction.None) {
                    handler = Expression.makeCatch(
                        handler.getTest(),
                        handler.getVariable(),
                        rBody.Node,
                        filter);

                    if (clone == null) {
                        clone = clone(handlers, i);
                    }
                }

                if (clone != null) {
                    clone[i] = handler;
                }
            }
        }

        final Result rFinally = rewriteExpression(node.getFinallyBlock(), Stack.Empty);

        action = action.or(rFinally.Action);

        // If the stack is initially not empty, rewrite to spill the stack 
        if (stack != Stack.Empty) {
            action = RewriteAction.SpillStack;
        }

        if (action != RewriteAction.None) {
            if (clone != null) {
                // okay to wrap because we aren't modifying the array 
                handlers = new ReadOnlyList<>(clone);
            }

            return new Result(
                action,
                new TryExpression(
                    node.getType(),
                    body.Node,
                    handlers,
                    rFinally.Node
                )
            );
        }

        return new Result(action, expr);
    }

    private Result rewriteThrowUnaryExpression(final Expression expr, final Stack stack) {
        final UnaryExpression node = (UnaryExpression) expr;

        // Throw statement itself does not care about the stack, but it will empty
        // the stack, and it may cause stack imbalance.  We need to restore the stack
        // after unconditional throw to make JIT happy; this has an effect of executing
        // 'throw' on an empty stack.

        final Result value = rewriteExpressionFreeTemps(node.getOperand(), Stack.Empty);

        RewriteAction action = value.Action;

        if (stack != Stack.Empty) {
            action = RewriteAction.SpillStack;
        }

        if (action != RewriteAction.None) {
            return new Result(
                action,
                Expression.makeThrow(value.Node, node.getType())
            );
        }

        return new Result(action, expr);
    }

    private Result rewriteSwitchExpression(final Expression expr, final Stack stack) {
        final SwitchExpression node = (SwitchExpression)expr;

        // The switch statement test is emitted on the stack in current state
        final Result switchValue = rewriteExpressionFreeTemps(node.getSwitchValue(), stack);

        RewriteAction action = switchValue.Action;
        ReadOnlyList<SwitchCase> cases = node.getCases();
        SwitchCase[] clone = null;
        
        for (int i = 0, n = cases.size(); i < n; i++) {
            Expression[] cloneTests = null;
            SwitchCase switchCase = cases.get(i);
            ExpressionList<? extends Expression> testValues = switchCase.getTestValues();

            for (int j = 0, m = testValues.size(); j < m; j++) {
                // All tests execute at the same stack state as the switch. 
                // This is guaranteed by the compiler (to simplify spilling)
                final Result test = rewriteExpression(testValues.get(j), stack);

                action = action.or(test.Action);

                if (cloneTests == null && test.Action != RewriteAction.None) {
                    cloneTests = clone(testValues, j);
                }

                if (cloneTests != null) {
                    cloneTests[j] = test.Node;
                }
            }

            // And all the cases also run on the same stack level. 
            final Result body = rewriteExpression(switchCase.getBody(), stack);

            action = action.or(body.Action);

            if (body.Action != RewriteAction.None || cloneTests != null) {
                if (cloneTests != null) {
                    testValues = new ExpressionList<>(cloneTests);
                }

                switchCase = new SwitchCase(body.Node, testValues);

                if (clone == null) {
                    clone = clone(cases, i);
                }
            }

            if (clone != null) {
                clone[i] = switchCase;
            }
        }

        // Default body also runs on initial stack
        final Result defaultBody = rewriteExpression(node.getDefaultBody(), stack);

        action = action.or(defaultBody.Action);

        if (action != RewriteAction.None) {
            if (clone != null) {
                // Okay to wrap because we aren't modifying the array
                cases = new ReadOnlyList<>(clone);
            }

            return new Result(
                action,
                new SwitchExpression(
                    node.getType(),
                    switchValue.Node,
                    defaultBody.Node,
                    node.getComparison(),
                    cases,
                    node.getOptions()
                )
            );
        }

        return new Result(action, expr);
    }

    private Result rewriteLoopExpression(final Expression expr, final Stack stack) {
        final LoopExpression node = (LoopExpression) expr;

        // The loop statement requires empty stack for itself, so it
        // can guarantee it to the child nodes. 
        final Result body = rewriteExpression(node.getBody(), Stack.Empty);

        RewriteAction action = body.Action;

        // However, the loop itself requires that it executes on an empty stack
        // so we need to rewrite if the stack is not empty. 
        if (stack != Stack.Empty) {
            action = RewriteAction.SpillStack;
        }

        if (action != RewriteAction.None) {
            return new Result(
                action,
                new LoopExpression(
                    body.getNode(),
                    node.getBreakTarget(),
                    node.getContinueTarget()
                )
            );
        }

        return new Result(action, expr);
    }

    private Result rewriteLabelExpression(final Expression expr, final Stack stack) {
        final LabelExpression node = (LabelExpression)expr;

        final Result expression = rewriteExpression(node.getDefaultValue(), stack);

        if (expression.Action != RewriteAction.None) {
            return new Result(
                expression.Action,
                Expression.label(node.getTarget(), expression.Node)
            );
        }

        return new Result(expression.Action, expr);
    }

    private Result rewriteGotoExpression(final Expression expr, final Stack stack) {
        final GotoExpression node = (GotoExpression) expr;

        // Goto requires empty stack to execute so the expression is 
        // going to execute on an empty stack.
        final Result value = rewriteExpressionFreeTemps(node.getValue(), Stack.Empty);

        // However, the statement itself needs an empty stack for itself
        // so if stack is not empty, rewrite to empty the stack.
        RewriteAction action = value.Action;

        if (stack != Stack.Empty) {
            action = RewriteAction.SpillStack;
        }

        if (action != RewriteAction.None) {
            return new Result(
                action,
                Expression.makeGoto(
                    node.getKind(),
                    node.getTarget(),
                    value.Node,
                    node.getType()
                )
            );
        }

        return new Result(action, expr);
    }

    private Result rewriteExtensionExpression(final Expression expr, final Stack stack) {
        final Result result = rewriteExpression(expr.reduceExtensions(), stack);
        
        // It's at least Copy because we reduced the node
        return new Result(result.Action.or(RewriteAction.Copy), result.Node);
    }

    private Result rewriteBlockExpression(final Expression expr, final Stack stack) {
        final BlockExpression node = (BlockExpression) expr;
        final int count = node.getExpressionCount();

        RewriteAction action = RewriteAction.None;
        Expression[] clone = null;

        for (int i = 0; i < count; i++) {
            final Expression expression = node.getExpression(i);

            // All statements within the block execute at the same stack state.
            final Result rewritten = rewriteExpression(expression, stack);

            action = action.or(rewritten.Action);

            if (clone == null && rewritten.Action != RewriteAction.None) {
                clone = clone(node.getExpressions(), i);
            }

            if (clone != null) {
                clone[i] = rewritten.Node;
            }
        }

        if (action != RewriteAction.None) {
            // okay to wrap since we know no one can mutate the clone array
            return new Result(
                action,
                node.rewrite(null, clone));
        }

        return new Result(action, expr);
    }

    private Result rewriteAssignBinaryExpression(final Expression expr, final Stack stack) {
        final BinaryExpression node = (BinaryExpression)expr;

        switch (node.getLeft().getNodeType()) {
            case MemberAccess:
                return rewriteMemberAssignment(node, stack);
            case Parameter:
                return rewriteVariableAssignment(node, stack);
            case Extension:
                return rewriteExtensionAssignment(node, stack);
            default:
                throw Error.invalidLValue(node.getLeft().getNodeType());
        }
    }

    private Result rewriteExtensionAssignment(final BinaryExpression node, final Stack stack) {
        final Result result = rewriteAssignBinaryExpression(
            Expression.assign(
                node.getLeft().reduceExtensions(),
                node.getRight()),
            stack);
        // It's at least Copy because we reduced the node.
        return new Result(result.Action.or(RewriteAction.Copy), result.Node);
    }

    private Result rewriteVariableAssignment(final BinaryExpression node, final Stack stack) {
        // Expression is evaluated on a stack in current state
        final Result right = rewriteExpression(node.getRight(), stack);
        if (right.Action != RewriteAction.None) {
            return new Result(right.Action, Expression.assign(node.getLeft(), right.Node));
        }
        return new Result(right.Action, node);
    }

    private Result rewriteMemberAssignment(final BinaryExpression node, final Stack stack) {
        final MemberExpression lValue = (MemberExpression)node.getLeft();
        final ChildRewriter cr = new ChildRewriter(stack, 2);

        // If there's an instance, it executes on the stack in current state, and the rest
        // is executed on non-empty stack.  Otherwise the stack is left unchanged.
        cr.add(lValue.getTarget());
        cr.add(node.getRight());

        if (cr.didRewrite()) {
            return cr.Finish(
                new AssignBinaryExpression(
                    MemberExpression.make(cr.get(0), lValue.getMember()),
                    cr.get(1)
                )
            );
        }

        return new Result(RewriteAction.None, node);
    }

    private Result rewriteNewArrayExpression(final Expression expr, final Stack stack) {
        final NewArrayExpression node = (NewArrayExpression)expr;
        final Stack newStack;

        if (node.getNodeType() == ExpressionType.NewArrayInit) {
            // In a case of array construction with element initialization 
            // the element expressions are never emitted on an empty stack because
            // the array reference and the index are on the stack. 
            newStack = Stack.NonEmpty;
        }
        else {
            // In a case of NewArrayBounds we make no modifications to the stack 
            // before emitting bounds expressions.
            newStack = stack;
        }

        final ChildRewriter cr = new ChildRewriter(newStack, node.getExpressions().size());

        cr.add(node.getExpressions());

        if (cr.didRewrite()) {
            final Type element = node.getType().getElementType();
            if (node.getNodeType() == ExpressionType.NewArrayInit) {
                return cr.Finish(Expression.newArrayInit(element, cr.get(0, -1)));
            }
            else {
                return cr.Finish(Expression.newArrayBounds(element, cr.get(0)));
            }
        }

        return cr.Finish(expr);
    }

    private Result rewriteNewExpression(final Expression expr, final Stack stack) {
        final NewExpression node = (NewExpression) expr;

        // The first expression starts on a stack as provided by parent,
        // rest are definitely non-emtpy (which ChildRewriter guarantees)
        final ChildRewriter cr = new ChildRewriter(stack, node.getArgumentCount());
        
        cr.addArguments(node);

        return cr.Finish(
            cr.didRewrite()
                ? new NewExpression(node.getConstructor(), cr.get(0, -1))
                : expr);
    }

    private Result rewriteMemberExpression(final Expression expr, final Stack stack) {
        final MemberExpression node = (MemberExpression)expr;

        // Expression is emitted on top of the stack in current state
        final Result expression = rewriteExpression(node.getTarget(), stack);

        if (expression.Action != RewriteAction.None) {
            return new Result(
                expression.Action,
                MemberExpression.make(expression.Node, node.getMember())
            );
        }

        return new Result(expression.Action, expr);
    }

    private Result rewriteLambdaExpression(final Expression expr, final Stack stack) {
        final LambdaExpression<?> node = (LambdaExpression<?>) expr;

        // Call back into the rewriter.
        final LambdaExpression<?> analyzedLambda = analyzeLambda(node);

        // If the lambda gets rewritten, we don't need to spill the stack, but
        // we do need to rebuild the tree above us so it includes the new node.
        final RewriteAction action = (analyzedLambda == node) ? RewriteAction.None : RewriteAction.Copy;

        return new Result(action, analyzedLambda);
    }

    private Result rewriteInvocationExpression(final Expression expr, final Stack stack) {
        InvocationExpression node = (InvocationExpression) expr;

        final ChildRewriter cr;

        // See if the lambda will be inlined...
        LambdaExpression lambda = node.getLambdaOperand();

        if (lambda != null) {
            // Arguments execute on current stack
            cr = new ChildRewriter(stack, node.getArgumentCount());
            cr.add(node.getArguments());

            // Lambda body also executes on current stack
            final StackSpiller spiller = new StackSpiller(stack);

            lambda = lambda.accept(spiller);

            if (cr.didRewrite() || spiller._lambdaRewrite != RewriteAction.None) {
                node = new InvocationExpression(lambda, cr.get(0, -1), node.getType());
            }

            final Result result = cr.Finish(node);

            return new Result(result.Action.or(spiller._lambdaRewrite), result.Node);
        }

        cr = new ChildRewriter(stack, node.getArgumentCount() + 1);

        // First argument starts on stack as provided.
        cr.add(node.getExpression());

        // Rest of arguments have non-empty stack (delegate instance on the stack).
        cr.add(node.getArguments());


        return cr.Finish(
            cr.didRewrite()
                ? new InvocationExpression(
                cr.get(0),
                cr.get(1, -1),
                node.getType())
                : expr);
    }

    private Result rewriteTypeBinaryExpression(final Expression expr, final Stack stack) {
        final TypeBinaryExpression node = (TypeBinaryExpression) expr;

        // The expression is emitted on top of current stack.
        final Result expression = rewriteExpression(node.getOperand(), stack);

        if (expression.Action != RewriteAction.None) {
            if (node.getNodeType() == ExpressionType.InstanceOf) {
                return new Result(
                    expression.Action,
                    Expression.instanceOf(
                        expression.Node,
                        node.getTypeOperand()
                    )
                );
            }
            else {
                return new Result(
                    expression.Action,
                    Expression.typeEqual(
                        expression.Node,
                        node.getTypeOperand()
                    )
                );
            }
        }

        return new Result(expression.Action, expr);
    }

    private Result rewriteConditionalExpression(final Expression expr, final Stack stack) {
        final ConditionalExpression node = (ConditionalExpression)expr;

        // Test executes at the stack as left by parent.
        final Result test = rewriteExpression(node.getTest(), stack);

        // The test is popped by conditional jump so branches execute
        // at the stack as left by parent too.
        final Result ifTrue = rewriteExpression(node.getIfTrue(), stack);
        final Result ifFalse = rewriteExpression(node.getIfFalse(), stack);

        final RewriteAction action = test.Action.or(ifTrue.Action).or(ifFalse.Action);

        if (action != RewriteAction.None) {
            return new Result(
                action,
                Expression.condition(
                    test.Node,
                    ifTrue.Node,
                    ifFalse.Node,
                    node.getType()
                )
            );
        }

        return new Result(action, expr);
    }

    private Result rewriteMethodCallExpression(final Expression expr, final Stack stack) {
        final MethodCallExpression node = (MethodCallExpression)expr;
        final ChildRewriter cr = new ChildRewriter(stack, node.getArgumentCount() + 1);

        // For instance methods, the instance executes on the stack as is,
        // but stays on the stack, making it non-empty.
        cr.add(node.getTarget());
        cr.addArguments(node);

        return cr.Finish(cr.didRewrite() ? node.rewrite(cr.get(0), cr.get(1, -1)) : expr);
    }

    private Result rewriteUnaryExpression(final Expression expr, final Stack stack) {
        final UnaryExpression node = (UnaryExpression)expr;

        assert node.getNodeType() != ExpressionType.Quote : "unexpected Quote";
        assert node.getNodeType() != ExpressionType.Throw : "unexpected Throw";

        // Operand is emitted on top of the stack as is 
        final Result expression = rewriteExpression(node.getOperand(), stack);

        if (expression.Action != RewriteAction.None) {
            return new Result(
                expression.Action,
                new UnaryExpression(
                    node.getNodeType(),
                    expression.Node,
                    node.getType(),
                    node.getMethod())
            );
        }

        return new Result(expression.Action, expr);
    }

    private Result rewriteLogicalBinaryExpression(final Expression expr, final Stack stack) {
        final BinaryExpression node = (BinaryExpression)expr;
        // Left expression runs on a stack as left by parent 
        final Result left = rewriteExpression(node.getLeft(), stack);
        // ... and so does the right one
        final Result right = rewriteExpression(node.getRight(), stack);
        //conversion is a lambda. stack state will be ignored.
        final Result conversion = rewriteExpression(node.getConversion(), stack);

        final RewriteAction action = left.Action.or(right.Action).or(conversion.Action);

        if (action != RewriteAction.None) {
            return new Result(
                action,
                BinaryExpression.create(
                    node.getNodeType(),
                    left.Node,
                    right.Node,
                    node.getType(),
                    node.getMethod(),
                    (LambdaExpression) conversion.Node
                )
            );
        }

        return new Result(action, expr);
    }

    private Result rewriteBinaryExpression(final Expression expr, final Stack stack) {
        final BinaryExpression node = (BinaryExpression) expr;
        final ChildRewriter cr = new ChildRewriter(stack, 3);

        // Left expression executes on the stack as left by parent
        cr.add(node.getLeft());
        // Right expression always has non-empty stack (left is on it)
        cr.add(node.getRight());
        // conversion is a lambda, stack state will be ignored
        cr.add(node.getConversion());

        if (cr.didRewrite()) {
            return cr.Finish(
                BinaryExpression.create(
                    node.getNodeType(),
                    cr.get(0),
                    cr.get(1),
                    node.getType(),
                    node.getMethod(),
                    (LambdaExpression) cr.get(2)));
        }

        return cr.Finish(expr);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DEPENDENT TYPES                                                                                                    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static enum Stack {
        Empty,
        NonEmpty
    }

    private static enum RewriteAction {
        None(0),
        Copy(1),
        SpillStack(3);

        private final int _flags;

        RewriteAction(final int flags) {
            _flags = flags;
        }

        public RewriteAction or(final RewriteAction action) {
            final int flags = action._flags | _flags;
            switch (flags) {
                case 0:
                    return None;
                case 1:
                    return Copy;
                case 3:
                    return SpillStack;
            }
            throw new IllegalArgumentException();
        }
    }

    private static class Result {
        private final RewriteAction Action;
        private final Expression Node;

        Result(final RewriteAction action, final Expression node) {
            Action = action;
            Node = node;
        }

        public RewriteAction getAction() {
            return Action;
        }

        public Expression getNode() {
            return Node;
        }
    }

    private static class TempMaker {
        private int _temp;
        private ArrayList<ParameterExpression> _freeTemps;
        private java.util.Stack<ParameterExpression> _usedTemps;
        private final ArrayList<ParameterExpression> _temps = new ArrayList<>();

        List<ParameterExpression> getTemps() {
            return _temps;
        }

        ParameterExpressionList getTempsList() {
            final ParameterExpression[] temps = new ParameterExpression[_temps.size()];
            _temps.toArray(temps);
            return new ParameterExpressionList(temps);
        }

        ParameterExpression temp(final Type type) {
            ParameterExpression temp;
            if (_freeTemps != null) {
                // Recycle from the free-list if possible.
                for (int i = _freeTemps.size() - 1; i >= 0; i--) {
                    temp = _freeTemps.get(i);
                    if (temp.getType() == type) {
                        _freeTemps.remove(i);
                        return useTemp(temp);
                    }
                }
            }

            // Not on the free-list, create a brand new one.
            temp = Expression.variable(type, "$temp$" + _temp++);
            _temps.add(temp);
            return useTemp(temp);
        }

        private ParameterExpression useTemp(final ParameterExpression temp) {
            assert _freeTemps == null || !_freeTemps.contains(temp);
            assert _usedTemps == null || !_usedTemps.contains(temp);

            if (_usedTemps == null) {
                _usedTemps = new java.util.Stack<>();
            }
            _usedTemps.push(temp);
            return temp;
        }

        private void freeTemp(final ParameterExpression temp) {
            assert _freeTemps == null || !_freeTemps.contains(temp);
            if (_freeTemps == null) {
                _freeTemps = new ArrayList<>();
            }
            _freeTemps.add(temp);
        }

        int mark() {
            return _usedTemps != null ? _usedTemps.size() : 0;
        }

        void free(final int mark) {
            // (_usedTemps != null) ==> (mark <= _usedTemps.Count)
            assert _usedTemps == null || mark <= _usedTemps.size();
            // (_usedTemps == null) ==> (mark == 0)
            assert mark == 0 || _usedTemps != null;

            if (_usedTemps != null) {
                while (mark < _usedTemps.size()) {
                    freeTemp(_usedTemps.pop());
                }
            }
        }

        void verifyTemps() {
            assert _usedTemps == null || _usedTemps.isEmpty();
        }
    }

    /**
     * A special subtype of BlockExpression that indicates to the compiler
     * that this block is a spilled expression and should not allow jumps in.
     */
    final static class SpilledExpressionBlock extends BlockN {
        SpilledExpressionBlock(final ExpressionList<? extends Expression> expressions) {
            super(expressions);
        }

        @Override
        BlockExpression rewrite(final ParameterExpressionList variables, final Expression[] args) {
            throw ContractUtils.unreachable();
        }
    }

    /**
     * Rewrites child expressions, spilling them into temps if needed. The stack
     * starts in the initial state, and after the first sub-expression is added,
     * it is change to non-empty.  This behavior can be overridden by setting the
     * stack manually between adds.
     *
     * When all children have been added, the caller should rewrite the node if
     * didRewrite() is true.  Then, it should call finish() with either the original
     * expression or the rewritten expression.  finish() will call Expression.comma()
     * if necessary and return a new Result.
     */
    private final class ChildRewriter {
        private final Expression[] _expressions;
        private int _expressionsCount;
        private List<Expression> _comma;
        private RewriteAction _action = RewriteAction.None;
        private Stack _stack;
        private boolean _done;

        ChildRewriter(final Stack stack, final int count) {
            _stack = stack;
            _expressions = new Expression[count];
        }

        void add(final Expression node) {
            assert !_done;

            if (node == null) {
                _expressions[_expressionsCount++] = null;
                return;
            }

            final Result exp = rewriteExpression(node, _stack);

            _action = _action.or(exp.Action);
            _stack = Stack.NonEmpty;

            // track items in case we need to copy or spill stack 
            _expressions[_expressionsCount++] = exp.Node;
        }

        void add(final ExpressionList expressions) {
            for (int i = 0, count = expressions.size(); i < count; i++) {
                add(expressions.get(i));
            }
        }

        void addArguments(final IArgumentProvider expressions) {
            for (int i = 0, count = expressions.getArgumentCount(); i < count; i++) {
                add(expressions.getArgument(i));
            }
        }

        private void ensureDone() {
            // done adding arguments, build the comma if necessary 
            if (!_done) {
                _done = true;

                if (_action == RewriteAction.SpillStack) {
                    final Expression[] clone = _expressions;
                    final int count = clone.length;
                    final List<Expression> comma = new ArrayList<>(count + 1);

                    for (int i = 0; i < count; i++) {
                        if (clone[i] != null) {
                            final StrongBox<Expression> temp = new StrongBox<>();
                            clone[i] = toTemp(clone[i], temp);
                            comma.add(temp.value);
                        }
                    }

                    _comma = comma;
                }
            }
        }

        boolean didRewrite() {
            return _action != RewriteAction.None;
        }

        RewriteAction getAction() {
            return _action;
        }

        Result Finish(Expression expr) {
            ensureDone();

            if (_action == RewriteAction.SpillStack) {
                assert(_comma.size() == _expressions.length + 1);
                _comma.add(expr);
                final Expression[] expressions = _comma.toArray(new Expression[_comma.size()]);
                expr = makeBlock(new ExpressionList<>(expressions));
            }

            return new Result(_action, expr);
        }

        Expression get(int index) {
            ensureDone();
            if (index < 0) {
                index += _expressions.length;
            }
            return _expressions[index];
        }

        ExpressionList<? extends Expression> get(final int first, final int last) {
            ensureDone();

            int end = last;

            if (end < 0) {
                end += _expressions.length;
            }

            final int count = end - first + 1;

            VerifyArgument.validElementRange(_expressions.length, first, first + count);

            if (count == _expressions.length) {
                assert (first == 0);
                // if the entire array is requested just return it so we don't make a new array
                return new ExpressionList<>(_expressions);
            }

            final Expression[] clone = new Expression[count];
            System.arraycopy(_expressions, first, clone, 0, count);
            return new ExpressionList<>(clone);
        }
    }
}
