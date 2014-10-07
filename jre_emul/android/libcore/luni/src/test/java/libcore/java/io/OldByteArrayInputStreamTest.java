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

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class OldByteArrayInputStreamTest extends junit.framework.TestCase {

    private ByteArrayInputStream is;

    public String fileString = "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\n";

    public void test_Constructor$B() {
        // Test for method java.io.ByteArrayInputStream(byte [])

        java.io.InputStream bis = new java.io.ByteArrayInputStream(fileString
                .getBytes());

        try {
            assertTrue("Unable to create ByteArrayInputStream",
                    bis.available() == fileString.length());
        } catch (Exception e) {
            System.out.println("Exception during Constructor test");
        }
    }

    public void test_Constructor$BII() throws IOException {
        // Test for method java.io.ByteArrayInputStream(byte [], int, int)

        byte[] zz = fileString.getBytes();
        java.io.InputStream bis = new java.io.ByteArrayInputStream(zz, 0, 100);

        try {
            assertEquals("Unable to create ByteArrayInputStream",
                    100, bis.available());
        } catch (Exception e) {
            fail("Exception during Constructor test");
        }

        // Regression test for Harmony-2405
        new SubByteArrayInputStream(new byte[] { 1, 2 }, 444, 13);
        assertEquals(444, SubByteArrayInputStream.pos);
        assertEquals(444, SubByteArrayInputStream.mark);
        assertEquals(2, SubByteArrayInputStream.count);
    }

    static class SubByteArrayInputStream extends ByteArrayInputStream {
        public static byte[] buf;

        public static int mark, pos, count;

        SubByteArrayInputStream(byte[] buf, int offset, int length)
                throws IOException {
            super(buf, offset, length);
            buf = super.buf;
            mark = super.mark;
            pos = super.pos;
            count = super.count;
        }
    }

    public void test_available() {
        // Test for method int java.io.ByteArrayInputStream.available()
        try {
            assertTrue("Returned incorrect number of available bytes", is
                    .available() == fileString.length());
        } catch (Exception e) {
            fail("Exception during available test");
        }
    }

    public void test_close() {
        is.read();
        try {
            is.close();
        } catch (java.io.IOException e) {
            fail("Test 1: Failed to close the input stream.");
        }
        try {
            is.read();
        } catch (Exception e) {
            fail("Test 2: Should be able to read from closed stream.");
        }
    }

    public void test_markI() {
        // Test for method void java.io.ByteArrayInputStream.mark(int)
        byte[] buf1 = new byte[100];
        byte[] buf2 = new byte[100];
        try {
            is.skip(3000);
            is.mark(1000);
            is.read(buf1, 0, buf1.length);
            is.reset();
            is.read(buf2, 0, buf2.length);
            is.reset();
            assertTrue("Failed to mark correct position", new String(buf1, 0,
                    buf1.length).equals(new String(buf2, 0, buf2.length)));

        } catch (Exception e) {
            fail("Exception during mark test");
        }

    }

    public void test_markSupported() {
        // Test for method boolean java.io.ByteArrayInputStream.markSupported()
        assertTrue("markSupported returned incorrect value", is.markSupported());
    }

    public void test_read() {
        // Test for method int java.io.ByteArrayInputStream.read()
        try {

            int c = is.read();
            is.reset();
            assertTrue("read returned incorrect char", c == fileString
                    .charAt(0));
        } catch (Exception e) {
            fail("Exception during read test");
        }
    }

    public void test_read$BII() throws IOException {
        byte[] buf1 = new byte[20];
        is.skip(50);
        is.mark(100);
        is.read(buf1, 0, buf1.length);
        assertTrue("Test 1: Failed to read correct data.",
                new String(buf1, 0, buf1.length).equals(
                        fileString.substring(50, 70)));

        // Illegal argument checks.
        try {
            is.read(null, 1, 0);
            fail("Test 2: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected.
        }

        try {
            is.read(buf1 , -1, 1);
            fail("Test 3: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            is.read(buf1 , 1, -1);
            fail("Test 4: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            is.read(buf1, 1, buf1.length);
            fail("Test 5: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    public void test_reset() {
        // Test for method void java.io.ByteArrayInputStream.reset()
        byte[] buf1 = new byte[10];
        byte[] buf2 = new byte[10];
        try {
            is.mark(200);
            is.read(buf1, 0, 10);
            is.reset();
            is.read(buf2, 0, 10);
            is.reset();
            assertTrue("Reset failed", new String(buf1, 0, buf1.length)
                    .equals(new String(buf2, 0, buf2.length)));
        } catch (Exception e) {
            fail("Exception during reset test : " + e.getMessage());
        }
    }

    public void test_skipJ() {
        // Test for method long java.io.ByteArrayInputStream.skip(long)
        byte[] buf1 = new byte[10];
        try {
            is.skip(100);
            is.read(buf1, 0, buf1.length);
            assertTrue("Failed to skip to correct position", new String(buf1,
                    0, buf1.length).equals(fileString.substring(100, 110)));
        } catch (Exception e) {
            fail("Exception during skip test : " + e.getMessage());
        }
    }

    protected void setUp() {
        is = new ByteArrayInputStream(fileString.getBytes());

    }

    protected void tearDown() {
        try {
            is.close();
        } catch (Exception e) {
        }
    }
}
