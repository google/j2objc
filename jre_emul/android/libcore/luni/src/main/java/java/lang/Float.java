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
 * The wrapper for the primitive type {@code float}.
 *
 * @see java.lang.Number
 * @since 1.0
 */
public final class Float extends Number implements Comparable<Float> {
    static final int EXPONENT_BIAS = 127;

    static final int EXPONENT_BITS = 9;
    static final int MANTISSA_BITS = 23;
    static final int NON_MANTISSA_BITS = 9;

    static final int SIGN_MASK     = 0x80000000;
    static final int EXPONENT_MASK = 0x7f800000;
    static final int MANTISSA_MASK = 0x007fffff;

    private static final long serialVersionUID = -2671257302660747028L;

    /**
     * The value which the receiver represents.
     */
    private final float value;

    /**
     * Constant for the maximum {@code float} value, (2 - 2<sup>-23</sup>) * 2<sup>127</sup>.
     */
    public static final float MAX_VALUE = 3.40282346638528860e+38f;

    /**
     * Constant for the minimum {@code float} value, 2<sup>-149</sup>.
     */
    public static final float MIN_VALUE = 1.40129846432481707e-45f;

    /**
     * Constant for the Not-a-Number (NaN) value of the {@code float} type.
     */
    public static final float NaN = 0.0f / 0.0f;

    /**
     * Constant for the positive infinity value of the {@code float} type.
     */
    public static final float POSITIVE_INFINITY = 1.0f / 0.0f;

    /**
     * Constant for the negative infinity value of the {@code float} type.
     */
    public static final float NEGATIVE_INFINITY = -1.0f / 0.0f;

    /**
     * Constant for the smallest positive normal value of the {@code float} type.
     *
     * @since 1.6
     */
    public static final float MIN_NORMAL = 1.1754943508222875E-38f;

    /**
     * Maximum base-2 exponent that a finite value of the {@code float} type may have.
     * Equal to {@code Math.getExponent(Float.MAX_VALUE)}.
     *
     * @since 1.6
     */
    public static final int MAX_EXPONENT = 127;

    /**
     * Minimum base-2 exponent that a normal value of the {@code float} type may have.
     * Equal to {@code Math.getExponent(Float.MIN_NORMAL)}.
     *
     * @since 1.6
     */
    public static final int MIN_EXPONENT = -126;

    /**
     * The {@link Class} object that represents the primitive type {@code
     * float}.
     *
     * @since 1.1
     */
    @SuppressWarnings("unchecked")
    public static final Class<Float> TYPE
            = (Class<Float>) float[].class.getComponentType();
    // Note: Float.TYPE can't be set to "float.class", since *that* is
    // defined to be "java.lang.Float.TYPE";

    /**
     * Constant for the number of bits needed to represent a {@code float} in
     * two's complement form.
     *
     * @since 1.5
     */
    public static final int SIZE = 32;

    /**
     * Constructs a new {@code Float} with the specified primitive float value.
     *
     * @param value
     *            the primitive float value to store in the new instance.
     */
    public Float(float value) {
        this.value = value;
    }

    /**
     * Constructs a new {@code Float} with the specified primitive double value.
     *
     * @param value
     *            the primitive double value to store in the new instance.
     */
    public Float(double value) {
        this.value = (float) value;
    }

    /**
     * Constructs a new {@code Float} from the specified string.
     *
     * @param string
     *            the string representation of a float value.
     * @throws NumberFormatException
     *             if {@code string} can not be parsed as a float value.
     * @see #parseFloat(String)
     */
    public Float(String string) throws NumberFormatException {
        this(parseFloat(string));
    }

    /**
     * Compares this object to the specified float object to determine their
     * relative order. There are two special cases:
     * <ul>
     * <li>{@code Float.NaN} is equal to {@code Float.NaN} and it is greater
     * than any other float value, including {@code Float.POSITIVE_INFINITY};</li>
     * <li>+0.0f is greater than -0.0f</li>
     * </ul>
     *
     * @param object
     *            the float object to compare this object to.
     * @return a negative value if the value of this float is less than the
     *         value of {@code object}; 0 if the value of this float and the
     *         value of {@code object} are equal; a positive value if the value
     *         of this float is greater than the value of {@code object}.
     * @see java.lang.Comparable
     * @since 1.2
     */
    public int compareTo(Float object) {
        return compare(value, object.value);
    }

    @Override
    public byte byteValue() {
        return (byte) value;
    }

    @Override
    public double doubleValue() {
        return value;
    }

    /**
     * Tests this double for equality with {@code object}.
     * To be equal, {@code object} must be an instance of {@code Float} and
     * {@code floatToIntBits} must give the same value for both objects.
     *
     * <p>Note that, unlike {@code ==}, {@code -0.0} and {@code +0.0} compare
     * unequal, and {@code NaN}s compare equal by this method.
     *
     * @param object
     *            the object to compare this float with.
     * @return {@code true} if the specified object is equal to this
     *         {@code Float}; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object object) {
        return (object instanceof Float) &&
                (floatToIntBits(this.value) == floatToIntBits(((Float) object).value));
    }

    /**
     * Returns an integer corresponding to the bits of the given
     * <a href="http://en.wikipedia.org/wiki/IEEE_754-1985">IEEE 754</a> single precision
     * float {@code value}. All <em>Not-a-Number (NaN)</em> values are converted to a single NaN
     * representation ({@code 0x7fc00000}) (compare to {@link #floatToRawIntBits}).
     */
    public static int floatToIntBits(float value) {
        if (value != value) {
            return 0x7fc00000;  // NaN.
        } else {
            return floatToRawIntBits(value);
        }
    }

    /**
     * Returns an integer corresponding to the bits of the given
     * <a href="http://en.wikipedia.org/wiki/IEEE_754-1985">IEEE 754</a> single precision
     * float {@code value}. <em>Not-a-Number (NaN)</em> values are preserved (compare
     * to {@link #floatToIntBits}).
     */
    public static native int floatToRawIntBits(float value) /*-[
      return *(int *) &value;
    ]-*/;

    /**
     * Gets the primitive value of this float.
     *
     * @return this object's primitive value.
     */
    @Override
    public float floatValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return floatToIntBits(value);
    }

    /**
     * Returns the <a href="http://en.wikipedia.org/wiki/IEEE_754-1985">IEEE 754</a>
     * single precision float corresponding to the given {@code bits}.
     */
    public static native float intBitsToFloat(int bits) /*-[
      return *(float *) &bits;
    ]-*/;

    @Override
    public int intValue() {
        return (int) value;
    }

    /**
     * Indicates whether this object represents an infinite value.
     *
     * @return {@code true} if the value of this float is positive or negative
     *         infinity; {@code false} otherwise.
     */
    public boolean isInfinite() {
        return isInfinite(value);
    }

    /**
     * Indicates whether the specified float represents an infinite value.
     *
     * @param f
     *            the float to check.
     * @return {@code true} if the value of {@code f} is positive or negative
     *         infinity; {@code false} otherwise.
     */
    public static boolean isInfinite(float f) {
        return (f == POSITIVE_INFINITY) || (f == NEGATIVE_INFINITY);
    }

    /**
     * Indicates whether this object is a <em>Not-a-Number (NaN)</em> value.
     *
     * @return {@code true} if this float is <em>Not-a-Number</em>;
     *         {@code false} if it is a (potentially infinite) float number.
     */
    public boolean isNaN() {
        return isNaN(value);
    }

    /**
     * Indicates whether the specified float is a <em>Not-a-Number (NaN)</em>
     * value.
     *
     * @param f
     *            the float value to check.
     * @return {@code true} if {@code f} is <em>Not-a-Number</em>;
     *         {@code false} if it is a (potentially infinite) float number.
     */
    public static boolean isNaN(float f) {
        return f != f;
    }

    @Override
    public long longValue() {
        return (long) value;
    }

    /**
     * Parses the specified string as a float value.
     *
     * @param string
     *            the string representation of a float value.
     * @return the primitive float value represented by {@code string}.
     * @throws NumberFormatException
     *             if {@code string} can not be parsed as a float value.
     * @see #valueOf(String)
     * @since 1.2
     */
    public static float parseFloat(String string) throws NumberFormatException {
        return StringToReal.parseFloat(string);
    }

    @Override
    public short shortValue() {
        return (short) value;
    }

    @Override
    public String toString() {
        return Float.toString(value);
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * specified float value.
     *
     * @param f
     *             the float to convert to a string.
     * @return a printable representation of {@code f}.
     */
    public static String toString(float f) {
        return RealToString.floatToString(f);
    }

    /**
     * Parses the specified string as a float value.
     *
     * @param string
     *            the string representation of a float value.
     * @return a {@code Float} instance containing the float value represented
     *         by {@code string}.
     * @throws NumberFormatException
     *             if {@code string} can not be parsed as a float value.
     * @see #parseFloat(String)
     */
    public static Float valueOf(String string) throws NumberFormatException {
        return parseFloat(string);
    }

    /**
     * Compares the two specified float values. There are two special cases:
     * <ul>
     * <li>{@code Float.NaN} is equal to {@code Float.NaN} and it is greater
     * than any other float value, including {@code Float.POSITIVE_INFINITY};</li>
     * <li>+0.0f is greater than -0.0f</li>
     * </ul>
     *
     * @param float1
     *            the first value to compare.
     * @param float2
     *            the second value to compare.
     * @return a negative value if {@code float1} is less than {@code float2};
     *         0 if {@code float1} and {@code float2} are equal; a positive
     *         value if {@code float1} is greater than {@code float2}.
     * @since 1.4
     */
    public static int compare(float float1, float float2) {
        // Non-zero, non-NaN checking.
        if (float1 > float2) {
            return 1;
        }
        if (float2 > float1) {
            return -1;
        }
        if (float1 == float2 && 0.0f != float1) {
            return 0;
        }

        // NaNs are equal to other NaNs and larger than any other float
        if (isNaN(float1)) {
            if (isNaN(float2)) {
                return 0;
            }
            return 1;
        } else if (isNaN(float2)) {
            return -1;
        }

        // Deal with +0.0 and -0.0
        int f1 = floatToRawIntBits(float1);
        int f2 = floatToRawIntBits(float2);
        // The below expression is equivalent to:
        // (f1 == f2) ? 0 : (f1 < f2) ? -1 : 1
        // because f1 and f2 are either 0 or Integer.MIN_VALUE
        return (f1 >> 31) - (f2 >> 31);
    }

    /**
     * Returns a {@code Float} instance for the specified float value.
     *
     * @param f
     *            the float value to store in the instance.
     * @return a {@code Float} instance containing {@code f}.
     * @since 1.5
     */
    public static Float valueOf(float f) {
        return new Float(f);
    }

    /**
     * Converts the specified float into its hexadecimal string representation.
     *
     * @param f
     *            the float to convert.
     * @return the hexadecimal string representation of {@code f}.
     * @since 1.5
     */
    public static String toHexString(float f) {
        /*
         * Reference: http://en.wikipedia.org/wiki/IEEE_754-1985
         */
        if (f != f) {
            return "NaN";
        }
        if (f == POSITIVE_INFINITY) {
            return "Infinity";
        }
        if (f == NEGATIVE_INFINITY) {
            return "-Infinity";
        }

        int bitValue = floatToIntBits(f);

        boolean negative = (bitValue & 0x80000000) != 0;
        // mask exponent bits and shift down
        int exponent = (bitValue & 0x7f800000) >>> 23;
        // mask significand bits and shift up
        // significand is 23-bits, so we shift to treat it like 24-bits
        int significand = (bitValue & 0x007FFFFF) << 1;

        if (exponent == 0 && significand == 0) {
            return (negative ? "-0x0.0p0" : "0x0.0p0");
        }

        StringBuilder hexString = new StringBuilder(10);
        if (negative) {
            hexString.append("-0x");
        } else {
            hexString.append("0x");
        }

        if (exponent == 0) { // denormal (subnormal) value
            hexString.append("0.");
            // significand is 23-bits, so there can be 6 hex digits
            int fractionDigits = 6;
            // remove trailing hex zeros, so Integer.toHexString() won't print
            // them
            while ((significand != 0) && ((significand & 0xF) == 0)) {
                significand >>>= 4;
                fractionDigits--;
            }
            // this assumes Integer.toHexString() returns lowercase characters
            String hexSignificand = Integer.toHexString(significand);

            // if there are digits left, then insert some '0' chars first
            if (significand != 0 && fractionDigits > hexSignificand.length()) {
                int digitDiff = fractionDigits - hexSignificand.length();
                while (digitDiff-- != 0) {
                    hexString.append('0');
                }
            }
            hexString.append(hexSignificand);
            hexString.append("p-126");
        } else { // normal value
            hexString.append("1.");
            // significand is 23-bits, so there can be 6 hex digits
            int fractionDigits = 6;
            // remove trailing hex zeros, so Integer.toHexString() won't print
            // them
            while ((significand != 0) && ((significand & 0xF) == 0)) {
                significand >>>= 4;
                fractionDigits--;
            }
            // this assumes Integer.toHexString() returns lowercase characters
            String hexSignificand = Integer.toHexString(significand);

            // if there are digits left, then insert some '0' chars first
            if (significand != 0 && fractionDigits > hexSignificand.length()) {
                int digitDiff = fractionDigits - hexSignificand.length();
                while (digitDiff-- != 0) {
                    hexString.append('0');
                }
            }
            hexString.append(hexSignificand);
            hexString.append('p');
            // remove exponent's 'bias' and convert to a string
            hexString.append(exponent - 127);
        }
        return hexString.toString();
    }

    /*
     * These ObjC methods are needed to support subclassing of NSNumber.
     * objCType is used by descriptionWithLocale:.
     * getValue: is used by copyWithZone:.
     */
    /*-[
    - (const char *)objCType {
      return "f";
    }

    - (void)getValue:(void *)buffer {
      *((float *) buffer) = value_;
    }
    ]-*/
}
