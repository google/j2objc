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
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import libcore.util.EmptyArray;

/*-[
#import "java/lang/Character.h"
#import "java/lang/Double.h"
#import "java/lang/Float.h"
#import "java/lang/Integer.h"
#import "java/lang/Long.h"
]-*/

/**
 * A modifiable {@link CharSequence sequence of characters} for use in creating
 * strings. This class is intended as a direct replacement of
 * {@link StringBuffer} for non-concurrent use; unlike {@code StringBuffer} this
 * class is not synchronized.
 *
 * <p>For particularly complex string-building needs, consider {@link java.util.Formatter}.
 *
 * <p>The majority of the modification methods on this class return {@code
 * this} so that method calls can be chained together. For example:
 * {@code new StringBuilder("a").append("b").append("c").toString()}.
 *
 * @see CharSequence
 * @see Appendable
 * @see StringBuffer
 * @see String
 * @see String#format
 * @since 1.5
 */
public final class StringBuilder extends AbstractStringBuilder implements
        Appendable, CharSequence, Serializable {

    private static final long serialVersionUID = 4383685877147921099L;

    /**
     * Constructs an instance with an initial capacity of {@code 16}.
     *
     * @see #capacity()
     */
    public StringBuilder() {
    }

    /**
     * Constructs an instance with the specified capacity.
     *
     * @param capacity
     *            the initial capacity to use.
     * @throws NegativeArraySizeException
     *             if the specified {@code capacity} is negative.
     * @see #capacity()
     */
    public StringBuilder(int capacity) {
        super(capacity);
    }

    /**
     * Constructs an instance that's initialized with the contents of the
     * specified {@code CharSequence}. The capacity of the new builder will be
     * the length of the {@code CharSequence} plus 16.
     *
     * @param seq
     *            the {@code CharSequence} to copy into the builder.
     * @throws NullPointerException
     *            if {@code seq} is {@code null}.
     */
    public StringBuilder(CharSequence seq) {
        this(seq.toString());
    }

    /**
     * Constructs an instance that's initialized with the contents of the
     * specified {@code String}. The capacity of the new builder will be the
     * length of the {@code String} plus 16.
     *
     * @param str
     *            the {@code String} to copy into the builder.
     * @throws NullPointerException
     *            if {@code str} is {@code null}.
     */
    public StringBuilder(String str) {
        super(str.length() + 16);
        append(str);
    }

    /**
     * Appends the string representation of the specified {@code boolean} value.
     * The {@code boolean} value is converted to a String according to the rule
     * defined by {@link String#valueOf(boolean)}.
     *
     * @param b
     *            the {@code boolean} value to append.
     * @return this builder.
     * @see String#valueOf(boolean)
     */
    public native StringBuilder append(boolean b) /*-[
      JreStringBuilder_appendString(&self->delegate_, b ? @"true" : @"false");
      return self;
    ]-*/;

    /**
     * Appends the string representation of the specified {@code char} value.
     * The {@code char} value is converted to a string according to the rule
     * defined by {@link String#valueOf(char)}.
     *
     * @param c
     *            the {@code char} value to append.
     * @return this builder.
     * @see String#valueOf(char)
     */
    public native StringBuilder append(char c) /*-[
      JreStringBuilder_appendChar(&self->delegate_, c);
      return self;
    ]-*/;

    /**
     * Appends the string representation of the specified {@code int} value. The
     * {@code int} value is converted to a string according to the rule defined
     * by {@link String#valueOf(int)}.
     *
     * @param i
     *            the {@code int} value to append.
     * @return this builder.
     * @see String#valueOf(int)
     */
    public StringBuilder append(int i) {
        IntegralToString.appendInt(this, i);
        return this;
    }

    /**
     * Appends the string representation of the specified {@code long} value.
     * The {@code long} value is converted to a string according to the rule
     * defined by {@link String#valueOf(long)}.
     *
     * @param l
     *            the {@code long} value.
     * @return this builder.
     * @see String#valueOf(long)
     */
    public StringBuilder append(long l) {
        IntegralToString.appendLong(this, l);
        return this;
    }

    /**
     * Appends the string representation of the specified {@code float} value.
     * The {@code float} value is converted to a string according to the rule
     * defined by {@link String#valueOf(float)}.
     *
     * @param f
     *            the {@code float} value to append.
     * @return this builder.
     * @see String#valueOf(float)
     */
    public StringBuilder append(float f) {
        RealToString.appendFloat(this, f);
        return this;
    }

    /**
     * Appends the string representation of the specified {@code double} value.
     * The {@code double} value is converted to a string according to the rule
     * defined by {@link String#valueOf(double)}.
     *
     * @param d
     *            the {@code double} value to append.
     * @return this builder.
     * @see String#valueOf(double)
     */
    public StringBuilder append(double d) {
        RealToString.appendDouble(this, d);
        return this;
    }

    /**
     * Appends the string representation of the specified {@code Object}.
     * The {@code Object} value is converted to a string according to the rule
     * defined by {@link String#valueOf(Object)}.
     *
     * @param obj
     *            the {@code Object} to append.
     * @return this builder.
     * @see String#valueOf(Object)
     */
    public native StringBuilder append(Object obj) /*-[
      if (obj == nil) {
        JreStringBuilder_appendNull(&self->delegate_);
      } else {
        JreStringBuilder_appendString(&self->delegate_, [obj description]);
      }
      return self;
    ]-*/;

    /**
     * Appends the contents of the specified string. If the string is {@code
     * null}, then the string {@code "null"} is appended.
     *
     * @param str
     *            the string to append.
     * @return this builder.
     */
    public native StringBuilder append(String str) /*-[
      JreStringBuilder_appendString(&self->delegate_, str);
      return self;
    ]-*/;

    /**
     * Appends the contents of the specified {@code StringBuffer}. If the
     * StringBuffer is {@code null}, then the string {@code "null"} is
     * appended.
     *
     * @param sb
     *            the {@code StringBuffer} to append.
     * @return this builder.
     */
    public native StringBuilder append(StringBuffer sb) /*-[
      if (sb == nil) {
        JreStringBuilder_appendNull(&self->delegate_);
      } else {
        JreStringBuilder_appendBuffer(
            &self->delegate_, sb->delegate_.buffer_, sb->delegate_.count_);
      }
      return self;
    ]-*/;

    /**
     * Appends the string representation of the specified {@code char[]}.
     * The {@code char[]} is converted to a string according to the rule
     * defined by {@link String#valueOf(char[])}.
     *
     * @param chars
     *            the {@code char[]} to append..
     * @return this builder.
     * @see String#valueOf(char[])
     */
    public native StringBuilder append(char[] chars) /*-[
      JreStringBuilder_appendCharArray(&self->delegate_, chars);
      return self;
    ]-*/;

    /**
     * Appends the string representation of the specified subset of the {@code
     * char[]}. The {@code char[]} value is converted to a String according to
     * the rule defined by {@link String#valueOf(char[],int,int)}.
     *
     * @param str
     *            the {@code char[]} to append.
     * @param offset
     *            the inclusive offset index.
     * @param len
     *            the number of characters.
     * @return this builder.
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code offset} and {@code len} do not specify a valid
     *             subsequence.
     * @see String#valueOf(char[],int,int)
     */
    public native StringBuilder append(char[] str, int offset, int len) /*-[
      JreStringBuilder_appendCharArraySubset(&self->delegate_, str, offset, len);
      return self;
    ]-*/;

    /**
     * Appends the string representation of the specified {@code CharSequence}.
     * If the {@code CharSequence} is {@code null}, then the string {@code
     * "null"} is appended.
     *
     * @param csq
     *            the {@code CharSequence} to append.
     * @return this builder.
     */
    public native StringBuilder append(CharSequence csq) /*-[
      if (csq == nil) {
        JreStringBuilder_appendNull(&self->delegate_);
      } else {
        JreStringBuilder_appendCharSequence(&self->delegate_, csq, 0, [csq length]);
      }
      return self;
    ]-*/;

    /**
     * Appends the string representation of the specified subsequence of the
     * {@code CharSequence}. If the {@code CharSequence} is {@code null}, then
     * the string {@code "null"} is used to extract the subsequence from.
     *
     * @param csq
     *            the {@code CharSequence} to append.
     * @param start
     *            the beginning index.
     * @param end
     *            the ending index.
     * @return this builder.
     * @throws IndexOutOfBoundsException
     *             if {@code start} or {@code end} are negative, {@code start}
     *             is greater than {@code end} or {@code end} is greater than
     *             the length of {@code csq}.
     */
    public native StringBuilder append(CharSequence csq, int start, int end) /*-[
      JreStringBuilder_appendCharSequence(&self->delegate_, csq, start, end);
      return self;
    ]-*/;

    /**
     * Appends the encoded Unicode code point. The code point is converted to a
     * {@code char[]} as defined by {@link Character#toChars(int)}.
     *
     * @param codePoint
     *            the Unicode code point to encode and append.
     * @return this builder.
     * @see Character#toChars(int)
     */
    public native StringBuilder appendCodePoint(int codePoint) /*-[
      JreStringBuilder_appendCharArray(
          &self->delegate_, JavaLangCharacter_toCharsWithInt_(codePoint));
      return self;
    ]-*/;

    /**
     * Deletes a sequence of characters specified by {@code start} and {@code
     * end}. Shifts any remaining characters to the left.
     *
     * @param start
     *            the inclusive start index.
     * @param end
     *            the exclusive end index.
     * @return this builder.
     * @throws StringIndexOutOfBoundsException
     *             if {@code start} is less than zero, greater than the current
     *             length or greater than {@code end}.
     */
    public native StringBuilder delete(int start, int end) /*-[
      JreStringBuilder_delete(&self->delegate_, start, end);
      return self;
    ]-*/;

    /**
     * Deletes the character at the specified index. shifts any remaining
     * characters to the left.
     *
     * @param index
     *            the index of the character to delete.
     * @return this builder.
     * @throws StringIndexOutOfBoundsException
     *             if {@code index} is less than zero or is greater than or
     *             equal to the current length.
     */
    public native StringBuilder deleteCharAt(int index) /*-[
      JreStringBuilder_deleteCharAt(&self->delegate_, index);
      return self;
    ]-*/;

    /**
     * Inserts the string representation of the specified {@code boolean} value
     * at the specified {@code offset}. The {@code boolean} value is converted
     * to a string according to the rule defined by
     * {@link String#valueOf(boolean)}.
     *
     * @param offset
     *            the index to insert at.
     * @param b
     *            the {@code boolean} value to insert.
     * @return this builder.
     * @throws StringIndexOutOfBoundsException
     *             if {@code offset} is negative or greater than the current
     *             {@code length}.
     * @see String#valueOf(boolean)
     */
    public native StringBuilder insert(int offset, boolean b) /*-[
      JreStringBuilder_insertString(&self->delegate_, offset, b ? @"true" : @"false");
      return self;
    ]-*/;

    /**
     * Inserts the string representation of the specified {@code char} value at
     * the specified {@code offset}. The {@code char} value is converted to a
     * string according to the rule defined by {@link String#valueOf(char)}.
     *
     * @param offset
     *            the index to insert at.
     * @param c
     *            the {@code char} value to insert.
     * @return this builder.
     * @throws IndexOutOfBoundsException
     *             if {@code offset} is negative or greater than the current
     *             {@code length()}.
     * @see String#valueOf(char)
     */
    public native StringBuilder insert(int offset, char c) /*-[
      JreStringBuilder_insertChar(&self->delegate_, offset, c);
      return self;
    ]-*/;

    /**
     * Inserts the string representation of the specified {@code int} value at
     * the specified {@code offset}. The {@code int} value is converted to a
     * String according to the rule defined by {@link String#valueOf(int)}.
     *
     * @param offset
     *            the index to insert at.
     * @param i
     *            the {@code int} value to insert.
     * @return this builder.
     * @throws StringIndexOutOfBoundsException
     *             if {@code offset} is negative or greater than the current
     *             {@code length()}.
     * @see String#valueOf(int)
     */
    public native StringBuilder insert(int offset, int i) /*-[
      JreStringBuilder_insertString(&self->delegate_, offset, JavaLangInteger_toStringWithInt_(i));
      return self;
    ]-*/;

    /**
     * Inserts the string representation of the specified {@code long} value at
     * the specified {@code offset}. The {@code long} value is converted to a
     * String according to the rule defined by {@link String#valueOf(long)}.
     *
     * @param offset
     *            the index to insert at.
     * @param l
     *            the {@code long} value to insert.
     * @return this builder.
     * @throws StringIndexOutOfBoundsException
     *             if {@code offset} is negative or greater than the current
     *             {code length()}.
     * @see String#valueOf(long)
     */
    public native StringBuilder insert(int offset, long l) /*-[
      JreStringBuilder_insertString(&self->delegate_, offset, JavaLangLong_toStringWithLong_(l));
      return self;
    ]-*/;

    /**
     * Inserts the string representation of the specified {@code float} value at
     * the specified {@code offset}. The {@code float} value is converted to a
     * string according to the rule defined by {@link String#valueOf(float)}.
     *
     * @param offset
     *            the index to insert at.
     * @param f
     *            the {@code float} value to insert.
     * @return this builder.
     * @throws StringIndexOutOfBoundsException
     *             if {@code offset} is negative or greater than the current
     *             {@code length()}.
     * @see String#valueOf(float)
     */
    public native StringBuilder insert(int offset, float f) /*-[
      JreStringBuilder_insertString(&self->delegate_, offset, JavaLangFloat_toStringWithFloat_(f));
      return self;
    ]-*/;

    /**
     * Inserts the string representation of the specified {@code double} value
     * at the specified {@code offset}. The {@code double} value is converted
     * to a String according to the rule defined by
     * {@link String#valueOf(double)}.
     *
     * @param offset
     *            the index to insert at.
     * @param d
     *            the {@code double} value to insert.
     * @return this builder.
     * @throws StringIndexOutOfBoundsException
     *             if {@code offset} is negative or greater than the current
     *             {@code length()}.
     * @see String#valueOf(double)
     */
    public native StringBuilder insert(int offset, double d) /*-[
      JreStringBuilder_insertString(
          &self->delegate_, offset, JavaLangDouble_toStringWithDouble_(d));
      return self;
    ]-*/;

    /**
     * Inserts the string representation of the specified {@code Object} at the
     * specified {@code offset}. The {@code Object} value is converted to a
     * String according to the rule defined by {@link String#valueOf(Object)}.
     *
     * @param offset
     *            the index to insert at.
     * @param obj
     *            the {@code Object} to insert.
     * @return this builder.
     * @throws StringIndexOutOfBoundsException
     *             if {@code offset} is negative or greater than the current
     *             {@code length()}.
     * @see String#valueOf(Object)
     */
    public native StringBuilder insert(int offset, Object obj) /*-[
      JreStringBuilder_insertString(
          &self->delegate_, offset, obj == nil ? @"null" : [obj description]);
      return self;
    ]-*/;

    /**
     * Inserts the specified string at the specified {@code offset}. If the
     * specified string is null, then the String {@code "null"} is inserted.
     *
     * @param offset
     *            the index to insert at.
     * @param str
     *            the {@code String} to insert.
     * @return this builder.
     * @throws StringIndexOutOfBoundsException
     *             if {@code offset} is negative or greater than the current
     *             {@code length()}.
     */
    public native StringBuilder insert(int offset, String str) /*-[
      JreStringBuilder_insertString(&self->delegate_, offset, str);
      return self;
    ]-*/;

    /**
     * Inserts the string representation of the specified {@code char[]} at the
     * specified {@code offset}. The {@code char[]} value is converted to a
     * String according to the rule defined by {@link String#valueOf(char[])}.
     *
     * @param offset
     *            the index to insert at.
     * @param ch
     *            the {@code char[]} to insert.
     * @return this builder.
     * @throws StringIndexOutOfBoundsException
     *             if {@code offset} is negative or greater than the current
     *             {@code length()}.
     * @see String#valueOf(char[])
     */
    public native StringBuilder insert(int offset, char[] ch) /*-[
      JreStringBuilder_insertCharArray(&self->delegate_, offset, ch);
      return self;
    ]-*/;

    /**
     * Inserts the string representation of the specified subsequence of the
     * {@code char[]} at the specified {@code offset}. The {@code char[]} value
     * is converted to a String according to the rule defined by
     * {@link String#valueOf(char[],int,int)}.
     *
     * @param offset
     *            the index to insert at.
     * @param str
     *            the {@code char[]} to insert.
     * @param strOffset
     *            the inclusive index.
     * @param strLen
     *            the number of characters.
     * @return this builder.
     * @throws StringIndexOutOfBoundsException
     *             if {@code offset} is negative or greater than the current
     *             {@code length()}, or {@code strOffset} and {@code strLen} do
     *             not specify a valid subsequence.
     * @see String#valueOf(char[],int,int)
     */
    public native StringBuilder insert(int offset, char[] str, int strOffset,
            int strLen) /*-[
      JreStringBuilder_insertCharArraySubset(&self->delegate_, offset, str, strOffset, strLen);
      return self;
    ]-*/;

    /**
     * Inserts the string representation of the specified {@code CharSequence}
     * at the specified {@code offset}. The {@code CharSequence} is converted
     * to a String as defined by {@link CharSequence#toString()}. If {@code s}
     * is {@code null}, then the String {@code "null"} is inserted.
     *
     * @param offset
     *            the index to insert at.
     * @param s
     *            the {@code CharSequence} to insert.
     * @return this builder.
     * @throws IndexOutOfBoundsException
     *             if {@code offset} is negative or greater than the current
     *             {@code length()}.
     * @see CharSequence#toString()
     */
    public native StringBuilder insert(int offset, CharSequence s) /*-[
      JreStringBuilder_insertString(&self->delegate_, offset, s == nil ? @"null" : [s description]);
      return self;
    ]-*/;

    /**
     * Inserts the string representation of the specified subsequence of the
     * {@code CharSequence} at the specified {@code offset}. The {@code
     * CharSequence} is converted to a String as defined by
     * {@link CharSequence#subSequence(int, int)}. If the {@code CharSequence}
     * is {@code null}, then the string {@code "null"} is used to determine the
     * subsequence.
     *
     * @param offset
     *            the index to insert at.
     * @param s
     *            the {@code CharSequence} to insert.
     * @param start
     *            the start of the subsequence of the character sequence.
     * @param end
     *            the end of the subsequence of the character sequence.
     * @return this builder.
     * @throws IndexOutOfBoundsException
     *             if {@code offset} is negative or greater than the current
     *             {@code length()}, or {@code start} and {@code end} do not
     *             specify a valid subsequence.
     * @see CharSequence#subSequence(int, int)
     */
    public native StringBuilder insert(int offset, CharSequence s, int start, int end) /*-[
      JreStringBuilder_insertCharSequence(&self->delegate_, offset, s, start, end);
      return self;
    ]-*/;

    /**
     * Replaces the specified subsequence in this builder with the specified
     * string.
     *
     * @param start
     *            the inclusive begin index.
     * @param end
     *            the exclusive end index.
     * @param string
     *            the replacement string.
     * @return this builder.
     * @throws StringIndexOutOfBoundsException
     *             if {@code start} is negative, greater than the current
     *             {@code length()} or greater than {@code end}.
     * @throws NullPointerException
     *            if {@code str} is {@code null}.
     */
    public native StringBuilder replace(int start, int end, String string) /*-[
      JreStringBuilder_replace(&self->delegate_, start, end, string);
      return self;
    ]-*/;

    /**
     * Reverses the order of characters in this builder.
     *
     * @return this buffer.
     */
    public native StringBuilder reverse() /*-[
      JreStringBuilder_reverse(&self->delegate_);
      return self;
    ]-*/;

    /**
     * Returns the contents of this builder.
     *
     * @return the string representation of the data in this builder.
     */
    @Override
    public native String toString() /*-[
      return JreStringBuilder_toString(&self->delegate_);
    ]-*/;

    @Override
    public int length() {
        return super.length();
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return super.substring(start, end);
    }

    /**
     * Reads the state of a {@code StringBuilder} from the passed stream and
     * restores it to this instance.
     *
     * @param in
     *            the stream to read the state from.
     * @throws IOException
     *             if the stream throws it during the read.
     * @throws ClassNotFoundException
     *             if the stream throws it during the read.
     */
    private native void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException /*-[
      [((JavaIoObjectInputStream *) nil_chk(inArg)) defaultReadObject];
      jint count = [inArg readInt];
      IOSCharArray *value = (IOSCharArray *) cast_chk([inArg readObject], [IOSCharArray class]);
      [self setWithCharArray:value withInt:count];
    ]-*/;

    /**
     * Writes the state of this object to the stream passed.
     *
     * @param out
     *            the stream to write the state to.
     * @throws IOException
     *             if the stream throws it during the write.
     * @serialData {@code int} - the length of this object. {@code char[]} - the
     *             buffer from this object, which may be larger than the length
     *             field.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(length());
        out.writeObject(getValue());
    }
}
