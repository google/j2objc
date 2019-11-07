/*
 * Error.java
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

package com.strobel.assembler.metadata;

import static java.lang.String.format;

/**
 * @author Mike Strobel
 */
final class Error {
    public static RuntimeException notGenericParameter(final TypeReference type) {
        return new UnsupportedOperationException(
            format(
                "TypeReference '%s' is not a generic parameter.",
                type.getFullName()
            )
        );
    }

    public static RuntimeException notWildcard(final TypeReference type) {
        throw new UnsupportedOperationException(
            format(
                "TypeReference '%s' is not a wildcard or captured type.",
                type.getFullName()
            )
        );
    }

    public static RuntimeException notBoundedType(final TypeReference type) {
        throw new UnsupportedOperationException(
            format(
                "TypeReference '%s' is not a bounded type.",
                type.getFullName()
            )
        );
    }

    public static RuntimeException notGenericType(final TypeReference type) {
        return new UnsupportedOperationException(
            format(
                "TypeReference '%s' is not a generic type.",
                type.getFullName()
            )
        );
    }

    public static RuntimeException notGenericMethod(final MethodReference method) {
        return new UnsupportedOperationException(
            format(
                "TypeReference '%s' is not a generic method.",
                method.getName()
            )
        );
    }

    public static RuntimeException notGenericMethodDefinition(final MethodReference method) {
        return new UnsupportedOperationException(
            format(
                "TypeReference '%s' is not a generic method definition.",
                method.getName()
            )
        );
    }

    public static RuntimeException noElementType(final TypeReference type) {
        return new UnsupportedOperationException(
            format(
                "TypeReference '%s' does not have an element type.",
                type.getFullName()
            )
        );
    }

    public static RuntimeException notEnumType(final TypeReference type) {
        return new UnsupportedOperationException(
            format(
                "TypeReference '%s' is not an enum type.",
                type.getFullName()
            )
        );
    }

    public static RuntimeException notArrayType(final TypeReference type) {
        return new UnsupportedOperationException(
            format(
                "TypeReference '%s' is not an array type.",
                type.getFullName()
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

    public static RuntimeException invalidSignatureNonGenericTypeTypeArguments(final TypeReference type) {
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

    public static RuntimeException invalidSignatureExpectedEndOfTypeVariables(final String signature, final int position) {
        return new IllegalArgumentException(
            format(
                "Invalid signature: expected end of type variable list at position %d.  (%s)",
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

    public static RuntimeException invalidSignatureExpectedParameterList(final String signature, final int position) {
        return new IllegalArgumentException(
            format(
                "Invalid signature: expected parameter type list at position %d.  (%s)",
                position,
                signature
            )
        );
    }

    public static RuntimeException invalidSignatureExpectedReturnType(final String signature, final int position) {
        return new IllegalArgumentException(
            format(
                "Invalid signature: expected return type at position %d.  (%s)",
                position,
                signature
            )
        );
    }

    public static RuntimeException invalidSignatureExpectedTypeVariable(final String signature, final int position) {
        return new IllegalArgumentException(
            format(
                "Invalid signature: expected type variable name at position %d.  (%s)",
                position,
                signature
            )
        );
    }

    public static RuntimeException invalidSignatureUnresolvedTypeVariable(
        final String signature,
        final String variableName,
        final int position) {

        return new IllegalArgumentException(
            format(
                "Invalid signature: unresolved type variable '%s' at position %d.  (%s)",
                variableName,
                position,
                signature
            )
        );
    }

    public static RuntimeException couldNotLoadObjectType() {
        throw new IllegalStateException("Could not load metadata for java.lang.Object.");
    }

    public static RuntimeException couldNotLoadClassType() {
        throw new IllegalStateException("Could not load metadata for java.lang.Class.");
    }
}
