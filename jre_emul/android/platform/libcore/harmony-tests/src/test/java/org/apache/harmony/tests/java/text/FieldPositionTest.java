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
import java.text.FieldPosition;

public class FieldPositionTest extends junit.framework.TestCase {

	/**
	 * @tests java.text.FieldPosition#FieldPosition(int)
	 */
	public void test_ConstructorI() {
		// Test for constructor java.text.FieldPosition(int)
		FieldPosition fpos = new FieldPosition(DateFormat.MONTH_FIELD);
		assertEquals("Test1: Constructor failed to set field identifier!",
				DateFormat.MONTH_FIELD, fpos.getField());
		assertNull("Constructor failed to set field attribute!", fpos
				.getFieldAttribute());
	}

	/**
	 * @tests java.text.FieldPosition#FieldPosition(java.text.Format$Field)
	 */
	public void test_ConstructorLjava_text_Format$Field() {
		// Test for constructor java.text.FieldPosition(Format.Field)
		FieldPosition fpos = new FieldPosition(DateFormat.Field.MONTH);
		assertSame("Constructor failed to set field attribute!",
				DateFormat.Field.MONTH, fpos.getFieldAttribute());
		assertEquals("Test1: Constructor failed to set field identifier!", -1,
				fpos.getField());
	}

	/**
	 * @tests java.text.FieldPosition#FieldPosition(java.text.Format$Field, int)
	 */
	public void test_ConstructorLjava_text_Format$FieldI() {
		// Test for constructor java.text.FieldPosition(Format.Field, int)
		FieldPosition fpos = new FieldPosition(DateFormat.Field.MONTH,
				DateFormat.MONTH_FIELD);
		assertSame("Constructor failed to set field attribute!",
				DateFormat.Field.MONTH, fpos.getFieldAttribute());
		assertEquals("Test1: Constructor failed to set field identifier!",
				DateFormat.MONTH_FIELD, fpos.getField());

		// test special cases
		FieldPosition fpos2 = new FieldPosition(DateFormat.Field.HOUR1,
				DateFormat.HOUR1_FIELD);
		assertSame("Constructor failed to set field attribute!",
				DateFormat.Field.HOUR1, fpos2.getFieldAttribute());
		assertEquals("Test2: Constructor failed to set field identifier!",
				DateFormat.HOUR1_FIELD, fpos2.getField());

		FieldPosition fpos3 = new FieldPosition(DateFormat.Field.TIME_ZONE,
				DateFormat.MONTH_FIELD);
		assertSame("Constructor failed to set field attribute!",
				DateFormat.Field.TIME_ZONE, fpos3.getFieldAttribute());
		assertEquals("Test3: Constructor failed to set field identifier!",
				DateFormat.MONTH_FIELD, fpos3.getField());
	}

	/**
	 * @tests java.text.FieldPosition#equals(java.lang.Object)
	 */
	public void test_equalsLjava_lang_Object() {
		// Test for method boolean
		// java.text.FieldPosition.equals(java.lang.Object)
		FieldPosition fpos = new FieldPosition(1);
		FieldPosition fpos1 = new FieldPosition(1);
		assertTrue("Identical objects were not equal!", fpos.equals(fpos1));

		FieldPosition fpos2 = new FieldPosition(2);
		assertTrue("Objects with a different ID should not be equal!", !fpos
				.equals(fpos2));

		fpos.setBeginIndex(1);
		fpos1.setBeginIndex(2);
		assertTrue("Objects with a different beginIndex were still equal!",
				!fpos.equals(fpos1));
		fpos1.setBeginIndex(1);
		fpos1.setEndIndex(2);
		assertTrue("Objects with a different endIndex were still equal!", !fpos
				.equals(fpos1));

		FieldPosition fpos3 = new FieldPosition(DateFormat.Field.ERA, 1);
		assertTrue("Objects with a different attribute should not be equal!",
				!fpos.equals(fpos3));
		FieldPosition fpos4 = new FieldPosition(DateFormat.Field.AM_PM, 1);
		assertTrue("Objects with a different attribute should not be equal!",
				!fpos3.equals(fpos4));
	}

	/**
	 * @tests java.text.FieldPosition#getBeginIndex()
	 */
	public void test_getBeginIndex() {
		// Test for method int java.text.FieldPosition.getBeginIndex()
		FieldPosition fpos = new FieldPosition(1);
		fpos.setEndIndex(3);
		fpos.setBeginIndex(2);
		assertEquals("getBeginIndex should have returned 2",
				2, fpos.getBeginIndex());
	}

	/**
	 * @tests java.text.FieldPosition#getEndIndex()
	 */
	public void test_getEndIndex() {
		// Test for method int java.text.FieldPosition.getEndIndex()
		FieldPosition fpos = new FieldPosition(1);
		fpos.setBeginIndex(2);
		fpos.setEndIndex(3);
		assertEquals("getEndIndex should have returned 3",
				3, fpos.getEndIndex());
	}

	/**
	 * @tests java.text.FieldPosition#getField()
	 */
	public void test_getField() {
		// Test for method int java.text.FieldPosition.getField()
		FieldPosition fpos = new FieldPosition(65);
		assertEquals("FieldPosition(65) should have caused getField to return 65",
				65, fpos.getField());
		FieldPosition fpos2 = new FieldPosition(DateFormat.Field.MINUTE);
		assertEquals("FieldPosition(DateFormat.Field.MINUTE) should have caused getField to return -1",
				-1, fpos2.getField());
	}

	/**
	 * @tests java.text.FieldPosition#getFieldAttribute()
	 */
	public void test_getFieldAttribute() {
		// Test for method int java.text.FieldPosition.getFieldAttribute()
		FieldPosition fpos = new FieldPosition(DateFormat.Field.TIME_ZONE);
		assertTrue(
				"FieldPosition(DateFormat.Field.TIME_ZONE) should have caused getFieldAttribute to return DateFormat.Field.TIME_ZONE",
				fpos.getFieldAttribute() == DateFormat.Field.TIME_ZONE);

		FieldPosition fpos2 = new FieldPosition(DateFormat.TIMEZONE_FIELD);
		assertNull(
				"FieldPosition(DateFormat.TIMEZONE_FIELD) should have caused getFieldAttribute to return null",
				fpos2.getFieldAttribute());
	}

	public void test_hashCode() {
	  // Test for method int java.text.FieldPosition.hashCode()
	  FieldPosition fpos1 = new FieldPosition(1);
	  FieldPosition fpos2 = new FieldPosition(1);
	  assertTrue("test 1: hash codes are not equal for equal objects.",
	             fpos1.hashCode() == fpos2.hashCode());
	  fpos1.setBeginIndex(5);
	  fpos1.setEndIndex(110);
	  assertTrue("test 2: hash codes are equal for non equal objects.",
	             fpos1.hashCode() != fpos2.hashCode());
	  fpos2.setBeginIndex(5);
	  fpos2.setEndIndex(110);
	  assertTrue("test 3: hash codes are not equal for equal objects.",
	             fpos1.hashCode() == fpos2.hashCode());

	  FieldPosition fpos3 = new FieldPosition(
	      DateFormat.Field.DAY_OF_WEEK_IN_MONTH);

	  assertTrue("test 4: hash codes are equal for non equal objects.",
	             fpos2.hashCode() != fpos3.hashCode());
	}

	public void test_setBeginIndexI() {
	  FieldPosition fpos = new FieldPosition(1);
	  fpos.setBeginIndex(2);
	  fpos.setEndIndex(3);
	  assertEquals("beginIndex should have been set to 2", 2, fpos
	                   .getBeginIndex());

	  fpos.setBeginIndex(Integer.MAX_VALUE);
	  assertEquals("beginIndex should have been set to Integer.MAX_VALUE",
	               Integer.MAX_VALUE, fpos.getBeginIndex());

	  fpos.setBeginIndex(-1);
	  assertEquals("beginIndex should have been set to -1",
	               -1, fpos.getBeginIndex());
	}

	public void test_setEndIndexI() {
	  FieldPosition fpos = new FieldPosition(1);
	  fpos.setEndIndex(3);
	  fpos.setBeginIndex(2);
	  assertEquals("EndIndex should have been set to 3", 3, fpos
	                   .getEndIndex());

	  fpos.setEndIndex(Integer.MAX_VALUE);
	  assertEquals("endIndex should have been set to Integer.MAX_VALUE",
	               Integer.MAX_VALUE, fpos.getEndIndex());

	  fpos.setEndIndex(-1);
	  assertEquals("endIndex should have been set to -1",
	               -1, fpos.getEndIndex());
	}

	/**
	 * @tests java.text.FieldPosition#toString()
	 */
	public void test_toString() {
		// Test for method java.lang.String java.text.FieldPosition.toString()
		FieldPosition fpos = new FieldPosition(1);
		fpos.setBeginIndex(2);
		fpos.setEndIndex(3);
		assertEquals(
				"ToString returned the wrong value:",
				"java.text.FieldPosition[field=1,attribute=null,beginIndex=2,endIndex=3]",
				fpos.toString());

		FieldPosition fpos2 = new FieldPosition(DateFormat.Field.ERA);
		fpos2.setBeginIndex(4);
		fpos2.setEndIndex(5);
		assertEquals("ToString returned the wrong value:",
				"java.text.FieldPosition[field=-1,attribute=" + DateFormat.Field.ERA
						+ ",beginIndex=4,endIndex=5]", fpos2.toString());
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
