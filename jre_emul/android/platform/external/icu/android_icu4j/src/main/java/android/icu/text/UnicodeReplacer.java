/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
**********************************************************************
*   Copyright (c) 2002, International Business Machines Corporation
*   and others.  All Rights Reserved.
**********************************************************************
*   Date        Name        Description
*   01/14/2002  aliu        Creation.
**********************************************************************
*/

package android.icu.text;

/**
 * <code>UnicodeReplacer</code> defines a protocol for objects that
 * replace a range of characters in a Replaceable string with output
 * text.  The replacement is done via the Replaceable API so as to
 * preserve out-of-band data.
 * @author Alan Liu
 */
interface UnicodeReplacer {

    /**
     * Replace characters in 'text' from 'start' to 'limit' with the
     * output text of this object.  Update the 'cursor' parameter to
     * give the cursor position and return the length of the
     * replacement text.
     *
     * @param text the text to be matched
     * @param start inclusive start index of text to be replaced
     * @param limit exclusive end index of text to be replaced;
     * must be greater than or equal to start
     * @param cursor output parameter for the cursor position.
     * Not all replacer objects will update this, but in a complete
     * tree of replacer objects, representing the entire output side
     * of a transliteration rule, at least one must update it.
     * @return the number of 16-bit code units in the text replacing
     * the characters at offsets start..(limit-1) in text
     */
    public abstract int replace(Replaceable text,
                                int start,
                                int limit,
                                int[] cursor);

    /**
     * Returns a string representation of this replacer.  If the
     * result of calling this function is passed to the appropriate
     * parser, typically TransliteratorParser, it will produce another
     * replacer that is equal to this one.
     * @param escapeUnprintable if TRUE then convert unprintable
     * character to their hex escape representations, \\uxxxx or
     * \\Uxxxxxxxx.  Unprintable characters are defined by
     * Utility.isUnprintable().
     */
    public abstract String toReplacerPattern(boolean escapeUnprintable);

    /**
     * Union the set of all characters that may output by this object
     * into the given set.
     * @param toUnionTo the set into which to union the output characters
     */
    public abstract void addReplacementSetTo(UnicodeSet toUnionTo);
}

//eof
