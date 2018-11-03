/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
*******************************************************************************
* Copyright (C) 1996-2016, International Business Machines Corporation and
* others. All Rights Reserved.
*******************************************************************************
*/
package android.icu.text;

import java.text.CharacterIterator;
import java.util.HashMap;
import java.util.Map;

import android.icu.impl.CharacterIteratorWrapper;
import android.icu.impl.coll.Collation;
import android.icu.impl.coll.CollationData;
import android.icu.impl.coll.CollationIterator;
import android.icu.impl.coll.ContractionsAndExpansions;
import android.icu.impl.coll.FCDIterCollationIterator;
import android.icu.impl.coll.FCDUTF16CollationIterator;
import android.icu.impl.coll.IterCollationIterator;
import android.icu.impl.coll.UTF16CollationIterator;
import android.icu.impl.coll.UVector32;

/**
 * <code>CollationElementIterator</code> is an iterator created by
 * a RuleBasedCollator to walk through a string. The return result of
 * each iteration is a 32-bit collation element (CE) that defines the
 * ordering priority of the next character or sequence of characters
 * in the source string.
 *
 * <p>For illustration, consider the following in Slovak and in traditional Spanish collation:
 * <blockquote>
 * <pre>
 * "ca" -&gt; the first collation element is CE('c') and the second
 *         collation element is CE('a').
 * "cha" -&gt; the first collation element is CE('ch') and the second
 *          collation element is CE('a').
 * </pre>
 * </blockquote>
 * And in German phonebook collation,
 * <blockquote>
 * <pre>
 * Since the character '&#230;' is a composed character of 'a' and 'e', the
 * iterator returns two collation elements for the single character '&#230;'
 *
 * "&#230;b" -&gt; the first collation element is collation_element('a'), the
 *              second collation element is collation_element('e'), and the
 *              third collation element is collation_element('b').
 * </pre>
 * </blockquote>
 *
 * <p>For collation ordering comparison, the collation element results
 * can not be compared simply by using basic arithmetic operators,
 * e.g. &lt;, == or &gt;, further processing has to be done. Details
 * can be found in the ICU
 * <a href="http://userguide.icu-project.org/collation/architecture">
 * User Guide</a>. An example of using the CollationElementIterator
 * for collation ordering comparison is the class
 * {@link android.icu.text.StringSearch}.
 *
 * <p>To construct a CollationElementIterator object, users
 * call the method getCollationElementIterator() on a
 * RuleBasedCollator that defines the desired sorting order.
 *
 * <p> Example:
 * <blockquote>
 * <pre>
 *  String testString = "This is a test";
 *  RuleBasedCollator rbc = new RuleBasedCollator("&amp;a&lt;b");
 *  CollationElementIterator iterator = rbc.getCollationElementIterator(testString);
 *  int primaryOrder = iterator.IGNORABLE;
 *  while (primaryOrder != iterator.NULLORDER) {
 *      int order = iterator.next();
 *      if (order != iterator.IGNORABLE &amp;&amp;
 *          order != iterator.NULLORDER) {
 *          // order is valid, not ignorable and we have not passed the end
 *          // of the iteration, we do something
 *          primaryOrder = CollationElementIterator.primaryOrder(order);
 *          System.out.println("Next primary order 0x" +
 *                             Integer.toHexString(primaryOrder));
 *      }
 *  }
 * </pre>
 * </blockquote>
 * <p>
 * The method next() returns the collation order of the next character based on
 * the comparison level of the collator. The method previous() returns the
 * collation order of the previous character based on the comparison level of
 * the collator. The Collation Element Iterator moves only in one direction
 * between calls to reset(), setOffset(), or setText(). That is, next() and
 * previous() can not be inter-used. Whenever previous() is to be called after
 * next() or vice versa, reset(), setOffset() or setText() has to be called first
 * to reset the status, shifting current position to either the end or the start of
 * the string (reset() or setText()), or the specified position (setOffset()).
 * Hence at the next call of next() or previous(), the first or last collation order,
 * or collation order at the specified position will be returned. If a change of
 * direction is done without one of these calls, the result is undefined.
 * <p>
 * This class is not subclassable.
 * @see Collator
 * @see RuleBasedCollator
 * @see StringSearch
 * @author Syn Wee Quek
 */
public final class CollationElementIterator
{
    private CollationIterator iter_;  // owned
    private RuleBasedCollator rbc_;  // aliased
    private int otherHalf_;
    /**
     * &lt;0: backwards; 0: just after reset() (previous() begins from end);
     * 1: just after setOffset(); >1: forward
     */
    private byte dir_;
    /**
     * Stores offsets from expansions and from unsafe-backwards iteration,
     * so that getOffset() returns intermediate offsets for the CEs
     * that are consistent with forward iteration.
     */
    private UVector32 offsets_;

    private String string_;  // TODO: needed in Java? if so, then add a UCharacterIterator field too?


    /**
     * This constant is returned by the iterator in the methods
     * next() and previous() when the end or the beginning of the
     * source string has been reached, and there are no more valid
     * collation elements to return.
     *
     * <p>See class documentation for an example of use.
     * @see #next
     * @see #previous */
    public final static int NULLORDER = 0xffffffff;

    /**
     * This constant is returned by the iterator in the methods
     * next() and previous() when a collation element result is to be
     * ignored.
     *
     * <p>See class documentation for an example of use.
     * @see #next
     * @see #previous */
    public static final int IGNORABLE = 0;

    /**
     * Return the primary order of the specified collation element,
     * i.e. the first 16 bits.  This value is unsigned.
     * @param ce the collation element
     * @return the element's 16 bits primary order.
     */
    public final static int primaryOrder(int ce) {
        return (ce >>> 16) & 0xffff;
    }

    /**
     * Return the secondary order of the specified collation element,
     * i.e. the 16th to 23th bits, inclusive.  This value is unsigned.
     * @param ce the collation element
     * @return the element's 8 bits secondary order
     */
    public final static int secondaryOrder(int ce) {
        return (ce >>> 8) & 0xff;
    }

    /**
     * Return the tertiary order of the specified collation element, i.e. the last
     * 8 bits.  This value is unsigned.
     * @param ce the collation element
     * @return the element's 8 bits tertiary order
     */
    public final static int tertiaryOrder(int ce) {
        return ce & 0xff;
    }


    private static final int getFirstHalf(long p, int lower32) {
        return ((int)p & 0xffff0000) | ((lower32 >> 16) & 0xff00) | ((lower32 >> 8) & 0xff);
    }

    private static final int getSecondHalf(long p, int lower32) {
        return ((int)p << 16) | ((lower32 >> 8) & 0xff00) | (lower32 & 0x3f);
    }

    private static final boolean ceNeedsTwoParts(long ce) {
        return (ce & 0xffff00ff003fL) != 0;
    }

    private CollationElementIterator(RuleBasedCollator collator) {
        iter_ = null;
        rbc_ = collator;
        otherHalf_ = 0;
        dir_ = 0;
        offsets_ = null;
    }

    /**
     * CollationElementIterator constructor. This takes a source
     * string and a RuleBasedCollator. The iterator will walk through
     * the source string based on the rules defined by the
     * collator. If the source string is empty, NULLORDER will be
     * returned on the first call to next().
     *
     * @param source the source string.
     * @param collator the RuleBasedCollator
     */
    CollationElementIterator(String source, RuleBasedCollator collator) {
        this(collator);
        setText(source);
    }
    // Note: The constructors should take settings & tailoring, not a collator,
    // to avoid circular dependencies.
    // However, for equals() we would need to be able to compare tailoring data for equality
    // without making CollationData or CollationTailoring depend on TailoredSet.
    // (See the implementation of RuleBasedCollator.equals().)
    // That might require creating an intermediate class that would be used
    // by both CollationElementIterator and RuleBasedCollator
    // but only contain the part of RBC.equals() related to data and rules.

    /**
     * CollationElementIterator constructor. This takes a source
     * character iterator and a RuleBasedCollator. The iterator will
     * walk through the source string based on the rules defined by
     * the collator. If the source string is empty, NULLORDER will be
     * returned on the first call to next().
     *
     * @param source the source string iterator.
     * @param collator the RuleBasedCollator
     */
    CollationElementIterator(CharacterIterator source, RuleBasedCollator collator) {
        this(collator);
        setText(source);
    }

    /**
     * CollationElementIterator constructor. This takes a source
     * character iterator and a RuleBasedCollator. The iterator will
     * walk through the source string based on the rules defined by
     * the collator. If the source string is empty, NULLORDER will be
     * returned on the first call to next().
     *
     * @param source the source string iterator.
     * @param collator the RuleBasedCollator
     */
    CollationElementIterator(UCharacterIterator source, RuleBasedCollator collator) {
        this(collator);
        setText(source);
    }

    /**
     * Returns the character offset in the source string
     * corresponding to the next collation element. I.e., getOffset()
     * returns the position in the source string corresponding to the
     * collation element that will be returned by the next call to
     * next() or previous(). This value could be any of:
     * <ul>
     * <li> The index of the <b>first</b> character corresponding to
     * the next collation element. (This means that if
     * <code>setOffset(offset)</code> sets the index in the middle of
     * a contraction, <code>getOffset()</code> returns the index of
     * the first character in the contraction, which may not be equal
     * to the original offset that was set. Hence calling getOffset()
     * immediately after setOffset(offset) does not guarantee that the
     * original offset set will be returned.)
     * <li> If normalization is on, the index of the <b>immediate</b>
     * subsequent character, or composite character with the first
     * character, having a combining class of 0.
     * <li> The length of the source string, if iteration has reached
     * the end.
     *</ul>
     *
     * @return The character offset in the source string corresponding to the
     *         collation element that will be returned by the next call to
     *         next() or previous().
     */
    public int getOffset() {
        if (dir_ < 0 && offsets_ != null && !offsets_.isEmpty()) {
            // CollationIterator.previousCE() decrements the CEs length
            // while it pops CEs from its internal buffer.
            int i = iter_.getCEsLength();
            if (otherHalf_ != 0) {
                // Return the trailing CE offset while we are in the middle of a 64-bit CE.
                ++i;
            }
            assert (i < offsets_.size());
            return offsets_.elementAti(i);
        }
        return iter_.getOffset();
    }

    /**
     * Get the next collation element in the source string.
     *
     * <p>This iterator iterates over a sequence of collation elements
     * that were built from the string. Because there isn't
     * necessarily a one-to-one mapping from characters to collation
     * elements, this doesn't mean the same thing as "return the
     * collation element [or ordering priority] of the next character
     * in the string".
     *
     * <p>This function returns the collation element that the
     * iterator is currently pointing to, and then updates the
     * internal pointer to point to the next element.
     *
     * @return the next collation element or NULLORDER if the end of the
     *         iteration has been reached.
     */
    public int next() {
        if (dir_ > 1) {
            // Continue forward iteration. Test this first.
            if (otherHalf_ != 0) {
                int oh = otherHalf_;
                otherHalf_ = 0;
                return oh;
            }
        } else if (dir_ == 1) {
            // next() after setOffset()
            dir_ = 2;
        } else if (dir_ == 0) {
            // The iter_ is already reset to the start of the text.
            dir_ = 2;
        } else /* dir_ < 0 */{
            // illegal change of direction
            throw new IllegalStateException("Illegal change of direction");
            // Java porting note: ICU4C sets U_INVALID_STATE_ERROR to the return status.
        }
        // No need to keep all CEs in the buffer when we iterate.
        iter_.clearCEsIfNoneRemaining();
        long ce = iter_.nextCE();
        if (ce == Collation.NO_CE) {
            return NULLORDER;
        }
        // Turn the 64-bit CE into two old-style 32-bit CEs, without quaternary bits.
        long p = ce >>> 32;
        int lower32 = (int) ce;
        int firstHalf = getFirstHalf(p, lower32);
        int secondHalf = getSecondHalf(p, lower32);
        if (secondHalf != 0) {
            otherHalf_ = secondHalf | 0xc0; // continuation CE
        }
        return firstHalf;
    }

    /**
     * Get the previous collation element in the source string.
     *
     * <p>This iterator iterates over a sequence of collation elements
     * that were built from the string. Because there isn't
     * necessarily a one-to-one mapping from characters to collation
     * elements, this doesn't mean the same thing as "return the
     * collation element [or ordering priority] of the previous
     * character in the string".
     *
     * <p>This function updates the iterator's internal pointer to
     * point to the collation element preceding the one it's currently
     * pointing to and then returns that element, while next() returns
     * the current element and then updates the pointer.
     *
     * @return the previous collation element, or NULLORDER when the start of
     *             the iteration has been reached.
     */
    public int previous() {
        if (dir_ < 0) {
            // Continue backwards iteration. Test this first.
            if (otherHalf_ != 0) {
                int oh = otherHalf_;
                otherHalf_ = 0;
                return oh;
            }
        } else if (dir_ == 0) {
            iter_.resetToOffset(string_.length());
            dir_ = -1;
        } else if (dir_ == 1) {
            // previous() after setOffset()
            dir_ = -1;
        } else /* dir_ > 1 */{
            // illegal change of direction
            throw new IllegalStateException("Illegal change of direction");
            // Java porting note: ICU4C sets U_INVALID_STATE_ERROR to the return status.
        }
        if (offsets_ == null) {
            offsets_ = new UVector32();
        }
        // If we already have expansion CEs, then we also have offsets.
        // Otherwise remember the trailing offset in case we need to
        // write offsets for an artificial expansion.
        int limitOffset = iter_.getCEsLength() == 0 ? iter_.getOffset() : 0;
        long ce = iter_.previousCE(offsets_);
        if (ce == Collation.NO_CE) {
            return NULLORDER;
        }
        // Turn the 64-bit CE into two old-style 32-bit CEs, without quaternary bits.
        long p = ce >>> 32;
        int lower32 = (int) ce;
        int firstHalf = getFirstHalf(p, lower32);
        int secondHalf = getSecondHalf(p, lower32);
        if (secondHalf != 0) {
            if (offsets_.isEmpty()) {
                // When we convert a single 64-bit CE into two 32-bit CEs,
                // we need to make this artificial expansion behave like a normal expansion.
                // See CollationIterator.previousCE().
                offsets_.addElement(iter_.getOffset());
                offsets_.addElement(limitOffset);
            }
            otherHalf_ = firstHalf;
            return secondHalf | 0xc0; // continuation CE
        }
        return firstHalf;
    }

    /**
     * Resets the cursor to the beginning of the string. The next
     * call to next() or previous() will return the first and last
     * collation element in the string, respectively.
     *
     * <p>If the RuleBasedCollator used by this iterator has had its
     * attributes changed, calling reset() will reinitialize the
     * iterator to use the new attributes.
     */
    public void reset() {
        iter_ .resetToOffset(0);
        otherHalf_ = 0;
        dir_ = 0;
    }

    /**
     * Sets the iterator to point to the collation element
     * corresponding to the character at the specified offset. The
     * value returned by the next call to next() will be the collation
     * element corresponding to the characters at offset.
     *
     * <p>If offset is in the middle of a contracting character
     * sequence, the iterator is adjusted to the start of the
     * contracting sequence. This means that getOffset() is not
     * guaranteed to return the same value set by this method.
     *
     * <p>If the decomposition mode is on, and offset is in the middle
     * of a decomposible range of source text, the iterator may not
     * return a correct result for the next forwards or backwards
     * iteration.  The user must ensure that the offset is not in the
     * middle of a decomposible range.
     *
     * @param newOffset the character offset into the original source string to
     *        set. Note that this is not an offset into the corresponding
     *        sequence of collation elements.
     */
    public void setOffset(int newOffset) {
        if (0 < newOffset && newOffset < string_.length()) {
            int offset = newOffset;
            do {
                char c = string_.charAt(offset);
                if (!rbc_.isUnsafe(c) ||
                        (Character.isHighSurrogate(c) && !rbc_.isUnsafe(string_.codePointAt(offset)))) {
                    break;
                }
                // Back up to before this unsafe character.
                --offset;
            } while (offset > 0);
            if (offset < newOffset) {
                // We might have backed up more than necessary.
                // For example, contractions "ch" and "cu" make both 'h' and 'u' unsafe,
                // but for text "chu" setOffset(2) should remain at 2
                // although we initially back up to offset 0.
                // Find the last safe offset no greater than newOffset by iterating forward.
                int lastSafeOffset = offset;
                do {
                    iter_.resetToOffset(lastSafeOffset);
                    do {
                        iter_.nextCE();
                    } while ((offset = iter_.getOffset()) == lastSafeOffset);
                    if (offset <= newOffset) {
                        lastSafeOffset = offset;
                    }
                } while (offset < newOffset);
                newOffset = lastSafeOffset;
            }
        }
        iter_.resetToOffset(newOffset);
        otherHalf_ = 0;
        dir_ = 1;
    }

    /**
     * Set a new source string for iteration, and reset the offset
     * to the beginning of the text.
     *
     * @param source the new source string for iteration.
     */
    public void setText(String source) {
        string_ = source; // TODO: do we need to remember the source string in a field?
        CollationIterator newIter;
        boolean numeric = rbc_.settings.readOnly().isNumeric();
        if (rbc_.settings.readOnly().dontCheckFCD()) {
            newIter = new UTF16CollationIterator(rbc_.data, numeric, string_, 0);
        } else {
            newIter = new FCDUTF16CollationIterator(rbc_.data, numeric, string_, 0);
        }
        iter_ = newIter;
        otherHalf_ = 0;
        dir_ = 0;
    }

    /**
     * Set a new source string iterator for iteration, and reset the
     * offset to the beginning of the text.
     *
     * <p>The source iterator's integrity will be preserved since a new copy
     * will be created for use.
     * @param source the new source string iterator for iteration.
     */
    public void setText(UCharacterIterator source) {
        string_ = source.getText(); // TODO: do we need to remember the source string in a field?
        // Note: In C++, we just setText(source.getText()).
        // In Java, we actually operate on a character iterator.
        // (The old code apparently did so only for a CharacterIterator;
        // for a UCharacterIterator it also just used source.getText()).
        // TODO: do we need to remember the cloned iterator in a field?
        UCharacterIterator src;
        try {
            src = (UCharacterIterator) source.clone();
        } catch (CloneNotSupportedException e) {
            // Fall back to ICU 52 behavior of iterating over the text contents
            // of the UCharacterIterator.
            setText(source.getText());
            return;
        }
        src.setToStart();
        CollationIterator newIter;
        boolean numeric = rbc_.settings.readOnly().isNumeric();
        if (rbc_.settings.readOnly().dontCheckFCD()) {
            newIter = new IterCollationIterator(rbc_.data, numeric, src);
        } else {
            newIter = new FCDIterCollationIterator(rbc_.data, numeric, src, 0);
        }
        iter_ = newIter;
        otherHalf_ = 0;
        dir_ = 0;
    }

    /**
     * Set a new source string iterator for iteration, and reset the
     * offset to the beginning of the text.
     *
     * @param source the new source string iterator for iteration.
     */
    public void setText(CharacterIterator source) {
        // Note: In C++, we just setText(source.getText()).
        // In Java, we actually operate on a character iterator.
        // TODO: do we need to remember the iterator in a field?
        // TODO: apparently we don't clone a CharacterIterator in Java,
        // we only clone the text for a UCharacterIterator?? see the old code in the constructors
        UCharacterIterator src = new CharacterIteratorWrapper(source);
        src.setToStart();
        string_ = src.getText(); // TODO: do we need to remember the source string in a field?
        CollationIterator newIter;
        boolean numeric = rbc_.settings.readOnly().isNumeric();
        if (rbc_.settings.readOnly().dontCheckFCD()) {
            newIter = new IterCollationIterator(rbc_.data, numeric, src);
        } else {
            newIter = new FCDIterCollationIterator(rbc_.data, numeric, src, 0);
        }
        iter_ = newIter;
        otherHalf_ = 0;
        dir_ = 0;
    }

    private static final class MaxExpSink implements ContractionsAndExpansions.CESink {
        MaxExpSink(Map<Integer, Integer> h) {
            maxExpansions = h;
        }

        @Override
        public void handleCE(long ce) {
        }

        @Override
        public void handleExpansion(long ces[], int start, int length) {
            if (length <= 1) {
                // We do not need to add single CEs into the map.
                return;
            }
            int count = 0; // number of CE "halves"
            for (int i = 0; i < length; ++i) {
                count += ceNeedsTwoParts(ces[start + i]) ? 2 : 1;
            }
            // last "half" of the last CE
            long ce = ces[start + length - 1];
            long p = ce >>> 32;
            int lower32 = (int) ce;
            int lastHalf = getSecondHalf(p, lower32);
            if (lastHalf == 0) {
                lastHalf = getFirstHalf(p, lower32);
                assert (lastHalf != 0);
            } else {
                lastHalf |= 0xc0; // old-style continuation CE
            }
            Integer oldCount = maxExpansions.get(lastHalf);
            if (oldCount == null || count > oldCount) {
                maxExpansions.put(lastHalf, count);
            }
        }

        private Map<Integer, Integer> maxExpansions;
    }

    static final Map<Integer, Integer> computeMaxExpansions(CollationData data) {
        Map<Integer, Integer> maxExpansions = new HashMap<Integer, Integer>();
        MaxExpSink sink = new MaxExpSink(maxExpansions);
        new ContractionsAndExpansions(null, null, sink, true).forData(data);
        return maxExpansions;
    }

    /**
     * Returns the maximum length of any expansion sequence that ends with
     * the specified collation element. If there is no expansion with this
     * collation element as the last element, returns 1.
     *
     * @param ce a collation element returned by previous() or next().
     * @return the maximum length of any expansion sequence ending
     *         with the specified collation element.
     */
    public int getMaxExpansion(int ce) {
        return getMaxExpansion(rbc_.tailoring.maxExpansions, ce);
    }

    static int getMaxExpansion(Map<Integer, Integer> maxExpansions, int order) {
        if (order == 0) {
            return 1;
        }
        Integer max;
        if (maxExpansions != null && (max = maxExpansions.get(order)) != null) {
            return max;
        }
        if ((order & 0xc0) == 0xc0) {
            // old-style continuation CE
            return 2;
        } else {
            return 1;
        }
    }

    /** Normalizes dir_=1 (just after setOffset()) to dir_=0 (just after reset()). */
    private byte normalizeDir() {
        return dir_ == 1 ? 0 : dir_;
    }

    /**
     * Tests that argument object is equals to this CollationElementIterator.
     * Iterators are equal if the objects uses the same RuleBasedCollator,
     * the same source text and have the same current position in iteration.
     * @param that object to test if it is equals to this
     *             CollationElementIterator
     */
    @Override
    public boolean equals(Object that) {
        if (that == this) {
            return true;
        }
        if (that instanceof CollationElementIterator) {
            CollationElementIterator thatceiter = (CollationElementIterator) that;
            return rbc_.equals(thatceiter.rbc_)
                    && otherHalf_ == thatceiter.otherHalf_
                    && normalizeDir() == thatceiter.normalizeDir()
                    && string_.equals(thatceiter.string_)
                    && iter_.equals(thatceiter.iter_);
        }
        return false;
    }

    /**
     * Mock implementation of hashCode(). This implementation always returns a constant
     * value. When Java assertion is enabled, this method triggers an assertion failure.
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Override
    @Deprecated
    public int hashCode() {
        assert false : "hashCode not designed";
        return 42;
    }

    /**
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public RuleBasedCollator getRuleBasedCollator() {
        return rbc_;
    }
}
