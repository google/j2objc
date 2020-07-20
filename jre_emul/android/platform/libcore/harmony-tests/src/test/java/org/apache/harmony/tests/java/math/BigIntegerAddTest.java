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
 * Method: add 
 */
public class BigIntegerAddTest extends TestCase {
    /**
     * Add two positive numbers of the same length
     */
    public void testCase1() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3};
        byte bBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {11, 22, 33, 44, 55, 66, 77, 11, 22, 33};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.add(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }

    /**
     * Add two negative numbers of the same length
     */
    public void testCase2() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3};
        byte bBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        int aSign = -1;
        int bSign = -1;        
        byte rBytes[] = {-12, -23, -34, -45, -56, -67, -78, -12, -23, -33};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.add(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }

    /**
     * Add two numbers of the same length.
     * The first one is positive and the second is negative.
     * The first one is greater in absolute value.
     */
    public void testCase3() {
        byte aBytes[] = {3, 4, 5, 6, 7, 8, 9};
        byte bBytes[] = {1, 2, 3, 4, 5, 6, 7};
        byte rBytes[] = {2, 2, 2, 2, 2, 2, 2};
        int aSign = 1;
        int bSign = -1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.add(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }

    /**
     * Add two numbers of the same length.
     * The first one is negative and the second is positive.
     * The first one is greater in absolute value.
     */
    public void testCase4() {
        byte aBytes[] = {3, 4, 5, 6, 7, 8, 9};
        byte bBytes[] = {1, 2, 3, 4, 5, 6, 7};
        byte rBytes[] = {-3, -3, -3, -3, -3, -3, -2};
        int aSign = -1;
        int bSign = 1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.add(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }

    /**
     * Add two numbers of the same length.
     * The first is positive and the second is negative.
     * The first is less in absolute value.
     */
    public void testCase5() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7};
        byte bBytes[] = {3, 4, 5, 6, 7, 8, 9};
        byte rBytes[] = {-3, -3, -3, -3, -3, -3, -2};
        int aSign = 1;
        int bSign = -1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.add(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }

    /**
     * Add two numbers of the same length.
     * The first one is negative and the second is positive.
     * The first one is less in absolute value.
     */
    public void testCase6() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7};
        byte bBytes[] = {3, 4, 5, 6, 7, 8, 9};
        byte rBytes[] = {2, 2, 2, 2, 2, 2, 2};
        int aSign = -1;
        int bSign = 1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.add(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }

    /**
     * Add two positive numbers of different length.
     * The first is longer.
     */
    public void testCase7() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 4, 5, 6, 7};
        byte bBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {1, 2, 3, 4, 15, 26, 37, 41, 52, 63, 74, 15, 26, 37};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.add(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }

    /**
     * Add two positive numbers of different length.
     * The second is longer.
     */
    public void testCase8() {
        byte aBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        byte bBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 4, 5, 6, 7};
        byte rBytes[] = {1, 2, 3, 4, 15, 26, 37, 41, 52, 63, 74, 15, 26, 37};
        BigInteger aNumber = new BigInteger(aBytes);
        BigInteger bNumber = new BigInteger(bBytes);
        BigInteger result = aNumber.add(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }

    /**
     * Add two negative numbers of different length.
     * The first is longer.
     */
    public void testCase9() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 4, 5, 6, 7};
        byte bBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        int aSign = -1;
        int bSign = -1;        
        byte rBytes[] = {-2, -3, -4, -5, -16, -27, -38, -42, -53, -64, -75, -16, -27, -37};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.add(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }

    /**
     * Add two negative numbers of different length.
     * The second is longer.
     */
    public void testCase10() {
        byte aBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        byte bBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 4, 5, 6, 7};
        int aSign = -1;
        int bSign = -1;        
        byte rBytes[] = {-2, -3, -4, -5, -16, -27, -38, -42, -53, -64, -75, -16, -27, -37};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.add(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }

    /**
     * Add two numbers of different length and sign.
     * The first is positive.
     * The first is longer.
     */
    public void testCase11() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 4, 5, 6, 7};
        byte bBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        int aSign = 1;
        int bSign = -1;        
        byte rBytes[] = {1, 2, 3, 3, -6, -15, -24, -40, -49, -58, -67, -6, -15, -23};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.add(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }

    /**
     * Add two numbers of different length and sign.
     * The first is positive.
     * The second is longer.
     */
    public void testCase12() {
        byte aBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        byte bBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 4, 5, 6, 7};
        int aSign = 1;
        int bSign = -1;        
        byte rBytes[] = {-2, -3, -4, -4, 5, 14, 23, 39, 48, 57, 66, 5, 14, 23};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.add(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }

    /**
     * Add two numbers of different length and sign.
     * The first is negative.
     * The first is longer.
     */
    public void testCase13() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 4, 5, 6, 7};
        byte bBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        int aSign = -1;
        int bSign = 1;        
        byte rBytes[] = {-2, -3, -4, -4, 5, 14, 23, 39, 48, 57, 66, 5, 14, 23};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.add(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }

    /**
     * Add two numbers of different length and sign.
     * The first is negative.
     * The second is longer.
     */
    public void testCase14() {
        byte aBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        byte bBytes[] = {1, 2, 3, 4, 5, 6, 7, 1, 2, 3, 4, 5, 6, 7};
        int aSign = -1;
        int bSign = 1;        
        byte rBytes[] = {1, 2, 3, 3, -6, -15, -24, -40, -49, -58, -67, -6, -15, -23};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.add(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    
    /**
     * Add two equal numbers of different signs
     */
    public void testCase15() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7};
        byte bBytes[] = {1, 2, 3, 4, 5, 6, 7};
        byte rBytes[] = {0};
        int aSign = -1;
        int bSign = 1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.add(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 0, result.signum());
    }

    /**
     * Add zero to a number
     */
    public void testCase16() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7};
        byte bBytes[] = {0};
        byte rBytes[] = {1, 2, 3, 4, 5, 6, 7};
        int aSign = 1;
        int bSign = 1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.add(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }

    /**
     * Add a number to zero
     */
    public void testCase17() {
        byte aBytes[] = {0};
        byte bBytes[] = {1, 2, 3, 4, 5, 6, 7};
        byte rBytes[] = {1, 2, 3, 4, 5, 6, 7};
        int aSign = 1;
        int bSign = 1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.add(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    
    /**
     * Add zero to zero
     */
    public void testCase18() {
        byte aBytes[] = {0};
        byte bBytes[] = {0};
        byte rBytes[] = {0};
        int aSign = 1;
        int bSign = 1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.add(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 0, result.signum());
    }
    
    /**
     * Add ZERO to a number
     */
    public void testCase19() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7};
        byte rBytes[] = {1, 2, 3, 4, 5, 6, 7};
        int aSign = 1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = BigInteger.ZERO;
        BigInteger result = aNumber.add(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }

    /**
     * Add a number to zero
     */
    public void testCase20() {
        byte bBytes[] = {1, 2, 3, 4, 5, 6, 7};
        byte rBytes[] = {1, 2, 3, 4, 5, 6, 7};
        int bSign = 1;
        BigInteger aNumber = BigInteger.ZERO;
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.add(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
    
    /**
     * Add ZERO to ZERO
     */
    public void testCase21() {
        byte rBytes[] = {0};
        BigInteger aNumber = BigInteger.ZERO;
        BigInteger bNumber = BigInteger.ZERO;
        BigInteger result = aNumber.add(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 0, result.signum());
    }

    /**
     * Add ONE to ONE
     */
    public void testCase22() {
        byte rBytes[] = {2};
        BigInteger aNumber = BigInteger.ONE;
        BigInteger bNumber = BigInteger.ONE;
        BigInteger result = aNumber.add(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }

    /**
     * Add two numbers so that carry is 1
     */
    public void testCase23() {
        byte aBytes[] = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        byte bBytes[] = {-1, -1, -1, -1, -1, -1, -1, -1};
        int aSign = 1;
        int bSign = 1;
        byte rBytes[] = {1, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -2};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.add(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }
}
