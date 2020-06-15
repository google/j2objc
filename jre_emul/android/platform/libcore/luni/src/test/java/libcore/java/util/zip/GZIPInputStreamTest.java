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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import libcore.io.IoUtils;
import libcore.io.Streams;
/* J2ObjC removed: not supported by Junit 4.11 (https://github.com/google/j2objc/issues/1318).
import libcore.junit.junit3.TestCaseWithRules;
import libcore.junit.util.ResourceLeakageDetector; */
import org.junit.Rule;
import org.junit.rules.TestRule;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public final class GZIPInputStreamTest extends junit.framework.TestCase /* J2ObjC removed: TestCaseWithRules */ {
    /* J2ObjC removed: not supported by Junit 4.11 (https://github.com/google/j2objc/issues/1318).
    @Rule
    public TestRule resourceLeakageDetectorRule = ResourceLeakageDetector.getRule(); */

    private static final byte[] HELLO_WORLD_GZIPPED = new byte[] {
        31, -117, 8, 0, 0, 0, 0, 0, 0, 0,  // 10 byte header
        -13, 72, -51, -55, -55, 87, 8, -49, 47, -54, 73, 1, 0, 86, -79, 23, 74, 11, 0, 0, 0  // data
    };

    /**
     * This is the same as the above, except that the 4th header byte is 2 (FHCRC flag)
     * and the 2 bytes after the header make up the CRC.
     *
     * Constructed manually because none of the commonly used tools appear to emit header CRCs.
     */
    private static final byte[] HELLO_WORLD_GZIPPED_WITH_HEADER_CRC = new byte[] {
        31, -117, 8, 2, 0, 0, 0, 0, 0, 0,  // 10 byte header
        29, 38, // 2 byte CRC.
        -13, 72, -51, -55, -55, 87, 8, -49, 47, -54, 73, 1, 0, 86, -79, 23, 74, 11, 0, 0, 0  // data
    };

    /*(
     * This is the same as {@code HELLO_WORLD_GZIPPED} except that the 4th header byte is 4
     * (FEXTRA flag) and that the 8 bytes after the header make up the extra.
     *
     * Constructed manually because none of the commonly used tools appear to emit header CRCs.
     */
    private static final byte[] HELLO_WORLD_GZIPPED_WITH_EXTRA = new byte[] {
        31, -117, 8, 4, 0, 0, 0, 0, 0, 0,  // 10 byte header
        6, 0, 4, 2, 4, 2, 4, 2,  // 2 byte extra length + 6 byte extra.
        -13, 72, -51, -55, -55, 87, 8, -49, 47, -54, 73, 1, 0, 86, -79, 23, 74, 11, 0, 0, 0  // data
    };

    public void testShortMessage() throws IOException {
        assertEquals("Hello World", new String(gunzip(HELLO_WORLD_GZIPPED), "UTF-8"));
    }

    public void testShortMessageWithCrc() throws IOException {
        assertEquals("Hello World", new String(gunzip(HELLO_WORLD_GZIPPED_WITH_HEADER_CRC), "UTF-8"));
    }

    public void testShortMessageWithHeaderExtra() throws IOException {
        assertEquals("Hello World", new String(gunzip(HELLO_WORLD_GZIPPED_WITH_EXTRA), "UTF-8"));
    }

    public void testLongMessage() throws IOException {
        byte[] data = new byte[1024 * 1024];
        new Random().nextBytes(data);
        assertTrue(Arrays.equals(data, gunzip(GZIPOutputStreamTest.gzip(data))));
    }

    /** http://b/3042574 GzipInputStream.skip() causing CRC failures */
    public void testSkip() throws IOException {
        byte[] data = new byte[1024 * 1024];
        byte[] gzipped = GZIPOutputStreamTest.gzip(data);
        GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(gzipped));
        long totalSkipped = 0;

        long count;
        do {
            count = in.skip(Long.MAX_VALUE);
            totalSkipped += count;
        } while (count > 0);

        assertEquals(data.length, totalSkipped);
        in.close();
    }

    // https://code.google.com/p/android/issues/detail?id=63873
    public void testMultipleMembers() throws Exception {
        final int length = HELLO_WORLD_GZIPPED.length;
        byte[] data = new byte[length * 2];
        System.arraycopy(HELLO_WORLD_GZIPPED, 0, data, 0, length);
        System.arraycopy(HELLO_WORLD_GZIPPED, 0, data, length, length);

        assertEquals("Hello WorldHello World", new String(gunzip(data), "UTF-8"));
    }

    // https://code.google.com/p/android/issues/detail?id=63873
    public void testTrailingNonGzipData() throws Exception {
        final int length = HELLO_WORLD_GZIPPED.length;
        // 50 bytes of 0s at the end of the first message.
        byte[] data = new byte[length  + 50];
        System.arraycopy(HELLO_WORLD_GZIPPED, 0, data, 0, length);
        assertEquals("Hello World", new String(gunzip(data), "UTF-8"));
    }

    // https://code.google.com/p/android/issues/detail?id=63873
    //
    // Differences from the RI: Tests show the RI ignores *some* types of partial
    // data but not others and this test case fails as a result. Our implementation
    // will throw if it sees the gzip magic sequence at the end of a member
    // but malformed / invalid data after.
    public void testTrailingHeaderAndPartialMember() throws Exception {
        final int length = HELLO_WORLD_GZIPPED.length;
        // Copy just the header from HELLO_WORLD_GZIPPED so that our input
        // stream becomes one complete member + a header member.
        byte[] data = new byte[length  + 10];
        System.arraycopy(HELLO_WORLD_GZIPPED, 0, data, 0, length);
        System.arraycopy(HELLO_WORLD_GZIPPED, 0, data, length, 10);

        // The trailing data is ignored since the amount of data available is
        // less than the size of a header + trailer (which is the absolute minimum required).
        byte[] unzipped = gunzip(data);
        assertEquals("Hello World", new String(unzipped, StandardCharsets.UTF_8));

        // Must fail here because the data contains a full header and partial data.
        data = new byte[length*2 - 4];
        System.arraycopy(HELLO_WORLD_GZIPPED, 0, data, 0, length);
        System.arraycopy(HELLO_WORLD_GZIPPED, 0, data, length, length - 4);

        try {
            gunzip(data);
            fail();
        } catch (EOFException expected) {
        }
    }

    // https://code.google.com/p/android/issues/detail?id=66409
    public void testMultipleMembersWithCustomBufferSize() throws Exception {
        final int[] memberSizes = new int[] { 1000, 2000 };

        // We don't care what the exact contents of this file is, as long
        // as the file has multiple members, and that the (compressed) size of
        // the second member is larger than the size of the input buffer.
        //
        // There's no way to achieve this for a GZIPOutputStream so we generate
        // pseudo-random sequence of bytes and assert that they don't compress
        // well.
        final Random r = new Random(10);
        byte[] bytes = new byte[3000];
        r.nextBytes(bytes);

        File f = File.createTempFile("GZIPInputStreamTest", ".gzip");
        int offset = 0;
        for (int size : memberSizes) {
            GZIPOutputStream gzos = null;
            try {
                FileOutputStream fos = new FileOutputStream(f, true /* append */);
                gzos = new GZIPOutputStream(fos, size + 1);
                gzos.write(bytes, offset, size);
                offset += size;
                gzos.finish();
            } finally {
                IoUtils.closeQuietly(gzos);
            }
        }

        assertTrue(f.length() > 2048);

        FileInputStream fis = new FileInputStream(f);
        GZIPInputStream gzip = null;
        try {
            gzip = new GZIPInputStream(fis, memberSizes[0]);
            byte[] unzipped = Streams.readFully(gzip);
            assertTrue(Arrays.equals(bytes, unzipped));
        } finally {
            IoUtils.closeQuietly(gzip);
        }
    }

    public void testNonGzipData() {
        byte[] data = new byte[100];
        try (InputStream is = new GZIPInputStream(new ByteArrayInputStream(data), data.length)) {
            fail();
        } catch (IOException expected) {
            // Zeroes do not match the GZIPInputStream.GZIP_MAGIC number.
            assertEquals("Not in GZIP format", expected.getMessage());
        }
    }

    /**
     * Test a openJdk8 fix for case where GZIPInputStream.readTrailer may accidently
     * close the input stream if trailing bytes looks "close" enough. Wrapping GZIP in
     * a ZipOutputStream will do that. */
    public void testNoCloseInReadTrailerDueToRead() throws IOException {
        final int numBytes = 128;
        byte[] data = new byte[numBytes];
        final boolean[] closedHolder = new boolean[]{false};

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            zipOutputStream.putNextEntry(new ZipEntry("entry1"));
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(zipOutputStream)) {
                gzipOutputStream.write(data);
            }
        }

        final byte[] compressedData = byteArrayOutputStream.toByteArray();
        InputStream byteArrayInputStream =
            new ByteArrayInputStream(compressedData) {
                @Override
                public void close() throws IOException {
                    closedHolder[0] = true;
                }
            };

        try (ZipInputStream zipInputStream = new ZipInputStream(byteArrayInputStream)) {
            zipInputStream.getNextEntry();
            try (InputStream in = new GZIPInputStream(zipInputStream)) {
                assertEquals(numBytes, in.skip(numBytes+1));
                assertFalse(closedHolder[0]);
            }
            assertTrue(closedHolder[0]);
        }
    }

    public static byte[] gunzip(byte[] bytes) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try (InputStream in = new GZIPInputStream(bis)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int count;
            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }

            return out.toByteArray();
        }
    }
}
