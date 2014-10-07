/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.util.zip;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * An {@code OutputStream} filter to decompress data. Callers write
 * compressed data in the "deflate" format, and uncompressed data is
 * written to the underlying stream.
 * @since 1.6
 */
public class InflaterOutputStream extends FilterOutputStream {
    private static final int DEFAULT_BUFFER_SIZE = 1024;

    protected final Inflater inf;
    protected final byte[] buf;

    private boolean closed = false;

    /**
     * Constructs an {@code InflaterOutputStream} with a new {@code Inflater} and an
     * implementation-defined default internal buffer size. {@code out} is a destination
     * for uncompressed data, and compressed data will be written to this stream.
     *
     * @param out the destination {@code OutputStream}
     */
    public InflaterOutputStream(OutputStream out) {
        this(out, new Inflater());
    }

    /**
     * Constructs an {@code InflaterOutputStream} with the given {@code Inflater} and an
     * implementation-defined default internal buffer size. {@code out} is a destination
     * for uncompressed data, and compressed data will be written to this stream.
     *
     * @param out the destination {@code OutputStream}
     * @param inf the {@code Inflater} to be used for decompression
     */
    public InflaterOutputStream(OutputStream out, Inflater inf) {
        this(out, inf, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Constructs an {@code InflaterOutputStream} with the given {@code Inflater} and
     * given internal buffer size. {@code out} is a destination
     * for uncompressed data, and compressed data will be written to this stream.
     *
     * @param out the destination {@code OutputStream}
     * @param inf the {@code Inflater} to be used for decompression
     * @param bufferSize the length in bytes of the internal buffer
     */
    public InflaterOutputStream(OutputStream out, Inflater inf, int bufferSize) {
        super(out);
        if (out == null) {
            throw new NullPointerException("out == null");
        } else if (inf == null) {
            throw new NullPointerException("inf == null");
        }
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("bufferSize <= 0: " + bufferSize);
        }
        this.inf = inf;
        this.buf = new byte[bufferSize];
    }

    /**
     * Writes remaining data into the output stream and closes the underlying
     * output stream.
     */
    @Override
    public void close() throws IOException {
        if (!closed) {
            finish();
            inf.end();
            out.close();
            closed = true;
        }
    }

    @Override
    public void flush() throws IOException {
        finish();
        out.flush();
    }

    /**
     * Finishes writing current uncompressed data into the InflaterOutputStream
     * without closing it.
     *
     * @throws IOException if an I/O error occurs, or the stream has been closed
     */
    public void finish() throws IOException {
        checkClosed();
        write();
    }

    /**
     * Writes a byte to the decompressing output stream. {@code b} should be a byte of
     * compressed input. The corresponding uncompressed data will be written to the underlying
     * stream.
     *
     * @param b the byte
     * @throws IOException if an I/O error occurs, or the stream has been closed
     * @throws ZipException if a zip exception occurs.
     */
    @Override
    public void write(int b) throws IOException, ZipException {
        write(new byte[] { (byte) b }, 0, 1);
    }

    /**
     * Writes to the decompressing output stream. The {@code bytes} array should contain
     * compressed input. The corresponding uncompressed data will be written to the underlying
     * stream.
     *
     * @throws IOException if an I/O error occurs, or the stream has been closed
     * @throws ZipException if a zip exception occurs.
     * @throws NullPointerException if {@code b == null}.
     * @throws IndexOutOfBoundsException if {@code off < 0 || len < 0 || off + len > b.length}
     */
    @Override
    public void write(byte[] bytes, int offset, int byteCount) throws IOException, ZipException {
        checkClosed();
        Arrays.checkOffsetAndCount(bytes.length, offset, byteCount);
        inf.setInput(bytes, offset, byteCount);
        write();
    }

    private void write() throws IOException, ZipException {
        try {
            int inflated;
            while ((inflated = inf.inflate(buf)) > 0) {
                out.write(buf, 0, inflated);
            }
        } catch (DataFormatException e) {
            throw new ZipException();
        }
    }

    private void checkClosed() throws IOException {
        if (closed) {
            throw new IOException();
        }
    }
}
