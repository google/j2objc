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

import java.io.Serializable;

/**
 * The {@code BitSet} class implements a bit field. Each element in a
 * {@code BitSet} can be on(1) or off(0). A {@code BitSet} is created with a
 * given size and grows if this size is exceeded. Growth is always rounded to a
 * 64 bit boundary.
 */
public class BitSet implements Serializable, Cloneable {
    private static final long serialVersionUID = 7997698588986878753L;

    private static final int OFFSET = 6;

    private static final int ELM_SIZE = 1 << OFFSET;

    private static final int RIGHT_BITS = ELM_SIZE - 1;

    private static final long[] TWO_N_ARRAY = new long[] { 0x1L, 0x2L, 0x4L,
            0x8L, 0x10L, 0x20L, 0x40L, 0x80L, 0x100L, 0x200L, 0x400L, 0x800L,
            0x1000L, 0x2000L, 0x4000L, 0x8000L, 0x10000L, 0x20000L, 0x40000L,
            0x80000L, 0x100000L, 0x200000L, 0x400000L, 0x800000L, 0x1000000L,
            0x2000000L, 0x4000000L, 0x8000000L, 0x10000000L, 0x20000000L,
            0x40000000L, 0x80000000L, 0x100000000L, 0x200000000L, 0x400000000L,
            0x800000000L, 0x1000000000L, 0x2000000000L, 0x4000000000L,
            0x8000000000L, 0x10000000000L, 0x20000000000L, 0x40000000000L,
            0x80000000000L, 0x100000000000L, 0x200000000000L, 0x400000000000L,
            0x800000000000L, 0x1000000000000L, 0x2000000000000L,
            0x4000000000000L, 0x8000000000000L, 0x10000000000000L,
            0x20000000000000L, 0x40000000000000L, 0x80000000000000L,
            0x100000000000000L, 0x200000000000000L, 0x400000000000000L,
            0x800000000000000L, 0x1000000000000000L, 0x2000000000000000L,
            0x4000000000000000L, 0x8000000000000000L };

    private long[] bits;

    private transient boolean needClear;

    private transient int actualArrayLength;

    private transient boolean isLengthActual;

    /**
     * Create a new {@code BitSet} with size equal to 64 bits.
     * 
     * @see #clear(int)
     * @see #set(int)
     * @see #clear()
     * @see #clear(int, int)
     * @see #set(int, boolean)
     * @see #set(int, int)
     * @see #set(int, int, boolean)
     */
    public BitSet() {
        bits = new long[1];
        actualArrayLength = 0;
        isLengthActual = true;
    }

    /**
     * Create a new {@code BitSet} with size equal to nbits. If nbits is not a
     * multiple of 64, then create a {@code BitSet} with size nbits rounded to
     * the next closest multiple of 64.
     * 
     * @param nbits
     *            the size of the bit set.
     * @throws NegativeArraySizeException
     *             if {@code nbits} is negative.
     * @see #clear(int)
     * @see #set(int)
     * @see #clear()
     * @see #clear(int, int)
     * @see #set(int, boolean)
     * @see #set(int, int)
     * @see #set(int, int, boolean)
     */
    public BitSet(int nbits) {
        if (nbits < 0) {
            throw new NegativeArraySizeException();
        }
        bits = new long[(nbits >> OFFSET) + ((nbits & RIGHT_BITS) > 0 ? 1 : 0)];
        actualArrayLength = 0;
        isLengthActual = true;
    }

    /**
     * Private constructor called from get(int, int) method
     * 
     * @param bits
     *            the size of the bit set
     */
    private BitSet(long[] bits, boolean needClear, int actualArrayLength,
            boolean isLengthActual) {
        this.bits = bits;
        this.needClear = needClear;
        this.actualArrayLength = actualArrayLength;
        this.isLengthActual = isLengthActual;
    }

    /**
     * Creates a copy of this {@code BitSet}.
     * 
     * @return a copy of this {@code BitSet}.
     */
    @Override
    public Object clone() {
        BitSet clone = new BitSet();
        clone.actualArrayLength = actualArrayLength;
        clone.bits = bits.clone();
        clone.isLengthActual = isLengthActual;
        clone.needClear = needClear;
        return clone;
    }

    /**
     * Compares the argument to this {@code BitSet} and returns whether they are
     * equal. The object must be an instance of {@code BitSet} with the same
     * bits set.
     * 
     * @param obj
     *            the {@code BitSet} object to compare.
     * @return a {@code boolean} indicating whether or not this {@code BitSet} and
     *         {@code obj} are equal.
     * @see #hashCode
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof BitSet) {
            long[] bsBits = ((BitSet) obj).bits;
            int length1 = this.actualArrayLength, length2 = ((BitSet) obj).actualArrayLength;
            if (this.isLengthActual && ((BitSet) obj).isLengthActual
                    && length1 != length2) {
                return false;
            }
            // If one of the BitSets is larger than the other, check to see if
            // any of its extra bits are set. If so return false.
            if (length1 <= length2) {
                for (int i = 0; i < length1; i++) {
                    if (bits[i] != bsBits[i]) {
                        return false;
                    }
                }
                for (int i = length1; i < length2; i++) {
                    if (bsBits[i] != 0) {
                        return false;
                    }
                }
            } else {
                for (int i = 0; i < length2; i++) {
                    if (bits[i] != bsBits[i]) {
                        return false;
                    }
                }
                for (int i = length2; i < length1; i++) {
                    if (bits[i] != 0) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Increase the size of the internal array to accommodate {@code pos} bits.
     * The new array max index will be a multiple of 64.
     * 
     * @param len
     *            the index the new array needs to be able to access.
     */
    private final void growLength(int len) {
        long[] tempBits = new long[Math.max(len, bits.length * 2)];
        System.arraycopy(bits, 0, tempBits, 0, this.actualArrayLength);
        bits = tempBits;
    }

    /**
     * Computes the hash code for this {@code BitSet}. If two {@code BitSet}s are equal
     * the have to return the same result for {@code hashCode()}.
     * 
     * @return the {@code int} representing the hash code for this bit
     *         set.
     * @see #equals
     * @see java.util.Hashtable
     */
    @Override
    public int hashCode() {
        long x = 1234;
        for (int i = 0, length = actualArrayLength; i < length; i++) {
            x ^= bits[i] * (i + 1);
        }
        return (int) ((x >> 32) ^ x);
    }

    /**
     * Retrieves the bit at index {@code pos}. Grows the {@code BitSet} if
     * {@code pos > size}.
     * 
     * @param pos
     *            the index of the bit to be retrieved.
     * @return {@code true} if the bit at {@code pos} is set,
     *         {@code false} otherwise.
     * @throws IndexOutOfBoundsException
     *             if {@code pos} is negative.
     * @see #clear(int)
     * @see #set(int)
     * @see #clear()
     * @see #clear(int, int)
     * @see #set(int, boolean)
     * @see #set(int, int)
     * @see #set(int, int, boolean)
     */
    public boolean get(int pos) {
        if (pos < 0) {
            // Negative index specified
            throw new IndexOutOfBoundsException("Negative index specified");
        }

        int arrayPos = pos >> OFFSET;
        if (arrayPos < actualArrayLength) {
            return (bits[arrayPos] & TWO_N_ARRAY[pos & RIGHT_BITS]) != 0;
        }
        return false;
    }

    /**
     * Retrieves the bits starting from {@code pos1} to {@code pos2} and returns
     * back a new bitset made of these bits. Grows the {@code BitSet} if
     * {@code pos2 > size}.
     * 
     * @param pos1
     *            beginning position.
     * @param pos2
     *            ending position.
     * @return new bitset of the range specified.
     * @throws IndexOutOfBoundsException
     *             if {@code pos1} or {@code pos2} is negative, or if
     *             {@code pos2} is smaller than {@code pos1}.
     * @see #get(int)
     */
    public BitSet get(int pos1, int pos2) {
        if (pos1 < 0 || pos2 < 0 || pos2 < pos1) {
            throw new IndexOutOfBoundsException("Negative index specified");
        }

        int last = actualArrayLength << OFFSET;
        if (pos1 >= last || pos1 == pos2) {
            return new BitSet(0);
        }
        if (pos2 > last) {
            pos2 = last;
        }

        int idx1 = pos1 >> OFFSET;
        int idx2 = (pos2 - 1) >> OFFSET;
        long factor1 = (~0L) << (pos1 & RIGHT_BITS);
        long factor2 = (~0L) >>> (ELM_SIZE - (pos2 & RIGHT_BITS));

        if (idx1 == idx2) {
            long result = (bits[idx1] & (factor1 & factor2)) >>> (pos1 % ELM_SIZE);
            if (result == 0) {
                return new BitSet(0);
            }
            return new BitSet(new long[] { result }, needClear, 1, true);
        }
        long[] newbits = new long[idx2 - idx1 + 1];
        // first fill in the first and last indexes in the new bitset
        newbits[0] = bits[idx1] & factor1;
        newbits[newbits.length - 1] = bits[idx2] & factor2;

        // fill in the in between elements of the new bitset
        for (int i = 1; i < idx2 - idx1; i++) {
            newbits[i] = bits[idx1 + i];
        }

        // shift all the elements in the new bitset to the right by pos1
        // % ELM_SIZE
        int numBitsToShift = pos1 & RIGHT_BITS;
        int actualLen = newbits.length;
        if (numBitsToShift != 0) {
            for (int i = 0; i < newbits.length; i++) {
                // shift the current element to the right regardless of
                // sign
                newbits[i] = newbits[i] >>> (numBitsToShift);

                // apply the last x bits of newbits[i+1] to the current
                // element
                if (i != newbits.length - 1) {
                    newbits[i] |= newbits[i + 1] << (ELM_SIZE - (numBitsToShift));
                }
                if (newbits[i] != 0) {
                    actualLen = i + 1;
                }
            }
        }
        return new BitSet(newbits, needClear, actualLen,
                newbits[actualLen - 1] != 0);
    }

    /**
     * Sets the bit at index {@code pos} to 1. Grows the {@code BitSet} if
     * {@code pos > size}.
     * 
     * @param pos
     *            the index of the bit to set.
     * @throws IndexOutOfBoundsException
     *             if {@code pos} is negative.
     * @see #clear(int)
     * @see #clear()
     * @see #clear(int, int)
     */
    public void set(int pos) {
        if (pos < 0) {
            throw new IndexOutOfBoundsException("Negative index specified");
        }

        int len = (pos >> OFFSET) + 1;
        if (len > bits.length) {
            growLength(len);
        }
        bits[len - 1] |= TWO_N_ARRAY[pos & RIGHT_BITS];
        if (len > actualArrayLength) {
            actualArrayLength = len;
            isLengthActual = true;
        }
        needClear();
    }

    /**
     * Sets the bit at index {@code pos} to {@code val}. Grows the
     * {@code BitSet} if {@code pos > size}.
     * 
     * @param pos
     *            the index of the bit to set.
     * @param val
     *            value to set the bit.
     * @throws IndexOutOfBoundsException
     *             if {@code pos} is negative.
     * @see #set(int)
     */
    public void set(int pos, boolean val) {
        if (val) {
            set(pos);
        } else {
            clear(pos);
        }
    }

    /**
     * Sets the bits starting from {@code pos1} to {@code pos2}. Grows the
     * {@code BitSet} if {@code pos2 > size}.
     * 
     * @param pos1
     *            beginning position.
     * @param pos2
     *            ending position.
     * @throws IndexOutOfBoundsException
     *             if {@code pos1} or {@code pos2} is negative, or if
     *             {@code pos2} is smaller than {@code pos1}.
     * @see #set(int)
     */
    public void set(int pos1, int pos2) {
        if (pos1 < 0 || pos2 < 0 || pos2 < pos1) {
            throw new IndexOutOfBoundsException("Negative index specified");
        }

        if (pos1 == pos2) {
            return;
        }
        int len2 = ((pos2 - 1) >> OFFSET) + 1;
        if (len2 > bits.length) {
            growLength(len2);
        }

        int idx1 = pos1 >> OFFSET;
        int idx2 = (pos2 - 1) >> OFFSET;
        long factor1 = (~0L) << (pos1 & RIGHT_BITS);
        long factor2 = (~0L) >>> (ELM_SIZE - (pos2 & RIGHT_BITS));

        if (idx1 == idx2) {
            bits[idx1] |= (factor1 & factor2);
        } else {
            bits[idx1] |= factor1;
            bits[idx2] |= factor2;
            for (int i = idx1 + 1; i < idx2; i++) {
                bits[i] |= (~0L);
            }
        }
        if (idx2 + 1 > actualArrayLength) {
            actualArrayLength = idx2 + 1;
            isLengthActual = true;
        }
        needClear();
    }

    private void needClear() {
        this.needClear = true;
    }

    /**
     * Sets the bits starting from {@code pos1} to {@code pos2} to the given
     * {@code val}. Grows the {@code BitSet} if {@code pos2 > size}.
     * 
     * @param pos1
     *            beginning position.
     * @param pos2
     *            ending position.
     * @param val
     *            value to set these bits.
     * @throws IndexOutOfBoundsException
     *             if {@code pos1} or {@code pos2} is negative, or if
     *             {@code pos2} is smaller than {@code pos1}.
     * @see #set(int,int)
     */
    public void set(int pos1, int pos2, boolean val) {
        if (val) {
            set(pos1, pos2);
        } else {
            clear(pos1, pos2);
        }
    }

    /**
     * Clears all the bits in this {@code BitSet}.
     * 
     * @see #clear(int)
     * @see #clear(int, int)
     */
    public void clear() {
        if (needClear) {
            for (int i = 0; i < bits.length; i++) {
                bits[i] = 0L;
            }
            actualArrayLength = 0;
            isLengthActual = true;
            needClear = false;
        }
    }

    /**
     * Clears the bit at index {@code pos}. Grows the {@code BitSet} if
     * {@code pos > size}.
     * 
     * @param pos
     *            the index of the bit to clear.
     * @throws IndexOutOfBoundsException
     *             if {@code pos} is negative.
     * @see #clear(int, int)
     */
    public void clear(int pos) {
        if (pos < 0) {
            // Negative index specified
            throw new IndexOutOfBoundsException("Negative index specified");
        }

        if (!needClear) {
            return;
        }
        int arrayPos = pos >> OFFSET;
        if (arrayPos < actualArrayLength) {
            bits[arrayPos] &= ~(TWO_N_ARRAY[pos & RIGHT_BITS]);
            if (bits[actualArrayLength - 1] == 0) {
                isLengthActual = false;
            }
        }
    }

    /**
     * Clears the bits starting from {@code pos1} to {@code pos2}. Grows the
     * {@code BitSet} if {@code pos2 > size}.
     * 
     * @param pos1
     *            beginning position.
     * @param pos2
     *            ending position.
     * @throws IndexOutOfBoundsException
     *             if {@code pos1} or {@code pos2} is negative, or if
     *             {@code pos2} is smaller than {@code pos1}.
     * @see #clear(int)
     */
    public void clear(int pos1, int pos2) {
        if (pos1 < 0 || pos2 < 0 || pos2 < pos1) {
            throw new IndexOutOfBoundsException("Negative index specified");
        }

        if (!needClear) {
            return;
        }
        int last = (actualArrayLength << OFFSET);
        if (pos1 >= last || pos1 == pos2) {
            return;
        }
        if (pos2 > last) {
            pos2 = last;
        }

        int idx1 = pos1 >> OFFSET;
        int idx2 = (pos2 - 1) >> OFFSET;
        long factor1 = (~0L) << (pos1 & RIGHT_BITS);
        long factor2 = (~0L) >>> (ELM_SIZE - (pos2 & RIGHT_BITS));

        if (idx1 == idx2) {
            bits[idx1] &= ~(factor1 & factor2);
        } else {
            bits[idx1] &= ~factor1;
            bits[idx2] &= ~factor2;
            for (int i = idx1 + 1; i < idx2; i++) {
                bits[i] = 0L;
            }
        }
        if ((actualArrayLength > 0) && (bits[actualArrayLength - 1] == 0)) {
            isLengthActual = false;
        }
    }

    /**
     * Flips the bit at index {@code pos}. Grows the {@code BitSet} if
     * {@code pos > size}.
     * 
     * @param pos
     *            the index of the bit to flip.
     * @throws IndexOutOfBoundsException
     *             if {@code pos} is negative.
     * @see #flip(int, int)
     */
    public void flip(int pos) {
        if (pos < 0) {
            throw new IndexOutOfBoundsException("Negative index specified");
        }

        int len = (pos >> OFFSET) + 1;
        if (len > bits.length) {
            growLength(len);
        }
        bits[len - 1] ^= TWO_N_ARRAY[pos & RIGHT_BITS];
        if (len > actualArrayLength) {
            actualArrayLength = len;
        }
        isLengthActual = !((actualArrayLength > 0) && (bits[actualArrayLength - 1] == 0));
        needClear();
    }

    /**
     * Flips the bits starting from {@code pos1} to {@code pos2}. Grows the
     * {@code BitSet} if {@code pos2 > size}.
     * 
     * @param pos1
     *            beginning position.
     * @param pos2
     *            ending position.
     * @throws IndexOutOfBoundsException
     *             if {@code pos1} or {@code pos2} is negative, or if
     *             {@code pos2} is smaller than {@code pos1}.
     * @see #flip(int)
     */
    public void flip(int pos1, int pos2) {
        if (pos1 < 0 || pos2 < 0 || pos2 < pos1) {
            throw new IndexOutOfBoundsException("Negative index specified");
        }

        if (pos1 == pos2) {
            return;
        }
        int len2 = ((pos2 - 1) >> OFFSET) + 1;
        if (len2 > bits.length) {
            growLength(len2);
        }

        int idx1 = pos1 >> OFFSET;
        int idx2 = (pos2 - 1) >> OFFSET;
        long factor1 = (~0L) << (pos1 & RIGHT_BITS);
        long factor2 = (~0L) >>> (ELM_SIZE - (pos2 & RIGHT_BITS));

        if (idx1 == idx2) {
            bits[idx1] ^= (factor1 & factor2);
        } else {
            bits[idx1] ^= factor1;
            bits[idx2] ^= factor2;
            for (int i = idx1 + 1; i < idx2; i++) {
                bits[i] ^= (~0L);
            }
        }
        if (len2 > actualArrayLength) {
            actualArrayLength = len2;
        }
        isLengthActual = !((actualArrayLength > 0) && (bits[actualArrayLength - 1] == 0));
        needClear();
    }

    /**
     * Checks if these two {@code BitSet}s have at least one bit set to true in the same
     * position.
     * 
     * @param bs
     *            {@code BitSet} used to calculate the intersection.
     * @return {@code true} if bs intersects with this {@code BitSet},
     *         {@code false} otherwise.
     */
    public boolean intersects(BitSet bs) {
        long[] bsBits = bs.bits;
        int length1 = actualArrayLength, length2 = bs.actualArrayLength;

        if (length1 <= length2) {
            for (int i = 0; i < length1; i++) {
                if ((bits[i] & bsBits[i]) != 0L) {
                    return true;
                }
            }
        } else {
            for (int i = 0; i < length2; i++) {
                if ((bits[i] & bsBits[i]) != 0L) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Performs the logical AND of this {@code BitSet} with another
     * {@code BitSet}. The values of this {@code BitSet} are changed accordingly.
     * 
     * @param bs
     *            {@code BitSet} to AND with.
     * @see #or
     * @see #xor
     */
    public void and(BitSet bs) {
        long[] bsBits = bs.bits;
        if (!needClear) {
            return;
        }
        int length1 = actualArrayLength, length2 = bs.actualArrayLength;
        if (length1 <= length2) {
            for (int i = 0; i < length1; i++) {
                bits[i] &= bsBits[i];
            }
        } else {
            for (int i = 0; i < length2; i++) {
                bits[i] &= bsBits[i];
            }
            for (int i = length2; i < length1; i++) {
                bits[i] = 0;
            }
            actualArrayLength = length2;
        }
        isLengthActual = !((actualArrayLength > 0) && (bits[actualArrayLength - 1] == 0));
    }

    /**
     * Clears all bits in the receiver which are also set in the parameter
     * {@code BitSet}. The values of this {@code BitSet} are changed accordingly.
     * 
     * @param bs
     *            {@code BitSet} to ANDNOT with.
     */
    public void andNot(BitSet bs) {
        long[] bsBits = bs.bits;
        if (!needClear) {
            return;
        }
        int range = actualArrayLength < bs.actualArrayLength ? actualArrayLength
                : bs.actualArrayLength;
        for (int i = 0; i < range; i++) {
            bits[i] &= ~bsBits[i];
        }

        if (actualArrayLength < range) {
            actualArrayLength = range;
        }
        isLengthActual = !((actualArrayLength > 0) && (bits[actualArrayLength - 1] == 0));
    }

    /**
     * Performs the logical OR of this {@code BitSet} with another {@code BitSet}.
     * The values of this {@code BitSet} are changed accordingly.
     *
     * @param bs
     *            {@code BitSet} to OR with.
     * @see #xor
     * @see #and
     */
    public void or(BitSet bs) {
        int bsActualLen = bs.getActualArrayLength();
        if (bsActualLen > bits.length) {
            long[] tempBits = new long[bsActualLen];
            System.arraycopy(bs.bits, 0, tempBits, 0, bs.actualArrayLength);
            for (int i = 0; i < actualArrayLength; i++) {
                tempBits[i] |= bits[i];
            }
            bits = tempBits;
            actualArrayLength = bsActualLen;
            isLengthActual = true;
        } else {
            long[] bsBits = bs.bits;
            for (int i = 0; i < bsActualLen; i++) {
                bits[i] |= bsBits[i];
            }
            if (bsActualLen > actualArrayLength) {
                actualArrayLength = bsActualLen;
                isLengthActual = true;
            }
        }
        needClear();
    }

    /**
     * Performs the logical XOR of this {@code BitSet} with another {@code BitSet}.
     * The values of this {@code BitSet} are changed accordingly.
     *
     * @param bs
     *            {@code BitSet} to XOR with.
     * @see #or
     * @see #and
     */
    public void xor(BitSet bs) {
        int bsActualLen = bs.getActualArrayLength();
        if (bsActualLen > bits.length) {
            long[] tempBits = new long[bsActualLen];
            System.arraycopy(bs.bits, 0, tempBits, 0, bs.actualArrayLength);
            for (int i = 0; i < actualArrayLength; i++) {
                tempBits[i] ^= bits[i];
            }
            bits = tempBits;
            actualArrayLength = bsActualLen;
            isLengthActual = !((actualArrayLength > 0) && (bits[actualArrayLength - 1] == 0));
        } else {
            long[] bsBits = bs.bits;
            for (int i = 0; i < bsActualLen; i++) {
                bits[i] ^= bsBits[i];
            }
            if (bsActualLen > actualArrayLength) {
                actualArrayLength = bsActualLen;
                isLengthActual = true;
            }
        }
        needClear();
    }

    /**
     * Returns the number of bits this {@code BitSet} has.
     * 
     * @return the number of bits contained in this {@code BitSet}.
     * @see #length
     */
    public int size() {
        return bits.length << OFFSET;
    }

    /**
     * Returns the number of bits up to and including the highest bit set.
     * 
     * @return the length of the {@code BitSet}.
     */
    public int length() {
        int idx = actualArrayLength - 1;
        while (idx >= 0 && bits[idx] == 0) {
            --idx;
        }
        actualArrayLength = idx + 1;
        if (idx == -1) {
            return 0;
        }
        int i = ELM_SIZE - 1;
        long val = bits[idx];
        while ((val & (TWO_N_ARRAY[i])) == 0 && i > 0) {
            i--;
        }
        return (idx << OFFSET) + i + 1;
    }

    private final int getActualArrayLength() {
        if (isLengthActual) {
            return actualArrayLength;
        }
        int idx = actualArrayLength - 1;
        while (idx >= 0 && bits[idx] == 0) {
            --idx;
        }
        actualArrayLength = idx + 1;
        isLengthActual = true;
        return actualArrayLength;
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * receiver.
     * 
     * @return a comma delimited list of the indices of all bits that are set.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(bits.length / 2);
        int bitCount = 0;
        sb.append('{');
        boolean comma = false;
        for (int i = 0; i < bits.length; i++) {
            if (bits[i] == 0) {
                bitCount += ELM_SIZE;
                continue;
            }
            for (int j = 0; j < ELM_SIZE; j++) {
                if (((bits[i] & (TWO_N_ARRAY[j])) != 0)) {
                    if (comma) {
                        sb.append(", "); //$NON-NLS-1$
                    }
                    sb.append(bitCount);
                    comma = true;
                }
                bitCount++;
            }
        }
        sb.append('}');
        return sb.toString();
    }

    /**
     * Returns the position of the first bit that is {@code true} on or after {@code pos}.
     * 
     * @param pos
     *            the starting position (inclusive).
     * @return -1 if there is no bits that are set to {@code true} on or after {@code pos}.
     */
    public int nextSetBit(int pos) {
        if (pos < 0) {
            throw new IndexOutOfBoundsException("Negative index specified");
        }

        if (pos >= actualArrayLength << OFFSET) {
            return -1;
        }

        int idx = pos >> OFFSET;
        // first check in the same bit set element
        if (bits[idx] != 0L) {
            for (int j = pos & RIGHT_BITS; j < ELM_SIZE; j++) {
                if (((bits[idx] & (TWO_N_ARRAY[j])) != 0)) {
                    return (idx << OFFSET) + j;
                }
            }

        }
        idx++;
        while (idx < actualArrayLength && bits[idx] == 0L) {
            idx++;
        }
        if (idx == actualArrayLength) {
            return -1;
        }

        // we know for sure there is a bit set to true in this element
        // since the bitset value is not 0L
        for (int j = 0; j < ELM_SIZE; j++) {
            if (((bits[idx] & (TWO_N_ARRAY[j])) != 0)) {
                return (idx << OFFSET) + j;
            }
        }

        return -1;
    }

    /**
     * Returns the position of the first bit that is {@code false} on or after {@code pos}.
     * 
     * @param pos
     *            the starting position (inclusive).
     * @return the position of the next bit set to {@code false}, even if it is further
     *         than this {@code BitSet}'s size.
     */
    public int nextClearBit(int pos) {
        if (pos < 0) {
            throw new IndexOutOfBoundsException("Negative index specified");
        }

        int length = actualArrayLength;
        int bssize = length << OFFSET;
        if (pos >= bssize) {
            return pos;
        }

        int idx = pos >> OFFSET;
        // first check in the same bit set element
        if (bits[idx] != (~0L)) {
            for (int j = pos % ELM_SIZE; j < ELM_SIZE; j++) {
                if (((bits[idx] & (TWO_N_ARRAY[j])) == 0)) {
                    return idx * ELM_SIZE + j;
                }
            }
        }
        idx++;
        while (idx < length && bits[idx] == (~0L)) {
            idx++;
        }
        if (idx == length) {
            return bssize;
        }

        // we know for sure there is a bit set to true in this element
        // since the bitset value is not 0L
        for (int j = 0; j < ELM_SIZE; j++) {
            if (((bits[idx] & (TWO_N_ARRAY[j])) == 0)) {
                return (idx << OFFSET) + j;
            }
        }

        return bssize;
    }

    /**
     * Returns true if all the bits in this {@code BitSet} are set to false.
     * 
     * @return {@code true} if the {@code BitSet} is empty,
     *         {@code false} otherwise.
     */
    public boolean isEmpty() {
        if (!needClear) {
            return true;
        }
        int length = bits.length;
        for (int idx = 0; idx < length; idx++) {
            if (bits[idx] != 0L) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the number of bits that are {@code true} in this {@code BitSet}.
     * 
     * @return the number of {@code true} bits in the set.
     */
    public int cardinality() {
        if (!needClear) {
            return 0;
        }
        int count = 0;
        int length = bits.length;
        // FIXME: need to test performance, if still not satisfied, change it to
        // 256-bits table based
        for (int idx = 0; idx < length; idx++) {
            count += pop(bits[idx] & 0xffffffffL);
            count += pop(bits[idx] >>> 32);
        }
        return count;
    }

    private final int pop(long x) {
        x = x - ((x >>> 1) & 0x55555555);
        x = (x & 0x33333333) + ((x >>> 2) & 0x33333333);
        x = (x + (x >>> 4)) & 0x0f0f0f0f;
        x = x + (x >>> 8);
        x = x + (x >>> 16);
        return (int) x & 0x0000003f;
    }
}
