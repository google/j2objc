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
 * Class:  java.math.BigInteger
 * Method: subtract 
 */
public class BigIntegerSubtractTest extends TestCase {
    /**
     * Subtract two positive numbers of the same length.
     * The first is greater.
     */
    public void testCase1() {
        byte aBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        byte bBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {9, 18, 27, 36, 45, 54, 63, 9, 18, 27};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.subtract(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals(1, result.signum());
    }

    /**
     * Subtract two positive numbers of the same length.
     * The second is greater.
     */
    public void testCase2() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3};
        byte bBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {-10, -19, -28, -37, -46, -55, -64, -10, -19, -27};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.subtract(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals(-1, result.signum());
    }

    /**
     * Subtract two numbers of the same length and different signs.
     * The first is positive.
     * The first is greater in absolute value.
     */
    public void testCase3() {
        byte aBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        byte bBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3};
        int aSign = 1;
        int bSign = -1;        
        byte rBytes[] = {11, 22, 33, 44, 55, 66, 77, 11, 22, 33};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.subtract(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals(1, result.signum());
    }

    /**
     * Subtract two numbers of the same length and different signs.
     * The first is positive.
     * The second is greater in absolute value.
     */
    public void testCase4() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3};
        byte bBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        int aSign = 1;
        int bSign = -1;        
        byte rBytes[] = {11, 22, 33, 44, 55, 66, 77, 11, 22, 33};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.subtract(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals(1, result.signum());
    }

    /**
     * Subtract two negative numbers of the same length.
     * The first is greater in absolute value.
     */
    public void testCase5() {
        byte aBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        byte bBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3};
        int aSign = -1;
        int bSign = -1;        
        byte rBytes[] = {-10, -19, -28, -37, -46, -55, -64, -10, -19, -27};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.subtract(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals(-1, result.signum());
    }

    /**
     * Subtract two negative numbers of the same length.
     * The second is greater in absolute value.
     */
    public void testCase6() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3};
        byte bBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        int aSign = -1;
        int bSign = -1;        
        byte rBytes[] = {9, 18, 27, 36, 45, 54, 63, 9, 18, 27};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.subtract(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals(1, result.signum());
    }

    /**
     * Subtract two numbers of the same length and different signs.
     * The first is negative.
     * The first is greater in absolute value.
     */
    public void testCase7() {
        byte aBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        byte bBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3};
        int aSign = -1;
        int bSign = 1;        
        byte rBytes[] = {-12, -23, -34, -45, -56, -67, -78, -12, -23, -33};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.subtract(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals(-1, result.signum());
    }

    /**
     * Subtract two numbers of the same length and different signs.
     * The first is negative.
     * The second is greater in absolute value.
     */
    public void testCase8() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3};
        byte bBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        int aSign = -1;
        int bSign = 1;        
        byte rBytes[] = {-12, -23, -34, -45, -56, -67, -78, -12, -23, -33};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.subtract(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals(-1, result.signum());
    }
    
    /**
     * Subtract two positive numbers of different length.
     * The first is longer.
     */
    public void testCase9() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 4, 5, 6, 7};
        byte bBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {1, 2, 3, 3, -6, -15, -24, -40, -49, -58, -67, -6, -15, -23};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.subtract(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals(1, result.signum());
    }
    
    /**
     * Subtract two positive numbers of different length.
     * The second is longer.
     */
    public void testCase10() {
        byte aBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        byte bBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 4, 5, 6, 7};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {-2, -3, -4, -4, 5, 14, 23, 39, 48, 57, 66, 5, 14, 23};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.subtract(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals(-1, result.signum());
    }

    /**
     * Subtract two numbers of different length and different signs.
     * The first is positive.
     * The first is greater in absolute value.
     */
    public void testCase11() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 4, 5, 6, 7};
        byte bBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        int aSign = 1;
        int bSign = -1;        
        byte rBytes[] = {1, 2, 3, 4, 15, 26, 37, 41, 52, 63, 74, 15, 26, 37};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.subtract(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals(1, result.signum());
    }

    /**
     * Subtract two numbers of the same length and different signs.
     * The first is positive.
     * The second is greater in absolute value.
     */
    public void testCase12() {
        byte aBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        byte bBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 4, 5, 6, 7};
        int aSign = 1;
        int bSign = -1;        
        byte rBytes[] = {1, 2, 3, 4, 15, 26, 37, 41, 52, 63, 74, 15, 26, 37};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.subtract(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals(1, result.signum());
    }
    
    /**
     * Subtract two numbers of different length and different signs.
     * The first is negative.
     * The first is longer.
     */
    public void testCase13() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 4, 5, 6, 7};
        byte bBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        int aSign = -1;
        int bSign = 1;        
        byte rBytes[] = {-2, -3, -4, -5, -16, -27, -38, -42, -53, -64, -75, -16, -27, -37};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.subtract(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals(-1, result.signum());
    }

    /**
     * Subtract two numbers of the same length and different signs.
     * The first is negative.
     * The second is longer.
     */
    public void testCase14() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 4, 5, 6, 7};
        byte bBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        int aSign = -1;
        int bSign = 1;        
        byte rBytes[] = {-2, -3, -4, -5, -16, -27, -38, -42, -53, -64, -75, -16, -27, -37};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.subtract(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals(-1, result.signum());
    }

    /**
     * Subtract two negative numbers of different length.
     * The first is longer.
     */
    public void testCase15() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 4, 5, 6, 7};
        byte bBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        int aSign = -1;
        int bSign = -1;        
        byte rBytes[] = {-2, -3, -4, -4, 5, 14, 23, 39, 48, 57, 66, 5, 14, 23};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.subtract(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals(-1, result.signum());
}
    
    /**
     * Subtract two negative numbers of different length.
     * The second is longer.
     */
    public void testCase16() {
        byte aBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        byte bBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 4, 5, 6, 7};
        int aSign = -1;
        int bSign = -1;        
        byte rBytes[] = {1, 2, 3, 3, -6, -15, -24, -40, -49, -58, -67, -6, -15, -23};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.subtract(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals(1, result.signum());
    }
    
    /**
     * Subtract two positive equal in absolute value numbers.
     */
    public void testCase17() {
        byte aBytes[] = {-120, 34, 78, -23, -111, 45, 127, 23, 45, -3};
        byte bBytes[] = {-120, 34, 78, -23, -111, 45, 127, 23, 45, -3};
        byte rBytes[] = {0};
        int aSign = 1;
        int bSign = 1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.subtract(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals(0, result.signum());
    }

    /**
     * Subtract zero from a number.
     * The number is positive.
     */
    public void testCase18() {
        byte aBytes[] = {120, 34, 78, -23, -111, 45, 127, 23, 45, -3};
        byte bBytes[] = {0};
        byte rBytes[] = {120, 34, 78, -23, -111, 45, 127, 23, 45, -3};
        int aSign = 1;
        int bSign = 0;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.subtract(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals(1, result.signum());
    }

    /**
     * Subtract a number from zero.
     * The number is negative.
     */
    public void testCase19() {
        byte aBytes[] = {0};
        byte bBytes[] = {120, 34, 78, -23, -111, 45, 127, 23, 45, -3};
        byte rBytes[] = {120, 34, 78, -23, -111, 45, 127, 23, 45, -3};
        int aSign = 0;
        int bSign = -1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.subtract(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals(1, result.signum());
    }

    /**
     * Subtract zero from zero.
     */
    public void testCase20() {
        byte aBytes[] = {0};
        byte bBytes[] = {0};
        byte rBytes[] = {0};
        int aSign = 0;
        int bSign = 0;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.subtract(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals(0, result.signum());
    }
    
    /**
     * Subtract ZERO from a number.
     * The number is positive.
     */
    public void testCase21() {
        byte aBytes[] = {120, 34, 78, -23, -111, 45, 127, 23, 45, -3};
        byte rBytes[] = {120, 34, 78, -23, -111, 45, 127, 23, 45, -3};
        int aSign = 1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = BigInteger.ZERO;
        BigInteger result = aNumber.subtract(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals(1, result.signum());
    }

    /**
     * Subtract a number from ZERO.
     * The number is negative.
     */
    public void testCase22() {
        byte bBytes[] = {120, 34, 78, -23, -111, 45, 127, 23, 45, -3};
        byte rBytes[] = {120, 34, 78, -23, -111, 45, 127, 23, 45, -3};
        int bSign = -1;
        BigInteger aNumber = BigInteger.ZERO;
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.subtract(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals(1, result.signum());
    }

    /**
     * Subtract ZERO from ZERO.
     */
    public void testCase23() {
        byte rBytes[] = {0};
        BigInteger aNumber = BigInteger.ZERO;
        BigInteger bNumber = BigInteger.ZERO;
        BigInteger result = aNumber.subtract(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals(0, result.signum());
    }

    /**
     * Subtract ONE from ONE.
     */
    public void testCase24() {
        byte rBytes[] = {0};
        BigInteger aNumber = BigInteger.ONE;
        BigInteger bNumber = BigInteger.ONE;
        BigInteger result = aNumber.subtract(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals(0, result.signum());
    }

    /**
     * Subtract two numbers so that borrow is 1.
     */
    public void testCase25() {
        byte aBytes[] = {-1, -1, -1, -1, -1, -1, -1, -1};
        byte bBytes[] = {-128, -128, -128, -128, -128, -128, -128, -128, -128};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {-128, 127, 127, 127, 127, 127, 127, 127, 127};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.subtract(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals(-1, result.signum());
    }
}

