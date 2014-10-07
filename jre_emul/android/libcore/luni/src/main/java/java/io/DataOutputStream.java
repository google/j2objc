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

import java.nio.ByteOrder;
import java.nio.charset.ModifiedUtf8;
import libcore.io.Memory;
import libcore.io.SizeOf;

/**
 * Wraps an existing {@link OutputStream} and writes big-endian typed data to it.
 * Typically, this stream can be read in by DataInputStream. Types that can be
 * written include byte, 16-bit short, 32-bit int, 32-bit float, 64-bit long,
 * 64-bit double, byte strings, and {@link DataInput MUTF-8} encoded strings.
 *
 * @see DataInputStream
 */
public class DataOutputStream extends FilterOutputStream implements DataOutput {
    private final byte[] scratch = new byte[8];

    /**
     * The number of bytes written out so far.
     */
    protected int written;

    /**
     * Constructs a new {@code DataOutputStream} on the {@code OutputStream}
     * {@code out}. Note that data written by this stream is not in a human
     * readable form but can be reconstructed by using a {@link DataInputStream}
     * on the resulting output.
     *
     * @param out
     *            the target stream for writing.
     */
    public DataOutputStream(OutputStream out) {
        super(out);
    }

    /**
     * Flushes this stream to ensure all pending data is sent out to the target
     * stream. This implementation then also flushes the target stream.
     *
     * @throws IOException
     *             if an error occurs attempting to flush this stream.
     */
    @Override
    public void flush() throws IOException {
        super.flush();
    }

    /**
     * Returns the total number of bytes written to the target stream so far.
     *
     * @return the number of bytes written to the target stream.
     */
    public final int size() {
        if (written < 0) {
            written = Integer.MAX_VALUE;
        }
        return written;
    }

    /**
     * Writes {@code count} bytes from the byte array {@code buffer} starting at
     * {@code offset} to the target stream.
     *
     * @param buffer
     *            the buffer to write to the target stream.
     * @param offset
     *            the index of the first byte in {@code buffer} to write.
     * @param count
     *            the number of bytes from the {@code buffer} to write.
     * @throws IOException
     *             if an error occurs while writing to the target stream.
     * @throws NullPointerException
     *             if {@code buffer} is {@code null}.
     */
    @Override
    public void write(byte[] buffer, int offset, int count) throws IOException {
        if (buffer == null) {
            throw new NullPointerException("buffer == null");
        }
        out.write(buffer, offset, count);
        written += count;
    }

    /**
     * Writes a byte to the target stream. Only the least significant byte of
     * the integer {@code oneByte} is written.
     *
     * @param oneByte
     *            the byte to write to the target stream.
     * @throws IOException
     *             if an error occurs while writing to the target stream.
     * @see DataInputStream#readByte()
     */
    @Override
    public void write(int oneByte) throws IOException {
        out.write(oneByte);
        written++;
    }

    /**
     * Writes a boolean to the target stream.
     *
     * @param val
     *            the boolean value to write to the target stream.
     * @throws IOException
     *             if an error occurs while writing to the target stream.
     * @see DataInputStream#readBoolean()
     */
    public final void writeBoolean(boolean val) throws IOException {
        out.write(val ? 1 : 0);
        written++;
    }

    /**
     * Writes an 8-bit byte to the target stream. Only the least significant
     * byte of the integer {@code val} is written.
     *
     * @param val
     *            the byte value to write to the target stream.
     * @throws IOException
     *             if an error occurs while writing to the target stream.
     * @see DataInputStream#readByte()
     * @see DataInputStream#readUnsignedByte()
     */
    public final void writeByte(int val) throws IOException {
        out.write(val);
        written++;
    }

    public final void writeBytes(String str) throws IOException {
        if (str.length() == 0) {
            return;
        }
        byte[] bytes = new byte[str.length()];
        for (int index = 0; index < str.length(); index++) {
            bytes[index] = (byte) str.charAt(index);
        }
        out.write(bytes);
        written += bytes.length;
    }

    public final void writeChar(int val) throws IOException {
        writeShort(val);
    }

    public final void writeChars(String str) throws IOException {
        byte[] bytes = str.getBytes("UTF-16BE");
        out.write(bytes);
        written += bytes.length;
    }

    public final void writeDouble(double val) throws IOException {
        writeLong(Double.doubleToLongBits(val));
    }

    public final void writeFloat(float val) throws IOException {
        writeInt(Float.floatToIntBits(val));
    }

    public final void writeInt(int val) throws IOException {
        Memory.pokeInt(scratch, 0, val, ByteOrder.BIG_ENDIAN);
        out.write(scratch, 0, SizeOf.INT);
        written += SizeOf.INT;
    }

    public final void writeLong(long val) throws IOException {
        Memory.pokeLong(scratch, 0, val, ByteOrder.BIG_ENDIAN);
        out.write(scratch, 0, SizeOf.LONG);
        written += SizeOf.LONG;
    }

    public final void writeShort(int val) throws IOException {
        Memory.pokeShort(scratch, 0, (short) val, ByteOrder.BIG_ENDIAN);
        out.write(scratch, 0, SizeOf.SHORT);
        written += SizeOf.SHORT;
    }

    public final void writeUTF(String str) throws IOException {
        write(ModifiedUtf8.encode(str));
    }
}
