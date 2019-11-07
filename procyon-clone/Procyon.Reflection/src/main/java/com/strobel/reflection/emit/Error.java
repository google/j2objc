/*
 * Error.java
 *
 * Copyright (c) 2012 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.reflection.emit;

import com.strobel.core.VerifyArgument;
import com.strobel.reflection.MemberInfo;
import com.strobel.reflection.MethodBase;
import com.strobel.reflection.Type;
import com.strobel.util.ContractUtils;

import java.lang.annotation.Annotation;

import static java.lang.String.format;

/**
 * @author strobelm
 */
final class Error {
    private Error() {
        throw ContractUtils.unreachable();
    }

    public static RuntimeException bytecodeGeneratorNotOwnedByMethodBuilder() {
        return new RuntimeException(
            "This CodeGenerator was not created by a MethodBuilder."
        );
    }

    public static RuntimeException typeHasBeenCreated() {
        return new RuntimeException(
            "Operation cannot be performed after createType() has been called."
        );
    }

    public static RuntimeException typeHasNotBeenCreated() {
        return new RuntimeException(
            "Operation cannot be performed until createType() has been called."
        );
    }

    public static RuntimeException typeIsGeneric() {
        return new IllegalStateException(
            "Operation is not valid on bound generic types."
        );
    }

    public static IllegalArgumentException memberContainsUnboundGenericParameters(final MemberInfo member) {
        return new IllegalArgumentException(
            format(
                "Member '%s' has unbound generic parameters in its signature.",
                member.getName()
            )
        );
    }

    public static RuntimeException methodIsGeneric() {
        return new IllegalStateException(
            "Operation is not valid on bound generic methods."
        );
    }

    public static RuntimeException methodIsFinished() {
        return new RuntimeException(
            "Cannot modify a method after it has been finished."
        );
    }

    public static RuntimeException unmatchedLocal() {
        return new RuntimeException(
            "Local variable does not belong to this method."
        );
    }

    public static RuntimeException badLabel() {
        return new RuntimeException(
            "Label does not belong to this method."
        );
    }

    public static RuntimeException badLabelContent() {
        return new RuntimeException(
            "Label has no value."
        );
    }

    public static RuntimeException labelAlreadyDefined() {
        return new RuntimeException(
            "Label has already been defined."
        );
    }

    public static RuntimeException unclosedExceptionBlock() {
        return new RuntimeException(
            "Unclosed exception block."
        );
    }

    public static RuntimeException illegalTwoByteBranch(final int position, final int address) {
        return new RuntimeException(
            format(
                "Illegal two byte branch (position = %s, address = %s).",
                position,
                address
            )
        );
    }

    public static RuntimeException invokeOpCodeRequired() {
        return new RuntimeException(
            "OpCode must be one of: INVOKEDYNAMIC, INVOKEINTERFACE, INVOKESPECIAL, INVOKESTATIC, INVOKEVIRTUAL"
        );
    }

    public static RuntimeException invalidType(final Type<?> type) {
        return new RuntimeException(
            format("Invalid type: %s", type)
        );
    }

    public static RuntimeException constructorNotFound() {
        return new RuntimeException(
            "Type does not have a constructor matching the specified arguments."
        );
    }

    public static RuntimeException cannotInstantiateUnboundGenericType(final Type<?> type) {
        return new RuntimeException(
            format(
                "Cannot instantiate type '%s' because it has unbound generic type parameters.",
                type
            )
        );
    }

    public static RuntimeException boxFailure(final Type<?> type) {
        return new RuntimeException(
            format(
                "Could not find a boxing method or constructor for type '%s'.",
                type
            )
        );
    }

    public static RuntimeException cannotConvertToOrFromVoid() {
        return new RuntimeException(
            "Cannot convert to or from 'void'."
        );
    }

    public static RuntimeException invalidCast(final Type sourceType, final Type targetType) {
        return new RuntimeException(
            format(
                "Cannot cast from '%s' to '%s'.",
                sourceType,
                targetType
            )
        );
    }

    public static RuntimeException newArrayDimensionsOutOfRange(final Type<?> arrayType, final int dimensions) {
        VerifyArgument.notNull(arrayType, "arrayType");

        int actualDimensions = 0;
        Type<?> currentType = arrayType;

        while (currentType.isArray()) {
            ++actualDimensions;
            currentType = arrayType.getElementType();
        }

        return new RuntimeException(
            format(
                "Cannot initialize %s dimensions of a(n) %s because the array only has %s dimensions.",
                dimensions,
                arrayType,
                actualDimensions
            )
        );
    }

    public static RuntimeException argumentIndexOutOfRange(final MethodBase method, final int index) {
        return new RuntimeException(
            format(
                "Argument %s is out of range.  Method: %s",
                index,
                method
            )
        );
    }

    public static RuntimeException cannotLoadThisForStaticMethod() {
        return new RuntimeException(
            "Cannot reference 'this' from within a static method."
        );
    }

    public static RuntimeException invalidBranchOpCode(final OpCode opCode) {
        return new RuntimeException(
            format("Expected a GOTO or JSR opcode, but found %s.", opCode)
        );
    }

    public static RuntimeException cannotModifyTypeAfterCreateType() {
        return new IllegalStateException("Type cannot be modified after calling createType().");
    }

    public static RuntimeException typeNameTooLong() {
        return new IllegalArgumentException("The specified name is too long.");
    }

    public static RuntimeException baseTypeCannotBeInterface() {
        return new IllegalArgumentException("Base type cannot be an interface.");
    }

    public static RuntimeException baseTypeCannotBeGenericParameter() {
        return new IllegalArgumentException("Base type cannot be a generic type parameter.");
    }

    public static RuntimeException typeCannotHaveItselfAsBaseType() {
        return new IllegalArgumentException("A type cannot have itself as its base type.");
    }

    public static RuntimeException typeCannotHaveItselfAsInterface() {
        return new IllegalArgumentException("A type cannot have itself as an interface.");
    }

    public static RuntimeException typeMustBeInterface() {
        return new IllegalArgumentException("Type is not an interface.");
    }

    public static RuntimeException typeMustBeInterface(final Type<?> type) {
        if (type == null) {
            return typeMustBeInterface();
        }

        return new IllegalArgumentException(format("Type %s is not an interface.", type.getName()));
    }

    public static RuntimeException typeNotCreated() {
        return new RuntimeException(
            "Type has not been created yet."
        );
    }

    public static RuntimeException cannotModifyMethodAfterCallingGetGenerator() {
        return new IllegalStateException("Method cannot be modified after calling getCodeGenerator().");
    }

    public static RuntimeException genericParametersAlreadySet() {
        return new IllegalStateException("Generic parameters have already been defined.");
    }

    public static RuntimeException methodHasOpenLocalScope() {
        return new IllegalStateException("Method body still has an open local scope.");
    }

    public static RuntimeException abstractMethodDeclaredOnNonAbstractType() {
        return new IllegalStateException("Abstract method declared on non-abstract class.");
    }

    public static RuntimeException abstractMethodCannotHaveBody() {
        return new IllegalStateException("Abstract method cannot have a body.");
    }

    public static RuntimeException methodHasEmptyBody(final MethodBuilder method) {
        return new IllegalStateException(
            format(
                "Method '%s' on type '%s' has an empty body.", method.getName(),
                method.getDeclaringType().getName()
            )
        );
    }

    public static RuntimeException notInExceptionBlock() {
        return new IllegalStateException("Not in an exception block.");
    }

    public static RuntimeException badExceptionCodeGenerated() {
        return new IllegalStateException("Incorrect code generated for exception block.");
    }

    public static RuntimeException catchRequiresThrowableType() {
        return new IllegalStateException("Catch block requires a Throwable type.");
    }

    public static RuntimeException couldNotLoadUnsafeClassInstance() {
        return new IllegalStateException("Could not load an instance of the sun.misc.Unsafe class.");
    }

    public static RuntimeException valueMustBeConstant() {
        return new IllegalArgumentException("Value must be a primitive compile-time constant.");
    }

    public static RuntimeException annotationRequiresValue(final Type<? extends Annotation> annotationType) {
        return new IllegalArgumentException(
            format(
                "Annotation '%s' requires an argument.",
                annotationType.getName()
            )
        );
    }

    public static RuntimeException attributeValueCountMismatch() {
        return new IllegalArgumentException("A matching number of attributes and values is required.");
    }

    public static RuntimeException attributeValueIncompatible(final Type<?> attributeType, final Type<?> valueType) {
        if (valueType == null || valueType == Type.NullType) {
            return new IllegalArgumentException(
                format(
                    "A null value is invalid for a attribute of type '%s'.",
                    attributeType.getName()
                )
            );
        }
        return new IllegalArgumentException(
            format(
                "A value of type '%s' is invalid for a attribute of type '%s'.",
                valueType.getName(),
                attributeType.getName()
            )
        );
    }

    public static RuntimeException annotationHasNoDefaultAttribute() {
        return new IllegalArgumentException("Annotation has no default attribute.");
    }

    public static RuntimeException typeNotAnAnnotation(final Type<? extends Annotation> type) {
        return new IllegalArgumentException(
            format("Type '%s' is not an annotation.", type.getName())
        );
    }

    public static RuntimeException classGenerationFailed(final TypeBuilder<?> t, final Throwable e) {
        return new IllegalStateException(
            format("Class generation failed for type '%s'.", t.getName()),
            e
        );
    }

    public static RuntimeException onlyAnnotationMethodsCanHaveDefaultValues() {
        return new IllegalStateException("Only annotation methods can have default values.");
    }

    public static RuntimeException genericTypeDefinitionRequired() {
        return new IllegalArgumentException("The specified type is not a generic type definition.");
    }

    public static RuntimeException interfacesCannotDefineConstructors() {
        return new IllegalStateException("Interfaces cannot define constructors.");
    }

    public static RuntimeException baseTypeHasNoDefaultConstructor(final Type<?> baseType) {
        if (baseType != null) {
            return new IllegalStateException(
                format(
                    "Base type '%s' has no visible default constructor.",
                    baseType
                )
            );
        }
        return new IllegalStateException("Base type has no visible default constructor.");
    }

    public static RuntimeException noCodeGeneratorForDefaultConstructor() {
        return new IllegalStateException("No code generator is available for a default constructor.");
    }

    public static IllegalArgumentException typeMustBeArray() {
        return new IllegalArgumentException("Type must be an array type.");
    }

    public static IllegalStateException branchAddressTooLarge() {
        return new IllegalStateException(
            "Branch address too large; expected 2 byte address, found 4 byte address.");
    }

    public static IllegalStateException checkedExceptionUnhandled(final Type<?> exceptionType) {
        return new IllegalStateException(
            format(
                "Method must handle '%s' or include it in its list of thrown types.",
                exceptionType.getName()
            )
        );
    }

    public static IllegalArgumentException methodBuilderBelongsToAnotherType() {
        return new IllegalArgumentException("The provided method belongs to another type.");
    }

    public static IllegalArgumentException parameterCountMismatch() {
        return new IllegalArgumentException(
            "The specified methods have different numbers of parameters."
        );
    }

    public static IllegalArgumentException incompatibleReturnTypes() {
        return new IllegalArgumentException(
            "Methods which return void cannot override methods which do not return void, " +
            "and vice versa."
        );
    }

    public static IllegalArgumentException methodNameMismatch() {
        return new IllegalArgumentException(
            "Cannot override a method with a different name."
        );
    }

    public static IllegalArgumentException staticInstanceMethodMismatch() {
        return new IllegalArgumentException(
            "Static methods cannot be overridden, and static methods cannot override" +
            " instance methods."
        );
    }

    public static IllegalArgumentException cannotOverrideFinalMethod() {
        return new IllegalArgumentException("Cannot override a final method.");
    }

    public static IllegalStateException codeGenerationException(final Throwable t) {
        return new IllegalStateException("An error occurred during code generation.", t);
    }

    public static RuntimeException notGenericType(final Type type) {
        return new UnsupportedOperationException(
            format(
                "Type '%s' is not a generic type.",
                type.getFullName()
            )
        );
    }

    public static UnsupportedOperationException notGenericTypeDefinition(final Type<?> type) {
        return new UnsupportedOperationException(
            format(
                "Type '%s' is not a generic type definition.",
                type.getFullName()
            )
        );
    }

    public static IllegalStateException defineGenericParametersAlreadyCalled() {
        return new IllegalStateException("defineGenericParameters() has already been called.");

    }

    public static IllegalArgumentException argumentMustBeTypeBuilder() {
        return new IllegalArgumentException("Argument must be a TypeBuilder.");
    }

    public static IllegalStateException cannotModifyFieldAfterTypeCreated() {
        return new IllegalStateException("Field cannot be modified after declaring type has been created.");
    }
}
