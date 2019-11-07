/*
 * AnsiTextOutput.java
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

package com.strobel.decompiler;

import com.strobel.assembler.ir.Instruction;
import com.strobel.assembler.ir.OpCode;
import com.strobel.assembler.metadata.*;
import com.strobel.core.StringUtilities;
import com.strobel.decompiler.ast.AstCode;
import com.strobel.decompiler.ast.Variable;
import com.strobel.io.Ansi;

import java.io.StringWriter;
import java.io.Writer;

public class AnsiTextOutput extends PlainTextOutput {
    private final static class Delimiters {
        final static String L = "L";
        final static String T = "T";
        final static String DOLLAR = "$";
        final static String DOT = ".";
        final static String SLASH = "/";
        final static String LEFT_BRACKET = "[";
        final static String SEMICOLON = ";";

        static String get(final char c) {
            switch (c) {
                case 'L': return L;
                case 'T': return T;
                case '$': return DOLLAR;
                case '.': return DOT;
                case '/': return SLASH;
                case '[': return LEFT_BRACKET;
                case ':': return SEMICOLON;
            }
            return String.valueOf(c);
        }
    }

    private final Ansi _keyword;
    private final Ansi _instruction;
    private final Ansi _label;
    private final Ansi _type;
    private final Ansi _typeVariable;
    private final Ansi _package;
    private final Ansi _method;
    private final Ansi _field;
    private final Ansi _local;
    private final Ansi _literal;
    private final Ansi _textLiteral;
    private final Ansi _comment;
    private final Ansi _operator;
    private final Ansi _delimiter;
    private final Ansi _attribute;
    private final Ansi _error;

    public AnsiTextOutput() {
        this(new StringWriter(), ColorScheme.DARK);
    }

    public AnsiTextOutput(final ColorScheme colorScheme) {
        this(new StringWriter(), colorScheme);
    }

    public AnsiTextOutput(final Writer writer) {
        this(writer, ColorScheme.DARK);
    }

    public AnsiTextOutput(final Writer writer, final ColorScheme colorScheme) {
        super(writer);

        final boolean light = colorScheme == ColorScheme.LIGHT;

        _keyword = new Ansi(Ansi.Attribute.NORMAL, new Ansi.AnsiColor(light ? 21 : 33), null);
        _instruction = new Ansi(Ansi.Attribute.NORMAL, new Ansi.AnsiColor(light ? 91 : 141), null);
        _label = new Ansi(Ansi.Attribute.NORMAL, new Ansi.AnsiColor(light ? 249 : 249), null);
        _type = new Ansi(Ansi.Attribute.NORMAL, new Ansi.AnsiColor(light ? 25 : 45), null);
        _typeVariable = new Ansi(Ansi.Attribute.NORMAL, new Ansi.AnsiColor(light ? 29 : 79), null);
        _package = new Ansi(Ansi.Attribute.NORMAL, new Ansi.AnsiColor(light ? 32 : 111), null);
        _method = new Ansi(Ansi.Attribute.NORMAL, new Ansi.AnsiColor(light ? 162 : 212), null);
        _field = new Ansi(Ansi.Attribute.NORMAL, new Ansi.AnsiColor(light ? 136 : 222), null);
        _local = new Ansi(Ansi.Attribute.NORMAL, (Ansi.AnsiColor) null, null);
        _literal = new Ansi(Ansi.Attribute.NORMAL, new Ansi.AnsiColor(light ? 197 : 204), null);
        _textLiteral = new Ansi(Ansi.Attribute.NORMAL, new Ansi.AnsiColor(light ? 28 : 42), null);
        _comment = new Ansi(Ansi.Attribute.NORMAL, new Ansi.AnsiColor(light ? 244 : 244), null);
        _operator = new Ansi(Ansi.Attribute.NORMAL, new Ansi.AnsiColor(light ? 242 : 247), null);
        _delimiter = new Ansi(Ansi.Attribute.NORMAL, new Ansi.AnsiColor(light ? 242 : 252), null);
        _attribute = new Ansi(Ansi.Attribute.NORMAL, new Ansi.AnsiColor(light ? 166 : 214), null);
        _error = new Ansi(Ansi.Attribute.NORMAL, new Ansi.AnsiColor(light ? 196 : 196), null);
    }

    private String colorize(final String value, final Ansi ansi) {
        return ansi.colorize(StringUtilities.escape(value, false, isUnicodeOutputEnabled()));
    }

    @Override
    public void writeError(final String value) {
        writeAnsi(value, colorize(value, _error));
    }

    @Override
    public void writeLabel(final String value) {
        writeAnsi(value, colorize(value, _label));
    }

    protected final void writeAnsi(final String originalText, final String ansiText) {
        super.writeRaw(ansiText);

        if (originalText != null && ansiText != null) {
            super.column -= (ansiText.length() - originalText.length());
        }
    }

    @Override
    public void writeLiteral(final Object value) {
        final String literal = String.valueOf(value);
        writeAnsi(literal, colorize(literal, _literal));
    }

    @Override
    public void writeTextLiteral(final Object value) {
        final String literal = String.valueOf(value);
        writeAnsi(literal, colorize(literal, _textLiteral));
    }

    @Override
    public void writeComment(final String value) {
        writeAnsi(value, colorize(value, _comment));
    }

    @Override
    public void writeComment(final String format, final Object... args) {
        final String text = String.format(format, args);
        writeAnsi(text, colorize(text, _comment));
    }

    @Override
    public void writeDelimiter(final String text) {
        writeAnsi(text, colorize(text, _delimiter));
    }

    @Override
    public void writeAttribute(final String text) {
        writeAnsi(text, colorize(text, _attribute));
    }

    @Override
    public void writeOperator(final String text) {
        writeAnsi(text, colorize(text, _operator));
    }

    @Override
    public void writeKeyword(final String text) {
        writeAnsi(text, colorize(text, _keyword));
    }

    @Override
    public void writeDefinition(final String text, final Object definition, final boolean isLocal) {
        if (text == null) {
            super.write(null);
            return;
        }

        final String colorizedText;

        if (definition instanceof Instruction ||
            definition instanceof OpCode ||
            definition instanceof AstCode) {

            colorizedText = colorize(text, _instruction);
        }
        else if (definition instanceof TypeReference) {
            colorizedText = colorizeType(text, (TypeReference) definition);
        }
        else if (definition instanceof MethodReference ||
                 definition instanceof IMethodSignature) {

            colorizedText = colorize(text, _method);
        }
        else if (definition instanceof FieldReference) {
            colorizedText = colorize(text, _field);
        }
        else if (definition instanceof VariableReference ||
                 definition instanceof ParameterReference ||
                 definition instanceof Variable) {

            colorizedText = colorize(text, _local);
        }
        else if (definition instanceof PackageReference) {
            colorizedText = colorizePackage(text);
        }
        else if (definition instanceof Label ||
                 definition instanceof com.strobel.decompiler.ast.Label) {

            colorizedText = colorize(text, _label);
        }
        else {
            colorizedText = text;
        }

        writeAnsi(text, colorizedText);
    }

    @Override
    public void writeReference(final String text, final Object reference, final boolean isLocal) {
        if (text == null) {
            super.write(null);
            return;
        }

        final String colorizedText;

        if (reference instanceof Instruction ||
            reference instanceof OpCode ||
            reference instanceof AstCode ||
            reference instanceof MethodHandleType) {

            colorizedText = colorize(text, _instruction);
        }
        else if (reference instanceof TypeReference) {
            colorizedText = colorizeType(text, (TypeReference) reference);
        }
        else if (reference instanceof MethodReference ||
                 reference instanceof IMethodSignature) {

            colorizedText = colorize(text, _method);
        }
        else if (reference instanceof FieldReference) {
            colorizedText = colorize(text, _field);
        }
        else if (reference instanceof VariableReference ||
                 reference instanceof ParameterReference ||
                 reference instanceof Variable) {

            colorizedText = colorize(text, _local);
        }
        else if (reference instanceof PackageReference) {
            colorizedText = colorizePackage(text);
        }
        else if (reference instanceof Label ||
                 reference instanceof com.strobel.decompiler.ast.Label) {

            colorizedText = colorize(text, _label);
        }
        else {
            colorizedText = StringUtilities.escape(text, false, isUnicodeOutputEnabled());
        }

        writeAnsi(text, colorizedText);
    }

    @SuppressWarnings("ConstantConditions")
    private String colorizeType(final String text, final TypeReference type) {
        return colorizeTypeCore(new StringBuilder(), text, type).toString();
    }

    private StringBuilder colorizeTypeCore(final StringBuilder sb, final String text, final TypeReference type) {
        if (type.isPrimitive() && text.length() > 1) {
            return sb.append(colorize(text, _keyword));
        }

        int arrayDepth = 0;
        TypeReference elementType = type;

        while (arrayDepth < text.length() && text.charAt(arrayDepth) == '[') {
            arrayDepth++;

            if (elementType.isArray()) {
                elementType = elementType.getElementType();
            }
        }

        if (arrayDepth > 0) {
            colorizeTypeCore(
                sb.append(colorize(StringUtilities.repeat('[', arrayDepth), _delimiter)),
                text.substring(arrayDepth),
                elementType
            );
        }

        final String packageName = type.getPackageName();
        final TypeDefinition resolvedType = type.resolve();

        String s = text;

        final boolean isTypeVariable = s.startsWith("T") && s.endsWith(";");
        final boolean isSignature = isTypeVariable || s.startsWith("L") && s.endsWith(";");

        Ansi typeColor = isTypeVariable || type.isGenericParameter() ? _typeVariable : _type;

        if (StringUtilities.isNullOrEmpty(packageName)) {
            if (resolvedType != null && resolvedType.isAnnotation()) {
                return sb.append(colorize(s, _attribute));
            }
            else {
                return sb.append(colorize(s, typeColor));
            }
        }

        char delimiter = '.';
        String packagePrefix = packageName + delimiter;

        if (isSignature) {
            s = s.substring(1, s.length() - 1);
        }

        if (!StringUtilities.startsWith(s, packagePrefix)) {
            delimiter = '/';
            packagePrefix = packageName.replace('.', delimiter) + delimiter;
        }

        final String typeName;

        if (isSignature) {
            sb.append(colorize(isTypeVariable ? Delimiters.T : Delimiters.L, _delimiter));
        }

        if (StringUtilities.startsWith(s, packagePrefix)) {
            final String[] packageParts = packageName.split("\\.");

            for (int i = 0; i < packageParts.length; i++) {
                if (i != 0) {
                    sb.append(colorize(String.valueOf(delimiter), _delimiter));
                }

                sb.append(colorize(packageParts[i], _package));
            }

            sb.append(colorize(String.valueOf(delimiter), _delimiter));

            typeName = s.substring(packagePrefix.length());
        }
        else {
            typeName = s;
        }

        typeColor = resolvedType != null && resolvedType.isAnnotation() ? _attribute : typeColor;

        colorizeDelimitedName(sb, typeName, typeColor);

        if (isSignature) {
            sb.append(colorize(Delimiters.SEMICOLON, _delimiter));
        }

        return sb;
    }

    private StringBuilder colorizeDelimitedName(final StringBuilder sb, final String typeName, final Ansi typeColor) {
        final int end = typeName.length();

        if (end == 0) {
            return sb;
        }

        int start = 0;
        int i = start;

        while (i < end) {
            final char ch = typeName.charAt(i);

            switch (ch) {
                case '[':
                case '.':
                case '$':
                    sb.append(colorize(typeName.substring(start, i), typeColor));
                    sb.append(colorize(Delimiters.get(ch), _delimiter));
                    start = i + 1;
                    break;
            }

            ++i;
        }

        if (start < end) {
            sb.append(colorize(typeName.substring(start, end), typeColor));
        }

        return sb;
    }

    private String colorizePackage(final String text) {
        final String[] packageParts = text.split("\\.");
        final StringBuilder sb = new StringBuilder(text.length() * 2);

        for (int i = 0; i < packageParts.length; i++) {
            if (i != 0) {
                sb.append(colorize(".", _delimiter));
            }

            final String packagePart = packageParts[i];

            if ("*".equals(packagePart)) {
                sb.append(packagePart);
            }
            else {
                sb.append(colorize(packagePart, _package));
            }
        }

        return sb.toString();
    }

    public enum ColorScheme {
        DARK,
        LIGHT
    }
}
