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

import java.util.HashMap;
import java.util.Map;

/**
 * <code>RuleBasedTransliterator</code> is a transliterator
 * that reads a set of rules in order to determine how to perform
 * translations. Rule sets are stored in resource bundles indexed by
 * name. Rules within a rule set are separated by semicolons (';').
 * To include a literal semicolon, prefix it with a backslash ('\').
 * Unicode Pattern_White_Space is ignored.
 * If the first non-blank character on a line is '#',
 * the entire line is ignored as a comment.
 *
 * <p>Each set of rules consists of two groups, one forward, and one
 * reverse. This is a convention that is not enforced; rules for one
 * direction may be omitted, with the result that translations in
 * that direction will not modify the source text. In addition,
 * bidirectional forward-reverse rules may be specified for
 * symmetrical transformations.
 *
 * <p><b>Rule syntax</b>
 *
 * <p>Rule statements take one of the following forms:
 *
 * <dl>
 *     <dt><code>$alefmadda=\u0622;</code></dt>
 *     <dd><strong>Variable definition.</strong> The name on the
 *         left is assigned the text on the right. In this example,
 *         after this statement, instances of the left hand name,
 *         &quot;<code>$alefmadda</code>&quot;, will be replaced by
 *         the Unicode character U+0622. Variable names must begin
 *         with a letter and consist only of letters, digits, and
 *         underscores. Case is significant. Duplicate names cause
 *         an exception to be thrown, that is, variables cannot be
 *         redefined. The right hand side may contain well-formed
 *         text of any length, including no text at all (&quot;<code>$empty=;</code>&quot;).
 *         The right hand side may contain embedded <code>UnicodeSet</code>
 *         patterns, for example, &quot;<code>$softvowel=[eiyEIY]</code>&quot;.</dd>
 *     <dd>&nbsp;</dd>
 *     <dt><code>ai&gt;$alefmadda;</code></dt>
 *     <dd><strong>Forward translation rule.</strong> This rule
 *         states that the string on the left will be changed to the
 *         string on the right when performing forward
 *         transliteration.</dd>
 *     <dt>&nbsp;</dt>
 *     <dt><code>ai&lt;$alefmadda;</code></dt>
 *     <dd><strong>Reverse translation rule.</strong> This rule
 *         states that the string on the right will be changed to
 *         the string on the left when performing reverse
 *         transliteration.</dd>
 * </dl>
 *
 * <dl>
 *     <dt><code>ai&lt;&gt;$alefmadda;</code></dt>
 *     <dd><strong>Bidirectional translation rule.</strong> This
 *         rule states that the string on the right will be changed
 *         to the string on the left when performing forward
 *         transliteration, and vice versa when performing reverse
 *         transliteration.</dd>
 * </dl>
 *
 * <p>Translation rules consist of a <em>match pattern</em> and an <em>output
 * string</em>. The match pattern consists of literal characters,
 * optionally preceded by context, and optionally followed by
 * context. Context characters, like literal pattern characters,
 * must be matched in the text being transliterated. However, unlike
 * literal pattern characters, they are not replaced by the output
 * text. For example, the pattern &quot;<code>abc{def}</code>&quot;
 * indicates the characters &quot;<code>def</code>&quot; must be
 * preceded by &quot;<code>abc</code>&quot; for a successful match.
 * If there is a successful match, &quot;<code>def</code>&quot; will
 * be replaced, but not &quot;<code>abc</code>&quot;. The final '<code>}</code>'
 * is optional, so &quot;<code>abc{def</code>&quot; is equivalent to
 * &quot;<code>abc{def}</code>&quot;. Another example is &quot;<code>{123}456</code>&quot;
 * (or &quot;<code>123}456</code>&quot;) in which the literal
 * pattern &quot;<code>123</code>&quot; must be followed by &quot;<code>456</code>&quot;.
 *
 * <p>The output string of a forward or reverse rule consists of
 * characters to replace the literal pattern characters. If the
 * output string contains the character '<code>|</code>', this is
 * taken to indicate the location of the <em>cursor</em> after
 * replacement. The cursor is the point in the text at which the
 * next replacement, if any, will be applied. The cursor is usually
 * placed within the replacement text; however, it can actually be
 * placed into the precending or following context by using the
 * special character '<code>@</code>'. Examples:
 *
 * <blockquote>
 *     <p><code>a {foo} z &gt; | @ bar; # foo -&gt; bar, move cursor
 *     before a<br>
 *     {foo} xyz &gt; bar @@|; #&nbsp;foo -&gt; bar, cursor between
 *     y and z</code>
 * </blockquote>
 *
 * <p><b>UnicodeSet</b>
 *
 * <p><code>UnicodeSet</code> patterns may appear anywhere that
 * makes sense. They may appear in variable definitions.
 * Contrariwise, <code>UnicodeSet</code> patterns may themselves
 * contain variable references, such as &quot;<code>$a=[a-z];$not_a=[^$a]</code>&quot;,
 * or &quot;<code>$range=a-z;$ll=[$range]</code>&quot;.
 *
 * <p><code>UnicodeSet</code> patterns may also be embedded directly
 * into rule strings. Thus, the following two rules are equivalent:
 *
 * <blockquote>
 *     <p><code>$vowel=[aeiou]; $vowel&gt;'*'; # One way to do this<br>
 *     [aeiou]&gt;'*';
 *     &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;#
 *     Another way</code>
 * </blockquote>
 *
 * <p>See {@link UnicodeSet} for more documentation and examples.
 *
 * <p><b>Segments</b>
 *
 * <p>Segments of the input string can be matched and copied to the
 * output string. This makes certain sets of rules simpler and more
 * general, and makes reordering possible. For example:
 *
 * <blockquote>
 *     <p><code>([a-z]) &gt; $1 $1;
 *     &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;#
 *     double lowercase letters<br>
 *     ([:Lu:]) ([:Ll:]) &gt; $2 $1; # reverse order of Lu-Ll pairs</code>
 * </blockquote>
 *
 * <p>The segment of the input string to be copied is delimited by
 * &quot;<code>(</code>&quot; and &quot;<code>)</code>&quot;. Up to
 * nine segments may be defined. Segments may not overlap. In the
 * output string, &quot;<code>$1</code>&quot; through &quot;<code>$9</code>&quot;
 * represent the input string segments, in left-to-right order of
 * definition.
 *
 * <p><b>Anchors</b>
 *
 * <p>Patterns can be anchored to the beginning or the end of the text. This is done with the
 * special characters '<code>^</code>' and '<code>$</code>'. For example:
 *
 * <blockquote>
 *   <p><code>^ a&nbsp;&nbsp; &gt; 'BEG_A'; &nbsp;&nbsp;# match 'a' at start of text<br>
 *   &nbsp; a&nbsp;&nbsp; &gt; 'A';&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; # match other instances
 *   of 'a'<br>
 *   &nbsp; z $ &gt; 'END_Z'; &nbsp;&nbsp;# match 'z' at end of text<br>
 *   &nbsp; z&nbsp;&nbsp; &gt; 'Z';&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; # match other instances
 *   of 'z'</code>
 * </blockquote>
 *
 * <p>It is also possible to match the beginning or the end of the text using a <code>UnicodeSet</code>.
 * This is done by including a virtual anchor character '<code>$</code>' at the end of the
 * set pattern. Although this is usually the match chafacter for the end anchor, the set will
 * match either the beginning or the end of the text, depending on its placement. For
 * example:
 *
 * <blockquote>
 *   <p><code>$x = [a-z$]; &nbsp;&nbsp;# match 'a' through 'z' OR anchor<br>
 *   $x 1&nbsp;&nbsp;&nbsp; &gt; 2;&nbsp;&nbsp; # match '1' after a-z or at the start<br>
 *   &nbsp;&nbsp; 3 $x &gt; 4; &nbsp;&nbsp;# match '3' before a-z or at the end</code>
 * </blockquote>
 *
 * <p><b>Example</b>
 *
 * <p>The following example rules illustrate many of the features of
 * the rule language.
 *
 * <table border="0" cellpadding="4">
 *     <tr>
 *         <td style="vertical-align: top;">Rule 1.</td>
 *         <td style="vertical-align: top; write-space: nowrap;"><code>abc{def}&gt;x|y</code></td>
 *     </tr>
 *     <tr>
 *         <td style="vertical-align: top;">Rule 2.</td>
 *         <td style="vertical-align: top; write-space: nowrap;"><code>xyz&gt;r</code></td>
 *     </tr>
 *     <tr>
 *         <td style="vertical-align: top;">Rule 3.</td>
 *         <td style="vertical-align: top; write-space: nowrap;"><code>yz&gt;q</code></td>
 *     </tr>
 * </table>
 *
 * <p>Applying these rules to the string &quot;<code>adefabcdefz</code>&quot;
 * yields the following results:
 *
 * <table border="0" cellpadding="4">
 *     <tr>
 *         <td style="vertical-align: top; write-space: nowrap;"><code>|adefabcdefz</code></td>
 *         <td style="vertical-align: top;">Initial state, no rules match. Advance
 *         cursor.</td>
 *     </tr>
 *     <tr>
 *         <td style="vertical-align: top; write-space: nowrap;"><code>a|defabcdefz</code></td>
 *         <td style="vertical-align: top;">Still no match. Rule 1 does not match
 *         because the preceding context is not present.</td>
 *     </tr>
 *     <tr>
 *         <td style="vertical-align: top; write-space: nowrap;"><code>ad|efabcdefz</code></td>
 *         <td style="vertical-align: top;">Still no match. Keep advancing until
 *         there is a match...</td>
 *     </tr>
 *     <tr>
 *         <td style="vertical-align: top; write-space: nowrap;"><code>ade|fabcdefz</code></td>
 *         <td style="vertical-align: top;">...</td>
 *     </tr>
 *     <tr>
 *         <td style="vertical-align: top; write-space: nowrap;"><code>adef|abcdefz</code></td>
 *         <td style="vertical-align: top;">...</td>
 *     </tr>
 *     <tr>
 *         <td style="vertical-align: top; write-space: nowrap;"><code>adefa|bcdefz</code></td>
 *         <td style="vertical-align: top;">...</td>
 *     </tr>
 *     <tr>
 *         <td style="vertical-align: top; write-space: nowrap;"><code>adefab|cdefz</code></td>
 *         <td style="vertical-align: top;">...</td>
 *     </tr>
 *     <tr>
 *         <td style="vertical-align: top; write-space: nowrap;"><code>adefabc|defz</code></td>
 *         <td style="vertical-align: top;">Rule 1 matches; replace &quot;<code>def</code>&quot;
 *         with &quot;<code>xy</code>&quot; and back up the cursor
 *         to before the '<code>y</code>'.</td>
 *     </tr>
 *     <tr>
 *         <td style="vertical-align: top; write-space: nowrap;"><code>adefabcx|yz</code></td>
 *         <td style="vertical-align: top;">Although &quot;<code>xyz</code>&quot; is
 *         present, rule 2 does not match because the cursor is
 *         before the '<code>y</code>', not before the '<code>x</code>'.
 *         Rule 3 does match. Replace &quot;<code>yz</code>&quot;
 *         with &quot;<code>q</code>&quot;.</td>
 *     </tr>
 *     <tr>
 *         <td style="vertical-align: top; write-space: nowrap;"><code>adefabcxq|</code></td>
 *         <td style="vertical-align: top;">The cursor is at the end;
 *         transliteration is complete.</td>
 *     </tr>
 * </table>
 *
 * <p>The order of rules is significant. If multiple rules may match
 * at some point, the first matching rule is applied.
 *
 * <p>Forward and reverse rules may have an empty output string.
 * Otherwise, an empty left or right hand side of any statement is a
 * syntax error.
 *
 * <p>Single quotes are used to quote any character other than a
 * digit or letter. To specify a single quote itself, inside or
 * outside of quotes, use two single quotes in a row. For example,
 * the rule &quot;<code>'&gt;'&gt;o''clock</code>&quot; changes the
 * string &quot;<code>&gt;</code>&quot; to the string &quot;<code>o'clock</code>&quot;.
 *
 * <p><b>Notes</b>
 *
 * <p>While a RuleBasedTransliterator is being built, it checks that
 * the rules are added in proper order. For example, if the rule
 * &quot;a&gt;x&quot; is followed by the rule &quot;ab&gt;y&quot;,
 * then the second rule will throw an exception. The reason is that
 * the second rule can never be triggered, since the first rule
 * always matches anything it matches. In other words, the first
 * rule <em>masks</em> the second rule.
 *
 * <p>Copyright (c) IBM Corporation 1999-2000. All rights reserved.
 *
 * @author Alan Liu
 * @deprecated This API is ICU internal only.
 * @hide Only a subset of ICU is exposed in Android
 * @hide draft / provisional / internal are hidden on Android
 */
@Deprecated
public class RuleBasedTransliterator extends Transliterator {

    private final Data data;

//    /**
//     * Constructs a new transliterator from the given rules.
//     * @param rules rules, separated by ';'
//     * @param direction either FORWARD or REVERSE.
//     * @exception IllegalArgumentException if rules are malformed
//     * or direction is invalid.
//     */
//     public RuleBasedTransliterator(String ID, String rules, int direction,
//                                   UnicodeFilter filter) {
//        super(ID, filter);
//        if (direction != FORWARD && direction != REVERSE) {
//            throw new IllegalArgumentException("Invalid direction");
//        }
//
//        TransliteratorParser parser = new TransliteratorParser();
//        parser.parse(rules, direction);
//        if (parser.idBlockVector.size() != 0 ||
//            parser.compoundFilter != null) {
//            throw new IllegalArgumentException("::ID blocks illegal in RuleBasedTransliterator constructor");
//        }
//
//        data = (Data)parser.dataVector.get(0);
//        setMaximumContextLength(data.ruleSet.getMaximumContextLength());
//     }

//    /**
//     * Constructs a new transliterator from the given rules in the
//     * <code>FORWARD</code> direction.
//     * @param rules rules, separated by ';'
//     * @exception IllegalArgumentException if rules are malformed
//     * or direction is invalid.
//     */
//    public RuleBasedTransliterator(String ID, String rules) {
//        this(ID, rules, FORWARD, null);
//    }

    RuleBasedTransliterator(String ID, Data data, UnicodeFilter filter) {
        super(ID, filter);
        this.data = data;
        setMaximumContextLength(data.ruleSet.getMaximumContextLength());
    }

    /**
     * Implements {@link Transliterator#handleTransliterate}.
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Override
    @Deprecated
    protected void handleTransliterate(Replaceable text,
                                       Position index, boolean incremental) {
        /* We keep start and limit fixed the entire time,
         * relative to the text -- limit may move numerically if text is
         * inserted or removed.  The cursor moves from start to limit, with
         * replacements happening under it.
         *
         * Example: rules 1. ab>x|y
         *                2. yc>z
         *
         * |eabcd   start - no match, advance cursor
         * e|abcd   match rule 1 - change text & adjust cursor
         * ex|ycd   match rule 2 - change text & adjust cursor
         * exz|d    no match, advance cursor
         * exzd|    done
         */

        /* A rule like
         *   a>b|a
         * creates an infinite loop. To prevent that, we put an arbitrary
         * limit on the number of iterations that we take, one that is
         * high enough that any reasonable rules are ok, but low enough to
         * prevent a server from hanging.  The limit is 16 times the
         * number of characters n, unless n is so large that 16n exceeds a
         * uint32_t.
         */
        synchronized(data)  {
            int loopCount = 0;
            int loopLimit = (index.limit - index.start) << 4;
            if (loopLimit < 0) {
                loopLimit = 0x7FFFFFFF;
            }

            while (index.start < index.limit &&
                    loopCount <= loopLimit &&
                    data.ruleSet.transliterate(text, index, incremental)) {
                ++loopCount;
            }
        }
    }


    static class Data {
        public Data() {
            variableNames = new HashMap<String, char[]>();
            ruleSet = new TransliterationRuleSet();
        }

        /**
         * Rule table.  May be empty.
         */
        public TransliterationRuleSet ruleSet;

        /**
         * Map variable name (String) to variable (char[]).  A variable name
         * corresponds to zero or more characters, stored in a char[] array in
         * this hash.  One or more of these chars may also correspond to a
         * UnicodeSet, in which case the character in the char[] in this hash is
         * a stand-in: it is an index for a secondary lookup in
         * data.variables.  The stand-in also represents the UnicodeSet in
         * the stored rules.
         */
        Map<String, char[]> variableNames;

        /**
         * Map category variable (Character) to UnicodeMatcher or UnicodeReplacer.
         * Variables that correspond to a set of characters are mapped
         * from variable name to a stand-in character in data.variableNames.
         * The stand-in then serves as a key in this hash to lookup the
         * actual UnicodeSet object.  In addition, the stand-in is
         * stored in the rule text to represent the set of characters.
         * variables[i] represents character (variablesBase + i).
         */
        Object[] variables;

        /**
         * The character that represents variables[0].  Characters
         * variablesBase through variablesBase +
         * variables.length - 1 represent UnicodeSet objects.
         */
        char variablesBase;

        /**
         * Return the UnicodeMatcher represented by the given character, or
         * null if none.
         */
        public UnicodeMatcher lookupMatcher(int standIn) {
            int i = standIn - variablesBase;
            return (i >= 0 && i < variables.length)
                ? (UnicodeMatcher) variables[i] : null;
        }

        /**
         * Return the UnicodeReplacer represented by the given character, or
         * null if none.
         */
        public UnicodeReplacer lookupReplacer(int standIn) {
            int i = standIn - variablesBase;
            return (i >= 0 && i < variables.length)
                ? (UnicodeReplacer) variables[i] : null;
        }
    }


    /**
     * Return a representation of this transliterator as source rules.
     * These rules will produce an equivalent transliterator if used
     * to construct a new transliterator.
     * @param escapeUnprintable if TRUE then convert unprintable
     * character to their hex escape representations, \\uxxxx or
     * \\Uxxxxxxxx.  Unprintable characters are those other than
     * U+000A, U+0020..U+007E.
     * @return rules string
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Override
    @Deprecated
    public String toRules(boolean escapeUnprintable) {
        return data.ruleSet.toRules(escapeUnprintable);
    }

//    /**
//     * Return the set of all characters that may be modified by this
//     * Transliterator, ignoring the effect of our filter.
//     */
//    protected UnicodeSet handleGetSourceSet() {
//        return data.ruleSet.getSourceTargetSet(false, unicodeFilter);
//    }
//
//    /**
//     * Returns the set of all characters that may be generated as
//     * replacement text by this transliterator.
//     */
//    public UnicodeSet getTargetSet() {
//        return data.ruleSet.getSourceTargetSet(true, unicodeFilter);
//    }

    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    @Override
    public void addSourceTargetSet(UnicodeSet filter, UnicodeSet sourceSet, UnicodeSet targetSet) {
        data.ruleSet.addSourceTargetSet(filter, sourceSet, targetSet);
    }

    /**
     * Temporary hack for registry problem. Needs to be replaced by better architecture.
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public Transliterator safeClone() {
        UnicodeFilter filter = getFilter();
        if (filter != null && filter instanceof UnicodeSet) {
            filter = new UnicodeSet((UnicodeSet)filter);
        }
        return new RuleBasedTransliterator(getID(), data, filter);
    }
}


