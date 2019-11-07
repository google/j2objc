/*
 * Buffer.java
 *
 * Copyright (c) 2013 Mike Strobel
 *
 * This source code is based on Mono.Cecil from Jb Evain, Copyright (c) Jb Evain;
 * and ILSpy/ICSharpCode from SharpDevelop, Copyright (c) AlphaSierraPapa.
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.assembler.metadata;

import com.strobel.core.VerifyArgument;
import com.strobel.util.EmptyArrayCache;

import java.nio.BufferUnderflowException;
import java.util.Arrays;

/**
 * @author Mike Strobel
 */
public class Buffer {
    private final static int DEFAULT_SIZE = 64;

    private byte[] _data;
    private int _length;
    private int _position;

    public Buffer() {
        _data = new byte[DEFAULT_SIZE];
        _length = DEFAULT_SIZE;
    }

    public Buffer(final byte[] data) {
        _data = VerifyArgument.notNull(data, "data");
        _length = data.length;
    }

    public Buffer(final int initialSize) {
        _data = new byte[initialSize];
        _length = initialSize;
    }

    public int size() {
        return _length;
    }

    public void flip() {
        _length = _position;
        _position = 0;
    }

    public int position() {
        return _position;
    }

    public void position(final int position) {
        if (position > _length) {
            throw new BufferUnderflowException();
        }
        _position = position;
    }

    public void advance(final int length) {
        if (_position + length > _length) {
            _position = _length;
            throw new BufferUnderflowException();
        }
        _position += length;
    }

    public void reset() {
        reset(DEFAULT_SIZE);
    }

    public void reset(final int initialSize) {
        if (VerifyArgument.isNonNegative(initialSize, "initialSize") == 0) {
            _data = EmptyArrayCache.EMPTY_BYTE_ARRAY;
        }
        else if (initialSize > _data.length || initialSize < _data.length / 4) {
            _data = new byte[initialSize];
        }
        _length = initialSize;
        _position = 0;
    }

    public byte[] array() {
        return _data;
    }

    public int read(final byte[] buffer, final int offset, final int length) {
        if (buffer == null) {
            throw new NullPointerException();
        }

        if (offset < 0 || length < 0 || length > buffer.length - offset) {
            throw new IndexOutOfBoundsException();
        }

        if (_position >= _length) {
            return -1;
        }

        final int available = _length - _position;
        final int actualLength = Math.min(length, available);

        if (actualLength <= 0) {
            return 0;
        }

        System.arraycopy(_data, _position, buffer, offset, actualLength);

        _position += actualLength;

        return actualLength;
    }

    public String readUtf8() {
        final int utfLength = readUnsignedShort();
        final byte[] byteBuffer = new byte[utfLength];
        final char[] charBuffer = new char[utfLength];

        int ch, ch2, ch3;
        int count = 0;
        int charactersRead = 0;

        read(byteBuffer, 0, utfLength);

        while (count < utfLength) {
            ch = (int) byteBuffer[count] & 0xFF;
            if (ch > 127) {
                break;
            }
            count++;
            charBuffer[charactersRead++] = (char) ch;
        }

        while (count < utfLength) {
            ch = (int) byteBuffer[count] & 0xff;

            switch (ch & 0xE0) {
                case 0x00:
                case 0x10:
                case 0x20:
                case 0x30:
                case 0x40:
                case 0x50:
                case 0x60:
                case 0x70:
                    /* 0xxxxxxx*/
                    count++;
                    charBuffer[charactersRead++] = (char) ch;
                    break;

                case 0xC0:
                    /* 110x xxxx   10xx xxxx*/
                    count += 2;

                    if (count > utfLength) {
                        throw new IllegalStateException("malformed input: partial character at end");
                    }

                    ch2 = (int) byteBuffer[count - 1];

                    if ((ch2 & 0xC0) != 0x80) {
                        throw new IllegalStateException("malformed input around byte " + count);
                    }

                    charBuffer[charactersRead++] = (char) ((ch & 0x1F) << 6 | ch2 & 0x3F);
                    break;

                case 0xE0:
                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
                    count += 3;

                    if (count > utfLength) {
                        throw new IllegalStateException("malformed input: partial character at end");
                    }

                    ch2 = (int) byteBuffer[count - 2];
                    ch3 = (int) byteBuffer[count - 1];

                    if ((ch2 & 0xC0) != 0x80 || (ch3 & 0xC0) != 0x80) {
                        throw new IllegalStateException("malformed input around byte " + (count - 1));
                    }

                    charBuffer[charactersRead++] = (char) ((ch & 0x0F) << 12 |
                                                           (ch2 & 0x3F) << 6 |
                                                           (ch3 & 0x3F) << 0);
                    break;

                default:
                    /* 10xx xxxx,  1111 xxxx */
                    throw new IllegalStateException("malformed input around byte " + count);
            }
        }

        // The number of chars produced may be less than utfLength
        return new String(charBuffer, 0, charactersRead);
    }

    public byte readByte() {
        verifyReadableBytes(1);
        return _data[_position++];
    }

    public int readUnsignedByte() {
        verifyReadableBytes(1);
        return _data[_position++] & 0xFF;
    }

    public short readShort() {
        verifyReadableBytes(2);
        return (short) ((readUnsignedByte() << 8) +
                        (readUnsignedByte() << 0));
    }

    public int readUnsignedShort() {
        verifyReadableBytes(2);
        return ((readUnsignedByte() << 8) +
                (readUnsignedByte() << 0));
    }

    public int readInt() {
        verifyReadableBytes(4);
        return (readUnsignedByte() << 24) +
               (readUnsignedByte() << 16) +
               (readUnsignedByte() << 8) +
               (readUnsignedByte() << 0);
    }

    public long readLong() {
        verifyReadableBytes(8);
        return ((long)readUnsignedByte() << 56) +
               ((long)readUnsignedByte() << 48) +
               ((long)readUnsignedByte() << 40) +
               ((long)readUnsignedByte() << 32) +
               ((long)readUnsignedByte() << 24) +
               ((long)readUnsignedByte() << 16) +
               ((long)readUnsignedByte() << 8) +
               ((long)readUnsignedByte() << 0);
    }

    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    public Buffer writeByte(final int b) {
        ensureWriteableBytes(1);

        _data[_position++] = (byte) (b & 0xFF);

        return this;
    }

    public Buffer writeShort(final int s) {
        ensureWriteableBytes(2);

        _data[_position++] = (byte) ((s >>> 8) & 0xFF);
        _data[_position++] = (byte) (s & 0xFF);

        return this;
    }

    public Buffer writeInt(final int i) {
        ensureWriteableBytes(4);

        _data[_position++] = (byte) ((i >>> 24) & 0xFF);
        _data[_position++] = (byte) ((i >>> 16) & 0xFF);
        _data[_position++] = (byte) ((i >>> 8) & 0xFF);
        _data[_position++] = (byte) (i & 0xFF);

        return this;
    }

    public Buffer writeLong(final long l) {
        ensureWriteableBytes(8);

        int i = (int) (l >>> 32);

        _data[_position++] = (byte) ((i >>> 24) & 0xFF);
        _data[_position++] = (byte) ((i >>> 16) & 0xFF);
        _data[_position++] = (byte) ((i >>> 8) & 0xFF);
        _data[_position++] = (byte) (i & 0xFF);

        i = (int) l;

        _data[_position++] = (byte) ((i >>> 24) & 0xFF);
        _data[_position++] = (byte) ((i >>> 16) & 0xFF);
        _data[_position++] = (byte) ((i >>> 8) & 0xFF);
        _data[_position++] = (byte) (i & 0xFF);

        return this;
    }

    public Buffer writeFloat(final float f) {
        return writeInt(Float.floatToRawIntBits(f));
    }

    public Buffer writeDouble(final double d) {
        return writeLong(Double.doubleToRawLongBits(d));
    }

    @SuppressWarnings("ConstantConditions")
    public Buffer writeUtf8(final String s) {
        final int charLength = s.length();

        ensureWriteableBytes(2 + charLength);

        // optimistic algorithm: instead of computing the byte length and then
        // serializing the string (which requires two loops), we assume the byte
        // length is equal to char length (which is the most frequent case), and
        // we start serializing the string right away. During the serialization,
        // if we find that this assumption is wrong, we continue with the
        // general method.
        _data[_position++] = (byte) (charLength >>> 8);
        _data[_position++] = (byte) charLength;

        for (int i = 0; i < charLength; ++i) {
            char c = s.charAt(i);
            if (c >= '\001' && c <= '\177') {
                _data[_position++] = (byte) c;
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

                _data[_position] = (byte) (byteLength >>> 8);
                _data[_position + 1] = (byte) byteLength;

                ensureWriteableBytes(2 + byteLength);

                for (int j = i; j < charLength; ++j) {
                    c = s.charAt(j);
                    if (c >= '\001' && c <= '\177') {
                        _data[_position++] = (byte) c;
                    }
                    else if (c > '\u07FF') {
                        _data[_position++] = (byte) (0xE0 | c >> 12 & 0xF);
                        _data[_position++] = (byte) (0x80 | c >> 6 & 0x3F);
                        _data[_position++] = (byte) (0x80 | c & 0x3F);
                    }
                    else {
                        _data[_position++] = (byte) (0xC0 | c >> 6 & 0x1F);
                        _data[_position++] = (byte) (0x80 | c & 0x3F);
                    }
                }
                break;
            }
        }

        return this;
    }

    public Buffer putByteArray(final byte[] b, final int offset, final int length) {
        ensureWriteableBytes(length);
        if (b != null) {
            System.arraycopy(b, offset, _data, _position, length);
        }
        _position += length;
        return this;
    }

    protected void verifyReadableBytes(final int size) {
        if (VerifyArgument.isNonNegative(size, "size") > 0 && _position + size > _length) {
            throw new BufferUnderflowException();
        }
    }

    protected void ensureWriteableBytes(final int size) {
        final int minLength = _position + size;

        if (minLength > _data.length) {
            final int length1 = 2 * _data.length;
            final int length2 = _position + size;

            _data = Arrays.copyOf(_data, Math.max(length1, length2));
        }

        _length = Math.max(minLength, _length);
    }
}
