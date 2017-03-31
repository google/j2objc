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

package libcore.java.util.zip;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import tests.support.resource.Support_Resources;

public class OldZipFileTest extends junit.framework.TestCase {

    public byte[] getAllBytesFromStream(InputStream is) throws IOException {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        byte[] buf = new byte[512];
        int iRead;
        while ((iRead = is.read(buf, 0, buf.length)) != -1) {
            bs.write(buf, 0, iRead);
        }
        return bs.toByteArray();
    }

    public void test_size() throws IOException {
        zfile.close();
        try {
            zfile.size();
            fail("IllegalStateException expected");
        } catch (IllegalStateException expected) {
        }
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

    // the file hyts_zipFile.zip in setup must be included as a resource
    private String tempFileName;

    private ZipFile zfile;

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

        zentry = zfile.getEntry("File2.txt");
        zfile.close();
        try {
            is = zfile.getInputStream(zentry);
            fail("IllegalStateException expected");
        } catch (IllegalStateException ee) {
            // expected
        }
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    @Override
    protected void setUp() throws IOException {
        // Create a local copy of the file since some tests want to alter information.
        File tempFile = File.createTempFile("OldZipFileTest", "zip");
        tempFileName = tempFile.getAbsolutePath();
        InputStream is = Support_Resources.getStream("hyts_ZipFile.zip");
        FileOutputStream fos = new FileOutputStream(tempFile);
        byte[] rbuf = getAllBytesFromStream(is);
        fos.write(rbuf, 0, rbuf.length);
        is.close();
        fos.close();
        zfile = new ZipFile(tempFile);
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    @Override
    protected void tearDown() throws IOException {
        // Note zfile is a user-defined zip file used by other tests and
        // should not be deleted
        zfile.close();
    }
}
