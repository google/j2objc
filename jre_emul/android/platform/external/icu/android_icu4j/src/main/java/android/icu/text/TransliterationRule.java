/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.text;

import android.icu.impl.Utility;

/**
 * A transliteration rule used by
 * <code>RuleBasedTransliterator</code>.
 * <code>TransliterationRule</code> is an immutable object.
 *
 * <p>A rule consists of an input pattern and an output string.  When
 * the input pattern is matched, the output string is emitted.  The
 * input pattern consists of zero or more characters which are matched
 * exactly (the key) and optional context.  Context must match if it
 * is specified.  Context may be specified before the key, after the
 * key, or both.  The key, preceding context, and following context
 * may contain variables.  Variables represent a set of Unicode
 * characters, such as the letters <i>a</i> through <i>z</i>.
 * Variables are detected by looking up each character in a supplied
 * variable list to see if it has been so defined.
 *
 * <p>A rule may contain segments in its input string and segment
 * references in its output string.  A segment is a substring of the
 * input pattern, indicated by an offset and limit.  The segment may
 * be in the preceding or following context.  It may not span a
 * context boundary.  A segment reference is a special character in
 * the output string that causes a segment of the input string (not
 * the input pattern) to be copied to the output string.  The range of
 * special characters that represent segment references is defined by
 * RuleBasedTransliterator.Data.
 *
 * <p>Example: The rule "([a-z]) . ([0-9]) > $2 . $1" will change the input
 * string "abc.123" to "ab1.c23".
 *
 * <p>Copyright &copy; IBM Corporation 1999.  All rights reserved.
 *
 * @author Alan Liu
 */
class TransliterationRule {

    // TODO Eliminate the pattern and keyLength data members.  They
    // are used only by masks() and getIndexValue() which are called
    // only during build time, not during run-time.  Perhaps these
    // methods and pattern/keyLength can be isolated into a separate
    // object.

    /**
     * The match that must occur before the key, or null if there is no
     * preceding context.
     */
    private StringMatcher anteContext;

    /**
     * The matcher object for the key.  If null, then the key is empty.
     */
    private StringMatcher key;

    /**
     * The match that must occur after the key, or null if there is no
     * following context.
     */
    private StringMatcher postContext;

    /**
     * The object that performs the replacement if the key,
     * anteContext, and postContext are matched.  Never null.
     */
    private UnicodeReplacer output;

    /**
     * The string that must be matched, consisting of the anteContext, key,
     * and postContext, concatenated together, in that order.  Some components
     * may be empty (zero length).
     * @see anteContextLength
     * @see keyLength
     */
    private String pattern;

    /**
     * An array of matcher objects corresponding to the input pattern
     * segments.  If there are no segments this is null.  N.B. This is
     * a UnicodeMatcher for generality, but in practice it is always a
     * StringMatcher.  In the future we may generalize this, but for
     * now we sometimes cast down to StringMatcher.
     */
    UnicodeMatcher[] segments;

    /**
     * The length of the string that must match before the key.  If
     * zero, then there is no matching requirement before the key.
     * Substring [0,anteContextLength) of pattern is the anteContext.
     */
    private int anteContextLength;

    /**
     * The length of the key.  Substring [anteContextLength,
     * anteContextLength + keyLength) is the key.
     */
    private int keyLength;

    /**
     * Miscellaneous attributes.
     */
    byte flags;

    /**
     * Flag attributes.
     */
    static final int ANCHOR_START = 1;
    static final int ANCHOR_END   = 2;

    /**
     * An alias pointer to the data for this rule.  The data provides
     * lookup services for matchers and segments.
     */
    private final RuleBasedTransliterator.Data data;


    /**
     * Construct a new rule with the given input, output text, and other
     * attributes.  A cursor position may be specified for the output text.
     * @param input input string, including key and optional ante and
     * post context
     * @param anteContextPos offset into input to end of ante context, or -1 if
     * none.  Must be <= input.length() if not -1.
     * @param postContextPos offset into input to start of post context, or -1
     * if none.  Must be <= input.length() if not -1, and must be >=
     * anteContextPos.
     * @param output output string
     * @param cursorPos offset into output at which cursor is located, or -1 if
     * none.  If less than zero, then the cursor is placed after the
     * <code>output</code>; that is, -1 is equivalent to
     * <code>output.length()</code>.  If greater than
     * <code>output.length()</code> then an exception is thrown.
     * @param cursorOffset an offset to be added to cursorPos to position the
     * cursor either in the ante context, if < 0, or in the post context, if >
     * 0.  For example, the rule "abc{def} > | @@@ xyz;" changes "def" to
     * "xyz" and moves the cursor to before "a".  It would have a cursorOffset
     * of -3.
     * @param segs array of UnicodeMatcher corresponding to input pattern
     * segments, or null if there are none
     * @param anchorStart true if the the rule is anchored on the left to
     * the context start
     * @param anchorEnd true if the rule is anchored on the right to the
     * context limit
     */
    public TransliterationRule(String input,
                               int anteContextPos, int postContextPos,
                               String output,
                               int cursorPos, int cursorOffset,
                               UnicodeMatcher[] segs,
                               boolean anchorStart, boolean anchorEnd,
                               RuleBasedTransliterator.Data theData) {
        data = theData;

        // Do range checks only when warranted to save time
        if (anteContextPos < 0) {
            anteContextLength = 0;
        } else {
            if (anteContextPos > input.length()) {
                throw new IllegalArgumentException("Invalid ante context");
            }
            anteContextLength = anteContextPos;
        }
        if (postContextPos < 0) {
            keyLength = input.length() - anteContextLength;
        } else {
            if (postContextPos < anteContextLength ||
                postContextPos > input.length()) {
                throw new IllegalArgumentException("Invalid post context");
            }
            keyLength = postContextPos - anteContextLength;
        }
        if (cursorPos < 0) {
            cursorPos = output.length();
        } else if (cursorPos > output.length()) {
            throw new IllegalArgumentException("Invalid cursor position");
        }

        // We don't validate the segments array.  The caller must
        // guarantee that the segments are well-formed (that is, that
        // all $n references in the output refer to indices of this
        // array, and that no array elements are null).
        this.segments = segs;

        pattern = input;
        flags = 0;
        if (anchorStart) {
            flags |= ANCHOR_START;
        }
        if (anchorEnd) {
            flags |= ANCHOR_END;
        }

        anteContext = null;
        if (anteContextLength > 0) {
            anteContext = new StringMatcher(pattern.substring(0, anteContextLength),
                                            0, data);
        }

        key = null;
        if (keyLength > 0) {
            key = new StringMatcher(pattern.substring(anteContextLength, anteContextLength + keyLength),
                                    0, data);
        }

        int postContextLength = pattern.length() - keyLength - anteContextLength;
        postContext = null;
        if (postContextLength > 0) {
            postContext = new StringMatcher(pattern.substring(anteContextLength + keyLength),
                                            0, data);
        }

        this.output = new StringReplacer(output, cursorPos + cursorOffset, data);
    }

    /**
     * Return the preceding context length.  This method is needed to
     * support the <code>Transliterator</code> method
     * <code>getMaximumContextLength()</code>.
     */
    public int getAnteContextLength() {
        return anteContextLength + (((flags & ANCHOR_START) != 0) ? 1 : 0);
    }

    /**
     * Internal method.  Returns 8-bit index value for this rule.
     * This is the low byte of the first character of the key,
     * unless the first character of the key is a set.  If it's a
     * set, or otherwise can match multiple keys, the index value is -1.
     */
    final int getIndexValue() {
        if (anteContextLength == pattern.length()) {
            // A pattern with just ante context {such as foo)>bar} can
            // match any key.
            return -1;
        }
        int c = UTF16.charAt(pattern, anteContextLength);
        return data.lookupMatcher(c) == null ? (c & 0xFF) : -1;
    }

    /**
     * Internal method.  Returns true if this rule matches the given
     * index value.  The index value is an 8-bit integer, 0..255,
     * representing the low byte of the first character of the key.
     * It matches this rule if it matches the first character of the
     * key, or if the first character of the key is a set, and the set
     * contains any character with a low byte equal to the index
     * value.  If the rule contains only ante context, as in foo)>bar,
     * then it will match any key.
     */
    final boolean matchesIndexValue(int v) {
        // Delegate to the key, or if there is none, to the postContext.
        // If there is neither then we match any key; return true.
        UnicodeMatcher m = (key != null) ? key : postContext;
        return (m != null) ? m.matchesIndexValue(v) : true;
    }

    /**
     * Return true if this rule masks another rule.  If r1 masks r2 then
     * r1 matches any input string that r2 matches.  If r1 masks r2 and r2 masks
     * r1 then r1 == r2.  Examples: "a>x" masks "ab>y".  "a>x" masks "a[b]>y".
     * "[c]a>x" masks "[dc]a>y".
     */
    public boolean masks(TransliterationRule r2) {
        /* Rule r1 masks rule r2 if the string formed of the
         * antecontext, key, and postcontext overlaps in the following
         * way:
         *
         * r1:      aakkkpppp
         * r2:     aaakkkkkpppp
         *            ^
         *
         * The strings must be aligned at the first character of the
         * key.  The length of r1 to the left of the alignment point
         * must be <= the length of r2 to the left; ditto for the
         * right.  The characters of r1 must equal (or be a superset
         * of) the corresponding characters of r2.  The superset
         * operation should be performed to check for UnicodeSet
         * masking.
         *
         * Anchors:  Two patterns that differ only in anchors only
         * mask one another if they are exactly equal, and r2 has
         * all the anchors r1 has (optionally, plus some).  Here Y
         * means the row masks the column, N means it doesn't.
         *
         *         ab   ^ab    ab$  ^ab$
         *   ab    Y     Y     Y     Y
         *  ^ab    N     Y     N     Y
         *   ab$   N     N     Y     Y
         *  ^ab$   N     N     N     Y
         *
         * Post context: {a}b masks ab, but not vice versa, since {a}b
         * matches everything ab matches, and {a}b matches {|a|}b but ab
         * does not.  Pre context is different (a{b} does not align with
         * ab).
         */

        /* LIMITATION of the current mask algorithm: Some rule
         * maskings are currently not detected.  For example,
         * "{Lu}]a>x" masks "A]a>y".  This can be added later. TODO
         */

        int len = pattern.length();
        int left = anteContextLength;
        int left2 = r2.anteContextLength;
        int right = pattern.length() - left;
        int right2 = r2.pattern.length() - left2;

        // TODO Clean this up -- some logic might be combinable with the
        // next statement.

        // Test for anchor masking
        if (left == left2 && right == right2 &&
            keyLength <= r2.keyLength &&
            r2.pattern.regionMatches(0, pattern, 0, len)) {
            // The following boolean logic implements the table above
            return (flags == r2.flags) ||
                (!((flags & ANCHOR_START) != 0) && !((flags & ANCHOR_END) != 0)) ||
                (((r2.flags & ANCHOR_START) != 0) && ((r2.flags & ANCHOR_END) != 0));
        }

        return left <= left2 &&
            (right < right2 ||
             (right == right2 && keyLength <= r2.keyLength)) &&
            r2.pattern.regionMatches(left2 - left, pattern, 0, len);
    }

    static final int posBefore(Replaceable str, int pos) {
        return (pos > 0) ?
            pos - UTF16.getCharCount(str.char32At(pos-1)) :
            pos - 1;
    }

    static final int posAfter(Replaceable str, int pos) {
        return (pos >= 0 && pos < str.length()) ?
            pos + UTF16.getCharCount(str.char32At(pos)) :
            pos + 1;
    }

    /**
     * Attempt a match and replacement at the given position.  Return
     * the degree of match between this rule and the given text.  The
     * degree of match may be mismatch, a partial match, or a full
     * match.  A mismatch means at least one character of the text
     * does not match the context or key.  A partial match means some
     * context and key characters match, but the text is not long
     * enough to match all of them.  A full match means all context
     * and key characters match.
     *
     * If a full match is obtained, perform a replacement, update pos,
     * and return U_MATCH.  Otherwise both text and pos are unchanged.
     *
     * @param text the text
     * @param pos the position indices
     * @param incremental if TRUE, test for partial matches that may
     * be completed by additional text inserted at pos.limit.
     * @return one of <code>U_MISMATCH</code>,
     * <code>U_PARTIAL_MATCH</code>, or <code>U_MATCH</code>.  If
     * incremental is FALSE then U_PARTIAL_MATCH will not be returned.
     */
    public int matchAndReplace(Replaceable text,
                               Transliterator.Position pos,
                               boolean incremental) {
        // Matching and replacing are done in one method because the
        // replacement operation needs information obtained during the
        // match.  Another way to do this is to have the match method
        // create a match result struct with relevant offsets, and to pass
        // this into the replace method.

        // ============================ MATCH ===========================

        // Reset segment match data
        if (segments != null) {
            for (int i=0; i<segments.length; ++i) {
                ((StringMatcher) segments[i]).resetMatch();
            }
        }

        int keyLimit;
        int[] intRef = new int[1];

        // ------------------------ Ante Context ------------------------

        // A mismatch in the ante context, or with the start anchor,
        // is an outright U_MISMATCH regardless of whether we are
        // incremental or not.
        int oText; // offset into 'text'
        int minOText;

        // Note (1): We process text in 16-bit code units, rather than
        // 32-bit code points.  This works because stand-ins are
        // always in the BMP and because we are doing a literal match
        // operation, which can be done 16-bits at a time.

        int anteLimit = posBefore(text, pos.contextStart);

        int match;

        // Start reverse match at char before pos.start
        intRef[0] = posBefore(text, pos.start);

        if (anteContext != null) {
            match = anteContext.matches(text, intRef, anteLimit, false);
            if (match != UnicodeMatcher.U_MATCH) {
                return UnicodeMatcher.U_MISMATCH;
            }
        }

        oText = intRef[0];

        minOText = posAfter(text, oText);

        // ------------------------ Start Anchor ------------------------

        if (((flags & ANCHOR_START) != 0) && oText != anteLimit) {
            return UnicodeMatcher.U_MISMATCH;
        }

        // -------------------- Key and Post Context --------------------

        intRef[0] = pos.start;

        if (key != null) {
            match = key.matches(text, intRef, pos.limit, incremental);
            if (match != UnicodeMatcher.U_MATCH) {
                return match;
            }
        }

        keyLimit = intRef[0];

        if (postContext != null) {
            if (incremental && keyLimit == pos.limit) {
                // The key matches just before pos.limit, and there is
                // a postContext.  Since we are in incremental mode,
                // we must assume more characters may be inserted at
                // pos.limit -- this is a partial match.
                return UnicodeMatcher.U_PARTIAL_MATCH;
            }

            match = postContext.matches(text, intRef, pos.contextLimit, incremental);
            if (match != UnicodeMatcher.U_MATCH) {
                return match;
            }
        }

        oText = intRef[0];

        // ------------------------- Stop Anchor ------------------------

        if (((flags & ANCHOR_END)) != 0) {
            if (oText != pos.contextLimit) {
                return UnicodeMatcher.U_MISMATCH;
            }
            if (incremental) {
                return UnicodeMatcher.U_PARTIAL_MATCH;
            }
        }

        // =========================== REPLACE ==========================

        // We have a full match.  The key is between pos.start and
        // keyLimit.

        int newLength = output.replace(text, pos.start, keyLimit, intRef);
        int lenDelta = newLength - (keyLimit - pos.start);
        int newStart = intRef[0];

        oText += lenDelta;
        pos.limit += lenDelta;
        pos.contextLimit += lenDelta;
        // Restrict new value of start to [minOText, min(oText, pos.limit)].
        pos.start = Math.max(minOText, Math.min(Math.min(oText, pos.limit), newStart));
        return UnicodeMatcher.U_MATCH;
    }

    /**
     * Create a source string that represents this rule.  Append it to the
     * given string.
     */
    public String toRule(boolean escapeUnprintable) {
       // int i;

        StringBuffer rule = new StringBuffer();

        // Accumulate special characters (and non-specials following them)
        // into quoteBuf.  Append quoteBuf, within single quotes, when
        // a non-quoted element must be inserted.
        StringBuffer quoteBuf = new StringBuffer();

        // Do not emit the braces '{' '}' around the pattern if there
        // is neither anteContext nor postContext.
        boolean emitBraces =
            (anteContext != null) || (postContext != null);

        // Emit start anchor
        if ((flags & ANCHOR_START) != 0) {
            rule.append('^');
        }

        // Emit the input pattern
        Utility.appendToRule(rule, anteContext, escapeUnprintable, quoteBuf);

        if (emitBraces) {
            Utility.appendToRule(rule, '{', true, escapeUnprintable, quoteBuf);
        }

        Utility.appendToRule(rule, key, escapeUnprintable, quoteBuf);

        if (emitBraces) {
            Utility.appendToRule(rule, '}', true, escapeUnprintable, quoteBuf);
        }

        Utility.appendToRule(rule, postContext, escapeUnprintable, quoteBuf);

        // Emit end anchor
        if ((flags & ANCHOR_END) != 0) {
            rule.append('$');
        }

        Utility.appendToRule(rule, " > ", true, escapeUnprintable, quoteBuf);

        // Emit the output pattern

        Utility.appendToRule(rule, output.toReplacerPattern(escapeUnprintable),
                     true, escapeUnprintable, quoteBuf);

        Utility.appendToRule(rule, ';', true, escapeUnprintable, quoteBuf);

        return rule.toString();
    }

    /**
     * Return a string representation of this object.
     * @return string representation of this object
     */
    @Override
    public String toString() {
        return '{' + toRule(true) + '}';
    }

    /**
     * Find the source and target sets, subject to the input filter.
     * There is a known issue with filters containing multiple characters.
     */
    // TODO: Problem: the rule is [{ab}]c > x
    // The filter is [a{bc}].
    // If the input is abc, then the rule will work.
    // However, following code applying the filter won't catch that case.

    void addSourceTargetSet(UnicodeSet filter, UnicodeSet sourceSet, UnicodeSet targetSet, UnicodeSet revisiting) {
        int limit = anteContextLength + keyLength;
        UnicodeSet tempSource = new UnicodeSet();
        UnicodeSet temp = new UnicodeSet();

        // We need to walk through the pattern.
        // Iff some of the characters at ALL of the the positions are matched by the filter, then we add temp to toUnionTo
        for (int i=anteContextLength; i<limit; ) {
            int ch = UTF16.charAt(pattern, i);
            i += UTF16.getCharCount(ch);
            UnicodeMatcher matcher = data.lookupMatcher(ch);
            if (matcher == null) {
                if (!filter.contains(ch)) {
                    return;
                }
                tempSource.add(ch);
            } else {
                try {
                    if (!filter.containsSome((UnicodeSet) matcher)) {
                        return;
                    }
                    matcher.addMatchSetTo(tempSource);
                } catch (ClassCastException e) { // if the matcher is not a UnicodeSet
                    temp.clear();
                    matcher.addMatchSetTo(temp);
                    if (!filter.containsSome(temp)) {
                        return;
                    }
                    tempSource.addAll(temp);
                }
            }
        }
        // if we made our way through the gauntlet, add to source/target
        sourceSet.addAll(tempSource);
        output.addReplacementSetTo(targetSet);
    }
}
