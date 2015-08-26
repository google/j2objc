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
import java.io.PushbackInputStream;
import java.nio.ByteOrder;
import java.nio.charset.ModifiedUtf8;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import libcore.io.Memory;
import libcore.io.Streams;

/**
 * Used to read (decompress) the data from zip files.
 *
 * <p>A zip file (or "archive") is a collection of (possibly) compressed files.
 * When reading from a {@code ZipInputStream}, you call {@link #getNextEntry}
 * which returns a {@link ZipEntry} of metadata corresponding to the userdata that follows.
 * When you appear to have hit the end of this stream (which is really just the end of the current
 * entry's userdata), call {@code getNextEntry} again. When it returns null,
 * there are no more entries in the input file.
 *
 * <p>Although {@code InflaterInputStream} can only read compressed zip
 * entries, this class can read non-compressed entries as well.
 *
 * <p>Use {@link ZipFile} if you need random access to entries by name, but use this class
 * if you just want to iterate over all entries.
 *
 * <h3>Example</h3>
 * <p>Using {@code ZipInputStream} is a little more complicated than {@link GZIPInputStream}
 * because zip files are containers that can contain multiple files. This code pulls all the
 * files out of a zip file, similar to the {@code unzip(1)} utility.
 * <pre>
 * InputStream is = ...
 * ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
 * try {
 *     ZipEntry ze;
 *     while ((ze = zis.getNextEntry()) != null) {
 *         ByteArrayOutputStream baos = new ByteArrayOutputStream();
 *         byte[] buffer = new byte[1024];
 *         int count;
 *         while ((count = zis.read(buffer)) != -1) {
 *             baos.write(buffer, 0, count);
 *         }
 *         String filename = ze.getName();
 *         byte[] bytes = baos.toByteArray();
 *         // do something with 'filename' and 'bytes'...
 *     }
 * } finally {
 *     zis.close();
 * }
 * </pre>
 */
public class ZipInputStream extends InflaterInputStream implements ZipConstants {
    private static final int ZIPLocalHeaderVersionNeeded = 20;

    private boolean entriesEnd = false;

    private boolean hasDD = false;

    private int entryIn = 0;

    private int inRead, lastRead = 0;

    private ZipEntry currentEntry;

    private boolean currentEntryIsZip64;

    private final byte[] hdrBuf = new byte[LOCHDR - LOCVER + 8];

    private final CRC32 crc = new CRC32();

    private byte[] stringBytesBuf = new byte[256];

    private char[] stringCharBuf = new char[256];

    /**
     * Constructs a new {@code ZipInputStream} to read zip entries from the given input stream.
     *
     * <p>UTF-8 is used to decode all strings in the file.
     */
    public ZipInputStream(InputStream stream) {
        super(new PushbackInputStream(stream, BUF_SIZE), new Inflater(true));
        if (stream == null) {
            throw new NullPointerException("stream == null");
        }
    }

    /**
     * Closes this {@code ZipInputStream}.
     *
     * @throws IOException
     *             if an {@code IOException} occurs.
     */
    @Override
    public void close() throws IOException {
        if (!closed) {
            closeEntry(); // Close the current entry
            super.close();
        }
    }

    /**
     * Closes the current zip entry and prepares to read the next entry.
     *
     * @throws IOException
     *             if an {@code IOException} occurs.
     */
    public void closeEntry() throws IOException {
        checkClosed();
        if (currentEntry == null) {
            return;
        }

        /*
         * The following code is careful to leave the ZipInputStream in a
         * consistent state, even when close() results in an exception. It does
         * so by:
         *  - pushing bytes back into the source stream
         *  - reading a data descriptor footer from the source stream
         *  - resetting fields that manage the entry being closed
         */

        // Ensure all entry bytes are read
        Exception failure = null;
        try {
            Streams.skipAll(this);
        } catch (Exception e) {
            failure = e;
        }

        int inB, out;
        if (currentEntry.compressionMethod == ZipEntry.DEFLATED) {
            inB = inf.getTotalIn();
            out = inf.getTotalOut();
        } else {
            inB = inRead;
            out = inRead;
        }
        int diff = entryIn - inB;
        // Pushback any required bytes
        if (diff != 0) {
            ((PushbackInputStream) in).unread(buf, len - diff, diff);
        }

        try {
            readAndVerifyDataDescriptor(inB, out, currentEntryIsZip64);
        } catch (Exception e) {
            if (failure == null) { // otherwise we're already going to throw
                failure = e;
            }
        }

        inf.reset();
        lastRead = inRead = entryIn = len = 0;
        crc.reset();
        currentEntry = null;

        if (failure != null) {
            if (failure instanceof IOException) {
                throw (IOException) failure;
            } else if (failure instanceof RuntimeException) {
                throw (RuntimeException) failure;
            }
            AssertionError error = new AssertionError();
            error.initCause(failure);
            throw error;
        }
    }

    private void readAndVerifyDataDescriptor(long inB, long out, boolean isZip64) throws IOException {
        if (hasDD) {
            if (isZip64) {
                // 8 additional bytes since the compressed / uncompressed size fields
                // in the extended header are 8 bytes each, instead of 4 bytes each.
                Streams.readFully(in, hdrBuf, 0, EXTHDR + 8);
            } else {
                Streams.readFully(in, hdrBuf, 0, EXTHDR);
            }

            int sig = Memory.peekInt(hdrBuf, 0, ByteOrder.LITTLE_ENDIAN);
            if (sig != (int) EXTSIG) {
                throw new ZipException(String.format("unknown format (EXTSIG=%x)", sig));
            }
            currentEntry.crc = ((long) Memory.peekInt(hdrBuf, EXTCRC, ByteOrder.LITTLE_ENDIAN)) & 0xffffffffL;

            if (isZip64) {
                currentEntry.compressedSize = Memory.peekLong(hdrBuf, EXTSIZ, ByteOrder.LITTLE_ENDIAN);
                // Note that we apply an adjustment of 4 bytes to the offset of EXTLEN to account
                // for the 8 byte size for zip64.
                currentEntry.size = Memory.peekLong(hdrBuf, EXTLEN + 4, ByteOrder.LITTLE_ENDIAN);
            } else {
                currentEntry.compressedSize = ((long) Memory.peekInt(hdrBuf, EXTSIZ, ByteOrder.LITTLE_ENDIAN)) & 0xffffffffL;
                currentEntry.size = ((long) Memory.peekInt(hdrBuf, EXTLEN, ByteOrder.LITTLE_ENDIAN)) & 0xffffffffL;
            }
        }
        if (currentEntry.crc != crc.getValue()) {
            throw new ZipException("CRC mismatch");
        }
        if (currentEntry.compressedSize != inB || currentEntry.size != out) {
            throw new ZipException("Size mismatch");
        }
    }

    /**
     * Returns the next entry from this {@code ZipInputStream} or {@code null} if
     * no more entries are present.
     *
     * @throws IOException if an {@code IOException} occurs.
     */
    public ZipEntry getNextEntry() throws IOException {
        closeEntry();
        if (entriesEnd) {
            return null;
        }

        // Read the signature to see whether there's another local file header.
        Streams.readFully(in, hdrBuf, 0, 4);
        int hdr = Memory.peekInt(hdrBuf, 0, ByteOrder.LITTLE_ENDIAN);
        if (hdr == CENSIG) {
            entriesEnd = true;
            return null;
        }
        if (hdr != LOCSIG) {
            return null;
        }

        // Read the local file header.
        Streams.readFully(in, hdrBuf, 0, (LOCHDR - LOCVER));
        int version = peekShort(0) & 0xff;
        if (version > ZIPLocalHeaderVersionNeeded) {
            throw new ZipException("Cannot read local header version " + version);
        }
        int flags = peekShort(LOCFLG - LOCVER);
        if ((flags & ZipFile.GPBF_UNSUPPORTED_MASK) != 0) {
            throw new ZipException("Invalid General Purpose Bit Flag: " + flags);
        }

        hasDD = ((flags & ZipFile.GPBF_DATA_DESCRIPTOR_FLAG) != 0);
        int ceLastModifiedTime = peekShort(LOCTIM - LOCVER);
        int ceLastModifiedDate = peekShort(LOCTIM - LOCVER + 2);
        int ceCompressionMethod = peekShort(LOCHOW - LOCVER);
        long ceCrc = 0, ceCompressedSize = 0, ceSize = -1;
        if (!hasDD) {
            ceCrc = ((long) Memory.peekInt(hdrBuf, LOCCRC - LOCVER, ByteOrder.LITTLE_ENDIAN)) & 0xffffffffL;
            ceCompressedSize = ((long) Memory.peekInt(hdrBuf, LOCSIZ - LOCVER, ByteOrder.LITTLE_ENDIAN)) & 0xffffffffL;
            ceSize = ((long) Memory.peekInt(hdrBuf, LOCLEN - LOCVER, ByteOrder.LITTLE_ENDIAN)) & 0xffffffffL;
        }
        int nameLength = peekShort(LOCNAM - LOCVER);
        if (nameLength == 0) {
            throw new ZipException("Entry is not named");
        }
        int extraLength = peekShort(LOCEXT - LOCVER);

        String name = readString(nameLength);
        currentEntry = createZipEntry(name);
        currentEntry.time = ceLastModifiedTime;
        currentEntry.modDate = ceLastModifiedDate;
        currentEntry.setMethod(ceCompressionMethod);
        if (ceSize != -1) {
            currentEntry.setCrc(ceCrc);
            currentEntry.setSize(ceSize);
            currentEntry.setCompressedSize(ceCompressedSize);
        }
        if (extraLength > 0) {
            byte[] extraData = new byte[extraLength];
            Streams.readFully(in, extraData, 0, extraLength);
            currentEntry.setExtra(extraData);
            currentEntryIsZip64 = Zip64.parseZip64ExtendedInfo(currentEntry, false /* from central directory */);
        } else {
            currentEntryIsZip64 = false;
        }

        return currentEntry;
    }

    /**
     * Reads bytes from the current stream position returning the string representation.
     */
    private String readString(int byteLength) throws IOException {
        if (byteLength > stringBytesBuf.length) {
            stringBytesBuf = new byte[byteLength];
        }
        Streams.readFully(in, stringBytesBuf, 0, byteLength);
        // The number of chars will always be less than or equal to the number of bytes. It's
        // fine if this buffer is too long.
        if (byteLength > stringCharBuf.length) {
            stringCharBuf = new char[byteLength];
        }
        return ModifiedUtf8.decode(stringBytesBuf, stringCharBuf, 0, byteLength);
    }

    private int peekShort(int offset) {
        return Memory.peekShort(hdrBuf, offset, ByteOrder.LITTLE_ENDIAN) & 0xffff;
    }

    /**
     * Reads up to {@code byteCount} uncompressed bytes into the buffer
     * starting at {@code byteOffset}. Returns the number of bytes actually read, or -1.
     */
    @Override
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        checkClosed();
        Arrays.checkOffsetAndCount(buffer.length, byteOffset, byteCount);

        if (inf.finished() || currentEntry == null) {
            return -1;
        }

        if (currentEntry.compressionMethod == ZipEntry.STORED) {
            int csize = (int) currentEntry.size;
            if (inRead >= csize) {
                return -1;
            }
            if (lastRead >= len) {
                lastRead = 0;
                if ((len = in.read(buf)) == -1) {
                    eof = true;
                    return -1;
                }
                entryIn += len;
            }
            int toRead = byteCount > (len - lastRead) ? len - lastRead : byteCount;
            if ((csize - inRead) < toRead) {
                toRead = csize - inRead;
            }
            System.arraycopy(buf, lastRead, buffer, byteOffset, toRead);
            lastRead += toRead;
            inRead += toRead;
            crc.update(buffer, byteOffset, toRead);
            return toRead;
        }
        if (inf.needsInput()) {
            fill();
            if (len > 0) {
                entryIn += len;
            }
        }
        int read;
        try {
            read = inf.inflate(buffer, byteOffset, byteCount);
        } catch (DataFormatException e) {
            throw new ZipException(e.getMessage());
        }
        if (read == 0 && inf.finished()) {
            return -1;
        }
        crc.update(buffer, byteOffset, read);
        return read;
    }

    @Override
    public int available() throws IOException {
        checkClosed();
        // The InflaterInputStream contract says we must only return 0 or 1.
        return (currentEntry == null || inRead < currentEntry.size) ? 1 : 0;
    }

    /**
     * creates a {@link ZipEntry } with the given name.
     *
     * @param name
     *            the name of the entry.
     * @return the created {@code ZipEntry}.
     */
    protected ZipEntry createZipEntry(String name) {
        return new ZipEntry(name);
    }

    private void checkClosed() throws IOException {
        if (closed) {
            throw new IOException("Stream is closed");
        }
    }
}
