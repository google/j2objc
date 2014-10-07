/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package libcore.java.io;

import java.io.IOException;
import java.io.OutputStream;

public class OldOutputStreamTest extends junit.framework.TestCase {

    class BasicOutputStream extends OutputStream {

        private static final int BUFFER_SIZE = 20;
        private byte[] buffer;
        private int position;

        public BasicOutputStream() {
            buffer = new byte[BUFFER_SIZE];
            position = 0;
        }

        public void write(int oneByte) throws IOException {
            if (position < BUFFER_SIZE) {
                buffer[position] = (byte) (oneByte & 255);
                position++;
            } else {
                throw new IOException("Internal buffer overflow.");
            }
        }

        public byte[] getBuffer() {
            return buffer;
        }
    }

    private final byte[] shortByteArray = "Lorem ipsum...".getBytes();
    private final byte[] longByteArray = "Lorem ipsum dolor sit amet...".getBytes();

    public void test_write$B() {
        BasicOutputStream bos = new BasicOutputStream();
        boolean expected;
        byte[] buffer;

        try {
            bos.write(shortByteArray);
        } catch (IOException e) {
            fail("Test 1: Unexpected IOException encountered.");
        }

        expected = true;
        buffer = bos.getBuffer();
        for (int i = 0; i < (shortByteArray.length) && expected; i++) {
            expected = (shortByteArray[i] == buffer[i]);
        }
        assertTrue("Test 1: Test byte array has not been written correctly.",
                   expected);

        try {
            bos.write(longByteArray);
            fail("Test 2: IOException expected.");
        } catch (IOException e) {}
    }

    public void test_write$BII() {
        BasicOutputStream bos = new BasicOutputStream();
        boolean expected;
        byte[] buffer;

        try {
            bos.write(shortByteArray, 6, 5);
        } catch (IOException e) {
            fail("Test 1: Unexpected IOException encountered.");
        }

        expected = true;
        buffer = bos.getBuffer();
        for (int i = 6, j = 0; j < 5 && expected; i++, j++) {
            expected = (shortByteArray[i] == buffer[j]);
        }
        assertTrue("Test 1: Test byte array has not been written correctly.",
                   expected);

        try {
            bos.write(longByteArray, 5, 20);
            fail("Test 2: IOException expected.");
        } catch (IOException e) {}

        try {
            bos.write(longByteArray, -1, 10);
            fail("Test 3: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        } catch (IOException e) {
            fail("Test 3: Unexpected IOException encountered.");
        }

        try {
            bos.write(longByteArray, 10, -1);
            fail("Test 4: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        } catch (IOException e) {
            fail("Test 4: Unexpected IOException encountered.");
        }

        try {
            bos.write(longByteArray, 20, 10);
            fail("Test 5: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        } catch (IOException e) {
            fail("Test 5: Unexpected IOException encountered.");
        }
    }
}
