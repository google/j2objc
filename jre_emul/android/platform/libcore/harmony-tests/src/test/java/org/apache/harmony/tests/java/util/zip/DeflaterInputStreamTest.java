/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

package org.apache.harmony.tests.java.util.zip;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;
import libcore.io.Streams;
/* J2ObjC removed: not supported by Junit 4.11 (https://github.com/google/j2objc/issues/1318).
import libcore.junit.junit3.TestCaseWithRules;
import libcore.junit.util.ResourceLeakageDetector;
import libcore.junit.util.ResourceLeakageDetector.DisableResourceLeakageDetection; */
import org.junit.Rule;
import org.junit.rules.TestRule;

public class DeflaterInputStreamTest extends junit.framework.TestCase /* J2ObjC removed: TestCaseWithRules */ {
    /* J2ObjC removed: not supported by Junit 4.11 (https://github.com/google/j2objc/issues/1318).
    @Rule
    public TestRule guardRule = ResourceLeakageDetector.getRule(); */

    private static final String TEST_STR = "Hi,this is a test";

    private static final byte[] TEST_STRING_DEFLATED_BYTES = {
            120, -100, -13, -56, -44, 41, -55, -56, 44, 86,
            0, -94, 68, -123, -110, -44, -30, 18, 0, 52,
            34, 5, -13 };

    private InputStream is;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        is = new ByteArrayInputStream(TEST_STR.getBytes("UTF-8"));
    }

    @Override
    protected void tearDown() throws Exception {
        is.close();
        super.tearDown();
    }

    /**
     * DeflaterInputStream#available()
     */
    public void testAvailable() throws IOException {
        byte[] buf = new byte[1024];
        DeflaterInputStream dis = new DeflaterInputStream(is);
        assertEquals(1, dis.available());
        assertEquals(120, dis.read());
        assertEquals(1, dis.available());
        assertEquals(22, dis.read(buf, 0, 1024));
        assertEquals(0, dis.available());
        assertEquals(-1, dis.read());
        assertEquals(0, dis.available());
        dis.close();
        try {
            dis.available();
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
    }

    /**
     * DeflaterInputStream#close()
     */
    public void testClose() throws IOException {
        byte[] buf = new byte[1024];
        DeflaterInputStream dis = new DeflaterInputStream(is);
        assertEquals(1, dis.available());
        dis.close();
        try {
            dis.available();
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
        try {
            dis.read(buf, 0, 1024);
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
        // can close after close
        dis.close();
    }

    /**
     * DeflaterInputStream#mark()
     */
    public void testMark() throws IOException {
        // mark do nothing
        DeflaterInputStream dis = new DeflaterInputStream(is);
        dis.mark(-1);
        dis.mark(0);
        dis.mark(1);
        dis.close();
        dis.mark(1);
    }

    /**
     * DeflaterInputStream#markSupported()
     */
    public void testMarkSupported() throws IOException {
        DeflaterInputStream dis = new DeflaterInputStream(is);
        assertFalse(dis.markSupported());
        dis.close();
        assertFalse(dis.markSupported());
    }

    /**
     * DeflaterInputStream#read()
     */
    public void testRead() throws IOException {
        DeflaterInputStream dis = new DeflaterInputStream(is);
        assertEquals(1, dis.available());
        assertEquals(120, dis.read());
        assertEquals(1, dis.available());
        assertEquals(156, dis.read());
        assertEquals(1, dis.available());
        assertEquals(243, dis.read());
        assertEquals(1, dis.available());
        dis.close();
        try {
            dis.read();
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
    }

    public void testRead_golden() throws Exception {
        try (DeflaterInputStream dis = new DeflaterInputStream(is)) {
            byte[] contents = Streams.readFully(dis);
            assertTrue(Arrays.equals(TEST_STRING_DEFLATED_BYTES, contents));
        }

        try (DeflaterInputStream dis = new DeflaterInputStream(
                new ByteArrayInputStream(TEST_STR.getBytes("UTF-8")))) {
            byte[] result = new byte[32];
            int count = 0;
            int bytesRead;
            while ((bytesRead = dis.read(result, count, 4)) != -1) {
                count += bytesRead;
            }
            assertEquals(23, count);
            byte[] splicedResult = new byte[23];
            System.arraycopy(result, 0, splicedResult, 0, 23);
            assertTrue(Arrays.equals(TEST_STRING_DEFLATED_BYTES, splicedResult));
        }
    }

    public void testRead_leavesBufUnmodified() throws Exception {
        DeflaterInputStreamWithPublicBuffer dis = new DeflaterInputStreamWithPublicBuffer(is);
        byte[] contents = Streams.readFully(dis);
        assertTrue(Arrays.equals(TEST_STRING_DEFLATED_BYTES, contents));

        // protected field buf is a part of the public API of this class.
        // we guarantee that it's only used as an input buffer, and not for
        // anything else.
        byte[] buf = dis.getBuffer();
        byte[] expected = TEST_STR.getBytes("UTF-8");

        byte[] splicedBuf = new byte[expected.length];
        System.arraycopy(buf, 0, splicedBuf, 0, splicedBuf.length);
        assertTrue(Arrays.equals(expected, splicedBuf));
    }

    /**
     * DeflaterInputStream#read(byte[], int, int)
     */
    public void testReadByteArrayIntInt() throws IOException {
        byte[] buf1 = new byte[256];
        byte[] buf2 = new byte[256];
        try (DeflaterInputStream dis = new DeflaterInputStream(is)) {
            assertEquals(23, dis.read(buf1, 0, 256));
        }

        try (DeflaterInputStream dis = new DeflaterInputStream(is)) {
            assertEquals(8, dis.read(buf2, 0, 256));
        }

        is = new ByteArrayInputStream(TEST_STR.getBytes("UTF-8"));
        DeflaterInputStream dis = new DeflaterInputStream(is);
        assertEquals(1, dis.available());
        assertEquals(120, dis.read());
        assertEquals(1, dis.available());
        assertEquals(22, dis.read(buf2, 0, 256));
        assertEquals(0, dis.available());
        assertEquals(-1, dis.read());
        assertEquals(0, dis.available());
        try {
            dis.read(buf1, 0, 512);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            dis.read(null, 0, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            dis.read(null, -1, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            dis.read(null, -1, -1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            dis.read(buf1, -1, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            dis.read(buf1, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        dis.close();
        try {
            dis.read(buf1, 0, 512);
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
        try {
            dis.read(buf1, 0, 1);
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
        try {
            dis.read(null, 0, 0);
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
        try {
            dis.read(null, -1, 0);
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
        try {
            dis.read(null, -1, -1);
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
        try {
            dis.read(buf1, -1, -1);
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
        try {
            dis.read(buf1, 0, -1);
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
    }

    /**
     * DeflaterInputStream#reset()
     */
    public void testReset() throws IOException {
        DeflaterInputStream dis = new DeflaterInputStream(is);
        try {
            dis.reset();
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
        dis.close();
        try {
            dis.reset();
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
    }

    /**
     * DeflaterInputStream#skip()
     */
    public void testSkip() throws IOException {
        byte[] buf = new byte[1024];
        try (DeflaterInputStream dis = new DeflaterInputStream(is)) {
            assertEquals(1, dis.available());
            dis.skip(1);
            assertEquals(1, dis.available());
            assertEquals(22, dis.read(buf, 0, 1024));
            assertEquals(0, dis.available());
            assertEquals(0, dis.available());
            is = new ByteArrayInputStream(TEST_STR.getBytes("UTF-8"));
        }

        is = new ByteArrayInputStream(TEST_STR.getBytes("UTF-8"));
        try (DeflaterInputStream dis = new DeflaterInputStream(is)) {
            assertEquals(23, dis.skip(Long.MAX_VALUE));
            assertEquals(0, dis.available());
        }

        DeflaterInputStream dis = new DeflaterInputStream(is);
        assertEquals(1, dis.available());
        dis.skip(56);
        assertEquals(0, dis.available());
        assertEquals(-1, dis.read(buf, 0, 1024));

        assertEquals(0, dis.available());
        // can still skip
        dis.skip(1);
        dis.close();
        try {
            dis.skip(1);
            fail("should throw IOException");
        } catch (IOException e) {
            // expected
        }
    }

    /**
     * DeflaterInputStream#DeflaterInputStream(InputStream)
     */
    /* J2ObjC removed: not supported by Junit 4.11 (https://github.com/google/j2objc/issues/1318).
    @DisableResourceLeakageDetection(
            why = "DeflaterInputStream does not clean up the default Deflater created in the"
                    + " constructor if the constructor fails; i.e. constructor calls"
                    + " this(..., new Deflater(), ...) and that constructor fails but does not know"
                    + " that it needs to call Deflater.end() as the caller has no access to it",
            bug = "31798154") */
    public void testDeflaterInputStreamInputStream() throws IOException {
        // ok
        new DeflaterInputStream(is).close();
        // fail
        try {
            new DeflaterInputStream(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * DataFormatException#DataFormatException()
     */
    public void testDataFormatException() {
        new DataFormatException();
    }

    /**
     * DeflaterInputStream#DeflaterInputStream(InputStream, Deflater)
     */
    public void testDeflaterInputStreamInputStreamDeflater() throws IOException {
        // ok
        Deflater deflater = new Deflater();
        try {
            new DeflaterInputStream(is, deflater).close();
            // fail
            try {
                new DeflaterInputStream(is, null);
                fail("should throw NullPointerException");
            } catch (NullPointerException e) {
                // expected
            }
            try {
                new DeflaterInputStream(null, deflater);
                fail("should throw NullPointerException");
            } catch (NullPointerException e) {
                // expected
            }
        } finally {
            deflater.end();
        }
    }

    /**
     * DeflaterInputStream#DeflaterInputStream(InputStream, Deflater, int)
     */
    public void testDeflaterInputStreamInputStreamDeflaterInt() {
        // ok
        Deflater deflater = new Deflater();
        try {
            new DeflaterInputStream(is, deflater, 1024);
            // fail
            try {
                new DeflaterInputStream(is, null, 1024);
                fail("should throw NullPointerException");
            } catch (NullPointerException e) {
                // expected
            }
            try {
                new DeflaterInputStream(null, deflater, 1024);
                fail("should throw NullPointerException");
            } catch (NullPointerException e) {
                // expected
            }
            try {
                new DeflaterInputStream(is, deflater, -1);
                fail("should throw IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                // expected
            }
            try {
                new DeflaterInputStream(null, deflater, -1);
                fail("should throw NullPointerException");
            } catch (NullPointerException e) {
                // expected
            }
            try {
                new DeflaterInputStream(is, null, -1);
                fail("should throw NullPointerException");
            } catch (NullPointerException e) {
                // expected
            }
        } finally {
            deflater.end();
        }
    }

    public static final class DeflaterInputStreamWithPublicBuffer extends DeflaterInputStream {

        public DeflaterInputStreamWithPublicBuffer(InputStream in) {
            super(in);
        }

        public byte[] getBuffer() {
            return buf;
        }
    }
}
