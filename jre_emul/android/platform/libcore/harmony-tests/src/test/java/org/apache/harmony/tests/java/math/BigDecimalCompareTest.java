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
/**
 * @author Elena Semukhina
 */

package org.apache.harmony.tests.java.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

import junit.framework.TestCase;

/**
 * Class:  java.math.BigDecimal
 * Methods: abs, compareTo, equals, hashCode, 
 * max, min, negate, signum
 */
public class BigDecimalCompareTest extends TestCase {
    /**
     * Abs() of a negative BigDecimal
     */
    public void testAbsNeg() {
        String a = "-123809648392384754573567356745735.63567890295784902768787678287E+21";
        BigDecimal aNumber = new BigDecimal(a);
        String result = "123809648392384754573567356745735635678902957849027687.87678287";
        assertEquals("incorrect value", result, aNumber.abs().toString());
    }

    /**
     * Abs() of a positive BigDecimal
     */
    public void testAbsPos() {
        String a = "123809648392384754573567356745735.63567890295784902768787678287E+21";
        BigDecimal aNumber = new BigDecimal(a);
        String result = "123809648392384754573567356745735635678902957849027687.87678287";
        assertEquals("incorrect value", result, aNumber.abs().toString());
    }

    /**
     * Abs(MathContext) of a negative BigDecimal
     */
    public void testAbsMathContextNeg() {
        String a = "-123809648392384754573567356745735.63567890295784902768787678287E+21";
        BigDecimal aNumber = new BigDecimal(a);
        int precision = 15;
        RoundingMode rm = RoundingMode.HALF_DOWN;
        MathContext mc = new MathContext(precision, rm);
        String result = "1.23809648392385E+53";
        int resScale = -39;
        BigDecimal res = aNumber.abs(mc);
        assertEquals("incorrect value", result, res.toString());
        assertEquals("incorrect scale", resScale, res.scale());
    }

    /**
     * Abs(MathContext) of a positive BigDecimal
     */
    public void testAbsMathContextPos() {
        String a = "123809648392384754573567356745735.63567890295784902768787678287E+21";
        BigDecimal aNumber = new BigDecimal(a);
        int precision = 41;
        RoundingMode rm = RoundingMode.HALF_EVEN;
        MathContext mc = new MathContext(precision, rm);
        String result = "1.2380964839238475457356735674573563567890E+53";
        int resScale = -13;
        BigDecimal res = aNumber.abs(mc);
        assertEquals("incorrect value", result, res.toString());
        assertEquals("incorrect scale", resScale, res.scale());
    }

    /**
     * Compare to a number of an equal scale
     */
    public void testCompareEqualScale1() {
        String a = "12380964839238475457356735674573563567890295784902768787678287";
        int aScale = 18;
        String b = "4573563567890295784902768787678287";
        int bScale = 18;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        int result = 1;
        assertEquals("incorrect result", result, aNumber.compareTo(bNumber));
    }

    /**
     * Compare to a number of an equal scale
     */
    public void testCompareEqualScale2() {
        String a = "12380964839238475457356735674573563567890295784902768787678287";
        int aScale = 18;
        String b = "4573563923487289357829759278282992758247567890295784902768787678287";
        int bScale = 18;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        int result = -1;
        assertEquals("incorrect result", result, aNumber.compareTo(bNumber));
    }

    /**
     * Compare to a number of an greater scale
     */
    public void testCompareGreaterScale1() {
        String a = "12380964839238475457356735674573563567890295784902768787678287";
        int aScale = 28;
        String b = "4573563567890295784902768787678287";
        int bScale = 18;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        int result = 1;
        assertEquals("incorrect result", result, aNumber.compareTo(bNumber));
    }

    /**
     * Compare to a number of an greater scale
     */
    public void testCompareGreaterScale2() {
        String a = "12380964839238475457356735674573563567890295784902768787678287";
        int aScale = 48;
        String b = "4573563567890295784902768787678287";
        int bScale = 2;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        int result = -1;
        assertEquals("incorrect result", result, aNumber.compareTo(bNumber));
    }

    /**
     * Compare to a number of an less scale
     */
    public void testCompareLessScale1() {
        String a = "12380964839238475457356735674573563567890295784902768787678287";
        int aScale = 18;
        String b = "4573563567890295784902768787678287";
        int bScale = 28;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        int result = 1;
        assertEquals("incorrect result", result, aNumber.compareTo(bNumber));
    }

    /**
     * Compare to a number of an less scale
     */
    public void testCompareLessScale2() {
        String a = "12380964839238475457356735674573";
        int aScale = 36;
        String b = "45735635948573894578349572001798379183767890295784902768787678287";
        int bScale = 48;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        int result = -1;
        assertEquals("incorrect result", result, aNumber.compareTo(bNumber));
    }

    /**
     * Equals() for unequal BigDecimals
     */
    public void testEqualsUnequal1() {
       String a = "92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = -24;
       String b = "7472334223847623782375469293018787918347987234564568";
       int bScale = 13;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
       assertFalse(aNumber.equals(bNumber));
    }

    /**
     * Equals() for unequal BigDecimals
     */
    public void testEqualsUnequal2() {
       String a = "92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = -24;
       String b = "92948782094488478231212478987482988429808779810457634781384756794987";
       int bScale = 13;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
       assertFalse(aNumber.equals(bNumber));
    }

    /**
     * Equals() for unequal BigDecimals
     */
    public void testEqualsUnequal3() {
       String a = "92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = -24;
       String b = "92948782094488478231212478987482988429808779810457634781384756794987";
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       assertFalse(aNumber.equals(b));
    }

    /**
     * equals() for equal BigDecimals
     */
    public void testEqualsEqual() {
       String a = "92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = -24;
       String b = "92948782094488478231212478987482988429808779810457634781384756794987";
       int bScale = -24;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
       assertEquals(aNumber, bNumber);
    }

    /**
     * equals() for equal BigDecimals
     */
    public void testEqualsNull() {
       String a = "92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = -24;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       assertFalse(aNumber.equals(null));
    }

    /**
     * hashCode() for equal BigDecimals
     */
    public void testHashCodeEqual() {
       String a = "92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = -24;
       String b = "92948782094488478231212478987482988429808779810457634781384756794987";
       int bScale = -24;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
       assertEquals("incorrect value", aNumber.hashCode(), bNumber.hashCode());
    }

    /**
     * hashCode() for unequal BigDecimals
     */
    public void testHashCodeUnequal() {
       String a = "8478231212478987482988429808779810457634781384756794987";
       int aScale = 41;
       String b = "92948782094488478231212478987482988429808779810457634781384756794987";
       int bScale = -24;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
       assertTrue("incorrect value", aNumber.hashCode() != bNumber.hashCode());
    }

    /**
     * max() for equal BigDecimals
     */
    public void testMaxEqual() {
       String a = "8478231212478987482988429808779810457634781384756794987";
       int aScale = 41;
       String b = "8478231212478987482988429808779810457634781384756794987";
       int bScale = 41;
       String c = "8478231212478987482988429808779810457634781384756794987";
       int cScale = 41;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
       BigDecimal cNumber = new BigDecimal(new BigInteger(c), cScale);
       assertEquals("incorrect value", cNumber, aNumber.max(bNumber));
    }

    /**
     * max() for unequal BigDecimals
     */
    public void testMaxUnequal1() {
       String a = "92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = 24;
       String b = "92948782094488478231212478987482988429808779810457634781384756794987";
       int bScale = 41;
       String c = "92948782094488478231212478987482988429808779810457634781384756794987";
       int cScale = 24;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
       BigDecimal cNumber = new BigDecimal(new BigInteger(c), cScale);
       assertEquals("incorrect value", cNumber, aNumber.max(bNumber));
    }

    /**
     * max() for unequal BigDecimals
     */
    public void testMaxUnequal2() {
       String a = "92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = 41;
       String b = "94488478231212478987482988429808779810457634781384756794987";
       int bScale = 41;
       String c = "92948782094488478231212478987482988429808779810457634781384756794987";
       int cScale = 41;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
       BigDecimal cNumber = new BigDecimal(new BigInteger(c), cScale);
       assertEquals("incorrect value", cNumber, aNumber.max(bNumber));
    }

    /**
     * min() for equal BigDecimals
     */
    public void testMinEqual() {
       String a = "8478231212478987482988429808779810457634781384756794987";
       int aScale = 41;
       String b = "8478231212478987482988429808779810457634781384756794987";
       int bScale = 41;
       String c = "8478231212478987482988429808779810457634781384756794987";
       int cScale = 41;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
       BigDecimal cNumber = new BigDecimal(new BigInteger(c), cScale);
       assertEquals("incorrect value", cNumber, aNumber.min(bNumber));
    }

    /**
     * min() for unequal BigDecimals
     */
    public void testMinUnequal1() {
       String a = "92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = 24;
       String b = "92948782094488478231212478987482988429808779810457634781384756794987";
       int bScale = 41;
       String c = "92948782094488478231212478987482988429808779810457634781384756794987";
       int cScale = 41;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
       BigDecimal cNumber = new BigDecimal(new BigInteger(c), cScale);
       assertEquals("incorrect value", cNumber, aNumber.min(bNumber));
    }

    /**
     * min() for unequal BigDecimals
     */
    public void testMinUnequal2() {
       String a = "92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = 41;
       String b = "94488478231212478987482988429808779810457634781384756794987";
       int bScale = 41;
       String c = "94488478231212478987482988429808779810457634781384756794987";
       int cScale = 41;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
       BigDecimal cNumber = new BigDecimal(new BigInteger(c), cScale);
       assertEquals("incorrect value", cNumber, aNumber.min(bNumber));
    }

    /**
     * plus() for a positive BigDecimal
     */
    public void testPlusPositive() {
       String a = "92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = 41;
       String c = "92948782094488478231212478987482988429808779810457634781384756794987";
       int cScale = 41;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal cNumber = new BigDecimal(new BigInteger(c), cScale);
       assertEquals("incorrect value", cNumber, aNumber.plus());
    }

    /**
     * plus(MathContext) for a positive BigDecimal
     */
    public void testPlusMathContextPositive() {
       String a = "92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = 41;
       int precision = 37;
       RoundingMode rm = RoundingMode.FLOOR;
       MathContext mc = new MathContext(precision, rm);
       String c = "929487820944884782312124789.8748298842";
       int cScale = 10;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal res = aNumber.plus(mc);
       assertEquals("incorrect value", c, res.toString());
       assertEquals("incorrect scale", cScale, res.scale());
    }

    /**
     * plus() for a negative BigDecimal
     */
    public void testPlusNegative() {
       String a = "-92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = 41;
       String c = "-92948782094488478231212478987482988429808779810457634781384756794987";
       int cScale = 41;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal cNumber = new BigDecimal(new BigInteger(c), cScale);
       assertEquals("incorrect value", cNumber, aNumber.plus());
    }

    /**
     * plus(MathContext) for a negative BigDecimal
     */
    public void testPlusMathContextNegative() {
       String a = "-92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = 49;
       int precision = 46;
       RoundingMode rm = RoundingMode.CEILING;
       MathContext mc = new MathContext(precision, rm);
       String c = "-9294878209448847823.121247898748298842980877981";
       int cScale = 27;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal res = aNumber.plus(mc);
       assertEquals("incorrect value", c, res.toString());
       assertEquals("incorrect scale", cScale, res.scale());
    }

    /**
     * negate() for a positive BigDecimal
     */
    public void testNegatePositive() {
       String a = "92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = 41;
       String c = "-92948782094488478231212478987482988429808779810457634781384756794987";
       int cScale = 41;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal cNumber = new BigDecimal(new BigInteger(c), cScale);
       assertEquals("incorrect value", cNumber, aNumber.negate());
    }

    /**
     * negate(MathContext) for a positive BigDecimal
     */
    public void testNegateMathContextPositive() {
       String a = "92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = 41;
       int precision = 37;
       RoundingMode rm = RoundingMode.FLOOR;
       MathContext mc = new MathContext(precision, rm);
       String c = "-929487820944884782312124789.8748298842";
       int cScale = 10;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal res = aNumber.negate(mc);
       assertEquals("incorrect value", c, res.toString());
       assertEquals("incorrect scale", cScale, res.scale());
    }

    /**
     * negate() for a negative BigDecimal
     */
    public void testNegateNegative() {
       String a = "-92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = 41;
       String c = "92948782094488478231212478987482988429808779810457634781384756794987";
       int cScale = 41;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal cNumber = new BigDecimal(new BigInteger(c), cScale);
       assertEquals("incorrect value", cNumber, aNumber.negate());
    }

    /**
     * negate(MathContext) for a negative BigDecimal
     */
    public void testNegateMathContextNegative() {
       String a = "-92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = 49;
       int precision = 46;
       RoundingMode rm = RoundingMode.CEILING;
       MathContext mc = new MathContext(precision, rm);
       String c = "9294878209448847823.121247898748298842980877981";
       int cScale = 27;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       BigDecimal res = aNumber.negate(mc);
       assertEquals("incorrect value", c, res.toString());
       assertEquals("incorrect scale", cScale, res.scale());
    }

    /**
     * signum() for a positive BigDecimal
     */
    public void testSignumPositive() {
       String a = "92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = 41;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       assertEquals("incorrect value", 1, aNumber.signum());
    }

    /**
     * signum() for a negative BigDecimal
     */
    public void testSignumNegative() {
       String a = "-92948782094488478231212478987482988429808779810457634781384756794987";
       int aScale = 41;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       assertEquals("incorrect value", -1, aNumber.signum());
    }

    /**
     * signum() for zero
     */
    public void testSignumZero() {
       String a = "0";
       int aScale = 41;
       BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
       assertEquals("incorrect value", 0, aNumber.signum());
    }
    
    /*
     * Regression test for HARMONY-6406
     */
    public void testApproxPrecision() {
        BigDecimal testInstance = BigDecimal.TEN.multiply(new BigDecimal("0.1"));
        int result = testInstance.compareTo(new BigDecimal("1.00"));
        assertEquals(0, result);
    }
}
