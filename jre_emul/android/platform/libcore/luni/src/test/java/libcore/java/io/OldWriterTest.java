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
import java.io.Writer;
import junit.framework.TestCase;
import tests.support.Support_ASimpleWriter;

public class OldWriterTest extends TestCase {

    public void test_appendChar() throws IOException {
        Writer tobj = new Support_ASimpleWriter(2);
        tobj.append('a');
        tobj.append('b');
        assertEquals("Wrong stuff written!", "ab", tobj.toString());
        try {
            tobj.append('c');
            fail("IOException not thrown!");
        } catch (IOException e) {
            // expected
        }
    }

    public void test_appendCharSequence() throws IOException {
        String testString = "My Test String";
        Writer tobj = new Support_ASimpleWriter(20);
        tobj.append(testString);
        assertEquals("Wrong stuff written!", testString, tobj.toString());
        try {
            tobj.append(testString);
            fail("IOException not thrown!");
        } catch (IOException e) {
            // expected
        }
    }

    public void test_appendCharSequenceIntInt() throws IOException {
        String testString = "My Test String";
        Writer tobj = new Support_ASimpleWriter(21);
        testString = "0123456789abcdefghijABCDEFGHIJ";
        tobj.append(testString, 0, 5);
        assertEquals("Wrong stuff written!", "01234", tobj.toString());
        tobj.append(testString, 10, 15);
        assertEquals("Wrong stuff written!", "01234abcde", tobj.toString());
        tobj.append(testString, 20, 30);
        assertEquals("Wrong stuff written!", "01234abcdeABCDEFGHIJ", tobj.toString());
        try {
            tobj.append(testString, 30, 31);
            fail("IndexOutOfBoundsException not thrown!");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        tobj.append(testString, 20, 21); // Just fill the writer to its limit!
        try {
            tobj.append(testString, 29, 30);
            fail("IOException not thrown!");
        } catch (IOException e) {
            // expected
        }
    }

    public void test_appendCharSequenceIntInt_Exception() throws IOException {
        String testString = "My Test String";
        Writer tobj = new Support_ASimpleWriter(21);
        try {
            tobj.append(testString, 30, 31);
            fail("IndexOutOfBoundsException not thrown!");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            tobj.append(testString, -1, 1);
            fail("IndexOutOfBoundsException not thrown!");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            tobj.append(testString, 0, -1);
            fail("IndexOutOfBoundsException not thrown!");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    public void test_write$C() throws IOException {
        Writer tobj = new Support_ASimpleWriter(21);
        tobj.write("01234".toCharArray());
        assertEquals("Wrong stuff written!", "01234", tobj.toString());
        tobj.write("abcde".toCharArray());
        assertEquals("Wrong stuff written!", "01234abcde", tobj.toString());
        tobj.write("ABCDEFGHIJ".toCharArray());
        assertEquals("Wrong stuff written!", "01234abcdeABCDEFGHIJ", tobj.toString());
        tobj.write("z".toCharArray()); // Just fill the writer to its limit!
        try {
            tobj.write("LES JEUX SONT FAITS".toCharArray());
            fail("IOException not thrown!");
        } catch (IOException e) {
            // expected
        }
    }

    public void test_writeI() throws IOException {
        Writer tobj = new Support_ASimpleWriter(2);
        tobj.write('a');
        tobj.write('b');
        assertEquals("Wrong stuff written!", "ab", tobj.toString());
        try {
            tobj.write('c');
            fail("IOException not thrown!");
        } catch (IOException e) {
            // expected
        }
    }

    public void test_writeLjava_lang_String() throws IOException {
        Writer tobj = new Support_ASimpleWriter(21);
        tobj.write("01234");
        assertEquals("Wrong stuff written!", "01234", tobj.toString());
        tobj.write("abcde");
        assertEquals("Wrong stuff written!", "01234abcde", tobj.toString());
        tobj.write("ABCDEFGHIJ");
        assertEquals("Wrong stuff written!", "01234abcdeABCDEFGHIJ", tobj.toString());
        tobj.write("z"); // Just fill the writer to its limit!
        try {
            tobj.write("LES JEUX SONT FAITS");
            fail("IOException not thrown!");
        } catch (IOException e) {
            // expected
        }
    }

    public void test_writeLjava_lang_StringII() throws IOException {
        String testString;
        Writer tobj = new Support_ASimpleWriter(21);
        testString = "0123456789abcdefghijABCDEFGHIJ";
        tobj.write(testString, 0, 5);
        assertEquals("Wrong stuff written!", "01234", tobj.toString());
        tobj.write(testString, 10, 5);
        assertEquals("Wrong stuff written!", "01234abcde", tobj.toString());
        tobj.write(testString, 20, 10);
        assertEquals("Wrong stuff written!", "01234abcdeABCDEFGHIJ", tobj.toString());
        try {
            tobj.write(testString, 30, 1);
            fail("IndexOutOfBoundsException not thrown!");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        tobj.write(testString, 20, 1); // Just fill the writer to its limit!
        try {
            tobj.write(testString, 29, 1);
            fail("IOException not thrown!");
        } catch (IOException e) {
            // expected
        }
    }

    public void test_writeLjava_lang_StringII_Exception() throws IOException {
        String testString = "My Test String";
        Writer tobj = new Support_ASimpleWriter(21);
        try {
            tobj.write(testString, 30, 31);
            fail("IndexOutOfBoundsException not thrown!");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            tobj.write(testString, -1, 1);
            fail("IndexOutOfBoundsException not thrown!");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            tobj.write(testString, 0, -1);
            fail("IndexOutOfBoundsException not thrown!");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }
}
