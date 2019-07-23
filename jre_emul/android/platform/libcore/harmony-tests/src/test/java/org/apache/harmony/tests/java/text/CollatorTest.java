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

import java.text.Collator;
import java.util.Locale;

public class CollatorTest extends junit.framework.TestCase {

	/**
	 * @tests java.text.Collator#clone()
	 */
	public void test_clone() {
		Collator c = Collator.getInstance(Locale.GERMAN);
		Collator c2 = (Collator) c.clone();
		assertTrue("Clones answered false to equals", c.equals(c2));
		assertTrue("Clones were equivalent", c != c2);
	}

	/**
	 * @tests java.text.Collator#compare(java.lang.Object, java.lang.Object)
	 */
	public void test_compareLjava_lang_ObjectLjava_lang_Object() {
		Collator c = Collator.getInstance(Locale.FRENCH);
		Object o, o2;

		c.setStrength(Collator.IDENTICAL);
		o = "E";
		o2 = "F";
		assertTrue("a) Failed on primary difference", c.compare(o, o2) < 0);
		o = "e";
		o2 = "\u00e9";
		assertTrue("a) Failed on secondary difference", c.compare(o, o2) < 0);
		o = "e";
		o2 = "E";
		assertTrue("a) Failed on tertiary difference", c.compare(o, o2) < 0);
		o = "e";
		o2 = "e";
		assertEquals("a) Failed on equivalence", 0, c.compare(o, o2));
		assertTrue("a) Failed on primary expansion",
				c.compare("\u01db", "v") < 0);

		c.setStrength(Collator.TERTIARY);
		o = "E";
		o2 = "F";
		assertTrue("b) Failed on primary difference", c.compare(o, o2) < 0);
		o = "e";
		o2 = "\u00e9";
		assertTrue("b) Failed on secondary difference", c.compare(o, o2) < 0);
		o = "e";
		o2 = "E";
		assertTrue("b) Failed on tertiary difference", c.compare(o, o2) < 0);
		o = "\u0001";
		o2 = "\u0002";
		assertEquals("b) Failed on identical", 0, c.compare(o, o2));
		o = "e";
		o2 = "e";
		assertEquals("b) Failed on equivalence", 0, c.compare(o, o2));

		c.setStrength(Collator.SECONDARY);
		o = "E";
		o2 = "F";
		assertTrue("c) Failed on primary difference", c.compare(o, o2) < 0);
		o = "e";
		o2 = "\u00e9";
		assertTrue("c) Failed on secondary difference", c.compare(o, o2) < 0);
		o = "e";
		o2 = "E";
		assertEquals("c) Failed on tertiary difference", 0, c.compare(o, o2));
		o = "\u0001";
		o2 = "\u0002";
		assertEquals("c) Failed on identical", 0, c.compare(o, o2));
		o = "e";
		o2 = "e";
		assertEquals("c) Failed on equivalence", 0, c.compare(o, o2));

		c.setStrength(Collator.PRIMARY);
		o = "E";
		o2 = "F";
		assertTrue("d) Failed on primary difference", c.compare(o, o2) < 0);
		o = "e";
		o2 = "\u00e9";
		assertEquals("d) Failed on secondary difference", 0, c.compare(o, o2));
		o = "e";
		o2 = "E";
		assertEquals("d) Failed on tertiary difference", 0, c.compare(o, o2));
		o = "\u0001";
		o2 = "\u0002";
		assertEquals("d) Failed on identical", 0, c.compare(o, o2));
		o = "e";
		o2 = "e";
		assertEquals("d) Failed on equivalence", 0, c.compare(o, o2));

		try {
			c.compare("e", new StringBuffer("Blah"));
		} catch (ClassCastException e) {
			// correct
			return;
		}
		fail("Failed to throw ClassCastException");
	}

	/**
	 * @tests java.text.Collator#equals(java.lang.Object)
	 */
	public void test_equalsLjava_lang_Object() {
		Collator c = Collator.getInstance(Locale.ENGLISH);
		Collator c2 = (Collator) c.clone();
		assertTrue("Cloned collators not equal", c.equals(c2));
		c2.setStrength(Collator.SECONDARY);
		assertTrue("Collators with different strengths equal", !c.equals(c2));
	}

	/**
	 * @tests java.text.Collator#equals(java.lang.String, java.lang.String)
	 */
	public void test_equalsLjava_lang_StringLjava_lang_String() {
		Collator c = Collator.getInstance(Locale.FRENCH);

		c.setStrength(Collator.IDENTICAL);
		assertTrue("a) Failed on primary difference", !c.equals("E", "F"));
		assertTrue("a) Failed on secondary difference", !c
				.equals("e", "\u00e9"));
		assertTrue("a) Failed on tertiary difference", !c.equals("e", "E"));
		assertTrue("a) Failed on equivalence", c.equals("e", "e"));

		c.setStrength(Collator.TERTIARY);
		assertTrue("b) Failed on primary difference", !c.equals("E", "F"));
		assertTrue("b) Failed on secondary difference", !c
				.equals("e", "\u00e9"));
		assertTrue("b) Failed on tertiary difference", !c.equals("e", "E"));
		assertTrue("b) Failed on identical", c.equals("\u0001", "\u0002"));
		assertTrue("b) Failed on equivalence", c.equals("e", "e"));

		c.setStrength(Collator.SECONDARY);
		assertTrue("c) Failed on primary difference", !c.equals("E", "F"));
		assertTrue("c) Failed on secondary difference", !c
				.equals("e", "\u00e9"));
		assertTrue("c) Failed on tertiary difference", c.equals("e", "E"));
		assertTrue("c) Failed on identical", c.equals("\u0001", "\u0002"));
		assertTrue("c) Failed on equivalence", c.equals("e", "e"));

		c.setStrength(Collator.PRIMARY);
		assertTrue("d) Failed on primary difference", !c.equals("E", "F"));
		assertTrue("d) Failed on secondary difference", c.equals("e", "\u00e9"));
		assertTrue("d) Failed on tertiary difference", c.equals("e", "E"));
		assertTrue("d) Failed on identical", c.equals("\u0001", "\u0002"));
		assertTrue("d) Failed on equivalence", c.equals("e", "e"));
	}

    /**
     * @tests java.text.Collator#getAvailableLocales()
     */
    //FIXME This test fails on Harmony ClassLibrary
	public void failing_test_getAvailableLocales() {
		Locale[] locales = Collator.getAvailableLocales();
		assertTrue("No locales", locales.length > 0);
		boolean english = false, german = false;
		for (int i = locales.length; --i >= 0;) {
			if (locales[i].equals(Locale.ENGLISH))
				english = true;
			if (locales[i].equals(Locale.GERMAN))
				german = true;
			// Output the working locale to help diagnose a hang
			Collator c1 = Collator.getInstance(locales[i]);
			assertTrue("Doesn't work", c1.compare("a", "b") < 0);
			assertTrue("Wrong decomposition",
					c1.getDecomposition() == Collator.NO_DECOMPOSITION);
			assertTrue("Wrong strength", c1.getStrength() == Collator.TERTIARY);
		}
		assertTrue("Missing locales", english && german);
	}

	/**
	 * @tests java.text.Collator#getInstance()
	 */
	public void test_getInstance() {
		Collator c1 = Collator.getInstance();
		Collator c2 = Collator.getInstance(Locale.getDefault());
		assertTrue("Wrong locale", c1.equals(c2));
	}

	/**
	 * @tests java.text.Collator#getInstance(java.util.Locale)
	 */
	public void test_getInstanceLjava_util_Locale() {
		assertTrue("Used to test", true);
	}

	/**
	 * @tests java.text.Collator#setDecomposition(int)
	 */
	//FIXME This test fails on Harmony ClassLibrary
	public void failing_test_setDecompositionI() {
		Collator c = Collator.getInstance(Locale.FRENCH);
		c.setStrength(Collator.IDENTICAL);
		c.setDecomposition(Collator.NO_DECOMPOSITION);
		assertTrue("Collator should not be using decomposition", !c.equals(
				"\u212B", "\u00C5")); // "ANGSTROM SIGN" and "LATIN CAPITAL
		// LETTER A WITH RING ABOVE"
		c.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
		assertTrue("Collator should be using decomposition", c.equals("\u212B",
				"\u00C5")); // "ANGSTROM SIGN" and "LATIN CAPITAL LETTER A WITH
		// RING ABOVE"
		assertTrue("Should not be equal under canonical decomposition", !c
				.equals("\u2163", "IV")); // roman number "IV"
		c.setDecomposition(Collator.FULL_DECOMPOSITION);
		assertTrue("Should be equal under full decomposition", c.equals(
				"\u2163", "IV")); // roman number "IV"
	}

	/**
	 * @tests java.text.Collator#setStrength(int)
	 */
	public void test_setStrengthI() {
		assertTrue("Used to test", true);
	}
}
