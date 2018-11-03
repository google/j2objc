/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2017 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
package android.icu.text;

import java.nio.BufferOverflowException;
import java.util.Arrays;

/**
 * Records lengths of string edits but not replacement text.
 * Supports replacements, insertions, deletions in linear progression.
 * Does not support moving/reordering of text.
 *
 * @hide Only a subset of ICU is exposed in Android
 * @hide draft / provisional / internal are hidden on Android
 */
public final class Edits {
    // 0000uuuuuuuuuuuu records u+1 unchanged text units.
    private static final int MAX_UNCHANGED_LENGTH = 0x1000;
    private static final int MAX_UNCHANGED = MAX_UNCHANGED_LENGTH - 1;

    // 0wwwcccccccccccc with w=1..6 records ccc+1 replacements of w:w text units.
    // No length change.
    private static final int MAX_SHORT_WIDTH = 6;
    private static final int MAX_SHORT_CHANGE_LENGTH = 0xfff;
    private static final int MAX_SHORT_CHANGE = 0x6fff;

    // 0111mmmmmmnnnnnn records a replacement of m text units with n.
    // m or n = 61: actual length follows in the next edits array unit.
    // m or n = 62..63: actual length follows in the next two edits array units.
    // Bit 30 of the actual length is in the head unit.
    // Trailing units have bit 15 set.
    private static final int LENGTH_IN_1TRAIL = 61;
    private static final int LENGTH_IN_2TRAIL = 62;

    private static final int STACK_CAPACITY = 100;
    private char[] array;
    private int length;
    private int delta;

    /**
     * Constructs an empty object.
     * @hide draft / provisional / internal are hidden on Android
     */
    public Edits() {
        array = new char[STACK_CAPACITY];
    }

    /**
     * Resets the data but may not release memory.
     * @hide draft / provisional / internal are hidden on Android
     */
    public void reset() {
        length = delta = 0;
    }

    private void setLastUnit(int last) {
        array[length - 1] = (char)last;
    }
    private int lastUnit() {
        return length > 0 ? array[length - 1] : 0xffff;
    }

    /**
     * Adds a record for an unchanged segment of text.
     * Normally called from inside ICU string transformation functions, not user code.
     * @hide draft / provisional / internal are hidden on Android
     */
    public void addUnchanged(int unchangedLength) {
        if(unchangedLength < 0) {
            throw new IllegalArgumentException(
                    "addUnchanged(" + unchangedLength + "): length must not be negative");
        }
        // Merge into previous unchanged-text record, if any.
        int last = lastUnit();
        if(last < MAX_UNCHANGED) {
            int remaining = MAX_UNCHANGED - last;
            if (remaining >= unchangedLength) {
                setLastUnit(last + unchangedLength);
                return;
            }
            setLastUnit(MAX_UNCHANGED);
            unchangedLength -= remaining;
        }
        // Split large lengths into multiple units.
        while(unchangedLength >= MAX_UNCHANGED_LENGTH) {
            append(MAX_UNCHANGED);
            unchangedLength -= MAX_UNCHANGED_LENGTH;
        }
        // Write a small (remaining) length.
        if(unchangedLength > 0) {
            append(unchangedLength - 1);
        }
    }

    /**
     * Adds a record for a text replacement/insertion/deletion.
     * Normally called from inside ICU string transformation functions, not user code.
     * @hide draft / provisional / internal are hidden on Android
     */
    public void addReplace(int oldLength, int newLength) {
        if(oldLength == newLength && 0 < oldLength && oldLength <= MAX_SHORT_WIDTH) {
            // Replacement of short oldLength text units by same-length new text.
            // Merge into previous short-replacement record, if any.
            int last = lastUnit();
            if(MAX_UNCHANGED < last && last < MAX_SHORT_CHANGE &&
                    (last >> 12) == oldLength && (last & 0xfff) < MAX_SHORT_CHANGE_LENGTH) {
                setLastUnit(last + 1);
                return;
            }
            append(oldLength << 12);
            return;
        }

        if(oldLength < 0 || newLength < 0) {
            throw new IllegalArgumentException(
                    "addReplace(" + oldLength + ", " + newLength +
                    "): both lengths must be non-negative");
        }
        if (oldLength == 0 && newLength == 0) {
            return;
        }
        int newDelta = newLength - oldLength;
        if (newDelta != 0) {
            if ((newDelta > 0 && delta >= 0 && newDelta > (Integer.MAX_VALUE - delta)) ||
                    (newDelta < 0 && delta < 0 && newDelta < (Integer.MIN_VALUE - delta))) {
                // Integer overflow or underflow.
                throw new IndexOutOfBoundsException();
            }
            delta += newDelta;
        }

        int head = 0x7000;
        if (oldLength < LENGTH_IN_1TRAIL && newLength < LENGTH_IN_1TRAIL) {
            head |= oldLength << 6;
            head |= newLength;
            append(head);
        } else if ((array.length - length) >= 5 || growArray()) {
            int limit = length + 1;
            if(oldLength < LENGTH_IN_1TRAIL) {
                head |= oldLength << 6;
            } else if(oldLength <= 0x7fff) {
                head |= LENGTH_IN_1TRAIL << 6;
                array[limit++] = (char)(0x8000 | oldLength);
            } else {
                head |= (LENGTH_IN_2TRAIL + (oldLength >> 30)) << 6;
                array[limit++] = (char)(0x8000 | (oldLength >> 15));
                array[limit++] = (char)(0x8000 | oldLength);
            }
            if(newLength < LENGTH_IN_1TRAIL) {
                head |= newLength;
            } else if(newLength <= 0x7fff) {
                head |= LENGTH_IN_1TRAIL;
                array[limit++] = (char)(0x8000 | newLength);
            } else {
                head |= LENGTH_IN_2TRAIL + (newLength >> 30);
                array[limit++] = (char)(0x8000 | (newLength >> 15));
                array[limit++] = (char)(0x8000 | newLength);
            }
            array[length] = (char)head;
            length = limit;
        }
    }

    private void append(int r) {
        if(length < array.length || growArray()) {
            array[length++] = (char)r;
        }
    }

    private boolean growArray() {
        int newCapacity;
        if (array.length == STACK_CAPACITY) {
            newCapacity = 2000;
        } else if (array.length == Integer.MAX_VALUE) {
            throw new BufferOverflowException();
        } else if (array.length >= (Integer.MAX_VALUE / 2)) {
            newCapacity = Integer.MAX_VALUE;
        } else {
            newCapacity = 2 * array.length;
        }
        // Grow by at least 5 units so that a maximal change record will fit.
        if ((newCapacity - array.length) < 5) {
            throw new BufferOverflowException();
        }
        array = Arrays.copyOf(array, newCapacity);
        return true;
    }

    /**
     * How much longer is the new text compared with the old text?
     * @return new length minus old length
     * @hide draft / provisional / internal are hidden on Android
     */
    public int lengthDelta() { return delta; }
    /**
     * @return true if there are any change edits
     * @hide draft / provisional / internal are hidden on Android
     */
    public boolean hasChanges()  {
        if (delta != 0) {
            return true;
        }
        for (int i = 0; i < length; ++i) {
            if (array[i] > MAX_UNCHANGED) {
                return true;
            }
        }
        return false;
    }

    /**
     * Access to the list of edits.
     * @see #getCoarseIterator
     * @see #getFineIterator
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final class Iterator {
        private final char[] array;
        private int index;
        private final int length;
        private int remaining;
        private final boolean onlyChanges_, coarse;

        private boolean changed;
        private int oldLength_, newLength_;
        private int srcIndex, replIndex, destIndex;

        private Iterator(char[] a, int len, boolean oc, boolean crs) {
            array = a;
            length = len;
            onlyChanges_ = oc;
            coarse = crs;
        }

        private int readLength(int head) {
            if (head < LENGTH_IN_1TRAIL) {
                return head;
            } else if (head < LENGTH_IN_2TRAIL) {
                assert(index < length);
                assert(array[index] >= 0x8000);
                return array[index++] & 0x7fff;
            } else {
                assert((index + 2) <= length);
                assert(array[index] >= 0x8000);
                assert(array[index + 1] >= 0x8000);
                int len = ((head & 1) << 30) |
                        ((array[index] & 0x7fff) << 15) |
                        (array[index + 1] & 0x7fff);
                index += 2;
                return len;
            }
        }

        private void updateIndexes() {
            srcIndex += oldLength_;
            if (changed) {
                replIndex += newLength_;
            }
            destIndex += newLength_;
        }

        private boolean noNext() {
            // No change beyond the string.
            changed = false;
            oldLength_ = newLength_ = 0;
            return false;
        }

        /**
         * Advances to the next edit.
         * @return true if there is another edit
         * @hide draft / provisional / internal are hidden on Android
         */
        public boolean next() {
            return next(onlyChanges_);
        }

        private boolean next(boolean onlyChanges) {
            // We have an errorCode in case we need to start guarding against integer overflows.
            // It is also convenient for caller loops if we bail out when an error was set elsewhere.
            updateIndexes();
            if (remaining > 0) {
                // Fine-grained iterator: Continue a sequence of equal-length changes.
                --remaining;
                return true;
            }
            if (index >= length) {
                return noNext();
            }
            int u = array[index++];
            if (u <= MAX_UNCHANGED) {
                // Combine adjacent unchanged ranges.
                changed = false;
                oldLength_ = u + 1;
                while (index < length && (u = array[index]) <= MAX_UNCHANGED) {
                    ++index;
                    oldLength_ += u + 1;
                }
                newLength_ = oldLength_;
                if (onlyChanges) {
                    updateIndexes();
                    if (index >= length) {
                        return noNext();
                    }
                    // already fetched u > MAX_UNCHANGED at index
                    ++index;
                } else {
                    return true;
                }
            }
            changed = true;
            if (u <= MAX_SHORT_CHANGE) {
                if (coarse) {
                    int w = u >> 12;
                    int len = (u & 0xfff) + 1;
                    oldLength_ = newLength_ = len * w;
                } else {
                    // Split a sequence of equal-length changes that was compressed into one unit.
                    oldLength_ = newLength_ = u >> 12;
                    remaining = u & 0xfff;
                    return true;
                }
            } else {
                assert(u <= 0x7fff);
                oldLength_ = readLength((u >> 6) & 0x3f);
                newLength_ = readLength(u & 0x3f);
                if (!coarse) {
                    return true;
                }
            }
            // Combine adjacent changes.
            while (index < length && (u = array[index]) > MAX_UNCHANGED) {
                ++index;
                if (u <= MAX_SHORT_CHANGE) {
                    int w = u >> 12;
                    int len = (u & 0xfff) + 1;
                    len = len * w;
                    oldLength_ += len;
                    newLength_ += len;
                } else {
                    assert(u <= 0x7fff);
                    int oldLen = readLength((u >> 6) & 0x3f);
                    int newLen = readLength(u & 0x3f);
                    oldLength_ += oldLen;
                    newLength_ += newLen;
                }
            }
            return true;
        }

        /**
         * Finds the edit that contains the source index.
         * The source index may be found in a non-change
         * even if normal iteration would skip non-changes.
         * Normal iteration can continue from a found edit.
         *
         * <p>The iterator state before this search logically does not matter.
         * (It may affect the performance of the search.)
         *
         * <p>The iterator state after this search is undefined
         * if the source index is out of bounds for the source string.
         *
         * @param i source index
         * @return true if the edit for the source index was found
         * @hide draft / provisional / internal are hidden on Android
         */
        public boolean findSourceIndex(int i) {
            if (i < 0) { return false; }
            if (i < srcIndex) {
                // Reset the iterator to the start.
                index = remaining = oldLength_ = newLength_ = srcIndex = replIndex = destIndex = 0;
            } else if (i < (srcIndex + oldLength_)) {
                // The index is in the current span.
                return true;
            }
            while (next(false)) {
                if (i < (srcIndex + oldLength_)) {
                    // The index is in the current span.
                    return true;
                }
                if (remaining > 0) {
                    // Is the index in one of the remaining compressed edits?
                    // srcIndex is the start of the current span, before the remaining ones.
                    int len = (remaining + 1) * oldLength_;
                    if (i < (srcIndex + len)) {
                        int n = (i - srcIndex) / oldLength_;  // 1 <= n <= remaining
                        len = n * oldLength_;
                        srcIndex += len;
                        replIndex += len;
                        destIndex += len;
                        remaining -= n;
                        return true;
                    }
                    // Make next() skip all of these edits at once.
                    oldLength_ = newLength_ = len;
                    remaining = 0;
                }
            }
            return false;
        }

        /**
         * @return true if this edit replaces oldLength() units with newLength() different ones.
         *         false if oldLength units remain unchanged.
         * @hide draft / provisional / internal are hidden on Android
         */
        public boolean hasChange() { return changed; }
        /**
         * @return the number of units in the original string which are replaced or remain unchanged.
         * @hide draft / provisional / internal are hidden on Android
         */
        public int oldLength() { return oldLength_; }
        /**
         * @return the number of units in the modified string, if hasChange() is true.
         *         Same as oldLength if hasChange() is false.
         * @hide draft / provisional / internal are hidden on Android
         */
        public int newLength() { return newLength_; }

        /**
         * @return the current index into the source string
         * @hide draft / provisional / internal are hidden on Android
         */
        public int sourceIndex() { return srcIndex; }
        /**
         * @return the current index into the replacement-characters-only string,
         *         not counting unchanged spans
         * @hide draft / provisional / internal are hidden on Android
         */
        public int replacementIndex() { return replIndex; }
        /**
         * @return the current index into the full destination string
         * @hide draft / provisional / internal are hidden on Android
         */
        public int destinationIndex() { return destIndex; }
    };

    /**
     * Returns an Iterator for coarse-grained changes for simple string updates.
     * Skips non-changes.
     * @return an Iterator that merges adjacent changes.
     * @hide draft / provisional / internal are hidden on Android
     */
    public Iterator getCoarseChangesIterator() {
        return new Iterator(array, length, true, true);
    }

    /**
     * Returns an Iterator for coarse-grained changes and non-changes for simple string updates.
     * @return an Iterator that merges adjacent changes.
     * @hide draft / provisional / internal are hidden on Android
     */
    public Iterator getCoarseIterator() {
        return new Iterator(array, length, false, true);
    }

    /**
     * Returns an Iterator for fine-grained changes for modifying styled text.
     * Skips non-changes.
     * @return an Iterator that separates adjacent changes.
     * @hide draft / provisional / internal are hidden on Android
     */
    public Iterator getFineChangesIterator() {
        return new Iterator(array, length, true, false);
    }

    /**
     * Returns an Iterator for fine-grained changes and non-changes for modifying styled text.
     * @return an Iterator that separates adjacent changes.
     * @hide draft / provisional / internal are hidden on Android
     */
    public Iterator getFineIterator() {
        return new Iterator(array, length, false, false);
    }
}
