/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
**********************************************************************
*   Copyright (c) 2002-2007, International Business Machines Corporation
*   and others.  All Rights Reserved.
**********************************************************************
*   Date        Name        Description
*   01/14/2002  aliu        Creation.
**********************************************************************
*/

package android.icu.text;
import android.icu.impl.Utility;

/**
 * A replacer that produces static text as its output.  The text may
 * contain transliterator stand-in characters that represent nested
 * UnicodeReplacer objects, making it possible to encode a tree of
 * replacers in a StringReplacer.  A StringReplacer that contains such
 * stand-ins is called a <em>complex</em> StringReplacer.  A complex
 * StringReplacer has a slower processing loop than a non-complex one.
 * @author Alan Liu
 */
class StringReplacer implements UnicodeReplacer {

    /**
     * Output text, possibly containing stand-in characters that
     * represent nested UnicodeReplacers.
     */
    private String output;

    /**
     * Cursor position.  Value is ignored if hasCursor is false.
     */
    private int cursorPos;

    /**
     * True if this object outputs a cursor position.
     */
    private boolean hasCursor;

    /**
     * A complex object contains nested replacers and requires more
     * complex processing.  StringReplacers are initially assumed to
     * be complex.  If no nested replacers are seen during processing,
     * then isComplex is set to false, and future replacements are
     * short circuited for better performance.
     */
    private boolean isComplex;

    /**
     * Object that translates stand-in characters in 'output' to
     * UnicodeReplacer objects.
     */
    private final RuleBasedTransliterator.Data data;

    /**
     * Construct a StringReplacer that sets the emits the given output
     * text and sets the cursor to the given position.
     * @param theOutput text that will replace input text when the
     * replace() method is called.  May contain stand-in characters
     * that represent nested replacers.
     * @param theCursorPos cursor position that will be returned by
     * the replace() method
     * @param theData transliterator context object that translates
     * stand-in characters to UnicodeReplacer objects
     */
    public StringReplacer(String theOutput,
                          int theCursorPos,
                          RuleBasedTransliterator.Data theData) {
        output = theOutput;
        cursorPos = theCursorPos;
        hasCursor = true;
        data = theData;
        isComplex = true;
    }

    /**
     * Construct a StringReplacer that sets the emits the given output
     * text and does not modify the cursor.
     * @param theOutput text that will replace input text when the
     * replace() method is called.  May contain stand-in characters
     * that represent nested replacers.
     * @param theData transliterator context object that translates
     * stand-in characters to UnicodeReplacer objects
     */
    public StringReplacer(String theOutput,
                          RuleBasedTransliterator.Data theData) {
        output = theOutput;
        cursorPos = 0;
        hasCursor = false;
        data = theData;
        isComplex = true;
    }

//=    public static UnicodeReplacer valueOf(String output,
//=                                          int cursorPos,
//=                                          RuleBasedTransliterator.Data data) {
//=        if (output.length() == 1) {
//=            char c = output.charAt(0);
//=            UnicodeReplacer r = data.lookupReplacer(c);
//=            if (r != null) {
//=                return r;
//=            }
//=        }
//=        return new StringReplacer(output, cursorPos, data);
//=    }

    /**
     * UnicodeReplacer API
     */
    @Override
    public int replace(Replaceable text,
                       int start,
                       int limit,
                       int[] cursor) {
        int outLen;
        int newStart = 0;

        // NOTE: It should be possible to _always_ run the complex
        // processing code; just slower.  If not, then there is a bug
        // in the complex processing code.

        // Simple (no nested replacers) Processing Code :
        if (!isComplex) {
            text.replace(start, limit, output);
            outLen = output.length();

            // Setup default cursor position (for cursorPos within output)
            newStart = cursorPos;
        }

        // Complex (nested replacers) Processing Code :
        else {
            /* When there are segments to be copied, use the Replaceable.copy()
             * API in order to retain out-of-band data.  Copy everything to the
             * end of the string, then copy them back over the key.  This preserves
             * the integrity of indices into the key and surrounding context while
             * generating the output text.
             */
            StringBuffer buf = new StringBuffer();
            int oOutput; // offset into 'output'
            isComplex = false;

            // The temporary buffer starts at tempStart, and extends
            // to destLimit + tempExtra.  The start of the buffer has a single
            // character from before the key.  This provides style
            // data when addition characters are filled into the
            // temporary buffer.  If there is nothing to the left, use
            // the non-character U+FFFF, which Replaceable subclasses
            // should treat specially as a "no-style character."
            // destStart points to the point after the style context
            // character, so it is tempStart+1 or tempStart+2.
            int tempStart = text.length(); // start of temp buffer
            int destStart = tempStart; // copy new text to here
            if (start > 0) {
                int len = UTF16.getCharCount(text.char32At(start-1));
                text.copy(start-len, start, tempStart);
                destStart += len;
            } else {
                text.replace(tempStart, tempStart, "\uFFFF");
                destStart++;
            }
            int destLimit = destStart;
            int tempExtra = 0; // temp chars after destLimit

            for (oOutput=0; oOutput<output.length(); ) {
                if (oOutput == cursorPos) {
                    // Record the position of the cursor
                    newStart = buf.length() + destLimit - destStart; // relative to start
                    // the buf.length() was inserted for bug 5789
                    // the problem is that if we are accumulating into a buffer (when r == null below)
                    // then the actual length of the text at that point needs to add the buf length.
                    // there was an alternative suggested in #5789, but that looks like it won't work
                    // if we have accumulated some stuff in the dest part AND have a non-zero buffer.
                }
                int c = UTF16.charAt(output, oOutput);

                // When we are at the last position copy the right style
                // context character into the temporary buffer.  We don't
                // do this before because it will provide an incorrect
                // right context for previous replace() operations.
                int nextIndex = oOutput + UTF16.getCharCount(c);
                if (nextIndex == output.length()) {
                    tempExtra = UTF16.getCharCount(text.char32At(limit));
                    text.copy(limit, limit+tempExtra, destLimit);
                }

                UnicodeReplacer r = data.lookupReplacer(c);
                if (r == null) {
                    // Accumulate straight (non-segment) text.
                    UTF16.append(buf, c);
                } else {
                    isComplex = true;

                    // Insert any accumulated straight text.
                    if (buf.length() > 0) {
                        text.replace(destLimit, destLimit, buf.toString());
                        destLimit += buf.length();
                        buf.setLength(0);
                    }

                    // Delegate output generation to replacer object
                    int len = r.replace(text, destLimit, destLimit, cursor);
                    destLimit += len;
                }
                oOutput = nextIndex;
            }
            // Insert any accumulated straight text.
            if (buf.length() > 0) {
                text.replace(destLimit, destLimit, buf.toString());
                destLimit += buf.length();
            }
            if (oOutput == cursorPos) {
                // Record the position of the cursor
                newStart = destLimit - destStart; // relative to start
            }

            outLen = destLimit - destStart;

            // Copy new text to start, and delete it
            text.copy(destStart, destLimit, start);
            text.replace(tempStart + outLen, destLimit + tempExtra + outLen, "");

            // Delete the old text (the key)
            text.replace(start + outLen, limit + outLen, "");
        }

        if (hasCursor) {
            // Adjust the cursor for positions outside the key.  These
            // refer to code points rather than code units.  If cursorPos
            // is within the output string, then use newStart, which has
            // already been set above.
            if (cursorPos < 0) {
                newStart = start;
                int n = cursorPos;
                // Outside the output string, cursorPos counts code points
                while (n < 0 && newStart > 0) {
                    newStart -= UTF16.getCharCount(text.char32At(newStart-1));
                    ++n;
                }
                newStart += n;
            } else if (cursorPos > output.length()) {
                newStart = start + outLen;
                int n = cursorPos - output.length();
                // Outside the output string, cursorPos counts code points
                while (n > 0 && newStart < text.length()) {
                    newStart += UTF16.getCharCount(text.char32At(newStart));
                    --n;
                }
                newStart += n;
            } else {
                // Cursor is within output string.  It has been set up above
                // to be relative to start.
                newStart += start;
            }

            cursor[0] = newStart;
        }

        return outLen;
    }

    /**
     * UnicodeReplacer API
     */
    @Override
    public String toReplacerPattern(boolean escapeUnprintable) {
        StringBuffer rule = new StringBuffer();
        StringBuffer quoteBuf = new StringBuffer();

        int cursor = cursorPos;

        // Handle a cursor preceding the output
        if (hasCursor && cursor < 0) {
            while (cursor++ < 0) {
                Utility.appendToRule(rule, '@', true, escapeUnprintable, quoteBuf);
            }
            // Fall through and append '|' below
        }

        for (int i=0; i<output.length(); ++i) {
            if (hasCursor && i == cursor) {
                Utility.appendToRule(rule, '|', true, escapeUnprintable, quoteBuf);
            }
            char c = output.charAt(i); // Ok to use 16-bits here

            UnicodeReplacer r = data.lookupReplacer(c);
            if (r == null) {
                Utility.appendToRule(rule, c, false, escapeUnprintable, quoteBuf);
            } else {
                StringBuffer buf = new StringBuffer(" ");
                buf.append(r.toReplacerPattern(escapeUnprintable));
                buf.append(' ');
                Utility.appendToRule(rule, buf.toString(),
                                     true, escapeUnprintable, quoteBuf);
            }
        }

        // Handle a cursor after the output.  Use > rather than >= because
        // if cursor == output.length() it is at the end of the output,
        // which is the default position, so we need not emit it.
        if (hasCursor && cursor > output.length()) {
            cursor -= output.length();
            while (cursor-- > 0) {
                Utility.appendToRule(rule, '@', true, escapeUnprintable, quoteBuf);
            }
            Utility.appendToRule(rule, '|', true, escapeUnprintable, quoteBuf);
        }
        // Flush quoteBuf out to result
        Utility.appendToRule(rule, -1,
                             true, escapeUnprintable, quoteBuf);

        return rule.toString();
    }

    /**
     * Union the set of all characters that may output by this object
     * into the given set.
     * @param toUnionTo the set into which to union the output characters
     */
    @Override
    public void addReplacementSetTo(UnicodeSet toUnionTo) {
        int ch;
        for (int i=0; i<output.length(); i+=UTF16.getCharCount(ch)) {
            ch = UTF16.charAt(output, i);
            UnicodeReplacer r = data.lookupReplacer(ch);
            if (r == null) {
                toUnionTo.add(ch);
            } else {
                r.addReplacementSetTo(toUnionTo);
            }
        }
    }
}

//eof
