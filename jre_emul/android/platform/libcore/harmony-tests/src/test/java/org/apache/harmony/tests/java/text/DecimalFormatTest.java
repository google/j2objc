/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.tests.java.text;

import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.AttributedCharacterIterator;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;


public class DecimalFormatTest extends TestCase {

    // https://code.google.com/p/android/issues/detail?id=59600
    public void test_setNan_emptyString() throws Exception {
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setNaN("");
        DecimalFormat df = new DecimalFormat();
        df.setDecimalFormatSymbols(dfs);
        df.format(Double.NaN);
    }

    public void testAttributedCharacterIterator() throws Exception {
        // Regression for http://issues.apache.org/jira/browse/HARMONY-333
        AttributedCharacterIterator iterator = new DecimalFormat().formatToCharacterIterator(
                new Integer(1));
        assertNotNull(iterator);
        assertFalse("attributes should exist", iterator.getAttributes().isEmpty());
    }

    public void test_parse_bigDecimal() throws Exception {
        // parseBigDecimal default to false
        DecimalFormat form = (DecimalFormat) DecimalFormat.getInstance(Locale.US);
        assertFalse(form.isParseBigDecimal());
        form.setParseBigDecimal(true);
        assertTrue(form.isParseBigDecimal());

        Number result = form.parse("123.123");
        assertEquals(new BigDecimal("123.123"), result);

        form.setParseBigDecimal(false);
        assertFalse(form.isParseBigDecimal());

        result = form.parse("123.123");
        assertFalse(result instanceof BigDecimal);
    }

    public void test_parse_integerOnly() throws Exception {
        DecimalFormat format = new DecimalFormat();
        assertFalse("Default value of isParseIntegerOnly is true", format.isParseIntegerOnly());

        format.setParseIntegerOnly(true);
        assertTrue(format.isParseIntegerOnly());
        Number result = format.parse("123.123");
        assertEquals(new Long("123"), result);

        format.setParseIntegerOnly(false);
        assertFalse(format.isParseIntegerOnly());
        result = format.parse("123.123");
        assertEquals(new Double("123.123"), result);
    }

    // Test the type of the returned object
    public void test_parse_returnType() {
        DecimalFormat form = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        Number number = form.parse("23.1", new ParsePosition(0));
        assertTrue(number instanceof Double);

        // Test parsed object of type double when
        // parseBigDecimal is set to true

        form = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        number = form.parse("23.1", new ParsePosition(0));
        assertTrue(number instanceof Double);

        form.setParseBigDecimal(true);
        number = form.parse("23.1", new ParsePosition(0));

        assertTrue(number instanceof BigDecimal);
        assertEquals(new BigDecimal("23.1"), number);

        // When parseIntegerOnly set to true, all numbers will be parsed
        // into Long unless the value is out of the bound of Long or
        // some special values such as NaN or Infinity.

        form = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        form.setParseIntegerOnly(true);
        number = form.parse("23.1f", new ParsePosition(0));

        assertTrue(number instanceof Long);

        number = form.parse("23.0", new ParsePosition(0));
        assertTrue(number instanceof Long);

        number = form.parse("-0.0", new ParsePosition(0));
        assertTrue(number instanceof Long);
        assertTrue(new Long(0).equals(number));

        // The last integers representable by long.
        number = form.parse("9223372036854775807.00", new ParsePosition(0));
        assertEquals(Long.class, number.getClass());
        number = form.parse("9223372036854775808.00", new ParsePosition(0));
        assertEquals(Double.class, number.getClass());
        // The first integers that need to be represented by double.
        number = form.parse("-9223372036854775808.00", new ParsePosition(0));
        assertEquals(Long.class, number.getClass());
        number = form.parse("-9223372036854775809.00", new ParsePosition(0));
        assertEquals(Double.class, number.getClass());

        // Even if parseIntegerOnly is set to true, NaN will be parsed to Double

        form = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        form.setParseIntegerOnly(true);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        number = form.parse(symbols.getNaN(), new ParsePosition(0));
        assertTrue(number instanceof Double);

        // Even if parseIntegerOnly is set to true, Infinity will still be
        // parsed to Double

        form = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        form.setParseIntegerOnly(true);
        symbols = new DecimalFormatSymbols();
        number = form.parse(symbols.getInfinity(), new ParsePosition(0));
        assertTrue(number instanceof Double);

        // ParseBigDecimal take precedence of parseBigInteger

        form = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        form.setParseIntegerOnly(true);
        form.setParseBigDecimal(true);

        number = form.parse("23.1f", new ParsePosition(0));

        assertTrue(number instanceof BigDecimal);

        number = form.parse("23.0", new ParsePosition(0));
        assertTrue(number instanceof BigDecimal);

        number = form.parse("-92,233,720,368,547,758,080.00", new ParsePosition(0));
        assertFalse(number instanceof BigInteger);
        assertTrue(number instanceof BigDecimal);

        // Test whether the parsed object is of type float. (To be specific,
        // they are of type Double)

        form = (DecimalFormat) NumberFormat.getInstance(Locale.US);

        number = form.parse("23.1f", new ParsePosition(0));
        assertTrue(number instanceof Double);

        form.setParseBigDecimal(true);
        number = form.parse("23.1f", new ParsePosition(0));
        assertTrue(number instanceof BigDecimal);
        assertEquals(new BigDecimal("23.1"), number);

        // Integer will be parsed to Long, unless parseBigDecimal is set to true

        form = (DecimalFormat) NumberFormat.getInstance(Locale.US);

        number = form.parse("123", new ParsePosition(0));
        assertTrue(number instanceof Long);

        form.setParseBigDecimal(true);
        number = form.parse("123", new ParsePosition(0));
        assertTrue(number instanceof BigDecimal);
        assertEquals(new BigDecimal("123"), number);

        // NaN will be parsed to Double, no matter parseBigDecimal set or not.

        form = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        symbols = new DecimalFormatSymbols();
        number = form.parse(symbols.getNaN() + "", new ParsePosition(0));
        assertTrue(number instanceof Double);

        form.setParseBigDecimal(true);
        number = form.parse(symbols.getNaN() + "", new ParsePosition(0));
        assertTrue(number instanceof Double);

        // Infinity will be parsed to Double, no matter parseBigDecimal set or
        // not.

        form = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        symbols = new DecimalFormatSymbols();

        number = form.parse(symbols.getInfinity(), new ParsePosition(0));

        assertTrue(number instanceof Double);
        assertEquals("Infinity", number.toString());
        // When set bigDecimal to true, the result of parsing infinity

        form = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        symbols = new DecimalFormatSymbols();
        form.setParseBigDecimal(true);

        number = form.parse(symbols.getInfinity(), new ParsePosition(0));
        assertTrue(number instanceof Double);
        assertEquals("Infinity", number.toString());

        // Negative infinity will be parsed to double no matter parseBigDecimal
        // set or not

        form = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        symbols = new DecimalFormatSymbols();

        number = form.parse("-" + symbols.getInfinity(), new ParsePosition(0));

        assertTrue(number instanceof Double);
        assertEquals("-Infinity", number.toString());

        // When set bigDecimal to true, the result of parsing minus infinity

        form = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        symbols = new DecimalFormatSymbols();
        form.setParseBigDecimal(true);

        number = form.parse("-" + symbols.getInfinity(), new ParsePosition(0));

        assertTrue(number instanceof Double);
        assertEquals("-Infinity", number.toString());

        // -0.0 will be parsed to different type according to the combination of
        // parseBigDecimal and parseIntegerOnly

        form = (DecimalFormat) NumberFormat.getInstance(Locale.US);

        // parseBigDecimal == true;
        // parseIntegerOnly == false;
        form.setParseBigDecimal(true);
        number = form.parse("-0", new ParsePosition(0));
        assertTrue(number instanceof BigDecimal);

        number = form.parse("-0.0", new ParsePosition(0));
        assertTrue(number instanceof BigDecimal);

        // parseBigDecimal == false;
        // parseIntegerOnly == true;
        form.setParseBigDecimal(false);
        form.setParseIntegerOnly(true);
        number = form.parse("-0", new ParsePosition(0));

        assertTrue(number instanceof Long);

        number = form.parse("-0.0", new ParsePosition(0));
        assertTrue(number instanceof Long);

        // parseBigDecimal == false;
        // parseIntegerOnly == false;
        form.setParseBigDecimal(false);
        form.setParseIntegerOnly(false);
        number = form.parse("-0", new ParsePosition(0));
        assertTrue(number instanceof Double);

        number = form.parse("-0.0", new ParsePosition(0));
        assertTrue(number instanceof Double);

        // parseBigDecimal == true;
        // parseIntegerOnly == true;
        // parseBigDecimal take precedence of parseBigInteger
        form.setParseBigDecimal(true);
        form.setParseIntegerOnly(true);
        number = form.parse("-0", new ParsePosition(0));
        assertTrue(number instanceof BigDecimal);

        number = form.parse("-0.0", new ParsePosition(0));
        assertTrue(number instanceof BigDecimal);

        number = form.parse("12.4", new ParsePosition(0));
        assertTrue(number instanceof BigDecimal);

        // When parseBigDecimal is set to false, no matter how massive the
        // mantissa part of a number is, the number will be parsed into Double

        form = (DecimalFormat) NumberFormat.getInstance(Locale.US);

        number = form.parse("9,223,372,036,854,775,808.00",
                new ParsePosition(0));

        assertTrue(number instanceof Double);
        assertEquals("9.223372036854776E18", number.toString());

        number = form.parse("-92,233,720,368,547,758,080.00",
                new ParsePosition(0));
        assertTrue(number instanceof Double);
        assertEquals("-9.223372036854776E19", number.toString());

        // When parseBigDecimal is set to true, if mantissa part of number
        // exceeds Long.MAX_VALUE, the number will be parsed into BigDecimal

        form = (DecimalFormat) NumberFormat.getInstance(Locale.US);

        form.setParseBigDecimal(true);
        number = form.parse("9,223,372,036,854,775,808.00",
                new ParsePosition(0));

        assertTrue(number instanceof BigDecimal);

        assertEquals(9.223372036854776E18, number.doubleValue(), 0);

        number = form.parse("-92,233,720,368,547,758,080.00", new ParsePosition(0));

        assertTrue(number instanceof BigDecimal);
        assertEquals(-9.223372036854776E19, number.doubleValue(), 0);

        // The minimum value of Long will be parsed to Long when parseBigDecimal
        // is not set

        ParsePosition pos = new ParsePosition(0);
        DecimalFormat df = new DecimalFormat();
        pos = new ParsePosition(0);
        Number nb = df.parse("" + Long.MIN_VALUE, pos);
        assertTrue(nb instanceof Long);

        // The maximum value of Long will be parsed to Long when parseBigDecimal
        // is set
        pos = new ParsePosition(0);
        df = new DecimalFormat();
        pos = new ParsePosition(0);
        nb = df.parse("" + Long.MAX_VALUE, pos);
        assertTrue(nb instanceof Long);

        // When parsing invalid string( which is neither consist of digits nor
        // NaN/Infinity), a null will be returned.

        pos = new ParsePosition(0);
        df = new DecimalFormat();
        try {
            nb = df.parse("invalid", pos);
            assertNull(nb);
        } catch (NullPointerException e) {
            fail("Should not throw NPE");
        }
    }

    public void test_parse_largeBigDecimal() {
        DecimalFormat form = (DecimalFormat) DecimalFormat.getInstance(Locale.US);
        form.setParseIntegerOnly(true);
        form.setParseBigDecimal(true);

        final String doubleMax2 = "359,538,626,972,463,141,629,054,847,463,408,"
                + "713,596,141,135,051,689,993,197,834,953,606,314,521,560,057,077,"
                + "521,179,117,265,533,756,343,080,917,907,028,764,928,468,642,653,"
                + "778,928,365,536,935,093,407,075,033,972,099,821,153,102,564,152,"
                + "490,980,180,778,657,888,151,737,016,910,267,884,609,166,473,806,"
                + "445,896,331,617,118,664,246,696,549,595,652,408,289,446,337,476,"
                + "354,361,838,599,762,500,808,052,368,249,716,736";
        Number number = form.parse(doubleMax2, new ParsePosition(0));
        assertTrue(number instanceof BigDecimal);
        BigDecimal result = (BigDecimal) number;
        assertEquals(new BigDecimal(Double.MAX_VALUE).add(new BigDecimal(Double.MAX_VALUE)),
                result);
    }

    public void testMaximumFractionDigits_getAndSet() {
        DecimalFormat form = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        // getMaximumFractionDigits of DecimalFormat defaults to 3
        assertEquals(3, form.getMaximumFractionDigits());

        form.setMaximumFractionDigits(310);
        assertEquals(310, form.getMaximumFractionDigits());

        // Deliberately > 340. The API docs mention 340 and suggest that you can set the value
        // higher but it will use 340 as a ceiling.
        form.setMaximumFractionDigits(500);
        assertEquals(500, form.getMaximumFractionDigits());

        form.setMaximumFractionDigits(500);
        assertEquals(500, form.getMaximumFractionDigits());
        form.format(12.3);
        assertEquals(500, form.getMaximumFractionDigits());

        form.setMaximumFractionDigits(-2);
        assertEquals(0, form.getMaximumFractionDigits());
    }

    public void testMinimumFractionDigits_getAndSet() {
        DecimalFormat form = (DecimalFormat) NumberFormat.getInstance(Locale.US);

        // getMinimumFractionDigits from NumberFormat (default to 0)
        // getMinimumFractionDigits from DecimalFormat (default to 0)
        assertEquals(0, form.getMinimumFractionDigits());

        form.setMinimumFractionDigits(310);
        assertEquals(310, form.getMinimumFractionDigits());

        // Deliberately > 340. The API docs mention 340 and suggest that you can set the value
        // higher but it will use 340 as a ceiling.
        form.setMinimumFractionDigits(500);
        assertEquals(500, form.getMinimumFractionDigits());

        form.setMaximumFractionDigits(400);
        assertEquals(400, form.getMinimumFractionDigits());

        form.setMinimumFractionDigits(-3);
        assertEquals(0, form.getMinimumFractionDigits());
    }

    public void testMaximumIntegerDigits_getAndSet() {
        // When use default locale, in this case zh_CN
        // the returned instance of NumberFormat is a DecimalFormat
        DecimalFormat form = new DecimalFormat("00.###E0");
        assertEquals(2, form.getMaximumIntegerDigits());

        form = (DecimalFormat) NumberFormat.getInstance(Locale.US);

        form.setMaximumIntegerDigits(300);
        assertEquals(300, form.getMaximumIntegerDigits());

        // Deliberately > 309. The API docs mention 309 and suggest that you can set the value
        // higher but it will use 309 as a ceiling.
        form.setMaximumIntegerDigits(500);
        assertEquals(500, form.getMaximumIntegerDigits());

        form = new DecimalFormat("00.###E0");
        assertEquals(2, form.getMaximumIntegerDigits());

        form.setMaximumIntegerDigits(500);
        assertEquals(500, form.getMaximumIntegerDigits());
        form.format(12.3);
        assertEquals(500, form.getMaximumIntegerDigits());

        form.setMaximumIntegerDigits(-3);
        assertEquals(0, form.getMaximumIntegerDigits());

        // regression test for HARMONY-878
        assertTrue(new DecimalFormat("0\t'0'").getMaximumIntegerDigits() > 0);
    }

    public void testMinimumIntegerDigits_getAndSet() {
        final int minIntDigit = 1;
        DecimalFormat form = (DecimalFormat) NumberFormat.getInstance(Locale.US);

        // getMaximumIntegerDigits from DecimalFormat (default to 1)
        assertEquals(minIntDigit, form.getMinimumIntegerDigits());

        form.setMinimumIntegerDigits(300);
        assertEquals(300, form.getMinimumIntegerDigits());

        // Deliberately > 309. The API docs mention 309 and suggest that you can set the value
        // higher but it will use 309 as a ceiling.
        form.setMinimumIntegerDigits(500);
        assertEquals(500, form.getMinimumIntegerDigits());

        form.setMaximumIntegerDigits(400);
        assertEquals(400, form.getMinimumIntegerDigits());

        form.setMinimumIntegerDigits(-3);
        assertEquals(0, form.getMinimumIntegerDigits());
    }

    // When MaxFractionDigits is set first and less than MinFractionDigits, max
    // will be changed to min value
    public void testMinimumFactionDigits_minChangesMax() {
        DecimalFormat form = (DecimalFormat) NumberFormat.getInstance(Locale.US);

        form.setMaximumFractionDigits(100);
        form.setMinimumFractionDigits(200);

        assertEquals(200, form.getMaximumFractionDigits());
        assertEquals(200, form.getMinimumFractionDigits());

        form.setMaximumIntegerDigits(100);
        form.setMinimumIntegerDigits(200);

        assertEquals(200, form.getMaximumIntegerDigits());
        assertEquals(200, form.getMinimumIntegerDigits());
    }

    // When MinFractionDigits is set first and less than MaxFractionDigits, min
    // will be changed to max value
    public void testMaximumFactionDigits_maxChangesMin() {
        DecimalFormat form = (DecimalFormat) NumberFormat.getInstance(Locale.US);

        form.setMinimumFractionDigits(200);
        form.setMaximumFractionDigits(100);

        assertEquals(100, form.getMaximumFractionDigits());
        assertEquals(100, form.getMinimumFractionDigits());

        form.setMinimumIntegerDigits(200);
        form.setMaximumIntegerDigits(100);

        assertEquals(100, form.getMaximumIntegerDigits());
        assertEquals(100, form.getMinimumIntegerDigits());
    }

    public void test_formatObject_errorCases() {
        DecimalFormat form = (DecimalFormat) NumberFormat.getInstance(Locale.US);

        // If Object(including null) is not of type Number,
        // IllegalArgumentException will be thrown out
        try {
            form.format(new Object(), new StringBuffer(), new FieldPosition(0));
            fail("Should throw IAE");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            form.format(null, new StringBuffer(), new FieldPosition(0));
            fail("Should throw IAE");
        } catch (IllegalArgumentException e) {
            // expected
        }

        // When StringBuffer == null || FieldPosition == null
        // NullPointerException will be thrown out.
        try {
            form.format(new Double(1.9), null, new FieldPosition(0));
            fail("Should throw NPE");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            form.format(new Double(1.3), new StringBuffer(), null);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            form.format(new Object(), new StringBuffer(), new FieldPosition(0));
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void test_formatObject() {
        DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(Locale.US);

        // format maxLong
        FieldPosition pos = new FieldPosition(0);
        StringBuffer out = format.format(new Long(Long.MAX_VALUE), new StringBuffer(), pos);
        assertTrue("Wrong result L1: " + out, out.toString().equals("9,223,372,036,854,775,807"));

        // format minLong
        pos = new FieldPosition(0);
        out = format.format(new Long(Long.MIN_VALUE), new StringBuffer(), pos);
        assertTrue("Wrong result L2: " + out, out.toString().equals("-9,223,372,036,854,775,808"));

        // format maxLong of type BigInteger
        pos = new FieldPosition(0);
        out = format.format(new java.math.BigInteger(String.valueOf(Long.MAX_VALUE)),
                new StringBuffer(), pos);
        assertTrue("Wrong result BI1: " + out, out.toString().equals("9,223,372,036,854,775,807"));

        // format minLong of type BigInteger
        pos = new FieldPosition(0);
        out = format.format(new java.math.BigInteger(String.valueOf(Long.MIN_VALUE)),
                new StringBuffer(), pos);
        assertTrue("Wrong result BI2: " + out, out.toString().equals("-9,223,372,036,854,775,808"));

        // format maxLong + 1
        java.math.BigInteger big;
        pos = new FieldPosition(0);
        big = new java.math.BigInteger(String.valueOf(Long.MAX_VALUE))
                .add(new java.math.BigInteger("1"));
        out = format.format(big, new StringBuffer(), pos);
        assertTrue("Wrong result BI3: " + out, out.toString().equals("9,223,372,036,854,775,808"));

        // format minLong - 1
        pos = new FieldPosition(0);
        big = new java.math.BigInteger(String.valueOf(Long.MIN_VALUE))
                .add(new java.math.BigInteger("-1"));
        out = format.format(big, new StringBuffer(), pos);
        assertTrue("Wrong result BI4: " + out, out.toString().equals("-9,223,372,036,854,775,809"));

        // format big decimal
        pos = new FieldPosition(0);
        out = format.format(new java.math.BigDecimal("51.348"), new StringBuffer(), pos);
        assertTrue("Wrong result BD1: " + out, out.toString().equals("51.348"));

        // format big decimal
        pos = new FieldPosition(0);
        out = format.format(new java.math.BigDecimal("51"), new StringBuffer(), pos);
        assertTrue("Wrong result BD2: " + out, out.toString().equals("51"));

        // format big decimal Double.MAX_VALUE * 2
        java.math.BigDecimal bigDecimal;
        pos = new FieldPosition(0);
        final String doubleMax2 = "359,538,626,972,463,141,629,054,847,463,408,"
                + "713,596,141,135,051,689,993,197,834,953,606,314,521,560,057,077,"
                + "521,179,117,265,533,756,343,080,917,907,028,764,928,468,642,653,"
                + "778,928,365,536,935,093,407,075,033,972,099,821,153,102,564,152,"
                + "490,980,180,778,657,888,151,737,016,910,267,884,609,166,473,806,"
                + "445,896,331,617,118,664,246,696,549,595,652,408,289,446,337,476,"
                + "354,361,838,599,762,500,808,052,368,249,716,736";
        bigDecimal = new BigDecimal(Double.MAX_VALUE).add(new BigDecimal(Double.MAX_VALUE));
        out = format.format(bigDecimal, new StringBuffer(), pos);
        assertTrue("Wrong result BDmax2: " + out, out.toString().equals(doubleMax2));

        // format big decimal Double.MIN_VALUE + Double.MIN_VALUE
        // and Double.MIN_VALUE - Double.MIN_VALUE
        pos = new FieldPosition(0);

        bigDecimal = new BigDecimal(Double.MIN_VALUE).add(new BigDecimal(Double.MIN_VALUE));
        out = format.format(bigDecimal, new StringBuffer(), pos);

        bigDecimal = new BigDecimal(Float.MAX_VALUE).add(new BigDecimal(Float.MAX_VALUE));
        out = format.format(bigDecimal, new StringBuffer(), pos);
        final String BDFloatMax2 = "680,564,693,277,057,719,623,408,366,969,033,850,880";
        assertTrue("Wrong result BDFloatMax2: " + out, out.toString().equals(BDFloatMax2));
        // format big decimal Float.MIN_VALUE + Float.MIN_VALUE
        // and Float.MIN_VALUE - Float.MIN_VALUE
        bigDecimal = new BigDecimal(Float.MIN_VALUE).add(new BigDecimal(Float.MIN_VALUE));
        out = format.format(bigDecimal, new StringBuffer(), pos);
        final String BDFloatMin2 = "0";

        bigDecimal = new BigDecimal(Float.MIN_VALUE).subtract(new BigDecimal(Float.MIN_VALUE));
        out = format.format(bigDecimal, new StringBuffer(), pos);

        assertTrue("Wrong result BDFloatMax2: " + out, out.toString().equals(BDFloatMin2));
    }

    public void test_equals() {
        DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        DecimalFormat cloned = (DecimalFormat) format.clone();
        cloned.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
        assertEquals(format, cloned);

        Currency c = Currency.getInstance(Locale.US);
        cloned.setCurrency(c);

        assertEquals(format, cloned);
    }

    public void test_getNegativePrefix() {
        DecimalFormat df = new DecimalFormat();
        df.setNegativePrefix("--");
        assertTrue("Incorrect negative prefix", df.getNegativePrefix().equals("--"));
    }

    public void test_getNegativeSuffix() {
        DecimalFormat df = new DecimalFormat();
        df.setNegativeSuffix("&");
        assertTrue("Incorrect negative suffix", df.getNegativeSuffix().equals("&"));
    }

    public void test_getPositivePrefix() {
        DecimalFormat df = new DecimalFormat();
        df.setPositivePrefix("++");
        assertTrue("Incorrect positive prefix", df.getPositivePrefix().equals("++"));
    }

    public void test_getPositiveSuffix() {
        DecimalFormat df = new DecimalFormat();
        df.setPositiveSuffix("%");
        assertTrue("Incorrect positive prefix", df.getPositiveSuffix().equals("%"));
    }

    public void test_setPositivePrefix() throws Exception {
        DecimalFormat format = new DecimalFormat();
        assertEquals("", format.getPositivePrefix());

        format.setPositivePrefix("PosPrf");
        assertEquals("PosPrf", format.getPositivePrefix());
        assertTrue(format.parse("PosPrf123.45").doubleValue() == 123.45);

        format.setPositivePrefix("");
        assertEquals("", format.getPositivePrefix());

        format.setPositivePrefix(null);
        assertNull(format.getPositivePrefix());
    }

    public void test_setPositiveSuffix() throws Exception {
        DecimalFormat format = new DecimalFormat();
        assertEquals("", format.getPositiveSuffix());

        format.setPositiveSuffix("PosSfx");
        assertEquals("PosSfx", format.getPositiveSuffix());
        assertTrue(format.parse("123.45PosSfx").doubleValue() == 123.45);

        format.setPositiveSuffix("");
        assertEquals("", format.getPositiveSuffix());

        format.setPositiveSuffix(null);
        assertNull(format.getPositiveSuffix());
    }

    public void test_setNegativePrefix() throws Exception {
        DecimalFormat format = new DecimalFormat();
        assertEquals("-", format.getNegativePrefix());

        format.setNegativePrefix("NegPrf");
        assertEquals("NegPrf", format.getNegativePrefix());
        assertTrue(format.parse("NegPrf123.45").doubleValue() == -123.45);
        format.setNegativePrefix("");
        assertEquals("", format.getNegativePrefix());

        format.setNegativePrefix(null);
        assertNull(format.getNegativePrefix());
    }

    public void test_setNegativeSuffix() throws Exception {
        DecimalFormat format = new DecimalFormat();
        assertEquals("", format.getNegativeSuffix());

        format.setNegativeSuffix("NegSfx");
        assertEquals("NegSfx", format.getNegativeSuffix());
        assertTrue(format.parse("123.45NegPfx").doubleValue() == 123.45);

        format.setNegativeSuffix("");
        assertEquals("", format.getNegativeSuffix());

        format.setNegativeSuffix(null);
        assertNull(format.getNegativeSuffix());
    }

    public void test_setGroupingUsed() {
        DecimalFormat format = new DecimalFormat();

        StringBuffer buf = new StringBuffer();
        format.setGroupingUsed(false);
        format.format(new Long(1970), buf, new FieldPosition(0));
        assertEquals("1970", buf.toString());
        assertFalse(format.isGroupingUsed());
        format.format(new Long(1970), buf, new FieldPosition(0));
        assertEquals("19701970", buf.toString());
        assertFalse(format.isGroupingUsed());

        format.setGroupingUsed(true);
        format.format(new Long(1970), buf, new FieldPosition(0));
        assertEquals("197019701,970", buf.toString());
        assertTrue(format.isGroupingUsed());
    }

    public void test_isGroupingUsed() {
        assertFalse(new DecimalFormat("####.##").isGroupingUsed());
        assertFalse(new DecimalFormat("######.######").isGroupingUsed());
        assertFalse(new DecimalFormat("000000.000000").isGroupingUsed());
        assertFalse(new DecimalFormat("######.000000").isGroupingUsed());
        assertFalse(new DecimalFormat("000000.######").isGroupingUsed());
        assertFalse(new DecimalFormat(" ###.###").isGroupingUsed());
        assertFalse(new DecimalFormat("$#####.######").isGroupingUsed());
        assertFalse(new DecimalFormat("$$####.######").isGroupingUsed());

        assertTrue(new DecimalFormat("###,####").isGroupingUsed());
    }

    public void testConstructor_noArg() {
        // Test for method java.text.DecimalFormat()
        // the constructor form that specifies a pattern is equal to the form
        // constructed with no pattern and applying that pattern using the
        // applyPattern call
        DecimalFormat format1 = new DecimalFormat();
        format1.applyPattern("'$'1000.0000");
        DecimalFormat format2 = new DecimalFormat();
        format2.applyPattern("'$'1000.0000");
        assertTrue("Constructed format did not match applied format object",
                format2.equals(format1));
        DecimalFormat format3 = new DecimalFormat("'$'1000.0000");
        assertTrue("Constructed format did not match applied format object",
                format3.equals(format1));
        DecimalFormat format4 = new DecimalFormat("'$'8000.0000");
        assertTrue("Constructed format did not match applied format object",
                !format4.equals(format1));
    }

    public void testConstructor_string() {
        // Test for method java.text.DecimalFormat(java.lang.String)
        // the constructor form that specifies a pattern is equal to the form
        // constructed with no pattern and applying that pattern using the
        // applyPattern call
        DecimalFormat format = new DecimalFormat("'$'0000.0000");
        DecimalFormat format1 = new DecimalFormat();
        format1.applyPattern("'$'0000.0000");
        assertTrue("Constructed format did not match applied format object",
                format.equals(format1));

        new DecimalFormat("####.##");
        new DecimalFormat("######.######");
        new DecimalFormat("000000.000000");
        new DecimalFormat("######.000000");
        new DecimalFormat("000000.######");
        new DecimalFormat(" ###.###");
        new DecimalFormat("$#####.######");
        new DecimalFormat("$$####.######");
        new DecimalFormat("%#,##,###,####");
        new DecimalFormat("#,##0.00;(#,##0.00)");

        try {
            new DecimalFormat(null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            new DecimalFormat("%#,##,###,####'");
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            new DecimalFormat("#.##0.00");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testConstructor_stringAndSymbols() {
        // case 1: Try to construct object using correct pattern and format
        // symbols.
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.CANADA);
        DecimalFormat format1 = new DecimalFormat("'$'1000.0000", dfs);
        DecimalFormat format2 = new DecimalFormat();
        format2.applyPattern("'$'1000.0000");
        format2.setDecimalFormatSymbols(dfs);
        assertTrue("Constructed format did not match applied format object",
                format2.equals(format1));
        assertTrue("Constructed format did not match applied format object",
                !format1.equals(
                        new DecimalFormat("'$'1000.0000", new DecimalFormatSymbols(Locale.CHINA))));

        // case 2: Try to construct object using null arguments.
        try {
            new DecimalFormat("'$'1000.0000", (DecimalFormatSymbols) null);
            fail();
        } catch (NullPointerException expected) {
        }
        try {
            new DecimalFormat(null, new DecimalFormatSymbols());
            fail();
        } catch (NullPointerException expected) {
        }
        try {
            new DecimalFormat(null, (DecimalFormatSymbols) null);
            fail();
        } catch (NullPointerException expected) {
        }

        // case 3: Try to construct object using incorrect pattern.
        try {
            new DecimalFormat("$'", new DecimalFormatSymbols());
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void test_applyPattern() {
        DecimalFormat format = new DecimalFormat("#.#");
        assertEquals("Wrong pattern 1", "#0.#", format.toPattern());
        format = new DecimalFormat("#.");
        assertEquals("Wrong pattern 2", "#0.", format.toPattern());
        format = new DecimalFormat("#");
        assertEquals("Wrong pattern 3", "#", format.toPattern());
        format = new DecimalFormat(".#");
        assertEquals("Wrong pattern 4", "#.0", format.toPattern());

        // Regression for HARMONY-6485
        format = new DecimalFormat();
        format.setMinimumIntegerDigits(0);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(0);
        format.applyPattern("00.0#");
        assertEquals("Minimum integer digits not set", 2, format.getMinimumIntegerDigits());
        assertEquals("Minimum fraction digits not set", 1, format.getMinimumFractionDigits());
        assertEquals("Maximum fraction digits not set", 2, format.getMaximumFractionDigits());

        try {
            format.applyPattern(null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            format.applyPattern("%#,##,###,####'");
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            format.applyPattern("#.##0.00");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    // AndroidOnly: icu supports 2 grouping sizes
    public void test_applyPattern_icu2GroupingSizes() {
        DecimalFormat decFormat = new DecimalFormat("#.#");
        String[] patterns = {
                "####.##", "######.######", "000000.000000",
                "######.000000", "000000.######", " ###.###", "$#####.######",
                "$$####.######", "%#,##,###,####", "#,##0.00;(#,##0.00)",
                "##.##-E"
        };

        String[] expResult = {
                "#0.##", "#0.######", "#000000.000000",
                "#.000000", "#000000.######", " #0.###", "$#0.######",
                "$$#0.######",
                "%#,###,####", // icu only. icu supports two grouping sizes
                "#,##0.00;(#,##0.00)",
                "#0.##-E"
                // icu only. E in the suffix does not need to be quoted.
        };

        for (int i = 0; i < patterns.length; i++) {
            decFormat.applyPattern(patterns[i]);
            String result = decFormat.toPattern();
            assertEquals("Failed to apply following pattern: " + patterns[i] +
                    "\n expected: " + expResult[i] +
                    "\n returned: " + result, expResult[i], result);
        }
    }

    public void test_applyLocalizedPattern() throws Exception {
        DecimalFormat format = new DecimalFormat();

        // case 1: Try to apply correct variants of pattern.
        format.applyLocalizedPattern("#.#");
        assertEquals("Wrong pattern 1", "#0.#", format.toLocalizedPattern());
        format.applyLocalizedPattern("#.");
        assertEquals("Wrong pattern 2", "#0.", format.toLocalizedPattern());
        format.applyLocalizedPattern("#");
        assertEquals("Wrong pattern 3", "#", format.toLocalizedPattern());
        format.applyLocalizedPattern(".#");
        assertEquals("Wrong pattern 4", "#.0", format.toLocalizedPattern());

        // case 2: Try to apply malformed patten.
        try {
            format.applyLocalizedPattern("'#,#:#0.0#;(#)");
            fail();
        } catch (IllegalArgumentException expected) {
        }

        // case 3: Try to apply null pattern.
        try {
            format.applyLocalizedPattern((String) null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void test_toPattern() {
        DecimalFormat format = new DecimalFormat();
        format.applyPattern("#.#");
        assertEquals("Wrong pattern 1", "#0.#", format.toPattern());
        format.applyPattern("#.");
        assertEquals("Wrong pattern 2", "#0.", format.toPattern());
        format.applyPattern("#");
        assertEquals("Wrong pattern 3", "#", format.toPattern());
        format.applyPattern(".#");
        assertEquals("Wrong pattern 4", "#.0", format.toPattern());
    }

    public void test_toLocalizedPattern() {
        DecimalFormat format = new DecimalFormat();
        format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
        format.applyLocalizedPattern("#.#");
        assertEquals("Wrong pattern 1", "#0.#", format.toLocalizedPattern());
        format.applyLocalizedPattern("#.");
        assertEquals("Wrong pattern 2", "#0.", format.toLocalizedPattern());
        format.applyLocalizedPattern("#");
        assertEquals("Wrong pattern 3", "#", format.toLocalizedPattern());
        format.applyLocalizedPattern(".#");
        assertEquals("Wrong pattern 4", "#.0", format.toLocalizedPattern());
    }

    public void test_hashCode() {
        DecimalFormat df1 = new DecimalFormat();
        DecimalFormat df2 = (DecimalFormat) df1.clone();
        assertTrue("Hash codes of equals object are not equal", df2.hashCode() == df1.hashCode());
    }

    public void test_clone() {
        DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        DecimalFormat cloned = (DecimalFormat) format.clone();
        assertEquals(cloned.getDecimalFormatSymbols(), format.getDecimalFormatSymbols());

        format = new DecimalFormat("'$'0000.0000");
        DecimalFormat format1 = (DecimalFormat) (format.clone());
        // make sure the objects are equal
        assertTrue("Object's clone isn't equal!", format.equals(format1));
        // change the content of the clone and make sure it's not equal anymore
        // verifies that it's data is now distinct from the original
        format1.applyPattern("'$'0000.####");
        assertTrue("Object's changed clone should not be equal!", !format.equals(format1));
    }

    public void test_formatDouble_maximumFractionDigits() {
        DecimalFormat df = new DecimalFormat("###0.##", new DecimalFormatSymbols(Locale.US));
        df.setMaximumFractionDigits(3);
        assertEquals(3, df.getMaximumFractionDigits());
        assertEquals("1.235", df.format(1.23456));
        df.setMinimumFractionDigits(4);
        assertEquals(4, df.getMaximumFractionDigits());
        assertEquals("456.0000", df.format(456));

        df = new DecimalFormat("##0.#");
        df.setMaximumFractionDigits(30);
        assertEquals("0", df.format(0.0));
        assertEquals("-0", df.format(-0.0));
        assertEquals("1", df.format(1.0));
        assertEquals("-1", df.format(-1.0));
    }

    public void test_formatDouble_minimumFractionDigits() {
        DecimalFormat df = new DecimalFormat("###0.##", new DecimalFormatSymbols(Locale.US));
        df.setMinimumFractionDigits(4);
        assertEquals(4, df.getMinimumFractionDigits());
        assertEquals("1.2300", df.format(1.23));
        df.setMaximumFractionDigits(2);
        assertEquals(2, df.getMinimumFractionDigits());
        assertEquals("456.00", df.format(456));

        df = new DecimalFormat("##0.#", new DecimalFormatSymbols(Locale.US));
        df.setMinimumFractionDigits(30);
        assertEquals("0.000000000000000000000000000000", df.format(0.0));
        assertEquals("-0.000000000000000000000000000000", df.format(-0.0));
        assertEquals("1.000000000000000000000000000000", df.format(1.0));
        assertEquals("-1.000000000000000000000000000000", df.format(-1.0));
    }

    public void test_formatDouble_withFieldPosition() {
        new Support_DecimalFormat(
                "test_formatDLjava_lang_StringBufferLjava_text_FieldPosition")
                .t_format_with_FieldPosition();
    }

    // This test serves as a regression test for Android's behavior.
    // There are many patterns that produce different output from the RI but are sometimes the
    // consequence of Android following the ICU DecimalFormat rules.
    /* J2ObjC: Our DecimalFormat implementation is compatible with the RI, not Android
    public void test_formatDouble_scientificNotation() {
        FormatTester formatTester = new FormatTester();
        final DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.US);

        DecimalFormat df = new DecimalFormat("00.0#E0", dfs);
        // ["00.0#E0",isDecimalSeparatorAlwaysShown=false,groupingSize=0,multiplier=1,
        // negativePrefix=-,negativeSuffix=,positivePrefix=,positiveSuffix=,maxIntegerDigits=2,
        // maxFractionDigits=2,minIntegerDigits=2,minFractionDigits=1,grouping=false]
        // Because maximum integer digit was not explicitly set: The exponent can be any integer.
        // Scientific notation => use significant digit logic
        // '@' not present: Significant digits: Min: 1,
        // Max: "min integer digits" (2) + "max fractional digits (2) == 4
        formatTester.format(df, "00.0E0", 0.0);
        formatTester.format(df, "10.0E-1", 1.0);
        formatTester.format(df, "12.0E0", 12.0);
        formatTester.format(df, "12.3E1", 123.0);
        formatTester.format(df, "12.34E2", 1234.0);
        formatTester.format(df, "12.35E3", 12346.0);
        formatTester.format(df, "10.0E4", 99999.0);
        formatTester.format(df, "12.0E-1", 1.2);
        formatTester.format(df, "12.3E0", 12.3);
        formatTester.format(df, "12.34E1", 123.4);
        formatTester.format(df, "12.35E2", 1234.6);
        formatTester.format(df, "10.0E3", 9999.9);
        formatTester.format(df, "10.0E-2", 0.1);
        formatTester.format(df, "12.0E-2", 0.12);
        formatTester.format(df, "12.3E-2", 0.123);
        formatTester.format(df, "12.34E-2", 0.1234);
        formatTester.format(df, "12.35E-2", 0.12346);
        formatTester.format(df, "10.0E-1", 0.99999);
        formatTester.format(df, "-10.0E-1", -1.0);
        formatTester.format(df, "-12.0E0", -12.0);
        formatTester.format(df, "-12.3E1", -123.0);
        formatTester.format(df, "-12.34E2", -1234.0);
        formatTester.format(df, "-12.35E3", -12346.0);
        formatTester.format(df, "-10.0E4", -99999.0);

        df = new DecimalFormat("#00.0##E0", dfs);
        // ["#00.0##E0",isDecimalSeparatorAlwaysShown=false,groupingSize=0,multiplier=1,
        // negativePrefix=-,negativeSuffix=,positivePrefix=,positiveSuffix=,maxIntegerDigits=3,
        // maxFractionDigits=3,minIntegerDigits=2,minFractionDigits=1,grouping=false]
        // Because maximum integer digit count is set: The exponent must be a multiple of it (3).
        // Scientific notation => use significant digit logic
        // '@' not present: Significant digits: Min: 1,
        // Max: "min integer digits" (2) + "max fractional digits (3) == 5
        formatTester.format(df, "100E-3", 0.1);
        formatTester.format(df, "120E-3", 0.12);
        formatTester.format(df, "123E-3", 0.123);
        formatTester.format(df, "123.4E-3", 0.1234);
        formatTester.format(df, "123.46E-3", 0.1234567);
        formatTester.format(df, "10E-3", 0.01);
        formatTester.format(df, "12E-3", 0.012);
        formatTester.format(df, "12.3E-3", 0.0123);
        formatTester.format(df, "12.34E-3", 0.01234);
        formatTester.format(df, "12.346E-3", 0.01234567);
        formatTester.format(df, "1.0E-3", 0.001);
        formatTester.format(df, "1.2E-3", 0.0012);
        formatTester.format(df, "1.23E-3", 0.00123);
        formatTester.format(df, "1.234E-3", 0.001234);
        formatTester.format(df, "1.2346E-3", 0.001234567);
        formatTester.format(df, "100E-6", 0.0001);
        formatTester.format(df, "120E-6", 0.00012);
        formatTester.format(df, "123E-6", 0.000123);
        formatTester.format(df, "123.4E-6", 0.0001234);
        formatTester.format(df, "123.46E-6", 0.0001234567);
        formatTester.format(df, "0.0E0", 0.0);
        formatTester.format(df, "1.0E0", 1.0);
        formatTester.format(df, "12E0", 12.0);
        formatTester.format(df, "123E0", 123.0);
        formatTester.format(df, "1.234E3", 1234.0);
        formatTester.format(df, "12.345E3", 12345.0);
        formatTester.format(df, "123.46E3", 123456.0);
        formatTester.format(df, "1.2346E6", 1234567.0);
        formatTester.format(df, "12.346E6", 12345678.0);
        formatTester.format(df, "100E6", 99999999.0);

        df = new DecimalFormat("#.0E0", dfs);
        // ["#.0E0",isDecimalSeparatorAlwaysShown=false,groupingSize=0,multiplier=1,
        // negativePrefix=-,negativeSuffix=,positivePrefix=,positiveSuffix=,maxIntegerDigits=1,
        // maxFractionDigits=1,minIntegerDigits=0,minFractionDigits=1,grouping=false]
        // Because maximum integer digit count is set: The exponent must be a multiple of it (1).
        // Scientific notation => use significant digit logic
        // '@' not present: Significant digits: Min: 1,
        // Max: "min integer digits" (0) + "max fractional digits (1) == 1
        formatTester.format(df, "0.0E0", 0.0);
        formatTester.format(df, "1.0E0", 1.0);
        formatTester.format(df, "1.0E1", 12.0);
        formatTester.format(df, "1.0E2", 123.0);
        formatTester.format(df, "1.0E3", 1234.0);
        formatTester.format(df, "1.0E4", 9999.0);

        df = new DecimalFormat("0.E0", dfs);
        // ["0.E0",isDecimalSeparatorAlwaysShown=true,groupingSize=0,multiplier=1,negativePrefix=-,
        // negativeSuffix=,positivePrefix=,positiveSuffix=,maxIntegerDigits=1,maxFractionDigits=0,
        // minIntegerDigits=1,minFractionDigits=0,grouping=false]
        // Because maximum integer digit was not explicitly set: The exponent can be any integer.
        // Scientific notation => use significant digit logic
        // '@' not present: Significant digits: Min: 1,
        // Max: "min integer digits" (1) + "max fractional digits (0) == 1
        formatTester.format(df, "0E0", 0.0);
        formatTester.format(df, "1E0", 1.0);
        formatTester.format(df, "1E1", 12.0);
        formatTester.format(df, "1E2", 123.0);
        formatTester.format(df, "1E3", 1234.0);
        formatTester.format(df, "1E4", 9999.0);

        df = new DecimalFormat("##0.00#E0", dfs);
        // ["##0.00#E0",isDecimalSeparatorAlwaysShown=false,groupingSize=0,multiplier=1,
        // negativePrefix=-,negativeSuffix=,positivePrefix=,positiveSuffix=,maxIntegerDigits=3,
        // maxFractionDigits=3,minIntegerDigits=1,minFractionDigits=2,grouping=false]
        // Because maximum integer digit count is set: The exponent must be a multiple of it (3).
        // Scientific notation => use significant digit logic
        // '@' not present: Significant digits: Min: 1,
        // Max: "min integer digits" (1) + "max fractional digits (3) == 4
        formatTester.format(df, "100E-3", 0.1);
        formatTester.format(df, "123.5E-3", 0.1234567);
        formatTester.format(df, "1.00E0", 0.9999999);
        formatTester.format(df, "10.0E-3", 0.01);
        formatTester.format(df, "12.35E-3", 0.01234567);
        formatTester.format(df, "100E-3", 0.09999999);
        formatTester.format(df, "1.00E-3", 0.001);
        formatTester.format(df, "1.235E-3", 0.001234567);
        formatTester.format(df, "10.0E-3", 0.009999999);
        formatTester.format(df, "100E-6", 0.0001);
        formatTester.format(df, "123.5E-6", 0.0001234567);
        formatTester.format(df, "1.00E-3", 0.0009999999);

        df = new DecimalFormat("###0.00#E0", dfs);
        // ["###0.00#E0",isDecimalSeparatorAlwaysShown=false,groupingSize=0,multiplier=1,
        // negativePrefix=-,negativeSuffix=,positivePrefix=,positiveSuffix=,maxIntegerDigits=4,
        // maxFractionDigits=3,minIntegerDigits=1,minFractionDigits=2,grouping=false]
        // Because maximum integer digit count is set: The exponent must be a multiple of it (4).
        // Scientific notation => use significant digit logic
        // '@' not present: Significant digits: Min: 1,
        // Max: "min integer digits" (1) + "max fractional digits (3) == 4
        formatTester.format(df, "1000E-4", 0.1);
        formatTester.format(df, "1235E-4", 0.12345678);
        formatTester.format(df, "1.00E0", 0.99999999);
        formatTester.format(df, "100E-4", 0.01);
        formatTester.format(df, "123.5E-4", 0.012345678);
        formatTester.format(df, "1000E-4", 0.099999999);
        formatTester.format(df, "10.0E-4", 0.001);
        formatTester.format(df, "12.35E-4", 0.0012345678);
        formatTester.format(df, "100E-4", 0.0099999999);
        formatTester.format(df, "1.00E-4", 0.0001);
        formatTester.format(df, "1.235E-4", 0.00012345678);
        formatTester.format(df, "10.0E-4", 0.00099999999);
        formatTester.format(df, "1000E-8", 0.00001);
        formatTester.format(df, "1235E-8", 0.000012345678);
        formatTester.format(df, "1.00E-4", 0.000099999999);

        df = new DecimalFormat("###0.0#E0", dfs);
        // ["###0.0#E0",isDecimalSeparatorAlwaysShown=false,groupingSize=0,multiplier=1,
        // negativePrefix=-,negativeSuffix=,positivePrefix=,positiveSuffix=,maxIntegerDigits=4,
        // maxFractionDigits=2,minIntegerDigits=1,minFractionDigits=1,grouping=false]
        // Because maximum integer digit count is set: The exponent must be a multiple of it (4).
        // Scientific notation => use significant digit logic
        // '@' not present: Significant digits: Min: 1,
        // Max: "min integer digits" (1) + "max fractional digits (2) == 3
        formatTester.format(df, "1000E-4", 0.1);
        formatTester.format(df, "1230E-4", 0.1234567);
        formatTester.format(df, "1.0E0", 0.9999999);
        formatTester.format(df, "100E-4", 0.01);
        formatTester.format(df, "123E-4", 0.01234567);
        formatTester.format(df, "1000E-4", 0.09999999);
        formatTester.format(df, "10E-4", 0.001);
        formatTester.format(df, "12.3E-4", 0.001234567);
        formatTester.format(df, "100E-4", 0.009999999);
        formatTester.format(df, "1.0E-4", 0.0001);
        formatTester.format(df, "1.23E-4", 0.0001234567);
        formatTester.format(df, "10E-4", 0.0009999999);
        formatTester.format(df, "1000E-8", 0.00001);
        formatTester.format(df, "1230E-8", 0.00001234567);
        formatTester.format(df, "1.0E-4", 0.00009999999);

        df = new DecimalFormat("##0.0E0", dfs);
        // ["##0.0E0",isDecimalSeparatorAlwaysShown=false,groupingSize=0,multiplier=1,
        // negativePrefix=-,negativeSuffix=,positivePrefix=,positiveSuffix=,maxIntegerDigits=3,
        // maxFractionDigits=1,minIntegerDigits=1,minFractionDigits=1,grouping=false]
        // Because maximum integer digit count is set: The exponent must be a multiple of it (3).
        // Scientific notation => use significant digit logic
        // '@' not present: Significant digits: Min: 1,
        // Max: "min integer digits" (1) + "max fractional digits (1) == 2
        formatTester.format(df, "0.0E0", 0.0);
        formatTester.format(df, "1.0E0", 1.0);
        formatTester.format(df, "12E0", 12.0);
        formatTester.format(df, "120E0", 123.0);
        formatTester.format(df, "1.2E3", 1234.0);
        formatTester.format(df, "12E3", 12346.0);
        formatTester.format(df, "100E3", 99999.0);
        formatTester.format(df, "1.0E6", 999999.0);

        df = new DecimalFormat("0.#E0", dfs);
        // ["0.#E0",isDecimalSeparatorAlwaysShown=false,groupingSize=0,multiplier=1,
        // negativePrefix=-,negativeSuffix=,positivePrefix=,positiveSuffix=,maxIntegerDigits=1,
        // maxFractionDigits=1,minIntegerDigits=1,minFractionDigits=0,grouping=false]
        // Because maximum integer digit was not explicitly set: The exponent can be any integer.
        // Scientific notation => use significant digit logic
        // '@' not present: Significant digits: Min: 1,
        // Max: "min integer digits" (1) + "max fractional digits (1) == 2
        formatTester.format(df, "0E0", 0.0);
        formatTester.format(df, "1E0", 1.0);
        formatTester.format(df, "1.2E1", 12.0);
        formatTester.format(df, "1.2E2", 123.0);
        formatTester.format(df, "1.2E3", 1234.0);
        formatTester.format(df, "1E4", 9999.0);

        df = new DecimalFormat(".0E0", dfs);
        // [".0E0",isDecimalSeparatorAlwaysShown=true,groupingSize=0,multiplier=1,negativePrefix=-,
        // negativeSuffix=,positivePrefix=,positiveSuffix=,maxIntegerDigits=0,maxFractionDigits=1,
        // minIntegerDigits=0,minFractionDigits=1,grouping=false]
        // Because maximum integer digit was not explicitly set: The exponent can be any integer.
        // Scientific notation => use significant digit logic
        // '@' not present: Significant digits: Min: 1,
        // Max: "min integer digits" (0) + "max fractional digits (1) == 2
        formatTester.format(df, ".0E0", 0.0);
        formatTester.format(df, ".1E1", 1.0);
        formatTester.format(df, ".1E2", 12.0);
        formatTester.format(df, ".1E3", 123.0);
        formatTester.format(df, ".1E4", 1234.0);
        formatTester.format(df, ".1E5", 9999.0);

        formatTester.throwFailures();
    }

    public void test_formatDouble_scientificNotationMinusZero() throws Exception {
        FormatTester formatTester = new FormatTester();
        final DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.US);

        DecimalFormat df = new DecimalFormat("00.0#E0", dfs);
        // ["00.0#E0",isDecimalSeparatorAlwaysShown=false,groupingSize=0,multiplier=1,
        // negativePrefix=-,negativeSuffix=,positivePrefix=,positiveSuffix=,maxIntegerDigits=2,
        // maxFractionDigits=2,minIntegerDigits=2,minFractionDigits=1,grouping=false]
        // Because maximum integer digit was not explicitly set: The exponent can be any integer.
        // Scientific notation => use significant digit logic
        // '@' not present: Significant digits: Min: 1,
        // Max: "min integer digits" (2) + "max fractional digits (2) == 4
        formatTester.format(df, "-00.0E0", -0.0);

        df = new DecimalFormat("##0.0E0", dfs);
        // ["##0.0E0",isDecimalSeparatorAlwaysShown=false,groupingSize=0,multiplier=1,
        // negativePrefix=-,negativeSuffix=,positivePrefix=,positiveSuffix=,maxIntegerDigits=3,
        // maxFractionDigits=1,minIntegerDigits=1,minFractionDigits=1,grouping=false]
        // Because maximum integer digit count is set: The exponent must be a multiple of it (3).
        // Scientific notation => use significant digit logic
        // '@' not present: Significant digits: Min: 1,
        // Max: "min integer digits" (1) + "max fractional digits (1) == 2
        formatTester.format(df, "-0.0E0", -0.0);

        df = new DecimalFormat("#.0E0", dfs);
        // ["#.0E0",isDecimalSeparatorAlwaysShown=false,groupingSize=0,multiplier=1,
        // negativePrefix=-,negativeSuffix=,positivePrefix=,positiveSuffix=,maxIntegerDigits=1,
        // maxFractionDigits=1,minIntegerDigits=0,minFractionDigits=1,grouping=false]
        // Because maximum integer digit count is set: The exponent must be a multiple of it (1).
        // Scientific notation => use significant digit logic
        // '@' not present: Significant digits: Min: 1,
        // Max: "min integer digits" (0) + "max fractional digits (1) == 2
        formatTester.format(df, "-0.0E0", -0.0);

        df = new DecimalFormat("0.#E0", dfs);
        // ["0.#E0",isDecimalSeparatorAlwaysShown=false,groupingSize=0,multiplier=1,
        // negativePrefix=-,negativeSuffix=,positivePrefix=,positiveSuffix=,maxIntegerDigits=1,
        // maxFractionDigits=1,minIntegerDigits=1,minFractionDigits=0,grouping=false]
        // Because maximum integer digit was not explicitly set: The exponent can be any integer.
        // Scientific notation => use significant digit logic
        // '@' not present: Significant digits: Min: 1,
        // Max: "min integer digits" (1) + "max fractional digits (1) == 2
        formatTester.format(df, "-0E0", -0.0);

        df = new DecimalFormat(".0E0", dfs);
        // [".0E0",isDecimalSeparatorAlwaysShown=true,groupingSize=0,multiplier=1,negativePrefix=-,
        // negativeSuffix=,positivePrefix=,positiveSuffix=,maxIntegerDigits=0,maxFractionDigits=1,
        // minIntegerDigits=0,minFractionDigits=1,grouping=false]
        // Because maximum integer digit was not explicitly set: The exponent can be any integer.
        // Scientific notation => use significant digit logic
        // '@' not present: Significant digits: Min: 1,
        // Max: "min integer digits" (0) + "max fractional digits (1) == 1
        formatTester.format(df, "-.0E0", -0.0);

        formatTester.throwFailures();
    }*/

    public void test_formatLong_maximumIntegerDigits() {
        DecimalFormat df = new DecimalFormat("###0.##");
        df.setMaximumIntegerDigits(2);
        assertEquals(2, df.getMaximumIntegerDigits());
        assertEquals("34", df.format(1234));
        df.setMinimumIntegerDigits(4);
        assertEquals(4, df.getMaximumIntegerDigits());
        assertEquals("0026", df.format(26));
    }

    public void test_formatLong_minimumIntegerDigits() {
        DecimalFormat df = new DecimalFormat("###0.##", new DecimalFormatSymbols(Locale.US));
        df.setMinimumIntegerDigits(3);
        assertEquals(3, df.getMinimumIntegerDigits());
        assertEquals("012", df.format(12));
        df.setMaximumIntegerDigits(2);
        assertEquals(2, df.getMinimumIntegerDigits());
        assertEquals("00.7", df.format(0.7));
    }

    // See also the _formatDouble tests. This tests a subset of patterns / values.
    /* J2ObjC: Our DecimalFormat implementation is compatible with the RI, not Android
    public void test_formatLong_scientificNotation() {
        FormatTester formatTester = new FormatTester();
        final DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.US);

        DecimalFormat df = new DecimalFormat("00.0#E0", dfs);
        // ["00.0#E0",isDecimalSeparatorAlwaysShown=false,groupingSize=0,multiplier=1,
        // negativePrefix=-,negativeSuffix=,positivePrefix=,positiveSuffix=,maxIntegerDigits=2,
        // maxFractionDigits=2,minIntegerDigits=2,minFractionDigits=1,grouping=false]
        // Because maximum integer digit was not explicitly set: The exponent can be any integer.
        // Scientific notation => use significant digit logic
        // '@' not present: Significant digits: Min: 1,
        // Max: "min integer digits" (2) + "max fractional digits (2) == 4
        formatTester.format(df, "00.0E0", 0);
        formatTester.format(df, "10.0E-1", 1);
        formatTester.format(df, "12.0E0", 12);
        formatTester.format(df, "12.3E1", 123);
        formatTester.format(df, "12.34E2", 1234);
        formatTester.format(df, "12.35E3", 12346);
        formatTester.format(df, "10.0E4", 99999);
        formatTester.format(df, "-10.0E-1", -1);
        formatTester.format(df, "-12.0E0", -12);
        formatTester.format(df, "-12.3E1", -123);
        formatTester.format(df, "-12.34E2", -1234);
        formatTester.format(df, "-12.35E3", -12346);
        formatTester.format(df, "-10.0E4", -99999);

        df = new DecimalFormat("##0.0E0", dfs);
        // ["##0.0E0",isDecimalSeparatorAlwaysShown=false,groupingSize=0,multiplier=1,
        // negativePrefix=-,negativeSuffix=,positivePrefix=,positiveSuffix=,maxIntegerDigits=3,
        // maxFractionDigits=1,minIntegerDigits=1,minFractionDigits=1,grouping=false]
        // Because maximum integer digit count is set: The exponent must be a multiple of it (3).
        // Scientific notation => use significant digit logic
        // '@' not present: Significant digits: Min: 1,
        // Max: "min integer digits" (1) + "max fractional digits (1) == 2
        formatTester.format(df, "0.0E0", 0);
        formatTester.format(df, "1.0E0", 1);
        formatTester.format(df, "12E0", 12);
        formatTester.format(df, "120E0", 123);
        formatTester.format(df, "1.2E3", 1234);
        formatTester.format(df, "12E3", 12346);
        formatTester.format(df, "100E3", 99999);
        formatTester.format(df, "1.0E6", 999999);

        df = new DecimalFormat("#00.0##E0", dfs);
        // ["##0.0E0",isDecimalSeparatorAlwaysShown=false,groupingSize=0,multiplier=1,
        // negativePrefix=-,negativeSuffix=,positivePrefix=,positiveSuffix=,maxIntegerDigits=3,
        // maxFractionDigits=1,minIntegerDigits=1,minFractionDigits=1,grouping=false]
        // Because maximum integer digit count is set: The exponent must be a multiple of it (3).
        // Scientific notation => use significant digit logic
        // '@' not present: Significant digits: Min: 1,
        // Max: "min integer digits" (2) + "max fractional digits (3) == 5
        formatTester.format(df, "0.0E0", 0);
        formatTester.format(df, "1.0E0", 1);
        formatTester.format(df, "12E0", 12);
        formatTester.format(df, "123E0", 123);
        formatTester.format(df, "1.234E3", 1234);
        formatTester.format(df, "12.345E3", 12345);
        formatTester.format(df, "123.46E3", 123456);
        formatTester.format(df, "1.2346E6", 1234567);
        formatTester.format(df, "12.346E6", 12345678);
        formatTester.format(df, "100E6", 99999999);

        df = new DecimalFormat("#.0E0", dfs);
        // ["#.0E0",isDecimalSeparatorAlwaysShown=false,groupingSize=0,multiplier=1,
        // negativePrefix=-,negativeSuffix=,positivePrefix=,positiveSuffix=,maxIntegerDigits=1,
        // maxFractionDigits=1,minIntegerDigits=0,minFractionDigits=1,grouping=false]
        // Because maximum integer digit count is set: The exponent must be a multiple of it (1).
        // Scientific notation => use significant digit logic
        // '@' not present: Significant digits: Min: 1,
        // Max: "min integer digits" (0) + "max fractional digits (1) == 1
        formatTester.format(df, "0.0E0", 0);
        formatTester.format(df, "1.0E0", 1);
        formatTester.format(df, "1.0E1", 12);
        formatTester.format(df, "1.0E2", 123);
        formatTester.format(df, "1.0E3", 1234);
        formatTester.format(df, "1.0E4", 9999);

        df = new DecimalFormat("0.#E0", dfs);
        // ["0.#E0",isDecimalSeparatorAlwaysShown=false,groupingSize=0,multiplier=1,
        // negativePrefix=-,negativeSuffix=,positivePrefix=,positiveSuffix=,maxIntegerDigits=1,
        // maxFractionDigits=1,minIntegerDigits=1,minFractionDigits=0,grouping=false]
        // Because maximum integer digit was not explicitly set: The exponent can be any integer.
        // Scientific notation => use significant digit logic
        // '@' not present: Significant digits: Min: 1,
        // Max: "min integer digits" (1) + "max fractional digits (1) == 2
        formatTester.format(df, "0E0", 0);
        formatTester.format(df, "1E0", 1);
        formatTester.format(df, "1.2E1", 12);
        formatTester.format(df, "1.2E2", 123);
        formatTester.format(df, "1.2E3", 1234);
        formatTester.format(df, "1E4", 9999);

        df = new DecimalFormat(".0E0", dfs);
        // [".0E0",isDecimalSeparatorAlwaysShown=true,groupingSize=0,multiplier=1,negativePrefix=-,
        // negativeSuffix=,positivePrefix=,positiveSuffix=,maxIntegerDigits=0,maxFractionDigits=1,
        // minIntegerDigits=0,minFractionDigits=1,grouping=false]
        // Because maximum integer digit was not explicitly set: The exponent can be any integer.
        // Scientific notation => use significant digit logic
        // '@' not present: Significant digits: Min: 1,
        // Max: "min integer digits" (0) + "max fractional digits (1) == 1
        formatTester.format(df, ".0E0", 0);
        formatTester.format(df, ".1E1", 1);
        formatTester.format(df, ".1E2", 12);
        formatTester.format(df, ".1E3", 123);
        formatTester.format(df, ".1E4", 1234);
        formatTester.format(df, ".1E5", 9999);

        formatTester.throwFailures();
    }*/

    // Demonstrates that fraction digit rounding occurs as expected with 1, 10 and 14 fraction
    // digits, using numbers that are well within the precision of IEEE 754.
    public void test_formatDouble_maxFractionDigits() {
        final DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.US);
        DecimalFormat format = new DecimalFormat("#0.#", dfs);
        format.setGroupingUsed(false);
        format.setMaximumIntegerDigits(400);
        format.setMaximumFractionDigits(1);

        assertEquals("1", format.format(0.99));
        assertEquals("1", format.format(0.95));
        assertEquals("0.9", format.format(0.94));
        assertEquals("0.9", format.format(0.90));

        assertEquals("0.2", format.format(0.19));
        assertEquals("0.2", format.format(0.15));
        assertEquals("0.1", format.format(0.14));
        assertEquals("0.1", format.format(0.10));

        format.setMaximumFractionDigits(10);
        assertEquals("1", format.format(0.99999999999));
        assertEquals("1", format.format(0.99999999995));
        assertEquals("0.9999999999", format.format(0.99999999994));
        assertEquals("0.9999999999", format.format(0.99999999990));

        assertEquals("0.1111111112", format.format(0.11111111119));
        assertEquals("0.1111111112", format.format(0.11111111115));
        assertEquals("0.1111111111", format.format(0.11111111114));
        assertEquals("0.1111111111", format.format(0.11111111110));

        format.setMaximumFractionDigits(14);
        assertEquals("1", format.format(0.999999999999999));
        assertEquals("1", format.format(0.999999999999995));
        assertEquals("0.99999999999999", format.format(0.999999999999994));
        assertEquals("0.99999999999999", format.format(0.999999999999990));

        assertEquals("0.11111111111112", format.format(0.111111111111119));
        assertEquals("0.11111111111112", format.format(0.111111111111115));
        assertEquals("0.11111111111111", format.format(0.111111111111114));
        assertEquals("0.11111111111111", format.format(0.111111111111110));
    }

    // This demonstrates rounding at the 15th decimal digit. By setting the maximum fraction digits
    // we force rounding at a point just below the full IEEE 754 precision. IEEE 754 should be
    // precise to just above 15 decimal digits.
    // df.format() with no limits always emits up to 16 decimal digits, slightly above what IEEE 754
    // can store precisely.
    public void test_formatDouble_roundingTo15Digits() throws Exception {
        final DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("#.#", dfs);
        df.setMaximumIntegerDigits(400);
        df.setGroupingUsed(false);

        df.setMaximumFractionDigits(0);
        assertEquals("1000000000000000", df.format(999999999999999.9));
        df.setMaximumFractionDigits(1);
        assertEquals("100000000000000", df.format(99999999999999.99));
        df.setMaximumFractionDigits(2);
        assertEquals("10000000000000", df.format(9999999999999.999));
        df.setMaximumFractionDigits(3);
        assertEquals("1000000000000", df.format(999999999999.9999));
        df.setMaximumFractionDigits(4);
        assertEquals("100000000000", df.format(99999999999.99999));
        df.setMaximumFractionDigits(5);
        assertEquals("10000000000", df.format(9999999999.999999));
        df.setMaximumFractionDigits(6);
        assertEquals("1000000000", df.format(999999999.9999999));
        df.setMaximumFractionDigits(7);
        assertEquals("100000000", df.format(99999999.99999999));
        df.setMaximumFractionDigits(8);
        assertEquals("10000000", df.format(9999999.999999999));
        df.setMaximumFractionDigits(9);
        assertEquals("1000000", df.format(999999.9999999999));
        df.setMaximumFractionDigits(10);
        assertEquals("100000", df.format(99999.99999999999));
        df.setMaximumFractionDigits(11);
        assertEquals("10000", df.format(9999.999999999999));
        df.setMaximumFractionDigits(12);
        assertEquals("1000", df.format(999.9999999999999));
        df.setMaximumFractionDigits(13);
        assertEquals("100", df.format(99.99999999999999));
        df.setMaximumFractionDigits(14);
        assertEquals("10", df.format(9.999999999999999));
        df.setMaximumFractionDigits(15);
        assertEquals("1", df.format(0.9999999999999999));
    }

    // This checks formatting over most of the representable IEEE 754 range using a formatter that
    // should be performing no lossy rounding.
    // It checks that the formatted double can be parsed to get the original double.
    // IEEE 754 can represent values from (decimal) ~2.22507E−308 to ~1.79769E308 to full precision.
    // Close to zero it can go down to 4.94066E-324 with a loss of precision.
    // At the extremes of the double range this test will not be testing doubles that exactly
    // represent powers of 10. The test is only interested in whether the doubles closest
    // to powers of 10 that can be represented can each be turned into a string and read back again
    // to arrive at the original double.
    public void test_formatDouble_wideRange() throws Exception {
        for (int i = -324; i < 309; i++) {
            // Generate a decimal number we are interested in: 1 * 10^i
            String stringForm = "1e" + i;
            BigDecimal bigDecimal = new BigDecimal(stringForm);

            // Obtain the nearest double representation of the decimal number.
            // This is lossy because going from BigDecimal -> double is inexact, but we should
            // arrive at a number that is as close as possible to the decimal value. We assume that
            // BigDecimal is capable of this, but it is not critical to this test if it gets it a
            // little wrong, though it may mean we are testing a double value different from the one
            // we thought we were.
            double d = bigDecimal.doubleValue();

            assertDecimalFormatIsLossless(d);
        }
    }

    // This test is a regression test for http://b/17656132.
    // It checks hand-picked values can be formatted and parsed to get the original double.
    // The formatter as configured should perform no rounding.
    public void test_formatDouble_roundingProblemCases() throws Exception {
        // Most of the double literals below are not exactly representable in IEEE 754 but
        // it should not matter to this test.
        assertDecimalFormatIsLossless(999999999999999.9);
        assertDecimalFormatIsLossless(99999999999999.99);
        assertDecimalFormatIsLossless(9999999999999.999);
        assertDecimalFormatIsLossless(999999999999.9999);
        assertDecimalFormatIsLossless(99999999999.99999);
        assertDecimalFormatIsLossless(9999999999.999999);
        assertDecimalFormatIsLossless(999999999.9999999);
        assertDecimalFormatIsLossless(99999999.99999999);
        assertDecimalFormatIsLossless(9999999.999999999);
        assertDecimalFormatIsLossless(999999.9999999999);
        assertDecimalFormatIsLossless(99999.99999999999);
        assertDecimalFormatIsLossless(9999.999999999999);
        assertDecimalFormatIsLossless(999.9999999999999);
        assertDecimalFormatIsLossless(99.99999999999999);
        assertDecimalFormatIsLossless(9.999999999999999);
        assertDecimalFormatIsLossless(0.9999999999999999);
    }

    // This test checks hand-picked values can be formatted and parsed to get the original double.
    // The formatter as configured should perform no rounding.
    // These numbers are not affected by http://b/17656132.
    public void test_formatDouble_varyingScale() throws Exception {
        // Most of the double literals below are not exactly representable in IEEE 754 but
        // it should not matter to this test.

        assertDecimalFormatIsLossless(999999999999999.);

        assertDecimalFormatIsLossless(123456789012345.);
        assertDecimalFormatIsLossless(12345678901234.5);
        assertDecimalFormatIsLossless(1234567890123.25);
        assertDecimalFormatIsLossless(999999999999.375);
        assertDecimalFormatIsLossless(99999999999.0625);
        assertDecimalFormatIsLossless(9999999999.03125);
        assertDecimalFormatIsLossless(999999999.015625);
        assertDecimalFormatIsLossless(99999999.0078125);
        assertDecimalFormatIsLossless(9999999.00390625);
        assertDecimalFormatIsLossless(999999.001953125);
        assertDecimalFormatIsLossless(9999.00048828125);
        assertDecimalFormatIsLossless(999.000244140625);
        assertDecimalFormatIsLossless(99.0001220703125);
        assertDecimalFormatIsLossless(9.00006103515625);
        assertDecimalFormatIsLossless(0.000030517578125);
    }

    private static void assertDecimalFormatIsLossless(double d) throws Exception {
        final DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.US);
        DecimalFormat format = new DecimalFormat("#0.#", dfs);
        format.setGroupingUsed(false);
        format.setMaximumIntegerDigits(400);
        format.setMaximumFractionDigits(400);

        // Every floating point binary can be represented exactly in decimal if you have enough
        // digits. This shows the value actually being tested.
        String testId = "decimalValue: " + new BigDecimal(d);

        // As a sanity check we try out parseDouble() with the string generated by
        // Double.toString(). Strictly speaking Double.toString() is probably not guaranteed to be
        // lossless, but in reality it probably is, or at least is close enough.
        assertDoubleEqual(
                testId + " failed parseDouble(toString()) sanity check",
                d, Double.parseDouble(Double.toString(d)));

        // Format the number: If this is lossy it is a problem. We are trying to check that it
        // doesn't lose any unnecessary precision.
        String result = format.format(d);

        // Here we use Double.parseDouble() which should able to parse a number we know was
        // representable as a double into the original double. If parseDouble() is not implemented
        // correctly the test is invalid.
        double doubleParsed = Double.parseDouble(result);
        assertDoubleEqual(testId + " (format() produced " + result + ")",
                d, doubleParsed);

        // For completeness we try to parse using the formatter too. If this fails but the format
        // above didn't it may be a problem with parse(), or with format() that we didn't spot.
        assertDoubleEqual(testId + " failed parse(format()) check",
                d, format.parse(result).doubleValue());
    }

    private static void assertDoubleEqual(String message, double d, double doubleParsed) {
        assertEquals(message,
                createPrintableDouble(d),createPrintableDouble(doubleParsed));
    }

    private static String createPrintableDouble(double d) {
        return Double.toString(d) + "(" + Long.toHexString(Double.doubleToRawLongBits(d)) + ")";
    }

    // Concise demonstration of http://b/17656132 using hard-coded expected values.
    /* J2ObjC: This test is actually a demonstration of a bug.
    public void test_formatDouble_bug17656132() {
        final DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("#0.#", dfs);
        df.setGroupingUsed(false);
        df.setMaximumIntegerDigits(400);
        df.setMaximumFractionDigits(400);

        // The expected values below come from the RI and are correct 16 decimal digit
        // representations of the formatted value. Android does something different.
        // The decimal value given in each comment is the actual double value as represented by
        // IEEE 754. See new BigDecimal(double).

        // double: 999999999999999.9 is decimal 999999999999999.875
        assertEquals("999999999999999.9", df.format(999999999999999.9));
        // double: 99999999999999.98 is decimal 99999999999999.984375
        assertEquals("99999999999999.98", df.format(99999999999999.98));
        // double 9999999999999.998 is decimal 9999999999999.998046875
        assertEquals("9999999999999.998", df.format(9999999999999.998));
        // double 999999999999.9999 is decimal 999999999999.9998779296875
        assertEquals("999999999999.9999", df.format(999999999999.9999));
        // double 99999999999.99998 is decimal 99999999999.9999847412109375
        assertEquals("99999999999.99998", df.format(99999999999.99998));
        // double 9999999999.999998 is decimal 9999999999.9999980926513671875
        assertEquals("9999999999.999998", df.format(9999999999.999998));
        // double 1E23 is decimal 99999999999999991611392
        assertEquals("9999999999999999", df.format(1E23));
    }*/

    public void test_getDecimalFormatSymbols() {
        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
        DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
        assertNotSame(dfs, df.getDecimalFormatSymbols());
    }

    public void test_getCurrency() {
        Currency currK = Currency.getInstance("KRW");
        Currency currX = Currency.getInstance("XXX");
        Currency currE = Currency.getInstance("EUR");

        DecimalFormat df = (DecimalFormat) NumberFormat.getCurrencyInstance(new Locale("ko", "KR"));
        assertTrue("Test1: Returned incorrect currency", df.getCurrency() == currK);

        df = (DecimalFormat) NumberFormat.getCurrencyInstance(new Locale("", "KR"));
        assertTrue("Test2: Returned incorrect currency", df.getCurrency() == currK);

        df = (DecimalFormat) NumberFormat.getCurrencyInstance(new Locale("ko", ""));
        assertTrue("Test3: Returned incorrect currency", df.getCurrency() == currX);

        df = (DecimalFormat) NumberFormat.getCurrencyInstance(new Locale("fr", "FR"));
        assertTrue("Test4: Returned incorrect currency", df.getCurrency() == currE);

        // Regression for HARMONY-1351
        df = (DecimalFormat) NumberFormat.getCurrencyInstance(new Locale("QWERTY"));
        assertTrue("Test5: Returned incorrect currency", df.getCurrency() == currX);

        // JDK fails these tests since it doesn't have the PREEURO variant
        // df = (DecimalFormat)NumberFormat.getCurrencyInstance(new Locale("fr",
        // "FR","PREEURO"));
        // assertTrue("Test5: Returned incorrect currency", df.getCurrency() ==
        // currF);
    }

    public void test_getGroupingSize() {
        DecimalFormat df = new DecimalFormat("###0.##");
        assertEquals("Wrong unset size", 0, df.getGroupingSize());
        df = new DecimalFormat("#,##0.##");
        assertEquals("Wrong set size", 3, df.getGroupingSize());
        df = new DecimalFormat("#,###,###0.##");
        assertEquals("Wrong multiple set size", 4, df.getGroupingSize());
    }

    public void test_getMultiplier() {
        final int defaultMultiplier = 1;
        DecimalFormat form = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        assertEquals(defaultMultiplier, form.getMultiplier());

        DecimalFormat df = new DecimalFormat("###0.##");
        assertEquals("Wrong unset multiplier", 1, df.getMultiplier());
        df = new DecimalFormat("###0.##%");
        assertEquals("Wrong percent multiplier", 100, df.getMultiplier());
        df = new DecimalFormat("###0.##\u2030");
        assertEquals("Wrong mille multiplier", 1000, df.getMultiplier());
    }

    public void test_isDecimalSeparatorAlwaysShown() {
        DecimalFormat df = new DecimalFormat("###0.##");
        assertTrue("Wrong unset value", !df.isDecimalSeparatorAlwaysShown());
        df = new DecimalFormat("###0.00");
        assertTrue("Wrong unset2 value", !df.isDecimalSeparatorAlwaysShown());
        df = new DecimalFormat("###0.");
        assertTrue("Wrong set value", df.isDecimalSeparatorAlwaysShown());
    }

    public void test_parse_withParsePosition() {
        DecimalFormat format = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH);
        ParsePosition pos = new ParsePosition(0);
        Number result = format.parse("9223372036854775807", pos);
        assertTrue("Wrong result type for Long.MAX_VALUE", result.getClass() == Long.class);
        assertTrue("Wrong result Long.MAX_VALUE", result.longValue() == Long.MAX_VALUE);
        pos = new ParsePosition(0);
        result = format.parse("-9223372036854775808", pos);
        assertTrue("Wrong result type for Long.MIN_VALUE", result.getClass() == Long.class);
        assertTrue("Wrong result Long.MIN_VALUE: " + result.longValue(),
                result.longValue() == Long.MIN_VALUE);
        pos = new ParsePosition(0);
        result = format.parse("9223372036854775808", pos);
        assertTrue("Wrong result type for Long.MAX_VALUE+1", result.getClass() == Double.class);
        assertTrue("Wrong result Long.MAX_VALUE + 1",
                result.doubleValue() == (double) Long.MAX_VALUE + 1);
        pos = new ParsePosition(0);
        result = format.parse("-9223372036854775809", pos);
        assertTrue("Wrong result type for Long.MIN_VALUE+1", result.getClass() == Double.class);
        assertTrue("Wrong result Long.MIN_VALUE - 1",
                result.doubleValue() == (double) Long.MIN_VALUE - 1);

        pos = new ParsePosition(0);
        result = format.parse("18446744073709551629", pos);
        assertTrue("Wrong result type for overflow", result.getClass() == Double.class);
        assertTrue("Wrong result for overflow", result.doubleValue() == 18446744073709551629d);

        pos = new ParsePosition(0);
        result = format.parse("42325917317067571199", pos);
        assertTrue("Wrong result type for overflow a: " + result,
                result.getClass() == Double.class);
        assertTrue("Wrong result for overflow a: " + result,
                result.doubleValue() == 42325917317067571199d);
        pos = new ParsePosition(0);
        result = format.parse("4232591731706757119E1", pos);
        assertTrue("Wrong result type for overflow b: " + result,
                result.getClass() == Double.class);
        assertTrue("Wrong result for overflow b: " + result,
                result.doubleValue() == 42325917317067571190d);
        pos = new ParsePosition(0);
        result = format.parse(".42325917317067571199E20", pos);
        assertTrue("Wrong result type for overflow c: " + result,
                result.getClass() == Double.class);
        assertTrue("Wrong result for overflow c: " + result,
                result.doubleValue() == 42325917317067571199d);
        pos = new ParsePosition(0);
        result = format.parse("922337203685477580.9E1", pos);
        assertTrue("Wrong result type for overflow d: " + result,
                result.getClass() == Double.class);
        assertTrue("Wrong result for overflow d: " + result,
                result.doubleValue() == 9223372036854775809d);
        pos = new ParsePosition(0);
        result = format.parse("9.223372036854775809E18", pos);
        assertTrue("Wrong result type for overflow e: " + result,
                result.getClass() == Double.class);
        assertTrue("Wrong result for overflow e: " + result,
                result.doubleValue() == 9223372036854775809d);
    }

    /* J2ObjC: Android behavior differs from RI.
    public void test_parse_withMultiplier() {
        DecimalFormat format = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH);
        Number result;

        format.setMultiplier(100);
        result = format.parse("9223372036854775807", new ParsePosition(0));
        assertEquals("Wrong result type multiplier 100: " + result, Double.class,
                result.getClass());
        assertEquals("Wrong result for multiplier 100: " + result,
                92233720368547758.07d, result.doubleValue());

        format.setMultiplier(1000);
        result = format.parse("9223372036854775807", new ParsePosition(0));
        assertEquals("Wrong result type multiplier 1000: " + result, Double.class,
                result.getClass());
        assertEquals("Wrong result for multiplier 1000: " + result,
                9223372036854775.807d, result.doubleValue());

        format.setMultiplier(10000);
        result = format.parse("9223372036854775807", new ParsePosition(0));
        assertEquals("Wrong result type multiplier 10000: " + result,
                Double.class, result.getClass());
        assertEquals("Wrong result for multiplier 10000: " + result,
                922337203685477.5807d, result.doubleValue());
    }*/

    public void test_setDecimalFormatSymbols() {
        DecimalFormat df = new DecimalFormat("###0.##");
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('@');
        df.setDecimalFormatSymbols(dfs);
        assertTrue("Not set", df.getDecimalFormatSymbols().equals(dfs));
        assertEquals("Symbols not used", "1@2", df.format(1.2));

        // The returned symbols may be cloned in two spots
        // 1. When set
        // 2. When returned
        DecimalFormat format = new DecimalFormat();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        format.setDecimalFormatSymbols(symbols);
        DecimalFormatSymbols symbolsOut = format.getDecimalFormatSymbols();
        assertNotSame(symbols, symbolsOut);
    }

    public void test_setDecimalSeparatorAlwaysShown() {
        DecimalFormat df = new DecimalFormat("###0.##", new DecimalFormatSymbols(Locale.US));
        assertEquals("Wrong default result", "5", df.format(5));
        df.setDecimalSeparatorAlwaysShown(true);
        assertTrue("Not set", df.isDecimalSeparatorAlwaysShown());
        assertEquals("Wrong set result", "7.", df.format(7));
    }

    public void test_setCurrency() {
        Locale locale = Locale.CANADA;
        DecimalFormat df = ((DecimalFormat) NumberFormat.getCurrencyInstance(locale));

        try {
            df.setCurrency(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }

        Currency currency = Currency.getInstance("AED");
        df.setCurrency(currency);
        assertTrue("Returned incorrect currency", currency == df.getCurrency());
        assertEquals("Returned incorrect currency symbol", currency.getSymbol(locale),
                df.getDecimalFormatSymbols().getCurrencySymbol());
        assertEquals("Returned incorrect international currency symbol", currency.getCurrencyCode(),
                df.getDecimalFormatSymbols().getInternationalCurrencySymbol());
    }

    public void test_setGroupingSize() {
        DecimalFormat df = new DecimalFormat("###0.##", new DecimalFormatSymbols(Locale.ENGLISH));
        df.setGroupingUsed(true);
        df.setGroupingSize(2);
        assertEquals("Value not set", 2, df.getGroupingSize());
        String result = df.format(123);
        assertTrue("Invalid format:" + result, result.equals("1,23"));
    }

    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new DecimalFormat());
    }

    public void testSerializationHarmonyRICompatible() throws Exception {
        NumberFormat nf = NumberFormat.getInstance(Locale.FRANCE);

        DecimalFormat df = null;
        if (!(nf instanceof DecimalFormat)) {
            throw new Error("This NumberFormat is not a DecimalFormat");

        }
        df = (DecimalFormat) nf;

        ObjectInputStream oinput = null;

        DecimalFormat deserializedDF = null;

        try {
            oinput = new ObjectInputStream(getClass().getResource(
                    "/serialization/org/apache/harmony/tests/java/text/DecimalFormat.ser")
                    .openStream());
            deserializedDF = (DecimalFormat) oinput.readObject();
        } finally {
            try {
                if (null != oinput) {
                    oinput.close();
                }
            } catch (Exception e) {
                // ignore
            }
        }

        assertEquals(df.getNegativePrefix(), deserializedDF.getNegativePrefix());
        assertEquals(df.getNegativeSuffix(), deserializedDF.getNegativeSuffix());
        assertEquals(df.getPositivePrefix(), deserializedDF.getPositivePrefix());
        assertEquals(df.getPositiveSuffix(), deserializedDF.getPositiveSuffix());
        assertEquals(df.getCurrency(), deserializedDF.getCurrency());

        DecimalFormatSymbolsTest.assertDecimalFormatSymbolsRIFrance(
                deserializedDF.getDecimalFormatSymbols());

        assertEquals(df.getGroupingSize(), df.getGroupingSize());
        assertEquals(df.getMaximumFractionDigits(), deserializedDF
                .getMaximumFractionDigits());

        assertEquals(df.getMaximumIntegerDigits(), deserializedDF
                .getMaximumIntegerDigits());

        assertEquals(df.getMinimumFractionDigits(), deserializedDF
                .getMinimumFractionDigits());
        assertEquals(df.getMinimumIntegerDigits(), deserializedDF
                .getMinimumIntegerDigits());
        assertEquals(df.getMultiplier(), deserializedDF.getMultiplier());

        // Deliberately omitted this assertion. Since different data resource
        // will cause the assertion fail.
        // assertEquals(df, deserializedDF);

    }

    public void test_parse_infinityBigDecimalFalse() {
        // Regression test for HARMONY-106
        DecimalFormat format = (DecimalFormat) NumberFormat.getInstance();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        Number number = format.parse(symbols.getInfinity(),
                new ParsePosition(0));
        assertTrue(number instanceof Double);
        assertTrue(Double.isInfinite(number.doubleValue()));
    }

    public void test_parse_minusInfinityBigDecimalFalse() {
        // Regression test for HARMONY-106
        DecimalFormat format = (DecimalFormat) NumberFormat.getInstance();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        Number number = format.parse("-" + symbols.getInfinity(),
                new ParsePosition(0));
        assertTrue(number instanceof Double);
        assertTrue(Double.isInfinite(number.doubleValue()));
    }

    public void test_setDecimalFormatSymbolsAsNull() {
        // Regression for HARMONY-1070
        DecimalFormat format = (DecimalFormat) NumberFormat.getInstance();
        format.setDecimalFormatSymbols(null);
    }

    public void test_formatToCharacterIterator_null() {
        try {
            // Regression for HARMONY-466
            new DecimalFormat().formatToCharacterIterator(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            // expected
        }
    }

    public void test_formatToCharacterIterator_original() {
        new Support_DecimalFormat(
                "test_formatToCharacterIteratorLjava_lang_Object")
                .t_formatToCharacterIterator();
    }

    public void test_formatToCharacterIterator() throws Exception {
        AttributedCharacterIterator iterator;
        int[] runStarts;
        int[] runLimits;
        String result;
        char current;

        // BigInteger.
        iterator = new DecimalFormat().formatToCharacterIterator(new BigInteger("123456789"));
        runStarts = new int[] { 0, 0, 0, 3, 4, 4, 4, 7, 8, 8, 8 };
        runLimits = new int[] { 3, 3, 3, 4, 7, 7, 7, 8, 11, 11, 11 };
        result = "123,456,789";
        current = iterator.current();
        for (int i = 0; i < runStarts.length; i++) {
            assertEquals("wrong start @" + i, runStarts[i], iterator.getRunStart());
            assertEquals("wrong limit @" + i, runLimits[i], iterator.getRunLimit());
            assertEquals("wrong char @" + i, result.charAt(i), current);
            current = iterator.next();
        }
        assertEquals(0, iterator.getBeginIndex());
        assertEquals(11, iterator.getEndIndex());

        // For BigDecimal with multiplier test.
        DecimalFormat df = new DecimalFormat();
        df.setMultiplier(10);
        iterator = df.formatToCharacterIterator(new BigDecimal("12345678901234567890"));
        result = "123,456,789,012,345,678,900";
        current = iterator.current();
        for (int i = 0; i < result.length(); i++) {
            assertEquals("wrong char @" + i, result.charAt(i), current);
            current = iterator.next();
        }

        // For BigDecimal with multiplier test.
        df = new DecimalFormat();
        df.setMultiplier(-1);
        df.setMaximumFractionDigits(20);
        iterator = df.formatToCharacterIterator(new BigDecimal("1.23456789012345678901"));
        result = "-1.23456789012345678901";
        current = iterator.current();
        for (int i = 0; i < result.length(); i++) {
            assertEquals("wrong char @" + i, result.charAt(i), current);
            current = iterator.next();
        }

        iterator = new DecimalFormat().formatToCharacterIterator(new BigDecimal("1.23456789E301"));
        runStarts = new int[] { 0, 0, 2, 3, 3, 3, 6, 7, 7, 7, 10, 11, 11, 11, 14 };
        runLimits = new int[] { 2, 2, 3, 6, 6, 6, 7, 10, 10, 10, 11, 14, 14, 14, 15 };
        result = "12,345,678,900,"; // 000,000,000,000....
        current = iterator.current();
        for (int i = 0; i < runStarts.length; i++) {
            assertEquals("wrong start @" + i, runStarts[i], iterator.getRunStart());
            assertEquals("wrong limit @" + i, runLimits[i], iterator.getRunLimit());
            assertEquals("wrong char @" + i, result.charAt(i), current);
            current = iterator.next();
        }
        assertEquals(0, iterator.getBeginIndex());
        assertEquals(402, iterator.getEndIndex());

        iterator = new DecimalFormat().formatToCharacterIterator(new BigDecimal("1.2345678E4"));
        runStarts = new int[] { 0, 0, 2, 3, 3, 3, 6, 7, 7, 7 };
        runLimits = new int[] { 2, 2, 3, 6, 6, 6, 7, 10, 10, 10 };
        result = "12,345.678";
        current = iterator.current();
        for (int i = 0; i < runStarts.length; i++) {
            assertEquals("wrong start @" + i, runStarts[i], iterator.getRunStart());
            assertEquals("wrong limit @" + i, runLimits[i], iterator.getRunLimit());
            assertEquals("wrong char @" + i, result.charAt(i), current);
            current = iterator.next();
        }
        assertEquals(0, iterator.getBeginIndex());
        assertEquals(10, iterator.getEndIndex());
    }

    public void test_formatToCharacterIterator_veryLarge() throws Exception {
        AttributedCharacterIterator iterator;
        int[] runStarts;
        int[] runLimits;
        String result;
        char current;

        Number number = new BigDecimal("1.23456789E1234");
        assertEquals("1.23456789E+1234", number.toString());
        iterator = new DecimalFormat().formatToCharacterIterator(number);
        runStarts = new int[] { 0, 0, 2, 3, 3, 3, 6, 7, 7, 7, 10, 11, 11, 11, 14 };
        runLimits = new int[] { 2, 2, 3, 6, 6, 6, 7, 10, 10, 10, 11, 14, 14, 14, 15 };
        result = "12,345,678,900,"; // 000,000,000,000....
        current = iterator.current();
        for (int i = 0; i < runStarts.length; i++) {
            assertEquals("wrong start @" + i, runStarts[i], iterator.getRunStart());
            assertEquals("wrong limit @" + i, runLimits[i], iterator.getRunLimit());
            assertEquals("wrong char @" + i, result.charAt(i), current);
            current = iterator.next();
        }
        assertEquals(0, iterator.getBeginIndex());
        assertEquals(1646, iterator.getEndIndex());
    }

    public void test_formatToCharacterIterator_roundingUnnecessaryArithmeticException() {
        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(Locale.US);
        decimalFormat.setRoundingMode(RoundingMode.UNNECESSARY);
        decimalFormat.setMaximumFractionDigits(0);
        try {
            // when rounding is needed, but RoundingMode is set to RoundingMode.UNNECESSARY,
            // throw ArithmeticException
            decimalFormat.formatToCharacterIterator(new Double(1.5));
            fail("ArithmeticException expected");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    public void test_formatDouble_roundingUnnecessaryArithmeticException() {
        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(Locale.US);
        decimalFormat.setMaximumFractionDigits(0);
        decimalFormat.setRoundingMode(RoundingMode.UNNECESSARY);

        try {
            // when rounding is needed, but RoundingMode is set to RoundingMode.UNNECESSARY,
            // throw ArithmeticException
            decimalFormat.format(11.5, new StringBuffer(), new FieldPosition(0));
            fail("ArithmeticException expected");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    public void test_format_roundingUnnecessaryArithmeticException() {
        final DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.US);
        DecimalFormat decimalFormat = new DecimalFormat("00.0#E0", dfs);

        decimalFormat.setRoundingMode(RoundingMode.UNNECESSARY);
        try {
            // when rounding is needed, but RoundingMode is set to RoundingMode.UNNECESSARY,
            // throw ArithmeticException
            decimalFormat.format(99999, new StringBuffer(), new FieldPosition(0));
            fail("ArithmeticException expected");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    public void test_getRoundingMode() {
        // get the default RoundingMode of this DecimalFormat
        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(Locale.US);

        // the default RoundingMode is HALF_EVEN
        assertEquals("Incorrect default RoundingMode",
                decimalFormat.getRoundingMode(), RoundingMode.HALF_EVEN);

        // set RoundingMode.HALF_DOWN of this DecimalFormat
        decimalFormat.setRoundingMode(RoundingMode.HALF_DOWN);
        assertEquals("Returned incorrect RoundingMode",
                decimalFormat.getRoundingMode(), RoundingMode.HALF_DOWN);

    }

    public void test_setRoundingMode_null() {
        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(Locale.US);
        try {
            // when the given RoundingMode is null, throw NullPointerException
            decimalFormat.setRoundingMode(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            // expected
        }
    }

    public void test_format_withRoundingMode() {
        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(Locale.US);
        // ignore the fraction part of a given value
        decimalFormat.setMaximumFractionDigits(0);

        // set RoundingMode.HALF_DOWN of this DecimalFormat and test its
        // behavior
        decimalFormat.setRoundingMode(RoundingMode.HALF_DOWN);
        String result = decimalFormat.format(11.3);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.HALF_DOWN", "11", result);

        result = decimalFormat.format(11.5);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.HALF_DOWN", "11", result);

        result = decimalFormat.format(11.6);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.HALF_DOWN", "12", result);

        // set RoundingMode.CEILING of this DecimalFormat and test its
        // behavior
        decimalFormat.setRoundingMode(RoundingMode.CEILING);
        result = decimalFormat.format(11.3);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.CEILING", "12", result);

        result = decimalFormat.format(-11.5);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.CEILING", "-11", result);

        // set RoundingMode.DOWN of this DecimalFormat and test its
        // behavior
        decimalFormat.setRoundingMode(RoundingMode.DOWN);
        result = decimalFormat.format(11.3);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.DOWN", "11", result);

        result = decimalFormat.format(-11.5);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.DOWN", "-11", result);

        result = decimalFormat.format(0);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.DOWN", "0", result);

        // set RoundingMode.FLOOR of this DecimalFormat and test its
        // behavior
        decimalFormat.setRoundingMode(RoundingMode.FLOOR);
        result = decimalFormat.format(11.3);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.FLOOR", "11", result);

        result = decimalFormat.format(-11.5);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.FLOOR", "-12", result);

        result = decimalFormat.format(0);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.FLOOR", "0", result);

        // set RoundingMode.HALF_EVEN of this DecimalFormat and test its
        // behavior
        decimalFormat.setRoundingMode(RoundingMode.HALF_EVEN);
        result = decimalFormat.format(5.5);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.HALF_EVEN", "6", result);

        result = decimalFormat.format(-5.5);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.HALF_EVEN", "-6", result);

        result = decimalFormat.format(0.2);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.HALF_EVEN", "0", result);

        // set RoundingMode.HALF_UP of this DecimalFormat and test its
        // behavior
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
        result = decimalFormat.format(5.5);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.HALF_UP", "6", result);

        result = decimalFormat.format(-5.5);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.HALF_UP", "-6", result);

        result = decimalFormat.format(0.2);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.HALF_UP", "0", result);

        result = decimalFormat.format(-0.2);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.HALF_UP", "-0", result);

        // set RoundingMode.UP of this DecimalFormat and test its
        // behavior
        decimalFormat.setRoundingMode(RoundingMode.UP);
        result = decimalFormat.format(5.5);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.UP", "6", result);

        result = decimalFormat.format(-5.5);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.UP", "-6", result);

        result = decimalFormat.format(0.2);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.UP", "1", result);

        result = decimalFormat.format(-0.2);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.UP", "-1", result);

        // set RoundingMode.UNNECESSARY of this DecimalFormat and test its
        // behavior
        decimalFormat.setRoundingMode(RoundingMode.UNNECESSARY);

        try {
            // when rounding is needed but RoundingMode is set to RoundingMode.UNNECESSARY,
            // throw ArithmeticException
            result = decimalFormat.format(5.5);
            fail("ArithmeticException expected: RoundingMode.UNNECESSARY");
        } catch (ArithmeticException e) {
            // expected
        }

        result = decimalFormat.format(1.0);
        assertEquals(
                "Incorrect RoundingMode behavior: RoundingMode.UNNECESSARY", "1", result);

        result = decimalFormat.format(-1.0);
        assertEquals(
                "Incorrect RoundingMode behavior: RoundingMode.UNNECESSARY", "-1", result);

        // set MaxFractionDigits to 3, test different DecimalFormat format
        // function with differnt RoundingMode
        decimalFormat.setMaximumFractionDigits(3);

        // set RoundingMode.HALF_DOWN of this DecimalFormat and test its
        // behavior
        decimalFormat.setRoundingMode(RoundingMode.HALF_DOWN);
        result = decimalFormat.format(11.5653);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.HALF_DOWN", "11.565", result);

        result = decimalFormat.format(11.5655);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.HALF_DOWN", "11.565", result);

        result = decimalFormat.format(11.5656);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.HALF_DOWN", "11.566", result);

        // set RoundingMode.CEILING of this DecimalFormat and test its
        // behavior
        decimalFormat.setRoundingMode(RoundingMode.CEILING);
        result = decimalFormat.format(11.5653);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.CEILING", "11.566", result);

        result = decimalFormat.format(-11.5653);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.CEILING", "-11.565", result);

        // set RoundingMode.DOWN of this DecimalFormat and test its
        // behavior
        decimalFormat.setRoundingMode(RoundingMode.DOWN);
        result = decimalFormat.format(11.5653);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.DOWN", "11.565", result);

        result = decimalFormat.format(-11.5653);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.DOWN", "-11.565", result);

        result = decimalFormat.format(0);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.DOWN", "0", result);

        // set RoundingMode.FLOOR of this DecimalFormat and test its
        // behavior
        decimalFormat.setRoundingMode(RoundingMode.FLOOR);
        result = decimalFormat.format(11.5653);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.FLOOR", "11.565", result);

        result = decimalFormat.format(-11.5655);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.FLOOR", "-11.566", result);

        result = decimalFormat.format(0);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.FLOOR", "0", result);

        // set RoundingMode.HALF_EVEN of this DecimalFormat and test its
        // behavior
        decimalFormat.setRoundingMode(RoundingMode.HALF_EVEN);
        result = decimalFormat.format(11.5653);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.HALF_EVEN", "11.565", result);

        result = decimalFormat.format(-11.5655);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.HALF_EVEN", "-11.566", result);

        result = decimalFormat.format(11.5656);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.HALF_EVEN", "11.566", result);

        // set RoundingMode.HALF_UP of this DecimalFormat and test its
        // behavior
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
        result = decimalFormat.format(11.5653);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.HALF_UP", "11.565", result);

        result = decimalFormat.format(-11.5655);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.HALF_UP", "-11.566", result);

        result = decimalFormat.format(11.5656);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.HALF_UP", "11.566", result);

        // set RoundingMode.UP of this DecimalFormat and test its
        // behavior
        decimalFormat.setRoundingMode(RoundingMode.UP);
        result = decimalFormat.format(11.5653);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.UP", "11.566", result);

        result = decimalFormat.format(-11.5655);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.UP", "-11.566", result);

        // set RoundingMode.UNNECESSARY of this DecimalFormat and test its
        // behavior
        decimalFormat.setRoundingMode(RoundingMode.UNNECESSARY);
        result = decimalFormat.format(-11.565);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.UNNECESSARY",
                "-11.565", result);

        result = decimalFormat.format(11.565);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.UNNECESSARY",
                "11.565", result);

        // when setting MaxFractionDigits to negative value -2, default it as
        // zero, test different DecimalFormat format
        // function with differnt RoundingMode
        decimalFormat.setMaximumFractionDigits(-2);

        // set RoundingMode.HALF_DOWN of this DecimalFormat and test its
        // behavior
        decimalFormat.setRoundingMode(RoundingMode.HALF_DOWN);
        result = decimalFormat.format(11.3);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.HALF_DOWN", "11", result);

        result = decimalFormat.format(11.5);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.HALF_DOWN", "11", result);

        result = decimalFormat.format(11.6);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.HALF_DOWN", "12", result);

        // set RoundingMode.CEILING of this DecimalFormat and test its
        // behavior
        decimalFormat.setRoundingMode(RoundingMode.CEILING);
        result = decimalFormat.format(11.3);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.CEILING", "12", result);

        result = decimalFormat.format(-11.5);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.CEILING", "-11", result);

        // set RoundingMode.DOWN of this DecimalFormat and test its
        // behavior
        decimalFormat.setRoundingMode(RoundingMode.DOWN);
        result = decimalFormat.format(11.3);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.DOWN", "11", result);

        result = decimalFormat.format(-11.5);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.DOWN", "-11", result);

        result = decimalFormat.format(0);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.DOWN", "0", result);

        // set RoundingMode.FLOOR of this DecimalFormat and test its
        // behavior
        decimalFormat.setRoundingMode(RoundingMode.FLOOR);
        result = decimalFormat.format(11.3);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.FLOOR", "11", result);

        result = decimalFormat.format(-11.5);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.FLOOR", "-12", result);

        result = decimalFormat.format(0);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.FLOOR", "0", result);

        // set RoundingMode.HALF_EVEN of this DecimalFormat and test its
        // behavior
        decimalFormat.setRoundingMode(RoundingMode.HALF_EVEN);
        result = decimalFormat.format(5.5);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.HALF_EVEN", "6", result);

        result = decimalFormat.format(-5.5);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.HALF_EVEN", "-6", result);

        result = decimalFormat.format(0.2);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.HALF_EVEN", "0", result);

        // set RoundingMode.HALF_UP of this DecimalFormat and test its
        // behavior
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
        result = decimalFormat.format(5.5);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.HALF_UP", "6", result);

        result = decimalFormat.format(-5.5);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.HALF_UP", "-6", result);

        result = decimalFormat.format(0.2);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.HALF_UP", "0", result);

        result = decimalFormat.format(-0.2);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.HALF_UP", "-0", result);

        // set RoundingMode.UP of this DecimalFormat and test its
        // behavior
        decimalFormat.setRoundingMode(RoundingMode.UP);
        result = decimalFormat.format(5.5);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.UP", "6", result);

        result = decimalFormat.format(-5.5);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.UP", "-6", result);

        result = decimalFormat.format(0.2);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.UP", "1", result);

        result = decimalFormat.format(-0.2);
        assertEquals("Incorrect RoundingMode behavior: RoundingMode.UP", "-1", result);

        // set RoundingMode.UNNECESSARY of this DecimalFormat and test its
        // behavior
        decimalFormat.setRoundingMode(RoundingMode.UNNECESSARY);

        result = decimalFormat.format(1.0);
        assertEquals(
                "Incorrect RoundingMode behavior: RoundingMode.UNNECESSARY", "1", result);

        result = decimalFormat.format(-1.0);
        assertEquals(
                "Incorrect RoundingMode behavior: RoundingMode.UNNECESSARY", "-1", result);

        // Regression for HARMONY-6485
        // Test with applyPattern call after setRoundingMode

        // set RoundingMode.HALF_UP of this DecimalFormat and test its
        // behavior
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
        decimalFormat.applyPattern(".##");
        result = decimalFormat.format(0.125);
        assertEquals("Incorrect RoundingMode behavior after applyPattern", ".13", result);
        result = decimalFormat.format(0.255);
        assertEquals("Incorrect RoundingMode behavior after applyPattern", ".26", result);
        result = decimalFormat.format(0.732);
        assertEquals("Incorrect RoundingMode behavior after applyPattern", ".73", result);
        result = decimalFormat.format(0.467);
        assertEquals("Incorrect RoundingMode behavior after applyPattern", ".47", result);

        // set RoundingMode.HALF_DOWN of this DecimalFormat and test its
        // behavior
        decimalFormat.setRoundingMode(RoundingMode.HALF_DOWN);
        decimalFormat.applyPattern(".##");
        result = decimalFormat.format(0.125);
        assertEquals("Incorrect RoundingMode behavior after applyPattern", ".12", result);
        result = decimalFormat.format(0.255);
        assertEquals("Incorrect RoundingMode behavior after applyPattern", ".25", result);
        result = decimalFormat.format(0.732);
        assertEquals("Incorrect RoundingMode behavior after applyPattern", ".73", result);
        result = decimalFormat.format(0.467);
        assertEquals("Incorrect RoundingMode behavior after applyPattern", ".47", result);

        // set RoundingMode.UP of this DecimalFormat and test its
        // behavior
        decimalFormat.setRoundingMode(RoundingMode.UP);
        decimalFormat.applyPattern(".##");
        result = decimalFormat.format(0.125);
        assertEquals("Incorrect RoundingMode behavior after applyPattern", ".13", result);
        result = decimalFormat.format(0.255);
        assertEquals("Incorrect RoundingMode behavior after applyPattern", ".26", result);
        result = decimalFormat.format(0.732);
        assertEquals("Incorrect RoundingMode behavior after applyPattern", ".74", result);
        result = decimalFormat.format(0.467);
        assertEquals("Incorrect RoundingMode behavior after applyPattern", ".47", result);

        // set RoundingMode.DOWN of this DecimalFormat and test its
        // behavior
        decimalFormat.setRoundingMode(RoundingMode.DOWN);
        decimalFormat.applyPattern(".##");
        result = decimalFormat.format(0.125);
        assertEquals("Incorrect RoundingMode behavior after applyPattern", ".12", result);
        result = decimalFormat.format(0.255);
        assertEquals("Incorrect RoundingMode behavior after applyPattern", ".25", result);
        result = decimalFormat.format(0.732);
        assertEquals("Incorrect RoundingMode behavior after applyPattern", ".73", result);
        result = decimalFormat.format(0.467);
        assertEquals("Incorrect RoundingMode behavior after applyPattern", ".46", result);

        // set RoundingMode.HALF_EVEN of this DecimalFormat and test its
        // behavior
        decimalFormat.setRoundingMode(RoundingMode.HALF_EVEN);
        decimalFormat.applyPattern(".##");
        result = decimalFormat.format(0.125);
        assertEquals("Incorrect RoundingMode behavior after applyPattern", ".12", result);
        result = decimalFormat.format(0.255);
        assertEquals("Incorrect RoundingMode behavior after applyPattern", ".26", result);
        result = decimalFormat.format(0.732);
        assertEquals("Incorrect RoundingMode behavior after applyPattern", ".73", result);
        result = decimalFormat.format(0.467);
        assertEquals("Incorrect RoundingMode behavior after applyPattern", ".47", result);
    }

    private static class FormatTester {
        private List<AssertionFailedError> failures = new ArrayList<AssertionFailedError>();

        public void format(DecimalFormat format, String expected, double value) {
            try {
                Assert.assertEquals(format.toPattern() + ": " + value, expected,
                        format.format(value));
            } catch (AssertionFailedError e) {
                failures.add(e);
            }
        }

        public void format(DecimalFormat format, String expected, int value) {
            try {
                Assert.assertEquals(format.toPattern() + ": " + value, expected,
                        format.format(value));
            } catch (AssertionFailedError e) {
                failures.add(e);
            }
        }

        public void throwFailures() throws AssertionFailedError {
            if (failures.isEmpty()) {
                return;
            }
            if (failures.size() == 1) {
                throw failures.get(0);
            }
            AssertionFailedError combined = new AssertionFailedError("Multiple format failures");
            for (AssertionFailedError failure : failures) {
                combined.addSuppressed(failure);
            }
            throw combined;
        }
    }
}
