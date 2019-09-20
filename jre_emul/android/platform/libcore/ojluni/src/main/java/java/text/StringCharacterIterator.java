/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

package java.text;

/**
 * An implementation of {@link CharacterIterator} for strings.
 */
public final class StringCharacterIterator implements CharacterIterator {

    String string;

    int start, end, offset;

    /**
     * Constructs a new {@code StringCharacterIterator} on the specified string.
     * The begin and current indices are set to the beginning of the string, the
     * end index is set to the length of the string.
     *
     * @param value
     *            the source string to iterate over.
     */
    public StringCharacterIterator(String value) {
        string = value;
        start = offset = 0;
        end = string.length();
    }

    /**
     * Constructs a new {@code StringCharacterIterator} on the specified string
     * with the current index set to the specified value. The begin index is set
     * to the beginning of the string, the end index is set to the length of the
     * string.
     *
     * @param value
     *            the source string to iterate over.
     * @param location
     *            the current index.
     * @throws IllegalArgumentException
     *            if {@code location} is negative or greater than the length
     *            of the source string.
     */
    public StringCharacterIterator(String value, int location) {
        string = value;
        start = 0;
        end = string.length();
        if (location < 0 || location > end) {
            throw new IllegalArgumentException();
        }
        offset = location;
    }

    /**
     * Constructs a new {@code StringCharacterIterator} on the specified string
     * with the begin, end and current index set to the specified values.
     *
     * @param value
     *            the source string to iterate over.
     * @param start
     *            the index of the first character to iterate.
     * @param end
     *            the index one past the last character to iterate.
     * @param location
     *            the current index.
     * @throws IllegalArgumentException
     *            if {@code start < 0}, {@code start > end}, {@code location <
     *            start}, {@code location > end} or if {@code end} is greater
     *            than the length of {@code value}.
     */
    public StringCharacterIterator(String value, int start, int end, int location) {
        string = value;
        if (start < 0 || end > string.length() || start > end
                || location < start || location > end) {
            throw new IllegalArgumentException();
        }
        this.start = start;
        this.end = end;
        offset = location;
    }

    /**
     * Returns a new {@code StringCharacterIterator} with the same source
     * string, begin, end, and current index as this iterator.
     *
     * @return a shallow copy of this iterator.
     * @see java.lang.Cloneable
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns the character at the current index in the source string.
     *
     * @return the current character, or {@code DONE} if the current index is
     *         past the end.
     */
    public char current() {
        if (offset == end) {
            return DONE;
        }
        return string.charAt(offset);
    }

    /**
     * Compares the specified object with this {@code StringCharacterIterator}
     * and indicates if they are equal. In order to be equal, {@code object}
     * must be an instance of {@code StringCharacterIterator} that iterates over
     * the same sequence of characters with the same index.
     *
     * @param object
     *            the object to compare with this object.
     * @return {@code true} if the specified object is equal to this
     *         {@code StringCharacterIterator}; {@code false} otherwise.
     * @see #hashCode
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof StringCharacterIterator)) {
            return false;
        }
        StringCharacterIterator it = (StringCharacterIterator) object;
        return string.equals(it.string) && start == it.start && end == it.end
                && offset == it.offset;
    }

    /**
     * Sets the current position to the begin index and returns the character at
     * the new position in the source string.
     *
     * @return the character at the begin index or {@code DONE} if the begin
     *         index is equal to the end index.
     */
    public char first() {
        if (start == end) {
            return DONE;
        }
        offset = start;
        return string.charAt(offset);
    }

    /**
     * Returns the begin index in the source string.
     *
     * @return the index of the first character of the iteration.
     */
    public int getBeginIndex() {
        return start;
    }

    /**
     * Returns the end index in the source string.
     *
     * @return the index one past the last character of the iteration.
     */
    public int getEndIndex() {
        return end;
    }

    /**
     * Returns the current index in the source string.
     *
     * @return the current index.
     */
    public int getIndex() {
        return offset;
    }

    @Override
    public int hashCode() {
        return string.hashCode() + start + end + offset;
    }

    /**
     * Sets the current position to the end index - 1 and returns the character
     * at the new position.
     *
     * @return the character before the end index or {@code DONE} if the begin
     *         index is equal to the end index.
     */
    public char last() {
        if (start == end) {
            return DONE;
        }
        offset = end - 1;
        return string.charAt(offset);
    }

    /**
     * Increments the current index and returns the character at the new index.
     *
     * @return the character at the next index, or {@code DONE} if the next
     *         index would be past the end.
     */
    public char next() {
        if (offset >= (end - 1)) {
            offset = end;
            return DONE;
        }
        return string.charAt(++offset);
    }

    /**
     * Decrements the current index and returns the character at the new index.
     *
     * @return the character at the previous index, or {@code DONE} if the
     *         previous index would be past the beginning.
     */
    public char previous() {
        if (offset == start) {
            return DONE;
        }
        return string.charAt(--offset);
    }

    /**
     * Sets the current index in the source string.
     *
     * @param location
     *            the index the current position is set to.
     * @return the character at the new index, or {@code DONE} if
     *         {@code location} is set to the end index.
     * @throws IllegalArgumentException
     *            if {@code location} is smaller than the begin index or greater
     *            than the end index.
     */
    public char setIndex(int location) {
        if (location < start || location > end) {
            throw new IllegalArgumentException();
        }
        offset = location;
        if (offset == end) {
            return DONE;
        }
        return string.charAt(offset);
    }

    /**
     * Sets the source string to iterate over. The begin and end positions are
     * set to the start and end of this string.
     *
     * @param value
     *            the new source string.
     */
    public void setText(String value) {
        string = value;
        start = offset = 0;
        end = value.length();
    }
}
