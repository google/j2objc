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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.ByteOrder;
import java.util.Arrays;
import libcore.io.Memory;
import libcore.io.Streams;

/**
 * The {@code GZIPInputStream} class is used to read data stored in the GZIP
 * format, reading and decompressing GZIP data from the underlying stream into
 * its buffer.
 *
 * <h3>Example</h3>
 * <p>Using {@code GZIPInputStream} is easier than {@link ZipInputStream}
 * because GZIP is only for compression, and is not a container for multiple files.
 * This code decompresses the data from a GZIP stream, similar to the {@code gunzip(1)} utility.
 * <pre>
 * InputStream is = ...
 * GZIPInputStream zis = new GZIPInputStream(new BufferedInputStream(is));
 * try {
 *     // Reading from 'zis' gets you the uncompressed bytes...
 *     processStream(zis);
 * } finally {
 *     zis.close();
 * }
 * </pre>
 *
 * <p>Note that this class ignores all remaining data at the end of the last
 * GZIP member.
 */
public class GZIPInputStream extends InflaterInputStream {
    private static final int FCOMMENT = 16;

    private static final int FEXTRA = 4;

    private static final int FHCRC = 2;

    private static final int FNAME = 8;

    private static final int GZIP_TRAILER_SIZE = 8;

    /**
     * The magic header for the GZIP format.
     */
    public static final int GZIP_MAGIC = 0x8b1f;

    /**
     * The checksum algorithm used when handling uncompressed data.
     */
    protected CRC32 crc = new CRC32();

    /**
     * Indicates the end of the input stream.
     */
    protected boolean eos = false;

    /**
     * Construct a {@code GZIPInputStream} to read from GZIP data from the
     * underlying stream.
     *
     * @param is
     *            the {@code InputStream} to read data from.
     * @throws IOException
     *             if an {@code IOException} occurs.
     */
    public GZIPInputStream(InputStream is) throws IOException {
        this(is, BUF_SIZE);
    }

    /**
     * Construct a {@code GZIPInputStream} to read from GZIP data from the
     * underlying stream. Set the internal buffer size to {@code size}.
     *
     * @param is
     *            the {@code InputStream} to read data from.
     * @param size
     *            the internal read buffer size.
     * @throws IOException
     *             if an {@code IOException} occurs.
     */
    public GZIPInputStream(InputStream is, int size) throws IOException {
        super(is, new Inflater(true), size);

        try {
            byte[] header = readHeader(is);
            final short magic = Memory.peekShort(header, 0, ByteOrder.LITTLE_ENDIAN);
            if (magic != (short) GZIP_MAGIC) {
                throw new IOException(String.format("unknown format (magic number %x)", magic));
            }

            parseGzipHeader(is, header, crc, buf);
        } catch (IOException e) {
            close(); // release the inflater
            throw e;
        }
    }

    /**
     * Closes this stream and any underlying streams.
     */
    @Override
    public void close() throws IOException {
        eos = true;
        super.close();
    }

    @Override
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        if (closed) {
            throw new IOException("Stream is closed");
        }
        if (eos) {
            return -1;
        }
        Arrays.checkOffsetAndCount(buffer.length, byteOffset, byteCount);

        int bytesRead;
        try {
            bytesRead = super.read(buffer, byteOffset, byteCount);
        } finally {
            eos = eof; // update eos after every read(), even when it throws
        }

        if (bytesRead != -1) {
            crc.update(buffer, byteOffset, bytesRead);
        }

        if (eos) {
            verifyCrc();
            eos = maybeReadNextMember();
            if (!eos) {
                crc.reset();
                inf.reset();
                eof = false;
                len = 0;
            }
        }

        return bytesRead;
    }

    private boolean maybeReadNextMember() throws IOException {
        // If we have any unconsumed data in the inflater buffer, we have to
        // scan that first. The fact that we've reached here implies we've
        // successfully consumed the GZIP trailer.
        final int remaining = inf.getRemaining() - GZIP_TRAILER_SIZE;
        if (remaining > 0) {
            // NOTE: We make sure we create a pushback stream exactly once,
            // even if the input stream contains multiple members.
            //
            // The push back stream we create must therefore be able to contain
            // (worst case) the entire buffer even though there may be fewer bytes
            // remaining when it is first created.
            if (!(in instanceof PushbackInputStream)) {
                in = new PushbackInputStream(in, buf.length);
            }
            ((PushbackInputStream) in).unread(buf,
                    inf.getCurrentOffset() + GZIP_TRAILER_SIZE, remaining);
        }

        final byte[] buffer;
        try {
            buffer = readHeader(in);
        } catch (EOFException eof) {
            // We've reached the end of the stream and there are no more members
            // to read. Note that we might also hit this if there are fewer than
            // GZIP_HEADER_LENGTH bytes at the end of a member. We don't care
            // because we're specified to ignore all data at the end of the last
            // gzip record.
            return true;
        }

        final short magic = Memory.peekShort(buffer, 0, ByteOrder.LITTLE_ENDIAN);
        if (magic != (short) GZIP_MAGIC) {
            // Don't throw here because we've already read one valid member
            // from this stream.
            return true;
        }

        // We've encountered the gzip magic number, so we assume there's another
        // member in the stream.
        parseGzipHeader(in, buffer, crc, buf);
        return false;
    }

    private static byte[] readHeader(InputStream in) throws IOException {
        byte[] header = new byte[10];
        Streams.readFully(in, header, 0, header.length);
        return header;
    }

    private static void parseGzipHeader(InputStream in, byte[] header,
            CRC32 crc, byte[] scratch) throws IOException {
        final byte flags = header[3];
        final boolean hcrc = (flags & FHCRC) != 0;
        if (hcrc) {
            crc.update(header, 0, header.length);
        }
        if ((flags & FEXTRA) != 0) {
            Streams.readFully(in, header, 0, 2);
            if (hcrc) {
                crc.update(header, 0, 2);
            }
            int length = Memory.peekShort(header, 0, ByteOrder.LITTLE_ENDIAN) & 0xffff;
            while (length > 0) {
                int max = length > scratch.length ? scratch.length : length;
                int result = in.read(scratch, 0, max);
                if (result == -1) {
                    throw new EOFException();
                }
                if (hcrc) {
                    crc.update(scratch, 0, result);
                }
                length -= result;
            }
        }
        if ((flags & FNAME) != 0) {
            readZeroTerminated(in, crc, hcrc);
        }
        if ((flags & FCOMMENT) != 0) {
            readZeroTerminated(in, crc, hcrc);
        }
        if (hcrc) {
            Streams.readFully(in, header, 0, 2);
            short crc16 = Memory.peekShort(header, 0, ByteOrder.LITTLE_ENDIAN);
            if ((short) crc.getValue() != crc16) {
                throw new IOException("CRC mismatch");
            }
            crc.reset();
        }
    }

    private void verifyCrc() throws IOException {
        // Get non-compressed bytes read by fill
        int size = inf.getRemaining();
        final int trailerSize = 8; // crc (4 bytes) + total out (4 bytes)
        byte[] b = new byte[trailerSize];
        int copySize = (size > trailerSize) ? trailerSize : size;

        System.arraycopy(buf, len - size, b, 0, copySize);
        Streams.readFully(in, b, copySize, trailerSize - copySize);

        if (Memory.peekInt(b, 0, ByteOrder.LITTLE_ENDIAN) != (int) crc.getValue()) {
            throw new IOException("CRC mismatch");
        }
        if (Memory.peekInt(b, 4, ByteOrder.LITTLE_ENDIAN) != inf.getTotalOut()) {
            throw new IOException("Size mismatch");
        }
    }

    private static void readZeroTerminated(InputStream in, CRC32 crc, boolean hcrc)
            throws IOException {
        int result;
        // TODO: Fix these single byte reads. This method is used to consume the
        // header FNAME & FCOMMENT which aren't widely used in gzip files.
        while ((result = in.read()) > 0) {
            if (hcrc) {
                crc.update(result);
            }
        }
        if (result == -1) {
            throw new EOFException();
        }
        // Add the zero
        if (hcrc) {
            crc.update(result);
        }
    }
}
