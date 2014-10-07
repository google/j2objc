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
 * A buffer of doubles.
 * <p>
 * A double buffer can be created in either one of the following ways:
 * <ul>
 * <li>{@link #allocate(int) Allocate} a new double array and create a buffer
 * based on it;</li>
 * <li>{@link #wrap(double[]) Wrap} an existing double array to create a new
 * buffer;</li>
 * <li>Use
 * {@link java.nio.ByteBuffer#asDoubleBuffer() ByteBuffer.asDoubleBuffer} to
 * create a double buffer based on a byte buffer.</li>
 * </ul>
 */
public abstract class DoubleBuffer extends Buffer implements
        Comparable<DoubleBuffer> {

    /**
     * Creates a double buffer based on a newly allocated double array.
     *
     * @param capacity
     *            the capacity of the new buffer.
     * @return the created double buffer.
     * @throws IllegalArgumentException
     *             if {@code capacity} is less than zero.
     */
    public static DoubleBuffer allocate(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity < 0: " + capacity);
        }
        return new DoubleArrayBuffer(new double[capacity]);
    }

    /**
     * Creates a new double buffer by wrapping the given double array.
     * <p>
     * Calling this method has the same effect as
     * {@code wrap(array, 0, array.length)}.
     *
     * @param array
     *            the double array which the new buffer will be based on.
     * @return the created double buffer.
     */
    public static DoubleBuffer wrap(double[] array) {
        return wrap(array, 0, array.length);
    }

    /**
     * Creates a new double buffer by wrapping the given double array.
     * <p>
     * The new buffer's position will be {@code start}, limit will be
     * {@code start + doubleCount}, capacity will be the length of the array.
     *
     * @param array
     *            the double array which the new buffer will be based on.
     * @param start
     *            the start index, must not be negative and not greater than
     *            {@code array.length}.
     * @param doubleCount
     *            the length, must not be negative and not greater than
     *            {@code array.length - start}.
     * @return the created double buffer.
     * @throws IndexOutOfBoundsException
     *                if either {@code start} or {@code doubleCount} is invalid.
     */
    public static DoubleBuffer wrap(double[] array, int start, int doubleCount) {
        Arrays.checkOffsetAndCount(array.length, start, doubleCount);
        DoubleBuffer buf = new DoubleArrayBuffer(array);
        buf.position = start;
        buf.limit = start + doubleCount;
        return buf;
    }

    DoubleBuffer(int capacity, long effectiveDirectAddress) {
        super(3, capacity, effectiveDirectAddress);
    }

    public final double[] array() {
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
     * The new buffer shares its content with this buffer, which means that this
     * buffer's change of content will be visible to the new buffer. The two
     * buffer's position, limit and mark are independent.
     *
     * @return a read-only version of this buffer.
     */
    public abstract DoubleBuffer asReadOnlyBuffer();

    /**
     * Compacts this double buffer.
     * <p>
     * The remaining doubles will be moved to the head of the buffer, staring
     * from position zero. Then the position is set to {@code remaining()}; the
     * limit is set to capacity; the mark is cleared.
     *
     * @return this buffer.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public abstract DoubleBuffer compact();

    /**
     * Compare the remaining doubles of this buffer to another double buffer's
     * remaining doubles.
     *
     * @param otherBuffer
     *            another double buffer.
     * @return a negative value if this is less than {@code other}; 0 if this
     *         equals to {@code other}; a positive value if this is greater
     *         than {@code other}.
     * @throws ClassCastException
     *                if {@code other} is not a double buffer.
     */
    public int compareTo(DoubleBuffer otherBuffer) {
        int compareRemaining = (remaining() < otherBuffer.remaining()) ? remaining()
                : otherBuffer.remaining();
        int thisPos = position;
        int otherPos = otherBuffer.position;
        double thisDouble, otherDouble;
        while (compareRemaining > 0) {
            thisDouble = get(thisPos);
            otherDouble = otherBuffer.get(otherPos);
            // checks for double and NaN inequality
            if ((thisDouble != otherDouble)
                    && ((thisDouble == thisDouble) || (otherDouble == otherDouble))) {
                return thisDouble < otherDouble ? -1 : 1;
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
     * as this buffer's. The duplicated buffer's read-only property and byte
     * order are the same as this buffer's, too.
     * <p>
     * The new buffer shares its content with this buffer, which means either
     * buffer's change of content will be visible to the other. The two buffers'
     * position, limit and mark are independent.
     */
    public abstract DoubleBuffer duplicate();

    /**
     * Checks whether this double buffer is equal to another object. If {@code
     * other} is not a {@code DoubleBuffer} then {@code false} is returned.
     *
     * <p>Two double buffers are equal if their remaining doubles are equal.
     * Position, limit, capacity and mark are not considered.
     *
     * <p>This method considers two doubles {@code a} and {@code b} to be equal
     * if {@code a == b} or if {@code a} and {@code b} are both {@code NaN}.
     * Unlike {@link Double#equals}, this method considers {@code -0.0} and
     * {@code +0.0} to be equal.
     *
     * @param other
     *            the object to compare with this double buffer.
     * @return {@code true} if this double buffer is equal to {@code other},
     *         {@code false} otherwise.
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof DoubleBuffer)) {
            return false;
        }
        DoubleBuffer otherBuffer = (DoubleBuffer) other;

        if (remaining() != otherBuffer.remaining()) {
            return false;
        }

        int myPosition = position;
        int otherPosition = otherBuffer.position;
        boolean equalSoFar = true;
        while (equalSoFar && (myPosition < limit)) {
            double a = get(myPosition++);
            double b = otherBuffer.get(otherPosition++);
            equalSoFar = a == b || (a != a && b != b);
        }

        return equalSoFar;
    }

    /**
     * Returns the double at the current position and increases the position by
     * 1.
     *
     * @return the double at the current position.
     * @throws BufferUnderflowException
     *                if the position is equal or greater than limit.
     */
    public abstract double get();

    /**
     * Reads doubles from the current position into the specified double array
     * and increases the position by the number of doubles read.
     * <p>
     * Calling this method has the same effect as
     * {@code get(dst, 0, dst.length)}.
     *
     * @param dst
     *            the destination double array.
     * @return this buffer.
     * @throws BufferUnderflowException
     *                if {@code dst.length} is greater than {@code remaining()}.
     */
    public DoubleBuffer get(double[] dst) {
        return get(dst, 0, dst.length);
    }

    /**
     * Reads doubles from the current position into the specified double array,
     * starting from the specified offset, and increases the position by the
     * number of doubles read.
     *
     * @param dst
     *            the target double array.
     * @param dstOffset
     *            the offset of the double array, must not be negative and not
     *            greater than {@code dst.length}.
     * @param doubleCount
     *            the number of doubles to read, must be no less than zero and
     *            not greater than {@code dst.length - dstOffset}.
     * @return this buffer.
     * @throws IndexOutOfBoundsException
     *                if either {@code dstOffset} or {@code doubleCount} is invalid.
     * @throws BufferUnderflowException
     *                if {@code doubleCount} is greater than {@code remaining()}.
     */
    public DoubleBuffer get(double[] dst, int dstOffset, int doubleCount) {
        Arrays.checkOffsetAndCount(dst.length, dstOffset, doubleCount);
        if (doubleCount > remaining()) {
            throw new BufferUnderflowException();
        }
        for (int i = dstOffset; i < dstOffset + doubleCount; ++i) {
            dst[i] = get();
        }
        return this;
    }

    /**
     * Returns a double at the specified index; the position is not changed.
     *
     * @param index
     *            the index, must not be negative and less than limit.
     * @return a double at the specified index.
     * @throws IndexOutOfBoundsException
     *                if index is invalid.
     */
    public abstract double get(int index);

    public final boolean hasArray() {
        return protectedHasArray();
    }

    /**
     * Calculates this buffer's hash code from the remaining chars. The
     * position, limit, capacity and mark don't affect the hash code.
     *
     * @return the hash code calculated from the remaining chars.
     */
    @Override
    public int hashCode() {
        int myPosition = position;
        int hash = 0;
        long l;
        while (myPosition < limit) {
            l = Double.doubleToLongBits(get(myPosition++));
            hash = hash + ((int) l) ^ ((int) (l >> 32));
        }
        return hash;
    }

    /**
     * Indicates whether this buffer is direct. A direct buffer will try its
     * best to take advantage of native memory APIs and it may not stay in the
     * Java heap, so it is not affected by garbage collection.
     * <p>
     * A double buffer is direct if it is based on a byte buffer and the byte
     * buffer is direct.
     *
     * @return {@code true} if this buffer is direct, {@code false} otherwise.
     */
    public abstract boolean isDirect();

    /**
     * Returns the byte order used by this buffer when converting doubles
     * from/to bytes.
     * <p>
     * If this buffer is not based on a byte buffer, then this always returns
     * the platform's native byte order.
     *
     * @return the byte order used by this buffer when converting doubles
     *         from/to bytes.
     */
    public abstract ByteOrder order();

    /**
     * Child class implements this method to realize {@code array()}.
     *
     * @see #array()
     */
    abstract double[] protectedArray();

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
     * Writes the given double to the current position and increases the
     * position by 1.
     *
     * @param d
     *            the double to write.
     * @return this buffer.
     * @throws BufferOverflowException
     *                if position is equal or greater than limit.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public abstract DoubleBuffer put(double d);

    /**
     * Writes doubles from the given double array to the current position and
     * increases the position by the number of doubles written.
     * <p>
     * Calling this method has the same effect as
     * {@code put(src, 0, src.length)}.
     *
     * @param src
     *            the source double array.
     * @return this buffer.
     * @throws BufferOverflowException
     *                if {@code remaining()} is less than {@code src.length}.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public final DoubleBuffer put(double[] src) {
        return put(src, 0, src.length);
    }

    /**
     * Writes doubles from the given double array, starting from the specified
     * offset, to the current position and increases the position by the number
     * of doubles written.
     *
     * @param src
     *            the source double array.
     * @param srcOffset
     *            the offset of double array, must not be negative and not
     *            greater than {@code src.length}.
     * @param doubleCount
     *            the number of doubles to write, must be no less than zero and
     *            not greater than {@code src.length - srcOffset}.
     * @return this buffer.
     * @throws BufferOverflowException
     *                if {@code remaining()} is less than {@code doubleCount}.
     * @throws IndexOutOfBoundsException
     *                if either {@code srcOffset} or {@code doubleCount} is invalid.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public DoubleBuffer put(double[] src, int srcOffset, int doubleCount) {
        Arrays.checkOffsetAndCount(src.length, srcOffset, doubleCount);
        if (doubleCount > remaining()) {
            throw new BufferOverflowException();
        }
        for (int i = srcOffset; i < srcOffset + doubleCount; ++i) {
            put(src[i]);
        }
        return this;
    }

    /**
     * Writes all the remaining doubles of the {@code src} double buffer to this
     * buffer's current position, and increases both buffers' position by the
     * number of doubles copied.
     *
     * @param src
     *            the source double buffer.
     * @return this buffer.
     * @throws BufferOverflowException
     *                if {@code src.remaining()} is greater than this buffer's
     *                {@code remaining()}.
     * @throws IllegalArgumentException
     *                if {@code src} is this buffer.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public DoubleBuffer put(DoubleBuffer src) {
        if (isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        if (src == this) {
            throw new IllegalArgumentException("src == this");
        }
        if (src.remaining() > remaining()) {
            throw new BufferOverflowException();
        }
        double[] doubles = new double[src.remaining()];
        src.get(doubles);
        put(doubles);
        return this;
    }

    /**
     * Write a double to the specified index of this buffer and the position is
     * not changed.
     *
     * @param index
     *            the index, must not be negative and less than the limit.
     * @param d
     *            the double to write.
     * @return this buffer.
     * @throws IndexOutOfBoundsException
     *                if index is invalid.
     * @throws ReadOnlyBufferException
     *                if no changes may be made to the contents of this buffer.
     */
    public abstract DoubleBuffer put(int index, double d);

    /**
     * Returns a sliced buffer that shares its content with this buffer.
     * <p>
     * The sliced buffer's capacity will be this buffer's {@code remaining()},
     * and its zero position will correspond to this buffer's current position.
     * The new buffer's position will be 0, limit will be its capacity, and its
     * mark is cleared. The new buffer's read-only property and byte order are
     * the same as this buffer's.
     * <p>
     * The new buffer shares its content with this buffer, which means either
     * buffer's change of content will be visible to the other. The two buffers'
     * position, limit and mark are independent.
     */
    public abstract DoubleBuffer slice();
}
