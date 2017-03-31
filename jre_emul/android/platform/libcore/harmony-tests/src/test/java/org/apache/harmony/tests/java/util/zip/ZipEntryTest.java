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
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import libcore.io.Streams;
import tests.support.resource.Support_Resources;

public class ZipEntryTest extends junit.framework.TestCase {
    // zip file hyts_ZipFile.zip must be included as a resource
    private ZipEntry zentry;
    private ZipFile zfile;

    private long orgSize;
    private long orgCompressedSize;
    private long orgCrc;
    private long orgTime;

    /**
     * java.util.zip.ZipEntry#ZipEntry(java.lang.String)
     */
    public void test_ConstructorLjava_lang_String() {
        // Test for method java.util.zip.ZipEntry(java.lang.String)
        zentry = zfile.getEntry("File3.txt");
        assertNotNull("Failed to create ZipEntry", zentry);
        try {
            zentry = zfile.getEntry(null);
            fail("NullPointerException not thrown");
        } catch (NullPointerException e) {
        }
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < 65535; i++) {
            s.append('a');
        }
        try {
            zentry = new ZipEntry(s.toString());
        } catch (IllegalArgumentException e) {
            fail("Unexpected IllegalArgumentException During Test.");
        }
        try {
            s.append('a');
            zentry = new ZipEntry(s.toString());
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }
        try {
            String n = null;
            zentry = new ZipEntry(n);
            fail("NullPointerException not thrown");
        } catch (NullPointerException e) {
        }
    }

    /**
     * java.util.zip.ZipEntry#getComment()
     */
    public void test_getComment() {
        // Test for method java.lang.String java.util.zip.ZipEntry.getComment()
        ZipEntry zipEntry = new ZipEntry("zippy.zip");
        assertNull("Incorrect Comment Returned.", zipEntry.getComment());
        zipEntry.setComment("This Is A Comment");
        assertEquals("Incorrect Comment Returned.",
                "This Is A Comment", zipEntry.getComment());
    }

    /**
     * java.util.zip.ZipEntry#getCompressedSize()
     */
    public void test_getCompressedSize() {
        // Test for method long java.util.zip.ZipEntry.getCompressedSize()
        assertTrue("Incorrect compressed size returned", zentry
                .getCompressedSize() == orgCompressedSize);
    }

    /**
     * java.util.zip.ZipEntry#getCrc()
     */
    public void test_getCrc() {
        // Test for method long java.util.zip.ZipEntry.getCrc()
        assertEquals("Failed to get Crc", orgCrc, zentry.getCrc());
    }

    /**
     * java.util.zip.ZipEntry#getExtra()
     */
    public void test_getExtra() {
        // Test for method byte [] java.util.zip.ZipEntry.getExtra()
        assertNull("Incorrect extra information returned",
                zentry.getExtra());
        byte[] ba = { 'T', 'E', 'S', 'T' };
        zentry = new ZipEntry("test.tst");
        zentry.setExtra(ba);
        assertEquals("Incorrect Extra Information Returned.",
                ba, zentry.getExtra());
    }

    /**
     * java.util.zip.ZipEntry#getMethod()
     */
    public void test_getMethod() {
        // Test for method int java.util.zip.ZipEntry.getMethod()
        zentry = zfile.getEntry("File1.txt");
        assertEquals("Incorrect compression method returned",
                java.util.zip.ZipEntry.STORED, zentry.getMethod());
        zentry = zfile.getEntry("File3.txt");
        assertEquals("Incorrect compression method returned",
                java.util.zip.ZipEntry.DEFLATED, zentry.getMethod());
        zentry = new ZipEntry("test.tst");
        assertEquals("Incorrect Method Returned.", -1, zentry.getMethod());
    }

    /**
     * java.util.zip.ZipEntry#getName()
     */
    public void test_getName() {
        // Test for method java.lang.String java.util.zip.ZipEntry.getName()
        assertEquals("Incorrect name returned - Note return result somewhat ambiguous in spec",
                "File1.txt", zentry.getName());
    }

    /**
     * java.util.zip.ZipEntry#getSize()
     */
    public void test_getSize() {
        // Test for method long java.util.zip.ZipEntry.getSize()
        assertEquals("Incorrect size returned", orgSize, zentry.getSize());
    }

    /**
     * java.util.zip.ZipEntry#getTime()
     */
    public void test_getTime() {
        // Test for method long java.util.zip.ZipEntry.getTime()
        assertEquals("Failed to get time", orgTime, zentry.getTime());
    }

    /**
     * java.util.zip.ZipEntry#isDirectory()
     */
    public void test_isDirectory() {
        // Test for method boolean java.util.zip.ZipEntry.isDirectory()
        assertTrue("Entry should not answer true to isDirectory", !zentry
                .isDirectory());
        zentry = new ZipEntry("Directory/");
        assertTrue("Entry should answer true to isDirectory", zentry
                .isDirectory());
    }

    /**
     * java.util.zip.ZipEntry#setComment(java.lang.String)
     */
    public void test_setCommentLjava_lang_String() {
        // Test for method void
        // java.util.zip.ZipEntry.setComment(java.lang.String)
        zentry = zfile.getEntry("File1.txt");
        zentry.setComment("Set comment using api");
        assertEquals("Comment not correctly set",
                "Set comment using api", zentry.getComment());
        String n = null;
        zentry.setComment(n);
        assertNull("Comment not correctly set", zentry.getComment());
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < 0xFFFF; i++) {
            s.append('a');
        }
        try {
            zentry.setComment(s.toString());
        } catch (IllegalArgumentException e) {
            fail("Unexpected IllegalArgumentException During Test.");
        }
        try {
            s.append('a');
            zentry.setComment(s.toString());
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * java.util.zip.ZipEntry#setCompressedSize(long)
     */
    public void test_setCompressedSizeJ() {
        // Test for method void java.util.zip.ZipEntry.setCompressedSize(long)
        zentry.setCompressedSize(orgCompressedSize + 10);
        assertEquals("Set compressed size failed",
                (orgCompressedSize + 10), zentry.getCompressedSize());
        zentry.setCompressedSize(0);
        assertEquals("Set compressed size failed",
                0, zentry.getCompressedSize());
        zentry.setCompressedSize(-25);
        assertEquals("Set compressed size failed",
                -25, zentry.getCompressedSize());
        zentry.setCompressedSize(4294967296l);
        assertEquals("Set compressed size failed",
                4294967296l, zentry.getCompressedSize());
    }

    /**
     * java.util.zip.ZipEntry#setCrc(long)
     */
    public void test_setCrcJ() {
        // Test for method void java.util.zip.ZipEntry.setCrc(long)
        zentry.setCrc(orgCrc + 100);
        assertEquals("Failed to set Crc", (orgCrc + 100), zentry.getCrc());
        zentry.setCrc(0);
        assertEquals("Failed to set Crc", 0, zentry.getCrc());
        try {
            zentry.setCrc(-25);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }
        try {
            zentry.setCrc(4294967295l);
        } catch (IllegalArgumentException e) {
            fail("Unexpected IllegalArgumentException during test");
        }
        try {
            zentry.setCrc(4294967296l);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * java.util.zip.ZipEntry#setExtra(byte[])
     */
    public void test_setExtra$B() {
        // Test for method void java.util.zip.ZipEntry.setExtra(byte [])
        zentry = zfile.getEntry("File1.txt");
        zentry.setExtra("Test setting extra information".getBytes());
        assertEquals("Extra information not written properly", "Test setting extra information", new String(zentry
                .getExtra(), 0, zentry.getExtra().length)
        );
        zentry = new ZipEntry("test.tst");
        byte[] ba = new byte[0xFFFF];
        try {
            zentry.setExtra(ba);
        } catch (IllegalArgumentException e) {
            fail("Unexpected IllegalArgumentException during test");
        }
        try {
            ba = new byte[0xFFFF + 1];
            zentry.setExtra(ba);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }

        // One constructor
        ZipEntry zeInput = new ZipEntry("InputZIP");
        byte[] extraB = { 'a', 'b', 'd', 'e' };
        zeInput.setExtra(extraB);
        assertEquals(extraB, zeInput.getExtra());
        assertEquals(extraB[3], zeInput.getExtra()[3]);
        assertEquals(extraB.length, zeInput.getExtra().length);

        // test another constructor
        ZipEntry zeOutput = new ZipEntry(zeInput);
        assertEquals(zeInput.getExtra()[3], zeOutput.getExtra()[3]);
        assertEquals(zeInput.getExtra().length, zeOutput.getExtra().length);
        assertEquals(extraB[3], zeOutput.getExtra()[3]);
        assertEquals(extraB.length, zeOutput.getExtra().length);
    }

    /**
     * java.util.zip.ZipEntry#setMethod(int)
     */
    public void test_setMethodI() {
        // Test for method void java.util.zip.ZipEntry.setMethod(int)
        zentry = zfile.getEntry("File3.txt");
        zentry.setMethod(ZipEntry.STORED);
        assertEquals("Failed to set compression method",
                ZipEntry.STORED, zentry.getMethod());
        zentry.setMethod(ZipEntry.DEFLATED);
        assertEquals("Failed to set compression method",
                ZipEntry.DEFLATED, zentry.getMethod());
        try {
            int error = 1;
            zentry = new ZipEntry("test.tst");
            zentry.setMethod(error);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * java.util.zip.ZipEntry#setSize(long)
     */
    public void test_setSizeJ() {
        // Test for method void java.util.zip.ZipEntry.setSize(long)
        zentry.setSize(orgSize + 10);
        assertEquals("Set size failed", (orgSize + 10), zentry.getSize());
        zentry.setSize(0);
        assertEquals("Set size failed", 0, zentry.getSize());
        try {
            zentry.setSize(-25);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }
        try {
            zentry.setCrc(4294967295l);
        } catch (IllegalArgumentException e) {
            fail("Unexpected IllegalArgumentException during test");
        }
        try {
            zentry.setCrc(4294967296l);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * java.util.zip.ZipEntry#setTime(long)
     */
    public void test_setTimeJ() {
        // Test for method void java.util.zip.ZipEntry.setTime(long)
        zentry.setTime(orgTime + 10000);
        assertEquals("Test 1: Failed to set time: " + zentry.getTime(), (orgTime + 10000),
                zentry.getTime());
        zentry.setTime(orgTime - 10000);
        assertEquals("Test 2: Failed to set time: " + zentry.getTime(), (orgTime - 10000),
                zentry.getTime());
        TimeZone zone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("EST"));
            zentry.setTime(0);
            assertEquals("Test 3: Failed to set time: " + zentry.getTime(),
                    315550800000L, zentry.getTime());
            TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
            assertEquals("Test 3a: Failed to set time: " + zentry.getTime(),
                    315532800000L, zentry.getTime());
            zentry.setTime(0);
            TimeZone.setDefault(TimeZone.getTimeZone("EST"));
            assertEquals("Test 3b: Failed to set time: " + zentry.getTime(),
                    315550800000L, zentry.getTime());

            zentry.setTime(-25);
            assertEquals("Test 4: Failed to set time: " + zentry.getTime(),
                    315550800000L, zentry.getTime());
            zentry.setTime(4354837200000L);
            assertEquals("Test 5: Failed to set time: " + zentry.getTime(),
                    315550800000L, zentry.getTime());
        } finally {
            TimeZone.setDefault(zone);
        }
    }

    /**
     * java.util.zip.ZipEntry#toString()
     */
    public void test_toString() {
        // Test for method java.lang.String java.util.zip.ZipEntry.toString()
        assertTrue("Returned incorrect entry name", zentry.toString().indexOf(
                "File1.txt") >= 0);
    }

    /**
     * java.util.zip.ZipEntry#ZipEntry(java.util.zip.ZipEntry)
     */
    public void test_ConstructorLjava_util_zip_ZipEntry() {
        // Test for method java.util.zip.ZipEntry(util.zip.ZipEntry)
        zentry.setSize(2);
        zentry.setCompressedSize(4);
        zentry.setComment("Testing");
        ZipEntry zentry2 = new ZipEntry(zentry);
        assertEquals("ZipEntry Created With Incorrect Size.",
                2, zentry2.getSize());
        assertEquals("ZipEntry Created With Incorrect Compressed Size.", 4, zentry2
                .getCompressedSize());
        assertEquals("ZipEntry Created With Incorrect Comment.", "Testing", zentry2
                .getComment());
        assertEquals("ZipEntry Created With Incorrect Crc.",
                orgCrc, zentry2.getCrc());
        assertEquals("ZipEntry Created With Incorrect Time.",
                orgTime, zentry2.getTime());
    }

    /**
     * java.util.zip.ZipEntry#clone()
     */
    public void test_clone() {
        // Test for method java.util.zip.ZipEntry.clone()
        Object obj = zentry.clone();
        assertEquals("toString()", zentry.toString(), obj.toString());
        assertEquals("hashCode()", zentry.hashCode(), obj.hashCode());

        // One constructor
        ZipEntry zeInput = new ZipEntry("InputZIP");
        byte[] extraB = { 'a', 'b', 'd', 'e' };
        zeInput.setExtra(extraB);
        assertEquals(extraB, zeInput.getExtra());
        assertEquals(extraB[3], zeInput.getExtra()[3]);
        assertEquals(extraB.length, zeInput.getExtra().length);

        // test Clone()
        ZipEntry zeOutput = (ZipEntry) zeInput.clone();
        assertEquals(zeInput.getExtra()[3], zeOutput.getExtra()[3]);
        assertEquals(zeInput.getExtra().length, zeOutput.getExtra().length);
        assertEquals(extraB[3], zeOutput.getExtra()[3]);
        assertEquals(extraB.length, zeOutput.getExtra().length);
    }

    @Override
    protected void setUp() throws Exception {
        // Create a local copy of the file since some tests want to alter
        // information.
        final File f = File.createTempFile("ZipEntryTest", ".zip");
        InputStream is = Support_Resources.getStream("hyts_ZipFile.zip");

        FileOutputStream fos = new java.io.FileOutputStream(f);
        Streams.copy(is, fos);
        is.close();
        fos.close();

        zfile = new ZipFile(f);
        zentry = zfile.getEntry("File1.txt");

        orgSize = zentry.getSize();
        orgCompressedSize = zentry.getCompressedSize();
        orgCrc = zentry.getCrc();
        orgTime = zentry.getTime();
    }

    @Override
    protected void tearDown() {
        try {
            if (zfile != null) {
                zfile.close();
            }
        } catch (IOException ignored) {
        }
    }
}

