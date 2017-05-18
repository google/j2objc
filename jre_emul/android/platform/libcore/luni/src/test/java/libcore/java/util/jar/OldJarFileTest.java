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
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import junit.framework.TestCase;
import tests.support.resource.Support_Resources;

public class OldJarFileTest extends TestCase {

    private final String jarName = "hyts_patch.jar"; // a 'normal' jar file
    private final String entryName = "foo/bar/A.class";
    private File resources;

    @Override public void setUp() throws Exception {
        super.setUp();
        resources = Support_Resources.createTempFolder();
    }

    public void test_ConstructorLjava_io_File() throws IOException {
        try {
            new JarFile(new File("Wrong.file"));
            fail("Should throw IOException");
        } catch (IOException expected) {
        }

        Support_Resources.copyFile(resources, null, jarName);
        new JarFile(new File(resources, jarName));
    }

    public void test_ConstructorLjava_lang_String() throws IOException {
        try {
            new JarFile("Wrong.file");
            fail("Should throw IOException");
        } catch (IOException expected) {
        }

        Support_Resources.copyFile(resources, null, jarName);
        String fileName = (new File(resources, jarName)).getCanonicalPath();
        new JarFile(fileName);
    }

    public void test_ConstructorLjava_lang_StringZ() throws IOException {
        try {
            new JarFile("Wrong.file", false);
            fail("Should throw IOException");
        } catch (IOException expected) {
        }

        Support_Resources.copyFile(resources, null, jarName);
        String fileName = (new File(resources, jarName)).getCanonicalPath();
        new JarFile(fileName, true);
    }

    public void test_ConstructorLjava_io_FileZ() throws IOException {
        try {
            new JarFile(new File("Wrong.file"), true);
            fail("Should throw IOException");
        } catch (IOException expected) {
        }

        Support_Resources.copyFile(resources, null, jarName);
        new JarFile(new File(resources, jarName), false);
    }

    public void test_ConstructorLjava_io_FileZI() throws IOException {
        try {
            new JarFile(new File("Wrong.file"), true,
                    ZipFile.OPEN_READ);
            fail("Should throw IOException");
        } catch (IOException expected) {
        }

        Support_Resources.copyFile(resources, null, jarName);
        new JarFile(new File(resources, jarName), false,
                ZipFile.OPEN_READ);

        try {
            Support_Resources.copyFile(resources, null, jarName);
            new JarFile(new File(resources, jarName), false,
                    ZipFile.OPEN_READ | ZipFile.OPEN_DELETE + 33);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void test_close() throws IOException {
        String modifiedJarName = "Modified_SF_EntryAttributes.jar";
        Support_Resources.copyFile(resources, null, modifiedJarName);
        JarFile jarFile = new JarFile(new File(resources, modifiedJarName), true);
        jarFile.entries();

        jarFile.close();
        jarFile.close();

        // Can not check IOException
    }

    public void test_getInputStreamLjava_util_jar_JarEntry() throws IOException {
        Support_Resources.copyFile(resources, null, jarName);
        File localFile = new File(resources, jarName);

        byte[] b = new byte[1024];
        JarFile jf = new JarFile(localFile);
        InputStream is = jf.getInputStream(jf.getEntry(entryName));
        assertTrue("Returned invalid stream", is.available() > 0);
        int r = is.read(b, 0, 1024);
        is.close();
        StringBuilder stringBuffer = new StringBuilder(r);
        for (int i = 0; i < r; i++) {
            stringBuffer.append((char) (b[i] & 0xff));
        }
        String contents = stringBuffer.toString();
        assertTrue("Incorrect stream read", contents.indexOf("bar") > 0);
        jf.close();

        jf = new JarFile(localFile);
        InputStream in = jf.getInputStream(new JarEntry("invalid"));
        assertNull("Got stream for non-existent entry", in);

        try {
            Support_Resources.copyFile(resources, null, jarName);
            File signedFile = new File(resources, jarName);
            jf = new JarFile(signedFile);
            JarEntry jre = new JarEntry("foo/bar/A.class");
            jf.getInputStream(jre);
            // InputStream returned in any way, exception can be thrown in case
            // of reading from this stream only.
            // fail("Should throw ZipException");
        } catch (ZipException expected) {
        }

        try {
            Support_Resources.copyFile(resources, null, jarName);
            File signedFile = new File(resources, jarName);
            jf = new JarFile(signedFile);
            JarEntry jre = new JarEntry("foo/bar/A.class");
            jf.close();
            jf.getInputStream(jre);
            // InputStream returned in any way, exception can be thrown in case
            // of reading from this stream only.
            // The same for IOException
            fail("Should throw IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }
}
