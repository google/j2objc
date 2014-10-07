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
 * Methods: and, andNot
 */
public class BigIntegerNotTest extends TestCase {
    /**
     * andNot for two positive numbers; the first is longer
     */
    public void testAndNotPosPosFirstLonger() {
        byte aBytes[] = {-128, 9, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, -117, 23, 87, -25, -75};
        byte bBytes[] = {-2, -3, -4, -4, 5, 14, 23, 39, 48, 57, 66, 5, 14, 23};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {0, -128, 9, 56, 100, 0, 0, 1, 1, 90, 1, -32, 0, 10, -126, 21, 82, -31, -96};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.andNot(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }

    /**
     * andNot for two positive numbers; the first is shorter
     */
    public void testAndNotPosPosFirstShorter() {
        byte aBytes[] = {-2, -3, -4, -4, 5, 14, 23, 39, 48, 57, 66, 5, 14, 23};
        byte bBytes[] = {-128, 9, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, -117, 23, 87, -25, -75};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {73, -92, -48, 4, 12, 6, 4, 32, 48, 64, 0, 8, 2};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.andNot(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }

    /**
     * andNot for two negative numbers; the first is longer
     */
    public void testAndNotNegNegFirstLonger() {
        byte aBytes[] = {-128, 9, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, -117, 23, 87, -25, -75};
        byte bBytes[] = {-2, -3, -4, -4, 5, 14, 23, 39, 48, 57, 66, 5, 14, 23};
        int aSign = -1;
        int bSign = -1;        
        byte rBytes[] = {73, -92, -48, 4, 12, 6, 4, 32, 48, 64, 0, 8, 2};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.andNot(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }

    /**
     * andNot for a negative and a positive numbers; the first is longer
     */
    public void testNegPosFirstLonger() {
        byte aBytes[] = {-128, 9, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, -117, 23, 87, -25, -75};
        byte bBytes[] = {-2, -3, -4, -4, 5, 14, 23, 39, 48, 57, 66, 5, 14, 23};
        int aSign = -1;
        int bSign = 1;        
        byte rBytes[] = {-1, 127, -10, -57, -101, 1, 2, 2, 2, -96, -16, 8, -40, -59, 68, -88, -88, 16, 72};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.andNot(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }

    /**
     * Not for ZERO 
     */
    public void testNotZero() {
        byte rBytes[] = {-1};
        BigInteger aNumber = BigInteger.ZERO;
        BigInteger result = aNumber.not();
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }

    /**
     * Not for ONE
     */
    public void testNotOne() {
        byte rBytes[] = {-2};
        BigInteger aNumber = BigInteger.ONE;
        BigInteger result = aNumber.not();
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }

    /**
     * Not for a positive number
     */
    public void testNotPos() {
        byte aBytes[] = {-128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, -117};
        int aSign = 1;
        byte rBytes[] = {-1, 127, -57, -101, 1, 75, -90, -46, -92, -4, 14, -36, -27, 116};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.not();
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }

    /**
     * Not for a negative number
     */
    public void testNotNeg() {
        byte aBytes[] = {-128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, -117};
        int aSign = -1;
        byte rBytes[] = {0, -128, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, -118};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.not();
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }

    /**
     * Not for a negative number
     */

    public void testNotSpecialCase() {
        byte aBytes[] = {-1, -1, -1, -1};
        int aSign = 1;
        byte rBytes[] = {-1, 0, 0, 0, 0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.not();
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", -1, result.signum());
    }
}