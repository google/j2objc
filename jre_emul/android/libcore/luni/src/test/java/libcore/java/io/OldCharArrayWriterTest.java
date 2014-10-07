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

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.StringWriter;
import tests.support.Support_ASimpleWriter;

public class OldCharArrayWriterTest extends junit.framework.TestCase {

    CharArrayWriter cw;
    CharArrayReader cr;

    public void test_ConstructorI() {
        // Test for method java.io.CharArrayWriter(int)
        cw = new CharArrayWriter(90);
        assertEquals("Test 1: Incorrect writer created.", 0, cw.size());

        try {
            cw = new CharArrayWriter(-1);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            // Expected.
        }
    }

    public void test_write$CII_Exception() {
        char[] target = new char[10];
        cw = new CharArrayWriter();
        try {
            cw.write(target, -1, 1);
            fail("Test 1: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
        try {
            cw.write(target, 0, -1);
            fail("Test 2: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
        try {
            cw.write(target, 1, target.length);
            fail("Test 3: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
        try {
            cw.write((char[]) null, 1, 1);
            fail("Test 4: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected.
        }
    }

    public void test_writeToLjava_io_Writer() {
        Support_ASimpleWriter ssw = new Support_ASimpleWriter(true);
        cw.write("HelloWorld", 0, 10);
        StringWriter sw = new StringWriter();
        try {
            cw.writeTo(sw);
            assertEquals("Test 1: Writer failed to write correct chars;",
                         "HelloWorld", sw.toString());
        } catch (IOException e) {
            fail("Exception during writeTo test : " + e.getMessage());
        }

        try {
            cw.writeTo(ssw);
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_appendLjava_langCharSequenceII() {
        String testString = "My Test String";
        CharArrayWriter writer = new CharArrayWriter(10);

        // Illegal argument checks.
        try {
            writer.append(testString, -1, 0);
            fail("Test 1: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }
        try {
            writer.append(testString, 0, -1);
            fail("Test 2: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }
        try {
            writer.append(testString, 1, 0);
            fail("Test 3: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }
        try {
            writer.append(testString, 1, testString.length() + 1);
            fail("Test 4: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }

        writer.append(testString, 1, 3);
        writer.flush();
        assertEquals("Test 5: Appending failed;",
                testString.substring(1, 3), writer.toString());
        writer.close();
    }

    protected void setUp() {
        cw = new CharArrayWriter();
    }

    protected void tearDown() {
        if (cr != null) {
            cr.close();
        }
        cw.close();
    }
}
