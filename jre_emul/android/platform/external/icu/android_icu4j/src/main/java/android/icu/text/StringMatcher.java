/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2001-2004, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.text;
import android.icu.impl.Utility;

/**
 * An object that matches a fixed input string, implementing the
 * UnicodeMatcher API.  This object also implements the
 * UnicodeReplacer API, allowing it to emit the matched text as
 * output.  Since the match text may contain flexible match elements,
 * such as UnicodeSets, the emitted text is not the match pattern, but
 * instead a substring of the actual matched text.  Following
 * convention, the output text is the leftmost match seen up to this
 * point.
 *
 * A StringMatcher may represent a segment, in which case it has a
 * positive segment number.  This affects how the matcher converts
 * itself to a pattern but does not otherwise affect its function.
 *
 * A StringMatcher that is not a segment should not be used as a
 * UnicodeReplacer.
 */
class StringMatcher implements UnicodeMatcher, UnicodeReplacer {

    /**
     * The text to be matched.
     */
    private String pattern;

    /**
     * Start offset, in the match text, of the <em>rightmost</em>
     * match.
     */
    private int matchStart;

    /**
     * Limit offset, in the match text, of the <em>rightmost</em>
     * match.
     */
    private int matchLimit;

    /**
     * The segment number, 1-based, or 0 if not a segment.
     */
    private int segmentNumber;

    /**
     * Context object that maps stand-ins to matcher and replacer
     * objects.
     */
    private final RuleBasedTransliterator.Data data;

    /**
     * Construct a matcher that matches the given pattern string.
     * @param theString the pattern to be matched, possibly containing
     * stand-ins that represent nested UnicodeMatcher objects.
     * @param segmentNum the segment number from 1..n, or 0 if this is
     * not a segment.
     * @param theData context object mapping stand-ins to
     * UnicodeMatcher objects.
     */
    public StringMatcher(String theString,
                         int segmentNum,
                         RuleBasedTransliterator.Data theData) {
        data = theData;
        pattern = theString;
        matchStart = matchLimit = -1;
        segmentNumber = segmentNum;
    }

    /**
     * Construct a matcher that matches a substring of the given
     * pattern string.
     * @param theString the pattern to be matched, possibly containing
     * stand-ins that represent nested UnicodeMatcher objects.
     * @param start first character of theString to be matched
     * @param limit index after the last character of theString to be
     * matched.
     * @param segmentNum the segment number from 1..n, or 0 if this is
     * not a segment.
     * @param theData context object mapping stand-ins to
     * UnicodeMatcher objects.
     */
    public StringMatcher(String theString,
                         int start,
                         int limit,
                         int segmentNum,
                         RuleBasedTransliterator.Data theData) {
        this(theString.substring(start, limit), segmentNum, theData);
    }

    /**
     * Implement UnicodeMatcher
     */
    @Override
    public int matches(Replaceable text,
                       int[] offset,
                       int limit,
                       boolean incremental) {
        // Note (1): We process text in 16-bit code units, rather than
        // 32-bit code points.  This works because stand-ins are
        // always in the BMP and because we are doing a literal match
        // operation, which can be done 16-bits at a time.
        int i;
        int[] cursor = new int[] { offset[0] };
        if (limit < cursor[0]) {
            // Match in the reverse direction
            for (i=pattern.length()-1; i>=0; --i) {
                char keyChar = pattern.charAt(i); // OK; see note (1) above
                UnicodeMatcher subm = data.lookupMatcher(keyChar);
                if (subm == null) {
                    if (cursor[0] > limit &&
                        keyChar == text.charAt(cursor[0])) { // OK; see note (1) above
                        --cursor[0];
                    } else {
                        return U_MISMATCH;
                    }
                } else {
                    int m =
                        subm.matches(text, cursor, limit, incremental);
                    if (m != U_MATCH) {
                        return m;
                    }
                }
            }
            // Record the match position, but adjust for a normal
            // forward start, limit, and only if a prior match does not
            // exist -- we want the rightmost match.
            if (matchStart < 0) {
                matchStart = cursor[0]+1;
                matchLimit = offset[0]+1;
            }
        } else {
            for (i=0; i<pattern.length(); ++i) {
                if (incremental && cursor[0] == limit) {
                    // We've reached the context limit without a mismatch and
                    // without completing our match.
                    return U_PARTIAL_MATCH;
                }
                char keyChar = pattern.charAt(i); // OK; see note (1) above
                UnicodeMatcher subm = data.lookupMatcher(keyChar);
                if (subm == null) {
                    // Don't need the cursor < limit check if
                    // incremental is true (because it's done above); do need
                    // it otherwise.
                    if (cursor[0] < limit &&
                        keyChar == text.charAt(cursor[0])) { // OK; see note (1) above
                        ++cursor[0];
                    } else {
                        return U_MISMATCH;
                    }
                } else {
                    int m =
                        subm.matches(text, cursor, limit, incremental);
                    if (m != U_MATCH) {
                        return m;
                    }
                }
            }
            // Record the match position
            matchStart = offset[0];
            matchLimit = cursor[0];
        }

        offset[0] = cursor[0];
        return U_MATCH;
    }

    /**
     * Implement UnicodeMatcher
     */
    @Override
    public String toPattern(boolean escapeUnprintable) {
        StringBuffer result = new StringBuffer();
        StringBuffer quoteBuf = new StringBuffer();
        if (segmentNumber > 0) { // i.e., if this is a segment
            result.append('(');
        }
        for (int i=0; i<pattern.length(); ++i) {
            char keyChar = pattern.charAt(i); // OK; see note (1) above
            UnicodeMatcher m = data.lookupMatcher(keyChar);
            if (m == null) {
                Utility.appendToRule(result, keyChar, false, escapeUnprintable, quoteBuf);
            } else {
                Utility.appendToRule(result, m.toPattern(escapeUnprintable),
                                     true, escapeUnprintable, quoteBuf);
            }
        }
        if (segmentNumber > 0) { // i.e., if this is a segment
            result.append(')');
        }
        // Flush quoteBuf out to result
        Utility.appendToRule(result, -1,
                             true, escapeUnprintable, quoteBuf);
        return result.toString();
    }

    /**
     * Implement UnicodeMatcher
     */
    @Override
    public boolean matchesIndexValue(int v) {
        if (pattern.length() == 0) {
            return true;
        }
        int c = UTF16.charAt(pattern, 0);
        UnicodeMatcher m = data.lookupMatcher(c);
        return (m == null) ? ((c & 0xFF) == v) : m.matchesIndexValue(v);
    }

    /**
     * Implementation of UnicodeMatcher API.  Union the set of all
     * characters that may be matched by this object into the given
     * set.
     * @param toUnionTo the set into which to union the source characters
     */
    @Override
    public void addMatchSetTo(UnicodeSet toUnionTo) {
        int ch;
        for (int i=0; i<pattern.length(); i+=UTF16.getCharCount(ch)) {
            ch = UTF16.charAt(pattern, i);
            UnicodeMatcher matcher = data.lookupMatcher(ch);
            if (matcher == null) {
                toUnionTo.add(ch);
            } else {
                matcher.addMatchSetTo(toUnionTo);
            }
        }
    }

    /**
     * UnicodeReplacer API
     */
    @Override
    public int replace(Replaceable text,
                       int start,
                       int limit,
                       int[] cursor) {

        int outLen = 0;

        // Copy segment with out-of-band data
        int dest = limit;
        // If there was no match, that means that a quantifier
        // matched zero-length.  E.g., x (a)* y matched "xy".
        if (matchStart >= 0) {
            if (matchStart != matchLimit) {
                text.copy(matchStart, matchLimit, dest);
                outLen = matchLimit - matchStart;
            }
        }

        text.replace(start, limit, ""); // delete original text

        return outLen;
    }

    /**
     * UnicodeReplacer API
     */
    @Override
    public String toReplacerPattern(boolean escapeUnprintable) {
        // assert(segmentNumber > 0);
        StringBuffer rule = new StringBuffer("$");
        Utility.appendNumber(rule, segmentNumber, 10, 1);
        return rule.toString();
    }

    /**
     * Remove any match data.  This must be called before performing a
     * set of matches with this segment.
     */
    public void resetMatch() {
        matchStart = matchLimit = -1;
    }

    /**
     * Union the set of all characters that may output by this object
     * into the given set.
     * @param toUnionTo the set into which to union the output characters
     */
    @Override
    public void addReplacementSetTo(UnicodeSet toUnionTo) {
        // The output of this replacer varies; it is the source text between
        // matchStart and matchLimit.  Since this varies depending on the
        // input text, we can't compute it here.  We can either do nothing
        // or we can add ALL characters to the set.  It's probably more useful
        // to do nothing.
    }
}

//eof
