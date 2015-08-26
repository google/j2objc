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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import libcore.io.Streams;

/**
 * An {@code InputStream} filter to compress data. Callers read
 * compressed data in the "deflate" format from the uncompressed
 * underlying stream.
 * @since 1.6
 */
public class DeflaterInputStream extends FilterInputStream {
    private static final int DEFAULT_BUFFER_SIZE = 1024;

    protected final Deflater def;
    protected final byte[] buf;

    private boolean closed = false;
    private boolean available = true;

    /**
     * Constructs a {@code DeflaterInputStream} with a new {@code Deflater} and an
     * implementation-defined default internal buffer size. {@code in} is a source of
     * uncompressed data, and this stream will be a source of compressed data.
     *
     * @param in the source {@code InputStream}
     */
    public DeflaterInputStream(InputStream in) {
        this(in, new Deflater(), DEFAULT_BUFFER_SIZE);
    }

    /**
     * Constructs a {@code DeflaterInputStream} with the given {@code Deflater} and an
     * implementation-defined default internal buffer size. {@code in} is a source of
     * uncompressed data, and this stream will be a source of compressed data.
     *
     * @param in the source {@code InputStream}
     * @param deflater the {@code Deflater} to be used for compression
     */
    public DeflaterInputStream(InputStream in, Deflater deflater) {
        this(in, deflater, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Constructs a {@code DeflaterInputStream} with the given {@code Deflater} and
     * given internal buffer size. {@code in} is a source of
     * uncompressed data, and this stream will be a source of compressed data.
     *
     * @param in the source {@code InputStream}
     * @param deflater the {@code Deflater} to be used for compression
     * @param bufferSize the length in bytes of the internal buffer
     */
    public DeflaterInputStream(InputStream in, Deflater deflater, int bufferSize) {
        super(in);
        if (in == null) {
            throw new NullPointerException("in == null");
        } else if (deflater == null) {
            throw new NullPointerException("deflater == null");
        }
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("bufferSize <= 0: " + bufferSize);
        }
        this.def = deflater;
        this.buf = new byte[bufferSize];
    }

    /**
     * Closes the underlying input stream and discards any remaining uncompressed
     * data.
     */
    @Override
    public void close() throws IOException {
        closed = true;
        def.end();
        in.close();
    }

    /**
     * Reads a byte from the compressed input stream. The result will be a byte of compressed
     * data corresponding to an uncompressed byte or bytes read from the underlying stream.
     *
     * @return the byte or -1 if the end of the stream has been reached.
     */
    @Override public int read() throws IOException {
        return Streams.readSingleByte(this);
    }

    /**
     * Reads up to {@code byteCount} bytes of compressed data into a byte buffer. The result will be bytes of compressed
     * data corresponding to an uncompressed byte or bytes read from the underlying stream.
     * Returns the number of bytes read or -1 if the end of the compressed input
     * stream has been reached.
     */
    @Override public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        checkClosed();
        Arrays.checkOffsetAndCount(buffer.length, byteOffset, byteCount);
        if (byteCount == 0) {
            return 0;
        }

        if (!available) {
            return -1;
        }

        int count = 0;
        while (count < byteCount && !def.finished()) {
            if (def.needsInput()) {
                // read data from input stream
                int bytesRead = in.read(buf);
                if (bytesRead == -1) {
                    def.finish();
                } else {
                    def.setInput(buf, 0, bytesRead);
                }
            }
            int bytesDeflated = def.deflate(buffer, byteOffset + count, byteCount - count);
            if (bytesDeflated == -1) {
                break;
            }
            count += bytesDeflated;
        }
        if (count == 0) {
            count = -1;
            available = false;
        }

        if (def.finished()) {
            available = false;
        }
        return count;
    }

    /**
     * {@inheritDoc}
     * <p>Note: if {@code n > Integer.MAX_VALUE}, this stream will only attempt to
     * skip {@code Integer.MAX_VALUE} bytes.
     */
    @Override
    public long skip(long byteCount) throws IOException {
        byteCount = Math.min(Integer.MAX_VALUE, byteCount);
        return Streams.skipByReading(this, byteCount);
    }

    /**
     * Returns 0 when when this stream has exhausted its input; and 1 otherwise.
     * A result of 1 does not guarantee that further bytes can be returned,
     * with or without blocking.
     *
     * <p>Although consistent with the RI, this behavior is inconsistent with
     * {@link InputStream#available()}, and violates the <a
     * href="http://en.wikipedia.org/wiki/Liskov_substitution_principle">Liskov
     * Substitution Principle</a>. This method should not be used.
     *
     * @return 0 if no further bytes are available. Otherwise returns 1,
     *         which suggests (but does not guarantee) that additional bytes are
     *         available.
     * @throws IOException if this stream is closed or an error occurs
     */
    @Override
    public int available() throws IOException {
        checkClosed();
        return available ? 1 : 0;
    }

    /**
     * Returns false because {@code DeflaterInputStream} does not support
     * {@code mark}/{@code reset}.
     */
    @Override
    public boolean markSupported() {
        return false;
    }

    /**
     * This operation is not supported and does nothing.
     */
    @Override
    public void mark(int limit) {
    }

    /**
     * This operation is not supported and throws {@code IOException}.
     */
    @Override
    public void reset() throws IOException {
        throw new IOException();
    }

    private void checkClosed() throws IOException {
        if (closed) {
            throw new IOException("Stream is closed");
        }
    }
}
