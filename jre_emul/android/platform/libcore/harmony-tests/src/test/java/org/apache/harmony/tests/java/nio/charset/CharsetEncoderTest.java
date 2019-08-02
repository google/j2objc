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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnmappableCharacterException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;

import junit.framework.TestCase;

/**
 * API unit test for java.nio.charset.CharsetEncoder
 */
public class CharsetEncoderTest extends TestCase {

	static final int MAX_BYTES = 3;

	static final float AVER_BYTES = 0.5f;

	// charset for mock class
	private static final Charset MOCKCS = new MockCharset("CharsetEncoderTest_mock", new String[0]);

	Charset cs = MOCKCS;

	// default encoder
	CharsetEncoder encoder;

	// default for Charset abstract class
	byte[] defaultReplacement = new byte[] { 63 };

	// specific for Charset implementation subclass
	byte[] specifiedReplacement = new byte[] { 63 };

	static final String unistr = " buffer";// \u8000\u8001\u00a5\u3000\r\n";

	byte[] unibytes = new byte[] { 32, 98, 117, 102, 102, 101, 114 };

	byte[] unibytesWithRep = null;

	byte[] surrogate = new byte[0];

	protected void setUp() throws Exception {
		super.setUp();
		encoder = cs.newEncoder();
		if (null == unibytesWithRep) {
			byte[] replacement = encoder.replacement();
			unibytesWithRep = new byte[replacement.length + unibytes.length];
			System.arraycopy(replacement, 0, unibytesWithRep, 0,
					replacement.length);
			System.arraycopy(unibytes, 0, unibytesWithRep, replacement.length,
					unibytes.length);
		}
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testSpecificDefaultValue() {
		assertTrue(encoder.averageBytesPerChar() == AVER_BYTES);
		assertTrue(encoder.maxBytesPerChar() == MAX_BYTES);
	}

	public void testDefaultValue() {
		assertEquals(CodingErrorAction.REPORT, encoder.malformedInputAction());
		assertEquals(CodingErrorAction.REPORT, encoder.unmappableCharacterAction());
		assertSame(encoder, encoder.onMalformedInput(CodingErrorAction.IGNORE));
		assertSame(encoder, encoder.onUnmappableCharacter(CodingErrorAction.IGNORE));
		if (encoder instanceof MockCharsetEncoder) {
			assertTrue(Arrays.equals(encoder.replacement(), defaultReplacement));
		} else {
			assertTrue(Arrays.equals(encoder.replacement(), specifiedReplacement));
		}

	}

	/*
	 * Class under test for constructor CharsetEncoder(Charset, float, float)
	 */
	public void testCharsetEncoderCharsetfloatfloat() {
		// default value
		encoder = new MockCharsetEncoder(cs, (float) AVER_BYTES, MAX_BYTES);
		assertSame(encoder.charset(), cs);
		assertTrue(encoder.averageBytesPerChar() == AVER_BYTES);
		assertTrue(encoder.maxBytesPerChar() == MAX_BYTES);
		assertEquals(CodingErrorAction.REPORT, encoder.malformedInputAction());
		assertEquals(CodingErrorAction.REPORT, encoder
				.unmappableCharacterAction());
		assertEquals(new String(encoder.replacement()), new String(
				defaultReplacement));
		assertSame(encoder, encoder.onMalformedInput(CodingErrorAction.IGNORE));
		assertSame(encoder, encoder
				.onUnmappableCharacter(CodingErrorAction.IGNORE));

		// normal case
		CharsetEncoder ec = new MockCharsetEncoder(cs, 1, MAX_BYTES);
		assertSame(ec.charset(), cs);
		assertEquals(1.0, ec.averageBytesPerChar(), 0);
		assertTrue(ec.maxBytesPerChar() == MAX_BYTES);

		/*
		 * ------------------------ Exceptional cases -------------------------
		 */
		// NullPointerException: null charset
		try {
			ec = new MockCharsetEncoder(null, 1, MAX_BYTES);
			fail("should throw null pointer exception");
		} catch (NullPointerException e) {
		}

		ec = new MockCharsetEncoder(new MockCharset("mock", new String[0]), 1,
				MAX_BYTES);

                // Commented out since the comment is wrong since MAX_BYTES > 1
		// // OK: average length less than max length
		// ec = new MockCharsetEncoder(cs, MAX_BYTES, 1);
		// assertTrue(ec.averageBytesPerChar() == MAX_BYTES);
		// assertTrue(ec.maxBytesPerChar() == 1);

		// Illegal Argument: zero length
		try {
			ec = new MockCharsetEncoder(cs, 0, MAX_BYTES);
			fail("should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
		try {
			ec = new MockCharsetEncoder(cs, 1, 0);
			fail("should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}

		// Illegal Argument: negative length
		try {
			ec = new MockCharsetEncoder(cs, -1, MAX_BYTES);
			fail("should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
		try {
			ec = new MockCharsetEncoder(cs, 1, -1);
			fail("should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
	}

	/*
	 * Class under test for constructor CharsetEncoder(Charset, float, float,
	 * byte[])
	 */
	public void testCharsetEncoderCharsetfloatfloatbyteArray() {
		byte[] ba = getLegalByteArray();
		// normal case
		CharsetEncoder ec = new MockCharsetEncoder(cs, 1, MAX_BYTES, ba);
		assertSame(ec.charset(), cs);
		assertEquals(1.0, ec.averageBytesPerChar(), 0.0);
		assertTrue(ec.maxBytesPerChar() == MAX_BYTES);
		assertSame(ba, ec.replacement());

		/*
		 * ------------------------ Exceptional cases -------------------------
		 */
		// NullPointerException: null charset
		try {
			ec = new MockCharsetEncoder(null, 1, MAX_BYTES, ba);
			fail("should throw null pointer exception");
		} catch (NullPointerException e) {
		}

		// Illegal Argument: null byte array
		try {
			ec = new MockCharsetEncoder(cs, 1, MAX_BYTES, null);
			fail("should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
		// Illegal Argument: empty byte array
		try {
			ec = new MockCharsetEncoder(cs, 1, MAX_BYTES, new byte[0]);
			fail("should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
		// Illegal Argument: byte array is longer than max length
		try {
			ec = new MockCharsetEncoder(cs, 1, MAX_BYTES, new byte[] { 1, 2,
					MAX_BYTES, 4 });
			fail("should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}

                // Commented out since the comment is wrong since MAX_BYTES > 1
                // This test throws IllegalArgumentException on Harmony and RI
		// // OK: average length less than max length
		// ec = new MockCharsetEncoder(cs, MAX_BYTES, ba.length, ba);
		// assertTrue(ec.averageBytesPerChar() == MAX_BYTES);
		// assertTrue(ec.maxBytesPerChar() == ba.length);

		// Illegal Argument: zero length
		try {
			ec = new MockCharsetEncoder(cs, 0, MAX_BYTES, ba);
			fail("should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
		try {
			ec = new MockCharsetEncoder(cs, 1, 0, ba);
			fail("should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}

		// Illegal Argument: negative length
		try {
			ec = new MockCharsetEncoder(cs, -1, MAX_BYTES, ba);
			fail("should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
		try {
			ec = new MockCharsetEncoder(cs, 1, -1, ba);
			fail("should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
	}

	/*
	 * Class under test for boolean canEncode(char)
	 */
	public void testCanEncodechar() throws CharacterCodingException {
		// for non-mapped char
		assertTrue(encoder.canEncode('\uc2c0'));
		// surrogate char for unicode
		// 1st byte: d800-dbff
		// 2nd byte: dc00-dfff
		assertTrue(encoder.canEncode('\ud800'));
		// valid surrogate pair
		assertTrue(encoder.canEncode('\udc00'));
	}

	/*-----------------------------------------
	 * Class under test for illegal state case
	 * methods which can change internal states are two encode, flush, two canEncode, reset
	 * -----------------------------------------
	 */

	// Normal case: just after reset, and it also means reset can be done
	// anywhere
	public void testResetIllegalState() throws CharacterCodingException {
		assertSame(encoder, encoder.reset());
		encoder.canEncode('\ud901');
		assertSame(encoder, encoder.reset());
		encoder.canEncode("\ud901\udc00");
		assertSame(encoder, encoder.reset());
		encoder.encode(CharBuffer.wrap("aaa"));
		assertSame(encoder, encoder.reset());
		encoder.encode(CharBuffer.wrap("aaa"), ByteBuffer.allocate(3), false);
		assertSame(encoder, encoder.reset());
		encoder.encode(CharBuffer.wrap("aaa"), ByteBuffer.allocate(3), true);
		assertSame(encoder, encoder.reset());
	}

  public void testFlushIllegalState() throws CharacterCodingException {
    CharBuffer in = CharBuffer.wrap("aaa");
    ByteBuffer out = ByteBuffer.allocate(5);

    // Illegal state: after reset.
    encoder.reset();
    try {
      encoder.flush(out);
      fail();
    } catch (IllegalStateException expected) {
    }

    // Normal case: after encode with endOfInput is true
    assertSame(encoder, encoder.reset());
    encoder.encode(in, out, true);
    out.rewind();
    CoderResult result = encoder.flush(out);

    // Good state: flush twice
    encoder.flush(out);

    // Illegal state: flush after encode with endOfInput is false
    assertSame(encoder, encoder.reset());
    encoder.encode(in, out, false);
    try {
      encoder.flush(out);
      fail();
    } catch (IllegalStateException expected) {
    }
  }

	public void testFlushAfterConstructing() {
		ByteBuffer out = ByteBuffer.allocate(5);

		//Illegal state: flush after instance created
		try {
			encoder.flush(out);
			fail("should throw IllegalStateException");
		} catch (IllegalStateException e) {
			// Expected
		}

	}

	// test illegal states for encode facade
	public void testEncodeFacadeIllegalState() throws CharacterCodingException {
		// encode facade can be execute in anywhere
		CharBuffer in = CharBuffer.wrap("aaa");
		// Normal case: just created
		encoder.encode(in);
		in.rewind();

		// Normal case: just after encode facade
		encoder.encode(in);
		in.rewind();

		// Normal case: just after canEncode
		assertSame(encoder, encoder.reset());
		encoder.canEncode("\ud902\udc00");
		encoder.encode(in);
		in.rewind();
		assertSame(encoder, encoder.reset());
		encoder.canEncode('\ud902');
		encoder.encode(in);
		in.rewind();

		// Normal case: just after encode with that endOfInput is true
		assertSame(encoder, encoder.reset());
		encoder.encode(CharBuffer.wrap("testCanEncodeIllegalState2"),
				ByteBuffer.allocate(30), true);
		encoder.encode(in);
		in.rewind();

		// Normal case:just after encode with that endOfInput is false
		assertSame(encoder, encoder.reset());
		encoder.encode(CharBuffer.wrap("testCanEncodeIllegalState3"),
				ByteBuffer.allocate(30), false);
		encoder.encode(in);
		in.rewind();

		// Normal case: just after flush
		assertSame(encoder, encoder.reset());
		encoder.encode(CharBuffer.wrap("testCanEncodeIllegalState4"),
				ByteBuffer.allocate(30), true);
		encoder.flush(ByteBuffer.allocate(10));
		encoder.encode(in);
		in.rewind();
	}

	// test illegal states for two encode method with endOfInput is true
	public void testEncodeTrueIllegalState() throws CharacterCodingException {
		CharBuffer in = CharBuffer.wrap("aaa");
		ByteBuffer out = ByteBuffer.allocate(5);
		// Normal case: just created
		encoder.encode(in, out, true);
		in.rewind();
		out.rewind();

		in.rewind();
		out.rewind();

		// Normal case: just after encode with that endOfInput is true
		assertSame(encoder, encoder.reset());
		encoder.encode(CharBuffer.wrap("testCanEncodeIllegalState2"),
				ByteBuffer.allocate(30), true);
		encoder.encode(in, out, true);
		in.rewind();
		out.rewind();

		// Normal case:just after encode with that endOfInput is false
		assertSame(encoder, encoder.reset());
		encoder.encode(CharBuffer.wrap("testCanEncodeIllegalState3"),
				ByteBuffer.allocate(30), false);
		encoder.encode(in, out, true);
		in.rewind();
		out.rewind();

		// Illegal state: just after flush
		assertSame(encoder, encoder.reset());
		encoder.encode(CharBuffer.wrap("testCanEncodeIllegalState4"),
				ByteBuffer.allocate(30), true);
		encoder.flush(ByteBuffer.allocate(10));
		try {
			encoder.encode(in, out, true);
			fail("should illegal state");
		} catch (IllegalStateException e) {
		}

		// Normal case: after canEncode
		assertSame(encoder, encoder.reset());
		encoder.canEncode("\ud906\udc00");
		encoder.encode(in, out, true);
		in.rewind();
		out.rewind();
		assertSame(encoder, encoder.reset());
		encoder.canEncode('\ud905');
		encoder.encode(in, out, true);
	}

	// test illegal states for two encode method with endOfInput is false
	public void testEncodeFalseIllegalState() throws CharacterCodingException {
		CharBuffer in = CharBuffer.wrap("aaa");
		ByteBuffer out = ByteBuffer.allocate(5);
		// Normal case: just created
		encoder.encode(in, out, false);
		in.rewind();
		out.rewind();

		// Illegal state: just after encode facade
		assertSame(encoder, encoder.reset());
		encoder.encode(CharBuffer.wrap("testCanEncodeIllegalState1"));
		try {
			encoder.encode(in, out, false);
			fail("should illegal state");
		} catch (IllegalStateException e) {
		}

		// Illegal state: just after encode with that endOfInput is true
		assertSame(encoder, encoder.reset());
		encoder.encode(CharBuffer.wrap("testCanEncodeIllegalState2"),
				ByteBuffer.allocate(30), true);
		try {
			encoder.encode(in, out, false);
			fail("should illegal state");
		} catch (IllegalStateException e) {
		}

		// Normal case:just after encode with that endOfInput is false
		assertSame(encoder, encoder.reset());
		encoder.encode(CharBuffer.wrap("testCanEncodeIllegalState3"),
				ByteBuffer.allocate(30), false);
		encoder.encode(in, out, false);
		in.rewind();
		out.rewind();

		// Illegal state: just after flush
		assertSame(encoder, encoder.reset());
		encoder.encode(CharBuffer.wrap("testCanEncodeIllegalState4"),
				ByteBuffer.allocate(30), true);
		encoder.flush(ByteBuffer.allocate(10));
		try {
			encoder.encode(in, out, false);
			fail("should illegal state");
		} catch (IllegalStateException e) {
		}

		// Normal case: after canEncode
		assertSame(encoder, encoder.reset());
		encoder.canEncode("\ud906\udc00");
		encoder.encode(in, out, false);
		in.rewind();
		out.rewind();
		assertSame(encoder, encoder.reset());
		encoder.canEncode('\ud905');
		encoder.encode(in, out, false);
	}

	// test illegal states for two canEncode methods
	public void testCanEncodeIllegalState() throws CharacterCodingException {
		// Normal case: just created
		encoder.canEncode("\ud900\udc00");
		encoder.canEncode('\ud900');

		// Illegal state: just after encode with that endOfInput is true
		assertSame(encoder, encoder.reset());
		encoder.encode(CharBuffer.wrap("testCanEncodeIllegalState2"),
				ByteBuffer.allocate(30), true);
		try {
			encoder.canEncode("\ud903\udc00");
			fail("should throw illegal state exception");
		} catch (IllegalStateException e) {
		}

		// Illegal state:just after encode with that endOfInput is false
		assertSame(encoder, encoder.reset());
		encoder.encode(CharBuffer.wrap("testCanEncodeIllegalState3"),
				ByteBuffer.allocate(30), false);
		try {
			encoder.canEncode("\ud904\udc00");
			fail("should throw illegal state exception");
		} catch (IllegalStateException e) {
		}

		// Normal case: just after flush
		encoder.encode(CharBuffer.wrap("testCanEncodeIllegalState4"),
				ByteBuffer.allocate(30), true);
		encoder.flush(ByteBuffer.allocate(10));
		encoder.canEncode("\ud905\udc00");
		encoder.canEncode('\ud906');

		// Normal case: after reset again
		assertSame(encoder, encoder.reset());
		encoder.canEncode("\ud906\udc00");
		encoder.canEncode('\ud905');
	}

	/*
	 * --------------------------------- illegal state test end
	 * ---------------------------------
	 */

	/*
	 * Class under test for boolean canEncode(CharSequence)
	 */
	public void testCanEncodeCharSequence() {
		// for non-mapped char
		assertTrue(encoder.canEncode("\uc2c0"));
		// surrogate char for unicode
		// 1st byte: d800-dbff
		// 2nd byte: dc00-dfff
		// valid surrogate pair
		assertTrue(encoder.canEncode("\ud800\udc00"));
		// invalid surrogate pair
		assertTrue(encoder.canEncode("\ud800\udb00"));
	}

    public void test_canEncode_empty() throws Exception {
        assertTrue(encoder.canEncode(""));
    }

    public void test_canEncode_null() throws Exception {
        try {
            encoder.canEncode(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

	/*
	 * Class under test for Charset charset()
	 */
	public void testCharset() {
		try {
			encoder = new MockCharsetEncoder(Charset.forName("gbk"), 1,
					MAX_BYTES);
			// assertSame(encoder.charset(), Charset.forName("gbk"));
		} catch (UnsupportedCharsetException e) {
			System.err
					.println("Don't support GBK encoding, ignore current test");
		}
	}

	/*
	 * Class under test for ByteBuffer encode(CharBuffer)
	 */
	public void testEncodeCharBuffer() throws CharacterCodingException {
		// Null pointer
		try {
			encoder.encode(null);
			fail("should throw null pointer exception");
		} catch (NullPointerException e) {
		}

		// empty input buffer
		ByteBuffer out = encoder.encode(CharBuffer.wrap(""));
		assertEquals(out.position(), 0);
		assertByteArray(out, new byte[0]);
		// assertByteArray(out, surrogate);

		// normal case
		out = encoder.encode(CharBuffer.wrap(unistr));
		assertEquals(out.position(), 0);
		assertByteArray(out, addSurrogate(unibytes));

        // Regression test for harmony-3378
        Charset cs = Charset.forName("UTF-8");
        CharsetEncoder encoder = cs.newEncoder();
        encoder.onMalformedInput(CodingErrorAction.REPLACE);
        encoder = encoder.replaceWith(new byte[] { (byte) 0xef, (byte) 0xbf,
                (byte) 0xbd, });
        CharBuffer in = CharBuffer.wrap("\ud800");
        out = encoder.encode(in);
        assertNotNull(out);
	}

	private byte[] addSurrogate(byte[] expected) {
		if (surrogate.length > 0) {
			byte[] temp = new byte[surrogate.length + expected.length];
			System.arraycopy(surrogate, 0, temp, 0, surrogate.length);
			System.arraycopy(expected, 0, temp, surrogate.length,
					expected.length);
			expected = temp;
		}
		return expected;
	}

	/**
	 * @return
	 */
	protected byte[] getEmptyByteArray() {
		return new byte[0];
	}

	CharBuffer getMalformedCharBuffer() {
		return CharBuffer.wrap("malform buffer");
	}

	CharBuffer getUnmapCharBuffer() {
		return CharBuffer.wrap("unmap buffer");
	}

	CharBuffer getExceptionCharBuffer() {
		return CharBuffer.wrap("runtime buffer");
	}

	public void testEncodeCharBufferException() throws CharacterCodingException {
		ByteBuffer out;
		CharBuffer in;
		// MalformedException:
		in = getMalformedCharBuffer();
		encoder.onMalformedInput(CodingErrorAction.REPORT);
		encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
		if (in != null) {
			try {
                // regression test for Harmony-1379
				encoder.encode(in);
				fail("should throw MalformedInputException");
			} catch (MalformedInputException e) {
			}

			encoder.reset();
			in.rewind();
			encoder.onMalformedInput(CodingErrorAction.IGNORE);
			out = encoder.encode(in);
			assertByteArray(out, addSurrogate(unibytes));

			encoder.reset();
			in.rewind();
			encoder.onMalformedInput(CodingErrorAction.REPLACE);
			out = encoder.encode(in);
			assertByteArray(out, addSurrogate(unibytesWithRep));
		}

		// Unmapped Exception:
		in = getUnmapCharBuffer();
		encoder.onMalformedInput(CodingErrorAction.REPORT);
		encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
		if (in != null) {
			encoder.reset();
			try {
				encoder.encode(in);
				fail("should throw UnmappableCharacterException");
			} catch (UnmappableCharacterException e) {
			}

			encoder.reset();
			in.rewind();
			encoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
			out = encoder.encode(in);
			assertByteArray(out, unibytes);

			encoder.reset();
			in.rewind();
			encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
			out = encoder.encode(in);
			assertByteArray(out, unibytesWithRep);
		}

		// RuntimeException
		try {
			encoder.encode(getExceptionCharBuffer());
			fail("should throw runtime exception");
		} catch (RuntimeException e) {
		}
	}

	/*
	 * utility method, extract given bytebuffer to a string and compare with
	 * give string
	 */
	void assertByteArray(ByteBuffer out, byte[] expected) {
		out = out.duplicate();
		if (out.position() != 0) {
			out.flip();
		}
		byte[] ba = new byte[out.limit() - out.position()];
		out.get(ba);
		// byte[] ba = out.array();
		assertTrue(Arrays.equals(ba, expected));
	}

	/*
	 * Class under test for CoderResult encode(CharBuffer, ByteBuffer, boolean)
	 */
	public void testEncodeCharBufferByteBufferboolean()
			throws CharacterCodingException {
		ByteBuffer out = ByteBuffer.allocate(200);
		CharBuffer in = CharBuffer.wrap(unistr);
		// Null pointer
		try {
			encoder.encode(null, out, true);
			fail("should throw null pointer exception");
		} catch (NullPointerException e) {
		}
		try {
			encoder.encode(in, null, true);
			fail("should throw null pointer exception");
		} catch (NullPointerException e) {
		}

		// normal case, one complete operation
		assertSame(encoder, encoder.reset());
		in.rewind();
		out.rewind();
		assertSame(CoderResult.UNDERFLOW, encoder.encode(in, out, true));
		assertEquals(out.limit(), 200);
		assertTrue(out.position() > 0);
		assertTrue(out.remaining() > 0);
		assertEquals(out.capacity(), 200);
		assertByteArray(out, addSurrogate(unibytes));
		in.rewind();

		encoder.flush(out);

		// normal case, one complete operation, but call twice, first time set
		// endOfInput to false
		assertSame(encoder, encoder.reset());
		in.rewind();
		out = ByteBuffer.allocate(200);
		assertSame(CoderResult.UNDERFLOW, encoder.encode(in, out, false));
		assertEquals(out.limit(), 200);
		assertTrue(out.position() > 0);
		assertTrue(out.remaining() > 0);
		assertEquals(out.capacity(), 200);
		assertByteArray(out, addSurrogate(unibytes));

		in.rewind();
		assertSame(CoderResult.UNDERFLOW, encoder.encode(in, out, false));
		in.rewind();
		assertSame(CoderResult.UNDERFLOW, encoder.encode(in, out, true));
		assertEquals(out.limit(), 200);
		assertTrue(out.position() > 0);
		assertTrue(out.remaining() > 0);
		assertEquals(out.capacity(), 200);

		assertByteArray(out, addSurrogate(duplicateByteArray(unibytes, 3)));

		// overflow
		out = ByteBuffer.allocate(4);
		assertSame(encoder, encoder.reset());
		in.rewind();
		out.rewind();
		assertSame(CoderResult.OVERFLOW, encoder.encode(in, out, true));
		assertEquals(out.limit(), 4);
		assertEquals(out.position(), 4);
		assertEquals(out.remaining(), 0);
		assertEquals(out.capacity(), 4);
		ByteBuffer temp = ByteBuffer.allocate(200);
		out.flip();
		temp.put(out);
		out = temp;
		assertSame(CoderResult.UNDERFLOW, encoder.encode(in, out, true));
		assertEquals(out.limit(), 200);
		assertTrue(out.position() > 0);
		assertTrue(out.remaining() > 0);
		assertEquals(out.capacity(), 200);
		assertByteArray(out, addSurrogate(unibytes));

		assertSame(encoder, encoder.reset());
		in.rewind();
		out = ByteBuffer.allocate(4);
		assertSame(CoderResult.OVERFLOW, encoder.encode(in, out, false));
		assertEquals(out.limit(), 4);
		assertEquals(out.position(), 4);
		assertEquals(out.remaining(), 0);
		assertEquals(out.capacity(), 4);
		temp = ByteBuffer.allocate(200);
		out.flip();
		temp.put(out);
		out = temp;
		assertSame(CoderResult.UNDERFLOW, encoder.encode(in, out, false));
		assertEquals(out.limit(), 200);
		assertTrue(out.position() > 0);
		assertTrue(out.remaining() > 0);
		assertEquals(out.capacity(), 200);
		assertByteArray(out, addSurrogate(unibytes));
	}

	void printByteBuffer(ByteBuffer buffer) {
		System.out.println("print buffer");
		if (buffer.position() != 0) {
			buffer.flip();
		}
		byte[] ba = buffer.array();
		for (int i = 0; i < ba.length; i++) {
			System.out.println(Integer.toHexString(ba[i]));
		}
	}

	public void testEncodeCharBufferByteBufferbooleanExceptionFalse()
			throws CharacterCodingException {
		implTestEncodeCharBufferByteBufferbooleanException(false);
	}

	public void testEncodeCharBufferByteBufferbooleanExceptionTrue()
			throws CharacterCodingException {
		implTestEncodeCharBufferByteBufferbooleanException(true);
	}

	private byte[] duplicateByteArray(byte[] ba, int times) {
		byte[] result = new byte[ba.length * times];
		for (int i = 0; i < times; i++) {
			System.arraycopy(ba, 0, result, i * ba.length, ba.length);
		}
		return result;
	}

	protected void implTestEncodeCharBufferByteBufferbooleanException(
			boolean endOfInput) throws CharacterCodingException {
		ByteBuffer out = ByteBuffer.allocate(100);

		// MalformedException:
		CharBuffer in = getMalformedCharBuffer();
		encoder.onMalformedInput(CodingErrorAction.REPORT);
		encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
		if (in != null) {
			encoder.reset();
			CoderResult r = encoder.encode(in, out, endOfInput);
			assertTrue(r.isMalformed());

			encoder.reset();
			out.clear();
			in.rewind();
			encoder.onMalformedInput(CodingErrorAction.IGNORE);
			assertSame(CoderResult.UNDERFLOW, encoder.encode(in, out,
					endOfInput));
			assertCodingErrorAction(endOfInput, out, in, unibytes);

			encoder.reset();
			out.clear();
			in.rewind();
			encoder.onMalformedInput(CodingErrorAction.REPLACE);
			assertSame(CoderResult.UNDERFLOW, encoder.encode(in, out,
					endOfInput));
			assertCodingErrorAction(endOfInput, out, in, unibytesWithRep);
		} else {
			System.out.println("Cannot find malformed char buffer for "
					+ cs.name());
		}

		// Unmapped Exception:
		in = getUnmapCharBuffer();
		encoder.onMalformedInput(CodingErrorAction.REPORT);
		encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
		if (in != null) {
			encoder.reset();
			out.clear();
			assertTrue(encoder.encode(in, out, endOfInput).isUnmappable());

			encoder.reset();
			out.clear();
			in.rewind();
			encoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
			assertSame(CoderResult.UNDERFLOW, encoder.encode(in, out,
					endOfInput));
			assertCodingErrorAction(endOfInput, out, in, unibytes);

			encoder.reset();
			out.clear();
			in.rewind();
			encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
			assertSame(CoderResult.UNDERFLOW, encoder.encode(in, out,
					endOfInput));
			assertCodingErrorAction(endOfInput, out, in, unibytesWithRep);
		} else {
			System.out.println("Cannot find unmapped char buffer for "
					+ cs.name());
		}

		// RuntimeException
		try {
			encoder.encode(getExceptionCharBuffer());
			fail("should throw runtime exception");
		} catch (RuntimeException e) {
		}
	}

	private void assertCodingErrorAction(boolean endOfInput, ByteBuffer out,
			CharBuffer in, byte[] expect) {
		if (endOfInput) {
			assertByteArray(out, addSurrogate(expect));
		} else {
			in.rewind();
			assertSame(CoderResult.UNDERFLOW, encoder.encode(in, out,
					endOfInput));
			in.rewind();
			assertSame(CoderResult.UNDERFLOW, encoder.encode(in, out, true));
			assertByteArray(out, addSurrogate(duplicateByteArray(expect, 3)));
		}
	}

	/*
	 * Class under test for CoderResult flush(ByteBuffer)
	 */
	public void testFlush() throws CharacterCodingException {
		ByteBuffer out = ByteBuffer.allocate(6);
		CharBuffer in = CharBuffer.wrap("aaa");
		assertEquals(in.remaining(), 3);

		// by encode facade, so that internal state will be wrong
		encoder.encode(CharBuffer.wrap("testFlush"), ByteBuffer.allocate(20),
				true);
		assertSame(CoderResult.UNDERFLOW, encoder
				.flush(ByteBuffer.allocate(50)));
	}

	/*
	 * test isLegalReplacement(byte[])
	 */
	public void test_isLegalReplacement_null() {
		try {
			encoder.isLegalReplacement(null);
			fail("should throw null pointer exception");
		} catch (NullPointerException e) {
		}
	}

	public void test_isLegalReplacement_good() {
		assertTrue(encoder.isLegalReplacement(specifiedReplacement));
	}

	public void test_isLegalReplacement_bad() {
		assertTrue(encoder.isLegalReplacement(new byte[200]));
		byte[] ba = getIllegalByteArray();
		if (ba != null) {
			assertFalse(encoder.isLegalReplacement(ba));
		}
	}

	public void test_isLegalReplacement_empty_array() {
		// ISO, ASC, GB, UTF8 encoder will throw exception in RI
		// others will pass
		assertTrue(encoder.isLegalReplacement(new byte[0]));
	}

	public void testOnMalformedInput() {
		assertSame(CodingErrorAction.REPORT, encoder.malformedInputAction());
		try {
			encoder.onMalformedInput(null);
			fail("should throw null pointer exception");
		} catch (IllegalArgumentException e) {
		}
		encoder.onMalformedInput(CodingErrorAction.IGNORE);
		assertSame(CodingErrorAction.IGNORE, encoder.malformedInputAction());
	}

	public void testOnUnmappableCharacter() {
		assertSame(CodingErrorAction.REPORT, encoder
				.unmappableCharacterAction());
		try {
			encoder.onUnmappableCharacter(null);
			fail("should throw null pointer exception");
		} catch (IllegalArgumentException e) {
		}
		encoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
		assertSame(CodingErrorAction.IGNORE, encoder
				.unmappableCharacterAction());
	}

	public void testReplacement() {
		try {
			encoder.replaceWith(null);
			fail("should throw null pointer exception");
		} catch (IllegalArgumentException e) {
		}
		try {
			encoder.replaceWith(new byte[0]);
			fail("should throw null pointer exception");
		} catch (IllegalArgumentException e) {
		}
		try {
			encoder.replaceWith(new byte[100]);
			fail("should throw null pointer exception");
		} catch (IllegalArgumentException e) {
		}

		byte[] nr = getLegalByteArray();
		assertSame(encoder, encoder.replaceWith(nr));
		assertSame(nr, encoder.replacement());

		nr = getIllegalByteArray();
		try {
			encoder.replaceWith(new byte[100]);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	protected byte[] getLegalByteArray() {
		return new byte[] { 'a' };
	}

	protected byte[] getIllegalByteArray() {
		return new byte[155];
	}

	/*
	 * Mock subclass of CharsetEncoder For protected method test
	 */
	public static class MockCharsetEncoder extends CharsetEncoder {

		boolean flushed = false;

		public boolean isFlushed() {
			boolean result = flushed;
			flushed = false;
			return result;
		}

		public boolean isLegalReplacement(byte[] ba) {
			if (ba.length == 155) {// specified magic number, return false
				return false;
			}
			return super.isLegalReplacement(ba);
		}

		public MockCharsetEncoder(Charset cs, float aver, float max) {
			super(cs, aver, max);
		}

		public MockCharsetEncoder(Charset cs, float aver, float max,
				byte[] replacement) {
			super(cs, aver, max, replacement);
		}

		protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
			int inPosition = in.position();
			char[] input = new char[in.remaining()];
			in.get(input);
			String result = new String(input);
			if (result.startsWith("malform")) {
				// reset the cursor to the error position
				in.position(inPosition);
				// in.position(0);
				// set the error length
				return CoderResult.malformedForLength("malform".length());
			} else if (result.startsWith("unmap")) {
				// reset the cursor to the error position
				in.position(inPosition);
				// in.position(0);
				// set the error length
				return CoderResult.unmappableForLength("unmap".length());
			} else if (result.startsWith("runtime")) {
				// reset the cursor to the error position
				in.position(0);
				// set the error length
				throw new RuntimeException("runtime");
			}
			int inLeft = input.length;
			int outLeft = out.remaining();
			CoderResult r = CoderResult.UNDERFLOW;
			int length = inLeft;
			if (outLeft < inLeft) {
				r = CoderResult.OVERFLOW;
				length = outLeft;
				in.position(inPosition + outLeft);
			}
			for (int i = 0; i < length; i++) {
				out.put((byte) input[i]);
			}
			return r;
		}

		protected CoderResult implFlush(ByteBuffer out) {
			CoderResult result = super.implFlush(out);
			int length = 0;
			if (out.remaining() >= 5) {
				length = 5;
				result = CoderResult.UNDERFLOW;
				flushed = true;
				// for (int i = 0; i < length; i++) {
				// out.put((byte)'f');
				// }
			} else {
				length = out.remaining();
				result = CoderResult.OVERFLOW;
			}
			return result;
		}

		protected void implReplaceWith(byte[] ba) {
			assertSame(ba, replacement());
		}

	}

	/*
	 * mock charset for test encoder initialization
	 */
	public static class MockCharset extends Charset {
		protected MockCharset(String arg0, String[] arg1) {
			super(arg0, arg1);
		}

		public boolean contains(Charset arg0) {
			return false;
		}

		public CharsetDecoder newDecoder() {
			return new CharsetDecoderTest.MockCharsetDecoder(this,
					(float) AVER_BYTES, MAX_BYTES);
		}

		public CharsetEncoder newEncoder() {
			return new MockCharsetEncoder(this, (float) AVER_BYTES, MAX_BYTES);
		}
	}

}
