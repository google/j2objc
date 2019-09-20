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

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;

import android.icu.impl.ICUData;
import android.icu.impl.ICUDebug;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.UResource;
import android.icu.impl.coll.CollationData;
import android.icu.impl.coll.CollationRoot;
import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;
import android.icu.lang.UScript;
import android.icu.util.Freezable;
import android.icu.util.ICUException;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import android.icu.util.UResourceBundle;
import android.icu.util.VersionInfo;

/**
* <strong>[icu enhancement]</strong> ICU's replacement for {@link java.text.Collator}.&nbsp;Methods, fields, and other functionality specific to ICU are labeled '<strong>[icu]</strong>'.
*
* <p>Collator performs locale-sensitive string comparison. A concrete
* subclass, RuleBasedCollator, allows customization of the collation
* ordering by the use of rule sets.
*
* <p>A Collator is thread-safe only when frozen. See {@link #isFrozen()} and {@link Freezable}.
*
* <p>Following the <a href=http://www.unicode.org>Unicode
* Consortium</a>'s specifications for the
* <a href="http://www.unicode.org/unicode/reports/tr10/">Unicode Collation
* Algorithm (UCA)</a>, there are 5 different levels of strength used
* in comparisons:
*
* <ul>
* <li>PRIMARY strength: Typically, this is used to denote differences between
*     base characters (for example, "a" &lt; "b").
*     It is the strongest difference. For example, dictionaries are divided
*     into different sections by base character.
* <li>SECONDARY strength: Accents in the characters are considered secondary
*     differences (for example, "as" &lt; "&agrave;s" &lt; "at"). Other
*     differences
*     between letters can also be considered secondary differences, depending
*     on the language. A secondary difference is ignored when there is a
*     primary difference anywhere in the strings.
* <li>TERTIARY strength: Upper and lower case differences in characters are
*     distinguished at tertiary strength (for example, "ao" &lt; "Ao" &lt;
*     "a&ograve;"). In addition, a variant of a letter differs from the base
*     form on the tertiary strength (such as "A" and "Ⓐ"). Another
*     example is the
*     difference between large and small Kana. A tertiary difference is ignored
*     when there is a primary or secondary difference anywhere in the strings.
* <li>QUATERNARY strength: When punctuation is ignored
*     (see <a href="http://userguide.icu-project.org/collation/concepts#TOC-Ignoring-Punctuation">
*     Ignoring Punctuations in the User Guide</a>) at PRIMARY to TERTIARY
*     strength, an additional strength level can
*     be used to distinguish words with and without punctuation (for example,
*     "ab" &lt; "a-b" &lt; "aB").
*     This difference is ignored when there is a PRIMARY, SECONDARY or TERTIARY
*     difference. The QUATERNARY strength should only be used if ignoring
*     punctuation is required.
* <li>IDENTICAL strength:
*     When all other strengths are equal, the IDENTICAL strength is used as a
*     tiebreaker. The Unicode code point values of the NFD form of each string
*     are compared, just in case there is no difference.
*     For example, Hebrew cantellation marks are only distinguished at this
*     strength. This strength should be used sparingly, as only code point
*     value differences between two strings is an extremely rare occurrence.
*     Using this strength substantially decreases the performance for both
*     comparison and collation key generation APIs. This strength also
*     increases the size of the collation key.
* </ul>
*
* Unlike the JDK, ICU4J's Collator deals only with 2 decomposition modes,
* the canonical decomposition mode and one that does not use any decomposition.
* The compatibility decomposition mode, java.text.Collator.FULL_DECOMPOSITION
* is not supported here. If the canonical
* decomposition mode is set, the Collator handles un-normalized text properly,
* producing the same results as if the text were normalized in NFD. If
* canonical decomposition is turned off, it is the user's responsibility to
* ensure that all text is already in the appropriate form before performing
* a comparison or before getting a CollationKey.
*
* <p>For more information about the collation service see the
* <a href="http://userguide.icu-project.org/collation">User Guide</a>.
*
* <p>Examples of use
* <pre>
* // Get the Collator for US English and set its strength to PRIMARY
* Collator usCollator = Collator.getInstance(Locale.US);
* usCollator.setStrength(Collator.PRIMARY);
* if (usCollator.compare("abc", "ABC") == 0) {
*     System.out.println("Strings are equivalent");
* }
*
* The following example shows how to compare two strings using the
* Collator for the default locale.
*
* // Compare two strings in the default locale
* Collator myCollator = Collator.getInstance();
* myCollator.setDecomposition(NO_DECOMPOSITION);
* if (myCollator.compare("&agrave;&#92;u0325", "a&#92;u0325&#768;") != 0) {
*     System.out.println("&agrave;&#92;u0325 is not equals to a&#92;u0325&#768; without decomposition");
*     myCollator.setDecomposition(CANONICAL_DECOMPOSITION);
*     if (myCollator.compare("&agrave;&#92;u0325", "a&#92;u0325&#768;") != 0) {
*         System.out.println("Error: &agrave;&#92;u0325 should be equals to a&#92;u0325&#768; with decomposition");
*     }
*     else {
*         System.out.println("&agrave;&#92;u0325 is equals to a&#92;u0325&#768; with decomposition");
*     }
* }
* else {
*     System.out.println("Error: &agrave;&#92;u0325 should be not equals to a&#92;u0325&#768; without decomposition");
* }
* </pre>
*
* @see RuleBasedCollator
* @see CollationKey
* @author Syn Wee Quek
*/
public abstract class Collator implements Comparator<Object>, Freezable<Collator>, Cloneable
{
    // public data members ---------------------------------------------------

    /**
     * Strongest collator strength value. Typically used to denote differences
     * between base characters. See class documentation for more explanation.
     * @see #setStrength
     * @see #getStrength
     */
    public final static int PRIMARY = 0;

    /**
     * Second level collator strength value.
     * Accents in the characters are considered secondary differences.
     * Other differences between letters can also be considered secondary
     * differences, depending on the language.
     * See class documentation for more explanation.
     * @see #setStrength
     * @see #getStrength
     */
    public final static int SECONDARY = 1;

    /**
     * Third level collator strength value.
     * Upper and lower case differences in characters are distinguished at this
     * strength level. In addition, a variant of a letter differs from the base
     * form on the tertiary level.
     * See class documentation for more explanation.
     * @see #setStrength
     * @see #getStrength
     */
    public final static int TERTIARY = 2;

    /**
     * <strong>[icu]</strong> Fourth level collator strength value.
     * When punctuation is ignored
     * (see <a href="http://userguide.icu-project.org/collation/concepts#TOC-Ignoring-Punctuation">
     * Ignoring Punctuation in the User Guide</a>) at PRIMARY to TERTIARY
     * strength, an additional strength level can
     * be used to distinguish words with and without punctuation.
     * See class documentation for more explanation.
     * @see #setStrength
     * @see #getStrength
     */
    public final static int QUATERNARY = 3;

    /**
     * Smallest Collator strength value. When all other strengths are equal,
     * the IDENTICAL strength is used as a tiebreaker. The Unicode code point
     * values of the NFD form of each string are compared, just in case there
     * is no difference.
     * See class documentation for more explanation.
     * <p>
     * Note this value is different from JDK's
     */
    public final static int IDENTICAL = 15;

    /**
     * <strong>[icu] Note:</strong> This is for backwards compatibility with Java APIs only.  It
     * should not be used, IDENTICAL should be used instead.  ICU's
     * collation does not support Java's FULL_DECOMPOSITION mode.
     */
    public final static int FULL_DECOMPOSITION = IDENTICAL;

    /**
     * Decomposition mode value. With NO_DECOMPOSITION set, Strings
     * will not be decomposed for collation. This is the default
     * decomposition setting unless otherwise specified by the locale
     * used to create the Collator.
     *
     * <p><strong>Note</strong> this value is different from the JDK's.
     * @see #CANONICAL_DECOMPOSITION
     * @see #getDecomposition
     * @see #setDecomposition
     */
    public final static int NO_DECOMPOSITION = 16;

    /**
     * Decomposition mode value. With CANONICAL_DECOMPOSITION set,
     * characters that are canonical variants according to the Unicode standard
     * will be decomposed for collation.
     *
     * <p>CANONICAL_DECOMPOSITION corresponds to Normalization Form D as
     * described in <a href="http://www.unicode.org/unicode/reports/tr15/">
     * Unicode Technical Report #15</a>.
     *
     * @see #NO_DECOMPOSITION
     * @see #getDecomposition
     * @see #setDecomposition
     */
    public final static int CANONICAL_DECOMPOSITION = 17;

    /**
     * Reordering codes for non-script groups that can be reordered under collation.
     *
     * @see #getReorderCodes
     * @see #setReorderCodes
     * @see #getEquivalentReorderCodes
     */
    public static interface ReorderCodes {
        /**
         * A special reordering code that is used to specify the default reordering codes for a locale.
         */
        public final static int DEFAULT          = -1;  // == UScript.INVALID_CODE
        /**
         * A special reordering code that is used to specify no reordering codes.
         */
        public final static int NONE          = UScript.UNKNOWN;
        /**
         * A special reordering code that is used to specify all other codes used for reordering except
         * for the codes listed as ReorderingCodes and those listed explicitly in a reordering.
         */
        public final static int OTHERS          = UScript.UNKNOWN;
        /**
         * Characters with the space property.
         * This is equivalent to the rule value "space".
         */
        public final static int SPACE          = 0x1000;
        /**
         * The first entry in the enumeration of reordering groups. This is intended for use in
         * range checking and enumeration of the reorder codes.
         */
        public final static int FIRST          = SPACE;
        /**
         * Characters with the punctuation property.
         * This is equivalent to the rule value "punct".
         */
        public final static int PUNCTUATION    = 0x1001;
        /**
         * Characters with the symbol property.
         * This is equivalent to the rule value "symbol".
         */
        public final static int SYMBOL         = 0x1002;
        /**
         * Characters with the currency property.
         * This is equivalent to the rule value "currency".
         */
        public final static int CURRENCY       = 0x1003;
        /**
         * Characters with the digit property.
         * This is equivalent to the rule value "digit".
         */
        public final static int DIGIT          = 0x1004;
        /**
         * One more than the highest normal ReorderCodes value.
         * @deprecated ICU 58 The numeric value may change over time, see ICU ticket #12420.
         * @hide unsupported on Android
         */
        @Deprecated
        public final static int LIMIT          = 0x1005;
    }

    // public methods --------------------------------------------------------

    /**
     * Compares the equality of two Collator objects. Collator objects are equal if they have the same
     * collation (sorting &amp; searching) behavior.
     *
     * <p>The base class checks for null and for equal types.
     * Subclasses should override.
     *
     * @param obj the Collator to compare to.
     * @return true if this Collator has exactly the same collation behavior as obj, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        // Subclasses: Call this method and then add more specific checks.
        return this == obj || (obj != null && getClass() == obj.getClass());
    }

    /**
     * Generates a hash code for this Collator object.
     *
     * <p>The implementation exists just for consistency with {@link #equals(Object)}
     * implementation in this class and does not generate a useful hash code.
     * Subclasses should override this implementation.
     *
     * @return a hash code value.
     */
    @Override
    public int hashCode() {
        // Dummy return to prevent compile warnings.
        return 0;
    }

    // public setters --------------------------------------------------------

    private void checkNotFrozen() {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen Collator");
        }
    }

    /**
     * Sets this Collator's strength attribute. The strength attribute
     * determines the minimum level of difference considered significant
     * during comparison.
     *
     * <p>The base class method does nothing. Subclasses should override it if appropriate.
     *
     * <p>See the Collator class description for an example of use.
     * @param newStrength the new strength value.
     * @see #getStrength
     * @see #PRIMARY
     * @see #SECONDARY
     * @see #TERTIARY
     * @see #QUATERNARY
     * @see #IDENTICAL
     * @throws IllegalArgumentException if the new strength value is not valid.
     */
    public void setStrength(int newStrength)
    {
        checkNotFrozen();
    }

    /**
     * @return this, for chaining
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public Collator setStrength2(int newStrength)
    {
        setStrength(newStrength);
        return this;
    }

    /**
     * Sets the decomposition mode of this Collator.  Setting this
     * decomposition attribute with CANONICAL_DECOMPOSITION allows the
     * Collator to handle un-normalized text properly, producing the
     * same results as if the text were normalized. If
     * NO_DECOMPOSITION is set, it is the user's responsibility to
     * insure that all text is already in the appropriate form before
     * a comparison or before getting a CollationKey. Adjusting
     * decomposition mode allows the user to select between faster and
     * more complete collation behavior.
     *
     * <p>Since a great many of the world's languages do not require
     * text normalization, most locales set NO_DECOMPOSITION as the
     * default decomposition mode.
     *
     * <p>The base class method does nothing. Subclasses should override it if appropriate.
     *
     * <p>See getDecomposition for a description of decomposition
     * mode.
     *
     * @param decomposition the new decomposition mode
     * @see #getDecomposition
     * @see #NO_DECOMPOSITION
     * @see #CANONICAL_DECOMPOSITION
     * @throws IllegalArgumentException If the given value is not a valid
     *            decomposition mode.
     */
    public void setDecomposition(int decomposition)
    {
        checkNotFrozen();
    }

    /**
     * Sets the reordering codes for this collator.
     * Collation reordering allows scripts and some other groups of characters
     * to be moved relative to each other. This reordering is done on top of
     * the DUCET/CLDR standard collation order. Reordering can specify groups to be placed
     * at the start and/or the end of the collation order. These groups are specified using
     * UScript codes and {@link Collator.ReorderCodes} entries.
     *
     * <p>By default, reordering codes specified for the start of the order are placed in the
     * order given after several special non-script blocks. These special groups of characters
     * are space, punctuation, symbol, currency, and digit. These special groups are represented with
     * {@link Collator.ReorderCodes} entries. Script groups can be intermingled with
     * these special non-script groups if those special groups are explicitly specified in the reordering.
     *
     * <p>The special code {@link Collator.ReorderCodes#OTHERS OTHERS}
     * stands for any script that is not explicitly
     * mentioned in the list of reordering codes given. Anything that is after OTHERS
     * will go at the very end of the reordering in the order given.
     *
     * <p>The special reorder code {@link Collator.ReorderCodes#DEFAULT DEFAULT}
     * will reset the reordering for this collator
     * to the default for this collator. The default reordering may be the DUCET/CLDR order or may be a reordering that
     * was specified when this collator was created from resource data or from rules. The
     * DEFAULT code <b>must</b> be the sole code supplied when it is used.
     * If not, then an {@link IllegalArgumentException} will be thrown.
     *
     * <p>The special reorder code {@link Collator.ReorderCodes#NONE NONE}
     * will remove any reordering for this collator.
     * The result of setting no reordering will be to have the DUCET/CLDR ordering used. The
     * NONE code <b>must</b> be the sole code supplied when it is used.
     *
     * @param order the reordering codes to apply to this collator; if this is null or an empty array
     * then this clears any existing reordering
     * @see #getReorderCodes
     * @see #getEquivalentReorderCodes
     * @see Collator.ReorderCodes
     * @see UScript
     */
    public void setReorderCodes(int... order)
    {
        throw new UnsupportedOperationException("Needs to be implemented by the subclass.");
    }

    // public getters --------------------------------------------------------

    /**
     * Returns the Collator for the current default locale.
     * The default locale is determined by java.util.Locale.getDefault().
     * @return the Collator for the default locale (for example, en_US) if it
     *         is created successfully. Otherwise if there is no Collator
     *         associated with the current locale, the root collator
     *         will be returned.
     * @see java.util.Locale#getDefault()
     * @see #getInstance(Locale)
     */
    public static final Collator getInstance()
    {
        return getInstance(ULocale.getDefault());
    }

    /**
     * Clones the collator.
     * @return a clone of this collator.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    // begin registry stuff

    /**
     * A factory used with registerFactory to register multiple collators and provide
     * display names for them.  If standard locale display names are sufficient,
     * Collator instances may be registered instead.
     * <p><b>Note:</b> as of ICU4J 3.2, the default API for CollatorFactory uses
     * ULocale instead of Locale.  Instead of overriding createCollator(Locale),
     * new implementations should override createCollator(ULocale).  Note that
     * one of these two methods <b>MUST</b> be overridden or else an infinite
     * loop will occur.
     * @hide unsupported on Android
     */
    public static abstract class CollatorFactory {
        /**
         * Return true if this factory will be visible.  Default is true.
         * If not visible, the locales supported by this factory will not
         * be listed by getAvailableLocales.
         *
         * @return true if this factory is visible
         */
        public boolean visible() {
            return true;
        }

        /**
         * Return an instance of the appropriate collator.  If the locale
         * is not supported, return null.
         * <b>Note:</b> as of ICU4J 3.2, implementations should override
         * this method instead of createCollator(Locale).
         * @param loc the locale for which this collator is to be created.
         * @return the newly created collator.
         */
        public Collator createCollator(ULocale loc) {
            return createCollator(loc.toLocale());
        }

        /**
         * Return an instance of the appropriate collator.  If the locale
         * is not supported, return null.
         * <p><b>Note:</b> as of ICU4J 3.2, implementations should override
         * createCollator(ULocale) instead of this method, and inherit this
         * method's implementation.  This method is no longer abstract
         * and instead delegates to createCollator(ULocale).
         * @param loc the locale for which this collator is to be created.
         * @return the newly created collator.
         */
         public Collator createCollator(Locale loc) {
            return createCollator(ULocale.forLocale(loc));
        }

        /**
         * Return the name of the collator for the objectLocale, localized for the displayLocale.
         * If objectLocale is not visible or not defined by the factory, return null.
         * @param objectLocale the locale identifying the collator
         * @param displayLocale the locale for which the display name of the collator should be localized
         * @return the display name
         */
        public String getDisplayName(Locale objectLocale, Locale displayLocale) {
            return getDisplayName(ULocale.forLocale(objectLocale), ULocale.forLocale(displayLocale));
        }

        /**
         * Return the name of the collator for the objectLocale, localized for the displayLocale.
         * If objectLocale is not visible or not defined by the factory, return null.
         * @param objectLocale the locale identifying the collator
         * @param displayLocale the locale for which the display name of the collator should be localized
         * @return the display name
         */
        public String getDisplayName(ULocale objectLocale, ULocale displayLocale) {
            if (visible()) {
                Set<String> supported = getSupportedLocaleIDs();
                String name = objectLocale.getBaseName();
                if (supported.contains(name)) {
                    return objectLocale.getDisplayName(displayLocale);
                }
            }
            return null;
        }

        /**
         * Return an unmodifiable collection of the locale names directly
         * supported by this factory.
         *
         * @return the set of supported locale IDs.
         */
        public abstract Set<String> getSupportedLocaleIDs();

        /**
         * Empty default constructor.
         */
        protected CollatorFactory() {
        }
    }

    static abstract class ServiceShim {
        abstract Collator getInstance(ULocale l);
        abstract Object registerInstance(Collator c, ULocale l);
        abstract Object registerFactory(CollatorFactory f);
        abstract boolean unregister(Object k);
        abstract Locale[] getAvailableLocales(); // TODO remove
        abstract ULocale[] getAvailableULocales();
        abstract String getDisplayName(ULocale ol, ULocale dl);
    }

    private static ServiceShim shim;
    private static ServiceShim getShim() {
        // Note: this instantiation is safe on loose-memory-model configurations
        // despite lack of synchronization, since the shim instance has no state--
        // it's all in the class init.  The worst problem is we might instantiate
        // two shim instances, but they'll share the same state so that's ok.
        if (shim == null) {
            try {
                Class<?> cls = Class.forName("android.icu.text.CollatorServiceShim");
                shim = (ServiceShim)cls.newInstance();
            }
            catch (MissingResourceException e)
            {
                ///CLOVER:OFF
                throw e;
                ///CLOVER:ON
            }
            catch (Exception e) {
                ///CLOVER:OFF
                if(DEBUG){
                    e.printStackTrace();
                }
                throw new ICUException(e);
                ///CLOVER:ON
            }
        }
        return shim;
    }

    /**
     * Simpler/faster methods for ASCII than ones based on Unicode data.
     * TODO: There should be code like this somewhere already??
     */
    private static final class ASCII {
        static boolean equalIgnoreCase(CharSequence left, CharSequence right) {
            int length = left.length();
            if (length != right.length()) { return false; }
            for (int i = 0; i < length; ++i) {
                char lc = left.charAt(i);
                char rc = right.charAt(i);
                if (lc == rc) { continue; }
                if ('A' <= lc && lc <= 'Z') {
                    if ((lc + 0x20) == rc) { continue; }
                } else if ('A' <= rc && rc <= 'Z') {
                    if ((rc + 0x20) == lc) { continue; }
                }
                return false;
            }
            return true;
        }
    }

    private static final boolean getYesOrNo(String keyword, String s) {
        if (ASCII.equalIgnoreCase(s, "yes")) {
            return true;
        }
        if (ASCII.equalIgnoreCase(s, "no")) {
            return false;
        }
        throw new IllegalArgumentException("illegal locale keyword=value: " + keyword + "=" + s);
    }

    private static final int getIntValue(String keyword, String s, String... values) {
        for (int i = 0; i < values.length; ++i) {
            if (ASCII.equalIgnoreCase(s, values[i])) {
                return i;
            }
        }
        throw new IllegalArgumentException("illegal locale keyword=value: " + keyword + "=" + s);
    }

    private static final int getReorderCode(String keyword, String s) {
        return Collator.ReorderCodes.FIRST +
                getIntValue(keyword, s, "space", "punct", "symbol", "currency", "digit");
        // Not supporting "others" = UCOL_REORDER_CODE_OTHERS
        // as a synonym for Zzzz = USCRIPT_UNKNOWN for now:
        // Avoid introducing synonyms/aliases.
    }

    /**
     * Sets collation attributes according to locale keywords. See
     * http://www.unicode.org/reports/tr35/tr35-collation.html#Collation_Settings
     *
     * Using "alias" keywords and values where defined:
     * http://www.unicode.org/reports/tr35/tr35.html#Old_Locale_Extension_Syntax
     * http://unicode.org/repos/cldr/trunk/common/bcp47/collation.xml
     */
    private static void setAttributesFromKeywords(ULocale loc, Collator coll, RuleBasedCollator rbc) {
        // Check for collation keywords that were already deprecated
        // before any were supported in createInstance() (except for "collation").
        String value = loc.getKeywordValue("colHiraganaQuaternary");
        if (value != null) {
            throw new UnsupportedOperationException("locale keyword kh/colHiraganaQuaternary");
        }
        value = loc.getKeywordValue("variableTop");
        if (value != null) {
            throw new UnsupportedOperationException("locale keyword vt/variableTop");
        }
        // Parse known collation keywords, ignore others.
        value = loc.getKeywordValue("colStrength");
        if (value != null) {
            // Note: Not supporting typo "quarternary" because it was never supported in locale IDs.
            int strength = getIntValue("colStrength", value,
                    "primary", "secondary", "tertiary", "quaternary", "identical");
            coll.setStrength(strength <= Collator.QUATERNARY ? strength : Collator.IDENTICAL);
        }
        value = loc.getKeywordValue("colBackwards");
        if (value != null) {
            if (rbc != null) {
                rbc.setFrenchCollation(getYesOrNo("colBackwards", value));
            } else {
                throw new UnsupportedOperationException(
                        "locale keyword kb/colBackwards only settable for RuleBasedCollator");
            }
        }
        value = loc.getKeywordValue("colCaseLevel");
        if (value != null) {
            if (rbc != null) {
                rbc.setCaseLevel(getYesOrNo("colCaseLevel", value));
            } else {
                throw new UnsupportedOperationException(
                        "locale keyword kb/colBackwards only settable for RuleBasedCollator");
            }
        }
        value = loc.getKeywordValue("colCaseFirst");
        if (value != null) {
            if (rbc != null) {
                int cf = getIntValue("colCaseFirst", value, "no", "lower", "upper");
                if (cf == 0) {
                    rbc.setLowerCaseFirst(false);
                    rbc.setUpperCaseFirst(false);
                } else if (cf == 1) {
                    rbc.setLowerCaseFirst(true);
                } else /* cf == 2 */ {
                    rbc.setUpperCaseFirst(true);
                }
            } else {
                throw new UnsupportedOperationException(
                        "locale keyword kf/colCaseFirst only settable for RuleBasedCollator");
            }
        }
        value = loc.getKeywordValue("colAlternate");
        if (value != null) {
            if (rbc != null) {
                rbc.setAlternateHandlingShifted(
                        getIntValue("colAlternate", value, "non-ignorable", "shifted") != 0);
            } else {
                throw new UnsupportedOperationException(
                        "locale keyword ka/colAlternate only settable for RuleBasedCollator");
            }
        }
        value = loc.getKeywordValue("colNormalization");
        if (value != null) {
            coll.setDecomposition(getYesOrNo("colNormalization", value) ?
                    Collator.CANONICAL_DECOMPOSITION : Collator.NO_DECOMPOSITION);
        }
        value = loc.getKeywordValue("colNumeric");
        if (value != null) {
            if (rbc != null) {
                rbc.setNumericCollation(getYesOrNo("colNumeric", value));
            } else {
                throw new UnsupportedOperationException(
                        "locale keyword kn/colNumeric only settable for RuleBasedCollator");
            }
        }
        value = loc.getKeywordValue("colReorder");
        if (value != null) {
            int[] codes = new int[UScript.CODE_LIMIT + Collator.ReorderCodes.LIMIT - Collator.ReorderCodes.FIRST];
            int codesLength = 0;
            int scriptNameStart = 0;
            for (;;) {
                if (codesLength == codes.length) {
                    throw new IllegalArgumentException(
                            "too many script codes for colReorder locale keyword: " + value);
                }
                int limit = scriptNameStart;
                while (limit < value.length() && value.charAt(limit) != '-') { ++limit; }
                String scriptName = value.substring(scriptNameStart, limit);
                int code;
                if (scriptName.length() == 4) {
                    // Strict parsing, accept only 4-letter script codes, not long names.
                    code = UCharacter.getPropertyValueEnum(UProperty.SCRIPT, scriptName);
                } else {
                    code = getReorderCode("colReorder", scriptName);
                }
                codes[codesLength++] = code;
                if (limit == value.length()) { break; }
                scriptNameStart = limit + 1;
            }
            if (codesLength == 0) {
                throw new IllegalArgumentException("no script codes for colReorder locale keyword");
            }
            int[] args = new int[codesLength];
            System.arraycopy(codes, 0, args, 0, codesLength);
            coll.setReorderCodes(args);
        }
        value = loc.getKeywordValue("kv");
        if (value != null) {
            coll.setMaxVariable(getReorderCode("kv", value));
        }
    }

    /**
     * <strong>[icu]</strong> Returns the Collator for the desired locale.
     *
     * <p>For some languages, multiple collation types are available;
     * for example, "de@collation=phonebook".
     * Starting with ICU 54, collation attributes can be specified via locale keywords as well,
     * in the old locale extension syntax ("el@colCaseFirst=upper")
     * or in language tag syntax ("el-u-kf-upper").
     * See <a href="http://userguide.icu-project.org/collation/api">User Guide: Collation API</a>.
     *
     * @param locale the desired locale.
     * @return Collator for the desired locale if it is created successfully.
     *         Otherwise if there is no Collator
     *         associated with the current locale, the root collator will
     *         be returned.
     * @see java.util.Locale
     * @see java.util.ResourceBundle
     * @see #getInstance(Locale)
     * @see #getInstance()
     */
    public static final Collator getInstance(ULocale locale) {
        // fetching from service cache is faster than instantiation
        if (locale == null) {
            locale = ULocale.getDefault();
        }
        Collator coll = getShim().getInstance(locale);
        if (!locale.getName().equals(locale.getBaseName())) {  // any keywords?
            setAttributesFromKeywords(locale, coll,
                    (coll instanceof RuleBasedCollator) ? (RuleBasedCollator)coll : null);
        }
        return coll;
    }

    /**
     * Returns the Collator for the desired locale.
     *
     * <p>For some languages, multiple collation types are available;
     * for example, "de-u-co-phonebk".
     * Starting with ICU 54, collation attributes can be specified via locale keywords as well,
     * in the old locale extension syntax ("el@colCaseFirst=upper", only with {@link ULocale})
     * or in language tag syntax ("el-u-kf-upper").
     * See <a href="http://userguide.icu-project.org/collation/api">User Guide: Collation API</a>.
     *
     * @param locale the desired locale.
     * @return Collator for the desired locale if it is created successfully.
     *         Otherwise if there is no Collator
     *         associated with the current locale, the root collator will
     *         be returned.
     * @see java.util.Locale
     * @see java.util.ResourceBundle
     * @see #getInstance(ULocale)
     * @see #getInstance()
     */
    public static final Collator getInstance(Locale locale) {
        return getInstance(ULocale.forLocale(locale));
    }

    /**
     * <strong>[icu]</strong> Registers a collator as the default collator for the provided locale.  The
     * collator should not be modified after it is registered.
     *
     * <p>Because ICU may choose to cache Collator objects internally, this must
     * be called at application startup, prior to any calls to
     * Collator.getInstance to avoid undefined behavior.
     *
     * @param collator the collator to register
     * @param locale the locale for which this is the default collator
     * @return an object that can be used to unregister the registered collator.
     *
     * @hide unsupported on Android
     */
    public static final Object registerInstance(Collator collator, ULocale locale) {
        return getShim().registerInstance(collator, locale);
    }

    /**
     * <strong>[icu]</strong> Registers a collator factory.
     *
     * <p>Because ICU may choose to cache Collator objects internally, this must
     * be called at application startup, prior to any calls to
     * Collator.getInstance to avoid undefined behavior.
     *
     * @param factory the factory to register
     * @return an object that can be used to unregister the registered factory.
     *
     * @hide unsupported on Android
     */
    public static final Object registerFactory(CollatorFactory factory) {
        return getShim().registerFactory(factory);
    }

    /**
     * <strong>[icu]</strong> Unregisters a collator previously registered using registerInstance.
     * @param registryKey the object previously returned by registerInstance.
     * @return true if the collator was successfully unregistered.
     * @hide unsupported on Android
     */
    public static final boolean unregister(Object registryKey) {
        if (shim == null) {
            return false;
        }
        return shim.unregister(registryKey);
    }

    /**
     * Returns the set of locales, as Locale objects, for which collators
     * are installed.  Note that Locale objects do not support RFC 3066.
     * @return the list of locales in which collators are installed.
     * This list includes any that have been registered, in addition to
     * those that are installed with ICU4J.
     */
    public static Locale[] getAvailableLocales() {
        // TODO make this wrap getAvailableULocales later
        if (shim == null) {
            return ICUResourceBundle.getAvailableLocales(
                ICUData.ICU_COLLATION_BASE_NAME, ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        }
        return shim.getAvailableLocales();
    }

    /**
     * <strong>[icu]</strong> Returns the set of locales, as ULocale objects, for which collators
     * are installed.  ULocale objects support RFC 3066.
     * @return the list of locales in which collators are installed.
     * This list includes any that have been registered, in addition to
     * those that are installed with ICU4J.
     */
    public static final ULocale[] getAvailableULocales() {
        if (shim == null) {
            return ICUResourceBundle.getAvailableULocales(
                ICUData.ICU_COLLATION_BASE_NAME, ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        }
        return shim.getAvailableULocales();
    }

    /**
     * The list of keywords for this service.  This must be kept in sync with
     * the resource data.
     */
    private static final String[] KEYWORDS = { "collation" };

    /**
     * The resource name for this service.  Note that this is not the same as
     * the keyword for this service.
     */
    private static final String RESOURCE = "collations";

    /**
     * The resource bundle base name for this service.
     * *since ICU 3.0
     */

    private static final String BASE = ICUData.ICU_COLLATION_BASE_NAME;

    /**
     * <strong>[icu]</strong> Returns an array of all possible keywords that are relevant to
     * collation. At this point, the only recognized keyword for this
     * service is "collation".
     * @return an array of valid collation keywords.
     * @see #getKeywordValues
     */
    public static final String[] getKeywords() {
        return KEYWORDS;
    }

    /**
     * <strong>[icu]</strong> Given a keyword, returns an array of all values for
     * that keyword that are currently in use.
     * @param keyword one of the keywords returned by getKeywords.
     * @see #getKeywords
     */
    public static final String[] getKeywordValues(String keyword) {
        if (!keyword.equals(KEYWORDS[0])) {
            throw new IllegalArgumentException("Invalid keyword: " + keyword);
        }
        return ICUResourceBundle.getKeywordValues(BASE, RESOURCE);
    }

    /**
     * <strong>[icu]</strong> Given a key and a locale, returns an array of string values in a preferred
     * order that would make a difference. These are all and only those values where
     * the open (creation) of the service with the locale formed from the input locale
     * plus input keyword and that value has different behavior than creation with the
     * input locale alone.
     * @param key           one of the keys supported by this service.  For now, only
     *                      "collation" is supported.
     * @param locale        the locale
     * @param commonlyUsed  if set to true it will return only commonly used values
     *                      with the given locale in preferred order.  Otherwise,
     *                      it will return all the available values for the locale.
     * @return an array of string values for the given key and the locale.
     */
    public static final String[] getKeywordValuesForLocale(String key, ULocale locale,
                                                           boolean commonlyUsed) {
        // Note: The parameter commonlyUsed is not used.
        // The switch is in the method signature for consistency
        // with other locale services.

        // Read available collation values from collation bundles.
        ICUResourceBundle bundle = (ICUResourceBundle)
                UResourceBundle.getBundleInstance(
                        ICUData.ICU_COLLATION_BASE_NAME, locale);
        KeywordsSink sink = new KeywordsSink();
        bundle.getAllItemsWithFallback("collations", sink);
        return sink.values.toArray(new String[sink.values.size()]);
    }

    private static final class KeywordsSink extends UResource.Sink {
        LinkedList<String> values = new LinkedList<String>();
        boolean hasDefault = false;

        @Override
        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            UResource.Table collations = value.getTable();
            for (int i = 0; collations.getKeyAndValue(i, key, value); ++i) {
                int type = value.getType();
                if (type == UResourceBundle.STRING) {
                    if (!hasDefault && key.contentEquals("default")) {
                        String defcoll = value.getString();
                        if (!defcoll.isEmpty()) {
                            values.remove(defcoll);
                            values.addFirst(defcoll);
                            hasDefault = true;
                        }
                    }
                } else if (type == UResourceBundle.TABLE && !key.startsWith("private-")) {
                    String collkey = key.toString();
                    if (!values.contains(collkey)) {
                        values.add(collkey);
                    }
                }
            }
        }
    }

    /**
     * <strong>[icu]</strong> Returns the functionally equivalent locale for the given
     * requested locale, with respect to given keyword, for the
     * collation service.  If two locales return the same result, then
     * collators instantiated for these locales will behave
     * equivalently.  The converse is not always true; two collators
     * may in fact be equivalent, but return different results, due to
     * internal details.  The return result has no other meaning than
     * that stated above, and implies nothing as to the relationship
     * between the two locales.  This is intended for use by
     * applications who wish to cache collators, or otherwise reuse
     * collators when possible.  The functional equivalent may change
     * over time.  For more information, please see the <a
     * href="http://userguide.icu-project.org/locale#TOC-Locales-and-Services">
     * Locales and Services</a> section of the ICU User Guide.
     * @param keyword a particular keyword as enumerated by
     * getKeywords.
     * @param locID The requested locale
     * @param isAvailable If non-null, isAvailable[0] will receive and
     * output boolean that indicates whether the requested locale was
     * 'available' to the collation service. If non-null, isAvailable
     * must have length &gt;= 1.
     * @return the locale
     */
    public static final ULocale getFunctionalEquivalent(String keyword,
                                                        ULocale locID,
                                                        boolean isAvailable[]) {
        return ICUResourceBundle.getFunctionalEquivalent(BASE, ICUResourceBundle.ICU_DATA_CLASS_LOADER, RESOURCE,
                                                         keyword, locID, isAvailable, true);
    }

    /**
     * <strong>[icu]</strong> Returns the functionally equivalent locale for the given
     * requested locale, with respect to given keyword, for the
     * collation service.
     * @param keyword a particular keyword as enumerated by
     * getKeywords.
     * @param locID The requested locale
     * @return the locale
     * @see #getFunctionalEquivalent(String,ULocale,boolean[])
     */
    public static final ULocale getFunctionalEquivalent(String keyword,
                                                        ULocale locID) {
        return getFunctionalEquivalent(keyword, locID, null);
    }

    /**
     * <strong>[icu]</strong> Returns the name of the collator for the objectLocale, localized for the
     * displayLocale.
     * @param objectLocale the locale of the collator
     * @param displayLocale the locale for the collator's display name
     * @return the display name
     */
    static public String getDisplayName(Locale objectLocale, Locale displayLocale) {
        return getShim().getDisplayName(ULocale.forLocale(objectLocale),
                                        ULocale.forLocale(displayLocale));
    }

    /**
     * <strong>[icu]</strong> Returns the name of the collator for the objectLocale, localized for the
     * displayLocale.
     * @param objectLocale the locale of the collator
     * @param displayLocale the locale for the collator's display name
     * @return the display name
     */
    static public String getDisplayName(ULocale objectLocale, ULocale displayLocale) {
        return getShim().getDisplayName(objectLocale, displayLocale);
    }

    /**
     * <strong>[icu]</strong> Returns the name of the collator for the objectLocale, localized for the
     * default <code>DISPLAY</code> locale.
     * @param objectLocale the locale of the collator
     * @return the display name
     * @see android.icu.util.ULocale.Category#DISPLAY
     */
    static public String getDisplayName(Locale objectLocale) {
        return getShim().getDisplayName(ULocale.forLocale(objectLocale), ULocale.getDefault(Category.DISPLAY));
    }

    /**
     * <strong>[icu]</strong> Returns the name of the collator for the objectLocale, localized for the
     * default <code>DISPLAY</code> locale.
     * @param objectLocale the locale of the collator
     * @return the display name
     * @see android.icu.util.ULocale.Category#DISPLAY
     */
    static public String getDisplayName(ULocale objectLocale) {
        return getShim().getDisplayName(objectLocale, ULocale.getDefault(Category.DISPLAY));
    }

    /**
     * Returns this Collator's strength attribute. The strength attribute
     * determines the minimum level of difference considered significant.
     * <strong>[icu] Note:</strong> This can return QUATERNARY strength, which is not supported by the
     * JDK version.
     * <p>
     * See the Collator class description for more details.
     * <p>The base class method always returns {@link #TERTIARY}.
     * Subclasses should override it if appropriate.
     *
     * @return this Collator's current strength attribute.
     * @see #setStrength
     * @see #PRIMARY
     * @see #SECONDARY
     * @see #TERTIARY
     * @see #QUATERNARY
     * @see #IDENTICAL
     */
    public int getStrength()
    {
        return TERTIARY;
    }

    /**
     * Returns the decomposition mode of this Collator. The decomposition mode
     * determines how Unicode composed characters are handled.
     * <p>
     * See the Collator class description for more details.
     * <p>The base class method always returns {@link #NO_DECOMPOSITION}.
     * Subclasses should override it if appropriate.
     *
     * @return the decomposition mode
     * @see #setDecomposition
     * @see #NO_DECOMPOSITION
     * @see #CANONICAL_DECOMPOSITION
     */
    public int getDecomposition()
    {
        return NO_DECOMPOSITION;
    }

    // public other methods -------------------------------------------------

    /**
     * Compares the equality of two text Strings using
     * this Collator's rules, strength and decomposition mode.  Convenience method.
     * @param source the source string to be compared.
     * @param target the target string to be compared.
     * @return true if the strings are equal according to the collation
     *         rules, otherwise false.
     * @see #compare
     * @throws NullPointerException thrown if either arguments is null.
     */
    public boolean equals(String source, String target)
    {
        return (compare(source, target) == 0);
    }

    /**
     * <strong>[icu]</strong> Returns a UnicodeSet that contains all the characters and sequences tailored
     * in this collator.
     * @return a pointer to a UnicodeSet object containing all the
     *         code points and sequences that may sort differently than
     *         in the root collator.
     */
    public UnicodeSet getTailoredSet()
    {
        return new UnicodeSet(0, 0x10FFFF);
    }

    /**
     * Compares the source text String to the target text String according to
     * this Collator's rules, strength and decomposition mode.
     * Returns an integer less than,
     * equal to or greater than zero depending on whether the source String is
     * less than, equal to or greater than the target String. See the Collator
     * class description for an example of use.
     *
     * @param source the source String.
     * @param target the target String.
     * @return Returns an integer value. Value is less than zero if source is
     *         less than target, value is zero if source and target are equal,
     *         value is greater than zero if source is greater than target.
     * @see CollationKey
     * @see #getCollationKey
     * @throws NullPointerException thrown if either argument is null.
     */
    public abstract int compare(String source, String target);

    /**
     * Compares the source Object to the target Object.
     *
     * @param source the source Object.
     * @param target the target Object.
     * @return Returns an integer value. Value is less than zero if source is
     *         less than target, value is zero if source and target are equal,
     *         value is greater than zero if source is greater than target.
     * @throws ClassCastException thrown if either arguments cannot be cast to CharSequence.
     */
    @Override
    public int compare(Object source, Object target) {
        return doCompare((CharSequence)source, (CharSequence)target);
    }

    /**
     * Compares two CharSequences.
     * The base class just calls compare(left.toString(), right.toString()).
     * Subclasses should instead implement this method and have the String API call this method.
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected int doCompare(CharSequence left, CharSequence right) {
        return compare(left.toString(), right.toString());
    }

    /**
     * <p>
     * Transforms the String into a CollationKey suitable for efficient
     * repeated comparison.  The resulting key depends on the collator's
     * rules, strength and decomposition mode.
     *
     * <p>Note that collation keys are often less efficient than simply doing comparison.
     * For more details, see the ICU User Guide.
     *
     * <p>See the CollationKey class documentation for more information.
     * @param source the string to be transformed into a CollationKey.
     * @return the CollationKey for the given String based on this Collator's
     *         collation rules. If the source String is null, a null
     *         CollationKey is returned.
     * @see CollationKey
     * @see #compare(String, String)
     * @see #getRawCollationKey
     */
    public abstract CollationKey getCollationKey(String source);

    /**
     * <strong>[icu]</strong> Returns the simpler form of a CollationKey for the String source following
     * the rules of this Collator and stores the result into the user provided argument
     * key.  If key has a internal byte array of length that's too small for the result,
     * the internal byte array will be grown to the exact required size.
     *
     * <p>Note that collation keys are often less efficient than simply doing comparison.
     * For more details, see the ICU User Guide.
     *
     * @param source the text String to be transformed into a RawCollationKey
     * @return If key is null, a new instance of RawCollationKey will be
     *         created and returned, otherwise the user provided key will be
     *         returned.
     * @see #compare(String, String)
     * @see #getCollationKey
     * @see RawCollationKey
     * @hide unsupported on Android
     */
    public abstract RawCollationKey getRawCollationKey(String source,
                                                       RawCollationKey key);

    /**
     * <strong>[icu]</strong> Sets the variable top to the top of the specified reordering group.
     * The variable top determines the highest-sorting character
     * which is affected by the alternate handling behavior.
     * If that attribute is set to UCOL_NON_IGNORABLE, then the variable top has no effect.
     *
     * <p>The base class implementation throws an UnsupportedOperationException.
     * @param group one of Collator.ReorderCodes.SPACE, Collator.ReorderCodes.PUNCTUATION,
     *              Collator.ReorderCodes.SYMBOL, Collator.ReorderCodes.CURRENCY;
     *              or Collator.ReorderCodes.DEFAULT to restore the default max variable group
     * @return this
     * @see #getMaxVariable
     */
    public Collator setMaxVariable(int group) {
        throw new UnsupportedOperationException("Needs to be implemented by the subclass.");
    }

    /**
     * <strong>[icu]</strong> Returns the maximum reordering group whose characters are affected by
     * the alternate handling behavior.
     *
     * <p>The base class implementation returns Collator.ReorderCodes.PUNCTUATION.
     * @return the maximum variable reordering group.
     * @see #setMaxVariable
     */
    public int getMaxVariable() {
        return Collator.ReorderCodes.PUNCTUATION;
    }

    /**
     * <strong>[icu]</strong> Sets the variable top to the primary weight of the specified string.
     *
     * <p>Beginning with ICU 53, the variable top is pinned to
     * the top of one of the supported reordering groups,
     * and it must not be beyond the last of those groups.
     * See {@link #setMaxVariable(int)}.
     *
     * @param varTop one or more (if contraction) characters to which the
     *               variable top should be set
     * @return variable top primary weight
     * @exception IllegalArgumentException
     *                is thrown if varTop argument is not a valid variable top element. A variable top element is
     *                invalid when
     *                <ul>
     *                <li>it is a contraction that does not exist in the Collation order
     *                <li>the variable top is beyond
     *                    the last reordering group supported by setMaxVariable()
     *                <li>when the varTop argument is null or zero in length.
     *                </ul>
     * @see #getVariableTop
     * @see RuleBasedCollator#setAlternateHandlingShifted
     * @deprecated ICU 53 Call {@link #setMaxVariable(int)} instead.
     * @hide original deprecated declaration
     */
    @Deprecated
    public abstract int setVariableTop(String varTop);

    /**
     * <strong>[icu]</strong> Gets the variable top value of a Collator.
     *
     * @return the variable top primary weight
     * @see #getMaxVariable
     */
    public abstract int getVariableTop();

    /**
     * <strong>[icu]</strong> Sets the variable top to the specified primary weight.
     *
     * <p>Beginning with ICU 53, the variable top is pinned to
     * the top of one of the supported reordering groups,
     * and it must not be beyond the last of those groups.
     * See {@link #setMaxVariable(int)}.
     *
     * @param varTop primary weight, as returned by setVariableTop or getVariableTop
     * @see #getVariableTop
     * @see #setVariableTop(String)
     * @deprecated ICU 53 Call setMaxVariable() instead.
     * @hide original deprecated declaration
     */
    @Deprecated
    public abstract void setVariableTop(int varTop);

    /**
     * <strong>[icu]</strong> Returns the version of this collator object.
     * @return the version object associated with this collator
     */
    public abstract VersionInfo getVersion();

    /**
     * <strong>[icu]</strong> Returns the UCA version of this collator object.
     * @return the version object associated with this collator
     */
    public abstract VersionInfo getUCAVersion();

    /**
     * Retrieves the reordering codes for this collator.
     * These reordering codes are a combination of UScript codes and ReorderCodes.
     * @return a copy of the reordering codes for this collator;
     * if none are set then returns an empty array
     * @see #setReorderCodes
     * @see #getEquivalentReorderCodes
     * @see Collator.ReorderCodes
     * @see UScript
     */
    public int[] getReorderCodes()
    {
        throw new UnsupportedOperationException("Needs to be implemented by the subclass.");
    }

    /**
     * Retrieves all the reorder codes that are grouped with the given reorder code. Some reorder
     * codes are grouped and must reorder together.
     * Beginning with ICU 55, scripts only reorder together if they are primary-equal,
     * for example Hiragana and Katakana.
     *
     * @param reorderCode The reorder code to determine equivalence for.
     * @return the set of all reorder codes in the same group as the given reorder code.
     * @see #setReorderCodes
     * @see #getReorderCodes
     * @see Collator.ReorderCodes
     * @see UScript
     */
    public static int[] getEquivalentReorderCodes(int reorderCode) {
        CollationData baseData = CollationRoot.getData();
        return baseData.getEquivalentScripts(reorderCode);
    }


    // Freezable interface implementation -------------------------------------------------

    /**
     * Determines whether the object has been frozen or not.
     *
     * <p>An unfrozen Collator is mutable and not thread-safe.
     * A frozen Collator is immutable and thread-safe.
     */
    @Override
    public boolean isFrozen() {
        return false;
    }

    /**
     * Freezes the collator.
     * @return the collator itself.
     */
    @Override
    public Collator freeze() {
        throw new UnsupportedOperationException("Needs to be implemented by the subclass.");
    }

    /**
     * Provides for the clone operation. Any clone is initially unfrozen.
     */
    @Override
    public Collator cloneAsThawed() {
        throw new UnsupportedOperationException("Needs to be implemented by the subclass.");
    }

    /**
     * Empty default constructor to make javadocs happy
     */
    protected Collator()
    {
    }

    private static final boolean DEBUG = ICUDebug.enabled("collator");

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
     * <p>Note: This method will be implemented in ICU 3.0; ICU 2.8
     * contains a partial preview implementation.  The * <i>actual</i>
     * locale is returned correctly, but the <i>valid</i> locale is
     * not, in most cases.
     *
     * <p>The base class method always returns {@link ULocale#ROOT}.
     * Subclasses should override it if appropriate.
     *
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
    public ULocale getLocale(ULocale.Type type) {
        return ULocale.ROOT;
    }

    /**
     * Set information about the locales that were used to create this
     * object.  If the object was not constructed from locale data,
     * both arguments should be set to null.  Otherwise, neither
     * should be null.  The actual locale must be at the same level or
     * less specific than the valid locale.  This method is intended
     * for use by factories or other entities that create objects of
     * this class.
     *
     * <p>The base class method does nothing. Subclasses should override it if appropriate.
     *
     * @param valid the most specific locale containing any resource
     * data, or null
     * @param actual the locale containing data used to construct this
     * object, or null
     * @see android.icu.util.ULocale
     * @see android.icu.util.ULocale#VALID_LOCALE
     * @see android.icu.util.ULocale#ACTUAL_LOCALE
     */
    void setLocale(ULocale valid, ULocale actual) {}

    // -------- END ULocale boilerplate --------
}
