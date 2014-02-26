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

package java.nio.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.spi.AbstractInterruptibleChannel;

/**
 * An abstract channel type for interaction with a platform file.
 * <p>
 * A {@code FileChannel} defines the methods for reading, writing, memory
 * mapping, and manipulating the logical state of a platform file. This type
 * does not have a method for opening files, since this behavior has been
 * delegated to the {@link java.io.FileInputStream},
 * {@link java.io.FileOutputStream} and {@link java.io.RandomAccessFile} types.
 * <p>
 * FileChannels created from a {@code FileInputStream} or a
 * {@code RandomAccessFile} created in mode "r", are read-only. FileChannels
 * created from a {@code FileOutputStream} are write-only. FileChannels created
 * from a {@code RandomAccessFile} created in mode "rw" are read/write.
 * FileChannels created from a {@code RandomAccessFile} that was opened in
 * append-mode will also be in append-mode -- meaning that each write will be
 * proceeded by a seek to the end of file.
 * <p>
 * FileChannels have a virtual pointer into the file which is referred to as a
 * file <em>position</em>. The position can be manipulated by moving it
 * within the file, and the current position can be queried.
 * <p>
 * FileChannels also have an associated <em>size</em>. The size of the file
 * is the number of bytes that it currently contains. The size can be
 * manipulated by adding more bytes to the end of the file (which increases the
 * size) or truncating the file (which decreases the size). The current size can
 * also be queried.
 * <p>
 * FileChannels have operations beyond the simple read, write, and close. They
 * can also:
 * <ul>
 * <li>request that cached data be forced onto the disk,</li>
 * <li>lock ranges of bytes associated with the file,</li>
 * <li>transfer data directly to another channel in a manner that has the
 * potential to be optimized by the platform,</li>
 * <li>memory-mapping files into NIO buffers to provide efficient manipulation
 * of file data,</li>
 * <li>read and write to the file at absolute byte offsets in a fashion that
 * does not modify the current position.</li>
 * </ul>
 * <p>
 * FileChannels are thread-safe. Only one operation involving manipulation of
 * the file position may be executed at the same time. Subsequent calls to such
 * operations will block, and one of those blocked will be freed to continue
 * when the first operation has completed. There is no ordered queue or fairness
 * applied to the blocked threads.
 * <p>
 * It is undefined whether operations that do not manipulate the file position
 * will also block when there are any other operations in-flight.
 * <p>
 * The logical view of the underlying file is consistent across all FileChannels
 * and I/O streams opened on the same file by the same VM.
 * Therefore, modifications performed via a channel will be visible to the
 * stream and vice versa; this includes modifications to the file position,
 * content, size, etc.
 */
public abstract class FileChannel extends AbstractInterruptibleChannel
        implements GatheringByteChannel, ScatteringByteChannel, ByteChannel {

    /**
     * {@code MapMode} defines file mapping mode constants.
     */
    public static class MapMode {
        /**
         * Private mapping mode (equivalent to copy on write).
         */
        public static final MapMode PRIVATE = new MapMode("PRIVATE");

        /**
         * Read-only mapping mode.
         */
        public static final MapMode READ_ONLY = new MapMode("READ_ONLY");

        /**
         * Read-write mapping mode.
         */
        public static final MapMode READ_WRITE = new MapMode("READ_WRITE");

        // The string used to display the mapping mode.
        private final String displayName;

        /*
         * Private constructor prevents others creating new modes.
         */
        private MapMode(String displayName) {
            this.displayName = displayName;
        }

        /**
         * Returns a string version of the mapping mode.
         *
         * @return this map mode as string.
         */
        @Override
        public String toString() {
            return displayName;
        }
    }

    /**
     * Protected default constructor.
     */
    protected FileChannel() {
    }

    /**
     * Requests that all updates to this channel are committed to the storage
     * device.
     * <p>
     * When this method returns, all modifications made to the platform file
     * underlying this channel have been committed if the file resides on a
     * local storage device. If the file is not hosted locally, for example on a
     * networked file system, then applications cannot be certain that the
     * modifications have been committed.
     * <p>
     * There are no assurances given that changes made to the file using methods
     * defined elsewhere will be committed. For example, changes made via a
     * mapped byte buffer may not be committed.
     * <p>
     * The <code>metadata</code> parameter indicates whether the update should
     * include the file's metadata such as last modification time, last access
     * time, etc. Note that passing <code>true</code> may invoke an underlying
     * write to the operating system (if the platform is maintaining metadata
     * such as last access time), even if the channel is opened read-only.
     *
     * @param metadata
     *            {@code true} if the file metadata should be flushed in
     *            addition to the file content, {@code false} otherwise.
     * @throws ClosedChannelException
     *             if this channel is already closed.
     * @throws IOException
     *             if another I/O error occurs.
     */
    public abstract void force(boolean metadata) throws IOException;

    /**
     * Obtains an exclusive lock on this file.
     * <p>
     * This is a convenience method for acquiring a maximum length lock on a
     * file. It is equivalent to:
     * {@code fileChannel.lock(0L, Long.MAX_VALUE, false);}
     *
     * @return the lock object representing the locked file area.
     * @throws ClosedChannelException
     *             the file channel is closed.
     * @throws NonWritableChannelException
     *             this channel was not opened for writing.
     * @throws OverlappingFileLockException
     *             either a lock is already held that overlaps this lock
     *             request, or another thread is waiting to acquire a lock that
     *             will overlap with this request.
     * @throws FileLockInterruptionException
     *             the calling thread was interrupted while waiting to acquire
     *             the lock.
     * @throws AsynchronousCloseException
     *             the channel was closed while the calling thread was waiting
     *             to acquire the lock.
     * @throws IOException
     *             if another I/O error occurs while obtaining the requested
     *             lock.
     */
    public final FileLock lock() throws IOException {
        return lock(0L, Long.MAX_VALUE, false);
    }

    /**
     * Obtains a lock on a specified region of the file.
     * <p>
     * This is the blocking version of lock acquisition, see also the
     * <code>tryLock()</code> methods.
     * <p>
     * Attempts to acquire an overlapping lock region will fail. The attempt
     * will fail if the overlapping lock has already been obtained, or if
     * another thread is currently waiting to acquire the overlapping lock.
     * <p>
     * If the request is not for an overlapping lock, the thread calling this
     * method will block until the lock is obtained (likely by no contention or
     * another process releasing a lock), or until this thread is interrupted or
     * the channel is closed.
     * <p>
     * If the lock is obtained successfully then the {@link FileLock} object
     * returned represents the lock for subsequent operations on the locked
     * region.
     * <p>
     * If the thread is interrupted while waiting for the lock, the thread is
     * set to the interrupted state and throws a
     * {@link FileLockInterruptionException}. If this channel is closed while
     * the thread is waiting to obtain the lock then the thread throws a
     * {@link AsynchronousCloseException}.
     * <p>
     * There is no requirement for the position and size to be within the
     * current start and length of the file.
     * <p>
     * Some platforms do not support shared locks, and if a request is made for
     * a shared lock on such a platform, this method will attempt to acquire an
     * exclusive lock instead. It is undefined whether the lock obtained is
     * advisory or mandatory.
     *
     * @param position
     *            the starting position for the locked region.
     * @param size
     *            the length of the locked region in bytes.
     * @param shared
     *            a flag indicating whether an attempt should be made to acquire
     *            a shared lock.
     * @return the file lock object.
     * @throws IllegalArgumentException
     *             if {@code position} or {@code size} is negative.
     * @throws ClosedChannelException
     *             if this channel is closed.
     * @throws OverlappingFileLockException
     *             if the requested region overlaps an existing lock or pending
     *             lock request.
     * @throws NonReadableChannelException
     *             if the channel is not opened in read-mode but shared is true.
     * @throws NonWritableChannelException
     *             if the channel is not opened in write mode but shared is
     *             false.
     * @throws AsynchronousCloseException
     *             if this channel is closed by another thread while this method
     *             is executing.
     * @throws FileLockInterruptionException
     *             if the thread is interrupted while in the state of waiting on
     *             the desired file lock.
     * @throws IOException
     *             if another I/O error occurs.
     */
    public abstract FileLock lock(long position, long size, boolean shared)
            throws IOException;

    /**
     * Maps the file into memory. There can be three modes: read-only,
     * read/write and private. After mapping, changes made to memory or the file
     * channel do not affect the other storage place.
     * <p>
     * Note: mapping a file into memory is usually expensive.
     *
     * @param mode
     *            one of the three mapping modes.
     * @param position
     *            the starting position of the file.
     * @param size
     *            the size of the region to map into memory.
     * @return the mapped byte buffer.
     * @throws NonReadableChannelException
     *             if the FileChannel is not opened for reading but the given
     *             mode is "READ_ONLY".
     * @throws NonWritableChannelException
     *             if the FileChannel is not opened for writing but the given
     *             mode is not "READ_ONLY".
     * @throws IllegalArgumentException
     *             if the given parameters of position and size are not correct.
     *             Both must be non negative. {@code size} also must not be
     *             bigger than max integer.
     * @throws IOException
     *             if any I/O error occurs.
     */
    public abstract MappedByteBuffer map(FileChannel.MapMode mode,
            long position, long size) throws IOException;

    /**
     * Returns the current value of the file position pointer.
     *
     * @return the current position as a positive integer number of bytes from
     *         the start of the file.
     * @throws ClosedChannelException
     *             if this channel is closed.
     * @throws IOException
     *             if another I/O error occurs.
     */
    public abstract long position() throws IOException;

    /**
     * Sets the file position pointer to a new value.
     * <p>
     * The argument is the number of bytes counted from the start of the file.
     * The position cannot be set to a value that is negative. The new position
     * can be set beyond the current file size. If set beyond the current file
     * size, attempts to read will return end of file. Write operations will
     * succeed but they will fill the bytes between the current end of file and
     * the new position with the required number of (unspecified) byte values.
     *
     * @param offset
     *            the new file position, in bytes.
     * @return the receiver.
     * @throws IllegalArgumentException
     *             if the new position is negative.
     * @throws ClosedChannelException
     *             if this channel is closed.
     * @throws IOException
     *             if another I/O error occurs.
     */
    public abstract FileChannel position(long offset) throws IOException;

    /**
     * Reads bytes from this file channel into the given buffer.
     * <p>
     * The maximum number of bytes that will be read is the remaining number of
     * bytes in the buffer when the method is invoked. The bytes will be copied
     * into the buffer starting at the buffer's current position.
     * <p>
     * The call may block if other threads are also attempting to read from this
     * channel.
     * <p>
     * Upon completion, the buffer's position is set to the end of the bytes
     * that have been read. The buffer's limit is not changed.
     *
     * @param buffer
     *            the byte buffer to receive the bytes.
     * @return the number of bytes actually read.
     * @throws AsynchronousCloseException
     *             if another thread closes the channel during the read.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread during the
     *             read.
     * @throws ClosedChannelException
     *             if this channel is closed.
     * @throws IOException
     *             if another I/O error occurs, details are in the message.
     * @throws NonReadableChannelException
     *             if the channel has not been opened in a mode that permits
     *             reading.
     */
    public abstract int read(ByteBuffer buffer) throws IOException;

    /**
     * Reads bytes from this file channel into the given buffer starting from
     * the specified file position.
     * <p>
     * The bytes are read starting at the given file position (up to the
     * remaining number of bytes in the buffer). The number of bytes actually
     * read is returned.
     * <p>
     * If {@code position} is beyond the current end of file, then no bytes are
     * read.
     * <p>
     * Note that the file position is unmodified by this method.
     *
     * @param buffer
     *            the buffer to receive the bytes.
     * @param position
     *            the (non-negative) position at which to read the bytes.
     * @return the number of bytes actually read.
     * @throws AsynchronousCloseException
     *             if this channel is closed by another thread while this method
     *             is executing.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while this
     *             operation is in progress. The calling thread will have the
     *             interrupt state set, and the channel will be closed.
     * @throws ClosedChannelException
     *             if this channel is closed.
     * @throws IllegalArgumentException
     *             if <code>position</code> is less than 0.
     * @throws IOException
     *             if another I/O error occurs.
     * @throws NonReadableChannelException
     *             if the channel has not been opened in a mode that permits
     *             reading.
     */
    public abstract int read(ByteBuffer buffer, long position)
            throws IOException;

    /**
     * Reads bytes from this file channel and stores them in the specified array
     * of buffers. This method attempts to read as many bytes as can be stored
     * in the buffer array from this channel and returns the number of bytes
     * actually read. It also increases the file position by the number of bytes
     * read.
     * <p>
     * If a read operation is in progress, subsequent threads will block until
     * the read is completed and will then contend for the ability to read.
     * <p>
     * Calling this method is equivalent to calling
     * {@code read(buffers, 0, buffers.length);}
     *
     * @param buffers
     *            the array of byte buffers into which the bytes will be copied.
     * @return the number of bytes actually read.
     * @throws AsynchronousCloseException
     *             if this channel is closed by another thread during this read
     *             operation.
     * @throws ClosedByInterruptException
     *             if the thread is interrupted by another thread during this
     *             read operation.
     * @throws ClosedChannelException
     *             if this channel is closed.
     * @throws IOException
     *             if another I/O error occurs; details are in the message.
     * @throws NonReadableChannelException
     *             if the channel has not been opened in a mode that permits
     *             reading.
     */
    public final long read(ByteBuffer[] buffers) throws IOException {
        return read(buffers, 0, buffers.length);
    }

    /**
     * Reads bytes from this file channel into a subset of the given buffers.
     * This method attempts to read all {@code remaining()} bytes from {@code
     * length} byte buffers, in order, starting at {@code targets[offset]}. It
     * increases the file position by the number of bytes actually read. The
     * number of bytes actually read is returned.
     * <p>
     * If a read operation is in progress, subsequent threads will block until
     * the read is completed and will then contend for the ability to read.
     *
     * @param buffers
     *            the array of byte buffers into which the bytes will be copied.
     * @param start
     *            the index of the first buffer to store bytes in.
     * @param number
     *            the maximum number of buffers to store bytes in.
     * @return the number of bytes actually read.
     * @throws AsynchronousCloseException
     *             if this channel is closed by another thread during this read
     *             operation.
     * @throws ClosedByInterruptException
     *             if the thread is interrupted by another thread during this
     *             read operation.
     * @throws ClosedChannelException
     *             if this channel is closed.
     * @throws IndexOutOfBoundsException
     *             if {@code start < 0} or {@code number < 0}, or if
     *             {@code start + number} is greater than the size of
     *             {@code buffers}.
     * @throws IOException
     *             if another I/O error occurs; details are in the message.
     * @throws NonReadableChannelException
     *             if the channel has not been opened in a mode that permits
     *             reading.
     */
    public abstract long read(ByteBuffer[] buffers, int start, int number)
            throws IOException;

    /**
     * Returns the size of the file underlying this channel in bytes.
     *
     * @return the size of the file in bytes.
     * @throws ClosedChannelException
     *             if this channel is closed.
     * @throws IOException
     *             if an I/O error occurs while getting the size of the file.
     */
    public abstract long size() throws IOException;

    /**
     * Reads up to {@code count} bytes from {@code src} and stores them in this
     * channel's file starting at {@code position}. No bytes are transferred if
     * {@code position} is larger than the size of this channel's file. Less
     * than {@code count} bytes are transferred if there are less bytes
     * remaining in the source channel or if the source channel is non-blocking
     * and has less than {@code count} bytes immediately available in its output
     * buffer.
     * <p>
     * Note that this channel's position is not modified.
     *
     * @param src
     *            the source channel to read bytes from.
     * @param position
     *            the non-negative start position.
     * @param count
     *            the non-negative number of bytes to transfer.
     * @return the number of bytes that are transferred.
     * @throws IllegalArgumentException
     *             if the parameters are invalid.
     * @throws NonReadableChannelException
     *             if the source channel is not readable.
     * @throws NonWritableChannelException
     *             if this channel is not writable.
     * @throws ClosedChannelException
     *             if either channel has already been closed.
     * @throws AsynchronousCloseException
     *             if either channel is closed by other threads during this
     *             operation.
     * @throws ClosedByInterruptException
     *             if the thread is interrupted during this operation.
     * @throws IOException
     *             if any I/O error occurs.
     */
    public abstract long transferFrom(ReadableByteChannel src, long position,
            long count) throws IOException;

    /**
     * Reads up to {@code count} bytes from this channel's file starting at
     * {@code position} and writes them to {@code target}. No bytes are
     * transferred if {@code position} is larger than the size of this channel's
     * file. Less than {@code count} bytes are transferred if there less bytes
     * available from this channel's file or if the target channel is
     * non-blocking and has less than {@code count} bytes free in its input
     * buffer.
     * <p>
     * Note that this channel's position is not modified.
     *
     * @param position
     *            the non-negative position to begin.
     * @param count
     *            the non-negative number of bytes to transfer.
     * @param target
     *            the target channel to write to.
     * @return the number of bytes that were transferred.
     * @throws IllegalArgumentException
     *             if the parameters are invalid.
     * @throws NonReadableChannelException
     *             if this channel is not readable.
     * @throws NonWritableChannelException
     *             if the target channel is not writable.
     * @throws ClosedChannelException
     *             if either channel has already been closed.
     * @throws AsynchronousCloseException
     *             if either channel is closed by other threads during this
     *             operation.
     * @throws ClosedByInterruptException
     *             if the thread is interrupted during this operation.
     * @throws IOException
     *             if any I/O error occurs.
     */
    public abstract long transferTo(long position, long count,
            WritableByteChannel target) throws IOException;

    /**
     * Truncates the file underlying this channel to a given size. Any bytes
     * beyond the given size are removed from the file. If there are no bytes
     * beyond the given size then the file contents are unmodified.
     * <p>
     * If the file position is currently greater than the given size, then it is
     * set to the new size.
     *
     * @param size
     *            the maximum size of the underlying file.
     * @throws IllegalArgumentException
     *             if the requested size is negative.
     * @throws ClosedChannelException
     *             if this channel is closed.
     * @throws NonWritableChannelException
     *             if the channel cannot be written to.
     * @throws IOException
     *             if another I/O error occurs.
     * @return this channel.
     */
    public abstract FileChannel truncate(long size) throws IOException;

    /**
     * Attempts to acquire an exclusive lock on this file without blocking.
     * <p>
     * This is a convenience method for attempting to acquire a maximum length
     * lock on the file. It is equivalent to:
     * {@code fileChannel.tryLock(0L, Long.MAX_VALUE, false);}
     * <p>
     * The method returns {@code null} if the acquisition would result in an
     * overlapped lock with another OS process.
     *
     * @return the file lock object, or {@code null} if the lock would overlap
     *         with an existing exclusive lock in another OS process.
     * @throws ClosedChannelException
     *             if the file channel is closed.
     * @throws OverlappingFileLockException
     *             if a lock already exists that overlaps this lock request or
     *             another thread is waiting to acquire a lock that will overlap
     *             with this request.
     * @throws IOException
     *             if any I/O error occurs.
     */
    public final FileLock tryLock() throws IOException {
        return tryLock(0L, Long.MAX_VALUE, false);
    }

    /**
     * Attempts to acquire an exclusive lock on this file without blocking. The
     * method returns {@code null} if the acquisition would result in an
     * overlapped lock with another OS process.
     * <p>
     * It is possible to acquire a lock for any region even if it's completely
     * outside of the file's size. The size of the lock is fixed. If the file
     * grows outside of the lock that region of the file won't be locked by this
     * lock.
     *
     * @param position
     *            the starting position.
     * @param size
     *            the size of file to lock.
     * @param shared
     *            true if the lock is shared.
     * @return the file lock object, or {@code null} if the lock would overlap
     *         with an existing exclusive lock in another OS process.
     * @throws IllegalArgumentException
     *             if any parameters are invalid.
     * @throws ClosedChannelException
     *             if the file channel is closed.
     * @throws OverlappingFileLockException
     *             if a lock is already held that overlaps this lock request or
     *             another thread is waiting to acquire a lock that will overlap
     *             with this request.
     * @throws IOException
     *             if any I/O error occurs.
     */
    public abstract FileLock tryLock(long position, long size, boolean shared)
            throws IOException;

    /**
     * Writes bytes from the given byte buffer to this file channel.
     * <p>
     * The bytes are written starting at the current file position, and after
     * some number of bytes are written (up to the remaining number of bytes in
     * the buffer) the file position is increased by the number of bytes
     * actually written.
     *
     * @param src
     *            the byte buffer containing the bytes to be written.
     * @return the number of bytes actually written.
     * @throws NonWritableChannelException
     *             if the channel was not opened for writing.
     * @throws ClosedChannelException
     *             if the channel was already closed.
     * @throws AsynchronousCloseException
     *             if another thread closes the channel during the write.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while this
     *             operation is in progress. The interrupt state of the calling
     *             thread is set and the channel is closed.
     * @throws IOException
     *             if another I/O error occurs, details are in the message.
     * @see java.nio.channels.WritableByteChannel#write(java.nio.ByteBuffer)
     */
    public abstract int write(ByteBuffer src) throws IOException;

    /**
     * Writes bytes from the given buffer to this file channel starting at the
     * given file position.
     * <p>
     * The bytes are written starting at the given file position (up to the
     * remaining number of bytes in the buffer). The number of bytes actually
     * written is returned.
     * <p>
     * If the position is beyond the current end of file, then the file is first
     * extended up to the given position by the required number of unspecified
     * byte values.
     * <p>
     * Note that the file position is not modified by this method.
     *
     * @param buffer
     *            the buffer containing the bytes to be written.
     * @param position
     *            the (non-negative) position at which to write the bytes.
     * @return the number of bytes actually written.
     * @throws IllegalArgumentException
     *             if <code>position</code> is less than 0.
     * @throws ClosedChannelException
     *             if this channel is closed.
     * @throws NonWritableChannelException
     *             if the channel was not opened in write-mode.
     * @throws AsynchronousCloseException
     *             if this channel is closed by another thread while this method
     *             is executing.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while this
     *             operation is in progress. The interrupt state of the calling
     *             thread is set and the channel is closed.
     * @throws IOException
     *             if another I/O error occurs.
     */
    public abstract int write(ByteBuffer buffer, long position)
            throws IOException;

    /**
     * Writes bytes from all the given byte buffers to this file channel.
     * <p>
     * The bytes are written starting at the current file position, and after
     * the bytes are written (up to the remaining number of bytes in all the
     * buffers), the file position is increased by the number of bytes actually
     * written.
     * <p>
     * Calling this method is equivalent to calling
     * {@code write(buffers, 0, buffers.length);}
     *
     * @param buffers
     *            the buffers containing bytes to write.
     * @return the number of bytes actually written.
     * @throws AsynchronousCloseException
     *             if this channel is closed by another thread during this write
     *             operation.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while this
     *             operation is in progress. The interrupt state of the calling
     *             thread is set and the channel is closed.
     * @throws ClosedChannelException
     *             if this channel is closed.
     * @throws IOException
     *             if another I/O error occurs; details are in the message.
     * @throws NonWritableChannelException
     *             if this channel was not opened for writing.
     */
    public final long write(ByteBuffer[] buffers) throws IOException {
        return write(buffers, 0, buffers.length);
    }

    /**
     * Attempts to write a subset of the given bytes from the buffers to this
     * file channel. This method attempts to write all {@code remaining()}
     * bytes from {@code length} byte buffers, in order, starting at {@code
     * sources[offset]}. The number of bytes actually written is returned.
     * <p>
     * If a write operation is in progress, subsequent threads will block until
     * the write is completed and then contend for the ability to write.
     *
     * @param buffers
     *            the array of byte buffers that is the source for bytes written
     *            to this channel.
     * @param offset
     *            the index of the first buffer in {@code buffers }to get bytes
     *            from.
     * @param length
     *            the number of buffers to get bytes from.
     * @return the number of bytes actually written to this channel.
     * @throws AsynchronousCloseException
     *             if this channel is closed by another thread during this write
     *             operation.
     * @throws ClosedByInterruptException
     *             if another thread interrupts the calling thread while this
     *             operation is in progress. The interrupt state of the calling
     *             thread is set and the channel is closed.
     * @throws ClosedChannelException
     *             if this channel is closed.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code length < 0}, or if
     *             {@code offset + length} is greater than the size of
     *             {@code buffers}.
     * @throws IOException
     *             if another I/O error occurs; details are in the message.
     * @throws NonWritableChannelException
     *             if this channel was not opened for writing.
     */
    public abstract long write(ByteBuffer[] buffers, int offset, int length)
            throws IOException;
}
