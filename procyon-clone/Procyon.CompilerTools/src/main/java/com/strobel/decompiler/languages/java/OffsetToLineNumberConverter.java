package com.strobel.decompiler.languages.java;

/**
 * An instance capable of converting from a raw bytecode offset number to a Java
 * source code line number.
 */
public interface OffsetToLineNumberConverter {
    /**
     * indicates that the line number is unknown
     */
    public static final int UNKNOWN_LINE_NUMBER = -100;
    
    /**
     * a do-nothing offset-to-line-number converter which always returns {@link #UNKNOWN_LINE_NUMBER}
     */
    public static final OffsetToLineNumberConverter NOOP_CONVERTER = new OffsetToLineNumberConverter() {        
        @Override
        public int getLineForOffset(int offset) { return UNKNOWN_LINE_NUMBER; }
    };    

    /**
     * Given a raw bytecode offset number, returns the corresponding Java line number.
     * If there is no exact match for 'offset', returns the previous exact-match line number.
     * 
     * @param offset a raw bytecode offset
     * @return the corresponding Java source code line number
     */
    public int getLineForOffset( int offset);
}
