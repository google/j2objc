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

package libcore.java.nio.channels;

import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
/* J2ObjC removed: mockito not supported
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
 */

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;
import libcore.io.IoUtils;
/* J2ObjC removed: libcore.junit not supported
import libcore.junit.junit3.TestCaseWithRules;
import libcore.junit.util.ResourceLeakageDetector;
import libcore.junit.util.ResourceLeakageDetector.LeakageDetectorRule;
 */
import org.junit.Rule;

/* J2ObjC removed: TestCaseWithRules not supported
public class FileChannelTest extends TestCaseWithRules {
 */
public class FileChannelTest extends TestCase {

    /* J2ObjC removed: ResourceLeakageDetector not supported
    @Rule
    public LeakageDetectorRule guardRule = ResourceLeakageDetector.getRule();
     */

    public void testReadOnlyByteArrays() throws Exception {
        ByteBuffer readOnly = ByteBuffer.allocate(1).asReadOnlyBuffer();
        File tmp = File.createTempFile("FileChannelTest", "tmp");

        // You can't read into a read-only buffer...
        FileChannel fc = new FileInputStream(tmp).getChannel();
        try {
            fc.read(readOnly);
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            fc.read(new ByteBuffer[] { readOnly });
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            fc.read(new ByteBuffer[] { readOnly }, 0, 1);
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            fc.read(readOnly, 0L);
            fail();
        } catch (IllegalArgumentException expected) {
        }
        fc.close();


        // But you can write from a read-only buffer...
        fc = new FileOutputStream(tmp).getChannel();
        fc.write(readOnly);
        fc.write(new ByteBuffer[] { readOnly });
        fc.write(new ByteBuffer[] { readOnly }, 0, 1);
        fc.write(readOnly, 0L);
        fc.close();
    }

    public void test_readv() throws Exception {
        File tmp = File.createTempFile("FileChannelTest", "tmp");
        FileChannel fc = new FileOutputStream(tmp).getChannel();
        fc.write(ByteBuffer.wrap("abcdABCD".getBytes("US-ASCII")));
        fc.close();
        // Check that both direct and non-direct buffers work.
        fc = new FileInputStream(tmp).getChannel();
        ByteBuffer[] buffers = new ByteBuffer[] { ByteBuffer.allocateDirect(4), ByteBuffer.allocate(4) };
        assertEquals(8, fc.read(buffers));
        fc.close();
        assertEquals(8, buffers[0].limit() + buffers[1].limit());
        byte[] bytes = new byte[4];
        buffers[0].flip();
        buffers[0].get(bytes);
        assertEquals("abcd", new String(bytes, "US-ASCII"));
        buffers[1].flip();
        buffers[1].get(bytes);
        assertEquals("ABCD", new String(bytes, "US-ASCII"));
    }

    public void test_writev() throws Exception {
        File tmp = File.createTempFile("FileChannelTest", "tmp");
        FileChannel fc = new FileOutputStream(tmp).getChannel();
        // Check that both direct and non-direct buffers work.
        ByteBuffer[] buffers = new ByteBuffer[] { ByteBuffer.allocateDirect(4), ByteBuffer.allocate(4) };
        buffers[0].put("abcd".getBytes("US-ASCII")).flip();
        buffers[1].put("ABCD".getBytes("US-ASCII")).flip();
        assertEquals(8, fc.write(buffers));
        fc.close();
        assertEquals(8, tmp.length());
        assertEquals("abcdABCD", new String(IoUtils.readFileAsString(tmp.getPath())));
    }

    public void test_append() throws Exception {
        File tmp = File.createTempFile("FileChannelTest", "tmp");
        FileOutputStream fos = new FileOutputStream(tmp, true);
        FileChannel fc = fos.getChannel();

        fc.write(ByteBuffer.wrap("hello".getBytes("US-ASCII")));
        fc.position(0);
        // The RI reports end of file
        assertEquals(5, fc.position());
        // ...but writes to the end of the file.
        fc.write(ByteBuffer.wrap(" world".getBytes("US-ASCII")));
        fos.close();

        assertEquals("hello world", new String(IoUtils.readFileAsString(tmp.getPath())));
    }

    public void test_position_writeAddsPadding() throws Exception {
        byte[] initialBytes = "12345".getBytes("US-ASCII");
        int initialFileSize = initialBytes.length; // 5
        FileChannel fc = createFileContainingBytes(initialBytes);

        int positionBeyondSize = 10;
        fc.position(positionBeyondSize);
        assertEquals(positionBeyondSize, fc.position());
        assertEquals(initialFileSize, fc.size());

        byte[] newBytes = "6789A".getBytes("US-ASCII");
        fc.write(ByteBuffer.wrap(newBytes));

        int expectedNewLength = positionBeyondSize + newBytes.length;
        assertEquals(expectedNewLength, fc.position());
        assertEquals(expectedNewLength, fc.size());

        fc.close();
    }

    public void test_truncate_greaterThanSizeWithPositionChange() throws Exception {
        byte[] initialBytes = "12345".getBytes("US-ASCII");
        int initialFileSize = initialBytes.length; // 5
        FileChannel fc = createFileContainingBytes(initialBytes);

        int initialPosition = 40;
        fc.position(initialPosition); // Should not affect the file size
        assertEquals(initialPosition, fc.position());
        assertEquals(initialFileSize, fc.size());

        // truncateArg < position, truncateArg > initialFileSize: Should not affect the file size
        // and should move the position.
        int truncateArg = 10;
        fc.truncate(truncateArg);  // Should not affect the file size, but should move the position.
        assertEquals(initialFileSize, fc.size());
        // The RI does not behave properly here, according to the docs for truncate(), position()
        // should now be the same as truncateArg.
        assertEquals(truncateArg, fc.position());

        fc.close();
    }

    public void test_truncate_greaterThanSizeWithoutPositionChange() throws Exception {
        byte[] initialBytes = "123456789A".getBytes("US-ASCII");
        int initialFileSize = initialBytes.length; // 10
        FileChannel fc = createFileContainingBytes(initialBytes);

        int initialPosition = 5;
        fc.position(initialPosition);
        assertEquals(initialPosition, fc.position());
        assertEquals(initialFileSize, fc.size());

        // truncateArg > position, truncateArg > initialFileSize: Should not affect the file size
        // and should not move the position.
        int truncateArg = 15;
        fc.truncate(truncateArg);
        assertEquals(initialFileSize, fc.size());
        assertEquals(initialPosition, fc.position());

        fc.close();
    }

    public void test_truncate_lessThanSizeWithPositionChange() throws Exception {
        byte[] initialBytes = "123456789A".getBytes("US-ASCII");
        int initialFileSize = initialBytes.length; // 10
        FileChannel fc = createFileContainingBytes(initialBytes);

        int initialPosition = initialFileSize;
        fc.position(initialPosition);
        assertEquals(initialPosition, fc.position());
        assertEquals(initialFileSize, fc.size());

        int truncateArg = 5;
        // truncateArg < initialPosition, truncateArg < initialFileSize: Should affect the file size
        // and should move the position.
        fc.truncate(truncateArg);
        assertEquals(truncateArg, fc.size());
        assertEquals(truncateArg, fc.position());

        fc.close();
    }

    public void test_truncate_lessThanSizeWithoutPositionChange() throws Exception {
        byte[] initialBytes = "123456789A".getBytes("US-ASCII");
        int initialFileSize = initialBytes.length; // 10
        FileChannel fc = createFileContainingBytes(initialBytes);

        int initialPosition = 4;
        fc.position(initialPosition);
        assertEquals(initialPosition, fc.position());
        assertEquals(initialFileSize, fc.size());

        int truncateArg = 5;
        // truncateArg > initialPosition, truncateArg < initialFileSize: Should affect the file size
        // and should not move the position.
        fc.truncate(truncateArg);
        assertEquals(truncateArg, fc.size());
        assertEquals(initialPosition, fc.position());

        fc.close();
    }

    // b/27351214
    public void test_close_fromFileDescriptor() throws Exception {
        // Create a valid FileDescriptor
        File tmp = File.createTempFile("FileChannelTest", "tmp");
        FileOutputStream fos = new FileOutputStream(tmp);
        FileDescriptor fd = fos.getFD();
        assertTrue(fd.valid());

        // Create FileOutputStream from FileDescriptor
        FileOutputStream fosFromFd = new FileOutputStream(fd);
        // Create FileChannel from FileOutputStream created from FileDescriptor
        FileChannel fc = fosFromFd.getChannel();

        // Invalidate FileDescriptor
        fos.close();
        assertFalse(fd.valid());

        // Close FileOutputStream and therefore close the FileChannel.
        // Without the fix for b/27351214 this will throw an exception
        // due to channel preClosing the file descriptor after it's
        // closed.
        fosFromFd.close();
    }

    /* J2ObjC removed: ResourceLeakageDetector not supported
    public void test_closeGuardSupport_open_without_append() throws IOException {
        File tmpFile = File.createTempFile("file", "txt");
        try (FileInputStream fis = new FileInputStream(tmpFile)) {
            try (FileChannel fc = fis.getChannel()) {
                guardRule.assertUnreleasedResourceCount(fc, 1);
            }
        }
    }
     */

    /* J2ObjC removed: ResourceLeakageDetector not supported
    public void test_closeGuardSupport_open_with_append() throws IOException {
        File tmpFile = File.createTempFile("file", "txt");
        try (FileOutputStream fos = new FileOutputStream(tmpFile)) {
            try (FileChannel fc = fos.getChannel()) {
                guardRule.assertUnreleasedResourceCount(fc, 1);
            }
        }
    }
     */

    private static FileChannel createFileContainingBytes(byte[] bytes) throws IOException {
        File tmp = File.createTempFile("FileChannelTest", "tmp");
        FileOutputStream fos = new FileOutputStream(tmp, true);
        FileChannel fc = fos.getChannel();
        fc.write(ByteBuffer.wrap(bytes));
        fc.close();

        assertEquals(bytes.length, tmp.length());

        fc = new RandomAccessFile(tmp, "rw").getChannel();
        assertEquals(bytes.length, fc.size());

        return fc;
    }

    /**
     * The test verifies that FileChannel#open(Path, Set<OpenOption>, FileAttribute ...) returns the
     * same object returned by #newFileChannel(Path, Set<OpenOption>, FileAttribute ...) method
     * in given Paths's FileSystemProvider.
     */
    /* J2ObjC removed: ResourceLeakageDetector not supported
    public void test_open_Path_Set_FileAttributes() throws IOException {
        Path mockPath = mock(Path.class);
        FileSystem mockFileSystem = mock(FileSystem.class);
        FileSystemProvider mockFileSystemProvider = mock(FileSystemProvider.class);
        FileChannel mockFileChannel = mock(FileChannel.class);

        FileAttribute mockFileAttribute1 = mock(FileAttribute.class);
        FileAttribute mockFileAttribute2 = mock(FileAttribute.class);

        Set<StandardOpenOption> standardOpenOptions = new HashSet<>();
        standardOpenOptions.add(READ);
        standardOpenOptions.add(WRITE);

        when(mockPath.getFileSystem()).thenReturn(mockFileSystem);
        when(mockFileSystem.provider()).thenReturn(mockFileSystemProvider);
        when(mockFileSystemProvider.newFileChannel(mockPath, standardOpenOptions,
                mockFileAttribute1, mockFileAttribute2)).thenReturn(mockFileChannel);

        assertEquals(mockFileChannel, FileChannel.open(mockPath, standardOpenOptions,
                mockFileAttribute1, mockFileAttribute2));
    }
     */

    /**
     * The test verifies that FileChannel#open(Path, OpenOption ...) returns the
     * same object returned by #newFileChannel(Path, OpenOption ...) method
     * in given Paths's FileSystemProvider.
     */
    /* J2ObjC removed: ResourceLeakageDetector not supported
    public void test_open_Path_OpenOptions() throws IOException {

        Path mockPath = mock(Path.class);
        FileSystem mockFileSystem = mock(FileSystem.class);
        FileSystemProvider mockFileSystemProvider = mock(FileSystemProvider.class);
        FileChannel mockFileChannel = mock(FileChannel.class);

        Set<StandardOpenOption> standardOpenOptions = new HashSet<>();
        standardOpenOptions.add(READ);
        standardOpenOptions.add(WRITE);

        when(mockPath.getFileSystem()).thenReturn(mockFileSystem);
        when(mockFileSystem.provider()).thenReturn(mockFileSystemProvider);
        when(mockFileSystemProvider.newFileChannel(mockPath, standardOpenOptions))
                .thenReturn(mockFileChannel);

        assertEquals(mockFileChannel, FileChannel.open(mockPath, READ, WRITE));
    }
     */
}
