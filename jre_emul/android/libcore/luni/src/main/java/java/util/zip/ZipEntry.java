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
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import libcore.io.Streams;
import libcore.io.BufferIterator;
import libcore.io.HeapBufferIterator;

/**
 * An entry within a zip file.
 * An entry has attributes such as its name (which is actually a path) and the uncompressed size
 * of the corresponding data. An entry does not contain the data itself, but can be used as a key
 * with {@link ZipFile#getInputStream}. The class documentation for {@link ZipInputStream} and
 * {@link ZipOutputStream} shows how {@code ZipEntry} is used in conjunction with those two classes.
 */
public class ZipEntry implements ZipConstants, Cloneable {
    String name;
    String comment;

    long crc = -1; // Needs to be a long to distinguish -1 ("not set") from the 0xffffffff CRC32.

    long compressedSize = -1;
    long size = -1;

    int compressionMethod = -1;
    int time = -1;
    int modDate = -1;

    byte[] extra;

    int nameLength = -1;
    long localHeaderRelOffset = -1;

    /**
     * Zip entry state: Deflated.
     */
    public static final int DEFLATED = 8;

    /**
     * Zip entry state: Stored.
     */
    public static final int STORED = 0;

    /**
     * Constructs a new {@code ZipEntry} with the specified name. The name is actually a path,
     * and may contain {@code /} characters.
     *
     * @throws IllegalArgumentException
     *             if the name length is outside the range (> 0xFFFF).
     */
    public ZipEntry(String name) {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        if (name.length() > 0xFFFF) {
            throw new IllegalArgumentException("Name too long: " + name.length());
        }
        this.name = name;
    }

    /**
     * Returns the comment for this {@code ZipEntry}, or {@code null} if there is no comment.
     * If we're reading a zip file using {@code ZipInputStream}, the comment is not available.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Gets the compressed size of this {@code ZipEntry}.
     *
     * @return the compressed size, or -1 if the compressed size has not been
     *         set.
     */
    public long getCompressedSize() {
        return compressedSize;
    }

    /**
     * Gets the checksum for this {@code ZipEntry}.
     *
     * @return the checksum, or -1 if the checksum has not been set.
     */
    public long getCrc() {
        return crc;
    }

    /**
     * Gets the extra information for this {@code ZipEntry}.
     *
     * @return a byte array containing the extra information, or {@code null} if
     *         there is none.
     */
    public byte[] getExtra() {
        return extra;
    }

    /**
     * Gets the compression method for this {@code ZipEntry}.
     *
     * @return the compression method, either {@code DEFLATED}, {@code STORED}
     *         or -1 if the compression method has not been set.
     */
    public int getMethod() {
        return compressionMethod;
    }

    /**
     * Gets the name of this {@code ZipEntry}.
     *
     * @return the entry name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the uncompressed size of this {@code ZipEntry}.
     *
     * @return the uncompressed size, or {@code -1} if the size has not been
     *         set.
     */
    public long getSize() {
        return size;
    }

    /**
     * Gets the last modification time of this {@code ZipEntry}.
     *
     * @return the last modification time as the number of milliseconds since
     *         Jan. 1, 1970.
     */
    public long getTime() {
        if (time != -1) {
            GregorianCalendar cal = new GregorianCalendar();
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(1980 + ((modDate >> 9) & 0x7f), ((modDate >> 5) & 0xf) - 1,
                    modDate & 0x1f, (time >> 11) & 0x1f, (time >> 5) & 0x3f,
                    (time & 0x1f) << 1);
            return cal.getTime().getTime();
        }
        return -1;
    }

    /**
     * Determine whether or not this {@code ZipEntry} is a directory.
     *
     * @return {@code true} when this {@code ZipEntry} is a directory, {@code
     *         false} otherwise.
     */
    public boolean isDirectory() {
        return name.charAt(name.length() - 1) == '/';
    }

    /**
     * Sets the comment for this {@code ZipEntry}.
     * @throws IllegalArgumentException if the comment is >= 64 Ki UTF-8 bytes.
     */
    public void setComment(String comment) {
        if (comment == null) {
            this.comment = null;
            return;
        }

        byte[] commentBytes = comment.getBytes(StandardCharsets.UTF_8);
        if (commentBytes.length > 0xffff) {
            throw new IllegalArgumentException("Comment too long: " + commentBytes.length);
        }
        this.comment = comment;
    }

    /**
     * Sets the compressed size for this {@code ZipEntry}.
     *
     * @param value
     *            the compressed size (in bytes).
     */
    public void setCompressedSize(long value) {
        compressedSize = value;
    }

    /**
     * Sets the checksum for this {@code ZipEntry}.
     *
     * @param value
     *            the checksum for this entry.
     * @throws IllegalArgumentException
     *             if {@code value} is < 0 or > 0xFFFFFFFFL.
     */
    public void setCrc(long value) {
        if (value >= 0 && value <= 0xFFFFFFFFL) {
            crc = value;
        } else {
            throw new IllegalArgumentException("Bad CRC32: " + value);
        }
    }

    /**
     * Sets the extra information for this {@code ZipEntry}.
     *
     * @throws IllegalArgumentException if the data length >= 64 KiB.
     */
    public void setExtra(byte[] data) {
        if (data != null && data.length > 0xffff) {
            throw new IllegalArgumentException("Extra data too long: " + data.length);
        }
        extra = data;
    }

    /**
     * Sets the compression method for this entry to either {@code DEFLATED} or {@code STORED}.
     * The default is {@code DEFLATED}, which will cause the size, compressed size, and CRC to be
     * set automatically, and the entry's data to be compressed. If you switch to {@code STORED}
     * note that you'll have to set the size (or compressed size; they must be the same, but it's
     * okay to only set one) and CRC yourself because they must appear <i>before</i> the user data
     * in the resulting zip file. See {@link #setSize} and {@link #setCrc}.
     * @throws IllegalArgumentException
     *             when value is not {@code DEFLATED} or {@code STORED}.
     */
    public void setMethod(int value) {
        if (value != STORED && value != DEFLATED) {
            throw new IllegalArgumentException("Bad method: " + value);
        }
        compressionMethod = value;
    }

    /**
     * Sets the uncompressed size of this {@code ZipEntry}.
     *
     * @param value
     *            the uncompressed size for this entry.
     * @throws IllegalArgumentException
     *             if {@code value} < 0 or {@code value} > 0xFFFFFFFFL.
     */
    public void setSize(long value) {
        if (value >= 0 && value <= 0xFFFFFFFFL) {
            size = value;
        } else {
            throw new IllegalArgumentException("Bad size: " + value);
        }
    }

    /**
     * Sets the modification time of this {@code ZipEntry}.
     *
     * @param value
     *            the modification time as the number of milliseconds since Jan.
     *            1, 1970.
     */
    public void setTime(long value) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date(value));
        int year = cal.get(Calendar.YEAR);
        if (year < 1980) {
            modDate = 0x21;
            time = 0;
        } else {
            modDate = cal.get(Calendar.DATE);
            modDate = ((cal.get(Calendar.MONTH) + 1) << 5) | modDate;
            modDate = ((cal.get(Calendar.YEAR) - 1980) << 9) | modDate;
            time = cal.get(Calendar.SECOND) >> 1;
            time = (cal.get(Calendar.MINUTE) << 5) | time;
            time = (cal.get(Calendar.HOUR_OF_DAY) << 11) | time;
        }
    }

    /**
     * Returns the string representation of this {@code ZipEntry}.
     *
     * @return the string representation of this {@code ZipEntry}.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Constructs a new {@code ZipEntry} using the values obtained from {@code
     * ze}.
     *
     * @param ze
     *            the {@code ZipEntry} from which to obtain values.
     */
    public ZipEntry(ZipEntry ze) {
        name = ze.name;
        comment = ze.comment;
        time = ze.time;
        size = ze.size;
        compressedSize = ze.compressedSize;
        crc = ze.crc;
        compressionMethod = ze.compressionMethod;
        modDate = ze.modDate;
        extra = ze.extra;
        nameLength = ze.nameLength;
        localHeaderRelOffset = ze.localHeaderRelOffset;
    }

    /**
     * Returns a deep copy of this zip entry.
     */
    @Override public Object clone() {
        try {
            ZipEntry result = (ZipEntry) super.clone();
            result.extra = extra != null ? extra.clone() : null;
            return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns the hash code for this {@code ZipEntry}.
     *
     * @return the hash code of the entry.
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /*
     * Internal constructor.  Creates a new ZipEntry by reading the
     * Central Directory Entry from "in", which must be positioned at
     * the CDE signature.
     *
     * On exit, "in" will be positioned at the start of the next entry.
     */
    ZipEntry(byte[] hdrBuf, InputStream in) throws IOException {
        Streams.readFully(in, hdrBuf, 0, hdrBuf.length);

        BufferIterator it = HeapBufferIterator.iterator(hdrBuf, 0, hdrBuf.length, ByteOrder.LITTLE_ENDIAN);

        int sig = it.readInt();
        if (sig != CENSIG) {
             throw new ZipException("Central Directory Entry not found");
        }

        it.seek(8);
        int gpbf = it.readShort() & 0xffff;

        if ((gpbf & ZipFile.GPBF_UNSUPPORTED_MASK) != 0) {
            throw new ZipException("Invalid General Purpose Bit Flag: " + gpbf);
        }

        compressionMethod = it.readShort() & 0xffff;
        time = it.readShort() & 0xffff;
        modDate = it.readShort() & 0xffff;

        // These are 32-bit values in the file, but 64-bit fields in this object.
        crc = ((long) it.readInt()) & 0xffffffffL;
        compressedSize = ((long) it.readInt()) & 0xffffffffL;
        size = ((long) it.readInt()) & 0xffffffffL;

        nameLength = it.readShort() & 0xffff;
        int extraLength = it.readShort() & 0xffff;
        int commentByteCount = it.readShort() & 0xffff;

        // This is a 32-bit value in the file, but a 64-bit field in this object.
        it.seek(42);
        localHeaderRelOffset = ((long) it.readInt()) & 0xffffffffL;

        byte[] nameBytes = new byte[nameLength];
        Streams.readFully(in, nameBytes, 0, nameBytes.length);
        if (containsNulByte(nameBytes)) {
           throw new ZipException("Filename contains NUL byte: " + Arrays.toString(nameBytes));
        }
        name = new String(nameBytes, 0, nameBytes.length, StandardCharsets.UTF_8);

        if (extraLength > 0) {
            extra = new byte[extraLength];
            Streams.readFully(in, extra, 0, extraLength);
        }

        // The RI has always assumed UTF-8. (If GPBF_UTF8_FLAG isn't set, the encoding is
        // actually IBM-437.)
        if (commentByteCount > 0) {
            byte[] commentBytes = new byte[commentByteCount];
            Streams.readFully(in, commentBytes, 0, commentByteCount);
            comment = new String(commentBytes, 0, commentBytes.length, StandardCharsets.UTF_8);
        }
    }
    
    private static boolean containsNulByte(byte[] bytes) {
        for (byte b : bytes) {
            if (b == 0) {
                return true;
            }
        }
        return false;
    }
}
