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

/**
 * Defines an interface for classes that are able to write big-endian typed data to some
 * target. Typically, this data can be read in by a class which implements
 * DataInput. Types that can be written include byte, 16-bit short, 32-bit int,
 * 32-bit float, 64-bit long, 64-bit double, byte strings, and {@link DataInput
 * MUTF-8} encoded strings.
 *
 * @see DataOutputStream
 * @see RandomAccessFile
 */
public interface DataOutput {

    /**
     * Writes the entire contents of the byte array {@code buffer} to this
     * stream.
     *
     * @param buffer
     *            the buffer to write.
     * @throws IOException
     *             if an I/O error occurs while writing.
     */
    public abstract void write(byte[] buffer) throws IOException;

    /**
     * Writes {@code count} bytes from the byte array {@code buffer} starting at
     * offset {@code index}.
     *
     * @param buffer
     *            the buffer to write.
     * @param offset
     *            the index of the first byte in {@code buffer} to write.
     * @param count
     *            the number of bytes from the {@code buffer} to write.
     * @throws IOException
     *             if an I/O error occurs while writing.
     */
    public abstract void write(byte[] buffer, int offset, int count) throws IOException;

    /**
     * Writes the specified 8-bit byte.
     *
     * @param oneByte
     *            the byte to write.
     * @throws IOException
     *             if an I/O error occurs while writing.
     * @see DataInput#readByte()
     */
    public abstract void write(int oneByte) throws IOException;

    /**
     * Writes the specified boolean.
     *
     * @param val
     *            the boolean value to write.
     * @throws IOException
     *             if an I/O error occurs while writing.
     * @see DataInput#readBoolean()
     */
    public abstract void writeBoolean(boolean val) throws IOException;

    /**
     * Writes the specified 8-bit byte.
     *
     * @param val
     *            the byte value to write.
     * @throws IOException
     *             if an I/O error occurs while writing.
     * @see DataInput#readByte()
     * @see DataInput#readUnsignedByte()
     */
    public abstract void writeByte(int val) throws IOException;

    /**
     * Writes the low order 8-bit bytes from the specified string.
     *
     * @param str
     *            the string containing the bytes to write.
     * @throws IOException
     *             if an I/O error occurs while writing.
     */
    public abstract void writeBytes(String str) throws IOException;

    /**
     * Writes the specified 16-bit character in big-endian order. Only the two least significant
     * bytes of the integer {@code oneByte} are written.
     *
     * @param val
     *            the character to write.
     * @throws IOException
     *             if an I/O error occurs while writing.
     * @see DataInput#readChar()
     */
    public abstract void writeChar(int val) throws IOException;

    /**
     * Writes the 16-bit characters contained in {@code str} in big-endian order.
     *
     * @param str
     *            the string that contains the characters to write.
     * @throws IOException
     *             if an I/O error occurs while writing.
     * @see DataInput#readChar()
     */
    public abstract void writeChars(String str) throws IOException;

    /**
     * Writes the specified 64-bit double in big-endian order. The resulting output is the eight
     * bytes returned by {@link Double#doubleToLongBits(double)}.
     *
     * @param val
     *            the double to write.
     * @throws IOException
     *             if an I/O error occurs while writing.
     * @see DataInput#readDouble()
     */
    public abstract void writeDouble(double val) throws IOException;

    /**
     * Writes the specified 32-bit float in big-endian order. The resulting output is the four bytes
     * returned by {@link Float#floatToIntBits(float)}.
     *
     * @param val
     *            the float to write.
     * @throws IOException
     *             if an I/O error occurs while writing.
     * @see DataInput#readFloat()
     */
    public abstract void writeFloat(float val) throws IOException;

    /**
     * Writes the specified 32-bit int in big-endian order.
     *
     * @param val
     *            the int to write.
     * @throws IOException
     *             if an I/O error occurs while writing.
     * @see DataInput#readInt()
     */
    public abstract void writeInt(int val) throws IOException;

    /**
     * Writes the specified 64-bit long in big-endian order.
     *
     * @param val
     *            the long to write.
     * @throws IOException
     *             if an I/O error occurs while writing.
     * @see DataInput#readLong()
     */
    public abstract void writeLong(long val) throws IOException;

    /**
     * Writes the specified 16-bit short in big-endian order. Only the lower two bytes of {@code
     * val} are written.
     *
     * @param val
     *            the short to write.
     * @throws IOException
     *             if an I/O error occurs while writing.
     * @see DataInput#readShort()
     * @see DataInput#readUnsignedShort()
     */
    public abstract void writeShort(int val) throws IOException;

    /**
     * Writes the specified string encoded in {@link DataInput modified UTF-8}.
     *
     * @param str
     *            the string to write encoded in {@link DataInput modified UTF-8}.
     * @throws IOException
     *             if an I/O error occurs while writing.
     * @see DataInput#readUTF()
     */
    public abstract void writeUTF(String str) throws IOException;
}
