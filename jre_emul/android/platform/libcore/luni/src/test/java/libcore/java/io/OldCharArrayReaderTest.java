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
import java.io.IOException;

public class OldCharArrayReaderTest extends junit.framework.TestCase {

    char[] hw = { 'H', 'e', 'l', 'l', 'o', 'W', 'o', 'r', 'l', 'd' };

    CharArrayReader cr;

    /**
     * java.io.CharArrayReader#CharArrayReader(char[])
     */
    public void test_Constructor$C() {
        // Test for method java.io.CharArrayReader(char [])

        try {
            cr = new CharArrayReader(hw);
            assertTrue("Failed to create reader", cr.ready());
        } catch (IOException e) {
            fail("Exception determining ready state : " + e.getMessage());
        }
    }

    /**
     * java.io.CharArrayReader#CharArrayReader(char[], int, int)
     */
    public void test_Constructor$CII() throws IOException {
        try {
            cr = new CharArrayReader(null, 0, 0);
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected.
        }
        try {
            cr = new CharArrayReader(hw, -1, 0);
            fail("Test 2: IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            // Expected.
        }
        try {
            cr = new CharArrayReader(hw, 0, -1);
            fail("Test 3: IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            // Expected.
        }
        try {
            cr = new CharArrayReader(hw, hw.length + 1, 1);
            fail("Test 4: IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            // Expected.
        }

        cr = new CharArrayReader(hw, 5, 5);
        assertTrue("Test 5: Failed to create reader", cr.ready());
        assertEquals("Test 6: Incorrect character read;",
                'W', cr.read());
    }

    /**
     * java.io.CharArrayReader#close()
     */
    public void test_close() {
        cr = new CharArrayReader(hw);
        cr.close();
        try {
            cr.read();
            fail("Failed to throw exception on read from closed stream");
        } catch (IOException e) {
            // Expected.
        }

    }

    /**
     * java.io.CharArrayReader#mark(int)
     */
    public void test_markI() throws IOException {
        cr = new CharArrayReader(hw);
        cr.skip(5L);
        cr.mark(100);
        cr.read();
        cr.reset();
        assertEquals("Test 1: Failed to mark correct position;",
                'W', cr.read());

        cr.close();
        try {
            cr.mark(100);
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * java.io.CharArrayReader#markSupported()
     */
    public void test_markSupported() {
        cr = new CharArrayReader(hw);
        assertTrue("markSupported returned false", cr.markSupported());
    }

    /**
     * java.io.CharArrayReader#read()
     */
    public void test_read() throws IOException {
        cr = new CharArrayReader(hw);
        assertEquals("Test 1: Read returned incorrect char;",
                'H', cr.read());
        cr = new CharArrayReader(new char[] { '\u8765' });
        assertTrue("Test 2: Incorrect double byte char;",
                cr.read() == '\u8765');

        cr.close();
        try {
            cr.read();
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    /**
     * java.io.CharArrayReader#read(char[], int, int)
     */
    public void test_read$CII() throws IOException {
        // Test for method int java.io.CharArrayReader.read(char [], int, int)
        char[] c = new char[11];
        cr = new CharArrayReader(hw);
        cr.read(c, 1, 10);
        assertTrue("Test 1: Read returned incorrect chars.",
                new String(c, 1, 10).equals(new String(hw, 0, 10)));

        // Illegal argument checks.
        try {
            cr.read(null, 1, 0);
            fail("Test 2: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected.
        }

        try {
            cr.read(c , -1, 1);
            fail("Test 3: ArrayIndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            cr.read(c , 1, -1);
            fail("Test 4: ArrayIndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            cr.read(c, 1, c.length);
            fail("Test 5: ArrayIndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        cr.close();
        try {
            cr.read(c, 1, 1);
            fail("Test 6: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_ready() {
        // Test for method boolean java.io.CharArrayReader.ready()
        cr = new CharArrayReader(hw);
        boolean expectException = false;
        try {
            assertTrue("ready returned false", cr.ready());
            cr.skip(1000);
            assertTrue("ready returned true", !cr.ready());
            cr.close();
            expectException = true;
            cr.ready();
            fail("No exception 1");
        } catch (IOException e) {
            if (!expectException)
                fail("Unexpected: " + e);
        }
        try {
            cr = new CharArrayReader(hw);
            cr.close();
            cr.ready();
            fail("No exception 2");
        } catch (IOException e) {
        }
    }

    /**
     * java.io.CharArrayReader#reset()
     */
    public void test_reset() throws IOException {
        cr = new CharArrayReader(hw);
        cr.skip(5L);
        cr.mark(100);
        cr.read();
        cr.reset();
        assertEquals("Test 1: Reset failed to return to marker position.",
                'W', cr.read());

        cr.close();
        try {
            cr.reset();
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    public void test_skipJ() throws IOException {
        long skipped = 0;
        cr = new CharArrayReader(hw);
        skipped = cr.skip(5L);
        assertEquals("Test 1: Failed to skip correct number of chars;",
                5L, skipped);
        assertEquals("Test 2: Skip skipped wrong chars;",
                'W', cr.read());

        cr.close();
        try {
            cr.skip(1);
            fail("Test 3: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

    protected void tearDown() {
        if (cr != null)
            cr.close();
    }
}
