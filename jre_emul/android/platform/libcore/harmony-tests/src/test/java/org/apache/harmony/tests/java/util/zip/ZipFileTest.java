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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import libcore.io.Streams;
//import libcore.java.lang.ref.FinalizationTester;
import tests.support.resource.Support_Resources;

public class ZipFileTest extends junit.framework.TestCase {

    // the file hyts_zipFile.zip in setup must be included as a resource
    private String tempFileName;
    private ZipFile zfile;

    /**
     * java.util.zip.ZipFile#ZipFile(java.io.File, int)
     */
    public void test_ConstructorLjava_io_FileI() throws IOException {
        zfile.close(); // about to reopen the same temp file

        File file = new File(tempFileName);
        ZipFile zip = new ZipFile(file, ZipFile.OPEN_DELETE | ZipFile.OPEN_READ);
        zip.close();
        assertTrue("Zip should not exist", !file.exists());

        file = new File(tempFileName);
        try {
            zip = new ZipFile(file, ZipFile.OPEN_READ);
            fail("IOException expected");
        } catch (IOException ee) {
            // expected
        }
        file = new File(tempFileName);
        try {
            zip = new ZipFile(file, -1);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ee) {
            // expected
        }
    }

    /**
     * @throws IOException
     * java.util.zip.ZipFile#ZipFile(java.lang.String)
     */
    public void test_ConstructorLjava_lang_String() throws IOException {
        zfile.close(); // about to reopen the same temp file
        ZipFile zip = new ZipFile(tempFileName);
        zip.close();
        File file = File.createTempFile("zip", "tmp");
        try {
            zip = new ZipFile(file.getAbsolutePath());
            fail("ZipException expected");
        } catch (ZipException ee) {
            // expected
        }
        file.delete();
    }

    protected ZipEntry test_finalize1(ZipFile zip) {
        return zip.getEntry("File1.txt");
    }

    protected ZipFile test_finalize2(File file) throws IOException {
        return new ZipFile(file);
    }

    /**
     * java.util.zip.ZipFile#finalize()
     */
    // J2ObjC: Finalization works a bit differently.
//    public void test_finalize() throws IOException {
//        InputStream in = Support_Resources.getStream("hyts_ZipFile.zip");
//        File file = Support_Resources.createTempFile(".jar");
//        OutputStream out = new FileOutputStream(file);
//        int result;
//        byte[] buf = new byte[4096];
//        while ((result = in.read(buf)) != -1) {
//            out.write(buf, 0, result);
//        }
//        in.close();
//        out.close();
//        /*
//         * ZipFile zip = new ZipFile(file); ZipEntry entry1 =
//         * zip.getEntry("File1.txt"); assertNotNull("Did not find entry",
//         * entry1); entry1 = null; zip = null;
//         */
//
//        assertNotNull("Did not find entry", test_finalize1(test_finalize2(file)));
//        FinalizationTester.induceFinalization();
//        file.delete();
//        assertTrue("Zip should not exist", !file.exists());
//    }

    /**
     * @throws IOException
     * java.util.zip.ZipFile#close()
     */
    public void test_close() throws IOException {
        // Test for method void java.util.zip.ZipFile.close()
        File fl = new File(tempFileName);
        ZipFile zf = new ZipFile(fl);
        InputStream is1 = zf.getInputStream(zf.getEntry("File1.txt"));
        InputStream is2 = zf.getInputStream(zf.getEntry("File2.txt"));

        is1.read();
        is2.read();

        zf.close();

        try {
            is1.read();
            fail("IOException expected");
        } catch (IOException ee) {
            // expected
        }

        try {
            is2.read();
            fail("IOException expected");
        } catch (IOException ee) {
            // expected
        }
    }

    /**
     * java.util.zip.ZipFile#entries()
     */
    public void test_entries() throws Exception {
        // Test for method java.util.Enumeration java.util.zip.ZipFile.entries()
        Enumeration<? extends ZipEntry> enumer = zfile.entries();
        int c = 0;
        while (enumer.hasMoreElements()) {
            ++c;
            enumer.nextElement();
        }
        assertTrue("Incorrect number of entries returned: " + c, c == 6);

        Enumeration<? extends ZipEntry> enumeration = zfile.entries();
        zfile.close();
        try {
            enumeration.nextElement();
            fail("did not detect closed file");
        } catch (IllegalStateException expected) {
        }

        try {
            enumeration.hasMoreElements();
            fail("did not detect closed file");
        } catch (IllegalStateException expected) {
        }

        try {
            zfile.entries();
            fail("did not detect closed file");
        } catch (IllegalStateException expected) {
        }
    }

    /**
     * java.util.zip.ZipFile#getEntry(java.lang.String)
     */
    public void test_getEntryLjava_lang_String() throws IOException {
        // Test for method java.util.zip.ZipEntry
        // java.util.zip.ZipFile.getEntry(java.lang.String)
        java.util.zip.ZipEntry zentry = zfile.getEntry("File1.txt");
        assertNotNull("Could not obtain ZipEntry", zentry);
        int r;
        InputStream in;

        zentry = zfile.getEntry("testdir1/File1.txt");
        assertNotNull("Could not obtain ZipEntry: testdir1/File1.txt", zentry);
        zentry = zfile.getEntry("testdir1/");
        assertNotNull("Could not obtain ZipEntry: testdir1/", zentry);
        in = zfile.getInputStream(zentry);
        assertNotNull("testdir1/ should not have null input stream", in);
        r = in.read();
        in.close();
        assertEquals("testdir1/ should not contain data", -1, r);

        zentry = zfile.getEntry("testdir1/testdir1");
        assertNotNull("Could not obtain ZipEntry: testdir1/testdir1", zentry);
        in = zfile.getInputStream(zentry);
        byte[] buf = new byte[256];
        r = in.read(buf);
        in.close();
        assertEquals("incorrect contents", "This is also text", new String(buf,
                0, r));
    }

    public void test_getEntryLjava_lang_String_AndroidOnly() throws IOException {
        java.util.zip.ZipEntry zentry = zfile.getEntry("File1.txt");
        assertNotNull("Could not obtain ZipEntry", zentry);
        int r;
        InputStream in;

        zentry = zfile.getEntry("testdir1");
        assertNotNull("Must be able to obtain ZipEntry: testdir1", zentry);
        in = zfile.getInputStream(zentry);
        /*
         * Android delivers empty InputStream, RI no InputStream at all. The
         * spec doesn't clarify this, so we need to deal with both situations.
         */
        int data = -1;
        if (in != null) {
            data = in.read();
            in.close();
        }
        assertEquals("Must not be able to read directory data", -1, data);
    }

    public void test_getEntryLjava_lang_String_Ex() throws IOException {
        java.util.zip.ZipEntry zentry = zfile.getEntry("File1.txt");
        assertNotNull("Could not obtain ZipEntry", zentry);

        zfile.close();
        try {
            zfile.getEntry("File2.txt");
            fail("IllegalStateException expected");
        } catch (IllegalStateException ee) {
        }
    }

    /**
     * @throws IOException
     * java.util.zip.ZipFile#getInputStream(java.util.zip.ZipEntry)
     */
    public void test_getInputStreamLjava_util_zip_ZipEntry() throws IOException {
        // Test for method java.io.InputStream
        // java.util.zip.ZipFile.getInputStream(java.util.zip.ZipEntry)
        ZipEntry zentry = null;
        InputStream is = null;
        try {
            zentry = zfile.getEntry("File1.txt");
            is = zfile.getInputStream(zentry);
            byte[] rbuf = new byte[1000];
            int r;
            is.read(rbuf, 0, r = (int) zentry.getSize());
            assertEquals("getInputStream read incorrect data", "This is text",
                    new String(rbuf, 0, r));
        } catch (java.io.IOException e) {
            fail("IOException during getInputStream");
        } finally {
            try {
                is.close();
            } catch (java.io.IOException e) {
                fail("Failed to close input stream");
            }
        }

        zentry = zfile.getEntry("File2.txt");
        zfile.close();
        try {
            is = zfile.getInputStream(zentry);
            fail("IllegalStateException expected");
        } catch (IllegalStateException ee) {
            // expected
        }

        // ZipException can not be checked. Stream object returned or null.
    }

    /**
     * java.util.zip.ZipFile#getName()
     */
    public void test_getName() {
        // Test for method java.lang.String java.util.zip.ZipFile.getName()
        assertTrue("Returned incorrect name: " + zfile.getName(), zfile
                .getName().equals(tempFileName));
    }

    /**
     * @throws IOException
     * java.util.zip.ZipFile#size()
     */
    public void test_size() throws IOException {
        assertEquals(6, zfile.size());
        zfile.close();
        try {
            zfile.size();
            fail("IllegalStateException expected");
        } catch (IllegalStateException expected) {
        }
    }

    /**
     * java.io.InputStream#reset()
     */
    public void test_reset() throws IOException {
        // read an uncompressed entry
        ZipEntry zentry = zfile.getEntry("File1.txt");
        InputStream is = zfile.getInputStream(zentry);
        byte[] rbuf1 = new byte[6];
        byte[] rbuf2 = new byte[6];
        int r1, r2;
        r1 = is.read(rbuf1);
        assertEquals(rbuf1.length, r1);
        r2 = is.read(rbuf2);
        assertEquals(rbuf2.length, r2);

        try {
            is.reset();
            fail();
        } catch (IOException expected) {
        }
        is.close();

        // read a compressed entry
        byte[] rbuf3 = new byte[4185];
        ZipEntry zentry2 = zfile.getEntry("File3.txt");
        is = zfile.getInputStream(zentry2);
        r1 = is.read(rbuf3);
        assertEquals(4183, r1);
        try {
            is.reset();
            fail();
        } catch (IOException expected) {
        }
        is.close();

        is = zfile.getInputStream(zentry2);
        r1 = is.read(rbuf3, 0, 3000);
        assertEquals(3000, r1);
        try {
            is.reset();
            fail();
        } catch (IOException expected) {
        }
        is.close();
    }

    /**
     * java.io.InputStream#reset()
     */
    public void test_reset_subtest0() throws IOException {
        // read an uncompressed entry
        ZipEntry zentry = zfile.getEntry("File1.txt");
        InputStream is = zfile.getInputStream(zentry);
        byte[] rbuf1 = new byte[12];
        byte[] rbuf2 = new byte[12];
        int r = is.read(rbuf1, 0, 4);
        assertEquals(4, r);
        is.mark(0);
        r = is.read(rbuf1);
        assertEquals(8, r);
        assertEquals(-1, is.read());

        try {
            is.reset();
            fail();
        } catch (IOException expected) {
        }

        is.close();

        // read a compressed entry
        byte[] rbuf3 = new byte[4185];
        ZipEntry zentry2 = zfile.getEntry("File3.txt");
        is = zfile.getInputStream(zentry2);
        r = is.read(rbuf3, 0, 3000);
        assertEquals(3000, r);
        is.mark(0);
        r = is.read(rbuf3);
        assertEquals(1183, r);
        assertEquals(-1, is.read());

        try {
            is.reset();
            fail();
        } catch (IOException expected) {
        }

        is.close();
    }

    @Override
    protected void setUp() throws IOException {
        // Create a local copy of the file since some tests want to alter information.
        File tempFile = File.createTempFile("OldZipFileTest", "zip");
        tempFileName = tempFile.getAbsolutePath();


        InputStream is = Support_Resources.getStream("hyts_ZipFile.zip");
        FileOutputStream fos = new FileOutputStream(tempFile);
        Streams.copy(is, fos);

        is.close();
        fos.close();
        zfile = new ZipFile(tempFile);
    }

    @Override
    protected void tearDown() throws IOException {
        if (zfile != null) {
            zfile.close();
        }
    }
}
