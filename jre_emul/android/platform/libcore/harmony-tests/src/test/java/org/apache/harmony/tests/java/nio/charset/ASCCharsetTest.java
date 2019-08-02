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
 * Test charset US-ASCII.
 */
public class ASCCharsetTest extends AbstractCharsetTestCase {

	/**
	 * Constructor.
	 * 
	 */
	public ASCCharsetTest(String arg0) {
		super(arg0, "US-ASCII", new String[] { "ISO646-US", "ASCII", "cp367",
				"ascii7", "ANSI_X3.4-1986", "iso-ir-6", "us", "646",
				"iso_646.irv:1983", "csASCII", "ANSI_X3.4-1968",
				"ISO_646.irv:1991" }, true, true); // "ibm-367"
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see tests.api.java.nio.charset.ConcreteCharsetTest#testEncode_Normal()
	 */
	public void testEncode_Normal() {
		String input = "ab\u5D14\u654F";
		byte[] output = new byte[] { 97, 98,
				this.testingCharset.newEncoder().replacement()[0],
				this.testingCharset.newEncoder().replacement()[0] };
		internalTestEncode(input, output);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see tests.api.java.nio.charset.ConcreteCharsetTest#testDecode_Normal()
	 */
	public void testDecode_Normal() {
		byte[] input = new byte[] { 97, 98, 63, 63 };
		char[] output = "ab??".toCharArray();
		internalTestDecode(input, output);
	}

}
