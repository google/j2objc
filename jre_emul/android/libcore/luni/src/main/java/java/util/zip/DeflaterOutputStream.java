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
import libcore.io.Streams;

/**
 * This class provides an implementation of {@code FilterOutputStream} that
 * compresses data using the <i>DEFLATE</i> algorithm. Basically it wraps the
 * {@code Deflater} class and takes care of the buffering.
 *
 * @see Deflater
 */
public class DeflaterOutputStream extends FilterOutputStream {
    static final int BUF_SIZE = 512;

    /**
     * The buffer for the data to be written to.
     */
    protected byte[] buf;

    /**
     * The deflater used.
     */
    protected Deflater def;

    boolean done = false;

    private final boolean syncFlush;

    /**
     * Constructs a new instance with a default-constructed {@link Deflater}.
     */
    public DeflaterOutputStream(OutputStream os) {
        this(os, new Deflater(), BUF_SIZE, false);
    }

    /**
     * Constructs a new instance with the given {@code Deflater}.
     */
    public DeflaterOutputStream(OutputStream os, Deflater def) {
        this(os, def, BUF_SIZE, false);
    }

    /**
     * Constructs a new instance with the given {@code Deflater} and buffer size.
     */
    public DeflaterOutputStream(OutputStream os, Deflater def, int bufferSize) {
        this(os, def, bufferSize, false);
    }

    /**
     * Constructs a new instance with the given flushing behavior (see {@link #flush}).
     * @since 1.7
     */
    public DeflaterOutputStream(OutputStream os, boolean syncFlush) {
        this(os, new Deflater(), BUF_SIZE, syncFlush);
    }

    /**
     * Constructs a new instance with the given {@code Deflater} and
     * flushing behavior (see {@link #flush}).
     * @since 1.7
     */
    public DeflaterOutputStream(OutputStream os, Deflater def, boolean syncFlush) {
        this(os, def, BUF_SIZE, syncFlush);
    }

    /**
     * Constructs a new instance with the given {@code Deflater}, buffer size, and
     * flushing behavior (see {@link #flush}).
     * @since 1.7
     */
    public DeflaterOutputStream(OutputStream os, Deflater def, int bufferSize, boolean syncFlush) {
        super(os);
        if (os == null) {
            throw new NullPointerException("os == null");
        } else if (def == null) {
            throw new NullPointerException("def == null");
        }
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("bufferSize <= 0: " + bufferSize);
        }
        this.def = def;
        this.syncFlush = syncFlush;
        buf = new byte[bufferSize];
    }

    /**
     * Compress the data in the input buffer and write it to the underlying
     * stream.
     *
     * @throws IOException
     *             If an error occurs during deflation.
     */
    protected void deflate() throws IOException {
        int byteCount;
        while ((byteCount = def.deflate(buf)) != 0) {
            out.write(buf, 0, byteCount);
        }
    }

    /**
     * Writes any unwritten compressed data to the underlying stream, the closes
     * all underlying streams. This stream can no longer be used after close()
     * has been called.
     *
     * @throws IOException
     *             If an error occurs while closing the data compression
     *             process.
     */
    @Override
    public void close() throws IOException {
        // everything closed here should also be closed in ZipOutputStream.close()
        if (!def.finished()) {
            finish();
        }
        def.end();
        out.close();
    }

    /**
     * Writes any unwritten data to the underlying stream. Does not close the
     * stream.
     *
     * @throws IOException
     *             If an error occurs.
     */
    public void finish() throws IOException {
        if (done) {
            return;
        }
        def.finish();
        while (!def.finished()) {
            int byteCount = def.deflate(buf);
            out.write(buf, 0, byteCount);
        }
        done = true;
    }

    @Override public void write(int i) throws IOException {
        Streams.writeSingleByte(this, i);
    }

    /**
     * Compresses {@code byteCount} bytes of data from {@code buf} starting at
     * {@code offset} and writes it to the underlying stream.
     * @throws IOException
     *             If an error occurs during writing.
     */
    @Override public void write(byte[] buffer, int offset, int byteCount) throws IOException {
        if (done) {
            throw new IOException("attempt to write after finish");
        }
        Arrays.checkOffsetAndCount(buffer.length, offset, byteCount);
        if (!def.needsInput()) {
            throw new IOException();
        }
        def.setInput(buffer, offset, byteCount);
        deflate();
    }

    /**
     * Flushes the underlying stream. This flushes only the bytes that can be
     * compressed at the highest level.
     *
     * <p>For deflater output streams constructed with the {@code syncFlush} parameter set to true,
     * this first flushes all outstanding data so that it may be immediately read by its recipient.
     * Doing so may degrade compression but improve interactive behavior.
     */
    @Override public void flush() throws IOException {
        // Though not documented, it's illegal to call deflate with any flush param
        // other than Z_FINISH after the deflater has finished. See the error checking
        // at the start of the deflate function in deflate.c.
        if (syncFlush && !done) {
            int byteCount;
            while ((byteCount = def.deflate(buf, 0, buf.length, Deflater.SYNC_FLUSH)) != 0) {
                out.write(buf, 0, byteCount);
            }
        }
        out.flush();
    }
}
