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

import java.util.Set;

public class Support_SetTest extends junit.framework.TestCase {

	Set<Integer> set; // must contain only the Integers 0 to 99

	public Support_SetTest(String p1) {
		super(p1);
	}

	public Support_SetTest(String p1, Set<Integer> s) {
		super(p1);
		set = s;
	}

	@Override
    public void runTest() {
		// add
		assertTrue("Set Test - Adding a duplicate element changed the set",
				!set.add(new Integer(50)));
		assertTrue("Set Test - Removing an element did not change the set", set
				.remove(new Integer(50)));
		assertTrue(
				"Set Test - Adding and removing a duplicate element failed to remove it",
				!set.contains(new Integer(50)));
		set.add(new Integer(50));
		new Support_CollectionTest("", set).runTest();
	}
}
