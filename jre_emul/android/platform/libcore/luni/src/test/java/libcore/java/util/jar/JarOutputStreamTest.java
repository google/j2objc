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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public class JarOutputStreamTest extends junit.framework.TestCase {

    /**
     * java.util.jar.JarOutputStream#putNextEntry(java.util.zip.ZipEntry)
     */
    public void test_putNextEntryLjava_util_zip_ZipEntry() throws Exception {

    }

    public void test_JarOutputStreamLjava_io_OutputStreamLjava_util_jar_Manifest()
            throws IOException {
        File fooJar = File.createTempFile("hyts_", ".jar");
        fooJar.deleteOnExit();
        File barZip = File.createTempFile("hyts_", ".zip");
        barZip.deleteOnExit();

        FileOutputStream fos = new FileOutputStream(fooJar);

        Manifest man = new Manifest();
        Attributes att = man.getMainAttributes();
        att.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        att.put(Attributes.Name.MAIN_CLASS, "foo.bar.execjartest.Foo");
        att.put(Attributes.Name.CLASS_PATH, barZip.getName());

        fos.close();
        try {
            new JarOutputStream(fos, man);
            fail("IOException expected");
        } catch (IOException ee) {
            // expected
        }

        try {
            new JarOutputStream(fos, null);
            fail("NullPointerException expected");
        } catch (NullPointerException ee) {
            // expected
        }
    }

    public void test_JarOutputStreamLjava_io_OutputStream() throws IOException {
        File fooJar = File.createTempFile("hyts_", ".jar");

        FileOutputStream fos = new FileOutputStream(fooJar);
        ZipEntry ze = new ZipEntry("Test");

        try {
            JarOutputStream joutFoo = new JarOutputStream(fos);
            joutFoo.putNextEntry(ze);
            joutFoo.write(33);
        } catch (IOException ee) {
            fail("IOException is not expected");
        }

        fos.close();
        fooJar.delete();
        try {
            JarOutputStream joutFoo = new JarOutputStream(fos);
            joutFoo.putNextEntry(ze);
            fail("IOException expected");
        } catch (IOException ee) {
            // expected
        }
    }
}
