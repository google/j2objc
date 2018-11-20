/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2006-2016, Google, International Business Machines Corporation
 * and others. All Rights Reserved.
 *******************************************************************************
 */

package android.icu.dev.test.format;

import java.text.ParsePosition;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.PatternTokenizer;
import android.icu.impl.Utility;
import android.icu.text.DateFormat;
import android.icu.text.DateTimePatternGenerator;
import android.icu.text.DateTimePatternGenerator.FormatParser;
import android.icu.text.DateTimePatternGenerator.VariableField;
import android.icu.text.SimpleDateFormat;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import android.icu.util.Calendar;
import android.icu.util.GregorianCalendar;
import android.icu.util.SimpleTimeZone;
import android.icu.util.TimeZone;
import android.icu.util.ULocale;

public class DateTimeGeneratorTest extends TestFmwk {
    public static boolean GENERATE_TEST_DATA;
    static {
        try {
            GENERATE_TEST_DATA = System.getProperty("GENERATE_TEST_DATA") != null;
        } catch (SecurityException e) {
            GENERATE_TEST_DATA = false;
        }
    };
    public static int RANDOM_COUNT = 1000;
    public static boolean DEBUG = false;

    @Test
    public void TestC() {
        String[][] tests = {
                {"zh", "Cm", "Bh:mm"},
                {"de", "Cm", "HH:mm"},
                {"en", "Cm", "h:mm a"},
                {"en-BN", "Cm", "h:mm b"},
                {"gu-IN", "Cm", "h:mm B"},
                {"und-IN", "Cm", "h:mm a"},
        };
        for (String[] test : tests) {
            DateTimePatternGenerator gen = DateTimePatternGenerator.getInstance(ULocale.forLanguageTag(test[0]));
            String skeleton = test[1];
            String pattern = gen.getBestPattern(skeleton);
            assertEquals(test[0] + "/" + skeleton, test[2], pattern);
        }
    }

    @Test
    public void TestSimple() {
        // some simple use cases
        ULocale locale = ULocale.GERMANY;
        TimeZone zone = TimeZone.getTimeZone("Europe/Paris");

        // make from locale
        DateTimePatternGenerator gen = DateTimePatternGenerator.getInstance(locale);
        SimpleDateFormat format = new SimpleDateFormat(gen.getBestPattern("MMMddHmm"), locale);
        format.setTimeZone(zone);
        assertEquals("simple format: MMMddHmm", "14. Okt., 08:58", format.format(sampleDate));
        // (a generator can be built from scratch, but that is not a typical use case)

        // modify the generator by adding patterns
        DateTimePatternGenerator.PatternInfo returnInfo = new DateTimePatternGenerator.PatternInfo();
        gen.addPattern("d'. von' MMMM", true, returnInfo);
        // the returnInfo is mostly useful for debugging problem cases
        format.applyPattern(gen.getBestPattern("MMMMdHmm"));
        assertEquals("modified format: MMMdHmm", "14. von Oktober, 08:58", format.format(sampleDate));

        // get a pattern and modify it
        format = (SimpleDateFormat)DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, locale);
        format.setTimeZone(zone);
        String pattern = format.toPattern();
        assertEquals("full-date", "Donnerstag, 14. Oktober 1999 um 08:58:59 Mitteleurop\u00E4ische Sommerzeit", format.format(sampleDate));

        // modify it to change the zone.
        String newPattern = gen.replaceFieldTypes(pattern, "vvvv");
        format.applyPattern(newPattern);
        assertEquals("full-date: modified zone", "Donnerstag, 14. Oktober 1999 um 08:58:59 Mitteleurop\u00E4ische Zeit", format.format(sampleDate));

        // add test of basic cases

        //lang  YYYYMMM MMMd    MMMdhmm hmm hhmm    Full Date-Time
        // en  Mar 2007    Mar 4   6:05 PM Mar 4   6:05 PM 06:05 PM    Sunday, March 4, 2007 6:05:05 PM PT
        DateTimePatternGenerator enGen = DateTimePatternGenerator.getInstance(ULocale.ENGLISH);
        TimeZone enZone = TimeZone.getTimeZone("Etc/GMT");
        SimpleDateFormat enFormat = (SimpleDateFormat)DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, ULocale.ENGLISH);
        enFormat.setTimeZone(enZone);
        String[][] tests = {
                {"yyyyMMMdd", "Oct 14, 1999"},
                {"yyyyqqqq", "4th quarter 1999"},
                {"yMMMdd", "Oct 14, 1999"},
                {"EyyyyMMMdd", "Thu, Oct 14, 1999"},
                {"yyyyMMdd", "10/14/1999"},
                {"yyyyMMM", "Oct 1999"},
                {"yyyyMM", "10/1999"},
                {"yyMM", "10/99"},
                {"yMMMMMd", "O 14, 1999"},  // narrow format
                {"EEEEEMMMMMd", "T, O 14"},  // narrow format
                {"MMMd", "Oct 14"},
                {"MMMdhmm", "Oct 14, 6:58 AM"},
                {"EMMMdhmms", "Thu, Oct 14, 6:58:59 AM"},
                {"MMdhmm", "10/14, 6:58 AM"},
                {"EEEEMMMdhmms", "Thursday, Oct 14, 6:58:59 AM"},
                {"yyyyMMMddhhmmss", "Oct 14, 1999, 6:58:59 AM"}, // (fixed expected result per ticket 6872<-7180)
                {"EyyyyMMMddhhmmss", "Thu, Oct 14, 1999, 6:58:59 AM"}, // (fixed expected result per ticket 6872<-7180)
                {"hmm", "6:58 AM"},
                {"hhmm", "6:58 AM"}, // (fixed expected result per ticket 6872<-7180)
                {"hhmmVVVV", "6:58 AM GMT"}, // (fixed expected result per ticket 6872<-7180)
        };
        for (int i = 0; i < tests.length; ++i) {
            final String testSkeleton = tests[i][0];
            String pat = enGen.getBestPattern(testSkeleton);
            enFormat.applyPattern(pat);
            String formattedDate = enFormat.format(sampleDate);
            assertEquals("Testing skeleton '" + testSkeleton + "' with  " + sampleDate, tests[i][1], formattedDate);
        }
    }

    @Test
    public void TestRoot() {
        DateTimePatternGenerator rootGen = DateTimePatternGenerator.getInstance(ULocale.ROOT);
        SimpleDateFormat rootFormat = new SimpleDateFormat(rootGen.getBestPattern("yMdHms"), ULocale.ROOT);
        rootFormat.setTimeZone(gmt);
        // *** expected result should be "1999-10-14 6:58:59" with current data, changed test temporarily to match current result, needs investigation
        assertEquals("root format: yMdHms", "1999-10-14 06:58:59", rootFormat.format(sampleDate));
    }

    @Test
    public void TestEmpty() {
        // now nothing
        DateTimePatternGenerator nullGen = DateTimePatternGenerator.getEmptyInstance();
        SimpleDateFormat format = new SimpleDateFormat(nullGen.getBestPattern("yMdHms"), ULocale.ROOT);
        TimeZone rootZone = TimeZone.getTimeZone("Etc/GMT");
        format.setTimeZone(rootZone);
    }

    @Test
    public void TestPatternParser() {
        StringBuffer buffer = new StringBuffer();
        PatternTokenizer pp = new PatternTokenizer()
        .setIgnorableCharacters(new UnicodeSet("[-]"))
        .setSyntaxCharacters(new UnicodeSet("[a-zA-Z]"))
        .setEscapeCharacters(new UnicodeSet("[b#]"))
        .setUsingQuote(true);
        logln("Using Quote");
        for (int i = 0; i < patternTestData.length; ++i) {
            String patternTest = (String) patternTestData[i];
            CheckPattern(buffer, pp, patternTest);
        }
        String[] randomSet = {"abcdef", "$12!@#-", "'\\"};
        for (int i = 0; i < RANDOM_COUNT; ++i) {
            String patternTest = getRandomString(randomSet, 0, 10);
            CheckPattern(buffer, pp, patternTest);
        }
        logln("Using Backslash");
        pp.setUsingQuote(false).setUsingSlash(true);
        for (int i = 0; i < patternTestData.length; ++i) {
            String patternTest = (String) patternTestData[i];
            CheckPattern(buffer, pp, patternTest);
        }
        for (int i = 0; i < RANDOM_COUNT; ++i) {
            String patternTest = getRandomString(randomSet, 0, 10);
            CheckPattern(buffer, pp, patternTest);
        }
    }

    Random random = new java.util.Random(-1);

    private String getRandomString(String[] randomList, int minLen, int maxLen) {
        StringBuffer result = new StringBuffer();
        int len = random.nextInt(maxLen + 1 - minLen) + minLen;
        for (int i = minLen; i < len; ++ i) {
            String source = randomList[random.nextInt(randomList.length)]; // don't bother with surrogates
            char ch = source.charAt(random.nextInt(source.length()));
            UTF16.append(result, ch);
        }
        return result.toString();
    }

    private void CheckPattern(StringBuffer buffer, PatternTokenizer pp, String patternTest) {
        pp.setPattern(patternTest);
        if (DEBUG && isVerbose()) {
            showItems(buffer, pp, patternTest);
        }
        String normalized = pp.setStart(0).normalize();
        logln("input:\t<" + patternTest + ">" + "\tnormalized:\t<" + normalized + ">");
        String doubleNormalized = pp.setPattern(normalized).normalize();
        if (!normalized.equals(doubleNormalized)) {
            errln("Normalization not idempotent:\t" + patternTest + "\tnormalized: " + normalized +  "\tnormalized2: " + doubleNormalized);
            // allow for debugging at the point of failure
            if (DEBUG) {
                pp.setPattern(patternTest);
                normalized = pp.setStart(0).normalize();
                pp.setPattern(normalized);
                showItems(buffer, pp, normalized);
                doubleNormalized = pp.normalize();
            }
        }
    }

    private void showItems(StringBuffer buffer, PatternTokenizer pp, String patternTest) {
        logln("input:\t<" + patternTest + ">");
        while (true) {
            buffer.setLength(0);
            int status = pp.next(buffer);
            if (status == PatternTokenizer.DONE) break;
            String lit = "";
            if (status != PatternTokenizer.SYNTAX ) {
                lit = "\t<" + pp.quoteLiteral(buffer) + ">";
            }
            logln("\t" + statusName[status] + "\t<" + buffer + ">" + lit);
        }
    }

    static final String[] statusName = {"DONE", "SYNTAX", "LITERAL", "BROKEN_QUOTE", "BROKEN_ESCAPE", "UNKNOWN"};

    @Test
    public void TestBasic() {
        ULocale uLocale = null;
        DateTimePatternGenerator dtfg = null;
        Date date = null;
        for (int i = 0; i < dateTestData.length; ++i) {
            if (dateTestData[i] instanceof ULocale) {
                uLocale = (ULocale) dateTestData[i];
                dtfg = DateTimePatternGenerator.getInstance(uLocale);
                if (GENERATE_TEST_DATA) logln("new ULocale(\"" + uLocale.toString() + "\"),");
            } else if (dateTestData[i] instanceof Date) {
                date = (Date) dateTestData[i];
                if (GENERATE_TEST_DATA) logln("new Date(" + date.getTime()+ "L),");
            } else if (dateTestData[i] instanceof String) {
                String testSkeleton = (String) dateTestData[i];
                String pattern = dtfg.getBestPattern(testSkeleton);
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, uLocale);
                String formatted = sdf.format(date);
                if (GENERATE_TEST_DATA) logln("new String[] {\"" + testSkeleton + "\", \"" + Utility.escape(formatted) + "\"},");
                //logln(uLocale + "\t" + testSkeleton + "\t" + pattern + "\t" + sdf.format(date));
            } else {
                String[] testPair = (String[]) dateTestData[i];
                String testSkeleton = testPair[0];
                String testFormatted = testPair[1];
                String pattern = dtfg.getBestPattern(testSkeleton);
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, uLocale);
                String formatted = sdf.format(date);
                if (GENERATE_TEST_DATA) {
                    logln("new String[] {\"" + testSkeleton + "\", \"" + Utility.escape(formatted) + "\"},");
                } else if (!formatted.equals(testFormatted)) {
                    errln(uLocale + "\tformatted string doesn't match test case: " + testSkeleton + "\t generated: " +  pattern + "\t expected: " + testFormatted + "\t got: " + formatted);
                    if (true) { // debug
                        pattern = dtfg.getBestPattern(testSkeleton);
                        sdf = new SimpleDateFormat(pattern, uLocale);
                        formatted = sdf.format(date);
                    }
                }
                //logln(uLocale + "\t" + testSkeleton + "\t" + pattern + "\t" + sdf.format(date));
            }
        }
    }

    static final Object[] patternTestData = {
        "'$f''#c",
        "'' 'a",
        "'.''.'",
        "\\u0061\\\\",
        "mm.dd 'dd ' x",
        "'' ''",
    };

    // can be generated by using GENERATE_TEST_DATA. Must be reviewed before adding
    static final Object[] dateTestData = {
        new Date(916300739123L), // 1999-01-13T23:58:59.123,0-0800

        new ULocale("en_US"),
        new String[] {"yM", "1/1999"},
        new String[] {"yMMM", "Jan 1999"},
        new String[] {"yMd", "1/13/1999"},
        new String[] {"yMMMd", "Jan 13, 1999"},
        new String[] {"Md", "1/13"},
        new String[] {"MMMd", "Jan 13"},
        new String[] {"MMMMd", "January 13"},
        new String[] {"yQQQ", "Q1 1999"},
        new String[] {"hhmm", "11:58 PM"},
        new String[] {"HHmm", "23:58"},
        new String[] {"jjmm", "11:58 PM"},
        new String[] {"mmss", "58:59"},
        new String[] {"yyyyMMMM", "January 1999"}, // (new item for testing 6872<-5702)
        new String[] {"MMMEd", "Wed, Jan 13"},
        new String[] {"Ed", "13 Wed"},
        new String[] {"jmmssSSS", "11:58:59.123 PM"},
        new String[] {"JJmm", "11:58"},

        new ULocale("en_US@calendar=japanese"), // (new locale for testing ticket 6872<-5702)
        new String[] {"yM", "1/11 H"},
        new String[] {"yMMM", "Jan 11 Heisei"},
        new String[] {"yMd", "1/13/11 H"},
        new String[] {"yMMMd", "Jan 13, 11 Heisei"},
        new String[] {"Md", "1/13"},
        new String[] {"MMMd", "Jan 13"},
        new String[] {"MMMMd", "January 13"},
        new String[] {"yQQQ", "Q1 11 Heisei"},
        new String[] {"hhmm", "11:58 PM"},
        new String[] {"HHmm", "23:58"},
        new String[] {"jjmm", "11:58 PM"},
        new String[] {"mmss", "58:59"},
        new String[] {"yyyyMMMM", "January 11 Heisei"},
        new String[] {"MMMEd", "Wed, Jan 13"},
        new String[] {"Ed", "13 Wed"},
        new String[] {"jmmssSSS", "11:58:59.123 PM"},
        new String[] {"JJmm", "11:58"},

        new ULocale("de_DE"),
        new String[] {"yM", "1.1999"},
        new String[] {"yMMM", "Jan. 1999"},
        new String[] {"yMd", "13.1.1999"},
        new String[] {"yMMMd", "13. Jan. 1999"},
        new String[] {"Md", "13.1."},   // 13.1
        new String[] {"MMMd", "13. Jan."},
        new String[] {"MMMMd", "13. Januar"},
        new String[] {"yQQQ", "Q1 1999"},
        new String[] {"hhmm", "11:58 nachm."},
        new String[] {"HHmm", "23:58"},
        new String[] {"jjmm", "23:58"},
        new String[] {"mmss", "58:59"},
        new String[] {"yyyyMMMM", "Januar 1999"}, // (new item for testing 6872<-5702)
        new String[] {"MMMEd", "Mi., 13. Jan."},
        new String[] {"Ed", "Mi., 13."},
        new String[] {"jmmssSSS", "23:58:59,123"},
        new String[] {"JJmm", "23:58"},

        new ULocale("fi"),
        new String[] {"yM", "1.1999"}, // (fixed expected result per ticket 6872<-6626)
        new String[] {"yMMM", "tammi 1999"}, // (fixed expected result per ticket 6872<-7007)
        new String[] {"yMd", "13.1.1999"},
        new String[] {"yMMMd", "13. tammik. 1999"},
        new String[] {"Md", "13.1."},
        new String[] {"MMMd", "13. tammik."},
        new String[] {"MMMMd", "13. tammikuuta"},
        new String[] {"yQQQ", "1. nelj. 1999"},
        new String[] {"hhmm", "11.58 ip."},
        new String[] {"HHmm", "23.58"},
        new String[] {"jjmm", "23.58"},
        new String[] {"mmss", "58.59"},
        new String[] {"yyyyMMMM", "tammikuu 1999"}, // (new item for testing 6872<-5702,7007)
        new String[] {"MMMEd", "ke 13. tammik."},
        new String[] {"Ed", "ke 13."},
        new String[] {"jmmssSSS", "23.58.59,123"},
        new String[] {"JJmm", "23.58"},

        new ULocale("es"),
        new String[] {"yM", "1/1999"},
        new String[] {"yMMM", "ene. 1999"},
        new String[] {"yMd", "13/1/1999"},
        new String[] {"yMMMd", "13 ene. 1999"},
        new String[] {"Md", "13/1"},
        new String[] {"MMMd", "13 ene."},
        new String[] {"MMMMd", "13 de enero"},
        new String[] {"yQQQ", "T1 1999"},
        new String[] {"hhmm", "11:58 p. m."},
        new String[] {"HHmm", "23:58"},
        new String[] {"jjmm", "23:58"},
        new String[] {"mmss", "58:59"},
        new String[] {"yyyyMMMM", "enero de 1999"},
        new String[] {"MMMEd", "mi\u00E9., 13 ene."},
        new String[] {"Ed", "mi\u00E9. 13"},
        new String[] {"jmmssSSS", "23:58:59,123"},
        new String[] {"JJmm", "23:58"},

        new ULocale("ja"), // (new locale for testing ticket 6872<-6626)
        new String[] {"yM", "1999/1"},
        new String[] {"yMMM", "1999\u5E741\u6708"},
        new String[] {"yMd", "1999/1/13"},
        new String[] {"yMMMd", "1999\u5E741\u670813\u65E5"},
        new String[] {"Md", "1/13"},
        new String[] {"MMMd", "1\u670813\u65E5"},
        new String[] {"MMMMd", "1\u670813\u65E5"},
        new String[] {"yQQQ", "1999/Q1"},
        new String[] {"hhmm", "\u5348\u5F8C11:58"},
        new String[] {"HHmm", "23:58"},
        new String[] {"jjmm", "23:58"},
        new String[] {"mmss", "58:59"},
        new String[] {"yyyyMMMM", "1999\u5E741\u6708"}, // (new item for testing 6872<-5702)
        new String[] {"MMMEd", "1\u670813\u65E5(\u6C34)"},
        new String[] {"Ed", "13\u65E5(\u6C34)"},
        new String[] {"jmmssSSS", "23:58:59.123"},
        new String[] {"JJmm", "23:58"},

        new ULocale("ja@calendar=japanese"), // (new locale for testing ticket 6872<-5702)
        new String[] {"yM", "\u5E73\u621011/1"},
        new String[] {"yMMM", "\u5E73\u621011\u5E741\u6708"},
        new String[] {"yMd", "\u5E73\u621011/1/13"},
        new String[] {"yMMMd", "\u5E73\u621011\u5E741\u670813\u65E5"},
        new String[] {"Md", "1/13"},
        new String[] {"MMMd", "1\u670813\u65E5"},
        new String[] {"MMMMd", "1\u670813\u65E5"},
        new String[] {"yQQQ", "\u5E73\u621011/Q1"},
        new String[] {"hhmm", "\u5348\u5F8C11:58"},
        new String[] {"HHmm", "23:58"},
        new String[] {"jjmm", "23:58"},
        new String[] {"mmss", "58:59"},
        new String[] {"yyyyMMMM", "\u5E73\u621011\u5E741\u6708"},
        new String[] {"MMMEd", "1\u670813\u65E5(\u6C34)"},
        new String[] {"Ed", "13\u65E5(\u6C34)"},
        new String[] {"jmmssSSS", "23:58:59.123"},
        new String[] {"JJmm", "23:58"},

        new ULocale("zh_Hans_CN"),
        new String[] {"yM", "1999\u5E741\u6708"},
        new String[] {"yMMM", "1999\u5E741\u6708"}, // (fixed expected result per ticket 6872<-6626)
        new String[] {"yMd", "1999/1/13"},
        new String[] {"yMMMd", "1999\u5E741\u670813\u65E5"}, // (fixed expected result per ticket 6872<-6626)
        new String[] {"Md", "1/13"},
        new String[] {"MMMd", "1\u670813\u65E5"}, // (fixed expected result per ticket 6872<-6626)
        new String[] {"MMMMd", "1\u670813\u65E5"},
        new String[] {"yQQQ", "1999\u5E74\u7B2C1\u5B63\u5EA6"},
        new String[] {"hhmm", "\u4E0B\u534811:58"},
        new String[] {"HHmm", "23:58"},
        new String[] {"jjmm", "\u4E0B\u534811:58"},
        new String[] {"mmss", "58:59"},
        new String[] {"yyyyMMMM", "1999\u5E741\u6708"}, // (new item for testing 6872<-5702)
        new String[] {"MMMEd", "1\u670813\u65E5\u5468\u4E09"},
        new String[] {"Ed", "13\u65E5\u5468\u4E09"},
        new String[] {"jmmssSSS", "\u4E0B\u534811:58:59.123"},
        new String[] {"JJmm", "11:58"},

        new ULocale("zh_TW@calendar=roc"), // (new locale for testing ticket 6872<-5702)
        new String[] {"yM", "\u6C11\u570B88/1"},
        new String[] {"yMMM", "\u6C11\u570B88\u5E741\u6708"},
        new String[] {"yMd", "\u6C11\u570B88/1/13"},
        new String[] {"yMMMd", "\u6C11\u570B88\u5E741\u670813\u65E5"},
        new String[] {"Md", "1/13"},
        new String[] {"MMMd", "1\u670813\u65E5"},
        new String[] {"MMMMd", "1\u670813\u65E5"},
        new String[] {"yQQQ", "\u6C11\u570B88\u5E741\u5B63"},
        new String[] {"hhmm", "\u4E0B\u534811:58"},
        new String[] {"HHmm", "23:58"},
        new String[] {"jjmm", "\u4E0B\u534811:58"},
        new String[] {"mmss", "58:59"},
        new String[] {"yyyyMMMM", "\u6C11\u570B88\u5E741\u6708"},
        new String[] {"MMMEd", "1\u670813\u65E5\u9031\u4E09"},
        new String[] {"Ed", "13 \u9031\u4E09"},
        new String[] {"jmmssSSS", "\u4E0B\u534811:58:59.123"},
        new String[] {"JJmm", "11:58"},

        new ULocale("ru"),
        new String[] {"yM", "01.1999"},
        new String[] {"yMMM", "\u044F\u043D\u0432. 1999 \u0433."},
        new String[] {"yMd", "13.01.1999"},
        new String[] {"yMMMd", "13 \u044F\u043D\u0432. 1999 \u0433."},
        new String[] {"Md", "13.01"},
        new String[] {"MMMd", "13 \u044F\u043D\u0432."},
        new String[] {"MMMMd", "13 \u044F\u043D\u0432\u0430\u0440\u044F"},
        new String[] {"yQQQ", "1-\u0439 \u043A\u0432. 1999 \u0433."},
        new String[] {"hhmm", "11:58 \u041F\u041F"},
        new String[] {"HHmm", "23:58"},
        new String[] {"jjmm", "23:58"},
        new String[] {"mmss", "58:59"},
        new String[] {"yyyyMMMM", "\u044F\u043D\u0432\u0430\u0440\u044C 1999 \u0433."},
        new String[] {"MMMEd", "\u0441\u0440, 13 \u044F\u043D\u0432."},
        new String[] {"Ed", "\u0441\u0440, 13"},
        new String[] {"jmmssSSS", "23:58:59,123"},
        new String[] {"JJmm", "23:58"},

        new ULocale("zh@calendar=chinese"),
        new String[] {"yM", "1998\u620A\u5BC5\u5E74\u5341\u4E00\u6708"},
        new String[] {"yMMM", "1998\u620A\u5BC5\u5E74\u5341\u4E00\u6708"},
        new String[] {"yMd", "1998\u5E74\u5341\u4E00\u670826"},
        new String[] {"yMMMd", "1998\u5E74\u5341\u4E00\u670826"},
        new String[] {"Md", "11-26"},
        new String[] {"MMMd", "\u5341\u4E00\u670826\u65E5"},
        new String[] {"MMMMd", "\u5341\u4E00\u670826\u65E5"},
        new String[] {"yQQQ", "1998\u620A\u5BC5\u5E74\u7B2C\u56DB\u5B63\u5EA6"},
        new String[] {"hhmm", "\u4E0B\u534811:58"},
        new String[] {"HHmm", "23:58"},
        new String[] {"jjmm", "\u4E0B\u534811:58"},
        new String[] {"mmss", "58:59"},
        new String[] {"yyyyMMMM", "1998\u620A\u5BC5\u5E74\u5341\u4E00\u6708"},
        new String[] {"MMMEd", "\u5341\u4E00\u670826\u65E5\u5468\u4E09"},
        new String[] {"Ed", "26\u65E5\u5468\u4E09"},
        new String[] {"jmmssSSS", "\u4E0B\u534811:58:59.123"},
        new String[] {"JJmm", "11:58"},
    };

    @Test
    public void DayMonthTest() {
        final ULocale locale = ULocale.FRANCE;

        // set up the generator
        DateTimePatternGenerator dtpgen
        = DateTimePatternGenerator.getInstance(locale);

        // get a pattern for an abbreviated month and day
        final String pattern = dtpgen.getBestPattern("MMMd");
        SimpleDateFormat formatter = new SimpleDateFormat(pattern, locale);

        // use it to format (or parse)
        String formatted = formatter.format(new Date());
        logln("formatted=" + formatted);
        // for French, the result is "13 sept."
    }

    @Test
    public void TestOrdering() {
        ULocale[] locales = ULocale.getAvailableLocales();
        for (int i = 0; i < locales.length; ++i) {
            for (int style1 = DateFormat.FULL; style1 <= DateFormat.SHORT; ++style1) {
                for (int style2 = DateFormat.FULL; style2 < style1; ++style2) {
                    checkCompatible(style1, style2, locales[i]);
                }
            }
        }
    }

    @Test
    public void TestReplacingZoneString() {
        Date testDate = new Date();
        TimeZone testTimeZone = TimeZone.getTimeZone("America/New_York");
        TimeZone bogusTimeZone = new SimpleTimeZone(1234, "Etc/Unknown");
        Calendar calendar = Calendar.getInstance();
        ParsePosition parsePosition = new ParsePosition(0);

        ULocale[] locales = ULocale.getAvailableLocales();
        int count = 0;
        for (int i = 0; i < locales.length; ++i) {
            // skip the country locales unless we are doing exhaustive tests
            if (getExhaustiveness() < 6) {
                if (locales[i].getCountry().length() > 0) {
                    continue;
                }
            }
            count++;
            // Skipping some test case in the non-exhaustive mode to reduce the test time
            //ticket#6503
            if(getExhaustiveness()<=5 && count%3!=0){
                continue;
            }
            logln(locales[i].toString());
            DateTimePatternGenerator dtpgen
            = DateTimePatternGenerator.getInstance(locales[i]);

            for (int style1 = DateFormat.FULL; style1 <= DateFormat.SHORT; ++style1) {
                final SimpleDateFormat oldFormat = (SimpleDateFormat) DateFormat.getTimeInstance(style1, locales[i]);
                String pattern = oldFormat.toPattern();
                String newPattern = dtpgen.replaceFieldTypes(pattern, "VVVV"); // replaceZoneString(pattern, "VVVV");
                if (newPattern.equals(pattern)) {
                    continue;
                }
                // verify that it roundtrips parsing
                SimpleDateFormat newFormat = new SimpleDateFormat(newPattern, locales[i]);
                newFormat.setTimeZone(testTimeZone);
                String formatted = newFormat.format(testDate);
                calendar.setTimeZone(bogusTimeZone);
                parsePosition.setIndex(0);
                newFormat.parse(formatted, calendar, parsePosition);
                if (parsePosition.getErrorIndex() >= 0) {
                    errln("Failed parse with VVVV:\t" + locales[i] + ",\t\"" + pattern + "\",\t\"" + newPattern + "\",\t\"" + formatted.substring(0,parsePosition.getErrorIndex()) + "{}" + formatted.substring(parsePosition.getErrorIndex()) + "\"");
                } else if (!calendar.getTimeZone().getID().equals(testTimeZone.getID())) {
                    errln("Failed timezone roundtrip with VVVV:\t" + locales[i] + ",\t\"" + pattern + "\",\t\"" + newPattern + "\",\t\"" + formatted + "\",\t" + calendar.getTimeZone().getID() + " != " + testTimeZone.getID());
                } else {
                    logln(locales[i] + ":\t\"" + pattern + "\" => \t\"" + newPattern + "\"\t" + formatted);
                }
            }
        }
    }

    @Test
    public void TestVariableCharacters() {
        UnicodeSet valid = new UnicodeSet("[G y Y u U r Q q M L l w W d D F g E e c a h H K k m s S A z Z O v V X x]");
        for (char c = 0; c < 0xFF; ++c) {
            boolean works = false;
            try {
                VariableField vf = new VariableField(String.valueOf(c), true);
                logln("VariableField " + vf.toString());
                works = true;
            } catch (Exception e) {}
            if (works != valid.contains(c)) {
                if (works) {
                    errln("VariableField can be created with illegal character: " + c);
                } else {
                    errln("VariableField can't be created with legal character: " + c);
                }
            }
        }
    }

    static String[] DATE_STYLE_NAMES = {
        "FULL", "LONG", "MEDIUM", "SHORT"
    };

    /**
     * @param fullOrder
     * @param longOrder
     */
    private void checkCompatible(int style1, int style2, ULocale uLocale) {
        DateOrder order1 = getOrdering(style1, uLocale);
        DateOrder order2 = getOrdering(style2, uLocale);
        if (!order1.hasSameOrderAs(order2)) {
            // Note: This test case was updated by #6806 and no longer reports
            // ordering difference as an error case.
            logln(showOrderComparison(uLocale, style1, style2, order1, order2));
        }
    }

    private String showOrderComparison(ULocale uLocale, int style1, int style2, DateOrder order1, DateOrder order2) {
        String pattern1 = ((SimpleDateFormat) DateFormat.getDateInstance(style1, uLocale)).toPattern();
        String pattern2 = ((SimpleDateFormat) DateFormat.getDateInstance(style2, uLocale)).toPattern();
        return "Mismatch in in ordering for " + uLocale + ": " + DATE_STYLE_NAMES[style1] + ": " + order1 + ", <" + pattern1
                + ">; "
                + DATE_STYLE_NAMES[style2] + ": " + order2 + ", <" + pattern2 + ">; " ;
    }

    /**
     * Main date fields -- Poor-man's enum -- change to real enum when we get JDK 1.5
     */
    public static class DateFieldType {
        private String name;
        private DateFieldType(String string) {
            name = string;
        }

        public static DateFieldType
        YEAR = new DateFieldType("YEAR"),
        MONTH = new DateFieldType("MONTH"),
        DAY = new DateFieldType("DAY");

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Simple struct for output from getOrdering
     */
    static class DateOrder {
        int monthLength;
        DateFieldType[] fields = new DateFieldType[3];

        public boolean isCompatible(DateOrder other) {
            return monthLength == other.monthLength;
        }
        /**
         * @param order2
         * @return
         */
        public boolean hasSameOrderAs(DateOrder other) {
            // TODO Auto-generated method stub
            return fields[0] == other.fields[0] && fields[1] == other.fields[1] && fields[2] == other.fields[2];
        }
        @Override
        public String toString() {
            return "{" + monthLength + ", " + fields[0]  + ", " + fields[1]  + ", " + fields[2] + "}";
        }
        @Override
        public boolean equals(Object that) {
            DateOrder other = (DateOrder) that;
            return monthLength == other.monthLength && fields[0] == other.fields[0] && fields[1] == other.fields[1] && fields[2] == other.fields[2];
        }
    }

    DateTimePatternGenerator.FormatParser formatParser = new DateTimePatternGenerator.FormatParser ();
    DateTimePatternGenerator generator = DateTimePatternGenerator.getEmptyInstance();

    private Calendar sampleCalendar;
    {
        sampleCalendar = new GregorianCalendar(TimeZone.getTimeZone("America/Los_Angeles"));
        sampleCalendar.set(1999, Calendar.OCTOBER, 13, 23, 58, 59);
    }

    private Date sampleDate = sampleCalendar.getTime();
    private TimeZone gmt = TimeZone.getTimeZone("Etc/GMT");

    /**
     * Replace the zone string with a different type, eg v's for z's, etc. <p>Called with a pattern, such as one gotten from
     * <pre>
     * String pattern = ((SimpleDateFormat) DateFormat.getTimeInstance(style, locale)).toPattern();
     * </pre>
     * @param pattern original pattern to change, such as "HH:mm zzzz"
     * @param newZone Must be: z, zzzz, Z, ZZZZ, v, vvvv, V, or VVVV
     * @return
     */
    public String replaceZoneString(String pattern, String newZone) {
        final List itemList = formatParser.set(pattern).getItems();
        boolean changed = false;
        for (int i = 0; i < itemList.size(); ++i) {
            Object item = itemList.get(i);
            if (item instanceof VariableField) {
                VariableField variableField = (VariableField) item;
                if (variableField.getType() == DateTimePatternGenerator.ZONE) {
                    if (!variableField.toString().equals(newZone)) {
                        changed = true;
                        itemList.set(i, new VariableField(newZone, true));
                    }
                }
            }
        }
        return changed ? formatParser.toString() : pattern;
    }

    public boolean containsZone(String pattern) {
        for (Iterator it = formatParser.set(pattern).getItems().iterator(); it.hasNext();) {
            Object item = it.next();
            if (item instanceof VariableField) {
                VariableField variableField = (VariableField) item;
                if (variableField.getType() == DateTimePatternGenerator.ZONE) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the ordering from a particular date format. Best is to use
     * DateFormat.FULL to get the format with String form month (like "January")
     * and DateFormat.SHORT for the numeric format order. They may be different.
     * (Theoretically all 4 formats could be different but that never happens in
     * practice.)
     *
     * @param style
     *          DateFormat.FULL..DateFormat.SHORT
     * @param locale
     *          desired locale.
     * @return
     * @return list of ordered items DateFieldType (I
     *         didn't know what form you really wanted so this is just a
     *         stand-in.)
     */
    private DateOrder getOrdering(int style, ULocale locale) {
        // and the date pattern
        String pattern = ((SimpleDateFormat) DateFormat.getDateInstance(style, locale)).toPattern();
        int count = 0;
        DateOrder result = new DateOrder();

        for (Iterator it = formatParser.set(pattern).getItems().iterator(); it.hasNext();) {
            Object item = it.next();
            if (!(item instanceof String)) {
                // the first character of the variable field determines the type,
                // according to CLDR.
                String variableField = item.toString();
                switch (variableField.charAt(0)) {
                case 'y': case 'Y': case 'u':
                    result.fields[count++] = DateFieldType.YEAR;
                    break;
                case 'M': case 'L':
                    result.monthLength = variableField.length();
                    if (result.monthLength < 2) {
                        result.monthLength = 2;
                    }
                    result.fields[count++] = DateFieldType.MONTH;
                    break;
                case 'd': case 'D': case 'F': case 'g':
                    result.fields[count++] = DateFieldType.DAY;
                    break;
                }
            }
        }
        return result;
    }

    /* Tests the method
     *        public static DateTimePatternGenerator getInstance()
     */
    @Test
    public void TestGetInstance(){
        try{
            DateTimePatternGenerator.getInstance();
        } catch(Exception e){
            errln("DateTimePatternGenerator.getInstance() was not suppose to " +
                    "return an exception.");
        }
    }

    /* Tests the method
     *        public String getSkeleton(String pattern)
     */
    @Test
    public void TestGetSkeleton(){
        DateTimePatternGenerator dtpg = DateTimePatternGenerator.getInstance();
        String[] cases = {"MMDD","MMMDD","MMM-DD","DD/MMM","ddM","MMMMd"};
        String[] results = {"MMDD","MMMDD","MMMDD","MMMDD","Mdd","MMMMd"};
        for(int i=0; i<cases.length; i++){
            if(!dtpg.getSkeleton(cases[i]).equals(results[i])){
                errln("DateTimePatternGenerator.getSkeleton(String) did " +
                        "return the expected result when passing " + cases[i] +
                        " and expected " + results[i] + " but got " +
                        dtpg.getSkeleton(cases[i]));
            }
        }
    }

    /* Tests the method
     *        public String getCanonicalSkeletonAllowingDuplicates(String pattern)
     */
    public void TestGetCanonicalSkeletonAllowingDuplicates(){
        DateTimePatternGenerator dtpg = DateTimePatternGenerator.getInstance();
        String[] cases = {"GyQMwEdaHmsSv","LegH","Legh"};
        String[] results = {"GyQMwEdHmsSv","MEdH","MEdh"};
        for(int i=0; i<cases.length; i++){
            if(!dtpg.getCanonicalSkeletonAllowingDuplicates(cases[i]).equals(results[i])){
                errln("DateTimePatternGenerator.getCanonicalSkeletonAllowingDuplicates(String) did " +
                        "return the expected result when passing " + cases[i] +
                        " and expected " + results[i] + " but got " +
                        dtpg.getCanonicalSkeletonAllowingDuplicates(cases[i]));
            }
        }
    }

    /* Tests the method
     *        public String getBaseSkeleton(String pattern)
     */
    @Test
    public void TestGetBaseSkeleton(){
        DateTimePatternGenerator dtpg = DateTimePatternGenerator.getInstance();
        String[] cases = {"MMDD","MMMDD","MMM-DD","DD/MMM","ddM","MMMMd"};
        String[] results = {"MD","MMMD","MMMD","MMMD","Md","MMMMd"};
        for(int i=0; i<cases.length; i++){
            if(!dtpg.getBaseSkeleton(cases[i]).equals(results[i])){
                errln("DateTimePatternGenerator.getSkeleton(String) did " +
                        "return the expected result when passing " + cases[i] +
                        " and expected " + results[i] + " but got " +
                        dtpg.getBaseSkeleton(cases[i]));
            }
        }
    }

    /* Tests the method
     *        public Map<String, String> getSkeletons(Map<String, String> result)
     */
    @Test
    public void TestGetSkeletons(){
        DateTimePatternGenerator dtpg = DateTimePatternGenerator.getInstance();
        // Tests when "if (result == null)" is true
        try{
            dtpg.getSkeletons(null);
        } catch(Exception e){
            errln("DateTimePatternGenerator.getSkeletons(Map) was suppose to " +
                    "return a new LinkedHashMap for a null parameter.");
        }

        // Tests when "if (result == null)" is false
        Map<String,String> mm = new LinkedHashMap<String, String>();
        try{
            dtpg.getSkeletons(mm);
        } catch(Exception e){
            errln("DateTimePatternGenerator.getSkeletons(Map) was suppose to " +
                    "return a new LinkedHashMap for a LinkedHashMap parameter.");
        }
    }

    /* Tests the method
     *        public Set<String> getBaseSkeletons(Set<String> result)
     */
    @Test
    public void TestGetBaseSkeletons(){
        DateTimePatternGenerator dtpg = DateTimePatternGenerator.getInstance();
        // Tests when "if (result == null)" is true
        try{
            dtpg.getBaseSkeletons(null);
        } catch(Exception e){
            errln("DateTimePatternGenerator.getBaseSkeletons(Map) was suppose to " +
                    "return a new LinkedHashMap for a null parameter.");
        }

        // Tests when "if (result == null)" is false
        Set<String> mm = new HashSet<String>();
        try{
            dtpg.getBaseSkeletons(mm);
        } catch(Exception e){
            errln("DateTimePatternGenerator.getBaseSkeletons(Map) was suppose to " +
                    "return a new LinkedHashMap for a HashSet parameter.");
        }
    }

    /* Tests the method
     *        public String getDecimal()
     */
    @Test
    public void TestGetDecimal(){
        DateTimePatternGenerator dtpg = DateTimePatternGenerator.getInstance();
        if(!dtpg.getDecimal().equals(".")){
            errln("DateTimePatternGenerator.getDecimal() was to return '.' " +
                    "when the object gets a new instance.");
        }

        String[] cases = {",","-","","*","&","a","0"};
        for(int i=0; i<cases.length; i++){
            dtpg.setDecimal(cases[i]);
            if(!dtpg.getDecimal().equals(cases[i])){
                errln("DateTimePatternGenerator.getDecimal() was to return " + cases[i] +
                        "when setting decimal with " + cases[i]);
            }
        }
    }

    /* Tests the method
     *        public Collection<String> getRedundants(Collection<String> output)
     */
    @Test
    public void TestGetRedundants(){
        DateTimePatternGenerator dtpg = DateTimePatternGenerator.getInstance();

        // Tests when "if (output == null)" is true
        try{
            dtpg.getRedundants(null);
        } catch(Exception e){
            errln("DateTimeGenerator.getRedundants was not supposed to return " +
                    "an exception when passing a null parameter: " + e);
        }

        // Tests when "if (output == null)" is false
        try{
            Collection<String> out = new LinkedHashSet<String>();
            dtpg.getRedundants(out);
        } catch(Exception e){
            errln("DateTimeGenerator.getRedundants was not supposed to return " +
                    "an exception when passing a new LinkedHashSet<String>() parameter: " + e);
        }
    }

    /* Tests the method
     *        public String setAppendItemFormat(int field)
     */
    @Test
    public void TestSetAppendItemFormat(){
        DateTimePatternGenerator dtpg = DateTimePatternGenerator.getInstance();
        String[] cases = {"d","u","m","m","y"};
        for(int i=0; i<cases.length; i++){
            dtpg.setAppendItemFormat(i, cases[i]);
            if(!dtpg.getAppendItemFormat(i).equals(cases[i])){
                errln("DateTimePatternGenerator.getAppendItemFormat(int field) " +
                        "did not return as expected. Value set at " + i + " was " +
                        cases[i] + " but got back " + dtpg.getAppendItemFormat(i));
            }
        }
    }

    /* Tests the method
     *        public String getAppendItemFormat(int field)
     */
    @Test
    public void TestGetAppendItemFormat(){
        DateTimePatternGenerator dtpg = DateTimePatternGenerator.getInstance(ULocale.ENGLISH);
        int[] fields = {DateTimePatternGenerator.ERA,DateTimePatternGenerator.DAY,DateTimePatternGenerator.SECOND};
        String[] results = {"{0} {1}","{0} ({2}: {1})","{0} ({2}: {1})"};
        for(int i=0; i<fields.length; i++){
            if(!dtpg.getAppendItemFormat(fields[i]).equals(results[i])){
                errln("DateTimePatternGenerator.getAppendItemFormat(int field) " +
                        "did not return as expected. For field " + fields[i] + ", was expecting " +
                        results[i] + " but got back " + dtpg.getAppendItemFormat(fields[i]));
            }
        }
    }

    /* Tests the method
     *    public String getAppendItemName(int field)
     */
    private final class AppendItemName {
        public int field;
        public String name;
        public AppendItemName(int f, String n) {
            field = f;
            name = n;
        }
    }

    @Test
    public void TestGetAppendItemName(){
        final AppendItemName[] appendItemNames = {
                new AppendItemName( DateTimePatternGenerator.YEAR,    "vuosi" ),
                new AppendItemName( DateTimePatternGenerator.MONTH,   "kuukausi" ),
                new AppendItemName( DateTimePatternGenerator.WEEKDAY, "viikonp\u00E4iv\u00E4" ),
                new AppendItemName( DateTimePatternGenerator.DAY,     "p\u00E4iv\u00E4" ),
                new AppendItemName( DateTimePatternGenerator.HOUR,    "tunti" ),
        };

        DateTimePatternGenerator dtpg = DateTimePatternGenerator.getInstance();
        String[] cases = {"d","u","m","m","y"};
        for(int i=0; i<cases.length; i++){
            dtpg.setAppendItemName(i, cases[i]);
            if(!dtpg.getAppendItemName(i).equals(cases[i])){
                errln("DateTimePatternGenerator.getAppendItemFormat(int field) " +
                        "did not return as expected. Value set at " + i + " was " +
                        cases[i] + " but got back " + dtpg.getAppendItemName(i));
            }
        }

        DateTimePatternGenerator dtpgfi = DateTimePatternGenerator.getInstance(ULocale.forLanguageTag("fi"));
        for (AppendItemName appendItemName: appendItemNames) {
            String name = dtpgfi.getAppendItemName(appendItemName.field);
            if (!name.equals(appendItemName.name)) {
                errln("DateTimePatternGenerator.getAppendItemName returns invalid name for field " + appendItemName.field
                        + ": got " + name + " but expected " + appendItemName.name);
            }
        }
    }

    /* Tests the method
     *    public static boolean isSingleField(String skeleton)
     */
    @SuppressWarnings("static-access")
    @Test
    public void TestIsSingleField(){
        DateTimePatternGenerator dtpg = DateTimePatternGenerator.getInstance();
        String[] cases = {" ", "m","mm","md","mmd","mmdd"};
        boolean[] results = {true,true,true,false,false,false};
        for(int i=0; i<cases.length; i++){
            if(dtpg.isSingleField(cases[i]) != results[i]){
                errln("DateTimePatternGenerator.isSingleField(String skeleton) " +
                        "did not return as expected. Value passed was " + cases[i] +
                        " but got back " + dtpg.isSingleField(cases[i]));
            }
        }
    }

    /* Tests the method
     *    public Object freeze()
     *    public Object cloneAsThawed()
     */
    @Test
    public void TestFreezeAndCloneAsThawed(){
        DateTimePatternGenerator dtpg = DateTimePatternGenerator.getInstance();

        if(dtpg.isFrozen() != false){
            errln("DateTimePatternGenerator.isFrozen() is suppose to return false " +
                    "for a DateTimePatternGenerator object that was just " +
                    "created.");
        }

        dtpg.freeze();
        if(dtpg.isFrozen() != true){
            errln("DateTimePatternGenerator.isFrozen() is suppose to return true " +
                    "for a DateTimePatternGenerator object that was just " +
                    "created and freeze.");
        }

        DateTimePatternGenerator dtpg2 = dtpg.cloneAsThawed();
        if(dtpg.isFrozen() != false){
            errln("DateTimePatternGenerator.isFrozen() is suppose to return false " +
                    "for a DateTimePatternGenerator object that was just " +
                    "clone as thawed.");
        }
        if(dtpg2.isFrozen() != false){
            errln("DateTimePatternGenerator.isFrozen() is suppose to return false " +
                    "for a second DateTimePatternGenerator object that was just " +
                    "clone as thawed.");
        }
    }

    /* Tests the method
     *    public Object clone()
     */
    @Test
    public void TestClone(){
        DateTimePatternGenerator dtpg = DateTimePatternGenerator.getInstance();
        DateTimePatternGenerator dtpg2 = (DateTimePatternGenerator) dtpg.clone();
        dtpg = (DateTimePatternGenerator) dtpg2.clone();
    }

    /* Tests the constructor
     *    public VariableField(String string)
     */
    //TODO(user) why is this "unused"
    @SuppressWarnings("unused")
    @Test
    public void TestVariableField_String(){
        String[] cases = {"d","mm","aa"};
        String[] invalid = {null,"","dummy"};
        for(int i=0; i<cases.length; i++){
            try{
                VariableField vf = new VariableField(cases[i]);
            } catch(Exception e){
                errln("VariableField constructor was not suppose to return " +
                        "an exception when created when passing " + cases[i]);
            }
        }
        for(int i=0; i<invalid.length; i++){
            try{
                VariableField vf = new VariableField(invalid[i]);
                errln("VariableField constructor was suppose to return " +
                        "an exception when created when passing " + invalid[i]);
            } catch(Exception e){}
        }
    }

    /* Tests the method
     *    public FormatParser set(String string, boolean strict)
     */
    @Test
    public void TestSet(){
        FormatParser fp = new FormatParser();
        //Tests when "if (string.length() == 0)" is true
        try{
            fp.set("",true);
        }catch(Exception e){
            errln("FormatParser.set(String,boolean) was not suppose to " +
                    "return an exception.");
        }
    }

    /* Tests the method
     *    public String toString()
     */
    @Test
    public void TestToString(){
        FormatParser fp = new FormatParser();
        if(!fp.toString().equals("")){
            errln("FormatParser.toString() was suppose to return an " +
                    "empty string for a new FormatParser object.");
        }

        String[] cases = {"m","d","y","mm","mmm","mm dd","mm':'dd","mm-dd-yyyy"};
        String[] results = {"m","d","y","mm","mmm","mm dd","mm:dd","mm-dd-yyyy"};
        for(int i=0; i<cases.length; i++){
            fp.set(cases[i]);
            if(!fp.toString().equals(results[i])){
                errln("FormatParser.toString() was suppose to return " + results[i] +
                        " after setting the object. Got: " + fp.toString());
            }
        }
    }

    /* Tests the method
     *    public boolean hasDateAndTimeFields()
     */
    @Test
    public void TestHasDateAndTimeFields(){
        FormatParser fp = new FormatParser();
        if(fp.hasDateAndTimeFields() != false){
            errln("FormatParser.hasDateAndTimeFields() was suppose to return " +
                    "false when a new object is created.");
        }

        String[] cases = {"MMDDYY", "HHMMSS", "", "MM/DD/YYYY HH:MM:SS",
                "MMDDYY HHMMSS", "HHMMSS MMDDYYYY", "HMS MDY"};
        boolean[] results = {false,true,false,true,true,true,true};
        for(int i=0; i<cases.length; i++){
            fp.set(cases[i]);
            if(fp.hasDateAndTimeFields() != results[i]){
                errln("FormatParser.hasDateAndTimeFields() was suppose to " +
                        "return " + results[i] + " but returned " +
                        fp.hasDateAndTimeFields() + " for parameter " +
                        cases[i] + " that is set to FormatParser.");
            }
        }
    }

    /* Tests the method
     *    private void checkFrozen()
     * from public void setDateTimeFormat(String dateTimeFormat)
     */
    @Test
    public void TestCheckFrozen(){
        // Tests when "if (isFrozen())" is true
        DateTimePatternGenerator dt = DateTimePatternGenerator.getInstance();
        try{
            dt.freeze();
            dt.setDateTimeFormat("MMDDYYYY");
            errln("DateTimePatternGenerator.checkFrozen() was suppose to " +
                    "return an exception when trying to setDateTimeFormat " +
                    "for a frozen object.");
        } catch(Exception e){}
        dt = dt.cloneAsThawed();
    }

    /* Tests the method
     *    public String getFields(String pattern)
     */
    @Test
    public void TestGetFields(){
        DateTimePatternGenerator dt = DateTimePatternGenerator.getInstance();
        String[] cases = {"MMDDYY", "HHMMSS", "", "MM/DD/YYYY HH:MM:SS",
                "MMDDYY HHMMSS", "HHMMSS MMDDYYYY", "HMS MDY"};
        String[] results = {"{Month:N}{Day_Of_Year:N}{Year:N}",
                "{Hour:N}{Month:N}{Fractional_Second:N}","",
                "{Month:N}/{Day_Of_Year:N}/{Year:N} {Hour:N}:{Month:N}:{Fractional_Second:N}",
                "{Month:N}{Day_Of_Year:N}{Year:N} {Hour:N}{Month:N}{Fractional_Second:N}",
                "{Hour:N}{Month:N}{Fractional_Second:N} {Month:N}{Day_Of_Year:N}{Year:N}",
        "{Hour:N}{Month:N}{Fractional_Second:N} {Month:N}{Day_Of_Year:N}{Year:N}"};
        for(int i=0; i<cases.length; i++){
            if(!dt.getFields(cases[i]).equals(results[i])) {
                errln("DateTimePatternGenerator.getFields(String) did not " +
                        "not return an expected result when passing " + cases[i] +
                        ". Got " + dt.getFields(cases[i]) + " but expected " +
                        results[i]);
            }
        }
    }

    /*
     * Test case for DateFormatPatternGenerator threading problem #7169
     */
    @Test
    public void TestT7169() {
        Thread[] workers = new Thread[10];
        for (int i = 0 ; i < workers.length; i++) {
            workers[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int i = 0; i < 50; i++) {
                            DateTimePatternGenerator patternGenerator =
                                    DateTimePatternGenerator.getFrozenInstance(ULocale.US);
                            patternGenerator.getBestPattern("MMMMd");
                        }
                    } catch (Exception e) {
                        errln("FAIL: Caught an exception (frozen)" + e);
                    }
                    try {
                        for (int i = 0; i < 50; i++) {
                            DateTimePatternGenerator patternGenerator =
                                    DateTimePatternGenerator.getInstance(ULocale.US);
                            patternGenerator.getBestPattern("MMMMd");
                        }
                    } catch (Exception e) {
                        errln("FAIL: Caught an exception " + e);
                    }
                }
            });
        }
        for (Thread wk : workers) {
            wk.start();
        }
        for (Thread wk : workers) {
            try {
                wk.join();
            } catch (InterruptedException ie) {

            }
        }
    }

    /**
     * Test handling of options
     *
     * For reference, as of ICU 4.3.3,
     *  root/gregorian has
     *      Hm{"H:mm"}
     *      Hms{"H:mm:ss"}
     *      hm{"h:mm a"}
     *      hms{"h:mm:ss a"}
     *  en/gregorian has
     *      Hm{"H:mm"}
     *      Hms{"H:mm:ss"}
     *      hm{"h:mm a"}
     *  be/gregorian has
     *      HHmmss{"HH.mm.ss"}
     *      Hm{"HH.mm"}
     *      hm{"h.mm a"}
     *      hms{"h.mm.ss a"}
     */
    private final class TestOptionsItem {
        public String locale;
        public String skeleton;
        public String expectedPattern;
        public int options;
        // Simple constructor
        public TestOptionsItem(String loc, String skel, String expectedPat, int opts) {
            locale = loc;
            skeleton = skel;
            expectedPattern = expectedPat;
            options = opts;
        }
    }
    @Test
    public void TestOptions() {
        final TestOptionsItem[] testOptionsData = {
                new TestOptionsItem( "en", "Hmm",  "HH:mm",   DateTimePatternGenerator.MATCH_NO_OPTIONS        ),
                new TestOptionsItem( "en", "HHmm", "HH:mm",   DateTimePatternGenerator.MATCH_NO_OPTIONS        ),
                new TestOptionsItem( "en", "hhmm", "h:mm a",  DateTimePatternGenerator.MATCH_NO_OPTIONS        ),
                new TestOptionsItem( "en", "Hmm",  "HH:mm",   DateTimePatternGenerator.MATCH_HOUR_FIELD_LENGTH ),
                new TestOptionsItem( "en", "HHmm", "HH:mm",   DateTimePatternGenerator.MATCH_HOUR_FIELD_LENGTH ),
                new TestOptionsItem( "en", "hhmm", "hh:mm a", DateTimePatternGenerator.MATCH_HOUR_FIELD_LENGTH ),
                new TestOptionsItem( "da", "Hmm",  "HH.mm",   DateTimePatternGenerator.MATCH_NO_OPTIONS        ),
                new TestOptionsItem( "da", "HHmm", "HH.mm",   DateTimePatternGenerator.MATCH_NO_OPTIONS        ),
                new TestOptionsItem( "da", "hhmm", "h.mm a",  DateTimePatternGenerator.MATCH_NO_OPTIONS        ),
                new TestOptionsItem( "da", "Hmm",  "H.mm",    DateTimePatternGenerator.MATCH_HOUR_FIELD_LENGTH ),
                new TestOptionsItem( "da", "HHmm", "HH.mm",   DateTimePatternGenerator.MATCH_HOUR_FIELD_LENGTH ),
                new TestOptionsItem( "da", "hhmm", "hh.mm a", DateTimePatternGenerator.MATCH_HOUR_FIELD_LENGTH ),
                //
                new TestOptionsItem( "en",                   "yyyy",  "yyyy",  DateTimePatternGenerator.MATCH_NO_OPTIONS ),
                new TestOptionsItem( "en",                   "YYYY",  "YYYY",  DateTimePatternGenerator.MATCH_NO_OPTIONS ),
                new TestOptionsItem( "en",                   "U",     "y",     DateTimePatternGenerator.MATCH_NO_OPTIONS ),
                new TestOptionsItem( "en@calendar=japanese", "yyyy",  "y G",   DateTimePatternGenerator.MATCH_NO_OPTIONS ),
                new TestOptionsItem( "en@calendar=japanese", "YYYY",  "Y G",   DateTimePatternGenerator.MATCH_NO_OPTIONS ),
                new TestOptionsItem( "en@calendar=japanese", "U",     "y G",   DateTimePatternGenerator.MATCH_NO_OPTIONS ),
                new TestOptionsItem( "en@calendar=chinese",  "yyyy",  "r(U)",     DateTimePatternGenerator.MATCH_NO_OPTIONS ),
                new TestOptionsItem( "en@calendar=chinese",  "YYYY",  "Y(Y)",     DateTimePatternGenerator.MATCH_NO_OPTIONS ), // not a good result, want r(Y) or r(U)
                new TestOptionsItem( "en@calendar=chinese",  "U",     "r(U)",     DateTimePatternGenerator.MATCH_NO_OPTIONS ),
                new TestOptionsItem( "en@calendar=chinese",  "Gy",    "r(U)",     DateTimePatternGenerator.MATCH_NO_OPTIONS ),
                new TestOptionsItem( "en@calendar=chinese",  "GU",    "r(U)",     DateTimePatternGenerator.MATCH_NO_OPTIONS ),
                new TestOptionsItem( "en@calendar=chinese",  "ULLL",  "MMM U",    DateTimePatternGenerator.MATCH_NO_OPTIONS ),
                new TestOptionsItem( "en@calendar=chinese",  "yMMM",  "MMM r(U)", DateTimePatternGenerator.MATCH_NO_OPTIONS ),
                new TestOptionsItem( "en@calendar=chinese",  "GUMMM", "MMM r(U)", DateTimePatternGenerator.MATCH_NO_OPTIONS ),
                new TestOptionsItem( "zh@calendar=chinese",  "yyyy",  "rU\u5E74",    DateTimePatternGenerator.MATCH_NO_OPTIONS ),
                new TestOptionsItem( "zh@calendar=chinese",  "YYYY",  "YY\u5E74",    DateTimePatternGenerator.MATCH_NO_OPTIONS ), // not a good result, want r(Y) or r(U)
                new TestOptionsItem( "zh@calendar=chinese",  "U",     "rU\u5E74",    DateTimePatternGenerator.MATCH_NO_OPTIONS ),
                new TestOptionsItem( "zh@calendar=chinese",  "Gy",    "rU\u5E74",    DateTimePatternGenerator.MATCH_NO_OPTIONS ),
                new TestOptionsItem( "zh@calendar=chinese",  "GU",    "rU\u5E74",    DateTimePatternGenerator.MATCH_NO_OPTIONS ),
                new TestOptionsItem( "zh@calendar=chinese",  "ULLL",  "U\u5E74MMM",  DateTimePatternGenerator.MATCH_NO_OPTIONS ),
                new TestOptionsItem( "zh@calendar=chinese",  "yMMM",  "rU\u5E74MMM", DateTimePatternGenerator.MATCH_NO_OPTIONS ),
                new TestOptionsItem( "zh@calendar=chinese",  "GUMMM", "rU\u5E74MMM", DateTimePatternGenerator.MATCH_NO_OPTIONS ),
        };

        for (int i = 0; i < testOptionsData.length; ++i) {
            ULocale uloc = new ULocale(testOptionsData[i].locale);
            DateTimePatternGenerator dtpgen = DateTimePatternGenerator.getInstance(uloc);
            String pattern = dtpgen.getBestPattern(testOptionsData[i].skeleton, testOptionsData[i].options);
            if (pattern.compareTo(testOptionsData[i].expectedPattern) != 0) {
                errln("Locale " + testOptionsData[i].locale + ", skeleton " + testOptionsData[i].skeleton +
                        ", options " + ((testOptionsData[i].options != 0)? "!=0": "==0") +
                        ", expected pattern " + testOptionsData[i].expectedPattern + ", got " + pattern);
            }
        }
    }

    /**
     * Test that DTPG can handle all valid pattern character / length combinations
     */
    private final class AllFieldsTestItem {
        public char patternChar;
        public int[] fieldLengths;
        public String mustIncludeOneOf;
        // Simple constructor
        public AllFieldsTestItem(char pC, int[] fL, String mI) {
            patternChar = pC;
            fieldLengths = fL;
            mustIncludeOneOf = mI;
        }
    }

    @Test
    public void TestAllFieldPatterns() {
        String[] localeNames = {
                "root",
                "root@calendar=japanese",
                "root@calendar=chinese",
                "en",
                "en@calendar=japanese",
                "en@calendar=chinese",
        };
        final AllFieldsTestItem[] testItems = {
                //                     pat   fieldLengths             generated pattern must
                //                     chr   to test                  include one of these
                new AllFieldsTestItem( 'G',  new int[]{1,2,3,4,5},    "G"    ), // era
                // year
                new AllFieldsTestItem( 'y',  new int[]{1,2,3,4},      "yU"   ), // year
                new AllFieldsTestItem( 'Y',  new int[]{1,2,3,4},      "Y"    ), // year for week of year
                new AllFieldsTestItem( 'u',  new int[]{1,2,3,4,5},    "yuU"  ), // extended year
                new AllFieldsTestItem( 'U',  new int[]{1,2,3,4,5},    "yU"   ), // cyclic year name
                // quarter
                new AllFieldsTestItem( 'Q',  new int[]{1,2,3,4},      "Qq"   ), // x
                new AllFieldsTestItem( 'q',  new int[]{1,2,3,4},      "Qq"   ), // standalone
                // month
                new AllFieldsTestItem( 'M',  new int[]{1,2,3,4,5},    "ML"   ), // x
                new AllFieldsTestItem( 'L',  new int[]{1,2,3,4,5},    "ML"   ), // standalone
                // week
                new AllFieldsTestItem( 'w',  new int[]{1,2},          "w"    ), // week of year
                new AllFieldsTestItem( 'W',  new int[]{1},            "W"    ), // week of month
                // day
                new AllFieldsTestItem( 'd',  new int[]{1,2},          "d"    ), // day of month
                new AllFieldsTestItem( 'D',  new int[]{1,2,3},        "D"    ), // day of year
                new AllFieldsTestItem( 'F',  new int[]{1},            "F"    ), // day of week in month
                new AllFieldsTestItem( 'g',  new int[]{7},            "g"    ), // modified julian day
                // weekday
                new AllFieldsTestItem( 'E',  new int[]{1,2,3,4,5,6},  "Eec"  ), // day of week
                new AllFieldsTestItem( 'e',  new int[]{1,2,3,4,5,6},  "Eec"  ), // local day of week
                new AllFieldsTestItem( 'c',  new int[]{1,2,3,4,5,6},  "Eec"  ), // standalone local day of week
                // day period
                //  new AllFieldsTestItem( 'a',  new int[]{1},            "a"    ), // am or pm   // not clear this one is supposed to work (it doesn't)
                // hour
                new AllFieldsTestItem( 'h',  new int[]{1,2},          "hK"   ), // 12 (1-12)
                new AllFieldsTestItem( 'H',  new int[]{1,2},          "Hk"   ), // 24 (0-23)
                new AllFieldsTestItem( 'K',  new int[]{1,2},          "hK"   ), // 12 (0-11)
                new AllFieldsTestItem( 'k',  new int[]{1,2},          "Hk"   ), // 24 (1-24)
                new AllFieldsTestItem( 'j',  new int[]{1,2},          "hHKk" ), // locale default
                // minute
                new AllFieldsTestItem( 'm',  new int[]{1,2},          "m"    ), // x
                // second & fractions
                new AllFieldsTestItem( 's',  new int[]{1,2},          "s"    ), // x
                new AllFieldsTestItem( 'S',  new int[]{1,2,3,4},      "S"    ), // fractional second
                new AllFieldsTestItem( 'A',  new int[]{8},            "A"    ), // milliseconds in day
                // zone
                new AllFieldsTestItem( 'z',  new int[]{1,2,3,4},      "z"    ), // x
                new AllFieldsTestItem( 'Z',  new int[]{1,2,3,4,5},    "Z"    ), // x
                new AllFieldsTestItem( 'O',  new int[]{1,4},          "O"    ), // x
                new AllFieldsTestItem( 'v',  new int[]{1,4},          "v"    ), // x
                new AllFieldsTestItem( 'V',  new int[]{1,2,3,4},      "V"    ), // x
                new AllFieldsTestItem( 'X',  new int[]{1,2,3,4,5},    "X"    ), // x
                new AllFieldsTestItem( 'x',  new int[]{1,2,3,4,5},    "x"    ), // x
        };
        final int FIELD_LENGTH_MAX = 8;

        for (String localeName: localeNames) {
            ULocale uloc = new ULocale(localeName);
            DateTimePatternGenerator dtpgen = DateTimePatternGenerator.getInstance(uloc);
            for (AllFieldsTestItem testItem: testItems) {
                char[] skelBuf = new char[FIELD_LENGTH_MAX];
                for (int chrIndx = 0; chrIndx < FIELD_LENGTH_MAX; chrIndx++) {
                    skelBuf[chrIndx] = testItem.patternChar;
                }
                for (int lenIndx = 0; lenIndx < testItem.fieldLengths.length; lenIndx++) {
                    int skelLen = testItem.fieldLengths[lenIndx];
                    if (skelLen > FIELD_LENGTH_MAX) {
                        continue;
                    };
                    String skeleton = new String(skelBuf, 0, skelLen);
                    String pattern = dtpgen.getBestPattern(skeleton);
                    if (pattern.length() <= 0) {
                        errln("DateTimePatternGenerator getBestPattern for locale " + localeName +
                                ", skeleton " + skeleton + ", produces 0-length pattern");
                    } else {
                        // test that resulting pattern has at least one char in mustIncludeOneOf
                        boolean inQuoted = false;
                        int patIndx, patLen = pattern.length();
                        for (patIndx = 0; patIndx < patLen; patIndx++) {
                            char c = pattern.charAt(patIndx);
                            if (c == '\'') {
                                inQuoted = !inQuoted;
                            } else if (!inQuoted && c <= 'z' && c >= 'A') {
                                if (testItem.mustIncludeOneOf.indexOf(c) >= 0) {
                                    break;
                                }
                            }
                        }
                        if (patIndx >= patLen) {
                            errln("DateTimePatternGenerator getBestPattern for locale " + localeName +
                                    ", skeleton " + skeleton +
                                    ", produces pattern without required chars: " + pattern);
                        }
                    }
                }
            }
        }
    }

    @Test
    public void TestJavaLocale() {
        DateTimePatternGenerator genUloc = DateTimePatternGenerator.getInstance(ULocale.GERMANY);
        DateTimePatternGenerator genLoc = DateTimePatternGenerator.getInstance(Locale.GERMANY);

        final String pat = "yMdHms";
        String patUloc = genUloc.getBestPattern(pat);
        String patLoc = genLoc.getBestPattern(pat);

        assertEquals("German pattern 'yMdHms' - getInstance with Java Locale", patUloc, patLoc);
    }

    /* Tests the method
     *    public static int getAppendFormatNumber(String string)
     */
    @Test
    public void TestGetAppendFormatNumber(){
        int fieldNum;
        fieldNum = DateTimePatternGenerator.getAppendFormatNumber("Era");
        assertEquals("DateTimePatternGenerator.getAppendFormatNumber for Era", 0, fieldNum);
        fieldNum = DateTimePatternGenerator.getAppendFormatNumber("Timezone");
        assertEquals("DateTimePatternGenerator.getAppendFormatNumber for Timezone", 15, fieldNum);
    }

    /*
     * Coverage for methods otherwise not covered by other tests.
     */
    @Test
    public void TestCoverage() {
        DateTimePatternGenerator dtpg;

        // DateTimePatternGenerator#getDefaultHourFormatChar
        // DateTimePatternGenerator#setDefaultHourFormatChar
        {
            dtpg = DateTimePatternGenerator.getEmptyInstance();
            assertEquals("Default hour char on empty instance", 'H', dtpg.getDefaultHourFormatChar());
            dtpg.setDefaultHourFormatChar('e');
            assertEquals("Default hour char after explicit set", 'e', dtpg.getDefaultHourFormatChar());
            dtpg = DateTimePatternGenerator.getInstance(ULocale.ENGLISH);
            assertEquals("Default hour char on populated English instance", 'h', dtpg.getDefaultHourFormatChar());
        }

        // DateTimePatternGenerator#getSkeletonAllowingDuplicates
        // DateTimePatternGenerator#getCanonicalSkeletonAllowingDuplicates
        // DateTimePatternGenerator#getCanonicalChar
        {
            dtpg = DateTimePatternGenerator.getInstance(ULocale.ENGLISH);
            assertEquals("Example skeleton with no duplicate fields", "MMMdd", dtpg.getSkeleton("dd/MMM"));
            assertEquals("Should return same result as getSkeleton with no duplicate fields",
                    dtpg.getSkeleton("dd/MMM"), dtpg.getSkeletonAllowingDuplicates("dd/MMM"));

            try {
                dtpg.getSkeleton("dd/MMM Zz");
                fail("getSkeleton should throw upon duplicate fields");
            } catch(IllegalArgumentException e) {
                assertEquals("getSkeleton should throw upon duplicate fields",
                        "Conflicting fields:\tZ, z\t in dd/MMM Zz", e.getMessage());
            }

            assertEquals("Should not throw upon duplicate fields",
                    "MMMddZ", dtpg.getSkeletonAllowingDuplicates("dd/MMM Zz"));
            assertEquals("Should not throw upon duplicate fields and should return Canonical fields",
                    "MMMddv", dtpg.getCanonicalSkeletonAllowingDuplicates("dd/MMM Zz"));
        }

        // DistanceInfo#toString
        // DateTimePatternGenerator#showMask
        try {
            String actual = invokeToString("android.icu.text.DateTimePatternGenerator$DistanceInfo");
            assertEquals("DistanceInfo toString", "missingFieldMask: , extraFieldMask: ", actual);
        } catch(Exception e) {
            errln("Couldn't call DistanceInfo.toString(): " + e.toString());
        }

        // DateTimePatternGenerator#skeletonsAreSimilar
        // DateTimePatternGenerator#getSet
        {
            dtpg = DateTimePatternGenerator.getInstance(ULocale.ENGLISH);
            assertTrue("Trivial skeletonsAreSimilar", dtpg.skeletonsAreSimilar("MMMdd", "MMMdd"));
            assertTrue("Different number of chars in skeletonsAreSimilar", dtpg.skeletonsAreSimilar("Mddd", "MMMdd"));
            assertFalse("Failure case for skeletonsAreSimilar", dtpg.skeletonsAreSimilar("mmDD", "MMMdd"));
        }
    }

    @Test
    public void TestEmptyInstance() {
        DateTimePatternGenerator dtpg = DateTimePatternGenerator.getEmptyInstance();
        String skeleton = "GrMMd";
        String message = "DTPG getEmptyInstance should not throw exceptions on basic operations and should conform to "
                + "the example in setAppendItemFormat";
        assertEquals(message, "G ├'F7': d┤ ├'F3': MM┤ ├'F1': y┤", dtpg.getBestPattern(skeleton));
        dtpg.addPattern("d-MM-yyyy", false, new DateTimePatternGenerator.PatternInfo());
        assertEquals(message, "d-MM-y ├'F0': G┤", dtpg.getBestPattern(skeleton));
        dtpg.setAppendItemFormat(DateTimePatternGenerator.ERA, "{0}, {1}");
        assertEquals(message, "d-MM-y, G", dtpg.getBestPattern(skeleton));
    }
}
