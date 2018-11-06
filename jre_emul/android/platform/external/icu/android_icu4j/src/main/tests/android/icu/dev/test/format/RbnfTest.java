/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2015, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.format;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.Locale;
import java.util.Random;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.math.BigDecimal;
import android.icu.text.DecimalFormat;
import android.icu.text.DecimalFormatSymbols;
import android.icu.text.DisplayContext;
import android.icu.text.NumberFormat;
import android.icu.text.RuleBasedNumberFormat;
import android.icu.util.ULocale;

/**
 * This does not test lenient parse mode, since testing the default implementation
 * introduces a dependency on collation.  See RbnfLenientScannerTest.
 */
public class RbnfTest extends TestFmwk {
    static String fracRules =
        "%main:\n" +
        // this rule formats the number if it's 1 or more.  It formats
        // the integral part using a DecimalFormat ("#,##0" puts
        // thousands separators in the right places) and the fractional
        // part using %%frac.  If there is no fractional part, it
        // just shows the integral part.
        "    x.0: <#,##0<[ >%%frac>];\n" +
        // this rule formats the number if it's between 0 and 1.  It
        // shows only the fractional part (0.5 shows up as "1/2," not
        // "0 1/2")
        "    0.x: >%%frac>;\n" +
        // the fraction rule set.  This works the same way as the one in the
        // preceding example: We multiply the fractional part of the number
        // being formatted by each rule's base value and use the rule that
        // produces the result closest to 0 (or the first rule that produces 0).
        // Since we only provide rules for the numbers from 2 to 10, we know
        // we'll get a fraction with a denominator between 2 and 10.
        // "<0<" causes the numerator of the fraction to be formatted
        // using numerals
        "%%frac:\n" +
        "    2: 1/2;\n" +
        "    3: <0</3;\n" +
        "    4: <0</4;\n" +
        "    5: <0</5;\n" +
        "    6: <0</6;\n" +
        "    7: <0</7;\n" +
        "    8: <0</8;\n" +
        "    9: <0</9;\n" +
        "   10: <0</10;\n";

    @Test
    public void TestCoverage() {
        String durationInSecondsRules =
                // main rule set for formatting with words
                "%with-words:\n"
                        // take care of singular and plural forms of "second"
                        + "    0 seconds; 1 second; =0= seconds;\n"
                        // use %%min to format values greater than 60 seconds
                        + "    60/60: <%%min<[, >>];\n"
                        // use %%hr to format values greater than 3,600 seconds
                        // (the ">>>" below causes us to see the number of minutes
                        // when when there are zero minutes)
                        + "    3600/60: <%%hr<[, >>>];\n"
                        // this rule set takes care of the singular and plural forms
                        // of "minute"
                        + "%%min:\n"
                        + "    0 minutes; 1 minute; =0= minutes;\n"
                        // this rule set takes care of the singular and plural forms
                        // of "hour"
                        + "%%hr:\n"
                        + "    0 hours; 1 hour; =0= hours;\n"

                        // main rule set for formatting in numerals
                        + "%in-numerals:\n"
                        // values below 60 seconds are shown with "sec."
                        + "    =0= sec.;\n"
                        // higher values are shown with colons: %%min-sec is used for
                        // values below 3,600 seconds...
                        + "    60: =%%min-sec=;\n"
                        // ...and %%hr-min-sec is used for values of 3,600 seconds
                        // and above
                        + "    3600: =%%hr-min-sec=;\n"
                        // this rule causes values of less than 10 minutes to show without
                        // a leading zero
                        + "%%min-sec:\n"
                        + "    0: :=00=;\n"
                        + "    60/60: <0<>>;\n"
                        // this rule set is used for values of 3,600 or more.  Minutes are always
                        // shown, and always shown with two digits
                        + "%%hr-min-sec:\n"
                        + "    0: :=00=;\n"
                        + "    60/60: <00<>>;\n"
                        + "    3600/60: <#,##0<:>>>;\n"
                        // the lenient-parse rules allow several different characters to be used
                        // as delimiters between hours, minutes, and seconds
                        + "%%lenient-parse:\n"
                        + "    & : = . = ' ' = -;\n";

        // extra calls to boost coverage numbers
        RuleBasedNumberFormat fmt0 = new RuleBasedNumberFormat(RuleBasedNumberFormat.SPELLOUT);
        RuleBasedNumberFormat fmt1 = (RuleBasedNumberFormat)fmt0.clone();
        RuleBasedNumberFormat fmt2 = new RuleBasedNumberFormat(RuleBasedNumberFormat.SPELLOUT);
        if (!fmt0.equals(fmt0)) {
            errln("self equality fails");
        }
        if (!fmt0.equals(fmt1)) {
            errln("clone equality fails");
        }
        if (!fmt0.equals(fmt2)) {
            errln("duplicate equality fails");
        }
        String str = fmt0.toString();
        logln(str);

        RuleBasedNumberFormat fmt3 =  new RuleBasedNumberFormat(durationInSecondsRules);

        if (fmt0.equals(fmt3)) {
            errln("nonequal fails");
        }
        if (!fmt3.equals(fmt3)) {
            errln("self equal 2 fails");
        }
        str = fmt3.toString();
        logln(str);

        String[] names = fmt3.getRuleSetNames();

        try {
            fmt3.setDefaultRuleSet(null);
            fmt3.setDefaultRuleSet("%%foo");
            errln("sdrf %%foo didn't fail");
        }
        catch (Exception e) {
            logln("Got the expected exception");
        }

        try {
            fmt3.setDefaultRuleSet("%bogus");
            errln("sdrf %bogus didn't fail");
        }
        catch (Exception e) {
            logln("Got the expected exception");
        }

        try {
            str = fmt3.format(2.3, names[0]);
            logln(str);
            str = fmt3.format(2.3, "%%foo");
            errln("format double %%foo didn't fail");
        }
        catch (Exception e) {
            logln("Got the expected exception");
        }

        try {
            str = fmt3.format(123L, names[0]);
            logln(str);
            str = fmt3.format(123L, "%%foo");
            errln("format double %%foo didn't fail");
        }
        catch (Exception e) {
            logln("Got the expected exception");
        }

        RuleBasedNumberFormat fmt4 = new RuleBasedNumberFormat(fracRules, Locale.ENGLISH);
        RuleBasedNumberFormat fmt5 = new RuleBasedNumberFormat(fracRules, Locale.ENGLISH);
        str = fmt4.toString();
        logln(str);
        if (!fmt4.equals(fmt5)) {
            errln("duplicate 2 equality failed");
        }
        str = fmt4.format(123L);
        logln(str);
        try {
            Number num = fmt4.parse(str);
            logln(num.toString());
        }
        catch (Exception e) {
            errln("parse caught exception");
        }

        str = fmt4.format(.000123);
        logln(str);
        try {
            Number num = fmt4.parse(str);
            logln(num.toString());
        }
        catch (Exception e) {
            errln("parse caught exception");
        }

        str = fmt4.format(456.000123);
        logln(str);
        try {
            Number num = fmt4.parse(str);
            logln(num.toString());
        }
        catch (Exception e) {
            errln("parse caught exception");
        }
    }

    @Test
    public void TestUndefinedSpellout() {
        Locale greek = new Locale("el", "", "");
        RuleBasedNumberFormat[] formatters = {
                new RuleBasedNumberFormat(greek, RuleBasedNumberFormat.SPELLOUT),
                new RuleBasedNumberFormat(greek, RuleBasedNumberFormat.ORDINAL),
                new RuleBasedNumberFormat(greek, RuleBasedNumberFormat.DURATION),
        };

        String[] data = {
                "0",
                "1",
                "15",
                "20",
                "23",
                "73",
                "88",
                "100",
                "106",
                "127",
                "200",
                "579",
                "1,000",
                "2,000",
                "3,004",
                "4,567",
                "15,943",
                "105,000",
                "2,345,678",
                "-36",
                "-36.91215",
                "234.56789"
        };

        NumberFormat decFormat = NumberFormat.getInstance(Locale.US);
        for (int j = 0; j < formatters.length; ++j) {
            android.icu.text.NumberFormat formatter = formatters[j];
            logln("formatter[" + j + "]");
            for (int i = 0; i < data.length; ++i) {
                try {
                    String result = formatter.format(decFormat.parse(data[i]));
                    logln("[" + i + "] " + data[i] + " ==> " + result);
                }
                catch (Exception e) {
                    errln("formatter[" + j + "], data[" + i + "] " + data[i] + " threw exception " + e.getMessage());
                }
            }
        }
    }

    /**
     * Perform a simple spot check on the English spellout rules
     */
    @Test
    public void TestEnglishSpellout() {
        RuleBasedNumberFormat formatter = new RuleBasedNumberFormat(Locale.US,
                RuleBasedNumberFormat.SPELLOUT);
        String[][] testData = {
                { "1", "one" },
                { "15", "fifteen" },
                { "20", "twenty" },
                { "23", "twenty-three" },
                { "73", "seventy-three" },
                { "88", "eighty-eight" },
                { "100", "one hundred" },
                { "106", "one hundred six" },
                { "127", "one hundred twenty-seven" },
                { "200", "two hundred" },
                { "579", "five hundred seventy-nine" },
                { "1,000", "one thousand" },
                { "2,000", "two thousand" },
                { "3,004", "three thousand four" },
                { "4,567", "four thousand five hundred sixty-seven" },
                { "15,943", "fifteen thousand nine hundred forty-three" },
                { "2,345,678", "two million three hundred forty-five "
                        + "thousand six hundred seventy-eight" },
                { "-36", "minus thirty-six" },
                { "234.567", "two hundred thirty-four point five six seven" }
        };

        doTest(formatter, testData, true);
    }

    /**
     * Perform a simple spot check on the English ordinal-abbreviation rules
     */
    @Test
    public void TestOrdinalAbbreviations() {
        RuleBasedNumberFormat formatter= new RuleBasedNumberFormat(Locale.US,
                RuleBasedNumberFormat.ORDINAL);
        String[][] testData = {
                { "1", "1st" },
                { "2", "2nd" },
                { "3", "3rd" },
                { "4", "4th" },
                { "7", "7th" },
                { "10", "10th" },
                { "11", "11th" },
                { "13", "13th" },
                { "20", "20th" },
                { "21", "21st" },
                { "22", "22nd" },
                { "23", "23rd" },
                { "24", "24th" },
                { "33", "33rd" },
                { "102", "102nd" },
                { "312", "312th" },
                { "12,345", "12,345th" }
        };

        doTest(formatter, testData, false);
    }

    /**
     * Perform a simple spot check on the duration-formatting rules
     */
    @Test
    public void TestDurations() {
        RuleBasedNumberFormat formatter = new RuleBasedNumberFormat(Locale.US,
                RuleBasedNumberFormat.DURATION);
        String[][] testData = {
                { "3,600", "1:00:00" },             //move me and I fail
                { "0", "0 sec." },
                { "1", "1 sec." },
                { "24", "24 sec." },
                { "60", "1:00" },
                { "73", "1:13" },
                { "145", "2:25" },
                { "666", "11:06" },
                //            { "3,600", "1:00:00" },
                { "3,740", "1:02:20" },
                { "10,293", "2:51:33" }
        };

        doTest(formatter, testData, true);
    }

    /**
     * Perform a simple spot check on the Spanish spellout rules
     */
    @Test
    public void TestSpanishSpellout() {
        RuleBasedNumberFormat formatter = new RuleBasedNumberFormat(new Locale("es", "es",
                ""), RuleBasedNumberFormat.SPELLOUT);
        String[][] testData = {
                { "1", "uno" },
                { "6", "seis" },
                { "16", "diecis\u00e9is" },
                { "20", "veinte" },
                { "24", "veinticuatro" },
                { "26", "veintis\u00e9is" },
                { "73", "setenta y tres" },
                { "88", "ochenta y ocho" },
                { "100", "cien" },
                { "106", "ciento seis" },
                { "127", "ciento veintisiete" },
                { "200", "doscientos" },
                { "579", "quinientos setenta y nueve" },
                { "1,000", "mil" },
                { "2,000", "dos mil" },
                { "3,004", "tres mil cuatro" },
                { "4,567", "cuatro mil quinientos sesenta y siete" },
                { "15,943", "quince mil novecientos cuarenta y tres" },
                { "2,345,678", "dos millones trescientos cuarenta y cinco mil "
                        + "seiscientos setenta y ocho"},
                { "-36", "menos treinta y seis" },
                { "234.567", "doscientos treinta y cuatro coma cinco seis siete" }
        };

        doTest(formatter, testData, true);
    }

    /**
     * Perform a simple spot check on the French spellout rules
     */
    @Test
    public void TestFrenchSpellout() {
        RuleBasedNumberFormat formatter = new RuleBasedNumberFormat(Locale.FRANCE,
                RuleBasedNumberFormat.SPELLOUT);
        String[][] testData = {
                { "1", "un" },
                { "15", "quinze" },
                { "20", "vingt" },
                { "21", "vingt-et-un" },
                { "23", "vingt-trois" },
                { "62", "soixante-deux" },
                { "70", "soixante-dix" },
                { "71", "soixante-et-onze" },
                { "73", "soixante-treize" },
                { "80", "quatre-vingts" },
                { "88", "quatre-vingt-huit" },
                { "100", "cent" },
                { "106", "cent six" },
                { "127", "cent vingt-sept" },
                { "200", "deux cents" },
                { "579", "cinq cent soixante-dix-neuf" },
                { "1,000", "mille" },
                { "1,123", "mille cent vingt-trois" },
                { "1,594", "mille cinq cent quatre-vingt-quatorze" },
                { "2,000", "deux mille" },
                { "3,004", "trois mille quatre" },
                { "4,567", "quatre mille cinq cent soixante-sept" },
                { "15,943", "quinze mille neuf cent quarante-trois" },
                { "2,345,678", "deux millions trois cent quarante-cinq mille "
                        + "six cent soixante-dix-huit" },
                { "-36", "moins trente-six" },
                { "234.567", "deux cent trente-quatre virgule cinq six sept" }
        };

        doTest(formatter, testData, true);
    }

    /**
     * Perform a simple spot check on the Swiss French spellout rules
     */
    @Test
    public void TestSwissFrenchSpellout() {
        RuleBasedNumberFormat formatter = new RuleBasedNumberFormat(new Locale("fr", "CH"),
                RuleBasedNumberFormat.SPELLOUT);
        String[][] testData = {
                { "1", "un" },
                { "15", "quinze" },
                { "20", "vingt" },
                { "21", "vingt-et-un" },
                { "23", "vingt-trois" },
                { "62", "soixante-deux" },
                { "70", "septante" },
                { "71", "septante-et-un" },
                { "73", "septante-trois" },
                { "80", "huitante" },
                { "88", "huitante-huit" },
                { "100", "cent" },
                { "106", "cent six" },
                { "127", "cent vingt-sept" },
                { "200", "deux cents" },
                { "579", "cinq cent septante-neuf" },
                { "1,000", "mille" },
                { "1,123", "mille cent vingt-trois" },
                { "1,594", "mille cinq cent nonante-quatre" },
                { "2,000", "deux mille" },
                { "3,004", "trois mille quatre" },
                { "4,567", "quatre mille cinq cent soixante-sept" },
                { "15,943", "quinze mille neuf cent quarante-trois" },
                { "2,345,678", "deux millions trois cent quarante-cinq mille "
                        + "six cent septante-huit" },
                { "-36", "moins trente-six" },
                { "234.567", "deux cent trente-quatre virgule cinq six sept" }
        };

        doTest(formatter, testData, true);
    }

    /**
     * Perform a simple spot check on the Italian spellout rules
     */
    @Test
    public void TestItalianSpellout() {
        RuleBasedNumberFormat formatter = new RuleBasedNumberFormat(Locale.ITALIAN,
                RuleBasedNumberFormat.SPELLOUT);
        String[][] testData = {
                { "1", "uno" },
                { "15", "quindici" },
                { "20", "venti" },
                { "23", "venti\u00ADtr\u00E9" },
                { "73", "settanta\u00ADtr\u00E9" },
                { "88", "ottant\u00ADotto" },
                { "100", "cento" },
                { "106", "cento\u00ADsei" },
                { "108", "cent\u00ADotto" },
                { "127", "cento\u00ADventi\u00ADsette" },
                { "181", "cent\u00ADottant\u00ADuno" },
                { "200", "due\u00ADcento" },
                { "579", "cinque\u00ADcento\u00ADsettanta\u00ADnove" },
                { "1,000", "mille" },
                { "2,000", "due\u00ADmila" },
                { "3,004", "tre\u00ADmila\u00ADquattro" },
                { "4,567", "quattro\u00ADmila\u00ADcinque\u00ADcento\u00ADsessanta\u00ADsette" },
                { "15,943", "quindici\u00ADmila\u00ADnove\u00ADcento\u00ADquaranta\u00ADtr\u00E9" },
                { "-36", "meno trenta\u00ADsei" },
                { "234.567", "due\u00ADcento\u00ADtrenta\u00ADquattro virgola cinque sei sette" }
        };

        doTest(formatter, testData, true);
    }

    /**
     * Perform a simple spot check on the German spellout rules
     */
    @Test
    public void TestGermanSpellout() {
        RuleBasedNumberFormat formatter = new RuleBasedNumberFormat(Locale.GERMANY,
                RuleBasedNumberFormat.SPELLOUT);
        String[][] testData = {
                { "1", "eins" },
                { "15", "f\u00fcnfzehn" },
                { "20", "zwanzig" },
                { "23", "drei\u00ADund\u00ADzwanzig" },
                { "73", "drei\u00ADund\u00ADsiebzig" },
                { "88", "acht\u00ADund\u00ADachtzig" },
                { "100", "ein\u00ADhundert" },
                { "106", "ein\u00ADhundert\u00ADsechs" },
                { "127", "ein\u00ADhundert\u00ADsieben\u00ADund\u00ADzwanzig" },
                { "200", "zwei\u00ADhundert" },
                { "579", "f\u00fcnf\u00ADhundert\u00ADneun\u00ADund\u00ADsiebzig" },
                { "1,000", "ein\u00ADtausend" },
                { "2,000", "zwei\u00ADtausend" },
                { "3,004", "drei\u00ADtausend\u00ADvier" },
                { "4,567", "vier\u00ADtausend\u00ADf\u00fcnf\u00ADhundert\u00ADsieben\u00ADund\u00ADsechzig" },
                { "15,943", "f\u00fcnfzehn\u00ADtausend\u00ADneun\u00ADhundert\u00ADdrei\u00ADund\u00ADvierzig" },
                { "2,345,678", "zwei Millionen drei\u00ADhundert\u00ADf\u00fcnf\u00ADund\u00ADvierzig\u00ADtausend\u00AD"
                        + "sechs\u00ADhundert\u00ADacht\u00ADund\u00ADsiebzig" }
        };

        doTest(formatter, testData, true);
    }

    /**
     * Perform a simple spot check on the Thai spellout rules
     */
    @Test
    public void TestThaiSpellout() {
        RuleBasedNumberFormat formatter = new RuleBasedNumberFormat(new Locale("th", "TH"),
                RuleBasedNumberFormat.SPELLOUT);
        String[][] testData = {
                { "0", "\u0e28\u0e39\u0e19\u0e22\u0e4c" },
                { "1", "\u0e2b\u0e19\u0e36\u0e48\u0e07" },
                { "10", "\u0e2a\u0e34\u0e1a" },
                { "11", "\u0e2a\u0e34\u0e1a\u200b\u0e40\u0e2d\u0e47\u0e14" },
                { "21", "\u0e22\u0e35\u0e48\u200b\u0e2a\u0e34\u0e1a\u200b\u0e40\u0e2d\u0e47\u0e14" },
                { "101", "\u0e2b\u0e19\u0e36\u0e48\u0e07\u200b\u0e23\u0e49\u0e2d\u0e22\u200b\u0e2b\u0e19\u0e36\u0e48\u0e07" },
                { "1.234", "\u0e2b\u0e19\u0e36\u0e48\u0e07\u200b\u0e08\u0e38\u0e14\u200b\u0e2a\u0e2d\u0e07\u0e2a\u0e32\u0e21\u0e2a\u0e35\u0e48" },
                { "21.45", "\u0e22\u0e35\u0e48\u200b\u0e2a\u0e34\u0e1a\u200b\u0e40\u0e2d\u0e47\u0e14\u200b\u0e08\u0e38\u0e14\u200b\u0e2a\u0e35\u0e48\u0e2b\u0e49\u0e32" },
                { "22.45", "\u0e22\u0e35\u0e48\u200b\u0e2a\u0e34\u0e1a\u200b\u0e2a\u0e2d\u0e07\u200b\u0e08\u0e38\u0e14\u200b\u0e2a\u0e35\u0e48\u0e2b\u0e49\u0e32" },
                { "23.45", "\u0e22\u0e35\u0e48\u200b\u0e2a\u0e34\u0e1a\u200b\u0e2a\u0e32\u0e21\u200b\u0e08\u0e38\u0e14\u200b\u0e2a\u0e35\u0e48\u0e2b\u0e49\u0e32" },
                { "123.45", "\u0e2b\u0e19\u0e36\u0e48\u0e07\u200b\u0e23\u0e49\u0e2d\u0e22\u200b\u0e22\u0e35\u0e48\u200b\u0e2a\u0e34\u0e1a\u200b\u0e2a\u0e32\u0e21\u200b\u0e08\u0e38\u0e14\u200b\u0e2a\u0e35\u0e48\u0e2b\u0e49\u0e32" },
                { "12,345.678", "\u0E2B\u0E19\u0E36\u0E48\u0E07\u200b\u0E2B\u0E21\u0E37\u0E48\u0E19\u200b\u0E2A\u0E2D\u0E07\u200b\u0E1E\u0E31\u0E19\u200b\u0E2A\u0E32\u0E21\u200b\u0E23\u0E49\u0E2D\u0E22\u200b\u0E2A\u0E35\u0E48\u200b\u0E2A\u0E34\u0E1A\u200b\u0E2B\u0E49\u0E32\u200b\u0E08\u0E38\u0E14\u200b\u0E2B\u0E01\u0E40\u0E08\u0E47\u0E14\u0E41\u0E1B\u0E14" },
        };

        doTest(formatter, testData, true);
    }

    /**
     * Perform a simple spot check on the ordinal spellout rules
     */
    @Test
    public void TestPluralRules() {
        String enRules = "%digits-ordinal:"
                + "-x: −>>;"
                + "0: =#,##0=$(ordinal,one{st}two{nd}few{rd}other{th})$;";
        RuleBasedNumberFormat enFormatter = new RuleBasedNumberFormat(enRules, ULocale.ENGLISH);
        String[][] enTestData = {
                { "1", "1st" },
                { "2", "2nd" },
                { "3", "3rd" },
                { "4", "4th" },
                { "11", "11th" },
                { "12", "12th" },
                { "13", "13th" },
                { "14", "14th" },
                { "21", "21st" },
                { "22", "22nd" },
                { "23", "23rd" },
                { "24", "24th" },
        };

        doTest(enFormatter, enTestData, true);

        // This is trying to model the feminine form, but don't worry about the details too much.
        // We're trying to test the plural rules.
        String ruRules = "%spellout-numbering:"
                + "-x: минус >>;"
                + "x.x: [<< $(cardinal,one{целый}other{целых})$ ]>%%fractions-feminine>;"
                + "0: ноль;"
                + "1: один;"
                + "2: два;"
                + "3: три;"
                + "4: четыре;"
                + "5: пять;"
                + "6: шесть;"
                + "7: семь;"
                + "8: восемь;"
                + "9: девять;"
                + "10: десять;"
                + "11: одиннадцать;"
                + "12: двенадцать;"
                + "13: тринадцать;"
                + "14: четырнадцать;"
                + "15: пятнадцать;"
                + "16: шестнадцать;"
                + "17: семнадцать;"
                + "18: восемнадцать;"
                + "19: девятнадцать;"
                + "20: двадцать[ >>];"
                + "30: тридцать[ >>];"
                + "40: сорок[ >>];"
                + "50: пятьдесят[ >>];"
                + "60: шестьдесят[ >>];"
                + "70: семьдесят[ >>];"
                + "80: восемьдесят[ >>];"
                + "90: девяносто[ >>];"
                + "100: сто[ >>];"
                + "200: <<сти[ >>];"
                + "300: <<ста[ >>];"
                + "500: <<сот[ >>];"
                + "1000: << $(cardinal,one{тысяча}few{тысячи}other{тысяч})$[ >>];"
                + "1000000: << $(cardinal,one{миллион}few{миллионы}other{миллионов})$[ >>];"
                + "%%fractions-feminine:"
                + "10: <%spellout-numbering< $(cardinal,one{десятая}other{десятых})$;"
                + "100: <%spellout-numbering< $(cardinal,one{сотая}other{сотых})$;";
        RuleBasedNumberFormat ruFormatter = new RuleBasedNumberFormat(ruRules, new ULocale("ru"));
        String[][] ruTestData = {
                { "1", "один" },
                { "100", "сто" },
                { "125", "сто двадцать пять" },
                { "399", "триста девяносто девять" },
                { "1,000", "один тысяча" },
                { "1,001", "один тысяча один" },
                { "2,000", "два тысячи" },
                { "2,001", "два тысячи один" },
                { "2,002", "два тысячи два" },
                { "3,333", "три тысячи триста тридцать три" },
                { "5,000", "пять тысяч" },
                { "11,000", "одиннадцать тысяч" },
                { "21,000", "двадцать один тысяча" },
                { "22,000", "двадцать два тысячи" },
                { "25,001", "двадцать пять тысяч один" },
                { "0.1", "один десятая" },
                { "0.2", "два десятых" },
                { "0.21", "двадцать один сотая" },
                { "0.22", "двадцать два сотых" },
                { "21.1", "двадцать один целый один десятая" },
                { "22.2", "двадцать два целых два десятых" },
        };

        doTest(ruFormatter, ruTestData, true);

        // Make sure there are no divide by 0 errors.
        String result = new RuleBasedNumberFormat(ruRules, new ULocale("ru")).format(21000);
        if (!"двадцать один тысяча".equals(result)) {
            errln("Got " + result + " for 21000");
        }
    }

    /**
     * Perform a simple spot check on the parsing going into an infinite loop for alternate rules.
     */
    @Test
    public void TestMultiplePluralRules() {
        // This is trying to model the feminine form, but don't worry about the details too much.
        // We're trying to test the plural rules where there are different prefixes.
        String ruRules = "%spellout-cardinal-feminine-genitive:"
                + "-x: минус >>;"
                + "x.x: << запятая >>;"
                + "0: ноля;"
                + "1: одной;"
                + "2: двух;"
                + "3: трех;"
                + "4: четырех;"
                + "5: пяти;"
                + "6: шести;"
                + "7: семи;"
                + "8: восьми;"
                + "9: девяти;"
                + "10: десяти;"
                + "11: одиннадцати;"
                + "12: двенадцати;"
                + "13: тринадцати;"
                + "14: четырнадцати;"
                + "15: пятнадцати;"
                + "16: шестнадцати;"
                + "17: семнадцати;"
                + "18: восемнадцати;"
                + "19: девятнадцати;"
                + "20: двадцати[ >>];"
                + "30: тридцати[ >>];"
                + "40: сорока[ >>];"
                + "50: пятидесяти[ >>];"
                + "60: шестидесяти[ >>];"
                + "70: семидесяти[ >>];"
                + "80: восемидесяти[ >>];"
                + "90: девяноста[ >>];"
                + "100: ста[ >>];"
                + "200: <<сот[ >>];"
                + "1000: << $(cardinal,one{тысяча}few{тысячи}other{тысяч})$[ >>];"
                + "1000000: =#,##0=;"
                + "%spellout-cardinal-feminine:"
                + "-x: минус >>;"
                + "x.x: << запятая >>;"
                + "0: ноль;"
                + "1: одна;"
                + "2: две;"
                + "3: три;"
                + "4: четыре;"
                + "5: пять;"
                + "6: шесть;"
                + "7: семь;"
                + "8: восемь;"
                + "9: девять;"
                + "10: десять;"
                + "11: одиннадцать;"
                + "12: двенадцать;"
                + "13: тринадцать;"
                + "14: четырнадцать;"
                + "15: пятнадцать;"
                + "16: шестнадцать;"
                + "17: семнадцать;"
                + "18: восемнадцать;"
                + "19: девятнадцать;"
                + "20: двадцать[ >>];"
                + "30: тридцать[ >>];"
                + "40: сорок[ >>];"
                + "50: пятьдесят[ >>];"
                + "60: шестьдесят[ >>];"
                + "70: семьдесят[ >>];"
                + "80: восемьдесят[ >>];"
                + "90: девяносто[ >>];"
                + "100: сто[ >>];"
                + "200: <<сти[ >>];"
                + "300: <<ста[ >>];"
                + "500: <<сот[ >>];"
                + "1000: << $(cardinal,one{тысяча}few{тысячи}other{тысяч})$[ >>];"
                + "1000000: =#,##0=;";
        RuleBasedNumberFormat ruFormatter = new RuleBasedNumberFormat(ruRules, new ULocale("ru"));
        try {
            Number result;
            if (1000 != (result = ruFormatter.parse(ruFormatter.format(1000))).doubleValue()) {
                errln("RuleBasedNumberFormat did not return the correct value. Got: " + result);
            }
            if (1000 != (result = ruFormatter.parse(ruFormatter.format(1000, "%spellout-cardinal-feminine-genitive"))).doubleValue()) {
                errln("RuleBasedNumberFormat did not return the correct value. Got: " + result);
            }
            if (1000 != (result = ruFormatter.parse(ruFormatter.format(1000, "%spellout-cardinal-feminine"))).doubleValue()) {
                errln("RuleBasedNumberFormat did not return the correct value. Got: " + result);
            }
        }
        catch (ParseException e) {
            errln(e.toString());
        }
    }

    @Test
    public void TestFractionalRuleSet() {
        RuleBasedNumberFormat formatter = new RuleBasedNumberFormat(fracRules,
                Locale.ENGLISH);

        String[][] testData = {
                { "0", "0" },
                { "1", "1" },
                { "10", "10" },
                { ".1", "1/10" },
                { ".11", "1/9" },
                { ".125", "1/8" },
                { ".1428", "1/7" },
                { ".1667", "1/6" },
                { ".2", "1/5" },
                { ".25", "1/4" },
                { ".333", "1/3" },
                { ".5", "1/2" },
                { "1.1", "1 1/10" },
                { "2.11", "2 1/9" },
                { "3.125", "3 1/8" },
                { "4.1428", "4 1/7" },
                { "5.1667", "5 1/6" },
                { "6.2", "6 1/5" },
                { "7.25", "7 1/4" },
                { "8.333", "8 1/3" },
                { "9.5", "9 1/2" },
                { ".2222", "2/9" },
                { ".4444", "4/9" },
                { ".5555", "5/9" },
                { "1.2856", "1 2/7" }
        };
        doTest(formatter, testData, false); // exact values aren't parsable from fractions
    }

    @Test
    public void TestSwedishSpellout()
    {
        Locale locale = new Locale("sv", "", "");
        RuleBasedNumberFormat formatter = new RuleBasedNumberFormat(locale,
                RuleBasedNumberFormat.SPELLOUT);

        String[][] testDataDefault = {
                { "101", "ett\u00ADhundra\u00ADett" },
                { "123", "ett\u00ADhundra\u00ADtjugo\u00ADtre" },
                { "1,001", "et\u00ADtusen ett" },
                { "1,100", "et\u00ADtusen ett\u00ADhundra" },
                { "1,101", "et\u00ADtusen ett\u00ADhundra\u00ADett" },
                { "1,234", "et\u00ADtusen tv\u00e5\u00ADhundra\u00ADtrettio\u00ADfyra" },
                { "10,001", "tio\u00ADtusen ett" },
                { "11,000", "elva\u00ADtusen" },
                { "12,000", "tolv\u00ADtusen" },
                { "20,000", "tjugo\u00ADtusen" },
                { "21,000", "tjugo\u00ADet\u00ADtusen" },
                { "21,001", "tjugo\u00ADet\u00ADtusen ett" },
                { "200,000", "tv\u00e5\u00ADhundra\u00ADtusen" },
                { "201,000", "tv\u00e5\u00ADhundra\u00ADet\u00ADtusen" },
                { "200,200", "tv\u00e5\u00ADhundra\u00ADtusen tv\u00e5\u00ADhundra" },
                { "2,002,000", "tv\u00e5 miljoner tv\u00e5\u00ADtusen" },
                { "12,345,678", "tolv miljoner tre\u00ADhundra\u00ADfyrtio\u00ADfem\u00ADtusen sex\u00ADhundra\u00ADsjuttio\u00AD\u00e5tta" },
                { "123,456.789", "ett\u00ADhundra\u00ADtjugo\u00ADtre\u00ADtusen fyra\u00ADhundra\u00ADfemtio\u00ADsex komma sju \u00e5tta nio" },
                { "-12,345.678", "minus tolv\u00ADtusen tre\u00ADhundra\u00ADfyrtio\u00ADfem komma sex sju \u00e5tta" },
        };

        logln("testing default rules");
        doTest(formatter, testDataDefault, true);

        String[][] testDataNeutrum = {
                { "101", "ett\u00adhundra\u00adett" },
                { "1,001", "et\u00adtusen ett" },
                { "1,101", "et\u00adtusen ett\u00adhundra\u00adett" },
                { "10,001", "tio\u00adtusen ett" },
                { "21,001", "tjugo\u00adet\u00adtusen ett" }
        };

        formatter.setDefaultRuleSet("%spellout-cardinal-neuter");
        logln("testing neutrum rules");
        doTest(formatter, testDataNeutrum, true);

        String[][] testDataYear = {
                { "101", "ett\u00adhundra\u00adett" },
                { "900", "nio\u00adhundra" },
                { "1,001", "et\u00adtusen ett" },
                { "1,100", "elva\u00adhundra" },
                { "1,101", "elva\u00adhundra\u00adett" },
                { "1,234", "tolv\u00adhundra\u00adtrettio\u00adfyra" },
                { "2,001", "tjugo\u00adhundra\u00adett" },
                { "10,001", "tio\u00adtusen ett" }
        };

        formatter.setDefaultRuleSet("%spellout-numbering-year");
        logln("testing year rules");
        doTest(formatter, testDataYear, true);
    }

    @Test
    public void TestBigNumbers() {
        BigInteger bigI = new BigInteger("1234567890", 10);
        StringBuffer buf = new StringBuffer();
        RuleBasedNumberFormat fmt = new RuleBasedNumberFormat(RuleBasedNumberFormat.SPELLOUT);
        fmt.format(bigI, buf, null);
        logln("big int: " + buf.toString());

        buf.setLength(0);
        java.math.BigDecimal bigD = new java.math.BigDecimal(bigI);
        fmt.format(bigD, buf, null);
        logln("big dec: " + buf.toString());
    }

    @Test
    public void TestTrailingSemicolon() {
        String thaiRules =
            "%default:\n" +
            "  -x: \u0e25\u0e1a>>;\n" +
            "  x.x: <<\u0e08\u0e38\u0e14>>>;\n" +
            "  \u0e28\u0e39\u0e19\u0e22\u0e4c; \u0e2b\u0e19\u0e36\u0e48\u0e07; \u0e2a\u0e2d\u0e07; \u0e2a\u0e32\u0e21;\n" +
            "  \u0e2a\u0e35\u0e48; \u0e2b\u0e49\u0e32; \u0e2b\u0e01; \u0e40\u0e08\u0e47\u0e14; \u0e41\u0e1b\u0e14;\n" +
            "  \u0e40\u0e01\u0e49\u0e32; \u0e2a\u0e34\u0e1a; \u0e2a\u0e34\u0e1a\u0e40\u0e2d\u0e47\u0e14;\n" +
            "  \u0e2a\u0e34\u0e1a\u0e2a\u0e2d\u0e07; \u0e2a\u0e34\u0e1a\u0e2a\u0e32\u0e21;\n" +
            "  \u0e2a\u0e34\u0e1a\u0e2a\u0e35\u0e48; \u0e2a\u0e34\u0e1a\u0e2b\u0e49\u0e32;\n" +
            "  \u0e2a\u0e34\u0e1a\u0e2b\u0e01; \u0e2a\u0e34\u0e1a\u0e40\u0e08\u0e47\u0e14;\n" +
            "  \u0e2a\u0e34\u0e1a\u0e41\u0e1b\u0e14; \u0e2a\u0e34\u0e1a\u0e40\u0e01\u0e49\u0e32;\n" +
            "  20: \u0e22\u0e35\u0e48\u0e2a\u0e34\u0e1a[>%%alt-ones>];\n" +
            "  30: \u0e2a\u0e32\u0e21\u0e2a\u0e34\u0e1a[>%%alt-ones>];\n" +
            "  40: \u0e2a\u0e35\u0e48\u0e2a\u0e34\u0e1a[>%%alt-ones>];\n" +
            "  50: \u0e2b\u0e49\u0e32\u0e2a\u0e34\u0e1a[>%%alt-ones>];\n" +
            "  60: \u0e2b\u0e01\u0e2a\u0e34\u0e1a[>%%alt-ones>];\n" +
            "  70: \u0e40\u0e08\u0e47\u0e14\u0e2a\u0e34\u0e1a[>%%alt-ones>];\n" +
            "  80: \u0e41\u0e1b\u0e14\u0e2a\u0e34\u0e1a[>%%alt-ones>];\n" +
            "  90: \u0e40\u0e01\u0e49\u0e32\u0e2a\u0e34\u0e1a[>%%alt-ones>];\n" +
            "  100: <<\u0e23\u0e49\u0e2d\u0e22[>>];\n" +
            "  1000: <<\u0e1e\u0e31\u0e19[>>];\n" +
            "  10000: <<\u0e2b\u0e21\u0e37\u0e48\u0e19[>>];\n" +
            "  100000: <<\u0e41\u0e2a\u0e19[>>];\n" +
            "  1,000,000: <<\u0e25\u0e49\u0e32\u0e19[>>];\n" +
            "  1,000,000,000: <<\u0e1e\u0e31\u0e19\u0e25\u0e49\u0e32\u0e19[>>];\n" +
            "  1,000,000,000,000: <<\u0e25\u0e49\u0e32\u0e19\u0e25\u0e49\u0e32\u0e19[>>];\n" +
            "  1,000,000,000,000,000: =#,##0=;\n" +
            "%%alt-ones:\n" +
            "  \u0e28\u0e39\u0e19\u0e22\u0e4c;\n" +
            "  \u0e40\u0e2d\u0e47\u0e14;\n" +
            "  =%default=;\n ; ;; ";

        RuleBasedNumberFormat formatter = new RuleBasedNumberFormat(thaiRules, new Locale("th", "TH", ""));

        String[][] testData = {
                { "0", "\u0e28\u0e39\u0e19\u0e22\u0e4c" },
                { "1", "\u0e2b\u0e19\u0e36\u0e48\u0e07" },
                { "123.45", "\u0e2b\u0e19\u0e36\u0e48\u0e07\u0e23\u0e49\u0e2d\u0e22\u0e22\u0e35\u0e48\u0e2a\u0e34\u0e1a\u0e2a\u0e32\u0e21\u0e08\u0e38\u0e14\u0e2a\u0e35\u0e48\u0e2b\u0e49\u0e32" }
        };

        doTest(formatter, testData, true);
    }

    @Test
    public void TestSmallValues() {
        String[][] testData = {
                { "0.001", "zero point zero zero one" },
                { "0.0001", "zero point zero zero zero one" },
                { "0.00001", "zero point zero zero zero zero one" },
                { "0.000001", "zero point zero zero zero zero zero one" },
                { "0.0000001", "zero point zero zero zero zero zero zero one" },
                { "0.00000001", "zero point zero zero zero zero zero zero zero one" },
                { "0.000000001", "zero point zero zero zero zero zero zero zero zero one" },
                { "0.0000000001", "zero point zero zero zero zero zero zero zero zero zero one" },
                { "0.00000000001", "zero point zero zero zero zero zero zero zero zero zero zero one" },
                { "0.000000000001", "zero point zero zero zero zero zero zero zero zero zero zero zero one" },
                { "0.0000000000001", "zero point zero zero zero zero zero zero zero zero zero zero zero zero one" },
                { "0.00000000000001", "zero point zero zero zero zero zero zero zero zero zero zero zero zero zero one" },
                { "0.000000000000001", "zero point zero zero zero zero zero zero zero zero zero zero zero zero zero zero one" },
                { "10,000,000.001", "ten million point zero zero one" },
                { "10,000,000.0001", "ten million point zero zero zero one" },
                { "10,000,000.00001", "ten million point zero zero zero zero one" },
                { "10,000,000.000001", "ten million point zero zero zero zero zero one" },
                { "10,000,000.0000001", "ten million point zero zero zero zero zero zero one" },
                { "10,000,000.00000001", "ten million point zero zero zero zero zero zero zero one" },
                { "10,000,000.000000002", "ten million point zero zero zero zero zero zero zero zero two" },
                { "10,000,000", "ten million" },
                { "1,234,567,890.0987654", "one billion two hundred thirty-four million five hundred sixty-seven thousand eight hundred ninety point zero nine eight seven six five four" },
                { "123,456,789.9876543", "one hundred twenty-three million four hundred fifty-six thousand seven hundred eighty-nine point nine eight seven six five four three" },
                { "12,345,678.87654321", "twelve million three hundred forty-five thousand six hundred seventy-eight point eight seven six five four three two one" },
                { "1,234,567.7654321", "one million two hundred thirty-four thousand five hundred sixty-seven point seven six five four three two one" },
                { "123,456.654321", "one hundred twenty-three thousand four hundred fifty-six point six five four three two one" },
                { "12,345.54321", "twelve thousand three hundred forty-five point five four three two one" },
                { "1,234.4321", "one thousand two hundred thirty-four point four three two one" },
                { "123.321", "one hundred twenty-three point three two one" },
                { "0.0000000011754944", "zero point zero zero zero zero zero zero zero zero one one seven five four nine four four" },
                { "0.000001175494351", "zero point zero zero zero zero zero one one seven five four nine four three five one" },
        };

        RuleBasedNumberFormat formatter = new RuleBasedNumberFormat(Locale.US, RuleBasedNumberFormat.SPELLOUT);
        doTest(formatter, testData, true);
    }

    @Test
    public void TestRuleSetDisplayName() {
        /**
         * Spellout rules for U.K. English.
         * I borrow the rule sets for TestRuleSetDisplayName()
         */
        final String ukEnglish =
                "%simplified:\n"
                        + "    -x: minus >>;\n"
                        + "    x.x: << point >>;\n"
                        + "    zero; one; two; three; four; five; six; seven; eight; nine;\n"
                        + "    ten; eleven; twelve; thirteen; fourteen; fifteen; sixteen;\n"
                        + "        seventeen; eighteen; nineteen;\n"
                        + "    20: twenty[->>];\n"
                        + "    30: thirty[->>];\n"
                        + "    40: forty[->>];\n"
                        + "    50: fifty[->>];\n"
                        + "    60: sixty[->>];\n"
                        + "    70: seventy[->>];\n"
                        + "    80: eighty[->>];\n"
                        + "    90: ninety[->>];\n"
                        + "    100: << hundred[ >>];\n"
                        + "    1000: << thousand[ >>];\n"
                        + "    1,000,000: << million[ >>];\n"
                        + "    1,000,000,000,000: << billion[ >>];\n"
                        + "    1,000,000,000,000,000: =#,##0=;\n"
                        + "%alt-teens:\n"
                        + "    =%simplified=;\n"
                        + "    1000>: <%%alt-hundreds<[ >>];\n"
                        + "    10,000: =%simplified=;\n"
                        + "    1,000,000: << million[ >%simplified>];\n"
                        + "    1,000,000,000,000: << billion[ >%simplified>];\n"
                        + "    1,000,000,000,000,000: =#,##0=;\n"
                        + "%%alt-hundreds:\n"
                        + "    0: SHOULD NEVER GET HERE!;\n"
                        + "    10: <%simplified< thousand;\n"
                        + "    11: =%simplified= hundred>%%empty>;\n"
                        + "%%empty:\n"
                        + "    0:;"
                        + "%ordinal:\n"
                        + "    zeroth; first; second; third; fourth; fifth; sixth; seventh;\n"
                        + "        eighth; ninth;\n"
                        + "    tenth; eleventh; twelfth; thirteenth; fourteenth;\n"
                        + "        fifteenth; sixteenth; seventeenth; eighteenth;\n"
                        + "        nineteenth;\n"
                        + "    twentieth; twenty->>;\n"
                        + "    30: thirtieth; thirty->>;\n"
                        + "    40: fortieth; forty->>;\n"
                        + "    50: fiftieth; fifty->>;\n"
                        + "    60: sixtieth; sixty->>;\n"
                        + "    70: seventieth; seventy->>;\n"
                        + "    80: eightieth; eighty->>;\n"
                        + "    90: ninetieth; ninety->>;\n"
                        + "    100: <%simplified< hundredth; <%simplified< hundred >>;\n"
                        + "    1000: <%simplified< thousandth; <%simplified< thousand >>;\n"
                        + "    1,000,000: <%simplified< millionth; <%simplified< million >>;\n"
                        + "    1,000,000,000,000: <%simplified< billionth;\n"
                        + "        <%simplified< billion >>;\n"
                        + "    1,000,000,000,000,000: =#,##0=;"
                        + "%default:\n"
                        + "    -x: minus >>;\n"
                        + "    x.x: << point >>;\n"
                        + "    =%simplified=;\n"
                        + "    100: << hundred[ >%%and>];\n"
                        + "    1000: << thousand[ >%%and>];\n"
                        + "    100,000>>: << thousand[>%%commas>];\n"
                        + "    1,000,000: << million[>%%commas>];\n"
                        + "    1,000,000,000,000: << billion[>%%commas>];\n"
                        + "    1,000,000,000,000,000: =#,##0=;\n"
                        + "%%and:\n"
                        + "    and =%default=;\n"
                        + "    100: =%default=;\n"
                        + "%%commas:\n"
                        + "    ' and =%default=;\n"
                        + "    100: , =%default=;\n"
                        + "    1000: , <%default< thousand, >%default>;\n"
                        + "    1,000,000: , =%default=;"
                        + "%%lenient-parse:\n"
                        + "    & ' ' , ',' ;\n";
        ULocale.setDefault(ULocale.US);
        String[][] localizations = new String[][] {
            /* public rule sets*/
                {"%simplified", "%default", "%ordinal"},
            /* display names in "en_US" locale*/
                {"en_US", "Simplified", "Default", "Ordinal"},
            /* display names in "zh_Hans" locale*/
                {"zh_Hans", "\u7B80\u5316", "\u7F3A\u7701",  "\u5E8F\u5217"},
            /* display names in a fake locale*/
                {"foo_Bar_BAZ", "Simplified", "Default", "Ordinal"}
        };

        //Construct RuleBasedNumberFormat by rule sets and localizations list
        RuleBasedNumberFormat formatter
                = new RuleBasedNumberFormat(ukEnglish, localizations, ULocale.US);
        RuleBasedNumberFormat f2= new RuleBasedNumberFormat(ukEnglish, localizations);
        assertTrue("Check the two formatters' equality", formatter.equals(f2));

        //get displayName by name
        String[] ruleSetNames = formatter.getRuleSetNames();
        for (int i=0; i<ruleSetNames.length; i++) {
            logln("Rule set name: " + ruleSetNames[i]);
            String RSName_defLoc = formatter.getRuleSetDisplayName(ruleSetNames[i]);
            assertEquals("Display name in default locale", localizations[1][i+1], RSName_defLoc);
            String RSName_loc = formatter.getRuleSetDisplayName(ruleSetNames[i], ULocale.CHINA);
            assertEquals("Display name in Chinese", localizations[2][i+1], RSName_loc);
        }

        // getDefaultRuleSetName
        String defaultRS = formatter.getDefaultRuleSetName();
        //you know that the default rule set is %simplified according to rule sets string ukEnglish
        assertEquals("getDefaultRuleSetName", "%simplified", defaultRS);

        //get locales of localizations
        ULocale[] locales = formatter.getRuleSetDisplayNameLocales();
        for (int i=0; i<locales.length; i++) {
            logln(locales[i].getName());
        }

        //get displayNames
        String[] RSNames_defLoc = formatter.getRuleSetDisplayNames();
        for (int i=0; i<RSNames_defLoc.length; i++) {
            assertEquals("getRuleSetDisplayNames in default locale", localizations[1][i+1], RSNames_defLoc[i]);
        }

        String[] RSNames_loc = formatter.getRuleSetDisplayNames(ULocale.UK);
        for (int i=0; i<RSNames_loc.length; i++) {
            assertEquals("getRuleSetDisplayNames in English", localizations[1][i+1], RSNames_loc[i]);
        }

        RSNames_loc = formatter.getRuleSetDisplayNames(ULocale.CHINA);
        for (int i=0; i<RSNames_loc.length; i++) {
            assertEquals("getRuleSetDisplayNames in Chinese", localizations[2][i+1], RSNames_loc[i]);
        }

        RSNames_loc = formatter.getRuleSetDisplayNames(new ULocale("foo_Bar_BAZ"));
        for (int i=0; i<RSNames_loc.length; i++) {
            assertEquals("getRuleSetDisplayNames in fake locale", localizations[3][i+1], RSNames_loc[i]);
        }
    }

    @Test
    public void TestAllLocales() {
        StringBuilder errors = new StringBuilder();
        String[] names = {
                " (spellout) ",
                " (ordinal) "
                //" (duration) " // English only
        };
        double[] numbers = {45.678, 1, 2, 10, 11, 100, 110, 200, 1000, 1111, -1111};
        int count = numbers.length;
        Random r = (count <= numbers.length ? null : createRandom());

        for (ULocale loc : NumberFormat.getAvailableULocales()) {
            for (int j = 0; j < names.length; ++j) {
                RuleBasedNumberFormat fmt = new RuleBasedNumberFormat(loc, j+1);
                if (!loc.equals(fmt.getLocale(ULocale.ACTUAL_LOCALE))) {
                    // Skip the redundancy
                    break;
                }

                for (int c = 0; c < count; c++) {
                    double n;
                    if (c < numbers.length) {
                        n = numbers[c];
                    } else {
                        n = (r.nextInt(10000) - 3000) / 16d;
                    }

                    String s = fmt.format(n);
                    if (isVerbose()) {
                        logln(loc.getName() + names[j] + "success format: " + n + " -> " + s);
                    }

                    try {
                        // RBNF parse is extremely slow when lenient option is enabled.
                        // non-lenient parse
                        fmt.setLenientParseMode(false);
                        Number num = fmt.parse(s);
                        if (isVerbose()) {
                            logln(loc.getName() + names[j] + "success parse: " + s + " -> " + num);
                        }
                        if (j != 0) {
                            // TODO: Fix the ordinal rules.
                            continue;
                        }
                        if (n != num.doubleValue()) {
                            errors.append("\n" + loc + names[j] + "got " + num + " expected " + n);
                        }
                    } catch (ParseException pe) {
                        String msg = loc.getName() + names[j] + "ERROR:" + pe.getMessage();
                        logln(msg);
                        errors.append("\n" + msg);
                    }
                }
            }
        }
        if (errors.length() > 0) {
            errln(errors.toString());
        }
    }

    void doTest(RuleBasedNumberFormat formatter, String[][] testData,
                boolean testParsing) {
        //        NumberFormat decFmt = NumberFormat.getInstance(Locale.US);
        NumberFormat decFmt = new DecimalFormat("#,###.################");
        try {
            for (int i = 0; i < testData.length; i++) {
                String number = testData[i][0];
                String expectedWords = testData[i][1];
                if (isVerbose()) {
                    logln("test[" + i + "] number: " + number + " target: " + expectedWords);
                }
                Number num = decFmt.parse(number);
                String actualWords = formatter.format(num);

                if (!actualWords.equals(expectedWords)) {
                    errln("Spot check format failed: for " + number + ", expected\n    "
                            + expectedWords + ", but got\n    " +
                            actualWords);
                }
                else if (testParsing) {
                    String actualNumber = decFmt.format(formatter
                            .parse(actualWords));

                    if (!actualNumber.equals(number)) {
                        errln("Spot check parse failed: for " + actualWords +
                                ", expected " + number + ", but got " +
                                actualNumber);
                    }
                }
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
            errln("Test failed with exception: " + e.toString());
        }
    }

    /* Tests the method
     *      public boolean equals(Object that)
     */
    @Test
    public void TestEquals(){
        // Tests when "if (!(that instanceof RuleBasedNumberFormat))" is true
        RuleBasedNumberFormat rbnf = new RuleBasedNumberFormat("dummy");
        if (rbnf.equals("dummy") ||
                rbnf.equals(new Character('a')) ||
                rbnf.equals(new Object()) ||
                rbnf.equals(-1) ||
                rbnf.equals(0) ||
                rbnf.equals(1) ||
                rbnf.equals(-1.0) ||
                rbnf.equals(0.0) ||
                rbnf.equals(1.0))
        {
            errln("RuleBasedNumberFormat.equals(Object that) was suppose to " +
                    "be false for an invalid object.");
        }

        // Tests when
        // "if (!locale.equals(that2.locale) || lenientParse != that2.lenientParse)"
        // is true
        RuleBasedNumberFormat rbnf1 = new RuleBasedNumberFormat("dummy", new Locale("en"));
        RuleBasedNumberFormat rbnf2 = new RuleBasedNumberFormat("dummy", new Locale("jp"));
        RuleBasedNumberFormat rbnf3 = new RuleBasedNumberFormat("dummy", new Locale("sp"));
        RuleBasedNumberFormat rbnf4 = new RuleBasedNumberFormat("dummy", new Locale("fr"));

        if(rbnf1.equals(rbnf2) || rbnf1.equals(rbnf3) ||
                rbnf1.equals(rbnf4) || rbnf2.equals(rbnf3) ||
                rbnf2.equals(rbnf4) || rbnf3.equals(rbnf4)){
            errln("RuleBasedNumberFormat.equals(Object that) was suppose to " +
                    "be false for an invalid object.");
        }

        if(!rbnf1.equals(rbnf1)){
            errln("RuleBasedNumberFormat.equals(Object that) was not suppose to " +
                    "be false for an invalid object.");
        }

        if(!rbnf2.equals(rbnf2)){
            errln("RuleBasedNumberFormat.equals(Object that) was not suppose to " +
                    "be false for an invalid object.");
        }

        if(!rbnf3.equals(rbnf3)){
            errln("RuleBasedNumberFormat.equals(Object that) was not suppose to " +
                    "be false for an invalid object.");
        }

        if(!rbnf4.equals(rbnf4)){
            errln("RuleBasedNumberFormat.equals(Object that) was not suppose to " +
                    "be false for an invalid object.");
        }

        RuleBasedNumberFormat rbnf5 = new RuleBasedNumberFormat("dummy", new Locale("en"));
        RuleBasedNumberFormat rbnf6 = new RuleBasedNumberFormat("dummy", new Locale("en"));

        if(!rbnf5.equals(rbnf6)){
            errln("RuleBasedNumberFormat.equals(Object that) was not suppose to " +
                    "be false for an invalid object.");
        }
        rbnf6.setLenientParseMode(true);

        if(rbnf5.equals(rbnf6)){
            errln("RuleBasedNumberFormat.equals(Object that) was suppose to " +
                    "be false for an invalid object.");
        }

        // Tests when "if (!ruleSets[i].equals(that2.ruleSets[i]))" is true
        RuleBasedNumberFormat rbnf7 = new RuleBasedNumberFormat("not_dummy", new Locale("en"));
        if(rbnf5.equals(rbnf7)){
            errln("RuleBasedNumberFormat.equals(Object that) was suppose to " +
                    "be false for an invalid object.");
        }
    }

    /* Tests the method
     *      public ULocale[] getRuleSetDisplayNameLocales()
     */
    @Test
    public void TestGetRuleDisplayNameLocales(){
        // Tests when "if (ruleSetDisplayNames != null" is false
        RuleBasedNumberFormat rbnf = new RuleBasedNumberFormat("dummy");
        rbnf.getRuleSetDisplayNameLocales();
        if(rbnf.getRuleSetDisplayNameLocales() != null){
            errln("RuleBasedNumberFormat.getRuleDisplayNameLocales() was suppose to " +
                    "return null.");
        }
    }

    /* Tests the method
     *      private String[] getNameListForLocale(ULocale loc)
     *      public String[] getRuleSetDisplayNames(ULocale loc)
     */
    @Test
    public void TestGetNameListForLocale(){
        // Tests when "if (names != null)" is false and
        //  "if (loc != null && ruleSetDisplayNames != null)" is false
        RuleBasedNumberFormat rbnf = new RuleBasedNumberFormat("dummy");
        rbnf.getRuleSetDisplayNames(null);
        try{
            rbnf.getRuleSetDisplayNames(null);
        } catch(Exception e){
            errln("RuleBasedNumberFormat.getRuleSetDisplayNames(ULocale loc) " +
                    "was not suppose to have an exception.");
        }
    }

    /* Tests the method
     *      public String getRuleSetDisplayName(String ruleSetName, ULocale loc)
     */
    @Test
    public void TestGetRulesSetDisplayName(){
        RuleBasedNumberFormat rbnf = new RuleBasedNumberFormat("dummy");
        //rbnf.getRuleSetDisplayName("dummy", new ULocale("en_US"));

        // Tests when "if (names != null) " is true

        // Tests when the method throws an exception
        try{
            rbnf.getRuleSetDisplayName("", new ULocale("en_US"));
            errln("RuleBasedNumberFormat.getRuleSetDisplayName(String ruleSetName, ULocale loc) " +
                    "was suppose to have an exception.");
        } catch(Exception e){}

        try{
            rbnf.getRuleSetDisplayName("dummy", new ULocale("en_US"));
            errln("RuleBasedNumberFormat.getRuleSetDisplayName(String ruleSetName, ULocale loc) " +
                    "was suppose to have an exception.");
        } catch(Exception e){}
    }

    /* Test the method
     *      public void process(StringBuffer buf, NFRuleSet ruleSet)
     */
    @Test
    public void TestChineseProcess(){
        String ruleWithChinese =
            "%simplified:\n"
            + "    -x: minus >>;\n"
            + "    x.x: << point >>;\n"
            + "    zero; one; two; three; four; five; six; seven; eight; nine;\n"
            + "    ten; eleven; twelve; thirteen; fourteen; fifteen; sixteen;\n"
            + "        seventeen; eighteen; nineteen;\n"
            + "    20: twenty[->>];\n"
            + "    30: thirty[->>];\n"
            + "    40: forty[->>];\n"
            + "    50: fifty[->>];\n"
            + "    60: sixty[->>];\n"
            + "    70: seventy[->>];\n"
            + "    80: eighty[->>];\n"
            + "    90: ninety[->>];\n"
            + "    100: << hundred[ >>];\n"
            + "    1000: << thousand[ >>];\n"
            + "    1,000,000: << million[ >>];\n"
            + "    1,000,000,000,000: << billion[ >>];\n"
            + "    1,000,000,000,000,000: =#,##0=;\n"
            + "%alt-teens:\n"
            + "    =%simplified=;\n"
            + "    1000>: <%%alt-hundreds<[ >>];\n"
            + "    10,000: =%simplified=;\n"
            + "    1,000,000: << million[ >%simplified>];\n"
            + "    1,000,000,000,000: << billion[ >%simplified>];\n"
            + "    1,000,000,000,000,000: =#,##0=;\n"
            + "%%alt-hundreds:\n"
            + "    0: SHOULD NEVER GET HERE!;\n"
            + "    10: <%simplified< thousand;\n"
            + "    11: =%simplified= hundred>%%empty>;\n"
            + "%%empty:\n"
            + "    0:;"
            + "%accounting:\n"
            + "    \u842c; \u842c; \u842c; \u842c; \u842c; \u842c; \u842c; \u842c;\n"
            + "        \u842c; \u842c;\n"
            + "    \u842c; \u842c; \u842c; \u842c; \u842c;\n"
            + "        \u842c; \u842c; \u842c; \u842c;\n"
            + "        \u842c;\n"
            + "    twentieth; \u96f6|>>;\n"
            + "    30: \u96f6; \u96f6|>>;\n"
            + "    40: \u96f6; \u96f6|>>;\n"
            + "    50: \u96f6; \u96f6|>>;\n"
            + "    60: \u96f6; \u96f6|>>;\n"
            + "    70: \u96f6; \u96f6|>>;\n"
            + "    80: \u96f6; \u96f6|>>;\n"
            + "    90: \u96f6; \u96f6|>>;\n"
            + "    100: <%simplified< \u96f6; <%simplified< \u96f6 >>;\n"
            + "    1000: <%simplified< \u96f6; <%simplified< \u96f6 >>;\n"
            + "    1,000,000: <%simplified< \u96f6; <%simplified< \u96f6 >>;\n"
            + "    1,000,000,000,000: <%simplified< \u96f6;\n"
            + "        <%simplified< \u96f6 >>;\n"
            + "    1,000,000,000,000,000: =#,##0=;"
            + "%default:\n"
            + "    -x: minus >>;\n"
            + "    x.x: << point >>;\n"
            + "    =%simplified=;\n"
            + "    100: << hundred[ >%%and>];\n"
            + "    1000: << thousand[ >%%and>];\n"
            + "    100,000>>: << thousand[>%%commas>];\n"
            + "    1,000,000: << million[>%%commas>];\n"
            + "    1,000,000,000,000: << billion[>%%commas>];\n"
            + "    1,000,000,000,000,000: =#,##0=;\n"
            + "%%and:\n"
            + "    and =%default=;\n"
            + "    100: =%default=;\n"
            + "%%commas:\n"
            + "    ' and =%default=;\n"
            + "    100: , =%default=;\n"
            + "    1000: , <%default< thousand, >%default>;\n"
            + "    1,000,000: , =%default=;"
            + "%traditional:\n"
            + "    -x: \u3007| >>;\n"
            + "    x.x: << \u9ede >>;\n"
            + "    \u842c; \u842c; \u842c; \u842c; \u842c; \u842c; \u842c; \u842c; \u842c; \u842c;\n"
            + "    \u842c; \u842c; \u842c; \u842c; \u842c; \u842c; \u842c;\n"
            + "        \u842c; \u842c; \u842c;\n"
            + "    20: \u842c[->>];\n"
            + "    30: \u842c[->>];\n"
            + "    40: \u842c[->>];\n"
            + "    50: \u842c[->>];\n"
            + "    60: \u842c[->>];\n"
            + "    70: \u842c[->>];\n"
            + "    80: \u842c[->>];\n"
            + "    90: \u842c[->>];\n"
            + "    100: << \u842c[ >>];\n"
            + "    1000: << \u842c[ >>];\n"
            + "    1,000,000: << \u842c[ >>];\n"
            + "    1,000,000,000,000: << \u842c[ >>];\n"
            + "    1,000,000,000,000,000: =#,##0=;\n"
            + "%time:\n"
            + "    =0= sec.;\n"
            + "    60: =%%min-sec=;\n"
            + "    3600: =%%hr-min-sec=;\n"
            + "%%min-sec:\n"
            + "    0: *=00=;\n"
            + "    60/60: <0<>>;\n"
            + "%%hr-min-sec:\n"
            + "    0: *=00=;\n"
            + "    60/60: <00<>>;\n"
            + "    3600/60: <#,##0<:>>>;\n"
            + "%%post-process:android.icu.text.RBNFChinesePostProcessor\n";

        RuleBasedNumberFormat rbnf =  new RuleBasedNumberFormat(ruleWithChinese, ULocale.CHINESE);
        String[] ruleNames = rbnf.getRuleSetNames();
        try{
            // Test with "null" rules
            rbnf.format(0.0, null);
            errln("This was suppose to return an exception for a null format");
        } catch(Exception e){}
        for(int i=0; i<ruleNames.length; i++){
            try{
                rbnf.format(-123450.6789,ruleNames[i]);
            } catch(Exception e){
                errln("RBNFChinesePostProcessor was not suppose to return an exception " +
                        "when being formatted with parameters 0.0 and " + ruleNames[i]);
            }
        }
    }

    @Test
    public void TestSetDecimalFormatSymbols() {
        RuleBasedNumberFormat rbnf = new RuleBasedNumberFormat(Locale.ENGLISH, RuleBasedNumberFormat.ORDINAL);

        DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.ENGLISH);

        double number = 1001;

        String[] expected = { "1,001st", "1&001st" };

        String result = rbnf.format(number);
        if (!result.equals(expected[0])) {
            errln("Format Error - Got: " + result + " Expected: " + expected[0]);
        }

        /* Set new symbol for testing */
        dfs.setGroupingSeparator('&');
        rbnf.setDecimalFormatSymbols(dfs);

        result = rbnf.format(number);
        if (!result.equals(expected[1])) {
            errln("Format Error - Got: " + result + " Expected: " + expected[1]);
        }
    }

    @Test
    public void TestContext() {
        class TextContextItem {
            public String locale;
            public int format;
            public DisplayContext context;
            public double value;
            public String expectedResult;
            // Simple constructor
            public TextContextItem(String loc, int fmt, DisplayContext ctxt, double val, String expRes) {
                locale = loc;
                format = fmt;
                context = ctxt;
                value = val;
                expectedResult = expRes;
            }
        }
        final TextContextItem[] items = {
                new TextContextItem( "sv", RuleBasedNumberFormat.SPELLOUT, DisplayContext.CAPITALIZATION_FOR_MIDDLE_OF_SENTENCE,    123.45, "ett\u00ADhundra\u00ADtjugo\u00ADtre komma fyra fem" ),
                new TextContextItem( "sv", RuleBasedNumberFormat.SPELLOUT, DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE, 123.45, "Ett\u00ADhundra\u00ADtjugo\u00ADtre komma fyra fem" ),
                new TextContextItem( "sv", RuleBasedNumberFormat.SPELLOUT, DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU,       123.45, "ett\u00ADhundra\u00ADtjugo\u00ADtre komma fyra fem" ),
                new TextContextItem( "sv", RuleBasedNumberFormat.SPELLOUT, DisplayContext.CAPITALIZATION_FOR_STANDALONE,            123.45, "ett\u00ADhundra\u00ADtjugo\u00ADtre komma fyra fem" ),
                new TextContextItem( "en", RuleBasedNumberFormat.SPELLOUT, DisplayContext.CAPITALIZATION_FOR_MIDDLE_OF_SENTENCE,    123.45, "one hundred twenty-three point four five" ),
                new TextContextItem( "en", RuleBasedNumberFormat.SPELLOUT, DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE, 123.45, "One hundred twenty-three point four five" ),
                new TextContextItem( "en", RuleBasedNumberFormat.SPELLOUT, DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU,       123.45, "One hundred twenty-three point four five" ),
                new TextContextItem( "en", RuleBasedNumberFormat.SPELLOUT, DisplayContext.CAPITALIZATION_FOR_STANDALONE,            123.45, "One hundred twenty-three point four five" ),
        };
        for (TextContextItem item: items) {
            ULocale locale = new ULocale(item.locale);
            RuleBasedNumberFormat rbnf = new RuleBasedNumberFormat(locale, item.format);
            rbnf.setContext(item.context);
            String result = rbnf.format(item.value, rbnf.getDefaultRuleSetName());
            if (!result.equals(item.expectedResult)) {
                errln("Error for locale " + item.locale + ", context " + item.context + ", expected " + item.expectedResult + ", got " + result);
            }
            RuleBasedNumberFormat rbnfClone = (RuleBasedNumberFormat)rbnf.clone();
            if (!rbnfClone.equals(rbnf)) {
                errln("Error for locale " + item.locale + ", context " + item.context + ", rbnf.clone() != rbnf");
            } else {
                result = rbnfClone.format(item.value, rbnfClone.getDefaultRuleSetName());
                if (!result.equals(item.expectedResult)) {
                    errln("Error with clone for locale " + item.locale + ", context " + item.context + ", expected " + item.expectedResult + ", got " + result);
                }
            }
        }
    }

    @Test
    public void TestInfinityNaN() {
        String enRules = "%default:"
                + "-x: minus >>;"
                + "Inf: infinite;"
                + "NaN: not a number;"
                + "0: =#,##0=;";
        RuleBasedNumberFormat enFormatter = new RuleBasedNumberFormat(enRules, ULocale.ENGLISH);
        String[][] enTestData = {
                {"1", "1"},
                {"\u221E", "infinite"},
                {"-\u221E", "minus infinite"},
                {"NaN", "not a number"},

        };

        doTest(enFormatter, enTestData, true);

        // Test the default behavior when the rules are undefined.
        enRules = "%default:"
                + "-x: ->>;"
                + "0: =#,##0=;";
        enFormatter = new RuleBasedNumberFormat(enRules, ULocale.ENGLISH);
        String[][] enDefaultTestData = {
                {"1", "1"},
                {"\u221E", "∞"},
                {"-\u221E", "-∞"},
                {"NaN", "NaN"},

        };

        doTest(enFormatter, enDefaultTestData, true);
    }

    @Test
    public void TestVariableDecimalPoint() {
        String enRules = "%spellout-numbering:"
                + "-x: minus >>;"
                + "x.x: << point >>;"
                + "x,x: << comma >>;"
                + "0.x: xpoint >>;"
                + "0,x: xcomma >>;"
                + "0: zero;"
                + "1: one;"
                + "2: two;"
                + "3: three;"
                + "4: four;"
                + "5: five;"
                + "6: six;"
                + "7: seven;"
                + "8: eight;"
                + "9: nine;";
        RuleBasedNumberFormat enFormatter = new RuleBasedNumberFormat(enRules, ULocale.ENGLISH);
        String[][] enTestPointData = {
                {"1.1", "one point one"},
                {"1.23", "one point two three"},
                {"0.4", "xpoint four"},
        };
        doTest(enFormatter, enTestPointData, true);
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(ULocale.ENGLISH);
        decimalFormatSymbols.setDecimalSeparator(',');
        enFormatter.setDecimalFormatSymbols(decimalFormatSymbols);
        String[][] enTestCommaData = {
                {"1.1", "one comma one"},
                {"1.23", "one comma two three"},
                {"0.4", "xcomma four"},
        };
        doTest(enFormatter, enTestCommaData, true);
    }

    @Test
    public void TestRounding() {
        RuleBasedNumberFormat enFormatter = new RuleBasedNumberFormat(ULocale.ENGLISH, RuleBasedNumberFormat.SPELLOUT);
        String[][] enTestFullData = {
                {"0", "zero"},
                {"0.4", "zero point four"},
                {"0.49", "zero point four nine"},
                {"0.5", "zero point five"},
                {"0.51", "zero point five one"},
                {"0.99", "zero point nine nine"},
                {"1", "one"},
                {"1.01", "one point zero one"},
                {"1.49", "one point four nine"},
                {"1.5", "one point five"},
                {"1.51", "one point five one"},
                {"450359962737049.6", "four hundred fifty trillion three hundred fifty-nine billion nine hundred sixty-two million seven hundred thirty-seven thousand forty-nine point six"}, // 2^52 / 10
                {"450359962737049.7", "four hundred fifty trillion three hundred fifty-nine billion nine hundred sixty-two million seven hundred thirty-seven thousand forty-nine point seven"}, // 2^52 + 1 / 10
        };
        doTest(enFormatter, enTestFullData, false);

        enFormatter.setMaximumFractionDigits(0);
        enFormatter.setRoundingMode(BigDecimal.ROUND_HALF_EVEN);
        String[][] enTestIntegerData = {
                {"0", "zero"},
                {"0.4", "zero"},
                {"0.49", "zero"},
                {"0.5", "zero"},
                {"0.51", "one"},
                {"0.99", "one"},
                {"1", "one"},
                {"1.01", "one"},
                {"1.49", "one"},
                {"1.5", "two"},
                {"1.51", "two"},
        };
        doTest(enFormatter, enTestIntegerData, false);

        enFormatter.setMaximumFractionDigits(1);
        enFormatter.setRoundingMode(BigDecimal.ROUND_HALF_EVEN);
        String[][] enTestTwoDigitsData = {
                {"0", "zero"},
                {"0.04", "zero"},
                {"0.049", "zero"},
                {"0.05", "zero"},
                {"0.051", "zero point one"},
                {"0.099", "zero point one"},
                {"10.11", "ten point one"},
                {"10.149", "ten point one"},
                {"10.15", "ten point two"},
                {"10.151", "ten point two"},
        };
        doTest(enFormatter, enTestTwoDigitsData, false);

        enFormatter.setMaximumFractionDigits(3);
        enFormatter.setRoundingMode(BigDecimal.ROUND_DOWN);
        String[][] enTestThreeDigitsDownData = {
                {"4.3", "four point three"}, // Not 4.299!
        };
        doTest(enFormatter, enTestThreeDigitsDownData, false);
    }

    @Test
    public void testLargeNumbers() {
        RuleBasedNumberFormat rbnf = new RuleBasedNumberFormat(ULocale.US, RuleBasedNumberFormat.SPELLOUT);

        String[][] enTestFullData = {
                {"9999999999999998", "nine quadrillion nine hundred ninety-nine trillion nine hundred ninety-nine billion nine hundred ninety-nine million nine hundred ninety-nine thousand nine hundred ninety-eight"},
                {"9999999999999999", "nine quadrillion nine hundred ninety-nine trillion nine hundred ninety-nine billion nine hundred ninety-nine million nine hundred ninety-nine thousand nine hundred ninety-nine"},
                {"999999999999999999", "nine hundred ninety-nine quadrillion nine hundred ninety-nine trillion nine hundred ninety-nine billion nine hundred ninety-nine million nine hundred ninety-nine thousand nine hundred ninety-nine"},
                {"1000000000000000000", "1,000,000,000,000,000,000"}, // The rules don't go to 1 quintillion yet
                {"-9223372036854775809", "-9,223,372,036,854,775,809"}, // We've gone beyond 64-bit precision
                {"-9223372036854775808", "-9,223,372,036,854,775,808"}, // We've gone beyond +64-bit precision
                {"-9223372036854775807", "minus 9,223,372,036,854,775,807"}, // Minimum 64-bit precision
                {"-9223372036854775806", "minus 9,223,372,036,854,775,806"}, // Minimum 64-bit precision + 1
                {"9223372036854774111", "9,223,372,036,854,774,111"}, // Below 64-bit precision
                {"9223372036854774999", "9,223,372,036,854,774,999"}, // Below 64-bit precision
                {"9223372036854775000", "9,223,372,036,854,775,000"}, // Below 64-bit precision
                {"9223372036854775806", "9,223,372,036,854,775,806"}, // Maximum 64-bit precision - 1
                {"9223372036854775807", "9,223,372,036,854,775,807"}, // Maximum 64-bit precision
                {"9223372036854775808", "9,223,372,036,854,775,808"}, // We've gone beyond 64-bit precision
        };
        doTest(rbnf, enTestFullData, false);
    }
}
