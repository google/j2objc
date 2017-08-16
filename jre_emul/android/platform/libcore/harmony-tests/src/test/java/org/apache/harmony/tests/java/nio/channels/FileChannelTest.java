/* Licensed to the Apache Software Foundation (ASF) under one or more
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

package org.apache.harmony.tests.java.nio.channels;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.OverlappingFileLockException;
import java.nio.channels.Pipe;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Arrays;

import junit.framework.TestCase;

import libcore.io.IoUtils;

public class FileChannelTest extends TestCase {

    private static final int CAPACITY = 100;

    private static final int LIMITED_CAPACITY = 2;

    private static final int TIME_OUT = 10000;

    private static final String CONTENT = "MYTESTSTRING needs to be a little long";

    private static final byte[] TEST_BYTES;

    private static final byte[] CONTENT_AS_BYTES;

    private static final int CONTENT_AS_BYTES_LENGTH;

    static {
        try {
            TEST_BYTES = "test".getBytes("iso8859-1");
            CONTENT_AS_BYTES = CONTENT.getBytes("iso8859-1");
            CONTENT_AS_BYTES_LENGTH = CONTENT_AS_BYTES.length;
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    private static final int CONTENT_LENGTH = CONTENT.length();

    private FileChannel readOnlyFileChannel;

    private FileChannel readOnlyFileChannel2;

    private FileChannel writeOnlyFileChannel;

    private FileChannel writeOnlyFileChannel2;

    private FileChannel readWriteFileChannel;

    private File fileOfReadOnlyFileChannel;

    private File fileOfWriteOnlyFileChannel;

    private File fileOfReadWriteFileChannel;

    private ReadableByteChannel readByteChannel;

    private WritableByteChannel writableByteChannel;

    private DatagramChannel datagramChannelSender;

    private DatagramChannel datagramChannelReceiver;

    private ServerSocketChannel serverSocketChannel;

    private SocketChannel socketChannelSender;

    private SocketChannel socketChannelReceiver;

    private Pipe pipe;

    // to read content from FileChannel
    private FileInputStream fis;

    private FileLock fileLock;

    protected void setUp() throws Exception {
        fileOfReadOnlyFileChannel = File.createTempFile(
                "File_of_readOnlyFileChannel", "tmp");
        fileOfReadOnlyFileChannel.deleteOnExit();
        fileOfWriteOnlyFileChannel = File.createTempFile(
                "File_of_writeOnlyFileChannel", "tmp");
        fileOfWriteOnlyFileChannel.deleteOnExit();
        fileOfReadWriteFileChannel = File.createTempFile(
                "File_of_readWriteFileChannel", "tmp");
        fileOfReadWriteFileChannel.deleteOnExit();
        fis = null;
        fileLock = null;
        readOnlyFileChannel = new FileInputStream(fileOfReadOnlyFileChannel)
                .getChannel();
        readOnlyFileChannel2 = new FileInputStream(fileOfReadOnlyFileChannel)
                .getChannel();
        writeOnlyFileChannel = new FileOutputStream(fileOfWriteOnlyFileChannel)
                .getChannel();
        writeOnlyFileChannel2 = new FileOutputStream(fileOfWriteOnlyFileChannel)
                .getChannel();
        readWriteFileChannel = new RandomAccessFile(fileOfReadWriteFileChannel,
                "rw").getChannel();
    }

    protected void tearDown() {
        IoUtils.closeQuietly(readOnlyFileChannel);
        IoUtils.closeQuietly(readOnlyFileChannel2);
        IoUtils.closeQuietly(writeOnlyFileChannel);
        IoUtils.closeQuietly(writeOnlyFileChannel2);
        IoUtils.closeQuietly(readWriteFileChannel);
        IoUtils.closeQuietly(fis);

        if (null != fileLock) {
            try {
                fileLock.release();
            } catch (IOException e) {
                // do nothing
            }
        }

        if (null != fileOfReadOnlyFileChannel) {
            fileOfReadOnlyFileChannel.delete();
        }
        if (null != fileOfWriteOnlyFileChannel) {
            fileOfWriteOnlyFileChannel.delete();
        }
        if (null != fileOfReadWriteFileChannel) {
            fileOfReadWriteFileChannel.delete();
        }

        IoUtils.closeQuietly(datagramChannelSender);
        IoUtils.closeQuietly(datagramChannelReceiver);
        IoUtils.closeQuietly(serverSocketChannel);
        IoUtils.closeQuietly(socketChannelSender);
        IoUtils.closeQuietly(socketChannelReceiver);
        if (null != pipe) {
            IoUtils.closeQuietly(pipe.source());
            IoUtils.closeQuietly(pipe.sink());
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#force(boolean)
     */
    public void test_forceJ() throws Exception {
        ByteBuffer writeBuffer = ByteBuffer.wrap(CONTENT_AS_BYTES);
        writeOnlyFileChannel.write(writeBuffer);
        writeOnlyFileChannel.force(true);

        byte[] readBuffer = new byte[CONTENT_AS_BYTES_LENGTH];
        fis = new FileInputStream(fileOfWriteOnlyFileChannel);
        fis.read(readBuffer);
        assertTrue(Arrays.equals(CONTENT_AS_BYTES, readBuffer));
    }

    /**
     * @tests java.nio.channels.FileChannel#force(boolean)
     */
    public void test_forceJ_closed() throws Exception {
        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.force(true);
            fail();
        } catch (ClosedChannelException expected) {
        }

        try {
            writeOnlyFileChannel.force(false);
            fail();
        } catch (ClosedChannelException expected) {
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#force(boolean)
     */
    public void test_forceJ_ReadOnlyChannel() throws Exception {
        // force on a read only file channel has no effect.
        readOnlyFileChannel.force(true);
        readOnlyFileChannel.force(false);
    }

    /**
     * @tests java.nio.channels.FileChannel#position()
     */
    public void test_position_Init() throws Exception {
        assertEquals(0, readOnlyFileChannel.position());
        assertEquals(0, writeOnlyFileChannel.position());
        assertEquals(0, readWriteFileChannel.position());
    }

    /**
     * @tests java.nio.channels.FileChannel#position()
     */
    public void test_position_ReadOnly() throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);

        assertEquals(0, readOnlyFileChannel.position());
        ByteBuffer readBuffer = ByteBuffer.allocate(CONTENT_LENGTH);
        readOnlyFileChannel.read(readBuffer);
        assertEquals(CONTENT_LENGTH, readOnlyFileChannel.position());
    }

    /**
     * Initializes test file.
     *
     * @param file
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void writeDataToFile(File file) throws FileNotFoundException,
            IOException {
        FileOutputStream fos = new FileOutputStream(file);
        try {
            fos.write(CONTENT_AS_BYTES);
        } finally {
            fos.close();
        }
    }

    /**
     * Initializes large test file.
     *
     * @param file the file to be written
     * @param size the content size to be written
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void writeLargeDataToFile(File file, int size) throws FileNotFoundException,
            IOException {
        FileOutputStream fos = new FileOutputStream(file);
        byte[] buf = new byte[size];

        try {
            // we don't care about content - just need a particular file size
            fos.write(buf);
        } finally {
            fos.close();
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#position()
     */
    public void test_position_WriteOnly() throws Exception {
        ByteBuffer writeBuffer = ByteBuffer.wrap(CONTENT_AS_BYTES);
        writeOnlyFileChannel.write(writeBuffer);
        assertEquals(CONTENT_LENGTH, writeOnlyFileChannel.position());
    }

    /**
     * @tests java.nio.channels.FileChannel#position()
     */
    public void test_position_ReadWrite() throws Exception {
        writeDataToFile(fileOfReadWriteFileChannel);

        assertEquals(0, readWriteFileChannel.position());
        ByteBuffer readBuffer = ByteBuffer.allocate(CONTENT_LENGTH);
        readWriteFileChannel.read(readBuffer);
        assertEquals(CONTENT_LENGTH, readWriteFileChannel.position());

        ByteBuffer writeBuffer = ByteBuffer.wrap(CONTENT_AS_BYTES);
        readWriteFileChannel.write(writeBuffer);
        assertEquals(CONTENT_LENGTH * 2, readWriteFileChannel.position());
    }

    /**
     * @tests java.nio.channels.FileChannel#position()
     */
    public void test_position_Closed() throws Exception {
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.position();
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException expected) {
        }

        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.position();
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException expected) {
        }

        readWriteFileChannel.close();
        try {
            readWriteFileChannel.position();
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException expected) {
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#position(long)
     */
    public void test_positionJ_Closed() throws Exception {
        final long POSITION = 100;

        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.position(POSITION);
            fail();
        } catch (ClosedChannelException expected) {
        }

        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.position(POSITION);
            fail();
        } catch (ClosedChannelException expected) {
        }

        readWriteFileChannel.close();
        try {
            readWriteFileChannel.position(POSITION);
            fail();
        } catch (ClosedChannelException expected) {
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#position(long)
     */
    public void test_positionJ_Negative() throws Exception {
        final long NEGATIVE_POSITION = -1;
        try {
            readOnlyFileChannel.position(NEGATIVE_POSITION);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            writeOnlyFileChannel.position(NEGATIVE_POSITION);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            readWriteFileChannel.position(NEGATIVE_POSITION);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#position(long)
     */
    public void test_positionJ_ReadOnly() throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);

        // set the position of the read only file channel to POSITION
        final int POSITION = 4;
        readOnlyFileChannel.position(POSITION);

        // reads the content left to readBuffer through read only file channel
        ByteBuffer readBuffer = ByteBuffer.allocate(CONTENT_LENGTH);
        int count = readOnlyFileChannel.read(readBuffer);
        assertEquals(CONTENT_LENGTH - POSITION, count);

        // asserts the content read is the part which stays beyond the POSITION
        readBuffer.flip();
        int i = POSITION;
        while (readBuffer.hasRemaining()) {
            assertEquals(CONTENT_AS_BYTES[i], readBuffer.get());
            i++;
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#position(long)
     */
    public void test_positionJ_WriteOnly() throws Exception {
        writeDataToFile(fileOfWriteOnlyFileChannel);

        // init data to write
        ByteBuffer writeBuffer = ByteBuffer.wrap(CONTENT_AS_BYTES);

        // set the position of the write only file channel to POSITION
        final int POSITION = 4;
        writeOnlyFileChannel.position(POSITION);

        // writes to the write only file channel
        writeOnlyFileChannel.write(writeBuffer);
        // force to write out.
        writeOnlyFileChannel.close();

        // gets the result of the write only file channel
        byte[] result = new byte[POSITION + CONTENT_LENGTH];
        fis = new FileInputStream(fileOfWriteOnlyFileChannel);
        fis.read(result);

        // constructs the expected result which has content[0... POSITION] plus
        // content[0...length()]
        byte[] expectedResult = new byte[POSITION + CONTENT_LENGTH];
        System.arraycopy(CONTENT_AS_BYTES, 0, expectedResult, 0, POSITION);
        System.arraycopy(CONTENT_AS_BYTES, 0, expectedResult, POSITION,
                CONTENT_LENGTH);

        // asserts result of the write only file channel same as expected
        assertTrue(Arrays.equals(expectedResult, result));
    }

    /**
     * @tests java.nio.channels.FileChannel#size()
     */
    public void test_size_Init() throws Exception {
        assertEquals(0, readOnlyFileChannel.size());
        assertEquals(0, writeOnlyFileChannel.size());
        assertEquals(0, readWriteFileChannel.size());
    }

    /**
     * @tests java.nio.channels.FileChannel#size()
     */
    public void test_size() throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);
        assertEquals(fileOfReadOnlyFileChannel.length(), readOnlyFileChannel
                .size());


        // REGRESSION test for read(ByteBuffer[], int, int) on special files
        try {
            FileChannel specialFile =
                new FileInputStream("/dev/zero").getChannel();
            assertEquals(0, specialFile.size());
            ByteBuffer buf = ByteBuffer.allocate(8);
            assertEquals(8, specialFile.read(buf));
            ByteBuffer[] bufs = { ByteBuffer.allocate(8) };
            assertEquals(8, specialFile.read(bufs, 0, 1));
            specialFile.close();
        } catch (FileNotFoundException e) {
            // skip test if special file doesn't exist
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#size()
     */
    public void test_size_Closed() throws Exception {
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.size();
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }

        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.size();
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }

        readWriteFileChannel.close();
        try {
            readWriteFileChannel.size();
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#truncate(long)
     */
    public void test_truncateJ_Closed() throws Exception {
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.truncate(0);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }

        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.truncate(0);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }

        readWriteFileChannel.close();
        try {
            readWriteFileChannel.truncate(-1);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#truncate(long)
     */
    public void test_truncateJ_IllegalArgument() throws Exception {
        // regression test for Harmony-941
        try {
            readOnlyFileChannel.truncate(-1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            writeOnlyFileChannel.truncate(-1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            readWriteFileChannel.truncate(-1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#truncate(long)
     */
    public void test_truncateJ_ReadOnly() throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);
        try {
            readOnlyFileChannel.truncate(readOnlyFileChannel.size());
            fail("should throw NonWritableChannelException.");
        } catch (NonWritableChannelException e) {
            // expected
        }

        try {
            readOnlyFileChannel.truncate(0);
            fail("should throw NonWritableChannelException.");
        } catch (NonWritableChannelException e) {
            // expected
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#truncate(long)
     */
    public void test_truncateJ() throws Exception {
        writeDataToFile(fileOfReadWriteFileChannel);

        int truncateLength = CONTENT_LENGTH + 2;
        assertEquals(readWriteFileChannel, readWriteFileChannel.truncate(truncateLength));
        assertEquals(CONTENT_LENGTH, fileOfReadWriteFileChannel.length());

        truncateLength = CONTENT_LENGTH;
        assertEquals(readWriteFileChannel, readWriteFileChannel
                .truncate(truncateLength));
        assertEquals(CONTENT_LENGTH, fileOfReadWriteFileChannel.length());

        truncateLength = CONTENT_LENGTH / 2;
        assertEquals(readWriteFileChannel, readWriteFileChannel
                .truncate(truncateLength));
        assertEquals(truncateLength, fileOfReadWriteFileChannel.length());
    }

    /**
     * @tests java.nio.channels.FileChannel#lock()
     */
    public void test_lock_Closed() throws Exception {
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.lock();
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException expected) {}

        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.lock();
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException expected) {}

        readWriteFileChannel.close();
        try {
            readWriteFileChannel.lock();
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException expected) {}
    }

    /**
     * @tests java.nio.channels.FileChannel#lock()
     */
    public void test_lock_NonWritable() throws Exception {
        try {
            readOnlyFileChannel.lock();
            fail("should throw NonWritableChannelException");
        } catch (NonWritableChannelException expected) {}
    }

    /**
     * @tests java.nio.channels.FileChannel#lock()
     */
    public void test_lock() throws Exception {
        fileLock = writeOnlyFileChannel.lock();
        assertTrue(fileLock.isValid());
        assertFalse(fileLock.isShared());
        assertSame(writeOnlyFileChannel, fileLock.channel());
        assertEquals(Long.MAX_VALUE, fileLock.size());
        assertEquals(0, fileLock.position());
    }

    /**
     * @tests java.nio.channels.FileChannel#lock()
     */
    public void test_lock_OverlappingException() throws Exception {
        fileLock = writeOnlyFileChannel.lock();
        assertTrue(fileLock.isValid());

        // Test the same channel cannot be locked twice.
        try {
            writeOnlyFileChannel.lock();
            fail("should throw OverlappingFileLockException");
        } catch (OverlappingFileLockException expected) {}

        // Test that a different channel on the same file also cannot be locked.
        try {
            writeOnlyFileChannel2.lock();
            fail("should throw OverlappingFileLockException");
        } catch (OverlappingFileLockException expected) {}
    }

    /**
     * @tests java.nio.channels.FileChannel#lock()
     */
    public void test_lock_After_Release() throws Exception {
        fileLock = writeOnlyFileChannel.lock();
        fileLock.release();
        // After release file lock can be obtained again.
        fileLock = writeOnlyFileChannel.lock();
        assertTrue(fileLock.isValid());

        // A different channel should be able to obtain a lock after it has been released
        fileLock.release();
        assertTrue(writeOnlyFileChannel2.lock().isValid());
    }

    /**
     * @tests java.nio.channels.FileChannel#lock(long, long, boolean)
     */
    public void test_lockJJZ_Closed() throws Exception {
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.lock(0, 10, false);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }

        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.lock(0, 10, false);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }

        readWriteFileChannel.close();
        try {
            readWriteFileChannel.lock(0, 10, false);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }

        // throws ClosedChannelException before IllegalArgumentException
        try {
            readWriteFileChannel.lock(-1, 0, false);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#lock(long, long, boolean)
     */
    public void test_lockJJZ_IllegalArgument() throws Exception {
        try {
            writeOnlyFileChannel.lock(0, -1, false);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            writeOnlyFileChannel.lock(-1, 0, false);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            readWriteFileChannel.lock(-1, -1, false);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            readWriteFileChannel.lock(Long.MAX_VALUE, 1, false);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#lock(long, long, boolean)
     */
    public void test_lockJJZ_NonWritable() throws Exception {
        try {
            readOnlyFileChannel.lock(0, 10, false);
            fail("should throw NonWritableChannelException");
        } catch (NonWritableChannelException e) {
            // expected
        }

        // throws NonWritableChannelException before IllegalArgumentException
        try {
            readOnlyFileChannel.lock(-1, 0, false);
            fail("should throw NonWritableChannelException");
        } catch (NonWritableChannelException e) {
            // expected
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#lock(long, long, boolean)
     */
    public void test_lockJJZ_NonReadable() throws Exception {
        try {
            writeOnlyFileChannel.lock(0, 10, true);
            fail("should throw NonReadableChannelException");
        } catch (NonReadableChannelException e) {
            // expected
        }

        // throws NonReadableChannelException before IllegalArgumentException
        try {
            writeOnlyFileChannel.lock(-1, 0, true);
            fail("should throw NonReadableChannelException");
        } catch (NonReadableChannelException e) {
            // expected
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#lock(long, long, boolean)
     */
    public void test_lockJJZ_Shared() throws Exception {
        final long POSITION = 100;
        final long SIZE = 200;
        fileLock = readOnlyFileChannel.lock(POSITION, SIZE, true);
        assertTrue(fileLock.isValid());
        // fileLock.isShared depends on whether the underlying platform support
        // shared lock, but it works on Windows & Linux.
        assertTrue(fileLock.isShared());
        assertSame(readOnlyFileChannel, fileLock.channel());
        assertEquals(POSITION, fileLock.position());
        assertEquals(SIZE, fileLock.size());
    }

    /**
     * @tests java.nio.channels.FileChannel#lock(long, long, boolean)
     */
    public void test_lockJJZ_NotShared() throws Exception {
        final long POSITION = 100;
        final long SIZE = 200;
        fileLock = writeOnlyFileChannel.lock(POSITION, SIZE, false);
        assertTrue(fileLock.isValid());
        assertFalse(fileLock.isShared());
        assertSame(writeOnlyFileChannel, fileLock.channel());
        assertEquals(POSITION, fileLock.position());
        assertEquals(SIZE, fileLock.size());
    }

    /**
     * @tests java.nio.channels.FileChannel#lock(long, long, boolean)
     */
    public void test_lockJJZ_Long_MAX_VALUE() throws Exception {
        final long POSITION = 0;
        final long SIZE = Long.MAX_VALUE;
        fileLock = readOnlyFileChannel.lock(POSITION, SIZE, true);
        assertTrue(fileLock.isValid());
        assertTrue(fileLock.isShared());
        assertEquals(POSITION, fileLock.position());
        assertEquals(SIZE, fileLock.size());
        assertSame(readOnlyFileChannel, fileLock.channel());
    }

    /**
     * @tests java.nio.channels.FileChannel#lock(long, long, boolean)
     */
    public void test_lockJJZ_Overlapping() throws Exception {
        final long POSITION = 100;
        final long SIZE = 200;
        fileLock = writeOnlyFileChannel.lock(POSITION, SIZE, false);
        assertTrue(fileLock.isValid());

        // Test the same channel cannot be locked twice.
        try {
            writeOnlyFileChannel.lock(POSITION + 1, SIZE, false);
            fail("should throw OverlappingFileLockException");
        } catch (OverlappingFileLockException expected) {}

        // Test that a different channel on the same file also cannot be locked.
        try {
            writeOnlyFileChannel2.lock(POSITION + 1, SIZE, false);
            fail("should throw OverlappingFileLockException");
        } catch (OverlappingFileLockException expected) {}
    }

    /**
     * @tests java.nio.channels.FileChannel#lock(long, long, boolean)
     */
    public void test_lockJJZ_NotOverlapping() throws Exception {
        final long POSITION = 100;
        final long SIZE = 200;
        FileLock fileLock1 = writeOnlyFileChannel.lock(POSITION, SIZE, false);
        assertTrue(fileLock1.isValid());
        FileLock fileLock2 = writeOnlyFileChannel.lock(POSITION + SIZE, SIZE,
                false);
        assertTrue(fileLock2.isValid());
    }

    /**
     * @tests java.nio.channels.FileChannel#lock(long,long,boolean)
     */
    public void test_lockJJZ_After_Release() throws Exception {
        fileLock = writeOnlyFileChannel.lock(0, 10, false);
        fileLock.release();
        // after release file lock can be obtained again.
        fileLock = writeOnlyFileChannel.lock(0, 10, false);
        assertTrue(fileLock.isValid());
    }

    /**
     * @tests java.nio.channels.FileChannel#tryLock()
     */
    public void test_tryLock_Closed() throws Exception {
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.tryLock();
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException expected) {}

        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.tryLock();
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException expected) {}

        readWriteFileChannel.close();
        try {
            readWriteFileChannel.tryLock();
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException expected) {}
    }

    /**
     * @tests java.nio.channels.FileChannel#tryLock()
     */
    public void test_tryLock_NonWritable() throws Exception {
        try {
            readOnlyFileChannel.tryLock();
            fail("should throw NonWritableChannelException");
        } catch (NonWritableChannelException expected) {}
    }

    /**
     * @tests java.nio.channels.FileChannel#tryLock()
     */
    public void test_tryLock() throws Exception {
        fileLock = writeOnlyFileChannel.tryLock();
        assertTrue(fileLock.isValid());
        assertFalse(fileLock.isShared());
        assertSame(writeOnlyFileChannel, fileLock.channel());
        assertEquals(0, fileLock.position());
        assertEquals(Long.MAX_VALUE, fileLock.size());
    }

    /**
     * @tests java.nio.channels.FileChannel#tryLock()
     */
    public void test_tryLock_Overlapping() throws Exception {
        fileLock = writeOnlyFileChannel.tryLock();
        assertTrue(fileLock.isValid());

        // Test the same channel cannot be locked twice.
        try {
            writeOnlyFileChannel.tryLock();
            fail("should throw OverlappingFileLockException");
        } catch (OverlappingFileLockException expected) {}

        // Test that a different channel on the same file also cannot be locked.
        try {
            writeOnlyFileChannel2.tryLock();
            fail("should throw OverlappingFileLockException");
        } catch (OverlappingFileLockException expected) {}
    }

    /**
     * @tests java.nio.channels.FileChannel#tryLock()
     */
    public void test_tryLock_After_Release() throws Exception {
        fileLock = writeOnlyFileChannel.tryLock();
        fileLock.release();

        // After release file lock can be obtained again.
        fileLock = writeOnlyFileChannel.tryLock();
        assertTrue(fileLock.isValid());

        // Test that the same channel can acquire the lock after it has been released
        fileLock.release();
        fileLock = writeOnlyFileChannel.tryLock();
        assertTrue(fileLock.isValid());

        // Test that a different channel can acquire the lock after it has been released
        fileLock.release();
        fileLock = writeOnlyFileChannel2.tryLock();
        assertTrue(fileLock.isValid());
    }

    /**
     * @tests java.nio.channels.FileChannel#tryLock(long, long, boolean)
     */
    public void test_tryLockJJZ_Closed() throws Exception {
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.tryLock(0, 10, false);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }

        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.tryLock(0, 10, false);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }

        readWriteFileChannel.close();
        try {
            readWriteFileChannel.tryLock(0, 10, false);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }

        // throws ClosedChannelException before IllegalArgumentException
        try {
            readWriteFileChannel.tryLock(-1, 0, false);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#tryLock(long, long, boolean)
     */
    public void test_tryLockJJZ_IllegalArgument() throws Exception {
        try {
            writeOnlyFileChannel.tryLock(0, -1, false);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            writeOnlyFileChannel.tryLock(-1, 0, false);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            readWriteFileChannel.tryLock(-1, -1, false);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            readWriteFileChannel.tryLock(Long.MAX_VALUE, 1, false);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#tryLock(long, long, boolean)
     */
    public void test_tryLockJJZ_NonWritable() throws Exception {
        try {
            readOnlyFileChannel.tryLock(0, 10, false);
            fail("should throw NonWritableChannelException");
        } catch (NonWritableChannelException e) {
            // expected
        }

        // throws NonWritableChannelException before IllegalArgumentException
        try {
            readOnlyFileChannel.tryLock(-1, 0, false);
            fail("should throw NonWritableChannelException");
        } catch (NonWritableChannelException e) {
            // expected
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#tryLock(long, long, boolean)
     */
    public void test_tryLockJJZ_NonReadable() throws Exception {
        try {
            writeOnlyFileChannel.tryLock(0, 10, true);
            fail("should throw NonReadableChannelException");
        } catch (NonReadableChannelException e) {
            // expected
        }

        // throws NonReadableChannelException before IllegalArgumentException
        try {
            writeOnlyFileChannel.tryLock(-1, 0, true);
            fail("should throw NonReadableChannelException");
        } catch (NonReadableChannelException e) {
            // expected
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#tryLock(long, long, boolean)
     */
    public void test_tryLockJJZ_Shared() throws Exception {
        final long POSITION = 100;
        final long SIZE = 200;
        fileLock = readOnlyFileChannel.tryLock(POSITION, SIZE, true);
        assertTrue(fileLock.isValid());
        // fileLock.isShared depends on whether the underlying platform support
        // shared lock, but it works on Windows & Linux.
        assertTrue(fileLock.isShared());
        assertSame(readOnlyFileChannel, fileLock.channel());
        assertEquals(POSITION, fileLock.position());
        assertEquals(SIZE, fileLock.size());
    }

    /**
     * @tests java.nio.channels.FileChannel#tryLock(long, long, boolean)
     */
    public void test_tryLockJJZ_NotShared() throws Exception {
        final long POSITION = 100;
        final long SIZE = 200;
        fileLock = writeOnlyFileChannel.tryLock(POSITION, SIZE, false);
        assertTrue(fileLock.isValid());
        assertFalse(fileLock.isShared());
        assertSame(writeOnlyFileChannel, fileLock.channel());
        assertEquals(POSITION, fileLock.position());
        assertEquals(SIZE, fileLock.size());
    }

    /**
     * @tests java.nio.channels.FileChannel#tryLock(long, long, boolean)
     */
    public void test_tryLockJJZ_Long_MAX_VALUE() throws Exception {
        final long POSITION = 0;
        final long SIZE = Long.MAX_VALUE;
        fileLock = readOnlyFileChannel.tryLock(POSITION, SIZE, true);
        assertTrue(fileLock.isValid());
        assertTrue(fileLock.isShared());
        assertEquals(POSITION, fileLock.position());
        assertEquals(SIZE, fileLock.size());
        assertSame(readOnlyFileChannel, fileLock.channel());
    }

    /**
     * @tests java.nio.channels.FileChannel#tryLock(long, long, boolean)
     */
    public void test_tryLockJJZ_Overlapping() throws Exception {
        final long POSITION = 100;
        final long SIZE = 200;
        fileLock = writeOnlyFileChannel.lock(POSITION, SIZE, false);
        assertTrue(fileLock.isValid());

        // Test the same channel cannot be locked twice.
        try {
            writeOnlyFileChannel.tryLock(POSITION + 1, SIZE, false);
            fail("should throw OverlappingFileLockException");
        } catch (OverlappingFileLockException expected) {}

        // Test that a different channel on the same file also cannot be locked.
        try {
            writeOnlyFileChannel2.tryLock(POSITION + 1, SIZE, false);
            fail("should throw OverlappingFileLockException");
        } catch (OverlappingFileLockException expected) {}
    }

    /**
     * @tests java.nio.channels.FileChannel#tryLock(long, long, boolean)
     */
    public void test_tryLockJJZ_NotOverlapping() throws Exception {
        final long POSITION = 100;
        final long SIZE = 200;
        FileLock fileLock1 = writeOnlyFileChannel
                .tryLock(POSITION, SIZE, false);
        assertTrue(fileLock1.isValid());

        FileLock fileLock2 = writeOnlyFileChannel.tryLock(POSITION + SIZE,
                SIZE, false);
        assertTrue(fileLock2.isValid());
    }

    /**
     * @tests java.nio.channels.FileChannel#tryLock(long,long,boolean)
     */
    public void test_tryLockJJZ_After_Release() throws Exception {
        fileLock = writeOnlyFileChannel.tryLock(0, 10, false);
        fileLock.release();

        // after release file lock can be obtained again.
        fileLock = writeOnlyFileChannel.tryLock(0, 10, false);
        assertTrue(fileLock.isValid());
    }

    /**
     * @tests java.nio.channels.FileChannel#read(ByteBuffer)
     */
    public void test_readLByteBuffer_Null() throws Exception {
        ByteBuffer readBuffer = null;

        try {
            readOnlyFileChannel.read(readBuffer);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            readWriteFileChannel.read(readBuffer);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#read(ByteBuffer)
     */
    public void test_readLByteBuffer_Closed() throws Exception {
        ByteBuffer readBuffer = ByteBuffer.allocate(CAPACITY);

        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.read(readBuffer);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }

        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.read(readBuffer);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }

        readWriteFileChannel.close();
        try {
            readWriteFileChannel.read(readBuffer);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }

        // should throw ClosedChannelException first
        readBuffer = null;
        try {
            readWriteFileChannel.read(readBuffer);
            fail();
        } catch (ClosedChannelException expected) {
        } catch (NullPointerException expected) {
        }
    }

    public void test_readLByteBuffer_WriteOnly() throws Exception {
        ByteBuffer readBuffer = ByteBuffer.allocate(CAPACITY);

        try {
            writeOnlyFileChannel.read(readBuffer);
            fail();
        } catch (NonReadableChannelException expected) {
        }

        readBuffer = null;
        try {
            writeOnlyFileChannel.read(readBuffer);
            fail();
        } catch (NonReadableChannelException expected) {
        } catch (NullPointerException expected) {
        }
    }

    public void test_readLByteBuffer_EmptyFile() throws Exception {
        ByteBuffer readBuffer = ByteBuffer.allocate(CAPACITY);
        int result = readOnlyFileChannel.read(readBuffer);
        assertEquals(-1, result);
        assertEquals(0, readBuffer.position());
    }

    /**
     * @tests java.nio.channels.FileChannel#read(ByteBuffer)
     */
    public void test_readLByteBuffer_LimitedCapacity() throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);

        ByteBuffer readBuffer = ByteBuffer.allocate(LIMITED_CAPACITY);
        int result = readOnlyFileChannel.read(readBuffer);
        assertEquals(LIMITED_CAPACITY, result);
        assertEquals(LIMITED_CAPACITY, readBuffer.position());
        readBuffer.flip();
        for (int i = 0; i < LIMITED_CAPACITY; i++) {
            assertEquals(CONTENT_AS_BYTES[i], readBuffer.get());
        }
    }

    public void test_readLByteBuffer() throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);

        ByteBuffer readBuffer = ByteBuffer.allocate(CONTENT_AS_BYTES_LENGTH);
        int result = readOnlyFileChannel.read(readBuffer);
        assertEquals(CONTENT_AS_BYTES_LENGTH, result);
        assertEquals(CONTENT_AS_BYTES_LENGTH, readBuffer.position());
        readBuffer.flip();
        for (int i = 0; i < CONTENT_AS_BYTES_LENGTH; i++) {
            assertEquals(CONTENT_AS_BYTES[i], readBuffer.get());
        }
    }

    public void test_readLByteBufferJ_Null() throws Exception {
        try {
            readOnlyFileChannel.read(null, 0);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            readWriteFileChannel.read(null, 0);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void test_readLByteBufferJ_Closed() throws Exception {
        ByteBuffer readBuffer = ByteBuffer.allocate(CAPACITY);

        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.read(readBuffer, 0);
            fail();
        } catch (ClosedChannelException expected) {
        }

        readWriteFileChannel.close();
        try {
            readWriteFileChannel.read(readBuffer, 0);
            fail();
        } catch (ClosedChannelException expected) {
        }
    }

    public void test_readLByteBufferJ_IllegalArgument() throws Exception {
        ByteBuffer readBuffer = ByteBuffer.allocate(CAPACITY);

        try {
            readOnlyFileChannel.read(readBuffer, -1);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            writeOnlyFileChannel.read(readBuffer, -1);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            readWriteFileChannel.read(readBuffer, -1);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void test_readLByteBufferJ_WriteOnly() throws Exception {
        ByteBuffer readBuffer = ByteBuffer.allocate(CAPACITY);

        try {
            writeOnlyFileChannel.read(readBuffer, 0);
            fail();
        } catch (NonReadableChannelException expected) {
        }
    }

    public void test_readLByteBufferJ_Emptyfile() throws Exception {
        ByteBuffer readBuffer = ByteBuffer.allocate(CAPACITY);
        int result = readOnlyFileChannel.read(readBuffer, 0);
        assertEquals(-1, result);
        assertEquals(0, readBuffer.position());
    }

    public void test_readLByteBufferJ_Position_BeyondFileLimit() throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);

        ByteBuffer readBuffer = ByteBuffer.allocate(CAPACITY);
        int result = readOnlyFileChannel.read(readBuffer,
                CONTENT_AS_BYTES.length);
        assertEquals(-1, result);
        assertEquals(0, readBuffer.position());
    }

    public void test_readLByteBufferJ_Position_As_Long() throws Exception {
        ByteBuffer readBuffer = ByteBuffer.allocate(CAPACITY);
        try {
            readOnlyFileChannel.read(readBuffer, Long.MAX_VALUE);
        } catch (IOException expected) {
        }
    }

    public void test_readLByteBufferJ() throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);
        ByteBuffer readBuffer = ByteBuffer.allocate(CAPACITY);

        final int BUFFER_POSITION = 1;
        readBuffer.position(BUFFER_POSITION);

        final int POSITION = 2;
        int result = readOnlyFileChannel.read(readBuffer, POSITION);
        assertEquals(CONTENT_AS_BYTES_LENGTH - POSITION, result);
        assertEquals(BUFFER_POSITION + result, readBuffer.position());

        readBuffer.flip();
        readBuffer.position(BUFFER_POSITION);
        for (int i = POSITION; i < CONTENT_AS_BYTES_LENGTH; i++) {
            assertEquals(CONTENT_AS_BYTES[i], readBuffer.get());
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#read(ByteBuffer[])
     */
    public void test_read$LByteBuffer() throws Exception {
        // regression test for Harmony-849
        writeDataToFile(fileOfReadOnlyFileChannel);
        ByteBuffer[] readBuffers = new ByteBuffer[2];
        readBuffers[0] = ByteBuffer.allocate(CAPACITY);
        readBuffers[1] = ByteBuffer.allocate(CAPACITY);

        long readCount = readOnlyFileChannel.read(readBuffers);
        assertEquals(CONTENT_AS_BYTES_LENGTH, readCount);
        assertEquals(CONTENT_AS_BYTES_LENGTH, readBuffers[0].position());
        assertEquals(0, readBuffers[1].position());
        readBuffers[0].flip();
        for (int i = 0; i < CONTENT_AS_BYTES_LENGTH; i++) {
            assertEquals(CONTENT_AS_BYTES[i], readBuffers[0].get());
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#read(ByteBuffer[])
     */
    public void test_read$LByteBuffer_mock() throws Exception {
        FileChannel mockChannel = new MockFileChannel();
        ByteBuffer[] buffers = new ByteBuffer[2];
        mockChannel.read(buffers);
        // Verify that calling read(ByteBuffer[] dsts) leads to the method
        // read(dsts, 0, dsts.length)
        assertTrue(((MockFileChannel)mockChannel).isReadCalled);
    }

    /**
     * @tests java.nio.channels.FileChannel#read(ByteBuffer[], int, int)
     */
    public void test_read$LByteBufferII_Null() throws Exception {
        ByteBuffer[] readBuffers = null;

        try {
            readOnlyFileChannel.read(readBuffers, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            readOnlyFileChannel.read(readBuffers, 1, 11);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            writeOnlyFileChannel.read(readBuffers, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            readWriteFileChannel.read(readBuffers, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        // first throws NullPointerException
        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.read(readBuffers, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#read(ByteBuffer[], int, int)
     */
    public void test_read$LByteBufferII_Closed() throws Exception {
        ByteBuffer[] readBuffers = new ByteBuffer[2];
        readBuffers[0] = ByteBuffer.allocate(CAPACITY);

        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.read(readBuffers, 0, 1);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }

        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.read(readBuffers, 0, 1);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }

        readWriteFileChannel.close();
        try {
            readWriteFileChannel.read(readBuffers, 0, 1);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }

        // regression test for Harmony-902
        readBuffers[0] = null;
        try {
            readOnlyFileChannel.read(readBuffers, 0, 1);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }
        try {
            writeOnlyFileChannel.read(readBuffers, 0, 1);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }
        try {
            readWriteFileChannel.read(readBuffers, 0, 1);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#read(ByteBuffer[], int, int)
     */
    public void test_read$LByteBufferII_WriteOnly() throws Exception {
        ByteBuffer[] readBuffers = new ByteBuffer[2];
        readBuffers[0] = ByteBuffer.allocate(CAPACITY);

        try {
            writeOnlyFileChannel.read(readBuffers, 0, 1);
            fail("should throw NonReadableChannelException");
        } catch (NonReadableChannelException e) {
            // expected
        }

        // first throws NonReadableChannelException.
        readBuffers[0] = null;
        try {
            writeOnlyFileChannel.read(readBuffers, 0, 1);
            fail("should throw NonReadableChannelException");
        } catch (NonReadableChannelException e) {
            // expected
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#read(ByteBuffer[], int, int)
     */
    public void test_read$LByteBufferII_IndexOutOfBound() throws Exception {
        ByteBuffer[] readBuffers = new ByteBuffer[2];
        readBuffers[0] = ByteBuffer.allocate(CAPACITY);
        readBuffers[1] = ByteBuffer.allocate(CAPACITY);

        try {
            readOnlyFileChannel.read(readBuffers, 2, 1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }

        try {
            readWriteFileChannel.read(null, -1, 0);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        } catch (NullPointerException expected) {
        }

        try {
            writeOnlyFileChannel.read(readBuffers, 0, 3);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }

        try {
            readWriteFileChannel.read(readBuffers, -1, 0);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }

        readWriteFileChannel.close();
        try {
            readWriteFileChannel.read(readBuffers, 0, 3);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#read(ByteBuffer[], int, int)
     */
    public void test_read$LByteBufferII_EmptyFile() throws Exception {
        ByteBuffer[] readBuffers = new ByteBuffer[2];
        readBuffers[0] = ByteBuffer.allocate(CAPACITY);
        readBuffers[1] = ByteBuffer.allocate(CAPACITY);
        long result = readOnlyFileChannel.read(readBuffers, 0, 2);
        assertEquals(-1, result);
        assertEquals(0, readBuffers[0].position());
        assertEquals(0, readBuffers[1].position());
    }

    /**
     * @tests java.nio.channels.FileChannel#read(ByteBuffer[], int, int)
     */
    public void test_read$LByteBufferII_EmptyBuffers() throws Exception {
        ByteBuffer[] readBuffers = new ByteBuffer[2];
        try {
            readOnlyFileChannel.read(readBuffers, 0, 2);
        } catch (NullPointerException e) {
            // expected
        }

        writeDataToFile(fileOfReadOnlyFileChannel);
        readBuffers[0] = ByteBuffer.allocate(CAPACITY);
        try {
            readOnlyFileChannel.read(readBuffers, 0, 2);
        } catch (NullPointerException e) {
            // expected
        }

        long result = readOnlyFileChannel.read(readBuffers, 0, 1);
        assertEquals(CONTENT_AS_BYTES_LENGTH, result);
    }

    /**
     * @tests java.nio.channels.FileChannel#read(ByteBuffer[], int, int)
     */
    public void test_read$LByteBufferII_EmptyFile_EmptyBuffers()
            throws Exception {
        ByteBuffer[] readBuffers = new ByteBuffer[2];
        // will not throw NullPointerException
        long result = readOnlyFileChannel.read(readBuffers, 0, 0);
        assertEquals(0, result);
    }

    /**
     * @tests java.nio.channels.FileChannel#read(ByteBuffer[], int, int)
     */
    public void test_read$LByteBufferII_Length_Zero() throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);
        ByteBuffer[] readBuffers = new ByteBuffer[2];
        readBuffers[0] = ByteBuffer.allocate(LIMITED_CAPACITY);
        readBuffers[1] = ByteBuffer.allocate(LIMITED_CAPACITY);
        long result = readOnlyFileChannel.read(readBuffers, 1, 0);
        assertEquals(0, result);
    }

    /**
     * @tests java.nio.channels.FileChannel#read(ByteBuffer[], int, int)
     */
    public void test_read$LByteBufferII_LimitedCapacity() throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);
        ByteBuffer[] readBuffers = new ByteBuffer[2];
        readBuffers[0] = ByteBuffer.allocate(LIMITED_CAPACITY);
        readBuffers[1] = ByteBuffer.allocate(LIMITED_CAPACITY);

        // reads to the second buffer
        long result = readOnlyFileChannel.read(readBuffers, 1, 1);
        assertEquals(LIMITED_CAPACITY, result);
        assertEquals(0, readBuffers[0].position());
        assertEquals(LIMITED_CAPACITY, readBuffers[1].position());

        readBuffers[1].flip();
        for (int i = 0; i < LIMITED_CAPACITY; i++) {
            assertEquals(CONTENT_AS_BYTES[i], readBuffers[1].get());
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#read(ByteBuffer[], int, int)
     */
    public void test_read$LByteBufferII() throws Exception {
        writeDataToFile(fileOfReadOnlyFileChannel);
        ByteBuffer[] readBuffers = new ByteBuffer[2];
        readBuffers[0] = ByteBuffer.allocate(CAPACITY);
        readBuffers[1] = ByteBuffer.allocate(CAPACITY);

        // writes to the second buffer
        assertEquals(CONTENT_AS_BYTES_LENGTH, readOnlyFileChannel.read(
                readBuffers, 1, 1));
        assertEquals(0, readBuffers[0].position());
        assertEquals(CONTENT_AS_BYTES_LENGTH, readBuffers[1].position());

        readBuffers[1].flip();
        for (int i = 0; i < CONTENT_AS_BYTES_LENGTH; i++) {
            assertEquals(CONTENT_AS_BYTES[i], readBuffers[1].get());
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#isOpen()
     */
    public void test_isOpen() throws Exception {
        // Regression for HARMONY-40
        File logFile = File.createTempFile("out", "tmp");
        logFile.deleteOnExit();
        FileOutputStream out = new FileOutputStream(logFile, true);
        FileChannel channel = out.getChannel();
        out.write(1);
        out.close();
        assertFalse("Assert 0: Channel is still open", channel.isOpen());
    }

    /**
     * @tests java.nio.channels.FileChannel#position()
     */
    public void test_position_append() throws Exception {
        // Regression test for Harmony-508
        File tmpfile = File.createTempFile("FileOutputStream", "tmp");
        tmpfile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tmpfile);
        byte[] b = new byte[10];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) i;
        }
        fos.write(b);
        fos.flush();
        fos.close();
        FileOutputStream f = new FileOutputStream(tmpfile, true);
        // We're in append mode, so we should be positioned at the end of the file.
        assertEquals(10, f.getChannel().position());
    }


    /**
     * @tests java.nio.channels.FileChannel#map(MapMode,long,long)
     */
    public void test_map_AbnormalMode() throws IOException {
        try {
            writeOnlyFileChannel.map(MapMode.READ_ONLY, 0, CONTENT_LENGTH);
            fail("should throw NonReadableChannelException.");
        } catch (NonReadableChannelException ex) {
            // expected;
        }
        try {
            writeOnlyFileChannel.map(MapMode.READ_WRITE, 0, CONTENT_LENGTH);
            fail("should throw NonReadableChannelException.");
        } catch (NonReadableChannelException ex) {
            // expected;
        }
        try {
            writeOnlyFileChannel.map(MapMode.PRIVATE, 0, CONTENT_LENGTH);
            fail("should throw NonReadableChannelException.");
        } catch (NonReadableChannelException ex) {
            // expected;
        }
        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.map(MapMode.READ_WRITE, 0, -1);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException ex) {
            // expected;
        }

        try {
            readOnlyFileChannel.map(MapMode.READ_WRITE, 0, CONTENT_LENGTH);
            fail("should throw NonWritableChannelException .");
        } catch (NonWritableChannelException ex) {
            // expected;
        }
        try {
            readOnlyFileChannel.map(MapMode.PRIVATE, 0, CONTENT_LENGTH);
            fail("should throw NonWritableChannelException .");
        } catch (NonWritableChannelException ex) {
            // expected;
        }
        try {
            readOnlyFileChannel.map(MapMode.READ_WRITE, -1, CONTENT_LENGTH);
            fail("should throw IAE.");
        } catch (IllegalArgumentException ex) {
            // expected;
        }
        try {
            readOnlyFileChannel.map(MapMode.READ_WRITE, 0, -1);
            fail("should throw IAE.");
        } catch (IllegalArgumentException ex) {
            // expected;
        }

        try {
            readOnlyFileChannel.map(MapMode.READ_ONLY, 0, CONTENT_LENGTH + 1);
            fail();
        } catch (NonWritableChannelException expected) {
        } catch (IOException expected) {
        }
        try {
            readOnlyFileChannel.map(MapMode.READ_ONLY, 2, CONTENT_LENGTH - 1);
            fail();
        } catch (NonWritableChannelException expected) {
        } catch (IOException expected) {
        }

        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.map(MapMode.READ_WRITE, 0, -1);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException ex) {
            // expected;
        }
        try {
            readOnlyFileChannel.map(MapMode.READ_ONLY, 2, CONTENT_LENGTH - 1);
            fail("should throw IOException.");
        } catch (IOException ex) {
            // expected;
        }

        readWriteFileChannel.close();
        try {
            readWriteFileChannel.map(MapMode.READ_WRITE, 0, -1);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException ex) {
            // expected;
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#map(MapMode,long,long)
     */
    public void test_map_ReadOnly_CloseChannel() throws IOException {
        // close channel has no effect on map if mapped
        assertEquals(0, readWriteFileChannel.size());
        MappedByteBuffer mapped = readWriteFileChannel.map(MapMode.READ_ONLY,
                0, CONTENT_LENGTH);
        assertEquals(CONTENT_LENGTH, readWriteFileChannel.size());
        readOnlyFileChannel.close();
        assertEquals(CONTENT_LENGTH, mapped.limit());
    }

    /**
     * @tests java.nio.channels.FileChannel#map(MapMode,long,long)
     */
    public void test_map_Private_CloseChannel() throws IOException {
        MappedByteBuffer mapped = readWriteFileChannel.map(MapMode.PRIVATE, 0,
                CONTENT_LENGTH);
        readWriteFileChannel.close();
        mapped.put(TEST_BYTES);
        assertEquals(CONTENT_LENGTH, mapped.limit());
        assertEquals("test".length(), mapped.position());
    }

    /**
     * @tests java.nio.channels.FileChannel#map(MapMode,long,long)
     */
    public void test_map_ReadOnly() throws IOException {
        MappedByteBuffer mapped = null;
        // try put something to readonly map
        writeDataToFile(fileOfReadOnlyFileChannel);
        mapped = readOnlyFileChannel.map(MapMode.READ_ONLY, 0, CONTENT_LENGTH);
        try {
            mapped.put(TEST_BYTES);
            fail("should throw ReadOnlyBufferException.");
        } catch (ReadOnlyBufferException ex) {
            // expected;
        }
        assertEquals(CONTENT_LENGTH, mapped.limit());
        assertEquals(CONTENT_LENGTH, mapped.capacity());
        assertEquals(0, mapped.position());

        // try to get a readonly map from read/write channel
        writeDataToFile(fileOfReadWriteFileChannel);
        mapped = readWriteFileChannel.map(MapMode.READ_ONLY, 0, CONTENT
                .length());
        assertEquals(CONTENT_LENGTH, mapped.limit());
        assertEquals(CONTENT_LENGTH, mapped.capacity());
        assertEquals(0, mapped.position());

        // map not change channel's position
        assertEquals(0, readOnlyFileChannel.position());
        assertEquals(0, readWriteFileChannel.position());
    }

    /**
     * @tests java.nio.channels.FileChannel#map(MapMode,long,long)
     */
    public void test_map_ReadOnly_NonZeroPosition() throws IOException {
        this.writeDataToFile(fileOfReadOnlyFileChannel);
        MappedByteBuffer mapped = readOnlyFileChannel.map(MapMode.READ_ONLY,
                10, CONTENT_LENGTH - 10);
        assertEquals(CONTENT_LENGTH - 10, mapped.limit());
        assertEquals(CONTENT_LENGTH - 10, mapped.capacity());
        assertEquals(0, mapped.position());
    }

    /**
     * @tests java.nio.channels.FileChannel#map(MapMode,long,long)
     */
    public void test_map_Private() throws IOException {
        this.writeDataToFile(fileOfReadWriteFileChannel);
        MappedByteBuffer mapped = readWriteFileChannel.map(MapMode.PRIVATE, 0,
                CONTENT_LENGTH);
        assertEquals(CONTENT_LENGTH, mapped.limit());
        // test copy on write if private
        ByteBuffer returnByPut = mapped.put(TEST_BYTES);
        assertSame(returnByPut, mapped);
        ByteBuffer checkBuffer = ByteBuffer.allocate(CONTENT_LENGTH);
        mapped.force();
        readWriteFileChannel.read(checkBuffer);
        assertEquals(CONTENT, new String(checkBuffer.array(), "iso8859-1"));

        // test overflow
        try {
            mapped.put(("test" + CONTENT).getBytes("iso8859-1"));
            fail("should throw BufferOverflowException.");
        } catch (BufferOverflowException ex) {
            // expected;
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#map(MapMode,long,long)
     */
    public void test_map_Private_NonZeroPosition() throws IOException {
        MappedByteBuffer mapped = readWriteFileChannel.map(MapMode.PRIVATE, 10,
                CONTENT_LENGTH - 10);
        assertEquals(CONTENT_LENGTH - 10, mapped.limit());
        assertEquals(CONTENT_LENGTH - 10, mapped.capacity());
        assertEquals(0, mapped.position());
    }

    /**
     * @tests java.nio.channels.FileChannel#map(MapMode,long,long)
     */
    public void test_map_ReadWrite() throws IOException {
        MappedByteBuffer mapped = null;
        writeDataToFile(fileOfReadWriteFileChannel);
        mapped = readWriteFileChannel.map(MapMode.READ_WRITE, 0, CONTENT
                .length());

        // put something will change its channel
        ByteBuffer returnByPut = mapped.put(TEST_BYTES);
        assertSame(returnByPut, mapped);
        String checkString = "test" + CONTENT.substring(4);
        ByteBuffer checkBuffer = ByteBuffer.allocate(CONTENT_LENGTH);
        mapped.force();
        readWriteFileChannel.position(0);
        readWriteFileChannel.read(checkBuffer);
        assertEquals(checkString, new String(checkBuffer.array(), "iso8859-1"));

        try {
            mapped.put(("test" + CONTENT).getBytes("iso8859-1"));
            fail("should throw BufferOverflowException.");
        } catch (BufferOverflowException ex) {
            // expected;
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#map(MapMode,long,long)
     */
    public void test_map_ReadWrite_NonZeroPosition() throws IOException {
        // test position non-zero
        writeDataToFile(fileOfReadWriteFileChannel);
        MappedByteBuffer mapped = readWriteFileChannel.map(MapMode.READ_WRITE,
                10, CONTENT_LENGTH - 10);
        assertEquals(CONTENT_LENGTH - 10, mapped.limit());
        assertEquals(CONTENT.length() - 10, mapped.capacity());
        assertEquals(0, mapped.position());
        mapped.put(TEST_BYTES);
        ByteBuffer checkBuffer = ByteBuffer.allocate(CONTENT_LENGTH);
        readWriteFileChannel.read(checkBuffer);
        String expected = CONTENT.substring(0, 10) + "test"
                + CONTENT.substring(10 + "test".length());
        assertEquals(expected, new String(checkBuffer.array(), "iso8859-1"));
    }

    /**
     * Tests map() method for the value of positions exceeding memory
     * page size and allocation granularity size.
     *
     * @tests java.nio.channels.FileChannel#map(MapMode,long,long)
     */
    public void test_map_LargePosition() throws IOException {
        // Regression test for HARMONY-3085
        int[] sizes = {
            4096, // 4K size (normal page size for Linux & Windows)
            65536, // 64K size (alocation granularity size for Windows)
        };
        final int CONTENT_LEN = 10;

        for (int i = 0; i < sizes.length; ++i) {
            // reset the file and the channel for the iterations
            // (for the first iteration it was done by setUp()
            if (i > 0 ) {
                fileOfReadOnlyFileChannel = File.createTempFile(
                        "File_of_readOnlyFileChannel", "tmp");
                fileOfReadOnlyFileChannel.deleteOnExit();
                readOnlyFileChannel = new FileInputStream(fileOfReadOnlyFileChannel)
                        .getChannel();
            }

            writeLargeDataToFile(fileOfReadOnlyFileChannel, sizes[i] + 2 * CONTENT_LEN);
            MappedByteBuffer mapped = readOnlyFileChannel.map(MapMode.READ_ONLY,
                    sizes[i], CONTENT_LEN);
            assertEquals("Incorrectly mapped file channel for " + sizes[i]
                    + " position (capacity)", CONTENT_LEN, mapped.capacity());
            assertEquals("Incorrectly mapped file channel for " + sizes[i]
                    + " position (limit)", CONTENT_LEN, mapped.limit());
            assertEquals("Incorrectly mapped file channel for " + sizes[i]
                    + " position (position)", 0, mapped.position());

            // map not change channel's position
            assertEquals(0, readOnlyFileChannel.position());

            // Close the file and the channel before the next iteration
            readOnlyFileChannel.close();
            fileOfReadOnlyFileChannel.delete();
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#write(ByteBuffer)
     */
    public void test_writeLByteBuffer_Null() throws Exception {
        ByteBuffer writeBuffer = null;

        try {
            writeOnlyFileChannel.write(writeBuffer);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            readWriteFileChannel.write(writeBuffer);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#write(ByteBuffer)
     */
    public void test_writeLByteBuffer_Closed() throws Exception {
        ByteBuffer writeBuffer = ByteBuffer.allocate(CAPACITY);

        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.write(writeBuffer);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }

        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.write(writeBuffer);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }

        readWriteFileChannel.close();
        try {
            readWriteFileChannel.write(writeBuffer);
            fail();
        } catch (ClosedChannelException expected) {
        }

        writeBuffer = null;
        try {
            readWriteFileChannel.read(writeBuffer);
            fail();
        } catch (NullPointerException expected) {
        } catch (ClosedChannelException expected) {
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#write(ByteBuffer)
     */
    public void test_writeLByteBuffer_ReadOnly() throws Exception {
        ByteBuffer writeBuffer = ByteBuffer.allocate(CAPACITY);

        try {
            readOnlyFileChannel.write(writeBuffer);
            fail("should throw NonWritableChannelException");
        } catch (NonWritableChannelException e) {
            // expected
        }

        // first throws NonWriteableChannelException
        writeBuffer = null;
        try {
            readOnlyFileChannel.write(writeBuffer);
            fail("should throw NonWritableChannelException");
        } catch (NonWritableChannelException e) {
            // expected
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#write(ByteBuffer)
     */
    public void test_writeLByteBuffer() throws Exception {
        ByteBuffer writeBuffer = ByteBuffer.wrap(CONTENT_AS_BYTES);

        int result = writeOnlyFileChannel.write(writeBuffer);
        assertEquals(CONTENT_AS_BYTES_LENGTH, result);
        assertEquals(CONTENT_AS_BYTES_LENGTH, writeBuffer.position());
        writeOnlyFileChannel.close();

        assertEquals(CONTENT_AS_BYTES_LENGTH, fileOfWriteOnlyFileChannel
                .length());

        fis = new FileInputStream(fileOfWriteOnlyFileChannel);
        byte[] inputBuffer = new byte[CONTENT_AS_BYTES_LENGTH];
        fis.read(inputBuffer);
        assertTrue(Arrays.equals(CONTENT_AS_BYTES, inputBuffer));
    }

    /**
     * @tests java.nio.channels.FileChannel#write(ByteBuffer)
     */
    public void test_writeLByteBuffer_positioned() throws Exception {
        final int pos = 5;
        ByteBuffer writeBuffer = ByteBuffer.wrap(CONTENT_AS_BYTES);
        writeBuffer.position(pos);
        int result = writeOnlyFileChannel.write(writeBuffer);
        assertEquals(CONTENT_AS_BYTES_LENGTH - pos, result);
        assertEquals(CONTENT_AS_BYTES_LENGTH, writeBuffer.position());
        writeOnlyFileChannel.close();

        assertEquals(CONTENT_AS_BYTES_LENGTH - pos, fileOfWriteOnlyFileChannel
                .length());

        fis = new FileInputStream(fileOfWriteOnlyFileChannel);
        byte[] inputBuffer = new byte[CONTENT_AS_BYTES_LENGTH - pos];
        fis.read(inputBuffer);
        String test = CONTENT.substring(pos);
        assertTrue(Arrays.equals(test.getBytes("iso8859-1"), inputBuffer));
    }

    public void test_writeLByteBufferJ_Null() throws Exception {
        try {
            readWriteFileChannel.write(null, 0);
            fail();
        } catch (NullPointerException expected) {
        } catch (NonWritableChannelException expected) {
        }

        try {
            writeOnlyFileChannel.write(null, 0);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            readWriteFileChannel.write(null, 0);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void test_writeLByteBufferJ_Closed() throws Exception {
        ByteBuffer writeBuffer = ByteBuffer.allocate(CAPACITY);

        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.write(writeBuffer, 0);
            fail();
        } catch (ClosedChannelException expected) {
        }

        readWriteFileChannel.close();
        try {
            readWriteFileChannel.write(writeBuffer, 0);
            fail();
        } catch (ClosedChannelException expected) {
        }
    }

    public void test_writeLByteBufferJ_ReadOnly() throws Exception {
        ByteBuffer writeBuffer = ByteBuffer.allocate(CAPACITY);
        try {
            readOnlyFileChannel.write(writeBuffer, 10);
            fail();
        } catch (NonWritableChannelException expected) {
        }
    }

    public void test_writeLByteBufferJ_IllegalArgument() throws Exception {
        ByteBuffer writeBuffer = ByteBuffer.allocate(CAPACITY);

        try {
            readOnlyFileChannel.write(writeBuffer, -1);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            writeOnlyFileChannel.write(writeBuffer, -1);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            readWriteFileChannel.write(writeBuffer, -1);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#write(ByteBuffer,long)
     */
    public void test_writeLByteBufferJ() throws Exception {
        writeDataToFile(fileOfWriteOnlyFileChannel);

        final int POSITION = 4;
        ByteBuffer writeBuffer = ByteBuffer.wrap(CONTENT_AS_BYTES);
        int result = writeOnlyFileChannel.write(writeBuffer, POSITION);
        assertEquals(CONTENT_AS_BYTES_LENGTH, result);
        assertEquals(CONTENT_AS_BYTES_LENGTH, writeBuffer.position());
        writeOnlyFileChannel.close();

        assertEquals(POSITION + CONTENT_AS_BYTES_LENGTH,
                fileOfWriteOnlyFileChannel.length());

        fis = new FileInputStream(fileOfWriteOnlyFileChannel);
        byte[] inputBuffer = new byte[POSITION + CONTENT_AS_BYTES_LENGTH];
        fis.read(inputBuffer);
        byte[] expectedResult = new byte[POSITION + CONTENT_AS_BYTES_LENGTH];
        System.arraycopy(CONTENT_AS_BYTES, 0, expectedResult, 0, POSITION);
        System.arraycopy(CONTENT_AS_BYTES, 0, expectedResult, POSITION,
                CONTENT_AS_BYTES_LENGTH);
        assertTrue(Arrays.equals(expectedResult, inputBuffer));
    }

    /**
     * @tests java.nio.channels.FileChannel#write(ByteBuffer[])
     */
    public void test_write$LByteBuffer() throws Exception {
        ByteBuffer[] writeBuffers = new ByteBuffer[2];
        MockFileChannel mockFileChannel = new MockFileChannel();
        mockFileChannel.write(writeBuffers);
        // verify that calling write(ByteBuffer[] srcs) leads to the method
        // write(srcs, 0, srcs.length)
        assertTrue(mockFileChannel.isWriteCalled);
    }

    /**
     * @tests java.nio.channels.FileChannel#write(ByteBuffer[],int,int)
     */
    public void test_write$LByteBufferII_Null() throws Exception {
        ByteBuffer[] writeBuffers = null;

        try {
            readOnlyFileChannel.write(writeBuffers, 1, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            writeOnlyFileChannel.write(writeBuffers, 1, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            readWriteFileChannel.write(writeBuffers, 1, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        // first throws NullPointerException
        readWriteFileChannel.close();
        try {
            readWriteFileChannel.write(writeBuffers, 0, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#write(ByteBuffer[],int,int)
     */
    public void test_write$LByteBufferII_Closed() throws Exception {
        ByteBuffer[] writeBuffers = new ByteBuffer[2];
        writeBuffers[0] = ByteBuffer.allocate(CAPACITY);
        writeBuffers[1] = ByteBuffer.allocate(CAPACITY);

        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.write(writeBuffers, 0, 2);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }

        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.write(writeBuffers, 0, 2);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }

        readWriteFileChannel.close();
        try {
            readWriteFileChannel.write(writeBuffers, 0, 2);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }

        // throws ClosedChannelException first
        writeBuffers[0] = null;
        try {
            readWriteFileChannel.write(writeBuffers, 0, 2);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#write(ByteBuffer[],int,int)
     */
    public void test_write$LByteBufferII_ReadOnly() throws Exception {
        ByteBuffer[] writeBuffers = new ByteBuffer[2];
        writeBuffers[0] = ByteBuffer.allocate(CAPACITY);
        writeBuffers[1] = ByteBuffer.allocate(CAPACITY);

        try {
            readOnlyFileChannel.write(writeBuffers, 0, 2);
            fail();
        } catch (NonWritableChannelException e) {
        }

        try {
            readOnlyFileChannel.write(writeBuffers, 0, -1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }

        writeBuffers = null;
        try {
            readOnlyFileChannel.write(writeBuffers, 0, 1);
            fail();
        } catch (NullPointerException expected) {
        }

        readOnlyFileChannel.close();
    }

    /**
     * @tests java.nio.channels.FileChannel#write(ByteBuffer[],int,int)
     */
    public void test_write$LByteBufferII_EmptyBuffers() throws Exception {
        ByteBuffer[] writeBuffers = new ByteBuffer[2];
        try {
            writeOnlyFileChannel.write(writeBuffers, 0, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            readWriteFileChannel.write(writeBuffers, 0, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#write(ByteBuffer[],int,int)
     */
    public void test_write$LByteBufferII() throws Exception {
        ByteBuffer[] writeBuffers = new ByteBuffer[2];
        writeBuffers[0] = ByteBuffer.wrap(CONTENT_AS_BYTES);
        writeBuffers[1] = ByteBuffer.wrap(CONTENT_AS_BYTES);

        long result = writeOnlyFileChannel.write(writeBuffers, 0, 2);
        assertEquals(CONTENT_AS_BYTES_LENGTH * 2, result);
        assertEquals(CONTENT_AS_BYTES_LENGTH, writeBuffers[0].position());
        assertEquals(CONTENT_AS_BYTES_LENGTH, writeBuffers[1].position());
        writeOnlyFileChannel.close();

        assertEquals(CONTENT_AS_BYTES_LENGTH * 2, fileOfWriteOnlyFileChannel
                .length());

        fis = new FileInputStream(fileOfWriteOnlyFileChannel);
        byte[] inputBuffer = new byte[CONTENT_AS_BYTES_LENGTH];
        fis.read(inputBuffer);
        byte[] expectedResult = new byte[CONTENT_AS_BYTES_LENGTH * 2];
        System.arraycopy(CONTENT_AS_BYTES, 0, expectedResult, 0,
                CONTENT_AS_BYTES_LENGTH);
        System.arraycopy(CONTENT_AS_BYTES, 0, expectedResult,
                CONTENT_AS_BYTES_LENGTH, CONTENT_AS_BYTES_LENGTH);
        assertTrue(Arrays.equals(CONTENT_AS_BYTES, inputBuffer));
    }

    /**
     * @tests java.nio.channels.FileChannel#transferFrom(ReadableByteChannel,long,long)
     */
    public void test_transferFromLReadableByteChannelJJ_Closed()
            throws Exception {
        readByteChannel = DatagramChannel.open();
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.transferFrom(readByteChannel, 0, 0);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
            // expected
        }

        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.transferFrom(readByteChannel, 0, 10);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
            // expected
        }

        readWriteFileChannel.close();
        try {
            readWriteFileChannel.transferFrom(readByteChannel, 0, 0);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
            // expected
        }

        // should throw ClosedChannelException first.
        try {
            readWriteFileChannel.transferFrom(readByteChannel, 0, -1);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
            // expected
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#transferFrom(ReadableByteChannel,long,long)
     */
    public void test_transferFromLReadableByteChannelJJ_SourceClosed()
            throws Exception {
        readByteChannel = DatagramChannel.open();
        readByteChannel.close();

        try {
            readOnlyFileChannel.transferFrom(readByteChannel, 0, 10);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
            // expected
        }

        try {
            writeOnlyFileChannel.transferFrom(readByteChannel, 0, 10);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
            // expected
        }

        try {
            readWriteFileChannel.transferFrom(readByteChannel, 0, 10);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
            // expected
        }

        // should throw ClosedChannelException first.
        try {
            readWriteFileChannel.transferFrom(readByteChannel, 0, -1);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
            // expected
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#transferFrom(ReadableByteChannel,long,long)
     */
    public void test_transferFromLReadableByteChannelJJ_IllegalArgument()
            throws Exception {
        readByteChannel = DatagramChannel.open();
        try {
            writeOnlyFileChannel.transferFrom(readByteChannel, 10, -1);
            fail("should throw IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            readWriteFileChannel.transferFrom(readByteChannel, -1, -10);
            fail("should throw IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#transferFrom(ReadableByteChannel,long,long)
     */
    public void test_transferFromLReadableByteChannelJJ_NonWritable()
            throws Exception {
        readByteChannel = DatagramChannel.open();
        try {
            readOnlyFileChannel.transferFrom(readByteChannel, 0, 0);
            fail("should throw NonWritableChannelException.");
        } catch (NonWritableChannelException e) {
            // expected
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#transferFrom(ReadableByteChannel,long,long)
     */
    public void test_transferFromLReadableByteChannelJJ_SourceNonReadable()
            throws Exception {
        try {
            readWriteFileChannel.transferFrom(writeOnlyFileChannel, 0, 0);
            fail("should throw NonReadableChannelException.");
        } catch (NonReadableChannelException e) {
            // expected
        }

        // not throws NonReadableChannelException first if position beyond file
        // size.
        readWriteFileChannel.transferFrom(writeOnlyFileChannel, 10, 10);
    }

    /**
     * @tests java.nio.channels.FileChannel#transferFrom(ReadableByteChannel,long,long)
     */
    public void test_transferFromLReadableByteChannelJJ_PositionBeyondSize()
            throws Exception {
        // init data to file.
        writeDataToFile(fileOfReadOnlyFileChannel);
        writeDataToFile(fileOfWriteOnlyFileChannel);

        final int READONLYFILECHANNELPOSITION = 2;
        readOnlyFileChannel.position(READONLYFILECHANNELPOSITION);

        final int POSITION = CONTENT_AS_BYTES_LENGTH * 2;
        final int LENGTH = 5;
        long result = writeOnlyFileChannel.transferFrom(readOnlyFileChannel,
                POSITION, LENGTH);
        assertEquals(0, result);
        assertEquals(0, writeOnlyFileChannel.position());
        assertEquals(READONLYFILECHANNELPOSITION, readOnlyFileChannel
                .position());
    }

    /**
     * @tests java.nio.channels.FileChannel#transferFrom(ReadableByteChannel,long,long)
     */
    public void test_transferFromLReadableByteChannelJJ_FileChannel()
            throws Exception {
        // init data to file.
        writeDataToFile(fileOfReadOnlyFileChannel);
        writeDataToFile(fileOfWriteOnlyFileChannel);

        final int READONLYFILECHANNELPOSITION = 2;
        final int WRITEONLYFILECHANNELPOSITION = 4;
        readOnlyFileChannel.position(READONLYFILECHANNELPOSITION);
        writeOnlyFileChannel.position(WRITEONLYFILECHANNELPOSITION);

        final int POSITION = 3;
        final int LENGTH = 5;
        long result = writeOnlyFileChannel.transferFrom(readOnlyFileChannel,
                POSITION, LENGTH);
        assertEquals(LENGTH, result);
        assertEquals(WRITEONLYFILECHANNELPOSITION, writeOnlyFileChannel
                .position());
        assertEquals(READONLYFILECHANNELPOSITION + LENGTH, readOnlyFileChannel
                .position());
        writeOnlyFileChannel.close();

        final int EXPECTED_LENGTH = POSITION + LENGTH;
        fis = new FileInputStream(fileOfWriteOnlyFileChannel);
        byte[] resultContent = new byte[EXPECTED_LENGTH];
        fis.read(resultContent);

        byte[] expectedContent = new byte[EXPECTED_LENGTH];
        System.arraycopy(CONTENT_AS_BYTES, 0, expectedContent, 0, POSITION);
        System.arraycopy(CONTENT_AS_BYTES, READONLYFILECHANNELPOSITION,
                expectedContent, POSITION, LENGTH);
        assertTrue(Arrays.equals(expectedContent, resultContent));
    }

    /**
     * @tests java.nio.channels.FileChannel#transferFrom(ReadableByteChannel,long,long)
     */
    public void test_transferFromLReadableByteChannelJJ_DatagramChannel()
            throws Exception {
        // connects two datagramChannels.
        datagramChannelReceiver = DatagramChannel.open();
        datagramChannelReceiver.socket().bind(
                new InetSocketAddress(InetAddress.getLocalHost(), 0));
        datagramChannelSender = DatagramChannel.open();
        datagramChannelSender.socket().bind(
                new InetSocketAddress(InetAddress.getLocalHost(), 0));
        datagramChannelReceiver.socket().setSoTimeout(TIME_OUT);
        datagramChannelReceiver.connect(datagramChannelSender.socket()
                .getLocalSocketAddress());
        datagramChannelSender.socket().setSoTimeout(TIME_OUT);
        ByteBuffer writeBuffer = ByteBuffer.wrap(CONTENT_AS_BYTES);
        datagramChannelSender.socket().setSoTimeout(TIME_OUT);
        // sends data from datagramChannelSender to datagramChannelReceiver.
        datagramChannelSender.send(writeBuffer, datagramChannelReceiver
                .socket().getLocalSocketAddress());
        datagramChannelReceiver.socket().setSoTimeout(TIME_OUT);

        // transfers data from datagramChannelReceiver to fileChannel.
        long result = writeOnlyFileChannel.transferFrom(
                datagramChannelReceiver, 0, CONTENT_AS_BYTES_LENGTH);
        assertEquals(CONTENT_AS_BYTES_LENGTH, result);
        assertEquals(0, writeOnlyFileChannel.position());
        writeOnlyFileChannel.close();

        // gets content from file.
        fis = new FileInputStream(fileOfWriteOnlyFileChannel);
        assertEquals(CONTENT_AS_BYTES_LENGTH, fileOfWriteOnlyFileChannel
                .length());
        byte[] resultContent = new byte[CONTENT_AS_BYTES_LENGTH];
        fis.read(resultContent);

        // compares contents.
        assertTrue(Arrays.equals(CONTENT_AS_BYTES, resultContent));
    }

    /**
     * @tests java.nio.channels.FileChannel#transferFrom(ReadableByteChannel,long,long)
     */
    public void test_transferFromLReadableByteChannelJJ_SocketChannel()
            throws Exception {
        // connects two socketChannels.
        socketChannelReceiver = SocketChannel.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(
                new InetSocketAddress(InetAddress.getLocalHost(), 0));
        socketChannelReceiver.socket().setSoTimeout(TIME_OUT);
        socketChannelReceiver.connect(serverSocketChannel.socket()
                .getLocalSocketAddress());
        serverSocketChannel.socket().setSoTimeout(TIME_OUT);
        socketChannelSender = serverSocketChannel.accept();
        socketChannelSender.socket().setSoTimeout(TIME_OUT);

        // sends data from socketChannelSender to socketChannelReceiver.
        ByteBuffer writeBuffer = ByteBuffer.wrap(CONTENT_AS_BYTES);
        socketChannelSender.write(writeBuffer);

        // transfers data from socketChannelReceiver to fileChannel.
        long result = readWriteFileChannel.transferFrom(socketChannelReceiver,
                0, CONTENT_AS_BYTES_LENGTH);
        assertEquals(CONTENT_AS_BYTES_LENGTH, result);
        assertEquals(0, readWriteFileChannel.position());
        readWriteFileChannel.close();

        // gets content from file.
        fis = new FileInputStream(fileOfReadWriteFileChannel);
        assertEquals(CONTENT_AS_BYTES_LENGTH, fileOfReadWriteFileChannel
                .length());
        byte[] resultContent = new byte[CONTENT_AS_BYTES_LENGTH];
        fis.read(resultContent);

        // compares content.
        assertTrue(Arrays.equals(CONTENT_AS_BYTES, resultContent));
    }

    /**
     * @tests java.nio.channels.FileChannel#transferFrom(ReadableByteChannel,long,long)
     */
    public void test_transferFromLReadableByteChannelJJ_Pipe() throws Exception {
        // inits data in file.
        writeDataToFile(fileOfWriteOnlyFileChannel);

        // inits pipe.
        pipe = Pipe.open();

        // writes content to pipe.
        ByteBuffer writeBuffer = ByteBuffer.wrap(CONTENT_AS_BYTES);
        pipe.sink().write(writeBuffer);

        // transfers data from pipe to fileChannel.
        final int OFFSET = 2;
        final int LENGTH = 4;
        long result = writeOnlyFileChannel.transferFrom(pipe.source(), OFFSET,
                LENGTH);
        assertEquals(LENGTH, result);
        writeOnlyFileChannel.close();

        // gets content from file.
        fis = new FileInputStream(fileOfWriteOnlyFileChannel);
        byte[] resultBytes = new byte[OFFSET + LENGTH];
        fis.read(resultBytes);

        // compares content.
        byte[] expectedBytes = new byte[OFFSET + LENGTH];
        System.arraycopy(CONTENT_AS_BYTES, 0, expectedBytes, 0, OFFSET);
        System.arraycopy(CONTENT_AS_BYTES, 0, expectedBytes, OFFSET, LENGTH);

        assertTrue(Arrays.equals(expectedBytes, resultBytes));
    }

    /**
     * @tests java.nio.channels.FileChannel#transferTo(long,long,WritableByteChannel)
     */
    public void test_transferToJJLWritableByteChannel_Null() throws Exception {
        writableByteChannel = null;
        try {
            readOnlyFileChannel.transferTo(0, 10, writableByteChannel);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            writeOnlyFileChannel.transferTo(0, 10, writableByteChannel);
            fail();
        } catch (NullPointerException expected) {
        } catch (NonReadableChannelException expected) {
        }

        try {
            readWriteFileChannel.transferTo(0, 10, writableByteChannel);
            fail();
        } catch (NullPointerException expected) {
        }

        readOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.transferTo(-1, 0, writableByteChannel);
            fail();
        } catch (NullPointerException expected) {
        } catch (NonReadableChannelException expected) {
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#transferTo(long,long,WritableByteChannel)
     */
    public void test_transferToJJLWritableByteChannel_Closed() throws Exception {
        writableByteChannel = DatagramChannel.open();
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.transferTo(0, 10, writableByteChannel);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
            // expected
        }

        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.transferTo(0, 10, writableByteChannel);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
            // expected
        }

        readWriteFileChannel.close();
        try {
            readWriteFileChannel.transferTo(0, 10, writableByteChannel);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
            // expected
        }

        // should throw ClosedChannelException first.
        try {
            readWriteFileChannel.transferTo(0, -1, writableByteChannel);
            fail("should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
            // expected
        }
    }

    public void test_transferToJJLWritableByteChannel_SourceClosed() throws Exception {
        writableByteChannel = DatagramChannel.open();
        writableByteChannel.close();

        try {
            readOnlyFileChannel.transferTo(0, 10, writableByteChannel);
            fail();
        } catch (ClosedChannelException expected) {
        }

        try {
            writeOnlyFileChannel.transferTo(0, 10, writableByteChannel);
            fail();
        } catch (ClosedChannelException expected) {
        } catch (NonReadableChannelException expected) {
        }

        try {
            readWriteFileChannel.transferTo(0, 10, writableByteChannel);
            fail();
        } catch (ClosedChannelException expected) {
        }

        try {
            readWriteFileChannel.transferTo(0, -1, writableByteChannel);
            fail();
        } catch (ClosedChannelException expected) {
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#transferTo(long,long,WritableByteChannel)
     */
    public void test_transferToJJLWritableByteChannel_IllegalArgument()
            throws Exception {
        writableByteChannel = DatagramChannel.open();
        try {
            readOnlyFileChannel.transferTo(10, -1, writableByteChannel);
            fail("should throw IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            readWriteFileChannel.transferTo(-1, -10, writableByteChannel);
            fail("should throw IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void test_transferToJJLWritableByteChannel_NonReadable() throws Exception {
        writableByteChannel = DatagramChannel.open();
        try {
            writeOnlyFileChannel.transferTo(0, 10, writableByteChannel);
            fail();
        } catch (NonReadableChannelException expected) {
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#transferTo(long,long,WritableByteChannel)
     */
    public void test_transferToJJLWritableByteChannel_TargetNonWritable()
            throws Exception {
        try {
            readWriteFileChannel.transferTo(0, 0, readOnlyFileChannel);
            fail("should throw NonWritableChannelException.");
        } catch (NonWritableChannelException e) {
            // expected
        }

        // first throws NonWritableChannelException even position out of file
        // size.
        try {
            readWriteFileChannel.transferTo(10, 10, readOnlyFileChannel);
            fail("should throw NonWritableChannelException.");
        } catch (NonWritableChannelException e) {
            // expected
        }

        // regression test for Harmony-941
        // first throws NonWritableChannelException even arguments are illegal.
        try {
            readWriteFileChannel.transferTo(-1, 10, readOnlyFileChannel);
            fail("should throw NonWritableChannelException.");
        } catch (NonWritableChannelException e) {
            // expected
        }

        try {
            readWriteFileChannel.transferTo(0, -1, readOnlyFileChannel);
            fail("should throw NonWritableChannelException.");
        } catch (NonWritableChannelException e) {
            // expected
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#transferTo(long,long,WritableByteChannel)
     */
    public void test_transferToJJLWritableByteChannel_PositionBeyondSize()
            throws Exception {
        // init data to file.
        writeDataToFile(fileOfReadOnlyFileChannel);
        writeDataToFile(fileOfWriteOnlyFileChannel);

        final int WRITEONLYFILECHANNELPOSITION = 2;
        writeOnlyFileChannel.position(WRITEONLYFILECHANNELPOSITION);

        final int POSITION = CONTENT_AS_BYTES_LENGTH * 2;
        final int LENGTH = 5;
        long result = readOnlyFileChannel.transferTo(POSITION, LENGTH,
                writeOnlyFileChannel);
        assertEquals(0, result);
        assertEquals(0, readOnlyFileChannel.position());
        assertEquals(WRITEONLYFILECHANNELPOSITION, writeOnlyFileChannel
                .position());
    }

    /**
     * @tests java.nio.channels.FileChannel#transferTo(long,long,WritableByteChannel)
     */
    public void test_transferToJJLWritableByteChannel_FileChannel()
            throws Exception {
        // init data to file.
        writeDataToFile(fileOfReadOnlyFileChannel);
        writeDataToFile(fileOfWriteOnlyFileChannel);

        final int READONLYFILECHANNELPOSITION = 2;
        final int WRITEONLYFILECHANNELPOSITION = 4;
        readOnlyFileChannel.position(READONLYFILECHANNELPOSITION);
        writeOnlyFileChannel.position(WRITEONLYFILECHANNELPOSITION);

        final int POSITION = 3;
        final int LENGTH = 5;
        long result = readOnlyFileChannel.transferTo(POSITION, LENGTH,
                writeOnlyFileChannel);
        assertEquals(LENGTH, result);
        assertEquals(READONLYFILECHANNELPOSITION, readOnlyFileChannel
                .position());
        assertEquals(WRITEONLYFILECHANNELPOSITION + LENGTH,
                writeOnlyFileChannel.position());
        writeOnlyFileChannel.close();

        final int EXPECTED_LENGTH = WRITEONLYFILECHANNELPOSITION + LENGTH;
        fis = new FileInputStream(fileOfWriteOnlyFileChannel);
        byte[] resultContent = new byte[EXPECTED_LENGTH];
        fis.read(resultContent);

        byte[] expectedContent = new byte[EXPECTED_LENGTH];
        System.arraycopy(CONTENT_AS_BYTES, 0, expectedContent, 0,
                WRITEONLYFILECHANNELPOSITION);
        System.arraycopy(CONTENT_AS_BYTES, POSITION, expectedContent,
                WRITEONLYFILECHANNELPOSITION, LENGTH);
        assertTrue(Arrays.equals(expectedContent, resultContent));
    }

    /**
     * @tests java.nio.channels.FileChannel#transferTo(long,long,WritableByteChannel)
     */
    public void test_transferToJJLWritableByteChannel_SocketChannel()
            throws Exception {
        // inits data into file.
        writeDataToFile(fileOfReadOnlyFileChannel);

        // connects two socketChannels.
        socketChannelReceiver = SocketChannel.open();
        socketChannelReceiver.socket().bind(
                new InetSocketAddress(InetAddress.getLocalHost(), 0));
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(
                new InetSocketAddress(InetAddress.getLocalHost(), 0));
        socketChannelReceiver.socket().setSoTimeout(TIME_OUT);
        socketChannelReceiver.connect(serverSocketChannel.socket()
                .getLocalSocketAddress());
        serverSocketChannel.socket().setSoTimeout(TIME_OUT);
        socketChannelSender = serverSocketChannel.accept();
        socketChannelSender.socket().setSoTimeout(TIME_OUT);

        // position here should have no effect on transferTo since it uses
        // offset from file_begin
        final int POSITION = 10;
        readOnlyFileChannel.position(POSITION);

        // transfers data from file to socketChannelSender.
        final int OFFSET = 2;
        long result = readOnlyFileChannel.transferTo(OFFSET,
                CONTENT_AS_BYTES_LENGTH * 2, socketChannelSender);
        final int LENGTH = CONTENT_AS_BYTES_LENGTH - OFFSET;
        assertEquals(LENGTH, result);
        assertEquals(POSITION, readOnlyFileChannel.position());
        readOnlyFileChannel.close();
        socketChannelSender.close();

        // gets contents from socketChannelReceiver.
        ByteBuffer readBuffer = ByteBuffer.allocate(LENGTH + 1);
        int totalRead = 0;
        int countRead = 0;
        long beginTime = System.currentTimeMillis();
        while ((countRead = socketChannelReceiver.read(readBuffer)) != -1) {
            totalRead += countRead;
            // TIMEOUT
            if (System.currentTimeMillis() - beginTime > TIME_OUT) {
                break;
            }
        }
        assertEquals(LENGTH, totalRead);

        // compares contents.
        readBuffer.flip();
        for (int i = OFFSET; i < CONTENT_AS_BYTES_LENGTH; i++) {
            assertEquals(CONTENT_AS_BYTES[i], readBuffer.get());
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#transferTo(long,long,WritableByteChannel)
     */
    public void test_transferToJJLWritableByteChannel_DatagramChannel()
            throws Exception {
        // inits data to file.
        writeDataToFile(fileOfReadOnlyFileChannel);

        // connects two datagramChannel
        datagramChannelReceiver = DatagramChannel.open();
        datagramChannelReceiver.socket().bind(
                new InetSocketAddress(InetAddress.getLocalHost(), 0));
        datagramChannelSender = DatagramChannel.open();
        datagramChannelSender.socket().bind(
                new InetSocketAddress(InetAddress.getLocalHost(), 0));
        datagramChannelSender.socket().setSoTimeout(TIME_OUT);
        datagramChannelSender.connect(datagramChannelReceiver.socket()
                .getLocalSocketAddress());
        datagramChannelReceiver.socket().setSoTimeout(TIME_OUT);
        datagramChannelReceiver.connect(datagramChannelSender.socket()
                .getLocalSocketAddress());

        // transfers data from fileChannel to datagramChannelSender
        long result = readOnlyFileChannel.transferTo(0,
                CONTENT_AS_BYTES_LENGTH, datagramChannelSender);
        assertEquals(CONTENT_AS_BYTES_LENGTH, result);
        assertEquals(0, readOnlyFileChannel.position());
        readOnlyFileChannel.close();
        datagramChannelSender.close();

        // gets contents from datagramChannelReceiver
        ByteBuffer readBuffer = ByteBuffer.allocate(CONTENT_AS_BYTES_LENGTH);
        long beginTime = System.currentTimeMillis();
        int totalRead = 0;
        while (totalRead < CONTENT_AS_BYTES_LENGTH) {
            totalRead += datagramChannelReceiver.read(readBuffer);
            if (System.currentTimeMillis() - beginTime > TIME_OUT) {
                break;
            }
        }
        assertEquals(CONTENT_AS_BYTES_LENGTH, totalRead);

        // compares contents.
        readBuffer.flip();
        for (int i = 0; i < CONTENT_AS_BYTES_LENGTH; i++) {
            assertEquals(CONTENT_AS_BYTES[i], readBuffer.get());
        }
    }

    /**
     * @tests java.nio.channels.FileChannel#transferTo(long,long,WritableByteChannel)
     */
    public void test_transferToJJLWritableByteChannel_Pipe() throws Exception {
        // inits data in file.
        writeDataToFile(fileOfReadOnlyFileChannel);

        // inits pipe.
        pipe = Pipe.open();

        // transfers data from fileChannel to pipe.
        final int OFFSET = 2;
        final int LENGTH = 4;
        long result = readOnlyFileChannel.transferTo(OFFSET, LENGTH, pipe
                .sink());
        assertEquals(LENGTH, result);
        assertEquals(0, readOnlyFileChannel.position());
        readOnlyFileChannel.close();

        // gets content from pipe.
        ByteBuffer readBuffer = ByteBuffer.allocate(LENGTH);
        result = pipe.source().read(readBuffer);
        assertEquals(LENGTH, result);

        // compares content.
        readBuffer.flip();
        for (int i = OFFSET; i < OFFSET + LENGTH; i++) {
            assertEquals(CONTENT_AS_BYTES[i], readBuffer.get());
        }
    }

    /**
     * Regression test for Harmony-3324
     * Make sure we could delete the file after we called transferTo() method.
     */
    public void test_transferTo_couldDelete() throws Exception {
        // init data in files
        writeDataToFile(fileOfReadOnlyFileChannel);
        writeDataToFile(fileOfWriteOnlyFileChannel);

        // call transferTo() method
        readOnlyFileChannel.transferTo(0 , 2, writeOnlyFileChannel);

        // delete both files
        readOnlyFileChannel.close();
        writeOnlyFileChannel.close();
        boolean rDel = fileOfReadOnlyFileChannel.delete();
        boolean wDel = fileOfWriteOnlyFileChannel.delete();

        // make sure both files were deleted
        assertTrue("File " + readOnlyFileChannel + " exists", rDel);
        assertTrue("File " + writeOnlyFileChannel + " exists", wDel);
    }

    /**
     * Regression test for Harmony-3324
     * Make sure we could delete the file after we called transferFrom() method.
     */
    public void test_transferFrom_couldDelete() throws Exception {
        // init data in files
        writeDataToFile(fileOfReadOnlyFileChannel);
        writeDataToFile(fileOfWriteOnlyFileChannel);

        // call transferTo() method
        writeOnlyFileChannel.transferFrom(readOnlyFileChannel, 0 , 2);

        // delete both files
        readOnlyFileChannel.close();
        writeOnlyFileChannel.close();
        boolean rDel = fileOfReadOnlyFileChannel.delete();
        boolean wDel = fileOfWriteOnlyFileChannel.delete();

        // make sure both files were deleted
        assertTrue("File " + readOnlyFileChannel + " exists", rDel);
        assertTrue("File " + writeOnlyFileChannel + " exists", wDel);
    }

    private class MockFileChannel extends FileChannel {

        private boolean isLockCalled = false;

        private boolean isTryLockCalled = false;

        private boolean isReadCalled = false;

        private boolean isWriteCalled = false;

        public void force(boolean arg0) throws IOException {
            // do nothing
        }

        public FileLock lock(long position, long size, boolean shared)
                throws IOException {
            // verify that calling lock() leads to the method
            // lock(0, Long.MAX_VALUE, false).
            if (0 == position && Long.MAX_VALUE == size && false == shared) {
                isLockCalled = true;
            }
            return null;
        }

        public MappedByteBuffer map(MapMode arg0, long arg1, long arg2)
                throws IOException {
            return null;
        }

        public long position() throws IOException {
            return 0;
        }

        public FileChannel position(long arg0) throws IOException {
            return null;
        }

        public int read(ByteBuffer arg0) throws IOException {
            return 0;
        }

        public int read(ByteBuffer arg0, long arg1) throws IOException {
            return 0;
        }

        public long read(ByteBuffer[] srcs, int offset, int length)
                throws IOException {
            // verify that calling read(ByteBuffer[] srcs) leads to the method
            // read(srcs, 0, srcs.length)
            if (0 == offset && length == srcs.length) {
                isReadCalled = true;
            }
            return 0;
        }

        public long size() throws IOException {
            return 0;
        }

        public long transferFrom(ReadableByteChannel arg0, long arg1, long arg2)
                throws IOException {
            return 0;
        }

        public long transferTo(long arg0, long arg1, WritableByteChannel arg2)
                throws IOException {
            return 0;
        }

        public FileChannel truncate(long arg0) throws IOException {
            return null;
        }

        public FileLock tryLock(long position, long size, boolean shared)
                throws IOException {
            // verify that calling tryLock() leads to the method
            // tryLock(0, Long.MAX_VALUE, false).
            if (0 == position && Long.MAX_VALUE == size && false == shared) {
                isTryLockCalled = true;
            }
            return null;
        }

        public int write(ByteBuffer arg0) throws IOException {
            return 0;
        }

        public int write(ByteBuffer arg0, long arg1) throws IOException {
            return 0;
        }

        public long write(ByteBuffer[] srcs, int offset, int length)
                throws IOException {
            // verify that calling write(ByteBuffer[] srcs) leads to the method
            // write(srcs, 0, srcs.length)
            if(0 == offset && length == srcs.length){
                isWriteCalled = true;
            }
            return 0;
        }

        protected void implCloseChannel() throws IOException {

        }
    }
}
