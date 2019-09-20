/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2002-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */

/**
 * Port From:   ICU4C v2.1 : cintltest
 * Source File: $ICU4CRoot/source/test/cintltest/cmsccoll.c
 */

package android.icu.dev.test.collator;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Ignore;
import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.Utility;
import android.icu.lang.UScript;
import android.icu.text.CollationElementIterator;
import android.icu.text.CollationKey;
import android.icu.text.CollationKey.BoundMode;
import android.icu.text.Collator;
import android.icu.text.Collator.ReorderCodes;
import android.icu.text.Normalizer;
import android.icu.text.RawCollationKey;
import android.icu.text.RuleBasedCollator;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import android.icu.text.UnicodeSetIterator;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;

public class CollationMiscTest extends TestFmwk {
    //private static final int NORM_BUFFER_TEST_LEN_ = 32;
    private static final class Tester
    {
        int u;
        String NFC;
        String NFD;
    }

    private static final boolean hasCollationElements(Locale locale)
    {
        ICUResourceBundle rb = (ICUResourceBundle)UResourceBundle.getBundleInstance(ICUData.ICU_COLLATION_BASE_NAME,locale);
        if (rb != null) {
            try {
                String collkey = rb.getStringWithFallback("collations/default");
                ICUResourceBundle elements = rb.getWithFallback("collations/" + collkey);
                if (elements != null) {
                    return true;
                }
            } catch (Exception e) {
            }
        }
        return false;
    }

    @Test
    public void TestComposeDecompose()
    {
        Tester t[] = new Tester[0x30000];
        t[0] = new Tester();
        logln("Testing UCA extensively\n");
        RuleBasedCollator coll;
        try {
            coll = (RuleBasedCollator)Collator.getInstance(Locale.ENGLISH);
        }
        catch (Exception e) {
            warnln("Error opening collator\n");
            return;
        }

        int noCases = 0;
        for (int u = 0; u < 0x30000; u ++) {
            String comp = UTF16.valueOf(u);
            int len = comp.length();
            t[noCases].NFC = Normalizer.normalize(u, Normalizer.NFC);
            t[noCases].NFD = Normalizer.normalize(u, Normalizer.NFD);

            if (t[noCases].NFC.length() != t[noCases].NFD.length()
                || (t[noCases].NFC.compareTo(t[noCases].NFD) != 0)
                || (len != t[noCases].NFD.length())
                || (comp.compareTo(t[noCases].NFD) != 0)) {
                t[noCases].u = u;
                if (len != t[noCases].NFD.length()
                    || (comp.compareTo(t[noCases].NFD) != 0)) {
                    t[noCases].NFC = comp;
                }
                noCases ++;
                t[noCases] = new Tester();
            }
        }

        for (int u = 0; u < noCases; u ++) {
            if (!coll.equals(t[u].NFC, t[u].NFD)) {
                errln("Failure: codePoint \\u" + Integer.toHexString(t[u].u)
                      + " fails TestComposeDecompose in the UCA");
                CollationTest.doTest(this, coll, t[u].NFC, t[u].NFD, 0);
            }
        }

        logln("Testing locales, number of cases = " + noCases);
        Locale loc[] = Collator.getAvailableLocales();
        for (int i = 0; i < loc.length; i ++) {
            if (hasCollationElements(loc[i])) {
                logln("Testing locale " + loc[i].getDisplayName());
                coll = (RuleBasedCollator)Collator.getInstance(loc[i]);
                coll.setStrength(Collator.IDENTICAL);

                for (int u = 0; u < noCases; u ++) {
                    if (!coll.equals(t[u].NFC, t[u].NFD)) {
                        errln("Failure: codePoint \\u"
                              + Integer.toHexString(t[u].u)
                              + " fails TestComposeDecompose for locale "
                              + loc[i].getDisplayName());
                        // this tests for the iterators too
                        CollationTest.doTest(this, coll, t[u].NFC, t[u].NFD,
                                             0);
                    }
                }
            }
        }
    }

    @Test
    public void TestRuleOptions() {
        // values here are hardcoded and are correct for the current UCA when
        // the UCA changes, one might be forced to change these values.

        /*
         * These strings contain the last character before [variable top]
         * and the first and second characters (by primary weights) after it.
         * See FractionalUCA.txt. For example:
            [last variable [0C FE, 05, 05]] # U+10A7F OLD SOUTH ARABIAN NUMERIC INDICATOR
            [variable top = 0C FE]
            [first regular [0D 0A, 05, 05]] # U+0060 GRAVE ACCENT
           and
            00B4; [0D 0C, 05, 05]
         *
         * Note: Starting with UCA 6.0, the [variable top] collation element
         * is not the weight of any character or string,
         * which means that LAST_VARIABLE_CHAR_STRING sorts before [last variable].
         */
        String LAST_VARIABLE_CHAR_STRING = "\\U00010A7F";
        String FIRST_REGULAR_CHAR_STRING = "\\u0060";
        String SECOND_REGULAR_CHAR_STRING = "\\u00B4";

        /*
         * This string has to match the character that has the [last regular] weight
         * which changes with each UCA version.
         * See the bottom of FractionalUCA.txt which says something like
            [last regular [7A FE, 05, 05]] # U+1342E EGYPTIAN HIEROGLYPH AA032
         *
         * Note: Starting with UCA 6.0, the [last regular] collation element
         * is not the weight of any character or string,
         * which means that LAST_REGULAR_CHAR_STRING sorts before [last regular].
         */
        String LAST_REGULAR_CHAR_STRING = "\\U0001342E";

        String[] rules = {
            // cannot test this anymore, as [last primary ignorable] doesn't
            // have a  code point associated to it anymore
            // "&[before 3][last primary ignorable]<<<k",
            // - all befores here amount to zero
            /* "you cannot go before ...": The parser now sets an error for such nonsensical rules.
            "&[before 3][first tertiary ignorable]<<<a",
            "&[before 3][last tertiary ignorable]<<<a", */
            /*
             * However, there is a real secondary ignorable (artificial addition in FractionalUCA.txt),
             * and it *is* possible to "go before" that.
             */
            "&[before 3][first secondary ignorable]<<<a",
            "&[before 3][last secondary ignorable]<<<a",
            // 'normal' befores
            /*
             * Note: With a "SPACE first primary" boundary CE in FractionalUCA.txt,
             * it is not possible to tailor &[first primary ignorable]<a or &[last primary ignorable]<a
             * because there is no tailoring space before that boundary.
             * Made the tests work by tailoring to a space instead.
             */
            "&[before 3][first primary ignorable]<<<c<<<b &' '<a",  /* was &[first primary ignorable]<a */
            // we don't have a code point that corresponds to the last primary
            // ignorable
            "&[before 3][last primary ignorable]<<<c<<<b &' '<a",  /* was &[last primary ignorable]<a */
            "&[before 3][first variable]<<<c<<<b &[first variable]<a",
            "&[last variable]<a &[before 3][last variable]<<<c<<<b ",
            "&[first regular]<a &[before 1][first regular]<b",
            "&[before 1][last regular]<b &[last regular]<a",
            "&[before 1][first implicit]<b &[first implicit]<a",
            /* The current builder does not support tailoring to unassigned-implicit CEs (seems unnecessary, adds complexity).
            "&[before 1][last implicit]<b &[last implicit]<a", */
            "&[last variable]<z" +
            "&' '<x" +  /* was &[last primary ignorable]<x, see above */
            "&[last secondary ignorable]<<y&[last tertiary ignorable]<<<w&[top]<u",
        };
        String[][] data = {
            // {"k", "\u20e3"},
            /* "you cannot go before ...": The parser now sets an error for such nonsensical rules.
            {"\\u0000", "a"}, // you cannot go before first tertiary ignorable
            {"\\u0000", "a"}, // you cannot go before last tertiary ignorable */
            /*
             * However, there is a real secondary ignorable (artificial addition in FractionalUCA.txt),
             * and it *is* possible to "go before" that.
             */
            {"\\u0000", "a"},
            {"\\u0000", "a"},
            /*
             * Note: With a "SPACE first primary" boundary CE in FractionalUCA.txt,
             * it is not possible to tailor &[first primary ignorable]<a or &[last primary ignorable]<a
             * because there is no tailoring space before that boundary.
             * Made the tests work by tailoring to a space instead.
             */
            {"c", "b", "\\u0332", "a"},
            {"\\u0332", "\\u20e3", "c", "b", "a"},
            {"c", "b", "\\u0009", "a", "\\u000a"},
            {LAST_VARIABLE_CHAR_STRING, "c", "b", /* [last variable] */ "a", FIRST_REGULAR_CHAR_STRING},
            {"b", FIRST_REGULAR_CHAR_STRING, "a", SECOND_REGULAR_CHAR_STRING},
            // The character in the second ordering test string
            // has to match the character that has the [last regular] weight
            // which changes with each UCA version.
            // See the bottom of FractionalUCA.txt which says something like
            // [last regular [CE 27, 05, 05]] # U+1342E EGYPTIAN HIEROGLYPH AA032
            {LAST_REGULAR_CHAR_STRING, "b", /* [last regular] */ "a", "\\u4e00"},
            {"b", "\\u4e00", "a", "\\u4e01"},
            /* The current builder does not support tailoring to unassigned-implicit CEs (seems unnecessary, adds complexity).
            {"b", "\\U0010FFFD", "a"}, */
            {"\ufffb",  "w", "y", "\u20e3", "x", LAST_VARIABLE_CHAR_STRING, "z", "u"},
        };

        for (int i = 0; i< rules.length; i++) {
            logln(String.format("rules[%d] = \"%s\"", i, rules[i]));
            genericRulesStarter(rules[i], data[i]);
        }
    }

    void genericRulesStarter(String rules, String[] s) {
        genericRulesStarterWithResult(rules, s, -1);
    }

    void genericRulesStarterWithResult(String rules, String[] s, int result) {

        RuleBasedCollator coll = null;
        try {
            coll = new RuleBasedCollator(rules);
            // logln("Rules starter for " + rules);
            genericOrderingTestWithResult(coll, s, result);
        } catch (Exception e) {
            warnln("Unable to open collator with rules " + rules + ": " + e);
        }
    }

    void genericRulesStarterWithOptionsAndResult(String rules, String[] s, String[] atts, Object[] attVals, int result) {
        RuleBasedCollator coll = null;
        try {
            coll = new RuleBasedCollator(rules);
            genericOptionsSetter(coll, atts, attVals);
            genericOrderingTestWithResult(coll, s, result);
        } catch (Exception e) {
            warnln("Unable to open collator with rules " + rules);
        }
    }
    void genericOrderingTestWithResult(Collator coll, String[] s, int result) {
        String t1 = "";
        String t2 = "";

        for(int i = 0; i < s.length - 1; i++) {
            for(int j = i+1; j < s.length; j++) {
                t1 = Utility.unescape(s[i]);
                t2 = Utility.unescape(s[j]);
                // System.out.println(i + " " + j);
                CollationTest.doTest(this, (RuleBasedCollator)coll, t1, t2,
                                     result);
            }
        }
    }

    void reportCResult(String source, String target, CollationKey sourceKey, CollationKey targetKey,
                       int compareResult, int keyResult, int incResult, int expectedResult ) {
        if (expectedResult < -1 || expectedResult > 1) {
            errln("***** invalid call to reportCResult ****");
            return;
        }
        boolean ok1 = (compareResult == expectedResult);
        boolean ok2 = (keyResult == expectedResult);
        boolean ok3 = (incResult == expectedResult);
        if (ok1 && ok2 && ok3 /* synwee to undo && !isVerbose()*/) {
            return;
        } else {
            String msg1 = ok1? "Ok: compare(\"" : "FAIL: compare(\"";
            String msg2 = "\", \"";
            String msg3 = "\") returned ";
            String msg4 = "; expected ";
            String sExpect = new String("");
            String sResult = new String("");
            sResult = CollationTest.appendCompareResult(compareResult, sResult);
            sExpect = CollationTest.appendCompareResult(expectedResult, sExpect);
            if (ok1) {
                // logln(msg1 + source + msg2 + target + msg3 + sResult);
            } else {
                errln(msg1 + source + msg2 + target + msg3 + sResult + msg4 + sExpect);
            }
            msg1 = ok2 ? "Ok: key(\"" : "FAIL: key(\"";
            msg2 = "\").compareTo(key(\"";
            msg3 = "\")) returned ";
            sResult = CollationTest.appendCompareResult(keyResult, sResult);
            if (ok2) {
                // logln(msg1 + source + msg2 + target + msg3 + sResult);
            } else {
                errln(msg1 + source + msg2 + target + msg3 + sResult + msg4 + sExpect);
                msg1 = "  ";
                msg2 = " vs. ";
                errln(msg1 + CollationTest.prettify(sourceKey) + msg2 + CollationTest.prettify(targetKey));
            }
            msg1 = ok3 ? "Ok: incCompare(\"" : "FAIL: incCompare(\"";
            msg2 = "\", \"";
            msg3 = "\") returned ";
            sResult = CollationTest.appendCompareResult(incResult, sResult);
            if (ok3) {
                // logln(msg1 + source + msg2 + target + msg3 + sResult);
            } else {
                errln(msg1 + source + msg2 + target + msg3 + sResult + msg4 + sExpect);
            }
        }
    }

    @Test
    public void TestBeforePrefixFailure() {
        String[] rules = {
            "&g <<< a&[before 3]\uff41 <<< x",
            "&\u30A7=\u30A7=\u3047=\uff6a&\u30A8=\u30A8=\u3048=\uff74&[before 3]\u30a7<<<\u30a9",
            "&[before 3]\u30a7<<<\u30a9&\u30A7=\u30A7=\u3047=\uff6a&\u30A8=\u30A8=\u3048=\uff74",
        };
        String[][] data = {
            {"x", "\uff41"},
            {"\u30a9", "\u30a7"},
            {"\u30a9", "\u30a7"},
        };

        for(int i = 0; i< rules.length; i++) {
            genericRulesStarter(rules[i], data[i]);
        }
    }

    @Test
    public void TestContractionClosure() {
        // Note: This was also ported to the data-driven test, see collationtest.txt.
        String[] rules = {
            "&b=\u00e4\u00e4",
            "&b=\u00C5",
        };
        String[][] data = {
            { "b", "\u00e4\u00e4", "a\u0308a\u0308", "\u00e4a\u0308", "a\u0308\u00e4" },
            { "b", "\u00C5", "A\u030A", "\u212B" },
        };

        for(int i = 0; i< rules.length; i++) {
            genericRulesStarterWithResult(rules[i], data[i], 0);
        }
    }

    @Test
    public void TestPrefixCompose() {
        String rule1 = "&\u30a7<<<\u30ab|\u30fc=\u30ac|\u30fc";

        String string = rule1;
        try {
            RuleBasedCollator coll = new RuleBasedCollator(string);
            logln("rule:" + coll.getRules());
        } catch (Exception e) {
            warnln("Error open RuleBasedCollator rule = " + string);
        }
    }

    @Test
    public void TestStrCollIdenticalPrefix() {
        String rule = "&\ud9b0\udc70=\ud9b0\udc71";
        String test[] = {
            "ab\ud9b0\udc70",
            "ab\ud9b0\udc71"
        };
        genericRulesStarterWithResult(rule, test, 0);
    }

    @Test
    public void TestPrefix() {
        String[] rules = {
            "&z <<< z|a",
            "&z <<< z|   a",
            "[strength I]&a=\ud900\udc25&z<<<\ud900\udc25|a",
        };
        String[][] data = {
            {"zz", "za"},
            {"zz", "za"},
            {"aa", "az", "\ud900\udc25z", "\ud900\udc25a", "zz"},
        };

        for(int i = 0; i<rules.length; i++) {
            genericRulesStarter(rules[i], data[i]);
        }
    }

    @Test
    public void TestNewJapanese() {

        String test1[] = {
            "\u30b7\u30e3\u30fc\u30ec",
            "\u30b7\u30e3\u30a4",
            "\u30b7\u30e4\u30a3",
            "\u30b7\u30e3\u30ec",
            "\u3061\u3087\u3053",
            "\u3061\u3088\u3053",
            "\u30c1\u30e7\u30b3\u30ec\u30fc\u30c8",
            "\u3066\u30fc\u305f",
            "\u30c6\u30fc\u30bf",
            "\u30c6\u30a7\u30bf",
            "\u3066\u3048\u305f",
            "\u3067\u30fc\u305f",
            "\u30c7\u30fc\u30bf",
            "\u30c7\u30a7\u30bf",
            "\u3067\u3048\u305f",
            "\u3066\u30fc\u305f\u30fc",
            "\u30c6\u30fc\u30bf\u30a1",
            "\u30c6\u30a7\u30bf\u30fc",
            "\u3066\u3047\u305f\u3041",
            "\u3066\u3048\u305f\u30fc",
            "\u3067\u30fc\u305f\u30fc",
            "\u30c7\u30fc\u30bf\u30a1",
            "\u3067\u30a7\u305f\u30a1",
            "\u30c7\u3047\u30bf\u3041",
            "\u30c7\u30a8\u30bf\u30a2",
            "\u3072\u3086",
            "\u3073\u3085\u3042",
            "\u3074\u3085\u3042",
            "\u3073\u3085\u3042\u30fc",
            "\u30d3\u30e5\u30a2\u30fc",
            "\u3074\u3085\u3042\u30fc",
            "\u30d4\u30e5\u30a2\u30fc",
            "\u30d2\u30e5\u30a6",
            "\u30d2\u30e6\u30a6",
            "\u30d4\u30e5\u30a6\u30a2",
            "\u3073\u3085\u30fc\u3042\u30fc",
            "\u30d3\u30e5\u30fc\u30a2\u30fc",
            "\u30d3\u30e5\u30a6\u30a2\u30fc",
            "\u3072\u3085\u3093",
            "\u3074\u3085\u3093",
            "\u3075\u30fc\u308a",
            "\u30d5\u30fc\u30ea",
            "\u3075\u3045\u308a",
            "\u3075\u30a5\u308a",
            "\u3075\u30a5\u30ea",
            "\u30d5\u30a6\u30ea",
            "\u3076\u30fc\u308a",
            "\u30d6\u30fc\u30ea",
            "\u3076\u3045\u308a",
            "\u30d6\u30a5\u308a",
            "\u3077\u3046\u308a",
            "\u30d7\u30a6\u30ea",
            "\u3075\u30fc\u308a\u30fc",
            "\u30d5\u30a5\u30ea\u30fc",
            "\u3075\u30a5\u308a\u30a3",
            "\u30d5\u3045\u308a\u3043",
            "\u30d5\u30a6\u30ea\u30fc",
            "\u3075\u3046\u308a\u3043",
            "\u30d6\u30a6\u30ea\u30a4",
            "\u3077\u30fc\u308a\u30fc",
            "\u3077\u30a5\u308a\u30a4",
            "\u3077\u3046\u308a\u30fc",
            "\u30d7\u30a6\u30ea\u30a4",
            "\u30d5\u30fd",
            "\u3075\u309e",
            "\u3076\u309d",
            "\u3076\u3075",
            "\u3076\u30d5",
            "\u30d6\u3075",
            "\u30d6\u30d5",
            "\u3076\u309e",
            "\u3076\u3077",
            "\u30d6\u3077",
            "\u3077\u309d",
            "\u30d7\u30fd",
            "\u3077\u3075",
        };

        String test2[] = {
            "\u306f\u309d", // H\u309d
            "\u30cf\u30fd", // K\u30fd
            "\u306f\u306f", // HH
            "\u306f\u30cf", // HK
            "\u30cf\u30cf", // KK
            "\u306f\u309e", // H\u309e
            "\u30cf\u30fe", // K\u30fe
            "\u306f\u3070", // HH\u309b
            "\u30cf\u30d0", // KK\u309b
            "\u306f\u3071", // HH\u309c
            "\u30cf\u3071", // KH\u309c
            "\u30cf\u30d1", // KK\u309c
            "\u3070\u309d", // H\u309b\u309d
            "\u30d0\u30fd", // K\u309b\u30fd
            "\u3070\u306f", // H\u309bH
            "\u30d0\u30cf", // K\u309bK
            "\u3070\u309e", // H\u309b\u309e
            "\u30d0\u30fe", // K\u309b\u30fe
            "\u3070\u3070", // H\u309bH\u309b
            "\u30d0\u3070", // K\u309bH\u309b
            "\u30d0\u30d0", // K\u309bK\u309b
            "\u3070\u3071", // H\u309bH\u309c
            "\u30d0\u30d1", // K\u309bK\u309c
            "\u3071\u309d", // H\u309c\u309d
            "\u30d1\u30fd", // K\u309c\u30fd
            "\u3071\u306f", // H\u309cH
            "\u30d1\u30cf", // K\u309cK
            "\u3071\u3070", // H\u309cH\u309b
            "\u3071\u30d0", // H\u309cK\u309b
            "\u30d1\u30d0", // K\u309cK\u309b
            "\u3071\u3071", // H\u309cH\u309c
            "\u30d1\u30d1", // K\u309cK\u309c
        };

        String[] att = { "strength", };
        Object[] val = { new Integer(Collator.QUATERNARY), };

        String[] attShifted = { "strength", "AlternateHandling"};
        Object valShifted[] = { new Integer(Collator.QUATERNARY),
                                Boolean.TRUE };

        genericLocaleStarterWithOptions(Locale.JAPANESE, test1, att, val);
        genericLocaleStarterWithOptions(Locale.JAPANESE, test2, att, val);

        genericLocaleStarterWithOptions(Locale.JAPANESE, test1, attShifted,
                                        valShifted);
        genericLocaleStarterWithOptions(Locale.JAPANESE, test2, attShifted,
                                        valShifted);
    }

    void genericLocaleStarter(Locale locale, String s[]) {
        RuleBasedCollator coll = null;
        try {
            coll = (RuleBasedCollator)Collator.getInstance(locale);

        } catch (Exception e) {
            warnln("Unable to open collator for locale " + locale);
            return;
        }
        // logln("Locale starter for " + locale);
        genericOrderingTest(coll, s);
    }

    void genericLocaleStarterWithOptions(Locale locale, String[] s, String[] attrs, Object[] values) {
        genericLocaleStarterWithOptionsAndResult(locale, s, attrs, values, -1);
    }

    private void genericOptionsSetter(RuleBasedCollator coll, String[] attrs, Object[] values) {
        for(int i = 0; i < attrs.length; i++) {
            if (attrs[i].equals("strength")) {
                coll.setStrength(((Integer)values[i]).intValue());
            }
            else if (attrs[i].equals("decomp")) {
                coll.setDecomposition(((Integer)values[i]).intValue());
            }
            else if (attrs[i].equals("AlternateHandling")) {
                coll.setAlternateHandlingShifted(((Boolean)values[i]
                                                  ).booleanValue());
            }
            else if (attrs[i].equals("NumericCollation")) {
                coll.setNumericCollation(((Boolean)values[i]).booleanValue());
            }
            else if (attrs[i].equals("UpperFirst")) {
                coll.setUpperCaseFirst(((Boolean)values[i]).booleanValue());
            }
            else if (attrs[i].equals("LowerFirst")) {
                coll.setLowerCaseFirst(((Boolean)values[i]).booleanValue());
            }
            else if (attrs[i].equals("CaseLevel")) {
                coll.setCaseLevel(((Boolean)values[i]).booleanValue());
            }
        }
    }

    void genericLocaleStarterWithOptionsAndResult(Locale locale, String[] s, String[] attrs, Object[] values, int result) {
        RuleBasedCollator coll = null;
        try {
            coll = (RuleBasedCollator)Collator.getInstance(locale);
        } catch (Exception e) {
            warnln("Unable to open collator for locale " + locale);
            return;
        }
        // logln("Locale starter for " +locale);

        // logln("Setting attributes");
        genericOptionsSetter(coll, attrs, values);

        genericOrderingTestWithResult(coll, s, result);
    }

    void genericOrderingTest(Collator coll, String[] s) {
        genericOrderingTestWithResult(coll, s, -1);
    }

    @Test
    public void TestNonChars() {
        String test[] = {
            "\u0000",  /* ignorable */
            "\uFFFE",  /* special merge-sort character with minimum non-ignorable weights */
            "\uFDD0", "\uFDEF",
            "\\U0001FFFE", "\\U0001FFFF",  /* UCA 6.0: noncharacters are treated like unassigned, */
            "\\U0002FFFE", "\\U0002FFFF",  /* not like ignorable. */
            "\\U0003FFFE", "\\U0003FFFF",
            "\\U0004FFFE", "\\U0004FFFF",
            "\\U0005FFFE", "\\U0005FFFF",
            "\\U0006FFFE", "\\U0006FFFF",
            "\\U0007FFFE", "\\U0007FFFF",
            "\\U0008FFFE", "\\U0008FFFF",
            "\\U0009FFFE", "\\U0009FFFF",
            "\\U000AFFFE", "\\U000AFFFF",
            "\\U000BFFFE", "\\U000BFFFF",
            "\\U000CFFFE", "\\U000CFFFF",
            "\\U000DFFFE", "\\U000DFFFF",
            "\\U000EFFFE", "\\U000EFFFF",
            "\\U000FFFFE", "\\U000FFFFF",
            "\\U0010FFFE", "\\U0010FFFF",
            "\uFFFF"  /* special character with maximum primary weight */
        };
        Collator coll = null;
        try {
            coll = Collator.getInstance(new Locale("en", "US"));
        } catch (Exception e) {
            warnln("Unable to open collator");
            return;
        }
        // logln("Test non characters");

        genericOrderingTestWithResult(coll, test, -1);
    }

    @Test
    public void TestExtremeCompression() {
        String[] test = new String[4];

        for(int i = 0; i<4; i++) {
            StringBuffer temp = new StringBuffer();
            for (int j = 0; j < 2047; j++) {
                temp.append('a');
            }
            temp.append((char)('a' + i));
            test[i] = temp.toString();
        }

        genericLocaleStarter(new Locale("en", "US"), test);
    }

    /**
     * Tests surrogate support.
     */
    @Test
    public void TestSurrogates() {
        String test[] = {"z","\ud900\udc25", "\ud805\udc50", "\ud800\udc00y",
                         "\ud800\udc00r", "\ud800\udc00f", "\ud800\udc00",
                         "\ud800\udc00c", "\ud800\udc00b", "\ud800\udc00fa",
                         "\ud800\udc00fb", "\ud800\udc00a", "c", "b"};

        String rule = "&z < \ud900\udc25 < \ud805\udc50 < \ud800\udc00y "
            + "< \ud800\udc00r < \ud800\udc00f << \ud800\udc00 "
            + "< \ud800\udc00fa << \ud800\udc00fb < \ud800\udc00a "
            + "< c < b";
        genericRulesStarter(rule, test);
    }

    @Test
    public void TestBocsuCoverage() {
        String test = "\u0041\u0441\u4441\\U00044441\u4441\u0441\u0041";
        Collator coll = Collator.getInstance();
        coll.setStrength(Collator.IDENTICAL);
        CollationKey key = coll.getCollationKey(test);
        logln("source:" + key.getSourceString());
    }

    @Test
    public void TestCyrillicTailoring() {
        String test[] = {
            "\u0410b",
            "\u0410\u0306a",
            "\u04d0A"
        };

        // Most of the following are commented out because UCA 8.0
        // drops most of the Cyrillic contractions from the default order.
        // See CLDR ticket #7246 "root collation: remove Cyrillic contractions".

        // genericLocaleStarter(new Locale("en", ""), test);
        // genericRulesStarter("&\u0410 = \u0410", test);
        // genericRulesStarter("&Z < \u0410", test);
        genericRulesStarter("&\u0410 = \u0410 < \u04d0", test);
        genericRulesStarter("&Z < \u0410 < \u04d0", test);
        // genericRulesStarter("&\u0410 = \u0410 < \u0410\u0301", test);
        // genericRulesStarter("&Z < \u0410 < \u0410\u0301", test);
    }

    @Test
    public void TestSuppressContractions() {
        String testNoCont2[] = {
            "\u0410\u0302a",
            "\u0410\u0306b",
            "\u0410c"
        };
        String testNoCont[] = {
            "a\u0410",
            "A\u0410\u0306",
            "\uFF21\u0410\u0302"
        };

        genericRulesStarter("[suppressContractions [\u0400-\u047f]]", testNoCont);
        genericRulesStarter("[suppressContractions [\u0400-\u047f]]", testNoCont2);
    }

    @Test
    public void TestCase() {
        String gRules = "\u0026\u0030\u003C\u0031\u002C\u2460\u003C\u0061\u002C\u0041";
        String[] testCase = {
            "1a", "1A", "\u2460a", "\u2460A"
        };
        int[][] caseTestResults = {
            { -1, -1, -1, 0, -1, -1, 0, 0, -1 },
            { 1, -1, -1, 0, -1, -1, 0, 0, 1 },
            { -1, -1, -1, 0, 1, -1, 0, 0, -1 },
            { 1, -1, 1, 0, -1, -1, 0, 0, 1 }

        };
        boolean[][] caseTestAttributes = {
            { false, false},
            { true, false},
            { false, true},
            { true, true}
        };

        int i,j,k;
        Collator  myCollation;
        try {
            myCollation = Collator.getInstance(new Locale("en", "US"));
        } catch (Exception e) {
            warnln("ERROR: in creation of rule based collator ");
            return;
        }
        // logln("Testing different case settings");
        myCollation.setStrength(Collator.TERTIARY);

        for(k = 0; k <4; k++) {
            if (caseTestAttributes[k][0] == true) {
                // upper case first
                ((RuleBasedCollator)myCollation).setUpperCaseFirst(true);
            }
            else {
                // upper case first
                ((RuleBasedCollator)myCollation).setLowerCaseFirst(true);
            }
            ((RuleBasedCollator)myCollation).setCaseLevel(
                                                          caseTestAttributes[k][1]);

            // logln("Case first = " + caseTestAttributes[k][0] + ", Case level = " + caseTestAttributes[k][1]);
            for (i = 0; i < 3 ; i++) {
                for(j = i+1; j<4; j++) {
                    CollationTest.doTest(this,
                                         (RuleBasedCollator)myCollation,
                                         testCase[i], testCase[j],
                                         caseTestResults[k][3*i+j-1]);
                }
            }
        }
        try {
            myCollation = new RuleBasedCollator(gRules);
        } catch (Exception e) {
            warnln("ERROR: in creation of rule based collator");
            return;
        }
        // logln("Testing different case settings with custom rules");
        myCollation.setStrength(Collator.TERTIARY);

        for(k = 0; k<4; k++) {
            if (caseTestAttributes[k][0] == true) {
                ((RuleBasedCollator)myCollation).setUpperCaseFirst(true);
            }
            else {
                ((RuleBasedCollator)myCollation).setUpperCaseFirst(false);
            }
            ((RuleBasedCollator)myCollation).setCaseLevel(
                                                          caseTestAttributes[k][1]);
            for (i = 0; i < 3 ; i++) {
                for(j = i+1; j<4; j++) {
                    CollationTest.doTest(this,
                                         (RuleBasedCollator)myCollation,
                                         testCase[i], testCase[j],
                                         caseTestResults[k][3*i+j-1]);
                }
            }
        }

        {
            String[] lowerFirst = {
                "h",
                "H",
                "ch",
                "Ch",
                "CH",
                "cha",
                "chA",
                "Cha",
                "ChA",
                "CHa",
                "CHA",
                "i",
                "I"
            };

            String[] upperFirst = {
                "H",
                "h",
                "CH",
                "Ch",
                "ch",
                "CHA",
                "CHa",
                "ChA",
                "Cha",
                "chA",
                "cha",
                "I",
                "i"
            };
            // logln("mixed case test");
            // logln("lower first, case level off");
            genericRulesStarter("[caseFirst lower]&H<ch<<<Ch<<<CH", lowerFirst);
            // logln("upper first, case level off");
            genericRulesStarter("[caseFirst upper]&H<ch<<<Ch<<<CH", upperFirst);
            // logln("lower first, case level on");
            genericRulesStarter("[caseFirst lower][caseLevel on]&H<ch<<<Ch<<<CH", lowerFirst);
            // logln("upper first, case level on");
            genericRulesStarter("[caseFirst upper][caseLevel on]&H<ch<<<Ch<<<CH", upperFirst);
        }
    }

    @Test
    public void TestIncompleteCnt() {
        String[] cnt1 = {
            "AA",
            "AC",
            "AZ",
            "AQ",
            "AB",
            "ABZ",
            "ABQ",
            "Z",
            "ABC",
            "Q",
            "B"
        };

        String[] cnt2 = {
            "DA",
            "DAD",
            "DAZ",
            "MAR",
            "Z",
            "DAVIS",
            "MARK",
            "DAV",
            "DAVI"
        };
        RuleBasedCollator coll =  null;
        String temp = " & Z < ABC < Q < B";
        try {
            coll = new RuleBasedCollator(temp);
        } catch (Exception e) {
            warnln("fail to create RuleBasedCollator");
            return;
        }

        int size = cnt1.length;
        for(int i = 0; i < size-1; i++) {
            for(int j = i+1; j < size; j++) {
                String t1 = cnt1[i];
                String t2 = cnt1[j];
                CollationTest.doTest(this, coll, t1, t2, -1);
            }
        }

        temp = " & Z < DAVIS < MARK <DAV";
        try {
            coll = new RuleBasedCollator(temp);
        } catch (Exception e) {
            warnln("fail to create RuleBasedCollator");
            return;
        }

        size = cnt2.length;
        for(int i = 0; i < size-1; i++) {
            for(int j = i+1; j < size; j++) {
                String t1 = cnt2[i];
                String t2 = cnt2[j];
                CollationTest.doTest(this, coll, t1, t2, -1);
            }
        }
    }

    @Test
    public void TestBlackBird() {
        String[] shifted = {
            "black bird",
            "black-bird",
            "blackbird",
            "black Bird",
            "black-Bird",
            "blackBird",
            "black birds",
            "black-birds",
            "blackbirds"
        };
        int[] shiftedTert = {
            0,
            0,
            0,
            -1,
            0,
            0,
            -1,
            0,
            0
        };
        String[] nonignorable = {
            "black bird",
            "black Bird",
            "black birds",
            "black-bird",
            "black-Bird",
            "black-birds",
            "blackbird",
            "blackBird",
            "blackbirds"
        };
        int i = 0, j = 0;
        int size = 0;
        Collator coll = Collator.getInstance(new Locale("en", "US"));
        //ucol_setAttribute(coll, UCOL_NORMALIZATION_MODE, UCOL_OFF, &status);
        //ucol_setAttribute(coll, UCOL_ALTERNATE_HANDLING, UCOL_NON_IGNORABLE, &status);
        ((RuleBasedCollator)coll).setAlternateHandlingShifted(false);
        size = nonignorable.length;
        for(i = 0; i < size-1; i++) {
            for(j = i+1; j < size; j++) {
                String t1 = nonignorable[i];
                String t2 = nonignorable[j];
                CollationTest.doTest(this, (RuleBasedCollator)coll, t1, t2, -1);
            }
        }
        ((RuleBasedCollator)coll).setAlternateHandlingShifted(true);
        coll.setStrength(Collator.QUATERNARY);
        size = shifted.length;
        for(i = 0; i < size-1; i++) {
            for(j = i+1; j < size; j++) {
                String t1 = shifted[i];
                String t2 = shifted[j];
                CollationTest.doTest(this, (RuleBasedCollator)coll, t1, t2, -1);
            }
        }
        coll.setStrength(Collator.TERTIARY);
        size = shifted.length;
        for(i = 1; i < size; i++) {
            String t1 = shifted[i-1];
            String t2 = shifted[i];
            CollationTest.doTest(this, (RuleBasedCollator)coll, t1, t2,
                                 shiftedTert[i]);
        }
    }

    @Test
    public void TestFunkyA() {
        String[] testSourceCases = {
            "\u0041\u0300\u0301",
            "\u0041\u0300\u0316",
            "\u0041\u0300",
            "\u00C0\u0301",
            // this would work with forced normalization
            "\u00C0\u0316",
        };

        String[] testTargetCases = {
            "\u0041\u0301\u0300",
            "\u0041\u0316\u0300",
            "\u00C0",
            "\u0041\u0301\u0300",
            // this would work with forced normalization
            "\u0041\u0316\u0300",
        };

        int[] results = {
            1,
            0,
            0,
            1,
            0
        };

        Collator  myCollation;
        try {
            myCollation = Collator.getInstance(new Locale("en", "US"));
        } catch (Exception e) {
            warnln("ERROR: in creation of rule based collator");
            return;
        }
        // logln("Testing some A letters, for some reason");
        myCollation.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        myCollation.setStrength(Collator.TERTIARY);
        for (int i = 0; i < 4 ; i++)
            {
                CollationTest.doTest(this, (RuleBasedCollator)myCollation,
                                     testSourceCases[i], testTargetCases[i],
                                     results[i]);
            }
    }

    @Test
    public void TestChMove() {
        String[] chTest = {
            "c",
            "C",
            "ca", "cb", "cx", "cy", "CZ",
            "c\u030C", "C\u030C",
            "h",
            "H",
            "ha", "Ha", "harly", "hb", "HB", "hx", "HX", "hy", "HY",
            "ch", "cH", "Ch", "CH",
            "cha", "charly", "che", "chh", "chch", "chr",
            "i", "I", "iarly",
            "r", "R",
            "r\u030C", "R\u030C",
            "s",
            "S",
            "s\u030C", "S\u030C",
            "z", "Z",
            "z\u030C", "Z\u030C"
        };
        Collator coll = null;
        try {
            coll = Collator.getInstance(new Locale("cs", ""));
        } catch (Exception e) {
            warnln("Cannot create Collator");
            return;
        }
        int size = chTest.length;
        for(int i = 0; i < size-1; i++) {
            for(int j = i+1; j < size; j++) {
                String t1 = chTest[i];
                String t2 = chTest[j];
                CollationTest.doTest(this, (RuleBasedCollator)coll, t1, t2, -1);
            }
        }
    }

    @Test
    public void TestImplicitTailoring() {
        String rules[] = {
            /* Tailor b and c before U+4E00. */
            "&[before 1]\u4e00 < b < c " +
            /* Now, before U+4E00 is c; put d and e after that. */
            "&[before 1]\u4e00 < d < e",
            "&\u4e00 < a <<< A < b <<< B",
            "&[before 1]\u4e00 < \u4e01 < \u4e02",
            "&[before 1]\u4e01 < \u4e02 < \u4e03",
        };
        String cases[][] = {
            { "b", "c", "d", "e", "\u4e00" },
            { "\u4e00", "a", "A", "b", "B", "\u4e01" },
            { "\u4e01", "\u4e02", "\u4e00" },
            { "\u4e02", "\u4e03", "\u4e01" },
        };

        int i = 0;

        for(i = 0; i < rules.length; i++) {
            genericRulesStarter(rules[i], cases[i]);
        }
    }

    @Test
    public void TestFCDProblem() {
        String s1 = "\u0430\u0306\u0325";
        String s2 = "\u04D1\u0325";
        Collator coll = null;
        try {
            coll = Collator.getInstance();
        } catch (Exception e) {
            warnln("Can't create collator");
            return;
        }

        coll.setDecomposition(Collator.NO_DECOMPOSITION);
        CollationTest.doTest(this, (RuleBasedCollator)coll, s1, s2, 0);
        coll.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        CollationTest.doTest(this, (RuleBasedCollator)coll, s1, s2, 0);
    }

    @Test
    public void TestEmptyRule() {
        String rulez = "";
        try {
            RuleBasedCollator coll = new RuleBasedCollator(rulez);
            logln("rule:" + coll.getRules());
        } catch (Exception e) {
            warnln(e.getMessage());
        }
    }

    /* superseded by TestBeforePinyin, since Chinese collation rules have changed */
    /*
    @Test
    public void TestJ784() {
        String[] data = {
            "A", "\u0101", "\u00e1", "\u01ce", "\u00e0",
            "E", "\u0113", "\u00e9", "\u011b", "\u00e8",
            "I", "\u012b", "\u00ed", "\u01d0", "\u00ec",
            "O", "\u014d", "\u00f3", "\u01d2", "\u00f2",
            "U", "\u016b", "\u00fa", "\u01d4", "\u00f9",
            "\u00fc", "\u01d6", "\u01d8", "\u01da", "\u01dc"
        };
        genericLocaleStarter(new Locale("zh", ""), data);
    }
    */

    @Test
    public void TestJ815() {
        String data[] = {
            "aa",
            "Aa",
            "ab",
            "Ab",
            "ad",
            "Ad",
            "ae",
            "Ae",
            "\u00e6",
            "\u00c6",
            "af",
            "Af",
            "b",
            "B"
        };
        genericLocaleStarter(new Locale("fr", ""), data);
        genericRulesStarter("[backwards 2]&A<<\u00e6/e<<<\u00c6/E", data);
    }

    @Test
    public void TestJ3087()
    {
        String rule[] = {
                "&h<H&CH=\u0427",
                /*
                 * The ICU 53 builder adheres to the principle that
                 * a rule is affected by previous rules but not following ones.
                 * Therefore, setting CH=\u0427 and then re-tailoring H makes CH != \u0427.
                "&CH=\u0427&h<H", */
                "&CH=\u0427"
        };
        RuleBasedCollator rbc = null;
        CollationElementIterator iter1;
        CollationElementIterator iter2;
        for (int i = 0; i < rule.length; i ++) {
            try {
                rbc = new RuleBasedCollator(rule[i]);
            } catch (Exception e) {
                warnln(e.getMessage());
                continue;
            }
            iter1 = rbc.getCollationElementIterator("CH");
            iter2 = rbc.getCollationElementIterator("\u0427");
            int ce1 = CollationElementIterator.IGNORABLE;
            int ce2 = CollationElementIterator.IGNORABLE;
            // The ICU 53 builder code sets the uppercase flag only on the first CE.
            int mask = ~0;
            while (ce1 != CollationElementIterator.NULLORDER
                   && ce2 != CollationElementIterator.NULLORDER) {
                ce1 = iter1.next();
                ce2 = iter2.next();
                if ((ce1 & mask) != (ce2 & mask)) {
                    errln("Error generating RuleBasedCollator with the rule "
                          + rule[i]);
                    errln("CH != \\u0427");
                }
                mask = ~0xc0;  // mask off case/continuation bits
            }
        }
    }

    // TODO(user): not running before
    @Ignore
    @Test
    public void DontTestJ831() { // Latvian does not use upper first
        String[] data = {
            "I",
            "i",
            "Y",
            "y"
        };
        genericLocaleStarter(new Locale("lv", ""), data);
    }

    @Test
    public void TestBefore() {
        String data[] = {
            "\u0101", "\u00e1", "\u01ce", "\u00e0", "A",
            "\u0113", "\u00e9", "\u011b", "\u00e8", "E",
            "\u012b", "\u00ed", "\u01d0", "\u00ec", "I",
            "\u014d", "\u00f3", "\u01d2", "\u00f2", "O",
            "\u016b", "\u00fa", "\u01d4", "\u00f9", "U",
            "\u01d6", "\u01d8", "\u01da", "\u01dc", "\u00fc"
        };
        genericRulesStarter(
                            "&[before 1]a<\u0101<\u00e1<\u01ce<\u00e0"
                            + "&[before 1]e<\u0113<\u00e9<\u011b<\u00e8"
                            + "&[before 1]i<\u012b<\u00ed<\u01d0<\u00ec"
                            + "&[before 1]o<\u014d<\u00f3<\u01d2<\u00f2"
                            + "&[before 1]u<\u016b<\u00fa<\u01d4<\u00f9"
                            + "&u<\u01d6<\u01d8<\u01da<\u01dc<\u00fc", data);
    }

    @Test
    public void TestHangulTailoring() {
        String[] koreanData = {
            "\uac00", "\u4f3d", "\u4f73", "\u5047", "\u50f9", "\u52a0", "\u53ef", "\u5475",
            "\u54e5", "\u5609", "\u5ac1", "\u5bb6", "\u6687", "\u67b6", "\u67b7", "\u67ef",
            "\u6b4c", "\u73c2", "\u75c2", "\u7a3c", "\u82db", "\u8304", "\u8857", "\u8888",
            "\u8a36", "\u8cc8", "\u8dcf", "\u8efb", "\u8fe6", "\u99d5",
            "\u4EEE", "\u50A2", "\u5496", "\u54FF", "\u5777", "\u5B8A", "\u659D", "\u698E",
            "\u6A9F", "\u73C8", "\u7B33", "\u801E", "\u8238", "\u846D", "\u8B0C"
        };

        String rules =
            "&\uac00 <<< \u4f3d <<< \u4f73 <<< \u5047 <<< \u50f9 <<< \u52a0 <<< \u53ef <<< \u5475 "
            + "<<< \u54e5 <<< \u5609 <<< \u5ac1 <<< \u5bb6 <<< \u6687 <<< \u67b6 <<< \u67b7 <<< \u67ef "
            + "<<< \u6b4c <<< \u73c2 <<< \u75c2 <<< \u7a3c <<< \u82db <<< \u8304 <<< \u8857 <<< \u8888 "
            + "<<< \u8a36 <<< \u8cc8 <<< \u8dcf <<< \u8efb <<< \u8fe6 <<< \u99d5 "
            + "<<< \u4EEE <<< \u50A2 <<< \u5496 <<< \u54FF <<< \u5777 <<< \u5B8A <<< \u659D <<< \u698E "
            + "<<< \u6A9F <<< \u73C8 <<< \u7B33 <<< \u801E <<< \u8238 <<< \u846D <<< \u8B0C";

        String rlz = rules;

        Collator coll = null;
        try {
            coll = new RuleBasedCollator(rlz);
        } catch (Exception e) {
            warnln("Unable to open collator with rules" + rules);
            return;
        }
        // logln("Using start of korean rules\n");
        genericOrderingTest(coll, koreanData);

        // no such locale in icu4j
        // logln("Using ko__LOTUS locale\n");
        // genericLocaleStarter(new Locale("ko__LOTUS", ""), koreanData);
    }

    @Test
    public void TestIncrementalNormalize() {
        Collator        coll = null;
        // logln("Test 1 ....");
        {
            /* Test 1.  Run very long unnormalized strings, to force overflow of*/
            /*          most buffers along the way.*/

            try {
                coll = Collator.getInstance(new Locale("en", "US"));
            } catch (Exception e) {
                warnln("Cannot get default instance!");
                return;
            }
            char baseA     =0x41;
            char ccMix[]   = {0x316, 0x321, 0x300};
            int          sLen;
            int          i;
            StringBuffer strA = new StringBuffer();
            StringBuffer strB = new StringBuffer();

            coll.setDecomposition(Collator.CANONICAL_DECOMPOSITION);

            for (sLen = 1000; sLen<1001; sLen++) {
                strA.delete(0, strA.length());
                strA.append(baseA);
                strB.delete(0, strB.length());
                strB.append(baseA);
                for (i=1; i< sLen; i++) {
                    strA.append(ccMix[i % 3]);
                    strB.insert(1, ccMix[i % 3]);
                }
                coll.setStrength(Collator.TERTIARY);   // Do test with default strength, which runs
                CollationTest.doTest(this, (RuleBasedCollator)coll,
                                     strA.toString(), strB.toString(), 0);    //   optimized functions in the impl
                coll.setStrength(Collator.IDENTICAL);   // Do again with the slow, general impl.
                CollationTest.doTest(this, (RuleBasedCollator)coll,
                                     strA.toString(), strB.toString(), 0);
            }
        }
        /*  Test 2:  Non-normal sequence in a string that extends to the last character*/
        /*         of the string.  Checks a couple of edge cases.*/
        // logln("Test 2 ....");
        {
            String strA = "AA\u0300\u0316";
            String strB = "A\u00c0\u0316";
            coll.setStrength(Collator.TERTIARY);
            CollationTest.doTest(this, (RuleBasedCollator)coll, strA, strB, 0);
        }
        /*  Test 3:  Non-normal sequence is terminated by a surrogate pair.*/
        // logln("Test 3 ....");
        {
            String strA = "AA\u0300\u0316\uD800\uDC01";
            String strB = "A\u00c0\u0316\uD800\uDC00";
            coll.setStrength(Collator.TERTIARY);
            CollationTest.doTest(this, (RuleBasedCollator)coll, strA, strB, 1);
        }
        /*  Test 4:  Imbedded nulls do not terminate a string when length is specified.*/
        // logln("Test 4 ....");
        /*
         * not a valid test since string are null-terminated in java{
         char strA[] = {0x41, 0x00, 0x42};
         char strB[] = {0x41, 0x00, 0x00};

         int result = coll.compare(new String(strA), new String(strB));
         if (result != 1) {
         errln("ERROR 1 in test 4\n");
         }

         result = coll.compare(new String(strA, 0, 1), new String(strB, 0, 1));
         if (result != 0) {
         errln("ERROR 1 in test 4\n");
         }

         CollationKey sortKeyA = coll.getCollationKey(new String(strA));
         CollationKey sortKeyB = coll.getCollationKey(new String(strB));

         int r = sortKeyA.compareTo(sortKeyB);
         if (r <= 0) {
         errln("Error 4 in test 4\n");
         }

         coll.setStrength(Collator.IDENTICAL);
         sortKeyA = coll.getCollationKey(new String(strA));
         sortKeyB = coll.getCollationKey(new String(strB));

         r = sortKeyA.compareTo(sortKeyB);
         if (r <= 0) {
         errln("Error 7 in test 4\n");
         }

         coll.setStrength(Collator.TERTIARY);
         }
        */
        /*  Test 5:  Null characters in non-normal source strings.*/
        // logln("Test 5 ....");
        /*
         * not a valid test since string are null-terminated in java{
         {
         char strA[] = {0x41, 0x41, 0x300, 0x316, 0x00, 0x42,};
         char strB[] = {0x41, 0x41, 0x300, 0x316, 0x00, 0x00,};


         int result = coll.compare(new String(strA, 0, 6), new String(strB, 0, 6));
         if (result < 0) {
         errln("ERROR 1 in test 5\n");
         }
         result = coll.compare(new String(strA, 0, 4), new String(strB, 0, 4));
         if (result != 0) {
         errln("ERROR 2 in test 5\n");
         }

         CollationKey sortKeyA = coll.getCollationKey(new String(strA));
         CollationKey sortKeyB = coll.getCollationKey(new String(strB));
         int r = sortKeyA.compareTo(sortKeyB);
         if (r <= 0) {
         errln("Error 4 in test 5\n");
         }

         coll.setStrength(Collator.IDENTICAL);

         sortKeyA = coll.getCollationKey(new String(strA));
         sortKeyB = coll.getCollationKey(new String(strB));
         r = sortKeyA.compareTo(sortKeyB);
         if (r <= 0) {
         errln("Error 7 in test 5\n");
         }

         coll.setStrength(Collator.TERTIARY);
         }
        */
        /*  Test 6:  Null character as base of a non-normal combining sequence.*/
        // logln("Test 6 ....");
        /*
         * not a valid test since string are null-terminated in java{
         {
         char strA[] = {0x41, 0x0, 0x300, 0x316, 0x41, 0x302,};
         char strB[] = {0x41, 0x0, 0x302, 0x316, 0x41, 0x300,};

         int result = coll.compare(new String(strA, 0, 5), new String(strB, 0, 5));
         if (result != -1) {
         errln("Error 1 in test 6\n");
         }
         result = coll.compare(new String(strA, 0, 1), new String(strB, 0, 1));
         if (result != 0) {
         errln("Error 2 in test 6\n");
         }
         }
        */
    }

    @Test
    public void TestContraction() {
        String[] testrules = {
            "&A = AB / B",
            "&A = A\\u0306/\\u0306",
            "&c = ch / h",
        };
        String[] testdata = {
            "AB", "AB", "A\u0306", "ch"
        };
        String[] testdata2 = {
            "\u0063\u0067",
            "\u0063\u0068",
            "\u0063\u006C",
        };
        /*
         * These pairs of rule strings are not guaranteed to yield the very same mappings.
         * In fact, LDML 24 recommends an improved way of creating mappings
         * which always yields different mappings for such pairs. See
         * http://www.unicode.org/reports/tr35/tr35-33/tr35-collation.html#Orderings
        String[] testrules3 = {
            "&z < xyz &xyzw << B",
            "&z < xyz &xyz << B / w",
            "&z < ch &achm << B",
            "&z < ch &a << B / chm",
            "&\ud800\udc00w << B",
            "&\ud800\udc00 << B / w",
            "&a\ud800\udc00m << B",
            "&a << B / \ud800\udc00m",
        }; */

        RuleBasedCollator  coll = null;
        for (int i = 0; i < testrules.length; i ++) {
            CollationElementIterator iter1 = null;
            int j = 0;
            // logln("Rule " + testrules[i] + " for testing\n");
            String rule = testrules[i];
            try {
                coll = new RuleBasedCollator(rule);
            } catch (Exception e) {
                warnln("Collator creation failed " + testrules[i]);
                return;
            }
            try {
                iter1 = coll.getCollationElementIterator(testdata[i]);
            } catch (Exception e) {
                errln("Collation iterator creation failed\n");
                return;
            }
            while (j < 2) {
                CollationElementIterator iter2;
                int ce;
                try {
                    iter2 = coll.getCollationElementIterator(String.valueOf(testdata[i].charAt(j)));

                }catch (Exception e) {
                    errln("Collation iterator creation failed\n");
                    return;
                }
                ce = iter2.next();
                while (ce != CollationElementIterator.NULLORDER) {
                    if (iter1.next() != ce) {
                        errln("Collation elements in contraction split does not match\n");
                        return;
                    }
                    ce = iter2.next();
                }
                j ++;
            }
            if (iter1.next() != CollationElementIterator.NULLORDER) {
                errln("Collation elements not exhausted\n");
                return;
            }
        }
        String rule = "& a < b < c < ch < d & c = ch / h";
        try {
            coll = new RuleBasedCollator(rule);
        } catch (Exception e) {
            errln("cannot create rulebased collator");
            return;
        }

        if (coll.compare(testdata2[0], testdata2[1]) != -1) {
            errln("Expected " + testdata2[0] + " < " + testdata2[1]);
            return;
        }
        if (coll.compare(testdata2[1], testdata2[2]) != -1) {
            errln("Expected " + testdata2[1] + " < " + testdata2[2]);
            return;
        }
        /* see above -- for (int i = 0; i < testrules3.length; i += 2) {
            RuleBasedCollator          coll1, coll2;
            CollationElementIterator iter1, iter2;
            char               ch = 0x0042;
            int            ce;
            rule = testrules3[i];
            try {
                coll1 = new RuleBasedCollator(rule);
            } catch (Exception e) {
                errln("Fail: cannot create rulebased collator, rule:" + rule);
                return;
            }
            rule = testrules3[i + 1];
            try {
                coll2 = new RuleBasedCollator(rule);
            } catch (Exception e) {
                errln("Collator creation failed " + testrules[i]);
                return;
            }
            try {
                iter1 = coll1.getCollationElementIterator(String.valueOf(ch));
                iter2 = coll2.getCollationElementIterator(String.valueOf(ch));
            } catch (Exception e) {
                errln("Collation iterator creation failed\n");
                return;
            }
            ce = iter1.next();

            while (ce != CollationElementIterator.NULLORDER) {
                if (ce != iter2.next()) {
                    errln("CEs does not match\n");
                    return;
                }
                ce = iter1.next();
            }
            if (iter2.next() != CollationElementIterator.NULLORDER) {
                errln("CEs not exhausted\n");
                return;
            }
        } */
    }

    @Test
    public void TestExpansion() {
        String[] testrules = {
            /*
             * This seems to have tested that M was not mapped to an expansion.
             * I believe the old builder just did that because it computed the extension CEs
             * at the very end, which was a bug.
             * Among other problems, it violated the core tailoring principle
             * by making an earlier rule depend on a later one.
             * And, of course, if M did not get an expansion, then it was primary different from K,
             * unlike what the rule &K<<M says.
            "&J << K / B & K << M",
             */
            "&J << K / B << M"
        };
        String[] testdata = {
            "JA", "MA", "KA", "KC", "JC", "MC",
        };

        Collator  coll;
        for (int i = 0; i < testrules.length; i++) {
            // logln("Rule " + testrules[i] + " for testing\n");
            String rule = testrules[i];
            try {
                coll = new RuleBasedCollator(rule);
            } catch (Exception e) {
                warnln("Collator creation failed " + testrules[i]);
                return;
            }

            for (int j = 0; j < 5; j ++) {
                CollationTest.doTest(this, (RuleBasedCollator)coll,
                                     testdata[j], testdata[j + 1], -1);
            }
        }
    }

    @Test
    public void TestContractionEndCompare()
    {
        String rules = "&b=ch";
        String src = "bec";
        String tgt = "bech";
        Collator coll = null;
        try {
            coll = new RuleBasedCollator(rules);
        } catch (Exception e) {
            warnln("Collator creation failed " + rules);
            return;
        }
        CollationTest.doTest(this, (RuleBasedCollator)coll, src, tgt, 1);
    }

    @Test
    public void TestLocaleRuleBasedCollators() {
        if (TestFmwk.getExhaustiveness() < 5) {
            // not serious enough to run this
            return;
        }
        Locale locale[] = Collator.getAvailableLocales();
        String prevrule = null;
        for (int i = 0; i < locale.length; i ++) {
            Locale l = locale[i];
            try {
                ICUResourceBundle rb = (ICUResourceBundle)UResourceBundle.getBundleInstance(ICUData.ICU_COLLATION_BASE_NAME,l);
                String collkey = rb.getStringWithFallback("collations/default");
                ICUResourceBundle elements = rb.getWithFallback("collations/" + collkey);
                if (elements == null) {
                    continue;
                }
                String rule = null;
                /*
                  Object[][] colldata = (Object[][])elements;
                  // %%CollationBin
                  if (colldata[0][1] instanceof byte[]){
                  rule = (String)colldata[1][1];
                  }
                  else {
                  rule = (String)colldata[0][1];
                  }
                */
                rule = elements.getString("Sequence");

                RuleBasedCollator col1 =
                    (RuleBasedCollator)Collator.getInstance(l);
                if (!rule.equals(col1.getRules())) {
                    errln("Rules should be the same in the RuleBasedCollator and Locale");
                }
                if (rule != null && rule.length() > 0
                    && !rule.equals(prevrule)) {
                    RuleBasedCollator col2 = new RuleBasedCollator(rule);
                    if (!col1.equals(col2)) {
                        errln("Error creating RuleBasedCollator from " +
                              "locale rules for " + l.toString());
                    }
                }
                prevrule = rule;
            } catch (Exception e) {
                warnln("Error retrieving resource bundle for testing: " + e.toString());
            }
        }
    }

    @Test
    public void TestOptimize() {
        /* this is not really a test - just trying out
         * whether copying of UCA contents will fail
         * Cannot really test, since the functionality
         * remains the same.
         */
        String rules[] = {
            "[optimize [\\uAC00-\\uD7FF]]"
        };
        String data[][] = {
            { "a", "b"}
        };
        int i = 0;

        for(i = 0; i<rules.length; i++) {
            genericRulesStarter(rules[i], data[i]);
        }
    }

    @Test
    public void TestIdenticalCompare()
    {
        try {
            RuleBasedCollator coll
                = new RuleBasedCollator("& \uD800\uDC00 = \uD800\uDC01");
            String strA = "AA\u0300\u0316\uD800\uDC01";
            String strB = "A\u00c0\u0316\uD800\uDC00";
            coll.setStrength(Collator.IDENTICAL);
            CollationTest.doTest(this, coll, strA, strB, 1);
        } catch (Exception e) {
            warnln(e.getMessage());
        }
    }

    @Test
    public void TestMergeSortKeys()
    {
        String cases[] = {"abc", "abcd", "abcde"};
        String prefix = "foo";
        String suffix = "egg";
        CollationKey mergedPrefixKeys[] = new CollationKey[cases.length];
        CollationKey mergedSuffixKeys[] = new CollationKey[cases.length];

        Collator coll = Collator.getInstance(Locale.ENGLISH);
        genericLocaleStarter(Locale.ENGLISH, cases);

        int strength = Collator.PRIMARY;
        while (strength <= Collator.IDENTICAL) {
            coll.setStrength(strength);
            CollationKey prefixKey = coll.getCollationKey(prefix);
            CollationKey suffixKey = coll.getCollationKey(suffix);
            for (int i = 0; i < cases.length; i ++) {
                CollationKey key = coll.getCollationKey(cases[i]);
                mergedPrefixKeys[i] = prefixKey.merge(key);
                mergedSuffixKeys[i] = suffixKey.merge(key);
                if (mergedPrefixKeys[i].getSourceString() != null
                    || mergedSuffixKeys[i].getSourceString() != null) {
                    errln("Merged source string error: expected null");
                }
                if (i > 0) {
                    if (mergedPrefixKeys[i-1].compareTo(mergedPrefixKeys[i])
                        >= 0) {
                        errln("Error while comparing prefixed keys @ strength "
                              + strength);
                        errln(CollationTest.prettify(mergedPrefixKeys[i-1]));
                        errln(CollationTest.prettify(mergedPrefixKeys[i]));
                    }
                    if (mergedSuffixKeys[i-1].compareTo(mergedSuffixKeys[i])
                        >= 0) {
                        errln("Error while comparing suffixed keys @ strength "
                              + strength);
                        errln(CollationTest.prettify(mergedSuffixKeys[i-1]));
                        errln(CollationTest.prettify(mergedSuffixKeys[i]));
                    }
                }
            }
            if (strength == Collator.QUATERNARY) {
                strength = Collator.IDENTICAL;
            }
            else {
                strength ++;
            }
        }
    }

    @Test
    public void TestVariableTop()
    {
        // ICU 53+: The character must be in a supported reordering group,
        // and the variable top is pinned to the end of that group.
        // parseNextToken is not released as public so i create my own rules
        String rules = "& ' ' < b < c < de < fg & hi = j";
        try {
            RuleBasedCollator coll = new RuleBasedCollator(rules);
            String tokens[] = {" ", "b", "c", "de", "fg", "hi", "j", "ab"};
            coll.setAlternateHandlingShifted(true);
            for (int i = 0; i < tokens.length; i ++) {
                int varTopOriginal = coll.getVariableTop();
                try {
                    int varTop = coll.setVariableTop(tokens[i]);
                    if (i > 4) {
                        errln("Token " + tokens[i] + " expected to fail");
                    }
                    if (varTop != coll.getVariableTop()) {
                        errln("Error setting and getting variable top");
                    }
                    CollationKey key1 = coll.getCollationKey(tokens[i]);
                    for (int j = 0; j < i; j ++) {
                        CollationKey key2 = coll.getCollationKey(tokens[j]);
                        if (key2.compareTo(key1) < 0) {
                            errln("Setting variable top shouldn't change the comparison sequence");
                        }
                        byte sortorder[] = key2.toByteArray();
                        if (sortorder.length > 0
                            && (key2.toByteArray())[0] > 1) {
                            errln("Primary sort order should be 0");
                        }
                    }
                } catch (Exception e) {
                    CollationElementIterator iter
                        = coll.getCollationElementIterator(tokens[i]);
                    /*int ce =*/ iter.next();
                    int ce2 = iter.next();
                    if (ce2 == CollationElementIterator.NULLORDER) {
                        errln("Token " + tokens[i] + " not expected to fail");
                    }
                    if (coll.getVariableTop() != varTopOriginal) {
                        errln("When exception is thrown variable top should "
                              + "not be changed");
                    }
                }
                coll.setVariableTop(varTopOriginal);
                if (varTopOriginal != coll.getVariableTop()) {
                    errln("Couldn't restore old variable top\n");
                }
            }

            // Testing calling with error set
            try {
                coll.setVariableTop("");
                errln("Empty string should throw an IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                logln("PASS: Empty string failed as expected");
            }
            try {
                coll.setVariableTop(null);
                errln("Null string should throw an IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                logln("PASS: null string failed as expected");
            }
        } catch (Exception e) {
            warnln("Error creating RuleBasedCollator");
        }
    }

    // ported from cmsccoll.c
    @Test
    public void TestVariableTopSetting() {
        int varTopOriginal = 0, varTop1, varTop2;
        Collator coll = Collator.getInstance(ULocale.ROOT);

        String empty = "";
        String space = " ";
        String dot = ".";  /* punctuation */
        String degree = "\u00b0";  /* symbol */
        String dollar = "$";  /* currency symbol */
        String zero = "0";  /* digit */

        varTopOriginal = coll.getVariableTop();
        logln(String.format("coll.getVariableTop(root) -> %08x", varTopOriginal));
        ((RuleBasedCollator)coll).setAlternateHandlingShifted(true);

        varTop1 = coll.setVariableTop(space);
        varTop2 = coll.getVariableTop();
        logln(String.format("coll.setVariableTop(space) -> %08x", varTop1));
        if(varTop1 != varTop2 ||
                !coll.equals(empty, space) ||
                coll.equals(empty, dot) ||
                coll.equals(empty, degree) ||
                coll.equals(empty, dollar) ||
                coll.equals(empty, zero) ||
                coll.compare(space, dot) >= 0) {
            errln("coll.setVariableTop(space) did not work");
        }

        varTop1 = coll.setVariableTop(dot);
        varTop2 = coll.getVariableTop();
        logln(String.format("coll.setVariableTop(dot) -> %08x", varTop1));
        if(varTop1 != varTop2 ||
                !coll.equals(empty, space) ||
                !coll.equals(empty, dot) ||
                coll.equals(empty, degree) ||
                coll.equals(empty, dollar) ||
                coll.equals(empty, zero) ||
                coll.compare(dot, degree) >= 0) {
            errln("coll.setVariableTop(dot) did not work");
        }

        varTop1 = coll.setVariableTop(degree);
        varTop2 = coll.getVariableTop();
        logln(String.format("coll.setVariableTop(degree) -> %08x", varTop1));
        if(varTop1 != varTop2 ||
                !coll.equals(empty, space) ||
                !coll.equals(empty, dot) ||
                !coll.equals(empty, degree) ||
                coll.equals(empty, dollar) ||
                coll.equals(empty, zero) ||
                coll.compare(degree, dollar) >= 0) {
            errln("coll.setVariableTop(degree) did not work");
        }

        varTop1 = coll.setVariableTop(dollar);
        varTop2 = coll.getVariableTop();
        logln(String.format("coll.setVariableTop(dollar) -> %08x", varTop1));
        if(varTop1 != varTop2 ||
                !coll.equals(empty, space) ||
                !coll.equals(empty, dot) ||
                !coll.equals(empty, degree) ||
                !coll.equals(empty, dollar) ||
                coll.equals(empty, zero) ||
                coll.compare(dollar, zero) >= 0) {
            errln("coll.setVariableTop(dollar) did not work");
        }

        logln("Testing setting variable top to contractions");
        try {
            coll.setVariableTop("@P");
            errln("Invalid contraction succeded in setting variable top!");
        } catch(Exception expected) {
        }

        logln("Test restoring variable top");
        coll.setVariableTop(varTopOriginal);
        if(varTopOriginal != coll.getVariableTop()) {
            errln("Couldn't restore old variable top");
        }
    }

    // ported from cmsccoll.c
    @Test
    public void TestMaxVariable() {
        int oldMax, max;

        String empty = "";
        String space = " ";
        String dot = ".";  /* punctuation */
        String degree = "\u00b0";  /* symbol */
        String dollar = "$";  /* currency symbol */
        String zero = "0";  /* digit */

        Collator coll = Collator.getInstance(ULocale.ROOT);

        oldMax = coll.getMaxVariable();
        logln(String.format("coll.getMaxVariable(root) -> %04x", oldMax));
        ((RuleBasedCollator)coll).setAlternateHandlingShifted(true);

        coll.setMaxVariable(Collator.ReorderCodes.SPACE);
        max = coll.getMaxVariable();
        logln(String.format("coll.setMaxVariable(space) -> %04x", max));
        if(max != Collator.ReorderCodes.SPACE ||
                !coll.equals(empty, space) ||
                coll.equals(empty, dot) ||
                coll.equals(empty, degree) ||
                coll.equals(empty, dollar) ||
                coll.equals(empty, zero) ||
                coll.compare(space, dot) >= 0) {
            errln("coll.setMaxVariable(space) did not work");
        }

        coll.setMaxVariable(Collator.ReorderCodes.PUNCTUATION);
        max = coll.getMaxVariable();
        logln(String.format("coll.setMaxVariable(punctuation) -> %04x", max));
        if(max != Collator.ReorderCodes.PUNCTUATION ||
                !coll.equals(empty, space) ||
                !coll.equals(empty, dot) ||
                coll.equals(empty, degree) ||
                coll.equals(empty, dollar) ||
                coll.equals(empty, zero) ||
                coll.compare(dot, degree) >= 0) {
            errln("coll.setMaxVariable(punctuation) did not work");
        }

        coll.setMaxVariable(Collator.ReorderCodes.SYMBOL);
        max = coll.getMaxVariable();
        logln(String.format("coll.setMaxVariable(symbol) -> %04x", max));
        if(max != Collator.ReorderCodes.SYMBOL ||
                !coll.equals(empty, space) ||
                !coll.equals(empty, dot) ||
                !coll.equals(empty, degree) ||
                coll.equals(empty, dollar) ||
                coll.equals(empty, zero) ||
                coll.compare(degree, dollar) >= 0) {
            errln("coll.setMaxVariable(symbol) did not work");
        }

        coll.setMaxVariable(Collator.ReorderCodes.CURRENCY);
        max = coll.getMaxVariable();
        logln(String.format("coll.setMaxVariable(currency) -> %04x", max));
        if(max != Collator.ReorderCodes.CURRENCY ||
                !coll.equals(empty, space) ||
                !coll.equals(empty, dot) ||
                !coll.equals(empty, degree) ||
                !coll.equals(empty, dollar) ||
                coll.equals(empty, zero) ||
                coll.compare(dollar, zero) >= 0) {
            errln("coll.setMaxVariable(currency) did not work");
        }

        logln("Test restoring maxVariable");
        coll.setMaxVariable(oldMax);
        if(oldMax != coll.getMaxVariable()) {
            errln("Couldn't restore old maxVariable");
        }
    }

    @Test
    public void TestUCARules()
    {
        try {
            // only root locale can have empty tailorings .. not English!
            RuleBasedCollator coll
                = (RuleBasedCollator)Collator.getInstance(new Locale("","",""));
            String rule
                = coll.getRules(false);
            if (!rule.equals("")) {
                errln("Empty rule string should have empty rules " + rule);
            }
            rule = coll.getRules(true);
            if (rule.equals("")) {
                errln("UCA rule string should not be empty");
            }
            coll = new RuleBasedCollator(rule);
        } catch (Exception e) {
            // Android patch: Add --omitCollationRules to genrb.
            logln(e.getMessage());
            // Android patch end.
        }
    }

    /**
     * Jitterbug 2726
     */
    @Test
    public void TestShifted()
    {
        RuleBasedCollator collator = (RuleBasedCollator) Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
        collator.setAlternateHandlingShifted(true);
        CollationTest.doTest(this, collator, " a", "a", 0); // works properly
        CollationTest.doTest(this, collator, "a", "a ", 0); // inconsistent results
    }

    /**
     * Test for CollationElementIterator previous and next for the whole set of
     * unicode characters with normalization on.
     */
    @Test
    public void TestNumericCollation()
    {
        String basicTestStrings[] = {"hello1", "hello2", "hello123456"};
        String preZeroTestStrings[] = {"avery1",
                                       "avery01",
                                       "avery001",
                                       "avery0001"};
        String thirtyTwoBitNumericStrings[] = {"avery42949672960",
                                               "avery42949672961",
                                               "avery42949672962",
                                               "avery429496729610"};

        String supplementaryDigits[] = {"\uD835\uDFCE", // 0
                                        "\uD835\uDFCF", // 1
                                        "\uD835\uDFD0", // 2
                                        "\uD835\uDFD1", // 3
                                        "\uD835\uDFCF\uD835\uDFCE", // 10
                                        "\uD835\uDFCF\uD835\uDFCF", // 11
                                        "\uD835\uDFCF\uD835\uDFD0", // 12
                                        "\uD835\uDFD0\uD835\uDFCE", // 20
                                        "\uD835\uDFD0\uD835\uDFCF", // 21
                                        "\uD835\uDFD0\uD835\uDFD0" // 22
        };

        String foreignDigits[] = {"\u0661",
                                  "\u0662",
                                  "\u0663",
                                  "\u0661\u0660",
                                  "\u0661\u0662",
                                  "\u0661\u0663",
                                  "\u0662\u0660",
                                  "\u0662\u0662",
                                  "\u0662\u0663",
                                  "\u0663\u0660",
                                  "\u0663\u0662",
                                  "\u0663\u0663"
        };

        //Additional tests to cover bug reported in #9476
        String lastDigitDifferent[]={"2004","2005",
                                     "110005", "110006",
                                     "11005", "11006",
                                     "100000000005","100000000006"};

        // Open our collator.
        RuleBasedCollator coll
            = (RuleBasedCollator)Collator.getInstance(Locale.ENGLISH);
        String att[] = {"NumericCollation"};
        Boolean val[] = {Boolean.TRUE};
        genericLocaleStarterWithOptions(Locale.ENGLISH, basicTestStrings, att,
                                        val);
        genericLocaleStarterWithOptions(Locale.ENGLISH,
                                        thirtyTwoBitNumericStrings, att, val);
        genericLocaleStarterWithOptions(Locale.ENGLISH, foreignDigits, att,
                                        val);
        genericLocaleStarterWithOptions(Locale.ENGLISH, supplementaryDigits,
                                        att, val);

        // Setting up our collator to do digits.
        coll.setNumericCollation(true);

        // Testing that prepended zeroes still yield the correct collation
        // behavior.
        // We expect that every element in our strings array will be equal.
        for (int i = 0; i < preZeroTestStrings.length - 1; i ++) {
            for (int j = i + 1; j < preZeroTestStrings.length; j ++) {
                CollationTest.doTest(this, coll, preZeroTestStrings[i],
                                     preZeroTestStrings[j],0);
            }
        }

        //Testing that the behavior reported in #9476 is fixed
        //We expect comparisons between adjacent pairs will result in -1
        for (int i=0; i < lastDigitDifferent.length -1; i=i+2 ) {
            CollationTest.doTest(this, coll, lastDigitDifferent[i], lastDigitDifferent[i+1], -1);
        }


        //cover setNumericCollationDefault, getNumericCollation
        assertTrue("The Numeric Collation setting is on", coll.getNumericCollation());
        coll.setNumericCollationDefault();
        logln("After set Numeric to default, the setting is: " + coll.getNumericCollation());
    }

    @Test
    public void Test3249()
    {
        String rule = "&x < a &z < a";
        try {
            RuleBasedCollator coll = new RuleBasedCollator(rule);
            if(coll!=null){
                logln("Collator did not throw an exception");
            }
        } catch (Exception e) {
            warnln("Error creating RuleBasedCollator with " + rule + " failed");
        }
    }

    @Test
    public void TestTibetanConformance()
    {
        String test[] = {"\u0FB2\u0591\u0F71\u0061", "\u0FB2\u0F71\u0061"};
        try {
            Collator coll = Collator.getInstance();
            coll.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
            if (coll.compare(test[0], test[1]) != 0) {
                errln("Tibetan comparison error");
            }
            CollationTest.doTest(this, (RuleBasedCollator)coll,
                                 test[0], test[1], 0);
        } catch (Exception e) {
            warnln("Error creating UCA collator");
        }
    }

    @Test
    public void TestJ3347()
    {
        try {
            Collator coll = Collator.getInstance(Locale.FRENCH);
            ((RuleBasedCollator)coll).setAlternateHandlingShifted(true);
            if (coll.compare("6", "!6") != 0) {
                errln("Jitterbug 3347 failed");
            }
        } catch (Exception e) {
            warnln("Error creating UCA collator");
        }
    }

    @Test
    public void TestPinyinProblem()
    {
        String test[] = { "\u4E56\u4E56\u7761", "\u4E56\u5B69\u5B50" };
        genericLocaleStarter(new Locale("zh", "", "PINYIN"), test);
    }

    /* supercedes TestJ784 */
    @Test
    public void TestBeforePinyin() {
        String rules =
            "&[before 2]A << \u0101  <<< \u0100 << \u00E1 <<< \u00C1 << \u01CE <<< \u01CD << \u00E0 <<< \u00C0" +
            "&[before 2]e << \u0113 <<< \u0112 << \u00E9 <<< \u00C9 << \u011B <<< \u011A << \u00E8 <<< \u00C8" +
            "&[before 2] i << \u012B <<< \u012A << \u00ED <<< \u00CD << \u01D0 <<< \u01CF << \u00EC <<< \u00CC" +
            "&[before 2] o << \u014D <<< \u014C << \u00F3 <<< \u00D3 << \u01D2 <<< \u01D1 << \u00F2 <<< \u00D2" +
            "&[before 2]u << \u016B <<< \u016A << \u00FA <<< \u00DA << \u01D4 <<< \u01D3 << \u00F9 <<< \u00D9" +
            "&U << \u01D6 <<< \u01D5 << \u01D8 <<< \u01D7 << \u01DA <<< \u01D9 << \u01DC <<< \u01DB << \u00FC";

        String test[] = {
            "l\u0101",
            "la",
            "l\u0101n",
            "lan ",
            "l\u0113",
            "le",
            "l\u0113n",
            "len"
        };

        String test2[] = {
            "x\u0101",
            "x\u0100",
            "X\u0101",
            "X\u0100",
            "x\u00E1",
            "x\u00C1",
            "X\u00E1",
            "X\u00C1",
            "x\u01CE",
            "x\u01CD",
            "X\u01CE",
            "X\u01CD",
            "x\u00E0",
            "x\u00C0",
            "X\u00E0",
            "X\u00C0",
            "xa",
            "xA",
            "Xa",
            "XA",
            "x\u0101x",
            "x\u0100x",
            "x\u00E1x",
            "x\u00C1x",
            "x\u01CEx",
            "x\u01CDx",
            "x\u00E0x",
            "x\u00C0x",
            "xax",
            "xAx"
        };
        /* TODO: port builder fixes to before */
        genericRulesStarter(rules, test);
        genericLocaleStarter(new Locale("zh","",""), test);
        genericRulesStarter(rules, test2);
        genericLocaleStarter(new Locale("zh","",""), test2);
    }

    @Test
    public void TestUpperFirstQuaternary()
    {
      String tests[] = { "B", "b", "Bb", "bB" };
      String[] att = { "strength", "UpperFirst" };
      Object attVals[] = { new Integer(Collator.QUATERNARY), Boolean.TRUE };
      genericLocaleStarterWithOptions(new Locale("root","",""), tests, att, attVals);
    }

    @Test
    public void TestJ4960()
    {
        String tests[] = { "\\u00e2T", "aT" };
        String att[] = { "strength", "CaseLevel" };
        Object attVals[] = { new Integer(Collator.PRIMARY), Boolean.TRUE };
        String tests2[] = { "a", "A" };
        String rule = "&[first tertiary ignorable]=A=a";
        String att2[] = { "CaseLevel" };
        Object attVals2[] = { Boolean.TRUE };
        // Test whether we correctly ignore primary ignorables on case level when
        // we have only primary & case level
        genericLocaleStarterWithOptionsAndResult(new Locale("root", ""), tests, att, attVals, 0);
        // Test whether ICU4J will make case level for sortkeys that have primary strength
        // and case level
        genericLocaleStarterWithOptions(new Locale("root", ""), tests2, att, attVals);
        // Test whether completely ignorable letters have case level info (they shouldn't)
        genericRulesStarterWithOptionsAndResult(rule, tests2, att2, attVals2, 0);
    }

    @Test
    public void TestJB5298(){
        ULocale[] locales = Collator.getAvailableULocales();
        logln("Number of collator locales returned : " + locales.length);
        // double-check keywords
        String[] keywords = Collator.getKeywords();
        if (keywords.length != 1 || !keywords[0].equals("collation")) {
            throw new IllegalArgumentException("internal collation error");
        }

        String[] values = Collator.getKeywordValues("collation");
        log("Collator.getKeywordValues returned: ");
        for(int i=0; i<values.length;i++){
            log(values[i]+", ");
        }
        logln("");
        logln("Number of collation keyword values returned : " + values.length);
        for(int i=0; i<values.length;i++){
            if (values[i].startsWith("private-")) {
                errln("Collator.getKeywordValues() returns private collation keyword: " + values[i]);
            }
        }

        Set foundValues = new TreeSet(Arrays.asList(values));

        for (int i = 0; i < locales.length; ++i) {
          for (int j = 0; j < values.length; ++j) {
            ULocale tryLocale = values[j].equals("standard")
            ? locales[i] : new ULocale(locales[i] + "@collation=" + values[j]);
            // only append if not standard
            ULocale canon = Collator.getFunctionalEquivalent("collation",tryLocale);
            if (!canon.equals(tryLocale)) {
                continue; // has a different
            }else {// functional equivalent, so skip
                logln(tryLocale + " : "+canon+", ");
            }
            String can = canon.toString();
            int idx = can.indexOf("@collation=");
            String val = idx >= 0 ? can.substring(idx+11, can.length()) : "";
            if(val.length()>0 && !foundValues.contains(val)){
                errln("Unknown collation found "+ can);
            }
          }
        }
        logln(" ");
    }

    public void
    TestJ5367()
    {
        String[] test = { "a", "y" };
        String rules = "&Ny << Y &[first secondary ignorable] <<< a";
        genericRulesStarter(rules, test);
    }

    public void
    TestVI5913()
    {

        String rules[] = {
                "&a < \u00e2 <<< \u00c2",
                "&a < \u1FF3 ",  // OMEGA WITH YPOGEGRAMMENI
                "&s < \u0161 ",  // &s < s with caron
                /*
                 * Note: Just tailoring &z<ae^ does not work as expected:
                 * The UCA spec requires for discontiguous contractions that they
                 * extend an *existing match* by one combining mark at a time.
                 * Therefore, ae must be a contraction so that the builder finds
                 * discontiguous contractions for ae^, for example with an intervening underdot.
                 * Only then do we get the expected tail closure with a\u1EC7, a\u1EB9\u0302, etc.
                 */
                "&x < ae &z < a\u00EA",  // &x < ae &z < a+e with circumflex
        };
        String cases[][] = {
            { "\u1EAC", "A\u0323\u0302", "\u1EA0\u0302", "\u00C2\u0323", },
            { "\u1FA2", "\u03C9\u0313\u0300\u0345", "\u1FF3\u0313\u0300",
              "\u1F60\u0300\u0345", "\u1f62\u0345", "\u1FA0\u0300", },
            { "\u1E63\u030C", "s\u0323\u030C", "s\u030C\u0323"},
            { "a\u1EC7", //  a+ e with dot below and circumflex
              "a\u1EB9\u0302", // a + e with dot below + combining circumflex
              "a\u00EA\u0323", // a + e with circumflex + combining dot below
            }
        };


        for(int i = 0; i < rules.length; i++) {

            RuleBasedCollator coll = null;
            try {
                coll = new RuleBasedCollator(rules[i]);
            } catch (Exception e) {
                warnln("Unable to open collator with rules " + rules[i]);
            }

            logln("Test case["+i+"]:");
            CollationKey expectingKey = coll.getCollationKey(cases[i][0]);
            for (int j=1; j<cases[i].length; j++) {
                CollationKey key = coll.getCollationKey(cases[i][j]);
                if ( key.compareTo(expectingKey)!=0) {
                    errln("Error! Test case["+i+"]:"+"source:" + key.getSourceString());
                    errln("expecting:"+CollationTest.prettify(expectingKey)+ "got:"+  CollationTest.prettify(key));
                }
                logln("   Key:"+  CollationTest.prettify(key));
            }
        }


        RuleBasedCollator vi_vi = null;
        try {
            vi_vi = (RuleBasedCollator)Collator.getInstance(
                                                      new Locale("vi", ""));
            logln("VI sort:");
            CollationKey expectingKey = vi_vi.getCollationKey(cases[0][0]);
            for (int j=1; j<cases[0].length; j++) {
                CollationKey key = vi_vi.getCollationKey(cases[0][j]);
                if ( key.compareTo(expectingKey)!=0) {
                    // TODO (claireho): change the logln to errln after vi.res is up-to-date.
                    // errln("source:" + key.getSourceString());
                    // errln("expecting:"+prettify(expectingKey)+ "got:"+  prettify(key));
                    logln("Error!! in Vietnese sort - source:" + key.getSourceString());
                    logln("expecting:"+CollationTest.prettify(expectingKey)+ "got:"+  CollationTest.prettify(key));
                }
                // logln("source:" + key.getSourceString());
                logln("   Key:"+  CollationTest.prettify(key));
            }
        } catch (Exception e) {
            warnln("Error creating Vietnese collator");
            return;
        }

    }


    @Test
    public void Test6179()
    {
        String rules[] = {
                "&[last primary ignorable]<< a  &[first primary ignorable]<<b ",
                "&[last secondary ignorable]<<< a &[first secondary ignorable]<<<b",
        };
        // defined in UCA5.1
        String firstPrimIgn = "\u0332";
        String lastPrimIgn = "\uD800\uDDFD";
        String firstVariable = "\u0009";
        byte[] secIgnKey = {1,1,4,0};

        int i=0;
        {

            RuleBasedCollator coll = null;
            try {
                coll = new RuleBasedCollator(rules[i]);
            } catch (Exception e) {
                warnln("Unable to open collator with rules " + rules[i] + ": " + e);
                return;
            }

            logln("Test rule["+i+"]"+rules[i]);

            CollationKey keyA = coll.getCollationKey("a");
            logln("Key for \"a\":"+  CollationTest.prettify(keyA));
            if (keyA.compareTo(coll.getCollationKey(lastPrimIgn))<=0) {
                CollationKey key = coll.getCollationKey(lastPrimIgn);
                logln("Collation key for 0xD800 0xDDFD: "+CollationTest.prettify(key));
                errln("Error! String \"a\" must be greater than \uD800\uDDFD -"+
                      "[Last Primary Ignorable]");
            }
            if (keyA.compareTo(coll.getCollationKey(firstVariable))>=0) {
                CollationKey key = coll.getCollationKey(firstVariable);
                logln("Collation key for 0x0009: "+CollationTest.prettify(key));
                errln("Error! String \"a\" must be less than 0x0009 - [First Variable]");
            }
            CollationKey keyB = coll.getCollationKey("b");
            logln("Key for \"b\":"+  CollationTest.prettify(keyB));
            if (keyB.compareTo(coll.getCollationKey(firstPrimIgn))<=0) {
                CollationKey key = coll.getCollationKey(firstPrimIgn);
                logln("Collation key for 0x0332: "+CollationTest.prettify(key));
                errln("Error! String \"b\" must be greater than 0x0332 -"+
                      "[First Primary Ignorable]");
            }
            if (keyB.compareTo(coll.getCollationKey(firstVariable))>=0) {
                CollationKey key = coll.getCollationKey(firstVariable);
                logln("Collation key for 0x0009: "+CollationTest.prettify(key));
                errln("Error! String \"b\" must be less than 0x0009 - [First Variable]");
            }
        }
        {
            i=1;
            RuleBasedCollator coll = null;
            try {
                coll = new RuleBasedCollator(rules[i]);
            } catch (Exception e) {
                warnln("Unable to open collator with rules " + rules[i]);
            }

            logln("Test rule["+i+"]"+rules[i]);

            CollationKey keyA = coll.getCollationKey("a");
            logln("Key for \"a\":"+  CollationTest.prettify(keyA));
            byte[] keyAInBytes = keyA.toByteArray();
            for (int j=0; j<keyAInBytes.length && j<secIgnKey.length; j++) {
                if (keyAInBytes[j]!=secIgnKey[j]) {
                    if ((char)keyAInBytes[j]<=(char)secIgnKey[j]) {
                        logln("Error! String \"a\" must be greater than [Last Secondary Ignorable]");
                    }
                    break;
                }
            }
            if (keyA.compareTo(coll.getCollationKey(firstVariable))>=0) {
                errln("Error! String \"a\" must be less than 0x0009 - [First Variable]");
                CollationKey key = coll.getCollationKey(firstVariable);
                logln("Collation key for 0x0009: "+CollationTest.prettify(key));
            }
            CollationKey keyB = coll.getCollationKey("b");
            logln("Key for \"b\":"+  CollationTest.prettify(keyB));
            byte[] keyBInBytes = keyB.toByteArray();
            for (int j=0; j<keyBInBytes.length && j<secIgnKey.length; j++) {
                if (keyBInBytes[j]!=secIgnKey[j]) {
                    if ((char)keyBInBytes[j]<=(char)secIgnKey[j]) {
                        errln("Error! String \"b\" must be greater than [Last Secondary Ignorable]");
                    }
                    break;
                }
            }
            if (keyB.compareTo(coll.getCollationKey(firstVariable))>=0) {
                CollationKey key = coll.getCollationKey(firstVariable);
                logln("Collation key for 0x0009: "+CollationTest.prettify(key));
                errln("Error! String \"b\" must be less than 0x0009 - [First Variable]");
            }
        }
    }

    @Test
    public void TestUCAPrecontext()
    {
        String rules[] = {
                "& \u00B7<a ",
                "& L\u00B7 << a", // 'a' is an expansion.
        };
        String cases[] = {
            "\u00B7",
            "\u0387",
            "a",
            "l",
            "L\u0332",
            "l\u00B7",
            "l\u0387",
            "L\u0387",
            "la\u0387",
            "La\u00b7",
        };

        // Test en sort
        RuleBasedCollator en = null;

        logln("EN sort:");
        try {
            en = (RuleBasedCollator)Collator.getInstance(
                    new Locale("en", ""));
            for (int j=0; j<cases.length; j++) {
                CollationKey key = en.getCollationKey(cases[j]);
                if (j>0) {
                    CollationKey prevKey = en.getCollationKey(cases[j-1]);
                    if (key.compareTo(prevKey)<0) {
                        errln("Error! EN test["+j+"]:source:" + cases[j]+
                        " is not >= previous test string.");
                    }
                }
                /*
                if ( key.compareTo(expectingKey)!=0) {
                    errln("Error! Test case["+i+"]:"+"source:" + key.getSourceString());
                    errln("expecting:"+prettify(expectingKey)+ "got:"+  prettify(key));
                }
                */
                logln("String:"+cases[j]+"   Key:"+  CollationTest.prettify(key));
            }
        } catch (Exception e) {
            warnln("Error creating English collator");
            return;
        }

        // Test ja sort
        RuleBasedCollator ja = null;
        logln("JA sort:");
        try {
            ja = (RuleBasedCollator)Collator.getInstance(
                    new Locale("ja", ""));
            for (int j=0; j<cases.length; j++) {
                CollationKey key = ja.getCollationKey(cases[j]);
                if (j>0) {
                    CollationKey prevKey = ja.getCollationKey(cases[j-1]);
                    if (key.compareTo(prevKey)<0) {
                        errln("Error! JA test["+j+"]:source:" + cases[j]+
                        " is not >= previous test string.");
                    }
                }
                logln("String:"+cases[j]+"   Key:"+  CollationTest.prettify(key));
            }
        } catch (Exception e) {
            warnln("Error creating Japanese collator");
            return;
        }
        for(int i = 0; i < rules.length; i++) {

            RuleBasedCollator coll = null;
            logln("Tailoring rule:"+rules[i]);
            try {
                coll = new RuleBasedCollator(rules[i]);
            } catch (Exception e) {
                warnln("Unable to open collator with rules " + rules[i]);
                continue;
            }

            for (int j=0; j<cases.length; j++) {
                CollationKey key = coll.getCollationKey(cases[j]);
                if (j>0) {
                    CollationKey prevKey = coll.getCollationKey(cases[j-1]);
                    if (i==1 && j==3) {
                        if (key.compareTo(prevKey)>0) {
                            errln("Error! Rule:"+rules[i]+" test["+j+"]:source:"+
                            cases[j]+" is not <= previous test string.");
                        }
                    }
                    else {
                        if (key.compareTo(prevKey)<0) {
                            errln("Error! Rule:"+rules[i]+" test["+j+"]:source:"+
                            cases[j]+" is not >= previous test string.");
                        }
                    }
                }
                logln("String:"+cases[j]+"   Key:"+  CollationTest.prettify(key));
            }
        }
    }


    /**
     * Stores a test case for collation testing.
     */
    private class OneTestCase {
        /** The first value to compare.  **/
        public String m_source_;

        /** The second value to compare. **/
        public String m_target_;

        /**
         *  0 if the two values sort equal,
         * -1 if the first value sorts before the second
         *  1 if the first value sorts after the first
         */
        public int m_result_;

        public OneTestCase(String source, String target, int result) {
            m_source_ = source;
            m_target_ = target;
            m_result_ = result;
        }
    }

    /**
     * Convenient function to test collation rules.
     * @param testCases
     * @param rules Collation rules in ICU format.  All the strings in this
     *     array represent the same rule, expressed in different forms.
     */
    private void doTestCollation(
        OneTestCase[] testCases, String[] rules) {

        Collator  myCollation;
        for (String rule : rules) {
            try {
                myCollation = new RuleBasedCollator(rule);
            } catch (Exception e) {
                warnln("ERROR: in creation of rule based collator: " + e);
                return;
            }

            myCollation.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
            myCollation.setStrength(Collator.TERTIARY);
            for (OneTestCase testCase : testCases) {
                CollationTest.doTest(this, (RuleBasedCollator)myCollation,
                                     testCase.m_source_,
                                     testCase.m_target_,
                                     testCase.m_result_);
            }
        }
    }

     // Test cases to check whether the rules equivalent to
     // "&a<b<c<d &b<<k<<l<<m &k<<<x<<<y<<<z &a=1=2=3" are working fine.
    private OneTestCase[] m_rangeTestCases_ = {
        //               Left                  Right             Result
        new OneTestCase( "\u0061",             "\u0062",             -1 ),  // "a" < "b"
        new OneTestCase( "\u0062",             "\u0063",             -1 ),  // "b" < "c"
        new OneTestCase( "\u0061",             "\u0063",             -1 ),  // "a" < "c"

        new OneTestCase( "\u0062",             "\u006b",             -1 ),  // "b" << "k"
        new OneTestCase( "\u006b",             "\u006c",             -1 ),  // "k" << "l"
        new OneTestCase( "\u0062",             "\u006c",             -1 ),  // "b" << "l"
        new OneTestCase( "\u0061",             "\u006c",             -1 ),  // "a" << "l"
        new OneTestCase( "\u0061",             "\u006d",             -1 ),  // "a" << "m"

        new OneTestCase( "\u0079",             "\u006d",             -1 ),  // "y" < "f"
        new OneTestCase( "\u0079",             "\u0067",             -1 ),  // "y" < "g"
        new OneTestCase( "\u0061",             "\u0068",             -1 ),  // "y" < "h"
        new OneTestCase( "\u0061",             "\u0065",             -1 ),  // "g" < "e"

        new OneTestCase( "\u0061",             "\u0031",              0 ),   // "a" == "1"
        new OneTestCase( "\u0061",             "\u0032",              0 ),   // "a" == "2"
        new OneTestCase( "\u0061",             "\u0033",              0 ),   // "a" == "3"
        new OneTestCase( "\u0061",             "\u0066",             -1 ),   // "a" < "f",
        new OneTestCase( "\u006c\u0061",       "\u006b\u0062",       -1 ),  // "la" < "kb"
        new OneTestCase( "\u0061\u0061\u0061", "\u0031\u0032\u0033",  0 ),  // "aaa" == "123"
        new OneTestCase( "\u0062",             "\u007a",             -1 ),  // "b" < "z"
        new OneTestCase( "\u0061\u007a\u0062", "\u0032\u0079\u006d", -1 ),  // "azm" < "2yc"
    };

     // Test cases to check whether the rules equivalent to
     // "&\ufffe<\uffff<\U00010000<\U00010001<\U00010002
     //  &\U00010000<<\U00020001<<\U00020002<<\U00020002
     //  &\U00020001=\U0003001=\U0004001=\U0004002
     //  &\U00040008<\U00030008<\UU00020008"
     // are working fine.
    private OneTestCase[] m_rangeTestCasesSupplemental_ = {
        //               Left                Right               Result
        new OneTestCase( "\u4e00",           "\ufffb",             -1 ),
        new OneTestCase( "\ufffb",           "\ud800\udc00",       -1 ),  // U+FFFB < U+10000
        new OneTestCase( "\ud800\udc00",    "\ud800\udc01",        -1 ),  // U+10000 < U+10001

        new OneTestCase( "\u4e00",           "\ud800\udc01",       -1 ),  // U+4E00 < U+10001
        new OneTestCase( "\ud800\udc01",    "\ud800\udc02",        -1 ),  // U+10001 < U+10002
        new OneTestCase( "\ud800\udc00",    "\ud840\udc02",        -1 ),  // U+10000 < U+10002
        new OneTestCase( "\u4e00",           "\u0d840\udc02",      -1 ),  // U+4E00 < U+10002

    };

    // Test cases in disjoint random code points.  To test only the compact syntax.
    // Rule:  &q<w<e<r &w<<t<<y<<u &t<<<i<<<o<<<p &o=a=s=d
    private OneTestCase[] m_qwertCollationTestCases_ = {
        new OneTestCase("q", "w" , -1),
        new OneTestCase("w", "e" , -1),

        new OneTestCase("y", "u" , -1),
        new OneTestCase("q", "u" , -1),

        new OneTestCase("t", "i" , -1),
        new OneTestCase("o", "p" , -1),

        new OneTestCase("y", "e" , -1),
        new OneTestCase("i", "u" , -1),

        new OneTestCase("quest", "were" , -1),
        new OneTestCase("quack", "quest", -1)
    };

    // Tests the compact list with ASCII codepoints.
    @Test
    public void TestSameStrengthList() {
        String[] rules = new String[] {
            // Normal
            "&a<b<c<d &b<<k<<l<<m &k<<<x<<<y<<<z &y<f<g<h<e &a=1=2=3",

            // Lists
            "&a<*bcd &b<<*klm &k<<<*xyz &y<*fghe &a=*123",

            // Lists with quoted characters
            "&'\u0061'<*bcd &b<<*klm &k<<<*xyz &y<*f'\u0067\u0068'e &a=*123",
        };
        doTestCollation(m_rangeTestCases_, rules);
    }

    @Test
    public void TestSameStrengthListQuoted() {
        String[] rules = new String[] {
            "&'\u0061'<*bcd &b<<*klm &k<<<*xyz &y<*f'\u0067\u0068'e &a=1=2=3",
            "&'\u0061'<*b'\u0063'd &b<<*klm &k<<<*xyz &'\u0079'<*fgh'\u0065' " +
            "&a=*'\u0031\u0032\u0033'",

            "&'\u0061'<*'\u0062'c'\u0064' &b<<*klm &k<<<*xyz  &y<*fghe " +
            "&a=*'\u0031\u0032\u0033'",
        };
        doTestCollation(m_rangeTestCases_, rules);
    }

    // Tests the compact list with ASCII codepoints in non-codepoint order.
    @Test
    public void TestSameStrengthListQwerty() {
        String[] rules = new String[] {
            "&q<w<e<r &w<<t<<y<<u &t<<<i<<<o<<<p &o=a=s=d",   // Normal
            "&q<*wer &w<<*tyu &t<<<*iop &o=*asd",             // Lists
        };

        doTestCollation(m_qwertCollationTestCases_, rules);
    }

    // Tests the compact list with supplemental codepoints.
    @Test
    public void TestSameStrengthListWithSupplementalCharacters() {
        String[] rules = new String[] {
            // ** Rule without compact list syntax **
            // \u4e00 < \ufffb < \U00010000    < \U00010001  < \U00010002
            "&\u4e00<\ufffb<'\ud800\udc00'<'\ud800\udc01'<'\ud800\udc02' " +
            // \U00010000    << \U00020001   << \U00020002       \U00020002
            "&'\ud800\udc00'<<'\ud840\udc01'<<'\ud840\udc02'<<'\ud840\udc02'  " +
            // \U00020001   = \U0003001    = \U0004001    = \U0004002
            "&'\ud840\udc01'='\ud880\udc01'='\ud8c0\udc01'='\ud8c0\udc02'",

            // ** Rule with compact list syntax **
            // \u4e00 <* \ufffb\U00010000  \U00010001
            "&\u4e00<*'\ufffb\ud800\udc00\ud800\udc01\ud800\udc02' " +
            // \U00010000   <<* \U00020001  \U00020002
            "&'\ud800\udc00'<<*'\ud840\udc01\ud840\udc02\ud840\udc03'  " +
            // \U00020001   =* \U0003001   \U0003002   \U0003003   \U0004001
            "&'\ud840\udc01'=*'\ud880\udc01\ud880\udc02\ud880\udc03\ud8c0\udc01' "

        };
        doTestCollation(m_rangeTestCasesSupplemental_, rules);
    }


    // Tests the compact range syntax with ASCII codepoints.
    @Test
    public void TestSameStrengthListRanges() {
        String[] rules = new String[] {
            // Ranges
            "&a<*b-d &b<<*k-m &k<<<*x-z &y<*f-he &a=*1-3",

            // Ranges with quoted characters
            "&'\u0061'<*'\u0062'-'\u0064' &b<<*klm &k<<<*xyz " +
            "&'\u0079'<*'\u0066'-'\u0068e' &a=*123",
            "&'\u0061'<*'\u0062'-'\u0064' " +
            "&b<<*'\u006B'-m &k<<<*x-'\u007a' " +
            "&'\u0079'<*'\u0066'-h'\u0065' &a=*'\u0031\u0032\u0033'",
        };

        doTestCollation(m_rangeTestCases_, rules);
    }

    // Tests the compact range syntax with supplemental codepoints.
    @Test
    public void TestSameStrengthListRangesWithSupplementalCharacters() {
        String[] rules = new String[] {
            // \u4e00 <* \ufffb\U00010000  \U00010001
            "&\u4e00<*'\ufffb'\ud800\udc00-'\ud800\udc02' " +
            // \U00010000   <<* \U00020001   - \U00020003
            "&'\ud800\udc00'<<*'\ud840\udc01'-'\ud840\udc03'  " +
            // \U00020001   =* \U0003001   \U0004001
            "&'\ud840\udc01'=*'\ud880\udc01'-'\ud880\udc03\ud8c0\udc01' "
        };
        doTestCollation(m_rangeTestCasesSupplemental_, rules);
    }

    // Tests the compact range syntax with special characters used as syntax characters in rules.
    @Test
    public void TestSpecialCharacters() {
        String rules[] = new String[] {
                // Normal
                "&';'<'+'<','<'-'<'&'<'*'",

                // List
                "&';'<*'+,-&*'",

                // Range
                "&';'<*'+'-'-&*'",

                "&'\u003b'<'\u002b'<'\u002c'<'\u002d'<'\u0026'<'\u002a'",

                "&'\u003b'<*'\u002b\u002c\u002d\u0026\u002a'",
                "&'\u003b'<*'\u002b\u002c\u002d\u0026\u002a'",
                "&'\u003b'<*'\u002b'-'\u002d\u0026\u002a'",
                "&'\u003b'<*'\u002b'-'\u002d\u0026\u002a'",
        };
        OneTestCase[] testCases = new OneTestCase[] {
            new OneTestCase("\u003b", "\u002b", -1), // ; < +
            new OneTestCase("\u002b", "\u002c", -1), // + < ,
            new OneTestCase("\u002c", "\u002d", -1), // , < -
            new OneTestCase("\u002d", "\u0026", -1), // - < &
        };
        doTestCollation(testCases, rules);
    }

    @Test
    public void TestInvalidListsAndRanges() {
        String[] invalidRules = new String[] {
            // Range not in starred expression
            "&\u4e00<\ufffb-'\ud800\udc02'",

            // Range without start
            "&a<*-c",

            // Range without end
            "&a<*b-",

            // More than one hyphen
            "&a<*b-g-l",

            // Range in the wrong order
            "&a<*k-b",
        };
        for (String rule : invalidRules) {
            try {
                Collator myCollation = new RuleBasedCollator(rule);
                warnln("ERROR: Creation of collator didn't fail for " + rule + " when it should.");
                CollationTest.doTest(this, (RuleBasedCollator)myCollation,
                        "x",
                        "y",
                        -1);

           } catch (Exception e) {
                continue;
            }
           throw new IllegalArgumentException("ERROR: Invalid collator with rule " + rule + " worked fine.");
        }
    }

    // This is the same example above with ' and space added.
    // They work a little different than expected.  Desired rules are commented out.
    @Test
    public void TestQuoteAndSpace() {
        String rules[] = new String[] {
                // These are working as expected.
                "&';'<'+'<','<'-'<'&'<''<'*'<' '",

                // List.  Desired rule is
                // "&';'<*'+,-&''* '",
                // but it doesn't work.  Instead, '' should be outside quotes as below.
                "&';'<*'+,-&''''* '",

                // Range.  Similar issues here as well.  The following are working.
                //"&';'<*'+'-'-&''* '",
                //"&';'<*'+'-'-&'\\u0027'* '",
                "&';'<*'+'-'-&''''* '",
                //"&';'<*'+'-'-&'\\u0027'* '",

                // The following rules are not working.
                // "&';'<'+'<','<'-'<'&'<\\u0027<'*'<' '",
                //"&'\u003b'<'\u002b'<'\u002c'<'\u002d'<'\u0026'<'\u0027'<\u002a'<'\u0020'",
                //"&'\u003b'<'\u002b'<'\u002c'<'\u002d'<'\u0026'<\\u0027<\u002a'<'\u0020'",
        };

        OneTestCase[] testCases = new OneTestCase[] {
            new OneTestCase("\u003b", "\u002b", -1), // ; < ,
            new OneTestCase("\u002b", "\u002c", -1), // ; < ,
            new OneTestCase("\u002c", "\u002d", -1), // , < -
            new OneTestCase("\u002d", "\u0026", -1), // - < &
            new OneTestCase("\u0026", "\u0027", -1), // & < '
            new OneTestCase("\u0027", "\u002a", -1), // ' < *
            // new OneTestCase("\u002a", "\u0020", -1), // * < <space>
        };
        doTestCollation(testCases, rules);
    }

    /*
     * Tests the method public boolean equals(Object target) in CollationKey
     */
    @Test
    public void TestCollationKeyEquals() {
        CollationKey ck = new CollationKey("", (byte[]) null);

        // Tests when "if (!(target instanceof CollationKey))" is true
        if (ck.equals(new Object())) {
            errln("CollationKey.equals() was not suppose to return false "
                    + "since it is comparing to a non Collation Key object.");
        }
        if (ck.equals("")) {
            errln("CollationKey.equals() was not suppose to return false "
                    + "since it is comparing to a non Collation Key object.");
        }
        if (ck.equals(0)) {
            errln("CollationKey.equals() was not suppose to return false "
                    + "since it is comparing to a non Collation Key object.");
        }
        if (ck.equals(0.0)) {
            errln("CollationKey.equals() was not suppose to return false "
                    + "since it is comparing to a non Collation Key object.");
        }

        // Tests when "if (target == null)" is true
        if (ck.equals((CollationKey) null)) {
            errln("CollationKey.equals() was not suppose to return false "
                    + "since it is comparing to a null Collation Key object.");
        }
    }

    /*
     * Tests the method public int hashCode() in CollationKey
     */
    @Test
    public void TestCollationKeyHashCode() {
        CollationKey ck = new CollationKey("", (byte[]) null);

        // Tests when "if (m_key_ == null)" is true
        if (ck.hashCode() != 1) {
            errln("CollationKey.hashCode() was suppose to return 1 "
                    + "when m_key is null due a null parameter in the " + "constructor.");
        }
    }

    /*
     * Tests the method public CollationKey getBound(int boundType, int noOfLevels)
     */
    @Test
    public void TestGetBound() {
        CollationKey ck = new CollationKey("", (byte[]) null);

        // Tests when "if (noOfLevels > Collator.PRIMARY)" is false
        // Tests when "default: " is true for "switch (boundType)"
        try {
            ck.getBound(BoundMode.COUNT, -1);
            errln("CollationKey.getBound(int,int) was suppose to return an "
                    + "exception for an invalid boundType value.");
        } catch (Exception e) {
        }

        // Tests when "if (noOfLevels > 0)"
        byte b[] = {};
        CollationKey ck1 = new CollationKey("", b);
        try {
            ck1.getBound(0, 1);
            errln("CollationKey.getBound(int,int) was suppose to return an "
                    + "exception a value of noOfLevels that exceeds expected.");
        } catch (Exception e) {
        }
    }

    /*
     * Tests the method public CollationKey merge(CollationKey source)
     */
    @Test
    public void TestMerge() {
        byte b[] = {};
        CollationKey ck = new CollationKey("", b);

        // Tests when "if (source == null || source.getLength() == 0)" is true
        try {
            ck.merge(null);
            errln("Collationkey.merge(CollationKey) was suppose to return " + "an exception for a null parameter.");
        } catch (Exception e) {
        }
        try {
            ck.merge(ck);
            errln("Collationkey.merge(CollationKey) was suppose to return " + "an exception for a null parameter.");
        } catch (Exception e) {
        }
    }

    /* Test the method public int compareTo(RawCollationKey rhs) */
    @Test
    public void TestRawCollationKeyCompareTo(){
        RawCollationKey rck = new RawCollationKey();
        byte[] b = {(byte) 10, (byte) 20};
        RawCollationKey rck100 = new RawCollationKey(b, 2);

        if(rck.compareTo(rck) != 0){
            errln("RawCollatonKey.compareTo(RawCollationKey) was suppose to return 0 " +
                    "for two idential RawCollationKey objects.");
        }

        if(rck.compareTo(rck100) == 0){
            errln("RawCollatonKey.compareTo(RawCollationKey) was not suppose to return 0 " +
                    "for two different RawCollationKey objects.");
        }
    }

    /* Track7223: CollationElementIterator does not return correct order for Hungarian */
    @Test
    public void TestHungarianTailoring(){
        String rules = new String("&DZ<dzs<<<Dzs<<<DZS" +
                                  "&G<gy<<<Gy<<<GY" +
                                  "&L<ly<<<Ly<<<LY" +
                                  "&N<ny<<<Ny<<<NY" +
                                  "&S<sz<<<Sz<<<SZ" +
                                  "&T<ty<<<Ty<<<TY" +
                                  "&Z<zs<<<Zs<<<ZS" +
                                  "&O<\u00f6<<<\u00d6<<\u0151<<<\u0150" +
                                  "&U<\u00fc<<<\u00dc<<\u0171<<<\u0171" +
                                  "&cs<<<ccs/cs" +
                                  "&Cs<<<Ccs/cs" +
                                  "&CS<<<CCS/CS" +
                                  "&dz<<<ddz/dz" +
                                  "&Dz<<<Ddz/dz" +
                                  "&DZ<<<DDZ/DZ" +
                                  "&dzs<<<ddzs/dzs" +
                                  "&Dzs<<<Ddzs/dzs" +
                                  "&DZS<<<DDZS/DZS" +
                                  "&gy<<<ggy/gy" +
                                  "&Gy<<<Ggy/gy" +
                                  "&GY<<<GGY/GY");
        RuleBasedCollator coll;
        try {
            String str1 = "ggy";
            String str2 = "GGY";
            coll = new RuleBasedCollator(rules);
            if (coll.compare("ggy", "GGY") >= 0) {
                  errln("TestHungarianTailoring.compare(" + str1 + ","+ str2 +
                        ") was suppose to return -1 ");
            }
            CollationKey sortKey1 = coll.getCollationKey(str1);
            CollationKey sortKey2 = coll.getCollationKey(str2);
            if (sortKey1.compareTo(sortKey2) >= 0) {
                  errln("TestHungarianTailoring getCollationKey(\"" + str1 +"\") was suppose "+
                        "less than getCollationKey(\""+ str2 + "\").");
                  errln("  getCollationKey(\"ggy\"):" + CollationTest.prettify(sortKey1) +
                        "  getCollationKey(\"GGY\"):" + CollationTest.prettify(sortKey2));
            }

            CollationElementIterator iter1 = coll.getCollationElementIterator(str1);
            CollationElementIterator iter2 = coll.getCollationElementIterator(str2);
            int ce1, ce2;
            while((ce1 = iter1.next()) != CollationElementIterator.NULLORDER &&
                  (ce2 = iter2.next()) != CollationElementIterator.NULLORDER) {
                if (ce1 > ce2) {
                  errln("TestHungarianTailoring.CollationElementIterator(" + str1 +
                      ","+ str2 + ") was suppose to return -1 ");
                }
            }
          } catch (Exception e) {
              e.printStackTrace();
          }
     }

    @Test
    public void TestImport(){
        try{
            RuleBasedCollator vicoll = (RuleBasedCollator)Collator.getInstance(new ULocale("vi"));
            RuleBasedCollator escoll = (RuleBasedCollator)Collator.getInstance(new ULocale("es"));
            RuleBasedCollator viescoll = new RuleBasedCollator(vicoll.getRules() + escoll.getRules());
            RuleBasedCollator importviescoll = new RuleBasedCollator("[import vi][import es]");

            UnicodeSet tailoredSet = viescoll.getTailoredSet();
            UnicodeSet importTailoredSet = importviescoll.getTailoredSet();

            if(!tailoredSet.equals(importTailoredSet)){
                warnln("Tailored set not equal");
            }

            for (UnicodeSetIterator it = new UnicodeSetIterator(tailoredSet); it.next();) {
                String t = it.getString();
                CollationKey sk1 = viescoll.getCollationKey(t);
                CollationKey sk2 = importviescoll.getCollationKey(t);
                if(!sk1.equals(sk2)){
                    warnln("Collation key's not equal for " + t);
                }
            }

        }catch(Exception e){
            // Android patch: Add --omitCollationRules to genrb.
            logln("ERROR: in creation of rule based collator");
            // Android patch end.
        }
    }

    @Test
    public void TestImportWithType(){
        try{
            RuleBasedCollator vicoll = (RuleBasedCollator)Collator.getInstance(new ULocale("vi"));
            RuleBasedCollator decoll = (RuleBasedCollator)Collator.getInstance(ULocale.forLanguageTag("de-u-co-phonebk"));
            RuleBasedCollator videcoll = new RuleBasedCollator(vicoll.getRules() + decoll.getRules());
            RuleBasedCollator importvidecoll = new RuleBasedCollator("[import vi][import de-u-co-phonebk]");

            UnicodeSet tailoredSet = videcoll.getTailoredSet();
            UnicodeSet importTailoredSet = importvidecoll.getTailoredSet();

            if(!tailoredSet.equals(importTailoredSet)){
                warnln("Tailored set not equal");
            }

            for (UnicodeSetIterator it = new UnicodeSetIterator(tailoredSet); it.next();) {
                String t = it.getString();
                CollationKey sk1 = videcoll.getCollationKey(t);
                CollationKey sk2 = importvidecoll.getCollationKey(t);
                if(!sk1.equals(sk2)){
                    warnln("Collation key's not equal for " + t);
                }
            }

        }catch(Exception e){
            // Android patch: Add --omitCollationRules to genrb.
            logln("ERROR: in creation of rule based collator");
            // Android patch end.
        }
    }

    /*
     * This test ensures that characters placed before a character in a different script have the same lead byte
     * in their collation key before and after script reordering.
     */
    @Test
    public void TestBeforeRuleWithScriptReordering() throws Exception
    {
        /* build collator */
        String rules = "&[before 1]\u03b1 < \u0e01";
        int[] reorderCodes = {UScript.GREEK};
        int result;

        Collator myCollation = new RuleBasedCollator(rules);
        myCollation.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        myCollation.setStrength(Collator.TERTIARY);

        String base = "\u03b1"; /* base */
        String before = "\u0e01"; /* ko kai */

        /* check collation results - before rule applied but not script reordering */
        result = myCollation.compare(base, before);
        if (!(result > 0)) {
            errln("Collation result not correct before script reordering.");
        }

        /* check the lead byte of the collation keys before script reordering */
        CollationKey baseKey = myCollation.getCollationKey(base);
        CollationKey beforeKey = myCollation.getCollationKey(before);
        byte[] baseKeyBytes = baseKey.toByteArray();
        byte[] beforeKeyBytes = beforeKey.toByteArray();
        if (baseKeyBytes[0] != beforeKeyBytes[0]) {
            errln("Different lead byte for sort keys using before rule and before script reordering. base character lead byte = "
                    + baseKeyBytes[0] + ", before character lead byte = " + beforeKeyBytes[0]);
       }

        /* reorder the scripts */
        myCollation.setReorderCodes(reorderCodes);

        /* check collation results - before rule applied and after script reordering */
        result = myCollation.compare(base, before);
        if (!(result > 0)) {
            errln("Collation result not correct after script reordering.");
        }

        /* check the lead byte of the collation keys after script reordering */
        baseKey = myCollation.getCollationKey(base);
        beforeKey = myCollation.getCollationKey(before);
        baseKeyBytes = baseKey.toByteArray();
        beforeKeyBytes = beforeKey.toByteArray();
        if (baseKeyBytes[0] != beforeKeyBytes[0]) {
            errln("Different lead byte for sort keys using before rule and before script reordering. base character lead byte = "
                    + baseKeyBytes[0] + ", before character lead byte = " + beforeKeyBytes[0]);
       }
    }

    /*
     * Test that in a primary-compressed sort key all bytes except the first one are unchanged under script reordering.
     */
    @Test
    public void TestNonLeadBytesDuringCollationReordering() throws Exception
    {
        Collator myCollation;
        byte[] baseKey;
        byte[] reorderKey;
        int[] reorderCodes = {UScript.GREEK};
        String testString = "\u03b1\u03b2\u03b3";

        /* build collator tertiary */
        myCollation = new RuleBasedCollator("");
        myCollation.setStrength(Collator.TERTIARY);
        baseKey = myCollation.getCollationKey(testString).toByteArray();

        myCollation.setReorderCodes(reorderCodes);
        reorderKey = myCollation.getCollationKey(testString).toByteArray();

        if (baseKey.length != reorderKey.length) {
            errln("Key lengths not the same during reordering.\n");
        }

        for (int i = 1; i < baseKey.length; i++) {
            if (baseKey[i] != reorderKey[i]) {
                errln("Collation key bytes not the same at position " + i);
            }
        }

        /* build collator tertiary */
        myCollation = new RuleBasedCollator("");
        myCollation.setStrength(Collator.QUATERNARY);
        baseKey = myCollation.getCollationKey(testString).toByteArray();

        myCollation.setReorderCodes(reorderCodes);
        reorderKey = myCollation.getCollationKey(testString).toByteArray();

        if (baseKey.length != reorderKey.length) {
            errln("Key lengths not the same during reordering.\n");
        }

        for (int i = 1; i < baseKey.length; i++) {
            if (baseKey[i] != reorderKey[i]) {
                errln("Collation key bytes not the same at position " + i);
            }
        }
    }

    /*
     * Test reordering API.
     */
    @Test
    public void TestReorderingAPI() throws Exception
    {
        Collator myCollation;
        int[] reorderCodes = {UScript.GREEK, UScript.HAN, ReorderCodes.PUNCTUATION};
        int[] duplicateReorderCodes = {UScript.HIRAGANA, UScript.GREEK, ReorderCodes.CURRENCY, UScript.KATAKANA};
        int[] reorderCodesStartingWithDefault = {ReorderCodes.DEFAULT, UScript.GREEK, UScript.HAN, ReorderCodes.PUNCTUATION};
        int[] retrievedReorderCodes;
        String greekString = "\u03b1";
        String punctuationString = "\u203e";

        /* build collator tertiary */
        myCollation = new RuleBasedCollator("");
        myCollation.setStrength(Collator.TERTIARY);

        /* set the reorderding */
        myCollation.setReorderCodes(reorderCodes);

        retrievedReorderCodes = myCollation.getReorderCodes();
        if (!Arrays.equals(reorderCodes, retrievedReorderCodes)) {
            errln("ERROR: retrieved reorder codes do not match set reorder codes.");
        }
        if (!(myCollation.compare(greekString, punctuationString) < 0)) {
            errln("ERROR: collation result should have been less.");
        }

        /* clear the reordering */
        myCollation.setReorderCodes(null);
        retrievedReorderCodes = myCollation.getReorderCodes();
        if (retrievedReorderCodes.length != 0) {
            errln("ERROR: retrieved reorder codes was not null.");
        }

        if (!(myCollation.compare(greekString, punctuationString) > 0)) {
            errln("ERROR: collation result should have been greater.");
        }

        // do it again with an empty but non-null array

        /* set the reorderding */
        myCollation.setReorderCodes(reorderCodes);

        retrievedReorderCodes = myCollation.getReorderCodes();
        if (!Arrays.equals(reorderCodes, retrievedReorderCodes)) {
            errln("ERROR: retrieved reorder codes do not match set reorder codes.");
        }
        if (!(myCollation.compare(greekString, punctuationString) < 0)) {
            errln("ERROR: collation result should have been less.");
        }

        /* clear the reordering */
        myCollation.setReorderCodes(new int[]{});
        retrievedReorderCodes = myCollation.getReorderCodes();
        if (retrievedReorderCodes.length != 0) {
            errln("ERROR: retrieved reorder codes was not null.");
        }

        if (!(myCollation.compare(greekString, punctuationString) > 0)) {
            errln("ERROR: collation result should have been greater.");
        }

        /* clear the reordering using [NONE] */
        myCollation.setReorderCodes(new int[]{ ReorderCodes.NONE });
        retrievedReorderCodes = myCollation.getReorderCodes();
        if (retrievedReorderCodes.length != 0) {
            errln("ERROR: [NONE] retrieved reorder codes was not null.");
        }

        boolean gotException = false;
        /* set duplicates in the reorder codes */
        try {
            myCollation.setReorderCodes(duplicateReorderCodes);
        } catch (IllegalArgumentException e) {
            // expect exception on illegal arguments
            gotException = true;
        }
        if (!gotException) {
            errln("ERROR: exception was not thrown for illegal reorder codes argument.");
        }

        /* set duplicate reorder codes */
        gotException = false;
        try {
            myCollation.setReorderCodes(reorderCodesStartingWithDefault);
        } catch (IllegalArgumentException e) {
            gotException = true;
        }
        if (!gotException) {
            errln("ERROR: reorder codes following a 'default' code should have thrown an exception but did not.");
        }
    }

    /*
     * Test reordering API.
     */
    @Test
    public void TestReorderingAPIWithRuleCreatedCollator() throws Exception
    {
        Collator myCollation;
        String rules = "[reorder Hani Grek]";
        int[] rulesReorderCodes = {UScript.HAN, UScript.GREEK};
        int[] reorderCodes = {UScript.GREEK, UScript.HAN, ReorderCodes.PUNCTUATION};
        int[] retrievedReorderCodes;


        /* build collator tertiary */
        myCollation = new RuleBasedCollator(rules);
        myCollation.setStrength(Collator.TERTIARY);

        retrievedReorderCodes = myCollation.getReorderCodes();
        if (!Arrays.equals(rulesReorderCodes, retrievedReorderCodes)) {
            errln("ERROR: retrieved reorder codes do not match set reorder codes.");
        }

        /* clear the reordering */
        myCollation.setReorderCodes(null);
        retrievedReorderCodes = myCollation.getReorderCodes();
        if (retrievedReorderCodes.length != 0) {
            errln("ERROR: retrieved reorder codes was not null.");
        }

        /* set the reorderding */
        myCollation.setReorderCodes(reorderCodes);

        retrievedReorderCodes = myCollation.getReorderCodes();
        if (!Arrays.equals(reorderCodes, retrievedReorderCodes)) {
            errln("ERROR: retrieved reorder codes do not match set reorder codes.");
        }

        /* reset the reordering */
        myCollation.setReorderCodes(ReorderCodes.DEFAULT);
        retrievedReorderCodes = myCollation.getReorderCodes();
        if (!Arrays.equals(rulesReorderCodes, retrievedReorderCodes)) {
            errln("ERROR: retrieved reorder codes do not match set reorder codes.");
        }
    }

    static boolean containsExpectedScript(int[] scripts, int expectedScript) {
        for (int i = 0; i < scripts.length; ++i) {
            if (expectedScript == scripts[i]) { return true; }
        }
        return false;
    }

    @Test
    public void TestEquivalentReorderingScripts() {
        // Beginning with ICU 55, collation reordering moves single scripts
        // rather than groups of scripts,
        // except where scripts share a range and sort primary-equal.
        final int[] expectedScripts = {
                UScript.HIRAGANA,
                UScript.KATAKANA,
                UScript.KATAKANA_OR_HIRAGANA
        };

        int[] equivalentScripts = RuleBasedCollator.getEquivalentReorderCodes(UScript.GOTHIC);
        if (equivalentScripts.length != 1 || equivalentScripts[0] != UScript.GOTHIC) {
            errln(String.format("ERROR/Gothic: retrieved equivalent scripts wrong: " +
                    "length expected 1, was = %d; expected [%d] was [%d]",
                    equivalentScripts.length, UScript.GOTHIC, equivalentScripts[0]));
        }

        equivalentScripts = RuleBasedCollator.getEquivalentReorderCodes(UScript.HIRAGANA);
        if (equivalentScripts.length != expectedScripts.length) {
            errln(String.format("ERROR/Hiragana: retrieved equivalent script length wrong: " +
                    "expected %d, was = %d",
                    expectedScripts.length, equivalentScripts.length));
        }
        int prevScript = -1;
        for (int i = 0; i < equivalentScripts.length; ++i) {
            int script = equivalentScripts[i];
            if (script <= prevScript) {
                errln("ERROR/Hiragana: equivalent scripts out of order at index " + i);
            }
            prevScript = script;
        }
        for (int code : expectedScripts) {
            if (!containsExpectedScript(equivalentScripts, code)) {
                errln("ERROR/Hiragana: equivalent scripts do not contain " + code);
            }
        }

        equivalentScripts = RuleBasedCollator.getEquivalentReorderCodes(UScript.KATAKANA);
        if (equivalentScripts.length != expectedScripts.length) {
            errln(String.format("ERROR/Katakana: retrieved equivalent script length wrong: " +
                    "expected %d, was = %d",
                    expectedScripts.length, equivalentScripts.length));
        }
        for (int code : expectedScripts) {
            if (!containsExpectedScript(equivalentScripts, code)) {
                errln("ERROR/Katakana: equivalent scripts do not contain " + code);
            }
        }

        equivalentScripts = RuleBasedCollator.getEquivalentReorderCodes(UScript.KATAKANA_OR_HIRAGANA);
        if (equivalentScripts.length != expectedScripts.length) {
            errln(String.format("ERROR/Hrkt: retrieved equivalent script length wrong: " +
                    "expected %d, was = %d",
                    expectedScripts.length, equivalentScripts.length));
        }

        equivalentScripts = RuleBasedCollator.getEquivalentReorderCodes(UScript.HAN);
        if (equivalentScripts.length != 3) {
            errln("ERROR/Hani: retrieved equivalent script length wrong: " +
                    "expected 3, was = " + equivalentScripts.length);
        }
        equivalentScripts = RuleBasedCollator.getEquivalentReorderCodes(UScript.SIMPLIFIED_HAN);
        if (equivalentScripts.length != 3) {
            errln("ERROR/Hans: retrieved equivalent script length wrong: " +
                    "expected 3, was = " + equivalentScripts.length);
        }
        equivalentScripts = RuleBasedCollator.getEquivalentReorderCodes(UScript.TRADITIONAL_HAN);
        if (equivalentScripts.length != 3) {
            errln("ERROR/Hant: retrieved equivalent script length wrong: " +
                    "expected 3, was = " + equivalentScripts.length);
        }

        equivalentScripts = RuleBasedCollator.getEquivalentReorderCodes(UScript.MEROITIC_CURSIVE);
        if (equivalentScripts.length != 2) {
            errln("ERROR/Merc: retrieved equivalent script length wrong: " +
                    "expected 2, was = " + equivalentScripts.length);
        }
        equivalentScripts = RuleBasedCollator.getEquivalentReorderCodes(UScript.MEROITIC_HIEROGLYPHS);
        if (equivalentScripts.length != 2) {
            errln("ERROR/Mero: retrieved equivalent script length wrong: " +
                    "expected 2, was = " + equivalentScripts.length);
        }
    }

    @Test
    public void TestGreekFirstReorderCloning() {
        String[] testSourceCases = {
            "\u0041",
            "\u03b1\u0041",
            "\u0061",
            "\u0041\u0061",
            "\u0391",
        };

        String[] testTargetCases = {
            "\u03b1",
            "\u0041\u03b1",
            "\u0391",
            "\u0391\u03b1",
            "\u0391",
        };

        int[] results = {
            1,
            -1,
            1,
            1,
            0
        };

        Collator  originalCollation;
        Collator  myCollation;
        String rules = "[reorder Grek]";
        try {
            originalCollation = new RuleBasedCollator(rules);
        } catch (Exception e) {
            warnln("ERROR: in creation of rule based collator");
            return;
        }
        try {
            myCollation = (Collator) originalCollation.clone();
        } catch (Exception e) {
            warnln("ERROR: in creation of rule based collator");
            return;
        }
        myCollation.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        myCollation.setStrength(Collator.TERTIARY);
        for (int i = 0; i < testSourceCases.length ; i++)
        {
            CollationTest.doTest(this, (RuleBasedCollator)myCollation,
                                 testSourceCases[i], testTargetCases[i],
                                 results[i]);
        }
    }

    /*
     * Utility function to test one collation reordering test case.
     * @param testcases Array of test cases.
     * @param n_testcases Size of the array testcases.
     * @param str_rules Array of rules.  These rules should be specifying the same rule in different formats.
     * @param n_rules Size of the array str_rules.
     */
    private void doTestOneReorderingAPITestCase(OneTestCase testCases[], int reorderTokens[])
    {
        Collator myCollation = Collator.getInstance(ULocale.ENGLISH);
        myCollation.setReorderCodes(reorderTokens);

        for (OneTestCase testCase : testCases) {
            CollationTest.doTest(this, (RuleBasedCollator)myCollation,
                    testCase.m_source_,
                    testCase.m_target_,
                    testCase.m_result_);
        }
    }

    @Test
    public void TestGreekFirstReorder()
    {
        String[] strRules = {
            "[reorder Grek]"
        };

        int[] apiRules = {
            UScript.GREEK
        };

        OneTestCase[] privateUseCharacterStrings = {
            new OneTestCase("\u0391", "\u0391", 0),
            new OneTestCase("\u0041", "\u0391", 1),
            new OneTestCase("\u03B1\u0041", "\u03B1\u0391", 1),
            new OneTestCase("\u0060", "\u0391", -1),
            new OneTestCase("\u0391", "\ue2dc", -1),
            new OneTestCase("\u0391", "\u0060", 1),
        };

        /* Test rules creation */
        doTestCollation(privateUseCharacterStrings, strRules);

        /* Test collation reordering API */
        doTestOneReorderingAPITestCase(privateUseCharacterStrings, apiRules);
    }

    @Test
    public void TestGreekLastReorder()
    {
        String[] strRules = {
            "[reorder Zzzz Grek]"
        };

        int[] apiRules = {
            UScript.UNKNOWN, UScript.GREEK
        };

        OneTestCase[] privateUseCharacterStrings = {
            new OneTestCase("\u0391", "\u0391", 0),
            new OneTestCase("\u0041", "\u0391", -1),
            new OneTestCase("\u03B1\u0041", "\u03B1\u0391", -1),
            new OneTestCase("\u0060", "\u0391", -1),
            new OneTestCase("\u0391", "\ue2dc", 1),
        };

        /* Test rules creation */
        doTestCollation(privateUseCharacterStrings, strRules);

        /* Test collation reordering API */
        doTestOneReorderingAPITestCase(privateUseCharacterStrings, apiRules);
    }

    @Test
    public void TestNonScriptReorder()
    {
        String[] strRules = {
            "[reorder Grek Symbol DIGIT Latn Punct space Zzzz cURRENCy]"
        };

        int[] apiRules = {
            UScript.GREEK, ReorderCodes.SYMBOL, ReorderCodes.DIGIT, UScript.LATIN,
            ReorderCodes.PUNCTUATION, ReorderCodes.SPACE, UScript.UNKNOWN,
            ReorderCodes.CURRENCY
        };

        OneTestCase[] privateUseCharacterStrings = {
            new OneTestCase("\u0391", "\u0041", -1),
            new OneTestCase("\u0041", "\u0391", 1),
            new OneTestCase("\u0060", "\u0041", -1),
            new OneTestCase("\u0060", "\u0391", 1),
            new OneTestCase("\u0024", "\u0041", 1),
        };

        /* Test rules creation */
        doTestCollation(privateUseCharacterStrings, strRules);

        /* Test collation reordering API */
        doTestOneReorderingAPITestCase(privateUseCharacterStrings, apiRules);
    }

    @Test
    public void TestHaniReorder()
    {
        String[] strRules = {
            "[reorder Hani]"
        };
        int[] apiRules = {
            UScript.HAN
        };

        OneTestCase[] privateUseCharacterStrings = {
            new OneTestCase("\u4e00", "\u0041", -1),
            new OneTestCase("\u4e00", "\u0060", 1),
            new OneTestCase("\uD86D\uDF40", "\u0041", -1),
            new OneTestCase("\uD86D\uDF40", "\u0060", 1),
            new OneTestCase("\u4e00", "\uD86D\uDF40", -1),
            new OneTestCase("\ufa27", "\u0041", -1),
            new OneTestCase("\uD869\uDF00", "\u0041", -1),
        };

        /* Test rules creation */
        doTestCollation(privateUseCharacterStrings, strRules);

        /* Test collation reordering API */
        doTestOneReorderingAPITestCase(privateUseCharacterStrings, apiRules);
    }

    @Test
    public void TestHaniReorderWithOtherRules()
    {
        String[] strRules = {
            "[reorder Hani]  &b<a"
        };

        OneTestCase[] privateUseCharacterStrings = {
            new OneTestCase("\u4e00", "\u0041", -1),
            new OneTestCase("\u4e00", "\u0060", 1),
            new OneTestCase("\uD86D\uDF40", "\u0041", -1),
            new OneTestCase("\uD86D\uDF40", "\u0060", 1),
            new OneTestCase("\u4e00", "\uD86D\uDF40", -1),
            new OneTestCase("\ufa27", "\u0041", -1),
            new OneTestCase("\uD869\uDF00", "\u0041", -1),
            new OneTestCase("b", "a", -1),
        };

        /* Test rules creation */
        doTestCollation(privateUseCharacterStrings, strRules);
    }

    @Test
    public void TestMultipleReorder()
    {
        String[] strRules = {
            "[reorder Grek Zzzz DIGIT Latn Hani]"
        };

        int[] apiRules = {
            UScript.GREEK, UScript.UNKNOWN, ReorderCodes.DIGIT, UScript.LATIN, UScript.HAN
        };

        OneTestCase[] collationTestCases = {
            new OneTestCase("\u0391", "\u0041", -1),
            new OneTestCase("\u0031", "\u0041", -1),
            new OneTestCase("u0041", "\u4e00", -1),
        };

        /* Test rules creation */
        doTestCollation(collationTestCases, strRules);

        /* Test collation reordering API */
        doTestOneReorderingAPITestCase(collationTestCases, apiRules);
    }

    @Test
    public void TestFrozeness()
    {
        Collator myCollation = Collator.getInstance(ULocale.CANADA);
        boolean exceptionCaught = false;

        myCollation.freeze();
        assertTrue("Collator not frozen.", myCollation.isFrozen());

        try {
            myCollation.setStrength(Collator.SECONDARY);
        } catch (UnsupportedOperationException e) {
            // expected
            exceptionCaught = true;
        }
        assertTrue("Frozen collator allowed change.", exceptionCaught);
        exceptionCaught = false;

        try {
            myCollation.setReorderCodes(ReorderCodes.DEFAULT);
        } catch (UnsupportedOperationException e) {
            // expected
            exceptionCaught = true;
        }
        assertTrue("Frozen collator allowed change.", exceptionCaught);
        exceptionCaught = false;

        try {
            myCollation.setVariableTop(12);
        } catch (UnsupportedOperationException e) {
            // expected
            exceptionCaught = true;
        }
        assertTrue("Frozen collator allowed change.", exceptionCaught);
        exceptionCaught = false;

        Collator myClone = null;
        try {
            myClone = (Collator) myCollation.clone();
        } catch (CloneNotSupportedException e) {
            // should not happen - clone is implemented in Collator
            errln("ERROR: unable to clone collator.");
        }
        assertTrue("Clone not frozen as expected.", myClone.isFrozen());

        myClone = myClone.cloneAsThawed();
        assertFalse("Clone not thawed as expected.", myClone.isFrozen());
    }

    // Test case for Ticket#9409
    // Unknown collation type should be ignored, without printing stack trace
    @Test
    public void TestUnknownCollationKeyword() {
        Collator coll1 = Collator.getInstance(new ULocale("en_US@collation=bogus"));
        Collator coll2 = Collator.getInstance(new ULocale("en_US"));
        assertEquals("Unknown collation keyword 'bogus' should be ignored", coll1, coll2);
    }
}
