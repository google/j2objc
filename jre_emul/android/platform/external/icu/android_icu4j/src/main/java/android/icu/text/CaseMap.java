/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2017 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
package android.icu.text;

import java.util.Locale;

import android.icu.impl.CaseMapImpl;
import android.icu.impl.UCaseProps;
import android.icu.lang.UCharacter;
import android.icu.util.ULocale;

/**
 * Low-level case mapping options and methods. Immutable.
 * "Setters" return instances with the union of the current and new options set.
 *
 * This class is not intended for public subclassing.
 *
 * @hide Only a subset of ICU is exposed in Android
 * @hide draft / provisional / internal are hidden on Android
 */
public abstract class CaseMap {
    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected int internalOptions;

    private CaseMap(int opt) { internalOptions = opt; }

    private static int getCaseLocale(Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return UCaseProps.getCaseLocale(locale);
    }

    /**
     * @return Lowercasing object with default options.
     * @hide draft / provisional / internal are hidden on Android
     */
    public static Lower toLower() { return Lower.DEFAULT; }
    /**
     * @return Uppercasing object with default options.
     * @hide draft / provisional / internal are hidden on Android
     */
    public static Upper toUpper() { return Upper.DEFAULT; }
    /**
     * @return Titlecasing object with default options.
     * @hide draft / provisional / internal are hidden on Android
     */
    public static Title toTitle() { return Title.DEFAULT; }
    /**
     * @return Case folding object with default options.
     * @hide draft / provisional / internal are hidden on Android
     */
    public static Fold fold() { return Fold.DEFAULT; }

    /**
     * Returns an instance that behaves like this one but
     * omits unchanged text when case-mapping with {@link Edits}.
     *
     * @return an options object with this option.
     * @hide draft / provisional / internal are hidden on Android
     */
    public abstract CaseMap omitUnchangedText();

    /**
     * Lowercasing options and methods. Immutable.
     *
     * @see #toLower()
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final class Lower extends CaseMap {
        private static final Lower DEFAULT = new Lower(0);
        private static final Lower OMIT_UNCHANGED = new Lower(CaseMapImpl.OMIT_UNCHANGED_TEXT);
        private Lower(int opt) { super(opt); }

        /**
         * {@inheritDoc}
         * @hide draft / provisional / internal are hidden on Android
         */
        @Override
        public Lower omitUnchangedText() {
            return OMIT_UNCHANGED;
        }

        /**
         * Lowercases a string and optionally records edits (see {@link #omitUnchangedText}).
         * Casing is locale-dependent and context-sensitive.
         * The result may be longer or shorter than the original.
         *
         * @param locale    The locale ID. Can be null for {@link Locale#getDefault}.
         *                  (See {@link ULocale#toLocale}.)
         * @param src       The original string.
         * @param dest      A buffer for the result string. Must not be null.
         * @param edits     Records edits for index mapping, working with styled text,
         *                  and getting only changes (if any).
         *                  This function calls edits.reset() first. edits can be null.
         * @return dest with the result string (or only changes) appended.
         *
         * @see UCharacter#toLowerCase(Locale, String)
         * @hide draft / provisional / internal are hidden on Android
         */
         public <A extends Appendable> A apply(
                 Locale locale, CharSequence src, A dest, Edits edits) {
             return CaseMapImpl.toLower(getCaseLocale(locale), internalOptions, src, dest, edits);
         }
    }

    /**
     * Uppercasing options and methods. Immutable.
     *
     * @see #toUpper()
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final class Upper extends CaseMap {
        private static final Upper DEFAULT = new Upper(0);
        private static final Upper OMIT_UNCHANGED = new Upper(CaseMapImpl.OMIT_UNCHANGED_TEXT);
        private Upper(int opt) { super(opt); }

        /**
         * {@inheritDoc}
         * @hide draft / provisional / internal are hidden on Android
         */
        @Override
        public Upper omitUnchangedText() {
            return OMIT_UNCHANGED;
        }

        /**
         * Uppercases a string and optionally records edits (see {@link #omitUnchangedText}).
         * Casing is locale-dependent and context-sensitive.
         * The result may be longer or shorter than the original.
         *
         * @param locale    The locale ID. Can be null for {@link Locale#getDefault}.
         *                  (See {@link ULocale#toLocale}.)
         * @param src       The original string.
         * @param dest      A buffer for the result string. Must not be null.
         * @param edits     Records edits for index mapping, working with styled text,
         *                  and getting only changes (if any).
         *                  This function calls edits.reset() first. edits can be null.
         * @return dest with the result string (or only changes) appended.
         *
         * @see UCharacter#toUpperCase(Locale, String)
         * @hide draft / provisional / internal are hidden on Android
         */
         public <A extends Appendable> A apply(
                 Locale locale, CharSequence src, A dest, Edits edits) {
             return CaseMapImpl.toUpper(getCaseLocale(locale), internalOptions, src, dest, edits);
         }
    }

    /**
     * Titlecasing options and methods. Immutable.
     *
     * @see #toTitle()
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final class Title extends CaseMap {
        private static final Title DEFAULT = new Title(0);
        private static final Title OMIT_UNCHANGED = new Title(CaseMapImpl.OMIT_UNCHANGED_TEXT);
        private Title(int opt) { super(opt); }

        /**
         * {@inheritDoc}
         * @hide draft / provisional / internal are hidden on Android
         */
        @Override
        public Title omitUnchangedText() {
            if (internalOptions == 0 || internalOptions == CaseMapImpl.OMIT_UNCHANGED_TEXT) {
                return OMIT_UNCHANGED;
            }
            return new Title(internalOptions | CaseMapImpl.OMIT_UNCHANGED_TEXT);
        }

        /**
         * Returns an instance that behaves like this one but
         * does not lowercase non-initial parts of words when titlecasing.
         *
         * <p>By default, titlecasing will titlecase the first cased character
         * of a word and lowercase all other characters.
         * With this option, the other characters will not be modified.
         *
         * @return an options object with this option.
         * @see UCharacter#TITLECASE_NO_LOWERCASE
         * @hide draft / provisional / internal are hidden on Android
         */
        public Title noLowercase() {
            return new Title(internalOptions | UCharacter.TITLECASE_NO_LOWERCASE);
        }

        // TODO: update references to the Unicode Standard for recent version
        /**
         * Returns an instance that behaves like this one but
         * does not adjust the titlecasing indexes from BreakIterator::next() indexes;
         * titlecases exactly the characters at breaks from the iterator.
         *
         * <p>By default, titlecasing will take each break iterator index,
         * adjust it by looking for the next cased character, and titlecase that one.
         * Other characters are lowercased.
         *
         * <p>This follows Unicode 4 &amp; 5 section 3.13 Default Case Operations:
         *
         * R3  toTitlecase(X): Find the word boundaries based on Unicode Standard Annex
         * #29, "Text Boundaries." Between each pair of word boundaries, find the first
         * cased character F. If F exists, map F to default_title(F); then map each
         * subsequent character C to default_lower(C).
         *
         * @return an options object with this option.
         * @see UCharacter#TITLECASE_NO_BREAK_ADJUSTMENT
         * @hide draft / provisional / internal are hidden on Android
         */
        public Title noBreakAdjustment() {
            return new Title(internalOptions | UCharacter.TITLECASE_NO_BREAK_ADJUSTMENT);
        }

        /**
         * Titlecases a string and optionally records edits (see {@link #omitUnchangedText}).
         * Casing is locale-dependent and context-sensitive.
         * The result may be longer or shorter than the original.
         *
         * <p>Titlecasing uses a break iterator to find the first characters of words
         * that are to be titlecased. It titlecases those characters and lowercases
         * all others. (This can be modified with options bits.)
         *
         * @param locale    The locale ID. Can be null for {@link Locale#getDefault}.
         *                  (See {@link ULocale#toLocale}.)
         * @param iter      A break iterator to find the first characters of words that are to be titlecased.
         *                  It is set to the source string (setText())
         *                  and used one or more times for iteration (first() and next()).
         *                  If null, then a word break iterator for the locale is used
         *                  (or something equivalent).
         * @param src       The original string.
         * @param dest      A buffer for the result string. Must not be null.
         * @param edits     Records edits for index mapping, working with styled text,
         *                  and getting only changes (if any).
         *                  This function calls edits.reset() first. edits can be null.
         * @return dest with the result string (or only changes) appended.
         *
         * @see UCharacter#toTitleCase(Locale, String, BreakIterator, int)
         * @hide draft / provisional / internal are hidden on Android
         */
         public <A extends Appendable> A apply(
                 Locale locale, BreakIterator iter, CharSequence src, A dest, Edits edits) {
             if (iter == null) {
                 iter = BreakIterator.getWordInstance(locale);
             }
             iter.setText(src.toString());
             return CaseMapImpl.toTitle(
                     getCaseLocale(locale), internalOptions, iter, src, dest, edits);
         }
    }

    /**
     * Case folding options and methods. Immutable.
     *
     * @see #fold()
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final class Fold extends CaseMap {
        private static final Fold DEFAULT = new Fold(0);
        private static final Fold TURKIC = new Fold(UCharacter.FOLD_CASE_EXCLUDE_SPECIAL_I);
        private static final Fold OMIT_UNCHANGED = new Fold(CaseMapImpl.OMIT_UNCHANGED_TEXT);
        private static final Fold TURKIC_OMIT_UNCHANGED = new Fold(
                UCharacter.FOLD_CASE_EXCLUDE_SPECIAL_I | CaseMapImpl.OMIT_UNCHANGED_TEXT);
        private Fold(int opt) { super(opt); }

        /**
         * {@inheritDoc}
         * @hide draft / provisional / internal are hidden on Android
         */
        @Override
        public Fold omitUnchangedText() {
            return (internalOptions & UCharacter.FOLD_CASE_EXCLUDE_SPECIAL_I) == 0 ?
                    OMIT_UNCHANGED : TURKIC_OMIT_UNCHANGED;
        }

        /**
         * Returns an instance that behaves like this one but
         * handles dotted I and dotless i appropriately for Turkic languages (tr, az).
         *
         * <p>Uses the Unicode CaseFolding.txt mappings marked with 'T' that
         * are to be excluded for default mappings and
         * included for the Turkic-specific mappings.
         *
         * @return an options object with this option.
         * @see UCharacter#FOLD_CASE_EXCLUDE_SPECIAL_I
         * @hide draft / provisional / internal are hidden on Android
         */
        public Fold turkic() {
            return (internalOptions & CaseMapImpl.OMIT_UNCHANGED_TEXT) == 0 ?
                    TURKIC : TURKIC_OMIT_UNCHANGED;
        }

        /**
         * Case-folds a string and optionally records edits (see {@link #omitUnchangedText}).
         *
         * <p>Case-folding is locale-independent and not context-sensitive,
         * but there is an option for whether to include or exclude mappings for dotted I
         * and dotless i that are marked with 'T' in CaseFolding.txt.
         *
         * <p>The result may be longer or shorter than the original.
         *
         * @param src       The original string.
         * @param dest      A buffer for the result string. Must not be null.
         * @param edits     Records edits for index mapping, working with styled text,
         *                  and getting only changes (if any).
         *                  This function calls edits.reset() first. edits can be null.
         * @return dest with the result string (or only changes) appended.
         *
         * @see UCharacter#foldCase(String, int)
         * @hide draft / provisional / internal are hidden on Android
         */
         public <A extends Appendable> A apply(CharSequence src, A dest, Edits edits) {
             return CaseMapImpl.fold(internalOptions, src, dest, edits);
         }
    }
}
