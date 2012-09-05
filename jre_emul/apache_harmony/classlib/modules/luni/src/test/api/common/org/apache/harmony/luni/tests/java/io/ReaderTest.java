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

package org.apache.harmony.luni.tests.java.io;

import java.io.IOException;
import java.io.Reader;

import junit.framework.TestCase;

public class ReaderTest extends TestCase {
    
    /**
     * @tests {@link java.io.Reader#mark(int)}
     */
    public void test_mark() {
        MockReader mockReader = new MockReader();
        try {
            mockReader.mark(0);
            fail("Should throw IOException for Reader do not support mark");
        } catch (IOException e) {
            // Excepted
        }
    }

    /**
     * @tests {@link java.io.Reader#read()}
     */
    public void test_read() throws IOException {
        MockReader reader = new MockReader();

        // return -1 when the stream is null;
        assertEquals("Should be equal to -1", -1, reader.read());

        String string = "MY TEST STRING";
        char[] srcBuffer = string.toCharArray();
        MockReader mockReader = new MockReader(srcBuffer);

        // normal read
        for (char c : srcBuffer) {
            assertEquals("Should be equal to \'" + c + "\'", c, mockReader
                    .read());
        }

        // return -1 when read Out of Index
        mockReader.read();
        assertEquals("Should be equal to -1", -1, reader.read());

    }

    /**
     * @tests {@link java.io.Reader#ready()}
     */
    public void test_ready() throws IOException {
        MockReader mockReader = new MockReader();
        assertFalse("Should always return false", mockReader.ready());

    }

    /**
     * @tests {@link java.io.Reader#reset()}
     */
    public void test_reset() {
        MockReader mockReader = new MockReader();
        try {
            mockReader.reset();
            fail("Should throw IOException");
        } catch (IOException e) {
            // Excepted
        }
    }

    /**
     * @tests {@link java.io.Reader#skip(long)}
     */
    public void test_skip() throws IOException {
        String string = "MY TEST STRING";
        char[] srcBuffer = string.toCharArray();
        int length = srcBuffer.length;
        MockReader mockReader = new MockReader(srcBuffer);
        assertEquals("Should be equal to \'M\'", 'M', mockReader.read());

        // normal skip
        mockReader.skip(length / 2);
        assertEquals("Should be equal to \'S\'", 'S', mockReader.read());

        // try to skip a bigger number of characters than the total
        // Should do nothing
        mockReader.skip(length);

        // try to skip a negative number of characters throw IllegalArgumentException
        try {
            mockReader.skip(-1);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Excepted
        }
    }

    class MockReader extends Reader {

        private char[] contents;

        private int current_offset = 0;

        private int length = 0;
        
        public MockReader() {
            super();
        }

        public MockReader(char[] data) {
            contents = data;
            length = contents.length;
        }

        @Override
        public void close() throws IOException {

            contents = null;
        }

        @Override
        public int read(char[] buf, int offset, int count) throws IOException {

            if (null == contents) {
                return -1;
            }
            if (length <= current_offset) {
                return -1;
            }
            if (buf.length < offset + count) {
                throw new IndexOutOfBoundsException();
            }

            count = Math.min(count, length - current_offset);
            for (int i = 0; i < count; i++) {
                buf[offset + i] = contents[current_offset + i];
            }
            current_offset += count;
            return count;
        }

    }
}
