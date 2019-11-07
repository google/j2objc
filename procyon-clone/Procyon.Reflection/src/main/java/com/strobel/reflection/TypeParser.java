/*
 * TypeParser.java
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

import com.strobel.core.MutableInteger;
import com.strobel.core.VerifyArgument;

/**
 * @author strobelm
 */
final class TypeParser {
    public static Type<?> parse(final String value) {
        VerifyArgument.notNullOrWhitespace(value, "value");

        switch (value.charAt(0)) {
            case 'L':
            case 'T':
                if (value.charAt(value.length() - 1) == ';') {
                    return parseSignature(value);
                }
                break;

            case '[':
                return parseSignature(value);
        }

        final int primitiveHash = hashPrimitiveName(value);
        final Type<?> primitiveType = PRIMITIVE_TYPES[primitiveHash];

        if (primitiveType != null && value.equals(primitiveType.getName())) {
            return primitiveType;
        }

        try {
            final Class<?> c = Class.forName(value);

            if (c != null) {
                return Type.of(c);
            }
        }
        catch (ClassNotFoundException ignored) {
        }

        return parseTopLevelSignature(value, new MutableInteger());
    }

    public static Type<?> parseSignature(final String signature) {
        VerifyArgument.notNullOrWhitespace(signature, "signature");

        return parseTopLevelSignature(signature, new MutableInteger());
    }

    private static Type<?> parseTopLevelSignature(final String s, final MutableInteger position) {
        final int i = position.getValue();

        if (i >= s.length()) {
            throw Error.invalidSignatureTypeExpected(s, i);
        }

        switch (s.charAt(i)) {
            case '*':
                return Type.makeWildcard();
            case '+':
                return Type.makeExtendsWildcard(parseTopLevelSignature(s, position.increment()));
            case '-':
                return Type.makeSuperWildcard(parseTopLevelSignature(s, position.increment()));
            case '[':
                return parseTopLevelSignature(s, position.increment()).makeArrayType();
            case 'B':
                return PrimitiveTypes.Byte;
            case 'C':
                return PrimitiveTypes.Character;
            case 'D':
                return PrimitiveTypes.Double;
            case 'F':
                return PrimitiveTypes.Float;
            case 'I':
                return PrimitiveTypes.Integer;
            case 'J':
                return PrimitiveTypes.Long;
            case 'L':
                return finishTopLevelType(s, position);
            case 'S':
                return PrimitiveTypes.Short;
            case 'T':
                throw Error.invalidSignatureTopLevelGenericParameterUnexpected(s, position.getValue());
            case 'V':
                return PrimitiveTypes.Void;
            case 'Z':
                return PrimitiveTypes.Boolean;
            default:
                throw Error.invalidSignatureUnexpectedToken(s, i);
        }
    }

    private static Type<?> finishTopLevelType(final String s, final MutableInteger position) {
        int i = position.getValue();

        assert s.charAt(i) == 'L';

        final Type<?> resolvedType;
        final Type<?>[] typeArguments;

        final StringBuilder sb = new StringBuilder();

        while (++i < s.length()) {
            final char c = s.charAt(i);

            switch (c) {
                case '/': {
                    sb.append('.');
                    continue;
                }

                case ';': {
                    try {
                        position.setValue(i + 1);
                        resolvedType = Type.of(Class.forName(sb.toString()));

                        if (resolvedType.isGenericTypeDefinition()) {
                            return resolvedType.getErasedType();
                        }

                        return resolvedType;
                    }
                    catch (ClassNotFoundException e) {
                        throw Error.couldNotResolveType(sb);
                    }
                }
                case '<': {
                    try {
                        resolvedType = Type.of(Class.forName(sb.toString()));

                        if (!resolvedType.isGenericType()) {
                            throw Error.invalidSignatureNonGenericTypeTypeArguments(resolvedType);
                        }

                        typeArguments = new Type<?>[resolvedType.getTypeArguments().size()];
                        position.setValue(i);

                        parseTypeParameters(s, position, resolvedType, typeArguments);

                        i = position.getValue();

                        if (s.charAt(i) != ';') {
                            throw Error.invalidSignatureUnexpectedToken(s, i);
                        }

                        position.increment();

                        final TypeBindings typeBindings = TypeBindings.create(
                            resolvedType.getGenericTypeParameters(),
                            typeArguments
                        );

                        if (typeBindings.hasBoundParameters()) {
                            return resolvedType.makeGenericType(typeArguments);
                        }

                        return resolvedType;
                    }
                    catch (ClassNotFoundException e) {
                        throw Error.couldNotResolveType(sb);
                    }
                }

                default: {
                    sb.append(c);
                }
            }
        }

        throw Error.invalidSignatureUnexpectedEnd(s, i);
    }

    private static void parseTypeParameters(
        final String s,
        final MutableInteger position,
        final Type<?> resolvedType,
        final Type<?>[] typeArguments) {

        int i = position.getValue();

        assert s.charAt(i) == '<';

        position.increment();

        for (int j = 0; j < typeArguments.length; j++) {
            typeArguments[j] = parseTypeArgument(s, position, resolvedType, j);
        }

        i = position.getValue();

        if (s.charAt(i) != '>') {
            throw Error.invalidSignatureExpectedEndOfTypeArguments(s, i);
        }

        position.increment();
    }

    private static Type<?> parseTypeArgument(
        final String s,
        final MutableInteger position,
        final Type<?> genericType,
        final int typeArgumentIndex) {

        int i = position.getValue();

        if (i >= s.length()) {
            throw Error.invalidSignatureExpectedTypeArgument(s, i);
        }

        switch (s.charAt(i)) {
            case '*':
                return Type.makeWildcard();
            case '+':
                return Type.makeExtendsWildcard(parseTypeArgument(s, position.increment(), genericType, typeArgumentIndex));
            case '-':
                return Type.makeSuperWildcard(parseTypeArgument(s, position.increment(), genericType, typeArgumentIndex));
            case '[':
                return parseTypeArgument(s, position.increment(), genericType, typeArgumentIndex).makeArrayType();
            case 'L':
                return finishTopLevelType(s, position);
            case 'T':
                while (++i < s.length()) {
                    if (s.charAt(i) == ';') {
                        position.setValue(i + 1);
                        return genericType.getGenericTypeParameters().get(typeArgumentIndex);
                    }
                }
                throw Error.invalidSignatureExpectedTypeArgument(s, position.getValue());
            default:
                throw Error.invalidSignatureUnexpectedToken(s, i);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Primitive Lookup">

    private static final Type<?>[] PRIMITIVE_TYPES = new Type<?>[16];

    static {
        for (final Type<?> t : PrimitiveTypes.allPrimitives()) {
            PRIMITIVE_TYPES[hashPrimitiveName(t.getName())] = t;
        }
    }

    private static int hashPrimitiveName(final String name) {
        if (name.length() < 3) {
            return 0;
        }
        return (name.charAt(0) + name.charAt(2)) % 16;
    }

    // </editor-fold>
}
