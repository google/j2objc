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

import java.io.Serializable;
import java.util.Arrays;

/**
 * StringBuffer is a variable size contiguous indexable array of characters. The
 * length of the StringBuffer is the number of characters it contains. The
 * capacity of the StringBuffer is the number of characters it can hold.
 * <p>
 * Characters may be inserted at any position up to the length of the
 * StringBuffer, increasing the length of the StringBuffer. Characters at any
 * position in the StringBuffer may be replaced, which does not affect the
 * StringBuffer length.
 * <p>
 * The capacity of a StringBuffer may be specified when the StringBuffer is
 * created. If the capacity of the StringBuffer is exceeded, the capacity is
 * increased.
 * 
 * @see String
 * @see StringBuilder
 * @since 1.0
 */
public final class StringBuffer implements Appendable, Serializable, CharSequence {
  static final int INITIAL_CAPACITY = 16;

    private char[] value;
  
    private int count;
  
    private boolean shared;

    private static final long serialVersionUID = 3388685877147921107L;

    /**
     * Constructs a new StringBuffer using the default capacity which is 16.
     */
    public StringBuffer() {
        value = new char[INITIAL_CAPACITY];
    }

    /**
     * Constructs a new StringBuffer using the specified capacity.
     * 
     * @param capacity
     *            the initial capacity.
     */
    public StringBuffer(int capacity) {
      value = new char[INITIAL_CAPACITY];
    }

    /**
     * Constructs a new StringBuffer containing the characters in the specified
     * string. The capacity of the new buffer will be the length of the
     * {@code String} plus the default capacity.
     * 
     * @param string
     *            the string content with which to initialize the new instance.
     * @throws NullPointerException
     *            if {@code string} is {@code null}.
     */
    public StringBuffer(String string) {
      this((CharSequence) string);
    }

    /**
     * Constructs a StringBuffer and initializes it with the content from the
     * specified {@code CharSequence}. The capacity of the new buffer will be
     * the length of the {@code CharSequence} plus the default capacity.
     * 
     * @param cs
     *            the content to initialize the instance.
     * @throws NullPointerException
     *            if {@code cs} is {@code null}.
     * @since 1.5
     */
    public StringBuffer(CharSequence cs) {
        if (cs == null) {
            throw new NullPointerException();
        }
        count = cs.length();
        shared = false;
        value = new char[count + INITIAL_CAPACITY];
        cs.toString().getChars(0, count, value, 0);
    }

    /**
     * Adds the string representation of the specified boolean to the end of
     * this StringBuffer.
     * <p>
     * If the argument is {@code true} the string {@code "true"} is appended,
     * otherwise the string {@code "false"} is appended.
     *
     * @param b
     *            the boolean to append.
     * @return this StringBuffer.
     * @see String#valueOf(boolean)
     */
    public synchronized StringBuffer append(boolean b) {
        return append(b ? "true" : "false"); //$NON-NLS-1$//$NON-NLS-2$
    }

    /**
     * Adds the specified character to the end of this buffer.
     * 
     * @param ch
     *            the character to append.
     * @return this StringBuffer.
     * @see String#valueOf(char)
     */
    public synchronized StringBuffer append(char ch) {
        if (count == value.length) {
            enlargeBuffer(count + 1);
        }
        value[count++] = ch;
        return this;
    }

    /**
     * Adds the string representation of the specified double to the end of this
     * StringBuffer.
     * 
     * @param d
     *            the double to append.
     * @return this StringBuffer.
     * @see String#valueOf(double)
     */
    public synchronized StringBuffer append(double d) {
        return append(Double.toString(d));
    }

    /**
     * Adds the string representation of the specified float to the end of this
     * StringBuffer.
     * 
     * @param f
     *            the float to append.
     * @return this StringBuffer.
     * @see String#valueOf(float)
     */
    public synchronized StringBuffer append(float f) {
        return append(Float.toString(f));
    }

    /**
     * Adds the string representation of the specified integer to the end of
     * this StringBuffer.
     * 
     * @param i
     *            the integer to append.
     * @return this StringBuffer.
     * @see String#valueOf(int)
     */
    public synchronized StringBuffer append(int i) {
        return append(Integer.toString(i));
    }

    /**
     * Adds the string representation of the specified long to the end of this
     * StringBuffer.
     * 
     * @param l
     *            the long to append.
     * @return this StringBuffer.
     * @see String#valueOf(long)
     */
    public synchronized StringBuffer append(long l) {
        return append(Long.toString(l));
    }

    /**
     * Adds the string representation of the specified object to the end of this
     * StringBuffer.
     * <p>
     * If the specified object is {@code null} the string {@code "null"} is
     * appended, otherwise the objects {@code toString} is used to get its
     * string representation.
     *
     * @param obj
     *            the object to append (may be null).
     * @return this StringBuffer.
     * @see String#valueOf(Object)
     */
    public synchronized StringBuffer append(Object obj) {
        if (obj == null) {
            appendNull();
        } else {
            append(obj.toString());
        }
        return this;
    }

    /**
     * Adds the specified string to the end of this buffer.
     * <p>
     * If the specified string is {@code null} the string {@code "null"} is
     * appended, otherwise the contents of the specified string is appended.
     *
     * @param string
     *            the string to append (may be null).
     * @return this StringBuffer.
     */
    public synchronized StringBuffer append(String string) {
        if (string == null) {
            appendNull();
            return this;
        }
        int adding = string.length();
        int newSize = count + adding;
        if (newSize > value.length) {
            enlargeBuffer(newSize);
        }
        string.getChars(0, adding, value, count);
        count = newSize;
        return this;
    }

    /**
     * Adds the specified StringBuffer to the end of this buffer.
     * <p>
     * If the specified StringBuffer is {@code null} the string {@code "null"}
     * is appended, otherwise the contents of the specified StringBuffer is
     * appended.
     *
     * @param sb
     *            the StringBuffer to append (may be null).
     * @return this StringBuffer.
     * 
     * @since 1.4
     */
    public synchronized StringBuffer append(StringBuffer sb) {
        if (sb == null) {
            appendNull();
        } else {
            synchronized (sb) {
                append(sb.getValue(), 0, sb.length());
            }
        }
        return this;
    }

    /**
     * Adds the character array to the end of this buffer.
     * 
     * @param chars
     *            the character array to append.
     * @return this StringBuffer.
     * @throws NullPointerException
     *            if {@code chars} is {@code null}.
     */
    public synchronized StringBuffer append(char chars[]) {
        int newSize = count + chars.length;
        if (newSize > value.length) {
            enlargeBuffer(newSize);
        }
        System.arraycopy(chars, 0, value, count, chars.length);
        count = newSize;
        return this;
    }

    /**
     * Adds the specified sequence of characters to the end of this buffer.
     * 
     * @param chars
     *            the character array to append.
     * @param start
     *            the starting offset.
     * @param length
     *            the number of characters.
     * @return this StringBuffer.
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code length < 0} , {@code start < 0} or {@code start +
     *             length > chars.length}.
     * @throws NullPointerException
     *            if {@code chars} is {@code null}.
     */
    public synchronized StringBuffer append(char chars[], int start, int length) {
        // Force null check of chars first!
        if (chars == null) {
            throw new NullPointerException();
        }
        if (start > chars.length || start < 0) {
            // luni.12=Offset out of bounds \: {0}
            throw new ArrayIndexOutOfBoundsException("Offset out of bounds: " + start);
        }
        if (length < 0 || chars.length - start < length) {
            // luni.18=Length out of bounds \: {0}
            throw new ArrayIndexOutOfBoundsException("Length out of bounds: " + length);
        }

        int newSize = count + length;
        if (newSize > value.length) {
            enlargeBuffer(newSize);
        }
        System.arraycopy(chars, start, value, count, length);
        count = newSize;
        return this;
    }

    /**
     * Appends the specified CharSequence to this buffer.
     * <p>
     * If the specified CharSequence is {@code null} the string {@code "null"}
     * is appended, otherwise the contents of the specified CharSequence is
     * appended.
     *
     * @param s
     *            the CharSequence to append.
     * @return this StringBuffer.
     * @since 1.5
     */
    public synchronized StringBuffer append(CharSequence s) {
        if (s == null) {
            appendNull();
        } else {
            append(s.toString());
        }
        return this;
    }

    /**
     * Appends the specified subsequence of the CharSequence to this buffer.
     * <p>
     * If the specified CharSequence is {@code null}, then the string {@code
     * "null"} is used to extract a subsequence.
     *
     * @param s
     *            the CharSequence to append.
     * @param start
     *            the inclusive start index.
     * @param end
     *            the exclusive end index.
     * @return this StringBuffer.
     * @throws IndexOutOfBoundsException
     *             if {@code start} or {@code end} are negative, {@code start}
     *             is greater than {@code end} or {@code end} is greater than
     *             the length of {@code s}.
     * @since 1.5
     */
    public synchronized StringBuffer append(CharSequence s, int start, int end) {
        if (s == null) {
            s = (CharSequence) "null"; //$NON-NLS-1$
        }
        if (start < 0 || end < 0 || start > end || end > s.length()) {
            throw new IndexOutOfBoundsException();
        }

        return append(s.subSequence(start, end).toString());
    }

    public synchronized char charAt(int index) {
        if (index < 0 || index >= count) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return value[index];
    }

    /**
     * Deletes a range of characters.
     * 
     * @param start
     *            the offset of the first character.
     * @param end
     *            the offset one past the last character.
     * @return this StringBuffer.
     * @throws StringIndexOutOfBoundsException
     *             if {@code start < 0}, {@code start > end} or {@code end >
     *             length()}.
     */
    public synchronized StringBuffer delete(int start, int end) {
        if (start >= 0) {
            if (end > count) {
                end = count;
            }
            if (end == start) {
                return this;
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
                return this;
            }
        }
        throw new StringIndexOutOfBoundsException();
    }

    /**
     * Deletes the character at the specified offset.
     * 
     * @param index
     *            the offset of the character to delete.
     * @return this StringBuffer.
     * @throws StringIndexOutOfBoundsException
     *             if {@code location < 0} or {@code location >= length()}
     */
    public synchronized StringBuffer deleteCharAt(int index) {
        if (0 > index || index >= count) {
            throw new StringIndexOutOfBoundsException(index);
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
        return this;
    }

    public synchronized void ensureCapacity(int min) {
        if (min > value.length) {
            int twice = (value.length << 1) + 2;
            enlargeBuffer(twice > min ? twice : min);
        }
    }

    /**
     * Copies the requested sequence of characters to the {@code char[]} passed
     * starting at {@code destStart}.
     *
     * @param start
     *            the inclusive start index of the characters to copy.
     * @param end
     *            the exclusive end index of the characters to copy.
     * @param dest
     *            the {@code char[]} to copy the characters to.
     * @param destStart
     *            the inclusive start index of {@code dest} to begin copying to.
     * @throws IndexOutOfBoundsException
     *             if the {@code start} is negative, the {@code destStart} is
     *             negative, the {@code start} is greater than {@code end}, the
     *             {@code end} is greater than the current {@link #length()} or
     *             {@code destStart + end - begin} is greater than
     *             {@code dest.length}.
     */
    public synchronized void getChars(int start, int end, char[] dest, int destStart) {
        if (start > count || end > count || start > end) {
            throw new StringIndexOutOfBoundsException();
        }
        System.arraycopy(value, start, dest, destStart, end - start);
    }

    public synchronized int indexOf(String subString, int start) {
        if (subString == null) {
            throw new NullPointerException();
        }
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
     * Inserts the character into this buffer at the specified offset.
     * 
     * @param index
     *            the index at which to insert.
     * @param ch
     *            the character to insert.
     * @return this buffer.
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code index < 0} or {@code index > length()}.
     */
    public synchronized StringBuffer insert(int index, char ch) {
        if (0 > index || index > count) {
            // RI compatible exception type
            throw new ArrayIndexOutOfBoundsException(index);
        }
        move(1, index);
        value[index] = ch;
        count++;
        return this;
    }

    /**
     * Inserts the string representation of the specified boolean into this
     * buffer at the specified offset.
     * 
     * @param index
     *            the index at which to insert.
     * @param b
     *            the boolean to insert.
     * @return this buffer.
     * @throws StringIndexOutOfBoundsException
     *             if {@code index < 0} or {@code index > length()}.
     */
    public synchronized StringBuffer insert(int index, boolean b) {
        return insert(index, b ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Inserts the string representation of the specified integer into this
     * buffer at the specified offset.
     * 
     * @param index
     *            the index at which to insert.
     * @param i
     *            the integer to insert.
     * @return this buffer.
     * @throws StringIndexOutOfBoundsException
     *             if {@code index < 0} or {@code index > length()}.
     */
    public synchronized StringBuffer insert(int index, int i) {
        return insert(index, Integer.toString(i));
    }

    /**
     * Inserts the string representation of the specified long into this buffer
     * at the specified offset.
     * 
     * @param index
     *            the index at which to insert.
     * @param l
     *            the long to insert.
     * @return this buffer.
     * @throws StringIndexOutOfBoundsException
     *             if {@code index < 0} or {@code index > length()}.
     */
    public synchronized StringBuffer insert(int index, long l) {
        return insert(index, Long.toString(l));
    }

    /**
     * Inserts the string representation of the specified into this buffer
     * double at the specified offset.
     * 
     * @param index
     *            the index at which to insert.
     * @param d
     *            the double to insert.
     * @return this buffer.
     * @throws StringIndexOutOfBoundsException
     *             if {@code index < 0} or {@code index > length()}.
     */
    public synchronized StringBuffer insert(int index, double d) {
        return insert(index, Double.toString(d));
    }

    /**
     * Inserts the string representation of the specified float into this buffer
     * at the specified offset.
     * 
     * @param index
     *            the index at which to insert.
     * @param f
     *            the float to insert.
     * @return this buffer.
     * @throws StringIndexOutOfBoundsException
     *             if {@code index < 0} or {@code index > length()}.
     */
    public synchronized StringBuffer insert(int index, float f) {
        return insert(index, Float.toString(f));
    }

    /**
     * Inserts the string representation of the specified object into this
     * buffer at the specified offset.
     * <p>
     * If the specified object is {@code null}, the string {@code "null"} is
     * inserted, otherwise the objects {@code toString} method is used to get
     * its string representation.
     *
     * @param index
     *            the index at which to insert.
     * @param obj
     *            the object to insert (may be null).
     * @return this buffer.
     * @throws StringIndexOutOfBoundsException
     *             if {@code index < 0} or {@code index > length()}.
     */
    public synchronized StringBuffer insert(int index, Object obj) {
        return insert(index, obj == null ? "null" : obj.toString()); //$NON-NLS-1$
    }

    /**
     * Inserts the string into this buffer at the specified offset.
     * <p>
     * If the specified string is {@code null}, the string {@code "null"} is
     * inserted, otherwise the contents of the string is inserted.
     *
     * @param index
     *            the index at which to insert.
     * @param string
     *            the string to insert (may be null).
     * @return this buffer.
     * @throws StringIndexOutOfBoundsException
     *             if {@code index < 0} or {@code index > length()}.
     */
    public synchronized StringBuffer insert(int index, String string) {
        if (0 <= index && index <= count) {
            if (string == null) {
                string = "null"; //$NON-NLS-1$
            }
            int min = string.length();
            if (min != 0) {
                move(min, index);
                string.getChars(0, min, value, index);
                count += min;
            }
        } else {
            throw new StringIndexOutOfBoundsException(index);
        }
        return this;
    }

    /**
     * Inserts the character array into this buffer at the specified offset.
     * 
     * @param index
     *            the index at which to insert.
     * @param chars
     *            the character array to insert.
     * @return this buffer.
     * @throws StringIndexOutOfBoundsException
     *             if {@code index < 0} or {@code index > length()}.
     * @throws NullPointerException
     *            if {@code chars} is {@code null}.
     */
    public synchronized StringBuffer insert(int index, char[] chars) {
        if (0 > index || index > count) {
            throw new StringIndexOutOfBoundsException(index);
        }
        if (chars.length != 0) {
            move(chars.length, index);
            System.arraycopy(chars, 0, value, index, chars.length);
            count += chars.length;
        }
        return this;
    }

    /**
     * Inserts the specified subsequence of characters into this buffer at the
     * specified index.
     * 
     * @param index
     *            the index at which to insert.
     * @param chars
     *            the character array to insert.
     * @param start
     *            the starting offset.
     * @param length
     *            the number of characters.
     * @return this buffer.
     * @throws NullPointerException
     *             if {@code chars} is {@code null}.
     * @throws StringIndexOutOfBoundsException
     *             if {@code length < 0}, {@code start < 0}, {@code start +
     *             length > chars.length}, {@code index < 0} or {@code index >
     *             length()}
     */
    public synchronized StringBuffer insert(int index, char chars[], int start,
            int length) {
        if (0 <= index && index <= count) {
            // start + length could overflow, start/length maybe MaxInt
            if (start >= 0 && 0 <= length && length <= chars.length - start) {
                if (length != 0) {
                    move(length, index);
                    System.arraycopy(chars, start, value, index, length);
                    count += length;
                }
                return this;
            }
            throw new StringIndexOutOfBoundsException("offset " + start //$NON-NLS-1$
                    + ", length " + length //$NON-NLS-1$
                    + ", char[].length " + chars.length); //$NON-NLS-1$
        }
        throw new StringIndexOutOfBoundsException(index);
    }

    /**
     * Inserts the specified CharSequence into this buffer at the specified
     * index.
     * <p>
     * If the specified CharSequence is {@code null}, the string {@code "null"}
     * is inserted, otherwise the contents of the CharSequence.
     *
     * @param index
     *            The index at which to insert.
     * @param s
     *            The char sequence to insert.
     * @return this buffer.
     * @throws IndexOutOfBoundsException
     *             if {@code index < 0} or {@code index > length()}.
     * @since 1.5
     */
    public synchronized StringBuffer insert(int index, CharSequence s) {
        insert(index, s == null ? "null" : s.toString()); //$NON-NLS-1$
        return this;
    }

    /**
     * Inserts the specified subsequence into this buffer at the specified
     * index.
     * <p>
     * If the specified CharSequence is {@code null}, the string {@code "null"}
     * is inserted, otherwise the contents of the CharSequence.
     * 
     * @param index
     *            The index at which to insert.
     * @param s
     *            The char sequence to insert.
     * @param start
     *            The inclusive start index in the char sequence.
     * @param end
     *            The exclusive end index in the char sequence.
     * @return this buffer.
     * @throws IndexOutOfBoundsException
     *             if {@code index} is negative or greater than the current
     *             length, {@code start} or {@code end} are negative, {@code
     *             start} is greater than {@code end} or {@code end} is greater
     *             than the length of {@code s}.
     * @since 1.5
     */
    public synchronized StringBuffer insert(int index, CharSequence s,
            int start, int end) {
        if (s == null) {
            s = (CharSequence) "null"; //$NON-NLS-1$
        }
        if (index < 0 || index > count || start < 0 || end < 0 || start > end
                || end > s.length()) {
            throw new IndexOutOfBoundsException();
        }
        insert(index, s.subSequence(start, end).toString());
        return this;
    }

    /**
     * Replaces the characters in the specified range with the contents of the
     * specified string.
     * 
     * @param start
     *            the inclusive begin index.
     * @param end
     *            the exclusive end index.
     * @param string
     *            the string that will replace the contents in the range.
     * @return this buffer.
     * @throws StringIndexOutOfBoundsException
     *             if {@code start} or {@code end} are negative, {@code start}
     *             is greater than {@code end} or {@code end} is greater than
     *             the length of {@code s}.
     */
    public synchronized StringBuffer replace(int start, int end, String string) {
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
                return this;
            }
            if (start == end) {
                if (string == null) {
                    throw new NullPointerException();
                }
                insert(start, string);
                return this;
            }
        }
        throw new StringIndexOutOfBoundsException();
    }

    /**
     * Reverses the order of characters in this buffer.
     * 
     * @return this buffer.
     */
    public synchronized StringBuffer reverse() {
        if (count < 2) {
            return this;
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
                    return this;
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
        return this;
    }
    
    private void enlargeBuffer(int min) {
        int newSize = ((value.length >> 1) + value.length) + 2;
        char[] newData = new char[min > newSize ? min : newSize];
        System.arraycopy(value, 0, newData, 0, count);
        value = newData;
        shared = false;
    }

    final void appendNull() {
        int newSize = count + 4;
        if (newSize > value.length) {
            enlargeBuffer(newSize);
        }
        value[count++] = 'n';
        value[count++] = 'u';
        value[count++] = 'l';
        value[count++] = 'l';
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
     * The current length.
     * 
     * @return the number of characters contained in this instance.
     */
    public int length() {
        return count;
    }

    private void move(int size, int index) {
        int newSize;
        if (value.length - count >= size) {
            if (!shared) {
                System.arraycopy(value, index, value, index + size, count
                        - index); // index == count case is no-op
                return;
            }
            newSize = value.length;
        } else {
            int a = count + size, b = (value.length << 1) + 2;
            newSize = a > b ? a : b;
        }

        char[] newData = new char[newSize];
        System.arraycopy(value, 0, newData, 0, index);
        // index == count case is no-op
        System.arraycopy(value, index, newData, index + size, count - index);
        value = newData;
        shared = false;
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
    public synchronized void setCharAt(int index, char ch) {
        if (0 > index || index >= count) {
            throw new StringIndexOutOfBoundsException(index);
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
    public synchronized void setLength(int length) {
        if (length < 0) {
            throw new StringIndexOutOfBoundsException(length);
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
    public synchronized String substring(int start) {
        if (0 <= start && start <= count) {
            if (start == count) {
                return ""; //$NON-NLS-1$
            }

            // Remove String sharing for more performance
            return new String(value, start, count - start);
        }
        throw new StringIndexOutOfBoundsException(start);
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
    public synchronized String substring(int start, int end) {
        if (0 <= start && start <= end && end <= count) {
            if (start == end) {
                return ""; //$NON-NLS-1$
            }

            // Remove String sharing for more performance
            return new String(value, start, end - start);
        }
        throw new StringIndexOutOfBoundsException();
    }

    /**
     * Returns the current String representation.
     * 
     * @return a String containing the characters in this instance.
     */
    @Override
    public String toString() {
        if (count == 0) {
            return ""; //$NON-NLS-1$
        }
        // Optimize String sharing for more performance
        int wasted = value.length - count;
        if (wasted >= 256
                || (wasted >= INITIAL_CAPACITY && wasted >= (count >> 1))) {
            return new String(value, 0, count);
        }
        shared = true;
        return new String(value, 0, count);
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
    public synchronized CharSequence subSequence(int start, int end) {
        return (CharSequence) substring(start, end);
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
    
    public synchronized int lastIndexOf(String subString, int start) {
        if (subString == null) {
            throw new NullPointerException();
        }
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
    public synchronized void trimToSize() {
        if (count < value.length) {
            char[] newValue = new char[count];
            System.arraycopy(value, 0, newValue, 0, count);
            value = newValue;
            shared = false;
        }
    }

    final char[] getValue() {
        return value;
    }
}
