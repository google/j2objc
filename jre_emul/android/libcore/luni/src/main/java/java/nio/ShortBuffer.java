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

/**
 * A buffer of shorts.
 * <p>
 * A short buffer can be created in either of the following ways:
 * <ul>
 * <li>{@link #allocate(int) Allocate} a new short array and create a buffer
 * based on it;</li>
 * <li>{@link #wrap(short[]) Wrap} an existing short array to create a new
 * buffer;</li>
 * <li>Use {@link java.nio.ByteBuffer#asShortBuffer() ByteBuffer.asShortBuffer}
 * to create a short buffer based on a byte buffer.</li>
 * </ul>
 */
public abstract class ShortBuffer extends Buffer implements
        Comparable<ShortBuffer> {

    /**
     * Creates a short buffer based on a newly allocated short array.
     *
     * @param capacity
     *            the capacity of the new buffer.
     * @return the created short buffer.
     * @throws IllegalArgumentException
     *             if {@code capacity} is less than zero.
     */
    public static ShortBuffer allocate(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity < 0: " + capacity);
        }
        return new ShortArrayBuffer(new short[capacity]);
    }

    /**
     * Creates a new short buffer by wrapping the given short array.
     * <p>
     * Calling this method has the same effect as
     * {@code wrap(array, 0, array.length)}.
     *
     * @param array
     *            the short array which the new buffer will be based on.
     * @return the created short buffer.
     */
    public static ShortBuffer wrap(short[] array) {
        return wrap(array, 0, array.length);
    }

    /**
     * Creates a new short buffer by wrapping the given short array.
     * <p>
     * The new buffer's position will be {@code start}, limit will be
     * {@code start + shortCount}, capacity will be the length of the array.
     *
     * @param array
     *            the short array which the new buffer will be based on.
     * @param start
     *            the start index, must not be negative and not greater than
     *            {@code array.length}.
     * @param shortCount
     *            the length, must not be negative and not greater than
     *            {@code array.length - start}.
     * @return the created short buffer.
     * @throws IndexOutOfBoundsException
     *                if either {@code start} or {@code shortCount} is invalid.
     */
    public static ShortBuffer wrap(short[] array, int start, int shortCount) {
        Arrays.checkOffsetAndCount(array.length, start, shortCount);
        ShortBuffer buf = new ShortArrayBuffer(array);
        buf.position = start;
        buf.limit = start + shortCount;
        return buf;
    }

    ShortBuffer(int capacity, long effectiveDirectAddress) {
        super(1, capacity, effectiveDirectAddress);
    }

    public final short[] array() {
        return protectedArray();
    }

    public final int arrayOffset() {
        return protectedArrayOffset();
    }

    /**
     * Returns a read-only buffer that shares its content with this buffer.
     * <p>
     * The returned buffer is guaranteed to be a new instance, even if this
     * buffer is read-only itself. The new buffer's position, limit, capacity
     * and mark are the same as this buffer's.
     * <p>
     * The new buffer shares its content with this buffer, which means this
     * buffer's change of content will be visible to the new buffer. The two
     * buffer's position, limit and mark are independent.
     *
     * @return a read-only version of this buffer.
     */
    public abstract ShortBuffer asReadOnlyBuffer();

    /**
     * Compacts this short buffer.
     * <p>
     * The remaining shorts will be moved to the head of the buffer, starting
     * from position zero. Then the position is set to {@code remaining()}; the
     * limit is set to capacity; the mark is cleared.
     *
     * @return this buffer.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public abstract ShortBuffer compact();

    /**
     * Compare the remaining shorts of this buffer to another short buffer's
     * remaining shorts.
     *
     * @param otherBuffer
     *            another short buffer.
     * @return a negative value if this is less than {@code otherBuffer}; 0 if
     *         this equals to {@code otherBuffer}; a positive value if this is
     *         greater than {@code otherBuffer}.
     * @throws ClassCastException
     *                if {@code otherBuffer} is not a short buffer.
     */
    public int compareTo(ShortBuffer otherBuffer) {
        int compareRemaining = (remaining() < otherBuffer.remaining()) ? remaining()
                : otherBuffer.remaining();
        int thisPos = position;
        int otherPos = otherBuffer.position;
        short thisByte, otherByte;
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
     * as this buffer. The duplicated buffer's read-only property and byte order
     * are the same as this buffer's.
     * <p>
     * The new buffer shares its content with this buffer, which means either
     * buffer's change of content will be visible to the other. The two buffers'
     * position, limit and mark are independent.
     */
    public abstract ShortBuffer duplicate();

    /**
     * Checks whether this short buffer is equal to another object.
     * <p>
     * If {@code other} is not a short buffer then {@code false} is returned.
     * Two short buffers are equal if and only if their remaining shorts are
     * exactly the same. Position, limit, capacity and mark are not considered.
     *
     * @param other
     *            the object to compare with this short buffer.
     * @return {@code true} if this short buffer is equal to {@code other},
     *         {@code false} otherwise.
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ShortBuffer)) {
            return false;
        }
        ShortBuffer otherBuffer = (ShortBuffer) other;

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
     * Returns the short at the current position and increases the position by
     * 1.
     *
     * @return the short at the current position.
     * @throws BufferUnderflowException
     *                if the position is equal or greater than limit.
     */
    public abstract short get();

    /**
     * Reads shorts from the current position into the specified short array and
     * increases the position by the number of shorts read.
     * <p>
     * Calling this method has the same effect as
     * {@code get(dst, 0, dst.length)}.
     *
     * @param dst
     *            the destination short array.
     * @return this buffer.
     * @throws BufferUnderflowException
     *                if {@code dst.length} is greater than {@code remaining()}.
     */
    public ShortBuffer get(short[] dst) {
        return get(dst, 0, dst.length);
    }

    /**
     * Reads shorts from the current position into the specified short array,
     * starting from the specified offset, and increases the position by the
     * number of shorts read.
     *
     * @param dst
     *            the target short array.
     * @param dstOffset
     *            the offset of the short array, must not be negative and not
     *            greater than {@code dst.length}.
     * @param shortCount
     *            the number of shorts to read, must be no less than zero and
     *            not greater than {@code dst.length - dstOffset}.
     * @return this buffer.
     * @throws IndexOutOfBoundsException
     *                if either {@code dstOffset} or {@code shortCount} is invalid.
     * @throws BufferUnderflowException
     *                if {@code shortCount} is greater than {@code remaining()}.
     */
    public ShortBuffer get(short[] dst, int dstOffset, int shortCount) {
        Arrays.checkOffsetAndCount(dst.length, dstOffset, shortCount);
        if (shortCount > remaining()) {
            throw new BufferUnderflowException();
        }
        for (int i = dstOffset; i < dstOffset + shortCount; ++i) {
            dst[i] = get();
        }
        return this;
    }

    /**
     * Returns the short at the specified index; the position is not changed.
     *
     * @param index
     *            the index, must not be negative and less than limit.
     * @return a short at the specified index.
     * @throws IndexOutOfBoundsException
     *                if index is invalid.
     */
    public abstract short get(int index);

    public final boolean hasArray() {
        return protectedHasArray();
    }

    /**
     * Calculates this buffer's hash code from the remaining chars. The
     * position, limit, capacity and mark don't affect the hash code.
     *
     * @return the hash code calculated from the remaining shorts.
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
     * Indicates whether this buffer is direct. A direct buffer will try its
     * best to take advantage of native memory APIs and it may not stay in the
     * Java heap, so it is not affected by garbage collection.
     * <p>
     * A short buffer is direct if it is based on a byte buffer and the byte
     * buffer is direct.
     *
     * @return {@code true} if this buffer is direct, {@code false} otherwise.
     */
    public abstract boolean isDirect();

    /**
     * Returns the byte order used by this buffer when converting shorts from/to
     * bytes.
     * <p>
     * If this buffer is not based on a byte buffer, then always return the
     * platform's native byte order.
     *
     * @return the byte order used by this buffer when converting shorts from/to
     *         bytes.
     */
    public abstract ByteOrder order();

    /**
     * Child class implements this method to realize {@code array()}.
     *
     * @return see {@code array()}
     */
    abstract short[] protectedArray();

    /**
     * Child class implements this method to realize {@code arrayOffset()}.
     *
     * @return see {@code arrayOffset()}
     */
    abstract int protectedArrayOffset();

    /**
     * Child class implements this method to realize {@code hasArray()}.
     *
     * @return see {@code hasArray()}
     */
    abstract boolean protectedHasArray();

    /**
     * Writes the given short to the current position and increases the position
     * by 1.
     *
     * @param s
     *            the short to write.
     * @return this buffer.
     * @throws BufferOverflowException
     *                if position is equal or greater than limit.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public abstract ShortBuffer put(short s);

    /**
     * Writes shorts from the given short array to the current position and
     * increases the position by the number of shorts written.
     * <p>
     * Calling this method has the same effect as
     * {@code put(src, 0, src.length)}.
     *
     * @param src
     *            the source short array.
     * @return this buffer.
     * @throws BufferOverflowException
     *                if {@code remaining()} is less than {@code src.length}.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public final ShortBuffer put(short[] src) {
        return put(src, 0, src.length);
    }

    /**
     * Writes shorts from the given short array, starting from the specified
     * offset, to the current position and increases the position by the number
     * of shorts written.
     *
     * @param src
     *            the source short array.
     * @param srcOffset
     *            the offset of short array, must not be negative and not
     *            greater than {@code src.length}.
     * @param shortCount
     *            the number of shorts to write, must be no less than zero and
     *            not greater than {@code src.length - srcOffset}.
     * @return this buffer.
     * @throws BufferOverflowException
     *                if {@code remaining()} is less than {@code shortCount}.
     * @throws IndexOutOfBoundsException
     *                if either {@code srcOffset} or {@code shortCount} is invalid.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public ShortBuffer put(short[] src, int srcOffset, int shortCount) {
        Arrays.checkOffsetAndCount(src.length, srcOffset, shortCount);
        if (shortCount > remaining()) {
            throw new BufferOverflowException();
        }
        for (int i = srcOffset; i < srcOffset + shortCount; ++i) {
            put(src[i]);
        }
        return this;
    }

    /**
     * Writes all the remaining shorts of the {@code src} short buffer to this
     * buffer's current position, and increases both buffers' position by the
     * number of shorts copied.
     *
     * @param src
     *            the source short buffer.
     * @return this buffer.
     * @throws BufferOverflowException
     *                if {@code src.remaining()} is greater than this buffer's
     *                {@code remaining()}.
     * @throws IllegalArgumentException
     *                if {@code src} is this buffer.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public ShortBuffer put(ShortBuffer src) {
        if (isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        if (src == this) {
            throw new IllegalArgumentException("src == this");
        }
        if (src.remaining() > remaining()) {
            throw new BufferOverflowException();
        }
        short[] contents = new short[src.remaining()];
        src.get(contents);
        put(contents);
        return this;
    }

    /**
     * Writes a short to the specified index of this buffer; the position is not
     * changed.
     *
     * @param index
     *            the index, must not be negative and less than the limit.
     * @param s
     *            the short to write.
     * @return this buffer.
     * @throws IndexOutOfBoundsException
     *                if index is invalid.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public abstract ShortBuffer put(int index, short s);

    /**
     * Returns a sliced buffer that shares its content with this buffer.
     * <p>
     * The sliced buffer's capacity will be this buffer's {@code remaining()},
     * and its zero position will correspond to this buffer's current position.
     * The new buffer's position will be 0, limit will be its capacity, and its
     * mark is cleared. The new buffer's read-only property and byte order are
     * same as this buffer's.
     * <p>
     * The new buffer shares its content with this buffer, which means either
     * buffer's change of content will be visible to the other. The two buffers'
     * position, limit and mark are independent.
     */
    public abstract ShortBuffer slice();
}
