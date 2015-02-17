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

package java.lang;

import java.io.InvalidObjectException;
import java.util.Arrays;
import libcore.util.EmptyArray;

/**
 * A modifiable {@link CharSequence sequence of characters} for use in creating
 * and modifying Strings. This class is intended as a base class for
 * {@link StringBuffer} and {@link StringBuilder}.
 *
 * @see StringBuffer
 * @see StringBuilder
 * @since 1.5
 */
abstract class AbstractStringBuilder implements CharSequence {

    static final int INITIAL_CAPACITY = 16;

    private char[] value;

    private int count;

    private boolean shared;

    /*
     * Returns the character array.
     */
    final char[] getValue() {
        return value;
    }

    /*
     * Returns the underlying buffer and sets the shared flag.
     */
    final char[] shareValue() {
        shared = true;
        return value;
    }

    /*
     * Restores internal state after deserialization.
     */
    final void set(char[] val, int len) throws InvalidObjectException {
        if (val == null) {
            val = EmptyArray.CHAR;
        }
        if (val.length < len) {
            throw new InvalidObjectException("count out of range");
        }

        shared = false;
        value = val;
        count = len;
    }

    AbstractStringBuilder() {
        value = new char[INITIAL_CAPACITY];
    }

    AbstractStringBuilder(int capacity) {
        if (capacity < 0) {
            throw new NegativeArraySizeException(Integer.toString(capacity));
        }
        value = new char[capacity];
    }

    AbstractStringBuilder(String string) {
        count = string.length();
        shared = false;
        value = new char[count + INITIAL_CAPACITY];
        string.getChars(0, count, value, 0);
    }

    private void enlargeBuffer(int min) {
        int newCount = ((value.length >> 1) + value.length) + 2;
        char[] newData = new char[min > newCount ? min : newCount];
        System.arraycopy(value, 0, newData, 0, count);
        value = newData;
        shared = false;
    }

    final void appendNull() {
        int newCount = count + 4;
        if (newCount > value.length) {
            enlargeBuffer(newCount);
        }
        value[count++] = 'n';
        value[count++] = 'u';
        value[count++] = 'l';
        value[count++] = 'l';
    }

    final void append0(char[] chars) {
        int newCount = count + chars.length;
        if (newCount > value.length) {
            enlargeBuffer(newCount);
        }
        System.arraycopy(chars, 0, value, count, chars.length);
        count = newCount;
    }

    final void append0(char[] chars, int offset, int length) {
        Arrays.checkOffsetAndCount(chars.length, offset, length);
        int newCount = count + length;
        if (newCount > value.length) {
            enlargeBuffer(newCount);
        }
        System.arraycopy(chars, offset, value, count, length);
        count = newCount;
    }

    // Used internally by java_lang_IntegralToString.m.
    /*-[
    void AbstractStringBuilder_appendNativeBuffer(
        JavaLangAbstractStringBuilder *self, unichar *buffer, int length) {
      int newCount = self->count_ + length;
      if (newCount > (int) self->value_->size_) {
        JavaLangAbstractStringBuilder_enlargeBufferWithInt_(self, newCount);
      }
      memcpy(self->value_->buffer_ + self->count_, buffer, length * sizeof(unichar));
      self->count_ = newCount;
    }
    ]-*/

    final void append0(char ch) {
        if (count == value.length) {
            enlargeBuffer(count + 1);
        }
        value[count++] = ch;
    }

    // Used internally by java_lang_RealToString.m.
    /*-[
    void AbstractStringBuilder_appendCharNative(JavaLangAbstractStringBuilder *self, unichar ch) {
      if (self->count_ == (int) self->value_->size_) {
        JavaLangAbstractStringBuilder_enlargeBufferWithInt_(self, self->count_ + 1);
      }
      self->value_->buffer_[self->count_++] = ch;
    }
    ]-*/

    final void append0(String string) {
        if (string == null) {
            appendNull();
            return;
        }
        int length = string.length();
        int newCount = count + length;
        if (newCount > value.length) {
            enlargeBuffer(newCount);
        }
        string.getChars(0, length, value, count);
        count = newCount;
    }

    final void append0(CharSequence s, int start, int end) {
        if (s == null) {
            s = "null";
        }
        if ((start | end) < 0 || start > end || end > s.length()) {
            throw new IndexOutOfBoundsException();
        }

        int length = end - start;
        int newCount = count + length;
        if (newCount > value.length) {
            enlargeBuffer(newCount);
        } else if (shared) {
            value = value.clone();
            shared = false;
        }

        if (s instanceof String) {
            ((String) s).getChars(start, end, value, count);
        } else if (s instanceof AbstractStringBuilder) {
            AbstractStringBuilder other = (AbstractStringBuilder) s;
            System.arraycopy(other.value, start, value, count, length);
        } else {
            int j = count; // Destination index.
            for (int i = start; i < end; i++) {
                value[j++] = s.charAt(i);
            }
        }

        this.count = newCount;
    }

    /**
     * Returns the number of characters that can be held without growing.
     *
     * @return the capacity
     * @see #ensureCapacity
     * @see #length
     */
    public int capacity() {
        return value.length;
    }

    /**
     * Retrieves the character at the {@code index}.
     *
     * @param index
     *            the index of the character to retrieve.
     * @return the char value.
     * @throws IndexOutOfBoundsException
     *             if {@code index} is negative or greater than or equal to the
     *             current {@link #length()}.
     */
    public char charAt(int index) {
        if (index < 0 || index >= count) {
            throw indexAndLength(index);
        }
        return value[index];
    }

    private StringIndexOutOfBoundsException indexAndLength(int index) {
        throw new StringIndexOutOfBoundsException(count, index);
    }

    private StringIndexOutOfBoundsException startEndAndLength(int start, int end) {
        throw new StringIndexOutOfBoundsException(count, start, end - start);
    }

    final void delete0(int start, int end) {
        if (start >= 0) {
            if (end > count) {
                end = count;
            }
            if (end == start) {
                return;
            }
            if (end > start) {
                int length = count - end;
                if (length >= 0) {
                    if (!shared) {
                        System.arraycopy(value, end, value, start, length);
                    } else {
                        char[] newData = new char[value.length];
                        System.arraycopy(value, 0, newData, 0, start);
                        System.arraycopy(value, end, newData, start, length);
                        value = newData;
                        shared = false;
                    }
                }
                count -= end - start;
                return;
            }
        }
        throw startEndAndLength(start, end);
    }

    final void deleteCharAt0(int index) {
        if (index < 0 || index >= count) {
            throw indexAndLength(index);
        }
        int length = count - index - 1;
        if (length > 0) {
            if (!shared) {
                System.arraycopy(value, index + 1, value, index, length);
            } else {
                char[] newData = new char[value.length];
                System.arraycopy(value, 0, newData, 0, index);
                System.arraycopy(value, index + 1, newData, index, length);
                value = newData;
                shared = false;
            }
        }
        count--;
    }

    /**
     * Ensures that this object has a minimum capacity available before
     * requiring the internal buffer to be enlarged. The general policy of this
     * method is that if the {@code minimumCapacity} is larger than the current
     * {@link #capacity()}, then the capacity will be increased to the largest
     * value of either the {@code minimumCapacity} or the current capacity
     * multiplied by two plus two. Although this is the general policy, there is
     * no guarantee that the capacity will change.
     *
     * @param min
     *            the new minimum capacity to set.
     */
    public void ensureCapacity(int min) {
        if (min > value.length) {
            int ourMin = value.length*2 + 2;
            enlargeBuffer(Math.max(ourMin, min));
        }
    }

    /**
     * Copies the requested sequence of characters into {@code dst} passed
     * starting at {@code dst}.
     *
     * @param start
     *            the inclusive start index of the characters to copy.
     * @param end
     *            the exclusive end index of the characters to copy.
     * @param dst
     *            the {@code char[]} to copy the characters to.
     * @param dstStart
     *            the inclusive start index of {@code dst} to begin copying to.
     * @throws IndexOutOfBoundsException
     *             if the {@code start} is negative, the {@code dstStart} is
     *             negative, the {@code start} is greater than {@code end}, the
     *             {@code end} is greater than the current {@link #length()} or
     *             {@code dstStart + end - begin} is greater than
     *             {@code dst.length}.
     */
    public void getChars(int start, int end, char[] dst, int dstStart) {
        if (start > count || end > count || start > end) {
            throw startEndAndLength(start, end);
        }
        System.arraycopy(value, start, dst, dstStart, end - start);
    }

    final void insert0(int index, char[] chars) {
        if (index < 0 || index > count) {
            throw indexAndLength(index);
        }
        if (chars.length != 0) {
            move(chars.length, index);
            System.arraycopy(chars, 0, value, index, chars.length);
            count += chars.length;
        }
    }

    final void insert0(int index, char[] chars, int start, int length) {
        if (index >= 0 && index <= count) {
            // start + length could overflow, start/length maybe MaxInt
            if (start >= 0 && length >= 0 && length <= chars.length - start) {
                if (length != 0) {
                    move(length, index);
                    System.arraycopy(chars, start, value, index, length);
                    count += length;
                }
                return;
            }
        }
        throw new StringIndexOutOfBoundsException("this.length=" + count
                + "; index=" + index + "; chars.length=" + chars.length
                + "; start=" + start + "; length=" + length);
    }

    final void insert0(int index, char ch) {
        if (index < 0 || index > count) {
            // RI compatible exception type
            throw new ArrayIndexOutOfBoundsException(count, index);
        }
        move(1, index);
        value[index] = ch;
        count++;
    }

    final void insert0(int index, String string) {
        if (index >= 0 && index <= count) {
            if (string == null) {
                string = "null";
            }
            int min = string.length();
            if (min != 0) {
                move(min, index);
                string.getChars(0, min, value, index);
                count += min;
            }
        } else {
            throw indexAndLength(index);
        }
    }

    final void insert0(int index, CharSequence s, int start, int end) {
        if (s == null) {
            s = "null";
        }
        if ((index | start | end) < 0 || index > count || start > end || end > s.length()) {
            throw new IndexOutOfBoundsException();
        }
        insert0(index, s.subSequence(start, end).toString());
    }

    /**
     * The current length.
     *
     * @return the number of characters contained in this instance.
     */
    public int length() {
        return count;
    }

    private void move(int size, int index) {
        int newCount;
        if (value.length - count >= size) {
            if (!shared) {
                // index == count case is no-op
                System.arraycopy(value, index, value, index + size, count - index);
                return;
            }
            newCount = value.length;
        } else {
            newCount = Math.max(count + size, value.length*2 + 2);
        }

        char[] newData = new char[newCount];
        System.arraycopy(value, 0, newData, 0, index);
        // index == count case is no-op
        System.arraycopy(value, index, newData, index + size, count - index);
        value = newData;
        shared = false;
    }

    final void replace0(int start, int end, String string) {
        if (start >= 0) {
            if (end > count) {
                end = count;
            }
            if (end > start) {
                int stringLength = string.length();
                int diff = end - start - stringLength;
                if (diff > 0) { // replacing with fewer characters
                    if (!shared) {
                        // index == count case is no-op
                        System.arraycopy(value, end, value, start
                                + stringLength, count - end);
                    } else {
                        char[] newData = new char[value.length];
                        System.arraycopy(value, 0, newData, 0, start);
                        // index == count case is no-op
                        System.arraycopy(value, end, newData, start
                                + stringLength, count - end);
                        value = newData;
                        shared = false;
                    }
                } else if (diff < 0) {
                    // replacing with more characters...need some room
                    move(-diff, end);
                } else if (shared) {
                    value = value.clone();
                    shared = false;
                }
                string.getChars(0, stringLength, value, start);
                count -= diff;
                return;
            }
            if (start == end) {
                if (string == null) {
                    throw new NullPointerException("string == null");
                }
                insert0(start, string);
                return;
            }
        }
        throw startEndAndLength(start, end);
    }

    final void reverse0() {
        if (count < 2) {
            return;
        }
        if (!shared) {
            int end = count - 1;
            char frontHigh = value[0];
            char endLow = value[end];
            boolean allowFrontSur = true, allowEndSur = true;
            for (int i = 0, mid = count / 2; i < mid; i++, --end) {
                char frontLow = value[i + 1];
                char endHigh = value[end - 1];
                boolean surAtFront = allowFrontSur && frontLow >= 0xdc00
                        && frontLow <= 0xdfff && frontHigh >= 0xd800
                        && frontHigh <= 0xdbff;
                if (surAtFront && (count < 3)) {
                    return;
                }
                boolean surAtEnd = allowEndSur && endHigh >= 0xd800
                        && endHigh <= 0xdbff && endLow >= 0xdc00
                        && endLow <= 0xdfff;
                allowFrontSur = allowEndSur = true;
                if (surAtFront == surAtEnd) {
                    if (surAtFront) {
                        // both surrogates
                        value[end] = frontLow;
                        value[end - 1] = frontHigh;
                        value[i] = endHigh;
                        value[i + 1] = endLow;
                        frontHigh = value[i + 2];
                        endLow = value[end - 2];
                        i++;
                        end--;
                    } else {
                        // neither surrogates
                        value[end] = frontHigh;
                        value[i] = endLow;
                        frontHigh = frontLow;
                        endLow = endHigh;
                    }
                } else {
                    if (surAtFront) {
                        // surrogate only at the front
                        value[end] = frontLow;
                        value[i] = endLow;
                        endLow = endHigh;
                        allowFrontSur = false;
                    } else {
                        // surrogate only at the end
                        value[end] = frontHigh;
                        value[i] = endHigh;
                        frontHigh = frontLow;
                        allowEndSur = false;
                    }
                }
            }
            if ((count & 1) == 1 && (!allowFrontSur || !allowEndSur)) {
                value[end] = allowFrontSur ? endLow : frontHigh;
            }
        } else {
            char[] newData = new char[value.length];
            for (int i = 0, end = count; i < count; i++) {
                char high = value[i];
                if ((i + 1) < count && high >= 0xd800 && high <= 0xdbff) {
                    char low = value[i + 1];
                    if (low >= 0xdc00 && low <= 0xdfff) {
                        newData[--end] = low;
                        i++;
                    }
                }
                newData[--end] = high;
            }
            value = newData;
            shared = false;
        }
    }

    /**
     * Sets the character at the {@code index}.
     *
     * @param index
     *            the zero-based index of the character to replace.
     * @param ch
     *            the character to set.
     * @throws IndexOutOfBoundsException
     *             if {@code index} is negative or greater than or equal to the
     *             current {@link #length()}.
     */
    public void setCharAt(int index, char ch) {
        if (index < 0 || index >= count) {
            throw indexAndLength(index);
        }
        if (shared) {
            value = value.clone();
            shared = false;
        }
        value[index] = ch;
    }

    /**
     * Sets the current length to a new value. If the new length is larger than
     * the current length, then the new characters at the end of this object
     * will contain the {@code char} value of {@code \u0000}.
     *
     * @param length
     *            the new length of this StringBuffer.
     * @exception IndexOutOfBoundsException
     *                if {@code length < 0}.
     * @see #length
     */
    public void setLength(int length) {
        if (length < 0) {
            throw new StringIndexOutOfBoundsException("length < 0: " + length);
        }
        if (length > value.length) {
            enlargeBuffer(length);
        } else {
            if (shared) {
                char[] newData = new char[value.length];
                System.arraycopy(value, 0, newData, 0, count);
                value = newData;
                shared = false;
            } else {
                if (count < length) {
                    Arrays.fill(value, count, length, (char) 0);
                }
            }
        }
        count = length;
    }

    /**
     * Returns the String value of the subsequence from the {@code start} index
     * to the current end.
     *
     * @param start
     *            the inclusive start index to begin the subsequence.
     * @return a String containing the subsequence.
     * @throws StringIndexOutOfBoundsException
     *             if {@code start} is negative or greater than the current
     *             {@link #length()}.
     */
    public String substring(int start) {
        if (start >= 0 && start <= count) {
            if (start == count) {
                return "";
            }

            // Remove String sharing for more performance
            return new String(value, start, count - start);
        }
        throw indexAndLength(start);
    }

    /**
     * Returns the String value of the subsequence from the {@code start} index
     * to the {@code end} index.
     *
     * @param start
     *            the inclusive start index to begin the subsequence.
     * @param end
     *            the exclusive end index to end the subsequence.
     * @return a String containing the subsequence.
     * @throws StringIndexOutOfBoundsException
     *             if {@code start} is negative, greater than {@code end} or if
     *             {@code end} is greater than the current {@link #length()}.
     */
    public String substring(int start, int end) {
        if (start >= 0 && start <= end && end <= count) {
            if (start == end) {
                return "";
            }

            // Remove String sharing for more performance
            return new String(value, start, end - start);
        }
        throw startEndAndLength(start, end);
    }

    /**
     * Returns the current String representation.
     *
     * @return a String containing the characters in this instance.
     */
    @Override
    public String toString() {
        return toString0();
    }

    protected String toString0() {
        if (count == 0) {
            return "";
        }
        // Optimize String sharing for more performance
        int wasted = value.length - count;
        if (wasted >= 256
                || (wasted >= INITIAL_CAPACITY && wasted >= (count >> 1))) {
            return new String(value, 0, count);
        }
        shared = true;
        return new String(0, count, value);
    }

    /**
     * Returns a {@code CharSequence} of the subsequence from the {@code start}
     * index to the {@code end} index.
     *
     * @param start
     *            the inclusive start index to begin the subsequence.
     * @param end
     *            the exclusive end index to end the subsequence.
     * @return a CharSequence containing the subsequence.
     * @throws IndexOutOfBoundsException
     *             if {@code start} is negative, greater than {@code end} or if
     *             {@code end} is greater than the current {@link #length()}.
     * @since 1.4
     */
    public CharSequence subSequence(int start, int end) {
        return substring(start, end);
    }

    /**
     * Searches for the first index of the specified character. The search for
     * the character starts at the beginning and moves towards the end.
     *
     * @param string
     *            the string to find.
     * @return the index of the specified character, -1 if the character isn't
     *         found.
     * @see #lastIndexOf(String)
     * @since 1.4
     */
    public int indexOf(String string) {
        return indexOf(string, 0);
    }

    /**
     * Searches for the index of the specified character. The search for the
     * character starts at the specified offset and moves towards the end.
     *
     * @param subString
     *            the string to find.
     * @param start
     *            the starting offset.
     * @return the index of the specified character, -1 if the character isn't
     *         found
     * @see #lastIndexOf(String,int)
     * @since 1.4
     */
    public int indexOf(String subString, int start) {
        if (start < 0) {
            start = 0;
        }
        int subCount = subString.length();
        if (subCount > 0) {
            if (subCount + start > count) {
                return -1;
            }
            // TODO optimize charAt to direct array access
            char firstChar = subString.charAt(0);
            while (true) {
                int i = start;
                boolean found = false;
                for (; i < count; i++) {
                    if (value[i] == firstChar) {
                        found = true;
                        break;
                    }
                }
                if (!found || subCount + i > count) {
                    return -1; // handles subCount > count || start >= count
                }
                int o1 = i, o2 = 0;
                while (++o2 < subCount && value[++o1] == subString.charAt(o2)) {
                    // Intentionally empty
                }
                if (o2 == subCount) {
                    return i;
                }
                start = i + 1;
            }
        }
        return (start < count || start == 0) ? start : count;
    }

    /**
     * Searches for the last index of the specified character. The search for
     * the character starts at the end and moves towards the beginning.
     *
     * @param string
     *            the string to find.
     * @return the index of the specified character, -1 if the character isn't
     *         found.
     * @throws NullPointerException
     *             if {@code string} is {@code null}.
     * @see String#lastIndexOf(java.lang.String)
     * @since 1.4
     */
    public int lastIndexOf(String string) {
        return lastIndexOf(string, count);
    }

    /**
     * Searches for the index of the specified character. The search for the
     * character starts at the specified offset and moves towards the beginning.
     *
     * @param subString
     *            the string to find.
     * @param start
     *            the starting offset.
     * @return the index of the specified character, -1 if the character isn't
     *         found.
     * @throws NullPointerException
     *             if {@code subString} is {@code null}.
     * @see String#lastIndexOf(String,int)
     * @since 1.4
     */
    public int lastIndexOf(String subString, int start) {
        int subCount = subString.length();
        if (subCount <= count && start >= 0) {
            if (subCount > 0) {
                if (start > count - subCount) {
                    start = count - subCount; // count and subCount are both
                }
                // >= 1
                // TODO optimize charAt to direct array access
                char firstChar = subString.charAt(0);
                while (true) {
                    int i = start;
                    boolean found = false;
                    for (; i >= 0; --i) {
                        if (value[i] == firstChar) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        return -1;
                    }
                    int o1 = i, o2 = 0;
                    while (++o2 < subCount
                            && value[++o1] == subString.charAt(o2)) {
                        // Intentionally empty
                    }
                    if (o2 == subCount) {
                        return i;
                    }
                    start = i - 1;
                }
            }
            return start < count ? start : count;
        }
        return -1;
    }

    /**
     * Trims off any extra capacity beyond the current length. Note, this method
     * is NOT guaranteed to change the capacity of this object.
     *
     * @since 1.5
     */
    public void trimToSize() {
        if (count < value.length) {
            char[] newValue = new char[count];
            System.arraycopy(value, 0, newValue, 0, count);
            value = newValue;
            shared = false;
        }
    }

    /**
     * Retrieves the Unicode code point value at the {@code index}.
     *
     * @param index
     *            the index to the {@code char} code unit.
     * @return the Unicode code point value.
     * @throws IndexOutOfBoundsException
     *             if {@code index} is negative or greater than or equal to
     *             {@link #length()}.
     * @see Character
     * @see Character#codePointAt(char[], int, int)
     * @since 1.5
     */
    public int codePointAt(int index) {
        if (index < 0 || index >= count) {
            throw indexAndLength(index);
        }
        return Character.codePointAt(value, index, count);
    }

    /**
     * Retrieves the Unicode code point value that precedes the {@code index}.
     *
     * @param index
     *            the index to the {@code char} code unit within this object.
     * @return the Unicode code point value.
     * @throws IndexOutOfBoundsException
     *             if {@code index} is less than 1 or greater than
     *             {@link #length()}.
     * @see Character
     * @see Character#codePointBefore(char[], int, int)
     * @since 1.5
     */
    public int codePointBefore(int index) {
        if (index < 1 || index > count) {
            throw indexAndLength(index);
        }
        return Character.codePointBefore(value, index);
    }

    /**
     * Calculates the number of Unicode code points between {@code start}
     * and {@code end}.
     *
     * @param start
     *            the inclusive beginning index of the subsequence.
     * @param end
     *            the exclusive end index of the subsequence.
     * @return the number of Unicode code points in the subsequence.
     * @throws IndexOutOfBoundsException
     *             if {@code start} is negative or greater than
     *             {@code end} or {@code end} is greater than
     *             {@link #length()}.
     * @see Character
     * @see Character#codePointCount(char[], int, int)
     * @since 1.5
     */
    public int codePointCount(int start, int end) {
        if (start < 0 || end > count || start > end) {
            throw startEndAndLength(start, end);
        }
        return Character.codePointCount(value, start, end - start);
    }

    /**
     * Returns the index that is offset {@code codePointOffset} code points from
     * {@code index}.
     *
     * @param index
     *            the index to calculate the offset from.
     * @param codePointOffset
     *            the number of code points to count.
     * @return the index that is {@code codePointOffset} code points away from
     *         index.
     * @throws IndexOutOfBoundsException
     *             if {@code index} is negative or greater than
     *             {@link #length()} or if there aren't enough code points
     *             before or after {@code index} to match
     *             {@code codePointOffset}.
     * @see Character
     * @see Character#offsetByCodePoints(char[], int, int, int, int)
     * @since 1.5
     */
    public int offsetByCodePoints(int index, int codePointOffset) {
        return Character.offsetByCodePoints(value, 0, count, index,
                codePointOffset);
    }
}
