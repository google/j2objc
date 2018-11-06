/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*****************************************************************************************
 *
 *   Copyright (C) 1996-2012, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 **/

/** 
 * Port From:   JDK 1.4b1 : java.text.Format.IntlTestDecimalFormatAPI
 * Source File: java/text/format/IntlTestDecimalFormatAPI.java
 **/
 
/*
    @test 1.4 98/03/06
    @summary test International Decimal Format API
*/

package android.icu.dev.test.format;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;

import org.junit.Test;

import android.icu.math.BigDecimal;
import android.icu.math.MathContext;
import android.icu.text.DecimalFormat;
import android.icu.text.DecimalFormatSymbols;
import android.icu.text.NumberFormat;

public class IntlTestDecimalFormatAPI extends android.icu.dev.test.TestFmwk
{
    /**
     * Problem 1: simply running 
     * decF4.setRoundingMode(java.math.BigDecimal.ROUND_HALF_UP) does not work 
     * as decF4.setRoundingIncrement(.0001) must also be run.
     * Problem 2: decF4.format(8.88885) does not return 8.8889 as expected. 
     * You must run decF4.format(new BigDecimal(Double.valueOf(8.88885))) in 
     * order for this to work as expected.
     * Problem 3: There seems to be no way to set half up to be the default 
     * rounding mode.
     * We solved the problem with the code at the bottom of this page however 
     * this is not quite general purpose enough to include in icu4j. A static
     * setDefaultRoundingMode function would solve the problem nicely. Also 
     * decimal places past 20 are not handled properly. A small ammount of work 
     * would make bring this up to snuff.
     */
    @Test
    public void testJB1871()
    {
        // problem 2
        double number = 8.88885;
        String expected = "8.8889";
        
        String pat = ",##0.0000";
        DecimalFormat dec = new DecimalFormat(pat);
        dec.setRoundingMode(BigDecimal.ROUND_HALF_UP);
        double roundinginc = 0.0001;
        dec.setRoundingIncrement(roundinginc);
        String str = dec.format(number);
        if (!str.equals(expected)) {
            errln("Fail: " + number + " x \"" + pat + "\" = \"" +
                  str + "\", expected \"" + expected + "\"");
        }   

        pat = ",##0.0001";
        dec = new DecimalFormat(pat);
        dec.setRoundingMode(BigDecimal.ROUND_HALF_UP);
        str = dec.format(number);
        if (!str.equals(expected)) {
            errln("Fail: " + number + " x \"" + pat + "\" = \"" +
                  str + "\", expected \"" + expected + "\"");
        }  
        
        // testing 20 decimal places
        pat = ",##0.00000000000000000001";
        dec = new DecimalFormat(pat);
        BigDecimal bignumber = new BigDecimal("8.888888888888888888885");
        expected = "8.88888888888888888889";
        
        dec.setRoundingMode(BigDecimal.ROUND_HALF_UP);
        str = dec.format(bignumber); 
        if (!str.equals(expected)) {
            errln("Fail: " + bignumber + " x \"" + pat + "\" = \"" +
                  str + "\", expected \"" + expected + "\"");
        }   
        
    }

    /** 
     * This test checks various generic API methods in DecimalFormat to achieve 
     * 100% API coverage.
     */
    @Test
    public void TestAPI()
    {
        logln("DecimalFormat API test---"); logln("");
        Locale.setDefault(Locale.ENGLISH);

        // ======= Test constructors

        logln("Testing DecimalFormat constructors");

        DecimalFormat def = new DecimalFormat();

        final String pattern = new String("#,##0.# FF");
        DecimalFormat pat = null;
        try {
            pat = new DecimalFormat(pattern);
        }
        catch (IllegalArgumentException e) {
            errln("ERROR: Could not create DecimalFormat (pattern)");
        }

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRENCH);

        DecimalFormat cust1 = new DecimalFormat(pattern, symbols);

        // ======= Test clone(), assignment, and equality

        logln("Testing clone() and equality operators");

        Format clone = (Format) def.clone();
        if( ! def.equals(clone)) {
            errln("ERROR: Clone() failed");
        }

        // ======= Test various format() methods

        logln("Testing various format() methods");

//        final double d = -10456.0037; // this appears as -10456.003700000001 on NT
//        final double d = -1.04560037e-4; // this appears as -1.0456003700000002E-4 on NT
        final double d = -10456.00370000000000; // this works!
        final long l = 100000000;
        logln("" + d + " is the double value");

        StringBuffer res1 = new StringBuffer();
        StringBuffer res2 = new StringBuffer();
        StringBuffer res3 = new StringBuffer();
        StringBuffer res4 = new StringBuffer();
        FieldPosition pos1 = new FieldPosition(0);
        FieldPosition pos2 = new FieldPosition(0);
        FieldPosition pos3 = new FieldPosition(0);
        FieldPosition pos4 = new FieldPosition(0);

        res1 = def.format(d, res1, pos1);
        logln("" + d + " formatted to " + res1);

        res2 = pat.format(l, res2, pos2);
        logln("" + l + " formatted to " + res2);

        res3 = cust1.format(d, res3, pos3);
        logln("" + d + " formatted to " + res3);

        res4 = cust1.format(l, res4, pos4);
        logln("" + l + " formatted to " + res4);

        // ======= Test parse()

        logln("Testing parse()");

        String text = new String("-10,456.0037");
        ParsePosition pos = new ParsePosition(0);
        String patt = new String("#,##0.#");
        pat.applyPattern(patt);
        double d2 = pat.parse(text, pos).doubleValue();
        if(d2 != d) {
            errln("ERROR: Roundtrip failed (via parse(" + d2 + " != " + d + ")) for " + text);
        }
        logln(text + " parsed into " + (long) d2);

        // ======= Test getters and setters

        logln("Testing getters and setters");

        final DecimalFormatSymbols syms = pat.getDecimalFormatSymbols();
        def.setDecimalFormatSymbols(syms);
        if( ! pat.getDecimalFormatSymbols().equals(def.getDecimalFormatSymbols())) {
            errln("ERROR: set DecimalFormatSymbols() failed");
        }

        String posPrefix;
        pat.setPositivePrefix("+");
        posPrefix = pat.getPositivePrefix();
        logln("Positive prefix (should be +): " + posPrefix);
        if(posPrefix != "+") {
            errln("ERROR: setPositivePrefix() failed");
        }

        String negPrefix;
        pat.setNegativePrefix("-");
        negPrefix = pat.getNegativePrefix();
        logln("Negative prefix (should be -): " + negPrefix);
        if(negPrefix != "-") {
            errln("ERROR: setNegativePrefix() failed");
        }

        String posSuffix;
        pat.setPositiveSuffix("_");
        posSuffix = pat.getPositiveSuffix();
        logln("Positive suffix (should be _): " + posSuffix);
        if(posSuffix != "_") {
            errln("ERROR: setPositiveSuffix() failed");
        }

        String negSuffix;
        pat.setNegativeSuffix("~");
        negSuffix = pat.getNegativeSuffix();
        logln("Negative suffix (should be ~): " + negSuffix);
        if(negSuffix != "~") {
            errln("ERROR: setNegativeSuffix() failed");
        }

        long multiplier = 0;
        pat.setMultiplier(8);
        multiplier = pat.getMultiplier();
        logln("Multiplier (should be 8): " + multiplier);
        if(multiplier != 8) {
            errln("ERROR: setMultiplier() failed");
        }

        int groupingSize = 0;
        pat.setGroupingSize(2);
        groupingSize = pat.getGroupingSize();
        logln("Grouping size (should be 2): " + (long) groupingSize);
        if(groupingSize != 2) {
            errln("ERROR: setGroupingSize() failed");
        }

        pat.setDecimalSeparatorAlwaysShown(true);
        boolean tf = pat.isDecimalSeparatorAlwaysShown();
        logln("DecimalSeparatorIsAlwaysShown (should be true) is " +  (tf ? "true" : "false"));
        if(tf != true) {
            errln("ERROR: setDecimalSeparatorAlwaysShown() failed");
        }

        String funkyPat;
        funkyPat = pat.toPattern();
        logln("Pattern is " + funkyPat);

        String locPat;
        locPat = pat.toLocalizedPattern();
        logln("Localized pattern is " + locPat);

        // ======= Test applyPattern()

        logln("Testing applyPattern()");

        String p1 = new String("#,##0.0#;(#,##0.0#)");
        logln("Applying pattern " + p1);
        pat.applyPattern(p1);
        String s2;
        s2 = pat.toPattern();
        logln("Extracted pattern is " + s2);
        if( ! s2.equals(p1) ) {
            errln("ERROR: toPattern() result did not match pattern applied");
        }

        String p2 = new String("#,##0.0# FF;(#,##0.0# FF)");
        logln("Applying pattern " + p2);
        pat.applyLocalizedPattern(p2);
        String s3;
        s3 = pat.toLocalizedPattern();
        logln("Extracted pattern is " + s3);
        if( ! s3.equals(p2) ) {
            errln("ERROR: toLocalizedPattern() result did not match pattern applied");
        }
    }

    @Test
    public void testJB6134()
    {
        DecimalFormat decfmt = new DecimalFormat();
        StringBuffer buf = new StringBuffer();

        FieldPosition fposByInt = new FieldPosition(NumberFormat.INTEGER_FIELD);
        decfmt.format(123, buf, fposByInt);

        buf.setLength(0);
        FieldPosition fposByField = new FieldPosition(NumberFormat.Field.INTEGER);
        decfmt.format(123, buf, fposByField);

        if (fposByInt.getEndIndex() != fposByField.getEndIndex())
        {
            errln("ERROR: End index for integer field - fposByInt:" + fposByInt.getEndIndex() +
                " / fposByField: " + fposByField.getEndIndex());
        }
    }

    @Test
    public void testJB4971()
    {
        DecimalFormat decfmt = new DecimalFormat();
        MathContext resultICU;

        MathContext comp1 = new MathContext(0, MathContext.PLAIN);
        resultICU = decfmt.getMathContextICU();
        if ((comp1.getDigits() != resultICU.getDigits()) ||
            (comp1.getForm() != resultICU.getForm()) ||
            (comp1.getLostDigits() != resultICU.getLostDigits()) ||
            (comp1.getRoundingMode() != resultICU.getRoundingMode()))
        {
            errln("ERROR: Math context 1 not equal - result: " + resultICU.toString() +
                " / expected: " + comp1.toString());
        }

        MathContext comp2 = new MathContext(5, MathContext.ENGINEERING);
        decfmt.setMathContextICU(comp2);
        resultICU = decfmt.getMathContextICU();
        if ((comp2.getDigits() != resultICU.getDigits()) ||
            (comp2.getForm() != resultICU.getForm()) ||
            (comp2.getLostDigits() != resultICU.getLostDigits()) ||
            (comp2.getRoundingMode() != resultICU.getRoundingMode()))
        {
            errln("ERROR: Math context 2 not equal - result: " + resultICU.toString() +
                " / expected: " + comp2.toString());
        }

        java.math.MathContext result;

        java.math.MathContext comp3 = new java.math.MathContext(3, java.math.RoundingMode.DOWN);
        decfmt.setMathContext(comp3);
        result = decfmt.getMathContext();
        if ((comp3.getPrecision() != result.getPrecision()) ||
            (comp3.getRoundingMode() != result.getRoundingMode()))
        {
            errln("ERROR: Math context 3 not equal - result: " + result.toString() +
                " / expected: " + comp3.toString());
        }

    }

    @Test
    public void testJB6354()
    {
        DecimalFormat pat = new DecimalFormat("#,##0.00");
        java.math.BigDecimal r1, r2;

        // get default rounding increment
        r1 = pat.getRoundingIncrement();

        // set rounding mode with zero increment.  Rounding 
        // increment should be set by this operation
        pat.setRoundingMode(BigDecimal.ROUND_UP);
        r2 = pat.getRoundingIncrement();

        // check for different values
        if ((r1 != null) && (r2 != null))
        {
            if (r1.compareTo(r2) == 0)
            {
                errln("ERROR: Rounding increment did not change");
            }
        }
    }
    
    @Test
    public void testJB6648()
    {
        DecimalFormat df = new DecimalFormat();
        df.setParseStrict(true);
        
        String numstr = new String();
        
        String[] patterns = {
            "0",
            "00",
            "000",
            "0,000",
            "0.0",
            "#000.0"          
        };
        
        for(int i=0; i < patterns.length; i++) {
            df.applyPattern(patterns[i]);
            numstr = df.format(5);        
            try {
                Number n = df.parse(numstr);
                logln("INFO: Parsed " + numstr + " -> " + n);
            } catch (ParseException pe) {
                errln("ERROR: Failed round trip with strict parsing.");
            }           
        }
        
        df.applyPattern(patterns[1]);
        numstr = "005";        
        try {
            Number n = df.parse(numstr);
            logln("INFO: Successful parse for " + numstr + " with strict parse enabled. Number is " + n);
        } catch (ParseException pe) {
            errln("ERROR: Parse Exception encountered in strict mode: numstr -> " + numstr);
        }  
        
    }
}
