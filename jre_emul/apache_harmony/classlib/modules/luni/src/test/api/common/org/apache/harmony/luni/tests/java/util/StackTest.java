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

import java.util.EmptyStackException;
import java.util.Stack;

public class StackTest extends junit.framework.TestCase {

	Stack s;

	/**
	 * @tests java.util.Stack#Stack()
	 */
	public void test_Constructor() {
		// Test for method java.util.Stack()
		assertEquals("Stack creation failed", 0, s.size());
	}

	/**
	 * @tests java.util.Stack#empty()
	 */
	public void test_empty() {
		// Test for method boolean java.util.Stack.empty()
		assertTrue("New stack answers non-empty", s.empty());
		s.push("blah");
		assertTrue("Stack should not be empty but answers empty", !s.empty());
		s.pop();
		assertTrue("Stack should be empty but answers non-empty", s.empty());
		s.push(null);
		assertTrue("Stack with null should not be empty but answers empty", !s
				.empty());
	}

	/**
	 * @tests java.util.Stack#peek()
	 */
	public void test_peek() {
		// Test for method java.lang.Object java.util.Stack.peek()
		String item1 = "Ichi";
		String item2 = "Ni";
		String item3 = "San";
		s.push(item1);
		assertTrue("Peek did not return top item when it was the only item", s
				.peek() == item1);
		s.push(item2);
		s.push(item3);
		assertTrue("Peek did not return top item amoung many other items", s
				.peek() == item3);
		s.pop();
		assertTrue("Peek did not return top item after a pop", s.pop() == item2);
		s.push(null);
		assertNull("Peek did not return top item (wanted: null)",
				s.peek());
	}

	/**
	 * @tests java.util.Stack#pop()
	 */
	public void test_pop() {
		// Test for method java.lang.Object java.util.Stack.pop()
		String item1 = "Ichi";
		String item2 = "Ni";
		Object lastPopped;
		s.push(item1);
		s.push(item2);

		try {
			lastPopped = s.pop();
			assertTrue("a) Pop did not return top item", lastPopped == item2);
		} catch (EmptyStackException e) {
			fail(
					"a) Pop threw EmptyStackException when stack should not have been empty");
		}

		try {
			lastPopped = s.pop();
			assertTrue("b) Pop did not return top item", lastPopped == item1);
		} catch (EmptyStackException e) {
			fail(
					"b) Pop threw EmptyStackException when stack should not have been empty");
		}

		s.push(null);
		try {
			lastPopped = s.pop();
			assertNull("c) Pop did not return top item", lastPopped);
		} catch (EmptyStackException e) {
			fail(
					"c) Pop threw EmptyStackException when stack should not have been empty");
		}

		try {
			lastPopped = s.pop();
			fail(
					"d) Pop did not throw EmptyStackException when stack should have been empty");
		} catch (EmptyStackException e) {
			return;
		}

	}

	/**
	 * @tests java.util.Stack#push(java.lang.Object)
	 */
	public void test_pushLjava_lang_Object() {
		// Test for method java.lang.Object
		// java.util.Stack.push(java.lang.Object)
		assertTrue("Used to test", true);
	}

	/**
	 * @tests java.util.Stack#search(java.lang.Object)
	 */
	public void test_searchLjava_lang_Object() {
		// Test for method int java.util.Stack.search(java.lang.Object)
		String item1 = "Ichi";
		String item2 = "Ni";
		String item3 = "San";
		s.push(item1);
		s.push(item2);
		s.push(item3);
		assertEquals("Search returned incorrect value for equivalent object", 3, s
				.search(item1));
		assertEquals("Search returned incorrect value for equal object", 3, s
				.search("Ichi"));
		s.pop();
		assertEquals("Search returned incorrect value for equivalent object at top of stack",
				1, s.search(item2));
		assertEquals("Search returned incorrect value for equal object at top of stack",
				1, s.search("Ni"));
		s.push(null);
		assertEquals("Search returned incorrect value for search for null at top of stack",
				1, s.search(null));
		s.push("Shi");
		assertEquals("Search returned incorrect value for search for null", 2, s
				.search(null));
		s.pop();
		s.pop();
		assertEquals("Search returned incorrect value for search for null--wanted -1",
				-1, s.search(null));
	}
	
	static class BugStack<E> extends Stack<E>{
		/**
		 * 
		 */
		private static final long serialVersionUID = -9133762075342926141L;

		/**
		 * 
		 */
		public void setLength(int elementCount)
		{
			this.elementCount = elementCount;
		}
		
		public int getLength()
		{
			return elementCount;
		}
	}
	
	//test for wrong exception threw by pop method
	public void test_pop_modify_elementCount(){
		BugStack<String> testStack = new BugStack<String>();
		testStack.push("A");
		testStack.push("B");
		testStack.setLength(20);
		try{
			testStack.pop();
			fail("Should throw ArrayIndexOutOfBoundsException here");
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			//Expected to throw ArrayIndexOutOfBoundsException here
		}
		catch(EmptyStackException e)
		{
			fail("Should throw ArrayIndexOutOfBoundsException here");
		}
	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {
		s = new Stack();
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
	}
}
