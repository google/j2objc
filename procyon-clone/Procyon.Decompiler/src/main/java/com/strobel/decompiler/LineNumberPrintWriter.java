package com.strobel.decompiler;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;

/**
 * A specialization of {@link PrintWriter} which can automatically prefix lines with a
 * Java-commented, justified line number.  This class overrides only the
 * {@link #print(String)} and {@link #println(String)} methods, therefore all other
 * methods must be avoided.
 */
class LineNumberPrintWriter extends PrintWriter {
    public static final int NO_LINE_NUMBER = -1;
    private final String _emptyPrefix;
    private final String _format;
    private boolean _needsPrefix;
    private boolean _suppressLineNumbers;

    /**
     * Creates an instance.  The only valid "print" methods to call are
     * {@link #print(String)} and {@link #println(String)}.
     * 
     * @param maxLineNo the highest line number that 'this' will ever encounter
     * @param w the underlying {@link Writer} to which characters are printed.
     */
    public LineNumberPrintWriter( int maxLineNo, Writer w) {
        super( w);
        String maxNumberString = String.format( "%d", maxLineNo);
        int numberWidth = maxNumberString.length();
        _format = "/*%" + numberWidth + "d*/";
        String samplePrefix = String.format( _format, maxLineNo);
        char[] prefixChars = samplePrefix.toCharArray();
        Arrays.fill( prefixChars, ' ');
        _emptyPrefix = new String( prefixChars);
        _needsPrefix = true;
    }
    
    /**
     * Causes 'this' printer to not emit any line numbers or any whitespace padding.
     */
    public void suppressLineNumbers()
    {
        _suppressLineNumbers = true;
    }

    @Override
    public void print(String s) {
        this.print( NO_LINE_NUMBER, s);
    }

    @Override
    public void println(String s) {
        this.println( NO_LINE_NUMBER, s);
    }

    public void println( int lineNumber, String s) {
        this.doPrefix( lineNumber);
        super.println( s);
        _needsPrefix = true;
    }

    public void print( int lineNumber, String s) {
        this.doPrefix( lineNumber);
        super.print( s);
    }

    private void doPrefix( int lineNumber) {
        if ( _needsPrefix && ! _suppressLineNumbers) {
            if ( lineNumber == NO_LINE_NUMBER) {
                super.print( _emptyPrefix);
            } else {
                String prefix = String.format( _format, lineNumber);
                super.print( prefix);
            }
        }
        _needsPrefix = false;
    }
}