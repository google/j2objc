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

import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

/**
 * test case specific activity of gb18030 charset encoder
 */
public class GBCharsetEncoderTest extends CharsetEncoderTest {

	// charset for gb180303
	private static final Charset CS = Charset.forName("gb18030");

	/*
	 * @see CharsetEncoderTest#setUp()
	 */
	protected void setUp() throws Exception {
	  cs = CS;
	  specifiedReplacement = new byte[] { 0x1a };
	  super.setUp();
	}

	/*
	 * @see CharsetEncoderTest#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testCanEncodechar() {
		// normal case for utfCS
		assertTrue(encoder.canEncode('\u0077'));
		assertTrue(encoder.canEncode('\uc2a3'));

		// for non-mapped char
		assertTrue(encoder.canEncode('\uc2c0'));
	}

	/*
	 * Class under test for boolean canEncode(CharSequence)
	 */
	public void testCanEncodeCharSequence() {
		// surrogate char

		// valid surrogate pair
		assertTrue(encoder.canEncode("\ud800\udc00"));
		// invalid surrogate pair
		assertFalse(encoder.canEncode("\ud800\udb00"));
	}

	public void testSpecificDefaultValue() {
		assertEquals(4.0, encoder.maxBytesPerChar(), 0.0);
		assertEquals(2.5, encoder.averageBytesPerChar(), 0.0);
	}

	CharBuffer getMalformedCharBuffer() {
		return CharBuffer.wrap("\ud800 buffer");
	}

	CharBuffer getUnmapCharBuffer() {
		return null;
	}

	CharBuffer getExceptionCharBuffer() {
		return null;
	}

	protected byte[] getIllegalByteArray() {
		return new byte[] { (byte) 0xd8, (byte) 0x00 };
	}

}
