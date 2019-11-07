/*
 * Region.java
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

import com.strobel.core.StringUtilities;

import java.io.Serializable;

public final class Region implements Serializable {
    private static final long serialVersionUID = -7580225960304530502L;

    public final static Region EMPTY = new Region(0, 0, 0, 0);

    private final String _fileName;
    private final int _beginLine;
    private final int _endLine;
    private final int _beginColumn;
    private final int _endColumn;

    public Region(final TextLocation begin, final TextLocation end) {
        this(null, begin, end);
    }

    public Region(final String fileName, final TextLocation begin, final TextLocation end) {
        this(
            fileName,
            begin != null ? begin.line() : TextLocation.EMPTY.line(),
            end != null ? end.line() : TextLocation.EMPTY.line(),
            begin != null ? begin.column() : TextLocation.EMPTY.column(),
            end != null ? end.column() : TextLocation.EMPTY.column()
        );
    }

    public Region(final int beginLine, final int endLine, final int beginColumn, final int endColumn) {
        this(null, beginLine, endLine, beginColumn, endColumn);
    }

    public Region(final String fileName, final int beginLine, final int endLine, final int beginColumn, final int endColumn) {
        _fileName = fileName;
        _beginLine = beginLine;
        _endLine = endLine;
        _beginColumn = beginColumn;
        _endColumn = endColumn;
    }

    public final String getFileName() {
        return _fileName;
    }

    public final int getBeginLine() {
        return _beginLine;
    }

    /**
     * Note that {@code -1} indicates an unknown end.
     */
    public final int getEndLine() {
        return _endLine;
    }

    public final int getBeginColumn() {
        return _beginColumn;
    }

    /**
     * Note that {@code -1} indicates an unknown end.
     */
    public final int getEndColumn() {
        return _endColumn;
    }

    public final boolean isEmpty() {
        return _beginColumn <= 0;
    }

    public final boolean isInside(final int line, final int column) {
        if (isEmpty()) {
            return false;
        }

        return line >= _beginLine &&
               (line <= _endLine || _endLine == -1) &&
               (line != _beginLine || column >= _beginColumn) &&
               (line != _endLine || column <= _endColumn);
    }

    public final boolean IsInside(final TextLocation location) {
        return isInside(
            location != null ? location.line() : TextLocation.EMPTY.line(),
            location != null ? location.column() : TextLocation.EMPTY.column()
        );
    }

    @Override
    public final int hashCode() {
        return (_fileName != null ? _fileName.hashCode() : 0) ^
               (_beginColumn + 1100009 * _beginLine + 1200007 * _endLine + 1300021 * _endColumn);
    }

    @Override
    public final boolean equals(final Object obj) {
        if (obj instanceof Region) {
            final Region other = (Region) obj;

            return other._beginLine == _beginLine &&
                   other._beginColumn == _beginColumn &&
                   other._endLine == _endLine &&
                   other._endColumn == _endColumn &&
                   StringUtilities.equals(other._fileName, _fileName);
        }

        return false;
    }

    public final String toString() {
        return String.format(
            "[Region FileName=%s, Begin=(%d, %d), End=(%d, %d)]",
            _fileName,
            _beginLine,
            _beginColumn,
            _endLine,
            _endColumn
        );
    }
}
