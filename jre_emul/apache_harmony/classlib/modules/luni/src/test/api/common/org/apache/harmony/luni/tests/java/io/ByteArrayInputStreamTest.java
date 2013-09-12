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

package org.apache.harmony.luni.tests.java.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ByteArrayInputStreamTest extends junit.framework.TestCase {

    private InputStream is;

    public String fileString = "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\n";

    /**
     * @tests ByteArrayInputStream#ByteArrayInputStream(byte[])
     */
    public void test_Constructor$B() throws IOException {
        InputStream bis = new ByteArrayInputStream(fileString.getBytes());

        assertTrue("Unable to create ByteArrayInputStream",
                bis.available() == fileString.length());
    }

    /**
     * @tests ByteArrayInputStream#ByteArrayInputStream(byte[], int, int)
     */
    public void test_Constructor$BII() throws IOException {
        byte[] zz = fileString.getBytes();
        InputStream bis = new ByteArrayInputStream(zz, 0, 100);

        assertEquals("Unable to create ByteArrayInputStream", 100, bis
                .available());

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

    /**
     * @tests ByteArrayInputStream#available()
     */
    public void test_available() throws IOException {
        assertTrue("Returned incorrect number of available bytes", is
                .available() == fileString.length());
    }

    /**
     * @tests ByteArrayInputStream#close()
     */
    public void test_close() throws IOException {
        is.read();
        is.close();
        is.read(); // Should be able to read from a closed stream
    }

    /**
     * @tests ByteArrayInputStream#mark(int)
     */
    public void test_markI() throws IOException {
        byte[] buf1 = new byte[100];
        byte[] buf2 = new byte[100];
        is.skip(3000);
        is.mark(1000);
        is.read(buf1, 0, buf1.length);
        is.reset();
        is.read(buf2, 0, buf2.length);
        is.reset();
        assertTrue("Failed to mark correct position", new String(buf1, 0,
                buf1.length).equals(new String(buf2, 0, buf2.length)));
    }

    /**
     * @tests ByteArrayInputStream#markSupported()
     */
    public void test_markSupported() {
        assertTrue("markSupported returned incorrect value", is.markSupported());
    }

    /**
     * @tests ByteArrayInputStream#read()
     */
    public void test_read() throws IOException {
        InputStreamReader isr = new InputStreamReader(is);
        int c = isr.read();
        is.reset();
        assertTrue("read returned incorrect char", c == fileString.charAt(0));
    }

    /**
     * @tests ByteArrayInputStream#read(byte[], int, int)
     */
    public void test_read$BII() throws IOException {
        byte[] buf1 = new byte[20];
        is.skip(50);
        is.mark(100);
        is.read(buf1, 0, buf1.length);
        assertTrue("Failed to read correct data", new String(buf1, 0,
                buf1.length).equals(fileString.substring(50, 70)));
    }

    /**
     * @tests ByteArrayInputStream#reset()
     */
    public void test_reset() throws IOException {
        byte[] buf1 = new byte[10];
        byte[] buf2 = new byte[10];
        is.mark(200);
        is.read(buf1, 0, 10);
        is.reset();
        is.read(buf2, 0, 10);
        is.reset();
        assertTrue("Reset failed", new String(buf1, 0, buf1.length)
                .equals(new String(buf2, 0, buf2.length)));
    }

    /**
     * @tests ByteArrayInputStream#skip(long)
     */
    public void test_skipJ() throws IOException {
        byte[] buf1 = new byte[10];
        is.skip(100);
        is.read(buf1, 0, buf1.length);
        assertTrue("Failed to skip to correct position", new String(buf1, 0,
                buf1.length).equals(fileString.substring(100, 110)));
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
        is = new ByteArrayInputStream(fileString.getBytes());

    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
        try {
            is.close();
        } catch (Exception e) {
        }
    }
}
