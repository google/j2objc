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

/*
 * CHANGES BY BOBW:
 *
 * readLine() commented-out.
 */

package java.io;

/**
 * Defines an interface for classes that are able to read typed data from some
 * source. Typically, this data has been written by a class which implements
 * {@link DataOutput}. Types that can be read include byte, 16-bit short, 32-bit
 * int, 32-bit float, 64-bit long, 64-bit double, byte strings, and MUTF-8
 * strings.
 *
 * <h3>MUTF-8 (Modified UTF-8) Encoding</h3>
 * <p>
 * When encoding strings as UTF, implementations of {@code DataInput} and
 * {@code DataOutput} use a slightly modified form of UTF-8, hereafter referred
 * to as MUTF-8. This form is identical to standard UTF-8, except:
 * <ul>
 * <li>Only the one-, two-, and three-byte encodings are used.</li>
 * <li>Code points in the range <code>U+10000</code> &hellip;
 * <code>U+10ffff</code> are encoded as a surrogate pair, each of which is
 * represented as a three-byte encoded value.</li>
 * <li>The code point <code>U+0000</code> is encoded in two-byte form.</li>
 * </ul>
 * <p>
 * Please refer to <a href="http://unicode.org">The Unicode Standard</a> for
 * further information about character encoding. MUTF-8 is actually closer to
 * the (relatively less well-known) encoding <a
 * href="http://www.unicode.org/reports/tr26/">CESU-8</a> than to UTF-8 per se.
 *
 * @see DataInputStream
 * @see RandomAccessFile
 */
public interface DataInput {
    /**
     * Reads a boolean.
     * 
     * @return the next boolean value.
     * @throws EOFException if the end of the input is reached before the read
     *         request can be satisfied.
     * @throws IOException
     *             if an I/O error occurs while reading.
     * @see DataOutput#writeBoolean(boolean)
     */
    public abstract boolean readBoolean() throws IOException;

    /**
     * Reads an 8-bit byte value.
     * 
     * @return the next byte value.
     * @throws EOFException if the end of the input is reached before the read
     *         request can be satisfied.
     * @throws IOException
     *             if an I/O error occurs while reading.
     * @see DataOutput#writeByte(int)
     */
    public abstract byte readByte() throws IOException;

    /**
     * Reads a 16-bit character value.
     * 
     * @return the next char value.
     * @throws EOFException if the end of the input is reached before the read
     *         request can be satisfied.
     * @throws IOException
     *             if an I/O error occurs while reading.
     * @see DataOutput#writeChar(int)
     */
    public abstract char readChar() throws IOException;

    /**
     * Reads a 64-bit double value.
     * 
     * @return the next double value.
     * @throws EOFException if the end of the input is reached before the read
     *         request can be satisfied.
     * @throws IOException
     *             if an I/O error occurs while reading.
     * @see DataOutput#writeDouble(double)
     */
    public abstract double readDouble() throws IOException;

    /**
     * Reads a 32-bit float value.
     * 
     * @return the next float value.
     * @throws EOFException if the end of the input is reached before the read
     *         request can be satisfied.
     * @throws IOException
     *             if an I/O error occurs while reading.
     * @see DataOutput#writeFloat(float)
     */
    public abstract float readFloat() throws IOException;

    /**
     * Reads bytes into the byte array {@code buffer}. This method will block
     * until {@code buffer.length} number of bytes have been read.
     * 
     * @param buffer
     *            the buffer to read bytes into.
     * @throws EOFException if the end of the input is reached before the read
     *         request can be satisfied.
     * @throws IOException
     *             if an I/O error occurs while reading.
     * @see DataOutput#write(byte[])
     * @see DataOutput#write(byte[], int, int)
     */
    public abstract void readFully(byte[] buffer) throws IOException;

    /**
     * Reads bytes and stores them in the byte array {@code buffer} starting at
     * offset {@code offset}. This method blocks until {@code count} number of
     * bytes have been read.
     * 
     * @param buffer
     *            the byte array in which to store the bytes read.
     * @param offset
     *            the initial position in {@code buffer} to store the bytes
     *            read.
     * @param count
     *            the maximum number of bytes to store in {@code buffer}.
     * @throws EOFException if the end of the input is reached before the read
     *         request can be satisfied.
     * @throws IOException
     *             if an I/O error occurs while reading.
     * @see DataOutput#write(byte[])
     * @see DataOutput#write(byte[], int, int)
     */
    public abstract void readFully(byte[] buffer, int offset, int count)
            throws IOException;

    /**
     * Reads a 32-bit integer value.
     * 
     * @return the next int value.
     * @throws EOFException if the end of the input is reached before the read
     *         request can be satisfied.
     * @throws IOException
     *             if an I/O error occurs while reading.
     * @see DataOutput#writeInt(int)
     */
    public abstract int readInt() throws IOException;

    /**
     * Returns a string containing the next line of text available from this
     * stream. A line is made of zero or more characters followed by {@code
     * '\n'}, {@code '\r'}, {@code "\r\n"} or the end of the stream. The string
     * does not include the newline sequence.
     * 
     * @return the contents of the line or null if no characters have been read
     *         before the end of the stream.
     * @throws EOFException if the end of the input is reached before the read
     *         request can be satisfied.
     * @throws IOException
     *             if an I/O error occurs while reading.
     */
    public abstract String readLine() throws IOException;

    /**
     * Reads a 64-bit long value.
     * 
     * @return the next long value.
     * @throws EOFException if the end of the input is reached before the read
     *         request can be satisfied.
     * @throws IOException
     *             if an I/O error occurs while reading.
     * @see DataOutput#writeLong(long)
     */
    public abstract long readLong() throws IOException;

    /**
     * Reads a 16-bit short value.
     * 
     * @return the next short value.
     * @throws EOFException if the end of the input is reached before the read
     *         request can be satisfied.
     * @throws IOException
     *             if an I/O error occurs while reading.
     * @see DataOutput#writeShort(int)
     */
    public abstract short readShort() throws IOException;

    /**
     * Reads an unsigned 8-bit byte value and returns it as an int.
     * 
     * @return the next unsigned byte value.
     * @throws EOFException if the end of the input is reached before the read
     *         request can be satisfied.
     * @throws IOException
     *             if an I/O error occurs while reading.
     * @see DataOutput#writeByte(int)
     */
    public abstract int readUnsignedByte() throws IOException;

    /**
     * Reads a 16-bit unsigned short value and returns it as an int.
     * 
     * @return the next unsigned short value.
     * @throws EOFException if the end of the input is reached before the read
     *         request can be satisfied.
     * @throws IOException
     *             if an I/O error occurs while reading.
     * @see DataOutput#writeShort(int)
     */
    public abstract int readUnsignedShort() throws IOException;

    /**
     * Reads a string encoded with {@link DataInput modified UTF-8}.
     * 
     * @return the next string encoded with {@link DataInput modified UTF-8}.
     * @throws EOFException if the end of the input is reached before the read
     *         request can be satisfied.
     * @throws IOException
     *             if an I/O error occurs while reading.
     * @see DataOutput#writeUTF(java.lang.String)
     */
    public abstract String readUTF() throws IOException;

    /**
     * Skips {@code count} number of bytes. This method will not throw an
     * {@link EOFException} if the end of the input is reached before
     * {@code count} bytes where skipped.
     * 
     * @param count
     *            the number of bytes to skip.
     * @return the number of bytes actually skipped.
     * @throws IOException
     *             if a problem occurs during skipping.
     */
    public abstract int skipBytes(int count) throws IOException;
}
