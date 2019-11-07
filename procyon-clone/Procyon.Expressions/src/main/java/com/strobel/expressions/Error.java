/*
 * Error.java
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
import com.strobel.reflection.FieldInfo;
import com.strobel.reflection.MemberInfo;
import com.strobel.reflection.MemberType;
import com.strobel.reflection.MethodBase;
import com.strobel.reflection.Type;

import static java.lang.String.format;

/**
 * @author Mike Strobel
 */
final class Error {
    private Error() {
    }

    public static IllegalStateException extensionMustOverride(final String memberName) {
        return new IllegalStateException(
            format("Expression extensions must override %s.", memberName)
        );
    }

    public static IllegalStateException reducibleMustOverride(final String memberName) {
        return new IllegalStateException(
            format("Reducible expression extensions must override %s.", memberName)
        );
    }

    public static IllegalStateException memberNotField(final MemberInfo member) {
        return new IllegalStateException(
            format("Member '%s' must be a field.", member)
        );
    }

    public static IllegalStateException mustBeReducible() {
        return new IllegalStateException(
            "Expression must be reducible to perform this operation."
        );
    }

    public static IllegalStateException mustReduceToDifferent() {
        return new IllegalStateException(
            "Expression must reducible to a different expression."
        );
    }

    public static IllegalStateException reducedNotCompatible() {
        return new IllegalStateException(
            "Expression was reduced to an expression of a non-compatible type."
        );
    }

    public static IllegalStateException argumentTypesMustMatch() {
        return new IllegalStateException("Argument types must match.");
    }

    public static IllegalStateException argumentCannotBeOfTypeVoid() {
        return new IllegalStateException("Argument cannot be of type 'void'.");
    }

    public static IllegalStateException expressionMustBeWriteable(final String parameterName) {
        return new IllegalStateException(
            format("Argument '%s' must be writeable.", parameterName)
        );
    }

    public static IllegalStateException expressionMustBeReadable(final String parameterName) {
        return new IllegalStateException(
            format("Argument '%s' must be readable.", parameterName)
        );
    }

    public static IllegalStateException mustRewriteChildToSameType(final Type before, final Type after, final String callerName) {
        return new IllegalStateException(
            format("MethodBase '%s' performed an illegal type change from %s to %s.", callerName, before, after)
        );
    }

    public static IllegalStateException mustRewriteWithoutMethod(final MethodBase method, final String callerName) {
        return new IllegalStateException(
            format(
                "Rewritten expression calls method '%s', but the original node had no method.  " +
                "If this is is intentional, override '%s' and change it to allow this rewrite.",
                callerName,
                method
            )
        );
    }

    public static <T extends Expression> IllegalStateException mustRewriteToSameNode(
        final String callerName,
        final Class<T> type,
        final String overrideMethodName) {

        return new IllegalStateException(
            format(
                "When called from '%s', rewriting a node of type '%s' must return a non-null value of the " +
                "same type.  Alternatively, override '%s' and change it to not visit children of this type.",
                callerName,
                type.getName(),
                overrideMethodName
            )
        );
    }

    public static IllegalStateException unhandledUnary(final ExpressionType unaryType) {
        return new IllegalStateException(
            format("Unhandled unary expression type: %s.", unaryType)
        );
    }

    public static IllegalStateException unhandledBinary(final ExpressionType binaryType) {
        return new IllegalStateException(
            format("Unhandled binary expression type: %s.", binaryType)
        );
    }

    public static IllegalStateException unmodifiableCollection() {
        return new IllegalStateException("Collection cannot be modified.");
    }

    public static IllegalStateException duplicateVariable(final ParameterExpression variable) {
        return new IllegalStateException(
            format(
                "Found duplicate variable '%s'.  Each ParameterExpression in the list " +
                "must be a unique object.",
                variable.getName()
            )
        );
    }

    public static IllegalStateException unaryOperatorNotDefined(final ExpressionType operator, final Type operandType) {
        return new IllegalStateException(
            format(
                "The unary operator '%s' is not defined for type '%s'",
                operator,
                operandType
            )
        );
    }

    public static IllegalStateException operatorMethodMustNotBeStatic(final MethodBase method) {
        return new IllegalStateException(
            format(
                "MethodBase '%s.%s' cannot be used as an operator because it is static.",
                method.getDeclaringType().getBriefDescription(),
                method.getName()
            )
        );
    }

    public static IllegalStateException operatorMethodMustNotReturnVoid(final MethodBase method) {
        return new IllegalStateException(
            format(
                "MethodBase '%s.%s' cannot be used as an operator because it returns void.",
                method.getDeclaringType().getBriefDescription(),
                method.getName()
            )
        );
    }

    public static IllegalStateException operatorMethodParametersMustMatchReturnValue(final MethodBase method) {
        return new IllegalStateException(
            format(
                "MethodBase '%s.%s' cannot be used as an operator because its parameters do not match " +
                "its return value.",
                method.getDeclaringType().getBriefDescription(),
                method.getName()
            )
        );
    }

    public static IllegalStateException returnTypeDoesNotMatchOperandType(final ExpressionType expressionType, final MethodBase method) {
        return new IllegalStateException(
            format(
                "The return type for operator '%s' does not match the declaring type of method '%s.%s'.",
                expressionType,
                method.getDeclaringType().getBriefDescription(),
                method.getName()
            )
        );
    }

    public static IllegalStateException returnTypeDoesNotMatchOperandType(final MethodBase method) {
        return new IllegalStateException(
            format(
                "The return type of operator method '%s.%s' does not match the method's declaring type.",
                method.getDeclaringType().getBriefDescription(),
                method.getName()
            )
        );
    }

    public static IllegalStateException incorrectNumberOfConstructorArguments() {
        return new IllegalStateException("Incorrect number of arguments supplied for constructor call.");
    }

    public static IllegalStateException incorrectNumberOfLambdaArguments() {
        return new IllegalStateException("Incorrect number of arguments supplied for lambda invocation.");
    }

    public static IllegalStateException incorrectNumberOfLambdaDeclarationParameters() {
        return new IllegalStateException("Incorrect number of parameters supplied for lambda declaration.");
    }

    public static IllegalStateException incorrectNumberOfMethodCallArguments(final MethodBase method) {
        return new IllegalStateException(
            format(
                "Incorrect number of arguments supplied for call to method '%s.%s'",
                method.getDeclaringType().getBriefDescription(),
                method.getName()
            )
        );
    }

    public static IllegalStateException invalidUnboxType() {
        return new IllegalStateException(
            "Can only unbox from a standard boxed type or java.lang.Object to a primitive type."
        );
    }

    public static IllegalArgumentException unboxNotDefined(final Type<?> boxedType, final Type<?> unboxedType) {
        return new IllegalArgumentException(
            format("Could not resolve an unboxing method from %s to %s.", boxedType.getName(), unboxedType.getName())
        );
    }

    public static IllegalStateException invalidBoxType() {
        return new IllegalStateException(
            "Can only unbox from a standard boxed type or java.lang.Object to a primitive type."
        );
    }

    public static IllegalStateException argumentMustBeArray() {
        return new IllegalStateException("Argument must be an array.");
    }

    public static IllegalStateException argumentMustBeBoolean() {
        return new IllegalStateException("Argument must be a boolean.");
    }

    public static IllegalStateException argumentMustBeInteger() {
        return new IllegalStateException("Argument must be an integer.");
    }

    public static IllegalStateException argumentMustBeIntegral() {
        return new IllegalStateException("Argument must be an integral numeric type.");
    }

    public static IllegalStateException coercionOperatorNotDefined(final Type sourceType, final Type destinationType) {
        return new IllegalStateException(
            format(
                "No coercion operator is defined between types '%s' and '%s'.",
                sourceType,
                destinationType
            )
        );
    }

    public static IllegalArgumentException argumentMustNotHaveValueType() {
        return new IllegalArgumentException(
            "Argument must not have a primitive type."
        );
    }

    public static IllegalArgumentException argumentMustBeThrowable() {
        return new IllegalArgumentException(
            "Argument must derive from Throwable."
        );
    }

    public static IllegalStateException methodBasedOperatorMustHaveValidReturnType(final MethodBase method) {
        return new IllegalStateException(
            format(
                "The operator method '%s.%s' must return the same type as its declaring type " +
                "or a derived type.",
                method.getDeclaringType().getBriefDescription(),
                method.getName()
            )
        );
    }

    public static IllegalStateException methodBasedOperatorMustHaveValidReturnType(final ExpressionType operator, final MethodBase method) {
        return new IllegalStateException(
            format(
                "The operator method '%s.%s' for operator '%s' must return the same type as its " +
                "declaring type or a derived type.",
                method.getDeclaringType().getBriefDescription(),
                method.getName(),
                operator
            )
        );
    }

    public static IllegalStateException expressionTypeNotInvokable(final Type type) {
        return new IllegalStateException(
            format(
                "Expression of type '%s' cannot be invoked.  Invokable types must be interfaces " +
                "with exactly one method.",
                type
            )
        );
    }

    public static IllegalStateException binaryOperatorNotDefined(final ExpressionType operator, final Type leftType, final Type rightType) {
        return new IllegalStateException(
            format(
                "The binary operator '%s' is not defined for the types '%s' and '%s'.",
                operator,
                leftType,
                rightType
            )
        );
    }

    public static IllegalStateException referenceEqualityNotDefined(final Type leftType, final Type rightType) {
        return new IllegalStateException(
            format(
                "Reference equality is not defined for the types '%s' and '%s'.",
                leftType,
                rightType
            )
        );
    }

    public static IllegalStateException invalidOperator(final ExpressionType operator) {
        return new IllegalStateException(
            format("Invalid operator: %s", operator)
        );
    }

    public static IllegalStateException targetRequiredForNonStaticMethodCall(final MethodBase method) {
        return new IllegalStateException(
            format(
                "An invocation target expression is required for a call to non-static " +
                "method '%s.%s'.",
                method.getDeclaringType().getBriefDescription(),
                method.getName()
            )
        );
    }

    public static IllegalStateException targetRequiredForNonStaticFieldAccess(final FieldInfo field) {
        return new IllegalStateException(
            format(
                "An invocation target expression is required for access to non-static " +
                "field '%s.%s'.",
                field.getDeclaringType().getBriefDescription(),
                field.getName()
            )
        );
    }

    public static IllegalStateException targetInvalidForStaticFieldAccess(final FieldInfo field) {
        return new IllegalStateException(
            format(
                "A target expression cannot be used to access static field '%s.%s'.",
                field.getDeclaringType().getBriefDescription(),
                field.getName()
            )
        );
    }

    public static IllegalStateException targetInvalidForStaticMethodCall(final MethodBase method) {
        return new IllegalStateException(
            format(
                "An invocation target expression cannot be used to call static " +
                "method '%s.%s'.",
                method.getDeclaringType().getBriefDescription(),
                method.getName()
            )
        );
    }

    public static IllegalStateException targetAndMethodTypeMismatch(final MethodBase method, final Type targetType) {
        return new IllegalStateException(
            format(
                "Expression of type '%s' is not a valid invocation target for instance " +
                "method '%s.%s'.",
                targetType.getBriefDescription(),
                method.getDeclaringType().getBriefDescription(),
                method.getName()
            )
        );
    }

    public static IllegalStateException expressionTypeDoesNotMatchParameter(final Type argType, final Type parameterType) {
        return new IllegalStateException(
            format(
                "Expression of type '%s' cannot be used for constructor parameter of type '%s'.",
                parameterType.getBriefDescription(),
                argType.getBriefDescription()
            )
        );
    }

    public static IllegalStateException expressionTypeDoesNotMatchReturn(final Type bodyType, final Type returnType) {
        return new IllegalStateException(
            format(
                "Expression of type '%s' cannot be used as the body of a lambda with return type '%s'.",
                bodyType.getBriefDescription(),
                returnType.getBriefDescription()
            )
        );
    }

    public static IllegalStateException expressionTypeDoesNotMatchConstructorParameter(final Type argType, final Type parameterType) {
        return new IllegalStateException(
            format(
                "Expression of type '%s' cannot be used for parameter of type '%s'.",
                parameterType.getBriefDescription(),
                argType.getBriefDescription()
            )
        );
    }

    public static IllegalStateException expressionTypeDoesNotMatchMethodParameter(final Type argType, final Type parameterType, final MethodBase method) {
        return new IllegalStateException(
            format(
                "Expression of type '%s' cannot be used for parameter of type '%s' of method '%s.%s'.",
                argType.getBriefDescription(),
                parameterType.getBriefDescription(),
                method.getDeclaringType().getBriefDescription(),
                method.getName()
            )
        );
    }

    public static IllegalStateException expressionTypeDoesNotMatchAssignment(final Type leftType, final Type rightType) {
        return new IllegalStateException(
            format(
                "Expression of type '%s' cannot be used for assignment to type '%s'.",
                rightType.getBriefDescription(),
                leftType.getBriefDescription()
            )
        );
    }

    public static IllegalStateException methodDoesNotExistOnType(final String methodName, final Type type) {
        return new IllegalStateException(
            format(
                "No method '%s' exists on type '%s'.",
                methodName,
                type.getBriefDescription()
            )
        );
    }

    public static IllegalStateException fieldDoesNotExistOnType(final String fieldName, final Type type) {
        return new IllegalStateException(
            format(
                "No field '%s' exists on type '%s'.",
                fieldName,
                type.getBriefDescription()
            )
        );
    }

    public static IllegalStateException genericMethodWithArgsDoesNotExistOnType(final String methodName, final Type type) {
        return new IllegalStateException(
            format(
                "No generic method '%s' on type '%s' is compatible with the supplied type arguments and arguments.  " +
                "No type arguments should be provided if the method is non-generic.",
                methodName,
                type.getBriefDescription()
            )
        );
    }

    public static IllegalStateException methodWithArgsDoesNotExistOnType(final String methodName, final Type type) {
        return new IllegalStateException(
            format(
                "No method '%s' on type '%s' is compatible with the supplied arguments.",
                methodName,
                type.getBriefDescription()
            )
        );
    }

    public static IllegalStateException methodWithMoreThanOneMatch(final String methodName, final Type type) {
        return new IllegalStateException(
            format(
                "More than one method '%s' on type '%s' is compatible with the supplied arguments.",
                methodName,
                type.getBriefDescription()
            )
        );
    }

    public static IllegalStateException argumentMustBeArrayIndexType() {
        return new IllegalStateException(
            "Expression must be an integer-based array index."
        );
    }

    public static IllegalStateException conversionIsNotSupportedForArithmeticTypes() {
        return new IllegalStateException(
            "A conversion expression is not supported for arithmetic types."
        );
    }

    public static IllegalStateException operandTypesDoNotMatchParameters(final ExpressionType nodeType, final MethodBase method) {
        return new IllegalStateException(
            format(
                "The operands for operator '%s' do not match the parameters of method '%s'.",
                nodeType,
                method.getName()
            )
        );
    }

    public static IllegalStateException overloadOperatorTypeDoesNotMatchConversionType(final ExpressionType nodeType, final MethodBase method) {
        return new IllegalStateException(
            format(
                "The return type of overload method for operator '%s' does not match the parameter " +
                "type of conversion method '%s'.",
                nodeType,
                method.getName()
            )
        );
    }

    public static IllegalStateException lambdaTypeMustBeSingleMethodInterface() {
        return new IllegalStateException("Lambda type parameter must be an interface type with exactly one method.");
    }

    public static IllegalStateException parameterExpressionNotValidForDelegate(final Type parameterType, final Type delegateParameterType) {
        return new IllegalStateException(
            format(
                "ParameterExpression of type '%s' cannot be used for delegate parameter of type '%s'.",
                parameterType.getBriefDescription(),
                delegateParameterType.getBriefDescription()
            )
        );
    }

    public static IllegalStateException labelMustBeVoidOrHaveExpression() {
        return new IllegalStateException("Label type must be void if an expression is not supplied.");
    }

    public static IllegalArgumentException expressionTypeDoesNotMatchLabel(final Type valueType, final Type expectedType) {
        return new IllegalArgumentException(
            format(
                "Expression of type '%s' cannot be used for return type '%s'.",
                valueType.getBriefDescription(),
                expectedType.getBriefDescription()
            )
        );
    }

    public static IllegalArgumentException expressionTypeCannotInitializeArrayType(final Type itemType, final Type arrayElementType) {
        return new IllegalArgumentException(
            format(
                "An expression of type '%s' cannot be used to initialize an array of type '%s'.",
                itemType.getBriefDescription(),
                arrayElementType.getBriefDescription()
            )
        );
    }

    public static IllegalStateException catchVariableMustBeCompatibleWithCatchType(final Type catchType, final Type variableType) {
        return new IllegalStateException(
            format(
                "A variable of type '%s' cannot be used with a catch block with filter type '%s'.",
                variableType.getBriefDescription(),
                catchType.getBriefDescription()
            )
        );
    }

    public static IllegalStateException bodyOfCatchMustHaveSameTypeAsBodyOfTry() {
        return new IllegalStateException("Body of catch must have the same type as body of try.");
    }

    public static IllegalStateException tryMustHaveCatchOrFinally() {
        return new IllegalStateException("A try expression must have at least one catch or finally clause.");
    }

    public static IllegalStateException invalidLValue(final ExpressionType nodeType) {
        return new IllegalStateException(
            format("Invalid lvalue for assignment: %s.", nodeType)
        );
    }

    public static IllegalStateException allCaseBodiesMustHaveSameType() {
        return new IllegalStateException(
            "All case bodies and the default body must have the same type."
        );
    }

    public static IllegalStateException allTestValuesMustHaveTheSameType() {
        return new IllegalStateException(
            "All test values must have the same type."
        );
    }

    public static IllegalStateException defaultBodyMustBeSupplied() {
        return new IllegalStateException(
            "Default body must be supplied if case bodies are not void."
        );
    }

    public static IllegalStateException testValueTypeDoesNotMatchComparisonMethodParameter(
        final Type testValueType,
        final Type parameterType) {

        return new IllegalStateException(
            format(
                "Test value of type '%s' cannot be used for the comparison method parameter of type '%s'.",
                testValueType.getBriefDescription(),
                parameterType.getBriefDescription()
            )
        );
    }

    public static IllegalStateException equalityMustReturnBoolean(final MethodBase method) {
        return new IllegalStateException(
            format(
                "The user-defined equality method '%s' must return a boolean value.",
                method.getName()
            )
        );
    }

    public static IllegalStateException cannotCompileConstant(final Object value) {
        VerifyArgument.notNull(value, "value");

        return new IllegalStateException(
            format(
                "Cannot compile complex constant of type %s because no closure is available.",
                Type.getType(value)
            )
        );
    }

    public static IllegalStateException undefinedVariable(final String name, final Type type, final String currentLambdaName) {
        return new IllegalStateException(
            format(
                "Undefined variable '%s' of type %s in lambda '%s'.",
                name,
                type,
                currentLambdaName
            )
        );
    }

    public static IllegalStateException couldNotCreateDelegate(final Throwable t) {
        return new IllegalStateException(
            "Could not create delegate.",
            t
        );
    }

    public static IllegalStateException labelTargetAlreadyDefined(final String name) {
        return new IllegalStateException(
            format("Target already defined for label '%s'.", name)
        );
    }

    public static IllegalStateException ambiguousJump(final String name) {
        return new IllegalStateException(
            format("Cannot jump to ambiguous label '%s'.", name)
        );
    }

    public static IllegalStateException nonLocalJumpWithValue(final String name) {
        return new IllegalStateException(
            format(
                "Cannot jump to non-local label '%s' with a value. Only jumps to labels " +
                "defined in outer blocks can pass values.", name
            )
        );
    }

    public static IllegalStateException controlCannotEnterExpression() {
        return new IllegalStateException(
            "Control cannot enter an expression; only statements can be jumped into."
        );
    }

    public static IllegalStateException controlCannotEnterTry() {
        return new IllegalStateException("Control cannot enter a try block.");
    }

    public static IllegalStateException labelTargetUndefined(final String name) {
        return new IllegalStateException(
            format("Cannot jump to undefined label '%s'.", name)
        );
    }

    public static IllegalArgumentException primitiveCannotBeTypeBinaryOperand() {
        return new IllegalArgumentException(
            "Primitive-typed expressions cannot be used as operands in InstanceOf " +
            "or TypeEqual expressions."
        );
    }

    public static IllegalArgumentException primitiveCannotBeTypeBinaryType() {
        return new IllegalArgumentException(
            "The target type of an InstanceOf or TypeEqual expression must be a" +
            "reference type."
        );
    }

    public static IllegalStateException incorrectNumberOfIndexes() {
        return new IllegalStateException("Incorrect number of indexes.");
    }

    public static IllegalStateException unexpectedCoalesceOperator() {
        return new IllegalStateException("Unexpected coalesce operator.");
    }

    public static IllegalStateException invalidMemberType(final MemberType memberType) {
        return new IllegalStateException(
            format("Invalid member type: %s.", memberType)
        );
    }

    public static IllegalStateException andAlsoCannotProvideMethod() {
        return new IllegalStateException(
            "AndAlso expressions cannot provide a method for evaluation because " +
            "short circuiting behavior would not be possible."
        );
    }

    public static IllegalStateException orElseCannotProvideMethod() {
        return new IllegalStateException(
            "OrElse expressions cannot provide a method for evaluation because " +
            "short circuiting behavior would not be possible."
        );
    }

    public static IllegalStateException coalesceUsedOnNonNullableType() {
        return new IllegalStateException("Coalesce used with type that cannot be null.");
    }

    public static IllegalStateException extensionNotReduced() {
        return new IllegalStateException("Extension should have been reduced.");
    }

    public static IllegalStateException tryNotAllowedInFilter() {
        return new IllegalStateException("Try expression is not allowed inside a filter body.");
    }

    public static IllegalArgumentException argumentMustBeReferenceType() {
        return new IllegalArgumentException("Argument must be a reference type.");
    }

    public static IllegalArgumentException initializerMustBeAssignableToVariable() {
        return new IllegalArgumentException("Initializer must be assignable to variable.");
    }

    public static IllegalArgumentException testMustBeBooleanExpression() {
        return new IllegalArgumentException("Test must be a boolean expression.");
    }

    public static IllegalArgumentException continueTargetMustBeVoid() {
        return new IllegalArgumentException("Continue label target must be void.");
    }

    public static IllegalStateException cannotAccessThisFromStaticMember() {
        return new IllegalStateException("Cannot access 'this' from s static member.");
    }

    public static IllegalStateException incorrectlyTypedSelfExpression(final Type<?> expected, final Type<?> actual) {
        return new IllegalStateException(
            format(
                "Incorrectly typed 'this' expression: expected '%s', found '%s'.",
                expected.getName(),
                actual.getName()
            )
        );
    }

    public static IllegalStateException incorrectlyTypedSuperExpression(final Type<?> expected, final Type<?> actual) {
        return new IllegalStateException(
            format(
                "Incorrectly typed 'super' expression: expected '%s', found '%s'.",
                expected.getName(),
                actual.getName()
            )
        );
    }

    public static IllegalArgumentException twoOrMoreOperandsRequired() {
        return new IllegalArgumentException(
            "At least two operands are required for a binary operation.");
    }

    public static IllegalArgumentException concatRequiresAtLeastTwoOperands() {
        return new IllegalArgumentException(
            "A concat() expression requires at least two operands."
        );
    }

    public static IllegalArgumentException dynamicMethodCallRequiresTargetOrMethodHandle() {
        return new IllegalArgumentException(
            "Invocation target required for DynamicMethod call.  A target " +
            "instance must be provided, or the DynamicMethod must be constructed " +
            "from a MethodHandle."
        );
    }
}
