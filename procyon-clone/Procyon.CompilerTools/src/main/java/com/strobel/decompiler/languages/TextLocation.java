/*
 * TextLocation.java
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

package com.strobel.decompiler.languages;

import java.io.Serializable;

public final class TextLocation implements Comparable<TextLocation>, Serializable {
    private static final long serialVersionUID = -165593440170614692L;

    public final static int MIN_LINE = 1;
    public final static int MIN_COLUMN = 1;

    public final static TextLocation EMPTY = new TextLocation();

    private final int _line;
    private final int _column;

    private TextLocation() {
        _line = 0;
        _column = 0;
    }

    public TextLocation(final int line, final int column) {
        _line = line;//VerifyArgument.inRange(MIN_LINE, Integer.MAX_VALUE, line, "line");
        _column = column;//VerifyArgument.inRange(MIN_COLUMN, Integer.MAX_VALUE, column, "column");
    }

    public final int line() {
        return _line;
    }

    public final int column() {
        return _column;
    }

    public final boolean isEmpty() {
        return _line < MIN_LINE && _column < MIN_COLUMN;
    }

    public final boolean isBefore(final TextLocation other) {
        //noinspection SimplifiableIfStatement
        if (other == null || other.isEmpty()) {
            return false;
        }

        return _line < other._line ||
               _line == other._line && _column < other._column;
    }

    public final boolean isAfter(final TextLocation other) {
        return other == null ||
               other.isEmpty() ||
               _line > other._line ||
               _line == other._line && _column > other._column;
    }

    @Override
    public final String toString() {
        return String.format("(Line %d, Column %d)", _line, _column);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return super.equals(obj);
    }

    @Override
    public final int compareTo(final TextLocation o) {
        if (isBefore(o)) {
            return -1;
        }

        if (isAfter(o)) {
            return 1;
        }

        return 0;
    }
}
