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
 * The base class for all input streams. An input stream is a means of reading
 * data from a source in a byte-wise manner.
 * <p>
 * Some input streams also support marking a position in the input stream and
 * returning to this position later. This abstract class does not provide a
 * fully working implementation, so it needs to be subclassed, and at least the
 * {@link #read()} method needs to be overridden. Overriding some of the
 * non-abstract methods is also often advised, since it might result in higher
 * efficiency.
 * <p>
 * Many specialized input streams for purposes like reading from a file already
 * exist in this package.
 * 
 * @see OutputStream
 */
public abstract class InputStream extends Object implements Closeable {

    private static byte[] skipBuf;

    /**
     * This constructor does nothing. It is provided for signature
     * compatibility.
     */
    public InputStream() {
        /* empty */
    }

    /**
     * Returns the number of bytes that are available before this stream will
     * block. This implementation always returns 0. Subclasses should override
     * and indicate the correct number of bytes available.
     * 
     * @return the number of bytes available before blocking.
     * @throws IOException
     *             if an error occurs in this stream.
     */
    public int available() throws IOException {
        return 0;
    }

    /**
     * Closes this stream. Concrete implementations of this class should free
     * any resources during close. This implementation does nothing.
     * 
     * @throws IOException
     *             if an error occurs while closing this stream.
     */
    public void close() throws IOException {
        /* empty */
    }

    /**
     * Sets a mark position in this InputStream. The parameter {@code readlimit}
     * indicates how many bytes can be read before the mark is invalidated.
     * Sending {@code reset()} will reposition the stream back to the marked
     * position provided {@code readLimit} has not been surpassed.
     * <p>
     * This default implementation does nothing and concrete subclasses must
     * provide their own implementation.
     *
     * @param readlimit
     *            the number of bytes that can be read from this stream before
     *            the mark is invalidated.
     * @see #markSupported()
     * @see #reset()
     */
    public void mark(int readlimit) {
        /* empty */
    }

    /**
     * Indicates whether this stream supports the {@code mark()} and
     * {@code reset()} methods. The default implementation returns {@code false}.
     * 
     * @return always {@code false}.
     * @see #mark(int)
     * @see #reset()
     */
    public boolean markSupported() {
        return false;
    }

    /**
     * Reads a single byte from this stream and returns it as an integer in the
     * range from 0 to 255. Returns -1 if the end of the stream has been
     * reached. Blocks until one byte has been read, the end of the source
     * stream is detected or an exception is thrown.
     * 
     * @return the byte read or -1 if the end of stream has been reached.
     * @throws IOException
     *             if the stream is closed or another IOException occurs.
     */
    public abstract int read() throws IOException;

    /**
     * Reads bytes from this stream and stores them in the byte array {@code b}.
     * 
     * @param b
     *            the byte array in which to store the bytes read.
     * @return the number of bytes actually read or -1 if the end of the stream
     *         has been reached.
     * @throws IOException
     *             if this stream is closed or another IOException occurs.
     */
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * Reads at most {@code length} bytes from this stream and stores them in
     * the byte array {@code b} starting at {@code offset}.
     * 
     * @param b
     *            the byte array in which to store the bytes read.
     * @param offset
     *            the initial position in {@code buffer} to store the bytes read
     *            from this stream.
     * @param length
     *            the maximum number of bytes to store in {@code b}.
     * @return the number of bytes actually read or -1 if the end of the stream
     *         has been reached.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code length < 0}, or if
     *             {@code offset + length} is greater than the length of
     *             {@code b}.
     * @throws IOException
     *             if the stream is closed or another IOException occurs.
     */
    public int read(byte b[], int offset, int length) throws IOException {
        // Force null check for b first!
	if (b == null) {
	    throw new NullPointerException();
	}
        if (offset > b.length || offset < 0) {
            throw new ArrayIndexOutOfBoundsException("offset out of bounds: " + offset);
        } 
        if (length < 0 || length > b.length - offset) {
            throw new ArrayIndexOutOfBoundsException("length out of bounds: " + length);
        }
        for (int i = 0; i < length; i++) {
            int c;
            try {
                if ((c = read()) == -1) {
                    return i == 0 ? -1 : i;
                }
            } catch (IOException e) {
                if (i != 0) {
                    return i;
                }
                throw e;
            }
            b[offset + i] = (byte) c;
        }
        return length;
    }

    /**
     * Resets this stream to the last marked location. Throws an
     * {@code IOException} if the number of bytes read since the mark has been
     * set is greater than the limit provided to {@code mark}, or if no mark
     * has been set.
     * <p>
     * This implementation always throws an {@code IOException} and concrete
     * subclasses should provide the proper implementation.
     *
     * @throws IOException
     *             if this stream is closed or another IOException occurs.
     */
    public synchronized void reset() throws IOException {
        throw new IOException();
    }

    /**
     * Skips at most {@code n} bytes in this stream. It does nothing and returns
     * 0 if {@code n} is negative. Less than {@code n} characters are skipped if
     * the end of this stream is reached before the operation completes.
     * <p>
     * This default implementation reads {@code n} bytes into a temporary
     * buffer. Concrete subclasses should provide their own implementation.
     *
     * @param n
     *            the number of bytes to skip.
     * @return the number of bytes actually skipped.
     * @throws IOException
     *             if this stream is closed or another IOException occurs.
     */
    public long skip(long n) throws IOException {
        if (n <= 0) {
            return 0;
        }
        long skipped = 0;
        int toRead = n < 4096 ? (int) n : 4096;
        // We are unsynchronized, so take a local copy of the skipBuf at some
        // point in time.
        byte[] localBuf = skipBuf;
        if (localBuf == null || localBuf.length < toRead) {
            // May be lazily written back to the static. No matter if it
            // overwrites somebody else's store.
            skipBuf = localBuf = new byte[toRead];
        }
        while (skipped < n) {
            int read = read(localBuf, 0, toRead);
            if (read == -1) {
                return skipped;
            }
            skipped += read;
            if (read < toRead) {
                return skipped;
            }
            if (n - skipped < toRead) {
                toRead = (int) (n - skipped);
            }
        }
        return skipped;
    }
}
