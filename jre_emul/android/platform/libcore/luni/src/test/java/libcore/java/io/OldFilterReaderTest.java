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
import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class OldFilterReaderTest extends junit.framework.TestCase {

    private boolean called;
    private FilterReader fr;

    static class MyFilterReader extends java.io.FilterReader {
        public MyFilterReader(java.io.Reader reader) {
            super(reader);
        }
    }

    class MockReader extends java.io.Reader {
        public MockReader() {
        }

        public void close() throws IOException {
            called = true;
        }

        public void mark(int readLimit) throws IOException {
            called = true;
        }

        public boolean markSupported() {
            called = true;
            return false;
        }

        public int read() throws IOException {
            called = true;
            return 0;
        }

        public int read(char[] buffer, int offset, int count) throws IOException {
            called = true;
            return 0;
        }

        public boolean ready() throws IOException {
            called = true;
            return true;
        }

        public void reset() throws IOException {
            called = true;
        }

        public long skip(long count) throws IOException {
            called = true;
            return 0;
        }
    }

    public void test_ConstructorLjava_io_Reader() {

        FilterReader myReader = null;

        called = true;

        try {
            myReader = new MyFilterReader(null);
            fail("NullPointerException expected.");
        } catch (NullPointerException e) {
            // expected
        }
    }

    public void test_close() throws IOException {
        fr.close();
        assertTrue("close() has not been called.", called);
    }

    public void test_markI() throws IOException {
        fr.mark(0);
        assertTrue("mark(int) has not been called.", called);
    }

    public void test_markSupported() {
        fr.markSupported();
        assertTrue("markSupported() has not been called.", called);
    }

    public void test_read() throws IOException {
        fr.read();
        assertTrue("read() has not been called.", called);
    }

    public void test_read$CII() throws IOException {
        char[] buffer = new char[5];
        fr.read(buffer, 0, 5);
        assertTrue("read(char[], int, int) has not been called.", called);
    }

    public void test_read$CII_Exception() throws IOException {
        byte[] bbuffer = new byte[20];
        char[] buffer = new char[10];

        fr = new MyFilterReader(new InputStreamReader(
            new ByteArrayInputStream(bbuffer)));

        try {
            fr.read(buffer, 0, -1);
            fail("Test 1: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }

        try {
            fr.read(buffer, -1, 1);
            fail("Test 2: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }

        try {
            fr.read(buffer, 10, 1);
            fail("Test 3: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }
    }

    public void test_ready() throws IOException {
        fr.ready();
        assertTrue("ready() has not been called.", called);
    }

    public void test_reset() throws IOException {
        fr.reset();
        assertTrue("reset() has not been called.", called);
    }

    public void test_skip() throws IOException {
        fr.skip(10);
        assertTrue("skip(long) has not been called.", called);
    }

    protected void setUp() {

        fr = new MyFilterReader(new MockReader());
        called = false;
    }

    protected void tearDown() {

        try {
            fr.close();
        } catch (Exception e) {
            System.out.println("Exception during FilterReaderTest tear down.");
        }
    }
}
