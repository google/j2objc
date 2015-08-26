/*
 * Copyright (C) 2015 The Android Open Source Project
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
 * limitations under the License
 */

package libcore.java.util.zip;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public final class Zip64FileTest extends AbstractZipFileTest {
    @Override
    protected ZipOutputStream createZipOutputStream(OutputStream wrapped) {
        return new ZipOutputStream(wrapped, true /* forceZip64 */);
    }

    public void testZip64Support_largeNumberOfEntries() throws IOException {
        final File file = createZipFile(65550, 2, false /* setEntrySize */);
        ZipFile zf = null;
        try {
            zf = new ZipFile(file);
            assertEquals(65550, zf.size());

            Enumeration<? extends ZipEntry> entries = zf.entries();
            assertTrue(entries.hasMoreElements());
            ZipEntry ze = entries.nextElement();
            assertEquals(2, ze.getSize());
        } finally {
            if (zf != null) {
                zf.close();
            }
        }
    }

    public void testZip64Support_totalLargerThan4G() throws IOException {
        final File file = createZipFile(5, 1073741824L, false /* setEntrySize */);
        ZipFile zf = null;
        try {
            zf = new ZipFile(file);
            assertEquals(5, zf.size());
            Enumeration<? extends ZipEntry> entries = zf.entries();
            assertTrue(entries.hasMoreElements());
            ZipEntry ze = entries.nextElement();
            assertEquals(1073741824L, ze.getSize());
        } finally {
            if (zf != null) {
                zf.close();
            }
        }
    }

    public void testZip64Support_hugeEntry() throws IOException {
        try {
            createZipFile(1, 4294967410L, false /* setEntrySize */);
            fail();
        } catch (IOException expected) {
        }

        final File file = createZipFile(1, 4294967410L, true /* setEntrySize */);
        ZipFile zf = null;
        try {
            zf = new ZipFile(file);
            assertEquals(1, zf.size());
            Enumeration<? extends ZipEntry> entries = zf.entries();
            assertTrue(entries.hasMoreElements());
            ZipEntry ze = entries.nextElement();
            assertEquals(4294967410L, ze.getSize());
        } finally {
            if (zf != null) {
                zf.close();
            }
        }
    }

    private File createZipFile(int numEntries, long entrySize, boolean setEntrySize)
            throws IOException {
        File file = createTemporaryZipFile();
        // Don't force a 64 bit zip file to test that our heuristics work.
        ZipOutputStream os = new ZipOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)));
        writeEntries(os, numEntries, entrySize, setEntrySize);
        return file;
    }
}
