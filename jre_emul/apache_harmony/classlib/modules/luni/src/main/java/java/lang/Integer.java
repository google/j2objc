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

/**
 * The wrapper for the primitive type {@code int}.
 * <p>
 * As with the specification, this implementation relies on code laid out in <a
 * href="http://www.hackersdelight.org/">Henry S. Warren, Jr.'s Hacker's
 * Delight, (Addison Wesley, 2002)</a> as well as <a
 * href="http://aggregate.org/MAGIC/">The Aggregate's Magic Algorithms</a>.
 *
 * @see java.lang.Number
 * @since 1.1
 */
public final class Integer extends Number implements Comparable<Integer> {

    /**
     * The value which the receiver represents.
     */
    private final int value;

    /**
     * Constant for the maximum {@code int} value, 2<sup>31</sup>-1.
     */
    public static final int MAX_VALUE = 0x7FFFFFFF;

    /**
     * Constant for the minimum {@code int} value, -2<sup>31</sup>.
     */
    public static final int MIN_VALUE = 0x80000000;

    /**
     * Constant for the number of bits needed to represent an {@code int} in
     * two's complement form.
     *
     * @since 1.5
     */
    public static final int SIZE = 32;
    
    /**
     * The {@link Class} object that represents the primitive type {@code int}.
     */
    @SuppressWarnings("unchecked")
    public static final Class<Integer> TYPE = (Class<Integer>) new int[0]
            .getClass().getComponentType();

    // Note: This can't be set to "int.class", since *that* is
    // defined to be "java.lang.Integer.TYPE";

    /**
     * Constructs a new {@code Integer} with the specified primitive integer
     * value.
     * 
     * @param value
     *            the primitive integer value to store in the new instance.
     */
    public Integer(int value) {
        this.value = value;
    }

    /**
     * Constructs a new {@code Integer} from the specified string.
     * 
     * @param string
     *            the string representation of an integer value.
     * @throws NumberFormatException
     *             if {@code string} can not be decoded into an integer value.
     * @see #parseInt(String)
     */
    public Integer(String string) throws NumberFormatException {
        this(parseInt(string));
    }

    @Override
    public byte byteValue() {
        return (byte) value;
    }

    /**
     * Compares this object to the specified integer object to determine their
     * relative order.
     * 
     * @param object
     *            the integer object to compare this object to.
     * @return a negative value if the value of this integer is less than the
     *         value of {@code object}; 0 if the value of this integer and the
     *         value of {@code object} are equal; a positive value if the value
     *         of this integer is greater than the value of {@code object}.
     * @see java.lang.Comparable
     * @since 1.2
     */
    public int compareTo(Integer object) {
        if (object == null) {
            // When object is nil, Obj-C ignores messages sent to it.
            throw new NullPointerException();
        }
        return value > object.value ? 1 : (value < object.value ? -1 : 0);
    }

    /**
     * Parses the specified string and returns a {@code Integer} instance if the
     * string can be decoded into an integer value. The string may be an
     * optional minus sign "-" followed by a hexadecimal ("0x..." or "#..."),
     * octal ("0..."), or decimal ("...") representation of an integer.
     * 
     * @param string
     *            a string representation of an integer value.
     * @return an {@code Integer} containing the value represented by
     *         {@code string}.
     * @throws NumberFormatException
     *             if {@code string} can not be parsed as an integer value.
     */
    public static Integer decode(String string) throws NumberFormatException {
        if (string == null) {
            throw new NullPointerException();
        }
        int length = string.length(), i = 0;
        if (length == 0) {
            throw new NumberFormatException();
        }
        char firstDigit = string.charAt(i);
        boolean negative = firstDigit == '-';
        if (negative) {
            if (length == 1) {
                throw new NumberFormatException(string);
            }
            firstDigit = string.charAt(++i);
        }

        int base = 10;
        if (firstDigit == '0') {
            if (++i == length) {
                return valueOf(0);
            }
            if ((firstDigit = string.charAt(i)) == 'x' || firstDigit == 'X') {
                if (++i == length) {
                    throw new NumberFormatException(string);
                }
                base = 16;
            } else {
                base = 8;
            }
        } else if (firstDigit == '#') {
            if (++i == length) {
                throw new NumberFormatException(string);
            }
            base = 16;
        }

        int result = parse(string, i, base, negative);
        return valueOf(result);
    }

    @Override
    public double doubleValue() {
        return value;
    }

    /**
     * Compares this instance with the specified object and indicates if they
     * are equal. In order to be equal, {@code o} must be an instance of
     * {@code Integer} and have the same integer value as this object.
     * 
     * @param o
     *            the object to compare this integer with.
     * @return {@code true} if the specified object is equal to this
     *         {@code Integer}; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        return (o instanceof Integer)
                && (value == ((Integer) o).value);
    }

    @Override
    public float floatValue() {
        return value;
    }

    /**
     * Returns the {@code Integer} value of the system property identified by
     * {@code string}. Returns {@code null} if {@code string} is {@code null}
     * or empty, if the property can not be found or if its value can not be
     * parsed as an integer.
     * 
     * @param string
     *            the name of the requested system property.
     * @return the requested property's value as an {@code Integer} or
     *         {@code null}.
     */
    public static Integer getInteger(String string) {
        if (string == null || string.length() == 0) {
            return null;
        }
        String prop = System.getProperty(string);
        if (prop == null) {
            return null;
        }
        try {
            return decode(prop);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Returns the {@code Integer} value of the system property identified by
     * {@code string}. Returns the specified default value if {@code string} is
     * {@code null} or empty, if the property can not be found or if its value
     * can not be parsed as an integer.
     * 
     * @param string
     *            the name of the requested system property.
     * @param defaultValue
     *            the default value that is returned if there is no integer
     *            system property with the requested name.
     * @return the requested property's value as an {@code Integer} or the
     *         default value.
     */
    public static Integer getInteger(String string, int defaultValue) {
        if (string == null || string.length() == 0) {
            return valueOf(defaultValue);
        }
        String prop = System.getProperty(string);
        if (prop == null) {
            return valueOf(defaultValue);
        }
        try {
            return decode(prop);
        } catch (NumberFormatException ex) {
            return valueOf(defaultValue);
        }
    }

    /**
     * Returns the {@code Integer} value of the system property identified by
     * {@code string}. Returns the specified default value if {@code string} is
     * {@code null} or empty, if the property can not be found or if its value
     * can not be parsed as an integer.
     * 
     * @param string
     *            the name of the requested system property.
     * @param defaultValue
     *            the default value that is returned if there is no integer
     *            system property with the requested name.
     * @return the requested property's value as an {@code Integer} or the
     *         default value.
     */
    public static Integer getInteger(String string, Integer defaultValue) {
        if (string == null || string.length() == 0) {
            return defaultValue;
        }
        String prop = System.getProperty(string);
        if (prop == null) {
            return defaultValue;
        }
        try {
            return decode(prop);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    @Override
    public int hashCode() {
        return value;
    }

    /**
     * Gets the primitive value of this int.
     * 
     * @return this object's primitive value.
     */
    @Override
    public int intValue() {
        return value;
    }

    @Override
    public long longValue() {
        return value;
    }

    /**
     * Parses the specified string as a signed decimal integer value. The ASCII
     * character \u002d ('-') is recognized as the minus sign.
     * 
     * @param string
     *            the string representation of an integer value.
     * @return the primitive integer value represented by {@code string}.
     * @throws NumberFormatException
     *             if {@code string} is {@code null}, has a length of zero or
     *             can not be parsed as an integer value.
     */
    public static int parseInt(String string) throws NumberFormatException {
        return parseInt(string, 10);
    }

    /**
     * Parses the specified string as a signed integer value using the specified
     * radix. The ASCII character \u002d ('-') is recognized as the minus sign.
     * 
     * @param string
     *            the string representation of an integer value.
     * @param radix
     *            the radix to use when parsing.
     * @return the primitive integer value represented by {@code string} using
     *         {@code radix}.
     * @throws NumberFormatException
     *             if {@code string} is {@code null} or has a length of zero,
     *             {@code radix < Character.MIN_RADIX},
     *             {@code radix > Character.MAX_RADIX}, or if {@code string}
     *             can not be parsed as an integer value.
     */
    public static int parseInt(String string, int radix)
            throws NumberFormatException {
        if (string == null || radix < Character.MIN_RADIX
                || radix > Character.MAX_RADIX) {
            throw new NumberFormatException();
        }
        int length = string.length(), i = 0;
        if (length == 0) {
            throw new NumberFormatException(string);
        }
        boolean negative = string.charAt(i) == '-';
        if (negative && ++i == length) {
            throw new NumberFormatException(string);
        }

        return parse(string, i, radix, negative);
    }

    private static int parse(String string, int offset, int radix,
            boolean negative) throws NumberFormatException {
        int max = (negative ? Integer.MIN_VALUE : -Integer.MAX_VALUE) / radix;
        int result = 0, length = string.length();
        while (offset < length) {
            int digit = Character.digit(string.charAt(offset++), radix);
            if (digit == -1) {
                throw new NumberFormatException(string);
            }
            if (max > result) {
                throw new NumberFormatException(string);
            }
            int next = result * radix - digit;
            if (next > result) {
                throw new NumberFormatException(string);
            }
            result = next;
        }
        if (!negative) {
            result = -result;
            if (result < 0) {
                throw new NumberFormatException(string);
            }
        }
        return result;
    }

    @Override
    public short shortValue() {
        return (short) value;
    }

    /**
     * Converts the specified integer into its binary string representation. The
     * returned string is a concatenation of '0' and '1' characters.
     * 
     * @param i
     *            the integer to convert.
     * @return the binary string representation of {@code i}.
     */
    public static String toBinaryString(int i) {
        int count = 1, j = i;

        if (i < 0) {
            count = 32;
        } else {
            while ((j >>>= 1) != 0) {
                count++;
            }
        }

        char[] buffer = new char[count];
        do {
            buffer[--count] = (char) ((i & 1) + '0');
            i >>>= 1;
        } while (count > 0);
        return new String(0, buffer.length, buffer);
    }

    /**
     * Converts the specified integer into its hexadecimal string
     * representation. The returned string is a concatenation of characters from
     * '0' to '9' and 'a' to 'f'.
     * 
     * @param i
     *            the integer to convert.
     * @return the hexadecimal string representation of {@code i}.
     */
    public static String toHexString(int i) {
        int count = 1, j = i;

        if (i < 0) {
            count = 8;
        } else {
            while ((j >>>= 4) != 0) {
                count++;
            }
        }

        char[] buffer = new char[count];
        do {
            int t = i & 15;
            if (t > 9) {
                t = t - 10 + 'a';
            } else {
                t += '0';
            }
            buffer[--count] = (char) t;
            i >>>= 4;
        } while (count > 0);
        return new String(0, buffer.length, buffer);
    }

    /**
     * Converts the specified integer into its octal string representation. The
     * returned string is a concatenation of characters from '0' to '7'.
     * 
     * @param i
     *            the integer to convert.
     * @return the octal string representation of {@code i}.
     */
    public static String toOctalString(int i) {
        int count = 1, j = i;

        if (i < 0) {
            count = 11;
        } else {
            while ((j >>>= 3) != 0) {
                count++;
            }
        }

        char[] buffer = new char[count];
        do {
            buffer[--count] = (char) ((i & 7) + '0');
            i >>>= 3;
        } while (count > 0);
        return new String(0, buffer.length, buffer);
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    /**
     * Converts the specified integer into its decimal string representation.
     * The returned string is a concatenation of a minus sign if the number is
     * negative and characters from '0' to '9'.
     * 
     * @param value
     *            the integer to convert.
     * @return the decimal string representation of {@code value}.
     */
    public static native String toString(int value) /*-{
      NSNumber *num = [NSNumber numberWithInt:value];
      return [num stringValue];
    }-*/;

    /**
     * Converts the specified integer into a string representation based on the
     * specified radix. The returned string is a concatenation of a minus sign
     * if the number is negative and characters from '0' to '9' and 'a' to 'z',
     * depending on the radix. If {@code radix} is not in the interval defined
     * by {@code Character.MIN_RADIX} and {@code Character.MAX_RADIX} then 10 is
     * used as the base for the conversion.
     * 
     * @param i
     *            the integer to convert.
     * @param radix
     *            the base to use for the conversion.
     * @return the string representation of {@code i}.
     */
    public static String toString(int i, int radix) {
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
            radix = 10;
        }
        if (i == 0) {
            return "0"; //$NON-NLS-1$
        }

        int count = 2, j = i;
        boolean negative = i < 0;
        if (!negative) {
            count = 1;
            j = -i;
        }
        while ((i /= radix) != 0) {
            count++;
        }

        char[] buffer = new char[count];
        do {
            int ch = 0 - (j % radix);
            if (ch > 9) {
                ch = ch - 10 + 'a';
            } else {
                ch += '0';
            }
            buffer[--count] = (char) ch;
        } while ((j /= radix) != 0);
        if (negative) {
            buffer[0] = '-';
        }
        return new String(0, buffer.length, buffer);
    }

    /**
     * Parses the specified string as a signed decimal integer value.
     * 
     * @param string
     *            the string representation of an integer value.
     * @return an {@code Integer} instance containing the integer value
     *         represented by {@code string}.
     * @throws NumberFormatException
     *             if {@code string} is {@code null}, has a length of zero or
     *             can not be parsed as an integer value.
     * @see #parseInt(String)
     */
    public static Integer valueOf(String string) throws NumberFormatException {
        return valueOf(parseInt(string));
    }

    /**
     * Parses the specified string as a signed integer value using the specified
     * radix.
     * 
     * @param string
     *            the string representation of an integer value.
     * @param radix
     *            the radix to use when parsing.
     * @return an {@code Integer} instance containing the integer value
     *         represented by {@code string} using {@code radix}.
     * @throws NumberFormatException
     *             if {@code string} is {@code null} or has a length of zero,
     *             {@code radix < Character.MIN_RADIX},
     *             {@code radix > Character.MAX_RADIX}, or if {@code string}
     *             can not be parsed as an integer value.
     * @see #parseInt(String, int)
     */
    public static Integer valueOf(String string, int radix)
            throws NumberFormatException {
        return valueOf(parseInt(string, radix));
    }

    /**
     * Determines the highest (leftmost) bit of the specified integer that is 1
     * and returns the bit mask value for that bit. This is also referred to as
     * the Most Significant 1 Bit. Returns zero if the specified integer is
     * zero.
     * 
     * @param i
     *            the integer to examine.
     * @return the bit mask indicating the highest 1 bit in {@code i}.
     * @since 1.5
     */
    public static int highestOneBit(int i) {
        i |= (i >> 1);
        i |= (i >> 2);
        i |= (i >> 4);
        i |= (i >> 8);
        i |= (i >> 16);
        return (i & ~(i >>> 1));
    }

    /**
     * Determines the lowest (rightmost) bit of the specified integer that is 1
     * and returns the bit mask value for that bit. This is also referred
     * to as the Least Significant 1 Bit. Returns zero if the specified integer
     * is zero.
     * 
     * @param i
     *            the integer to examine.
     * @return the bit mask indicating the lowest 1 bit in {@code i}.
     * @since 1.5
     */
    public static int lowestOneBit(int i) {
        return (i & (-i));
    }

    /**
     * Determines the number of leading zeros in the specified integer prior to
     * the {@link #highestOneBit(int) highest one bit}.
     *
     * @param i
     *            the integer to examine.
     * @return the number of leading zeros in {@code i}.
     * @since 1.5
     */
    public static int numberOfLeadingZeros(int i) {
        i |= i >> 1;
        i |= i >> 2;
        i |= i >> 4;
        i |= i >> 8;
        i |= i >> 16;
        return bitCount(~i);
    }

    /**
     * Determines the number of trailing zeros in the specified integer after
     * the {@link #lowestOneBit(int) lowest one bit}.
     *
     * @param i
     *            the integer to examine.
     * @return the number of trailing zeros in {@code i}.
     * @since 1.5
     */
    public static int numberOfTrailingZeros(int i) {
        return bitCount((i & -i) - 1);
    }

    /**
     * Counts the number of 1 bits in the specified integer; this is also
     * referred to as population count.
     *
     * @param i
     *            the integer to examine.
     * @return the number of 1 bits in {@code i}.
     * @since 1.5
     */
    public static int bitCount(int i) {
        i -= ((i >> 1) & 0x55555555);
        i = (i & 0x33333333) + ((i >> 2) & 0x33333333);
        i = (((i >> 4) + i) & 0x0F0F0F0F);
        i += (i >> 8);
        i += (i >> 16);
        return (i & 0x0000003F);
    }

    /**
     * Rotates the bits of the specified integer to the left by the specified
     * number of bits.
     *
     * @param i
     *            the integer value to rotate left.
     * @param distance
     *            the number of bits to rotate.
     * @return the rotated value.
     * @since 1.5
     */
    public static int rotateLeft(int i, int distance) {
        if (distance == 0) {
            return i;
        }
        /*
         * According to JLS3, 15.19, the right operand of a shift is always
         * implicitly masked with 0x1F, which the negation of 'distance' is
         * taking advantage of.
         */
        return ((i << distance) | (i >>> (-distance)));
    }

    /**
     * Rotates the bits of the specified integer to the right by the specified
     * number of bits.
     *
     * @param i
     *            the integer value to rotate right.
     * @param distance
     *            the number of bits to rotate.
     * @return the rotated value.
     * @since 1.5
     */
    public static int rotateRight(int i, int distance) {
        if (distance == 0) {
            return i;
        }
        /*
         * According to JLS3, 15.19, the right operand of a shift is always
         * implicitly masked with 0x1F, which the negation of 'distance' is
         * taking advantage of.
         */
        return ((i >>> distance) | (i << (-distance)));
    }

    /**
     * Reverses the order of the bytes of the specified integer.
     * 
     * @param i
     *            the integer value for which to reverse the byte order.
     * @return the reversed value.
     * @since 1.5
     */
    public static int reverseBytes(int i) {
        int b3 = i >>> 24;
        int b2 = (i >>> 8) & 0xFF00;
        int b1 = (i & 0xFF00) << 8;
        int b0 = i << 24;
        return (b0 | b1 | b2 | b3);
    }

    /**
     * Reverses the order of the bits of the specified integer.
     * 
     * @param i
     *            the integer value for which to reverse the bit order.
     * @return the reversed value.
     * @since 1.5
     */
    public static int reverse(int i) {
        // From Hacker's Delight, 7-1, Figure 7-1
        i = (i & 0x55555555) << 1 | (i >> 1) & 0x55555555;
        i = (i & 0x33333333) << 2 | (i >> 2) & 0x33333333;
        i = (i & 0x0F0F0F0F) << 4 | (i >> 4) & 0x0F0F0F0F;
        return reverseBytes(i);
    }

    /**
     * Returns the value of the {@code signum} function for the specified
     * integer.
     * 
     * @param i
     *            the integer value to check.
     * @return -1 if {@code i} is negative, 1 if {@code i} is positive, 0 if
     *         {@code i} is zero.
     * @since 1.5
     */
    public static int signum(int i) {
        return (i == 0 ? 0 : (i < 0 ? -1 : 1));
    }

    /**
     * Returns a {@code Integer} instance for the specified integer value.
     * <p>
     * If it is not necessary to get a new {@code Integer} instance, it is
     * recommended to use this method instead of the constructor, since it
     * maintains a cache of instances which may result in better performance.
     *
     * @param i
     *            the integer value to store in the instance.
     * @return a {@code Integer} instance containing {@code i}.
     * @since 1.5
     */
    public static Integer valueOf(int i) {
        if (i < -128 || i > 127) {
            return new Integer(i);
        }
        return valueOfCache.CACHE [i+128];
    }

   static class valueOfCache {
        /**
         * <p>
         * A cache of instances used by {@link Integer#valueOf(int)} and auto-boxing.
         */
        static final Integer[] CACHE = new Integer[256];

        static {
            for(int i=-128; i<=127; i++) {
                CACHE[i+128] = new Integer(i);
            }
        }
    }
}
