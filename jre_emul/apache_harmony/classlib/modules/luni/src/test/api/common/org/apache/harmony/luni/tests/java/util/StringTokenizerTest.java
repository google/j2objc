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

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class StringTokenizerTest extends junit.framework.TestCase {

	/**
	 * @tests java.util.StringTokenizer#StringTokenizer(java.lang.String)
	 */
	public void test_ConstructorLjava_lang_String() {
		// Test for method java.util.StringTokenizer(java.lang.String)
		assertTrue("Used in tests", true);
	}

	/**
	 * @tests java.util.StringTokenizer#StringTokenizer(java.lang.String,
	 *        java.lang.String)
	 */
	public void test_ConstructorLjava_lang_StringLjava_lang_String() {
		// Test for method java.util.StringTokenizer(java.lang.String,
		// java.lang.String)
		StringTokenizer st = new StringTokenizer("This:is:a:test:String", ":");
		assertTrue("Created incorrect tokenizer", st.countTokens() == 5
				&& (st.nextElement().equals("This")));
	}

	/**
	 * @tests java.util.StringTokenizer#StringTokenizer(java.lang.String,
	 *        java.lang.String, boolean)
	 */
	public void test_ConstructorLjava_lang_StringLjava_lang_StringZ() {
		// Test for method java.util.StringTokenizer(java.lang.String,
		// java.lang.String, boolean)
		StringTokenizer st = new StringTokenizer("This:is:a:test:String", ":",
				true);
		st.nextElement();
		assertTrue("Created incorrect tokenizer", st.countTokens() == 8
				&& (st.nextElement().equals(":")));
	}

	/**
	 * @tests java.util.StringTokenizer#countTokens()
	 */
	public void test_countTokens() {
		// Test for method int java.util.StringTokenizer.countTokens()
		StringTokenizer st = new StringTokenizer("This is a test String");

		assertEquals("Incorrect token count returned", 5, st.countTokens());
	}

	/**
	 * @tests java.util.StringTokenizer#hasMoreElements()
	 */
	public void test_hasMoreElements() {
		// Test for method boolean java.util.StringTokenizer.hasMoreElements()

		StringTokenizer st = new StringTokenizer("This is a test String");
		st.nextElement();
		assertTrue("hasMoreElements returned incorrect value", st
				.hasMoreElements());
		st.nextElement();
		st.nextElement();
		st.nextElement();
		st.nextElement();
		assertTrue("hasMoreElements returned incorrect value", !st
				.hasMoreElements());
	}

	/**
	 * @tests java.util.StringTokenizer#hasMoreTokens()
	 */
	public void test_hasMoreTokens() {
		// Test for method boolean java.util.StringTokenizer.hasMoreTokens()
		StringTokenizer st = new StringTokenizer("This is a test String");
		for (int counter = 0; counter < 5; counter++) {
			assertTrue(
					"StringTokenizer incorrectly reports it has no more tokens",
					st.hasMoreTokens());
			st.nextToken();
		}
		assertTrue("StringTokenizer incorrectly reports it has more tokens",
				!st.hasMoreTokens());
	}

	/**
	 * @tests java.util.StringTokenizer#nextElement()
	 */
	public void test_nextElement() {
		// Test for method java.lang.Object
		// java.util.StringTokenizer.nextElement()
		StringTokenizer st = new StringTokenizer("This is a test String");
		assertEquals("nextElement returned incorrect value", "This", ((String) st
				.nextElement()));
		assertEquals("nextElement returned incorrect value", "is", ((String) st
				.nextElement()));
		assertEquals("nextElement returned incorrect value", "a", ((String) st
				.nextElement()));
		assertEquals("nextElement returned incorrect value", "test", ((String) st
				.nextElement()));
		assertEquals("nextElement returned incorrect value", "String", ((String) st
				.nextElement()));
		try {
			st.nextElement();
			fail(
					"nextElement failed to throw a NoSuchElementException when it should have been out of elements");
		} catch (NoSuchElementException e) {
			return;
		}
	}

	/**
	 * @tests java.util.StringTokenizer#nextToken()
	 */
	public void test_nextToken() {
		// Test for method java.lang.String
		// java.util.StringTokenizer.nextToken()
		StringTokenizer st = new StringTokenizer("This is a test String");
		assertEquals("nextToken returned incorrect value", 
				"This", st.nextToken());
		assertEquals("nextToken returned incorrect value", 
				"is", st.nextToken());
		assertEquals("nextToken returned incorrect value", 
				"a", st.nextToken());
		assertEquals("nextToken returned incorrect value", 
				"test", st.nextToken());
		assertEquals("nextToken returned incorrect value", 
				"String", st.nextToken());
		try {
			st.nextToken();
			fail(
					"nextToken failed to throw a NoSuchElementException when it should have been out of elements");
		} catch (NoSuchElementException e) {
			return;
		}
	}

	/**
	 * @tests java.util.StringTokenizer#nextToken(java.lang.String)
	 */
	public void test_nextTokenLjava_lang_String() {
		// Test for method java.lang.String
		// java.util.StringTokenizer.nextToken(java.lang.String)
		StringTokenizer st = new StringTokenizer("This is a test String");
		assertEquals("nextToken(String) returned incorrect value with normal token String",
				"This", st.nextToken(" "));
		assertEquals("nextToken(String) returned incorrect value with custom token String",
				" is a ", st.nextToken("tr"));
		assertEquals("calling nextToken() did not use the new default delimiter list",
				"es", st.nextToken());
	}

    public void test_hasMoreElements_NPE() {
        StringTokenizer stringTokenizer = new StringTokenizer(new String(),
                (String) null, true);
        try {
            stringTokenizer.hasMoreElements();
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }

        stringTokenizer = new StringTokenizer(new String(), (String) null);
        try {
            stringTokenizer.hasMoreElements();
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    public void test_hasMoreTokens_NPE() {
        StringTokenizer stringTokenizer = new StringTokenizer(new String(),
                (String) null, true);
        try {
            stringTokenizer.hasMoreTokens();
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }

        stringTokenizer = new StringTokenizer(new String(), (String) null);
        try {
            stringTokenizer.hasMoreTokens();
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    public void test_nextElement_NPE() {
        StringTokenizer stringTokenizer = new StringTokenizer(new String(),
                (String) null, true);
        try {
            stringTokenizer.nextElement();
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }

        stringTokenizer = new StringTokenizer(new String(), (String) null);
        try {
            stringTokenizer.nextElement();
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    public void test_nextToken_NPE() {
        StringTokenizer stringTokenizer = new StringTokenizer(new String(),
                (String) null, true);
        try {
            stringTokenizer.nextToken();
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }

        stringTokenizer = new StringTokenizer(new String(), (String) null);
        try {
            stringTokenizer.nextToken();
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    public void test_nextTokenLjava_lang_String_NPE() {
        StringTokenizer stringTokenizer = new StringTokenizer(new String());
        try {
            stringTokenizer.nextToken(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
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
