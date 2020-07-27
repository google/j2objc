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

/* J2ObjC modified.
import android.system.OsConstants; */
import static libcore.io.OsConstants.SEEK_CUR;
import libcore.io.Libcore;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public final class ZipFileTest extends AbstractZipFileTest {

    @Override
    protected ZipOutputStream createZipOutputStream(OutputStream wrapped) {
        return new ZipOutputStream(wrapped);
    }

    // http://b/30407219
    public void testZipFileOffsetNeverChangesAfterInit() throws Exception {
        final File f = createTemporaryZipFile();
        writeEntries(createZipOutputStream(new BufferedOutputStream(new FileOutputStream(f))),
                2 /* number of entries */, 1024 /* entry size */, true /* setEntrySize */);

        ZipFile zipFile = new ZipFile(f);
        FileDescriptor fd = new FileDescriptor();
        fd.setInt$(zipFile.getFileDescriptor());

        long initialOffset = Libcore.os.lseek(fd, 0, SEEK_CUR);

        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        assertOffset(initialOffset, fd);

        // Get references to the two elements in the file.
        ZipEntry entry1 = entries.nextElement();
        ZipEntry entry2 = entries.nextElement();
        assertFalse(entries.hasMoreElements());
        assertOffset(initialOffset, fd);

        InputStream is1 = zipFile.getInputStream(entry1);
        assertOffset(initialOffset, fd);
        is1.read(new byte[256]);
        assertOffset(initialOffset, fd);
        is1.close();

        assertNotNull(zipFile.getEntry(entry2.getName()));
        assertOffset(initialOffset, fd);

        zipFile.close();
    }

    private static void assertOffset(long initialOffset, FileDescriptor fd) throws Exception {
        long currentOffset = Libcore.os.lseek(fd, 0, SEEK_CUR);
        assertEquals(initialOffset, currentOffset);
    }

    // b/31077136
    public void test_FileNotFound() throws Exception {
        File nonExistentFile = new File("fileThatDefinitelyDoesntExist.zip");
        assertFalse(nonExistentFile.exists());

        try (ZipFile zipFile = new ZipFile(nonExistentFile, ZipFile.OPEN_READ)) {
            fail();
        } catch(FileNotFoundException expected) {}
    }
}
