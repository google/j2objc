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
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

/**
 *
 */
public class UTF16CharsetDecoderTest extends CharsetDecoderTest {

    boolean bigEndian = true;

    protected void setUp() throws Exception {
        cs = Charset.forName("utf-16");
        bom = "\ufeff";
        super.setUp();
    }

    /*
     * @see CharsetDecoderTest#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    protected ByteBuffer getByteBuffer() {
        // FIXME: different here
        // if don't specified BOM
        // ICU default is LE
        // JDK default is BE

        // maybe start with 0xFEFF, which means big endian
        // 0xFFFE, which means little endian
        byte[] b = (bigEndian) ? new byte[] { -1, -2, 32, 0, 98, 0, 117, 0,
                102, 0, 102, 0, 101, 0, 114, 0 } : new byte[] { -2, -1, 0, 32,
                0, 98, 0, 117, 0, 102, 0, 102, 0, 101, 0, 114 };
        return ByteBuffer.wrap(b);
    }

    protected ByteBuffer getHeadlessByteBuffer() {
        ByteBuffer b = getByteBuffer();
        b.position(2);
        byte[] bytes = new byte[b.remaining()];
        b.get(bytes);
        return ByteBuffer.wrap(bytes);
    }

    public void testLittleEndianByteBufferCharBuffer()
            throws CharacterCodingException, UnsupportedEncodingException {
        bigEndian = false;
        implTestDecodeByteBufferCharBuffer(getByteBuffer());
        bigEndian = true;
    }

    public void testLittleEndianReadOnlyByteBufferCharBuffer()
            throws CharacterCodingException, UnsupportedEncodingException {
        bigEndian = false;
        implTestDecodeByteBufferCharBuffer(getByteBuffer().asReadOnlyBuffer());
        bigEndian = true;
    }

    public void testLittleEndian() throws CharacterCodingException,
            UnsupportedEncodingException {
        bigEndian = false;
        implTestDecodeByteBuffer();
        bigEndian = true;
    }

    // FIXME: give up this tests
    // public void testDefaultCharsPerByte() {
    // // assertEquals(1, decoder.averageCharsPerByte());
    // // assertEquals(1, decoder.maxCharsPerByte());
    // assertEquals(decoder.averageCharsPerByte(), 0.5, 0.001);
    // assertEquals(decoder.maxCharsPerByte(), 2, 0.001);
    // }

    ByteBuffer getUnmappedByteBuffer() throws UnsupportedEncodingException {
        return null;
    }

    ByteBuffer getMalformedByteBuffer() throws UnsupportedEncodingException {
        return null;
        // FIXME: different here, RI can parse 0xd8d8
        // ByteBuffer buffer = ByteBuffer.allocate(100);
        // buffer.put((byte) -1);
        // buffer.put((byte) -2);
        // buffer.put((byte) 0xdc);
        // buffer.put((byte) 0xdc);
        // buffer.put(unibytes);
        // buffer.flip();
        // return buffer;
    }

    ByteBuffer getExceptionByteArray() throws UnsupportedEncodingException {
        return null;
    }
}
