/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.nio;

import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.OverlappingFileLockException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import libcore.io.ErrnoException;
import libcore.io.Libcore;
import libcore.io.StructFlock;
import libcore.util.MutableLong;
import static libcore.io.OsConstants.*;

/**
 * Our concrete implementation of the abstract FileChannel class.
 */
final class FileChannelImpl extends FileChannel {
    private static final Comparator<FileLock> LOCK_COMPARATOR = new Comparator<FileLock>() {
        public int compare(FileLock lock1, FileLock lock2) {
            long position1 = lock1.position();
            long position2 = lock2.position();
            return position1 > position2 ? 1 : (position1 < position2 ? -1 : 0);
        }
    };

    private final Object stream;
    private final FileDescriptor fd;
    private final int mode;

    // The set of acquired and pending locks.
    private final SortedSet<FileLock> locks = new TreeSet<FileLock>(LOCK_COMPARATOR);

    /**
     * Create a new file channel implementation class that wraps the given
     * fd and operates in the specified mode.
     */
    public FileChannelImpl(Object stream, FileDescriptor fd, int mode) {
        this.fd = fd;
        this.stream = stream;
        this.mode = mode;
    }

    private void checkOpen() throws ClosedChannelException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
    }

    private void checkReadable() {
        if ((mode & O_ACCMODE) == O_WRONLY) {
            throw new NonReadableChannelException();
        }
    }

    private void checkWritable() {
        if ((mode & O_ACCMODE) == O_RDONLY) {
            throw new NonWritableChannelException();
        }
    }

    protected void implCloseChannel() throws IOException {
        if (stream instanceof Closeable) {
            ((Closeable) stream).close();
        }
    }

    private FileLock basicLock(long position, long size, boolean shared, boolean wait) throws IOException {
        int accessMode = (mode & O_ACCMODE);
        if (accessMode == O_RDONLY) {
            if (!shared) {
                throw new NonWritableChannelException();
            }
        } else if (accessMode == O_WRONLY) {
            if (shared) {
                throw new NonReadableChannelException();
            }
        }

        if (position < 0 || size < 0) {
            throw new IllegalArgumentException("position=" + position + " size=" + size);
        }

        FileLock pendingLock = new FileLockImpl(this, position, size, shared);
        addLock(pendingLock);

        StructFlock flock = new StructFlock();
        flock.l_type = (short) (shared ? F_RDLCK : F_WRLCK);
        flock.l_whence = (short) SEEK_SET;
        flock.l_start = position;
        flock.l_len = translateLockLength(size);

        boolean success = false;
        try {
            success = (Libcore.os.fcntlFlock(fd, wait ? F_SETLKW64 : F_SETLK64, flock) != -1);
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsIOException();
        } finally {
            if (!success) {
                removeLock(pendingLock);
            }
        }
        return success ? pendingLock : null;
    }

    private static long translateLockLength(long byteCount) {
        // FileChannel uses Long.MAX_VALUE to mean "lock the whole file" where POSIX uses 0.
        return (byteCount == Long.MAX_VALUE) ? 0 : byteCount;
    }

    private static final class FileLockImpl extends FileLock {
        private boolean isReleased = false;

        public FileLockImpl(FileChannel channel, long position, long size, boolean shared) {
            super(channel, position, size, shared);
        }

        public boolean isValid() {
            return !isReleased && channel().isOpen();
        }

        public void release() throws IOException {
            if (!channel().isOpen()) {
                throw new ClosedChannelException();
            }
            if (!isReleased) {
                ((FileChannelImpl) channel()).release(this);
                isReleased = true;
            }
        }
    }

    public final FileLock lock(long position, long size, boolean shared) throws IOException {
        checkOpen();
        FileLock resultLock = null;
        {
            boolean completed = false;
            try {
                begin();
                resultLock = basicLock(position, size, shared, true);
                completed = true;
            } finally {
                end(completed);
            }
        }
        return resultLock;
    }

    public final FileLock tryLock(long position, long size, boolean shared) throws IOException {
        checkOpen();
        return basicLock(position, size, shared, false);
    }

    /**
     * Non-API method to release a given lock on a file channel. Assumes that
     * the lock will mark itself invalid after successful unlocking.
     */
    public void release(FileLock lock) throws IOException {
        checkOpen();

        StructFlock flock = new StructFlock();
        flock.l_type = (short) F_UNLCK;
        flock.l_whence = (short) SEEK_SET;
        flock.l_start = lock.position();
        flock.l_len = translateLockLength(lock.size());
        try {
            Libcore.os.fcntlFlock(fd, F_SETLKW64, flock);
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsIOException();
        }

        removeLock(lock);
    }

    public void force(boolean metadata) throws IOException {
        checkOpen();
        if ((mode & O_ACCMODE) != O_RDONLY) {
            try {
                if (metadata) {
                    Libcore.os.fsync(fd);
                } else {
                    Libcore.os.fdatasync(fd);
                }
            } catch (ErrnoException errnoException) {
                throw errnoException.rethrowAsIOException();
            }
        }
    }

    public final MappedByteBuffer map(MapMode mapMode, long position, long size) throws IOException {
        checkOpen();
        if (mapMode == null) {
            throw new NullPointerException("mapMode == null");
        }
        if (position < 0 || size < 0 || size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("position=" + position + " size=" + size);
        }
        int accessMode = (mode & O_ACCMODE);
        if (accessMode == O_RDONLY) {
            if (mapMode != MapMode.READ_ONLY) {
                throw new NonWritableChannelException();
            }
        } else if (accessMode == O_WRONLY) {
            throw new NonReadableChannelException();
        }
        if (position + size > size()) {
            // We can't defer to FileChannel.truncate because that will only make a file shorter,
            // and we only care about making our backing file longer here.
            try {
                Libcore.os.ftruncate(fd, position + size);
            } catch (ErrnoException ftruncateException) {
                // EINVAL can be thrown if we're dealing with non-regular
                // files, for example, character devices such as /dev/zero.
                // In those cases, we ignore the failed truncation and
                // continue on.
                try {
                    if (S_ISREG(Libcore.os.fstat(fd).st_mode) || ftruncateException.errno != EINVAL) {
                        throw ftruncateException.rethrowAsIOException();
                    }
                } catch (ErrnoException fstatException) {
                    throw fstatException.rethrowAsIOException();
                }
            }
        }
        long alignment = position - position % Libcore.os.sysconf(_SC_PAGE_SIZE);
        int offset = (int) (position - alignment);
        MemoryBlock block = MemoryBlock.mmap(fd, alignment, size + offset, mapMode);
        return new DirectByteBuffer(block, (int) size, offset, (mapMode == MapMode.READ_ONLY), mapMode);
    }

    public long position() throws IOException {
        checkOpen();
        try {
            return Libcore.os.lseek(fd, 0L, SEEK_CUR);
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsIOException();
        }
    }

    public FileChannel position(long newPosition) throws IOException {
        checkOpen();
        if (newPosition < 0) {
            throw new IllegalArgumentException("position: " + newPosition);
        }
        try {
            Libcore.os.lseek(fd, newPosition, SEEK_SET);
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsIOException();
        }
        return this;
    }

    public int read(ByteBuffer buffer, long position) throws IOException {
        if (position < 0) {
            throw new IllegalArgumentException("position: " + position);
        }
        return readImpl(buffer, position);
    }

    public int read(ByteBuffer buffer) throws IOException {
        return readImpl(buffer, -1);
    }

    private int readImpl(ByteBuffer buffer, long position) throws IOException {
        buffer.checkWritable();
        checkOpen();
        checkReadable();
        if (!buffer.hasRemaining()) {
            return 0;
        }
        int bytesRead = 0;
        boolean completed = false;
        try {
            begin();
            try {
                if (position == -1) {
                    bytesRead = Libcore.os.read(fd, buffer);
                } else {
                    bytesRead = Libcore.os.pread(fd, buffer, position);
                }
                if (bytesRead == 0) {
                    bytesRead = -1;
                }
            } catch (ErrnoException errnoException) {
                if (errnoException.errno == EAGAIN) {
                    // We don't throw if we try to read from an empty non-blocking pipe.
                    bytesRead = 0;
                } else {
                    throw errnoException.rethrowAsIOException();
                }
            }
            completed = true;
        } finally {
            end(completed && bytesRead >= 0);
        }
        if (bytesRead > 0) {
            buffer.position(buffer.position() + bytesRead);
        }
        return bytesRead;
    }

    private int transferIoVec(IoVec ioVec) throws IOException {
        if (ioVec.init() == 0) {
            return 0;
        }
        int bytesTransferred = 0;
        boolean completed = false;
        try {
            begin();
            bytesTransferred = ioVec.doTransfer(fd);
            completed = true;
        } finally {
            end(completed);
        }
        ioVec.didTransfer(bytesTransferred);
        return bytesTransferred;
    }

    public long read(ByteBuffer[] buffers, int offset, int length) throws IOException {
        Arrays.checkOffsetAndCount(buffers.length, offset, length);
        checkOpen();
        checkReadable();
        return transferIoVec(new IoVec(buffers, offset, length, IoVec.Direction.READV));
    }

    public long size() throws IOException {
        checkOpen();
        try {
            return Libcore.os.fstat(fd).st_size;
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsIOException();
        }
    }

    public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException {
        checkOpen();
        if (!src.isOpen()) {
            throw new ClosedChannelException();
        }
        checkWritable();
        if (position < 0 || count < 0 || count > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("position=" + position + " count=" + count);
        }
        if (position > size()) {
            return 0;
        }

        // Although sendfile(2) originally supported writing to a regular file.
        // In Linux 2.6 and later, it only supports writing to sockets.

        // If our source is a regular file, mmap(2) rather than reading.
        // Callers should only be using transferFrom for large transfers,
        // so the mmap(2) overhead isn't a concern.
        if (src instanceof FileChannel) {
            FileChannel fileSrc = (FileChannel) src;
            long size = fileSrc.size();
            long filePosition = fileSrc.position();
            count = Math.min(count, size - filePosition);
            ByteBuffer buffer = fileSrc.map(MapMode.READ_ONLY, filePosition, count);
            try {
                fileSrc.position(filePosition + count);
                return write(buffer, position);
            } finally {
                NioUtils.freeDirectBuffer(buffer);
            }
        }

        // For non-file channels, all we can do is read and write via userspace.
        ByteBuffer buffer = ByteBuffer.allocate((int) count);
        src.read(buffer);
        buffer.flip();
        return write(buffer, position);
    }

    public long transferTo(long position, long count, WritableByteChannel target) throws IOException {
        checkOpen();
        if (!target.isOpen()) {
            throw new ClosedChannelException();
        }
        checkReadable();
        if (target instanceof FileChannelImpl) {
            ((FileChannelImpl) target).checkWritable();
        }
        if (position < 0 || count < 0) {
            throw new IllegalArgumentException("position=" + position + " count=" + count);
        }

        if (count == 0 || position >= size()) {
            return 0;
        }
        count = Math.min(count, size() - position);

        // Try sendfile(2) first...
        boolean completed = false;
        if (target instanceof SocketChannelImpl) {
            FileDescriptor outFd = ((SocketChannelImpl) target).getFD();
            try {
                begin();
                try {
                    MutableLong offset = new MutableLong(position);
                    long rc = Libcore.os.sendfile(outFd, fd, offset, count);
                    completed = true;
                    return rc;
                } catch (ErrnoException errnoException) {
                    // If the OS doesn't support what we asked for, we want to fall through and
                    // try a different approach. If it does support it, but it failed, we're done.
                    if (errnoException.errno != ENOSYS && errnoException.errno != EINVAL) {
                        throw errnoException.rethrowAsIOException();
                    }
                }
            } finally {
                end(completed);
            }
        }
        // ...fall back to write(2).
        ByteBuffer buffer = null;
        try {
            buffer = map(MapMode.READ_ONLY, position, count);
            return target.write(buffer);
        } finally {
            NioUtils.freeDirectBuffer(buffer);
        }
    }

    public FileChannel truncate(long size) throws IOException {
        checkOpen();
        if (size < 0) {
            throw new IllegalArgumentException("size < 0: " + size);
        }
        checkWritable();
        if (size < size()) {
            try {
                Libcore.os.ftruncate(fd, size);
            } catch (ErrnoException errnoException) {
                throw errnoException.rethrowAsIOException();
            }
        }
        if (position() > size) {
            position(size);
        }
        return this;
    }

    public int write(ByteBuffer buffer, long position) throws IOException {
        if (position < 0) {
            throw new IllegalArgumentException("position < 0: " + position);
        }
        return writeImpl(buffer, position);
    }

    public int write(ByteBuffer buffer) throws IOException {
        return writeImpl(buffer, -1);
    }

    private int writeImpl(ByteBuffer buffer, long position) throws IOException {
        checkOpen();
        checkWritable();
        if (buffer == null) {
            throw new NullPointerException("buffer == null");
        }
        if (!buffer.hasRemaining()) {
            return 0;
        }
        int bytesWritten = 0;
        boolean completed = false;
        try {
            begin();
            try {
                if (position == -1) {
                    bytesWritten = Libcore.os.write(fd, buffer);
                } else {
                    bytesWritten = Libcore.os.pwrite(fd, buffer, position);
                }
            } catch (ErrnoException errnoException) {
                throw errnoException.rethrowAsIOException();
            }
            completed = true;
        } finally {
            end(completed);
        }
        if (bytesWritten > 0) {
            buffer.position(buffer.position() + bytesWritten);
        }
        return bytesWritten;
    }

    public long write(ByteBuffer[] buffers, int offset, int length) throws IOException {
        Arrays.checkOffsetAndCount(buffers.length, offset, length);
        checkOpen();
        checkWritable();
        return transferIoVec(new IoVec(buffers, offset, length, IoVec.Direction.WRITEV));
    }

    /**
     * @param copyingIn true if we're copying data into the buffers (typically
     * because the caller is a file/network read operation), false if we're
     * copying data out of the buffers (for a file/network write operation).
     */
    static int calculateTotalRemaining(ByteBuffer[] buffers, int offset, int length, boolean copyingIn) {
        int count = 0;
        for (int i = offset; i < offset + length; ++i) {
            count += buffers[i].remaining();
            if (copyingIn) {
                buffers[i].checkWritable();
            }
        }
        return count;
    }

    public FileDescriptor getFD() {
        return fd;
    }

    /**
     * Add a new pending lock to the manager. Throws an exception if the lock
     * would overlap an existing lock. Once the lock is acquired it remains in
     * this set as an acquired lock.
     */
    private synchronized void addLock(FileLock lock) throws OverlappingFileLockException {
        long lockEnd = lock.position() + lock.size();
        for (FileLock existingLock : locks) {
            if (existingLock.position() > lockEnd) {
                // This, and all remaining locks, start beyond our end (so
                // cannot overlap).
                break;
            }
            if (existingLock.overlaps(lock.position(), lock.size())) {
                throw new OverlappingFileLockException();
            }
        }
        locks.add(lock);
    }

    /**
     * Removes an acquired lock from the lock manager. If the lock did not exist
     * in the lock manager the operation is a no-op.
     */
    private synchronized void removeLock(FileLock lock) {
        locks.remove(lock);
    }
}
