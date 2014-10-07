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

import java.util.Collection;
import java.util.TreeSet;

/**
 * @tests java.util.Collection
 */
public class Support_CollectionTest extends junit.framework.TestCase {

	Collection<Integer> col; // must contain the Integers 0 to 99

	public Support_CollectionTest(String p1) {
		super(p1);
	}

	public Support_CollectionTest(String p1, Collection<Integer> c) {
		super(p1);
		col = c;
	}

	@Override
    public void runTest() {
		new Support_UnmodifiableCollectionTest("", col).runTest();

		// setup
		Collection<Integer> myCollection = new TreeSet<Integer>();
		myCollection.add(new Integer(101));
		myCollection.add(new Integer(102));
		myCollection.add(new Integer(103));

		// add
		assertTrue("CollectionTest - a) add did not work", col.add(new Integer(
				101)));
		assertTrue("CollectionTest - b) add did not work", col
				.contains(new Integer(101)));

		// remove
		assertTrue("CollectionTest - a) remove did not work", col
				.remove(new Integer(101)));
		assertTrue("CollectionTest - b) remove did not work", !col
				.contains(new Integer(101)));

		// addAll
		assertTrue("CollectionTest - a) addAll failed", col
				.addAll(myCollection));
		assertTrue("CollectionTest - b) addAll failed", col
				.containsAll(myCollection));

		// containsAll
		assertTrue("CollectionTest - a) containsAll failed", col
				.containsAll(myCollection));
		col.remove(new Integer(101));
		assertTrue("CollectionTest - b) containsAll failed", !col
				.containsAll(myCollection));

		// removeAll
		assertTrue("CollectionTest - a) removeAll failed", col
				.removeAll(myCollection));
		assertTrue("CollectionTest - b) removeAll failed", !col
				.removeAll(myCollection)); // should not change the colletion
											// the 2nd time around
		assertTrue("CollectionTest - c) removeAll failed", !col
				.contains(new Integer(102)));
		assertTrue("CollectionTest - d) removeAll failed", !col
				.contains(new Integer(103)));

		// retianAll
		col.addAll(myCollection);
		assertTrue("CollectionTest - a) retainAll failed", col
				.retainAll(myCollection));
		assertTrue("CollectionTest - b) retainAll failed", !col
				.retainAll(myCollection)); // should not change the colletion
											// the 2nd time around
		assertTrue("CollectionTest - c) retainAll failed", col
				.containsAll(myCollection));
		assertTrue("CollectionTest - d) retainAll failed", !col
				.contains(new Integer(0)));
		assertTrue("CollectionTest - e) retainAll failed", !col
				.contains(new Integer(50)));

		// clear
		col.clear();
		assertTrue("CollectionTest - a) clear failed", col.isEmpty());
		assertTrue("CollectionTest - b) clear failed", !col
				.contains(new Integer(101)));

	}

}
