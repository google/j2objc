/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.text;

import java.text.CharacterIterator;
import java.util.BitSet;

import android.icu.impl.CharacterIteration;

abstract class DictionaryBreakEngine implements LanguageBreakEngine {

    /* Helper class for improving readability of the Thai/Lao/Khmer word break
     * algorithm.
     */
    static class PossibleWord {
        // List size, limited by the maximum number of words in the dictionary
        // that form a nested sequence.
        private final static int POSSIBLE_WORD_LIST_MAX = 20;
        //list of word candidate lengths, in increasing length order
        private int lengths[];
        private int count[];    // Count of candidates
        private int prefix;     // The longest match with a dictionary word
        private int offset;     // Offset in the text of these candidates
        private int mark;       // The preferred candidate's offset
        private int current;    // The candidate we're currently looking at

        // Default constructor
        public PossibleWord() {
            lengths = new int[POSSIBLE_WORD_LIST_MAX];
            count = new int[1]; // count needs to be an array of 1 so that it can be pass as reference
            offset = -1;
        }

        // Fill the list of candidates if needed, select the longest, and return the number found
        public int candidates(CharacterIterator fIter, DictionaryMatcher dict, int rangeEnd) {
            int start = fIter.getIndex();
            if (start != offset) {
                offset = start;
                prefix = dict.matches(fIter, rangeEnd - start, lengths, count, lengths.length);
                // Dictionary leaves text after longest prefix, not longest word. Back up.
                if (count[0] <= 0) {
                    fIter.setIndex(start);
                }
            }
            if (count[0] > 0) {
                fIter.setIndex(start + lengths[count[0]-1]);
            }
            current = count[0] - 1;
            mark = current;
            return count[0];
        }

        // Select the currently marked candidate, point after it in the text, and invalidate self
        public int acceptMarked(CharacterIterator fIter) {
            fIter.setIndex(offset + lengths[mark]);
            return lengths[mark];
        }

        // Backup from the current candidate to the next shorter one; return true if that exists
        // and point the text after it
        public boolean backUp(CharacterIterator fIter) {
            if (current > 0) {
                fIter.setIndex(offset + lengths[--current]);
                return true;
            }
            return false;
        }

        // Return the longest prefix this candidate location shares with a dictionary word
        public int longestPrefix() {
            return prefix;
        }

        // Mark the current candidate as the one we like
        public void markCurrent() {
            mark = current;
        }
    }

    /**
     *  A deque-like structure holding raw ints.
     *  Partial, limited implementation, only what is needed by the dictionary implementation.
     *  For internal use only.
     * @hide draft / provisional / internal are hidden on Android
     */
    static class DequeI implements Cloneable {
        private int[] data = new int[50];
        private int lastIdx = 4;   // or base of stack. Index of element.
        private int firstIdx = 4;  // or Top of Stack. Index of element + 1.

        @Override
        public Object clone() throws CloneNotSupportedException {
            DequeI result = (DequeI)super.clone();
            result.data = data.clone();
            return result;
        }

        int size() {
            return firstIdx - lastIdx;
        }

        boolean isEmpty() {
            return size() == 0;
        }

        private void grow() {
            int[] newData = new int[data.length * 2];
            System.arraycopy(data,  0,  newData,  0, data.length);
            data = newData;
        }

        void offer(int v) {
            // Note that the actual use cases of offer() add at most one element.
            //   We make no attempt to handle more than a few.
            assert lastIdx > 0;
            data[--lastIdx] = v;
        }

        void push(int v) {
            if (firstIdx >= data.length) {
                grow();
            }
            data[firstIdx++] = v;
        }

        int pop() {
            assert size() > 0;
            return data[--firstIdx];
        }

        int peek() {
            assert size() > 0;
            return data[firstIdx - 1];
        }

        int peekLast() {
            assert size() > 0;
            return data[lastIdx];
        }

        int pollLast() {
            assert size() > 0;
            return data[lastIdx++];
        }

        boolean contains(int v) {
            for (int i=lastIdx; i< firstIdx; i++) {
                if (data[i] == v) {
                    return true;
                }
            }
            return false;
        }

        int elementAt(int i) {
            assert i < size();
            return data[lastIdx + i];
        }

        void removeAllElements() {
            lastIdx = firstIdx = 4;
        }
    }

    UnicodeSet fSet = new UnicodeSet();
    private BitSet fTypes = new BitSet(32);

    /**
     * @param breakTypes The types of break iterators that can use this engine.
     *  For example, BreakIterator.KIND_LINE
     */
    public DictionaryBreakEngine(Integer... breakTypes) {
        for (Integer type: breakTypes) {
            fTypes.set(type);
        }
    }

    @Override
    public boolean handles(int c, int breakType) {
        return fTypes.get(breakType) &&  // this type can use us
                fSet.contains(c);        // we recognize the character
    }

    @Override
    public int findBreaks(CharacterIterator text, int startPos, int endPos,
            int breakType, DequeI foundBreaks) {
        int result = 0;

         // Find the span of characters included in the set.
         //   The span to break begins at the current position int the text, and
         //   extends towards the start or end of the text, depending on 'reverse'.

        int start = text.getIndex();
        int current;
        int rangeStart;
        int rangeEnd;
        int c = CharacterIteration.current32(text);
        while ((current = text.getIndex()) < endPos && fSet.contains(c)) {
            CharacterIteration.next32(text);
            c = CharacterIteration.current32(text);
        }
        rangeStart = start;
        rangeEnd = current;

        // if (breakType >= 0 && breakType < 32 && (((uint32_t)1 << breakType) & fTypes)) {
        // TODO: Why does icu4c have this?
        result = divideUpDictionaryRange(text, rangeStart, rangeEnd, foundBreaks);
        text.setIndex(current);

        return result;
    }

    void setCharacters(UnicodeSet set) {
        fSet = new UnicodeSet(set);
        fSet.compact();
    }

    /**
     * <p>Divide up a range of known dictionary characters handled by this break engine.</p>
     *
     * @param text A UText representing the text
     * @param rangeStart The start of the range of dictionary characters
     * @param rangeEnd The end of the range of dictionary characters
     * @param foundBreaks Output of break positions. Positions are pushed.
     *                    Pre-existing contents of the output stack are unaltered.
     * @return The number of breaks found
     */
     abstract int divideUpDictionaryRange(CharacterIterator text,
                                          int               rangeStart,
                                          int               rangeEnd,
                                          DequeI            foundBreaks );
}
