/*
 * TypeAnalysis.java
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

import com.strobel.assembler.ir.attributes.AttributeNames;
import com.strobel.assembler.ir.attributes.SourceAttribute;
import com.strobel.assembler.metadata.*;
import com.strobel.core.CollectionUtilities;
import com.strobel.core.Predicate;
import com.strobel.core.StringComparison;
import com.strobel.core.StringUtilities;
import com.strobel.core.StrongBox;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.functions.Supplier;
import com.strobel.util.ContractUtils;

import java.util.*;

import static com.strobel.assembler.metadata.Flags.testAny;
import static com.strobel.core.CollectionUtilities.*;
import static com.strobel.decompiler.ast.PatternMatching.*;

public final class TypeAnalysis {
    private final static int FLAG_BOOLEAN_PROHIBITED = 0x01;

    private final List<ExpressionToInfer> _allExpressions = new ArrayList<>();
    private final Set<Variable> _singleStoreVariables = new LinkedHashSet<>();
    private final Set<Variable> _singleLoadVariables = new LinkedHashSet<>();
    private final Set<Variable> _allVariables = new LinkedHashSet<>();

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Map<Variable, List<ExpressionToInfer>> _assignmentExpressions = new LinkedHashMap<Variable, List<ExpressionToInfer>>() {
        @Override
        @SuppressWarnings("unchecked")
        public List<ExpressionToInfer> get(final Object key) {
            List<ExpressionToInfer> value = super.get(key);

            if (value == null) {
                if (_doneInitializing) {
                    return Collections.emptyList();
                }

                put((Variable) key, value = new ArrayList<>());
            }

            return value;
        }
    };

    private final Map<Variable, Set<TypeReference>> _previouslyInferred = new DefaultMap<>(CollectionUtilities.<TypeReference>setFactory());
    private final IdentityHashMap<Variable, TypeReference> _inferredVariableTypes = new IdentityHashMap<>();
    private final Stack<Expression> _stack = new Stack<>();

    private DecompilerContext _context;
    private CoreMetadataFactory _factory;
    private boolean _preserveMetadataTypes;
    private boolean _preserveMetadataGenericTypes;
    private boolean _doneInitializing;

    public static void run(final DecompilerContext context, final Block method) {
        final TypeAnalysis ta = new TypeAnalysis();

        final SourceAttribute localVariableTable = SourceAttribute.find(
            AttributeNames.LocalVariableTable,
            context.getCurrentMethod().getSourceAttributes()
        );

        final SourceAttribute localVariableTypeTable = SourceAttribute.find(
            AttributeNames.LocalVariableTypeTable,
            context.getCurrentMethod().getSourceAttributes()
        );

        ta._context = context;
        ta._factory = CoreMetadataFactory.make(context.getCurrentType(), context.getCurrentMethod());
        ta._preserveMetadataTypes = localVariableTable != null;
        ta._preserveMetadataGenericTypes = localVariableTypeTable != null;

        ta.createDependencyGraph(method);
        ta.identifySingleLoadVariables();
        ta._doneInitializing = true;
        ta.runInference();
    }

    public static void reset(final DecompilerContext context, final Block method) {
        final SourceAttribute localVariableTable = SourceAttribute.find(
            AttributeNames.LocalVariableTable,
            context.getCurrentMethod().getSourceAttributes()
        );

        final SourceAttribute localVariableTypeTable = SourceAttribute.find(
            AttributeNames.LocalVariableTypeTable,
            context.getCurrentMethod().getSourceAttributes()
        );

        final boolean preserveTypesFromMetadata = localVariableTable != null;
        final boolean preserveGenericTypesFromMetadata = localVariableTypeTable != null;

        for (final Expression e : method.getSelfAndChildrenRecursive(Expression.class)) {
            e.setInferredType(null);
            e.setExpectedType(null);

            final Object operand = e.getOperand();

            if (operand instanceof Variable) {
                final Variable variable = (Variable) operand;

                if (shouldResetVariableType(variable, preserveTypesFromMetadata, preserveGenericTypesFromMetadata)) {
                    variable.setType(null);
                }
            }
        }
    }

    private void createDependencyGraph(final Node node) {
        final StrongBox<Variable> v;

        if (node instanceof Condition) {
            ((Condition) node).getCondition().setExpectedType(BuiltinTypes.Boolean);
        }
        else if (node instanceof Loop &&
                 ((Loop) node).getCondition() != null) {

            ((Loop) node).getCondition().setExpectedType(BuiltinTypes.Boolean);
        }
        else if (node instanceof CatchBlock) {
            final CatchBlock catchBlock = (CatchBlock) node;

            if (catchBlock.getExceptionVariable() != null &&
                catchBlock.getExceptionType() != null &&
                catchBlock.getExceptionVariable().getType() == null) {

                catchBlock.getExceptionVariable().setType(catchBlock.getExceptionType());
            }
        }
        else if (node instanceof Expression) {
            final Expression expression = (Expression) node;
            final ExpressionToInfer expressionToInfer = new ExpressionToInfer();

            expressionToInfer.expression = expression;

            _allExpressions.add(expressionToInfer);

            findNestedAssignments(expression, expressionToInfer);

            if (expression.getCode().isStore()) {
                if (expression.getOperand() instanceof Variable &&
                    shouldInferVariableType((Variable) expression.getOperand())) {

                    _assignmentExpressions.get(expression.getOperand()).add(expressionToInfer);
                    _allVariables.add((Variable) expression.getOperand());
                }
                else if (matchLoad(expression.getArguments().get(0), v = new StrongBox<>()) &&
                         shouldInferVariableType(v.value)) {

                    _assignmentExpressions.get(v.value).add(expressionToInfer);
                    _allVariables.add(v.value);
                }
            }
        }
        else if (node instanceof Lambda) {
            final Lambda lambda = (Lambda) node;
            final List<Variable> parameters = lambda.getParameters();

            for (final Variable parameter : parameters) {
                _assignmentExpressions.get(parameter);
            }
        }

        for (final Node child : node.getChildren()) {
            createDependencyGraph(child);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void findNestedAssignments(final Expression expression, final ExpressionToInfer parent) {
        for (final Expression argument : expression.getArguments()) {
            final Object operand = argument.getOperand();

            if (operand instanceof Variable) {
                _allVariables.add((Variable) operand);
            }

            if (argument.getCode() == AstCode.Store) {
                final ExpressionToInfer expressionToInfer = new ExpressionToInfer();

                expressionToInfer.expression = argument;

                _allExpressions.add(expressionToInfer);

                final Variable variable = (Variable) operand;

                if (shouldInferVariableType(variable)) {
                    _assignmentExpressions.get(variable).add(expressionToInfer);
                    _allVariables.add(variable);

                    //
                    // The instruction that consumes the Store result is handled as if it was reading the variable.
                    //
                    parent.dependencies.add(variable);
                }
            }
            else if (argument.getCode() == AstCode.Inc) {
                final ExpressionToInfer expressionToInfer = new ExpressionToInfer();

                expressionToInfer.expression = argument;

                _allExpressions.add(expressionToInfer);

                final Variable variable = (Variable) operand;

                if (shouldInferVariableType(variable)) {
                    _assignmentExpressions.get(variable).add(expressionToInfer);
                    _allVariables.add(variable);

                    //
                    // The instruction that consumes the Store result is handled as if it was reading the variable.
                    //
                    parent.dependencies.add(variable);
                }
            }
            else if (argument.getCode() == AstCode.PreIncrement ||
                     argument.getCode() == AstCode.PostIncrement) {

                final ExpressionToInfer expressionToInfer = new ExpressionToInfer();

                expressionToInfer.expression = argument;

                _allExpressions.add(expressionToInfer);

                final Expression load = firstOrDefault(argument.getArguments());
                final StrongBox<Variable> variable = new StrongBox<>();

                if (load != null &&
                    matchLoadOrRet(load, variable) &&
                    shouldInferVariableType(variable.value)) {

                    _assignmentExpressions.get(variable.value).add(expressionToInfer);
                    _allVariables.add(variable.value);

                    //
                    // The instruction that consumes the Store result is handled as if it was reading the variable.
                    //
                    parent.dependencies.add(variable.value);
                }
            }
            else {
                final StrongBox<Variable> variable = new StrongBox<>();

                if (matchLoadOrRet(argument, variable) &&
                    shouldInferVariableType(variable.value)) {

                    parent.dependencies.add(variable.value);
                    _allVariables.add(variable.value);
                }
            }

            findNestedAssignments(argument, parent);
        }
    }

    private boolean isSingleStoreBoolean(final Variable variable) {
        if (_singleStoreVariables.contains(variable)) {
            final List<ExpressionToInfer> assignments = _assignmentExpressions.get(variable);
            final ExpressionToInfer e = single(assignments);
            return matchBooleanConstant(last(e.expression.getArguments())) != null;
        }
        return false;
    }

    private void identifySingleLoadVariables() {
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        final Map<Variable, List<ExpressionToInfer>> groupedExpressions = new DefaultMap<>(
            new Supplier<List<ExpressionToInfer>>() {
                @Override
                public List<ExpressionToInfer> get() {
                    return new ArrayList<>();
                }
            }
        );

        for (final ExpressionToInfer expressionToInfer : _allExpressions) {
            for (final Variable variable : expressionToInfer.dependencies) {
                groupedExpressions.get(variable).add(expressionToInfer);
            }
        }

        for (final Variable variable : groupedExpressions.keySet()) {
            final List<ExpressionToInfer> expressions = groupedExpressions.get(variable);

            if (expressions.size() == 1) {
                int references = 0;

                for (final Expression expression : expressions.get(0).expression.getSelfAndChildrenRecursive(Expression.class)) {
                    if (expression.getOperand() == variable &&
                        ++references > 1) {

                        break;
                    }
                }

                if (references == 1) {
                    _singleLoadVariables.add(variable);

                    //
                    // Mark the assignments as dependent on the type from the single load:
                    //
                    for (final ExpressionToInfer assignment : _assignmentExpressions.get(variable)) {
                        assignment.dependsOnSingleLoad = variable;
                    }
                }
            }
        }

        for (final Variable variable : _assignmentExpressions.keySet()) {
            if (_assignmentExpressions.get(variable).size() == 1) {
                _singleStoreVariables.add(variable);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void runInference() {
        _previouslyInferred.clear();
        _inferredVariableTypes.clear();

        int numberOfExpressionsAlreadyInferred = 0;

        //
        // Two flags that allow resolving cycles:
        //

        boolean ignoreSingleLoadDependencies = false;
        boolean assignVariableTypesBasedOnPartialInformation = false;

        final Predicate<Variable> dependentVariableTypesKnown = new Predicate<Variable>() {
            @Override
            public boolean test(final Variable v) {
                return inferTypeForVariable(v, null) != null || _singleLoadVariables.contains(v);
            }
        };

        while (numberOfExpressionsAlreadyInferred < _allExpressions.size()) {
            final int oldCount = numberOfExpressionsAlreadyInferred;

            for (final ExpressionToInfer e : _allExpressions) {
                if (!e.done &&
                    trueForAll(e.dependencies, dependentVariableTypesKnown) &&
                    (e.dependsOnSingleLoad == null || e.dependsOnSingleLoad.getType() != null || ignoreSingleLoadDependencies)) {

                    runInference(e.expression);
                    e.done = true;
                    numberOfExpressionsAlreadyInferred++;
                }
            }

            if (numberOfExpressionsAlreadyInferred == oldCount) {
                if (ignoreSingleLoadDependencies) {
                    if (assignVariableTypesBasedOnPartialInformation) {
                        throw new IllegalStateException("Could not infer any expression.");
                    }

                    assignVariableTypesBasedOnPartialInformation = true;
                }
                else {
                    //
                    // We have a cyclic dependency; we'll try to see if we can resolve it by ignoring single-load
                    // dependencies  This can happen if the variable was not actually assigned an expected type by
                    // the single-load instruction.
                    //
                    ignoreSingleLoadDependencies = true;
                    continue;
                }
            }
            else {
                assignVariableTypesBasedOnPartialInformation = false;
                ignoreSingleLoadDependencies = false;
            }

            //
            // Infer types for variables.
            //
            inferTypesForVariables(assignVariableTypesBasedOnPartialInformation);
        }

        verifyResults();
    }

    private void verifyResults() {
        final StrongBox<Expression> a = new StrongBox<>();

        for (final Variable variable : _allVariables) {
            final TypeReference type = variable.getType();

            if (type == null || type == BuiltinTypes.Null) {
                final TypeReference inferredType = inferTypeForVariable(variable, BuiltinTypes.Object);

                if (inferredType == null || inferredType == BuiltinTypes.Null) {
                    variable.setType(BuiltinTypes.Object);
                }
                else {
                    variable.setType(inferredType);
                }
            }
            else if (type.isWildcardType()) {
                variable.setType(MetadataHelper.getUpperBound(type));
            }
            else if (type.getSimpleType() == JvmType.Boolean) {
                //
                // Make sure constant assignments to boolean variables have boolean values,
                // and not integer values.
                //

                for (final ExpressionToInfer e : _assignmentExpressions.get(variable)) {
                    if (matchStore(e.expression, variable, a)) {
                        final Boolean booleanConstant = matchBooleanConstant(a.value);

                        if (booleanConstant != null) {
                            e.expression.setExpectedType(BuiltinTypes.Boolean);
                            e.expression.setInferredType(BuiltinTypes.Boolean);
                            a.value.setExpectedType(BuiltinTypes.Boolean);
                            a.value.setInferredType(BuiltinTypes.Boolean);
//                            a.value.setOperand(booleanConstant);
                        }
                    }
                }
            }
            else if (type.getSimpleType() == JvmType.Character) {
                //
                // Make sure constant assignments to boolean variables have boolean values,
                // and not integer values.
                //

                for (final ExpressionToInfer e : _assignmentExpressions.get(variable)) {
                    if (matchStore(e.expression, variable, a)) {
                        final Character characterConstant = matchCharacterConstant(a.value);

                        if (characterConstant != null) {
                            e.expression.setExpectedType(BuiltinTypes.Character);
                            e.expression.setInferredType(BuiltinTypes.Character);
                            a.value.setExpectedType(BuiltinTypes.Character);
                            a.value.setInferredType(BuiltinTypes.Character);
//                            a.value.setOperand(characterConstant);
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void inferTypesForVariables(final boolean assignVariableTypesBasedOnPartialInformation) {
        for (final Variable variable : _allVariables) {
            final List<ExpressionToInfer> expressionsToInfer = _assignmentExpressions.get(variable);

            boolean inferredFromNull = false;
            TypeReference inferredType = null;

            if (variable.isLambdaParameter()) {
                inferredType = _inferredVariableTypes.get(variable);

                if (inferredType == null) {
                    continue;
                }
            }
            else if (expressionsToInfer.isEmpty()) {
                continue;
            }
            else if (assignVariableTypesBasedOnPartialInformation ? anyDone(expressionsToInfer)
                                                                  : allDone(expressionsToInfer)) {

                for (final ExpressionToInfer e : expressionsToInfer) {
                    final List<Expression> arguments = e.expression.getArguments();

                    assert e.expression.getCode().isStore() && arguments.size() == 1 ||
                           e.expression.getCode() == AstCode.Inc ||
                           e.expression.getCode() == AstCode.PreIncrement ||
                           e.expression.getCode() == AstCode.PostIncrement;

                    final Expression assignedValue = arguments.get(0);

                    if (assignedValue.getInferredType() != null) {
                        if (inferredType == null) {
                            inferredType = adjustType(assignedValue.getInferredType(), e.flags);
                            inferredFromNull = match(assignedValue, AstCode.AConstNull);
                        }
                        else {
                            final TypeReference assigned = cleanTypeArguments(assignedValue.getInferredType(), inferredType);
                            final TypeReference commonSuper = adjustType(typeWithMoreInformation(inferredType, assigned), e.flags);

                            if (inferredFromNull &&
                                assigned != BuiltinTypes.Null &&
                                !MetadataHelper.isAssignableFrom(commonSuper, assigned)) {

                                //
                                // Maybe we had a null assignment that was incorrectly inferred as an
                                // a type incompatible with another assignment (e.g., assigned to null
                                // when int[] was expected, and later assigned with byte[]).
                                //

                                final TypeReference asSubType = MetadataHelper.asSubType(commonSuper, assigned);

                                inferredType = asSubType != null ? asSubType : assigned;
                                inferredFromNull = false;
                            }
                            else {
                                //
                                // Pick the common base type.
                                //
                                inferredType = commonSuper;
                            }
                        }
                    }
                }
            }
            else {
                continue;
            }

            if (inferredType == null) {
                inferredType = variable.getType();
            }
            else if (!inferredType.isUnbounded()) {
                inferredType = inferredType.hasSuperBound() ? inferredType.getSuperBound()
                                                            : inferredType.getExtendsBound();
            }

            if (shouldInferVariableType(variable) && inferredType != null) {
                variable.setType(inferredType);
                _inferredVariableTypes.put(variable, inferredType);

//                //
//                // Assign inferred type to all the assignments (in case they used different inferred types).
//                //
//                for (final ExpressionToInfer e : expressionsToInfer) {
//                    e.expression.setExpectedType(inferredType);
//                    e.expression.setInferredType(inferredType);
//                }

                //
                // Assign inferred types to all dependent expressions (in case they used different inferred types).
                //
                for (final ExpressionToInfer e : _allExpressions) {
                    if (e.dependencies.contains(variable) ||
                        expressionsToInfer.contains(e)) {

                        if (_stack.contains(e.expression)) {
                            continue;
                        }

                        boolean invalidate = false;

                        for (final Expression c : e.expression.getSelfAndChildrenRecursive(Expression.class)) {
                            if (_stack.contains(c)) {
                                continue;
                            }

                            c.setExpectedType(null);

                            if ((matchLoad(c, variable) || matchStore(c, variable)) &&
                                !MetadataHelper.isSameType(c.getInferredType(), inferredType)) {

                                c.setExpectedType(inferredType);
                            }

                            c.setInferredType(null);

                            invalidate = true;
                        }

                        if (invalidate) {
                            runInference(e.expression, e.flags);
                        }
                    }
                }
            }
        }
    }

    private boolean shouldInferVariableType(final Variable variable) {
        final VariableDefinition variableDefinition = variable.getOriginalVariable();

        if (variable.isGenerated() ||
            variable.isLambdaParameter()) {

            return true;
        }

        final ParameterDefinition parameter = variable.getOriginalParameter();

        if (parameter != null) {
            if (parameter == _context.getCurrentMethod().getBody().getThisParameter()) {
                return false;
            }

            final TypeReference parameterType = parameter.getParameterType();

            return !_preserveMetadataGenericTypes &&
                   (parameterType.isGenericType() || MetadataHelper.isRawType(parameterType));
        }

        //noinspection RedundantIfStatement
        if (variableDefinition != null &&
            variableDefinition.isFromMetadata() &&
            (variableDefinition.getVariableType().isGenericType() ? _preserveMetadataGenericTypes
                                                                  : _preserveMetadataTypes)) {

            return false;
        }

        return true;
    }

    private static boolean shouldResetVariableType(
        final Variable variable,
        final boolean preserveTypesFromMetadata,
        final boolean preserveGenericTypesFromMetadata) {

        if (variable.isGenerated() ||
            variable.isLambdaParameter()) {

            return true;
        }

        final VariableDefinition variableDefinition = variable.getOriginalVariable();

        //noinspection SimplifiableIfStatement
        if (variableDefinition != null &&
            variableDefinition.isFromMetadata() &&
            (variableDefinition.getVariableType().isGenericType() ? preserveGenericTypesFromMetadata
                                                                  : preserveTypesFromMetadata)) {

            return false;
        }

        return variableDefinition != null && variableDefinition.getVariableType() == BuiltinTypes.Integer ||
               variableDefinition != null && !variableDefinition.isTypeKnown();
    }

    private void runInference(final Expression expression) {
        runInference(expression, 0);
    }

    private void runInference(final Expression expression, final int flags) {
        final List<Expression> arguments = expression.getArguments();

        Variable changedVariable = null;
        boolean anyArgumentIsMissingExpectedType = false;

        for (final Expression argument : arguments) {
            if (argument.getExpectedType() == null) {
                anyArgumentIsMissingExpectedType = true;
                break;
            }
        }

        if (expression.getInferredType() == null || anyArgumentIsMissingExpectedType) {
            inferTypeForExpression(expression, expression.getExpectedType(), anyArgumentIsMissingExpectedType, flags);
        }
        else if (expression.getInferredType() == BuiltinTypes.Integer &&
                 expression.getExpectedType() == BuiltinTypes.Boolean) {

            if (expression.getCode() == AstCode.Load || expression.getCode() == AstCode.Store) {
                final Variable variable = (Variable) expression.getOperand();

                expression.setInferredType(BuiltinTypes.Boolean);

                if (variable.getType() == BuiltinTypes.Integer &&
                    shouldInferVariableType(variable)) {

                    variable.setType(BuiltinTypes.Boolean);
                    changedVariable = variable;
                }
            }
        }
        else if (expression.getInferredType() == BuiltinTypes.Integer &&
                 expression.getExpectedType() == BuiltinTypes.Character) {

            if (expression.getCode() == AstCode.Load || expression.getCode() == AstCode.Store) {
                final Variable variable = (Variable) expression.getOperand();

                expression.setInferredType(BuiltinTypes.Character);

                if (variable.getType() == BuiltinTypes.Integer &&
                    shouldInferVariableType(variable) &&
                    _singleLoadVariables.contains(variable)) {

                    variable.setType(BuiltinTypes.Character);
                    changedVariable = variable;
                }
            }
        }

        for (final Expression argument : arguments) {
            if (!argument.getCode().isStore()) {
                runInference(argument, flags);
            }
        }

        if (changedVariable != null) {
            if (_previouslyInferred.get(changedVariable).add(changedVariable.getType())) {
                invalidateDependentExpressions(expression, changedVariable);
            }
        }
    }

    private void invalidateDependentExpressions(final Expression expression, final Variable variable) {
        final List<ExpressionToInfer> assignments = _assignmentExpressions.get(variable);
        final TypeReference inferredType = _inferredVariableTypes.get(variable);

        for (final ExpressionToInfer e : _allExpressions) {
            if (e.expression != expression &&
                (e.dependencies.contains(variable) ||
                 assignments.contains(e))) {

                if (_stack.contains(e.expression)) {
                    continue;
                }

                boolean invalidate = false;

                for (final Expression c : e.expression.getSelfAndChildrenRecursive(Expression.class)) {
                    if (_stack.contains(c)) {
                        continue;
                    }

                    c.setExpectedType(null);

                    if ((matchLoad(c, variable) || matchStore(c, variable)) &&
                        !MetadataHelper.isSameType(c.getInferredType(), inferredType)) {

                        c.setExpectedType(inferredType);
                    }

                    c.setInferredType(null);

                    invalidate = true;
                }

                if (invalidate) {
//                    if (e.done) {
//                        --_numberOfExpressionsAlreadyInferred;
//                    }
//                    e.done = false;
                    runInference(e.expression, e.flags);
                }
            }
        }
    }

    private TypeReference inferTypeForExpression(final Expression expression, final TypeReference expectedType) {
        return inferTypeForExpression(expression, expectedType, 0);
    }

    private TypeReference inferTypeForExpression(final Expression expression, final TypeReference expectedType, final int flags) {
        return inferTypeForExpression(expression, expectedType, false, flags);
    }

    private TypeReference inferTypeForExpression(
        final Expression expression,
        final TypeReference expectedType,
        final boolean forceInferChildren) {

        return inferTypeForExpression(expression, expectedType, forceInferChildren, 0);
    }

    private TypeReference inferTypeForExpression(
        final Expression expression,
        final TypeReference expectedType,
        final boolean forceInferChildren,
        final int flags) {

        boolean actualForceInferChildren = forceInferChildren;

        if (expectedType != null &&
            !isSameType(expression.getExpectedType(), expectedType)) {

            expression.setExpectedType(expectedType);

            //
            // Store and Inc are special cases and never gets reevaluated.
            //
            if (!expression.getCode().isStore()) {
                actualForceInferChildren = true;
            }
        }

        if (actualForceInferChildren || expression.getInferredType() == null) {
            expression.setInferredType(doInferTypeForExpression(expression, expectedType, actualForceInferChildren, flags));
        }

        return expression.getInferredType();
    }

    @SuppressWarnings("ConstantConditions")
    private TypeReference doInferTypeForExpression(
        final Expression expression,
        final TypeReference expectedType,
        final boolean forceInferChildren,
        final int flags) {

        if (_stack.contains(expression) && !match(expression, AstCode.LdC)) {
            return expectedType;
        }

        _stack.push(expression);

        try {
            final AstCode code = expression.getCode();
            final Object operand = expression.getOperand();
            final List<Expression> arguments = expression.getArguments();

            switch (code) {
                case LogicalNot: {
                    if (forceInferChildren) {
                        inferTypeForExpression(arguments.get(0), BuiltinTypes.Boolean);
                    }

                    return BuiltinTypes.Boolean;
                }

                case LogicalAnd:
                case LogicalOr: {
                    if (forceInferChildren) {
                        inferTypeForExpression(arguments.get(0), BuiltinTypes.Boolean);
                        inferTypeForExpression(arguments.get(1), BuiltinTypes.Boolean);
                    }

                    return BuiltinTypes.Boolean;
                }

                case TernaryOp: {
                    if (forceInferChildren) {
                        inferTypeForExpression(arguments.get(0), BuiltinTypes.Boolean);
                    }

                    return inferBinaryArguments(
                        arguments.get(1),
                        arguments.get(2),
                        expectedType,
                        forceInferChildren,
                        null,
                        null,
                        0
                    );
                }

                case MonitorEnter:
                case MonitorExit:
                    return null;

                case Store: {
                    final Variable v = (Variable) operand;
                    final TypeReference lastInferredType = _inferredVariableTypes.get(v);

                    if (matchBooleanConstant(expression.getArguments().get(0)) != null &&
                        shouldInferVariableType(v) &&
                        isBoolean(inferTypeForVariable(v, expectedType != null ? expectedType : BuiltinTypes.Boolean, true, flags))) {

                        return BuiltinTypes.Boolean;
                    }

                    if (forceInferChildren || lastInferredType == null && v.getType() == null) {
                        //
                        // NOTE: Do not use 'expectedType' here!
                        //
                        TypeReference inferredType = inferTypeForExpression(
                            expression.getArguments().get(0),
                            inferTypeForVariable(v, null, flags),
                            flags
                        );

                        if (inferredType != null && inferredType.isWildcardType()) {
                            inferredType = MetadataHelper.getUpperBound(inferredType);
                        }

                        if (inferredType != null) {
                            return adjustType(inferredType, flags);
                        }
                    }

                    return adjustType(lastInferredType != null ? lastInferredType : v.getType(), flags);
                }

                case Load: {
                    final Variable v = (Variable) expression.getOperand();
                    final TypeReference inferredType = inferTypeForVariable(v, expectedType, flags);
                    final TypeDefinition thisType = _context.getCurrentType();

                    if (v.isParameter() &&
                        v.getOriginalParameter() == _context.getCurrentMethod().getBody().getThisParameter()) {

                        if (_singleLoadVariables.contains(v) && v.getType() == null) {
                            v.setType(thisType);
                        }

                        return thisType;
                    }

                    TypeReference result = inferredType;

                    if (expectedType != null &&
                        expectedType != BuiltinTypes.Null &&
                        shouldInferVariableType(v)) {

                        TypeReference tempResult;

                        if (MetadataHelper.isSubType(inferredType, expectedType)) {
                            tempResult = inferredType;
                        }
                        else {
                            tempResult = MetadataHelper.asSubType(inferredType, expectedType);
                        }

                        if (tempResult != null &&
                            tempResult.containsGenericParameters()) {

                            final Map<TypeReference, TypeReference> mappings = MetadataHelper.adapt(tempResult, inferredType);

                            List<TypeReference> mappingsToRemove = null;

                            for (final TypeReference key : mappings.keySet()) {
                                final GenericParameter gp = _context.getCurrentMethod().findTypeVariable(key.getSimpleName());

                                if (MetadataHelper.isSameType(gp, key, true)) {
                                    if (mappingsToRemove == null) {
                                        mappingsToRemove = new ArrayList<>();
                                    }
                                    mappingsToRemove.add(key);
                                }
                            }

                            if (mappingsToRemove != null) {
                                mappings.keySet().removeAll(mappingsToRemove);
                            }

                            if (!mappings.isEmpty()) {
                                tempResult = TypeSubstitutionVisitor.instance().visit(tempResult, mappings);
                            }
                        }

                        if (tempResult == null && v.getType() != null) {
                            tempResult = MetadataHelper.asSubType(v.getType(), expectedType);

                            if (tempResult == null) {
                                tempResult = MetadataHelper.asSubType(MetadataHelper.eraseRecursive(v.getType()), expectedType);
                            }
                        }

                        if (tempResult == null) {
                            tempResult = expectedType;
                        }

                        result = tempResult;

                        if (result.isGenericType()) {
                            if (expectedType.isGenericDefinition() && !result.isGenericDefinition()) {
                                result = result.getUnderlyingType();
                            }
                            if (MetadataHelper.areGenericsSupported(thisType)) {
                                if (MetadataHelper.getUnboundGenericParameterCount(result) > 0) {
                                    result = MetadataHelper.substituteGenericArguments(result, inferredType);
                                }
                            }
                        }

                        if (result.isGenericDefinition() && !MetadataHelper.canReferenceTypeVariablesOf(result, _context.getCurrentType())) {
                            result = new RawType(result.getUnderlyingType());
                        }
                    }

                    final List<ExpressionToInfer> assignments = _assignmentExpressions.get(v);

                    if (result == null && assignments.isEmpty()) {
                        result = BuiltinTypes.Object;
                    }

                    if (result != null && result.isWildcardType()) {
                        result = MetadataHelper.getUpperBound(result);
                    }

                    result = adjustType(result, flags);

                    if (flags != 0) {
                        for (int i = 0; i < assignments.size(); i++) {
                            assignments.get(i).flags |= flags;
                        }
                    }

                    _inferredVariableTypes.put(v, result);

                    if (result != null &&
                        !MetadataHelper.isSameType(result, inferredType) &&
                        _previouslyInferred.get(v).add(result)) {

                        expression.setInferredType(result);
                        invalidateDependentExpressions(expression, v);
                    }

                    if (_singleLoadVariables.contains(v) && v.getType() == null) {
                        v.setType(result);
                    }

                    return result;
                }

                case InvokeDynamic: {
                    return inferDynamicCall(expression, expectedType, forceInferChildren);
                }

                case InvokeVirtual:
                case InvokeSpecial:
                case InvokeStatic:
                case InvokeInterface: {
                    return inferCall(expression, expectedType, forceInferChildren);
                }

                case GetField: {
                    final FieldReference field = (FieldReference) operand;

                    if (forceInferChildren) {
                        final FieldDefinition resolvedField = field.resolve();
                        final FieldReference effectiveField = resolvedField != null ? resolvedField : field;
                        final TypeReference targetType = inferTypeForExpression(arguments.get(0), field.getDeclaringType());

                        if (targetType != null) {
                            final FieldReference asMember = MetadataHelper.asMemberOf(effectiveField, targetType);

                            return asMember.getFieldType();
                        }
                    }

                    return getFieldType((FieldReference) operand);
                }

                case GetStatic: {
                    return getFieldType((FieldReference) operand);
                }

                case PutField: {
                    if (forceInferChildren) {
                        inferTypeForExpression(
                            arguments.get(0),
                            ((FieldReference) operand).getDeclaringType()
                        );

                        inferTypeForExpression(
                            arguments.get(1),
                            getFieldType((FieldReference) operand)
                        );
                    }

                    return getFieldType((FieldReference) operand);
                }

                case PutStatic: {
                    if (forceInferChildren) {
                        inferTypeForExpression(
                            arguments.get(0),
                            getFieldType((FieldReference) operand)
                        );
                    }

                    return getFieldType((FieldReference) operand);
                }

                case __New: {
                    return (TypeReference) operand;
                }

                case PreIncrement:
                case PostIncrement: {
                    final TypeReference inferredType = inferTypeForExpression(
                        arguments.get(0),
                        null,
                        flags | FLAG_BOOLEAN_PROHIBITED
                    );

                    if (inferredType == null || inferredType == BuiltinTypes.Boolean) {
                        final Number n = (Number) operand;

                        if (n instanceof Long) {
                            return BuiltinTypes.Long;
                        }

                        return BuiltinTypes.Integer;
                    }

                    return inferredType;
                }

                case Not:
                case Neg: {
                    return inferTypeForExpression(arguments.get(0), expectedType);
                }

                case Add:
                case Sub:
                case Mul:
                case Or:
                case And:
                case Xor:
                case Div:
                case Rem: {
                    return inferBinaryExpression(code, arguments, flags);
                }

                case Shl: {
                    if (forceInferChildren) {
                        inferTypeForExpression(
                            arguments.get(1),
                            BuiltinTypes.Integer,
                            flags | FLAG_BOOLEAN_PROHIBITED
                        );
                    }

                    if (expectedType != null &&
                        (expectedType.getSimpleType() == JvmType.Integer ||
                         expectedType.getSimpleType() == JvmType.Long)) {

                        return doBinaryNumericPromotion(
                            inferTypeForExpression(
                                arguments.get(0),
                                expectedType,
                                flags | FLAG_BOOLEAN_PROHIBITED
                            )
                        );
                    }

                    return doBinaryNumericPromotion(
                        inferTypeForExpression(
                            arguments.get(0),
                            null,
                            flags | FLAG_BOOLEAN_PROHIBITED
                        )
                    );
                }

                case Shr:
                case UShr: {
                    if (forceInferChildren) {
                        inferTypeForExpression(arguments.get(1), BuiltinTypes.Integer, flags | FLAG_BOOLEAN_PROHIBITED);
                    }

                    final TypeReference type = doBinaryNumericPromotion(
                        inferTypeForExpression(
                            arguments.get(0),
                            null,
                            flags | FLAG_BOOLEAN_PROHIBITED
                        )
                    );

                    if (type == null) {
                        return null;
                    }

                    TypeReference expectedInputType = null;

                    switch (type.getSimpleType()) {
                        case Integer:
                            expectedInputType = BuiltinTypes.Integer;
                            break;
                        case Long:
                            expectedInputType = BuiltinTypes.Long;
                            break;
                    }

                    if (expectedInputType != null) {
                        inferTypeForExpression(arguments.get(0), expectedInputType);
                        return expectedInputType;
                    }

                    return type;
                }

                case CompoundAssignment: {
                    final Expression op = arguments.get(0);
                    final TypeReference targetType = inferTypeForExpression(op.getArguments().get(0), null);

                    if (forceInferChildren) {
                        inferTypeForExpression(arguments.get(0), targetType);
                    }

                    return targetType;
                }

                case AConstNull: {
                    if (expectedType != null && !expectedType.isPrimitive()) {
                        return expectedType;
                    }

                    return BuiltinTypes.Null;
                }

                case LdC: {
                    if (operand instanceof Boolean &&
                        matchBooleanConstant(expression) != null &&
                        !testAny(flags, FLAG_BOOLEAN_PROHIBITED)) {

                        return BuiltinTypes.Boolean;
                    }

                    if (operand instanceof Character && matchCharacterConstant(expression) != null) {
                        return BuiltinTypes.Character;
                    }

                    if (operand instanceof Number) {
                        final Number number = (Number) operand;

                        if (number instanceof Integer) {
                            if (expectedType != null) {
                                switch (expectedType.getSimpleType()) {
                                    case Boolean:
                                        if (number.intValue() == 0 || number.intValue() == 1) {
                                            return adjustType(BuiltinTypes.Boolean, flags);
                                        }
                                        return BuiltinTypes.Integer;

                                    case Byte:
                                        if (number.intValue() >= Byte.MIN_VALUE &&
                                            number.intValue() <= Byte.MAX_VALUE) {

                                            return BuiltinTypes.Byte;
                                        }
                                        return BuiltinTypes.Integer;

                                    case Character:
                                        if (number.intValue() >= Character.MIN_VALUE &&
                                            number.intValue() <= Character.MAX_VALUE) {

                                            return BuiltinTypes.Character;
                                        }
                                        return BuiltinTypes.Integer;

                                    case Short:
                                        if (number.intValue() >= Short.MIN_VALUE &&
                                            number.intValue() <= Short.MAX_VALUE) {

                                            return BuiltinTypes.Short;
                                        }
                                        return BuiltinTypes.Integer;
                                }
                            }
                            else if (matchBooleanConstant(expression) != null) {
                                return adjustType(BuiltinTypes.Boolean, flags);
                            }

                            return BuiltinTypes.Integer;
                        }

                        if (number instanceof Long) {
                            return BuiltinTypes.Long;
                        }

                        if (number instanceof Float) {
                            return BuiltinTypes.Float;
                        }

                        return BuiltinTypes.Double;
                    }

                    if (operand instanceof TypeReference) {
                        return _factory.makeParameterizedType(
                            _factory.makeNamedType("java.lang.Class"),
                            null,
                            (TypeReference) operand
                        );
                    }

                    return _factory.makeNamedType("java.lang.String");
                }

                case NewArray:
                case __NewArray:
                case __ANewArray: {
                    if (forceInferChildren) {
                        inferTypeForExpression(arguments.get(0), BuiltinTypes.Integer, flags | FLAG_BOOLEAN_PROHIBITED);
                    }
                    return ((TypeReference) operand).makeArrayType();
                }

                case MultiANewArray: {
                    if (forceInferChildren) {
                        for (int i = 0; i < arguments.size(); i++) {
                            inferTypeForExpression(arguments.get(i), BuiltinTypes.Integer, flags | FLAG_BOOLEAN_PROHIBITED);
                        }
                    }
                    return (TypeReference) operand;
                }

                case InitObject: {
                    return inferInitObject(expression, expectedType, forceInferChildren, (MethodReference) operand, arguments);
                }

                case InitArray: {
                    final TypeReference arrayType = (TypeReference) operand;
                    final TypeReference elementType = arrayType.getElementType();

                    if (forceInferChildren) {
                        for (final Expression argument : arguments) {
                            inferTypeForExpression(argument, elementType);
                        }
                    }

                    return arrayType;
                }

                case ArrayLength: {
                    return BuiltinTypes.Integer;
                }

                case LoadElement: {
//                    final TypeReference expectedArrayType = expectedType != null ? expectedType.makeArrayType() : null;
                    final TypeReference arrayType = inferTypeForExpression(arguments.get(0), /*expectedArrayType*/null);

                    inferTypeForExpression(arguments.get(1), BuiltinTypes.Integer, flags | FLAG_BOOLEAN_PROHIBITED);

                    if (arrayType != null && arrayType.isArray()) {
                        return arrayType.getElementType();
                    }

//                    if (expectedType != null) {
//                        return expectedType;
//                    }

                    return null;
                }

                case StoreElement: {
//                    final TypeReference expectedArrayType = expectedType != null ? expectedType.makeArrayType() : null;
                    final TypeReference arrayType = inferTypeForExpression(arguments.get(0), /*expectedArrayType*/null);

                    inferTypeForExpression(arguments.get(1), BuiltinTypes.Integer, flags | FLAG_BOOLEAN_PROHIBITED);

                    final TypeReference expectedElementType;

                    if (arrayType != null && arrayType.isArray()) {
                        expectedElementType = arrayType.getElementType();
                    }
//                    else if (expectedArrayType != null) {
//                        expectedElementType = expectedArrayType.getElementType();
//                    }
                    else {
                        expectedElementType = null;
                    }

                    if (forceInferChildren) {
                        inferTypeForExpression(arguments.get(2), expectedElementType);
                    }

                    return expectedElementType;
                }

                case __BIPush:
                case __SIPush: {
                    final Number number = (Number) operand;

                    if (expectedType != null) {
                        if (expectedType.getSimpleType() == JvmType.Boolean &&
                            (number.intValue() == 0 || number.intValue() == 1)) {

                            return BuiltinTypes.Boolean;
                        }

                        if (expectedType.getSimpleType() == JvmType.Byte &&
                            number.intValue() >= Byte.MIN_VALUE &&
                            number.intValue() <= Byte.MAX_VALUE) {

                            return BuiltinTypes.Byte;
                        }

                        if (expectedType.getSimpleType() == JvmType.Character &&
                            number.intValue() >= Character.MIN_VALUE &&
                            number.intValue() <= Character.MAX_VALUE) {

                            return BuiltinTypes.Character;
                        }

                        if (expectedType.getSimpleType().isIntegral()) {
                            return expectedType;
                        }
                    }
                    else if (code == AstCode.__BIPush) {
                        return BuiltinTypes.Byte;
                    }

                    return BuiltinTypes.Short;
                }

                case I2L:
                case I2F:
                case I2D:
                case L2I:
                case L2F:
                case L2D:
                case F2I:
                case F2L:
                case F2D:
                case D2I:
                case D2L:
                case D2F:
                case I2B:
                case I2C:
                case I2S: {
                    final TypeReference expectedArgumentType;
                    final TypeReference conversionResult;

                    switch (code) {
                        case I2L:
                            conversionResult = BuiltinTypes.Long;
                            expectedArgumentType = BuiltinTypes.Integer;
                            break;
                        case I2F:
                            conversionResult = BuiltinTypes.Float;
                            expectedArgumentType = BuiltinTypes.Integer;
                            break;
                        case I2D:
                            conversionResult = BuiltinTypes.Double;
                            expectedArgumentType = BuiltinTypes.Integer;
                            break;
                        case L2I:
                            conversionResult = BuiltinTypes.Integer;
                            expectedArgumentType = BuiltinTypes.Long;
                            break;
                        case L2F:
                            conversionResult = BuiltinTypes.Float;
                            expectedArgumentType = BuiltinTypes.Long;
                            break;
                        case L2D:
                            conversionResult = BuiltinTypes.Double;
                            expectedArgumentType = BuiltinTypes.Long;
                            break;
                        case F2I:
                            conversionResult = BuiltinTypes.Integer;
                            expectedArgumentType = BuiltinTypes.Float;
                            break;
                        case F2L:
                            conversionResult = BuiltinTypes.Long;
                            expectedArgumentType = BuiltinTypes.Float;
                            break;
                        case F2D:
                            conversionResult = BuiltinTypes.Double;
                            expectedArgumentType = BuiltinTypes.Float;
                            break;
                        case D2I:
                            conversionResult = BuiltinTypes.Integer;
                            expectedArgumentType = BuiltinTypes.Double;
                            break;
                        case D2L:
                            conversionResult = BuiltinTypes.Long;
                            expectedArgumentType = BuiltinTypes.Double;
                            break;
                        case D2F:
                            conversionResult = BuiltinTypes.Float;
                            expectedArgumentType = BuiltinTypes.Double;
                            break;
                        case I2B:
                            conversionResult = BuiltinTypes.Byte;
                            expectedArgumentType = BuiltinTypes.Integer;
                            break;
                        case I2C:
                            conversionResult = BuiltinTypes.Character;
                            expectedArgumentType = BuiltinTypes.Integer;
                            break;
                        case I2S:
                            conversionResult = BuiltinTypes.Short;
                            expectedArgumentType = BuiltinTypes.Integer;
                            break;
                        default:
                            throw ContractUtils.unsupported();
                    }

                    arguments.get(0).setExpectedType(expectedArgumentType);
                    return conversionResult;
                }

                case CheckCast:
                case Unbox: {
                    if (expectedType != null) {
                        final TypeReference castType = (TypeReference) operand;

                        TypeReference inferredType = MetadataHelper.asSubType(castType, expectedType);

                        if (forceInferChildren) {
                            inferredType = inferTypeForExpression(
                                arguments.get(0),
                                inferredType != null ? inferredType
                                                     : (TypeReference) operand
                            );
                        }

                        if (inferredType != null && MetadataHelper.isSubType(inferredType, MetadataHelper.eraseRecursive(castType))) {
                            expression.setOperand(inferredType);
                            return inferredType;
                        }
                    }
                    return (TypeReference) operand;
                }

                case Box: {
                    final TypeReference type = (TypeReference) operand;

                    if (forceInferChildren) {
                        inferTypeForExpression(arguments.get(0), type);
                    }

                    return type.isPrimitive() ? BuiltinTypes.Object : type;
                }

                case CmpEq:
                case CmpNe:
                case CmpLt:
                case CmpGe:
                case CmpGt:
                case CmpLe: {
                    if (forceInferChildren) {
                        return inferBinaryExpression(code, arguments, flags);
                    }

                    return BuiltinTypes.Boolean;
                }

                case __DCmpG:
                case __DCmpL:
                case __FCmpG:
                case __FCmpL:
                case __LCmp: {
                    if (forceInferChildren) {
                        return inferBinaryExpression(code, arguments, flags);
                    }

                    return BuiltinTypes.Integer;
                }

                case IfTrue: {
                    if (forceInferChildren) {
                        inferTypeForExpression(arguments.get(0), BuiltinTypes.Boolean, true);
                    }
                    return null;
                }

                case Goto:
                case Switch:
                case AThrow:
                case LoopOrSwitchBreak:
                case LoopContinue:
                case __Return: {
                    return null;
                }

                case __IReturn:
                case __LReturn:
                case __FReturn:
                case __DReturn:
                case __AReturn:
                case Return: {
                    final Expression lambdaBinding = expression.getUserData(AstKeys.PARENT_LAMBDA_BINDING);

                    if (lambdaBinding != null) {
                        final Lambda lambda = (Lambda) lambdaBinding.getOperand();
                        final MethodReference method = lambda.getMethod();

                        if (method == null) {
                            return null;
                        }

                        final TypeReference oldInferredType = lambda.getInferredReturnType();

                        TypeReference inferredType = expectedType;

                        TypeReference returnType = oldInferredType != null ? oldInferredType
                                                                           : expectedType;

                        if (forceInferChildren) {
                            if (returnType == null) {
                                returnType = lambda.getMethod().getReturnType();
                            }

                            if (returnType.containsGenericParameters()) {
                                Map<TypeReference, TypeReference> mappings = null;
                                TypeReference declaringType = method.getDeclaringType();

                                if (declaringType.isGenericType()) {
                                    for (final GenericParameter gp : declaringType.getGenericParameters()) {
                                        final GenericParameter inScope = _context.getCurrentMethod().findTypeVariable(gp.getName());

                                        if (inScope != null && MetadataHelper.isSameType(gp, inScope)) {
                                            continue;
                                        }

                                        if (mappings == null) {
                                            mappings = new HashMap<>();
                                        }

                                        if (!mappings.containsKey(gp)) {
                                            mappings.put(gp, MetadataHelper.eraseRecursive(gp));
                                        }
                                    }

                                    if (mappings != null) {
                                        declaringType = TypeSubstitutionVisitor.instance().visit(declaringType, mappings);

                                        if (declaringType != null) {
                                            final MethodReference boundMethod = MetadataHelper.asMemberOf(
                                                method,
                                                declaringType
                                            );

                                            if (boundMethod != null) {
                                                returnType = boundMethod.getReturnType();
                                            }
                                        }
                                    }
                                }
                            }

                            if (!arguments.isEmpty() && returnType != BuiltinTypes.Void) {
                                inferredType = inferTypeForExpression(arguments.get(0), returnType);
                            }

                            if (oldInferredType != null && inferredType != BuiltinTypes.Void) {
                                final TypeReference newInferredType = MetadataHelper.asSuper(
                                    inferredType,
                                    oldInferredType
                                );

                                if (newInferredType != null) {
                                    inferredType = newInferredType;
                                }
                            }
                        }

                        lambda.setExpectedReturnType(returnType);
                        lambda.setInferredReturnType(inferredType);

                        return inferredType;
                    }

                    final TypeReference returnType = _context.getCurrentMethod().getReturnType();

                    if (forceInferChildren && arguments.size() == 1) {
                        inferTypeForExpression(arguments.get(0), returnType, true);
                    }

                    return returnType;
                }

                case Bind: {
                    final Lambda lambda = (Lambda) expression.getOperand();

                    if (lambda == null) {
                        return null;
                    }

                    final MethodReference method = lambda.getMethod();
                    final List<Variable> parameters = lambda.getParameters();

                    TypeReference functionType = lambda.getFunctionType();

                    if (functionType != null && expectedType != null) {
                        final TypeReference asSubType = MetadataHelper.asSubType(functionType, expectedType);

                        if (asSubType != null) {
                            functionType = asSubType;
                        }
                    }

                    MethodReference boundMethod = MetadataHelper.asMemberOf(method, functionType);

                    if (boundMethod == null) {
                        boundMethod = method;
                    }

                    List<ParameterDefinition> methodParameters = boundMethod.getParameters();

                    final int argumentCount = Math.min(arguments.size(), methodParameters.size());

                    TypeReference inferredReturnType = null;

                    if (forceInferChildren) {
                        for (int i = 0; i < argumentCount; i++) {
                            final Expression argument = arguments.get(i);

                            inferTypeForExpression(
                                argument,
                                methodParameters.get(i).getParameterType()
                            );
                        }

                        final List<Variable> lambdaParameters = lambda.getParameters();

                        for (int i = 0, n = lambdaParameters.size(); i < n; i++) {
                            invalidateDependentExpressions(expression, lambdaParameters.get(i));
                        }

                        for (final Expression e : lambda.getChildrenAndSelfRecursive(Expression.class)) {
                            if (match(e, AstCode.Return)) {
                                runInference(e);

                                if (e.getInferredType() != null) {
                                    if (inferredReturnType != null) {
                                        inferredReturnType = MetadataHelper.asSuper(e.getInferredType(), inferredReturnType);
                                    }
                                    else {
                                        inferredReturnType = e.getInferredType();
                                    }
                                }
                            }
                        }
                    }

                    final MethodDefinition r = boundMethod.resolve();

                    if (functionType.containsGenericParameters() && boundMethod.containsGenericParameters() ||
                        r != null && r.getDeclaringType().containsGenericParameters() && r.containsGenericParameters()) {

                        final Map<TypeReference, TypeReference> mappings;
                        final Map<TypeReference, TypeReference> oldMappings = new HashMap<>();
                        final Map<TypeReference, TypeReference> newMappings = new HashMap<>();

                        final List<ParameterDefinition> p = boundMethod.getParameters();
                        final List<ParameterDefinition> rp = r != null ? r.getParameters() : method.getParameters();

                        final TypeReference returnType = r != null ? r.getReturnType()
                                                                   : method.getReturnType();

                        if (inferredReturnType != null) {
                            if (returnType.isGenericParameter()) {
                                final TypeReference boundReturnType = ensureReferenceType(inferredReturnType);

                                if (!MetadataHelper.isSameType(boundReturnType, returnType)) {
                                    newMappings.put(returnType, boundReturnType);
                                }
                            }
                            else if (returnType.containsGenericParameters()) {
                                final Map<TypeReference, TypeReference> returnMappings = new HashMap<>();

                                new AddMappingsForArgumentVisitor(returnType).visit(
                                    inferredReturnType,
                                    returnMappings
                                );

                                newMappings.putAll(returnMappings);
                            }
                        }

                        for (int i = 0, j = Math.max(0, parameters.size() - arguments.size()); i < arguments.size(); i++, j++) {
                            final Expression argument = arguments.get(i);
                            final TypeReference rType = rp.get(j).getParameterType();
                            final TypeReference pType = p.get(j).getParameterType();
                            final TypeReference aType = argument.getInferredType();

                            if (pType != null && rType.containsGenericParameters()) {
                                new AddMappingsForArgumentVisitor(pType).visit(rType, oldMappings);
                            }

                            if (aType != null && rType.containsGenericParameters()) {
                                new AddMappingsForArgumentVisitor(aType).visit(rType, newMappings);
                            }
                        }

                        mappings = oldMappings;

                        if (!newMappings.isEmpty()) {
                            for (final TypeReference t : newMappings.keySet()) {
                                final TypeReference oldMapping = oldMappings.get(t);
                                final TypeReference newMapping = newMappings.get(t);

                                if (oldMapping == null || MetadataHelper.isSubType(newMapping, oldMapping)) {
                                    mappings.put(t, newMapping);
                                }
                            }
                        }

                        if (!mappings.isEmpty()) {
                            final TypeReference declaringType = (r != null ? r : method).getDeclaringType();

                            TypeReference boundDeclaringType = TypeSubstitutionVisitor.instance().visit(declaringType, mappings);

                            if (boundDeclaringType != null && boundDeclaringType.isGenericType()) {
                                for (final GenericParameter gp : boundDeclaringType.getGenericParameters()) {
                                    final GenericParameter inScope = _context.getCurrentMethod().findTypeVariable(gp.getName());

                                    if (inScope != null && MetadataHelper.isSameType(gp, inScope)) {
                                        continue;
                                    }

                                    if (!mappings.containsKey(gp)) {
                                        mappings.put(gp, MetadataHelper.eraseRecursive(gp));
                                    }
                                }

                                boundDeclaringType = TypeSubstitutionVisitor.instance().visit(boundDeclaringType, mappings);
                            }

                            if (boundDeclaringType != null) {
                                functionType = boundDeclaringType;
                            }

                            final MethodReference newBoundMethod = MetadataHelper.asMemberOf(boundMethod, boundDeclaringType);

                            if (newBoundMethod != null) {
                                boundMethod = newBoundMethod;
                                lambda.setMethod(boundMethod);
                                methodParameters = boundMethod.getParameters();
                            }
                        }

                        for (int i = 0; i < methodParameters.size(); i++) {
                            final Variable variable = parameters.get(i);
                            final TypeReference variableType = methodParameters.get(i).getParameterType();
                            final TypeReference oldVariableType = variable.getType();

                            if (oldVariableType == null || !MetadataHelper.isSameType(variableType, oldVariableType)) {
                                invalidateDependentExpressions(expression, variable);
                            }
                        }
                    }

                    return functionType;
                }

                case Jsr: {
                    return BuiltinTypes.Integer;
                }

                case Ret: {
                    if (forceInferChildren) {
                        inferTypeForExpression(arguments.get(0), BuiltinTypes.Integer);
                    }
                    return null;
                }

                case Pop:
                case Pop2: {
                    return null;
                }

                case Dup:
                case Dup2: {
                    //
                    // TODO: Handle the more obscure DUP instructions.
                    //

                    final Expression argument = arguments.get(0);
                    final TypeReference result = inferTypeForExpression(argument, expectedType);

                    argument.setExpectedType(result);

                    return result;
                }

                case InstanceOf: {
                    return BuiltinTypes.Boolean;
                }

                case __IInc:
                case __IIncW:
                case Inc: {
                    final TypeReference inferredType = inferTypeForVariable(
                        (Variable) operand,
                        BuiltinTypes.Integer,
                        flags | FLAG_BOOLEAN_PROHIBITED
                    );

                    if (forceInferChildren) {
                        inferTypeForExpression(arguments.get(0), inferredType, true);
                    }

                    return inferredType;
                }

                case Leave:
                case EndFinally:
                case Nop: {
                    return null;
                }

                case DefaultValue: {
                    return (TypeReference) expression.getOperand();
                }

                default: {
                    System.err.printf("Type inference can't handle opcode '%s'.\n", code.getName());
                    return null;
                }
            }
        }
        finally {
            _stack.pop();
        }
    }

    @SuppressWarnings("ConstantConditions")
    private TypeReference inferInitObject(
        final Expression expression,
        final TypeReference expectedType,
        final boolean forceInferChildren,
        final MethodReference operand,
        final List<Expression> arguments) {

        final MethodReference resolvedCtor = operand instanceof IGenericInstance ? operand.resolve() : operand;
        final MethodReference constructor = resolvedCtor != null ? resolvedCtor : operand;
        final TypeReference type = constructor.getDeclaringType();

        TypeReference inferredType;

        if (expectedType != null && !MetadataHelper.isSameType(expectedType, BuiltinTypes.Object)) {
            final TypeReference asSubType = MetadataHelper.asSubType(type, expectedType);
            inferredType = asSubType != null ? asSubType : type;
        }
        else {
            inferredType = type;
        }

        final Map<TypeReference, TypeReference> mappings;

        if (inferredType.isGenericDefinition()) {
            mappings = new HashMap<>();

            for (final GenericParameter gp : inferredType.getGenericParameters()) {
                mappings.put(gp, MetadataHelper.eraseRecursive(gp));
            }
        }
        else {
            mappings = Collections.emptyMap();
        }

        if (forceInferChildren) {
            final MethodReference asMember = MetadataHelper.asMemberOf(
                constructor,
                TypeSubstitutionVisitor.instance().visit(inferredType, mappings)
            );

            final List<ParameterDefinition> parameters = asMember.getParameters();

            for (int i = 0; i < arguments.size() && i < parameters.size(); i++) {
                inferTypeForExpression(
                    arguments.get(i),
                    parameters.get(i).getParameterType()
                );
            }

            expression.setOperand(asMember);
        }

        if (inferredType == null) {
            return type;
        }

        final List<TypeReference> oldTypeArguments = expression.getUserData(AstKeys.TYPE_ARGUMENTS);

        if (inferredType instanceof IGenericInstance) {
            boolean typeArgumentsChanged = false;
            List<TypeReference> typeArguments = ((IGenericInstance) inferredType).getTypeArguments();

            for (int i = 0; i < typeArguments.size(); i++) {
                TypeReference t = typeArguments.get(i);

                while (t.isWildcardType()) {
                    t = t.hasExtendsBound() ? t.getExtendsBound() : MetadataHelper.getUpperBound(t);

                    if (!typeArgumentsChanged) {
                        typeArguments = toList(typeArguments);
                        typeArgumentsChanged = true;
                    }

                    typeArguments.set(i, t);
                }

                while (t.isGenericParameter()) {
                    final GenericParameter inScope = _context.getCurrentMethod().findTypeVariable(t.getName());

                    if (inScope != null && MetadataHelper.isSameType(t, inScope)) {
                        break;
                    }

                    if (oldTypeArguments != null &&
                        oldTypeArguments.size() == typeArguments.size()) {

                        final TypeReference o = oldTypeArguments.get(i);

                        if (!MetadataHelper.isSameType(o, t)) {
                            t = o;

                            if (!typeArgumentsChanged) {
                                typeArguments = toList(typeArguments);
                                typeArgumentsChanged = true;
                            }

                            typeArguments.set(i, t);
                            continue;
                        }
                    }

                    t = t.hasExtendsBound() ? t.getExtendsBound() : MetadataHelper.getUpperBound(t);

                    if (!typeArgumentsChanged) {
                        typeArguments = toList(typeArguments);
                        typeArgumentsChanged = true;
                    }

                    typeArguments.set(i, t);
                }
            }

            expression.putUserData(AstKeys.TYPE_ARGUMENTS, typeArguments);

            if (typeArgumentsChanged) {
                inferredType = inferredType.makeGenericType(typeArguments);
            }
        }

        return inferredType;
    }

    @SuppressWarnings("ConstantConditions")
    private TypeReference cleanTypeArguments(final TypeReference newType, final TypeReference alternateType) {
        if (!(alternateType instanceof IGenericInstance)) {
            return newType;
        }

        if (!StringUtilities.equals(newType.getInternalName(), alternateType.getInternalName())) {
            return newType;
        }

        final List<TypeReference> alternateTypeArguments = ((IGenericInstance) alternateType).getTypeArguments();

        boolean typeArgumentsChanged = false;
        List<TypeReference> typeArguments;

        if (newType instanceof IGenericInstance) {
            typeArguments = ((IGenericInstance) newType).getTypeArguments();
        }
        else {
            typeArguments = new ArrayList<>();
            typeArguments.addAll(newType.getGenericParameters());
        }

        for (int i = 0; i < typeArguments.size(); i++) {
            TypeReference t = typeArguments.get(i);

            while (t.isGenericParameter()) {
                final GenericParameter inScope = _context.getCurrentMethod().findTypeVariable(t.getName());

                if (inScope != null && MetadataHelper.isSameType(t, inScope)) {
                    break;
                }

                if (alternateTypeArguments != null &&
                    alternateTypeArguments.size() == typeArguments.size()) {

                    final TypeReference o = alternateTypeArguments.get(i);

                    if (!MetadataHelper.isSameType(o, t)) {
                        t = o;

                        if (!typeArgumentsChanged) {
                            typeArguments = toList(typeArguments);
                            typeArgumentsChanged = true;
                        }

                        typeArguments.set(i, t);
                        continue;
                    }
                }

                t = t.hasExtendsBound() ? t.getExtendsBound() : MetadataHelper.getUpperBound(t);

                if (!typeArgumentsChanged) {
                    typeArguments = toList(typeArguments);
                    typeArgumentsChanged = true;
                }

                typeArguments.set(i, t);
            }
        }

        if (typeArgumentsChanged) {
            return newType.makeGenericType(typeArguments);
        }

        return newType;
    }

    private TypeReference inferBinaryExpression(final AstCode code, final List<Expression> arguments, final int flags) {
        final Expression left = arguments.get(0);
        final Expression right = arguments.get(1);

        runInference(left);
        runInference(right);

        final TypeReference lInferred = left.getInferredType();
        final TypeReference rInferred = right.getInferredType();

        left.setExpectedType(lInferred);
        right.setExpectedType(lInferred);
        left.setInferredType(null);
        right.setInferredType(null);

        int operandFlags = 0;

        switch (code) {
            case And:
            case Or:
            case Xor:
            case CmpEq:
            case CmpNe: {
                if (left.getExpectedType() == BuiltinTypes.Boolean) {
                    if (right.getExpectedType() == BuiltinTypes.Integer) {
                        if (matchBooleanConstant(right) != null) {
                            right.setExpectedType(BuiltinTypes.Boolean);
                        }
                        else {
                            left.setExpectedType(BuiltinTypes.Integer);
                            operandFlags |= FLAG_BOOLEAN_PROHIBITED;
                        }
                    }
                    else if (right.getExpectedType() != BuiltinTypes.Boolean) {
                        left.setExpectedType(BuiltinTypes.Integer);
                        operandFlags |= FLAG_BOOLEAN_PROHIBITED;
                    }
                }
                else if (right.getExpectedType() == BuiltinTypes.Boolean) {
                    if (left.getExpectedType() == BuiltinTypes.Integer) {
                        if (matchBooleanConstant(left) != null) {
                            left.setExpectedType(BuiltinTypes.Boolean);
                        }
                        else {
                            right.setExpectedType(BuiltinTypes.Integer);
                            operandFlags |= FLAG_BOOLEAN_PROHIBITED;
                        }
                    }
                    else if (left.getExpectedType() != BuiltinTypes.Boolean) {
                        right.setExpectedType(BuiltinTypes.Integer);
                        operandFlags |= FLAG_BOOLEAN_PROHIBITED;
                    }
                }

                break;
            }

            default: {
                operandFlags |= FLAG_BOOLEAN_PROHIBITED;

                if (left.getExpectedType() == BuiltinTypes.Boolean ||
                    left.getExpectedType() == null && matchBooleanConstant(left) != null) {

                    left.setExpectedType(BuiltinTypes.Integer);
                }

                if (right.getExpectedType() == BuiltinTypes.Boolean ||
                    right.getExpectedType() == null && matchBooleanConstant(right) != null) {

                    right.setExpectedType(BuiltinTypes.Integer);
                }

                break;
            }
        }

        if (left.getExpectedType() == BuiltinTypes.Character) {
            if (right.getExpectedType() == BuiltinTypes.Integer && matchCharacterConstant(right) != null) {
                right.setExpectedType(BuiltinTypes.Character);
            }
        }
        else if (right.getExpectedType() == BuiltinTypes.Character) {
            if (left.getExpectedType() == BuiltinTypes.Integer && matchCharacterConstant(left) != null) {
                left.setExpectedType(BuiltinTypes.Character);
            }
        }

        final TypeReference lType = isSameType(lInferred, left.getExpectedType())
                                    ? lInferred
                                    : doInferTypeForExpression(left, left.getExpectedType(), true, operandFlags);

        final TypeReference rType = isSameType(rInferred, right.getExpectedType())
                                    ? rInferred
                                    : doInferTypeForExpression(right, right.getExpectedType(), true, operandFlags);
        
        final TypeReference operandType = inferBinaryArguments(
            left,
            right,
            typeWithMoreInformation(lType, rType),
            false,
            null,
            null,
            operandFlags
        );

        switch (code) {
            case CmpEq:
            case CmpNe:
            case CmpLt:
            case CmpGe:
            case CmpGt:
            case CmpLe:
                return BuiltinTypes.Boolean;

            case Add:
            case Sub:
            case Mul:
            case Or:
            case And:
            case Xor:
            case Div:
            case Rem:
                return adjustType(doBinaryNumericPromotion(operandType), flags);

            default:
                return adjustType(operandType, flags);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private TypeReference inferDynamicCall(final Expression expression, final TypeReference expectedType, final boolean forceInferChildren) {
        final List<Expression> arguments = expression.getArguments();
        final DynamicCallSite callSite = (DynamicCallSite) expression.getOperand();

        TypeReference inferredType = expression.getInferredType();

        if (inferredType == null) {
            inferredType = callSite.getMethodType().getReturnType();
        }

        TypeReference result = expectedType == null ? inferredType
                                                    : MetadataHelper.asSubType(inferredType, expectedType);

        if (result == null) {
            result = inferredType;
        }

        if (result.isGenericType() || MetadataHelper.isRawType(result)) {
            final MethodReference bootstrapMethod = callSite.getBootstrapMethod();

            if ("java/lang/invoke/LambdaMetafactory".equals(bootstrapMethod.getDeclaringType().getInternalName()) &&
                StringUtilities.equals("metafactory", bootstrapMethod.getName(), StringComparison.OrdinalIgnoreCase) &&
                callSite.getBootstrapArguments().size() == 3 &&
                callSite.getBootstrapArguments().get(1) instanceof MethodHandle) {

                final MethodHandle targetHandle = (MethodHandle) callSite.getBootstrapArguments().get(1);
                final MethodReference targetMethod = targetHandle.getMethod();
                final Map<TypeReference, TypeReference> expectedMappings = new HashMap<>();
                final Map<TypeReference, TypeReference> inferredMappings = new HashMap<>();

                MethodReference functionMethod = null;

                final TypeDefinition resolvedType = result.resolve();

                final List<MethodReference> methods = MetadataHelper.findMethods(
                    resolvedType != null ? resolvedType : result,
                    MetadataFilters.matchName(callSite.getMethodName())
                );

                for (final MethodReference m : methods) {
                    final MethodDefinition r = m.resolve();

                    if (r != null && r.isAbstract() && !r.isStatic() && !r.isDefault()) {
                        functionMethod = r;
                        break;
                    }
                }

                if (functionMethod == null) {
                    return null;
                }

                boolean firstArgIsTarget = false;
                MethodReference actualMethod = targetMethod;

                switch (targetHandle.getHandleType()) {
                    case GetField:
                    case PutField:
                    case InvokeVirtual:
                    case InvokeSpecial:
                    case InvokeInterface: {
                        if (arguments.size() > 0) {
                            final Expression arg = arguments.get(0);
                            final TypeReference expectedArgType = targetMethod.getDeclaringType();

                            if (forceInferChildren) {
                                inferTypeForExpression(arg, expectedArgType, true);
                            }

                            final TypeReference targetType = arg.getInferredType();

                            if (targetType != null &&
                                MetadataHelper.isSubType(targetType, expectedArgType)) {

                                firstArgIsTarget = true;

                                final MethodReference asMember = MetadataHelper.asMemberOf(actualMethod, targetType);

                                if (asMember != null) {
                                    actualMethod = asMember;
                                }
                            }
                        }
                    }
                }

                if (expectedType != null && expectedType.isGenericType() && !expectedType.isGenericDefinition()) {
                    final List<GenericParameter> genericParameters;

                    if (resolvedType != null) {
                        genericParameters = resolvedType.getGenericParameters();
                    }
                    else {
                        genericParameters = expectedType.getGenericParameters();
                    }

                    final List<TypeReference> typeArguments = ((IGenericInstance) expectedType).getTypeArguments();

                    if (typeArguments.size() == genericParameters.size()) {
                        for (int i = 0; i < genericParameters.size(); i++) {
                            final TypeReference typeArgument = typeArguments.get(i);
                            final GenericParameter genericParameter = genericParameters.get(i);

                            if (!MetadataHelper.isSameType(typeArgument, genericParameter, true)) {
                                expectedMappings.put(genericParameter, typeArgument);
                            }
                        }
                    }
                }

                new AddMappingsForArgumentVisitor(
                    actualMethod.isConstructor() ? actualMethod.getDeclaringType()
                                                 : actualMethod.getReturnType()
                ).visit(functionMethod.getReturnType(), inferredMappings);

                final List<ParameterDefinition> tp = actualMethod.getParameters();
                final List<ParameterDefinition> fp = functionMethod.getParameters();

                if (tp.size() == fp.size()) {
                    for (int i = 0; i < fp.size(); i++) {
                        new AddMappingsForArgumentVisitor(tp.get(i).getParameterType()).visit(fp.get(i).getParameterType(), inferredMappings);
                    }
                }

                for (final TypeReference key : expectedMappings.keySet()) {
                    final TypeReference expectedMapping = expectedMappings.get(key);
                    final TypeReference inferredMapping = inferredMappings.get(key);

                    if (inferredMapping == null || MetadataHelper.isSubType(expectedMapping, inferredMapping)) {
                        inferredMappings.put(key, expectedMapping);
                    }
                }

                result = TypeSubstitutionVisitor.instance().visit(
                    resolvedType != null ? resolvedType : result,
                    inferredMappings
                );

                if (!firstArgIsTarget || expectedType == null) {
                    return result;
                }

                //
                // If we have a target argument, see if we can improve its inferred type based on our result function type.
                // For example, if our target method is Set<E>::add(E), and our function type is Function<String, Boolean>, then
                // we should be able to infer that E is bound to String, and our target type is actually Set<String>.
                //

                TypeReference declaringType = actualMethod.getDeclaringType();

                if (!declaringType.isGenericDefinition() && !MetadataHelper.isRawType(actualMethod.getDeclaringType())) {
                    return result;
                }

                declaringType = declaringType.isGenericDefinition() ? declaringType : declaringType.resolve();

                if (declaringType == null) {
                    return result;
                }

                final MethodReference resultMethod = MetadataHelper.asMemberOf(functionMethod, result);

                actualMethod = actualMethod.resolve();

                if (resultMethod == null || actualMethod == null) {
                    return result;
                }

                inferredMappings.clear();

                new AddMappingsForArgumentVisitor(resultMethod.getReturnType()).visit(actualMethod.getReturnType(), inferredMappings);

                final List<ParameterDefinition> ap = actualMethod.getParameters();
                final List<ParameterDefinition> rp = resultMethod.getParameters();

                if (ap.size() == rp.size()) {
                    for (int i = 0, n = ap.size(); i < n; i++) {
                        new AddMappingsForArgumentVisitor(rp.get(i).getParameterType()).visit(ap.get(i).getParameterType(), inferredMappings);
                    }
                }

                final TypeReference resolvedTargetType = TypeSubstitutionVisitor.instance().visit(declaringType, inferredMappings);

                if (resolvedTargetType != null) {
                    inferTypeForExpression(
                        arguments.get(0),
                        resolvedTargetType,
                        true
                    );
                }
            }
        }

        return result;
    }

    @SuppressWarnings("ConstantConditions")
    private TypeReference inferCall(final Expression expression, final TypeReference expectedType, final boolean forceInferChildren) {
        final AstCode code = expression.getCode();
        final List<Expression> arguments = expression.getArguments();
        final MethodReference method = (MethodReference) expression.getOperand();
        final List<ParameterDefinition> parameters = method.getParameters();
        final boolean hasThis = code != AstCode.InvokeStatic && code != AstCode.InvokeDynamic;

        TypeReference targetType = null;
        MethodReference boundMethod = method;

        if (forceInferChildren) {
            final MethodDefinition r = method.resolve();

            MethodReference actualMethod;

            if (hasThis) {
                final Expression thisArg = arguments.get(0);

                final TypeReference expectedTargetType = thisArg.getInferredType() != null ? thisArg.getInferredType()
                                                                                           : thisArg.getExpectedType();

                if (expectedTargetType != null &&
                    expectedTargetType.isGenericType() &&
                    !expectedTargetType.isGenericDefinition()) {

                    boundMethod = MetadataHelper.asMemberOf(method, expectedTargetType);

                    targetType = inferTypeForExpression(
                        arguments.get(0),
                        expectedTargetType
                    );
                }
                else if (method.isConstructor()) {
                    targetType = method.getDeclaringType();
                }
                else {
                    targetType = inferTypeForExpression(
                        arguments.get(0),
                        method.getDeclaringType()
                    );
                }

                if (!(targetType instanceof RawType) &&
                    MetadataHelper.isRawType(targetType) &&
                    !MetadataHelper.canReferenceTypeVariablesOf(targetType, _context.getCurrentType())) {

                    targetType = MetadataHelper.erase(targetType);
                }

                final MethodReference m = targetType != null ? MetadataHelper.asMemberOf(r != null ? r : method, targetType)
                                                             : method;

                if (m != null) {
                    actualMethod = m;
                }
                else {
                    actualMethod = r != null ? r : boundMethod;
                }
            }
            else {
                actualMethod = r != null ? r : boundMethod;
            }

            boundMethod = actualMethod;
            expression.setOperand(boundMethod);

            List<ParameterDefinition> p = method.getParameters();

            Map<TypeReference, TypeReference> mappings = null;

            if (actualMethod.containsGenericParameters() || r != null && r.containsGenericParameters()) {
                final Map<TypeReference, TypeReference> oldMappings = new HashMap<>();
                final Map<TypeReference, TypeReference> newMappings = new HashMap<>();
                final Map<TypeReference, TypeReference> inferredMappings = new HashMap<>();

                if (targetType != null && targetType.isGenericType()) {
                    oldMappings.putAll(MetadataHelper.getGenericSubTypeMappings(targetType.getUnderlyingType(), targetType));
                }

                final List<ParameterDefinition> rp = r != null ? r.getParameters() : actualMethod.getParameters();
                final List<ParameterDefinition> cp = boundMethod.getParameters();

                final boolean mapOld = method instanceof IGenericInstance;

                for (int i = 0; i < parameters.size(); i++) {
                    final TypeReference rType = rp.get(i).getParameterType();
                    final TypeReference pType = p.get(i).getParameterType();
                    final TypeReference cType = cp.get(i).getParameterType();
                    final TypeReference aType = inferTypeForExpression(arguments.get(hasThis ? i + 1 : i), cType/*null*/);

                    if (mapOld && rType != null && rType.containsGenericParameters()) {
                        new AddMappingsForArgumentVisitor(pType).visit(rType, oldMappings);
                    }

                    if (cType != null && rType.containsGenericParameters()) {
                        new AddMappingsForArgumentVisitor(cType).visit(rType, newMappings);
                    }

                    if (aType != null && rType.containsGenericParameters()) {
                        new AddMappingsForArgumentVisitor(aType).visit(rType, inferredMappings);
                    }
                }

                if (expectedType != null) {
                    final TypeReference returnType = r != null ? r.getReturnType()
                                                               : actualMethod.getReturnType();

                    if (returnType.containsGenericParameters()) {
                        final Map<TypeReference, TypeReference> returnMappings = new HashMap<>();

                        new AddMappingsForArgumentVisitor(expectedType).visit(returnType, returnMappings);

                        newMappings.putAll(returnMappings);
                    }
                }

                if (!oldMappings.isEmpty() || !newMappings.isEmpty() || !inferredMappings.isEmpty()) {
                    mappings = oldMappings;

                    for (final TypeReference t : newMappings.keySet()) {
                        final TypeReference oldMapping = mappings.get(t);
                        final TypeReference newMapping = newMappings.get(t);

                        if (oldMapping == null || MetadataHelper.isSubType(newMapping, oldMapping)) {
                            mappings.put(t, newMapping);
                        }
                    }

                    for (final TypeReference t : inferredMappings.keySet()) {
                        final TypeReference oldMapping = mappings.get(t);
                        final TypeReference newMapping = inferredMappings.get(t);

                        if (oldMapping == null || MetadataHelper.isSubType(newMapping, oldMapping)) {
                            mappings.put(t, newMapping);
                        }
                    }
                }

                if (mappings != null) {
                    boundMethod = TypeSubstitutionVisitor.instance().visitMethod(r != null ? r : actualMethod, mappings);
                    actualMethod = boundMethod;
                    expression.setOperand(boundMethod);
                    p = boundMethod.getParameters();
                }

                final TypeReference boundDeclaringType = boundMethod.getDeclaringType();

                if (boundDeclaringType.isGenericType()) {
                    if (mappings == null) {
                        mappings = new HashMap<>();
                    }

                    for (final GenericParameter gp : boundDeclaringType.getGenericParameters()) {
                        final GenericParameter inScope = _context.getCurrentMethod().findTypeVariable(gp.getName());

                        if (inScope != null && MetadataHelper.isSameType(gp, inScope)) {
                            continue;
                        }

                        if (!mappings.containsKey(gp)) {
                            mappings.put(gp, MetadataHelper.eraseRecursive(gp));
                        }
                    }

                    boundMethod = TypeSubstitutionVisitor.instance().visitMethod(actualMethod, mappings);
                    expression.setOperand(boundMethod);
                    p = boundMethod.getParameters();
                }

                if (boundMethod.isGenericMethod()) {
                    if (mappings == null) {
                        mappings = new HashMap<>();
                    }

                    for (final GenericParameter gp : boundMethod.getGenericParameters()) {
                        if (!mappings.containsKey(gp)) {
                            mappings.put(gp, MetadataHelper.eraseRecursive(gp));
                        }
                    }

                    boundMethod = TypeSubstitutionVisitor.instance().visitMethod(actualMethod, mappings);
                    expression.setOperand(boundMethod);
                    p = boundMethod.getParameters();
                }

                if (r != null && method.isGenericMethod()) {
                    final HashMap<TypeReference, TypeReference> tempMappings = new HashMap<>();
                    final List<ParameterDefinition> bp = method.getParameters();

                    for (int i = 0, n = bp.size(); i < n; i++) {
                        new AddMappingsForArgumentVisitor(bp.get(i).getParameterType()).visit(
                            rp.get(i).getParameterType(),
                            tempMappings
                        );
                    }

                    boolean changed = false;

                    if (mappings == null) {
                        mappings = tempMappings;
                        changed = true;
                    }
                    else {
                        for (final TypeReference key : tempMappings.keySet()) {
                            if (!mappings.containsKey(key)) {
                                mappings.put(key, tempMappings.get(key));
                                changed = true;
                            }
                        }
                    }

                    if (changed) {
                        boundMethod = TypeSubstitutionVisitor.instance().visitMethod(actualMethod, mappings);
                        expression.setOperand(boundMethod);
                        p = boundMethod.getParameters();
                    }
                }
            }
            else {
                boundMethod = actualMethod;
            }

            if (hasThis && mappings != null) {
                TypeReference expectedTargetType;

                if (boundMethod.isConstructor()) {
                    expectedTargetType = MetadataHelper.substituteGenericArguments(boundMethod.getDeclaringType(), mappings);
                }
                else {
                    expectedTargetType = boundMethod.getDeclaringType();
                }

                if (expectedTargetType != null &&
                    expectedTargetType.isGenericDefinition() &&
                    arguments.get(0).getInferredType() != null) {

                    expectedTargetType = MetadataHelper.asSuper(
                        expectedTargetType,
                        arguments.get(0).getInferredType()
                    );
                }

                final TypeReference inferredTargetType = inferTypeForExpression(
                    arguments.get(0),
                    expectedTargetType,
                    forceInferChildren
                );

                if (inferredTargetType != null) {
                    targetType = MetadataHelper.substituteGenericArguments(inferredTargetType, mappings);

                    if (MetadataHelper.isRawType(targetType) &&
                        !MetadataHelper.canReferenceTypeVariablesOf(targetType, _context.getCurrentType())) {

                        targetType = MetadataHelper.erase(targetType);
                    }

                    boundMethod = MetadataHelper.asMemberOf(boundMethod, targetType);
                    p = boundMethod.getParameters();
                    expression.setOperand(boundMethod);
                }
            }

            for (int i = 0; i < parameters.size(); i++) {
                final TypeReference pType = p.get(i).getParameterType();

                final Expression argument = arguments.get(hasThis ? i + 1 : i);

                inferTypeForExpression(
                    argument,
                    pType,
                    forceInferChildren,
                    match(argument, AstCode.Load) && pType != BuiltinTypes.Boolean ? FLAG_BOOLEAN_PROHIBITED : 0
                );
            }
        }

        if (hasThis) {
            if (boundMethod.isConstructor()) {
                return boundMethod.getDeclaringType();
            }
        }

        return boundMethod.getReturnType();
    }

    private TypeReference inferTypeForVariable(final Variable v, final TypeReference expectedType) {
        return inferTypeForVariable(v, expectedType, false, 0);
    }

    private TypeReference inferTypeForVariable(final Variable v, final TypeReference expectedType, final int flags) {
        return inferTypeForVariable(v, expectedType, false, flags);
    }

    private TypeReference inferTypeForVariable(
        final Variable v,
        final TypeReference expectedType,
        final boolean favorExpectedOverActual,
        final int flags) {

        final TypeReference lastInferredType = _inferredVariableTypes.get(v);

        if (lastInferredType != null) {
            return adjustType(lastInferredType, flags);
        }

        if (isSingleStoreBoolean(v)) {
            return adjustType(BuiltinTypes.Boolean, flags);
        }

        if (favorExpectedOverActual && expectedType != null) {
            return adjustType(expectedType, flags);
        }

        final TypeReference variableType = v.getType();

        if (variableType != null) {
            return adjustType(variableType, flags);
        }

        if (v.isGenerated()) {
            return adjustType(expectedType, flags);
        }

        final ParameterDefinition p = v.getOriginalParameter();

        return adjustType(
            p != null ? p.getParameterType()
                      : v.getOriginalVariable().getVariableType(),
            flags
        );
    }

    private static TypeReference adjustType(final TypeReference type, final int flags) {
        if (testAny(flags, FLAG_BOOLEAN_PROHIBITED) && type == BuiltinTypes.Boolean) {
            return BuiltinTypes.Integer;
        }
        return type;
    }

    private TypeReference doBinaryNumericPromotion(final TypeReference type) {
        if (type == null) {
            return null;
        }

        switch (type.getSimpleType()) {
            case Byte:
            case Character:
            case Short:
                return BuiltinTypes.Integer;

            default:
                return type;
        }
    }

    private TypeReference inferBinaryArguments(
        final Expression left,
        final Expression right,
        final TypeReference expectedType,
        final boolean forceInferChildren,
        final TypeReference leftPreferred,
        final TypeReference rightPreferred,
        final int operandFlags) {

        TypeReference actualLeftPreferred = leftPreferred;
        TypeReference actualRightPreferred = rightPreferred;

        if (actualLeftPreferred == null) {
            actualLeftPreferred = doInferTypeForExpression(left, expectedType, forceInferChildren, operandFlags);
        }

        if (actualRightPreferred == null) {
            actualRightPreferred = doInferTypeForExpression(right, expectedType, forceInferChildren, operandFlags);
        }

        if (actualLeftPreferred == BuiltinTypes.Null) {
            if (actualRightPreferred != null && !actualRightPreferred.isPrimitive()) {
                actualLeftPreferred = actualRightPreferred;
            }
        }
        else if (actualRightPreferred == BuiltinTypes.Null) {
            if (actualLeftPreferred != null && !actualLeftPreferred.isPrimitive()) {
                actualRightPreferred = actualLeftPreferred;
            }
        }

        if (actualLeftPreferred == BuiltinTypes.Character) {
            if (actualRightPreferred == BuiltinTypes.Integer && matchCharacterConstant(right) != null) {
                actualRightPreferred = BuiltinTypes.Character;
            }
        }
        else if (actualRightPreferred == BuiltinTypes.Character) {
            if (actualLeftPreferred == BuiltinTypes.Integer && matchCharacterConstant(left) != null) {
                actualLeftPreferred = BuiltinTypes.Character;
            }
        }

        if (isSameType(actualLeftPreferred, actualRightPreferred)) {
            left.setInferredType(actualLeftPreferred);
            left.setExpectedType(actualLeftPreferred);
            right.setInferredType(actualLeftPreferred);
            right.setExpectedType(actualLeftPreferred);

            return actualLeftPreferred;
        }

        if (isSameType(actualRightPreferred, doInferTypeForExpression(left, actualRightPreferred, forceInferChildren, operandFlags))) {
            left.setInferredType(actualRightPreferred);
            left.setExpectedType(actualRightPreferred);
            right.setInferredType(actualRightPreferred);
            right.setExpectedType(actualRightPreferred);

            return actualRightPreferred;
        }

        if (isSameType(actualLeftPreferred, doInferTypeForExpression(right, actualLeftPreferred, forceInferChildren, operandFlags))) {
            left.setInferredType(actualLeftPreferred);
            left.setExpectedType(actualLeftPreferred);
            right.setInferredType(actualLeftPreferred);
            right.setExpectedType(actualLeftPreferred);

            return actualLeftPreferred;
        }

        final TypeReference result = typeWithMoreInformation(actualLeftPreferred, actualRightPreferred);

        left.setExpectedType(result);
        right.setExpectedType(result);
        left.setInferredType(doInferTypeForExpression(left, result, forceInferChildren, operandFlags));
        right.setInferredType(doInferTypeForExpression(right, result, forceInferChildren, operandFlags));

        return result;
    }

    private TypeReference typeWithMoreInformation(final TypeReference leftPreferred, final TypeReference rightPreferred) {
        if (leftPreferred == rightPreferred) {
            return leftPreferred;
        }

        final int left = getInformationAmount(leftPreferred);
        final int right = getInformationAmount(rightPreferred);

        if (left < right) {
            return rightPreferred;
        }

        if (left > right) {
            return leftPreferred;
        }

        if (leftPreferred != null && rightPreferred != null) {
            return MetadataHelper.findCommonSuperType(
                leftPreferred.isGenericDefinition() ? new RawType(leftPreferred)
                                                    : leftPreferred,
                rightPreferred.isGenericDefinition() ? new RawType(rightPreferred)
                                                     : rightPreferred
            );
        }

        return leftPreferred;
    }

    private static int getInformationAmount(final TypeReference type) {
        if (type == null || type == BuiltinTypes.Null) {
            return 0;
        }

        switch (type.getSimpleType()) {
            case Boolean:
                return 1;

            case Byte:
                return 8;

            case Character:
            case Short:
                return 16;

            case Integer:
            case Float:
                return 32;

            case Long:
            case Double:
                return 64;

            default:
                return 100;
        }
    }

    static TypeReference getFieldType(final FieldReference field) {
        final FieldDefinition resolvedField = field.resolve();

        if (resolvedField != null) {
            final FieldReference asMember = MetadataHelper.asMemberOf(resolvedField, field.getDeclaringType());

            return asMember.getFieldType();
        }

        return substituteTypeArguments(field.getFieldType(), field);
    }

    static TypeReference substituteTypeArguments(final TypeReference type, final MemberReference member) {
        if (type instanceof ArrayType) {
            final ArrayType arrayType = (ArrayType) type;

            final TypeReference elementType = substituteTypeArguments(
                arrayType.getElementType(),
                member
            );

            if (!MetadataResolver.areEquivalent(elementType, arrayType.getElementType())) {
                return elementType.makeArrayType();
            }

            return type;
        }

        if (type instanceof IGenericInstance) {
            final IGenericInstance genericInstance = (IGenericInstance) type;
            final List<TypeReference> newTypeArguments = new ArrayList<>();

            boolean isChanged = false;

            for (final TypeReference typeArgument : genericInstance.getTypeArguments()) {
                final TypeReference newTypeArgument = substituteTypeArguments(typeArgument, member);

                newTypeArguments.add(newTypeArgument);
                isChanged |= newTypeArgument != typeArgument;
            }

            return isChanged ? type.makeGenericType(newTypeArguments)
                             : type;
        }

        if (type instanceof GenericParameter) {
            final GenericParameter genericParameter = (GenericParameter) type;
            final IGenericParameterProvider owner = genericParameter.getOwner();

            if (member.getDeclaringType() instanceof ArrayType) {
                return member.getDeclaringType().getElementType();
            }
            else if (owner instanceof MethodReference && member instanceof MethodReference) {
                final MethodReference method = (MethodReference) member;
                final MethodReference ownerMethod = (MethodReference) owner;

                if (method.isGenericMethod() &&
                    MetadataResolver.areEquivalent(ownerMethod.getDeclaringType(), method.getDeclaringType()) &&
                    StringUtilities.equals(ownerMethod.getName(), method.getName()) &&
                    StringUtilities.equals(ownerMethod.getErasedSignature(), method.getErasedSignature())) {

                    if (method instanceof IGenericInstance) {
                        final List<TypeReference> typeArguments = ((IGenericInstance) member).getTypeArguments();
                        return typeArguments.get(genericParameter.getPosition());
                    }
                    else {
                        return method.getGenericParameters().get(genericParameter.getPosition());
                    }
                }
            }
            else if (owner instanceof TypeReference) {
                TypeReference declaringType;

                if (member instanceof TypeReference) {
                    declaringType = (TypeReference) member;
                }
                else {
                    declaringType = member.getDeclaringType();
                }

                if (MetadataResolver.areEquivalent((TypeReference) owner, declaringType)) {
                    if (declaringType instanceof IGenericInstance) {
                        final List<TypeReference> typeArguments = ((IGenericInstance) declaringType).getTypeArguments();
                        return typeArguments.get(genericParameter.getPosition());
                    }

                    if (!declaringType.isGenericDefinition()) {
                        declaringType = declaringType.getUnderlyingType();
                    }

                    if (declaringType != null && declaringType.isGenericDefinition()) {
                        return declaringType.getGenericParameters().get(genericParameter.getPosition());
                    }
                }
            }
        }

        return type;
    }


/*
    static TypeReference substituteTypeArguments(final TypeReference type, final MemberReference member, final TypeReference targetType) {
        if (type instanceof ArrayType) {
            final ArrayType arrayType = (ArrayType) type;
            final TypeReference elementType = substituteTypeArguments(arrayType.getElementType(), member, targetType);

            if (elementType != arrayType.getElementType()) {
                return elementType.makeArrayType();
            }

            return type;
        }

        if (type instanceof IGenericInstance) {
            final IGenericInstance genericInstance = (IGenericInstance) type;
            final List<TypeReference> newTypeArguments = new ArrayList<>();

            boolean isChanged = false;

            for (final TypeReference typeArgument : genericInstance.getTypeArguments()) {
                final TypeReference newTypeArgument = substituteTypeArguments(typeArgument, member, targetType);

                newTypeArguments.add(newTypeArgument);
                isChanged |= newTypeArgument != typeArgument;
            }

            return isChanged ? type.resolve().makeGenericType(newTypeArguments)
                             : type;
        }

        if (type instanceof GenericParameter) {
            final GenericParameter genericParameter = (GenericParameter) type;
            final IGenericParameterProvider owner = genericParameter.getOwner();

            if (owner == member && member instanceof IGenericInstance) {
                final List<TypeReference> typeArguments = ((IGenericInstance) member).getTypeArguments();
                return typeArguments.get(genericParameter.getPosition());
            }
            else if (targetType != null && owner == targetType.resolve() && targetType instanceof IGenericInstance) {
                final List<TypeReference> typeArguments = ((IGenericInstance) targetType).getTypeArguments();
                return typeArguments.get(genericParameter.getPosition());
            }
//            else {
//                return genericParameter.getExtendsBound();
//            }
        }

        return type;
    }
*/

    private boolean isSameType(final TypeReference t1, final TypeReference t2) {
        return MetadataHelper.isSameType(t1, t2, true);
    }

    private boolean anyDone(final List<ExpressionToInfer> expressions) {
        for (final ExpressionToInfer expression : expressions) {
            if (expression.done) {
                return true;
            }
        }
        return false;
    }

    private boolean allDone(final List<ExpressionToInfer> expressions) {
        for (final ExpressionToInfer expression : expressions) {
            if (!expression.done) {
                return false;
            }
        }
        return true;
    }

    public static <T> boolean trueForAll(final Iterable<T> sequence, final Predicate<T> condition) {
        for (final T item : sequence) {
            if (!condition.test(item)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isBoolean(final TypeReference type) {
        return type != null && type.getSimpleType() == JvmType.Boolean;
    }

    // <editor-fold defaultstate="collapsed" desc="ExpressionToInfer Class">

    final static class ExpressionToInfer {
        private final List<Variable> dependencies = new ArrayList<>();

        Expression expression;
        boolean done;
        Variable dependsOnSingleLoad;
        int flags;

        @Override
        public String toString() {
            if (done) {
                return "[Done] " + expression;
            }
            return expression.toString();
        }
    }

    // </editor-fold>

    private final static class AddMappingsForArgumentVisitor extends DefaultTypeVisitor<Map<TypeReference, TypeReference>, Void> {
        private TypeReference argumentType;

        AddMappingsForArgumentVisitor(final TypeReference argumentType) {
            this.argumentType = VerifyArgument.notNull(argumentType, "argumentType");
        }

        public Void visit(final TypeReference t, final Map<TypeReference, TypeReference> map) {
            final TypeReference a = argumentType;
            t.accept(this, map);
            argumentType = a;
            return null;
        }

        @Override
        public Void visitArrayType(final ArrayType t, final Map<TypeReference, TypeReference> map) {
            final TypeReference a = argumentType;

            if (a.isArray()) {
                argumentType = a.getElementType();
                visit(t.getElementType(), map);
            }

            return null;
        }

        @Override
        @SuppressWarnings("StatementWithEmptyBody")
        public Void visitGenericParameter(final GenericParameter t, final Map<TypeReference, TypeReference> map) {
            if (MetadataResolver.areEquivalent(argumentType, t)) {
                return null;
            }

            final TypeReference existingMapping = map.get(t);

            TypeReference mappedType = argumentType;

            mappedType = ensureReferenceType(mappedType);

            if (existingMapping == null) {
                if (!(mappedType instanceof RawType) && MetadataHelper.isRawType(mappedType)) {
                    final TypeReference bound = MetadataHelper.getUpperBound(t);
                    final TypeReference asSuper = MetadataHelper.asSuper(mappedType, bound);

                    if (asSuper != null) {
                        if (MetadataHelper.isSameType(MetadataHelper.getUpperBound(t), asSuper)) {
                            return null;
                        }
                        mappedType = asSuper;
                    }
                    else {
                        mappedType = MetadataHelper.erase(mappedType);
                    }
                }
                map.put(t, mappedType);
            }
            else if (MetadataHelper.isSubType(argumentType, existingMapping)) {
//                map.put(t, argumentType);
            }
            else {
                TypeReference commonSuperType = MetadataHelper.asSuper(mappedType, existingMapping);

                if (commonSuperType == null) {
                    commonSuperType = MetadataHelper.asSuper(existingMapping, mappedType);
                }

                if (commonSuperType == null) {
                    commonSuperType = MetadataHelper.findCommonSuperType(existingMapping, mappedType);
                }

                map.put(t, commonSuperType);
            }

            return null;
        }

        @Override
        public Void visitWildcard(final WildcardType t, final Map<TypeReference, TypeReference> map) {
            return null;
        }

        @Override
        public Void visitCompoundType(final CompoundTypeReference t, final Map<TypeReference, TypeReference> map) {
            return null;
        }

        @Override
        public Void visitParameterizedType(final TypeReference t, final Map<TypeReference, TypeReference> map) {
            final TypeReference r = MetadataHelper.asSuper(t.getUnderlyingType(), argumentType);
            final TypeReference s = MetadataHelper.asSubType(argumentType, r != null ? r : t.getUnderlyingType());

            if (s != null && s instanceof IGenericInstance) {
                final List<TypeReference> tArgs = ((IGenericInstance) t).getTypeArguments();
                final List<TypeReference> sArgs = ((IGenericInstance) s).getTypeArguments();

                if (tArgs.size() == sArgs.size()) {
                    for (int i = 0, n = tArgs.size(); i < n; i++) {
                        argumentType = sArgs.get(i);
                        visit(tArgs.get(i), map);
                    }
                }
            }

            return null;
        }

        @Override
        public Void visitPrimitiveType(final PrimitiveType t, final Map<TypeReference, TypeReference> map) {
            return null;
        }

        @Override
        public Void visitClassType(final TypeReference t, final Map<TypeReference, TypeReference> map) {
            return null;
        }

        @Override
        public Void visitNullType(final TypeReference t, final Map<TypeReference, TypeReference> map) {
            return null;
        }

        @Override
        public Void visitBottomType(final TypeReference t, final Map<TypeReference, TypeReference> map) {
            return null;
        }

        @Override
        public Void visitRawType(final RawType t, final Map<TypeReference, TypeReference> map) {
            return null;
        }
    }

    private static TypeReference ensureReferenceType(final TypeReference mappedType) {
        if (mappedType == null) {
            return null;
        }

        if (mappedType.isPrimitive()) {
            switch (mappedType.getSimpleType()) {
                case Boolean:
                    return CommonTypeReferences.Boolean;
                case Byte:
                    return CommonTypeReferences.Byte;
                case Character:
                    return CommonTypeReferences.Character;
                case Short:
                    return CommonTypeReferences.Short;
                case Integer:
                    return CommonTypeReferences.Integer;
                case Long:
                    return CommonTypeReferences.Long;
                case Float:
                    return CommonTypeReferences.Float;
                case Double:
                    return CommonTypeReferences.Double;
            }
        }

        return mappedType;
    }
}
