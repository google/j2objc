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

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.StringWriter;

public class CharArrayWriterTest extends junit.framework.TestCase {

    char[] hw = { 'H', 'e', 'l', 'l', 'o', 'W', 'o', 'r', 'l', 'd' };

    CharArrayWriter cw;

    CharArrayReader cr;

    /**
     * @tests java.io.CharArrayWriter#CharArrayWriter()
     */
    public void test_Constructor() {
        cw = new CharArrayWriter(90);
        assertEquals("Created incorrect writer", 0, cw.size());
    }

    /**
     * @tests java.io.CharArrayWriter#CharArrayWriter(int)
     */
    public void test_ConstructorI() {
        cw = new CharArrayWriter();
        assertEquals("Created incorrect writer", 0, cw.size());
    }

    /**
     * @tests java.io.CharArrayWriter#close()
     */
    public void test_close() {
        cw.close();
    }

    /**
     * @tests java.io.CharArrayWriter#flush()
     */
    public void test_flush() {
        cw.flush();
    }

    /**
     * @tests java.io.CharArrayWriter#reset()
     */
    public void test_reset() throws IOException {
        cw.write("HelloWorld", 5, 5);
        cw.reset();
        cw.write("HelloWorld", 0, 5);
        cr = new CharArrayReader(cw.toCharArray());
        char[] c = new char[100];
        cr.read(c, 0, 5);
        assertEquals("Reset failed to reset buffer", "Hello", new String(c, 0,
                5));
    }

    /**
     * @tests java.io.CharArrayWriter#size()
     */
    public void test_size() {
        assertEquals("Returned incorrect size", 0, cw.size());
        cw.write(hw, 5, 5);
        assertEquals("Returned incorrect size", 5, cw.size());
    }

    /**
     * @tests java.io.CharArrayWriter#toCharArray()
     */
    public void test_toCharArray() throws IOException {
        cw.write("HelloWorld", 0, 10);
        cr = new CharArrayReader(cw.toCharArray());
        char[] c = new char[100];
        cr.read(c, 0, 10);
        assertEquals("toCharArray failed to return correct array",
                "HelloWorld", new String(c, 0, 10));
    }

    /**
     * @tests java.io.CharArrayWriter#toString()
     */
    public void test_toString() {
        cw.write("HelloWorld", 5, 5);
        cr = new CharArrayReader(cw.toCharArray());
        assertEquals("Returned incorrect string", "World", cw.toString());
    }

    /**
     * @tests java.io.CharArrayWriter#write(char[], int, int)
     */
    public void test_write$CII() throws IOException {
        cw.write(hw, 5, 5);
        cr = new CharArrayReader(cw.toCharArray());
        char[] c = new char[100];
        cr.read(c, 0, 5);
        assertEquals("Writer failed to write correct chars", "World",
                new String(c, 0, 5));
    }

    /**
     * @tests java.io.CharArrayWriter#write(char[], int, int)
     */
    public void test_write$CII_2() {
        // Regression for HARMONY-387
        CharArrayWriter obj = new CharArrayWriter();
        try {
            obj.write(new char[] { '0' }, 0, -1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException t) {
            assertEquals(
                    "IndexOutOfBoundsException rather than a subclass expected",
                    IndexOutOfBoundsException.class, t.getClass());
        }
    }

    /**
     * @tests java.io.CharArrayWriter#write(int)
     */
    public void test_writeI() throws IOException {
        cw.write('T');
        cr = new CharArrayReader(cw.toCharArray());
        assertEquals("Writer failed to write char", 'T', cr.read());
    }

    /**
     * @tests java.io.CharArrayWriter#write(java.lang.String, int, int)
     */
    public void test_writeLjava_lang_StringII() throws IOException {
        cw.write("HelloWorld", 5, 5);
        cr = new CharArrayReader(cw.toCharArray());
        char[] c = new char[100];
        cr.read(c, 0, 5);
        assertEquals("Writer failed to write correct chars", "World",
                new String(c, 0, 5));
    }

    /**
     * @tests java.io.CharArrayWriter#write(java.lang.String, int, int)
     */
    public void test_writeLjava_lang_StringII_2()
            throws StringIndexOutOfBoundsException {
        // Regression for HARMONY-387
        CharArrayWriter obj = new CharArrayWriter();
        try {
            obj.write((String) null, -1, 0);
            fail("NullPointerException expected");
        } catch (NullPointerException t) {
            // Expected
        }
    }

    /**
     * @tests java.io.CharArrayWriter#writeTo(java.io.Writer)
     */
    public void test_writeToLjava_io_Writer() throws IOException {
        cw.write("HelloWorld", 0, 10);
        StringWriter sw = new StringWriter();
        cw.writeTo(sw);
        assertEquals("Writer failed to write correct chars", "HelloWorld", sw
                .toString());
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
        cw = new CharArrayWriter();
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
        if (cr != null) {
            cr.close();
        }
        cw.close();
    }

    /**
     * @tests java.io.CharArrayWriter#append(char)
     */
    public void test_appendChar() throws IOException {
        char testChar = ' ';
        CharArrayWriter writer = new CharArrayWriter(10);
        writer.append(testChar);
        writer.flush();
        assertEquals(String.valueOf(testChar), writer.toString());
        writer.close();
    }

    /**
     * @tests java.io.CharArrayWriter#append(CharSequence)
     */
    public void test_appendCharSequence() {
        String testString = "My Test String";
        CharArrayWriter writer = new CharArrayWriter(10);
        writer.append(testString);
        writer.flush();
        assertEquals(testString, writer.toString());
        writer.close();
    }

    /**
     * @tests java.io.CharArrayWriter#append(CharSequence, int, int)
     */
    public void test_appendCharSequenceIntInt() {
        String testString = "My Test String";
        CharArrayWriter writer = new CharArrayWriter(10);
        writer.append(testString, 1, 3);
        writer.flush();
        assertEquals(testString.substring(1, 3), writer.toString());
        writer.close();
    }
}
