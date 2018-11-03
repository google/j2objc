/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
* Copyright (C) 2010-2014, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* FCDUTF16CollationIterator.java, ported from utf16collationiterator.h/.cpp
*
* C++ version created on: 2010oct27
* created by: Markus W. Scherer
*/

package android.icu.impl.coll;

import android.icu.impl.Normalizer2Impl;

/**
 * Incrementally checks the input text for FCD and normalizes where necessary.
 * @hide Only a subset of ICU is exposed in Android
 */
public final class FCDUTF16CollationIterator extends UTF16CollationIterator {
    /**
     * Partial constructor, see {@link CollationIterator#CollationIterator(CollationData)}.
     */
    public FCDUTF16CollationIterator(CollationData d) {
        super(d);
        nfcImpl = d.nfcImpl;
    }

    public FCDUTF16CollationIterator(CollationData data, boolean numeric, CharSequence s, int p) {
        super(data, numeric, s, p);
        rawSeq = s;
        segmentStart = p;
        rawLimit = s.length();
        nfcImpl = data.nfcImpl;
        checkDir = 1;
    }

    @Override
    public boolean equals(Object other) {
        // Skip the UTF16CollationIterator and call its parent.
        if (!(other instanceof CollationIterator)
            || !((CollationIterator)this).equals(other)
            || !(other instanceof FCDUTF16CollationIterator))
        {
            return false;
        }
        FCDUTF16CollationIterator o = (FCDUTF16CollationIterator)other;
        // Compare the iterator state but not the text: Assume that the caller does that.
        if (checkDir != o.checkDir) {
            return false;
        }
        if (checkDir == 0 && (seq == rawSeq) != (o.seq == o.rawSeq)) {
            return false;
        }
        if (checkDir != 0 || seq == rawSeq) {
            return (pos - rawStart) == (o.pos - /*o.*/ rawStart);
        }
        else {
            return (segmentStart - rawStart) == (o.segmentStart - /*o.*/ rawStart) &&
                    (pos - start) == (o.pos - o.start);
        }
    }

    @Override
    public int hashCode() {
        assert false : "hashCode not designed";
        return 42; // any arbitrary constant will do
    }

    @Override
    public void resetToOffset(int newOffset) {
        reset();
        seq = rawSeq;
        start = segmentStart = pos = rawStart + newOffset;
        limit = rawLimit;
        checkDir = 1;
    }

    @Override
    public int getOffset() {
        if(checkDir != 0 || seq == rawSeq) {
            return pos - rawStart;
        } else if(pos == start) {
            return segmentStart - rawStart;
        } else {
            return segmentLimit - rawStart;
        }
    }

    @Override
    public void setText(boolean numeric, CharSequence s, int p) {
        super.setText(numeric, s, p);
        rawSeq = s;
        segmentStart = p;
        rawLimit = limit = s.length();
        checkDir = 1;
    }

    @Override
    public int nextCodePoint() {
        char c;
        for(;;) {
            if(checkDir > 0) {
                if(pos == limit) {
                    return Collation.SENTINEL_CP;
                }
                c = seq.charAt(pos++);
                if(CollationFCD.hasTccc(c)) {
                    if(CollationFCD.maybeTibetanCompositeVowel(c) ||
                            (pos != limit && CollationFCD.hasLccc(seq.charAt(pos)))) {
                        --pos;
                        nextSegment();
                        c = seq.charAt(pos++);
                    }
                }
                break;
            } else if(checkDir == 0 && pos != limit) {
                c = seq.charAt(pos++);
                break;
            } else {
                switchToForward();
            }
        }
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
        char c;
        for(;;) {
            if(checkDir < 0) {
                if(pos == start) {
                    return Collation.SENTINEL_CP;
                }
                c = seq.charAt(--pos);
                if(CollationFCD.hasLccc(c)) {
                    if(CollationFCD.maybeTibetanCompositeVowel(c) ||
                            (pos != start && CollationFCD.hasTccc(seq.charAt(pos - 1)))) {
                        ++pos;
                        previousSegment();
                        c = seq.charAt(--pos);
                    }
                }
                break;
            } else if(checkDir == 0 && pos != start) {
                c = seq.charAt(--pos);
                break;
            } else {
                switchToBackward();
            }
        }
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
        char c;
        for(;;) {
            if(checkDir > 0) {
                if(pos == limit) {
                    return NO_CP_AND_CE32;
                }
                c = seq.charAt(pos++);
                if(CollationFCD.hasTccc(c)) {
                    if(CollationFCD.maybeTibetanCompositeVowel(c) ||
                            (pos != limit && CollationFCD.hasLccc(seq.charAt(pos)))) {
                        --pos;
                        nextSegment();
                        c = seq.charAt(pos++);
                    }
                }
                break;
            } else if(checkDir == 0 && pos != limit) {
                c = seq.charAt(pos++);
                break;
            } else {
                switchToForward();
            }
        }
        return makeCodePointAndCE32Pair(c, trie.getFromU16SingleLead(c));
    }

    /* boolean foundNULTerminator(); */

    @Override
    protected void forwardNumCodePoints(int num) {
        // Specify the class to avoid a virtual-function indirection.
        // In Java, we would declare this class final.
        while(num > 0 && nextCodePoint() >= 0) {
            --num;
        }
    }

    @Override
    protected void backwardNumCodePoints(int num) {
        // Specify the class to avoid a virtual-function indirection.
        // In Java, we would declare this class final.
        while(num > 0 && previousCodePoint() >= 0) {
            --num;
        }
    }

    /**
     * Switches to forward checking if possible.
     * To be called when checkDir < 0 || (checkDir == 0 && pos == limit).
     * Returns with checkDir > 0 || (checkDir == 0 && pos != limit).
     */
    private void switchToForward() {
        assert((checkDir < 0 && seq == rawSeq) || (checkDir == 0 && pos == limit));
        if(checkDir < 0) {
            // Turn around from backward checking.
            start = segmentStart = pos;
            if(pos == segmentLimit) {
                limit = rawLimit;
                checkDir = 1;  // Check forward.
            } else {  // pos < segmentLimit
                checkDir = 0;  // Stay in FCD segment.
            }
        } else {
            // Reached the end of the FCD segment.
            if(seq == rawSeq) {
                // The input text segment is FCD, extend it forward.
            } else {
                // The input text segment needed to be normalized.
                // Switch to checking forward from it.
                seq = rawSeq;
                pos = start = segmentStart = segmentLimit;
                // Note: If this segment is at the end of the input text,
                // then it might help to return false to indicate that, so that
                // we do not have to re-check and normalize when we turn around and go backwards.
                // However, that would complicate the call sites for an optimization of an unusual case.
            }
            limit = rawLimit;
            checkDir = 1;
        }
    }

    /**
     * Extend the FCD text segment forward or normalize around pos.
     * To be called when checkDir > 0 && pos != limit.
     * Returns with checkDir == 0 and pos != limit.
     */
    private void nextSegment() {
        assert(checkDir > 0 && seq == rawSeq && pos != limit);
        // The input text [segmentStart..pos[ passes the FCD check.
        int p = pos;
        int prevCC = 0;
        for(;;) {
            // Fetch the next character's fcd16 value.
            int q = p;
            int c = Character.codePointAt(seq, p);
            p += Character.charCount(c);
            int fcd16 = nfcImpl.getFCD16(c);
            int leadCC = fcd16 >> 8;
            if(leadCC == 0 && q != pos) {
                // FCD boundary before the [q, p[ character.
                limit = segmentLimit = q;
                break;
            }
            if(leadCC != 0 && (prevCC > leadCC || CollationFCD.isFCD16OfTibetanCompositeVowel(fcd16))) {
                // Fails FCD check. Find the next FCD boundary and normalize.
                do {
                    q = p;
                    if(p == rawLimit) { break; }
                    c = Character.codePointAt(seq, p);
                    p += Character.charCount(c);
                } while(nfcImpl.getFCD16(c) > 0xff);
                normalize(pos, q);
                pos = start;
                break;
            }
            prevCC = fcd16 & 0xff;
            if(p == rawLimit || prevCC == 0) {
                // FCD boundary after the last character.
                limit = segmentLimit = p;
                break;
            }
        }
        assert(pos != limit);
        checkDir = 0;
    }

    /**
     * Switches to backward checking.
     * To be called when checkDir > 0 || (checkDir == 0 && pos == start).
     * Returns with checkDir < 0 || (checkDir == 0 && pos != start).
     */
    private void switchToBackward() {
        assert((checkDir > 0 && seq == rawSeq) || (checkDir == 0 && pos == start));
        if(checkDir > 0) {
            // Turn around from forward checking.
            limit = segmentLimit = pos;
            if(pos == segmentStart) {
                start = rawStart;
                checkDir = -1;  // Check backward.
            } else {  // pos > segmentStart
                checkDir = 0;  // Stay in FCD segment.
            }
        } else {
            // Reached the start of the FCD segment.
            if(seq == rawSeq) {
                // The input text segment is FCD, extend it backward.
            } else {
                // The input text segment needed to be normalized.
                // Switch to checking backward from it.
                seq = rawSeq;
                pos = limit = segmentLimit = segmentStart;
            }
            start = rawStart;
            checkDir = -1;
        }
    }

    /**
     * Extend the FCD text segment backward or normalize around pos.
     * To be called when checkDir < 0 && pos != start.
     * Returns with checkDir == 0 and pos != start.
     */
    private void previousSegment() {
        assert(checkDir < 0 && seq == rawSeq && pos != start);
        // The input text [pos..segmentLimit[ passes the FCD check.
        int p = pos;
        int nextCC = 0;
        for(;;) {
            // Fetch the previous character's fcd16 value.
            int q = p;
            int c = Character.codePointBefore(seq, p);
            p -= Character.charCount(c);
            int fcd16 = nfcImpl.getFCD16(c);
            int trailCC = fcd16 & 0xff;
            if(trailCC == 0 && q != pos) {
                // FCD boundary after the [p, q[ character.
                start = segmentStart = q;
                break;
            }
            if(trailCC != 0 && ((nextCC != 0 && trailCC > nextCC) ||
                                CollationFCD.isFCD16OfTibetanCompositeVowel(fcd16))) {
                // Fails FCD check. Find the previous FCD boundary and normalize.
                do {
                    q = p;
                    if(fcd16 <= 0xff || p == rawStart) { break; }
                    c = Character.codePointBefore(seq, p);
                    p -= Character.charCount(c);
                } while((fcd16 = nfcImpl.getFCD16(c)) != 0);
                normalize(q, pos);
                pos = limit;
                break;
            }
            nextCC = fcd16 >> 8;
            if(p == rawStart || nextCC == 0) {
                // FCD boundary before the following character.
                start = segmentStart = p;
                break;
            }
        }
        assert(pos != start);
        checkDir = 0;
    }

    private void normalize(int from, int to) {
        if(normalized == null) {
            normalized = new StringBuilder();
        }
        // NFD without argument checking.
        nfcImpl.decompose(rawSeq, from, to, normalized, to - from);
        // Switch collation processing into the FCD buffer
        // with the result of normalizing [segmentStart, segmentLimit[.
        segmentStart = from;
        segmentLimit = to;
        seq = normalized;
        start = 0;
        limit = start + normalized.length();
    }

    // Text pointers: The input text is rawSeq[rawStart, rawLimit[.
    // (In C++, these are const UChar * pointers.
    // In Java, we use CharSequence rawSeq and the parent class' seq
    // together with int indexes.)
    //
    // checkDir > 0:
    //
    // The input text rawSeq[segmentStart..pos[ passes the FCD check.
    // Moving forward checks incrementally.
    // segmentLimit is undefined. seq == rawSeq. limit == rawLimit.
    //
    // checkDir < 0:
    // The input text rawSeq[pos..segmentLimit[ passes the FCD check.
    // Moving backward checks incrementally.
    // segmentStart is undefined. seq == rawSeq. start == rawStart.
    //
    // checkDir == 0:
    //
    // The input text rawSeq[segmentStart..segmentLimit[ is being processed.
    // These pointers are at FCD boundaries.
    // Either this text segment already passes the FCD check
    // and seq==rawSeq && segmentStart==start<=pos<=limit==segmentLimit,
    // or the current segment had to be normalized so that
    // rawSeq[segmentStart..segmentLimit[ turned into the normalized string,
    // corresponding to seq==normalized && 0==start<=pos<=limit==start+normalized.length().
    private CharSequence rawSeq;
    private static final int rawStart = 0;
    private int segmentStart;
    private int segmentLimit;
    private int rawLimit;

    private final Normalizer2Impl nfcImpl;
    private StringBuilder normalized;
    // Direction of incremental FCD check. See comments before rawStart.
    private int checkDir;
}
