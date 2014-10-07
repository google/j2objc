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
import java.util.Vector;

public class NoSuchElementExceptionTest extends junit.framework.TestCase {

	/**
	 * @tests java.util.NoSuchElementException#NoSuchElementException()
	 */
	public void test_Constructor() {
		// Test for method java.util.NoSuchElementException()

		try {
			Vector v = new Vector();
			v.elements().nextElement();
		} catch (NoSuchElementException e) {
			return;
		}
		// if we make it to here, assert a fail
		fail("Failed to catch expected Exception");
	}

	/**
	 * @tests java.util.NoSuchElementException#NoSuchElementException(java.lang.String)
	 */
	public void test_ConstructorLjava_lang_String() {
		// Test for method java.util.NoSuchElementException(java.lang.String)

		try {
			Vector v = new Vector();
			v.firstElement();
		} catch (NoSuchElementException e) {
			return;
		}
		// if we make it to here, assert a fail
		fail("Failed to catch Exception");
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
