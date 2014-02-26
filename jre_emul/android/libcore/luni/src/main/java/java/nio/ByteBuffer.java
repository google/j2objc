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

import java.util.Arrays;
import libcore.io.Memory;

/**
 * A buffer for bytes.
 * <p>
 * A byte buffer can be created in either one of the following ways:
 * <ul>
 * <li>{@link #allocate(int) Allocate} a new byte array and create a buffer
 * based on it;</li>
 * <li>{@link #allocateDirect(int) Allocate} a memory block and create a direct
 * buffer based on it;</li>
 * <li>{@link #wrap(byte[]) Wrap} an existing byte array to create a new
 * buffer.</li>
 * </ul>
 *
 */
public abstract class ByteBuffer extends Buffer implements Comparable<ByteBuffer> {
    /**
     * The byte order of this buffer, default is {@code BIG_ENDIAN}.
     */
    ByteOrder order = ByteOrder.BIG_ENDIAN;

    /**
     * Creates a byte buffer based on a newly allocated byte array.
     *
     * @param capacity
     *            the capacity of the new buffer
     * @return the created byte buffer.
     * @throws IllegalArgumentException
     *             if {@code capacity < 0}.
     */
    public static ByteBuffer allocate(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity < 0: " + capacity);
        }
        return new ByteArrayBuffer(new byte[capacity]);
    }

    /**
     * Creates a direct byte buffer based on a newly allocated memory block.
     *
     * @param capacity
     *            the capacity of the new buffer
     * @return the created byte buffer.
     * @throws IllegalArgumentException
     *             if {@code capacity < 0}.
     */
    public static ByteBuffer allocateDirect(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity < 0: " + capacity);
        }
        return new DirectByteBuffer(MemoryBlock.allocate(capacity), capacity, 0, false, null);
    }

    /**
     * Creates a new byte buffer by wrapping the given byte array.
     * <p>
     * Calling this method has the same effect as
     * {@code wrap(array, 0, array.length)}.
     *
     * @param array
     *            the byte array which the new buffer will be based on
     * @return the created byte buffer.
     */
    public static ByteBuffer wrap(byte[] array) {
        return new ByteArrayBuffer(array);
    }

    /**
     * Creates a new byte buffer by wrapping the given byte array.
     * <p>
     * The new buffer's position will be {@code start}, limit will be
     * {@code start + byteCount}, capacity will be the length of the array.
     *
     * @param array
     *            the byte array which the new buffer will be based on.
     * @param start
     *            the start index, must not be negative and not greater than
     *            {@code array.length}.
     * @param byteCount
     *            the length, must not be negative and not greater than
     *            {@code array.length - start}.
     * @return the created byte buffer.
     * @throws IndexOutOfBoundsException
     *                if either {@code start} or {@code byteCount} is invalid.
     */
    public static ByteBuffer wrap(byte[] array, int start, int byteCount) {
        Arrays.checkOffsetAndCount(array.length, start, byteCount);
        ByteBuffer buf = new ByteArrayBuffer(array);
        buf.position = start;
        buf.limit = start + byteCount;
        return buf;
    }

    ByteBuffer(int capacity, long effectiveDirectAddress) {
        super(0, capacity, effectiveDirectAddress);
    }

    /**
     * Returns the byte array which this buffer is based on, if there is one.
     *
     * @return the byte array which this buffer is based on.
     * @throws ReadOnlyBufferException
     *                if this buffer is based on a read-only array.
     * @throws UnsupportedOperationException
     *                if this buffer is not based on an array.
     */
    @Override public final byte[] array() {
        return protectedArray();
    }

    /**
     * Returns the offset of the byte array which this buffer is based on, if
     * there is one.
     * <p>
     * The offset is the index of the array which corresponds to the zero
     * position of the buffer.
     *
     * @return the offset of the byte array which this buffer is based on.
     * @throws ReadOnlyBufferException
     *                if this buffer is based on a read-only array.
     * @throws UnsupportedOperationException
     *                if this buffer is not based on an array.
     */
    @Override public final int arrayOffset() {
        return protectedArrayOffset();
    }

    /**
     * Returns a char buffer which is based on the remaining content of this
     * byte buffer.
     * <p>
     * The new buffer's position is zero, its limit and capacity is the number
     * of remaining bytes divided by two, and its mark is not set. The new
     * buffer's read-only property and byte order are the same as this buffer's.
     * The new buffer is direct if this byte buffer is direct.
     * <p>
     * The new buffer shares its content with this buffer, which means either
     * buffer's change of content will be visible to the other. The two buffers'
     * position, limit and mark are independent.
     */
    public abstract CharBuffer asCharBuffer();

    /**
     * Returns a double buffer which is based on the remaining content of this
     * byte buffer.
     * <p>
     * The new buffer's position is zero, its limit and capacity is the number
     * of remaining bytes divided by eight, and its mark is not set. The new
     * buffer's read-only property and byte order are the same as this buffer's.
     * The new buffer is direct if this byte buffer is direct.
     * <p>
     * The new buffer shares its content with this buffer, which means either
     * buffer's change of content will be visible to the other. The two buffers'
     * position, limit and mark are independent.
     */
    public abstract DoubleBuffer asDoubleBuffer();

    /**
     * Returns a float buffer which is based on the remaining content of this
     * byte buffer.
     * <p>
     * The new buffer's position is zero, its limit and capacity is the number
     * of remaining bytes divided by four, and its mark is not set. The new
     * buffer's read-only property and byte order are the same as this buffer's.
     * The new buffer is direct if this byte buffer is direct.
     * <p>
     * The new buffer shares its content with this buffer, which means either
     * buffer's change of content will be visible to the other. The two buffers'
     * position, limit and mark are independent.
     */
    public abstract FloatBuffer asFloatBuffer();

    /**
     * Returns a int buffer which is based on the remaining content of this byte
     * buffer.
     * <p>
     * The new buffer's position is zero, its limit and capacity is the number
     * of remaining bytes divided by four, and its mark is not set. The new
     * buffer's read-only property and byte order are the same as this buffer's.
     * The new buffer is direct if this byte buffer is direct.
     * <p>
     * The new buffer shares its content with this buffer, which means either
     * buffer's change of content will be visible to the other. The two buffers'
     * position, limit and mark are independent.
     */
    public abstract IntBuffer asIntBuffer();

    /**
     * Returns a long buffer which is based on the remaining content of this
     * byte buffer.
     * <p>
     * The new buffer's position is zero, its limit and capacity is the number
     * of remaining bytes divided by eight, and its mark is not set. The new
     * buffer's read-only property and byte order are the same as this buffer's.
     * The new buffer is direct if this byte buffer is direct.
     * <p>
     * The new buffer shares its content with this buffer, which means either
     * buffer's change of content will be visible to the other. The two buffers'
     * position, limit and mark are independent.
     */
    public abstract LongBuffer asLongBuffer();

    /**
     * Returns a read-only buffer that shares its content with this buffer.
     * <p>
     * The returned buffer is guaranteed to be a new instance, even if this
     * buffer is read-only itself. The new buffer's position, limit, capacity
     * and mark are the same as this buffer.
     * <p>
     * The new buffer shares its content with this buffer, which means this
     * buffer's change of content will be visible to the new buffer. The two
     * buffer's position, limit and mark are independent.
     *
     * @return a read-only version of this buffer.
     */
    public abstract ByteBuffer asReadOnlyBuffer();

    /**
     * Returns a short buffer which is based on the remaining content of this
     * byte buffer.
     * <p>
     * The new buffer's position is zero, its limit and capacity is the number
     * of remaining bytes divided by two, and its mark is not set. The new
     * buffer's read-only property and byte order are the same as this buffer's.
     * The new buffer is direct if this byte buffer is direct.
     * <p>
     * The new buffer shares its content with this buffer, which means either
     * buffer's change of content will be visible to the other. The two buffers'
     * position, limit and mark are independent.
     */
    public abstract ShortBuffer asShortBuffer();

    /**
     * Compacts this byte buffer.
     * <p>
     * The remaining bytes will be moved to the head of the
     * buffer, starting from position zero. Then the position is set to
     * {@code remaining()}; the limit is set to capacity; the mark is
     * cleared.
     *
     * @return {@code this}
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public abstract ByteBuffer compact();

    /**
     * Compares the remaining bytes of this buffer to another byte buffer's
     * remaining bytes.
     *
     * @param otherBuffer
     *            another byte buffer.
     * @return a negative value if this is less than {@code other}; 0 if this
     *         equals to {@code other}; a positive value if this is greater
     *         than {@code other}.
     * @throws ClassCastException
     *                if {@code other} is not a byte buffer.
     */
    @Override public int compareTo(ByteBuffer otherBuffer) {
        int compareRemaining = (remaining() < otherBuffer.remaining()) ? remaining()
                : otherBuffer.remaining();
        int thisPos = position;
        int otherPos = otherBuffer.position;
        byte thisByte, otherByte;
        while (compareRemaining > 0) {
            thisByte = get(thisPos);
            otherByte = otherBuffer.get(otherPos);
            if (thisByte != otherByte) {
                return thisByte < otherByte ? -1 : 1;
            }
            thisPos++;
            otherPos++;
            compareRemaining--;
        }
        return remaining() - otherBuffer.remaining();
    }

    /**
     * Returns a duplicated buffer that shares its content with this buffer.
     * <p>
     * The duplicated buffer's position, limit, capacity and mark are the same
     * as this buffer's. The duplicated buffer's read-only property is the same
     * as this buffer's.
     *
     * <p>Note that <i>in contrast to all non-{@code byte} buffers</i>,
     * byte order is not preserved in the duplicate, and is instead set to
     * big-endian.
     *
     * <p>The new buffer shares its content with this buffer, which means either
     * buffer's change of content will be visible to the other. The two buffers'
     * position, limit and mark are independent.
     */
    public abstract ByteBuffer duplicate();

    /**
     * Checks whether this byte buffer is equal to another object.
     * <p>
     * If {@code other} is not a byte buffer then {@code false} is returned. Two
     * byte buffers are equal if and only if their remaining bytes are exactly
     * the same. Position, limit, capacity and mark are not considered.
     *
     * @param other
     *            the object to compare with this byte buffer.
     * @return {@code true} if this byte buffer is equal to {@code other},
     *         {@code false} otherwise.
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ByteBuffer)) {
            return false;
        }
        ByteBuffer otherBuffer = (ByteBuffer) other;

        if (remaining() != otherBuffer.remaining()) {
            return false;
        }

        int myPosition = position;
        int otherPosition = otherBuffer.position;
        boolean equalSoFar = true;
        while (equalSoFar && (myPosition < limit)) {
            equalSoFar = get(myPosition++) == otherBuffer.get(otherPosition++);
        }

        return equalSoFar;
    }

    /**
     * Returns the byte at the current position and increases the position by 1.
     *
     * @return the byte at the current position.
     * @throws BufferUnderflowException
     *                if the position is equal or greater than limit.
     */
    public abstract byte get();

    /**
     * Reads bytes from the current position into the specified byte array and
     * increases the position by the number of bytes read.
     * <p>
     * Calling this method has the same effect as
     * {@code get(dst, 0, dst.length)}.
     *
     * @param dst
     *            the destination byte array.
     * @return {@code this}
     * @throws BufferUnderflowException
     *                if {@code dst.length} is greater than {@code remaining()}.
     */
    public ByteBuffer get(byte[] dst) {
        return get(dst, 0, dst.length);
    }

    /**
     * Reads bytes from the current position into the specified byte array,
     * starting at the specified offset, and increases the position by the
     * number of bytes read.
     *
     * @param dst
     *            the target byte array.
     * @param dstOffset
     *            the offset of the byte array, must not be negative and
     *            not greater than {@code dst.length}.
     * @param byteCount
     *            the number of bytes to read, must not be negative and not
     *            greater than {@code dst.length - dstOffset}
     * @return {@code this}
     * @throws IndexOutOfBoundsException if {@code dstOffset < 0 ||  byteCount < 0}
     * @throws BufferUnderflowException if {@code byteCount > remaining()}
     */
    public ByteBuffer get(byte[] dst, int dstOffset, int byteCount) {
        Arrays.checkOffsetAndCount(dst.length, dstOffset, byteCount);
        if (byteCount > remaining()) {
            throw new BufferUnderflowException();
        }
        for (int i = dstOffset; i < dstOffset + byteCount; ++i) {
            dst[i] = get();
        }
        return this;
    }

    /**
     * Returns the byte at the specified index and does not change the position.
     *
     * @param index
     *            the index, must not be negative and less than limit.
     * @return the byte at the specified index.
     * @throws IndexOutOfBoundsException
     *                if index is invalid.
     */
    public abstract byte get(int index);

    /**
     * Returns the char at the current position and increases the position by 2.
     * <p>
     * The 2 bytes starting at the current position are composed into a char
     * according to the current byte order and returned.
     *
     * @return the char at the current position.
     * @throws BufferUnderflowException
     *                if the position is greater than {@code limit - 2}.
     */
    public abstract char getChar();

    /**
     * Returns the char at the specified index.
     * <p>
     * The 2 bytes starting from the specified index are composed into a char
     * according to the current byte order and returned. The position is not
     * changed.
     *
     * @param index
     *            the index, must not be negative and equal or less than
     *            {@code limit - 2}.
     * @return the char at the specified index.
     * @throws IndexOutOfBoundsException
     *                if {@code index} is invalid.
     */
    public abstract char getChar(int index);

    /**
     * Returns the double at the current position and increases the position by
     * 8.
     * <p>
     * The 8 bytes starting from the current position are composed into a double
     * according to the current byte order and returned.
     *
     * @return the double at the current position.
     * @throws BufferUnderflowException
     *                if the position is greater than {@code limit - 8}.
     */
    public abstract double getDouble();

    /**
     * Returns the double at the specified index.
     * <p>
     * The 8 bytes starting at the specified index are composed into a double
     * according to the current byte order and returned. The position is not
     * changed.
     *
     * @param index
     *            the index, must not be negative and equal or less than
     *            {@code limit - 8}.
     * @return the double at the specified index.
     * @throws IndexOutOfBoundsException
     *                if {@code index} is invalid.
     */
    public abstract double getDouble(int index);

    /**
     * Returns the float at the current position and increases the position by
     * 4.
     * <p>
     * The 4 bytes starting at the current position are composed into a float
     * according to the current byte order and returned.
     *
     * @return the float at the current position.
     * @throws BufferUnderflowException
     *                if the position is greater than {@code limit - 4}.
     */
    public abstract float getFloat();

    /**
     * Returns the float at the specified index.
     * <p>
     * The 4 bytes starting at the specified index are composed into a float
     * according to the current byte order and returned. The position is not
     * changed.
     *
     * @param index
     *            the index, must not be negative and equal or less than
     *            {@code limit - 4}.
     * @return the float at the specified index.
     * @throws IndexOutOfBoundsException
     *                if {@code index} is invalid.
     */
    public abstract float getFloat(int index);

    /**
     * Returns the int at the current position and increases the position by 4.
     * <p>
     * The 4 bytes starting at the current position are composed into a int
     * according to the current byte order and returned.
     *
     * @return the int at the current position.
     * @throws BufferUnderflowException
     *                if the position is greater than {@code limit - 4}.
     */
    public abstract int getInt();

    /**
     * Returns the int at the specified index.
     * <p>
     * The 4 bytes starting at the specified index are composed into a int
     * according to the current byte order and returned. The position is not
     * changed.
     *
     * @param index
     *            the index, must not be negative and equal or less than
     *            {@code limit - 4}.
     * @return the int at the specified index.
     * @throws IndexOutOfBoundsException
     *                if {@code index} is invalid.
     */
    public abstract int getInt(int index);

    /**
     * Returns the long at the current position and increases the position by 8.
     * <p>
     * The 8 bytes starting at the current position are composed into a long
     * according to the current byte order and returned.
     *
     * @return the long at the current position.
     * @throws BufferUnderflowException
     *                if the position is greater than {@code limit - 8}.
     */
    public abstract long getLong();

    /**
     * Returns the long at the specified index.
     * <p>
     * The 8 bytes starting at the specified index are composed into a long
     * according to the current byte order and returned. The position is not
     * changed.
     *
     * @param index
     *            the index, must not be negative and equal or less than
     *            {@code limit - 8}.
     * @return the long at the specified index.
     * @throws IndexOutOfBoundsException
     *                if {@code index} is invalid.
     */
    public abstract long getLong(int index);

    /**
     * Returns the short at the current position and increases the position by 2.
     * <p>
     * The 2 bytes starting at the current position are composed into a short
     * according to the current byte order and returned.
     *
     * @return the short at the current position.
     * @throws BufferUnderflowException
     *                if the position is greater than {@code limit - 2}.
     */
    public abstract short getShort();

    /**
     * Returns the short at the specified index.
     * <p>
     * The 2 bytes starting at the specified index are composed into a short
     * according to the current byte order and returned. The position is not
     * changed.
     *
     * @param index
     *            the index, must not be negative and equal or less than
     *            {@code limit - 2}.
     * @return the short at the specified index.
     * @throws IndexOutOfBoundsException
     *                if {@code index} is invalid.
     */
    public abstract short getShort(int index);

    @Override public final boolean hasArray() {
        return protectedHasArray();
    }

    /**
     * Calculates this buffer's hash code from the remaining chars. The
     * position, limit, capacity and mark don't affect the hash code.
     *
     * @return the hash code calculated from the remaining bytes.
     */
    @Override
    public int hashCode() {
        int myPosition = position;
        int hash = 0;
        while (myPosition < limit) {
            hash = hash + get(myPosition++);
        }
        return hash;
    }

    /**
     * Indicates whether this buffer is direct.
     *
     * @return {@code true} if this buffer is direct, {@code false} otherwise.
     */
    @Override public abstract boolean isDirect();

    /**
     * Indicates whether this buffer is still valid.
     *
     * @return {@code true} if this buffer is valid, {@code false} if the
     *         buffer was invalidated and should not be used anymore.
     * @hide
     */
    public boolean isValid() {
        return true;
    }

    /**
     * Returns the byte order used by this buffer when converting bytes from/to
     * other primitive types.
     * <p>
     * The default byte order of byte buffer is always
     * {@link ByteOrder#BIG_ENDIAN BIG_ENDIAN}
     *
     * @return the byte order used by this buffer when converting bytes from/to
     *         other primitive types.
     */
    public final ByteOrder order() {
        return order;
    }

    /**
     * Sets the byte order of this buffer.
     *
     * @param byteOrder
     *            the byte order to set. If {@code null} then the order
     *            will be {@link ByteOrder#LITTLE_ENDIAN LITTLE_ENDIAN}.
     * @return {@code this}
     * @see ByteOrder
     */
    public final ByteBuffer order(ByteOrder byteOrder) {
        if (byteOrder == null) {
            byteOrder = ByteOrder.LITTLE_ENDIAN;
        }
        order = byteOrder;
        return this;
    }

    /**
     * Child class implements this method to realize {@code array()}.
     *
     * @see #array()
     */
    abstract byte[] protectedArray();

    /**
     * Child class implements this method to realize {@code arrayOffset()}.
     *
     * @see #arrayOffset()
     */
    abstract int protectedArrayOffset();

    /**
     * Child class implements this method to realize {@code hasArray()}.
     *
     * @see #hasArray()
     */
    abstract boolean protectedHasArray();

    /**
     * Writes the given byte to the current position and increases the position
     * by 1.
     *
     * @param b
     *            the byte to write.
     * @return {@code this}
     * @throws BufferOverflowException
     *                if position is equal or greater than limit.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public abstract ByteBuffer put(byte b);

    /**
     * Writes bytes in the given byte array to the current position and
     * increases the position by the number of bytes written.
     * <p>
     * Calling this method has the same effect as
     * {@code put(src, 0, src.length)}.
     *
     * @param src
     *            the source byte array.
     * @return {@code this}
     * @throws BufferOverflowException
     *                if {@code remaining()} is less than {@code src.length}.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public final ByteBuffer put(byte[] src) {
        return put(src, 0, src.length);
    }

    /**
     * Writes bytes in the given byte array, starting from the specified offset,
     * to the current position and increases the position by the number of bytes
     * written.
     *
     * @param src
     *            the source byte array.
     * @param srcOffset
     *            the offset of byte array, must not be negative and not greater
     *            than {@code src.length}.
     * @param byteCount
     *            the number of bytes to write, must not be negative and not
     *            greater than {@code src.length - srcOffset}.
     * @return {@code this}
     * @throws BufferOverflowException
     *                if {@code remaining()} is less than {@code byteCount}.
     * @throws IndexOutOfBoundsException
     *                if either {@code srcOffset} or {@code byteCount} is invalid.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public ByteBuffer put(byte[] src, int srcOffset, int byteCount) {
        Arrays.checkOffsetAndCount(src.length, srcOffset, byteCount);
        if (byteCount > remaining()) {
            throw new BufferOverflowException();
        }
        for (int i = srcOffset; i < srcOffset + byteCount; ++i) {
            put(src[i]);
        }
        return this;
    }

    /**
     * Writes all the remaining bytes of the {@code src} byte buffer to this
     * buffer's current position, and increases both buffers' position by the
     * number of bytes copied.
     *
     * @param src
     *            the source byte buffer.
     * @return {@code this}
     * @throws BufferOverflowException
     *                if {@code src.remaining()} is greater than this buffer's
     *                {@code remaining()}.
     * @throws IllegalArgumentException
     *                if {@code src} is this buffer.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public ByteBuffer put(ByteBuffer src) {
        if (isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        if (src == this) {
            throw new IllegalArgumentException("src == this");
        }
        int srcByteCount = src.remaining();
        if (srcByteCount > remaining()) {
            throw new BufferOverflowException();
        }

        Object srcObject = src.isDirect() ? src : NioUtils.unsafeArray(src);
        int srcOffset = src.position();
        if (!src.isDirect()) {
            srcOffset += NioUtils.unsafeArrayOffset(src);
        }

        ByteBuffer dst = this;
        Object dstObject = dst.isDirect() ? dst : NioUtils.unsafeArray(dst);
        int dstOffset = dst.position();
        if (!dst.isDirect()) {
            dstOffset += NioUtils.unsafeArrayOffset(dst);
        }

        Memory.memmove(dstObject, dstOffset, srcObject, srcOffset, srcByteCount);
        src.position(src.limit());
        dst.position(dst.position() + srcByteCount);

        return this;
    }

    /**
     * Write a byte to the specified index of this buffer without changing the
     * position.
     *
     * @param index
     *            the index, must not be negative and less than the limit.
     * @param b
     *            the byte to write.
     * @return {@code this}
     * @throws IndexOutOfBoundsException
     *                if {@code index} is invalid.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public abstract ByteBuffer put(int index, byte b);

    /**
     * Writes the given char to the current position and increases the position
     * by 2.
     * <p>
     * The char is converted to bytes using the current byte order.
     *
     * @param value
     *            the char to write.
     * @return {@code this}
     * @throws BufferOverflowException
     *                if position is greater than {@code limit - 2}.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public abstract ByteBuffer putChar(char value);

    /**
     * Writes the given char to the specified index of this buffer.
     * <p>
     * The char is converted to bytes using the current byte order. The position
     * is not changed.
     *
     * @param index
     *            the index, must not be negative and equal or less than
     *            {@code limit - 2}.
     * @param value
     *            the char to write.
     * @return {@code this}
     * @throws IndexOutOfBoundsException
     *                if {@code index} is invalid.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public abstract ByteBuffer putChar(int index, char value);

    /**
     * Writes the given double to the current position and increases the position
     * by 8.
     * <p>
     * The double is converted to bytes using the current byte order.
     *
     * @param value
     *            the double to write.
     * @return {@code this}
     * @throws BufferOverflowException
     *                if position is greater than {@code limit - 8}.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public abstract ByteBuffer putDouble(double value);

    /**
     * Writes the given double to the specified index of this buffer.
     * <p>
     * The double is converted to bytes using the current byte order. The
     * position is not changed.
     *
     * @param index
     *            the index, must not be negative and equal or less than
     *            {@code limit - 8}.
     * @param value
     *            the double to write.
     * @return {@code this}
     * @throws IndexOutOfBoundsException
     *                if {@code index} is invalid.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public abstract ByteBuffer putDouble(int index, double value);

    /**
     * Writes the given float to the current position and increases the position
     * by 4.
     * <p>
     * The float is converted to bytes using the current byte order.
     *
     * @param value
     *            the float to write.
     * @return {@code this}
     * @throws BufferOverflowException
     *                if position is greater than {@code limit - 4}.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public abstract ByteBuffer putFloat(float value);

    /**
     * Writes the given float to the specified index of this buffer.
     * <p>
     * The float is converted to bytes using the current byte order. The
     * position is not changed.
     *
     * @param index
     *            the index, must not be negative and equal or less than
     *            {@code limit - 4}.
     * @param value
     *            the float to write.
     * @return {@code this}
     * @throws IndexOutOfBoundsException
     *                if {@code index} is invalid.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public abstract ByteBuffer putFloat(int index, float value);

    /**
     * Writes the given int to the current position and increases the position by
     * 4.
     * <p>
     * The int is converted to bytes using the current byte order.
     *
     * @param value
     *            the int to write.
     * @return {@code this}
     * @throws BufferOverflowException
     *                if position is greater than {@code limit - 4}.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public abstract ByteBuffer putInt(int value);

    /**
     * Writes the given int to the specified index of this buffer.
     * <p>
     * The int is converted to bytes using the current byte order. The position
     * is not changed.
     *
     * @param index
     *            the index, must not be negative and equal or less than
     *            {@code limit - 4}.
     * @param value
     *            the int to write.
     * @return {@code this}
     * @throws IndexOutOfBoundsException
     *                if {@code index} is invalid.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public abstract ByteBuffer putInt(int index, int value);

    /**
     * Writes the given long to the current position and increases the position
     * by 8.
     * <p>
     * The long is converted to bytes using the current byte order.
     *
     * @param value
     *            the long to write.
     * @return {@code this}
     * @throws BufferOverflowException
     *                if position is greater than {@code limit - 8}.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public abstract ByteBuffer putLong(long value);

    /**
     * Writes the given long to the specified index of this buffer.
     * <p>
     * The long is converted to bytes using the current byte order. The position
     * is not changed.
     *
     * @param index
     *            the index, must not be negative and equal or less than
     *            {@code limit - 8}.
     * @param value
     *            the long to write.
     * @return {@code this}
     * @throws IndexOutOfBoundsException
     *                if {@code index} is invalid.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public abstract ByteBuffer putLong(int index, long value);

    /**
     * Writes the given short to the current position and increases the position
     * by 2.
     * <p>
     * The short is converted to bytes using the current byte order.
     *
     * @param value
     *            the short to write.
     * @return {@code this}
     * @throws BufferOverflowException
     *                if position is greater than {@code limit - 2}.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public abstract ByteBuffer putShort(short value);

    /**
     * Writes the given short to the specified index of this buffer.
     * <p>
     * The short is converted to bytes using the current byte order. The
     * position is not changed.
     *
     * @param index
     *            the index, must not be negative and equal or less than
     *            {@code limit - 2}.
     * @param value
     *            the short to write.
     * @return {@code this}
     * @throws IndexOutOfBoundsException
     *                if {@code index} is invalid.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public abstract ByteBuffer putShort(int index, short value);

    /**
     * Returns a sliced buffer that shares its content with this buffer.
     * <p>
     * The sliced buffer's capacity will be this buffer's
     * {@code remaining()}, and it's zero position will correspond to
     * this buffer's current position. The new buffer's position will be 0,
     * limit will be its capacity, and its mark is cleared. The new buffer's
     * read-only property and byte order are the same as this buffer's.
     * <p>
     * The new buffer shares its content with this buffer, which means either
     * buffer's change of content will be visible to the other. The two buffers'
     * position, limit and mark are independent.
     */
    public abstract ByteBuffer slice();
}
