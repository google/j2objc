/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2009-2014, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.dev.test.lang;

import java.util.Collection;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.Utility;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import android.icu.text.UnicodeSet.SpanCondition;
import android.icu.util.OutputInt;

/**
 * @test
 * @summary General test of UnicodeSet string span.
 */
public class UnicodeSetStringSpanTest extends TestFmwk {
    // Simple test first, easier to debug.
    @Test
    public void TestSimpleStringSpan() {
        String pattern = "[a{ab}{bc}]";
        String string = "abc";
        UnicodeSet set = new UnicodeSet(pattern);
        set.complement();
        int pos = set.spanBack(string, 3, SpanCondition.SIMPLE);
        if (pos != 1) {
            errln(String.format("FAIL: UnicodeSet(%s).spanBack(%s) returns the wrong value pos %d (!= 1)",
                    set.toString(), string, pos));
        }
        pos = set.span(string, SpanCondition.SIMPLE);
        if (pos != 3) {
            errln(String.format("FAIL: UnicodeSet(%s).span(%s) returns the wrong value pos %d (!= 3)",
                    set.toString(), string, pos));
        }
        pos = set.span(string, 1, SpanCondition.SIMPLE);
        if (pos != 3) {
            errln(String.format("FAIL: UnicodeSet(%s).span(%s, 1) returns the wrong value pos %d (!= 3)",
                    set.toString(), string, pos));
        }
    }

    // test our slow implementation
    @Test
    public void TestSimpleStringSpanSlow() {
        String pattern = "[a{ab}{bc}]";
        String string = "abc";
        UnicodeSet uset = new UnicodeSet(pattern);
        uset.complement();
        UnicodeSetWithStrings set = new UnicodeSetWithStrings(uset);

        int length = containsSpanBackUTF16(set, string, 3, SpanCondition.SIMPLE);
        if (length != 1) {
            errln(String.format("FAIL: UnicodeSet(%s) containsSpanBackUTF16(%s) returns the wrong value length %d (!= 1)",
                    set.toString(), string, length));
        }
        length = containsSpanUTF16(set, string, SpanCondition.SIMPLE);
        if (length != 3) {
            errln(String.format("FAIL: UnicodeSet(%s) containsSpanUTF16(%s) returns the wrong value length %d (!= 3)",
                    set.toString(), string, length));
        }
        length = containsSpanUTF16(set, string.substring(1), SpanCondition.SIMPLE);
        if (length != 2) {
            errln(String.format("FAIL: UnicodeSet(%s) containsSpanUTF16(%s) returns the wrong value length %d (!= 2)",
                    set.toString(), string, length));
        }
    }

    // Test select patterns and strings, and test SIMPLE.
    @Test
    public void TestSimpleStringSpanAndFreeze() {
        String pattern = "[x{xy}{xya}{axy}{ax}]";
        final String string = "xx"
                + "xyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxya" + "xx"
                + "xyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxya" + "xx"
                + "xyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxyaxy" + "aaaa";

        UnicodeSet set = new UnicodeSet(pattern);
        if (set.containsAll(string)) {
            errln("FAIL: UnicodeSet(" + pattern + ").containsAll(" + string + ") should be FALSE");
        }

        // Remove trailing "aaaa".
        String string16 = string.substring(0, string.length() - 4);
        if (!set.containsAll(string16)) {
            errln("FAIL: UnicodeSet(" + pattern + ").containsAll(" + string + "[:-4]) should be TRUE");
        }

        String s16 = "byayaxya";
        if (   set.span(s16.substring(0, 8), SpanCondition.NOT_CONTAINED) != 4
            || set.span(s16.substring(0, 7), SpanCondition.NOT_CONTAINED) != 4
            || set.span(s16.substring(0, 6), SpanCondition.NOT_CONTAINED) != 4
            || set.span(s16.substring(0, 5), SpanCondition.NOT_CONTAINED) != 5
            || set.span(s16.substring(0, 4), SpanCondition.NOT_CONTAINED) != 4
            || set.span(s16.substring(0, 3), SpanCondition.NOT_CONTAINED) != 3) {
            errln("FAIL: UnicodeSet(" + pattern + ").span(while not) returns the wrong value");
        }

        pattern = "[a{ab}{abc}{cd}]";
        set.applyPattern(pattern);
        s16 = "acdabcdabccd";
        if (   set.span(s16.substring(0, 12), SpanCondition.CONTAINED) != 12
            || set.span(s16.substring(0, 12), SpanCondition.SIMPLE) != 6
            || set.span(s16.substring(7),     SpanCondition.SIMPLE) != 5) {
            errln("FAIL: UnicodeSet(" + pattern + ").span(while longest match) returns the wrong value");
        }
        set.freeze();
        if (   set.span(s16.substring(0, 12), SpanCondition.CONTAINED) != 12
            || set.span(s16.substring(0, 12), SpanCondition.SIMPLE) != 6
            || set.span(s16.substring(7),     SpanCondition.SIMPLE) != 5) {
            errln("FAIL: UnicodeSet(" + pattern + ").span(while longest match) returns the wrong value");
        }

        pattern = "[d{cd}{bcd}{ab}]";
        set = (UnicodeSet)set.cloneAsThawed();
        set.applyPattern(pattern).freeze();
        s16 = "abbcdabcdabd";
        if (   set.spanBack(s16, 12, SpanCondition.CONTAINED) != 0
            || set.spanBack(s16, 12, SpanCondition.SIMPLE) != 6
            || set.spanBack(s16,  5, SpanCondition.SIMPLE) != 0) {
            errln("FAIL: UnicodeSet(" + pattern + ").spanBack(while longest match) returns the wrong value");
        }
    }

    // more complex test. --------------------------------------------------------

    // Make the strings in a UnicodeSet easily accessible.
    private static class UnicodeSetWithStrings {
        private UnicodeSet set;
        private Collection<String> setStrings;
        private int stringsLength;

        public UnicodeSetWithStrings(final UnicodeSet normalSet) {
            set = normalSet;
            setStrings = normalSet.strings();
            stringsLength = setStrings.size();
        }

        public final UnicodeSet getSet() {
            return set;
        }

        public boolean hasStrings() {
            return (stringsLength > 0);
        }

        public Iterable<String> strings() {
            return setStrings;
        }
    }

    // Compare 16-bit Unicode strings (which may be malformed UTF-16)
    // at code point boundaries.
    // That is, each edge of a match must not be in the middle of a surrogate pair.
    static boolean matches16CPB(final String s, int start, int limit, final String t) {
        limit -= start;
        int length = t.length();
        return t.equals(s.substring(start, start + length))
                && !(0 < start && UTF16.isLeadSurrogate (s.charAt(start - 1)) &&
                                  UTF16.isTrailSurrogate(s.charAt(start)))
                && !(length < limit && UTF16.isLeadSurrogate (s.charAt(start + length - 1)) &&
                                       UTF16.isTrailSurrogate(s.charAt(start + length)));
    }

    // Implement span() with contains() for comparison.
    static int containsSpanUTF16(final UnicodeSetWithStrings set, final String s,
            SpanCondition spanCondition) {
        final UnicodeSet realSet = set.getSet();
        int length = s.length();
        if (!set.hasStrings()) {
            boolean spanContained = false;
            if (spanCondition != SpanCondition.NOT_CONTAINED) {
                spanContained = true; // Pin to 0/1 values.
            }

            int c;
            int start = 0, prev;
            while ((prev = start) < length) {
                c = s.codePointAt(start);
                start = s.offsetByCodePoints(start, 1);
                if (realSet.contains(c) != spanContained) {
                    break;
                }
            }
            return prev;
        } else if (spanCondition == SpanCondition.NOT_CONTAINED) {
            int c;
            int start, next;
            for (start = next = 0; start < length;) {
                c = s.codePointAt(next);
                next = s.offsetByCodePoints(next, 1);
                if (realSet.contains(c)) {
                    break;
                }
                for (String str : set.strings()) {
                    if (str.length() <= (length - start) && matches16CPB(s, start, length, str)) {
                        // spanNeedsStrings=true;
                        return start;
                    }
                }
                start = next;
            }
            return start;
        } else /* CONTAINED or SIMPLE */{
            int c;
            int start, next, maxSpanLimit = 0;
            for (start = next = 0; start < length;) {
                c = s.codePointAt(next);
                next = s.offsetByCodePoints(next, 1);
                if (!realSet.contains(c)) {
                    next = start; // Do not span this single, not-contained code point.
                }
                for (String str : set.strings()) {
                    if (str.length() <= (length - start) && matches16CPB(s, start, length, str)) {
                        // spanNeedsStrings=true;
                        int matchLimit = start + str.length();
                        if (matchLimit == length) {
                            return length;
                        }
                        if (spanCondition == SpanCondition.CONTAINED) {
                            // Iterate for the shortest match at each position.
                            // Recurse for each but the shortest match.
                            if (next == start) {
                                next = matchLimit; // First match from start.
                            } else {
                                if (matchLimit < next) {
                                    // Remember shortest match from start for iteration.
                                    int temp = next;
                                    next = matchLimit;
                                    matchLimit = temp;
                                }
                                // Recurse for non-shortest match from start.
                                int spanLength = containsSpanUTF16(set, s.substring(matchLimit),
                                        SpanCondition.CONTAINED);
                                if ((matchLimit + spanLength) > maxSpanLimit) {
                                    maxSpanLimit = matchLimit + spanLength;
                                    if (maxSpanLimit == length) {
                                        return length;
                                    }
                                }
                            }
                        } else /* spanCondition==SIMPLE */{
                            if (matchLimit > next) {
                                // Remember longest match from start.
                                next = matchLimit;
                            }
                        }
                    }
                }
                if (next == start) {
                    break; // No match from start.
                }
                start = next;
            }
            if (start > maxSpanLimit) {
                return start;
            } else {
                return maxSpanLimit;
            }
        }
    }

    static int containsSpanBackUTF16(final UnicodeSetWithStrings set, final String s, int length,
            SpanCondition spanCondition) {
        if (length == 0) {
            return 0;
        }
        final UnicodeSet realSet = set.getSet();
        if (!set.hasStrings()) {
            boolean spanContained = false;
            if (spanCondition != SpanCondition.NOT_CONTAINED) {
                spanContained = true; // Pin to 0/1 values.
            }

            int c;
            int prev = length;
            do {
                c = s.codePointBefore(prev);
                if (realSet.contains(c) != spanContained) {
                    break;
                }
                prev = s.offsetByCodePoints(prev, -1);
            } while (prev > 0);
            return prev;
        } else if (spanCondition == SpanCondition.NOT_CONTAINED) {
            int c;
            int prev = length, length0 = length;
            do {
                c = s.codePointBefore(prev);
                if (realSet.contains(c)) {
                    break;
                }
                for (String str : set.strings()) {
                    if (str.length() <= prev && matches16CPB(s, prev - str.length(), length0, str)) {
                        // spanNeedsStrings=true;
                        return prev;
                    }
                }
                prev = s.offsetByCodePoints(prev, -1);
            } while (prev > 0);
            return prev;
        } else /* SpanCondition.CONTAINED or SIMPLE */{
            int c;
            int prev = length, minSpanStart = length, length0 = length;
            do {
                c = s.codePointBefore(length);
                length = s.offsetByCodePoints(length, -1);
                if (!realSet.contains(c)) {
                    length = prev; // Do not span this single, not-contained code point.
                }
                for (String str : set.strings()) {
                    if (str.length() <= prev && matches16CPB(s, prev - str.length(), length0, str)) {
                        // spanNeedsStrings=true;
                        int matchStart = prev - str.length();
                        if (matchStart == 0) {
                            return 0;
                        }
                        if (spanCondition == SpanCondition.CONTAINED) {
                            // Iterate for the shortest match at each position.
                            // Recurse for each but the shortest match.
                            if (length == prev) {
                                length = matchStart; // First match from prev.
                            } else {
                                if (matchStart > length) {
                                    // Remember shortest match from prev for iteration.
                                    int temp = length;
                                    length = matchStart;
                                    matchStart = temp;
                                }
                                // Recurse for non-shortest match from prev.
                                int spanStart = containsSpanBackUTF16(set, s, matchStart,
                                        SpanCondition.CONTAINED);
                                if (spanStart < minSpanStart) {
                                    minSpanStart = spanStart;
                                    if (minSpanStart == 0) {
                                        return 0;
                                    }
                                }
                            }
                        } else /* spanCondition==SIMPLE */{
                            if (matchStart < length) {
                                // Remember longest match from prev.
                                length = matchStart;
                            }
                        }
                    }
                }
                if (length == prev) {
                    break; // No match from prev.
                }
            } while ((prev = length) > 0);
            if (prev < minSpanStart) {
                return prev;
            } else {
                return minSpanStart;
            }
        }
    }

    // spans to be performed and compared
    static final int SPAN_UTF16 = 1;
    static final int SPAN_UTF8 = 2;
    static final int SPAN_UTFS = 3;

    static final int SPAN_SET = 4;
    static final int SPAN_COMPLEMENT = 8;
    static final int SPAN_POLARITY = 0xc;

    static final int SPAN_FWD = 0x10;
    static final int SPAN_BACK = 0x20;
    static final int SPAN_DIRS = 0x30;

    static final int SPAN_CONTAINED = 0x100;
    static final int SPAN_SIMPLE = 0x200;
    static final int SPAN_CONDITION = 0x300;

    static final int SPAN_ALL = 0x33f;

    static SpanCondition invertSpanCondition(SpanCondition spanCondition, SpanCondition contained) {
        return spanCondition == SpanCondition.NOT_CONTAINED ? contained
                : SpanCondition.NOT_CONTAINED;
    }

    /*
     * Count spans on a string with the method according to type and set the span limits. The set may be the complement
     * of the original. When using spanBack() and comparing with span(), use a span condition for the first spanBack()
     * according to the expected number of spans. Sets typeName to an empty string if there is no such type. Returns -1
     * if the span option is filtered out.
     */
    static int getSpans(final UnicodeSetWithStrings set, boolean isComplement, final String s,
            int whichSpans, int type, String[] typeName, int limits[], int limitsCapacity,
            int expectCount) {
        final UnicodeSet realSet = set.getSet();
        int start, count, i;
        SpanCondition spanCondition, firstSpanCondition, contained;
        boolean isForward;

        int length = s.length();
        if (type < 0 || 7 < type) {
            typeName[0] = null;
            return 0;
        }

        final String typeNames16[] = {
                "contains",
                "contains(LM)",
                "span",
                "span(LM)",
                "containsBack",
                "containsBack(LM)",
                "spanBack",
                "spanBack(LM)" };

        typeName[0] = typeNames16[type];

        // filter span options
        if (type <= 3) {
            // span forward
            if ((whichSpans & SPAN_FWD) == 0) {
                return -1;
            }
            isForward = true;
        } else {
            // span backward
            if ((whichSpans & SPAN_BACK) == 0) {
                return -1;
            }
            isForward = false;
        }
        if ((type & 1) == 0) {
            // use SpanCondition.CONTAINED
            if ((whichSpans & SPAN_CONTAINED) == 0) {
                return -1;
            }
            contained = SpanCondition.CONTAINED;
        } else {
            // use SIMPLE
            if ((whichSpans & SPAN_SIMPLE) == 0) {
                return -1;
            }
            contained = SpanCondition.SIMPLE;
        }

        // Default first span condition for going forward with an uncomplemented set.
        spanCondition = SpanCondition.NOT_CONTAINED;
        if (isComplement) {
            spanCondition = invertSpanCondition(spanCondition, contained);
        }

        // First span condition for span(), used to terminate the spanBack() iteration.
        firstSpanCondition = spanCondition;

        // spanBack(): Its initial span condition is span()'s last span condition,
        // which is the opposite of span()'s first span condition
        // if we expect an even number of spans.
        // (The loop inverts spanCondition (expectCount-1) times
        // before the expectCount'th span() call.)
        // If we do not compare forward and backward directions, then we do not have an
        // expectCount and just start with firstSpanCondition.
        if (!isForward && (whichSpans & SPAN_FWD) != 0 && (expectCount & 1) == 0) {
            spanCondition = invertSpanCondition(spanCondition, contained);
        }

        count = 0;
        switch (type) {
        case 0:
        case 1:
            start = 0;
            for (;;) {
                start += containsSpanUTF16(set, s.substring(start), spanCondition);
                if (count < limitsCapacity) {
                    limits[count] = start;
                }
                ++count;
                if (start >= length) {
                    break;
                }
                spanCondition = invertSpanCondition(spanCondition, contained);
            }
            break;
        case 2:
        case 3:
            start = 0;
            for (;;) {
                start = realSet.span(s, start, spanCondition);
                if (count < limitsCapacity) {
                    limits[count] = start;
                }
                ++count;
                if (start >= length) {
                    break;
                }
                spanCondition = invertSpanCondition(spanCondition, contained);
            }
            break;
        case 4:
        case 5:
            for (;;) {
                ++count;
                if (count <= limitsCapacity) {
                    limits[limitsCapacity - count] = length;
                }
                length = containsSpanBackUTF16(set, s, length, spanCondition);
                if (length == 0 && spanCondition == firstSpanCondition) {
                    break;
                }
                spanCondition = invertSpanCondition(spanCondition, contained);
            }
            if (count < limitsCapacity) {
                for (i = count; i-- > 0;) {
                    limits[i] = limits[limitsCapacity - count + i];
                }
            }
            break;
        case 6:
        case 7:
            for (;;) {
                ++count;
                if (count <= limitsCapacity) {
                    limits[limitsCapacity - count] = length >= 0 ? length : s.length();
                }
                length = realSet.spanBack(s, length, spanCondition);
                if (length == 0 && spanCondition == firstSpanCondition) {
                    break;
                }
                spanCondition = invertSpanCondition(spanCondition, contained);
            }
            if (count < limitsCapacity) {
                for (i = count; i-- > 0;) {
                    limits[i] = limits[limitsCapacity - count + i];
                }
            }
            break;
        default:
            typeName = null;
            return -1;
        }

        return count;
    }

    // sets to be tested; odd index=isComplement
    static final int SLOW = 0;
    static final int SLOW_NOT = 1;
    static final int FAST = 2;
    static final int FAST_NOT = 3;
    static final int SET_COUNT = 4;

    static final String setNames[] = { "slow", "slow.not", "fast", "fast.not" };

    /*
     * Verify that we get the same results whether we look at text with contains(), span() or spanBack(), using unfrozen
     * or frozen versions of the set, and using the set or its complement (switching the spanConditions accordingly).
     * The latter verifies that set.span(spanCondition) == set.complement().span(!spanCondition).
     * 
     * The expectLimits[] are either provided by the caller (with expectCount>=0) or returned to the caller (with an
     * input expectCount<0).
     */
    void verifySpan(final UnicodeSetWithStrings sets[], final String s, int whichSpans,
            int expectLimits[], int expectCount,
            final String testName, int index) {
        int[] limits = new int[500];
        int limitsCount;
        int i, j;
        String[] typeName = new String[1];
        int type;

        for (i = 0; i < SET_COUNT; ++i) {
            if ((i & 1) == 0) {
                // Even-numbered sets are original, uncomplemented sets.
                if ((whichSpans & SPAN_SET) == 0) {
                    continue;
                }
            } else {
                // Odd-numbered sets are complemented.
                if ((whichSpans & SPAN_COMPLEMENT) == 0) {
                    continue;
                }
            }
            for (type = 0;; ++type) {
                limitsCount = getSpans(sets[i], (0 != (i & 1)), s, whichSpans, type, typeName, limits,
                        limits.length, expectCount);
                if (typeName[0] == null) {
                    break; // All types tried.
                }
                if (limitsCount < 0) {
                    continue; // Span option filtered out.
                }
                if (expectCount < 0) {
                    expectCount = limitsCount;
                    if (limitsCount > limits.length) {
                        errln(String.format("FAIL: %s[0x%x].%s.%s span count=%d > %d capacity - too many spans",
                                testName, index, setNames[i], typeName[0], limitsCount, limits.length));
                        return;
                    }
                    for (j = limitsCount; j-- > 0;) {
                        expectLimits[j] = limits[j];
                    }
                } else if (limitsCount != expectCount) {
                    errln(String.format("FAIL: %s[0x%x].%s.%s span count=%d != %d", testName, index, setNames[i],
                            typeName[0], limitsCount, expectCount));
                } else {
                    for (j = 0; j < limitsCount; ++j) {
                        if (limits[j] != expectLimits[j]) {
                            errln(String.format("FAIL: %s[0x%x].%s.%s span count=%d limits[%d]=%d != %d", testName,
                                    index, setNames[i], typeName[0], limitsCount, j, limits[j], expectLimits[j]));
                            break;
                        }
                    }
                }
            }
        }

        // Compare span() with containsAll()/containsNone(),
        // but only if we have expectLimits[] from the uncomplemented set.
        if ((whichSpans & SPAN_SET) != 0) {
            final String s16 = s;
            String string;
            int prev = 0, limit, len;
            for (i = 0; i < expectCount; ++i) {
                limit = expectLimits[i];
                len = limit - prev;
                if (len > 0) {
                    string = s16.substring(prev, prev + len); // read-only alias
                    if (0 != (i & 1)) {
                        if (!sets[SLOW].getSet().containsAll(string)) {
                            errln(String.format("FAIL: %s[0x%x].%s.containsAll(%d..%d)==false contradicts span()",
                                    testName, index, setNames[SLOW], prev, limit));
                            return;
                        }
                        if (!sets[FAST].getSet().containsAll(string)) {
                            errln(String.format("FAIL: %s[0x%x].%s.containsAll(%d..%d)==false contradicts span()",
                                    testName, index, setNames[FAST], prev, limit));
                            return;
                        }
                    } else {
                        if (!sets[SLOW].getSet().containsNone(string)) {
                            errln(String.format("FAIL: %s[0x%x].%s.containsNone(%d..%d)==false contradicts span()",
                                    testName, index, setNames[SLOW], prev, limit));
                            return;
                        }
                        if (!sets[FAST].getSet().containsNone(string)) {
                            errln(String.format("FAIL: %s[0x%x].%s.containsNone(%d..%d)==false contradicts span()",
                                    testName, index, setNames[FAST], prev, limit));
                            return;
                        }
                    }
                }
                prev = limit;
            }
        }
    }

    // Specifically test either UTF-16 or UTF-8.
    void verifySpan(final UnicodeSetWithStrings sets[], final String s, int whichSpans,
            final String testName, int index) {
        int[] expectLimits = new int[500];
        int expectCount = -1;
        verifySpan(sets, s, whichSpans, expectLimits, expectCount, testName, index);
    }

    // Test both UTF-16 and UTF-8 versions of span() etc. on the same sets and text,
    // unless either UTF is turned off in whichSpans.
    // Testing UTF-16 and UTF-8 together requires that surrogate code points
    // have the same contains(c) value as U+FFFD.
    void verifySpanBothUTFs(final UnicodeSetWithStrings sets[], final String s16, int whichSpans,
            final String testName, int index) {
        int[] expectLimits = new int[500];
        int expectCount;

        expectCount = -1; // Get expectLimits[] from verifySpan().

        if ((whichSpans & SPAN_UTF16) != 0) {
            verifySpan(sets, s16, whichSpans, expectLimits, expectCount, testName, index);
        }
    }

    static int nextCodePoint(int c) {
        // Skip some large and boring ranges.
        switch (c) {
        case 0x3441:
            return 0x4d7f;
        case 0x5100:
            return 0x9f00;
        case 0xb040:
            return 0xd780;
        case 0xe041:
            return 0xf8fe;
        case 0x10100:
            return 0x20000;
        case 0x20041:
            return 0xe0000;
        case 0xe0101:
            return 0x10fffd;
        default:
            return c + 1;
        }
    }

    // Verify that all implementations represent the same set.
    void verifySpanContents(final UnicodeSetWithStrings sets[], int whichSpans, final String testName) {
        StringBuffer s = new StringBuffer();
        int localWhichSpans;
        int c, first;
        for (first = c = 0;; c = nextCodePoint(c)) {
            if (c > 0x10ffff || s.length() > 1024) {
                localWhichSpans = whichSpans;
                verifySpanBothUTFs(sets, s.toString(), localWhichSpans, testName, first);
                if (c > 0x10ffff) {
                    break;
                }
                s.delete(0, s.length());
                first = c;
            }
            UTF16.append(s, c);
        }
    }

    // Test with a particular, interesting string.
    // Specify length and try NUL-termination.
    static final char interestingStringChars[] = { 0x61, 0x62, 0x20, // Latin, space
            0x3b1, 0x3b2, 0x3b3, // Greek
            0xd900, // lead surrogate
            0x3000, 0x30ab, 0x30ad, // wide space, Katakana
            0xdc05, // trail surrogate
            0xa0, 0xac00, 0xd7a3, // nbsp, Hangul
            0xd900, 0xdc05, // unassigned supplementary
            0xd840, 0xdfff, 0xd860, 0xdffe, // Han supplementary
            0xd7a4, 0xdc05, 0xd900, 0x2028  // unassigned, surrogates in wrong order, LS
    };
    static String interestingString = new String(interestingStringChars);
    static final String unicodeSet1 = "[[[:ID_Continue:]-[\\u30ab\\u30ad]]{\\u3000\\u30ab}{\\u3000\\u30ab\\u30ad}]";

    @Test
    public void TestInterestingStringSpan() {
        UnicodeSet uset = new UnicodeSet(Utility.unescape(unicodeSet1));
        SpanCondition spanCondition = SpanCondition.NOT_CONTAINED;
        int expect = 2;
        int start = 14;

        int c = 0xd840;
        boolean contains = uset.contains(c);
        if (false != contains) {
            errln(String.format("FAIL: UnicodeSet(unicodeSet1).contains(%d) = true (expect false)",
                  c));
        }

        UnicodeSetWithStrings set = new UnicodeSetWithStrings(uset);
        int len = containsSpanUTF16(set, interestingString.substring(start), spanCondition);
        if (expect != len) {
            errln(String.format("FAIL: containsSpanUTF16(unicodeSet1, \"%s(%d)\") = %d (expect %d)",
                  interestingString, start, len, expect));
        }

        len = uset.span(interestingString, start, spanCondition) - start;
        if (expect != len) {
            errln(String.format("FAIL: UnicodeSet(unicodeSet1).span(\"%s\", %d) = %d (expect %d)",
                  interestingString, start, len, expect));
        }
    }

    void verifySpanUTF16String(final UnicodeSetWithStrings sets[], int whichSpans, final String testName) {
        if ((whichSpans & SPAN_UTF16) == 0) {
            return;
        }
        verifySpan(sets, interestingString, (whichSpans & ~SPAN_UTF8), testName, 1);
    }

    // Take a set of span options and multiply them so that
    // each portion only has one of the options a, b and c.
    // If b==0, then the set of options is just modified with mask and a.
    // If b!=0 and c==0, then the set of options is just modified with mask, a and b.
    static int addAlternative(int whichSpans[], int whichSpansCount, int mask, int a, int b, int c) {
        int s;
        int i;

        for (i = 0; i < whichSpansCount; ++i) {
            s = whichSpans[i] & mask;
            whichSpans[i] = s | a;
            if (b != 0) {
                whichSpans[whichSpansCount + i] = s | b;
                if (c != 0) {
                    whichSpans[2 * whichSpansCount + i] = s | c;
                }
            }
        }
        return b == 0 ? whichSpansCount : c == 0 ? 2 * whichSpansCount : 3 * whichSpansCount;
    }

    // They are not representable in UTF-8, and a leading trail surrogate
    // and a trailing lead surrogate must not match in the middle of a proper surrogate pair.
    // U+20001 == \\uD840\\uDC01
    // U+20400 == \\uD841\\uDC00
    static final String patternWithUnpairedSurrogate =
        "[a\\U00020001\\U00020400{ab}{b\\uD840}{\\uDC00a}]";
    static final String stringWithUnpairedSurrogate =
        "aaab\\U00020001ba\\U00020400aba\\uD840ab\\uD840\\U00020000b\\U00020000a\\U00020000\\uDC00a\\uDC00babbb";

    static final String _63_a = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    static final String _64_a = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    static final String _63_b = "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb";
    static final String _64_b = "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb";
    static final String longPattern =
        "[a{" + _64_a + _64_a + _64_a + _64_a + "b}" + "{a" + _64_b + _64_b + _64_b + _64_b + "}]";

    @Test
    public void TestStringWithUnpairedSurrogateSpan() {
        String string = Utility.unescape(stringWithUnpairedSurrogate);
        UnicodeSet uset = new UnicodeSet(Utility.unescape(patternWithUnpairedSurrogate));
        SpanCondition spanCondition = SpanCondition.NOT_CONTAINED;
        int start = 17;
        int expect = 5;

        UnicodeSetWithStrings set = new UnicodeSetWithStrings(uset);
        int len = containsSpanUTF16(set, string.substring(start), spanCondition);
        if (expect != len) {
            errln(String.format("FAIL: containsSpanUTF16(patternWithUnpairedSurrogate, \"%s(%d)\") = %d (expect %d)",
                  string, start, len, expect));
        }

        len = uset.span(string, start, spanCondition) - start;
        if (expect != len) {
            errln(String.format("FAIL: UnicodeSet(patternWithUnpairedSurrogate).span(\"%s\", %d) = %d (expect %d)",
                  string, start, len, expect));
        }
    }

    @Test
    public void TestSpan() {
        // "[...]" is a UnicodeSet pattern.
        // "*" performs tests on all Unicode code points and on a selection of
        // malformed UTF-8/16 strings.
        // "-options" limits the scope of testing for the current set.
        // By default, the test verifies that equivalent boundaries are found
        // for UTF-16 and UTF-8, going forward and backward,
        // alternating NOT_CONTAINED with
        // either CONTAINED or SIMPLE.
        // Single-character options:
        // 8 -- UTF-16 and UTF-8 boundaries may differ.
        // Cause: contains(U+FFFD) is inconsistent with contains(some surrogates),
        // or the set contains strings with unpaired surrogates
        // which do not translate to valid UTF-8.
        // c -- set.span() and set.complement().span() boundaries may differ.
        // Cause: Set strings are not complemented.
        // b -- span() and spanBack() boundaries may differ.
        // Cause: Strings in the set overlap, and spanBack(CONTAINED)
        // and spanBack(SIMPLE) are defined to
        // match with non-overlapping substrings.
        // For example, with a set containing "ab" and "ba",
        // span() of "aba" yields boundaries { 0, 2, 3 }
        // because the initial "ab" matches from 0 to 2,
        // while spanBack() yields boundaries { 0, 1, 3 }
        // because the final "ba" matches from 1 to 3.
        // l -- CONTAINED and SIMPLE boundaries may differ.
        // Cause: Strings in the set overlap, and a longer match may
        // require a sequence including non-longest substrings.
        // For example, with a set containing "ab", "abc" and "cd",
        // span(contained) of "abcd" spans the entire string
        // but span(longest match) only spans the first 3 characters.
        // Each "-options" first resets all options and then applies the specified options.
        // A "-" without options resets the options.
        // The options are also reset for each new set.
        // Other strings will be spanned.
        final String testdata[] = {
                "[:ID_Continue:]",
                "*",
                "[:White_Space:]",
                "*",
                "[]",
                "*",
                "[\\u0000-\\U0010FFFF]",
                "*",
                "[\\u0000\\u0080\\u0800\\U00010000]",
                "*",
                "[\\u007F\\u07FF\\uFFFF\\U0010FFFF]",
                "*",
                unicodeSet1,
                "-c",
                "*",
                "[[[:ID_Continue:]-[\\u30ab\\u30ad]]{\\u30ab\\u30ad}{\\u3000\\u30ab\\u30ad}]",
                "-c",
                "*",

                // Overlapping strings cause overlapping attempts to match.
                "[x{xy}{xya}{axy}{ax}]",
                "-cl",

                // More repetitions of "xya" would take too long with the recursive
                // reference implementation.
                // containsAll()=false
                // test_string 0x14
                "xx" + "xyaxyaxyaxya" + // set.complement().span(longest match) will stop here.
                        "xx" + // set.complement().span(contained) will stop between the two 'x'es.
                        "xyaxyaxyaxya" + "xx" + "xyaxyaxyaxya" + // span() ends here.
                        "aaa",

                // containsAll()=true
                // test_string 0x15
                "xx" + "xyaxyaxyaxya" + "xx" + "xyaxyaxyaxya" + "xx" + "xyaxyaxyaxy",

                "-bc",
                // test_string 0x17
                "byayaxya", // span() -> { 4, 7, 8 } spanBack() -> { 5, 8 }
                "-c",
                "byayaxy", // span() -> { 4, 7 } complement.span() -> { 7 }
                "byayax", // span() -> { 4, 6 } complement.span() -> { 6 }
                "-",
                "byaya", // span() -> { 5 }
                "byay", // span() -> { 4 }
                "bya", // span() -> { 3 }

                // span(longest match) will not span the whole string.
                "[a{ab}{bc}]",
                "-cl",
                // test_string 0x21
                "abc",

                "[a{ab}{abc}{cd}]",
                "-cl",
                "acdabcdabccd",

                // spanBack(longest match) will not span the whole string.
                "[c{ab}{bc}]",
                "-cl",
                "abc",

                "[d{cd}{bcd}{ab}]",
                "-cl",
                "abbcdabcdabd",

                // Test with non-ASCII set strings - test proper handling of surrogate pairs
                // and UTF-8 trail bytes.
                // Copies of above test sets and strings, but transliterated to have
                // different code points with similar trail units.
                // Previous: a b c d
                // Unicode: 042B 30AB 200AB 204AB
                // UTF-16: 042B 30AB D840 DCAB D841 DCAB
                // UTF-8: D0 AB E3 82 AB F0 A0 82 AB F0 A0 92 AB
                "[\\u042B{\\u042B\\u30AB}{\\u042B\\u30AB\\U000200AB}{\\U000200AB\\U000204AB}]",
                "-cl",
                "\\u042B\\U000200AB\\U000204AB\\u042B\\u30AB\\U000200AB\\U000204AB\\u042B\\u30AB\\U000200AB\\U000200AB\\U000204AB",

                "[\\U000204AB{\\U000200AB\\U000204AB}{\\u30AB\\U000200AB\\U000204AB}{\\u042B\\u30AB}]",
                "-cl",
                "\\u042B\\u30AB\\u30AB\\U000200AB\\U000204AB\\u042B\\u30AB\\U000200AB\\U000204AB\\u042B\\u30AB\\U000204AB",

                // Stress bookkeeping and recursion.
                // The following strings are barely doable with the recursive
                // reference implementation.
                // The not-contained character at the end prevents an early exit from the span().
                "[b{bb}]",
                "-c",
                // test_string 0x33
                "bbbbbbbbbbbbbbbbbbbbbbbb-",
                // On complement sets, span() and spanBack() get different results
                // because b is not in the complement set and there is an odd number of b's
                // in the test string.
                "-bc",
                "bbbbbbbbbbbbbbbbbbbbbbbbb-",

                // Test with set strings with an initial or final code point span
                // longer than 254.
                longPattern,
                "-c",
                _64_a + _64_a + _64_a + _63_a + "b",
                _64_a + _64_a + _64_a + _64_a + "b",
                _64_a + _64_a + _64_a + _64_a + "aaaabbbb",
                "a" + _64_b + _64_b + _64_b + _63_b,
                "a" + _64_b + _64_b + _64_b + _64_b,
                "aaaabbbb" + _64_b + _64_b + _64_b + _64_b,

                // Test with strings containing unpaired surrogates.
                patternWithUnpairedSurrogate, "-8cl",
                stringWithUnpairedSurrogate };
        int i, j;
        int whichSpansCount = 1;
        int[] whichSpans = new int[96];
        for (i = whichSpans.length; i-- > 0;) {
            whichSpans[i] = SPAN_ALL;
        }

        UnicodeSet[] sets = new UnicodeSet[SET_COUNT];
        UnicodeSetWithStrings[] sets_with_str = new UnicodeSetWithStrings[SET_COUNT];

        String testName = null;
        @SuppressWarnings("unused")
        String testNameLimit;

        for (i = 0; i < testdata.length; ++i) {
            final String s = testdata[i];
            if (s.charAt(0) == '[') {
                // Create new test sets from this pattern.
                for (j = 0; j < SET_COUNT; ++j) {
                    sets_with_str[j] = null;
                    sets[j] = null;
                }
                sets[SLOW] = new UnicodeSet(Utility.unescape(s));
                sets[SLOW_NOT] = new UnicodeSet(sets[SLOW]);
                sets[SLOW_NOT].complement();
                // Intermediate set: Test cloning of a frozen set.
                UnicodeSet fast = new UnicodeSet(sets[SLOW]);
                fast.freeze();
                sets[FAST] = (UnicodeSet) fast.clone();
                fast = null;
                UnicodeSet fastNot = new UnicodeSet(sets[SLOW_NOT]);
                fastNot.freeze();
                sets[FAST_NOT] = (UnicodeSet) fastNot.clone();
                fastNot = null;

                for (j = 0; j < SET_COUNT; ++j) {
                    sets_with_str[j] = new UnicodeSetWithStrings(sets[j]);
                }

                testName = s + ':';
                whichSpans[0] = SPAN_ALL;
                whichSpansCount = 1;
            } else if (s.charAt(0) == '-') {
                whichSpans[0] = SPAN_ALL;
                whichSpansCount = 1;

                for (j = 1; j < s.length(); j++) {
                    switch (s.charAt(j)) {
                    case 'c':
                        whichSpansCount = addAlternative(whichSpans, whichSpansCount, ~SPAN_POLARITY, SPAN_SET,
                                SPAN_COMPLEMENT, 0);
                        break;
                    case 'b':
                        whichSpansCount = addAlternative(whichSpans, whichSpansCount, ~SPAN_DIRS, SPAN_FWD, SPAN_BACK,
                                0);
                        break;
                    case 'l':
                        // test CONTAINED FWD & BACK, and separately
                        // SIMPLE only FWD, and separately
                        // SIMPLE only BACK
                        whichSpansCount = addAlternative(whichSpans, whichSpansCount, ~(SPAN_DIRS | SPAN_CONDITION),
                                SPAN_DIRS | SPAN_CONTAINED, SPAN_FWD | SPAN_SIMPLE, SPAN_BACK | SPAN_SIMPLE);
                        break;
                    case '8':
                        whichSpansCount = addAlternative(whichSpans, whichSpansCount, ~SPAN_UTFS, SPAN_UTF16,
                                SPAN_UTF8, 0);
                        break;
                    default:
                        errln(String.format("FAIL: unrecognized span set option in \"%s\"", testdata[i]));
                        break;
                    }
                }
            } else if (s.equals("*")) {
                testNameLimit = "bad_string";
                for (j = 0; j < whichSpansCount; ++j) {
                    if (whichSpansCount > 1) {
                        testNameLimit += String.format("%%0x%3x", whichSpans[j]);
                    }
                    verifySpanUTF16String(sets_with_str, whichSpans[j], testName);
                }

                testNameLimit = "contents";
                for (j = 0; j < whichSpansCount; ++j) {
                    if (whichSpansCount > 1) {
                        testNameLimit += String.format("%%0x%3x", whichSpans[j]);
                    }
                    verifySpanContents(sets_with_str, whichSpans[j], testName);
                }
            } else {
                String string = Utility.unescape(s);
                testNameLimit = "test_string";
                for (j = 0; j < whichSpansCount; ++j) {
                    if (whichSpansCount > 1) {
                        testNameLimit += String.format("%%0x%3x", whichSpans[j]);
                    }
                    verifySpanBothUTFs(sets_with_str, string, whichSpans[j], testName, i);
                }
            }
        }
    }

    @Test
    public void TestSpanAndCount() {
        // a set with no strings
        UnicodeSet abc = new UnicodeSet('a', 'c');
        // a set with an "irrelevant" string (fully contained in the code point set)
        UnicodeSet crlf = new UnicodeSet().add('\n').add('\r').add("\r\n");
        // a set with no "irrelevant" string but some interesting overlaps
        UnicodeSet ab_cd = new UnicodeSet().add('a').add("ab").add("abc").add("cd");
        String s = "ab\n\r\r\n" + UTF16.valueOf(0x50000) + "abcde";
        OutputInt count = new OutputInt();
        assertEquals("abc span[8, 11[", 11,
                abc.spanAndCount(s, 8, SpanCondition.SIMPLE, count));
        assertEquals("abc count=3", 3, count.value);
        assertEquals("no abc span[2, 8[", 8,
                abc.spanAndCount(s, 2, SpanCondition.NOT_CONTAINED, count));
        assertEquals("no abc count=5", 5, count.value);
        assertEquals("line endings span[2, 6[", 6,
                crlf.spanAndCount(s, 2, SpanCondition.CONTAINED, count));
        assertEquals("line endings count=3", 3, count.value);
        assertEquals("no ab+cd span[2, 8[", 8,
                ab_cd.spanAndCount(s, 2, SpanCondition.NOT_CONTAINED, count));
        assertEquals("no ab+cd count=5", 5, count.value);
        assertEquals("ab+cd span[8, 12[", 12,
                ab_cd.spanAndCount(s, 8, SpanCondition.CONTAINED, count));
        assertEquals("ab+cd count=2", 2, count.value);
        assertEquals("1x abc span[8, 11[", 11,
                ab_cd.spanAndCount(s, 8, SpanCondition.SIMPLE, count));
        assertEquals("1x abc count=1", 1, count.value);

        abc.freeze();
        crlf.freeze();
        ab_cd.freeze();
        assertEquals("abc span[8, 11[ (frozen)", 11,
                abc.spanAndCount(s, 8, SpanCondition.SIMPLE, count));
        assertEquals("abc count=3 (frozen)", 3, count.value);
        assertEquals("no abc span[2, 8[ (frozen)", 8,
                abc.spanAndCount(s, 2, SpanCondition.NOT_CONTAINED, count));
        assertEquals("no abc count=5 (frozen)", 5, count.value);
        assertEquals("line endings span[2, 6[ (frozen)", 6,
                crlf.spanAndCount(s, 2, SpanCondition.CONTAINED, count));
        assertEquals("line endings count=3 (frozen)", 3, count.value);
        assertEquals("no ab+cd span[2, 8[ (frozen)", 8,
                ab_cd.spanAndCount(s, 2, SpanCondition.NOT_CONTAINED, count));
        assertEquals("no ab+cd count=5 (frozen)", 5, count.value);
        assertEquals("ab+cd span[8, 12[ (frozen)", 12,
                ab_cd.spanAndCount(s, 8, SpanCondition.CONTAINED, count));
        assertEquals("ab+cd count=2 (frozen)", 2, count.value);
        assertEquals("1x abc span[8, 11[ (frozen)", 11,
                ab_cd.spanAndCount(s, 8, SpanCondition.SIMPLE, count));
        assertEquals("1x abc count=1 (frozen)", 1, count.value);
    }
}
