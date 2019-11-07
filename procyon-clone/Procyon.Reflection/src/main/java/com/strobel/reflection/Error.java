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

package com.strobel.reflection;

import static java.lang.String.format;

/**
 * @author Mike Strobel
 */
final class Error {
    private Error() {
    }

    public static RuntimeException notGenericParameter(final Type type) {
        return new UnsupportedOperationException(
            format(
                "Type '%s' is not a generic parameter.",
                type.getFullName()
            )
        );
    }

    public static RuntimeException notWildcard(final Type type) {
        throw new UnsupportedOperationException(
            format(
                "Type '%s' is not a wildcard or captured type.",
                type.getFullName()
            )
        );
    }

    public static RuntimeException notBoundedType(final Type type) {
        throw new UnsupportedOperationException(
            format(
                "Type '%s' is not a bounded type.",
                type.getFullName()
            )
        );
    }

    public static RuntimeException notGenericType(final Type type) {
        return new UnsupportedOperationException(
            format(
                "Type '%s' is not a generic type.",
                type.getFullName()
            )
        );
    }

    public static RuntimeException notGenericMethod(final MethodInfo method) {
        return new UnsupportedOperationException(
            format(
                "Type '%s' is not a generic method.",
                method.getName()
            )
        );
    }

    public static RuntimeException notGenericMethodDefinition(final MethodInfo method) {
        return new UnsupportedOperationException(
            format(
                "Type '%s' is not a generic method definition.",
                method.getName()
            )
        );
    }

    public static RuntimeException noElementType(final Type type) {
        return new UnsupportedOperationException(
            format(
                "Type '%s' does not have an element type.",
                type.getFullName()
            )
        );
    }

    public static RuntimeException notEnumType(final Type type) {
        return new UnsupportedOperationException(
            format(
                "Type '%s' is not an enum type.",
                type.getFullName()
            )
        );
    }

    public static RuntimeException notArrayType(final Type type) {
        return new UnsupportedOperationException(
            format(
                "Type '%s' is not an array type.",
                type.getFullName()
            )
        );
    }

    public static RuntimeException ambiguousMatch() {
        return new RuntimeException("Ambiguous match found.");
    }

    public static RuntimeException incorrectNumberOfTypeArguments() {
        return new UnsupportedOperationException(
            "Incorrect number of type arguments provided."
        );
    }

    public static RuntimeException incorrectNumberOfTypeArguments(final Type type) {
        return new UnsupportedOperationException(
            format(
                "Incorrect number of type arguments provided for generic type '%s'.",
                type.getFullName()
            )
        );
    }

    public static RuntimeException notGenericTypeDefinition(final Type type) {
        return new UnsupportedOperationException(
            format(
                "Type '%s' is not a generic type definition.",
                type.getFullName()
            )
        );
    }

    public static RuntimeException notPrimitiveType(final Class<?> type) {
        return new UnsupportedOperationException(
            format(
                "Type '%s' is not a primitive type.",
                type.getName()
            )
        );
    }

    public static RuntimeException typeParameterNotDefined(final Type typeParameter) {
        return new UnsupportedOperationException(
            format(
                "Generic parameter '%s' is not defined on this type.",
                typeParameter.getFullName()
            )
        );
    }

    public static RuntimeException couldNotResolveMethod(final Object signature) {
        return new RuntimeException(
            format(
                "Could not resolve method '%s'.",
                signature
            )
        );
    }

    public static RuntimeException couldNotResolveMember(final MemberInfo member) {
        return new MemberResolutionException(member);
    }

    public static RuntimeException couldNotResolveType(final Object signature) {
        return new RuntimeException(
            format(
                "Could not resolve type '%s'.",
                signature
            )
        );
    }

    public static RuntimeException couldNotResolveParameterType(final Object signature) {
        return new RuntimeException(
            format(
                "Could not resolve type for parameter '%s'.",
                signature
            )
        );
    }

    public static RuntimeException typeArgumentsMustContainBoundType() {
        return new RuntimeException(
            "Type arguments must bind at least one generic parameter."
        );
    }

    public static RuntimeException compoundTypeMayOnlyHaveOneClassBound() {
        return new RuntimeException(
            "Compound types may only be bounded by one class, and it must be the first type in " +
            "the bound list.  All other bounds must be interface types."
        );
    }

    public static RuntimeException compoundTypeMayNotHaveGenericParameterBound() {
        return new RuntimeException(
            "Compound types may not be bounded by a generic parameter."
        );
    }

    public static RuntimeException typeCannotBeInstantiated(final Type<?> t) {
        return new IllegalStateException(
            format("Type '%s' cannot be instantiated.", t)
        );
    }

    public static RuntimeException typeInstantiationFailed(final Type<?> t, final Throwable cause) {
        return new IllegalStateException(
            format("Failed to instantiate type '%s'.", t),
            cause
        );
    }

    public static RuntimeException rawFieldBindingFailure(final FieldInfo field) {
        return new IllegalStateException(
            format(
                "Could not bind to runtime field '%s' on type '%s'.",
                field.getDescription(),
                field.getDeclaringType().toString()
            )
        );
    }

    public static RuntimeException rawMethodBindingFailure(final MethodBase method) {
        return new IllegalStateException(
            format(
                "Could not bind to runtime method '%s' on type '%s'.",
                method.getDescription(),
                method.getDeclaringType().toString()
            )
        );
    }

    public static RuntimeException targetInvocationException(final Throwable cause) {
        return new TargetInvocationException(cause);
    }

    public static MemberResolutionException couldNotResolveMatchingConstructor() {
        return new MemberResolutionException(
            "Could not find a constructor matching the provided arguments."
        );
    }

    public static RuntimeException invalidAncestorType(final Type<?> ancestorType, final Type<?> declaringType) {
        return new RuntimeException(
            format(
                "Type '%s' is not an ancestor of type '%s'.",
                ancestorType.getFullName(),
                declaringType.getFullName()
            )
        );
    }

    public static RuntimeException invalidSignatureTypeExpected(final String signature, final int position) {
        return new IllegalArgumentException(
            format(
                "Invalid signature: type expected at position %d (%s).",
                position,
                signature
            )
        );
    }
    public static RuntimeException invalidSignatureTopLevelGenericParameterUnexpected(final String signature, final int position) {
        return new IllegalArgumentException(
            format(
                "Invalid signature: unexpected generic parameter at position %d.  (%s)",
                position,
                signature
            )
        );
    }

    public static RuntimeException invalidSignatureNonGenericTypeTypeArguments(final Type<?> type) {
        return new IllegalArgumentException(
            format(
                "Invalid signature: unexpected type arguments specified for non-generic type '%s'.",
                type.getBriefDescription()
            )
        );
    }

    public static RuntimeException invalidSignatureUnexpectedToken(final String signature, final int position) {
        return new IllegalArgumentException(
            format(
                "Invalid signature: unexpected token at position %d.  (%s)",
                position,
                signature
            )
        );
    }

    public static RuntimeException invalidSignatureUnexpectedEnd(final String signature, final int position) {
        return new IllegalArgumentException(
            format(
                "Invalid signature: unexpected end of signature at position %d.  (%s)",
                position,
                signature
            )
        );
    }

    public static RuntimeException invalidSignatureExpectedEndOfTypeArguments(final String signature, final int position) {
        return new IllegalArgumentException(
            format(
                "Invalid signature: expected end of type argument list at position %d.  (%s)",
                position,
                signature
            )
        );
    }

    public static RuntimeException invalidSignatureExpectedTypeArgument(final String signature, final int position) {
        return new IllegalArgumentException(
            format(
                "Invalid signature: expected type argument at position %d.  (%s)",
                position,
                signature
            )
        );
    }
}
