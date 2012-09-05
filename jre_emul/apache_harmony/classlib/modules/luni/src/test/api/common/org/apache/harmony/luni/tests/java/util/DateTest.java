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

import java.util.Date;

public class DateTest extends junit.framework.TestCase {

	/**
	 * @tests java.util.Date#Date(long)
	 */
	public void test_ConstructorJ() {
		// Test for method java.util.Date(long)
        Date date = new Date(1000L);
        assertNotNull(date);
	}

	/**
	 * @tests java.util.Date#Date(java.lang.String)
	 */
	public void test_ConstructorLjava_lang_String() {
		// Test for method java.util.Date(java.lang.String)
		Date d1 = new Date("January 1, 1970, 00:00:00 GMT"); // the epoch
		Date d2 = new Date(0); // the epoch
		assertTrue("Created incorrect date", d1.equals(d2));
        
		try {
			// Regression for HARMONY-238
			new Date(null);
			fail("Constructor Date((String)null) should "
				+ "throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	/**
	 * @tests java.util.Date#after(java.util.Date)
	 */
	public void test_afterLjava_util_Date() {
		// Test for method boolean java.util.Date.after(java.util.Date)
		Date d1 = new Date(0);
		Date d2 = new Date(1900000);
		assertTrue("Older was returned as newer", d2.after(d1));
		assertTrue("Newer was returned as older", !d1.after(d2));
	}

	/**
	 * @tests java.util.Date#before(java.util.Date)
	 */
	public void test_beforeLjava_util_Date() {
		// Test for method boolean java.util.Date.before(java.util.Date)
		Date d1 = new Date(0);
		Date d2 = new Date(1900000);
		assertTrue("Older was returned as newer", !d2.before(d1));
		assertTrue("Newer was returned as older", d1.before(d2));
	}

	/**
	 * @tests java.util.Date#clone()
	 */
	public void test_clone() {
		// Test for method java.lang.Object java.util.Date.clone()
		Date d1 = new Date(100000);
		Date d2 = (Date) d1.clone();
		assertTrue(
				"Cloning date results in same reference--new date is equivalent",
				d1 != d2);
		assertTrue("Cloning date results unequal date", d1.equals(d2));
	}

	/**
	 * @tests java.util.Date#compareTo(java.util.Date)
	 */
	public void test_compareToLjava_util_Date() {
		// Test for method int java.util.Date.compareTo(java.util.Date)
		final int someNumber = 10000;
		Date d1 = new Date(someNumber);
		Date d2 = new Date(someNumber);
		Date d3 = new Date(someNumber + 1);
		Date d4 = new Date(someNumber - 1);
		assertEquals("Comparing a date to itself did not answer zero", 0, d1
				.compareTo(d1));
		assertEquals("Comparing equal dates did not answer zero", 0, d1
				.compareTo(d2));
		assertEquals("date1.compareTo(date2), where date1 > date2, did not result in 1",
				1, d1.compareTo(d4));
		assertEquals("date1.compareTo(date2), where date1 < date2, did not result in -1",
				-1, d1.compareTo(d3));

	}

	/**
	 * @tests java.util.Date#equals(java.lang.Object)
	 */
	public void test_equalsLjava_lang_Object() {
		// Test for method boolean java.util.Date.equals(java.lang.Object)
		Date d1 = new Date(0);
		Date d2 = new Date(1900000);
		Date d3 = new Date(1900000);
		assertTrue("Equality test failed", d2.equals(d3));
		assertTrue("Equality test failed", !d1.equals(d2));
	}

	/**
	 * @tests java.util.Date#getTime()
	 */
	public void test_getTime() {
		// Test for method long java.util.Date.getTime()
		Date d1 = new Date(0);
		Date d2 = new Date(1900000);
		assertEquals("Returned incorrect time", 1900000, d2.getTime());
		assertEquals("Returned incorrect time", 0, d1.getTime());
	}

	/**
	 * @tests java.util.Date#hashCode()
	 */
	public void test_hashCode() {
		// Test for method int java.util.Date.hashCode()
		Date d1 = new Date(0);
		Date d2 = new Date(1900000);
		assertEquals("Returned incorrect hash", 1900000, d2.hashCode());
		assertEquals("Returned incorrect hash", 0, d1.hashCode());
	}

	/**
	 * @tests java.util.Date#UTC(int, int, int, int, int, int)
	 */
	public void test_UTCIIIIII() {
		// Test for method long java.util.Date.UTC(int, int, int, int, int, int)
		assertTrue("Returned incorrect UTC value for epoch", Date.UTC(70, 0, 1,
				0, 0, 0) == (long) 0);
		assertTrue("Returned incorrect UTC value for epoch +1yr", Date.UTC(71,
				0, 1, 0, 0, 0) == (long) 365 * 24 * 60 * 60 * 1000);
	}
}
