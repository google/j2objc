/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.java.util.zip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
/* J2ObjC removed: not supported by Junit 4.11 (https://github.com/google/j2objc/issues/1318).
import libcore.junit.junit3.TestCaseWithRules;
import libcore.junit.util.ResourceLeakageDetector;
import libcore.junit.util.ResourceLeakageDetector.DisableResourceLeakageDetection; */
import org.junit.Rule;
import org.junit.rules.TestRule;

public final class ZipOutputStreamTest extends junit.framework.TestCase /* J2ObjC removed: TestCaseWithRules */ {
    /* J2ObjC removed: not supported by Junit 4.11 (https://github.com/google/j2objc/issues/1318).
    @Rule
    public TestRule guardRule = ResourceLeakageDetector.getRule(); */

    public void testShortMessage() throws IOException {
        byte[] data = "Hello World".getBytes("UTF-8");
        byte[] zipped = zip("short", data);
        assertEquals(Arrays.toString(data), Arrays.toString(ZipInputStreamTest.unzip("short", zipped)));
    }

    // http://b/3181430 --- a sign-extension bug on CRCs with the top bit set.
    public void test3181430() throws IOException {
        byte[] data = new byte[1]; // CRC32({ 0 }) == 0xd202ef8d
        byte[] zipped = zip("z", data);
        assertEquals(Arrays.toString(data), Arrays.toString(ZipInputStreamTest.unzip("z", zipped)));
    }

    public void testLongMessage() throws IOException {
        byte[] data = new byte[1024 * 1024];
        new Random().nextBytes(data);
        assertTrue(Arrays.equals(data, ZipInputStreamTest.unzip("r", zip("r", data))));
    }

    public static byte[] zip(String name, byte[] bytes) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        ZipOutputStream zippedOut = new ZipOutputStream(bytesOut);

        ZipEntry entry = new ZipEntry(name);
        zippedOut.putNextEntry(entry);
        zippedOut.write(bytes);
        zippedOut.closeEntry();

        zippedOut.close();
        return bytesOut.toByteArray();
    }

    /**
     * Reference implementation does allow writing of an empty zip using a {@link ZipOutputStream}.
     *
     * See JDK-6440786.
     */
    public void testCreateEmpty() throws IOException {
        File result = File.createTempFile("ZipFileTest", "zip");
        ZipOutputStream out =
                new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(result)));
        out.close();

        // Verify that the empty zip file can be read back using ZipInputStream.
        try (ZipInputStream in = new ZipInputStream(
            new BufferedInputStream(new FileInputStream(result)))) {
            assertNull(in.getNextEntry());
        }
    }

    /** Regression test for null comment causing a NullPointerException during write. */
    public void testNullComment() throws IOException {
        try (ZipOutputStream out = new ZipOutputStream(new ByteArrayOutputStream())) {
            out.setComment(null);
            out.putNextEntry(new ZipEntry("name"));
            out.write(new byte[1]);
            out.closeEntry();
            out.finish();
        }
    }

    /**
     * Test {@link ZipOutputStream#putNextEntry(ZipEntry)} that the current time will be used
     * if the entry has no set modification time.
     */
    public void testPutNextEntryUsingCurrentTime() throws IOException {
        // Zip file truncates time into 1s (before 1980) or 2s precision.
        long timeBeforeZip = System.currentTimeMillis() / 2000 * 2000;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipOutputStream out = new ZipOutputStream(bos)) {
            ZipEntry entryWithoutExplicitTime = new ZipEntry("name");
            // We do not set a time on the entry. We expect ZipOutputStream to use the current
            // system clock value.
            out.putNextEntry(entryWithoutExplicitTime);
            out.closeEntry();
            out.finish();
        }
        // timeAfterZip will normally be rounded down to  1 / 2 seconds boundary as well, but this
        // test accepts either exact or rounded-down values because the rounding behavior is outside
        // of this test's purpose
        long timeAfterZip = System.currentTimeMillis();

        // Read it back, and check the modification time is almost the system clock value
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            ZipEntry entry = zis.getNextEntry();
            assertEquals("name", entry.getName());
            assertTrue(timeBeforeZip <= entry.getTime());
            assertTrue(timeAfterZip >= entry.getTime());
        }
    }
}
