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

import junit.framework.TestCase;
import java.math.BigInteger;

/**
 * Class:   java.math.BigInteger
 * Methods: divide, remainder, mod, and divideAndRemainder 
 */
public class BigIntegerDivideTest extends TestCase {
    /**
     * Divide by zero
     */
    public void testCase1() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7};
        byte bBytes[] = {0};
        int aSign = 1;
        int bSign = 0;        
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        try {
            aNumber.divide(bNumber);
            fail("ArithmeticException has not been caught");
        } catch (ArithmeticException e) {
        }
    }

    /**
     * Divide by ZERO
     */
    public void testCase2() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7};
        int aSign = 1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = BigInteger.ZERO;
        try {
            aNumber.divide(bNumber);
            fail("ArithmeticException has not been caught");
        } catch (ArithmeticException e) {
        }
    }

    /**
     * Divide two equal positive numbers
     */
    public void testCase3() {
        byte aBytes[] = {-127, 100, 56, 7, 98, -1, 39, -128, 127};
        byte bBytes[] = {-127, 100, 56, 7, 98, -1, 39, -128, 127};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {1};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.divide(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }

    /**
     * Divide two equal in absolute value numbers of different signs.
     */
    public void testCase4() {
        byte aBytes[] = {-127, 100, 56, 7, 98, -1, 39, -128, 127};
        byte bBytes[] = {-127, 100, 56, 7, 98, -1, 39, -128, 127};
        int aSign = -1;
        int bSign = 1;        
        byte rBytes[] = {-1};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.divide(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }

    /**
     * Divide two numbers of different length and different signs.
     * The second is longer.
     */
    public void testCase5() {
        byte aBytes[] = {-127, 100, 56, 7, 98, -1, 39, -128, 127};
        byte bBytes[] = {-127, 100, 56, 7, 98, -1, 39, -128, 127, 1, 2, 3, 4, 5};
        int aSign = -1;
        int bSign = 1;        
        byte rBytes[] = {0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.divide(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 0, result.signum());
    }

    /**
     * Divide two positive numbers of the same length.
     * The second is greater.
     */
    public void testCase6() {
        byte aBytes[] = {1, 100, 56, 7, 98, -1, 39, -128, 127};
        byte bBytes[] = {15, 100, 56, 7, 98, -1, 39, -128, 127};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.divide(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 0, result.signum());
    }

    /**
     * Divide two positive numbers.
     */
    public void testCase7() {
        byte aBytes[] = {1, 100, 56, 7, 98, -1, 39, -128, 127, 5, 6, 7, 8, 9};
        byte bBytes[] = {15, 48, -29, 7, 98, -1, 39, -128};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {23, 115, 11, 78, 35, -11};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.divide(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }

    /**
     * Divide a positive number by a negative one.
     */
    public void testCase8() {
        byte aBytes[] = {1, 100, 56, 7, 98, -1, 39, -128, 127, 5, 6, 7, 8, 9};
        byte bBytes[] = {15, 48, -29, 7, 98, -1, 39, -128};
        int aSign = 1;
        int bSign = -1;        
        byte rBytes[] = {-24, -116, -12, -79, -36, 11};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.divide(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }

    /**
     * Divide a negative number by a positive one.
     */
    public void testCase9() {
        byte aBytes[] = {1, 100, 56, 7, 98, -1, 39, -128, 127, 5, 6, 7, 8, 9};
        byte bBytes[] = {15, 48, -29, 7, 98, -1, 39, -128};
        int aSign = -1;
        int bSign = 1;        
        byte rBytes[] = {-24, -116, -12, -79, -36, 11};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.divide(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }

    /**
     * Divide two negative numbers.
     */
    public void testCase10() {
        byte aBytes[] = {1, 100, 56, 7, 98, -1, 39, -128, 127, 5, 6, 7, 8, 9};
        byte bBytes[] = {15, 48, -29, 7, 98, -1, 39, -128};
        int aSign = -1;
        int bSign = -1;        
        byte rBytes[] = {23, 115, 11, 78, 35, -11};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.divide(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }

    /**
     * Divide zero by a negative number.
     */
    public void testCase11() {
        byte aBytes[] = {0};
        byte bBytes[] = {15, 48, -29, 7, 98, -1, 39, -128};
        int aSign = 0;
        int bSign = -1;        
        byte rBytes[] = {0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.divide(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 0, result.signum());
    }
    
    /**
     * Divide ZERO by a negative number.
     */
    public void testCase12() {
        byte bBytes[] = {15, 48, -29, 7, 98, -1, 39, -128};
        int bSign = -1;        
        byte rBytes[] = {0};
        BigInteger aNumber = BigInteger.ZERO;
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.divide(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 0, result.signum());
    }

    /**
     * Divide a positive number by ONE.
     */
    public void testCase13() {
        byte aBytes[] = {15, 48, -29, 7, 98, -1, 39, -128};
        int aSign = 1;        
        byte rBytes[] = {15, 48, -29, 7, 98, -1, 39, -128};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = BigInteger.ONE;
        BigInteger result = aNumber.divide(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }

    /**
     * Divide ONE by ONE.
     */
    public void testCase14() {
        byte rBytes[] = {1};
        BigInteger aNumber = BigInteger.ONE;
        BigInteger bNumber = BigInteger.ONE;
        BigInteger result = aNumber.divide(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    
    /**
     * Verifies the case when borrow != 0 in the private divide method.
     */
    public void testDivisionKnuth1() {
        byte aBytes[] = {-7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7};
        byte bBytes[] = {-3, -3, -3, -3};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {0, -5, -12, -33, -96, -36, -105, -56, 92, 15, 48, -109};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.divide(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }

    /**
     * Verifies the case when the divisor is already normalized.
     */
    public void testDivisionKnuthIsNormalized() {
        byte aBytes[] = {-9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5};
        byte bBytes[] = {-1, -1, -1, -1, -1, -1, -1, -1};
        int aSign = -1;
        int bSign = -1;        
        byte rBytes[] = {0, -9, -8, -7, -6, -5, -4, -3};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.divide(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }

    /**
     * Verifies the case when the first digits of the dividend
     * and divisor equal.
     */
    public void testDivisionKnuthFirstDigitsEqual() {
        byte aBytes[] = {2, -3, -4, -5, -1, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5};
        byte bBytes[] = {2, -3, -4, -5, -1, -1, -1, -1};
        int aSign = -1;
        int bSign = -1;        
        byte rBytes[] = {0, -1, -1, -1, -1, -2, -88, -60, 41};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.divide(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }

    /**
     * Divide the number of one digit by the number of one digit 
     */
    public void testDivisionKnuthOneDigitByOneDigit() {
        byte aBytes[] = {113, -83, 123, -5};
        byte bBytes[] = {2, -3, -4, -5};
        int aSign = 1;
        int bSign = -1;        
        byte rBytes[] = {-37};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.divide(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }

    /**
     * Divide the number of multi digits by the number of one digit 
     */
    public void testDivisionKnuthMultiDigitsByOneDigit() {
        byte aBytes[] = {113, -83, 123, -5, 18, -34, 67, 39, -29};
        byte bBytes[] = {2, -3, -4, -5};
        int aSign = 1;
        int bSign = -1;        
        byte rBytes[] = {-38, 2, 7, 30, 109, -43};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.divide(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }

    /**
     * Remainder of division by zero
     */
    public void testCase15() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7};
        byte bBytes[] = {0};
        int aSign = 1;
        int bSign = 0;        
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        try {
            aNumber.remainder(bNumber);
            fail("ArithmeticException has not been caught");
        } catch (ArithmeticException e) {
        }
    }

    /**
     * Remainder of division of equal numbers
     */
    public void testCase16() {
        byte aBytes[] = {-127, 100, 56, 7, 98, -1, 39, -128, 127};
        byte bBytes[] = {-127, 100, 56, 7, 98, -1, 39, -128, 127};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.remainder(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 0, result.signum());
    }

    /**
     * Remainder of division of two positive numbers
     */
    public void testCase17() {
        byte aBytes[] = {-127, 100, 56, 7, 98, -1, 39, -128, 127, 75};
        byte bBytes[] = {27, -15, 65, 39, 100};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {12, -21, 73, 56, 27};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.remainder(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }

    /**
     * Remainder of division of two negative numbers
     */
    public void testCase18() {
        byte aBytes[] = {-127, 100, 56, 7, 98, -1, 39, -128, 127, 75};
        byte bBytes[] = {27, -15, 65, 39, 100};
        int aSign = -1;
        int bSign = -1;        
        byte rBytes[] = {-13, 20, -74, -57, -27};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.remainder(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }

    /**
     * Remainder of division of two numbers of different signs.
     * The first is positive.
     */
    public void testCase19() {
        byte aBytes[] = {-127, 100, 56, 7, 98, -1, 39, -128, 127, 75};
        byte bBytes[] = {27, -15, 65, 39, 100};
        int aSign = 1;
        int bSign = -1;        
        byte rBytes[] = {12, -21, 73, 56, 27};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.remainder(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    
    /**
     * Remainder of division of two numbers of different signs.
     * The first is negative.
     */
    public void testCase20() {
        byte aBytes[] = {-127, 100, 56, 7, 98, -1, 39, -128, 127, 75};
        byte bBytes[] = {27, -15, 65, 39, 100};
        int aSign = -1;
        int bSign = 1;        
        byte rBytes[] = {-13, 20, -74, -57, -27};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.remainder(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }

    /**
     * Tests the step D6 from the Knuth algorithm
     */
    public void testRemainderKnuth1() {
        byte aBytes[] = {-9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1};
        byte bBytes[] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {1, 2, 3, 4, 5, 6, 7, 7, 18, -89};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.remainder(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }

    /**
     * Divide the number of one digit by the number of one digit 
     */
    public void testRemainderKnuthOneDigitByOneDigit() {
        byte aBytes[] = {113, -83, 123, -5};
        byte bBytes[] = {2, -3, -4, -50};
        int aSign = 1;
        int bSign = -1;        
        byte rBytes[] = {2, -9, -14, 53};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.remainder(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }

    /**
     * Divide the number of multi digits by the number of one digit 
     */
    public void testRemainderKnuthMultiDigitsByOneDigit() {
        byte aBytes[] = {113, -83, 123, -5, 18, -34, 67, 39, -29};
        byte bBytes[] = {2, -3, -4, -50};
        int aSign = 1;
        int bSign = -1;        
        byte rBytes[] = {2, -37, -60, 59};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.remainder(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }

    /**
     * divideAndRemainder of two numbers of different signs.
     * The first is negative.
     */
    public void testCase21() {
        byte aBytes[] = {-127, 100, 56, 7, 98, -1, 39, -128, 127, 75};
        byte bBytes[] = {27, -15, 65, 39, 100};
        int aSign = -1;
        int bSign = 1;        
        byte rBytes[][] = {
                {-5, 94, -115, -74, -85, 84},
                {-13, 20, -74, -57, -27}
        };
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result[] = aNumber.divideAndRemainder(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result[0].toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            if (resBytes[i] != rBytes[0][i]) {
                fail("Incorrect quotation");
            }
        }
        assertEquals(-1, result[0].signum());
        resBytes = result[1].toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            if (resBytes[i] != rBytes[1][i]) {
                fail("Incorrect remainder");
            }
            assertEquals(-1, result[1].signum());
        }
    }
    
    /**
     * mod when modulus is negative
     */
    public void testCase22() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7};
        byte bBytes[] = {1, 30, 40, 56, -1, 45};
        int aSign = 1;
        int bSign = -1;        
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        try {
            aNumber.mod(bNumber);
            fail("ArithmeticException has not been caught");
        } catch (ArithmeticException e) {
        }
    }

    /**
     * mod when a divisor is positive
     */
    public void testCase23() {
        byte aBytes[] = {-127, 100, 56, 7, 98, -1, 39, -128, 127, 75};
        byte bBytes[] = {27, -15, 65, 39, 100};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {12, -21, 73, 56, 27};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.mod(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }

    /**
     * mod when a divisor is negative
     */
    public void testCase24() {
        byte aBytes[] = {-127, 100, 56, 7, 98, -1, 39, -128, 127, 75};
        byte bBytes[] = {27, -15, 65, 39, 100};
        int aSign = -1;
        int bSign = 1;        
        byte rBytes[] = {15, 5, -9, -17, 73};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.mod(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
}
