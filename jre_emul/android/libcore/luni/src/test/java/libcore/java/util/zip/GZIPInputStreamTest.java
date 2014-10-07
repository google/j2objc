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
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import junit.framework.TestCase;

public final class GZIPInputStreamTest extends TestCase {

    private static final byte[] HELLO_WORLD_GZIPPED = new byte[] {
        31, -117, 8, 0, 0, 0, 0, 0, 0, 0, -13, 72, -51, -55, -55, 87, 8, -49,
        47, -54, 73, 1, 0, 86, -79, 23, 74, 11, 0, 0, 0
    };

    public void testShortMessage() throws IOException {
        assertEquals("Hello World", new String(gunzip(HELLO_WORLD_GZIPPED), "UTF-8"));
    }

    public void testLongMessage() throws IOException {
        byte[] data = new byte[1024 * 1024];
        new Random().nextBytes(data);
        assertTrue(Arrays.equals(data, gunzip(GZIPOutputStreamTest.gzip(data))));
    }

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

        try {
            gunzip(data);
            fail();
        } catch (EOFException expected) {
        }

        // Copy just the header from HELLO_WORLD_GZIPPED so that our input
        // stream becomes one complete member + a header member.
        data = new byte[length  + 18];
        System.arraycopy(HELLO_WORLD_GZIPPED, 0, data, 0, length);
        System.arraycopy(HELLO_WORLD_GZIPPED, 0, data, length, 18);

        try {
            gunzip(data);
            fail();
        } catch (EOFException expected) {
        }
    }

    public static byte[] gunzip(byte[] bytes) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        InputStream in = new GZIPInputStream(bis);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count;
        while ((count = in.read(buffer)) != -1) {
            out.write(buffer, 0, count);
        }

        byte[] outArray = out.toByteArray();
        in.close();

        return outArray;
    }
}
