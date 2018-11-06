/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*****************************************************************************************
 *
 *   Copyright (C) 1996-2016, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 **/

/**
 * Port From:   JDK 1.4b1 : java.text.Format.IntlTestDecimalFormatSymbols
 * Source File: java/text/format/IntlTestDecimalFormatSymbols.java
 **/

/*
    @test 1.4 98/03/06
    @summary test International Decimal Format Symbols
*/


package android.icu.dev.test.format;

import java.util.Arrays;
import java.util.Locale;

import org.junit.Test;

import android.icu.text.DecimalFormatSymbols;
import android.icu.util.Currency;
import android.icu.util.ULocale;

public class IntlTestDecimalFormatSymbols extends android.icu.dev.test.TestFmwk
{
    // Test the API of DecimalFormatSymbols; primarily a simple get/set set.
    @Test
    public void TestSymbols()
    {
        DecimalFormatSymbols fr = new DecimalFormatSymbols(Locale.FRENCH);

        DecimalFormatSymbols en = new DecimalFormatSymbols(Locale.ENGLISH);

        if(en.equals(fr)) {
            errln("ERROR: English DecimalFormatSymbols equal to French");
        }

        // just do some VERY basic tests to make sure that get/set work

        if(!en.getLocale().equals(Locale.ENGLISH)) {
            errln("ERROR: getLocale failed");
        }
        if(!en.getULocale().equals(ULocale.ENGLISH)) {
            errln("ERROR: getULocale failed");
        }

        if(!en.getLocale().equals(Locale.ENGLISH)) {
            errln("ERROR: getLocale failed");
        }
        if(!en.getULocale().equals(ULocale.ENGLISH)) {
            errln("ERROR: getULocale failed");
        }

        char zero = en.getZeroDigit();
        fr.setZeroDigit(zero);
        if(fr.getZeroDigit() != en.getZeroDigit()) {
            errln("ERROR: get/set ZeroDigit failed");
        }

        String[] digits = en.getDigitStrings();
        fr.setDigitStrings(digits);
        if (!Arrays.equals(fr.getDigitStrings(), en.getDigitStrings())) {
            errln("ERROR: get/set DigitStrings failed");
        }

        char sigDigit = en.getSignificantDigit();
        fr.setSignificantDigit(sigDigit);
        if(fr.getSignificantDigit() != en.getSignificantDigit()) {
            errln("ERROR: get/set SignificantDigit failed");
        }

        Currency currency = Currency.getInstance("USD");
        fr.setCurrency(currency);
        if (!fr.getCurrency().equals(currency)){
            errln("ERROR: get/set Currency failed");
        }

        char group = en.getGroupingSeparator();
        fr.setGroupingSeparator(group);
        if(fr.getGroupingSeparator() != en.getGroupingSeparator()) {
            errln("ERROR: get/set GroupingSeparator failed");
        }

        String groupStr = en.getGroupingSeparatorString();
        fr.setGroupingSeparatorString(groupStr);
        if (!fr.getGroupingSeparatorString().equals(en.getGroupingSeparatorString())) {
            errln("ERROR: get/set GroupingSeparatorString failed");
        }

        char decimal = en.getDecimalSeparator();
        fr.setDecimalSeparator(decimal);
        if(fr.getDecimalSeparator() != en.getDecimalSeparator()) {
            errln("ERROR: get/set DecimalSeparator failed");
        }

        String decimalStr = en.getDecimalSeparatorString();
        fr.setDecimalSeparatorString(decimalStr);
        if (!fr.getDecimalSeparatorString().equals(en.getDecimalSeparatorString())) {
            errln("ERROR: get/set DecimalSeparatorString failed");
        }

        char monetaryGroup = en.getMonetaryGroupingSeparator();
        fr.setMonetaryGroupingSeparator(monetaryGroup);
        if(fr.getMonetaryGroupingSeparator() != en.getMonetaryGroupingSeparator()) {
            errln("ERROR: get/set MonetaryGroupingSeparator failed");
        }

        String monetaryGroupStr = en.getMonetaryGroupingSeparatorString();
        fr.setMonetaryGroupingSeparatorString(monetaryGroupStr);
        if (!fr.getMonetaryGroupingSeparatorString().equals(en.getMonetaryGroupingSeparatorString())){
            errln("ERROR: get/set MonetaryGroupingSeparatorString failed");
        }

        char monetaryDecimal = en.getMonetaryDecimalSeparator();
        fr.setMonetaryDecimalSeparator(monetaryDecimal);
        if(fr.getMonetaryDecimalSeparator() != en.getMonetaryDecimalSeparator()) {
            errln("ERROR: get/set MonetaryDecimalSeparator failed");
        }

        String monetaryDecimalStr = en.getMonetaryDecimalSeparatorString();
        fr.setMonetaryDecimalSeparatorString(monetaryDecimalStr);
        if (!fr.getMonetaryDecimalSeparatorString().equals(en.getMonetaryDecimalSeparatorString())) {
            errln("ERROR: get/set MonetaryDecimalSeparatorString failed");
        }

        char perMill = en.getPerMill();
        fr.setPerMill(perMill);
        if(fr.getPerMill() != en.getPerMill()) {
            errln("ERROR: get/set PerMill failed");
        }

        String perMillStr = en.getPerMillString();
        fr.setPerMillString(perMillStr);
        if (!fr.getPerMillString().equals(en.getPerMillString())) {
            errln("ERROR: get/set PerMillString failed");
        }

        char percent = en.getPercent();
        fr.setPercent(percent);
        if(fr.getPercent() != en.getPercent()) {
            errln("ERROR: get/set Percent failed");
        }

        String percentStr = en.getPercentString();
        fr.setPercentString(percentStr);
        if (!fr.getPercentString().equals(en.getPercentString())) {
            errln("ERROR: get/set PercentString failed");
        }

        char digit = en.getDigit();
        fr.setDigit(digit);
        if(fr.getDigit() != en.getDigit()) {
            errln("ERROR: get/set Digit failed");
        }

        char patternSeparator = en.getPatternSeparator();
        fr.setPatternSeparator(patternSeparator);
        if(fr.getPatternSeparator() != en.getPatternSeparator()) {
            errln("ERROR: get/set PatternSeparator failed");
        }

        String infinity = en.getInfinity();
        fr.setInfinity(infinity);
        String infinity2 = fr.getInfinity();
        if(! infinity.equals(infinity2)) {
            errln("ERROR: get/set Infinity failed");
        }

        String nan = en.getNaN();
        fr.setNaN(nan);
        String nan2 = fr.getNaN();
        if(! nan.equals(nan2)) {
            errln("ERROR: get/set NaN failed");
        }

        char minusSign = en.getMinusSign();
        fr.setMinusSign(minusSign);
        if(fr.getMinusSign() != en.getMinusSign()) {
            errln("ERROR: get/set MinusSign failed");
        }

        String minusSignStr = en.getMinusSignString();
        fr.setMinusSignString(minusSignStr);
        if (!fr.getMinusSignString().equals(en.getMinusSignString())) {
            errln("ERROR: get/set MinusSignString failed");
        }

        char plusSign = en.getPlusSign();
        fr.setPlusSign(plusSign);
        if(fr.getPlusSign() != en.getPlusSign()) {
            errln("ERROR: get/set PlusSign failed");
        }

        String plusSignStr = en.getPlusSignString();
        fr.setPlusSignString(plusSignStr);
        if (!fr.getPlusSignString().equals(en.getPlusSignString())) {
            errln("ERROR: get/set PlusSignString failed");
        }

        char padEscape = en.getPadEscape();
        fr.setPadEscape(padEscape);
        if(fr.getPadEscape() != en.getPadEscape()) {
            errln("ERROR: get/set PadEscape failed");
        }

        String exponential = en.getExponentSeparator();
        fr.setExponentSeparator(exponential);
        if(fr.getExponentSeparator() != en.getExponentSeparator()) {
            errln("ERROR: get/set Exponential failed");
        }

        String exponentMultiplicationSign = en.getExponentMultiplicationSign();
        fr.setExponentMultiplicationSign(exponentMultiplicationSign);
        if(fr.getExponentMultiplicationSign() != en.getExponentMultiplicationSign()) {
            errln("ERROR: get/set ExponentMultiplicationSign failed");
        }

        // Test CurrencySpacing.
        // In CLDR 1.7, only root.txt has CurrencySpacing data. This data might
        // be different between en and fr in the future.
        for (int i = DecimalFormatSymbols.CURRENCY_SPC_CURRENCY_MATCH; i <= DecimalFormatSymbols.CURRENCY_SPC_INSERT; i++) {
            if (en.getPatternForCurrencySpacing(i, true) !=
                fr.getPatternForCurrencySpacing(i, true)) {
                errln("ERROR: get currency spacing item:"+ i+" before the currency");
                if (en.getPatternForCurrencySpacing(i, false) !=
                    fr.getPatternForCurrencySpacing(i, false)) {
                    errln("ERROR: get currency spacing item:" + i + " after currency");
                }
            }
        }

        String dash = "-";
        en.setPatternForCurrencySpacing(DecimalFormatSymbols.CURRENCY_SPC_INSERT, true, dash);
        if (dash != en.getPatternForCurrencySpacing(DecimalFormatSymbols.CURRENCY_SPC_INSERT, true)) {
            errln("ERROR: set currency spacing pattern for before currency.");
        }

        //DecimalFormatSymbols foo = new DecimalFormatSymbols(); //The variable is never used

        en = (DecimalFormatSymbols) fr.clone();

        if(! en.equals(fr)) {
            errln("ERROR: Clone failed");
        }
    }

    @Test
    public void testCoverage() {
        DecimalFormatSymbols df = new DecimalFormatSymbols();
        DecimalFormatSymbols df2 = (DecimalFormatSymbols)df.clone();
        if (!df.equals(df2) || df.hashCode() != df2.hashCode()) {
            errln("decimal format symbols clone, equals, or hashCode failed");
        }
    }

    @Test
    public void testDigitSymbols() {
        final char defZero = '0';
        final char[] defDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        final String[] defDigitStrings = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        final String[] osmanyaDigitStrings = {
            "\uD801\uDCA0", "\uD801\uDCA1", "\uD801\uDCA2", "\uD801\uDCA3", "\uD801\uDCA4",
            "\uD801\uDCA5", "\uD801\uDCA6", "\uD801\uDCA7", "\uD801\uDCA8", "\uD801\uDCA9"
        };

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);

        symbols.setDigitStrings(osmanyaDigitStrings);
        if (!Arrays.equals(symbols.getDigitStrings(), osmanyaDigitStrings)) {
            errln("ERROR: Osmanya digits (supplementary) should be set");
        }
        if (defZero != symbols.getZeroDigit()) {
            errln("ERROR: Zero digit should be 0");
        }
        if (!Arrays.equals(symbols.getDigits(), defDigits)) {
            errln("ERROR: Char digits should be Latin digits");
        }

        // Reset digits to Latin
        symbols.setZeroDigit(defZero);
        if (!Arrays.equals(symbols.getDigitStrings(), defDigitStrings)) {
            errln("ERROR: Latin digits should be set" + symbols.getDigitStrings()[0]);
        }
    }
}
