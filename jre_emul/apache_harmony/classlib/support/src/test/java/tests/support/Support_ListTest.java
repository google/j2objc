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

package tests.support;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class Support_ListTest extends junit.framework.TestCase {

	List<Integer> list; // must contain the Integers 0 to 99 in order

	public Support_ListTest(String p1) {
		super(p1);
	}

	public Support_ListTest(String p1, List<Integer> l) {
		super(p1);
		list = l;
	}

	@Override
    public void runTest() {
		int hashCode = 1;
		for (int counter = 0; counter < 100; counter++) {
			Object elem;
			elem = list.get(counter);
			hashCode = 31 * hashCode + elem.hashCode();
			assertTrue("ListTest - get failed", elem
					.equals(new Integer(counter)));
		}
		assertTrue("ListTest - hashCode failed", hashCode == list.hashCode());

		list.add(50, new Integer(1000));
		assertTrue("ListTest - a) add with index failed--did not insert", list
				.get(50).equals(new Integer(1000)));
		assertTrue(
				"ListTest - b) add with index failed--did not move following elements",
				list.get(51).equals(new Integer(50)));
		assertTrue(
				"ListTest - c) add with index failed--affected previous elements",
				list.get(49).equals(new Integer(49)));

		list.set(50, new Integer(2000));
		assertTrue("ListTest - a) set failed--did not set", list.get(50)
				.equals(new Integer(2000)));
		assertTrue("ListTest - b) set failed--affected following elements",
				list.get(51).equals(new Integer(50)));
		assertTrue("ListTest - c) set failed--affected previous elements", list
				.get(49).equals(new Integer(49)));

		list.remove(50);
		assertTrue("ListTest - a) remove with index failed--did not remove",
				list.get(50).equals(new Integer(50)));
		assertTrue(
				"ListTest - b) remove with index failed--did not move following elements",
				list.get(51).equals(new Integer(51)));
		assertTrue(
				"ListTest - c) remove with index failed--affected previous elements",
				list.get(49).equals(new Integer(49)));

		List<Integer> myList = new LinkedList<Integer>();
		myList.add(new Integer(500));
		myList.add(new Integer(501));
		myList.add(new Integer(502));

		list.addAll(50, myList);
		assertTrue("ListTest - a) addAll with index failed--did not insert",
				list.get(50).equals(new Integer(500)));
		assertTrue("ListTest - b) addAll with index failed--did not insert",
				list.get(51).equals(new Integer(501)));
		assertTrue("ListTest - c) addAll with index failed--did not insert",
				list.get(52).equals(new Integer(502)));
		assertTrue(
				"ListTest - d) addAll with index failed--did not move following elements",
				list.get(53).equals(new Integer(50)));
		assertTrue(
				"ListTest - e) addAll with index failed--affected previous elements",
				list.get(49).equals(new Integer(49)));

		List<Integer> mySubList = list.subList(50, 53);
		assertEquals(3, mySubList.size());
		assertTrue(
				"ListTest - a) sublist Failed--does not contain correct elements",
				mySubList.get(0).equals(new Integer(500)));
		assertTrue(
				"ListTest - b) sublist Failed--does not contain correct elements",
				mySubList.get(1).equals(new Integer(501)));
		assertTrue(
				"ListTest - c) sublist Failed--does not contain correct elements",
				mySubList.get(2).equals(new Integer(502)));

		t_listIterator(mySubList);

		mySubList.clear();
		assertEquals("ListTest - Clearing the sublist did not remove the appropriate elements from the original list",
				100, list.size());

		t_listIterator(list);
		ListIterator<Integer> li = list.listIterator();
		for (int counter = 0; li.hasNext(); counter++) {
			Object elem;
			elem = li.next();
			assertTrue("ListTest - listIterator failed", elem
					.equals(new Integer(counter)));
		}

		new Support_CollectionTest("", list).runTest();

	}

	public void t_listIterator(List<Integer> list) {
		ListIterator<Integer> li = list.listIterator(1);
		assertTrue("listIterator(1)", li.next() == list.get(1));

		int orgSize = list.size();
		li = list.listIterator();
		for (int i = 0; i <= orgSize; i++) {
			if (i == 0) {
                assertTrue("list iterator hasPrevious(): " + i, !li
						.hasPrevious());
            } else {
                assertTrue("list iterator hasPrevious(): " + i, li
						.hasPrevious());
            }
			if (i == list.size()) {
                assertTrue("list iterator hasNext(): " + i, !li.hasNext());
            } else {
                assertTrue("list iterator hasNext(): " + i, li.hasNext());
            }
			assertTrue("list iterator nextIndex(): " + i, li.nextIndex() == i);
			assertTrue("list iterator previousIndex(): " + i, li
					.previousIndex() == i - 1);
			boolean exception = false;
			try {
				assertTrue("list iterator next(): " + i, li.next() == list
						.get(i));
			} catch (NoSuchElementException e) {
				exception = true;
			}
			if (i == list.size()) {
                assertTrue("list iterator next() exception: " + i, exception);
            } else {
                assertTrue("list iterator next() exception: " + i, !exception);
            }
		}

		for (int i = orgSize - 1; i >= 0; i--) {
			assertTrue("list iterator previous(): " + i, li.previous() == list
					.get(i));
			assertTrue("list iterator nextIndex()2: " + i, li.nextIndex() == i);
			assertTrue("list iterator previousIndex()2: " + i, li
					.previousIndex() == i - 1);
			if (i == 0) {
                assertTrue("list iterator hasPrevious()2: " + i, !li
						.hasPrevious());
            } else {
                assertTrue("list iterator hasPrevious()2: " + i, li
						.hasPrevious());
            }
			assertTrue("list iterator hasNext()2: " + i, li.hasNext());
		}
		boolean exception = false;
		try {
			li.previous();
		} catch (NoSuchElementException e) {
			exception = true;
		}
		assertTrue("list iterator previous() exception", exception);

		Integer add1 = new Integer(600);
		Integer add2 = new Integer(601);
		li.add(add1);
		assertTrue("list iterator add(), size()", list.size() == (orgSize + 1));
		assertEquals("list iterator add(), nextIndex()", 1, li.nextIndex());
		assertEquals("list iterator add(), previousIndex()",
				0, li.previousIndex());
		Object next = li.next();
		assertTrue("list iterator add(), next(): " + next, next == list.get(1));
		li.add(add2);
		Object previous = li.previous();
		assertTrue("list iterator add(), previous(): " + previous,
				previous == add2);
		assertEquals("list iterator add(), nextIndex()2", 2, li.nextIndex());
		assertEquals("list iterator add(), previousIndex()2",
				1, li.previousIndex());

		li.remove();
		assertTrue("list iterator remove(), size()",
				list.size() == (orgSize + 1));
		assertEquals("list iterator remove(), nextIndex()", 2, li.nextIndex());
		assertEquals("list iterator remove(), previousIndex()", 1, li
				.previousIndex());
		assertTrue("list iterator previous()2", li.previous() == list.get(1));
		assertTrue("list iterator previous()3", li.previous() == list.get(0));
		assertTrue("list iterator next()2", li.next() == list.get(0));
		li.remove();
		assertTrue("list iterator hasPrevious()3", !li.hasPrevious());
		assertTrue("list iterator hasNext()3", li.hasNext());
		assertTrue("list iterator size()", list.size() == orgSize);
		assertEquals("list iterator nextIndex()3", 0, li.nextIndex());
		assertEquals("list iterator previousIndex()3", -1, li.previousIndex());
	}
}
