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
 * A buffer of ints.
 * <p>
 * A int buffer can be created in either of the following ways:
 * <ul>
 * <li>{@link #allocate(int) Allocate} a new int array and create a buffer
 * based on it;</li>
 * <li>{@link #wrap(int[]) Wrap} an existing int array to create a new buffer;</li>
 * <li>Use {@link java.nio.ByteBuffer#asIntBuffer() ByteBuffer.asIntBuffer} to
 * create a int buffer based on a byte buffer.</li>
 * </ul>
 */
public abstract class IntBuffer extends Buffer implements Comparable<IntBuffer> {

    /**
     * Creates an int buffer based on a newly allocated int array.
     *
     * @param capacity
     *            the capacity of the new buffer.
     * @return the created int buffer.
     * @throws IllegalArgumentException
     *             if {@code capacity} is less than zero.
     */
    public static IntBuffer allocate(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity < 0: " + capacity);
        }
        return new IntArrayBuffer(new int[capacity]);
    }

    /**
     * Creates a new int buffer by wrapping the given int array.
     * <p>
     * Calling this method has the same effect as
     * {@code wrap(array, 0, array.length)}.
     *
     * @param array
     *            the int array which the new buffer will be based on.
     * @return the created int buffer.
     */
    public static IntBuffer wrap(int[] array) {
        return wrap(array, 0, array.length);
    }

    /**
     * Creates a new int buffer by wrapping the given int array.
     * <p>
     * The new buffer's position will be {@code start}, limit will be
     * {@code start + intCount}, capacity will be the length of the array.
     *
     * @param array
     *            the int array which the new buffer will be based on.
     * @param start
     *            the start index, must not be negative and not greater than
     *            {@code array.length}
     * @param intCount
     *            the length, must not be negative and not greater than
     *            {@code array.length - start}.
     * @return the created int buffer.
     * @throws IndexOutOfBoundsException
     *                if either {@code start} or {@code intCount} is invalid.
     */
    public static IntBuffer wrap(int[] array, int start, int intCount) {
        Arrays.checkOffsetAndCount(array.length, start, intCount);
        IntBuffer buf = new IntArrayBuffer(array);
        buf.position = start;
        buf.limit = start + intCount;
        return buf;
    }

    IntBuffer(int capacity, long effectiveDirectAddress) {
        super(2, capacity, effectiveDirectAddress);
    }

    public final int[] array() {
        return protectedArray();
    }

    public final int arrayOffset() {
        return protectedArrayOffset();
    }

    /**
     * Returns a read-only buffer that shares its content with this buffer.
     * <p>
     * The returned buffer is guaranteed to be a new instance, even this buffer
     * is read-only itself. The new buffer's position, limit, capacity and mark
     * are the same as this buffer's.
     * <p>
     * The new buffer shares its content with this buffer, which means this
     * buffer's change of content will be visible to the new buffer. The two
     * buffer's position, limit and mark are independent.
     *
     * @return a read-only version of this buffer.
     */
    public abstract IntBuffer asReadOnlyBuffer();

    /**
     * Compacts this int buffer.
     * <p>
     * The remaining ints will be moved to the head of the buffer, starting from
     * position zero. Then the position is set to {@code remaining()}; the
     * limit is set to capacity; the mark is cleared.
     *
     * @return this buffer.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public abstract IntBuffer compact();

    /**
     * Compares the remaining ints of this buffer to another int buffer's
     * remaining ints.
     *
     * @param otherBuffer
     *            another int buffer.
     * @return a negative value if this is less than {@code other}; 0 if this
     *         equals to {@code other}; a positive value if this is greater
     *         than {@code other}.
     * @throws ClassCastException
     *                if {@code other} is not an int buffer.
     */
    public int compareTo(IntBuffer otherBuffer) {
        int compareRemaining = (remaining() < otherBuffer.remaining()) ? remaining()
                : otherBuffer.remaining();
        int thisPos = position;
        int otherPos = otherBuffer.position;
        int thisInt, otherInt;
        while (compareRemaining > 0) {
            thisInt = get(thisPos);
            otherInt = otherBuffer.get(otherPos);
            if (thisInt != otherInt) {
                return thisInt < otherInt ? -1 : 1;
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
    public abstract IntBuffer duplicate();

    /**
     * Checks whether this int buffer is equal to another object.
     * <p>
     * If {@code other} is not a int buffer then {@code false} is returned. Two
     * int buffers are equal if and only if their remaining ints are exactly the
     * same. Position, limit, capacity and mark are not considered.
     *
     * @param other
     *            the object to compare with this int buffer.
     * @return {@code true} if this int buffer is equal to {@code other},
     *         {@code false} otherwise.
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof IntBuffer)) {
            return false;
        }
        IntBuffer otherBuffer = (IntBuffer) other;

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
     * Returns the int at the current position and increases the position by 1.
     *
     * @return the int at the current position.
     * @throws BufferUnderflowException
     *                if the position is equal or greater than limit.
     */
    public abstract int get();

    /**
     * Reads ints from the current position into the specified int array and
     * increases the position by the number of ints read.
     * <p>
     * Calling this method has the same effect as
     * {@code get(dst, 0, dst.length)}.
     *
     * @param dst
     *            the destination int array.
     * @return this buffer.
     * @throws BufferUnderflowException
     *                if {@code dst.length} is greater than {@code remaining()}.
     */
    public IntBuffer get(int[] dst) {
        return get(dst, 0, dst.length);
    }

    /**
     * Reads ints from the current position into the specified int array,
     * starting from the specified offset, and increases the position by the
     * number of ints read.
     *
     * @param dst
     *            the target int array.
     * @param dstOffset
     *            the offset of the int array, must not be negative and not
     *            greater than {@code dst.length}.
     * @param intCount
     *            the number of ints to read, must be no less than zero and not
     *            greater than {@code dst.length - dstOffset}.
     * @return this buffer.
     * @throws IndexOutOfBoundsException
     *                if either {@code dstOffset} or {@code intCount} is invalid.
     * @throws BufferUnderflowException
     *                if {@code intCount} is greater than {@code remaining()}.
     */
    public IntBuffer get(int[] dst, int dstOffset, int intCount) {
        Arrays.checkOffsetAndCount(dst.length, dstOffset, intCount);
        if (intCount > remaining()) {
            throw new BufferUnderflowException();
        }
        for (int i = dstOffset; i < dstOffset + intCount; ++i) {
            dst[i] = get();
        }
        return this;
    }

    /**
     * Returns an int at the specified index; the position is not changed.
     *
     * @param index
     *            the index, must not be negative and less than limit.
     * @return an int at the specified index.
     * @throws IndexOutOfBoundsException
     *                if index is invalid.
     */
    public abstract int get(int index);

    public final boolean hasArray() {
        return protectedHasArray();
    }

    /**
     * Calculates this buffer's hash code from the remaining chars. The
     * position, limit, capacity and mark don't affect the hash code.
     *
     * @return the hash code calculated from the remaining ints.
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
     * An int buffer is direct if it is based on a byte buffer and the byte
     * buffer is direct.
     *
     * @return {@code true} if this buffer is direct, {@code false} otherwise.
     */
    public abstract boolean isDirect();

    /**
     * Returns the byte order used by this buffer when converting ints from/to
     * bytes.
     * <p>
     * If this buffer is not based on a byte buffer, then always return the
     * platform's native byte order.
     *
     * @return the byte order used by this buffer when converting ints from/to
     *         bytes.
     */
    public abstract ByteOrder order();

    /**
     * Child class implements this method to realize {@code array()}.
     *
     * @return see {@code array()}
     */
    abstract int[] protectedArray();

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
     * Writes the given int to the current position and increases the position
     * by 1.
     *
     * @param i
     *            the int to write.
     * @return this buffer.
     * @throws BufferOverflowException
     *                if position is equal or greater than limit.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public abstract IntBuffer put(int i);

    /**
     * Writes ints from the given int array to the current position and
     * increases the position by the number of ints written.
     * <p>
     * Calling this method has the same effect as
     * {@code put(src, 0, src.length)}.
     *
     * @param src
     *            the source int array.
     * @return this buffer.
     * @throws BufferOverflowException
     *                if {@code remaining()} is less than {@code src.length}.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public final IntBuffer put(int[] src) {
        return put(src, 0, src.length);
    }

    /**
     * Writes ints from the given int array, starting from the specified offset,
     * to the current position and increases the position by the number of ints
     * written.
     *
     * @param src
     *            the source int array.
     * @param srcOffset
     *            the offset of int array, must not be negative and not greater
     *            than {@code src.length}.
     * @param intCount
     *            the number of ints to write, must be no less than zero and not
     *            greater than {@code src.length - srcOffset}.
     * @return this buffer.
     * @throws BufferOverflowException
     *                if {@code remaining()} is less than {@code intCount}.
     * @throws IndexOutOfBoundsException
     *                if either {@code srcOffset} or {@code intCount} is invalid.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public IntBuffer put(int[] src, int srcOffset, int intCount) {
        if (isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        Arrays.checkOffsetAndCount(src.length, srcOffset, intCount);
        if (intCount > remaining()) {
            throw new BufferOverflowException();
        }
        for (int i = srcOffset; i < srcOffset + intCount; ++i) {
            put(src[i]);
        }
        return this;
    }

    /**
     * Writes all the remaining ints of the {@code src} int buffer to this
     * buffer's current position, and increases both buffers' position by the
     * number of ints copied.
     *
     * @param src
     *            the source int buffer.
     * @return this buffer.
     * @throws BufferOverflowException
     *                if {@code src.remaining()} is greater than this buffer's
     *                {@code remaining()}.
     * @throws IllegalArgumentException
     *                if {@code src} is this buffer.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public IntBuffer put(IntBuffer src) {
        if (isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        if (src == this) {
            throw new IllegalArgumentException("src == this");
        }
        if (src.remaining() > remaining()) {
            throw new BufferOverflowException();
        }
        int[] contents = new int[src.remaining()];
        src.get(contents);
        put(contents);
        return this;
    }

    /**
     * Write a int to the specified index of this buffer; the position is not
     * changed.
     *
     * @param index
     *            the index, must not be negative and less than the limit.
     * @param i
     *            the int to write.
     * @return this buffer.
     * @throws IndexOutOfBoundsException
     *                if index is invalid.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public abstract IntBuffer put(int index, int i);

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
    public abstract IntBuffer slice();
}
