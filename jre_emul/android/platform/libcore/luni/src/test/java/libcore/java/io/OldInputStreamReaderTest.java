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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import junit.framework.TestCase;
import tests.support.Support_ASimpleInputStream;

public class OldInputStreamReaderTest extends TestCase {

    private final String source = "This is a test message with Unicode character. \u4e2d\u56fd is China's name in Chinese";

    private InputStream in;

    private InputStreamReader reader;

    private InputStreamReader is;

    private InputStream fis;

    public String fileString = "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\n";

    protected void setUp() throws Exception {
        super.setUp();

        in = new ByteArrayInputStream(source.getBytes("UTF-8"));
        reader = new InputStreamReader(in, "UTF-8");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(bos);
        char[] buf = new char[fileString.length()];
        fileString.getChars(0, fileString.length(), buf, 0);
        osw.write(buf);
        osw.close();
        fis = new ByteArrayInputStream(bos.toByteArray());
        is = new InputStreamReader(fis);
    }

    protected void tearDown() throws Exception {
        try {
            in.close();
            is.close();
            fis.close();
        } catch (IOException e) {
        }

        super.tearDown();
    }

    public void testReadcharArrayintint() throws IOException {
        try {
            reader.read(new char[3], -1, 0);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //expected
        }
        try {
            reader.read(new char[3], 0, -1);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //expected
        }
        try {
            reader.read(new char[3], 4, 0);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //expected
        }
        try {
            reader.read(new char[3], 3, 1);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //expected
        }
        try {
            reader.read(new char[3], 1, 3);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //expected
        }
        try {
            reader.read(new char[3], 0, 4);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //expected
        }

        try {
            reader.read(null, 0, 0);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            //expected
        }

        assertEquals(0, reader.read(new char[3], 3, 0));
        char[] chars = new char[source.length()];
        assertEquals(0, reader.read(chars, 0, 0));
        assertEquals(0, chars[0]);
        assertEquals(3, reader.read(chars, 0, 3));
        assertEquals(5, reader.read(chars, 3, 5));
        assertEquals(source.length() - 8, reader.read(chars, 8,
                chars.length - 8));
        assertTrue(Arrays.equals(chars, source.toCharArray()));
        assertEquals(-1, reader.read(chars, 0, chars.length));
        assertTrue(Arrays.equals(chars, source.toCharArray()));
    }

    public void testReadcharArrayintint2() throws IOException {
        char[] chars = new char[source.length()];
        assertEquals(source.length() - 3, reader.read(chars, 0,
                chars.length - 3));
        assertEquals(3, reader.read(chars, 0, 10));
    }

    public void testReady() throws IOException {
        assertTrue(reader.ready());
        reader.read(new char[source.length()]);
        assertFalse(reader.ready());
    }

    public void testInputStreamReaderSuccessiveReads() throws IOException {
        byte[] data = new byte[8192 * 2];
        Arrays.fill(data, (byte) 116); // 116 = ISO-8859-1 value for 't'
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        InputStreamReader isr = new InputStreamReader(bis, "ISO-8859-1");

        // One less than the InputStreamReader.BUFFER_SIZE
        char[] buf = new char[8191];
        int bytesRead = isr.read(buf, 0, buf.length);
        if (bytesRead == -1) {
            throw new RuntimeException();
        }
        bytesRead = isr.read(buf, 0, buf.length);
        if (bytesRead == -1) {
            throw new RuntimeException();
        }
    }

    public void test_ConstructorLjava_io_InputStream() {
        // Test for method java.io.InputStreamReader(java.io.InputStream)
        assertTrue("Used to test other methods", true);
    }

    public void test_ConstructorLjava_io_InputStreamLjava_lang_String() {
        // Test for method java.io.InputStreamReader(java.io.InputStream,
        // java.lang.String)
        try {
            is = new InputStreamReader(fis, "8859_1");
        } catch (UnsupportedEncodingException e) {
            fail("Unable to create input stream : " + e.getMessage());
        }

        try {
            is = new InputStreamReader(fis, "Bogus");
        } catch (UnsupportedEncodingException e) {
            return;
        }
        fail("Failed to throw Unsupported Encoding exception");
    }

    public void test_close() {
        // Test for method void java.io.InputStreamReader.close()
        try {
            is.close();
        } catch (IOException e) {
            fail("Failed to close reader : " + e.getMessage());
        }
        try {
            is.read();
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
            // Exception means read failed due to close
        }

        is = new InputStreamReader(new Support_ASimpleInputStream(true));
        try {
            is.read();
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }

    }

    public void testClose() throws IOException {
        reader.close();
        try {
            reader.ready();
            fail("Should throw IOException");
        } catch (IOException e) {
        }
        reader.close();
    }

     public void test_read$CII() {
        // Test for method int java.io.InputStreamReader.read(char [], int, int)
        try {
            char[] rbuf = new char[100];
            char[] sbuf = new char[100];
            fileString.getChars(0, 100, sbuf, 0);
            is.read(rbuf, 0, 100);
            for (int i = 0; i < rbuf.length; i++)
                assertTrue("returned incorrect chars", rbuf[i] == sbuf[i]);
        } catch (IOException e) {
            fail("Exception during read test : " + e.getMessage());
        }
    }

    public void test_ready() {
        // Test for method boolean java.io.InputStreamReader.ready()
        try {
            assertTrue("Ready test failed", is.ready());
            is.read();
            assertTrue("More chars, but not ready", is.ready());
        } catch (IOException e) {
            fail("Exception during ready test : " + e.getMessage());
        }
    }

    /**
     * Test for regression of a bug that dropped characters when
     * multibyte encodings spanned buffer boundaries.
     */
    public void test_readWhenCharacterSpansBuffer() {
        final byte[] suffix = {
            (byte) 0x93, (byte) 0xa1, (byte) 0x8c, (byte) 0xb4,
            (byte) 0x97, (byte) 0x43, (byte) 0x88, (byte) 0xea,
            (byte) 0x98, (byte) 0x59
        };
        final char[] decodedSuffix = {
            (char) 0x85e4, (char) 0x539f, (char) 0x4f51, (char) 0x4e00,
            (char) 0x90ce
        };
        final int prefixLength = 8189;

        byte[] bytes = new byte[prefixLength + 10];
        Arrays.fill(bytes, (byte) ' ');
        System.arraycopy(suffix, 0, bytes, prefixLength, suffix.length);
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);

        try {
            InputStreamReader isr = new InputStreamReader(is, "SHIFT_JIS");
            char[] chars = new char[8192];
            int at = 0;

            for (;;) {
                int amt = isr.read(chars);
                if (amt <= 0) {
                    break;
                }

                for (int i = 0; i < amt; i++) {
                    char c = chars[i];
                    if (at < prefixLength) {
                        if (c != ' ') {
                            fail("Found bad prefix character " +
                                    (int) c + " at " + at);
                        }
                    } else {
                        char decoded = decodedSuffix[at - prefixLength];
                        if (c != decoded) {
                            fail("Found mismatched character " +
                                    (int) c + " at " + at);
                        }
                    }
                    at++;
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("unexpected exception", ex);
        }
    }
}
