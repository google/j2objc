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

import org.apache.harmony.luni.util.Util;

/*
 * CHANGES BY BOBW:
 *
 * readLine() commented-out.
 */
 
/**
 * Wraps an existing {@link InputStream} and reads typed data from it.
 * Typically, this stream has been written by a DataOutputStream. Types that can
 * be read include byte, 16-bit short, 32-bit int, 32-bit float, 64-bit long,
 * 64-bit double, byte strings, and strings encoded in
 * {@link DataInput modified UTF-8}.
 * 
 * @see DataOutputStream
 */
public class DataInputStream extends FilterInputStream implements DataInput {

    byte[] buff;

    /**
     * Constructs a new DataInputStream on the InputStream {@code in}. All
     * reads are then filtered through this stream. Note that data read by this
     * stream is not in a human readable format and was most likely created by a
     * DataOutputStream.
     * 
     * @param in
     *            the source InputStream the filter reads from.
     * @see DataOutputStream
     * @see RandomAccessFile
     */
    public DataInputStream(InputStream in) {
        super(in);
        buff = new byte[8];
    }

    /**
     * Reads bytes from this stream into the byte array {@code buffer}. Returns
     * the number of bytes that have been read.
     * 
     * @param buffer
     *            the buffer to read bytes into.
     * @return the number of bytes that have been read or -1 if the end of the
     *         stream has been reached.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @see DataOutput#write(byte[])
     * @see DataOutput#write(byte[], int, int)
     */
    @Override
    public final int read(byte[] buffer) throws IOException {
        return in.read(buffer, 0, buffer.length);
    }

    /**
     * Reads at most {@code length} bytes from this stream and stores them in
     * the byte array {@code buffer} starting at {@code offset}. Returns the
     * number of bytes that have been read or -1 if no bytes have been read and
     * the end of the stream has been reached.
     * 
     * @param buffer
     *            the byte array in which to store the bytes read.
     * @param offset
     *            the initial position in {@code buffer} to store the bytes
     *            read from this stream.
     * @param length
     *            the maximum number of bytes to store in {@code buffer}.
     * @return the number of bytes that have been read or -1 if the end of the
     *         stream has been reached.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @see DataOutput#write(byte[])
     * @see DataOutput#write(byte[], int, int)
     */
    @Override
    public final int read(byte[] buffer, int offset, int length)
            throws IOException {
        return in.read(buffer, offset, length);
    }

    /**
     * Reads a boolean from this stream.
     * 
     * @return the next boolean value from the source stream.
     * @throws EOFException
     *             if the end of the filtered stream is reached before one byte
     *             has been read.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @see DataOutput#writeBoolean(boolean)
     */
    public final boolean readBoolean() throws IOException {
        int temp = in.read();
        if (temp < 0) {
            throw new EOFException();
        }
        return temp != 0;
    }

    /**
     * Reads an 8-bit byte value from this stream.
     * 
     * @return the next byte value from the source stream.
     * @throws EOFException
     *             if the end of the filtered stream is reached before one byte
     *             has been read.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @see DataOutput#writeByte(int)
     */
    public final byte readByte() throws IOException {
        int temp = in.read();
        if (temp < 0) {
            throw new EOFException();
        }
        return (byte) temp;
    }

    /**
     * Reads a 16-bit character value from this stream.
     * 
     * @return the next char value from the source stream.
     * @throws EOFException
     *             if the end of the filtered stream is reached before two bytes
     *             have been read.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @see DataOutput#writeChar(int)
     */
    private int readToBuff(int count) throws IOException {
        int offset = 0;

        while(offset < count) {
            int bytesRead = in.read(buff, offset, count - offset);
            if(bytesRead == -1) return bytesRead;
            offset += bytesRead;
        } 
        return offset;
    }

    public final char readChar() throws IOException {
        if (readToBuff(2) < 0){
            throw new EOFException();
        }
        return (char) (((buff[0] & 0xff) << 8) | (buff[1] & 0xff));

    }

    /**
     * Reads a 64-bit double value from this stream.
     * 
     * @return the next double value from the source stream.
     * @throws EOFException
     *             if the end of the filtered stream is reached before eight
     *             bytes have been read.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @see DataOutput#writeDouble(double)
     */
    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    /**
     * Reads a 32-bit float value from this stream.
     * 
     * @return the next float value from the source stream.
     * @throws EOFException
     *             if the end of the filtered stream is reached before four
     *             bytes have been read.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @see DataOutput#writeFloat(float)
     */
    public final float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    /**
     * Reads bytes from this stream into the byte array {@code buffer}. This
     * method will block until {@code buffer.length} number of bytes have been
     * read.
     * 
     * @param buffer
     *            to read bytes into.
     * @throws EOFException
     *             if the end of the source stream is reached before enough
     *             bytes have been read.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @see DataOutput#write(byte[])
     * @see DataOutput#write(byte[], int, int)
     */
    public final void readFully(byte[] buffer) throws IOException {
        readFully(buffer, 0, buffer.length);
    }

    /**
     * Reads bytes from this stream and stores them in the byte array {@code
     * buffer} starting at the position {@code offset}. This method blocks until
     * {@code length} bytes have been read. If {@code length} is zero, then this
     * method returns without reading any bytes.
     * 
     * @param buffer
     *            the byte array into which the data is read.
     * @param offset
     *            the offset in {@code buffer} from where to store the bytes
     *            read.
     * @param length
     *            the maximum number of bytes to read.
     * @throws EOFException
     *             if the end of the source stream is reached before enough
     *             bytes have been read.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code length < 0}, or if {@code
     *             offset + length} is greater than the size of {@code buffer}.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @throws NullPointerException
     *             if {@code buffer} or the source stream are null.
     * @see java.io.DataInput#readFully(byte[], int, int)
     */
    public final void readFully(byte[] buffer, int offset, int length)
            throws IOException {
        if (length < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (length == 0) {
            return;
        }
        if (in == null) {
            throw new NullPointerException("InputStream is null");
        }
        if (buffer == null) {
            throw new NullPointerException("buffer is null");
        }
        if (offset < 0 || offset > buffer.length - length) {
            throw new IndexOutOfBoundsException();
        }
        while (length > 0) {
            int result = in.read(buffer, offset, length);
            if (result < 0) {
                throw new EOFException();
            }
            offset += result;
            length -= result;
        }
    }

    /**
     * Reads a 32-bit integer value from this stream.
     * 
     * @return the next int value from the source stream.
     * @throws EOFException
     *             if the end of the filtered stream is reached before four
     *             bytes have been read.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @see DataOutput#writeInt(int)
     */
    public final int readInt() throws IOException {
        if (readToBuff(4) < 0){
            throw new EOFException();
        }
        return ((buff[0] & 0xff) << 24) | ((buff[1] & 0xff) << 16) |
            ((buff[2] & 0xff) << 8) | (buff[3] & 0xff);
    }

    /**
     * Returns a string that contains the next line of text available from the
     * source stream. A line is represented by zero or more characters followed
     * by {@code '\n'}, {@code '\r'}, {@code "\r\n"} or the end of the stream.
     * The string does not include the newline sequence.
     * 
     * @return the contents of the line or {@code null} if no characters were
     *         read before the end of the source stream has been reached.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @deprecated Use {@link BufferedReader}
     */
    //@Deprecated
    public final String readLine() throws IOException {
        StringBuilder line = new StringBuilder(80); // Typical line length
        boolean foundTerminator = false;
        while (true) {
            int nextByte = in.read();
            switch (nextByte) {
                case -1:
                    if (line.length() == 0 && !foundTerminator) {
                        return null;
                    }
                    return line.toString();
                case (byte) '\r':
                    if (foundTerminator) {
                        ((PushbackInputStream) in).unread(nextByte);
                        return line.toString();
                    }
                    foundTerminator = true;
                    // Have to be able to peek ahead one byte
                    if (!(in.getClass() == PushbackInputStream.class)) {
                        in = new PushbackInputStream(in);
                    }
                    break;
                case (byte) '\n':
                    return line.toString();
                default:
                    if (foundTerminator) {
                        ((PushbackInputStream) in).unread(nextByte);
                        return line.toString();
                    }
                    line.append((char) nextByte);
            }
        }
    }

    /**
     * Reads a 64-bit long value from this stream.
     * 
     * @return the next long value from the source stream.
     * @throws EOFException
     *             if the end of the filtered stream is reached before eight
     *             bytes have been read.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @see DataOutput#writeLong(long)
     */
    public final long readLong() throws IOException {
        if (readToBuff(8) < 0){
            throw new EOFException();
        }
        int i1 = ((buff[0] & 0xff) << 24) | ((buff[1] & 0xff) << 16) |
            ((buff[2] & 0xff) << 8) | (buff[3] & 0xff);
        int i2 = ((buff[4] & 0xff) << 24) | ((buff[5] & 0xff) << 16) |
            ((buff[6] & 0xff) << 8) | (buff[7] & 0xff);

        return ((i1 & 0xffffffffL) << 32) | (i2 & 0xffffffffL);
    }

    /**
     * Reads a 16-bit short value from this stream.
     * 
     * @return the next short value from the source stream.
     * @throws EOFException
     *             if the end of the filtered stream is reached before two bytes
     *             have been read.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @see DataOutput#writeShort(int)
     */
    public final short readShort() throws IOException {
        if (readToBuff(2) < 0){
            throw new EOFException();
        }
        return (short) (((buff[0] & 0xff) << 8) | (buff[1] & 0xff));
    }

    /**
     * Reads an unsigned 8-bit byte value from this stream and returns it as an
     * int.
     * 
     * @return the next unsigned byte value from the source stream.
     * @throws EOFException
     *             if the end of the filtered stream has been reached before one
     *             byte has been read.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @see DataOutput#writeByte(int)
     */
    public final int readUnsignedByte() throws IOException {
        int temp = in.read();
        if (temp < 0) {
            throw new EOFException();
        }
        return temp;
    }

    /**
     * Reads a 16-bit unsigned short value from this stream and returns it as an
     * int.
     * 
     * @return the next unsigned short value from the source stream.
     * @throws EOFException
     *             if the end of the filtered stream is reached before two bytes
     *             have been read.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @see DataOutput#writeShort(int)
     */
    public final int readUnsignedShort() throws IOException {
        if (readToBuff(2) < 0){
            throw new EOFException();
        }
        return (char) (((buff[0] & 0xff) << 8) | (buff[1] & 0xff));
    }

    /**
     * Reads an string encoded in {@link DataInput modified UTF-8} from this
     * stream.
     * 
     * @return the next {@link DataInput MUTF-8} encoded string read from the
     *         source stream.
     * @throws EOFException if the end of the input is reached before the read
     *         request can be satisfied.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @see DataOutput#writeUTF(java.lang.String)
     */
    public final String readUTF() throws IOException {
        return decodeUTF(readUnsignedShort());
    }


    String decodeUTF(int utfSize) throws IOException {
        return decodeUTF(utfSize, this);
    }

    private static String decodeUTF(int utfSize, DataInput in) throws IOException {
        byte[] buf = new byte[utfSize];
        char[] out = new char[utfSize];
        in.readFully(buf, 0, utfSize);

        return Util.convertUTF8WithBuf(buf, out, 0, utfSize);
    }

    /**
     * Reads a string encoded in {@link DataInput modified UTF-8} from the
     * {@code DataInput} stream {@code in}.
     * 
     * @param in
     *            the input stream to read from.
     * @return the next {@link DataInput MUTF-8} encoded string from the source
     *         stream.
     * @throws IOException
     *             if a problem occurs while reading from this stream.
     * @see DataOutputStream#writeUTF(java.lang.String)
     */
    public static final String readUTF(DataInput in) throws IOException {
        return decodeUTF(in.readUnsignedShort(), in);
    }

    /**
     * Skips {@code count} number of bytes in this stream. Subsequent {@code
     * read()}s will not return these bytes unless {@code reset()} is used.
     * 
     * This method will not throw an {@link EOFException} if the end of the
     * input is reached before {@code count} bytes where skipped.
     *
     * @param count
     *            the number of bytes to skip.
     * @return the number of bytes actually skipped.
     * @throws IOException
     *             if a problem occurs during skipping.
     * @see #mark(int)
     * @see #reset()
     */
    public final int skipBytes(int count) throws IOException {
        int skipped = 0;
        long skip;
        while (skipped < count && (skip = in.skip(count - skipped)) != 0) {
            skipped += skip;
        }
        if (skipped < 0) {
            throw new EOFException();
        }
        return skipped;
    }
}
