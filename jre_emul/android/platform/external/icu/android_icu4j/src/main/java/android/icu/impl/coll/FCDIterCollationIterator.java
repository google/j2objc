/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
* Copyright (C) 2012-2014, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* FCDIterCollationIterator.java, ported from uitercollationiterator.h/.cpp
*
* C++ version created on: 2012sep23 (from utf16collationiterator.h)
* created by: Markus W. Scherer
*/

package android.icu.impl.coll;

import android.icu.impl.Normalizer2Impl;
import android.icu.text.UCharacterIterator;

/**
 * Incrementally checks the input text for FCD and normalizes where necessary.
 * @hide Only a subset of ICU is exposed in Android
 */
public final class FCDIterCollationIterator extends IterCollationIterator {
    public FCDIterCollationIterator(CollationData data, boolean numeric,
            UCharacterIterator ui, int startIndex) {
        super(data, numeric, ui);
        state = State.ITER_CHECK_FWD;
        start = startIndex;
        nfcImpl = data.nfcImpl;
    }

    @Override
    public void resetToOffset(int newOffset) {
        super.resetToOffset(newOffset);
        start = newOffset;
        state = State.ITER_CHECK_FWD;
    }

    @Override
    public int getOffset() {
        if(state.compareTo(State.ITER_CHECK_BWD) <= 0) {
            return iter.getIndex();
        } else if(state == State.ITER_IN_FCD_SEGMENT) {
            return pos;
        } else if(pos == 0) {
            return start;
        } else {
            return limit;
        }
    }

    @Override
    public int nextCodePoint() {
        int c;
        for(;;) {
            if(state == State.ITER_CHECK_FWD) {
                c = iter.next();
                if(c < 0) {
                    return c;
                }
                if(CollationFCD.hasTccc(c)) {
                    if(CollationFCD.maybeTibetanCompositeVowel(c) ||
                            CollationFCD.hasLccc(iter.current())) {
                        iter.previous();
                        if(!nextSegment()) {
                            return Collation.SENTINEL_CP;
                        }
                        continue;
                    }
                }
                if(isLeadSurrogate(c)) {
                    int trail = iter.next();
                    if(isTrailSurrogate(trail)) {
                        return Character.toCodePoint((char)c, (char)trail);
                    } else if(trail >= 0) {
                        iter.previous();
                    }
                }
                return c;
            } else if(state == State.ITER_IN_FCD_SEGMENT && pos != limit) {
                c = iter.nextCodePoint();
                pos += Character.charCount(c);
                assert(c >= 0);
                return c;
            } else if(state.compareTo(State.IN_NORM_ITER_AT_LIMIT) >= 0 &&
                    pos != normalized.length()) {
                c = normalized.codePointAt(pos);
                pos += Character.charCount(c);
                return c;
            } else {
                switchToForward();
            }
        }
    }

    @Override
    public int previousCodePoint() {
        int c;
        for(;;) {
            if(state == State.ITER_CHECK_BWD) {
                c = iter.previous();
                if(c < 0) {
                    start = pos = 0;
                    state = State.ITER_IN_FCD_SEGMENT;
                    return Collation.SENTINEL_CP;
                }
                if(CollationFCD.hasLccc(c)) {
                    int prev = Collation.SENTINEL_CP;
                    if(CollationFCD.maybeTibetanCompositeVowel(c) ||
                            CollationFCD.hasTccc(prev = iter.previous())) {
                        iter.next();
                        if(prev >= 0) {
                            iter.next();
                        }
                        if(!previousSegment()) {
                            return Collation.SENTINEL_CP;
                        }
                        continue;
                    }
                    // hasLccc(trail)=true for all trail surrogates
                    if(isTrailSurrogate(c)) {
                        if(prev < 0) {
                            prev = iter.previous();
                        }
                        if(isLeadSurrogate(prev)) {
                            return Character.toCodePoint((char)prev, (char)c);
                        }
                    }
                    if(prev >= 0) {
                        iter.next();
                    }
                }
                return c;
            } else if(state == State.ITER_IN_FCD_SEGMENT && pos != start) {
                c = iter.previousCodePoint();
                pos -= Character.charCount(c);
                assert(c >= 0);
                return c;
            } else if(state.compareTo(State.IN_NORM_ITER_AT_LIMIT) >= 0 && pos != 0) {
                c = normalized.codePointBefore(pos);
                pos -= Character.charCount(c);
                return c;
            } else {
                switchToBackward();
            }
        }
    }

    @Override
    protected long handleNextCE32() {
        int c;
        for(;;) {
            if(state == State.ITER_CHECK_FWD) {
                c = iter.next();
                if(c < 0) {
                    return NO_CP_AND_CE32;
                }
                if(CollationFCD.hasTccc(c)) {
                    if(CollationFCD.maybeTibetanCompositeVowel(c) ||
                            CollationFCD.hasLccc(iter.current())) {
                        iter.previous();
                        if(!nextSegment()) {
                            c = Collation.SENTINEL_CP;
                            return Collation.FALLBACK_CE32;
                        }
                        continue;
                    }
                }
                break;
            } else if(state == State.ITER_IN_FCD_SEGMENT && pos != limit) {
                c = iter.next();
                ++pos;
                assert(c >= 0);
                break;
            } else if(state.compareTo(State.IN_NORM_ITER_AT_LIMIT) >= 0 &&
                    pos != normalized.length()) {
                c = normalized.charAt(pos++);
                break;
            } else {
                switchToForward();
            }
        }
        return makeCodePointAndCE32Pair(c, trie.getFromU16SingleLead((char)c));
    }

    @Override
    protected char handleGetTrailSurrogate() {
        if(state.compareTo(State.ITER_IN_FCD_SEGMENT) <= 0) {
            int trail = iter.next();
            if(isTrailSurrogate(trail)) {
                if(state == State.ITER_IN_FCD_SEGMENT) { ++pos; }
            } else if(trail >= 0) {
                iter.previous();
            }
            return (char)trail;
        } else {
            assert(pos < normalized.length());
            char trail;
            if(Character.isLowSurrogate(trail = normalized.charAt(pos))) { ++pos; }
            return trail;
        }
    }

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
     */
    private void switchToForward() {
        assert(state == State.ITER_CHECK_BWD ||
                (state == State.ITER_IN_FCD_SEGMENT && pos == limit) ||
                (state.compareTo(State.IN_NORM_ITER_AT_LIMIT) >= 0 && pos == normalized.length()));
        if(state == State.ITER_CHECK_BWD) {
            // Turn around from backward checking.
            start = pos = iter.getIndex();
            if(pos == limit) {
                state = State.ITER_CHECK_FWD;  // Check forward.
            } else {  // pos < limit
                state = State.ITER_IN_FCD_SEGMENT;  // Stay in FCD segment.
            }
        } else {
            // Reached the end of the FCD segment.
            if(state == State.ITER_IN_FCD_SEGMENT) {
                // The input text segment is FCD, extend it forward.
            } else {
                // The input text segment needed to be normalized.
                // Switch to checking forward from it.
                if(state == State.IN_NORM_ITER_AT_START) {
                    iter.moveIndex(limit - start);
                }
                start = limit;
            }
            state = State.ITER_CHECK_FWD;
        }
    }

    /**
     * Extends the FCD text segment forward or normalizes around pos.
     * @return true if success
     */
    private boolean nextSegment() {
        assert(state == State.ITER_CHECK_FWD);
        // The input text [start..(iter index)[ passes the FCD check.
        pos = iter.getIndex();
        // Collect the characters being checked, in case they need to be normalized.
        if(s == null) {
            s = new StringBuilder();
        } else {
            s.setLength(0);
        }
        int prevCC = 0;
        for(;;) {
            // Fetch the next character and its fcd16 value.
            int c = iter.nextCodePoint();
            if(c < 0) { break; }
            int fcd16 = nfcImpl.getFCD16(c);
            int leadCC = fcd16 >> 8;
            if(leadCC == 0 && s.length() != 0) {
                // FCD boundary before this character.
                iter.previousCodePoint();
                break;
            }
            s.appendCodePoint(c);
            if(leadCC != 0 && (prevCC > leadCC || CollationFCD.isFCD16OfTibetanCompositeVowel(fcd16))) {
                // Fails FCD check. Find the next FCD boundary and normalize.
                for(;;) {
                    c = iter.nextCodePoint();
                    if(c < 0) { break; }
                    if(nfcImpl.getFCD16(c) <= 0xff) {
                        iter.previousCodePoint();
                        break;
                    }
                    s.appendCodePoint(c);
                }
                normalize(s);
                start = pos;
                limit = pos + s.length();
                state = State.IN_NORM_ITER_AT_LIMIT;
                pos = 0;
                return true;
            }
            prevCC = fcd16 & 0xff;
            if(prevCC == 0) {
                // FCD boundary after the last character.
                break;
            }
        }
        limit = pos + s.length();
        assert(pos != limit);
        iter.moveIndex(-s.length());
        state = State.ITER_IN_FCD_SEGMENT;
        return true;
    }

    /**
     * Switches to backward checking.
     */
    private void switchToBackward() {
        assert(state == State.ITER_CHECK_FWD ||
                (state == State.ITER_IN_FCD_SEGMENT && pos == start) ||
                (state.compareTo(State.IN_NORM_ITER_AT_LIMIT) >= 0 && pos == 0));
        if(state == State.ITER_CHECK_FWD) {
            // Turn around from forward checking.
            limit = pos = iter.getIndex();
            if(pos == start) {
                state = State.ITER_CHECK_BWD;  // Check backward.
            } else {  // pos > start
                state = State.ITER_IN_FCD_SEGMENT;  // Stay in FCD segment.
            }
        } else {
            // Reached the start of the FCD segment.
            if(state == State.ITER_IN_FCD_SEGMENT) {
                // The input text segment is FCD, extend it backward.
            } else {
                // The input text segment needed to be normalized.
                // Switch to checking backward from it.
                if(state == State.IN_NORM_ITER_AT_LIMIT) {
                    iter.moveIndex(start - limit);
                }
                limit = start;
            }
            state = State.ITER_CHECK_BWD;
        }
    }

    /**
     * Extends the FCD text segment backward or normalizes around pos.
     * @return true if success
     */
    private boolean previousSegment() {
        assert(state == State.ITER_CHECK_BWD);
        // The input text [(iter index)..limit[ passes the FCD check.
        pos = iter.getIndex();
        // Collect the characters being checked, in case they need to be normalized.
        if(s == null) {
            s = new StringBuilder();
        } else {
            s.setLength(0);
        }
        int nextCC = 0;
        for(;;) {
            // Fetch the previous character and its fcd16 value.
            int c = iter.previousCodePoint();
            if(c < 0) { break; }
            int fcd16 = nfcImpl.getFCD16(c);
            int trailCC = fcd16 & 0xff;
            if(trailCC == 0 && s.length() != 0) {
                // FCD boundary after this character.
                iter.nextCodePoint();
                break;
            }
            s.appendCodePoint(c);
            if(trailCC != 0 && ((nextCC != 0 && trailCC > nextCC) ||
                                CollationFCD.isFCD16OfTibetanCompositeVowel(fcd16))) {
                // Fails FCD check. Find the previous FCD boundary and normalize.
                while(fcd16 > 0xff) {
                    c = iter.previousCodePoint();
                    if(c < 0) { break; }
                    fcd16 = nfcImpl.getFCD16(c);
                    if(fcd16 == 0) {
                        iter.nextCodePoint();
                        break;
                    }
                    s.appendCodePoint(c);
                }
                s.reverse();
                normalize(s);
                limit = pos;
                start = pos - s.length();
                state = State.IN_NORM_ITER_AT_START;
                pos = normalized.length();
                return true;
            }
            nextCC = fcd16 >> 8;
            if(nextCC == 0) {
                // FCD boundary before the following character.
                break;
            }
        }
        start = pos - s.length();
        assert(pos != start);
        iter.moveIndex(s.length());
        state = State.ITER_IN_FCD_SEGMENT;
        return true;
    }

    private void normalize(CharSequence s) {
        if(normalized == null) {
            normalized = new StringBuilder();
        }
        // NFD without argument checking.
        nfcImpl.decompose(s, normalized);
    }

    private enum State {
        /**
         * The input text [start..(iter index)[ passes the FCD check.
         * Moving forward checks incrementally.
         * pos & limit are undefined.
         */
        ITER_CHECK_FWD,
        /**
         * The input text [(iter index)..limit[ passes the FCD check.
         * Moving backward checks incrementally.
         * start & pos are undefined.
         */
        ITER_CHECK_BWD,
        /**
         * The input text [start..limit[ passes the FCD check.
         * pos tracks the current text index.
         */
        ITER_IN_FCD_SEGMENT,
        /**
         * The input text [start..limit[ failed the FCD check and was normalized.
         * pos tracks the current index in the normalized string.
         * The text iterator is at the limit index.
         */
        IN_NORM_ITER_AT_LIMIT,
        /**
         * The input text [start..limit[ failed the FCD check and was normalized.
         * pos tracks the current index in the normalized string.
         * The text iterator is at the start index.
         */
        IN_NORM_ITER_AT_START
    }

    private State state;

    private int start;
    private int pos;
    private int limit;

    private final Normalizer2Impl nfcImpl;
    private StringBuilder s;
    private StringBuilder normalized;
}
