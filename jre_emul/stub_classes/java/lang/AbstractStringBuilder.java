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

    /*
     * Returns the character array.
     */
    final char[] getValue() {
        return null;
    }

    /*
     * Returns the underlying buffer and sets the shared flag.
     */
    final char[] shareValue() {
        return null;
    }

    /*
     * Restores internal state after deserialization.
     */
    final void set(char[] val, int len) throws InvalidObjectException {
    }

    AbstractStringBuilder() {
    }

    AbstractStringBuilder(int capacity) {
    }

    AbstractStringBuilder(String string) {
    }

    final void appendNull() {
    }

    final void append0(char[] chars) {
    }

    final void append0(char[] chars, int offset, int length) {
    }

    final void append0(char ch) {
    }

    final void append0(String string) {
    }

    final void append0(CharSequence s, int start, int end) {
    }

    /**
     * Returns the number of characters that can be held without growing.
     *
     * @return the capacity
     * @see #ensureCapacity
     * @see #length
     */
    public int capacity() {
        return 0;
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
        return 0;
    }

    final void delete0(int start, int end) {
    }

    final void deleteCharAt0(int index) {
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
    }

    final void insert0(int index, char[] chars) {
    }

    final void insert0(int index, char[] chars, int start, int length) {
    }

    final void insert0(int index, char ch) {
    }

    final void insert0(int index, String string) {
    }

    final void insert0(int index, CharSequence s, int start, int end) {
    }

    /**
     * The current length.
     *
     * @return the number of characters contained in this instance.
     */
    public int length() {
        return 0;
    }

    final void replace0(int start, int end, String string) {
    }

    final void reverse0() {
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
        return null;
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
        return null;
    }

    /**
     * Returns the current String representation.
     *
     * @return a String containing the characters in this instance.
     */
    @Override
    public String toString() {
        return null;
    }

    protected String toString0() {
        return null;
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
        return null;
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
        return 0;
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
        return 0;
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
        return 0;
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
        return 0;
    }

    /**
     * Trims off any extra capacity beyond the current length. Note, this method
     * is NOT guaranteed to change the capacity of this object.
     *
     * @since 1.5
     */
    public void trimToSize() {
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
        return 0;
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
        return 0;
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
        return 0;
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
        return 0;
    }
}
