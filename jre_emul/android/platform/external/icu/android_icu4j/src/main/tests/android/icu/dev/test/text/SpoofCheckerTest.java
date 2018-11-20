/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2009-2015, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.dev.test.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.BitSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.dev.test.TestUtil;
import android.icu.dev.test.TestUtil.JavaVendor;
import android.icu.impl.Utility;
import android.icu.lang.UScript;
import android.icu.text.Normalizer2;
import android.icu.text.SpoofChecker;
import android.icu.text.SpoofChecker.CheckResult;
import android.icu.text.SpoofChecker.RestrictionLevel;
import android.icu.text.UnicodeSet;
import android.icu.util.ULocale;

public class SpoofCheckerTest extends TestFmwk {
    /*
     * Identifiers for verifying that spoof checking is minimally alive and working.
     */
    char[] goodLatinChars = { (char) 0x75, (char) 0x7a };
    String goodLatin = new String(goodLatinChars); /* "uz", all ASCII */
    /* (not confusable) */
    char[] scMixedChars = { (char) 0x73, (char) 0x0441 };
    String scMixed = new String(scMixedChars); /* "sc", with Cyrillic 'c' */
    /* (mixed script, confusable */

    String scLatin = "sc";   /* "sc", plain ascii. */
    String goodCyrl = "\u0438\u043B";    // "Cyrillic small letter i and el"  Plain lower case Cyrillic letters, no latin confusables
    String goodGreek = "\u03c0\u03c6";   // "Greek small letter pi and phi"  Plain lower case Greek letters

    // Various 1 l I look-alikes
    String lll_Latin_a = "lI1";   // small letter l, cap I, digit 1, all ASCII
    //  "\uFF29\u217C\u0196"  Full-width I, Small Roman Numeral fifty, Latin Cap Letter IOTA
    String lll_Latin_b = "\uff29\u217c\u0196";
    String lll_Cyrl = "\u0406\u04C0\u0031";  // "\u0406\u04C01"
    /* The skeleton transform for all of the 'lll' lookalikes is ascii lower case letter l. */
    String lll_Skel = "lll";

    String han_Hiragana = "\u3086\u308A \u77F3\u7530";  // Hiragana, space, Han


    /*
     * Test basic constructor.
     */
    @Test
    public void TestUSpoof() {
        SpoofChecker sc = new SpoofChecker.Builder().build();
        if (sc == null) {
            errln("FAIL: null SpoofChecker");
        }
    }

    /*
     * Test build from source rules.
     */
    @Test
    public void TestOpenFromSourceRules() {
        if (TestUtil.getJavaVendor() == JavaVendor.IBM && TestUtil.getJavaVersion() == 5) {
            // Note: IBM Java 5 has a bug reading a large UTF-8 text contents
            logln("Skip this test case because of the IBM Java 5 bug");
            return;
        }
        String fileName;
        Reader confusables;

        try {
            SpoofChecker rsc = null;

            fileName = "unicode/confusables.txt";
            confusables = TestUtil.getDataReader(fileName, "UTF-8");
            try {
                rsc = new SpoofChecker.Builder().setData(confusables).build();
            } finally {
                confusables.close();
            }

            if (rsc == null) {
                errln("FAIL: null SpoofChecker");
                return;
            }
            // Check that newly built-from-rules SpoofChecker is able to function.
            checkSkeleton(rsc, "TestOpenFromSourceRules");

            SpoofChecker.CheckResult result = new SpoofChecker.CheckResult();
            rsc.failsChecks("Hello", result);

            // The checker we just built from source rules should be equivalent to the
            //  default checker created from prebuilt rules baked into the ICU data.
            SpoofChecker defaultChecker = new SpoofChecker.Builder().build();
            assertEquals("Checker built from rules equals default", defaultChecker, rsc);
            assertEquals("Checker built from rules has same hash code as default", defaultChecker.hashCode(), rsc.hashCode());

            SpoofChecker optionChecker = new SpoofChecker.Builder().
                                    setRestrictionLevel(RestrictionLevel.UNRESTRICTIVE).build();
            assertFalse("", optionChecker.equals(rsc));

            String stubConfusables =
                "# Stub confusables data\n" +
                "05AD ; 0596 ;  MA  # ( ֭ → ֖ ) HEBREW ACCENT DEHI → HEBREW ACCENT TIPEHA   #\n";

            // Verify that re-using a builder doesn't alter SpoofCheckers that were
            //  previously created by that builder. (The builder could modify data
            //  being used by the existing checker)

            SpoofChecker.Builder builder = new SpoofChecker.Builder();
            SpoofChecker testChecker1 = builder.build();
            assertTrue("", testChecker1.equals(defaultChecker));

            builder.setData(new StringReader(stubConfusables));
            builder.setRestrictionLevel(RestrictionLevel.UNRESTRICTIVE);
            builder.setChecks(SpoofChecker.SINGLE_SCRIPT_CONFUSABLE);
            Set<ULocale>allowedLocales = new HashSet<ULocale>();
            allowedLocales.add(ULocale.JAPANESE);
            allowedLocales.add(ULocale.FRENCH);
            builder.setAllowedLocales(allowedLocales);
            SpoofChecker testChecker2 = builder.build();
            SpoofChecker testChecker3 = builder.build();

            assertTrue("", testChecker1.equals(defaultChecker));
            assertFalse("", testChecker2.equals(defaultChecker));
            assertTrue("", testChecker2.equals(testChecker3));

        } catch (java.io.IOException e) {
            errln(e.toString());
        } catch (ParseException e) {
            errln(e.toString());
        }
    }

    /*
     * Set & Get Check Flags
     */
    @Test
    public void TestGetSetChecks1() {
        SpoofChecker sc = new SpoofChecker.Builder().setChecks(SpoofChecker.ALL_CHECKS).build();
        int t;
        t = sc.getChecks();
        assertEquals("", SpoofChecker.ALL_CHECKS, t);

        sc = new SpoofChecker.Builder().setChecks(0).build();
        t = sc.getChecks();
        assertEquals("", 0, t);

        int checks = SpoofChecker.WHOLE_SCRIPT_CONFUSABLE | SpoofChecker.MIXED_SCRIPT_CONFUSABLE
                | SpoofChecker.ANY_CASE;
        sc = new SpoofChecker.Builder().setChecks(checks).build();
        t = sc.getChecks();
        assertEquals("", checks, t);
    }

    /*
     * get & setAllowedChars
     */
    @Test
    public void TestGetSetAllowedChars() {
        SpoofChecker sc = new SpoofChecker.Builder().setChecks(SpoofChecker.CHAR_LIMIT).build();
        UnicodeSet us;
        UnicodeSet uset;

        uset = sc.getAllowedChars();
        assertTrue("", uset.isFrozen());
        us = new UnicodeSet(0x41, 0x5A); /* [A-Z] */
        sc = new SpoofChecker.Builder().setChecks(SpoofChecker.CHAR_LIMIT).setAllowedChars(us).build();
        assertEquals("", us, sc.getAllowedChars());
    }

    /*
     * get & set Checks
     */
    @Test
    public void TestGetSetChecks() {
        SpoofChecker sc = new SpoofChecker.Builder().build();
        int checks;
        int checks2;
        boolean checkResults;

        checks = sc.getChecks();
        assertEquals("", SpoofChecker.ALL_CHECKS, checks);

        checks &= ~(SpoofChecker.SINGLE_SCRIPT | SpoofChecker.MIXED_SCRIPT_CONFUSABLE);
        sc = new SpoofChecker.Builder().setChecks(checks).build();
        checks2 = sc.getChecks();
        assertEquals("", checks, checks2);

        /*
         * The checks that were disabled just above are the same ones that the "scMixed" test fails. So with those tests
         * gone checking that Identifier should now succeed
         */
        checkResults = sc.failsChecks(scMixed);
        assertFalse("", checkResults);
    }

    /*
     * AllowedLocales
     */
    @Test
    public void TestAllowedLocales() {
        SpoofChecker sc = new SpoofChecker.Builder().setChecks(SpoofChecker.CHAR_LIMIT).build();
        Set<ULocale> allowedLocales = null;
        Set<Locale> allowedJavaLocales = null;
        boolean checkResults;

        /* Default allowed locales list should be empty */
        allowedLocales = sc.getAllowedLocales();
        assertTrue("Empty allowed locales", allowedLocales.isEmpty());

        allowedJavaLocales = sc.getAllowedJavaLocales();
        assertTrue("Empty allowed Java locales", allowedJavaLocales.isEmpty());

        /* Allow en and ru, which should enable Latin and Cyrillic only to pass */
        ULocale enloc = new ULocale("en");
        ULocale ruloc = new ULocale("ru_RU");
        allowedLocales = new HashSet<ULocale>();
        allowedLocales.add(enloc);
        allowedLocales.add(ruloc);
        sc = new SpoofChecker.Builder().setChecks(SpoofChecker.CHAR_LIMIT).setAllowedLocales(allowedLocales).build();
        allowedLocales = sc.getAllowedLocales();
        assertTrue("en in allowed locales", allowedLocales.contains(enloc));
        assertTrue("ru_RU in allowed locales", allowedLocales.contains(ruloc));

        Locale frlocJ = new Locale("fr");
        allowedJavaLocales = new HashSet<Locale>();
        allowedJavaLocales.add(frlocJ);
        sc = new SpoofChecker.Builder().setChecks(SpoofChecker.CHAR_LIMIT).setAllowedJavaLocales(allowedJavaLocales).build();
        assertFalse("no en in allowed Java locales", allowedJavaLocales.contains(new Locale("en")));
        assertTrue("fr in allowed Java locales", allowedJavaLocales.contains(frlocJ));

        sc = new SpoofChecker.Builder().setChecks(SpoofChecker.CHAR_LIMIT).setAllowedLocales(allowedLocales).build();

        SpoofChecker.CheckResult result = new SpoofChecker.CheckResult();
        checkResults = sc.failsChecks(goodLatin);
        assertFalse("", checkResults);

        checkResults = sc.failsChecks(goodGreek, result);
        assertEquals("", SpoofChecker.CHAR_LIMIT, result.checks);

        checkResults = sc.failsChecks(goodCyrl);
        assertFalse("", checkResults);

        /* Reset with an empty locale list, which should allow all characters to pass */
        allowedLocales = new LinkedHashSet<ULocale>();
        sc = new SpoofChecker.Builder().setChecks(SpoofChecker.CHAR_LIMIT).setAllowedLocales(allowedLocales).build();

        checkResults = sc.failsChecks(goodGreek);
        assertFalse("", checkResults);
    }

    /*
     * AllowedChars set/get the UnicodeSet of allowed characters.
     */
    @Test
    public void TestAllowedChars() {
        SpoofChecker sc = new SpoofChecker.Builder().setChecks(SpoofChecker.CHAR_LIMIT).build();
        UnicodeSet set;
        UnicodeSet tmpSet;
        boolean checkResults;

        /* By default, we should see no restriction; the UnicodeSet should allow all characters. */
        set = sc.getAllowedChars();
        tmpSet = new UnicodeSet(0, 0x10ffff);
        assertEquals("", tmpSet, set);

        /* Remove a character that is in our good Latin test identifier from the allowed chars set. */
        tmpSet.remove(goodLatin.charAt(1));
        sc = new SpoofChecker.Builder().setChecks(SpoofChecker.CHAR_LIMIT).setAllowedChars(tmpSet).build();

        /* Latin Identifier should now fail; other non-latin test cases should still be OK */
        SpoofChecker.CheckResult result = new SpoofChecker.CheckResult();
        checkResults = sc.failsChecks(goodLatin, result);
        assertTrue("", checkResults);
        assertEquals("", SpoofChecker.CHAR_LIMIT, result.checks);
    }

    @Test
    public void TestCheck() {
        SpoofChecker sc = new SpoofChecker.Builder().setChecks(SpoofChecker.ALL_CHECKS).build();
        SpoofChecker.CheckResult result = new SpoofChecker.CheckResult();
        boolean checkResults;

        result.position = 666;
        checkResults = sc.failsChecks(goodLatin, result);
        assertFalse("", checkResults);
        assertEquals("", 0, result.checks);

        checkResults = sc.failsChecks(goodCyrl, result);
        assertFalse("", checkResults);
        assertEquals("", 0, result.checks);

        result.position = 666;
        checkResults = sc.failsChecks(scMixed, result);
        assertTrue("", checkResults);
        assertEquals("", SpoofChecker.RESTRICTION_LEVEL, result.checks);

        result.position = 666;
        checkResults = sc.failsChecks(han_Hiragana, result);
        assertFalse("", checkResults);
        assertEquals("", 0, result.checks);
    }

    @Test
    public void TestAreConfusable1() {
        SpoofChecker sc = new SpoofChecker.Builder().build();
        int checkResults;
        checkResults = sc.areConfusable(scLatin, scMixed);
        assertEquals("Latin/Mixed is not MIXED_SCRIPT_CONFUSABLE", SpoofChecker.MIXED_SCRIPT_CONFUSABLE, checkResults);

        checkResults = sc.areConfusable(goodGreek, scLatin);
        assertEquals("Greek/Latin is not unconfusable", 0, checkResults);

        checkResults = sc.areConfusable(lll_Latin_a, lll_Latin_b);
        assertEquals("Latin/Latin is not SINGLE_SCRIPT_CONFUSABLE", SpoofChecker.SINGLE_SCRIPT_CONFUSABLE, checkResults);
    }

    @Test
    public void TestGetSkeleton() {
        SpoofChecker sc = new SpoofChecker.Builder().setChecks(SpoofChecker.CONFUSABLE).build();
        String dest;
        dest = sc.getSkeleton(SpoofChecker.ANY_CASE, lll_Latin_a);
        assertEquals("", lll_Skel, dest);
    }

    /**
     * IntlTestSpoof is the top level test class for the Unicode Spoof detection tests
     */

    // Test the USpoofDetector API functions that require C++
    // The pure C part of the API, which is most of it, is tested in cintltst
    /**
     * IntlTestSpoof tests for USpoofDetector
     */
    @Test
    public void TestSpoofAPI() {
        SpoofChecker sc = new SpoofChecker.Builder().setChecks(SpoofChecker.ALL_CHECKS).build();
        String s = "xyz";
        SpoofChecker.CheckResult result = new SpoofChecker.CheckResult();
        result.position = 666;
        boolean checkResults = sc.failsChecks(s, result);
        assertFalse("", checkResults);
        assertEquals("", 0, result.position);

        sc = new SpoofChecker.Builder().build();
        String s1 = "cxs";
        String s2 = Utility.unescape("\\u0441\\u0445\\u0455"); // Cyrillic "cxs"
        int checkResult = sc.areConfusable(s1, s2);
        assertEquals("", SpoofChecker.MIXED_SCRIPT_CONFUSABLE | SpoofChecker.WHOLE_SCRIPT_CONFUSABLE, checkResult);

        sc = new SpoofChecker.Builder().build();
        s = "I1l0O";
        String dest = sc.getSkeleton(SpoofChecker.ANY_CASE, s);
        assertEquals("", dest, "lllOO");
    }

    @Test
    public void TestSkeleton() {
        SpoofChecker sc = new SpoofChecker.Builder().build();
        checkSkeleton(sc, "TestSkeleton");
    }

    // testSkeleton. Spot check a number of confusable skeleton substitutions from the
    // Unicode data file confusables.txt
    // Test cases chosen for substitutions of various lengths, and
    // membership in different mapping tables.
    public void checkSkeleton(SpoofChecker sc, String testName) {
        int ML = 0;
        int SL = SpoofChecker.SINGLE_SCRIPT_CONFUSABLE;
        int MA = SpoofChecker.ANY_CASE;
        int SA = SpoofChecker.SINGLE_SCRIPT_CONFUSABLE | SpoofChecker.ANY_CASE;

        checkSkeleton(sc, MA, "\\u02b9identifier'",  "'identifier'",  testName);

        checkSkeleton(sc, SL, "nochange", "nochange", testName);
        checkSkeleton(sc, SA, "nochange", "nochange", testName);
        checkSkeleton(sc, ML, "nochange", "nochange", testName);
        checkSkeleton(sc, MA, "nochange", "nochange", testName);
        checkSkeleton(sc, MA, "love", "love", testName);
        checkSkeleton(sc, MA, "1ove", "love", testName);   // Digit 1 to letter l
        checkSkeleton(sc, ML, "OOPS", "OOPS", testName);
        checkSkeleton(sc, ML, "00PS", "OOPS", testName);
        checkSkeleton(sc, MA, "OOPS", "OOPS", testName);
        checkSkeleton(sc, MA, "00PS", "OOPS", testName);   // Digit 0 to letter O
        checkSkeleton(sc, SL, "\\u059c", "\\u0301", testName);
        checkSkeleton(sc, SL, "\\u2A74", "\\u003A\\u003A\\u003D", testName);
        checkSkeleton(sc, SL, "\\u247E", "(ll)", testName);
        checkSkeleton(sc, SL, "\\uFDFB", "\\u062C\\u0644\\u0020\\u062C\\u0644\\u006c\\u0644\\u006f", testName);

        // 0C83 mapping existed in the ML and MA tables, did not exist in SL, SA (Original Unicode 7)
        //   mapping exists in all tables (ICU 55).
        // 0C83 ; 0983 ; ML #  KANNADA SIGN VISARGA to
        checkSkeleton(sc, SL, "\\u0C83", "\\u0983", testName);
        checkSkeleton(sc, SA, "\\u0C83", "\\u0983", testName);
        checkSkeleton(sc, ML, "\\u0C83", "\\u0983", testName);
        checkSkeleton(sc, MA, "\\u0C83", "\\u0983", testName);

        // 0391 mappings existed only in MA and SA tables (Original Unicode 7).
        //      mappings exist in all tables (ICU 55)
        checkSkeleton(sc, MA, "\\u0391", "A", testName);
        checkSkeleton(sc, SA, "\\u0391", "A", testName);
        checkSkeleton(sc, ML, "\\u0391", "A", testName);
        checkSkeleton(sc, SL, "\\u0391", "A", testName);

        // 13CF Mappings in all four tables, different in MA (Original Unicode 7).
        //      Mapping same in all tables (ICU 55)
        checkSkeleton(sc, ML, "\\u13CF", "b", testName);
        checkSkeleton(sc, MA, "\\u13CF", "b", testName);
        checkSkeleton(sc, SL, "\\u13CF", "b", testName);
        checkSkeleton(sc, SA, "\\u13CF", "b", testName);

        // 0022 ; 0027 0027 ;
        // all tables
        checkSkeleton(sc, SL, "\"", "\\u0027\\u0027", testName);
        checkSkeleton(sc, SA, "\"", "\\u0027\\u0027", testName);
        checkSkeleton(sc, ML, "\"", "\\u0027\\u0027", testName);
        checkSkeleton(sc, MA, "\"", "\\u0027\\u0027", testName);

    }

    // Internal function to run a single skeleton test case.
    //
    // Run a single confusable skeleton transformation test case.
    //
    void checkSkeleton(SpoofChecker sc, int type, String input, String expected, String testName) {
        String uInput = Utility.unescape(input);
        String uExpected = Utility.unescape(expected);
        String actual;
        actual = sc.getSkeleton(type, uInput);
        Throwable t = new Throwable();
        int lineNumberOfTest = t.getStackTrace()[1].getLineNumber();

        assertEquals(testName + " test at line " + lineNumberOfTest + " :  Expected (escaped): " + expected, uExpected, actual);
    }

    @Test
    public void TestAreConfusable() {
        SpoofChecker sc = new SpoofChecker.Builder().setChecks(SpoofChecker.CONFUSABLE).build();
        String s1 = "A long string that will overflow stack buffers.  A long string that will overflow stack buffers. "
                + "A long string that will overflow stack buffers.  A long string that will overflow stack buffers. ";
        String s2 = "A long string that wi11 overflow stack buffers.  A long string that will overflow stack buffers. "
                + "A long string that wi11 overflow stack buffers.  A long string that will overflow stack buffers. ";
        assertEquals("", SpoofChecker.SINGLE_SCRIPT_CONFUSABLE, sc.areConfusable(s1, s2));
    }

    @Test
    public void TestConfusableFlagVariants() {
        // The spoof checker should only return those tests that the user requested.  This test makes sure that
        // the checker doesn't return anything the user doesn't want.  This test started passing in ICU 58.

        String latn = "desordenado";
        String cyrl = "ԁеѕогԁепаԁо";
        String mixed = "dеѕогdenаdo";

        Object[][] tests = {
                // string 1, string 2, checks for spoof checker, expected output
                { latn, cyrl,
                    SpoofChecker.CONFUSABLE,
                    SpoofChecker.MIXED_SCRIPT_CONFUSABLE | SpoofChecker.WHOLE_SCRIPT_CONFUSABLE },
                { latn, cyrl,
                    SpoofChecker.MIXED_SCRIPT_CONFUSABLE | SpoofChecker.WHOLE_SCRIPT_CONFUSABLE,
                    SpoofChecker.MIXED_SCRIPT_CONFUSABLE | SpoofChecker.WHOLE_SCRIPT_CONFUSABLE },
                { latn, cyrl,
                    SpoofChecker.MIXED_SCRIPT_CONFUSABLE,
                    SpoofChecker.MIXED_SCRIPT_CONFUSABLE },
                { latn, cyrl,
                    SpoofChecker.WHOLE_SCRIPT_CONFUSABLE,
                    SpoofChecker.WHOLE_SCRIPT_CONFUSABLE },
                { latn, cyrl,
                    SpoofChecker.SINGLE_SCRIPT_CONFUSABLE,
                    0 },
                { latn, mixed,
                    SpoofChecker.CONFUSABLE,
                    SpoofChecker.MIXED_SCRIPT_CONFUSABLE },
                { latn, mixed,
                    SpoofChecker.MIXED_SCRIPT_CONFUSABLE,
                    SpoofChecker.MIXED_SCRIPT_CONFUSABLE },
                { latn, mixed,
                    SpoofChecker.MIXED_SCRIPT_CONFUSABLE | SpoofChecker.WHOLE_SCRIPT_CONFUSABLE,
                    SpoofChecker.MIXED_SCRIPT_CONFUSABLE },
                { latn, mixed,
                    SpoofChecker.WHOLE_SCRIPT_CONFUSABLE,
                    0 },
                { latn, latn,
                    SpoofChecker.CONFUSABLE,
                    SpoofChecker.SINGLE_SCRIPT_CONFUSABLE },
        };

        for (Object[] test : tests) {
            String s1 = (String) test[0];
            String s2 = (String) test[1];
            int checks = (Integer) test[2];
            int expectedResult = (Integer) test[3];

            // Sanity check: expectedResult should be a subset of checks
            assertEquals("Invalid test case", expectedResult & checks, expectedResult);

            SpoofChecker sc = new SpoofChecker.Builder().setChecks(checks).build();
            int actualResult = sc.areConfusable(s1, s2);
            assertEquals("Comparing '" + s1 + "' and '" + s2 + "' with checks '" + checks + "'",
                    expectedResult, actualResult);
        }
    }

    @Test
    public void TestInvisible() {
        SpoofChecker sc = new SpoofChecker.Builder().setChecks(SpoofChecker.INVISIBLE).build();
        String s = Utility.unescape("abcd\\u0301ef");
        SpoofChecker.CheckResult result = new SpoofChecker.CheckResult();
        result.position = -42;
        assertFalse("", sc.failsChecks(s, result));
        assertEquals("", 0, result.checks);
        assertEquals("", result.position, 0);

        String s2 = Utility.unescape("abcd\\u0301\\u0302\\u0301ef");
        assertTrue("", sc.failsChecks(s2, result));
        assertEquals("", SpoofChecker.INVISIBLE, result.checks);
        assertEquals("", 0, result.position);

        // Two acute accents, one from the composed a with acute accent, \u00e1,
        // and one separate.
        result.position = -42;
        String s3 = Utility.unescape("abcd\\u00e1\\u0301xyz");
        assertTrue("", sc.failsChecks(s3, result));
        assertEquals("", SpoofChecker.INVISIBLE, result.checks);
        assertEquals("", 0, result.position);
    }

    @Test
    public void TestRestrictionLevel() {
        Object[][] tests = {
                {"aγ♥", RestrictionLevel.UNRESTRICTIVE},
                {"a", RestrictionLevel.ASCII},
                {"γ", RestrictionLevel.SINGLE_SCRIPT_RESTRICTIVE},
                {"aアー", RestrictionLevel.HIGHLY_RESTRICTIVE},
                {"aऄ", RestrictionLevel.MODERATELY_RESTRICTIVE},
                {"aγ", RestrictionLevel.MINIMALLY_RESTRICTIVE},
                {"a♥", RestrictionLevel.UNRESTRICTIVE},
                {"a\u303c", RestrictionLevel.HIGHLY_RESTRICTIVE},
                {"aー\u303c", RestrictionLevel.HIGHLY_RESTRICTIVE},
                {"aー\u303cア", RestrictionLevel.HIGHLY_RESTRICTIVE},
                { "アaー\u303c", RestrictionLevel.HIGHLY_RESTRICTIVE},
                {"a1١", RestrictionLevel.MODERATELY_RESTRICTIVE},
                {"a1١۱", RestrictionLevel.MODERATELY_RESTRICTIVE},
                {"١ー\u303caア1१۱", RestrictionLevel.MINIMALLY_RESTRICTIVE},
                {"aアー\u303c1१١۱", RestrictionLevel.MINIMALLY_RESTRICTIVE},
        };

        UnicodeSet allowedChars = new UnicodeSet();
        // Allowed Identifier Characters. In addition to the Recommended Set,
        //    allow u303c, which has an interesting script extension of Hani Hira Kana.
        allowedChars.addAll(SpoofChecker.RECOMMENDED).add(0x303c);

        CheckResult checkResult = new CheckResult();
        for (Object[] test : tests) {
            String testString = (String) test[0];
            RestrictionLevel expectedLevel = (RestrictionLevel) test[1];
            for (RestrictionLevel levelSetInSpoofChecker : RestrictionLevel.values()) {
                SpoofChecker sc = new SpoofChecker.Builder()
                        .setAllowedChars(allowedChars)
                        .setRestrictionLevel(levelSetInSpoofChecker)
                        .setChecks(SpoofChecker.RESTRICTION_LEVEL) // only check this
                        .build();
                boolean actualValue = sc.failsChecks(testString, checkResult);
                assertEquals("Testing restriction level for '" + testString + "'",
                        expectedLevel, checkResult.restrictionLevel);

                // we want to fail if the text is (say) MODERATE and the testLevel is ASCII
                boolean expectedFailure = expectedLevel.compareTo(levelSetInSpoofChecker) > 0;
                assertEquals("Testing spoof restriction level for '" + testString + "', " + levelSetInSpoofChecker,
                        expectedFailure, actualValue);

                // Coverage for getRestrictionLevel
                assertEquals("Restriction level on built SpoofChecker should be same as on builder",
                        levelSetInSpoofChecker, sc.getRestrictionLevel());
            }
        }
    }

    @Test
    public void TestMixedNumbers() {
        Object[][] tests = {
                {"1", "[0]"},
                {"१", "[०]"},
                {"1१", "[0०]"},
                {"١۱", "[٠۰]"},
                {"a♥", "[]"},
                {"a\u303c", "[]"},
                {"aー\u303c", "[]"},
                {"aー\u303cア", "[]"},
                { "アaー\u303c", "[]"},
                {"a1١", "[0٠]"},
                {"a1١۱", "[0٠۰]"},
                {"١ー\u303caア1१۱", "[0٠۰०]"},
                {"aアー\u303c1१١۱", "[0٠۰०]"},
        };
        CheckResult checkResult = new CheckResult();
        for (Object[] test : tests) {
            String testString = (String) test[0];
            UnicodeSet expected = new UnicodeSet((String)test[1]);

            SpoofChecker sc = new SpoofChecker.Builder()
            .setChecks(SpoofChecker.MIXED_NUMBERS) // only check this
            .build();
            boolean actualValue = sc.failsChecks(testString, checkResult);
            assertEquals("", expected, checkResult.numerics);
            assertEquals("Testing spoof mixed numbers for '" + testString + "', ", expected.size() > 1, actualValue);
        }
    }

    @Test
    public void TestBug11635() {
        // The bug was an error in iterating through supplementary characters in IdentifierInfo.
        //  The three supplemental chars in the string are "123" from the mathematical bold digit range.
        //  Common script, Nd general category, and no other restrictions on allowed characters
        //  leaves "ABC123" as SINGLE_SCRIPT_RESTRICTIVE.
        String identifier = Utility.unescape("ABC\\U0001D7CF\\U0001D7D0\\U0001D7D1");
        CheckResult checkResult = new CheckResult();
        SpoofChecker sc = new SpoofChecker.Builder().setChecks(SpoofChecker.RESTRICTION_LEVEL).build();
        sc.failsChecks(identifier, checkResult);
        assertEquals("", RestrictionLevel.SINGLE_SCRIPT_RESTRICTIVE, checkResult.restrictionLevel);
    }

    private String parseHex(String in) {
        StringBuilder sb = new StringBuilder();
        for (String oneCharAsHexString : in.split("\\s+")) {
            if (oneCharAsHexString.length() > 0) {
                sb.appendCodePoint(Integer.parseInt(oneCharAsHexString, 16));
            }
        }
        return sb.toString();
    }

    private String escapeString(String in) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < in.length(); i++) {
            int c = in.codePointAt(i);
            if (c <= 0x7f) {
                out.append((char) c);
            } else if (c <= 0xffff) {
                out.append(String.format("\\u%04x", c));
            } else {
                out.append(String.format("\\U%06x", c));
                i++;
            }
        }
        return out.toString();
    }

    // Verify that each item from the Unicode confusables.txt file
    // transforms into the expected skeleton.
    @Test
    public void testConfData() {
        if (TestUtil.getJavaVendor() == JavaVendor.IBM && TestUtil.getJavaVersion() == 5) {
            // Note: IBM Java 5 has a bug reading a large UTF-8 text contents
            logln("Skip this test case because of the IBM Java 5 bug");
            return;
        }
        try {
            // Read in the confusables.txt file. (Distributed by Unicode.org)
            String fileName = "unicode/confusables.txt";
            BufferedReader confusablesRdr = TestUtil.getDataReader(fileName, "UTF-8");

            // Create a default spoof checker to use in this test.
            SpoofChecker sc = new SpoofChecker.Builder().build();

            // Parse lines from the confusables.txt file. Example Line:
            // FF44 ; 0064 ; SL # ( d -> d ) FULLWIDTH ....
            // Lines have three fields. The hex fields can contain more than one character,
            // and each character may be more than 4 digits (for supplemntals)
            // This regular expression matches lines and splits the fields into capture groups.
            // Capture group 1: map from chars
            // 2: map to chars
            // 3: table type, SL, ML, SA or MA (deprecated)
            // 4: Comment Lines Only
            // 5: Error Lines Only
            Matcher parseLine = Pattern.compile(
                    "\\ufeff?" + "(?:([0-9A-F\\s]+);([0-9A-F\\s]+);\\s*(SL|ML|SA|MA)\\s*(?:#.*?)?$)"
                            + "|\\ufeff?(\\s*(?:#.*)?)"). // Comment line
                            matcher("");
            Normalizer2 normalizer = Normalizer2.getNFDInstance();
            int lineNum = 0;
            String inputLine;
            while ((inputLine = confusablesRdr.readLine()) != null) {
                lineNum++;
                parseLine.reset(inputLine);
                if (!parseLine.matches()) {
                    errln("Syntax error in confusable data file at line " + lineNum);
                    errln(inputLine);
                    break;
                }
                if (parseLine.group(4) != null) {
                    continue; // comment line
                }
                String from = parseHex(parseLine.group(1));

                if (!normalizer.isNormalized(from)) {
                    // The source character was not NFD.
                    // Skip this case; the first step in obtaining a skeleton is to NFD the input,
                    // so the mapping in this line of confusables.txt will never be applied.
                    continue;
                }

                String rawExpected = parseHex(parseLine.group(2));
                String expected = normalizer.normalize(rawExpected);

                String actual;
                actual = sc.getSkeleton(from);

                if (!actual.equals(expected)) {
                    errln("confusables.txt: " + lineNum + ": " + parseLine.group(0));
                    errln("Actual: " + escapeString(actual));
                }
            }
            confusablesRdr.close();
        } catch (IOException e) {
            errln(e.toString());
        }
    }

    @Test
    public void TestCheckResultToString11447() {
        CheckResult checkResult = new CheckResult();
        SpoofChecker sc = new SpoofChecker.Builder()
                .setChecks(SpoofChecker.MIXED_NUMBERS)
                .build();
        sc.failsChecks("1१", checkResult);
        assertTrue("CheckResult: ", checkResult.toString().contains("MIXED_NUMBERS"));
    }

    @Test
    public void TestDeprecated() {
        // getSkeleton
        SpoofChecker sc = new SpoofChecker.Builder().build();
        assertEquals("Deprecated version of getSkeleton method does not work",
                sc.getSkeleton(SpoofChecker.ANY_CASE, scMixed),
                sc.getSkeleton(scMixed));

        // setData
        try {
            String fileName1 = "unicode/confusables.txt";
            String fileName2 = "unicode/confusablesWholeScript.txt";
            Reader reader1 = TestUtil.getDataReader(fileName1, "UTF-8");
            Reader reader2 = TestUtil.getDataReader(fileName2, "UTF-8");
            Reader reader3 = TestUtil.getDataReader(fileName1, "UTF-8");
            try {
                SpoofChecker sc2 = new SpoofChecker.Builder()
                        .setData(reader1, reader2)
                        .build();
                SpoofChecker sc1 = new SpoofChecker.Builder()
                        .setData(reader3)
                        .build();
                assertEquals("Deprecated version of setData method does not work", sc1, sc2);
            } finally {
                reader1.close();
                reader2.close();
                reader3.close();
            }
        } catch(IOException e) {
            fail("Could not load confusables data");
        } catch (ParseException e) {
            fail("Could not parse confusables data");
        }
    }

    @Test
    public void testScriptSet() {
        try {
            Class ScriptSet = Class.forName("android.icu.text.SpoofChecker$ScriptSet");
            Constructor ctor = ScriptSet.getDeclaredConstructor();
            ctor.setAccessible(true);
            BitSet ss = (BitSet) ctor.newInstance();

            ss.set(UScript.MYANMAR);
            assertEquals("ScriptSet toString with Myanmar", "<ScriptSet { Mymr }>", ss.toString());
            ss.set(UScript.BENGALI);
            ss.set(UScript.LATIN);
            assertEquals("ScriptSet toString with Myanmar, Latin, and Bengali", "<ScriptSet { Beng Latn Mymr }>", ss.toString());

            Method and = ScriptSet.getDeclaredMethod("and", Integer.TYPE);
            and.setAccessible(true);
            and.invoke(ss, UScript.BENGALI);
            assertEquals("ScriptSet toString with Bengali only", "<ScriptSet { Beng }>", ss.toString());

            Method setAll = ScriptSet.getDeclaredMethod("setAll");
            setAll.setAccessible(true);
            setAll.invoke(ss);
            assertEquals("ScriptSet toString with all scripts", "<ScriptSet { * }>", ss.toString());

            Method isFull = ScriptSet.getDeclaredMethod("isFull");
            isFull.setAccessible(true);
            boolean result = (Boolean) isFull.invoke(ss);
            assertEquals("ScriptSet should evaluate as full", true, result);

        } catch (ClassNotFoundException e) {
            fail("Failed while testing ScriptSet: " + e.getClass() + ": " + e.getMessage());
        } catch (InstantiationException e) {
            fail("Failed while testing ScriptSet: " + e.getClass() + ": " + e.getMessage());
        } catch (IllegalAccessException e) {
            fail("Failed while testing ScriptSet: " + e.getClass() + ": " + e.getMessage());
        } catch (SecurityException e) {
            fail("Failed while testing ScriptSet: " + e.getClass() + ": " + e.getMessage());
        } catch (NoSuchMethodException e) {
            fail("Failed while testing ScriptSet: " + e.getClass() + ": " + e.getMessage());
        } catch (IllegalArgumentException e) {
            fail("Failed while testing ScriptSet: " + e.getClass() + ": " + e.getMessage());
        } catch (InvocationTargetException e) {
            fail("Failed while testing ScriptSet: " + e.getClass() + ": " + e.getMessage());
        }
    }

    @Test
    public void testCopyConstructor() {
        SpoofChecker sc1 = new SpoofChecker.Builder()
                .setAllowedChars(SpoofChecker.RECOMMENDED)
                .setChecks(SpoofChecker.ALL_CHECKS &~ SpoofChecker.INVISIBLE)
                .build();
        SpoofChecker sc2 = new SpoofChecker.Builder(sc1).build();
        assertEquals("Copy constructor should produce identical instances", sc1, sc2);
    }
}
