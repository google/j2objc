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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.ChoiceFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.icu.impl.ICUConfig;
import android.icu.impl.PatternProps;
import android.icu.impl.Utility;
import android.icu.lang.UCharacter;
import android.icu.math.BigDecimal;
import android.icu.math.MathContext;
import android.icu.text.PluralRules.FixedDecimal;
import android.icu.util.Currency;
import android.icu.util.Currency.CurrencyUsage;
import android.icu.util.CurrencyAmount;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;

/**
 * <strong>[icu enhancement]</strong> ICU's replacement for {@link java.text.DecimalFormat}.&nbsp;Methods, fields, and other functionality specific to ICU are labeled '<strong>[icu]</strong>'.
 *
 * <code>DecimalFormat</code> is a concrete subclass of {@link NumberFormat} that formats
 * decimal numbers. It has a variety of features designed to make it possible to parse and
 * format numbers in any locale, including support for Western, Arabic, or Indic digits.
 * It also supports different flavors of numbers, including integers ("123"), fixed-point
 * numbers ("123.4"), scientific notation ("1.23E4"), percentages ("12%"), and currency
 * amounts ("$123.00", "USD123.00", "123.00 US dollars").  All of these flavors can be
 * easily localized.
 *
 * <p>To obtain a {@link NumberFormat} for a specific locale (including the default
 * locale) call one of <code>NumberFormat</code>'s factory methods such as {@link
 * NumberFormat#getInstance}. Do not call the <code>DecimalFormat</code> constructors
 * directly, unless you know what you are doing, since the {@link NumberFormat} factory
 * methods may return subclasses other than <code>DecimalFormat</code>. If you need to
 * customize the format object, do something like this:
 *
 * <blockquote><pre>
 * NumberFormat f = NumberFormat.getInstance(loc);
 * if (f instanceof DecimalFormat) {
 *     ((DecimalFormat) f).setDecimalSeparatorAlwaysShown(true);
 * }</pre></blockquote>
 *
 * <p><strong>Example Usage</strong>
 *
 * Print out a number using the localized number, currency, and percent
 * format for each locale.
 *
 * <blockquote><pre>
 * Locale[] locales = NumberFormat.getAvailableLocales();
 * double myNumber = -1234.56;
 * NumberFormat format;
 * for (int j=0; j&lt;3; ++j) {
 *     System.out.println("FORMAT");
 *     for (int i = 0; i &lt; locales.length; ++i) {
 *         if (locales[i].getCountry().length() == 0) {
 *            // Skip language-only locales
 *            continue;
 *         }
 *         System.out.print(locales[i].getDisplayName());
 *         switch (j) {
 *         case 0:
 *             format = NumberFormat.getInstance(locales[i]); break;
 *         case 1:
 *             format = NumberFormat.getCurrencyInstance(locales[i]); break;
 *         default:
 *             format = NumberFormat.getPercentInstance(locales[i]); break;
 *         }
 *         try {
 *             // Assume format is a DecimalFormat
 *             System.out.print(": " + ((DecimalFormat) format).toPattern()
 *                              + " -&gt; " + form.format(myNumber));
 *         } catch (Exception e) {}
 *         try {
 *             System.out.println(" -&gt; " + format.parse(form.format(myNumber)));
 *         } catch (ParseException e) {}
 *     }
 * }</pre></blockquote>
 *
 * <p>Another example use getInstance(style).<br>
 * Print out a number using the localized number, currency, percent,
 * scientific, integer, iso currency, and plural currency format for each locale.
 *
 * <blockquote><pre>
 * ULocale locale = new ULocale("en_US");
 * double myNumber = 1234.56;
 * for (int j=NumberFormat.NUMBERSTYLE; j&lt;=NumberFormat.PLURALCURRENCYSTYLE; ++j) {
 *     NumberFormat format = NumberFormat.getInstance(locale, j);
 *     try {
 *         // Assume format is a DecimalFormat
 *         System.out.print(": " + ((DecimalFormat) format).toPattern()
 *                          + " -&gt; " + form.format(myNumber));
 *     } catch (Exception e) {}
 *     try {
 *         System.out.println(" -&gt; " + format.parse(form.format(myNumber)));
 *     } catch (ParseException e) {}
 * }</pre></blockquote>
 *
 * <h3>Patterns</h3>
 *
 * <p>A <code>DecimalFormat</code> consists of a <em>pattern</em> and a set of
 * <em>symbols</em>.  The pattern may be set directly using {@link #applyPattern}, or
 * indirectly using other API methods which manipulate aspects of the pattern, such as the
 * minimum number of integer digits.  The symbols are stored in a {@link
 * DecimalFormatSymbols} object.  When using the {@link NumberFormat} factory methods, the
 * pattern and symbols are read from ICU's locale data.
 *
 * <h4>Special Pattern Characters</h4>
 *
 * <p>Many characters in a pattern are taken literally; they are matched during parsing
 * and output unchanged during formatting.  Special characters, on the other hand, stand
 * for other characters, strings, or classes of characters.  For example, the '#'
 * character is replaced by a localized digit.  Often the replacement character is the
 * same as the pattern character; in the U.S. locale, the ',' grouping character is
 * replaced by ','.  However, the replacement is still happening, and if the symbols are
 * modified, the grouping character changes.  Some special characters affect the behavior
 * of the formatter by their presence; for example, if the percent character is seen, then
 * the value is multiplied by 100 before being displayed.
 *
 * <p>To insert a special character in a pattern as a literal, that is, without any
 * special meaning, the character must be quoted.  There are some exceptions to this which
 * are noted below.
 *
 * <p>The characters listed here are used in non-localized patterns.  Localized patterns
 * use the corresponding characters taken from this formatter's {@link
 * DecimalFormatSymbols} object instead, and these characters lose their special status.
 * Two exceptions are the currency sign and quote, which are not localized.
 *
 * <blockquote>
 * <table border=0 cellspacing=3 cellpadding=0 summary="Chart showing symbol,
 *  location, localized, and meaning.">
 *   <tr style="background-color: #ccccff">
 *     <th align=left>Symbol
 *     <th align=left>Location
 *     <th align=left>Localized?
 *     <th align=left>Meaning
 *   <tr style="vertical-align: top;">
 *     <td><code>0</code>
 *     <td>Number
 *     <td>Yes
 *     <td>Digit
 *   <tr style="vertical-align: top; background-color: #eeeeff;">
 *     <td><code>1-9</code>
 *     <td>Number
 *     <td>Yes
 *     <td>'1' through '9' indicate rounding.
 *   <tr style="vertical-align: top;">
 *     <td><code>@</code>
 *     <td>Number
 *     <td>No
 *     <td>Significant digit
 *   <tr style="vertical-align: top; background-color: #eeeeff;">
 *     <td><code>#</code>
 *     <td>Number
 *     <td>Yes
 *     <td>Digit, zero shows as absent
 *   <tr style="vertical-align: top;">
 *     <td><code>.</code>
 *     <td>Number
 *     <td>Yes
 *     <td>Decimal separator or monetary decimal separator
 *   <tr style="vertical-align: top; background-color: #eeeeff;">
 *     <td><code>-</code>
 *     <td>Number
 *     <td>Yes
 *     <td>Minus sign
 *   <tr style="vertical-align: top;">
 *     <td><code>,</code>
 *     <td>Number
 *     <td>Yes
 *     <td>Grouping separator
 *   <tr style="vertical-align: top; background-color: #eeeeff;">
 *     <td><code>E</code>
 *     <td>Number
 *     <td>Yes
 *     <td>Separates mantissa and exponent in scientific notation.
 *         <em>Need not be quoted in prefix or suffix.</em>
 *   <tr style="vertical-align: top;">
 *     <td><code>+</code>
 *     <td>Exponent
 *     <td>Yes
 *     <td>Prefix positive exponents with localized plus sign.
 *         <em>Need not be quoted in prefix or suffix.</em>
 *   <tr style="vertical-align: top; background-color: #eeeeff;">
 *     <td><code>;</code>
 *     <td>Subpattern boundary
 *     <td>Yes
 *     <td>Separates positive and negative subpatterns
 *   <tr style="vertical-align: top;">
 *     <td><code>%</code>
 *     <td>Prefix or suffix
 *     <td>Yes
 *     <td>Multiply by 100 and show as percentage
 *   <tr style="vertical-align: top; background-color: #eeeeff;">
 *     <td><code>&#92;u2030</code>
 *     <td>Prefix or suffix
 *     <td>Yes
 *     <td>Multiply by 1000 and show as per mille
 *   <tr style="vertical-align: top;">
 *     <td><code>&#164;</code> (<code>&#92;u00A4</code>)
 *     <td>Prefix or suffix
 *     <td>No
 *     <td>Currency sign, replaced by currency symbol.  If
 *         doubled, replaced by international currency symbol.
 *         If tripled, replaced by currency plural names, for example,
 *         "US dollar" or "US dollars" for America.
 *         If present in a pattern, the monetary decimal separator
 *         is used instead of the decimal separator.
 *   <tr style="vertical-align: top; background-color: #eeeeff;">
 *     <td><code>'</code>
 *     <td>Prefix or suffix
 *     <td>No
 *     <td>Used to quote special characters in a prefix or suffix,
 *         for example, <code>"'#'#"</code> formats 123 to
 *         <code>"#123"</code>.  To create a single quote
 *         itself, use two in a row: <code>"# o''clock"</code>.
 *   <tr style="vertical-align: top;">
 *     <td><code>*</code>
 *     <td>Prefix or suffix boundary
 *     <td>Yes
 *     <td>Pad escape, precedes pad character
 * </table>
 * </blockquote>
 *
 * <p>A <code>DecimalFormat</code> pattern contains a postive and negative subpattern, for
 * example, "#,##0.00;(#,##0.00)".  Each subpattern has a prefix, a numeric part, and a
 * suffix.  If there is no explicit negative subpattern, the negative subpattern is the
 * localized minus sign prefixed to the positive subpattern. That is, "0.00" alone is
 * equivalent to "0.00;-0.00".  If there is an explicit negative subpattern, it serves
 * only to specify the negative prefix and suffix; the number of digits, minimal digits,
 * and other characteristics are ignored in the negative subpattern. That means that
 * "#,##0.0#;(#)" has precisely the same result as "#,##0.0#;(#,##0.0#)".
 *
 * <p>The prefixes, suffixes, and various symbols used for infinity, digits, thousands
 * separators, decimal separators, etc. may be set to arbitrary values, and they will
 * appear properly during formatting.  However, care must be taken that the symbols and
 * strings do not conflict, or parsing will be unreliable.  For example, either the
 * positive and negative prefixes or the suffixes must be distinct for {@link #parse} to
 * be able to distinguish positive from negative values.  Another example is that the
 * decimal separator and thousands separator should be distinct characters, or parsing
 * will be impossible.
 *
 * <p>The <em>grouping separator</em> is a character that separates clusters of integer
 * digits to make large numbers more legible.  It commonly used for thousands, but in some
 * locales it separates ten-thousands.  The <em>grouping size</em> is the number of digits
 * between the grouping separators, such as 3 for "100,000,000" or 4 for "1 0000
 * 0000". There are actually two different grouping sizes: One used for the least
 * significant integer digits, the <em>primary grouping size</em>, and one used for all
 * others, the <em>secondary grouping size</em>.  In most locales these are the same, but
 * sometimes they are different. For example, if the primary grouping interval is 3, and
 * the secondary is 2, then this corresponds to the pattern "#,##,##0", and the number
 * 123456789 is formatted as "12,34,56,789".  If a pattern contains multiple grouping
 * separators, the interval between the last one and the end of the integer defines the
 * primary grouping size, and the interval between the last two defines the secondary
 * grouping size. All others are ignored, so "#,##,###,####" == "###,###,####" ==
 * "##,#,###,####".
 *
 * <p>Illegal patterns, such as "#.#.#" or "#.###,###", will cause
 * <code>DecimalFormat</code> to throw an {@link IllegalArgumentException} with a message
 * that describes the problem.
 *
 * <h4>Pattern BNF</h4>
 *
 * <pre>
 * pattern    := subpattern (';' subpattern)?
 * subpattern := prefix? number exponent? suffix?
 * number     := (integer ('.' fraction)?) | sigDigits
 * prefix     := '&#92;u0000'..'&#92;uFFFD' - specialCharacters
 * suffix     := '&#92;u0000'..'&#92;uFFFD' - specialCharacters
 * integer    := '#'* '0'* '0'
 * fraction   := '0'* '#'*
 * sigDigits  := '#'* '@' '@'* '#'*
 * exponent   := 'E' '+'? '0'* '0'
 * padSpec    := '*' padChar
 * padChar    := '&#92;u0000'..'&#92;uFFFD' - quote
 * &#32;
 * Notation:
 *   X*       0 or more instances of X
 *   X?       0 or 1 instances of X
 *   X|Y      either X or Y
 *   C..D     any character from C up to D, inclusive
 *   S-T      characters in S, except those in T
 * </pre>
 * The first subpattern is for positive numbers. The second (optional)
 * subpattern is for negative numbers.
 *
 * <p>Not indicated in the BNF syntax above:
 *
 * <ul>
 *
 * <li>The grouping separator ',' can occur inside the integer and sigDigits
 * elements, between any two pattern characters of that element, as long as the integer or
 * sigDigits element is not followed by the exponent element.
 *
 * <li>Two grouping intervals are recognized: That between the decimal point and the first
 * grouping symbol, and that between the first and second grouping symbols. These
 * intervals are identical in most locales, but in some locales they differ. For example,
 * the pattern &quot;#,##,###&quot; formats the number 123456789 as
 * &quot;12,34,56,789&quot;.
 *
 * <li>The pad specifier <code>padSpec</code> may appear before the prefix, after the
 * prefix, before the suffix, after the suffix, or not at all.
 *
 * <li>In place of '0', the digits '1' through '9' may be used to indicate a rounding
 * increment.
 *
 * </ul>
 *
 * <h4>Parsing</h4>
 *
 * <p><code>DecimalFormat</code> parses all Unicode characters that represent decimal
 * digits, as defined by {@link UCharacter#digit}.  In addition,
 * <code>DecimalFormat</code> also recognizes as digits the ten consecutive characters
 * starting with the localized zero digit defined in the {@link DecimalFormatSymbols}
 * object.  During formatting, the {@link DecimalFormatSymbols}-based digits are output.
 *
 * <p>During parsing, grouping separators are ignored.
 *
 * <p>For currency parsing, the formatter is able to parse every currency style formats no
 * matter which style the formatter is constructed with.  For example, a formatter
 * instance gotten from NumberFormat.getInstance(ULocale, NumberFormat.CURRENCYSTYLE) can
 * parse formats such as "USD1.00" and "3.00 US dollars".
 *
 * <p>If {@link #parse(String, ParsePosition)} fails to parse a string, it returns
 * <code>null</code> and leaves the parse position unchanged.  The convenience method
 * {@link #parse(String)} indicates parse failure by throwing a {@link
 * java.text.ParseException}.
 *
 * <p>Parsing an extremely large or small absolute value (such as 1.0E10000 or 1.0E-10000)
 * requires huge memory allocation for representing the parsed number. Such input may expose
 * a risk of DoS attacks. To prevent huge memory allocation triggered by such inputs,
 * <code>DecimalFormat</code> internally limits of maximum decimal digits to be 1000. Thus,
 * an input string resulting more than 1000 digits in plain decimal representation (non-exponent)
 * will be treated as either overflow (positive/negative infinite) or underflow (+0.0/-0.0).
 *
 * <h4>Formatting</h4>
 *
 * <p>Formatting is guided by several parameters, all of which can be specified either
 * using a pattern or using the API.  The following description applies to formats that do
 * not use <a href="#sci">scientific notation</a> or <a href="#sigdig">significant
 * digits</a>.
 *
 * <ul><li>If the number of actual integer digits exceeds the <em>maximum integer
 * digits</em>, then only the least significant digits are shown.  For example, 1997 is
 * formatted as "97" if the maximum integer digits is set to 2.
 *
 * <li>If the number of actual integer digits is less than the <em>minimum integer
 * digits</em>, then leading zeros are added.  For example, 1997 is formatted as "01997"
 * if the minimum integer digits is set to 5.
 *
 * <li>If the number of actual fraction digits exceeds the <em>maximum fraction
 * digits</em>, then half-even rounding it performed to the maximum fraction digits.  For
 * example, 0.125 is formatted as "0.12" if the maximum fraction digits is 2.  This
 * behavior can be changed by specifying a rounding increment and a rounding mode.
 *
 * <li>If the number of actual fraction digits is less than the <em>minimum fraction
 * digits</em>, then trailing zeros are added.  For example, 0.125 is formatted as
 * "0.1250" if the mimimum fraction digits is set to 4.
 *
 * <li>Trailing fractional zeros are not displayed if they occur <em>j</em> positions
 * after the decimal, where <em>j</em> is less than the maximum fraction digits. For
 * example, 0.10004 is formatted as "0.1" if the maximum fraction digits is four or less.
 * </ul>
 *
 * <p><strong>Special Values</strong>
 *
 * <p><code>NaN</code> is represented as a single character, typically
 * <code>&#92;uFFFD</code>.  This character is determined by the {@link
 * DecimalFormatSymbols} object.  This is the only value for which the prefixes and
 * suffixes are not used.
 *
 * <p>Infinity is represented as a single character, typically <code>&#92;u221E</code>,
 * with the positive or negative prefixes and suffixes applied.  The infinity character is
 * determined by the {@link DecimalFormatSymbols} object.
 *
 * <h4><a name="sci">Scientific Notation</a></h4>
 *
 * <p>Numbers in scientific notation are expressed as the product of a mantissa and a
 * power of ten, for example, 1234 can be expressed as 1.234 x 10<sup>3</sup>. The
 * mantissa is typically in the half-open interval [1.0, 10.0) or sometimes [0.0, 1.0),
 * but it need not be.  <code>DecimalFormat</code> supports arbitrary mantissas.
 * <code>DecimalFormat</code> can be instructed to use scientific notation through the API
 * or through the pattern.  In a pattern, the exponent character immediately followed by
 * one or more digit characters indicates scientific notation.  Example: "0.###E0" formats
 * the number 1234 as "1.234E3".
 *
 * <ul>
 *
 * <li>The number of digit characters after the exponent character gives the minimum
 * exponent digit count.  There is no maximum.  Negative exponents are formatted using the
 * localized minus sign, <em>not</em> the prefix and suffix from the pattern.  This allows
 * patterns such as "0.###E0 m/s".  To prefix positive exponents with a localized plus
 * sign, specify '+' between the exponent and the digits: "0.###E+0" will produce formats
 * "1E+1", "1E+0", "1E-1", etc.  (In localized patterns, use the localized plus sign
 * rather than '+'.)
 *
 * <li>The minimum number of integer digits is achieved by adjusting the exponent.
 * Example: 0.00123 formatted with "00.###E0" yields "12.3E-4".  This only happens if
 * there is no maximum number of integer digits.  If there is a maximum, then the minimum
 * number of integer digits is fixed at one.
 *
 * <li>The maximum number of integer digits, if present, specifies the exponent grouping.
 * The most common use of this is to generate <em>engineering notation</em>, in which the
 * exponent is a multiple of three, e.g., "##0.###E0".  The number 12345 is formatted
 * using "##0.####E0" as "12.345E3".
 *
 * <li>When using scientific notation, the formatter controls the digit counts using
 * significant digits logic.  The maximum number of significant digits limits the total
 * number of integer and fraction digits that will be shown in the mantissa; it does not
 * affect parsing.  For example, 12345 formatted with "##0.##E0" is "12.3E3".  See the
 * section on significant digits for more details.
 *
 * <li>The number of significant digits shown is determined as follows: If
 * areSignificantDigitsUsed() returns false, then the minimum number of significant digits
 * shown is one, and the maximum number of significant digits shown is the sum of the
 * <em>minimum integer</em> and <em>maximum fraction</em> digits, and is unaffected by the
 * maximum integer digits.  If this sum is zero, then all significant digits are shown.
 * If areSignificantDigitsUsed() returns true, then the significant digit counts are
 * specified by getMinimumSignificantDigits() and getMaximumSignificantDigits().  In this
 * case, the number of integer digits is fixed at one, and there is no exponent grouping.
 *
 * <li>Exponential patterns may not contain grouping separators.
 *
 * </ul>
 *
 * <h4><a name="sigdig">Significant Digits</a></h4>
 *
 * <code>DecimalFormat</code> has two ways of controlling how many digits are shows: (a)
 * significant digits counts, or (b) integer and fraction digit counts.  Integer and
 * fraction digit counts are described above.  When a formatter is using significant
 * digits counts, the number of integer and fraction digits is not specified directly, and
 * the formatter settings for these counts are ignored.  Instead, the formatter uses
 * however many integer and fraction digits are required to display the specified number
 * of significant digits.  Examples:
 *
 * <blockquote>
 * <table border=0 cellspacing=3 cellpadding=0>
 *   <tr style="background-color: #ccccff">
 *     <th align=left>Pattern
 *     <th align=left>Minimum significant digits
 *     <th align=left>Maximum significant digits
 *     <th align=left>Number
 *     <th align=left>Output of format()
 *   <tr style="vertical-align: top;">
 *     <td><code>@@@</code>
 *     <td>3
 *     <td>3
 *     <td>12345
 *     <td><code>12300</code>
 *   <tr style="vertical-align: top; background-color: #eeeeff;">
 *     <td><code>@@@</code>
 *     <td>3
 *     <td>3
 *     <td>0.12345
 *     <td><code>0.123</code>
 *   <tr style="vertical-align: top;">
 *     <td><code>@@##</code>
 *     <td>2
 *     <td>4
 *     <td>3.14159
 *     <td><code>3.142</code>
 *   <tr style="vertical-align: top; background-color: #eeeeff;">
 *     <td><code>@@##</code>
 *     <td>2
 *     <td>4
 *     <td>1.23004
 *     <td><code>1.23</code>
 * </table>
 * </blockquote>
 *
 * <ul>
 *
 * <li>Significant digit counts may be expressed using patterns that specify a minimum and
 * maximum number of significant digits.  These are indicated by the <code>'@'</code> and
 * <code>'#'</code> characters.  The minimum number of significant digits is the number of
 * <code>'@'</code> characters.  The maximum number of significant digits is the number of
 * <code>'@'</code> characters plus the number of <code>'#'</code> characters following on
 * the right.  For example, the pattern <code>"@@@"</code> indicates exactly 3 significant
 * digits.  The pattern <code>"@##"</code> indicates from 1 to 3 significant digits.
 * Trailing zero digits to the right of the decimal separator are suppressed after the
 * minimum number of significant digits have been shown.  For example, the pattern
 * <code>"@##"</code> formats the number 0.1203 as <code>"0.12"</code>.
 *
 * <li>If a pattern uses significant digits, it may not contain a decimal separator, nor
 * the <code>'0'</code> pattern character.  Patterns such as <code>"@00"</code> or
 * <code>"@.###"</code> are disallowed.
 *
 * <li>Any number of <code>'#'</code> characters may be prepended to the left of the
 * leftmost <code>'@'</code> character.  These have no effect on the minimum and maximum
 * significant digits counts, but may be used to position grouping separators.  For
 * example, <code>"#,#@#"</code> indicates a minimum of one significant digits, a maximum
 * of two significant digits, and a grouping size of three.
 *
 * <li>In order to enable significant digits formatting, use a pattern containing the
 * <code>'@'</code> pattern character.  Alternatively, call {@link
 * #setSignificantDigitsUsed setSignificantDigitsUsed(true)}.
 *
 * <li>In order to disable significant digits formatting, use a pattern that does not
 * contain the <code>'@'</code> pattern character. Alternatively, call {@link
 * #setSignificantDigitsUsed setSignificantDigitsUsed(false)}.
 *
 * <li>The number of significant digits has no effect on parsing.
 *
 * <li>Significant digits may be used together with exponential notation. Such patterns
 * are equivalent to a normal exponential pattern with a minimum and maximum integer digit
 * count of one, a minimum fraction digit count of <code>getMinimumSignificantDigits() -
 * 1</code>, and a maximum fraction digit count of <code>getMaximumSignificantDigits() -
 * 1</code>. For example, the pattern <code>"@@###E0"</code> is equivalent to
 * <code>"0.0###E0"</code>.
 *
 * <li>If signficant digits are in use, then the integer and fraction digit counts, as set
 * via the API, are ignored.  If significant digits are not in use, then the signficant
 * digit counts, as set via the API, are ignored.
 *
 * </ul>
 *
 * <h4>Padding</h4>
 *
 * <p><code>DecimalFormat</code> supports padding the result of {@link #format} to a
 * specific width.  Padding may be specified either through the API or through the pattern
 * syntax.  In a pattern the pad escape character, followed by a single pad character,
 * causes padding to be parsed and formatted.  The pad escape character is '*' in
 * unlocalized patterns, and can be localized using {@link
 * DecimalFormatSymbols#setPadEscape}.  For example, <code>"$*x#,##0.00"</code> formats
 * 123 to <code>"$xx123.00"</code>, and 1234 to <code>"$1,234.00"</code>.
 *
 * <ul>
 *
 * <li>When padding is in effect, the width of the positive subpattern, including prefix
 * and suffix, determines the format width.  For example, in the pattern <code>"* #0
 * o''clock"</code>, the format width is 10.
 *
 * <li>The width is counted in 16-bit code units (Java <code>char</code>s).
 *
 * <li>Some parameters which usually do not matter have meaning when padding is used,
 * because the pattern width is significant with padding.  In the pattern "*
 * ##,##,#,##0.##", the format width is 14.  The initial characters "##,##," do not affect
 * the grouping size or maximum integer digits, but they do affect the format width.
 *
 * <li>Padding may be inserted at one of four locations: before the prefix, after the
 * prefix, before the suffix, or after the suffix.  If padding is specified in any other
 * location, {@link #applyPattern} throws an {@link IllegalArgumentException}.  If there
 * is no prefix, before the prefix and after the prefix are equivalent, likewise for the
 * suffix.
 *
 * <li>When specified in a pattern, the 16-bit <code>char</code> immediately following the
 * pad escape is the pad character. This may be any character, including a special pattern
 * character. That is, the pad escape <em>escapes</em> the following character. If there
 * is no character after the pad escape, then the pattern is illegal.
 *
 * </ul>
 *
 * <p>
 * <strong>Rounding</strong>
 *
 * <p><code>DecimalFormat</code> supports rounding to a specific increment.  For example,
 * 1230 rounded to the nearest 50 is 1250.  1.234 rounded to the nearest 0.65 is 1.3.  The
 * rounding increment may be specified through the API or in a pattern.  To specify a
 * rounding increment in a pattern, include the increment in the pattern itself.  "#,#50"
 * specifies a rounding increment of 50.  "#,##0.05" specifies a rounding increment of
 * 0.05.
 *
 * <ul>
 *
 * <li>Rounding only affects the string produced by formatting.  It does not affect
 * parsing or change any numerical values.
 *
 * <li>A <em>rounding mode</em> determines how values are rounded; see the {@link
 * android.icu.math.BigDecimal} documentation for a description of the modes.  Rounding
 * increments specified in patterns use the default mode, {@link
 * android.icu.math.BigDecimal#ROUND_HALF_EVEN}.
 *
 * <li>Some locales use rounding in their currency formats to reflect the smallest
 * currency denomination.
 *
 * <li>In a pattern, digits '1' through '9' specify rounding, but otherwise behave
 * identically to digit '0'.
 *
 * </ul>
 *
 * <h4>Synchronization</h4>
 *
 * <p><code>DecimalFormat</code> objects are not synchronized.  Multiple threads should
 * not access one formatter concurrently.
 *
 * @see          java.text.Format
 * @see          NumberFormat
 * @author       Mark Davis
 * @author       Alan Liu
 */
public class DecimalFormat extends NumberFormat {

    /**
     * Creates a DecimalFormat using the default pattern and symbols for the default
     * <code>FORMAT</code> locale. This is a convenient way to obtain a DecimalFormat when
     * internationalization is not the main concern.
     *
     * <p>To obtain standard formats for a given locale, use the factory methods on
     * NumberFormat such as getNumberInstance.  These factories will return the most
     * appropriate sub-class of NumberFormat for a given locale.
     *
     * @see NumberFormat#getInstance
     * @see NumberFormat#getNumberInstance
     * @see NumberFormat#getCurrencyInstance
     * @see NumberFormat#getPercentInstance
     * @see Category#FORMAT
     */
    public DecimalFormat() {
        ULocale def = ULocale.getDefault(Category.FORMAT);
        String pattern = getPattern(def, 0);
        // Always applyPattern after the symbols are set
        this.symbols = new DecimalFormatSymbols(def);
        setCurrency(Currency.getInstance(def));
        applyPatternWithoutExpandAffix(pattern, false);
        if (currencySignCount == CURRENCY_SIGN_COUNT_IN_PLURAL_FORMAT) {
            currencyPluralInfo = new CurrencyPluralInfo(def);
            // the exact pattern is not known until the plural count is known.
            // so, no need to expand affix now.
        } else {
            expandAffixAdjustWidth(null);
        }
    }

    /**
     * Creates a DecimalFormat from the given pattern and the symbols for the default
     * <code>FORMAT</code> locale. This is a convenient way to obtain a DecimalFormat when
     * internationalization is not the main concern.
     *
     * <p>To obtain standard formats for a given locale, use the factory methods on
     * NumberFormat such as getNumberInstance.  These factories will return the most
     * appropriate sub-class of NumberFormat for a given locale.
     *
     * @param pattern A non-localized pattern string.
     * @throws IllegalArgumentException if the given pattern is invalid.
     * @see NumberFormat#getInstance
     * @see NumberFormat#getNumberInstance
     * @see NumberFormat#getCurrencyInstance
     * @see NumberFormat#getPercentInstance
     * @see Category#FORMAT
     */
    public DecimalFormat(String pattern) {
        // Always applyPattern after the symbols are set
        ULocale def = ULocale.getDefault(Category.FORMAT);
        this.symbols = new DecimalFormatSymbols(def);
        setCurrency(Currency.getInstance(def));
        applyPatternWithoutExpandAffix(pattern, false);
        if (currencySignCount == CURRENCY_SIGN_COUNT_IN_PLURAL_FORMAT) {
            currencyPluralInfo = new CurrencyPluralInfo(def);
        } else {
            expandAffixAdjustWidth(null);
        }
    }

    /**
     * Creates a DecimalFormat from the given pattern and symbols. Use this constructor
     * when you need to completely customize the behavior of the format.
     *
     * <p>To obtain standard formats for a given locale, use the factory methods on
     * NumberFormat such as getInstance or getCurrencyInstance. If you need only minor
     * adjustments to a standard format, you can modify the format returned by a
     * NumberFormat factory method.
     *
     * @param pattern a non-localized pattern string
     * @param symbols the set of symbols to be used
     * @exception IllegalArgumentException if the given pattern is invalid
     * @see NumberFormat#getInstance
     * @see NumberFormat#getNumberInstance
     * @see NumberFormat#getCurrencyInstance
     * @see NumberFormat#getPercentInstance
     * @see DecimalFormatSymbols
     */
    public DecimalFormat(String pattern, DecimalFormatSymbols symbols) {
        createFromPatternAndSymbols(pattern, symbols);
    }

    private void createFromPatternAndSymbols(String pattern, DecimalFormatSymbols inputSymbols) {
        // Always applyPattern after the symbols are set
        symbols = (DecimalFormatSymbols) inputSymbols.clone();
        if (pattern.indexOf(CURRENCY_SIGN) >= 0) {
            // Only spend time with currency symbols when we're going to display it.
            // Also set some defaults before the apply pattern.
            setCurrencyForSymbols();
        }
        applyPatternWithoutExpandAffix(pattern, false);
        if (currencySignCount == CURRENCY_SIGN_COUNT_IN_PLURAL_FORMAT) {
            currencyPluralInfo = new CurrencyPluralInfo(symbols.getULocale());
        } else {
            expandAffixAdjustWidth(null);
        }
    }

    /**
     * Creates a DecimalFormat from the given pattern, symbols, information used for
     * currency plural format, and format style. Use this constructor when you need to
     * completely customize the behavior of the format.
     *
     * <p>To obtain standard formats for a given locale, use the factory methods on
     * NumberFormat such as getInstance or getCurrencyInstance.
     *
     * <p>If you need only minor adjustments to a standard format, you can modify the
     * format returned by a NumberFormat factory method using the setters.
     *
     * <p>If you want to completely customize a decimal format, using your own
     * DecimalFormatSymbols (such as group separators) and your own information for
     * currency plural formatting (such as plural rule and currency plural patterns), you
     * can use this constructor.
     *
     * @param pattern a non-localized pattern string
     * @param symbols the set of symbols to be used
     * @param infoInput the information used for currency plural format, including
     * currency plural patterns and plural rules.
     * @param style the decimal formatting style, it is one of the following values:
     * NumberFormat.NUMBERSTYLE; NumberFormat.CURRENCYSTYLE; NumberFormat.PERCENTSTYLE;
     * NumberFormat.SCIENTIFICSTYLE; NumberFormat.INTEGERSTYLE;
     * NumberFormat.ISOCURRENCYSTYLE; NumberFormat.PLURALCURRENCYSTYLE;
     */
    public DecimalFormat(String pattern, DecimalFormatSymbols symbols, CurrencyPluralInfo infoInput,
                         int style) {
        CurrencyPluralInfo info = infoInput;
        if (style == NumberFormat.PLURALCURRENCYSTYLE) {
            info = (CurrencyPluralInfo) infoInput.clone();
        }
        create(pattern, symbols, info, style);
    }

    private void create(String pattern, DecimalFormatSymbols inputSymbols, CurrencyPluralInfo info,
                        int inputStyle) {
        if (inputStyle != NumberFormat.PLURALCURRENCYSTYLE) {
            createFromPatternAndSymbols(pattern, inputSymbols);
        } else {
            // Always applyPattern after the symbols are set
            symbols = (DecimalFormatSymbols) inputSymbols.clone();
            currencyPluralInfo = info;
            // the pattern used in format is not fixed until formatting, in which, the
            // number is known and will be used to pick the right pattern based on plural
            // count.  Here, set the pattern as the pattern of plural count == "other".
            // For most locale, the patterns are probably the same for all plural
            // count. If not, the right pattern need to be re-applied during format.
            String currencyPluralPatternForOther =
                currencyPluralInfo.getCurrencyPluralPattern("other");
            applyPatternWithoutExpandAffix(currencyPluralPatternForOther, false);
            setCurrencyForSymbols();
        }
        style = inputStyle;
    }

    /**
     * Creates a DecimalFormat for currency plural format from the given pattern, symbols,
     * and style.
     */
    DecimalFormat(String pattern, DecimalFormatSymbols inputSymbols, int style) {
        CurrencyPluralInfo info = null;
        if (style == NumberFormat.PLURALCURRENCYSTYLE) {
            info = new CurrencyPluralInfo(inputSymbols.getULocale());
        }
        create(pattern, inputSymbols, info, style);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition) {
        return format(number, result, fieldPosition, false);
    }

    // See if number is negative.
    // usage: isNegative(multiply(numberToBeFormatted));
    private boolean isNegative(double number) {
        // Detecting whether a double is negative is easy with the exception of the value
        // -0.0. This is a double which has a zero mantissa (and exponent), but a negative
        // sign bit. It is semantically distinct from a zero with a positive sign bit, and
        // this distinction is important to certain kinds of computations. However, it's a
        // little tricky to detect, since (-0.0 == 0.0) and !(-0.0 < 0.0). How then, you
        // may ask, does it behave distinctly from +0.0? Well, 1/(-0.0) ==
        // -Infinity. Proper detection of -0.0 is needed to deal with the issues raised by
        // bugs 4106658, 4106667, and 4147706. Liu 7/6/98.
        return (number < 0.0) || (number == 0.0 && 1 / number < 0.0);
    }

    // Rounds the number and strips of the negative sign.
    // usage: round(multiply(numberToBeFormatted))
    private double round(double number) {
        boolean isNegative = isNegative(number);
        if (isNegative)
            number = -number;

        // Apply rounding after multiplier
        if (roundingDouble > 0.0) {
            // number = roundingDouble
            //    * round(number / roundingDouble, roundingMode, isNegative);
            return round(
                number, roundingDouble, roundingDoubleReciprocal, roundingMode,
                isNegative);
        }
        return number;
    }

    // Multiplies given number by multipler (if there is one) returning the new
    // number. If there is no multiplier, returns the number passed in unchanged.
    private double multiply(double number) {
        if (multiplier != 1) {
            return number * multiplier;
        }
        return number;
    }

    // [Spark/CDL] The actual method to format number. If boolean value
    // parseAttr == true, then attribute information will be recorded.
    private StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition,
                                boolean parseAttr) {
        fieldPosition.setBeginIndex(0);
        fieldPosition.setEndIndex(0);

        if (Double.isNaN(number)) {
            if (fieldPosition.getField() == NumberFormat.INTEGER_FIELD) {
                fieldPosition.setBeginIndex(result.length());
            } else if (fieldPosition.getFieldAttribute() == NumberFormat.Field.INTEGER) {
                fieldPosition.setBeginIndex(result.length());
            }

            result.append(symbols.getNaN());
            // TODO: Combine setting a single FieldPosition or adding to an AttributedCharacterIterator
            // into a function like recordAttribute(FieldAttribute, begin, end).

            // [Spark/CDL] Add attribute for NaN here.
            // result.append(symbols.getNaN());
            if (parseAttr) {
                addAttribute(Field.INTEGER, result.length() - symbols.getNaN().length(),
                             result.length());
            }
            if (fieldPosition.getField() == NumberFormat.INTEGER_FIELD) {
                fieldPosition.setEndIndex(result.length());
            } else if (fieldPosition.getFieldAttribute() == NumberFormat.Field.INTEGER) {
                fieldPosition.setEndIndex(result.length());
            }

            addPadding(result, fieldPosition, 0, 0);
            return result;
        }

        // Do this BEFORE checking to see if value is negative or infinite and
        // before rounding.
        number = multiply(number);
        boolean isNegative = isNegative(number);
        number = round(number);

        if (Double.isInfinite(number)) {
            int prefixLen = appendAffix(result, isNegative, true, fieldPosition, parseAttr);

            if (fieldPosition.getField() == NumberFormat.INTEGER_FIELD) {
                fieldPosition.setBeginIndex(result.length());
            } else if (fieldPosition.getFieldAttribute() == NumberFormat.Field.INTEGER) {
                fieldPosition.setBeginIndex(result.length());
            }

            // [Spark/CDL] Add attribute for infinity here.
            result.append(symbols.getInfinity());
            if (parseAttr) {
                addAttribute(Field.INTEGER, result.length() - symbols.getInfinity().length(),
                             result.length());
            }
            if (fieldPosition.getField() == NumberFormat.INTEGER_FIELD) {
                fieldPosition.setEndIndex(result.length());
            } else if (fieldPosition.getFieldAttribute() == NumberFormat.Field.INTEGER) {
                fieldPosition.setEndIndex(result.length());
            }

            int suffixLen = appendAffix(result, isNegative, false, fieldPosition, parseAttr);

            addPadding(result, fieldPosition, prefixLen, suffixLen);
            return result;
        }

        int precision = precision(false);

        // This is to fix rounding for scientific notation. See ticket:10542.
        // This code should go away when a permanent fix is done for ticket:9931.
        //
        // This block of code only executes for scientific notation so it will not interfere with the
        // previous fix in {@link #resetActualRounding} for fixed decimal numbers.
        // Moreover this code only runs when there is rounding to be done (precision > 0) and when the
        // rounding mode is something other than ROUND_HALF_EVEN.
        // This block of code does the correct rounding of number in advance so that it will fit into
        // the number of digits indicated by precision. In this way, we avoid using the default
        // ROUND_HALF_EVEN behavior of DigitList. For example, if number = 0.003016 and roundingMode =
        // ROUND_DOWN and precision = 3 then after this code executes, number = 0.00301 (3 significant digits)
        if (useExponentialNotation && precision > 0 && number != 0.0 && roundingMode != BigDecimal.ROUND_HALF_EVEN) {
           int log10RoundingIncr = 1 - precision + (int) Math.floor(Math.log10(Math.abs(number)));
           double roundingIncReciprocal = 0.0;
           double roundingInc = 0.0;
           if (log10RoundingIncr < 0) {
               roundingIncReciprocal =
                       BigDecimal.ONE.movePointRight(-log10RoundingIncr).doubleValue();
           } else {
               roundingInc =
                       BigDecimal.ONE.movePointRight(log10RoundingIncr).doubleValue();
           }
           number = DecimalFormat.round(number, roundingInc, roundingIncReciprocal, roundingMode, isNegative);
        }
        // End fix for ticket:10542

        // At this point we are guaranteed a nonnegative finite
        // number.
        synchronized (digitList) {
            digitList.set(number, precision, !useExponentialNotation &&
                          !areSignificantDigitsUsed());
            return subformat(number, result, fieldPosition, isNegative, false, parseAttr);
        }
    }

    /**
     * This is a special function used by the CompactDecimalFormat subclass.
     * It completes only the rounding portion of the formatting and returns
     * the resulting double. CompactDecimalFormat uses the result to compute
     * the plural form to use.
     *
     * @param number The number to format.
     * @return The number rounded to the correct number of significant digits
     * with negative sign stripped off.
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    double adjustNumberAsInFormatting(double number) {
        if (Double.isNaN(number)) {
            return number;
        }
        number = round(multiply(number));
        if (Double.isInfinite(number)) {
            return number;
        }
        return toDigitList(number).getDouble();
    }

    @Deprecated
    DigitList toDigitList(double number) {
        DigitList result = new DigitList();
        result.set(number, precision(false), false);
        return result;
    }

    /**
      * This is a special function used by the CompactDecimalFormat subclass
      * to determine if the number to be formatted is negative.
      *
      * @param number The number to format.
      * @return True if number is negative.
      * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
      */
     @Deprecated
     boolean isNumberNegative(double number) {
         if (Double.isNaN(number)) {
             return false;
         }
         return isNegative(multiply(number));
     }

    /**
     * Round a double value to the nearest multiple of the given rounding increment,
     * according to the given mode. This is equivalent to rounding value/roundingInc to
     * the nearest integer, according to the given mode, and returning that integer *
     * roundingInc. Note this is changed from the version in 2.4, since division of
     * doubles have inaccuracies. jitterbug 1871.
     *
     * @param number
     *            the absolute value of the number to be rounded
     * @param roundingInc
     *            the rounding increment
     * @param roundingIncReciprocal
     *            if non-zero, is the reciprocal of rounding inc.
     * @param mode
     *            a BigDecimal rounding mode
     * @param isNegative
     *            true if the number to be rounded is negative
     * @return the absolute value of the rounded result
     */
    private static double round(double number, double roundingInc, double roundingIncReciprocal,
                                int mode, boolean isNegative) {

        double div = roundingIncReciprocal == 0.0 ? number / roundingInc : number *
            roundingIncReciprocal;

        // do the absolute cases first

        switch (mode) {
        case BigDecimal.ROUND_CEILING:
            div = (isNegative ? Math.floor(div + epsilon) : Math.ceil(div - epsilon));
            break;
        case BigDecimal.ROUND_FLOOR:
            div = (isNegative ? Math.ceil(div - epsilon) : Math.floor(div + epsilon));
            break;
        case BigDecimal.ROUND_DOWN:
            div = (Math.floor(div + epsilon));
            break;
        case BigDecimal.ROUND_UP:
            div = (Math.ceil(div - epsilon));
            break;
        case BigDecimal.ROUND_UNNECESSARY:
            if (div != Math.floor(div)) {
                throw new ArithmeticException("Rounding necessary");
            }
            return number;
        default:

            // Handle complex cases, where the choice depends on the closer value.

            // We figure out the distances to the two possible values, ceiling and floor.
            // We then go for the diff that is smaller.  Only if they are equal does the
            // mode matter.

            double ceil = Math.ceil(div);
            double ceildiff = ceil - div; // (ceil * roundingInc) - number;
            double floor = Math.floor(div);
            double floordiff = div - floor; // number - (floor * roundingInc);

            // Note that the diff values were those mapped back to the "normal" space by
            // using the roundingInc. I don't have access to the original author of the
            // code but suspect that that was to produce better result in edge cases
            // because of machine precision, rather than simply using the difference
            // between, say, ceil and div.  However, it didn't work in all cases. Am
            // trying instead using an epsilon value.

            switch (mode) {
            case BigDecimal.ROUND_HALF_EVEN:
                // We should be able to just return Math.rint(a), but this
                // doesn't work in some VMs.
                // if one is smaller than the other, take the corresponding side
                if (floordiff + epsilon < ceildiff) {
                    div = floor;
                } else if (ceildiff + epsilon < floordiff) {
                    div = ceil;
                } else { // they are equal, so we want to round to whichever is even
                    double testFloor = floor / 2;
                    div = (testFloor == Math.floor(testFloor)) ? floor : ceil;
                }
                break;
            case BigDecimal.ROUND_HALF_DOWN:
                div = ((floordiff <= ceildiff + epsilon) ? floor : ceil);
                break;
            case BigDecimal.ROUND_HALF_UP:
                div = ((ceildiff <= floordiff + epsilon) ? ceil : floor);
                break;
            default:
                throw new IllegalArgumentException("Invalid rounding mode: " + mode);
            }
        }
        number = roundingIncReciprocal == 0.0 ? div * roundingInc : div / roundingIncReciprocal;
        return number;
    }

    private static double epsilon = 0.00000000001;

    /**
     */
    // [Spark/CDL] Delegate to format_long_StringBuffer_FieldPosition_boolean
    @Override
    public StringBuffer format(long number, StringBuffer result, FieldPosition fieldPosition) {
        return format(number, result, fieldPosition, false);
    }

    private StringBuffer format(long number, StringBuffer result, FieldPosition fieldPosition,
                                boolean parseAttr) {
        fieldPosition.setBeginIndex(0);
        fieldPosition.setEndIndex(0);

        // If we are to do rounding, we need to move into the BigDecimal
        // domain in order to do divide/multiply correctly.
        if (actualRoundingIncrementICU != null) {
            return format(BigDecimal.valueOf(number), result, fieldPosition);
        }

        boolean isNegative = (number < 0);
        if (isNegative)
            number = -number;

        // In general, long values always represent real finite numbers, so we don't have
        // to check for +/- Infinity or NaN. However, there is one case we have to be
        // careful of: The multiplier can push a number near MIN_VALUE or MAX_VALUE
        // outside the legal range. We check for this before multiplying, and if it
        // happens we use BigInteger instead.
        if (multiplier != 1) {
            boolean tooBig = false;
            if (number < 0) { // This can only happen if number == Long.MIN_VALUE
                long cutoff = Long.MIN_VALUE / multiplier;
                tooBig = (number <= cutoff); // number == cutoff can only happen if multiplier == -1
            } else {
                long cutoff = Long.MAX_VALUE / multiplier;
                tooBig = (number > cutoff);
            }
            if (tooBig) {
                // [Spark/CDL] Use
                // format_BigInteger_StringBuffer_FieldPosition_boolean instead
                // parseAttr is used to judge whether to synthesize attributes.
                return format(BigInteger.valueOf(isNegative ? -number : number), result,
                              fieldPosition, parseAttr);
            }
        }

        number *= multiplier;
        synchronized (digitList) {
            digitList.set(number, precision(true));
            // Issue 11808
            if (digitList.wasRounded() && roundingMode == BigDecimal.ROUND_UNNECESSARY) {
                throw new ArithmeticException("Rounding necessary");
            }
            return subformat(number, result, fieldPosition, isNegative, true, parseAttr);
        }
    }

    /**
     * Formats a BigInteger number.
     */
    @Override
    public StringBuffer format(BigInteger number, StringBuffer result,
                               FieldPosition fieldPosition) {
        return format(number, result, fieldPosition, false);
    }

    private StringBuffer format(BigInteger number, StringBuffer result, FieldPosition fieldPosition,
                                boolean parseAttr) {
        // If we are to do rounding, we need to move into the BigDecimal
        // domain in order to do divide/multiply correctly.
        if (actualRoundingIncrementICU != null) {
            return format(new BigDecimal(number), result, fieldPosition);
        }

        if (multiplier != 1) {
            number = number.multiply(BigInteger.valueOf(multiplier));
        }

        // At this point we are guaranteed a nonnegative finite
        // number.
        synchronized (digitList) {
            digitList.set(number, precision(true));
            // For issue 11808.
            if (digitList.wasRounded() && roundingMode == BigDecimal.ROUND_UNNECESSARY) {
                throw new ArithmeticException("Rounding necessary");
            }
            return subformat(number.intValue(), result, fieldPosition, number.signum() < 0, true,
                             parseAttr);
        }
    }

    /**
     * Formats a BigDecimal number.
     */
    @Override
    public StringBuffer format(java.math.BigDecimal number, StringBuffer result,
                               FieldPosition fieldPosition) {
        return format(number, result, fieldPosition, false);
    }

    private StringBuffer format(java.math.BigDecimal number, StringBuffer result,
                                FieldPosition fieldPosition,
            boolean parseAttr) {
        if (multiplier != 1) {
            number = number.multiply(java.math.BigDecimal.valueOf(multiplier));
        }

        if (actualRoundingIncrement != null) {
            number = number.divide(actualRoundingIncrement, 0, roundingMode).multiply(actualRoundingIncrement);
        }

        synchronized (digitList) {
            digitList.set(number, precision(false), !useExponentialNotation &&
                          !areSignificantDigitsUsed());
            // For issue 11808.
            if (digitList.wasRounded() && roundingMode == BigDecimal.ROUND_UNNECESSARY) {
                throw new ArithmeticException("Rounding necessary");
            }
            return subformat(number.doubleValue(), result, fieldPosition, number.signum() < 0,
                             false, parseAttr);
        }
    }

    /**
     * Formats a BigDecimal number.
     */
    @Override
    public StringBuffer format(BigDecimal number, StringBuffer result,
                               FieldPosition fieldPosition) {
         // This method is just a copy of the corresponding java.math.BigDecimal method
         // for now. It isn't very efficient since it must create a conversion object to
         // do math on the rounding increment. In the future we may try to clean this up,
         // or even better, limit our support to just one flavor of BigDecimal.
        if (multiplier != 1) {
            number = number.multiply(BigDecimal.valueOf(multiplier), mathContext);
        }

        if (actualRoundingIncrementICU != null) {
            number = number.divide(actualRoundingIncrementICU, 0, roundingMode)
                .multiply(actualRoundingIncrementICU, mathContext);
        }

        synchronized (digitList) {
            digitList.set(number, precision(false), !useExponentialNotation &&
                          !areSignificantDigitsUsed());
            // For issue 11808.
            if (digitList.wasRounded() && roundingMode == BigDecimal.ROUND_UNNECESSARY) {
                throw new ArithmeticException("Rounding necessary");
            }
            return subformat(number.doubleValue(), result, fieldPosition, number.signum() < 0,
                             false, false);
        }
    }

    /**
     * Returns true if a grouping separator belongs at the given position, based on whether
     * grouping is in use and the values of the primary and secondary grouping interval.
     *
     * @param pos the number of integer digits to the right of the current position. Zero
     * indicates the position after the rightmost integer digit.
     * @return true if a grouping character belongs at the current position.
     */
    private boolean isGroupingPosition(int pos) {
        boolean result = false;
        if (isGroupingUsed() && (pos > 0) && (groupingSize > 0)) {
            if ((groupingSize2 > 0) && (pos > groupingSize)) {
                result = ((pos - groupingSize) % groupingSize2) == 0;
            } else {
                result = pos % groupingSize == 0;
            }
        }
        return result;
    }

    /**
     * Return the number of fraction digits to display, or the total
     * number of digits for significant digit formats and exponential
     * formats.
     */
    private int precision(boolean isIntegral) {
        if (areSignificantDigitsUsed()) {
            return getMaximumSignificantDigits();
        } else if (useExponentialNotation) {
            return getMinimumIntegerDigits() + getMaximumFractionDigits();
        } else {
            return isIntegral ? 0 : getMaximumFractionDigits();
        }
    }

    private StringBuffer subformat(int number, StringBuffer result, FieldPosition fieldPosition,
                                   boolean isNegative, boolean isInteger, boolean parseAttr) {
        if (currencySignCount == CURRENCY_SIGN_COUNT_IN_PLURAL_FORMAT) {
            // compute the plural category from the digitList plus other settings
            return subformat(currencyPluralInfo.select(getFixedDecimal(number)),
                             result, fieldPosition, isNegative,
                             isInteger, parseAttr);
        } else {
            return subformat(result, fieldPosition, isNegative, isInteger, parseAttr);
        }
    }

    /**
     * This is ugly, but don't see a better way to do it without major restructuring of the code.
     */
    /*package*/ FixedDecimal getFixedDecimal(double number) {
        // get the visible fractions and the number of fraction digits.
       return getFixedDecimal(number, digitList);
    }

    FixedDecimal getFixedDecimal(double number, DigitList dl) {
        int fractionalDigitsInDigitList = dl.count - dl.decimalAt;
        int v;
        long f;
        int maxFractionalDigits;
        int minFractionalDigits;
        if (useSignificantDigits) {
            maxFractionalDigits = maxSignificantDigits - dl.decimalAt;
            minFractionalDigits = minSignificantDigits - dl.decimalAt;
            if (minFractionalDigits < 0) {
                minFractionalDigits = 0;
            }
            if (maxFractionalDigits < 0) {
                maxFractionalDigits = 0;
            }
        } else {
            maxFractionalDigits = getMaximumFractionDigits();
            minFractionalDigits = getMinimumFractionDigits();
        }
        v = fractionalDigitsInDigitList;
        if (v < minFractionalDigits) {
            v = minFractionalDigits;
        } else if (v > maxFractionalDigits) {
            v = maxFractionalDigits;
        }
        f = 0;
        if (v > 0) {
            for (int i = Math.max(0, dl.decimalAt); i < dl.count; ++i) {
                f *= 10;
                f += (dl.digits[i] - '0');
            }
            for (int i = v; i < fractionalDigitsInDigitList; ++i) {
                f *= 10;
            }
        }
        return new FixedDecimal(number, v, f);
    }

    private StringBuffer subformat(double number, StringBuffer result, FieldPosition fieldPosition,
                                   boolean isNegative,
            boolean isInteger, boolean parseAttr) {
        if (currencySignCount == CURRENCY_SIGN_COUNT_IN_PLURAL_FORMAT) {
            // compute the plural category from the digitList plus other settings
            return subformat(currencyPluralInfo.select(getFixedDecimal(number)),
                             result, fieldPosition, isNegative,
                             isInteger, parseAttr);
        } else {
            return subformat(result, fieldPosition, isNegative, isInteger, parseAttr);
        }
    }

    private StringBuffer subformat(String pluralCount, StringBuffer result, FieldPosition fieldPosition,
            boolean isNegative, boolean isInteger, boolean parseAttr) {
        // There are 2 ways to activate currency plural format: by applying a pattern with
        // 3 currency sign directly, or by instantiate a decimal formatter using
        // PLURALCURRENCYSTYLE.  For both cases, the number of currency sign in the
        // pattern is 3.  Even if the number of currency sign in the pattern is 3, it does
        // not mean we need to reset the pattern.  For 1st case, we do not need to reset
        // pattern.  For 2nd case, we might need to reset pattern, if the default pattern
        // (corresponding to plural count 'other') we use is different from the pattern
        // based on 'pluralCount'.
        //
        // style is only valid when decimal formatter is constructed through
        // DecimalFormat(pattern, symbol, style)
        if (style == NumberFormat.PLURALCURRENCYSTYLE) {
            // May need to reset pattern if the style is PLURALCURRENCYSTYLE.
            String currencyPluralPattern = currencyPluralInfo.getCurrencyPluralPattern(pluralCount);
            if (formatPattern.equals(currencyPluralPattern) == false) {
                applyPatternWithoutExpandAffix(currencyPluralPattern, false);
            }
        }
        // Expand the affix to the right name according to the plural rule.  This is only
        // used for currency plural formatting.  Currency plural name is not a fixed
        // static one, it is a dynamic name based on the currency plural count.  So, the
        // affixes need to be expanded here.  For other cases, the affix is a static one
        // based on pattern alone, and it is already expanded during applying pattern, or
        // setDecimalFormatSymbols, or setCurrency.
        expandAffixAdjustWidth(pluralCount);
        return subformat(result, fieldPosition, isNegative, isInteger, parseAttr);
    }

    /**
     * Complete the formatting of a finite number. On entry, the
     * digitList must be filled in with the correct digits.
     */
    private StringBuffer subformat(StringBuffer result, FieldPosition fieldPosition,
                                   boolean isNegative, boolean isInteger, boolean parseAttr) {
        // NOTE: This isn't required anymore because DigitList takes care of this.
        //
        // // The negative of the exponent represents the number of leading // zeros
        // between the decimal and the first non-zero digit, for // a value < 0.1 (e.g.,
        // for 0.00123, -fExponent == 2). If this // is more than the maximum fraction
        // digits, then we have an underflow // for the printed representation. We
        // recognize this here and set // the DigitList representation to zero in this
        // situation.
        //
        // if (-digitList.decimalAt >= getMaximumFractionDigits())
        // {
        // digitList.count = 0;
        // }



        // Per bug 4147706, DecimalFormat must respect the sign of numbers which format as
        // zero. This allows sensible computations and preserves relations such as
        // signum(1/x) = signum(x), where x is +Infinity or -Infinity.  Prior to this fix,
        // we always formatted zero values as if they were positive. Liu 7/6/98.
        if (digitList.isZero()) {
            digitList.decimalAt = 0; // Normalize
        }

        int prefixLen = appendAffix(result, isNegative, true, fieldPosition, parseAttr);

        if (useExponentialNotation) {
            subformatExponential(result, fieldPosition, parseAttr);
        } else {
            subformatFixed(result, fieldPosition, isInteger, parseAttr);
        }

        int suffixLen = appendAffix(result, isNegative, false, fieldPosition, parseAttr);
        addPadding(result, fieldPosition, prefixLen, suffixLen);
        return result;
    }

    private void subformatFixed(StringBuffer result,
            FieldPosition fieldPosition,
            boolean isInteger,
            boolean parseAttr) {
        String[] digits = symbols.getDigitStrings();

        String grouping = currencySignCount == CURRENCY_SIGN_COUNT_ZERO ?
                symbols.getGroupingSeparatorString(): symbols.getMonetaryGroupingSeparatorString();
        String decimal = currencySignCount == CURRENCY_SIGN_COUNT_ZERO ?
                symbols.getDecimalSeparatorString() : symbols.getMonetaryDecimalSeparatorString();
        boolean useSigDig = areSignificantDigitsUsed();
        int maxIntDig = getMaximumIntegerDigits();
        int minIntDig = getMinimumIntegerDigits();
        int i;
        // [Spark/CDL] Record the integer start index.
        int intBegin = result.length();
        // Record field information for caller.
        if (fieldPosition.getField() == NumberFormat.INTEGER_FIELD ||
                fieldPosition.getFieldAttribute() == NumberFormat.Field.INTEGER) {
            fieldPosition.setBeginIndex(intBegin);
        }
        long fractionalDigits = 0;
        int fractionalDigitsCount = 0;
        boolean recordFractionDigits = false;

        int sigCount = 0;
        int minSigDig = getMinimumSignificantDigits();
        int maxSigDig = getMaximumSignificantDigits();
        if (!useSigDig) {
            minSigDig = 0;
            maxSigDig = Integer.MAX_VALUE;
        }

        // Output the integer portion. Here 'count' is the total number of integer
        // digits we will display, including both leading zeros required to satisfy
        // getMinimumIntegerDigits, and actual digits present in the number.
        int count = useSigDig ? Math.max(1, digitList.decimalAt) : minIntDig;
        if (digitList.decimalAt > 0 && count < digitList.decimalAt) {
            count = digitList.decimalAt;
        }

        // Handle the case where getMaximumIntegerDigits() is smaller than the real
        // number of integer digits. If this is so, we output the least significant
        // max integer digits. For example, the value 1997 printed with 2 max integer
        // digits is just "97".

        int digitIndex = 0; // Index into digitList.fDigits[]
        if (count > maxIntDig && maxIntDig >= 0) {
            count = maxIntDig;
            digitIndex = digitList.decimalAt - count;
        }

        int sizeBeforeIntegerPart = result.length();
        for (i = count - 1; i >= 0; --i) {
            if (i < digitList.decimalAt && digitIndex < digitList.count
                && sigCount < maxSigDig) {
                // Output a real digit
                result.append(digits[digitList.getDigitValue(digitIndex++)]);
                ++sigCount;
            } else {
                // Output a zero (leading or trailing)
                result.append(digits[0]);
                if (sigCount > 0) {
                    ++sigCount;
                }
            }

            // Output grouping separator if necessary.
            if (isGroupingPosition(i)) {
                result.append(grouping);
                // [Spark/CDL] Add grouping separator attribute here.
                // Set only for the first instance.
                // Length of grouping separator is 1.
                if (fieldPosition.getFieldAttribute() == Field.GROUPING_SEPARATOR &&
                        fieldPosition.getBeginIndex() == 0 && fieldPosition.getEndIndex() == 0) {
                    fieldPosition.setBeginIndex(result.length()-1);
                    fieldPosition.setEndIndex(result.length());
                }
                if (parseAttr) {
                    addAttribute(Field.GROUPING_SEPARATOR, result.length() - 1, result.length());
                }
            }
        }

        // Record field information for caller.
        if (fieldPosition.getField() == NumberFormat.INTEGER_FIELD ||
                fieldPosition.getFieldAttribute() == NumberFormat.Field.INTEGER) {
            fieldPosition.setEndIndex(result.length());
        }

        // This handles the special case of formatting 0. For zero only, we count the
        // zero to the left of the decimal point as one signficant digit. Ordinarily we
        // do not count any leading 0's as significant. If the number we are formatting
        // is not zero, then either sigCount or digits.getCount() will be non-zero.
        if (sigCount == 0 && digitList.count == 0) {
          sigCount = 1;
        }

        // Determine whether or not there are any printable fractional digits. If
        // we've used up the digits we know there aren't.
        boolean fractionPresent = (!isInteger && digitIndex < digitList.count)
                || (useSigDig ? (sigCount < minSigDig) : (getMinimumFractionDigits() > 0));

        // If there is no fraction present, and we haven't printed any integer digits,
        // then print a zero. Otherwise we won't print _any_ digits, and we won't be
        // able to parse this string.
        if (!fractionPresent && result.length() == sizeBeforeIntegerPart)
            result.append(digits[0]);
        // [Spark/CDL] Add attribute for integer part.
        if (parseAttr) {
            addAttribute(Field.INTEGER, intBegin, result.length());
        }
        // Output the decimal separator if we always do so.
        if (decimalSeparatorAlwaysShown || fractionPresent) {
            if (fieldPosition.getFieldAttribute() == Field.DECIMAL_SEPARATOR) {
                fieldPosition.setBeginIndex(result.length());
            }
            result.append(decimal);
            if (fieldPosition.getFieldAttribute() == Field.DECIMAL_SEPARATOR) {
                fieldPosition.setEndIndex(result.length());
            }
            // [Spark/CDL] Add attribute for decimal separator
            if (parseAttr) {
                addAttribute(Field.DECIMAL_SEPARATOR, result.length() - 1, result.length());
            }
        }

        // Record field information for caller.
        if (fieldPosition.getField() == NumberFormat.FRACTION_FIELD) {
            fieldPosition.setBeginIndex(result.length());
        } else if (fieldPosition.getFieldAttribute() == NumberFormat.Field.FRACTION) {
            fieldPosition.setBeginIndex(result.length());
        }

        // [Spark/CDL] Record the begin index of fraction part.
        int fracBegin = result.length();
        recordFractionDigits = fieldPosition instanceof UFieldPosition;

        count = useSigDig ? Integer.MAX_VALUE : getMaximumFractionDigits();
        if (useSigDig && (sigCount == maxSigDig ||
                          (sigCount >= minSigDig && digitIndex == digitList.count))) {
            count = 0;
        }
        for (i = 0; i < count; ++i) {
            // Here is where we escape from the loop. We escape if we've output the
            // maximum fraction digits (specified in the for expression above). We
            // also stop when we've output the minimum digits and either: we have an
            // integer, so there is no fractional stuff to display, or we're out of
            // significant digits.
            if (!useSigDig && i >= getMinimumFractionDigits() &&
                (isInteger || digitIndex >= digitList.count)) {
                break;
            }

            // Output leading fractional zeros. These are zeros that come after the
            // decimal but before any significant digits. These are only output if
            // abs(number being formatted) < 1.0.
            if (-1 - i > (digitList.decimalAt - 1)) {
                result.append(digits[0]);
                if (recordFractionDigits) {
                    ++fractionalDigitsCount;
                    fractionalDigits *= 10;
                }
                continue;
            }

            // Output a digit, if we have any precision left, or a zero if we
            // don't. We don't want to output noise digits.
            if (!isInteger && digitIndex < digitList.count) {
                byte digit = digitList.getDigitValue(digitIndex++);
                result.append(digits[digit]);
                if (recordFractionDigits) {
                    ++fractionalDigitsCount;
                    fractionalDigits *= 10;
                    fractionalDigits += digit;
                }
            } else {
                result.append(digits[0]);
                if (recordFractionDigits) {
                    ++fractionalDigitsCount;
                    fractionalDigits *= 10;
                }
            }

            // If we reach the maximum number of significant digits, or if we output
            // all the real digits and reach the minimum, then we are done.
            ++sigCount;
            if (useSigDig && (sigCount == maxSigDig ||
                              (digitIndex == digitList.count && sigCount >= minSigDig))) {
                break;
            }
        }

        // Record field information for caller.
        if (fieldPosition.getField() == NumberFormat.FRACTION_FIELD) {
            fieldPosition.setEndIndex(result.length());
        } else if (fieldPosition.getFieldAttribute() == NumberFormat.Field.FRACTION) {
            fieldPosition.setEndIndex(result.length());
        }
        if (recordFractionDigits) {
            ((UFieldPosition) fieldPosition).setFractionDigits(fractionalDigitsCount, fractionalDigits);
        }

        // [Spark/CDL] Add attribute information if necessary.
        if (parseAttr && (decimalSeparatorAlwaysShown || fractionPresent)) {
            addAttribute(Field.FRACTION, fracBegin, result.length());
        }
    }

    private void subformatExponential(StringBuffer result,
            FieldPosition fieldPosition,
            boolean parseAttr) {
        String[] digits = symbols.getDigitStringsLocal();
        String decimal = currencySignCount == CURRENCY_SIGN_COUNT_ZERO ?
                symbols.getDecimalSeparatorString() : symbols.getMonetaryDecimalSeparatorString();
        boolean useSigDig = areSignificantDigitsUsed();
        int maxIntDig = getMaximumIntegerDigits();
        int minIntDig = getMinimumIntegerDigits();
        int i;
        // Record field information for caller.
        if (fieldPosition.getField() == NumberFormat.INTEGER_FIELD) {
            fieldPosition.setBeginIndex(result.length());
            fieldPosition.setEndIndex(-1);
        } else if (fieldPosition.getField() == NumberFormat.FRACTION_FIELD) {
            fieldPosition.setBeginIndex(-1);
        } else if (fieldPosition.getFieldAttribute() == NumberFormat.Field.INTEGER) {
            fieldPosition.setBeginIndex(result.length());
            fieldPosition.setEndIndex(-1);
        } else if (fieldPosition.getFieldAttribute() == NumberFormat.Field.FRACTION) {
            fieldPosition.setBeginIndex(-1);
        }

        // [Spark/CDL]
        // the begin index of integer part
        // the end index of integer part
        // the begin index of fractional part
        int intBegin = result.length();
        int intEnd = -1;
        int fracBegin = -1;
        int minFracDig = 0;
        if (useSigDig) {
            maxIntDig = minIntDig = 1;
            minFracDig = getMinimumSignificantDigits() - 1;
        } else {
            minFracDig = getMinimumFractionDigits();
            if (maxIntDig > MAX_SCIENTIFIC_INTEGER_DIGITS) {
                maxIntDig = 1;
                if (maxIntDig < minIntDig) {
                    maxIntDig = minIntDig;
                }
            }
            if (maxIntDig > minIntDig) {
                minIntDig = 1;
            }
        }
        long fractionalDigits = 0;
        int fractionalDigitsCount = 0;
        boolean recordFractionDigits = false;

        // Minimum integer digits are handled in exponential format by adjusting the
        // exponent. For example, 0.01234 with 3 minimum integer digits is "123.4E-4".

        // Maximum integer digits are interpreted as indicating the repeating
        // range. This is useful for engineering notation, in which the exponent is
        // restricted to a multiple of 3. For example, 0.01234 with 3 maximum integer
        // digits is "12.34e-3".  If maximum integer digits are defined and are larger
        // than minimum integer digits, then minimum integer digits are ignored.

        int exponent = digitList.decimalAt;
        if (maxIntDig > 1 && maxIntDig != minIntDig) {
            // A exponent increment is defined; adjust to it.
            exponent = (exponent > 0) ? (exponent - 1) / maxIntDig : (exponent / maxIntDig) - 1;
            exponent *= maxIntDig;
        } else {
            // No exponent increment is defined; use minimum integer digits.
            // If none is specified, as in "#E0", generate 1 integer digit.
            exponent -= (minIntDig > 0 || minFracDig > 0) ? minIntDig : 1;
        }

        // We now output a minimum number of digits, and more if there are more
        // digits, up to the maximum number of digits. We place the decimal point
        // after the "integer" digits, which are the first (decimalAt - exponent)
        // digits.
        int minimumDigits = minIntDig + minFracDig;
        // The number of integer digits is handled specially if the number
        // is zero, since then there may be no digits.
        int integerDigits = digitList.isZero() ? minIntDig : digitList.decimalAt - exponent;
        int totalDigits = digitList.count;
        if (minimumDigits > totalDigits)
            totalDigits = minimumDigits;
        if (integerDigits > totalDigits)
            totalDigits = integerDigits;

        for (i = 0; i < totalDigits; ++i) {
            if (i == integerDigits) {
                // Record field information for caller.
                if (fieldPosition.getField() == NumberFormat.INTEGER_FIELD) {
                    fieldPosition.setEndIndex(result.length());
                } else if (fieldPosition.getFieldAttribute() == NumberFormat.Field.INTEGER) {
                    fieldPosition.setEndIndex(result.length());
                }

                // [Spark/CDL] Add attribute for integer part
                if (parseAttr) {
                    intEnd = result.length();
                    addAttribute(Field.INTEGER, intBegin, result.length());
                }
                if (fieldPosition.getFieldAttribute() == Field.DECIMAL_SEPARATOR) {
                    fieldPosition.setBeginIndex(result.length());
                }
                result.append(decimal);
                if (fieldPosition.getFieldAttribute() == Field.DECIMAL_SEPARATOR) {
                    fieldPosition.setEndIndex(result.length());
                }
                // [Spark/CDL] Add attribute for decimal separator
                fracBegin = result.length();
                if (parseAttr) {
                    // Length of decimal separator is 1.
                    int decimalSeparatorBegin = result.length() - 1;
                    addAttribute(Field.DECIMAL_SEPARATOR, decimalSeparatorBegin,
                                 result.length());
                }
                // Record field information for caller.
                if (fieldPosition.getField() == NumberFormat.FRACTION_FIELD) {
                    fieldPosition.setBeginIndex(result.length());
                } else if (fieldPosition.getFieldAttribute() == NumberFormat.Field.FRACTION) {
                    fieldPosition.setBeginIndex(result.length());
                }
                recordFractionDigits = fieldPosition instanceof UFieldPosition;

            }
            byte digit = (i < digitList.count) ? digitList.getDigitValue(i) : (byte)0;
            result.append(digits[digit]);
            if (recordFractionDigits) {
                ++fractionalDigitsCount;
                fractionalDigits *= 10;
                fractionalDigits += digit;
            }
        }

        // For ICU compatibility and format 0 to 0E0 with pattern "#E0" [Richard/GCL]
        if (digitList.isZero() && (totalDigits == 0)) {
            result.append(digits[0]);
        }

        // add the decimal separator if it is to be always shown AND there are no decimal digits
        if ((fracBegin == -1) && this.decimalSeparatorAlwaysShown) {
            if (fieldPosition.getFieldAttribute() == Field.DECIMAL_SEPARATOR) {
                fieldPosition.setBeginIndex(result.length());
            }
            result.append(decimal);
            if (fieldPosition.getFieldAttribute() == Field.DECIMAL_SEPARATOR) {
                fieldPosition.setEndIndex(result.length());
            }
            if (parseAttr) {
                // Length of decimal separator is 1.
                int decimalSeparatorBegin = result.length() - 1;
                addAttribute(Field.DECIMAL_SEPARATOR, decimalSeparatorBegin, result.length());
            }
        }

        // Record field information
        if (fieldPosition.getField() == NumberFormat.INTEGER_FIELD) {
            if (fieldPosition.getEndIndex() < 0) {
                fieldPosition.setEndIndex(result.length());
            }
        } else if (fieldPosition.getField() == NumberFormat.FRACTION_FIELD) {
            if (fieldPosition.getBeginIndex() < 0) {
                fieldPosition.setBeginIndex(result.length());
            }
            fieldPosition.setEndIndex(result.length());
        } else if (fieldPosition.getFieldAttribute() == NumberFormat.Field.INTEGER) {
            if (fieldPosition.getEndIndex() < 0) {
                fieldPosition.setEndIndex(result.length());
            }
        } else if (fieldPosition.getFieldAttribute() == NumberFormat.Field.FRACTION) {
            if (fieldPosition.getBeginIndex() < 0) {
                fieldPosition.setBeginIndex(result.length());
            }
            fieldPosition.setEndIndex(result.length());
        }
        if (recordFractionDigits) {
            ((UFieldPosition) fieldPosition).setFractionDigits(fractionalDigitsCount, fractionalDigits);
        }

        // [Spark/CDL] Calculate the end index of integer part and fractional
        // part if they are not properly processed yet.
        if (parseAttr) {
            if (intEnd < 0) {
                addAttribute(Field.INTEGER, intBegin, result.length());
            }
            if (fracBegin > 0) {
                addAttribute(Field.FRACTION, fracBegin, result.length());
            }
        }

        // The exponent is output using the pattern-specified minimum exponent
        // digits. There is no maximum limit to the exponent digits, since truncating
        // the exponent would result in an unacceptable inaccuracy.
        if (fieldPosition.getFieldAttribute() == Field.EXPONENT_SYMBOL) {
            fieldPosition.setBeginIndex(result.length());
        }

        result.append(symbols.getExponentSeparator());
        if (fieldPosition.getFieldAttribute() == Field.EXPONENT_SYMBOL) {
            fieldPosition.setEndIndex(result.length());
        }
        // [Spark/CDL] For exponent symbol, add an attribute.
        if (parseAttr) {
            addAttribute(Field.EXPONENT_SYMBOL, result.length() -
                         symbols.getExponentSeparator().length(), result.length());
        }
        // For zero values, we force the exponent to zero. We must do this here, and
        // not earlier, because the value is used to determine integer digit count
        // above.
        if (digitList.isZero())
            exponent = 0;

        boolean negativeExponent = exponent < 0;
        if (negativeExponent) {
            exponent = -exponent;
            if (fieldPosition.getFieldAttribute() == Field.EXPONENT_SIGN) {
                fieldPosition.setBeginIndex(result.length());
            }
            result.append(symbols.getMinusSignString());
            if (fieldPosition.getFieldAttribute() == Field.EXPONENT_SIGN) {
                fieldPosition.setEndIndex(result.length());
            }
            // [Spark/CDL] If exponent has sign, then add an exponent sign
            // attribute.
            if (parseAttr) {
                // Length of exponent sign is 1.
                addAttribute(Field.EXPONENT_SIGN, result.length() - 1, result.length());
            }
        } else if (exponentSignAlwaysShown) {
            if (fieldPosition.getFieldAttribute() == Field.EXPONENT_SIGN) {
                fieldPosition.setBeginIndex(result.length());
            }
            result.append(symbols.getPlusSignString());
            if (fieldPosition.getFieldAttribute() == Field.EXPONENT_SIGN) {
                fieldPosition.setEndIndex(result.length());
            }
            // [Spark/CDL] Add an plus sign attribute.
            if (parseAttr) {
                // Length of exponent sign is 1.
                int expSignBegin = result.length() - 1;
                addAttribute(Field.EXPONENT_SIGN, expSignBegin, result.length());
            }
        }
        int expBegin = result.length();
        digitList.set(exponent);
        {
            int expDig = minExponentDigits;
            if (useExponentialNotation && expDig < 1) {
                expDig = 1;
            }
            for (i = digitList.decimalAt; i < expDig; ++i)
                result.append(digits[0]);
        }
        for (i = 0; i < digitList.decimalAt; ++i) {
            result.append((i < digitList.count) ? digits[digitList.getDigitValue(i)]
                          : digits[0]);
        }
        // [Spark/CDL] Add attribute for exponent part.
        if (fieldPosition.getFieldAttribute() == Field.EXPONENT) {
            fieldPosition.setBeginIndex(expBegin);
            fieldPosition.setEndIndex(result.length());
        }
        if (parseAttr) {
            addAttribute(Field.EXPONENT, expBegin, result.length());
        }
    }

    private final void addPadding(StringBuffer result, FieldPosition fieldPosition, int prefixLen,
                                  int suffixLen) {
        if (formatWidth > 0) {
            int len = formatWidth - result.length();
            if (len > 0) {
                char[] padding = new char[len];
                for (int i = 0; i < len; ++i) {
                    padding[i] = pad;
                }
                switch (padPosition) {
                case PAD_AFTER_PREFIX:
                    result.insert(prefixLen, padding);
                    break;
                case PAD_BEFORE_PREFIX:
                    result.insert(0, padding);
                    break;
                case PAD_BEFORE_SUFFIX:
                    result.insert(result.length() - suffixLen, padding);
                    break;
                case PAD_AFTER_SUFFIX:
                    result.append(padding);
                    break;
                }
                if (padPosition == PAD_BEFORE_PREFIX || padPosition == PAD_AFTER_PREFIX) {
                    fieldPosition.setBeginIndex(fieldPosition.getBeginIndex() + len);
                    fieldPosition.setEndIndex(fieldPosition.getEndIndex() + len);
                }
            }
        }
    }

    /**
     * Parses the given string, returning a <code>Number</code> object to represent the
     * parsed value. <code>Double</code> objects are returned to represent non-integral
     * values which cannot be stored in a <code>BigDecimal</code>. These are
     * <code>NaN</code>, infinity, -infinity, and -0.0. If {@link #isParseBigDecimal()} is
     * false (the default), all other values are returned as <code>Long</code>,
     * <code>BigInteger</code>, or <code>BigDecimal</code> values, in that order of
     * preference. If {@link #isParseBigDecimal()} is true, all other values are returned
     * as <code>BigDecimal</code> valuse. If the parse fails, null is returned.
     *
     * @param text the string to be parsed
     * @param parsePosition defines the position where parsing is to begin, and upon
     * return, the position where parsing left off. If the position has not changed upon
     * return, then parsing failed.
     * @return a <code>Number</code> object with the parsed value or
     * <code>null</code> if the parse failed
     */
    @Override
    public Number parse(String text, ParsePosition parsePosition) {
        return (Number) parse(text, parsePosition, null);
    }

    /**
     * Parses text from the given string as a CurrencyAmount. Unlike the parse() method,
     * this method will attempt to parse a generic currency name, searching for a match of
     * this object's locale's currency display names, or for a 3-letter ISO currency
     * code. This method will fail if this format is not a currency format, that is, if it
     * does not contain the currency pattern symbol (U+00A4) in its prefix or suffix.
     *
     * @param text the text to parse
     * @param pos input-output position; on input, the position within text to match; must
     *  have 0 &lt;= pos.getIndex() &lt; text.length(); on output, the position after the last
     *  matched character. If the parse fails, the position in unchanged upon output.
     * @return a CurrencyAmount, or null upon failure
     */
    @Override
    public CurrencyAmount parseCurrency(CharSequence text, ParsePosition pos) {
        Currency[] currency = new Currency[1];
        return (CurrencyAmount) parse(text.toString(), pos, currency);
    }

    /**
     * Parses the given text as either a Number or a CurrencyAmount.
     *
     * @param text the string to parse
     * @param parsePosition input-output position; on input, the position within text to
     * match; must have 0 <= pos.getIndex() < text.length(); on output, the position after
     * the last matched character. If the parse fails, the position in unchanged upon
     * output.
     * @param currency if non-null, a CurrencyAmount is parsed and returned; otherwise a
     * Number is parsed and returned
     * @return a Number or CurrencyAmount or null
     */
    private Object parse(String text, ParsePosition parsePosition, Currency[] currency) {
        int backup;
        int i = backup = parsePosition.getIndex();

        // Handle NaN as a special case:

        // Skip padding characters, if around prefix
        if (formatWidth > 0 &&
            (padPosition == PAD_BEFORE_PREFIX || padPosition == PAD_AFTER_PREFIX)) {
            i = skipPadding(text, i);
        }
        if (text.regionMatches(i, symbols.getNaN(), 0, symbols.getNaN().length())) {
            i += symbols.getNaN().length();
            // Skip padding characters, if around suffix
            if (formatWidth > 0 && (padPosition == PAD_BEFORE_SUFFIX ||
                                    padPosition == PAD_AFTER_SUFFIX)) {
                i = skipPadding(text, i);
            }
            parsePosition.setIndex(i);
            return new Double(Double.NaN);
        }

        // NaN parse failed; start over
        i = backup;

        boolean[] status = new boolean[STATUS_LENGTH];
        if (currencySignCount != CURRENCY_SIGN_COUNT_ZERO) {
            if (!parseForCurrency(text, parsePosition, currency, status)) {
                return null;
            }
        } else if (currency != null) {
            return null;
        } else {
            if (!subparse(text, parsePosition, digitList, status, currency, negPrefixPattern,
                          negSuffixPattern, posPrefixPattern, posSuffixPattern,
                          false, Currency.SYMBOL_NAME)) {
                parsePosition.setIndex(backup);
                return null;
            }
        }

        Number n = null;

        // Handle infinity
        if (status[STATUS_INFINITE]) {
            n = new Double(status[STATUS_POSITIVE] ? Double.POSITIVE_INFINITY :
                           Double.NEGATIVE_INFINITY);
        }

        // Handle underflow
        else if (status[STATUS_UNDERFLOW]) {
            n = status[STATUS_POSITIVE] ? new Double("0.0") : new Double("-0.0");
        }

        // Handle -0.0
        else if (!status[STATUS_POSITIVE] && digitList.isZero()) {
            n = new Double("-0.0");
        }

        else {
            // Do as much of the multiplier conversion as possible without
            // losing accuracy.
            int mult = multiplier; // Don't modify this.multiplier
            while (mult % 10 == 0) {
                --digitList.decimalAt;
                mult /= 10;
            }

            // Handle integral values
            if (!parseBigDecimal && mult == 1 && digitList.isIntegral()) {
                // hack quick long
                if (digitList.decimalAt < 12) { // quick check for long
                    long l = 0;
                    if (digitList.count > 0) {
                        int nx = 0;
                        while (nx < digitList.count) {
                            l = l * 10 + (char) digitList.digits[nx++] - '0';
                        }
                        while (nx++ < digitList.decimalAt) {
                            l *= 10;
                        }
                        if (!status[STATUS_POSITIVE]) {
                            l = -l;
                        }
                    }
                    n = Long.valueOf(l);
                } else {
                    BigInteger big = digitList.getBigInteger(status[STATUS_POSITIVE]);
                    n = (big.bitLength() < 64) ? (Number) Long.valueOf(big.longValue()) : (Number) big;
                }
            }
            // Handle non-integral values or the case where parseBigDecimal is set
            else {
                BigDecimal big = digitList.getBigDecimalICU(status[STATUS_POSITIVE]);
                n = big;
                if (mult != 1) {
                    n = big.divide(BigDecimal.valueOf(mult), mathContext);
                }
            }
        }

        // Assemble into CurrencyAmount if necessary
        return (currency != null) ? (Object) new CurrencyAmount(n, currency[0]) : (Object) n;
    }

    private boolean parseForCurrency(String text, ParsePosition parsePosition,
            Currency[] currency, boolean[] status) {
        int origPos = parsePosition.getIndex();
        if (!isReadyForParsing) {
            int savedCurrencySignCount = currencySignCount;
            setupCurrencyAffixForAllPatterns();
            // reset pattern back
            if (savedCurrencySignCount == CURRENCY_SIGN_COUNT_IN_PLURAL_FORMAT) {
                applyPatternWithoutExpandAffix(formatPattern, false);
            } else {
                applyPattern(formatPattern, false);
            }
            isReadyForParsing = true;
        }
        int maxPosIndex = origPos;
        int maxErrorPos = -1;
        boolean[] savedStatus = null;
        // First, parse against current pattern.
        // Since current pattern could be set by applyPattern(),
        // it could be an arbitrary pattern, and it may not be the one
        // defined in current locale.
        boolean[] tmpStatus = new boolean[STATUS_LENGTH];
        ParsePosition tmpPos = new ParsePosition(origPos);
        DigitList tmpDigitList = new DigitList();
        boolean found;
        if (style == NumberFormat.PLURALCURRENCYSTYLE) {
            found = subparse(text, tmpPos, tmpDigitList, tmpStatus, currency,
                             negPrefixPattern, negSuffixPattern, posPrefixPattern, posSuffixPattern,
                             true, Currency.LONG_NAME);
        } else {
            found = subparse(text, tmpPos, tmpDigitList, tmpStatus, currency,
                             negPrefixPattern, negSuffixPattern, posPrefixPattern, posSuffixPattern,
                             true, Currency.SYMBOL_NAME);
        }
        if (found) {
            if (tmpPos.getIndex() > maxPosIndex) {
                maxPosIndex = tmpPos.getIndex();
                savedStatus = tmpStatus;
                digitList = tmpDigitList;
            }
        } else {
            maxErrorPos = tmpPos.getErrorIndex();
        }
        // Then, parse against affix patterns.  Those are currency patterns and currency
        // plural patterns defined in the locale.
        for (AffixForCurrency affix : affixPatternsForCurrency) {
            tmpStatus = new boolean[STATUS_LENGTH];
            tmpPos = new ParsePosition(origPos);
            tmpDigitList = new DigitList();
            boolean result = subparse(text, tmpPos, tmpDigitList, tmpStatus, currency,
                                      affix.getNegPrefix(), affix.getNegSuffix(),
                                      affix.getPosPrefix(), affix.getPosSuffix(),
                                      true, affix.getPatternType());
            if (result) {
                found = true;
                if (tmpPos.getIndex() > maxPosIndex) {
                    maxPosIndex = tmpPos.getIndex();
                    savedStatus = tmpStatus;
                    digitList = tmpDigitList;
                }
            } else {
                maxErrorPos = (tmpPos.getErrorIndex() > maxErrorPos) ? tmpPos.getErrorIndex()
                    : maxErrorPos;
            }
        }
        // Finally, parse against simple affix to find the match.  For example, in
        // TestMonster suite, if the to-be-parsed text is "-\u00A40,00".
        // complexAffixCompare will not find match, since there is no ISO code matches
        // "\u00A4", and the parse stops at "\u00A4".  We will just use simple affix
        // comparison (look for exact match) to pass it.
        //
        // TODO: We should parse against simple affix first when
        // output currency is not requested. After the complex currency
        // parsing implementation was introduced, the default currency
        // instance parsing slowed down because of the new code flow.
        // I filed #10312 - Yoshito
        tmpStatus = new boolean[STATUS_LENGTH];
        tmpPos = new ParsePosition(origPos);
        tmpDigitList = new DigitList();

        // Disable complex currency parsing and try it again.
        boolean result = subparse(text, tmpPos, tmpDigitList, tmpStatus, currency,
                                  negativePrefix, negativeSuffix, positivePrefix, positiveSuffix,
                                  false /* disable complex currency parsing */, Currency.SYMBOL_NAME);
        if (result) {
            if (tmpPos.getIndex() > maxPosIndex) {
                maxPosIndex = tmpPos.getIndex();
                savedStatus = tmpStatus;
                digitList = tmpDigitList;
            }
            found = true;
        } else {
            maxErrorPos = (tmpPos.getErrorIndex() > maxErrorPos) ? tmpPos.getErrorIndex() :
                maxErrorPos;
        }

        if (!found) {
            // parsePosition.setIndex(origPos);
            parsePosition.setErrorIndex(maxErrorPos);
        } else {
            parsePosition.setIndex(maxPosIndex);
            parsePosition.setErrorIndex(-1);
            for (int index = 0; index < STATUS_LENGTH; ++index) {
                status[index] = savedStatus[index];
            }
        }
        return found;
    }

    // Get affix patterns used in locale's currency pattern (NumberPatterns[1]) and
    // currency plural pattern (CurrencyUnitPatterns).
    private void setupCurrencyAffixForAllPatterns() {
        if (currencyPluralInfo == null) {
            currencyPluralInfo = new CurrencyPluralInfo(symbols.getULocale());
        }
        affixPatternsForCurrency = new HashSet<AffixForCurrency>();

        // save the current pattern, since it will be changed by
        // applyPatternWithoutExpandAffix
        String savedFormatPattern = formatPattern;

        // CURRENCYSTYLE and ISOCURRENCYSTYLE should have the same prefix and suffix, so,
        // only need to save one of them.  Here, chose onlyApplyPatternWithoutExpandAffix
        // without saving the actualy pattern in 'pattern' data member.  TODO: is it uloc?
        applyPatternWithoutExpandAffix(getPattern(symbols.getULocale(), NumberFormat.CURRENCYSTYLE),
                                       false);
        AffixForCurrency affixes = new AffixForCurrency(
            negPrefixPattern, negSuffixPattern, posPrefixPattern, posSuffixPattern,
            Currency.SYMBOL_NAME);
        affixPatternsForCurrency.add(affixes);

        // add plural pattern
        Iterator<String> iter = currencyPluralInfo.pluralPatternIterator();
        Set<String> currencyUnitPatternSet = new HashSet<String>();
        while (iter.hasNext()) {
            String pluralCount = iter.next();
            String currencyPattern = currencyPluralInfo.getCurrencyPluralPattern(pluralCount);
            if (currencyPattern != null &&
                currencyUnitPatternSet.contains(currencyPattern) == false) {
                currencyUnitPatternSet.add(currencyPattern);
                applyPatternWithoutExpandAffix(currencyPattern, false);
                affixes = new AffixForCurrency(negPrefixPattern, negSuffixPattern, posPrefixPattern,
                                               posSuffixPattern, Currency.LONG_NAME);
                affixPatternsForCurrency.add(affixes);
            }
        }
        // reset pattern back
        formatPattern = savedFormatPattern;
    }

    // currency formatting style options
    private static final int CURRENCY_SIGN_COUNT_ZERO = 0;
    private static final int CURRENCY_SIGN_COUNT_IN_SYMBOL_FORMAT = 1;
    private static final int CURRENCY_SIGN_COUNT_IN_ISO_FORMAT = 2;
    private static final int CURRENCY_SIGN_COUNT_IN_PLURAL_FORMAT = 3;

    private static final int STATUS_INFINITE = 0;
    private static final int STATUS_POSITIVE = 1;
    private static final int STATUS_UNDERFLOW = 2;
    private static final int STATUS_LENGTH = 3;

    private static final UnicodeSet dotEquivalents = new UnicodeSet(
            //"[.\u2024\u3002\uFE12\uFE52\uFF0E\uFF61]"
            0x002E, 0x002E,
            0x2024, 0x2024,
            0x3002, 0x3002,
            0xFE12, 0xFE12,
            0xFE52, 0xFE52,
            0xFF0E, 0xFF0E,
            0xFF61, 0xFF61).freeze();

    private static final UnicodeSet commaEquivalents = new UnicodeSet(
            //"[,\u060C\u066B\u3001\uFE10\uFE11\uFE50\uFE51\uFF0C\uFF64]"
            0x002C, 0x002C,
            0x060C, 0x060C,
            0x066B, 0x066B,
            0x3001, 0x3001,
            0xFE10, 0xFE11,
            0xFE50, 0xFE51,
            0xFF0C, 0xFF0C,
            0xFF64, 0xFF64).freeze();

//    private static final UnicodeSet otherGroupingSeparators = new UnicodeSet(
//            //"[\\ '\u00A0\u066C\u2000-\u200A\u2018\u2019\u202F\u205F\u3000\uFF07]"
//            0x0020, 0x0020,
//            0x0027, 0x0027,
//            0x00A0, 0x00A0,
//            0x066C, 0x066C,
//            0x2000, 0x200A,
//            0x2018, 0x2019,
//            0x202F, 0x202F,
//            0x205F, 0x205F,
//            0x3000, 0x3000,
//            0xFF07, 0xFF07).freeze();

    private static final UnicodeSet strictDotEquivalents = new UnicodeSet(
            //"[.\u2024\uFE52\uFF0E\uFF61]"
            0x002E, 0x002E,
            0x2024, 0x2024,
            0xFE52, 0xFE52,
            0xFF0E, 0xFF0E,
            0xFF61, 0xFF61).freeze();

    private static final UnicodeSet strictCommaEquivalents = new UnicodeSet(
            //"[,\u066B\uFE10\uFE50\uFF0C]"
            0x002C, 0x002C,
            0x066B, 0x066B,
            0xFE10, 0xFE10,
            0xFE50, 0xFE50,
            0xFF0C, 0xFF0C).freeze();

//    private static final UnicodeSet strictOtherGroupingSeparators = new UnicodeSet(
//            //"[\\ '\u00A0\u066C\u2000-\u200A\u2018\u2019\u202F\u205F\u3000\uFF07]"
//            0x0020, 0x0020,
//            0x0027, 0x0027,
//            0x00A0, 0x00A0,
//            0x066C, 0x066C,
//            0x2000, 0x200A,
//            0x2018, 0x2019,
//            0x202F, 0x202F,
//            0x205F, 0x205F,
//            0x3000, 0x3000,
//            0xFF07, 0xFF07).freeze();

    private static final UnicodeSet defaultGroupingSeparators =
        // new UnicodeSet(dotEquivalents).addAll(commaEquivalents)
        //     .addAll(otherGroupingSeparators).freeze();
        new UnicodeSet(
                0x0020, 0x0020,
                0x0027, 0x0027,
                0x002C, 0x002C,
                0x002E, 0x002E,
                0x00A0, 0x00A0,
                0x060C, 0x060C,
                0x066B, 0x066C,
                0x2000, 0x200A,
                0x2018, 0x2019,
                0x2024, 0x2024,
                0x202F, 0x202F,
                0x205F, 0x205F,
                0x3000, 0x3002,
                0xFE10, 0xFE12,
                0xFE50, 0xFE52,
                0xFF07, 0xFF07,
                0xFF0C, 0xFF0C,
                0xFF0E, 0xFF0E,
                0xFF61, 0xFF61,
                0xFF64, 0xFF64).freeze();

    private static final UnicodeSet strictDefaultGroupingSeparators =
        // new UnicodeSet(strictDotEquivalents).addAll(strictCommaEquivalents)
        //     .addAll(strictOtherGroupingSeparators).freeze();
        new UnicodeSet(
                0x0020, 0x0020,
                0x0027, 0x0027,
                0x002C, 0x002C,
                0x002E, 0x002E,
                0x00A0, 0x00A0,
                0x066B, 0x066C,
                0x2000, 0x200A,
                0x2018, 0x2019,
                0x2024, 0x2024,
                0x202F, 0x202F,
                0x205F, 0x205F,
                0x3000, 0x3000,
                0xFE10, 0xFE10,
                0xFE50, 0xFE50,
                0xFE52, 0xFE52,
                0xFF07, 0xFF07,
                0xFF0C, 0xFF0C,
                0xFF0E, 0xFF0E,
                0xFF61, 0xFF61).freeze();

    static final UnicodeSet minusSigns =
        new UnicodeSet(
                0x002D, 0x002D,
                0x207B, 0x207B,
                0x208B, 0x208B,
                0x2212, 0x2212,
                0x2796, 0x2796,
                0xFE63, 0xFE63,
                0xFF0D, 0xFF0D).freeze();

    static final UnicodeSet plusSigns =
            new UnicodeSet(
                0x002B, 0x002B,
                0x207A, 0x207A,
                0x208A, 0x208A,
                0x2795, 0x2795,
                0xFB29, 0xFB29,
                0xFE62, 0xFE62,
                0xFF0B, 0xFF0B).freeze();

    // equivalent grouping and decimal support
    static final boolean skipExtendedSeparatorParsing = ICUConfig.get(
        "android.icu.text.DecimalFormat.SkipExtendedSeparatorParsing", "false")
        .equals("true");

    // allow control of requiring a matching decimal point when parsing
    boolean parseRequireDecimalPoint = false;

    // When parsing a number with big exponential value, it requires to transform the
    // value into a string representation to construct BigInteger instance.  We want to
    // set the maximum size because it can easily trigger OutOfMemoryException.
    // PARSE_MAX_EXPONENT is currently set to 1000 (See getParseMaxDigits()),
    // which is much bigger than MAX_VALUE of Double ( See the problem reported by ticket#5698
    private int PARSE_MAX_EXPONENT = 1000;

    /**
     * Parses the given text into a number. The text is parsed beginning at parsePosition,
     * until an unparseable character is seen.
     *
     * @param text the string to parse.
     * @param parsePosition the position at which to being parsing. Upon return, the first
     * unparseable character.
     * @param digits the DigitList to set to the parsed value.
     * @param status Upon return contains boolean status flags indicating whether the
     * value was infinite and whether it was positive.
     * @param currency return value for parsed currency, for generic currency parsing
     * mode, or null for normal parsing. In generic currency parsing mode, any currency is
     * parsed, not just the currency that this formatter is set to.
     * @param negPrefix negative prefix pattern
     * @param negSuffix negative suffix pattern
     * @param posPrefix positive prefix pattern
     * @param negSuffix negative suffix pattern
     * @param parseComplexCurrency whether it is complex currency parsing or not.
     * @param type type of currency to parse against, LONG_NAME only or not.
     */
    private final boolean subparse(
        String text, ParsePosition parsePosition, DigitList digits,
        boolean status[], Currency currency[], String negPrefix, String negSuffix, String posPrefix,
        String posSuffix, boolean parseComplexCurrency, int type) {

        int position = parsePosition.getIndex();
        int oldStart = parsePosition.getIndex();

        // Match padding before prefix
        if (formatWidth > 0 && padPosition == PAD_BEFORE_PREFIX) {
            position = skipPadding(text, position);
        }

        // Match positive and negative prefixes; prefer longest match.
        int posMatch = compareAffix(text, position, false, true, posPrefix, parseComplexCurrency, type, currency);
        int negMatch = compareAffix(text, position, true, true, negPrefix, parseComplexCurrency, type, currency);
        if (posMatch >= 0 && negMatch >= 0) {
            if (posMatch > negMatch) {
                negMatch = -1;
            } else if (negMatch > posMatch) {
                posMatch = -1;
            }
        }
        if (posMatch >= 0) {
            position += posMatch;
        } else if (negMatch >= 0) {
            position += negMatch;
        } else {
            parsePosition.setErrorIndex(position);
            return false;
        }

        // Match padding after prefix
        if (formatWidth > 0 && padPosition == PAD_AFTER_PREFIX) {
            position = skipPadding(text, position);
        }

        // process digits or Inf, find decimal position
        status[STATUS_INFINITE] = false;
        if (text.regionMatches(position, symbols.getInfinity(), 0,
                                              symbols.getInfinity().length())) {
            position += symbols.getInfinity().length();
            status[STATUS_INFINITE] = true;
        } else {
            // We now have a string of digits, possibly with grouping symbols, and decimal
            // points. We want to process these into a DigitList.  We don't want to put a
            // bunch of leading zeros into the DigitList though, so we keep track of the
            // location of the decimal point, put only significant digits into the
            // DigitList, and adjust the exponent as needed.

            digits.decimalAt = digits.count = 0;
            String decimal = (currencySignCount == CURRENCY_SIGN_COUNT_ZERO) ?
                    symbols.getDecimalSeparatorString() : symbols.getMonetaryDecimalSeparatorString();
            String grouping = (currencySignCount == CURRENCY_SIGN_COUNT_ZERO) ?
                    symbols.getGroupingSeparatorString() : symbols.getMonetaryGroupingSeparatorString();

            String exponentSep = symbols.getExponentSeparator();
            boolean sawDecimal = false;
            boolean sawGrouping = false;
            boolean sawDigit = false;
            long exponent = 0; // Set to the exponent value, if any

            // strict parsing
            boolean strictParse = isParseStrict();
            boolean strictFail = false; // did we exit with a strict parse failure?
            int lastGroup = -1; // where did we last see a grouping separator?
            int groupedDigitCount = 0;  // tracking count of digits delimited by grouping separator
            int gs2 = groupingSize2 == 0 ? groupingSize : groupingSize2;

            UnicodeSet decimalEquiv = skipExtendedSeparatorParsing ? UnicodeSet.EMPTY :
                getEquivalentDecimals(decimal, strictParse);
            UnicodeSet groupEquiv = skipExtendedSeparatorParsing ? UnicodeSet.EMPTY :
                (strictParse ? strictDefaultGroupingSeparators : defaultGroupingSeparators);

            // We have to track digitCount ourselves, because digits.count will pin when
            // the maximum allowable digits is reached.
            int digitCount = 0;

            int backup = -1;    // used for preserving the last confirmed position
            int[] parsedDigit = {-1};   // allocates int[1] for parsing a single digit

            while (position < text.length()) {
                // Check if the sequence at the current position matches a decimal digit
                int matchLen = matchesDigit(text, position, parsedDigit);
                if (matchLen > 0) {
                    // matched a digit
                    // Cancel out backup setting (see grouping handler below)
                    if (backup != -1) {
                        if (strictParse) {
                            // comma followed by digit, so group before comma is a secondary
                            // group. If there was a group separator before that, the group
                            // must == the secondary group length, else it can be <= the the
                            // secondary group length.
                            if ((lastGroup != -1 && groupedDigitCount != gs2)
                                    || (lastGroup == -1 && groupedDigitCount > gs2)) {
                                strictFail = true;
                                break;
                            }
                        }
                        lastGroup = backup;
                        groupedDigitCount = 0;
                    }

                    groupedDigitCount++;
                    position += matchLen;
                    backup = -1;
                    sawDigit = true;
                    if (parsedDigit[0] == 0 && digits.count == 0) {
                        // Handle leading zeros
                        if (!sawDecimal) {
                            // Ignore leading zeros in integer part of number.
                            continue;
                        }
                        // If we have seen the decimal, but no significant digits yet,
                        // then we account for leading zeros by decrementing the
                        // digits.decimalAt into negative values.
                        --digits.decimalAt;
                    } else {
                        ++digitCount;
                        digits.append((char) (parsedDigit[0] + '0'));
                    }
                    continue;
                }

                // Check if the sequence at the current position matches locale's decimal separator
                int decimalStrLen = decimal.length();
                if (text.regionMatches(position, decimal, 0, decimalStrLen)) {
                    // matched a decimal separator
                    if (strictParse) {
                        if (backup != -1 ||
                            (lastGroup != -1 && groupedDigitCount != groupingSize)) {
                            strictFail = true;
                            break;
                        }
                    }

                    // If we're only parsing integers, or if we ALREADY saw the decimal,
                    // then don't parse this one.
                    if (isParseIntegerOnly() || sawDecimal) {
                        break;
                    }

                    digits.decimalAt = digitCount; // Not digits.count!
                    sawDecimal = true;
                    position += decimalStrLen;
                    continue;
                }

                if (isGroupingUsed()) {
                    // Check if the sequence at the current position matches locale's grouping separator
                    int groupingStrLen = grouping.length();
                    if (text.regionMatches(position, grouping, 0, groupingStrLen)) {
                        if (sawDecimal) {
                            break;
                        }

                        if (strictParse) {
                            if ((!sawDigit || backup != -1)) {
                                // leading group, or two group separators in a row
                                strictFail = true;
                                break;
                            }
                        }

                        // Ignore grouping characters, if we are using them, but require that
                        // they be followed by a digit. Otherwise we backup and reprocess
                        // them.
                        backup = position;
                        position += groupingStrLen;
                        sawGrouping = true;
                        continue;
                    }
                }

                // Check if the code point at the current position matches one of decimal/grouping equivalent group chars
                int cp = text.codePointAt(position);
                if (!sawDecimal && decimalEquiv.contains(cp)) {
                    // matched a decimal separator
                    if (strictParse) {
                        if (backup != -1 ||
                            (lastGroup != -1 && groupedDigitCount != groupingSize)) {
                            strictFail = true;
                            break;
                        }
                    }

                    // If we're only parsing integers, or if we ALREADY saw the decimal,
                    // then don't parse this one.
                    if (isParseIntegerOnly()) {
                        break;
                    }

                    digits.decimalAt = digitCount; // Not digits.count!

                    // Once we see a decimal separator character, we only accept that
                    // decimal separator character from then on.
                    decimal = String.valueOf(Character.toChars(cp));

                    sawDecimal = true;
                    position += Character.charCount(cp);
                    continue;
                }

                if (isGroupingUsed() && !sawGrouping && groupEquiv.contains(cp)) {
                    // matched a grouping separator
                    if (sawDecimal) {
                        break;
                    }

                    if (strictParse) {
                        if ((!sawDigit || backup != -1)) {
                            // leading group, or two group separators in a row
                            strictFail = true;
                            break;
                        }
                    }

                    // Once we see a grouping character, we only accept that grouping
                    // character from then on.
                    grouping = String.valueOf(Character.toChars(cp));

                    // Ignore grouping characters, if we are using them, but require that
                    // they be followed by a digit. Otherwise we backup and reprocess
                    // them.
                    backup = position;
                    position += Character.charCount(cp);
                    sawGrouping = true;
                    continue;
                }

                // Check if the sequence at the current position matches locale's exponent separator
                int exponentSepStrLen = exponentSep.length();
                if (text.regionMatches(true, position, exponentSep, 0, exponentSepStrLen)) {
                    // parse sign, if present
                    boolean negExp = false;
                    int pos = position + exponentSep.length();
                    if (pos < text.length()) {
                        String plusSign = symbols.getPlusSignString();
                        String minusSign = symbols.getMinusSignString();
                        if (text.regionMatches(pos, plusSign, 0, plusSign.length())) {
                            pos += plusSign.length();
                        } else if (text.regionMatches(pos, minusSign, 0, minusSign.length())) {
                            pos += minusSign.length();
                            negExp = true;
                        }
                    }

                    DigitList exponentDigits = new DigitList();
                    exponentDigits.count = 0;
                    while (pos < text.length()) {
                        int digitMatchLen = matchesDigit(text, pos, parsedDigit);
                        if (digitMatchLen > 0) {
                            exponentDigits.append((char) (parsedDigit[0] + '0'));
                            pos += digitMatchLen;
                        } else {
                            break;
                        }
                    }

                    if (exponentDigits.count > 0) {
                        // defer strict parse until we know we have a bona-fide exponent
                        if (strictParse && sawGrouping) {
                            strictFail = true;
                            break;
                        }

                        // Quick overflow check for exponential part.  Actual limit check
                        // will be done later in this code.
                        if (exponentDigits.count > 10 /* maximum decimal digits for int */) {
                            if (negExp) {
                                // set underflow flag
                                status[STATUS_UNDERFLOW] = true;
                            } else {
                                // set infinite flag
                                status[STATUS_INFINITE] = true;
                            }
                        } else {
                            exponentDigits.decimalAt = exponentDigits.count;
                            exponent = exponentDigits.getLong();
                            if (negExp) {
                                exponent = -exponent;
                            }
                        }
                        position = pos; // Advance past the exponent
                    }

                    break; // Whether we fail or succeed, we exit this loop
                }

                // All other cases, stop parsing
                break;
            }

            if (digits.decimalAt == 0 && isDecimalPatternMatchRequired()) {
                if (this.formatPattern.indexOf(decimal) != -1) {
                    parsePosition.setIndex(oldStart);
                    parsePosition.setErrorIndex(position);
                    return false;
                }
            }

            if (backup != -1)
                position = backup;

            // If there was no decimal point we have an integer
            if (!sawDecimal) {
                digits.decimalAt = digitCount; // Not digits.count!
            }

            // check for strict parse errors
            if (strictParse && !sawDecimal) {
                if (lastGroup != -1 && groupedDigitCount != groupingSize) {
                    strictFail = true;
                }
            }
            if (strictFail) {
                // only set with strictParse and a leading zero error leading zeros are an
                // error with strict parsing except immediately before nondigit (except
                // group separator followed by digit), or end of text.

                parsePosition.setIndex(oldStart);
                parsePosition.setErrorIndex(position);
                return false;
            }

            // Adjust for exponent, if any
            exponent += digits.decimalAt;
            if (exponent < -getParseMaxDigits()) {
                status[STATUS_UNDERFLOW] = true;
            } else if (exponent > getParseMaxDigits()) {
                status[STATUS_INFINITE] = true;
            } else {
                digits.decimalAt = (int) exponent;
            }

            // If none of the text string was recognized. For example, parse "x" with
            // pattern "#0.00" (return index and error index both 0) parse "$" with
            // pattern "$#0.00". (return index 0 and error index 1).
            if (!sawDigit && digitCount == 0) {
                parsePosition.setIndex(oldStart);
                parsePosition.setErrorIndex(oldStart);
                return false;
            }
        }

        // Match padding before suffix
        if (formatWidth > 0 && padPosition == PAD_BEFORE_SUFFIX) {
            position = skipPadding(text, position);
        }

        // Match positive and negative suffixes; prefer longest match.
        if (posMatch >= 0) {
            posMatch = compareAffix(text, position, false, false, posSuffix, parseComplexCurrency, type, currency);
        }
        if (negMatch >= 0) {
            negMatch = compareAffix(text, position, true, false, negSuffix, parseComplexCurrency, type, currency);
        }
        if (posMatch >= 0 && negMatch >= 0) {
            if (posMatch > negMatch) {
                negMatch = -1;
            } else if (negMatch > posMatch) {
                posMatch = -1;
            }
        }

        // Fail if neither or both
        if ((posMatch >= 0) == (negMatch >= 0)) {
            parsePosition.setErrorIndex(position);
            return false;
        }

        position += (posMatch >= 0 ? posMatch : negMatch);

        // Match padding after suffix
        if (formatWidth > 0 && padPosition == PAD_AFTER_SUFFIX) {
            position = skipPadding(text, position);
        }

        parsePosition.setIndex(position);

        status[STATUS_POSITIVE] = (posMatch >= 0);

        if (parsePosition.getIndex() == oldStart) {
            parsePosition.setErrorIndex(position);
            return false;
        }
        return true;
    }

    /**
     * Check if the substring at the specified position matches a decimal digit.
     * If matched, this method sets the decimal value to <code>decVal</code> and
     * returns matched length.
     *
     * @param str       The input string
     * @param start     The start index
     * @param decVal    Receives decimal value
     * @return          Length of match, or 0 if the sequence at the position is not
     *                  a decimal digit.
     */
    private int matchesDigit(String str, int start, int[] decVal) {
        String[] localeDigits = symbols.getDigitStringsLocal();

        // Check if the sequence at the current position matches locale digits.
        for (int i = 0; i < 10; i++) {
            int digitStrLen = localeDigits[i].length();
            if (str.regionMatches(start, localeDigits[i], 0, digitStrLen)) {
                decVal[0] = i;
                return digitStrLen;
            }
        }

        // If no locale digit match, then check if this is a Unicode digit
        int cp = str.codePointAt(start);
        decVal[0] = UCharacter.digit(cp, 10);
        if (decVal[0] >= 0) {
            return Character.charCount(cp);
        }

        return 0;
    }

    /**
     * Returns a set of characters equivalent to the given desimal separator used for
     * parsing number.  This method may return an empty set.
     */
    private UnicodeSet getEquivalentDecimals(String decimal, boolean strictParse) {
        UnicodeSet equivSet = UnicodeSet.EMPTY;
        if (strictParse) {
            if (strictDotEquivalents.contains(decimal)) {
                equivSet = strictDotEquivalents;
            } else if (strictCommaEquivalents.contains(decimal)) {
                equivSet = strictCommaEquivalents;
            }
        } else {
            if (dotEquivalents.contains(decimal)) {
                equivSet = dotEquivalents;
            } else if (commaEquivalents.contains(decimal)) {
                equivSet = commaEquivalents;
            }
        }
        return equivSet;
    }

    /**
     * Starting at position, advance past a run of pad characters, if any. Return the
     * index of the first character after position that is not a pad character. Result is
     * >= position.
     */
    private final int skipPadding(String text, int position) {
        while (position < text.length() && text.charAt(position) == pad) {
            ++position;
        }
        return position;
    }

    /**
     * Returns the length matched by the given affix, or -1 if none. Runs of white space
     * in the affix, match runs of white space in the input. Pattern white space and input
     * white space are determined differently; see code.
     *
     * @param text input text
     * @param pos offset into input at which to begin matching
     * @param isNegative
     * @param isPrefix
     * @param affixPat affix pattern used for currency affix comparison
     * @param complexCurrencyParsing whether it is currency parsing or not
     * @param type compare against currency type, LONG_NAME only or not.
     * @param currency return value for parsed currency, for generic currency parsing
     * mode, or null for normal parsing.  In generic currency parsing mode, any currency
     * is parsed, not just the currency that this formatter is set to.
     * @return length of input that matches, or -1 if match failure
     */
    private int compareAffix(String text, int pos, boolean isNegative, boolean isPrefix,
                             String affixPat, boolean complexCurrencyParsing, int type, Currency[] currency) {
        if (currency != null || currencyChoice != null || (currencySignCount != CURRENCY_SIGN_COUNT_ZERO && complexCurrencyParsing)) {
            return compareComplexAffix(affixPat, text, pos, type, currency);
        }
        if (isPrefix) {
            return compareSimpleAffix(isNegative ? negativePrefix : positivePrefix, text, pos);
        } else {
            return compareSimpleAffix(isNegative ? negativeSuffix : positiveSuffix, text, pos);
        }

    }

    /**
     * Check for bidi marks: LRM, RLM, ALM
     */
    private static boolean isBidiMark(int c) {
        return (c==0x200E || c==0x200F || c==0x061C);
    }

    /**
     * Remove bidi marks from affix
     */
    private static String trimMarksFromAffix(String affix) {
        boolean hasBidiMark = false;
        int idx = 0;
        for (; idx < affix.length(); idx++) {
            if (isBidiMark(affix.charAt(idx))) {
                hasBidiMark = true;
                break;
            }
        }
        if (!hasBidiMark) {
            return affix;
        }

        StringBuilder buf = new StringBuilder();
        buf.append(affix, 0, idx);
        idx++;  // skip the first Bidi mark
        for (; idx < affix.length(); idx++) {
            char c = affix.charAt(idx);
            if (!isBidiMark(c)) {
                buf.append(c);
            }
        }

        return buf.toString();
    }

    /**
     * Return the length matched by the given affix, or -1 if none. Runs of white space in
     * the affix, match runs of white space in the input. Pattern white space and input
     * white space are determined differently; see code.
     *
     * @param affix pattern string, taken as a literal
     * @param input input text
     * @param pos offset into input at which to begin matching
     * @return length of input that matches, or -1 if match failure
     */
    private static int compareSimpleAffix(String affix, String input, int pos) {
        int start = pos;
        // Affixes here might consist of sign, currency symbol and related spacing, etc.
        // For more efficiency we should keep lazily-created trimmed affixes around in
        // instance variables instead of trimming each time they are used (the next step).
        String trimmedAffix = (affix.length() > 1)? trimMarksFromAffix(affix): affix;
        for (int i = 0; i < trimmedAffix.length();) {
            int c = UTF16.charAt(trimmedAffix, i);
            int len = UTF16.getCharCount(c);
            if (PatternProps.isWhiteSpace(c)) {
                // We may have a pattern like: \u200F and input text like: \u200F Note
                // that U+200F and U+0020 are Pattern_White_Space but only U+0020 is
                // UWhiteSpace. So we have to first do a direct match of the run of RULE
                // whitespace in the pattern, then match any extra characters.
                boolean literalMatch = false;
                while (pos < input.length()) {
                    int ic = UTF16.charAt(input, pos);
                    if (ic == c) {
                        literalMatch = true;
                        i += len;
                        pos += len;
                        if (i == trimmedAffix.length()) {
                            break;
                        }
                        c = UTF16.charAt(trimmedAffix, i);
                        len = UTF16.getCharCount(c);
                        if (!PatternProps.isWhiteSpace(c)) {
                            break;
                        }
                    } else if (isBidiMark(ic)) {
                        pos++; // just skip over this input text
                    } else {
                        break;
                    }
                }

                // Advance over run in trimmedAffix
                i = skipPatternWhiteSpace(trimmedAffix, i);

                // Advance over run in input text. Must see at least one white space char
                // in input, unless we've already matched some characters literally.
                int s = pos;
                pos = skipUWhiteSpace(input, pos);
                if (pos == s && !literalMatch) {
                    return -1;
                }
                // If we skip UWhiteSpace in the input text, we need to skip it in the
                // pattern.  Otherwise, the previous lines may have skipped over text
                // (such as U+00A0) that is also in the trimmedAffix.
                i = skipUWhiteSpace(trimmedAffix, i);
            } else {
                boolean match = false;
                while (pos < input.length()) {
                    int ic = UTF16.charAt(input, pos);
                    if (!match && equalWithSignCompatibility(ic, c)) {
                        i += len;
                        pos += len;
                        match = true;
                    } else if (isBidiMark(ic)) {
                        pos++; // just skip over this input text
                    } else {
                        break;
                    }
                }
                if (!match) {
                    return -1;
                }
            }
        }
        return pos - start;
    }

    private static boolean equalWithSignCompatibility(int lhs, int rhs) {
        return lhs == rhs
                || (minusSigns.contains(lhs) && minusSigns.contains(rhs))
                || (plusSigns.contains(lhs) && plusSigns.contains(rhs));
    }

    /**
     * Skips over a run of zero or more Pattern_White_Space characters at pos in text.
     */
    private static int skipPatternWhiteSpace(String text, int pos) {
        while (pos < text.length()) {
            int c = UTF16.charAt(text, pos);
            if (!PatternProps.isWhiteSpace(c)) {
                break;
            }
            pos += UTF16.getCharCount(c);
        }
        return pos;
    }

    /**
     * Skips over a run of zero or more isUWhiteSpace() characters at pos in text.
     */
    private static int skipUWhiteSpace(String text, int pos) {
        while (pos < text.length()) {
            int c = UTF16.charAt(text, pos);
            if (!UCharacter.isUWhiteSpace(c)) {
                break;
            }
            pos += UTF16.getCharCount(c);
        }
        return pos;
    }

     /**
     * Skips over a run of zero or more bidi marks at pos in text.
     */
    private static int skipBidiMarks(String text, int pos) {
        while (pos < text.length()) {
            int c = UTF16.charAt(text, pos);
            if (!isBidiMark(c)) {
                break;
            }
            pos += UTF16.getCharCount(c);
        }
        return pos;
    }

   /**
     * Returns the length matched by the given affix, or -1 if none.
     *
     * @param affixPat pattern string
     * @param text input text
     * @param pos offset into input at which to begin matching
     * @param type parse against currency type, LONG_NAME only or not.
     * @param currency return value for parsed currency, for generic
     * currency parsing mode, or null for normal parsing.  In generic
     * currency parsing mode, any currency is parsed, not just the
     * currency that this formatter is set to.
     * @return position after the matched text, or -1 if match failure
     */
    private int compareComplexAffix(String affixPat, String text, int pos, int type,
                                    Currency[] currency) {
        int start = pos;
        for (int i = 0; i < affixPat.length() && pos >= 0;) {
            char c = affixPat.charAt(i++);
            if (c == QUOTE) {
                for (;;) {
                    int j = affixPat.indexOf(QUOTE, i);
                    if (j == i) {
                        pos = match(text, pos, QUOTE);
                        i = j + 1;
                        break;
                    } else if (j > i) {
                        pos = match(text, pos, affixPat.substring(i, j));
                        i = j + 1;
                        if (i < affixPat.length() && affixPat.charAt(i) == QUOTE) {
                            pos = match(text, pos, QUOTE);
                            ++i;
                            // loop again
                        } else {
                            break;
                        }
                    } else {
                        // Unterminated quote; should be caught by apply
                        // pattern.
                        throw new RuntimeException();
                    }
                }
                continue;
            }

            String affix = null;

            switch (c) {
            case CURRENCY_SIGN:
                // since the currency names in choice format is saved the same way as
                // other currency names, do not need to do currency choice parsing here.
                // the general currency parsing parse against all names, including names
                // in choice format.  assert(currency != null || (getCurrency() != null &&
                // currencyChoice != null));
                boolean intl = i < affixPat.length() && affixPat.charAt(i) == CURRENCY_SIGN;
                if (intl) {
                    ++i;
                }
                boolean plural = i < affixPat.length() && affixPat.charAt(i) == CURRENCY_SIGN;
                if (plural) {
                    ++i;
                    intl = false;
                }
                // Parse generic currency -- anything for which we have a display name, or
                // any 3-letter ISO code.  Try to parse display name for our locale; first
                // determine our locale.  TODO: use locale in CurrencyPluralInfo
                ULocale uloc = getLocale(ULocale.VALID_LOCALE);
                if (uloc == null) {
                    // applyPattern has been called; use the symbols
                    uloc = symbols.getLocale(ULocale.VALID_LOCALE);
                }
                // Delegate parse of display name => ISO code to Currency
                ParsePosition ppos = new ParsePosition(pos);
                // using Currency.parse to handle mixed style parsing.
                String iso = Currency.parse(uloc, text, type, ppos);

                // If parse succeeds, populate currency[0]
                if (iso != null) {
                    if (currency != null) {
                        currency[0] = Currency.getInstance(iso);
                    } else {
                        // The formatter is currency-style but the client has not requested
                        // the value of the parsed currency. In this case, if that value does
                        // not match the formatter's current value, then the parse fails.
                        Currency effectiveCurr = getEffectiveCurrency();
                        if (iso.compareTo(effectiveCurr.getCurrencyCode()) != 0) {
                            pos = -1;
                            continue;
                        }
                    }
                    pos = ppos.getIndex();
                } else {
                    pos = -1;
                }
                continue;
            case PATTERN_PERCENT:
                affix = symbols.getPercentString();
                break;
            case PATTERN_PER_MILLE:
                affix = symbols.getPerMillString();
                break;
            case PATTERN_PLUS_SIGN:
                affix = symbols.getPlusSignString();
                break;
            case PATTERN_MINUS_SIGN:
                affix = symbols.getMinusSignString();
                break;
            default:
                // fall through to affix != null test, which will fail
                break;
            }

            if (affix != null) {
                pos = match(text, pos, affix);
                continue;
            }

            pos = match(text, pos, c);
            if (PatternProps.isWhiteSpace(c)) {
                i = skipPatternWhiteSpace(affixPat, i);
            }
        }

        return pos - start;
    }

    /**
     * Matches a single character at text[pos] and return the index of the next character
     * upon success. Return -1 on failure. If ch is a Pattern_White_Space then match a run of
     * white space in text.
     */
    static final int match(String text, int pos, int ch) {
        if (pos < 0 || pos >= text.length()) {
            return -1;
        }
        pos = skipBidiMarks(text, pos);
        if (PatternProps.isWhiteSpace(ch)) {
            // Advance over run of white space in input text
            // Must see at least one white space char in input
            int s = pos;
            pos = skipPatternWhiteSpace(text, pos);
            if (pos == s) {
                return -1;
            }
            return pos;
        }
        if (pos >= text.length() || UTF16.charAt(text, pos) != ch) {
            return -1;
        }
        pos = skipBidiMarks(text, pos + UTF16.getCharCount(ch));
        return pos;
    }

    /**
     * Matches a string at text[pos] and return the index of the next character upon
     * success. Return -1 on failure. Match a run of white space in str with a run of
     * white space in text.
     */
    static final int match(String text, int pos, String str) {
        for (int i = 0; i < str.length() && pos >= 0;) {
            int ch = UTF16.charAt(str, i);
            i += UTF16.getCharCount(ch);
            if (isBidiMark(ch)) {
                continue;
            }
            pos = match(text, pos, ch);
            if (PatternProps.isWhiteSpace(ch)) {
                i = skipPatternWhiteSpace(str, i);
            }
        }
        return pos;
    }

    /**
     * Returns a copy of the decimal format symbols used by this format.
     *
     * @return desired DecimalFormatSymbols
     * @see DecimalFormatSymbols
     */
    public DecimalFormatSymbols getDecimalFormatSymbols() {
        try {
            // don't allow multiple references
            return (DecimalFormatSymbols) symbols.clone();
        } catch (Exception foo) {
            return null; // should never happen
        }
    }

    /**
     * Sets the decimal format symbols used by this format. The format uses a copy of the
     * provided symbols.
     *
     * @param newSymbols desired DecimalFormatSymbols
     * @see DecimalFormatSymbols
     */
    public void setDecimalFormatSymbols(DecimalFormatSymbols newSymbols) {
        symbols = (DecimalFormatSymbols) newSymbols.clone();
        setCurrencyForSymbols();
        expandAffixes(null);
    }

    /**
     * Update the currency object to match the symbols. This method is used only when the
     * caller has passed in a symbols object that may not be the default object for its
     * locale.
     */
    private void setCurrencyForSymbols() {

        // Bug 4212072 Update the affix strings according to symbols in order to keep the
        // affix strings up to date.  [Richard/GCL]

        // With the introduction of the Currency object, the currency symbols in the DFS
        // object are ignored. For backward compatibility, we check any explicitly set DFS
        // object. If it is a default symbols object for its locale, we change the
        // currency object to one for that locale. If it is custom, we set the currency to
        // null.
        DecimalFormatSymbols def = new DecimalFormatSymbols(symbols.getULocale());

        if (symbols.getCurrencySymbol().equals(def.getCurrencySymbol())
                && symbols.getInternationalCurrencySymbol()
                       .equals(def.getInternationalCurrencySymbol())) {
            setCurrency(Currency.getInstance(symbols.getULocale()));
        } else {
            setCurrency(null);
        }
    }

    /**
     * Returns the positive prefix.
     *
     * <p>Examples: +123, $123, sFr123
     * @return the prefix
     */
    public String getPositivePrefix() {
        return positivePrefix;
    }

    /**
     * Sets the positive prefix.
     *
     * <p>Examples: +123, $123, sFr123
     * @param newValue the prefix
     */
    public void setPositivePrefix(String newValue) {
        positivePrefix = newValue;
        posPrefixPattern = null;
    }

    /**
     * Returns the negative prefix.
     *
     * <p>Examples: -123, ($123) (with negative suffix), sFr-123
     *
     * @return the prefix
     */
    public String getNegativePrefix() {
        return negativePrefix;
    }

    /**
     * Sets the negative prefix.
     *
     * <p>Examples: -123, ($123) (with negative suffix), sFr-123
     * @param newValue the prefix
     */
    public void setNegativePrefix(String newValue) {
        negativePrefix = newValue;
        negPrefixPattern = null;
    }

    /**
     * Returns the positive suffix.
     *
     * <p>Example: 123%
     *
     * @return the suffix
     */
    public String getPositiveSuffix() {
        return positiveSuffix;
    }

    /**
     * Sets the positive suffix.
     *
     * <p>Example: 123%
     * @param newValue the suffix
     */
    public void setPositiveSuffix(String newValue) {
        positiveSuffix = newValue;
        posSuffixPattern = null;
    }

    /**
     * Returns the negative suffix.
     *
     * <p>Examples: -123%, ($123) (with positive suffixes)
     *
     * @return the suffix
     */
    public String getNegativeSuffix() {
        return negativeSuffix;
    }

    /**
     * Sets the positive suffix.
     *
     * <p>Examples: 123%
     * @param newValue the suffix
     */
    public void setNegativeSuffix(String newValue) {
        negativeSuffix = newValue;
        negSuffixPattern = null;
    }

    /**
     * Returns the multiplier for use in percent, permill, etc. For a percentage, set the
     * suffixes to have "%" and the multiplier to be 100. (For Arabic, use arabic percent
     * symbol). For a permill, set the suffixes to have "\u2031" and the multiplier to be
     * 1000.
     *
     * <p>Examples: with 100, 1.23 -&gt; "123", and "123" -&gt; 1.23
     *
     * @return the multiplier
     */
    public int getMultiplier() {
        return multiplier;
    }

    /**
     * Sets the multiplier for use in percent, permill, etc. For a percentage, set the
     * suffixes to have "%" and the multiplier to be 100. (For Arabic, use arabic percent
     * symbol). For a permill, set the suffixes to have "\u2031" and the multiplier to be
     * 1000.
     *
     * <p>Examples: with 100, 1.23 -&gt; "123", and "123" -&gt; 1.23
     *
     * @param newValue the multiplier
     */
    public void setMultiplier(int newValue) {
        if (newValue == 0) {
            throw new IllegalArgumentException("Bad multiplier: " + newValue);
        }
        multiplier = newValue;
    }

    /**
     * <strong>[icu]</strong> Returns the rounding increment.
     *
     * @return A positive rounding increment, or <code>null</code> if a custom rounding
     * increment is not in effect.
     * @see #setRoundingIncrement
     * @see #getRoundingMode
     * @see #setRoundingMode
     */
    public java.math.BigDecimal getRoundingIncrement() {
        if (roundingIncrementICU == null)
            return null;
        return roundingIncrementICU.toBigDecimal();
    }

    /**
     * <strong>[icu]</strong> Sets the rounding increment. In the absence of a rounding increment, numbers
     * will be rounded to the number of digits displayed.
     *
     * @param newValue A positive rounding increment, or <code>null</code> or
     * <code>BigDecimal(0.0)</code> to use the default rounding increment.
     * @throws IllegalArgumentException if <code>newValue</code> is &lt; 0.0
     * @see #getRoundingIncrement
     * @see #getRoundingMode
     * @see #setRoundingMode
     */
    public void setRoundingIncrement(java.math.BigDecimal newValue) {
        if (newValue == null) {
            setRoundingIncrement((BigDecimal) null);
        } else {
            setRoundingIncrement(new BigDecimal(newValue));
        }
    }

    /**
     * <strong>[icu]</strong> Sets the rounding increment. In the absence of a rounding increment, numbers
     * will be rounded to the number of digits displayed.
     *
     * @param newValue A positive rounding increment, or <code>null</code> or
     * <code>BigDecimal(0.0)</code> to use the default rounding increment.
     * @throws IllegalArgumentException if <code>newValue</code> is &lt; 0.0
     * @see #getRoundingIncrement
     * @see #getRoundingMode
     * @see #setRoundingMode
     */
    public void setRoundingIncrement(BigDecimal newValue) {
        int i = newValue == null ? 0 : newValue.compareTo(BigDecimal.ZERO);
        if (i < 0) {
            throw new IllegalArgumentException("Illegal rounding increment");
        }
        if (i == 0) {
            setInternalRoundingIncrement(null);
        } else {
            setInternalRoundingIncrement(newValue);
        }
        resetActualRounding();
    }

    /**
     * <strong>[icu]</strong> Sets the rounding increment. In the absence of a rounding increment, numbers
     * will be rounded to the number of digits displayed.
     *
     * @param newValue A positive rounding increment, or 0.0 to use the default
     * rounding increment.
     * @throws IllegalArgumentException if <code>newValue</code> is &lt; 0.0
     * @see #getRoundingIncrement
     * @see #getRoundingMode
     * @see #setRoundingMode
     */
    public void setRoundingIncrement(double newValue) {
        if (newValue < 0.0) {
            throw new IllegalArgumentException("Illegal rounding increment");
        }
        if (newValue == 0.0d) {
            setInternalRoundingIncrement((BigDecimal) null);
        } else {
            // Should use BigDecimal#valueOf(double) instead of constructor
            // to avoid the double precision problem.
            setInternalRoundingIncrement(BigDecimal.valueOf(newValue));
        }
        resetActualRounding();
    }

    /**
     * Returns the rounding mode.
     *
     * @return A rounding mode, between <code>BigDecimal.ROUND_UP</code> and
     * <code>BigDecimal.ROUND_UNNECESSARY</code>.
     * @see #setRoundingIncrement
     * @see #getRoundingIncrement
     * @see #setRoundingMode
     * @see java.math.BigDecimal
     */
    @Override
    public int getRoundingMode() {
        return roundingMode;
    }

    /**
     * Sets the rounding mode. This has no effect unless the rounding increment is greater
     * than zero.
     *
     * @param roundingMode A rounding mode, between <code>BigDecimal.ROUND_UP</code> and
     * <code>BigDecimal.ROUND_UNNECESSARY</code>.
     * @exception IllegalArgumentException if <code>roundingMode</code> is unrecognized.
     * @see #setRoundingIncrement
     * @see #getRoundingIncrement
     * @see #getRoundingMode
     * @see java.math.BigDecimal
     */
    @Override
    public void setRoundingMode(int roundingMode) {
        if (roundingMode < BigDecimal.ROUND_UP || roundingMode > BigDecimal.ROUND_UNNECESSARY) {
            throw new IllegalArgumentException("Invalid rounding mode: " + roundingMode);
        }

        this.roundingMode = roundingMode;
        resetActualRounding();
    }

    /**
     * Returns the width to which the output of <code>format()</code> is padded. The width is
     * counted in 16-bit code units.
     *
     * @return the format width, or zero if no padding is in effect
     * @see #setFormatWidth
     * @see #getPadCharacter
     * @see #setPadCharacter
     * @see #getPadPosition
     * @see #setPadPosition
     */
    public int getFormatWidth() {
        return formatWidth;
    }

    /**
     * Sets the width to which the output of <code>format()</code> is
     * padded. The width is counted in 16-bit code units.  This method
     * also controls whether padding is enabled.
     *
     * @param width the width to which to pad the result of
     * <code>format()</code>, or zero to disable padding
     * @exception IllegalArgumentException if <code>width</code> is &lt; 0
     * @see #getFormatWidth
     * @see #getPadCharacter
     * @see #setPadCharacter
     * @see #getPadPosition
     * @see #setPadPosition
     */
    public void setFormatWidth(int width) {
        if (width < 0) {
            throw new IllegalArgumentException("Illegal format width");
        }
        formatWidth = width;
    }

    /**
     * <strong>[icu]</strong> Returns the character used to pad to the format width. The default is ' '.
     *
     * @return the pad character
     * @see #setFormatWidth
     * @see #getFormatWidth
     * @see #setPadCharacter
     * @see #getPadPosition
     * @see #setPadPosition
     */
    public char getPadCharacter() {
        return pad;
    }

    /**
     * <strong>[icu]</strong> Sets the character used to pad to the format width. If padding is not
     * enabled, then this will take effect if padding is later enabled.
     *
     * @param padChar the pad character
     * @see #setFormatWidth
     * @see #getFormatWidth
     * @see #getPadCharacter
     * @see #getPadPosition
     * @see #setPadPosition
     */
    public void setPadCharacter(char padChar) {
        pad = padChar;
    }

    /**
     * <strong>[icu]</strong> Returns the position at which padding will take place. This is the location at
     * which padding will be inserted if the result of <code>format()</code> is shorter
     * than the format width.
     *
     * @return the pad position, one of <code>PAD_BEFORE_PREFIX</code>,
     *         <code>PAD_AFTER_PREFIX</code>, <code>PAD_BEFORE_SUFFIX</code>, or
     *         <code>PAD_AFTER_SUFFIX</code>.
     * @see #setFormatWidth
     * @see #getFormatWidth
     * @see #setPadCharacter
     * @see #getPadCharacter
     * @see #setPadPosition
     * @see #PAD_BEFORE_PREFIX
     * @see #PAD_AFTER_PREFIX
     * @see #PAD_BEFORE_SUFFIX
     * @see #PAD_AFTER_SUFFIX
     */
    public int getPadPosition() {
        return padPosition;
    }

    /**
     * <strong>[icu]</strong> Sets the position at which padding will take place. This is the location at
     * which padding will be inserted if the result of <code>format()</code> is shorter
     * than the format width. This has no effect unless padding is enabled.
     *
     * @param padPos the pad position, one of <code>PAD_BEFORE_PREFIX</code>,
     * <code>PAD_AFTER_PREFIX</code>, <code>PAD_BEFORE_SUFFIX</code>, or
     * <code>PAD_AFTER_SUFFIX</code>.
     * @exception IllegalArgumentException if the pad position in unrecognized
     * @see #setFormatWidth
     * @see #getFormatWidth
     * @see #setPadCharacter
     * @see #getPadCharacter
     * @see #getPadPosition
     * @see #PAD_BEFORE_PREFIX
     * @see #PAD_AFTER_PREFIX
     * @see #PAD_BEFORE_SUFFIX
     * @see #PAD_AFTER_SUFFIX
     */
    public void setPadPosition(int padPos) {
        if (padPos < PAD_BEFORE_PREFIX || padPos > PAD_AFTER_SUFFIX) {
            throw new IllegalArgumentException("Illegal pad position");
        }
        padPosition = padPos;
    }

    /**
     * <strong>[icu]</strong> Returns whether or not scientific notation is used.
     *
     * @return true if this object formats and parses scientific notation
     * @see #setScientificNotation
     * @see #getMinimumExponentDigits
     * @see #setMinimumExponentDigits
     * @see #isExponentSignAlwaysShown
     * @see #setExponentSignAlwaysShown
     */
    public boolean isScientificNotation() {
        return useExponentialNotation;
    }

    /**
     * <strong>[icu]</strong> Sets whether or not scientific notation is used. When scientific notation is
     * used, the effective maximum number of integer digits is &lt;= 8. If the maximum number
     * of integer digits is set to more than 8, the effective maximum will be 1. This
     * allows this call to generate a 'default' scientific number format without
     * additional changes.
     *
     * @param useScientific true if this object formats and parses scientific notation
     * @see #isScientificNotation
     * @see #getMinimumExponentDigits
     * @see #setMinimumExponentDigits
     * @see #isExponentSignAlwaysShown
     * @see #setExponentSignAlwaysShown
     */
    public void setScientificNotation(boolean useScientific) {
        useExponentialNotation = useScientific;
    }

    /**
     * <strong>[icu]</strong> Returns the minimum exponent digits that will be shown.
     *
     * @return the minimum exponent digits that will be shown
     * @see #setScientificNotation
     * @see #isScientificNotation
     * @see #setMinimumExponentDigits
     * @see #isExponentSignAlwaysShown
     * @see #setExponentSignAlwaysShown
     */
    public byte getMinimumExponentDigits() {
        return minExponentDigits;
    }

    /**
     * <strong>[icu]</strong> Sets the minimum exponent digits that will be shown. This has no effect
     * unless scientific notation is in use.
     *
     * @param minExpDig a value &gt;= 1 indicating the fewest exponent
     * digits that will be shown
     * @exception IllegalArgumentException if <code>minExpDig</code> &lt; 1
     * @see #setScientificNotation
     * @see #isScientificNotation
     * @see #getMinimumExponentDigits
     * @see #isExponentSignAlwaysShown
     * @see #setExponentSignAlwaysShown
     */
    public void setMinimumExponentDigits(byte minExpDig) {
        if (minExpDig < 1) {
            throw new IllegalArgumentException("Exponent digits must be >= 1");
        }
        minExponentDigits = minExpDig;
    }

    /**
     * <strong>[icu]</strong> Returns whether the exponent sign is always shown.
     *
     * @return true if the exponent is always prefixed with either the localized minus
     * sign or the localized plus sign, false if only negative exponents are prefixed with
     * the localized minus sign.
     * @see #setScientificNotation
     * @see #isScientificNotation
     * @see #setMinimumExponentDigits
     * @see #getMinimumExponentDigits
     * @see #setExponentSignAlwaysShown
     */
    public boolean isExponentSignAlwaysShown() {
        return exponentSignAlwaysShown;
    }

    /**
     * <strong>[icu]</strong> Sets whether the exponent sign is always shown. This has no effect unless
     * scientific notation is in use.
     *
     * @param expSignAlways true if the exponent is always prefixed with either the
     * localized minus sign or the localized plus sign, false if only negative exponents
     * are prefixed with the localized minus sign.
     * @see #setScientificNotation
     * @see #isScientificNotation
     * @see #setMinimumExponentDigits
     * @see #getMinimumExponentDigits
     * @see #isExponentSignAlwaysShown
     */
    public void setExponentSignAlwaysShown(boolean expSignAlways) {
        exponentSignAlwaysShown = expSignAlways;
    }

    /**
     * Returns the grouping size. Grouping size is the number of digits between grouping
     * separators in the integer portion of a number. For example, in the number
     * "123,456.78", the grouping size is 3.
     *
     * @see #setGroupingSize
     * @see NumberFormat#isGroupingUsed
     * @see DecimalFormatSymbols#getGroupingSeparator
     */
    public int getGroupingSize() {
        return groupingSize;
    }

    /**
     * Sets the grouping size. Grouping size is the number of digits between grouping
     * separators in the integer portion of a number. For example, in the number
     * "123,456.78", the grouping size is 3.
     *
     * @see #getGroupingSize
     * @see NumberFormat#setGroupingUsed
     * @see DecimalFormatSymbols#setGroupingSeparator
     */
    public void setGroupingSize(int newValue) {
        groupingSize = (byte) newValue;
    }

    /**
     * <strong>[icu]</strong> Returns the secondary grouping size. In some locales one grouping interval
     * is used for the least significant integer digits (the primary grouping size), and
     * another is used for all others (the secondary grouping size). A formatter
     * supporting a secondary grouping size will return a positive integer unequal to the
     * primary grouping size returned by <code>getGroupingSize()</code>. For example, if
     * the primary grouping size is 4, and the secondary grouping size is 2, then the
     * number 123456789 formats as "1,23,45,6789", and the pattern appears as "#,##,###0".
     *
     * @return the secondary grouping size, or a value less than one if there is none
     * @see #setSecondaryGroupingSize
     * @see NumberFormat#isGroupingUsed
     * @see DecimalFormatSymbols#getGroupingSeparator
     */
    public int getSecondaryGroupingSize() {
        return groupingSize2;
    }

    /**
     * <strong>[icu]</strong> Sets the secondary grouping size. If set to a value less than 1, then
     * secondary grouping is turned off, and the primary grouping size is used for all
     * intervals, not just the least significant.
     *
     * @see #getSecondaryGroupingSize
     * @see NumberFormat#setGroupingUsed
     * @see DecimalFormatSymbols#setGroupingSeparator
     */
    public void setSecondaryGroupingSize(int newValue) {
        groupingSize2 = (byte) newValue;
    }

    /**
     * <strong>[icu]</strong> Returns the MathContext used by this format.
     *
     * @return desired MathContext
     * @see #getMathContext
     */
    public MathContext getMathContextICU() {
        return mathContext;
    }

    /**
     * <strong>[icu]</strong> Returns the MathContext used by this format.
     *
     * @return desired MathContext
     * @see #getMathContext
     */
    public java.math.MathContext getMathContext() {
        try {
            // don't allow multiple references
            return mathContext == null ? null : new java.math.MathContext(mathContext.getDigits(),
                    java.math.RoundingMode.valueOf(mathContext.getRoundingMode()));
        } catch (Exception foo) {
            return null; // should never happen
        }
    }

    /**
     * <strong>[icu]</strong> Sets the MathContext used by this format.
     *
     * @param newValue desired MathContext
     * @see #getMathContext
     */
    public void setMathContextICU(MathContext newValue) {
        mathContext = newValue;
    }

    /**
     * <strong>[icu]</strong> Sets the MathContext used by this format.
     *
     * @param newValue desired MathContext
     * @see #getMathContext
     */
    public void setMathContext(java.math.MathContext newValue) {
        mathContext = new MathContext(newValue.getPrecision(), MathContext.SCIENTIFIC, false,
                                      (newValue.getRoundingMode()).ordinal());
    }

    /**
     * Returns the behavior of the decimal separator with integers. (The decimal
     * separator will always appear with decimals.)  <p> Example: Decimal ON: 12345 -&gt;
     * 12345.; OFF: 12345 -&gt; 12345
     */
    public boolean isDecimalSeparatorAlwaysShown() {
        return decimalSeparatorAlwaysShown;
    }

    /**
     * When decimal match is not required, the input does not have to
     * contain a decimal mark when there is a decimal mark specified in the
     * pattern.
     * @param value true if input must contain a match to decimal mark in pattern
     * Default is false.
     */
     public void setDecimalPatternMatchRequired(boolean value) {
         parseRequireDecimalPoint = value;
     }

    /**
     * <strong>[icu]</strong> Returns whether the input to parsing must contain a decimal mark if there
     * is a decimal mark in the pattern.
     * @return true if input must contain a match to decimal mark in pattern
     */
    public boolean isDecimalPatternMatchRequired() {
        return parseRequireDecimalPoint;
    }


    /**
     * Sets the behavior of the decimal separator with integers. (The decimal separator
     * will always appear with decimals.)
     *
     * <p>This only affects formatting, and only where there might be no digits after the
     * decimal point, e.g., if true, 3456.00 -&gt; "3,456." if false, 3456.00 -&gt; "3456" This
     * is independent of parsing. If you want parsing to stop at the decimal point, use
     * setParseIntegerOnly.
     *
     * <p>
     * Example: Decimal ON: 12345 -&gt; 12345.; OFF: 12345 -&gt; 12345
     */
    public void setDecimalSeparatorAlwaysShown(boolean newValue) {
        decimalSeparatorAlwaysShown = newValue;
    }

    /**
     * <strong>[icu]</strong> Returns a copy of the CurrencyPluralInfo used by this format. It might
     * return null if the decimal format is not a plural type currency decimal
     * format. Plural type currency decimal format means either the pattern in the decimal
     * format contains 3 currency signs, or the decimal format is initialized with
     * PLURALCURRENCYSTYLE.
     *
     * @return desired CurrencyPluralInfo
     * @see CurrencyPluralInfo
     */
    public CurrencyPluralInfo getCurrencyPluralInfo() {
        try {
            // don't allow multiple references
            return currencyPluralInfo == null ? null :
                (CurrencyPluralInfo) currencyPluralInfo.clone();
        } catch (Exception foo) {
            return null; // should never happen
        }
    }

    /**
     * <strong>[icu]</strong> Sets the CurrencyPluralInfo used by this format. The format uses a copy of
     * the provided information.
     *
     * @param newInfo desired CurrencyPluralInfo
     * @see CurrencyPluralInfo
     */
    public void setCurrencyPluralInfo(CurrencyPluralInfo newInfo) {
        currencyPluralInfo = (CurrencyPluralInfo) newInfo.clone();
        isReadyForParsing = false;
    }

    /**
     * Overrides clone.
     */
    @Override
    public Object clone() {
        try {
            DecimalFormat other = (DecimalFormat) super.clone();
            other.symbols = (DecimalFormatSymbols) symbols.clone();
            other.digitList = new DigitList(); // fix for JB#5358
            if (currencyPluralInfo != null) {
                other.currencyPluralInfo = (CurrencyPluralInfo) currencyPluralInfo.clone();
            }
            other.attributes = new ArrayList<FieldPosition>(); // #9240
            other.currencyUsage = currencyUsage;

            // TODO: We need to figure out whether we share a single copy of DigitList by
            // multiple cloned copies.  format/subformat are designed to use a single
            // instance, but parse/subparse implementation is not.
            return other;
        } catch (Exception e) {
            throw new IllegalStateException();
        }
    }

    /**
     * Overrides equals.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!super.equals(obj))
            return false; // super does class check

        DecimalFormat other = (DecimalFormat) obj;
        // Add the comparison of the four new added fields ,they are posPrefixPattern,
        // posSuffixPattern, negPrefixPattern, negSuffixPattern. [Richard/GCL]
        // following are added to accomodate changes for currency plural format.
        return currencySignCount == other.currencySignCount
                && (style != NumberFormat.PLURALCURRENCYSTYLE ||
                    equals(posPrefixPattern, other.posPrefixPattern)
                && equals(posSuffixPattern, other.posSuffixPattern)
                && equals(negPrefixPattern, other.negPrefixPattern)
                && equals(negSuffixPattern, other.negSuffixPattern))
                && multiplier == other.multiplier
                && groupingSize == other.groupingSize
                && groupingSize2 == other.groupingSize2
                && decimalSeparatorAlwaysShown == other.decimalSeparatorAlwaysShown
                && useExponentialNotation == other.useExponentialNotation
                && (!useExponentialNotation || minExponentDigits == other.minExponentDigits)
                && useSignificantDigits == other.useSignificantDigits
                && (!useSignificantDigits || minSignificantDigits == other.minSignificantDigits
                        && maxSignificantDigits == other.maxSignificantDigits)
                && symbols.equals(other.symbols)
                && Utility.objectEquals(currencyPluralInfo, other.currencyPluralInfo)
                && currencyUsage.equals(other.currencyUsage);
    }

    // method to unquote the strings and compare
    private boolean equals(String pat1, String pat2) {
        if (pat1 == null || pat2 == null) {
            return (pat1 == null && pat2 == null);
        }
        // fast path
        if (pat1.equals(pat2)) {
            return true;
        }
        return unquote(pat1).equals(unquote(pat2));
    }

    private String unquote(String pat) {
        StringBuilder buf = new StringBuilder(pat.length());
        int i = 0;
        while (i < pat.length()) {
            char ch = pat.charAt(i++);
            if (ch != QUOTE) {
                buf.append(ch);
            }
        }
        return buf.toString();
    }

    // protected void handleToString(StringBuffer buf) {
    // buf.append("\nposPrefixPattern: '" + posPrefixPattern + "'\n");
    // buf.append("positivePrefix: '" + positivePrefix + "'\n");
    // buf.append("posSuffixPattern: '" + posSuffixPattern + "'\n");
    // buf.append("positiveSuffix: '" + positiveSuffix + "'\n");
    // buf.append("negPrefixPattern: '" +
    //     android.icu.impl.Utility.format1ForSource(negPrefixPattern) + "'\n");
    // buf.append("negativePrefix: '" +
    //     android.icu.impl.Utility.format1ForSource(negativePrefix) + "'\n");
    // buf.append("negSuffixPattern: '" + negSuffixPattern + "'\n");
    // buf.append("negativeSuffix: '" + negativeSuffix + "'\n");
    // buf.append("multiplier: '" + multiplier + "'\n");
    // buf.append("groupingSize: '" + groupingSize + "'\n");
    // buf.append("groupingSize2: '" + groupingSize2 + "'\n");
    // buf.append("decimalSeparatorAlwaysShown: '" + decimalSeparatorAlwaysShown + "'\n");
    // buf.append("useExponentialNotation: '" + useExponentialNotation + "'\n");
    // buf.append("minExponentDigits: '" + minExponentDigits + "'\n");
    // buf.append("useSignificantDigits: '" + useSignificantDigits + "'\n");
    // buf.append("minSignificantDigits: '" + minSignificantDigits + "'\n");
    // buf.append("maxSignificantDigits: '" + maxSignificantDigits + "'\n");
    // buf.append("symbols: '" + symbols + "'");
    // }

    /**
     * Overrides hashCode.
     */
    @Override
    public int hashCode() {
        return super.hashCode() * 37 + positivePrefix.hashCode();
        // just enough fields for a reasonable distribution
    }

    /**
     * Synthesizes a pattern string that represents the current state of this Format
     * object.
     *
     * @see #applyPattern
     */
    public String toPattern() {
        if (style == NumberFormat.PLURALCURRENCYSTYLE) {
            // the prefix or suffix pattern might not be defined yet, so they can not be
            // synthesized, instead, get them directly.  but it might not be the actual
            // pattern used in formatting.  the actual pattern used in formatting depends
            // on the formatted number's plural count.
            return formatPattern;
        }
        return toPattern(false);
    }

    /**
     * Synthesizes a localized pattern string that represents the current state of this
     * Format object.
     *
     * @see #applyPattern
     */
    public String toLocalizedPattern() {
        if (style == NumberFormat.PLURALCURRENCYSTYLE) {
            return formatPattern;
        }
        return toPattern(true);
    }

    /**
     * Expands the affix pattern strings into the expanded affix strings. If any affix
     * pattern string is null, do not expand it. This method should be called any time the
     * symbols or the affix patterns change in order to keep the expanded affix strings up
     * to date. This method also will be called before formatting if format currency
     * plural names, since the plural name is not a static one, it is based on the
     * currency plural count, the affix will be known only after the currency plural count
     * is know. In which case, the parameter 'pluralCount' will be a non-null currency
     * plural count. In all other cases, the 'pluralCount' is null, which means it is not
     * needed.
     */
    // Bug 4212072 [Richard/GCL]
    private void expandAffixes(String pluralCount) {
        // expandAffix() will set currencyChoice to a non-null value if
        // appropriate AND if it is null.
        currencyChoice = null;

        // Reuse one StringBuffer for better performance
        StringBuffer buffer = new StringBuffer();
        if (posPrefixPattern != null) {
            expandAffix(posPrefixPattern, pluralCount, buffer);
            positivePrefix = buffer.toString();
        }
        if (posSuffixPattern != null) {
            expandAffix(posSuffixPattern, pluralCount, buffer);
            positiveSuffix = buffer.toString();
        }
        if (negPrefixPattern != null) {
            expandAffix(negPrefixPattern, pluralCount, buffer);
            negativePrefix = buffer.toString();
        }
        if (negSuffixPattern != null) {
            expandAffix(negSuffixPattern, pluralCount, buffer);
            negativeSuffix = buffer.toString();
        }
    }

    /**
     * Expands an affix pattern into an affix string. All characters in the pattern are
     * literal unless bracketed by QUOTEs. The following characters outside QUOTE are
     * recognized: PATTERN_PERCENT, PATTERN_PER_MILLE, PATTERN_MINUS, and
     * CURRENCY_SIGN. If CURRENCY_SIGN is doubled, it is interpreted as an international
     * currency sign. If CURRENCY_SIGN is tripled, it is interpreted as currency plural
     * long names, such as "US Dollars". Any other character outside QUOTE represents
     * itself. Quoted text must be well-formed.
     *
     * This method is used in two distinct ways. First, it is used to expand the stored
     * affix patterns into actual affixes. For this usage, doFormat must be false. Second,
     * it is used to expand the stored affix patterns given a specific number (doFormat ==
     * true), for those rare cases in which a currency format references a ChoiceFormat
     * (e.g., en_IN display name for INR). The number itself is taken from digitList.
     * TODO: There are no currency ChoiceFormat patterns, figure out what is still relevant here.
     *
     * When used in the first way, this method has a side effect: It sets currencyChoice
     * to a ChoiceFormat object, if the currency's display name in this locale is a
     * ChoiceFormat pattern (very rare). It only does this if currencyChoice is null to
     * start with.
     *
     * @param pattern the non-null, possibly empty pattern
     * @param pluralCount the plural count. It is only used for currency plural format. In
     * which case, it is the plural count of the currency amount. For example, in en_US,
     * it is the singular "one", or the plural "other". For all other cases, it is null,
     * and is not being used.
     * @param buffer a scratch StringBuffer; its contents will be lost
     */
    // Bug 4212072 [Richard/GCL]
    private void expandAffix(String pattern, String pluralCount, StringBuffer buffer) {
        buffer.setLength(0);
        for (int i = 0; i < pattern.length();) {
            char c = pattern.charAt(i++);
            if (c == QUOTE) {
                for (;;) {
                    int j = pattern.indexOf(QUOTE, i);
                    if (j == i) {
                        buffer.append(QUOTE);
                        i = j + 1;
                        break;
                    } else if (j > i) {
                        buffer.append(pattern.substring(i, j));
                        i = j + 1;
                        if (i < pattern.length() && pattern.charAt(i) == QUOTE) {
                            buffer.append(QUOTE);
                            ++i;
                            // loop again
                        } else {
                            break;
                        }
                    } else {
                        // Unterminated quote; should be caught by apply
                        // pattern.
                        throw new RuntimeException();
                    }
                }
                continue;
            }

            switch (c) {
            case CURRENCY_SIGN:
                // As of ICU 2.2 we use the currency object, and ignore the currency
                // symbols in the DFS, unless we have a null currency object. This occurs
                // if resurrecting a pre-2.2 object or if the user sets a custom DFS.
                boolean intl = i < pattern.length() && pattern.charAt(i) == CURRENCY_SIGN;
                boolean plural = false;
                if (intl) {
                    ++i;
                    if (i < pattern.length() && pattern.charAt(i) == CURRENCY_SIGN) {
                        plural = true;
                        intl = false;
                        ++i;
                    }
                }
                String s = null;
                Currency currency = getCurrency();
                if (currency != null) {
                    // plural name is only needed when pluralCount != null, which means
                    // when formatting currency plural names.  For other cases,
                    // pluralCount == null, and plural names are not needed.
                    if (plural && pluralCount != null) {
                        s = currency.getName(symbols.getULocale(), Currency.PLURAL_LONG_NAME,
                                             pluralCount, null);
                    } else if (!intl) {
                        s = currency.getName(symbols.getULocale(), Currency.SYMBOL_NAME, null);
                    } else {
                        s = currency.getCurrencyCode();
                    }
                } else {
                    s = intl ? symbols.getInternationalCurrencySymbol() :
                        symbols.getCurrencySymbol();
                }
                // Here is where FieldPosition could be set for CURRENCY PLURAL.
                buffer.append(s);
                break;
            case PATTERN_PERCENT:
                buffer.append(symbols.getPercentString());
                break;
            case PATTERN_PER_MILLE:
                buffer.append(symbols.getPerMillString());
                break;
            case PATTERN_MINUS_SIGN:
                buffer.append(symbols.getMinusSignString());
                break;
            default:
                buffer.append(c);
                break;
            }
        }
    }

    /**
     * Append an affix to the given StringBuffer.
     *
     * @param buf
     *            buffer to append to
     * @param isNegative
     * @param isPrefix
     * @param fieldPosition
     * @param parseAttr
     */
    private int appendAffix(StringBuffer buf, boolean isNegative, boolean isPrefix,
                            FieldPosition fieldPosition,
                            boolean parseAttr) {
        if (currencyChoice != null) {
            String affixPat = null;
            if (isPrefix) {
                affixPat = isNegative ? negPrefixPattern : posPrefixPattern;
            } else {
                affixPat = isNegative ? negSuffixPattern : posSuffixPattern;
            }
            StringBuffer affixBuf = new StringBuffer();
            expandAffix(affixPat, null, affixBuf);
            buf.append(affixBuf);
            return affixBuf.length();
        }

        String affix = null;
        String pattern;
        if (isPrefix) {
            affix = isNegative ? negativePrefix : positivePrefix;
            pattern = isNegative ? negPrefixPattern : posPrefixPattern;
        } else {
            affix = isNegative ? negativeSuffix : positiveSuffix;
            pattern = isNegative ? negSuffixPattern : posSuffixPattern;
        }
        // [Spark/CDL] Invoke formatAffix2Attribute to add attributes for affix
        if (parseAttr) {
            // Updates for Ticket 11805.
            int offset = affix.indexOf(symbols.getCurrencySymbol());
            if (offset > -1) {
                formatAffix2Attribute(isPrefix, Field.CURRENCY, buf, offset,
                        symbols.getCurrencySymbol().length());
            }
            offset = affix.indexOf(symbols.getMinusSignString());
            if (offset > -1) {
                formatAffix2Attribute(isPrefix, Field.SIGN, buf, offset,
                        symbols.getMinusSignString().length());
            }
            offset = affix.indexOf(symbols.getPercentString());
            if (offset > -1) {
                formatAffix2Attribute(isPrefix, Field.PERCENT, buf, offset,
                        symbols.getPercentString().length());
            }
            offset = affix.indexOf(symbols.getPerMillString());
            if (offset > -1) {
                formatAffix2Attribute(isPrefix, Field.PERMILLE, buf, offset,
                        symbols.getPerMillString().length());
            }
            offset = pattern.indexOf("¤¤¤");
            if (offset > -1) {
                formatAffix2Attribute(isPrefix, Field.CURRENCY, buf, offset,
                        affix.length() - offset);
            }
        }

        // Look for SIGN, PERCENT, PERMILLE in the formatted affix.
        if (fieldPosition.getFieldAttribute() == NumberFormat.Field.SIGN) {
            String sign = isNegative ? symbols.getMinusSignString() : symbols.getPlusSignString();
            int firstPos = affix.indexOf(sign);
            if (firstPos > -1) {
                int startPos = buf.length() + firstPos;
                fieldPosition.setBeginIndex(startPos);
                fieldPosition.setEndIndex(startPos + sign.length());
            }
        } else if (fieldPosition.getFieldAttribute() == NumberFormat.Field.PERCENT) {
            int firstPos = affix.indexOf(symbols.getPercentString());
            if (firstPos > -1) {
                int startPos = buf.length() + firstPos;
                fieldPosition.setBeginIndex(startPos);
                fieldPosition.setEndIndex(startPos + symbols.getPercentString().length());
            }
        } else if (fieldPosition.getFieldAttribute() == NumberFormat.Field.PERMILLE) {
            int firstPos = affix.indexOf(symbols.getPerMillString());
            if (firstPos > -1) {
                int startPos = buf.length() + firstPos;
                fieldPosition.setBeginIndex(startPos);
                fieldPosition.setEndIndex(startPos + symbols.getPerMillString().length());
            }
        } else
        // If CurrencySymbol or InternationalCurrencySymbol is in the affix, check for currency symbol.
        // Get spelled out name if "¤¤¤" is in the pattern.
        if (fieldPosition.getFieldAttribute() == NumberFormat.Field.CURRENCY) {
            if (affix.indexOf(symbols.getCurrencySymbol()) > -1) {
                String aff = symbols.getCurrencySymbol();
                int firstPos = affix.indexOf(aff);
                int start = buf.length() + firstPos;
                int end = start + aff.length();
                fieldPosition.setBeginIndex(start);
                fieldPosition.setEndIndex(end);
            } else if (affix.indexOf(symbols.getInternationalCurrencySymbol()) > -1) {
                String aff = symbols.getInternationalCurrencySymbol();
                int firstPos = affix.indexOf(aff);
                int start = buf.length() + firstPos;
                int end = start + aff.length();
                fieldPosition.setBeginIndex(start);
                fieldPosition.setEndIndex(end);
            } else if (pattern.indexOf("¤¤¤") > -1) {
                // It's a plural, and we know where it is in the pattern.
                int firstPos = pattern.indexOf("¤¤¤");
                int start = buf.length() + firstPos;
                int end = buf.length() + affix.length(); // This seems clunky and wrong.
                fieldPosition.setBeginIndex(start);
                fieldPosition.setEndIndex(end);
            }
        }

        buf.append(affix);
        return affix.length();
    }

    // Fix for prefix and suffix in Ticket 11805.
    private void formatAffix2Attribute(boolean isPrefix, Field fieldType,
        StringBuffer buf, int offset, int symbolSize) {
        int begin;
        begin = offset;
        if (!isPrefix) {
            begin += buf.length();
        }

        addAttribute(fieldType, begin, begin + symbolSize);
    }

    /**
     * [Spark/CDL] Use this method to add attribute.
     */
    private void addAttribute(Field field, int begin, int end) {
        FieldPosition pos = new FieldPosition(field);
        pos.setBeginIndex(begin);
        pos.setEndIndex(end);
        attributes.add(pos);
    }

    /**
     * Formats the object to an attributed string, and return the corresponding iterator.
     */
    @Override
    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
      return formatToCharacterIterator(obj, NULL_UNIT);
    }

    AttributedCharacterIterator formatToCharacterIterator(Object obj, Unit unit) {
        if (!(obj instanceof Number))
            throw new IllegalArgumentException();
        Number number = (Number) obj;
        StringBuffer text = new StringBuffer();
        unit.writePrefix(text);
        attributes.clear();
        if (obj instanceof BigInteger) {
            format((BigInteger) number, text, new FieldPosition(0), true);
        } else if (obj instanceof java.math.BigDecimal) {
            format((java.math.BigDecimal) number, text, new FieldPosition(0)
                          , true);
        } else if (obj instanceof Double) {
            format(number.doubleValue(), text, new FieldPosition(0), true);
        } else if (obj instanceof Integer || obj instanceof Long) {
            format(number.longValue(), text, new FieldPosition(0), true);
        } else {
            throw new IllegalArgumentException();
        }
        unit.writeSuffix(text);
        AttributedString as = new AttributedString(text.toString());

        // add NumberFormat field attributes to the AttributedString
        for (int i = 0; i < attributes.size(); i++) {
            FieldPosition pos = attributes.get(i);
            Format.Field attribute = pos.getFieldAttribute();
            as.addAttribute(attribute, attribute, pos.getBeginIndex(), pos.getEndIndex());
        }

        // return the CharacterIterator from AttributedString
        return as.getIterator();
    }

    /**
     * Appends an affix pattern to the given StringBuffer. Localize unquoted specials.
     * <p>
     * <b>Note:</b> This implementation does not support new String localized symbols.
     */
    private void appendAffixPattern(StringBuffer buffer, boolean isNegative, boolean isPrefix,
                                    boolean localized) {
        String affixPat = null;
        if (isPrefix) {
            affixPat = isNegative ? negPrefixPattern : posPrefixPattern;
        } else {
            affixPat = isNegative ? negSuffixPattern : posSuffixPattern;
        }

        // When there is a null affix pattern, we use the affix itself.
        if (affixPat == null) {
            String affix = null;
            if (isPrefix) {
                affix = isNegative ? negativePrefix : positivePrefix;
            } else {
                affix = isNegative ? negativeSuffix : positiveSuffix;
            }
            // Do this crudely for now: Wrap everything in quotes.
            buffer.append(QUOTE);
            for (int i = 0; i < affix.length(); ++i) {
                char ch = affix.charAt(i);
                if (ch == QUOTE) {
                    buffer.append(ch);
                }
                buffer.append(ch);
            }
            buffer.append(QUOTE);
            return;
        }

        if (!localized) {
            buffer.append(affixPat);
        } else {
            int i, j;
            for (i = 0; i < affixPat.length(); ++i) {
                char ch = affixPat.charAt(i);
                switch (ch) {
                case QUOTE:
                    j = affixPat.indexOf(QUOTE, i + 1);
                    if (j < 0) {
                        throw new IllegalArgumentException("Malformed affix pattern: " + affixPat);
                    }
                    buffer.append(affixPat.substring(i, j + 1));
                    i = j;
                    continue;
                case PATTERN_PER_MILLE:
                    ch = symbols.getPerMill();
                    break;
                case PATTERN_PERCENT:
                    ch = symbols.getPercent();
                    break;
                case PATTERN_MINUS_SIGN:
                    ch = symbols.getMinusSign();
                    break;
                }
                // check if char is same as any other symbol
                if (ch == symbols.getDecimalSeparator() || ch == symbols.getGroupingSeparator()) {
                    buffer.append(QUOTE);
                    buffer.append(ch);
                    buffer.append(QUOTE);
                } else {
                    buffer.append(ch);
                }
            }
        }
    }

    /**
     * Does the real work of generating a pattern.
     * <p>
     * <b>Note:</b> This implementation does not support new String localized symbols.
     */
    private String toPattern(boolean localized) {
        StringBuffer result = new StringBuffer();
        char zero = localized ? symbols.getZeroDigit() : PATTERN_ZERO_DIGIT;
        char digit = localized ? symbols.getDigit() : PATTERN_DIGIT;
        char sigDigit = 0;
        boolean useSigDig = areSignificantDigitsUsed();
        if (useSigDig) {
            sigDigit = localized ? symbols.getSignificantDigit() : PATTERN_SIGNIFICANT_DIGIT;
        }
        char group = localized ? symbols.getGroupingSeparator() : PATTERN_GROUPING_SEPARATOR;
        int i;
        int roundingDecimalPos = 0; // Pos of decimal in roundingDigits
        String roundingDigits = null;
        int padPos = (formatWidth > 0) ? padPosition : -1;
        String padSpec = (formatWidth > 0)
            ? new StringBuffer(2).append(localized
                                         ? symbols.getPadEscape()
                                         : PATTERN_PAD_ESCAPE).append(pad).toString()
            : null;
        if (roundingIncrementICU != null) {
            i = roundingIncrementICU.scale();
            roundingDigits = roundingIncrementICU.movePointRight(i).toString();
            roundingDecimalPos = roundingDigits.length() - i;
        }
        for (int part = 0; part < 2; ++part) {
            // variable not used int partStart = result.length();
            if (padPos == PAD_BEFORE_PREFIX) {
                result.append(padSpec);
            }

            // Use original symbols read from resources in pattern eg. use "\u00A4"
            // instead of "$" in Locale.US [Richard/GCL]
            appendAffixPattern(result, part != 0, true, localized);
            if (padPos == PAD_AFTER_PREFIX) {
                result.append(padSpec);
            }
            int sub0Start = result.length();
            int g = isGroupingUsed() ? Math.max(0, groupingSize) : 0;
            if (g > 0 && groupingSize2 > 0 && groupingSize2 != groupingSize) {
                g += groupingSize2;
            }
            int maxDig = 0, minDig = 0, maxSigDig = 0;
            if (useSigDig) {
                minDig = getMinimumSignificantDigits();
                maxDig = maxSigDig = getMaximumSignificantDigits();
            } else {
                minDig = getMinimumIntegerDigits();
                maxDig = getMaximumIntegerDigits();
            }
            if (useExponentialNotation) {
                if (maxDig > MAX_SCIENTIFIC_INTEGER_DIGITS) {
                    maxDig = 1;
                }
            } else if (useSigDig) {
                maxDig = Math.max(maxDig, g + 1);
            } else {
                maxDig = Math.max(Math.max(g, getMinimumIntegerDigits()), roundingDecimalPos) + 1;
            }
            for (i = maxDig; i > 0; --i) {
                if (!useExponentialNotation && i < maxDig && isGroupingPosition(i)) {
                    result.append(group);
                }
                if (useSigDig) {
                    // #@,@### (maxSigDig == 5, minSigDig == 2) 65 4321 (1-based pos,
                    // count from the right) Use # if pos > maxSigDig or 1 <= pos <=
                    // (maxSigDig - minSigDig) Use @ if (maxSigDig - minSigDig) < pos <=
                    // maxSigDig
                    result.append((maxSigDig >= i && i > (maxSigDig - minDig)) ? sigDigit : digit);
                } else {
                    if (roundingDigits != null) {
                        int pos = roundingDecimalPos - i;
                        if (pos >= 0 && pos < roundingDigits.length()) {
                            result.append((char) (roundingDigits.charAt(pos) - '0' + zero));
                            continue;
                        }
                    }
                    result.append(i <= minDig ? zero : digit);
                }
            }
            if (!useSigDig) {
                if (getMaximumFractionDigits() > 0 || decimalSeparatorAlwaysShown) {
                    result.append(localized ? symbols.getDecimalSeparator() :
                                  PATTERN_DECIMAL_SEPARATOR);
                }
                int pos = roundingDecimalPos;
                for (i = 0; i < getMaximumFractionDigits(); ++i) {
                    if (roundingDigits != null && pos < roundingDigits.length()) {
                        result.append(pos < 0 ? zero :
                                      (char) (roundingDigits.charAt(pos) - '0' + zero));
                        ++pos;
                        continue;
                    }
                    result.append(i < getMinimumFractionDigits() ? zero : digit);
                }
            }
            if (useExponentialNotation) {
                if (localized) {
                    result.append(symbols.getExponentSeparator());
                } else {
                    result.append(PATTERN_EXPONENT);
                }
                if (exponentSignAlwaysShown) {
                    result.append(localized ? symbols.getPlusSign() : PATTERN_PLUS_SIGN);
                }
                for (i = 0; i < minExponentDigits; ++i) {
                    result.append(zero);
                }
            }
            if (padSpec != null && !useExponentialNotation) {
                int add = formatWidth
                        - result.length()
                        + sub0Start
                        - ((part == 0)
                           ? positivePrefix.length() + positiveSuffix.length()
                           : negativePrefix.length() + negativeSuffix.length());
                while (add > 0) {
                    result.insert(sub0Start, digit);
                    ++maxDig;
                    --add;
                    // Only add a grouping separator if we have at least 2 additional
                    // characters to be added, so we don't end up with ",###".
                    if (add > 1 && isGroupingPosition(maxDig)) {
                        result.insert(sub0Start, group);
                        --add;
                    }
                }
            }
            if (padPos == PAD_BEFORE_SUFFIX) {
                result.append(padSpec);
            }
            // Use original symbols read from resources in pattern eg. use "\u00A4"
            // instead of "$" in Locale.US [Richard/GCL]
            appendAffixPattern(result, part != 0, false, localized);
            if (padPos == PAD_AFTER_SUFFIX) {
                result.append(padSpec);
            }
            if (part == 0) {
                if (negativeSuffix.equals(positiveSuffix) &&
                    negativePrefix.equals(PATTERN_MINUS_SIGN + positivePrefix)) {
                    break;
                } else {
                    result.append(localized ? symbols.getPatternSeparator() : PATTERN_SEPARATOR);
                }
            }
        }
        return result.toString();
    }

    /**
     * Applies the given pattern to this Format object. A pattern is a short-hand
     * specification for the various formatting properties. These properties can also be
     * changed individually through the various setter methods.
     *
     * <p>There is no limit to integer digits are set by this routine, since that is the
     * typical end-user desire; use setMaximumInteger if you want to set a real value. For
     * negative numbers, use a second pattern, separated by a semicolon
     *
     * <p>Example "#,#00.0#" -&gt; 1,234.56
     *
     * <p>This means a minimum of 2 integer digits, 1 fraction digit, and a maximum of 2
     * fraction digits.
     *
     * <p>Example: "#,#00.0#;(#,#00.0#)" for negatives in parentheses.
     *
     * <p>In negative patterns, the minimum and maximum counts are ignored; these are
     * presumed to be set in the positive pattern.
     */
    public void applyPattern(String pattern) {
        applyPattern(pattern, false);
    }

    /**
     * Applies the given pattern to this Format object. The pattern is assumed to be in a
     * localized notation. A pattern is a short-hand specification for the various
     * formatting properties. These properties can also be changed individually through
     * the various setter methods.
     *
     * <p>There is no limit to integer digits are set by this routine, since that is the
     * typical end-user desire; use setMaximumInteger if you want to set a real value. For
     * negative numbers, use a second pattern, separated by a semicolon
     *
     * <p>Example "#,#00.0#" -&gt; 1,234.56
     *
     * <p>This means a minimum of 2 integer digits, 1 fraction digit, and a maximum of 2
     * fraction digits.
     *
     * <p>Example: "#,#00.0#;(#,#00.0#)" for negatives in parantheses.
     *
     * <p>In negative patterns, the minimum and maximum counts are ignored; these are
     * presumed to be set in the positive pattern.
     */
    public void applyLocalizedPattern(String pattern) {
        applyPattern(pattern, true);
    }

    /**
     * Does the real work of applying a pattern.
     */
    private void applyPattern(String pattern, boolean localized) {
        applyPatternWithoutExpandAffix(pattern, localized);
        expandAffixAdjustWidth(null);
    }

    private void expandAffixAdjustWidth(String pluralCount) {
        // Bug 4212072 Update the affix strings according to symbols in order to keep the
        // affix strings up to date.  [Richard/GCL]
        expandAffixes(pluralCount);

        // Now that we have the actual prefix and suffix, fix up formatWidth
        if (formatWidth > 0) {
            formatWidth += positivePrefix.length() + positiveSuffix.length();
        }
    }

    private void applyPatternWithoutExpandAffix(String pattern, boolean localized) {
        char zeroDigit = PATTERN_ZERO_DIGIT; // '0'
        char sigDigit = PATTERN_SIGNIFICANT_DIGIT; // '@'
        char groupingSeparator = PATTERN_GROUPING_SEPARATOR;
        char decimalSeparator = PATTERN_DECIMAL_SEPARATOR;
        char percent = PATTERN_PERCENT;
        char perMill = PATTERN_PER_MILLE;
        char digit = PATTERN_DIGIT; // '#'
        char separator = PATTERN_SEPARATOR;
        String exponent = String.valueOf(PATTERN_EXPONENT);
        char plus = PATTERN_PLUS_SIGN;
        char padEscape = PATTERN_PAD_ESCAPE;
        char minus = PATTERN_MINUS_SIGN; // Bug 4212072 [Richard/GCL]
        if (localized) {
            zeroDigit = symbols.getZeroDigit();
            sigDigit = symbols.getSignificantDigit();
            groupingSeparator = symbols.getGroupingSeparator();
            decimalSeparator = symbols.getDecimalSeparator();
            percent = symbols.getPercent();
            perMill = symbols.getPerMill();
            digit = symbols.getDigit();
            separator = symbols.getPatternSeparator();
            exponent = symbols.getExponentSeparator();
            plus = symbols.getPlusSign();
            padEscape = symbols.getPadEscape();
            minus = symbols.getMinusSign(); // Bug 4212072 [Richard/GCL]
        }
        char nineDigit = (char) (zeroDigit + 9);

        boolean gotNegative = false;

        int pos = 0;
        // Part 0 is the positive pattern. Part 1, if present, is the negative
        // pattern.
        for (int part = 0; part < 2 && pos < pattern.length(); ++part) {
            // The subpart ranges from 0 to 4: 0=pattern proper, 1=prefix, 2=suffix,
            // 3=prefix in quote, 4=suffix in quote. Subpart 0 is between the prefix and
            // suffix, and consists of pattern characters. In the prefix and suffix,
            // percent, permille, and currency symbols are recognized and translated.
            int subpart = 1, sub0Start = 0, sub0Limit = 0, sub2Limit = 0;

            // It's important that we don't change any fields of this object
            // prematurely. We set the following variables for the multiplier, grouping,
            // etc., and then only change the actual object fields if everything parses
            // correctly. This also lets us register the data from part 0 and ignore the
            // part 1, except for the prefix and suffix.
            StringBuilder prefix = new StringBuilder();
            StringBuilder suffix = new StringBuilder();
            int decimalPos = -1;
            int multpl = 1;
            int digitLeftCount = 0, zeroDigitCount = 0, digitRightCount = 0, sigDigitCount = 0;
            byte groupingCount = -1;
            byte groupingCount2 = -1;
            int padPos = -1;
            char padChar = 0;
            int incrementPos = -1;
            long incrementVal = 0;
            byte expDigits = -1;
            boolean expSignAlways = false;
            int currencySignCnt = 0;

            // The affix is either the prefix or the suffix.
            StringBuilder affix = prefix;

            int start = pos;

            PARTLOOP: for (; pos < pattern.length(); ++pos) {
                char ch = pattern.charAt(pos);
                switch (subpart) {
                case 0: // Pattern proper subpart (between prefix & suffix)
                    // Process the digits, decimal, and grouping characters. We record
                    // five pieces of information. We expect the digits to occur in the
                    // pattern ####00.00####, and we record the number of left digits,
                    // zero (central) digits, and right digits. The position of the last
                    // grouping character is recorded (should be somewhere within the
                    // first two blocks of characters), as is the position of the decimal
                    // point, if any (should be in the zero digits). If there is no
                    // decimal point, then there should be no right digits.
                    if (ch == digit) {
                        if (zeroDigitCount > 0 || sigDigitCount > 0) {
                            ++digitRightCount;
                        } else {
                            ++digitLeftCount;
                        }
                        if (groupingCount >= 0 && decimalPos < 0) {
                            ++groupingCount;
                        }
                    } else if ((ch >= zeroDigit && ch <= nineDigit) || ch == sigDigit) {
                        if (digitRightCount > 0) {
                            patternError("Unexpected '" + ch + '\'', pattern);
                        }
                        if (ch == sigDigit) {
                            ++sigDigitCount;
                        } else {
                            ++zeroDigitCount;
                            if (ch != zeroDigit) {
                                int p = digitLeftCount + zeroDigitCount + digitRightCount;
                                if (incrementPos >= 0) {
                                    while (incrementPos < p) {
                                        incrementVal *= 10;
                                        ++incrementPos;
                                    }
                                } else {
                                    incrementPos = p;
                                }
                                incrementVal += ch - zeroDigit;
                            }
                        }
                        if (groupingCount >= 0 && decimalPos < 0) {
                            ++groupingCount;
                        }
                    } else if (ch == groupingSeparator) {
                        // Bug 4212072 process the Localized pattern like
                        // "'Fr. '#'##0.05;'Fr.-'#'##0.05" (Locale="CH", groupingSeparator
                        // == QUOTE) [Richard/GCL]
                        if (ch == QUOTE && (pos + 1) < pattern.length()) {
                            char after = pattern.charAt(pos + 1);
                            if (!(after == digit || (after >= zeroDigit && after <= nineDigit))) {
                                // A quote outside quotes indicates either the opening
                                // quote or two quotes, which is a quote literal. That is,
                                // we have the first quote in 'do' or o''clock.
                                if (after == QUOTE) {
                                    ++pos;
                                    // Fall through to append(ch)
                                } else {
                                    if (groupingCount < 0) {
                                        subpart = 3; // quoted prefix subpart
                                    } else {
                                        // Transition to suffix subpart
                                        subpart = 2; // suffix subpart
                                        affix = suffix;
                                        sub0Limit = pos--;
                                    }
                                    continue;
                                }
                            }
                        }

                        if (decimalPos >= 0) {
                            patternError("Grouping separator after decimal", pattern);
                        }
                        groupingCount2 = groupingCount;
                        groupingCount = 0;
                    } else if (ch == decimalSeparator) {
                        if (decimalPos >= 0) {
                            patternError("Multiple decimal separators", pattern);
                        }
                        // Intentionally incorporate the digitRightCount, even though it
                        // is illegal for this to be > 0 at this point. We check pattern
                        // syntax below.
                        decimalPos = digitLeftCount + zeroDigitCount + digitRightCount;
                    } else {
                        if (pattern.regionMatches(pos, exponent, 0, exponent.length())) {
                            if (expDigits >= 0) {
                                patternError("Multiple exponential symbols", pattern);
                            }
                            if (groupingCount >= 0) {
                                patternError("Grouping separator in exponential", pattern);
                            }
                            pos += exponent.length();
                            // Check for positive prefix
                            if (pos < pattern.length() && pattern.charAt(pos) == plus) {
                                expSignAlways = true;
                                ++pos;
                            }
                            // Use lookahead to parse out the exponential part of the
                            // pattern, then jump into suffix subpart.
                            expDigits = 0;
                            while (pos < pattern.length() && pattern.charAt(pos) == zeroDigit) {
                                ++expDigits;
                                ++pos;
                            }

                            // 1. Require at least one mantissa pattern digit
                            // 2. Disallow "#+ @" in mantissa
                            // 3. Require at least one exponent pattern digit
                            if (((digitLeftCount + zeroDigitCount) < 1 &&
                                 (sigDigitCount + digitRightCount) < 1)
                                || (sigDigitCount > 0 && digitLeftCount > 0) || expDigits < 1) {
                                patternError("Malformed exponential", pattern);
                            }
                        }
                        // Transition to suffix subpart
                        subpart = 2; // suffix subpart
                        affix = suffix;
                        sub0Limit = pos--; // backup: for() will increment
                        continue;
                    }
                    break;
                case 1: // Prefix subpart
                case 2: // Suffix subpart
                    // Process the prefix / suffix characters Process unquoted characters
                    // seen in prefix or suffix subpart.

                    // Several syntax characters implicitly begins the next subpart if we
                    // are in the prefix; otherwise they are illegal if unquoted.
                    if (ch == digit || ch == groupingSeparator || ch == decimalSeparator
                            || (ch >= zeroDigit && ch <= nineDigit) || ch == sigDigit) {
                        // Any of these characters implicitly begins the
                        // next subpart if we are in the prefix
                        if (subpart == 1) { // prefix subpart
                            subpart = 0; // pattern proper subpart
                            sub0Start = pos--; // Reprocess this character
                            continue;
                        } else if (ch == QUOTE) {
                            // Bug 4212072 process the Localized pattern like
                            // "'Fr. '#'##0.05;'Fr.-'#'##0.05" (Locale="CH",
                            // groupingSeparator == QUOTE) [Richard/GCL]

                            // A quote outside quotes indicates either the opening quote
                            // or two quotes, which is a quote literal. That is, we have
                            // the first quote in 'do' or o''clock.
                            if ((pos + 1) < pattern.length() && pattern.charAt(pos + 1) == QUOTE) {
                                ++pos;
                                affix.append(ch);
                            } else {
                                subpart += 2; // open quote
                            }
                            continue;
                        }
                        patternError("Unquoted special character '" + ch + '\'', pattern);
                    } else if (ch == CURRENCY_SIGN) {
                        // Use lookahead to determine if the currency sign is
                        // doubled or not.
                        boolean doubled = (pos + 1) < pattern.length() &&
                            pattern.charAt(pos + 1) == CURRENCY_SIGN;

                        // Bug 4212072 To meet the need of expandAffix(String,
                        // StirngBuffer) [Richard/GCL]
                        if (doubled) {
                            ++pos; // Skip over the doubled character
                            affix.append(ch); // append two: one here, one below
                            if ((pos + 1) < pattern.length() &&
                                pattern.charAt(pos + 1) == CURRENCY_SIGN) {
                                ++pos; // Skip over the tripled character
                                affix.append(ch); // append again
                                currencySignCnt = CURRENCY_SIGN_COUNT_IN_PLURAL_FORMAT;
                            } else {
                                currencySignCnt = CURRENCY_SIGN_COUNT_IN_ISO_FORMAT;
                            }
                        } else {
                            currencySignCnt = CURRENCY_SIGN_COUNT_IN_SYMBOL_FORMAT;
                        }
                        // Fall through to append(ch)
                    } else if (ch == QUOTE) {
                        // A quote outside quotes indicates either the opening quote or
                        // two quotes, which is a quote literal. That is, we have the
                        // first quote in 'do' or o''clock.
                        if ((pos + 1) < pattern.length() && pattern.charAt(pos + 1) == QUOTE) {
                            ++pos;
                            affix.append(ch); // append two: one here, one below
                        } else {
                            subpart += 2; // open quote
                        }
                        // Fall through to append(ch)
                    } else if (ch == separator) {
                        // Don't allow separators in the prefix, and don't allow
                        // separators in the second pattern (part == 1).
                        if (subpart == 1 || part == 1) {
                            patternError("Unquoted special character '" + ch + '\'', pattern);
                        }
                        sub2Limit = pos++;
                        break PARTLOOP; // Go to next part
                    } else if (ch == percent || ch == perMill) {
                        // Next handle characters which are appended directly.
                        if (multpl != 1) {
                            patternError("Too many percent/permille characters", pattern);
                        }
                        multpl = (ch == percent) ? 100 : 1000;
                        // Convert to non-localized pattern
                        ch = (ch == percent) ? PATTERN_PERCENT : PATTERN_PER_MILLE;
                        // Fall through to append(ch)
                    } else if (ch == minus) {
                        // Convert to non-localized pattern
                        ch = PATTERN_MINUS_SIGN;
                        // Fall through to append(ch)
                    } else if (ch == padEscape) {
                        if (padPos >= 0) {
                            patternError("Multiple pad specifiers", pattern);
                        }
                        if ((pos + 1) == pattern.length()) {
                            patternError("Invalid pad specifier", pattern);
                        }
                        padPos = pos++; // Advance past pad char
                        padChar = pattern.charAt(pos);
                        continue;
                    }
                    affix.append(ch);
                    break;
                case 3: // Prefix subpart, in quote
                case 4: // Suffix subpart, in quote
                    // A quote within quotes indicates either the closing quote or two
                    // quotes, which is a quote literal. That is, we have the second quote
                    // in 'do' or 'don''t'.
                    if (ch == QUOTE) {
                        if ((pos + 1) < pattern.length() && pattern.charAt(pos + 1) == QUOTE) {
                            ++pos;
                            affix.append(ch);
                        } else {
                            subpart -= 2; // close quote
                        }
                        // Fall through to append(ch)
                    }
                    // NOTE: In ICU 2.2 there was code here to parse quoted percent and
                    // permille characters _within quotes_ and give them special
                    // meaning. This is incorrect, since quoted characters are literals
                    // without special meaning.
                    affix.append(ch);
                    break;
                }
            }

            if (subpart == 3 || subpart == 4) {
                patternError("Unterminated quote", pattern);
            }

            if (sub0Limit == 0) {
                sub0Limit = pattern.length();
            }

            if (sub2Limit == 0) {
                sub2Limit = pattern.length();
            }

            // Handle patterns with no '0' pattern character. These patterns are legal,
            // but must be recodified to make sense. "##.###" -> "#0.###". ".###" ->
            // ".0##".
            //
            // We allow patterns of the form "####" to produce a zeroDigitCount of zero
            // (got that?); although this seems like it might make it possible for
            // format() to produce empty strings, format() checks for this condition and
            // outputs a zero digit in this situation. Having a zeroDigitCount of zero
            // yields a minimum integer digits of zero, which allows proper round-trip
            // patterns. We don't want "#" to become "#0" when toPattern() is called (even
            // though that's what it really is, semantically).
            if (zeroDigitCount == 0 && sigDigitCount == 0 &&
                digitLeftCount > 0 && decimalPos >= 0) {
                // Handle "###.###" and "###." and ".###"
                int n = decimalPos;
                if (n == 0)
                    ++n; // Handle ".###"
                digitRightCount = digitLeftCount - n;
                digitLeftCount = n - 1;
                zeroDigitCount = 1;
            }

            // Do syntax checking on the digits, decimal points, and quotes.
            if ((decimalPos < 0 && digitRightCount > 0 && sigDigitCount == 0)
                || (decimalPos >= 0
                    && (sigDigitCount > 0
                        || decimalPos < digitLeftCount
                        || decimalPos > (digitLeftCount + zeroDigitCount)))
                || groupingCount == 0
                || groupingCount2 == 0
                || (sigDigitCount > 0 && zeroDigitCount > 0)
                || subpart > 2) { // subpart > 2 == unmatched quote
                patternError("Malformed pattern", pattern);
            }

            // Make sure pad is at legal position before or after affix.
            if (padPos >= 0) {
                if (padPos == start) {
                    padPos = PAD_BEFORE_PREFIX;
                } else if (padPos + 2 == sub0Start) {
                    padPos = PAD_AFTER_PREFIX;
                } else if (padPos == sub0Limit) {
                    padPos = PAD_BEFORE_SUFFIX;
                } else if (padPos + 2 == sub2Limit) {
                    padPos = PAD_AFTER_SUFFIX;
                } else {
                    patternError("Illegal pad position", pattern);
                }
            }

            if (part == 0) {
                // Set negative affixes temporarily to match the positive
                // affixes. Fix this up later after processing both parts.

                // Bug 4212072 To meet the need of expandAffix(String, StirngBuffer)
                // [Richard/GCL]
                posPrefixPattern = negPrefixPattern = prefix.toString();
                posSuffixPattern = negSuffixPattern = suffix.toString();

                useExponentialNotation = (expDigits >= 0);
                if (useExponentialNotation) {
                    minExponentDigits = expDigits;
                    exponentSignAlwaysShown = expSignAlways;
                }
                int digitTotalCount = digitLeftCount + zeroDigitCount + digitRightCount;
                // The effectiveDecimalPos is the position the decimal is at or would be
                // at if there is no decimal. Note that if decimalPos<0, then
                // digitTotalCount == digitLeftCount + zeroDigitCount.
                int effectiveDecimalPos = decimalPos >= 0 ? decimalPos : digitTotalCount;
                boolean useSigDig = (sigDigitCount > 0);
                setSignificantDigitsUsed(useSigDig);
                if (useSigDig) {
                    setMinimumSignificantDigits(sigDigitCount);
                    setMaximumSignificantDigits(sigDigitCount + digitRightCount);
                } else {
                    int minInt = effectiveDecimalPos - digitLeftCount;
                    setMinimumIntegerDigits(minInt);

                    // Upper limit on integer and fraction digits for a Java double
                    // [Richard/GCL]
                    setMaximumIntegerDigits(useExponentialNotation ? digitLeftCount + minInt :
                                            DOUBLE_INTEGER_DIGITS);
                    _setMaximumFractionDigits(decimalPos >= 0 ?
                                             (digitTotalCount - decimalPos) : 0);
                    setMinimumFractionDigits(decimalPos >= 0 ?
                                             (digitLeftCount + zeroDigitCount - decimalPos) : 0);
                }
                setGroupingUsed(groupingCount > 0);
                this.groupingSize = (groupingCount > 0) ? groupingCount : 0;
                this.groupingSize2 = (groupingCount2 > 0 && groupingCount2 != groupingCount)
                    ? groupingCount2 : 0;
                this.multiplier = multpl;
                setDecimalSeparatorAlwaysShown(decimalPos == 0 || decimalPos == digitTotalCount);
                if (padPos >= 0) {
                    padPosition = padPos;
                    formatWidth = sub0Limit - sub0Start; // to be fixed up below
                    pad = padChar;
                } else {
                    formatWidth = 0;
                }
                if (incrementVal != 0) {
                    // BigDecimal scale cannot be negative (even though this makes perfect
                    // sense), so we need to handle this.
                    int scale = incrementPos - effectiveDecimalPos;
                    roundingIncrementICU = BigDecimal.valueOf(incrementVal, scale > 0 ? scale : 0);
                    if (scale < 0) {
                        roundingIncrementICU = roundingIncrementICU.movePointRight(-scale);
                    }
                    roundingMode = BigDecimal.ROUND_HALF_EVEN;
                } else {
                    setRoundingIncrement((BigDecimal) null);
                }

                // Update currency sign count for the new pattern
                currencySignCount = currencySignCnt;
            } else {
                // Bug 4212072 To meet the need of expandAffix(String, StirngBuffer)
                // [Richard/GCL]
                negPrefixPattern = prefix.toString();
                negSuffixPattern = suffix.toString();
                gotNegative = true;
            }
        }


        // Bug 4140009 Process the empty pattern [Richard/GCL]
        if (pattern.length() == 0) {
            posPrefixPattern = posSuffixPattern = "";
            setMinimumIntegerDigits(0);
            setMaximumIntegerDigits(DOUBLE_INTEGER_DIGITS);
            setMinimumFractionDigits(0);
            _setMaximumFractionDigits(DOUBLE_FRACTION_DIGITS);
        }

        // If there was no negative pattern, or if the negative pattern is identical to
        // the positive pattern, then prepend the minus sign to the positive pattern to
        // form the negative pattern.

        // Bug 4212072 To meet the need of expandAffix(String, StirngBuffer) [Richard/GCL]

        if (!gotNegative ||
            (negPrefixPattern.equals(posPrefixPattern)
             && negSuffixPattern.equals(posSuffixPattern))) {
            negSuffixPattern = posSuffixPattern;
            negPrefixPattern = PATTERN_MINUS_SIGN + posPrefixPattern;
        }
        setLocale(null, null);
        // save the pattern
        formatPattern = pattern;

        // special handlings for currency instance
        if (currencySignCount != CURRENCY_SIGN_COUNT_ZERO) {
            // reset rounding increment and max/min fractional digits
            // by the currency
            Currency theCurrency = getCurrency();
            if (theCurrency != null) {
                setRoundingIncrement(theCurrency.getRoundingIncrement(currencyUsage));
                int d = theCurrency.getDefaultFractionDigits(currencyUsage);
                setMinimumFractionDigits(d);
                _setMaximumFractionDigits(d);
            }

            // initialize currencyPluralInfo if needed
            if (currencySignCount == CURRENCY_SIGN_COUNT_IN_PLURAL_FORMAT
                && currencyPluralInfo == null) {
                currencyPluralInfo = new CurrencyPluralInfo(symbols.getULocale());
            }
        }
        resetActualRounding();
    }


    private void patternError(String msg, String pattern) {
        throw new IllegalArgumentException(msg + " in pattern \"" + pattern + '"');
    }


    // Rewrite the following 4 "set" methods Upper limit on integer and fraction digits
    // for a Java double [Richard/GCL]

    /**
     * Sets the maximum number of digits allowed in the integer portion of a number. This
     * override limits the integer digit count to 2,000,000,000 to match ICU4C.
     *
     * @see NumberFormat#setMaximumIntegerDigits
     */
    @Override
    public void setMaximumIntegerDigits(int newValue) {
        // Android changed: Allow 2 billion integer digits.
        super.setMaximumIntegerDigits(Math.min(newValue, MAX_INTEGER_DIGITS));
    }

    /**
     * Sets the minimum number of digits allowed in the integer portion of a number. This
     * override limits the integer digit count to 309.
     *
     * @see NumberFormat#setMinimumIntegerDigits
     */
    @Override
    public void setMinimumIntegerDigits(int newValue) {
        super.setMinimumIntegerDigits(Math.min(newValue, DOUBLE_INTEGER_DIGITS));
    }

    /**
     * <strong>[icu]</strong> Returns the minimum number of significant digits that will be
     * displayed. This value has no effect unless {@link #areSignificantDigitsUsed()}
     * returns true.
     *
     * @return the fewest significant digits that will be shown
     */
    public int getMinimumSignificantDigits() {
        return minSignificantDigits;
    }

    /**
     * <strong>[icu]</strong> Returns the maximum number of significant digits that will be
     * displayed. This value has no effect unless {@link #areSignificantDigitsUsed()}
     * returns true.
     *
     * @return the most significant digits that will be shown
     */
    public int getMaximumSignificantDigits() {
        return maxSignificantDigits;
    }

    /**
     * <strong>[icu]</strong> Sets the minimum number of significant digits that will be displayed. If
     * <code>min</code> is less than one then it is set to one. If the maximum significant
     * digits count is less than <code>min</code>, then it is set to <code>min</code>.
     * This function also enables the use of significant digits by this formatter -
     * {@link #areSignificantDigitsUsed()} will return true.
     *
     * @param min the fewest significant digits to be shown
     */
    public void setMinimumSignificantDigits(int min) {
        if (min < 1) {
            min = 1;
        }
        // pin max sig dig to >= min
        int max = Math.max(maxSignificantDigits, min);
        minSignificantDigits = min;
        maxSignificantDigits = max;
        setSignificantDigitsUsed(true);
    }

    /**
     * <strong>[icu]</strong> Sets the maximum number of significant digits that will be displayed. If
     * <code>max</code> is less than one then it is set to one. If the minimum significant
     * digits count is greater than <code>max</code>, then it is set to <code>max</code>.
     * This function also enables the use of significant digits by this formatter -
     * {@link #areSignificantDigitsUsed()} will return true.
     *
     * @param max the most significant digits to be shown
     */
    public void setMaximumSignificantDigits(int max) {
        if (max < 1) {
            max = 1;
        }
        // pin min sig dig to 1..max
        int min = Math.min(minSignificantDigits, max);
        minSignificantDigits = min;
        maxSignificantDigits = max;
        setSignificantDigitsUsed(true);
    }

    /**
     * <strong>[icu]</strong> Returns true if significant digits are in use or false if integer and
     * fraction digit counts are in use.
     *
     * @return true if significant digits are in use
     */
    public boolean areSignificantDigitsUsed() {
        return useSignificantDigits;
    }

    /**
     * <strong>[icu]</strong> Sets whether significant digits are in use, or integer and fraction digit
     * counts are in use.
     *
     * @param useSignificantDigits true to use significant digits, or false to use integer
     * and fraction digit counts
     */
    public void setSignificantDigitsUsed(boolean useSignificantDigits) {
        this.useSignificantDigits = useSignificantDigits;
    }

    /**
     * Sets the <tt>Currency</tt> object used to display currency amounts. This takes
     * effect immediately, if this format is a currency format. If this format is not a
     * currency format, then the currency object is used if and when this object becomes a
     * currency format through the application of a new pattern.
     *
     * @param theCurrency new currency object to use. Must not be null.
     */
    @Override
    public void setCurrency(Currency theCurrency) {
        // If we are a currency format, then modify our affixes to
        // encode the currency symbol for the given currency in our
        // locale, and adjust the decimal digits and rounding for the
        // given currency.

        super.setCurrency(theCurrency);
        if (theCurrency != null) {
            String s = theCurrency.getName(symbols.getULocale(), Currency.SYMBOL_NAME, null);
            symbols.setCurrency(theCurrency);
            symbols.setCurrencySymbol(s);
        }

        if (currencySignCount != CURRENCY_SIGN_COUNT_ZERO) {
            if (theCurrency != null) {
                setRoundingIncrement(theCurrency.getRoundingIncrement(currencyUsage));
                int d = theCurrency.getDefaultFractionDigits(currencyUsage);
                setMinimumFractionDigits(d);
                setMaximumFractionDigits(d);
            }
            if (currencySignCount != CURRENCY_SIGN_COUNT_IN_PLURAL_FORMAT) {
                // This is not necessary for plural format type
                // because affixes will be resolved in subformat
                expandAffixes(null);
            }
        }
    }

    /**
     * Sets the <tt>Currency Usage</tt> object used to display currency.
     * This takes effect immediately, if this format is a
     * currency format.
     * @param newUsage new currency context object to use.
     */
    public void setCurrencyUsage(CurrencyUsage newUsage) {
        if (newUsage == null) {
            throw new NullPointerException("return value is null at method AAA");
        }
        currencyUsage = newUsage;
        Currency theCurrency = this.getCurrency();

        // We set rounding/digit based on currency context
        if (theCurrency != null) {
            setRoundingIncrement(theCurrency.getRoundingIncrement(currencyUsage));
            int d = theCurrency.getDefaultFractionDigits(currencyUsage);
            setMinimumFractionDigits(d);
            _setMaximumFractionDigits(d);
        }
    }

    /**
     * Returns the <tt>Currency Usage</tt> object used to display currency
     */
    public CurrencyUsage getCurrencyUsage() {
        return currencyUsage;
    }

    /**
     * Returns the currency in effect for this formatter. Subclasses should override this
     * method as needed. Unlike getCurrency(), this method should never return null.
     *
     * @deprecated This API is ICU internal only.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    @Override
    protected Currency getEffectiveCurrency() {
        Currency c = getCurrency();
        if (c == null) {
            c = Currency.getInstance(symbols.getInternationalCurrencySymbol());
        }
        return c;
    }

    /**
     * Sets the maximum number of digits allowed in the fraction portion of a number. This
     * override limits the fraction digit count to 340.
     *
     * @see NumberFormat#setMaximumFractionDigits
     */
    @Override
    public void setMaximumFractionDigits(int newValue) {
        _setMaximumFractionDigits(newValue);
        resetActualRounding();
    }

    /*
     * Internal method for DecimalFormat, setting maximum fractional digits
     * without triggering actual rounding recalculated.
     */
    private void _setMaximumFractionDigits(int newValue) {
        super.setMaximumFractionDigits(Math.min(newValue, DOUBLE_FRACTION_DIGITS));
    }

    /**
     * Sets the minimum number of digits allowed in the fraction portion of a number. This
     * override limits the fraction digit count to 340.
     *
     * @see NumberFormat#setMinimumFractionDigits
     */
    @Override
    public void setMinimumFractionDigits(int newValue) {
        super.setMinimumFractionDigits(Math.min(newValue, DOUBLE_FRACTION_DIGITS));
    }

    /**
     * Sets whether {@link #parse(String, ParsePosition)} returns BigDecimal. The
     * default value is false.
     *
     * @param value true if {@link #parse(String, ParsePosition)}
     * returns BigDecimal.
     */
    public void setParseBigDecimal(boolean value) {
        parseBigDecimal = value;
    }

    /**
     * Returns whether {@link #parse(String, ParsePosition)} returns BigDecimal.
     *
     * @return true if {@link #parse(String, ParsePosition)} returns BigDecimal.
     */
    public boolean isParseBigDecimal() {
        return parseBigDecimal;
    }

    /**
    * Set the maximum number of exponent digits when parsing a number.
    * If the limit is set too high, an OutOfMemoryException may be triggered.
    * The default value is 1000.
    * @param newValue the new limit
    */
    public void setParseMaxDigits(int newValue) {
        if (newValue > 0) {
            PARSE_MAX_EXPONENT = newValue;
        }
    }

    /**
    * Get the current maximum number of exponent digits when parsing a
    * number.
    * @return the maximum number of exponent digits for parsing
    */
    public int getParseMaxDigits() {
        return PARSE_MAX_EXPONENT;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        // Ticket#6449 Format.Field instances are not serializable. When
        // formatToCharacterIterator is called, attributes (ArrayList) stores
        // FieldPosition instances with NumberFormat.Field. Because NumberFormat.Field is
        // not serializable, we need to clear the contents of the list when writeObject is
        // called. We could remove the field or make it transient, but it will break
        // serialization compatibility.
        attributes.clear();

        stream.defaultWriteObject();
    }

    /**
     * First, read the default serializable fields from the stream. Then if
     * <code>serialVersionOnStream</code> is less than 1, indicating that the stream was
     * written by JDK 1.1, initialize <code>useExponentialNotation</code> to false, since
     * it was not present in JDK 1.1. Finally, set serialVersionOnStream back to the
     * maximum allowed value so that default serialization will work properly if this
     * object is streamed out again.
     */
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();

        // Bug 4185761 validate fields [Richard/GCL]

        // We only need to check the maximum counts because NumberFormat .readObject has
        // already ensured that the maximum is greater than the minimum count.

        // Commented for compatibility with previous version, and reserved for further use
        // if (getMaximumIntegerDigits() > DOUBLE_INTEGER_DIGITS ||
        // getMaximumFractionDigits() > DOUBLE_FRACTION_DIGITS) { throw new
        // InvalidObjectException("Digit count out of range"); }


        // Android changed: Allow 2 billion integer digits.
        // Truncate the maximumIntegerDigits to MAX_INTEGER_DIGITS and
        // maximumFractionDigits to DOUBLE_FRACTION_DIGITS

        if (getMaximumIntegerDigits() > MAX_INTEGER_DIGITS) {
            setMaximumIntegerDigits(MAX_INTEGER_DIGITS);
        }
        if (getMaximumFractionDigits() > DOUBLE_FRACTION_DIGITS) {
            _setMaximumFractionDigits(DOUBLE_FRACTION_DIGITS);
        }
        if (serialVersionOnStream < 2) {
            exponentSignAlwaysShown = false;
            setInternalRoundingIncrement(null);
            roundingMode = BigDecimal.ROUND_HALF_EVEN;
            formatWidth = 0;
            pad = ' ';
            padPosition = PAD_BEFORE_PREFIX;
            if (serialVersionOnStream < 1) {
                // Didn't have exponential fields
                useExponentialNotation = false;
            }
        }
        if (serialVersionOnStream < 3) {
            // Versions prior to 3 do not store a currency object.  Create one to match
            // the DecimalFormatSymbols object.
            setCurrencyForSymbols();
        }
        if (serialVersionOnStream < 4) {
            currencyUsage = CurrencyUsage.STANDARD;
        }
        serialVersionOnStream = currentSerialVersion;
        digitList = new DigitList();

        if (roundingIncrement != null) {
            setInternalRoundingIncrement(new BigDecimal(roundingIncrement));
        }
        resetActualRounding();
    }

    private void setInternalRoundingIncrement(BigDecimal value) {
        roundingIncrementICU = value;
        roundingIncrement = value == null ? null : value.toBigDecimal();
    }

    // ----------------------------------------------------------------------
    // INSTANCE VARIABLES
    // ----------------------------------------------------------------------

    private transient DigitList digitList = new DigitList();

    /**
     * The symbol used as a prefix when formatting positive numbers, e.g. "+".
     *
     * @serial
     * @see #getPositivePrefix
     */
    private String positivePrefix = "";

    /**
     * The symbol used as a suffix when formatting positive numbers. This is often an
     * empty string.
     *
     * @serial
     * @see #getPositiveSuffix
     */
    private String positiveSuffix = "";

    /**
     * The symbol used as a prefix when formatting negative numbers, e.g. "-".
     *
     * @serial
     * @see #getNegativePrefix
     */
    private String negativePrefix = "-";

    /**
     * The symbol used as a suffix when formatting negative numbers. This is often an
     * empty string.
     *
     * @serial
     * @see #getNegativeSuffix
     */
    private String negativeSuffix = "";

    /**
     * The prefix pattern for non-negative numbers. This variable corresponds to
     * <code>positivePrefix</code>.
     *
     * <p>This pattern is expanded by the method <code>expandAffix()</code> to
     * <code>positivePrefix</code> to update the latter to reflect changes in
     * <code>symbols</code>. If this variable is <code>null</code> then
     * <code>positivePrefix</code> is taken as a literal value that does not change when
     * <code>symbols</code> changes.  This variable is always <code>null</code> for
     * <code>DecimalFormat</code> objects older than stream version 2 restored from
     * stream.
     *
     * @serial
     */
    // [Richard/GCL]
    private String posPrefixPattern;

    /**
     * The suffix pattern for non-negative numbers. This variable corresponds to
     * <code>positiveSuffix</code>. This variable is analogous to
     * <code>posPrefixPattern</code>; see that variable for further documentation.
     *
     * @serial
     */
    // [Richard/GCL]
    private String posSuffixPattern;

    /**
     * The prefix pattern for negative numbers. This variable corresponds to
     * <code>negativePrefix</code>. This variable is analogous to
     * <code>posPrefixPattern</code>; see that variable for further documentation.
     *
     * @serial
     */
    // [Richard/GCL]
    private String negPrefixPattern;

    /**
     * The suffix pattern for negative numbers. This variable corresponds to
     * <code>negativeSuffix</code>. This variable is analogous to
     * <code>posPrefixPattern</code>; see that variable for further documentation.
     *
     * @serial
     */
    // [Richard/GCL]
    private String negSuffixPattern;

    /**
     * Formatter for ChoiceFormat-based currency names. If this field is not null, then
     * delegate to it to format currency symbols.
     * TODO: This is obsolete: Remove, and design extensible serialization. ICU ticket #12090.
     */
    private ChoiceFormat currencyChoice;

    /**
     * The multiplier for use in percent, permill, etc.
     *
     * @serial
     * @see #getMultiplier
     */
    private int multiplier = 1;

    /**
     * The number of digits between grouping separators in the integer portion of a
     * number. Must be greater than 0 if <code>NumberFormat.groupingUsed</code> is true.
     *
     * @serial
     * @see #getGroupingSize
     * @see NumberFormat#isGroupingUsed
     */
    private byte groupingSize = 3; // invariant, > 0 if useThousands

    /**
     * The secondary grouping size. This is only used for Hindi numerals, which use a
     * primary grouping of 3 and a secondary grouping of 2, e.g., "12,34,567". If this
     * value is less than 1, then secondary grouping is equal to the primary grouping.
     *
     */
    private byte groupingSize2 = 0;

    /**
     * If true, forces the decimal separator to always appear in a formatted number, even
     * if the fractional part of the number is zero.
     *
     * @serial
     * @see #isDecimalSeparatorAlwaysShown
     */
    private boolean decimalSeparatorAlwaysShown = false;

    /**
     * The <code>DecimalFormatSymbols</code> object used by this format. It contains the
     * symbols used to format numbers, e.g. the grouping separator, decimal separator, and
     * so on.
     *
     * @serial
     * @see #setDecimalFormatSymbols
     * @see DecimalFormatSymbols
     */
    private DecimalFormatSymbols symbols = null; // LIU new DecimalFormatSymbols();

    /**
     * True to use significant digits rather than integer and fraction digit counts.
     *
     * @serial
     */
    private boolean useSignificantDigits = false;

    /**
     * The minimum number of significant digits to show. Must be &gt;= 1 and &lt;=
     * maxSignificantDigits. Ignored unless useSignificantDigits == true.
     *
     * @serial
     */
    private int minSignificantDigits = 1;

    /**
     * The maximum number of significant digits to show. Must be &gt;=
     * minSignficantDigits. Ignored unless useSignificantDigits == true.
     *
     * @serial
     */
    private int maxSignificantDigits = 6;

    /**
     * True to force the use of exponential (i.e. scientific) notation
     * when formatting numbers.
     *
     *<p> Note that the JDK 1.2 public API provides no way to set this
     * field, even though it is supported by the implementation and
     * the stream format. The intent is that this will be added to the
     * API in the future.
     *
     * @serial
     */
    private boolean useExponentialNotation; // Newly persistent in JDK 1.2

    /**
     * The minimum number of digits used to display the exponent when a number is
     * formatted in exponential notation.  This field is ignored if
     * <code>useExponentialNotation</code> is not true.
     *
     * <p>Note that the JDK 1.2 public API provides no way to set this field, even though
     * it is supported by the implementation and the stream format. The intent is that
     * this will be added to the API in the future.
     *
     * @serial
     */
    private byte minExponentDigits; // Newly persistent in JDK 1.2

    /**
     * If true, the exponent is always prefixed with either the plus sign or the minus
     * sign. Otherwise, only negative exponents are prefixed with the minus sign. This has
     * no effect unless <code>useExponentialNotation</code> is true.
     *
     * @serial
     */
    private boolean exponentSignAlwaysShown = false;

    /**
     * The value to which numbers are rounded during formatting. For example, if the
     * rounding increment is 0.05, then 13.371 would be formatted as 13.350, assuming 3
     * fraction digits. Has the value <code>null</code> if rounding is not in effect, or a
     * positive value if rounding is in effect. Default value <code>null</code>.
     *
     * @serial
     */
    // Note: this is kept in sync with roundingIncrementICU.
    // it is only kept around to avoid a conversion when formatting a java.math.BigDecimal
    private java.math.BigDecimal roundingIncrement = null;

    /**
     * The value to which numbers are rounded during formatting. For example, if the
     * rounding increment is 0.05, then 13.371 would be formatted as 13.350, assuming 3
     * fraction digits. Has the value <code>null</code> if rounding is not in effect, or a
     * positive value if rounding is in effect. Default value <code>null</code>. WARNING:
     * the roundingIncrement value is the one serialized.
     *
     * @serial
     */
    private transient BigDecimal roundingIncrementICU = null;

    /**
     * The rounding mode. This value controls any rounding operations which occur when
     * applying a rounding increment or when reducing the number of fraction digits to
     * satisfy a maximum fraction digits limit. The value may assume any of the
     * <code>BigDecimal</code> rounding mode values. Default value
     * <code>BigDecimal.ROUND_HALF_EVEN</code>.
     *
     * @serial
     */
    private int roundingMode = BigDecimal.ROUND_HALF_EVEN;

    /**
     * Operations on <code>BigDecimal</code> numbers are controlled by a {@link
     * MathContext} object, which provides the context (precision and other information)
     * for the operation. The default <code>MathContext</code> settings are
     * <code>digits=0, form=PLAIN, lostDigits=false, roundingMode=ROUND_HALF_UP</code>;
     * these settings perform fixed point arithmetic with unlimited precision, as defined
     * for the original BigDecimal class in Java 1.1 and Java 1.2
     */
    // context for plain unlimited math
    private MathContext mathContext = new MathContext(0, MathContext.PLAIN);

    /**
     * The padded format width, or zero if there is no padding. Must be &gt;= 0. Default
     * value zero.
     *
     * @serial
     */
    private int formatWidth = 0;

    /**
     * The character used to pad the result of format to <code>formatWidth</code>, if
     * padding is in effect. Default value ' '.
     *
     * @serial
     */
    private char pad = ' ';

    /**
     * The position in the string at which the <code>pad</code> character will be
     * inserted, if padding is in effect.  Must have a value from
     * <code>PAD_BEFORE_PREFIX</code> to <code>PAD_AFTER_SUFFIX</code>. Default value
     * <code>PAD_BEFORE_PREFIX</code>.
     *
     * @serial
     */
    private int padPosition = PAD_BEFORE_PREFIX;

    /**
     * True if {@link #parse(String, ParsePosition)} to return BigDecimal rather than
     * Long, Double or BigDecimal except special values. This property is introduced for
     * J2SE 5 compatibility support.
     *
     * @serial
     * @see #setParseBigDecimal(boolean)
     * @see #isParseBigDecimal()
     */
    private boolean parseBigDecimal = false;

    /**
     * The currency usage for the NumberFormat(standard or cash usage).
     * It is used as STANDARD by default
     */
    private CurrencyUsage currencyUsage = CurrencyUsage.STANDARD;

    // ----------------------------------------------------------------------

    static final int currentSerialVersion = 4;

    /**
     * The internal serial version which says which version was written Possible values
     * are:
     *
     * <ul>
     *
     * <li><b>0</b> (default): versions before JDK 1.2
     *
     * <li><b>1</b>: version from JDK 1.2 and later, which includes the two new fields
     * <code>useExponentialNotation</code> and <code>minExponentDigits</code>.
     *
     * <li><b>2</b>: version on AlphaWorks, which adds roundingMode, formatWidth, pad,
     * padPosition, exponentSignAlwaysShown, roundingIncrement.
     *
     * <li><b>3</b>: ICU 2.2. Adds currency object.
     *
     * <li><b>4</b>: ICU 54. Adds currency usage(standard vs cash)
     *
     * </ul>
     *
     * @serial
     */
    private int serialVersionOnStream = currentSerialVersion;

    // ----------------------------------------------------------------------
    // CONSTANTS
    // ----------------------------------------------------------------------

    /**
     * <strong>[icu]</strong> Constant for {@link #getPadPosition()} and {@link #setPadPosition(int)} to
     * specify pad characters inserted before the prefix.
     *
     * @see #setPadPosition
     * @see #getPadPosition
     * @see #PAD_AFTER_PREFIX
     * @see #PAD_BEFORE_SUFFIX
     * @see #PAD_AFTER_SUFFIX
     */
    public static final int PAD_BEFORE_PREFIX = 0;

    /**
     * <strong>[icu]</strong> Constant for {@link #getPadPosition()} and {@link #setPadPosition(int)} to
     * specify pad characters inserted after the prefix.
     *
     * @see #setPadPosition
     * @see #getPadPosition
     * @see #PAD_BEFORE_PREFIX
     * @see #PAD_BEFORE_SUFFIX
     * @see #PAD_AFTER_SUFFIX
     */
    public static final int PAD_AFTER_PREFIX = 1;

    /**
     * <strong>[icu]</strong> Constant for {@link #getPadPosition()} and {@link #setPadPosition(int)} to
     * specify pad characters inserted before the suffix.
     *
     * @see #setPadPosition
     * @see #getPadPosition
     * @see #PAD_BEFORE_PREFIX
     * @see #PAD_AFTER_PREFIX
     * @see #PAD_AFTER_SUFFIX
     */
    public static final int PAD_BEFORE_SUFFIX = 2;

    /**
     * <strong>[icu]</strong> Constant for {@link #getPadPosition()} and {@link #setPadPosition(int)} to
     * specify pad characters inserted after the suffix.
     *
     * @see #setPadPosition
     * @see #getPadPosition
     * @see #PAD_BEFORE_PREFIX
     * @see #PAD_AFTER_PREFIX
     * @see #PAD_BEFORE_SUFFIX
     */
    public static final int PAD_AFTER_SUFFIX = 3;

    // Constants for characters used in programmatic (unlocalized) patterns.
    static final char PATTERN_ZERO_DIGIT = '0';
    static final char PATTERN_ONE_DIGIT = '1';
    static final char PATTERN_TWO_DIGIT = '2';
    static final char PATTERN_THREE_DIGIT = '3';
    static final char PATTERN_FOUR_DIGIT = '4';
    static final char PATTERN_FIVE_DIGIT = '5';
    static final char PATTERN_SIX_DIGIT = '6';
    static final char PATTERN_SEVEN_DIGIT = '7';
    static final char PATTERN_EIGHT_DIGIT = '8';
    static final char PATTERN_NINE_DIGIT = '9';
    static final char PATTERN_GROUPING_SEPARATOR = ',';
    static final char PATTERN_DECIMAL_SEPARATOR = '.';
    static final char PATTERN_DIGIT = '#';
    static final char PATTERN_SIGNIFICANT_DIGIT = '@';
    static final char PATTERN_EXPONENT = 'E';
    static final char PATTERN_PLUS_SIGN = '+';
    static final char PATTERN_MINUS_SIGN = '-';

    // Affix
    private static final char PATTERN_PER_MILLE = '\u2030';
    private static final char PATTERN_PERCENT = '%';
    static final char PATTERN_PAD_ESCAPE = '*';

    // Other
    private static final char PATTERN_SEPARATOR = ';';

    // Pad escape is package private to allow access by DecimalFormatSymbols.
    // Also plus sign. Also exponent.

    /**
     * The CURRENCY_SIGN is the standard Unicode symbol for currency. It is used in
     * patterns and substitued with either the currency symbol, or if it is doubled, with
     * the international currency symbol. If the CURRENCY_SIGN is seen in a pattern, then
     * the decimal separator is replaced with the monetary decimal separator.
     *
     * The CURRENCY_SIGN is not localized.
     */
    private static final char CURRENCY_SIGN = '\u00A4';

    private static final char QUOTE = '\'';

    /**
     * Upper limit on integer and fraction digits for a Java double [Richard/GCL]
     */
    static final int DOUBLE_INTEGER_DIGITS = 309;
    // Android changed: Allow 2 billion integer digits.
    // This change is necessary to stay feature-compatible in java.text.DecimalFormat which
    // used to be implemented using ICU4C (which has a 2 billion integer digits limit) and
    // is now implemented based on this class.
    static final int MAX_INTEGER_DIGITS = 2000000000;
    static final int DOUBLE_FRACTION_DIGITS = 340;

    /**
     * When someone turns on scientific mode, we assume that more than this number of
     * digits is due to flipping from some other mode that didn't restrict the maximum,
     * and so we force 1 integer digit. We don't bother to track and see if someone is
     * using exponential notation with more than this number, it wouldn't make sense
     * anyway, and this is just to make sure that someone turning on scientific mode with
     * default settings doesn't end up with lots of zeroes.
     */
    static final int MAX_SCIENTIFIC_INTEGER_DIGITS = 8;

    // Proclaim JDK 1.1 serial compatibility.
    private static final long serialVersionUID = 864413376551465018L;

    private ArrayList<FieldPosition> attributes = new ArrayList<FieldPosition>();

    // The following are used in currency format

    // -- triple currency sign char array
    // private static final char[] tripleCurrencySign = {0xA4, 0xA4, 0xA4};
    // -- triple currency sign string
    // private static final String tripleCurrencyStr = new String(tripleCurrencySign);
    //
    // -- default currency plural pattern char array
    // private static final char[] defaultCurrencyPluralPatternChar =
    //   {0, '.', '#', '#', ' ', 0xA4, 0xA4, 0xA4};
    // -- default currency plural pattern string
    // private static final String defaultCurrencyPluralPattern =
    //     new String(defaultCurrencyPluralPatternChar);

    // pattern used in this formatter
    private String formatPattern = "";
    // style is only valid when decimal formatter is constructed by
    // DecimalFormat(pattern, decimalFormatSymbol, style)
    private int style = NumberFormat.NUMBERSTYLE;
    /**
     * Represents whether this is a currency format, and which currency format style. 0:
     * not currency format type; 1: currency style -- symbol name, such as "$" for US
     * dollar. 2: currency style -- ISO name, such as USD for US dollar. 3: currency style
     * -- plural long name, such as "US Dollar" for "1.00 US Dollar", or "US Dollars" for
     * "3.00 US Dollars".
     */
    private int currencySignCount = CURRENCY_SIGN_COUNT_ZERO;

    /**
     * For parsing purposes, we need to remember all prefix patterns and suffix patterns
     * of every currency format pattern, including the pattern of the default currency
     * style, ISO currency style, and plural currency style. The patterns are set through
     * applyPattern. The following are used to represent the affix patterns in currency
     * plural formats.
     */
    private static final class AffixForCurrency {
        // negative prefix pattern
        private String negPrefixPatternForCurrency = null;
        // negative suffix pattern
        private String negSuffixPatternForCurrency = null;
        // positive prefix pattern
        private String posPrefixPatternForCurrency = null;
        // positive suffix pattern
        private String posSuffixPatternForCurrency = null;
        private final int patternType;

        public AffixForCurrency(String negPrefix, String negSuffix, String posPrefix,
                                String posSuffix, int type) {
            negPrefixPatternForCurrency = negPrefix;
            negSuffixPatternForCurrency = negSuffix;
            posPrefixPatternForCurrency = posPrefix;
            posSuffixPatternForCurrency = posSuffix;
            patternType = type;
        }

        public String getNegPrefix() {
            return negPrefixPatternForCurrency;
        }

        public String getNegSuffix() {
            return negSuffixPatternForCurrency;
        }

        public String getPosPrefix() {
            return posPrefixPatternForCurrency;
        }

        public String getPosSuffix() {
            return posSuffixPatternForCurrency;
        }

        public int getPatternType() {
            return patternType;
        }
    }

    // Affix pattern set for currency.  It is a set of AffixForCurrency, each element of
    // the set saves the negative prefix, negative suffix, positive prefix, and positive
    // suffix of a pattern.
    private transient Set<AffixForCurrency> affixPatternsForCurrency = null;

    // For currency parsing. Since currency parsing needs to parse against all currency
    // patterns, before the parsing, we need to set up the affix patterns for all currencies.
    private transient boolean isReadyForParsing = false;

    // Information needed for DecimalFormat to format/parse currency plural.
    private CurrencyPluralInfo currencyPluralInfo = null;

    /**
     * Unit is an immutable class for the textual representation of a unit, in
     * particular its prefix and suffix.
     *
     * @author rocketman
     *
     */
    static class Unit {
        private final String prefix;
        private final String suffix;

        public Unit(String prefix, String suffix) {
            this.prefix = prefix;
            this.suffix = suffix;
        }

        public void writeSuffix(StringBuffer toAppendTo) {
            toAppendTo.append(suffix);
        }

        public void writePrefix(StringBuffer toAppendTo) {
            toAppendTo.append(prefix);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Unit)) {
                return false;
            }
            Unit other = (Unit) obj;
            return prefix.equals(other.prefix) && suffix.equals(other.suffix);
        }
        @Override
        public String toString() {
            return prefix + "/" + suffix;
        }
    }

    static final Unit NULL_UNIT = new Unit("", "");

    // Note about rounding implementation
    //
    // The original design intended to skip rounding operation when roundingIncrement is not
    // set. However, rounding may need to occur when fractional digits exceed the width of
    // fractional part of pattern.
    //
    // DigitList class has built-in rounding mechanism, using ROUND_HALF_EVEN. This implementation
    // forces non-null roundingIncrement if the setting is other than ROUND_HALF_EVEN, otherwise,
    // when rounding occurs in DigitList by pattern's fractional digits' width, the result
    // does not match the rounding mode.
    //
    // Ideally, all rounding operation should be done in one place like ICU4C trunk does
    // (ICU4C rounding implementation was rewritten recently). This is intrim implemetation
    // to fix various issues. In the future, we should entire implementation of rounding
    // in this class, like ICU4C did.
    //
    // Once we fully implement rounding logic in DigitList, then following fields and methods
    // should be gone.

    private transient BigDecimal actualRoundingIncrementICU = null;
    private transient java.math.BigDecimal actualRoundingIncrement = null;

    /*
     * The actual rounding increment as a double.
     */
    private transient double roundingDouble = 0.0;

    /*
     * If the roundingDouble is the reciprocal of an integer (the most common case!), this
     * is set to be that integer.  Otherwise it is 0.0.
     */
    private transient double roundingDoubleReciprocal = 0.0;

    /*
     * Set roundingDouble, roundingDoubleReciprocal and actualRoundingIncrement
     * based on rounding mode and width of fractional digits. Whenever setting affecting
     * rounding mode, rounding increment and maximum width of fractional digits, then
     * this method must be called.
     *
     * roundingIncrementICU is the field storing the custom rounding increment value,
     * while actual rounding increment could be larger.
     */
    private void resetActualRounding() {
        if (roundingIncrementICU != null) {
            BigDecimal byWidth = getMaximumFractionDigits() > 0 ?
                    BigDecimal.ONE.movePointLeft(getMaximumFractionDigits()) : BigDecimal.ONE;
            if (roundingIncrementICU.compareTo(byWidth) >= 0) {
                actualRoundingIncrementICU = roundingIncrementICU;
            } else {
                actualRoundingIncrementICU = byWidth.equals(BigDecimal.ONE) ? null : byWidth;
            }
        } else {
            if (roundingMode == BigDecimal.ROUND_HALF_EVEN || isScientificNotation()) {
                // This rounding fix is irrelevant if mode is ROUND_HALF_EVEN as DigitList
                // does ROUND_HALF_EVEN for us.  This rounding fix won't work at all for
                // scientific notation.
                actualRoundingIncrementICU = null;
            } else {
                if (getMaximumFractionDigits() > 0) {
                    actualRoundingIncrementICU = BigDecimal.ONE.movePointLeft(getMaximumFractionDigits());
                }  else {
                    actualRoundingIncrementICU = BigDecimal.ONE;
                }
            }
        }

        if (actualRoundingIncrementICU == null) {
            setRoundingDouble(0.0d);
            actualRoundingIncrement = null;
        } else {
            setRoundingDouble(actualRoundingIncrementICU.doubleValue());
            actualRoundingIncrement = actualRoundingIncrementICU.toBigDecimal();
        }
    }

    static final double roundingIncrementEpsilon = 0.000000001;

    private void setRoundingDouble(double newValue) {
        roundingDouble = newValue;
        if (roundingDouble > 0.0d) {
            double rawRoundedReciprocal = 1.0d / roundingDouble;
            roundingDoubleReciprocal = Math.rint(rawRoundedReciprocal);
            if (Math.abs(rawRoundedReciprocal - roundingDoubleReciprocal) > roundingIncrementEpsilon) {
                roundingDoubleReciprocal = 0.0d;
            }
        } else {
            roundingDoubleReciprocal = 0.0d;
        }
    }
}

// eof
