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

import java.math.RoundingMode;
import java.text.ChoiceFormat;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Currency;
import java.util.Locale;

public class NumberFormatTest extends junit.framework.TestCase {

    /**
     * @tests java.text.NumberFormat#format(java.lang.Object,
     *        java.lang.StringBuffer, java.text.FieldPosition)
     */
    public void test_formatLjava_lang_ObjectLjava_lang_StringBufferLjava_text_FieldPosition() {
        FieldPosition pos;
        StringBuffer out;
        DecimalFormat format = (DecimalFormat) NumberFormat
                .getInstance(Locale.US);

        pos = new FieldPosition(0);
        out = format.format(new Long(Long.MAX_VALUE), new StringBuffer(), pos);
        assertEquals("Wrong result L1: " + out, "9,223,372,036,854,775,807",
                out.toString());

        pos = new FieldPosition(0);
        out = format.format(new Long(Long.MIN_VALUE), new StringBuffer(), pos);
        assertEquals("Wrong result L2: " + out, "-9,223,372,036,854,775,808",
                out.toString());

        pos = new FieldPosition(0);
        out = format.format(new java.math.BigInteger(String
                .valueOf(Long.MAX_VALUE)), new StringBuffer(), pos);
        assertEquals("Wrong result BI1: " + out, "9,223,372,036,854,775,807",
                out.toString());

        pos = new FieldPosition(0);
        out = format.format(new java.math.BigInteger(String
                .valueOf(Long.MIN_VALUE)), new StringBuffer(), pos);
        assertEquals("Wrong result BI2: " + out, "-9,223,372,036,854,775,808",
                out.toString());

        java.math.BigInteger big;
        pos = new FieldPosition(0);
        big = new java.math.BigInteger(String.valueOf(Long.MAX_VALUE))
                .add(new java.math.BigInteger("1"));
        out = format.format(big, new StringBuffer(), pos);
        assertEquals("Wrong result BI3: " + out, "9,223,372,036,854,775,808",
                out.toString());

        pos = new FieldPosition(0);
        big = new java.math.BigInteger(String.valueOf(Long.MIN_VALUE))
                .add(new java.math.BigInteger("-1"));
        out = format.format(big, new StringBuffer(), pos);
        assertEquals("Wrong result BI4: " + out, "-9,223,372,036,854,775,809",
                out.toString());

        pos = new FieldPosition(0);
        out = format.format(new java.math.BigDecimal("51.348"),
                new StringBuffer(), pos);
        assertEquals("Wrong result BD1: " + out, "51.348", out.toString());

        pos = new FieldPosition(0);
        out = format.format(new java.math.BigDecimal("51"), new StringBuffer(),
                pos);
        assertEquals("Wrong result BD2: " + out, "51", out.toString());

        try {
            format.format(this, new StringBuffer(), pos);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        try {
            format.format(null, new StringBuffer(), pos);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        try {
            format.format(new Long(0), null, pos);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }

        try {
            format.format(new Long(0), new StringBuffer(), null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * @tests java.text.NumberFormat#getIntegerInstance()
     */
    public void test_getIntegerInstance() throws ParseException {
        // Test for method java.text.NumberFormat getIntegerInstance()
        Locale origLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);

        DecimalFormat format = (DecimalFormat) NumberFormat
                .getIntegerInstance();

        assertEquals(
                "Test1: NumberFormat.getIntegerInstance().toPattern() returned wrong pattern",
                "#,##0", format.toPattern());
        assertEquals(
                "Test2: NumberFormat.getIntegerInstance().format(35.76) returned wrong value",
                "36", format.format(35.76));
        assertEquals(
                "Test3: NumberFormat.getIntegerInstance().parse(\"35.76\") returned wrong number",
                new Long(35), format.parse("35.76"));
        assertEquals(
                "Test4: NumberFormat.getIntegerInstance().parseObject(\"35.76\") returned wrong number",
                new Long(35), format.parseObject("35.76"));
        Locale.setDefault(origLocale);
    }

    /**
     * @tests java.text.NumberFormat#getIntegerInstance(java.util.Locale)
     */
    public void test_getIntegerInstanceLjava_util_Locale()
            throws ParseException {
        // Test for method java.text.NumberFormat
        // getIntegerInstance(java.util.Locale)
        Locale usLocale = Locale.US;
        Locale arLocale = new Locale("ar", "AE");

        DecimalFormat format = (DecimalFormat) NumberFormat
                .getIntegerInstance(usLocale);
        assertEquals(
                "Test1: NumberFormat.getIntegerInstance().toPattern() returned wrong pattern",
                "#,##0", format.toPattern());
        assertEquals(
                "Test2: NumberFormat.getIntegerInstance().format(-35.76) returned wrong value",
                "-36", format.format(-35.76));
        assertEquals(
                "Test3: NumberFormat.getIntegerInstance().parse(\"-36\") returned wrong number",
                new Long(-36), format.parse("-36"));
        assertEquals(
                "Test4: NumberFormat.getIntegerInstance().parseObject(\"-36\") returned wrong number",
                new Long(-36), format.parseObject("-36"));
        assertEquals(
                "Test5: NumberFormat.getIntegerInstance().getMaximumFractionDigits() returned wrong value",
                0, format.getMaximumFractionDigits());
        assertTrue("Test6: NumberFormat.getIntegerInstance().isParseIntegerOnly() returned wrong value",
                format.isParseIntegerOnly());

        // try with a locale that has a different integer pattern
        format = (DecimalFormat) NumberFormat.getIntegerInstance(arLocale);
        // Previous versions of android use just the positive format string (ICU4C) although now we
        // use '<positive_format>;<negative_format>' because of ICU4J denormalization.
        String variant = (format.toPattern().indexOf(';') > 0) ? "#,##0;-#,##0" : "#,##0";
        assertEquals(
                "Test7: NumberFormat.getIntegerInstance(new Locale(\"ar\", \"AE\")).toPattern() returned wrong pattern",
                variant, format.toPattern());
        /* J2ObjC: MacOS doesn't seem to have the Arabic symbols.
        assertEquals(
                "Test8: NumberFormat.getIntegerInstance(new Locale(\"ar\", \"AE\")).format(-6) returned wrong value",
                "-\u0666", format.format(-6));*/
        assertEquals(
                "Test9: NumberFormat.getIntegerInstance(new Locale(\"ar\", \"AE\")).parse(\"-36-\") returned wrong number",
                new Long(36), format.parse("36-"));
        assertEquals(
                "Test10: NumberFormat.getIntegerInstance(new Locale(\"ar\", \"AE\")).parseObject(\"36-\") returned wrong number",
                new Long(36), format.parseObject("36-"));

        assertEquals(
                "Test11: NumberFormat.getIntegerInstance(new Locale(\"ar\", \"AE\")).getMaximumFractionDigits() returned wrong value",
                0, format.getMaximumFractionDigits());
        assertTrue(
                   "Test12: NumberFormat.getIntegerInstance(new Locale(\"ar\", \"AE\")).isParseIntegerOnly() returned wrong value",
                   format.isParseIntegerOnly());
    }

    /**
     * @tests java.text.NumberFormat#getCurrency()
     */
    public void test_getCurrency() {
        // Test for method java.util.Currency getCurrency()

        // a subclass that supports currency formatting
        Currency currH = Currency.getInstance("HUF");
        NumberFormat format = NumberFormat.getInstance(new Locale("hu", "HU"));
        assertSame("Returned incorrect currency", currH, format.getCurrency());

        // a subclass that doesn't support currency formatting
        ChoiceFormat cformat = new ChoiceFormat(
                "0#Less than one|1#one|1<Between one and two|2<Greater than two");
        try {
            ((NumberFormat) cformat).getCurrency();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }
    }

    /**
     * @tests java.text.NumberFormat#getMaximumIntegerDigits()
     */
    public void test_getMaximumIntegerDigits() {
        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumIntegerDigits(2);
        assertEquals("Wrong result", "23", format.format(123));
    }

    /**
     * @tests java.text.NumberFormat#setCurrency(java.util.Currency)
     */
    public void test_setCurrencyLjava_util_Currency() {
        // Test for method void setCurrency(java.util.Currency)
        // a subclass that supports currency formatting
        Currency currA = Currency.getInstance("ARS");
        NumberFormat format = NumberFormat.getInstance(new Locale("hu", "HU"));
        format.setCurrency(currA);
        assertSame("Returned incorrect currency", currA, format.getCurrency());

        // a subclass that doesn't support currency formatting
        ChoiceFormat cformat = new ChoiceFormat(
                "0#Less than one|1#one|1<Between one and two|2<Greater than two");
        try {
            ((NumberFormat) cformat).setCurrency(currA);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }
    }
    /**
     * @tests java.text.NumberFormat#parseObject(java.lang.String, java.text.ParsePosition)
     */
    public void test_parseObjectLjava_lang_StringLjava_text_ParsePosition() {
    	// regression test for HARMONY-1003
    	assertNull(NumberFormat.getInstance().parseObject("0", new ParsePosition(-1)));

         // Regression for HARMONY-1685
         try {
             NumberFormat.getInstance().parseObject("test", null);
             fail("NullPointerException expected");
         } catch (NullPointerException e) {
            //expected
	    }
    }

    protected void setUp() {
    }

    protected void tearDown() {
    }

	public void test_setRoundingMode_NullRoundingMode() {
		try {
			// Create a subclass ChoiceFormat which doesn't support
			// RoundingMode
			ChoiceFormat choiceFormat = new ChoiceFormat(
					"0#Less than one|1#one|1<Between one and two|2<Greater than two");
			((NumberFormat) choiceFormat).setRoundingMode(null);
			// Follow the behavior of RI
			fail("UnsupportedOperationException expected");
		} catch (UnsupportedOperationException e) {
			// expected
		}
	}

	public void test_setRoundingMode_Normal() {
		try {
			// Create a subclass ChoiceFormat which doesn't support
			// RoundingMode
			ChoiceFormat choiceFormat = new ChoiceFormat(
					"0#Less than one|1#one|1<Between one and two|2<Greater than two");
			((NumberFormat) choiceFormat).setRoundingMode(RoundingMode.CEILING);
			fail("UnsupportedOperationException expected");
		} catch (UnsupportedOperationException e) {
			// expected
		}
	}

}
