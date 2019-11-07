/*
 * ITextOutput.java
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

/**
 * @author mstrobel
 */
public interface ITextOutput {
    int getRow();
    int getColumn();

    void indent();
    void unindent();

    void write(final char ch);
    void write(final String text);

    void writeError(final String value);

    void writeLabel(final String value);
    void writeLiteral(final Object value);
    void writeTextLiteral(final Object value);

    void writeComment(final String value);
    void writeComment(final String format, final Object... args);

    void write(final String format, final Object... args);
    void writeLine(final String text);
    void writeLine(final String format, final Object... args);
    void writeLine();

    void writeDelimiter(final String text);
    void writeOperator(final String text);
    void writeKeyword(final String text);

    void writeAttribute(String text);

    void writeDefinition(final String text, final Object definition);
    void writeDefinition(final String text, final Object definition, final boolean isLocal);

    void writeReference(final String text, final Object reference);
    void writeReference(final String text, final Object reference, final boolean isLocal);

    boolean isFoldingSupported();

    void markFoldStart(final String collapsedText, final boolean defaultCollapsed);
    void markFoldEnd();
    String getIndentToken();
    void setIndentToken(String indentToken);
}
