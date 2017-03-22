/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.java.nio.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CharsetEncoderTest extends junit.framework.TestCase {
    // None of the harmony or jtreg tests actually check that replaceWith does the right thing!
    public void test_replaceWith() throws Exception {
        Charset ascii = Charset.forName("US-ASCII");
        CharsetEncoder e = ascii.newEncoder();
        e.onMalformedInput(CodingErrorAction.REPLACE);
        e.onUnmappableCharacter(CodingErrorAction.REPLACE);
        e.replaceWith("=".getBytes("US-ASCII"));
        String input = "hello\u0666world";
        String output = ascii.decode(e.encode(CharBuffer.wrap(input))).toString();
        assertEquals("hello=world", output);
    }

    private void assertReplacementBytesForEncoder(String charset, byte[] bytes) {
        byte[] result = Charset.forName(charset).newEncoder().replacement();
        assertEquals(Arrays.toString(bytes), Arrays.toString(result));
    }

    // For all the guaranteed built-in charsets, check that we have the right default replacements.
    public void test_defaultReplacementBytesIso_8859_1() throws Exception {
        assertReplacementBytesForEncoder("ISO-8859-1", new byte[] { (byte) '?' });
    }
    public void test_defaultReplacementBytesUs_Ascii() throws Exception {
        assertReplacementBytesForEncoder("US-ASCII", new byte[] { (byte) '?' });
    }
    public void test_defaultReplacementBytesUtf_16() throws Exception {
        assertReplacementBytesForEncoder("UTF-16", new byte[] { (byte) 0xff, (byte) 0xfd });
    }
    public void test_defaultReplacementBytesUtf_16be() throws Exception {
        assertReplacementBytesForEncoder("UTF-16BE", new byte[] { (byte) 0xff, (byte) 0xfd });
    }
    public void test_defaultReplacementBytesUtf_16le() throws Exception {
        assertReplacementBytesForEncoder("UTF-16LE", new byte[] { (byte) 0xfd, (byte) 0xff });
    }
    public void test_defaultReplacementBytesUtf_8() throws Exception {
        assertReplacementBytesForEncoder("UTF-8", new byte[] { (byte) '?' });
    }

    public void testSurrogatePairAllAtOnce() throws Exception {
        // okay: surrogate pair seen all at once is decoded to U+20b9f.
        Charset cs = Charset.forName("UTF-32BE");
        CharsetEncoder e = cs.newEncoder();
        ByteBuffer bb = ByteBuffer.allocate(128);
        CoderResult cr = e.encode(CharBuffer.wrap(new char[] { '\ud842', '\udf9f' }), bb, false);
        assertEquals(CoderResult.UNDERFLOW, cr);
        assertEquals(4, bb.position());
        assertEquals((byte) 0x00, bb.get(0));
        assertEquals((byte) 0x02, bb.get(1));
        assertEquals((byte) 0x0b, bb.get(2));
        assertEquals((byte) 0x9f, bb.get(3));
    }

    public void testMalformedSurrogatePair() throws Exception {
        // malformed: low surrogate first is detected as an error.
        Charset cs = Charset.forName("UTF-32BE");
        CharsetEncoder e = cs.newEncoder();
        ByteBuffer bb = ByteBuffer.allocate(128);
        CoderResult cr = e.encode(CharBuffer.wrap(new char[] { '\udf9f' }), bb, false);
        assertTrue(cr.toString(), cr.isMalformed());
        assertEquals(1, cr.length());
    }

    /* J2ObjC: The IconvCharsetEncoder doesn't remember partial sequences.
    public void testCharsetEncoderSplitSurrogates_IGNORE() throws Exception {
        testCharsetEncoderSplitSurrogates(CodingErrorAction.IGNORE);
    }

    public void testCharsetEncoderSplitSurrogates_REPORT() throws Exception {
        testCharsetEncoderSplitSurrogates(CodingErrorAction.REPORT);
    }

    public void testCharsetEncoderSplitSurrogates_REPLACE() throws Exception {
        testCharsetEncoderSplitSurrogates(CodingErrorAction.REPLACE);
    }

    private void testCharsetEncoderSplitSurrogates(CodingErrorAction cea) throws Exception {
        // Writing the two halves of the surrogate pair in separate writes should work just fine.
        // This is true of Android and ICU, but not of the RI.

        // On the RI, writing the two halves of the surrogate pair in separate writes
        // is an error because the CharsetEncoder doesn't remember it's half-way through a
        // surrogate pair across the two calls!

        // IGNORE just ignores both characters, REPORT complains that the second is
        // invalid (because it doesn't remember seeing the first), and REPLACE inserts a
        // replacement character U+fffd when it sees the second character (because it too
        // doesn't remember seeing the first).

        // Android just does the right thing.

        Charset cs = Charset.forName("UTF-32BE");
        CharsetEncoder e = cs.newEncoder();
        e.onMalformedInput(cea);
        e.onUnmappableCharacter(cea);
        ByteBuffer bb = ByteBuffer.allocate(128);
        CoderResult cr = e.encode(CharBuffer.wrap(new char[] { '\ud842' }), bb, false);
        assertEquals(CoderResult.UNDERFLOW, cr);
        assertEquals(0, bb.position());
        cr = e.encode(CharBuffer.wrap(new char[] { '\udf9f' }), bb, false);
        assertEquals(CoderResult.UNDERFLOW, cr);
        int expectedPosition = 4;
        assertEquals(expectedPosition, bb.position());
        System.err.println(Arrays.toString(Arrays.copyOfRange(bb.array(), 0, bb.position())));
        assertEquals((byte) 0x00, bb.get(0));
        assertEquals((byte) 0x02, bb.get(1));
        assertEquals((byte) 0x0b, bb.get(2));
        assertEquals((byte) 0x9f, bb.get(3));
        cr = e.encode(CharBuffer.wrap(new char[] { }), bb, true);
        assertEquals(CoderResult.UNDERFLOW, cr);
        assertEquals(expectedPosition, bb.position());
        cr = e.flush(bb);
        assertEquals(CoderResult.UNDERFLOW, cr);
        assertEquals(expectedPosition, bb.position());
    }*/

    public void testFlushWithoutEndOfInput() throws Exception {
        Charset cs = Charset.forName("UTF-32BE");
        CharsetEncoder e = cs.newEncoder();
        ByteBuffer bb = ByteBuffer.allocate(128);
        CoderResult cr = e.encode(CharBuffer.wrap(new char[] { 'x' }), bb, false);
        assertEquals(CoderResult.UNDERFLOW, cr);
        assertEquals(4, bb.position());
        try {
            cr = e.flush(bb);
            fail();
        } catch (IllegalStateException expected) {
            // You must call encode with endOfInput true before you can flush.
        }

        // We had a bug where we wouldn't reset inEnd before calling encode in implFlush.
        // That would result in flush outputting garbage.
        cr = e.encode(CharBuffer.wrap(new char[] { 'x' }), bb, true);
        assertEquals(CoderResult.UNDERFLOW, cr);
        assertEquals(8, bb.position());
        cr = e.flush(bb);
        assertEquals(CoderResult.UNDERFLOW, cr);
        assertEquals(8, bb.position());
    }

    // Discards all input. Outputs a single byte 'X' on flush.
    private static final class MockCharset extends Charset {
        static final Charset INSTANCE = new MockCharset();

        private MockCharset() {
            super("MockCharset", new String[0]);
        }

        public boolean contains(Charset charset) {
            return false;
        }

        public CharsetEncoder newEncoder() {
            return new CharsetEncoder(INSTANCE, 1.f, 1.f) {
                protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
                    in.position(in.limit());
                    return CoderResult.UNDERFLOW;
                }

                protected CoderResult implFlush(ByteBuffer out) {
                    out.put((byte) 'X');
                    return CoderResult.UNDERFLOW;
                }
            };
        }

        public CharsetDecoder newDecoder() {
            return new CharsetDecoder(INSTANCE, 1.f, 1.f) {
                protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
                    in.position(in.limit());
                    return CoderResult.UNDERFLOW;
                }
            };
        }
    }

    // Repeated calls to flush() should not result in repeated calls to implFlush().
    public void testFlushNotCallingImplFlushRepeatedly() {
        CharsetEncoder e = MockCharset.INSTANCE.newEncoder();
        ByteBuffer bb = ByteBuffer.allocate(4);
        CoderResult cr = e.encode(CharBuffer.allocate(0), bb, true);
        assertEquals(CoderResult.UNDERFLOW, cr);
        cr = e.flush(bb);
        assertEquals(CoderResult.UNDERFLOW, cr);
        cr = e.flush(bb);
        assertEquals(CoderResult.UNDERFLOW, cr);
        assertEquals(1, bb.position());
        assertEquals((byte) 'X', bb.get(0));
        assertEquals(0x00, bb.get(1));
        assertEquals(0x00, bb.get(2));
        assertEquals(0x00, bb.get(3));
    }

    // http://b/19185235
    public void testFlushWithIncompleteInput() {
        CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
        ByteBuffer output = ByteBuffer.allocate(10);
        CoderResult result = encoder.encode(CharBuffer.wrap("\ud800"), output,
                true /* endOfInput */);
        /* J2ObjC: Our encoder returns malformed from the encode() call instead of the flush() call.
        assertTrue(result.isUnderflow());

        result = encoder.flush(output);*/
        assertTrue(result.isMalformed());
        assertEquals(1, result.length());
        assertEquals(0, output.position());
    }
}
