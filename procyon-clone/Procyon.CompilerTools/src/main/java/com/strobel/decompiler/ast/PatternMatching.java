/*
 * PatternMatching.java
 *
 * Copyright (c) 2013 Mike Strobel
 *
 * This source code is based on Mono.Cecil from Jb Evain, Copyright (c) Jb Evain;
 * and ILSpy/ICSharpCode from SharpDevelop, Copyright (c) AlphaSierraPapa.
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.decompiler.ast;

import com.strobel.assembler.metadata.ParameterDefinition;
import com.strobel.core.Comparer;
import com.strobel.core.Predicate;
import com.strobel.core.StrongBox;
import com.strobel.core.VerifyArgument;
import com.strobel.util.ContractUtils;

import java.util.ArrayList;
import java.util.List;

import static com.strobel.core.CollectionUtilities.any;
import static com.strobel.core.CollectionUtilities.single;

public final class PatternMatching {
    private PatternMatching() {
        throw ContractUtils.unreachable();
    }

    public static boolean match(final Node node, final AstCode code) {
        return node instanceof Expression &&
               ((Expression) node).getCode() == code;
    }

    public static boolean matchLeaveHandler(final Node node) {
        return match(node, AstCode.Leave) ||
               match(node, AstCode.EndFinally);
    }

    public static <T> boolean matchGetOperand(final Node node, final AstCode code, final StrongBox<? super T> operand) {
        if (node instanceof Expression) {
            final Expression expression = (Expression) node;

            if (expression.getCode() == code &&
                expression.getArguments().isEmpty()) {

                operand.set(expression.getOperand());
                return true;
            }
        }

        operand.set(null);
        return false;
    }

    public static <T> boolean matchGetOperand(
        final Node node,
        final AstCode code,
        final Class<T> operandType,
        final StrongBox<? super T> operand) {

        if (node instanceof Expression) {
            final Expression expression = (Expression) node;

            if (expression.getCode() == code &&
                expression.getArguments().isEmpty() &&
                operandType.isInstance(expression.getOperand())) {

                operand.set(expression.getOperand());
                return true;
            }
        }

        operand.set(null);
        return false;
    }

    public static boolean matchGetArguments(final Node node, final AstCode code, final List<Expression> arguments) {
        if (node instanceof Expression) {
            final Expression expression = (Expression) node;

            if (expression.getCode() == code) {
                assert expression.getOperand() == null;
                arguments.clear();
                arguments.addAll(expression.getArguments());
                return true;
            }
        }

        arguments.clear();
        return false;
    }

    public static <T> boolean matchGetArguments(
        final Node node,
        final AstCode code,
        final StrongBox<? super T> operand,
        final List<Expression> arguments) {

        if (node instanceof Expression) {
            final Expression expression = (Expression) node;

            if (expression.getCode() == code) {
                operand.set(expression.getOperand());
                arguments.clear();
                arguments.addAll(expression.getArguments());
                return true;
            }
        }

        operand.set(null);
        arguments.clear();
        return false;
    }

    public static boolean matchGetArgument(final Node node, final AstCode code, final StrongBox<Expression> argument) {
        final ArrayList<Expression> arguments = new ArrayList<>(1);

        if (matchGetArguments(node, code, arguments) && arguments.size() == 1) {
            argument.set(arguments.get(0));
            return true;
        }

        argument.set(null);
        return false;
    }

    public static <T> boolean matchGetArgument(
        final Node node,
        final AstCode code,
        final StrongBox<? super T> operand,
        final StrongBox<Expression> argument) {

        final ArrayList<Expression> arguments = new ArrayList<>(1);

        if (matchGetArguments(node, code, operand, arguments) && arguments.size() == 1) {
            argument.set(arguments.get(0));
            return true;
        }

        argument.set(null);
        return false;
    }

    public static <T> boolean matchGetArguments(
        final Node node,
        final AstCode code,
        final StrongBox<? super T> operand,
        final StrongBox<Expression> argument1,
        final StrongBox<Expression> argument2) {

        final ArrayList<Expression> arguments = new ArrayList<>(2);

        if (matchGetArguments(node, code, operand, arguments) && arguments.size() == 2) {
            argument1.set(arguments.get(0));
            argument2.set(arguments.get(1));
            return true;
        }

        argument1.set(null);
        argument2.set(null);
        return false;
    }

    public static <T> boolean matchSingle(
        final Block block,
        final AstCode code,
        final StrongBox<? super T> operand) {

        final List<Node> body = block.getBody();

        if (body.size() == 1 &&
            matchGetOperand(body.get(0), code, operand)) {

            return true;
        }

        operand.set(null);
        return false;
    }

    public static <T> boolean matchSingle(
        final Block block,
        final AstCode code,
        final StrongBox<? super T> operand,
        final StrongBox<Expression> argument) {

        final List<Node> body = block.getBody();

        if (body.size() == 1 &&
            matchGetArgument(body.get(0), code, operand, argument)) {

            return true;
        }

        operand.set(null);
        argument.set(null);
        return false;
    }

    public static boolean matchNullOrEmpty(final Block block) {
        return block == null || block.getBody().size() == 0;
    }

    public static boolean matchEmptyReturn(final Node node) {
        Node target = node;

        if (node instanceof Block || node instanceof BasicBlock) {
            final List<Node> body = node instanceof Block ? ((Block) node).getBody()
                                                          : ((BasicBlock) node).getBody();

            if (body.size() != 1) {
                return false;
            }

            target = body.get(0);
        }

        if (target instanceof Expression) {
            final Expression e = (Expression) target;

            return e.getCode() == AstCode.Return &&
                   e.getArguments().isEmpty();
        }

        return false;
    }

    public static boolean matchEmptyBlockOrLeave(final Node node) {
        if (node instanceof Block || node instanceof BasicBlock) {
            final List<Node> body = node instanceof Block ? ((Block) node).getBody()
                                                          : ((BasicBlock) node).getBody();

            switch (body.size()) {
                case 0:
                    return true;
                case 1:
                    return match(body.get(0), AstCode.Leave);
                default:
                    return false;
            }
        }

        if (node instanceof Expression) {
            final Expression e = (Expression) node;

            return e.getCode() == AstCode.Leave;
        }

        return false;
    }

    public static <T> boolean matchSingle(
        final BasicBlock block,
        final AstCode code,
        final StrongBox<? super T> operand,
        final StrongBox<Expression> argument) {

        final List<Node> body = block.getBody();

        if (body.size() == 2 &&
            body.get(0) instanceof Label &&
            matchGetArgument(body.get(1), code, operand, argument)) {

            return true;
        }

        operand.set(null);
        argument.set(null);
        return false;
    }

    public static <T> boolean matchSingleAndBreak(
        final BasicBlock block,
        final AstCode code,
        final StrongBox<? super T> operand,
        final StrongBox<Expression> argument,
        final StrongBox<Label> label) {

        final List<Node> body = block.getBody();

        if (body.size() == 3 &&
            body.get(0) instanceof Label &&
            matchGetArgument(body.get(1), code, operand, argument) &&
            matchGetOperand(body.get(2), AstCode.Goto, label)) {

            return true;
        }

        operand.set(null);
        argument.set(null);
        label.set(null);
        return false;
    }

    public static boolean matchSimpleBreak(final BasicBlock block, final StrongBox<Label> label) {
        final List<Node> body = block.getBody();

        if (body.size() == 2 &&
            body.get(0) instanceof Label &&
            matchGetOperand(body.get(1), AstCode.Goto, label)) {

            return true;
        }

        label.set(null);
        return false;
    }

    public static boolean matchSimpleBreak(final BasicBlock block, final Label label) {
        final List<Node> body = block.getBody();

        return body.size() == 2 &&
               body.get(0) instanceof Label &&
               match(body.get(1), AstCode.Goto) &&
               ((Expression) body.get(1)).getOperand() == label;
    }

    public static boolean matchAssignmentAndConditionalBreak(
        final BasicBlock block,
        final StrongBox<Expression> assignedValue,
        final StrongBox<Expression> condition,
        final StrongBox<Label> trueLabel,
        final StrongBox<Label> falseLabel,
        final StrongBox<Expression> equivalentLoad) {

        final List<Node> body = block.getBody();

        if (body.size() >= 4 &&
            body.get(0) instanceof Label &&
            body.get(body.size() - 3) instanceof Expression &&
            matchLastAndBreak(block, AstCode.IfTrue, trueLabel, condition, falseLabel)) {

            final Expression e = (Expression) body.get(body.size() - 3);

            if (match(e, AstCode.Store)) {
                assignedValue.set(e.getArguments().get(0));
                equivalentLoad.set(new Expression(AstCode.Load, e.getOperand(), e.getOffset()));
                return true;
            }

            if (match(e, AstCode.PutStatic)) {
                assignedValue.set(e.getArguments().get(0));
                equivalentLoad.set(new Expression(AstCode.GetStatic, e.getOperand(), e.getOffset()));
                return true;
            }

            if (matchElementAssignment(e, assignedValue, equivalentLoad)) {
                return true;
            }

            if (match(e, AstCode.PutField)) {
                final Expression arg0 = e.getArguments().get(0).clone();
                assignedValue.set(e.getArguments().get(1));
                equivalentLoad.set(new Expression(AstCode.GetField, null, arg0.getOffset(), arg0));
                return true;
            }
        }

        assignedValue.set(null);
        condition.set(null);
        trueLabel.set(null);
        falseLabel.set(null);
        return false;
    }

    public static boolean matchAssignment(final Node node, final StrongBox<Expression> assignedValue) {
        if (match(node, AstCode.Store) || match(node, AstCode.PutStatic)) {
            assignedValue.set(((Expression) node).getArguments().get(0));
            return true;
        }

        if (match(node, AstCode.StoreElement)) {
            assignedValue.set(((Expression) node).getArguments().get(2));
            return true;
        }

        if (match(node, AstCode.PutField)) {
            assignedValue.set(((Expression) node).getArguments().get(1));
            return true;
        }

        assignedValue.set(null);
        return false;
    }

    public static boolean matchAssignment(
        final Node node,
        final StrongBox<Expression> assignedValue,
        final StrongBox<Expression> equivalentLoad) {

        if (node instanceof Expression) {
            final Expression e = (Expression) node;

            if (match(e, AstCode.Store)) {
                assignedValue.set(e.getArguments().get(0));
                equivalentLoad.set(new Expression(AstCode.Load, e.getOperand(), e.getOffset()));
                return true;
            }

            if (match(e, AstCode.PutStatic)) {
                assignedValue.set(e.getArguments().get(0));
                equivalentLoad.set(new Expression(AstCode.GetStatic, e.getOperand(), e.getOffset()));
                return true;
            }

            if (matchElementAssignment(e, assignedValue, equivalentLoad)) {
                return true;
            }

            if (match(e, AstCode.PutField)) {
                final Expression arg0 = e.getArguments().get(0).clone();
                assignedValue.set(e.getArguments().get(1));
                equivalentLoad.set(new Expression(AstCode.GetField, e.getOperand(), arg0.getOffset(), arg0));
                return true;
            }
        }

        assignedValue.set(null);
        return false;
    }

    private static boolean matchElementAssignment(
        final Node node,
        final StrongBox<Expression> assignedValue,
        final StrongBox<Expression> equivalentLoad) {

        if (match(node, AstCode.StoreElement)) {
            final Expression e = (Expression) node;
            final Expression a0 = e.getArguments().get(0).clone();
            final Expression a1 = e.getArguments().get(1).clone();

            assignedValue.set(e.getArguments().get(2));
            equivalentLoad.set(new Expression(AstCode.LoadElement, null, a0.getOffset(), a0, a1));

            return true;
        }

        assignedValue.set(null);
        equivalentLoad.set(null);

        return false;
    }

    public static boolean matchLast(final BasicBlock block, final AstCode code) {
        final List<Node> body = block.getBody();

        return body.size() >= 1 &&
               match(body.get(body.size() - 1), code);
    }

    public static boolean matchLast(final Block block, final AstCode code) {
        final List<Node> body = block.getBody();

        return body.size() >= 1 &&
               match(body.get(body.size() - 1), code);
    }

    public static <T> boolean matchLast(
        final BasicBlock block,
        final AstCode code,
        final StrongBox<? super T> operand) {

        final List<Node> body = block.getBody();

        if (body.size() >= 1 &&
            matchGetOperand(body.get(body.size() - 1), code, operand)) {

            return true;
        }

        operand.set(null);
        return false;
    }

    public static <T> boolean matchLast(
        final Block block,
        final AstCode code,
        final StrongBox<? super T> operand) {

        final List<Node> body = block.getBody();

        if (body.size() >= 1 &&
            matchGetOperand(body.get(body.size() - 1), code, operand)) {

            return true;
        }

        operand.set(null);
        return false;
    }

    public static <T> boolean matchLast(
        final Block block,
        final AstCode code,
        final StrongBox<? super T> operand,
        final StrongBox<Expression> argument) {

        return matchLast(block.getBody(), code, operand, argument);
    }

    public static <T> boolean matchLast(
        final BasicBlock block,
        final AstCode code,
        final StrongBox<? super T> operand,
        final StrongBox<Expression> argument) {

        return matchLast(block.getBody(), code, operand, argument);
    }

    private static <T> boolean matchLast(
        final List<Node> body,
        final AstCode code,
        final StrongBox<? super T> operand,
        final StrongBox<Expression> argument) {

        if (body.size() >= 1 &&
            matchGetArgument(body.get(body.size() - 1), code, operand, argument)) {

            return true;
        }

        operand.set(null);
        argument.set(null);
        return false;
    }

    public static <T> boolean matchLastAndBreak(
        final BasicBlock block,
        final AstCode code,
        final StrongBox<? super T> operand,
        final StrongBox<Expression> argument,
        final StrongBox<Label> label) {

        final List<Node> body = block.getBody();

        if (body.size() >= 2 &&
            matchGetArgument(body.get(body.size() - 2), code, operand, argument) &&
            PatternMatching.matchGetOperand(body.get(body.size() - 1), AstCode.Goto, label)) {

            return true;
        }

        operand.set(null);
        argument.set(null);
        label.set(null);
        return false;
    }

    public static boolean matchThis(final Node node) {
        final StrongBox<Variable> operand = new StrongBox<>();
        final ParameterDefinition p;

        return matchGetOperand(node, AstCode.Load, operand) &&
               (p = operand.get().getOriginalParameter()) != null &&
               p.getPosition() == -1;
    }

    public static boolean matchLoadAny(final Node node, final Iterable<Variable> expectedVariables) {
        return any(
            expectedVariables,
            new Predicate<Variable>() {
                @Override
                public boolean test(final Variable variable) {
                    return matchLoad(node, variable);
                }
            }
        );
    }

    public static boolean matchLoad(final Node node, final StrongBox<Variable> variable) {
        return matchGetOperand(node, AstCode.Load, variable);
    }

    public static boolean matchNumericLdC(final Node node, final StrongBox<Number> value) {
        //noinspection ConstantConditions
        if (matchGetOperand(node, AstCode.LdC, value) &&
            value.get() instanceof Number) {

            return true;
        }

        value.set(null);
        return false;
    }

    public static boolean matchVariableIncDec(final Node node, final StrongBox<Variable> variable) {
        if (node instanceof Expression) {
            final Expression e = (Expression) node;
            final List<Expression> a = e.getArguments();
            final AstCode code = e.getCode();

            if (code.isIncDec()) {
                final Variable v;

                @SuppressWarnings("unchecked")
                final StrongBox<Number> valueBox = (StrongBox)variable;
                final StrongBox<Expression> argument = new StrongBox<>();

                if (matchGetArgument(e, AstCode.Inc, variable, argument) &&
                    (v = variable.get()) != null &&
                    matchNumericLdC(argument.get(), valueBox)) {

                    variable.set(v);
                    return true;
                }

                //noinspection ConstantConditions
                if (matchGetArgument(e, code, valueBox, argument) &&
                    valueBox.get() instanceof Number &&
                    matchLoad(argument.get(), variable)) {

                    return true;
                }
            }
        }

        variable.set(null);
        return false;
    }

    public static boolean matchVariableIncDec(
        final Node node,
        final StrongBox<Variable> variable,
        final StrongBox<Number> amount) {

        if (node instanceof Expression) {
            final Expression e = (Expression) node;
            final AstCode code = e.getCode();

            if (code.isIncDec()) {
                final StrongBox<Expression> argument = new StrongBox<>();

                if (matchGetArgument(e, AstCode.Inc, variable, argument) &&
                    matchNumericLdC(argument.get(), amount)) {

                    return true;
                }

                //noinspection ConstantConditions
                if (matchGetArgument(e, code, amount, argument) &&
                    amount.get() instanceof Number &&
                    matchLoad(argument.get(), variable)) {

                    return true;
                }
            }
        }

        variable.set(null);
        amount.set(null);
        return false;
    }

    public static boolean matchStore(
        final Node node,
        final StrongBox<Variable> variable,
        final StrongBox<Expression> argument) {

        return matchGetArgument(node, AstCode.Store, variable, argument);
    }

    public static boolean matchStore(
        final Node node,
        final StrongBox<Variable> variable,
        final List<Expression> argument) {

        return matchGetArguments(node, AstCode.Store, variable, argument);
    }

    public static boolean matchLoadOrRet(final Node node, final StrongBox<Variable> variable) {
        return matchGetOperand(node, AstCode.Load, variable) ||
               matchGetOperand(node, AstCode.Ret, variable);
    }

    public static boolean matchLoad(final Node node, final Variable expectedVariable) {
        final StrongBox<Variable> operand = new StrongBox<>();

        return matchGetOperand(node, AstCode.Load, operand) &&
               Comparer.equals(operand.get(), expectedVariable);
    }

    public static boolean matchStore(final Node node, final Variable expectedVariable) {
        return match(node, AstCode.Store) &&
               Comparer.equals(((Expression) node).getOperand(), expectedVariable);
    }

    public static boolean matchStore(
        final Node node,
        final Variable expectedVariable,
        final StrongBox<Expression> value) {

        final StrongBox<Variable> v = new StrongBox<>();

        if (matchGetArgument(node, AstCode.Store, v, value) &&
            Comparer.equals(((Expression) node).getOperand(), expectedVariable) &&
            v.get() == expectedVariable) {

            return true;
        }

        value.set(null);
        return false;
    }

    public static boolean matchLoad(
        final Node node,
        final Variable expectedVariable,
        final StrongBox<Expression> argument) {

        final StrongBox<Variable> operand = new StrongBox<>();

        return matchGetArgument(node, AstCode.Load, operand, argument) &&
               Comparer.equals(operand.get(), expectedVariable);
    }

    public static boolean matchLoadStore(
        final Node node,
        final Variable expectedVariable,
        final StrongBox<Variable> targetVariable) {

        final StrongBox<Expression> temp = new StrongBox<>();

        if (matchGetArgument(node, AstCode.Store, targetVariable, temp) &&
            matchLoad(temp.get(), expectedVariable)) {

            return true;
        }

        targetVariable.set(null);
        return false;
    }

    public static boolean matchLoadStoreAny(
        final Node node,
        final Iterable<Variable> expectedVariables,
        final StrongBox<Variable> targetVariable) {

        for (final Variable variable : VerifyArgument.notNull(expectedVariables, "expectedVariables")) {
            if (matchLoadStore(node, variable, targetVariable)) {
                return true;
            }
        }

        return false;
    }

    public static boolean matchBooleanComparison(
        final Node node,
        final StrongBox<Expression> argument,
        final StrongBox<Boolean> comparand) {

        final List<Expression> a = new ArrayList<>(2);

        if (matchGetArguments(node, AstCode.CmpEq, a) || matchGetArguments(node, AstCode.CmpNe, a)) {
            comparand.set(matchBooleanConstant(a.get(0)));

            if (comparand.get() == null) {
                comparand.set(matchBooleanConstant(a.get(1)));

                if (comparand.get() == null) {
                    return false;
                }

                argument.set(a.get(0));
            }
            else {
                argument.set(a.get(1));
            }

            comparand.set(match(node, AstCode.CmpEq) ^ (comparand.get() == Boolean.FALSE));
            return true;
        }

        return false;
    }

    public static boolean matchComparison(
        final Node node,
        final StrongBox<Expression> left,
        final StrongBox<Expression> right) {

        if (node instanceof Expression) {
            final Expression e = (Expression) node;

            switch (e.getCode()) {
                case CmpEq:
                case CmpNe:
                case CmpLt:
                case CmpGt:
                case CmpLe:
                case CmpGe: {
                    final List<Expression> arguments = e.getArguments();
                    left.set(arguments.get(0));
                    right.set(arguments.get(1));
                    return true;
                }
            }
        }

        left.set(null);
        right.set(null);
        return false;
    }

    public static boolean matchSimplifiableComparison(final Node node) {
        if (node instanceof Expression) {
            final Expression e = (Expression) node;

            switch (e.getCode()) {
                case CmpEq:
                case CmpNe:
                case CmpLt:
                case CmpGe:
                case CmpGt:
                case CmpLe: {
                    final Expression comparisonArgument = e.getArguments().get(0);

                    switch (comparisonArgument.getCode()) {
                        case __LCmp:
                        case __FCmpL:
                        case __FCmpG:
                        case __DCmpL:
                        case __DCmpG:
                            final Expression constantArgument = e.getArguments().get(1);
                            final StrongBox<Integer> comparand = new StrongBox<>();

                            return matchGetOperand(constantArgument, AstCode.LdC, Integer.class, comparand) &&
                                   comparand.get() == 0;
                    }
                }
            }
        }

        return false;
    }

    public static boolean matchReversibleComparison(final Node node) {
        if (match(node, AstCode.LogicalNot)) {
            switch (((Expression) node).getArguments().get(0).getCode()) {
                case CmpEq:
                case CmpNe:
                case CmpLt:
                case CmpGe:
                case CmpGt:
                case CmpLe:
                    return true;
            }
        }

        return false;
    }

    public static boolean matchReturnOrThrow(final Node node) {
        return match(node, AstCode.Return) ||
               match(node, AstCode.AThrow);
    }

    public static Boolean matchTrue(final Node node) {
        return Boolean.TRUE.equals(matchBooleanConstant(node));
    }

    public static Boolean matchFalse(final Node node) {
        return Boolean.FALSE.equals(matchBooleanConstant(node));
    }

    public static Boolean matchBooleanConstant(final Node node) {
        if (match(node, AstCode.LdC)) {
            final Object operand = ((Expression) node).getOperand();

            if (operand instanceof Boolean) {
                return (Boolean) operand;
            }

            if (operand instanceof Number && !(operand instanceof Float || operand instanceof Double)) {
                final long longValue = ((Number) operand).longValue();

                if (longValue == 0) {
                    return Boolean.FALSE;
                }

                if (longValue == 1) {
                    return Boolean.TRUE;
                }
            }
        }

        return null;
    }

    public static Character matchCharacterConstant(final Node node) {
        if (match(node, AstCode.LdC)) {
            final Object operand = ((Expression) node).getOperand();

            if (operand instanceof Character) {
                return (Character) operand;
            }

            if (operand instanceof Number && !(operand instanceof Float || operand instanceof Double)) {
                final long longValue = ((Number) operand).longValue();

                if (longValue >= Character.MIN_VALUE && longValue <= Character.MAX_VALUE) {
                    return (char) longValue;
                }
            }
        }

        return null;
    }

    public static boolean matchBooleanConstant(final Node node, final StrongBox<Boolean> value) {
        final Boolean booleanConstant = matchBooleanConstant(node);

        if (booleanConstant != null) {
            value.set(booleanConstant);
            return true;
        }

        value.set(null);
        return false;
    }

    public static boolean matchCharacterConstant(final Node node, final StrongBox<Character> value) {
        final Character characterConstant = matchCharacterConstant(node);

        if (characterConstant != null) {
            value.set(characterConstant);
            return true;
        }

        value.set(null);
        return false;
    }

    public static boolean matchUnconditionalBranch(final Node node) {
        return node instanceof Expression &&
               ((Expression) node).getCode().isUnconditionalControlFlow();
    }

    public static boolean matchLock(final List<Node> body, final int position, final StrongBox<LockInfo> result) {
        VerifyArgument.notNull(body, "body");
        VerifyArgument.notNull(result, "result");

        result.set(null);

        int head = position;

        if (head < 0 || head >= body.size()) {
            return false;
        }

        final List<Expression> a = new ArrayList<>();
        final Label leadingLabel;

        if (body.get(head) instanceof Label) {
            leadingLabel = (Label) body.get(head);
            ++head;
        }
        else {
            leadingLabel = null;
        }

        if (head >= body.size()) {
            return false;
        }

        if (matchGetArguments(body.get(head), AstCode.MonitorEnter, a)) {
            if (!match(a.get(0), AstCode.Load)) {
                return false;
            }

            result.set(new LockInfo(leadingLabel, (Expression) body.get(head)));
            return true;
        }

        final StrongBox<Variable> v = new StrongBox<>();
        final Variable lockVariable;

        Expression lockInit;
        Expression lockStore;
        Expression lockStoreCopy;

        if (head < body.size() - 1 &&
            matchGetArguments(body.get(head), AstCode.Store, v, a)) {

            lockVariable = v.get();
            lockInit = a.get(0);
            lockStore = (Expression) body.get(head++);

            if (matchLoadStore(body.get(head), lockVariable, v)) {
                lockStoreCopy = (Expression) body.get(head++);
            }
            else {
                lockStoreCopy = null;
            }

            if (head < body.size() &&
                matchGetArguments(body.get(head), AstCode.MonitorEnter, a)) {

                if (!matchLoad(a.get(0), lockVariable)) {
                    if (matchGetOperand(lockInit, AstCode.Load, v) &&
                        matchLoad(a.get(0), v.get())) {

                        lockStoreCopy = lockStore;
                        lockStore = null;
                        lockInit = null;
                    }
                    else {
                        return false;
                    }
                }

                result.set(
                    new LockInfo(
                        leadingLabel,
                        lockInit,
                        lockStore,
                        lockStoreCopy,
                        (Expression) body.get(head)
                    )
                );

                return true;
            }
        }

        return false;
    }

    public static boolean matchUnlock(final Node e, final LockInfo lockInfo) {
        if (lockInfo == null) {
            return false;
        }

        final StrongBox<Expression> a = new StrongBox<>();

        return matchGetArgument(e, AstCode.MonitorExit, a) &&
               (matchLoad(a.get(), lockInfo.lock) ||
                lockInfo.lockCopy != null && matchLoad(a.get(), lockInfo.lockCopy));
    }

    public static boolean matchVariableMutation(final Node node, final Variable variable) {
        VerifyArgument.notNull(node, "node");
        VerifyArgument.notNull(variable, "variable");

        if (node instanceof Expression) {
            final Expression e = (Expression) node;

            switch (e.getCode()) {
                case Store:
                case Inc:
                    return e.getOperand() == variable;

                case PreIncrement:
                case PostIncrement:
                    return matchLoad(single(e.getArguments()), variable);
            }
        }

        return false;
    }
}
