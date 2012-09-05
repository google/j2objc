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

package java.nio;

import org.apache.harmony.luni.platform.Endianness;

/**
 * HeapByteBuffer, ReadWriteHeapByteBuffer and ReadOnlyHeapByteBuffer compose
 * the implementation of array based byte buffers.
 * <p>
 * HeapByteBuffer implements all the shared readonly methods and is extended by
 * the other two classes.
 * </p>
 * <p>
 * All methods are marked final for runtime performance.
 * </p>
 * 
 */
abstract class HeapByteBuffer extends ByteBuffer {

    protected final byte[] backingArray;

    protected final int offset;

    HeapByteBuffer(byte[] backingArray) {
        this(backingArray, backingArray.length, 0);
    }

    HeapByteBuffer(int capacity) {
        this(new byte[capacity], capacity, 0);
    }

    HeapByteBuffer(byte[] backingArray, int capacity, int offset) {
        super(capacity);
        this.backingArray = backingArray;
        this.offset = offset;

        if (offset + capacity > backingArray.length) {
            throw new IndexOutOfBoundsException();
        }
    }

    /*
     * Override ByteBuffer.get(byte[], int, int) to improve performance.
     * 
     * (non-Javadoc)
     * 
     * @see java.nio.ByteBuffer#get(byte[], int, int)
     */
    @Override
    public final ByteBuffer get(byte[] dest, int off, int len) {
        int length = dest.length;
        if (off < 0 || len < 0 || (long) off + (long) len > length) {
            throw new IndexOutOfBoundsException();
        }
        if (len > remaining()) {
            throw new BufferUnderflowException();
        }
        System.arraycopy(backingArray, offset + position, dest, off, len);
        position += len;
        return this;
    }

    @Override
    public final byte get() {
        if (position == limit) {
            throw new BufferUnderflowException();
        }
        return backingArray[offset + position++];
    }

    @Override
    public final byte get(int index) {
        if (index < 0 || index >= limit) {
            throw new IndexOutOfBoundsException();
        }
        return backingArray[offset + index];
    }

    @Override
    public final double getDouble() {
        return Double.longBitsToDouble(getLong());
    }

    @Override
    public final double getDouble(int index) {
        return Double.longBitsToDouble(getLong(index));
    }

    @Override
    public final float getFloat() {
        return Float.intBitsToFloat(getInt());
    }

    @Override
    public final float getFloat(int index) {
        return Float.intBitsToFloat(getInt(index));
    }

    @Override
    public final int getInt() {
        int newPosition = position + 4;
        if (newPosition > limit) {
            throw new BufferUnderflowException();
        }
        int result = loadInt(position);
        position = newPosition;
        return result;
    }

    @Override
    public final int getInt(int index) {
        if (index < 0 || index + 4 > limit) {
            throw new IndexOutOfBoundsException();
        }
        return loadInt(index);
    }

    @Override
    public final long getLong() {
        int newPosition = position + 8;
        if (newPosition > limit) {
            throw new BufferUnderflowException();
        }
        long result = loadLong(position);
        position = newPosition;
        return result;
    }

    @Override
    public final long getLong(int index) {
        if (index < 0 || index + 8 > limit) {
            throw new IndexOutOfBoundsException();
        }
        return loadLong(index);
    }

    @Override
    public final short getShort() {
        int newPosition = position + 2;
        if (newPosition > limit) {
            throw new BufferUnderflowException();
        }
        short result = loadShort(position);
        position = newPosition;
        return result;
    }

    @Override
    public final short getShort(int index) {
        if (index < 0 || index + 2 > limit) {
            throw new IndexOutOfBoundsException();
        }
        return loadShort(index);
    }

    @Override
    public final boolean isDirect() {
        return false;
    }

    protected final int loadInt(int index) {
        int baseOffset = offset + index;
        int bytes = 0;
        if (order == Endianness.BIG_ENDIAN) {
            for (int i = 0; i < 4; i++) {
                bytes = bytes << 8;
                bytes = bytes | (backingArray[baseOffset + i] & 0xFF);
            }
        } else {
            for (int i = 3; i >= 0; i--) {
                bytes = bytes << 8;
                bytes = bytes | (backingArray[baseOffset + i] & 0xFF);
            }
        }
        return bytes;
    }

    protected final long loadLong(int index) {
        int baseOffset = offset + index;
        long bytes = 0;
        if (order == Endianness.BIG_ENDIAN) {
            for (int i = 0; i < 8; i++) {
                bytes = bytes << 8;
                bytes = bytes | (backingArray[baseOffset + i] & 0xFF);
            }
        } else {
            for (int i = 7; i >= 0; i--) {
                bytes = bytes << 8;
                bytes = bytes | (backingArray[baseOffset + i] & 0xFF);
            }
        }
        return bytes;
    }

    protected final short loadShort(int index) {
        int baseOffset = offset + index;
        short bytes = 0;
        if (order == Endianness.BIG_ENDIAN) {
            bytes = (short) (backingArray[baseOffset] << 8);
            bytes |= (backingArray[baseOffset + 1] & 0xFF);
        } else {
            bytes = (short) (backingArray[baseOffset + 1] << 8);
            bytes |= (backingArray[baseOffset] & 0xFF);
        }
        return bytes;
    }

    protected final void store(int index, int value) {
        int baseOffset = offset + index;
        if (order == Endianness.BIG_ENDIAN) {
            for (int i = 3; i >= 0; i--) {
                backingArray[baseOffset + i] = (byte) (value & 0xFF);
                value = value >> 8;
            }
        } else {
            for (int i = 0; i <= 3; i++) {
                backingArray[baseOffset + i] = (byte) (value & 0xFF);
                value = value >> 8;
            }
        }
    }

    protected final void store(int index, long value) {
        int baseOffset = offset + index;
        if (order == Endianness.BIG_ENDIAN) {
            for (int i = 7; i >= 0; i--) {
                backingArray[baseOffset + i] = (byte) (value & 0xFF);
                value = value >> 8;
            }
        } else {
            for (int i = 0; i <= 7; i++) {
                backingArray[baseOffset + i] = (byte) (value & 0xFF);
                value = value >> 8;
            }
        }
    }

    protected final void store(int index, short value) {
        int baseOffset = offset + index;
        if (order == Endianness.BIG_ENDIAN) {
            backingArray[baseOffset] = (byte) ((value >> 8) & 0xFF);
            backingArray[baseOffset + 1] = (byte) (value & 0xFF);
        } else {
            backingArray[baseOffset + 1] = (byte) ((value >> 8) & 0xFF);
            backingArray[baseOffset] = (byte) (value & 0xFF);
        }
    }

    @Override
    public final char getChar() {
        return (char) getShort();
    }

    @Override
    public final char getChar(int index) {
        return (char) getShort(index);
    }

    @Override
    public final ByteBuffer putChar(char value) {
        return putShort((short) value);
    }

    @Override
    public final ByteBuffer putChar(int index, char value) {
        return putShort(index, (short) value);
    }
}
