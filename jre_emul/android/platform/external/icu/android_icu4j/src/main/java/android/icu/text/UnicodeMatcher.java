/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2001-2016, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.text;

/**
 * <code>UnicodeMatcher</code> defines a protocol for objects that can
 * match a range of characters in a Replaceable string.
 */
public interface UnicodeMatcher {

    /**
     * Constant returned by <code>matches()</code> indicating a
     * mismatch between the text and this matcher.  The text contains
     * a character which does not match, or the text does not contain
     * all desired characters for a non-incremental match.
     */
    public static final int U_MISMATCH = 0;

    /**
     * Constant returned by <code>matches()</code> indicating a
     * partial match between the text and this matcher.  This value is
     * only returned for incremental match operations.  All characters
     * of the text match, but more characters are required for a
     * complete match.  Alternatively, for variable-length matchers,
     * all characters of the text match, and if more characters were
     * supplied at limit, they might also match.
     */
    public static final int U_PARTIAL_MATCH = 1;

    /**
     * Constant returned by <code>matches()</code> indicating a
     * complete match between the text and this matcher.  For an
     * incremental variable-length match, this value is returned if
     * the given text matches, and it is known that additional
     * characters would not alter the extent of the match.
     */
    public static final int U_MATCH = 2;

    /**
     * The character at index i, where i &lt; contextStart || i &gt;= contextLimit,
     * is ETHER.  This allows explicit matching by rules and UnicodeSets
     * of text outside the context.  In traditional terms, this allows anchoring
     * at the start and/or end.
     */
    static final char ETHER = '\uFFFF';

    /**
     * Return a UMatchDegree value indicating the degree of match for
     * the given text at the given offset.  Zero, one, or more
     * characters may be matched.
     *
     * Matching in the forward direction is indicated by limit &gt;
     * offset.  Characters from offset forwards to limit-1 will be
     * considered for matching.
     *
     * Matching in the reverse direction is indicated by limit &lt;
     * offset.  Characters from offset backwards to limit+1 will be
     * considered for matching.
     *
     * If limit == offset then the only match possible is a zero
     * character match (which subclasses may implement if desired).
     *
     * If U_MATCH is returned, then as a side effect, advance the
     * offset parameter to the limit of the matched substring.  In the
     * forward direction, this will be the index of the last matched
     * character plus one.  In the reverse direction, this will be the
     * index of the last matched character minus one.
     *
     * @param text the text to be matched
     * @param offset on input, the index into text at which to begin
     * matching.  On output, the limit of the matched text.  The
     * number of matched characters is the output value of offset
     * minus the input value.  Offset should always point to the
     * HIGH SURROGATE (leading code unit) of a pair of surrogates,
     * both on entry and upon return.
     * @param limit the limit index of text to be matched.  Greater
     * than offset for a forward direction match, less than offset for
     * a backward direction match.  The last character to be
     * considered for matching will be text.charAt(limit-1) in the
     * forward direction or text.charAt(limit+1) in the backward
     * direction.
     * @param incremental if TRUE, then assume further characters may
     * be inserted at limit and check for partial matching.  Otherwise
     * assume the text as given is complete.
     * @return a match degree value indicating a full match, a partial
     * match, or a mismatch.  If incremental is FALSE then
     * U_PARTIAL_MATCH should never be returned.
     */
    public abstract int matches(Replaceable text,
                                int[] offset,
                                int limit,
                                boolean incremental);

    /**
     * Returns a string representation of this matcher.  If the result of
     * calling this function is passed to the appropriate parser, it
     * will produce another matcher that is equal to this one.
     * @param escapeUnprintable if TRUE then convert unprintable
     * character to their hex escape representations, \\uxxxx or
     * \\Uxxxxxxxx.  Unprintable characters are those other than
     * U+000A, U+0020..U+007E.
     */
    public abstract String toPattern(boolean escapeUnprintable);

    /**
     * Returns TRUE if this matcher will match a character c, where c
     * &amp; 0xFF == v, at offset, in the forward direction (with limit &gt;
     * offset).  This is used by <tt>RuleBasedTransliterator</tt> for
     * indexing.
     *
     * <p>Note:  This API uses an int even though the value will be
     * restricted to 8 bits in order to avoid complications with
     * signedness (bytes convert to ints in the range -128..127).
     */
    public abstract boolean matchesIndexValue(int v);

    /**
     * Union the set of all characters that may be matched by this object
     * into the given set.
     * @param toUnionTo the set into which to union the source characters
     */
    public abstract void addMatchSetTo(UnicodeSet toUnionTo);
}

//eof
