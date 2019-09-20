/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.format;

import java.util.Locale;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.RuleBasedNumberFormat;

public class RbnfRoundTripTest extends TestFmwk {
    /**
     * Perform an exhaustive round-trip test on the English spellout rules
     */
    @Test
    public void TestEnglishSpelloutRT() {
        RuleBasedNumberFormat formatter
                        = new RuleBasedNumberFormat(Locale.US,
                        RuleBasedNumberFormat.SPELLOUT);

        doTest(formatter, -12345678, 12345678);
    }

    /**
     * Perform an exhaustive round-trip test on the duration-formatting rules
     */
    @Test
    public void TestDurationsRT() {
        RuleBasedNumberFormat formatter
                        = new RuleBasedNumberFormat(Locale.US,
                        RuleBasedNumberFormat.DURATION);

        doTest(formatter, 0, 12345678);
    }

    /**
     * Perform an exhaustive round-trip test on the Spanish spellout rules
     */
    @Test
    public void TestSpanishSpelloutRT() {
        RuleBasedNumberFormat formatter
                        = new RuleBasedNumberFormat(new Locale("es", "es",
                        ""), RuleBasedNumberFormat.SPELLOUT);

        doTest(formatter, -12345678, 12345678);
    }

    /**
     * Perform an exhaustive round-trip test on the French spellout rules
     */
    @Test
    public void TestFrenchSpelloutRT() {
        RuleBasedNumberFormat formatter
                        = new RuleBasedNumberFormat(Locale.FRANCE,
                        RuleBasedNumberFormat.SPELLOUT);

        doTest(formatter, -12345678, 12345678);
    }

    /**
     * Perform an exhaustive round-trip test on the Swiss French spellout rules
     */
    @Test
    public void TestSwissFrenchSpelloutRT() {
        RuleBasedNumberFormat formatter
                        = new RuleBasedNumberFormat(new Locale("fr", "CH",
                        ""), RuleBasedNumberFormat.SPELLOUT);

        doTest(formatter, -12345678, 12345678);
    }

    /**
     * Perform an exhaustive round-trip test on the Italian spellout rules
     */
    @Test
    public void TestItalianSpelloutRT() {
        RuleBasedNumberFormat formatter
                        = new RuleBasedNumberFormat(Locale.ITALIAN,
                        RuleBasedNumberFormat.SPELLOUT);

        doTest(formatter, -999999, 999999);
    }

    /**
     * Perform an exhaustive round-trip test on the German spellout rules
     */
    @Test
    public void TestGermanSpelloutRT() {
        RuleBasedNumberFormat formatter
                        = new RuleBasedNumberFormat(Locale.GERMANY,
                        RuleBasedNumberFormat.SPELLOUT);

        doTest(formatter, 0, 12345678);
    }

    /**
     * Perform an exhaustive round-trip test on the Swedish spellout rules
     */
    @Test
    public void TestSwedishSpelloutRT() {
        RuleBasedNumberFormat formatter
                        = new RuleBasedNumberFormat(new Locale("sv", "SE",
                        ""), RuleBasedNumberFormat.SPELLOUT);

        doTest(formatter, 0, 12345678);
    }

    /**
     * Perform an exhaustive round-trip test on the Dutch spellout rules
     */
    @Test
    public void TestDutchSpelloutRT() {
        RuleBasedNumberFormat formatter
                        = new RuleBasedNumberFormat(new Locale("nl", "NL",
                        ""), RuleBasedNumberFormat.SPELLOUT);

        doTest(formatter, -12345678, 12345678);
    }

    /**
     * Perform an exhaustive round-trip test on the Japanese spellout rules
     */
    @Test
    public void TestJapaneseSpelloutRT() {
        RuleBasedNumberFormat formatter
                        = new RuleBasedNumberFormat(Locale.JAPAN,
                        RuleBasedNumberFormat.SPELLOUT);

        doTest(formatter, 0, 12345678);
    }

    /**
     * Perform an exhaustive round-trip test on the Russian spellout rules
     */
    @Test
    public void TestRussianSpelloutRT() {
        RuleBasedNumberFormat formatter
                        = new RuleBasedNumberFormat(new Locale("ru", "RU",
                        ""), RuleBasedNumberFormat.SPELLOUT);

        doTest(formatter, 0, 12345678);
    }

    /**
     * Perform an exhaustive round-trip test on the Greek spellout rules
     */
    @Test
    public void TestGreekSpelloutRT() {
        RuleBasedNumberFormat formatter
                        = new RuleBasedNumberFormat(new Locale("el", "GR",
                        ""), RuleBasedNumberFormat.SPELLOUT);

        doTest(formatter, 0, 12345678);
    }

    /**
     * Perform an exhaustive round-trip test on the Greek spellout rules
     */
    @Test
    public void TestHebrewNumberingRT() {
        RuleBasedNumberFormat formatter
                        = new RuleBasedNumberFormat(new Locale("he", "IL",
                        ""), RuleBasedNumberFormat.NUMBERING_SYSTEM);

        formatter.setDefaultRuleSet("%hebrew");
        doTest(formatter, 0, 12345678);
    }

    void doTest(RuleBasedNumberFormat formatter,  long lowLimit,
                    long highLimit) {
        try {
            long count = 0;
            long increment = 1;
            for (long i = lowLimit; i <= highLimit; i += increment) {
                if (count % 1000 == 0)
                    logln(Long.toString(i));

                if (Math.abs(i) < 5000)
                    increment = 1;
                else if (Math.abs(i) < 500000)
                    increment = 2737;
                else
                    increment = 267437;

                String text = formatter.format(i);
                long rt = formatter.parse(text).longValue();

                if (rt != i) {
                    errln("Round-trip failed: " + i + " -> " + text +
                                    " -> " + rt);
                }

                ++count;
            }

            if (lowLimit < 0) {
                double d = 1.234;
                while (d < 1000) {
                    String text = formatter.format(d);
                    double rt = formatter.parse(text).doubleValue();

                    if (rt != d) {
                        errln("Round-trip failed: " + d + " -> " + text +
                                        " -> " + rt);
                    }
                    d *= 10;
                }
            }
        }
        catch (Throwable e) {
            errln("Test failed with exception: " + e.toString());
            e.printStackTrace();
        }
    }
}

