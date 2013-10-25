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

import java.util.Arrays;
import libcore.io.IoUtils;

/**
 * Receives information from a communications pipe. When two threads want to
 * pass data back and forth, one creates a piped output stream and the other one
 * creates a piped input stream.
 *
 * @see PipedOutputStream
 */
public class PipedInputStream extends InputStream {

    private Thread lastReader;

    private Thread lastWriter;

    private boolean isClosed;

    /**
     * The circular buffer through which data is passed. Data is read from the
     * range {@code [out, in)} and written to the range {@code [in, out)}.
     * Data in the buffer is either sequential: <pre>
     *     { - - - X X X X X X X - - - - - }
     *             ^             ^
     *             |             |
     *            out           in</pre>
     * ...or wrapped around the buffer's end: <pre>
     *     { X X X X - - - - - - - - X X X }
     *               ^               ^
     *               |               |
     *              in              out</pre>
     * When the buffer is empty, {@code in == -1}. Reading when the buffer is
     * empty will block until data is available. When the buffer is full,
     * {@code in == out}. Writing when the buffer is full will block until free
     * space is available.
     */
    protected byte[] buffer;

    /**
     * The index in {@code buffer} where the next byte will be written.
     */
    protected int in = -1;

    /**
     * The index in {@code buffer} where the next byte will be read.
     */
    protected int out;

    /**
     * The size of the default pipe in bytes.
     */
    protected static final int PIPE_SIZE = 1024;

    /**
     * Indicates if this pipe is connected.
     */
    boolean isConnected;

    /**
     * Constructs a new unconnected {@code PipedInputStream}. The resulting
     * stream must be connected to a {@link PipedOutputStream} before data may
     * be read from it.
     */
    public PipedInputStream() {}

    /**
     * Constructs a new {@code PipedInputStream} connected to the
     * {@link PipedOutputStream} {@code out}. Any data written to the output
     * stream can be read from the this input stream.
     *
     * @param out
     *            the piped output stream to connect to.
     * @throws IOException
     *             if this stream or {@code out} are already connected.
     */
    public PipedInputStream(PipedOutputStream out) throws IOException {
        connect(out);
    }

    /**
     * Constructs a new unconnected {@code PipedInputStream} with the given
     * buffer size. The resulting stream must be connected to a
     * {@code PipedOutputStream} before data may be read from it.
     *
     * @param pipeSize the size of the buffer in bytes.
     * @throws IllegalArgumentException if pipeSize is less than or equal to zero.
     * @since 1.6
     */
    public PipedInputStream(int pipeSize) {
        if (pipeSize <= 0) {
            throw new IllegalArgumentException("pipe size " + pipeSize + " too small");
        }
        buffer = new byte[pipeSize];
    }

    /**
     * Constructs a new {@code PipedInputStream} connected to the given {@code PipedOutputStream},
     * with the given buffer size. Any data written to the output stream can be read from this
     * input stream.
     *
     * @param out the {@code PipedOutputStream} to connect to.
     * @param pipeSize the size of the buffer in bytes.
     * @throws IOException if an I/O error occurs.
     * @throws IllegalArgumentException if pipeSize is less than or equal to zero.
     * @since 1.6
     */
    public PipedInputStream(PipedOutputStream out, int pipeSize) throws IOException {
        this(pipeSize);
        connect(out);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Unlike most streams, {@code PipedInputStream} returns 0 rather than throwing
     * {@code IOException} if the stream has been closed. Unconnected and broken pipes also
     * return 0.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public synchronized int available() throws IOException {
        if (buffer == null || in == -1) {
            return 0;
        }
        return in <= out ? buffer.length - out + in : in - out;
    }

    /**
     * Closes this stream. This implementation releases the buffer used for the
     * pipe and notifies all threads waiting to read or write.
     *
     * @throws IOException
     *             if an error occurs while closing this stream.
     */
    @Override
    public synchronized void close() throws IOException {
        buffer = null;
        notifyAll();
    }

    /**
     * Connects this {@code PipedInputStream} to a {@link PipedOutputStream}.
     * Any data written to the output stream becomes readable in this input
     * stream.
     *
     * @param src
     *            the source output stream.
     * @throws IOException
     *             if either stream is already connected.
     */
    public void connect(PipedOutputStream src) throws IOException {
        src.connect(this);
    }

    /**
     * Establishes the connection to the PipedOutputStream.
     *
     * @throws IOException
     *             If this Reader is already connected.
     */
    synchronized void establishConnection() throws IOException {
        if (isConnected) {
            throw new IOException("Pipe already connected");
        }
        if (buffer == null) { // We may already have allocated the buffer.
            buffer = new byte[PipedInputStream.PIPE_SIZE];
        }
        isConnected = true;
    }

    /**
     * Reads a single byte from this stream and returns it as an integer in the
     * range from 0 to 255. Returns -1 if the end of this stream has been
     * reached. If there is no data in the pipe, this method blocks until data
     * is available, the end of the stream is detected or an exception is
     * thrown.
     * <p>
     * Separate threads should be used to read from a {@code PipedInputStream}
     * and to write to the connected {@link PipedOutputStream}. If the same
     * thread is used, a deadlock may occur.
     *
     * @return the byte read or -1 if the end of the source stream has been
     *         reached.
     * @throws IOException
     *             if this stream is closed or not connected to an output
     *             stream, or if the thread writing to the connected output
     *             stream is no longer alive.
     */
    @Override
    public synchronized int read() throws IOException {
        if (!isConnected) {
            throw new IOException("Not connected");
        }
        if (buffer == null) {
            throw new IOException("InputStream is closed");
        }

        if (isClosed && in == -1) {
            // write end closed and no more need to read
            return -1;
        }

        /**
         * Set the last thread to be reading on this PipedInputStream. If
         * lastReader dies while someone is waiting to write an IOException of
         * "Pipe broken" will be thrown in receive()
         */
        lastReader = Thread.currentThread();
        try {
            int attempts = 3;
            while (in == -1) {
                // Are we at end of stream?
                if (isClosed) {
                    return -1;
                }
                if ((attempts-- <= 0) && lastWriter != null && !lastWriter.isAlive()) {
                    throw new IOException("Pipe broken");
                }
                // Notify callers of receive()
                notifyAll();
                wait(1000);
            }
        } catch (InterruptedException e) {
            IoUtils.throwInterruptedIoException();
        }

        int result = buffer[out++] & 0xff;
        if (out == buffer.length) {
            out = 0;
        }
        if (out == in) {
            // empty buffer
            in = -1;
            out = 0;
        }

        // let blocked writers write to the newly available buffer space
        notifyAll();

        return result;
    }

    /**
     * Reads at most {@code byteCount} bytes from this stream and stores them in the
     * byte array {@code bytes} starting at {@code offset}. Blocks until at
     * least one byte has been read, the end of the stream is detected or an
     * exception is thrown.
     * <p>
     * Separate threads should be used to read from a {@code PipedInputStream}
     * and to write to the connected {@link PipedOutputStream}. If the same
     * thread is used, a deadlock may occur.
     *
     * @return the number of bytes actually read or -1 if the end of the stream
     *         has been reached.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code byteCount < 0}, or if {@code
     *             offset + byteCount} is greater than the size of {@code bytes}.
     * @throws InterruptedIOException
     *             if the thread reading from this stream is interrupted.
     * @throws IOException
     *             if this stream is closed or not connected to an output
     *             stream, or if the thread writing to the connected output
     *             stream is no longer alive.
     * @throws NullPointerException
     *             if {@code bytes} is {@code null}.
     */
    @Override
    public synchronized int read(byte[] bytes, int offset, int byteCount) throws IOException {
        if (bytes == null) {
            throw new NullPointerException();
        }

        if (offset < 0 || offset > bytes.length || byteCount < 0
                || byteCount > bytes.length - offset) {
            throw new IndexOutOfBoundsException();
        }

        if (byteCount == 0) {
            return 0;
        }

        if (isClosed && in == -1) {
            // write end closed and no more need to read
            return -1;
        }

        if (!isConnected) {
            throw new IOException("Not connected");
        }

        if (buffer == null) {
            throw new IOException("InputStream is closed");
        }

        /*
         * Set the last thread to be reading on this PipedInputStream. If
         * lastReader dies while someone is waiting to write an IOException of
         * "Pipe broken" will be thrown in receive()
         */
        lastReader = Thread.currentThread();
        try {
            int attempts = 3;
            while (in == -1) {
                // Are we at end of stream?
                if (isClosed) {
                    return -1;
                }
                if ((attempts-- <= 0) && lastWriter != null && !lastWriter.isAlive()) {
                    throw new IOException("Pipe broken");
                }
                // Notify callers of receive()
                notifyAll();
                wait(1000);
            }
        } catch (InterruptedException e) {
            IoUtils.throwInterruptedIoException();
        }

        int totalCopied = 0;

        // copy bytes from out thru the end of buffer
        if (out >= in) {
            int leftInBuffer = buffer.length - out;
            int length = leftInBuffer < byteCount ? leftInBuffer : byteCount;
            System.arraycopy(buffer, out, bytes, offset, length);
            out += length;
            if (out == buffer.length) {
                out = 0;
            }
            if (out == in) {
                // empty buffer
                in = -1;
                out = 0;
            }
            totalCopied += length;
        }

        // copy bytes from out thru in
        if (totalCopied < byteCount && in != -1) {
            int leftInBuffer = in - out;
            int leftToCopy = byteCount - totalCopied;
            int length = leftToCopy < leftInBuffer ? leftToCopy : leftInBuffer;
            System.arraycopy(buffer, out, bytes, offset + totalCopied, length);
            out += length;
            if (out == in) {
                // empty buffer
                in = -1;
                out = 0;
            }
            totalCopied += length;
        }

        // let blocked writers write to the newly available buffer space
        notifyAll();

        return totalCopied;
    }

    /**
     * Receives a byte and stores it in this stream's {@code buffer}. This
     * method is called by {@link PipedOutputStream#write(int)}. The least
     * significant byte of the integer {@code oneByte} is stored at index
     * {@code in} in the {@code buffer}.
     * <p>
     * This method blocks as long as {@code buffer} is full.
     *
     * @param oneByte
     *            the byte to store in this pipe.
     * @throws InterruptedIOException
     *             if the {@code buffer} is full and the thread that has called
     *             this method is interrupted.
     * @throws IOException
     *             if this stream is closed or the thread that has last read
     *             from this stream is no longer alive.
     */
    protected synchronized void receive(int oneByte) throws IOException {
        if (buffer == null || isClosed) {
            throw new IOException("Pipe is closed");
        }
        if (lastReader != null && !lastReader.isAlive()) {
            throw new IOException("Pipe broken");
        }

        /*
         * Set the last thread to be writing on this PipedInputStream. If
         * lastWriter dies while someone is waiting to read an IOException of
         * "Pipe broken" will be thrown in read()
         */
        lastWriter = Thread.currentThread();
        try {
            while (buffer != null && out == in) {
                if (lastReader != null && !lastReader.isAlive()) {
                    throw new IOException("Pipe broken");
                }
                notifyAll();
                wait(1000);
            }
        } catch (InterruptedException e) {
            IoUtils.throwInterruptedIoException();
        }
        if (buffer == null) {
            throw new IOException("Pipe is closed");
        }
        if (in == -1) {
            in = 0;
        }
        buffer[in++] = (byte) oneByte;
        if (in == buffer.length) {
            in = 0;
        }

        // let blocked readers read the newly available data
        notifyAll();
    }

    synchronized void done() {
        isClosed = true;
        notifyAll();
    }
}
