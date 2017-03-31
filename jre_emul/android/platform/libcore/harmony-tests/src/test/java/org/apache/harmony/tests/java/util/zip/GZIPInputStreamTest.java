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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.Checksum;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import tests.support.resource.Support_Resources;

public class GZIPInputStreamTest extends junit.framework.TestCase {
    File resources;

    class TestGZIPInputStream extends GZIPInputStream {
        TestGZIPInputStream(InputStream in) throws IOException {
            super(in);
        }

        TestGZIPInputStream(InputStream in, int size) throws IOException {
            super(in, size);
        }

        Checksum getChecksum() {
            return crc;
        }

        boolean endofInput() {
            return eos;
        }
    }

    /**
     * @tests java.util.zip.GZIPInputStream#GZIPInputStream(java.io.InputStream)
     */
    public void test_ConstructorLjava_io_InputStream() {
        // test method java.util.zip.GZIPInputStream.constructor
        try {
            Support_Resources.copyFile(resources, null, "hyts_gInput.txt.gz");
            final URL gInput = new File(resources.toString() + "/hyts_gInput.txt.gz").toURL();
            TestGZIPInputStream inGZIP = new TestGZIPInputStream(gInput
                    .openConnection().getInputStream());
            assertNotNull("the constructor for GZIPInputStream is null",
                    inGZIP);
            assertEquals("the CRC value of the inputStream is not zero", 0, inGZIP
                    .getChecksum().getValue());
            inGZIP.close();
        } catch (IOException e) {
            fail(
                    "an IO error occured while trying to open the input file");
        }
    }

    /**
     * @tests java.util.zip.GZIPInputStream#GZIPInputStream(java.io.InputStream,
     *int)
     */
    public void test_ConstructorLjava_io_InputStreamI() {
        // test method java.util.zip.GZIPInputStream.constructorI
        try {
            Support_Resources.copyFile(resources, null, "hyts_gInput.txt.gz");
            final URL gInput = new File(resources.toString() + "/hyts_gInput.txt.gz").toURL();
            TestGZIPInputStream inGZIP = new TestGZIPInputStream(gInput
                    .openConnection().getInputStream(), 200);
            assertNotNull("the constructor for GZIPInputStream is null",
                    inGZIP);
            assertEquals("the CRC value of the inputStream is not zero", 0, inGZIP
                    .getChecksum().getValue());
            inGZIP.close();

            try {
                TestGZIPInputStream inGZIP1 = new TestGZIPInputStream(gInput
                        .openConnection().getInputStream(), 0);
                fail("IllegalArgumentException expected");
            } catch (IllegalArgumentException ioe) {
                //expected
            }

            Support_Resources.copyFile(resources, null, "hyts_checkInput.txt");
            final URL jarInput = new File(resources.toString() + "/hyts_checkInput.txt").toURL();
            try {
                TestGZIPInputStream inGZIP1 = new TestGZIPInputStream(jarInput
                        .openConnection().getInputStream(), 200);
                fail("IOException expected");
            } catch (IOException ex) {
                //expected
            }

        } catch (IOException e) {
            fail(
                    "an IO error occured while trying to open the input file");
        }
    }

    /**
     * @tests java.util.zip.GZIPInputStream#read(byte[], int, int)
     */
    public void test_read$BII() throws IOException {
        // test method java.util.zip.GZIPInputStream.readBII
        byte orgBuf[] = { '3', '5', '2', 'r', 'g', 'e', 'f', 'd', 'e', 'w' };
        byte outBuf[] = new byte[100];
        int result = 0;
        Support_Resources.copyFile(resources, null, "hyts_gInput.txt.gz");
        final URL gInput = new File(resources.toString() + "/hyts_gInput.txt.gz").toURL();
        TestGZIPInputStream inGZIP = new TestGZIPInputStream(gInput
                .openConnection().getInputStream());
        while (!(inGZIP.endofInput())) {
            result += inGZIP.read(outBuf, result, outBuf.length - result);
        }
        assertEquals(
                "the checkSum value of the compressed and decompressed data does not equal",
                2074883667L, inGZIP.getChecksum().getValue());
        for (int i = 0; i < orgBuf.length; i++) {
            assertEquals(
                    "the decompressed data does not equal the original data decompressed",
                    orgBuf[i], outBuf[i]);
            // System.out.println(orgBuf[i] + " " + outBuf[i]);
        }
        int r = 0;
        try {
            inGZIP.read(outBuf, 100, 1);
        } catch (IndexOutOfBoundsException e) {
            r = 1;
        }
        inGZIP.close();
        // line below fails on RI also, comment out.
        // assertEquals("Boundary Check was not present", 1, r);

        // Create compressed data which is exactly 512 bytes (after the
        // header),
        // the size of the InflaterStream internal buffer
        byte[] test = new byte[507];
        for (int i = 0; i < 256; i++) {
            test[i] = (byte) i;
        }
        for (int i = 256; i < test.length; i++) {
            test[i] = (byte) (256 - i);
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        GZIPOutputStream out = new GZIPOutputStream(bout);
        out.write(test);
        out.close();
        byte[] comp = bout.toByteArray();
        GZIPInputStream gin2 = new GZIPInputStream(new ByteArrayInputStream(
                comp), 512);
        int total = 0;
        while ((result = gin2.read(test)) != -1) {
            total += result;
        }
        assertEquals("Should return -1", -1, gin2.read());
        gin2.close();
        assertEquals("Incorrectly decompressed", test.length, total);

        gin2 = new GZIPInputStream(new ByteArrayInputStream(comp), 512);
        total = 0;
        while ((result = gin2.read(new byte[200])) != -1) {
            total += result;
        }
        assertEquals("Should return -1", -1, gin2.read());
        gin2.close();
        assertEquals("Incorrectly decompressed", test.length, total);

        gin2 = new GZIPInputStream(new ByteArrayInputStream(comp), 516);
        total = 0;
        while ((result = gin2.read(new byte[200])) != -1) {
            total += result;
        }
        assertEquals("Should return -1", -1, gin2.read());
        gin2.close();
        assertEquals("Incorrectly decompressed", test.length, total);

        comp[40] = 0;
        gin2 = new GZIPInputStream(new ByteArrayInputStream(comp), 512);
        boolean exception = false;
        try {
            while (gin2.read(test) != -1) {
                ;
            }
        } catch (IOException e) {
            exception = true;
        }
        assertTrue("Exception expected", exception);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream zipout = new GZIPOutputStream(baos);
        zipout.write(test);
        zipout.close();
        outBuf = new byte[530];
        GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(baos.toByteArray()));
        try {
            in.read(outBuf, 530, 1);
            fail("Test failed IOOBE was not thrown");
        } catch (IndexOutOfBoundsException e) {
        }
        while (true) {
            result = in.read(outBuf, 0, 5);
            if (result == -1) {
                //"EOF was reached";
                break;
            }
        }
        result = -10;
        result = in.read(null, 100, 1);
        result = in.read(outBuf, -100, 1);
        result = in.read(outBuf, -1, 1);// 100, 1);
    }

    /**
     * @tests java.util.zip.GZIPInputStream#close()
     */
    public void test_close() {
        // test method java.util.zip.GZIPInputStream.close
        byte outBuf[] = new byte[100];
        try {
            int result = 0;
            Support_Resources.copyFile(resources, null, "hyts_gInput.txt.gz");
            final URL gInput = new File(resources.toString() + "/hyts_gInput.txt.gz").toURL();
            TestGZIPInputStream inGZIP = new TestGZIPInputStream(gInput
                    .openConnection().getInputStream());
            while (!(inGZIP.endofInput())) {
                result += inGZIP.read(outBuf, result, outBuf.length - result);
            }
            assertEquals("the checkSum value of the compressed and decompressed data does not equal",
                    2074883667L, inGZIP.getChecksum().getValue());
            inGZIP.close();
            int r = 0;
            try {
                inGZIP.read(outBuf, 0, 1);
            } catch (IOException e) {
                r = 1;
            }
            assertEquals("GZIPInputStream can still be used after close is called",
                    1, r);
        } catch (IOException e) {
            e.printStackTrace();
            fail("unexpected: " + e);
        }
    }

    /**
     * Regression test for HARMONY-3703.
     *
     * @tests java.util.zip.GZIPInputStream#read()
     */
    public void test_read() throws IOException {
        GZIPInputStream gis = null;
        int result = 0;
        byte[] buffer = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        File f = new File(resources.getAbsolutePath() + "test.gz");
        FileOutputStream out = new FileOutputStream(f);
        GZIPOutputStream gout = new GZIPOutputStream(out);

        // write 100 bytes to the stream
        for (int i = 0; i < 10; i++) {
            gout.write(buffer);
        }
        gout.finish();
        out.write(1);
        out.close();

        gis = new GZIPInputStream(new FileInputStream(f));
        buffer = new byte[100];
        gis.read(buffer);
        result = gis.read();
        gis.close();
        f.delete();

        assertEquals("Incorrect value returned at the end of the file", -1, result);
    }

    @Override
    protected void setUp() {
        resources = Support_Resources.createTempFolder();
    }

    @Override
    protected void tearDown() {
    }

}
