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

/*-{
// From apache-harmony/classlib/modules/luni/src/main/native/luni/shared/floatbits.c
#define SINGLE_EXPONENT_MASK    0x7F800000
#define SINGLE_MANTISSA_MASK    0x007FFFFF
#define SINGLE_NAN_BITS         (SINGLE_EXPONENT_MASK | 0x00400000)
}-*/

/**
 * The wrapper for the primitive type {@code float}.
 *
 * @see java.lang.Number
 * @since 1.0
 */
public final class Float extends Number implements Comparable<Float> {

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
     * Constant for the Positive Infinity value of the {@code float} type.
     */
    public static final float POSITIVE_INFINITY = 1.0f / 0.0f;

    /**
     * Constant for the Negative Infinity value of the {@code float} type.
     */
    public static final float NEGATIVE_INFINITY = -1.0f / 0.0f;

    /**
     * The {@link Class} object that represents the primitive type {@code
     * float}.
     *
     * @since 1.1
     */
    @SuppressWarnings("unchecked")
    public static final Class<Float> TYPE = (Class<Float>) new float[0]
            .getClass().getComponentType();

    // Note: This can't be set to "float.class", since *that* is
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
     *             if {@code string} can not be decoded into a float value.
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
        if (object == null) {
            // When object is nil, Obj-C ignores messages sent to it.
            throw new NullPointerException();
        }
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
     * Compares this instance with the specified object and indicates if they
     * are equal. In order to be equal, {@code object} must be an instance of
     * {@code Float} and have the same float value as this object.
     *
     * @param object
     *            the object to compare this float with.
     * @return {@code true} if the specified object is equal to this
     *         {@code Float}; {@code false} otherwise.
     */
    @Override
    public native boolean equals(Object object) /*-{
        NSComparisonResult result = [self compare:object];
        return result == NSOrderedSame;
    }-*/;

    /**
     * Converts the specified float value to a binary representation conforming
     * to the IEEE 754 floating-point single precision bit layout. All
     * <em>Not-a-Number (NaN)</em> values are converted to a single NaN
     * representation ({@code 0x7ff8000000000000L}).
     *
     * @param value
     *            the float value to convert.
     * @return the IEEE 754 floating-point single precision representation of
     *         {@code value}.
     * @see #floatToRawIntBits(float)
     * @see #intBitsToFloat(int)
     */
    public static native int floatToIntBits(float value) /*-{
      // Modified from Harmony JNI implementation.
      int intValue = *(int *) &value;
      if ((intValue & SINGLE_EXPONENT_MASK) == SINGLE_EXPONENT_MASK) {
        if (intValue & SINGLE_MANTISSA_MASK) {
          return SINGLE_NAN_BITS;
        }
      }
      return intValue;
    }-*/;

    /**
     * Converts the specified float value to a binary representation conforming
     * to the IEEE 754 floating-point single precision bit layout.
     * <em>Not-a-Number (NaN)</em> values are preserved.
     *
     * @param value
     *            the float value to convert.
     * @return the IEEE 754 floating-point single precision representation of
     *         {@code value}.
     * @see #floatToIntBits(float)
     * @see #intBitsToFloat(int)
     */
    public static native int floatToRawIntBits(float value) /*-{
        return *(int *) &value;
    }-*/;

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
     * Converts the specified IEEE 754 floating-point single precision bit
     * pattern to a Java float value.
     *
     * @param bits
     *            the IEEE 754 floating-point single precision representation of
     *            a float value.
     * @return the float value converted from {@code bits}.
     * @see #floatToIntBits(float)
     * @see #floatToRawIntBits(float)
     */
    public static native float intBitsToFloat(int bits) /*-{
        return *(float *) &bits;
    }-*/;

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
    public static native boolean isInfinite(float f) /*-{
        return isinf(f);
    }-*/;

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
    public native static boolean isNaN(float f) /*-{
        return isnan(f);
    }-*/;

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
     *             if {@code string} is {@code null}, has a length of zero or
     *             can not be parsed as a float value.
     * @see #valueOf(String)
     * @since 1.2
     */
    public native static float parseFloat(String string) throws NumberFormatException /*-{
        return [string floatValue];
    }-*/;

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
    public native static String toString(float f) /*-{
        return [NSString stringWithFormat:@"%01.1f", f];
    }-*/;

    /**
     * Parses the specified string as a float value.
     *
     * @param string
     *            the string representation of a float value.
     * @return a {@code Float} instance containing the float value represented
     *         by {@code string}.
     * @throws NumberFormatException
     *             if {@code string} is {@code null}, has a length of zero or
     *             can not be parsed as a float value.
     * @see #parseFloat(String)
     */
    public static Float valueOf(String string) throws NumberFormatException {
        return valueOf(parseFloat(string));
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
    public static native String toHexString(float f) /*-{
        return [NSString stringWithFormat:@"%A", (double) f];
    }-*/;
}
