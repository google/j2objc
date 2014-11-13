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

import static org.apache.harmony.tests.java.lang.MathTest.COPYSIGN_DD_CASES;
import static org.apache.harmony.tests.java.lang.MathTest.COPYSIGN_FF_CASES;
import static org.apache.harmony.tests.java.lang.MathTest.GETEXPONENT_D_CASES;
import static org.apache.harmony.tests.java.lang.MathTest.GETEXPONENT_D_RESULTS;
import static org.apache.harmony.tests.java.lang.MathTest.GETEXPONENT_F_CASES;
import static org.apache.harmony.tests.java.lang.MathTest.GETEXPONENT_F_RESULTS;
import static org.apache.harmony.tests.java.lang.MathTest.NEXTAFTER_DD_START_CASES;
import static org.apache.harmony.tests.java.lang.MathTest.NEXTAFTER_DD_FD_DIRECTION_CASES;
import static org.apache.harmony.tests.java.lang.MathTest.NEXTAFTER_FD_START_CASES;

public class StrictMathTest extends junit.framework.TestCase {

    private static final double HYP = StrictMath.sqrt(2.0);

    private static final double OPP = 1.0;

    private static final double ADJ = 1.0;

    /* Required to make previous preprocessor flags work - do not remove */
    int unused = 0;

    /**
     * java.lang.StrictMath#abs(double)
     */
    public void test_absD() {
        // Test for method double java.lang.StrictMath.abs(double)

        assertTrue("Incorrect double abs value",
                (StrictMath.abs(-1908.8976) == 1908.8976));
        assertTrue("Incorrect double abs value",
                (StrictMath.abs(1908.8976) == 1908.8976));
    }

    /**
     * java.lang.StrictMath#abs(float)
     */
    public void test_absF() {
        // Test for method float java.lang.StrictMath.abs(float)
        assertTrue("Incorrect float abs value",
                (StrictMath.abs(-1908.8976f) == 1908.8976f));
        assertTrue("Incorrect float abs value",
                (StrictMath.abs(1908.8976f) == 1908.8976f));
    }

    /**
     * java.lang.StrictMath#abs(int)
     */
    public void test_absI() {
        // Test for method int java.lang.StrictMath.abs(int)
        assertTrue("Incorrect int abs value",
                (StrictMath.abs(-1908897) == 1908897));
        assertTrue("Incorrect int abs value",
                (StrictMath.abs(1908897) == 1908897));
    }

    /**
     * java.lang.StrictMath#abs(long)
     */
    public void test_absJ() {
        // Test for method long java.lang.StrictMath.abs(long)
        assertTrue("Incorrect long abs value", (StrictMath
                .abs(-19088976000089L) == 19088976000089L));
        assertTrue("Incorrect long abs value",
                (StrictMath.abs(19088976000089L) == 19088976000089L));
    }

    /**
     * java.lang.StrictMath#acos(double)
     */
    public void test_acosD() {
        // Test for method double java.lang.StrictMath.acos(double)
        assertTrue("Returned incorrect arc cosine", StrictMath.cos(StrictMath
                .acos(ADJ / HYP)) == ADJ / HYP);
    }

    /**
     * java.lang.StrictMath#asin(double)
     */
    public void test_asinD() {
        // Test for method double java.lang.StrictMath.asin(double)
        assertTrue("Returned incorrect arc sine", StrictMath.sin(StrictMath
                .asin(OPP / HYP)) == OPP / HYP);
    }

    /**
     * java.lang.StrictMath#atan(double)
     */
    public void test_atanD() {
        // Test for method double java.lang.StrictMath.atan(double)
        double answer = StrictMath.tan(StrictMath.atan(1.0));
        assertTrue("Returned incorrect arc tangent: " + answer, answer <= 1.0
                && answer >= 9.9999999999999983E-1);
    }

    /**
     * java.lang.StrictMath#atan2(double, double)
     */
    public void test_atan2DD() {
        // Test for method double java.lang.StrictMath.atan2(double, double)
        double answer = StrictMath.atan(StrictMath.tan(1.0));
        assertTrue("Returned incorrect arc tangent: " + answer, answer <= 1.0
                && answer >= 9.9999999999999983E-1);
    }

    /**
     * java.lang.StrictMath#cbrt(double)
     */
    @SuppressWarnings("boxing")
    public void test_cbrt_D() {
        // Test for special situations
        assertTrue("Should return Double.NaN", Double.isNaN(StrictMath
                .cbrt(Double.NaN)));
        assertEquals("Should return Double.POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, StrictMath
                .cbrt(Double.POSITIVE_INFINITY));
        assertEquals("Should return Double.NEGATIVE_INFINITY",
                Double.NEGATIVE_INFINITY, StrictMath
                .cbrt(Double.NEGATIVE_INFINITY));
        assertEquals(Double.doubleToLongBits(0.0), Double
                .doubleToLongBits(StrictMath.cbrt(0.0)));
        assertEquals(Double.doubleToLongBits(+0.0), Double
                .doubleToLongBits(StrictMath.cbrt(+0.0)));
        assertEquals(Double.doubleToLongBits(-0.0), Double
                .doubleToLongBits(StrictMath.cbrt(-0.0)));

        assertEquals("Should return 3.0", 3.0, StrictMath.cbrt(27.0));
        assertEquals("Should return 23.111993172558684", 23.111993172558684,
                StrictMath.cbrt(12345.6));
        assertEquals("Should return 5.643803094122362E102",
                5.643803094122362E102, StrictMath.cbrt(Double.MAX_VALUE));
        assertEquals("Should return 0.01", 0.01, StrictMath.cbrt(0.000001));

        assertEquals("Should return -3.0", -3.0, StrictMath.cbrt(-27.0));
        assertEquals("Should return -23.111993172558684", -23.111993172558684,
                StrictMath.cbrt(-12345.6));
        assertEquals("Should return 1.7031839360032603E-108",
                1.7031839360032603E-108, StrictMath.cbrt(Double.MIN_VALUE));
        assertEquals("Should return -0.01", -0.01, StrictMath.cbrt(-0.000001));

        try {
            StrictMath.cbrt((Double) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            //expected
        }
    }

    /**
     * java.lang.StrictMath#ceil(double)
     */
    public void test_ceilD() {
        // Test for method double java.lang.StrictMath.ceil(double)
        assertEquals("Incorrect ceiling for double",
                79, StrictMath.ceil(78.89), 0.0);
        assertEquals("Incorrect ceiling for double",
                -78, StrictMath.ceil(-78.89), 0.0);
    }

    /**
     * {@link java.lang.StrictMath#copySign(double, double)}
     * @since 1.6
     */
    @SuppressWarnings("boxing")
    public void test_copySign_DD() {
        for (int i = 0; i < COPYSIGN_DD_CASES.length; i++) {
            final double magnitude = COPYSIGN_DD_CASES[i];
            final long absMagnitudeBits = Double.doubleToLongBits(StrictMath
                    .abs(magnitude));
            final long negMagnitudeBits = Double.doubleToLongBits(-StrictMath
                    .abs(magnitude));

            // cases for NaN
            assertEquals("If the sign is NaN, the result should be positive.",
                    absMagnitudeBits, Double.doubleToLongBits(StrictMath
                    .copySign(magnitude, Double.NaN)));
            assertTrue("The result should be NaN.", Double.isNaN(StrictMath
                    .copySign(Double.NaN, magnitude)));

            for (int j = 0; j < COPYSIGN_DD_CASES.length; j++) {
                final double sign = COPYSIGN_DD_CASES[j];
                final long resultBits = Double.doubleToLongBits(StrictMath
                        .copySign(magnitude, sign));

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

        assertTrue("The result should be NaN.", Double.isNaN(StrictMath
                .copySign(Double.NaN, Double.NaN)));

        try {
            StrictMath.copySign((Double) null, 2.3);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            StrictMath.copySign(2.3, (Double) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            StrictMath.copySign((Double) null, (Double) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }

        double d = Double.longBitsToDouble(0xfff8000000000000L);
        assertEquals(1.0, StrictMath.copySign(1.0, d), 0d);
    }

    /**
     * {@link java.lang.StrictMath#copySign(float, float)}
     * @since 1.6
     */
    @SuppressWarnings("boxing")
    public void test_copySign_FF() {
        for (int i = 0; i < COPYSIGN_FF_CASES.length; i++) {
            final float magnitude = COPYSIGN_FF_CASES[i];
            final int absMagnitudeBits = Float.floatToIntBits(StrictMath
                    .abs(magnitude));
            final int negMagnitudeBits = Float.floatToIntBits(-StrictMath
                    .abs(magnitude));

            // cases for NaN
            assertEquals("If the sign is NaN, the result should be positive.",
                    absMagnitudeBits, Float.floatToIntBits(StrictMath.copySign(
                    magnitude, Float.NaN)));
            assertTrue("The result should be NaN.", Float.isNaN(StrictMath
                    .copySign(Float.NaN, magnitude)));

            for (int j = 0; j < COPYSIGN_FF_CASES.length; j++) {
                final float sign = COPYSIGN_FF_CASES[j];
                final int resultBits = Float.floatToIntBits(StrictMath
                        .copySign(magnitude, sign));
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

        assertTrue("The result should be NaN.", Float.isNaN(StrictMath
                .copySign(Float.NaN, Float.NaN)));

        try {
            StrictMath.copySign((Float) null, 2.3f);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            StrictMath.copySign(2.3f, (Float) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            StrictMath.copySign((Float) null, (Float) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }

        float f = Float.intBitsToFloat(0xffc00000);
        assertEquals(1.0f, StrictMath.copySign(1.0f, f), 0f);
    }

    /**
     * java.lang.StrictMath#cos(double)
     */
    public void test_cosD() {
        // Test for method double java.lang.StrictMath.cos(double)

        assertTrue("Returned incorrect cosine", StrictMath.cos(StrictMath
                .acos(ADJ / HYP)) == ADJ / HYP);
    }

    /**
     * java.lang.StrictMath#cosh(double)
     */
    @SuppressWarnings("boxing")
    public void test_cosh_D() {
        // Test for special situations        
        assertTrue("Should return NaN", Double.isNaN(StrictMath
                .cosh(Double.NaN)));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, StrictMath
                .cosh(Double.POSITIVE_INFINITY));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, StrictMath
                .cosh(Double.NEGATIVE_INFINITY));
        assertEquals("Should return 1.0", 1.0, StrictMath.cosh(+0.0));
        assertEquals("Should return 1.0", 1.0, StrictMath.cosh(-0.0));

        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, StrictMath.cosh(1234.56));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, StrictMath.cosh(-1234.56));
        assertEquals("Should return 1.0000000000005", 1.0000000000005,
                StrictMath.cosh(0.000001));
        assertEquals("Should return 1.0000000000005", 1.0000000000005,
                StrictMath.cosh(-0.000001));
        assertEquals("Should return 5.212214351945598", 5.212214351945598,
                StrictMath.cosh(2.33482));

        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, StrictMath.cosh(Double.MAX_VALUE));
        assertEquals("Should return 1.0", 1.0, StrictMath
                .cosh(Double.MIN_VALUE));
    }

    /**
     * java.lang.StrictMath#exp(double)
     */
    public void test_expD() {
        // Test for method double java.lang.StrictMath.exp(double)
        assertTrue("Incorrect answer returned for simple power", StrictMath
                .abs(StrictMath.exp(4D) - StrictMath.E * StrictMath.E
                        * StrictMath.E * StrictMath.E) < 0.1D);
        assertTrue("Incorrect answer returned for larger power", StrictMath
                .log(StrictMath.abs(StrictMath.exp(5.5D)) - 5.5D) < 10.0D);
    }

    /**
     * java.lang.StrictMath#expm1(double)
     */
    @SuppressWarnings("boxing")
    public void test_expm1_D() {
        //Test for special cases        
        assertTrue("Should return NaN", Double.isNaN(StrictMath.expm1(Double.NaN)));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, StrictMath.expm1(Double.POSITIVE_INFINITY));
        assertEquals("Should return -1.0", -1.0, StrictMath
                .expm1(Double.NEGATIVE_INFINITY));
        assertEquals(Double.doubleToLongBits(0.0), Double
                .doubleToLongBits(StrictMath.expm1(0.0)));
        assertEquals(Double.doubleToLongBits(+0.0), Double
                .doubleToLongBits(StrictMath.expm1(+0.0)));
        assertEquals(Double.doubleToLongBits(-0.0), Double
                .doubleToLongBits(StrictMath.expm1(-0.0)));

        assertEquals("Should return -9.999950000166666E-6",
                -9.999950000166666E-6, StrictMath.expm1(-0.00001));
        assertEquals("Should return 1.0145103074469635E60",
                1.0145103074469635E60, StrictMath.expm1(138.16951162));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, StrictMath
                .expm1(123456789123456789123456789.4521584223));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, StrictMath.expm1(Double.MAX_VALUE));
        assertEquals("Should return MIN_VALUE", Double.MIN_VALUE, StrictMath
                .expm1(Double.MIN_VALUE));

    }

    /**
     * java.lang.StrictMath#floor(double)
     */
    public void test_floorD() {
        // Test for method double java.lang.StrictMath.floor(double)
        assertEquals("Incorrect floor for double",
                78, StrictMath.floor(78.89), 0.0);
        assertEquals("Incorrect floor for double",
                -79, StrictMath.floor(-78.89), 0.0);
    }

    /**
     * {@link java.lang.StrictMath#getExponent(double)}
     * @since 1.6
     */
    @SuppressWarnings("boxing")
    public void test_getExponent_D() {
        for (int i = 0; i < GETEXPONENT_D_CASES.length; i++) {
            final double number = GETEXPONENT_D_CASES[i];
            final int result = GETEXPONENT_D_RESULTS[i];
            assertEquals("Wrong result of getExponent(double).", result,
                    StrictMath.getExponent(number));
        }

        try {
            StrictMath.getExponent((Double) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * {@link java.lang.StrictMath#getExponent(float)}
     * @since 1.6
     */
    @SuppressWarnings("boxing")
    public void test_getExponent_F() {
        for (int i = 0; i < GETEXPONENT_F_CASES.length; i++) {
            final float number = GETEXPONENT_F_CASES[i];
            final int result = GETEXPONENT_F_RESULTS[i];
            assertEquals("Wrong result of getExponent(float).", result,
                    StrictMath.getExponent(number));
        }
        try {
            StrictMath.getExponent((Float) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * java.lang.StrictMath#hypot(double, double)
     */
    @SuppressWarnings("boxing")
    public void test_hypot_DD() {
        // Test for special cases
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, StrictMath.hypot(Double.POSITIVE_INFINITY,
                1.0));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, StrictMath.hypot(Double.NEGATIVE_INFINITY,
                123.324));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, StrictMath.hypot(-758.2587,
                Double.POSITIVE_INFINITY));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, StrictMath.hypot(5687.21,
                Double.NEGATIVE_INFINITY));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, StrictMath.hypot(Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, StrictMath.hypot(Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY));
        assertTrue("Should return NaN", Double.isNaN(StrictMath.hypot(Double.NaN,
                2342301.89843)));
        assertTrue("Should return NaN", Double.isNaN(StrictMath.hypot(-345.2680,
                Double.NaN)));

        assertEquals("Should return 2396424.905416697", 2396424.905416697, StrictMath
                .hypot(12322.12, -2396393.2258));
        assertEquals("Should return 138.16958070558556", 138.16958070558556,
                StrictMath.hypot(-138.16951162, 0.13817035864));
        assertEquals("Should return 1.7976931348623157E308",
                1.7976931348623157E308, StrictMath.hypot(Double.MAX_VALUE, 211370.35));
        assertEquals("Should return 5413.7185", 5413.7185, StrictMath.hypot(
                -5413.7185, Double.MIN_VALUE));

    }

    /**
     * java.lang.StrictMath#IEEEremainder(double, double)
     */
    public void test_IEEEremainderDD() {
        // Test for method double java.lang.StrictMath.IEEEremainder(double,
        // double)
        assertEquals("Incorrect remainder returned", 0.0, StrictMath.IEEEremainder(
                1.0, 1.0), 0.0);
        assertTrue(
                "Incorrect remainder returned",
                StrictMath.IEEEremainder(1.32, 89.765) >= 1.4705063220631647E-2
                        || StrictMath.IEEEremainder(1.32, 89.765) >= 1.4705063220631649E-2);
    }

    /**
     * java.lang.StrictMath#log(double)
     */
    public void test_logD() {
        // Test for method double java.lang.StrictMath.log(double)
        for (double d = 10; d >= -10; d -= 0.5) {
            double answer = StrictMath.log(StrictMath.exp(d));
            assertTrue("Answer does not equal expected answer for d = " + d
                    + " answer = " + answer,
                    StrictMath.abs(answer - d) <= StrictMath
                            .abs(d * 0.00000001));
        }
    }

    /**
     * java.lang.StrictMath#log10(double)
     */
    @SuppressWarnings("boxing")
    public void test_log10_D() {
        // Test for special cases        
        assertTrue("Should return NaN", Double.isNaN(StrictMath
                .log10(Double.NaN)));
        assertTrue("Should return NaN", Double.isNaN(StrictMath
                .log10(-2541.05745687234187532)));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, StrictMath
                .log10(Double.POSITIVE_INFINITY));
        assertEquals("Should return NEGATIVE_INFINITY",
                Double.NEGATIVE_INFINITY, StrictMath.log10(0.0));
        assertEquals("Should return NEGATIVE_INFINITY",
                Double.NEGATIVE_INFINITY, StrictMath.log10(+0.0));
        assertEquals("Should return NEGATIVE_INFINITY",
                Double.NEGATIVE_INFINITY, StrictMath.log10(-0.0));
        assertEquals("Should return 14.0", 14.0, StrictMath.log10(StrictMath
                .pow(10, 14)));

        assertEquals("Should return 3.7389561269540406", 3.7389561269540406,
                StrictMath.log10(5482.2158));
        assertEquals("Should return 14.661551142893833", 14.661551142893833,
                StrictMath.log10(458723662312872.125782332587));
        assertEquals("Should return -0.9083828622192334", -0.9083828622192334,
                StrictMath.log10(0.12348583358871));
        assertEquals("Should return 308.25471555991675", 308.25471555991675,
                StrictMath.log10(Double.MAX_VALUE));
        assertEquals("Should return -323.3062153431158", -323.3062153431158,
                StrictMath.log10(Double.MIN_VALUE));
    }

    /**
     * java.lang.StrictMath#log1p(double)
     */
    @SuppressWarnings("boxing")
    public void test_log1p_D() {
        // Test for special cases
        assertTrue("Should return NaN", Double.isNaN(StrictMath
                .log1p(Double.NaN)));
        assertTrue("Should return NaN", Double.isNaN(StrictMath
                .log1p(-32.0482175)));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, StrictMath
                .log1p(Double.POSITIVE_INFINITY));
        assertEquals(Double.doubleToLongBits(0.0), Double
                .doubleToLongBits(StrictMath.log1p(0.0)));
        assertEquals(Double.doubleToLongBits(+0.0), Double
                .doubleToLongBits(StrictMath.log1p(+0.0)));
        assertEquals(Double.doubleToLongBits(-0.0), Double
                .doubleToLongBits(StrictMath.log1p(-0.0)));

        assertEquals("Should return -0.2941782295312541", -0.2941782295312541,
                StrictMath.log1p(-0.254856327));
        assertEquals("Should return 7.368050685564151", 7.368050685564151,
                StrictMath.log1p(1583.542));
        assertEquals("Should return 0.4633708685409921", 0.4633708685409921,
                StrictMath.log1p(0.5894227));
        assertEquals("Should return 709.782712893384", 709.782712893384,
                StrictMath.log1p(Double.MAX_VALUE));
        assertEquals("Should return Double.MIN_VALUE", Double.MIN_VALUE,
                StrictMath.log1p(Double.MIN_VALUE));
    }

    /**
     * java.lang.StrictMath#max(double, double)
     */
    public void test_maxDD() {
        // Test for method double java.lang.StrictMath.max(double, double)
        assertEquals("Incorrect double max value", 1908897.6000089, StrictMath.max(
                -1908897.6000089, 1908897.6000089), 0D);
        assertEquals("Incorrect double max value", 1908897.6000089, StrictMath.max(2.0,
                1908897.6000089), 0D);
        assertEquals("Incorrect double max value", -2.0, StrictMath.max(-2.0,
                -1908897.6000089), 0D);

    }

    /**
     * java.lang.StrictMath#max(float, float)
     */
    public void test_maxFF() {
        // Test for method float java.lang.StrictMath.max(float, float)
        assertTrue("Incorrect float max value", StrictMath.max(-1908897.600f,
                1908897.600f) == 1908897.600f);
        assertTrue("Incorrect float max value", StrictMath.max(2.0f,
                1908897.600f) == 1908897.600f);
        assertTrue("Incorrect float max value", StrictMath.max(-2.0f,
                -1908897.600f) == -2.0f);
    }

    /**
     * java.lang.StrictMath#max(int, int)
     */
    public void test_maxII() {
        // Test for method int java.lang.StrictMath.max(int, int)
        assertEquals("Incorrect int max value", 19088976, StrictMath.max(-19088976,
                19088976));
        assertEquals("Incorrect int max value",
                19088976, StrictMath.max(20, 19088976));
        assertEquals("Incorrect int max value",
                -20, StrictMath.max(-20, -19088976));
    }

    /**
     * java.lang.StrictMath#max(long, long)
     */
    public void test_maxJJ() {
        // Test for method long java.lang.StrictMath.max(long, long)
        assertEquals("Incorrect long max value", 19088976000089L, StrictMath.max(-19088976000089L,
                19088976000089L));
        assertEquals("Incorrect long max value", 19088976000089L, StrictMath.max(20,
                19088976000089L));
        assertEquals("Incorrect long max value", -20, StrictMath.max(-20,
                -19088976000089L));
    }

    /**
     * java.lang.StrictMath#min(double, double)
     */
    public void test_minDD() {
        // Test for method double java.lang.StrictMath.min(double, double)
        assertEquals("Incorrect double min value", -1908897.6000089, StrictMath.min(
                -1908897.6000089, 1908897.6000089), 0D);
        assertEquals("Incorrect double min value", 2.0, StrictMath.min(2.0,
                1908897.6000089), 0D);
        assertEquals("Incorrect double min value", -1908897.6000089, StrictMath.min(-2.0,
                -1908897.6000089), 0D);
    }

    /**
     * java.lang.StrictMath#min(float, float)
     */
    public void test_minFF() {
        // Test for method float java.lang.StrictMath.min(float, float)
        assertTrue("Incorrect float min value", StrictMath.min(-1908897.600f,
                1908897.600f) == -1908897.600f);
        assertTrue("Incorrect float min value", StrictMath.min(2.0f,
                1908897.600f) == 2.0f);
        assertTrue("Incorrect float min value", StrictMath.min(-2.0f,
                -1908897.600f) == -1908897.600f);
    }

    /**
     * java.lang.StrictMath#min(int, int)
     */
    public void test_minII() {
        // Test for method int java.lang.StrictMath.min(int, int)
        assertEquals("Incorrect int min value", -19088976, StrictMath.min(-19088976,
                19088976));
        assertEquals("Incorrect int min value",
                20, StrictMath.min(20, 19088976));
        assertEquals("Incorrect int min value",
                -19088976, StrictMath.min(-20, -19088976));

    }

    /**
     * java.lang.StrictMath#min(long, long)
     */
    public void test_minJJ() {
        // Test for method long java.lang.StrictMath.min(long, long)
        assertEquals("Incorrect long min value", -19088976000089L, StrictMath.min(-19088976000089L,
                19088976000089L));
        assertEquals("Incorrect long min value", 20, StrictMath.min(20,
                19088976000089L));
        assertEquals("Incorrect long min value", -19088976000089L, StrictMath.min(-20,
                -19088976000089L));
    }

    /**
     * {@link java.lang.StrictMath#nextAfter(double, double)}
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
                final long resultBits = Double.doubleToLongBits(StrictMath
                        .nextAfter(start, direction));
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
            assertTrue("The result should be NaN.", Double.isNaN(StrictMath
                    .nextAfter(NEXTAFTER_DD_START_CASES[i][0], Double.NaN)));
        }
        for (int i = 0; i < NEXTAFTER_DD_FD_DIRECTION_CASES.length; i++) {
            assertTrue("The result should be NaN.", Double.isNaN(StrictMath
                    .nextAfter(Double.NaN, NEXTAFTER_DD_FD_DIRECTION_CASES[i])));
        }
        assertTrue("The result should be NaN.", Double.isNaN(StrictMath
                .nextAfter(Double.NaN, Double.NaN)));

        // test for exception
        try {
            StrictMath.nextAfter((Double) null, 2.3);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            StrictMath.nextAfter(2.3, (Double) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            StrictMath.nextAfter((Double) null, (Double) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * {@link java.lang.StrictMath#nextAfter(float, double)}
     * @since 1.6
     */
    @SuppressWarnings("boxing")
    public void test_nextAfter_FD() {
        // test for most cases without exception
        for (int i = 0; i < NEXTAFTER_FD_START_CASES.length; i++) {
            final float start = NEXTAFTER_FD_START_CASES[i][0];
            final int nextUpBits = Float
                    .floatToIntBits(NEXTAFTER_FD_START_CASES[i][1]);
            final int nextDownBits = Float
                    .floatToIntBits(NEXTAFTER_FD_START_CASES[i][2]);

            for (int j = 0; j < NEXTAFTER_DD_FD_DIRECTION_CASES.length; j++) {
                final double direction = NEXTAFTER_DD_FD_DIRECTION_CASES[j];
                final int resultBits = Float.floatToIntBits(StrictMath
                        .nextAfter(start, direction));
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
            assertTrue("The result should be NaN.", Float.isNaN(StrictMath
                    .nextAfter(NEXTAFTER_FD_START_CASES[i][0], Float.NaN)));
        }
        for (int i = 0; i < NEXTAFTER_DD_FD_DIRECTION_CASES.length; i++) {
            assertTrue("The result should be NaN.", Float.isNaN(StrictMath
                    .nextAfter(Float.NaN, NEXTAFTER_DD_FD_DIRECTION_CASES[i])));
        }
        assertTrue("The result should be NaN.", Float.isNaN(StrictMath
                .nextAfter(Float.NaN, Float.NaN)));

        // test for exception
        try {
            StrictMath.nextAfter((Float) null, 2.3);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            StrictMath.nextAfter(2.3, (Float) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            StrictMath.nextAfter((Float) null, (Float) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * {@link java.lang.StrictMath#nextUp(double)}
     * @since 1.6
     */
    @SuppressWarnings("boxing")
    public void test_nextUp_D() {
        // This method is semantically equivalent to nextAfter(d,
        // Double.POSITIVE_INFINITY),
        // so we use the data of test_nextAfter_DD
        for (int i = 0; i < NEXTAFTER_DD_START_CASES.length; i++) {
            final double start = NEXTAFTER_DD_START_CASES[i][0];
            final long nextUpBits = Double
                    .doubleToLongBits(NEXTAFTER_DD_START_CASES[i][1]);
            final long resultBits = Double.doubleToLongBits(StrictMath
                    .nextUp(start));
            assertEquals("Result should be next up-number.", nextUpBits,
                    resultBits);
        }

        // test for cases with NaN
        assertTrue("The result should be NaN.", Double.isNaN(StrictMath
                .nextUp(Double.NaN)));

        // test for exception
        try {
            StrictMath.nextUp((Double) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * {@link java.lang.StrictMath#nextUp(float)}
     * @since 1.6
     */
    @SuppressWarnings("boxing")
    public void test_nextUp_F() {
        // This method is semantically equivalent to nextAfter(f,
        // Float.POSITIVE_INFINITY),
        // so we use the data of test_nextAfter_FD
        for (int i = 0; i < NEXTAFTER_FD_START_CASES.length; i++) {
            final float start = NEXTAFTER_FD_START_CASES[i][0];
            final int nextUpBits = Float
                    .floatToIntBits(NEXTAFTER_FD_START_CASES[i][1]);
            final int resultBits = Float.floatToIntBits(StrictMath
                    .nextUp(start));
            assertEquals("Result should be next up-number.", nextUpBits,
                    resultBits);
        }

        // test for cases with NaN
        assertTrue("The result should be NaN.", Float.isNaN(StrictMath
                .nextUp(Float.NaN)));

        // test for exception
        try {
            StrictMath.nextUp((Float) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * java.lang.StrictMath#pow(double, double)
     */
    public void test_powDD() {
        // Test for method double java.lang.StrictMath.pow(double, double)
        assertTrue("pow returned incorrect value",
                (long) StrictMath.pow(2, 8) == 256l);
        assertTrue("pow returned incorrect value",
                StrictMath.pow(2, -8) == 0.00390625d);
    }

    /**
     * java.lang.StrictMath#rint(double)
     */
    public void test_rintD() {
        // Test for method double java.lang.StrictMath.rint(double)
        assertEquals("Failed to round properly - up to odd",
                3.0, StrictMath.rint(2.9), 0D);
        assertTrue("Failed to round properly - NaN", Double.isNaN(StrictMath
                .rint(Double.NaN)));
        assertEquals("Failed to round properly down  to even", 2.0, StrictMath
                .rint(2.1), 0D);
        assertTrue("Failed to round properly " + 2.5 + " to even", StrictMath
                .rint(2.5) == 2.0);
    }

    /**
     * java.lang.StrictMath#round(double)
     */
    public void test_roundD() {
        // Test for method long java.lang.StrictMath.round(double)
        assertEquals("Incorrect rounding of a float",
                -91, StrictMath.round(-90.89d));
    }

    /**
     * java.lang.StrictMath#round(float)
     */
    public void test_roundF() {
        // Test for method int java.lang.StrictMath.round(float)
        assertEquals("Incorrect rounding of a float",
                -91, StrictMath.round(-90.89f));
    }

    /**
     * {@link java.lang.StrictMath#scalb(double, int)}
     * @since 1.6
     */
    @SuppressWarnings("boxing")
    public void test_scalb_DI() {
        // result is normal
        assertEquals(4.1422946304E7, StrictMath.scalb(1.2345, 25));
        assertEquals(3.679096698760986E-8, StrictMath.scalb(1.2345, -25));
        assertEquals(1.2345, StrictMath.scalb(1.2345, 0));
        assertEquals(7868514.304, StrictMath.scalb(0.2345, 25));

        double normal = StrictMath.scalb(0.2345, -25);
        assertEquals(6.98864459991455E-9, normal);
        // precision kept
        assertEquals(0.2345, StrictMath.scalb(normal, 25));

        assertEquals(0.2345, StrictMath.scalb(0.2345, 0));
        assertEquals(-4.1422946304E7, StrictMath.scalb(-1.2345, 25));
        assertEquals(-6.98864459991455E-9, StrictMath.scalb(-0.2345, -25));
        assertEquals(2.0, StrictMath.scalb(Double.MIN_NORMAL / 2, 1024));
        assertEquals(64.0, StrictMath.scalb(Double.MIN_VALUE, 1080));
        assertEquals(234, StrictMath.getExponent(StrictMath.scalb(1.0, 234)));
        assertEquals(3.9999999999999996, StrictMath.scalb(Double.MAX_VALUE,
                Double.MIN_EXPONENT));

        // result is near infinity
        double halfMax = StrictMath.scalb(1.0, Double.MAX_EXPONENT);
        assertEquals(8.98846567431158E307, halfMax);
        assertEquals(Double.MAX_VALUE, halfMax - StrictMath.ulp(halfMax)
                + halfMax);
        assertEquals(Double.POSITIVE_INFINITY, halfMax + halfMax);
        assertEquals(1.7976931348623155E308, StrictMath.scalb(1.0 - StrictMath
                .ulp(1.0), Double.MAX_EXPONENT + 1));
        assertEquals(Double.POSITIVE_INFINITY, StrictMath.scalb(
                1.0 - StrictMath.ulp(1.0), Double.MAX_EXPONENT + 2));

        halfMax = StrictMath.scalb(-1.0, Double.MAX_EXPONENT);
        assertEquals(-8.98846567431158E307, halfMax);
        assertEquals(-Double.MAX_VALUE, halfMax + StrictMath.ulp(halfMax)
                + halfMax);
        assertEquals(Double.NEGATIVE_INFINITY, halfMax + halfMax);

        assertEquals(Double.POSITIVE_INFINITY, StrictMath.scalb(0.345, 1234));
        assertEquals(Double.POSITIVE_INFINITY, StrictMath
                .scalb(44.345E102, 934));
        assertEquals(Double.NEGATIVE_INFINITY, StrictMath.scalb(-44.345E102,
                934));

        assertEquals(Double.POSITIVE_INFINITY, StrictMath.scalb(
                Double.MIN_NORMAL / 2, 4000));
        assertEquals(Double.POSITIVE_INFINITY, StrictMath.scalb(
                Double.MIN_VALUE, 8000));
        assertEquals(Double.POSITIVE_INFINITY, StrictMath.scalb(
                Double.MAX_VALUE, 1));
        assertEquals(Double.POSITIVE_INFINITY, StrictMath.scalb(
                Double.POSITIVE_INFINITY, 0));
        assertEquals(Double.POSITIVE_INFINITY, StrictMath.scalb(
                Double.POSITIVE_INFINITY, -1));
        assertEquals(Double.NEGATIVE_INFINITY, StrictMath.scalb(
                Double.NEGATIVE_INFINITY, -1));
        assertEquals(Double.NEGATIVE_INFINITY, StrictMath.scalb(
                Double.NEGATIVE_INFINITY, Double.MIN_EXPONENT));

        // result is subnormal/zero
        long posZeroBits = Double.doubleToLongBits(+0.0);
        long negZeroBits = Double.doubleToLongBits(-0.0);
        assertEquals(posZeroBits, Double.doubleToLongBits(StrictMath.scalb(
                +0.0, Integer.MAX_VALUE)));
        assertEquals(posZeroBits, Double.doubleToLongBits(StrictMath.scalb(
                +0.0, -123)));
        assertEquals(posZeroBits, Double.doubleToLongBits(StrictMath.scalb(
                +0.0, 0)));
        assertEquals(negZeroBits, Double.doubleToLongBits(StrictMath.scalb(
                -0.0, 123)));
        assertEquals(negZeroBits, Double.doubleToLongBits(StrictMath.scalb(
                -0.0, Integer.MIN_VALUE)));

        assertEquals(Double.MIN_VALUE, StrictMath.scalb(1.0, -1074));
        assertEquals(posZeroBits, Double.doubleToLongBits(StrictMath.scalb(1.0,
                -1075)));
        assertEquals(negZeroBits, Double.doubleToLongBits(StrictMath.scalb(
                -1.0, -1075)));

        // precision lost
        assertEquals(StrictMath.scalb(21.405, -1078), StrictMath.scalb(21.405,
                -1079));
        assertEquals(Double.MIN_VALUE, StrictMath.scalb(21.405, -1079));
        assertEquals(-Double.MIN_VALUE, StrictMath.scalb(-21.405, -1079));
        assertEquals(posZeroBits, Double.doubleToLongBits(StrictMath.scalb(
                21.405, -1080)));
        assertEquals(negZeroBits, Double.doubleToLongBits(StrictMath.scalb(
                -21.405, -1080)));
        assertEquals(posZeroBits, Double.doubleToLongBits(StrictMath.scalb(
                Double.MIN_VALUE, -1)));
        assertEquals(negZeroBits, Double.doubleToLongBits(StrictMath.scalb(
                -Double.MIN_VALUE, -1)));
        assertEquals(Double.MIN_VALUE, StrictMath.scalb(Double.MIN_NORMAL, -52));
        assertEquals(posZeroBits, Double.doubleToLongBits(StrictMath.scalb(
                Double.MIN_NORMAL, -53)));
        assertEquals(negZeroBits, Double.doubleToLongBits(StrictMath.scalb(
                -Double.MIN_NORMAL, -53)));
        assertEquals(Double.MIN_VALUE, StrictMath
                .scalb(Double.MAX_VALUE, -2098));
        assertEquals(posZeroBits, Double.doubleToLongBits(StrictMath.scalb(
                Double.MAX_VALUE, -2099)));
        assertEquals(negZeroBits, Double.doubleToLongBits(StrictMath.scalb(
                -Double.MAX_VALUE, -2099)));
        assertEquals(Double.MIN_VALUE, StrictMath.scalb(Double.MIN_NORMAL / 3,
                -51));
        assertEquals(posZeroBits, Double.doubleToLongBits(StrictMath.scalb(
                Double.MIN_NORMAL / 3, -52)));
        assertEquals(negZeroBits, Double.doubleToLongBits(StrictMath.scalb(
                -Double.MIN_NORMAL / 3, -52)));
        double subnormal = StrictMath.scalb(Double.MIN_NORMAL / 3, -25);
        assertEquals(2.2104123E-316, subnormal);
        // precision lost
        assertFalse(Double.MIN_NORMAL / 3 == StrictMath.scalb(subnormal, 25));

        // NaN
        assertTrue(Double.isNaN(StrictMath.scalb(Double.NaN, 1)));
        assertTrue(Double.isNaN(StrictMath.scalb(Double.NaN, 0)));
        assertTrue(Double.isNaN(StrictMath.scalb(Double.NaN, -120)));

        assertEquals(1283457024, Double.doubleToLongBits(StrictMath.scalb(
                Double.MIN_VALUE * 153, 23)));
        assertEquals(-9223372035571318784L, Double.doubleToLongBits(StrictMath
                .scalb(-Double.MIN_VALUE * 153, 23)));
        assertEquals(36908406321184768L, Double.doubleToLongBits(StrictMath
                .scalb(Double.MIN_VALUE * 153, 52)));
        assertEquals(-9186463630533591040L, Double.doubleToLongBits(StrictMath
                .scalb(-Double.MIN_VALUE * 153, 52)));

        // test for exception
        try {
            StrictMath.scalb((Double) null, (Integer) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            StrictMath.scalb(1.0, (Integer) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            StrictMath.scalb((Double) null, 1);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * {@link java.lang.StrictMath#scalb(float, int)}
     * @since 1.6
     */
    @SuppressWarnings("boxing")
    public void test_scalb_FI() {
        // result is normal
        assertEquals(4.1422946304E7f, StrictMath.scalb(1.2345f, 25));
        assertEquals(3.679096698760986E-8f, StrictMath.scalb(1.2345f, -25));
        assertEquals(1.2345f, StrictMath.scalb(1.2345f, 0));
        assertEquals(7868514.304f, StrictMath.scalb(0.2345f, 25));

        float normal = StrictMath.scalb(0.2345f, -25);
        assertEquals(6.98864459991455E-9f, normal);
        // precision kept
        assertEquals(0.2345f, StrictMath.scalb(normal, 25));

        assertEquals(0.2345f, StrictMath.scalb(0.2345f, 0));
        assertEquals(-4.1422946304E7f, StrictMath.scalb(-1.2345f, 25));
        assertEquals(-6.98864459991455E-9f, StrictMath.scalb(-0.2345f, -25));
        assertEquals(2.0f, StrictMath.scalb(Float.MIN_NORMAL / 2, 128));
        assertEquals(64.0f, StrictMath.scalb(Float.MIN_VALUE, 155));
        assertEquals(34, StrictMath.getExponent(StrictMath.scalb(1.0f, 34)));
        assertEquals(3.9999998f, StrictMath.scalb(Float.MAX_VALUE,
                Float.MIN_EXPONENT));

        // result is near infinity
        float halfMax = StrictMath.scalb(1.0f, Float.MAX_EXPONENT);
        assertEquals(1.7014118E38f, halfMax);
        assertEquals(Float.MAX_VALUE, halfMax - StrictMath.ulp(halfMax)
                + halfMax);
        assertEquals(Float.POSITIVE_INFINITY, halfMax + halfMax);
        assertEquals(3.4028233E38f, StrictMath.scalb(1.0f - StrictMath
                .ulp(1.0f), Float.MAX_EXPONENT + 1));
        assertEquals(Float.POSITIVE_INFINITY, StrictMath.scalb(
                1.0f - StrictMath.ulp(1.0f), Float.MAX_EXPONENT + 2));

        halfMax = StrictMath.scalb(-1.0f, Float.MAX_EXPONENT);
        assertEquals(-1.7014118E38f, halfMax);
        assertEquals(-Float.MAX_VALUE, halfMax + StrictMath.ulp(halfMax)
                + halfMax);
        assertEquals(Float.NEGATIVE_INFINITY, halfMax + halfMax);

        assertEquals(Float.POSITIVE_INFINITY, StrictMath.scalb(0.345f, 1234));
        assertEquals(Float.POSITIVE_INFINITY, StrictMath.scalb(44.345E10f, 934));
        assertEquals(Float.NEGATIVE_INFINITY, StrictMath
                .scalb(-44.345E10f, 934));

        assertEquals(Float.POSITIVE_INFINITY, StrictMath.scalb(
                Float.MIN_NORMAL / 2, 400));
        assertEquals(Float.POSITIVE_INFINITY, StrictMath.scalb(Float.MIN_VALUE,
                800));
        assertEquals(Float.POSITIVE_INFINITY, StrictMath.scalb(Float.MAX_VALUE,
                1));
        assertEquals(Float.POSITIVE_INFINITY, StrictMath.scalb(
                Float.POSITIVE_INFINITY, 0));
        assertEquals(Float.POSITIVE_INFINITY, StrictMath.scalb(
                Float.POSITIVE_INFINITY, -1));
        assertEquals(Float.NEGATIVE_INFINITY, StrictMath.scalb(
                Float.NEGATIVE_INFINITY, -1));
        assertEquals(Float.NEGATIVE_INFINITY, StrictMath.scalb(
                Float.NEGATIVE_INFINITY, Float.MIN_EXPONENT));

        // result is subnormal/zero
        int posZeroBits = Float.floatToIntBits(+0.0f);
        int negZeroBits = Float.floatToIntBits(-0.0f);
        assertEquals(posZeroBits, Float.floatToIntBits(StrictMath.scalb(+0.0f,
                Integer.MAX_VALUE)));
        assertEquals(posZeroBits, Float.floatToIntBits(StrictMath.scalb(+0.0f,
                -123)));
        assertEquals(posZeroBits, Float.floatToIntBits(StrictMath.scalb(+0.0f,
                0)));
        assertEquals(negZeroBits, Float.floatToIntBits(StrictMath.scalb(-0.0f,
                123)));
        assertEquals(negZeroBits, Float.floatToIntBits(StrictMath.scalb(-0.0f,
                Integer.MIN_VALUE)));

        assertEquals(Float.MIN_VALUE, StrictMath.scalb(1.0f, -149));
        assertEquals(posZeroBits, Float.floatToIntBits(StrictMath.scalb(1.0f,
                -150)));
        assertEquals(negZeroBits, Float.floatToIntBits(StrictMath.scalb(-1.0f,
                -150)));

        // precision lost
        assertEquals(StrictMath.scalb(21.405f, -154), StrictMath.scalb(21.405f,
                -153));
        assertEquals(Float.MIN_VALUE, StrictMath.scalb(21.405f, -154));
        assertEquals(-Float.MIN_VALUE, StrictMath.scalb(-21.405f, -154));
        assertEquals(posZeroBits, Float.floatToIntBits(StrictMath.scalb(
                21.405f, -155)));
        assertEquals(negZeroBits, Float.floatToIntBits(StrictMath.scalb(
                -21.405f, -155)));
        assertEquals(posZeroBits, Float.floatToIntBits(StrictMath.scalb(
                Float.MIN_VALUE, -1)));
        assertEquals(negZeroBits, Float.floatToIntBits(StrictMath.scalb(
                -Float.MIN_VALUE, -1)));
        assertEquals(Float.MIN_VALUE, StrictMath.scalb(Float.MIN_NORMAL, -23));
        assertEquals(posZeroBits, Float.floatToIntBits(StrictMath.scalb(
                Float.MIN_NORMAL, -24)));
        assertEquals(negZeroBits, Float.floatToIntBits(StrictMath.scalb(
                -Float.MIN_NORMAL, -24)));
        assertEquals(Float.MIN_VALUE, StrictMath.scalb(Float.MAX_VALUE, -277));
        assertEquals(posZeroBits, Float.floatToIntBits(StrictMath.scalb(
                Float.MAX_VALUE, -278)));
        assertEquals(negZeroBits, Float.floatToIntBits(StrictMath.scalb(
                -Float.MAX_VALUE, -278)));
        assertEquals(Float.MIN_VALUE, StrictMath.scalb(Float.MIN_NORMAL / 3,
                -22));
        assertEquals(posZeroBits, Float.floatToIntBits(StrictMath.scalb(
                Float.MIN_NORMAL / 3, -23)));
        assertEquals(negZeroBits, Float.floatToIntBits(StrictMath.scalb(
                -Float.MIN_NORMAL / 3, -23)));
        float subnormal = StrictMath.scalb(Float.MIN_NORMAL / 3, -11);
        assertEquals(1.913E-42f, subnormal);
        // precision lost
        assertFalse(Float.MIN_NORMAL / 3 == StrictMath.scalb(subnormal, 11));

        assertEquals(68747264, Float.floatToIntBits(StrictMath.scalb(
                Float.MIN_VALUE * 153, 23)));
        assertEquals(-2078736384, Float.floatToIntBits(StrictMath.scalb(
                -Float.MIN_VALUE * 153, 23)));

        assertEquals(4896, Float.floatToIntBits(StrictMath.scalb(
                Float.MIN_VALUE * 153, 5)));
        assertEquals(-2147478752, Float.floatToIntBits(StrictMath.scalb(
                -Float.MIN_VALUE * 153, 5)));

        // NaN
        assertTrue(Float.isNaN(StrictMath.scalb(Float.NaN, 1)));
        assertTrue(Float.isNaN(StrictMath.scalb(Float.NaN, 0)));
        assertTrue(Float.isNaN(StrictMath.scalb(Float.NaN, -120)));

        // test for exception
        try {
            StrictMath.scalb((Float) null, (Integer) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            StrictMath.scalb(1.0f, (Integer) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            StrictMath.scalb((Float) null, 1);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    /**
     * java.lang.StrictMath#signum(double)
     */
    public void test_signum_D() {
        assertTrue(Double.isNaN(StrictMath.signum(Double.NaN)));
        assertEquals(Double.doubleToLongBits(0.0), Double
                .doubleToLongBits(StrictMath.signum(0.0)));
        assertEquals(Double.doubleToLongBits(+0.0), Double
                .doubleToLongBits(StrictMath.signum(+0.0)));
        assertEquals(Double.doubleToLongBits(-0.0), Double
                .doubleToLongBits(StrictMath.signum(-0.0)));

        assertEquals(1.0, StrictMath.signum(253681.2187962), 0D);
        assertEquals(-1.0, StrictMath.signum(-125874693.56), 0D);
        assertEquals(1.0, StrictMath.signum(1.2587E-308), 0D);
        assertEquals(-1.0, StrictMath.signum(-1.2587E-308), 0D);

        assertEquals(1.0, StrictMath.signum(Double.MAX_VALUE), 0D);
        assertEquals(1.0, StrictMath.signum(Double.MIN_VALUE), 0D);
        assertEquals(-1.0, StrictMath.signum(-Double.MAX_VALUE), 0D);
        assertEquals(-1.0, StrictMath.signum(-Double.MIN_VALUE), 0D);
        assertEquals(1.0, StrictMath.signum(Double.POSITIVE_INFINITY), 0D);
        assertEquals(-1.0, StrictMath.signum(Double.NEGATIVE_INFINITY), 0D);

    }

    /**
     * java.lang.StrictMath#signum(float)
     */
    public void test_signum_F() {
        assertTrue(Float.isNaN(StrictMath.signum(Float.NaN)));
        assertEquals(Float.floatToIntBits(0.0f), Float
                .floatToIntBits(StrictMath.signum(0.0f)));
        assertEquals(Float.floatToIntBits(+0.0f), Float
                .floatToIntBits(StrictMath.signum(+0.0f)));
        assertEquals(Float.floatToIntBits(-0.0f), Float
                .floatToIntBits(StrictMath.signum(-0.0f)));

        assertEquals(1.0f, StrictMath.signum(253681.2187962f), 0f);
        assertEquals(-1.0f, StrictMath.signum(-125874693.56f), 0f);
        assertEquals(1.0f, StrictMath.signum(1.2587E-11f), 0f);
        assertEquals(-1.0f, StrictMath.signum(-1.2587E-11f), 0f);

        assertEquals(1.0f, StrictMath.signum(Float.MAX_VALUE), 0f);
        assertEquals(1.0f, StrictMath.signum(Float.MIN_VALUE), 0f);
        assertEquals(-1.0f, StrictMath.signum(-Float.MAX_VALUE), 0f);
        assertEquals(-1.0f, StrictMath.signum(-Float.MIN_VALUE), 0f);
        assertEquals(1.0f, StrictMath.signum(Float.POSITIVE_INFINITY), 0f);
        assertEquals(-1.0f, StrictMath.signum(Float.NEGATIVE_INFINITY), 0f);
    }

    /**
     * java.lang.StrictMath#sin(double)
     */
    public void test_sinD() {
        // Test for method double java.lang.StrictMath.sin(double)
        assertTrue("Returned incorrect sine", StrictMath.sin(StrictMath
                .asin(OPP / HYP)) == OPP / HYP);
    }

    /**
     * java.lang.StrictMath#sinh(double)
     */
    public void test_sinh_D() {
        // Test for special situations
        assertTrue(Double.isNaN(StrictMath.sinh(Double.NaN)));
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, StrictMath
                .sinh(Double.POSITIVE_INFINITY), 0D);
        assertEquals("Should return NEGATIVE_INFINITY",
                Double.NEGATIVE_INFINITY, StrictMath
                .sinh(Double.NEGATIVE_INFINITY), 0D);
        assertEquals(Double.doubleToLongBits(0.0), Double
                .doubleToLongBits(StrictMath.sinh(0.0)));
        assertEquals(Double.doubleToLongBits(+0.0), Double
                .doubleToLongBits(StrictMath.sinh(+0.0)));
        assertEquals(Double.doubleToLongBits(-0.0), Double
                .doubleToLongBits(StrictMath.sinh(-0.0)));

        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, StrictMath.sinh(1234.56), 0D);
        assertEquals("Should return NEGATIVE_INFINITY",
                Double.NEGATIVE_INFINITY, StrictMath.sinh(-1234.56), 0D);
        assertEquals("Should return 1.0000000000001666E-6",
                1.0000000000001666E-6, StrictMath.sinh(0.000001), 0D);
        assertEquals("Should return -1.0000000000001666E-6",
                -1.0000000000001666E-6, StrictMath.sinh(-0.000001), 0D);
        assertEquals("Should return 5.115386441963859", 5.115386441963859,
                StrictMath.sinh(2.33482), 0D);
        assertEquals("Should return POSITIVE_INFINITY",
                Double.POSITIVE_INFINITY, StrictMath.sinh(Double.MAX_VALUE), 0D);
        assertEquals("Should return 4.9E-324", 4.9E-324, StrictMath
                .sinh(Double.MIN_VALUE), 0D);
    }

    /**
     * java.lang.StrictMath#sqrt(double)
     */
    public void test_sqrtD() {
        // Test for method double java.lang.StrictMath.sqrt(double)
        assertEquals("Incorrect root returned1",
                2, StrictMath.sqrt(StrictMath.pow(StrictMath.sqrt(2), 4)), 0.0);
        assertEquals("Incorrect root returned2", 7, StrictMath.sqrt(49), 0.0);
    }

    /**
     * java.lang.StrictMath#tan(double)
     */
    public void test_tanD() {
        // Test for method double java.lang.StrictMath.tan(double)
        assertTrue(
                "Returned incorrect tangent: ",
                StrictMath.tan(StrictMath.atan(1.0)) <= 1.0
                        || StrictMath.tan(StrictMath.atan(1.0)) >= 9.9999999999999983E-1);
    }

    /**
     * java.lang.StrictMath#tanh(double)
     */
    public void test_tanh_D() {
        // Test for special situations
        assertTrue(Double.isNaN(StrictMath.tanh(Double.NaN)));
        assertEquals("Should return +1.0", +1.0, StrictMath
                .tanh(Double.POSITIVE_INFINITY), 0D);
        assertEquals("Should return -1.0", -1.0, StrictMath
                .tanh(Double.NEGATIVE_INFINITY), 0D);
        assertEquals(Double.doubleToLongBits(0.0), Double
                .doubleToLongBits(StrictMath.tanh(0.0)));
        assertEquals(Double.doubleToLongBits(+0.0), Double
                .doubleToLongBits(StrictMath.tanh(+0.0)));
        assertEquals(Double.doubleToLongBits(-0.0), Double
                .doubleToLongBits(StrictMath.tanh(-0.0)));

        assertEquals("Should return 1.0", 1.0, StrictMath.tanh(1234.56), 0D);
        assertEquals("Should return -1.0", -1.0, StrictMath.tanh(-1234.56), 0D);
        assertEquals("Should return 9.999999999996666E-7",
                9.999999999996666E-7, StrictMath.tanh(0.000001), 0D);
        assertEquals("Should return 0.981422884124941", 0.981422884124941,
                StrictMath.tanh(2.33482), 0D);
        assertEquals("Should return 1.0", 1.0, StrictMath
                .tanh(Double.MAX_VALUE), 0D);
        assertEquals("Should return 4.9E-324", 4.9E-324, StrictMath
                .tanh(Double.MIN_VALUE), 0D);
    }

    /**
     * java.lang.StrictMath#random()
     */
    public void test_random() {
        // There isn't a place for these tests so just stick them here
        assertEquals("Wrong value E",
                4613303445314885481L, Double.doubleToLongBits(StrictMath.E));
        assertEquals("Wrong value PI",
                4614256656552045848L, Double.doubleToLongBits(StrictMath.PI));

        for (int i = 500; i >= 0; i--) {
            double d = StrictMath.random();
            assertTrue("Generated number is out of range: " + d, d >= 0.0
                    && d < 1.0);
        }
    }

    /**
     * java.lang.StrictMath#toRadians(double)
     */
    public void test_toRadiansD() {
        for (double d = 500; d >= 0; d -= 1.0) {
            double converted = StrictMath.toDegrees(StrictMath.toRadians(d));
            assertTrue("Converted number not equal to original. d = " + d,
                    converted >= d * 0.99999999 && converted <= d * 1.00000001);
        }
    }

    /**
     * java.lang.StrictMath#toDegrees(double)
     */
    public void test_toDegreesD() {
        for (double d = 500; d >= 0; d -= 1.0) {
            double converted = StrictMath.toRadians(StrictMath.toDegrees(d));
            assertTrue("Converted number not equal to original. d = " + d,
                    converted >= d * 0.99999999 && converted <= d * 1.00000001);
        }
    }

    /**
     * java.lang.StrictMath#ulp(double)
     */
    @SuppressWarnings("boxing")
    public void test_ulp_D() {
        // Test for special cases
        assertTrue("Should return NaN", Double
                .isNaN(StrictMath.ulp(Double.NaN)));
        assertEquals("Returned incorrect value", Double.POSITIVE_INFINITY,
                StrictMath.ulp(Double.POSITIVE_INFINITY), 0D);
        assertEquals("Returned incorrect value", Double.POSITIVE_INFINITY,
                StrictMath.ulp(Double.NEGATIVE_INFINITY), 0D);
        assertEquals("Returned incorrect value", Double.MIN_VALUE, StrictMath
                .ulp(0.0), 0D);
        assertEquals("Returned incorrect value", Double.MIN_VALUE, StrictMath
                .ulp(+0.0), 0D);
        assertEquals("Returned incorrect value", Double.MIN_VALUE, StrictMath
                .ulp(-0.0), 0D);
        assertEquals("Returned incorrect value", StrictMath.pow(2, 971),
                StrictMath.ulp(Double.MAX_VALUE), 0D);
        assertEquals("Returned incorrect value", StrictMath.pow(2, 971),
                StrictMath.ulp(-Double.MAX_VALUE), 0D);

        assertEquals("Returned incorrect value", Double.MIN_VALUE, StrictMath
                .ulp(Double.MIN_VALUE), 0D);
        assertEquals("Returned incorrect value", Double.MIN_VALUE, StrictMath
                .ulp(-Double.MIN_VALUE), 0D);

        assertEquals("Returned incorrect value", 2.220446049250313E-16,
                StrictMath.ulp(1.0), 0D);
        assertEquals("Returned incorrect value", 2.220446049250313E-16,
                StrictMath.ulp(-1.0), 0D);
        assertEquals("Returned incorrect value", 2.2737367544323206E-13,
                StrictMath.ulp(1153.0), 0D);
    }

    /**
     * java.lang.StrictMath#ulp(float)
     */
    @SuppressWarnings("boxing")
    public void test_ulp_f() {
        // Test for special cases
        assertTrue("Should return NaN", Float.isNaN(StrictMath.ulp(Float.NaN)));
        assertEquals("Returned incorrect value", Float.POSITIVE_INFINITY,
                StrictMath.ulp(Float.POSITIVE_INFINITY), 0f);
        assertEquals("Returned incorrect value", Float.POSITIVE_INFINITY,
                StrictMath.ulp(Float.NEGATIVE_INFINITY), 0f);
        assertEquals("Returned incorrect value", Float.MIN_VALUE, StrictMath
                .ulp(0.0f), 0f);
        assertEquals("Returned incorrect value", Float.MIN_VALUE, StrictMath
                .ulp(+0.0f), 0f);
        assertEquals("Returned incorrect value", Float.MIN_VALUE, StrictMath
                .ulp(-0.0f), 0f);
        assertEquals("Returned incorrect value", 2.028241E31f, StrictMath
                .ulp(Float.MAX_VALUE), 0f);
        assertEquals("Returned incorrect value", 2.028241E31f, StrictMath
                .ulp(-Float.MAX_VALUE), 0f);

        assertEquals("Returned incorrect value", 1.4E-45f, StrictMath
                .ulp(Float.MIN_VALUE), 0f);
        assertEquals("Returned incorrect value", 1.4E-45f, StrictMath
                .ulp(-Float.MIN_VALUE), 0f);

        assertEquals("Returned incorrect value", 1.1920929E-7f, StrictMath
                .ulp(1.0f), 0f);
        assertEquals("Returned incorrect value", 1.1920929E-7f, StrictMath
                .ulp(-1.0f), 0f);
        assertEquals("Returned incorrect value", 1.2207031E-4f, StrictMath
                .ulp(1153.0f), 0f);
        assertEquals("Returned incorrect value", 5.6E-45f, Math
                .ulp(9.403954E-38f), 0f);
    }
}
