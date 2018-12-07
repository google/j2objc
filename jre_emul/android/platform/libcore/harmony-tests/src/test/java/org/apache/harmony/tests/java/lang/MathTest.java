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

package org.apache.harmony.tests.java.lang;

public class MathTest extends junit.framework.TestCase {

    double HYP = Math.sqrt(2.0);

    double OPP = 1.0;

    double ADJ = 1.0;

    /* Required to make previous preprocessor flags work - do not remove */
    int unused = 0;

    /**
     * java.lang.Math#abs(double)
     */
    public void test_absD() {
        // Test for method double java.lang.Math.abs(double)

        assertTrue("Incorrect double abs value",
                (Math.abs(-1908.8976) == 1908.8976));
        assertTrue("Incorrect double abs value",
                (Math.abs(1908.8976) == 1908.8976));
    }

    /**
     * java.lang.Math#abs(float)
     */
    public void test_absF() {
        // Test for method float java.lang.Math.abs(float)
        assertTrue("Incorrect float abs value",
                (Math.abs(-1908.8976f) == 1908.8976f));
        assertTrue("Incorrect float abs value",
                (Math.abs(1908.8976f) == 1908.8976f));
    }

    /**
     * java.lang.Math#abs(int)
     */
    public void test_absI() {
        // Test for method int java.lang.Math.abs(int)
        assertTrue("Incorrect int abs value", (Math.abs(-1908897) == 1908897));
        assertTrue("Incorrect int abs value", (Math.abs(1908897) == 1908897));
    }

    /**
     * java.lang.Math#abs(long)
     */
    public void test_absJ() {
        // Test for method long java.lang.Math.abs(long)
        assertTrue("Incorrect long abs value",
                (Math.abs(-19088976000089L) == 19088976000089L));
        assertTrue("Incorrect long abs value",
                (Math.abs(19088976000089L) == 19088976000089L));
    }

    /**
     * java.lang.Math#acos(double)
     */
    public void test_acosD() {
        // Test for method double java.lang.Math.acos(double)
        double r = Math.cos(Math.acos(ADJ / HYP));
        long lr = Double.doubleToLongBits(r);
        long t = Double.doubleToLongBits(ADJ / HYP);
        assertTrue("Returned incorrect arc cosine", lr == t || (lr + 1) == t
                || (lr - 1) == t);
    }

    /**
     * java.lang.Math#asin(double)
     */
    public void test_asinD() {
        // Test for method double java.lang.Math.asin(double)
        double r = Math.sin(Math.asin(OPP / HYP));
        long lr = Double.doubleToLongBits(r);
        long t = Double.doubleToLongBits(OPP / HYP);
        assertTrue("Returned incorrect arc sine", lr == t || (lr + 1) == t
                || (lr - 1) == t);
    }

    /**
     * java.lang.Math#atan(double)
     */
    public void test_atanD() {
        // Test for method double java.lang.Math.atan(double)
        double answer = Math.tan(Math.atan(1.0));
        assertTrue("Returned incorrect arc tangent: " + answer, answer <= 1.0
                && answer >= 9.9999999999999983E-1);
    }

    /**
     * java.lang.Math#atan2(double, double)
     */
    public void test_atan2DD() {
        // Test for method double java.lang.Math.atan2(double, double)
        double answer = Math.atan(Math.tan(1.0));
        assertTrue("Returned incorrect arc tangent: " + answer, answer <= 1.0
                && answer >= 9.9999999999999983E-1);
    }

    /**
     * java.lang.Math#cbrt(double)
     */
    public void test_cbrt_D() {
        //Test for special situations
        assertTrue(Double.isNaN(Math.cbrt(Double.NaN)));
        assertEquals(Double.POSITIVE_INFINITY, Math.cbrt(Double.POSITIVE_INFINITY), 0D);
        assertEquals(Double.NEGATIVE_INFINITY, Math.cbrt(Double.NEGATIVE_INFINITY), 0D);
        assertEquals(Double.doubleToLongBits(0.0), Double.doubleToLongBits(Math.cbrt(0.0)));
        assertEquals(Double.doubleToLongBits(+0.0), Double.doubleToLongBits(Math.cbrt(+0.0)));
        assertEquals(Double.doubleToLongBits(-0.0), Double.doubleToLongBits(Math.cbrt(-0.0)));

        assertEquals(3.0, Math.cbrt(27.0), 0D);
        assertEquals(23.111993172558684, Math.cbrt(12345.6), Math.ulp(23.111993172558684));
        assertEquals(5.643803094122362E102, Math.cbrt(Double.MAX_VALUE), 0D);
        assertEquals(0.01, Math.cbrt(0.000001), 0D);

        assertEquals(-3.0, Math.cbrt(-27.0), 0D);
        assertEquals(-23.111993172558684, Math.cbrt(-12345.6), Math.ulp(-23.111993172558684));
        assertEquals(1.7031839360032603E-108, Math.cbrt(Double.MIN_VALUE), 0D);
        assertEquals(-0.01, Math.cbrt(-0.000001), 0D);
    }

    /**
     * java.lang.Math#ceil(double)
     */
    public void test_ceilD() {
        // Test for method double java.lang.Math.ceil(double)
        assertEquals("Incorrect ceiling for double",
                79, Math.ceil(78.89), 0);
        assertEquals("Incorrect ceiling for double",
                -78, Math.ceil(-78.89), 0);
    }

    /**
     * cases for test_copySign_DD in MathTest/StrictMathTest
     */
    static final double[] COPYSIGN_DD_CASES = new double[] {
            Double.POSITIVE_INFINITY, Double.MAX_VALUE, 3.4E302, 2.3,
            Double.MIN_NORMAL, Double.MIN_NORMAL / 2, Double.MIN_VALUE, +0.0,
            0.0, -0.0, -Double.MIN_VALUE, -Double.MIN_NORMAL / 2,
            -Double.MIN_NORMAL, -4.5, -3.4E102, -Double.MAX_VALUE,
            Double.NEGATIVE_INFINITY };

    /**
     * {@link java.lang.Math#copySign(double, double)}
     * @since 1.6
     */
    @SuppressWarnings("boxing")
    public void test_copySign_DD() {
        for (int i = 0; i < COPYSIGN_DD_CASES.length; i++) {
            final double magnitude = COPYSIGN_DD_CASES[i];
            final long absMagnitudeBits = Double.doubleToLongBits(Math
                    .abs(magnitude));
            final long negMagnitudeBits = Double.doubleToLongBits(-Math
                    .abs(magnitude));

            // cases for NaN
            assertEquals("If the sign is NaN, the result should be positive.",
                    absMagnitudeBits, Double.doubleToLongBits(Math.copySign(
                    magnitude, Double.NaN)));
            assertTrue("The result should be NaN.", Double.isNaN(Math.copySign(
                    Double.NaN, magnitude)));

            for (int j = 0; j < COPYSIGN_DD_CASES.length; j++) {
                final double sign = COPYSIGN_DD_CASES[j];
                final long resultBits = Double.doubleToLongBits(Math.copySign(
                        magnitude, sign));

                if (sign > 0 || Double.valueOf(+0.0).equals(sign)
                        || Double.valueOf(0.0).equals(sign)) {
                    assertEquals(
                            "If the sign is positive, the result should be positive.",
                            absMagnitudeBits, resultBits);
                }
                if (sign < 0 || Double.valueOf(-0.0).equals(sign)) {
                    assertEquals(
                            "If the sign is negative, the result should be negative.",
                            negMagnitudeBits, resultBits);
                }
            }
        }

        assertTrue("The result should be NaN.", Double.isNaN(Math.copySign(
                Double.NaN, Double.NaN)));

        try {
            Math.copySign((Double) null, 2.3);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            Math.copySign(2.3, (Double) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            Math.copySign((Double) null, (Double) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * cases for test_copySign_FF in MathTest/StrictMathTest
     */
    static final float[] COPYSIGN_FF_CASES = new float[] {
            Float.POSITIVE_INFINITY, Float.MAX_VALUE, 3.4E12f, 2.3f,
            Float.MIN_NORMAL, Float.MIN_NORMAL / 2, Float.MIN_VALUE, +0.0f,
            0.0f, -0.0f, -Float.MIN_VALUE, -Float.MIN_NORMAL / 2,
            -Float.MIN_NORMAL, -4.5f, -5.6442E21f, -Float.MAX_VALUE,
            Float.NEGATIVE_INFINITY };

    /**
     * {@link java.lang.Math#copySign(float, float)}
     * @since 1.6
     */
    @SuppressWarnings("boxing")
    public void test_copySign_FF() {
        if (System.getProperty("os.arch").equals("armv7")) {
          return;
        }
        for (int i = 0; i < COPYSIGN_FF_CASES.length; i++) {
            final float magnitude = COPYSIGN_FF_CASES[i];
            final int absMagnitudeBits = Float.floatToIntBits(Math
                    .abs(magnitude));
            final int negMagnitudeBits = Float.floatToIntBits(-Math
                    .abs(magnitude));

            // cases for NaN
            assertEquals("If the sign is NaN, the result should be positive.",
                    absMagnitudeBits, Float.floatToIntBits(Math.copySign(
                    magnitude, Float.NaN)));
            assertTrue("The result should be NaN.", Float.isNaN(Math.copySign(
                    Float.NaN, magnitude)));

            for (int j = 0; j < COPYSIGN_FF_CASES.length; j++) {
                final float sign = COPYSIGN_FF_CASES[j];
                final int resultBits = Float.floatToIntBits(Math.copySign(
                        magnitude, sign));
                if (sign > 0 || Float.valueOf(+0.0f).equals(sign)
                        || Float.valueOf(0.0f).equals(sign)) {
                    assertEquals(
                            "If the sign is positive, the result should be positive.",
                            absMagnitudeBits, resultBits);
                }
                if (sign < 0 || Float.valueOf(-0.0f).equals(sign)) {
                    assertEquals(
                            "If the sign is negative, the result should be negative.",
                            negMagnitudeBits, resultBits);
                }
            }
        }

        assertTrue("The result should be NaN.", Float.isNaN(Math.copySign(
                Float.NaN, Float.NaN)));

        try {
            Math.copySign((Float) null, 2.3f);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            Math.copySign(2.3f, (Float) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            Math.copySign((Float) null, (Float) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * java.lang.Math#cos(double)
     */
    public void test_cosD() {
        // Test for method double java.lang.Math.cos(double)
        assertEquals("Incorrect answer", 1.0, Math.cos(0), 0D);
        assertEquals("Incorrect answer", 0.5403023058681398, Math.cos(1), 0D);
    }

    /**
     * java.lang.Math#cosh(double)
     */
    public void test_cosh_D() {
        // Test for special situations
        assertTrue(Double.isNaN(Math.cosh(Double.NaN)));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.cosh(Double.POSITIVE_INFINITY), 0D);
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.cosh(Double.NEGATIVE_INFINITY), 0D);
        assertEquals("Should return 1.0", 1.0, Math.cosh(+0.0), 0D);
        assertEquals("Should return 1.0", 1.0, Math.cosh(-0.0), 0D);

        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.cosh(1234.56), 0D);
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.cosh(-1234.56), 0D);
        assertEquals("Should return 1.0000000000005", 1.0000000000005, Math
                .cosh(0.000001), 0D);
        assertEquals("Should return 1.0000000000005", 1.0000000000005, Math
                .cosh(-0.000001), 0D);
        assertEquals("Should return 5.212214351945598", 5.212214351945598, Math
                .cosh(2.33482), 0D);

        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.cosh(Double.MAX_VALUE), 0D);
        assertEquals("Should return 1.0", 1.0, Math.cosh(Double.MIN_VALUE), 0D);
    }

    /**
     * java.lang.Math#exp(double)
     */
    public void test_expD() {
        // Test for method double java.lang.Math.exp(double)
        assertTrue("Incorrect answer returned for simple power", Math.abs(Math
                .exp(4D)
                - Math.E * Math.E * Math.E * Math.E) < 0.1D);
        assertTrue("Incorrect answer returned for larger power", Math.log(Math
                .abs(Math.exp(5.5D)) - 5.5D) < 10.0D);
    }

    /**
     * java.lang.Math#expm1(double)
     */
    public void test_expm1_D() {
        // Test for special cases
        assertTrue("Should return NaN", Double.isNaN(Math.expm1(Double.NaN)));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.expm1(Double.POSITIVE_INFINITY), 0D);
        assertEquals("Should return -1.0", -1.0, Math
                .expm1(Double.NEGATIVE_INFINITY), 0D);
        assertEquals(Double.doubleToLongBits(0.0), Double.doubleToLongBits(Math
                .expm1(0.0)));
        assertEquals(Double.doubleToLongBits(+0.0), Double
                .doubleToLongBits(Math.expm1(+0.0)));
        assertEquals(Double.doubleToLongBits(-0.0), Double
                .doubleToLongBits(Math.expm1(-0.0)));

        assertEquals("Should return -9.999950000166666E-6",
                -9.999950000166666E-6, Math.expm1(-0.00001), 0D);
        assertEquals("Should return 1.0145103074469635E60",
                1.0145103074469635E60, Math.expm1(138.16951162), 0D);
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math
                .expm1(123456789123456789123456789.4521584223), 0D);
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.expm1(Double.MAX_VALUE), 0D);
        assertEquals("Should return MIN_VALUE", Double.MIN_VALUE, Math
                .expm1(Double.MIN_VALUE), 0D);
    }

    /**
     * java.lang.Math#floor(double)
     */
    public void test_floorD() {
        assertEquals("Incorrect floor for int", 42, Math.floor(42), 0);
        assertEquals("Incorrect floor for -int", -2, Math.floor(-2), 0);
        assertEquals("Incorrect floor for zero", 0d, Math.floor(0d), 0);

        assertEquals("Incorrect floor for +double", 78, Math.floor(78.89), 0);
        assertEquals("Incorrect floor for -double", -79, Math.floor(-78.89), 0);
        assertEquals("floor large +double", 3.7314645675925406E19, Math.floor(3.7314645675925406E19), 0);
        assertEquals("floor large -double", -8.173521839218E12, Math.floor(-8.173521839218E12), 0);
        assertEquals("floor small double", 0.0d, Math.floor(1.11895241315E-102), 0);

        // Compare toString representations here since -0.0 = +0.0, and
        // NaN != NaN and we need to distinguish
        assertEquals("Floor failed for NaN",
                Double.toString(Double.NaN), Double.toString(Math.floor(Double.NaN)));
        assertEquals("Floor failed for +0.0",
                Double.toString(+0.0d), Double.toString(Math.floor(+0.0d)));
        assertEquals("Floor failed for -0.0",
                Double.toString(-0.0d), Double.toString(Math.floor(-0.0d)));
        assertEquals("Floor failed for +infinity",
                Double.toString(Double.POSITIVE_INFINITY), Double.toString(Math.floor(Double.POSITIVE_INFINITY)));
        assertEquals("Floor failed for -infinity",
                Double.toString(Double.NEGATIVE_INFINITY), Double.toString(Math.floor(Double.NEGATIVE_INFINITY)));
    }

    /**
     * cases for test_getExponent_D in MathTest/StrictMathTest
     */
    static final double GETEXPONENT_D_CASES[] = new double[] {
            Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
            Double.MAX_VALUE, -Double.MAX_VALUE, 2.342E231, -2.342E231, 2800.0,
            -2800.0, 5.323, -5.323, 1.323, -1.323, 0.623, -0.623, 0.323,
            -0.323, Double.MIN_NORMAL * 24, -Double.MIN_NORMAL * 24,
            Double.MIN_NORMAL, -Double.MIN_NORMAL, Double.MIN_NORMAL / 2,
            -Double.MIN_NORMAL / 2, Double.MIN_VALUE, -Double.MIN_VALUE, +0.0,
            0.0, -0.0, Double.NaN };

    /**
     * result for test_getExponent_D in MathTest/StrictMathTest
     */
    static final int GETEXPONENT_D_RESULTS[] = new int[] {
            Double.MAX_EXPONENT + 1, Double.MAX_EXPONENT + 1,
            Double.MAX_EXPONENT, Double.MAX_EXPONENT, 768, 768, 11, 11, 2, 2,
            0, 0, -1, -1, -2, -2, -1018, -1018, Double.MIN_EXPONENT,
            Double.MIN_EXPONENT, Double.MIN_EXPONENT - 1,
            Double.MIN_EXPONENT - 1, Double.MIN_EXPONENT - 1,
            Double.MIN_EXPONENT - 1, Double.MIN_EXPONENT - 1,
            Double.MIN_EXPONENT - 1, Double.MIN_EXPONENT - 1,
            Double.MAX_EXPONENT + 1 };

    /**
     * {@link java.lang.Math#getExponent(double)}
     * @since 1.6
     */
    @SuppressWarnings("boxing")
    public void test_getExponent_D() {
        for (int i = 0; i < GETEXPONENT_D_CASES.length; i++) {
            final double number = GETEXPONENT_D_CASES[i];
            final int result = GETEXPONENT_D_RESULTS[i];
            assertEquals("Wrong result of getExponent(double).", result, Math
                    .getExponent(number));
        }

        try {
            Math.getExponent((Double) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * cases for test_getExponent_F in MathTest/StrictMathTest
     */
    static final float GETEXPONENT_F_CASES[] = new float[] {
            Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.MAX_VALUE,
            -Float.MAX_VALUE, 3.4256E23f, -3.4256E23f, 2800.0f, -2800.0f,
            5.323f, -5.323f, 1.323f, -1.323f, 0.623f, -0.623f, 0.323f, -0.323f,
            Float.MIN_NORMAL * 24, -Float.MIN_NORMAL * 24, Float.MIN_NORMAL,
            -Float.MIN_NORMAL, Float.MIN_NORMAL / 2, -Float.MIN_NORMAL / 2,
            Float.MIN_VALUE, -Float.MIN_VALUE, +0.0f, 0.0f, -0.0f, Float.NaN, 1, Float.MIN_NORMAL * 1.5f };

    /**
     * result for test_getExponent_F in MathTest/StrictMathTest
     */
    static final int GETEXPONENT_F_RESULTS[] = new int[] {
            Float.MAX_EXPONENT + 1, Float.MAX_EXPONENT + 1, Float.MAX_EXPONENT,
            Float.MAX_EXPONENT, 78, 78, 11, 11, 2, 2, 0, 0, -1, -1, -2, -2,
            -122, -122, Float.MIN_EXPONENT, Float.MIN_EXPONENT,
            Float.MIN_EXPONENT - 1, Float.MIN_EXPONENT - 1,
            Float.MIN_EXPONENT - 1, Float.MIN_EXPONENT - 1,
            Float.MIN_EXPONENT - 1, Float.MIN_EXPONENT - 1,
            Float.MIN_EXPONENT - 1, Float.MAX_EXPONENT + 1, 0, Float.MIN_EXPONENT };

    /**
     * {@link java.lang.Math#getExponent(float)}
     * @since 1.6
     */
    @SuppressWarnings("boxing")
    public void test_getExponent_F() {
        for (int i = 0; i < GETEXPONENT_F_CASES.length; i++) {
            final float number = GETEXPONENT_F_CASES[i];
            final int result = GETEXPONENT_F_RESULTS[i];
            assertEquals("Wrong result of getExponent(float).", result, Math
                    .getExponent(number));
        }
        try {
            Math.getExponent((Float) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * java.lang.Math#hypot(double, double)
     */
    public void test_hypot_DD() {
        // Test for special cases
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.hypot(Double.POSITIVE_INFINITY,
                1.0), 0D);
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.hypot(Double.NEGATIVE_INFINITY,
                123.324), 0D);
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.hypot(-758.2587,
                Double.POSITIVE_INFINITY), 0D);
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.hypot(5687.21,
                Double.NEGATIVE_INFINITY), 0D);
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.hypot(Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY), 0D);
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.hypot(Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY), 0D);
        assertTrue("Should be NaN", Double.isNaN(Math.hypot(Double.NaN,
                2342301.89843)));
        assertTrue("Should be NaN", Double.isNaN(Math.hypot(-345.2680,
                Double.NaN)));

        assertEquals("Should return 2396424.905416697", 2396424.905416697, Math
                .hypot(12322.12, -2396393.2258), 0D);
        assertEquals("Should return 138.16958070558556", 138.16958070558556,
                Math.hypot(-138.16951162, 0.13817035864), 0D);
        assertEquals("Should return 1.7976931348623157E308",
                1.7976931348623157E308, Math.hypot(Double.MAX_VALUE, 211370.35), 0D);
        assertEquals("Should return 5413.7185", 5413.7185, Math.hypot(
                -5413.7185, Double.MIN_VALUE), 0D);
    }

    /**
     * java.lang.Math#IEEEremainder(double, double)
     */
    public void test_IEEEremainderDD() {
        // Test for method double java.lang.Math.IEEEremainder(double, double)
        assertEquals("Incorrect remainder returned",
                0.0, Math.IEEEremainder(1.0, 1.0), 0D);
        assertTrue("Incorrect remainder returned", Math.IEEEremainder(1.32,
                89.765) >= 1.4705063220631647E-2
                || Math.IEEEremainder(1.32, 89.765) >= 1.4705063220631649E-2);
    }

    /**
     * java.lang.Math#log(double)
     */
    public void test_logD() {
        // Test for method double java.lang.Math.log(double)
        for (double d = 10; d >= -10; d -= 0.5) {
            double answer = Math.log(Math.exp(d));
            assertTrue("Answer does not equal expected answer for d = " + d
                    + " answer = " + answer, Math.abs(answer - d) <= Math
                    .abs(d * 0.00000001));
        }
    }

    /**
     * java.lang.Math#log10(double)
     */
    @SuppressWarnings("boxing")
    public void test_log10_D() {
        // Test for special cases
        assertTrue(Double.isNaN(Math.log10(Double.NaN)));
        assertTrue(Double.isNaN(Math.log10(-2541.05745687234187532)));
        assertTrue(Double.isNaN(Math.log10(-0.1)));
        assertEquals(Double.POSITIVE_INFINITY, Math.log10(Double.POSITIVE_INFINITY));
        assertEquals(Double.NEGATIVE_INFINITY, Math.log10(0.0));
        assertEquals(Double.NEGATIVE_INFINITY, Math.log10(+0.0));
        assertEquals(Double.NEGATIVE_INFINITY, Math.log10(-0.0));

        assertEquals(3.0, Math.log10(1000.0));
        assertEquals(14.0, Math.log10(Math.pow(10, 14)));
        assertEquals(3.7389561269540406, Math.log10(5482.2158));
        assertEquals(14.661551142893833, Math.log10(458723662312872.125782332587));
        assertEquals(-0.9083828622192334, Math.log10(0.12348583358871));
        assertEquals(308.25471555991675, Math.log10(Double.MAX_VALUE));
        assertEquals(-323.3062153431158, Math.log10(Double.MIN_VALUE));
    }

    /**
     * java.lang.Math#log1p(double)
     */
    public void test_log1p_D() {
        // Test for special cases
        assertTrue("Should return NaN", Double.isNaN(Math.log1p(Double.NaN)));
        assertTrue("Should return NaN", Double.isNaN(Math.log1p(-32.0482175)));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, Math.log1p(Double.POSITIVE_INFINITY), 0D);
        assertEquals(Double.doubleToLongBits(0.0), Double.doubleToLongBits(Math
                .log1p(0.0)));
        assertEquals(Double.doubleToLongBits(+0.0), Double
                .doubleToLongBits(Math.log1p(+0.0)));
        assertEquals(Double.doubleToLongBits(-0.0), Double
                .doubleToLongBits(Math.log1p(-0.0)));

        assertEquals("Should return -0.2941782295312541", -0.2941782295312541,
                Math.log1p(-0.254856327), 0D);
        assertEquals("Should return 7.368050685564151", 7.368050685564151, Math
                .log1p(1583.542), 0D);
        assertEquals("Should return 0.4633708685409921", 0.4633708685409921,
                Math.log1p(0.5894227), 0D);
        assertEquals("Should return 709.782712893384", 709.782712893384, Math
                .log1p(Double.MAX_VALUE), 0D);
        assertEquals("Should return Double.MIN_VALUE", Double.MIN_VALUE, Math
                .log1p(Double.MIN_VALUE), 0D);
    }

    public void test_maxDD_Math() {
        test_maxDD(true /* use Math */);
    }

    public void test_maxDD_Double() {
        test_maxDD(false /* use Math */);
    }

    /**
     * java.lang.Math#max(double, double)
     */
    private static void test_maxDD(boolean useMath) {
        // Test for method double java.lang.Math.max(double, double)
        assertEquals("Incorrect double max value", 1908897.6000089,
                max(-1908897.6000089, 1908897.6000089, useMath), 0D);
        assertEquals("Incorrect double max value",
                1908897.6000089, max(2.0, 1908897.6000089, useMath), 0D);
        assertEquals("Incorrect double max value", -2.0, max(-2.0, -1908897.6000089, useMath), 0D);

        // Compare toString representations here since -0.0 = +0.0, and
        // NaN != NaN and we need to distinguish
        assertEquals("Max failed for NaN",
                Double.toString(Double.NaN), Double.toString(max(Double.NaN, 42.0d, useMath)));
        assertEquals("Max failed for NaN",
                Double.toString(Double.NaN), Double.toString(max(42.0d, Double.NaN, useMath)));
        assertEquals("Max failed for 0.0",
                Double.toString(+0.0d), Double.toString(max(+0.0d, -0.0d, useMath)));
        assertEquals("Max failed for 0.0",
                Double.toString(+0.0d), Double.toString(max(-0.0d, +0.0d, useMath)));
        assertEquals("Max failed for -0.0d",
                Double.toString(-0.0d), Double.toString(max(-0.0d, -0.0d, useMath)));
        assertEquals("Max failed for 0.0",
                Double.toString(+0.0d), Double.toString(max(+0.0d, +0.0d, useMath)));
    }

    /**
     * java.lang.Math#max(float, float)
     */
    public void test_maxFF() {
        // Test for method float java.lang.Math.max(float, float)
        assertTrue("Incorrect float max value", Math.max(-1908897.600f,
                1908897.600f) == 1908897.600f);
        assertTrue("Incorrect float max value",
                Math.max(2.0f, 1908897.600f) == 1908897.600f);
        assertTrue("Incorrect float max value",
                Math.max(-2.0f, -1908897.600f) == -2.0f);

        // Compare toString representations here since -0.0 = +0.0, and
        // NaN != NaN and we need to distinguish
        assertEquals("Max failed for NaN",
                Float.toString(Float.NaN), Float.toString(Math.max(Float.NaN, 42.0f)));
        assertEquals("Max failed for NaN",
                Float.toString(Float.NaN), Float.toString(Math.max(42.0f, Float.NaN)));
        assertEquals("Max failed for 0.0",
                Float.toString(+0.0f), Float.toString(Math.max(+0.0f, -0.0f)));
        assertEquals("Max failed for 0.0",
                Float.toString(+0.0f), Float.toString(Math.max(-0.0f, +0.0f)));
        assertEquals("Max failed for -0.0f",
                Float.toString(-0.0f), Float.toString(Math.max(-0.0f, -0.0f)));
        assertEquals("Max failed for 0.0",
                Float.toString(+0.0f), Float.toString(Math.max(+0.0f, +0.0f)));
    }

    /**
     * java.lang.Math#max(int, int)
     */
    public void test_maxII() {
        // Test for method int java.lang.Math.max(int, int)
        assertEquals("Incorrect int max value",
                19088976, Math.max(-19088976, 19088976));
        assertEquals("Incorrect int max value",
                19088976, Math.max(20, 19088976));
        assertEquals("Incorrect int max value", -20, Math.max(-20, -19088976));
    }

    /**
     * java.lang.Math#max(long, long)
     */
    public void test_maxJJ() {
        // Test for method long java.lang.Math.max(long, long)
        assertEquals("Incorrect long max value", 19088976000089L, Math.max(-19088976000089L,
                19088976000089L));
        assertEquals("Incorrect long max value",
                19088976000089L, Math.max(20, 19088976000089L));
        assertEquals("Incorrect long max value",
                -20, Math.max(-20, -19088976000089L));
    }

    public void test_minDD_Math() {
        test_minDD(true /* useMath */);
    }

    public void test_minDD_Double() {
        test_minDD(false /* useMath */);
    }

    /**
     * java.lang.Math#min(double, double)
     */
    private static void test_minDD(boolean useMath) {
        // Test for method double java.lang.Math.min(double, double)
        assertEquals("Incorrect double min value", -1908897.6000089,
                min(-1908897.6000089, 1908897.6000089, useMath), 0D);
        assertEquals("Incorrect double min value",
                2.0, min(2.0, 1908897.6000089, useMath), 0D);
        assertEquals("Incorrect double min value", -1908897.6000089,
                min(-2.0, -1908897.6000089, useMath), 0D);
        assertEquals("Incorrect double min value", 1.0d, Math.min(1.0d, 1.0d));

        // Compare toString representations here since -0.0 = +0.0, and
        // NaN != NaN and we need to distinguish
        assertEquals("Min failed for NaN",
                Double.toString(Double.NaN), Double.toString(min(Double.NaN, 42.0d, useMath)));
        assertEquals("Min failed for NaN",
                Double.toString(Double.NaN), Double.toString(min(42.0d, Double.NaN, useMath)));
        assertEquals("Min failed for -0.0",
                Double.toString(-0.0d), Double.toString(min(+0.0d, -0.0d, useMath)));
        assertEquals("Min failed for -0.0",
                Double.toString(-0.0d), Double.toString(min(-0.0d, +0.0d, useMath)));
        assertEquals("Min failed for -0.0d",
                Double.toString(-0.0d), Double.toString(min(-0.0d, -0.0d, useMath)));
        assertEquals("Min failed for 0.0",
                Double.toString(+0.0d), Double.toString(min(+0.0d, +0.0d, useMath)));
    }

    private static double min(double a, double b, boolean useMath) {
        if (useMath) {
            return Math.min(a, b);
        } else {
            return Double.min(a, b);
        }
    }

    private static double max(double a, double b, boolean useMath) {
        if (useMath) {
            return Math.max(a, b);
        } else {
            return Double.max(a, b);
        }
    }

    /**
     * java.lang.Math#min(float, float)
     */
    public void test_minFF() {
        // Test for method float java.lang.Math.min(float, float)
        assertTrue("Incorrect float min value", Math.min(-1908897.600f,
                1908897.600f) == -1908897.600f);
        assertTrue("Incorrect float min value",
                Math.min(2.0f, 1908897.600f) == 2.0f);
        assertTrue("Incorrect float min value",
                Math.min(-2.0f, -1908897.600f) == -1908897.600f);
        assertEquals("Incorrect float min value", 1.0f, Math.min(1.0f, 1.0f));

        // Compare toString representations here since -0.0 = +0.0, and
        // NaN != NaN and we need to distinguish
        assertEquals("Min failed for NaN",
                Float.toString(Float.NaN), Float.toString(Math.min(Float.NaN, 42.0f)));
        assertEquals("Min failed for NaN",
                Float.toString(Float.NaN), Float.toString(Math.min(42.0f, Float.NaN)));
        assertEquals("Min failed for -0.0",
                Float.toString(-0.0f), Float.toString(Math.min(+0.0f, -0.0f)));
        assertEquals("Min failed for -0.0",
                Float.toString(-0.0f), Float.toString(Math.min(-0.0f, +0.0f)));
        assertEquals("Min failed for -0.0f",
                Float.toString(-0.0f), Float.toString(Math.min(-0.0f, -0.0f)));
        assertEquals("Min failed for 0.0",
                Float.toString(+0.0f), Float.toString(Math.min(+0.0f, +0.0f)));
    }

    /**
     * java.lang.Math#min(int, int)
     */
    public void test_minII() {
        // Test for method int java.lang.Math.min(int, int)
        assertEquals("Incorrect int min value",
                -19088976, Math.min(-19088976, 19088976));
        assertEquals("Incorrect int min value", 20, Math.min(20, 19088976));
        assertEquals("Incorrect int min value",
                -19088976, Math.min(-20, -19088976));

    }

    /**
     * java.lang.Math#min(long, long)
     */
    public void test_minJJ() {
        // Test for method long java.lang.Math.min(long, long)
        assertEquals("Incorrect long min value", -19088976000089L, Math.min(-19088976000089L,
                19088976000089L));
        assertEquals("Incorrect long min value",
                20, Math.min(20, 19088976000089L));
        assertEquals("Incorrect long min value",
                -19088976000089L, Math.min(-20, -19088976000089L));
    }

    /**
     * start number cases for test_nextAfter_DD in MathTest/StrictMathTest
     * NEXTAFTER_DD_START_CASES[i][0] is the start number
     * NEXTAFTER_DD_START_CASES[i][1] is the nextUp of start number
     * NEXTAFTER_DD_START_CASES[i][2] is the nextDown of start number
     */
    static final double NEXTAFTER_DD_START_CASES[][] = new double[][] {
            { 3.4, 3.4000000000000004, 3.3999999999999995 },
            { -3.4, -3.3999999999999995, -3.4000000000000004 },
            { 3.4233E109, 3.4233000000000005E109, 3.4232999999999996E109 },
            { -3.4233E109, -3.4232999999999996E109, -3.4233000000000005E109 },
            { +0.0, Double.MIN_VALUE, -Double.MIN_VALUE },
            { 0.0, Double.MIN_VALUE, -Double.MIN_VALUE },
            { -0.0, Double.MIN_VALUE, -Double.MIN_VALUE },
            { Double.MIN_VALUE, 1.0E-323, +0.0 },
            { -Double.MIN_VALUE, -0.0, -1.0E-323 },
            { Double.MIN_NORMAL, 2.225073858507202E-308, 2.225073858507201E-308 },
            { -Double.MIN_NORMAL, -2.225073858507201E-308,
                    -2.225073858507202E-308 },
            { Double.MAX_VALUE, Double.POSITIVE_INFINITY,
                    1.7976931348623155E308 },
            { -Double.MAX_VALUE, -1.7976931348623155E308,
                    Double.NEGATIVE_INFINITY },
            { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
                    Double.MAX_VALUE },
            { Double.NEGATIVE_INFINITY, -Double.MAX_VALUE,
                    Double.NEGATIVE_INFINITY } };

    /**
     * direction number cases for test_nextAfter_DD/test_nextAfter_FD in
     * MathTest/StrictMathTest
     */
    static final double NEXTAFTER_DD_FD_DIRECTION_CASES[] = new double[] {
            Double.POSITIVE_INFINITY, Double.MAX_VALUE, 8.8, 3.4, 1.4,
            Double.MIN_NORMAL, Double.MIN_NORMAL / 2, Double.MIN_VALUE, +0.0,
            0.0, -0.0, -Double.MIN_VALUE, -Double.MIN_NORMAL / 2,
            -Double.MIN_NORMAL, -1.4, -3.4, -8.8, -Double.MAX_VALUE,
            Double.NEGATIVE_INFINITY };

    /**
     * {@link java.lang.Math#nextAfter(double, double)}
     * @since 1.6
     */
    @SuppressWarnings("boxing")
    public void test_nextAfter_DD() {
        // test for most cases without exception
        for (int i = 0; i < NEXTAFTER_DD_START_CASES.length; i++) {
            final double start = NEXTAFTER_DD_START_CASES[i][0];
            final long nextUpBits = Double
                    .doubleToLongBits(NEXTAFTER_DD_START_CASES[i][1]);
            final long nextDownBits = Double
                    .doubleToLongBits(NEXTAFTER_DD_START_CASES[i][2]);

            for (int j = 0; j < NEXTAFTER_DD_FD_DIRECTION_CASES.length; j++) {
                final double direction = NEXTAFTER_DD_FD_DIRECTION_CASES[j];
                final long resultBits = Double.doubleToLongBits(Math.nextAfter(
                        start, direction));
                final long directionBits = Double.doubleToLongBits(direction);
                if (direction > start) {
                    assertEquals("Result should be next up-number.",
                            nextUpBits, resultBits);
                } else if (direction < start) {
                    assertEquals("Result should be next down-number.",
                            nextDownBits, resultBits);
                } else {
                    assertEquals("Result should be direction.", directionBits,
                            resultBits);
                }
            }
        }

        // test for cases with NaN
        for (int i = 0; i < NEXTAFTER_DD_START_CASES.length; i++) {
            assertTrue("The result should be NaN.", Double.isNaN(Math
                    .nextAfter(NEXTAFTER_DD_START_CASES[i][0], Double.NaN)));
        }
        for (int i = 0; i < NEXTAFTER_DD_FD_DIRECTION_CASES.length; i++) {
            assertTrue("The result should be NaN.", Double.isNaN(Math
                    .nextAfter(Double.NaN, NEXTAFTER_DD_FD_DIRECTION_CASES[i])));
        }
        assertTrue("The result should be NaN.", Double.isNaN(Math.nextAfter(
                Double.NaN, Double.NaN)));

        // test for exception
        try {
            Math.nextAfter((Double) null, 2.3);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            Math.nextAfter(2.3, (Double) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            Math.nextAfter((Double) null, (Double) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * start number cases for test_nextAfter_FD in MathTest/StrictMathTest
     * NEXTAFTER_FD_START_CASES[i][0] is the start number
     * NEXTAFTER_FD_START_CASES[i][1] is the nextUp of start number
     * NEXTAFTER_FD_START_CASES[i][2] is the nextDown of start number
     */
    static final float NEXTAFTER_FD_START_CASES[][] = new float[][] {
            { 3.4f, 3.4000003f, 3.3999999f },
            { -3.4f, -3.3999999f, -3.4000003f },
            { 3.4233E19f, 3.4233002E19f, 3.4232998E19f },
            { -3.4233E19f, -3.4232998E19f, -3.4233002E19f },
            { +0.0f, Float.MIN_VALUE, -Float.MIN_VALUE },
            { 0.0f, Float.MIN_VALUE, -Float.MIN_VALUE },
            { -0.0f, Float.MIN_VALUE, -Float.MIN_VALUE },
            { Float.MIN_VALUE, 2.8E-45f, +0.0f },
            { -Float.MIN_VALUE, -0.0f, -2.8E-45f },
            { Float.MIN_NORMAL, 1.1754945E-38f, 1.1754942E-38f },
            { -Float.MIN_NORMAL, -1.1754942E-38f, -1.1754945E-38f },
            { Float.MAX_VALUE, Float.POSITIVE_INFINITY, 3.4028233E38f },
            { -Float.MAX_VALUE, -3.4028233E38f, Float.NEGATIVE_INFINITY },
            { Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.MAX_VALUE },
            { Float.NEGATIVE_INFINITY, -Float.MAX_VALUE,
                    Float.NEGATIVE_INFINITY } };

    /**
     * {@link java.lang.Math#nextAfter(float, double)}
     * @since 1.6
     */
    @SuppressWarnings("boxing")
    public void test_nextAfter_FD() {
        if (System.getProperty("os.arch").equals("armv7")) {
          return;
        }
        // test for most cases without exception
        for (int i = 0; i < NEXTAFTER_FD_START_CASES.length; i++) {
            final float start = NEXTAFTER_FD_START_CASES[i][0];
            final int nextUpBits = Float
                    .floatToIntBits(NEXTAFTER_FD_START_CASES[i][1]);
            final int nextDownBits = Float
                    .floatToIntBits(NEXTAFTER_FD_START_CASES[i][2]);

            for (int j = 0; j < NEXTAFTER_DD_FD_DIRECTION_CASES.length; j++) {
                final double direction = NEXTAFTER_DD_FD_DIRECTION_CASES[j];
                final int resultBits = Float.floatToIntBits(Math.nextAfter(
                        start, direction));
                if (direction > start) {
                    assertEquals("Result should be next up-number.",
                            nextUpBits, resultBits);
                } else if (direction < start) {
                    assertEquals("Result should be next down-number.",
                            nextDownBits, resultBits);
                } else {
                    final int equivalentBits = Float.floatToIntBits(new Float(
                            direction));
                    assertEquals(
                            "Result should be a number equivalent to direction.",
                            equivalentBits, resultBits);
                }
            }
        }

        // test for cases with NaN
        for (int i = 0; i < NEXTAFTER_FD_START_CASES.length; i++) {
            assertTrue("The result should be NaN.", Float.isNaN(Math.nextAfter(
                    NEXTAFTER_FD_START_CASES[i][0], Float.NaN)));
        }
        for (int i = 0; i < NEXTAFTER_DD_FD_DIRECTION_CASES.length; i++) {
            assertTrue("The result should be NaN.", Float.isNaN(Math.nextAfter(
                    Float.NaN, NEXTAFTER_DD_FD_DIRECTION_CASES[i])));
        }
        assertTrue("The result should be NaN.", Float.isNaN(Math.nextAfter(
                Float.NaN, Float.NaN)));

        // test for exception
        try {
            Math.nextAfter((Float) null, 2.3);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            Math.nextAfter(2.3, (Float) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            Math.nextAfter((Float) null, (Float) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * {@link java.lang.Math#nextUp(double)}
     * @since 1.6
     */
    @SuppressWarnings("boxing")
    public void test_nextUp_D() {
        if (System.getProperty("os.arch").equals("armv7")) {
          return;
        }
        // This method is semantically equivalent to nextAfter(d,
        // Double.POSITIVE_INFINITY),
        // so we use the data of test_nextAfter_DD
        for (int i = 0; i < NEXTAFTER_DD_START_CASES.length; i++) {
            final double start = NEXTAFTER_DD_START_CASES[i][0];
            final long nextUpBits = Double
                    .doubleToLongBits(NEXTAFTER_DD_START_CASES[i][1]);
            final long resultBits = Double.doubleToLongBits(Math.nextUp(start));
            assertEquals("Result should be next up-number.", nextUpBits,
                    resultBits);
        }

        // test for cases with NaN
        assertTrue("The result should be NaN.", Double.isNaN(Math
                .nextUp(Double.NaN)));

        // test for exception
        try {
            Math.nextUp((Double) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * {@link java.lang.Math#nextUp(float)}
     * @since 1.6
     */
    @SuppressWarnings("boxing")
    public void test_nextUp_F() {
        if (System.getProperty("os.arch").equals("armv7")) {
          return;
        }
        // This method is semantically equivalent to nextAfter(f,
        // Float.POSITIVE_INFINITY),
        // so we use the data of test_nextAfter_FD
        for (int i = 0; i < NEXTAFTER_FD_START_CASES.length; i++) {
            final float start = NEXTAFTER_FD_START_CASES[i][0];
            final int nextUpBits = Float
                    .floatToIntBits(NEXTAFTER_FD_START_CASES[i][1]);
            final int resultBits = Float.floatToIntBits(Math.nextUp(start));
            assertEquals("Result should be next up-number.", nextUpBits,
                    resultBits);
        }

        // test for cases with NaN
        assertTrue("The result should be NaN.", Float.isNaN(Math
                .nextUp(Float.NaN)));

        // test for exception
        try {
            Math.nextUp((Float) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * {@link java.lang.Math#nextDown(double)}
     * @since 1.8
     */
    @SuppressWarnings("boxing")
    public void test_nextDown_D() {
        // This method is semantically equivalent to nextAfter(d,
        // Double.NEGATIVE_INFINITY),
        // so we use the data of test_nextAfter_DD
        for (int i = 0; i < NEXTAFTER_DD_START_CASES.length; i++) {
            final double start = NEXTAFTER_DD_START_CASES[i][0];
            final long nextDownBits = Double
                    .doubleToLongBits(NEXTAFTER_DD_START_CASES[i][2]);
            final long resultBits = Double.doubleToLongBits(Math.nextDown(start));
            assertEquals("Result should be next down-number.", nextDownBits,
                    resultBits);
        }

        // test for cases with NaN
        assertTrue("The result should be NaN.", Double.isNaN(Math
                .nextDown(Double.NaN)));

        // test for exception
        try {
            Math.nextDown((Double) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * {@link java.lang.Math#nextDown(float)}
     * @since 1.8
     */
    @SuppressWarnings("boxing")
    public void test_nextDown_F() {
        // This method is semantically equivalent to nextAfter(f,
        // Float.NEGATIVE_INFINITY),
        // so we use the data of test_nextAfter_FD
        for (int i = 0; i < NEXTAFTER_FD_START_CASES.length; i++) {
            final float start = NEXTAFTER_FD_START_CASES[i][0];
            final int nextDownBits = Float
                    .floatToIntBits(NEXTAFTER_FD_START_CASES[i][2]);
            final int resultBits = Float.floatToIntBits(Math.nextDown(start));
            assertEquals("Result should be next down-number.", nextDownBits,
                    resultBits);
        }

        // test for cases with NaN
        assertTrue("The result should be NaN.", Float.isNaN(Math
                .nextDown(Float.NaN)));

        // test for exception
        try {
            Math.nextDown((Float) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * java.lang.Math#pow(double, double)
     */
    public void test_powDD() {
        // Test for method double java.lang.Math.pow(double, double)
        double NZERO = longTodouble(doubleTolong(0.0) ^ 0x8000000000000000L);
        double p1 = 1.0;
        double p2 = 2.0;
        double p3 = 3.0;
        double p4 = 4.0;
        double p5 = 5.0;
        double p6 = 6.0;
        double p7 = 7.0;
        double p8 = 8.0;
        double p9 = 9.0;
        double p10 = 10.0;
        double p11 = 11.0;
        double p12 = 12.0;
        double p13 = 13.0;
        double p14 = 14.0;
        double p15 = 15.0;
        double p16 = 16.0;
        double[] values = { p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12,
                p13, p14, p15, p16 };

        for (int x = 0; x < values.length; x++) {
            double dval = values[x];
            double nagateDval = negateDouble(dval);
            if (nagateDval == Double.NaN) {
                continue;
            }

            // If the second argument is positive or negative zero, then the
            // result is 1.0.
            assertEquals("Result should be Math.pow(" + dval
                    + ",-0.0)=+1.0", 1.0, Math.pow(dval, NZERO));
            assertEquals("Result should be Math.pow(" + nagateDval
                    + ",-0.0)=+1.0", 1.0, Math.pow(nagateDval, NZERO));
            assertEquals("Result should be Math.pow(" + dval
                    + ",+0.0)=+1.0", 1.0, Math.pow(dval, +0.0));
            assertEquals("Result should be Math.pow(" + nagateDval
                    + ",+0.0)=+1.0", 1.0, Math.pow(nagateDval, +0.0));

            // If the second argument is 1.0, then the result is the same as the
            // first argument.
            assertEquals("Result should be Math.pow(" + dval + "," + 1.0 + ")="
                    + dval, dval, Math.pow(dval, 1.0));
            assertEquals("Result should be Math.pow(" + nagateDval + "," + 1.0
                    + ")=" + nagateDval, nagateDval, Math.pow(nagateDval, 1.0));

            // If the second argument is NaN, then the result is NaN.
            assertEquals("Result should be Math.pow(" + nagateDval + ","
                    + Double.NaN + ")=" + Double.NaN, Double.NaN, Math.pow(nagateDval,
                    Double.NaN));

            if (dval > 1) {
                // If the first argument is NaN and the second argument is
                // nonzero,
                // then the result is NaN.
                assertEquals("Result should be Math.pow(" + Double.NaN + ","
                        + dval + ")=" + Double.NaN, Double.NaN, Math.pow(Double.NaN, dval));
                assertEquals("Result should be Math.pow(" + Double.NaN + ","
                        + nagateDval + ")=" + Double.NaN, Double.NaN, Math.pow(Double.NaN,
                        nagateDval));

                /*
                 * If the first argument is positive zero and the second
                 * argument is greater than zero, or the first argument is
                 * positive infinity and the second argument is less than zero,
                 * then the result is positive zero.
                 */
                assertEquals("Result should be Math.pow(" + 0.0 + "," + dval
                        + ")=" + 0.0, +0.0, Math.pow(0.0, dval));
                assertEquals("Result should be Math.pow("
                        + Double.POSITIVE_INFINITY + "," + nagateDval + ")="
                        + 0.0, +0.0, Math.pow(Double.POSITIVE_INFINITY, nagateDval));

                /*
                 * If the first argument is positive zero and the second
                 * argument is less than zero, or the first argument is positive
                 * infinity and the second argument is greater than zero, then
                 * the result is positive infinity.
                 */
                assertEquals("Result should be Math.pow(" + 0.0 + ","
                        + nagateDval + ")=" + Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
                        Math.pow(0.0, nagateDval));
                assertEquals("Result should be Math.pow("
                        + Double.POSITIVE_INFINITY + "," + dval + ")="
                        + Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Math.pow(
                        Double.POSITIVE_INFINITY, dval));

                // Not a finite odd integer
                if (dval % 2 == 0) {
                    /*
                     * If the first argument is negative zero and the second
                     * argument is greater than zero but not a finite odd
                     * integer, or the first argument is negative infinity and
                     * the second argument is less than zero but not a finite
                     * odd integer, then the result is positive zero.
                     */
                    assertEquals("Result should be Math.pow(" + NZERO + ","
                            + dval + ")=" + 0.0, +0.0, Math.pow(NZERO, dval));
                    assertEquals("Result should be Math.pow("
                            + Double.NEGATIVE_INFINITY + "," + nagateDval
                            + ")=" + 0.0, +0.0, Math.pow(Double.NEGATIVE_INFINITY,
                            nagateDval));

                    /*
                     * If the first argument is negative zero and the second
                     * argument is less than zero but not a finite odd integer,
                     * or the first argument is negative infinity and the second
                     * argument is greater than zero but not a finite odd
                     * integer, then the result is positive infinity.
                     */
                    assertEquals("Result should be Math.pow(" + NZERO + ","
                            + nagateDval + ")=" + Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
                            Math.pow(NZERO, nagateDval));
                    assertEquals("Result should be Math.pow("
                            + Double.NEGATIVE_INFINITY + "," + dval + ")="
                            + Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Math.pow(
                            Double.NEGATIVE_INFINITY, dval));
                }

                // finite odd integer
                if (dval % 2 != 0) {
                    /*
                     * If the first argument is negative zero and the second
                     * argument is a positive finite odd integer, or the first
                     * argument is negative infinity and the second argument is
                     * a negative finite odd integer, then the result is
                     * negative zero.
                     */
                    assertEquals("Result should be Math.pow(" + NZERO + ","
                            + dval + ")=" + NZERO, NZERO, Math.pow(NZERO, dval));
                    assertEquals("Result should be Math.pow("
                            + Double.NEGATIVE_INFINITY + "," + nagateDval
                            + ")=" + NZERO, NZERO, Math.pow(Double.NEGATIVE_INFINITY,
                            nagateDval));
                    /*
                     * If the first argument is negative zero and the second
                     * argument is a negative finite odd integer, or the first
                     * argument is negative infinity and the second argument is
                     * a positive finite odd integer then the result is negative
                     * infinity.
                     */
                    assertEquals("Result should be Math.pow(" + NZERO + ","
                            + nagateDval + ")=" + Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
                            Math.pow(NZERO, nagateDval));
                    assertEquals("Result should be Math.pow("
                            + Double.NEGATIVE_INFINITY + "," + dval + ")="
                            + Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Math.pow(
                            Double.NEGATIVE_INFINITY, dval));
                }

                /**
                 * 1. If the first argument is finite and less than zero if the
                 * second argument is a finite even integer, the result is equal
                 * to the result of raising the absolute value of the first
                 * argument to the power of the second argument 
                 *
                 * 2. if the second argument is a finite odd integer, the result is equal to the
                 * negative of the result of raising the absolute value of the
                 * first argument to the power of the second argument 
                 *
                 * 3. if the second argument is finite and not an integer, then the result
                 * is NaN.
                 */
                for (int j = 1; j < values.length; j++) {
                    double jval = values[j];
                    if (jval % 2.0 == 0.0) {
                        assertEquals("" + nagateDval + " " + jval, Math.pow(
                                dval, jval), Math.pow(nagateDval, jval));
                    } else {
                        assertEquals("" + nagateDval + " " + jval, -1.0
                                * Math.pow(dval, jval), Math.pow(nagateDval,
                                jval));
                    }
                    assertEquals(Double.NaN, Math
                            .pow(nagateDval, jval / 0.5467));
                    assertEquals(Double.NaN, Math.pow(nagateDval, -1.0 * jval
                            / 0.5467));
                }
            }

            if (dval > 1) {
                /*
                 * If the absolute value of the first argument is greater than 1
                 * and the second argument is positive infinity, or the absolute
                 * value of the first argument is less than 1 and the second
                 * argument is negative infinity, then the result is positive
                 * infinity.
                 */
                assertEquals("Result should be Math.pow(" + dval + ","
                        + Double.POSITIVE_INFINITY + ")="
                        + Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Math.pow(dval,
                        Double.POSITIVE_INFINITY));

                assertEquals("Result should be Math.pow(" + nagateDval + ","
                        + Double.NEGATIVE_INFINITY + ")="
                        + Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Math.pow(-0.13456,
                        Double.NEGATIVE_INFINITY));

                /*
                 * If the absolute value of the first argument is greater than 1
                 * and the second argument is negative infinity, or the absolute
                 * value of the first argument is less than 1 and the second
                 * argument is positive infinity, then the result is positive
                 * zero.
                 */
                assertEquals("Result should be Math.pow(" + dval + ","
                        + Double.NEGATIVE_INFINITY + ")= +0.0", +0.0, Math.pow(dval,
                        Double.NEGATIVE_INFINITY));
                assertEquals("Result should be Math.pow(" + nagateDval + ","
                        + Double.POSITIVE_INFINITY + ")= +0.0", +0.0, Math.pow(
                        -0.13456, Double.POSITIVE_INFINITY));
            }

            assertEquals("Result should be Math.pow(" + 0.0 + "," + dval + ")="
                    + 0.0, 0.0, Math.pow(0.0, dval));
            assertEquals("Result should be Math.pow(" + Double.NaN + "," + dval
                    + ")=" + Double.NaN, Double.NaN, Math.pow(Double.NaN, dval));
        }
        assertTrue("pow returned incorrect value",
                (long) Math.pow(2, 8) == 256l);
        assertTrue("pow returned incorrect value",
                Math.pow(2, -8) == 0.00390625d);
        assertEquals("Incorrect root returned1",
                2, Math.sqrt(Math.pow(Math.sqrt(2), 4)), 0);

        assertEquals(Double.NEGATIVE_INFINITY, Math.pow(-10.0, 3.093403029238847E15));
        assertEquals(Double.POSITIVE_INFINITY, Math.pow(10.0, 3.093403029238847E15));
    }

    private double longTodouble(long longvalue) {
        return Double.longBitsToDouble(longvalue);
    }

    private long doubleTolong(double doublevalue) {
        return Double.doubleToLongBits(doublevalue);
    }

    private double negateDouble(double doublevalue) {
        return doublevalue * -1.0;
    }

    /**
     * java.lang.Math#rint(double)
     */
    public void test_rintD() {
        // Test for method double java.lang.Math.rint(double)
        assertEquals("Failed to round properly - up to odd",
                3.0, Math.rint(2.9), 0D);
        assertTrue("Failed to round properly - NaN", Double.isNaN(Math
                .rint(Double.NaN)));
        assertEquals("Failed to round properly down  to even",
                2.0, Math.rint(2.1), 0D);
        assertTrue("Failed to round properly " + 2.5 + " to even", Math
                .rint(2.5) == 2.0);
        assertTrue("Failed to round properly " + (+0.0d),
                Math.rint(+0.0d) == +0.0d);
        assertTrue("Failed to round properly " + (-0.0d),
                Math.rint(-0.0d) == -0.0d);
    }

    /**
     * java.lang.Math#round(double)
     */
    public void test_roundD() {
        // Test for method long java.lang.Math.round(double)
        assertEquals("Incorrect rounding of a float", -91, Math.round(-90.89d));
    }

    /**
     * java.lang.Math#round(float)
     */
    public void test_roundF() {
        // Test for method int java.lang.Math.round(float)
        assertEquals("Incorrect rounding of a float", -91, Math.round(-90.89f));
    }

    /**
     * {@link java.lang.Math#scalb(double, int)}
     * @since 1.6
     */
    @SuppressWarnings("boxing")
    public void test_scalb_DI() {
        if (System.getProperty("os.arch").equals("armv7")) {
          return;
        }
        // result is normal
        assertEquals(4.1422946304E7, Math.scalb(1.2345, 25));
        assertEquals(3.679096698760986E-8, Math.scalb(1.2345, -25));
        assertEquals(1.2345, Math.scalb(1.2345, 0));
        assertEquals(7868514.304, Math.scalb(0.2345, 25));

        double normal = Math.scalb(0.2345, -25);
        assertEquals(6.98864459991455E-9, normal);
        // precision kept
        assertEquals(0.2345, Math.scalb(normal, 25));

        assertEquals(0.2345, Math.scalb(0.2345, 0));
        assertEquals(-4.1422946304E7, Math.scalb(-1.2345, 25));
        assertEquals(-6.98864459991455E-9, Math.scalb(-0.2345, -25));
        assertEquals(2.0, Math.scalb(Double.MIN_NORMAL / 2, 1024));
        assertEquals(64.0, Math.scalb(Double.MIN_VALUE, 1080));
        assertEquals(234, Math.getExponent(Math.scalb(1.0, 234)));
        assertEquals(3.9999999999999996, Math.scalb(Double.MAX_VALUE,
                Double.MIN_EXPONENT));

        // result is near infinity
        double halfMax = Math.scalb(1.0, Double.MAX_EXPONENT);
        assertEquals(8.98846567431158E307, halfMax);
        assertEquals(Double.MAX_VALUE, halfMax - Math.ulp(halfMax) + halfMax);
        assertEquals(Double.POSITIVE_INFINITY, halfMax + halfMax);
        assertEquals(1.7976931348623155E308, Math.scalb(1.0 - Math.ulp(1.0),
                Double.MAX_EXPONENT + 1));
        assertEquals(Double.POSITIVE_INFINITY, Math.scalb(1.0 - Math.ulp(1.0),
                Double.MAX_EXPONENT + 2));

        halfMax = Math.scalb(-1.0, Double.MAX_EXPONENT);
        assertEquals(-8.98846567431158E307, halfMax);
        assertEquals(-Double.MAX_VALUE, halfMax + Math.ulp(halfMax) + halfMax);
        assertEquals(Double.NEGATIVE_INFINITY, halfMax + halfMax);

        assertEquals(Double.POSITIVE_INFINITY, Math.scalb(0.345, 1234));
        assertEquals(Double.POSITIVE_INFINITY, Math.scalb(44.345E102, 934));
        assertEquals(Double.NEGATIVE_INFINITY, Math.scalb(-44.345E102, 934));

        assertEquals(Double.POSITIVE_INFINITY, Math.scalb(
                Double.MIN_NORMAL / 2, 4000));
        assertEquals(Double.POSITIVE_INFINITY, Math.scalb(Double.MIN_VALUE,
                8000));
        assertEquals(Double.POSITIVE_INFINITY, Math.scalb(Double.MAX_VALUE, 1));
        assertEquals(Double.POSITIVE_INFINITY, Math.scalb(
                Double.POSITIVE_INFINITY, 0));
        assertEquals(Double.POSITIVE_INFINITY, Math.scalb(
                Double.POSITIVE_INFINITY, -1));
        assertEquals(Double.NEGATIVE_INFINITY, Math.scalb(
                Double.NEGATIVE_INFINITY, -1));
        assertEquals(Double.NEGATIVE_INFINITY, Math.scalb(
                Double.NEGATIVE_INFINITY, Double.MIN_EXPONENT));

        // result is subnormal/zero
        long posZeroBits = Double.doubleToLongBits(+0.0);
        long negZeroBits = Double.doubleToLongBits(-0.0);
        assertEquals(posZeroBits, Double.doubleToLongBits(Math.scalb(+0.0,
                Integer.MAX_VALUE)));
        assertEquals(posZeroBits, Double.doubleToLongBits(Math
                .scalb(+0.0, -123)));
        assertEquals(posZeroBits, Double.doubleToLongBits(Math.scalb(+0.0, 0)));
        assertEquals(negZeroBits, Double
                .doubleToLongBits(Math.scalb(-0.0, 123)));
        assertEquals(negZeroBits, Double.doubleToLongBits(Math.scalb(-0.0,
                Integer.MIN_VALUE)));

        assertEquals(Double.MIN_VALUE, Math.scalb(1.0, -1074));
        assertEquals(posZeroBits, Double.doubleToLongBits(Math
                .scalb(1.0, -1075)));
        assertEquals(negZeroBits, Double.doubleToLongBits(Math.scalb(-1.0,
                -1075)));

        // precision lost
        assertEquals(Math.scalb(21.405, -1078), Math.scalb(21.405, -1079));
        assertEquals(Double.MIN_VALUE, Math.scalb(21.405, -1079));
        assertEquals(-Double.MIN_VALUE, Math.scalb(-21.405, -1079));
        assertEquals(posZeroBits, Double.doubleToLongBits(Math.scalb(21.405,
                -1080)));
        assertEquals(negZeroBits, Double.doubleToLongBits(Math.scalb(-21.405,
                -1080)));
        assertEquals(posZeroBits, Double.doubleToLongBits(Math.scalb(
                Double.MIN_VALUE, -1)));
        assertEquals(negZeroBits, Double.doubleToLongBits(Math.scalb(
                -Double.MIN_VALUE, -1)));
        assertEquals(Double.MIN_VALUE, Math.scalb(Double.MIN_NORMAL, -52));
        assertEquals(posZeroBits, Double.doubleToLongBits(Math.scalb(
                Double.MIN_NORMAL, -53)));
        assertEquals(negZeroBits, Double.doubleToLongBits(Math.scalb(
                -Double.MIN_NORMAL, -53)));
        assertEquals(Double.MIN_VALUE, Math.scalb(Double.MAX_VALUE, -2098));
        assertEquals(posZeroBits, Double.doubleToLongBits(Math.scalb(
                Double.MAX_VALUE, -2099)));
        assertEquals(negZeroBits, Double.doubleToLongBits(Math.scalb(
                -Double.MAX_VALUE, -2099)));
        assertEquals(Double.MIN_VALUE, Math.scalb(Double.MIN_NORMAL / 3, -51));
        assertEquals(posZeroBits, Double.doubleToLongBits(Math.scalb(
                Double.MIN_NORMAL / 3, -52)));
        assertEquals(negZeroBits, Double.doubleToLongBits(Math.scalb(
                -Double.MIN_NORMAL / 3, -52)));
        double subnormal = Math.scalb(Double.MIN_NORMAL / 3, -25);
        assertEquals(2.2104123E-316, subnormal);
        // precision lost
        assertFalse(Double.MIN_NORMAL / 3 == Math.scalb(subnormal, 25));

        // NaN
        assertTrue(Double.isNaN(Math.scalb(Double.NaN, 1)));
        assertTrue(Double.isNaN(Math.scalb(Double.NaN, 0)));
        assertTrue(Double.isNaN(Math.scalb(Double.NaN, -120)));

        assertEquals(1283457024, Double.doubleToLongBits(Math.scalb(
                Double.MIN_VALUE * 153, 23)));
        assertEquals(-9223372035571318784L, Double.doubleToLongBits(Math.scalb(
                -Double.MIN_VALUE * 153, 23)));
        assertEquals(36908406321184768L, Double.doubleToLongBits(Math.scalb(
                Double.MIN_VALUE * 153, 52)));
        assertEquals(-9186463630533591040L, Double.doubleToLongBits(Math.scalb(
                -Double.MIN_VALUE * 153, 52)));

        // test for exception
        try {
            Math.scalb((Double) null, (Integer) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            Math.scalb(1.0, (Integer) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            Math.scalb((Double) null, 1);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }

        long b1em1022 = 0x0010000000000000L; // bit representation of
        // Double.MIN_NORMAL
        long b1em1023 = 0x0008000000000000L; // bit representation of half of
        // Double.MIN_NORMAL
        // assert exact identity
        assertEquals(b1em1023, Double.doubleToLongBits(Math.scalb(Double
                .longBitsToDouble(b1em1022), -1)));
    }

    /**
     * {@link java.lang.Math#scalb(float, int)}
     * @since 1.6
     */
    @SuppressWarnings("boxing")
    public void test_scalb_FI() {
        if (System.getProperty("os.arch").equals("armv7")) {
          return;
        }
        // result is normal
        assertEquals(4.1422946304E7f, Math.scalb(1.2345f, 25));
        assertEquals(3.679096698760986E-8f, Math.scalb(1.2345f, -25));
        assertEquals(1.2345f, Math.scalb(1.2345f, 0));
        assertEquals(7868514.304f, Math.scalb(0.2345f, 25));

        float normal = Math.scalb(0.2345f, -25);
        assertEquals(6.98864459991455E-9f, normal);
        // precision kept
        assertEquals(0.2345f, Math.scalb(normal, 25));

        assertEquals(0.2345f, Math.scalb(0.2345f, 0));
        assertEquals(-4.1422946304E7f, Math.scalb(-1.2345f, 25));
        assertEquals(-6.98864459991455E-9f, Math.scalb(-0.2345f, -25));
        assertEquals(2.0f, Math.scalb(Float.MIN_NORMAL / 2, 128));
        assertEquals(64.0f, Math.scalb(Float.MIN_VALUE, 155));
        assertEquals(34, Math.getExponent(Math.scalb(1.0f, 34)));
        assertEquals(3.9999998f, Math
                .scalb(Float.MAX_VALUE, Float.MIN_EXPONENT));

        // result is near infinity
        float halfMax = Math.scalb(1.0f, Float.MAX_EXPONENT);
        assertEquals(1.7014118E38f, halfMax);
        assertEquals(Float.MAX_VALUE, halfMax - Math.ulp(halfMax) + halfMax);
        assertEquals(Float.POSITIVE_INFINITY, halfMax + halfMax);
        assertEquals(3.4028233E38f, Math.scalb(1.0f - Math.ulp(1.0f),
                Float.MAX_EXPONENT + 1));
        assertEquals(Float.POSITIVE_INFINITY, Math.scalb(1.0f - Math.ulp(1.0f),
                Float.MAX_EXPONENT + 2));

        halfMax = Math.scalb(-1.0f, Float.MAX_EXPONENT);
        assertEquals(-1.7014118E38f, halfMax);
        assertEquals(-Float.MAX_VALUE, halfMax + Math.ulp(halfMax) + halfMax);
        assertEquals(Float.NEGATIVE_INFINITY, halfMax + halfMax);

        assertEquals(Float.POSITIVE_INFINITY, Math.scalb(0.345f, 1234));
        assertEquals(Float.POSITIVE_INFINITY, Math.scalb(44.345E10f, 934));
        assertEquals(Float.NEGATIVE_INFINITY, Math.scalb(-44.345E10f, 934));

        assertEquals(Float.POSITIVE_INFINITY, Math.scalb(Float.MIN_NORMAL / 2,
                400));
        assertEquals(Float.POSITIVE_INFINITY, Math.scalb(Float.MIN_VALUE, 800));
        assertEquals(Float.POSITIVE_INFINITY, Math.scalb(Float.MAX_VALUE, 1));
        assertEquals(Float.POSITIVE_INFINITY, Math.scalb(
                Float.POSITIVE_INFINITY, 0));
        assertEquals(Float.POSITIVE_INFINITY, Math.scalb(
                Float.POSITIVE_INFINITY, -1));
        assertEquals(Float.NEGATIVE_INFINITY, Math.scalb(
                Float.NEGATIVE_INFINITY, -1));
        assertEquals(Float.NEGATIVE_INFINITY, Math.scalb(
                Float.NEGATIVE_INFINITY, Float.MIN_EXPONENT));

        // result is subnormal/zero
        int posZeroBits = Float.floatToIntBits(+0.0f);
        int negZeroBits = Float.floatToIntBits(-0.0f);
        assertEquals(posZeroBits, Float.floatToIntBits(Math.scalb(+0.0f,
                Integer.MAX_VALUE)));
        assertEquals(posZeroBits, Float.floatToIntBits(Math.scalb(+0.0f, -123)));
        assertEquals(posZeroBits, Float.floatToIntBits(Math.scalb(+0.0f, 0)));
        assertEquals(negZeroBits, Float.floatToIntBits(Math.scalb(-0.0f, 123)));
        assertEquals(negZeroBits, Float.floatToIntBits(Math.scalb(-0.0f,
                Integer.MIN_VALUE)));

        assertEquals(Float.MIN_VALUE, Math.scalb(1.0f, -149));
        assertEquals(posZeroBits, Float.floatToIntBits(Math.scalb(1.0f, -150)));
        assertEquals(negZeroBits, Float.floatToIntBits(Math.scalb(-1.0f, -150)));

        // precision lost
        assertEquals(Math.scalb(21.405f, -154), Math.scalb(21.405f, -153));
        assertEquals(Float.MIN_VALUE, Math.scalb(21.405f, -154));
        assertEquals(-Float.MIN_VALUE, Math.scalb(-21.405f, -154));
        assertEquals(posZeroBits, Float.floatToIntBits(Math
                .scalb(21.405f, -155)));
        assertEquals(negZeroBits, Float.floatToIntBits(Math.scalb(-21.405f,
                -155)));
        assertEquals(posZeroBits, Float.floatToIntBits(Math.scalb(
                Float.MIN_VALUE, -1)));
        assertEquals(negZeroBits, Float.floatToIntBits(Math.scalb(
                -Float.MIN_VALUE, -1)));
        assertEquals(Float.MIN_VALUE, Math.scalb(Float.MIN_NORMAL, -23));
        assertEquals(posZeroBits, Float.floatToIntBits(Math.scalb(
                Float.MIN_NORMAL, -24)));
        assertEquals(negZeroBits, Float.floatToIntBits(Math.scalb(
                -Float.MIN_NORMAL, -24)));
        assertEquals(Float.MIN_VALUE, Math.scalb(Float.MAX_VALUE, -277));
        assertEquals(posZeroBits, Float.floatToIntBits(Math.scalb(
                Float.MAX_VALUE, -278)));
        assertEquals(negZeroBits, Float.floatToIntBits(Math.scalb(
                -Float.MAX_VALUE, -278)));
        assertEquals(Float.MIN_VALUE, Math.scalb(Float.MIN_NORMAL / 3, -22));
        assertEquals(posZeroBits, Float.floatToIntBits(Math.scalb(
                Float.MIN_NORMAL / 3, -23)));
        assertEquals(negZeroBits, Float.floatToIntBits(Math.scalb(
                -Float.MIN_NORMAL / 3, -23)));
        float subnormal = Math.scalb(Float.MIN_NORMAL / 3, -11);
        assertEquals(1.913E-42f, subnormal);
        // precision lost
        assertFalse(Float.MIN_NORMAL / 3 == Math.scalb(subnormal, 11));

        assertEquals(68747264, Float.floatToIntBits(Math.scalb(
                Float.MIN_VALUE * 153, 23)));
        assertEquals(-2078736384, Float.floatToIntBits(Math.scalb(
                -Float.MIN_VALUE * 153, 23)));

        assertEquals(4896, Float.floatToIntBits(Math.scalb(
                Float.MIN_VALUE * 153, 5)));
        assertEquals(-2147478752, Float.floatToIntBits(Math.scalb(
                -Float.MIN_VALUE * 153, 5)));

        // NaN
        assertTrue(Float.isNaN(Math.scalb(Float.NaN, 1)));
        assertTrue(Float.isNaN(Math.scalb(Float.NaN, 0)));
        assertTrue(Float.isNaN(Math.scalb(Float.NaN, -120)));

        // test for exception
        try {
            Math.scalb((Float) null, (Integer) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            Math.scalb(1.0f, (Integer) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            Math.scalb((Float) null, 1);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }

        int b1em126 = 0x00800000; // bit representation of Float.MIN_NORMAL
        int b1em127 = 0x00400000; // bit representation of half
        // Float.MIN_NORMAL
        // assert exact identity
        assertEquals(b1em127, Float.floatToIntBits(Math.scalb(Float
                .intBitsToFloat(b1em126), -1)));
    }

    /**
     * java.lang.Math#signum(double)
     */
    public void test_signum_D() {
        assertTrue(Double.isNaN(Math.signum(Double.NaN)));
        assertTrue(Double.isNaN(Math.signum(Double.NaN)));
        assertEquals(Double.doubleToLongBits(0.0), Double.doubleToLongBits(Math
                .signum(0.0)));
        assertEquals(Double.doubleToLongBits(+0.0), Double
                .doubleToLongBits(Math.signum(+0.0)));
        assertEquals(Double.doubleToLongBits(-0.0), Double
                .doubleToLongBits(Math.signum(-0.0)));

        assertEquals(1.0, Math.signum(253681.2187962), 0D);
        assertEquals(-1.0, Math.signum(-125874693.56), 0D);
        if (!System.getProperty("os.arch").equals("armv7")) {
          assertEquals(1.0, Math.signum(1.2587E-308), 0D);
          assertEquals(-1.0, Math.signum(-1.2587E-308), 0D);
        }

        assertEquals(1.0, Math.signum(Double.MAX_VALUE), 0D);
        if (!System.getProperty("os.arch").equals("armv7")) {
          assertEquals(1.0, Math.signum(Double.MIN_VALUE), 0D);
        }
        assertEquals(-1.0, Math.signum(-Double.MAX_VALUE), 0D);
        if (!System.getProperty("os.arch").equals("armv7")) {
          assertEquals(-1.0, Math.signum(-Double.MIN_VALUE), 0D);
        }
        assertEquals(1.0, Math.signum(Double.POSITIVE_INFINITY), 0D);
        assertEquals(-1.0, Math.signum(Double.NEGATIVE_INFINITY), 0D);
    }

    /**
     * java.lang.Math#signum(float)
     */
    public void test_signum_F() {
        assertTrue(Float.isNaN(Math.signum(Float.NaN)));
        assertEquals(Float.floatToIntBits(0.0f), Float
                .floatToIntBits(Math.signum(0.0f)));
        assertEquals(Float.floatToIntBits(+0.0f), Float
                .floatToIntBits(Math.signum(+0.0f)));
        assertEquals(Float.floatToIntBits(-0.0f), Float
                .floatToIntBits(Math.signum(-0.0f)));

        assertEquals(1.0f, Math.signum(253681.2187962f), 0f);
        assertEquals(-1.0f, Math.signum(-125874693.56f), 0f);
        if (!System.getProperty("os.arch").equals("armv7")) {
          assertEquals(1.0f, Math.signum(1.2587E-11f), 0f);
          assertEquals(-1.0f, Math.signum(-1.2587E-11f), 0f);
        }

        assertEquals(1.0f, Math.signum(Float.MAX_VALUE), 0f);
        if (!System.getProperty("os.arch").equals("armv7")) {
          assertEquals(1.0f, Math.signum(Float.MIN_VALUE), 0f);
        }
        assertEquals(-1.0f, Math.signum(-Float.MAX_VALUE), 0f);
        if (!System.getProperty("os.arch").equals("armv7")) {
          assertEquals(-1.0f, Math.signum(-Float.MIN_VALUE), 0f);
        }
        assertEquals(1.0f, Math.signum(Float.POSITIVE_INFINITY), 0f);
        assertEquals(-1.0f, Math.signum(Float.NEGATIVE_INFINITY), 0f);
    }

    /**
     * java.lang.Math#sin(double)
     */
    public void test_sinD() {
        // Test for method double java.lang.Math.sin(double)
        assertEquals("Incorrect answer", 0.0, Math.sin(0), 0D);
        assertEquals("Incorrect answer", 0.8414709848078965, Math.sin(1), 0D);
    }

    /**
     * java.lang.Math#sinh(double)
     */
    public void test_sinh_D() {
        // Test for special situations
        assertTrue(Double.isNaN(Math.sinh(Double.NaN)));
        assertEquals(Double.POSITIVE_INFINITY, Math.sinh(Double.POSITIVE_INFINITY), 0D);
        assertEquals(Double.NEGATIVE_INFINITY, Math.sinh(Double.NEGATIVE_INFINITY), 0D);
        assertEquals(Double.doubleToLongBits(0.0), Double.doubleToLongBits(Math.sinh(0.0)));
        assertEquals(Double.doubleToLongBits(+0.0), Double.doubleToLongBits(Math.sinh(+0.0)));
        assertEquals(Double.doubleToLongBits(-0.0), Double.doubleToLongBits(Math.sinh(-0.0)));

        assertEquals(Double.POSITIVE_INFINITY, Math.sinh(1234.56), 0D);
        assertEquals(Double.NEGATIVE_INFINITY, Math.sinh(-1234.56), 0D);
        assertEquals(1.0000000000001666E-6, Math.sinh(0.000001), 0D);
        assertEquals(-1.0000000000001666E-6, Math.sinh(-0.000001), 0D);
        assertEquals(5.115386441963859, Math.sinh(2.33482), Math.ulp(5.115386441963859));
        assertEquals(Double.POSITIVE_INFINITY, Math.sinh(Double.MAX_VALUE), 0D);
        assertEquals(4.9E-324, Math.sinh(Double.MIN_VALUE), 0D);
    }

    /**
     * java.lang.Math#sqrt(double)
     */
    public void test_sqrtD() {
        // Test for method double java.lang.Math.sqrt(double)
        assertEquals("Incorrect root returned2", 7, Math.sqrt(49), 0);
    }

    /**
     * java.lang.Math#tan(double)
     */
    public void test_tanD() {
        // Test for method double java.lang.Math.tan(double)
        assertEquals("Incorrect answer", 0.0, Math.tan(0), 0D);
        if (System.getProperty("os.arch").equals("armv7")) {
          // Relax the epsilon requirement for armv7.
          assertEquals("Incorrect answer", 1.557407724654902, Math.tan(1), 0.0000001D);
        } else {
          assertEquals("Incorrect answer", 1.557407724654902, Math.tan(1), 0D);
        }

    }

    /**
     * java.lang.Math#tanh(double)
     */
    public void test_tanh_D() {
        // Test for special situations
        assertTrue("Should return NaN", Double.isNaN(Math.tanh(Double.NaN)));
        assertEquals("Should return +1.0", +1.0, Math
                .tanh(Double.POSITIVE_INFINITY), 0D);
        assertEquals("Should return -1.0", -1.0, Math
                .tanh(Double.NEGATIVE_INFINITY), 0D);
        assertEquals(Double.doubleToLongBits(0.0), Double.doubleToLongBits(Math
                .tanh(0.0)));
        assertEquals(Double.doubleToLongBits(+0.0), Double
                .doubleToLongBits(Math.tanh(+0.0)));
        assertEquals(Double.doubleToLongBits(-0.0), Double
                .doubleToLongBits(Math.tanh(-0.0)));

        assertEquals("Should return 1.0", 1.0, Math.tanh(1234.56), 0D);
        assertEquals("Should return -1.0", -1.0, Math.tanh(-1234.56), 0D);
        assertEquals("Should return 9.999999999996666E-7",
                9.999999999996666E-7, Math.tanh(0.000001), 0D);
        assertEquals("Should return 0.981422884124941", 0.981422884124941, Math
                .tanh(2.33482), 0D);
        assertEquals("Should return 1.0", 1.0, Math.tanh(Double.MAX_VALUE), 0D);
        assertEquals("Should return 4.9E-324", 4.9E-324, Math
                .tanh(Double.MIN_VALUE), 0D);
    }

    /**
     * java.lang.Math#random()
     */
    public void test_random() {
        // There isn't a place for these tests so just stick them here
        assertEquals("Wrong value E",
                4613303445314885481L, Double.doubleToLongBits(Math.E));
        assertEquals("Wrong value PI",
                4614256656552045848L, Double.doubleToLongBits(Math.PI));

        for (int i = 500; i >= 0; i--) {
            double d = Math.random();
            assertTrue("Generated number is out of range: " + d, d >= 0.0
                    && d < 1.0);
        }
    }

    /**
     * java.lang.Math#toRadians(double)
     */
    public void test_toRadiansD() {
        for (double d = 500; d >= 0; d -= 1.0) {
            double converted = Math.toDegrees(Math.toRadians(d));
            assertTrue("Converted number not equal to original. d = " + d,
                    converted >= d * 0.99999999 && converted <= d * 1.00000001);
        }
    }

    /**
     * java.lang.Math#toDegrees(double)
     */
    public void test_toDegreesD() {
        for (double d = 500; d >= 0; d -= 1.0) {
            double converted = Math.toRadians(Math.toDegrees(d));
            assertTrue("Converted number not equal to original. d = " + d,
                    converted >= d * 0.99999999 && converted <= d * 1.00000001);
        }
    }

    /**
     * java.lang.Math#ulp(double)
     */
    @SuppressWarnings("boxing")
    public void test_ulp_D() {
        // Test for special cases
        assertTrue("Should return NaN", Double.isNaN(Math.ulp(Double.NaN)));
        assertEquals("Returned incorrect value", Double.POSITIVE_INFINITY, Math
                .ulp(Double.POSITIVE_INFINITY), 0D);
        assertEquals("Returned incorrect value", Double.POSITIVE_INFINITY, Math
                .ulp(Double.NEGATIVE_INFINITY), 0D);
        assertEquals("Returned incorrect value", Double.MIN_VALUE, Math
                .ulp(0.0), 0D);
        assertEquals("Returned incorrect value", Double.MIN_VALUE, Math
                .ulp(+0.0), 0D);
        assertEquals("Returned incorrect value", Double.MIN_VALUE, Math
                .ulp(-0.0), 0D);
        assertEquals("Returned incorrect value", Math.pow(2, 971), Math
                .ulp(Double.MAX_VALUE), 0D);
        assertEquals("Returned incorrect value", Math.pow(2, 971), Math
                .ulp(-Double.MAX_VALUE), 0D);

        assertEquals("Returned incorrect value", Double.MIN_VALUE, Math
                .ulp(Double.MIN_VALUE), 0D);
        assertEquals("Returned incorrect value", Double.MIN_VALUE, Math
                .ulp(-Double.MIN_VALUE), 0D);

        assertEquals("Returned incorrect value", 2.220446049250313E-16, Math
                .ulp(1.0), 0D);
        assertEquals("Returned incorrect value", 2.220446049250313E-16, Math
                .ulp(-1.0), 0D);
        assertEquals("Returned incorrect value", 2.2737367544323206E-13, Math
                .ulp(1153.0), 0D);
    }

    /**
     * java.lang.Math#ulp(float)
     */
    @SuppressWarnings("boxing")
    public void test_ulp_f() {
        // Test for special cases
        assertTrue("Should return NaN", Float.isNaN(Math.ulp(Float.NaN)));
        assertEquals("Returned incorrect value", Float.POSITIVE_INFINITY, Math
                .ulp(Float.POSITIVE_INFINITY), 0f);
        assertEquals("Returned incorrect value", Float.POSITIVE_INFINITY, Math
                .ulp(Float.NEGATIVE_INFINITY), 0f);
        assertEquals("Returned incorrect value", Float.MIN_VALUE, Math
                .ulp(0.0f), 0f);
        assertEquals("Returned incorrect value", Float.MIN_VALUE, Math
                .ulp(+0.0f), 0f);
        assertEquals("Returned incorrect value", Float.MIN_VALUE, Math
                .ulp(-0.0f), 0f);
        assertEquals("Returned incorrect value", 2.028241E31f, Math
                .ulp(Float.MAX_VALUE), 0f);
        assertEquals("Returned incorrect value", 2.028241E31f, Math
                .ulp(-Float.MAX_VALUE), 0f);

        assertEquals("Returned incorrect value", 1.4E-45f, Math
                .ulp(Float.MIN_VALUE), 0f);
        assertEquals("Returned incorrect value", 1.4E-45f, Math
                .ulp(-Float.MIN_VALUE), 0f);

        assertEquals("Returned incorrect value", 1.1920929E-7f, Math.ulp(1.0f),
                0f);
        assertEquals("Returned incorrect value", 1.1920929E-7f,
                Math.ulp(-1.0f), 0f);
        assertEquals("Returned incorrect value", 1.2207031E-4f, Math
                .ulp(1153.0f), 0f);
        assertEquals("Returned incorrect value", 5.6E-45f, Math
                .ulp(9.403954E-38f), 0f);
    }

    /**
     * {@link java.lang.Math#shiftIntBits(int, int)}
     * @since 1.6
     */
    public void test_shiftIntBits_II() {
        if (System.getProperty("os.arch").equals("armv7")) {
          return;
        }
        class Tuple {
            public int result;

            public int value;

            public int factor;

            public Tuple(int result, int value, int factor) {
                this.result = result;
                this.value = value;
                this.factor = factor;
            }
        }
        final Tuple[] TUPLES = new Tuple[] {
                // sub-normal to sub-normal
                new Tuple(0x00000000, 0x00000001, -1),
                // round to even
                new Tuple(0x00000002, 0x00000003, -1),
                // round to even
                new Tuple(0x00000001, 0x00000005, -3),
                // round to infinity
                new Tuple(0x00000002, 0x0000000d, -3),
                // round to infinity

                // normal to sub-normal
                new Tuple(0x00000002, 0x01a00000, -24),
                // round to even
                new Tuple(0x00000004, 0x01e00000, -24),
                // round to even
                new Tuple(0x00000003, 0x01c80000, -24),
                // round to infinity
                new Tuple(0x00000004, 0x01e80000, -24),
                // round to infinity
        };
        for (int i = 0; i < TUPLES.length; ++i) {
            Tuple tuple = TUPLES[i];
            assertEquals(tuple.result, Float.floatToIntBits(Math.scalb(Float
                    .intBitsToFloat(tuple.value), tuple.factor)));
            assertEquals(tuple.result, Float.floatToIntBits(-Math.scalb(-Float
                    .intBitsToFloat(tuple.value), tuple.factor)));
        }
    }

    /**
     * {@link java.lang.Math#shiftLongBits(long, long)}
     * <p/>
     * Round result to nearest value on precision lost.
     * @since 1.6
     */
    public void test_shiftLongBits_LL() {
        if (System.getProperty("os.arch").equals("armv7")) {
          return;
        }
        class Tuple {
            public long result;

            public long value;

            public int factor;

            public Tuple(long result, long value, int factor) {
                this.result = result;
                this.value = value;
                this.factor = factor;
            }
        }
        final Tuple[] TUPLES = new Tuple[] {
                // sub-normal to sub-normal
                new Tuple(0x00000000L, 0x00000001L, -1),
                //round to even
                new Tuple(0x00000002L, 0x00000003L, -1),
                //round to even
                new Tuple(0x00000001L, 0x00000005L, -3),
                //round to infinity
                new Tuple(0x00000002L, 0x0000000dL, -3),
                //round to infinity

                // normal to sub-normal
                new Tuple(0x0000000000000002L, 0x0034000000000000L, -53), // round to even
                new Tuple(0x0000000000000004L, 0x003c000000000000L, -53), // round to even
                new Tuple(0x0000000000000003L, 0x0035000000000000L, -53), // round to infinity
                new Tuple(0x0000000000000004L, 0x003d000000000000L, -53), // round to infinity
        };
        for (int i = 0; i < TUPLES.length; ++i) {
            Tuple tuple = TUPLES[i];
            assertEquals(tuple.result, Double.doubleToLongBits(Math.scalb(
                    Double.longBitsToDouble(tuple.value), tuple.factor)));
            assertEquals(tuple.result, Double.doubleToLongBits(-Math.scalb(
                    -Double.longBitsToDouble(tuple.value), tuple.factor)));
        }
    }
}
