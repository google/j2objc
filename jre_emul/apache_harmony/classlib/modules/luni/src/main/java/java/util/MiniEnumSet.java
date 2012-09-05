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
 * A concrete EnumSet for enums with 64 or fewer elements.
 */
@SuppressWarnings("serial")
final class MiniEnumSet<E extends Enum<E>> extends EnumSet<E> {
    private static final int MAX_ELEMENTS = 64;
    
    private int size;
    
    private final E[] enums;    
    
    private long bits;
    
    MiniEnumSet(Class<E> elementType) {
        super(elementType);
        enums = elementType.getEnumConstants();
    }
    
    private class MiniEnumSetIterator implements Iterator<E> {

        /**
         * The bits yet to be returned for bits. As values from the current index are returned,
         * their bits are zeroed out.
         */
        private long currentBits = bits;

        /**
         * The single bit of the next value to return.
         */
        private long mask = currentBits & -currentBits; // the lowest 1 bit in currentBits

        /**
         * The candidate for removal. If null, no value may be removed.
         */
        private E last;

        public boolean hasNext() {
            return mask != 0;
        }

        public E next() {
            if (mask == 0) {
                throw new NoSuchElementException();
            }

            int ordinal = Long.numberOfTrailingZeros(mask);
            last = enums[ordinal];

            currentBits &= ~mask;
            mask = currentBits & -currentBits; // the lowest 1 bit in currentBits

            return last;
        }

        public void remove() {
            if (last == null) {
                throw new IllegalStateException();
            }

            MiniEnumSet.this.remove(last);
            last = null;
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new MiniEnumSetIterator();
    }

    @Override
    public int size() {
        return size;
    }
    
    @Override
    public void clear() {
        bits = 0;
        size = 0;
    }
    
    @Override
    public boolean add(E element) {
        if (!isValidType(element.getDeclaringClass())) {
            throw new ClassCastException();
        }

        long oldBits = bits;
        long newBits = oldBits | (1L << element.ordinal());
        if (oldBits != newBits) {
            bits = newBits;
            size++;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean addAll(Collection<? extends E> collection) {
        if (collection.isEmpty()) {
            return false;
        }
        if (collection instanceof EnumSet) {
            EnumSet<?> set = (EnumSet<?>) collection;
            if (!isValidType(set.elementClass)) {
                throw new ClassCastException();
            }

            MiniEnumSet<?> miniSet = (MiniEnumSet<?>) set;
            long oldBits = bits;
            long newBits = oldBits | miniSet.bits;
            bits = newBits;
            size = Long.bitCount(newBits);
            return (oldBits != newBits);
        }
        return super.addAll(collection);
    }
    
    @Override
    public boolean contains(Object object) {
        if (object == null || !isValidType(object.getClass())) {
            return false;
        }

        @SuppressWarnings("unchecked") // guarded by isValidType()
        Enum<E> element = (Enum<E>) object;
        int ordinal = element.ordinal();
        return (bits & (1L << ordinal)) != 0;
    }
    
    @Override
    public boolean containsAll(Collection<?> collection) {
        if (collection.isEmpty()) {
            return true;
        }
        if (collection instanceof MiniEnumSet) {
            MiniEnumSet<?> set = (MiniEnumSet<?>) collection;
            long setBits = set.bits;
            return isValidType(set.elementClass) && ((bits & setBits) == setBits);
        }
        return !(collection instanceof EnumSet) && super.containsAll(collection);  
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

            MiniEnumSet<E> miniSet = (MiniEnumSet<E>) set;
            long oldBits = bits;
            long newBits = oldBits & ~miniSet.bits;
            if (oldBits != newBits) {
                bits = newBits;
                size = Long.bitCount(newBits);
                return true;
            }
            return false;
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

            MiniEnumSet<E> miniSet = (MiniEnumSet<E>) set;
            long oldBits = bits;
            long newBits = oldBits & miniSet.bits;
            if (oldBits != newBits) {
                bits = newBits;
                size = Long.bitCount(newBits);
                return true;
            }
            return false;
        }
        return super.retainAll(collection);
    }
    
    @Override
    public boolean remove(Object object) {
        if (object == null || !isValidType(object.getClass())) {
            return false;
        }

        @SuppressWarnings("unchecked") // guarded by isValidType() 
        Enum<E> element = (Enum<E>) object;
        int ordinal = element.ordinal();
        long oldBits = bits;
        long newBits = oldBits & ~(1L << ordinal);
        if (oldBits != newBits) {
            bits = newBits;
            size--;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof EnumSet)) {
            return super.equals(object);
        }
        EnumSet<?> set =(EnumSet<?>) object;
        if (!isValidType(set.elementClass)) {
            return size == 0 && set.isEmpty();
        }
        return bits == ((MiniEnumSet<?>) set).bits;
    }
    
    @Override
    void complement() {
        if (enums.length != 0) {
            bits = ~bits;
            bits &= (-1L >>> (MAX_ELEMENTS - enums.length));
            size = enums.length - size;
        }
    }
    
    @Override
    void setRange(E start, E end) {
        int length = end.ordinal() - start.ordinal() + 1;
        long range = (-1L >>> (MAX_ELEMENTS - length)) << start.ordinal();
        bits |= range;
        size = Long.bitCount(bits);
    }
}
