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
 * Wraps an existing {@link InputStream} and performs some transformation on
 * the input data while it is being read. Transformations can be anything from a
 * simple byte-wise filtering input data to an on-the-fly compression or
 * decompression of the underlying stream. Input streams that wrap another input
 * stream and provide some additional functionality on top of it usually inherit
 * from this class.
 *
 * @see FilterOutputStream
 */
public class FilterInputStream extends InputStream {

    /**
     * The source input stream that is filtered.
     */
    protected volatile InputStream in;

    /**
     * Constructs a new {@code FilterInputStream} with the specified input
     * stream as source.
     *
     * <p><strong>Warning:</strong> passing a null source creates an invalid
     * {@code FilterInputStream}, that fails on every method that is not
     * overridden. Subclasses should check for null in their constructors.
     *
     * @param in the input stream to filter reads on.
     */
    protected FilterInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public int available() throws IOException {
        return in.available();
    }

    /**
     * Closes this stream. This implementation closes the filtered stream.
     *
     * @throws IOException
     *             if an error occurs while closing this stream.
     */
    @Override
    public void close() throws IOException {
        in.close();
    }

    /**
     * Sets a mark position in this stream. The parameter {@code readlimit}
     * indicates how many bytes can be read before the mark is invalidated.
     * Sending {@code reset()} will reposition this stream back to the marked
     * position, provided that {@code readlimit} has not been surpassed.
     * <p>
     * This implementation sets a mark in the filtered stream.
     *
     * @param readlimit
     *            the number of bytes that can be read from this stream before
     *            the mark is invalidated.
     * @see #markSupported()
     * @see #reset()
     */
    @Override
    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
    }

    /**
     * Indicates whether this stream supports {@code mark()} and {@code reset()}.
     * This implementation returns whether or not the filtered stream supports
     * marking.
     *
     * @return {@code true} if {@code mark()} and {@code reset()} are supported,
     *         {@code false} otherwise.
     * @see #mark(int)
     * @see #reset()
     * @see #skip(long)
     */
    @Override
    public boolean markSupported() {
        return in.markSupported();
    }

    /**
     * Reads a single byte from the filtered stream and returns it as an integer
     * in the range from 0 to 255. Returns -1 if the end of this stream has been
     * reached.
     *
     * @return the byte read or -1 if the end of the filtered stream has been
     *         reached.
     * @throws IOException
     *             if the stream is closed or another IOException occurs.
     */
    @Override
    public int read() throws IOException {
        return in.read();
    }

    @Override public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        return in.read(buffer, byteOffset, byteCount);
    }

    /**
     * Resets this stream to the last marked location. This implementation
     * resets the target stream.
     *
     * @throws IOException
     *             if this stream is already closed, no mark has been set or the
     *             mark is no longer valid because more than {@code readlimit}
     *             bytes have been read since setting the mark.
     * @see #mark(int)
     * @see #markSupported()
     */
    @Override
    public synchronized void reset() throws IOException {
        in.reset();
    }

    /**
     * Skips {@code byteCount} bytes in this stream. Subsequent
     * calls to {@code read} will not return these bytes unless {@code reset} is
     * used. This implementation skips {@code byteCount} bytes in the
     * filtered stream.
     *
     * @return the number of bytes actually skipped.
     * @throws IOException
     *             if this stream is closed or another IOException occurs.
     * @see #mark(int)
     * @see #reset()
     */
    @Override
    public long skip(long byteCount) throws IOException {
        return in.skip(byteCount);
    }
}
