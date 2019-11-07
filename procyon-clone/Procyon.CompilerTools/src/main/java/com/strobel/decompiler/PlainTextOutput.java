/*
 * PlainTextOutput.java
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

import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.UndeclaredThrowableException;

public class PlainTextOutput implements ITextOutput {
    private final static String NULL_TEXT = String.valueOf((Object) null);

    private final Writer _writer;
    private String _indentToken = "    ";
    private int _indent;
    private boolean _needsIndent;
    private boolean _isUnicodeOutputEnabled;

    protected int line = 1;
    protected int column = 1;

    public PlainTextOutput() {
        _writer = new StringWriter();
    }

    public PlainTextOutput(final Writer writer) {
        _writer = VerifyArgument.notNull(writer, "writer");
    }

    @Override
    public final String getIndentToken() {
        final String indentToken = _indentToken;
        return indentToken != null ? indentToken : StringUtilities.EMPTY;
    }

    @Override
    public final void setIndentToken(final String indentToken) {
        _indentToken = indentToken;
    }

    public final boolean isUnicodeOutputEnabled() {
        return _isUnicodeOutputEnabled;
    }

    public final void setUnicodeOutputEnabled(final boolean unicodeOutputEnabled) {
        _isUnicodeOutputEnabled = unicodeOutputEnabled;
    }

    protected void writeIndent() {
        if (_needsIndent) {
            _needsIndent = false;

            final String indentToken = getIndentToken();

            for (int i = 0; i < _indent; i++) {
                try {
                    _writer.write(indentToken);
                }
                catch (IOException e) {
                    throw new UndeclaredThrowableException(e);
                }
            }

            column += indentToken.length() * _indent;
        }
    }

    @Override
    public int getRow() {
        return line;
    }

    @Override
    public int getColumn() {
        return _needsIndent ? column + (_indent * getIndentToken().length()) : column;
    }

    @Override
    public void indent() {
        ++_indent;
    }

    @Override
    public void unindent() {
        --_indent;
    }

    @Override
    public void write(final char ch) {
        writeIndent();
        try {
            if (isUnicodeOutputEnabled()) {
                _writer.write(ch);
            }
            else {
                _writer.write(StringUtilities.escape(ch));
            }
            column++;
        }
        catch (IOException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    @Override
    public void write(final String text) {
        writeRaw(isUnicodeOutputEnabled() ? text : StringUtilities.escape(text));
    }

    /**
     * Write the specified text without applying any escaping.
     *
     * @param text
     *     The text to write
     */
    protected void writeRaw(final String text) {
        writeIndent();

        try {
            final int length = text != null ? text.length() : NULL_TEXT.length();

            _writer.write(text);

            column += length;

            if (text == null) {
                return;
            }

            boolean newLineSeen = false;

            for (int i = 0; i < length; i++) {
                if (text.charAt(i) == '\n') {
                    line++;
                    column = 0;
                    newLineSeen = true;
                }
                else if (newLineSeen) {
                    column++;
                }
            }
        }
        catch (IOException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    @Override
    public void writeError(final String value) {
        write(value);
    }

    @Override
    public void writeLabel(final String value) {
        write(value);
    }

    @Override
    public void writeLiteral(final Object value) {
        write(String.valueOf(value));
    }

    @Override
    public void writeTextLiteral(final Object value) {
        write(String.valueOf(value));
    }

    @Override
    public void writeComment(final String value) {
        write(value);
    }

    @Override
    public void writeComment(final String format, final Object... args) {
        write(format, args);
    }

    @Override
    public void write(final String format, final Object... args) {
        write(String.format(format, args));
    }

    @Override
    public void writeLine(final String text) {
        write(text);
        writeLine();
    }

    @Override
    public void writeLine(final String format, final Object... args) {
        write(String.format(format, args));
        writeLine();
    }

    @Override
    public void writeLine() {
        writeIndent();
        try {
            _writer.write("\n");
        }
        catch (IOException e) {
            throw new UndeclaredThrowableException(e);
        }
        _needsIndent = true;
        ++line;
        column = 1;
    }

    @Override
    public void writeDelimiter(final String text) {
        write(text);
    }

    @Override
    public void writeOperator(final String text) {
        write(text);
    }

    @Override
    public void writeKeyword(final String text) {
        write(text);
    }

    @Override
    public void writeAttribute(final String text) {
        write(text);
    }

    @Override
    public void writeDefinition(final String text, final Object definition) {
        writeDefinition(text, definition, true);
    }

    @Override
    public void writeDefinition(final String text, final Object definition, final boolean isLocal) {
        write(text);
    }

    @Override
    public void writeReference(final String text, final Object reference) {
        writeReference(text, reference, false);
    }

    @Override
    public void writeReference(final String text, final Object reference, final boolean isLocal) {
        write(text);
    }

    @Override
    public boolean isFoldingSupported() {
        return false;
    }

    @Override
    public void markFoldStart(final String collapsedText, final boolean defaultCollapsed) {
    }

    @Override
    public void markFoldEnd() {
    }

    @Override
    public String toString() {
        return _writer.toString();
    }
}
