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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import junit.framework.TestCase;
import tests.support.resource.Support_Resources;

public class OldZipInputStreamTest extends TestCase {
    private ZipInputStream zis;
    private byte[] dataBytes = "Some data in my file".getBytes();

    @Override
    protected void setUp() throws IOException {
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
    }

    @Override
    protected void tearDown() throws IOException {
        if (zis != null) {
            zis.close();
        }
    }

    public void test_skipJ() throws Exception {
        File resources = Support_Resources.createTempFolder();
        Support_Resources.copyFile(resources, null, "Broken_manifest.jar");
        FileInputStream fis = new FileInputStream(new File(resources,
                "Broken_manifest.jar"));

        ZipInputStream zis1 = new ZipInputStream(fis);

        zis1.getNextEntry();
        zis1.getNextEntry();

        try {
            zis1.skip(10);
            fail("ZipException expected");
        } catch (ZipException ee) {
            // expected
        }

        try {
            zis1.close();  // Android throws exception here, already!
            zis1.skip(10);  // But RI here, only!
            fail("IOException expected");
        } catch (IOException ee) {
            // expected
        }
    }

    public void test_read$BII() throws Exception {
        byte[] rbuf;

        File resources = Support_Resources.createTempFolder();
        Support_Resources.copyFile(resources, null, "Broken_manifest.jar");
        FileInputStream fis = new FileInputStream(new File(resources,
                "Broken_manifest.jar"));

        ZipInputStream zis1 = new ZipInputStream(fis);

        zis1.getNextEntry();
        zis1.getNextEntry();

        rbuf = new byte[100];

        try {
            zis1.read(rbuf, 10, 90);
            fail("ZipException expected");
        } catch (ZipException ee) {
            // expected
        }

        try {
            zis1.close();  // Android throws exception here, already!
            zis1.read(rbuf, 10, 90);  // But RI here, only!
            fail("IOException expected");
        } catch (IOException ee) {
            // expected
        }
    }

    public void test_closeEntry() throws Exception {
        zis.getNextEntry();
        zis.closeEntry();
        zis.getNextEntry();
        zis.close();
        try {
            zis.closeEntry();
            fail("IOException expected");
        } catch (IOException ee) {
            // expected
        }
        File resources = Support_Resources.createTempFolder();
        Support_Resources.copyFile(resources, null, "Broken_manifest.jar");
        FileInputStream fis = new FileInputStream(new File(resources,
                "Broken_manifest.jar"));

        ZipInputStream zis1 = new ZipInputStream(fis);

        try {
            for (int i = 0; i < 6; i++) {
                zis1.getNextEntry();
                zis1.closeEntry();
            }
            fail("ZipException expected");
        } catch (ZipException ee) {
            // expected
        }
    }

    class Mock_ZipInputStream extends ZipInputStream {
        boolean createFlag = false;

        public Mock_ZipInputStream(InputStream arg0) {
            super(arg0);
        }

        boolean getCreateFlag() {
            return createFlag;
        }

        protected ZipEntry createZipEntry(String name) {
            createFlag = true;
            return super.createZipEntry(name);
        }
    }

    public void test_createZipEntryLjava_lang_String() throws Exception {

        File resources = Support_Resources.createTempFolder();
        Support_Resources.copyFile(resources, null, "Broken_manifest.jar");
        File fl = new File(resources, "Broken_manifest.jar");
        FileInputStream fis = new FileInputStream(fl);

        Mock_ZipInputStream zis1 = new Mock_ZipInputStream(fis);
        assertFalse(zis1.getCreateFlag());
        zis1.getNextEntry();
        assertTrue(zis1.getCreateFlag());
    }
}
