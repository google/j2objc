/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2012, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.util;

import java.util.Iterator;
import java.util.Set;

import android.icu.text.UTF16;

/**
 * UnicodeSetIterator iterates over the contents of a UnicodeSet.  It
 * iterates over either code points or code point ranges.  After all
 * code points or ranges have been returned, it returns the
 * multicharacter strings of the UnicodSet, if any.
 *
 * <p>To iterate over code points, use a loop like this:
 * <pre>
 * UnicodeSetIterator it = new UnicodeSetIterator(set);
 * while (set.next()) {
 *   if (set.codepoint != UnicodeSetIterator.IS_STRING) {
 *     processCodepoint(set.codepoint);
 *   } else {
 *     processString(set.string);
 *   }
 * }
 * </pre>
 *
 * <p>To iterate over code point ranges, use a loop like this:
 * <pre>
 * UnicodeSetIterator it = new UnicodeSetIterator(set);
 * while (set.nextRange()) {
 *   if (set.codepoint != UnicodeSetIterator.IS_STRING) {
 *     processCodepointRange(set.codepoint, set.codepointEnd);
 *   } else {
 *     processString(set.string);
 *   }
 * }
 * </pre>
 * @author M. Davis
 */
public class UnicodeMapIterator<T> {

    /**
     * Value of <tt>codepoint</tt> if the iterator points to a string.
     * If <tt>codepoint == IS_STRING</tt>, then examine
     * <tt>string</tt> for the current iteration result.
     */
    public static int IS_STRING = -1;

    /**
     * Current code point, or the special value <tt>IS_STRING</tt>, if
     * the iterator points to a string.
     */
    public int codepoint;

    /**
     * When iterating over ranges using <tt>nextRange()</tt>,
     * <tt>codepointEnd</tt> contains the inclusive end of the
     * iteration range, if <tt>codepoint != IS_STRING</tt>.  If
     * iterating over code points using <tt>next()</tt>, or if
     * <tt>codepoint == IS_STRING</tt>, then the value of
     * <tt>codepointEnd</tt> is undefined.
     */
    public int codepointEnd;

    /**
     * If <tt>codepoint == IS_STRING</tt>, then <tt>string</tt> points
     * to the current string.  If <tt>codepoint != IS_STRING</tt>, the
     * value of <tt>string</tt> is undefined.
     */
    public String string;

    /**
     * The value associated with this element or range.
     */
    public T value;

    /**
     * Create an iterator over the given set.
     * @param set set to iterate over
     */
    public UnicodeMapIterator(UnicodeMap set) {
        reset(set);
    }

    /**
     * Create an iterator over nothing.  <tt>next()</tt> and
     * <tt>nextRange()</tt> return false. This is a convenience
     * constructor allowing the target to be set later.
     */
    public UnicodeMapIterator() {
        reset(new UnicodeMap());
    }

    /**
     * Returns the next element in the set, either a single code point
     * or a string.  If there are no more elements in the set, return
     * false.  If <tt>codepoint == IS_STRING</tt>, the value is a
     * string in the <tt>string</tt> field.  Otherwise the value is a
     * single code point in the <tt>codepoint</tt> field.
     * 
     * <p>The order of iteration is all code points in sorted order,
     * followed by all strings sorted order.  <tt>codepointEnd</tt> is
     * undefined after calling this method.  <tt>string</tt> is
     * undefined unless <tt>codepoint == IS_STRING</tt>.  Do not mix
     * calls to <tt>next()</tt> and <tt>nextRange()</tt> without
     * calling <tt>reset()</tt> between them.  The results of doing so
     * are undefined.
     *
     * @return true if there was another element in the set and this
     * object contains the element.
     */
    public boolean next() {
        if (nextElement <= endElement) {
            codepoint = codepointEnd = nextElement++;
            return true;
        }
        while (range < endRange) {
            if (loadRange(++range) == null) {
                continue;
            }
            codepoint = codepointEnd = nextElement++;
            return true;
        }

        // stringIterator == null iff there are no string elements remaining

        if (stringIterator == null) return false;
        codepoint = IS_STRING; // signal that value is actually a string
        string = (String)stringIterator.next();
        if (!stringIterator.hasNext()) stringIterator = null;
        return true;
    }

    /**
     * Returns the next element in the set, either a code point range
     * or a string.  If there are no more elements in the set, return
     * false.  If <tt>codepoint == IS_STRING</tt>, the value is a
     * string in the <tt>string</tt> field.  Otherwise the value is a
     * range of one or more code points from <tt>codepoint</tt> to
     * <tt>codepointeEnd</tt> inclusive.
     * 
     * <p>The order of iteration is all code points ranges in sorted
     * order, followed by all strings sorted order.  Ranges are
     * disjoint and non-contiguous.  <tt>string</tt> is undefined
     * unless <tt>codepoint == IS_STRING</tt>.  Do not mix calls to
     * <tt>next()</tt> and <tt>nextRange()</tt> without calling
     * <tt>reset()</tt> between them.  The results of doing so are
     * undefined.
     *
     * @return true if there was another element in the set and this
     * object contains the element.
     */
    public boolean nextRange() {
        if (nextElement <= endElement) {
            codepointEnd = endElement;
            codepoint = nextElement;
            nextElement = endElement+1;
            return true;
        }
        while (range < endRange) {
            if (loadRange(++range) == null) {
                continue;
            }
            codepointEnd = endElement;
            codepoint = nextElement;
            nextElement = endElement+1;
            return true;
        }

        // stringIterator == null iff there are no string elements remaining

        if (stringIterator == null) return false;
        codepoint = IS_STRING; // signal that value is actually a string
        string = (String)stringIterator.next();
        if (!stringIterator.hasNext()) stringIterator = null;
        return true;
    }

    /**
     * Sets this iterator to visit the elements of the given set and
     * resets it to the start of that set.  The iterator is valid only
     * so long as <tt>set</tt> is valid.
     * @param set the set to iterate over.
     */
    public void reset(UnicodeMap set) {
        this.map = set;
        reset();
    }

    /**
     * Resets this iterator to the start of the set.
     * @return 
     */
    public UnicodeMapIterator<T> reset() {
        endRange = map.getRangeCount() - 1;
        // both next*() methods will test: if (nextElement <= endElement)
        // we set them to fail this test, which will cause them to load the first range
        nextElement = 0; 
        endElement = -1;
        range = -1;

        stringIterator = null;
        Set<String> strings = map.getNonRangeStrings();
        if (strings != null) {
            stringIterator = strings.iterator();
            if (!stringIterator.hasNext()) stringIterator = null;
        }
        value = null;
        return this;
    }

    /**
     * Gets the current string from the iterator. Only use after calling next(), not nextRange().
     */
    public String getString() {
        if (codepoint != IS_STRING) {
            return UTF16.valueOf(codepoint);
        }
        return string;
    }

    // ======================= PRIVATES ===========================

    private UnicodeMap<T> map;
    private int endRange = 0;
    private int range = 0;
    private Iterator<String> stringIterator = null;
    protected int endElement;
    protected int nextElement;

    /*
     * Invariant: stringIterator is null when there are no (more) strings remaining
     */

    protected T loadRange(int range) {
        nextElement = map.getRangeStart(range);
        endElement = map.getRangeEnd(range);
        value = map.getRangeValue(range);
        return value;
    }
}
