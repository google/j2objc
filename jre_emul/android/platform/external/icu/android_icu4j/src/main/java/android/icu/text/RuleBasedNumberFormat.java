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

import java.math.BigInteger;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;

import android.icu.impl.ICUData;
import android.icu.impl.ICUDebug;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.PatternProps;
import android.icu.lang.UCharacter;
import android.icu.math.BigDecimal;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import android.icu.util.UResourceBundle;
import android.icu.util.UResourceBundleIterator;


/**
 * <p>A class that formats numbers according to a set of rules. This number formatter is
 * typically used for spelling out numeric values in words (e.g., 25,3476 as
 * &quot;twenty-five thousand three hundred seventy-six&quot; or &quot;vingt-cinq mille trois
 * cents soixante-seize&quot; or
 * &quot;funfundzwanzigtausenddreihundertsechsundsiebzig&quot;), but can also be used for
 * other complicated formatting tasks, such as formatting a number of seconds as hours,
 * minutes and seconds (e.g., 3,730 as &quot;1:02:10&quot;).</p>
 *
 * <p>The resources contain three predefined formatters for each locale: spellout, which
 * spells out a value in words (123 is &quot;one hundred twenty-three&quot;); ordinal, which
 * appends an ordinal suffix to the end of a numeral (123 is &quot;123rd&quot;); and
 * duration, which shows a duration in seconds as hours, minutes, and seconds (123 is
 * &quot;2:03&quot;).&nbsp; The client can also define more specialized <tt>RuleBasedNumberFormat</tt>s
 * by supplying programmer-defined rule sets.</p>
 *
 * <p>The behavior of a <tt>RuleBasedNumberFormat</tt> is specified by a textual description
 * that is either passed to the constructor as a <tt>String</tt> or loaded from a resource
 * bundle. In its simplest form, the description consists of a semicolon-delimited list of <em>rules.</em>
 * Each rule has a string of output text and a value or range of values it is applicable to.
 * In a typical spellout rule set, the first twenty rules are the words for the numbers from
 * 0 to 19:</p>
 *
 * <pre>zero; one; two; three; four; five; six; seven; eight; nine;
 * ten; eleven; twelve; thirteen; fourteen; fifteen; sixteen; seventeen; eighteen; nineteen;</pre>
 *
 * <p>For larger numbers, we can use the preceding set of rules to format the ones place, and
 * we only have to supply the words for the multiples of 10:</p>
 *
 * <pre>20: twenty[-&gt;&gt;];
 * 30: thirty{-&gt;&gt;];
 * 40: forty[-&gt;&gt;];
 * 50: fifty[-&gt;&gt;];
 * 60: sixty[-&gt;&gt;];
 * 70: seventy[-&gt;&gt;];
 * 80: eighty[-&gt;&gt;];
 * 90: ninety[-&gt;&gt;];</pre>
 *
 * <p>In these rules, the <em>base value</em> is spelled out explicitly and set off from the
 * rule's output text with a colon. The rules are in a sorted list, and a rule is applicable
 * to all numbers from its own base value to one less than the next rule's base value. The
 * &quot;&gt;&gt;&quot; token is called a <em>substitution</em> and tells the formatter to
 * isolate the number's ones digit, format it using this same set of rules, and place the
 * result at the position of the &quot;&gt;&gt;&quot; token. Text in brackets is omitted if
 * the number being formatted is an even multiple of 10 (the hyphen is a literal hyphen; 24
 * is &quot;twenty-four,&quot; not &quot;twenty four&quot;).</p>
 *
 * <p>For even larger numbers, we can actually look up several parts of the number in the
 * list:</p>
 *
 * <pre>100: &lt;&lt; hundred[ &gt;&gt;];</pre>
 *
 * <p>The &quot;&lt;&lt;&quot; represents a new kind of substitution. The &lt;&lt; isolates
 * the hundreds digit (and any digits to its left), formats it using this same rule set, and
 * places the result where the &quot;&lt;&lt;&quot; was. Notice also that the meaning of
 * &gt;&gt; has changed: it now refers to both the tens and the ones digits. The meaning of
 * both substitutions depends on the rule's base value. The base value determines the rule's <em>divisor,</em>
 * which is the highest power of 10 that is less than or equal to the base value (the user
 * can change this). To fill in the substitutions, the formatter divides the number being
 * formatted by the divisor. The integral quotient is used to fill in the &lt;&lt;
 * substitution, and the remainder is used to fill in the &gt;&gt; substitution. The meaning
 * of the brackets changes similarly: text in brackets is omitted if the value being
 * formatted is an even multiple of the rule's divisor. The rules are applied recursively, so
 * if a substitution is filled in with text that includes another substitution, that
 * substitution is also filled in.</p>
 *
 * <p>This rule covers values up to 999, at which point we add another rule:</p>
 *
 * <pre>1000: &lt;&lt; thousand[ &gt;&gt;];</pre>
 *
 * <p>Again, the meanings of the brackets and substitution tokens shift because the rule's
 * base value is a higher power of 10, changing the rule's divisor. This rule can actually be
 * used all the way up to 999,999. This allows us to finish out the rules as follows:</p>
 *
 * <pre>1,000,000: &lt;&lt; million[ &gt;&gt;];
 * 1,000,000,000: &lt;&lt; billion[ &gt;&gt;];
 * 1,000,000,000,000: &lt;&lt; trillion[ &gt;&gt;];
 * 1,000,000,000,000,000: OUT OF RANGE!;</pre>
 *
 * <p>Commas, periods, and spaces can be used in the base values to improve legibility and
 * are ignored by the rule parser. The last rule in the list is customarily treated as an
 * &quot;overflow rule,&quot; applying to everything from its base value on up, and often (as
 * in this example) being used to print out an error message or default representation.
 * Notice also that the size of the major groupings in large numbers is controlled by the
 * spacing of the rules: because in English we group numbers by thousand, the higher rules
 * are separated from each other by a factor of 1,000.</p>
 *
 * <p>To see how these rules actually work in practice, consider the following example:
 * Formatting 25,430 with this rule set would work like this:</p>
 *
 * <table border="0" width="630">
 *   <tr>
 *     <td style="width: 21;"></td>
 *     <td style="width: 257; vertical-align: top;"><strong>&lt;&lt; thousand &gt;&gt;</strong></td>
 *     <td style="width: 340; vertical-align: top;">[the rule whose base value is 1,000 is applicable to 25,340]</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 21;"></td>
 *     <td style="width: 257; vertical-align: top;"><strong>twenty-&gt;&gt;</strong> thousand &gt;&gt;</td>
 *     <td style="width: 340; vertical-align: top;">[25,340 over 1,000 is 25. The rule for 20 applies.]</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 21;"></td>
 *     <td style="width: 257; vertical-align: top;">twenty-<strong>five</strong> thousand &gt;&gt;</td>
 *     <td style="width: 340; vertical-align: top;">[25 mod 10 is 5. The rule for 5 is &quot;five.&quot;</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 21;"></td>
 *     <td style="width: 257; vertical-align: top;">twenty-five thousand <strong>&lt;&lt; hundred &gt;&gt;</strong></td>
 *     <td style="width: 340; vertical-align: top;">[25,340 mod 1,000 is 340. The rule for 100 applies.]</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 21;"></td>
 *     <td style="width: 257; vertical-align: top;">twenty-five thousand <strong>three</strong> hundred &gt;&gt;</td>
 *     <td style="width: 340; vertical-align: top;">[340 over 100 is 3. The rule for 3 is &quot;three.&quot;]</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 21;"></td>
 *     <td style="width: 257; vertical-align: top;">twenty-five thousand three hundred <strong>forty</strong></td>
 *     <td style="width: 340; vertical-align: top;">[340 mod 100 is 40. The rule for 40 applies. Since 40 divides
 *     evenly by 10, the hyphen and substitution in the brackets are omitted.]</td>
 *   </tr>
 * </table>
 *
 * <p>The above syntax suffices only to format positive integers. To format negative numbers,
 * we add a special rule:</p>
 *
 * <pre>-x: minus &gt;&gt;;</pre>
 *
 * <p>This is called a <em>negative-number rule,</em> and is identified by &quot;-x&quot;
 * where the base value would be. This rule is used to format all negative numbers. the
 * &gt;&gt; token here means &quot;find the number's absolute value, format it with these
 * rules, and put the result here.&quot;</p>
 *
 * <p>We also add a special rule called a <em>fraction rule </em>for numbers with fractional
 * parts:</p>
 *
 * <pre>x.x: &lt;&lt; point &gt;&gt;;</pre>
 *
 * <p>This rule is used for all positive non-integers (negative non-integers pass through the
 * negative-number rule first and then through this rule). Here, the &lt;&lt; token refers to
 * the number's integral part, and the &gt;&gt; to the number's fractional part. The
 * fractional part is formatted as a series of single-digit numbers (e.g., 123.456 would be
 * formatted as &quot;one hundred twenty-three point four five six&quot;).</p>
 *
 * <p>To see how this rule syntax is applied to various languages, examine the resource data.</p>
 *
 * <p>There is actually much more flexibility built into the rule language than the
 * description above shows. A formatter may own multiple rule sets, which can be selected by
 * the caller, and which can use each other to fill in their substitutions. Substitutions can
 * also be filled in with digits, using a DecimalFormat object. There is syntax that can be
 * used to alter a rule's divisor in various ways. And there is provision for much more
 * flexible fraction handling. A complete description of the rule syntax follows:</p>
 *
 * <hr>
 *
 * <p>The description of a <tt>RuleBasedNumberFormat</tt>'s behavior consists of one or more <em>rule
 * sets.</em> Each rule set consists of a name, a colon, and a list of <em>rules.</em> A rule
 * set name must begin with a % sign. Rule sets with names that begin with a single % sign
 * are <em>public:</em> the caller can specify that they be used to format and parse numbers.
 * Rule sets with names that begin with %% are <em>private:</em> they exist only for the use
 * of other rule sets. If a formatter only has one rule set, the name may be omitted.</p>
 *
 * <p>The user can also specify a special &quot;rule set&quot; named <tt>%%lenient-parse</tt>.
 * The body of <tt>%%lenient-parse</tt> isn't a set of number-formatting rules, but a <tt>RuleBasedCollator</tt>
 * description which is used to define equivalences for lenient parsing. For more information
 * on the syntax, see <tt>RuleBasedCollator</tt>. For more information on lenient parsing,
 * see <tt>setLenientParse()</tt>. <em>Note:</em> symbols that have syntactic meaning
 * in collation rules, such as '&amp;', have no particular meaning when appearing outside
 * of the <tt>lenient-parse</tt> rule set.</p>
 *
 * <p>The body of a rule set consists of an ordered, semicolon-delimited list of <em>rules.</em>
 * Internally, every rule has a base value, a divisor, rule text, and zero, one, or two <em>substitutions.</em>
 * These parameters are controlled by the description syntax, which consists of a <em>rule
 * descriptor,</em> a colon, and a <em>rule body.</em></p>
 *
 * <p>A rule descriptor can take one of the following forms (text in <em>italics</em> is the
 * name of a token):</p>
 *
 * <table border="0" width="100%">
 *   <tr>
 *     <td style="width: 5%; vertical-align: top;"></td>
 *     <td style="width: 8%; vertical-align: top;"><em>bv</em>:</td>
 *     <td valign="top"><em>bv</em> specifies the rule's base value. <em>bv</em> is a decimal
 *     number expressed using ASCII digits. <em>bv</em> may contain spaces, period, and commas,
 *     which are ignored. The rule's divisor is the highest power of 10 less than or equal to
 *     the base value.</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 5%; vertical-align: top;"></td>
 *     <td style="width: 8%; vertical-align: top;"><em>bv</em>/<em>rad</em>:</td>
 *     <td valign="top"><em>bv</em> specifies the rule's base value. The rule's divisor is the
 *     highest power of <em>rad</em> less than or equal to the base value.</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 5%; vertical-align: top;"></td>
 *     <td style="width: 8%; vertical-align: top;"><em>bv</em>&gt;:</td>
 *     <td valign="top"><em>bv</em> specifies the rule's base value. To calculate the divisor,
 *     let the radix be 10, and the exponent be the highest exponent of the radix that yields a
 *     result less than or equal to the base value. Every &gt; character after the base value
 *     decreases the exponent by 1. If the exponent is positive or 0, the divisor is the radix
 *     raised to the power of the exponent; otherwise, the divisor is 1.</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 5%; vertical-align: top;"></td>
 *     <td style="width: 8%; vertical-align: top;"><em>bv</em>/<em>rad</em>&gt;:</td>
 *     <td valign="top"><em>bv</em> specifies the rule's base value. To calculate the divisor,
 *     let the radix be <em>rad</em>, and the exponent be the highest exponent of the radix that
 *     yields a result less than or equal to the base value. Every &gt; character after the radix
 *     decreases the exponent by 1. If the exponent is positive or 0, the divisor is the radix
 *     raised to the power of the exponent; otherwise, the divisor is 1.</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 5%; vertical-align: top;"></td>
 *     <td style="width: 8%; vertical-align: top;">-x:</td>
 *     <td valign="top">The rule is a negative-number rule.</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 5%; vertical-align: top;"></td>
 *     <td style="width: 8%; vertical-align: top;">x.x:</td>
 *     <td valign="top">The rule is an <em>improper fraction rule</em>. If the full stop in
 *     the middle of the rule name is replaced with the decimal point
 *     that is used in the language or DecimalFormatSymbols, then that rule will
 *     have precedence when formatting and parsing this rule. For example, some
 *     languages use the comma, and can thus be written as x,x instead. For example,
 *     you can use "x.x: &lt;&lt; point &gt;&gt;;x,x: &lt;&lt; comma &gt;&gt;;" to
 *     handle the decimal point that matches the language's natural spelling of
 *     the punctuation of either the full stop or comma.</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 5%; vertical-align: top;"></td>
 *     <td style="width: 8%; vertical-align: top;">0.x:</td>
 *     <td valign="top">The rule is a <em>proper fraction rule</em>. If the full stop in
 *     the middle of the rule name is replaced with the decimal point
 *     that is used in the language or DecimalFormatSymbols, then that rule will
 *     have precedence when formatting and parsing this rule. For example, some
 *     languages use the comma, and can thus be written as 0,x instead. For example,
 *     you can use "0.x: point &gt;&gt;;0,x: comma &gt;&gt;;" to
 *     handle the decimal point that matches the language's natural spelling of
 *     the punctuation of either the full stop or comma</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 5%; vertical-align: top;"></td>
 *     <td style="width: 8%; vertical-align: top;">x.0:</td>
 *     <td valign="top">The rule is a <em>master rule</em>. If the full stop in
 *     the middle of the rule name is replaced with the decimal point
 *     that is used in the language or DecimalFormatSymbols, then that rule will
 *     have precedence when formatting and parsing this rule. For example, some
 *     languages use the comma, and can thus be written as x,0 instead. For example,
 *     you can use "x.0: &lt;&lt; point;x,0: &lt;&lt; comma;" to
 *     handle the decimal point that matches the language's natural spelling of
 *     the punctuation of either the full stop or comma</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 5%; vertical-align: top;"></td>
 *     <td style="width: 8%; vertical-align: top;">Inf:</td>
 *     <td style="vertical-align: top;">The rule for infinity.</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 5%; vertical-align: top;"></td>
 *     <td style="width: 8%; vertical-align: top;">NaN:</td>
 *     <td style="vertical-align: top;">The rule for an IEEE 754 NaN (not a number).</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 5%; vertical-align: top;"></td>
 *     <td style="width: 8%; vertical-align: top;"><em>nothing</em></td>
 *     <td style="vertical-align: top;">If the rule's rule descriptor is left out, the base value is one plus the
 *     preceding rule's base value (or zero if this is the first rule in the list) in a normal
 *     rule set.&nbsp; In a fraction rule set, the base value is the same as the preceding rule's
 *     base value.</td>
 *   </tr>
 * </table>
 *
 * <p>A rule set may be either a regular rule set or a <em>fraction rule set,</em> depending
 * on whether it is used to format a number's integral part (or the whole number) or a
 * number's fractional part. Using a rule set to format a rule's fractional part makes it a
 * fraction rule set.</p>
 *
 * <p>Which rule is used to format a number is defined according to one of the following
 * algorithms: If the rule set is a regular rule set, do the following:
 *
 * <ul>
 *   <li>If the rule set includes a master rule (and the number was passed in as a <tt>double</tt>),
 *     use the master rule.&nbsp; (If the number being formatted was passed in as a <tt>long</tt>,
 *     the master rule is ignored.)</li>
 *   <li>If the number is negative, use the negative-number rule.</li>
 *   <li>If the number has a fractional part and is greater than 1, use the improper fraction
 *     rule.</li>
 *   <li>If the number has a fractional part and is between 0 and 1, use the proper fraction
 *     rule.</li>
 *   <li>Binary-search the rule list for the rule with the highest base value less than or equal
 *     to the number. If that rule has two substitutions, its base value is not an even multiple
 *     of its divisor, and the number <em>is</em> an even multiple of the rule's divisor, use the
 *     rule that precedes it in the rule list. Otherwise, use the rule itself.</li>
 * </ul>
 *
 * <p>If the rule set is a fraction rule set, do the following:
 *
 * <ul>
 *   <li>Ignore negative-number and fraction rules.</li>
 *   <li>For each rule in the list, multiply the number being formatted (which will always be
 *     between 0 and 1) by the rule's base value. Keep track of the distance between the result
 *     the nearest integer.</li>
 *   <li>Use the rule that produced the result closest to zero in the above calculation. In the
 *     event of a tie or a direct hit, use the first matching rule encountered. (The idea here is
 *     to try each rule's base value as a possible denominator of a fraction. Whichever
 *     denominator produces the fraction closest in value to the number being formatted wins.) If
 *     the rule following the matching rule has the same base value, use it if the numerator of
 *     the fraction is anything other than 1; if the numerator is 1, use the original matching
 *     rule. (This is to allow singular and plural forms of the rule text without a lot of extra
 *     hassle.)</li>
 * </ul>
 *
 * <p>A rule's body consists of a string of characters terminated by a semicolon. The rule
 * may include zero, one, or two <em>substitution tokens,</em> and a range of text in
 * brackets. The brackets denote optional text (and may also include one or both
 * substitutions). The exact meanings of the substitution tokens, and under what conditions
 * optional text is omitted, depend on the syntax of the substitution token and the context.
 * The rest of the text in a rule body is literal text that is output when the rule matches
 * the number being formatted.</p>
 *
 * <p>A substitution token begins and ends with a <em>token character.</em> The token
 * character and the context together specify a mathematical operation to be performed on the
 * number being formatted. An optional <em>substitution descriptor </em>specifies how the
 * value resulting from that operation is used to fill in the substitution. The position of
 * the substitution token in the rule body specifies the location of the resultant text in
 * the original rule text.</p>
 *
 * <p>The meanings of the substitution token characters are as follows:</p>
 *
 * <table border="0" width="100%">
 *   <tr>
 *     <td style="width: 37;"></td>
 *     <td style="width: 23;">&gt;&gt;</td>
 *     <td style="width: 165; vertical-align: top;">in normal rule</td>
 *     <td>Divide the number by the rule's divisor and format the remainder</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 37;"></td>
 *     <td style="width: 23;"></td>
 *     <td style="width: 165; vertical-align: top;">in negative-number rule</td>
 *     <td>Find the absolute value of the number and format the result</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 37;"></td>
 *     <td style="width: 23;"></td>
 *     <td style="width: 165; vertical-align: top;">in fraction or master rule</td>
 *     <td>Isolate the number's fractional part and format it.</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 37;"></td>
 *     <td style="width: 23;"></td>
 *     <td style="width: 165; vertical-align: top;">in rule in fraction rule set</td>
 *     <td>Not allowed.</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 37;"></td>
 *     <td style="width: 23;">&gt;&gt;&gt;</td>
 *     <td style="width: 165; vertical-align: top;">in normal rule</td>
 *     <td>Divide the number by the rule's divisor and format the remainder,
 *       but bypass the normal rule-selection process and just use the
 *       rule that precedes this one in this rule list.</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 37;"></td>
 *     <td style="width: 23;"></td>
 *     <td style="width: 165; vertical-align: top;">in all other rules</td>
 *     <td>Not allowed.</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 37;"></td>
 *     <td style="width: 23;">&lt;&lt;</td>
 *     <td style="width: 165; vertical-align: top;">in normal rule</td>
 *     <td>Divide the number by the rule's divisor and format the quotient</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 37;"></td>
 *     <td style="width: 23;"></td>
 *     <td style="width: 165; vertical-align: top;">in negative-number rule</td>
 *     <td>Not allowed.</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 37;"></td>
 *     <td style="width: 23;"></td>
 *     <td style="width: 165; vertical-align: top;">in fraction or master rule</td>
 *     <td>Isolate the number's integral part and format it.</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 37;"></td>
 *     <td style="width: 23;"></td>
 *     <td style="width: 165; vertical-align: top;">in rule in fraction rule set</td>
 *     <td>Multiply the number by the rule's base value and format the result.</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 37;"></td>
 *     <td style="width: 23;">==</td>
 *     <td style="width: 165; vertical-align: top;">in all rule sets</td>
 *     <td>Format the number unchanged</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 37;"></td>
 *     <td style="width: 23;">[]</td>
 *     <td style="width: 165; vertical-align: top;">in normal rule</td>
 *     <td>Omit the optional text if the number is an even multiple of the rule's divisor</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 37;"></td>
 *     <td style="width: 23;"></td>
 *     <td style="width: 165; vertical-align: top;">in negative-number rule</td>
 *     <td>Not allowed.</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 37;"></td>
 *     <td style="width: 23;"></td>
 *     <td style="width: 165; vertical-align: top;">in improper-fraction rule</td>
 *     <td>Omit the optional text if the number is between 0 and 1 (same as specifying both an
 *     x.x rule and a 0.x rule)</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 37;"></td>
 *     <td style="width: 23;"></td>
 *     <td style="width: 165; vertical-align: top;">in master rule</td>
 *     <td>Omit the optional text if the number is an integer (same as specifying both an x.x
 *     rule and an x.0 rule)</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 37;"></td>
 *     <td style="width: 23;"></td>
 *     <td style="width: 165; vertical-align: top;">in proper-fraction rule</td>
 *     <td>Not allowed.</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 37;"></td>
 *     <td style="width: 23;"></td>
 *     <td style="width: 165; vertical-align: top;">in rule in fraction rule set</td>
 *     <td>Omit the optional text if multiplying the number by the rule's base value yields 1.</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 37;">$(cardinal,<i>plural syntax</i>)$</td>
 *     <td style="width: 23;"></td>
 *     <td style="width: 165; vertical-align: top;">in all rule sets</td>
 *     <td>This provides the ability to choose a word based on the number divided by the radix to the power of the
 *     exponent of the base value for the specified locale, which is normally equivalent to the &lt;&lt; value.
 *     This uses the cardinal plural rules from PluralFormat. All strings used in the plural format are treated
 *     as the same base value for parsing.</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 37;">$(ordinal,<i>plural syntax</i>)$</td>
 *     <td style="width: 23;"></td>
 *     <td style="width: 165; vertical-align: top;">in all rule sets</td>
 *     <td>This provides the ability to choose a word based on the number divided by the radix to the power of the
 *     exponent of the base value for the specified locale, which is normally equivalent to the &lt;&lt; value.
 *     This uses the ordinal plural rules from PluralFormat. All strings used in the plural format are treated
 *     as the same base value for parsing.</td>
 *   </tr>
 * </table>
 *
 * <p>The substitution descriptor (i.e., the text between the token characters) may take one
 * of three forms:</p>
 *
 * <table border="0" width="100%">
 *   <tr>
 *     <td style="width: 42;"></td>
 *     <td style="width: 166; vertical-align: top;">a rule set name</td>
 *     <td>Perform the mathematical operation on the number, and format the result using the
 *     named rule set.</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 42;"></td>
 *     <td style="width: 166; vertical-align: top;">a DecimalFormat pattern</td>
 *     <td>Perform the mathematical operation on the number, and format the result using a
 *     DecimalFormat with the specified pattern.&nbsp; The pattern must begin with 0 or #.</td>
 *   </tr>
 *   <tr>
 *     <td style="width: 42;"></td>
 *     <td style="width: 166; vertical-align: top;">nothing</td>
 *     <td>Perform the mathematical operation on the number, and format the result using the rule
 *     set containing the current rule, except:<ul>
 *       <li>You can't have an empty substitution descriptor with a == substitution.</li>
 *       <li>If you omit the substitution descriptor in a &gt;&gt; substitution in a fraction rule,
 *         format the result one digit at a time using the rule set containing the current rule.</li>
 *       <li>If you omit the substitution descriptor in a &lt;&lt; substitution in a rule in a
 *         fraction rule set, format the result using the default rule set for this formatter.</li>
 *     </ul>
 *     </td>
 *   </tr>
 * </table>
 *
 * <p>Whitespace is ignored between a rule set name and a rule set body, between a rule
 * descriptor and a rule body, or between rules. If a rule body begins with an apostrophe,
 * the apostrophe is ignored, but all text after it becomes significant (this is how you can
 * have a rule's rule text begin with whitespace). There is no escape function: the semicolon
 * is not allowed in rule set names or in rule text, and the colon is not allowed in rule set
 * names. The characters beginning a substitution token are always treated as the beginning
 * of a substitution token.</p>
 *
 * <p>See the resource data and the demo program for annotated examples of real rule sets
 * using these features.</p>
 *
 * @author Richard Gillam
 * @see NumberFormat
 * @see DecimalFormat
 * @see PluralFormat
 * @see PluralRules
 * @hide Only a subset of ICU is exposed in Android
 */
public class RuleBasedNumberFormat extends NumberFormat {

    //-----------------------------------------------------------------------
    // constants
    //-----------------------------------------------------------------------

    // Generated by serialver from JDK 1.4.1_01
    static final long serialVersionUID = -7664252765575395068L;

    /**
     * Selector code that tells the constructor to create a spellout formatter
     */
    public static final int SPELLOUT = 1;

    /**
     * Selector code that tells the constructor to create an ordinal formatter
     */
    public static final int ORDINAL = 2;

    /**
     * Selector code that tells the constructor to create a duration formatter
     */
    public static final int DURATION = 3;

    /**
     * Selector code that tells the constructor to create a numbering system formatter
     */
    public static final int NUMBERING_SYSTEM = 4;

    //-----------------------------------------------------------------------
    // data members
    //-----------------------------------------------------------------------

    /**
     * The formatter's rule sets.
     */
    private transient NFRuleSet[] ruleSets = null;

    /**
     * The formatter's rule names mapped to rule sets.
     */
    private transient Map<String, NFRuleSet> ruleSetsMap = null;

    /**
     * A pointer to the formatter's default rule set.  This is always included
     * in ruleSets.
     */
    private transient NFRuleSet defaultRuleSet = null;

    /**
     * The formatter's locale.  This is used to create DecimalFormatSymbols and
     * Collator objects.
     * @serial
     */
    private ULocale locale = null;

    /**
     * The formatter's rounding mode.
     * @serial
     */
    private int roundingMode = BigDecimal.ROUND_UNNECESSARY;

    /**
     * Collator to be used in lenient parsing.  This variable is lazy-evaluated:
     * the collator is actually created the first time the client does a parse
     * with lenient-parse mode turned on.
     */
    private transient RbnfLenientScannerProvider scannerProvider = null;

    // flag to mark whether we've previously looked for a scanner and failed
    private transient boolean lookedForScanner;

    /**
     * The DecimalFormatSymbols object that any DecimalFormat objects this
     * formatter uses should use.  This variable is lazy-evaluated: it isn't
     * filled in if the rule set never uses a DecimalFormat pattern.
     */
    private transient DecimalFormatSymbols decimalFormatSymbols = null;

    /**
     * The NumberFormat used when lenient parsing numbers.  This needs to reflect
     * the locale.  This is lazy-evaluated, like decimalFormatSymbols.  It is
     * here so it can be shared by different NFSubstitutions.
     */
    private transient DecimalFormat decimalFormat = null;

    /**
     * The rule used when dealing with infinity. This is lazy-evaluated, and derived from decimalFormat.
     * It is here so it can be shared by different NFRuleSets.
     */
    private transient NFRule defaultInfinityRule = null;

    /**
     * The rule used when dealing with IEEE 754 NaN. This is lazy-evaluated, and derived from decimalFormat.
     * It is here so it can be shared by different NFRuleSets.
     */
    private transient NFRule defaultNaNRule = null;

    /**
     * Flag specifying whether lenient parse mode is on or off.  Off by default.
     * @serial
     */
    private boolean lenientParse = false;

    /**
     * If the description specifies lenient-parse rules, they're stored here until
     * the collator is created.
     */
    private transient String lenientParseRules;

    /**
     * If the description specifies post-process rules, they're stored here until
     * post-processing is required.
     */
    private transient String postProcessRules;

    /**
     * Post processor lazily constructed from the postProcessRules.
     */
    private transient RBNFPostProcessor postProcessor;

    /**
     * Localizations for rule set names.
     * @serial
     */
    private Map<String, String[]> ruleSetDisplayNames;

    /**
     * The public rule set names;
     * @serial
     */
    private String[] publicRuleSetNames;

    /**
     * Data for handling context-based capitalization
     */
    private boolean capitalizationInfoIsSet = false;
    private boolean capitalizationForListOrMenu = false;
    private boolean capitalizationForStandAlone = false;
    private transient BreakIterator capitalizationBrkIter = null;


    private static final boolean DEBUG  =  ICUDebug.enabled("rbnf");

    //-----------------------------------------------------------------------
    // constructors
    //-----------------------------------------------------------------------

    /**
     * Creates a RuleBasedNumberFormat that behaves according to the description
     * passed in.  The formatter uses the default <code>FORMAT</code> locale.
     * @param description A description of the formatter's desired behavior.
     * See the class documentation for a complete explanation of the description
     * syntax.
     * @see Category#FORMAT
     */
    public RuleBasedNumberFormat(String description) {
        locale = ULocale.getDefault(Category.FORMAT);
        init(description, null);
    }

    /**
     * Creates a RuleBasedNumberFormat that behaves according to the description
     * passed in.  The formatter uses the default <code>FORMAT</code> locale.
     * <p>
     * The localizations data provides information about the public
     * rule sets and their localized display names for different
     * locales. The first element in the list is an array of the names
     * of the public rule sets.  The first element in this array is
     * the initial default ruleset.  The remaining elements in the
     * list are arrays of localizations of the names of the public
     * rule sets.  Each of these is one longer than the initial array,
     * with the first String being the ULocale ID, and the remaining
     * Strings being the localizations of the rule set names, in the
     * same order as the initial array.
     * @param description A description of the formatter's desired behavior.
     * See the class documentation for a complete explanation of the description
     * syntax.
     * @param localizations a list of localizations for the rule set
     * names in the description.
     * @see Category#FORMAT
     */
    public RuleBasedNumberFormat(String description, String[][] localizations) {
        locale = ULocale.getDefault(Category.FORMAT);
        init(description, localizations);
    }

    /**
     * Creates a RuleBasedNumberFormat that behaves according to the description
     * passed in.  The formatter uses the specified locale to determine the
     * characters to use when formatting in numerals, and to define equivalences
     * for lenient parsing.
     * @param description A description of the formatter's desired behavior.
     * See the class documentation for a complete explanation of the description
     * syntax.
     * @param locale A locale, which governs which characters are used for
     * formatting values in numerals, and which characters are equivalent in
     * lenient parsing.
     */
    public RuleBasedNumberFormat(String description, Locale locale) {
        this(description, ULocale.forLocale(locale));
    }

    /**
     * Creates a RuleBasedNumberFormat that behaves according to the description
     * passed in.  The formatter uses the specified locale to determine the
     * characters to use when formatting in numerals, and to define equivalences
     * for lenient parsing.
     * @param description A description of the formatter's desired behavior.
     * See the class documentation for a complete explanation of the description
     * syntax.
     * @param locale A locale, which governs which characters are used for
     * formatting values in numerals, and which characters are equivalent in
     * lenient parsing.
     */
    public RuleBasedNumberFormat(String description, ULocale locale) {
        this.locale = locale;
        init(description, null);
    }

    /**
     * Creates a RuleBasedNumberFormat that behaves according to the description
     * passed in.  The formatter uses the specified locale to determine the
     * characters to use when formatting in numerals, and to define equivalences
     * for lenient parsing.
     * <p>
     * The localizations data provides information about the public
     * rule sets and their localized display names for different
     * locales. The first element in the list is an array of the names
     * of the public rule sets.  The first element in this array is
     * the initial default ruleset.  The remaining elements in the
     * list are arrays of localizations of the names of the public
     * rule sets.  Each of these is one longer than the initial array,
     * with the first String being the ULocale ID, and the remaining
     * Strings being the localizations of the rule set names, in the
     * same order as the initial array.
     * @param description A description of the formatter's desired behavior.
     * See the class documentation for a complete explanation of the description
     * syntax.
     * @param localizations a list of localizations for the rule set names in the description.
     * @param locale A ULocale that governs which characters are used for
     * formatting values in numerals, and determines which characters are equivalent in
     * lenient parsing.
     */
    public RuleBasedNumberFormat(String description, String[][] localizations, ULocale locale) {
        this.locale = locale;
        init(description, localizations);
    }

    /**
     * Creates a RuleBasedNumberFormat from a predefined description.  The selector
     * code chooses among three possible predefined formats: spellout, ordinal,
     * and duration.
     * @param locale The locale for the formatter.
     * @param format A selector code specifying which kind of formatter to create for that
     * locale.  There are three legal values: SPELLOUT, which creates a formatter that
     * spells out a value in words in the desired language, ORDINAL, which attaches
     * an ordinal suffix from the desired language to the end of a number (e.g. "123rd"),
     * and DURATION, which formats a duration in seconds as hours, minutes, and seconds.
     */
    public RuleBasedNumberFormat(Locale locale, int format) {
        this(ULocale.forLocale(locale), format);
    }

    /**
     * Creates a RuleBasedNumberFormat from a predefined description.  The selector
     * code chooses among three possible predefined formats: spellout, ordinal,
     * and duration.
     * @param locale The locale for the formatter.
     * @param format A selector code specifying which kind of formatter to create for that
     * locale.  There are four legal values: SPELLOUT, which creates a formatter that
     * spells out a value in words in the desired language, ORDINAL, which attaches
     * an ordinal suffix from the desired language to the end of a number (e.g. "123rd"),
     * DURATION, which formats a duration in seconds as hours, minutes, and seconds, and
     * NUMBERING_SYSTEM, which is used to invoke rules for alternate numbering
     * systems such as the Hebrew numbering system, or for Roman numerals, etc..
     */
    public RuleBasedNumberFormat(ULocale locale, int format) {
        this.locale = locale;

        ICUResourceBundle bundle = (ICUResourceBundle)UResourceBundle.
            getBundleInstance(ICUData.ICU_RBNF_BASE_NAME, locale);

        // TODO: determine correct actual/valid locale.  Note ambiguity
        // here -- do actual/valid refer to pattern, DecimalFormatSymbols,
        // or Collator?
        ULocale uloc = bundle.getULocale();
        setLocale(uloc, uloc);

        StringBuilder description = new StringBuilder();
        String[][] localizations = null;

        try {
            ICUResourceBundle rules = bundle.getWithFallback("RBNFRules/"+rulenames[format-1]);
            UResourceBundleIterator it = rules.getIterator();
            while (it.hasNext()) {
               description.append(it.nextString());
            }
        }
        catch (MissingResourceException e1) {
        }

        // We use findTopLevel() instead of get() because
        // it's faster when we know that it's usually going to fail.
        UResourceBundle locNamesBundle = bundle.findTopLevel(locnames[format - 1]);
        if (locNamesBundle != null) {
            localizations = new String[locNamesBundle.getSize()][];
            for (int i = 0; i < localizations.length; ++i) {
                localizations[i] = locNamesBundle.get(i).getStringArray();
            }
        }
        // else there are no localized names. It's not that important.

        init(description.toString(), localizations);
    }

    private static final String[] rulenames = {
        "SpelloutRules", "OrdinalRules", "DurationRules", "NumberingSystemRules",
    };
    private static final String[] locnames = {
        "SpelloutLocalizations", "OrdinalLocalizations", "DurationLocalizations", "NumberingSystemLocalizations",
    };

    /**
     * Creates a RuleBasedNumberFormat from a predefined description.  Uses the
     * default <code>FORMAT</code> locale.
     * @param format A selector code specifying which kind of formatter to create.
     * There are three legal values: SPELLOUT, which creates a formatter that spells
     * out a value in words in the default locale's language, ORDINAL, which attaches
     * an ordinal suffix from the default locale's language to a numeral, and
     * DURATION, which formats a duration in seconds as hours, minutes, and seconds always rounding down.
     * or NUMBERING_SYSTEM, which is used for alternate numbering systems such as Hebrew.
     * @see Category#FORMAT
     */
    public RuleBasedNumberFormat(int format) {
        this(ULocale.getDefault(Category.FORMAT), format);
    }

    //-----------------------------------------------------------------------
    // boilerplate
    //-----------------------------------------------------------------------

    /**
     * Duplicates this formatter.
     * @return A RuleBasedNumberFormat that is equal to this one.
     */
    @Override
    public Object clone() {
        return super.clone();
    }

    /**
     * Tests two RuleBasedNumberFormats for equality.
     * @param that The formatter to compare against this one.
     * @return true if the two formatters have identical behavior.
     */
    @Override
    public boolean equals(Object that) {
        // if the other object isn't a RuleBasedNumberFormat, that's
        // all we need to know
        // Test for capitalization info equality is adequately handled
        // by the NumberFormat test for capitalizationSetting equality;
        // the info here is just derived from that.
        if (!(that instanceof RuleBasedNumberFormat)) {
            return false;
        } else {
            // cast the other object's pointer to a pointer to a
            // RuleBasedNumberFormat
            RuleBasedNumberFormat that2 = (RuleBasedNumberFormat)that;

            // compare their locales and lenient-parse modes
            if (!locale.equals(that2.locale) || lenientParse != that2.lenientParse) {
                return false;
            }

            // if that succeeds, then compare their rule set lists
            if (ruleSets.length != that2.ruleSets.length) {
                return false;
            }
            for (int i = 0; i < ruleSets.length; i++) {
                if (!ruleSets[i].equals(that2.ruleSets[i])) {
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * Mock implementation of hashCode(). This implementation always returns a constant
     * value. When Java assertion is enabled, this method triggers an assertion failure.
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Override
    @Deprecated
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Generates a textual description of this formatter.
     * @return a String containing a rule set that will produce a RuleBasedNumberFormat
     * with identical behavior to this one.  This won't necessarily be identical
     * to the rule set description that was originally passed in, but will produce
     * the same result.
     */
    @Override
    public String toString() {

        // accumulate the descriptions of all the rule sets in a
        // StringBuffer, then cast it to a String and return it
        StringBuilder result = new StringBuilder();
        for (NFRuleSet ruleSet : ruleSets) {
            result.append(ruleSet.toString());
        }
        return result.toString();
    }

    /**
     * Writes this object to a stream.
     * @param out The stream to write to.
     */
    private void writeObject(java.io.ObjectOutputStream out)
        throws java.io.IOException {
        // we just write the textual description to the stream, so we
        // have an implementation-independent streaming format
        out.writeUTF(this.toString());
        out.writeObject(this.locale);
        out.writeInt(this.roundingMode);
    }

    /**
     * Reads this object in from a stream.
     * @param in The stream to read from.
     */
    private void readObject(java.io.ObjectInputStream in)
        throws java.io.IOException {

        // read the description in from the stream
        String description = in.readUTF();
        ULocale loc;

        try {
            loc = (ULocale) in.readObject();
        } catch (Exception e) {
            loc = ULocale.getDefault(Category.FORMAT);
        }
        try {
            roundingMode = in.readInt();
        } catch (Exception ignored) {
        }

        // build a brand-new RuleBasedNumberFormat from the description,
        // then steal its substructure.  This object's substructure and
        // the temporary RuleBasedNumberFormat drop on the floor and
        // get swept up by the garbage collector
        RuleBasedNumberFormat temp = new RuleBasedNumberFormat(description, loc);
        ruleSets = temp.ruleSets;
        ruleSetsMap = temp.ruleSetsMap;
        defaultRuleSet = temp.defaultRuleSet;
        publicRuleSetNames = temp.publicRuleSetNames;
        decimalFormatSymbols = temp.decimalFormatSymbols;
        decimalFormat = temp.decimalFormat;
        locale = temp.locale;
        defaultInfinityRule = temp.defaultInfinityRule;
        defaultNaNRule = temp.defaultNaNRule;
    }


    //-----------------------------------------------------------------------
    // public API functions
    //-----------------------------------------------------------------------

    /**
     * Returns a list of the names of all of this formatter's public rule sets.
     * @return A list of the names of all of this formatter's public rule sets.
     */
    public String[] getRuleSetNames() {
        return publicRuleSetNames.clone();
    }

    /**
     * Return a list of locales for which there are locale-specific display names
     * for the rule sets in this formatter.  If there are no localized display names, return null.
     * @return an array of the ULocales for which there is rule set display name information
     */
    public ULocale[] getRuleSetDisplayNameLocales() {
        if (ruleSetDisplayNames != null) {
            Set<String> s = ruleSetDisplayNames.keySet();
            String[] locales = s.toArray(new String[s.size()]);
            Arrays.sort(locales, String.CASE_INSENSITIVE_ORDER);
            ULocale[] result = new ULocale[locales.length];
            for (int i = 0; i < locales.length; ++i) {
                result[i] = new ULocale(locales[i]);
            }
            return result;
        }
        return null;
    }

    private String[] getNameListForLocale(ULocale loc) {
        if (loc != null && ruleSetDisplayNames != null) {
            String[] localeNames = { loc.getBaseName(), ULocale.getDefault(Category.DISPLAY).getBaseName() };
            for (String lname : localeNames) {
                while (lname.length() > 0) {
                    String[] names = ruleSetDisplayNames.get(lname);
                    if (names != null) {
                        return names;
                    }
                    lname = ULocale.getFallback(lname);
                }
            }
        }
        return null;
    }

    /**
     * Return the rule set display names for the provided locale.  These are in the same order
     * as those returned by getRuleSetNames.  The locale is matched against the locales for
     * which there is display name data, using normal fallback rules.  If no locale matches,
     * the default display names are returned.  (These are the internal rule set names minus
     * the leading '%'.)
     * @return an array of the locales that have display name information
     * @see #getRuleSetNames
     */
    public String[] getRuleSetDisplayNames(ULocale loc) {
        String[] names = getNameListForLocale(loc);
        if (names != null) {
            return names.clone();
        }
        names = getRuleSetNames();
        for (int i = 0; i < names.length; ++i) {
            names[i] = names[i].substring(1);
        }
        return names;
    }

    /**
     * Return the rule set display names for the current default <code>DISPLAY</code> locale.
     * @return an array of the display names
     * @see #getRuleSetDisplayNames(ULocale)
     * @see Category#DISPLAY
     */
    public String[] getRuleSetDisplayNames() {
        return getRuleSetDisplayNames(ULocale.getDefault(Category.DISPLAY));
    }

    /**
     * Return the rule set display name for the provided rule set and locale.
     * The locale is matched against the locales for which there is display name data, using
     * normal fallback rules.  If no locale matches, the default display name is returned.
     * @return the display name for the rule set
     * @see #getRuleSetDisplayNames
     * @throws IllegalArgumentException if ruleSetName is not a valid rule set name for this format
     */
    public String getRuleSetDisplayName(String ruleSetName, ULocale loc) {
        String[] rsnames = publicRuleSetNames;
        for (int ix = 0; ix < rsnames.length; ++ix) {
            if (rsnames[ix].equals(ruleSetName)) {
                String[] names = getNameListForLocale(loc);
                if (names != null) {
                    return names[ix];
                }
                return rsnames[ix].substring(1);
            }
        }
        throw new IllegalArgumentException("unrecognized rule set name: " + ruleSetName);
    }

    /**
     * Return the rule set display name for the provided rule set in the current default <code>DISPLAY</code> locale.
     * @return the display name for the rule set
     * @see #getRuleSetDisplayName(String,ULocale)
     * @see Category#DISPLAY
     */
    public String getRuleSetDisplayName(String ruleSetName) {
        return getRuleSetDisplayName(ruleSetName, ULocale.getDefault(Category.DISPLAY));
    }

    /**
     * Formats the specified number according to the specified rule set.
     * @param number The number to format.
     * @param ruleSet The name of the rule set to format the number with.
     * This must be the name of a valid public rule set for this formatter.
     * @return A textual representation of the number.
     */
    public String format(double number, String ruleSet) throws IllegalArgumentException {
        if (ruleSet.startsWith("%%")) {
            throw new IllegalArgumentException("Can't use internal rule set");
        }
        return adjustForContext(format(number, findRuleSet(ruleSet)));
    }

    /**
     * Formats the specified number according to the specified rule set.
     * (If the specified rule set specifies a master ["x.0"] rule, this function
     * ignores it.  Convert the number to a double first if you ned it.)  This
     * function preserves all the precision in the long-- it doesn't convert it
     * to a double.
     * @param number The number to format.
     * @param ruleSet The name of the rule set to format the number with.
     * This must be the name of a valid public rule set for this formatter.
     * @return A textual representation of the number.
     */
    public String format(long number, String ruleSet) throws IllegalArgumentException {
        if (ruleSet.startsWith("%%")) {
            throw new IllegalArgumentException("Can't use internal rule set");
        }
        return adjustForContext(format(number, findRuleSet(ruleSet)));
    }

    /**
     * Formats the specified number using the formatter's default rule set.
     * (The default rule set is the last public rule set defined in the description.)
     * @param number The number to format.
     * @param toAppendTo A StringBuffer that the result should be appended to.
     * @param ignore This function doesn't examine or update the field position.
     * @return toAppendTo
     */
    @Override
    public StringBuffer format(double number,
                               StringBuffer toAppendTo,
                               FieldPosition ignore) {
        // this is one of the inherited format() methods.  Since it doesn't
        // have a way to select the rule set to use, it just uses the
        // default one
        // Note, the BigInteger/BigDecimal methods below currently go through this.
        if (toAppendTo.length() == 0) {
            toAppendTo.append(adjustForContext(format(number, defaultRuleSet)));
        } else {
            // appending to other text, don't capitalize
            toAppendTo.append(format(number, defaultRuleSet));
        }
        return toAppendTo;
    }

    /**
     * Formats the specified number using the formatter's default rule set.
     * (The default rule set is the last public rule set defined in the description.)
     * (If the specified rule set specifies a master ["x.0"] rule, this function
     * ignores it.  Convert the number to a double first if you ned it.)  This
     * function preserves all the precision in the long-- it doesn't convert it
     * to a double.
     * @param number The number to format.
     * @param toAppendTo A StringBuffer that the result should be appended to.
     * @param ignore This function doesn't examine or update the field position.
     * @return toAppendTo
     */
    @Override
    public StringBuffer format(long number,
                               StringBuffer toAppendTo,
                               FieldPosition ignore) {
        // this is one of the inherited format() methods.  Since it doesn't
        // have a way to select the rule set to use, it just uses the
        // default one
        if (toAppendTo.length() == 0) {
            toAppendTo.append(adjustForContext(format(number, defaultRuleSet)));
        } else {
            // appending to other text, don't capitalize
            toAppendTo.append(format(number, defaultRuleSet));
        }
        return toAppendTo;
    }

    /**
     * <strong style="font-family: helvetica; color: red;">NEW</strong>
     * Implement android.icu.text.NumberFormat:
     * Format a BigInteger.
     */
    @Override
    public StringBuffer format(BigInteger number,
                               StringBuffer toAppendTo,
                               FieldPosition pos) {
        return format(new android.icu.math.BigDecimal(number), toAppendTo, pos);
    }

    /**
     * <strong style="font-family: helvetica; color: red;">NEW</strong>
     * Implement android.icu.text.NumberFormat:
     * Format a BigDecimal.
     */
    @Override
    public StringBuffer format(java.math.BigDecimal number,
                               StringBuffer toAppendTo,
                               FieldPosition pos) {
        return format(new android.icu.math.BigDecimal(number), toAppendTo, pos);
    }

    private static final android.icu.math.BigDecimal MAX_VALUE = android.icu.math.BigDecimal.valueOf(Long.MAX_VALUE);
    private static final android.icu.math.BigDecimal MIN_VALUE = android.icu.math.BigDecimal.valueOf(Long.MIN_VALUE);

    /**
     * <strong style="font-family: helvetica; color: red;">NEW</strong>
     * Implement android.icu.text.NumberFormat:
     * Format a BigDecimal.
     */
    @Override
    public StringBuffer format(android.icu.math.BigDecimal number,
                               StringBuffer toAppendTo,
                               FieldPosition pos) {
        if (MIN_VALUE.compareTo(number) >= 0 || MAX_VALUE.compareTo(number) <= 0) {
            // We're outside of our normal range that this framework can handle.
            // The DecimalFormat will provide more accurate results.
            return getDecimalFormat().format(number, toAppendTo, pos);
        }
        if (number.scale() == 0) {
            return format(number.longValue(), toAppendTo, pos);
        }
        return format(number.doubleValue(), toAppendTo, pos);
    }

    /**
     * Parses the specified string, beginning at the specified position, according
     * to this formatter's rules.  This will match the string against all of the
     * formatter's public rule sets and return the value corresponding to the longest
     * parseable substring.  This function's behavior is affected by the lenient
     * parse mode.
     * @param text The string to parse
     * @param parsePosition On entry, contains the position of the first character
     * in "text" to examine.  On exit, has been updated to contain the position
     * of the first character in "text" that wasn't consumed by the parse.
     * @return The number that corresponds to the parsed text.  This will be an
     * instance of either Long or Double, depending on whether the result has a
     * fractional part.
     * @see #setLenientParseMode
     */
    @Override
    public Number parse(String text, ParsePosition parsePosition) {

        // parsePosition tells us where to start parsing.  We copy the
        // text in the string from here to the end inro a new string,
        // and create a new ParsePosition and result variable to use
        // for the duration of the parse operation
        String workingText = text.substring(parsePosition.getIndex());
        ParsePosition workingPos = new ParsePosition(0);
        Number tempResult = null;

        // keep track of the largest number of characters consumed in
        // the various trials, and the result that corresponds to it
        Number result = NFRule.ZERO;
        ParsePosition highWaterMark = new ParsePosition(workingPos.getIndex());

        // iterate over the public rule sets (beginning with the default one)
        // and try parsing the text with each of them.  Keep track of which
        // one consumes the most characters: that's the one that determines
        // the result we return
        for (int i = ruleSets.length - 1; i >= 0; i--) {
            // skip private or unparseable rule sets
            if (!ruleSets[i].isPublic() || !ruleSets[i].isParseable()) {
                continue;
            }

            // try parsing the string with the rule set.  If it gets past the
            // high-water mark, update the high-water mark and the result
            tempResult = ruleSets[i].parse(workingText, workingPos, Double.MAX_VALUE);
            if (workingPos.getIndex() > highWaterMark.getIndex()) {
                result = tempResult;
                highWaterMark.setIndex(workingPos.getIndex());
            }
            // commented out because this API on ParsePosition doesn't exist in 1.1.x
            //            if (workingPos.getErrorIndex() > highWaterMark.getErrorIndex()) {
            //                highWaterMark.setErrorIndex(workingPos.getErrorIndex());
            //            }

            // if we manage to use up all the characters in the string,
            // we don't have to try any more rule sets
            if (highWaterMark.getIndex() == workingText.length()) {
                break;
            }

            // otherwise, reset our internal parse position to the
            // beginning and try again with the next rule set
            workingPos.setIndex(0);
        }

        // add the high water mark to our original parse position and
        // return the result
        parsePosition.setIndex(parsePosition.getIndex() + highWaterMark.getIndex());
        // commented out because this API on ParsePosition doesn't exist in 1.1.x
        //        if (highWaterMark.getIndex() == 0) {
        //            parsePosition.setErrorIndex(parsePosition.getIndex() + highWaterMark.getErrorIndex());
        //        }
        return result;
    }

    /**
     * Turns lenient parse mode on and off.
     *
     * When in lenient parse mode, the formatter uses an RbnfLenientScanner
     * for parsing the text.  Lenient parsing is only in effect if a scanner
     * is set.  If a provider is not set, and this is used for parsing,
     * a default scanner <code>RbnfLenientScannerProviderImpl</code> will be set if
     * it is available on the classpath.  Otherwise this will have no effect.
     *
     * @param enabled If true, turns lenient-parse mode on; if false, turns it off.
     * @see RbnfLenientScanner
     * @see RbnfLenientScannerProvider
     */
    public void setLenientParseMode(boolean enabled) {
        lenientParse = enabled;
    }

    /**
     * Returns true if lenient-parse mode is turned on.  Lenient parsing is off
     * by default.
     * @return true if lenient-parse mode is turned on.
     * @see #setLenientParseMode
     */
    public boolean lenientParseEnabled() {
        return lenientParse;
    }

    /**
     * Sets the provider for the lenient scanner.  If this has not been set,
     * {@link #setLenientParseMode}
     * has no effect.  This is necessary to decouple collation from format code.
     * @param scannerProvider the provider
     * @see #setLenientParseMode
     * @see #getLenientScannerProvider
     */
    public void setLenientScannerProvider(RbnfLenientScannerProvider scannerProvider) {
        this.scannerProvider = scannerProvider;
    }

    /**
     * Returns the lenient scanner provider.  If none was set, and lenient parse is
     * enabled, this will attempt to instantiate a default scanner, setting it if
     * it was successful.  Otherwise this returns false.
     *
     * @see #setLenientScannerProvider
     */
    public RbnfLenientScannerProvider getLenientScannerProvider() {
        // there's a potential race condition if two threads try to set/get the scanner at
        // the same time, but you get what you get, and you shouldn't be using this from
        // multiple threads anyway.
        if (scannerProvider == null && lenientParse && !lookedForScanner) {
            try {
                lookedForScanner = true;
                Class<?> cls = Class.forName("android.icu.impl.text.RbnfScannerProviderImpl");
                RbnfLenientScannerProvider provider = (RbnfLenientScannerProvider)cls.newInstance();
                setLenientScannerProvider(provider);
            }
            catch (Exception e) {
                // any failure, we just ignore and return null
            }
        }

        return scannerProvider;
    }

    /**
     * Override the default rule set to use.  If ruleSetName is null, reset
     * to the initial default rule set.
     * @param ruleSetName the name of the rule set, or null to reset the initial default.
     * @throws IllegalArgumentException if ruleSetName is not the name of a public ruleset.
     */
    public void setDefaultRuleSet(String ruleSetName) {
        if (ruleSetName == null) {
            if (publicRuleSetNames.length > 0) {
                defaultRuleSet = findRuleSet(publicRuleSetNames[0]);
            } else {
                defaultRuleSet = null;
                int n = ruleSets.length;
                while (--n >= 0) {
                   String currentName = ruleSets[n].getName();
                   if (currentName.equals("%spellout-numbering") ||
                       currentName.equals("%digits-ordinal") ||
                       currentName.equals("%duration")) {

                       defaultRuleSet = ruleSets[n];
                       return;
                   }
                }

                n = ruleSets.length;
                while (--n >= 0) {
                    if (ruleSets[n].isPublic()) {
                        defaultRuleSet = ruleSets[n];
                        break;
                    }
                }
            }
        } else if (ruleSetName.startsWith("%%")) {
            throw new IllegalArgumentException("cannot use private rule set: " + ruleSetName);
        } else {
            defaultRuleSet = findRuleSet(ruleSetName);
        }
    }

    /**
     * Return the name of the current default rule set.
     * @return the name of the current default rule set, if it is public, else the empty string.
     */
    public String getDefaultRuleSetName() {
        if (defaultRuleSet != null && defaultRuleSet.isPublic()) {
            return defaultRuleSet.getName();
        }
        return "";
    }

    /**
     * Sets the decimal format symbols used by this formatter. The formatter uses a copy of the
     * provided symbols.
     *
     * @param newSymbols desired DecimalFormatSymbols
     * @see DecimalFormatSymbols
     */
    public void setDecimalFormatSymbols(DecimalFormatSymbols newSymbols) {
        if (newSymbols != null) {
            decimalFormatSymbols = (DecimalFormatSymbols) newSymbols.clone();
            if (decimalFormat != null) {
                decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);
            }
            if (defaultInfinityRule != null) {
                defaultInfinityRule = null;
                getDefaultInfinityRule(); // Reset with the new DecimalFormatSymbols
            }
            if (defaultNaNRule != null) {
                defaultNaNRule = null;
                getDefaultNaNRule(); // Reset with the new DecimalFormatSymbols
            }

            // Apply the new decimalFormatSymbols by reparsing the rulesets
            for (NFRuleSet ruleSet : ruleSets) {
                ruleSet.setDecimalFormatSymbols(decimalFormatSymbols);
            }
        }
    }

    /**
     * <strong>[icu]</strong> Set a particular DisplayContext value in the formatter,
     * such as CAPITALIZATION_FOR_STANDALONE. Note: For getContext, see
     * NumberFormat.
     *
     * @param context The DisplayContext value to set.
     */
    // Here we override the NumberFormat implementation in order to
    // lazily initialize relevant items
    @Override
    public void setContext(DisplayContext context) {
        super.setContext(context);
        if (!capitalizationInfoIsSet &&
              (context==DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU || context==DisplayContext.CAPITALIZATION_FOR_STANDALONE)) {
            initCapitalizationContextInfo(locale);
            capitalizationInfoIsSet = true;
        }
        if (capitalizationBrkIter == null && (context==DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE ||
              (context==DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU && capitalizationForListOrMenu) ||
              (context==DisplayContext.CAPITALIZATION_FOR_STANDALONE && capitalizationForStandAlone) )) {
            capitalizationBrkIter = BreakIterator.getSentenceInstance(locale);
        }
    }

    /**
     * Returns the rounding mode.
     *
     * @return A rounding mode, between <code>BigDecimal.ROUND_UP</code> and
     * <code>BigDecimal.ROUND_UNNECESSARY</code>.
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
     * @see #getRoundingMode
     * @see java.math.BigDecimal
     */
    @Override
    public void setRoundingMode(int roundingMode) {
        if (roundingMode < BigDecimal.ROUND_UP || roundingMode > BigDecimal.ROUND_UNNECESSARY) {
            throw new IllegalArgumentException("Invalid rounding mode: " + roundingMode);
        }

        this.roundingMode = roundingMode;
    }


    //-----------------------------------------------------------------------
    // package-internal API
    //-----------------------------------------------------------------------

    /**
     * Returns a reference to the formatter's default rule set.  The default
     * rule set is the last public rule set in the description, or the one
     * most recently set by setDefaultRuleSet.
     * @return The formatter's default rule set.
     */
    NFRuleSet getDefaultRuleSet() {
        return defaultRuleSet;
    }

    /**
     * Returns the scanner to use for lenient parsing.  The scanner is
     * provided by the provider.
     * @return The collator to use for lenient parsing, or null if lenient parsing
     * is turned off.
     */
    RbnfLenientScanner getLenientScanner() {
        if (lenientParse) {
            RbnfLenientScannerProvider provider = getLenientScannerProvider();
            if (provider != null) {
                return provider.get(locale, lenientParseRules);
            }
        }
        return null;
    }

    /**
     * Returns the DecimalFormatSymbols object that should be used by all DecimalFormat
     * instances owned by this formatter.  This object is lazily created: this function
     * creates it the first time it's called.
     * @return The DecimalFormatSymbols object that should be used by all DecimalFormat
     * instances owned by this formatter.
     */
    DecimalFormatSymbols getDecimalFormatSymbols() {
        // lazy-evaluate the DecimalFormatSymbols object.  This object
        // is shared by all DecimalFormat instances belonging to this
        // formatter
        if (decimalFormatSymbols == null) {
            decimalFormatSymbols = new DecimalFormatSymbols(locale);
        }
        return decimalFormatSymbols;
    }

    DecimalFormat getDecimalFormat() {
        if (decimalFormat == null) {
            // Don't use NumberFormat.getInstance, which can cause a recursive call
            String pattern = getPattern(locale, NUMBERSTYLE);
            decimalFormat = new DecimalFormat(pattern, getDecimalFormatSymbols());
        }
        return decimalFormat;
    }

    PluralFormat createPluralFormat(PluralRules.PluralType pluralType, String pattern) {
        return new PluralFormat(locale, pluralType, pattern, getDecimalFormat());
    }

    /**
     * Returns the default rule for infinity. This object is lazily created: this function
     * creates it the first time it's called.
     */
    NFRule getDefaultInfinityRule() {
        if (defaultInfinityRule == null) {
            defaultInfinityRule = new NFRule(this, "Inf: " + getDecimalFormatSymbols().getInfinity());
        }
        return defaultInfinityRule;
    }

    /**
     * Returns the default rule for NaN. This object is lazily created: this function
     * creates it the first time it's called.
     */
    NFRule getDefaultNaNRule() {
        if (defaultNaNRule == null) {
            defaultNaNRule = new NFRule(this, "NaN: " + getDecimalFormatSymbols().getNaN());
        }
        return defaultNaNRule;
    }

    //-----------------------------------------------------------------------
    // construction implementation
    //-----------------------------------------------------------------------

    /**
     * This extracts the special information from the rule sets before the
     * main parsing starts.  Extra whitespace must have already been removed
     * from the description.  If found, the special information is removed from the
     * description and returned, otherwise the description is unchanged and null
     * is returned.  Note: the trailing semicolon at the end of the special
     * rules is stripped.
     * @param description the rbnf description with extra whitespace removed
     * @param specialName the name of the special rule text to extract
     * @return the special rule text, or null if the rule was not found
     */
    private String extractSpecial(StringBuilder description, String specialName) {
        String result = null;
        int lp = description.indexOf(specialName);
        if (lp != -1) {
            // we've got to make sure we're not in the middle of a rule
            // (where specialName would actually get treated as
            // rule text)
            if (lp == 0 || description.charAt(lp - 1) == ';') {
                // locate the beginning and end of the actual special
                // rules (there may be whitespace between the name and
                // the first token in the description)
                int lpEnd = description.indexOf(";%", lp);

                if (lpEnd == -1) {
                    lpEnd = description.length() - 1; // later we add 1 back to get the '%'
                }
                int lpStart = lp + specialName.length();
                while (lpStart < lpEnd &&
                       PatternProps.isWhiteSpace(description.charAt(lpStart))) {
                    ++lpStart;
                }

                // copy out the special rules
                result = description.substring(lpStart, lpEnd);

                // remove the special rule from the description
                description.delete(lp, lpEnd+1); // delete the semicolon but not the '%'
            }
        }
        return result;
    }

    /**
     * This function parses the description and uses it to build all of
     * internal data structures that the formatter uses to do formatting
     * @param description The description of the formatter's desired behavior.
     * This is either passed in by the caller or loaded out of a resource
     * by one of the constructors, and is in the description format specified
     * in the class docs.
     */
    private void init(String description, String[][] localizations) {
        initLocalizations(localizations);

        // start by stripping the trailing whitespace from all the rules
        // (this is all the whitespace follwing each semicolon in the
        // description).  This allows us to look for rule-set boundaries
        // by searching for ";%" without having to worry about whitespace
        // between the ; and the %
        StringBuilder descBuf = stripWhitespace(description);

        // check to see if there's a set of lenient-parse rules.  If there
        // is, pull them out into our temporary holding place for them,
        // and delete them from the description before the real description-
        // parsing code sees them

        lenientParseRules = extractSpecial(descBuf, "%%lenient-parse:");
        postProcessRules = extractSpecial(descBuf, "%%post-process:");

        // pre-flight parsing the description and count the number of
        // rule sets (";%" marks the end of one rule set and the beginning
        // of the next)
        int numRuleSets = 1;
        int p = 0;
        while ((p = descBuf.indexOf(";%", p)) != -1) {
            ++numRuleSets;
            p += 2; // Skip the length of ";%"
        }

        // our rule list is an array of the appropriate size
        ruleSets = new NFRuleSet[numRuleSets];
        ruleSetsMap = new HashMap<String, NFRuleSet>(numRuleSets * 2 + 1);
        defaultRuleSet = null;

        // Used to count the number of public rule sets
        // Public rule sets have names that begin with % instead of %%.
        int publicRuleSetCount = 0;

        // divide up the descriptions into individual rule-set descriptions
        // and store them in a temporary array.  At each step, we also
        // new up a rule set, but all this does is initialize its name
        // and remove it from its description.  We can't actually parse
        // the rest of the descriptions and finish initializing everything
        // because we have to know the names and locations of all the rule
        // sets before we can actually set everything up
        String[] ruleSetDescriptions = new String[numRuleSets];

        int curRuleSet = 0;
        int start = 0;

        while (curRuleSet < ruleSets.length) {
            p = descBuf.indexOf(";%", start);
            if (p < 0) {
                p = descBuf.length() - 1;
            }
            ruleSetDescriptions[curRuleSet] = descBuf.substring(start, p + 1);
            NFRuleSet ruleSet = new NFRuleSet(this, ruleSetDescriptions, curRuleSet);
            ruleSets[curRuleSet] = ruleSet;
            String currentName = ruleSet.getName();
            ruleSetsMap.put(currentName, ruleSet);
            if (!currentName.startsWith("%%")) {
                ++publicRuleSetCount;
                if (defaultRuleSet == null
                        && currentName.equals("%spellout-numbering")
                        || currentName.equals("%digits-ordinal")
                        || currentName.equals("%duration"))
                {
                    defaultRuleSet = ruleSet;
                }
            }
            ++curRuleSet;
            start = p + 1;
        }

        // now we can take note of the formatter's default rule set, which
        // is the last public rule set in the description (it's the last
        // rather than the first so that a user can create a new formatter
        // from an existing formatter and change its default behavior just
        // by appending more rule sets to the end)

        // {dlf} Initialization of a fraction rule set requires the default rule
        // set to be known.  For purposes of initialization, this is always the
        // last public rule set, no matter what the localization data says.

        // Set the default ruleset to the last public ruleset, unless one of the predefined
        // ruleset names %spellout-numbering, %digits-ordinal, or %duration is found

        if (defaultRuleSet == null) {
            for (int i = ruleSets.length - 1; i >= 0; --i) {
                if (!ruleSets[i].getName().startsWith("%%")) {
                    defaultRuleSet = ruleSets[i];
                    break;
                }
            }
        }
        if (defaultRuleSet == null) {
            defaultRuleSet = ruleSets[ruleSets.length - 1];
        }

        // finally, we can go back through the temporary descriptions
        // list and finish setting up the substructure
        for (int i = 0; i < ruleSets.length; i++) {
            ruleSets[i].parseRules(ruleSetDescriptions[i]);
        }

        // Now that the rules are initialized, the 'real' default rule
        // set can be adjusted by the localization data.

        // prepare an array of the proper size and copy the names into it
        String[] publicRuleSetTemp = new String[publicRuleSetCount];
        publicRuleSetCount = 0;
        for (int i = ruleSets.length - 1; i >= 0; i--) {
            if (!ruleSets[i].getName().startsWith("%%")) {
                publicRuleSetTemp[publicRuleSetCount++] = ruleSets[i].getName();
            }
        }

        if (publicRuleSetNames != null) {
            // confirm the names, if any aren't in the rules, that's an error
            // it is ok if the rules contain public rule sets that are not in this list
            loop: for (int i = 0; i < publicRuleSetNames.length; ++i) {
                String name = publicRuleSetNames[i];
                for (int j = 0; j < publicRuleSetTemp.length; ++j) {
                    if (name.equals(publicRuleSetTemp[j])) {
                        continue loop;
                    }
                }
                throw new IllegalArgumentException("did not find public rule set: " + name);
            }

            defaultRuleSet = findRuleSet(publicRuleSetNames[0]); // might be different
        } else {
            publicRuleSetNames = publicRuleSetTemp;
        }
    }

    /**
     * Take the localizations array and create a Map from the locale strings to
     * the localization arrays.
     */
    private void initLocalizations(String[][] localizations) {
        if (localizations != null) {
            publicRuleSetNames = localizations[0].clone();

            Map<String, String[]> m = new HashMap<String, String[]>();
            for (int i = 1; i < localizations.length; ++i) {
                String[] data = localizations[i];
                String loc = data[0];
                String[] names = new String[data.length-1];
                if (names.length != publicRuleSetNames.length) {
                    throw new IllegalArgumentException("public name length: " + publicRuleSetNames.length +
                                                       " != localized names[" + i + "] length: " + names.length);
                }
                System.arraycopy(data, 1, names, 0, names.length);
                m.put(loc, names);
            }

            if (!m.isEmpty()) {
                ruleSetDisplayNames = m;
            }
        }
    }

    /**
     * Set capitalizationForListOrMenu, capitalizationForStandAlone
     */
    private void initCapitalizationContextInfo(ULocale theLocale) {
        ICUResourceBundle rb = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, theLocale);
        try {
            ICUResourceBundle rdb = rb.getWithFallback("contextTransforms/number-spellout");
            int[] intVector = rdb.getIntVector();
            if (intVector.length >= 2) {
                capitalizationForListOrMenu = (intVector[0] != 0);
                capitalizationForStandAlone = (intVector[1] != 0);
            }
        } catch (MissingResourceException e) {
            // use default
        }
    }

    /**
     * This function is used by init() to strip whitespace between rules (i.e.,
     * after semicolons).
     * @param description The formatter description
     * @return The description with all the whitespace that follows semicolons
     * taken out.
     */
    private StringBuilder stripWhitespace(String description) {
        // since we don't have a method that deletes characters (why?!!)
        // create a new StringBuffer to copy the text into
        StringBuilder result = new StringBuilder();
        int descriptionLength = description.length();

        // iterate through the characters...
        int start = 0;
        while (start < descriptionLength) {
            // seek to the first non-whitespace character...
            while (start < descriptionLength
                   && PatternProps.isWhiteSpace(description.charAt(start)))
            {
                ++start;
            }

            //if the first non-whitespace character is semicolon, skip it and continue
            if (start < descriptionLength && description.charAt(start) == ';') {
                start += 1;
                continue;
            }

            // locate the next semicolon in the text and copy the text from
            // our current position up to that semicolon into the result
            int p = description.indexOf(';', start);
            if (p == -1) {
                // or if we don't find a semicolon, just copy the rest of
                // the string into the result
                result.append(description.substring(start));
                break;
            }
            else if (p < descriptionLength) {
                result.append(description.substring(start, p + 1));
                start = p + 1;
            }
            else {
                // when we get here, we've seeked off the end of the string, and
                // we terminate the loop (we continue until *start* is -1 rather
                // than until *p* is -1, because otherwise we'd miss the last
                // rule in the description)
                break;
            }
        }
        return result;
    }

    //-----------------------------------------------------------------------
    // formatting implementation
    //-----------------------------------------------------------------------

    /**
     * Bottleneck through which all the public format() methods
     * that take a double pass. By the time we get here, we know
     * which rule set we're using to do the formatting.
     * @param number The number to format
     * @param ruleSet The rule set to use to format the number
     * @return The text that resulted from formatting the number
     */
    private String format(double number, NFRuleSet ruleSet) {
        // all API format() routines that take a double vector through
        // here.  Create an empty string buffer where the result will
        // be built, and pass it to the rule set (along with an insertion
        // position of 0 and the number being formatted) to the rule set
        // for formatting
        StringBuilder result = new StringBuilder();
        if (getRoundingMode() != BigDecimal.ROUND_UNNECESSARY) {
            // We convert to a string because BigDecimal insists on excessive precision.
            number = new BigDecimal(Double.toString(number)).setScale(getMaximumFractionDigits(), roundingMode).doubleValue();
        }
        ruleSet.format(number, result, 0, 0);
        postProcess(result, ruleSet);
        return result.toString();
    }

    /**
     * Bottleneck through which all the public format() methods
     * that take a long pass. By the time we get here, we know
     * which rule set we're using to do the formatting.
     * @param number The number to format
     * @param ruleSet The rule set to use to format the number
     * @return The text that resulted from formatting the number
     */
    private String format(long number, NFRuleSet ruleSet) {
        // all API format() routines that take a double vector through
        // here.  We have these two identical functions-- one taking a
        // double and one taking a long-- the couple digits of precision
        // that long has but double doesn't (both types are 8 bytes long,
        // but double has to borrow some of the mantissa bits to hold
        // the exponent).
        // Create an empty string buffer where the result will
        // be built, and pass it to the rule set (along with an insertion
        // position of 0 and the number being formatted) to the rule set
        // for formatting
        StringBuilder result = new StringBuilder();
        if (number == Long.MIN_VALUE) {
            // We can't handle this value right now. Provide an accurate default value.
            result.append(getDecimalFormat().format(Long.MIN_VALUE));
        }
        else {
            ruleSet.format(number, result, 0, 0);
        }
        postProcess(result, ruleSet);
        return result.toString();
    }

    /**
     * Post-process the rules if we have a post-processor.
     */
    private void postProcess(StringBuilder result, NFRuleSet ruleSet) {
        if (postProcessRules != null) {
            if (postProcessor == null) {
                int ix = postProcessRules.indexOf(";");
                if (ix == -1) {
                    ix = postProcessRules.length();
                }
                String ppClassName = postProcessRules.substring(0, ix).trim();
                try {
                    Class<?> cls = Class.forName(ppClassName);
                    postProcessor = (RBNFPostProcessor)cls.newInstance();
                    postProcessor.init(this, postProcessRules);
                }
                catch (Exception e) {
                    // if debug, print it out
                    if (DEBUG) System.out.println("could not locate " + ppClassName + ", error " +
                                       e.getClass().getName() + ", " + e.getMessage());
                    postProcessor = null;
                    postProcessRules = null; // don't try again
                    return;
                }
            }

            postProcessor.process(result, ruleSet);
        }
    }

    /**
     * Adjust capitalization of formatted result for display context
     */
    private String adjustForContext(String result) {
        if (result != null && result.length() > 0 && UCharacter.isLowerCase(result.codePointAt(0))) {
            DisplayContext capitalization = getContext(DisplayContext.Type.CAPITALIZATION);
            if (  capitalization==DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE ||
                  (capitalization == DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU && capitalizationForListOrMenu) ||
                  (capitalization == DisplayContext.CAPITALIZATION_FOR_STANDALONE && capitalizationForStandAlone) ) {
                if (capitalizationBrkIter == null) {
                    // should only happen when deserializing, etc.
                    capitalizationBrkIter = BreakIterator.getSentenceInstance(locale);
                }
                return UCharacter.toTitleCase(locale, result, capitalizationBrkIter,
                                UCharacter.TITLECASE_NO_LOWERCASE | UCharacter.TITLECASE_NO_BREAK_ADJUSTMENT);
            }
        }
        return result;
    }

    /**
     * Returns the named rule set.  Throws an IllegalArgumentException
     * if this formatter doesn't have a rule set with that name.
     * @param name The name of the desired rule set
     * @return The rule set with that name
     */
    NFRuleSet findRuleSet(String name) throws IllegalArgumentException {
        NFRuleSet result = ruleSetsMap.get(name);
        if (result == null) {
            throw new IllegalArgumentException("No rule set named " + name);
        }
        return result;
    }
}
