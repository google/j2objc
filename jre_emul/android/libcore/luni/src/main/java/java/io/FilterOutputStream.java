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
import libcore.util.SneakyThrow;

/**
 * Wraps an existing {@link OutputStream} and performs some transformation on
 * the output data while it is being written. Transformations can be anything
 * from a simple byte-wise filtering output data to an on-the-fly compression or
 * decompression of the underlying stream. Output streams that wrap another
 * output stream and provide some additional functionality on top of it usually
 * inherit from this class.
 *
 * @see FilterOutputStream
 */
public class FilterOutputStream extends OutputStream {

    /**
     * The target output stream for this filter stream.
     */
    protected OutputStream out;

    /**
     * Constructs a new {@code FilterOutputStream} with {@code out} as its
     * target stream.
     *
     * @param out
     *            the target stream that this stream writes to.
     */
    public FilterOutputStream(OutputStream out) {
        this.out = out;
    }

    /**
     * Closes this stream. This implementation closes the target stream.
     *
     * @throws IOException
     *             if an error occurs attempting to close this stream.
     */
    @Override
    public void close() throws IOException {
        Throwable thrown = null;
        try {
            flush();
        } catch (Throwable e) {
            thrown = e;
        }

        try {
            out.close();
        } catch (Throwable e) {
            if (thrown == null) {
                thrown = e;
            }
        }

        if (thrown != null) {
            SneakyThrow.sneakyThrow(thrown);
        }
    }

    /**
     * Ensures that all pending data is sent out to the target stream. This
     * implementation flushes the target stream.
     *
     * @throws IOException
     *             if an error occurs attempting to flush this stream.
     */
    @Override
    public void flush() throws IOException {
        out.flush();
    }

    /**
     * Writes {@code count} bytes from the byte array {@code buffer} starting at
     * {@code offset} to the target stream.
     *
     * @param buffer
     *            the buffer to write.
     * @param offset
     *            the index of the first byte in {@code buffer} to write.
     * @param length
     *            the number of bytes in {@code buffer} to write.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code count < 0}, or if
     *             {@code offset + count} is bigger than the length of
     *             {@code buffer}.
     * @throws IOException
     *             if an I/O error occurs while writing to this stream.
     */
    @Override
    public void write(byte[] buffer, int offset, int length) throws IOException {
        Arrays.checkOffsetAndCount(buffer.length, offset, length);
        for (int i = 0; i < length; i++) {
            // Call write() instead of out.write() since subclasses could
            // override the write() method.
            write(buffer[offset + i]);
        }
    }

    /**
     * Writes one byte to the target stream. Only the low order byte of the
     * integer {@code oneByte} is written.
     *
     * @param oneByte
     *            the byte to be written.
     * @throws IOException
     *             if an I/O error occurs while writing to this stream.
     */
    @Override
    public void write(int oneByte) throws IOException {
        out.write(oneByte);
    }
}
