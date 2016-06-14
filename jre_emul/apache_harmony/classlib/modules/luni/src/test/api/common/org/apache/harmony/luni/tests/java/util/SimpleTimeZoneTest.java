/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.luni.tests.java.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class SimpleTimeZoneTest extends junit.framework.TestCase {

	SimpleTimeZone st1;

	SimpleTimeZone st2;

	/**
	 * @tests java.util.SimpleTimeZone#SimpleTimeZone(int, java.lang.String)
	 */
	public void test_ConstructorILjava_lang_String() {
		// Test for method java.util.SimpleTimeZone(int, java.lang.String)

		SimpleTimeZone st = new SimpleTimeZone(1000, "TEST");
		assertEquals("Incorrect TZ constructed", "TEST", st.getID());
		assertTrue("Incorrect TZ constructed: " + "returned wrong offset", st
				.getRawOffset() == 1000);
		assertTrue("Incorrect TZ constructed" + "using daylight savings", !st
				.useDaylightTime());
	}

	/**
	 * @tests java.util.SimpleTimeZone#SimpleTimeZone(int, java.lang.String,
	 *        int, int, int, int, int, int, int, int)
	 */
	public void test_ConstructorILjava_lang_StringIIIIIIII() {
		// Test for method java.util.SimpleTimeZone(int, java.lang.String, int,
		// int, int, int, int, int, int, int)
		SimpleTimeZone st = new SimpleTimeZone(1000, "TEST", Calendar.NOVEMBER,
				1, Calendar.SUNDAY, 0, Calendar.NOVEMBER, -1, Calendar.SUNDAY,
				0);
		assertTrue("Incorrect TZ constructed", st
				.inDaylightTime(new GregorianCalendar(1998, Calendar.NOVEMBER,
						13).getTime()));
		assertTrue("Incorrect TZ constructed", !(st
				.inDaylightTime(new GregorianCalendar(1998, Calendar.OCTOBER,
						13).getTime())));
		assertEquals("Incorrect TZ constructed", "TEST", st.getID());
		assertEquals("Incorrect TZ constructed", 1000, st.getRawOffset());
		assertTrue("Incorrect TZ constructed", st.useDaylightTime());
	}

	/**
	 * @tests java.util.SimpleTimeZone#SimpleTimeZone(int, java.lang.String,
	 *        int, int, int, int, int, int, int, int, int)
	 */
	public void test_ConstructorILjava_lang_StringIIIIIIIII() {
		// Test for method java.util.SimpleTimeZone(int, java.lang.String, int,
		// int, int, int, int, int, int, int, int)
		SimpleTimeZone st = new SimpleTimeZone(1000, "TEST", Calendar.NOVEMBER,
				1, Calendar.SUNDAY, 0, Calendar.NOVEMBER, -1, Calendar.SUNDAY,
				0, 1000 * 60 * 60);
		assertTrue("Incorrect TZ constructed", st
				.inDaylightTime(new GregorianCalendar(1998, Calendar.NOVEMBER,
						13).getTime()));
		assertTrue("Incorrect TZ constructed", !(st
				.inDaylightTime(new GregorianCalendar(1998, Calendar.OCTOBER,
						13).getTime())));
		assertEquals("Incorrect TZ constructed", "TEST", st.getID());
		assertEquals("Incorrect TZ constructed", 1000, st.getRawOffset());
		assertTrue("Incorrect TZ constructed", st.useDaylightTime());
		assertTrue("Incorrect TZ constructed",
				st.getDSTSavings() == 1000 * 60 * 60);
	}

	/**
	 * @tests java.util.SimpleTimeZone#SimpleTimeZone(int, java.lang.String,
	 *        int, int, int, int, int, int, int, int, int, int, int)
	 */
	public void test_ConstructorILjava_lang_StringIIIIIIIIIII() {
		// Test for method java.util.SimpleTimeZone(int, java.lang.String, int,
		// int, int, int, int, int, int, int, int, int, int)
		// TODO : Implement test
		//Regression for HARMONY-1241
		assertNotNull(new SimpleTimeZone(
                TimeZone.LONG,
                "Europe/Paris",
                SimpleTimeZone.STANDARD_TIME,
                SimpleTimeZone.STANDARD_TIME,
                SimpleTimeZone.UTC_TIME,
                SimpleTimeZone.WALL_TIME,
                SimpleTimeZone.WALL_TIME,
                TimeZone.SHORT,
                SimpleTimeZone.STANDARD_TIME,
                TimeZone.LONG,
                SimpleTimeZone.UTC_TIME,
                SimpleTimeZone.STANDARD_TIME,
                TimeZone.LONG));
        //seems RI doesn't check the startTimeMode and endTimeMode at all
        //this behavior is contradicts with spec
        assertNotNull(new SimpleTimeZone(
                TimeZone.LONG,
                "Europe/Paris",
                SimpleTimeZone.STANDARD_TIME,
                SimpleTimeZone.STANDARD_TIME,
                SimpleTimeZone.UTC_TIME,
                SimpleTimeZone.WALL_TIME,
                Integer.MAX_VALUE,
                TimeZone.SHORT,
                SimpleTimeZone.STANDARD_TIME,
                TimeZone.LONG,
                SimpleTimeZone.UTC_TIME,
                Integer.MIN_VALUE,
                TimeZone.LONG));
	}

	/**
	 * @tests java.util.SimpleTimeZone#clone()
	 */
	public void test_clone() {
		// Test for method java.lang.Object java.util.SimpleTimeZone.clone()
		SimpleTimeZone st1 = new SimpleTimeZone(1000, "TEST",
				Calendar.NOVEMBER, 1, Calendar.SUNDAY, 0, Calendar.NOVEMBER,
				-1, Calendar.SUNDAY, 0);
		SimpleTimeZone stA = new SimpleTimeZone(1, "Gah");
		assertTrue("Clone resulted in same reference", st1.clone() != st1);
		assertTrue("Clone resulted in unequal object", ((SimpleTimeZone) st1
				.clone()).equals(st1));
		assertTrue("Clone resulted in same reference", stA.clone() != stA);
		assertTrue("Clone resulted in unequal object", ((SimpleTimeZone) stA
				.clone()).equals(stA));
	}

	/**
	 * @tests java.util.SimpleTimeZone#getDSTSavings()
	 */
	public void test_getDSTSavings() {
		// Test for method int java.util.SimpleTimeZone.getDSTSavings()
		st1 = new SimpleTimeZone(0, "TEST");

		assertEquals("Non-zero default daylight savings",
				0, st1.getDSTSavings());
		st1.setStartRule(0, 1, 1, 1);
		st1.setEndRule(11, 1, 1, 1);

		assertEquals("Incorrect default daylight savings",
				3600000, st1.getDSTSavings());
		st1 = new SimpleTimeZone(-5 * 3600000, "EST", Calendar.APRIL, 1,
				-Calendar.SUNDAY, 2 * 3600000, Calendar.OCTOBER, -1,
				Calendar.SUNDAY, 2 * 3600000, 7200000);
		assertEquals("Incorrect daylight savings from constructor", 7200000, st1
				.getDSTSavings());

	}

	/**
	 * @tests java.util.SimpleTimeZone#getOffset(int, int, int, int, int, int)
	 *
	TODO(tball): getOffset broken in second test
	public void test_getOffsetIIIIII() {
		// Test for method int java.util.SimpleTimeZone.getOffset(int, int, int,
		// int, int, int)
        TimeZone st1 = TimeZone.getTimeZone("EST");
		assertTrue("Incorrect offset returned", st1.getOffset(
				GregorianCalendar.AD, 1998, Calendar.NOVEMBER, 11,
				Calendar.WEDNESDAY, 0) == -(5 * 60 * 60 * 1000));

        st1 = TimeZone.getTimeZone("EST");
        assertEquals("Incorrect offset returned", -(5 * 60 * 60 * 1000), st1
                .getOffset(GregorianCalendar.AD, 1998, Calendar.JUNE, 11,
                        Calendar.THURSDAY, 0));
        
        // Regression for HARMONY-5459
        st1 = (TimeZone)TimeZone.getDefault(); 
        int fourHours = 4*60*60*1000; 
        st1.setRawOffset(fourHours); 
        assertEquals(fourHours, st1.getOffset(1, 2099, 01, 1, 5, 0));

	}

	/**
   * @tests java.util.SimpleTimeZone#getRawOffset()
   */
	public void test_getRawOffset() {
		// Test for method int java.util.SimpleTimeZone.getRawOffset()
		st1 = (SimpleTimeZone) TimeZone.getTimeZone("EST");
		assertTrue("Incorrect offset returned",
				st1.getRawOffset() == -(5 * 60 * 60 * 1000));

	}

	/**
	 * @tests java.util.SimpleTimeZone#hasSameRules(java.util.TimeZone)
	 */
	public void test_hasSameRulesLjava_util_TimeZone() {
		// Test for method boolean
		// java.util.SimpleTimeZone.hasSameRules(java.util.TimeZone)
		SimpleTimeZone st = new SimpleTimeZone(1000, "TEST", Calendar.NOVEMBER,
				1, Calendar.SUNDAY, 0, Calendar.NOVEMBER, -1, Calendar.SUNDAY,
				0);
		SimpleTimeZone sameAsSt = new SimpleTimeZone(1000, "REST",
				Calendar.NOVEMBER, 1, Calendar.SUNDAY, 0, Calendar.NOVEMBER,
				-1, Calendar.SUNDAY, 0);
		SimpleTimeZone notSameAsSt = new SimpleTimeZone(1000, "PEST",
				Calendar.NOVEMBER, 2, Calendar.SUNDAY, 0, Calendar.NOVEMBER,
				-1, Calendar.SUNDAY, 0);
		assertTrue("Time zones have same rules but return false", st
				.hasSameRules(sameAsSt));
		assertTrue("Time zones have different rules but return true", !st
				.hasSameRules(notSameAsSt));
	}

	/**
	 * @tests java.util.SimpleTimeZone#inDaylightTime(java.util.Date)
	 *
	TODO(tball): enable when in daylight time tables are added.
	public void test_inDaylightTimeLjava_util_Date() {
		// Test for method boolean
		// java.util.SimpleTimeZone.inDaylightTime(java.util.Date)
        TimeZone zone = TimeZone.getTimeZone("EST");
		GregorianCalendar gc = new GregorianCalendar(1998, Calendar.JUNE, 11);
		assertFalse("Returned incorrect daylight value1", zone.inDaylightTime(gc
				.getTime()));
		gc = new GregorianCalendar(1998, Calendar.NOVEMBER, 11);
        assertFalse("Returned incorrect daylight value1", zone.inDaylightTime(gc
                .getTime()));
		gc = new GregorianCalendar(zone);
		gc.set(1999, Calendar.APRIL, 4, 1, 59, 59);
		assertTrue("Returned incorrect daylight value3", !(zone
				.inDaylightTime(gc.getTime())));
		Date date = new Date(gc.getTime().getTime() + 1000);
		assertFalse("Returned incorrect daylight value4", zone
				.inDaylightTime(date));
		gc.set(1999, Calendar.OCTOBER, 31, 1, 0, 0);
		assertTrue("Returned incorrect daylight value5", !(zone
				.inDaylightTime(gc.getTime())));
		date = new Date(gc.getTime().getTime() - 1000);
		assertFalse("Returned incorrect daylight value6", zone
				.inDaylightTime(date));

		assertTrue("Returned incorrect daylight value7", !zone
				.inDaylightTime(new Date(891752400000L + 7200000 - 1)));
		assertFalse("Returned incorrect daylight value8", zone
				.inDaylightTime(new Date(891752400000L + 7200000)));
		assertFalse("Returned incorrect daylight value9", zone
				.inDaylightTime(new Date(909288000000L + 7200000 - 1)));
		assertTrue("Returned incorrect daylight value10", !zone
				.inDaylightTime(new Date(909288000000L + 7200000)));
	}

	/**
	 * @tests java.util.SimpleTimeZone#setDSTSavings(int)
	 */
	public void test_setDSTSavingsI() {
		// Test for method void java.util.SimpleTimeZone.setDSTSavings(int)
		SimpleTimeZone st = new SimpleTimeZone(1000, "Test_TZ");
		st.setStartRule(0, 1, 1, 1);
		st.setEndRule(11, 1, 1, 1);
		st.setDSTSavings(1);
		assertEquals("Daylight savings amount not set", 1, st.getDSTSavings());
	}

	/**
	 * @tests java.util.SimpleTimeZone#setEndRule(int, int, int)
	 */
	public void test_setEndRuleIII() {
		// Test for method void java.util.SimpleTimeZone.setEndRule(int, int,
		// int)
		assertTrue("Used to test", true);
	}

	/**
	 * @tests java.util.SimpleTimeZone#setEndRule(int, int, int, int)
	 */
	public void test_setEndRuleIIII() {
		// Test for method void java.util.SimpleTimeZone.setEndRule(int, int,
		// int, int)
		SimpleTimeZone st = new SimpleTimeZone(1000, "Test_TZ");
		// Spec indicates that both end and start must be set or result is
		// undefined
		st.setStartRule(Calendar.NOVEMBER, 1, Calendar.SUNDAY, 0);
		st.setEndRule(Calendar.NOVEMBER, -1, Calendar.SUNDAY, 0);
		assertTrue("StartRule improperly set1", st.useDaylightTime());
		assertTrue("StartRule improperly set2", st
				.inDaylightTime(new GregorianCalendar(1998, Calendar.NOVEMBER,
						13).getTime()));
		assertTrue("StartRule improperly set3", !(st
				.inDaylightTime(new GregorianCalendar(1998, Calendar.OCTOBER,
						13).getTime())));
	}

	/**
	 * @tests java.util.SimpleTimeZone#setEndRule(int, int, int, int, boolean)
	 */
	public void test_setEndRuleIIIIZ() {
		// Test for method void java.util.SimpleTimeZone.setEndRule(int, int,
		// int, int, boolean)
        SimpleTimeZone st = new SimpleTimeZone(1000, "Test_TZ");
		// Spec indicates that both end and start must be set or result is
		// undefined
		st.setStartRule(Calendar.NOVEMBER, 8, Calendar.SUNDAY, 1, false);
		st.setEndRule(Calendar.NOVEMBER, 15, Calendar.SUNDAY, 1, true);
		assertTrue("StartRule improperly set1", st.useDaylightTime());
		assertTrue("StartRule improperly set2", st
				.inDaylightTime((new GregorianCalendar(1999, Calendar.NOVEMBER,
						7, 12, 0).getTime())));
		assertTrue("StartRule improperly set3", st
				.inDaylightTime((new GregorianCalendar(1999, Calendar.NOVEMBER,
						20, 12, 0).getTime())));
		assertTrue("StartRule improperly set4", !(st
				.inDaylightTime(new GregorianCalendar(1999, Calendar.NOVEMBER,
						6, 12, 0).getTime())));
		assertTrue("StartRule improperly set5", !(st
				.inDaylightTime(new GregorianCalendar(1999, Calendar.NOVEMBER,
						21, 12, 0).getTime())));
	}

	/**
	 * @tests java.util.SimpleTimeZone#setRawOffset(int)
	 */
	public void test_setRawOffsetI() {
		// Test for method void java.util.SimpleTimeZone.setRawOffset(int)

		st1 = (SimpleTimeZone) TimeZone.getTimeZone("EST");
		int off = st1.getRawOffset();
		st1.setRawOffset(1000);
		boolean val = st1.getRawOffset() == 1000;
		st1.setRawOffset(off);
		assertTrue("Incorrect offset set", val);
	}

	/**
	 * @tests java.util.SimpleTimeZone#setStartRule(int, int, int)
	 */
	public void test_setStartRuleIII() {
		// Test for method void java.util.SimpleTimeZone.setStartRule(int, int,
		// int)
		SimpleTimeZone st = new SimpleTimeZone(1000, "Test_TZ");
		// Spec indicates that both end and start must be set or result is
		// undefined
		st.setStartRule(Calendar.NOVEMBER, 1, 1);
		st.setEndRule(Calendar.DECEMBER, 1, 1);
		assertTrue("StartRule improperly set", st.useDaylightTime());
		assertTrue("StartRule improperly set", st
				.inDaylightTime((new GregorianCalendar(1998, Calendar.NOVEMBER,
						13).getTime())));
		assertTrue("StartRule improperly set", !(st
				.inDaylightTime(new GregorianCalendar(1998, Calendar.OCTOBER,
						13).getTime())));

	}

	/**
	 * @tests java.util.SimpleTimeZone#setStartRule(int, int, int, int)
	 */
	public void test_setStartRuleIIII() {
		// Test for method void java.util.SimpleTimeZone.setStartRule(int, int,
		// int, int)
		SimpleTimeZone st = new SimpleTimeZone(1000, "Test_TZ");
		// Spec indicates that both end and start must be set or result is
		// undefined
		st.setStartRule(Calendar.NOVEMBER, 1, Calendar.SUNDAY, 0);
		st.setEndRule(Calendar.NOVEMBER, -1, Calendar.SUNDAY, 0);
		assertTrue("StartRule improperly set1", st.useDaylightTime());
		assertTrue("StartRule improperly set2", st
				.inDaylightTime((new GregorianCalendar(1998, Calendar.NOVEMBER,
						13).getTime())));
		assertTrue("StartRule improperly set3", !(st
				.inDaylightTime(new GregorianCalendar(1998, Calendar.OCTOBER,
						13).getTime())));
	}

	/**
	 * @tests java.util.SimpleTimeZone#setStartRule(int, int, int, int, boolean)
	 */
	public void test_setStartRuleIIIIZ() {
		// Test for method void java.util.SimpleTimeZone.setStartRule(int, int,
		// int, int, boolean)
        SimpleTimeZone st = new SimpleTimeZone(0, "Test");
		// Spec indicates that both end and start must be set or result is
		// undefined
		st.setStartRule(Calendar.NOVEMBER, 1, Calendar.SUNDAY, 1, true);
		st.setEndRule(Calendar.NOVEMBER, 15, Calendar.SUNDAY, 1, false);
		assertTrue("StartRule improperly set1", st.useDaylightTime());
		assertTrue("StartRule improperly set2", st
				.inDaylightTime((new GregorianCalendar(1999, Calendar.NOVEMBER,
						7, 12, 0).getTime())));
		assertTrue("StartRule improperly set3", st
				.inDaylightTime((new GregorianCalendar(1999, Calendar.NOVEMBER,
						13, 12, 0).getTime())));
		assertTrue("StartRule improperly set4", !(st
				.inDaylightTime(new GregorianCalendar(1999, Calendar.NOVEMBER,
						6, 12, 0).getTime())));
		assertTrue("StartRule improperly set5", !(st
				.inDaylightTime(new GregorianCalendar(1999, Calendar.NOVEMBER,
						14, 12, 0).getTime())));
	}

	/**
	 * @tests java.util.SimpleTimeZone#setStartYear(int)
	 */
	public void test_setStartYearI() {
		// Test for method void java.util.SimpleTimeZone.setStartYear(int)
		SimpleTimeZone st = new SimpleTimeZone(1000, "Test_TZ");
		st.setStartRule(Calendar.NOVEMBER, 1, Calendar.SUNDAY, 0);
		st.setEndRule(Calendar.NOVEMBER, -1, Calendar.SUNDAY, 0);
		st.setStartYear(1999);
		assertTrue("set year improperly set1", !(st
				.inDaylightTime(new GregorianCalendar(1999, Calendar.JULY, 12)
						.getTime())));
		assertTrue("set year improperly set2", !(st
				.inDaylightTime(new GregorianCalendar(1998, Calendar.OCTOBER,
						13).getTime())));
		assertTrue("set year improperly set3", (st
				.inDaylightTime(new GregorianCalendar(1999, Calendar.NOVEMBER,
						13).getTime())));
	}

	/**
	 * @tests java.util.SimpleTimeZone#toString()
	 */
	public void test_toString() {
		// Test for method java.lang.String java.util.SimpleTimeZone.toString()
		String string = TimeZone.getTimeZone("EST").toString();
		assertNotNull("toString() returned null", string);
		assertTrue("toString() is empty", string.length() != 0);
	}

	/**
	 * @tests java.util.SimpleTimeZone#useDaylightTime()
	 */
	public void test_useDaylightTime() {
		// Test for method boolean java.util.SimpleTimeZone.useDaylightTime()
		SimpleTimeZone st = new SimpleTimeZone(1000, "Test_TZ");
		assertTrue("useDaylightTime returned incorrect value", !st
				.useDaylightTime());
		// Spec indicates that both end and start must be set or result is
		// undefined
		st.setStartRule(Calendar.NOVEMBER, 1, Calendar.SUNDAY, 0);
		st.setEndRule(Calendar.NOVEMBER, -1, Calendar.SUNDAY, 0);
		assertTrue("useDaylightTime returned incorrect value", st
				.useDaylightTime());
	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
	}
}
