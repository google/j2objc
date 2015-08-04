/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.text;

import java.util.Comparator;
import java.util.Locale;

import libcore.icu.ICU;

/**
 * Performs locale-sensitive string comparison.
 * <p>
 * Following the <a href=http://www.unicode.org>Unicode Consortium</a>'s
 * specifications for the <a
 * href="http://www.unicode.org/unicode/reports/tr10/"> Unicode Collation
 * Algorithm (UCA)</a>, there are 4 different levels of strength used in
 * comparisons:
 * <ul>
 * <li>PRIMARY strength: Typically, this is used to denote differences between
 * base characters (for example, "a" &lt; "b"). It is the strongest difference.
 * For example, dictionaries are divided into different sections by base
 * character.
 * <li>SECONDARY strength: Accents in the characters are considered secondary
 * differences (for example, "as" &lt; "&agrave;s" &lt; "at"). Other differences
 * between letters can also be considered secondary differences, depending on
 * the language. A secondary difference is ignored when there is a primary
 * difference anywhere in the strings.
 * <li>TERTIARY strength: Upper and lower case differences in characters are
 * distinguished at tertiary strength (for example, "ao" &lt; "Ao" &lt;
 * "a&ograve;"). In addition, a variant of a letter differs from the base form
 * on the tertiary strength (such as "A" and "&#9398;"). Another example is the
 * difference between large and small Kana. A tertiary difference is ignored
 * when there is a primary or secondary difference anywhere in the strings.
 * <li>IDENTICAL strength: When all other strengths are equal, the IDENTICAL
 * strength is used as a tiebreaker. The Unicode code point values of the NFD
 * form of each string are compared, just in case there is no difference. For
 * example, Hebrew cantellation marks are only distinguished at this strength.
 * This strength should be used sparingly, as only code point value differences
 * between two strings are an extremely rare occurrence. Using this strength
 * substantially decreases the performance for both comparison and collation key
 * generation APIs. This strength also increases the size of the collation key.
 * </ul>
 * <p>
 * This {@code Collator} deals only with two decomposition modes, the canonical
 * decomposition mode and one that does not use any decomposition. The
 * compatibility decomposition mode
 * {@code java.text.Collator.FULL_DECOMPOSITION} is not supported here. If the
 * canonical decomposition mode is set, {@code Collator} handles un-normalized
 * text properly, producing the same results as if the text were normalized in
 * NFD. If canonical decomposition is turned off, it is the user's
 * responsibility to ensure that all text is already in the appropriate form
 * before performing a comparison or before getting a {@link CollationKey}.
 * <p>
 * <em>Examples:</em>
 * <blockquote>
 *
 * <pre>
 * // Get the Collator for US English and set its strength to PRIMARY
 * Collator usCollator = Collator.getInstance(Locale.US);
 * usCollator.setStrength(Collator.PRIMARY);
 * if (usCollator.compare(&quot;abc&quot;, &quot;ABC&quot;) == 0) {
 *     System.out.println(&quot;Strings are equivalent&quot;);
 * }
 * </pre>
 *
 * </blockquote>
 * <p>
 * The following example shows how to compare two strings using the collator for
 * the default locale.
 * <blockquote>
 *
 * <pre>
 * // Compare two strings in the default locale
 * Collator myCollator = Collator.getInstance();
 * myCollator.setDecomposition(Collator.NO_DECOMPOSITION);
 * if (myCollator.compare(&quot;\u00e0\u0325&quot;, &quot;a\u0325\u0300&quot;) != 0) {
 *     System.out.println(&quot;\u00e0\u0325 is not equal to a\u0325\u0300 without decomposition&quot;);
 *     myCollator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
 *     if (myCollator.compare(&quot;\u00e0\u0325&quot;, &quot;a\u0325\u0300&quot;) != 0) {
 *         System.out.println(&quot;Error: \u00e0\u0325 should be equal to a\u0325\u0300 with decomposition&quot;);
 *     } else {
 *         System.out.println(&quot;\u00e0\u0325 is equal to a\u0325\u0300 with decomposition&quot;);
 *     }
 * } else {
 *     System.out.println(&quot;Error: \u00e0\u0325 should be not equal to a\u0325\u0300 without decomposition&quot;);
 * }
 * </pre>
 *
 * </blockquote>
 *
 * @see CollationKey
 */
public abstract class Collator implements Comparator<Object>, Cloneable {
    /**
     * Constant used to specify the decomposition rule.
     */
    public static final int NO_DECOMPOSITION = 0;

    /**
     * Constant used to specify the decomposition rule.
     */
    public static final int CANONICAL_DECOMPOSITION = 1;

    /**
     * Constant used to specify the decomposition rule. This value for
     * decomposition is not supported.
     */
    public static final int FULL_DECOMPOSITION = 2;

    /**
     * Constant used to specify the collation strength.
     */
    public static final int PRIMARY = 0;

    /**
     * Constant used to specify the collation strength.
     */
    public static final int SECONDARY = 1;

    /**
     * Constant used to specify the collation strength.
     */
    public static final int TERTIARY = 2;

    /**
     * Constant used to specify the collation strength.
     */
    public static final int IDENTICAL = 3;


    /**
     * Compares two objects to determine their relative order. The objects must
     * be strings.
     *
     * @param object1
     *            the first string to compare.
     * @param object2
     *            the second string to compare.
     * @return a negative value if {@code object1} is less than {@code object2},
     *         0 if they are equal, and a positive value if {@code object1} is
     *         greater than {@code object2}.
     * @throws ClassCastException
     *         if {@code object1} or {@code object2} is not a {@code String}.
     */
    public int compare(Object object1, Object object2) {
        return compare((String) object1, (String) object2);
    }

    /**
     * Compares two strings to determine their relative order.
     *
     * @param string1
     *            the first string to compare.
     * @param string2
     *            the second string to compare.
     * @return a negative value if {@code string1} is less than {@code string2},
     *         0 if they are equal and a positive value if {@code string1} is
     *         greater than {@code string2}.
     */
    public abstract int compare(String string1, String string2);

    /**
     * Compares two strings using the collation rules to determine if they are
     * equal.
     *
     * @param string1
     *            the first string to compare.
     * @param string2
     *            the second string to compare.
     * @return {@code true} if {@code string1} and {@code string2} are equal
     *         using the collation rules, false otherwise.
     */
    public boolean equals(String string1, String string2) {
        return compare(string1, string2) == 0;
    }

    /**
     * Returns an array of locales for which custom {@code Collator} instances
     * are available.
     * <p>Note that Android does not support user-supplied locale service providers.
     */
    public static Locale[] getAvailableLocales() {
        return ICU.getAvailableLocales();
    }

    /**
     * Returns a {@link CollationKey} for the specified string for this collator
     * with the current decomposition rule and strength value.
     *
     * @param string
     *            the source string that is converted into a collation key.
     * @return the collation key for {@code string}.
     */
    public abstract CollationKey getCollationKey(String string);

    /**
     * Returns the decomposition rule for this collator.
     *
     * @return the decomposition rule, either {@code NO_DECOMPOSITION} or
     *         {@code CANONICAL_DECOMPOSITION}. {@code FULL_DECOMPOSITION} is
     *         not supported.
     */
    public abstract int getDecomposition();

    /**
     * Returns a {@code Collator} instance which is appropriate for the user's default
     * {@code Locale}.
     * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
     */
    public static Collator getInstance() {
        return getInstance(Locale.getDefault());
    }

    /**
     * Returns a {@code Collator} instance which is appropriate for {@code locale}.
     */
    public static Collator getInstance(Locale locale) {
        if (locale == null) {
            throw new NullPointerException("locale == null");
        }
        return new IOSCollator(locale);
    }

    /**
     * Returns the strength value for this collator.
     *
     * @return the strength value, either PRIMARY, SECONDARY, TERTIARY or
     *         IDENTICAL.
     */
    public abstract int getStrength();

    /**
     * Sets the decomposition rule for this collator.
     *
     * @param value
     *            the decomposition rule, either {@code NO_DECOMPOSITION} or
     *            {@code CANONICAL_DECOMPOSITION}. {@code FULL_DECOMPOSITION}
     *            is not supported.
     * @throws IllegalArgumentException
     *            if the provided decomposition rule is not valid. This includes
     *            {@code FULL_DECOMPOSITION}.
     */
    public abstract void setDecomposition(int value);

    /**
     * Sets the strength value for this collator.
     *
     * @param value
     *            the strength value, either PRIMARY, SECONDARY, TERTIARY, or
     *            IDENTICAL.
     * @throws IllegalArgumentException
     *            if the provided strength value is not valid.
     */
    public abstract void setStrength(int value);

    @Override
    public Object clone() {
      try {
          return (IOSCollator) super.clone();
      } catch (CloneNotSupportedException e) {
          throw new AssertionError(e);
      }
    }
}
