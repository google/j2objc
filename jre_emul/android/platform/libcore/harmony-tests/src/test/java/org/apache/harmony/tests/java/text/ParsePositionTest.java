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

import java.text.ParsePosition;

public class ParsePositionTest extends junit.framework.TestCase {

	ParsePosition pp;

    /**
     * @tests java.text.ParsePosition#ParsePosition(int) 
     */
    public void test_ConstructorI() {
        // Test for method java.text.ParsePosition(int)
        ParsePosition pp1 = new ParsePosition(Integer.MIN_VALUE);
        assertTrue("Initialization failed.", pp1.getIndex() == Integer.MIN_VALUE);
        assertEquals("Initialization failed.", -1, pp1.getErrorIndex());
    }

	/**
	 * @tests java.text.ParsePosition#equals(java.lang.Object)
	 */
	public void test_equalsLjava_lang_Object() {
		// Test for method boolean
		// java.text.ParsePosition.equals(java.lang.Object)
		ParsePosition pp2 = new ParsePosition(43);
		pp2.setErrorIndex(56);
		assertTrue("equals failed.", !pp.equals(pp2));
		pp.setErrorIndex(56);
		pp.setIndex(43);
		assertTrue("equals failed.", pp.equals(pp2));
	}

	/**
	 * @tests java.text.ParsePosition#getErrorIndex()
	 */
	public void test_getErrorIndex() {
		// Test for method int java.text.ParsePosition.getErrorIndex()
		pp.setErrorIndex(56);
		assertEquals("getErrorIndex failed.", 56, pp.getErrorIndex());
	}

	/**
	 * @tests java.text.ParsePosition#getIndex()
	 */
	public void test_getIndex() {
		// Test for method int java.text.ParsePosition.getIndex()
		assertTrue("getIndex failed.", pp.getIndex() == Integer.MAX_VALUE);
	}

	/**
	 * @tests java.text.ParsePosition#hashCode()
	 */
	public void test_hashCode() {
		ParsePosition pp2 = new ParsePosition(Integer.MAX_VALUE);
		// Test for method int java.text.ParsePosition.hashCode()
                // Test that the hashcode is stable (and not the identity hashCode).
		assertEquals(pp.hashCode(), pp2.hashCode());
	}

	/**
	 * @tests java.text.ParsePosition#setErrorIndex(int)
	 */
	public void test_setErrorIndexI() {
		// Test for method void java.text.ParsePosition.setErrorIndex(int)
		pp.setErrorIndex(4564);
		assertEquals("setErrorIndex failed.", 4564, pp.getErrorIndex());
	}

	/**
	 * @tests java.text.ParsePosition#setIndex(int)
	 */
	public void test_setIndexI() {
		// Test for method void java.text.ParsePosition.setIndex(int)
		pp.setIndex(4564);
		assertEquals("setErrorIndex failed.", 4564, pp.getIndex());
	}

	/**
	 * @tests java.text.ParsePosition#toString()
	 */
	public void test_toString() {
		// Test for method java.lang.String java.text.ParsePosition.toString()
		assertEquals("toString failed.", 
				"java.text.ParsePosition[index=2147483647,errorIndex=-1]", pp.toString());
	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {

		pp = new ParsePosition(Integer.MAX_VALUE);
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
	}
}
