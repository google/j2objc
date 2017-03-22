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
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

public class CharsetDecoderTest extends junit.framework.TestCase {
    // None of the harmony or jtreg tests actually check that replaceWith does the right thing!
    public void test_replaceWith() throws Exception {
        CharsetDecoder d = Charset.forName("UTF-16").newDecoder();
        d.replaceWith("x");
        d.onMalformedInput(CodingErrorAction.REPLACE);
        d.onUnmappableCharacter(CodingErrorAction.REPLACE);
        ByteBuffer in = ByteBuffer.wrap(new byte[] { 109, 97, 109 });
        assertEquals("\u6d61x", d.decode(in).toString());
    }

    // http://code.google.com/p/android/issues/detail?id=4237
    public void test_ByteArray_decode_no_offset() throws Exception {
        CharsetDecoder decoder = Charset.forName("UTF-16").newDecoder();
        byte[] arr = encode("UTF-16", "Android");
        ByteBuffer inBuffer = ByteBuffer.wrap(arr, 0, arr.length).slice();
        CharBuffer outBuffer = CharBuffer.allocate(arr.length);
        decoder.reset();
        CoderResult coderResult = decoder.decode(inBuffer, outBuffer, true);
        assertFalse(coderResult.toString(), coderResult.isError());
        decoder.flush(outBuffer);
        outBuffer.flip();
        assertEquals("Android", outBuffer.toString().trim());
    }

    // http://code.google.com/p/android/issues/detail?id=4237
    public void test_ByteArray_decode_with_offset() throws Exception {
        CharsetDecoder decoder = Charset.forName("UTF-16").newDecoder();
        byte[] arr = encode("UTF-16", "Android");
        arr = prependByteToByteArray(arr, new Integer(1).byteValue());
        int offset = 1;
        ByteBuffer inBuffer = ByteBuffer.wrap(arr, offset, arr.length - offset).slice();
        CharBuffer outBuffer = CharBuffer.allocate(arr.length - offset);
        decoder.reset();
        CoderResult coderResult = decoder.decode(inBuffer, outBuffer, true);
        assertFalse(coderResult.toString(), coderResult.isError());
        decoder.flush(outBuffer);
        outBuffer.flip();
        assertEquals("Android", outBuffer.toString().trim());
    }

    // http://code.google.com/p/android/issues/detail?id=4237
    public void test_ByteArray_decode_with_offset_using_facade_method() throws Exception {
        CharsetDecoder decoder = Charset.forName("UTF-16").newDecoder();
        byte[] arr = encode("UTF-16", "Android");
        arr = prependByteToByteArray(arr, new Integer(1).byteValue());
        int offset = 1;
        CharBuffer outBuffer = decoder.decode(ByteBuffer.wrap(arr, offset, arr.length - offset));
        assertEquals("Android", outBuffer.toString().trim());
    }

    private static byte[] prependByteToByteArray(byte[] arr, byte b) {
        byte[] result = new byte[arr.length + 1];
        result[0] = b;
        System.arraycopy(arr, 0, result, 1, arr.length);
        return result;
    }

    private static byte[] encode(String charsetName, String s) throws Exception {
        CharsetEncoder encoder = Charset.forName(charsetName).newEncoder();
        ByteBuffer buf = encoder.encode(CharBuffer.wrap(s));
        byte[] out = new byte[buf.remaining()];
        buf.get(out);
        assertEquals(0, buf.remaining());
        return out;
    }

    /* J2ObjC: This test also fails with the RI. Android must expect its UTF-8 decoder to save
     * partial bytes that have been read from an incomplete sequence.
    public void testUtf8BytesSplitAcrossMultipleWrites() throws Exception {
        CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
        CharBuffer cb = CharBuffer.allocate(128);
        CoderResult cr;
        cr = decoder.decode(ByteBuffer.wrap(new byte[] { (byte) 0xe2 }), cb, false);
        assertEquals(CoderResult.UNDERFLOW, cr);
        cr = decoder.decode(ByteBuffer.wrap(new byte[] { (byte) 0x98 }), cb, false);
        assertEquals(CoderResult.UNDERFLOW, cr);
        cr = decoder.decode(ByteBuffer.wrap(new byte[] { (byte) 0x83 }), cb, false);
        assertEquals(CoderResult.UNDERFLOW, cr);
        cr = decoder.decode(ByteBuffer.wrap(new byte[0]), cb, true);
        assertEquals(CoderResult.UNDERFLOW, cr);
        cr = decoder.flush(cb);
        assertEquals(CoderResult.UNDERFLOW, cr);
        assertEquals(1, cb.position());
        assertEquals('\u2603', cb.get(0));
    }*/

    public void testBufferWithNonZeroOffset() {
        CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
        CharBuffer cb = CharBuffer.allocate(128);
        cb.position(42);
        CharBuffer out = cb.slice();
        CoderResult cr = decoder.decode(
                ByteBuffer.wrap(new byte[] { 'h', 'e', 'l', 'l', 'o'}), out, false);
        assertTrue(cr.isUnderflow());
        assertEquals(5, out.position());
    }
}
