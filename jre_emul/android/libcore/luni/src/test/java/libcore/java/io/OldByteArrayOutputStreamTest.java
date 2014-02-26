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
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;
import tests.support.Support_OutputStream;

/**
 * Automated Test Suite for class java.io.ByteArrayOutputStream
 *
 * @see java.io.ByteArrayOutputStream
 */
public class OldByteArrayOutputStreamTest extends TestCase {

    ByteArrayOutputStream bos = null;

    public String fileString = "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_ByteArrayOutputStream\nTest_java_io_DataInputStream\n";

    public void test_ConstructorI() {
        bos = new java.io.ByteArrayOutputStream(100);
        assertEquals("Test 1: Failed to create stream;", 0, bos.size());

        try {
            bos = new ByteArrayOutputStream(-1);
            fail("Test 2: IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            // Expected.
        }
    }

    public void test_toStringLjava_lang_String() throws Exception {
        bos = new ByteArrayOutputStream();

        bos.write(fileString.getBytes(), 0, fileString.length());
        assertTrue("Test 1: Returned incorrect 8859-1 String",
                bos.toString("8859_1").equals(fileString));
        assertTrue("Test 2: Returned incorrect 8859-2 String",
                bos.toString("8859_2").equals(fileString));

        try {
            bos.toString("NotAnEcoding");
            fail("Test 3: UnsupportedEncodingException expected.");
        } catch (UnsupportedEncodingException e) {
            // Expected.
        }
    }

    public void test_write$BII_Exception() {
        byte[] target = new byte[10];
        bos = new ByteArrayOutputStream();
        try {
            bos.write(target, -1, 1);
            fail("Test 1: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
        try {
            bos.write(target, 0, -1);
            fail("Test 2: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
        try {
            bos.write(target, 1, target.length);
            fail("Test 3: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
        try {
            bos.write(null, 1, 1);
            fail("Test 4: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected.
        }
    }

    public void test_writeToLjava_io_OutputStream() throws Exception {
        Support_OutputStream sos = new Support_OutputStream();
        bos = new java.io.ByteArrayOutputStream();
        bos.write(fileString.getBytes(), 0, 10);
        bos.writeTo(sos);
        assertTrue("Test 1: Incorrect string written.",
                sos.toString().equals(
                        fileString.substring(0, 10)));

        sos.setThrowsException(true);
        try {
            bos.writeTo(sos);
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }
}
