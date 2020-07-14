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
import java.nio.charset.Charset;

/**
 * TODO typedef
 */
public class UTF16LECharsetDecoderTest extends CharsetDecoderTest {

    protected void setUp() throws Exception {
        cs = Charset.forName("utf-16le");
        super.setUp();
    }

    /*
     * @see CharsetDecoderTest#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // // FIXME: give up this tests
    // public void testDefaultCharsPerByte(){
    // // assertEquals(1, decoder.averageCharsPerByte());
    // // assertEquals(1, decoder.maxCharsPerByte());
    // assertEquals(decoder.averageCharsPerByte(), 0.5, 0.001);
    // assertEquals(decoder.maxCharsPerByte(), 2, 0.001);
    // }

    ByteBuffer getUnmappedByteBuffer() throws UnsupportedEncodingException {
        // no unmap byte buffer
        return null;
    }

    ByteBuffer getMalformedByteBuffer() throws UnsupportedEncodingException {
        // FIXME: different here, JDK can parse 0xd8d8
        // ByteBuffer buffer = ByteBuffer.allocate(100);
        // buffer.put((byte)0xd8);
        // buffer.put((byte)0xd8);
        // buffer.put(unibytes);
        // buffer.flip();
        // return buffer;
        return null;
    }

    ByteBuffer getExceptionByteArray() throws UnsupportedEncodingException {
        return null;
    }

    protected ByteBuffer getByteBuffer() {
        return ByteBuffer.wrap(new byte[] { 32, 0, 98, 0, 117, 0, 102, 0, 102,
                0, 101, 0, 114, 0 });
    }
}
