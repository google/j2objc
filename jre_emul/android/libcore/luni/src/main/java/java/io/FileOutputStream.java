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
import libcore.io.IoBridge;
import libcore.io.IoUtils;
import static libcore.io.OsConstants.*;

/**
 * An output stream that writes bytes to a file. If the output file exists, it
 * can be replaced or appended to. If it does not exist, a new file will be
 * created.
 * <pre>   {@code
 *   File file = ...
 *   OutputStream out = null;
 *   try {
 *     out = new BufferedOutputStream(new FileOutputStream(file));
 *     ...
 *   } finally {
 *     if (out != null) {
 *       out.close();
 *     }
 *   }
 * }</pre>
 *
 * <p>This stream is <strong>not buffered</strong>. Most callers should wrap
 * this stream with a {@link BufferedOutputStream}.
 *
 * <p>Use {@link FileWriter} to write characters, as opposed to bytes, to a file.
 *
 * @see BufferedOutputStream
 * @see FileInputStream
 */
public class FileOutputStream extends OutputStream {

    private FileDescriptor fd;
    private final boolean shouldClose;

    /** The unique file channel. Lazily initialized because it's rarely needed. */
    private FileChannel channel;

    /** File access mode */
    private final int mode;

    private final CloseGuard guard = CloseGuard.get();

    /**
     * Constructs a new {@code FileOutputStream} that writes to {@code file}. The file will be
     * truncated if it exists, and created if it doesn't exist.
     *
     * @throws FileNotFoundException if file cannot be opened for writing.
     */
    public FileOutputStream(File file) throws FileNotFoundException {
        this(file, false);
    }

    /**
     * Constructs a new {@code FileOutputStream} that writes to {@code file}.
     * If {@code append} is true and the file already exists, it will be appended to; otherwise
     * it will be truncated. The file will be created if it does not exist.
     *
     * @throws FileNotFoundException if the file cannot be opened for writing.
     */
    public FileOutputStream(File file, boolean append) throws FileNotFoundException {
        if (file == null) {
            throw new NullPointerException("file == null");
        }
        this.mode = O_WRONLY | O_CREAT | (append ? O_APPEND : O_TRUNC);
        this.fd = IoBridge.open(file.getAbsolutePath(), mode);
        this.shouldClose = true;
        this.guard.open("close");
    }

    /**
     * Constructs a new {@code FileOutputStream} that writes to {@code fd}.
     *
     * @throws NullPointerException if {@code fd} is null.
     */
    public FileOutputStream(FileDescriptor fd) {
        if (fd == null) {
            throw new NullPointerException("fd == null");
        }
        this.fd = fd;
        this.shouldClose = false;
        this.mode = O_WRONLY;
        this.channel = NioUtils.newFileChannelSafe(this, fd, mode);
        // Note that we do not call guard.open here because the
        // FileDescriptor is not owned by the stream.
    }

    /**
     * Constructs a new {@code FileOutputStream} that writes to {@code path}. The file will be
     * truncated if it exists, and created if it doesn't exist.
     *
     * @throws FileNotFoundException if file cannot be opened for writing.
     */
    public FileOutputStream(String path) throws FileNotFoundException {
        this(path, false);
    }

    /**
     * Constructs a new {@code FileOutputStream} that writes to {@code path}.
     * If {@code append} is true and the file already exists, it will be appended to; otherwise
     * it will be truncated. The file will be created if it does not exist.
     *
     * @throws FileNotFoundException if the file cannot be opened for writing.
     */
    public FileOutputStream(String path, boolean append) throws FileNotFoundException {
        this(new File(path), append);
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
     * Returns a write-only {@link FileChannel} that shares its position with
     * this stream.
     */
    public FileChannel getChannel() {
        synchronized (this) {
            if (channel == null) {
                channel = NioUtils.newFileChannel(this, fd, mode);
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

    @Override
    public void write(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        IoBridge.write(fd, buffer, byteOffset, byteCount);
    }

    @Override
    public void write(int oneByte) throws IOException {
        write(new byte[] { (byte) oneByte }, 0, 1);
    }
}
