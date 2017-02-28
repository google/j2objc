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

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateFormatTest extends junit.framework.TestCase {

	/**
	 * @tests java.text.DateFormat#clone()
	 */
	public void test_clone() {
		DateFormat format = DateFormat.getInstance();
		DateFormat clone = (DateFormat) format.clone();
		assertTrue("Clone not equal", format.equals(clone));
		clone.getNumberFormat().setMinimumFractionDigits(123);
		assertTrue("Clone shares NumberFormat", !format.equals(clone));
	}

	/**
	 * @tests java.text.DateFormat#getAvailableLocales()
	 */
	public void test_getAvailableLocales() {
		Locale[] locales = DateFormat.getAvailableLocales();
		assertTrue("No locales", locales.length > 0);
		boolean english = false, german = false;
		for (int i = locales.length; --i >= 0;) {
			if (locales[i].equals(Locale.ENGLISH))
				english = true;
			if (locales[i].equals(Locale.GERMAN))
				german = true;
			DateFormat f1 = DateFormat.getDateTimeInstance(DateFormat.SHORT,
					DateFormat.SHORT, locales[i]);
			assertTrue("Doesn't work",
					f1.format(new Date()).getClass() == String.class);
		}
		assertTrue("Missing locales", english && german);
	}

	/**
	 * @tests java.text.DateFormat#getCalendar()
	 */
	public void test_getCalendar() {
		DateFormat format = DateFormat.getInstance();
		Calendar cal1 = format.getCalendar();
		Calendar cal2 = format.getCalendar();
		assertTrue("Calendars not identical", cal1 == cal2);
	}

	/**
	 * @tests java.text.DateFormat#getDateInstance()
	 */
	public void test_getDateInstance() {
		SimpleDateFormat f2 = (SimpleDateFormat) DateFormat.getDateInstance();
		assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
		assertTrue("Wrong default", f2.equals(DateFormat.getDateInstance(
				DateFormat.DEFAULT, Locale.getDefault())));
		assertEquals(f2.getDateFormatSymbols(), new DateFormatSymbols());
		assertTrue("Doesn't work",
				f2.format(new Date()).getClass() == String.class);
	}

	/**
	 * @tests java.text.DateFormat#getDateInstance(int)
	 */
	public void test_getDateInstanceI() {
		assertTrue("Default not medium",
				DateFormat.DEFAULT == DateFormat.MEDIUM);

		SimpleDateFormat f2 = (SimpleDateFormat) DateFormat
				.getDateInstance(DateFormat.SHORT);
		assertTrue("Wrong class1", f2.getClass() == SimpleDateFormat.class);
		assertTrue("Wrong default1", f2.equals(DateFormat.getDateInstance(
				DateFormat.SHORT, Locale.getDefault())));
		assertTrue("Wrong symbols1", f2.getDateFormatSymbols().equals(
				new DateFormatSymbols()));
		assertTrue("Doesn't work1",
				f2.format(new Date()).getClass() == String.class);

		f2 = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.MEDIUM);
		assertTrue("Wrong class2", f2.getClass() == SimpleDateFormat.class);
		assertTrue("Wrong default2", f2.equals(DateFormat.getDateInstance(
				DateFormat.MEDIUM, Locale.getDefault())));
		assertTrue("Wrong symbols2", f2.getDateFormatSymbols().equals(
				new DateFormatSymbols()));
		assertTrue("Doesn't work2",
				f2.format(new Date()).getClass() == String.class);

		f2 = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.LONG);
		assertTrue("Wrong class3", f2.getClass() == SimpleDateFormat.class);
		assertTrue("Wrong default3", f2.equals(DateFormat.getDateInstance(
				DateFormat.LONG, Locale.getDefault())));
		assertTrue("Wrong symbols3", f2.getDateFormatSymbols().equals(
				new DateFormatSymbols()));
		assertTrue("Doesn't work3",
				f2.format(new Date()).getClass() == String.class);

		f2 = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.FULL);
		assertTrue("Wrong class4", f2.getClass() == SimpleDateFormat.class);
		assertTrue("Wrong default4", f2.equals(DateFormat.getDateInstance(
				DateFormat.FULL, Locale.getDefault())));
		assertTrue("Wrong symbols4", f2.getDateFormatSymbols().equals(
				new DateFormatSymbols()));
		assertTrue("Doesn't work4",
				f2.format(new Date()).getClass() == String.class);
		
		// regression test for HARMONY-940
		try {
			DateFormat.getDateInstance(77);
            fail("Should throw IAE");
		} catch (IllegalArgumentException iae) {
			//expected
		}
	}

	/**
	 * @tests java.text.DateFormat#getDateInstance(int, java.util.Locale)
	 */
	public void test_getDateInstanceILjava_util_Locale() {
		SimpleDateFormat f2 = (SimpleDateFormat) DateFormat.getDateInstance(
				DateFormat.SHORT, Locale.GERMAN);
		assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
		assertTrue("Wrong symbols", f2.getDateFormatSymbols().equals(
				new DateFormatSymbols(Locale.GERMAN)));
		assertTrue("Doesn't work",
				f2.format(new Date()).getClass() == String.class);

		f2 = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.MEDIUM,
				Locale.GERMAN);
		assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
		assertTrue("Wrong symbols", f2.getDateFormatSymbols().equals(
				new DateFormatSymbols(Locale.GERMAN)));
		assertTrue("Doesn't work",
				f2.format(new Date()).getClass() == String.class);

		f2 = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.LONG,
				Locale.GERMAN);
		assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
		assertTrue("Wrong symbols", f2.getDateFormatSymbols().equals(
				new DateFormatSymbols(Locale.GERMAN)));
		assertTrue("Doesn't work",
				f2.format(new Date()).getClass() == String.class);

		f2 = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.FULL,
				Locale.GERMAN);
		assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
		assertTrue("Wrong symbols", f2.getDateFormatSymbols().equals(
				new DateFormatSymbols(Locale.GERMAN)));
		assertTrue("Doesn't work",
				f2.format(new Date()).getClass() == String.class);

		// regression test for HARMONY-940
		try {
			DateFormat.getDateInstance(77, Locale.GERMAN);
            fail("Should throw IAE");
		} catch (IllegalArgumentException iae) {
			//expected
		}
	}

	/**
	 * @tests java.text.DateFormat#getDateTimeInstance()
	 */
	public void test_getDateTimeInstance() {
		SimpleDateFormat f2 = (SimpleDateFormat) DateFormat
				.getDateTimeInstance();
		assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
		assertTrue("Wrong default", f2.equals(DateFormat.getDateTimeInstance(
				DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.getDefault())));
		assertTrue("Wrong symbols", f2.getDateFormatSymbols().equals(
				new DateFormatSymbols()));
		assertTrue("Doesn't work",
				f2.format(new Date()).getClass() == String.class);
	}

	private void testDateTime(int dStyle, int tStyle) {
		SimpleDateFormat f2 = (SimpleDateFormat) DateFormat
				.getDateTimeInstance(dStyle, tStyle);
		assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
		SimpleDateFormat date = (SimpleDateFormat) DateFormat.getDateInstance(
				dStyle, Locale.getDefault());
		SimpleDateFormat time = (SimpleDateFormat) DateFormat.getTimeInstance(
				tStyle, Locale.getDefault());
		assertEquals(f2.toPattern(), date.toPattern() + " " + time.toPattern());
		assertEquals(f2.getDateFormatSymbols(), new DateFormatSymbols());
		assertTrue("Doesn't work",
				f2.format(new Date()).getClass() == String.class);
	}

	/**
	 * @tests java.text.DateFormat#getDateTimeInstance(int, int)
	 */
	public void test_getDateTimeInstanceII() {
		testDateTime(DateFormat.SHORT, DateFormat.SHORT);
		testDateTime(DateFormat.SHORT, DateFormat.MEDIUM);
		testDateTime(DateFormat.SHORT, DateFormat.LONG);
		testDateTime(DateFormat.SHORT, DateFormat.FULL);

		testDateTime(DateFormat.MEDIUM, DateFormat.SHORT);
		testDateTime(DateFormat.MEDIUM, DateFormat.MEDIUM);
		testDateTime(DateFormat.MEDIUM, DateFormat.LONG);
		testDateTime(DateFormat.MEDIUM, DateFormat.FULL);

		testDateTime(DateFormat.LONG, DateFormat.SHORT);
		testDateTime(DateFormat.LONG, DateFormat.MEDIUM);
		testDateTime(DateFormat.LONG, DateFormat.LONG);
		testDateTime(DateFormat.LONG, DateFormat.FULL);

		testDateTime(DateFormat.FULL, DateFormat.SHORT);
		testDateTime(DateFormat.FULL, DateFormat.MEDIUM);
		testDateTime(DateFormat.FULL, DateFormat.LONG);
		testDateTime(DateFormat.FULL, DateFormat.FULL);
		
		// regression test for HARMONY-940
		try {
			DateFormat.getDateTimeInstance(77, 66);
            fail("Should throw IAE");
		} catch (IllegalArgumentException iae) {
			//expected
		}
	}

	private void testDateTimeLocale(int dStyle, int tStyle) {
		SimpleDateFormat f2 = (SimpleDateFormat) DateFormat
				.getDateTimeInstance(dStyle, tStyle, Locale.GERMAN);
		assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
		SimpleDateFormat date = (SimpleDateFormat) DateFormat.getDateInstance(
				dStyle, Locale.GERMAN);
		SimpleDateFormat time = (SimpleDateFormat) DateFormat.getTimeInstance(
				tStyle, Locale.GERMAN);
		assertTrue("Wrong default", f2.toPattern().equals(
				date.toPattern() + " " + time.toPattern()));
		assertTrue("Wrong symbols", f2.getDateFormatSymbols().equals(
				new DateFormatSymbols(Locale.GERMAN)));
		assertTrue("Doesn't work",
				f2.format(new Date()).getClass() == String.class);
	}

	/**
	 * @tests java.text.DateFormat#getDateTimeInstance(int, int,
	 *        java.util.Locale)
	 */
	public void test_getDateTimeInstanceIILjava_util_Locale() {
		testDateTimeLocale(DateFormat.SHORT, DateFormat.SHORT);
		testDateTimeLocale(DateFormat.SHORT, DateFormat.MEDIUM);
		testDateTimeLocale(DateFormat.SHORT, DateFormat.LONG);
		testDateTimeLocale(DateFormat.SHORT, DateFormat.FULL);

		testDateTimeLocale(DateFormat.MEDIUM, DateFormat.SHORT);
		testDateTimeLocale(DateFormat.MEDIUM, DateFormat.MEDIUM);
		testDateTimeLocale(DateFormat.MEDIUM, DateFormat.LONG);
		testDateTimeLocale(DateFormat.MEDIUM, DateFormat.FULL);

		testDateTimeLocale(DateFormat.LONG, DateFormat.SHORT);
		testDateTimeLocale(DateFormat.LONG, DateFormat.MEDIUM);
		testDateTimeLocale(DateFormat.LONG, DateFormat.LONG);
		testDateTimeLocale(DateFormat.LONG, DateFormat.FULL);

		testDateTimeLocale(DateFormat.FULL, DateFormat.SHORT);
		testDateTimeLocale(DateFormat.FULL, DateFormat.MEDIUM);
		testDateTimeLocale(DateFormat.FULL, DateFormat.LONG);
		testDateTimeLocale(DateFormat.FULL, DateFormat.FULL);

		// regression test for HARMONY-940
		try {
			DateFormat.getDateTimeInstance(77, 66, Locale.GERMAN);
            fail("Should throw IAE");
		} catch (IllegalArgumentException iae) {
			//expected
		}
	}

	/**
	 * @tests java.text.DateFormat#getInstance()
	 */
	public void test_getInstance() {
		SimpleDateFormat f2 = (SimpleDateFormat) DateFormat.getInstance();
		assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
		assertTrue("Wrong default", f2.equals(DateFormat.getDateTimeInstance(
				DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())));
		assertTrue("Wrong symbols", f2.getDateFormatSymbols().equals(
				new DateFormatSymbols()));
		assertTrue("Doesn't work",
				f2.format(new Date()).getClass() == String.class);
	}

	/**
	 * @tests java.text.DateFormat#getNumberFormat()
	 */
	public void test_getNumberFormat() {
		DateFormat format = DateFormat.getInstance();
		NumberFormat nf1 = format.getNumberFormat();
		NumberFormat nf2 = format.getNumberFormat();
		assertTrue("NumberFormats not identical", nf1 == nf2);
	}

	/**
	 * @tests java.text.DateFormat#getTimeInstance()
	 */
	public void test_getTimeInstance() {
		SimpleDateFormat f2 = (SimpleDateFormat) DateFormat.getTimeInstance();
		assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
		assertTrue("Wrong default", f2.equals(DateFormat.getTimeInstance(
				DateFormat.DEFAULT, Locale.getDefault())));
		assertTrue("Wrong symbols", f2.getDateFormatSymbols().equals(
				new DateFormatSymbols()));
		assertTrue("Doesn't work",
				f2.format(new Date()).getClass() == String.class);
	}

	/**
	 * @tests java.text.DateFormat#getTimeInstance(int)
	 */
	public void test_getTimeInstanceI() {
		SimpleDateFormat f2 = (SimpleDateFormat) DateFormat
				.getTimeInstance(DateFormat.SHORT);
		assertTrue("Wrong class1", f2.getClass() == SimpleDateFormat.class);
		assertTrue("Wrong default1", f2.equals(DateFormat.getTimeInstance(
				DateFormat.SHORT, Locale.getDefault())));
		assertTrue("Wrong symbols1", f2.getDateFormatSymbols().equals(
				new DateFormatSymbols()));
		assertTrue("Doesn't work1",
				f2.format(new Date()).getClass() == String.class);

		f2 = (SimpleDateFormat) DateFormat.getTimeInstance(DateFormat.MEDIUM);
		assertTrue("Wrong class2", f2.getClass() == SimpleDateFormat.class);
		assertTrue("Wrong default2", f2.equals(DateFormat.getTimeInstance(
				DateFormat.MEDIUM, Locale.getDefault())));
		assertTrue("Wrong symbols2", f2.getDateFormatSymbols().equals(
				new DateFormatSymbols()));
		assertTrue("Doesn't work2",
				f2.format(new Date()).getClass() == String.class);

		f2 = (SimpleDateFormat) DateFormat.getTimeInstance(DateFormat.LONG);
		assertTrue("Wrong class3", f2.getClass() == SimpleDateFormat.class);
		assertTrue("Wrong default3", f2.equals(DateFormat.getTimeInstance(
				DateFormat.LONG, Locale.getDefault())));
		assertTrue("Wrong symbols3", f2.getDateFormatSymbols().equals(
				new DateFormatSymbols()));
		assertTrue("Doesn't work3",
				f2.format(new Date()).getClass() == String.class);

		f2 = (SimpleDateFormat) DateFormat.getTimeInstance(DateFormat.FULL);
		assertTrue("Wrong class4", f2.getClass() == SimpleDateFormat.class);
		assertTrue("Wrong default4", f2.equals(DateFormat.getTimeInstance(
				DateFormat.FULL, Locale.getDefault())));
		assertTrue("Wrong symbols4", f2.getDateFormatSymbols().equals(
				new DateFormatSymbols()));
		assertTrue("Doesn't work4",
				f2.format(new Date()).getClass() == String.class);

		// regression test for HARMONY-940
		try {
			DateFormat.getTimeInstance(77);
            fail("Should throw IAE");
		} catch (IllegalArgumentException iae) {
			//expected
		}
	}

	/**
	 * @tests java.text.DateFormat#getTimeInstance(int, java.util.Locale)
	 */
	public void test_getTimeInstanceILjava_util_Locale() {
		SimpleDateFormat f2 = (SimpleDateFormat) DateFormat.getTimeInstance(
				DateFormat.SHORT, Locale.GERMAN);
		assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
		assertTrue("Wrong symbols", f2.getDateFormatSymbols().equals(
				new DateFormatSymbols(Locale.GERMAN)));
		assertTrue("Doesn't work",
				f2.format(new Date()).getClass() == String.class);

		f2 = (SimpleDateFormat) DateFormat.getTimeInstance(DateFormat.MEDIUM,
				Locale.GERMAN);
		assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
		assertTrue("Wrong symbols", f2.getDateFormatSymbols().equals(
				new DateFormatSymbols(Locale.GERMAN)));
		assertTrue("Doesn't work",
				f2.format(new Date()).getClass() == String.class);

		f2 = (SimpleDateFormat) DateFormat.getTimeInstance(DateFormat.LONG,
				Locale.GERMAN);
		assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
		assertTrue("Wrong symbols", f2.getDateFormatSymbols().equals(
				new DateFormatSymbols(Locale.GERMAN)));
		assertTrue("Doesn't work",
				f2.format(new Date()).getClass() == String.class);

		f2 = (SimpleDateFormat) DateFormat.getTimeInstance(DateFormat.FULL,
				Locale.GERMAN);
		assertTrue("Wrong class", f2.getClass() == SimpleDateFormat.class);
		assertTrue("Wrong symbols", f2.getDateFormatSymbols().equals(
				new DateFormatSymbols(Locale.GERMAN)));
		assertTrue("Doesn't work",
				f2.format(new Date()).getClass() == String.class);
		
		// regression test for HARMONY-940
		try {
			DateFormat.getTimeInstance(77, Locale.GERMAN);
            fail("Should throw IAE");
		} catch (IllegalArgumentException iae) {
			//expected
		}
	}

	/**
	 * @tests java.text.DateFormat#setCalendar(java.util.Calendar)
	 */
	public void test_setCalendarLjava_util_Calendar() {
		DateFormat format = DateFormat.getInstance();
		Calendar cal = Calendar.getInstance();
		format.setCalendar(cal);
		assertTrue("Not identical Calendar", cal == format.getCalendar());
	}

	/**
	 * @tests java.text.DateFormat#setNumberFormat(java.text.NumberFormat)
	 */
	public void test_setNumberFormatLjava_text_NumberFormat() {
		DateFormat format = DateFormat.getInstance();
		NumberFormat f1 = NumberFormat.getInstance();
		format.setNumberFormat(f1);
		assertTrue("Not identical NumberFormat", f1 == format.getNumberFormat());
	}
	
	/**
	 * @tests java.text.DateFormat#parse(String)
	 */
	public void test_parse_LString() {
		DateFormat format = DateFormat.getInstance();
		try {
			format.parse("not a Date");
			fail("should throw ParseException first");
		} catch (ParseException e) {
			assertNotNull(e.getMessage());
		}
	}

    /**
     * @tests java.text.DateFormat#setLenient(boolean)
     */
    public void test_setLenient() {
	Date d = null;
	DateFormat output = new SimpleDateFormat("MM/dd/yy");
	output.setLenient(false);
	try {
	    d = output.parse("01/01/-1");
	    fail("Should throw ParseException here.");
	} catch (ParseException e) {}
    }
}
