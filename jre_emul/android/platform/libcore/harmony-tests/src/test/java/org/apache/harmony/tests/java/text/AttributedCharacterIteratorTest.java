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

import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.CharacterIterator;

public class AttributedCharacterIteratorTest extends junit.framework.TestCase {

	/**
	 * @tests java.text.AttributedCharacterIterator#current()
	 */
	public void test_current() {
		String test = "Test 23ring";
		AttributedString attrString = new AttributedString(test);
		AttributedCharacterIterator it = attrString.getIterator();
		assertEquals("Wrong first", 'T', it.current());
		it.next();
		assertEquals("Wrong second", 'e', it.current());
		for (int i = 0; i < 9; i++)
			it.next();
		assertEquals("Wrong last", 'g', it.current());
		it.next();
		assertTrue("Wrong final", it.current() == CharacterIterator.DONE);

		it = attrString.getIterator(null, 2, 8);
		assertEquals("Wrong first2", 's', it.current());
	}

	/**
	 * @tests java.text.AttributedCharacterIterator#first()
	 */
	public void test_first() {
		String test = "Test 23ring";
		AttributedString attrString = new AttributedString(test);
		AttributedCharacterIterator it = attrString.getIterator();
		assertEquals("Wrong first1", 'T', it.first());
		it = attrString.getIterator(null, 0, 3);
		assertEquals("Wrong first2", 'T', it.first());
		it = attrString.getIterator(null, 2, 8);
		assertEquals("Wrong first3", 's', it.first());
		it = attrString.getIterator(null, 11, 11);
		assertTrue("Wrong first4", it.first() == CharacterIterator.DONE);
	}

	/**
	 * @tests java.text.AttributedCharacterIterator#getBeginIndex()
	 */
	public void test_getBeginIndex() {
		String test = "Test 23ring";
		AttributedString attrString = new AttributedString(test);
		AttributedCharacterIterator it = attrString.getIterator(null, 2, 6);
		assertEquals("Wrong begin index", 2, it.getBeginIndex());
	}

	/**
	 * @tests java.text.AttributedCharacterIterator#getEndIndex()
	 */
	public void test_getEndIndex() {
		String test = "Test 23ring";
		AttributedString attrString = new AttributedString(test);
		AttributedCharacterIterator it = attrString.getIterator(null, 2, 6);
		assertEquals("Wrong begin index", 6, it.getEndIndex());
	}

	/**
	 * @tests java.text.AttributedCharacterIterator#getIndex()
	 */
	public void test_getIndex() {
		String test = "Test 23ring";
		AttributedString attrString = new AttributedString(test);
		AttributedCharacterIterator it = attrString.getIterator();
		assertEquals("Wrong first", 0, it.getIndex());
		it.next();
		assertEquals("Wrong second", 1, it.getIndex());
		for (int i = 0; i < 9; i++)
			it.next();
		assertEquals("Wrong last", 10, it.getIndex());
		it.next();
		assertEquals("Wrong final", 11, it.getIndex());
	}

	/**
	 * @tests java.text.AttributedCharacterIterator#last()
	 */
	public void test_last() {
		String test = "Test 23ring";
		AttributedString attrString = new AttributedString(test);
		AttributedCharacterIterator it = attrString.getIterator();
		assertEquals("Wrong last1", 'g', it.last());
		it = attrString.getIterator(null, 0, 3);
		assertEquals("Wrong last2", 's', it.last());
		it = attrString.getIterator(null, 2, 8);
		assertEquals("Wrong last3", 'r', it.last());
		it = attrString.getIterator(null, 0, 0);
		assertTrue("Wrong last4", it.last() == CharacterIterator.DONE);
	}

	/**
	 * @tests java.text.AttributedCharacterIterator#next()
	 */
	public void test_next() {
		String test = "Test 23ring";
		AttributedString attrString = new AttributedString(test);
		AttributedCharacterIterator it = attrString.getIterator();
		assertEquals("Wrong first", 'e', it.next());
		for (int i = 0; i < 8; i++)
			it.next();
		assertEquals("Wrong last", 'g', it.next());
		assertTrue("Wrong final", it.next() == CharacterIterator.DONE);

		it = attrString.getIterator(null, 2, 8);
		assertEquals("Wrong first2", 't', it.next());
	}

	/**
	 * @tests java.text.AttributedCharacterIterator#previous()
	 */
	public void test_previous() {
		String test = "Test 23ring";
		AttributedString attrString = new AttributedString(test);
		AttributedCharacterIterator it = attrString.getIterator();
		it.setIndex(11);
		assertEquals("Wrong first", 'g', it.previous());
	}

	/**
	 * @tests java.text.AttributedCharacterIterator#setIndex(int)
	 */
	public void test_setIndexI() {
		String test = "Test 23ring";
		AttributedString attrString = new AttributedString(test);
		AttributedCharacterIterator it = attrString.getIterator();
		it.setIndex(5);
		assertEquals("Wrong first", '2', it.current());
	}

	/**
	 * @tests java.text.AttributedCharacterIterator#getRunLimit(java.text.AttributedCharacterIterator$Attribute)
	 */
	public void test_getRunLimitLjava_text_AttributedCharacterIterator$Attribute() {
		AttributedString as = new AttributedString("test");
		as.addAttribute(AttributedCharacterIterator.Attribute.LANGUAGE, "a", 2,
				3);
		AttributedCharacterIterator it = as.getIterator();
		assertEquals("non-null value limit",
				2, it.getRunLimit(AttributedCharacterIterator.Attribute.LANGUAGE));

		as = new AttributedString("test");
		as.addAttribute(AttributedCharacterIterator.Attribute.LANGUAGE, null,
				2, 3);
		it = as.getIterator();
		assertEquals("null value limit",
				4, it.getRunLimit(AttributedCharacterIterator.Attribute.LANGUAGE));
	}

	protected void setUp() {
	}

	protected void tearDown() {
	}
}
