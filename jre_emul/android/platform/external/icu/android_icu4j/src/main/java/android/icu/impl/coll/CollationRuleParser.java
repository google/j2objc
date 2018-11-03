/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
* Copyright (C) 2013-2015, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* CollationRuleParser.java, ported from collationruleparser.h/.cpp
*
* C++ version created on: 2013apr10
* created by: Markus W. Scherer
*/

package android.icu.impl.coll;

import java.text.ParseException;
import java.util.ArrayList;

import android.icu.impl.IllegalIcuArgumentException;
import android.icu.impl.PatternProps;
import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;
import android.icu.text.Collator;
import android.icu.text.Normalizer2;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import android.icu.util.ULocale;

/**
 * @hide Only a subset of ICU is exposed in Android
 */
public final class CollationRuleParser {
    /** Special reset positions. */
    enum Position {
        FIRST_TERTIARY_IGNORABLE,
        LAST_TERTIARY_IGNORABLE,
        FIRST_SECONDARY_IGNORABLE,
        LAST_SECONDARY_IGNORABLE,
        FIRST_PRIMARY_IGNORABLE,
        LAST_PRIMARY_IGNORABLE,
        FIRST_VARIABLE,
        LAST_VARIABLE,
        FIRST_REGULAR,
        LAST_REGULAR,
        FIRST_IMPLICIT,
        LAST_IMPLICIT,
        FIRST_TRAILING,
        LAST_TRAILING
    }
    static final Position[] POSITION_VALUES = Position.values();

    /**
     * First character of contractions that encode special reset positions.
     * U+FFFE cannot be tailored via rule syntax.
     *
     * The second contraction character is POS_BASE + Position.
     */
    static final char POS_LEAD = 0xfffe;
    /**
     * Base for the second character of contractions that encode special reset positions.
     * Braille characters U+28xx are printable and normalization-inert.
     * @see POS_LEAD
     */
    static final char POS_BASE = 0x2800;

    static abstract class Sink {
        /**
         * Adds a reset.
         * strength=UCOL_IDENTICAL for &str.
         * strength=UCOL_PRIMARY/UCOL_SECONDARY/UCOL_TERTIARY for &[before n]str where n=1/2/3.
         */
        abstract void addReset(int strength, CharSequence str);
        /**
         * Adds a relation with strength and prefix | str / extension.
         */
        abstract void addRelation(int strength, CharSequence prefix,
                CharSequence str, CharSequence extension);

        void suppressContractions(UnicodeSet set) {}

        void optimize(UnicodeSet set) {}
    }

    interface Importer {
        String getRules(String localeID, String collationType);
    }

    /**
     * Constructor.
     * The Sink must be set before parsing.
     * The Importer can be set, otherwise [import locale] syntax is not supported.
     */
    CollationRuleParser(CollationData base) {
        baseData = base;
    }

    /**
     * Sets the pointer to a Sink object.
     * The pointer is aliased: Pointer copy without cloning or taking ownership.
     */
    void setSink(Sink sinkAlias) {
        sink = sinkAlias;
    }

    /**
     * Sets the pointer to an Importer object.
     * The pointer is aliased: Pointer copy without cloning or taking ownership.
     */
    void setImporter(Importer importerAlias) {
        importer = importerAlias;
    }

    void parse(String ruleString, CollationSettings outSettings) throws ParseException {
        settings = outSettings;
        parse(ruleString);
    }

    private static final int UCOL_DEFAULT = -1;
    private static final int UCOL_OFF = 0;
    private static final int UCOL_ON = 1;

    /** UCOL_PRIMARY=0 .. UCOL_IDENTICAL=15 */
    private static final int STRENGTH_MASK = 0xf;
    private static final int STARRED_FLAG = 0x10;
    private static final int OFFSET_SHIFT = 8;

    private static final String BEFORE = "[before";

    // In C++, we parse into temporary UnicodeString objects named "raw" or "str".
    // In Java, we reuse this StringBuilder.
    private final StringBuilder rawBuilder = new StringBuilder();

    private void parse(String ruleString) throws ParseException {
        rules = ruleString;
        ruleIndex = 0;

        while(ruleIndex < rules.length()) {
            char c = rules.charAt(ruleIndex);
            if(PatternProps.isWhiteSpace(c)) {
                ++ruleIndex;
                continue;
            }
            switch(c) {
            case 0x26:  // '&'
                parseRuleChain();
                break;
            case 0x5b:  // '['
                parseSetting();
                break;
            case 0x23:  // '#' starts a comment, until the end of the line
                ruleIndex = skipComment(ruleIndex + 1);
                break;
            case 0x40:  // '@' is equivalent to [backwards 2]
                settings.setFlag(CollationSettings.BACKWARD_SECONDARY, true);
                ++ruleIndex;
                break;
            case 0x21:  // '!' used to turn on Thai/Lao character reversal
                // Accept but ignore. The root collator has contractions
                // that are equivalent to the character reversal, where appropriate.
                ++ruleIndex;
                break;
            default:
                setParseError("expected a reset or setting or comment");
                break;
            }
        }
    }

    private void parseRuleChain() throws ParseException {
        int resetStrength = parseResetAndPosition();
        boolean isFirstRelation = true;
        for(;;) {
            int result = parseRelationOperator();
            if(result < 0) {
                if(ruleIndex < rules.length() && rules.charAt(ruleIndex) == 0x23) {
                    // '#' starts a comment, until the end of the line
                    ruleIndex = skipComment(ruleIndex + 1);
                    continue;
                }
                if(isFirstRelation) {
                    setParseError("reset not followed by a relation");
                }
                return;
            }
            int strength = result & STRENGTH_MASK;
            if(resetStrength < Collator.IDENTICAL) {
                // reset-before rule chain
                if(isFirstRelation) {
                    if(strength != resetStrength) {
                        setParseError("reset-before strength differs from its first relation");
                        return;
                    }
                } else {
                    if(strength < resetStrength) {
                        setParseError("reset-before strength followed by a stronger relation");
                        return;
                    }
                }
            }
            int i = ruleIndex + (result >> OFFSET_SHIFT);  // skip over the relation operator
            if((result & STARRED_FLAG) == 0) {
                parseRelationStrings(strength, i);
            } else {
                parseStarredCharacters(strength, i);
            }
            isFirstRelation = false;
        }
    }

    private int parseResetAndPosition() throws ParseException {
        int i = skipWhiteSpace(ruleIndex + 1);
        int j;
        char c;
        int resetStrength;
        if(rules.regionMatches(i, BEFORE, 0, BEFORE.length()) &&
                (j = i + BEFORE.length()) < rules.length() &&
                PatternProps.isWhiteSpace(rules.charAt(j)) &&
                ((j = skipWhiteSpace(j + 1)) + 1) < rules.length() &&
                0x31 <= (c = rules.charAt(j)) && c <= 0x33 &&
                rules.charAt(j + 1) == 0x5d) {
            // &[before n] with n=1 or 2 or 3
            resetStrength = Collator.PRIMARY + (c - 0x31);
            i = skipWhiteSpace(j + 2);
        } else {
            resetStrength = Collator.IDENTICAL;
        }
        if(i >= rules.length()) {
            setParseError("reset without position");
            return UCOL_DEFAULT;
        }
        if(rules.charAt(i) == 0x5b) {  // '['
            i = parseSpecialPosition(i, rawBuilder);
        } else {
            i = parseTailoringString(i, rawBuilder);
        }
        try {
            sink.addReset(resetStrength, rawBuilder);
        } catch(Exception e) {
            setParseError("adding reset failed", e);
            return UCOL_DEFAULT;
        }
        ruleIndex = i;
        return resetStrength;
    }

    private int parseRelationOperator() {
        ruleIndex = skipWhiteSpace(ruleIndex);
        if(ruleIndex >= rules.length()) { return UCOL_DEFAULT; }
        int strength;
        int i = ruleIndex;
        char c = rules.charAt(i++);
        switch(c) {
        case 0x3c:  // '<'
            if(i < rules.length() && rules.charAt(i) == 0x3c) {  // <<
                ++i;
                if(i < rules.length() && rules.charAt(i) == 0x3c) {  // <<<
                    ++i;
                    if(i < rules.length() && rules.charAt(i) == 0x3c) {  // <<<<
                        ++i;
                        strength = Collator.QUATERNARY;
                    } else {
                        strength = Collator.TERTIARY;
                    }
                } else {
                    strength = Collator.SECONDARY;
                }
            } else {
                strength = Collator.PRIMARY;
            }
            if(i < rules.length() && rules.charAt(i) == 0x2a) {  // '*'
                ++i;
                strength |= STARRED_FLAG;
            }
            break;
        case 0x3b:  // ';' same as <<
            strength = Collator.SECONDARY;
            break;
        case 0x2c:  // ',' same as <<<
            strength = Collator.TERTIARY;
            break;
        case 0x3d:  // '='
            strength = Collator.IDENTICAL;
            if(i < rules.length() && rules.charAt(i) == 0x2a) {  // '*'
                ++i;
                strength |= STARRED_FLAG;
            }
            break;
        default:
            return UCOL_DEFAULT;
        }
        return ((i - ruleIndex) << OFFSET_SHIFT) | strength;
    }

    private void parseRelationStrings(int strength, int i) throws ParseException {
        // Parse
        //     prefix | str / extension
        // where prefix and extension are optional.
        String prefix = "";
        CharSequence extension = "";
        i = parseTailoringString(i, rawBuilder);
        char next = (i < rules.length()) ? rules.charAt(i) : 0;
        if(next == 0x7c) {  // '|' separates the context prefix from the string.
            prefix = rawBuilder.toString();
            i = parseTailoringString(i + 1, rawBuilder);
            next = (i < rules.length()) ? rules.charAt(i) : 0;
        }
        // str = rawBuilder (do not modify rawBuilder any more in this function)
        if(next == 0x2f) {  // '/' separates the string from the extension.
            StringBuilder extBuilder = new StringBuilder();
            i = parseTailoringString(i + 1, extBuilder);
            extension = extBuilder;
        }
        if(prefix.length() != 0) {
            int prefix0 = prefix.codePointAt(0);
            int c = rawBuilder.codePointAt(0);
            if(!nfc.hasBoundaryBefore(prefix0) || !nfc.hasBoundaryBefore(c)) {
                setParseError("in 'prefix|str', prefix and str must each start with an NFC boundary");
                return;
            }
        }
        try {
            sink.addRelation(strength, prefix, rawBuilder, extension);
        } catch(Exception e) {
            setParseError("adding relation failed", e);
            return;
        }
        ruleIndex = i;
    }

    private void parseStarredCharacters(int strength, int i) throws ParseException {
        String empty = "";
        i = parseString(skipWhiteSpace(i), rawBuilder);
        if(rawBuilder.length() == 0) {
            setParseError("missing starred-relation string");
            return;
        }
        int prev = -1;
        int j = 0;
        for(;;) {
            while(j < rawBuilder.length()) {
                int c = rawBuilder.codePointAt(j);
                if(!nfd.isInert(c)) {
                    setParseError("starred-relation string is not all NFD-inert");
                    return;
                }
                try {
                    sink.addRelation(strength, empty, UTF16.valueOf(c), empty);
                } catch(Exception e) {
                    setParseError("adding relation failed", e);
                    return;
                }
                j += Character.charCount(c);
                prev = c;
            }
            if(i >= rules.length() || rules.charAt(i) != 0x2d) {  // '-'
                break;
            }
            if(prev < 0) {
                setParseError("range without start in starred-relation string");
                return;
            }
            i = parseString(i + 1, rawBuilder);
            if(rawBuilder.length() == 0) {
                setParseError("range without end in starred-relation string");
                return;
            }
            int c = rawBuilder.codePointAt(0);
            if(c < prev) {
                setParseError("range start greater than end in starred-relation string");
                return;
            }
            // range prev-c
            while(++prev <= c) {
                if(!nfd.isInert(prev)) {
                    setParseError("starred-relation string range is not all NFD-inert");
                    return;
                }
                if(isSurrogate(prev)) {
                    setParseError("starred-relation string range contains a surrogate");
                    return;
                }
                if(0xfffd <= prev && prev <= 0xffff) {
                    setParseError("starred-relation string range contains U+FFFD, U+FFFE or U+FFFF");
                    return;
                }
                try {
                    sink.addRelation(strength, empty, UTF16.valueOf(prev), empty);
                } catch(Exception e) {
                    setParseError("adding relation failed", e);
                    return;
                }
            }
            prev = -1;
            j = Character.charCount(c);
        }
        ruleIndex = skipWhiteSpace(i);
    }

    private int parseTailoringString(int i, StringBuilder raw) throws ParseException {
        i = parseString(skipWhiteSpace(i), raw);
        if(raw.length() == 0) {
            setParseError("missing relation string");
        }
        return skipWhiteSpace(i);
    }

    private int parseString(int i, StringBuilder raw) throws ParseException {
        raw.setLength(0);
        while(i < rules.length()) {
            char c = rules.charAt(i++);
            if(isSyntaxChar(c)) {
                if(c == 0x27) {  // apostrophe
                    if(i < rules.length() && rules.charAt(i) == 0x27) {
                        // Double apostrophe, encodes a single one.
                        raw.append((char)0x27);
                        ++i;
                        continue;
                    }
                    // Quote literal text until the next single apostrophe.
                    for(;;) {
                        if(i == rules.length()) {
                            setParseError("quoted literal text missing terminating apostrophe");
                            return i;
                        }
                        c = rules.charAt(i++);
                        if(c == 0x27) {
                            if(i < rules.length() && rules.charAt(i) == 0x27) {
                                // Double apostrophe inside quoted literal text,
                                // still encodes a single apostrophe.
                                ++i;
                            } else {
                                break;
                            }
                        }
                        raw.append(c);
                    }
                } else if(c == 0x5c) {  // backslash
                    if(i == rules.length()) {
                        setParseError("backslash escape at the end of the rule string");
                        return i;
                    }
                    int cp = rules.codePointAt(i);
                    raw.appendCodePoint(cp);
                    i += Character.charCount(cp);
                } else {
                    // Any other syntax character terminates a string.
                    --i;
                    break;
                }
            } else if(PatternProps.isWhiteSpace(c)) {
                // Unquoted white space terminates a string.
                --i;
                break;
            } else {
                raw.append(c);
            }
        }
        for(int j = 0; j < raw.length();) {
            int c = raw.codePointAt(j);
            if(isSurrogate(c)) {
                setParseError("string contains an unpaired surrogate");
                return i;
            }
            if(0xfffd <= c && c <= 0xffff) {
                setParseError("string contains U+FFFD, U+FFFE or U+FFFF");
                return i;
            }
            j += Character.charCount(c);
        }
        return i;
    }

    // TODO: Widen UTF16.isSurrogate(char16) to take an int.
    private static final boolean isSurrogate(int c) {
        return (c & 0xfffff800) == 0xd800;
    }

    private static final String[] positions = {
        "first tertiary ignorable",
        "last tertiary ignorable",
        "first secondary ignorable",
        "last secondary ignorable",
        "first primary ignorable",
        "last primary ignorable",
        "first variable",
        "last variable",
        "first regular",
        "last regular",
        "first implicit",
        "last implicit",
        "first trailing",
        "last trailing"
    };

    /**
     * Sets str to a contraction of U+FFFE and (U+2800 + Position).
     * @return rule index after the special reset position
     * @throws ParseException 
     */
    private int parseSpecialPosition(int i, StringBuilder str) throws ParseException {
        int j = readWords(i + 1, rawBuilder);
        if(j > i && rules.charAt(j) == 0x5d && rawBuilder.length() != 0) {  // words end with ]
            ++j;
            String raw = rawBuilder.toString();
            str.setLength(0);
            for(int pos = 0; pos < positions.length; ++pos) {
                if(raw.equals(positions[pos])) {
                    str.append(POS_LEAD).append((char)(POS_BASE + pos));
                    return j;
                }
            }
            if(raw.equals("top")) {
                str.append(POS_LEAD).append((char)(POS_BASE + Position.LAST_REGULAR.ordinal()));
                return j;
            }
            if(raw.equals("variable top")) {
                str.append(POS_LEAD).append((char)(POS_BASE + Position.LAST_VARIABLE.ordinal()));
                return j;
            }
        }
        setParseError("not a valid special reset position");
        return i;
    }

    private void parseSetting() throws ParseException {
        int i = ruleIndex + 1;
        int j = readWords(i, rawBuilder);
        if(j <= i || rawBuilder.length() == 0) {
            setParseError("expected a setting/option at '['");
        }
        // startsWith() etc. are available for String but not CharSequence/StringBuilder.
        String raw = rawBuilder.toString();
        if(rules.charAt(j) == 0x5d) {  // words end with ]
            ++j;
            if(raw.startsWith("reorder") &&
                    (raw.length() == 7 || raw.charAt(7) == 0x20)) {
                parseReordering(raw);
                ruleIndex = j;
                return;
            }
            if(raw.equals("backwards 2")) {
                settings.setFlag(CollationSettings.BACKWARD_SECONDARY, true);
                ruleIndex = j;
                return;
            }
            String v;
            int valueIndex = raw.lastIndexOf(0x20);
            if(valueIndex >= 0) {
                v = raw.substring(valueIndex + 1);
                raw = raw.substring(0, valueIndex);
            } else {
                v = "";
            }
            if(raw.equals("strength") && v.length() == 1) {
                int value = UCOL_DEFAULT;
                char c = v.charAt(0);
                if(0x31 <= c && c <= 0x34) {  // 1..4
                    value = Collator.PRIMARY + (c - 0x31);
                } else if(c == 0x49) {  // 'I'
                    value = Collator.IDENTICAL;
                }
                if(value != UCOL_DEFAULT) {
                    settings.setStrength(value);
                    ruleIndex = j;
                    return;
                }
            } else if(raw.equals("alternate")) {
                int value = UCOL_DEFAULT;
                if(v.equals("non-ignorable")) {
                    value = 0;  // UCOL_NON_IGNORABLE
                } else if(v.equals("shifted")) {
                    value = 1;  // UCOL_SHIFTED
                }
                if(value != UCOL_DEFAULT) {
                    settings.setAlternateHandlingShifted(value > 0);
                    ruleIndex = j;
                    return;
                }
            } else if(raw.equals("maxVariable")) {
                int value = UCOL_DEFAULT;
                if(v.equals("space")) {
                    value = CollationSettings.MAX_VAR_SPACE;
                } else if(v.equals("punct")) {
                    value = CollationSettings.MAX_VAR_PUNCT;
                } else if(v.equals("symbol")) {
                    value = CollationSettings.MAX_VAR_SYMBOL;
                } else if(v.equals("currency")) {
                    value = CollationSettings.MAX_VAR_CURRENCY;
                }
                if(value != UCOL_DEFAULT) {
                    settings.setMaxVariable(value, 0);
                    settings.variableTop = baseData.getLastPrimaryForGroup(
                        Collator.ReorderCodes.FIRST + value);
                    assert(settings.variableTop != 0);
                    ruleIndex = j;
                    return;
                }
            } else if(raw.equals("caseFirst")) {
                int value = UCOL_DEFAULT;
                if(v.equals("off")) {
                    value = UCOL_OFF;
                } else if(v.equals("lower")) {
                    value = CollationSettings.CASE_FIRST;  // UCOL_LOWER_FIRST
                } else if(v.equals("upper")) {
                    value = CollationSettings.CASE_FIRST_AND_UPPER_MASK;  // UCOL_UPPER_FIRST
                }
                if(value != UCOL_DEFAULT) {
                    settings.setCaseFirst(value);
                    ruleIndex = j;
                    return;
                }
            } else if(raw.equals("caseLevel")) {
                int value = getOnOffValue(v);
                if(value != UCOL_DEFAULT) {
                    settings.setFlag(CollationSettings.CASE_LEVEL, value > 0);
                    ruleIndex = j;
                    return;
                }
            } else if(raw.equals("normalization")) {
                int value = getOnOffValue(v);
                if(value != UCOL_DEFAULT) {
                    settings.setFlag(CollationSettings.CHECK_FCD, value > 0);
                    ruleIndex = j;
                    return;
                }
            } else if(raw.equals("numericOrdering")) {
                int value = getOnOffValue(v);
                if(value != UCOL_DEFAULT) {
                    settings.setFlag(CollationSettings.NUMERIC, value > 0);
                    ruleIndex = j;
                    return;
                }
            } else if(raw.equals("hiraganaQ")) {
                int value = getOnOffValue(v);
                if(value != UCOL_DEFAULT) {
                    if(value == UCOL_ON) {
                        setParseError("[hiraganaQ on] is not supported");
                    }
                    ruleIndex = j;
                    return;
                }
            } else if(raw.equals("import")) {
                // BCP 47 language tag -> ICU locale ID
                ULocale localeID;
                try {
                    localeID = new ULocale.Builder().setLanguageTag(v).build();
                } catch(Exception e) {
                    setParseError("expected language tag in [import langTag]", e);
                    return;
                }
                // localeID minus all keywords
                String baseID = localeID.getBaseName();
                // @collation=type, or length=0 if not specified
                String collationType = localeID.getKeywordValue("collation");
                if(importer == null) {
                    setParseError("[import langTag] is not supported");
                } else {
                    String importedRules;
                    try {
                        importedRules =
                            importer.getRules(baseID,
                                    collationType != null ? collationType : "standard");
                    } catch(Exception e) {
                        setParseError("[import langTag] failed", e);
                        return;
                    }
                    String outerRules = rules;
                    int outerRuleIndex = ruleIndex;
                    try {
                        parse(importedRules);
                    } catch(Exception e) {
                        ruleIndex = outerRuleIndex;  // Restore the original index for error reporting.
                        setParseError("parsing imported rules failed", e);
                    }
                    rules = outerRules;
                    ruleIndex = j;
                }
                return;
            }
        } else if(rules.charAt(j) == 0x5b) {  // words end with [
            UnicodeSet set = new UnicodeSet();
            j = parseUnicodeSet(j, set);
            if(raw.equals("optimize")) {
                try {
                    sink.optimize(set);
                } catch(Exception e) {
                    setParseError("[optimize set] failed", e);
                }
                ruleIndex = j;
                return;
            } else if(raw.equals("suppressContractions")) {
                try {
                    sink.suppressContractions(set);
                } catch(Exception e) {
                    setParseError("[suppressContractions set] failed", e);
                }
                ruleIndex = j;
                return;
            }
        }
        setParseError("not a valid setting/option");
    }

    private void parseReordering(CharSequence raw) throws ParseException {
        int i = 7;  // after "reorder"
        if(i == raw.length()) {
            // empty [reorder] with no codes
            settings.resetReordering();
            return;
        }
        // Parse the codes in [reorder aa bb cc].
        ArrayList<Integer> reorderCodes = new ArrayList<Integer>();
        while(i < raw.length()) {
            ++i;  // skip the word-separating space
            int limit = i;
            while(limit < raw.length() && raw.charAt(limit) != ' ') { ++limit; }
            String word = raw.subSequence(i, limit).toString();
            int code = getReorderCode(word);
            if(code < 0) {
                setParseError("unknown script or reorder code");
                return;
            }
            reorderCodes.add(code);
            i = limit;
        }
        if(reorderCodes.isEmpty()) {
            settings.resetReordering();
        } else {
            int[] codes = new int[reorderCodes.size()];
            int j = 0;
            for(Integer code : reorderCodes) { codes[j++] = code; }
            settings.setReordering(baseData, codes);
        }
    }

    private static final String[] gSpecialReorderCodes = {
        "space", "punct", "symbol", "currency", "digit"
    };

    /**
     * Gets a script or reorder code from its string representation.
     * @return the script/reorder code, or
     * -1 if not recognized
     */
    public static int getReorderCode(String word) {
        for(int i = 0; i < gSpecialReorderCodes.length; ++i) {
            if(word.equalsIgnoreCase(gSpecialReorderCodes[i])) {
                return Collator.ReorderCodes.FIRST + i;
            }
        }
        try {
            int script = UCharacter.getPropertyValueEnum(UProperty.SCRIPT, word);
            if(script >= 0) {
                return script;
            }
        } catch (IllegalIcuArgumentException e) {
            // fall through
        }
        if(word.equalsIgnoreCase("others")) {
            return Collator.ReorderCodes.OTHERS;  // same as Zzzz = USCRIPT_UNKNOWN 
        }
        return -1;
    }

    private static int getOnOffValue(String s) {
        if(s.equals("on")) {
            return UCOL_ON;
        } else if(s.equals("off")) {
            return UCOL_OFF;
        } else {
            return UCOL_DEFAULT;
        }
    }

    private int parseUnicodeSet(int i, UnicodeSet set) throws ParseException {
        // Collect a UnicodeSet pattern between a balanced pair of [brackets].
        int level = 0;
        int j = i;
        for(;;) {
            if(j == rules.length()) {
                setParseError("unbalanced UnicodeSet pattern brackets");
                return j;
            }
            char c = rules.charAt(j++);
            if(c == 0x5b) {  // '['
                ++level;
            } else if(c == 0x5d) {  // ']'
                if(--level == 0) { break; }
            }
        }
        try {
            set.applyPattern(rules.substring(i, j));
        } catch(Exception e) {
            setParseError("not a valid UnicodeSet pattern: " + e.getMessage());
        }
        j = skipWhiteSpace(j);
        if(j == rules.length() || rules.charAt(j) != 0x5d) {
            setParseError("missing option-terminating ']' after UnicodeSet pattern");
            return j;
        }
        return ++j;
    }

    private int readWords(int i, StringBuilder raw) {
        raw.setLength(0);
        i = skipWhiteSpace(i);
        for(;;) {
            if(i >= rules.length()) { return 0; }
            char c = rules.charAt(i);
            if(isSyntaxChar(c) && c != 0x2d && c != 0x5f) {  // syntax except -_
                if(raw.length() == 0) { return i; }
                int lastIndex = raw.length() - 1;
                if(raw.charAt(lastIndex) == ' ') {  // remove trailing space
                    raw.setLength(lastIndex);
                }
                return i;
            }
            if(PatternProps.isWhiteSpace(c)) {
                raw.append(' ');
                i = skipWhiteSpace(i + 1);
            } else {
                raw.append(c);
                ++i;
            }
        }
    }

    private int skipComment(int i) {
        // skip to past the newline
        while(i < rules.length()) {
            char c = rules.charAt(i++);
            // LF or FF or CR or NEL or LS or PS
            if(c == 0xa || c == 0xc || c == 0xd || c == 0x85 || c == 0x2028 || c == 0x2029) {
                // Unicode Newline Guidelines: "A readline function should stop at NLF, LS, FF, or PS."
                // NLF (new line function) = CR or LF or CR+LF or NEL.
                // No need to collect all of CR+LF because a following LF will be ignored anyway.
                break;
            }
        }
        return i;
    }

    private void setParseError(String reason) throws ParseException {
        throw makeParseException(reason);
    }

    private void setParseError(String reason, Exception e) throws ParseException {
        ParseException newExc = makeParseException(reason + ": " + e.getMessage());
        newExc.initCause(e);
        throw newExc;
    }

    private ParseException makeParseException(String reason) {
        return new ParseException(appendErrorContext(reason), ruleIndex);
    }

    private static final int U_PARSE_CONTEXT_LEN = 16;

    // C++ setErrorContext()
    private String appendErrorContext(String reason) {
        // Note: This relies on the calling code maintaining the ruleIndex
        // at a position that is useful for debugging.
        // For example, at the beginning of a reset or relation etc.
        StringBuilder msg = new StringBuilder(reason);
        msg.append(" at index ").append(ruleIndex);
        // We are not counting line numbers.

        msg.append(" near \"");
        // before ruleIndex
        int start = ruleIndex - (U_PARSE_CONTEXT_LEN - 1);
        if(start < 0) {
            start = 0;
        } else if(start > 0 && Character.isLowSurrogate(rules.charAt(start))) {
            ++start;
        }
        msg.append(rules, start, ruleIndex);

        msg.append('!');
        // starting from ruleIndex
        int length = rules.length() - ruleIndex;
        if(length >= U_PARSE_CONTEXT_LEN) {
            length = U_PARSE_CONTEXT_LEN - 1;
            if(Character.isHighSurrogate(rules.charAt(ruleIndex + length - 1))) {
                --length;
            }
        }
        msg.append(rules, ruleIndex, ruleIndex + length);
        return msg.append('\"').toString();
    }

    /**
     * ASCII [:P:] and [:S:]:
     * [\u0021-\u002F \u003A-\u0040 \u005B-\u0060 \u007B-\u007E]
     */
    private static boolean isSyntaxChar(int c) {
        return 0x21 <= c && c <= 0x7e &&
                (c <= 0x2f || (0x3a <= c && c <= 0x40) ||
                (0x5b <= c && c <= 0x60) || (0x7b <= c));
    }

    private int skipWhiteSpace(int i) {
        while(i < rules.length() && PatternProps.isWhiteSpace(rules.charAt(i))) {
            ++i;
        }
        return i;
    }

    private Normalizer2 nfd = Normalizer2.getNFDInstance();
    private Normalizer2 nfc = Normalizer2.getNFCInstance();

    private String rules;
    private final CollationData baseData;
    private CollationSettings settings;

    private Sink sink;
    private Importer importer;

    private int ruleIndex;
}
