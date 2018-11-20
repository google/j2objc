/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.text;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Locale;

import android.icu.util.ICUException;
import android.icu.util.ULocale;

// Java porting note:
//
//        The ICU4C implementation contains dead code in many places.
//      While porting the ICU4C linear search implementation, this dead code
//      was not fully ported. The code blocks tagged by "// *** Boyer-Moore ***"
//      are those dead code blocks, still available in ICU4C.

//        The ICU4C implementation does not seem to handle UCharacterIterator pointing
//      to a fragment of text properly. ICU4J uses CharacterIterator to navigate through
//      the input text. We need to carefully review the code ported from ICU4C
//      assuming the start index is 0.

//        ICU4C implementation initializes pattern.CE and pattern.PCE. It looks like
//      CE is no longer used, except in a few places checking CELength. It looks like this
//      is a leftover from already-disabled Boyer-Moore search code. This Java implementation
//      preserves the code, but we should clean this up later.

/** 
 *
 * <tt>StringSearch</tt> is a {@link SearchIterator} that provides
 * language-sensitive text searching based on the comparison rules defined
 * in a {@link RuleBasedCollator} object.
 * StringSearch ensures that language eccentricity can be
 * handled, e.g. for the German collator, characters &szlig; and SS will be matched
 * if case is chosen to be ignored.
 * See the <a href="http://source.icu-project.org/repos/icu/icuhtml/trunk/design/collation/ICU_collation_design.htm">
 * "ICU Collation Design Document"</a> for more information.
 * <p>
 * There are 2 match options for selection:<br>
 * Let S' be the sub-string of a text string S between the offsets start and
 * end [start, end].
 * <br>
 * A pattern string P matches a text string S at the offsets [start, end]
 * if
 * <pre> 
 * option 1. Some canonical equivalent of P matches some canonical equivalent
 *           of S'
 * option 2. P matches S' and if P starts or ends with a combining mark,
 *           there exists no non-ignorable combining mark before or after S?
 *           in S respectively.
 * </pre>
 * Option 2. is the default.
 * <p>
 * This search has APIs similar to that of other text iteration mechanisms 
 * such as the break iterators in {@link BreakIterator}. Using these 
 * APIs, it is easy to scan through text looking for all occurrences of 
 * a given pattern. This search iterator allows changing of direction by 
 * calling a {@link #reset} followed by a {@link #next} or {@link #previous}.
 * Though a direction change can occur without calling {@link #reset} first,
 * this operation comes with some speed penalty.
 * Match results in the forward direction will match the result matches in
 * the backwards direction in the reverse order
 * <p>
 * {@link SearchIterator} provides APIs to specify the starting position
 * within the text string to be searched, e.g. {@link SearchIterator#setIndex setIndex},
 * {@link SearchIterator#preceding preceding} and {@link SearchIterator#following following}.
 * Since the starting position will be set as it is specified, please take note that
 * there are some danger points at which the search may render incorrect
 * results:
 * <ul>
 * <li> In the midst of a substring that requires normalization.
 * <li> If the following match is to be found, the position should not be the
 *      second character which requires swapping with the preceding
 *      character. Vice versa, if the preceding match is to be found, the
 *      position to search from should not be the first character which
 *      requires swapping with the next character. E.g certain Thai and
 *      Lao characters require swapping.
 * <li> If a following pattern match is to be found, any position within a
 *      contracting sequence except the first will fail. Vice versa if a
 *      preceding pattern match is to be found, an invalid starting point
 *      would be any character within a contracting sequence except the last.
 * </ul>
 * <p>
 * A {@link BreakIterator} can be used if only matches at logical breaks are desired.
 * Using a {@link BreakIterator} will only give you results that exactly matches the
 * boundaries given by the {@link BreakIterator}. For instance the pattern "e" will
 * not be found in the string "\u00e9" if a character break iterator is used.
 * <p>
 * Options are provided to handle overlapping matches.
 * E.g. In English, overlapping matches produces the result 0 and 2
 * for the pattern "abab" in the text "ababab", where mutually
 * exclusive matches only produces the result of 0.
 * <p>
 * Options are also provided to implement "asymmetric search" as described in
 * <a href="http://www.unicode.org/reports/tr10/#Asymmetric_Search">
 * UTS #10 Unicode Collation Algorithm</a>, specifically the ElementComparisonType
 * values.
 * <p>
 * Though collator attributes will be taken into consideration while
 * performing matches, there are no APIs here for setting and getting the
 * attributes. These attributes can be set by getting the collator
 * from {@link #getCollator} and using the APIs in {@link RuleBasedCollator}.
 * Lastly to update <tt>StringSearch</tt> to the new collator attributes,
 * {@link #reset} has to be called.
 * <p> 
 * Restriction: <br>
 * Currently there are no composite characters that consists of a
 * character with combining class &gt; 0 before a character with combining
 * class == 0. However, if such a character exists in the future,
 * <tt>StringSearch</tt> does not guarantee the results for option 1.
 * <p>
 * Consult the {@link SearchIterator} documentation for information on
 * and examples of how to use instances of this class to implement text
 * searching.
 * <p>
 * Note, <tt>StringSearch</tt> is not to be subclassed.
 * </p>
 * @see SearchIterator
 * @see RuleBasedCollator
 * @author Laura Werner, synwee
 */
// internal notes: all methods do not guarantee the correct status of the 
// characteriterator. the caller has to maintain the original index position
// if necessary. methods could change the index position as it deems fit
public final class StringSearch extends SearchIterator {

    private Pattern pattern_;
    private RuleBasedCollator collator_;

    // positions within the collation element iterator is used to determine
    // if we are at the start of the text.
    private CollationElementIterator textIter_;
    private CollationPCE textProcessedIter_;

    // utility collation element, used throughout program for temporary
    // iteration.
    private CollationElementIterator utilIter_;

    private Normalizer2 nfd_;

    private int strength_;
    int ceMask_;
    int variableTop_;

    private boolean toShift_;

    // *** Boyer-Moore ***
    // private char[] canonicalPrefixAccents_;
    // private char[] canonicalSuffixAccents_;

    /**
     * Initializes the iterator to use the language-specific rules defined in 
     * the argument collator to search for argument pattern in the argument 
     * target text. The argument <code>breakiter</code> is used to define logical matches.
     * See super class documentation for more details on the use of the target 
     * text and {@link BreakIterator}.
     * @param pattern text to look for.
     * @param target target text to search for pattern. 
     * @param collator {@link RuleBasedCollator} that defines the language rules
     * @param breakiter A {@link BreakIterator} that is used to determine the 
     *                boundaries of a logical match. This argument can be null.
     * @throws IllegalArgumentException thrown when argument target is null,
     *            or of length 0
     * @see BreakIterator
     * @see RuleBasedCollator
     */
    public StringSearch(String pattern, CharacterIterator target, RuleBasedCollator collator,
            BreakIterator breakiter) {

        // This implementation is ported from ICU4C usearch_open()

        super(target, breakiter);

        // string search does not really work when numeric collation is turned on
        if (collator.getNumericCollation()) {
            throw new UnsupportedOperationException("Numeric collation is not supported by StringSearch");
        }

        collator_ = collator;
        strength_ = collator.getStrength();
        ceMask_ = getMask(strength_);
        toShift_ = collator.isAlternateHandlingShifted();
        variableTop_ = collator.getVariableTop();

        nfd_ = Normalizer2.getNFDInstance();

        pattern_ = new Pattern(pattern);

        search_.setMatchedLength(0);
        search_.matchedIndex_ = DONE;

        utilIter_ = null;
        textIter_ = new CollationElementIterator(target, collator);

        textProcessedIter_ = null;

        // This is done by super class constructor
        /*
        search_.isOverlap_ = false;
        search_.isCanonicalMatch_ = false;
        search_.elementComparisonType_ = ElementComparisonType.STANDARD_ELEMENT_COMPARISON;
        search_.isForwardSearching_ = true;
        search_.reset_ = true;
         */
        ULocale collLocale = collator.getLocale(ULocale.VALID_LOCALE);
        search_.internalBreakIter_ = BreakIterator.getCharacterInstance(collLocale == null ? ULocale.ROOT : collLocale);
        search_.internalBreakIter_.setText((CharacterIterator)target.clone());  // We need to create a clone

        initialize();
    }

    /**
     * Initializes the iterator to use the language-specific rules defined in 
     * the argument collator to search for argument pattern in the argument 
     * target text. No {@link BreakIterator}s are set to test for logical matches.
     * @param pattern text to look for.
     * @param target target text to search for pattern. 
     * @param collator {@link RuleBasedCollator} that defines the language rules
     * @throws IllegalArgumentException thrown when argument target is null,
     *            or of length 0
     * @see RuleBasedCollator
     */
    public StringSearch(String pattern, CharacterIterator target, RuleBasedCollator collator) {
        this(pattern, target, collator, null);
    }

    /**
     * Initializes the iterator to use the language-specific rules and 
     * break iterator rules defined in the argument locale to search for 
     * argument pattern in the argument target text. 
     * @param pattern text to look for.
     * @param target target text to search for pattern. 
     * @param locale locale to use for language and break iterator rules
     * @throws IllegalArgumentException thrown when argument target is null,
     *            or of length 0. ClassCastException thrown if the collator for 
     *            the specified locale is not a RuleBasedCollator.
     */
    public StringSearch(String pattern, CharacterIterator target, Locale locale) {
        this(pattern, target, ULocale.forLocale(locale));
    }

    /**
     * Initializes the iterator to use the language-specific rules and 
     * break iterator rules defined in the argument locale to search for 
     * argument pattern in the argument target text. 
     * See super class documentation for more details on the use of the target 
     * text and {@link BreakIterator}.
     * @param pattern text to look for.
     * @param target target text to search for pattern. 
     * @param locale locale to use for language and break iterator rules
     * @throws IllegalArgumentException thrown when argument target is null,
     *            or of length 0. ClassCastException thrown if the collator for 
     *            the specified locale is not a RuleBasedCollator.
     * @see BreakIterator
     * @see RuleBasedCollator
     * @see SearchIterator
     */
    public StringSearch(String pattern, CharacterIterator target, ULocale locale) {
        this(pattern, target, (RuleBasedCollator) Collator.getInstance(locale), null);
    }

    /**
     * Initializes the iterator to use the language-specific rules and 
     * break iterator rules defined in the default locale to search for 
     * argument pattern in the argument target text.
     * @param pattern text to look for.
     * @param target target text to search for pattern. 
     * @throws IllegalArgumentException thrown when argument target is null,
     *            or of length 0. ClassCastException thrown if the collator for 
     *            the default locale is not a RuleBasedCollator.
     */
    public StringSearch(String pattern, String target) {
        this(pattern, new StringCharacterIterator(target),
                (RuleBasedCollator) Collator.getInstance(), null);
    }

    /**
     * Gets the {@link RuleBasedCollator} used for the language rules.
     * <p>
     * Since <tt>StringSearch</tt> depends on the returned {@link RuleBasedCollator}, any 
     * changes to the {@link RuleBasedCollator} result should follow with a call to 
     * either {@link #reset()} or {@link #setCollator(RuleBasedCollator)} to ensure the correct 
     * search behavior.
     * </p>
     * @return {@link RuleBasedCollator} used by this <tt>StringSearch</tt>
     * @see RuleBasedCollator
     * @see #setCollator
     */
    public RuleBasedCollator getCollator() {
        return collator_;
    }

    /**
     * Sets the {@link RuleBasedCollator} to be used for language-specific searching.
     * <p>
     * The iterator's position will not be changed by this method.
     * @param collator to use for this <tt>StringSearch</tt>
     * @throws IllegalArgumentException thrown when collator is null
     * @see #getCollator
     */
    public void setCollator(RuleBasedCollator collator) {
        if (collator == null) {
            throw new IllegalArgumentException("Collator can not be null");
        }
        collator_ = collator;
        ceMask_ = getMask(collator_.getStrength());

        ULocale collLocale = collator.getLocale(ULocale.VALID_LOCALE);
        search_.internalBreakIter_ = BreakIterator.getCharacterInstance(collLocale == null ? ULocale.ROOT : collLocale);
        search_.internalBreakIter_.setText((CharacterIterator)search_.text().clone());  // We need to create a clone

        toShift_ = collator.isAlternateHandlingShifted();
        variableTop_ = collator.getVariableTop();
        textIter_ = new CollationElementIterator(pattern_.text_, collator);
        utilIter_ = new CollationElementIterator(pattern_.text_, collator);

        // initialize() _after_ setting the iterators for the new collator.
        initialize();
    }

    /**
     * Returns the pattern for which <tt>StringSearch</tt> is searching for.
     * @return the pattern searched for
     */
    public String getPattern() {
        return pattern_.text_;
    }

    /**
     * Set the pattern to search for.  
     * The iterator's position will not be changed by this method.
     * @param pattern for searching
     * @see #getPattern
     * @exception IllegalArgumentException thrown if pattern is null or of
     *               length 0
     */
    public void setPattern(String pattern) {
        if (pattern == null || pattern.length() <= 0) {
            throw new IllegalArgumentException(
                    "Pattern to search for can not be null or of length 0");
        }
        pattern_.text_ = pattern;
        initialize();
    }

    /**
     * Determines whether canonical matches (option 1, as described in the 
     * class documentation) is set.
     * See setCanonical(boolean) for more information.
     * @see #setCanonical
     * @return true if canonical matches is set, false otherwise
     */
    //TODO: hoist this to SearchIterator
    public boolean isCanonical() {
        return search_.isCanonicalMatch_;
    }

    /**
     * Set the canonical match mode. See class documentation for details.
     * The default setting for this property is false.
     * @param allowCanonical flag indicator if canonical matches are allowed
     * @see #isCanonical
     */
    //TODO: hoist this to SearchIterator
    public void setCanonical(boolean allowCanonical) {
        search_.isCanonicalMatch_ = allowCanonical;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTarget(CharacterIterator text) {
        super.setTarget(text);
        textIter_.setText(text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIndex() {
        int result = textIter_.getOffset();
        if (isOutOfBounds(search_.beginIndex(), search_.endIndex(), result)) {
            return DONE;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setIndex(int position) {
        // Java porting note: This method is equivalent to setOffset() in ICU4C.
        // ICU4C SearchIterator::setOffset() is a pure virtual method, while
        // ICU4J SearchIterator.setIndex() is not abstract method.

        super.setIndex(position);
        textIter_.setOffset(position);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        // reset is setting the attributes that are already in
        // string search, hence all attributes in the collator should
        // be retrieved without any problems

        boolean sameCollAttribute = true;
        int ceMask;
        boolean shift;
        int varTop;

        // **** hack to deal w/ how processed CEs encode quaternary ****
        int newStrength = collator_.getStrength();
        if ((strength_ < Collator.QUATERNARY && newStrength >= Collator.QUATERNARY)
                || (strength_ >= Collator.QUATERNARY && newStrength < Collator.QUATERNARY)) {
            sameCollAttribute = false;
        }

        strength_ = collator_.getStrength();
        ceMask = getMask(strength_);
        if (ceMask_ != ceMask) {
            ceMask_ = ceMask;
            sameCollAttribute = false;
        }

        shift = collator_.isAlternateHandlingShifted();
        if (toShift_ != shift) {
            toShift_ = shift;
            sameCollAttribute = false;
        }

        varTop = collator_.getVariableTop();
        if (variableTop_ != varTop) {
            variableTop_ = varTop;
            sameCollAttribute = false;
        }

        if (!sameCollAttribute) {
            initialize();
        }

        textIter_.setText(search_.text());

        search_.setMatchedLength(0);
        search_.matchedIndex_ = DONE;
        search_.isOverlap_ = false;
        search_.isCanonicalMatch_ = false;
        search_.elementComparisonType_ = ElementComparisonType.STANDARD_ELEMENT_COMPARISON;
        search_.isForwardSearching_ = true;
        search_.reset_ = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int handleNext(int position) {
        if (pattern_.CELength_ == 0) {
            search_.matchedIndex_ = search_.matchedIndex_ == DONE ?
                                    getIndex() : search_.matchedIndex_ + 1;
            search_.setMatchedLength(0);
            textIter_.setOffset(search_.matchedIndex_);
            if (search_.matchedIndex_ == search_.endIndex()) {
                search_.matchedIndex_ = DONE;
            }
        } else {
            if (search_.matchedLength() <= 0) {
                // the flipping direction issue has already been handled
                // in next()
                // for boundary check purposes. this will ensure that the
                // next match will not preceed the current offset
                // note search_.matchedIndex_ will always be set to something
                // in the code
                search_.matchedIndex_ = position - 1;
            }

            textIter_.setOffset(position);

            // ICU4C comment:
            // if strsrch_->breakIter is always the same as m_breakiterator_
            // then we don't need to check the match boundaries here because
            // usearch_handleNextXXX will already have done it.
            if (search_.isCanonicalMatch_) {
                // *could* actually use exact here 'cause no extra accents allowed...
                handleNextCanonical();
            } else {
                handleNextExact();
            }

            if (search_.matchedIndex_ == DONE) {
                textIter_.setOffset(search_.endIndex());
            } else {
                textIter_.setOffset(search_.matchedIndex_);
            }

            return search_.matchedIndex_;
        }

        return DONE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int handlePrevious(int position) {
        if (pattern_.CELength_ == 0) {
            search_.matchedIndex_ =
                    search_.matchedIndex_ == DONE ? getIndex() : search_.matchedIndex_;
            if (search_.matchedIndex_ == search_.beginIndex()) {
                setMatchNotFound();
            } else {
                search_.matchedIndex_--;
                textIter_.setOffset(search_.matchedIndex_);
                search_.setMatchedLength(0);
            }
        } else {
            textIter_.setOffset(position);

            if (search_.isCanonicalMatch_) {
                // *could* use exact match here since extra accents *not* allowed!
                handlePreviousCanonical();
            } else {
                handlePreviousExact();
            }
        }

        return search_.matchedIndex_;
    }

    // ------------------ Internal implementation code ---------------------------

    private static final int INITIAL_ARRAY_SIZE_ = 256;

    // *** Boyer-Moore ***
    // private static final Normalizer2Impl nfcImpl_ = Norm2AllModes.getNFCInstance().impl;
    // private static final int LAST_BYTE_MASK_ = 0xff;
    // private static final int SECOND_LAST_BYTE_SHIFT_ = 8;

    private static final int PRIMARYORDERMASK = 0xffff0000;
    private static final int SECONDARYORDERMASK = 0x0000ff00;
    private static final int TERTIARYORDERMASK = 0x000000ff;

    /**
     * Getting the mask for collation strength
     * @param strength collation strength
     * @return collation element mask
     */
    private static int getMask(int strength) {
        switch (strength) {
        case Collator.PRIMARY:
            return PRIMARYORDERMASK;
        case Collator.SECONDARY:
            return SECONDARYORDERMASK | PRIMARYORDERMASK;
        default:
            return TERTIARYORDERMASK | SECONDARYORDERMASK | PRIMARYORDERMASK;
        }
    }


    // *** Boyer-Moore ***
    /*
    private final char getFCD(String str, int offset) {
        char ch = str.charAt(offset);
        if (ch < 0x180) {
            return (char) nfcImpl_.getFCD16FromBelow180(ch);
        } else if (nfcImpl_.singleLeadMightHaveNonZeroFCD16(ch)) {
            if (!Character.isHighSurrogate(ch)) {
                return (char) nfcImpl_.getFCD16FromNormData(ch);
            } else {
                char c2;
                if (++offset < str.length() && Character.isLowSurrogate(c2 = str.charAt(offset))) {
                    return (char) nfcImpl_.getFCD16FromNormData(Character.toCodePoint(ch, c2));
                }
            }
        }
        return 0;
    }

    private final char getFCD(int c) {
        return (char)nfcImpl_.getFCD16(c);
    }
    */

    /**
     * Getting the modified collation elements taking into account the collation
     * attributes.
     * 
     * @param sourcece
     * @return the modified collation element
     */
    private int getCE(int sourcece) {
        // note for tertiary we can't use the collator->tertiaryMask, that
        // is a preprocessed mask that takes into account case options. since
        // we are only concerned with exact matches, we don't need that.
        sourcece &= ceMask_;

        if (toShift_) {
            // alternate handling here, since only the 16 most significant digits
            // is only used, we can safely do a compare without masking
            // if the ce is a variable, we mask and get only the primary values
            // no shifting to quartenary is required since all primary values
            // less than variabletop will need to be masked off anyway.
            if (variableTop_ > sourcece) {
                if (strength_ >= Collator.QUATERNARY) {
                    sourcece &= PRIMARYORDERMASK;
                } else {
                    sourcece = CollationElementIterator.IGNORABLE;
                }
            }
        } else if (strength_ >= Collator.QUATERNARY && sourcece == CollationElementIterator.IGNORABLE) {
            sourcece = 0xFFFF;
        }

        return sourcece;
    }

    /**
     * Direct port of ICU4C static int32_t * addTouint32_tArray(...) in usearch.cpp.
     * This is used for appending a PCE to Pattern.PCE_ buffer. We probably should
     * implement this in Pattern class.
     * 
     * @param destination target array
     * @param offset destination offset to add value
     * @param destinationlength target array size
     * @param value to be added
     * @param increments incremental size expected
     * @return new destination array, destination if there was no new allocation
     */
    private static int[] addToIntArray(int[] destination, int offset, int destinationlength,
            int value, int increments) {
        int newlength = destinationlength;
        if (offset + 1 == newlength) {
            newlength += increments;
            int temp[] = new int[newlength];
            System.arraycopy(destination, 0, temp, 0, offset);
            destination = temp;
        }
        destination[offset] = value;
        return destination;
    }

    /**
     * Direct port of ICU4C static int64_t * addTouint64_tArray(...) in usearch.cpp.
     * This is used for appending a PCE to Pattern.PCE_ buffer. We probably should
     * implement this in Pattern class.
     * 
     * @param destination target array
     * @param offset destination offset to add value
     * @param destinationlength target array size
     * @param value to be added
     * @param increments incremental size expected
     * @return new destination array, destination if there was no new allocation
     */
    private static long[] addToLongArray(long[] destination, int offset, int destinationlength,
            long value, int increments) {
        int newlength = destinationlength;
        if (offset + 1 == newlength) {
            newlength += increments;
            long temp[] = new long[newlength];
            System.arraycopy(destination, 0, temp, 0, offset);
            destination = temp;
        }
        destination[offset] = value;
        return destination;
    }

    /**
     * Initializing the ce table for a pattern.
     * Stores non-ignorable collation keys.
     * Table size will be estimated by the size of the pattern text. Table
     * expansion will be perform as we go along. Adding 1 to ensure that the table
     * size definitely increases.
     * @return total number of expansions
     */
    // TODO: We probably do not need Pattern CE table.
    private int initializePatternCETable() {
        int[] cetable = new int[INITIAL_ARRAY_SIZE_];
        int cetablesize = cetable.length;
        int patternlength = pattern_.text_.length();
        CollationElementIterator coleiter = utilIter_;

        if (coleiter == null) {
            coleiter = new CollationElementIterator(pattern_.text_, collator_);
            utilIter_ = coleiter;
        } else {
            coleiter.setText(pattern_.text_);
        }

        int offset = 0;
        int result = 0;
        int ce;

        while ((ce = coleiter.next()) != CollationElementIterator.NULLORDER) {
            int newce = getCE(ce);
            if (newce != CollationElementIterator.IGNORABLE /* 0 */) {
                int[] temp = addToIntArray(cetable, offset, cetablesize, newce,
                        patternlength - coleiter.getOffset() + 1);
                offset++;
                cetable = temp;
            }
            result += (coleiter.getMaxExpansion(ce) - 1);
        }

        cetable[offset] = 0;
        pattern_.CE_ = cetable;
        pattern_.CELength_ = offset;

        return result;
    }

    /**
     * Initializing the pce table for a pattern.
     * Stores non-ignorable collation keys.
     * Table size will be estimated by the size of the pattern text. Table
     * expansion will be perform as we go along. Adding 1 to ensure that the table
     * size definitely increases.
     * @return total number of expansions
     */
    private int initializePatternPCETable() {
        long[] pcetable = new long[INITIAL_ARRAY_SIZE_];
        int pcetablesize = pcetable.length;
        int patternlength = pattern_.text_.length();
        CollationElementIterator coleiter = utilIter_;

        if (coleiter == null) {
            coleiter = new CollationElementIterator(pattern_.text_, collator_);
            utilIter_ = coleiter;
        } else {
            coleiter.setText(pattern_.text_);
        }

        int offset = 0;
        int result = 0;
        long pce;

        CollationPCE iter = new CollationPCE(coleiter);

        // ** Should processed CEs be signed or unsigned?
        // ** (the rest of the code in this file seems to play fast-and-loose with
        // ** whether a CE is signed or unsigned. For example, look at routine above this one.)
        while ((pce = iter.nextProcessed(null)) != CollationPCE.PROCESSED_NULLORDER) {
            long[] temp = addToLongArray(pcetable, offset, pcetablesize, pce, patternlength - coleiter.getOffset() + 1);
            offset++;
            pcetable = temp;
        }

        pcetable[offset] = 0;
        pattern_.PCE_ = pcetable;
        pattern_.PCELength_ = offset;

        return result;
    }

    // TODO: This method only triggers initializePatternCETable(), which is probably no
    //      longer needed.
    private int initializePattern() {
        // Since the strength is primary, accents are ignored in the pattern.

        // *** Boyer-Moore ***
        /*
        if (strength_ == Collator.PRIMARY) {
            pattern_.hasPrefixAccents_ = false;
            pattern_.hasSuffixAccents_ = false;
        } else {
            pattern_.hasPrefixAccents_ = (getFCD(pattern_.text_, 0) >>> SECOND_LAST_BYTE_SHIFT_) != 0;
            pattern_.hasSuffixAccents_ = (getFCD(pattern_.text_.codePointBefore(pattern_.text_.length())) & LAST_BYTE_MASK_) != 0;
        }
        */

        pattern_.PCE_ = null;

        // since intializePattern is an internal method status is a success.
        return initializePatternCETable();
    }

    // *** Boyer-Moore ***
    /*
     private final void setShiftTable(char shift[], 
                                         char backshift[], 
                                         int cetable[], int cesize, 
                                         int expansionsize,
                                         int defaultforward,
                                         int defaultbackward) {
         // No implementation
     }
     */

    // TODO: This method only triggers initializePattern(), which is probably no
    //      longer needed.
    private void initialize() {
        /* int expandlength = */ initializePattern();

        // *** Boyer-Moore ***
        /*
        if (pattern_.CELength_ > 0) {
            int cesize = pattern_.CELength_;
            int minlength = cesize > expandlength ? cesize - expandlength : 1;
            pattern_.defaultShiftSize_ = minlength;
            setShiftTable(pattern_.shift_, pattern_.backShift_, pattern_.CE_, cesize,
                    expandlength, minlength, minlength);
            return;
        }
        return pattern_.defaultShiftSize_;
        */
    }

    /**
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected void setMatchNotFound() {
        super.setMatchNotFound();
        // SearchIterator#setMatchNotFound() does following:
        //      search_.matchedIndex_ = DONE;
        //      search_.setMatchedLength(0);
        if (search_.isForwardSearching_) {
            textIter_.setOffset(search_.text().getEndIndex());
        } else {
            textIter_.setOffset(0);
        }
    }

    /**
     * Checks if the offset runs out of the text string range
     * @param textstart offset of the first character in the range
     * @param textlimit limit offset of the text string range
     * @param offset to test
     * @return true if offset is out of bounds, false otherwise
     */
    private static final boolean isOutOfBounds(int textstart, int textlimit, int offset) {
        return offset < textstart || offset > textlimit;
    }

    /**
     * Checks for identical match
     * @param start offset of possible match
     * @param end offset of possible match
     * @return TRUE if identical match is found
     */
    private boolean checkIdentical(int start, int end) {
        if (strength_ != Collator.IDENTICAL) {
            return true;
        }
        // Note: We could use Normalizer::compare() or similar, but for short strings
        // which may not be in FCD it might be faster to just NFD them.
        String textstr = getString(targetText, start, end - start);
        if (Normalizer.quickCheck(textstr, Normalizer.NFD, 0) == Normalizer.NO) {
            textstr = Normalizer.decompose(textstr, false);
        }
        String patternstr = pattern_.text_;
        if (Normalizer.quickCheck(patternstr, Normalizer.NFD, 0) == Normalizer.NO) {
            patternstr = Normalizer.decompose(patternstr, false);
        }
        return textstr.equals(patternstr);
    }

    private boolean initTextProcessedIter() {
        if (textProcessedIter_ == null) {
            textProcessedIter_ = new CollationPCE(textIter_);
        } else {
            textProcessedIter_.init(textIter_);
        }
        return true;
    }

    /*
     * Find the next break boundary after startIndex. If the UStringSearch object
     * has an external break iterator, use that. Otherwise use the internal character
     * break iterator.
     */
    private int nextBoundaryAfter(int startIndex) {
        BreakIterator breakiterator = search_.breakIter();

        if (breakiterator == null) {
            breakiterator = search_.internalBreakIter_;
        }

        if (breakiterator != null) {
            return breakiterator.following(startIndex);
        }

        return startIndex;
    }

    /*
     * Returns TRUE if index is on a break boundary. If the UStringSearch
     * has an external break iterator, test using that, otherwise test
     * using the internal character break iterator.
     */
    private boolean isBreakBoundary(int index) {
        BreakIterator breakiterator = search_.breakIter();

        if (breakiterator == null) {
            breakiterator = search_.internalBreakIter_;
        }

        return (breakiterator != null && breakiterator.isBoundary(index));
    }


    // Java porting note: Followings are corresponding to UCompareCEsResult enum
    private static final int CE_MATCH = -1;
    private static final int CE_NO_MATCH = 0;
    private static final int CE_SKIP_TARG = 1;
    private static final int CE_SKIP_PATN = 2;

    private static int CE_LEVEL2_BASE = 0x00000005;
    private static int CE_LEVEL3_BASE = 0x00050000;

    private static int compareCE64s(long targCE, long patCE, ElementComparisonType compareType) {
        if (targCE == patCE) {
            return CE_MATCH;
        }
        if (compareType == ElementComparisonType.STANDARD_ELEMENT_COMPARISON) {
            return CE_NO_MATCH;
        }

        long targCEshifted = targCE >>> 32;
        long patCEshifted = patCE >>> 32;
        long mask;

        mask = 0xFFFF0000L;
        int targLev1 = (int)(targCEshifted & mask);
        int patLev1 = (int)(patCEshifted & mask);
        if (targLev1 != patLev1) {
            if (targLev1 == 0) {
                return CE_SKIP_TARG;
            }
            if (patLev1 == 0
                    && compareType == ElementComparisonType.ANY_BASE_WEIGHT_IS_WILDCARD) {
                return CE_SKIP_PATN;
            }
            return CE_NO_MATCH;
        }

        mask = 0x0000FFFFL;
        int targLev2 = (int)(targCEshifted & mask);
        int patLev2 = (int)(patCEshifted & mask);
        if (targLev2 != patLev2) {
            if (targLev2 == 0) {
                return CE_SKIP_TARG;
            }
            if (patLev2 == 0
                    && compareType == ElementComparisonType.ANY_BASE_WEIGHT_IS_WILDCARD) {
                return CE_SKIP_PATN;
            }
            return (patLev2 == CE_LEVEL2_BASE ||
                    (compareType == ElementComparisonType.ANY_BASE_WEIGHT_IS_WILDCARD &&
                        targLev2 == CE_LEVEL2_BASE)) ? CE_MATCH : CE_NO_MATCH;
        }

        mask = 0xFFFF0000L;
        int targLev3 = (int)(targCE & mask);
        int patLev3 = (int)(patCE & mask);
        if (targLev3 != patLev3) {
            return (patLev3 == CE_LEVEL3_BASE ||
                    (compareType == ElementComparisonType.ANY_BASE_WEIGHT_IS_WILDCARD &&
                        targLev3 == CE_LEVEL3_BASE) )? CE_MATCH: CE_NO_MATCH;
        }

        return CE_MATCH;
    }

    /**
     * An object used for receiving matched index in search() and
     * searchBackwards().
     */
    private static class Match {
        int start_ = -1;
        int limit_ = -1;
    }

    private boolean search(int startIdx, Match m) {
        // Input parameter sanity check.
        if (pattern_.CELength_ == 0
                || startIdx < search_.beginIndex()
                || startIdx > search_.endIndex()) {
            throw new IllegalArgumentException("search(" + startIdx + ", m) - expected position to be between " +
                    search_.beginIndex() + " and " + search_.endIndex());
        }

        if (pattern_.PCE_ == null) {
            initializePatternPCETable();
        }

        textIter_.setOffset(startIdx);
        CEBuffer ceb = new CEBuffer(this);

        int targetIx = 0;
        CEI targetCEI = null;
        int patIx;
        boolean found;

        int mStart = -1;
        int mLimit = -1;
        int minLimit;
        int maxLimit;

        // Outer loop moves over match starting positions in the
        //      target CE space.
        // Here we see the target as a sequence of collation elements, resulting from the following:
        // 1. Target characters were decomposed, and (if appropriate) other compressions and expansions are applied
        //    (for example, digraphs such as IJ may be broken into two characters).
        // 2. An int64_t CE weight is determined for each resulting unit (high 16 bits are primary strength, next
        //    16 bits are secondary, next 16 (the high 16 bits of the low 32-bit half) are tertiary. Any of these
        //    fields that are for strengths below that of the collator are set to 0. If this makes the int64_t
        //    CE weight 0 (as for a combining diacritic with secondary weight when the collator strentgh is primary),
        //    then the CE is deleted, so the following code sees only CEs that are relevant.
        // For each CE, the lowIndex and highIndex correspond to where this CE begins and ends in the original text.
        // If lowIndex==highIndex, either the CE resulted from an expansion/decomposition of one of the original text
        // characters, or the CE marks the limit of the target text (in which case the CE weight is UCOL_PROCESSED_NULLORDER).
        for (targetIx = 0; ; targetIx++) {
            found = true;
            // Inner loop checks for a match beginning at each
            // position from the outer loop.
            int targetIxOffset = 0;
            long patCE = 0;
            // For targetIx > 0, this ceb.get gets a CE that is as far back in the ring buffer
            // (compared to the last CE fetched for the previous targetIx value) as we need to go
            // for this targetIx value, so if it is non-NULL then other ceb.get calls should be OK.
            CEI firstCEI = ceb.get(targetIx);
            if (firstCEI == null) {
                throw new ICUException("CEBuffer.get(" + targetIx + ") returned null.");
            }

            for (patIx = 0; patIx < pattern_.PCELength_; patIx++) {
                patCE = pattern_.PCE_[patIx];
                targetCEI = ceb.get(targetIx + patIx + targetIxOffset);
                // Compare CE from target string with CE from the pattern.
                // Note that the target CE will be UCOL_PROCESSED_NULLORDER if we reach the end of input,
                // which will fail the compare, below.
                int ceMatch = compareCE64s(targetCEI.ce_, patCE, search_.elementComparisonType_);
                if (ceMatch == CE_NO_MATCH) {
                    found = false;
                    break;
                } else if (ceMatch > CE_NO_MATCH) {
                    if (ceMatch == CE_SKIP_TARG) {
                        // redo with same patCE, next targCE
                        patIx--;
                        targetIxOffset++;
                    } else { // ceMatch == CE_SKIP_PATN
                        // redo with same targCE, next patCE
                        targetIxOffset--;
                    }
                }
            }
            targetIxOffset += pattern_.PCELength_; // this is now the offset in target CE space to end of the match so far

            if (!found && ((targetCEI == null) || (targetCEI.ce_ != CollationPCE.PROCESSED_NULLORDER))) {
                // No match at this targetIx.  Try again at the next.
                continue;
            }

            if (!found) {
                // No match at all, we have run off the end of the target text.
                break;
            }

            // We have found a match in CE space.
            // Now determine the bounds in string index space.
            // There still is a chance of match failure if the CE range not correspond to
            // an acceptable character range.
            //
            CEI lastCEI = ceb.get(targetIx + targetIxOffset -1);

            mStart = firstCEI.lowIndex_;
            minLimit = lastCEI.lowIndex_;

            // Look at the CE following the match.  If it is UCOL_NULLORDER the match
            // extended to the end of input, and the match is good.

            // Look at the high and low indices of the CE following the match. If
            // they are the same it means one of two things:
            //    1. The match extended to the last CE from the target text, which is OK, or
            //    2. The last CE that was part of the match is in an expansion that extends
            //       to the first CE after the match. In this case, we reject the match.
            CEI nextCEI = null;
            if (search_.elementComparisonType_ == ElementComparisonType.STANDARD_ELEMENT_COMPARISON) {
                nextCEI = ceb.get(targetIx + targetIxOffset);
                maxLimit = nextCEI.lowIndex_;
                if (nextCEI.lowIndex_ == nextCEI.highIndex_ && nextCEI.ce_ != CollationPCE.PROCESSED_NULLORDER) {
                    found = false;
                }
            } else {
                for (;; ++targetIxOffset) {
                    nextCEI = ceb.get(targetIx + targetIxOffset);
                    maxLimit = nextCEI.lowIndex_;
                    // If we are at the end of the target too, match succeeds
                    if (nextCEI.ce_ == CollationPCE.PROCESSED_NULLORDER) {
                        break;
                    }
                    // As long as the next CE has primary weight of 0,
                    // it is part of the last target element matched by the pattern;
                    // make sure it can be part of a match with the last patCE
                    if ((((nextCEI.ce_) >>> 32) & 0xFFFF0000L) == 0) {
                        int ceMatch = compareCE64s(nextCEI.ce_, patCE, search_.elementComparisonType_);
                        if (ceMatch == CE_NO_MATCH || ceMatch == CE_SKIP_PATN ) {
                            found = false;
                            break;
                        }
                    // If lowIndex == highIndex, this target CE is part of an expansion of the last matched
                    // target element, but it has non-zero primary weight => match fails
                    } else if ( nextCEI.lowIndex_ == nextCEI.highIndex_ ) {
                        found = false;
                        break;
                    // Else the target CE is not part of an expansion of the last matched element, match succeeds
                    } else {
                        break;
                    }
                }
            }

            // Check for the start of the match being within a combining sequence.
            // This can happen if the pattern itself begins with a combining char, and
            // the match found combining marks in the target text that were attached
            // to something else.
            // This type of match should be rejected for not completely consuming a
            // combining sequence.
            if (!isBreakBoundary(mStart)) {
                found = false;
            }

            // Check for the start of the match being within an Collation Element Expansion,
            // meaning that the first char of the match is only partially matched.
            // With expansions, the first CE will report the index of the source
            // character, and all subsequent (expansions) CEs will report the source index of the
            // _following_ character.
            int secondIx = firstCEI.highIndex_;
            if (mStart == secondIx) {
                found = false;
            }

            // Allow matches to end in the middle of a grapheme cluster if the following
            // conditions are met; this is needed to make prefix search work properly in
            // Indic, see #11750
            // * the default breakIter is being used
            // * the next collation element after this combining sequence
            //   - has non-zero primary weight
            //   - corresponds to a separate character following the one at end of the current match
            //   (the second of these conditions, and perhaps both, may be redundant given the
            //   subsequent check for normalization boundary; however they are likely much faster
            //   tests in any case)
            // * the match limit is a normalization boundary
            boolean allowMidclusterMatch =
                            breakIterator == null &&
                            (((nextCEI.ce_) >>> 32) & 0xFFFF0000L) != 0 &&
                            maxLimit >= lastCEI.highIndex_ && nextCEI.highIndex_ > maxLimit &&
                            (nfd_.hasBoundaryBefore(codePointAt(targetText, maxLimit)) ||
                                    nfd_.hasBoundaryAfter(codePointBefore(targetText, maxLimit)));

            // If those conditions are met, then:
            // * do NOT advance the candidate match limit (mLimit) to a break boundary; however
            //   the match limit may be backed off to a previous break boundary. This handles
            //   cases in which mLimit includes target characters that are ignorable with current
            //   settings (such as space) and which extend beyond the pattern match.
            // * do NOT require that end of the combining sequence not extend beyond the match in CE space
            // * do NOT require that match limit be on a breakIter boundary

            // Advance the match end position to the first acceptable match boundary.
            // This advances the index over any combining characters.
            mLimit = maxLimit;
            if (minLimit < maxLimit) {
                // When the last CE's low index is same with its high index, the CE is likely
                // a part of expansion. In this case, the index is located just after the
                // character corresponding to the CEs compared above. If the index is right
                // at the break boundary, move the position to the next boundary will result
                // incorrect match length when there are ignorable characters exist between
                // the position and the next character produces CE(s). See ticket#8482.
                if (minLimit == lastCEI.highIndex_ && isBreakBoundary(minLimit)) {
                    mLimit = minLimit;
                } else {
                    int nba = nextBoundaryAfter(minLimit);
                    // Note that we can have nba < maxLimit && nba >= minLImit, in which
                    // case we want to set mLimit to nba regardless of allowMidclusterMatch
                    // (i.e. we back off mLimit to the previous breakIterator boundary).
                    if (nba >= lastCEI.highIndex_ && (!allowMidclusterMatch || nba < maxLimit)) {
                        mLimit = nba;
                    }
                }
            }

            if (!allowMidclusterMatch) {
                // If advancing to the end of a combining sequence in character indexing space
                // advanced us beyond the end of the match in CE space, reject this match.
                if (mLimit > maxLimit) {
                    found = false;
                }

                if (!isBreakBoundary(mLimit)) {
                    found = false;
                }
            }

            if (!checkIdentical(mStart, mLimit)) {
                found = false;
            }

            if (found) {
                break;
            }
        }

        // All Done.  Store back the match bounds to the caller.
        //
        if (found == false) {
            mLimit = -1;
            mStart = -1;
        }

        if (m != null) {
            m.start_ = mStart;
            m.limit_ = mLimit;
        }

        return found;
    }

    private static int codePointAt(CharacterIterator iter, int index) {
        int currentIterIndex = iter.getIndex();
        char codeUnit = iter.setIndex(index);
        int cp = codeUnit;
        if (Character.isHighSurrogate(codeUnit)) {
            char nextUnit = iter.next();
            if (Character.isLowSurrogate(nextUnit)) {
                cp = Character.toCodePoint(codeUnit, nextUnit);
            }
        }
        iter.setIndex(currentIterIndex);  // restore iter position
        return cp;
    }

    private static int codePointBefore(CharacterIterator iter, int index) {
        int currentIterIndex = iter.getIndex();
        iter.setIndex(index);
        char codeUnit = iter.previous();
        int cp = codeUnit;
        if (Character.isLowSurrogate(codeUnit)) {
            char prevUnit = iter.previous();
            if (Character.isHighSurrogate(prevUnit)) {
                cp = Character.toCodePoint(prevUnit, codeUnit);
            }
        }
        iter.setIndex(currentIterIndex);  // restore iter position
        return cp;
    }

    private boolean searchBackwards(int startIdx, Match m) {
        //ICU4C_TODO comment:  reject search patterns beginning with a combining char.

        // Input parameter sanity check.
        if (pattern_.CELength_ == 0
                || startIdx < search_.beginIndex()
                || startIdx > search_.endIndex()) {
            throw new IllegalArgumentException("searchBackwards(" + startIdx + ", m) - expected position to be between " +
                    search_.beginIndex() + " and " + search_.endIndex());
        }

        if (pattern_.PCE_ == null) {
            initializePatternPCETable();
        }

        CEBuffer ceb = new CEBuffer(this);
        int targetIx = 0;

        /*
         * Pre-load the buffer with the CE's for the grapheme
         * after our starting position so that we're sure that
         * we can look at the CE following the match when we
         * check the match boundaries.
         *
         * This will also pre-fetch the first CE that we'll
         * consider for the match.
         */
        if (startIdx < search_.endIndex()) {
            BreakIterator bi = search_.internalBreakIter_;
            int next = bi.following(startIdx);

            textIter_.setOffset(next);

            for (targetIx = 0; ; targetIx++) {
                if (ceb.getPrevious(targetIx).lowIndex_ < startIdx) {
                    break;
                }
            }
        } else {
            textIter_.setOffset(startIdx);
        }

        CEI targetCEI = null;
        int patIx;
        boolean found;

        int limitIx = targetIx;
        int mStart = -1;
        int mLimit = -1;
        int minLimit;
        int maxLimit;

        // Outer loop moves over match starting positions in the
        //      target CE space.
        // Here, targetIx values increase toward the beginning of the base text (i.e. we get the text CEs in reverse order).
        // But  patIx is 0 at the beginning of the pattern and increases toward the end.
        // So this loop performs a comparison starting with the end of pattern, and prcessd toward the beginning of the pattern
        // and the beginning of the base text.
        for (targetIx = limitIx; ; targetIx++) {
            found = true;
            // For targetIx > limitIx, this ceb.getPrevious gets a CE that is as far back in the ring buffer
            // (compared to the last CE fetched for the previous targetIx value) as we need to go
            // for this targetIx value, so if it is non-NULL then other ceb.getPrevious calls should be OK.
            CEI lastCEI = ceb.getPrevious(targetIx);
            if (lastCEI == null) {
                throw new ICUException("CEBuffer.getPrevious(" + targetIx + ") returned null.");
            }
            // Inner loop checks for a match beginning at each
            // position from the outer loop.
            int targetIxOffset = 0;
            for (patIx = pattern_.PCELength_ - 1; patIx >= 0; patIx--) {
                long patCE = pattern_.PCE_[patIx];

                targetCEI = ceb.getPrevious(targetIx + pattern_.PCELength_ - 1 - patIx + targetIxOffset);
                // Compare CE from target string with CE from the pattern.
                // Note that the target CE will be UCOL_NULLORDER if we reach the end of input,
                // which will fail the compare, below.
                int ceMatch = compareCE64s(targetCEI.ce_, patCE, search_.elementComparisonType_);
                if (ceMatch == CE_NO_MATCH) {
                    found = false;
                    break;
                } else if (ceMatch > CE_NO_MATCH) {
                    if (ceMatch == CE_SKIP_TARG) {
                        // redo with same patCE, next targCE
                        patIx++;
                        targetIxOffset++;
                    } else { // ceMatch == CE_SKIP_PATN
                        // redo with same targCE, next patCE
                        targetIxOffset--;
                    }
                }
            }

            if (!found && ((targetCEI == null) || (targetCEI.ce_ != CollationPCE.PROCESSED_NULLORDER))) {
                // No match at this targetIx.  Try again at the next.
                continue;
            }

            if (!found) {
                // No match at all, we have run off the end of the target text.
                break;
            }

            // We have found a match in CE space.
            // Now determine the bounds in string index space.
            // There still is a chance of match failure if the CE range not correspond to
            // an acceptable character range.
            //
            CEI firstCEI = ceb.getPrevious(targetIx + pattern_.PCELength_ - 1 + targetIxOffset);
            mStart = firstCEI.lowIndex_;

            // Check for the start of the match being within a combining sequence.
            // This can happen if the pattern itself begins with a combining char, and
            // the match found combining marks in the target text that were attached
            // to something else.
            // This type of match should be rejected for not completely consuming a
            // combining sequence.
            if (!isBreakBoundary(mStart)) {
                found = false;
            }

            // Look at the high index of the first CE in the match. If it's the same as the
            // low index, the first CE in the match is in the middle of an expansion.
            if (mStart == firstCEI.highIndex_) {
                found = false;
            }

            minLimit = lastCEI.lowIndex_;

            if (targetIx > 0) {
                // Look at the CE following the match.  If it is UCOL_NULLORDER the match
                // extended to the end of input, and the match is good.

                // Look at the high and low indices of the CE following the match. If
                // they are the same it means one of two things:
                //    1. The match extended to the last CE from the target text, which is OK, or
                //    2. The last CE that was part of the match is in an expansion that extends
                //       to the first CE after the match. In this case, we reject the match.
                CEI nextCEI  = ceb.getPrevious(targetIx - 1);

                if (nextCEI.lowIndex_ == nextCEI.highIndex_ && nextCEI.ce_ != CollationPCE.PROCESSED_NULLORDER) {
                    found = false;
                }

                mLimit = maxLimit = nextCEI.lowIndex_;

                // Allow matches to end in the middle of a grapheme cluster if the following
                // conditions are met; this is needed to make prefix search work properly in
                // Indic, see #11750
                // * the default breakIter is being used
                // * the next collation element after this combining sequence
                //   - has non-zero primary weight
                //   - corresponds to a separate character following the one at end of the current match
                //   (the second of these conditions, and perhaps both, may be redundant given the
                //   subsequent check for normalization boundary; however they are likely much faster
                //   tests in any case)
                // * the match limit is a normalization boundary
                boolean allowMidclusterMatch =
                                breakIterator == null &&
                                (((nextCEI.ce_) >>> 32) & 0xFFFF0000L) != 0 &&
                                maxLimit >= lastCEI.highIndex_ && nextCEI.highIndex_ > maxLimit &&
                                (nfd_.hasBoundaryBefore(codePointAt(targetText, maxLimit)) ||
                                        nfd_.hasBoundaryAfter(codePointBefore(targetText, maxLimit)));

                // If those conditions are met, then:
                // * do NOT advance the candidate match limit (mLimit) to a break boundary; however
                //   the match limit may be backed off to a previous break boundary. This handles
                //   cases in which mLimit includes target characters that are ignorable with current
                //   settings (such as space) and which extend beyond the pattern match.
                // * do NOT require that end of the combining sequence not extend beyond the match in CE space
                // * do NOT require that match limit be on a breakIter boundary

                // Advance the match end position to the first acceptable match boundary.
                // This advances the index over any combining charcters.
                if (minLimit < maxLimit) {
                    int nba = nextBoundaryAfter(minLimit);
                    // Note that we can have nba < maxLimit && nba >= minLImit, in which
                    // case we want to set mLimit to nba regardless of allowMidclusterMatch
                    // (i.e. we back off mLimit to the previous breakIterator boundary).
                    if (nba >= lastCEI.highIndex_ && (!allowMidclusterMatch || nba < maxLimit)) {
                        mLimit = nba;
                    }
                }

                if (!allowMidclusterMatch) {
                    // If advancing to the end of a combining sequence in character indexing space
                    // advanced us beyond the end of the match in CE space, reject this match.
                    if (mLimit > maxLimit) {
                        found = false;
                    }

                    // Make sure the end of the match is on a break boundary
                    if (!isBreakBoundary(mLimit)) {
                        found = false;
                    }
                }

            } else {
                // No non-ignorable CEs after this point.
                // The maximum position is detected by boundary after
                // the last non-ignorable CE. Combining sequence
                // across the start index will be truncated.
                int nba = nextBoundaryAfter(minLimit);
                mLimit = maxLimit = (nba > 0) && (startIdx > nba) ? nba : startIdx;
            }

            if (!checkIdentical(mStart, mLimit)) {
                found = false;
            }

            if (found) {
                break;
            }
        }

        // All Done.  Store back the match bounds to the caller.
        //
        if (found == false) {
            mLimit = -1;
            mStart = -1;
        }

        if (m != null) {
            m.start_ = mStart;
            m.limit_ = mLimit;
        }

        return found;
    }

    // Java porting note:
    //
    // ICU4C usearch_handleNextExact() is identical to usearch_handleNextCanonical()
    // for the linear search implementation. The differences are addressed in search().
    // 
    private boolean handleNextExact() {
        return handleNextCommonImpl();
    }

    private boolean handleNextCanonical() {
        return handleNextCommonImpl();
    }

    private boolean handleNextCommonImpl() {
        int textOffset = textIter_.getOffset();
        Match match = new Match();

        if (search(textOffset, match)) {
            search_.matchedIndex_ = match.start_;
            search_.setMatchedLength(match.limit_ - match.start_);
            return true;
        } else {
            setMatchNotFound();
            return false;
        }
    }

    // Java porting note:
    //
    // ICU4C usearch_handlePreviousExact() is identical to usearch_handlePreviousCanonical()
    // for the linear search implementation. The differences are addressed in searchBackwards().
    //
    private boolean handlePreviousExact() {
        return handlePreviousCommonImpl();
    }

    private boolean handlePreviousCanonical() {
        return handlePreviousCommonImpl();
    }

    private boolean handlePreviousCommonImpl() {
        int textOffset;

        if (search_.isOverlap_) {
            if (search_.matchedIndex_ != DONE) {
                textOffset = search_.matchedIndex_ + search_.matchedLength() - 1;
            } else {
                // move the start position at the end of possible match
                initializePatternPCETable();
                if (!initTextProcessedIter()) {
                    setMatchNotFound();
                    return false;
                }
                for (int nPCEs = 0; nPCEs < pattern_.PCELength_ - 1; nPCEs++) {
                    long pce = textProcessedIter_.nextProcessed(null);
                    if (pce == CollationPCE.PROCESSED_NULLORDER) {
                        // at the end of the text
                        break;
                    }
                }
                textOffset = textIter_.getOffset();
            }
        } else {
            textOffset = textIter_.getOffset();
        }

        Match match = new Match();
        if (searchBackwards(textOffset, match)) {
            search_.matchedIndex_ = match.start_;
            search_.setMatchedLength(match.limit_ - match.start_);
            return true;
        } else {
            setMatchNotFound();
            return false;
        }
    }

    /**
     * Gets a substring out of a CharacterIterator
     * 
     * Java porting note: Not available in ICU4C
     * 
     * @param text CharacterIterator
     * @param start start offset
     * @param length of substring
     * @return substring from text starting at start and length length
     */
    private static final String getString(CharacterIterator text, int start, int length) {
        StringBuilder result = new StringBuilder(length);
        int offset = text.getIndex();
        text.setIndex(start);
        for (int i = 0; i < length; i++) {
            result.append(text.current());
            text.next();
        }
        text.setIndex(offset);
        return result.toString();
    }

    /**
     * Java port of ICU4C struct UPattern (usrchimp.h)
     */
    private static final class Pattern {
        /** Pattern string */
        String text_;

        long[] PCE_;
        int PCELength_ = 0;

        // TODO: We probably do not need CE_ / CELength_
        @SuppressWarnings("unused")
        int[] CE_;
        int CELength_ = 0;

        // *** Boyer-Moore ***
        // boolean hasPrefixAccents_ = false;
        // boolean hasSuffixAccents_ = false;
        // int defaultShiftSize_;
        // char[] shift_;
        // char[] backShift_;

        protected Pattern(String pattern) {
            text_ = pattern;
        }
    }

    /**
     * Java port of ICU4C UCollationPCE (usrchimp.h)
     */
    private static class CollationPCE {
        public static final long PROCESSED_NULLORDER = -1;

        private static final int DEFAULT_BUFFER_SIZE = 16;
        private static final int BUFFER_GROW = 8;

        // Note: PRIMARYORDERMASK is also duplicated in StringSearch class
        private static final int PRIMARYORDERMASK = 0xffff0000;
        private static final int CONTINUATION_MARKER = 0xc0;

        private PCEBuffer pceBuffer_ = new PCEBuffer();
        private CollationElementIterator cei_;
        private int strength_;
        private boolean toShift_;
        private boolean isShifted_;
        private int variableTop_;

        public CollationPCE(CollationElementIterator iter) {
            init(iter);
        }

        public void init(CollationElementIterator iter) {
            cei_ = iter;
            init(iter.getRuleBasedCollator());
        }

        private void init(RuleBasedCollator coll) {
            strength_ = coll.getStrength();
            toShift_ = coll.isAlternateHandlingShifted();
            isShifted_ = false;
            variableTop_ = coll.getVariableTop();
        }

        @SuppressWarnings("fallthrough")
        private long processCE(int ce) {
            long primary = 0, secondary = 0, tertiary = 0, quaternary = 0;

            // This is clean, but somewhat slow...
            // We could apply the mask to ce and then
            // just get all three orders...
            switch (strength_) {
            default:
                tertiary = CollationElementIterator.tertiaryOrder(ce);
                /* note fall-through */

            case Collator.SECONDARY:
                secondary = CollationElementIterator.secondaryOrder(ce);
                /* note fall-through */

            case Collator.PRIMARY:
                primary = CollationElementIterator.primaryOrder(ce);
            }

            // **** This should probably handle continuations too. ****
            // **** That means that we need 24 bits for the primary ****
            // **** instead of the 16 that we're currently using. ****
            // **** So we can lay out the 64 bits as: 24.12.12.16. ****
            // **** Another complication with continuations is that ****
            // **** the *second* CE is marked as a continuation, so ****
            // **** we always have to peek ahead to know how long ****
            // **** the primary is... ****
            if ((toShift_ && variableTop_ > ce && primary != 0) || (isShifted_ && primary == 0)) {

                if (primary == 0) {
                    return CollationElementIterator.IGNORABLE;
                }

                if (strength_ >= Collator.QUATERNARY) {
                    quaternary = primary;
                }

                primary = secondary = tertiary = 0;
                isShifted_ = true;
            } else {
                if (strength_ >= Collator.QUATERNARY) {
                    quaternary = 0xFFFF;
                }

                isShifted_ = false;
            }

            return primary << 48 | secondary << 32 | tertiary << 16 | quaternary;
        }

        /**
         * Get the processed ordering priority of the next collation element in the text.
         * A single character may contain more than one collation element.
         * 
         * Note: This is equivalent to
         * UCollationPCE::nextProcessed(int32_t *ixLow, int32_t *ixHigh, UErrorCode *status);
         *
         * @param range receiving the iterator index before/after fetching the CE.
         * @return The next collation elements ordering, otherwise returns PROCESSED_NULLORDER 
         *         if an error has occurred or if the end of string has been reached
         */
        public long nextProcessed(Range range) {
            long result = CollationElementIterator.IGNORABLE;
            int low = 0, high = 0;

            pceBuffer_.reset();

            do {
                low = cei_.getOffset();
                int ce = cei_.next();
                high = cei_.getOffset();

                if (ce == CollationElementIterator.NULLORDER) {
                     result = PROCESSED_NULLORDER;
                     break;
                }

                result = processCE(ce);
            } while (result == CollationElementIterator.IGNORABLE);

            if (range != null) {
                range.ixLow_ = low;
                range.ixHigh_ = high;
            }

            return result;
        }

        /**
         * Get the processed ordering priority of the previous collation element in the text.
         * A single character may contain more than one collation element.
         *
         * Note: This is equivalent to
         * UCollationPCE::previousProcessed(int32_t *ixLow, int32_t *ixHigh, UErrorCode *status);
         *
         * @param range receiving the iterator index before/after fetching the CE.
         * @return The previous collation elements ordering, otherwise returns 
         *         PROCESSED_NULLORDER if an error has occurred or if the start of
         *         string has been reached.
         */
        public long previousProcessed(Range range) {
            long result = CollationElementIterator.IGNORABLE;
            int low = 0, high = 0;

            // pceBuffer_.reset();

            while (pceBuffer_.empty()) {
                // buffer raw CEs up to non-ignorable primary
                RCEBuffer rceb = new RCEBuffer();
                int ce;

                boolean finish = false;

                // **** do we need to reset rceb, or will it always be empty at this point ****
                do {
                    high = cei_.getOffset();
                    ce = cei_.previous();
                    low = cei_.getOffset();

                    if (ce == CollationElementIterator.NULLORDER) {
                        if (!rceb.empty()) {
                            break;
                        }

                        finish = true;
                        break;
                    }

                    rceb.put(ce, low, high);
                } while ((ce & PRIMARYORDERMASK) == 0 || isContinuation(ce));

                if (finish) {
                    break;
                }

                // process the raw CEs
                while (!rceb.empty()) {
                    RCEI rcei = rceb.get();

                    result = processCE(rcei.ce_);

                    if (result != CollationElementIterator.IGNORABLE) {
                        pceBuffer_.put(result, rcei.low_, rcei.high_);
                    }
                }
            }

            if (pceBuffer_.empty()) {
                // **** Is -1 the right value for ixLow, ixHigh? ****
                if (range != null) {
                    range.ixLow_ = -1;
                    range.ixHigh_ = -1;
                }
                return CollationElementIterator.NULLORDER;
            }

            PCEI pcei = pceBuffer_.get();

            if (range != null) {
                range.ixLow_ = pcei.low_;
                range.ixHigh_ = pcei.high_;
            }

            return pcei.ce_;
        }

        private static boolean isContinuation(int ce) {
            return ((ce & CONTINUATION_MARKER) == CONTINUATION_MARKER);
        }

        public static final class Range {
            int ixLow_;
            int ixHigh_;
        }

        /** Processed collation element buffer stuff ported from ICU4C ucoleitr.cpp */
        private static final class PCEI {
            long ce_;
            int low_;
            int high_;
        }

        private static final class PCEBuffer {
            private PCEI[] buffer_ = new PCEI[DEFAULT_BUFFER_SIZE];
            private int bufferIndex_ = 0;

            void reset() {
                bufferIndex_ = 0;
            }

            boolean empty() {
                return bufferIndex_ <= 0;
            }

            void put(long ce, int ixLow, int ixHigh)
            {
                if (bufferIndex_ >= buffer_.length) {
                    PCEI[] newBuffer = new PCEI[buffer_.length + BUFFER_GROW];
                    System.arraycopy(buffer_, 0, newBuffer, 0, buffer_.length);
                    buffer_ = newBuffer;
                }
                buffer_[bufferIndex_] = new PCEI();
                buffer_[bufferIndex_].ce_ = ce;
                buffer_[bufferIndex_].low_ = ixLow;
                buffer_[bufferIndex_].high_ = ixHigh;

                bufferIndex_ += 1;
            }

            PCEI get() {
                if (bufferIndex_ > 0) {
                    return buffer_[--bufferIndex_];
                }
                return null;
            }
        }

        /** Raw collation element buffer stuff ported from ICU4C ucoleitr.cpp */
        private static final class RCEI {
            int ce_;
            int low_;
            int high_;
        }

        private static final class RCEBuffer {
            private RCEI[] buffer_ = new RCEI[DEFAULT_BUFFER_SIZE];
            private int bufferIndex_ = 0;

            boolean empty() {
                return bufferIndex_ <= 0;
            }

            void put(int ce, int ixLow, int ixHigh) {
                if (bufferIndex_ >= buffer_.length) {
                    RCEI[] newBuffer = new RCEI[buffer_.length + BUFFER_GROW];
                    System.arraycopy(buffer_, 0, newBuffer, 0, buffer_.length);
                    buffer_ = newBuffer;
                }
                buffer_[bufferIndex_] = new RCEI();
                buffer_[bufferIndex_].ce_ = ce;
                buffer_[bufferIndex_].low_ = ixLow;
                buffer_[bufferIndex_].high_ = ixHigh;

                bufferIndex_ += 1;
            }

            RCEI get() {
                if (bufferIndex_ > 0) {
                    return buffer_[--bufferIndex_];
                }
                return null;
            }
        }
    }

    /**
     * Java port of ICU4C CEI (usearch.cpp)
     * 
     * CEI  Collation Element + source text index.
     *      These structs are kept in the circular buffer.
     */
    private static class CEI {
        long ce_;
        int lowIndex_;
        int highIndex_;
    }

    /**
     * CEBuffer A circular buffer of CEs from the text being searched
     */
    private static class CEBuffer {
        // Java porting note: ICU4C uses the size for stack buffer
        // static final int DEFAULT_CEBUFFER_SIZE = 96;

        static final int CEBUFFER_EXTRA = 32;
        static final int MAX_TARGET_IGNORABLES_PER_PAT_JAMO_L = 8;
        static final int MAX_TARGET_IGNORABLES_PER_PAT_OTHER = 3;

        CEI[] buf_;
        int bufSize_;
        int firstIx_;
        int limitIx_;

        // Java porting note: No references in ICU4C implementation
        // CollationElementIterator ceIter_;

        StringSearch strSearch_;

        CEBuffer(StringSearch ss) {
            strSearch_ = ss;
            bufSize_ = ss.pattern_.PCELength_ + CEBUFFER_EXTRA;
            if (ss.search_.elementComparisonType_ != ElementComparisonType.STANDARD_ELEMENT_COMPARISON) {
                String patText = ss.pattern_.text_;
                if (patText != null) {
                    for (int i = 0; i < patText.length(); i++) {
                        char c = patText.charAt(i);
                        if (MIGHT_BE_JAMO_L(c)) {
                            bufSize_ += MAX_TARGET_IGNORABLES_PER_PAT_JAMO_L;
                        } else {
                            // No check for surrogates, we might allocate slightly more buffer than necessary.
                            bufSize_ += MAX_TARGET_IGNORABLES_PER_PAT_OTHER;
                        }
                    }
                }
            }

            // Not used - see above
            // ceIter_ = ss.textIter_;

            firstIx_ = 0;
            limitIx_ = 0;

            if (!ss.initTextProcessedIter()) {
                return;
            }

            buf_ = new CEI[bufSize_];
        }

        // Get the CE with the specified index.
        //   Index must be in the range
        //             n-history_size < index < n+1
        //   where n is the largest index to have been fetched by some previous call to this function.
        //   The CE value will be UCOL__PROCESSED_NULLORDER at end of input.
        //
        CEI get(int index) {
            int i = index % bufSize_;

            if (index >= firstIx_ && index < limitIx_) {
                // The request was for an entry already in our buffer.
                // Just return it.
                return buf_[i];
            }

            // Caller is requesting a new, never accessed before, CE.
            // Verify that it is the next one in sequence, which is all
            // that is allowed.
            if (index != limitIx_) {
                assert(false);
                return null;
            }

            // Manage the circular CE buffer indexing
            limitIx_++;

            if (limitIx_ - firstIx_ >= bufSize_) {
                // The buffer is full, knock out the lowest-indexed entry.
                firstIx_++;
            }

            CollationPCE.Range range = new CollationPCE.Range();
            if (buf_[i] == null) {
                buf_[i] = new CEI();
            }
            buf_[i].ce_ = strSearch_.textProcessedIter_.nextProcessed(range);
            buf_[i].lowIndex_ = range.ixLow_;
            buf_[i].highIndex_ = range.ixHigh_;

            return buf_[i];
        }

        // Get the CE with the specified index.
        //   Index must be in the range
        //             n-history_size < index < n+1
        //   where n is the largest index to have been fetched by some previous call to this function.
        //   The CE value will be UCOL__PROCESSED_NULLORDER at end of input.
        //
        CEI getPrevious(int index) {
            int i = index % bufSize_;

            if (index >= firstIx_ && index < limitIx_) {
                // The request was for an entry already in our buffer.
                // Just return it.
                return buf_[i];
            }

            // Caller is requesting a new, never accessed before, CE.
            // Verify that it is the next one in sequence, which is all
            // that is allowed.
            if (index != limitIx_) {
                assert(false);
                return null;
            }

            // Manage the circular CE buffer indexing
            limitIx_++;

            if (limitIx_ - firstIx_ >= bufSize_) {
                // The buffer is full, knock out the lowest-indexed entry.
                firstIx_++;
            }

            CollationPCE.Range range = new CollationPCE.Range();
            if (buf_[i] == null) {
                buf_[i] = new CEI();
            }
            buf_[i].ce_ = strSearch_.textProcessedIter_.previousProcessed(range);
            buf_[i].lowIndex_ = range.ixLow_;
            buf_[i].highIndex_ = range.ixHigh_;

            return buf_[i];
        }

        static boolean MIGHT_BE_JAMO_L(char c) {
            return (c >= 0x1100 && c <= 0x115E)
                    || (c >= 0x3131 && c <= 0x314E)
                    || (c >= 0x3165 && c <= 0x3186);
        }
    }
}
