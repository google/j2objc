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

package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import libcore.io.SizeOf;

/**
 * The {@code BitSet} class implements a
 * <a href="http://en.wikipedia.org/wiki/Bit_array">bit array</a>.
 * Each element is either true or false. A {@code BitSet} is created with a given size and grows
 * automatically if this size is exceeded.
 */
public class BitSet implements Serializable, Cloneable {
    private static final long serialVersionUID = 7997698588986878753L;

    private static final long ALL_ONES = ~0L;

    /**
     * The bits. Access bit n thus:
     *
     *   boolean bit = (bits[n / 64] | (1 << n)) != 0;
     *
     * Note that Java's shift operators truncate their rhs to the log2 size of the lhs.
     * That is, there's no "% 64" needed because it's implicit in the shift.
     *
     * TODO: would int[] be significantly more efficient for Android at the moment?
     */
    private long[] bits;

    /**
     * The number of elements of 'bits' that are actually in use (non-zero). Amongst other
     * things, this guarantees that isEmpty is cheap, because we never have to examine the array.
     */
    private transient int longCount;

    /**
     * Updates 'longCount' by inspecting 'bits'. Assumes that the new longCount is <= the current
     * longCount, to avoid scanning large tracts of empty array. This means it's safe to call
     * directly after a clear operation that may have cleared the highest set bit, but
     * not safe after an xor operation that may have cleared the highest set bit or
     * made a new highest set bit. In that case, you'd need to set 'longCount' to a conservative
     * estimate before calling this method.
     */
    private void shrinkSize() {
        int i = longCount - 1;
        while (i >= 0 && bits[i] == 0) {
            --i;
        }
        this.longCount = i + 1;
    }

    /**
     * Creates a new {@code BitSet} with size equal to 64 bits.
     */
    public BitSet() {
        this(new long[1]);
    }

    /**
     * Creates a new {@code BitSet} with size equal to {@code bitCount}, rounded up to
     * a multiple of 64.
     *
     * @throws NegativeArraySizeException if {@code bitCount < 0}.
     */
    public BitSet(int bitCount) {
        if (bitCount < 0) {
            throw new NegativeArraySizeException(Integer.toString(bitCount));
        }
        this.bits = arrayForBits(bitCount);
        this.longCount = 0;
    }

    private BitSet(long[] bits) {
        this.bits = bits;
        this.longCount = bits.length;
        shrinkSize();
    }

    private static long[] arrayForBits(int bitCount) {
        return new long[(bitCount + 63)/ 64];
    }

    @Override public Object clone() {
        try {
            BitSet clone = (BitSet) super.clone();
            clone.bits = bits.clone();
            clone.shrinkSize();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BitSet)) {
            return false;
        }
        BitSet lhs = (BitSet) o;
        if (this.longCount != lhs.longCount) {
            return false;
        }
        for (int i = 0; i < longCount; ++i) {
            if (bits[i] != lhs.bits[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Ensures that our long[] can hold at least 64 * desiredLongCount bits.
     */
    private void ensureCapacity(int desiredLongCount) {
        if (desiredLongCount <= bits.length) {
            return;
        }
        int newLength = Math.max(desiredLongCount, bits.length * 2);
        long[] newBits = new long[newLength];
        System.arraycopy(bits, 0, newBits, 0, longCount);
        this.bits = newBits;
        // 'longCount' is unchanged by this operation: the long[] is larger,
        // but you're not yet using any more of it.
    }

    @Override public int hashCode() {
        // The RI doesn't use Arrays.hashCode, and explicitly specifies this algorithm.
        long x = 1234;
        for (int i = 0; i < longCount; ++i) {
            x ^= bits[i] * (i + 1);
        }
        return (int) ((x >> 32) ^ x);
    }

    /**
     * Returns the bit at index {@code index}. Indexes greater than the current length return false.
     *
     * @throws IndexOutOfBoundsException if {@code index < 0}.
     */
    public boolean get(int index) {
        if (index < 0) { // TODO: until we have an inlining JIT.
            checkIndex(index);
        }
        int arrayIndex = index / 64;
        if (arrayIndex >= longCount) {
            return false;
        }
        return (bits[arrayIndex] & (1L << index)) != 0;
    }

    /**
     * Sets the bit at index {@code index} to true.
     *
     * @throws IndexOutOfBoundsException if {@code index < 0}.
     */
    public void set(int index) {
        if (index < 0) { // TODO: until we have an inlining JIT.
            checkIndex(index);
        }
        int arrayIndex = index / 64;
        if (arrayIndex >= bits.length) {
            ensureCapacity(arrayIndex + 1);
        }
        bits[arrayIndex] |= (1L << index);
        longCount = Math.max(longCount, arrayIndex + 1);
    }

    /**
     * Clears the bit at index {@code index}.
     *
     * @throws IndexOutOfBoundsException if {@code index < 0}.
     */
    public void clear(int index) {
        if (index < 0) { // TODO: until we have an inlining JIT.
            checkIndex(index);
        }
        int arrayIndex = index / 64;
        if (arrayIndex >= longCount) {
            return;
        }
        bits[arrayIndex] &= ~(1L << index);
        shrinkSize();
    }

    /**
     * Flips the bit at index {@code index}.
     *
     * @throws IndexOutOfBoundsException if {@code index < 0}.
     */
    public void flip(int index) {
        if (index < 0) { // TODO: until we have an inlining JIT.
            checkIndex(index);
        }
        int arrayIndex = index / 64;
        if (arrayIndex >= bits.length) {
            ensureCapacity(arrayIndex + 1);
        }
        bits[arrayIndex] ^= (1L << index);
        longCount = Math.max(longCount, arrayIndex + 1);
        shrinkSize();
    }

    private void checkIndex(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("index < 0: " + index);
        }
    }

    private void checkRange(int fromIndex, int toIndex) {
        if ((fromIndex | toIndex) < 0 || toIndex < fromIndex) {
            throw new IndexOutOfBoundsException("fromIndex=" + fromIndex + " toIndex=" + toIndex);
        }
    }

    /**
     * Returns a new {@code BitSet} containing the
     * range of bits {@code [fromIndex, toIndex)}, shifted down so that the bit
     * at {@code fromIndex} is at bit 0 in the new {@code BitSet}.
     *
     * @throws IndexOutOfBoundsException
     *             if {@code fromIndex} or {@code toIndex} is negative, or if
     *             {@code toIndex} is smaller than {@code fromIndex}.
     */
    public BitSet get(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex);

        int last = 64 * longCount;
        if (fromIndex >= last || fromIndex == toIndex) {
            return new BitSet(0);
        }
        if (toIndex > last) {
            toIndex = last;
        }

        int firstArrayIndex = fromIndex / 64;
        int lastArrayIndex = (toIndex - 1) / 64;
        long lowMask = ALL_ONES << fromIndex;
        long highMask = ALL_ONES >>> -toIndex;

        if (firstArrayIndex == lastArrayIndex) {
            long result = (bits[firstArrayIndex] & (lowMask & highMask)) >>> fromIndex;
            if (result == 0) {
                return new BitSet(0);
            }
            return new BitSet(new long[] { result });
        }

        long[] newBits = new long[lastArrayIndex - firstArrayIndex + 1];

        // first fill in the first and last indexes in the new BitSet
        newBits[0] = bits[firstArrayIndex] & lowMask;
        newBits[newBits.length - 1] = bits[lastArrayIndex] & highMask;

        // fill in the in between elements of the new BitSet
        for (int i = 1; i < lastArrayIndex - firstArrayIndex; i++) {
            newBits[i] = bits[firstArrayIndex + i];
        }

        // shift all the elements in the new BitSet to the right
        int numBitsToShift = fromIndex % 64;
        int actualLen = newBits.length;
        if (numBitsToShift != 0) {
            for (int i = 0; i < newBits.length; i++) {
                // shift the current element to the right regardless of
                // sign
                newBits[i] = newBits[i] >>> (numBitsToShift);

                // apply the last x bits of newBits[i+1] to the current
                // element
                if (i != newBits.length - 1) {
                    newBits[i] |= newBits[i + 1] << -numBitsToShift;
                }
                if (newBits[i] != 0) {
                    actualLen = i + 1;
                }
            }
        }
        return new BitSet(newBits);
    }

    /**
     * Sets the bit at index {@code index} to {@code state}.
     *
     * @throws IndexOutOfBoundsException if {@code index < 0}.
     */
    public void set(int index, boolean state) {
        if (state) {
            set(index);
        } else {
            clear(index);
        }
    }

    /**
     * Sets the range of bits {@code [fromIndex, toIndex)} to {@code state}.
     *
     * @throws IndexOutOfBoundsException
     *             if {@code fromIndex} or {@code toIndex} is negative, or if
     *             {@code toIndex} is smaller than {@code fromIndex}.
     */
    public void set(int fromIndex, int toIndex, boolean state) {
        if (state) {
            set(fromIndex, toIndex);
        } else {
            clear(fromIndex, toIndex);
        }
    }

    /**
     * Clears all the bits in this {@code BitSet}. This method does not change the capacity.
     * Use {@code clear} if you want to reuse this {@code BitSet} with the same capacity, but
     * create a new {@code BitSet} if you're trying to potentially reclaim memory.
     */
    public void clear() {
        Arrays.fill(bits, 0, longCount, 0L);
        longCount = 0;
    }

    /**
     * Sets the range of bits {@code [fromIndex, toIndex)}.
     *
     * @throws IndexOutOfBoundsException
     *             if {@code fromIndex} or {@code toIndex} is negative, or if
     *             {@code toIndex} is smaller than {@code fromIndex}.
     */
    public void set(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex);
        if (fromIndex == toIndex) {
            return;
        }
        int firstArrayIndex = fromIndex / 64;
        int lastArrayIndex = (toIndex - 1) / 64;
        if (lastArrayIndex >= bits.length) {
            ensureCapacity(lastArrayIndex + 1);
        }

        long lowMask = ALL_ONES << fromIndex;
        long highMask = ALL_ONES >>> -toIndex;
        if (firstArrayIndex == lastArrayIndex) {
            bits[firstArrayIndex] |= (lowMask & highMask);
        } else {
            int i = firstArrayIndex;
            bits[i++] |= lowMask;
            while (i < lastArrayIndex) {
                bits[i++] |= ALL_ONES;
            }
            bits[i++] |= highMask;
        }
        longCount = Math.max(longCount, lastArrayIndex + 1);
    }

    /**
     * Clears the range of bits {@code [fromIndex, toIndex)}.
     *
     * @throws IndexOutOfBoundsException
     *             if {@code fromIndex} or {@code toIndex} is negative, or if
     *             {@code toIndex} is smaller than {@code fromIndex}.
     */
    public void clear(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex);
        if (fromIndex == toIndex || longCount == 0) {
            return;
        }
        int last = 64 * longCount;
        if (fromIndex >= last) {
            return;
        }
        if (toIndex > last) {
            toIndex = last;
        }
        int firstArrayIndex = fromIndex / 64;
        int lastArrayIndex = (toIndex - 1) / 64;

        long lowMask = ALL_ONES << fromIndex;
        long highMask = ALL_ONES >>> -toIndex;
        if (firstArrayIndex == lastArrayIndex) {
            bits[firstArrayIndex] &= ~(lowMask & highMask);
        } else {
            int i = firstArrayIndex;
            bits[i++] &= ~lowMask;
            while (i < lastArrayIndex) {
                bits[i++] = 0L;
            }
            bits[i++] &= ~highMask;
        }
        shrinkSize();
    }

    /**
     * Flips the range of bits {@code [fromIndex, toIndex)}.
     *
     * @throws IndexOutOfBoundsException
     *             if {@code fromIndex} or {@code toIndex} is negative, or if
     *             {@code toIndex} is smaller than {@code fromIndex}.
     */
    public void flip(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex);
        if (fromIndex == toIndex) {
            return;
        }
        int firstArrayIndex = fromIndex / 64;
        int lastArrayIndex = (toIndex - 1) / 64;
        if (lastArrayIndex >= bits.length) {
            ensureCapacity(lastArrayIndex + 1);
        }

        long lowMask = ALL_ONES << fromIndex;
        long highMask = ALL_ONES >>> -toIndex;
        if (firstArrayIndex == lastArrayIndex) {
            bits[firstArrayIndex] ^= (lowMask & highMask);
        } else {
            int i = firstArrayIndex;
            bits[i++] ^= lowMask;
            while (i < lastArrayIndex) {
                bits[i++] ^= ALL_ONES;
            }
            bits[i++] ^= highMask;
        }
        longCount = Math.max(longCount, lastArrayIndex + 1);
        shrinkSize();
    }

    /**
     * Returns true if {@code this.and(bs)} is non-empty, but may be faster than computing that.
     */
    public boolean intersects(BitSet bs) {
        long[] bsBits = bs.bits;
        int length = Math.min(this.longCount, bs.longCount);
        for (int i = 0; i < length; ++i) {
            if ((bits[i] & bsBits[i]) != 0L) {
                return true;
            }
        }
        return false;
    }

    /**
     * Logically ands the bits of this {@code BitSet} with {@code bs}.
     */
    public void and(BitSet bs) {
        int minSize = Math.min(this.longCount, bs.longCount);
        for (int i = 0; i < minSize; ++i) {
            bits[i] &= bs.bits[i];
        }
        Arrays.fill(bits, minSize, longCount, 0L);
        shrinkSize();
    }

    /**
     * Clears all bits in this {@code BitSet} which are also set in {@code bs}.
     */
    public void andNot(BitSet bs) {
        int minSize = Math.min(this.longCount, bs.longCount);
        for (int i = 0; i < minSize; ++i) {
            bits[i] &= ~bs.bits[i];
        }
        shrinkSize();
    }

    /**
     * Logically ors the bits of this {@code BitSet} with {@code bs}.
     */
    public void or(BitSet bs) {
        int minSize = Math.min(this.longCount, bs.longCount);
        int maxSize = Math.max(this.longCount, bs.longCount);
        ensureCapacity(maxSize);
        for (int i = 0; i < minSize; ++i) {
            bits[i] |= bs.bits[i];
        }
        if (bs.longCount > minSize) {
            System.arraycopy(bs.bits, minSize, bits, minSize, maxSize - minSize);
        }
        longCount = maxSize;
    }

    /**
     * Logically xors the bits of this {@code BitSet} with {@code bs}.
     */
    public void xor(BitSet bs) {
        int minSize = Math.min(this.longCount, bs.longCount);
        int maxSize = Math.max(this.longCount, bs.longCount);
        ensureCapacity(maxSize);
        for (int i = 0; i < minSize; ++i) {
            bits[i] ^= bs.bits[i];
        }
        if (bs.longCount > minSize) {
            System.arraycopy(bs.bits, minSize, bits, minSize, maxSize - minSize);
        }
        longCount = maxSize;
        shrinkSize();
    }

    /**
     * Returns the capacity in bits of the array implementing this {@code BitSet}. This is
     * unrelated to the length of the {@code BitSet}, and not generally useful.
     * Use {@link #nextSetBit} to iterate, or {@link #length} to find the highest set bit.
     */
    public int size() {
        return bits.length * 64;
    }

    /**
     * Returns the number of bits up to and including the highest bit set. This is unrelated to
     * the {@link #size} of the {@code BitSet}.
     */
    public int length() {
        if (longCount == 0) {
            return 0;
        }
        return 64 * (longCount - 1) + (64 - Long.numberOfLeadingZeros(bits[longCount - 1]));
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * receiver: a comma-delimited list of the indexes of all set bits.
     * For example: {@code "{0,1,8}"}.
     */
    @Override public String toString() {
        //System.err.println("BitSet[longCount=" + longCount + ",bits=" + Arrays.toString(bits) + "]");
        StringBuilder sb = new StringBuilder(longCount / 2);
        sb.append('{');
        boolean comma = false;
        for (int i = 0; i < longCount; ++i) {
            if (bits[i] != 0) {
                for (int j = 0; j < 64; ++j) {
                    if ((bits[i] & 1L << j) != 0) {
                        if (comma) {
                            sb.append(", ");
                        } else {
                            comma = true;
                        }
                        sb.append(64 * i + j);
                    }
                }
            }
        }
        sb.append('}');
        return sb.toString();
    }

    /**
     * Returns the index of the first bit that is set on or after {@code index}, or -1
     * if no higher bits are set.
     * @throws IndexOutOfBoundsException if {@code index < 0}.
     */
    public int nextSetBit(int index) {
        checkIndex(index);
        int arrayIndex = index / 64;
        if (arrayIndex >= longCount) {
            return -1;
        }
        long mask = ALL_ONES << index;
        if ((bits[arrayIndex] & mask) != 0) {
            return 64 * arrayIndex + Long.numberOfTrailingZeros(bits[arrayIndex] & mask);
        }
        while (++arrayIndex < longCount && bits[arrayIndex] == 0) {
        }
        if (arrayIndex == longCount) {
            return -1;
        }
        return 64 * arrayIndex + Long.numberOfTrailingZeros(bits[arrayIndex]);
    }

    /**
     * Returns the index of the first bit that is clear on or after {@code index}.
     * Since all bits past the end are implicitly clear, this never returns -1.
     * @throws IndexOutOfBoundsException if {@code index < 0}.
     */
    public int nextClearBit(int index) {
        checkIndex(index);
        int arrayIndex = index / 64;
        if (arrayIndex >= longCount) {
            return index;
        }
        long mask = ALL_ONES << index;
        if ((~bits[arrayIndex] & mask) != 0) {
            return 64 * arrayIndex + Long.numberOfTrailingZeros(~bits[arrayIndex] & mask);
        }
        while (++arrayIndex < longCount && bits[arrayIndex] == ALL_ONES) {
        }
        if (arrayIndex == longCount) {
            return 64 * longCount;
        }
        return 64 * arrayIndex + Long.numberOfTrailingZeros(~bits[arrayIndex]);
    }

    /**
     * Returns the index of the first bit that is set on or before {@code index}, or -1 if
     * no lower bits are set or {@code index == -1}.
     * @throws IndexOutOfBoundsException if {@code index < -1}.
     * @since 1.7
     */
    public int previousSetBit(int index) {
        if (index == -1) {
            return -1;
        }
        checkIndex(index);
        // TODO: optimize this.
        for (int i = index; i >= 0; --i) {
            if (get(i)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the index of the first bit that is clear on or before {@code index}, or -1 if
     * no lower bits are clear or {@code index == -1}.
     * @throws IndexOutOfBoundsException if {@code index < -1}.
     * @since 1.7
     */
    public int previousClearBit(int index) {
        if (index == -1) {
            return -1;
        }
        checkIndex(index);
        // TODO: optimize this.
        for (int i = index; i >= 0; --i) {
            if (!get(i)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns true if all the bits in this {@code BitSet} are set to false, false otherwise.
     */
    public boolean isEmpty() {
        return (longCount == 0);
    }

    /**
     * Returns the number of bits that are {@code true} in this {@code BitSet}.
     */
    public int cardinality() {
        int result = 0;
        for (int i = 0; i < longCount; ++i) {
            result += Long.bitCount(bits[i]);
        }
        return result;
    }

    /**
     * Equivalent to {@code BitSet.valueOf(LongBuffer.wrap(longs))}, but likely to be faster.
     * This is likely to be the fastest way to create a {@code BitSet} because it's closest
     * to the internal representation.
     * @since 1.7
     */
    public static BitSet valueOf(long[] longs) {
        return new BitSet(longs.clone());
    }

    /**
     * Returns a {@code BitSet} corresponding to {@code longBuffer}, interpreted as a little-endian
     * sequence of bits. This method does not alter the {@code LongBuffer}.
     * @since 1.7
     */
    public static BitSet valueOf(LongBuffer longBuffer) {
        // The bulk get would mutate LongBuffer (even if we reset position later), and it's not
        // clear that's allowed. My assumption is that it's the long[] variant that's the common
        // case anyway, so copy the buffer into a long[].
        long[] longs = new long[longBuffer.remaining()];
        for (int i = 0; i < longs.length; ++i) {
            longs[i] = longBuffer.get(longBuffer.position() + i);
        }
        return BitSet.valueOf(longs);
    }

    /**
     * Equivalent to {@code BitSet.valueOf(ByteBuffer.wrap(bytes))}.
     * @since 1.7
     */
    public static BitSet valueOf(byte[] bytes) {
        return BitSet.valueOf(ByteBuffer.wrap(bytes));
    }

    /**
     * Returns a {@code BitSet} corresponding to {@code byteBuffer}, interpreted as a little-endian
     * sequence of bits. This method does not alter the {@code ByteBuffer}.
     * @since 1.7
     */
    public static BitSet valueOf(ByteBuffer byteBuffer) {
        byteBuffer = byteBuffer.slice().order(ByteOrder.LITTLE_ENDIAN);
        long[] longs = arrayForBits(byteBuffer.remaining() * 8);
        int i = 0;
        while (byteBuffer.remaining() >= SizeOf.LONG) {
            longs[i++] = byteBuffer.getLong();
        }
        for (int j = 0; byteBuffer.hasRemaining(); ++j) {
            longs[i] |= ((((long) byteBuffer.get()) & 0xff) << (8*j));
        }
        return BitSet.valueOf(longs);
    }

    /**
     * Returns a new {@code long[]} containing a little-endian representation of the bits of
     * this {@code BitSet}, suitable for passing to {@code valueOf} to reconstruct
     * this {@code BitSet}.
     * @since 1.7
     */
    public long[] toLongArray() {
        return Arrays.copyOf(bits, longCount);
    }

    /**
     * Returns a new {@code byte[]} containing a little-endian representation the bits of
     * this {@code BitSet}, suitable for passing to {@code valueOf} to reconstruct
     * this {@code BitSet}.
     * @since 1.7
     */
    public byte[] toByteArray() {
        int bitCount = length();
        byte[] result = new byte[(bitCount + 7)/ 8];
        for (int i = 0; i < result.length; ++i) {
            int lowBit = 8 * i;
            int arrayIndex = lowBit / 64;
            result[i] = (byte) (bits[arrayIndex] >>> lowBit);
        }
        return result;
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        // The serialized form doesn't include a 'longCount' field, so we'll have to scan the array.
        this.longCount = this.bits.length;
        shrinkSize();
    }
}
