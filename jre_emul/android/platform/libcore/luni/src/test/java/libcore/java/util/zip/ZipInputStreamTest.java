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

import libcore.io.Streams;
import tests.support.resource.Support_Resources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
/* J2ObjC removed: not supported by Junit 4.11 (https://github.com/google/j2objc/issues/1318).
import libcore.junit.junit3.TestCaseWithRules;
import libcore.junit.util.ResourceLeakageDetector; */
import org.junit.Rule;
import org.junit.rules.TestRule;

public final class ZipInputStreamTest extends junit.framework.TestCase /* J2ObjC removed: TestCaseWithRules */ {
    /* J2ObjC removed: not supported by Junit 4.11 (https://github.com/google/j2objc/issues/1318).
    @Rule
    public TestRule guardRule = ResourceLeakageDetector.getRule(); */

    public void testShortMessage() throws IOException {
        byte[] data = "Hello World".getBytes("UTF-8");
        byte[] zipped = ZipOutputStreamTest.zip("short", data);
        assertEquals(Arrays.toString(data), Arrays.toString(unzip("short", zipped)));
    }

    public void testLongMessage() throws IOException {
        byte[] data = new byte[1024 * 1024];
        new Random().nextBytes(data);
        assertTrue(Arrays.equals(data, unzip("r", ZipOutputStreamTest.zip("r", data))));
    }

    public static byte[] unzip(String name, byte[] bytes) throws IOException {
        ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(bytes));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        ZipEntry entry = in.getNextEntry();
        assertEquals(name, entry.getName());

        byte[] buffer = new byte[1024];
        int count;
        while ((count = in.read(buffer)) != -1) {
            out.write(buffer, 0, count);
        }

        assertNull(in.getNextEntry()); // There's only one entry in the Zip files we create.

        in.close();
        return out.toByteArray();
    }

    /**
     * Reference implementation allows reading of empty zip using a {@link ZipInputStream}.
     */
    public void testReadEmpty() throws IOException {
        InputStream emptyZipIn = Support_Resources.getStream("java/util/zip/EmptyArchive.zip");
        ZipInputStream in = new ZipInputStream(emptyZipIn);
        try {
            ZipEntry entry = in.getNextEntry();
            assertNull("An empty zip has no entries", entry);
        } finally {
            in.close();
        }
    }

    // NOTE: Using octal because it's easiest to use "hexdump -b" to dump file contents.
    private static final byte[] INCOMPLETE_ZIP = new byte[] {
            0120, 0113, 0003, 0004, 0024, 0000, 0010, 0010, 0010, 0000, 0002, 0035, (byte) 0330,
            0106, 0000, 0000, 0000, 0000, 0000, 0000, 0000, 0000, 0000, 0000, 0000, 0000, 0013,
            0000, 0000, 0000, 0146, 0157, 0157, 0057, 0142, 0141, 0162, 0056, 0160, 0156, 0147 };

    // http://b//21846904
    public void testReadOnIncompleteStream() throws Exception {
        ZipInputStream zi = new ZipInputStream(new ByteArrayInputStream(INCOMPLETE_ZIP));
        ZipEntry ze = zi.getNextEntry();

        // read() and closeEntry() must throw IOExceptions to indicate that
        // the stream is corrupt. The bug above reported that they would loop
        // forever.
        try {
            zi.read(new byte[1024], 0, 1024);
            fail();
        } catch (IOException expected) {
        }

        try {
            zi.closeEntry();
            fail();
        } catch (IOException expected) {
        }

        zi.close();
    }

    public void testAvailable() throws Exception {
        // NOTE: We don't care about the contents of any of these entries as long as they're
        // not empty.
        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(
                zip(new String[] { "foo", "bar", "baz" }, new byte[] { 0, 0, 0, 1, 1, 1 })));

        assertEquals(1, zis.available());
        zis.getNextEntry();
        assertEquals(1, zis.available());
        zis.closeEntry();
        // On Android M and below, this call would return "1". That seems a bit odd given that the
        // contract for available states that we should return 1 if there are any bytes left to read
        // from the "current" entry.
        assertEquals(0, zis.available());

        // There shouldn't be any bytes left to read if the entry is fully consumed...
        zis.getNextEntry();
        Streams.readFullyNoClose(zis);
        assertEquals(0, zis.available());

        // ... or if the entry is fully skipped over.
        zis.getNextEntry();
        zis.skip(Long.MAX_VALUE);
        assertEquals(0, zis.available());

        // There are no entries left in the file, so there whould be nothing left to read.
        assertNull(zis.getNextEntry());
        assertEquals(0, zis.available());

        zis.close();
    }

    private static final byte[] ZIP_WITH_DATA_DESCRIPTOR = new byte[] {
(byte) 80, 75, 3, 4, 10, 0, 8, 0, 0, 0, -51, 90, -121, 80, -20, 62, -84, -103, 2, 0, 0, 0, 2, 0, 0, 0, 8, 0, 28, 0, 116, 101, 115, 116, 46, 116, 120, 116, 85, 84, 9, 0, 3, 97, 84, -116, 94, 102, 84, -116, 94, 117, 120, 11, 0, 1, 4, -119, 66, 0, 0, 4, 83, 95, 1, 0, 72, 10, 80, 75, 7, 8, -20, 62, -84, -103, 2, 0, 0, 0, 2, 0, 0, 0, 80, 75, 1, 2, 30, 3, 10, 0, 0, 0, 0, 0, -51, 90, -121, 80, -20, 62, -84, -103, 2, 0, 0, 0, 2, 0, 0, 0, 8, 0, 24, 0, 0, 0, 0, 0, 1, 0, 0, 0, -92, -127, 0, 0, 0, 0, 116, 101, 115, 116, 46, 116, 120, 116, 85, 84, 5, 0, 3, 97, 84, -116, 94, 117, 120, 11, 0, 1, 4, -119, 66, 0, 0, 4, 83, 95, 1, 0, 80, 75, 5, 6, 0, 0, 0, 0, 1, 0, 1, 0, 78, 0, 0, 0, 84, 0, 0, 0, 0, 0 };

    public void testDataDescriptorOnStoredEntry() throws Exception {
        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(
                ZIP_WITH_DATA_DESCRIPTOR));

        ZipEntry entry = zis.getNextEntry();
        assertEquals("test.txt", entry.getName());

        zis.close();
    }

    private static byte[] zip(String[] names, byte[] bytes) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        ZipOutputStream zippedOut = new ZipOutputStream(bytesOut);

        for (String name : names) {
            ZipEntry entry = new ZipEntry(name);
            zippedOut.putNextEntry(entry);
            zippedOut.write(bytes);
            zippedOut.closeEntry();
        }

        zippedOut.close();
        return bytesOut.toByteArray();
    }
}
