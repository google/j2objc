package com.strobel.decompiler.languages.java;

import com.strobel.assembler.ir.attributes.LineNumberTableAttribute;
import com.strobel.assembler.ir.attributes.LineNumberTableEntry;
import com.strobel.core.VerifyArgument;

import java.util.Arrays;

/**
 * An implementation of {@link OffsetToLineNumberConverter} which works on top of a
 * {@link LineNumberTableAttribute} data structure created from a compiled Java method.
 */
public class LineNumberTableConverter implements OffsetToLineNumberConverter {
    private final int[] _offset2LineNo;
    private final int _maxOffset;

    public LineNumberTableConverter(final LineNumberTableAttribute lineNumberTable) {
        VerifyArgument.notNull(lineNumberTable, "lineNumberTable");

        _maxOffset = lineNumberTable.getMaxOffset();
        _offset2LineNo = new int[_maxOffset + 1];

        Arrays.fill(_offset2LineNo, OffsetToLineNumberConverter.UNKNOWN_LINE_NUMBER);

        for (final LineNumberTableEntry entry : lineNumberTable.getEntries()) {
            _offset2LineNo[entry.getOffset()] = entry.getLineNumber();
        }

        //
        // "Fill in the blanks".  Not all offsets from the compiler have line numbers,
        // so we will assume that offset N has the same line number as (N-1) or (N-2),
        // etc., looking backward for the most recent line number.
        //

        int lastLine = _offset2LineNo[0];

        for (int i = 1; i < _maxOffset + 1; i++) {
            final int thisLine = _offset2LineNo[i];

            if (thisLine == OffsetToLineNumberConverter.UNKNOWN_LINE_NUMBER) {
                _offset2LineNo[i] = lastLine;
            }
            else {
                lastLine = thisLine;
            }
        }
    }

    @Override
    public int getLineForOffset(int offset) {
        VerifyArgument.isNonNegative(offset, "offset");
        assert offset >= 0 : "offset must be >= 0; received an offset of " + offset;

        //
        // Sadly, the line number table stops before the end of the offsets, and all higher
        // offsets are assumed to map to the last line number in the data structure-- so we've
        // got to be lax here:
        //

        if (offset > _maxOffset) {
            offset = _maxOffset;
        }

        return _offset2LineNo[offset];
    }
}
