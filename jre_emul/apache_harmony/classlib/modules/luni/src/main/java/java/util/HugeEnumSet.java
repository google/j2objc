/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.util;


/**
 * A concrete EnumSet for enums with more than 64 elements.
 */
@SuppressWarnings("serial")
final class HugeEnumSet<E extends Enum<E>> extends EnumSet<E> {
    
    private static final int BIT_IN_LONG = 64;

    final private E[] enums;
    
    private long[] bits;
    
    private int size;
    
    HugeEnumSet(Class<E> elementType) {
        super(elementType);
        enums = elementType.getEnumConstants();
        bits = new long[(enums.length + BIT_IN_LONG - 1) / BIT_IN_LONG];
    }
    
    private class HugeEnumSetIterator implements Iterator<E> {

        /**
         * The bits yet to be returned for the long in bits[index]. As values from the current index
         * are returned, their bits are zeroed out. When this reaches zero, the index must be
         * incremented.
         */
        private long currentBits = bits[0];

        /**
         * The index into HugeEnumSet.bits of the next value to return.
         */
        private int index;

        /**
         * The single bit of the next value to return.
         */
        private long mask;

        /**
         * The candidate for removal. If null, no value may be removed.
         */
        private E last;

        private HugeEnumSetIterator() {
            computeNextElement();
        }

        /**
         * Assigns mask and index to the next available value, cycling currentBits as necessary.
         */
        void computeNextElement() {
            while (true) {
                if (currentBits != 0) {
                    mask = currentBits & -currentBits; // the lowest 1 bit in currentBits
                    return;
                } else if (++index < bits.length) {
                    currentBits = bits[index];
                } else {
                    mask = 0;
                    return;
                }
            }
        }

        public boolean hasNext() {
            return mask != 0;
        }

        public E next() {
            if (mask == 0) {
                throw new NoSuchElementException();
            }

            int ordinal = Long.numberOfTrailingZeros(mask) + index * BIT_IN_LONG;
            last = enums[ordinal];

            currentBits &= ~mask;
            computeNextElement();

            return last;
        }

        public void remove() {
            if (last == null) {
                throw new IllegalStateException();
            }

            HugeEnumSet.this.remove(last);
            last = null;
        }
    }
    
    @Override
    public boolean add(E element) {
        if (!isValidType(element.getDeclaringClass())) {
            throw new ClassCastException();
        }

        int ordinal = element.ordinal();
        int index = ordinal / BIT_IN_LONG;
        int inBits = ordinal % BIT_IN_LONG;
        long oldBits = bits[index];
        long newBits = oldBits | (1L << inBits);
        if (oldBits != newBits) {
            bits[index] = newBits;
            size++;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean addAll(Collection<? extends E> collection) {
        if (collection.isEmpty() || collection == this) {
            return false;
        }

        if (collection instanceof EnumSet) {
            EnumSet<?> set = (EnumSet<?>) collection;
            if (!isValidType(set.elementClass)) {
                throw new ClassCastException();
            }

            HugeEnumSet<E> hugeSet = (HugeEnumSet<E>) set;
            boolean changed = false;
            for (int i = 0; i < bits.length; i++) {
                long oldBits = bits[i];
                long newBits = oldBits | hugeSet.bits[i];
                if (oldBits != newBits) {
                    bits[i] = newBits;
                    size += Long.bitCount(newBits) - Long.bitCount(oldBits);
                    changed = true;
                }
            }
            return changed;
        }
        return super.addAll(collection);
    }
    
    @Override
    public int size() {
        return size;
    }
    
    @Override
    public void clear() {
        Arrays.fill(bits, 0);
        size = 0;
    }
    
    @Override
    protected void complement() {
        size = 0;
        for (int i = 0, length = bits.length; i < length; i++) {
            long b = ~bits[i];

            // zero out unused bits on the last element
            if (i == length - 1) {
                b &= -1L >>> (BIT_IN_LONG - (enums.length % BIT_IN_LONG));
            }

            size += Long.bitCount(b);
            bits[i] = b;
        }
    }
    
    @Override
    public boolean contains(Object object) {
        if (object == null || !isValidType(object.getClass())) {
            return false;
        }

        @SuppressWarnings("unchecked") // guarded by isValidType()
        int ordinal = ((E) object).ordinal();
        int index = ordinal / BIT_IN_LONG;
        int inBits = ordinal % BIT_IN_LONG;
        return (bits[index] & (1L << inBits)) != 0;
    }
    
    @Override
    public HugeEnumSet<E> clone() {
        HugeEnumSet<E> set = (HugeEnumSet<E>) super.clone();
        set.bits = bits.clone();
        return set;
    }
    
    @Override
    public boolean containsAll(Collection<?> collection) {
        if (collection.isEmpty()) {
            return true;
        }
        if (collection instanceof HugeEnumSet) {
            HugeEnumSet<?> set = (HugeEnumSet<?>) collection;
            if (isValidType(set.elementClass)) {
                for (int i = 0; i < bits.length; i++) {
                    long setBits = set.bits[i];
                    if ((bits[i] & setBits) != setBits) {
                        return false;
                    }
                }
                return true;
            }
        }
        return !(collection instanceof EnumSet) && super.containsAll(collection);
    }
    
    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (!isValidType(object.getClass())) {
            return super.equals(object);
        }
        return Arrays.equals(bits, ((HugeEnumSet<?>) object).bits);
    }
    
    @Override
    public Iterator<E> iterator() {
        return new HugeEnumSetIterator();
    }
    
    @Override
    public boolean remove(Object object) {
        if (object == null || !isValidType(object.getClass())) {
            return false;
        }

        @SuppressWarnings("unchecked") // guarded by isValidType()
        int ordinal = ((E) object).ordinal();
        int index = ordinal / BIT_IN_LONG;
        int inBits = ordinal % BIT_IN_LONG;
        long oldBits = bits[index];
        long newBits = oldBits & ~(1L << inBits);
        if (oldBits != newBits) {
            bits[index] = newBits;
            size--;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean removeAll(Collection<?> collection) {
        if (collection.isEmpty()) {
            return false;
        }
        
        if (collection instanceof EnumSet) {
            EnumSet<?> set = (EnumSet<?>) collection;
            if (!isValidType(set.elementClass)) {
                return false;
            }

            HugeEnumSet<E> hugeSet = (HugeEnumSet<E>) set;
            boolean changed = false;
            for (int i = 0; i < bits.length; i++) {
                long oldBits = bits[i];
                long newBits = oldBits & ~hugeSet.bits[i];
                if (oldBits != newBits) {
                    bits[i] = newBits;
                    size += Long.bitCount(newBits) - Long.bitCount(oldBits);
                    changed = true;
                }
            }
            return changed;
        }
        return super.removeAll(collection);
    }
    
    @Override
    public boolean retainAll(Collection<?> collection) {
        if (collection instanceof EnumSet) {
            EnumSet<?> set = (EnumSet<?>) collection;
            if (!isValidType(set.elementClass)) {
                if (size > 0) {
                    clear();
                    return true;
                } else {
                    return false;
                }
            }

            HugeEnumSet<E> hugeSet = (HugeEnumSet<E>) set;
            boolean changed = false;
            for (int i = 0; i < bits.length; i++) {
                long oldBits = bits[i];
                long newBits = oldBits & hugeSet.bits[i];
                if (oldBits != newBits) {
                    bits[i] = newBits;
                    size += Long.bitCount(newBits) - Long.bitCount(oldBits);
                    changed = true;
                }
            }
            return changed;
        }
        return super.retainAll(collection);
    }
    
    @Override
    void setRange(E start, E end) {
        int startOrdinal = start.ordinal();
        int startIndex = startOrdinal / BIT_IN_LONG;
        int startInBits = startOrdinal % BIT_IN_LONG;

        int endOrdinal = end.ordinal();
        int endIndex = endOrdinal / BIT_IN_LONG;
        int endInBits = endOrdinal % BIT_IN_LONG;

        if (startIndex == endIndex) {
            long range = (-1L >>> (BIT_IN_LONG -(endInBits - startInBits + 1))) << startInBits;
            size -= Long.bitCount(bits[startIndex]);
            bits[startIndex] |= range;
            size += Long.bitCount(bits[startIndex]);

        } else {
            long range = (-1L >>> startInBits) << startInBits;
            size -= Long.bitCount(bits[startIndex]);
            bits[startIndex] |= range;
            size += Long.bitCount(bits[startIndex]);

            // endInBits + 1 is the number of consecutive ones.
            // 63 - endInBits is the following zeros of the right most one.
            range = -1L >>> (BIT_IN_LONG - (endInBits + 1));
            size -= Long.bitCount(bits[endIndex]);
            bits[endIndex] |= range;
            size += Long.bitCount(bits[endIndex]);
            for (int i = (startIndex + 1); i <= (endIndex - 1); i++) {
                size -= Long.bitCount(bits[i]);
                bits[i] = -1L;
                size += Long.bitCount(bits[i]);
            }
        }
    }
}
