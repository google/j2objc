/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.nio.charset;

/**
 * Provides convenient access to the most important built-in charsets. Saves a hash lookup and
 * unnecessary handling of UnsupportedEncodingException at call sites, compared to using the
 * charset's name.
 *
 * Also various special-case charset conversions (for performance).
 *
 * @hide internal use only
 */
public final class Charsets {
    /**
     * A cheap and type-safe constant for the ISO-8859-1 Charset.
     */
    public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");

    /**
     * A cheap and type-safe constant for the US-ASCII Charset.
     */
    public static final Charset US_ASCII = Charset.forName("US-ASCII");

    /**
     * A cheap and type-safe constant for the UTF-8 Charset.
     */
    public static final Charset UTF_8 = Charset.forName("UTF-8");

    /**
     * Returns a new byte array containing the bytes corresponding to the given characters,
     * encoded in US-ASCII. Unrepresentable characters are replaced by (byte) '?'.
     */
    public static native byte[] toAsciiBytes(char[] chars, int offset, int length);

    /**
     * Returns a new byte array containing the bytes corresponding to the given characters,
     * encoded in ISO-8859-1. Unrepresentable characters are replaced by (byte) '?'.
     */
    public static native byte[] toIsoLatin1Bytes(char[] chars, int offset, int length);

    /**
     * Returns a new byte array containing the bytes corresponding to the given characters,
     * encoded in UTF-8. All characters are representable in UTF-8.
     */
    public static native byte[] toUtf8Bytes(char[] chars, int offset, int length);

    /**
     * Returns a new byte array containing the bytes corresponding to the given characters,
     * encoded in UTF-16BE. All characters are representable in UTF-16BE.
     */
    public static byte[] toBigEndianUtf16Bytes(char[] chars, int offset, int length) {
        byte[] result = new byte[length * 2];
        int end = offset + length;
        int resultIndex = 0;
        for (int i = offset; i < end; ++i) {
            char ch = chars[i];
            result[resultIndex++] = (byte) (ch >> 8);
            result[resultIndex++] = (byte) ch;
        }
        return result;
    }

    /**
     * Decodes the given US-ASCII bytes into the given char[]. Equivalent to but faster than:
     *
     * for (int i = 0; i < count; ++i) {
     *     char ch = (char) (data[start++] & 0xff);
     *     value[i] = (ch <= 0x7f) ? ch : REPLACEMENT_CHAR;
     * }
     */
    public static native void asciiBytesToChars(byte[] bytes, int offset, int length, char[] chars);

    /**
     * Decodes the given ISO-8859-1 bytes into the given char[]. Equivalent to but faster than:
     *
     * for (int i = 0; i < count; ++i) {
     *     value[i] = (char) (data[start++] & 0xff);
     * }
     */
    public static native void isoLatin1BytesToChars(byte[] bytes, int offset, int length, char[] chars);

    private Charsets() {
    }
}
