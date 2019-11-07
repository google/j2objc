/*
m * Copyright (c) 2003, 2005, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.strobel.assembler.metadata.signatures;

import java.lang.reflect.GenericSignatureFormatError;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class SignatureParser {
    private final static boolean DEBUG = Boolean.getBoolean("DEBUG");
    private final static TypeArgument[] EMPTY_TYPE_ARGUMENTS = new TypeArgument[0];
    private final static char EOI = ':';

    private char[] input; // the input signature
    private int index = 0; // index into the input

    private SignatureParser() {
    }

    public static SignatureParser make() {
        return new SignatureParser();
    }

    private char current() {
        assert (index <= input.length);
        try {
            if (index < input.length) {
                return input[index];
            }
        }
        catch (final ArrayIndexOutOfBoundsException ignored) {
        }

        return EOI;
    }

    private void advance() {
        assert (index <= input.length);
        index++;
    }

    private Error error(final String errorMsg) {
        if (DEBUG) {
            System.out.println("Parse error:" + errorMsg);
        }
        return new GenericSignatureFormatError();
    }

    public ClassSignature parseClassSignature(final String s) {
        if (DEBUG) {
            System.out.println("Parsing class sig:" + s);
        }
        input = s.toCharArray();
        index = 0;
        return parseClassSignature();
    }

    public MethodTypeSignature parseMethodSignature(final String s) {
        if (DEBUG) {
            System.out.println("Parsing method sig:" + s);
        }
        input = s.toCharArray();
        index = 0;
        return parseMethodTypeSignature();
    }

    public TypeSignature parseTypeSignature(final String s) {
        if (DEBUG) {
            System.out.println("Parsing type sig:" + s);
        }
        input = s.toCharArray();
        index = 0;
        return parseTypeSignature();
    }

    private ClassSignature parseClassSignature() {
        assert (index == 0);

        return ClassSignature.make(
            parseZeroOrMoreFormalTypeParameters(),
            parseClassTypeSignature(),
            parseSuperInterfaces()
        );
    }

    private FormalTypeParameter[] parseZeroOrMoreFormalTypeParameters() {
        if (current() == '<') {
            return parseFormalTypeParameters();
        }
        else {
            return new FormalTypeParameter[0];
        }
    }

    private FormalTypeParameter[] parseFormalTypeParameters() {
        final Collection<FormalTypeParameter> ftps = new ArrayList<>(3);

        assert (current() == '<');

        if (current() != '<') {
            throw error("expected <");
        }

        advance();
        ftps.add(parseFormalTypeParameter());

        while (current() != '>') {
            ftps.add(parseFormalTypeParameter());
        }

        advance();

        final FormalTypeParameter[] formalTypeParameters = new FormalTypeParameter[ftps.size()];

        return ftps.toArray(formalTypeParameters);
    }

    private FormalTypeParameter parseFormalTypeParameter() {
        return FormalTypeParameter.make(
            parseIdentifier(),
            parseZeroOrMoreBounds()
        );
    }

    private String parseIdentifier() {
        final StringBuilder result = new StringBuilder();
        while (!Character.isWhitespace(current())) {
            final char c = current();
            switch (c) {
                case ';':
                case '.':
                case '/':
                case ':':
                case '>':
                case '<':
                    return result.toString();
                default: {
                    result.append(c);
                    advance();
                }
            }
        }
        return result.toString();
    }

    private FieldTypeSignature parseFieldTypeSignature() {
        switch (current()) {
            case 'L':
                return parseClassTypeSignature();
            case 'T':
                return parseTypeVariableSignature();
            case '[':
                return parseArrayTypeSignature();
            default:
                throw error("Expected Field Type Signature");
        }
    }

    private ClassTypeSignature parseClassTypeSignature() {
        assert (current() == 'L');

        if (current() != 'L') {
            throw error("expected a class type");
        }

        advance();

        final List<SimpleClassTypeSignature> typeSignatures = new ArrayList<>(5);

        typeSignatures.add(parseSimpleClassTypeSignature(false));
        parseClassTypeSignatureSuffix(typeSignatures);

        if (current() != ';') {
            throw error("expected ';' got '" + current() + "'");
        }

        advance();

        return ClassTypeSignature.make(typeSignatures);
    }

    private SimpleClassTypeSignature parseSimpleClassTypeSignature(final boolean dollar) {
        final String id = parseIdentifier();
        final int position = index;
        final char c = current();

        switch (c) {
            case ';':
            case '/':
            case '.':
            case '$': {
                return SimpleClassTypeSignature.make(id, dollar, new TypeArgument[0]);
            }

            case '<': {
                return SimpleClassTypeSignature.make(id, dollar, parseTypeArguments());
            }

            default: {
                throw error(position + ": expected < or ; or /");
            }
        }
    }

    private void parseClassTypeSignatureSuffix(final List<SimpleClassTypeSignature> typeSignatures) {
        while (current() == '/' || current() == '.') {
            final boolean dollar = (current() == '.');
            advance();
            typeSignatures.add(parseSimpleClassTypeSignature(dollar));
        }
    }

    private TypeArgument[] parseTypeArguments() {
        final Collection<TypeArgument> tas = new ArrayList<>(3);
        assert (current() == '<');
        if (current() != '<') {
            throw error("expected <");
        }
        advance();
        tas.add(parseTypeArgument());
        while (current() != '>') {
            //(matches(current(),  '+', '-', 'L', '[', 'T', '*')) {
            tas.add(parseTypeArgument());
        }
        advance();
        final TypeArgument[] taa = new TypeArgument[tas.size()];
        return tas.toArray(taa);
    }

    private TypeArgument parseTypeArgument() {
        final char c = current();

        switch (c) {
            case '+': {
                advance();
                return Wildcard.make(BottomSignature.make(), parseFieldTypeSignature());
            }
            case '*': {
                advance();
                return Wildcard.make(
                    BottomSignature.make(),
                    SimpleClassTypeSignature.make("java.lang.Object", false, EMPTY_TYPE_ARGUMENTS)
                );
            }
            case '-': {
                advance();
                return Wildcard.make(
                    parseFieldTypeSignature(),
                    SimpleClassTypeSignature.make("java.lang.Object", false, EMPTY_TYPE_ARGUMENTS)
                );
            }
            default: {
                return parseFieldTypeSignature();
            }
        }
    }

    // TypeVariableSignature -> T identifier

    private TypeVariableSignature parseTypeVariableSignature() {
        assert (current() == 'T');
        if (current() != 'T') {
            throw error("expected a type variable usage");
        }
        advance();
        final TypeVariableSignature ts =
            TypeVariableSignature.make(parseIdentifier());
        if (current() != ';') {
            throw error(
                "; expected in signature of type variable named" +
                ts.getName()
            );
        }
        advance();
        return ts;
    }

    // ArrayTypeSignature -> [ TypeSignature

    private ArrayTypeSignature parseArrayTypeSignature() {
        if (current() != '[') {
            throw error("expected array type signature");
        }
        advance();
        return ArrayTypeSignature.make(parseTypeSignature());
    }

    // TypeSignature -> BaseType | FieldTypeSignature

    private TypeSignature parseTypeSignature() {
        switch (current()) {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'Z':
            case 'V':
                return parseBaseType();
            default:
                return parseFieldTypeSignature();
        }
    }

    private BaseType parseBaseType() {
        switch (current()) {
            case 'B':
                advance();
                return ByteSignature.make();
            case 'C':
                advance();
                return CharSignature.make();
            case 'D':
                advance();
                return DoubleSignature.make();
            case 'F':
                advance();
                return FloatSignature.make();
            case 'I':
                advance();
                return IntSignature.make();
            case 'J':
                advance();
                return LongSignature.make();
            case 'S':
                advance();
                return ShortSignature.make();
            case 'Z':
                advance();
                return BooleanSignature.make();
            case 'V':
                advance();
                return VoidSignature.make();
            default: {
                throw error("expected primitive type");
            }
        }
    }

    private FieldTypeSignature[] parseZeroOrMoreBounds() {
        final List<FieldTypeSignature> fts = new ArrayList<>(3);

        if (current() == ':') {
            advance();
            switch (current()) {
                case ':': // empty class bound
                    fts.add(BottomSignature.make());
                    break;

                default: // parse class bound
                    fts.add(parseFieldTypeSignature());
                    break;
            }

            // zero or more interface bounds
            while (current() == ':') {
                advance();
                fts.add(parseFieldTypeSignature());
            }
        }

        return fts.toArray(new FieldTypeSignature[fts.size()]);
    }

    private ClassTypeSignature[] parseSuperInterfaces() {
        final Collection<ClassTypeSignature> cts =
            new ArrayList<>(5);
        while (current() == 'L') {
            cts.add(parseClassTypeSignature());
        }
        final ClassTypeSignature[] cta = new ClassTypeSignature[cts.size()];
        return cts.toArray(cta);
    }

    // parse a method signature based on the implicit input.
    private MethodTypeSignature parseMethodTypeSignature() {
        assert (index == 0);

        return MethodTypeSignature.make(
            parseZeroOrMoreFormalTypeParameters(),
            parseFormalParameters(),
            parseReturnType(),
            parseZeroOrMoreThrowsSignatures()
        );
    }

    // (TypeSignature*)
    private TypeSignature[] parseFormalParameters() {
        if (current() != '(') {
            throw error("expected (");
        }
        advance();
        final TypeSignature[] pts = parseZeroOrMoreTypeSignatures();
        if (current() != ')') {
            throw error("expected )");
        }
        advance();
        return pts;
    }

    // TypeSignature*
    private TypeSignature[] parseZeroOrMoreTypeSignatures() {
        final Collection<TypeSignature> ts = new ArrayList<>();
        boolean stop = false;
        while (!stop) {
            switch (current()) {
                case 'B':
                case 'C':
                case 'D':
                case 'F':
                case 'I':
                case 'J':
                case 'S':
                case 'Z':
                case 'L':
                case 'T':
                case '[': {
                    ts.add(parseTypeSignature());
                    break;
                }
                default:
                    stop = true;
            }
        }
        /*      while( matches(current(),
                       'B', 'C', 'D', 'F', 'I', 'J', 'S', 'Z', 'L', 'T', '[')
               ) {
            ts.add(parseTypeSignature());
            }*/
        final TypeSignature[] ta = new TypeSignature[ts.size()];
        return ts.toArray(ta);
    }

    // ReturnType -> V | TypeSignature

    private ReturnType parseReturnType() {
        if (current() == 'V') {
            advance();
            return VoidSignature.make();
        }
        else {
            return parseTypeSignature();
        }
    }

    // ThrowSignature*
    private FieldTypeSignature[] parseZeroOrMoreThrowsSignatures() {
        final Collection<FieldTypeSignature> ets =
            new ArrayList<>(3);
        while (current() == '^') {
            ets.add(parseThrowsSignature());
        }
        final FieldTypeSignature[] eta = new FieldTypeSignature[ets.size()];
        return ets.toArray(eta);
    }

    // ThrowSignature -> ^ FieldTypeSignature

    private FieldTypeSignature parseThrowsSignature() {
        assert (current() == '^');
        if (current() != '^') {
            throw error("expected throws signature");
        }
        advance();
        return parseFieldTypeSignature();
    }
}
