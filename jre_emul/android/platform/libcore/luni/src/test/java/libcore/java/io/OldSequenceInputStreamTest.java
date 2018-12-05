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
import java.io.SequenceInputStream;
import tests.support.Support_ASimpleInputStream;

public class OldSequenceInputStreamTest extends junit.framework.TestCase {

    Support_ASimpleInputStream simple1, simple2;
    SequenceInputStream si;
    String s1 = "Hello";
    String s2 = "World";

    public void test_available() throws IOException {
        assertEquals("Returned incorrect number of bytes!", s1.length(), si.available());
        simple2.throwExceptionOnNextUse = true;
        assertTrue("IOException on second stream should not affect at this time!",
                si.available() == s1.length());
        simple1.throwExceptionOnNextUse = true;
        try {
            si.available();
            fail("IOException not thrown!");
        } catch (IOException e) {
            // expected
        }
    }

    public void test_close2() throws IOException {
        simple1.throwExceptionOnNextUse = true;
        try {
            si.close();
            fail("IOException not thrown!");
        } catch (IOException e) {
            // expected
        }
    }

    public void test_read() throws IOException {
        si.read();
        assertEquals("Test 1: Incorrect char read;",
                s1.charAt(1), (char) si.read());

        // We are still reading from the first input stream, should be ok.
        simple2.throwExceptionOnNextUse = true;
        try {
            assertEquals("Test 2: Incorrect char read;",
                    s1.charAt(2), (char) si.read());
        } catch (IOException e) {
            fail("Test 3: Unexpected IOException.");
        }

        simple1.throwExceptionOnNextUse = true;
        try {
            si.read();
            fail("Test 4: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
        simple1.throwExceptionOnNextUse = false;

        // Reading bytes 4 and 5 of the first input stream should be ok again.
        si.read();
        si.read();

        // Reading the first byte of the second input stream should fail.
        try {
            si.read();
            fail("Test 5: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }

        // Reading from the second input stream should be ok now.
        simple2.throwExceptionOnNextUse = false;
        try {
            assertEquals("Test 6: Incorrect char read;",
                    s2.charAt(0), (char) si.read());
        } catch (IOException e) {
            fail("Test 7: Unexpected IOException.");
        }

        si.close();
        assertTrue("Test 8: -1 expected when reading from a closed " +
                   "sequence input stream.", si.read() == -1);
    }

    public void test_read_exc() throws IOException {
        simple2.throwExceptionOnNextUse = true;
        assertEquals("IOException on second stream should not affect at this time!", 72, si.read());
        simple1.throwExceptionOnNextUse = true;
        try {
            si.read();
            fail("IOException not thrown!");
        } catch (IOException e) {
            // expected
        }
    }

    public void test_read$BII_Excpetion() throws IOException {
        byte[] buf = new byte[4];
        si.read(buf, 0, 2);
        si.read(buf, 2, 1);
        simple2.throwExceptionOnNextUse = true;
        si.read(buf, 3, 1);
        assertEquals("Wrong stuff read!", "Hell", new String(buf));
        simple1.throwExceptionOnNextUse = true;
        try {
            si.read(buf, 3, 1);
            fail("IOException not thrown!");
        } catch (IOException e) {
            // expected
        }

        buf = new byte[10];
        simple1 = new Support_ASimpleInputStream(s1);
        simple2 = new Support_ASimpleInputStream(s2);
        si = new SequenceInputStream(simple1, simple2);
        try {
            si.read(buf, -1, 1);
            fail("IndexOutOfBoundsException was not thrown");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
        try {
            si.read(buf, 0, -1);
            fail("IndexOutOfBoundsException was not thrown");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
        try {
            si.read(buf, 1, 10);
            fail("IndexOutOfBoundsException was not thrown");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    protected void setUp() {
        simple1 = new Support_ASimpleInputStream(s1);
        simple2 = new Support_ASimpleInputStream(s2);
        si = new SequenceInputStream(simple1, simple2);
    }
}
