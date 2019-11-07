/*
 * CodeStream.java
 *
 * Copyright (c) 2012 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.reflection.emit;

import java.util.Arrays;

/**
 * @author strobelm
 */
public final class CodeStream {

    private final static int DEFAULT_SIZE = 64;

    /**
     * The content of this stream.
     */
    private byte[] _data;
    /**
     * Actual number of bytes in this stream.
     */
    private int _length;

    /**
     * Constructs a new {@link CodeStream} with a default initial
     * size.
     */
    public CodeStream() {
        _data = new byte[DEFAULT_SIZE];
    }

    /**
     * Constructs a new {@link CodeStream} with the given initial
     * size.
     * @param initialSize the initial size of the byte stream to be constructed.
     */
    public CodeStream(final int initialSize) {
        _data = new byte[initialSize];
    }

    public void reset() {
        reset(DEFAULT_SIZE);
    }

    public void reset(final int initialSize) {
        _data = new byte[DEFAULT_SIZE];
        _length = 0;
    }

    public byte[] getData() {
        return _data;
    }

    public int getLength() {
        return _length;
    }

    /**
     * Puts a byte into this byte stream. The byte stream is automatically
     * enlarged if necessary.
     * @param b a byte.
     * @return this byte stream.
     */
    public CodeStream putByte(final int b) {
        ensureCapacity(1);

        _data[_length++] = (byte)(b & 0xFF);

        return this;
    }

    /**
     * Puts two bytes into this byte stream. The byte stream is automatically
     * enlarged if necessary.
     * @param b1 a byte.
     * @param b2 another byte.
     * @return this byte stream.
     */
    CodeStream put11(final int b1, final int b2) {
        ensureCapacity(2);

        _data[_length++] = (byte)(b1 & 0xFF);
        _data[_length++] = (byte)(b2 & 0xFF);

        return this;
    }

    /**
     * Puts a short into this byte stream. The byte stream is automatically
     * enlarged if necessary.
     * @param s a short.
     * @return this byte stream.
     */
    public CodeStream putShort(final int s) {
        ensureCapacity(2);

        _data[_length++] = (byte)((s >>> 8) & 0xFF);
        _data[_length++] = (byte)(s & 0xFF);

        return this;
    }

    /**
     * Puts a byte and a short into this byte stream. The byte stream is
     * automatically enlarged if necessary.
     * @param b a byte.
     * @param s a short.
     * @return this byte stream.
     */
    CodeStream put12(final int b, final int s) {
        ensureCapacity(3);

        _data[_length++] = (byte)(b & 0xFF);
        _data[_length++] = (byte)((s >>> 8) & 0xFF);
        _data[_length++] = (byte)(s & 0xFF);

        return this;
    }

    /**
     * Puts an int into this byte stream. The byte stream is automatically
     * enlarged if necessary.
     * @param i an int.
     * @return this byte stream.
     */
    public CodeStream putInt(final int i) {
        ensureCapacity(4);

        _data[_length++] = (byte)((i >>> 24) & 0xFF);
        _data[_length++] = (byte)((i >>> 16) & 0xFF);
        _data[_length++] = (byte)((i >>> 8) & 0xFF);
        _data[_length++] = (byte)(i & 0xFF);

        return this;
    }

    /**
     * Puts a long into this byte stream. The byte stream is automatically
     * enlarged if necessary.
     * @param l a long.
     * @return this byte stream.
     */
    public CodeStream putLong(final long l) {
        ensureCapacity(8);

        int i = (int)(l >>> 32);

        _data[_length++] = (byte)((i >>> 24) & 0xFF);
        _data[_length++] = (byte)((i >>> 16) & 0xFF);
        _data[_length++] = (byte)((i >>> 8) & 0xFF);
        _data[_length++] = (byte)(i & 0xFF);

        i = (int)l;

        _data[_length++] = (byte)((i >>> 24) & 0xFF);
        _data[_length++] = (byte)((i >>> 16) & 0xFF);
        _data[_length++] = (byte)((i >>> 8) & 0xFF);
        _data[_length++] = (byte)(i & 0xFF);

        return this;
    }

    /**
     * Puts a float into this byte stream. The byte stream is automatically
     * enlarged if necessary.
     * @param f a float.
     * @return this byte stream.
     */
    public CodeStream putFloat(final float f) {
        return putInt(Float.floatToRawIntBits(f));
    }

    /**
     * Puts a double into this byte stream. The byte stream is automatically
     * enlarged if necessary.
     * @param d a double.
     * @return this byte stream.
     */
    public CodeStream putDouble(final double d) {
        return putLong(Double.doubleToRawLongBits(d));
    }

    /**
     * Puts an UTF8 string into this byte stream. The byte stream is
     * automatically enlarged if necessary.
     * @param s a String.
     * @return this byte stream.
     */
    @SuppressWarnings("ConstantConditions")
    public CodeStream putUtf8(final String s) {
        final int charLength = s.length();

        ensureCapacity(2 + charLength);

        final int originalLength = _length;

        // optimistic algorithm: instead of computing the byte length and then
        // serializing the string (which requires two loops), we assume the byte
        // length is equal to char length (which is the most frequent case), and
        // we start serializing the string right away. During the serialization,
        // if we find that this assumption is wrong, we continue with the
        // general method.
        _data[_length++] = (byte)(charLength >>> 8);
        _data[_length++] = (byte)charLength;

        for (int i = 0; i < charLength; ++i) {
            char c = s.charAt(i);
            if (c >= '\001' && c <= '\177') {
                _data[_length++] = (byte)c;
            }
            else {
                int byteLength = i;
                for (int j = i; j < charLength; ++j) {
                    c = s.charAt(j);
                    if (c >= '\001' && c <= '\177') {
                        byteLength++;
                    }
                    else if (c > '\u07FF') {
                        byteLength += 3;
                    }
                    else {
                        byteLength += 2;
                    }
                }

                _data[originalLength] = (byte)(byteLength >>> 8);
                _data[originalLength + 1] = (byte)byteLength;

                ensureCapacity(2 + byteLength);

                for (int j = i; j < charLength; ++j) {
                    c = s.charAt(j);
                    if (c >= '\001' && c <= '\177') {
                        _data[_length++] = (byte)c;
                    }
                    else if (c > '\u07FF') {
                        _data[_length++] = (byte)(0xE0 | c >> 12 & 0xF);
                        _data[_length++] = (byte)(0x80 | c >> 6 & 0x3F);
                        _data[_length++] = (byte)(0x80 | c & 0x3F);
                    }
                    else {
                        _data[_length++] = (byte)(0xC0 | c >> 6 & 0x1F);
                        _data[_length++] = (byte)(0x80 | c & 0x3F);
                    }
                }
                break;
            }
        }

        return this;
    }

    /**
     * Puts an array of bytes into this byte stream. The byte stream is
     * automatically enlarged if necessary.
     * @param b      an array of bytes. May be <tt>null</tt> to put <tt>length</tt>
     *               null bytes into this byte stream.
     * @param offset index of the fist byte of b that must be copied.
     * @param length number of bytes of b that must be copied.
     * @return this byte stream.
     */
    public CodeStream putByteArray(final byte[] b, final int offset, final int length) {
        ensureCapacity(length);
        if (b != null) {
            System.arraycopy(b, offset, _data, _length, length);
        }
        _length += length;
        return this;
    }

    /**
     * Enlarge this byte stream so that it can receive n more bytes.
     * @param size number of additional bytes that this byte stream should be
     *             able to receive.
     */
    void ensureCapacity(final int size) {
        if (_length + size <= _data.length) {
            return;
        }

        final int length1 = 2 * _data.length;
        final int length2 = _length + size;

        _data = Arrays.copyOf(_data, length1 > length2 ? length1 : length2);
    }
}
