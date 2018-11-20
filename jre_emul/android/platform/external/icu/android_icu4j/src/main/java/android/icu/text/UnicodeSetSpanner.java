/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2014-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.text;

import android.icu.text.UnicodeSet.SpanCondition;
import android.icu.util.OutputInt;

/**
 * A helper class used to count, replace, and trim CharSequences based on UnicodeSet matches.
 * An instance is immutable (and thus thread-safe) iff the source UnicodeSet is frozen.
 * <p><b>Note:</b> The counting, deletion, and replacement depend on alternating a {@link SpanCondition} with
 * its inverse. That is, the code spans, then spans for the inverse, then spans, and so on.
 * For the inverse, the following mapping is used:
 * <ul>
 * <li>{@link UnicodeSet.SpanCondition#SIMPLE} → {@link UnicodeSet.SpanCondition#NOT_CONTAINED}</li>
 * <li>{@link UnicodeSet.SpanCondition#CONTAINED} → {@link UnicodeSet.SpanCondition#NOT_CONTAINED}</li>
 * <li>{@link UnicodeSet.SpanCondition#NOT_CONTAINED} → {@link UnicodeSet.SpanCondition#SIMPLE}</li>
 * </ul>
 * These are actually not complete inverses. However, the alternating works because there are no gaps.
 * For example, with [a{ab}{bc}], you get the following behavior when scanning forward:
 *
 * <table border="1">
 * <tr><th>SIMPLE</th><td>xxx[ab]cyyy</td></tr>
 * <tr><th>CONTAINED</th><td>xxx[abc]yyy</td></tr>
 * <tr><th>NOT_CONTAINED</th><td>[xxx]ab[cyyy]</td></tr>
 * </table>
 * <p>So here is what happens when you alternate:
 *
 * <table border="1">
 * <tr><th>start</th><td>|xxxabcyyy</td></tr>
 * <tr><th>NOT_CONTAINED</th><td>xxx|abcyyy</td></tr>
 * <tr><th>CONTAINED</th><td>xxxabc|yyy</td></tr>
 * <tr><th>NOT_CONTAINED</th><td>xxxabcyyy|</td></tr>
 * </table>
 * <p>The entire string is traversed.
 */
public class UnicodeSetSpanner {

    private final UnicodeSet unicodeSet;

    /**
     * Create a spanner from a UnicodeSet. For speed and safety, the UnicodeSet should be frozen. However, this class
     * can be used with a non-frozen version to avoid the cost of freezing.
     * 
     * @param source
     *            the original UnicodeSet
     */
    public UnicodeSetSpanner(UnicodeSet source) {
        unicodeSet = source;
    }

    /**
     * Returns the UnicodeSet used for processing. It is frozen iff the original was.
     * 
     * @return the construction set.
     */
    public UnicodeSet getUnicodeSet() {
        return unicodeSet;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof UnicodeSetSpanner && unicodeSet.equals(((UnicodeSetSpanner) other).unicodeSet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return unicodeSet.hashCode();
    }

    /**
     * Options for replaceFrom and countIn to control how to treat each matched span. 
     * It is similar to whether one is replacing [abc] by x, or [abc]* by x.
     */
    public enum CountMethod {
        /**
         * Collapse spans. That is, modify/count the entire matching span as a single item, instead of separate
         * set elements.
         */
        WHOLE_SPAN,
        /**
         * Use the smallest number of elements in the spanned range for counting and modification,
         * based on the {@link UnicodeSet.SpanCondition}.
         * If the set has no strings, this will be the same as the number of spanned code points.
         * <p>For example, in the string "abab" with SpanCondition.SIMPLE:
         * <ul>
         * <li>spanning with [ab] will count four MIN_ELEMENTS.</li>
         * <li>spanning with [{ab}] will count two MIN_ELEMENTS.</li>
         * <li>spanning with [ab{ab}] will also count two MIN_ELEMENTS.</li>
         * </ul>
         */
        MIN_ELEMENTS,
        // Note: could in the future have an additional option MAX_ELEMENTS
    }

    /**
     * Returns the number of matching characters found in a character sequence, 
     * counting by CountMethod.MIN_ELEMENTS using SpanCondition.SIMPLE.
     * The code alternates spans; see the class doc for {@link UnicodeSetSpanner} for a note about boundary conditions.
     * @param sequence
     *            the sequence to count characters in
     * @return the count. Zero if there are none.
     */
    public int countIn(CharSequence sequence) {
        return countIn(sequence, CountMethod.MIN_ELEMENTS, SpanCondition.SIMPLE);
    }

    /**
     * Returns the number of matching characters found in a character sequence, using SpanCondition.SIMPLE.
     * The code alternates spans; see the class doc for {@link UnicodeSetSpanner} for a note about boundary conditions.
     * @param sequence
     *            the sequence to count characters in
     * @param countMethod
     *            whether to treat an entire span as a match, or individual elements as matches
     * @return the count. Zero if there are none.
     */
    public int countIn(CharSequence sequence, CountMethod countMethod) {
        return countIn(sequence, countMethod, SpanCondition.SIMPLE);
    }

    /**
     * Returns the number of matching characters found in a character sequence.
     * The code alternates spans; see the class doc for {@link UnicodeSetSpanner} for a note about boundary conditions.
     * @param sequence
     *            the sequence to count characters in
     * @param countMethod
     *            whether to treat an entire span as a match, or individual elements as matches
     * @param spanCondition
     *            the spanCondition to use. SIMPLE or CONTAINED means only count the elements in the span;
     *            NOT_CONTAINED is the reverse.
     *            <br><b>WARNING: </b> when a UnicodeSet contains strings, there may be unexpected behavior in edge cases.
     * @return the count. Zero if there are none.
     */
    public int countIn(CharSequence sequence, CountMethod countMethod, SpanCondition spanCondition) {
        int count = 0;
        int start = 0;
        SpanCondition skipSpan = spanCondition == SpanCondition.NOT_CONTAINED ? SpanCondition.SIMPLE
                : SpanCondition.NOT_CONTAINED;
        final int length = sequence.length();
        OutputInt spanCount = null;
        while (start != length) {
            int endOfSpan = unicodeSet.span(sequence, start, skipSpan);
            if (endOfSpan == length) {
                break;
            }
            if (countMethod == CountMethod.WHOLE_SPAN) {
                start = unicodeSet.span(sequence, endOfSpan, spanCondition);
                count += 1;
            } else {
                if (spanCount == null) {
                    spanCount = new OutputInt();
                }
                start = unicodeSet.spanAndCount(sequence, endOfSpan, spanCondition, spanCount);
                count += spanCount.value;
            }
        }
        return count;
    }

    /**
     * Delete all the matching spans in sequence, using SpanCondition.SIMPLE
     * The code alternates spans; see the class doc for {@link UnicodeSetSpanner} for a note about boundary conditions.
     * @param sequence
     *            charsequence to replace matching spans in.
     * @return modified string.
     */
    public String deleteFrom(CharSequence sequence) {
        return replaceFrom(sequence, "", CountMethod.WHOLE_SPAN, SpanCondition.SIMPLE);
    }

    /**
     * Delete all matching spans in sequence, according to the spanCondition.
     * The code alternates spans; see the class doc for {@link UnicodeSetSpanner} for a note about boundary conditions.
     * @param sequence
     *            charsequence to replace matching spans in.
     * @param spanCondition
     *            specify whether to modify the matching spans (CONTAINED or SIMPLE) or the non-matching (NOT_CONTAINED)
     * @return modified string.
     */
    public String deleteFrom(CharSequence sequence, SpanCondition spanCondition) {
        return replaceFrom(sequence, "", CountMethod.WHOLE_SPAN, spanCondition);
    }

    /**
     * Replace all matching spans in sequence by the replacement,
     * counting by CountMethod.MIN_ELEMENTS using SpanCondition.SIMPLE.
     * The code alternates spans; see the class doc for {@link UnicodeSetSpanner} for a note about boundary conditions.
     * @param sequence
     *            charsequence to replace matching spans in.
     * @param replacement
     *            replacement sequence. To delete, use ""
     * @return modified string.
     */
    public String replaceFrom(CharSequence sequence, CharSequence replacement) {
        return replaceFrom(sequence, replacement, CountMethod.MIN_ELEMENTS, SpanCondition.SIMPLE);
    }

    /**
     * Replace all matching spans in sequence by replacement, according to the CountMethod, using SpanCondition.SIMPLE.
     * The code alternates spans; see the class doc for {@link UnicodeSetSpanner} for a note about boundary conditions.
     * 
     * @param sequence
     *            charsequence to replace matching spans in.
     * @param replacement
     *            replacement sequence. To delete, use ""
     * @param countMethod
     *            whether to treat an entire span as a match, or individual elements as matches
     * @return modified string.
     */
    public String replaceFrom(CharSequence sequence, CharSequence replacement, CountMethod countMethod) {
        return replaceFrom(sequence, replacement, countMethod, SpanCondition.SIMPLE);
    }

    /**
     * Replace all matching spans in sequence by replacement, according to the countMethod and spanCondition.
     * The code alternates spans; see the class doc for {@link UnicodeSetSpanner} for a note about boundary conditions.
     * @param sequence
     *            charsequence to replace matching spans in.
     * @param replacement
     *            replacement sequence. To delete, use ""
     * @param countMethod 
     *            whether to treat an entire span as a match, or individual elements as matches
     * @param spanCondition
     *            specify whether to modify the matching spans (CONTAINED or SIMPLE) or the non-matching
     *            (NOT_CONTAINED)
     * @return modified string.
     */
    public String replaceFrom(CharSequence sequence, CharSequence replacement, CountMethod countMethod,
            SpanCondition spanCondition) {
        SpanCondition copySpan = spanCondition == SpanCondition.NOT_CONTAINED ? SpanCondition.SIMPLE
                : SpanCondition.NOT_CONTAINED;
        final boolean remove = replacement.length() == 0;
        StringBuilder result = new StringBuilder();
        // TODO, we can optimize this to
        // avoid this allocation unless needed

        final int length = sequence.length();
        OutputInt spanCount = null;
        for (int endCopy = 0; endCopy != length;) {
            int endModify;
            if (countMethod == CountMethod.WHOLE_SPAN) {
                endModify = unicodeSet.span(sequence, endCopy, spanCondition);
            } else {
                if (spanCount == null) {
                    spanCount = new OutputInt();
                }
                endModify = unicodeSet.spanAndCount(sequence, endCopy, spanCondition, spanCount);
            }
            if (remove || endModify == 0) {
                // do nothing
            } else if (countMethod == CountMethod.WHOLE_SPAN) {
                result.append(replacement);
            } else {
                for (int i = spanCount.value; i > 0; --i) {
                    result.append(replacement);
                }
            }
            if (endModify == length) {
                break;
            }
            endCopy = unicodeSet.span(sequence, endModify, copySpan);
            result.append(sequence.subSequence(endModify, endCopy));
        }
        return result.toString();
    }

    /**
     * Options for the trim() method
     */
    public enum TrimOption {
        /**
         * Trim leading spans.
         */
        LEADING,
        /**
         * Trim leading and trailing spans.
         */
        BOTH,
        /**
         * Trim trailing spans.
         */
        TRAILING;
    }

    /**
     * Returns a trimmed sequence (using CharSequence.subsequence()), that omits matching elements at the start and
     * end of the string, using TrimOption.BOTH and SpanCondition.SIMPLE. For example:
     * 
     * <pre>
     * {@code
     * 
     *   new UnicodeSet("[ab]").trim("abacatbab")}
     * </pre>
     * 
     * ... returns {@code "cat"}.
     * @param sequence
     *            the sequence to trim
     * @return a subsequence
     */
    public CharSequence trim(CharSequence sequence) {
        return trim(sequence, TrimOption.BOTH, SpanCondition.SIMPLE);
    }

    /**
     * Returns a trimmed sequence (using CharSequence.subsequence()), that omits matching elements at the start or
     * end of the string, using the trimOption and SpanCondition.SIMPLE. For example:
     * 
     * <pre>
     * {@code
     * 
     *   new UnicodeSet("[ab]").trim("abacatbab", TrimOption.LEADING)}
     * </pre>
     * 
     * ... returns {@code "catbab"}.
     * 
     * @param sequence
     *            the sequence to trim
     * @param trimOption
     *            LEADING, TRAILING, or BOTH
     * @return a subsequence
     */
    public CharSequence trim(CharSequence sequence, TrimOption trimOption) {
        return trim(sequence, trimOption, SpanCondition.SIMPLE);
    }

    /**
     * Returns a trimmed sequence (using CharSequence.subsequence()), that omits matching elements at the start or
     * end of the string, depending on the trimOption and spanCondition. For example:
     * 
     * <pre>
     * {@code
     * 
     *   new UnicodeSet("[ab]").trim("abacatbab", TrimOption.LEADING, SpanCondition.SIMPLE)}
     * </pre>
     * 
     * ... returns {@code "catbab"}.
     * 
     * @param sequence
     *            the sequence to trim
     * @param trimOption
     *            LEADING, TRAILING, or BOTH
     * @param spanCondition
     *            SIMPLE, CONTAINED or NOT_CONTAINED
     * @return a subsequence
     */
    public CharSequence trim(CharSequence sequence, TrimOption trimOption, SpanCondition spanCondition) {
        int endLeadContained, startTrailContained;
        final int length = sequence.length();
        if (trimOption != TrimOption.TRAILING) {
            endLeadContained = unicodeSet.span(sequence, spanCondition);
            if (endLeadContained == length) {
                return "";
            }
        } else {
            endLeadContained = 0;
        }
        if (trimOption != TrimOption.LEADING) {
            startTrailContained = unicodeSet.spanBack(sequence, spanCondition);
        } else {
            startTrailContained = length;
        }
        return endLeadContained == 0 && startTrailContained == length ? sequence : sequence.subSequence(
                endLeadContained, startTrailContained);
    }

}
