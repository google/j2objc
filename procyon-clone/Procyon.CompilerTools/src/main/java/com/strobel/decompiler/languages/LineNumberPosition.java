package com.strobel.decompiler.languages;

import java.util.List;

/**
 * Indicates the position of a line number in a decompiler-written Java class.
 * For example, an expression which originally came from line number 13 might have
 * actually been emitted at column 65 of line 27 in the decompiler-emitted source
 * file.
 */
public class LineNumberPosition {
    /** the line number from the original source code */
    private final int _originalLine;
    
    /** the line number in the decompiler-emitted source file */
    private final int _emittedLine;
    
    /** the 1-indexed column number in the decompiler-emitted source file */
    private final int _emittedColumn;
    
    /**
     * Creates a new line number position from a decompiler-emitted expression or
     * statement.
     * 
     * @param originalLine the line number of the expression/statement in the original code
     * @param emittedLine the line number of the expression/statement in the decompiler-emitted code
     * @param emittedColumn the 1-indexed column number of the expression/statement in the decompiler-emitted code
     */
    public LineNumberPosition( int originalLine, int emittedLine, int emittedColumn) {
        _originalLine = originalLine;
        _emittedLine = emittedLine;
        _emittedColumn = emittedColumn;
    }

    public int getOriginalLine() {
        return _originalLine;
    }

    public int getEmittedLine() {
        return _emittedLine;
    }

    public int getEmittedColumn() {
        return _emittedColumn;
    }
    
    public static int computeMaxLineNumber( List<LineNumberPosition> lineNumPositions) {
        int maxLineNo = 1;
        for ( LineNumberPosition pos : lineNumPositions) {
            int originalLine = pos.getOriginalLine();
            maxLineNo = Math.max( maxLineNo, originalLine);
        }
        return maxLineNo;
    }
    
    @Override
    public String toString() {
        return "Line # Position : {orig=" + _originalLine + ", " +
                "emitted=" + _emittedLine + "/" + _emittedColumn + "}";
    }
    
}
