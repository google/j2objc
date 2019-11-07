/*
 * Inlining.java
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

import com.strobel.annotations.NotNull;
import com.strobel.assembler.metadata.MetadataHelper;
import com.strobel.core.CollectionUtilities;
import com.strobel.core.MutableInteger;
import com.strobel.core.Predicate;
import com.strobel.core.StrongBox;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.util.ContractUtils;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.strobel.core.CollectionUtilities.*;
import static com.strobel.decompiler.ast.PatternMatching.*;
import static java.lang.String.format;

final class Inlining {
    @SuppressWarnings({ "FieldCanBeLocal", "UnusedDeclaration" })
    private final DecompilerContext _context;
    private final Block _method;
    private final boolean _aggressive;

    final Map<Variable, MutableInteger> loadCounts;
    final Map<Variable, MutableInteger> storeCounts;
    final Map<Variable, List<Expression>> loads;
    final StrongBox<Variable> _tempVariable = new StrongBox<>();
    final StrongBox<Expression> _tempExpression = new StrongBox<>();

    public Inlining(final DecompilerContext context, final Block method) {
        this(context, method, false);
    }

    public Inlining(final DecompilerContext context, final Block method, final boolean aggressive) {
        _context = context;
        _method = method;
        _aggressive = aggressive;

        loadCounts = new DefaultMap<>(MutableInteger.SUPPLIER);
        storeCounts = new DefaultMap<>(MutableInteger.SUPPLIER);
        loads = new DefaultMap<>(CollectionUtilities.<Expression>listFactory());

        analyzeMethod();
    }

    // <editor-fold defaultstate="collapsed" desc="Load/Store Analysis">

    final void analyzeMethod() {
        loadCounts.clear();
        storeCounts.clear();

        analyzeNode(_method);
    }

    final void analyzeNode(final Node node) {
        if (node instanceof Expression) {
            final Expression e = (Expression) node;

            if (matchLoadOrRet(e, _tempVariable)) {
                increment(loadCounts, _tempVariable.get());
                loads.get(_tempVariable.get()).add(e);
            }
            else if (matchStore(e, _tempVariable, _tempExpression)) {
                increment(storeCounts, _tempVariable.get());
            }
            else if (matchVariableIncDec(e, _tempVariable)) {
                increment(loadCounts, _tempVariable.get());
                increment(storeCounts, _tempVariable.get());
                loads.get(_tempVariable.get()).add(e);
            }
            else if (e.getOperand() instanceof Variable) {
                throw new IllegalStateException(
                    String.format(
                        "Unexpected instruction <%s> with variable operand at offset %d in %s:%s",
                        e,
                        e.getOffset(),
                        _context.getCurrentMethod().getFullName(),
                        _context.getCurrentMethod().getErasedSignature()
                    )
                );
            }

            for (final Expression argument : e.getArguments()) {
                analyzeNode(argument);
            }
        }
        else {
            if (node instanceof CatchBlock) {
                final CatchBlock catchBlock = (CatchBlock) node;
                final Variable exceptionVariable = catchBlock.getExceptionVariable();

                if (exceptionVariable != null) {
                    increment(storeCounts, exceptionVariable);
                }
            }

            for (final Node child : node.getChildren()) {
                analyzeNode(child);
            }
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Inlining">

    final boolean inlineAllVariables() {
        boolean modified = false;

        for (final Block block : _method.getSelfAndChildrenRecursive(Block.class)) {
            modified |= inlineAllInBlock(block);
        }

        return modified;
    }

    final boolean inlineAllInBlock(final Block block) {
        boolean modified = false;

        final List<Node> body = block.getBody();
        final StrongBox<Variable> tempVariable = new StrongBox<>();
        final StrongBox<Expression> tempExpression = new StrongBox<>();

        if (block instanceof CatchBlock && !body.isEmpty()) {
            final CatchBlock catchBlock = (CatchBlock) block;
            final Variable v = catchBlock.getExceptionVariable();

            if (v != null &&
                v.isGenerated() &&
                count(storeCounts, v) == 1 &&
                count(loadCounts, v) <= 1) {

                if (matchGetArgument(body.get(0), AstCode.Store, tempVariable, tempExpression) &&
                    matchLoad(tempExpression.get(), v)) {

                    body.remove(0);
                    catchBlock.setExceptionVariable(tempVariable.get());
                    modified = true;
                }
            }
        }

        for (int i = 0; i < body.size() - 1; ) {
            final Node node = body.get(i);

            if (matchGetArgument(node, AstCode.Store, tempVariable, tempExpression) &&
                inlineOneIfPossible(block.getBody(), i, _aggressive)) {

                modified = true;
                i = 0;//Math.max(0, i - 1);
            }
            else {
                i++;
            }
        }

        for (final Node node : body) {
            if (node instanceof BasicBlock) {
                modified |= inlineAllInBasicBlock((BasicBlock) node);
            }
        }

        return modified;
    }

    final boolean inlineAllInBasicBlock(final BasicBlock basicBlock) {
        boolean modified = false;

        final List<Node> body = basicBlock.getBody();
        final StrongBox<Variable> tempVariable = new StrongBox<>();
        final StrongBox<Expression> tempExpression = new StrongBox<>();

        for (int i = 0; i < body.size(); ) {
            final Node node = body.get(i);

            if (matchGetArgument(node, AstCode.Store, tempVariable, tempExpression) &&
                inlineOneIfPossible(basicBlock.getBody(), i, _aggressive)) {

                modified = true;
                i = Math.max(0, i - 1);
            }
            else {
                i++;
            }
        }

        return modified;
    }

    final boolean inlineIfPossible(final List<Node> body, final MutableInteger position) {
        final int currentPosition = position.getValue();

        if (inlineOneIfPossible(body, currentPosition, true)) {
            position.setValue(currentPosition - inlineInto(body, currentPosition, _aggressive));
            return true;
        }

        return false;
    }

    final int inlineInto(final List<Node> body, final int position, final boolean aggressive) {
        if (position >= body.size()) {
            return 0;
        }

        int count = 0;
        int p = position;

        while (--p >= 0) {
            final Node node = body.get(p);

            if (node instanceof Expression) {
                final Expression e = (Expression) node;

                if (e.getCode() != AstCode.Store) {
                    break;
                }

                if (inlineOneIfPossible(body, p, aggressive)) {
                    ++count;
                }
            }
            else {
                break;
            }
        }

        return count;
    }

    final boolean inlineIfPossible(final Variable variable, final Expression inlinedExpression, final Node next, final boolean aggressive) {
        //
        // Ensure the variable is accessed only a single time.
        //
        final int storeCount = count(storeCounts, variable);
        final int loadCount = count(loadCounts, variable);

        if (storeCount != 1 || loadCount > 1) {
            return false;
        }

        if (!canInline(aggressive, variable)) {
            return false;
        }

        Node n = next;

        if (n instanceof Condition) {
            n = ((Condition) n).getCondition();
        }
        else if (n instanceof Loop) {
            n = ((Loop) n).getCondition();
        }

        if (!(n instanceof Expression)) {
            return false;
        }

        final StrongBox<Variable> v = new StrongBox<>();
        final StrongBox<Expression> parent = new StrongBox<>();
        final MutableInteger position = new MutableInteger();

        if (matchStore(inlinedExpression, v, parent) &&
            match(parent.value, AstCode.InitArray) &&
            (match(n, AstCode.LoadElement) || match(n, AstCode.StoreElement))) {

            //
            // Don't allow creation of `(n = new X[] ( ... )[n]`.  It's ugly, and I hate it.
            //
            return false;
        }

        if (findLoadInNext((Expression) n, variable, inlinedExpression, parent, position) == Boolean.TRUE) {
            if (!aggressive &&
                !(variable.isGenerated() ||
                  notFromMetadata(variable) && matchReturnOrThrow(n) /* allow inline to return or throw */) &&
                !nonAggressiveInlineInto((Expression) n, parent.get(), inlinedExpression)) {

                return false;
            }

            final List<Expression> parentArguments = parent.get().getArguments();
            final Map<Expression, Expression> parentLookup = new IdentityHashMap<>();

            for (final Expression node : next.getSelfAndChildrenRecursive(Expression.class)) {
                for (final Expression child : node.getArguments()) {
                    parentLookup.put(child, node);
                }
            }

            final List<Expression> nestedAssignments = inlinedExpression.getSelfAndChildrenRecursive(
                Expression.class,
                new Predicate<Expression>() {
                    @Override
                    public boolean test(final Expression node) {
                        return node.getCode() == AstCode.Store;
                    }
                }
            );

            //
            // Make sure we do not inline an initialization expression into the left-hand side of an assignment
            // whose value references the initialized variable.  For example, do not allow inlining in this case:
            //
            //     v = (x = y); v.f = x.f - 1 => (x = y).f = x.f - 1
            //
            for (final Expression assignment : nestedAssignments) {
                Expression lastParent = parentArguments.get(position.getValue());

                for (final Expression e : getParents((Expression) n, parentLookup, parentArguments.get(position.getValue()))) {
                    if (e.getCode().isWriteOperation()) {
                        boolean lastParentFound = false;

                        for (final Expression a : e.getArguments()) {
                            if (lastParentFound) {
                                if (AstOptimizer.references(a, (Variable) assignment.getOperand())) {
                                    return false;
                                }
                            }
                            else if (a == lastParent) {
                                lastParentFound = true;
                            }
                        }
                    }
                    lastParent = e;
                }
            }

            //
            // Assign the ranges of the Load instruction.
            //
            inlinedExpression.getRanges().addAll(
                parentArguments.get(position.getValue()).getRanges()
            );

            parentArguments.set(position.getValue(), inlinedExpression);

            return true;
        }

        return false;
    }

    private boolean notFromMetadata(final Variable variable) {
        return variable.isGenerated() ||
               !variable.isParameter() && !variable.getOriginalVariable().isFromMetadata();
    }

    private boolean nonAggressiveInlineInto(
        final Expression next,
        final Expression parent,
        final Expression inlinedExpression) {

        if (inlinedExpression.getCode() == AstCode.DefaultValue) {
            return true;
        }

        switch (next.getCode()) {
            case Return:
            case IfTrue:
            case Switch: {
                final List<Expression> arguments = next.getArguments();
                return arguments.size() == 1 && arguments.get(0) == parent;
            }

            case DefaultValue: {
                return true;
            }

            default: {
                return false;
            }
        }
    }

    final Boolean findLoadInNext(
        final Expression expression,
        final Variable variable,
        final Expression expressionBeingMoved,
        final StrongBox<Expression> parent,
        final MutableInteger position) {

        parent.set(null);
        position.setValue(0);

        if (expression == null) {
            return Boolean.FALSE;
        }

        final AstCode code = expression.getCode();
        final List<Expression> arguments = expression.getArguments();

        for (int i = 0; i < arguments.size(); i++) {
            //
            // Stop when seeing an opcode that does not guarantee that its operands will be evaluated.
            // Inlining in that case might result in the inlined expression not being evaluated.
            //
            if (i == 1 &&
                (code == AstCode.LogicalAnd ||
                 code == AstCode.LogicalOr ||
                 code == AstCode.TernaryOp)) {

                return Boolean.FALSE;
            }

            final Expression argument = arguments.get(i);

            if (argument.getCode() == AstCode.Load && argument.getOperand() == variable) {
                switch (code) {
                    case PreIncrement:
                    case PostIncrement:
                        if (expressionBeingMoved.getCode() != AstCode.Load) {
                            return Boolean.FALSE;
                        }
                        break;
                }
                parent.set(expression);
                position.setValue(i);
                return Boolean.TRUE;
            }

            final StrongBox<Expression> tempExpression = new StrongBox<>();
            final StrongBox<Object> tempOperand = new StrongBox<>();

            if (matchGetArgument(argument, AstCode.PostIncrement, tempOperand, tempExpression) &&
                matchGetOperand(tempExpression.get(), AstCode.Load, tempOperand) && tempOperand.get() == variable) {

                return Boolean.FALSE;
            }

            final Boolean result = findLoadInNext(argument, variable, expressionBeingMoved, parent, position);

            if (Boolean.TRUE.equals(result)) {
                return result;
            }
        }

        if (isSafeForInlineOver(expression, expressionBeingMoved)) {
            //
            // Continue searching.
            //
            return null;
        }

        //
        // Abort; inlining not possible.
        //
        return Boolean.FALSE;
    }

    static boolean isSafeForInlineOver(final Expression expression, final Expression expressionBeingMoved) {
        switch (expression.getCode()) {
            case Load: {
                final Variable loadedVariable = (Variable) expression.getOperand();

                for (final Expression potentialStore : expressionBeingMoved.getSelfAndChildrenRecursive(Expression.class)) {
                    if (matchVariableMutation(potentialStore, loadedVariable)) {
                        return false;
                    }
                }

                //
                // The expression is loading a non-forbidden variable.
                //
                return true;
            }

            default: {
                //
                // Expressions with no side effects are safe (except for Load, which is handled above).
                //
                return hasNoSideEffect(expression);
            }
        }
    }

    final boolean inlineOneIfPossible(final List<Node> body, final int position, final boolean aggressive) {
        final StrongBox<Variable> variable = new StrongBox<>();
        final StrongBox<Expression> inlinedExpression = new StrongBox<>();

        final Node node = body.get(position);

        if (matchGetArgument(node, AstCode.Store, variable, inlinedExpression)) {
            final Node next = getOrDefault(body, position + 1);
            final Variable v = variable.get();
            final Expression e = inlinedExpression.get();
            final Expression current = (Expression) node;

            if (inlineIfPossible(v, e, next, aggressive)) {
                //
                // Assign the ranges of the Store instruction.
                //
                e.getRanges().addAll(current.getRanges());

                //
                // Remove the store instruction.
                //
                body.remove(position);
                return true;
            }

            if (match(e, AstCode.Store) &&
                canInline(true, variable.value) &&
                count(storeCounts, variable.value) == 1 &&
                count(loadCounts, variable.value) <= 1 &&
                count(loadCounts, (Variable) e.getOperand()) <= 1) {

                //
                // Check to see if we have an expression like 'x = y = <some expression>`, where both
                // `x` and `y` are loaded at most once.  Remove one of them and replace the corresponding
                // load operand.
                //

                final Variable currentVariable = variable.value;
                final Variable nestedVariable = (Variable) e.getOperand();

                if (MetadataHelper.isSameType(currentVariable.getType(), nestedVariable.getType())) {
                    final List<Expression> currentLoads = loads.get(currentVariable);
                    final List<Expression> nestedLoads = loads.get(nestedVariable);

                    if (nestedVariable.isGenerated()) {
                        for (final Expression load : nestedLoads) {
                            load.setOperand(currentVariable);
                            currentLoads.add(load);
                            increment(loadCounts, currentVariable);
                        }

                        nestedLoads.clear();
                    }
                    else {
                        current.setOperand(nestedVariable);

                        for (final Expression load : currentLoads) {
                            load.setOperand(nestedVariable);
                            nestedLoads.add(load);
                            increment(loadCounts, nestedVariable);
                        }

                        currentLoads.clear();
                    }

                    final Expression nestedValue = single(e.getArguments());

                    current.getArguments().set(0, nestedValue);

                    return true;
                }
            }

            if (matchStore(e, variable, inlinedExpression)) {
                //
                // Check to see if we have an expression like 'x = y = <some expression>` followed by an
                // expression that loads 'y'.  If so, see if we can substitute 'x' for 'y' and remove 'y'.
                //

                final Expression loadThisInstead = new Expression(AstCode.Load, v, current.getOffset());

                if (inlineIfPossible(variable.get(), loadThisInstead, next, aggressive)) {
                    //
                    // Hoist the inner store up, clear the load/store counts for the removed variable,
                    // increment the load count for this variable.
                    //

                    current.getArguments().set(0, single(e.getArguments()));

                    storeCounts.get(variable.get()).setValue(0);
                    loadCounts.get(variable.get()).setValue(0);

                    increment(loadCounts, v);

                    return true;
                }
            }

            if (count(loadCounts, v) == 0 &&
                canInline(aggressive, v)) {

                //
                // The variable is never loaded.
                //
                if (hasNoSideEffect(e)) {
                    //
                    // Remove the expression completely.
                    //
                    body.remove(position);
                    return true;
                }

                if (canBeExpressionStatement(e)) {
                    //
                    // Assign the ranges of the Store instruction.
                    //
                    e.getRanges().addAll(current.getRanges());

                    //
                    // Remove the store, but keep the inner expression;
                    //
                    body.set(position, e);
                    return true;
                }
            }
        }

        return false;
    }

    private boolean canInline(final boolean aggressive, final Variable variable) {
        return aggressive ? notFromMetadata(variable) : variable.isGenerated();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Copy Propagation">

    @SuppressWarnings("ConstantConditions")
    final void copyPropagation() {
        for (final Block block : _method.getSelfAndChildrenRecursive(Block.class)) {
            final List<Node> body = block.getBody();

            final StrongBox<Variable> variable = new StrongBox<>();
            final StrongBox<Expression> copiedExpression = new StrongBox<>();

            for (int i = 0; i < body.size(); i++) {
                if (matchGetArgument(body.get(i), AstCode.Store, variable, copiedExpression) &&
                    !variable.get().isParameter() &&
                    count(storeCounts, variable.get()) == 1 &&
                    canPerformCopyPropagation(copiedExpression.get(), variable.get())) {

                    //
                    // Un-inline the arguments of the Load instruction.
                    //

                    final List<Expression> arguments = copiedExpression.get().getArguments();
                    final Variable[] uninlinedArgs = new Variable[arguments.size()];

                    for (int j = 0; j < uninlinedArgs.length; j++) {
                        final Variable newVariable = new Variable();

                        newVariable.setGenerated(true);
                        newVariable.setName(format("%s_cp_%d", variable.get().getName(), j));

                        uninlinedArgs[j] = newVariable;

                        body.add(i++, new Expression(AstCode.Store, uninlinedArgs[j], Expression.MYSTERY_OFFSET));
                    }

                    //
                    // Perform copy propagation.
                    //

                    for (final Expression expression : _method.getSelfAndChildrenRecursive(Expression.class)) {
                        if (expression.getCode().isLoad() &&
                            expression.getOperand() == variable.get()) {

                            //expression.setCode(copiedExpression.get().getCode());
                            expression.setOperand(copiedExpression.get().getOperand());

                            for (final Variable uninlinedArg : uninlinedArgs) {
                                expression.getArguments().add(new Expression(AstCode.Load, uninlinedArg, Expression.MYSTERY_OFFSET));
                            }
                        }
                    }

                    body.remove(i);

                    if (uninlinedArgs.length > 0) {
                        //
                        // If we un-inlined anything, we need to update the usage counters.
                        //
                        analyzeMethod();
                    }

                    //
                    // Inlining may be possible after removal of body.get(i).
                    //
                    inlineInto(body, i, _aggressive);

                    i -= uninlinedArgs.length + 1;
                }
            }
        }
    }

    final boolean canPerformCopyPropagation(final Expression expr, final Variable copyVariable) {
        switch (expr.getCode()) {
            case Load: {
                final Variable v = (Variable) expr.getOperand();

                if (v.isParameter()) {
                    //
                    // Parameters can be copied only if they aren't assigned to.
                    //
                    return count(storeCounts, v) == 0 &&
                           notFromMetadata(copyVariable);
                }

                //
                // Variables can be copied only if both the variable and the target copy variable are generated,
                // and if the variable has only a single assignment.
                //
                return v.isGenerated() &&
                       copyVariable.isGenerated() &&
                       count(storeCounts, v) == 1;
            }

            default: {
                return false;
            }
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Helper Methods">

    static boolean hasNoSideEffect(final Expression expression) {
        switch (expression.getCode()) {
            case Load:
//            case LoadElement:
            case AConstNull:
            case LdC:
                return true;

            case Add:
            case Sub:
            case Mul:
            case Div:
            case Rem:
            case Shl:
            case Shr:
            case UShr:
            case And:
            case Or:
            case Xor:
                return hasNoSideEffect(expression.getArguments().get(0)) &&
                       hasNoSideEffect(expression.getArguments().get(1));

            case Not:
            case Neg:
                return hasNoSideEffect(expression.getArguments().get(0));

//            case Store:
//                return hasNoSideEffect(expression.getArguments().get(0));

            default:
                return false;
        }
    }

    static boolean canBeExpressionStatement(final Expression expression) {
        switch (expression.getCode()) {
            case PutStatic:
            case PutField:
            case InvokeVirtual:
            case InvokeSpecial:
            case InvokeStatic:
            case InvokeInterface:
            case InvokeDynamic:
            case __New:
            case Store:
            case StoreElement:
            case Inc:
            case PreIncrement:
            case PostIncrement:
                return true;

            default:
                return false;
        }
    }

    static int count(final Map<Variable, MutableInteger> map, final Variable variable) {
        final MutableInteger count = map.get(variable);
        return count != null ? count.getValue() : 0;
    }

    private static void increment(final Map<Variable, MutableInteger> map, final Variable variable) {
        final MutableInteger count = map.get(variable);

        if (count == null) {
            map.put(variable, new MutableInteger(1));
        }
        else {
            count.increment();
        }
    }

    private static Iterable<Expression> getParents(final Expression scope, final Map<Expression, Expression> parentLookup, final Expression node) {
        return new Iterable<Expression>() {
            @NotNull
            @Override
            public final Iterator<Expression> iterator() {
                return new Iterator<Expression>() {
                    Expression current = updateCurrent(node);

                    @SuppressWarnings("unchecked")
                    private Expression updateCurrent(Expression node) {
                        if (node != null && node != Node.NULL) {
                            if (node == scope) {
                                return null;
                            }

                            node = parentLookup.get(node);

                            return node;
                        }

                        return null;
                    }

                    @Override
                    public final boolean hasNext() {
                        return current != null;
                    }

                    @Override
                    public final Expression next() {
                        final Expression next = current;

                        if (next == null) {
                            throw new NoSuchElementException();
                        }

                        current = updateCurrent(next);
                        return next;
                    }

                    @Override
                    public final void remove() {
                        throw ContractUtils.unsupported();
                    }
                };
            }
        };
    }

    // </editor-fold>
}
