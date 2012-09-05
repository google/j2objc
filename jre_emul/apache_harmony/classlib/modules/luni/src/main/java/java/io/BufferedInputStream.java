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

/**
 * Wraps an existing {@link InputStream} and <em>buffers</em> the input.
 * Expensive interaction with the underlying input stream is minimized, since
 * most (smaller) requests can be satisfied by accessing the buffer alone. The
 * drawback is that some extra space is required to hold the buffer and that
 * copying takes place when filling that buffer, but this is usually outweighed
 * by the performance benefits.
 * 
 * <p/>A typical application pattern for the class looks like this:<p/>
 *
 * <pre>
 * BufferedInputStream buf = new BufferedInputStream(new FileInputStream(&quot;file.java&quot;));
 * </pre>
 *
 * @see BufferedOutputStream
 */
public class BufferedInputStream extends FilterInputStream {
    /**
     * The buffer containing the current bytes read from the target InputStream.
     */
    protected volatile byte[] buf;

    /**
     * The total number of bytes inside the byte array {@code buf}.
     */
    protected int count;

    /**
     * The current limit, which when passed, invalidates the current mark.
     */
    protected int marklimit;

    /**
     * The currently marked position. -1 indicates no mark has been set or the
     * mark has been invalidated.
     */
    protected int markpos = -1;

    /**
     * The current position within the byte array {@code buf}.
     */
    protected int pos;

    /**
     * Constructs a new {@code BufferedInputStream} on the {@link InputStream}
     * {@code in}. The default buffer size (8 KB) is allocated and all reads
     * can now be filtered through this stream.
     * 
     * @param in
     *            the InputStream the buffer reads from.
     */
    public BufferedInputStream(InputStream in) {
        super(in);
        buf = new byte[8192];
    }

    /**
     * Constructs a new {@code BufferedInputStream} on the {@link InputStream}
     * {@code in}. The buffer size is specified by the parameter {@code size}
     * and all reads are now filtered through this stream.
     * 
     * @param in
     *            the input stream the buffer reads from.
     * @param size
     *            the size of buffer to allocate.
     * @throws IllegalArgumentException
     *             if {@code size < 0}.
     */
    public BufferedInputStream(InputStream in, int size) {
        super(in);
        if (size <= 0) {
            throw new IllegalArgumentException("size must be > 0");
        }
        buf = new byte[size];
    }

    /**
     * Returns the number of bytes that are available before this stream will
     * block. This method returns the number of bytes available in the buffer
     * plus those available in the source stream.
     * 
     * @return the number of bytes available before blocking.
     * @throws IOException
     *             if this stream is closed.
     */
    @Override
    public synchronized int available() throws IOException {
        InputStream localIn = in; // 'in' could be invalidated by close()
        if (buf == null || localIn == null) {
            throw new IOException("Stream is closed");
        }
        return count - pos + localIn.available();
    }

    /**
     * Closes this stream. The source stream is closed and any resources
     * associated with it are released.
     * 
     * @throws IOException
     *             if an error occurs while closing this stream.
     */
    @Override
    public void close() throws IOException {
        buf = null;
        InputStream localIn = in;
        in = null;
        if (localIn != null) {
            localIn.close();
        }
    }

    private int fillbuf(InputStream localIn, byte[] localBuf)
            throws IOException {
        if (markpos == -1 || (pos - markpos >= marklimit)) {
            /* Mark position not set or exceeded readlimit */
            int result = localIn.read(localBuf);
            if (result > 0) {
                markpos = -1;
                pos = 0;
                count = result == -1 ? 0 : result;
            }
            return result;
        }
        if (markpos == 0 && marklimit > localBuf.length) {
            /* Increase buffer size to accommodate the readlimit */
            int newLength = localBuf.length * 2;
            if (newLength > marklimit) {
                newLength = marklimit;
            }
            byte[] newbuf = new byte[newLength];
            System.arraycopy(localBuf, 0, newbuf, 0, localBuf.length);
            // Reassign buf, which will invalidate any local references
            // FIXME: what if buf was null?
            localBuf = buf = newbuf;
        } else if (markpos > 0) {
            System.arraycopy(localBuf, markpos, localBuf, 0, localBuf.length
                    - markpos);
        }
        /* Set the new position and mark position */
        pos -= markpos;
        count = markpos = 0;
        int bytesread = localIn.read(localBuf, pos, localBuf.length - pos);
        count = bytesread <= 0 ? pos : pos + bytesread;
        return bytesread;
    }

    /**
     * Sets a mark position in this stream. The parameter {@code readlimit}
     * indicates how many bytes can be read before a mark is invalidated.
     * Calling {@code reset()} will reposition the stream back to the marked
     * position if {@code readlimit} has not been surpassed. The underlying
     * buffer may be increased in size to allow {@code readlimit} number of
     * bytes to be supported.
     * 
     * @param readlimit
     *            the number of bytes that can be read before the mark is
     *            invalidated.
     * @see #reset()
     */
    @Override
    public synchronized void mark(int readlimit) {
        marklimit = readlimit;
        markpos = pos;
    }

    /**
     * Indicates whether {@code BufferedInputStream} supports the {@code mark()}
     * and {@code reset()} methods.
     * 
     * @return {@code true} for BufferedInputStreams.
     * @see #mark(int)
     * @see #reset()
     */
    @Override
    public boolean markSupported() {
        return true;
    }

    /**
     * Reads a single byte from this stream and returns it as an integer in the
     * range from 0 to 255. Returns -1 if the end of the source string has been
     * reached. If the internal buffer does not contain any available bytes then
     * it is filled from the source stream and the first byte is returned.
     * 
     * @return the byte read or -1 if the end of the source stream has been
     *         reached.
     * @throws IOException
     *             if this stream is closed or another IOException occurs.
     */
    @Override
    public synchronized int read() throws IOException {
        // Use local refs since buf and in may be invalidated by an
        // unsynchronized close()
        byte[] localBuf = buf;
        InputStream localIn = in;
        if (localBuf == null || localIn == null) {
            throw new IOException("Stream is closed");
        }

        /* Are there buffered bytes available? */
        if (pos >= count && fillbuf(localIn, localBuf) == -1) {
            return -1; /* no, fill buffer */
        }
        // localBuf may have been invalidated by fillbuf
        if (localBuf != buf) {
            localBuf = buf;
            if (localBuf == null) {
                throw new IOException("Stream is closed");
            }
        }

        /* Did filling the buffer fail with -1 (EOF)? */
        if (count - pos > 0) {
            return localBuf[pos++] & 0xFF;
        }
        return -1;
    }

    /**
     * Reads at most {@code length} bytes from this stream and stores them in
     * byte array {@code buffer} starting at offset {@code offset}. Returns the
     * number of bytes actually read or -1 if no bytes were read and the end of
     * the stream was encountered. If all the buffered bytes have been used, a
     * mark has not been set and the requested number of bytes is larger than
     * the receiver's buffer size, this implementation bypasses the buffer and
     * simply places the results directly into {@code buffer}.
     * 
     * @param buffer
     *            the byte array in which to store the bytes read.
     * @param offset
     *            the initial position in {@code buffer} to store the bytes read
     *            from this stream.
     * @param length
     *            the maximum number of bytes to store in {@code buffer}.
     * @return the number of bytes actually read or -1 if end of stream.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code length < 0}, or if
     *             {@code offset + length} is greater than the size of
     *             {@code buffer}.
     * @throws IOException
     *             if the stream is already closed or another IOException
     *             occurs.
     */
    @Override
    public synchronized int read(byte[] buffer, int offset, int length)
            throws IOException {
        // Use local ref since buf may be invalidated by an unsynchronized
        // close()
        byte[] localBuf = buf;
        if (localBuf == null) {
            throw new IOException("Stream is closed");
        }
	if (buffer == null) {
	    throw new NullPointerException();
	}
        // avoid int overflow
        if (offset > buffer.length - length || offset < 0 || length < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (length == 0) {
            return 0;
        }
        InputStream localIn = in;
        if (localIn == null) {
            throw new IOException("Stream is closed");
        }

        int required;
        if (pos < count) {
            /* There are bytes available in the buffer. */
            int copylength = count - pos >= length ? length : count - pos;
            System.arraycopy(localBuf, pos, buffer, offset, copylength);
            pos += copylength;
            if (copylength == length || localIn.available() == 0) {
                return copylength;
            }
            offset += copylength;
            required = length - copylength;
        } else {
            required = length;
        }

        while (true) {
            int read;
            /*
             * If we're not marked and the required size is greater than the
             * buffer, simply read the bytes directly bypassing the buffer.
             */
            if (markpos == -1 && required >= localBuf.length) {
                read = localIn.read(buffer, offset, required);
                if (read == -1) {
                    return required == length ? -1 : length - required;
                }
            } else {
                if (fillbuf(localIn, localBuf) == -1) {
                    return required == length ? -1 : length - required;
                }
                // localBuf may have been invalidated by fillbuf
                if (localBuf != buf) {
                    localBuf = buf;
                    if (localBuf == null) {
                        throw new IOException("Stream is closed");
                    }
                }

                read = count - pos >= required ? required : count - pos;
                System.arraycopy(localBuf, pos, buffer, offset, read);
                pos += read;
            }
            required -= read;
            if (required == 0) {
                return length;
            }
            if (localIn.available() == 0) {
                return length - required;
            }
            offset += read;
        }
    }

    /**
     * Resets this stream to the last marked location.
     * 
     * @throws IOException
     *             if this stream is closed, no mark has been set or the mark is
     *             no longer valid because more than {@code readlimit} bytes
     *             have been read since setting the mark.
     * @see #mark(int)
     */
    @Override
    public synchronized void reset() throws IOException {
        if (buf == null) {
            throw new IOException("Stream is closed");
        }
        if (-1 == markpos) {
            throw new IOException("Mark has been invalidated.");
        }
        pos = markpos;
    }

    /**
     * Skips {@code amount} number of bytes in this stream. Subsequent
     * {@code read()}'s will not return these bytes unless {@code reset()} is
     * used.
     * 
     * @param amount
     *            the number of bytes to skip. {@code skip} does nothing and
     *            returns 0 if {@code amount} is less than zero.
     * @return the number of bytes actually skipped.
     * @throws IOException
     *             if this stream is closed or another IOException occurs.
     */
    @Override
    public synchronized long skip(long amount) throws IOException {
        // Use local refs since buf and in may be invalidated by an
        // unsynchronized close()
        byte[] localBuf = buf;
        InputStream localIn = in;
        if (localBuf == null) {
            throw new IOException("Stream is closed");
        }
        if (amount < 1) {
            return 0;
        }
        if (localIn == null) {
            throw new IOException("Stream is closed");
        }

        if (count - pos >= amount) {
            pos += amount;
            return amount;
        }
        long read = count - pos;
        pos = count;

        if (markpos != -1) {
            if (amount <= marklimit) {
                if (fillbuf(localIn, localBuf) == -1) {
                    return read;
                }
                if (count - pos >= amount - read) {
                    pos += amount - read;
                    return amount;
                }
                // Couldn't get all the bytes, skip what we read
                read += (count - pos);
                pos = count;
                return read;
            }
        }
        return read + localIn.skip(amount - read);
    }
}
