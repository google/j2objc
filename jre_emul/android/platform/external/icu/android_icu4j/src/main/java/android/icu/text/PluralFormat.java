/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2007-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */

package android.icu.text;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.Map;

import android.icu.impl.Utility;
import android.icu.text.PluralRules.FixedDecimal;
import android.icu.text.PluralRules.PluralType;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;

/**
 * <code>PluralFormat</code> supports the creation of internationalized
 * messages with plural inflection. It is based on <i>plural
 * selection</i>, i.e. the caller specifies messages for each
 * plural case that can appear in the user's language and the
 * <code>PluralFormat</code> selects the appropriate message based on
 * the number.
 *
 * <h3>The Problem of Plural Forms in Internationalized Messages</h3>
 * <p>
 * Different languages have different ways to inflect
 * plurals. Creating internationalized messages that include plural
 * forms is only feasible when the framework is able to handle plural
 * forms of <i>all</i> languages correctly. <code>ChoiceFormat</code>
 * doesn't handle this well, because it attaches a number interval to
 * each message and selects the message whose interval contains a
 * given number. This can only handle a finite number of
 * intervals. But in some languages, like Polish, one plural case
 * applies to infinitely many intervals (e.g., the paucal case applies to
 * numbers ending with 2, 3, or 4 except those ending with 12, 13, or
 * 14). Thus <code>ChoiceFormat</code> is not adequate.
 * <p>
 * <code>PluralFormat</code> deals with this by breaking the problem
 * into two parts:
 * <ul>
 * <li>It uses <code>PluralRules</code> that can define more complex
 *     conditions for a plural case than just a single interval. These plural
 *     rules define both what plural cases exist in a language, and to
 *     which numbers these cases apply.
 * <li>It provides predefined plural rules for many languages. Thus, the programmer
 *     need not worry about the plural cases of a language and
 *     does not have to define the plural cases; they can simply
 *     use the predefined keywords. The whole plural formatting of messages can
 *     be done using localized patterns from resource bundles. For predefined plural
 *     rules, see the CLDR <i>Language Plural Rules</i> page at
 *    http://unicode.org/repos/cldr-tmp/trunk/diff/supplemental/language_plural_rules.html
 * </ul>
 *
 * <h4>Usage of <code>PluralFormat</code></h4>
 * <p>Note: Typically, plural formatting is done via <code>MessageFormat</code>
 * with a <code>plural</code> argument type,
 * rather than using a stand-alone <code>PluralFormat</code>.
 * <p>
 * This discussion assumes that you use <code>PluralFormat</code> with
 * a predefined set of plural rules. You can create one using one of
 * the constructors that takes a <code>ULocale</code> object. To
 * specify the message pattern, you can either pass it to the
 * constructor or set it explicitly using the
 * <code>applyPattern()</code> method. The <code>format()</code>
 * method takes a number object and selects the message of the
 * matching plural case. This message will be returned.
 *
 * <h5>Patterns and Their Interpretation</h5>
 * <p>
 * The pattern text defines the message output for each plural case of the
 * specified locale. Syntax:
 * <blockquote><pre>
 * pluralStyle = [offsetValue] (selector '{' message '}')+
 * offsetValue = "offset:" number
 * selector = explicitValue | keyword
 * explicitValue = '=' number  // adjacent, no white space in between
 * keyword = [^[[:Pattern_Syntax:][:Pattern_White_Space:]]]+
 * message: see {@link MessageFormat}
 * </pre></blockquote>
 * Pattern_White_Space between syntax elements is ignored, except
 * between the {curly braces} and their sub-message,
 * and between the '=' and the number of an explicitValue.
 * <p>
 * There are 6 predefined case keywords in CLDR/ICU - 'zero', 'one', 'two', 'few', 'many' and
 * 'other'. You always have to define a message text for the default plural case
 * "<code>other</code>" which is contained in every rule set.
 * If you do not specify a message text for a particular plural case, the
 * message text of the plural case "<code>other</code>" gets assigned to this
 * plural case.
 * <p>
 * When formatting, the input number is first matched against the explicitValue clauses.
 * If there is no exact-number match, then a keyword is selected by calling
 * the <code>PluralRules</code> with the input number <em>minus the offset</em>.
 * (The offset defaults to 0 if it is omitted from the pattern string.)
 * If there is no clause with that keyword, then the "other" clauses is returned.
 * <p>
 * An unquoted pound sign (<code>#</code>) in the selected sub-message
 * itself (i.e., outside of arguments nested in the sub-message)
 * is replaced by the input number minus the offset.
 * The number-minus-offset value is formatted using a
 * <code>NumberFormat</code> for the <code>PluralFormat</code>'s locale. If you
 * need special number formatting, you have to use a <code>MessageFormat</code>
 * and explicitly specify a <code>NumberFormat</code> argument.
 * <strong>Note:</strong> That argument is formatting without subtracting the offset!
 * If you need a custom format and have a non-zero offset, then you need to pass the
 * number-minus-offset value as a separate parameter.
 *
 * <p>For a usage example, see the {@link MessageFormat} class documentation.
 *
 * <h4>Defining Custom Plural Rules</h4>
 * <p>If you need to use <code>PluralFormat</code> with custom rules, you can
 * create a <code>PluralRules</code> object and pass it to
 * <code>PluralFormat</code>'s constructor. If you also specify a locale in this
 * constructor, this locale will be used to format the number in the message
 * texts.
 * <p>
 * For more information about <code>PluralRules</code>, see
 * {@link PluralRules}.
 *
 * @author tschumann (Tim Schumann)
 */
public class PluralFormat extends UFormat {
    private static final long serialVersionUID = 1L;

    /**
     * The locale used for standard number formatting and getting the predefined
     * plural rules (if they were not defined explicitely).
     * @serial
     */
    private ULocale ulocale = null;

    /**
     * The plural rules used for plural selection.
     * @serial
     */
    private PluralRules pluralRules = null;

    /**
     * The applied pattern string.
     * @serial
     */
    private String pattern = null;

    /**
     * The MessagePattern which contains the parsed structure of the pattern string.
     */
    transient private MessagePattern msgPattern;

    /**
     * Obsolete with use of MessagePattern since ICU 4.8. Used to be:
     * The format messages for each plural case. It is a mapping:
     *  <code>String</code>(plural case keyword) --&gt; <code>String</code>
     *  (message for this plural case).
     * @serial
     */
    private Map<String, String> parsedValues = null;

    /**
     * This <code>NumberFormat</code> is used for the standard formatting of
     * the number inserted into the message.
     * @serial
     */
    private NumberFormat numberFormat = null;

    /**
     * The offset to subtract before invoking plural rules.
     */
    transient private double offset = 0;

    /**
     * Creates a new cardinal-number <code>PluralFormat</code> for the default <code>FORMAT</code> locale.
     * This locale will be used to get the set of plural rules and for standard
     * number formatting.
     * @see Category#FORMAT
     */
    public PluralFormat() {
        init(null, PluralType.CARDINAL, ULocale.getDefault(Category.FORMAT), null);
    }

    /**
     * Creates a new cardinal-number <code>PluralFormat</code> for a given locale.
     * @param ulocale the <code>PluralFormat</code> will be configured with
     *        rules for this locale. This locale will also be used for standard
     *        number formatting.
     */
    public PluralFormat(ULocale ulocale) {
        init(null, PluralType.CARDINAL, ulocale, null);
    }

    /**
     * Creates a new cardinal-number <code>PluralFormat</code> for a given
     * {@link java.util.Locale}.
     * @param locale the <code>PluralFormat</code> will be configured with
     *        rules for this locale. This locale will also be used for standard
     *        number formatting.
     */
    public PluralFormat(Locale locale) {
        this(ULocale.forLocale(locale));
    }

    /**
     * Creates a new cardinal-number <code>PluralFormat</code> for a given set of rules.
     * The standard number formatting will be done using the default <code>FORMAT</code> locale.
     * @param rules defines the behavior of the <code>PluralFormat</code>
     *        object.
     * @see Category#FORMAT
     */
    public PluralFormat(PluralRules rules) {
        init(rules, PluralType.CARDINAL, ULocale.getDefault(Category.FORMAT), null);
    }

    /**
     * Creates a new cardinal-number <code>PluralFormat</code> for a given set of rules.
     * The standard number formatting will be done using the given locale.
     * @param ulocale the default number formatting will be done using this
     *        locale.
     * @param rules defines the behavior of the <code>PluralFormat</code>
     *        object.
     */
    public PluralFormat(ULocale ulocale, PluralRules rules) {
        init(rules, PluralType.CARDINAL, ulocale, null);
    }

    /**
     * Creates a new cardinal-number <code>PluralFormat</code> for a given set of rules.
     * The standard number formatting will be done using the given locale.
     * @param locale the default number formatting will be done using this
     *        locale.
     * @param rules defines the behavior of the <code>PluralFormat</code>
     *        object.
     */
    public PluralFormat(Locale locale, PluralRules rules) {
        this(ULocale.forLocale(locale), rules);
    }

    /**
     * Creates a new <code>PluralFormat</code> for the plural type.
     * The standard number formatting will be done using the given locale.
     * @param ulocale the default number formatting will be done using this
     *        locale.
     * @param type The plural type (e.g., cardinal or ordinal).
     */
    public PluralFormat(ULocale ulocale, PluralType type) {
        init(null, type, ulocale, null);
    }

    /**
     * Creates a new <code>PluralFormat</code> for the plural type.
     * The standard number formatting will be done using the given {@link java.util.Locale}.
     * @param locale the default number formatting will be done using this
     *        locale.
     * @param type The plural type (e.g., cardinal or ordinal).
     */
    public PluralFormat(Locale locale, PluralType type) {
        this(ULocale.forLocale(locale), type);
    }

    /**
     * Creates a new cardinal-number <code>PluralFormat</code> for a given pattern string.
     * The default <code>FORMAT</code> locale will be used to get the set of plural rules and for
     * standard number formatting.
     * @param  pattern the pattern for this <code>PluralFormat</code>.
     * @throws IllegalArgumentException if the pattern is invalid.
     * @see Category#FORMAT
     */
    public PluralFormat(String pattern) {
        init(null, PluralType.CARDINAL, ULocale.getDefault(Category.FORMAT), null);
        applyPattern(pattern);
    }

    /**
     * Creates a new cardinal-number <code>PluralFormat</code> for a given pattern string and
     * locale.
     * The locale will be used to get the set of plural rules and for
     * standard number formatting.
     * <p>Example code:{@sample external/icu/android_icu4j/src/samples/java/android/icu/samples/text/pluralformat/PluralFormatSample.java PluralFormatExample}
     * @param ulocale the <code>PluralFormat</code> will be configured with
     *        rules for this locale. This locale will also be used for standard
     *        number formatting.
     * @param  pattern the pattern for this <code>PluralFormat</code>.
     * @throws IllegalArgumentException if the pattern is invalid.
     */
    public PluralFormat(ULocale ulocale, String pattern) {
        init(null, PluralType.CARDINAL, ulocale, null);
        applyPattern(pattern);
    }

    /**
     * Creates a new cardinal-number <code>PluralFormat</code> for a given set of rules and a
     * pattern.
     * The standard number formatting will be done using the default <code>FORMAT</code> locale.
     * @param rules defines the behavior of the <code>PluralFormat</code>
     *        object.
     * @param  pattern the pattern for this <code>PluralFormat</code>.
     * @throws IllegalArgumentException if the pattern is invalid.
     * @see Category#FORMAT
     */
    public PluralFormat(PluralRules rules, String pattern) {
        init(rules, PluralType.CARDINAL, ULocale.getDefault(Category.FORMAT), null);
        applyPattern(pattern);
    }

    /**
     * Creates a new cardinal-number <code>PluralFormat</code> for a given set of rules, a
     * pattern and a locale.
     * @param ulocale the <code>PluralFormat</code> will be configured with
     *        rules for this locale. This locale will also be used for standard
     *        number formatting.
     * @param rules defines the behavior of the <code>PluralFormat</code>
     *        object.
     * @param  pattern the pattern for this <code>PluralFormat</code>.
     * @throws IllegalArgumentException if the pattern is invalid.
     */
    public PluralFormat(ULocale ulocale, PluralRules rules, String pattern) {
        init(rules, PluralType.CARDINAL, ulocale, null);
        applyPattern(pattern);
    }

    /**
     * Creates a new <code>PluralFormat</code> for a plural type, a
     * pattern and a locale.
     * @param ulocale the <code>PluralFormat</code> will be configured with
     *        rules for this locale. This locale will also be used for standard
     *        number formatting.
     * @param type The plural type (e.g., cardinal or ordinal).
     * @param  pattern the pattern for this <code>PluralFormat</code>.
     * @throws IllegalArgumentException if the pattern is invalid.
     */
    public PluralFormat(ULocale ulocale, PluralType type, String pattern) {
        init(null, type, ulocale, null);
        applyPattern(pattern);
    }

    /**
     * Creates a new <code>PluralFormat</code> for a plural type, a
     * pattern and a locale.
     * @param ulocale the <code>PluralFormat</code> will be configured with
     *        rules for this locale. This locale will also be used for standard
     *        number formatting.
     * @param type The plural type (e.g., cardinal or ordinal).
     * @param pattern the pattern for this <code>PluralFormat</code>.
     * @param numberFormat The number formatter to use.
     * @throws IllegalArgumentException if the pattern is invalid.
     */
    /*package*/ PluralFormat(ULocale ulocale, PluralType type, String pattern, NumberFormat numberFormat) {
        init(null, type, ulocale, numberFormat);
        applyPattern(pattern);
    }

    /*
     * Initializes the <code>PluralRules</code> object.
     * Postcondition:<br/>
     *   <code>ulocale</code>    :  is <code>locale</code><br/>
     *   <code>pluralRules</code>:  if <code>rules</code> != <code>null</code>
     *                              it's set to rules, otherwise it is the
     *                              predefined plural rule set for the locale
     *                              <code>ulocale</code>.<br/>
     *   <code>parsedValues</code>: is <code>null</code><br/>
     *   <code>pattern</code>:      is <code>null</code><br/>
     *   <code>numberFormat</code>: a <code>NumberFormat</code> for the locale
     *                              <code>ulocale</code>.
     */
    private void init(PluralRules rules, PluralType type, ULocale locale, NumberFormat numberFormat) {
        ulocale = locale;
        pluralRules = (rules == null) ? PluralRules.forLocale(ulocale, type)
                                      : rules;
        resetPattern();
        this.numberFormat = (numberFormat == null) ? NumberFormat.getInstance(ulocale) : numberFormat;
    }

    private void resetPattern() {
        pattern = null;
        if(msgPattern != null) {
            msgPattern.clear();
        }
        offset = 0;
    }

    /**
     * Sets the pattern used by this plural format.
     * The method parses the pattern and creates a map of format strings
     * for the plural rules.
     * Patterns and their interpretation are specified in the class description.
     *
     * @param pattern the pattern for this plural format.
     * @throws IllegalArgumentException if the pattern is invalid.
     */
    public void applyPattern(String pattern) {
        this.pattern = pattern;
        if (msgPattern == null) {
            msgPattern = new MessagePattern();
        }
        try {
            msgPattern.parsePluralStyle(pattern);
            offset = msgPattern.getPluralOffset(0);
        } catch(RuntimeException e) {
            resetPattern();
            throw e;
        }
    }

    /**
     * Returns the pattern for this PluralFormat.
     *
     * @return the pattern string
     */
    public String toPattern() {
        return pattern;
    }

    /**
     * Finds the PluralFormat sub-message for the given number, or the "other" sub-message.
     * @param pattern A MessagePattern.
     * @param partIndex the index of the first PluralFormat argument style part.
     * @param selector the PluralSelector for mapping the number (minus offset) to a keyword.
     * @param context worker object for the selector.
     * @param number a number to be matched to one of the PluralFormat argument's explicit values,
     *        or mapped via the PluralSelector.
     * @return the sub-message start part index.
     */
    /*package*/ static int findSubMessage(
            MessagePattern pattern, int partIndex,
            PluralSelector selector, Object context, double number) {
        int count=pattern.countParts();
        double offset;
        MessagePattern.Part part=pattern.getPart(partIndex);
        if(part.getType().hasNumericValue()) {
            offset=pattern.getNumericValue(part);
            ++partIndex;
        } else {
            offset=0;
        }
        // The keyword is null until we need to match against a non-explicit, not-"other" value.
        // Then we get the keyword from the selector.
        // (In other words, we never call the selector if we match against an explicit value,
        // or if the only non-explicit keyword is "other".)
        String keyword=null;
        // When we find a match, we set msgStart>0 and also set this boolean to true
        // to avoid matching the keyword again (duplicates are allowed)
        // while we continue to look for an explicit-value match.
        boolean haveKeywordMatch=false;
        // msgStart is 0 until we find any appropriate sub-message.
        // We remember the first "other" sub-message if we have not seen any
        // appropriate sub-message before.
        // We remember the first matching-keyword sub-message if we have not seen
        // one of those before.
        // (The parser allows [does not check for] duplicate keywords.
        // We just have to make sure to take the first one.)
        // We avoid matching the keyword twice by also setting haveKeywordMatch=true
        // at the first keyword match.
        // We keep going until we find an explicit-value match or reach the end of the plural style.
        int msgStart=0;
        // Iterate over (ARG_SELECTOR [ARG_INT|ARG_DOUBLE] message) tuples
        // until ARG_LIMIT or end of plural-only pattern.
        do {
            part=pattern.getPart(partIndex++);
            MessagePattern.Part.Type type=part.getType();
            if(type==MessagePattern.Part.Type.ARG_LIMIT) {
                break;
            }
            assert type==MessagePattern.Part.Type.ARG_SELECTOR;
            // part is an ARG_SELECTOR followed by an optional explicit value, and then a message
            if(pattern.getPartType(partIndex).hasNumericValue()) {
                // explicit value like "=2"
                part=pattern.getPart(partIndex++);
                if(number==pattern.getNumericValue(part)) {
                    // matches explicit value
                    return partIndex;
                }
            } else if(!haveKeywordMatch) {
                // plural keyword like "few" or "other"
                // Compare "other" first and call the selector if this is not "other".
                if(pattern.partSubstringMatches(part, "other")) {
                    if(msgStart==0) {
                        msgStart=partIndex;
                        if(keyword!=null && keyword.equals("other")) {
                            // This is the first "other" sub-message,
                            // and the selected keyword is also "other".
                            // Do not match "other" again.
                            haveKeywordMatch=true;
                        }
                    }
                } else {
                    if(keyword==null) {
                        keyword=selector.select(context, number-offset);
                        if(msgStart!=0 && keyword.equals("other")) {
                            // We have already seen an "other" sub-message.
                            // Do not match "other" again.
                            haveKeywordMatch=true;
                            // Skip keyword matching but do getLimitPartIndex().
                        }
                    }
                    if(!haveKeywordMatch && pattern.partSubstringMatches(part, keyword)) {
                        // keyword matches
                        msgStart=partIndex;
                        // Do not match this keyword again.
                        haveKeywordMatch=true;
                    }
                }
            }
            partIndex=pattern.getLimitPartIndex(partIndex);
        } while(++partIndex<count);
        return msgStart;
    }

    /**
     * Interface for selecting PluralFormat keywords for numbers.
     * The PluralRules class was intended to implement this interface,
     * but there is no public API that uses a PluralSelector,
     * only MessageFormat and PluralFormat have PluralSelector implementations.
     * Therefore, PluralRules is not marked to implement this non-public interface,
     * to avoid confusing users.
     * @hide draft / provisional / internal are hidden on Android
     */
    /*package*/ interface PluralSelector {
        /**
         * Given a number, returns the appropriate PluralFormat keyword.
         *
         * @param context worker object for the selector.
         * @param number The number to be plural-formatted.
         * @return The selected PluralFormat keyword.
         */
        public String select(Object context, double number);
    }

    // See PluralSelector:
    // We could avoid this adapter class if we made PluralSelector public
    // (or at least publicly visible) and had PluralRules implement PluralSelector.
    private final class PluralSelectorAdapter implements PluralSelector {
        @Override
        public String select(Object context, double number) {
            FixedDecimal dec = (FixedDecimal) context;
            assert dec.source == (dec.isNegative ? -number : number);
            return pluralRules.select(dec);
        }
    }
    transient private PluralSelectorAdapter pluralRulesWrapper = new PluralSelectorAdapter();

    /**
     * Formats a plural message for a given number.
     *
     * @param number a number for which the plural message should be formatted.
     *        If no pattern has been applied to this
     *        <code>PluralFormat</code> object yet, the formatted number will
     *        be returned.
     * @return the string containing the formatted plural message.
     */
    public final String format(double number) {
        return format(number, number);
    }

    /**
     * Formats a plural message for a given number and appends the formatted
     * message to the given <code>StringBuffer</code>.
     * @param number a number object (instance of <code>Number</code> for which
     *        the plural message should be formatted. If no pattern has been
     *        applied to this <code>PluralFormat</code> object yet, the
     *        formatted number will be returned.
     *        Note: If this object is not an instance of <code>Number</code>,
     *              the <code>toAppendTo</code> will not be modified.
     * @param toAppendTo the formatted message will be appended to this
     *        <code>StringBuffer</code>.
     * @param pos will be ignored by this method.
     * @return the string buffer passed in as toAppendTo, with formatted text
     *         appended.
     * @throws IllegalArgumentException if number is not an instance of Number
     */
    @Override
    public StringBuffer format(Object number, StringBuffer toAppendTo,
            FieldPosition pos) {
        if (!(number instanceof Number)) {
            throw new IllegalArgumentException("'" + number + "' is not a Number");
        }
        Number numberObject = (Number) number;
        toAppendTo.append(format(numberObject, numberObject.doubleValue()));
        return toAppendTo;
    }

    private String format(Number numberObject, double number) {
        // If no pattern was applied, return the formatted number.
        if (msgPattern == null || msgPattern.countParts() == 0) {
            return numberFormat.format(numberObject);
        }

        // Get the appropriate sub-message.
        // Select it based on the formatted number-offset.
        double numberMinusOffset = number - offset;
        String numberString;
        if (offset == 0) {
            numberString = numberFormat.format(numberObject);  // could be BigDecimal etc.
        } else {
            numberString = numberFormat.format(numberMinusOffset);
        }
        FixedDecimal dec;
        if(numberFormat instanceof DecimalFormat) {
            dec = ((DecimalFormat) numberFormat).getFixedDecimal(numberMinusOffset);
        } else {
            dec = new FixedDecimal(numberMinusOffset);
        }
        int partIndex = findSubMessage(msgPattern, 0, pluralRulesWrapper, dec, number);
        // Replace syntactic # signs in the top level of this sub-message
        // (not in nested arguments) with the formatted number-offset.
        StringBuilder result = null;
        int prevIndex = msgPattern.getPart(partIndex).getLimit();
        for (;;) {
            MessagePattern.Part part = msgPattern.getPart(++partIndex);
            MessagePattern.Part.Type type = part.getType();
            int index = part.getIndex();
            if (type == MessagePattern.Part.Type.MSG_LIMIT) {
                if (result == null) {
                    return pattern.substring(prevIndex, index);
                } else {
                    return result.append(pattern, prevIndex, index).toString();
                }
            } else if (type == MessagePattern.Part.Type.REPLACE_NUMBER ||
                        // JDK compatibility mode: Remove SKIP_SYNTAX.
                        (type == MessagePattern.Part.Type.SKIP_SYNTAX && msgPattern.jdkAposMode())) {
                if (result == null) {
                    result = new StringBuilder();
                }
                result.append(pattern, prevIndex, index);
                if (type == MessagePattern.Part.Type.REPLACE_NUMBER) {
                    result.append(numberString);
                }
                prevIndex = part.getLimit();
            } else if (type == MessagePattern.Part.Type.ARG_START) {
                if (result == null) {
                    result = new StringBuilder();
                }
                result.append(pattern, prevIndex, index);
                prevIndex = index;
                partIndex = msgPattern.getLimitPartIndex(partIndex);
                index = msgPattern.getPart(partIndex).getLimit();
                MessagePattern.appendReducedApostrophes(pattern, prevIndex, index, result);
                prevIndex = index;
            }
        }
    }

    /**
     * This method is not yet supported by <code>PluralFormat</code>.
     * @param text the string to be parsed.
     * @param parsePosition defines the position where parsing is to begin,
     * and upon return, the position where parsing left off.  If the position
     * has not changed upon return, then parsing failed.
     * @return nothing because this method is not yet implemented.
     * @throws UnsupportedOperationException will always be thrown by this method.
     */
    public Number parse(String text, ParsePosition parsePosition) {
        // You get number ranges from this. You can't get an exact number.
        throw new UnsupportedOperationException();
    }

    /**
     * This method is not yet supported by <code>PluralFormat</code>.
     * @param source the string to be parsed.
     * @param pos defines the position where parsing is to begin,
     * and upon return, the position where parsing left off.  If the position
     * has not changed upon return, then parsing failed.
     * @return nothing because this method is not yet implemented.
     * @throws UnsupportedOperationException will always be thrown by this method.
     */
    @Override
    public Object parseObject(String source, ParsePosition pos) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method returns the PluralRules type found from parsing.
     * @param source the string to be parsed.
     * @param pos defines the position where parsing is to begin,
     * and upon return, the position where parsing left off.  If the position
     * is a negative index, then parsing failed.
     * @return Returns the PluralRules type. For example, it could be "zero", "one", "two", "few", "many" or "other")
     */
    /*package*/ String parseType(String source, RbnfLenientScanner scanner, FieldPosition pos) {
        // If no pattern was applied, return null.
        if (msgPattern == null || msgPattern.countParts() == 0) {
            pos.setBeginIndex(-1);
            pos.setEndIndex(-1);
            return null;
        }
        int partIndex = 0;
        int currMatchIndex;
        int count=msgPattern.countParts();
        int startingAt = pos.getBeginIndex();
        if (startingAt < 0) {
            startingAt = 0;
        }

        // The keyword is null until we need to match against a non-explicit, not-"other" value.
        // Then we get the keyword from the selector.
        // (In other words, we never call the selector if we match against an explicit value,
        // or if the only non-explicit keyword is "other".)
        String keyword = null;
        String matchedWord = null;
        int matchedIndex = -1;
        // Iterate over (ARG_SELECTOR ARG_START message ARG_LIMIT) tuples
        // until the end of the plural-only pattern.
        while (partIndex < count) {
            MessagePattern.Part partSelector=msgPattern.getPart(partIndex++);
            if (partSelector.getType() != MessagePattern.Part.Type.ARG_SELECTOR) {
                // Bad format
                continue;
            }

            MessagePattern.Part partStart=msgPattern.getPart(partIndex++);
            if (partStart.getType() != MessagePattern.Part.Type.MSG_START) {
                // Bad format
                continue;
            }

            MessagePattern.Part partLimit=msgPattern.getPart(partIndex++);
            if (partLimit.getType() != MessagePattern.Part.Type.MSG_LIMIT) {
                // Bad format
                continue;
            }

            String currArg = pattern.substring(partStart.getLimit(), partLimit.getIndex());
            if (scanner != null) {
                // If lenient parsing is turned ON, we've got some time consuming parsing ahead of us.
                int[] scannerMatchResult = scanner.findText(source, currArg, startingAt);
                currMatchIndex = scannerMatchResult[0];
            }
            else {
                currMatchIndex = source.indexOf(currArg, startingAt);
            }
            if (currMatchIndex >= 0 && currMatchIndex >= matchedIndex && (matchedWord == null || currArg.length() > matchedWord.length())) {
                matchedIndex = currMatchIndex;
                matchedWord = currArg;
                keyword = pattern.substring(partStart.getLimit(), partLimit.getIndex());
            }
        }
        if (keyword != null) {
            pos.setBeginIndex(matchedIndex);
            pos.setEndIndex(matchedIndex + matchedWord.length());
            return keyword;
        }

        // Not found!
        pos.setBeginIndex(-1);
        pos.setEndIndex(-1);
        return null;
    }

    /**
     * Sets the locale used by this <code>PluraFormat</code> object.
     * Note: Calling this method resets this <code>PluraFormat</code> object,
     *     i.e., a pattern that was applied previously will be removed,
     *     and the NumberFormat is set to the default number format for
     *     the locale.  The resulting format behaves the same as one
     *     constructed from {@link #PluralFormat(ULocale, PluralRules.PluralType)}
     *     with PluralType.CARDINAL.
     * @param ulocale the <code>ULocale</code> used to configure the
     *     formatter. If <code>ulocale</code> is <code>null</code>, the
     *     default <code>FORMAT</code> locale will be used.
     * @see Category#FORMAT
     * @deprecated ICU 50 This method clears the pattern and might create
     *             a different kind of PluralRules instance;
     *             use one of the constructors to create a new instance instead.
     * @hide original deprecated declaration
     */
    @Deprecated
    public void setLocale(ULocale ulocale) {
        if (ulocale == null) {
            ulocale = ULocale.getDefault(Category.FORMAT);
        }
        init(null, PluralType.CARDINAL, ulocale, null);
    }

    /**
     * Sets the number format used by this formatter.  You only need to
     * call this if you want a different number format than the default
     * formatter for the locale.
     * @param format the number format to use.
     */
    public void setNumberFormat(NumberFormat format) {
        numberFormat = format;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object rhs) {
        if(this == rhs) {
            return true;
        }
        if(rhs == null || getClass() != rhs.getClass()) {
            return false;
        }
        PluralFormat pf = (PluralFormat)rhs;
        return
            Utility.objectEquals(ulocale, pf.ulocale) &&
            Utility.objectEquals(pluralRules, pf.pluralRules) &&
            Utility.objectEquals(msgPattern, pf.msgPattern) &&
            Utility.objectEquals(numberFormat, pf.numberFormat);
    }

    /**
     * Returns true if this equals the provided PluralFormat.
     * @param rhs the PluralFormat to compare against
     * @return true if this equals rhs
     */
    public boolean equals(PluralFormat rhs) {
        return equals((Object)rhs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return pluralRules.hashCode() ^ parsedValues.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("locale=" + ulocale);
        buf.append(", rules='" + pluralRules + "'");
        buf.append(", pattern='" + pattern + "'");
        buf.append(", format='" + numberFormat + "'");
        return buf.toString();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        pluralRulesWrapper = new PluralSelectorAdapter();
        // Ignore the parsedValues from an earlier class version (before ICU 4.8)
        // and rebuild the msgPattern.
        parsedValues = null;
        if (pattern != null) {
            applyPattern(pattern);
        }
    }
}
