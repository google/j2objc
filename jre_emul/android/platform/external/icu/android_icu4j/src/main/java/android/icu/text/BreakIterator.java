/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2016, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

package android.icu.text;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Locale;
import java.util.MissingResourceException;

import android.icu.impl.CSCharacterIterator;
import android.icu.impl.CacheValue;
import android.icu.impl.ICUDebug;
import android.icu.util.ICUCloneNotSupportedException;
import android.icu.util.ULocale;

/**
 * <strong>[icu enhancement]</strong> ICU's replacement for {@link java.text.BreakIterator}.&nbsp;Methods, fields, and other functionality specific to ICU are labeled '<strong>[icu]</strong>'.
 *
 * <p>A class that locates boundaries in text.  This class defines a protocol for
 * objects that break up a piece of natural-language text according to a set
 * of criteria.  Instances or subclasses of BreakIterator can be provided, for
 * example, to break a piece of text into words, sentences, or logical characters
 * according to the conventions of some language or group of languages.
 *
 * We provide five built-in types of BreakIterator:
 * <ul><li>getTitleInstance() returns a BreakIterator that locates boundaries
 * between title breaks.
 * <li>getSentenceInstance() returns a BreakIterator that locates boundaries
 * between sentences.  This is useful for triple-click selection, for example.
 * <li>getWordInstance() returns a BreakIterator that locates boundaries between
 * words.  This is useful for double-click selection or "find whole words" searches.
 * This type of BreakIterator makes sure there is a boundary position at the
 * beginning and end of each legal word.  (Numbers count as words, too.)  Whitespace
 * and punctuation are kept separate from real words.
 * <li>getLineInstance() returns a BreakIterator that locates positions where it is
 * legal for a text editor to wrap lines.  This is similar to word breaking, but
 * not the same: punctuation and whitespace are generally kept with words (you don't
 * want a line to start with whitespace, for example), and some special characters
 * can force a position to be considered a line-break position or prevent a position
 * from being a line-break position.
 * <li>getCharacterInstance() returns a BreakIterator that locates boundaries between
 * logical characters.  Because of the structure of the Unicode encoding, a logical
 * character may be stored internally as more than one Unicode code point.  (A with an
 * umlaut may be stored as an a followed by a separate combining umlaut character,
 * for example, but the user still thinks of it as one character.)  This iterator allows
 * various processes (especially text editors) to treat as characters the units of text
 * that a user would think of as characters, rather than the units of text that the
 * computer sees as "characters".</ul>
 * The text boundary positions are found according to the rules
 * described in Unicode Standard Annex #29, Text Boundaries, and
 * Unicode Standard Annex #14, Line Breaking Properties.  These
 * are available at http://www.unicode.org/reports/tr14/ and
 * http://www.unicode.org/reports/tr29/.
 * <p>
 * BreakIterator's interface follows an "iterator" model (hence the name), meaning it
 * has a concept of a "current position" and methods like first(), last(), next(),
 * and previous() that update the current position.  All BreakIterators uphold the
 * following invariants:
 * <ul><li>The beginning and end of the text are always treated as boundary positions.
 * <li>The current position of the iterator is always a boundary position (random-
 * access methods move the iterator to the nearest boundary position before or
 * after the specified position, not _to_ the specified position).
 * <li>DONE is used as a flag to indicate when iteration has stopped.  DONE is only
 * returned when the current position is the end of the text and the user calls next(),
 * or when the current position is the beginning of the text and the user calls
 * previous().
 * <li>Break positions are numbered by the positions of the characters that follow
 * them.  Thus, under normal circumstances, the position before the first character
 * is 0, the position after the first character is 1, and the position after the
 * last character is 1 plus the length of the string.
 * <li>The client can change the position of an iterator, or the text it analyzes,
 * at will, but cannot change the behavior.  If the user wants different behavior, he
 * must instantiate a new iterator.</ul>
 *
 * BreakIterator accesses the text it analyzes through a CharacterIterator, which makes
 * it possible to use BreakIterator to analyze text in any text-storage vehicle that
 * provides a CharacterIterator interface. When BreakIterator.setText(CharacterIterator) or
 * getText() was called, the CharacterIterator must not be modified, or else the BreakIterator
 * behavior is undefined. In particular, call BreakIterator.setText(),
 * not CharacterIterator.setText().
 *
 * <b>Note:</b>  Some types of BreakIterator can take a long time to create, and
 * instances of BreakIterator are not currently cached by the system.  For
 * optimal performance, keep instances of BreakIterator around as long as makes
 * sense.  For example, when word-wrapping a document, don't create and destroy a
 * new BreakIterator for each line.  Create one break iterator for the whole document
 * (or whatever stretch of text you're wrapping) and use it to do the whole job of
 * wrapping the text.
 *
  * <P>
 * <strong>Examples</strong>:<P>
 * Creating and using text boundaries
 * <blockquote>
 * <pre>
 * public static void main(String args[]) {
 *      if (args.length == 1) {
 *          String stringToExamine = args[0];
 *          //print each word in order
 *          BreakIterator boundary = BreakIterator.getWordInstance();
 *          boundary.setText(stringToExamine);
 *          printEachForward(boundary, stringToExamine);
 *          //print each sentence in reverse order
 *          boundary = BreakIterator.getSentenceInstance(Locale.US);
 *          boundary.setText(stringToExamine);
 *          printEachBackward(boundary, stringToExamine);
 *          printFirst(boundary, stringToExamine);
 *          printLast(boundary, stringToExamine);
 *      }
 * }
 * </pre>
 * </blockquote>
 *
 * Print each element in order
 * <blockquote>
 * <pre>
 * public static void printEachForward(BreakIterator boundary, String source) {
 *     int start = boundary.first();
 *     for (int end = boundary.next();
 *          end != BreakIterator.DONE;
 *          start = end, end = boundary.next()) {
 *          System.out.println(source.substring(start,end));
 *     }
 * }
 * </pre>
 * </blockquote>
 *
 * Print each element in reverse order
 * <blockquote>
 * <pre>
 * public static void printEachBackward(BreakIterator boundary, String source) {
 *     int end = boundary.last();
 *     for (int start = boundary.previous();
 *          start != BreakIterator.DONE;
 *          end = start, start = boundary.previous()) {
 *         System.out.println(source.substring(start,end));
 *     }
 * }
 * </pre>
 * </blockquote>
 *
 * Print first element
 * <blockquote>
 * <pre>
 * public static void printFirst(BreakIterator boundary, String source) {
 *     int start = boundary.first();
 *     int end = boundary.next();
 *     System.out.println(source.substring(start,end));
 * }
 * </pre>
 * </blockquote>
 *
 * Print last element
 * <blockquote>
 * <pre>
 * public static void printLast(BreakIterator boundary, String source) {
 *     int end = boundary.last();
 *     int start = boundary.previous();
 *     System.out.println(source.substring(start,end));
 * }
 * </pre>
 * </blockquote>
 *
 * Print the element at a specified position
 * <blockquote>
 * <pre>
 * public static void printAt(BreakIterator boundary, int pos, String source) {
 *     int end = boundary.following(pos);
 *     int start = boundary.previous();
 *     System.out.println(source.substring(start,end));
 * }
 * </pre>
 * </blockquote>
 *
 * Find the next word
 * <blockquote>
 * <pre>
 * public static int nextWordStartAfter(int pos, String text) {
 *     BreakIterator wb = BreakIterator.getWordInstance();
 *     wb.setText(text);
 *     int wordStart = wb.following(pos);
 *     for (;;) {
 *         int wordLimit = wb.next();
 *         if (wordLimit == BreakIterator.DONE) {
 *             return BreakIterator.DONE;
 *         }
 *         int wordStatus = wb.getRuleStatus();
 *         if (wordStatus != BreakIterator.WORD_NONE) {
 *             return wordStart;
 *         }
 *         wordStart = wordLimit;
 *      }
 * }
 * </pre>
 * The iterator returned by {@link #getWordInstance} is unique in that
 * the break positions it returns don't represent both the start and end of the
 * thing being iterated over.  That is, a sentence-break iterator returns breaks
 * that each represent the end of one sentence and the beginning of the next.
 * With the word-break iterator, the characters between two boundaries might be a
 * word, or they might be the punctuation or whitespace between two words.  The
 * above code uses {@link #getRuleStatus} to identify and ignore boundaries associated
 * with punctuation or other non-word characters.
 * </blockquote>
 *
 * @see CharacterIterator
 *
 */

public abstract class BreakIterator implements Cloneable
{

    private static final boolean DEBUG = ICUDebug.enabled("breakiterator");

    /**
     * Default constructor.  There is no state that is carried by this abstract
     * base class.
     */
    protected BreakIterator()
    {
    }

    /**
     * Clone method.  Creates another BreakIterator with the same behavior and
     * current state as this one.
     * @return The clone.
     */
    @Override
    public Object clone()
    {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            ///CLOVER:OFF
            throw new ICUCloneNotSupportedException(e);
            ///CLOVER:ON
        }
    }

    /**
     * DONE is returned by previous() and next() after all valid
     * boundaries have been returned.
     */
    public static final int DONE = -1;

    /**
     * Set the iterator to the first boundary position.  This is always the beginning
     * index of the text this iterator iterates over.  For example, if
     * the iterator iterates over a whole string, this function will
     * always return 0.
     * @return The character offset of the beginning of the stretch of text
     * being broken.
     */
    public abstract int first();

    /**
     * Set the iterator to the last boundary position.  This is always the "past-the-end"
     * index of the text this iterator iterates over.  For example, if the
     * iterator iterates over a whole string (call it "text"), this function
     * will always return text.length().
     * @return The character offset of the end of the stretch of text
     * being broken.
     */
    public abstract int last();

    /**
     * Move the iterator by the specified number of steps in the text.
     * A positive number moves the iterator forward; a negative number
     * moves the iterator backwards. If this causes the iterator
     * to move off either end of the text, this function returns DONE;
     * otherwise, this function returns the position of the appropriate
     * boundary.  Calling this function is equivalent to calling next() or
     * previous() n times.
     * @param n The number of boundaries to advance over (if positive, moves
     * forward; if negative, moves backwards).
     * @return The position of the boundary n boundaries from the current
     * iteration position, or DONE if moving n boundaries causes the iterator
     * to advance off either end of the text.
     */
    public abstract int next(int n);

    /**
     * Advances the iterator forward one boundary.  The current iteration
     * position is updated to point to the next boundary position after the
     * current position, and this is also the value that is returned.  If
     * the current position is equal to the value returned by last(), or to
     * DONE, this function returns DONE and sets the current position to
     * DONE.
     * @return The position of the first boundary position following the
     * iteration position.
     */
    public abstract int next();

    /**
     * Move the iterator backward one boundary.  The current iteration
     * position is updated to point to the last boundary position before
     * the current position, and this is also the value that is returned.  If
     * the current position is equal to the value returned by first(), or to
     * DONE, this function returns DONE and sets the current position to
     * DONE.
     * @return The position of the last boundary position preceding the
     * iteration position.
     */
    public abstract int previous();

    /**
     * Sets the iterator's current iteration position to be the first
     * boundary position following the specified position.  (Whether the
     * specified position is itself a boundary position or not doesn't
     * matter-- this function always moves the iteration position to the
     * first boundary after the specified position.)  If the specified
     * position is the past-the-end position, returns DONE.
     * @param offset The character position to start searching from.
     * @return The position of the first boundary position following
     * "offset" (whether or not "offset" itself is a boundary position),
     * or DONE if "offset" is the past-the-end offset.
     */
    public abstract int following(int offset);

    /**
     * Sets the iterator's current iteration position to be the last
     * boundary position preceding the specified position.  (Whether the
     * specified position is itself a boundary position or not doesn't
     * matter-- this function always moves the iteration position to the
     * last boundary before the specified position.)  If the specified
     * position is the starting position, returns DONE.
     * @param offset The character position to start searching from.
     * @return The position of the last boundary position preceding
     * "offset" (whether of not "offset" itself is a boundary position),
     * or DONE if "offset" is the starting offset of the iterator.
     */
    public int preceding(int offset) {
        // NOTE:  This implementation is here solely because we can't add new
        // abstract methods to an existing class.  There is almost ALWAYS a
        // better, faster way to do this.
        int pos = following(offset);
        while (pos >= offset && pos != DONE)
            pos = previous();
        return pos;
    }

    /**
     * Return true if the specified position is a boundary position.  If the
     * function returns true, the current iteration position is set to the
     * specified position; if the function returns false, the current
     * iteration position is set as though following() had been called.
     * @param offset the offset to check.
     * @return True if "offset" is a boundary position.
     */
    public boolean isBoundary(int offset) {
        // Again, this is the default implementation, which is provided solely because
        // we couldn't add a new abstract method to an existing class.  The real
        // implementations will usually need to do a little more work.
        if (offset == 0) {
            return true;
        }
        else
            return following(offset - 1) == offset;
    }

    /**
     * Return the iterator's current position.
     * @return The iterator's current position.
     */
    public abstract int current();


    /**
     * Tag value for "words" that do not fit into any of other categories.
     * Includes spaces and most punctuation.
     */
    public static final int WORD_NONE           = 0;

    /**
     * Upper bound for tags for uncategorized words.
     */
    public static final int WORD_NONE_LIMIT     = 100;

    /**
     * Tag value for words that appear to be numbers, lower limit.
     */
    public static final int WORD_NUMBER         = 100;

    /**
     * Tag value for words that appear to be numbers, upper limit.
     */
    public static final int WORD_NUMBER_LIMIT   = 200;

    /**
     * Tag value for words that contain letters, excluding
     * hiragana, katakana or ideographic characters, lower limit.
     */
    public static final int WORD_LETTER         = 200;

    /**
     * Tag value for words containing letters, upper limit
     */
    public static final int WORD_LETTER_LIMIT   = 300;

    /**
     * Tag value for words containing kana characters, lower limit
     */
    public static final int WORD_KANA           = 300;

    /**
     * Tag value for words containing kana characters, upper limit
     */
    public static final int WORD_KANA_LIMIT     = 400;

    /**
     * Tag value for words containing ideographic characters, lower limit
     */
    public static final int WORD_IDEO           = 400;

    /**
     * Tag value for words containing ideographic characters, upper limit
     */
    public static final int WORD_IDEO_LIMIT     = 500;

    /**
     * For RuleBasedBreakIterators, return the status tag from the
     * break rule that determined the most recently
     * returned break position.
     * <p>
     * For break iterator types that do not support a rule status,
     * a default value of 0 is returned.
     * <p>
     * @return The status from the break rule that determined the most recently
     *         returned break position.
     */

    public int  getRuleStatus() {
        return 0;
    }

    /**
     * For RuleBasedBreakIterators, get the status (tag) values from the break rule(s)
     * that determined the most recently returned break position.
     * <p>
     * For break iterator types that do not support rule status,
     * no values are returned.
     * <p>
     * If the size  of the output array is insufficient to hold the data,
     *  the output will be truncated to the available length.  No exception
     *  will be thrown.
     *
     * @param fillInArray an array to be filled in with the status values.
     * @return          The number of rule status values from rules that determined
     *                  the most recent boundary returned by the break iterator.
     *                  In the event that the array is too small, the return value
     *                  is the total number of status values that were available,
     *                  not the reduced number that were actually returned.
     */
    public int getRuleStatusVec(int[] fillInArray) {
        if (fillInArray != null && fillInArray.length > 0) {
            fillInArray[0] = 0;
        }
        return 1;
    }

    /**
     * Returns a CharacterIterator over the text being analyzed.
     * For at least some subclasses of BreakIterator, this is a reference
     * to the <b>actual iterator being used</b> by the BreakIterator,
     * and therefore, this function's return value should be treated as
     * <tt>const</tt>.  No guarantees are made about the current position
     * of this iterator when it is returned.  If you need to move that
     * position to examine the text, clone this function's return value first.
     * @return A CharacterIterator over the text being analyzed.
     */
    public abstract CharacterIterator getText();

    /**
     * Sets the iterator to analyze a new piece of text.  The new
     * piece of text is passed in as a String, and the current
     * iteration position is reset to the beginning of the string.
     * (The old text is dropped.)
     * @param newText A String containing the text to analyze with
     * this BreakIterator.
     */
    public void setText(String newText)
    {
        setText(new StringCharacterIterator(newText));
    }

    /**
     * Sets the iterator to analyze a new piece of text.  The new
     * piece of text is passed in as a CharSequence, and the current
     * iteration position is reset to the beginning of the text.
     * (The old text is dropped.)
     * @param newText A CharSequence containing the text to analyze with
     * this BreakIterator.
     * @hide draft / provisional / internal are hidden on Android
     */
    public void setText(CharSequence newText) {
        setText(new CSCharacterIterator(newText));
    }

    /**
     * Sets the iterator to analyze a new piece of text.  The
     * BreakIterator is passed a CharacterIterator through which
     * it will access the text itself.  The current iteration
     * position is reset to the CharacterIterator's start index.
     * (The old iterator is dropped.)
     * @param newText A CharacterIterator referring to the text
     * to analyze with this BreakIterator (the iterator's current
     * position is ignored, but its other state is significant).
     */
    public abstract void setText(CharacterIterator newText);

    /**
     * <strong>[icu]</strong>
     */
    public static final int KIND_CHARACTER = 0;
    /**
     * <strong>[icu]</strong>
     */
    public static final int KIND_WORD = 1;
    /**
     * <strong>[icu]</strong>
     */
    public static final int KIND_LINE = 2;
    /**
     * <strong>[icu]</strong>
     */
    public static final int KIND_SENTENCE = 3;
    /**
     * <strong>[icu]</strong>
     */
    public static final int KIND_TITLE = 4;

    /**
     */
    private static final int KIND_COUNT = 5;

    private static final CacheValue<?>[] iterCache = new CacheValue<?>[5];

    /**
     * Returns a new instance of BreakIterator that locates word boundaries.
     * This function assumes that the text being analyzed is in the default
     * locale's language.
     * @return An instance of BreakIterator that locates word boundaries.
     */
    public static BreakIterator getWordInstance()
    {
        return getWordInstance(ULocale.getDefault());
    }

    /**
     * Returns a new instance of BreakIterator that locates word boundaries.
     * @param where A locale specifying the language of the text to be
     * analyzed.
     * @return An instance of BreakIterator that locates word boundaries.
     * @throws NullPointerException if <code>where</code> is null.
     */
    public static BreakIterator getWordInstance(Locale where)
    {
        return getBreakInstance(ULocale.forLocale(where), KIND_WORD);
    }

    /**
     * <strong>[icu]</strong> Returns a new instance of BreakIterator that locates word boundaries.
     * @param where A locale specifying the language of the text to be
     * analyzed.
     * @return An instance of BreakIterator that locates word boundaries.
     * @throws NullPointerException if <code>where</code> is null.
     */
    public static BreakIterator getWordInstance(ULocale where)
    {
        return getBreakInstance(where, KIND_WORD);
    }

    /**
     * Returns a new instance of BreakIterator that locates legal line-
     * wrapping positions.  This function assumes the text being broken
     * is in the default locale's language.
     * @return A new instance of BreakIterator that locates legal
     * line-wrapping positions.
     */
    public static BreakIterator getLineInstance()
    {
        return getLineInstance(ULocale.getDefault());
    }

    /**
     * Returns a new instance of BreakIterator that locates legal line-
     * wrapping positions.
     * @param where A Locale specifying the language of the text being broken.
     * @return A new instance of BreakIterator that locates legal
     * line-wrapping positions.
     * @throws NullPointerException if <code>where</code> is null.
     */
    public static BreakIterator getLineInstance(Locale where)
    {
        return getBreakInstance(ULocale.forLocale(where), KIND_LINE);
    }

    /**
     * <strong>[icu]</strong> Returns a new instance of BreakIterator that locates legal line-
     * wrapping positions.
     * @param where A Locale specifying the language of the text being broken.
     * @return A new instance of BreakIterator that locates legal
     * line-wrapping positions.
     * @throws NullPointerException if <code>where</code> is null.
     */
    public static BreakIterator getLineInstance(ULocale where)
    {
        return getBreakInstance(where, KIND_LINE);
    }

    /**
     * Returns a new instance of BreakIterator that locates logical-character
     * boundaries.  This function assumes that the text being analyzed is
     * in the default locale's language.
     * @return A new instance of BreakIterator that locates logical-character
     * boundaries.
     */
    public static BreakIterator getCharacterInstance()
    {
        return getCharacterInstance(ULocale.getDefault());
    }

    /**
     * Returns a new instance of BreakIterator that locates logical-character
     * boundaries.
     * @param where A Locale specifying the language of the text being analyzed.
     * @return A new instance of BreakIterator that locates logical-character
     * boundaries.
     * @throws NullPointerException if <code>where</code> is null.
     */
    public static BreakIterator getCharacterInstance(Locale where)
    {
        return getBreakInstance(ULocale.forLocale(where), KIND_CHARACTER);
    }

    /**
     * <strong>[icu]</strong> Returns a new instance of BreakIterator that locates logical-character
     * boundaries.
     * @param where A Locale specifying the language of the text being analyzed.
     * @return A new instance of BreakIterator that locates logical-character
     * boundaries.
     * @throws NullPointerException if <code>where</code> is null.
     */
    public static BreakIterator getCharacterInstance(ULocale where)
    {
        return getBreakInstance(where, KIND_CHARACTER);
    }

    /**
     * Returns a new instance of BreakIterator that locates sentence boundaries.
     * This function assumes the text being analyzed is in the default locale's
     * language.
     * @return A new instance of BreakIterator that locates sentence boundaries.
     */
    public static BreakIterator getSentenceInstance()
    {
        return getSentenceInstance(ULocale.getDefault());
    }

    /**
     * Returns a new instance of BreakIterator that locates sentence boundaries.
     * @param where A Locale specifying the language of the text being analyzed.
     * @return A new instance of BreakIterator that locates sentence boundaries.
     * @throws NullPointerException if <code>where</code> is null.
     */
    public static BreakIterator getSentenceInstance(Locale where)
    {
        return getBreakInstance(ULocale.forLocale(where), KIND_SENTENCE);
    }

    /**
     * <strong>[icu]</strong> Returns a new instance of BreakIterator that locates sentence boundaries.
     * @param where A Locale specifying the language of the text being analyzed.
     * @return A new instance of BreakIterator that locates sentence boundaries.
     * @throws NullPointerException if <code>where</code> is null.
     */
    public static BreakIterator getSentenceInstance(ULocale where)
    {
        return getBreakInstance(where, KIND_SENTENCE);
    }

    /**
     * <strong>[icu]</strong> Returns a new instance of BreakIterator that locates title boundaries.
     * This function assumes the text being analyzed is in the default locale's
     * language. The iterator returned locates title boundaries as described for
     * Unicode 3.2 only. For Unicode 4.0 and above title boundary iteration,
     * please use a word boundary iterator. {@link #getWordInstance}
     * @return A new instance of BreakIterator that locates title boundaries.
     */
    public static BreakIterator getTitleInstance()
    {
        return getTitleInstance(ULocale.getDefault());
    }

    /**
     * <strong>[icu]</strong> Returns a new instance of BreakIterator that locates title boundaries.
     * The iterator returned locates title boundaries as described for
     * Unicode 3.2 only. For Unicode 4.0 and above title boundary iteration,
     * please use Word Boundary iterator.{@link #getWordInstance}
     * @param where A Locale specifying the language of the text being analyzed.
     * @return A new instance of BreakIterator that locates title boundaries.
     * @throws NullPointerException if <code>where</code> is null.
     */
    public static BreakIterator getTitleInstance(Locale where)
    {
        return getBreakInstance(ULocale.forLocale(where), KIND_TITLE);
    }

    /**
     * <strong>[icu]</strong> Returns a new instance of BreakIterator that locates title boundaries.
     * The iterator returned locates title boundaries as described for
     * Unicode 3.2 only. For Unicode 4.0 and above title boundary iteration,
     * please use Word Boundary iterator.{@link #getWordInstance}
     * @param where A Locale specifying the language of the text being analyzed.
     * @return A new instance of BreakIterator that locates title boundaries.
     * @throws NullPointerException if <code>where</code> is null.*/
    public static BreakIterator getTitleInstance(ULocale where)
    {
        return getBreakInstance(where, KIND_TITLE);
    }

    /**
     * <strong>[icu]</strong> Registers a new break iterator of the indicated kind, to use in the given
     * locale.  Clones of the iterator will be returned if a request for a break iterator
     * of the given kind matches or falls back to this locale.
     *
     * <p>Because ICU may choose to cache BreakIterator objects internally, this must
     * be called at application startup, prior to any calls to
     * BreakIterator.getInstance to avoid undefined behavior.
     *
     * @param iter the BreakIterator instance to adopt.
     * @param locale the Locale for which this instance is to be registered
     * @param kind the type of iterator for which this instance is to be registered
     * @return a registry key that can be used to unregister this instance
     * @hide unsupported on Android
     */
    public static Object registerInstance(BreakIterator iter, Locale locale, int kind) {
        return registerInstance(iter, ULocale.forLocale(locale), kind);
    }

    /**
     * <strong>[icu]</strong> Registers a new break iterator of the indicated kind, to use in the given
     * locale.  Clones of the iterator will be returned if a request for a break iterator
     * of the given kind matches or falls back to this locale.
     *
     * <p>Because ICU may choose to cache BreakIterator objects internally, this must
     * be called at application startup, prior to any calls to
     * BreakIterator.getInstance to avoid undefined behavior.
     *
     * @param iter the BreakIterator instance to adopt.
     * @param locale the Locale for which this instance is to be registered
     * @param kind the type of iterator for which this instance is to be registered
     * @return a registry key that can be used to unregister this instance
     * @hide unsupported on Android
     */
    public static Object registerInstance(BreakIterator iter, ULocale locale, int kind) {
        // If the registered object matches the one in the cache, then
        // flush the cached object.
        if (iterCache[kind] != null) {
            BreakIteratorCache cache = (BreakIteratorCache) iterCache[kind].get();
            if (cache != null) {
                if (cache.getLocale().equals(locale)) {
                    iterCache[kind] = null;
                }
            }
        }
        return getShim().registerInstance(iter, locale, kind);
    }

    /**
     * <strong>[icu]</strong> Unregisters a previously-registered BreakIterator using the key returned
     * from the register call.  Key becomes invalid after this call and should not be used
     * again.
     * @param key the registry key returned by a previous call to registerInstance
     * @return true if the iterator for the key was successfully unregistered
     * @hide unsupported on Android
     */
    public static boolean unregister(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("registry key must not be null");
        }
        // TODO: we don't do code coverage for the following lines
        // because in getBreakInstance we always instantiate the shim,
        // and test execution is such that we always instantiate a
        // breakiterator before we get to the break iterator tests.
        // this is for modularization, and we could remove the
        // dependencies in getBreakInstance by rewriting part of the
        // LocaleData code, or perhaps by accepting it into the
        // module.
        ///CLOVER:OFF
        if (shim != null) {
            // Unfortunately, we don't know what is being unregistered
            // -- what `kind' and what locale -- so we flush all
            // caches.  This is safe but inefficient if people are
            // actively registering and unregistering.
            for (int kind=0; kind<KIND_COUNT; ++kind) {
                iterCache[kind] = null;
            }
            return shim.unregister(key);
        }
        return false;
        ///CLOVER:ON
    }

    // end of registration

    /**
     * Returns a particular kind of BreakIterator for a locale.
     * Avoids writing a switch statement with getXYZInstance(where) calls.
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public static BreakIterator getBreakInstance(ULocale where, int kind) {
        if (where == null) {
            throw new NullPointerException("Specified locale is null");
        }
        if (iterCache[kind] != null) {
            BreakIteratorCache cache = (BreakIteratorCache)iterCache[kind].get();
            if (cache != null) {
                if (cache.getLocale().equals(where)) {
                    return cache.createBreakInstance();
                }
            }
        }

        // sigh, all to avoid linking in ICULocaleData...
        BreakIterator result = getShim().createBreakIterator(where, kind);

        BreakIteratorCache cache = new BreakIteratorCache(where, result);
        iterCache[kind] = CacheValue.getInstance(cache);
        if (result instanceof RuleBasedBreakIterator) {
            RuleBasedBreakIterator rbbi = (RuleBasedBreakIterator)result;
            rbbi.setBreakType(kind);
        }

        return result;
    }


    /**
     * Returns a list of locales for which BreakIterators can be used.
     * @return An array of Locales.  All of the locales in the array can
     * be used when creating a BreakIterator.
     */
    public static synchronized Locale[] getAvailableLocales()
    {
        // to avoid linking ICULocaleData
        return getShim().getAvailableLocales();
    }

    /**
     * <strong>[icu]</strong> Returns a list of locales for which BreakIterators can be used.
     * @return An array of Locales.  All of the locales in the array can
     * be used when creating a BreakIterator.
     * @hide draft / provisional / internal are hidden on Android
     */
    public static synchronized ULocale[] getAvailableULocales()
    {
        // to avoid linking ICULocaleData
        return getShim().getAvailableULocales();
    }

    private static final class BreakIteratorCache {

        private BreakIterator iter;
        private ULocale where;

        BreakIteratorCache(ULocale where, BreakIterator iter) {
            this.where = where;
            this.iter = (BreakIterator) iter.clone();
        }

        ULocale getLocale() {
            return where;
        }

        BreakIterator createBreakInstance() {
            return (BreakIterator) iter.clone();
        }
    }

    static abstract class BreakIteratorServiceShim {
        public abstract Object registerInstance(BreakIterator iter, ULocale l, int k);
        public abstract boolean unregister(Object key);
        public abstract Locale[] getAvailableLocales();
        public abstract ULocale[] getAvailableULocales();
        public abstract BreakIterator createBreakIterator(ULocale l, int k);
    }

    private static BreakIteratorServiceShim shim;
    private static BreakIteratorServiceShim getShim() {
        // Note: this instantiation is safe on loose-memory-model configurations
        // despite lack of synchronization, since the shim instance has no state--
        // it's all in the class init.  The worst problem is we might instantiate
        // two shim instances, but they'll share the same state so that's ok.
        if (shim == null) {
            /* J2ObjC removed: use of reflection.
            try {
                Class<?> cls = Class.forName("android.icu.text.BreakIteratorFactory");
                shim = (BreakIteratorServiceShim)cls.newInstance();
            }
            catch (MissingResourceException e)
            {
                throw e;
            }
            catch (Exception e) {
                ///CLOVER:OFF
                if(DEBUG){
                    e.printStackTrace();
                }
                throw new RuntimeException(e.getMessage());
                ///CLOVER:ON
            } */
            shim = new android.icu.text.BreakIteratorFactory();
        }
        return shim;
    }

    // -------- BEGIN ULocale boilerplate --------

    /**
     * <strong>[icu]</strong> Returns the locale that was used to create this object, or null.
     * This may may differ from the locale requested at the time of
     * this object's creation.  For example, if an object is created
     * for locale <tt>en_US_CALIFORNIA</tt>, the actual data may be
     * drawn from <tt>en</tt> (the <i>actual</i> locale), and
     * <tt>en_US</tt> may be the most specific locale that exists (the
     * <i>valid</i> locale).
     *
     * <p>Note: The <i>actual</i> locale is returned correctly, but the <i>valid</i>
     * locale is not, in most cases.
     * @param type type of information requested, either {@link
     * android.icu.util.ULocale#VALID_LOCALE} or {@link
     * android.icu.util.ULocale#ACTUAL_LOCALE}.
     * @return the information specified by <i>type</i>, or null if
     * this object was not constructed from locale data.
     * @see android.icu.util.ULocale
     * @see android.icu.util.ULocale#VALID_LOCALE
     * @see android.icu.util.ULocale#ACTUAL_LOCALE
     * @hide draft / provisional / internal are hidden on Android
     */
    public final ULocale getLocale(ULocale.Type type) {
        return type == ULocale.ACTUAL_LOCALE ?
            this.actualLocale : this.validLocale;
    }

    /**
     * Set information about the locales that were used to create this
     * object.  If the object was not constructed from locale data,
     * both arguments should be set to null.  Otherwise, neither
     * should be null.  The actual locale must be at the same level or
     * less specific than the valid locale.  This method is intended
     * for use by factories or other entities that create objects of
     * this class.
     * @param valid the most specific locale containing any resource
     * data, or null
     * @param actual the locale containing data used to construct this
     * object, or null
     * @see android.icu.util.ULocale
     * @see android.icu.util.ULocale#VALID_LOCALE
     * @see android.icu.util.ULocale#ACTUAL_LOCALE
     */
    final void setLocale(ULocale valid, ULocale actual) {
        // Change the following to an assertion later
        if ((valid == null) != (actual == null)) {
            ///CLOVER:OFF
            throw new IllegalArgumentException();
            ///CLOVER:ON
        }
        // Another check we could do is that the actual locale is at
        // the same level or less specific than the valid locale.
        this.validLocale = valid;
        this.actualLocale = actual;
    }

    /**
     * The most specific locale containing any resource data, or null.
     * @see android.icu.util.ULocale
     */
    private ULocale validLocale;

    /**
     * The locale containing data used to construct this object, or
     * null.
     * @see android.icu.util.ULocale
     */
    private ULocale actualLocale;

    // -------- END ULocale boilerplate --------
}
