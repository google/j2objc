/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.lang;

/*-[
#include "java/lang/Character.h"
]-*/

/**
 * A mutable sequence of characters.  This class provides an API compatible
 * with {@code StringBuffer}, but with no guarantee of synchronization.
 * This class is designed for use as a drop-in replacement for
 * {@code StringBuffer} in places where the string buffer was being
 * used by a single thread (as is generally the case).   Where possible,
 * it is recommended that this class be used in preference to
 * {@code StringBuffer} as it will be faster under most implementations.
 *
 * <p>The principal operations on a {@code StringBuilder} are the
 * {@code append} and {@code insert} methods, which are
 * overloaded so as to accept data of any type. Each effectively
 * converts a given datum to a string and then appends or inserts the
 * characters of that string to the string builder. The
 * {@code append} method always adds these characters at the end
 * of the builder; the {@code insert} method adds the characters at
 * a specified point.
 * <p>
 * For example, if {@code z} refers to a string builder object
 * whose current contents are "{@code start}", then
 * the method call {@code z.append("le")} would cause the string
 * builder to contain "{@code startle}", whereas
 * {@code z.insert(4, "le")} would alter the string builder to
 * contain "{@code starlet}".
 * <p>
 * In general, if sb refers to an instance of a {@code StringBuilder},
 * then {@code sb.append(x)} has the same effect as
 * {@code sb.insert(sb.length(), x)}.
 * <p>
 * Every string builder has a capacity. As long as the length of the
 * character sequence contained in the string builder does not exceed
 * the capacity, it is not necessary to allocate a new internal
 * buffer. If the internal buffer overflows, it is automatically made larger.
 *
 * <p>Instances of {@code StringBuilder} are not safe for
 * use by multiple threads. If such synchronization is required then it is
 * recommended that {@link java.lang.StringBuffer} be used.
 *
 * <p>Unless otherwise noted, passing a {@code null} argument to a constructor
 * or method in this class will cause a {@link NullPointerException} to be
 * thrown.
 *
 * @author      Michael McCloskey
 * @see         java.lang.StringBuffer
 * @see         java.lang.String
 * @since       1.5
 */
public final class StringBuilder
    extends AbstractStringBuilder
    implements java.io.Serializable, CharSequence
{

    /** use serialVersionUID for interoperability */
    static final long serialVersionUID = 4383685877147921099L;

    /**
     * Constructs a string builder with no characters in it and an
     * initial capacity of 16 characters.
     */
    public StringBuilder() {
    }

    /**
     * Constructs a string builder with no characters in it and an
     * initial capacity specified by the {@code capacity} argument.
     *
     * @param      capacity  the initial capacity.
     * @throws     NegativeArraySizeException  if the {@code capacity}
     *               argument is less than {@code 0}.
     */
    public StringBuilder(int capacity) {
        super(capacity);
    }

    /**
     * Constructs a string builder initialized to the contents of the
     * specified string. The initial capacity of the string builder is
     * {@code 16} plus the length of the string argument.
     *
     * @param   str   the initial contents of the buffer.
     */
    public StringBuilder(String str) {
        super(str.length() + 16);
        append(str);
    }

    /**
     * Constructs a string builder that contains the same characters
     * as the specified {@code CharSequence}. The initial capacity of
     * the string builder is {@code 16} plus the length of the
     * {@code CharSequence} argument.
     *
     * @param      seq   the sequence to copy.
     */
    public StringBuilder(CharSequence seq) {
        this(seq.length() + 16);
        append(seq);
    }

    public StringBuilder append(Object obj) {
        return append(String.valueOf(obj));
    }

    public native StringBuilder append(String str) /*-[
      JreStringBuilder_appendString(&self->delegate_, str);
      return self;
    ]-*/;

    /**
     * Appends the specified {@code StringBuffer} to this sequence.
     * <p>
     * The characters of the {@code StringBuffer} argument are appended,
     * in order, to this sequence, increasing the
     * length of this sequence by the length of the argument.
     * If {@code sb} is {@code null}, then the four characters
     * {@code "null"} are appended to this sequence.
     * <p>
     * Let <i>n</i> be the length of this character sequence just prior to
     * execution of the {@code append} method. Then the character at index
     * <i>k</i> in the new character sequence is equal to the character at
     * index <i>k</i> in the old character sequence, if <i>k</i> is less than
     * <i>n</i>; otherwise, it is equal to the character at index <i>k-n</i>
     * in the argument {@code sb}.
     *
     * @param   sb   the {@code StringBuffer} to append.
     * @return  a reference to this object.
     */
    public native StringBuilder append(StringBuffer sb) /*-[
      JreStringBuilder_appendStringBuffer(&self->delegate_, sb);
      return self;
    ]-*/;

    @Override
    public native StringBuilder append(CharSequence s) /*-[
      JreStringBuilder_appendCharSequence(&self->delegate_, s);
      return self;
    ]-*/;

    /**
     * @throws     IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public native StringBuilder append(CharSequence s, int start, int end) /*-[
      JreStringBuilder_appendCharSequenceSubset(&self->delegate_, s, start, end);
      return self;
    ]-*/;

    public native StringBuilder append(char[] str) /*-[
      JreStringBuilder_appendCharArray(&self->delegate_, str);
      return self;
    ]-*/;

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public native StringBuilder append(char[] str, int offset, int len) /*-[
      JreStringBuilder_appendCharArraySubset(&self->delegate_, str, offset, len);
      return self;
    ]-*/;

    public StringBuilder append(boolean b) {
        return append(Boolean.toString(b));
    }

    @Override
    public native StringBuilder append(char c) /*-[
      JreStringBuilder_appendChar(&self->delegate_, c);
      return self;
    ]-*/;

    public native StringBuilder append(int i) /*-[
      JreStringBuilder_appendInt(&self->delegate_, i);
      return self;
    ]-*/;

    public native StringBuilder append(long lng) /*-[
      JreStringBuilder_appendLong(&self->delegate_, lng);
      return self;
    ]-*/;

    public native StringBuilder append(float f) /*-[
      JreStringBuilder_appendFloat(&self->delegate_, f);
      return self;
    ]-*/;

    public native StringBuilder append(double d) /*-[
      JreStringBuilder_appendDouble(&self->delegate_, d);
      return self;
    ]-*/;

    /**
     * @since 1.5
     */
    public native StringBuilder appendCodePoint(int codePoint) /*-[
      JreStringBuilder_appendCharArray(
          &self->delegate_, JavaLangCharacter_toCharsWithInt_(codePoint));
      return self;
    ]-*/;

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    public native StringBuilder delete(int start, int end) /*-[
      JreStringBuilder_delete(&self->delegate_, start, end);
      return self;
    ]-*/;

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    public native StringBuilder deleteCharAt(int index) /*-[
      JreStringBuilder_deleteCharAt(&self->delegate_, index);
      return self;
    ]-*/;

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    public native StringBuilder replace(int start, int end, String str) /*-[
      JreStringBuilder_replace(&self->delegate_, start, end, str);
      return self;
    ]-*/;

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    public native StringBuilder insert(int index, char[] str, int offset, int len) /*-[
      JreStringBuilder_insertCharArraySubset(&self->delegate_, index, str, offset, len);
      return self;
    ]-*/;

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    public StringBuilder insert(int offset, Object obj) {
        return insert(offset, String.valueOf(obj));
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    public native StringBuilder insert(int offset, String str) /*-[
      JreStringBuilder_insertString(&self->delegate_, offset, str);
      return self;
    ]-*/;

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    public native StringBuilder insert(int offset, char[] str) /*-[
      JreStringBuilder_insertCharArray(&self->delegate_, offset, str);
      return self;
    ]-*/;

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public StringBuilder insert(int dstOffset, CharSequence s) {
        return insert(dstOffset, String.valueOf(s));
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public native StringBuilder insert(int dstOffset, CharSequence s, int start, int end) /*-[
      JreStringBuilder_insertCharSequence(&self->delegate_, dstOffset, s, start, end);
      return self;
    ]-*/;

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    public StringBuilder insert(int offset, boolean b) {
        return insert(offset, Boolean.toString(b));
    }

    /**
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public native StringBuilder insert(int offset, char c) /*-[
      JreStringBuilder_insertChar(&self->delegate_, offset, c);
      return self;
    ]-*/;

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    public StringBuilder insert(int offset, int i) {
        return insert(offset, Integer.toString(i));
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    public StringBuilder insert(int offset, long l) {
        return insert(offset, Long.toString(l));
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    public StringBuilder insert(int offset, float f) {
        return insert(offset, Float.toString(f));
    }

    /**
     * @throws StringIndexOutOfBoundsException {@inheritDoc}
     */
    public StringBuilder insert(int offset, double d) {
        return insert(offset, Double.toString(d));
    }

    public native StringBuilder reverse() /*-[
      JreStringBuilder_reverse(&self->delegate_);
      return self;
    ]-*/;

    @Override
    public native String toString() /*-[
      return JreStringBuilder_toString(&self->delegate_);
    ]-*/;

    /**
     * Save the state of the {@code StringBuilder} instance to a stream
     * (that is, serialize it).
     *
     * @serialData the number of characters currently stored in the string
     *             builder ({@code int}), followed by the characters in the
     *             string builder ({@code char[]}).   The length of the
     *             {@code char} array may be greater than the number of
     *             characters currently stored in the string builder, in which
     *             case extra characters are ignored.
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        s.defaultWriteObject();
        s.writeInt(length());
        s.writeObject(getValue());
    }

    /**
     * readObject is called to restore the state of the StringBuffer from
     * a stream.
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        int count = s.readInt();
        char[] value = (char[]) s.readObject();
        append(value, 0, count);
    }

}
