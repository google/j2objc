/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2001-2012, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

/** 
 * Port From:   ICU4C v1.8.1 : format : NumberFormatRegressionTest
 * Source File: $ICU4CRoot/source/test/intltest/numrgts.cpp
 **/

package android.icu.dev.test.format;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.Locale;

import org.junit.Test;

import android.icu.text.DateFormat;
import android.icu.text.DecimalFormat;
import android.icu.text.DecimalFormatSymbols;
import android.icu.text.NumberFormat;
import android.icu.util.Calendar;
import android.icu.util.ULocale;

/** 
 * Performs regression test for MessageFormat
 **/
public class NumberFormatRegressionTest extends android.icu.dev.test.TestFmwk {
    /**
     * alphaWorks upgrade
     */
    @Test
    public void Test4161100() {
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        nf.setMinimumFractionDigits(1);
        nf.setMaximumFractionDigits(1);
        double a = -0.09;
        String s = nf.format(a);
        logln(a + " x " +
              ((DecimalFormat) nf).toPattern() + " = " + s);
        if (!s.equals("-0.1")) {
            errln("FAIL");
        }
    }
    
    /**
     * DateFormat should call setIntegerParseOnly(TRUE) on adopted
     * NumberFormat objects.
     */
    @Test
    public void TestJ691() {
        
        Locale loc = new Locale("fr", "CH");
    
        // set up the input date string & expected output
        String udt = "11.10.2000";
        String exp = "11.10.00";
    
        // create a Calendar for this locale
        Calendar cal = Calendar.getInstance(loc);
    
        // create a NumberFormat for this locale
        NumberFormat nf = NumberFormat.getInstance(loc);
    
        // *** Here's the key: We don't want to have to do THIS:
        //nf.setParseIntegerOnly(true);
    
        // create the DateFormat
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, loc);
    
        df.setCalendar(cal);
        df.setNumberFormat(nf);
    
        // set parsing to lenient & parse
        Date ulocdat = new Date();
        df.setLenient(true);
        try {
            ulocdat = df.parse(udt);
        } catch (java.text.ParseException pe) {
            errln(pe.getMessage());
        }
        // format back to a string
        String outString = df.format(ulocdat);
    
        if (!outString.equals(exp)) {
            errln("FAIL: " + udt + " => " + outString);
        }
    }
    
    /**
     * Test getIntegerInstance();
     */
    @Test
    public void Test4408066() {
        
        NumberFormat nf1 = NumberFormat.getIntegerInstance();
        NumberFormat nf2 = NumberFormat.getIntegerInstance(Locale.CHINA);
    
        //test isParseIntegerOnly
        if (!nf1.isParseIntegerOnly() || !nf2.isParseIntegerOnly()) {
            errln("Failed : Integer Number Format Instance should set setParseIntegerOnly(true)");
        }
    
        //Test format
        {
            double[] data = {
                -3.75, -2.5, -1.5, 
                -1.25, 0,    1.0, 
                1.25,  1.5,  2.5, 
                3.75,  10.0, 255.5
                };
            String[] expected = {
                "-4", "-2", "-2",
                "-1", "0",  "1",
                "1",  "2",  "2",
                "4",  "10", "256"
                };
    
            for (int i = 0; i < data.length; ++i) {
                String result = nf1.format(data[i]);
                if (!result.equals(expected[i])) {
                    errln("Failed => Source: " + Double.toString(data[i]) 
                        + ";Formatted : " + result
                        + ";but expectted: " + expected[i]);
                }
            }
        }
        //Test parse, Parsing should stop at "."
        {
            String data[] = {
                "-3.75", "-2.5", "-1.5", 
                "-1.25", "0",    "1.0", 
                "1.25",  "1.5",  "2.5", 
                "3.75",  "10.0", "255.5"
                };
            long[] expected = {
                -3, -2, -1,
                -1, 0,  1,
                1,  1,  2,
                3,  10, 255
                };
            
            for (int i = 0; i < data.length; ++i) {
                Number n = null;
                try {
                    n = nf1.parse(data[i]);
                } catch (ParseException e) {
                    errln("Failed: " + e.getMessage());
                }
                if (!(n instanceof Long) || (n instanceof Integer)) {
                    errln("Failed: Integer Number Format should parse string to Long/Integer");
                }
                if (n.longValue() != expected[i]) {
                    errln("Failed=> Source: " + data[i] 
                        + ";result : " + n.toString()
                        + ";expected :" + Long.toString(expected[i]));
                }
            }
        }
    }
    
    //Test New serialized DecimalFormat(2.0) read old serialized forms of DecimalFormat(1.3.1.1)
    @Test
    public void TestSerialization() throws IOException{
        byte[][] contents = NumberFormatSerialTestData.getContent();
        double data = 1234.56;
        String[] expected = {
            "1,234.56", "$1,234.56", "123,456%", "1.23456E3"};
        for (int i = 0; i < 4; ++i) {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(contents[i]));
            try {
                NumberFormat format = (NumberFormat) ois.readObject();
                String result = format.format(data);
                if (result.equals(expected[i])) {
                    logln("OK: Deserialized bogus NumberFormat(new version read old version)");
                } else {
                    errln("FAIL: the test data formats are not euqal");
                }
            } catch (Exception e) {
                warnln("FAIL: " + e.getMessage());
            }
        }
    }

    /*
     * Test case for JB#5509, strict parsing issue
     */
    @Test
    public void TestJB5509() {
        String[] data = {
                "1,2",
                "1.2",
                "1,2.5",
                "1,23.5",
                "1,234.5",
                "1,234",
                "1,234,567",
                "1,234,567.8",
                "1,234,5",
                "1,234,5.6",
                "1,234,56.7"
        };
        boolean[] expected = { // false for expected parse failure
                false,
                true,
                false,
                false,
                true,
                true,
                true,
                true,
                false,
                false,
                false,
                false
        };

        DecimalFormat df = new DecimalFormat("#,##0.###", new DecimalFormatSymbols(new ULocale("en_US")));
        df.setParseStrict(true);
        for (int i = 0; i < data.length; i++) {
            try {
                df.parse(data[i]);
                if (!expected[i]) {
                    errln("Failed: ParseException must be thrown for string " + data[i]);
                }
            } catch (ParseException pe) {
                if (expected[i]) {
                    errln("Failed: ParseException must not be thrown for string " + data[i]);
                }
            }
        }
    }

    /*
     * Test case for ticket#5698 - parsing extremely large/small values
     */
    @Test
    public void TestT5698() {
        final String[] data = {
                "12345679E66666666666666666",
                "-12345679E66666666666666666",
                ".1E2147483648", // exponent > max int
                ".1E2147483647", // exponent == max int
                ".1E-2147483648", // exponent == min int
                ".1E-2147483649", // exponent < min int
                "1.23E350", // value > max double
                "1.23E300", // value < max double
                "-1.23E350", // value < min double
                "-1.23E300", // value > min double
                "4.9E-324", // value = smallest non-zero double
                "1.0E-325", // 0 < value < smallest non-zero positive double0
                "-1.0E-325", // 0 > value > largest non-zero negative double
        };
        final double[] expected = {
                Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                0.0,
                0.0,
                Double.POSITIVE_INFINITY,
                1.23e300d,
                Double.NEGATIVE_INFINITY,
                -1.23e300d,
                4.9e-324d,
                0.0,
                -0.0,
        };

        NumberFormat nfmt = NumberFormat.getInstance();

        for (int i = 0; i < data.length; i++) {
            try {
                Number n = nfmt.parse(data[i]);
                if (expected[i] != n.doubleValue()) {
                    errln("Failed: Parsed result for " + data[i] + ": " 
                            + n.doubleValue() + " / expected: " + expected[i]);
                }
            } catch (ParseException pe) {
                errln("Failed: ParseException is thrown for " + data[i]);
            }
        }
    }
    @Test
    public void TestSurrogatesParsing() { // Test parsing of numbers that use digits from the supplemental planes.
        final String[] data = {
                "1\ud801\udca2,3\ud801\udca45.67", // 
                "\ud801\udca1\ud801\udca2,\ud801\udca3\ud801\udca4\ud801\udca5.\ud801\udca6\ud801\udca7\ud801\udca8", // 
                "\ud835\udfd2.\ud835\udfd7E-\ud835\udfd1",
                "\ud835\udfd3.8E-0\ud835\udfd0"
                };
        final double[] expected = {
                12345.67,
                12345.678,
                0.0049,
                0.058
        };

        NumberFormat nfmt = NumberFormat.getInstance();

        for (int i = 0; i < data.length; i++) {
            try {
                Number n = nfmt.parse(data[i]);
                if (expected[i] != n.doubleValue()) {
                    errln("Failed: Parsed result for " + data[i] + ": " 
                            + n.doubleValue() + " / expected: " + expected[i]);
                }
            } catch (ParseException pe) {
                errln("Failed: ParseException is thrown for " + data[i]);
            }
        }
    }

    void checkNBSPPatternRtNum(String testcase, NumberFormat nf, double myNumber) {
        String myString = nf.format(myNumber);
        
            double aNumber;
            try {
                aNumber = nf.parse(myString).doubleValue();
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                errln("FAIL: " + testcase +" - failed to parse. " + e.toString());
                return;
            }
            if(Math.abs(aNumber-myNumber)>.001) {
            errln("FAIL: "+testcase+": formatted "+myNumber+", parsed into "+aNumber+"\n");
        } else {
            logln("PASS: "+testcase+": formatted "+myNumber+", parsed into "+aNumber+"\n");
        }
    }

    void checkNBSPPatternRT(String testcase, NumberFormat nf) {
        checkNBSPPatternRtNum(testcase, nf, 12345.);
        checkNBSPPatternRtNum(testcase, nf, -12345.);
    }

    @Test
    public void TestNBSPInPattern() {
    NumberFormat nf = null;
    String testcase;
    
    
    testcase="ar_AE UNUM_CURRENCY";
    nf = NumberFormat.getCurrencyInstance(new ULocale("ar_AE"));
    checkNBSPPatternRT(testcase, nf);
    // if we don't have CLDR 1.6 data, bring out the problem anyways 
    
    String SPECIAL_PATTERN = "\u00A4\u00A4'\u062f.\u0625.\u200f\u00a0'###0.00";
    testcase = "ar_AE special pattern: " + SPECIAL_PATTERN;
    nf = new DecimalFormat();
    ((DecimalFormat)nf).applyPattern(SPECIAL_PATTERN);
    checkNBSPPatternRT(testcase, nf);
    
    }

    /*
     * Test case for #9293
     * Parsing currency in strict mode
     */
    @Test
    public void TestT9293() {
        NumberFormat fmt = NumberFormat.getCurrencyInstance();
        fmt.setParseStrict(true);

        final int val = 123456;
        String txt = fmt.format(123456);

        ParsePosition pos = new ParsePosition(0);
        Number num = fmt.parse(txt, pos);

        if (pos.getErrorIndex() >= 0) {
            errln("FAIL: Parsing " + txt + " - error index: " + pos.getErrorIndex());
        } else if (val != num.intValue()) {
            errln("FAIL: Parsed result: " + num + " - expected: " + val);
        }
    }
}
