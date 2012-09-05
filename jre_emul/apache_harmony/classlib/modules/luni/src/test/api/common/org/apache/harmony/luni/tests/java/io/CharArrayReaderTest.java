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
import java.io.IOException;

public class CharArrayReaderTest extends junit.framework.TestCase {

    char[] hw = { 'H', 'e', 'l', 'l', 'o', 'W', 'o', 'r', 'l', 'd' };

    CharArrayReader cr;

    /**
     * @tests java.io.CharArrayReader#CharArrayReader(char[])
     */
    public void test_Constructor$C() throws IOException {
        cr = new CharArrayReader(hw);
        assertTrue("Failed to create reader", cr.ready());
    }

    /**
     * @tests java.io.CharArrayReader#CharArrayReader(char[], int, int)
     */
    public void test_Constructor$CII() throws IOException {
        cr = new CharArrayReader(hw, 5, 5);
        assertTrue("Failed to create reader", cr.ready());

        int c = cr.read();
        assertTrue("Created incorrect reader--returned '" + (char) c
                + "' intsead of 'W'", c == 'W');
    }

    /**
     * @tests java.io.CharArrayReader#close()
     */
    public void test_close() {
        cr = new CharArrayReader(hw);
        cr.close();
        try {
            cr.read();
            fail("Failed to throw exception on read from closed stream");
        } catch (IOException e) {
            // Expected
        }

        // No-op
        cr.close();
    }

    /**
     * @tests java.io.CharArrayReader#mark(int)
     */
    public void test_markI() throws IOException {
        cr = new CharArrayReader(hw);
        cr.skip(5L);
        cr.mark(100);
        cr.read();
        cr.reset();
        assertEquals("Failed to mark correct position", 'W', cr.read());
    }

    /**
     * @tests java.io.CharArrayReader#markSupported()
     */
    public void test_markSupported() {
        cr = new CharArrayReader(hw);
        assertTrue("markSupported returned false", cr.markSupported());
    }

    /**
     * @tests java.io.CharArrayReader#read()
     */
    public void test_read() throws IOException {
        cr = new CharArrayReader(hw);
        assertEquals("Read returned incorrect char", 'H', cr.read());
        cr = new CharArrayReader(new char[] { '\u8765' });
        assertTrue("Incorrect double byte char", cr.read() == '\u8765');
    }

    /**
     * @tests java.io.CharArrayReader#read(char[], int, int)
     */
    public void test_read$CII() throws IOException {
        char[] c = new char[11];
        cr = new CharArrayReader(hw);
        cr.read(c, 1, 10);
        assertTrue("Read returned incorrect chars", new String(c, 1, 10)
                .equals(new String(hw, 0, 10)));
    }

    /**
     * @tests java.io.CharArrayReader#ready()
     */
    public void test_ready() throws IOException {
        cr = new CharArrayReader(hw);
        assertTrue("ready returned false", cr.ready());
        cr.skip(1000);
        assertTrue("ready returned true", !cr.ready());
        cr.close();

        try {
            cr.ready();
            fail("No exception 1");
        } catch (IOException e) {
            // expected
        }
        try {
            cr = new CharArrayReader(hw);
            cr.close();
            cr.ready();
            fail("No exception 2");
        } catch (IOException e) {
            // expected
        }
    }

    /**
     * @tests java.io.CharArrayReader#reset()
     */
    public void test_reset() throws IOException {
        cr = new CharArrayReader(hw);
        cr.skip(5L);
        cr.mark(100);
        cr.read();
        cr.reset();
        assertEquals("Reset failed to return to marker position", 'W', cr
                .read());

        // Regression for HARMONY-4357
        String str = "offsetHello world!";
        char[] data = new char[str.length()];
        str.getChars(0, str.length(), data, 0);
        int offsetLength = 6;
        int length = data.length - offsetLength;

        CharArrayReader reader = new CharArrayReader(data, offsetLength, length);
        reader.reset();
        for (int i = 0; i < length; i++) {
            assertEquals(data[offsetLength + i], (char) reader.read());
        }
    }

    /**
     * @tests java.io.CharArrayReader#skip(long)
     */
    public void test_skipJ() throws IOException {
        cr = new CharArrayReader(hw);
        long skipped = cr.skip(5L);

        assertEquals("Failed to skip correct number of chars", 5L, skipped);
        assertEquals("Skip skipped wrong chars", 'W', cr.read());
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
        if (cr != null)
            cr.close();
    }
}
