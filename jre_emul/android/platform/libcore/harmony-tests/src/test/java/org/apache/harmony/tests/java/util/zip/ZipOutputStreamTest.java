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
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/* J2ObjC removed: not supported by Junit 4.11 (https://github.com/google/j2objc/issues/1318).
import libcore.junit.junit3.TestCaseWithRules;
import libcore.junit.util.ResourceLeakageDetector.DisableResourceLeakageDetection;
import libcore.junit.util.ResourceLeakageDetector; */
import org.junit.Rule;
import org.junit.rules.TestRule;

public class ZipOutputStreamTest extends junit.framework.TestCase /* J2ObjC removed: TestCaseWithRules */ {
    /* J2ObjC removed: not supported by Junit 4.11 (https://github.com/google/j2objc/issues/1318).
    @Rule
    public TestRule guardRule = ResourceLeakageDetector.getRule(); */

    ZipOutputStream zos;

    ByteArrayOutputStream bos;

    ZipInputStream zis;

    static final String data = "HelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorld";

    /**
     * java.util.zip.ZipOutputStream#close()
     */
    public void test_close() throws Exception {
        zos.putNextEntry(new ZipEntry("XX"));
        zos.closeEntry();
        zos.close();

        // Regression for HARMONY-97
        ZipOutputStream zos = new ZipOutputStream(new ByteArrayOutputStream());
        zos.putNextEntry(new ZipEntry("myFile"));
        zos.close();
        zos.close(); // Should be a no-op
    }

    /**
     * java.util.zip.ZipOutputStream#closeEntry()
     */
    public void test_closeEntry() throws IOException {
        ZipEntry ze = new ZipEntry("testEntry");
        ze.setTime(System.currentTimeMillis());
        zos.putNextEntry(ze);
        zos.write("Hello World".getBytes("UTF-8"));
        zos.closeEntry();
        assertTrue("closeEntry failed to update required fields",
                ze.getSize() == 11 && ze.getCompressedSize() == 13);

    }

    /**
     * java.util.zip.ZipOutputStream#finish()
     */
    public void test_finish() throws Exception {
        ZipEntry ze = new ZipEntry("test");
        zos.putNextEntry(ze);
        zos.write("Hello World".getBytes());
        zos.finish();
        assertEquals("Finish failed to closeCurrentEntry", 11, ze.getSize());

        ZipOutputStream zos = new ZipOutputStream(new ByteArrayOutputStream());
        zos.putNextEntry(new ZipEntry("myFile"));
        zos.finish();
        zos.close();
        try {
            zos.finish();
            fail("Assert 0: Expected IOException");
        } catch (IOException e) {
            // Expected
        }
    }

    /**
     * java.util.zip.ZipOutputStream#putNextEntry(java.util.zip.ZipEntry)
     */
    public void test_putNextEntryLjava_util_zip_ZipEntry() throws IOException {
        ZipEntry ze = new ZipEntry("testEntry");
        ze.setTime(System.currentTimeMillis());
        zos.putNextEntry(ze);
        zos.write("Hello World".getBytes());
        zos.closeEntry();
        zos.close();
        zis = new ZipInputStream(new ByteArrayInputStream(bos.toByteArray()));
        ZipEntry ze2 = zis.getNextEntry();
        zis.closeEntry();
        assertEquals("Failed to write correct entry", ze.getName(), ze2.getName());
        assertEquals("Failed to write correct entry", ze.getCrc(), ze2.getCrc());
        try {
            zos.putNextEntry(ze);
            fail("Entry with incorrect setting failed to throw exception");
        } catch (IOException e) {
            // expected
        }
    }

    /**
     * java.util.zip.ZipOutputStream#setComment(java.lang.String)
     */
    /* J2ObjC removed: not supported by Junit 4.11 (https://github.com/google/j2objc/issues/1318).
    @DisableResourceLeakageDetection(
            why = "InflaterOutputStream.close() does not work properly if finish() throws an"
                    + " exception; finish() throws an exception if the output is invalid; this is"
                    + " an issue with the ZipOutputStream created in setUp()",
            bug = "31797037") */
    public void test_setCommentLjava_lang_String() {
        // There is no way to get the comment back, so no way to determine if
        // the comment is set correct
        zos.setComment("test setComment");

        try {
            zos.setComment(new String(new byte[0xFFFF + 1]));
            fail("Comment over 0xFFFF in length should throw exception");
        } catch (IllegalArgumentException e) {
            // Passed
        }
    }

    /**
     * java.util.zip.ZipOutputStream#setLevel(int)
     */
    public void test_setLevelI() throws IOException {
        ZipEntry ze = new ZipEntry("test");
        zos.putNextEntry(ze);
        zos.write(data.getBytes());
        zos.closeEntry();
        long csize = ze.getCompressedSize();
        zos.setLevel(9); // Max Compression
        zos.putNextEntry(ze = new ZipEntry("test2"));
        zos.write(data.getBytes());
        zos.closeEntry();
        assertTrue("setLevel failed", csize <= ze.getCompressedSize());
    }

    /**
     * java.util.zip.ZipOutputStream#setMethod(int)
     */
    public void test_setMethodI() throws IOException {
        ZipEntry ze = new ZipEntry("test");
        zos.setMethod(ZipOutputStream.STORED);
        CRC32 tempCrc = new CRC32();
        tempCrc.update(data.getBytes());
        ze.setCrc(tempCrc.getValue());
        ze.setSize(new String(data).length());
        zos.putNextEntry(ze);
        zos.write(data.getBytes());
        zos.closeEntry();
        long csize = ze.getCompressedSize();
        zos.setMethod(ZipOutputStream.DEFLATED);
        zos.putNextEntry(ze = new ZipEntry("test2"));
        zos.write(data.getBytes());
        zos.closeEntry();
        assertTrue("setLevel failed", csize >= ze.getCompressedSize());
    }

    /**
     * java.util.zip.ZipOutputStream#write(byte[], int, int)
     */
    /* J2ObjC removed: not supported by Junit 4.11 (https://github.com/google/j2objc/issues/1318).
    @DisableResourceLeakageDetection(
            why = "InflaterOutputStream.close() does not work properly if finish() throws an"
                    + " exception; finish() throws an exception if the output is invalid.",
            bug = "31797037") */
    public void test_write$BII() throws IOException {
        ZipEntry ze = new ZipEntry("test");
        zos.putNextEntry(ze);
        zos.write(data.getBytes());
        zos.closeEntry();
        zos.close();
        zos = null;
        zis = new ZipInputStream(new ByteArrayInputStream(bos.toByteArray()));
        zis.getNextEntry();
        byte[] b = new byte[data.length()];
        int r = 0;
        int count = 0;
        while (count != b.length && (r = zis.read(b, count, b.length)) != -1) {
            count += r;
        }
        zis.closeEntry();
        assertEquals("Write failed to write correct bytes", new String(b), data);

        File f = File.createTempFile("testZip", "tst");
        f.deleteOnExit();
        FileOutputStream stream = new FileOutputStream(f);
        ZipOutputStream zip = new ZipOutputStream(stream);
        zip.setMethod(ZipEntry.STORED);

        try {
            zip.putNextEntry(new ZipEntry("Second"));
            fail("Not set an entry. Should have thrown ZipException.");
        } catch (ZipException e) {
            // expected -- We have not set an entry
        }

        try {
            // We try to write data without entry
            zip.write(new byte[2]);
            fail("Writing data without an entry. Should have thrown IOException");
        } catch (IOException e) {
            // expected
        }

        try {
            // Try to write without an entry and with nonsense offset and
            // length
            zip.write(new byte[2], 0, 12);
            fail("Writing data without an entry. Should have thrown IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        // Regression for HARMONY-4405
        try {
            zip.write(null, 0, -2);
            fail();
        } catch (NullPointerException expected) {
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            zip.write(null, 0, 2);
            fail();
        } catch (NullPointerException expected) {
        }
        try {
            zip.write(new byte[2], 0, -2);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }

        // Close stream because ZIP is invalid
        stream.close();
    }

    /**
     * java.util.zip.ZipOutputStream#write(byte[], int, int)
     */
    /* J2ObjC removed: not supported by Junit 4.11 (https://github.com/google/j2objc/issues/1318).
    @DisableResourceLeakageDetection(
            why = "InflaterOutputStream.close() does not work properly if finish() throws an"
                    + " exception; finish() throws an exception if the output is invalid; this is"
                    + " an issue with the ZipOutputStream created in setUp()",
            bug = "31797037") */
    public void test_write$BII_2() throws IOException {
        // Regression for HARMONY-577
        File f1 = File.createTempFile("testZip1", "tst");
        f1.deleteOnExit();
        FileOutputStream stream1 = new FileOutputStream(f1);
        ZipOutputStream zip1 = new ZipOutputStream(stream1);
        zip1.putNextEntry(new ZipEntry("one"));
        zip1.setMethod(ZipOutputStream.STORED);
        zip1.setMethod(ZipEntry.STORED);

        zip1.write(new byte[2]);

        try {
            zip1.putNextEntry(new ZipEntry("Second"));
            fail("ZipException expected");
        } catch (ZipException e) {
            // expected - We have not set an entry
        }

        try {
            zip1.write(new byte[2]); // try to write data without entry
            fail("expected IOE there");
        } catch (IOException e2) {
            // expected
        }

        zip1.close();
    }
    /**
     * Test standard and info-zip-extended timestamp rounding
     */
    public void test_timeSerializationRounding() throws Exception {
        List<ZipEntry> entries = new ArrayList<>();
        ZipEntry zipEntry;

        entries.add(zipEntry = new ZipEntry("test1"));
        final long someTimestamp = 1479139143200L;
        zipEntry.setTime(someTimestamp);

        entries.add(zipEntry = new ZipEntry("test2"));
        zipEntry.setLastModifiedTime(FileTime.fromMillis(someTimestamp));

        for (ZipEntry entry : entries) {
            zos.putNextEntry(entry);
            zos.write(data.getBytes());
            zos.closeEntry();
        }
        zos.close();

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
           // getTime should be rounded down to a multiple of 2s
            ZipEntry readEntry = zis.getNextEntry();
            assertEquals((someTimestamp / 2000) * 2000,
                         readEntry.getTime());

            // With extended timestamp getTime&getLastModifiedTime should berounded down to a
            // multiple of 1s
            readEntry = zis.getNextEntry();
            assertEquals((someTimestamp / 1000) * 1000,
                         readEntry.getLastModifiedTime().toMillis());
            assertEquals((someTimestamp / 1000) * 1000,
                         readEntry.getTime());
        }
    }

    /**
     * Test info-zip extended timestamp support
     */
    public void test_exttSupport() throws Exception {
        List<ZipEntry> entries = new ArrayList<>();

        ZipEntry zipEntry;

        // There's no sane way to access ONLY mtime
        Field mtimeField = ZipEntry.class.getDeclaredField("mtime");
        mtimeField.setAccessible(true);

        // Serialized DOS timestamp resolution is 2s. Serialized extended
        // timestamp resolution is 1s. If we won't use rounded values then
        // asserting time equality would be more complicated (resolution of
        // getTime depends weather we use extended timestamp).
        //
        // We have to call setTime on all entries. If it's not set then
        // ZipOutputStream will call setTime(System.currentTimeMillis()) on it.
        // I will use this as a excuse to test whether setting particular time
        // values (~< 1980 ~> 2099) triggers use of the extended last-modified
        // timestamp.
        final long timestampWithinDostimeBound = ZipEntry.UPPER_DOSTIME_BOUND;
        assertEquals(0, timestampWithinDostimeBound % 1000);
        final long timestampBeyondDostimeBound = ZipEntry.UPPER_DOSTIME_BOUND + 2000;
        assertEquals(0, timestampBeyondDostimeBound % 1000);

        // This will set both dos timestamp and last-modified timestamp (because < 1980)
        entries.add(zipEntry = new ZipEntry("test_setTime"));
        zipEntry.setTime(0);
        assertNotNull(mtimeField.get(zipEntry));

        // Explicitly set info-zip last-modified extended timestamp
        entries.add(zipEntry = new ZipEntry("test_setLastModifiedTime"));
        zipEntry.setLastModifiedTime(FileTime.fromMillis(1000));

        // Set creation time and (since we have to call setTime on ZipEntry, otherwise
        // ZipOutputStream will call setTime(System.currentTimeMillis()) and the getTime()
        // assert will fail due to low serialization resolution) test that calling
        // setTime with value <= ZipEntry.UPPER_DOSTIME_BOUND won't set the info-zip
        // last-modified extended timestamp.
        entries.add(zipEntry = new ZipEntry("test_setCreationTime"));
        zipEntry.setCreationTime(FileTime.fromMillis(1000));
        zipEntry.setTime(timestampWithinDostimeBound);
        assertNull(mtimeField.get(zipEntry));

        // Set last access time and test that calling setTime with value >
        // ZipEntry.UPPER_DOSTIME_BOUND will set the info-zip last-modified extended
        // timestamp
        entries.add(zipEntry = new ZipEntry("test_setLastAccessTime"));
        zipEntry.setLastAccessTime(FileTime.fromMillis(3000));
        zipEntry.setTime(timestampBeyondDostimeBound);
        assertNotNull(mtimeField.get(zipEntry));

        for (ZipEntry entry : entries) {
            zos.putNextEntry(entry);
            zos.write(data.getBytes());
            zos.closeEntry();
        }
        zos.close();

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            for (ZipEntry entry : entries) {
                ZipEntry readEntry = zis.getNextEntry();
                assertEquals(entry.getName(), readEntry.getName());
                assertEquals(entry.getName(), entry.getTime(), readEntry.getTime());
                assertEquals(entry.getName(), entry.getLastModifiedTime(), readEntry.getLastModifiedTime());
                assertEquals(entry.getLastAccessTime(), readEntry.getLastAccessTime());
                assertEquals(entry.getCreationTime(), readEntry.getCreationTime());
            }
        }
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        zos = new ZipOutputStream(bos = new ByteArrayOutputStream());
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            // Close the ZipInputStream first as that does not fail.
            if (zis != null) {
                zis.close();
            }
            if (zos != null) {
                // This will throw a ZipException if nothing is written to the ZipOutputStream.
                zos.close();
            }
        } catch (Exception e) {
        }
        super.tearDown();
    }
}
