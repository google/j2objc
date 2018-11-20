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
 * Port From:   ICU4C v1.8.1 : format : IntlTestDecimalFormatAPI
 * Source File: $ICU4CRoot/source/test/intltest/dcfmapts.cpp
 **/

package android.icu.dev.test.format;

import java.text.AttributedCharacterIterator;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

import android.icu.text.CurrencyPluralInfo;
import android.icu.text.DecimalFormat;
import android.icu.text.DecimalFormatSymbols;
import android.icu.text.NumberFormat;
import android.icu.util.ULocale;

// This is an API test, not a unit test.  It doesn't test very many cases, and doesn't
// try to test the full functionality.  It just calls each function in the class and
// verifies that it works on a basic level.
public class IntlTestDecimalFormatAPIC extends android.icu.dev.test.TestFmwk {

    // This test checks various generic API methods in DecimalFormat to achieve 100% API coverage.
    @Test
    public void TestAPI() {

        logln("DecimalFormat API test---");
        logln("");
        Locale.setDefault(Locale.ENGLISH);

        // ======= Test constructors

        logln("Testing DecimalFormat constructors");

        DecimalFormat def = new DecimalFormat();

        final String pattern = new String("#,##0.# FF");
        final DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRENCH);
        final CurrencyPluralInfo infoInput = new CurrencyPluralInfo(ULocale.FRENCH);

        DecimalFormat pat = null;
        try {
            pat = new DecimalFormat(pattern);
        } catch (IllegalArgumentException e) {
            errln("ERROR: Could not create DecimalFormat (pattern)");
        }

        DecimalFormat cust1 = null;
        try {
            cust1 = new DecimalFormat(pattern, symbols);
        } catch (IllegalArgumentException e) {
            errln("ERROR: Could not create DecimalFormat (pattern, symbols)");
        }

        @SuppressWarnings("unused")
        DecimalFormat cust2 = null;
        try {
            cust2 = new DecimalFormat(pattern, symbols, infoInput, NumberFormat.PLURALCURRENCYSTYLE);
        } catch (IllegalArgumentException e) {
            errln("ERROR: Could not create DecimalFormat (pattern, symbols, infoInput, style)");
        }


        // ======= Test clone(), assignment, and equality

        logln("Testing clone() and equality operators");

        Format clone = (Format) def.clone();
        if (!def.equals(clone)) {
            errln("ERROR: Clone() failed");
        }

        // ======= Test various format() methods

        logln("Testing various format() methods");

        //        final double d = -10456.0037; // this appears as -10456.003700000001 on NT
        //        final double d = -1.04560037e-4; // this appears as -1.0456003700000002E-4 on NT
        final double d = -10456.00370000000000; // this works!
        final long l = 100000000;
        logln("" + Double.toString(d) + " is the double value");

        StringBuffer res1 = new StringBuffer();
        StringBuffer res2 = new StringBuffer();
        StringBuffer res3 = new StringBuffer();
        StringBuffer res4 = new StringBuffer();
        FieldPosition pos1 = new FieldPosition(0);
        FieldPosition pos2 = new FieldPosition(0);
        FieldPosition pos3 = new FieldPosition(0);
        FieldPosition pos4 = new FieldPosition(0);

        res1 = def.format(d, res1, pos1);
        logln("" + Double.toString(d) + " formatted to " + res1);

        res2 = pat.format(l, res2, pos2);
        logln("" + l + " formatted to " + res2);

        res3 = cust1.format(d, res3, pos3);
        logln("" + Double.toString(d) + " formatted to " + res3);

        res4 = cust1.format(l, res4, pos4);
        logln("" + l + " formatted to " + res4);

        // ======= Test parse()

        logln("Testing parse()");

        String text = new String("-10,456.0037");
        ParsePosition pos = new ParsePosition(0);
        String patt = new String("#,##0.#");
        pat.applyPattern(patt);
        double d2 = pat.parse(text, pos).doubleValue();
        if (d2 != d) {
            errln(
                "ERROR: Roundtrip failed (via parse(" + Double.toString(d2) + " != " + Double.toString(d) + ")) for " + text);
        }
        logln(text + " parsed into " + (long) d2);

        // ======= Test getters and setters

        logln("Testing getters and setters");

        final DecimalFormatSymbols syms = pat.getDecimalFormatSymbols();
        def.setDecimalFormatSymbols(syms);
        if (!pat.getDecimalFormatSymbols().equals(def.getDecimalFormatSymbols())) {
            errln("ERROR: set DecimalFormatSymbols() failed");
        }

        String posPrefix;
        pat.setPositivePrefix("+");
        posPrefix = pat.getPositivePrefix();
        logln("Positive prefix (should be +): " + posPrefix);
        if (posPrefix != "+") {
            errln("ERROR: setPositivePrefix() failed");
        }

        String negPrefix;
        pat.setNegativePrefix("-");
        negPrefix = pat.getNegativePrefix();
        logln("Negative prefix (should be -): " + negPrefix);
        if (negPrefix != "-") {
            errln("ERROR: setNegativePrefix() failed");
        }

        String posSuffix;
        pat.setPositiveSuffix("_");
        posSuffix = pat.getPositiveSuffix();
        logln("Positive suffix (should be _): " + posSuffix);
        if (posSuffix != "_") {
            errln("ERROR: setPositiveSuffix() failed");
        }

        String negSuffix;
        pat.setNegativeSuffix("~");
        negSuffix = pat.getNegativeSuffix();
        logln("Negative suffix (should be ~): " + negSuffix);
        if (negSuffix != "~") {
            errln("ERROR: setNegativeSuffix() failed");
        }

        long multiplier = 0;
        pat.setMultiplier(8);
        multiplier = pat.getMultiplier();
        logln("Multiplier (should be 8): " + multiplier);
        if (multiplier != 8) {
            errln("ERROR: setMultiplier() failed");
        }

        int groupingSize = 0;
        pat.setGroupingSize(2);
        groupingSize = pat.getGroupingSize();
        logln("Grouping size (should be 2): " + (long) groupingSize);
        if (groupingSize != 2) {
            errln("ERROR: setGroupingSize() failed");
        }

        pat.setDecimalSeparatorAlwaysShown(true);
        boolean tf = pat.isDecimalSeparatorAlwaysShown();
        logln(
            "DecimalSeparatorIsAlwaysShown (should be true) is " + (tf ? "true" : "false"));
        if (tf != true) {
            errln("ERROR: setDecimalSeparatorAlwaysShown() failed");
        }

        String funkyPat;
        funkyPat = pat.toPattern();
        logln("Pattern is " + funkyPat);

        String locPat;
        locPat = pat.toLocalizedPattern();
        logln("Localized pattern is " + locPat);

        pat.setCurrencyPluralInfo(infoInput);
        if(!infoInput.equals(pat.getCurrencyPluralInfo())) {
            errln("ERROR: set/get CurrencyPluralInfo() failed");
        }


        pat.setCurrencyPluralInfo(infoInput);
        if(!infoInput.equals(pat.getCurrencyPluralInfo())) {
            errln("ERROR: set/get CurrencyPluralInfo() failed");
        }

        // ======= Test applyPattern()

        logln("Testing applyPattern()");

        String p1 = new String("#,##0.0#;(#,##0.0#)");
        logln("Applying pattern " + p1);
        pat.applyPattern(p1);
        String s2;
        s2 = pat.toPattern();
        logln("Extracted pattern is " + s2);
        if (!s2.equals(p1)) {
            errln("ERROR: toPattern() result did not match pattern applied");
        }

        String p2 = new String("#,##0.0# FF;(#,##0.0# FF)");
        logln("Applying pattern " + p2);
        pat.applyLocalizedPattern(p2);
        String s3;
        s3 = pat.toLocalizedPattern();
        logln("Extracted pattern is " + s3);
        if (!s3.equals(p2)) {
            errln("ERROR: toLocalizedPattern() result did not match pattern applied");
        }

        // ======= Test getStaticClassID()

        //        logln("Testing instanceof()");

        //        try {
        //           NumberFormat test = new DecimalFormat();

        //            if (! (test instanceof DecimalFormat)) {
        //                errln("ERROR: instanceof failed");
        //            }
        //        }
        //        catch (Exception e) {
        //            errln("ERROR: Couldn't create a DecimalFormat");
        //        }

    }

    @Test
    public void TestRounding() {
        double Roundingnumber = 2.55;
        double Roundingnumber1 = -2.55;
        //+2.55 results   -2.55 results
        double result[] = {
            3, -3,
            2, -2,
            3, -2,
            2, -3,
            3, -3,
            3, -3,
            3, -3
        };
        DecimalFormat pat = new DecimalFormat();
        String s = "";
        s = pat.toPattern();
        logln("pattern = " + s);
        int mode;
        int i = 0;
        String message;
        String resultStr;
        for (mode = 0; mode < 7; mode++) {
            pat.setRoundingMode(mode);
            if (pat.getRoundingMode() != mode) {
                errln(
                     "SetRoundingMode or GetRoundingMode failed for mode=" + mode);
            }

            //for +2.55 with RoundingIncrement=1.0
            pat.setRoundingIncrement(1.0);
            resultStr = pat.format(Roundingnumber);
            message = "round(" + Roundingnumber
                    + "," + mode + ",FALSE) with RoundingIncrement=1.0==>";
            verify(message, resultStr, result[i++]);
            message = "";
            resultStr = "";

            //for -2.55 with RoundingIncrement=1.0
            resultStr = pat.format(Roundingnumber1);
            message = "round(" + Roundingnumber1
                    + "," + mode + ",FALSE) with RoundingIncrement=1.0==>";
            verify(message, resultStr, result[i++]);
            message = "";
            resultStr = "";
        }
    }

    @Test
    public void testFormatToCharacterIterator() {

        Number number = new Double(350.76);
        Number negativeNumber = new Double(-350.76);

        Locale us = Locale.US;

        // test number instance
        t_Format(1, number, NumberFormat.getNumberInstance(us),
                getNumberVectorUS());

        // test percent instance
        t_Format(3, number, NumberFormat.getPercentInstance(us),
                getPercentVectorUS());

        // test permille pattern
        DecimalFormat format = new DecimalFormat("###0.##\u2030");
        t_Format(4, number, format, getPermilleVector());

        // test exponential pattern with positive exponent
        format = new DecimalFormat("00.0#E0");
        t_Format(5, number, format, getPositiveExponentVector());

        // test exponential pattern with negative exponent
        format = new DecimalFormat("0000.0#E0");
        t_Format(6, number, format, getNegativeExponentVector());

        // test currency instance with US Locale
        t_Format(7, number, NumberFormat.getCurrencyInstance(us),
                getPositiveCurrencyVectorUS());

        // test negative currency instance with US Locale
        t_Format(8, negativeNumber, NumberFormat.getCurrencyInstance(us),
                getNegativeCurrencyVectorUS());

        // test multiple grouping separators
        number = new Long(100300400);
        t_Format(11, number, NumberFormat.getNumberInstance(us),
                getNumberVector2US());

        // test 0
        number = new Long(0);
        t_Format(12, number, NumberFormat.getNumberInstance(us),
                getZeroVector());
    }

    private static List<FieldContainer> getNumberVectorUS() {
        List<FieldContainer> v = new ArrayList<FieldContainer>(3);
        v.add(new FieldContainer(0, 3, NumberFormat.Field.INTEGER));
        v.add(new FieldContainer(3, 4, NumberFormat.Field.DECIMAL_SEPARATOR));
        v.add(new FieldContainer(4, 6, NumberFormat.Field.FRACTION));
        return v;
    }

//    private static Vector getPositiveCurrencyVectorTR() {
//        Vector v = new Vector();
//        v.add(new FieldContainer(0, 3, NumberFormat.Field.INTEGER));
//        v.add(new FieldContainer(4, 6, NumberFormat.Field.CURRENCY));
//        return v;
//    }
//
//    private static Vector getNegativeCurrencyVectorTR() {
//        Vector v = new Vector();
//        v.add(new FieldContainer(0, 1, NumberFormat.Field.SIGN));
//        v.add(new FieldContainer(1, 4, NumberFormat.Field.INTEGER));
//        v.add(new FieldContainer(5, 7, NumberFormat.Field.CURRENCY));
//        return v;
//    }

    private static List<FieldContainer> getPositiveCurrencyVectorUS() {
        List<FieldContainer> v = new ArrayList<FieldContainer>(4);
        v.add(new FieldContainer(0, 1, NumberFormat.Field.CURRENCY));
        v.add(new FieldContainer(1, 4, NumberFormat.Field.INTEGER));
        v.add(new FieldContainer(4, 5, NumberFormat.Field.DECIMAL_SEPARATOR));
        v.add(new FieldContainer(5, 7, NumberFormat.Field.FRACTION));
        return v;
    }

    private static List<FieldContainer> getNegativeCurrencyVectorUS() {
        List<FieldContainer> v = new ArrayList<FieldContainer>(4);
        // SIGN added with fix for issue 11805.
        v.add(new FieldContainer(0, 1, NumberFormat.Field.SIGN));
        v.add(new FieldContainer(1, 2, NumberFormat.Field.CURRENCY));
        v.add(new FieldContainer(2, 5, NumberFormat.Field.INTEGER));
        v.add(new FieldContainer(5, 6, NumberFormat.Field.DECIMAL_SEPARATOR));
        v.add(new FieldContainer(6, 8, NumberFormat.Field.FRACTION));
        return v;
    }

    private static List<FieldContainer> getPercentVectorUS() {
        List<FieldContainer> v = new ArrayList<FieldContainer>(5);
        v.add(new FieldContainer(0, 2, NumberFormat.Field.INTEGER));
        v.add(new FieldContainer(2, 3, NumberFormat.Field.INTEGER));
        v.add(new FieldContainer(2, 3, NumberFormat.Field.GROUPING_SEPARATOR));
        v.add(new FieldContainer(3, 6, NumberFormat.Field.INTEGER));
        v.add(new FieldContainer(6, 7, NumberFormat.Field.PERCENT));
        return v;
    }

    private static List<FieldContainer> getPermilleVector() {
        List<FieldContainer> v = new ArrayList<FieldContainer>(2);
        v.add(new FieldContainer(0, 6, NumberFormat.Field.INTEGER));
        v.add(new FieldContainer(6, 7, NumberFormat.Field.PERMILLE));
        return v;
    }

    private static List<FieldContainer> getNegativeExponentVector() {
        List<FieldContainer> v = new ArrayList<FieldContainer>(6);
        v.add(new FieldContainer(0, 4, NumberFormat.Field.INTEGER));
        v.add(new FieldContainer(4, 5, NumberFormat.Field.DECIMAL_SEPARATOR));
        v.add(new FieldContainer(5, 6, NumberFormat.Field.FRACTION));
        v.add(new FieldContainer(6, 7, NumberFormat.Field.EXPONENT_SYMBOL));
        v.add(new FieldContainer(7, 8, NumberFormat.Field.EXPONENT_SIGN));
        v.add(new FieldContainer(8, 9, NumberFormat.Field.EXPONENT));
        return v;
    }

    private static List<FieldContainer> getPositiveExponentVector() {
        List<FieldContainer> v = new ArrayList<FieldContainer>(5);
        v.add(new FieldContainer(0, 2, NumberFormat.Field.INTEGER));
        v.add(new FieldContainer(2, 3, NumberFormat.Field.DECIMAL_SEPARATOR));
        v.add(new FieldContainer(3, 5, NumberFormat.Field.FRACTION));
        v.add(new FieldContainer(5, 6, NumberFormat.Field.EXPONENT_SYMBOL));
        v.add(new FieldContainer(6, 7, NumberFormat.Field.EXPONENT));
        return v;
    }

    private static List<FieldContainer> getNumberVector2US() {
        List<FieldContainer> v = new ArrayList<FieldContainer>(7);
        v.add(new FieldContainer(0, 3, NumberFormat.Field.INTEGER));
        v.add(new FieldContainer(3, 4, NumberFormat.Field.GROUPING_SEPARATOR));
        v.add(new FieldContainer(3, 4, NumberFormat.Field.INTEGER));
        v.add(new FieldContainer(4, 7, NumberFormat.Field.INTEGER));
        v.add(new FieldContainer(7, 8, NumberFormat.Field.GROUPING_SEPARATOR));
        v.add(new FieldContainer(7, 8, NumberFormat.Field.INTEGER));
        v.add(new FieldContainer(8, 11, NumberFormat.Field.INTEGER));
        return v;
    }

    private static List<FieldContainer> getZeroVector() {
        List<FieldContainer> v = new ArrayList<FieldContainer>(1);
        v.add(new FieldContainer(0, 1, NumberFormat.Field.INTEGER));
        return v;
    }

    private void t_Format(int count, Object object, Format format,
            List<FieldContainer> expectedResults) {
        List<FieldContainer> results = findFields(format.formatToCharacterIterator(object));
        assertTrue("Test " + count
                + ": Format returned incorrect CharacterIterator for "
                + format.format(object), compare(results, expectedResults));
    }

    /**
     * compares two vectors regardless of the order of their elements
     */
    private static boolean compare(List vector1, List vector2) {
        return vector1.size() == vector2.size() && vector1.containsAll(vector2);
    }

    /**
     * finds attributes with regards to char index in this
     * AttributedCharacterIterator, and puts them in a vector
     *
     * @param iterator
     * @return a vector, each entry in this vector are of type FieldContainer ,
     *         which stores start and end indexes and an attribute this range
     *         has
     */
    private static List<FieldContainer> findFields(AttributedCharacterIterator iterator) {
        List<FieldContainer> result = new ArrayList<FieldContainer>();
        while (iterator.getIndex() != iterator.getEndIndex()) {
            int start = iterator.getRunStart();
            int end = iterator.getRunLimit();

            Iterator it = iterator.getAttributes().keySet().iterator();
            while (it.hasNext()) {
                AttributedCharacterIterator.Attribute attribute = (AttributedCharacterIterator.Attribute) it
                        .next();
                Object value = iterator.getAttribute(attribute);
                result.add(new FieldContainer(start, end, attribute, value));
                // System.out.println(start + " " + end + ": " + attribute + ",
                // " + value );
                // System.out.println("v.add(new FieldContainer(" + start +"," +
                // end +"," + attribute+ "," + value+ "));");
            }
            iterator.setIndex(end);
        }
        return result;
    }
    protected static class FieldContainer {
        int start, end;

        AttributedCharacterIterator.Attribute attribute;

        Object value;

//         called from support_decimalformat and support_simpledateformat tests
        public FieldContainer(int start, int end,
        AttributedCharacterIterator.Attribute attribute) {
            this(start, end, attribute, attribute);
        }

//         called from support_messageformat tests
        public FieldContainer(int start, int end, AttributedCharacterIterator.Attribute attribute, int value) {
        this(start, end, attribute, new Integer(value));
        }

//         called from support_messageformat tests
        public FieldContainer(int start, int end, AttributedCharacterIterator.Attribute attribute,
        Object value) {
        this.start = start;
        this.end = end;
        this.attribute = attribute;
        this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
        if (!(obj instanceof FieldContainer))
        return false;

        FieldContainer fc = (FieldContainer) obj;
        return (start == fc.start && end == fc.end
        && attribute == fc.attribute && value.equals(fc.value));
        }
    }

    /*Helper functions */
    public void verify(String message, String got, double expected) {
        logln(message + got + " Expected : " + (long)expected);
        String expectedStr = "";
        expectedStr=expectedStr + (long)expected;
        if(!got.equals(expectedStr) ) {
            errln("ERROR: Round() failed:  " + message + got + "  Expected : " + expectedStr);
        }
    }
}
//eof
