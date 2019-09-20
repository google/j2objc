/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
* Copyright (C) 2010-2014, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* UTF16CollationIterator.java, ported from utf16collationiterator.h/.cpp
*
* C++ version created on: 2010oct27
* created by: Markus W. Scherer
*/

package android.icu.impl.coll;

/**
 * UTF-16 collation element and character iterator.
 * Handles normalized UTF-16 text, with length or NUL-terminated.
 * Unnormalized text is handled by a subclass.
 * @hide Only a subset of ICU is exposed in Android
 */
public class UTF16CollationIterator extends CollationIterator {
    /**
     * Partial constructor, see {@link CollationIterator#CollationIterator(CollationData)}.
     */
    public UTF16CollationIterator(CollationData d) {
        super(d);
    }

    public UTF16CollationIterator(CollationData d, boolean numeric, CharSequence s, int p) {
        super(d, numeric);
        seq = s;
        start = 0;
        pos = p;
        limit = s.length();
    }

    @Override
    public boolean equals(Object other) {
        if(!super.equals(other)) { return false; }
        UTF16CollationIterator o = (UTF16CollationIterator)other;
        // Compare the iterator state but not the text: Assume that the caller does that.
        return (pos - start) == (o.pos - o.start);
    }

    @Override
    public int hashCode() {
        assert false : "hashCode not designed";
        return 42; // any arbitrary constant will do
    }

    @Override
    public void resetToOffset(int newOffset) {
        reset();
        pos = start + newOffset;
    }

    @Override
    public int getOffset() {
        return pos - start;
    }

    public void setText(boolean numeric, CharSequence s, int p) {
        reset(numeric);
        seq = s;
        start = 0;
        pos = p;
        limit = s.length();
    }

    @Override
    public int nextCodePoint() {
        if(pos == limit) {
            return Collation.SENTINEL_CP;
        }
        char c = seq.charAt(pos++);
        char trail;
        if(Character.isHighSurrogate(c) && pos != limit &&
                Character.isLowSurrogate(trail = seq.charAt(pos))) {
            ++pos;
            return Character.toCodePoint(c, trail);
        } else {
            return c;
        }
    }

    @Override
    public int previousCodePoint() {
        if(pos == start) {
            return Collation.SENTINEL_CP;
        }
        char c = seq.charAt(--pos);
        char lead;
        if(Character.isLowSurrogate(c) && pos != start &&
                Character.isHighSurrogate(lead = seq.charAt(pos - 1))) {
            --pos;
            return Character.toCodePoint(lead, c);
        } else {
            return c;
        }
    }

    @Override
    protected long handleNextCE32() {
        if(pos == limit) {
            return NO_CP_AND_CE32;
        }
        char c = seq.charAt(pos++);
        return makeCodePointAndCE32Pair(c, trie.getFromU16SingleLead(c));
    }

    @Override
    protected char handleGetTrailSurrogate() {
        if(pos == limit) { return 0; }
        char trail;
        if(Character.isLowSurrogate(trail = seq.charAt(pos))) { ++pos; }
        return trail;
    }

    /* boolean foundNULTerminator(); */

    @Override
    protected void forwardNumCodePoints(int num) {
        while(num > 0 && pos != limit) {
            char c = seq.charAt(pos++);
            --num;
            if(Character.isHighSurrogate(c) && pos != limit &&
                    Character.isLowSurrogate(seq.charAt(pos))) {
                ++pos;
            }
        }
    }

    @Override
    protected void backwardNumCodePoints(int num) {
        while(num > 0 && pos != start) {
            char c = seq.charAt(--pos);
            --num;
            if(Character.isLowSurrogate(c) && pos != start &&
                    Character.isHighSurrogate(seq.charAt(pos-1))) {
                --pos;
            }
        }
    }

    protected CharSequence seq;
    protected int start;
    protected int pos;
    protected int limit;
}
