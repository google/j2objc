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
 * Wraps an existing {@link Reader} and performs some transformation on the
 * input data while it is being read. Transformations can be anything from a
 * simple byte-wise filtering input data to an on-the-fly compression or
 * decompression of the underlying reader. Readers that wrap another reader and
 * provide some additional functionality on top of it usually inherit from this
 * class.
 *
 * @see FilterWriter
 */
public abstract class FilterReader extends Reader {

    /**
     * The target Reader which is being filtered.
     */
    protected Reader in;

    /**
     * Constructs a new FilterReader on the Reader {@code in}.
     *
     * @param in
     *            The non-null Reader to filter reads on.
     */
    protected FilterReader(Reader in) {
        super(in);
        this.in = in;
    }

    /**
     * Closes this reader. This implementation closes the filtered reader.
     *
     * @throws IOException
     *             if an error occurs while closing this reader.
     */
    @Override
    public void close() throws IOException {
        synchronized (lock) {
            in.close();
        }
    }

    /**
     * Sets a mark position in this reader. The parameter {@code readlimit}
     * indicates how many bytes can be read before the mark is invalidated.
     * Sending {@code reset()} will reposition this reader back to the marked
     * position, provided that {@code readlimit} has not been surpassed.
     * <p>
     * This implementation sets a mark in the filtered reader.
     *
     * @param readlimit
     *            the number of bytes that can be read from this reader before
     *            the mark is invalidated.
     * @throws IOException
     *             if an error occurs while marking this reader.
     * @see #markSupported()
     * @see #reset()
     */
    @Override
    public synchronized void mark(int readlimit) throws IOException {
        synchronized (lock) {
            in.mark(readlimit);
        }
    }

    /**
     * Indicates whether this reader supports {@code mark()} and {@code reset()}.
     * This implementation returns whether the filtered reader supports marking.
     *
     * @return {@code true} if {@code mark()} and {@code reset()} are supported
     *         by the filtered reader, {@code false} otherwise.
     * @see #mark(int)
     * @see #reset()
     * @see #skip(long)
     */
    @Override
    public boolean markSupported() {
        synchronized (lock) {
            return in.markSupported();
        }
    }

    /**
     * Reads a single character from the filtered reader and returns it as an
     * integer with the two higher-order bytes set to 0. Returns -1 if the end
     * of the filtered reader has been reached.
     *
     * @return The character read or -1 if the end of the filtered reader has
     *         been reached.
     * @throws IOException
     *             if an error occurs while reading from this reader.
     */
    @Override
    public int read() throws IOException {
        synchronized (lock) {
            return in.read();
        }
    }

    /**
     * Reads up to {@code count} characters from the filtered reader and stores them
     * in the byte array {@code buffer} starting at {@code offset}. Returns the
     * number of characters actually read or -1 if no characters were read and
     * the end of the filtered reader was encountered.
     *
     * @throws IOException
     *             if an error occurs while reading from this reader.
     */
    @Override
    public int read(char[] buffer, int offset, int count) throws IOException {
        synchronized (lock) {
            return in.read(buffer, offset, count);
        }
    }

    /**
     * Indicates whether this reader is ready to be read without blocking. If
     * the result is {@code true}, the next {@code read()} will not block. If
     * the result is {@code false}, this reader may or may not block when
     * {@code read()} is sent.
     *
     * @return {@code true} if this reader will not block when {@code read()}
     *         is called, {@code false} if unknown or blocking will occur.
     * @throws IOException
     *             if the reader is closed or some other I/O error occurs.
     */
    @Override
    public boolean ready() throws IOException {
        synchronized (lock) {
            return in.ready();
        }
    }

    /**
     * Resets this reader's position to the last marked location. Invocations of
     * {@code read()} and {@code skip()} will occur from this new location. If
     * this reader was not marked, the behavior depends on the implementation of
     * {@code reset()} in the Reader subclass that is filtered by this reader.
     * The default behavior for Reader is to throw an {@code IOException}.
     *
     * @throws IOException
     *             if a problem occurred or the filtered reader does not support
     *             {@code mark()} and {@code reset()}.
     * @see #mark(int)
     * @see #markSupported()
     */
    @Override
    public void reset() throws IOException {
        synchronized (lock) {
            in.reset();
        }
    }

    /**
     * Skips {@code charCount} characters in this reader. Subsequent calls to {@code read}
     * will not return these characters unless {@code reset} is used. The
     * default implementation is to skip characters in the filtered reader.
     *
     * @return the number of characters actually skipped.
     * @throws IOException
     *             if the filtered reader is closed or some other I/O error
     *             occurs.
     * @see #mark(int)
     * @see #markSupported()
     * @see #reset()
     */
    @Override
    public long skip(long charCount) throws IOException {
        synchronized (lock) {
            return in.skip(charCount);
        }
    }
}
