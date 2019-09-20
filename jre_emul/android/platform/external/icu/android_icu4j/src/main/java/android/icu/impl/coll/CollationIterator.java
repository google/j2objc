/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
* Copyright (C) 2010-2014, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* CollationIterator.java, ported from collationiterator.h/.cpp
*
* C++ version created on: 2010oct27
* created by: Markus W. Scherer
*/

package android.icu.impl.coll;

import android.icu.impl.Normalizer2Impl.Hangul;
import android.icu.impl.Trie2_32;
import android.icu.util.BytesTrie;
import android.icu.util.CharsTrie;
import android.icu.util.ICUException;

/**
 * Collation element iterator and abstract character iterator.
 *
 * When a method returns a code point value, it must be in 0..10FFFF,
 * except it can be negative as a sentinel value.
 * @hide Only a subset of ICU is exposed in Android
 */
public abstract class CollationIterator {
    private static final class CEBuffer {
        /** Large enough for CEs of most short strings. */
        private static final int INITIAL_CAPACITY = 40;

        CEBuffer() {}

        void append(long ce) {
            if(length >= INITIAL_CAPACITY) {
                ensureAppendCapacity(1);
            }
            buffer[length++] = ce;
        }

        void appendUnsafe(long ce) {
            buffer[length++] = ce;
        }

        void ensureAppendCapacity(int appCap) {
            int capacity = buffer.length;
            if((length + appCap) <= capacity) { return; }
            do {
                if(capacity < 1000) {
                    capacity *= 4;
                } else {
                    capacity *= 2;
                }
            } while(capacity < (length + appCap));
            long[] newBuffer = new long[capacity];
            System.arraycopy(buffer, 0, newBuffer, 0, length);
            buffer = newBuffer;
        }

        void incLength() {
            // Use INITIAL_CAPACITY for a very simple fastpath.
            // (Rather than buffer.getCapacity().)
            if(length >= INITIAL_CAPACITY) {
                ensureAppendCapacity(1);
            }
            ++length;
        }

        long set(int i, long ce) {
            return buffer[i] = ce;
        }
        long get(int i) { return buffer[i]; }

        long[] getCEs() { return buffer; }

        int length = 0;

        private long[] buffer = new long[INITIAL_CAPACITY];
    }

    // State of combining marks skipped in discontiguous contraction.
    // We create a state object on first use and keep it around deactivated between uses.
    private static final class SkippedState {
        // Born active but empty.
        SkippedState() {}
        void clear() {
            oldBuffer.setLength(0);
            pos = 0;
            // The newBuffer is reset by setFirstSkipped().
        }

        boolean isEmpty() { return oldBuffer.length() == 0; }

        boolean hasNext() { return pos < oldBuffer.length(); }

        // Requires hasNext().
        int next() {
            int c = oldBuffer.codePointAt(pos);
            pos += Character.charCount(c);
            return c;
        }

        // Accounts for one more input code point read beyond the end of the marks buffer.
        void incBeyond() {
            assert(!hasNext());
            ++pos;
        }

        // Goes backward through the skipped-marks buffer.
        // Returns the number of code points read beyond the skipped marks
        // that need to be backtracked through normal input.
        int backwardNumCodePoints(int n) {
            int length = oldBuffer.length();
            int beyond = pos - length;
            if(beyond > 0) {
                if(beyond >= n) {
                    // Not back far enough to re-enter the oldBuffer.
                    pos -= n;
                    return n;
                } else {
                    // Back out all beyond-oldBuffer code points and re-enter the buffer.
                    pos = oldBuffer.offsetByCodePoints(length, beyond - n);
                    return beyond;
                }
            } else {
                // Go backwards from inside the oldBuffer.
                pos = oldBuffer.offsetByCodePoints(pos, -n);
                return 0;
            }
        }

        void setFirstSkipped(int c) {
            skipLengthAtMatch = 0;
            newBuffer.setLength(0);
            newBuffer.appendCodePoint(c);
        }

        void skip(int c) {
            newBuffer.appendCodePoint(c);
        }

        void recordMatch() { skipLengthAtMatch = newBuffer.length(); }

        // Replaces the characters we consumed with the newly skipped ones.
        void replaceMatch() {
            // Note: UnicodeString.replace() pins pos to at most length().
            int oldLength = oldBuffer.length();
            if(pos > oldLength) { pos = oldLength; }
            oldBuffer.delete(0, pos).insert(0, newBuffer, 0, skipLengthAtMatch);
            pos = 0;
        }

        void saveTrieState(CharsTrie trie) { trie.saveState(state); }
        void resetToTrieState(CharsTrie trie) { trie.resetToState(state); }

        // Combining marks skipped in previous discontiguous-contraction matching.
        // After that discontiguous contraction was completed, we start reading them from here.
        private final StringBuilder oldBuffer = new StringBuilder();
        // Combining marks newly skipped in current discontiguous-contraction matching.
        // These might have been read from the normal text or from the oldBuffer.
        private final StringBuilder newBuffer = new StringBuilder();
        // Reading index in oldBuffer,
        // or counter for how many code points have been read beyond oldBuffer (pos-oldBuffer.length()).
        private int pos;
        // newBuffer.length() at the time of the last matching character.
        // When a partial match fails, we back out skipped and partial-matching input characters.
        private int skipLengthAtMatch;
        // We save the trie state before we attempt to match a character,
        // so that we can skip it and try the next one.
        private CharsTrie.State state = new CharsTrie.State();
    };

    /**
     * Partially constructs the iterator.
     * In Java, we cache partially constructed iterators
     * and finish their setup when starting to work on text
     * (via reset(boolean) and the setText(numeric, ...) methods of subclasses).
     * This avoids memory allocations for iterators that remain unused.
     *
     * <p>In C++, there is only one constructor, and iterators are
     * stack-allocated as needed.
     */
    public CollationIterator(CollationData d) {
        trie = d.trie;
        data = d;
        numCpFwd = -1;
        isNumeric = false;
        ceBuffer = null;
    }

    public CollationIterator(CollationData d, boolean numeric) {
        trie = d.trie;
        data = d;
        numCpFwd = -1;
        isNumeric = numeric;
        ceBuffer = new CEBuffer();
    }

    @Override
    public boolean equals(Object other) {
        // Subclasses: Call this method and then add more specific checks.
        // Compare the iterator state but not the collation data (trie & data fields):
        // Assume that the caller compares the data.
        // Ignore skipped since that should be unused between calls to nextCE().
        // (It only stays around to avoid another memory allocation.)
        if(other == null) { return false; }
        if(!this.getClass().equals(other.getClass())) { return false; }
        CollationIterator o = (CollationIterator)other;
        if(!(ceBuffer.length == o.ceBuffer.length &&
                cesIndex == o.cesIndex &&
                numCpFwd == o.numCpFwd &&
                isNumeric == o.isNumeric)) {
            return false;
        }
        for(int i = 0; i < ceBuffer.length; ++i) {
            if(ceBuffer.get(i) != o.ceBuffer.get(i)) { return false; }
        }
        return true;
    }

    @Override
    public int hashCode() {
        // Dummy return to prevent compile warnings.
        return 0;
    }

    /**
     * Resets the iterator state and sets the position to the specified offset.
     * Subclasses must implement, and must call the parent class method,
     * or CollationIterator.reset().
     */
    public abstract void resetToOffset(int newOffset);

    public abstract int getOffset();

    /**
     * Returns the next collation element.
     */
    public final long nextCE() {
        if(cesIndex < ceBuffer.length) {
            // Return the next buffered CE.
            return ceBuffer.get(cesIndex++);
        }
        assert cesIndex == ceBuffer.length;
        ceBuffer.incLength();
        long cAndCE32 = handleNextCE32();
        int c = (int)(cAndCE32 >> 32);
        int ce32 = (int)cAndCE32;
        int t = ce32 & 0xff;
        if(t < Collation.SPECIAL_CE32_LOW_BYTE) {  // Forced-inline of isSpecialCE32(ce32).
            // Normal CE from the main data.
            // Forced-inline of ceFromSimpleCE32(ce32).
            return ceBuffer.set(cesIndex++,
                    ((long)(ce32 & 0xffff0000) << 32) | ((long)(ce32 & 0xff00) << 16) | (t << 8));
        }
        CollationData d;
        // The compiler should be able to optimize the previous and the following
        // comparisons of t with the same constant.
        if(t == Collation.SPECIAL_CE32_LOW_BYTE) {
            if(c < 0) {
                return ceBuffer.set(cesIndex++, Collation.NO_CE);
            }
            d = data.base;
            ce32 = d.getCE32(c);
            t = ce32 & 0xff;
            if(t < Collation.SPECIAL_CE32_LOW_BYTE) {
                // Normal CE from the base data.
                return ceBuffer.set(cesIndex++,
                        ((long)(ce32 & 0xffff0000) << 32) | ((long)(ce32 & 0xff00) << 16) | (t << 8));
            }
        } else {
            d = data;
        }
        if(t == Collation.LONG_PRIMARY_CE32_LOW_BYTE) {
            // Forced-inline of ceFromLongPrimaryCE32(ce32).
            return ceBuffer.set(cesIndex++,
                    ((long)(ce32 - t) << 32) | Collation.COMMON_SEC_AND_TER_CE);
        }
        return nextCEFromCE32(d, c, ce32);
    }

    /**
     * Fetches all CEs.
     * @return getCEsLength()
     */
    public final int fetchCEs() {
        while(nextCE() != Collation.NO_CE) {
            // No need to loop for each expansion CE.
            cesIndex = ceBuffer.length;
        }
        return ceBuffer.length;
    }

    /**
     * Overwrites the current CE (the last one returned by nextCE()).
     */
    final void setCurrentCE(long ce) {
        assert cesIndex > 0;
        ceBuffer.set(cesIndex - 1, ce);
    }

    /**
     * Returns the previous collation element.
     */
    public final long previousCE(UVector32 offsets) {
        if(ceBuffer.length > 0) {
            // Return the previous buffered CE.
            return ceBuffer.get(--ceBuffer.length);
        }
        offsets.removeAllElements();
        int limitOffset = getOffset();
        int c = previousCodePoint();
        if(c < 0) { return Collation.NO_CE; }
        if(data.isUnsafeBackward(c, isNumeric)) {
            return previousCEUnsafe(c, offsets);
        }
        // Simple, safe-backwards iteration:
        // Get a CE going backwards, handle prefixes but no contractions.
        int ce32 = data.getCE32(c);
        CollationData d;
        if(ce32 == Collation.FALLBACK_CE32) {
            d = data.base;
            ce32 = d.getCE32(c);
        } else {
            d = data;
        }
        if(Collation.isSimpleOrLongCE32(ce32)) {
            return Collation.ceFromCE32(ce32);
        }
        appendCEsFromCE32(d, c, ce32, false);
        if(ceBuffer.length > 1) {
            offsets.addElement(getOffset());
            // For an expansion, the offset of each non-initial CE is the limit offset,
            // consistent with forward iteration.
            while(offsets.size() <= ceBuffer.length) {
                offsets.addElement(limitOffset);
            };
        }
        return ceBuffer.get(--ceBuffer.length);
    }

    public final int getCEsLength() {
        return ceBuffer.length;
    }

    public final long getCE(int i) {
        return ceBuffer.get(i);
    }

    public final long[] getCEs() {
        return ceBuffer.getCEs();
    }

    final void clearCEs() {
        cesIndex = ceBuffer.length = 0;
    }

    public final void clearCEsIfNoneRemaining() {
        if(cesIndex == ceBuffer.length) { clearCEs(); }
    }

    /**
     * Returns the next code point (with post-increment).
     * Public for identical-level comparison and for testing.
     */
    public abstract int nextCodePoint();

    /**
     * Returns the previous code point (with pre-decrement).
     * Public for identical-level comparison and for testing.
     */
    public abstract int previousCodePoint();

    protected final void reset() {
        cesIndex = ceBuffer.length = 0;
        if(skipped != null) { skipped.clear(); }
    }
    /**
     * Resets the state as well as the numeric setting,
     * and completes the initialization.
     * Only exists in Java where we reset cached CollationIterator instances
     * rather than stack-allocating temporary ones.
     * (See also the constructor comments.)
     */
    protected final void reset(boolean numeric) {
        if(ceBuffer == null) {
            ceBuffer = new CEBuffer();
        }
        reset();
        isNumeric = numeric;
    }

    /**
     * Returns the next code point and its local CE32 value.
     * Returns Collation.FALLBACK_CE32 at the end of the text (c<0)
     * or when c's CE32 value is to be looked up in the base data (fallback).
     *
     * The code point is used for fallbacks, context and implicit weights.
     * It is ignored when the returned CE32 is not special (e.g., FFFD_CE32).
     *
     * Returns the code point in bits 63..32 (signed) and the CE32 in bits 31..0.
     */
    protected long handleNextCE32() {
        int c = nextCodePoint();
        if(c < 0) { return NO_CP_AND_CE32; }
        return makeCodePointAndCE32Pair(c, data.getCE32(c));
    }
    protected long makeCodePointAndCE32Pair(int c, int ce32) {
        return ((long)c << 32) | (ce32 & 0xffffffffL);
    }
    protected static final long NO_CP_AND_CE32 = (-1L << 32) | (Collation.FALLBACK_CE32 & 0xffffffffL);

    /**
     * Called when handleNextCE32() returns a LEAD_SURROGATE_TAG for a lead surrogate code unit.
     * Returns the trail surrogate in that case and advances past it,
     * if a trail surrogate follows the lead surrogate.
     * Otherwise returns any other code unit and does not advance.
     */
    protected char handleGetTrailSurrogate() {
        return 0;
    }

    /**
     * Called when handleNextCE32() returns with c==0, to see whether it is a NUL terminator.
     * (Not needed in Java.)
     */
    /*protected boolean foundNULTerminator() {
        return false;
    }*/

    /**
     * @return false if surrogate code points U+D800..U+DFFF
     *         map to their own implicit primary weights (for UTF-16),
     *         or true if they map to CE(U+FFFD) (for UTF-8)
     */
    protected boolean forbidSurrogateCodePoints() {
        return false;
    }

    protected abstract void forwardNumCodePoints(int num);

    protected abstract void backwardNumCodePoints(int num);

    /**
     * Returns the CE32 from the data trie.
     * Normally the same as data.getCE32(), but overridden in the builder.
     * Call this only when the faster data.getCE32() cannot be used.
     */
    protected int getDataCE32(int c) {
        return data.getCE32(c);
    }

    protected int getCE32FromBuilderData(int ce32) {
        throw new ICUException("internal program error: should be unreachable");
    }

    protected final void appendCEsFromCE32(CollationData d, int c, int ce32,
                           boolean forward) {
        while(Collation.isSpecialCE32(ce32)) {
            switch(Collation.tagFromCE32(ce32)) {
            case Collation.FALLBACK_TAG:
            case Collation.RESERVED_TAG_3:
                throw new ICUException("internal program error: should be unreachable");
            case Collation.LONG_PRIMARY_TAG:
                ceBuffer.append(Collation.ceFromLongPrimaryCE32(ce32));
                return;
            case Collation.LONG_SECONDARY_TAG:
                ceBuffer.append(Collation.ceFromLongSecondaryCE32(ce32));
                return;
            case Collation.LATIN_EXPANSION_TAG:
                ceBuffer.ensureAppendCapacity(2);
                ceBuffer.set(ceBuffer.length, Collation.latinCE0FromCE32(ce32));
                ceBuffer.set(ceBuffer.length + 1, Collation.latinCE1FromCE32(ce32));
                ceBuffer.length += 2;
                return;
            case Collation.EXPANSION32_TAG: {
                int index = Collation.indexFromCE32(ce32);
                int length = Collation.lengthFromCE32(ce32);
                ceBuffer.ensureAppendCapacity(length);
                do {
                    ceBuffer.appendUnsafe(Collation.ceFromCE32(d.ce32s[index++]));
                } while(--length > 0);
                return;
            }
            case Collation.EXPANSION_TAG: {
                int index = Collation.indexFromCE32(ce32);
                int length = Collation.lengthFromCE32(ce32);
                ceBuffer.ensureAppendCapacity(length);
                do {
                    ceBuffer.appendUnsafe(d.ces[index++]);
                } while(--length > 0);
                return;
            }
            case Collation.BUILDER_DATA_TAG:
                ce32 = getCE32FromBuilderData(ce32);
                if(ce32 == Collation.FALLBACK_CE32) {
                    d = data.base;
                    ce32 = d.getCE32(c);
                }
                break;
            case Collation.PREFIX_TAG:
                if(forward) { backwardNumCodePoints(1); }
                ce32 = getCE32FromPrefix(d, ce32);
                if(forward) { forwardNumCodePoints(1); }
                break;
            case Collation.CONTRACTION_TAG: {
                int index = Collation.indexFromCE32(ce32);
                int defaultCE32 = d.getCE32FromContexts(index);  // Default if no suffix match.
                if(!forward) {
                    // Backward contractions are handled by previousCEUnsafe().
                    // c has contractions but they were not found.
                    ce32 = defaultCE32;
                    break;
                }
                int nextCp;
                if(skipped == null && numCpFwd < 0) {
                    // Some portion of nextCE32FromContraction() pulled out here as an ASCII fast path,
                    // avoiding the function call and the nextSkippedCodePoint() overhead.
                    nextCp = nextCodePoint();
                    if(nextCp < 0) {
                        // No more text.
                        ce32 = defaultCE32;
                        break;
                    } else if((ce32 & Collation.CONTRACT_NEXT_CCC) != 0 &&
                            !CollationFCD.mayHaveLccc(nextCp)) {
                        // All contraction suffixes start with characters with lccc!=0
                        // but the next code point has lccc==0.
                        backwardNumCodePoints(1);
                        ce32 = defaultCE32;
                        break;
                    }
                } else {
                    nextCp = nextSkippedCodePoint();
                    if(nextCp < 0) {
                        // No more text.
                        ce32 = defaultCE32;
                        break;
                    } else if((ce32 & Collation.CONTRACT_NEXT_CCC) != 0 &&
                            !CollationFCD.mayHaveLccc(nextCp)) {
                        // All contraction suffixes start with characters with lccc!=0
                        // but the next code point has lccc==0.
                        backwardNumSkipped(1);
                        ce32 = defaultCE32;
                        break;
                    }
                }
                ce32 = nextCE32FromContraction(d, ce32, d.contexts, index + 2, defaultCE32, nextCp);
                if(ce32 == Collation.NO_CE32) {
                    // CEs from a discontiguous contraction plus the skipped combining marks
                    // have been appended already.
                    return;
                }
                break;
            }
            case Collation.DIGIT_TAG:
                if(isNumeric) {
                    appendNumericCEs(ce32, forward);
                    return;
                } else {
                    // Fetch the non-numeric-collation CE32 and continue.
                    ce32 = d.ce32s[Collation.indexFromCE32(ce32)];
                    break;
                }
            case Collation.U0000_TAG:
                assert(c == 0);
                // NUL-terminated input not supported in Java.
                // Fetch the normal ce32 for U+0000 and continue.
                ce32 = d.ce32s[0];
                break;
            case Collation.HANGUL_TAG: {
                int[] jamoCE32s = d.jamoCE32s;
                c -= Hangul.HANGUL_BASE;
                int t = c % Hangul.JAMO_T_COUNT;
                c /= Hangul.JAMO_T_COUNT;
                int v = c % Hangul.JAMO_V_COUNT;
                c /= Hangul.JAMO_V_COUNT;
                if((ce32 & Collation.HANGUL_NO_SPECIAL_JAMO) != 0) {
                    // None of the Jamo CE32s are isSpecialCE32().
                    // Avoid recursive function calls and per-Jamo tests.
                    ceBuffer.ensureAppendCapacity(t == 0 ? 2 : 3);
                    ceBuffer.set(ceBuffer.length, Collation.ceFromCE32(jamoCE32s[c]));
                    ceBuffer.set(ceBuffer.length + 1, Collation.ceFromCE32(jamoCE32s[19 + v]));
                    ceBuffer.length += 2;
                    if(t != 0) {
                        ceBuffer.appendUnsafe(Collation.ceFromCE32(jamoCE32s[39 + t]));
                    }
                    return;
                } else {
                    // We should not need to compute each Jamo code point.
                    // In particular, there should be no offset or implicit ce32.
                    appendCEsFromCE32(d, Collation.SENTINEL_CP, jamoCE32s[c], forward);
                    appendCEsFromCE32(d, Collation.SENTINEL_CP, jamoCE32s[19 + v], forward);
                    if(t == 0) { return; }
                    // offset 39 = 19 + 21 - 1:
                    // 19 = JAMO_L_COUNT
                    // 21 = JAMO_T_COUNT
                    // -1 = omit t==0
                    ce32 = jamoCE32s[39 + t];
                    c = Collation.SENTINEL_CP;
                    break;
                }
            }
            case Collation.LEAD_SURROGATE_TAG: {
                assert(forward);  // Backward iteration should never see lead surrogate code _unit_ data.
                assert(isLeadSurrogate(c));
                char trail;
                if(Character.isLowSurrogate(trail = handleGetTrailSurrogate())) {
                    c = Character.toCodePoint((char)c, trail);
                    ce32 &= Collation.LEAD_TYPE_MASK;
                    if(ce32 == Collation.LEAD_ALL_UNASSIGNED) {
                        ce32 = Collation.UNASSIGNED_CE32;  // unassigned-implicit
                    } else if(ce32 == Collation.LEAD_ALL_FALLBACK ||
                            (ce32 = d.getCE32FromSupplementary(c)) == Collation.FALLBACK_CE32) {
                        // fall back to the base data
                        d = d.base;
                        ce32 = d.getCE32FromSupplementary(c);
                    }
                } else {
                    // c is an unpaired surrogate.
                    ce32 = Collation.UNASSIGNED_CE32;
                }
                break;
            }
            case Collation.OFFSET_TAG:
                assert(c >= 0);
                ceBuffer.append(d.getCEFromOffsetCE32(c, ce32));
                return;
            case Collation.IMPLICIT_TAG:
                assert(c >= 0);
                if(isSurrogate(c) && forbidSurrogateCodePoints()) {
                    ce32 = Collation.FFFD_CE32;
                    break;
                } else {
                    ceBuffer.append(Collation.unassignedCEFromCodePoint(c));
                    return;
                }
            }
        }
        ceBuffer.append(Collation.ceFromSimpleCE32(ce32));
    }

    // TODO: Propose widening the UTF16 method.
    private static final boolean isSurrogate(int c) {
        return (c & 0xfffff800) == 0xd800;
    }

    // TODO: Propose widening the UTF16 method.
    protected static final boolean isLeadSurrogate(int c) {
        return (c & 0xfffffc00) == 0xd800;
    }

    // TODO: Propose widening the UTF16 method.
    protected static final boolean isTrailSurrogate(int c) {
        return (c & 0xfffffc00) == 0xdc00;
    }

    // Main lookup trie of the data object.
    protected final Trie2_32 trie;
    protected final CollationData data;

    private final long nextCEFromCE32(CollationData d, int c, int ce32) {
        --ceBuffer.length;  // Undo ceBuffer.incLength().
        appendCEsFromCE32(d, c, ce32, true);
        return ceBuffer.get(cesIndex++);
    }

    private final int getCE32FromPrefix(CollationData d, int ce32) {
        int index = Collation.indexFromCE32(ce32);
        ce32 = d.getCE32FromContexts(index);  // Default if no prefix match.
        index += 2;
        // Number of code points read before the original code point.
        int lookBehind = 0;
        CharsTrie prefixes = new CharsTrie(d.contexts, index);
        for(;;) {
            int c = previousCodePoint();
            if(c < 0) { break; }
            ++lookBehind;
            BytesTrie.Result match = prefixes.nextForCodePoint(c);
            if(match.hasValue()) {
                ce32 = prefixes.getValue();
            }
            if(!match.hasNext()) { break; }
        }
        forwardNumCodePoints(lookBehind);
        return ce32;
    }

    private final int nextSkippedCodePoint() {
        if(skipped != null && skipped.hasNext()) { return skipped.next(); }
        if(numCpFwd == 0) { return Collation.SENTINEL_CP; }
        int c = nextCodePoint();
        if(skipped != null && !skipped.isEmpty() && c >= 0) { skipped.incBeyond(); }
        if(numCpFwd > 0 && c >= 0) { --numCpFwd; }
        return c;
    }

    private final void backwardNumSkipped(int n) {
        if(skipped != null && !skipped.isEmpty()) {
            n = skipped.backwardNumCodePoints(n);
        }
        backwardNumCodePoints(n);
        if(numCpFwd >= 0) { numCpFwd += n; }
    }

    private final int nextCE32FromContraction(
            CollationData d, int contractionCE32,
            CharSequence trieChars, int trieOffset, int ce32, int c) {
        // c: next code point after the original one

        // Number of code points read beyond the original code point.
        // Needed for discontiguous contraction matching.
        int lookAhead = 1;
        // Number of code points read since the last match (initially only c).
        int sinceMatch = 1;
        // Normally we only need a contiguous match,
        // and therefore need not remember the suffixes state from before a mismatch for retrying.
        // If we are already processing skipped combining marks, then we do track the state.
        CharsTrie suffixes = new CharsTrie(trieChars, trieOffset);
        if(skipped != null && !skipped.isEmpty()) { skipped.saveTrieState(suffixes); }
        BytesTrie.Result match = suffixes.firstForCodePoint(c);
        for(;;) {
            int nextCp;
            if(match.hasValue()) {
                ce32 = suffixes.getValue();
                if(!match.hasNext() || (c = nextSkippedCodePoint()) < 0) {
                    return ce32;
                }
                if(skipped != null && !skipped.isEmpty()) { skipped.saveTrieState(suffixes); }
                sinceMatch = 1;
            } else if(match == BytesTrie.Result.NO_MATCH || (nextCp = nextSkippedCodePoint()) < 0) {
                // No match for c, or partial match (BytesTrie.Result.NO_VALUE) and no further text.
                // Back up if necessary, and try a discontiguous contraction.
                if((contractionCE32 & Collation.CONTRACT_TRAILING_CCC) != 0 &&
                        // Discontiguous contraction matching extends an existing match.
                        // If there is no match yet, then there is nothing to do.
                        ((contractionCE32 & Collation.CONTRACT_SINGLE_CP_NO_MATCH) == 0 ||
                            sinceMatch < lookAhead)) {
                    // The last character of at least one suffix has lccc!=0,
                    // allowing for discontiguous contractions.
                    // UCA S2.1.1 only processes non-starters immediately following
                    // "a match in the table" (sinceMatch=1).
                    if(sinceMatch > 1) {
                        // Return to the state after the last match.
                        // (Return to sinceMatch=0 and re-fetch the first partially-matched character.)
                        backwardNumSkipped(sinceMatch);
                        c = nextSkippedCodePoint();
                        lookAhead -= sinceMatch - 1;
                        sinceMatch = 1;
                    }
                    if(d.getFCD16(c) > 0xff) {
                        return nextCE32FromDiscontiguousContraction(
                            d, suffixes, ce32, lookAhead, c);
                    }
                }
                break;
            } else {
                // Continue after partial match (BytesTrie.Result.NO_VALUE) for c.
                // It does not have a result value, therefore it is not itself "a match in the table".
                // If a partially-matched c has ccc!=0 then
                // it might be skipped in discontiguous contraction.
                c = nextCp;
                ++sinceMatch;
            }
            ++lookAhead;
            match = suffixes.nextForCodePoint(c);
        }
        backwardNumSkipped(sinceMatch);
        return ce32;
    }

    private final int nextCE32FromDiscontiguousContraction(
            CollationData d, CharsTrie suffixes, int ce32,
            int lookAhead, int c) {
        // UCA section 3.3.2 Contractions:
        // Contractions that end with non-starter characters
        // are known as discontiguous contractions.
        // ... discontiguous contractions must be detected in input text
        // whenever the final sequence of non-starter characters could be rearranged
        // so as to make a contiguous matching sequence that is canonically equivalent.

        // UCA: http://www.unicode.org/reports/tr10/#S2.1
        // S2.1 Find the longest initial substring S at each point that has a match in the table.
        // S2.1.1 If there are any non-starters following S, process each non-starter C.
        // S2.1.2 If C is not blocked from S, find if S + C has a match in the table.
        //     Note: A non-starter in a string is called blocked
        //     if there is another non-starter of the same canonical combining class or zero
        //     between it and the last character of canonical combining class 0.
        // S2.1.3 If there is a match, replace S by S + C, and remove C.

        // First: Is a discontiguous contraction even possible?
        int fcd16 = d.getFCD16(c);
        assert(fcd16 > 0xff);  // The caller checked this already, as a shortcut.
        int nextCp = nextSkippedCodePoint();
        if(nextCp < 0) {
            // No further text.
            backwardNumSkipped(1);
            return ce32;
        }
        ++lookAhead;
        int prevCC = fcd16 & 0xff;
        fcd16 = d.getFCD16(nextCp);
        if(fcd16 <= 0xff) {
            // The next code point after c is a starter (S2.1.1 "process each non-starter").
            backwardNumSkipped(2);
            return ce32;
        }

        // We have read and matched (lookAhead-2) code points,
        // read non-matching c and peeked ahead at nextCp.
        // Return to the state before the mismatch and continue matching with nextCp.
        if(skipped == null || skipped.isEmpty()) {
            if(skipped == null) {
                skipped = new SkippedState();
            }
            suffixes.reset();
            if(lookAhead > 2) {
                // Replay the partial match so far.
                backwardNumCodePoints(lookAhead);
                suffixes.firstForCodePoint(nextCodePoint());
                for(int i = 3; i < lookAhead; ++i) {
                    suffixes.nextForCodePoint(nextCodePoint());
                }
                // Skip c (which did not match) and nextCp (which we will try now).
                forwardNumCodePoints(2);
            }
            skipped.saveTrieState(suffixes);
        } else {
            // Reset to the trie state before the failed match of c.
            skipped.resetToTrieState(suffixes);
        }

        skipped.setFirstSkipped(c);
        // Number of code points read since the last match (at this point: c and nextCp).
        int sinceMatch = 2;
        c = nextCp;
        for(;;) {
            BytesTrie.Result match;
            // "If C is not blocked from S, find if S + C has a match in the table." (S2.1.2)
            if(prevCC < (fcd16 >> 8) && (match = suffixes.nextForCodePoint(c)).hasValue()) {
                // "If there is a match, replace S by S + C, and remove C." (S2.1.3)
                // Keep prevCC unchanged.
                ce32 = suffixes.getValue();
                sinceMatch = 0;
                skipped.recordMatch();
                if(!match.hasNext()) { break; }
                skipped.saveTrieState(suffixes);
            } else {
                // No match for "S + C", skip C.
                skipped.skip(c);
                skipped.resetToTrieState(suffixes);
                prevCC = fcd16 & 0xff;
            }
            if((c = nextSkippedCodePoint()) < 0) { break; }
            ++sinceMatch;
            fcd16 = d.getFCD16(c);
            if(fcd16 <= 0xff) {
                // The next code point after c is a starter (S2.1.1 "process each non-starter").
                break;
            }
        }
        backwardNumSkipped(sinceMatch);
        boolean isTopDiscontiguous = skipped.isEmpty();
        skipped.replaceMatch();
        if(isTopDiscontiguous && !skipped.isEmpty()) {
            // We did get a match after skipping one or more combining marks,
            // and we are not in a recursive discontiguous contraction.
            // Append CEs from the contraction ce32
            // and then from the combining marks that we skipped before the match.
            c = Collation.SENTINEL_CP;
            for(;;) {
                appendCEsFromCE32(d, c, ce32, true);
                // Fetch CE32s for skipped combining marks from the normal data, with fallback,
                // rather than from the CollationData where we found the contraction.
                if(!skipped.hasNext()) { break; }
                c = skipped.next();
                ce32 = getDataCE32(c);
                if(ce32 == Collation.FALLBACK_CE32) {
                    d = data.base;
                    ce32 = d.getCE32(c);
                } else {
                    d = data;
                }
                // Note: A nested discontiguous-contraction match
                // replaces consumed combining marks with newly skipped ones
                // and resets the reading position to the beginning.
            }
            skipped.clear();
            ce32 = Collation.NO_CE32;  // Signal to the caller that the result is in the ceBuffer.
        }
        return ce32;
    }

    /**
     * Returns the previous CE when data.isUnsafeBackward(c, isNumeric).
     */
    private final long previousCEUnsafe(int c, UVector32 offsets) {
        // We just move through the input counting safe and unsafe code points
        // without collecting the unsafe-backward substring into a buffer and
        // switching to it.
        // This is to keep the logic simple. Otherwise we would have to handle
        // prefix matching going before the backward buffer, switching
        // to iteration and back, etc.
        // In the most important case of iterating over a normal string,
        // reading from the string itself is already maximally fast.
        // The only drawback there is that after getting the CEs we always
        // skip backward to the safe character rather than switching out
        // of a backwardBuffer.
        // But this should not be the common case for previousCE(),
        // and correctness and maintainability are more important than
        // complex optimizations.
        // Find the first safe character before c.
        int numBackward = 1;
        while((c = previousCodePoint()) >= 0) {
            ++numBackward;
            if(!data.isUnsafeBackward(c, isNumeric)) {
                break;
            }
        }
        // Set the forward iteration limit.
        // Note: This counts code points.
        // We cannot enforce a limit in the middle of a surrogate pair or similar.
        numCpFwd = numBackward;
        // Reset the forward iterator.
        cesIndex = 0;
        assert(ceBuffer.length == 0);
        // Go forward and collect the CEs.
        int offset = getOffset();
        while(numCpFwd > 0) {
            // nextCE() normally reads one code point.
            // Contraction matching and digit specials read more and check numCpFwd.
            --numCpFwd;
            // Append one or more CEs to the ceBuffer.
            nextCE();
            assert(ceBuffer.get(ceBuffer.length - 1) != Collation.NO_CE);
            // No need to loop for getting each expansion CE from nextCE().
            cesIndex = ceBuffer.length;
            // However, we need to write an offset for each CE.
            // This is for CollationElementIterator.getOffset() to return
            // intermediate offsets from the unsafe-backwards segment.
            assert(offsets.size() < ceBuffer.length);
            offsets.addElement(offset);
            // For an expansion, the offset of each non-initial CE is the limit offset,
            // consistent with forward iteration.
            offset = getOffset();
            while(offsets.size() < ceBuffer.length) {
                offsets.addElement(offset);
            };
        }
        assert(offsets.size() == ceBuffer.length);
        // End offset corresponding to just after the unsafe-backwards segment.
        offsets.addElement(offset);
        // Reset the forward iteration limit
        // and move backward to before the segment for which we fetched CEs.
        numCpFwd = -1;
        backwardNumCodePoints(numBackward);
        // Use the collected CEs and return the last one.
        cesIndex = 0;  // Avoid cesIndex > ceBuffer.length when that gets decremented.
        return ceBuffer.get(--ceBuffer.length);
    }

    /**
     * Turns a string of digits (bytes 0..9)
     * into a sequence of CEs that will sort in numeric order.
     *
     * Starts from this ce32's digit value and consumes the following/preceding digits.
     * The digits string must not be empty and must not have leading zeros.
     */
    private final void appendNumericCEs(int ce32, boolean forward) {
        // Collect digits.
        // TODO: Use some kind of a byte buffer? We only store values 0..9.
        StringBuilder digits = new StringBuilder();
        if(forward) {
            for(;;) {
                char digit = Collation.digitFromCE32(ce32);
                digits.append(digit);
                if(numCpFwd == 0) { break; }
                int c = nextCodePoint();
                if(c < 0) { break; }
                ce32 = data.getCE32(c);
                if(ce32 == Collation.FALLBACK_CE32) {
                    ce32 = data.base.getCE32(c);
                }
                if(!Collation.hasCE32Tag(ce32, Collation.DIGIT_TAG)) {
                    backwardNumCodePoints(1);
                    break;
                }
                if(numCpFwd > 0) { --numCpFwd; }
            }
        } else {
            for(;;) {
                char digit = Collation.digitFromCE32(ce32);
                digits.append(digit);
                int c = previousCodePoint();
                if(c < 0) { break; }
                ce32 = data.getCE32(c);
                if(ce32 == Collation.FALLBACK_CE32) {
                    ce32 = data.base.getCE32(c);
                }
                if(!Collation.hasCE32Tag(ce32, Collation.DIGIT_TAG)) {
                    forwardNumCodePoints(1);
                    break;
                }
            }
            // Reverse the digit string.
            digits.reverse();
        }
        int pos = 0;
        do {
            // Skip leading zeros.
            while(pos < (digits.length() - 1) && digits.charAt(pos) == 0) { ++pos; }
            // Write a sequence of CEs for at most 254 digits at a time.
            int segmentLength = digits.length() - pos;
            if(segmentLength > 254) { segmentLength = 254; }
            appendNumericSegmentCEs(digits.subSequence(pos, pos + segmentLength));
            pos += segmentLength;
        } while(pos < digits.length());
    }

    /**
     * Turns 1..254 digits into a sequence of CEs.
     * Called by appendNumericCEs() for each segment of at most 254 digits.
     */
    private final void appendNumericSegmentCEs(CharSequence digits) {
        int length = digits.length();
        assert(1 <= length && length <= 254);
        assert(length == 1 || digits.charAt(0) != 0);
        long numericPrimary = data.numericPrimary;
        // Note: We use primary byte values 2..255: digits are not compressible.
        if(length <= 7) {
            // Very dense encoding for small numbers.
            int value = digits.charAt(0);
            for(int i = 1; i < length; ++i) {
                value = value * 10 + digits.charAt(i);
            }
            // Primary weight second byte values:
            //     74 byte values   2.. 75 for small numbers in two-byte primary weights.
            //     40 byte values  76..115 for medium numbers in three-byte primary weights.
            //     16 byte values 116..131 for large numbers in four-byte primary weights.
            //    124 byte values 132..255 for very large numbers with 4..127 digit pairs.
            int firstByte = 2;
            int numBytes = 74;
            if(value < numBytes) {
                // Two-byte primary for 0..73, good for day & month numbers etc.
                long primary = numericPrimary | ((firstByte + value) << 16);
                ceBuffer.append(Collation.makeCE(primary));
                return;
            }
            value -= numBytes;
            firstByte += numBytes;
            numBytes = 40;
            if(value < numBytes * 254) {
                // Three-byte primary for 74..10233=74+40*254-1, good for year numbers and more.
                long primary = numericPrimary |
                    ((firstByte + value / 254) << 16) | ((2 + value % 254) << 8);
                ceBuffer.append(Collation.makeCE(primary));
                return;
            }
            value -= numBytes * 254;
            firstByte += numBytes;
            numBytes = 16;
            if(value < numBytes * 254 * 254) {
                // Four-byte primary for 10234..1042489=10234+16*254*254-1.
                long primary = numericPrimary | (2 + value % 254);
                value /= 254;
                primary |= (2 + value % 254) << 8;
                value /= 254;
                primary |= (firstByte + value % 254) << 16;
                ceBuffer.append(Collation.makeCE(primary));
                return;
            }
            // original value > 1042489
        }
        assert(length >= 7);

        // The second primary byte value 132..255 indicates the number of digit pairs (4..127),
        // then we generate primary bytes with those pairs.
        // Omit trailing 00 pairs.
        // Decrement the value for the last pair.

        // Set the exponent. 4 pairs.132, 5 pairs.133, ..., 127 pairs.255.
        int numPairs = (length + 1) / 2;
        long primary = numericPrimary | ((132 - 4 + numPairs) << 16);
        // Find the length without trailing 00 pairs.
        while(digits.charAt(length - 1) == 0 && digits.charAt(length - 2) == 0) {
            length -= 2;
        }
        // Read the first pair.
        int pair;
        int pos;
        if((length & 1) != 0) {
            // Only "half a pair" if we have an odd number of digits.
            pair = digits.charAt(0);
            pos = 1;
        } else {
            pair = digits.charAt(0) * 10 + digits.charAt(1);
            pos = 2;
        }
        pair = 11 + 2 * pair;
        // Add the pairs of digits between pos and length.
        int shift = 8;
        while(pos < length) {
            if(shift == 0) {
                // Every three pairs/bytes we need to store a 4-byte-primary CE
                // and start with a new CE with the '0' primary lead byte.
                primary |= pair;
                ceBuffer.append(Collation.makeCE(primary));
                primary = numericPrimary;
                shift = 16;
            } else {
                primary |= pair << shift;
                shift -= 8;
            }
            pair = 11 + 2 * (digits.charAt(pos) * 10 + digits.charAt(pos + 1));
            pos += 2;
        }
        primary |= (pair - 1) << shift;
        ceBuffer.append(Collation.makeCE(primary));
    }

    private CEBuffer ceBuffer;
    private int cesIndex;

    private SkippedState skipped;

    // Number of code points to read forward, or -1.
    // Used as a forward iteration limit in previousCEUnsafe().
    private int numCpFwd;
    // Numeric collation (CollationSettings.NUMERIC).
    private boolean isNumeric;
}
