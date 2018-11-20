/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2015, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.text;

import com.google.j2objc.annotations.Weak;

import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.List;

import android.icu.impl.PatternProps;
import android.icu.impl.Utility;

/**
 * A class representing a single rule in a RuleBasedNumberFormat.  A rule
 * inserts its text into the result string and then passes control to its
 * substitutions, which do the same thing.
 */
final class NFRule {
    //-----------------------------------------------------------------------
    // constants
    //-----------------------------------------------------------------------

    /**
     * Special base value used to identify a negative-number rule
     */
    static final int NEGATIVE_NUMBER_RULE = -1;

    /**
     * Special base value used to identify an improper fraction (x.x) rule
     */
    static final int IMPROPER_FRACTION_RULE = -2;

    /**
     * Special base value used to identify a proper fraction (0.x) rule
     */
    static final int PROPER_FRACTION_RULE = -3;

    /**
     * Special base value used to identify a master rule
     */
    static final int MASTER_RULE = -4;

    /**
     * Special base value used to identify an infinity rule
     */
    static final int INFINITY_RULE = -5;

    /**
     * Special base value used to identify a not a number rule
     */
    static final int NAN_RULE = -6;

    static final Long ZERO = (long) 0;

    //-----------------------------------------------------------------------
    // data members
    //-----------------------------------------------------------------------

    /**
     * The rule's base value
     */
    private long baseValue;

    /**
     * The rule's radix (the radix to the power of the exponent equals
     * the rule's divisor)
     */
    private int radix = 10;

    /**
     * The rule's exponent (the radix raised to the power of the exponent
     * equals the rule's divisor)
     */
    private short exponent = 0;

    /**
     * If this is a fraction rule, this is the decimal point from DecimalFormatSymbols to match.
     */
    private char decimalPoint = 0;

    /**
     * The rule's rule text.  When formatting a number, the rule's text
     * is inserted into the result string, and then the text from any
     * substitutions is inserted into the result string
     */
    private String ruleText = null;

    /**
     * The rule's plural format when defined. This is not a substitution
     * because it only works on the current baseValue. It's normally not used
     * due to the overhead.
     */
    private PluralFormat rulePatternFormat = null;

    /**
     * The rule's first substitution (the one with the lower offset
     * into the rule text)
     */
    private NFSubstitution sub1 = null;

    /**
     * The rule's second substitution (the one with the higher offset
     * into the rule text)
     */
    private NFSubstitution sub2 = null;

    /**
     * The RuleBasedNumberFormat that owns this rule
     */
    @Weak private final RuleBasedNumberFormat formatter;

    //-----------------------------------------------------------------------
    // construction
    //-----------------------------------------------------------------------

    /**
     * Creates one or more rules based on the description passed in.
     * @param description The description of the rule(s).
     * @param owner The rule set containing the new rule(s).
     * @param predecessor The rule that precedes the new one(s) in "owner"'s
     * rule list
     * @param ownersOwner The RuleBasedNumberFormat that owns the
     * rule set that owns the new rule(s)
     * @param returnList One or more instances of NFRule are added and returned here
     */
    public static void makeRules(String                description,
                                   NFRuleSet             owner,
                                   NFRule                predecessor,
                                   RuleBasedNumberFormat ownersOwner,
                                   List<NFRule>          returnList) {
        // we know we're making at least one rule, so go ahead and
        // new it up and initialize its basevalue and divisor
        // (this also strips the rule descriptor, if any, off the
        // description string)
        NFRule rule1 = new NFRule(ownersOwner, description);
        description = rule1.ruleText;

        // check the description to see whether there's text enclosed
        // in brackets
        int brack1 = description.indexOf('[');
        int brack2 = brack1 < 0 ? -1 : description.indexOf(']');

        // if the description doesn't contain a matched pair of brackets,
        // or if it's of a type that doesn't recognize bracketed text,
        // then leave the description alone, initialize the rule's
        // rule text and substitutions, and return that rule
        if (brack2 < 0 || brack1 > brack2
            || rule1.baseValue == PROPER_FRACTION_RULE
            || rule1.baseValue == NEGATIVE_NUMBER_RULE
            || rule1.baseValue == INFINITY_RULE
            || rule1.baseValue == NAN_RULE)
        {
            rule1.extractSubstitutions(owner, description, predecessor);
        }
        else {
            // if the description does contain a matched pair of brackets,
            // then it's really shorthand for two rules (with one exception)
            NFRule rule2 = null;
            StringBuilder sbuf = new StringBuilder();

            // we'll actually only split the rule into two rules if its
            // base value is an even multiple of its divisor (or it's one
            // of the special rules)
            if ((rule1.baseValue > 0
                 && rule1.baseValue % (power(rule1.radix, rule1.exponent)) == 0)
                || rule1.baseValue == IMPROPER_FRACTION_RULE
                || rule1.baseValue == MASTER_RULE)
            {

                // if it passes that test, new up the second rule.  If the
                // rule set both rules will belong to is a fraction rule
                // set, they both have the same base value; otherwise,
                // increment the original rule's base value ("rule1" actually
                // goes SECOND in the rule set's rule list)
                rule2 = new NFRule(ownersOwner, null);
                if (rule1.baseValue >= 0) {
                    rule2.baseValue = rule1.baseValue;
                    if (!owner.isFractionSet()) {
                        ++rule1.baseValue;
                    }
                }
                else if (rule1.baseValue == IMPROPER_FRACTION_RULE) {
                    // if the description began with "x.x" and contains bracketed
                    // text, it describes both the improper fraction rule and
                    // the proper fraction rule
                    rule2.baseValue = PROPER_FRACTION_RULE;
                }
                else if (rule1.baseValue == MASTER_RULE) {
                    // if the description began with "x.0" and contains bracketed
                    // text, it describes both the master rule and the
                    // improper fraction rule
                    rule2.baseValue = rule1.baseValue;
                    rule1.baseValue = IMPROPER_FRACTION_RULE;
                }

                // both rules have the same radix and exponent (i.e., the
                // same divisor)
                rule2.radix = rule1.radix;
                rule2.exponent = rule1.exponent;

                // rule2's rule text omits the stuff in brackets: initialize
                // its rule text and substitutions accordingly
                sbuf.append(description.substring(0, brack1));
                if (brack2 + 1 < description.length()) {
                    sbuf.append(description.substring(brack2 + 1));
                }
                rule2.extractSubstitutions(owner, sbuf.toString(), predecessor);
            }

            // rule1's text includes the text in the brackets but omits
            // the brackets themselves: initialize _its_ rule text and
            // substitutions accordingly
            sbuf.setLength(0);
            sbuf.append(description.substring(0, brack1));
            sbuf.append(description.substring(brack1 + 1, brack2));
            if (brack2 + 1 < description.length()) {
                sbuf.append(description.substring(brack2 + 1));
            }
            rule1.extractSubstitutions(owner, sbuf.toString(), predecessor);

            // if we only have one rule, return it; if we have two, return
            // a two-element array containing them (notice that rule2 goes
            // BEFORE rule1 in the list: in all cases, rule2 OMITS the
            // material in the brackets and rule1 INCLUDES the material
            // in the brackets)
            if (rule2 != null) {
                if (rule2.baseValue >= 0) {
                    returnList.add(rule2);
                }
                else {
                    owner.setNonNumericalRule(rule2);
                }
            }
        }
        if (rule1.baseValue >= 0) {
            returnList.add(rule1);
        }
        else {
            owner.setNonNumericalRule(rule1);
        }
    }

    /**
     * Nominal constructor for NFRule.  Most of the work of constructing
     * an NFRule is actually performed by makeRules().
     */
    public NFRule(RuleBasedNumberFormat formatter, String ruleText) {
        this.formatter = formatter;
        this.ruleText = ruleText == null ? null : parseRuleDescriptor(ruleText);
    }

    /**
     * This function parses the rule's rule descriptor (i.e., the base
     * value and/or other tokens that precede the rule's rule text
     * in the description) and sets the rule's base value, radix, and
     * exponent according to the descriptor.  (If the description doesn't
     * include a rule descriptor, then this function sets everything to
     * default values and the rule set sets the rule's real base value).
     * @param description The rule's description
     * @return If "description" included a rule descriptor, this is
     * "description" with the descriptor and any trailing whitespace
     * stripped off.  Otherwise; it's "descriptor" unchanged.
     */
    private String parseRuleDescriptor(String description) {
        String descriptor;

        // the description consists of a rule descriptor and a rule body,
        // separated by a colon.  The rule descriptor is optional.  If
        // it's omitted, just set the base value to 0.
        int p = description.indexOf(":");
        if (p != -1) {
            // copy the descriptor out into its own string and strip it,
            // along with any trailing whitespace, out of the original
            // description
            descriptor = description.substring(0, p);
            ++p;
            while (p < description.length() && PatternProps.isWhiteSpace(description.charAt(p))) {
                ++p;
            }
            description = description.substring(p);

            // check first to see if the rule descriptor matches the token
            // for one of the special rules.  If it does, set the base
            // value to the correct identifier value
            int descriptorLength = descriptor.length();
            char firstChar = descriptor.charAt(0);
            char lastChar = descriptor.charAt(descriptorLength - 1);
            if (firstChar >= '0' && firstChar <= '9' && lastChar != 'x') {
                // if the rule descriptor begins with a digit, it's a descriptor
                // for a normal rule
                long tempValue = 0;
                char c = 0;
                p = 0;

                // begin parsing the descriptor: copy digits
                // into "tempValue", skip periods, commas, and spaces,
                // stop on a slash or > sign (or at the end of the string),
                // and throw an exception on any other character
                while (p < descriptorLength) {
                    c = descriptor.charAt(p);
                    if (c >= '0' && c <= '9') {
                        tempValue = tempValue * 10 + (c - '0');
                    }
                    else if (c == '/' || c == '>') {
                        break;
                    }
                    else if (!PatternProps.isWhiteSpace(c) && c != ',' && c != '.') {
                        throw new IllegalArgumentException("Illegal character " + c + " in rule descriptor");
                    }
                    ++p;
                }

                // Set the rule's base value according to what we parsed
                setBaseValue(tempValue);

                // if we stopped the previous loop on a slash, we're
                // now parsing the rule's radix.  Again, accumulate digits
                // in tempValue, skip punctuation, stop on a > mark, and
                // throw an exception on anything else
                if (c == '/') {
                    tempValue = 0;
                    ++p;
                    while (p < descriptorLength) {
                        c = descriptor.charAt(p);
                        if (c >= '0' && c <= '9') {
                            tempValue = tempValue * 10 + (c - '0');
                        }
                        else if (c == '>') {
                            break;
                        }
                        else if (!PatternProps.isWhiteSpace(c) && c != ',' && c != '.') {
                            throw new IllegalArgumentException("Illegal character " + c + " in rule descriptor");
                        }
                        ++p;
                    }

                    // tempValue now contains the rule's radix.  Set it
                    // accordingly, and recalculate the rule's exponent
                    radix = (int)tempValue;
                    if (radix == 0) {
                        throw new IllegalArgumentException("Rule can't have radix of 0");
                    }
                    exponent = expectedExponent();
                }

                // if we stopped the previous loop on a > sign, then continue
                // for as long as we still see > signs.  For each one,
                // decrement the exponent (unless the exponent is already 0).
                // If we see another character before reaching the end of
                // the descriptor, that's also a syntax error.
                if (c == '>') {
                    while (p < descriptorLength) {
                        c = descriptor.charAt(p);
                        if (c == '>' && exponent > 0) {
                            --exponent;
                        } else {
                            throw new IllegalArgumentException("Illegal character in rule descriptor");
                        }
                        ++p;
                    }
                }
            }
            else if (descriptor.equals("-x")) {
                setBaseValue(NEGATIVE_NUMBER_RULE);
            }
            else if (descriptorLength == 3) {
                if (firstChar == '0' && lastChar == 'x') {
                    setBaseValue(PROPER_FRACTION_RULE);
                    decimalPoint = descriptor.charAt(1);
                }
                else if (firstChar == 'x' && lastChar == 'x') {
                    setBaseValue(IMPROPER_FRACTION_RULE);
                    decimalPoint = descriptor.charAt(1);
                }
                else if (firstChar == 'x' && lastChar == '0') {
                    setBaseValue(MASTER_RULE);
                    decimalPoint = descriptor.charAt(1);
                }
                else if (descriptor.equals("NaN")) {
                    setBaseValue(NAN_RULE);
                }
                else if (descriptor.equals("Inf")) {
                    setBaseValue(INFINITY_RULE);
                }
            }
        }
        // else use the default base value for now.

        // finally, if the rule body begins with an apostrophe, strip it off
        // (this is generally used to put whitespace at the beginning of
        // a rule's rule text)
        if (description.length() > 0 && description.charAt(0) == '\'') {
            description = description.substring(1);
        }

        // return the description with all the stuff we've just waded through
        // stripped off the front.  It now contains just the rule body.
        return description;
    }

    /**
     * Searches the rule's rule text for the substitution tokens,
     * creates the substitutions, and removes the substitution tokens
     * from the rule's rule text.
     * @param owner The rule set containing this rule
     * @param predecessor The rule preceding this one in "owners" rule list
     * @param ruleText The rule text
     */
    private void extractSubstitutions(NFRuleSet             owner,
                                      String                ruleText,
                                      NFRule                predecessor) {
        this.ruleText = ruleText;
        sub1 = extractSubstitution(owner, predecessor);
        if (sub1 == null) {
            // Small optimization. There is no need to create a redundant NullSubstitution.
            sub2 = null;
        }
        else {
            sub2 = extractSubstitution(owner, predecessor);
        }
        ruleText = this.ruleText;
        int pluralRuleStart = ruleText.indexOf("$(");
        int pluralRuleEnd = (pluralRuleStart >= 0 ? ruleText.indexOf(")$", pluralRuleStart) : -1);
        if (pluralRuleEnd >= 0) {
            int endType = ruleText.indexOf(',', pluralRuleStart);
            if (endType < 0) {
                throw new IllegalArgumentException("Rule \"" + ruleText + "\" does not have a defined type");
            }
            String type = this.ruleText.substring(pluralRuleStart + 2, endType);
            PluralRules.PluralType pluralType;
            if ("cardinal".equals(type)) {
                pluralType = PluralRules.PluralType.CARDINAL;
            }
            else if ("ordinal".equals(type)) {
                pluralType = PluralRules.PluralType.ORDINAL;
            }
            else {
                throw new IllegalArgumentException(type + " is an unknown type");
            }
            rulePatternFormat = formatter.createPluralFormat(pluralType,
                    ruleText.substring(endType + 1, pluralRuleEnd));
        }
    }

    /**
     * Searches the rule's rule text for the first substitution token,
     * creates a substitution based on it, and removes the token from
     * the rule's rule text.
     * @param owner The rule set containing this rule
     * @param predecessor The rule preceding this one in the rule set's
     * rule list
     * @return The newly-created substitution.  This is never null; if
     * the rule text doesn't contain any substitution tokens, this will
     * be a NullSubstitution.
     */
    private NFSubstitution extractSubstitution(NFRuleSet             owner,
                                               NFRule                predecessor) {
        NFSubstitution result;
        int subStart;
        int subEnd;

        // search the rule's rule text for the first two characters of
        // a substitution token
        subStart = indexOfAnyRulePrefix(ruleText);

        // if we didn't find one, create a null substitution positioned
        // at the end of the rule text
        if (subStart == -1) {
            return null;
        }

        // special-case the ">>>" token, since searching for the > at the
        // end will actually find the > in the middle
        if (ruleText.startsWith(">>>", subStart)) {
            subEnd = subStart + 2;
        }
        else {
            // otherwise the substitution token ends with the same character
            // it began with
            char c = ruleText.charAt(subStart);
            subEnd = ruleText.indexOf(c, subStart + 1);
            // special case for '<%foo<<'
            if (c == '<' && subEnd != -1 && subEnd < ruleText.length() - 1 && ruleText.charAt(subEnd+1) == c) {
                // ordinals use "=#,##0==%abbrev=" as their rule.  Notice that the '==' in the middle
                // occurs because of the juxtaposition of two different rules.  The check for '<' is a hack
                // to get around this.  Having the duplicate at the front would cause problems with
                // rules like "<<%" to format, say, percents...
                ++subEnd;
            }
        }

        // if we don't find the end of the token (i.e., if we're on a single,
        // unmatched token character), create a null substitution positioned
        // at the end of the rule
        if (subEnd == -1) {
            return null;
        }

        // if we get here, we have a real substitution token (or at least
        // some text bounded by substitution token characters).  Use
        // makeSubstitution() to create the right kind of substitution
        result = NFSubstitution.makeSubstitution(subStart, this, predecessor, owner,
                this.formatter, ruleText.substring(subStart, subEnd + 1));

        // remove the substitution from the rule text
        ruleText = ruleText.substring(0, subStart) + ruleText.substring(subEnd + 1);
        return result;
    }

    /**
     * Sets the rule's base value, and causes the radix and exponent
     * to be recalculated.  This is used during construction when we
     * don't know the rule's base value until after it's been
     * constructed.  It should not be used at any other time.
     * @param newBaseValue The new base value for the rule.
     */
    final void setBaseValue(long newBaseValue) {
        // set the base value
        baseValue = newBaseValue;
        radix = 10;

        // if this isn't a special rule, recalculate the radix and exponent
        // (the radix always defaults to 10; if it's supposed to be something
        // else, it's cleaned up by the caller and the exponent is
        // recalculated again-- the only function that does this is
        // NFRule.parseRuleDescriptor() )
        if (baseValue >= 1) {
            exponent = expectedExponent();

            // this function gets called on a fully-constructed rule whose
            // description didn't specify a base value.  This means it
            // has substitutions, and some substitutions hold on to copies
            // of the rule's divisor.  Fix their copies of the divisor.
            if (sub1 != null) {
                sub1.setDivisor(radix, exponent);
            }
            if (sub2 != null) {
                sub2.setDivisor(radix, exponent);
            }
        }
        else {
            // if this is a special rule, its radix and exponent are basically
            // ignored.  Set them to "safe" default values
            exponent = 0;
        }
    }

    /**
     * This calculates the rule's exponent based on its radix and base
     * value.  This will be the highest power the radix can be raised to
     * and still produce a result less than or equal to the base value.
     */
    private short expectedExponent() {
        // since the log of 0, or the log base 0 of something, causes an
        // error, declare the exponent in these cases to be 0 (we also
        // deal with the special-rule identifiers here)
        if (radix == 0 || baseValue < 1) {
            return 0;
        }

        // we get rounding error in some cases-- for example, log 1000 / log 10
        // gives us 1.9999999996 instead of 2.  The extra logic here is to take
        // that into account
        short tempResult = (short)(Math.log(baseValue) / Math.log(radix));
        if (power(radix, (short)(tempResult + 1)) <= baseValue) {
            return (short)(tempResult + 1);
        } else {
            return tempResult;
        }
    }

    private static final String[] RULE_PREFIXES = new String[] {
            "<<", "<%", "<#", "<0",
            ">>", ">%", ">#", ">0",
            "=%", "=#", "=0"
    };

    /**
     * Searches the rule's rule text for any of the specified strings.
     * @return The index of the first match in the rule's rule text
     * (i.e., the first substring in the rule's rule text that matches
     * _any_ of the strings in "strings").  If none of the strings in
     * "strings" is found in the rule's rule text, returns -1.
     */
    private static int indexOfAnyRulePrefix(String ruleText) {
        int result = -1;
        if (ruleText.length() > 0) {
            int pos;
            for (String string : RULE_PREFIXES) {
                pos = ruleText.indexOf(string);
                if (pos != -1 && (result == -1 || pos < result)) {
                    result = pos;
                }
            }
        }
        return result;
    }

    //-----------------------------------------------------------------------
    // boilerplate
    //-----------------------------------------------------------------------

    /**
     * Tests two rules for equality.
     * @param that The rule to compare this one against
     * @return True if the two rules are functionally equivalent
     */
    public boolean equals(Object that) {
        if (that instanceof NFRule) {
            NFRule that2 = (NFRule)that;

            return baseValue == that2.baseValue
                && radix == that2.radix
                && exponent == that2.exponent
                && ruleText.equals(that2.ruleText)
                && Utility.objectEquals(sub1, that2.sub1)
                && Utility.objectEquals(sub2, that2.sub2);
        }
        return false;
    }
    
    public int hashCode() {
        assert false : "hashCode not designed";
        return 42;
    }

    /**
     * Returns a textual representation of the rule.  This won't
     * necessarily be the same as the description that this rule
     * was created with, but it will produce the same result.
     * @return A textual description of the rule
     */
    public String toString() {
        StringBuilder result = new StringBuilder();

        // start with the rule descriptor.  Special-case the special rules
        if (baseValue == NEGATIVE_NUMBER_RULE) {
            result.append("-x: ");
        }
        else if (baseValue == IMPROPER_FRACTION_RULE) {
            result.append('x').append(decimalPoint == 0 ? '.' : decimalPoint).append("x: ");
        }
        else if (baseValue == PROPER_FRACTION_RULE) {
            result.append('0').append(decimalPoint == 0 ? '.' : decimalPoint).append("x: ");
        }
        else if (baseValue == MASTER_RULE) {
            result.append('x').append(decimalPoint == 0 ? '.' : decimalPoint).append("0: ");
        }
        else if (baseValue == INFINITY_RULE) {
            result.append("Inf: ");
        }
        else if (baseValue == NAN_RULE) {
            result.append("NaN: ");
        }
        else {
            // for a normal rule, write out its base value, and if the radix is
            // something other than 10, write out the radix (with the preceding
            // slash, of course).  Then calculate the expected exponent and if
            // if isn't the same as the actual exponent, write an appropriate
            // number of > signs.  Finally, terminate the whole thing with
            // a colon.
            result.append(String.valueOf(baseValue));
            if (radix != 10) {
                result.append('/').append(radix);
            }
            int numCarets = expectedExponent() - exponent;
            for (int i = 0; i < numCarets; i++)
                result.append('>');
            result.append(": ");
        }

        // if the rule text begins with a space, write an apostrophe
        // (whitespace after the rule descriptor is ignored; the
        // apostrophe is used to make the whitespace significant)
        if (ruleText.startsWith(" ") && (sub1 == null || sub1.getPos() != 0)) {
            result.append('\'');
        }

        // now, write the rule's rule text, inserting appropriate
        // substitution tokens in the appropriate places
        StringBuilder ruleTextCopy = new StringBuilder(ruleText);
        if (sub2 != null) {
            ruleTextCopy.insert(sub2.getPos(), sub2.toString());
        }
        if (sub1 != null) {
            ruleTextCopy.insert(sub1.getPos(), sub1.toString());
        }
        result.append(ruleTextCopy.toString());

        // and finally, top the whole thing off with a semicolon and
        // return the result
        result.append(';');
        return result.toString();
    }

    //-----------------------------------------------------------------------
    // simple accessors
    //-----------------------------------------------------------------------

    /**
     * Returns the rule's base value
     * @return The rule's base value
     */
    public final char getDecimalPoint() {
        return decimalPoint;
    }

    /**
     * Returns the rule's base value
     * @return The rule's base value
     */
    public final long getBaseValue() {
        return baseValue;
    }

    /**
     * Returns the rule's divisor (the value that cotrols the behavior
     * of its substitutions)
     * @return The rule's divisor
     */
    public long getDivisor() {
        return power(radix, exponent);
    }

    //-----------------------------------------------------------------------
    // formatting
    //-----------------------------------------------------------------------

    /**
     * Formats the number, and inserts the resulting text into
     * toInsertInto.
     * @param number The number being formatted
     * @param toInsertInto The string where the resultant text should
     * be inserted
     * @param pos The position in toInsertInto where the resultant text
     * should be inserted
     */
    public void doFormat(long number, StringBuilder toInsertInto, int pos, int recursionCount) {
        // first, insert the rule's rule text into toInsertInto at the
        // specified position, then insert the results of the substitutions
        // into the right places in toInsertInto (notice we do the
        // substitutions in reverse order so that the offsets don't get
        // messed up)
        int pluralRuleStart = ruleText.length();
        int lengthOffset = 0;
        if (rulePatternFormat == null) {
            toInsertInto.insert(pos, ruleText);
        }
        else {
            pluralRuleStart = ruleText.indexOf("$(");
            int pluralRuleEnd = ruleText.indexOf(")$", pluralRuleStart);
            int initialLength = toInsertInto.length();
            if (pluralRuleEnd < ruleText.length() - 1) {
                toInsertInto.insert(pos, ruleText.substring(pluralRuleEnd + 2));
            }
            toInsertInto.insert(pos, rulePatternFormat.format(number / power(radix, exponent)));
            if (pluralRuleStart > 0) {
                toInsertInto.insert(pos, ruleText.substring(0, pluralRuleStart));
            }
            lengthOffset = ruleText.length() - (toInsertInto.length() - initialLength);
        }
        if (sub2 != null) {
            sub2.doSubstitution(number, toInsertInto, pos - (sub2.getPos() > pluralRuleStart ? lengthOffset : 0), recursionCount);
        }
        if (sub1 != null) {
            sub1.doSubstitution(number, toInsertInto, pos - (sub1.getPos() > pluralRuleStart ? lengthOffset : 0), recursionCount);
        }
    }

    /**
     * Formats the number, and inserts the resulting text into
     * toInsertInto.
     * @param number The number being formatted
     * @param toInsertInto The string where the resultant text should
     * be inserted
     * @param pos The position in toInsertInto where the resultant text
     * should be inserted
     */
    public void doFormat(double number, StringBuilder toInsertInto, int pos, int recursionCount) {
        // first, insert the rule's rule text into toInsertInto at the
        // specified position, then insert the results of the substitutions
        // into the right places in toInsertInto
        // [again, we have two copies of this routine that do the same thing
        // so that we don't sacrifice precision in a long by casting it
        // to a double]
        int pluralRuleStart = ruleText.length();
        int lengthOffset = 0;
        if (rulePatternFormat == null) {
            toInsertInto.insert(pos, ruleText);
        }
        else {
            pluralRuleStart = ruleText.indexOf("$(");
            int pluralRuleEnd = ruleText.indexOf(")$", pluralRuleStart);
            int initialLength = toInsertInto.length();
            if (pluralRuleEnd < ruleText.length() - 1) {
                toInsertInto.insert(pos, ruleText.substring(pluralRuleEnd + 2));
            }
            double pluralVal = number;
            if (0 <= pluralVal && pluralVal < 1) {
                // We're in a fractional rule, and we have to match the NumeratorSubstitution behavior.
                // 2.3 can become 0.2999999999999998 for the fraction due to rounding errors.
                pluralVal = Math.round(pluralVal * power(radix, exponent));
            }
            else {
                pluralVal = pluralVal / power(radix, exponent);
            }
            toInsertInto.insert(pos, rulePatternFormat.format((long)(pluralVal)));
            if (pluralRuleStart > 0) {
                toInsertInto.insert(pos, ruleText.substring(0, pluralRuleStart));
            }
            lengthOffset = ruleText.length() - (toInsertInto.length() - initialLength);
        }
        if (sub2 != null) {
            sub2.doSubstitution(number, toInsertInto, pos - (sub2.getPos() > pluralRuleStart ? lengthOffset : 0), recursionCount);
        }
        if (sub1 != null) {
            sub1.doSubstitution(number, toInsertInto, pos - (sub1.getPos() > pluralRuleStart ? lengthOffset : 0), recursionCount);
        }
    }

    /**
     * This is an equivalent to Math.pow that accurately works on 64-bit numbers
     * @param base The base
     * @param exponent The exponent
     * @return radix ** exponent
     * @see Math#pow(double, double)
     */
    static long power(long base, short exponent) {
        if (exponent < 0) {
            throw new IllegalArgumentException("Exponent can not be negative");
        }
        if (base < 0) {
            throw new IllegalArgumentException("Base can not be negative");
        }
        long result = 1;
        while (exponent > 0) {
            if ((exponent & 1) == 1) {
                result *= base;
            }
            base *= base;
            exponent >>= 1;
        }
        return result;
    }

    /**
     * Used by the owning rule set to determine whether to invoke the
     * rollback rule (i.e., whether this rule or the one that precedes
     * it in the rule set's list should be used to format the number)
     * @param number The number being formatted
     * @return True if the rule set should use the rule that precedes
     * this one in its list; false if it should use this rule
     */
    public boolean shouldRollBack(long number) {
        // we roll back if the rule contains a modulus substitution,
        // the number being formatted is an even multiple of the rule's
        // divisor, and the rule's base value is NOT an even multiple
        // of its divisor
        // In other words, if the original description had
        //    100: << hundred[ >>];
        // that expands into
        //    100: << hundred;
        //    101: << hundred >>;
        // internally.  But when we're formatting 200, if we use the rule
        // at 101, which would normally apply, we get "two hundred zero".
        // To prevent this, we roll back and use the rule at 100 instead.
        // This is the logic that makes this happen: the rule at 101 has
        // a modulus substitution, its base value isn't an even multiple
        // of 100, and the value we're trying to format _is_ an even
        // multiple of 100.  This is called the "rollback rule."
        if (!((sub1 != null && sub1.isModulusSubstitution()) || (sub2 != null && sub2.isModulusSubstitution()))) {
            return false;
        }
        long divisor = power(radix, exponent);
        return (number % divisor) == 0 && (baseValue % divisor) != 0;
    }

    //-----------------------------------------------------------------------
    // parsing
    //-----------------------------------------------------------------------

    /**
     * Attempts to parse the string with this rule.
     * @param text The string being parsed
     * @param parsePosition On entry, the value is ignored and assumed to
     * be 0. On exit, this has been updated with the position of the first
     * character not consumed by matching the text against this rule
     * (if this rule doesn't match the text at all, the parse position
     * if left unchanged (presumably at 0) and the function returns
     * new Long(0)).
     * @param isFractionRule True if this rule is contained within a
     * fraction rule set.  This is only used if the rule has no
     * substitutions.
     * @return If this rule matched the text, this is the rule's base value
     * combined appropriately with the results of parsing the substitutions.
     * If nothing matched, this is new Long(0) and the parse position is
     * left unchanged.  The result will be an instance of Long if the
     * result is an integer and Double otherwise.  The result is never null.
     */
    public Number doParse(String text, ParsePosition parsePosition, boolean isFractionRule,
                          double upperBound) {

        // internally we operate on a copy of the string being parsed
        // (because we're going to change it) and use our own ParsePosition
        ParsePosition pp = new ParsePosition(0);

        // check to see whether the text before the first substitution
        // matches the text at the beginning of the string being
        // parsed.  If it does, strip that off the front of workText;
        // otherwise, dump out with a mismatch
        int sub1Pos = sub1 != null ? sub1.getPos() : ruleText.length();
        int sub2Pos = sub2 != null ? sub2.getPos() : ruleText.length();
        String workText = stripPrefix(text, ruleText.substring(0, sub1Pos), pp);
        int prefixLength = text.length() - workText.length();

        if (pp.getIndex() == 0 && sub1Pos != 0) {
            // commented out because ParsePosition doesn't have error index in 1.1.x
            //                parsePosition.setErrorIndex(pp.getErrorIndex());
            return ZERO;
        }
        if (baseValue == INFINITY_RULE) {
            // If you match this, don't try to perform any calculations on it.
            parsePosition.setIndex(pp.getIndex());
            return Double.POSITIVE_INFINITY;
        }
        if (baseValue == NAN_RULE) {
            // If you match this, don't try to perform any calculations on it.
            parsePosition.setIndex(pp.getIndex());
            return Double.NaN;
        }

        // this is the fun part.  The basic guts of the rule-matching
        // logic is matchToDelimiter(), which is called twice.  The first
        // time it searches the input string for the rule text BETWEEN
        // the substitutions and tries to match the intervening text
        // in the input string with the first substitution.  If that
        // succeeds, it then calls it again, this time to look for the
        // rule text after the second substitution and to match the
        // intervening input text against the second substitution.
        //
        // For example, say we have a rule that looks like this:
        //    first << middle >> last;
        // and input text that looks like this:
        //    first one middle two last
        // First we use stripPrefix() to match "first " in both places and
        // strip it off the front, leaving
        //    one middle two last
        // Then we use matchToDelimiter() to match " middle " and try to
        // match "one" against a substitution.  If it's successful, we now
        // have
        //    two last
        // We use matchToDelimiter() a second time to match " last" and
        // try to match "two" against a substitution.  If "two" matches
        // the substitution, we have a successful parse.
        //
        // Since it's possible in many cases to find multiple instances
        // of each of these pieces of rule text in the input string,
        // we need to try all the possible combinations of these
        // locations.  This prevents us from prematurely declaring a mismatch,
        // and makes sure we match as much input text as we can.
        int highWaterMark = 0;
        double result = 0;
        int start = 0;
        double tempBaseValue = Math.max(0, baseValue);

        do {
            // our partial parse result starts out as this rule's base
            // value.  If it finds a successful match, matchToDelimiter()
            // will compose this in some way with what it gets back from
            // the substitution, giving us a new partial parse result
            pp.setIndex(0);
            double partialResult = matchToDelimiter(workText, start, tempBaseValue,
                                                    ruleText.substring(sub1Pos, sub2Pos), rulePatternFormat,
                                                    pp, sub1, upperBound).doubleValue();

            // if we got a successful match (or were trying to match a
            // null substitution), pp is now pointing at the first unmatched
            // character.  Take note of that, and try matchToDelimiter()
            // on the input text again
            if (pp.getIndex() != 0 || sub1 == null) {
                start = pp.getIndex();

                String workText2 = workText.substring(pp.getIndex());
                ParsePosition pp2 = new ParsePosition(0);

                // the second matchToDelimiter() will compose our previous
                // partial result with whatever it gets back from its
                // substitution if there's a successful match, giving us
                // a real result
                partialResult = matchToDelimiter(workText2, 0, partialResult,
                                                 ruleText.substring(sub2Pos), rulePatternFormat, pp2, sub2,
                                                 upperBound).doubleValue();

                // if we got a successful match on this second
                // matchToDelimiter() call, update the high-water mark
                // and result (if necessary)
                if (pp2.getIndex() != 0 || sub2 == null) {
                    if (prefixLength + pp.getIndex() + pp2.getIndex() > highWaterMark) {
                        highWaterMark = prefixLength + pp.getIndex() + pp2.getIndex();
                        result = partialResult;
                    }
                }
                // commented out because ParsePosition doesn't have error index in 1.1.x
                //                    else {
                //                        int temp = pp2.getErrorIndex() + sub1.getPos() + pp.getIndex();
                //                        if (temp> parsePosition.getErrorIndex()) {
                //                            parsePosition.setErrorIndex(temp);
                //                        }
                //                    }
            }
            // commented out because ParsePosition doesn't have error index in 1.1.x
            //                else {
            //                    int temp = sub1.getPos() + pp.getErrorIndex();
            //                    if (temp > parsePosition.getErrorIndex()) {
            //                        parsePosition.setErrorIndex(temp);
            //                    }
            //                }
            // keep trying to match things until the outer matchToDelimiter()
            // call fails to make a match (each time, it picks up where it
            // left off the previous time)
        }
        while (sub1Pos != sub2Pos && pp.getIndex() > 0 && pp.getIndex()
                 < workText.length() && pp.getIndex() != start);

        // update the caller's ParsePosition with our high-water mark
        // (i.e., it now points at the first character this function
        // didn't match-- the ParsePosition is therefore unchanged if
        // we didn't match anything)
        parsePosition.setIndex(highWaterMark);
        // commented out because ParsePosition doesn't have error index in 1.1.x
        //        if (highWaterMark > 0) {
        //            parsePosition.setErrorIndex(0);
        //        }

        // this is a hack for one unusual condition: Normally, whether this
        // rule belong to a fraction rule set or not is handled by its
        // substitutions.  But if that rule HAS NO substitutions, then
        // we have to account for it here.  By definition, if the matching
        // rule in a fraction rule set has no substitutions, its numerator
        // is 1, and so the result is the reciprocal of its base value.
        if (isFractionRule && highWaterMark > 0 && sub1 == null) {
            result = 1 / result;
        }

        // return the result as a Long if possible, or as a Double
        if (result == (long)result) {
            return Long.valueOf((long)result);
        } else {
            return new Double(result);
        }
    }

    /**
     * This function is used by parse() to match the text being parsed
     * against a possible prefix string.  This function
     * matches characters from the beginning of the string being parsed
     * to characters from the prospective prefix.  If they match, pp is
     * updated to the first character not matched, and the result is
     * the unparsed part of the string.  If they don't match, the whole
     * string is returned, and pp is left unchanged.
     * @param text The string being parsed
     * @param prefix The text to match against
     * @param pp On entry, ignored and assumed to be 0.  On exit, points
     * to the first unmatched character (assuming the whole prefix matched),
     * or is unchanged (if the whole prefix didn't match).
     * @return If things match, this is the unparsed part of "text";
     * if they didn't match, this is "text".
     */
    private String stripPrefix(String text, String prefix, ParsePosition pp) {
        // if the prefix text is empty, dump out without doing anything
        if (prefix.length() == 0) {
            return text;
        } else {
            // otherwise, use prefixLength() to match the beginning of
            // "text" against "prefix".  This function returns the
            // number of characters from "text" that matched (or 0 if
            // we didn't match the whole prefix)
            int pfl = prefixLength(text, prefix);
            if (pfl != 0) {
                // if we got a successful match, update the parse position
                // and strip the prefix off of "text"
                pp.setIndex(pp.getIndex() + pfl);
                return text.substring(pfl);

                // if we didn't get a successful match, leave everything alone
            } else {
                return text;
            }
        }
    }

    /**
     * Used by parse() to match a substitution and any following text.
     * "text" is searched for instances of "delimiter".  For each instance
     * of delimiter, the intervening text is tested to see whether it
     * matches the substitution.  The longest match wins.
     * @param text The string being parsed
     * @param startPos The position in "text" where we should start looking
     * for "delimiter".
     * @param baseVal A partial parse result (often the rule's base value),
     * which is combined with the result from matching the substitution
     * @param delimiter The string to search "text" for.
     * @param pp Ignored and presumed to be 0 on entry.  If there's a match,
     * on exit this will point to the first unmatched character.
     * @param sub If we find "delimiter" in "text", this substitution is used
     * to match the text between the beginning of the string and the
     * position of "delimiter."  (If "delimiter" is the empty string, then
     * this function just matches against this substitution and updates
     * everything accordingly.)
     * @param upperBound When matching the substitution, it will only
     * consider rules with base values lower than this value.
     * @return If there's a match, this is the result of composing
     * baseValue with the result of matching the substitution.  Otherwise,
     * this is new Long(0).  It's never null.  If the result is an integer,
     * this will be an instance of Long; otherwise, it's an instance of
     * Double.
     */
    private Number matchToDelimiter(String text, int startPos, double baseVal,
                                    String delimiter, PluralFormat pluralFormatDelimiter, ParsePosition pp, NFSubstitution sub, double upperBound) {
        // if "delimiter" contains real (i.e., non-ignorable) text, search
        // it for "delimiter" beginning at "start".  If that succeeds, then
        // use "sub"'s doParse() method to match the text before the
        // instance of "delimiter" we just found.
        if (!allIgnorable(delimiter)) {
            ParsePosition tempPP = new ParsePosition(0);
            Number tempResult;

            // use findText() to search for "delimiter".  It returns a two-
            // element array: element 0 is the position of the match, and
            // element 1 is the number of characters that matched
            // "delimiter".
            int[] temp = findText(text, delimiter, pluralFormatDelimiter, startPos);
            int dPos = temp[0];
            int dLen = temp[1];

            // if findText() succeeded, isolate the text preceding the
            // match, and use "sub" to match that text
            while (dPos >= 0) {
                String subText = text.substring(0, dPos);
                if (subText.length() > 0) {
                    tempResult = sub.doParse(subText, tempPP, baseVal, upperBound,
                                             formatter.lenientParseEnabled());

                    // if the substitution could match all the text up to
                    // where we found "delimiter", then this function has
                    // a successful match.  Bump the caller's parse position
                    // to point to the first character after the text
                    // that matches "delimiter", and return the result
                    // we got from parsing the substitution.
                    if (tempPP.getIndex() == dPos) {
                        pp.setIndex(dPos + dLen);
                        return tempResult;
                    }
                    // commented out because ParsePosition doesn't have error index in 1.1.x
                    //                    else {
                    //                        if (tempPP.getErrorIndex() > 0) {
                    //                            pp.setErrorIndex(tempPP.getErrorIndex());
                    //                        } else {
                    //                            pp.setErrorIndex(tempPP.getIndex());
                    //                        }
                    //                    }
                }

                // if we didn't match the substitution, search for another
                // copy of "delimiter" in "text" and repeat the loop if
                // we find it
                tempPP.setIndex(0);
                temp = findText(text, delimiter, pluralFormatDelimiter, dPos + dLen);
                dPos = temp[0];
                dLen = temp[1];
            }
            // if we make it here, this was an unsuccessful match, and we
            // leave pp unchanged and return 0
            pp.setIndex(0);
            return ZERO;

            // if "delimiter" is empty, or consists only of ignorable characters
            // (i.e., is semantically empty), thwe we obviously can't search
            // for "delimiter".  Instead, just use "sub" to parse as much of
            // "text" as possible.
        }
        else if (sub == null) {
            return baseVal;
        }
        else {
            ParsePosition tempPP = new ParsePosition(0);
            Number result = ZERO;
            // try to match the whole string against the substitution
            Number tempResult = sub.doParse(text, tempPP, baseVal, upperBound,
                    formatter.lenientParseEnabled());
            if (tempPP.getIndex() != 0) {
                // if there's a successful match (or it's a null
                // substitution), update pp to point to the first
                // character we didn't match, and pass the result from
                // sub.doParse() on through to the caller
                pp.setIndex(tempPP.getIndex());
                if (tempResult != null) {
                    result = tempResult;
                }
            }
            // commented out because ParsePosition doesn't have error index in 1.1.x
            //            else {
            //                pp.setErrorIndex(tempPP.getErrorIndex());
            //            }

            // and if we get to here, then nothing matched, so we return
            // 0 and leave pp alone
            return result;
        }
    }

    /**
     * Used by stripPrefix() to match characters.  If lenient parse mode
     * is off, this just calls startsWith().  If lenient parse mode is on,
     * this function uses CollationElementIterators to match characters in
     * the strings (only primary-order differences are significant in
     * determining whether there's a match).
     * @param str The string being tested
     * @param prefix The text we're hoping to see at the beginning
     * of "str"
     * @return If "prefix" is found at the beginning of "str", this
     * is the number of characters in "str" that were matched (this
     * isn't necessarily the same as the length of "prefix" when matching
     * text with a collator).  If there's no match, this is 0.
     */
    private int prefixLength(String str, String prefix) {
        // if we're looking for an empty prefix, it obviously matches
        // zero characters.  Just go ahead and return 0.
        if (prefix.length() == 0) {
            return 0;
        }

        RbnfLenientScanner scanner = formatter.getLenientScanner();
        if (scanner != null) {
            return scanner.prefixLength(str, prefix);
        }

        // If lenient parsing is turned off, forget all that crap above.
        // Just use String.startsWith() and be done with it.
        if (str.startsWith(prefix)) {
            return prefix.length();
        }
        return 0;
    }

    /**
     * Searches a string for another string.  If lenient parsing is off,
     * this just calls indexOf().  If lenient parsing is on, this function
     * uses CollationElementIterator to match characters, and only
     * primary-order differences are significant in determining whether
     * there's a match.
     * @param str The string to search
     * @param key The string to search "str" for
     * @param startingAt The index into "str" where the search is to
     * begin
     * @return A two-element array of ints.  Element 0 is the position
     * of the match, or -1 if there was no match.  Element 1 is the
     * number of characters in "str" that matched (which isn't necessarily
     * the same as the length of "key")
     */
    private int[] findText(String str, String key, PluralFormat pluralFormatKey, int startingAt) {
        RbnfLenientScanner scanner = formatter.getLenientScanner();
        if (pluralFormatKey != null) {
            FieldPosition position = new FieldPosition(NumberFormat.INTEGER_FIELD);
            position.setBeginIndex(startingAt);
            pluralFormatKey.parseType(str, scanner, position);
            int start = position.getBeginIndex();
            if (start >= 0) {
                int pluralRuleStart = ruleText.indexOf("$(");
                int pluralRuleSuffix = ruleText.indexOf(")$", pluralRuleStart) + 2;
                int matchLen = position.getEndIndex() - start;
                String prefix = ruleText.substring(0, pluralRuleStart);
                String suffix = ruleText.substring(pluralRuleSuffix);
                if (str.regionMatches(start - prefix.length(), prefix, 0, prefix.length())
                        && str.regionMatches(start + matchLen, suffix, 0, suffix.length()))
                {
                    return new int[]{start - prefix.length(), matchLen + prefix.length() + suffix.length()};
                }
            }
            return new int[]{-1, 0};
        }

        if (scanner != null) {
            // if lenient parsing is turned ON, we've got some work
            // ahead of us
            return scanner.findText(str, key, startingAt);
        }
        // if lenient parsing is turned off, this is easy. Just call
        // String.indexOf() and we're done
        return new int[]{str.indexOf(key, startingAt), key.length()};
    }

    /**
     * Checks to see whether a string consists entirely of ignorable
     * characters.
     * @param str The string to test.
     * @return true if the string is empty of consists entirely of
     * characters that the number formatter's collator says are
     * ignorable at the primary-order level.  false otherwise.
     */
    private boolean allIgnorable(String str) {
        // if the string is empty, we can just return true
        if (str == null || str.length() == 0) {
            return true;
        }
        RbnfLenientScanner scanner = formatter.getLenientScanner();
        return scanner != null && scanner.allIgnorable(str);
    }

    public void setDecimalFormatSymbols(DecimalFormatSymbols newSymbols) {
        if (sub1 != null) {
            sub1.setDecimalFormatSymbols(newSymbols);
        }
        if (sub2 != null) {
            sub2.setDecimalFormatSymbols(newSymbols);
        }
    }
}
