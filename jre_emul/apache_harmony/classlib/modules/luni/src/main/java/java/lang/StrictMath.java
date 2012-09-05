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

import java.util.Random;

/**
 * Class StrictMath provides basic math constants and operations such as
 * trigonometric functions, hyperbolic functions, exponential, logarithms, etc.
 * <p>
 * In contrast to class {@link Math}, the methods in this class return exactly
 * the same results on all platforms. Algorithms based on these methods thus
 * behave the same (e.g. regarding numerical convergence) on all platforms,
 * complying with the slogan "write once, run everywhere". On the other side,
 * the implementation of class StrictMath may be less efficient than that of
 * class Math, as class StrictMath cannot utilize platform specific features
 * such as an extended precision math co-processors.
 * <p>
 * The methods in this class are specified using the "Freely Distributable Math
 * Library" (fdlibm), version 5.3.
 * <p>
 * <a href="http://www.netlib.org/fdlibm/">http://www.netlib.org/fdlibm/</a>
 */
public final class StrictMath {

    /**
     * The double value closest to e, the base of the natural logarithm.
     */
    public static final double E = 2.718281828459045;

    /**
     * The double value closest to pi, the ratio of a circle's circumference to
     * its diameter.
     */
    public static final double PI = 3.141592653589793;

    private static java.util.Random random_;

    /**
     * Prevents this class from being instantiated.
     */
    private StrictMath() {
    }

    /**
     * Returns the absolute value of the argument.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code abs(-0.0) = +0.0}</li>
     * <li>{@code abs(+infinity) = +infinity}</li>
     * <li>{@code abs(-infinity) = +infinity}</li>
     * <li>{@code abs(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value whose absolute value has to be computed.
     * @return the absolute value of the argument.
     */
    public static double abs(double d) {
        long bits = Double.doubleToLongBits(d);
        bits &= 0x7fffffffffffffffL;
        return Double.longBitsToDouble(bits);
    }

    /**
     * Returns the absolute value of the argument.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code abs(-0.0) = +0.0}</li>
     * <li>{@code abs(+infinity) = +infinity}</li>
     * <li>{@code abs(-infinity) = +infinity}</li>
     * <li>{@code abs(NaN) = NaN}</li>
     * </ul>
     *
     * @param f
     *            the value whose absolute value has to be computed.
     * @return the argument if it is positive, otherwise the negation of the
     *         argument.
     */
    public static float abs(float f) {
        int bits = Float.floatToIntBits(f);
        bits &= 0x7fffffff;
        return Float.intBitsToFloat(bits);
    }

    /**
     * Returns the absolute value of the argument.
     * <p>
     * If the argument is {@code Integer.MIN_VALUE}, {@code Integer.MIN_VALUE}
     * is returned.
     *
     * @param i
     *            the value whose absolute value has to be computed.
     * @return the argument if it is positive, otherwise the negation of the
     *         argument.
     */
    public static int abs(int i) {
        return i >= 0 ? i : -i;
    }

    /**
     * Returns the absolute value of the argument.
     * <p>
     * If the argument is {@code Long.MIN_VALUE}, {@code Long.MIN_VALUE} is
     * returned.
     *
     * @param l
     *            the value whose absolute value has to be computed.
     * @return the argument if it is positive, otherwise the negation of the
     *         argument.
     */
    public static long abs(long l) {
        return l >= 0 ? l : -l;
    }

    /**
     * Returns the closest double approximation of the arc cosine of the
     * argument within the range {@code [0..pi]}.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code acos((anything > 1) = NaN}</li>
     * <li>{@code acos((anything < -1) = NaN}</li>
     * <li>{@code acos(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value to compute arc cosine of.
     * @return the arc cosine of the argument.
     */
    public static native double acos(double d) /*-{
      return acos(d);
    }-*/;

    /**
     * Returns the closest double approximation of the arc sine of the argument
     * within the range {@code [-pi/2..pi/2]}.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code asin((anything > 1)) = NaN}</li>
     * <li>{@code asin((anything < -1)) = NaN}</li>
     * <li>{@code asin(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value whose arc sine has to be computed.
     * @return the arc sine of the argument.
     */
    public static native double asin(double d) /*-{
      return asin(d);
    }-*/;

    /**
     * Returns the closest double approximation of the arc tangent of the
     * argument within the range {@code [-pi/2..pi/2]}.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code atan(+0.0) = +0.0}</li>
     * <li>{@code atan(-0.0) = -0.0}</li>
     * <li>{@code atan(+infinity) = +pi/2}</li>
     * <li>{@code atan(-infinity) = -pi/2}</li>
     * <li>{@code atan(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value whose arc tangent has to be computed.
     * @return the arc tangent of the argument.
     */
    public static native double atan(double d) /*-{
      return atan(d);
    }-*/;

    /**
     * Returns the closest double approximation of the arc tangent of
     * {@code y/x} within the range {@code [-pi..pi]}. This is the angle of the
     * polar representation of the rectangular coordinates (x,y).
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code atan2((anything), NaN ) = NaN;}</li>
     * <li>{@code atan2(NaN , (anything) ) = NaN;}</li>
     * <li>{@code atan2(+0.0, +(anything but NaN)) = +0.0}</li>
     * <li>{@code atan2(-0.0, +(anything but NaN)) = -0.0}</li>
     * <li>{@code atan2(+0.0, -(anything but NaN)) = +pi}</li>
     * <li>{@code atan2(-0.0, -(anything but NaN)) = -pi}</li>
     * <li>{@code atan2(+(anything but 0 and NaN), 0) = +pi/2}</li>
     * <li>{@code atan2(-(anything but 0 and NaN), 0) = -pi/2}</li>
     * <li>{@code atan2(+(anything but infinity and NaN), +infinity)} {@code =}
     * {@code +0.0}</li>
     * <li>{@code atan2(-(anything but infinity and NaN), +infinity)} {@code =}
     * {@code -0.0}</li>
     * <li>{@code atan2(+(anything but infinity and NaN), -infinity) = +pi}</li>
     * <li>{@code atan2(-(anything but infinity and NaN), -infinity) = -pi}</li>
     * <li>{@code atan2(+infinity, +infinity ) = +pi/4}</li>
     * <li>{@code atan2(-infinity, +infinity ) = -pi/4}</li>
     * <li>{@code atan2(+infinity, -infinity ) = +3pi/4}</li>
     * <li>{@code atan2(-infinity, -infinity ) = -3pi/4}</li>
     * <li>{@code atan2(+infinity, (anything but,0, NaN, and infinity))}
     * {@code =} {@code +pi/2}</li>
     * <li>{@code atan2(-infinity, (anything but,0, NaN, and infinity))}
     * {@code =} {@code -pi/2}</li>
     * </ul>
     *
     * @param y
     *            the numerator of the value whose atan has to be computed.
     * @param x
     *            the denominator of the value whose atan has to be computed.
     * @return the arc tangent of {@code y/x}.
     */
    public static native double atan2(double y, double x) /*-{
      return atan2(x, y);
    }-*/;
    
    /**
     * Returns the closest double approximation of the cube root of the
     * argument.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code cbrt(+0.0) = +0.0}</li>
     * <li>{@code cbrt(-0.0) = -0.0}</li>
     * <li>{@code cbrt(+infinity) = +infinity}</li>
     * <li>{@code cbrt(-infinity) = -infinity}</li>
     * <li>{@code cbrt(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value whose cube root has to be computed.
     * @return the cube root of the argument.
     */
    public static native double cbrt(double d) /*-{
      return cbrt(d);
    }-*/;

    /**
     * Returns the double conversion of the most negative (closest to negative
     * infinity) integer value which is greater than the argument.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code ceil(+0.0) = +0.0}</li>
     * <li>{@code ceil(-0.0) = -0.0}</li>
     * <li>{@code ceil((anything in range (-1,0)) = -0.0}</li>
     * <li>{@code ceil(+infinity) = +infinity}</li>
     * <li>{@code ceil(-infinity) = -infinity}</li>
     * <li>{@code ceil(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value whose closest integer value has to be computed.
     * @return the ceiling of the argument.
     */
    public static native double ceil(double d) /*-{
      return ceil(d);
    }-*/;
    
    
    /**
     * Returns the closest double approximation of the hyperbolic cosine of the
     * argument.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code cosh(+infinity) = +infinity}</li>
     * <li>{@code cosh(-infinity) = +infinity}</li>
     * <li>{@code cosh(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value whose hyperbolic cosine has to be computed.
     * @return the hyperbolic cosine of the argument.
     */
    public static native double cosh(double d) /*-{
      return cosh(d);
    }-*/;

    /**
     * Returns the closest double approximation of the cosine of the argument.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code cos(+infinity) = NaN}</li>
     * <li>{@code cos(-infinity) = NaN}</li>
     * <li>{@code cos(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the angle whose cosine has to be computed, in radians.
     * @return the cosine of the argument.
     */
    public static native double cos(double d) /*-{
      return cos(d);
    }-*/;

    /**
     * Returns the closest double approximation of the raising "e" to the power
     * of the argument.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code exp(+infinity) = +infinity}</li>
     * <li>{@code exp(-infinity) = +0.0}</li>
     * <li>{@code exp(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value whose exponential has to be computed.
     * @return the exponential of the argument.
     */
    public static native double exp(double d) /*-{
      return exp(d);
    }-*/;
    
    /**
     * Returns the closest double approximation of <i>{@code e}</i><sup>
     * {@code d}</sup>{@code - 1}. If the argument is very close to 0, it is
     * much more accurate to use {@code expm1(d)+1} than {@code exp(d)} (due to
     * cancellation of significant digits).
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code expm1(+0.0) = +0.0}</li>
     * <li>{@code expm1(-0.0) = -0.0}</li>
     * <li>{@code expm1(+infinity) = +infinity}</li>
     * <li>{@code expm1(-infinity) = -1.0}</li>
     * <li>{@code expm1(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value to compute the <i>{@code e}</i><sup>{@code d}</sup>
     *            {@code - 1} of.
     * @return the <i>{@code e}</i><sup>{@code d}</sup>{@code - 1} value
     *         of the argument.
     */
    public static native double expm1(double d) /*-{
      return expm1(d);
    }-*/;

    /**
     * Returns the double conversion of the most positive (closest to
     * positive infinity) integer value which is less than the argument.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code floor(+0.0) = +0.0}</li>
     * <li>{@code floor(-0.0) = -0.0}</li>
     * <li>{@code floor(+infinity) = +infinity}</li>
     * <li>{@code floor(-infinity) = -infinity}</li>
     * <li>{@code floor(NaN) = NaN}</li>
     * </ul>
     *
     * @param d the value whose closest integer value has to be computed.
     * @return the floor of the argument.
     */
    public static native double floor(double d) /*-{
      return floor(d);
    }-*/;
    
    /**
     * Returns {@code sqrt(}<i>{@code x}</i><sup>{@code 2}</sup>{@code +}
     * <i> {@code y}</i><sup>{@code 2}</sup>{@code )}. The final result is
     * without medium underflow or overflow.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code hypot(+infinity, (anything including NaN)) = +infinity}</li>
     * <li>{@code hypot(-infinity, (anything including NaN)) = +infinity}</li>
     * <li>{@code hypot((anything including NaN), +infinity) = +infinity}</li>
     * <li>{@code hypot((anything including NaN), -infinity) = +infinity}</li>
     * <li>{@code hypot(NaN, NaN) = NaN}</li>
     * </ul>
     *
     * @param x
     *            a double number.
     * @param y
     *            a double number.
     * @return the {@code sqrt(}<i>{@code x}</i><sup>{@code 2}</sup>{@code +}
     *         <i> {@code y}</i><sup>{@code 2}</sup>{@code )} value of the
     *         arguments.
     */
    public static native double hypot(double x, double y) /*-{
      return hypot(x, y);
    }-*/;

    /**
     * Returns the remainder of dividing {@code x} by {@code y} using the IEEE
     * 754 rules. The result is {@code x-round(x/p)*p} where {@code round(x/p)}
     * is the nearest integer (rounded to even), but without numerical
     * cancellation problems.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code IEEEremainder((anything), 0) = NaN}</li>
     * <li>{@code IEEEremainder(+infinity, (anything)) = NaN}</li>
     * <li>{@code IEEEremainder(-infinity, (anything)) = NaN}</li>
     * <li>{@code IEEEremainder(NaN, (anything)) = NaN}</li>
     * <li>{@code IEEEremainder((anything), NaN) = NaN}</li>
     * <li>{@code IEEEremainder(x, +infinity) = x } where x is anything but
     * +/-infinity</li>
     * <li>{@code IEEEremainder(x, -infinity) = x } where x is anything but
     * +/-infinity</li>
     * </ul>
     *
     * @param x
     *            the numerator of the operation.
     * @param y
     *            the denominator of the operation.
     * @return the IEEE754 floating point reminder of {@code x/y}.
     */
    public static native double IEEEremainder(double x, double y) /*-{
      // According to the Mac OS X math.h online man page, their routines are
      // all IEEE Standard 754 compliant.
      return remainder(x, y);
    }-*/;

    /**
     * Returns the closest double approximation of the natural logarithm of the
     * argument.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code log(+0.0) = -infinity}</li>
     * <li>{@code log(-0.0) = -infinity}</li>
     * <li>{@code log((anything < 0) = NaN}</li>
     * <li>{@code log(+infinity) = +infinity}</li>
     * <li>{@code log(-infinity) = NaN}</li>
     * <li>{@code log(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value whose log has to be computed.
     * @return the natural logarithm of the argument.
     */
    public static native double log(double d) /*-{
      return log(d);
    }-*/;
    
    /**
     * Returns the closest double approximation of the base 10 logarithm of the
     * argument.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code log10(+0.0) = -infinity}</li>
     * <li>{@code log10(-0.0) = -infinity}</li>
     * <li>{@code log10((anything < 0) = NaN}</li>
     * <li>{@code log10(+infinity) = +infinity}</li>
     * <li>{@code log10(-infinity) = NaN}</li>
     * <li>{@code log10(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value whose base 10 log has to be computed.
     * @return the natural logarithm of the argument.
     */
    public static native double log10(double d) /*-{
      return log10(d);
    }-*/;
    
    /**
     * Returns the closest double approximation of the natural logarithm of the
     * sum of the argument and 1. If the argument is very close to 0, it is much
     * more accurate to use {@code log1p(d)} than {@code log(1.0+d)} (due to
     * numerical cancellation).
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code log1p(+0.0) = +0.0}</li>
     * <li>{@code log1p(-0.0) = -0.0}</li>
     * <li>{@code log1p((anything < 1)) = NaN}</li>
     * <li>{@code log1p(-1.0) = -infinity}</li>
     * <li>{@code log1p(+infinity) = +infinity}</li>
     * <li>{@code log1p(-infinity) = NaN}</li>
     * <li>{@code log1p(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value to compute the {@code ln(1+d)} of.
     * @return the natural logarithm of the sum of the argument and 1.
     */
    public static native double log1p(double d) /*-{
      return log1p(d);
    }-*/;

    /**
     * Returns the most positive (closest to positive infinity) of the two
     * arguments.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code max(NaN, (anything)) = NaN}</li>
     * <li>{@code max((anything), NaN) = NaN}</li>
     * <li>{@code max(+0.0, -0.0) = +0.0}</li>
     * <li>{@code max(-0.0, +0.0) = +0.0}</li>
     * </ul>
     *
     * @param d1
     *            the first argument.
     * @param d2
     *            the second argument.
     * @return the larger of {@code d1} and {@code d2}.
     */
    public static native double max(double d1, double d2) /*-{
      return fmax(d1, d2);
    }-*/;

    /**
     * Returns the most positive (closest to positive infinity) of the two
     * arguments.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code max(NaN, (anything)) = NaN}</li>
     * <li>{@code max((anything), NaN) = NaN}</li>
     * <li>{@code max(+0.0, -0.0) = +0.0}</li>
     * <li>{@code max(-0.0, +0.0) = +0.0}</li>
     * </ul>
     *
     * @param f1
     *            the first argument.
     * @param f2
     *            the second argument.
     * @return the larger of {@code f1} and {@code f2}.
     */
    public static native float max(float f1, float f2) /*-{
      return fmaxf(f1, f2);
    }-*/;

    /**
     * Returns the most positive (closest to positive infinity) of the two
     * arguments.
     *
     * @param i1
     *            the first argument.
     * @param i2
     *            the second argument.
     * @return the larger of {@code i1} and {@code i2}.
     */
    public static int max(int i1, int i2) {
        return i1 > i2 ? i1 : i2;
    }

    /**
     * Returns the most positive (closest to positive infinity) of the two
     * arguments.
     *
     * @param l1
     *            the first argument.
     * @param l2
     *            the second argument.
     * @return the larger of {@code l1} and {@code l2}.
     */
    public static long max(long l1, long l2) {
        return l1 > l2 ? l1 : l2;
    }

    /**
     * Returns the most negative (closest to negative infinity) of the two
     * arguments.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code min(NaN, (anything)) = NaN}</li>
     * <li>{@code min((anything), NaN) = NaN}</li>
     * <li>{@code min(+0.0, -0.0) = -0.0}</li>
     * <li>{@code min(-0.0, +0.0) = -0.0}</li>
     * </ul>
     *
     * @param d1
     *            the first argument.
     * @param d2
     *            the second argument.
     * @return the smaller of {@code d1} and {@code d2}.
     */
    public static native double min(double d1, double d2) /*-{
      return fmin(d1, d2);
    }-*/;

    /**
     * Returns the most negative (closest to negative infinity) of the two
     * arguments.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code min(NaN, (anything)) = NaN}</li>
     * <li>{@code min((anything), NaN) = NaN}</li>
     * <li>{@code min(+0.0, -0.0) = -0.0}</li>
     * <li>{@code min(-0.0, +0.0) = -0.0}</li>
     * </ul>
     *
     * @param f1
     *            the first argument.
     * @param f2
     *            the second argument.
     * @return the smaller of {@code f1} and {@code f2}.
     */
    public static native float min(float f1, float f2) /*-{
      return fminf(f1, f2);
    }-*/;

    /**
     * Returns the most negative (closest to negative infinity) of the two
     * arguments.
     *
     * @param i1
     *            the first argument.
     * @param i2
     *            the second argument.
     * @return the smaller of {@code i1} and {@code i2}.
     */
    public static int min(int i1, int i2) {
        return i1 < i2 ? i1 : i2;
    }

    /**
     * Returns the most negative (closest to negative infinity) of the two
     * arguments.
     *
     * @param l1
     *            the first argument.
     * @param l2
     *            the second argument.
     * @return the smaller of {@code l1} and {@code l2}.
     */
    public static long min(long l1, long l2) {
        return l1 < l2 ? l1 : l2;
    }

    /**
     * Returns the closest double approximation of the result of raising
     * {@code x} to the power of {@code y}.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code pow((anything), +0.0) = 1.0}</li>
     * <li>{@code pow((anything), -0.0) = 1.0}</li>
     * <li>{@code pow(x, 1.0) = x}</li>
     * <li>{@code pow((anything), NaN) = NaN}</li>
     * <li>{@code pow(NaN, (anything except 0)) = NaN}</li>
     * <li>{@code pow(+/-(|x| > 1), +infinity) = +infinity}</li>
     * <li>{@code pow(+/-(|x| > 1), -infinity) = +0.0}</li>
     * <li>{@code pow(+/-(|x| < 1), +infinity) = +0.0}</li>
     * <li>{@code pow(+/-(|x| < 1), -infinity) = +infinity}</li>
     * <li>{@code pow(+/-1.0 , +infinity) = NaN}</li>
     * <li>{@code pow(+/-1.0 , -infinity) = NaN}</li>
     * <li>{@code pow(+0.0, (+anything except 0, NaN)) = +0.0}</li>
     * <li>{@code pow(-0.0, (+anything except 0, NaN, odd integer)) = +0.0}</li>
     * <li>{@code pow(+0.0, (-anything except 0, NaN)) = +infinity}</li>
     * <li>{@code pow(-0.0, (-anything except 0, NAN, odd integer))} {@code =}
     * {@code +infinity}</li>
     * <li>{@code pow(-0.0, (odd integer)) = -pow( +0 , (odd integer) )}</li>
     * <li>{@code pow(+infinity, (+anything except 0, NaN)) = +infinity}</li>
     * <li>{@code pow(+infinity, (-anything except 0, NaN)) = +0.0}</li>
     * <li>{@code pow(-infinity, (anything)) = -pow(0, (-anything))}</li>
     * <li>{@code pow((-anything), (integer))} {@code =}
     * {@code pow(-1,(integer))*pow(+anything,integer)}</li>
     * <li>{@code pow((-anything except 0 and infinity), (non-integer))}
     * {@code =} {@code NAN}</li>
     * </ul>
     *
     * @param x
     *            the base of the operation.
     * @param y
     *            the exponent of the operation.
     * @return {@code x} to the power of {@code y}.
     */
    public static native double pow(double x, double y) /*-{
      return pow(x, y);
    }-*/;

    /**
     * Returns a pseudo-random number between 0.0 (inclusive) and 1.0
     * (exclusive).
     *
     * @return a pseudo-random number.
     */
    public static double random() {
        if (random_ == null)
            random_ = new Random();
        return random_.nextDouble();
    }

    /**
     * Returns the double conversion of the result of rounding the argument to
     * an integer. Tie breaks are rounded towards even.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code rint(+0.0) = +0.0}</li>
     * <li>{@code rint(-0.0) = -0.0}</li>
     * <li>{@code rint(+infinity) = +infinity}</li>
     * <li>{@code rint(-infinity) = -infinity}</li>
     * <li>{@code rint(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value to be rounded.
     * @return the closest integer to the argument (as a double).
     */
    public static native double rint(double d) /*-{
      return rint(d);
    }-*/;

    /**
     * Returns the result of rounding the argument to an integer. The result is
     * equivalent to {@code (long) Math.floor(d+0.5)}.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code round(+0.0) = +0.0}</li>
     * <li>{@code round(-0.0) = +0.0}</li>
     * <li>{@code round((anything > Long.MAX_VALUE) = Long.MAX_VALUE}</li>
     * <li>{@code round((anything < Long.MIN_VALUE) = Long.MIN_VALUE}</li>
     * <li>{@code round(+infinity) = Long.MAX_VALUE}</li>
     * <li>{@code round(-infinity) = Long.MIN_VALUE}</li>
     * <li>{@code round(NaN) = +0.0}</li>
     * </ul>
     *
     * @param d
     *            the value to be rounded.
     * @return the closest integer to the argument.
     */
    public static long round(double d) {
        // check for NaN
        if (d != d)
            return 0L;
        return (long) Math.floor(d + 0.5d);
    }

    /**
     * Returns the result of rounding the argument to an integer. The result is
     * equivalent to {@code (int) Math.floor(f+0.5)}.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code round(+0.0) = +0.0}</li>
     * <li>{@code round(-0.0) = +0.0}</li>
     * <li>{@code round((anything > Integer.MAX_VALUE) = Integer.MAX_VALUE}</li>
     * <li>{@code round((anything < Integer.MIN_VALUE) = Integer.MIN_VALUE}</li>
     * <li>{@code round(+infinity) = Integer.MAX_VALUE}</li>
     * <li>{@code round(-infinity) = Integer.MIN_VALUE}</li>
     * <li>{@code round(NaN) = +0.0}</li>
     * </ul>
     *
     * @param f
     *            the value to be rounded.
     * @return the closest integer to the argument.
     */
    public static int round(float f) {
        // check for NaN
        if (f != f)
            return 0;
        return (int) Math.floor(f + 0.5f);
    }
    
    /**
     * Returns the signum function of the argument. If the argument is less than
     * zero, it returns -1.0. If the argument is greater than zero, 1.0 is
     * returned. If the argument is either positive or negative zero, the
     * argument is returned as result.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code signum(+0.0) = +0.0}</li>
     * <li>{@code signum(-0.0) = -0.0}</li>
     * <li>{@code signum(+infinity) = +1.0}</li>
     * <li>{@code signum(-infinity) = -1.0}</li>
     * <li>{@code signum(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value whose signum has to be computed.
     * @return the value of the signum function.
     */
    public static double signum(double d){
        if(Double.isNaN(d)){
            return Double.NaN;
        }
        double sig = d;
        if(d > 0){
            sig = 1.0;
        }else if (d < 0){
            sig = -1.0;
        }
        return sig;
    }
    
    /**
     * Returns the signum function of the argument. If the argument is less than
     * zero, it returns -1.0. If the argument is greater than zero, 1.0 is
     * returned. If the argument is either positive or negative zero, the
     * argument is returned as result.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code signum(+0.0) = +0.0}</li>
     * <li>{@code signum(-0.0) = -0.0}</li>
     * <li>{@code signum(+infinity) = +1.0}</li>
     * <li>{@code signum(-infinity) = -1.0}</li>
     * <li>{@code signum(NaN) = NaN}</li>
     * </ul>
     *
     * @param f
     *            the value whose signum has to be computed.
     * @return the value of the signum function.
     */
    public static float signum(float f){
        if(Float.isNaN(f)){
            return Float.NaN;
        }
        float sig = f;
        if(f > 0){
            sig = 1.0f;
        }else if (f < 0){
            sig = -1.0f;
        }
        return sig;
    }

    /**
     * Returns the closest double approximation of the hyperbolic sine of the
     * argument.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code sinh(+0.0) = +0.0}</li>
     * <li>{@code sinh(-0.0) = -0.0}</li>
     * <li>{@code sinh(+infinity) = +infinity}</li>
     * <li>{@code sinh(-infinity) = -infinity}</li>
     * <li>{@code sinh(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value whose hyperbolic sine has to be computed.
     * @return the hyperbolic sine of the argument.
     */
    public static native double sinh(double d) /*-{
      return sinh(d);
    }-*/;
    
    /**
     * Returns the closest double approximation of the sine of the argument.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code sin(+0.0) = +0.0}</li>
     * <li>{@code sin(-0.0) = -0.0}</li>
     * <li>{@code sin(+infinity) = NaN}</li>
     * <li>{@code sin(-infinity) = NaN}</li>
     * <li>{@code sin(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the angle whose sin has to be computed, in radians.
     * @return the sine of the argument.
     */
    public static native double sin(double d) /*-{
      return sin(d);
    }-*/;

    /**
     * Returns the closest double approximation of the square root of the
     * argument.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code sqrt(+0.0) = +0.0}</li>
     * <li>{@code sqrt(-0.0) = -0.0}</li>
     * <li>{@code sqrt( (anything < 0) ) = NaN}</li>
     * <li>{@code sqrt(+infinity) = +infinity}</li>
     * <li>{@code sqrt(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value whose square root has to be computed.
     * @return the square root of the argument.
     */
    public static native double sqrt(double d) /*-{
      return sqrt(d);
    }-*/;

    /**
     * Returns the closest double approximation of the tangent of the argument.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code tan(+0.0) = +0.0}</li>
     * <li>{@code tan(-0.0) = -0.0}</li>
     * <li>{@code tan(+infinity) = NaN}</li>
     * <li>{@code tan(-infinity) = NaN}</li>
     * <li>{@code tan(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the angle whose tangens has to be computed, in radians.
     * @return the tangent of the argument.
     */
    public static native double tan(double d) /*-{
      return tan(d);
    }-*/;

    /**
     * Returns the closest double approximation of the hyperbolic tangent of the
     * argument. The absolute value is always less than 1.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code tanh(+0.0) = +0.0}</li>
     * <li>{@code tanh(-0.0) = -0.0}</li>
     * <li>{@code tanh(+infinity) = +1.0}</li>
     * <li>{@code tanh(-infinity) = -1.0}</li>
     * <li>{@code tanh(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the value whose hyperbolic tangent has to be computed.
     * @return the hyperbolic tangent of the argument
     */
    public static native double tanh(double d) /*-{
      return tanh(d);
    }-*/;
    
    /**
     * Returns the measure in degrees of the supplied radian angle. The result
     * is {@code angrad * 180 / pi}.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code toDegrees(+0.0) = +0.0}</li>
     * <li>{@code toDegrees(-0.0) = -0.0}</li>
     * <li>{@code toDegrees(+infinity) = +infinity}</li>
     * <li>{@code toDegrees(-infinity) = -infinity}</li>
     * <li>{@code toDegrees(NaN) = NaN}</li>
     * </ul>
     *
     * @param angrad
     *            an angle in radians.
     * @return the degree measure of the angle.
     */
    public static double toDegrees(double angrad) {
        return angrad * 180d / PI;
    }

    /**
     * Returns the measure in radians of the supplied degree angle. The result
     * is {@code angdeg / 180 * pi}.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code toRadians(+0.0) = +0.0}</li>
     * <li>{@code toRadians(-0.0) = -0.0}</li>
     * <li>{@code toRadians(+infinity) = +infinity}</li>
     * <li>{@code toRadians(-infinity) = -infinity}</li>
     * <li>{@code toRadians(NaN) = NaN}</li>
     * </ul>
     *
     * @param angdeg
     *            an angle in degrees.
     * @return the radian measure of the angle.
     */
    public static double toRadians(double angdeg) {
        return angdeg / 180d * PI;
    }
    
    /**
     * Returns the argument's ulp (unit in the last place). The size of a ulp of
     * a double value is the positive distance between this value and the double
     * value next larger in magnitude. For non-NaN {@code x},
     * {@code ulp(-x) == ulp(x)}.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code ulp(+0.0) = Double.MIN_VALUE}</li>
     * <li>{@code ulp(-0.0) = Double.MIN_VALUE}</li>
     * <li>{@code ulp(+infintiy) = infinity}</li>
     * <li>{@code ulp(-infintiy) = infinity}</li>
     * <li>{@code ulp(NaN) = NaN}</li>
     * </ul>
     *
     * @param d
     *            the floating-point value to compute ulp of.
     * @return the size of a ulp of the argument.
     */
    public static double ulp(double d) {
        // special cases
        if (Double.isInfinite(d)) {
            return Double.POSITIVE_INFINITY;
        } else if (d == Double.MAX_VALUE || d == -Double.MAX_VALUE) {
            return pow(2, 971);
        }
        d = Math.abs(d);
        return nextafter(d, Double.MAX_VALUE) - d;
    }

    /**
     * Returns the argument's ulp (unit in the last place). The size of a ulp of
     * a float value is the positive distance between this value and the float
     * value next larger in magnitude. For non-NaN {@code x},
     * {@code ulp(-x) == ulp(x)}.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code ulp(+0.0) = Float.MIN_VALUE}</li>
     * <li>{@code ulp(-0.0) = Float.MIN_VALUE}</li>
     * <li>{@code ulp(+infintiy) = infinity}</li>
     * <li>{@code ulp(-infintiy) = infinity}</li>
     * <li>{@code ulp(NaN) = NaN}</li>
     * </ul>
     *
     * @param f
     *            the floating-point value to compute ulp of.
     * @return the size of a ulp of the argument.
     */
    public static float ulp(float f) {
        // special cases
        if (Float.isNaN(f)) {
            return Float.NaN;
        } else if (Float.isInfinite(f)) {
            return Float.POSITIVE_INFINITY;
        } else if (f == Float.MAX_VALUE || f == -Float.MAX_VALUE) {
            return (float) pow(2, 104);
        }
        f = Math.abs(f);
        return nextafterf(f, Float.MAX_VALUE) - f;
    }

    private native static double nextafter(double x, double y) /*-{
      return nextafter(x, y);
    }-*/;

    private native static float nextafterf(float x, float y) /*-{
      return nextafterf(x, y);
    }-*/;
}
