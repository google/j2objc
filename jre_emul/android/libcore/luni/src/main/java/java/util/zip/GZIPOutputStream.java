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

import java.io.IOException;
import java.io.OutputStream;

/**
 * The {@code GZIPOutputStream} class is used to write data to a stream in the
 * GZIP storage format.
 *
 * <h3>Example</h3>
 * <p>Using {@code GZIPOutputStream} is a little easier than {@link ZipOutputStream}
 * because GZIP is only for compression, and is not a container for multiple files.
 * This code creates a GZIP stream, similar to the {@code gzip(1)} utility.
 * <pre>
 * OutputStream os = ...
 * byte[] bytes = ...
 * GZIPOutputStream zos = new GZIPOutputStream(new BufferedOutputStream(os));
 * try {
 *     zos.write(bytes);
 * } finally {
 *     zos.close();
 * }
 * </pre>
 */
public class GZIPOutputStream extends DeflaterOutputStream {

    /**
     * The checksum algorithm used when treating uncompressed data.
     */
    protected CRC32 crc = new CRC32();

    /**
     * Constructs a new {@code GZIPOutputStream} to write data in GZIP format to
     * the given stream.
     */
    public GZIPOutputStream(OutputStream os) throws IOException {
        this(os, BUF_SIZE, false);
    }

    /**
     * Constructs a new {@code GZIPOutputStream} to write data in GZIP format to
     * the given stream with the given flushing behavior (see {@link DeflaterOutputStream#flush}).
     * @since 1.7
     */
    public GZIPOutputStream(OutputStream os, boolean syncFlush) throws IOException {
        this(os, BUF_SIZE, syncFlush);
    }

    /**
     * Constructs a new {@code GZIPOutputStream} to write data in GZIP format to
     * the given stream with the given internal buffer size.
     */
    public GZIPOutputStream(OutputStream os, int bufferSize) throws IOException {
        this(os, bufferSize, false);
    }

    /**
     * Constructs a new {@code GZIPOutputStream} to write data in GZIP format to
     * the given stream with the given internal buffer size and
     * flushing behavior (see {@link DeflaterOutputStream#flush}).
     * @since 1.7
     */
    public GZIPOutputStream(OutputStream os, int bufferSize, boolean syncFlush) throws IOException {
        super(os, new Deflater(Deflater.DEFAULT_COMPRESSION, true), bufferSize, syncFlush);
        writeShort(GZIPInputStream.GZIP_MAGIC);
        out.write(Deflater.DEFLATED);
        out.write(0); // flags
        writeLong(0); // mod time
        out.write(0); // extra flags
        out.write(0); // operating system
    }

    /**
     * Indicates to the stream that all data has been written out, and any GZIP
     * terminal data can now be written.
     *
     * @throws IOException
     *             if an {@code IOException} occurs.
     */
    @Override
    public void finish() throws IOException {
        super.finish();
        writeLong(crc.getValue());
        writeLong(crc.tbytes);
    }

    /**
     * Write up to nbytes of data from the given buffer, starting at offset off,
     * to the underlying stream in GZIP format.
     */
    @Override
    public void write(byte[] buffer, int off, int nbytes) throws IOException {
        super.write(buffer, off, nbytes);
        crc.update(buffer, off, nbytes);
    }

    private long writeLong(long i) throws IOException {
        // Write out the long value as an unsigned int
        int unsigned = (int) i;
        out.write(unsigned & 0xFF);
        out.write((unsigned >> 8) & 0xFF);
        out.write((unsigned >> 16) & 0xFF);
        out.write((unsigned >> 24) & 0xFF);
        return i;
    }

    private int writeShort(int i) throws IOException {
        out.write(i & 0xFF);
        out.write((i >> 8) & 0xFF);
        return i;
    }
}
