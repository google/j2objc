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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import junit.framework.TestCase;
import tests.support.Support_OutputStream;

public class OldOutputStreamWriterTest extends TestCase {

    OutputStreamWriter osw;
    InputStreamReader isr;

    private Support_OutputStream fos;

    public String testString = "This is a test message with Unicode characters. \u4e2d\u56fd is China's name in Chinese";

    protected void setUp() throws Exception {
        super.setUp();
        fos = new Support_OutputStream(500);
        osw = new OutputStreamWriter(fos, "UTF-8");
    }

    protected void tearDown() throws Exception {
        try {
            if (isr != null) isr.close();
            osw.close();
        } catch (Exception e) {
        }

        super.tearDown();
    }

    public void test_ConstructorLjava_io_OutputStream() throws IOException {
        OutputStreamWriter writer = null;

        try {
            writer = new OutputStreamWriter(null);
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected
        }

        try {
            writer = new OutputStreamWriter(new Support_OutputStream());
        } catch (Exception e) {
            fail("Test 2: Unexpected exception: " + e.getMessage());
        }

        // Test that the default encoding has been used.
        assertEquals("Test 3: Incorrect default encoding used.",
                     Charset.defaultCharset(),
                     Charset.forName(writer.getEncoding()));

        if (writer != null) writer.close();
    }

    public void test_ConstructorLjava_io_OutputStreamLjava_lang_String()
            throws UnsupportedEncodingException {

        try {
            osw = new OutputStreamWriter(null, "utf-8");
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected
        }

        try {
            osw = new OutputStreamWriter(fos, (String) null);
            fail("Test 2: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected
        }

        try {
            osw = new OutputStreamWriter(fos, "");
            fail("Test 3: UnsupportedEncodingException expected.");
        } catch (UnsupportedEncodingException e) {
            // Expected
        }

        try {
            osw = new OutputStreamWriter(fos, "Bogus");
            fail("Test 4: UnsupportedEncodingException expected.");
        } catch (UnsupportedEncodingException e) {
            // Expected
        }

        try {
            osw = new OutputStreamWriter(fos, "8859_1");
        } catch (UnsupportedEncodingException e) {
            fail("Test 5: Unexpected UnsupportedEncodingException.");
        }

        assertEquals("Test 6: Encoding not set correctly. ",
                Charset.forName("8859_1"),
                Charset.forName(osw.getEncoding()));
    }

    public void test_ConstructorLjava_io_OutputStreamLjava_nio_charset_Charset()
            throws IOException {
        OutputStreamWriter writer;
        Support_OutputStream out = new Support_OutputStream();
        Charset cs = Charset.forName("ascii");

        try {
            writer = new OutputStreamWriter(null, cs);
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected
        }

        try {
            writer = new OutputStreamWriter(out, (Charset) null);
            fail("Test 2: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected
        }

        writer = new OutputStreamWriter(out, cs);
        assertEquals("Test 3: Encoding not set correctly. ",
                     cs, Charset.forName(writer.getEncoding()));
        writer.close();
    }

    public void test_ConstructorLjava_io_OutputStreamLjava_nio_charset_CharsetEncoder()
            throws IOException {
        OutputStreamWriter writer;
        Support_OutputStream out = new Support_OutputStream();
        Charset cs = Charset.forName("ascii");
        CharsetEncoder enc = cs.newEncoder();

        try {
            writer = new OutputStreamWriter(null, enc);
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected
        }

        try {
            writer = new OutputStreamWriter(out, (CharsetEncoder) null);
            fail("Test 2: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected
        }

        writer = new OutputStreamWriter(out, cs);
        assertEquals("Test 3: CharacterEncoder not set correctly. ",
                     cs, Charset.forName(writer.getEncoding()));
        writer.close();
    }

    public void test_close() {

        fos.setThrowsException(true);
        try {
            osw.close();
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }

/* Test 2 does not work and has therefore been disabled (see Ticket #87).
        // Test 2: Write should not fail since the closing
        // in test 1 has not been successful.
        try {
            osw.write("Lorem ipsum...");
        } catch (IOException e) {
            fail("Test 2: Unexpected IOException.");
        }

        // Test 3: Close should succeed.
        fos.setThrowsException(false);
        try {
            osw.close();
        } catch (IOException e) {
            fail("Test 3: Unexpected IOException.");
        }
*/

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            OutputStreamWriter writer = new OutputStreamWriter(bout,
                    "ISO2022JP");
            writer.write(new char[] { 'a' });
            writer.close();
            // The default is ASCII, there should not be any mode changes.
            String converted = new String(bout.toByteArray(), "ISO8859_1");
            assertTrue("Test 4: Invalid conversion: " + converted,
                       converted.equals("a"));

            bout.reset();
            writer = new OutputStreamWriter(bout, "ISO2022JP");
            writer.write(new char[] { '\u3048' });
            writer.flush();
            // The byte sequence should not switch to ASCII mode until the
            // stream is closed.
            converted = new String(bout.toByteArray(), "ISO8859_1");
            assertTrue("Test 5: Invalid conversion: " + converted,
                       converted.equals("\u001b$B$("));
            writer.close();
            converted = new String(bout.toByteArray(), "ISO8859_1");
            assertTrue("Test 6: Invalid conversion: " + converted,
                       converted.equals("\u001b$B$(\u001b(B"));

            bout.reset();
            writer = new OutputStreamWriter(bout, "ISO2022JP");
            writer.write(new char[] { '\u3048' });
            writer.write(new char[] { '\u3048' });
            writer.close();
            // There should not be a mode switch between writes.
            assertEquals("Test 7: Invalid conversion. ",
                         "\u001b$B$($(\u001b(B",
                         new String(bout.toByteArray(), "ISO8859_1"));
        } catch (UnsupportedEncodingException e) {
            // Can't test missing converter.
            System.out.println(e);
        } catch (IOException e) {
            fail("Unexpected: " + e);
        }
    }

    public void test_flush() {
        // Test for method void java.io.OutputStreamWriter.flush()
        try {
            char[] buf = new char[testString.length()];
            osw.write(testString, 0, testString.length());
            osw.flush();
            openInputStream();
            isr.read(buf, 0, buf.length);
            assertTrue("Test 1: Characters have not been flushed.",
                       new String(buf, 0, buf.length).equals(testString));
        } catch (Exception e) {
            fail("Test 1: Unexpected exception: " + e.getMessage());
        }

        fos.setThrowsException(true);
        try {
            osw.flush();
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
            // Expected
        }
        fos.setThrowsException(false);
    }

    public void test_write_US_ASCII() throws Exception {
        testEncodeCharset("US-ASCII", 128);
    }

    public void test_write_ISO_8859_1() throws Exception {
        testEncodeCharset("ISO-8859-1", 256);
    }

    public void test_write_UTF_16BE() throws Exception {
        testEncodeCharset("UTF-16BE", 0xd800);
    }

    public void test_write_UTF_16LE() throws Exception {
        testEncodeCharset("UTF-16LE", 0xd800);
    }

    public void test_write_UTF_16() throws Exception {
        testEncodeCharset("UTF-16", 0xd800);
    }

    public void test_write_UTF_8() throws Exception {
        testEncodeCharset("UTF-8", 0xd800);
    }

    private void testEncodeCharset(String charset, int maxChar) throws Exception {
        char[] chars = new char[maxChar];
        for (int i = 0; i < maxChar; i++) {
            chars[i] = (char) i;
        }

        // to byte array
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        OutputStreamWriter charsOut = new OutputStreamWriter(bytesOut, charset);
        charsOut.write(chars);
        charsOut.flush();

        // decode from byte array, one character at a time
        ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytesOut.toByteArray());
        InputStreamReader charsIn = new InputStreamReader(bytesIn, charset);
        for (int i = 0; i < maxChar; i++) {
            assertEquals(i, charsIn.read());
        }
        assertEquals(-1, charsIn.read());

        // decode from byte array, using byte buffers
        bytesIn = new ByteArrayInputStream(bytesOut.toByteArray());
        charsIn = new InputStreamReader(bytesIn, charset);
        char[] decoded = new char[maxChar];
        for (int r = 0; r < maxChar; ) {
            r += charsIn.read(decoded, r, maxChar - r);
        }
        assertEquals(-1, charsIn.read());
        for (int i = 0; i < maxChar; i++) {
            assertEquals(i, decoded[i]);
        }
    }

    public void test_getEncoding() throws IOException {
        OutputStreamWriter writer;
        writer = new OutputStreamWriter(new Support_OutputStream(), "utf-8");
        assertEquals("Test 1: Incorrect encoding returned.",
                     Charset.forName("utf-8"),
                     Charset.forName(writer.getEncoding()));

        writer.close();
        assertNull("Test 2: getEncoding() did not return null for a closed writer.",
                   writer.getEncoding());
    }

    public void test_write$CII() throws IOException {
        char[] chars = testString.toCharArray();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Support_OutputStream out = new Support_OutputStream(500);
        OutputStreamWriter writer;

        writer = new OutputStreamWriter(out, "utf-8");

        try {
            writer.write(chars, -1, 1);
            fail("Test 1: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            writer.write(chars, 0, -1);
            fail("Test 2: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            writer.write(new char[0], 0, 1);
            fail("Test 3: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            writer.write((char[]) null, 0, 1);
            fail("Test 4: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected
        }

        try {
            writer.write(chars, 1, chars.length);
            fail("Test 5a: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
        try {
            writer.write(chars, 0, chars.length + 1);
            fail("Test 5b: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
        try {
            writer.write(chars, chars.length, 1);
            fail("Test 5c: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
        try {
            writer.write(chars, chars.length + 1, 0);
            fail("Test 5d: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        out.setThrowsException(true);
        try {
            for (int i = 0; i < 200; i++) {
                writer.write(chars, 0, chars.length);
            }
            fail("Test 6: IOException expected.");
        } catch (IOException e) {
            // Expected
        }
        out.setThrowsException(false);

        writer.close();
        writer = new OutputStreamWriter(baos, "utf-8");
        writer.write(chars, 1, 2);
        writer.flush();
        assertEquals("Test 7: write(char[], int, int) has not produced the " +
                     "expected content in the output stream.",
                     "hi", baos.toString("utf-8"));

        writer.write(chars, 0, chars.length);
        writer.flush();
        assertEquals("Test 8: write(char[], int, int) has not produced the " +
                "expected content in the output stream.",
                "hi" + testString, baos.toString("utf-8"));

        writer.close();
        try {
            writer.write((char[]) null, -1, -1);
            fail("Test 9: IOException expected.");
        } catch (IOException e) {
            // Expected
        }
    }

    public void test_writeI() throws IOException {
        Support_OutputStream out = new Support_OutputStream(500);
        OutputStreamWriter writer;

        out.setThrowsException(true);
        writer = new OutputStreamWriter(out, "utf-8");
        try {
            // Since there is an internal buffer in the encoder, more than
            // one character needs to be written.
            for (int i = 0; i < 200; i++) {
                for (int j = 0; j < testString.length(); j++) {
                    writer.write(testString.charAt(j));
                }
            }
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
            // Expected
        }
        out.setThrowsException(false);
        writer.close();

        writer = new OutputStreamWriter(out, "utf-8");
        writer.write(1);
        writer.flush();
        String str = new String(out.toByteArray(), "utf-8");
        assertEquals("Test 2: ", "\u0001", str);

        writer.write(2);
        writer.flush();
        str = new String(out.toByteArray(), "utf-8");
        assertEquals("Test 3: ", "\u0001\u0002", str);

        writer.write(-1);
        writer.flush();
        str = new String(out.toByteArray(), "utf-8");
        assertEquals("Test 4: ", "\u0001\u0002\uffff", str);

        writer.write(0xfedcb);
        writer.flush();
        str = new String(out.toByteArray(), "utf-8");
        assertEquals("Test 5: ", "\u0001\u0002\uffff\uedcb", str);

        writer.close();
        try {
            writer.write(1);
            fail("Test 6: IOException expected.");
        } catch (IOException e) {
            // Expected
        }
    }

    public void test_writeLjava_lang_StringII() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Support_OutputStream out = new Support_OutputStream(500);
        OutputStreamWriter writer;

        writer = new OutputStreamWriter(out, "utf-8");

        try {
            writer.write("Lorem", -1, 0);
            fail("Test 1: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            writer.write("Lorem", 0, -1);
            fail("Test 2: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            writer.write("", 0, 1);
            fail("Test 3: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            writer.write(testString, 1, testString.length());
            fail("Test 4a: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            writer.write(testString, 0, testString.length() + 1);
            fail("Test 4b: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            writer.write(testString, testString.length(), 1);
            fail("Test 4c: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            writer.write(testString, testString.length() + 1, 0);
            fail("Test 4d: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        try {
            writer.write((String) null, 0, 1);
            fail("Test 5: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected
        }

        out.setThrowsException(true);
        try {
            for (int i = 0; i < 200; i++) {
                writer.write(testString, 0, testString.length());
            }
            fail("Test 6: IOException expected.");
        } catch (IOException e) {
            // Expected
        }
        out.setThrowsException(false);

        writer.close();
        writer = new OutputStreamWriter(baos, "utf-8");

        writer.write("abc", 1, 2);
        writer.flush();
        assertEquals("Test 7: write(String, int, int) has not produced the " +
                     "expected content in the output stream.",
                     "bc", baos.toString("utf-8"));

        writer.write(testString, 0, testString.length());
        writer.flush();
        assertEquals("Test 7: write(String, int, int) has not produced the " +
                     "expected content in the output stream.",
                     "bc" + testString, baos.toString("utf-8"));

        writer.close();
        try {
            writer.write("abc", 0, 1);
            fail("Test 8: IOException expected.");
        } catch (IOException e) {
            // Expected
        }
    }

    private void openInputStream() {
        try {
            isr = new InputStreamReader(new ByteArrayInputStream(fos.toByteArray()), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            fail("UTF-8 not supported");
        }
    }
}
