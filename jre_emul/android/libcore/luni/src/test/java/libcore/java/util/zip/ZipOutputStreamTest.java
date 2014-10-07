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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import junit.framework.TestCase;

public final class ZipOutputStreamTest extends TestCase {
    public void testShortMessage() throws IOException {
        byte[] data = "Hello World".getBytes("UTF-8");
        byte[] zipped = zip("short", data);
        assertEquals(Arrays.toString(data), Arrays.toString(ZipInputStreamTest.unzip("short", zipped)));
    }

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
}
