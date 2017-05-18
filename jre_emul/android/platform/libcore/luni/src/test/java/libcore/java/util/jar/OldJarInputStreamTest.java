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

package libcore.java.util.jar;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import tests.support.resource.Support_Resources;

public class OldJarInputStreamTest extends junit.framework.TestCase {

    public void test_ConstructorLjava_io_InputStreamZ() {
        try {
            // we need a buffered stream because ByteArrayInputStream.close() is a no-op
            InputStream is = new BufferedInputStream(new ByteArrayInputStream(new byte[0]));
            is.close();
            new JarInputStream(is, false);
            fail("IOException expected");
        } catch (IOException ee) {
            // expected
        }
    }


    class Mock_JarInputStream extends JarInputStream {

        public Mock_JarInputStream(InputStream in) throws IOException {
            super(in);
        }

        public ZipEntry createZipEntry(String str) {
            return super.createZipEntry(str);
        }
    }

    public void test_createZipEntryLjava_lang_String() throws Exception {
        File resources = Support_Resources.createTempFolder();
        Support_Resources.copyFile(resources, null, "Broken_entry.jar");
        InputStream is = Support_Resources.getStream("Broken_entry.jar");
        Mock_JarInputStream mjis = new Mock_JarInputStream(is);
        assertNotNull(mjis.createZipEntry("New entry"));
    }

    public void test_read$ZII() throws Exception {
        File resources = Support_Resources.createTempFolder();
        Support_Resources.copyFile(resources, null, "Broken_entry_data.jar");
        InputStream is = Support_Resources.getStream("Broken_entry_data.jar");
        JarInputStream jis = new JarInputStream(is, true);
        byte b[] = new byte[100];

        jis.getNextEntry();
        jis.read(b, 0, 100);
        jis.getNextEntry();
        jis.getNextEntry();
        jis.getNextEntry();

        try {
            jis.read(b, 0, 100);
            fail("ZipException expected");
        } catch (ZipException ee) {
            // expected
        }

        try {
            jis.close();  // Android throws exception here, already!
            jis.read(b, 0, 100);  // But RI here, only!
            fail("IOException expected");
        } catch (IOException ee) {
            // expected
        }
    }
}
