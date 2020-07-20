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

package libcore.java.nio.channels;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.OverlappingFileLockException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import junit.framework.TestCase;

public final class OldFileChannelTest extends TestCase {

    private static final int CAPACITY = 100;

    private static final String CONTENT = "MYTESTSTRING needs to be a little long";

    private static final byte[] TEST_BYTES;

    static {
        try {
            TEST_BYTES = "test".getBytes("iso8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    private static final int CONTENT_LENGTH = CONTENT.length();

    private static final byte[] CONTENT_AS_BYTES = CONTENT.getBytes();

    private static final int CONTENT_AS_BYTES_LENGTH = CONTENT_AS_BYTES.length;

    private FileChannel readOnlyFileChannel;

    private FileChannel writeOnlyFileChannel;

    private FileChannel readWriteFileChannel;

    private File fileOfReadOnlyFileChannel;

    private File fileOfWriteOnlyFileChannel;

    private File fileOfReadWriteFileChannel;

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
        writeOnlyFileChannel = new FileOutputStream(fileOfWriteOnlyFileChannel)
                .getChannel();
        readWriteFileChannel = new RandomAccessFile(fileOfReadWriteFileChannel,
                "rw").getChannel();
    }

    protected void tearDown() {
        if (null != readOnlyFileChannel) {
            try {
                readOnlyFileChannel.close();
            } catch (IOException e) {
                // do nothing
            }
        }
        if (null != writeOnlyFileChannel) {
            try {
                writeOnlyFileChannel.close();
            } catch (IOException e) {
                // do nothing
            }
        }
        if (null != readWriteFileChannel) {
            try {
                readWriteFileChannel.close();
            } catch (IOException e) {
                // do nothing
            }
        }
        if (null != fis) {
            try {
                fis.close();
            } catch (IOException e) {
                // do nothing
            }
        }

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
    }

    public void test_forceZ() throws Exception {
        ByteBuffer writeBuffer = ByteBuffer.wrap(CONTENT_AS_BYTES);
        writeOnlyFileChannel.write(writeBuffer);
        writeOnlyFileChannel.force(true);

        byte[] readBuffer = new byte[CONTENT_AS_BYTES_LENGTH];
        fis = new FileInputStream(fileOfWriteOnlyFileChannel);
        fis.read(readBuffer);
        assertTrue(Arrays.equals(CONTENT_AS_BYTES, readBuffer));

        writeOnlyFileChannel.write(writeBuffer);
        writeOnlyFileChannel.force(false);
        fis.close();

        readBuffer = new byte[CONTENT_AS_BYTES_LENGTH];
        fis = new FileInputStream(fileOfWriteOnlyFileChannel);
        fis.read(readBuffer);
        assertTrue(Arrays.equals(CONTENT_AS_BYTES, readBuffer));
        fis.close();
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
    private void writeLargeDataToFile(File file, int size)
            throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream(file);
        byte[] buf = new byte[size];

        try {
            // we don't care about content - just need a particular file size
            fos.write(buf);
        } finally {
            fos.close();
        }
    }

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

    public void testTryLockVeryLarge() throws IOException {
        long tooBig = Integer.MAX_VALUE + 1L;
        FileLock lock = readWriteFileChannel.tryLock(tooBig, 1, false);
        assertLockFails(tooBig, 1);
        lock.release();

        lock = readWriteFileChannel.tryLock(0, tooBig, false);
        assertLockFails(0, 1);
        lock.release();
    }

    public void testTryLockOverlapping() throws IOException {
        FileLock lockOne = readWriteFileChannel.tryLock(0, 10, false);
        FileLock lockTwo = readWriteFileChannel.tryLock(10, 20, false);
        assertLockFails(0, 10);
        lockOne.release();
        assertLockFails(5, 10);
        lockOne = readWriteFileChannel.tryLock(0, 10, false);
        lockTwo.release();
        lockOne.release();
    }

    public void test_readLByteBufferJ_IllegalArgument() throws Exception {
        ByteBuffer readBuffer = ByteBuffer.allocate(CAPACITY);

        try {
            readOnlyFileChannel.read(readBuffer, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            writeOnlyFileChannel.read(readBuffer, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            readWriteFileChannel.read(readBuffer, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        // throws IllegalArgumentException first.
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.read(readBuffer, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.read(readBuffer, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        readWriteFileChannel.close();
        try {
            readWriteFileChannel.read(readBuffer, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void test_read$LByteBufferII_Null() throws Exception {

        try {
            readOnlyFileChannel.read(null, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            readOnlyFileChannel.read(null, 0, 3);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            readOnlyFileChannel.read(null, 1, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            readOnlyFileChannel.read(null, 2, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            readOnlyFileChannel.read(null, 3, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            writeOnlyFileChannel.read(null, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            writeOnlyFileChannel.read(null, 0, 3);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            writeOnlyFileChannel.read(null, 1, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            writeOnlyFileChannel.read(null, 2, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            writeOnlyFileChannel.read(null, 3, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            readWriteFileChannel.read(null, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            readWriteFileChannel.read(null, 0, 3);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            readWriteFileChannel.read(null, 1, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            readWriteFileChannel.read(null, 2, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            readWriteFileChannel.read(null, 3, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        // first throws NullPointerException
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.read(null, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            readOnlyFileChannel.read(null, 0, 3);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            readOnlyFileChannel.read(null, 1, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            readOnlyFileChannel.read(null, 2, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            readOnlyFileChannel.read(null, 3, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        readWriteFileChannel.close();
        try {
            readWriteFileChannel.read(null, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            readWriteFileChannel.read(null, 0, 3);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            readWriteFileChannel.read(null, 1, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            readWriteFileChannel.read(null, 2, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            readWriteFileChannel.read(null, 3, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.read(null, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            writeOnlyFileChannel.read(null, 0, 3);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            writeOnlyFileChannel.read(null, 1, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            writeOnlyFileChannel.read(null, 2, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            writeOnlyFileChannel.read(null, 3, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }


    private void doTestForIOOBException(FileChannel channel,
            ByteBuffer[] buffer) throws IOException{
        try {
            channel.read(buffer, -1, 0);
            fail("should throw IndexOutOfBoundException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            channel.read(buffer, 0, -1);
            fail("should throw IndexOutOfBoundException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            channel.read(buffer, 0, 3);
            fail("should throw IndexOutOfBoundException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            channel.read(buffer, 1, 2);
            fail("should throw IndexOutOfBoundException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            channel.read(buffer, 2, 1);
            fail("should throw IndexOutOfBoundException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            channel.read(buffer, 3, 0);
            fail("should throw IndexOutOfBoundException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    public void test_read$LByteBufferII_IndexOutOfBound() throws Exception {
        ByteBuffer[] readBuffers = new ByteBuffer[2];
        readBuffers[0] = ByteBuffer.allocate(CAPACITY);
        readBuffers[1] = ByteBuffer.allocate(CAPACITY);
        ByteBuffer[] readBuffersNull = new ByteBuffer[2];

        doTestForIOOBException(readOnlyFileChannel, readBuffers);
        doTestForIOOBException(readWriteFileChannel, readBuffers);
        doTestForIOOBException(writeOnlyFileChannel, readBuffers);

        doTestForIOOBException(readOnlyFileChannel, readBuffersNull);
        doTestForIOOBException(readWriteFileChannel, readBuffersNull);
        doTestForIOOBException(writeOnlyFileChannel, readBuffersNull);
        try {
            readOnlyFileChannel.read(null, -1, 0);
            fail("should throw IndexOutOfBoundException");
        } catch (NullPointerException expected) {
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            readOnlyFileChannel.read(null, 0, -1);
            fail("should throw IndexOutOfBoundException");
        } catch (NullPointerException expected) {
        } catch (IndexOutOfBoundsException expected) {
        }

        try {
            readWriteFileChannel.read(null, -1, 0);
            fail("should throw IndexOutOfBoundException");
        } catch (NullPointerException expected) {
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            readWriteFileChannel.read(null, 0, -1);
            fail("should throw IndexOutOfBoundException");
        } catch (NullPointerException expected) {
        } catch (IndexOutOfBoundsException expected) {
        }

        try {
            writeOnlyFileChannel.read(null, -1, 0);
            fail("should throw IndexOutOfBoundException");
        } catch (NullPointerException expected) {
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            writeOnlyFileChannel.read(null, 0, -1);
            fail("should throw IndexOutOfBoundException");
        } catch (NullPointerException expected) {
        } catch (IndexOutOfBoundsException expected) {
        }

        readOnlyFileChannel.close();
        doTestForIOOBException(readOnlyFileChannel, readBuffers);
        doTestForIOOBException(readOnlyFileChannel, readBuffersNull);

        readWriteFileChannel.close();
        doTestForIOOBException(readWriteFileChannel, readBuffers);
        doTestForIOOBException(readWriteFileChannel, readBuffersNull);

        writeOnlyFileChannel.close();
        doTestForIOOBException(writeOnlyFileChannel, readBuffers);
        doTestForIOOBException(writeOnlyFileChannel, readBuffersNull);
    }

    public void test_read$LByteBufferII_EmptyFile() throws Exception {
        ByteBuffer[] readBuffers = new ByteBuffer[2];
        readBuffers[0] = ByteBuffer.allocate(CAPACITY);
        readBuffers[1] = ByteBuffer.allocate(CAPACITY);
        long result = readOnlyFileChannel.read(readBuffers, 0, 2);
        assertEquals(-1, result);
        assertEquals(0, readBuffers[0].position());
        assertEquals(0, readBuffers[1].position());
    }

    public void test_read$LByteBufferII_EmptyBuffers() throws Exception {
        ByteBuffer[] readBuffers = new ByteBuffer[2];
        readBuffers[0] = ByteBuffer.allocate(CAPACITY);

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

    public void test_isOpen() throws Exception {
        // Regression for HARMONY-40
        File logFile = File.createTempFile("out", "tmp");
        logFile.deleteOnExit();
        FileOutputStream out = new FileOutputStream(logFile, true);
        FileChannel channel = out.getChannel();
        out.write(1);
        assertTrue("Assert 0: Channel is not open", channel.isOpen());
        out.close();
        assertFalse("Assert 0: Channel is still open", channel.isOpen());
    }

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
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }

        // should throw ClosedChannelException first
        try {
            readWriteFileChannel.read((ByteBuffer) null);
            fail("should throw ClosedChannelException");
        } catch (NullPointerException e) {
        } catch (ClosedChannelException e) {
        }

        try {
            readOnlyFileChannel.write((ByteBuffer) null);
            fail("should throw ClosedChannelException");
        } catch (NullPointerException e) {
        } catch (ClosedChannelException e) {
        }

        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.write((ByteBuffer) null);
            fail("should throw ClosedChannelException");
        } catch (NullPointerException e) {
        } catch (ClosedChannelException e) {
        }
    }

    public void test_writeLByteBufferJ_Postion_As_Long() throws Exception {
        ByteBuffer writeBuffer = ByteBuffer.wrap(TEST_BYTES);
        try {
            writeOnlyFileChannel.write(writeBuffer, Long.MAX_VALUE);
        } catch (IOException e) {
            // expected
        }
    }

    public void test_writeLByteBufferJ_IllegalArgument() throws Exception {
        ByteBuffer writeBuffer = ByteBuffer.allocate(CAPACITY);

        try {
            readOnlyFileChannel.write(writeBuffer, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            writeOnlyFileChannel.write(writeBuffer, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            readWriteFileChannel.write(writeBuffer, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        // throws IllegalArgumentException first.
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.write(writeBuffer, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.write(writeBuffer, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        readWriteFileChannel.close();
        try {
            readWriteFileChannel.write(writeBuffer, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void test_writeLByteBufferJ_NonZeroPosition() throws Exception {
        final int pos = 5;
        ByteBuffer writeBuffer = ByteBuffer.wrap(CONTENT_AS_BYTES);
        writeBuffer.position(pos);
        int result = writeOnlyFileChannel.write(writeBuffer, pos);
        assertEquals(CONTENT_AS_BYTES_LENGTH - pos, result);
        assertEquals(CONTENT_AS_BYTES_LENGTH, writeBuffer.position());
        writeOnlyFileChannel.close();

        assertEquals(CONTENT_AS_BYTES_LENGTH, fileOfWriteOnlyFileChannel
                .length());

        fis = new FileInputStream(fileOfWriteOnlyFileChannel);
        byte[] inputBuffer = new byte[CONTENT_AS_BYTES_LENGTH - pos];
        fis.skip(pos);
        fis.read(inputBuffer);
        String test = CONTENT.substring(pos);
        assertTrue(Arrays.equals(test.getBytes(), inputBuffer));
    }

    public void test_write$LByteBuffer_Closed() throws Exception {
        ByteBuffer[] writeBuffers = new ByteBuffer[2];
        writeBuffers[0] = ByteBuffer.allocate(CAPACITY);
        writeBuffers[1] = ByteBuffer.allocate(CAPACITY);

        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.write(writeBuffers);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }

        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.write(writeBuffers);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }

        readWriteFileChannel.close();
        try {
            readWriteFileChannel.write(writeBuffers);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }
    }

    public void test_write$LByteBuffer_ReadOnly() throws Exception {
        ByteBuffer[] writeBuffers = new ByteBuffer[2];
        writeBuffers[0] = ByteBuffer.allocate(CAPACITY);
        writeBuffers[1] = ByteBuffer.allocate(CAPACITY);

        try {
            readOnlyFileChannel.write(writeBuffers);
            fail("should throw NonWritableChannelException");
        } catch (NonWritableChannelException e) {
            // expected
        }
    }

    public void test_write$LByteBuffer_EmptyBuffers() throws Exception {
        ByteBuffer[] writeBuffers = new ByteBuffer[2];
        writeBuffers[0] = ByteBuffer.allocate(this.CONTENT_LENGTH);
        try {
            writeOnlyFileChannel.write(writeBuffers);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            readWriteFileChannel.write(writeBuffers);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    public void test_write$LByteBuffer() throws Exception {
        ByteBuffer[] writeBuffers = new ByteBuffer[2];
        writeBuffers[0] = ByteBuffer.wrap(CONTENT_AS_BYTES);
        writeBuffers[1] = ByteBuffer.wrap(CONTENT_AS_BYTES);

        long result = writeOnlyFileChannel.write(writeBuffers);
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
        readOnlyFileChannel.close();
        try {
            readOnlyFileChannel.write(writeBuffers, 1, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        writeOnlyFileChannel.close();
        try {
            writeOnlyFileChannel.write(writeBuffers, 1, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        readWriteFileChannel.close();
        try {
            readWriteFileChannel.write(writeBuffers, 1, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    public void test_write$LByteBufferII_IndexOutOfBound() throws Exception {
        ByteBuffer[] writeBuffers = new ByteBuffer[2];
        writeBuffers[0] = ByteBuffer.allocate(this.CONTENT_LENGTH);
        writeBuffers[1] = ByteBuffer.allocate(this.CONTENT_LENGTH);

        try {
            writeOnlyFileChannel.write(writeBuffers, -1, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            writeOnlyFileChannel.write(writeBuffers, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            writeOnlyFileChannel.write(writeBuffers, 0, 3);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            writeOnlyFileChannel.write(writeBuffers, 1, 2);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            writeOnlyFileChannel.write(writeBuffers, 2, 1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            writeOnlyFileChannel.write(writeBuffers, 3, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            readWriteFileChannel.write(writeBuffers, -1, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            readWriteFileChannel.write(writeBuffers, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            readWriteFileChannel.write(writeBuffers, 0, 3);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            readWriteFileChannel.write(writeBuffers, 1, 2);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            readWriteFileChannel.write(writeBuffers, 2, 1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            readWriteFileChannel.write(writeBuffers, 3, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            readOnlyFileChannel.write(writeBuffers, -1, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            readOnlyFileChannel.write(writeBuffers, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            readOnlyFileChannel.write(writeBuffers, 0, 3);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            readOnlyFileChannel.write(writeBuffers, 1, 2);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            readOnlyFileChannel.write(writeBuffers, 2, 1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            readOnlyFileChannel.write(writeBuffers, 3, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    public void test_write$LByteBufferII_EmptyBuffers() throws Exception {
        ByteBuffer[] writeBuffers = new ByteBuffer[2];
        writeBuffers[0] = ByteBuffer.allocate(this.CONTENT_LENGTH);
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

    public void test_transferToJJLWritableByteChannel_IllegalArgument()
            throws Exception {
        WritableByteChannel writableByteChannel = DatagramChannel.open();
        try {
            readOnlyFileChannel.transferTo(10, -1, writableByteChannel);
            fail("should throw IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            readWriteFileChannel.transferTo(-1, 10, writableByteChannel);
            fail("should throw IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    private void assertLockFails(long position, long size) throws IOException {
        try {
            readWriteFileChannel.tryLock(position, size, false);
            fail();
        } catch (OverlappingFileLockException expected) {
        }
    }
}
