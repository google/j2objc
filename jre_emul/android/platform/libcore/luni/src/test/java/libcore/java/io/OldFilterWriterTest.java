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

import java.io.ByteArrayOutputStream;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class OldFilterWriterTest extends junit.framework.TestCase {

    private boolean called;
    private FilterWriter fw;

    static class MyFilterWriter extends java.io.FilterWriter {
        public MyFilterWriter(java.io.Writer writer) {
            super(writer);
        }
    }

    class MockWriter extends java.io.Writer {
        public MockWriter() {
        }

        public void close() throws IOException {
            called = true;
        }

        public void flush() throws IOException {
            called = true;
        }

        public void write(char[] buffer, int offset, int count) throws IOException {
            called = true;
        }

        public void write(int oneChar) throws IOException {
            called = true;
        }

        public void write(String str, int offset, int count) throws IOException {
            called = true;
        }

        public long skip(long count) throws IOException {
            called = true;
            return 0;
        }
    }

    public void test_ConstructorLjava_io_Writer() {

        FilterWriter myWriter = null;

        called = true;

        try {
            myWriter = new MyFilterWriter(null);
            fail("NullPointerException expected.");
        } catch (NullPointerException e) {
            // expected
        }
    }

    public void test_close() throws IOException {
        fw.close();
        assertTrue("close() has not been called.", called);
    }

    public void test_flush() throws IOException {
        fw.flush();
        assertTrue("flush() has not been called.", called);
    }

    public void test_writeI() throws IOException {
        fw.write(0);
        assertTrue("write(int) has not been called.", called);
    }

    public void test_write$CII() throws IOException {
        char[] buffer = new char[5];
        fw.write(buffer, 0, 5);
        assertTrue("write(char[], int, int) has not been called.", called);
    }

    public void test_write$CII_Exception() throws IOException {
        char[] buffer = new char[10];

        fw = new MyFilterWriter(new OutputStreamWriter(
            new ByteArrayOutputStream()));

        try {
            fw.write(buffer, 0, -1);
            fail("Test 1: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }

        try {
            fw.write(buffer, -1, 1);
            fail("Test 2: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }

        try {
            fw.write(buffer, 10, 1);
            fail("Test 3: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }
    }

    public void test_writeLjava_lang_StringII() throws IOException {
        fw.write("Hello world", 0, 5);
        assertTrue("write(String, int, int) has not been called.", called);
    }

    protected void setUp() {

        fw = new MyFilterWriter(new MockWriter());
        called = false;
    }

    protected void tearDown() {

        try {
            fw.close();
        } catch (Exception e) {
            System.out.println("Exception during OldFilterWriterTest tear down.");
        }
    }
}
