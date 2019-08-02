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

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderMalfunctionError;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.MalformedInputException;
import java.util.Arrays;

import junit.framework.TestCase;

public class CharsetDecoder2Test extends TestCase {

    /**
	 * @tests java.nio.charset.CharsetDecoder.CharsetDecoder(Charset, float,
	 *        float)
	 */
	public void test_ConstructorLjava_nio_charset_CharsetFF() {
		// Regression for HARMONY-142
		try {
			Charset cs = Charset.forName("UTF-8"); //$NON-NLS-1$
			new MockCharsetDecoderForHarmony142(cs, 1.1f, 1);
			fail("Assert 0: Should throw IllegalArgumentException."); //$NON-NLS-1$
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	/*
	 * MockCharsetDecoderForHarmony142: for constructor test
	 */
	static class MockCharsetDecoderForHarmony142 extends CharsetDecoder {
		protected MockCharsetDecoderForHarmony142(Charset cs,
				float averageBytesPerChar, float maxBytesPerChar) {
			super(cs, averageBytesPerChar, maxBytesPerChar);
		}

		protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
			return null;
		}
	}

	/**
	 * @tests java.nio.charset.CharsetDecoder#decode(java.nio.ByteBuffer)
	 */
	public void test_decode() throws CharacterCodingException {
		// Regression for HARMONY-33
//		ByteBuffer bb = ByteBuffer.allocate(1);
//		bb.put(0, (byte) 77);
//		CharsetDecoder decoder = Charset.forName("UTF-16").newDecoder();
//		decoder.onMalformedInput(CodingErrorAction.REPLACE);
//		decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
//		decoder.decode(bb);

		// Regression for HARMONY-67
//		byte[] b = new byte[] { (byte) 1 };
//		ByteBuffer buf = ByteBuffer.wrap(b);
//		CharBuffer charbuf = Charset.forName("UTF-16").decode(buf);
//		assertEquals("Assert 0: charset UTF-16", 1, charbuf.length());
//
//		charbuf = Charset.forName("UTF-16BE").decode(buf);
//		assertEquals("Assert 1: charset UTF-16BE", 0, charbuf.length());
//
//		charbuf = Charset.forName("UTF-16LE").decode(buf);
//		assertEquals("Assert 2: charset UTF16LE", 0, charbuf.length());

		// Regression for HARMONY-99
		CharsetDecoder decoder2 = Charset.forName("UTF-16").newDecoder();
		decoder2.onMalformedInput(CodingErrorAction.REPORT);
		decoder2.onUnmappableCharacter(CodingErrorAction.REPORT);
		ByteBuffer in = ByteBuffer.wrap(new byte[] { 109, 97, 109 });
		try {
			decoder2.decode(in);
			fail("Assert 3: MalformedInputException should have thrown");
		} catch (MalformedInputException e) {
			//expected
		}
	}

    /*
     * Test malfunction decode(ByteBuffer)
     */
    public void test_decodeLjava_nio_ByteBuffer() throws Exception {
		MockMalfunctionCharset cs1 = new MockMalfunctionCharset(
				"Harmony-124-1", null); //$NON-NLS-1$
		try {
			cs1.newDecoder().onMalformedInput(CodingErrorAction.REPLACE)
					.onUnmappableCharacter(CodingErrorAction.REPLACE).decode(
							ByteBuffer.wrap(new byte[] { 0x00, 0x11 }));
			fail("Assert 0: should throw CoderMalfunctionError");  // NON-NLS-1$
		} catch (CoderMalfunctionError e) {
			// expected
		}

		MockMalfunctionCharset cs2 = new MockMalfunctionCharset(
				"Harmony-124-2", null); //$NON-NLS-1$
		try {
			cs2.decode(ByteBuffer.wrap(new byte[] { 0x00, 0x11 }));
			fail("Assert 1: Charset.decode should throw CoderMalfunctionError");  // NON-NLS-1
		} catch (CoderMalfunctionError e) {
			// expected
		}
	}

	/*
	 * Mock charset class with malfunction decode & encode.
	 */
	static final class MockMalfunctionCharset extends Charset {

		public MockMalfunctionCharset(String canonicalName, String[] aliases) {
			super(canonicalName, aliases);
		}

		public boolean contains(Charset cs) {
			return false;
		}

		public CharsetDecoder newDecoder() {
			return new MockMalfunctionDecoder(this);
		}

		public CharsetEncoder newEncoder() {
			return new MockMalfunctionEncoder(this);
		}
	}

	/*
	 * Mock decoder. decodeLoop always throws unexpected exception.
	 */
	static class MockMalfunctionDecoder extends java.nio.charset.CharsetDecoder {

		public MockMalfunctionDecoder(Charset cs) {
			super(cs, 1, 10);
		}

		protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
			throw new BufferOverflowException();
		}
	}

	/*
	 * Mock encoder. encodeLoop always throws unexpected exception.
	 */
	static class MockMalfunctionEncoder extends java.nio.charset.CharsetEncoder {

		public MockMalfunctionEncoder(Charset cs) {
			super(cs, 1, 3, new byte[] { (byte) '?' });
		}

		protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
			throw new BufferOverflowException();
		}
	}

	/*
	 * Test the method decode(ByteBuffer) .
	 */
	public void testDecodeLjava_nio_ByteBuffer_ReplaceOverflow()
			throws Exception {
		String replaceString = "a";
		Charset cs = Charset.forName("UTF-8");
		MockMalformedDecoder decoder = new MockMalformedDecoder(cs);
		decoder.onMalformedInput(CodingErrorAction.REPLACE);
		decoder.replaceWith(replaceString);
		CharBuffer out = CharBuffer.allocate(1);
		// MockMalformedDecoder treats the second byte '0x38' as malformed,
		// but "out" doesn't have enough space for replace string.
		ByteBuffer in = ByteBuffer.wrap(new byte[] { 0x45, 0x38, 0x45, 0x45 });
		CoderResult result = decoder.decode(in, out, false);
		assertTrue(result.isOverflow());

		// allocate enough space for "out"
		out = CharBuffer.allocate(10);
		// replace string should be put into "out" firstly,
		// and then decode "in".
		result = decoder.decode(in, out, true);
		out.flip();
		assertTrue(result.isUnderflow());
		assertEquals("bb", out.toString());
	}

	/*
	 * Mock decoder. It treats byte whose value is less than "0x40" as
	 * malformed.
	 */
	static class MockMalformedDecoder extends java.nio.charset.CharsetDecoder {

		public MockMalformedDecoder(Charset cs) {
			super(cs, 1, 10);
		}

		/*
		 * It treats byte whose value is less than "0x40" as malformed.
		 * Otherwise, it's decoded as 'b'.
		 */
		protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
			while (in.hasRemaining()) {
				byte b = in.get();
				if (b < 0x40) {
					return CoderResult.malformedForLength(1);
				}
				if (!out.hasRemaining()) {
					return CoderResult.OVERFLOW;
				}
				out.put((char) 'b');
			}
			return CoderResult.UNDERFLOW;
		}
	}


    public void testInvalidDecoding() throws IOException {

        byte[][] invalidSequences = new byte[][] {
            // overlong NULL
            { (byte) 0xC0, (byte) 0x80 },
            // overlong ascii 'A'
            { (byte) 0xC0, (byte) 0xC1 },
            // overlong "/../"
            { (byte) 0x2F, (byte) 0xC0, (byte) 0xAE, (byte) 0x2E, (byte) 0x2F },
            // Invalid encoding 2r11111000 (sequence too long)
            { (byte) 0xF8 },
            // Invalid encoding 2r10000000 (sequence too short)
            { (byte) 0x80 }
        };

        CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);

        /*
         * When bytebuffer has a backing array...
         */
        for (byte[] bytes : invalidSequences) {
            try {
                CharBuffer cb = decoder.decode(ByteBuffer.wrap(bytes));
                fail("No exception thrown on " + Arrays.toString(bytes) + " '" + cb + "'");
            } catch (MalformedInputException expected) {
            }
        }

        /*
         * When bytebuffer has _not_ got a backing array...
         */
        for (byte[] bytes : invalidSequences) {
            try {
                ByteBuffer bb = ByteBuffer.allocateDirect(8);
                bb.put(bytes).flip();
                CharBuffer cb = decoder.decode(bb);
                fail("No exception thrown on " + Arrays.toString(bytes) + " '" + cb + "'");
            } catch (MalformedInputException expected) {
            }
        }
    }

}
