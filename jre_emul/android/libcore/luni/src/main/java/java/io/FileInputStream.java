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

package java.io;

import dalvik.system.CloseGuard;

import java.nio.NioUtils;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import libcore.io.ErrnoException;
import libcore.io.IoBridge;
import libcore.io.IoUtils;
import libcore.io.Libcore;
import libcore.io.Streams;
import static libcore.io.OsConstants.*;

/**
 * An input stream that reads bytes from a file.
 * <pre>   {@code
 *   File file = ...
 *   InputStream in = null;
 *   try {
 *     in = new BufferedInputStream(new FileInputStream(file));
 *     ...
 *   } finally {
 *     if (in != null) {
 *       in.close();
 *     }
 *   }
 * }</pre>
 *
 * <p>This stream is <strong>not buffered</strong>. Most callers should wrap
 * this stream with a {@link BufferedInputStream}.
 *
 * <p>Use {@link FileReader} to read characters, as opposed to bytes, from a
 * file.
 *
 * @see BufferedInputStream
 * @see FileOutputStream
 */
public class FileInputStream extends InputStream {

    private FileDescriptor fd;
    private final boolean shouldClose;

    /** The unique file channel. Lazily initialized because it's rarely needed. */
    private FileChannel channel;

    private final CloseGuard guard = CloseGuard.get();

    /**
     * Constructs a new {@code FileInputStream} that reads from {@code file}.
     *
     * @param file
     *            the file from which this stream reads.
     * @throws FileNotFoundException
     *             if {@code file} does not exist.
     */
    public FileInputStream(File file) throws FileNotFoundException {
        if (file == null) {
            throw new NullPointerException("file == null");
        }
        this.fd = IoBridge.open(file.getAbsolutePath(), O_RDONLY);
        this.shouldClose = true;
        guard.open("close");
    }

    /**
     * Constructs a new {@code FileInputStream} that reads from {@code fd}.
     *
     * @param fd
     *            the FileDescriptor from which this stream reads.
     * @throws NullPointerException
     *             if {@code fd} is {@code null}.
     */
    public FileInputStream(FileDescriptor fd) {
        if (fd == null) {
            throw new NullPointerException("fd == null");
        }
        this.fd = fd;
        this.shouldClose = false;
        // Note that we do not call guard.open here because the
        // FileDescriptor is not owned by the stream.
    }

    /**
     * Equivalent to {@code new FileInputStream(new File(path))}.
     */
    public FileInputStream(String path) throws FileNotFoundException {
        this(new File(path));
    }

    @Override
    public int available() throws IOException {
        return IoBridge.available(fd);
    }

    @Override
    public void close() throws IOException {
        guard.close();
        synchronized (this) {
            if (channel != null) {
                channel.close();
            }
            if (shouldClose) {
                IoUtils.close(fd);
            } else {
                // An owned fd has been invalidated by IoUtils.close, but
                // we need to explicitly stop using an unowned fd (http://b/4361076).
                fd = new FileDescriptor();
            }
        }
    }

    /**
     * Ensures that all resources for this stream are released when it is about
     * to be garbage collected.
     *
     * @throws IOException
     *             if an error occurs attempting to finalize this stream.
     */
    @Override protected void finalize() throws IOException {
        try {
            if (guard != null) {
                guard.warnIfOpen();
            }
            close();
        } finally {
            try {
                super.finalize();
            } catch (Throwable t) {
                // for consistency with the RI, we must override Object.finalize() to
                // remove the 'throws Throwable' clause.
                throw new AssertionError(t);
            }
        }
    }

    /**
     * Returns a read-only {@link FileChannel} that shares its position with
     * this stream.
     */
    public FileChannel getChannel() {
        synchronized (this) {
            if (channel == null) {
                channel = NioUtils.newFileChannel(this, fd, O_RDONLY);
            }
            return channel;
        }
    }

    /**
     * Returns the underlying file descriptor.
     */
    public final FileDescriptor getFD() throws IOException {
        return fd;
    }

    @Override public int read() throws IOException {
        return Streams.readSingleByte(this);
    }

    @Override public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        return IoBridge.read(fd, buffer, byteOffset, byteCount);
    }

    @Override
    public long skip(long byteCount) throws IOException {
        if (byteCount < 0) {
            throw new IOException("byteCount < 0: " + byteCount);
        }
        try {
            // Try lseek(2). That returns the new offset, but we'll throw an
            // exception if it couldn't perform exactly the seek we asked for.
            Libcore.os.lseek(fd, byteCount, SEEK_CUR);
            return byteCount;
        } catch (ErrnoException errnoException) {
            if (errnoException.errno == ESPIPE) {
                // You can't seek on a pipe, so fall back to the superclass' implementation.
                return super.skip(byteCount);
            }
            throw errnoException.rethrowAsIOException();
        }
    }
}
