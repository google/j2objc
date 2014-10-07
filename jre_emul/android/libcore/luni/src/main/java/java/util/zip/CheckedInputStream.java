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
import java.io.InputStream;
import libcore.io.Streams;

/**
 * The {@code CheckedInputStream} class is used to maintain a checksum at the
 * same time as the data, on which the checksum is computed, is read from a
 * stream. The purpose of this checksum is to establish data integrity,
 * comparing the computed checksum against a published checksum value.
 */
public class CheckedInputStream extends java.io.FilterInputStream {

    private final Checksum check;

    /**
     * Constructs a new {@code CheckedInputStream} on {@code InputStream}
     * {@code is}. The checksum will be calculated using the algorithm
     * implemented by {@code csum}.
     *
     * <p><strong>Warning:</strong> passing a null source creates an invalid
     * {@code CheckedInputStream}. All operations on such a stream will fail.
     *
     * @param is
     *            the input stream to calculate checksum from.
     * @param csum
     *            an entity implementing the checksum algorithm.
     */
    public CheckedInputStream(InputStream is, Checksum csum) {
        super(is);
        check = csum;
    }

    /**
     * Reads one byte of data from the underlying input stream and updates the
     * checksum with the byte data.
     *
     * @return {@code -1} at the end of the stream, a single byte value
     *         otherwise.
     * @throws IOException
     *             if an {@code IOException} occurs.
     */
    @Override
    public int read() throws IOException {
        int x = in.read();
        if (x != -1) {
            check.update(x);
        }
        return x;
    }

    /**
     * Reads up to {@code byteCount} bytes of data from the underlying input stream, storing it
     * into {@code buffer}, starting at offset {@code byteOffset}. The checksum is
     * updated with the bytes read.
     * Returns the number of bytes actually read or {@code -1} if arrived at the
     * end of the filtered stream while reading the data.
     *
     * @throws IOException
     *             if this stream is closed or some I/O error occurs.
     */
    @Override
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        int bytesRead = in.read(buffer, byteOffset, byteCount);
        if (bytesRead != -1) {
            check.update(buffer, byteOffset, bytesRead);
        }
        return bytesRead;
    }

    /**
     * Returns the checksum calculated on the stream read so far.
     */
    public Checksum getChecksum() {
        return check;
    }

    /**
     * Skip up to {@code byteCount} bytes of data on the underlying input
     * stream. Any skipped bytes are added to the running checksum value.
     *
     * @param byteCount the number of bytes to skip.
     * @throws IOException if this stream is closed or another I/O error occurs.
     * @return the number of bytes skipped.
     */
    @Override
    public long skip(long byteCount) throws IOException {
        return Streams.skipByReading(this, byteCount);
    }
}
