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
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import junit.framework.TestCase;
import tests.support.resource.Support_Resources;

public class InflaterInputStreamTest extends TestCase {

    // files hyts_construO,hyts_construOD,hyts_construODI needs to be
    // included as resources
    byte outPutBuf[] = new byte[500];

    class MyInflaterInputStream extends InflaterInputStream {
        MyInflaterInputStream(InputStream in) {
            super(in);
        }

        MyInflaterInputStream(InputStream in, Inflater infl) {
            super(in, infl);
        }

        MyInflaterInputStream(InputStream in, Inflater infl, int size) {
            super(in, infl, size);
        }

        void myFill() throws IOException {
            fill();
        }
    }

    /**
     * java.util.zip.InflaterInputStream#InflaterInputStream(java.io.InputStream)
     */
    public void test_ConstructorLjava_io_InputStream() throws IOException {
        //FIXME This test doesn't pass in Harmony classlib or Sun 5.0_7 RI
        /*
		int result = 0;
		int buffer[] = new int[500];
		InputStream infile = Support_Resources
				.getStream("hyts_construO.bin");

		InflaterInputStream inflatIP = new InflaterInputStream(infile);

		int i = 0;
		while ((result = inflatIP.read()) != -1) {
			buffer[i] = result;
			i++;
		}
		inflatIP.close();
        */
    }

    /**
     * java.util.zip.InflaterInputStream#InflaterInputStream(java.io.InputStream,
     *java.util.zip.Inflater)
     */
    public void test_ConstructorLjava_io_InputStreamLjava_util_zip_Inflater() throws IOException {
        byte byteArray[] = new byte[100];
        InputStream infile = Support_Resources.getStream("hyts_construOD.bin");
        Inflater inflate = new Inflater();
        InflaterInputStream inflatIP = new InflaterInputStream(infile,
                inflate);

        inflatIP.read(byteArray, 0, 5);// only suppose to read in 5 bytes
        inflatIP.close();
    }

    /**
     * java.util.zip.InflaterInputStream#InflaterInputStream(java.io.InputStream,
     *java.util.zip.Inflater, int)
     */
    public void test_ConstructorLjava_io_InputStreamLjava_util_zip_InflaterI() throws IOException {
        int result = 0;
        int buffer[] = new int[500];
        InputStream infile = Support_Resources.getStream("hyts_construODI.bin");
        Inflater inflate = new Inflater();
        InflaterInputStream inflatIP = new InflaterInputStream(infile,
                inflate, 1);

        int i = 0;
        while ((result = inflatIP.read()) != -1) {
            buffer[i] = result;
            i++;
        }
        inflatIP.close();
    }

    /**
     * java.util.zip.InflaterInputStream#InflaterInputStream(java.io.InputStream,
     *java.util.zip.Inflater, int)
     */
    public void test_ConstructorLjava_io_InputStreamLjava_util_zip_InflaterI_1() throws IOException {
        InputStream infile = Support_Resources.getStream("hyts_construODI.bin");
        Inflater inflate = new Inflater();
        InflaterInputStream inflatIP = null;
        try {
            inflatIP = new InflaterInputStream(infile, null, 1);
            fail("NullPointerException expected");
        } catch (NullPointerException NPE) {
            //expected
        }

        try {
            inflatIP = new InflaterInputStream(null, inflate, 1);
            fail("NullPointerException expected");
        } catch (NullPointerException NPE) {
            //expected
        }

        try {
            inflatIP = new InflaterInputStream(infile, inflate, -1);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException iae) {
            //expected
        }
    }

    /**
     * java.util.zip.InflaterInputStream#mark(int)
     */
    public void test_markI() {
        InputStream is = new ByteArrayInputStream(new byte[10]);
        InflaterInputStream iis = new InflaterInputStream(is);
        // mark do nothing, do no check
        iis.mark(0);
        iis.mark(-1);
        iis.mark(10000000);
    }

    /**
     * java.util.zip.InflaterInputStream#markSupported()
     */
    public void test_markSupported() {
        InputStream is = new ByteArrayInputStream(new byte[10]);
        InflaterInputStream iis = new InflaterInputStream(is);
        assertFalse(iis.markSupported());
        assertTrue(is.markSupported());
    }

    /**
     * java.util.zip.InflaterInputStream#read()
     */
    public void test_read() throws IOException {
        int result = 0;
        int buffer[] = new int[500];
        byte orgBuffer[] = { 1, 3, 4, 7, 8 };
        InputStream infile = Support_Resources
                .getStream("hyts_construOD.bin");
        Inflater inflate = new Inflater();
        InflaterInputStream inflatIP = new InflaterInputStream(infile,
                inflate);

        int i = 0;
        while ((result = inflatIP.read()) != -1) {
            buffer[i] = result;
            i++;
        }
        inflatIP.close();

        for (int j = 0; j < orgBuffer.length; j++) {
            assertEquals(
                    "original compressed data did not equal decompressed data",
                    orgBuffer[j], buffer[j]);
        }
    }

    /**
     * java.util.zip.InflaterInputStream#read(byte [], int, int)
     */
    public void test_read_LBII() throws IOException {
        int result = 0;
        InputStream infile = Support_Resources.getStream("hyts_construOD.bin");
        Inflater inflate = new Inflater();
        InflaterInputStream inflatIP = new InflaterInputStream(infile, inflate);

        byte[] b = new byte[3];
        try {
            result = inflatIP.read(null, 0, 1);
            fail("NullPointerException expected");
        } catch (NullPointerException npe) {
            //expected
        }

        assertEquals(0, inflatIP.read(b, 0, 0));

        try {
            result = inflatIP.read(b, 5, 2); //offset higher
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException iobe) {
            //expected
        }

        inflatIP.close();
        try {
            inflatIP.read(b, 0, 1); //read after close
            fail("IOException expected");
        } catch (IOException ioe) {
            //expected
        }
    }

    public void testAvailableNonEmptySource() throws Exception {
        // this byte[] is a deflation of these bytes: { 1, 3, 4, 6 }
        byte[] deflated = { 72, -119, 99, 100, 102, 97, 3, 0, 0, 31, 0, 15, 0 };
        InputStream in = new InflaterInputStream(new ByteArrayInputStream(deflated));
        // InflaterInputStream.available() returns either 1 or 0, even though
        // that contradicts the behavior defined in InputStream.available()
        assertEquals(1, in.read());
        assertEquals(1, in.available());
        assertEquals(3, in.read());
        assertEquals(1, in.available());
        assertEquals(4, in.read());
        assertEquals(1, in.available());
        assertEquals(6, in.read());
        assertEquals(0, in.available());
        assertEquals(-1, in.read());
        assertEquals(-1, in.read());
    }

    public void testAvailableSkip() throws Exception {
        // this byte[] is a deflation of these bytes: { 1, 3, 4, 6 }
        byte[] deflated = { 72, -119, 99, 100, 102, 97, 3, 0, 0, 31, 0, 15, 0 };
        InputStream in = new InflaterInputStream(new ByteArrayInputStream(deflated));
        assertEquals(1, in.available());
        assertEquals(4, in.skip(4));
        assertEquals(0, in.available());
    }

    public void testAvailableEmptySource() throws Exception {
        // this byte[] is a deflation of the empty file
        byte[] deflated = { 120, -100, 3, 0, 0, 0, 0, 1 };
        InputStream in = new InflaterInputStream(new ByteArrayInputStream(deflated));
        assertEquals(-1, in.read());
        assertEquals(-1, in.read());
        assertEquals(0, in.available());
    }

    /**
     * java.util.zip.InflaterInputStream#read(byte[], int, int)
     */
    public void test_read$BII() throws IOException {
        byte[] test = new byte[507];
        for (int i = 0; i < 256; i++) {
            test[i] = (byte) i;
        }
        for (int i = 256; i < test.length; i++) {
            test[i] = (byte) (256 - i);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream dos = new DeflaterOutputStream(baos);
        dos.write(test);
        dos.close();
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        InflaterInputStream iis = new InflaterInputStream(is);
        byte[] outBuf = new byte[530];
        int result = 0;
        while (true) {
            result = iis.read(outBuf, 0, 5);
            if (result == -1) {
                //"EOF was reached";
                break;
            }
        }
        try {
            iis.read(outBuf, -1, 10);
            fail("should throw IOOBE.");
        } catch (IndexOutOfBoundsException e) {
            // expected;
        }
    }

    public void test_read$BII2() throws IOException {
        File resources = Support_Resources.createTempFolder();
        Support_Resources.copyFile(resources, null, "Broken_manifest.jar");
        FileInputStream fis = new FileInputStream(new File(resources,
                "Broken_manifest.jar"));
        InflaterInputStream iis = new InflaterInputStream(fis);
        byte[] outBuf = new byte[530];

        iis.close();
        try {
            iis.read(outBuf, 0, 5);
            fail("IOException expected");
        } catch (IOException ee) {
            // expected.
        }
    }

    public void test_read$BII3() throws IOException {
        File resources = Support_Resources.createTempFolder();
        Support_Resources.copyFile(resources, null, "Broken_manifest.jar");
        FileInputStream fis = new FileInputStream(new File(resources,
                "Broken_manifest.jar"));
        InflaterInputStream iis = new InflaterInputStream(fis);
        byte[] outBuf = new byte[530];

        try {
            iis.read();
            fail("IOException expected.");
        } catch (IOException ee) {
            // expected
        }
    }

    /**
     * java.util.zip.InflaterInputStream#reset()
     */
    public void test_reset() {
        InputStream is = new ByteArrayInputStream(new byte[10]);
        InflaterInputStream iis = new InflaterInputStream(is);
        try {
            iis.reset();
            fail("Should throw IOException");
        } catch (IOException e) {
            // correct
        }
    }

    /**
     * java.util.zip.InflaterInputStream#skip(long)
     */
    public void test_skipJ() throws IOException {
        InputStream is = Support_Resources.getStream("hyts_available.tst");
        InflaterInputStream iis = new InflaterInputStream(is);

        // Tests for skipping a negative number of bytes.
        try {
            iis.skip(-3);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        assertEquals("Incorrect Byte Returned.", 5, iis.read());

        try {
            iis.skip(Integer.MIN_VALUE);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        assertEquals("Incorrect Byte Returned.", 4, iis.read());

        // Test to make sure the correct number of bytes were skipped
        assertEquals("Incorrect Number Of Bytes Skipped.", 3, iis.skip(3));

        // Test to see if the number of bytes skipped returned is true.
        assertEquals("Incorrect Byte Returned.", 7, iis.read());

        assertEquals("Incorrect Number Of Bytes Skipped.", 0, iis.skip(0));
        assertEquals("Incorrect Byte Returned.", 0, iis.read());

        // Test for skipping more bytes than available in the stream
        assertEquals("Incorrect Number Of Bytes Skipped.", 2, iis.skip(4));
        assertEquals("Incorrect Byte Returned.", -1, iis.read());
        iis.close();
    }

    /**
     * java.util.zip.InflaterInputStream#skip(long)
     */
    public void test_skipJ2() throws IOException {
        int result = 0;
        int buffer[] = new int[100];
        byte orgBuffer[] = { 1, 3, 4, 7, 8 };

        // testing for negative input to skip
        InputStream infile = Support_Resources
                .getStream("hyts_construOD.bin");
        Inflater inflate = new Inflater();
        InflaterInputStream inflatIP = new InflaterInputStream(infile,
                inflate, 10);
        long skip;
        try {
            skip = inflatIP.skip(Integer.MIN_VALUE);
            fail("Expected IllegalArgumentException when skip() is called with negative parameter");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        inflatIP.close();

        // testing for number of bytes greater than input.
        InputStream infile2 = Support_Resources
                .getStream("hyts_construOD.bin");
        InflaterInputStream inflatIP2 = new InflaterInputStream(infile2);

        // looked at how many bytes the skip skipped. It is
        // 5 and its supposed to be the entire input stream.

        skip = inflatIP2.skip(Integer.MAX_VALUE);
        // System.out.println(skip);
        assertEquals("method skip() returned wrong number of bytes skipped",
                5, skip);

        // test for skipping of 2 bytes
        InputStream infile3 = Support_Resources
                .getStream("hyts_construOD.bin");
        InflaterInputStream inflatIP3 = new InflaterInputStream(infile3);
        skip = inflatIP3.skip(2);
        assertEquals("the number of bytes returned by skip did not correspond with its input parameters",
                2, skip);
        int i = 0;
        result = 0;
        while ((result = inflatIP3.read()) != -1) {
            buffer[i] = result;
            i++;
        }
        inflatIP2.close();

        for (int j = 2; j < orgBuffer.length; j++) {
            assertEquals(
                    "original compressed data did not equal decompressed data",
                    orgBuffer[j], buffer[j - 2]);
        }
    }

    /**
     * java.util.zip.InflaterInputStream#available()
     */
    public void test_available() throws IOException {
        InputStream is = Support_Resources.getStream("hyts_available.tst");
        InflaterInputStream iis = new InflaterInputStream(is);

        int available;
        for (int i = 0; i < 11; i++) {
            iis.read();
            available = iis.available();
            if (available == 0) {
                assertEquals("Expected no more bytes to read", -1, iis.read());
            } else {
                assertEquals("Bytes Available Should Return 1.", 1, available);
            }
        }

        iis.close();
        try {
            iis.available();
            fail("available after close should throw IOException.");
        } catch (IOException e) {
            // Expected
        }
    }

    /**
     * java.util.zip.InflaterInputStream#close()
     */
    public void test_close() throws IOException {
        InflaterInputStream iin = new InflaterInputStream(
                new ByteArrayInputStream(new byte[0]));
        iin.close();

        // test for exception
        iin.close();
    }

    // http://b/26462400
    public void testInflaterInputStreamWithExternalInflater() throws Exception {
        InputStream base = new ByteArrayInputStream(new byte[] { 'h', 'i'});
        Inflater inf = new Inflater();

        InflaterInputStream iis = new InflaterInputStream(base, inf, 512);
        iis.close();
        try {
            inf.reset();
            fail();
        } catch (IllegalStateException espected) {
            // Expected because the inflater should've been closed when the stream was.
        }

        inf = new Inflater();
        iis = new InflaterInputStream(base, inf);
        iis.close();
        try {
            inf.reset();
            fail();
        } catch (IllegalStateException espected) {
            // Expected because the inflater should've been closed when the stream was.
        }
    }
}
