/* Licensed to the Apache Software Foundation (ASF) under one or more
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

package org.apache.harmony.luni.tests.java.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @tests java.util.LinkedHashSet
 */
public class LinkedHashSetTest extends junit.framework.TestCase {

	LinkedHashSet hs;

	static Object[] objArray;
	{
		objArray = new Object[1000];
		for (int i = 0; i < objArray.length; i++)
			objArray[i] = new Integer(i);
	}

	/**
	 * @tests java.util.LinkedHashSet#LinkedHashSet()
	 */
	public void test_Constructor() {
		// Test for method java.util.LinkedHashSet()
		LinkedHashSet hs2 = new LinkedHashSet();
		assertEquals("Created incorrect LinkedHashSet", 0, hs2.size());
	}

	/**
	 * @tests java.util.LinkedHashSet#LinkedHashSet(int)
	 */
	public void test_ConstructorI() {
		// Test for method java.util.LinkedHashSet(int)
		LinkedHashSet hs2 = new LinkedHashSet(5);
		assertEquals("Created incorrect LinkedHashSet", 0, hs2.size());
		try {
			new LinkedHashSet(-1);
		} catch (IllegalArgumentException e) {
			return;
		}
		fail(
				"Failed to throw IllegalArgumentException for capacity < 0");
	}

	/**
	 * @tests java.util.LinkedHashSet#LinkedHashSet(int, float)
	 */
	public void test_ConstructorIF() {
		// Test for method java.util.LinkedHashSet(int, float)
		LinkedHashSet hs2 = new LinkedHashSet(5, (float) 0.5);
		assertEquals("Created incorrect LinkedHashSet", 0, hs2.size());
		try {
			new LinkedHashSet(0, 0);
		} catch (IllegalArgumentException e) {
			return;
		}
		fail(
				"Failed to throw IllegalArgumentException for initial load factor <= 0");
	}

	/**
	 * @tests java.util.LinkedHashSet#LinkedHashSet(java.util.Collection)
	 */
	public void test_ConstructorLjava_util_Collection() {
		// Test for method java.util.LinkedHashSet(java.util.Collection)
		LinkedHashSet hs2 = new LinkedHashSet(Arrays.asList(objArray));
		for (int counter = 0; counter < objArray.length; counter++)
			assertTrue("LinkedHashSet does not contain correct elements", hs
					.contains(objArray[counter]));
		assertTrue("LinkedHashSet created from collection incorrect size", hs2
				.size() == objArray.length);
	}

	/**
	 * @tests java.util.LinkedHashSet#add(java.lang.Object)
	 */
	public void test_addLjava_lang_Object() {
		// Test for method boolean java.util.LinkedHashSet.add(java.lang.Object)
		int size = hs.size();
		hs.add(new Integer(8));
		assertTrue("Added element already contained by set", hs.size() == size);
		hs.add(new Integer(-9));
		assertTrue("Failed to increment set size after add",
				hs.size() == size + 1);
		assertTrue("Failed to add element to set", hs.contains(new Integer(-9)));
	}

	/**
	 * @tests java.util.LinkedHashSet#clear()
	 */
	public void test_clear() {
		// Test for method void java.util.LinkedHashSet.clear()
		Set orgSet = (Set) hs.clone();
		hs.clear();
		Iterator i = orgSet.iterator();
		assertEquals("Returned non-zero size after clear", 0, hs.size());
		while (i.hasNext())
			assertTrue("Failed to clear set", !hs.contains(i.next()));
	}

	/**
	 * @tests java.util.LinkedHashSet#clone()
	 */
	public void test_clone() {
		// Test for method java.lang.Object java.util.LinkedHashSet.clone()
		LinkedHashSet hs2 = (LinkedHashSet) hs.clone();
		assertTrue("clone returned an equivalent LinkedHashSet", hs != hs2);
		assertTrue("clone did not return an equal LinkedHashSet", hs
				.equals(hs2));
	}

	/**
	 * @tests java.util.LinkedHashSet#contains(java.lang.Object)
	 */
	public void test_containsLjava_lang_Object() {
		// Test for method boolean
		// java.util.LinkedHashSet.contains(java.lang.Object)
		assertTrue("Returned false for valid object", hs.contains(objArray[90]));
		assertTrue("Returned true for invalid Object", !hs
				.contains(new Object()));

		LinkedHashSet s = new LinkedHashSet();
		s.add(null);
		assertTrue("Cannot handle null", s.contains(null));
	}

	/**
	 * @tests java.util.LinkedHashSet#isEmpty()
	 */
	public void test_isEmpty() {
		// Test for method boolean java.util.LinkedHashSet.isEmpty()
		assertTrue("Empty set returned false", new LinkedHashSet().isEmpty());
		assertTrue("Non-empty set returned true", !hs.isEmpty());
	}

	/**
	 * @tests java.util.LinkedHashSet#iterator()
	 */
	public void test_iterator() {
		// Test for method java.util.Iterator java.util.LinkedHashSet.iterator()
		Iterator i = hs.iterator();
		int x = 0;
		int j;
		for (j = 0; i.hasNext(); j++) {
			Object oo = i.next();
			if (oo != null) {
				Integer ii = (Integer) oo;
				assertTrue("Incorrect element found", ii.intValue() == j);
			} else {
				assertTrue("Cannot find null", hs.contains(oo));
			}
			++x;
		}
		assertTrue("Returned iteration of incorrect size", hs.size() == x);

		LinkedHashSet s = new LinkedHashSet();
		s.add(null);
		assertNull("Cannot handle null", s.iterator().next());
	}

	/**
	 * @tests java.util.LinkedHashSet#remove(java.lang.Object)
	 */
	public void test_removeLjava_lang_Object() {
		// Test for method boolean
		// java.util.LinkedHashSet.remove(java.lang.Object)
		int size = hs.size();
		hs.remove(new Integer(98));
		assertTrue("Failed to remove element", !hs.contains(new Integer(98)));
		assertTrue("Failed to decrement set size", hs.size() == size - 1);

		LinkedHashSet s = new LinkedHashSet();
		s.add(null);
		assertTrue("Cannot handle null", s.remove(null));
	}

	/**
	 * @tests java.util.LinkedHashSet#size()
	 */
	public void test_size() {
		// Test for method int java.util.LinkedHashSet.size()
		assertTrue("Returned incorrect size", hs.size() == (objArray.length + 1));
		hs.clear();
		assertEquals("Cleared set returned non-zero size", 0, hs.size());
	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {
		hs = new LinkedHashSet();
		for (int i = 0; i < objArray.length; i++)
			hs.add(objArray[i]);
		hs.add(null);
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
	}
}
