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
package org.apache.harmony.luni.tests.java.lang;

public class ObjectTest extends junit.framework.TestCase {

	/**
	 * Test objects.
	 */
	Object obj1 = new Object();

	Object obj2 = new Object();

	/**
	 * Generic state indicator.
	 */
	int status = 0;

	int ready = 0;

	/**
	 * @tests java.lang.Object#Object()
	 */
	public void test_Constructor() {
		// Test for method java.lang.Object()
		assertNotNull("Constructor failed !!!", new Object());
	}

	/**
	 * @tests java.lang.Object#equals(java.lang.Object)
	 */
	public void test_equalsLjava_lang_Object() {
		// Test for method boolean java.lang.Object.equals(java.lang.Object)
		assertTrue("Same object should be equal", obj1.equals(obj1));
		assertTrue("Different objects should not be equal", !obj1.equals(obj2));
	}

	/**
	 * @tests java.lang.Object#hashCode()
	 */
	public void test_hashCode() {
		// Test for method int java.lang.Object.hashCode()
		assertTrue("Same object should have same hash.",
				obj1.hashCode() == obj1.hashCode());
		assertTrue("Same object should have same hash.",
				obj2.hashCode() == obj2.hashCode());
	}

	/**
	 * @tests java.lang.Object#toString()
	 */
	public void test_toString() {
		// Test for method java.lang.String java.lang.Object.toString()
		assertNotNull("Object toString returned null.", obj1.toString());
	}
}
