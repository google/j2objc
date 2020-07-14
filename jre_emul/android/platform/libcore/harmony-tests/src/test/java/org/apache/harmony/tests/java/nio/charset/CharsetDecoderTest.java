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

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnmappableCharacterException;

import junit.framework.TestCase;

/**
 * API unit test for java.nio.CharsetDecoder
 */
public class CharsetDecoderTest extends TestCase {

    protected static final int MAX_BYTES = 3;

    protected static final double AVER_BYTES = 0.5;

    // default charset
    private static final Charset MOCKCS = new CharsetEncoderTest.MockCharset(
            "mock", new String[0]);

    Charset cs = MOCKCS;

    // default decoder
    protected static CharsetDecoder decoder;

    String bom = "";

    protected void setUp() throws Exception {
        super.setUp();
        decoder = cs.newDecoder();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // FIXME: give up this tests
    // /*
    // * test default value
    // */
    // public void testDefaultCharsPerByte() {
    // assertTrue(decoder.averageCharsPerByte() == AVER_BYTES);
    // assertTrue(decoder.maxCharsPerByte() == MAX_BYTES);
    // }

    public void testDefaultValues() {
        assertSame(cs, decoder.charset());
        try {
            decoder.detectedCharset();
            fail("should unsupported");
        } catch (UnsupportedOperationException e) {
        }
        try {
            assertTrue(decoder.isCharsetDetected());
            fail("should unsupported");
        } catch (UnsupportedOperationException e) {
        }
        assertFalse(decoder.isAutoDetecting());
        assertSame(CodingErrorAction.REPORT, decoder.malformedInputAction());
        assertSame(CodingErrorAction.REPORT, decoder
                .unmappableCharacterAction());
        assertEquals(decoder.replacement(), "\ufffd");
    }

    /*
     * test constructor
     */
    public void testCharsetDecoder() {
        // default value
        decoder = new MockCharsetDecoder(cs, (float) AVER_BYTES, MAX_BYTES);

        // normal case
        CharsetDecoder ec = new MockCharsetDecoder(cs, 1, MAX_BYTES);
        assertSame(ec.charset(), cs);
        assertEquals(1.0, ec.averageCharsPerByte(), 0.0);
        assertTrue(ec.maxCharsPerByte() == MAX_BYTES);

        /*
         * ------------------------ Exceptional cases -------------------------
         */
        // Normal case: null charset
        ec = new MockCharsetDecoder(null, 1, MAX_BYTES);
        assertNull(ec.charset());
        assertEquals(1.0, ec.averageCharsPerByte(), 0.0);
        assertTrue(ec.maxCharsPerByte() == MAX_BYTES);

        ec = new MockCharsetDecoder(new CharsetEncoderTest.MockCharset("mock",
                new String[0]), 1, MAX_BYTES);

        // Commented out since the comment is wrong since MAX_BYTES > 1
        // // OK: average length less than max length
        // ec = new MockCharsetDecoder(cs, MAX_BYTES, 1);
        // assertTrue(ec.averageCharsPerByte() == MAX_BYTES);
        // assertTrue(ec.maxCharsPerByte() == 1);

        // Illegal Argument: zero length
        try {
            ec = new MockCharsetDecoder(cs, 0, MAX_BYTES);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            ec = new MockCharsetDecoder(cs, 1, 0);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

        // Illegal Argument: negative length
        try {
            ec = new MockCharsetDecoder(cs, -1, MAX_BYTES);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            ec = new MockCharsetDecoder(cs, 1, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    /*
     * test onMalformedInput
     */
    public void testOnMalformedInput() {
        assertSame(CodingErrorAction.REPORT, decoder.malformedInputAction());
        try {
            decoder.onMalformedInput(null);
            fail("should throw null pointer exception");
        } catch (IllegalArgumentException e) {
        }
        decoder.onMalformedInput(CodingErrorAction.IGNORE);
        assertSame(CodingErrorAction.IGNORE, decoder.malformedInputAction());
    }

    /*
     * test unmappableCharacter
     */
    public void testOnUnmappableCharacter() {
        assertSame(CodingErrorAction.REPORT, decoder
                .unmappableCharacterAction());
        try {
            decoder.onUnmappableCharacter(null);
            fail("should throw null pointer exception");
        } catch (IllegalArgumentException e) {
        }
        decoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
        assertSame(CodingErrorAction.IGNORE, decoder
                .unmappableCharacterAction());
    }

    /*
     * test replaceWith
     */
    public void testReplaceWith() {
        try {
            decoder.replaceWith(null);
            fail("should throw null pointer exception");
        } catch (IllegalArgumentException e) {
        }
        try {
            decoder.replaceWith("");
            fail("should throw null pointer exception");
        } catch (IllegalArgumentException e) {
        }
        try {
            decoder.replaceWith("testReplaceWith");
            fail("should throw illegal argument exception");
        } catch (IllegalArgumentException e) {
        }

        decoder.replaceWith("a");
        assertSame("a", decoder.replacement());
    }

    /*
     * Class under test for CharBuffer decode(ByteBuffer)
     */
    public void testDecodeByteBuffer() throws CharacterCodingException {
        implTestDecodeByteBuffer();
    }

    void implTestDecodeByteBuffer() throws CharacterCodingException {
        // Null pointer
        try {
            decoder.decode(null);
            fail("should throw null pointer exception");
        } catch (NullPointerException e) {
        }

        // empty input buffer
        CharBuffer out = decoder.decode(ByteBuffer.allocate(0));
        assertCharBufferValue("", out);

        // normal case
        ByteBuffer in = getByteBuffer();
        out = decoder.decode(in);
        assertEquals(0, out.position());
        assertEquals(getString().length(), out.limit());
        assertEquals(getString().length(), out.remaining());
        assertCharBufferValue(getString(), out);

        // normal read only case
        in = getByteBuffer().asReadOnlyBuffer();
        out = decoder.decode(in);
        assertEquals(out.position(), 0);
        assertEquals(out.limit(), getString().length());
        assertEquals(out.remaining(), getString().length());
        assertCharBufferValue(getString(), out);
    }

    public void testDecodeByteBufferException()
            throws CharacterCodingException, UnsupportedEncodingException {
        CharBuffer out;
        ByteBuffer in;
        String replaceStr = decoder.replacement() + getString();

        // MalformedException:
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        in = getMalformedByteBuffer();
        if (in != null) {
            try {
                CharBuffer buffer = decoder.decode(in);
                assertTrue(buffer.remaining() > 0);
                fail("should throw MalformedInputException");
            } catch (MalformedInputException e) {
            }

            decoder.reset();
            in.rewind();
            decoder.onMalformedInput(CodingErrorAction.IGNORE);
            out = decoder.decode(in);
            assertCharBufferValue(getString(), out);

            decoder.reset();
            in.rewind();
            decoder.onMalformedInput(CodingErrorAction.REPLACE);
            out = decoder.decode(in);
            assertCharBufferValue(replaceStr, out);
        }

        // Unmapped Exception:
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        in = getUnmappedByteBuffer();
        if (in != null) {
            try {
                decoder.decode(in);
                fail("should throw UnmappableCharacterException");
            } catch (UnmappableCharacterException e) {
            }

            decoder.reset();
            in.rewind();
            decoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
            out = decoder.decode(in);
            assertCharBufferValue(getString(), out);

            decoder.reset();
            in.rewind();
            decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
            out = decoder.decode(in);
            assertCharBufferValue(replaceStr, out);
        }

        // RuntimeException
        try {
            decoder.decode(getExceptionByteArray());
            fail("should throw runtime exception");
        } catch (RuntimeException e) {
        }
    }

    /*
     * Class under test for CoderResult decode(ByteBuffer, CharBuffer, boolean)
     */
    public void testDecodeByteBufferCharBuffer() {
        implTestDecodeByteBufferCharBuffer(getByteBuffer());
    }

    public void testDecodeByteBufferCharBufferReadOnly() {
        implTestDecodeByteBufferCharBuffer(getByteBuffer());
    }

    void implTestDecodeByteBufferCharBuffer(ByteBuffer in) {
        CharBuffer out = CharBuffer.allocate(100);

        // Null pointer
        decoder.reset();
        try {
            decoder.decode(null, out, true);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
        try {
            decoder.decode(in, null, true);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }

        // normal case, one complete operation
        decoder.reset();
        in.rewind();
        out.rewind();
        assertSame(CoderResult.UNDERFLOW, decoder.decode(in, out, true));
        assertEquals(out.limit(), 100);
        assertEquals(out.position(), getString().length());
        assertEquals(out.remaining(), 100 - getString().length());
        assertEquals(out.capacity(), 100);
        assertCharBufferValue(getString(), out);
        decoder.flush(out);

        // normal case, one complete operation, but call twice, first time set
        // endOfInput to false
        decoder.reset();
        in.rewind();
        out.clear();
        assertSame(CoderResult.UNDERFLOW, decoder.decode(in, out, false));
        assertEquals(out.limit(), 100);
        assertEquals(out.position(), getString().length());
        assertEquals(out.remaining(), 100 - getString().length());
        assertEquals(out.capacity(), 100);
        assertCharBufferValue(getString(), out);

        decoder.reset();
        in.rewind();
        out.clear();
        assertSame(CoderResult.UNDERFLOW, decoder.decode(in, out, false));
        in = getHeadlessByteBuffer();
        assertSame(CoderResult.UNDERFLOW, decoder.decode(in, out, false));
        in.rewind();
        assertSame(CoderResult.UNDERFLOW, decoder.decode(in, out, true));
        assertEquals(out.limit(), 100);
        assertTrue(out.position() > 0);
        assertEquals(out.remaining(), out.capacity() - out.position());
        assertEquals(out.capacity(), 100);
        assertCharBufferValue(getString() + getString() + getString(), out);

        // overflow
        out = CharBuffer.allocate(4);
        decoder.reset();
        in = getByteBuffer();
        out.rewind();
        assertSame(CoderResult.OVERFLOW, decoder.decode(in, out, false));

        assertCharBufferValue(getString().substring(0, 4), out);
        out = CharBuffer.allocate(100);
        assertSame(CoderResult.UNDERFLOW, decoder.decode(in, out, false));
        assertCharBufferValue(getString().substring(4), out);
        in.rewind();
        out = CharBuffer.allocate(100);
        assertSame(CoderResult.UNDERFLOW, decoder.decode(in, out, true));
        assertCharBufferValue(bom + getString(), out);
    }

    public void testDecodeCharBufferByteBufferUnmappedException()
            throws CharacterCodingException, UnsupportedEncodingException {
        implTestDecodeCharBufferByteBufferUnmappedException(
                getUnmappedByteBuffer(), true);
    }

    public void testDecodeCharBufferByteIncompleteBufferUnmappedException()
            throws CharacterCodingException, UnsupportedEncodingException {
        implTestDecodeCharBufferByteBufferUnmappedException(
                getUnmappedByteBuffer(), false);
    }

    public void testDecodeCharBufferByteReadOnlyBufferUnmappedException()
            throws CharacterCodingException, UnsupportedEncodingException {
        implTestDecodeCharBufferByteBufferUnmappedException(
                readOnly(getUnmappedByteBuffer()), true);
    }

    public void testDecodeCharBufferByteReadOnlyIncompleteBufferUnmappedException()
            throws CharacterCodingException, UnsupportedEncodingException {
        implTestDecodeCharBufferByteBufferUnmappedException(
                readOnly(getUnmappedByteBuffer()), false);
    }

    void implTestDecodeCharBufferByteBufferUnmappedException(ByteBuffer in,
            boolean endOfInput) throws CharacterCodingException,
            UnsupportedEncodingException {
        if (null == in) {
            return;
        }
        CharBuffer out = CharBuffer.allocate(50);

        decoder.onMalformedInput(CodingErrorAction.REPORT);
        decoder.reset();
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        CoderResult result = decoder.decode(in, out, endOfInput);
        assertTrue(result.isUnmappable());

        decoder.reset();
        out.clear();
        in.rewind();
        decoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
        assertSame(CoderResult.UNDERFLOW, decoder.decode(in, out, endOfInput));
        assertCharBufferValue(getString(), out);

        decoder.reset();
        out.clear();
        in.rewind();
        decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
        assertSame(CoderResult.UNDERFLOW, decoder.decode(in, out, endOfInput));
        assertCharBufferValue(decoder.replacement() + getString(), out);
    }

    public void testDecodeCharBufferByteBufferMalformedException()
            throws CharacterCodingException, UnsupportedEncodingException {
        implTestDecodeCharBufferByteBufferMalformedException(
                getMalformedByteBuffer(), true);
    }

    public void testDecodeCharBufferByteIncompleteBufferMalformedException()
            throws CharacterCodingException, UnsupportedEncodingException {

        implTestDecodeCharBufferByteBufferMalformedException(
                getMalformedByteBuffer(), false);
    }

    public void testDecodeCharBufferByteReadOnlyBufferMalformedException()
            throws CharacterCodingException, UnsupportedEncodingException {
        implTestDecodeCharBufferByteBufferMalformedException(
                readOnly(getMalformedByteBuffer()), true);
    }

    public void testDecodeCharBufferByteReadOnlyIncompleteBufferMalformedException()
            throws CharacterCodingException, UnsupportedEncodingException {
        implTestDecodeCharBufferByteBufferMalformedException(
                readOnly(getMalformedByteBuffer()), false);
    }

    void implTestDecodeCharBufferByteBufferMalformedException(ByteBuffer in,
            boolean endOfInput) throws CharacterCodingException,
            UnsupportedEncodingException {
        if (null == in) {
            return;
        }
        CharBuffer out = CharBuffer.allocate(getString().length() * 3);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        decoder.reset();
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        CoderResult result = decoder.decode(in, out, endOfInput);
        assertTrue(result.isMalformed());

        decoder.reset();
        out.clear();
        in.rewind();
        decoder.onMalformedInput(CodingErrorAction.IGNORE);
        assertSame(CoderResult.UNDERFLOW, decoder.decode(in, out, endOfInput));
        assertCharBufferValue(getString(), out);

        decoder.reset();
        out.clear();
        in.rewind();
        decoder.onMalformedInput(CodingErrorAction.REPLACE);
        assertSame(CoderResult.UNDERFLOW, decoder.decode(in, out, endOfInput));
        assertCharBufferValue(decoder.replacement() + getString(), out);
    }

    public void testDecodeCharBufferByteBufferException()
            throws CharacterCodingException, UnsupportedEncodingException {
        implTestDecodeCharBufferByteBufferException(getExceptionByteArray(),
                true);
    }

    public void testDecodeCharBufferByteIncompleteBufferException()
            throws CharacterCodingException, UnsupportedEncodingException {
        implTestDecodeCharBufferByteBufferException(getExceptionByteArray(),
                false);
    }

    public void testDecodeCharBufferByteReadOnlyBufferException()
            throws CharacterCodingException, UnsupportedEncodingException {
        implTestDecodeCharBufferByteBufferException(
                readOnly(getExceptionByteArray()), true);
    }

    public void testDecodeCharBufferByteReadOnlyIncompleteBufferException()
            throws CharacterCodingException, UnsupportedEncodingException {
        implTestDecodeCharBufferByteBufferException(
                readOnly(getExceptionByteArray()), false);
    }

    void implTestDecodeCharBufferByteBufferException(ByteBuffer in,
            boolean endOfInput) throws CharacterCodingException,
            UnsupportedEncodingException {
        CharBuffer out = CharBuffer.allocate(50);
        decoder.reset();
        try {
            decoder.decode(in, out, endOfInput);
            fail("should throw runtime exception");
        } catch (RuntimeException e) {
        }
    }

    private ByteBuffer readOnly(ByteBuffer b) {
        if (null == b) {
            return null;
        }
        return b.asReadOnlyBuffer();
    }

    protected String getString() {
        return " buffer";
    }

    protected ByteBuffer getByteBuffer() {
        return ByteBuffer.wrap(new byte[] { 32, 98, 117, 102, 102, 101, 114 });
    }

    protected ByteBuffer getHeadlessByteBuffer() {
        return getByteBuffer();
    }

    ByteBuffer getExceptionByteArray() throws UnsupportedEncodingException {
        // "runtime"
        return ByteBuffer
                .wrap(new byte[] { 114, 117, 110, 116, 105, 109, 101 });
    }

    ByteBuffer getUnmappedByteBuffer() throws UnsupportedEncodingException {
        // "unmap buffer"
        byte[] ba = new byte[] { 117, 110, 109, 97, 112, 32, 98, 117, 102, 102,
                101, 114 };
        return ByteBuffer.wrap(ba);
    }

    ByteBuffer getMalformedByteBuffer() throws UnsupportedEncodingException {
        // "malform buffer"
        byte[] ba = new byte[] { 109, 97, 108, 102, 111, 114, 109, 32, 98, 117,
                102, 102, 101, 114 };
        return ByteBuffer.wrap(ba);
    }

    private void assertCharBufferValue(String expected, CharBuffer out) {
        if (out.position() != 0) {
            out.flip();
        }
        assertEquals(expected, new String(out.array(), out.arrayOffset(), out
                .arrayOffset() + out.limit()));
    }

    /*
     * test flush
     */
    public void testFlush() throws CharacterCodingException {
        CharBuffer out = CharBuffer.allocate(10);
        ByteBuffer in = ByteBuffer.wrap(new byte[] { 12, 12 });
        decoder.decode(in, out, true);
        assertSame(CoderResult.UNDERFLOW, decoder.flush(out));

        decoder.reset();
        decoder.decode((ByteBuffer) in.rewind(), (CharBuffer) out.rewind(),
                true);
        assertSame(CoderResult.UNDERFLOW, decoder
                .flush(CharBuffer.allocate(10)));
    }

    /*
     * ---------------------------------- methods to test illegal state
     * -----------------------------------
     */
    // Normal case: just after reset, and it also means reset can be done
    // anywhere
    public void testResetIllegalState() throws CharacterCodingException {
        decoder.reset();
        decoder.decode(getByteBuffer());
        decoder.reset();
        decoder.decode(getByteBuffer(), CharBuffer.allocate(3), false);
        decoder.reset();
        decoder.decode(getByteBuffer(), CharBuffer.allocate(3), true);
        decoder.reset();
    }

    public void testFlushIllegalState() throws CharacterCodingException {
        ByteBuffer in = ByteBuffer.wrap(new byte[] { 98, 98 });
        CharBuffer out = CharBuffer.allocate(5);

        // Illegal state: after reset.
        decoder.reset();
        try {
            decoder.flush(out);
            fail();
        } catch (IllegalStateException expected) {
        }

        // Normal case: after decode with endOfInput is true
        decoder.reset();
        decoder.decode(in, out, true);
        out.rewind();
        CoderResult result = decoder.flush(out);
        assertSame(result, CoderResult.UNDERFLOW);

        // Good state: flush twice
        decoder.flush(out);

        // Illegal state: flush after decode with endOfInput is false
        decoder.reset();
        decoder.decode(in, out, false);
        try {
            decoder.flush(out);
            fail();
        } catch (IllegalStateException expected) {
        }
    }

    // test illegal states for decode facade
    public void testDecodeFacadeIllegalState() throws CharacterCodingException {
        // decode facade can be execute in anywhere
        ByteBuffer in = getByteBuffer();

        // Normal case: just created
        decoder.decode(in);
        in.rewind();

        // Normal case: just after decode facade
        decoder.decode(in);
        in.rewind();

        // Normal case: just after decode with that endOfInput is true
        decoder.reset();
        decoder.decode(getByteBuffer(), CharBuffer.allocate(30), true);
        decoder.decode(in);
        in.rewind();

        // Normal case:just after decode with that endOfInput is false
        decoder.reset();
        decoder.decode(getByteBuffer(), CharBuffer.allocate(30), false);
        decoder.decode(in);
        in.rewind();

        // Normal case: just after flush
        decoder.reset();
        decoder.decode(getByteBuffer(), CharBuffer.allocate(30), true);
        decoder.flush(CharBuffer.allocate(10));
        decoder.decode(in);
        in.rewind();
    }

    // test illegal states for two decode method with endOfInput is true
    public void testDecodeTrueIllegalState() throws CharacterCodingException {
        ByteBuffer in = ByteBuffer.wrap(new byte[] { 98, 98 });
        CharBuffer out = CharBuffer.allocate(100);
        // Normal case: just created
        decoder.decode(in, out, true);
        in.rewind();
        out.rewind();

        // Normal case: just after decode with that endOfInput is true
        decoder.reset();
        decoder.decode(in, CharBuffer.allocate(30), true);
        in.rewind();
        decoder.decode(in, out, true);
        in.rewind();
        out.rewind();

        // Normal case:just after decode with that endOfInput is false
        decoder.reset();
        decoder.decode(in, CharBuffer.allocate(30), false);
        in.rewind();
        decoder.decode(in, out, true);
        in.rewind();
        out.rewind();

        // Illegal state: just after flush
        decoder.reset();
        decoder.decode(in, CharBuffer.allocate(30), true);
        decoder.flush(CharBuffer.allocate(10));
        in.rewind();
        try {
            decoder.decode(in, out, true);
            fail("should illegal state");
        } catch (IllegalStateException e) {
        }
        in.rewind();
        out.rewind();

    }

    // test illegal states for two decode method with endOfInput is false
    public void testDecodeFalseIllegalState() throws CharacterCodingException {
        ByteBuffer in = ByteBuffer.wrap(new byte[] { 98, 98 });
        CharBuffer out = CharBuffer.allocate(5);
        // Normal case: just created
        decoder.decode(in, out, false);
        in.rewind();
        out.rewind();

        // Illegal state: just after decode facade
        decoder.reset();
        decoder.decode(in);
        in.rewind();
        try {
            decoder.decode(in, out, false);
            fail("should illegal state");
        } catch (IllegalStateException e) {
        }
        in.rewind();
        out.rewind();

        // Illegal state: just after decode with that endOfInput is true
        decoder.reset();
        decoder.decode(in, CharBuffer.allocate(30), true);
        in.rewind();
        try {
            decoder.decode(in, out, false);
            fail("should illegal state");
        } catch (IllegalStateException e) {
        }
        in.rewind();
        out.rewind();

        // Normal case:just after decode with that endOfInput is false
        decoder.reset();
        decoder.decode(in, CharBuffer.allocate(30), false);
        in.rewind();
        decoder.decode(in, out, false);
        in.rewind();
        out.rewind();

        // Illegal state: just after flush
        decoder.reset();
        decoder.decode(in, CharBuffer.allocate(30), true);
        in.rewind();
        decoder.flush(CharBuffer.allocate(10));
        try {
            decoder.decode(in, out, false);
            fail("should illegal state");
        } catch (IllegalStateException e) {
        }
    }

    /*
     * --------------------------------- illegal state test end
     * ---------------------------------
     */

    public void testImplFlush() {
        decoder = new MockCharsetDecoder(cs, 1, 3);
        assertEquals(CoderResult.UNDERFLOW, ((MockCharsetDecoder) decoder)
                .pubImplFlush(null));
    }

    public void testImplOnMalformedInput() {
        decoder = new MockCharsetDecoder(cs, 1, 3);
        assertEquals(CoderResult.UNDERFLOW, ((MockCharsetDecoder) decoder)
                .pubImplFlush(null));

    }

    public void testImplOnUnmappableCharacter() {
        decoder = new MockCharsetDecoder(cs, 1, 3);
        ((MockCharsetDecoder) decoder).pubImplOnUnmappableCharacter(null);
    }

    public void testImplReplaceWith() {
        decoder = new MockCharsetDecoder(cs, 1, 3);
        ((MockCharsetDecoder) decoder).pubImplReplaceWith(null);
    }

    public void testImplReset() {
        decoder = new MockCharsetDecoder(cs, 1, 3);
        ((MockCharsetDecoder) decoder).pubImplReset();
    }

    /*
     * mock decoder
     */
    public static class MockCharsetDecoder extends CharsetDecoder {
        public MockCharsetDecoder(Charset cs, float ave, float max) {
            super(cs, ave, max);
        }

        protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
            int inPosition = in.position();
            byte[] input = new byte[in.remaining()];
            in.get(input);
            String result;
            try {
                result = new String(input, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError(e);
            }
            if (result.startsWith("malform")) {
                // reset the cursor to the error position
                in.position(inPosition);
                // set the error length
                return CoderResult.malformedForLength("malform".length());
            } else if (result.startsWith("unmap")) {
                // reset the cursor to the error position
                in.position(inPosition);
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
                out.put((char) input[i]);
            }
            return r;
        }

        protected CoderResult implFlush(CharBuffer out) {
            CoderResult result = super.implFlush(out);
            if (out.remaining() >= 5) {
                // TODO
                // out.put("flush");
                result = CoderResult.UNDERFLOW;
            } else {
                // out.put("flush", 0, out.remaining());
                result = CoderResult.OVERFLOW;
            }
            return result;
        }

        public CoderResult pubImplFlush(CharBuffer out) {
            return super.implFlush(out);
        }

        public void pubImplOnMalformedInput(CodingErrorAction newAction) {
            super.implOnMalformedInput(newAction);
        }

        public void pubImplOnUnmappableCharacter(CodingErrorAction newAction) {
            super.implOnUnmappableCharacter(newAction);
        }

        public void pubImplReplaceWith(String newReplacement) {
            super.implReplaceWith(newReplacement);
        }

        public void pubImplReset() {
            super.implReset();
        }

    }

}
