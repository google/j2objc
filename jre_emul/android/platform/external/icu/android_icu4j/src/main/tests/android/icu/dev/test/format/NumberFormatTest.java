/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2001-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */

/**
 * Port From:   ICU4C v1.8.1 : format : NumberFormatTest
 * Source File: $ICU4oot/source/test/intltest/numfmtst.cpp
 **/

package android.icu.dev.test.format;

import java.io.IOException;
import java.math.BigInteger;
import java.text.AttributedCharacterIterator;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.dev.test.TestUtil;
import android.icu.dev.test.format.IntlTestDecimalFormatAPIC.FieldContainer;
import android.icu.impl.ICUConfig;
import android.icu.impl.LocaleUtility;
import android.icu.impl.data.ResourceReader;
import android.icu.impl.data.TokenIterator;
import android.icu.math.BigDecimal;
import android.icu.math.MathContext;
import android.icu.text.CompactDecimalFormat;
import android.icu.text.DecimalFormat;
import android.icu.text.DecimalFormatSymbols;
import android.icu.text.DisplayContext;
import android.icu.text.MeasureFormat;
import android.icu.text.NumberFormat;
import android.icu.text.NumberFormat.NumberFormatFactory;
import android.icu.text.NumberFormat.SimpleNumberFormatFactory;
import android.icu.text.NumberingSystem;
import android.icu.text.RuleBasedNumberFormat;
import android.icu.util.Currency;
import android.icu.util.CurrencyAmount;
import android.icu.util.ULocale;

public class NumberFormatTest extends TestFmwk {

    private static ULocale EN = new ULocale("en");

    private static Number toNumber(String s) {
        if (s.equals("NaN")) {
            return Double.NaN;
        } else if (s.equals("-Inf")) {
            return Double.NEGATIVE_INFINITY;
        } else if (s.equals("Inf")) {
            return Double.POSITIVE_INFINITY;
        }
        return new BigDecimal(s);
    }


    private DataDrivenNumberFormatTestUtility.CodeUnderTest ICU =
            new DataDrivenNumberFormatTestUtility.CodeUnderTest() {
                @Override
                public Character Id() { return 'J'; }

                @Override
                public String format(NumberFormatTestData tuple) {
                    DecimalFormat fmt = newDecimalFormat(tuple);
                    String actual = fmt.format(toNumber(tuple.format));
                    String expected = tuple.output;
                    if (!expected.equals(actual)) {
                        return "Expected " + expected + ", got " + actual;
                    }
                    return null;
                }

                @Override
                public String toPattern(NumberFormatTestData tuple) {
                    DecimalFormat fmt = newDecimalFormat(tuple);
                    StringBuilder result = new StringBuilder();
                    if (tuple.toPattern != null) {
                        String expected = tuple.toPattern;
                        String actual = fmt.toPattern();
                        if (!expected.equals(actual)) {
                            result.append("Expected toPattern=" + expected + ", got " + actual);
                        }
                    }
                    if (tuple.toLocalizedPattern != null) {
                        String expected = tuple.toLocalizedPattern;
                        String actual = fmt.toLocalizedPattern();
                        if (!expected.equals(actual)) {
                            result.append("Expected toLocalizedPattern=" + expected + ", got " + actual);
                        }
                    }
                    return result.length() == 0 ? null : result.toString();
                }

                @Override
                public String parse(NumberFormatTestData tuple) {
                    DecimalFormat fmt = newDecimalFormat(tuple);
                    ParsePosition ppos = new ParsePosition(0);
                    Number actual = fmt.parse(tuple.parse, ppos);
                    if (ppos.getIndex() == 0) {
                        if (!tuple.output.equals("fail")) {
                            return "Parse error expected.";
                        }
                        return null;
                    }
                    if (tuple.output.equals("fail")) {
                        return "Parse succeeded: "+actual+", but was expected to fail.";
                    }
                    Number expected = toNumber(tuple.output);
                    // number types cannot be compared, this is the best we can do.
                    if (expected.doubleValue() != (actual.doubleValue())) {
                        return "Expected: " + expected + ", got: " + actual;
                    }
                    return null;
                }

                @Override
                public String parseCurrency(NumberFormatTestData tuple) {
                    DecimalFormat fmt = newDecimalFormat(tuple);
                    ParsePosition ppos = new ParsePosition(0);
                    CurrencyAmount currAmt = fmt.parseCurrency(tuple.parse, ppos);
                    if (ppos.getIndex() == 0) {
                        if (!tuple.output.equals("fail")) {
                            return "Parse error expected.";
                        }
                        return null;
                    }
                    if (tuple.output.equals("fail")) {
                        return "Parse succeeded: "+currAmt+", but was expected to fail.";
                    }
                    Number expected = toNumber(tuple.output);
                    Number actual = currAmt.getNumber();
                    // number types cannot be compared, this is the best we can do.
                    if (expected.doubleValue() != (actual.doubleValue())) {
                        return "Expected: " + expected + ", got: " + actual;
                    }

                    if (!tuple.outputCurrency.equals(currAmt.getCurrency().toString())) {
                        return "Expected currency: " + tuple.outputCurrency + ", got: " + currAmt.getCurrency();
                    }
                    return null;
                }

                /**
                 * @param tuple
                 * @return
                 */
                private DecimalFormat newDecimalFormat(NumberFormatTestData tuple) {

                    DecimalFormat fmt = new DecimalFormat(
                            tuple.pattern == null ? "0" : tuple.pattern,
                            new DecimalFormatSymbols(tuple.locale == null ? EN : tuple.locale));
                    adjustDecimalFormat(tuple, fmt);
                    return fmt;
                }
                /**
                 * @param tuple
                 * @param fmt
                 */
                private void adjustDecimalFormat(NumberFormatTestData tuple, DecimalFormat fmt) {
                    if (tuple.minIntegerDigits != null) {
                        fmt.setMinimumIntegerDigits(tuple.minIntegerDigits);
                    }
                    if (tuple.maxIntegerDigits != null) {
                        fmt.setMaximumIntegerDigits(tuple.maxIntegerDigits);
                    }
                    if (tuple.minFractionDigits != null) {
                        fmt.setMinimumFractionDigits(tuple.minFractionDigits);
                    }
                    if (tuple.maxFractionDigits != null) {
                        fmt.setMaximumFractionDigits(tuple.maxFractionDigits);
                    }
                    if (tuple.currency != null) {
                        fmt.setCurrency(tuple.currency);
                    }
                    if (tuple.minGroupingDigits != null) {
                        // Oops we don't support this.
                    }
                    if (tuple.useSigDigits != null) {
                        fmt.setSignificantDigitsUsed(
                                tuple.useSigDigits != 0);
                    }
                    if (tuple.minSigDigits != null) {
                        fmt.setMinimumSignificantDigits(tuple.minSigDigits);
                    }
                    if (tuple.maxSigDigits != null) {
                        fmt.setMaximumSignificantDigits(tuple.maxSigDigits);
                    }
                    if (tuple.useGrouping != null) {
                        fmt.setGroupingUsed(tuple.useGrouping != 0);
                    }
                    if (tuple.multiplier != null) {
                        fmt.setMultiplier(tuple.multiplier);
                    }
                    if (tuple.roundingIncrement != null) {
                        fmt.setRoundingIncrement(tuple.roundingIncrement.doubleValue());
                    }
                    if (tuple.formatWidth != null) {
                        fmt.setFormatWidth(tuple.formatWidth);
                    }
                    if (tuple.padCharacter != null && tuple.padCharacter.length() > 0) {
                        fmt.setPadCharacter(tuple.padCharacter.charAt(0));
                    }
                    if (tuple.useScientific != null) {
                        fmt.setScientificNotation(tuple.useScientific != 0);
                    }
                    if (tuple.grouping != null) {
                        fmt.setGroupingSize(tuple.grouping);
                    }
                    if (tuple.grouping2 != null) {
                        fmt.setSecondaryGroupingSize(tuple.grouping2);
                    }
                    if (tuple.roundingMode != null) {
                        fmt.setRoundingMode(tuple.roundingMode);
                    }
                    if (tuple.currencyUsage != null) {
                        fmt.setCurrencyUsage(tuple.currencyUsage);
                    }                    if (tuple.minimumExponentDigits != null) {
                        fmt.setMinimumExponentDigits(
                                tuple.minimumExponentDigits.byteValue());
                    }
                    if (tuple.exponentSignAlwaysShown != null) {
                        fmt.setExponentSignAlwaysShown(
                                tuple.exponentSignAlwaysShown != 0);
                    }
                    if (tuple.decimalSeparatorAlwaysShown != null) {
                        fmt.setDecimalSeparatorAlwaysShown(
                                tuple.decimalSeparatorAlwaysShown != 0);
                    }
                    if (tuple.padPosition != null) {
                        fmt.setPadPosition(tuple.padPosition);
                    }
                    if (tuple.positivePrefix != null) {
                        fmt.setPositivePrefix(tuple.positivePrefix);
                    }
                    if (tuple.positiveSuffix != null) {
                        fmt.setPositiveSuffix(tuple.positiveSuffix);
                    }
                    if (tuple.negativePrefix != null) {
                        fmt.setNegativePrefix(tuple.negativePrefix);
                    }
                    if (tuple.negativeSuffix != null) {
                        fmt.setNegativeSuffix(tuple.negativeSuffix);
                    }
                    if (tuple.localizedPattern != null) {
                        fmt.applyLocalizedPattern(tuple.localizedPattern);
                    }
                    int lenient = tuple.lenient == null ? 1 : tuple.lenient.intValue();
                    fmt.setParseStrict(lenient == 0);
                    if (tuple.parseIntegerOnly != null) {
                        fmt.setParseIntegerOnly(tuple.parseIntegerOnly != 0);
                    }
                    if (tuple.decimalPatternMatchRequired != null) {
                        fmt.setDecimalPatternMatchRequired(tuple.decimalPatternMatchRequired != 0);
                    }
                    if (tuple.parseNoExponent != null) {
                        // Oops, not supported for now
                    }
                }
    };


    private DataDrivenNumberFormatTestUtility.CodeUnderTest JDK =
            new DataDrivenNumberFormatTestUtility.CodeUnderTest() {
                @Override
                public Character Id() { return 'K'; }

                @Override
                public String format(NumberFormatTestData tuple) {
                    java.text.DecimalFormat fmt = newDecimalFormat(tuple);
                    String actual = fmt.format(toNumber(tuple.format));
                    String expected = tuple.output;
                    if (!expected.equals(actual)) {
                        return "Expected " + expected + ", got " + actual;
                    }
                    return null;
                }

                @Override
                public String toPattern(NumberFormatTestData tuple) {
                    java.text.DecimalFormat fmt = newDecimalFormat(tuple);
                    StringBuilder result = new StringBuilder();
                    if (tuple.toPattern != null) {
                        String expected = tuple.toPattern;
                        String actual = fmt.toPattern();
                        if (!expected.equals(actual)) {
                            result.append("Expected toPattern=" + expected + ", got " + actual);
                        }
                    }
                    if (tuple.toLocalizedPattern != null) {
                        String expected = tuple.toLocalizedPattern;
                        String actual = fmt.toLocalizedPattern();
                        if (!expected.equals(actual)) {
                            result.append("Expected toLocalizedPattern=" + expected + ", got " + actual);
                        }
                    }
                    return result.length() == 0 ? null : result.toString();
                }

                @Override
                public String parse(NumberFormatTestData tuple) {
                    java.text.DecimalFormat fmt = newDecimalFormat(tuple);
                    ParsePosition ppos = new ParsePosition(0);
                    Number actual = fmt.parse(tuple.parse, ppos);
                    if (ppos.getIndex() == 0) {
                        if (!tuple.output.equals("fail")) {
                            return "Parse error expected.";
                        }
                        return null;
                    }
                    if (tuple.output.equals("fail")) {
                        return "Parse succeeded: "+actual+", but was expected to fail.";
                    }
                    Number expected = toNumber(tuple.output);
                    // number types cannot be compared, this is the best we can do.
                    if (expected.doubleValue() != actual.doubleValue()) {
                        return "Expected: " + expected + ", got: " + actual;
                    }
                    return null;
                }



                /**
                 * @param tuple
                 * @return
                 */
                private java.text.DecimalFormat newDecimalFormat(NumberFormatTestData tuple) {
                    java.text.DecimalFormat fmt = new java.text.DecimalFormat(
                            tuple.pattern == null ? "0" : tuple.pattern,
                            new java.text.DecimalFormatSymbols(
                                    (tuple.locale == null ? EN : tuple.locale).toLocale()));
                    adjustDecimalFormat(tuple, fmt);
                    return fmt;
                }

                /**
                 * @param tuple
                 * @param fmt
                 */
                private void adjustDecimalFormat(NumberFormatTestData tuple, java.text.DecimalFormat fmt) {
                    if (tuple.minIntegerDigits != null) {
                        fmt.setMinimumIntegerDigits(tuple.minIntegerDigits);
                    }
                    if (tuple.maxIntegerDigits != null) {
                        fmt.setMaximumIntegerDigits(tuple.maxIntegerDigits);
                    }
                    if (tuple.minFractionDigits != null) {
                        fmt.setMinimumFractionDigits(tuple.minFractionDigits);
                    }
                    if (tuple.maxFractionDigits != null) {
                        fmt.setMaximumFractionDigits(tuple.maxFractionDigits);
                    }
                    if (tuple.currency != null) {
                        fmt.setCurrency(java.util.Currency.getInstance(tuple.currency.toString()));
                    }
                    if (tuple.minGroupingDigits != null) {
                        // Oops we don't support this.
                    }
                    if (tuple.useSigDigits != null) {
                        // Oops we don't support this
                    }
                    if (tuple.minSigDigits != null) {
                        // Oops we don't support this
                    }
                    if (tuple.maxSigDigits != null) {
                        // Oops we don't support this
                    }
                    if (tuple.useGrouping != null) {
                        fmt.setGroupingUsed(tuple.useGrouping != 0);
                    }
                    if (tuple.multiplier != null) {
                        fmt.setMultiplier(tuple.multiplier);
                    }
                    if (tuple.roundingIncrement != null) {
                        // Not supported
                    }
                    if (tuple.formatWidth != null) {
                        // Not supported
                    }
                    if (tuple.padCharacter != null && tuple.padCharacter.length() > 0) {
                        // Not supported
                    }
                    if (tuple.useScientific != null) {
                        // Not supported
                    }
                    if (tuple.grouping != null) {
                        fmt.setGroupingSize(tuple.grouping);
                    }
                    if (tuple.grouping2 != null) {
                        // Not supported
                    }
                    if (tuple.roundingMode != null) {
                        // Not supported
                    }
                    if (tuple.currencyUsage != null) {
                        // Not supported
                    }
                    if (tuple.minimumExponentDigits != null) {
                        // Not supported
                    }
                    if (tuple.exponentSignAlwaysShown != null) {
                        // Not supported
                    }
                    if (tuple.decimalSeparatorAlwaysShown != null) {
                        fmt.setDecimalSeparatorAlwaysShown(
                                tuple.decimalSeparatorAlwaysShown != 0);
                    }
                    if (tuple.padPosition != null) {
                        // Not supported
                    }
                    if (tuple.positivePrefix != null) {
                        fmt.setPositivePrefix(tuple.positivePrefix);
                    }
                    if (tuple.positiveSuffix != null) {
                        fmt.setPositiveSuffix(tuple.positiveSuffix);
                    }
                    if (tuple.negativePrefix != null) {
                        fmt.setNegativePrefix(tuple.negativePrefix);
                    }
                    if (tuple.negativeSuffix != null) {
                        fmt.setNegativeSuffix(tuple.negativeSuffix);
                    }
                    if (tuple.localizedPattern != null) {
                        fmt.applyLocalizedPattern(tuple.localizedPattern);
                    }

                    // lenient parsing not supported by JDK
                    if (tuple.parseIntegerOnly != null) {
                        fmt.setParseIntegerOnly(tuple.parseIntegerOnly != 0);
                    }
                    if (tuple.decimalPatternMatchRequired != null) {
                       // Oops, not supported
                    }
                    if (tuple.parseNoExponent != null) {
                        // Oops, not supported for now
                    }
                }
    };

    @Test
    public void TestRoundingScientific10542() {
        DecimalFormat format =
                new DecimalFormat("0.00E0");

        int[] roundingModes = {
              BigDecimal.ROUND_CEILING,
              BigDecimal.ROUND_DOWN,
              BigDecimal.ROUND_FLOOR,
              BigDecimal.ROUND_HALF_DOWN,
              BigDecimal.ROUND_HALF_EVEN,
              BigDecimal.ROUND_HALF_UP,
              BigDecimal.ROUND_UP};
        String[] descriptions = {
                "Round Ceiling",
                "Round Down",
                "Round Floor",
                "Round half down",
                "Round half even",
                "Round half up",
                "Round up"};

        double[] values = {-0.003006, -0.003005, -0.003004, 0.003014, 0.003015, 0.003016};
        // The order of these expected values correspond to the order of roundingModes and the order of values.
        String[][] expected = {
                {"-3.00E-3", "-3.00E-3", "-3.00E-3", "3.02E-3", "3.02E-3", "3.02E-3"},
                {"-3.00E-3", "-3.00E-3", "-3.00E-3", "3.01E-3", "3.01E-3", "3.01E-3"},
                {"-3.01E-3", "-3.01E-3", "-3.01E-3", "3.01E-3", "3.01E-3", "3.01E-3"},
                {"-3.01E-3", "-3.00E-3", "-3.00E-3", "3.01E-3", "3.01E-3", "3.02E-3"},
                {"-3.01E-3", "-3.00E-3", "-3.00E-3", "3.01E-3", "3.02E-3", "3.02E-3"},
                {"-3.01E-3", "-3.01E-3", "-3.00E-3", "3.01E-3", "3.02E-3", "3.02E-3"},
                {"-3.01E-3", "-3.01E-3", "-3.01E-3", "3.02E-3", "3.02E-3", "3.02E-3"}};
        verifyRounding(format, values, expected, roundingModes, descriptions);
        values = new double[]{-3006.0, -3005, -3004, 3014, 3015, 3016};
        // The order of these expected values correspond to the order of roundingModes and the order of values.
        expected = new String[][]{
                {"-3.00E3", "-3.00E3", "-3.00E3", "3.02E3", "3.02E3", "3.02E3"},
                {"-3.00E3", "-3.00E3", "-3.00E3", "3.01E3", "3.01E3", "3.01E3"},
                {"-3.01E3", "-3.01E3", "-3.01E3", "3.01E3", "3.01E3", "3.01E3"},
                {"-3.01E3", "-3.00E3", "-3.00E3", "3.01E3", "3.01E3", "3.02E3"},
                {"-3.01E3", "-3.00E3", "-3.00E3", "3.01E3", "3.02E3", "3.02E3"},
                {"-3.01E3", "-3.01E3", "-3.00E3", "3.01E3", "3.02E3", "3.02E3"},
                {"-3.01E3", "-3.01E3", "-3.01E3", "3.02E3", "3.02E3", "3.02E3"}};
        verifyRounding(format, values, expected, roundingModes, descriptions);
        values = new double[]{0.0, -0.0};
        // The order of these expected values correspond to the order of roundingModes and the order of values.
        expected = new String[][]{
                {"0.00E0", "-0.00E0"},
                {"0.00E0", "-0.00E0"},
                {"0.00E0", "-0.00E0"},
                {"0.00E0", "-0.00E0"},
                {"0.00E0", "-0.00E0"},
                {"0.00E0", "-0.00E0"},
                {"0.00E0", "-0.00E0"}};
        verifyRounding(format, values, expected, roundingModes, descriptions);
        values = new double[]{1e25, 1e25 + 1e15, 1e25 - 1e15};
        // The order of these expected values correspond to the order of roundingModes and the order of values.
        expected = new String[][]{
                {"1.00E25", "1.01E25", "1.00E25"},
                {"1.00E25", "1.00E25", "9.99E24"},
                {"1.00E25", "1.00E25", "9.99E24"},
                {"1.00E25", "1.00E25", "1.00E25"},
                {"1.00E25", "1.00E25", "1.00E25"},
                {"1.00E25", "1.00E25", "1.00E25"},
                {"1.00E25", "1.01E25", "1.00E25"}};
        verifyRounding(format, values, expected, roundingModes, descriptions);
        values = new double[]{-1e25, -1e25 + 1e15, -1e25 - 1e15};
        // The order of these expected values correspond to the order of roundingModes and the order of values.
        expected = new String[][]{
                {"-1.00E25", "-9.99E24", "-1.00E25"},
                {"-1.00E25", "-9.99E24", "-1.00E25"},
                {"-1.00E25", "-1.00E25", "-1.01E25"},
                {"-1.00E25", "-1.00E25", "-1.00E25"},
                {"-1.00E25", "-1.00E25", "-1.00E25"},
                {"-1.00E25", "-1.00E25", "-1.00E25"},
                {"-1.00E25", "-1.00E25", "-1.01E25"}};
        verifyRounding(format, values, expected, roundingModes, descriptions);
        values = new double[]{1e-25, 1e-25 + 1e-35, 1e-25 - 1e-35};
        // The order of these expected values correspond to the order of roundingModes and the order of values.
        expected = new String[][]{
                {"1.00E-25", "1.01E-25", "1.00E-25"},
                {"1.00E-25", "1.00E-25", "9.99E-26"},
                {"1.00E-25", "1.00E-25", "9.99E-26"},
                {"1.00E-25", "1.00E-25", "1.00E-25"},
                {"1.00E-25", "1.00E-25", "1.00E-25"},
                {"1.00E-25", "1.00E-25", "1.00E-25"},
                {"1.00E-25", "1.01E-25", "1.00E-25"}};
        verifyRounding(format, values, expected, roundingModes, descriptions);
        values = new double[]{-1e-25, -1e-25 + 1e-35, -1e-25 - 1e-35};
        // The order of these expected values correspond to the order of roundingModes and the order of values.
        expected = new String[][]{
                {"-1.00E-25", "-9.99E-26", "-1.00E-25"},
                {"-1.00E-25", "-9.99E-26", "-1.00E-25"},
                {"-1.00E-25", "-1.00E-25", "-1.01E-25"},
                {"-1.00E-25", "-1.00E-25", "-1.00E-25"},
                {"-1.00E-25", "-1.00E-25", "-1.00E-25"},
                {"-1.00E-25", "-1.00E-25", "-1.00E-25"},
                {"-1.00E-25", "-1.00E-25", "-1.01E-25"}};
        verifyRounding(format, values, expected, roundingModes, descriptions);
    }

    private void verifyRounding(DecimalFormat format, double[] values, String[][] expected, int[] roundingModes,
            String[] descriptions) {
        for (int i = 0; i < roundingModes.length; i++) {
            format.setRoundingMode(roundingModes[i]);
            for (int j = 0; j < values.length; j++) {
                assertEquals(descriptions[i]+" " +values[j], expected[i][j], format.format(values[j]));
            }
        }
    }

    @Test
    public void Test10419RoundingWith0FractionDigits() {
        Object[][] data = new Object[][]{
                {BigDecimal.ROUND_CEILING, 1.488, "2"},
                {BigDecimal.ROUND_DOWN, 1.588, "1"},
                {BigDecimal.ROUND_FLOOR, 1.588, "1"},
                {BigDecimal.ROUND_HALF_DOWN, 1.5, "1"},
                {BigDecimal.ROUND_HALF_EVEN, 2.5, "2"},
                {BigDecimal.ROUND_HALF_UP, 2.5, "3"},
                {BigDecimal.ROUND_UP, 1.5, "2"},
        };
        NumberFormat nff = NumberFormat.getNumberInstance(ULocale.ENGLISH);
        nff.setMaximumFractionDigits(0);
        for (Object[] item : data) {
          nff.setRoundingMode(((Integer) item[0]).intValue());
          assertEquals("Test10419", item[2], nff.format(item[1]));
        }
    }

    @Test
    public void TestParseNegativeWithFaLocale() {
        DecimalFormat parser = (DecimalFormat) NumberFormat.getInstance(new ULocale("fa"));
        try {
            double value = parser.parse("-0,5").doubleValue();
            assertEquals("Expect -0.5", -0.5, value);
        } catch (ParseException e) {
            TestFmwk.errln("Parsing -0.5 should have succeeded.");
        }
    }

    @Test
    public void TestParseNegativeWithAlternativeMinusSign() {
        DecimalFormat parser = (DecimalFormat) NumberFormat.getInstance(new ULocale("en"));
        try {
            double value = parser.parse("\u208B0.5").doubleValue();
            assertEquals("Expect -0.5", -0.5, value);
        } catch (ParseException e) {
            TestFmwk.errln("Parsing -0.5 should have succeeded.");
        }
    }

    // Test various patterns
    @Test
    public void TestPatterns() {

        DecimalFormatSymbols sym = new DecimalFormatSymbols(Locale.US);
        final String pat[]    = { "#.#", "#.", ".#", "#" };
        int pat_length = pat.length;
        final String newpat[] = { "#0.#", "#0.", "#.0", "#" };
        final String num[]    = { "0",   "0.", ".0", "0" };
        for (int i=0; i<pat_length; ++i)
        {
            DecimalFormat fmt = new DecimalFormat(pat[i], sym);
            String newp = fmt.toPattern();
            if (!newp.equals(newpat[i]))
                errln("FAIL: Pattern " + pat[i] + " should transmute to " + newpat[i] +
                        "; " + newp + " seen instead");

            String s = ((NumberFormat)fmt).format(0);
            if (!s.equals(num[i]))
            {
                errln("FAIL: Pattern " + pat[i] + " should format zero as " + num[i] +
                        "; " + s + " seen instead");
                logln("Min integer digits = " + fmt.getMinimumIntegerDigits());
            }
            // BigInteger 0 - ticket#4731
            s = ((NumberFormat)fmt).format(BigInteger.ZERO);
            if (!s.equals(num[i]))
            {
                errln("FAIL: Pattern " + pat[i] + " should format BigInteger zero as " + num[i] +
                        "; " + s + " seen instead");
                logln("Min integer digits = " + fmt.getMinimumIntegerDigits());
            }
        }
    }

    // Test exponential pattern
    @Test
    public void TestExponential() {

        DecimalFormatSymbols sym = new DecimalFormatSymbols(Locale.US);
        final String pat[] = { "0.####E0", "00.000E00", "##0.######E000", "0.###E0;[0.###E0]" };
        int pat_length = pat.length;

        double val[] = { 0.01234, 123456789, 1.23e300, -3.141592653e-271 };
        int val_length = val.length;
        final String valFormat[] = {
                // 0.####E0
                "1.234E-2", "1.2346E8", "1.23E300", "-3.1416E-271",
                // 00.000E00
                "12.340E-03", "12.346E07", "12.300E299", "-31.416E-272",
                // ##0.######E000
                "12.34E-003", "123.4568E006", "1.23E300", "-314.1593E-273",
                // 0.###E0;[0.###E0]
                "1.234E-2", "1.235E8", "1.23E300", "[3.142E-271]" };
        /*double valParse[] =
            {
                0.01234, 123460000, 1.23E300, -3.1416E-271,
                0.01234, 123460000, 1.23E300, -3.1416E-271,
                0.01234, 123456800, 1.23E300, -3.141593E-271,
                0.01234, 123500000, 1.23E300, -3.142E-271,
            };*/ //The variable is never used

        int lval[] = { 0, -1, 1, 123456789 };
        int lval_length = lval.length;
        final String lvalFormat[] = {
                // 0.####E0
                "0E0", "-1E0", "1E0", "1.2346E8",
                // 00.000E00
                "00.000E00", "-10.000E-01", "10.000E-01", "12.346E07",
                // ##0.######E000
                "0E000", "-1E000", "1E000", "123.4568E006",
                // 0.###E0;[0.###E0]
                "0E0", "[1E0]", "1E0", "1.235E8" };
        int lvalParse[] =
            {
                0, -1, 1, 123460000,
                0, -1, 1, 123460000,
                0, -1, 1, 123456800,
                0, -1, 1, 123500000,
            };
        int ival = 0, ilval = 0;
        for (int p = 0; p < pat_length; ++p) {
            DecimalFormat fmt = new DecimalFormat(pat[p], sym);
            logln("Pattern \"" + pat[p] + "\" -toPattern-> \"" + fmt.toPattern() + "\"");
            int v;
            for (v = 0; v < val_length; ++v) {
                String s;
                s = ((NumberFormat) fmt).format(val[v]);
                logln(" " + val[v] + " -format-> " + s);
                if (!s.equals(valFormat[v + ival]))
                    errln("FAIL: Expected " + valFormat[v + ival]);

                ParsePosition pos = new ParsePosition(0);
                double a = fmt.parse(s, pos).doubleValue();
                if (pos.getIndex() == s.length()) {
                    logln("  -parse-> " + Double.toString(a));
                    // Use epsilon comparison as necessary
                } else
                    errln("FAIL: Partial parse (" + pos.getIndex() + " chars) -> " + a);
            }
            for (v = 0; v < lval_length; ++v) {
                String s;
                s = ((NumberFormat) fmt).format(lval[v]);
                logln(" " + lval[v] + "L -format-> " + s);
                if (!s.equals(lvalFormat[v + ilval]))
                    errln("ERROR: Expected " + lvalFormat[v + ilval] + " Got: " + s);

                ParsePosition pos = new ParsePosition(0);
                long a = 0;
                Number A = fmt.parse(s, pos);
                if (A != null) {
                    a = A.longValue();
                    if (pos.getIndex() == s.length()) {
                        logln("  -parse-> " + a);
                        if (a != lvalParse[v + ilval])
                            errln("FAIL: Expected " + lvalParse[v + ilval]);
                    } else
                        errln("FAIL: Partial parse (" + pos.getIndex() + " chars) -> " + Long.toString(a));
                } else {
                    errln("Fail to parse the string: " + s);
                }
            }
            ival += val_length;
            ilval += lval_length;
        }
    }

    // Test the handling of quotes
    @Test
    public void TestQuotes() {

        StringBuffer pat;
        DecimalFormatSymbols sym = new DecimalFormatSymbols(Locale.US);
        pat = new StringBuffer("a'fo''o'b#");
        DecimalFormat fmt = new DecimalFormat(pat.toString(), sym);
        String s = ((NumberFormat)fmt).format(123);
        logln("Pattern \"" + pat + "\"");
        logln(" Format 123 . " + s);
        if (!s.equals("afo'ob123"))
            errln("FAIL: Expected afo'ob123");

        s ="";
        pat = new StringBuffer("a''b#");
        fmt = new DecimalFormat(pat.toString(), sym);
        s = ((NumberFormat)fmt).format(123);
        logln("Pattern \"" + pat + "\"");
        logln(" Format 123 . " + s);
        if (!s.equals("a'b123"))
            errln("FAIL: Expected a'b123");
    }

    @Test
    public void TestParseCurrencyTrailingSymbol() {
        // see sun bug 4709840
        NumberFormat fmt = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        float val = 12345.67f;
        String str = fmt.format(val);
        logln("val: " + val + " str: " + str);
        try {
            Number num = fmt.parse(str);
            logln("num: " + num);
        } catch (ParseException e) {
            errln("parse of '" + str + "' threw exception: " + e);
        }
    }

    /**
     * Test the handling of the currency symbol in patterns.
     **/
    @Test
    public void TestCurrencySign() {
        DecimalFormatSymbols sym = new DecimalFormatSymbols(Locale.US);
        StringBuffer pat = new StringBuffer("");
        char currency = 0x00A4;
        // "\xA4#,##0.00;-\xA4#,##0.00"
        pat.append(currency).append("#,##0.00;-").append(currency).append("#,##0.00");
        DecimalFormat fmt = new DecimalFormat(pat.toString(), sym);
        String s = ((NumberFormat) fmt).format(1234.56);
        pat = new StringBuffer();
        logln("Pattern \"" + fmt.toPattern() + "\"");
        logln(" Format " + 1234.56 + " . " + s);
        assertEquals("symbol, pos", "$1,234.56", s);

        s = ((NumberFormat) fmt).format(-1234.56);
        logln(" Format " + Double.toString(-1234.56) + " . " + s);
        assertEquals("symbol, neg", "-$1,234.56", s);

        pat.setLength(0);
        // "\xA4\xA4 #,##0.00;\xA4\xA4 -#,##0.00"
        pat.append(currency).append(currency).append(" #,##0.00;").append(currency).append(currency).append(" -#,##0.00");
        fmt = new DecimalFormat(pat.toString(), sym);
        s = ((NumberFormat) fmt).format(1234.56);
        logln("Pattern \"" + fmt.toPattern() + "\"");
        logln(" Format " + Double.toString(1234.56) + " . " + s);
        assertEquals("name, pos", "USD 1,234.56", s);

        s = ((NumberFormat) fmt).format(-1234.56);
        logln(" Format " + Double.toString(-1234.56) + " . " + s);
        assertEquals("name, neg", "USD -1,234.56", s);
    }

    @Test
    public void TestSpaceParsing() {
        // the data are:
        // the string to be parsed, parsed position, parsed error index
        String[][] DATA = {
                {"$124", "4", "-1"},
                {"$124 $124", "4", "-1"},
                {"$124 ", "4", "-1"},
                {"$ 124 ", "5", "-1"},
                {"$\u00A0124 ", "5", "-1"},
                {" $ 124 ", "0", "0"}, // TODO: need to handle space correctly
                {"124$", "0", "3"}, // TODO: need to handle space correctly
                // {"124 $", "5", "-1"}, TODO: OK or NOT?
                {"124 $", "0", "3"},
        };
        NumberFormat foo = NumberFormat.getCurrencyInstance();
        for (int i = 0; i < DATA.length; ++i) {
            ParsePosition parsePosition = new ParsePosition(0);
            String stringToBeParsed = DATA[i][0];
            int parsedPosition = Integer.parseInt(DATA[i][1]);
            int errorIndex = Integer.parseInt(DATA[i][2]);
            try {
                Number result = foo.parse(stringToBeParsed, parsePosition);
                if (parsePosition.getIndex() != parsedPosition ||
                        parsePosition.getErrorIndex() != errorIndex) {
                    errln("FAILED parse " + stringToBeParsed + "; parse position: " + parsePosition.getIndex() + "; error position: " + parsePosition.getErrorIndex());
                }
                if (parsePosition.getErrorIndex() == -1 &&
                        result.doubleValue() != 124) {
                    errln("FAILED parse " + stringToBeParsed + "; value " + result.doubleValue());
                }
            } catch (Exception e) {
                errln("FAILED " + e.toString());
            }
        }
    }


    @Test
    public void TestMultiCurrencySign() {
        String[][] DATA = {
                // the fields in the following test are:
                // locale,
                // currency pattern (with negative pattern),
                // currency number to be formatted,
                // currency format using currency symbol name, such as "$" for USD,
                // currency format using currency ISO name, such as "USD",
                // currency format using plural name, such as "US dollars".
                // for US locale
                {"en_US", "\u00A4#,##0.00;-\u00A4#,##0.00", "1234.56", "$1,234.56", "USD1,234.56", "US dollars1,234.56"},
                {"en_US", "\u00A4#,##0.00;-\u00A4#,##0.00", "-1234.56", "-$1,234.56", "-USD1,234.56", "-US dollars1,234.56"},
                {"en_US", "\u00A4#,##0.00;-\u00A4#,##0.00", "1", "$1.00", "USD1.00", "US dollars1.00"},
                // for CHINA locale
                {"zh_CN", "\u00A4#,##0.00;(\u00A4#,##0.00)", "1234.56", "\uFFE51,234.56", "CNY1,234.56", "\u4EBA\u6C11\u5E011,234.56"},
                {"zh_CN", "\u00A4#,##0.00;(\u00A4#,##0.00)", "-1234.56", "(\uFFE51,234.56)", "(CNY1,234.56)", "(\u4EBA\u6C11\u5E011,234.56)"},
                {"zh_CN", "\u00A4#,##0.00;(\u00A4#,##0.00)", "1", "\uFFE51.00", "CNY1.00", "\u4EBA\u6C11\u5E011.00"}
        };

        String doubleCurrencyStr = "\u00A4\u00A4";
        String tripleCurrencyStr = "\u00A4\u00A4\u00A4";

        for (int i=0; i<DATA.length; ++i) {
            String locale = DATA[i][0];
            String pat = DATA[i][1];
            Double numberToBeFormat = new Double(DATA[i][2]);
            DecimalFormatSymbols sym = new DecimalFormatSymbols(new ULocale(locale));
            for (int j=1; j<=3; ++j) {
                // j represents the number of currency sign in the pattern.
                if (j == 2) {
                    pat = pat.replaceAll("\u00A4", doubleCurrencyStr);
                } else if (j == 3) {
                    pat = pat.replaceAll("\u00A4\u00A4", tripleCurrencyStr);
                }
                DecimalFormat fmt = new DecimalFormat(pat, sym);
                String s = ((NumberFormat) fmt).format(numberToBeFormat);
                // DATA[i][3] is the currency format result using a
                // single currency sign.
                // DATA[i][4] is the currency format result using
                // double currency sign.
                // DATA[i][5] is the currency format result using
                // triple currency sign.
                // DATA[i][j+2] is the currency format result using
                // 'j' number of currency sign.
                String currencyFormatResult = DATA[i][2+j];
                if (!s.equals(currencyFormatResult)) {
                    errln("FAIL format: Expected " + currencyFormatResult);
                }
                try {
                    // mix style parsing
                    for (int k=3; k<=5; ++k) {
                        // DATA[i][3] is the currency format result using a
                        // single currency sign.
                        // DATA[i][4] is the currency format result using
                        // double currency sign.
                        // DATA[i][5] is the currency format result using
                        // triple currency sign.
                        String oneCurrencyFormat = DATA[i][k];
                        if (fmt.parse(oneCurrencyFormat).doubleValue() !=
                                numberToBeFormat.doubleValue()) {
                            errln("FAILED parse " + oneCurrencyFormat);
                        }
                    }
                } catch (ParseException e) {
                    errln("FAILED, DecimalFormat parse currency: " + e.toString());
                }
            }
        }
    }

    @Test
    public void TestCurrencyFormatForMixParsing() {
        MeasureFormat curFmt = MeasureFormat.getCurrencyFormat(new ULocale("en_US"));
        String[] formats = {
                "$1,234.56",  // string to be parsed
                "USD1,234.56",
                "US dollars1,234.56",
                "1,234.56 US dollars"
        };
        try {
            for (int i = 0; i < formats.length; ++i) {
                String stringToBeParsed = formats[i];
                CurrencyAmount parsedVal = (CurrencyAmount)curFmt.parseObject(stringToBeParsed);
                Number val = parsedVal.getNumber();
                if (!val.equals(new BigDecimal("1234.56"))) {
                    errln("FAIL: getCurrencyFormat of default locale (en_US) failed roundtripping the number. val=" + val);
                }
                if (!parsedVal.getCurrency().equals(Currency.getInstance("USD"))) {
                    errln("FAIL: getCurrencyFormat of default locale (en_US) failed roundtripping the currency");
                }
            }
        } catch (ParseException e) {
            errln("parse FAILED: " + e.toString());
        }
    }

    @Test
    public void TestDecimalFormatCurrencyParse() {
        // Locale.US
        DecimalFormatSymbols sym = new DecimalFormatSymbols(Locale.US);
        StringBuffer pat = new StringBuffer("");
        char currency = 0x00A4;
        // "\xA4#,##0.00;-\xA4#,##0.00"
        pat.append(currency).append(currency).append(currency).append("#,##0.00;-").append(currency).append(currency).append(currency).append("#,##0.00");
        DecimalFormat fmt = new DecimalFormat(pat.toString(), sym);
        String[][] DATA = {
                // the data are:
                // string to be parsed, the parsed result (number)
                {"$1.00", "1"},
                {"USD1.00", "1"},
                {"1.00 US dollar", "1"},
                {"$1,234.56", "1234.56"},
                {"USD1,234.56", "1234.56"},
                {"1,234.56 US dollar", "1234.56"},
        };
        try {
            for (int i = 0; i < DATA.length; ++i) {
                String stringToBeParsed = DATA[i][0];
                double parsedResult = Double.parseDouble(DATA[i][1]);
                Number num = fmt.parse(stringToBeParsed);
                if (num.doubleValue() != parsedResult) {
                    errln("FAIL parse: Expected " + parsedResult);
                }
            }
        } catch (ParseException e) {
            errln("FAILED, DecimalFormat parse currency: " + e.toString());
        }
    }

    /**
     * Test localized currency patterns.
     */
    @Test
    public void TestCurrency() {
        String[] DATA = {
                "fr", "CA", "", "1,50\u00a0$",
                "de", "DE", "", "1,50\u00a0\u20AC",
                "de", "DE", "PREEURO", "1,50\u00a0DM",
                "fr", "FR", "", "1,50\u00a0\u20AC",
                "fr", "FR", "PREEURO", "1,50\u00a0F",
        };

        for (int i=0; i<DATA.length; i+=4) {
            Locale locale = new Locale(DATA[i], DATA[i+1], DATA[i+2]);
            NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
            String s = fmt.format(1.50);
            if (s.equals(DATA[i+3])) {
                logln("Ok: 1.50 x " + locale + " => " + s);
            } else {
                logln("FAIL: 1.50 x " + locale + " => " + s +
                        ", expected " + DATA[i+3]);
            }
        }

        // format currency with CurrencyAmount
        for (int i=0; i<DATA.length; i+=4) {
            Locale locale = new Locale(DATA[i], DATA[i+1], DATA[i+2]);

            Currency curr = Currency.getInstance(locale);
            logln("\nName of the currency is: " + curr.getName(locale, Currency.LONG_NAME, new boolean[] {false}));
            CurrencyAmount cAmt = new CurrencyAmount(1.5, curr);
            logln("CurrencyAmount object's hashCode is: " + cAmt.hashCode()); //cover hashCode

            NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
            String sCurr = fmt.format(cAmt);
            if (sCurr.equals(DATA[i+3])) {
                logln("Ok: 1.50 x " + locale + " => " + sCurr);
            } else {
                errln("FAIL: 1.50 x " + locale + " => " + sCurr +
                        ", expected " + DATA[i+3]);
            }
        }

        //Cover MeasureFormat.getCurrencyFormat()
        ULocale save = ULocale.getDefault();
        ULocale.setDefault(ULocale.US);
        MeasureFormat curFmt = MeasureFormat.getCurrencyFormat();
        String strBuf = curFmt.format(new CurrencyAmount(new Float(1234.56), Currency.getInstance("USD")));

        try {
            CurrencyAmount parsedVal = (CurrencyAmount)curFmt.parseObject(strBuf);
            Number val = parsedVal.getNumber();
            if (!val.equals(new BigDecimal("1234.56"))) {
                errln("FAIL: getCurrencyFormat of default locale (en_US) failed roundtripping the number. val=" + val);
            }
            if (!parsedVal.getCurrency().equals(Currency.getInstance("USD"))) {
                errln("FAIL: getCurrencyFormat of default locale (en_US) failed roundtripping the currency");
            }
        }
        catch (ParseException e) {
            errln("FAIL: " + e.getMessage());
        }
        ULocale.setDefault(save);
    }

    @Test
    public void TestCurrencyIsoPluralFormat() {
        String[][] DATA = {
                // the data are:
                // locale,
                // currency amount to be formatted,
                // currency ISO code to be formatted,
                // format result using CURRENCYSTYLE,
                // format result using ISOCURRENCYSTYLE,
                // format result using PLURALCURRENCYSTYLE,
                {"en_US", "1", "USD", "$1.00", "USD1.00", "1.00 US dollars"},
                {"en_US", "1234.56", "USD", "$1,234.56", "USD1,234.56", "1,234.56 US dollars"},
                {"en_US", "-1234.56", "USD", "-$1,234.56", "-USD1,234.56", "-1,234.56 US dollars"},
                {"zh_CN", "1", "USD", "US$1.00", "USD1.00", "1.00美元"},
                {"zh_CN", "1234.56", "USD", "US$1,234.56", "USD1,234.56", "1,234.56美元"},
                {"zh_CN", "1", "CNY", "￥1.00", "CNY1.00", "1.00人民币"},
                {"zh_CN", "1234.56", "CNY", "￥1,234.56", "CNY1,234.56", "1,234.56人民币"},
                {"ru_RU", "1", "RUB", "1,00 \u20BD", "1,00 RUB", "1,00 российского рубля"},
                {"ru_RU", "2", "RUB", "2,00 \u20BD", "2,00 RUB", "2,00 российского рубля"},
                {"ru_RU", "5", "RUB", "5,00 \u20BD", "5,00 RUB", "5,00 российского рубля"},
                // test locale without currency information
                {"root", "-1.23", "USD", "-US$ 1.23", "-USD 1.23", "-1.23 USD"},
                {"root@numbers=latn", "-1.23", "USD", "-US$ 1.23", "-USD 1.23", "-1.23 USD"}, // ensure that the root locale is still used with modifiers
                {"root@numbers=arab", "-1.23", "USD", "\u061C-\u0661\u066B\u0662\u0663\u00A0US$", "\u061C-\u0661\u066B\u0662\u0663\u00A0USD", "\u061C-\u0661\u066B\u0662\u0663 USD"}, // ensure that the root locale is still used with modifiers
                {"es_AR", "1", "INR", "INR\u00A01,00", "INR\u00A01,00", "1,00 rupia india"},
                {"ar_EG", "1", "USD", "١٫٠٠\u00A0US$", "١٫٠٠\u00A0USD", "١٫٠٠ دولار أمريكي"},
        };

        for (int i=0; i<DATA.length; ++i) {
            for (int k = NumberFormat.CURRENCYSTYLE;
                    k <= NumberFormat.PLURALCURRENCYSTYLE;
                    ++k) {
                // k represents currency format style.
                if ( k != NumberFormat.CURRENCYSTYLE &&
                        k != NumberFormat.ISOCURRENCYSTYLE &&
                        k != NumberFormat.PLURALCURRENCYSTYLE ) {
                    continue;
                }
                String localeString = DATA[i][0];
                Double numberToBeFormat = new Double(DATA[i][1]);
                String currencyISOCode = DATA[i][2];
                ULocale locale = new ULocale(localeString);
                NumberFormat numFmt = NumberFormat.getInstance(locale, k);
                numFmt.setCurrency(Currency.getInstance(currencyISOCode));
                String strBuf = numFmt.format(numberToBeFormat);
                int resultDataIndex = k-1;
                if ( k == NumberFormat.CURRENCYSTYLE ) {
                    resultDataIndex = k+2;
                }
                // DATA[i][resultDataIndex] is the currency format result
                // using 'k' currency style.
                String formatResult = DATA[i][resultDataIndex];
                if (!strBuf.equals(formatResult)) {
                    errln("FAIL: localeID: " + localeString + ", expected(" + formatResult.length() + "): \"" + formatResult + "\", actual(" + strBuf.length() + "): \"" + strBuf + "\"");
                }
                try {
                    // test parsing, and test parsing for all currency formats.
                    for (int j = 3; j < 6; ++j) {
                        // DATA[i][3] is the currency format result using
                        // CURRENCYSTYLE formatter.
                        // DATA[i][4] is the currency format result using
                        // ISOCURRENCYSTYLE formatter.
                        // DATA[i][5] is the currency format result using
                        // PLURALCURRENCYSTYLE formatter.
                        String oneCurrencyFormatResult = DATA[i][j];
                        Number val = numFmt.parse(oneCurrencyFormatResult);
                        if (val.doubleValue() != numberToBeFormat.doubleValue()) {
                            errln("FAIL: getCurrencyFormat of locale " + localeString + " failed roundtripping the number. val=" + val + "; expected: " + numberToBeFormat);
                        }
                    }
                }
                catch (ParseException e) {
                    errln("FAIL: " + e.getMessage());
                }
            }
        }
    }


    @Test
    public void TestMiscCurrencyParsing() {
        String[][] DATA = {
                // each has: string to be parsed, parsed position, error position
                {"1.00 ", "0", "4"},
                {"1.00 UAE dirha", "0", "4"},
                {"1.00 us dollar", "14", "-1"},
                {"1.00 US DOLLAR", "14", "-1"},
                {"1.00 usd", "0", "4"},
        };
        ULocale locale = new ULocale("en_US");
        for (int i=0; i<DATA.length; ++i) {
            String stringToBeParsed = DATA[i][0];
            int parsedPosition = Integer.parseInt(DATA[i][1]);
            int errorIndex = Integer.parseInt(DATA[i][2]);
            NumberFormat numFmt = NumberFormat.getInstance(locale, NumberFormat.CURRENCYSTYLE);
            ParsePosition parsePosition = new ParsePosition(0);
            Number val = numFmt.parse(stringToBeParsed, parsePosition);
            if (parsePosition.getIndex() != parsedPosition ||
                    parsePosition.getErrorIndex() != errorIndex) {
                errln("FAIL: parse failed. expected error position: " + errorIndex + "; actual: " + parsePosition.getErrorIndex());
                errln("FAIL: parse failed. expected position: " + parsedPosition +"; actual: " + parsePosition.getIndex());
            }
            if (parsePosition.getErrorIndex() == -1 &&
                    val.doubleValue() != 1.00) {
                errln("FAIL: parse failed. expected 1.00, actual:" + val);
            }
        }
    }

    @Test
    public void TestParseCurrency() {
        class ParseCurrencyItem {
            private final String localeString;
            private final String descrip;
            private final String currStr;
            private final int    numExpectPos;
            private final int    numExpectVal;
            private final int    curExpectPos;
            private final int    curExpectVal;
            private final String curExpectCurr;

            ParseCurrencyItem(String locStr, String desc, String curr, int numExPos, int numExVal, int curExPos, int curExVal, String curExCurr) {
                localeString  = locStr;
                descrip       = desc;
                currStr       = curr;
                numExpectPos  = numExPos;
                numExpectVal  = numExVal;
                curExpectPos  = curExPos;
                curExpectVal  = curExVal;
                curExpectCurr = curExCurr;
            }
            public String getLocaleString()  { return localeString; }
            public String getDescrip()       { return descrip; }
            public String getCurrStr()       { return currStr; }
            public int    getNumExpectPos()  { return numExpectPos; }
            public int    getNumExpectVal()  { return numExpectVal; }
            public int    getCurExpectPos()  { return curExpectPos; }
            public int    getCurExpectVal()  { return curExpectVal; }
            public String getCurExpectCurr() { return curExpectCurr; }
        }
        final ParseCurrencyItem[] parseCurrencyItems = {
                new ParseCurrencyItem( "en_US", "dollars2", "$2.00",            5,  2,  5,  2,  "USD" ),
                new ParseCurrencyItem( "en_US", "dollars4", "$4",               2,  4,  2,  4,  "USD" ),
                new ParseCurrencyItem( "en_US", "dollars9", "9\u00A0$",         0,  0,  0,  0,  ""    ),
                new ParseCurrencyItem( "en_US", "pounds3",  "\u00A33.00",       0,  0,  5,  3,  "GBP" ),
                new ParseCurrencyItem( "en_US", "pounds5",  "\u00A35",          0,  0,  2,  5,  "GBP" ),
                new ParseCurrencyItem( "en_US", "pounds7",  "7\u00A0\u00A3",    0,  0,  0,  0,  ""    ),
                new ParseCurrencyItem( "en_US", "euros8",   "\u20AC8",          0,  0,  2,  8,  "EUR" ),

                new ParseCurrencyItem( "en_GB", "pounds3",  "\u00A33.00",       5,  3,  5,  3,  "GBP" ),
                new ParseCurrencyItem( "en_GB", "pounds5",  "\u00A35",          2,  5,  2,  5,  "GBP" ),
                new ParseCurrencyItem( "en_GB", "pounds7",  "7\u00A0\u00A3",    0,  0,  0,  0,  ""    ),
                new ParseCurrencyItem( "en_GB", "euros4",   "4,00\u00A0\u20AC", 0,  0,  0,  0,  ""    ),
                new ParseCurrencyItem( "en_GB", "euros6",   "6\u00A0\u20AC",    0,  0,  0,  0,  ""    ),
                new ParseCurrencyItem( "en_GB", "euros8",   "\u20AC8",          0,  0,  2,  8,  "EUR" ),
                new ParseCurrencyItem( "en_GB", "dollars4", "US$4",             0,  0,  4,  4,  "USD" ),

                new ParseCurrencyItem( "fr_FR", "euros4",   "4,00\u00A0\u20AC", 6,  4,  6,  4,  "EUR" ),
                new ParseCurrencyItem( "fr_FR", "euros6",   "6\u00A0\u20AC",    3,  6,  3,  6,  "EUR" ),
                new ParseCurrencyItem( "fr_FR", "euros8",   "\u20AC8",          0,  0,  0,  0,  ""    ),
                new ParseCurrencyItem( "fr_FR", "dollars2", "$2.00",            0,  0,  0,  0,  ""    ),
                new ParseCurrencyItem( "fr_FR", "dollars4", "$4",               0,  0,  0,  0,  ""    ),
        };
        for (ParseCurrencyItem item: parseCurrencyItems) {
            String localeString = item.getLocaleString();
            ULocale uloc = new ULocale(localeString);
            NumberFormat fmt = null;
            try {
                fmt = NumberFormat.getCurrencyInstance(uloc);
            } catch (Exception e) {
                errln("NumberFormat.getCurrencyInstance fails for locale " + localeString);
                continue;
            }
            String currStr = item.getCurrStr();
            ParsePosition parsePos = new ParsePosition(0);

            Number numVal = fmt.parse(currStr, parsePos);
            if ( parsePos.getIndex() != item.getNumExpectPos() || (numVal != null && numVal.intValue() != item.getNumExpectVal()) ) {
                if (numVal != null) {
                    errln("NumberFormat.getCurrencyInstance parse " + localeString + "/" + item.getDescrip() +
                            ", expect pos/val " + item.getNumExpectPos() + "/" + item.getNumExpectVal() +
                            ", get " + parsePos.getIndex() + "/" + numVal.intValue() );
                } else {
                    errln("NumberFormat.getCurrencyInstance parse " + localeString + "/" + item.getDescrip() +
                            ", expect pos/val " + item.getNumExpectPos() + "/" + item.getNumExpectVal() +
                            ", get " + parsePos.getIndex() + "/(NULL)" );
                }
            }

            parsePos.setIndex(0);
            CurrencyAmount currAmt = fmt.parseCurrency(currStr, parsePos);
            if ( parsePos.getIndex() != item.getCurExpectPos() || (currAmt != null && (currAmt.getNumber().intValue() != item.getCurExpectVal() ||
                    currAmt.getCurrency().getCurrencyCode().compareTo(item.getCurExpectCurr()) != 0)) ) {
                if (currAmt != null) {
                    errln("NumberFormat.getCurrencyInstance parseCurrency " + localeString + "/" + item.getDescrip() +
                            ", expect pos/val/curr " + item.getCurExpectPos() + "/" + item.getCurExpectVal() + "/" + item.getCurExpectCurr() +
                            ", get " + parsePos.getIndex() + "/" + currAmt.getNumber().intValue() + "/" + currAmt.getCurrency().getCurrencyCode() );
                } else {
                    errln("NumberFormat.getCurrencyInstance parseCurrency " + localeString + "/" + item.getDescrip() +
                            ", expect pos/val/curr " + item.getCurExpectPos() + "/" + item.getCurExpectVal() + "/" + item.getCurExpectCurr() +
                            ", get " + parsePos.getIndex() + "/(NULL)" );
                }
            }
        }
    }

    @Test
    public void TestParseCurrPatternWithDecStyle() {
        String currpat = "¤#,##0.00";
        String parsetxt = "x0y$";
        DecimalFormat decfmt = (DecimalFormat)NumberFormat.getInstance(new ULocale("en_US"), NumberFormat.NUMBERSTYLE);
        decfmt.applyPattern(currpat);
        ParsePosition ppos = new ParsePosition(0);
        Number value = decfmt.parse(parsetxt, ppos);
        if (ppos.getIndex() != 0) {
            errln("DecimalFormat.parse expected to fail but got ppos " + ppos.getIndex() + ", value " + value);
        }
    }

    /**
     * Test the Currency object handling, new as of ICU 2.2.
     */
    @Test
    public void TestCurrencyObject() {
        NumberFormat fmt =
                NumberFormat.getCurrencyInstance(Locale.US);

        expectCurrency(fmt, null, 1234.56, "$1,234.56");

        expectCurrency(fmt, Currency.getInstance(Locale.FRANCE),
                1234.56, "\u20AC1,234.56"); // Euro

        expectCurrency(fmt, Currency.getInstance(Locale.JAPAN),
                1234.56, "\u00A51,235"); // Yen

        expectCurrency(fmt, Currency.getInstance(new Locale("fr", "CH", "")),
                1234.56, "CHF1,234.56"); // no more 0.05 rounding here, see cldrbug 5548

        expectCurrency(fmt, Currency.getInstance(Locale.US),
                1234.56, "$1,234.56");

        fmt = NumberFormat.getCurrencyInstance(Locale.FRANCE);

        expectCurrency(fmt, null, 1234.56, "1 234,56 \u20AC");

        expectCurrency(fmt, Currency.getInstance(Locale.JAPAN),
                1234.56, "1 235 JPY"); // Yen

        expectCurrency(fmt, Currency.getInstance(new Locale("fr", "CH", "")),
                1234.56, "1 234,56 CHF"); // no more rounding here, see cldrbug 5548

        expectCurrency(fmt, Currency.getInstance(Locale.US),
                1234.56, "1 234,56 $US");

        expectCurrency(fmt, Currency.getInstance(Locale.FRANCE),
                1234.56, "1 234,56 \u20AC"); // Euro
    }

    @Test
    public void TestCompatibleCurrencies() {
        NumberFormat fmt =
                NumberFormat.getCurrencyInstance(Locale.US);
        expectParseCurrency(fmt, Currency.getInstance(Locale.JAPAN), "\u00A51,235"); // Yen half-width
        expectParseCurrency(fmt, Currency.getInstance(Locale.JAPAN), "\uFFE51,235"); // Yen full-wdith
    }

    @Test
    public void TestCurrencyPatterns() {
        int i;
        Locale[] locs = NumberFormat.getAvailableLocales();
        for (i=0; i<locs.length; ++i) {
            NumberFormat nf = NumberFormat.getCurrencyInstance(locs[i]);
            // Make sure currency formats do not have a variable number
            // of fraction digits
            int min = nf.getMinimumFractionDigits();
            int max = nf.getMaximumFractionDigits();
            if (min != max) {
                String a = nf.format(1.0);
                String b = nf.format(1.125);
                errln("FAIL: " + locs[i] +
                        " min fraction digits != max fraction digits; "+
                        "x 1.0 => " + a +
                        "; x 1.125 => " + b);
            }

            // Make sure EURO currency formats have exactly 2 fraction digits
            if (nf instanceof DecimalFormat) {
                Currency curr = ((DecimalFormat) nf).getCurrency();
                if (curr != null && "EUR".equals(curr.getCurrencyCode())) {
                    if (min != 2 || max != 2) {
                        String a = nf.format(1.0);
                        errln("FAIL: " + locs[i] +
                                " is a EURO format but it does not have 2 fraction digits; "+
                                "x 1.0 => " +
                                a);
                    }
                }
            }
        }
    }

    /**
     * Do rudimentary testing of parsing.
     */
    @Test
    public void TestParse() {
        String arg = "0.0";
        DecimalFormat format = new DecimalFormat("00");
        double aNumber = 0l;
        try {
            aNumber = format.parse(arg).doubleValue();
        } catch (ParseException e) {
            System.out.println(e);
        }
        logln("parse(" + arg + ") = " + aNumber);
    }

    /**
     * Test proper rounding by the format method.
     */
    @Test
    public void TestRounding487() {

        NumberFormat nf = NumberFormat.getInstance();
        roundingTest(nf, 0.00159999, 4, "0.0016");
        roundingTest(nf, 0.00995, 4, "0.01");

        roundingTest(nf, 12.3995, 3, "12.4");

        roundingTest(nf, 12.4999, 0, "12");
        roundingTest(nf, - 19.5, 0, "-20");

    }

    /**
     * Test the functioning of the secondary grouping value.
     */
    @Test
    public void TestSecondaryGrouping() {

        DecimalFormatSymbols US = new DecimalFormatSymbols(Locale.US);
        DecimalFormat f = new DecimalFormat("#,##,###", US);

        expect(f, 123456789L, "12,34,56,789");
        expectPat(f, "#,##,###");
        f.applyPattern("#,###");

        f.setSecondaryGroupingSize(4);
        expect(f, 123456789L, "12,3456,789");
        expectPat(f, "#,####,###");
        NumberFormat g = NumberFormat.getInstance(new Locale("hi", "IN"));

        String out = "";
        long l = 1876543210L;
        out = g.format(l);

        // expect "1,87,65,43,210", but with Hindi digits
        //         01234567890123
        boolean ok = true;
        if (out.length() != 14) {
            ok = false;
        } else {
            for (int i = 0; i < out.length(); ++i) {
                boolean expectGroup = false;
                switch (i) {
                case 1 :
                case 4 :
                case 7 :
                case 10 :
                    expectGroup = true;
                    break;
                }
                // Later -- fix this to get the actual grouping
                // character from the resource bundle.
                boolean isGroup = (out.charAt(i) == 0x002C);
                if (isGroup != expectGroup) {
                    ok = false;
                    break;
                }
            }
        }
        if (!ok) {
            errln("FAIL  Expected "+ l + " x hi_IN . \"1,87,65,43,210\" (with Hindi digits), got \""
                    + out + "\"");
        } else {
            logln("Ok    " + l + " x hi_IN . \"" + out + "\"");
        }
    }

    /*
     * Internal test utility.
     */
    private void roundingTest(NumberFormat nf, double x, int maxFractionDigits, final String expected) {
        nf.setMaximumFractionDigits(maxFractionDigits);
        String out = nf.format(x);
        logln(x + " formats with " + maxFractionDigits + " fractional digits to " + out);
        if (!out.equals(expected))
            errln("FAIL: Expected " + expected);
    }

    /**
     * Upgrade to alphaWorks
     */
    @Test
    public void TestExponent() {
        DecimalFormatSymbols US = new DecimalFormatSymbols(Locale.US);
        DecimalFormat fmt1 = new DecimalFormat("0.###E0", US);
        DecimalFormat fmt2 = new DecimalFormat("0.###E+0", US);
        int n = 1234;
        expect2(fmt1, n, "1.234E3");
        expect2(fmt2, n, "1.234E+3");
        expect(fmt1, "1.234E+3", n); // Either format should parse "E+3"

    }

    /**
     * Upgrade to alphaWorks
     */
    @Test
    public void TestScientific() {

        DecimalFormatSymbols US = new DecimalFormatSymbols(Locale.US);

        // Test pattern round-trip
        final String PAT[] = { "#E0", "0.####E0", "00.000E00", "##0.####E000", "0.###E0;[0.###E0]" };
        int PAT_length = PAT.length;
        int DIGITS[] = {
                // min int, max int, min frac, max frac
                0, 1, 0, 0, // "#E0"
                1, 1, 0, 4, // "0.####E0"
                2, 2, 3, 3, // "00.000E00"
                1, 3, 0, 4, // "##0.####E000"
                1, 1, 0, 3, // "0.###E0;[0.###E0]"
        };
        for (int i = 0; i < PAT_length; ++i) {
            String pat = PAT[i];
            DecimalFormat df = new DecimalFormat(pat, US);
            String pat2 = df.toPattern();
            if (pat.equals(pat2)) {
                logln("Ok   Pattern rt \"" + pat + "\" . \"" + pat2 + "\"");
            } else {
                errln("FAIL Pattern rt \"" + pat + "\" . \"" + pat2 + "\"");
            }
            // Make sure digit counts match what we expect
            if (df.getMinimumIntegerDigits() != DIGITS[4 * i]
                    || df.getMaximumIntegerDigits() != DIGITS[4 * i + 1]
                            || df.getMinimumFractionDigits() != DIGITS[4 * i + 2]
                                    || df.getMaximumFractionDigits() != DIGITS[4 * i + 3]) {
                errln("FAIL \""+ pat+ "\" min/max int; min/max frac = "
                        + df.getMinimumIntegerDigits() + "/"
                        + df.getMaximumIntegerDigits() + ";"
                        + df.getMinimumFractionDigits() + "/"
                        + df.getMaximumFractionDigits() + ", expect "
                        + DIGITS[4 * i] + "/"
                        + DIGITS[4 * i + 1] + ";"
                        + DIGITS[4 * i + 2] + "/"
                        + DIGITS[4 * i + 3]);
            }
        }

        expect2(new DecimalFormat("#E0", US), 12345.0, "1.2345E4");
        expect(new DecimalFormat("0E0", US), 12345.0, "1E4");

        // pattern of NumberFormat.getScientificInstance(Locale.US) = "0.######E0" not "#E0"
        // so result = 1.234568E4 not 1.2345678901E4
        //when the pattern problem is finalized, delete comment mark'//'
        //of the following code
        expect2(NumberFormat.getScientificInstance(Locale.US), 12345.678901, "1.2345678901E4");
        logln("Testing NumberFormat.getScientificInstance(ULocale) ...");
        expect2(NumberFormat.getScientificInstance(ULocale.US), 12345.678901, "1.2345678901E4");

        expect(new DecimalFormat("##0.###E0", US), 12345.0, "12.34E3");
        expect(new DecimalFormat("##0.###E0", US), 12345.00001, "12.35E3");
        expect2(new DecimalFormat("##0.####E0", US), 12345, "12.345E3");

        // pattern of NumberFormat.getScientificInstance(Locale.US) = "0.######E0" not "#E0"
        // so result = 1.234568E4 not 1.2345678901E4
        expect2(NumberFormat.getScientificInstance(Locale.FRANCE), 12345.678901, "1,2345678901E4");
        logln("Testing NumberFormat.getScientificInstance(ULocale) ...");
        expect2(NumberFormat.getScientificInstance(ULocale.FRANCE), 12345.678901, "1,2345678901E4");

        expect(new DecimalFormat("##0.####E0", US), 789.12345e-9, "789.12E-9");
        expect2(new DecimalFormat("##0.####E0", US), 780.e-9, "780E-9");
        expect(new DecimalFormat(".###E0", US), 45678.0, ".457E5");
        expect2(new DecimalFormat(".###E0", US), 0, ".0E0");
        /*
        expect(new DecimalFormat[] { new DecimalFormat("#E0", US),
                                     new DecimalFormat("##E0", US),
                                     new DecimalFormat("####E0", US),
                                     new DecimalFormat("0E0", US),
                                     new DecimalFormat("00E0", US),
                                     new DecimalFormat("000E0", US),
                                   },
               new Long(45678000),
               new String[] { "4.5678E7",
                              "45.678E6",
                              "4567.8E4",
                              "5E7",
                              "46E6",
                              "457E5",
                            }
               );
        !
        ! Unroll this test into individual tests below...
        !
         */
        expect2(new DecimalFormat("#E0", US), 45678000, "4.5678E7");
        expect2(new DecimalFormat("##E0", US), 45678000, "45.678E6");
        expect2(new DecimalFormat("####E0", US), 45678000, "4567.8E4");
        expect(new DecimalFormat("0E0", US), 45678000, "5E7");
        expect(new DecimalFormat("00E0", US), 45678000, "46E6");
        expect(new DecimalFormat("000E0", US), 45678000, "457E5");
        /*
        expect(new DecimalFormat("###E0", US, status),
               new Object[] { new Double(0.0000123), "12.3E-6",
                              new Double(0.000123), "123E-6",
                              new Double(0.00123), "1.23E-3",
                              new Double(0.0123), "12.3E-3",
                              new Double(0.123), "123E-3",
                              new Double(1.23), "1.23E0",
                              new Double(12.3), "12.3E0",
                              new Double(123), "123E0",
                              new Double(1230), "1.23E3",
                             });
        !
        ! Unroll this test into individual tests below...
        !
         */
        expect2(new DecimalFormat("###E0", US), 0.0000123, "12.3E-6");
        expect2(new DecimalFormat("###E0", US), 0.000123, "123E-6");
        expect2(new DecimalFormat("###E0", US), 0.00123, "1.23E-3");
        expect2(new DecimalFormat("###E0", US), 0.0123, "12.3E-3");
        expect2(new DecimalFormat("###E0", US), 0.123, "123E-3");
        expect2(new DecimalFormat("###E0", US), 1.23, "1.23E0");
        expect2(new DecimalFormat("###E0", US), 12.3, "12.3E0");
        expect2(new DecimalFormat("###E0", US), 123.0, "123E0");
        expect2(new DecimalFormat("###E0", US), 1230.0, "1.23E3");
        /*
        expect(new DecimalFormat("0.#E+00", US, status),
               new Object[] { new Double(0.00012), "1.2E-04",
                              new Long(12000),     "1.2E+04",
                             });
        !
        ! Unroll this test into individual tests below...
        !
         */
        expect2(new DecimalFormat("0.#E+00", US), 0.00012, "1.2E-04");
        expect2(new DecimalFormat("0.#E+00", US), 12000, "1.2E+04");
    }

    /**
     * Upgrade to alphaWorks
     */
    @Test
    public void TestPad() {

        DecimalFormatSymbols US = new DecimalFormatSymbols(Locale.US);
        expect2(new DecimalFormat("*^##.##", US), 0, "^^^^0");
        expect2(new DecimalFormat("*^##.##", US), -1.3, "^-1.3");
        expect2(
                new DecimalFormat("##0.0####E0*_ 'g-m/s^2'", US),
                0,
                "0.0E0______ g-m/s^2");
        expect(
                new DecimalFormat("##0.0####E0*_ 'g-m/s^2'", US),
                1.0 / 3,
                "333.333E-3_ g-m/s^2");
        expect2(new DecimalFormat("##0.0####*_ 'g-m/s^2'", US), 0, "0.0______ g-m/s^2");
        expect(
                new DecimalFormat("##0.0####*_ 'g-m/s^2'", US),
                1.0 / 3,
                "0.33333__ g-m/s^2");

        // Test padding before a sign
        final String formatStr = "*x#,###,###,##0.0#;*x(###,###,##0.0#)";
        expect2(new DecimalFormat(formatStr, US), -10, "xxxxxxxxxx(10.0)");
        expect2(new DecimalFormat(formatStr, US), -1000, "xxxxxxx(1,000.0)");
        expect2(new DecimalFormat(formatStr, US), -1000000, "xxx(1,000,000.0)");
        expect2(new DecimalFormat(formatStr, US), -100.37, "xxxxxxxx(100.37)");
        expect2(new DecimalFormat(formatStr, US), -10456.37, "xxxxx(10,456.37)");
        expect2(new DecimalFormat(formatStr, US), -1120456.37, "xx(1,120,456.37)");
        expect2(new DecimalFormat(formatStr, US), -112045600.37, "(112,045,600.37)");
        expect2(new DecimalFormat(formatStr, US), -1252045600.37, "(1,252,045,600.37)");

        expect2(new DecimalFormat(formatStr, US), 10, "xxxxxxxxxxxx10.0");
        expect2(new DecimalFormat(formatStr, US), 1000, "xxxxxxxxx1,000.0");
        expect2(new DecimalFormat(formatStr, US), 1000000, "xxxxx1,000,000.0");
        expect2(new DecimalFormat(formatStr, US), 100.37, "xxxxxxxxxx100.37");
        expect2(new DecimalFormat(formatStr, US), 10456.37, "xxxxxxx10,456.37");
        expect2(new DecimalFormat(formatStr, US), 1120456.37, "xxxx1,120,456.37");
        expect2(new DecimalFormat(formatStr, US), 112045600.37, "xx112,045,600.37");
        expect2(new DecimalFormat(formatStr, US), 10252045600.37, "10,252,045,600.37");

        // Test padding between a sign and a number
        final String formatStr2 = "#,###,###,##0.0#*x;(###,###,##0.0#*x)";
        expect2(new DecimalFormat(formatStr2, US), -10, "(10.0xxxxxxxxxx)");
        expect2(new DecimalFormat(formatStr2, US), -1000, "(1,000.0xxxxxxx)");
        expect2(new DecimalFormat(formatStr2, US), -1000000, "(1,000,000.0xxx)");
        expect2(new DecimalFormat(formatStr2, US), -100.37, "(100.37xxxxxxxx)");
        expect2(new DecimalFormat(formatStr2, US), -10456.37, "(10,456.37xxxxx)");
        expect2(new DecimalFormat(formatStr2, US), -1120456.37, "(1,120,456.37xx)");
        expect2(new DecimalFormat(formatStr2, US), -112045600.37, "(112,045,600.37)");
        expect2(new DecimalFormat(formatStr2, US), -1252045600.37, "(1,252,045,600.37)");

        expect2(new DecimalFormat(formatStr2, US), 10, "10.0xxxxxxxxxxxx");
        expect2(new DecimalFormat(formatStr2, US), 1000, "1,000.0xxxxxxxxx");
        expect2(new DecimalFormat(formatStr2, US), 1000000, "1,000,000.0xxxxx");
        expect2(new DecimalFormat(formatStr2, US), 100.37, "100.37xxxxxxxxxx");
        expect2(new DecimalFormat(formatStr2, US), 10456.37, "10,456.37xxxxxxx");
        expect2(new DecimalFormat(formatStr2, US), 1120456.37, "1,120,456.37xxxx");
        expect2(new DecimalFormat(formatStr2, US), 112045600.37, "112,045,600.37xx");
        expect2(new DecimalFormat(formatStr2, US), 10252045600.37, "10,252,045,600.37");

        //testing the setPadCharacter(UnicodeString) and getPadCharacterString()
        DecimalFormat fmt = new DecimalFormat("#", US);
        char padString = 'P';
        fmt.setPadCharacter(padString);
        expectPad(fmt, "*P##.##", DecimalFormat.PAD_BEFORE_PREFIX, 5, padString);
        fmt.setPadCharacter('^');
        expectPad(fmt, "*^#", DecimalFormat.PAD_BEFORE_PREFIX, 1, '^');
        //commented untill implementation is complete
        /*  fmt.setPadCharacter((UnicodeString)"^^^");
          expectPad(fmt, "*^^^#", DecimalFormat.kPadBeforePrefix, 3, (UnicodeString)"^^^");
          padString.remove();
          padString.append((UChar)0x0061);
          padString.append((UChar)0x0302);
          fmt.setPadCharacter(padString);
          UChar patternChars[]={0x002a, 0x0061, 0x0302, 0x0061, 0x0302, 0x0023, 0x0000};
          UnicodeString pattern(patternChars);
          expectPad(fmt, pattern , DecimalFormat.kPadBeforePrefix, 4, padString);
         */
    }

    /**
     * Upgrade to alphaWorks
     */
    @Test
    public void TestPatterns2() {
        DecimalFormatSymbols US = new DecimalFormatSymbols(Locale.US);
        DecimalFormat fmt = new DecimalFormat("#", US);

        char hat = 0x005E; /*^*/

        expectPad(fmt, "*^#", DecimalFormat.PAD_BEFORE_PREFIX, 1, hat);
        expectPad(fmt, "$*^#", DecimalFormat.PAD_AFTER_PREFIX, 2, hat);
        expectPad(fmt, "#*^", DecimalFormat.PAD_BEFORE_SUFFIX, 1, hat);
        expectPad(fmt, "#$*^", DecimalFormat.PAD_AFTER_SUFFIX, 2, hat);
        expectPad(fmt, "$*^$#", -1);
        expectPad(fmt, "#$*^$", -1);
        expectPad(fmt, "'pre'#,##0*x'post'", DecimalFormat.PAD_BEFORE_SUFFIX, 12, (char) 0x0078 /*x*/);
        expectPad(fmt, "''#0*x", DecimalFormat.PAD_BEFORE_SUFFIX, 3, (char) 0x0078 /*x*/);
        expectPad(fmt, "'I''ll'*a###.##", DecimalFormat.PAD_AFTER_PREFIX, 10, (char) 0x0061 /*a*/);

        fmt.applyPattern("AA#,##0.00ZZ");
        fmt.setPadCharacter(hat);

        fmt.setFormatWidth(10);

        fmt.setPadPosition(DecimalFormat.PAD_BEFORE_PREFIX);
        expectPat(fmt, "*^AA#,##0.00ZZ");

        fmt.setPadPosition(DecimalFormat.PAD_BEFORE_SUFFIX);
        expectPat(fmt, "AA#,##0.00*^ZZ");

        fmt.setPadPosition(DecimalFormat.PAD_AFTER_SUFFIX);
        expectPat(fmt, "AA#,##0.00ZZ*^");

        //            12  3456789012
        String exp = "AA*^#,##0.00ZZ";
        fmt.setFormatWidth(12);
        fmt.setPadPosition(DecimalFormat.PAD_AFTER_PREFIX);
        expectPat(fmt, exp);

        fmt.setFormatWidth(13);
        //              12  34567890123
        expectPat(fmt, "AA*^##,##0.00ZZ");

        fmt.setFormatWidth(14);
        //              12  345678901234
        expectPat(fmt, "AA*^###,##0.00ZZ");

        fmt.setFormatWidth(15);
        //              12  3456789012345
        expectPat(fmt, "AA*^####,##0.00ZZ"); // This is the interesting case

        fmt.setFormatWidth(16);
        //              12  34567890123456
        expectPat(fmt, "AA*^#,###,##0.00ZZ");
    }

    @Test
    public void TestRegistration() {
        final ULocale SRC_LOC = ULocale.FRANCE;
        final ULocale SWAP_LOC = ULocale.US;

        class TestFactory extends SimpleNumberFormatFactory {
            NumberFormat currencyStyle;

            TestFactory() {
                super(SRC_LOC, true);
                currencyStyle = NumberFormat.getIntegerInstance(SWAP_LOC);
            }

            @Override
            public NumberFormat createFormat(ULocale loc, int formatType) {
                if (formatType == FORMAT_CURRENCY) {
                    return currencyStyle;
                }
                return null;
            }
        }

        NumberFormat f0 = NumberFormat.getIntegerInstance(SWAP_LOC);
        NumberFormat f1 = NumberFormat.getIntegerInstance(SRC_LOC);
        NumberFormat f2 = NumberFormat.getCurrencyInstance(SRC_LOC);
        Object key = NumberFormat.registerFactory(new TestFactory());
        NumberFormat f3 = NumberFormat.getCurrencyInstance(SRC_LOC);
        NumberFormat f4 = NumberFormat.getIntegerInstance(SRC_LOC);
        NumberFormat.unregister(key); // restore for other tests
        NumberFormat f5 = NumberFormat.getCurrencyInstance(SRC_LOC);

        float n = 1234.567f;
        logln("f0 swap int: " + f0.format(n));
        logln("f1 src int: " + f1.format(n));
        logln("f2 src cur: " + f2.format(n));
        logln("f3 reg cur: " + f3.format(n));
        logln("f4 reg int: " + f4.format(n));
        logln("f5 unreg cur: " + f5.format(n));

        if (!f3.format(n).equals(f0.format(n))) {
            errln("registered service did not match");
        }
        if (!f4.format(n).equals(f1.format(n))) {
            errln("registered service did not inherit");
        }
        if (!f5.format(n).equals(f2.format(n))) {
            errln("unregistered service did not match original");
        }
    }

    @Test
    public void TestScientific2() {
        // jb 2552
        DecimalFormat fmt = (DecimalFormat)NumberFormat.getCurrencyInstance();
        Number num = new Double(12.34);
        expect(fmt, num, "$12.34");
        fmt.setScientificNotation(true);
        expect(fmt, num, "$1.23E1");
        fmt.setScientificNotation(false);
        expect(fmt, num, "$12.34");
    }

    @Test
    public void TestScientificGrouping() {
        // jb 2552
        DecimalFormat fmt = new DecimalFormat("###.##E0");
        expect(fmt, .01234, "12.3E-3");
        expect(fmt, .1234, "123E-3");
        expect(fmt, 1.234, "1.23E0");
        expect(fmt, 12.34, "12.3E0");
        expect(fmt, 123.4, "123E0");
        expect(fmt, 1234, "1.23E3");
    }

    // additional coverage tests

    // sigh, can't have static inner classes, why not?

    static final class PI extends Number {
        /**
         * For serialization
         */
        private static final long serialVersionUID = -305601227915602172L;

        private PI() {}
        @Override
        public int intValue() { return (int)Math.PI; }
        @Override
        public long longValue() { return (long)Math.PI; }
        @Override
        public float  floatValue() { return (float)Math.PI; }
        @Override
        public double doubleValue() { return Math.PI; }
        @Override
        public byte byteValue() { return (byte)Math.PI; }
        @Override
        public short shortValue() { return (short)Math.PI; }

        public static final Number INSTANCE = new PI();
    }

    @Test
    public void TestCoverage() {
        NumberFormat fmt = NumberFormat.getNumberInstance(); // default locale
        logln(fmt.format(new BigInteger("1234567890987654321234567890987654321", 10)));

        fmt = NumberFormat.getScientificInstance(); // default locale

        logln(fmt.format(PI.INSTANCE));

        try {
            logln(fmt.format("12345"));
            errln("numberformat of string did not throw exception");
        }
        catch (Exception e) {
            logln("PASS: numberformat of string failed as expected");
        }

        int hash = fmt.hashCode();
        logln("hash code " + hash);

        logln("compare to string returns: " + fmt.equals(""));

        // For ICU 2.6 - alan
        DecimalFormatSymbols US = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("'*&'' '\u00A4' ''&*' #,##0.00", US);
        df.setCurrency(Currency.getInstance("INR"));
        expect2(df, 1.0, "*&' \u20B9 '&* 1.00");
        expect2(df, -2.0, "-*&' \u20B9 '&* 2.00");
        df.applyPattern("#,##0.00 '*&'' '\u00A4' ''&*'");
        expect2(df, 2.0, "2.00 *&' \u20B9 '&*");
        expect2(df, -1.0, "-1.00 *&' \u20B9 '&*");

        java.math.BigDecimal r;

        r = df.getRoundingIncrement();
        if (r != null) {
            errln("FAIL: rounding = " + r + ", expect null");
        }

        if (df.isScientificNotation()) {
            errln("FAIL: isScientificNotation = true, expect false");
        }

        df.applyPattern("0.00000");
        df.setScientificNotation(true);
        if (!df.isScientificNotation()) {
            errln("FAIL: isScientificNotation = false, expect true");
        }
        df.setMinimumExponentDigits((byte)2);
        if (df.getMinimumExponentDigits() != 2) {
            errln("FAIL: getMinimumExponentDigits = " +
                    df.getMinimumExponentDigits() + ", expect 2");
        }
        df.setExponentSignAlwaysShown(true);
        if (!df.isExponentSignAlwaysShown()) {
            errln("FAIL: isExponentSignAlwaysShown = false, expect true");
        }
        df.setSecondaryGroupingSize(0);
        if (df.getSecondaryGroupingSize() != 0) {
            errln("FAIL: getSecondaryGroupingSize = " +
                    df.getSecondaryGroupingSize() + ", expect 0");
        }
        expect2(df, 3.14159, "3.14159E+00");

        // DecimalFormatSymbols#getInstance
        DecimalFormatSymbols decsym1 = DecimalFormatSymbols.getInstance();
        DecimalFormatSymbols decsym2 = new DecimalFormatSymbols();
        if (!decsym1.equals(decsym2)) {
            errln("FAIL: DecimalFormatSymbols returned by getInstance()" +
                    "does not match new DecimalFormatSymbols().");
        }
        decsym1 = DecimalFormatSymbols.getInstance(Locale.JAPAN);
        decsym2 = DecimalFormatSymbols.getInstance(ULocale.JAPAN);
        if (!decsym1.equals(decsym2)) {
            errln("FAIL: DecimalFormatSymbols returned by getInstance(Locale.JAPAN)" +
                    "does not match the one returned by getInstance(ULocale.JAPAN).");
        }

        // DecimalFormatSymbols#getAvailableLocales/#getAvailableULocales
        Locale[] allLocales = DecimalFormatSymbols.getAvailableLocales();
        if (allLocales.length == 0) {
            errln("FAIL: Got a empty list for DecimalFormatSymbols.getAvailableLocales");
        } else {
            logln("PASS: " + allLocales.length +
                    " available locales returned by DecimalFormatSymbols.getAvailableLocales");
        }
        ULocale[] allULocales = DecimalFormatSymbols.getAvailableULocales();
        if (allULocales.length == 0) {
            errln("FAIL: Got a empty list for DecimalFormatSymbols.getAvailableLocales");
        } else {
            logln("PASS: " + allULocales.length +
                    " available locales returned by DecimalFormatSymbols.getAvailableULocales");
        }
    }

    @Test
    public void TestWhiteSpaceParsing() {
        DecimalFormatSymbols US = new DecimalFormatSymbols(Locale.US);
        DecimalFormat fmt = new DecimalFormat("a  b#0c  ", US);
        int n = 1234;
        expect(fmt, "a b1234c ", n);
        expect(fmt, "a   b1234c   ", n);
    }

    /**
     * Test currencies whose display name is a ChoiceFormat.
     */
    @Test
    public void TestComplexCurrency() {
        //  CLDR No Longer uses complex currency symbols.
        //  Skipping this test.
        //        Locale loc = new Locale("kn", "IN", "");
        //        NumberFormat fmt = NumberFormat.getCurrencyInstance(loc);

        //        expect2(fmt, 1.0, "Re.\u00a01.00");
        //        expect(fmt, 1.001, "Re.\u00a01.00"); // tricky
        //        expect2(fmt, 12345678.0, "Rs.\u00a01,23,45,678.00");
        //        expect2(fmt, 0.5, "Rs.\u00a00.50");
        //        expect2(fmt, -1.0, "-Re.\u00a01.00");
        //        expect2(fmt, -10.0, "-Rs.\u00a010.00");
    }

    @Test
    public void TestCurrencyKeyword() {
        ULocale locale = new ULocale("th_TH@currency=QQQ");
        NumberFormat format = NumberFormat.getCurrencyInstance(locale);
        String result = format.format(12.34f);
        if (!"QQQ12.34".equals(result)) {
            errln("got unexpected currency: " + result);
        }
    }

    /**
     * Test alternate numbering systems
     */
    @Test
    public void TestNumberingSystems() {
        class TestNumberingSystemItem {
            private final String localeName;
            private final double value;
            private final boolean isRBNF;
            private final String expectedResult;

            TestNumberingSystemItem(String loc, double val, boolean rbnf, String exp) {
                localeName  = loc;
                value = val;
                isRBNF = rbnf;
                expectedResult = exp;
            }
        }

        final TestNumberingSystemItem[] DATA = {
                new TestNumberingSystemItem( "en_US@numbers=thai",        1234.567, false, "\u0e51,\u0e52\u0e53\u0e54.\u0e55\u0e56\u0e57" ),
                new TestNumberingSystemItem( "en_US@numbers=thai",        1234.567, false, "\u0E51,\u0E52\u0E53\u0E54.\u0E55\u0E56\u0E57" ),
                new TestNumberingSystemItem( "en_US@numbers=hebr",        5678.0,   true,  "\u05D4\u05F3\u05EA\u05E8\u05E2\u05F4\u05D7" ),
                new TestNumberingSystemItem( "en_US@numbers=arabext",     1234.567, false, "\u06F1\u066c\u06F2\u06F3\u06F4\u066b\u06F5\u06F6\u06F7" ),
                new TestNumberingSystemItem( "de_DE@numbers=foobar",      1234.567, false, "1.234,567" ),
                new TestNumberingSystemItem( "ar_EG",                     1234.567, false, "\u0661\u066c\u0662\u0663\u0664\u066b\u0665\u0666\u0667" ),
                new TestNumberingSystemItem( "th_TH@numbers=traditional", 1234.567, false, "\u0E51,\u0E52\u0E53\u0E54.\u0E55\u0E56\u0E57" ), // fall back to native per TR35
                new TestNumberingSystemItem( "ar_MA",                     1234.567, false, "1.234,567" ),
                new TestNumberingSystemItem( "en_US@numbers=hanidec",     1234.567, false, "\u4e00,\u4e8c\u4e09\u56db.\u4e94\u516d\u4e03" ),
                new TestNumberingSystemItem( "ta_IN@numbers=native",      1234.567, false, "\u0BE7,\u0BE8\u0BE9\u0BEA.\u0BEB\u0BEC\u0BED" ),
                new TestNumberingSystemItem( "ta_IN@numbers=traditional", 1235.0,   true,  "\u0BF2\u0BE8\u0BF1\u0BE9\u0BF0\u0BEB" ),
                new TestNumberingSystemItem( "ta_IN@numbers=finance",     1234.567, false, "1,234.567" ), // fall back to default per TR35
                new TestNumberingSystemItem( "zh_TW@numbers=native",      1234.567, false, "\u4e00,\u4e8c\u4e09\u56db.\u4e94\u516d\u4e03" ),
                new TestNumberingSystemItem( "zh_TW@numbers=traditional", 1234.567, true,  "\u4E00\u5343\u4E8C\u767E\u4E09\u5341\u56DB\u9EDE\u4E94\u516D\u4E03" ),
                new TestNumberingSystemItem( "zh_TW@numbers=finance",     1234.567, true,  "\u58F9\u4EDF\u8CB3\u4F70\u53C3\u62FE\u8086\u9EDE\u4F0D\u9678\u67D2" )
        };


        for (TestNumberingSystemItem item : DATA) {
            ULocale loc = new ULocale(item.localeName);
            NumberFormat fmt = NumberFormat.getInstance(loc);
            if (item.isRBNF) {
                expect3(fmt,item.value,item.expectedResult);
            } else {
                expect2(fmt,item.value,item.expectedResult);
            }
        }
    }

    // Coverage tests for methods not being called otherwise.
    @Test
    public void TestNumberingSystemCoverage() {
        // Test getAvaliableNames
        String[] availableNames = NumberingSystem.getAvailableNames();
        if (availableNames == null || availableNames.length <= 0) {
            errln("ERROR: NumberingSystem.getAvailableNames() returned a null or empty array.");
        } else {
            boolean latnFound = false;
            for (String name : availableNames){
                if ("latn".equals(name)) {
                    latnFound = true;
                    break;
                }
            }

            if (!latnFound) {
                errln("ERROR: 'latn' numbering system not found on NumberingSystem.getAvailableNames().");
            }
        }

        // Test NumberingSystem.getInstance()
        NumberingSystem ns1 = NumberingSystem.getInstance();
        if (ns1 == null || ns1.isAlgorithmic()) {
            errln("ERROR: NumberingSystem.getInstance() returned a null or invalid NumberingSystem");
        }

        // Test NumberingSystem.getInstance(int,boolean,String)
        /* Parameters used: the ones used in the default constructor
         * radix = 10;
         * algorithmic = false;
         * desc = "0123456789";
         */
        NumberingSystem ns2 = NumberingSystem.getInstance(10, false, "0123456789");
        if (ns2 == null || ns2.isAlgorithmic()) {
            errln("ERROR: NumberingSystem.getInstance(int,boolean,String) returned a null or invalid NumberingSystem");
        }

        // Test NumberingSystem.getInstance(Locale)
        NumberingSystem ns3 = NumberingSystem.getInstance(Locale.ENGLISH);
        if (ns3 == null || ns3.isAlgorithmic()) {
            errln("ERROR: NumberingSystem.getInstance(Locale) returned a null or invalid NumberingSystem");
        }
    }

    @Test
    public void Test6816() {
        Currency cur1 = Currency.getInstance(new Locale("und", "PH"));

        NumberFormat nfmt = NumberFormat.getCurrencyInstance(new Locale("und", "PH"));
        DecimalFormatSymbols decsym = ((DecimalFormat)nfmt).getDecimalFormatSymbols();
        Currency cur2 = decsym.getCurrency();

        if ( !cur1.getCurrencyCode().equals("PHP") || !cur2.getCurrencyCode().equals("PHP")) {
            errln("FAIL: Currencies should match PHP: cur1 = "+cur1.getCurrencyCode()+"; cur2 = "+cur2.getCurrencyCode());
        }

    }

    @Test
    public void TestThreadedFormat() {

        class FormatTask implements Runnable {
            DecimalFormat fmt;
            StringBuffer buf;
            boolean inc;
            float num;

            FormatTask(DecimalFormat fmt, int index) {
                this.fmt = fmt;
                this.buf = new StringBuffer();
                this.inc = (index & 0x1) == 0;
                this.num = inc ? 0 : 10000;
            }

            @Override
            public void run() {
                if (inc) {
                    while (num < 10000) {
                        buf.append(fmt.format(num) + "\n");
                        num += 3.14159;
                    }
                } else {
                    while (num > 0) {
                        buf.append(fmt.format(num) + "\n");
                        num -= 3.14159;
                    }
                }
            }

            String result() {
                return buf.toString();
            }
        }

        DecimalFormat fmt = new DecimalFormat("0.####");
        FormatTask[] tasks = new FormatTask[8];
        for (int i = 0; i < tasks.length; ++i) {
            tasks[i] = new FormatTask(fmt, i);
        }

        TestUtil.runUntilDone(tasks);

        for (int i = 2; i < tasks.length; i++) {
            String str1 = tasks[i].result();
            String str2 = tasks[i-2].result();
            if (!str1.equals(str2)) {
                System.out.println("mismatch at " + i);
                System.out.println(str1);
                System.out.println(str2);
                errln("decimal format thread mismatch");

                break;
            }
            str1 = str2;
        }
    }

    @Test
    public void TestPerMill() {
        DecimalFormat fmt = new DecimalFormat("###.###\u2030");
        assertEquals("0.4857 x ###.###\u2030",
                "485.7\u2030", fmt.format(0.4857));

        DecimalFormatSymbols sym = new DecimalFormatSymbols(Locale.ENGLISH);
        sym.setPerMill('m');
        DecimalFormat fmt2 = new DecimalFormat("", sym);
        fmt2.applyLocalizedPattern("###.###m");
        assertEquals("0.4857 x ###.###m",
                "485.7m", fmt2.format(0.4857));
    }

    @Test
    public void TestIllegalPatterns() {
        // Test cases:
        // Prefix with "-:" for illegal patterns
        // Prefix with "+:" for legal patterns
        String DATA[] = {
                // Unquoted special characters in the suffix are illegal
                "-:000.000|###",
                "+:000.000'|###'",
        };
        for (int i=0; i<DATA.length; ++i) {
            String pat=DATA[i];
            boolean valid = pat.charAt(0) == '+';
            pat = pat.substring(2);
            Exception e = null;
            try {
                // locale doesn't matter here
                new DecimalFormat(pat);
            } catch (IllegalArgumentException e1) {
                e = e1;
            } catch (IndexOutOfBoundsException e1) {
                e = e1;
            }
            String msg = (e==null) ? "success" : e.getMessage();
            if ((e==null) == valid) {
                logln("Ok: pattern \"" + pat + "\": " + msg);
            } else {
                errln("FAIL: pattern \"" + pat + "\" should have " +
                        (valid?"succeeded":"failed") + "; got " + msg);
            }
        }
    }

    /**
     * Parse a CurrencyAmount using the given NumberFormat, with
     * the 'delim' character separating the number and the currency.
     */
    private static CurrencyAmount parseCurrencyAmount(String str, NumberFormat fmt,
            char delim)
                    throws ParseException {
        int i = str.indexOf(delim);
        return new CurrencyAmount(fmt.parse(str.substring(0,i)),
                Currency.getInstance(str.substring(i+1)));
    }

    /**
     * Return an integer representing the next token from this
     * iterator.  The integer will be an index into the given list, or
     * -1 if there are no more tokens, or -2 if the token is not on
     * the list.
     */
    private static int keywordIndex(String tok) {
        for (int i=0; i<KEYWORDS.length; ++i) {
            if (tok.equals(KEYWORDS[i])) {
                return i;
            }
        }
        return -1;
    }

    private static final String KEYWORDS[] = {
        /*0*/ "ref=", // <reference pattern to parse numbers>
        /*1*/ "loc=", // <locale for formats>
        /*2*/ "f:",   // <pattern or '-'> <number> <exp. string>
        /*3*/ "fp:",  // <pattern or '-'> <number> <exp. string> <exp. number>
        /*4*/ "rt:",  // <pattern or '-'> <(exp.) number> <(exp.) string>
        /*5*/ "p:",   // <pattern or '-'> <string> <exp. number>
        /*6*/ "perr:", // <pattern or '-'> <invalid string>
        /*7*/ "pat:", // <pattern or '-'> <exp. toPattern or '-' or 'err'>
        /*8*/ "fpc:", // <loc or '-'> <curr.amt> <exp. string> <exp. curr.amt>
        /*9*/ "strict=", // true or false
    };

    @SuppressWarnings("resource")  // InputStream is will be closed by the ResourceReader.
    @Test
    public void TestCases() {
        String caseFileName = "NumberFormatTestCases.txt";
        java.io.InputStream is = NumberFormatTest.class.getResourceAsStream(caseFileName);

        ResourceReader reader = new ResourceReader(is, caseFileName, "utf-8");
        TokenIterator tokens = new TokenIterator(reader);

        Locale loc = new Locale("en", "US", "");
        DecimalFormat ref = null, fmt = null;
        MeasureFormat mfmt = null;
        String pat = null, str = null, mloc = null;
        boolean strict = false;

        try {
            for (;;) {
                String tok = tokens.next();
                if (tok == null) {
                    break;
                }
                String where = "(" + tokens.getLineNumber() + ") ";
                int cmd = keywordIndex(tok);
                switch (cmd) {
                case 0:
                    // ref= <reference pattern>
                    ref = new DecimalFormat(tokens.next(),
                            new DecimalFormatSymbols(Locale.US));
                    ref.setParseStrict(strict);
                    logln("Setting reference pattern to:\t" + ref);
                    break;
                case 1:
                    // loc= <locale>
                    loc = LocaleUtility.getLocaleFromName(tokens.next());
                    pat = ((DecimalFormat) NumberFormat.getInstance(loc)).toPattern();
                    logln("Setting locale to:\t" + loc + ", \tand pattern to:\t" + pat);
                    break;
                case 2: // f:
                case 3: // fp:
                case 4: // rt:
                case 5: // p:
                    tok = tokens.next();
                    if (!tok.equals("-")) {
                        pat = tok;
                    }
                    try {
                        fmt = new DecimalFormat(pat, new DecimalFormatSymbols(loc));
                        fmt.setParseStrict(strict);
                    } catch (IllegalArgumentException iae) {
                        errln(where + "Pattern \"" + pat + '"');
                        iae.printStackTrace();
                        tokens.next(); // consume remaining tokens
                        //tokens.next();
                        if (cmd == 3) tokens.next();
                        continue;
                    }
                    str = null;
                    try {
                        if (cmd == 2 || cmd == 3 || cmd == 4) {
                            // f: <pattern or '-'> <number> <exp. string>
                            // fp: <pattern or '-'> <number> <exp. string> <exp. number>
                            // rt: <pattern or '-'> <number> <string>
                            String num = tokens.next();
                            str = tokens.next();
                            Number n = ref.parse(num);
                            assertEquals(where + '"' + pat + "\".format(" + num + ")",
                                    str, fmt.format(n));
                            if (cmd == 3) { // fp:
                                n = ref.parse(tokens.next());
                            }
                            if (cmd != 2) { // != f:
                                assertEquals(where + '"' + pat + "\".parse(\"" + str + "\")",
                                        n, fmt.parse(str));
                            }
                        }
                        // p: <pattern or '-'> <string to parse> <exp. number>
                        else {
                            str = tokens.next();
                            String expstr = tokens.next();
                            Number parsed = fmt.parse(str);
                            Number exp = ref.parse(expstr);
                            assertEquals(where + '"' + pat + "\".parse(\"" + str + "\")",
                                    exp, parsed);
                        }
                    } catch (ParseException e) {
                        errln(where + '"' + pat + "\".parse(\"" + str +
                                "\") threw an exception");
                        e.printStackTrace();
                    }
                    break;
                case 6:
                    // perr: <pattern or '-'> <invalid string>
                    errln("Under construction");
                    return;
                case 7:
                    // pat: <pattern> <exp. toPattern, or '-' or 'err'>
                    String testpat = tokens.next();
                    String exppat  = tokens.next();
                    boolean err    = exppat.equals("err");
                    if (testpat.equals("-")) {
                        if (err) {
                            errln("Invalid command \"pat: - err\" at " +  tokens.describePosition());
                            continue;
                        }
                        testpat = pat;
                    }
                    if (exppat.equals("-")) exppat = testpat;
                    try {
                        DecimalFormat f = null;
                        if (testpat == pat) { // [sic]
                            f = fmt;
                        } else {
                            f = new DecimalFormat(testpat);
                            f.setParseStrict(strict);
                        }
                        if (err) {
                            errln(where + "Invalid pattern \"" + testpat +
                                    "\" was accepted");
                        } else {
                            assertEquals(where + '"' + testpat + "\".toPattern()",
                                    exppat, f.toPattern());
                        }
                    } catch (IllegalArgumentException iae2) {
                        if (err) {
                            logln("Ok: " + where + "Invalid pattern \"" + testpat +
                                    "\" threw an exception");
                        } else {
                            errln(where + "Valid pattern \"" + testpat +
                                    "\" threw an exception");
                            iae2.printStackTrace();
                        }
                    }
                    break;
                case 8: // fpc:
                    tok = tokens.next();
                    if (!tok.equals("-")) {
                        mloc = tok;
                        ULocale l = new ULocale(mloc);
                        try {
                            mfmt = MeasureFormat.getCurrencyFormat(l);
                        } catch (IllegalArgumentException iae) {
                            errln(where + "Loc \"" + tok + '"');
                            iae.printStackTrace();
                            tokens.next(); // consume remaining tokens
                            tokens.next();
                            tokens.next();
                            continue;
                        }
                    }
                    str = null;
                    try {
                        // fpc: <loc or '-'> <curr.amt> <exp. string> <exp. curr.amt>
                        String currAmt = tokens.next();
                        str = tokens.next();
                        CurrencyAmount target = parseCurrencyAmount(currAmt, ref, '/');
                        String formatResult = mfmt.format(target);
                        assertEquals(where + "getCurrencyFormat(" + mloc + ").format(" + currAmt + ")",
                                str, formatResult);
                        target = parseCurrencyAmount(tokens.next(), ref, '/');
                        CurrencyAmount parseResult = (CurrencyAmount) mfmt.parseObject(str);
                        assertEquals(where + "getCurrencyFormat(" + mloc + ").parse(\"" + str + "\")",
                                target, parseResult);
                    } catch (ParseException e) {
                        errln(where + '"' + pat + "\".parse(\"" + str +
                                "\") threw an exception");
                        e.printStackTrace();
                    }
                    break;
                case 9: // strict= true or false
                    strict = "true".equalsIgnoreCase(tokens.next());
                    logln("Setting strict to:\t" + strict);
                    break;
                case -1:
                    errln("Unknown command \"" + tok + "\" at " + tokens.describePosition());
                    return;
                }
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                reader.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Test
    public void TestFieldPositionDecimal() {
        DecimalFormat nf = (DecimalFormat) android.icu.text.NumberFormat.getInstance(ULocale.ENGLISH);
        nf.setPositivePrefix("FOO");
        nf.setPositiveSuffix("BA");
        StringBuffer buffer = new StringBuffer();
        FieldPosition fp = new FieldPosition(NumberFormat.Field.DECIMAL_SEPARATOR);
        nf.format(35.47, buffer, fp);
        assertEquals("35.47", "FOO35.47BA", buffer.toString());
        assertEquals("fp begin", 5, fp.getBeginIndex());
        assertEquals("fp end", 6, fp.getEndIndex());
    }

    @Test
    public void TestFieldPositionInteger() {
        DecimalFormat nf = (DecimalFormat) android.icu.text.NumberFormat.getInstance(ULocale.ENGLISH);
        nf.setPositivePrefix("FOO");
        nf.setPositiveSuffix("BA");
        StringBuffer buffer = new StringBuffer();
        FieldPosition fp = new FieldPosition(NumberFormat.Field.INTEGER);
        nf.format(35.47, buffer, fp);
        assertEquals("35.47", "FOO35.47BA", buffer.toString());
        assertEquals("fp begin", 3, fp.getBeginIndex());
        assertEquals("fp end", 5, fp.getEndIndex());
    }

    @Test
    public void TestFieldPositionFractionButInteger() {
        DecimalFormat nf = (DecimalFormat) android.icu.text.NumberFormat.getInstance(ULocale.ENGLISH);
        nf.setPositivePrefix("FOO");
        nf.setPositiveSuffix("BA");
        StringBuffer buffer = new StringBuffer();
        FieldPosition fp = new FieldPosition(NumberFormat.Field.FRACTION);
        nf.format(35, buffer, fp);
        assertEquals("35", "FOO35BA", buffer.toString());
        assertEquals("fp begin", 5, fp.getBeginIndex());
        assertEquals("fp end", 5, fp.getEndIndex());
    }

    @Test
    public void TestFieldPositionFraction() {
        DecimalFormat nf = (DecimalFormat) android.icu.text.NumberFormat.getInstance(ULocale.ENGLISH);
        nf.setPositivePrefix("FOO");
        nf.setPositiveSuffix("BA");
        StringBuffer buffer = new StringBuffer();
        FieldPosition fp = new FieldPosition(NumberFormat.Field.FRACTION);
        nf.format(35.47, buffer, fp);
        assertEquals("35.47", "FOO35.47BA", buffer.toString());
        assertEquals("fp begin", 6, fp.getBeginIndex());
        assertEquals("fp end", 8, fp.getEndIndex());
    }

    @Test
    public void TestFieldPositionCurrency() {
        DecimalFormat nf = (DecimalFormat) android.icu.text.NumberFormat.getCurrencyInstance(Locale.US);
        double amount = 35.47;
        double negAmount = -34.567;
        FieldPosition cp = new FieldPosition(NumberFormat.Field.CURRENCY);

        StringBuffer buffer0 = new StringBuffer();
        nf.format(amount, buffer0, cp);
        assertEquals("$35.47", "$35.47", buffer0.toString());
        assertEquals("cp begin", 0, cp.getBeginIndex());
        assertEquals("cp end", 1, cp.getEndIndex());

        StringBuffer buffer01 = new StringBuffer();
        nf.format(negAmount, buffer01, cp);
        assertEquals("-$34.57", "-$34.57", buffer01.toString());
        assertEquals("cp begin", 1, cp.getBeginIndex());
        assertEquals("cp end", 2, cp.getEndIndex());

        nf.setCurrency(Currency.getInstance(Locale.FRANCE));
        StringBuffer buffer1 = new StringBuffer();
        nf.format(amount, buffer1, cp);
        assertEquals("€35.47", "€35.47", buffer1.toString());
        assertEquals("cp begin", 0, cp.getBeginIndex());
        assertEquals("cp end", 1, cp.getEndIndex());

        nf.setCurrency(Currency.getInstance(new Locale("fr", "ch", "")));
        StringBuffer buffer2 = new StringBuffer();
        nf.format(amount, buffer2, cp);
        assertEquals("CHF35.47", "CHF35.47", buffer2.toString());
        assertEquals("cp begin", 0, cp.getBeginIndex());
        assertEquals("cp end", 3, cp.getEndIndex());

        StringBuffer buffer20 = new StringBuffer();
        nf.format(negAmount, buffer20, cp);
        assertEquals("-CHF34.57", "-CHF34.57", buffer20.toString());
        assertEquals("cp begin", 1, cp.getBeginIndex());
        assertEquals("cp end", 4, cp.getEndIndex());

        nf = (DecimalFormat) android.icu.text.NumberFormat.getCurrencyInstance(Locale.FRANCE);
        StringBuffer buffer3 = new StringBuffer();
        nf.format(amount, buffer3, cp);
        assertEquals("35,47 €", "35,47 €", buffer3.toString());
        assertEquals("cp begin", 6, cp.getBeginIndex());
        assertEquals("cp end", 7, cp.getEndIndex());

        StringBuffer buffer4 = new StringBuffer();
        nf.format(negAmount, buffer4, cp);
        assertEquals("-34,57 €", "-34,57 €", buffer4.toString());
        assertEquals("cp begin", 7, cp.getBeginIndex());
        assertEquals("cp end", 8, cp.getEndIndex());

        nf.setCurrency(Currency.getInstance(new Locale("fr", "ch")));
        StringBuffer buffer5 = new StringBuffer();
        nf.format(negAmount, buffer5, cp);
        assertEquals("-34,57 CHF", "-34,57 CHF", buffer5.toString());
        assertEquals("cp begin", 7, cp.getBeginIndex());
        assertEquals("cp end", 10, cp.getEndIndex());

        NumberFormat plCurrencyFmt = NumberFormat.getInstance(new Locale("fr", "ch"), NumberFormat.PLURALCURRENCYSTYLE);
        StringBuffer buffer6 = new StringBuffer();
        plCurrencyFmt.format(negAmount, buffer6, cp);
        assertEquals("-34.57 francs suisses", "-34.57 francs suisses", buffer6.toString());
        assertEquals("cp begin", 7, cp.getBeginIndex());
        assertEquals("cp end", 21, cp.getEndIndex());

        // Positive value with PLURALCURRENCYSTYLE.
        plCurrencyFmt = NumberFormat.getInstance(new Locale("ja", "ch"), NumberFormat.PLURALCURRENCYSTYLE);
        StringBuffer buffer7 = new StringBuffer();
        plCurrencyFmt.format(amount, buffer7, cp);
        assertEquals("35.47スイス フラン", "35.47スイス フラン", buffer7.toString());
        assertEquals("cp begin", 5, cp.getBeginIndex());
        assertEquals("cp end", 12, cp.getEndIndex());

        // PLURALCURRENCYSTYLE for non-ASCII.
        plCurrencyFmt = NumberFormat.getInstance(new Locale("ja", "de"), NumberFormat.PLURALCURRENCYSTYLE);
        StringBuffer buffer8 = new StringBuffer();
        plCurrencyFmt.format(negAmount, buffer8, cp);
        assertEquals("-34.57ユーロ", "-34.57ユーロ", buffer8.toString());
        assertEquals("cp begin", 6, cp.getBeginIndex());
        assertEquals("cp end", 9, cp.getEndIndex());

        nf = (DecimalFormat) android.icu.text.NumberFormat.getCurrencyInstance(Locale.JAPAN);
        nf.setCurrency(Currency.getInstance(new Locale("ja", "jp")));
        StringBuffer buffer9 = new StringBuffer();
        nf.format(negAmount, buffer9, cp);
        assertEquals("-￥35", "-￥35", buffer9.toString());
        assertEquals("cp begin", 1, cp.getBeginIndex());
        assertEquals("cp end", 2, cp.getEndIndex());

        // Negative value with PLURALCURRENCYSTYLE.
        plCurrencyFmt = NumberFormat.getInstance(new Locale("ja", "ch"), NumberFormat.PLURALCURRENCYSTYLE);
        StringBuffer buffer10 = new StringBuffer();
        plCurrencyFmt.format(negAmount, buffer10, cp);
        assertEquals("-34.57スイス フラン", "-34.57スイス フラン", buffer10.toString());
        assertEquals("cp begin", 6, cp.getBeginIndex());
        assertEquals("cp end", 13, cp.getEndIndex());

        // Nagative value with PLURALCURRENCYSTYLE, Arabic digits.
        nf = (DecimalFormat) android.icu.text.NumberFormat.getCurrencyInstance(new Locale("ar", "eg"));
        plCurrencyFmt = NumberFormat.getInstance(new Locale("ar", "eg"), NumberFormat.PLURALCURRENCYSTYLE);
        StringBuffer buffer11 = new StringBuffer();
        plCurrencyFmt.format(negAmount, buffer11, cp);
        assertEquals("؜-٣٤٫٥٧ جنيه مصري", "؜-٣٤٫٥٧ جنيه مصري", buffer11.toString());
        assertEquals("cp begin", 8, cp.getBeginIndex());
        assertEquals("cp end", 17, cp.getEndIndex());
    }

    @Test
    public void TestRounding() {
        DecimalFormat nf = (DecimalFormat) android.icu.text.NumberFormat.getInstance(ULocale.ENGLISH);
        if (false) { // for debugging specific value
            nf.setRoundingMode(BigDecimal.ROUND_HALF_UP);
            checkRounding(nf, new BigDecimal("300.0300000000"), 0, new BigDecimal("0.020000000"));
        }
        // full tests
        int[] roundingIncrements = {1, 2, 5, 20, 50, 100};
        int[] testValues = {0, 300};
        for (int j = 0; j < testValues.length; ++j) {
            for (int mode = BigDecimal.ROUND_UP; mode < BigDecimal.ROUND_HALF_EVEN; ++mode) {
                nf.setRoundingMode(mode);
                for (int increment = 0; increment < roundingIncrements.length; ++increment) {
                    BigDecimal base = new BigDecimal(testValues[j]);
                    BigDecimal rInc = new BigDecimal(roundingIncrements[increment]);
                    checkRounding(nf,  base, 20, rInc);
                    rInc = new BigDecimal("1.000000000").divide(rInc);
                    checkRounding(nf,  base, 20, rInc);
                }
            }
        }
    }

    @Test
    public void TestRoundingPattern() {
        class TestRoundingPatternItem {
            String     pattern;
            double     roundingIncrement;
            double     testCase;
            String     expected;

            TestRoundingPatternItem(String pattern, double roundingIncrement, double testCase, String expected) {
                this.pattern = pattern;
                this.roundingIncrement = roundingIncrement;
                this.testCase = testCase;
                this.expected = expected;
            }
        };

        TestRoundingPatternItem []tests = {
                new TestRoundingPatternItem("##0.65", 0.65, 1.234, "1.30"),
                new TestRoundingPatternItem("#50", 50.0, 1230, "1250")
        };

        DecimalFormat df = (DecimalFormat) android.icu.text.NumberFormat.getInstance(ULocale.ENGLISH);
        String result;
        BigDecimal bd;
        for (int i = 0; i < tests.length; i++) {
            df.applyPattern(tests[i].pattern);

            result = df.format(tests[i].testCase);

            if (!tests[i].expected.equals(result)) {
                errln("String Pattern Rounding Test Failed: Pattern: \"" + tests[i].pattern + "\" Number: " + tests[i].testCase + " - Got: " + result + " Expected: " + tests[i].expected);
            }

            bd = new BigDecimal(tests[i].roundingIncrement);

            df.setRoundingIncrement(bd);

            result = df.format(tests[i].testCase);

            if (!tests[i].expected.equals(result)) {
                errln("BigDecimal Rounding Test Failed: Pattern: \"" + tests[i].pattern + "\" Number: " + tests[i].testCase + " - Got: " + result + " Expected: " + tests[i].expected);
            }
        }
    }

    @Test
    public void TestBigDecimalRounding() {
        String figure = "50.000000004";
        Double dbl = new Double(figure);
        BigDecimal dec = new BigDecimal(figure);

        DecimalFormat f = (DecimalFormat) NumberFormat.getInstance();
        f.applyPattern("00.00######");

        assertEquals("double format", "50.00", f.format(dbl));
        assertEquals("bigdec format", "50.00", f.format(dec));

        int maxFracDigits = f.getMaximumFractionDigits();
        BigDecimal roundingIncrement = new BigDecimal("1").movePointLeft(maxFracDigits);

        f.setRoundingIncrement(roundingIncrement);
        f.setRoundingMode(BigDecimal.ROUND_DOWN);
        assertEquals("Rounding down", f.format(dbl), f.format(dec));

        f.setRoundingIncrement(roundingIncrement);
        f.setRoundingMode(BigDecimal.ROUND_HALF_UP);
        assertEquals("Rounding half up", f.format(dbl), f.format(dec));
    }

    void checkRounding(DecimalFormat nf, BigDecimal base, int iterations, BigDecimal increment) {
        nf.setRoundingIncrement(increment.toBigDecimal());
        BigDecimal lastParsed = new BigDecimal(Integer.MIN_VALUE); // used to make sure that rounding is monotonic
        for (int i = -iterations; i <= iterations; ++i) {
            BigDecimal iValue = base.add(increment.multiply(new BigDecimal(i)).movePointLeft(1));
            BigDecimal smallIncrement = new BigDecimal("0.00000001");
            if (iValue.signum() != 0) {
                smallIncrement.multiply(iValue); // scale unless zero
            }
            // we not only test the value, but some values in a small range around it.
            lastParsed = checkRound(nf, iValue.subtract(smallIncrement), lastParsed);
            lastParsed = checkRound(nf, iValue, lastParsed);
            lastParsed = checkRound(nf, iValue.add(smallIncrement), lastParsed);
        }
    }

    private BigDecimal checkRound(DecimalFormat nf, BigDecimal iValue, BigDecimal lastParsed) {
        String formatedBigDecimal = nf.format(iValue);
        String formattedDouble = nf.format(iValue.doubleValue());
        if (!equalButForTrailingZeros(formatedBigDecimal, formattedDouble)) {

            errln("Failure at: " + iValue + " (" + iValue.doubleValue() + ")"
                    + ",\tRounding-mode: " + roundingModeNames[nf.getRoundingMode()]
                            + ",\tRounding-increment: " + nf.getRoundingIncrement()
                            + ",\tdouble: " + formattedDouble
                            + ",\tBigDecimal: " + formatedBigDecimal);

        } else {
            logln("Value: " + iValue
                    + ",\tRounding-mode: " + roundingModeNames[nf.getRoundingMode()]
                            + ",\tRounding-increment: " + nf.getRoundingIncrement()
                            + ",\tdouble: " + formattedDouble
                            + ",\tBigDecimal: " + formatedBigDecimal);
        }
        try {
            // Number should have compareTo(...)
            BigDecimal parsed = toBigDecimal(nf.parse(formatedBigDecimal));
            if (lastParsed.compareTo(parsed) > 0) {
                errln("Rounding wrong direction!: " + lastParsed + " > " + parsed);
            }
            lastParsed = parsed;
        } catch (ParseException e) {
            errln("Parse Failure with: " + formatedBigDecimal);
        }
        return lastParsed;
    }

    static BigDecimal toBigDecimal(Number number) {
        return number instanceof BigDecimal ? (BigDecimal) number
                : number instanceof BigInteger ? new BigDecimal((BigInteger)number)
        : number instanceof java.math.BigDecimal ? new BigDecimal((java.math.BigDecimal)number)
                : number instanceof Double ? new BigDecimal(number.doubleValue())
        : number instanceof Float ? new BigDecimal(number.floatValue())
                : new BigDecimal(number.longValue());
    }

    static String[] roundingModeNames = {
        "ROUND_UP", "ROUND_DOWN", "ROUND_CEILING", "ROUND_FLOOR",
        "ROUND_HALF_UP", "ROUND_HALF_DOWN", "ROUND_HALF_EVEN",
        "ROUND_UNNECESSARY"
    };

    private static boolean equalButForTrailingZeros(String formatted1, String formatted2) {
        if (formatted1.length() == formatted2.length()) return formatted1.equals(formatted2);
        return stripFinalZeros(formatted1).equals(stripFinalZeros(formatted2));
    }

    private static String stripFinalZeros(String formatted) {
        int len1 = formatted.length();
        char ch;
        while (len1 > 0 && ((ch = formatted.charAt(len1-1)) == '0' || ch == '.')) --len1;
        if (len1==1 && ((ch = formatted.charAt(len1-1)) == '-')) --len1;
        return formatted.substring(0,len1);
    }

    //------------------------------------------------------------------
    // Support methods
    //------------------------------------------------------------------

    // Format-Parse test
    public void expect2(NumberFormat fmt, Number n, String exp) {
        // Don't round-trip format test, since we explicitly do it
        expect(fmt, n, exp, false);
        expect(fmt, exp, n);
    }
    // Format-Parse test
    public void expect3(NumberFormat fmt, Number n, String exp) {
        // Don't round-trip format test, since we explicitly do it
        expect_rbnf(fmt, n, exp, false);
        expect_rbnf(fmt, exp, n);
    }

    // Format-Parse test (convenience)
    public void expect2(NumberFormat fmt, double n, String exp) {
        expect2(fmt, new Double(n), exp);
    }
    // Format-Parse test (convenience)
    public void expect3(NumberFormat fmt, double n, String exp) {
        expect3(fmt, new Double(n), exp);
    }

    // Format-Parse test (convenience)
    public void expect2(NumberFormat fmt, long n, String exp) {
        expect2(fmt, new Long(n), exp);
    }
    // Format-Parse test (convenience)
    public void expect3(NumberFormat fmt, long n, String exp) {
        expect3(fmt, new Long(n), exp);
    }

    // Format test
    public void expect(NumberFormat fmt, Number n, String exp, boolean rt) {
        StringBuffer saw = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        fmt.format(n, saw, pos);
        String pat = ((DecimalFormat)fmt).toPattern();
        if (saw.toString().equals(exp)) {
            logln("Ok   " + n + " x " +
                    pat + " = \"" +
                    saw + "\"");
            // We should be able to round-trip the formatted string =>
            // number => string (but not the other way around: number
            // => string => number2, might have number2 != number):
            if (rt) {
                try {
                    Number n2 = fmt.parse(exp);
                    StringBuffer saw2 = new StringBuffer();
                    fmt.format(n2, saw2, pos);
                    if (!saw2.toString().equals(exp)) {
                        errln("expect() format test rt, locale " + fmt.getLocale(ULocale.VALID_LOCALE) +
                                ", FAIL \"" + exp + "\" => " + n2 + " => \"" + saw2 + '"');
                    }
                } catch (ParseException e) {
                    errln("expect() format test rt, locale " + fmt.getLocale(ULocale.VALID_LOCALE) +
                            ", " + e.getMessage());
                    return;
                }
            }
        } else {
            errln("expect() format test, locale " + fmt.getLocale(ULocale.VALID_LOCALE) +
                    ", FAIL " + n + " x " + pat + " = \"" + saw + "\", expected \"" + exp + "\"");
        }
    }
    // Format test
    public void expect_rbnf(NumberFormat fmt, Number n, String exp, boolean rt) {
        StringBuffer saw = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        fmt.format(n, saw, pos);
        if (saw.toString().equals(exp)) {
            logln("Ok   " + n + " = \"" +
                    saw + "\"");
            // We should be able to round-trip the formatted string =>
            // number => string (but not the other way around: number
            // => string => number2, might have number2 != number):
            if (rt) {
                try {
                    Number n2 = fmt.parse(exp);
                    StringBuffer saw2 = new StringBuffer();
                    fmt.format(n2, saw2, pos);
                    if (!saw2.toString().equals(exp)) {
                        errln("expect_rbnf() format test rt, locale " + fmt.getLocale(ULocale.VALID_LOCALE) +
                                ", FAIL \"" + exp + "\" => " + n2 + " => \"" + saw2 + '"');
                    }
                } catch (ParseException e) {
                    errln("expect_rbnf() format test rt, locale " + fmt.getLocale(ULocale.VALID_LOCALE) +
                            ", " + e.getMessage());
                    return;
                }
            }
        } else {
            errln("expect_rbnf() format test, locale " + fmt.getLocale(ULocale.VALID_LOCALE) +
                    ", FAIL " + n + " = \"" + saw + "\", expected \"" + exp + "\"");
        }
    }

    // Format test (convenience)
    public void expect(NumberFormat fmt, Number n, String exp) {
        expect(fmt, n, exp, true);
    }

    // Format test (convenience)
    public void expect(NumberFormat fmt, double n, String exp) {
        expect(fmt, new Double(n), exp);
    }

    // Format test (convenience)
    public void expect(NumberFormat fmt, long n, String exp) {
        expect(fmt, new Long(n), exp);
    }

    // Parse test
    public void expect(NumberFormat fmt, String str, Number n) {
        Number num = null;
        try {
            num = fmt.parse(str);
        } catch (ParseException e) {
            errln(e.getMessage());
            return;
        }
        String pat = ((DecimalFormat)fmt).toPattern();
        // A little tricky here -- make sure Double(12345.0) and
        // Long(12345) match.
        if (num.equals(n) || num.doubleValue() == n.doubleValue()) {
            logln("Ok   \"" + str + "\" x " +
                    pat + " = " +
                    num);
        } else {
            errln("expect() parse test, locale " + fmt.getLocale(ULocale.VALID_LOCALE) +
                    ", FAIL \"" + str + "\" x " + pat + " = " + num + ", expected " + n);
        }
    }

    // Parse test
    public void expect_rbnf(NumberFormat fmt, String str, Number n) {
        Number num = null;
        try {
            num = fmt.parse(str);
        } catch (ParseException e) {
            errln(e.getMessage());
            return;
        }
        // A little tricky here -- make sure Double(12345.0) and
        // Long(12345) match.
        if (num.equals(n) || num.doubleValue() == n.doubleValue()) {
            logln("Ok   \"" + str + " = " +
                    num);
        } else {
            errln("expect_rbnf() parse test, locale " + fmt.getLocale(ULocale.VALID_LOCALE) +
                    ", FAIL \"" + str + " = " + num + ", expected " + n);
        }
    }

    // Parse test (convenience)
    public void expect(NumberFormat fmt, String str, double n) {
        expect(fmt, str, new Double(n));
    }

    // Parse test (convenience)
    public void expect(NumberFormat fmt, String str, long n) {
        expect(fmt, str, new Long(n));
    }

    private void expectCurrency(NumberFormat nf, Currency curr,
            double value, String string) {
        DecimalFormat fmt = (DecimalFormat) nf;
        if (curr != null) {
            fmt.setCurrency(curr);
        }
        String s = fmt.format(value).replace('\u00A0', ' ');

        if (s.equals(string)) {
            logln("Ok: " + value + " x " + curr + " => " + s);
        } else {
            errln("FAIL: " + value + " x " + curr + " => " + s +
                    ", expected " + string);
        }
    }

    public void expectPad(DecimalFormat fmt, String pat, int pos) {
        expectPad(fmt, pat, pos, 0, (char)0);
    }

    public void expectPad(DecimalFormat fmt, final String pat, int pos, int width, final char pad) {
        int apos = 0, awidth = 0;
        char apadStr;
        try {
            fmt.applyPattern(pat);
            apos = fmt.getPadPosition();
            awidth = fmt.getFormatWidth();
            apadStr = fmt.getPadCharacter();
        } catch (Exception e) {
            apos = -1;
            awidth = width;
            apadStr = pad;
        }

        if (apos == pos && awidth == width && apadStr == pad) {
            logln("Ok   \"" + pat + "\" pos="
                    + apos + ((pos == -1) ? "" : " width=" + awidth + " pad=" + apadStr));
        } else {
            errln("FAIL \"" + pat + "\" pos=" + apos + " width="
                    + awidth + " pad=" + apadStr + ", expected "
                    + pos + " " + width + " " + pad);
        }
    }

    public void expectPat(DecimalFormat fmt, final String exp) {
        String pat = fmt.toPattern();
        if (pat.equals(exp)) {
            logln("Ok   \"" + pat + "\"");
        } else {
            errln("FAIL \"" + pat + "\", expected \"" + exp + "\"");
        }
    }


    private void expectParseCurrency(NumberFormat fmt, Currency expected, String text) {
        ParsePosition pos = new ParsePosition(0);
        CurrencyAmount currencyAmount = fmt.parseCurrency(text, pos);
        assertTrue("Parse of " + text + " should have succeeded.", pos.getIndex() > 0);
        assertEquals("Currency should be correct.", expected, currencyAmount.getCurrency());
    }

    @Test
    public void TestJB3832(){
        ULocale locale = new ULocale("pt_PT@currency=PTE");
        NumberFormat format = NumberFormat.getCurrencyInstance(locale);
        Currency curr = Currency.getInstance(locale);
        logln("\nName of the currency is: " + curr.getName(locale, Currency.LONG_NAME, new boolean[] {false}));
        CurrencyAmount cAmt = new CurrencyAmount(1150.50, curr);
        logln("CurrencyAmount object's hashCode is: " + cAmt.hashCode()); //cover hashCode
        String str = format.format(cAmt);
        String expected = "1,150$50\u00a0\u200b";
        if(!expected.equals(str)){
            errln("Did not get the expected output Expected: "+expected+" Got: "+ str);
        }
    }

    @Test
    public void TestStrictParse() {
        String[] pass = {
                "0",           // single zero before end of text is not leading
                "0 ",          // single zero at end of number is not leading
                "0.",          // single zero before period (or decimal, it's ambiguous) is not leading
                "0,",          // single zero before comma (not group separator) is not leading
                "0.0",         // single zero before decimal followed by digit is not leading
                "0. ",         // same as above before period (or decimal) is not leading
                "0.100,5",     // comma stops parse of decimal (no grouping)
                ".00",         // leading decimal is ok, even with zeros
                "1234567",     // group separators are not required
                "12345, ",     // comma not followed by digit is not a group separator, but end of number
                "1,234, ",     // if group separator is present, group sizes must be appropriate
                "1,234,567",   // ...secondary too
                "0E",          // an exponent not followed by zero or digits is not an exponent
                "00",          // leading zero before zero - used to be error - see ticket #7913
                "012",         // leading zero before digit - used to be error - see ticket #7913
                "0,456",       // leading zero before group separator - used to be error - see ticket #7913
        };
        String[] fail = {
                "1,2",       // wrong number of digits after group separator
                ",0",        // leading group separator before zero
                ",1",        // leading group separator before digit
                ",.02",      // leading group separator before decimal
                "1,.02",     // group separator before decimal
                "1,,200",    // multiple group separators
                "1,45",      // wrong number of digits in primary group
                "1,45 that", // wrong number of digits in primary group
                "1,45.34",   // wrong number of digits in primary group
                "1234,567",  // wrong number of digits in secondary group
                "12,34,567", // wrong number of digits in secondary group
                "1,23,456,7890", // wrong number of digits in primary and secondary groups
        };

        DecimalFormat nf = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
        runStrictParseBatch(nf, pass, fail);

        String[] scientificPass = {
                "0E2",      // single zero before exponent is ok
                "1234E2",   // any number of digits before exponent is ok
                "1,234E",   // an exponent string not followed by zero or digits is not an exponent
                "00E2",     // leading zeroes now allowed in strict mode - see ticket #
        };
        String[] scientificFail = {
                "1,234E2",  // group separators with exponent fail
        };

        nf = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
        runStrictParseBatch(nf, scientificPass, scientificFail);

        String[] mixedPass = {
                "12,34,567",
                "12,34,567,",
                "12,34,567, that",
                "12,34,567 that",
        };
        String[] mixedFail = {
                "12,34,56",
                "12,34,56,",
                "12,34,56, that ",
                "12,34,56 that",
        };

        nf = new DecimalFormat("#,##,##0.#");
        runStrictParseBatch(nf, mixedPass, mixedFail);
    }

    void runStrictParseBatch(DecimalFormat nf, String[] pass, String[] fail) {
        nf.setParseStrict(false);
        runStrictParseTests("should pass", nf, pass, true);
        runStrictParseTests("should also pass", nf, fail, true);
        nf.setParseStrict(true);
        runStrictParseTests("should still pass", nf, pass, true);
        runStrictParseTests("should fail", nf, fail, false);
    }

    void runStrictParseTests(String msg, DecimalFormat nf, String[] tests, boolean pass) {
        logln("");
        logln("pattern: '" + nf.toPattern() + "'");
        logln(msg);
        for (int i = 0; i < tests.length; ++i) {
            String str = tests[i];
            ParsePosition pp = new ParsePosition(0);
            Number n = nf.parse(str, pp);
            String formatted = n != null ? nf.format(n) : "null";
            String err = pp.getErrorIndex() == -1 ? "" : "(error at " + pp.getErrorIndex() + ")";
            if ((err.length() == 0) != pass) {
                errln("'" + str + "' parsed '" +
                        str.substring(0, pp.getIndex()) +
                        "' returned " + n + " formats to '" +
                        formatted + "' " + err);
            } else {
                if (err.length() > 0) {
                    err = "got expected " + err;
                }
                logln("'" + str + "' parsed '" +
                        str.substring(0, pp.getIndex()) +
                        "' returned " + n + " formats to '" +
                        formatted + "' " + err);
            }
        }
    }

    @Test
    public void TestJB5251(){
        //save default locale
        ULocale defaultLocale = ULocale.getDefault();
        ULocale.setDefault(new ULocale("qr_QR"));
        try {
            NumberFormat.getInstance();
        }
        catch (Exception e) {
            errln("Numberformat threw exception for non-existent locale. It should use the default.");
        }
        //reset default locale
        ULocale.setDefault(defaultLocale);
    }

    @Test
    public void TestParseReturnType() {
        String[] defaultNonBigDecimals = {
                "123",      // Long
                "123.0",    // Long
                "0.0",      // Long
                "12345678901234567890"      // BigInteger
        };

        String[] doubles = {
                "-0.0",
                "NaN",
                "\u221E"    // Infinity
        };

        DecimalFormatSymbols sym = new DecimalFormatSymbols(Locale.US);
        DecimalFormat nf = new DecimalFormat("#.#", sym);

        if (nf.isParseBigDecimal()) {
            errln("FAIL: isParseDecimal() must return false by default");
        }

        // isParseBigDecimal() is false
        for (int i = 0; i < defaultNonBigDecimals.length; i++) {
            try {
                Number n = nf.parse(defaultNonBigDecimals[i]);
                if (n instanceof BigDecimal) {
                    errln("FAIL: parse returns BigDecimal instance");
                }
            } catch (ParseException e) {
                errln("parse of '" + defaultNonBigDecimals[i] + "' threw exception: " + e);
            }
        }
        // parse results for doubls must be always Double
        for (int i = 0; i < doubles.length; i++) {
            try {
                Number n = nf.parse(doubles[i]);
                if (!(n instanceof Double)) {
                    errln("FAIL: parse does not return Double instance");
                }
            } catch (ParseException e) {
                errln("parse of '" + doubles[i] + "' threw exception: " + e);
            }
        }

        // force this DecimalFormat to return BigDecimal
        nf.setParseBigDecimal(true);
        if (!nf.isParseBigDecimal()) {
            errln("FAIL: isParseBigDecimal() must return true");
        }

        // isParseBigDecimal() is true
        for (int i = 0; i < defaultNonBigDecimals.length; i++) {
            try {
                Number n = nf.parse(defaultNonBigDecimals[i]);
                if (!(n instanceof BigDecimal)) {
                    errln("FAIL: parse does not return BigDecimal instance");
                }
            } catch (ParseException e) {
                errln("parse of '" + defaultNonBigDecimals[i] + "' threw exception: " + e);
            }
        }
        // parse results for doubls must be always Double
        for (int i = 0; i < doubles.length; i++) {
            try {
                Number n = nf.parse(doubles[i]);
                if (!(n instanceof Double)) {
                    errln("FAIL: parse does not return Double instance");
                }
            } catch (ParseException e) {
                errln("parse of '" + doubles[i] + "' threw exception: " + e);
            }
        }
    }

    @Test
    public void TestNonpositiveMultiplier() {
        DecimalFormat df = new DecimalFormat("0");

        // test zero multiplier

        try {
            df.setMultiplier(0);

            // bad
            errln("DecimalFormat.setMultiplier(0) did not throw an IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // good
        }

        // test negative multiplier

        try {
            df.setMultiplier(-1);

            if (df.getMultiplier() != -1) {
                errln("DecimalFormat.setMultiplier(-1) did not change the multiplier to -1");
                return;
            }

            // good
        } catch (IllegalArgumentException ex) {
            // bad
            errln("DecimalFormat.setMultiplier(-1) threw an IllegalArgumentException");
            return;
        }

        expect(df, "1122.123", -1122.123);
        expect(df, "-1122.123", 1122.123);
        expect(df, "1.2", -1.2);
        expect(df, "-1.2", 1.2);

        expect2(df, Long.MAX_VALUE, BigInteger.valueOf(Long.MAX_VALUE).negate().toString());
        expect2(df, Long.MIN_VALUE, BigInteger.valueOf(Long.MIN_VALUE).negate().toString());
        expect2(df, Long.MAX_VALUE / 2, BigInteger.valueOf(Long.MAX_VALUE / 2).negate().toString());
        expect2(df, Long.MIN_VALUE / 2, BigInteger.valueOf(Long.MIN_VALUE / 2).negate().toString());

        expect2(df, BigDecimal.valueOf(Long.MAX_VALUE), BigDecimal.valueOf(Long.MAX_VALUE).negate().toString());
        expect2(df, BigDecimal.valueOf(Long.MIN_VALUE), BigDecimal.valueOf(Long.MIN_VALUE).negate().toString());

        expect2(df, java.math.BigDecimal.valueOf(Long.MAX_VALUE), java.math.BigDecimal.valueOf(Long.MAX_VALUE).negate().toString());
        expect2(df, java.math.BigDecimal.valueOf(Long.MIN_VALUE), java.math.BigDecimal.valueOf(Long.MIN_VALUE).negate().toString());
    }

    @Test
    public void TestJB5358() {
        int numThreads = 10;
        String numstr = "12345";
        double expected = 12345;
        DecimalFormatSymbols sym = new DecimalFormatSymbols(Locale.US);
        DecimalFormat fmt = new DecimalFormat("#.#", sym);
        ArrayList errors = new ArrayList();

        ParseThreadJB5358[] threads = new ParseThreadJB5358[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new ParseThreadJB5358((DecimalFormat)fmt.clone(), numstr, expected, errors);
            threads[i].start();
        }
        for (int i = 0; i < numThreads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        if (errors.size() != 0) {
            StringBuffer errBuf = new StringBuffer();
            for (int i = 0; i < errors.size(); i++) {
                errBuf.append((String)errors.get(i));
                errBuf.append("\n");
            }
            errln("FAIL: " + errBuf);
        }
    }

    static private class ParseThreadJB5358 extends Thread {
        private final DecimalFormat decfmt;
        private final String numstr;
        private final double expect;
        private final ArrayList errors;

        public ParseThreadJB5358(DecimalFormat decfmt, String numstr, double expect, ArrayList errors) {
            this.decfmt = decfmt;
            this.numstr = numstr;
            this.expect = expect;
            this.errors = errors;
        }

        @Override
        public void run() {
            for (int i = 0; i < 10000; i++) {
                try {
                    Number n = decfmt.parse(numstr);
                    if (n.doubleValue() != expect) {
                        synchronized(errors) {
                            errors.add(new String("Bad parse result - expected:" + expect + " actual:" + n.doubleValue()));
                        }
                    }
                } catch (Throwable t) {
                    synchronized(errors) {
                        errors.add(new String(t.getClass().getName() + " - " + t.getMessage()));
                    }
                }
            }
        }
    }

    @Test
    public void TestSetCurrency() {
        DecimalFormatSymbols decf1 = DecimalFormatSymbols.getInstance(ULocale.US);
        DecimalFormatSymbols decf2 = DecimalFormatSymbols.getInstance(ULocale.US);
        decf2.setCurrencySymbol("UKD");
        DecimalFormat format1 = new DecimalFormat("000.000", decf1);
        DecimalFormat format2 = new DecimalFormat("000.000", decf2);
        Currency euro = Currency.getInstance("EUR");
        format1.setCurrency(euro);
        format2.setCurrency(euro);
        assertEquals("Reset with currency symbol", format1, format2);
    }

    /*
     * Testing the method public StringBuffer format(Object number, ...)
     */
    @Test
    public void TestFormat() {
        NumberFormat nf = NumberFormat.getInstance();
        StringBuffer sb = new StringBuffer("dummy");
        FieldPosition fp = new FieldPosition(0);

        // Tests when "if (number instanceof Long)" is true
        try {
            nf.format(new Long("0"), sb, fp);
        } catch (Exception e) {
            errln("NumberFormat.format(Object number, ...) was not suppose to "
                    + "return an exception for a Long object. Error: " + e);
        }

        // Tests when "else if (number instanceof BigInteger)" is true
        try {
            nf.format((Object)new BigInteger("0"), sb, fp);
        } catch (Exception e) {
            errln("NumberFormat.format(Object number, ...) was not suppose to "
                    + "return an exception for a BigInteger object. Error: " + e);
        }

        // Tests when "else if (number instanceof java.math.BigDecimal)" is true
        try {
            nf.format((Object)new java.math.BigDecimal("0"), sb, fp);
        } catch (Exception e) {
            errln("NumberFormat.format(Object number, ...) was not suppose to "
                    + "return an exception for a java.math.BigDecimal object. Error: " + e);
        }

        // Tests when "else if (number instanceof android.icu.math.BigDecimal)" is true
        try {
            nf.format((Object)new android.icu.math.BigDecimal("0"), sb, fp);
        } catch (Exception e) {
            errln("NumberFormat.format(Object number, ...) was not suppose to "
                    + "return an exception for a android.icu.math.BigDecimal object. Error: " + e);
        }

        // Tests when "else if (number instanceof CurrencyAmount)" is true
        try {
            CurrencyAmount ca = new CurrencyAmount(0.0, Currency.getInstance(new ULocale("en_US")));
            nf.format((Object)ca, sb, fp);
        } catch (Exception e) {
            errln("NumberFormat.format(Object number, ...) was not suppose to "
                    + "return an exception for a CurrencyAmount object. Error: " + e);
        }

        // Tests when "else if (number instanceof Number)" is true
        try {
            nf.format(0.0, sb, fp);
        } catch (Exception e) {
            errln("NumberFormat.format(Object number, ...) was not suppose to "
                    + "to return an exception for a Number object. Error: " + e);
        }

        // Tests when "else" is true
        try {
            nf.format(new Object(), sb, fp);
            errln("NumberFormat.format(Object number, ...) was suppose to "
                    + "return an exception for an invalid object.");
        } catch (Exception e) {
        }

        try {
            nf.format(new String("dummy"), sb, fp);
            errln("NumberFormat.format(Object number, ...) was suppose to "
                    + "return an exception for an invalid object.");
        } catch (Exception e) {
        }
    }

    /*
     * Coverage tests for the implementation of abstract format methods not being called otherwise
     */
    public void TestFormatAbstractImplCoverage() {
        NumberFormat df = DecimalFormat.getInstance(Locale.ENGLISH);
        NumberFormat cdf = CompactDecimalFormat.getInstance(Locale.ENGLISH, CompactDecimalFormat.CompactStyle.SHORT);
        NumberFormat rbf = new RuleBasedNumberFormat(ULocale.ENGLISH, RuleBasedNumberFormat.SPELLOUT);

        /*
         *  Test  NumberFormat.format(BigDecimal,StringBuffer,FieldPosition)
         */
        StringBuffer sb = new StringBuffer();
        String result = df.format(new BigDecimal(2000.43), sb, new FieldPosition(0)).toString();
        if (!"2,000.43".equals(result)) {
            errln("DecimalFormat failed. Expected: 2,000.43 - Actual: " + result);
        }

        sb.delete(0, sb.length());
        result = cdf.format(new BigDecimal(2000.43), sb, new FieldPosition(0)).toString();
        if (!"2K".equals(result)) {
            errln("DecimalFormat failed. Expected: 2K - Actual: " + result);
        }

        sb.delete(0, sb.length());
        result = rbf.format(new BigDecimal(2000.43), sb, new FieldPosition(0)).toString();
        if (!"two thousand point four three".equals(result)) {
            errln("DecimalFormat failed. Expected: 'two thousand point four three' - Actual: '" + result + "'");
        }
    }

    /*
     * Tests the method public final static NumberFormat getInstance(int style) public static NumberFormat
     * getInstance(Locale inLocale, int style) public static NumberFormat getInstance(ULocale desiredLocale, int choice)
     */
    @Test
    public void TestGetInstance() {
        // Tests "public final static NumberFormat getInstance(int style)"
        int maxStyle = NumberFormat.STANDARDCURRENCYSTYLE;

        int[] invalid_cases = { NumberFormat.NUMBERSTYLE - 1, NumberFormat.NUMBERSTYLE - 2,
                maxStyle + 1, maxStyle + 2 };

        for (int i = NumberFormat.NUMBERSTYLE; i < maxStyle; i++) {
            try {
                NumberFormat.getInstance(i);
            } catch (Exception e) {
                errln("NumberFormat.getInstance(int style) was not suppose to "
                        + "return an exception for passing value of " + i);
            }
        }

        for (int i = 0; i < invalid_cases.length; i++) {
            try {
                NumberFormat.getInstance(invalid_cases[i]);
                errln("NumberFormat.getInstance(int style) was suppose to "
                        + "return an exception for passing value of " + invalid_cases[i]);
            } catch (Exception e) {
            }
        }

        // Tests "public static NumberFormat getInstance(Locale inLocale, int style)"
        String[] localeCases = { "en_US", "fr_FR", "de_DE", "jp_JP" };

        for (int i = NumberFormat.NUMBERSTYLE; i < maxStyle; i++) {
            for (int j = 0; j < localeCases.length; j++) {
                try {
                    NumberFormat.getInstance(new Locale(localeCases[j]), i);
                } catch (Exception e) {
                    errln("NumberFormat.getInstance(Locale inLocale, int style) was not suppose to "
                            + "return an exception for passing value of " + localeCases[j] + ", " + i);
                }
            }
        }

        // Tests "public static NumberFormat getInstance(ULocale desiredLocale, int choice)"
        // Tests when "if (choice < NUMBERSTYLE || choice > PLURALCURRENCYSTYLE)" is true
        for (int i = 0; i < invalid_cases.length; i++) {
            try {
                NumberFormat.getInstance((ULocale) null, invalid_cases[i]);
                errln("NumberFormat.getInstance(ULocale inLocale, int choice) was not suppose to "
                        + "return an exception for passing value of " + invalid_cases[i]);
            } catch (Exception e) {
            }
        }
    }

    /*
     * Tests the class public static abstract class NumberFormatFactory
     */
    @Test
    public void TestNumberFormatFactory() {
        /*
         * The following class allows the method public NumberFormat createFormat(Locale loc, int formatType) to be
         * tested.
         */
        class TestFactory extends NumberFormatFactory {
            @Override
            public Set<String> getSupportedLocaleNames() {
                return null;
            }

            @Override
            public NumberFormat createFormat(ULocale loc, int formatType) {
                return null;
            }
        }

        /*
         * The following class allows the method public NumberFormat createFormat(ULocale loc, int formatType) to be
         * tested.
         */
        class TestFactory1 extends NumberFormatFactory {
            @Override
            public Set<String> getSupportedLocaleNames() {
                return null;
            }

            @Override
            public NumberFormat createFormat(Locale loc, int formatType) {
                return null;
            }
        }

        TestFactory tf = new TestFactory();
        TestFactory1 tf1 = new TestFactory1();

        /*
         * Tests the method public boolean visible()
         */
        if (tf.visible() != true) {
            errln("NumberFormatFactory.visible() was suppose to return true.");
        }

        /*
         * Tests the method public NumberFormat createFormat(Locale loc, int formatType)
         */
        if (tf.createFormat(new Locale(""), 0) != null) {
            errln("NumberFormatFactory.createFormat(Locale loc, int formatType) " + "was suppose to return null");
        }

        /*
         * Tests the method public NumberFormat createFormat(ULocale loc, int formatType)
         */
        if (tf1.createFormat(new ULocale(""), 0) != null) {
            errln("NumberFormatFactory.createFormat(ULocale loc, int formatType) " + "was suppose to return null");
        }
    }

    /*
     * Tests the class public static abstract class SimpleNumberFormatFactory extends NumberFormatFactory
     */
    @Test
    public void TestSimpleNumberFormatFactory() {
        class TestSimpleNumberFormatFactory extends SimpleNumberFormatFactory {
            /*
             * Tests the method public SimpleNumberFormatFactory(Locale locale)
             */
            TestSimpleNumberFormatFactory() {
                super(new Locale(""));
            }
        }
        @SuppressWarnings("unused")
        TestSimpleNumberFormatFactory tsnff = new TestSimpleNumberFormatFactory();
    }

    /*
     * Tests the method public static ULocale[] getAvailableLocales()
     */
    @SuppressWarnings("static-access")
    @Test
    public void TestGetAvailableLocales() {
        // Tests when "if (shim == null)" is true
        @SuppressWarnings("serial")
        class TestGetAvailableLocales extends NumberFormat {
            @Override
            public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
                return null;
            }

            @Override
            public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
                return null;
            }

            @Override
            public StringBuffer format(BigInteger number, StringBuffer toAppendTo, FieldPosition pos) {
                return null;
            }

            @Override
            public StringBuffer format(java.math.BigDecimal number, StringBuffer toAppendTo, FieldPosition pos) {
                return null;
            }

            @Override
            public StringBuffer format(BigDecimal number, StringBuffer toAppendTo, FieldPosition pos) {
                return null;
            }

            @Override
            public Number parse(String text, ParsePosition parsePosition) {
                return null;
            }
        }

        try {
            TestGetAvailableLocales test = new TestGetAvailableLocales();
            test.getAvailableLocales();
        } catch (Exception e) {
            errln("NumberFormat.getAvailableLocales() was not suppose to "
                    + "return an exception when getting getting available locales.");
        }
    }

    /*
     * Tests the method public void setMinimumIntegerDigits(int newValue)
     */
    @Test
    public void TestSetMinimumIntegerDigits() {
        NumberFormat nf = NumberFormat.getInstance();
        // For valid array, it is displayed as {min value, max value}
        // Tests when "if (minimumIntegerDigits > maximumIntegerDigits)" is true
        int[][] cases = { { -1, 0 }, { 0, 1 }, { 1, 0 }, { 2, 0 }, { 2, 1 }, { 10, 0 } };
        int[] expectedMax = { 0, 1, 1, 2, 2, 10 };
        if (cases.length != expectedMax.length) {
            errln("Can't continue test case method TestSetMinimumIntegerDigits "
                    + "since the test case arrays are unequal.");
        } else {
            for (int i = 0; i < cases.length; i++) {
                nf.setMaximumIntegerDigits(cases[i][1]);
                nf.setMinimumIntegerDigits(cases[i][0]);
                if (nf.getMaximumIntegerDigits() != expectedMax[i]) {
                    errln("NumberFormat.setMinimumIntegerDigits(int newValue "
                            + "did not return an expected result for parameter " + cases[i][1] + " and " + cases[i][0]
                                    + " and expected " + expectedMax[i] + " but got " + nf.getMaximumIntegerDigits());
                }
            }
        }
    }

    /*
     * Tests the method public int getRoundingMode() public void setRoundingMode(int roundingMode)
     */
    @Test
    public void TestRoundingMode() {
        @SuppressWarnings("serial")
        class TestRoundingMode extends NumberFormat {
            @Override
            public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
                return null;
            }

            @Override
            public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
                return null;
            }

            @Override
            public StringBuffer format(BigInteger number, StringBuffer toAppendTo, FieldPosition pos) {
                return null;
            }

            @Override
            public StringBuffer format(java.math.BigDecimal number, StringBuffer toAppendTo, FieldPosition pos) {
                return null;
            }

            @Override
            public StringBuffer format(BigDecimal number, StringBuffer toAppendTo, FieldPosition pos) {
                return null;
            }

            @Override
            public Number parse(String text, ParsePosition parsePosition) {
                return null;
            }
        }
        TestRoundingMode tgrm = new TestRoundingMode();

        // Tests the function 'public void setRoundingMode(int roundingMode)'
        try {
            tgrm.setRoundingMode(0);
            errln("NumberFormat.setRoundingMode(int) was suppose to return an exception");
        } catch (Exception e) {
        }

        // Tests the function 'public int getRoundingMode()'
        try {
            tgrm.getRoundingMode();
            errln("NumberFormat.getRoundingMode() was suppose to return an exception");
        } catch (Exception e) {
        }
    }

    /*
     * Testing lenient decimal/grouping separator parsing
     */
    @Test
    public void TestLenientSymbolParsing() {
        DecimalFormat fmt = new DecimalFormat();
        DecimalFormatSymbols sym = new DecimalFormatSymbols();

        expect(fmt, "12\u300234", 12.34);

        // Ticket#7345 - case 1
        // Even strict parsing, the decimal separator set in the symbols
        // should be successfully parsed.

        sym.setDecimalSeparator('\u3002');

        // non-strict
        fmt.setDecimalFormatSymbols(sym);

        // strict - failed before the fix for #7345
        fmt.setParseStrict(true);
        expect(fmt, "23\u300245", 23.45);
        fmt.setParseStrict(false);


        // Ticket#7345 - case 2
        // Decimal separator variants other than DecimalFormatSymbols.decimalSeparator
        // should not hide the grouping separator DecimalFormatSymbols.groupingSeparator.
        sym.setDecimalSeparator('.');
        sym.setGroupingSeparator(',');
        fmt.setDecimalFormatSymbols(sym);

        expect(fmt, "1,234.56", 1234.56);

        sym.setGroupingSeparator('\uFF61');
        fmt.setDecimalFormatSymbols(sym);

        expect(fmt, "2\uFF61345.67", 2345.67);

        // Ticket#7128
        //
        sym.setGroupingSeparator(',');
        fmt.setDecimalFormatSymbols(sym);

        String skipExtSepParse = ICUConfig.get("android.icu.text.DecimalFormat.SkipExtendedSeparatorParsing", "false");
        if (skipExtSepParse.equals("true")) {
            // When the property SkipExtendedSeparatorParsing is true,
            // DecimalFormat does not use the extended equivalent separator
            // data and only uses the one in DecimalFormatSymbols.
            expect(fmt, "23 456", 23);
        } else {
            // Lenient separator parsing is enabled by default.
            // A space character below is interpreted as a
            // group separator, even ',' is used as grouping
            // separator in the symbols.
            expect(fmt, "12 345", 12345);
        }
    }

    /*
     * Testing currency driven max/min fraction digits problem
     * reported by ticket#7282
     */
    @Test
    public void TestCurrencyFractionDigits() {
        double value = 99.12345;

        // Create currency instance
        NumberFormat cfmt  = NumberFormat.getCurrencyInstance(new ULocale("ja_JP"));
        String text1 = cfmt.format(value);

        // Reset the same currency and format the test value again
        cfmt.setCurrency(cfmt.getCurrency());
        String text2 = cfmt.format(value);

        // output1 and output2 must be identical
        if (!text1.equals(text2)) {
            errln("NumberFormat.format() should return the same result - text1="
                    + text1 + " text2=" + text2);
        }
    }

    /*
     * Testing rounding to negative zero problem
     * reported by ticket#7609
     */
    @Test
    public void TestNegZeroRounding() {

        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
        df.setRoundingMode(MathContext.ROUND_HALF_UP);
        df.setMinimumFractionDigits(1);
        df.setMaximumFractionDigits(1);
        String text1 = df.format(-0.01);

        df.setRoundingIncrement(0.1);
        String text2 = df.format(-0.01);

        // output1 and output2 must be identical
        if (!text1.equals(text2)) {
            errln("NumberFormat.format() should return the same result - text1="
                    + text1 + " text2=" + text2);
        }

    }

    @Test
    public void TestCurrencyAmountCoverage() {
        CurrencyAmount ca, cb;

        try {
            ca = new CurrencyAmount(null, null);
            errln("NullPointerException should have been thrown.");
        } catch (NullPointerException ex) {
        }
        try {
            ca = new CurrencyAmount(new Integer(0), null);
            errln("NullPointerException should have been thrown.");
        } catch (NullPointerException ex) {
        }

        ca = new CurrencyAmount(new Integer(0), Currency.getInstance(new ULocale("ja_JP")));
        cb = new CurrencyAmount(new Integer(1), Currency.getInstance(new ULocale("ja_JP")));
        if (ca.equals(null)) {
            errln("Comparison should return false.");
        }
        if (!ca.equals(ca)) {
            errln("Comparision should return true.");
        }
        if (ca.equals(cb)) {
            errln("Comparison should return false.");
        }
    }

    @Test
    public void TestExponentParse() {
        ParsePosition parsePos = new ParsePosition(0);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat fmt = new DecimalFormat("#####", symbols);
        Number result = fmt.parse("5.06e-27", parsePos);
        if ( result.doubleValue() != 5.06E-27 || parsePos.getIndex() != 8) {
            errln("ERROR: ERROR: parse failed - expected 5.06E-27, 8; got " + result.doubleValue() + ", " + parsePos.getIndex());
        }
    }

    @Test
    public void TestExplicitParents() {
        // We use these for testing because decimal and grouping separators will be inherited from es_419
        // starting with CLDR 2.0
        String[] DATA = {
                "es", "CO", "", "1.250,75",
                "es", "ES", "", "1.250,75",
                "es", "GQ", "", "1.250,75",
                "es", "MX", "", "1,250.75",
                "es", "US", "", "1,250.75",
                "es", "VE", "", "1.250,75",

        };

        for (int i=0; i<DATA.length; i+=4) {
            Locale locale = new Locale(DATA[i], DATA[i+1], DATA[i+2]);
            NumberFormat fmt = NumberFormat.getInstance(locale);
            String s = fmt.format(1250.75);
            if (s.equals(DATA[i+3])) {
                logln("Ok: 1250.75 x " + locale + " => " + s);
            } else {
                errln("FAIL: 1250.75 x " + locale + " => " + s +
                        ", expected " + DATA[i+3]);
            }
        }
    }

    /*
     * Test case for #9240
     * ICU4J 49.1 DecimalFormat did not clone the internal object holding
     * formatted text attribute information properly. Therefore, DecimalFormat
     * created by cloning may return incorrect results or may throw an exception
     * when formatToCharacterIterator is invoked from multiple threads.
     */
    @Test
    public void TestFormatToCharacterIteratorThread() {
        final int COUNT = 10;

        DecimalFormat fmt1 = new DecimalFormat("#0");
        DecimalFormat fmt2 = (DecimalFormat)fmt1.clone();

        int[] res1 = new int[COUNT];
        int[] res2 = new int[COUNT];

        Thread t1 = new Thread(new FormatCharItrTestThread(fmt1, 1, res1));
        Thread t2 = new Thread(new FormatCharItrTestThread(fmt2, 100, res2));

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            //TODO
        }

        int val1 = res1[0];
        int val2 = res2[0];

        for (int i = 0; i < COUNT; i++) {
            if (res1[i] != val1) {
                errln("Inconsistent first run limit in test thread 1");
            }
            if (res2[i] != val2) {
                errln("Inconsistent first run limit in test thread 2");
            }
        }
    }

    @Test
    public void TestParseMaxDigits() {
        DecimalFormat fmt = new DecimalFormat();
        String number = "100000000000";
        int newParseMax = number.length() - 1;

        fmt.setParseMaxDigits(-1);

        /* Default value is 1000 */
        if (fmt.getParseMaxDigits() != 1000) {
            errln("Fail valid value checking in setParseMaxDigits.");
        }

        try {
            if (fmt.parse(number).doubleValue() == Float.POSITIVE_INFINITY) {
                errln("Got Infinity but should NOT when parsing number: " + number);
            }

            fmt.setParseMaxDigits(newParseMax);

            if (fmt.parse(number).doubleValue() != Float.POSITIVE_INFINITY) {
                errln("Did not get Infinity but should when parsing number: " + number);
            }
        } catch (ParseException ex) {

        }
    }

    private static class FormatCharItrTestThread implements Runnable {
        private final NumberFormat fmt;
        private final int num;
        private final int[] result;

        FormatCharItrTestThread(NumberFormat fmt, int num, int[] result) {
            this.fmt = fmt;
            this.num = num;
            this.result = result;
        }

        @Override
        public void run() {
            for (int i = 0; i < result.length; i++) {
                AttributedCharacterIterator acitr = fmt.formatToCharacterIterator(num);
                acitr.first();
                result[i] = acitr.getRunLimit();
            }
        }
    }

    @Test
    public void TestRoundingBehavior() {
        final Object[][] TEST_CASES = {
                {
                    ULocale.US,                             // ULocale - null for default locale
                    "#.##",                                 // Pattern
                    Integer.valueOf(BigDecimal.ROUND_DOWN), // Rounding Mode or null (implicit)
                    Double.valueOf(0.0d),                   // Rounding increment, Double or BigDecimal, or null (implicit)
                    Double.valueOf(123.4567d),              // Input value, Long, Double, BigInteger or BigDecimal
                    "123.45"                                // Expected result, null for exception
                },
                {
                    ULocale.US,
                    "#.##",
                    null,
                    Double.valueOf(0.1d),
                    Double.valueOf(123.4567d),
                    "123.5"
                },
                {
                    ULocale.US,
                    "#.##",
                    Integer.valueOf(BigDecimal.ROUND_DOWN),
                    Double.valueOf(0.1d),
                    Double.valueOf(123.4567d),
                    "123.4"
                },
                {
                    ULocale.US,
                    "#.##",
                    Integer.valueOf(BigDecimal.ROUND_UNNECESSARY),
                    null,
                    Double.valueOf(123.4567d),
                    null
                },
                {
                    ULocale.US,
                    "#.##",
                    Integer.valueOf(BigDecimal.ROUND_DOWN),
                    null,
                    Long.valueOf(1234),
                    "1234"
                },
        };

        int testNum = 1;

        for (Object[] testCase : TEST_CASES) {
            // 0: locale
            // 1: pattern
            ULocale locale = testCase[0] == null ? ULocale.getDefault() : (ULocale)testCase[0];
            String pattern = (String)testCase[1];

            DecimalFormat fmt = new DecimalFormat(pattern, DecimalFormatSymbols.getInstance(locale));

            // 2: rounding mode
            Integer roundingMode = null;
            if (testCase[2] != null) {
                roundingMode = (Integer)testCase[2];
                fmt.setRoundingMode(roundingMode);
            }

            // 3: rounding increment
            if (testCase[3] != null) {
                if (testCase[3] instanceof Double) {
                    fmt.setRoundingIncrement((Double)testCase[3]);
                } else if (testCase[3] instanceof BigDecimal) {
                    fmt.setRoundingIncrement((BigDecimal)testCase[3]);
                } else if (testCase[3] instanceof java.math.BigDecimal) {
                    fmt.setRoundingIncrement((java.math.BigDecimal)testCase[3]);
                }
            }

            // 4: input number
            String s = null;
            boolean bException = false;
            try {
                s = fmt.format(testCase[4]);
            } catch (ArithmeticException e) {
                bException = true;
            }

            if (bException) {
                if (testCase[5] != null) {
                    errln("Test case #" + testNum + ": ArithmeticException was thrown.");
                }
            } else {
                if (testCase[5] == null) {
                    errln("Test case #" + testNum +
                            ": ArithmeticException must be thrown, but got formatted result: " +
                            s);
                } else {
                    assertEquals("Test case #" + testNum, testCase[5], s);
                }
            }

            testNum++;
        }
    }

    @Test
    public void TestSignificantDigits() {
        double input[] = {
                0, 0,
                123, -123,
                12345, -12345,
                123.45, -123.45,
                123.44501, -123.44501,
                0.001234, -0.001234,
                0.00000000123, -0.00000000123,
                0.0000000000000000000123, -0.0000000000000000000123,
                1.2, -1.2,
                0.0000000012344501, -0.0000000012344501,
                123445.01, -123445.01,
                12344501000000000000000000000000000.0, -12344501000000000000000000000000000.0,
        };
        String[] expected = {
                "0.00", "0.00",
                "123", "-123",
                "12345", "-12345",
                "123.45", "-123.45",
                "123.45", "-123.45",
                "0.001234", "-0.001234",
                "0.00000000123", "-0.00000000123",
                "0.0000000000000000000123", "-0.0000000000000000000123",
                "1.20", "-1.20",
                "0.0000000012345", "-0.0000000012345",
                "123450", "-123450",
                "12345000000000000000000000000000000", "-12345000000000000000000000000000000",
        };
        DecimalFormat numberFormat =
                (DecimalFormat) NumberFormat.getInstance(ULocale.US);
        numberFormat.setSignificantDigitsUsed(true);
        numberFormat.setMinimumSignificantDigits(3);
        numberFormat.setMaximumSignificantDigits(5);
        numberFormat.setGroupingUsed(false);
        for (int i = 0; i < input.length; i++) {
            assertEquals("TestSignificantDigits", expected[i], numberFormat.format(input[i]));
        }
    }

    @Test
    public void TestBug9936() {
        DecimalFormat numberFormat =
                (DecimalFormat) NumberFormat.getInstance(ULocale.US);
        assertFalse("", numberFormat.areSignificantDigitsUsed());

        numberFormat.setSignificantDigitsUsed(true);
        assertTrue("", numberFormat.areSignificantDigitsUsed());

        numberFormat.setSignificantDigitsUsed(false);
        assertFalse("", numberFormat.areSignificantDigitsUsed());

        numberFormat.setMinimumSignificantDigits(3);
        assertTrue("", numberFormat.areSignificantDigitsUsed());

        numberFormat.setSignificantDigitsUsed(false);
        numberFormat.setMaximumSignificantDigits(6);
        assertTrue("", numberFormat.areSignificantDigitsUsed());
    }

    @Test
    public void TestShowZero() {
        DecimalFormat numberFormat =
                (DecimalFormat) NumberFormat.getInstance(ULocale.US);
        numberFormat.setSignificantDigitsUsed(true);
        numberFormat.setMaximumSignificantDigits(3);
        assertEquals("TestShowZero", "0", numberFormat.format(0.0));
    }

    @Test
    public void TestCurrencyPlurals() {
        String[][] tests = {
                {"en", "USD", "1", "1 US dollar"},
                {"en", "USD", "1.0", "1.0 US dollars"},
                {"en", "USD", "1.00", "1.00 US dollars"},
                {"en", "USD", "1.99", "1.99 US dollars"},
                {"en", "AUD", "1", "1 Australian dollar"},
                {"en", "AUD", "1.00", "1.00 Australian dollars"},
                {"sl", "USD", "1", "1 ameri\u0161ki dolar"},
                {"sl", "USD", "2", "2 ameri\u0161ka dolarja"},
                {"sl", "USD", "3", "3 ameri\u0161ki dolarji"},
                {"sl", "USD", "5", "5 ameriških dolarjev"},
                {"fr", "USD", "1.99", "1,99 dollar des États-Unis"},
                {"ru", "RUB", "1", "1 \u0440\u043E\u0441\u0441\u0438\u0439\u0441\u043A\u0438\u0439 \u0440\u0443\u0431\u043B\u044C"},
                {"ru", "RUB", "2", "2 \u0440\u043E\u0441\u0441\u0438\u0439\u0441\u043A\u0438\u0445 \u0440\u0443\u0431\u043B\u044F"},
                {"ru", "RUB", "5", "5 \u0440\u043E\u0441\u0441\u0438\u0439\u0441\u043A\u0438\u0445 \u0440\u0443\u0431\u043B\u0435\u0439"},
        };
        for (String test[] : tests) {
            DecimalFormat numberFormat = (DecimalFormat) DecimalFormat.getInstance(new ULocale(test[0]), NumberFormat.PLURALCURRENCYSTYLE);
            numberFormat.setCurrency(Currency.getInstance(test[1]));
            double number = Double.parseDouble(test[2]);
            int dotPos = test[2].indexOf('.');
            int decimals = dotPos < 0 ? 0 : test[2].length() - dotPos - 1;
            int digits = dotPos < 0 ? test[2].length() : test[2].length() - 1;
            numberFormat.setMaximumFractionDigits(decimals);
            numberFormat.setMinimumFractionDigits(decimals);
            String actual = numberFormat.format(number);
            assertEquals(test[0] + "\t" + test[1] + "\t" + test[2], test[3], actual);
            numberFormat.setMaximumSignificantDigits(digits);
            numberFormat.setMinimumSignificantDigits(digits);
            actual = numberFormat.format(number);
            assertEquals(test[0] + "\t" + test[1] + "\t" + test[2], test[3], actual);
        }
    }

    @Test
    public void TestCustomCurrencySignAndSeparator() {
        DecimalFormatSymbols custom = new DecimalFormatSymbols(ULocale.US);

        custom.setCurrencySymbol("*");
        custom.setMonetaryGroupingSeparator('^');
        custom.setMonetaryDecimalSeparator(':');

        DecimalFormat fmt = new DecimalFormat("\u00A4 #,##0.00", custom);

        final String numstr = "* 1^234:56";
        expect2(fmt, 1234.56, numstr);
    }

    @Test
    public void TestParseSignsAndMarks() {
        class SignsAndMarksItem {
            public String locale;
            public boolean lenient;
            public String numString;
            public double value;
             // Simple constructor
            public SignsAndMarksItem(String loc, boolean lnt, String numStr, double val) {
                locale = loc;
                lenient = lnt;
                numString = numStr;
                value = val;
            }
        };
        final SignsAndMarksItem[] items = {
            // *** Note, ICU4J lenient number parsing does not handle arbitrary whitespace, but can
            // treat some whitespace as a grouping separator. The cases marked *** below depend
            // on isGroupingUsed() being set for the locale, which in turn depends on grouping
            // separators being present in the decimalFormat pattern for the locale (& num sys).
            //
            //                    locale                lenient numString                               value
            new SignsAndMarksItem("en",                 false,  "12",                                    12 ),
            new SignsAndMarksItem("en",                 true,   "12",                                    12 ),
            new SignsAndMarksItem("en",                 false,  "-23",                                  -23 ),
            new SignsAndMarksItem("en",                 true,   "-23",                                  -23 ),
            new SignsAndMarksItem("en",                 true,   "- 23",                                 -23 ), // ***
            new SignsAndMarksItem("en",                 false,  "\u200E-23",                            -23 ),
            new SignsAndMarksItem("en",                 true,   "\u200E-23",                            -23 ),
            new SignsAndMarksItem("en",                 true,   "\u200E- 23",                           -23 ), // ***

            new SignsAndMarksItem("en@numbers=arab",    false,  "\u0663\u0664",                          34 ),
            new SignsAndMarksItem("en@numbers=arab",    true,   "\u0663\u0664",                          34 ),
            new SignsAndMarksItem("en@numbers=arab",    false,  "-\u0664\u0665",                        -45 ),
            new SignsAndMarksItem("en@numbers=arab",    true,   "-\u0664\u0665",                        -45 ),
            new SignsAndMarksItem("en@numbers=arab",    true,   "- \u0664\u0665",                       -45 ), // ***
            new SignsAndMarksItem("en@numbers=arab",    false,  "\u200F-\u0664\u0665",                  -45 ),
            new SignsAndMarksItem("en@numbers=arab",    true,   "\u200F-\u0664\u0665",                  -45 ),
            new SignsAndMarksItem("en@numbers=arab",    true,   "\u200F- \u0664\u0665",                 -45 ), // ***

            new SignsAndMarksItem("en@numbers=arabext", false,  "\u06F5\u06F6",                          56 ),
            new SignsAndMarksItem("en@numbers=arabext", true,   "\u06F5\u06F6",                          56 ),
            new SignsAndMarksItem("en@numbers=arabext", false,  "-\u06F6\u06F7",                        -67 ),
            new SignsAndMarksItem("en@numbers=arabext", true,   "-\u06F6\u06F7",                        -67 ),
            new SignsAndMarksItem("en@numbers=arabext", true,   "- \u06F6\u06F7",                       -67 ), // ***
            new SignsAndMarksItem("en@numbers=arabext", false,  "\u200E-\u200E\u06F6\u06F7",            -67 ),
            new SignsAndMarksItem("en@numbers=arabext", true,   "\u200E-\u200E\u06F6\u06F7",            -67 ),
            new SignsAndMarksItem("en@numbers=arabext", true,   "\u200E-\u200E \u06F6\u06F7",           -67 ), // ***

            new SignsAndMarksItem("he",                 false,  "12",                                    12 ),
            new SignsAndMarksItem("he",                 true,   "12",                                    12 ),
            new SignsAndMarksItem("he",                 false,  "-23",                                  -23 ),
            new SignsAndMarksItem("he",                 true,   "-23",                                  -23 ),
            new SignsAndMarksItem("he",                 true,   "- 23",                                 -23 ), // ***
            new SignsAndMarksItem("he",                 false,  "\u200E-23",                            -23 ),
            new SignsAndMarksItem("he",                 true,   "\u200E-23",                            -23 ),
            new SignsAndMarksItem("he",                 true,   "\u200E- 23",                           -23 ), // ***

            new SignsAndMarksItem("ar",                 false,  "\u0663\u0664",                          34 ),
            new SignsAndMarksItem("ar",                 true,   "\u0663\u0664",                          34 ),
            new SignsAndMarksItem("ar",                 false,  "-\u0664\u0665",                        -45 ),
            new SignsAndMarksItem("ar",                 true,   "-\u0664\u0665",                        -45 ),
            new SignsAndMarksItem("ar",                 true,   "- \u0664\u0665",                       -45 ), // ***
            new SignsAndMarksItem("ar",                 false,  "\u200F-\u0664\u0665",                  -45 ),
            new SignsAndMarksItem("ar",                 true,   "\u200F-\u0664\u0665",                  -45 ),
            new SignsAndMarksItem("ar",                 true,   "\u200F- \u0664\u0665",                 -45 ), // ***

            new SignsAndMarksItem("ar_MA",              false,  "12",                                    12 ),
            new SignsAndMarksItem("ar_MA",              true,   "12",                                    12 ),
            new SignsAndMarksItem("ar_MA",              false,  "-23",                                  -23 ),
            new SignsAndMarksItem("ar_MA",              true,   "-23",                                  -23 ),
            new SignsAndMarksItem("ar_MA",              true,   "- 23",                                 -23 ), // ***
            new SignsAndMarksItem("ar_MA",              false,  "\u200E-23",                            -23 ),
            new SignsAndMarksItem("ar_MA",              true,   "\u200E-23",                            -23 ),
            new SignsAndMarksItem("ar_MA",              true,   "\u200E- 23",                           -23 ), // ***

            new SignsAndMarksItem("fa",                 false,  "\u06F5\u06F6",                          56 ),
            new SignsAndMarksItem("fa",                 true,   "\u06F5\u06F6",                          56 ),
            new SignsAndMarksItem("fa",                 false,  "\u2212\u06F6\u06F7",                   -67 ),
            new SignsAndMarksItem("fa",                 true,   "\u2212\u06F6\u06F7",                   -67 ),
            new SignsAndMarksItem("fa",                 true,   "\u2212 \u06F6\u06F7",                  -67 ), // ***
            new SignsAndMarksItem("fa",                 false,  "\u200E\u2212\u200E\u06F6\u06F7",       -67 ),
            new SignsAndMarksItem("fa",                 true,   "\u200E\u2212\u200E\u06F6\u06F7",       -67 ),
            new SignsAndMarksItem("fa",                 true,   "\u200E\u2212\u200E \u06F6\u06F7",      -67 ), // ***

            new SignsAndMarksItem("ps",                 false,  "\u06F5\u06F6",                          56 ),
            new SignsAndMarksItem("ps",                 true,   "\u06F5\u06F6",                          56 ),
            new SignsAndMarksItem("ps",                 false,  "-\u06F6\u06F7",                        -67 ),
            new SignsAndMarksItem("ps",                 true,   "-\u06F6\u06F7",                        -67 ),
            new SignsAndMarksItem("ps",                 true,   "- \u06F6\u06F7",                       -67 ), // ***
            new SignsAndMarksItem("ps",                 false,  "\u200E-\u200E\u06F6\u06F7",            -67 ),
            new SignsAndMarksItem("ps",                 true,   "\u200E-\u200E\u06F6\u06F7",            -67 ),
            new SignsAndMarksItem("ps",                 true,   "\u200E-\u200E \u06F6\u06F7",           -67 ), // ***
            new SignsAndMarksItem("ps",                 false,  "-\u200E\u06F6\u06F7",                  -67 ),
            new SignsAndMarksItem("ps",                 true,   "-\u200E\u06F6\u06F7",                  -67 ),
            new SignsAndMarksItem("ps",                 true,   "-\u200E \u06F6\u06F7",                 -67 ), // ***
        };
        for (SignsAndMarksItem item: items) {
            ULocale locale = new ULocale(item.locale);
            NumberFormat numfmt = NumberFormat.getInstance(locale);
            if (numfmt != null) {
                numfmt.setParseStrict(!item.lenient);
                ParsePosition ppos = new ParsePosition(0);
                Number num = numfmt.parse(item.numString, ppos);
                if (num != null && ppos.getIndex() == item.numString.length()) {
                    double parsedValue = num.doubleValue();
                    if (parsedValue != item.value) {
                        errln("FAIL: locale " + item.locale + ", lenient " + item.lenient + ", parse of \"" + item.numString + "\" gives value " + parsedValue);
                    }
                } else {
                    errln("FAIL: locale " + item.locale + ", lenient " + item.lenient + ", parse of \"" + item.numString + "\" gives position " + ppos.getIndex());
                }
            } else {
                errln("FAIL: NumberFormat.getInstance for locale " + item.locale);
            }
        }
    }

    @Test
    public void TestContext() {
        // just a minimal sanity check for now
        NumberFormat nfmt = NumberFormat.getInstance();
        DisplayContext context = nfmt.getContext(DisplayContext.Type.CAPITALIZATION);
        if (context != DisplayContext.CAPITALIZATION_NONE) {
            errln("FAIL: Initial NumberFormat.getContext() is not CAPITALIZATION_NONE");
        }
        nfmt.setContext(DisplayContext.CAPITALIZATION_FOR_STANDALONE);
        context = nfmt.getContext(DisplayContext.Type.CAPITALIZATION);
        if (context != DisplayContext.CAPITALIZATION_FOR_STANDALONE) {
            errln("FAIL: NumberFormat.getContext() does not return the value set, CAPITALIZATION_FOR_STANDALONE");
        }
    }

    @Test
    public void TestAccountingCurrency() {
        String[][] tests = {
                //locale              num         curr fmt per loc     curr std fmt         curr acct fmt        rt
                {"en_US",             "1234.5",   "$1,234.50",         "$1,234.50",         "$1,234.50",         "true"},
                {"en_US@cf=account",  "1234.5",   "$1,234.50",         "$1,234.50",         "$1,234.50",         "true"},
                {"en_US",             "-1234.5",  "-$1,234.50",        "-$1,234.50",        "($1,234.50)",       "true"},
                {"en_US@cf=standard", "-1234.5",  "-$1,234.50",        "-$1,234.50",        "($1,234.50)",       "true"},
                {"en_US@cf=account",  "-1234.5",  "($1,234.50)",       "-$1,234.50",        "($1,234.50)",       "true"},
                {"en_US",             "0",        "$0.00",             "$0.00",             "$0.00",             "true"},
                {"en_US",             "-0.2",     "-$0.20",            "-$0.20",            "($0.20)",           "true"},
                {"en_US@cf=standard", "-0.2",     "-$0.20",            "-$0.20",            "($0.20)",           "true"},
                {"en_US@cf=account",  "-0.2",     "($0.20)",           "-$0.20",            "($0.20)",           "true"},
                {"ja_JP",             "10000",    "￥10,000",          "￥10,000",          "￥10,000",          "true" },
                {"ja_JP",             "-1000.5",  "-￥1,000",          "-￥1,000",          "(￥1,000)",         "false"},
                {"ja_JP@cf=account",  "-1000.5",  "(￥1,000)",         "-￥1,000",          "(￥1,000)",         "false"},
                {"de_DE",             "-23456.7", "-23.456,70\u00A0€", "-23.456,70\u00A0€", "-23.456,70\u00A0€", "true" },
        };
        for (String[] data : tests) {
            ULocale loc = new ULocale(data[0]);
            double num = Double.parseDouble(data[1]);
            String fmtPerLocExpected   = data[2];
            String fmtStandardExpected = data[3];
            String fmtAccountExpected  = data[4];
            boolean rt = Boolean.parseBoolean(data[5]);

            NumberFormat fmtPerLoc = NumberFormat.getInstance(loc, NumberFormat.CURRENCYSTYLE);
            expect(fmtPerLoc, num, fmtPerLocExpected, rt);

            NumberFormat fmtStandard = NumberFormat.getInstance(loc, NumberFormat.STANDARDCURRENCYSTYLE);
            expect(fmtStandard, num, fmtStandardExpected, rt);

            NumberFormat fmtAccount = NumberFormat.getInstance(loc, NumberFormat.ACCOUNTINGCURRENCYSTYLE);
            expect(fmtAccount, num, fmtAccountExpected, rt);
        }
    }

    @Test
    public void TestCurrencyUsage() {
        // the 1st one is checking setter/getter, while the 2nd one checks for getInstance
        // compare the Currency and Currency Cash Digits
        // Note that as of CLDR 26:
        // * TWD switches from 0 decimals to 2; PKR still has 0, so change test to that
        // * CAD rounds to .05 in the cash style only.
        for (int i = 0; i < 2; i++) {
            String original_expected = "PKR124";
            DecimalFormat custom = null;
            if (i == 0) {
                custom = (DecimalFormat) DecimalFormat.getInstance(new ULocale("en_US@currency=PKR"),
                        DecimalFormat.CURRENCYSTYLE);

                String original = custom.format(123.567);
                assertEquals("Test Currency Context", original_expected, original);

                // test the getter
                assertEquals("Test Currency Context Purpose", custom.getCurrencyUsage(),
                        Currency.CurrencyUsage.STANDARD);
                custom.setCurrencyUsage(Currency.CurrencyUsage.CASH);
                assertEquals("Test Currency Context Purpose", custom.getCurrencyUsage(), Currency.CurrencyUsage.CASH);
            } else {
                custom = (DecimalFormat) DecimalFormat.getInstance(new ULocale("en_US@currency=PKR"),
                        DecimalFormat.CASHCURRENCYSTYLE);

                // test the getter
                assertEquals("Test Currency Context Purpose", custom.getCurrencyUsage(), Currency.CurrencyUsage.CASH);
            }

            String cash_currency = custom.format(123.567);
            String cash_currency_expected = "PKR124";
            assertEquals("Test Currency Context", cash_currency_expected, cash_currency);
        }

        // the 1st one is checking setter/getter, while the 2nd one checks for getInstance
        // compare the Currency and Currency Cash Rounding
        for (int i = 0; i < 2; i++) {
            String original_rounding_expected = "CA$123.57";
            DecimalFormat fmt = null;
            if (i == 0) {
                fmt = (DecimalFormat) DecimalFormat.getInstance(new ULocale("en_US@currency=CAD"),
                        DecimalFormat.CURRENCYSTYLE);

                String original_rounding = fmt.format(123.566);
                assertEquals("Test Currency Context", original_rounding_expected, original_rounding);

                fmt.setCurrencyUsage(Currency.CurrencyUsage.CASH);
            } else {
                fmt = (DecimalFormat) DecimalFormat.getInstance(new ULocale("en_US@currency=CAD"),
                        DecimalFormat.CASHCURRENCYSTYLE);
            }

            String cash_rounding_currency = fmt.format(123.567);
            String cash__rounding_currency_expected = "CA$123.55";
            assertEquals("Test Currency Context", cash__rounding_currency_expected, cash_rounding_currency);
        }

        // the 1st one is checking setter/getter, while the 2nd one checks for getInstance
        // Test the currency change
        for (int i = 0; i < 2; i++) {
            DecimalFormat fmt2 = null;
            if (i == 1) {
                fmt2 = (DecimalFormat) NumberFormat.getInstance(new ULocale("en_US@currency=JPY"),
                        NumberFormat.CURRENCYSTYLE);
                fmt2.setCurrencyUsage(Currency.CurrencyUsage.CASH);
            } else {
                fmt2 = (DecimalFormat) NumberFormat.getInstance(new ULocale("en_US@currency=JPY"),
                        NumberFormat.CASHCURRENCYSTYLE);
            }

            fmt2.setCurrency(Currency.getInstance("PKR"));
            String PKR_changed = fmt2.format(123.567);
            String PKR_changed_expected = "PKR124";
            assertEquals("Test Currency Context", PKR_changed_expected, PKR_changed);
        }
    }

    @Test
    public void TestParseRequiredDecimalPoint() {

        String[] testPattern = { "00.####", "00.0", "00" };

        String value2Parse = "99";
        double parseValue  =  99;
        DecimalFormat parser = new DecimalFormat();
        double result;
        boolean hasDecimalPoint;
        for (int i = 0; i < testPattern.length; i++) {
            parser.applyPattern(testPattern[i]);
            hasDecimalPoint = testPattern[i].contains(".");

            parser.setDecimalPatternMatchRequired(false);
            try {
                result = parser.parse(value2Parse).doubleValue();
                assertEquals("wrong parsed value", parseValue, result);
            } catch (ParseException e) {
                TestFmwk.errln("Parsing " + value2Parse + " should have succeeded with " + testPattern[i] +
                            " and isDecimalPointMatchRequired set to: " + parser.isDecimalPatternMatchRequired());
            }

            parser.setDecimalPatternMatchRequired(true);
            try {
                result = parser.parse(value2Parse).doubleValue();
                if(hasDecimalPoint){
                    TestFmwk.errln("Parsing " + value2Parse + " should NOT have succeeded with " + testPattern[i] +
                            " and isDecimalPointMatchRequired set to: " + parser.isDecimalPatternMatchRequired());
                }
            } catch (ParseException e) {
                    // OK, should fail
            }
        }

    }


    //TODO(user): investigate
    @Test
    public void TestDataDrivenICU() {
        DataDrivenNumberFormatTestUtility.runSuite(
                "numberformattestspecification.txt", ICU);
    }

    //TODO(user): investigate
    @Test
    public void TestDataDrivenJDK() {
        // Android patch: Android java.text.DecimalFormat is actually ICU.
        if (TestUtil.getJavaVendor() == TestUtil.JavaVendor.Android) return;
        // Android patch end.
        DataDrivenNumberFormatTestUtility.runSuite(
                "numberformattestspecification.txt", JDK);
    }


    @Test
    public void TestCurrFmtNegSameAsPositive() {
        DecimalFormatSymbols decfmtsym = DecimalFormatSymbols.getInstance(Locale.US);
        decfmtsym.setMinusSign('\u200B'); // ZERO WIDTH SPACE, in ICU4J cannot set to empty string
        DecimalFormat decfmt = new DecimalFormat("\u00A4#,##0.00;\u00A4#,##0.00", decfmtsym);
        String currFmtResult = decfmt.format(-100.0);
        if (!currFmtResult.equals("\u200B$100.00")) {
            errln("decfmt.toPattern results wrong, expected \u200B$100.00, got " + currFmtResult);
        }
    }

    @Test
    public void TestNumberFormatTestDataToString() {
        new NumberFormatTestData().toString();
    }

   // Testing for Issue 11805.
    @Test
    public void TestFormatToCharacterIteratorIssue11805 () {
        final double number = -350.76;
        DecimalFormat dfUS = (DecimalFormat) DecimalFormat.getCurrencyInstance(Locale.US);
        String strUS = dfUS.format(number);
        Set<AttributedCharacterIterator.Attribute> resultUS  = dfUS.formatToCharacterIterator(number).getAllAttributeKeys();
        assertEquals("Negative US Results: " + strUS, 5, resultUS.size());

        // For each test, add assert that all the fields are present and in the right spot.
        // TODO: Add tests for identify and position of each field, as in IntlTestDecimalFormatAPIC.

        DecimalFormat dfDE = (DecimalFormat) DecimalFormat.getCurrencyInstance(Locale.GERMANY);
        String strDE = dfDE.format(number);
        Set<AttributedCharacterIterator.Attribute> resultDE  = dfDE.formatToCharacterIterator(number).getAllAttributeKeys();
        assertEquals("Negative DE Results: " + strDE, 5, resultDE.size());

        DecimalFormat dfIN = (DecimalFormat) DecimalFormat.getCurrencyInstance(new Locale("hi", "in"));
        String strIN = dfIN.format(number);
        Set<AttributedCharacterIterator.Attribute> resultIN  = dfIN.formatToCharacterIterator(number).getAllAttributeKeys();
        assertEquals("Negative IN Results: " + strIN, 5, resultIN.size());

        DecimalFormat dfJP = (DecimalFormat) DecimalFormat.getCurrencyInstance(Locale.JAPAN);
        String strJP = dfJP.format(number);
        Set<AttributedCharacterIterator.Attribute> resultJP  = dfJP.formatToCharacterIterator(number).getAllAttributeKeys();
        assertEquals("Negative JA Results: " + strJP, 3, resultJP.size());

        DecimalFormat dfGB = (DecimalFormat) DecimalFormat.getCurrencyInstance(new Locale("en", "gb"));
        String strGB = dfGB.format(number);
        Set<AttributedCharacterIterator.Attribute> resultGB  = dfGB.formatToCharacterIterator(number).getAllAttributeKeys();
        assertEquals("Negative GB Results: " + strGB , 5, resultGB.size());

        DecimalFormat dfPlural = (DecimalFormat) NumberFormat.getInstance(new Locale("en", "gb"),
            NumberFormat.PLURALCURRENCYSTYLE);
        strGB = dfPlural.format(number);
        resultGB = dfPlural.formatToCharacterIterator(number).getAllAttributeKeys();
        assertEquals("Negative GB Results: " + strGB , 5, resultGB.size());

        strGB = dfPlural.format(1);
        resultGB = dfPlural.formatToCharacterIterator(1).getAllAttributeKeys();
        assertEquals("Negative GB Results: " + strGB , 4, resultGB.size());

        // Test output with unit value.
        DecimalFormat auPlural = (DecimalFormat) NumberFormat.getInstance(new Locale("en", "au"),
                NumberFormat.PLURALCURRENCYSTYLE);
        String strAU = auPlural.format(1L);
        Set<AttributedCharacterIterator.Attribute> resultAU  =
                auPlural.formatToCharacterIterator(1L).getAllAttributeKeys();
        assertEquals("Unit AU Result: " + strAU , 4, resultAU.size());

        // Verify Permille fields.
        DecimalFormatSymbols sym = new DecimalFormatSymbols(new Locale("en", "gb"));
        DecimalFormat dfPermille = new DecimalFormat("####0.##\u2030", sym);
        strGB = dfPermille.format(number);
        resultGB = dfPermille.formatToCharacterIterator(number).getAllAttributeKeys();
        assertEquals("Negative GB Permille Results: " + strGB , 3, resultGB.size());
    }

    // Testing for Issue 11808.
    @Test
    public void TestRoundUnnecessarytIssue11808 () {
        DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();
        StringBuffer result = new StringBuffer("");
        df.setRoundingMode(BigDecimal.ROUND_UNNECESSARY);
        df.applyPattern("00.0#E0");

        try {
            df.format(99999.0, result, new FieldPosition(0));
            fail("Missing ArithmeticException for double: " + result);
        } catch (ArithmeticException expected) {
            // The exception should be thrown, since rounding is needed.
        }

        try {
            result = df.format(99999, result, new FieldPosition(0));
            fail("Missing ArithmeticException for int: " + result);
       } catch (ArithmeticException expected) {
           // The exception should be thrown, since rounding is needed.
        }

        try {
            result = df.format(new BigInteger("999999"), result, new FieldPosition(0));
            fail("Missing ArithmeticException for BigInteger: " + result);
        } catch (ArithmeticException expected) {
            // The exception should be thrown, since rounding is needed.
        }

        try {
            result = df.format(new BigDecimal("99999"), result, new FieldPosition(0));
            fail("Missing ArithmeticException for BigDecimal: " + result);
        } catch (ArithmeticException expected) {
            // The exception should be thrown, since rounding is needed.
        }

        try {
            result = df.format(new BigDecimal("-99999"), result, new FieldPosition(0));
            fail("Missing ArithmeticException for BigDecimal: " + result);
        } catch (ArithmeticException expected) {
            // The exception should be thrown, since rounding is needed.
        }
    }

    // Testing for Issue 11735.
    @Test
    public void TestNPEIssue11735() {
        DecimalFormat fmt = new DecimalFormat("0", new DecimalFormatSymbols(new ULocale("en")));
        ParsePosition ppos = new ParsePosition(0);
        assertEquals("Currency symbol missing in parse. Expect null result.",
                fmt.parseCurrency("53.45", ppos), null);
    }

    private void CompareAttributedCharacterFormatOutput(AttributedCharacterIterator iterator,
        List<FieldContainer> expected, String formattedOutput) {

        List<FieldContainer> result = new ArrayList<FieldContainer>();
        while (iterator.getIndex() != iterator.getEndIndex()) {
            int start = iterator.getRunStart();
            int end = iterator.getRunLimit();
            Iterator it = iterator.getAttributes().keySet().iterator();
            AttributedCharacterIterator.Attribute attribute = (AttributedCharacterIterator.Attribute) it.next();
            Object value = iterator.getAttribute(attribute);
            result.add(new FieldContainer(start, end, attribute, value));
            iterator.setIndex(end);
        }
        assertEquals("Comparing vector length for " + formattedOutput,
            expected.size(), result.size());

        if (!expected.containsAll(result)) {
          // Print information on the differences.
          for (int i = 0; i < expected.size(); i++) {
            System.out.println("     expected[" + i + "] =" +
                expected.get(i).start + " " +
                expected.get(i).end + " " +
                expected.get(i).attribute + " " +
                expected.get(i).value);
            System.out.println(" result[" + i + "] =" +
                result.get(i).start + " " +
                result.get(i).end + " " +
                result.get(i).attribute + " " +
                result.get(i).value);
          }
        }
        // TODO: restore when #11914 is fixed.
        // assertTrue("Comparing vector results for " + formattedOutput,
        //    expected.containsAll(result));
    }

    // Testing for Issue 11914, missing FieldPositions for some field types.
    @Test
    public void TestNPEIssue11914() {
        // First test: Double value with grouping separators.
        List<FieldContainer> v1 = new ArrayList<FieldContainer>(7);
        v1.add(new FieldContainer(0, 3, NumberFormat.Field.INTEGER));
        v1.add(new FieldContainer(3, 4, NumberFormat.Field.GROUPING_SEPARATOR));
        v1.add(new FieldContainer(4, 7, NumberFormat.Field.INTEGER));
        v1.add(new FieldContainer(7, 8, NumberFormat.Field.GROUPING_SEPARATOR));
        v1.add(new FieldContainer(8, 11, NumberFormat.Field.INTEGER));
        v1.add(new FieldContainer(11, 12, NumberFormat.Field.DECIMAL_SEPARATOR));
        v1.add(new FieldContainer(12, 15, NumberFormat.Field.FRACTION));

        Number number = new Double(123456789.9753);
        ULocale usLoc = new ULocale("en-US");
        DecimalFormatSymbols US = new DecimalFormatSymbols(usLoc);

        NumberFormat outFmt = NumberFormat.getNumberInstance(usLoc);
        String numFmtted = outFmt.format(number);
        AttributedCharacterIterator iterator =
                outFmt.formatToCharacterIterator(number);
        CompareAttributedCharacterFormatOutput(iterator, v1, numFmtted);

        // Second test: Double with scientific notation formatting.
        List<FieldContainer> v2 = new ArrayList<FieldContainer>(7);
        v2.add(new FieldContainer(0, 1, NumberFormat.Field.INTEGER));
        v2.add(new FieldContainer(1, 2, NumberFormat.Field.DECIMAL_SEPARATOR));
        v2.add(new FieldContainer(2, 5, NumberFormat.Field.FRACTION));
        v2.add(new FieldContainer(5, 6, NumberFormat.Field.EXPONENT_SYMBOL));
        v2.add(new FieldContainer(6, 7, NumberFormat.Field.EXPONENT_SIGN));
        v2.add(new FieldContainer(7, 8, NumberFormat.Field.EXPONENT));
        DecimalFormat fmt2 = new DecimalFormat("0.###E+0", US);

        numFmtted = fmt2.format(number);
        iterator = fmt2.formatToCharacterIterator(number);
        CompareAttributedCharacterFormatOutput(iterator, v2, numFmtted);

        // Third test. BigInteger with grouping separators.
        List<FieldContainer> v3 = new ArrayList<FieldContainer>(7);
        v3.add(new FieldContainer(0, 1, NumberFormat.Field.SIGN));
        v3.add(new FieldContainer(1, 2, NumberFormat.Field.INTEGER));
        v3.add(new FieldContainer(2, 3, NumberFormat.Field.GROUPING_SEPARATOR));
        v3.add(new FieldContainer(3, 6, NumberFormat.Field.INTEGER));
        v3.add(new FieldContainer(6, 7, NumberFormat.Field.GROUPING_SEPARATOR));
        v3.add(new FieldContainer(7, 10, NumberFormat.Field.INTEGER));
        v3.add(new FieldContainer(10, 11, NumberFormat.Field.GROUPING_SEPARATOR));
        v3.add(new FieldContainer(11, 14, NumberFormat.Field.INTEGER));
        v3.add(new FieldContainer(14, 15, NumberFormat.Field.GROUPING_SEPARATOR));
        v3.add(new FieldContainer(15, 18, NumberFormat.Field.INTEGER));
        v3.add(new FieldContainer(18, 19, NumberFormat.Field.GROUPING_SEPARATOR));
        v3.add(new FieldContainer(19, 22, NumberFormat.Field.INTEGER));
        v3.add(new FieldContainer(22, 23, NumberFormat.Field.GROUPING_SEPARATOR));
        v3.add(new FieldContainer(23, 26, NumberFormat.Field.INTEGER));
        BigInteger bigNumberInt = new BigInteger("-1234567890246813579");
        String fmtNumberBigInt = outFmt.format(bigNumberInt);

        iterator = outFmt.formatToCharacterIterator(bigNumberInt);
        CompareAttributedCharacterFormatOutput(iterator, v3, fmtNumberBigInt);

        // Fourth test: BigDecimal with exponential formatting.
        List<FieldContainer> v4 = new ArrayList<FieldContainer>(7);
        v4.add(new FieldContainer(0, 1, NumberFormat.Field.SIGN));
        v4.add(new FieldContainer(1, 2, NumberFormat.Field.INTEGER));
        v4.add(new FieldContainer(2, 3, NumberFormat.Field.DECIMAL_SEPARATOR));
        v4.add(new FieldContainer(3, 6, NumberFormat.Field.FRACTION));
        v4.add(new FieldContainer(6, 7, NumberFormat.Field.EXPONENT_SYMBOL));
        v4.add(new FieldContainer(7, 8, NumberFormat.Field.EXPONENT_SIGN));
        v4.add(new FieldContainer(8, 9, NumberFormat.Field.EXPONENT));

        java.math.BigDecimal numberBigD = new java.math.BigDecimal(-123456789);
        String fmtNumberBigDExp = fmt2.format(numberBigD);

        iterator = fmt2.formatToCharacterIterator(numberBigD);
        CompareAttributedCharacterFormatOutput(iterator, v4, fmtNumberBigDExp);

    }

    // Test that the decimal is shown even when there are no fractional digits
    @Test
    public void Test11621() throws Exception {
        String pat = "0.##E0";

        DecimalFormatSymbols icuSym = new DecimalFormatSymbols(Locale.US);
        DecimalFormat icuFmt = new DecimalFormat(pat, icuSym);
        icuFmt.setDecimalSeparatorAlwaysShown(true);
        String icu = ((NumberFormat)icuFmt).format(299792458);

        java.text.DecimalFormatSymbols jdkSym = new java.text.DecimalFormatSymbols(Locale.US);
        java.text.DecimalFormat jdkFmt = new java.text.DecimalFormat(pat,jdkSym);
        jdkFmt.setDecimalSeparatorAlwaysShown(true);
        String jdk = ((java.text.NumberFormat)jdkFmt).format(299792458);

        assertEquals("ICU and JDK placement of decimal in exponent", jdk, icu);
    }

    private void checkFormatWithField(String testInfo, Format format, Object object,
            String expected, Format.Field field, int begin, int end) {
        StringBuffer buffer = new StringBuffer();
        FieldPosition pos = new FieldPosition(field);
        format.format(object, buffer, pos);

        assertEquals("Test " + testInfo + ": incorrect formatted text", expected, buffer.toString());

        if (begin != pos.getBeginIndex() || end != pos.getEndIndex()) {
            assertEquals("Index mismatch", field + " " + begin + ".." + end,
                pos.getFieldAttribute() + " " + pos.getBeginIndex() + ".." + pos.getEndIndex());
        }
    }

    @Test
    public void TestMissingFieldPositionsCurrency() {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getCurrencyInstance(ULocale.US);
        Number number = new Double(92314587.66);
        String result = "$92,314,587.66";

        checkFormatWithField("currency", formatter, number, result,
            NumberFormat.Field.CURRENCY, 0, 1);
        checkFormatWithField("integer", formatter, number, result,
            NumberFormat.Field.INTEGER, 1, 11);
        checkFormatWithField("grouping separator", formatter, number, result,
            NumberFormat.Field.GROUPING_SEPARATOR, 3, 4);
        checkFormatWithField("decimal separator", formatter, number, result,
            NumberFormat.Field.DECIMAL_SEPARATOR, 11, 12);
        checkFormatWithField("fraction", formatter, number, result,
            NumberFormat.Field.FRACTION, 12, 14);
    }

    @Test
    public void TestMissingFieldPositionsNegativeDouble() {
        // test for exponential fields with double
        DecimalFormatSymbols us_symbols = new DecimalFormatSymbols(ULocale.US);
        Number number = new Double(-12345678.90123);
        DecimalFormat formatter = new DecimalFormat("0.#####E+00", us_symbols);
        String numFmtted = formatter.format(number);

        checkFormatWithField("sign", formatter, number, numFmtted,
            NumberFormat.Field.SIGN, 0, 1);
        checkFormatWithField("integer", formatter, number, numFmtted,
            NumberFormat.Field.INTEGER, 1, 2);
        checkFormatWithField("decimal separator", formatter, number, numFmtted,
            NumberFormat.Field.DECIMAL_SEPARATOR, 2, 3);
        checkFormatWithField("exponent symbol", formatter, number, numFmtted,
            NumberFormat.Field.EXPONENT_SYMBOL, 8, 9);
        checkFormatWithField("exponent sign", formatter, number, numFmtted,
            NumberFormat.Field.EXPONENT_SIGN, 9, 10);
        checkFormatWithField("exponent", formatter, number, numFmtted,
            NumberFormat.Field.EXPONENT, 10, 12);
    }

    @Test
    public void TestMissingFieldPositionsPerCent() {
        // Check PERCENT
        DecimalFormat percentFormat = (DecimalFormat) NumberFormat.getPercentInstance(ULocale.US);
        Number number = new Double(-0.986);
        String numberFormatted = percentFormat.format(number);
        checkFormatWithField("sign", percentFormat, number, numberFormatted,
            NumberFormat.Field.SIGN, 0, 1);
        checkFormatWithField("integer", percentFormat, number, numberFormatted,
            NumberFormat.Field.INTEGER, 1, 3);
        checkFormatWithField("percent", percentFormat, number, numberFormatted,
            NumberFormat.Field.PERCENT, 3, 4);
    }

    @Test
    public void TestMissingFieldPositionsPerCentPattern() {
        // Check PERCENT with more digits
        DecimalFormatSymbols us_symbols = new DecimalFormatSymbols(ULocale.US);
        DecimalFormat fmtPercent = new DecimalFormat("0.#####%", us_symbols);
        Number number = new Double(-0.986);
        String numFmtted = fmtPercent.format(number);

        checkFormatWithField("sign", fmtPercent, number, numFmtted,
            NumberFormat.Field.SIGN, 0, 1);
        checkFormatWithField("integer", fmtPercent, number, numFmtted,
            NumberFormat.Field.INTEGER, 1, 3);
        checkFormatWithField("decimal separator", fmtPercent, number, numFmtted,
            NumberFormat.Field.DECIMAL_SEPARATOR, 3, 4);
        checkFormatWithField("fraction", fmtPercent, number, numFmtted,
            NumberFormat.Field.FRACTION, 4, 5);
        checkFormatWithField("percent", fmtPercent, number, numFmtted,
            NumberFormat.Field.PERCENT, 5, 6);
    }

    @Test
    public void TestMissingFieldPositionsPerMille() {
        // Check PERMILLE
        DecimalFormatSymbols us_symbols = new DecimalFormatSymbols(ULocale.US);
        DecimalFormat fmtPerMille = new DecimalFormat("0.######‰", us_symbols);
        Number numberPermille = new Double(-0.98654);
        String numFmtted = fmtPerMille.format(numberPermille);

        checkFormatWithField("sign", fmtPerMille, numberPermille, numFmtted,
            NumberFormat.Field.SIGN, 0, 1);
        checkFormatWithField("integer", fmtPerMille, numberPermille, numFmtted,
            NumberFormat.Field.INTEGER, 1, 4);
        checkFormatWithField("decimal separator", fmtPerMille, numberPermille, numFmtted,
            NumberFormat.Field.DECIMAL_SEPARATOR, 4, 5);
        checkFormatWithField("fraction", fmtPerMille, numberPermille, numFmtted,
            NumberFormat.Field.FRACTION, 5, 7);
        checkFormatWithField("permille", fmtPerMille, numberPermille, numFmtted,
            NumberFormat.Field.PERMILLE, 7, 8);
    }

    @Test
    public void TestMissingFieldPositionsNegativeBigInt() {
      DecimalFormatSymbols us_symbols = new DecimalFormatSymbols(ULocale.US);
        DecimalFormat formatter = new DecimalFormat("0.#####E+0", us_symbols);
        Number number = new BigDecimal("-123456789987654321");
        String bigDecFmtted = formatter.format(number);

        checkFormatWithField("sign", formatter, number, bigDecFmtted,
            NumberFormat.Field.SIGN, 0, 1);
        checkFormatWithField("integer", formatter, number, bigDecFmtted,
            NumberFormat.Field.INTEGER, 1, 2);
        checkFormatWithField("decimal separator", formatter, number, bigDecFmtted,
            NumberFormat.Field.DECIMAL_SEPARATOR, 2, 3);
        checkFormatWithField("exponent symbol", formatter, number, bigDecFmtted,
            NumberFormat.Field.EXPONENT_SYMBOL, 8, 9);
        checkFormatWithField("exponent sign", formatter, number, bigDecFmtted,
            NumberFormat.Field.EXPONENT_SIGN, 9, 10);
        checkFormatWithField("exponent", formatter, number, bigDecFmtted,
            NumberFormat.Field.EXPONENT, 10, 12);
    }

    @Test
    public void TestMissingFieldPositionsNegativeLong() {
        Number number = new Long("-123456789987654321");
        DecimalFormatSymbols us_symbols = new DecimalFormatSymbols(ULocale.US);
        DecimalFormat formatter = new DecimalFormat("0.#####E+0", us_symbols);
        String longFmtted = formatter.format(number);

        checkFormatWithField("sign", formatter, number, longFmtted,
            NumberFormat.Field.SIGN, 0, 1);
        checkFormatWithField("integer", formatter, number, longFmtted,
            NumberFormat.Field.INTEGER, 1, 2);
        checkFormatWithField("decimal separator", formatter, number, longFmtted,
            NumberFormat.Field.DECIMAL_SEPARATOR, 2, 3);
        checkFormatWithField("exponent symbol", formatter, number, longFmtted,
            NumberFormat.Field.EXPONENT_SYMBOL, 8, 9);
        checkFormatWithField("exponent sign", formatter, number, longFmtted,
            NumberFormat.Field.EXPONENT_SIGN, 9, 10);
        checkFormatWithField("exponent", formatter, number, longFmtted,
            NumberFormat.Field.EXPONENT, 10, 12);
    }

    @Test
    public void TestMissingFieldPositionsPositiveBigDec() {
        // Check complex positive;negative pattern.
        DecimalFormatSymbols us_symbols = new DecimalFormatSymbols(ULocale.US);
        DecimalFormat fmtPosNegSign = new DecimalFormat("+0.####E+00;-0.#######E+0", us_symbols);
        Number positiveExp = new Double("9876543210");
        String posExpFormatted = fmtPosNegSign.format(positiveExp);

        checkFormatWithField("sign", fmtPosNegSign, positiveExp, posExpFormatted,
            NumberFormat.Field.SIGN, 0, 1);
        checkFormatWithField("integer", fmtPosNegSign, positiveExp, posExpFormatted,
            NumberFormat.Field.INTEGER, 1, 2);
        checkFormatWithField("decimal separator", fmtPosNegSign, positiveExp, posExpFormatted,
            NumberFormat.Field.DECIMAL_SEPARATOR, 2, 3);
        checkFormatWithField("fraction", fmtPosNegSign, positiveExp, posExpFormatted,
            NumberFormat.Field.FRACTION, 3, 7);
        checkFormatWithField("exponent symbol", fmtPosNegSign, positiveExp, posExpFormatted,
            NumberFormat.Field.EXPONENT_SYMBOL, 7, 8);
        checkFormatWithField("exponent sign", fmtPosNegSign, positiveExp, posExpFormatted,
            NumberFormat.Field.EXPONENT_SIGN, 8, 9);
        checkFormatWithField("exponent", fmtPosNegSign, positiveExp, posExpFormatted,
            NumberFormat.Field.EXPONENT, 9, 11);
    }

    @Test
    public void TestMissingFieldPositionsNegativeBigDec() {
        // Check complex positive;negative pattern.
      DecimalFormatSymbols us_symbols = new DecimalFormatSymbols(ULocale.US);
        DecimalFormat fmtPosNegSign = new DecimalFormat("+0.####E+00;-0.#######E+0", us_symbols);
        Number negativeExp = new BigDecimal("-0.000000987654321083");
        String negExpFormatted = fmtPosNegSign.format(negativeExp);

        checkFormatWithField("sign", fmtPosNegSign, negativeExp, negExpFormatted,
            NumberFormat.Field.SIGN, 0, 1);
        checkFormatWithField("integer", fmtPosNegSign, negativeExp, negExpFormatted,
            NumberFormat.Field.INTEGER, 1, 2);
        checkFormatWithField("decimal separator", fmtPosNegSign, negativeExp, negExpFormatted,
            NumberFormat.Field.DECIMAL_SEPARATOR, 2, 3);
        checkFormatWithField("fraction", fmtPosNegSign, negativeExp, negExpFormatted,
            NumberFormat.Field.FRACTION, 3, 7);
        checkFormatWithField("exponent symbol", fmtPosNegSign, negativeExp, negExpFormatted,
            NumberFormat.Field.EXPONENT_SYMBOL, 7, 8);
        checkFormatWithField("exponent sign", fmtPosNegSign, negativeExp, negExpFormatted,
            NumberFormat.Field.EXPONENT_SIGN, 8, 9);
        checkFormatWithField("exponent", fmtPosNegSign, negativeExp, negExpFormatted,
            NumberFormat.Field.EXPONENT, 9, 11);
    }

    @Test
    public void TestStringSymbols() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(ULocale.US);

        String[] customDigits = {"(0)", "(1)", "(2)", "(3)", "(4)", "(5)", "(6)", "(7)", "(8)", "(9)"};
        symbols.setDigitStrings(customDigits);
        symbols.setDecimalSeparatorString("~~");
        symbols.setGroupingSeparatorString("^^");

        DecimalFormat fmt = new DecimalFormat("#,##0.0#", symbols);

        expect2(fmt, 1234567.89, "(1)^^(2)(3)(4)^^(5)(6)(7)~~(8)(9)");
    }
}
