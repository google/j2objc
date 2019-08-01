/*
 * Copyright (C) 2014 The Android Open Source Project
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

package libcore.util;

/**
 * Hexadecimal encoding where each byte is represented by two hexadecimal digits.
 * @hide
 */
@libcore.api.CorePlatformApi
public class HexEncoding {

    /** Hidden constructor to prevent instantiation. */
    private HexEncoding() {}

    private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

    /**
     * Encodes the provided data as a sequence of hexadecimal characters.
     */
    @libcore.api.CorePlatformApi
    public static char[] encode(byte[] data) {
        return encode(data, 0, data.length);
    }

    /**
     * Encodes the provided data as a sequence of hexadecimal characters.
     */
    @libcore.api.CorePlatformApi
    public static char[] encode(byte[] data, int offset, int len) {
        char[] result = new char[len * 2];
        for (int i = 0; i < len; i++) {
            byte b = data[offset + i];
            int resultIndex = 2 * i;
            result[resultIndex] = (HEX_DIGITS[(b >>> 4) & 0x0f]);
            result[resultIndex + 1] = (HEX_DIGITS[b & 0x0f]);
        }

        return result;
    }

    /**
     * Encodes the provided data as a sequence of hexadecimal characters.
     */
    @libcore.api.CorePlatformApi
    public static String encodeToString(byte[] data) {
        return new String(encode(data));
    }

    /**
     * Decodes the provided hexadecimal string into a byte array.  Odd-length inputs
     * are not allowed.
     *
     * Throws an {@code IllegalArgumentException} if the input is malformed.
     */
    @libcore.api.CorePlatformApi
    public static byte[] decode(String encoded) throws IllegalArgumentException {
        return decode(encoded.toCharArray());
    }

    /**
     * Decodes the provided hexadecimal string into a byte array. If {@code allowSingleChar}
     * is {@code true} odd-length inputs are allowed and the first character is interpreted
     * as the lower bits of the first result byte.
     *
     * Throws an {@code IllegalArgumentException} if the input is malformed.
     */
    public static byte[] decode(String encoded, boolean allowSingleChar) throws IllegalArgumentException {
        return decode(encoded.toCharArray(), allowSingleChar);
    }

    /**
     * Decodes the provided hexadecimal string into a byte array.  Odd-length inputs
     * are not allowed.
     *
     * Throws an {@code IllegalArgumentException} if the input is malformed.
     */
    @libcore.api.CorePlatformApi
    public static byte[] decode(char[] encoded) throws IllegalArgumentException {
        return decode(encoded, false);
    }

    /**
     * Decodes the provided hexadecimal string into a byte array. If {@code allowSingleChar}
     * is {@code true} odd-length inputs are allowed and the first character is interpreted
     * as the lower bits of the first result byte.
     *
     * Throws an {@code IllegalArgumentException} if the input is malformed.
     */
    @libcore.api.CorePlatformApi
    public static byte[] decode(char[] encoded, boolean allowSingleChar) throws IllegalArgumentException {
        int resultLengthBytes = (encoded.length + 1) / 2;
        byte[] result = new byte[resultLengthBytes];

        int resultOffset = 0;
        int i = 0;
        if (allowSingleChar) {
            if ((encoded.length % 2) != 0) {
                // Odd number of digits -- the first digit is the lower 4 bits of the first result byte.
                result[resultOffset++] = (byte) toDigit(encoded, i);
                i++;
            }
        } else {
            if ((encoded.length % 2) != 0) {
                throw new IllegalArgumentException("Invalid input length: " + encoded.length);
            }
        }

        for (int len = encoded.length; i < len; i += 2) {
            result[resultOffset++] = (byte) ((toDigit(encoded, i) << 4) | toDigit(encoded, i + 1));
        }

        return result;
    }

    private static int toDigit(char[] str, int offset) throws IllegalArgumentException {
        // NOTE: that this isn't really a code point in the traditional sense, since we're
        // just rejecting surrogate pairs outright.
        int pseudoCodePoint = str[offset];

        if ('0' <= pseudoCodePoint && pseudoCodePoint <= '9') {
            return pseudoCodePoint - '0';
        } else if ('a' <= pseudoCodePoint && pseudoCodePoint <= 'f') {
            return 10 + (pseudoCodePoint - 'a');
        } else if ('A' <= pseudoCodePoint && pseudoCodePoint <= 'F') {
            return 10 + (pseudoCodePoint - 'A');
        }

        throw new IllegalArgumentException("Illegal char: " + str[offset] +
                " at offset " + offset);
    }
}
