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
* @author Vladimir N. Molotkov
* @version $Revision$
*/

package tests.security.spec;

import junit.framework.TestCase;

import java.math.BigInteger;
import java.security.spec.RSAOtherPrimeInfo;

/**
 * Tests for <code>RSAOtherPrimeInfo</code> class fields and methods.
 *
 */
public class RSAOtherPrimeInfoTest extends TestCase {

    /**
     * Test #1 for <code>RSAOtherPrimeInfo(BigInteger,BigInteger,BigInteger)</code> ctor
     * Assertion: constructs <code>RSAOtherPrimeInfo</code>
     * object using valid parameter
     */
    public final void testRSAOtherPrimeInfo01() {
        Object o =
            new RSAOtherPrimeInfo(BigInteger.valueOf(1L),
                                  BigInteger.valueOf(2L),
                                  BigInteger.valueOf(3L));
        assertTrue(o instanceof RSAOtherPrimeInfo);
    }

    /**
     * Test #2 for <code>RSAOtherPrimeInfo(BigInteger,BigInteger,BigInteger)</code> ctor
     * Assertion: NullPointerException if prime is null
     */
    public final void testRSAOtherPrimeInfo02() {
        try {
            new RSAOtherPrimeInfo(null,
                                  BigInteger.valueOf(2L),
                                  BigInteger.valueOf(3L));
            fail("Expected NPE not thrown");
        } catch (NullPointerException e) {
        }
    }

    /**
     * Test #3 for <code>RSAOtherPrimeInfo(BigInteger,BigInteger,BigInteger)</code> ctor
     * Assertion: NullPointerException if primeExponent is null
     */
    public final void testRSAOtherPrimeInfo03() {
        try {
            new RSAOtherPrimeInfo(BigInteger.valueOf(1L),
                                  null,
                                  BigInteger.valueOf(3L));
            fail("Expected NPE not thrown");
        } catch (NullPointerException e) {
        }
    }

    /**
     * Test #4 for <code>RSAOtherPrimeInfo(BigInteger,BigInteger,BigInteger)</code> ctor
     * Assertion: NullPointerException if crtCoefficient is null
     */
    public final void testRSAOtherPrimeInfo04() {
        try {
            new RSAOtherPrimeInfo(BigInteger.valueOf(1L),
                                  BigInteger.valueOf(2L),
                                  null);
            fail("Expected NPE not thrown");
        } catch (NullPointerException e) {
        }
    }

    /**
     * Test #5 for <code>RSAOtherPrimeInfo(BigInteger,BigInteger,BigInteger)</code> ctor
     * Assertion: NullPointerException if prime and crtCoefficient is null
     */
    public final void testRSAOtherPrimeInfo05() {
        try {
            new RSAOtherPrimeInfo(null,
                                  BigInteger.valueOf(2L),
                                  null);
            fail("Expected NPE not thrown");
        } catch (NullPointerException e) {
        }
    }

    /**
     * Test for <code>getCrtCoefficient()</code> method<br>
     * Assertion: returns CRT coefficient value
     */
    public final void testGetCrtCoefficient() {
        RSAOtherPrimeInfo ropi =
            new RSAOtherPrimeInfo(BigInteger.valueOf(1L),
                                  BigInteger.valueOf(2L),
                                  BigInteger.valueOf(3L));
        assertEquals(3L, ropi.getCrtCoefficient().longValue());
    }

    /**
     * Test for <code>getPrime()</code> method<br>
     * Assertion: returns prime value
     */
    public final void testGetPrime() {
        RSAOtherPrimeInfo ropi =
            new RSAOtherPrimeInfo(BigInteger.valueOf(1L),
                                  BigInteger.valueOf(2L),
                                  BigInteger.valueOf(3L));
        assertEquals(1L, ropi.getPrime().longValue());
    }

    /**
     * Test for <code>getExponent()</code> method<br>
     * Assertion: returns prime exponent value
     */
    public final void testGetExponent() {
        RSAOtherPrimeInfo ropi =
            new RSAOtherPrimeInfo(BigInteger.valueOf(1L),
                                  BigInteger.valueOf(2L),
                                  BigInteger.valueOf(3L));
        assertEquals(2L, ropi.getExponent().longValue());
    }

}
