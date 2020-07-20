/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2014-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.impl;

import java.text.CharacterIterator;
import java.util.HashSet;
import java.util.Locale;

import android.icu.impl.ICUResourceBundle.OpenType;
import android.icu.text.BreakIterator;
import android.icu.text.FilteredBreakIteratorBuilder;
import android.icu.text.UCharacterIterator;
import android.icu.util.BytesTrie;
import android.icu.util.CharsTrie;
import android.icu.util.CharsTrieBuilder;
import android.icu.util.StringTrieBuilder;
import android.icu.util.ULocale;

/**
 * @author tomzhang
 * @hide Only a subset of ICU is exposed in Android
 */
public class SimpleFilteredSentenceBreakIterator extends BreakIterator {

    private BreakIterator delegate;
    private UCharacterIterator text; // TODO(Tom): suffice to move into the local scope in next() ?
    private CharsTrie backwardsTrie; // i.e. ".srM" for Mrs.
    private CharsTrie forwardsPartialTrie; // Has ".a" for "a.M."

    /**
     * @param adoptBreakIterator
     *            break iterator to adopt
     * @param forwardsPartialTrie
     *            forward & partial char trie to adopt
     * @param backwardsTrie
     *            backward trie to adopt
     */
    public SimpleFilteredSentenceBreakIterator(BreakIterator adoptBreakIterator, CharsTrie forwardsPartialTrie,
            CharsTrie backwardsTrie) {
        this.delegate = adoptBreakIterator;
        this.forwardsPartialTrie = forwardsPartialTrie;
        this.backwardsTrie = backwardsTrie;
    }


    /**
     * Reset the filter from the delegate.
     */
    private final void resetState() {
        text = UCharacterIterator.getInstance((CharacterIterator) delegate.getText().clone());
    }

    /**
     * Is there an exception at this point?
     *
     * @param n the location of the possible break
     * @return
     */
    private final boolean breakExceptionAt(int n) {
        // Note: the C++ version of this function is SimpleFilteredSentenceBreakIterator::breakExceptionAt()

        int bestPosn = -1;
        int bestValue = -1;

        // loops while 'n' points to an exception
        text.setIndex(n);
        backwardsTrie.reset();
        int uch;



        // Assume a space is following the '.' (so we handle the case: "Mr. /Brown")
        if ((uch = text.previousCodePoint()) == ' ') { // TODO: skip a class of chars here??
            // TODO only do this the 1st time?
        } else {
            uch = text.nextCodePoint();
        }

        BytesTrie.Result r = BytesTrie.Result.INTERMEDIATE_VALUE;

        while ((uch = text.previousCodePoint()) != UCharacterIterator.DONE && // more to consume backwards and..
                ((r = backwardsTrie.nextForCodePoint(uch)).hasNext())) {// more in the trie
            if (r.hasValue()) { // remember the best match so far
                bestPosn = text.getIndex();
                bestValue = backwardsTrie.getValue();
            }
        }

        if (r.matches()) { // exact match?
            bestValue = backwardsTrie.getValue();
            bestPosn = text.getIndex();
        }

        if (bestPosn >= 0) {
            if (bestValue == Builder.MATCH) { // exact match!
                return true; // Exception here.
            } else if (bestValue == Builder.PARTIAL && forwardsPartialTrie != null) {
                // make sure there's a forward trie
                // We matched the "Ph." in "Ph.D." - now we need to run everything through the forwards trie
                // to see if it matches something going forward.
                forwardsPartialTrie.reset();

                BytesTrie.Result rfwd = BytesTrie.Result.INTERMEDIATE_VALUE;
                text.setIndex(bestPosn); // hope that's close ..
                while ((uch = text.nextCodePoint()) != BreakIterator.DONE
                        && ((rfwd = forwardsPartialTrie.nextForCodePoint(uch)).hasNext())) {
                }
                if (rfwd.matches()) {
                    // Exception here
                    return true;
                } // else fall through
            } // else fall through
        } // else fall through
        return false; // No exception here.
    }

    /**
     * Given that the delegate has already given its "initial" answer,
     * find the NEXT actual (non-suppressed) break.
     * @param n initial position from delegate
     * @return new break position or BreakIterator.DONE
     */
    private final int internalNext(int n) {
        if (n == BreakIterator.DONE || // at end or
                backwardsTrie == null) { // .. no backwards table loaded == no exceptions
            return n;
        }
        resetState();

        final int textLen = text.getLength();

        while (n != BreakIterator.DONE && n != textLen) {
            // outer loop runs once per underlying break (from fDelegate).
            // loops while 'n' points to an exception.

            if (breakExceptionAt(n)) {
                // n points to a break exception
                n = delegate.next();
            } else {
                // no exception at this spot
                return n;
            }
        }
        return n; //hit underlying DONE or break at end of text
    }

    /**
     * Given that the delegate has already given its "initial" answer,
     * find the PREV actual (non-suppressed) break.
     * @param n initial position from delegate
     * @return new break position or BreakIterator.DONE
     */
    private final int internalPrev(int n) {
        if (n == 0 || n == BreakIterator.DONE || // at end or
                backwardsTrie == null) { // .. no backwards table loaded == no exceptions
            return n;
        }
        resetState();

        while (n != BreakIterator.DONE && n != 0) {
            // outer loop runs once per underlying break (from fDelegate).
            // loops while 'n' points to an exception.

            if (breakExceptionAt(n)) {
                // n points to a break exception
                n = delegate.previous();
            } else {
                // no exception at this spot
                return n;
            }
        }
        return n; //hit underlying DONE or break at end of text
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        SimpleFilteredSentenceBreakIterator other = (SimpleFilteredSentenceBreakIterator) obj;
        return delegate.equals(other.delegate) && text.equals(other.text) && backwardsTrie.equals(other.backwardsTrie)
                && forwardsPartialTrie.equals(other.forwardsPartialTrie);
    }

    @Override
    public int hashCode() {
        return (forwardsPartialTrie.hashCode() * 39) + (backwardsTrie.hashCode() * 11) + delegate.hashCode();
    }

    @Override
    public Object clone() {
        SimpleFilteredSentenceBreakIterator other = (SimpleFilteredSentenceBreakIterator) super.clone();
        return other;
    }


    @Override
    public int first() {
        // Don't suppress a break opportunity at the beginning of text.
        return delegate.first();
    }

    @Override
    public int preceding(int offset) {
        return internalPrev(delegate.preceding(offset));
    }

    @Override
    public int previous() {
        return internalPrev(delegate.previous());
    }

    @Override
    public int current() {
        return delegate.current();
    }

    @Override
    public boolean isBoundary(int offset) {
        if(!delegate.isBoundary(offset)) {
            return false; // No underlying break to suppress?
        }

        // delegate thinks there's a break…
        if(backwardsTrie == null) {
            return true; // no data
        }

        resetState();
        return !breakExceptionAt(offset); // if there's an exception: no break.
    }

    @Override
    public int next() {
        return internalNext(delegate.next());
    }

    @Override
    public int next(int n) {
        return internalNext(delegate.next(n));
    }

    @Override
    public int following(int offset) {
        return internalNext(delegate.following(offset));
    }

    @Override
    public int last() {
        // Don't suppress a break opportunity at the end of text.
        return delegate.last();
    }

    @Override
    public CharacterIterator getText() {
        return delegate.getText();
    }

    @Override
    public void setText(CharacterIterator newText) {
        delegate.setText(newText);
    }

    public static class Builder extends FilteredBreakIteratorBuilder {
        /**
         * filter set to store all exceptions
         */
        private HashSet<CharSequence> filterSet = new HashSet<CharSequence>();

        static final int PARTIAL = (1 << 0); // < partial - need to run through forward trie
        static final int MATCH = (1 << 1); // < exact match - skip this one.
        static final int SuppressInReverse = (1 << 0);
        static final int AddToForward = (1 << 1);

        public Builder(Locale loc) {
            this(ULocale.forLocale(loc));
        }
        /**
         * Create SimpleFilteredBreakIteratorBuilder using given locale
         * @param loc the locale to get filtered iterators
         */
        public Builder(ULocale loc) {
            ICUResourceBundle rb = ICUResourceBundle.getBundleInstance(
                    ICUData.ICU_BRKITR_BASE_NAME, loc, OpenType.LOCALE_ROOT);

            ICUResourceBundle breaks = rb.findWithFallback("exceptions/SentenceBreak");

            if (breaks != null) {
                for (int index = 0, size = breaks.getSize(); index < size; ++index) {
                    ICUResourceBundle b = (ICUResourceBundle) breaks.get(index);
                    String br = b.getString();
                    filterSet.add(br);
                }
            }
        }

        /**
         * Create SimpleFilteredBreakIteratorBuilder with no exception
         */
        public Builder() {
        }

        @Override
        public boolean suppressBreakAfter(CharSequence str) {
            return filterSet.add(str);
        }

        @Override
        public boolean unsuppressBreakAfter(CharSequence str) {
            return filterSet.remove(str);
        }

        @Override
        public BreakIterator wrapIteratorWithFilter(BreakIterator adoptBreakIterator) {
            if( filterSet.isEmpty() ) {
                // Short circuit - nothing to except.
                return adoptBreakIterator;
            }

            CharsTrieBuilder builder = new CharsTrieBuilder();
            CharsTrieBuilder builder2 = new CharsTrieBuilder();

            int revCount = 0;
            int fwdCount = 0;

            int subCount = filterSet.size();
            CharSequence[] ustrs = new CharSequence[subCount];
            int[] partials = new int[subCount];

            CharsTrie backwardsTrie = null; // i.e. ".srM" for Mrs.
            CharsTrie forwardsPartialTrie = null; // Has ".a" for "a.M."

            int i = 0;
            for (CharSequence s : filterSet) {
                ustrs[i] = s; // copy by value?
                partials[i] = 0; // default: no partial
                i++;
            }

            for (i = 0; i < subCount; i++) {
                String thisStr = ustrs[i].toString(); // TODO: don't cast to String?
                int nn = thisStr.indexOf('.'); // TODO: non-'.' abbreviations
                if (nn > -1 && (nn + 1) != thisStr.length()) {
                    // is partial.
                    // is it unique?
                    int sameAs = -1;
                    for (int j = 0; j < subCount; j++) {
                        if (j == i)
                            continue;
                        if (thisStr.regionMatches(0, ustrs[j].toString() /* TODO */, 0, nn + 1)) {
                            if (partials[j] == 0) { // hasn't been processed yet
                                partials[j] = SuppressInReverse | AddToForward;
                            } else if ((partials[j] & SuppressInReverse) != 0) {
                                sameAs = j; // the other entry is already in the reverse table.
                            }
                        }
                    }

                    if ((sameAs == -1) && (partials[i] == 0)) {
                        StringBuilder prefix = new StringBuilder(thisStr.substring(0, nn + 1));
                        // first one - add the prefix to the reverse table.
                        prefix.reverse();
                        builder.add(prefix, PARTIAL);
                        revCount++;
                        partials[i] = SuppressInReverse | AddToForward;
                    }
                }
            }

            for (i = 0; i < subCount; i++) {
                final String thisStr = ustrs[i].toString(); // TODO
                if (partials[i] == 0) {
                    StringBuilder reversed = new StringBuilder(thisStr).reverse();
                    builder.add(reversed, MATCH);
                    revCount++;
                } else {
                    // an optimization would be to only add the portion after the '.'
                    // for example, for "Ph.D." we store ".hP" in the reverse table. We could just store "D." in the
                    // forward,
                    // instead of "Ph.D." since we already know the "Ph." part is a match.
                    // would need the trie to be able to hold 0-length strings, though.
                    builder2.add(thisStr, MATCH); // forward
                    fwdCount++;
                }
            }

            if (revCount > 0) {
                backwardsTrie = builder.build(StringTrieBuilder.Option.FAST);
            }

            if (fwdCount > 0) {
                forwardsPartialTrie = builder2.build(StringTrieBuilder.Option.FAST);
            }
            return new SimpleFilteredSentenceBreakIterator(adoptBreakIterator, forwardsPartialTrie, backwardsTrie);
        }
    }
}
