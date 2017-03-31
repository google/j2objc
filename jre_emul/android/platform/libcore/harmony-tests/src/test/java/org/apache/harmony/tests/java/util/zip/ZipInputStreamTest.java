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
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import junit.framework.TestCase;
import tests.support.resource.Support_Resources;

public class ZipInputStreamTest extends TestCase {
    // the file hyts_zipFile.zip used in setup needs to included as a resource
    private ZipEntry zentry;

    private ZipInputStream zis;

    private byte[] zipBytes;

    private byte[] dataBytes = "Some data in my file".getBytes();

    @Override
    protected void setUp() {
        try {
            InputStream is = Support_Resources.getStream("hyts_ZipFile.zip");
            if (is == null) {
                System.out.println("file hyts_ZipFile.zip can not be found");
            }
            zis = new ZipInputStream(is);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(bos);
            ZipEntry entry = new ZipEntry("myFile");
            zos.putNextEntry(entry);
            zos.write(dataBytes);
            zos.closeEntry();
            zos.close();
            zipBytes = bos.toByteArray();
        } catch (Exception e) {
            System.out.println("Exception during ZipFile setup:");
            e.printStackTrace();
        }
    }

    @Override
    protected void tearDown() {
        if (zis != null) {
            try {
                zis.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * java.util.zip.ZipInputStream#ZipInputStream(java.io.InputStream)
     */
    public void test_ConstructorLjava_io_InputStream() throws Exception {
        zentry = zis.getNextEntry();
        zis.closeEntry();
    }

    /**
     * java.util.zip.ZipInputStream#close()
     */
    public void test_close() {
        try {
            zis.close();
            byte[] rbuf = new byte[10];
            zis.read(rbuf, 0, 1);
        } catch (IOException e) {
            return;
        }
        fail("Read data after stream was closed");
    }

    /**
     * java.util.zip.ZipInputStream#close()
     */
    public void test_close2() throws Exception {
        // Regression for HARMONY-1101
        zis.close();
        // another call to close should NOT cause an exception
        zis.close();
    }

    /**
     * java.util.zip.ZipInputStream#closeEntry()
     */
    public void test_closeEntry() throws Exception {
        zentry = zis.getNextEntry();
        zis.closeEntry();
    }

    public void test_closeAfterException() throws Exception {
        File resources = Support_Resources.createTempFolder();
        Support_Resources.copyFile(resources, null, "Broken_manifest.jar");
        FileInputStream fis = new FileInputStream(new File(resources,
                "Broken_manifest.jar"));

        ZipInputStream zis1 = new ZipInputStream(fis);

        try {
            for (int i = 0; i < 6; i++) {
                zis1.getNextEntry();
            }
            fail("ZipException expected");
        } catch (ZipException ee) {
            // expected
        }

        zis1.close();
        try {
            zis1.getNextEntry();
            fail("IOException expected");
        } catch (IOException ee) {
            // expected
        }
    }

    /**
     * java.util.zip.ZipInputStream#getNextEntry()
     */
    public void test_getNextEntry() throws Exception {
        assertNotNull("getNextEntry failed", zis.getNextEntry());
    }

    /**
     * java.util.zip.ZipInputStream#read(byte[], int, int)
     */
    public void test_read$BII() throws Exception {
        zentry = zis.getNextEntry();
        byte[] rbuf = new byte[(int) zentry.getSize()];
        int r = zis.read(rbuf, 0, rbuf.length);
        new String(rbuf, 0, r);
        assertEquals("Failed to read entry", 12, r);
    }

    public void testReadOneByteAtATime() throws IOException {
        InputStream in = new FilterInputStream(Support_Resources.getStream("hyts_ZipFile.zip")) {
            @Override
            public int read(byte[] buffer, int offset, int count) throws IOException {
                return super.read(buffer, offset, 1); // one byte at a time
            }

            @Override
            public int read(byte[] buffer) throws IOException {
                return super.read(buffer, 0, 1); // one byte at a time
            }
        };

        zis = new ZipInputStream(in);
        while ((zentry = zis.getNextEntry()) != null) {
            zentry.getName();
        }
        zis.close();
    }

    /**
     * java.util.zip.ZipInputStream#skip(long)
     */
    public void test_skipJ() throws Exception {
        zentry = zis.getNextEntry();
        byte[] rbuf = new byte[(int) zentry.getSize()];
        zis.skip(2);
        int r = zis.read(rbuf, 0, rbuf.length);
        assertEquals("Failed to skip data", 10, r);

        zentry = zis.getNextEntry();
        zentry = zis.getNextEntry();
        long s = zis.skip(1025);
        assertEquals("invalid skip: " + s, 1025, s);

        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes));
        zis.getNextEntry();
        long skipLen = dataBytes.length / 2;
        assertEquals("Assert 0: failed valid skip", skipLen, zis.skip(skipLen));
        zis.skip(dataBytes.length);
        assertEquals("Assert 1: performed invalid skip", 0, zis.skip(1));
        assertEquals("Assert 2: failed zero len skip", 0, zis.skip(0));
        try {
            zis.skip(-1);
            fail("Assert 3: Expected Illegal argument exception");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    public void test_available() throws Exception {

        File resources = Support_Resources.createTempFolder();
        Support_Resources.copyFile(resources, null, "hyts_ZipFile.zip");
        File fl = new File(resources, "hyts_ZipFile.zip");
        FileInputStream fis = new FileInputStream(fl);

        ZipInputStream zis1 = new ZipInputStream(fis);
        ZipEntry entry = zis1.getNextEntry();
        assertNotNull("No entry in the archive.", entry);
        long entrySize = entry.getSize();
        assertTrue("Entry size was < 1", entrySize > 0);
        int i = 0;
        while (zis1.available() > 0) {
            zis1.skip(1);
            i++;
        }
        if (i != entrySize) {
            fail("ZipInputStream.available or ZipInputStream.skip does not " +
                    "working properly. Only skipped " + i +
                    " bytes instead of " + entrySize);
        }
        assertEquals(0, zis1.skip(1));
        assertEquals(0, zis1.available());
        zis1.closeEntry();
        assertEquals(1, zis.available());
        zis1.close();
        try {
            zis1.available();
            fail("IOException expected");
        } catch (IOException ee) {
            // expected
        }
    }
}
