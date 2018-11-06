/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2009-2016, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.format;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.Random;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.text.RbnfScannerProviderImpl;
import android.icu.text.RbnfLenientScannerProvider;
import android.icu.text.RuleBasedNumberFormat;
import android.icu.util.ULocale;

public class RbnfLenientScannerTest extends TestFmwk {
    private static final RbnfLenientScannerProvider provider = new RbnfScannerProviderImpl();

    /**
     * Ensure that the default provider is instantiated and used if none is set
     * and lenient parse is on.
     */
    @Test
    public void TestDefaultProvider() {
        RuleBasedNumberFormat formatter
            = new RuleBasedNumberFormat(Locale.US,
                                        RuleBasedNumberFormat.SPELLOUT);
        formatter.setLenientScannerProvider(null);
        formatter.setLenientParseMode(true);
        String[][] lpTestData = {
            { "2 thousand six HUNDRED   fifty-7", "2,657" },
        };

        doLenientParseTest(formatter, lpTestData);
    }

    /**
     * Perform a simple spot check on the English spellout rules
     */
    @Test
    public void TestEnglishSpellout() {
        RuleBasedNumberFormat formatter
            = new RuleBasedNumberFormat(Locale.US,
                                        RuleBasedNumberFormat.SPELLOUT);
        formatter.setLenientScannerProvider(provider);
        formatter.setLenientParseMode(true);
        String[][] lpTestData = {
            { "FOurhundred     thiRTY six", "436" },
            // test spaces before fifty-7 causing lenient parse match of "fifty-" to " fifty"
            // leaving "-7" for remaining parse, resulting in 2643 as the parse result.
            { "fifty-7", "57" },
            { " fifty-7", "57" },
            { "  fifty-7", "57" },
            { "2 thousand six HUNDRED   fifty-7", "2,657" },
            { "fifteen hundred and zero", "1,500" }
        };

        doLenientParseTest(formatter, lpTestData);
    }

    /**
     * Perform a simple spot check on the duration-formatting rules
     */
    @Test
    public void TestDurations() {
        RuleBasedNumberFormat formatter
            = new RuleBasedNumberFormat(Locale.US,
                                        RuleBasedNumberFormat.DURATION);
        formatter.setLenientScannerProvider(provider);
        formatter.setLenientParseMode(true);
        String[][] lpTestData = {
            { "2-51-33", "10,293" }
        };
        doLenientParseTest(formatter, lpTestData);
    }

    /**
     * Perform a simple spot check on the French spellout rules
     */
    @Test
    public void TestFrenchSpellout() {
        RuleBasedNumberFormat formatter
            = new RuleBasedNumberFormat(Locale.FRANCE,
                                        RuleBasedNumberFormat.SPELLOUT);
        formatter.setLenientScannerProvider(provider);
        formatter.setLenientParseMode(true);
        String[][] lpTestData = {
            { "trente-et-un", "31" },
            { "un cent quatre vingt dix huit", "198" }
        };
        doLenientParseTest(formatter, lpTestData);
    }

    /**
     * Perform a simple spot check on the German spellout rules
     */
    @Test
    public void TestGermanSpellout() {
        RuleBasedNumberFormat formatter
            = new RuleBasedNumberFormat(Locale.GERMANY,
                                        RuleBasedNumberFormat.SPELLOUT);
        formatter.setLenientScannerProvider(provider);
        formatter.setLenientParseMode(true);
        String[][] lpTestData = {
            { "ein Tausend sechs Hundert fuenfunddreissig", "1,635" }
        };
        doLenientParseTest(formatter, lpTestData);
    }

    @Test
    public void TestAllLocales() {
        StringBuffer errors = null;
        ULocale[] locales = ULocale.getAvailableLocales();
        String[] names = {
            " (spellout) ",
            " (ordinal)  ",
            " (duration) "
        };
        double[] numbers = {45.678, 1, 2, 10, 11, 100, 110, 200, 1000, 1111, -1111};
        Random r = null;

        // RBNF parse is extremely slow when lenient option is enabled.
        // For non-exhaustive mode, we only test a few locales.
        // "nl_NL", "be" had crash problem reported by #6534
        String[] parseLocales = {"en_US", "nl_NL", "be"};

        for (int i = 0; i < locales.length; ++i) {
            ULocale loc = locales[i];
            int count = numbers.length;
            boolean testParse = true;
            if (TestFmwk.getExhaustiveness() <= 5) {
                testParse = false;
                for (int k = 0; k < parseLocales.length; k++) {
                    if (loc.toString().equals(parseLocales[k])) {
                        testParse = true;
                        break;
                    }
                }
            } else {
                //RBNF parse is too slow.  Increase count only for debugging purpose for now.
                //count = 100;
            }

            for (int j = 0; j < 3; ++j) {
                RuleBasedNumberFormat fmt = new RuleBasedNumberFormat(loc, j+1);

                for (int c = 0; c < count; c++) {
                    double n;
                    if (c < numbers.length) {
                        n = numbers[c];
                    } else {
                        if (r == null) {
                            r = createRandom();
                        }
                        n = ((int)(r.nextInt(10000) - 3000)) / 16d;
                    }

                    String s = fmt.format(n);
                    logln(loc.getName() + names[j] + "success format: " + n + " -> " + s);

                    if (testParse) {
                        // We do not validate the result in this test case,
                        // because there are cases which do not round trip by design.
                        try {
                            // non-lenient parse
                            fmt.setLenientParseMode(false);
                            Number num = fmt.parse(s);
                            logln(loc.getName() + names[j] + "success parse: " + s + " -> " + num);

                            // lenient parse
                            fmt.setLenientScannerProvider(provider);
                            fmt.setLenientParseMode(true);
                            num = fmt.parse(s);
                            logln(loc.getName() + names[j] + "success parse (lenient): " + s + " -> " + num);
                        } catch (ParseException pe) {
                            String msg = loc.getName() + names[j] + "ERROR:" + pe.getMessage();
                            logln(msg);
                            if (errors == null) {
                                errors = new StringBuffer();
                            }
                            errors.append("\n" + msg);
                        }
                    }
                }
            }
        }
        if (errors != null) {
            //TODO: We need to fix parse problems - see #6895 / #6896
            //errln(errors.toString());
            logln(errors.toString());
        }
    }

    void doLenientParseTest(RuleBasedNumberFormat formatter,
                            String[][] testData) {
        NumberFormat decFmt = NumberFormat.getInstance(Locale.US);

        try {
            for (int i = 0; i < testData.length; i++) {
                String words = testData[i][0];
                String expectedNumber = testData[i][1];
                String actualNumber = decFmt.format(formatter.parse(words));

                if (!actualNumber.equals(expectedNumber)) {
                    errln("Lenient-parse spot check failed: for "
                          + words + ", expected " + expectedNumber
                          + ", but got " + actualNumber);
                }
            }
        }
        catch (Throwable e) {
            errln("Test failed with exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
