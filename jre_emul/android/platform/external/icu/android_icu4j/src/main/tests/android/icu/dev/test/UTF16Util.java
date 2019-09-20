/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
*******************************************************************************
* Copyright (C) 2002-2004, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*/
package android.icu.dev.test;

/**
 * Utility class for supplementary code point 
 * support. This one is written purely for updating
 * Normalization sample from the unicode.org site.
 * If you want the real thing, use UTF16 class
 * from ICU4J
 * @author Vladimir Weinstein, Markus Scherer
 */
public class UTF16Util {
    static final int suppOffset = (0xd800 << 10) + 0xdc00 - 0x10000;

    /**
     * Method nextCodePoint. Returns the next code point
     * in a string. 
     * @param s String in question
     * @param i index from which we want a code point
     * @return int codepoint at index i
     */
    public static final int nextCodePoint(String s, int i) {
        int ch = s.charAt(i);
        if (0xd800 <= ch && ch <= 0xdbff && ++i < s.length()) {
            int ch2 = s.charAt(i);
            if (0xdc00 <= ch2 && ch2 <= 0xdfff) {
                ch = (ch << 10) + ch2 - suppOffset;
            }
        }
        return ch;
    }

    /**
     * Method prevCodePoint. Gets the code point preceding
     * index i (predecrement). 
     * @param s String in question
     * @param i index in string
     * @return int codepoint at index --i
     */
    public static final int prevCodePoint(String s, int i) {
        int ch = s.charAt(--i);
        if (0xdc00 <= ch && ch <= 0xdfff && --i >= 0) {
            int ch2 = s.charAt(i);
            if (0xd800 <= ch2 && ch2 <= 0xdbff) {
                ch = (ch2 << 10) + ch - suppOffset;
            }
        }
        return ch;
    }

    /**
     * Method nextCodePoint. Returns the next code point
     * in a string. 
     * @param s StringBuffer in question
     * @param i index from which we want a code point
     * @return int codepoint at index i
     */
    public static final int nextCodePoint(StringBuffer s, int i) {
        int ch = s.charAt(i);
        if (0xd800 <= ch && ch <= 0xdbff && ++i < s.length()) {
            int ch2 = s.charAt(i);
            if (0xdc00 <= ch2 && ch2 <= 0xdfff) {
                ch = (ch << 10) + ch2 - suppOffset;
            }
        }
        return ch;
    }

    /**
     * Method prevCodePoint. Gets the code point preceding
     * index i (predecrement). 
     * @param s StringBuffer in question
     * @param i index in string
     * @return int codepoint at index --i
     */
    public static final int prevCodePoint(StringBuffer s, int i) {
        int ch = s.charAt(--i);
        if (0xdc00 <= ch && ch <= 0xdfff && --i >= 0) {
            int ch2 = s.charAt(i);
            if (0xd800 <= ch2 && ch2 <= 0xdbff) {
                ch = (ch2 << 10) + ch - suppOffset;
            }
        }
        return ch;
    }

    /**
     * Method codePointLength. Returns the length 
     * in UTF-16 code units of a given code point
     * @param c code point in question
     * @return int length in UTF-16 code units. Can be 1 or 2
     */
    public static final int codePointLength(int c) {
        return c <= 0xffff ? 1 : 2;
    }

    /**
     * Method appendCodePoint. Appends a code point
     * to a StringBuffer
     * @param buffer StringBuffer in question
     * @param ch code point to append
     */
    public static final void appendCodePoint(StringBuffer buffer, int ch) {
        if (ch <= 0xffff) {
            buffer.append((char)ch);
        } else {
            buffer.append((char)(0xd7c0 + (ch >> 10)));
            buffer.append((char)(0xdc00 + (ch & 0x3ff))); 
        }
    }

    /**
     * Method insertCodePoint. Inserts a code point in
     * a StringBuffer
     * @param buffer StringBuffer in question
     * @param i index at which we want code point to be inserted
     * @param ch code point to be inserted
     */
    public static final void insertCodePoint(StringBuffer buffer, int i, int ch) {
        if (ch <= 0xffff) {
            buffer.insert(i, (char)ch);
        } else {
            buffer.insert(i, (char)(0xd7c0 + (ch >> 10))).insert(i + 1, (char)(0xdc00 + (ch & 0x3ff))); 
        }
    }
    
    /**
     * Method setCodePointAt. Changes a code point at a
     * given index. Can change the length of the string.
     * @param buffer StringBuffer in question
     * @param i index at which we want to change the contents
     * @param ch replacement code point
     * @return int difference in resulting StringBuffer length
     */
    public static final int setCodePointAt(StringBuffer buffer, int i, int ch) {
        int cp = nextCodePoint(buffer, i);
        
        if (ch <= 0xffff && cp <= 0xffff) { // Both BMP
            buffer.setCharAt(i, (char)ch);
            return 0;
        } else if (ch > 0xffff && cp > 0xffff) { // Both supplementary
            buffer.setCharAt(i, (char)(0xd7c0 + (ch >> 10)));
            buffer.setCharAt(i+1, (char)(0xdc00 + (ch & 0x3ff)));
            return 0;
        } else if (ch <= 0xffff && cp > 0xffff) { // putting BMP instead of supplementary, buffer shrinks
            buffer.setCharAt(i, (char)ch);
            buffer.deleteCharAt(i+1);
            return -1;
        } else { //if (ch > 0xffff && cp <= 0xffff) { // putting supplementary instead of BMP, buffer grows
            buffer.setCharAt(i, (char)(0xd7c0 + (ch >> 10)));
            buffer.insert(i+1, (char)(0xdc00 + (ch & 0x3ff))); 
            return 1;           
        }
    }

    /**
     * Method countCodePoint. Counts the UTF-32 code points
     * in a UTF-16 encoded string.
     * @param source String in question.
     * @return int number of code points in this string
     */
    public static final int countCodePoint(String source) 
    {         
        int result = 0;
        char ch;
        boolean hadLeadSurrogate = false;
        
        for (int i = 0; i < source.length(); ++ i) 
        {
            ch = source.charAt(i);
            if (hadLeadSurrogate && 0xdc00 <= ch && ch <= 0xdfff) {
                hadLeadSurrogate = false;           // count valid trail as zero
            }
            else
            {
                hadLeadSurrogate = (0xd800 <= ch && ch <= 0xdbff);
                ++ result;                          // count others as 1
            }
        }
        
        return result;
    }
    
    /**
     * Method countCodePoint. Counts the UTF-32 code points
     * in a UTF-16 encoded string.
     * @param source StringBuffer in question.
     * @return int number of code points in this string
     */
    public static final int countCodePoint(StringBuffer source) 
    {         
        int result = 0;
        char ch;
        boolean hadLeadSurrogate = false;
        
        for (int i = 0; i < source.length(); ++ i) 
        {
            ch = source.charAt(i);
            if (hadLeadSurrogate && 0xdc00 <= ch && ch <= 0xdfff) {
                hadLeadSurrogate = false;           // count valid trail as zero
            }
            else
            {
                hadLeadSurrogate = (0xd800 <= ch && ch <= 0xdbff);
                ++ result;                          // count others as 1
            }
        }
        
        return result;
    }
    /**
     * The minimum value for Supplementary code points
     */
    public static final int SUPPLEMENTARY_MIN_VALUE  = 0x10000;  
    /**
     * Determines how many chars this char32 requires.
     * If a validity check is required, use <code>
     * <a href="../UCharacter.html#isLegal(char)">isLegal()</a></code> on 
     * char32 before calling.
     * @param char32 the input codepoint.
     * @return 2 if is in supplementary space, otherwise 1. 
     */
    public static int getCharCount(int char32) 
    {
        if (char32 < SUPPLEMENTARY_MIN_VALUE) {
            return 1;
        }
        return 2;
    }
    /**
     * Lead surrogate maximum value
     * @stable ICU 2.1
     */
    public static final int LEAD_SURROGATE_MAX_VALUE = 0xDBFF;
    /**
     * Lead surrogate minimum value
     * @stable ICU 2.1
     */
    public static final int LEAD_SURROGATE_MIN_VALUE = 0xD800;
    
    /**
     * Trail surrogate minimum value
     * @stable ICU 2.1
     */
    public static final int TRAIL_SURROGATE_MIN_VALUE = 0xDC00; 
    /**
     * Trail surrogate maximum value
     * @stable ICU 2.1
     */
    public static final int TRAIL_SURROGATE_MAX_VALUE = 0xDFFF;
    /**
     * Determines whether the code value is a surrogate.
     * @param char16 the input character.
     * @return true iff the input character is a surrogate.
     * @stable ICU 2.1
     */
    public static boolean isSurrogate(char char16) 
    {
        return LEAD_SURROGATE_MIN_VALUE <= char16 && 
            char16 <= TRAIL_SURROGATE_MAX_VALUE;
    }
        
    /**
     * Determines whether the character is a trail surrogate.
     * @param char16 the input character.
     * @return true iff the input character is a trail surrogate.
     * @stable ICU 2.1
     */
    public static boolean isTrailSurrogate(char char16) 
    {
        return (TRAIL_SURROGATE_MIN_VALUE <= char16 && 
                char16 <= TRAIL_SURROGATE_MAX_VALUE);
    }
        
    /**
     * Determines whether the character is a lead surrogate.
     * @param char16 the input character.
     * @return true iff the input character is a lead surrogate
     * @stable ICU 2.1
     */
    public static boolean isLeadSurrogate(char char16) 
    {
        return LEAD_SURROGATE_MIN_VALUE <= char16 && 
            char16 <= LEAD_SURROGATE_MAX_VALUE;
    }
    /**
     * Extract a single UTF-32 value from a substring.
     * Used when iterating forwards or backwards (with
     * <code>UTF16.getCharCount()</code>, as well as random access. If a
     * validity check is required, use 
     * <code><a href="../UCharacter.html#isLegal(char)">UCharacter.isLegal()
     * </a></code> on the return value.
     * If the char retrieved is part of a surrogate pair, its supplementary
     * character will be returned. If a complete supplementary character is 
     * not found the incomplete character will be returned
     * @param source array of UTF-16 chars
     * @param start offset to substring in the source array for analyzing
     * @param limit offset to substring in the source array for analyzing
     * @param offset16 UTF-16 offset relative to start
     * @return UTF-32 value for the UTF-32 value that contains the char at
     *         offset16. The boundaries of that codepoint are the same as in
     *         <code>bounds32()</code>.
     * @exception IndexOutOfBoundsException thrown if offset16 is not within 
     *            the range of start and limit.
     * @stable ICU 2.1
     */
    public static int charAt(char source[], int start, int limit, 
                             int offset16)
    {
        offset16 += start;
        if (offset16 < start || offset16 >= limit) {
            throw new ArrayIndexOutOfBoundsException(offset16);
        }
            
        char single = source[offset16];
        if (!isSurrogate(single)) {
            return single;
        }

        // Convert the UTF-16 surrogate pair if necessary.
        // For simplicity in usage, and because the frequency of pairs is 
        // low, look both directions.      
        if (single <= LEAD_SURROGATE_MAX_VALUE) {
            offset16 ++;
            if (offset16 >= limit) {
                return single;
            }
            char trail = source[offset16];
            if (isTrailSurrogate(trail)) {
                return getRawSupplementary(single, trail);
            }
        } 
        else { // isTrailSurrogate(single), so
            if (offset16 == start) {
                return single;
            }
            offset16 --;
            char lead = source[offset16];
            if (isLeadSurrogate(lead))
                return getRawSupplementary(lead, single);
        }
        return single; // return unmatched surrogate
    }
    /**
     * Shift value for lead surrogate to form a supplementary character.
     */
    private static final int LEAD_SURROGATE_SHIFT_ = 10;
    
    /** 
     * Offset to add to combined surrogate pair to avoid msking.
     */
    private static final int SURROGATE_OFFSET_ = 
                           SUPPLEMENTARY_MIN_VALUE - 
                           (LEAD_SURROGATE_MIN_VALUE << 
                           LEAD_SURROGATE_SHIFT_) - 
                           TRAIL_SURROGATE_MIN_VALUE;   

    
   /**
    * Forms a supplementary code point from the argument character<br>
    * Note this is for internal use hence no checks for the validity of the
    * surrogate characters are done
    * @param lead lead surrogate character
    * @param trail trailing surrogate character
    * @return code point of the supplementary character
    */
    public static int getRawSupplementary(char lead, char trail)
    {
        return (lead << LEAD_SURROGATE_SHIFT_) + trail + SURROGATE_OFFSET_;
    }
    
}
