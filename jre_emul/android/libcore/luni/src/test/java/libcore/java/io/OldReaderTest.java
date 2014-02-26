/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.java.io;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

import junit.framework.TestCase;
import tests.support.Support_ASimpleReader;

public class OldReaderTest extends TestCase {

    public void test_Reader() {
        MockReader r = new MockReader();
        assertTrue("Test 1: Lock has not been set correctly.", r.lockSet(r));
    }

    public void test_Reader_CharBufferChar() throws IOException {
        Support_ASimpleReader simple;
        simple = new Support_ASimpleReader("Bla bla, what else?");
        CharBuffer buf = CharBuffer.allocate(4);
        assertEquals("Wrong return value!", 4, simple.read(buf));
        buf.rewind();
        assertEquals("Wrong stuff read!", "Bla ", String.valueOf(buf));
        simple.read(buf);
        buf.rewind();
        assertEquals("Wrong stuff read!", "bla,", String.valueOf(buf));
        simple.throwExceptionOnNextUse = true;
        try {
            simple.read(buf);
            fail("IOException not thrown!");
        } catch (IOException expected) {
        }
    }

    public void test_Read_$C() throws IOException {
        Support_ASimpleReader simple;
        simple = new Support_ASimpleReader("Bla bla, what else?");
        char[] buf = new char[4];
        assertEquals("Wrong return value!", 4, simple.read(buf));
        assertEquals("Wrong stuff read!", "Bla ", new String(buf));
        simple.read(buf);
        assertEquals("Wrong stuff read!", "bla,", new String(buf));
        simple.throwExceptionOnNextUse = true;
        try {
            simple.read(buf);
            fail("IOException not thrown!");
        } catch (IOException expected) {
        }
    }


    public void test_markSupported() {
        assertFalse("markSupported must return false", new MockReader().markSupported());
    }

    public void test_read() throws IOException {
        Support_ASimpleReader simple = new Support_ASimpleReader("Bla bla, what else?");
        int res = simple.read();
        assertEquals("Wrong stuff read!", 'B', res);
        res = simple.read();
        assertEquals("Wrong stuff read!", 'l', res);
        simple.throwExceptionOnNextUse = true;
        try {
            simple.read();
            fail("IOException not thrown!");
        } catch (IOException expected) {
        }
    }

    public void test_ready() throws IOException {
        Support_ASimpleReader simple = new Support_ASimpleReader("Bla bla, what else?");
        simple.throwExceptionOnNextUse = true;
        try {
            simple.ready();
            fail("IOException not thrown!");
        } catch (IOException expected) {
        }
    }

    public void test_skip() throws IOException {
        Support_ASimpleReader simple = new Support_ASimpleReader("Bla bla, what else?");
        char[] buf = new char[4];
        simple.read(buf);
        assertEquals("Wrong stuff read!", "Bla ", new String(buf));
        simple.skip(5);
        simple.read(buf);
        assertEquals("Wrong stuff read!", "what", new String(buf));
        simple.throwExceptionOnNextUse = true;
        try {
            simple.skip(1);
            fail("IOException not thrown!");
        } catch (IOException expected) {
        }
    }

    class MockReader extends Reader {

        @Override public void close() {}

        @Override public int read(char[] buf, int offset, int count) {
            throw new UnsupportedOperationException();
        }

        public boolean lockSet(Object o) {
            return (lock == o);
        }
    }
}
