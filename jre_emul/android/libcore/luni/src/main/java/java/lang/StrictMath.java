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

/*
 * acos, asin, atan, cosh, sinh, tanh, exp, expm1, log, log10, log1p, and cbrt
 * have been implemented with the following license.
 * ====================================================
 * Copyright (C) 2004 by Sun Microsystems, Inc. All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this
 * software is freely granted, provided that this notice
 * is preserved.
 * ====================================================
 */

package java.lang;

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
    public static final double E = Math.E;

    /**
     * The double value closest to pi, the ratio of a circle's circumference to
     * its diameter.
     */
    public static final double PI = Math.PI;

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
     */
    public static double abs(double d) {
        return Math.abs(d);
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
     */
    public static float abs(float f) {
        return Math.abs(f);
    }

    /**
     * Returns the absolute value of the argument.
     * <p>
     * If the argument is {@code Integer.MIN_VALUE}, {@code Integer.MIN_VALUE}
     * is returned.
     */
    public static int abs(int i) {
        return Math.abs(i);
    }

    /**
     * Returns the absolute value of the argument.
     * <p>
     * If the argument is {@code Long.MIN_VALUE}, {@code Long.MIN_VALUE} is
     * returned.
     */
    public static long abs(long l) {
        return Math.abs(l);
    }

    private static final double PIO2_HI = 1.57079632679489655800e+00;
    private static final double PIO2_LO = 6.12323399573676603587e-17;
    private static final double PS0 = 1.66666666666666657415e-01;
    private static final double PS1 = -3.25565818622400915405e-01;
    private static final double PS2 = 2.01212532134862925881e-01;
    private static final double PS3 = -4.00555345006794114027e-02;
    private static final double PS4 = 7.91534994289814532176e-04;
    private static final double PS5 = 3.47933107596021167570e-05;
    private static final double QS1 = -2.40339491173441421878e+00;
    private static final double QS2 = 2.02094576023350569471e+00;
    private static final double QS3 = -6.88283971605453293030e-01;
    private static final double QS4 = 7.70381505559019352791e-02;
    private static final double HUGE = 1.000e+300;
    private static final double PIO4_HI = 7.85398163397448278999e-01;

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
     * @param x
     *            the value to compute arc cosine of.
     * @return the arc cosine of the argument.
     */
    public static double acos(double x) {
        double z, p, q, r, w, s, c, df;
        int hx, ix;
        final long bits = Double.doubleToRawLongBits(x);
        hx = (int) (bits >>> 32);
        ix = hx & 0x7fffffff;
        if (ix >= 0x3ff00000) { /* |x| >= 1 */
            if ((((ix - 0x3ff00000) | ((int) bits))) == 0) { /* |x|==1 */
                if (hx > 0) {
                    return 0.0; /* ieee_acos(1) = 0 */
                } else {
                    return 3.14159265358979311600e+00 + 2.0 * PIO2_LO; /* ieee_acos(-1)= pi */
                }
            }
            return (x - x) / (x - x); /* ieee_acos(|x|>1) is NaN */
        }

        if (ix < 0x3fe00000) { /* |x| < 0.5 */
            if (ix <= 0x3c600000) {
                return PIO2_HI + PIO2_LO;/* if|x|<2**-57 */
            }

            z = x * x;
            p = z * (PS0 + z
                    * (PS1 + z * (PS2 + z * (PS3 + z * (PS4 + z * PS5)))));
            q = 1.00000000000000000000e+00 + z * (QS1 + z * (QS2 + z * (QS3 + z * QS4)));
            r = p / q;
            return PIO2_HI - (x - (PIO2_LO - x * r));
        } else if (hx < 0) { /* x < -0.5 */
            z = (1.00000000000000000000e+00 + x) * 0.5;
            p = z * (PS0 + z
                    * (PS1 + z * (PS2 + z * (PS3 + z * (PS4 + z * PS5)))));
            q = 1.00000000000000000000e+00 + z * (QS1 + z * (QS2 + z * (QS3 + z * QS4)));
            s = StrictMath.sqrt(z);
            r = p / q;
            w = r * s - PIO2_LO;
            return 3.14159265358979311600e+00 - 2.0 * (s + w);
        } else { /* x > 0.5 */
            z = (1.00000000000000000000e+00 - x) * 0.5;
            s = StrictMath.sqrt(z);
            df = s;
            df = Double.longBitsToDouble(
                    Double.doubleToRawLongBits(df) & 0xffffffffL << 32);
            c = (z - df * df) / (s + df);
            p = z * (PS0 + z
                    * (PS1 + z * (PS2 + z * (PS3 + z * (PS4 + z * PS5)))));
            q = 1.00000000000000000000e+00 + z * (QS1 + z * (QS2 + z * (QS3 + z * QS4)));
            r = p / q;
            w = r * s + c;
            return 2.0 * (df + w);
        }
    }

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
     * @param x
     *            the value whose arc sine has to be computed.
     * @return the arc sine of the argument.
     */
    public static double asin(double x) {
        double t, w, p, q, c, r, s;
        int hx, ix;
        final long bits = Double.doubleToRawLongBits(x);
        hx = (int) (bits >>> 32);
        ix = hx & 0x7fffffff;
        if (ix >= 0x3ff00000) { /* |x|>= 1 */
            if ((((ix - 0x3ff00000) | ((int) bits))) == 0) {
                /* ieee_asin(1)=+-pi/2 with inexact */
                return x * PIO2_HI + x * PIO2_LO;
            }
            return (x - x) / (x - x); /* ieee_asin(|x|>1) is NaN */
        } else if (ix < 0x3fe00000) { /* |x|<0.5 */
            if (ix < 0x3e400000) { /* if |x| < 2**-27 */
                if (HUGE + x > 1.00000000000000000000e+00) {
                    return x;/* return x with inexact if x!=0 */
                }
            } else {
                t = x * x;
                p = t * (PS0 + t
                        * (PS1 + t * (PS2 + t * (PS3 + t * (PS4 + t * PS5)))));
                q = 1.00000000000000000000e+00 + t * (QS1 + t * (QS2 + t * (QS3 + t * QS4)));
                w = p / q;
                return x + x * w;
            }
        }
        /* 1> |x|>= 0.5 */
        w = 1.00000000000000000000e+00 - Math.abs(x);
        t = w * 0.5;
        p = t * (PS0 + t * (PS1 + t * (PS2 + t * (PS3 + t * (PS4 + t * PS5)))));
        q = 1.00000000000000000000e+00 + t * (QS1 + t * (QS2 + t * (QS3 + t * QS4)));
        s = StrictMath.sqrt(t);
        if (ix >= 0x3FEF3333) { /* if |x| > 0.975 */
            w = p / q;
            t = PIO2_HI - (2.0 * (s + s * w) - PIO2_LO);
        } else {
            w = s;
            w = Double.longBitsToDouble(
                    Double.doubleToRawLongBits(w) & 0xffffffffL << 32);
            c = (t - w * w) / (s + w);
            r = p / q;
            p = 2.0 * s * r - (PIO2_LO - 2.0 * c);
            q = PIO4_HI - 2.0 * w;
            t = PIO4_HI - (p - q);
        }
        return (hx > 0) ? t : -t;
    }

    private static final double[] ATANHI = { 4.63647609000806093515e-01,
            7.85398163397448278999e-01, 9.82793723247329054082e-01,
            1.57079632679489655800e+00 };
    private static final double[] ATANLO = { 2.26987774529616870924e-17,
            3.06161699786838301793e-17, 1.39033110312309984516e-17,
            6.12323399573676603587e-17 };
    private static final double AT0 = 3.33333333333329318027e-01;
    private static final double AT1 = -1.99999999998764832476e-01;
    private static final double AT2 = 1.42857142725034663711e-01;
    private static final double AT3 = -1.11111104054623557880e-01;
    private static final double AT4 = 9.09088713343650656196e-02;
    private static final double AT5 = -7.69187620504482999495e-02;
    private static final double AT6 = 6.66107313738753120669e-02;
    private static final double AT7= -5.83357013379057348645e-02;
    private static final double AT8 = 4.97687799461593236017e-02;
    private static final double AT9 = -3.65315727442169155270e-02;
    private static final double AT10 = 1.62858201153657823623e-02;

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
     * @param x
     *            the value whose arc tangent has to be computed.
     * @return the arc tangent of the argument.
     */
    public static double atan(double x) {
       double w, s1, s2, z;
        int ix, hx, id;

        final long bits = Double.doubleToRawLongBits(x);
        hx = (int) (bits >>> 32);
        ix = hx & 0x7fffffff;
        if (ix >= 0x44100000) { /* if |x| >= 2^66 */
            if (ix > 0x7ff00000 || (ix == 0x7ff00000 && (((int) bits) != 0))) {
                return x + x; /* NaN */
            }
            if (hx > 0) {
                return ATANHI[3] + ATANLO[3];
            } else {
                return -ATANHI[3] - ATANLO[3];
            }
        }
        if (ix < 0x3fdc0000) { /* |x| < 0.4375 */
            if (ix < 0x3e200000) { /* |x| < 2^-29 */
                if (HUGE + x > 1.00000000000000000000e+00) {
                    return x; /* raise inexact */
                }
            }
            id = -1;
        } else {
            x = Math.abs(x);
            if (ix < 0x3ff30000) { /* |x| < 1.1875 */
                if (ix < 0x3fe60000) { /* 7/16 <=|x|<11/16 */
                    id = 0;
                    x = (2.0 * x - 1.00000000000000000000e+00) / (2.0 + x);
                } else { /* 11/16<=|x|< 19/16 */
                    id = 1;
                    x = (x - 1.00000000000000000000e+00) / (x + 1.00000000000000000000e+00);
                }
            } else {
                if (ix < 0x40038000) { /* |x| < 2.4375 */
                    id = 2;
                    x = (x - 1.5) / (1.00000000000000000000e+00 + 1.5 * x);
                } else { /* 2.4375 <= |x| < 2^66 */
                    id = 3;
                    x = -1.0 / x;
                }
            }
        }

        /* end of argument reduction */
        z = x * x;
        w = z * z;
        /* break sum from i=0 to 10 aT[i]z**(i+1) into odd and even poly */
        s1 = z * (AT0 + w * (AT2 + w
                * (AT4 + w * (AT6 + w * (AT8 + w * AT10)))));
        s2 = w * (AT1 + w * (AT3 + w * (AT5 + w * (AT7 + w * AT9))));
        if (id < 0) {
            return x - x * (s1 + s2);
        } else {
            z = ATANHI[id] - ((x * (s1 + s2) - ATANLO[id]) - x);
            return (hx < 0) ? -z : z;
        }
    }

    private static final double PI_O_4 = 7.8539816339744827900E-01;
    private static final double PI_O_2 = 1.5707963267948965580E+00;
    private static final double PI_LO = 1.2246467991473531772E-16;

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
    public static double atan2(double y, double x) {
        double z;
        int k, m, hx, hy, ix, iy;
        int lx, ly; // watch out, should be unsigned

        final long yBits = Double.doubleToRawLongBits(y);
        final long xBits = Double.doubleToRawLongBits(x);

        hx = (int) (xBits >>> 32); // __HI(x);
        ix = hx & 0x7fffffff;
        lx = (int) xBits; // __LO(x);
        hy = (int) (yBits >>> 32); // __HI(y);
        iy = hy & 0x7fffffff;
        ly = (int) yBits; // __LO(y);
        if (((ix | ((lx | -lx) >> 31)) > 0x7ff00000)
                || ((iy | ((ly | -ly) >> 31)) > 0x7ff00000)) { /* x or y is NaN */
            return x + y;
        }
        if ((hx - 0x3ff00000 | lx) == 0) {
            return StrictMath.atan(y); /* x=1.0 */
        }

        m = ((hy >> 31) & 1) | ((hx >> 30) & 2); /* 2*sign(x)+sign(y) */

        /* when y = 0 */
        if ((iy | ly) == 0) {
            switch (m) {
                case 0:
                case 1:
                    return y; /* ieee_atan(+-0,+anything)=+-0 */
                case 2:
                    return 3.14159265358979311600e+00 + TINY;/* ieee_atan(+0,-anything) = pi */
                case 3:
                    return -3.14159265358979311600e+00 - TINY;/* ieee_atan(-0,-anything) =-pi */
            }
        }
        /* when x = 0 */
        if ((ix | lx) == 0)
            return (hy < 0) ? -PI_O_2 - TINY : PI_O_2 + TINY;

        /* when x is INF */
        if (ix == 0x7ff00000) {
            if (iy == 0x7ff00000) {
                switch (m) {
                    case 0:
                        return PI_O_4 + TINY;/* ieee_atan(+INF,+INF) */
                    case 1:
                        return -PI_O_4 - TINY;/* ieee_atan(-INF,+INF) */
                    case 2:
                        return 3.0 * PI_O_4 + TINY;/* ieee_atan(+INF,-INF) */
                    case 3:
                        return -3.0 * PI_O_4 - TINY;/* ieee_atan(-INF,-INF) */
                }
            } else {
                switch (m) {
                    case 0:
                        return 0.0; /* ieee_atan(+...,+INF) */
                    case 1:
                        return -0.0; /* ieee_atan(-...,+INF) */
                    case 2:
                        return 3.14159265358979311600e+00 + TINY; /* ieee_atan(+...,-INF) */
                    case 3:
                        return -3.14159265358979311600e+00 - TINY; /* ieee_atan(-...,-INF) */
                }
            }
        }
        /* when y is INF */
        if (iy == 0x7ff00000)
            return (hy < 0) ? -PI_O_2 - TINY : PI_O_2 + TINY;

        /* compute y/x */
        k = (iy - ix) >> 20;
        if (k > 60) {
            z = PI_O_2 + 0.5 * PI_LO; /* |y/x| > 2**60 */
        } else if (hx < 0 && k < -60) {
            z = 0.0; /* |y|/x < -2**60 */
        } else {
            z = StrictMath.atan(Math.abs(y / x)); /* safe to do y/x */
        }

        switch (m) {
            case 0:
                return z; /* ieee_atan(+,+) */
            case 1:
                // __HI(z) ^= 0x80000000;
                z = Double.longBitsToDouble(
                        Double.doubleToRawLongBits(z) ^ (0x80000000L << 32));
                return z; /* ieee_atan(-,+) */
            case 2:
                return 3.14159265358979311600e+00 - (z - PI_LO);/* ieee_atan(+,-) */
            default: /* case 3 */
                return (z - PI_LO) - 3.14159265358979311600e+00;/* ieee_atan(-,-) */
        }
    }

    private static final int B1 = 715094163;
    private static final int B2 = 696219795;
    private static final double C = 5.42857142857142815906e-01;
    private static final double D = -7.05306122448979611050e-01;
    private static final double CBRTE = 1.41428571428571436819e+00;
    private static final double F = 1.60714285714285720630e+00;
    private static final double G = 3.57142857142857150787e-01;

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
     * @param x
     *            the value whose cube root has to be computed.
     * @return the cube root of the argument.
     */
    public static double cbrt(double x) {
        if (x < 0) {
            return -cbrt(-x);
        }
        int hx;
        double r, s, w;
        int sign; // caution: should be unsigned
        long bits = Double.doubleToRawLongBits(x);

        hx = (int) (bits >>> 32);
        sign = hx & 0x80000000; /* sign= sign(x) */
        hx ^= sign;
        if (hx >= 0x7ff00000) {
            return (x + x); /* ieee_cbrt(NaN,INF) is itself */
        }

        if ((hx | ((int) bits)) == 0) {
            return x; /* ieee_cbrt(0) is itself */
        }

        // __HI(x) = hx; /* x <- |x| */
        bits &= 0x00000000ffffffffL;
        bits |= ((long) hx << 32);

        long tBits = Double.doubleToRawLongBits(0.0) & 0x00000000ffffffffL;
        double t = 0.0;
        /* rough cbrt to 5 bits */
        if (hx < 0x00100000) { /* subnormal number */
            // __HI(t)=0x43500000; /*set t= 2**54*/
            tBits |= 0x43500000L << 32;
            t = Double.longBitsToDouble(tBits);
            t *= x;

            // __HI(t)=__HI(t)/3+B2;
            tBits = Double.doubleToRawLongBits(t);
            long tBitsHigh = tBits >> 32;
            tBits &= 0x00000000ffffffffL;
            tBits |= ((tBitsHigh / 3) + B2) << 32;
            t = Double.longBitsToDouble(tBits);

        } else {
            // __HI(t)=hx/3+B1;
            tBits |= ((long) ((hx / 3) + B1)) << 32;
            t = Double.longBitsToDouble(tBits);
        }

        /* new cbrt to 23 bits, may be implemented in single precision */
        r = t * t / x;
        s = C + r * t;
        t *= G + F / (s + CBRTE + D / s);

        /* chopped to 20 bits and make it larger than ieee_cbrt(x) */
        tBits = Double.doubleToRawLongBits(t);
        tBits &= 0xFFFFFFFFL << 32;
        tBits += 0x00000001L << 32;
        t = Double.longBitsToDouble(tBits);

        /* one step newton iteration to 53 bits with error less than 0.667 ulps */
        s = t * t; /* t*t is exact */
        r = x / s;
        w = t + t;
        r = (r - t) / (w + r); /* r-s is exact */
        t = t + t * r;

        /* retore the sign bit */
        tBits = Double.doubleToRawLongBits(t);
        tBits |= ((long) sign) << 32;
        return Double.longBitsToDouble(tBits);
    }

    /**
     * Returns the double conversion of the most negative (closest to negative
     * infinity) integer value greater than or equal to the argument.
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
     */
    public static native double ceil(double d) /*-[
      return ceil(d);
    ]-*/;

    private static final long ONEBITS = Double.doubleToRawLongBits(1.00000000000000000000e+00)
            & 0x00000000ffffffffL;

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
     * @param x
     *            the value whose hyperbolic cosine has to be computed.
     * @return the hyperbolic cosine of the argument.
     */
    public static double cosh(double x) {
        double t, w;
        int ix;
        final long bits = Double.doubleToRawLongBits(x);
        ix = (int) (bits >>> 32) & 0x7fffffff;

        /* x is INF or NaN */
        if (ix >= 0x7ff00000) {
            return x * x;
        }

        /* |x| in [0,0.5*ln2], return 1+ieee_expm1(|x|)^2/(2*ieee_exp(|x|)) */
        if (ix < 0x3fd62e43) {
            t = expm1(Math.abs(x));
            w = 1.00000000000000000000e+00 + t;
            if (ix < 0x3c800000)
                return w; /* ieee_cosh(tiny) = 1 */
            return 1.00000000000000000000e+00 + (t * t) / (w + w);
        }

        /* |x| in [0.5*ln2,22], return (ieee_exp(|x|)+1/ieee_exp(|x|)/2; */
        if (ix < 0x40360000) {
            t = exp(Math.abs(x));
            return 0.5 * t + 0.5 / t;
        }

        /* |x| in [22, ieee_log(maxdouble)] return half*ieee_exp(|x|) */
        if (ix < 0x40862E42) {
            return 0.5 * exp(Math.abs(x));
        }

        /* |x| in [log(maxdouble), overflowthresold] */
        final long lx = ((ONEBITS >>> 29) + ((int) bits)) & 0x00000000ffffffffL;
        // watch out: lx should be an unsigned int
        // lx = *( (((*(unsigned*)&one)>>29)) + (unsigned*)&x);
        if (ix < 0x408633CE || (ix == 0x408633ce) && (lx <= 0x8fb9f87dL)) {
            w = exp(0.5 * Math.abs(x));
            t = 0.5 * w;
            return t * w;
        }

        /* |x| > overflowthresold, ieee_cosh(x) overflow */
        return HUGE * HUGE;
    }

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
    public static native double cos(double d) /*-[
      return cos(d);
    ]-*/;

    private static final double TWON24 = 5.96046447753906250000e-08;
    private static final double TWO54 = 1.80143985094819840000e+16,
            TWOM54 = 5.55111512312578270212e-17;
    private static final double TWOM1000 = 9.33263618503218878990e-302;
    private static final double O_THRESHOLD = 7.09782712893383973096e+02;
    private static final double U_THRESHOLD = -7.45133219101941108420e+02;
    private static final double INVLN2 = 1.44269504088896338700e+00;
    private static final double P1 = 1.66666666666666019037e-01;
    private static final double P2 = -2.77777777770155933842e-03;
    private static final double P3 = 6.61375632143793436117e-05;
    private static final double P4 = -1.65339022054652515390e-06;
    private static final double P5 = 4.13813679705723846039e-08;

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
     * @param x
     *            the value whose exponential has to be computed.
     * @return the exponential of the argument.
     */
    public static double exp(double x) {
        double y, c, t;
        double hi = 0, lo = 0;
        int k = 0, xsb;
        int hx; // should be unsigned, be careful!
        final long bits = Double.doubleToRawLongBits(x);
        int lowBits = (int) bits;
        int highBits = (int) (bits >>> 32);
        hx = highBits & 0x7fffffff;
        xsb = (highBits >>> 31) & 1;

        /* filter out non-finite argument */
        if (hx >= 0x40862E42) { /* if |x|>=709.78... */
            if (hx >= 0x7ff00000) {
                if (((hx & 0xfffff) | lowBits) != 0) {
                    return x + x; /* NaN */
                } else {
                    return (xsb == 0) ? x : 0.0; /* ieee_exp(+-inf)={inf,0} */
                }
            }

            if (x > O_THRESHOLD) {
                return HUGE * HUGE; /* overflow */
            }

            if (x < U_THRESHOLD) {
                return TWOM1000 * TWOM1000; /* underflow */
            }
        }

        /* argument reduction */
        if (hx > 0x3fd62e42) { /* if |x| > 0.5 ln2 */
            if (hx < 0x3FF0A2B2) { /* and |x| < 1.5 ln2 */
                hi = x - ((xsb == 0) ? 6.93147180369123816490e-01 :
                        -6.93147180369123816490e-01); // LN2HI[xsb];
                lo = (xsb == 0) ? 1.90821492927058770002e-10 :
                        -1.90821492927058770002e-10; // LN2LO[xsb];
                k = 1 - xsb - xsb;
            } else {
                k = (int) (INVLN2 * x + ((xsb == 0) ? 0.5 : -0.5 ));//halF[xsb]);
                t = k;
                hi = x - t * 6.93147180369123816490e-01; //ln2HI[0]; /* t*ln2HI is exact here */
                lo = t * 1.90821492927058770002e-10; //ln2LO[0];
            }
            x = hi - lo;
        } else if (hx < 0x3e300000) { /* when |x|<2**-28 */
            if (HUGE + x > 1.00000000000000000000e+00)
                return 1.00000000000000000000e+00 + x;/* trigger inexact */
        } else {
            k = 0;
        }

        /* x is now in primary range */
        t = x * x;
        c = x - t * (P1 + t * (P2 + t * (P3 + t * (P4 + t * P5))));
        if (k == 0) {
            return 1.00000000000000000000e+00 - ((x * c) / (c - 2.0) - x);
        } else {
            y = 1.00000000000000000000e+00 - ((lo - (x * c) / (2.0 - c)) - hi);
        }
        long yBits = Double.doubleToRawLongBits(y);
        if (k >= -1021) {
            yBits += ((long) (k << 20)) << 32; /* add k to y's exponent */
            return Double.longBitsToDouble(yBits);
        } else {
            yBits += ((long) ((k + 1000) << 20)) << 32;/* add k to y's exponent */
            return Double.longBitsToDouble(yBits) * TWOM1000;
        }
    }

    private static final double TINY = 1.0e-300;
    private static final double LN2_HI = 6.93147180369123816490e-01;
    private static final double LN2_LO = 1.90821492927058770002e-10;
    private static final double Q1 = -3.33333333333331316428e-02;
    private static final double Q2 = 1.58730158725481460165e-03;
    private static final double Q3 = -7.93650757867487942473e-05;
    private static final double Q4 = 4.00821782732936239552e-06;
    private static final double Q5 = -2.01099218183624371326e-07;

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
     * @param x
     *            the value to compute the <i>{@code e}</i><sup>{@code d}</sup>
     *            {@code - 1} of.
     * @return the <i>{@code e}</i><sup>{@code d}</sup>{@code - 1} value of the
     *         argument.
     */
    public static double expm1(double x) {
        double y, hi, lo, t, e, hxs, hfx, r1, c = 0.0;
        int k, xsb;
        long yBits = 0;
        final long bits = Double.doubleToRawLongBits(x);
        int highBits = (int) (bits >>> 32);
        int lowBits = (int) (bits);
        int hx = highBits & 0x7fffffff; // caution: should be unsigned!
        xsb = highBits & 0x80000000; /* sign bit of x */
        y = xsb == 0 ? x : -x; /* y = |x| */

        /* filter out huge and non-finite argument */
        if (hx >= 0x4043687A) { /* if |x|>=56*ln2 */
            if (hx >= 0x40862E42) { /* if |x|>=709.78... */
                if (hx >= 0x7ff00000) {
                    if (((hx & 0xfffff) | lowBits) != 0) {
                        return x + x; /* NaN */
                    } else {
                        return (xsb == 0) ? x : -1.0;/* ieee_exp(+-inf)={inf,-1} */
                    }
                }
                if (x > O_THRESHOLD) {
                    return HUGE * HUGE; /* overflow */
                }
            }
            if (xsb != 0) { /* x < -56*ln2, return -1.0 with inexact */
                if (x + TINY < 0.0) { /* raise inexact */
                    return TINY - 1.00000000000000000000e+00; /* return -1 */
                }
            }
        }
        /* argument reduction */
        if (hx > 0x3fd62e42) { /* if |x| > 0.5 ln2 */
            if (hx < 0x3FF0A2B2) { /* and |x| < 1.5 ln2 */
                if (xsb == 0) {
                    hi = x - LN2_HI;
                    lo = LN2_LO;
                    k = 1;
                } else {
                    hi = x + LN2_HI;
                    lo = -LN2_LO;
                    k = -1;
                }
            } else {
                k = (int) (INVLN2 * x + ((xsb == 0) ? 0.5 : -0.5));
                t = k;
                hi = x - t * LN2_HI; /* t*ln2_hi is exact here */
                lo = t * LN2_LO;
            }
            x = hi - lo;
            c = (hi - x) - lo;
        } else if (hx < 0x3c900000) { /* when |x|<2**-54, return x */
            // t = huge+x; /* return x with inexact flags when x!=0 */
            // return x - (t-(huge+x));
            return x; // inexact flag is not set, but Java ignors this flag
                      // anyway
        } else {
            k = 0;
        }

        /* x is now in primary range */
        hfx = 0.5 * x;
        hxs = x * hfx;
        r1 = 1.00000000000000000000e+00 + hxs * (Q1 + hxs * (Q2 + hxs * (Q3 + hxs * (Q4 + hxs * Q5))));
        t = 3.0 - r1 * hfx;
        e = hxs * ((r1 - t) / (6.0 - x * t));
        if (k == 0) {
            return x - (x * e - hxs); /* c is 0 */
        } else {
            e = (x * (e - c) - c);
            e -= hxs;
            if (k == -1) {
                return 0.5 * (x - e) - 0.5;
            }

            if (k == 1) {
                if (x < -0.25) {
                    return -2.0 * (e - (x + 0.5));
                } else {
                    return 1.00000000000000000000e+00 + 2.0 * (x - e);
                }
            }

            if (k <= -2 || k > 56) { /* suffice to return ieee_exp(x)-1 */
                y = 1.00000000000000000000e+00 - (e - x);
                yBits = Double.doubleToRawLongBits(y);
                yBits += (((long) k) << 52); /* add k to y's exponent */
                return Double.longBitsToDouble(yBits) - 1.00000000000000000000e+00;
            }

            long tBits = Double.doubleToRawLongBits(1.00000000000000000000e+00) & 0x00000000ffffffffL;

            if (k < 20) {
                tBits |= (((long) 0x3ff00000) - (0x200000 >> k)) << 32;
                y = Double.longBitsToDouble(tBits) - (e - x);
                yBits = Double.doubleToRawLongBits(y);
                yBits += (((long) k) << 52); /* add k to y's exponent */
                return Double.longBitsToDouble(yBits);
            } else {
                tBits |= ((((long) 0x3ff) - k) << 52); /* 2^-k */
                y = x - (e + Double.longBitsToDouble(tBits));
                y += 1.00000000000000000000e+00;
                yBits = Double.doubleToRawLongBits(y);
                yBits += (((long) k) << 52); /* add k to y's exponent */
                return Double.longBitsToDouble(yBits);
            }
        }
    }

    /**
     * Returns the double conversion of the most positive (closest to positive
     * infinity) integer less than or equal to the argument.
     * <p>
     * Special cases:
     * <ul>
     * <li>{@code floor(+0.0) = +0.0}</li>
     * <li>{@code floor(-0.0) = -0.0}</li>
     * <li>{@code floor(+infinity) = +infinity}</li>
     * <li>{@code floor(-infinity) = -infinity}</li>
     * <li>{@code floor(NaN) = NaN}</li>
     * </ul>
     */
    public static native double floor(double d) /*-[
      return floor(d);
    ]-*/;

    /**
     * Returns {@code sqrt(}<i>{@code x}</i><sup>{@code 2}</sup>{@code +} <i>
     * {@code y}</i><sup>{@code 2}</sup>{@code )}. The final result is without
     * medium underflow or overflow.
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
    public static native double hypot(double x, double y) /*-[
      // ARM processors return hypot(x, NaN) as x, so test separately.
      if (isnan(x) || isnan(y)) {
        return (isinf(x) || isinf(y)) ? JavaLangDouble_POSITIVE_INFINITY : JavaLangDouble_NaN;
      }
      return hypot(x, y);
    ]-*/;

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
     * @return the IEEE754 floating point reminder of of {@code x/y}.
     */
    public static native double IEEEremainder(double x, double y) /*-[
      // According to the Mac OS X math.h online man page, their routines are
      // all IEEE Standard 754 compliant.
      return remainder(x, y);
    ]-*/;

    private static final double LG1 = 6.666666666666735130e-01;
    private static final double LG2 = 3.999999999940941908e-01;
    private static final double LG3 = 2.857142874366239149e-01;
    private static final double LG4 = 2.222219843214978396e-01;
    private static final double LG5 = 1.818357216161805012e-01;
    private static final double LG6 = 1.531383769920937332e-01;
    private static final double LG7 = 1.479819860511658591e-01;

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
     * @param x
     *            the value whose log has to be computed.
     * @return the natural logarithm of the argument.
     */
    public static double log(double x) {
        double hfsq, f, s, z, R, w, t1, t2, dk;
        int hx, i, j, k = 0;
        int lx; // watch out, should be unsigned

        long bits = Double.doubleToRawLongBits(x);
        hx = (int) (bits >>> 32); /* high word of x */
        lx = (int) bits; /* low word of x */

        if (hx < 0x00100000) { /* x < 2**-1022 */
            if (((hx & 0x7fffffff) | lx) == 0) {
                return -TWO54 / 0.0; /* ieee_log(+-0)=-inf */
            }

            if (hx < 0) {
                return (x - x) / 0.0; /* ieee_log(-#) = NaN */
            }

            k -= 54;
            x *= TWO54; /* subnormal number, scale up x */
            bits = Double.doubleToRawLongBits(x);
            hx = (int) (bits >>> 32); /* high word of x */
        }

        if (hx >= 0x7ff00000) {
            return x + x;
        }

        k += (hx >> 20) - 1023;
        hx &= 0x000fffff;
        bits &= 0x00000000ffffffffL;
        i = (hx + 0x95f64) & 0x100000;
        bits |= ((long) hx | (i ^ 0x3ff00000)) << 32; /* normalize x or x/2 */
        x = Double.longBitsToDouble(bits);
        k += (i >> 20);
        f = x - 1.0;

        if ((0x000fffff & (2 + hx)) < 3) { /* |f| < 2**-20 */
            if (f == 0.0) {
                if (k == 0) {
                    return 0.0;
                } else {
                    dk = k;
                }
                return dk * LN2_HI + dk * LN2_LO;
            }

            R = f * f * (0.5 - 0.33333333333333333 * f);
            if (k == 0) {
                return f - R;
            } else {
                dk = k;
                return dk * LN2_HI - ((R - dk * LN2_LO) - f);
            }
        }
        s = f / (2.0 + f);
        dk = k;
        z = s * s;
        i = hx - 0x6147a;
        w = z * z;
        j = 0x6b851 - hx;
        t1 = w * (LG2 + w * (LG4 + w * LG6));
        t2 = z * (LG1 + w * (LG3 + w * (LG5 + w * LG7)));
        i |= j;
        R = t2 + t1;
        if (i > 0) {
            hfsq = 0.5 * f * f;
            if (k == 0) {
                return f - (hfsq - s * (hfsq + R));
            } else {
                return dk * LN2_HI
                        - ((hfsq - (s * (hfsq + R) + dk * LN2_LO)) - f);
            }
        } else {
            if (k == 0) {
                return f - s * (f - R);
            } else {
                return dk * LN2_HI - ((s * (f - R) - dk * LN2_LO) - f);
            }
        }
    }

    private static final double IVLN10 = 4.34294481903251816668e-01;
    private static final double LOG10_2HI = 3.01029995663611771306e-01;
    private static final double LOG10_2LO = 3.69423907715893078616e-13;

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
     * @param x
     *            the value whose base 10 log has to be computed.
     * @return the the base 10 logarithm of x
     */
    public static double log10(double x) {
        double y, z;
        int i, k = 0, hx;
        int lx; // careful: lx should be unsigned!
        long bits = Double.doubleToRawLongBits(x);
        hx = (int) (bits >> 32); /* high word of x */
        lx = (int) bits; /* low word of x */
        if (hx < 0x00100000) { /* x < 2**-1022 */
            if (((hx & 0x7fffffff) | lx) == 0) {
                return -TWO54 / 0.0; /* ieee_log(+-0)=-inf */
            }

            if (hx < 0) {
                return (x - x) / 0.0; /* ieee_log(-#) = NaN */
            }

            k -= 54;
            x *= TWO54; /* subnormal number, scale up x */
            bits = Double.doubleToRawLongBits(x);
            hx = (int) (bits >> 32); /* high word of x */
        }

        if (hx >= 0x7ff00000) {
            return x + x;
        }

        k += (hx >> 20) - 1023;
        i = (int) (((k & 0x00000000ffffffffL) & 0x80000000) >>> 31);
        hx = (hx & 0x000fffff) | ((0x3ff - i) << 20);
        y = k + i;
        bits &= 0x00000000ffffffffL;
        bits |= ((long) hx) << 32;
        x = Double.longBitsToDouble(bits); // __HI(x) = hx;
        z = y * LOG10_2LO + IVLN10 * log(x);
        return z + y * LOG10_2HI;
    }

    private static final double LP1 = 6.666666666666735130e-01,
            LP2 = 3.999999999940941908e-01, /* 3FD99999 9997FA04 */
            LP3 = 2.857142874366239149e-01, /* 3FD24924 94229359 */
            LP4 = 2.222219843214978396e-01, /* 3FCC71C5 1D8E78AF */
            LP5 = 1.818357216161805012e-01, /* 3FC74664 96CB03DE */
            LP6 = 1.531383769920937332e-01, /* 3FC39A09 D078C69F */
            LP7 = 1.479819860511658591e-01; /* 3FC2F112 DF3E5244 */

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
     * @param x
     *            the value to compute the {@code ln(1+d)} of.
     * @return the natural logarithm of the sum of the argument and 1.
     */

    public static double log1p(double x) {
        double hfsq, f = 0.0, c = 0.0, s, z, R, u = 0.0;
        int k, hx, hu = 0, ax;

        final long bits = Double.doubleToRawLongBits(x);
        hx = (int) (bits >>> 32); /* high word of x */
        ax = hx & 0x7fffffff;

        k = 1;
        if (hx < 0x3FDA827A) { /* x < 0.41422 */
            if (ax >= 0x3ff00000) { /* x <= -1.0 */
                if (x == -1.0) {
                    return -TWO54 / 0.0; /* ieee_log1p(-1)=+inf */
                } else {
                    return (x - x) / (x - x); /* ieee_log1p(x<-1)=NaN */
                }
            }
            if (ax < 0x3e200000) {
                if (TWO54 + x > 0.0 && ax < 0x3c900000) {
                    return x;
                } else {
                    return x - x * x * 0.5;
                }
            }
            if (hx > 0 || hx <= 0xbfd2bec3) {
                k = 0;
                f = x;
                hu = 1;
            } /* -0.2929<x<0.41422 */
        }

        if (hx >= 0x7ff00000) {
            return x + x;
        }

        if (k != 0) {
            long uBits;
            if (hx < 0x43400000) {
                u = 1.0 + x;
                uBits = Double.doubleToRawLongBits(u);
                hu = (int) (uBits >>> 32);
                k = (hu >> 20) - 1023;
                c = (k > 0) ? 1.0 - (u - x) : x - (u - 1.0);/* correction term */
                c /= u;
            } else {
                uBits = Double.doubleToRawLongBits(x);
                hu = (int) (uBits >>> 32);
                k = (hu >> 20) - 1023;
                c = 0;
            }
            hu &= 0x000fffff;
            if (hu < 0x6a09e) {
                // __HI(u) = hu|0x3ff00000; /* normalize u */
                uBits &= 0x00000000ffffffffL;
                uBits |= ((long) hu | 0x3ff00000) << 32;
                u = Double.longBitsToDouble(uBits);
            } else {
                k += 1;
                // __HI(u) = hu|0x3fe00000; /* normalize u/2 */
                uBits &= 0xffffffffL;
                uBits |= ((long) hu | 0x3fe00000) << 32;
                u = Double.longBitsToDouble(uBits);
                hu = (0x00100000 - hu) >> 2;
            }
            f = u - 1.0;
        }
        hfsq = 0.5 * f * f;
        if (hu == 0) { /* |f| < 2**-20 */
            if (f == 0.0) {
                if (k == 0) {
                    return 0.0;
                } else {
                    c += k * LN2_LO;
                    return k * LN2_HI + c;
                }
            }

            R = hfsq * (1.0 - 0.66666666666666666 * f);
            if (k == 0) {
                return f - R;
            } else {
                return k * LN2_HI - ((R - (k * LN2_LO + c)) - f);
            }
        }

        s = f / (2.0 + f);
        z = s * s;
        R = z * (LP1 + z * (LP2 + z
                * (LP3 + z * (LP4 + z * (LP5 + z * (LP6 + z * LP7))))));
        if (k == 0) {
            return f - (hfsq - s * (hfsq + R));
        } else {
            return k * LN2_HI
                    - ((hfsq - (s * (hfsq + R) + (k * LN2_LO + c))) - f);
        }
    }

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
     */
    public static double max(double d1, double d2) {
        if (d1 > d2)
            return d1;
        if (d1 < d2)
            return d2;
        /* if either arg is NaN, return NaN */
        if (d1 != d2)
            return Double.NaN;
        /* max( +0.0,-0.0) == +0.0 */
        if (d1 == 0.0 &&
                ((Double.doubleToLongBits(d1) & Double.doubleToLongBits(d2)) & 0x8000000000000000L) == 0)
            return 0.0;
        return d1;
    }

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
     */
    public static float max(float f1, float f2) {
        if (f1 > f2)
            return f1;
        if (f1 < f2)
            return f2;
        /* if either arg is NaN, return NaN */
        if (f1 != f2)
            return Float.NaN;
        /* max( +0.0,-0.0) == +0.0 */
        if (f1 == 0.0f &&
                ((Float.floatToIntBits(f1) & Float.floatToIntBits(f2)) & 0x80000000) == 0)
            return 0.0f;
        return f1;
    }

    /**
     * Returns the most positive (closest to positive infinity) of the two
     * arguments.
     */
    public static int max(int i1, int i2) {
        return Math.max(i1, i2);
    }

    /**
     * Returns the most positive (closest to positive infinity) of the two
     * arguments.
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
     */
    public static double min(double d1, double d2) {
        if (d1 > d2)
            return d2;
        if (d1 < d2)
            return d1;
        /* if either arg is NaN, return NaN */
        if (d1 != d2)
            return Double.NaN;
        /* min( +0.0,-0.0) == -0.0 */
        if (d1 == 0.0 &&
                ((Double.doubleToLongBits(d1) | Double.doubleToLongBits(d2)) & 0x8000000000000000l) != 0)
            return 0.0 * (-1.0);
        return d1;
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
     */
    public static float min(float f1, float f2) {
        if (f1 > f2)
            return f2;
        if (f1 < f2)
            return f1;
        /* if either arg is NaN, return NaN */
        if (f1 != f2)
            return Float.NaN;
        /* min( +0.0,-0.0) == -0.0 */
        if (f1 == 0.0f &&
                ((Float.floatToIntBits(f1) | Float.floatToIntBits(f2)) & 0x80000000) != 0)
            return 0.0f * (-1.0f);
        return f1;
    }

    /**
     * Returns the most negative (closest to negative infinity) of the two
     * arguments.
     */
    public static int min(int i1, int i2) {
        return Math.min(i1, i2);
    }

    /**
     * Returns the most negative (closest to negative infinity) of the two
     * arguments.
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
    public static native double pow(double x, double y) /*-[
      return pow(x, y);
    ]-*/;

    /**
     * Returns a pseudo-random number between 0.0 (inclusive) and 1.0
     * (exclusive).
     *
     * @return a pseudo-random number.
     */
    public static double random() {
        return Math.random();
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
    public static native double rint(double d) /*-[
      return rint(d);
    ]-*/;

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
        return Math.round(d);
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
        return Math.round(f);
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
    public static double signum(double d) {
        return Math.signum(d);
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
    public static float signum(float f) {
        return Math.signum(f);
    }

    private static final double shuge = 1.0e307;

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
     * @param x
     *            the value whose hyperbolic sine has to be computed.
     * @return the hyperbolic sine of the argument.
     */
    public static double sinh(double x) {
        double t, w, h;
        int ix, jx;
        final long bits = Double.doubleToRawLongBits(x);

        jx = (int) (bits >>> 32);
        ix = jx & 0x7fffffff;

        /* x is INF or NaN */
        if (ix >= 0x7ff00000) {
            return x + x;
        }

        h = 0.5;
        if (jx < 0) {
            h = -h;
        }

        /* |x| in [0,22], return sign(x)*0.5*(E+E/(E+1))) */
        if (ix < 0x40360000) { /* |x|<22 */
            if (ix < 0x3e300000) /* |x|<2**-28 */
                if (shuge + x > 1.00000000000000000000e+00) {
                    return x;/* ieee_sinh(tiny) = tiny with inexact */
                }
            t = expm1(Math.abs(x));
            if (ix < 0x3ff00000)
                return h * (2.0 * t - t * t / (t + 1.00000000000000000000e+00));
            return h * (t + t / (t + 1.00000000000000000000e+00));
        }

        /* |x| in [22, ieee_log(maxdouble)] return 0.5*ieee_exp(|x|) */
        if (ix < 0x40862E42) {
            return h * exp(Math.abs(x));
        }

        /* |x| in [log(maxdouble), overflowthresold] */
        final long lx = ((ONEBITS >>> 29) + ((int) bits)) & 0x00000000ffffffffL;
        // lx = *( (((*(unsigned*)&one)>>29)) + (unsigned*)&x);
        if (ix < 0x408633CE || (ix == 0x408633ce) && (lx <= 0x8fb9f87dL)) {
            w = exp(0.5 * Math.abs(x));
            t = h * w;
            return t * w;
        }

        /* |x| > overflowthresold, ieee_sinh(x) overflow */
        return x * shuge;
    }

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
    public static native double sin(double d) /*-[
      return sin(d);
    ]-*/;

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
     */
    public static native double sqrt(double d) /*-[
      return sqrt(d);
    ]-*/;

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
     *            the angle whose tangent has to be computed, in radians.
     * @return the tangent of the argument.
     */
    public static native double tan(double d) /*-[
      return tan(d);
    ]-*/;

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
     * @param x
     *            the value whose hyperbolic tangent has to be computed.
     * @return the hyperbolic tangent of the argument
     */
    public static double tanh(double x) {
        double t, z;
        int jx, ix;

        final long bits = Double.doubleToRawLongBits(x);
        /* High word of |x|. */
        jx = (int) (bits >>> 32);
        ix = jx & 0x7fffffff;

        /* x is INF or NaN */
        if (ix >= 0x7ff00000) {
            if (jx >= 0) {
                return 1.00000000000000000000e+00 / x + 1.00000000000000000000e+00; /* ieee_tanh(+-inf)=+-1 */
            } else {
                return 1.00000000000000000000e+00 / x - 1.00000000000000000000e+00; /* ieee_tanh(NaN) = NaN */
            }
        }

        /* |x| < 22 */
        if (ix < 0x40360000) { /* |x|<22 */
            if (ix < 0x3c800000) { /* |x|<2**-55 */
                return x * (1.00000000000000000000e+00 + x);/* ieee_tanh(small) = small */
            }

            if (ix >= 0x3ff00000) { /* |x|>=1 */
                t = Math.expm1(2.0 * Math.abs(x));
                z = 1.00000000000000000000e+00 - 2.0 / (t + 2.0);
            } else {
                t = Math.expm1(-2.0 * Math.abs(x));
                z = -t / (t + 2.0);
            }
            /* |x| > 22, return +-1 */
        } else {
            z = 1.00000000000000000000e+00 - TINY; /* raised inexact flag */
        }
        return (jx >= 0) ? z : -z;
    }

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
        return Math.toDegrees(angrad);
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
        return Math.toRadians(angdeg);
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
     * <li>{@code ulp(+infinity) = infinity}</li>
     * <li>{@code ulp(-infinity) = infinity}</li>
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
     * <li>{@code ulp(+infinity) = infinity}</li>
     * <li>{@code ulp(-infinity) = infinity}</li>
     * <li>{@code ulp(NaN) = NaN}</li>
     * </ul>
     *
     * @param f
     *            the floating-point value to compute ulp of.
     * @return the size of a ulp of the argument.
     */
    public static float ulp(float f) {
        return Math.ulp(f);
    }

    private static native double nextafter(double x, double y) /*-[
      return nextafter(x, y);
    ]-*/;

    /**
     * Returns a double with the given magnitude and the sign of {@code sign}.
     * If {@code sign} is NaN, the sign of the result is positive.
     *
     * @since 1.6
     */
    public static double copySign(double magnitude, double sign) {
        // We manually inline Double.isNaN here because the JIT can't do it yet.
        // With Double.isNaN: 236.3ns
        // With manual inline: 141.2ns
        // With no check (i.e. Math's behavior): 110.0ns
        // (Tested on a Nexus One.)
        long magnitudeBits = Double.doubleToRawLongBits(magnitude);
        long signBits = Double.doubleToRawLongBits((sign != sign) ? 1.0 : sign);
        magnitudeBits = (magnitudeBits & ~Double.SIGN_MASK)
                | (signBits & Double.SIGN_MASK);
        return Double.longBitsToDouble(magnitudeBits);
    }

    /**
     * Returns a float with the given magnitude and the sign of {@code sign}. If
     * {@code sign} is NaN, the sign of the result is positive.
     *
     * @since 1.6
     */
    public static float copySign(float magnitude, float sign) {
        // We manually inline Float.isNaN here because the JIT can't do it yet.
        // With Float.isNaN: 214.7ns
        // With manual inline: 112.3ns
        // With no check (i.e. Math's behavior): 93.1ns
        // (Tested on a Nexus One.)
        int magnitudeBits = Float.floatToRawIntBits(magnitude);
        int signBits = Float.floatToRawIntBits((sign != sign) ? 1.0f : sign);
        magnitudeBits = (magnitudeBits & ~Float.SIGN_MASK)
                | (signBits & Float.SIGN_MASK);
        return Float.intBitsToFloat(magnitudeBits);
    }

    /**
     * Returns the exponent of float {@code f}.
     *
     * @since 1.6
     */
    public static int getExponent(float f) {
        return Math.getExponent(f);
    }

    /**
     * Returns the exponent of double {@code d}.
     *
     * @since 1.6
     */
    public static int getExponent(double d) {
        return Math.getExponent(d);
    }

    /**
     * Returns the next double after {@code start} in the given
     * {@code direction}.
     *
     * @since 1.6
     */
    public static double nextAfter(double start, double direction) {
        if (start == 0 && direction == 0) {
            return direction;
        }
        return nextafter(start, direction);
    }

    /**
     * Returns the next float after {@code start} in the given {@code direction}
     * .
     *
     * @since 1.6
     */
    public static float nextAfter(float start, double direction) {
        return Math.nextAfter(start, direction);
    }

    /**
     * Returns the next double larger than {@code d}.
     *
     * @since 1.6
     */
    public static double nextUp(double d) {
        return Math.nextUp(d);
    }

    /**
     * Returns the next float larger than {@code f}.
     *
     * @since 1.6
     */
    public static float nextUp(float f) {
        return Math.nextUp(f);
    }

    /**
     * Returns {@code d} * 2^{@code scaleFactor}. The result may be rounded.
     *
     * @since 1.6
     */
    public static double scalb(double d, int scaleFactor) {
        if (Double.isNaN(d) || Double.isInfinite(d) || d == 0) {
            return d;
        }
        // change double to long for calculation
        long bits = Double.doubleToLongBits(d);
        // the sign of the results must be the same of given d
        long sign = bits & Double.SIGN_MASK;
        // calculates the factor of the result
        long factor = (int) ((bits & Double.EXPONENT_MASK) >> Double.MANTISSA_BITS)
                - Double.EXPONENT_BIAS + scaleFactor;

        // calculates the factor of sub-normal values
        int subNormalFactor = Long.numberOfLeadingZeros(bits & ~Double.SIGN_MASK)
                - Double.EXPONENT_BITS;
        if (subNormalFactor < 0) {
            // not sub-normal values
            subNormalFactor = 0;
        }
        if (Math.abs(d) < Double.MIN_NORMAL) {
            factor = factor - subNormalFactor;
        }
        if (factor > Double.MAX_EXPONENT) {
            return (d > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY);
        }

        long result;
        // if result is a sub-normal
        if (factor < -Double.EXPONENT_BIAS) {
            // the number of digits that shifts
            long digits = factor + Double.EXPONENT_BIAS + subNormalFactor;
            if (Math.abs(d) < Double.MIN_NORMAL) {
                // origin d is already sub-normal
                result = shiftLongBits(bits & Double.MANTISSA_MASK, digits);
            } else {
                // origin d is not sub-normal, change mantissa to sub-normal
                result = shiftLongBits(bits & Double.MANTISSA_MASK | 0x0010000000000000L, digits - 1);
            }
        } else {
            if (Math.abs(d) >= Double.MIN_NORMAL) {
                // common situation
                result = ((factor + Double.EXPONENT_BIAS) << Double.MANTISSA_BITS) | (bits & Double.MANTISSA_MASK);
            } else {
                // origin d is sub-normal, change mantissa to normal style
                result = ((factor + Double.EXPONENT_BIAS) << Double.MANTISSA_BITS) | ((bits << (subNormalFactor + 1)) & Double.MANTISSA_MASK);
            }
        }
        return Double.longBitsToDouble(result | sign);
    }

    /**
     * Returns {@code d} * 2^{@code scaleFactor}. The result may be rounded.
     *
     * @since 1.6
     */
    public static float scalb(float d, int scaleFactor) {
        if (Float.isNaN(d) || Float.isInfinite(d) || d == 0) {
            return d;
        }
        int bits = Float.floatToIntBits(d);
        int sign = bits & Float.SIGN_MASK;
        int factor = ((bits & Float.EXPONENT_MASK) >> Float.MANTISSA_BITS)
                - Float.EXPONENT_BIAS + scaleFactor;
        // calculates the factor of sub-normal values
        int subNormalFactor = Integer.numberOfLeadingZeros(bits & ~Float.SIGN_MASK) - Float.EXPONENT_BITS;
        if (subNormalFactor < 0) {
            // not sub-normal values
            subNormalFactor = 0;
        }
        if (Math.abs(d) < Float.MIN_NORMAL) {
            factor = factor - subNormalFactor;
        }
        if (factor > Float.MAX_EXPONENT) {
            return (d > 0 ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY);
        }

        int result;
        // if result is a sub-normal
        if (factor < -Float.EXPONENT_BIAS) {
            // the number of digits that shifts
            int digits = factor + Float.EXPONENT_BIAS + subNormalFactor;
            if (Math.abs(d) < Float.MIN_NORMAL) {
                // origin d is already sub-normal
                result = shiftIntBits(bits & Float.MANTISSA_MASK, digits);
            } else {
                // origin d is not sub-normal, change mantissa to sub-normal
                result = shiftIntBits(bits & Float.MANTISSA_MASK | 0x00800000, digits - 1);
            }
        } else {
            if (Math.abs(d) >= Float.MIN_NORMAL) {
                // common situation
                result = ((factor + Float.EXPONENT_BIAS) << Float.MANTISSA_BITS)
                        | (bits & Float.MANTISSA_MASK);
            } else {
                // origin d is sub-normal, change mantissa to normal style
                result = ((factor + Float.EXPONENT_BIAS)
                        << Float.MANTISSA_BITS) | (
                        (bits << (subNormalFactor + 1)) & Float.MANTISSA_MASK);
            }
        }
        return Float.intBitsToFloat(result | sign);
    }

    // Shifts integer bits as float, if the digits is positive, left-shift; if
    // not, shift to right and calculate its carry.
    private static int shiftIntBits(int bits, int digits) {
        if (digits > 0) {
            return bits << digits;
        }
        // change it to positive
        int absDigits = -digits;
        if (Integer.numberOfLeadingZeros(bits & ~Float.SIGN_MASK)
                <= (32 - absDigits)) {
            // some bits will remain after shifting, calculates its carry
            if ((((bits >> (absDigits - 1)) & 0x1) == 0) || Integer.numberOfTrailingZeros(bits) == (absDigits - 1)) {
                return bits >> absDigits;
            }
            return ((bits >> absDigits) + 1);
        }
        return 0;
    }

    // Shifts long bits as double, if the digits is positive, left-shift; if
    // not, shift to right and calculate its carry.
    private static long shiftLongBits(long bits, long digits) {
        if (digits > 0) {
            return bits << digits;
        }
        // change it to positive
        long absDigits = -digits;
        if (Long.numberOfLeadingZeros(bits & ~Double.SIGN_MASK)
                <= (64 - absDigits)) {
            // some bits will remain after shifting, calculates its carry
            if ((((bits >> (absDigits - 1)) & 0x1) == 0) || Long.numberOfTrailingZeros(bits) == (absDigits - 1)) {
                return bits >> absDigits;
            }
            return ((bits >> absDigits) + 1);
        }
        return 0;
    }
}
