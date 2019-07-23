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

package org.apache.harmony.tests.java.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

import junit.framework.TestCase;

/**
 * Class:  java.math.BigDecimal
 * Methods: add, subtract, multiply, divide 
 */
public class BigDecimalArithmeticTest extends TestCase {
    /**
     * Add two numbers of equal positive scales
     */
    public void testAddEqualScalePosPos() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 10;
        String b = "747233429293018787918347987234564568";
        int bScale = 10;
        String c = "123121247898748373566323807282924555312937.1991359555";
        int cScale = 10;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.add(bNumber);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", cScale, result.scale());
    }

    /**
     * Add two numbers of equal positive scales using MathContext
     */
    public void testAddMathContextEqualScalePosPos() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 10;
        String b = "747233429293018787918347987234564568";
        int bScale = 10;
        String c = "1.2313E+41";
        int cScale = -37;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        MathContext mc = new MathContext(5, RoundingMode.UP);
        BigDecimal result = aNumber.add(bNumber, mc);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", cScale, result.scale());
    }

    /**
     * Add two numbers of equal negative scales
     */
    public void testAddEqualScaleNegNeg() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = -10;
        String b = "747233429293018787918347987234564568";
        int bScale = -10;
        String c = "1.231212478987483735663238072829245553129371991359555E+61";
        int cScale = -10;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.add(bNumber);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", cScale, result.scale());
    }

    /**
     * Add two numbers of equal negative scales using MathContext
     */
    public void testAddMathContextEqualScaleNegNeg() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = -10;
        String b = "747233429293018787918347987234564568";
        int bScale = -10;
        String c = "1.2312E+61";
        int cScale = -57;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        MathContext mc = new MathContext(5, RoundingMode.FLOOR);
        BigDecimal result = aNumber.add(bNumber, mc);
        assertEquals("incorrect value ", c, result.toString());
        assertEquals("incorrect scale", cScale, result.scale());
    }

    /**
     * Add two numbers of different scales; the first is positive
     */
    public void testAddDiffScalePosNeg() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 15;
        String b = "747233429293018787918347987234564568";
        int bScale = -10;
        String c = "7472334294161400358170962860775454459810457634.781384756794987";
        int cScale = 15;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.add(bNumber);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", cScale, result.scale());
    }

    /**
     * Add two numbers of different scales using MathContext; the first is positive
     */
    public void testAddMathContextDiffScalePosNeg() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 15;
        String b = "747233429293018787918347987234564568";
        int bScale = -10;
        String c = "7.47233429416141E+45";
        int cScale = -31;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        MathContext mc = new MathContext(15, RoundingMode.CEILING);
        BigDecimal result = aNumber.add(bNumber, mc);
        assertEquals("incorrect value", c, c.toString());
        assertEquals("incorrect scale", cScale, result.scale());
    }

    /**
     * Add two numbers of different scales; the first is negative
     */
    public void testAddDiffScaleNegPos() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = -15;
        String b = "747233429293018787918347987234564568";
        int bScale = 10;
        String c = "1231212478987482988429808779810457634781459480137916301878791834798.7234564568";
        int cScale = 10;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.add(bNumber);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", cScale, result.scale());
    }

    /**
     * Add two zeroes of different scales; the first is negative
     */
    public void testAddDiffScaleZeroZero() {
        String a = "0";
        int aScale = -15;
        String b = "0";
        int bScale = 10;
        String c = "0E-10";
        int cScale = 10;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.add(bNumber);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", cScale, result.scale());
    }

    /**
     * Subtract two numbers of equal positive scales
     */
    public void testSubtractEqualScalePosPos() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 10;
        String b = "747233429293018787918347987234564568";
        int bScale = 10;
        String c = "123121247898748224119637948679166971643339.7522230419";
        int cScale = 10;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.subtract(bNumber);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", cScale, result.scale());
    }

    /**
     * Subtract two numbers of equal positive scales using MathContext
     */
    public void testSubtractMathContextEqualScalePosPos() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 10;
        String b = "747233429293018787918347987234564568";
        int bScale = 10;
        String c = "1.23121247898749E+41";
        int cScale = -27;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        MathContext mc = new MathContext(15, RoundingMode.CEILING);
        BigDecimal result = aNumber.subtract(bNumber, mc);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", cScale, result.scale());
    }

    /**
     * Subtract two numbers of equal negative scales
     */
    public void testSubtractEqualScaleNegNeg() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = -10;
        String b = "747233429293018787918347987234564568";
        int bScale = -10;
        String c = "1.231212478987482241196379486791669716433397522230419E+61";
        int cScale = -10;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.subtract(bNumber);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", cScale, result.scale());
    }

    /**
     * Subtract two numbers of different scales; the first is positive
     */
    public void testSubtractDiffScalePosNeg() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 15;
        String b = "747233429293018787918347987234564568";
        int bScale = -10;
        String c = "-7472334291698975400195996883915836900189542365.218615243205013";
        int cScale = 15;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.subtract(bNumber);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", cScale, result.scale());
    }

    /**
     * Subtract two numbers of different scales using MathContext;
     *  the first is positive
     */
    public void testSubtractMathContextDiffScalePosNeg() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 15;
        String b = "747233429293018787918347987234564568";
        int bScale = -10;
        String c = "-7.4723342916989754E+45";
        int cScale = -29;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        MathContext mc = new MathContext(17, RoundingMode.DOWN);
        BigDecimal result = aNumber.subtract(bNumber, mc);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", cScale, result.scale());
    }

    /**
     * Subtract two numbers of different scales; the first is negative
     */
    public void testSubtractDiffScaleNegPos() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = -15;
        String b = "747233429293018787918347987234564568";
        int bScale = 10;
        String c = "1231212478987482988429808779810457634781310033452057698121208165201.2765435432";
        int cScale = 10;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.subtract(bNumber);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", cScale, result.scale());
    }

    /**
     * Subtract two numbers of different scales using MathContext;
     *  the first is negative
     */
    public void testSubtractMathContextDiffScaleNegPos() {
        String a = "986798656676789766678767876078779810457634781384756794987";
        int aScale = -15;
        String b = "747233429293018787918347987234564568";
        int bScale = 40;
        String c = "9.867986566767897666787678760787798104576347813847567949870000000000000E+71";
        int cScale = -2;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        MathContext mc = new MathContext(70, RoundingMode.HALF_DOWN);
        BigDecimal result = aNumber.subtract(bNumber, mc);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", cScale, result.scale());
    }

    /**
     * Multiply two numbers of positive scales
     */
    public void testMultiplyScalePosPos() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 15;
        String b = "747233429293018787918347987234564568";
        int bScale = 10;
        String c = "92000312286217574978643009574114545567010139156902666284589309.1880727173060570190220616";
        int cScale = 25;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.multiply(bNumber);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", cScale, result.scale());
    }

    /**
     * Multiply two numbers of positive scales using MathContext
     */
    public void testMultiplyMathContextScalePosPos() {
        String a = "97665696756578755423325476545428779810457634781384756794987";
        int aScale = -25;
        String b = "87656965586786097685674786576598865";
        int bScale = 10;
        String c = "8.561078619600910561431314228543672720908E+108";
        int cScale = -69;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        MathContext mc = new MathContext(40, RoundingMode.HALF_DOWN);
        BigDecimal result = aNumber.multiply(bNumber, mc);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", cScale, result.scale());
    }

    /**
     * Multiply two numbers of negative scales
     */
    public void testMultiplyEqualScaleNegNeg() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = -15;
        String b = "747233429293018787918347987234564568";
        int bScale = -10;
        String c = "9.20003122862175749786430095741145455670101391569026662845893091880727173060570190220616E+111";
        int cScale = -25;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.multiply(bNumber);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", cScale, result.scale());
    }

    /**
     * Multiply two numbers of different scales
     */
    public void testMultiplyDiffScalePosNeg() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 10;
        String b = "747233429293018787918347987234564568";
        int bScale = -10;
        String c = "920003122862175749786430095741145455670101391569026662845893091880727173060570190220616";
        int cScale = 0;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.multiply(bNumber);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", cScale, result.scale());
    }

    /**
     * Multiply two numbers of different scales using MathContext
     */
    public void testMultiplyMathContextDiffScalePosNeg() {
        String a = "987667796597975765768768767866756808779810457634781384756794987";
        int aScale = 100;
        String b = "747233429293018787918347987234564568";
        int bScale = -70;
        String c = "7.3801839465418518653942222612429081498248509257207477E+68";
        int cScale = -16;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        MathContext mc = new MathContext(53, RoundingMode.HALF_UP);
        BigDecimal result = aNumber.multiply(bNumber, mc);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", cScale, result.scale());
    }

    /**
     * Multiply two numbers of different scales
     */
    public void testMultiplyDiffScaleNegPos() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = -15;
        String b = "747233429293018787918347987234564568";
        int bScale = 10;
        String c = "9.20003122862175749786430095741145455670101391569026662845893091880727173060570190220616E+91";
        int cScale = -5;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.multiply(bNumber);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", cScale, result.scale());
    }

    /**
     * Multiply two numbers of different scales using MathContext
     */
    public void testMultiplyMathContextDiffScaleNegPos() {
        String a = "488757458676796558668876576576579097029810457634781384756794987";
        int aScale = -63;
        String b = "747233429293018787918347987234564568";
        int bScale = 63;
        String c = "3.6521591193960361339707130098174381429788164316E+98";
        int cScale = -52;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        MathContext mc = new MathContext(47, RoundingMode.HALF_UP);
        BigDecimal result = aNumber.multiply(bNumber, mc);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", cScale, result.scale());
    }

    /**
     * pow(int)
     */
    public void testPow() {
        String a = "123121247898748298842980";
        int aScale = 10;
        int exp = 10;
        String c = "8004424019039195734129783677098845174704975003788210729597" +
                   "4875206425711159855030832837132149513512555214958035390490" +
                   "798520842025826.594316163502809818340013610490541783276343" +
                   "6514490899700151256484355936102754469438371850240000000000";
        int cScale = 100;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal result = aNumber.pow(exp);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", cScale, result.scale());
    }

    /**
     * pow(0)
     */
    public void testPow0() {
        String a = "123121247898748298842980";
        int aScale = 10;
        int exp = 0;
        String c = "1";
        int cScale = 0;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal result = aNumber.pow(exp);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", cScale, result.scale());
    }

    /**
     * ZERO.pow(0)
     */
    public void testZeroPow0() {
        String c = "1";
        int cScale = 0;
        BigDecimal result = BigDecimal.ZERO.pow(0);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", cScale, result.scale());
    }

    /**
     * pow(int, MathContext)
     */
    public void testPowMathContext() {
        String a = "123121247898748298842980";
        int aScale = 10;
        int exp = 10;
        String c = "8.0044E+130";
        int cScale = -126;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        MathContext mc = new MathContext(5, RoundingMode.HALF_UP);
        BigDecimal result = aNumber.pow(exp, mc);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", cScale, result.scale());
    }

    /**
     * Divide by zero
     */
    public void testDivideByZero() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 15;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = BigDecimal.valueOf(0L);
        try {
            aNumber.divide(bNumber);
            fail("ArithmeticException has not been caught");
        } catch (ArithmeticException e) {
            assertEquals("Improper exception message", "Division by zero", e.getMessage());
        }
    }

    /**
     * Divide with ROUND_UNNECESSARY
     */
    public void testDivideExceptionRM() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 15;
        String b = "747233429293018787918347987234564568";
        int bScale = 10;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        try {
            aNumber.divide(bNumber, BigDecimal.ROUND_UNNECESSARY);
            fail("ArithmeticException has not been caught");
        } catch (ArithmeticException e) {
            assertEquals("Improper exception message", "Rounding necessary", e.getMessage());
        }
    }

    /**
     * Divide with invalid rounding mode
     */
    public void testDivideExceptionInvalidRM() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 15;
        String b = "747233429293018787918347987234564568";
        int bScale = 10;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        try {
            aNumber.divide(bNumber, 100);
            fail("IllegalArgumentException has not been caught");
        } catch (IllegalArgumentException e) {
            assertEquals("Improper exception message", "Invalid rounding mode", e.getMessage());
        }
    }

    /**
     * Divide: local variable exponent is less than zero
     */
    public void testDivideExpLessZero() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 15;
        String b = "747233429293018787918347987234564568";
        int bScale = 10;
        String c = "1.64770E+10";
        int resScale = -5;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, resScale, BigDecimal.ROUND_CEILING);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * Divide: local variable exponent is equal to zero
     */
    public void testDivideExpEqualsZero() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = -15;
        String b = "747233429293018787918347987234564568";
        int bScale = 10;
        String c = "1.64769459009933764189139568605273529E+40";
        int resScale = -5;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, resScale, BigDecimal.ROUND_CEILING);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * Divide: local variable exponent is greater than zero
     */
    public void testDivideExpGreaterZero() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = -15;
        String b = "747233429293018787918347987234564568";
        int bScale = 20;
        String c = "1.647694590099337641891395686052735285121058381E+50";
        int resScale = -5;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, resScale, BigDecimal.ROUND_CEILING);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * Divide: remainder is zero
     */
    public void testDivideRemainderIsZero() {
        String a = "8311389578904553209874735431110";
        int aScale = -15;
        String b = "237468273682987234567849583746";
        int bScale = 20;
        String c = "3.5000000000000000000000000000000E+36";
        int resScale = -5;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, resScale, BigDecimal.ROUND_CEILING);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }
    
    /**
     * Divide: rounding mode is ROUND_UP, result is negative
     */
    public void testDivideRoundUpNeg() {
        String a = "-92948782094488478231212478987482988429808779810457634781384756794987";
        int aScale = -24;
        String b = "7472334223847623782375469293018787918347987234564568";
        int bScale = 13;
        String c = "-1.24390557635720517122423359799284E+53";
        int resScale = -21;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, resScale, BigDecimal.ROUND_UP);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * Divide: rounding mode is ROUND_UP, result is positive
     */
    public void testDivideRoundUpPos() {
        String a = "92948782094488478231212478987482988429808779810457634781384756794987";
        int aScale = -24;
        String b = "7472334223847623782375469293018787918347987234564568";
        int bScale = 13;
        String c = "1.24390557635720517122423359799284E+53";
        int resScale = -21;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, resScale, BigDecimal.ROUND_UP);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * Divide: rounding mode is ROUND_DOWN, result is negative
     */
    public void testDivideRoundDownNeg() {
        String a = "-92948782094488478231212478987482988429808779810457634781384756794987";
        int aScale = -24;
        String b = "7472334223847623782375469293018787918347987234564568";
        int bScale = 13;
        String c = "-1.24390557635720517122423359799283E+53";
        int resScale = -21;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, resScale, BigDecimal.ROUND_DOWN);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * Divide: rounding mode is ROUND_DOWN, result is positive
     */
    public void testDivideRoundDownPos() {
        String a = "92948782094488478231212478987482988429808779810457634781384756794987";
        int aScale = -24;
        String b = "7472334223847623782375469293018787918347987234564568";
        int bScale = 13;
        String c = "1.24390557635720517122423359799283E+53";
        int resScale = -21;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, resScale, BigDecimal.ROUND_DOWN);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }
    
    /**
     * Divide: rounding mode is ROUND_FLOOR, result is positive
     */
    public void testDivideRoundFloorPos() {
        String a = "92948782094488478231212478987482988429808779810457634781384756794987";
        int aScale = -24;
        String b = "7472334223847623782375469293018787918347987234564568";
        int bScale = 13;
        String c = "1.24390557635720517122423359799283E+53";
        int resScale = -21;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, resScale, BigDecimal.ROUND_FLOOR);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * Divide: rounding mode is ROUND_FLOOR, result is negative
     */
    public void testDivideRoundFloorNeg() {
        String a = "-92948782094488478231212478987482988429808779810457634781384756794987";
        int aScale = -24;
        String b = "7472334223847623782375469293018787918347987234564568";
        int bScale = 13;
        String c = "-1.24390557635720517122423359799284E+53";
        int resScale = -21;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, resScale, BigDecimal.ROUND_FLOOR);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }
    
    /**
     * Divide: rounding mode is ROUND_CEILING, result is positive
     */
    public void testDivideRoundCeilingPos() {
        String a = "92948782094488478231212478987482988429808779810457634781384756794987";
        int aScale = -24;
        String b = "7472334223847623782375469293018787918347987234564568";
        int bScale = 13;
        String c = "1.24390557635720517122423359799284E+53";
        int resScale = -21;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, resScale, BigDecimal.ROUND_CEILING);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * Divide: rounding mode is ROUND_CEILING, result is negative
     */
    public void testDivideRoundCeilingNeg() {
        String a = "-92948782094488478231212478987482988429808779810457634781384756794987";
        int aScale = -24;
        String b = "7472334223847623782375469293018787918347987234564568";
        int bScale = 13;
        String c = "-1.24390557635720517122423359799283E+53";
        int resScale = -21;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, resScale, BigDecimal.ROUND_CEILING);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }
    
    /**
     * Divide: rounding mode is ROUND_HALF_UP, result is positive; distance = -1
     */
    public void testDivideRoundHalfUpPos() {
        String a = "92948782094488478231212478987482988429808779810457634781384756794987";
        int aScale = -24;
        String b = "7472334223847623782375469293018787918347987234564568";
        int bScale = 13;
        String c = "1.24390557635720517122423359799284E+53";
        int resScale = -21;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, resScale, BigDecimal.ROUND_HALF_UP);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * Divide: rounding mode is ROUND_HALF_UP, result is negative; distance = -1
     */
    public void testDivideRoundHalfUpNeg() {
        String a = "-92948782094488478231212478987482988429808779810457634781384756794987";
        int aScale = -24;
        String b = "7472334223847623782375469293018787918347987234564568";
        int bScale = 13;
        String c = "-1.24390557635720517122423359799284E+53";
        int resScale = -21;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, resScale, BigDecimal.ROUND_HALF_UP);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }
    
    /**
     * Divide: rounding mode is ROUND_HALF_UP, result is positive; distance = 1
     */
    public void testDivideRoundHalfUpPos1() {
        String a = "92948782094488478231212478987482988798104576347813847567949855464535634534563456";
        int aScale = -24;
        String b = "74723342238476237823754692930187879183479";
        int bScale = 13;
        String c = "1.2439055763572051712242335979928354832010167729111113605E+76";
        int resScale = -21;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, resScale, BigDecimal.ROUND_HALF_UP);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * Divide: rounding mode is ROUND_HALF_UP, result is negative; distance = 1
     */
    public void testDivideRoundHalfUpNeg1() {
        String a = "-92948782094488478231212478987482988798104576347813847567949855464535634534563456";
        int aScale = -24;
        String b = "74723342238476237823754692930187879183479";
        int bScale = 13;
        String c = "-1.2439055763572051712242335979928354832010167729111113605E+76";
        int resScale = -21;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, resScale, BigDecimal.ROUND_HALF_UP);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }
    
    /**
     * Divide: rounding mode is ROUND_HALF_UP, result is negative; equidistant
     */
    public void testDivideRoundHalfUpNeg2() {
        String a = "-37361671119238118911893939591735";
        int aScale = 10;
        String b = "74723342238476237823787879183470";
        int bScale = 15;
        String c = "-1E+5";
        int resScale = -5;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, resScale, BigDecimal.ROUND_HALF_UP);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }
    
    /**
     * Divide: rounding mode is ROUND_HALF_DOWN, result is positive; distance = -1
     */
    public void testDivideRoundHalfDownPos() {
        String a = "92948782094488478231212478987482988429808779810457634781384756794987";
        int aScale = -24;
        String b = "7472334223847623782375469293018787918347987234564568";
        int bScale = 13;
        String c = "1.24390557635720517122423359799284E+53";
        int resScale = -21;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, resScale, BigDecimal.ROUND_HALF_DOWN);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * Divide: rounding mode is ROUND_HALF_DOWN, result is negative; distance = -1
     */
    public void testDivideRoundHalfDownNeg() {
        String a = "-92948782094488478231212478987482988429808779810457634781384756794987";
        int aScale = -24;
        String b = "7472334223847623782375469293018787918347987234564568";
        int bScale = 13;
        String c = "-1.24390557635720517122423359799284E+53";
        int resScale = -21;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, resScale, BigDecimal.ROUND_HALF_DOWN);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }
    
    /**
     * Divide: rounding mode is ROUND_HALF_DOWN, result is positive; distance = 1
     */
    public void testDivideRoundHalfDownPos1() {
        String a = "92948782094488478231212478987482988798104576347813847567949855464535634534563456";
        int aScale = -24;
        String b = "74723342238476237823754692930187879183479";
        int bScale = 13;
        String c = "1.2439055763572051712242335979928354832010167729111113605E+76";
        int resScale = -21;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, resScale, BigDecimal.ROUND_HALF_DOWN);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * Divide: rounding mode is ROUND_HALF_DOWN, result is negative; distance = 1
     */
    public void testDivideRoundHalfDownNeg1() {
        String a = "-92948782094488478231212478987482988798104576347813847567949855464535634534563456";
        int aScale = -24;
        String b = "74723342238476237823754692930187879183479";
        int bScale = 13;
        String c = "-1.2439055763572051712242335979928354832010167729111113605E+76";
        int resScale = -21;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, resScale, BigDecimal.ROUND_HALF_DOWN);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * Divide: rounding mode is ROUND_HALF_UP, result is negative; equidistant
     */
    public void testDivideRoundHalfDownNeg2() {
        String a = "-37361671119238118911893939591735";
        int aScale = 10;
        String b = "74723342238476237823787879183470";
        int bScale = 15;
        String c = "0E+5";
        int resScale = -5;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, resScale, BigDecimal.ROUND_HALF_DOWN);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * Divide: rounding mode is ROUND_HALF_EVEN, result is positive; distance = -1
     */
    public void testDivideRoundHalfEvenPos() {
        String a = "92948782094488478231212478987482988429808779810457634781384756794987";
        int aScale = -24;
        String b = "7472334223847623782375469293018787918347987234564568";
        int bScale = 13;
        String c = "1.24390557635720517122423359799284E+53";
        int resScale = -21;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, resScale, BigDecimal.ROUND_HALF_EVEN);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * Divide: rounding mode is ROUND_HALF_EVEN, result is negative; distance = -1
     */
    public void testDivideRoundHalfEvenNeg() {
        String a = "-92948782094488478231212478987482988429808779810457634781384756794987";
        int aScale = -24;
        String b = "7472334223847623782375469293018787918347987234564568";
        int bScale = 13;
        String c = "-1.24390557635720517122423359799284E+53";
        int resScale = -21;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, resScale, BigDecimal.ROUND_HALF_EVEN);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }
    
    /**
     * Divide: rounding mode is ROUND_HALF_EVEN, result is positive; distance = 1
     */
    public void testDivideRoundHalfEvenPos1() {
        String a = "92948782094488478231212478987482988798104576347813847567949855464535634534563456";
        int aScale = -24;
        String b = "74723342238476237823754692930187879183479";
        int bScale = 13;
        String c = "1.2439055763572051712242335979928354832010167729111113605E+76";
        int resScale = -21;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, resScale, BigDecimal.ROUND_HALF_EVEN);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * Divide: rounding mode is ROUND_HALF_EVEN, result is negative; distance = 1
     */
    public void testDivideRoundHalfEvenNeg1() {
        String a = "-92948782094488478231212478987482988798104576347813847567949855464535634534563456";
        int aScale = -24;
        String b = "74723342238476237823754692930187879183479";
        int bScale = 13;
        String c = "-1.2439055763572051712242335979928354832010167729111113605E+76";
        int resScale = -21;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, resScale, BigDecimal.ROUND_HALF_EVEN);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * Divide: rounding mode is ROUND_HALF_EVEN, result is negative; equidistant
     */
    public void testDivideRoundHalfEvenNeg2() {
        String a = "-37361671119238118911893939591735";
        int aScale = 10;
        String b = "74723342238476237823787879183470";
        int bScale = 15;
        String c = "0E+5";
        int resScale = -5;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, resScale, BigDecimal.ROUND_HALF_EVEN);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * Divide to BigDecimal
     */
    public void testDivideBigDecimal1() {
        String a = "-37361671119238118911893939591735";
        int aScale = 10;
        String b = "74723342238476237823787879183470";
        int bScale = 15;
        String c = "-5E+4";
        int resScale = -4;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * Divide to BigDecimal
     */
    public void testDivideBigDecimal2() {
        String a = "-37361671119238118911893939591735";
        int aScale = 10;
        String b = "74723342238476237823787879183470";
        int bScale = -15;
        String c = "-5E-26";
        int resScale = 26;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * divide(BigDecimal, scale, RoundingMode)
     */
    public void testDivideBigDecimalScaleRoundingModeUP() {
        String a = "-37361671119238118911893939591735";
        int aScale = 10;
        String b = "74723342238476237823787879183470";
        int bScale = -15;
        int newScale = 31;
        RoundingMode rm = RoundingMode.UP;
        String c = "-5.00000E-26";
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, newScale, rm);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", newScale, result.scale());
    }

    /**
     * divide(BigDecimal, scale, RoundingMode)
     */
    public void testDivideBigDecimalScaleRoundingModeDOWN() {
        String a = "-37361671119238118911893939591735";
        int aScale = 10;
        String b = "74723342238476237823787879183470";
        int bScale = 15;
        int newScale = 31;
        RoundingMode rm = RoundingMode.DOWN;
        String c = "-50000.0000000000000000000000000000000";
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, newScale, rm);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", newScale, result.scale());
    }

    /**
     * divide(BigDecimal, scale, RoundingMode)
     */
    public void testDivideBigDecimalScaleRoundingModeCEILING() {
        String a = "3736186567876876578956958765675671119238118911893939591735";
        int aScale = 100;
        String b = "74723342238476237823787879183470";
        int bScale = 15;
        int newScale = 45;
        RoundingMode rm = RoundingMode.CEILING;
        String c = "1E-45";
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, newScale, rm);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", newScale, result.scale());
    }

    /**
     * divide(BigDecimal, scale, RoundingMode)
     */
    public void testDivideBigDecimalScaleRoundingModeFLOOR() {
        String a = "3736186567876876578956958765675671119238118911893939591735";
        int aScale = 100;
        String b = "74723342238476237823787879183470";
        int bScale = 15;
        int newScale = 45;
        RoundingMode rm = RoundingMode.FLOOR;
        String c = "0E-45";
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, newScale, rm);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", newScale, result.scale());
    }

    /**
     * divide(BigDecimal, scale, RoundingMode)
     */
    public void testDivideBigDecimalScaleRoundingModeHALF_UP() {
        String a = "3736186567876876578956958765675671119238118911893939591735";
        int aScale = -51;
        String b = "74723342238476237823787879183470";
        int bScale = 45;
        int newScale = 3;
        RoundingMode rm = RoundingMode.HALF_UP;
        String c = "50000260373164286401361913262100972218038099522752460421" +
                   "05959924024355721031761947728703598332749334086415670525" +
                   "3761096961.670";
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, newScale, rm);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", newScale, result.scale());
    }

    /**
     * divide(BigDecimal, scale, RoundingMode)
     */
    public void testDivideBigDecimalScaleRoundingModeHALF_DOWN() {
        String a = "3736186567876876578956958765675671119238118911893939591735";
        int aScale = 5;
        String b = "74723342238476237823787879183470";
        int bScale = 15;
        int newScale = 7;
        RoundingMode rm = RoundingMode.HALF_DOWN;
        String c = "500002603731642864013619132621009722.1803810";
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, newScale, rm);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", newScale, result.scale());
    }

    /**
     * divide(BigDecimal, scale, RoundingMode)
     */
    public void testDivideBigDecimalScaleRoundingModeHALF_EVEN() {
        String a = "3736186567876876578956958765675671119238118911893939591735";
        int aScale = 5;
        String b = "74723342238476237823787879183470";
        int bScale = 15;
        int newScale = 7;
        RoundingMode rm = RoundingMode.HALF_EVEN;
        String c = "500002603731642864013619132621009722.1803810";
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, newScale, rm);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", newScale, result.scale());
    }

    /**
     * divide(BigDecimal, MathContext)
     */
    public void testDivideBigDecimalScaleMathContextUP() {
        String a = "3736186567876876578956958765675671119238118911893939591735";
        int aScale = 15;
        String b = "748766876876723342238476237823787879183470";
        int bScale = 10;
        int precision = 21;
        RoundingMode rm = RoundingMode.UP;
        MathContext mc = new MathContext(precision, rm);
        String c = "49897861180.2562512996";
        int resScale = 10;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, mc);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * divide(BigDecimal, MathContext)
     */
    public void testDivideBigDecimalScaleMathContextDOWN() {
        String a = "3736186567876876578956958765675671119238118911893939591735";
        int aScale = 15;
        String b = "748766876876723342238476237823787879183470";
        int bScale = 70;
        int precision = 21;
        RoundingMode rm = RoundingMode.DOWN;
        MathContext mc = new MathContext(precision, rm);
        String c = "4.98978611802562512995E+70";
        int resScale = -50;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, mc);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * divide(BigDecimal, MathContext)
     */
    public void testDivideBigDecimalScaleMathContextCEILING() {
        String a = "3736186567876876578956958765675671119238118911893939591735";
        int aScale = 15;
        String b = "748766876876723342238476237823787879183470";
        int bScale = 70;
        int precision = 21;
        RoundingMode rm = RoundingMode.CEILING;
        MathContext mc = new MathContext(precision, rm);
        String c = "4.98978611802562512996E+70";
        int resScale = -50;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, mc);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * divide(BigDecimal, MathContext)
     */
    public void testDivideBigDecimalScaleMathContextFLOOR() {
        String a = "3736186567876876578956958765675671119238118911893939591735";
        int aScale = 15;
        String b = "748766876876723342238476237823787879183470";
        int bScale = 70;
        int precision = 21;
        RoundingMode rm = RoundingMode.FLOOR;
        MathContext mc = new MathContext(precision, rm);
        String c = "4.98978611802562512995E+70";
        int resScale = -50;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, mc);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * divide(BigDecimal, MathContext)
     */
    public void testDivideBigDecimalScaleMathContextHALF_UP() {
        String a = "3736186567876876578956958765675671119238118911893939591735";
        int aScale = 45;
        String b = "134432345432345748766876876723342238476237823787879183470";
        int bScale = 70;
        int precision = 21;
        RoundingMode rm = RoundingMode.HALF_UP;
        MathContext mc = new MathContext(precision, rm);
        String c = "2.77923185514690367475E+26";
        int resScale = -6;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, mc);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * divide(BigDecimal, MathContext)
     */
    public void testDivideBigDecimalScaleMathContextHALF_DOWN() {
        String a = "3736186567876876578956958765675671119238118911893939591735";
        int aScale = 45;
        String b = "134432345432345748766876876723342238476237823787879183470";
        int bScale = 70;
        int precision = 21;
        RoundingMode rm = RoundingMode.HALF_DOWN;
        MathContext mc = new MathContext(precision, rm);
        String c = "2.77923185514690367475E+26";
        int resScale = -6;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, mc);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * divide(BigDecimal, MathContext)
     */
    public void testDivideBigDecimalScaleMathContextHALF_EVEN() {
        String a = "3736186567876876578956958765675671119238118911893939591735";
        int aScale = 45;
        String b = "134432345432345748766876876723342238476237823787879183470";
        int bScale = 70;
        int precision = 21;
        RoundingMode rm = RoundingMode.HALF_EVEN;
        MathContext mc = new MathContext(precision, rm);
        String c = "2.77923185514690367475E+26";
        int resScale = -6;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divide(bNumber, mc);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }


    /**
     * BigDecimal.divide with a scale that's too large
     * 
     * Regression test for HARMONY-6271
     */
    public void testDivideLargeScale() {
    	BigDecimal arg1 = new BigDecimal("320.0E+2147483647");
		BigDecimal arg2 = new BigDecimal("6E-2147483647");
    	try {
    		BigDecimal result = arg1.divide(arg2, Integer.MAX_VALUE, 
    				java.math.RoundingMode.CEILING);
    		fail("Expected ArithmeticException when dividing with a scale that's too large");
    	} catch (ArithmeticException e) {
    		// expected behaviour
    	}
    }

    /**
     * divideToIntegralValue(BigDecimal)
     */
    public void testDivideToIntegralValue() {
        String a = "3736186567876876578956958765675671119238118911893939591735";
        int aScale = 45;
        String b = "134432345432345748766876876723342238476237823787879183470";
        int bScale = 70;
        String c = "277923185514690367474770683";
        int resScale = 0;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divideToIntegralValue(bNumber);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * divideToIntegralValue(BigDecimal, MathContext)
     */
    public void testDivideToIntegralValueMathContextUP() {
        String a = "3736186567876876578956958765675671119238118911893939591735";
        int aScale = 45;
        String b = "134432345432345748766876876723342238476237823787879183470";
        int bScale = 70;
        int precision = 32;
        RoundingMode rm = RoundingMode.UP;
        MathContext mc = new MathContext(precision, rm);
        String c = "277923185514690367474770683";
        int resScale = 0;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divideToIntegralValue(bNumber, mc);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * divideToIntegralValue(BigDecimal, MathContext)
     */
    public void testDivideToIntegralValueMathContextDOWN() {
        String a = "3736186567876876578956958769675785435673453453653543654354365435675671119238118911893939591735";
        int aScale = 45;
        String b = "134432345432345748766876876723342238476237823787879183470";
        int bScale = 70;
        int precision = 75;
        RoundingMode rm = RoundingMode.DOWN;
        MathContext mc = new MathContext(precision, rm);
        String c = "2.7792318551469036747477068339450205874992634417590178670822889E+62";
        int resScale = -1;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.divideToIntegralValue(bNumber, mc);
        assertEquals("incorrect value", c, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }
    
    /**
     * divideAndRemainder(BigDecimal)
     */
    public void testDivideAndRemainder1() {
        String a = "3736186567876876578956958765675671119238118911893939591735";
        int aScale = 45;
        String b = "134432345432345748766876876723342238476237823787879183470";
        int bScale = 70;
        String res = "277923185514690367474770683";
        int resScale = 0;
        String rem = "1.3032693871288309587558885943391070087960319452465789990E-15";
        int remScale = 70;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result[] = aNumber.divideAndRemainder(bNumber);
        assertEquals("incorrect quotient value", res, result[0].toString());
        assertEquals("incorrect quotient scale", resScale, result[0].scale());
        assertEquals("incorrect remainder value", rem, result[1].toString());
        assertEquals("incorrect remainder scale", remScale, result[1].scale());
    }

    /**
     * divideAndRemainder(BigDecimal)
     */
    public void testDivideAndRemainder2() {
        String a = "3736186567876876578956958765675671119238118911893939591735";
        int aScale = -45;
        String b = "134432345432345748766876876723342238476237823787879183470";
        int bScale = 70;
        String res = "2779231855146903674747706830969461168692256919247547952" +
                     "2608549363170374005512836303475980101168105698072946555" +
                     "6862849";
        int resScale = 0;
        String rem = "3.4935796954060524114470681810486417234751682675102093970E-15";
        int remScale = 70;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result[] = aNumber.divideAndRemainder(bNumber);
        assertEquals("incorrect quotient value", res, result[0].toString());
        assertEquals("incorrect quotient scale", resScale, result[0].scale());
        assertEquals("incorrect remainder value", rem, result[1].toString());
        assertEquals("incorrect remainder scale", remScale, result[1].scale());
    }
    
    /**
     * divideAndRemainder(BigDecimal, MathContext)
     */
    public void testDivideAndRemainderMathContextUP() {
        String a = "3736186567876876578956958765675671119238118911893939591735";
        int aScale = 45;
        String b = "134432345432345748766876876723342238476237823787879183470";
        int bScale = 70;
        int precision = 75;
        RoundingMode rm = RoundingMode.UP;
        MathContext mc = new MathContext(precision, rm);
        String res = "277923185514690367474770683";
        int resScale = 0;
        String rem = "1.3032693871288309587558885943391070087960319452465789990E-15";
        int remScale = 70;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result[] = aNumber.divideAndRemainder(bNumber, mc);
        assertEquals("incorrect quotient value", res, result[0].toString());
        assertEquals("incorrect quotient scale", resScale, result[0].scale());
        assertEquals("incorrect remainder value", rem, result[1].toString());
        assertEquals("incorrect remainder scale", remScale, result[1].scale());
    }

    /**
     * divideAndRemainder(BigDecimal, MathContext)
     */
    public void testDivideAndRemainderMathContextDOWN() {
        String a = "3736186567876876578956958765675671119238118911893939591735";
        int aScale = 45;
        String b = "134432345432345748766876876723342238476237823787879183470";
        int bScale = 20;
        int precision = 15;
        RoundingMode rm = RoundingMode.DOWN;
        MathContext mc = new MathContext(precision, rm);
        String res = "0E-25";
        int resScale = 25;
        String rem = "3736186567876.876578956958765675671119238118911893939591735";
        int remScale = 45;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result[] = aNumber.divideAndRemainder(bNumber, mc);
        assertEquals("incorrect quotient value", res, result[0].toString());
        assertEquals("incorrect quotient scale", resScale, result[0].scale());
        assertEquals("incorrect remainder value", rem, result[1].toString());
        assertEquals("incorrect remainder scale", remScale, result[1].scale());
    }
    
    /**
     * remainder(BigDecimal)
     */
    public void testRemainder1() {
        String a = "3736186567876876578956958765675671119238118911893939591735";
        int aScale = 45;
        String b = "134432345432345748766876876723342238476237823787879183470";
        int bScale = 10;
        String res = "3736186567876.876578956958765675671119238118911893939591735";
        int resScale = 45;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.remainder(bNumber);
        assertEquals("incorrect quotient value", res, result.toString());
        assertEquals("incorrect quotient scale", resScale, result.scale());
    }

    /**
     * remainder(BigDecimal)
     */
    public void testRemainder2() {
        String a = "3736186567876876578956958765675671119238118911893939591735";
        int aScale = -45;
        String b = "134432345432345748766876876723342238476237823787879183470";
        int bScale = 10;
        String res = "1149310942946292909508821656680979993738625937.2065885780";
        int resScale = 10;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.remainder(bNumber);
        assertEquals("incorrect quotient value", res, result.toString());
        assertEquals("incorrect quotient scale", resScale, result.scale());
    }

    /**
     * remainder(BigDecimal, MathContext)
     */
    public void testRemainderMathContextHALF_UP() {
        String a = "3736186567876876578956958765675671119238118911893939591735";
        int aScale = 45;
        String b = "134432345432345748766876876723342238476237823787879183470";
        int bScale = 10;
        int precision = 15;
        RoundingMode rm = RoundingMode.HALF_UP;
        MathContext mc = new MathContext(precision, rm);
        String res = "3736186567876.876578956958765675671119238118911893939591735";
        int resScale = 45;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.remainder(bNumber, mc);
        assertEquals("incorrect quotient value", res, result.toString());
        assertEquals("incorrect quotient scale", resScale, result.scale());
    }

    /**
     * remainder(BigDecimal, MathContext)
     */
    public void testRemainderMathContextHALF_DOWN() {
        String a = "3736186567876876578956958765675671119238118911893939591735";
        int aScale = -45;
        String b = "134432345432345748766876876723342238476237823787879183470";
        int bScale = 10;
        int precision = 75;
        RoundingMode rm = RoundingMode.HALF_DOWN;
        MathContext mc = new MathContext(precision, rm);
        String res = "1149310942946292909508821656680979993738625937.2065885780";
        int resScale = 10;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = new BigDecimal(new BigInteger(b), bScale);
        BigDecimal result = aNumber.remainder(bNumber, mc);
        assertEquals("incorrect quotient value", res, result.toString());
        assertEquals("incorrect quotient scale", resScale, result.scale());
    }

    /**
     * round(BigDecimal, MathContext)
     */
    public void testRoundMathContextHALF_DOWN() {
        String a = "3736186567876876578956958765675671119238118911893939591735";
        int aScale = -45;
        int precision = 75;
        RoundingMode rm = RoundingMode.HALF_DOWN;
        MathContext mc = new MathContext(precision, rm);
        String res = "3.736186567876876578956958765675671119238118911893939591735E+102";
        int resScale = -45;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal result = aNumber.round(mc);
        assertEquals("incorrect quotient value", res, result.toString());
        assertEquals("incorrect quotient scale", resScale, result.scale());
    }

    /**
     * round(BigDecimal, MathContext)
     */
    public void testRoundMathContextHALF_UP() {
        String a = "3736186567876876578956958765675671119238118911893939591735";
        int aScale = 45;
        int precision = 15;
        RoundingMode rm = RoundingMode.HALF_UP;
        MathContext mc = new MathContext(precision, rm);
        String res = "3736186567876.88";
        int resScale = 2;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal result = aNumber.round(mc);
        assertEquals("incorrect quotient value", res, result.toString());
        assertEquals("incorrect quotient scale", resScale, result.scale());
    }

    /**
     * round(BigDecimal, MathContext) when precision = 0
     */
    public void testRoundMathContextPrecision0() {
        String a = "3736186567876876578956958765675671119238118911893939591735";
        int aScale = 45;
        int precision = 0;
        RoundingMode rm = RoundingMode.HALF_UP;
        MathContext mc = new MathContext(precision, rm);
        String res = "3736186567876.876578956958765675671119238118911893939591735";
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal result = aNumber.round(mc);
        assertEquals("incorrect quotient value", res, result.toString());
        assertEquals("incorrect quotient scale", aScale, result.scale());
    }


    /**
     * ulp() of a positive BigDecimal
     */
    public void testUlpPos() {
        String a = "3736186567876876578956958765675671119238118911893939591735";
        int aScale = -45;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal result = aNumber.ulp();
        String res = "1E+45";
        int resScale = -45;
        assertEquals("incorrect value", res, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * ulp() of a negative BigDecimal
     */
    public void testUlpNeg() {
        String a = "-3736186567876876578956958765675671119238118911893939591735";
        int aScale = 45;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal result = aNumber.ulp();
        String res = "1E-45";
        int resScale = 45;
        assertEquals("incorrect value", res, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }

    /**
     * ulp() of a negative BigDecimal
     */
    public void testUlpZero() {
        String a = "0";
        int aScale = 2;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal result = aNumber.ulp();
        String res = "0.01";
        int resScale = 2;
        assertEquals("incorrect value", res, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }
}
