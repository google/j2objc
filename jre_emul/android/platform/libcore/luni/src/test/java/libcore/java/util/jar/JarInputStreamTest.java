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

import tests.support.resource.Support_Resources;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

public class JarInputStreamTest extends junit.framework.TestCase {

    private static final int DSA_INDEX = 2;

    private static final int TEST_CLASS_INDEX = 4;

    private static final int TOTAL_ENTRIES = 4;

    private static final String A_CLASS = "foo/bar/A.class";

    // a 'normal' jar file
    private String jarName;

    // same as patch.jar but without a manifest file
    private String jarName2;

    @Override
    protected void setUp() {
        jarName = Support_Resources.getURL("morestuff/hyts_patch.jar");
        jarName2 = Support_Resources.getURL("morestuff/hyts_patch2.jar");
    }

    public void test_ConstructorLjava_io_InputStream() throws Exception {
        // Test for method java.util.jar.JarInputStream(java.io.InputStream)
        InputStream is = new URL(jarName).openConnection().getInputStream();
        boolean hasCorrectEntry = false;
        JarInputStream jis = new JarInputStream(is);
        assertNotNull("The jar input stream should have a manifest", jis.getManifest());

        JarEntry je = jis.getNextJarEntry();
        while (je != null) {
            if (je.getName().equals(A_CLASS)) {
                hasCorrectEntry = true;
            }
            je = jis.getNextJarEntry();
        }
        assertTrue("The jar input stream does not contain the correct entries", hasCorrectEntry);
    }

    public void test_closeAfterException() throws Exception {
        File resources = Support_Resources.createTempFolder();
        Support_Resources.copyFile(resources, null, "Broken_entry.jar");
        InputStream is = Support_Resources.getStream("Broken_entry.jar");
        JarInputStream jis = new JarInputStream(is, false);
        jis.getNextEntry();
        try {
            jis.getNextEntry();
            fail("ZipException expected");
        } catch (ZipException ee) {
            // expected
        }
        jis.close();
        try {
            jis.getNextEntry();
            fail("IOException expected");
        } catch (IOException ee) {
            // expected
        }
    }

    public void test_getNextJarEntry_Ex() throws Exception {
        final Set<String> desired = new HashSet<String>(Arrays
                .asList("foo/", "foo/bar/", "foo/bar/A.class", "Blah.txt"));
        Set<String> actual = new HashSet<String>();
        InputStream is = new URL(jarName).openConnection().getInputStream();
        JarInputStream jis = new JarInputStream(is);
        JarEntry je = jis.getNextJarEntry();
        while (je != null) {
            actual.add(je.toString());
            je = jis.getNextJarEntry();
        }
        assertEquals(actual, desired);
        jis.close();

        try {
            jis.getNextJarEntry();
            fail("IOException expected");
        } catch (IOException ee) {
            // expected
        }

        File resources = Support_Resources.createTempFolder();
        Support_Resources.copyFile(resources, null, "Broken_entry.jar");
        is = Support_Resources.getStream("Broken_entry.jar");
        jis = new JarInputStream(is, false);
        jis.getNextJarEntry();
        try {
            jis.getNextJarEntry();
            fail("ZipException expected");
        } catch (ZipException ee) {
            // expected
        }
    }

    public void test_getManifest() throws Exception {
        // Test for method java.util.jar.Manifest
        // java.util.jar.JarInputStream.getManifest()
        Manifest m;
        InputStream is = new URL(jarName2).openConnection().getInputStream();
        JarInputStream jis = new JarInputStream(is);
        m = jis.getManifest();
        assertNull("The jar input stream should not have a manifest", m);

        is = new URL(jarName).openConnection().getInputStream();
        jis = new JarInputStream(is);
        m = jis.getManifest();
        assertNotNull("The jar input stream should have a manifest", m);
    }

    public void test_getNextJarEntry() throws Exception {
        final Set<String> desired = new HashSet<String>(Arrays.asList(new String[] { "foo/",
                "foo/bar/", A_CLASS, "Blah.txt" }));
        Set<String> actual = new HashSet<String>();
        InputStream is = new URL(jarName).openConnection().getInputStream();
        JarInputStream jis = new JarInputStream(is);
        JarEntry je = jis.getNextJarEntry();
        while (je != null) {
            actual.add(je.toString());
            je = jis.getNextJarEntry();
        }
        assertEquals(actual, desired);
    }

    public void test_JarInputStream_Integrate_Jar_getNextEntry()
            throws IOException {
        String intJarName = Support_Resources.getURL("Integrate.jar");
        InputStream is = new URL(intJarName).openConnection()
                .getInputStream();
        JarInputStream jin = new JarInputStream(is, true);
        ZipEntry entry = null;
        int count = 0;
        while (count == 0 || entry != null) {
            count++;
            entry = jin.getNextEntry();
        }
        assertEquals(TOTAL_ENTRIES + 1, count);
        jin.close();
    }

    /**
     * J2ObjC removed: These test the modified jar feature j2objc doesn't support,
     * because executing downloaded code is banned from iOS apps
     */
//    public void test_JarInputStream_Modified_Class_getNextEntry()
//            throws IOException {
//        String modJarName = Support_Resources.getURL("Modified_Class.jar");
//        InputStream is = new URL(modJarName).openConnection()
//                .getInputStream();
//        JarInputStream jin = new JarInputStream(is, true);
//        ZipEntry zipEntry = null;
//
//        int count = 0;
//        while (count == 0 || zipEntry != null) {
//            count++;
//            try {
//                zipEntry = jin.getNextEntry();
//                if (count == TEST_CLASS_INDEX + 1) {
//                    fail("Should throw Security Exception");
//                }
//            } catch (SecurityException e) {
//                if (count != TEST_CLASS_INDEX + 1) {
//                    throw e;
//                }
//
//            }
//        }
//        assertEquals(TOTAL_ENTRIES + 2, count);
//        jin.close();
//    }
//
//    public void test_JarInputStream_Modified_Manifest_MainAttributes_getNextEntry()
//            throws IOException {
//        String modJarName = Support_Resources.getURL("Modified_Manifest_MainAttributes.jar");
//        InputStream is = new URL(modJarName).openConnection()
//                .getInputStream();
//        JarInputStream jin = new JarInputStream(is, true);
//
//        assertEquals("META-INF/TESTROOT.SF", jin.getNextEntry().getName());
//        assertEquals("META-INF/TESTROOT.DSA", jin.getNextEntry().getName());
//        try {
//            jin.getNextEntry();
//            fail();
//        } catch (SecurityException expected) {
//        }
//        assertEquals("META-INF/", jin.getNextEntry().getName());
//        assertEquals("Test.class", jin.getNextEntry().getName());
//        assertNull(jin.getNextEntry());
//        jin.close();
//    }
//
//    public void test_JarInputStream_Modified_Manifest_EntryAttributes_getNextEntry()
//            throws IOException {
//        String modJarName = Support_Resources
//                .getURL("Modified_Manifest_EntryAttributes.jar");
//        InputStream is = new URL(modJarName).openConnection()
//                .getInputStream();
//        JarInputStream jin = new JarInputStream(is, true);
//        ZipEntry zipEntry = null;
//
//        int count = 0;
//        while (count == 0 || zipEntry != null) {
//            count++;
//            try {
//                zipEntry = jin.getNextEntry();
//                if (count == DSA_INDEX + 1) {
//                    fail("Should throw Security Exception");
//                }
//            } catch (SecurityException e) {
//                if (count != DSA_INDEX + 1) {
//                    throw e;
//                }
//            }
//        }
//        assertEquals(TOTAL_ENTRIES + 2, count);
//        jin.close();
//    }
//
//    public void test_JarInputStream_Modified_SF_EntryAttributes_getNextEntry()
//            throws IOException {
//        String modJarName = Support_Resources
//                .getURL("Modified_SF_EntryAttributes.jar");
//        InputStream is = new URL(modJarName).openConnection()
//                .getInputStream();
//        JarInputStream jin = new JarInputStream(is, true);
//        ZipEntry zipEntry = null;
//
//        int count = 0;
//        while (count == 0 || zipEntry != null) {
//            count++;
//            try {
//                zipEntry = jin.getNextEntry();
//                if (count == DSA_INDEX + 1) {
//                    fail("Should throw Security Exception");
//                }
//            } catch (SecurityException e) {
//                if (count != DSA_INDEX + 1) {
//                    throw e;
//                }
//            }
//        }
//        assertEquals(TOTAL_ENTRIES + 2, count);
//        jin.close();
//    }
//
//    public void test_JarInputStream_Modified_Class_read() throws IOException {
//        String modJarName = Support_Resources.getURL("Modified_Class.jar");
//        InputStream is = new URL(modJarName).openConnection()
//                .getInputStream();
//        JarInputStream jin = new JarInputStream(is, true);
//        int count = 0;
//        ZipEntry zipEntry = null;
//        while (count == 0 || zipEntry != null) {
//            count++;
//            zipEntry = jin.getNextEntry();
//            byte[] buffer = new byte[1024];
//            try {
//                int length = 0;
//                while (length >= 0) {
//                    length = jin.read(buffer);
//                }
//                if (count == TEST_CLASS_INDEX) {
//                    fail("Should throw Security Exception");
//                }
//            } catch (SecurityException e) {
//                if (count < TEST_CLASS_INDEX) {
//                    throw e;
//                }
//            }
//        }
//        assertEquals(TOTAL_ENTRIES + 1, count);
//        jin.close();
//    }
//
//    public void test_Integrate_Jar_read() throws IOException {
//        String intJarName = Support_Resources.getURL("Integrate.jar");
//        InputStream is = new URL(intJarName).openConnection()
//                .getInputStream();
//        JarInputStream jin = new JarInputStream(is, true);
//        int count = 0;
//        ZipEntry zipEntry = null;
//        while (count == 0 || zipEntry != null) {
//            count++;
//            zipEntry = jin.getNextEntry();
//            byte[] buffer = new byte[1024];
//            int length = 0;
//            while (length >= 0) {
//                length = jin.read(buffer);
//            }
//
//        }
//        assertEquals(TOTAL_ENTRIES + 1, count);
//        jin.close();
//    }
//
//    public void test_JarInputStream_Modified_Manifest_MainAttributes_read()
//            throws IOException {
//        String modJarName = Support_Resources
//                .getURL("Modified_Manifest_MainAttributes.jar");
//        InputStream is = new URL(modJarName).openConnection()
//                .getInputStream();
//        JarInputStream jin = new JarInputStream(is, true);
//        int count = 0;
//        ZipEntry zipEntry = null;
//        while (count == 0 || zipEntry != null) {
//            count++;
//            zipEntry = jin.getNextEntry();
//            byte[] buffer = new byte[1024];
//            try {
//                int length = 0;
//                while (length >= 0) {
//                    length = jin.read(buffer);
//                }
//                if (count == DSA_INDEX) {
//                    fail("Should throw Security Exception");
//                }
//            } catch (SecurityException e) {
//                if (count != DSA_INDEX) {
//                    throw e;
//                }
//            }
//        }
//        assertEquals(TOTAL_ENTRIES + 1, count);
//        jin.close();
//    }
//
//    public void test_JarInputStream_Modified_SF_EntryAttributes_read()
//            throws IOException {
//        String modJarName = Support_Resources
//                .getURL("Modified_SF_EntryAttributes.jar");
//        InputStream is = new URL(modJarName).openConnection()
//                .getInputStream();
//        JarInputStream jin = new JarInputStream(is, true);
//        int count = 0;
//        ZipEntry zipEntry = null;
//        while (count == 0 || zipEntry != null) {
//            count++;
//            zipEntry = jin.getNextEntry();
//            byte[] buffer = new byte[1024];
//            try {
//                int length = 0;
//                while (length >= 0) {
//                    length = jin.read(buffer);
//                }
//                if (count == DSA_INDEX) {
//                    fail("Should throw Security Exception");
//                }
//            } catch (SecurityException e) {
//                if (count != DSA_INDEX) {
//                    throw e;
//                }
//            }
//        }
//        assertEquals(TOTAL_ENTRIES + 1, count);
//        jin.close();
//    }
//
//    public void test_getNextEntry() throws Exception {
//        File resources = Support_Resources.createTempFolder();
//        Support_Resources.copyFile(resources, null, "Broken_entry.jar");
//        InputStream is = Support_Resources.getStream("Broken_entry.jar");
//        JarInputStream jis = new JarInputStream(is, false);
//        jis.getNextEntry();
//        try {
//            jis.getNextEntry();
//            fail("ZipException expected");
//        } catch (ZipException ee) {
//            // expected
//        }
//
//        try {
//            jis.close();  // Android throws exception here, already!
//            jis.getNextEntry();  // But RI here, only!
//            fail("IOException expected");
//        } catch (IOException ee) {
//            // expected
//        }
//    }
//
//    /**
//     * hyts_metainf.jar contains an additional entry in META-INF (META-INF/bad_checksum.txt),
//     * that has been altered since jar signing - we expect to detect a mismatching digest.
//     */
//    public void test_metainf_verification() throws Exception {
//        String jarFilename = "hyts_metainf.jar";
//        File resources = Support_Resources.createTempFolder();
//        Support_Resources.copyFile(resources, null, jarFilename);
//        InputStream is = Support_Resources.getStream(jarFilename);
//
//        try (JarInputStream jis = new JarInputStream(is, true)) {
//            JarEntry je = jis.getNextJarEntry();
//            je = jis.getNextJarEntry();
//            je = jis.getNextJarEntry();
//            je = jis.getNextJarEntry();
//
//            if (!je.getName().equals("META-INF/bad_checksum.txt")) {
//                fail("Expected META-INF/bad_checksum.txt as a 4th entry, got:" + je.getName());
//            }
//            byte[] buffer = new byte[1024];
//            int length = 0;
//            try {
//                while (length >= 0) {
//                    length = jis.read(buffer);
//                }
//                fail("SecurityException expected");
//            } catch (SecurityException expected) {}
//        }
//    }

}
