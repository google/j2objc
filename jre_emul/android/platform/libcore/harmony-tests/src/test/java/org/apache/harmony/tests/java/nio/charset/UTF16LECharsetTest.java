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

package org.apache.harmony.tests.java.nio.charset;

/**
 * Test UTF-16LE.
 */
public class UTF16LECharsetTest extends AbstractCharsetTestCase {

	/**
	 * Constructor.
	 */
	public UTF16LECharsetTest(String arg0) {
		super(arg0, "UTF-16LE", new String[] { "UTF_16LE", "X-UTF-16LE" },
				true, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see tests.api.java.nio.charset.ConcreteCharsetTest#testEncode_Normal()
	 */
	public void testEncode_Normal() {
		String input = "ab\u5D14\u654F";
		byte[] output = new byte[] { 97, 0, 98, 0, 20, 93, 79, 101 };
		internalTestEncode(input, output);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see tests.api.java.nio.charset.ConcreteCharsetTest#testDecode_Normal()
	 */
	public void testDecode_Normal() {
		byte[] input = new byte[] { 97, 0, 98, 0, 20, 93, 79, 101 };
		char[] output = "ab\u5D14\u654F".toCharArray();
		internalTestDecode(input, output);
	}

}
