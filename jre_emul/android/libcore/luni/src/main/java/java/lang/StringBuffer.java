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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;

/**
 * A modifiable {@link CharSequence sequence of characters} for use in creating
 * strings, where all accesses are synchronized. This class has mostly been replaced
 * by {@link StringBuilder} because this synchronization is rarely useful. This
 * class is mainly used to interact with legacy APIs that expose it.
 *
 * <p>For particularly complex string-building needs, consider {@link java.util.Formatter}.
 *
 * <p>The majority of the modification methods on this class return {@code
 * this} so that method calls can be chained together. For example:
 * {@code new StringBuffer("a").append("b").append("c").toString()}.
 *
 * @see CharSequence
 * @see Appendable
 * @see StringBuilder
 * @see String
 * @see String#format
 * @since 1.0
 */
public final class StringBuffer extends AbstractStringBuilder implements Serializable {

    private static final long serialVersionUID = 3388685877147921107L;

    /* TODO(tball): enable when serialization is supported.
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("count", int.class),
        new ObjectStreamField("shared", boolean.class),
        new ObjectStreamField("value", char[].class),
    };
    */

    /**
     * Constructs a new StringBuffer using the default capacity which is 16.
     */
    public StringBuffer() {
    }

    /**
     * Constructs a new StringBuffer using the specified capacity.
     *
     * @param capacity
     *            the initial capacity.
     */
    public StringBuffer(int capacity) {
        super(capacity);
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
        super(string.length() + 16);
        append(string);
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
        this(cs.toString());
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
    public StringBuffer append(boolean b) {
        return append(b ? "true" : "false");
    }

    /**
     * Adds the specified character to the end of this buffer.
     *
     * @param ch
     *            the character to append.
     * @return this StringBuffer.
     * @see String#valueOf(char)
     */
    public synchronized native StringBuffer append(char ch) /*-[
      JreStringBuilder_appendChar(&self->delegate_, ch);
      return self;
    ]-*/;

    /**
     * Adds the string representation of the specified double to the end of this
     * StringBuffer.
     *
     * @param d
     *            the double to append.
     * @return this StringBuffer.
     * @see String#valueOf(double)
     */
    public StringBuffer append(double d) {
        RealToString.appendDouble(this, d);
        return this;
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
    public StringBuffer append(float f) {
        RealToString.appendFloat(this, f);
        return this;
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
    public StringBuffer append(int i) {
        IntegralToString.appendInt(this, i);
        return this;
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
    public StringBuffer append(long l) {
        IntegralToString.appendLong(this, l);
        return this;
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
    public synchronized native StringBuffer append(Object obj) /*-[
      if (obj == nil) {
        JreStringBuilder_appendNull(&self->delegate_);
      } else {
        JreStringBuilder_appendString(&self->delegate_, [obj description]);
      }
      return self;
    ]-*/;

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
    public synchronized native StringBuffer append(String string) /*-[
      JreStringBuilder_appendString(&self->delegate_, string);
      return self;
    ]-*/;

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
    public synchronized native StringBuffer append(StringBuffer sb) /*-[
      if (sb == nil) {
        JreStringBuilder_appendNull(&self->delegate_);
      } else {
        @synchronized(sb) {
          JreStringBuilder_appendBuffer(
              &self->delegate_, sb->delegate_.buffer_, sb->delegate_.count_);
        }
      }
      return self;
    ]-*/;

    /**
     * Adds the character array to the end of this buffer.
     *
     * @param chars
     *            the character array to append.
     * @return this StringBuffer.
     * @throws NullPointerException
     *            if {@code chars} is {@code null}.
     */
    public synchronized native StringBuffer append(char[] chars) /*-[
      JreStringBuilder_appendCharArray(&self->delegate_, chars);
      return self;
    ]-*/;

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
    public synchronized native StringBuffer append(char[] chars, int start, int length) /*-[
      JreStringBuilder_appendCharArraySubset(&self->delegate_, chars, start, length);
      return self;
    ]-*/;

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
    public synchronized native StringBuffer append(CharSequence s) /*-[
      if (s == nil) {
        JreStringBuilder_appendNull(&self->delegate_);
      } else {
        JreStringBuilder_appendCharSequence(&self->delegate_, s, 0, [s length]);
      }
      return self;
    ]-*/;

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
    public synchronized native StringBuffer append(CharSequence s, int start, int end) /*-[
      JreStringBuilder_appendCharSequence(&self->delegate_, s, start, end);
      return self;
    ]-*/;

    /**
     * Appends the string representation of the specified Unicode code point to
     * the end of this buffer.
     * <p>
     * The code point is converted to a {@code char[]} as defined by
     * {@link Character#toChars(int)}.
     *
     * @param codePoint
     *            the Unicode code point to encode and append.
     * @return this StringBuffer.
     * @see Character#toChars(int)
     * @since 1.5
     */
    public StringBuffer appendCodePoint(int codePoint) {
        return append(Character.toChars(codePoint));
    }

    @Override
    public synchronized char charAt(int index) {
        return super.charAt(index);
    }

    @Override
    public synchronized int codePointAt(int index) {
        return super.codePointAt(index);
    }

    @Override
    public synchronized int codePointBefore(int index) {
        return super.codePointBefore(index);
    }

    @Override
    public synchronized int codePointCount(int beginIndex, int endIndex) {
        return super.codePointCount(beginIndex, endIndex);
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
    public synchronized native StringBuffer delete(int start, int end) /*-[
      JreStringBuilder_delete(&self->delegate_, start, end);
      return self;
    ]-*/;

    /**
     * Deletes the character at the specified offset.
     *
     * @param location
     *            the offset of the character to delete.
     * @return this StringBuffer.
     * @throws StringIndexOutOfBoundsException
     *             if {@code location < 0} or {@code location >= length()}
     */
    public synchronized native StringBuffer deleteCharAt(int location) /*-[
      JreStringBuilder_deleteCharAt(&self->delegate_, location);
      return self;
    ]-*/;

    @Override
    public synchronized void ensureCapacity(int min) {
        super.ensureCapacity(min);
    }

    /**
     * Copies the requested sequence of characters to the {@code char[]} passed
     * starting at {@code idx}.
     *
     * @param start
     *            the starting offset of characters to copy.
     * @param end
     *            the ending offset of characters to copy.
     * @param buffer
     *            the destination character array.
     * @param idx
     *            the starting offset in the character array.
     * @throws IndexOutOfBoundsException
     *             if {@code start < 0}, {@code end > length()}, {@code start >
     *             end}, {@code index < 0}, {@code end - start > buffer.length -
     *             index}
     */
    @Override
    public synchronized void getChars(int start, int end, char[] buffer, int idx) {
        super.getChars(start, end, buffer, idx);
    }

    @Override
    public synchronized int indexOf(String subString, int start) {
        return super.indexOf(subString, start);
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
    public synchronized native StringBuffer insert(int index, char ch) /*-[
      JreStringBuilder_insertChar(&self->delegate_, index, ch);
      return self;
    ]-*/;

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
    public StringBuffer insert(int index, boolean b) {
        return insert(index, b ? "true" : "false");
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
    public StringBuffer insert(int index, int i) {
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
    public StringBuffer insert(int index, long l) {
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
    public StringBuffer insert(int index, double d) {
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
    public StringBuffer insert(int index, float f) {
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
    public StringBuffer insert(int index, Object obj) {
        return insert(index, obj == null ? "null" : obj.toString());
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
    public synchronized native StringBuffer insert(int index, String string) /*-[
      JreStringBuilder_insertString(&self->delegate_, index, string);
      return self;
    ]-*/;

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
    public synchronized native StringBuffer insert(int index, char[] chars) /*-[
      JreStringBuilder_insertCharArray(&self->delegate_, index, chars);
      return self;
    ]-*/;

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
    public synchronized native StringBuffer insert(
        int index, char[] chars, int start, int length) /*-[
      JreStringBuilder_insertCharArraySubset(&self->delegate_, index, chars, start, length);
      return self;
    ]-*/;

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
    public synchronized native StringBuffer insert(int index, CharSequence s) /*-[
      JreStringBuilder_insertString(&self->delegate_, index, s == nil ? @"null" : [s description]);
      return self;
    ]-*/;

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
    public synchronized native StringBuffer insert(int index, CharSequence s,
            int start, int end) /*-[
      JreStringBuilder_insertCharSequence(&self->delegate_, index, s, start, end);
      return self;
    ]-*/;

    @Override
    public synchronized int lastIndexOf(String subString, int start) {
        return super.lastIndexOf(subString, start);
    }

    @Override
    public int length() {
        return super.length();
    }

    @Override
    public synchronized int offsetByCodePoints(int index, int codePointOffset) {
        return super.offsetByCodePoints(index, codePointOffset);
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
    public synchronized native StringBuffer replace(int start, int end, String string) /*-[
      JreStringBuilder_replace(&self->delegate_, start, end, string);
      return self;
    ]-*/;

    /**
     * Reverses the order of characters in this buffer.
     *
     * @return this buffer.
     */
    public synchronized native StringBuffer reverse() /*-[
      JreStringBuilder_reverse(&self->delegate_);
      return self;
    ]-*/;

    @Override
    public synchronized void setCharAt(int index, char ch) {
        super.setCharAt(index, ch);
    }

    @Override
    public synchronized void setLength(int length) {
        super.setLength(length);
    }

    @Override
    public synchronized CharSequence subSequence(int start, int end) {
        return super.substring(start, end);
    }

    @Override
    public synchronized String substring(int start) {
        return super.substring(start);
    }

    @Override
    public synchronized String substring(int start, int end) {
        return super.substring(start, end);
    }

    @Override
    public synchronized native String toString() /*-[
      return JreStringBuilder_toString(&self->delegate_);
    ]-*/;

    @Override
    public synchronized void trimToSize() {
        super.trimToSize();
    }

    /* TODO(tball): enable when serialization is supported.
    private synchronized void writeObject(ObjectOutputStream out)
            throws IOException {
        ObjectOutputStream.PutField fields = out.putFields();
        fields.put("count", length());
        fields.put("shared", false);
        fields.put("value", getValue());
        out.writeFields();
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        ObjectInputStream.GetField fields = in.readFields();
        int count = fields.get("count", 0);
        char[] value = (char[]) fields.get("value", null);
        set(value, count);
    }
    */
}
