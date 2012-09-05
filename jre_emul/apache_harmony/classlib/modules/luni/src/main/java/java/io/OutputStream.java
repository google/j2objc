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
 * The base class for all output streams. An output stream is a means of writing
 * data to a target in a byte-wise manner. Most output streams expect the
 * {@link #flush()} method to be called before closing the stream, to ensure all
 * data is actually written through.
 * <p>
 * This abstract class does not provide a fully working implementation, so it
 * needs to be subclassed, and at least the {@link #write(int)} method needs to
 * be overridden. Overriding some of the non-abstract methods is also often
 * advised, since it might result in higher efficiency.
 * <p>
 * Many specialized output streams for purposes like writing to a file already
 * exist in this package.
 * 
 * @see InputStream
 */
public abstract class OutputStream implements Closeable, Flushable {

    /**
     * Default constructor.
     */
    public OutputStream() {
        super();
    }

    /**
     * Closes this stream. Implementations of this method should free any
     * resources used by the stream. This implementation does nothing.
     * 
     * @throws IOException
     *             if an error occurs while closing this stream.
     */
    public void close() throws IOException {
        /* empty */
    }

    /**
     * Flushes this stream. Implementations of this method should ensure that
     * any buffered data is written out. This implementation does nothing.
     * 
     * @throws IOException
     *             if an error occurs while flushing this stream.
     */
    public void flush() throws IOException {
        /* empty */
    }

    /**
     * Writes the entire contents of the byte array {@code buffer} to this
     * stream.
     * 
     * @param buffer
     *            the buffer to be written.
     * @throws IOException
     *             if an error occurs while writing to this stream.
     */
    public void write(byte buffer[]) throws IOException {
        write(buffer, 0, buffer.length);
    }

    /**
     * Writes {@code count} bytes from the byte array {@code buffer} starting at
     * position {@code offset} to this stream.
     * 
     * @param buffer
     *            the buffer to be written.
     * @param offset
     *            the start position in {@code buffer} from where to get bytes.
     * @param count
     *            the number of bytes from {@code buffer} to write to this
     *            stream.
     * @throws IOException
     *             if an error occurs while writing to this stream.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code count < 0}, or if
     *             {@code offset + count} is bigger than the length of
     *             {@code buffer}.
     */
    public void write(byte buffer[], int offset, int count) throws IOException {
        // avoid int overflow, check null buffer
        if (offset > buffer.length || offset < 0 || count < 0
                || count > buffer.length - offset) {
            throw new IndexOutOfBoundsException("arguments out of bounds");
        }
        for (int i = offset; i < offset + count; i++) {
            write(buffer[i]);
        }
    }

    /**
     * Writes a single byte to this stream. Only the least significant byte of
     * the integer {@code oneByte} is written to the stream.
     * 
     * @param oneByte
     *            the byte to be written.
     * @throws IOException
     *             if an error occurs while writing to this stream.
     */
    public abstract void write(int oneByte) throws IOException;

    /**
     * Returns true if this writer has encountered and suppressed an error. Used
     * by PrintStreams as an alternative to checked exceptions.
     */
    boolean checkError() {
        return false;
    }
}
