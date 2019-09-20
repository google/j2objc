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
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPrivateKeySpec;

/**
 * Tests for <code>RSAPrivateCrtKeySpec</code> class fields and methods
 *
 */
public class RSAPrivateCrtKeySpecTest extends TestCase {

    /**
     * Test #1 for <code>RSAPrivateCrtKeySpec</code> constructor
     * Assertion: Constructs <code>RSAPrivateCrtKeySpec</code>
     * object using valid parameters
     */
    public final void testRSAPrivateCrtKeySpec01() {
        KeySpec ks = new RSAPrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE);
        assertTrue(ks instanceof RSAPrivateCrtKeySpec);
    }

    /**
     * Test #2 for <code>RSAPrivateCrtKeySpec</code> constructor
     * Assertion: Constructs <code>RSAPrivateCrtKeySpec</code>
     * object using valid parameters
     */
    public final void testRSAPrivateCrtKeySpec02() {
        KeySpec ks = new RSAPrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE);
        assertTrue(ks instanceof RSAPrivateKeySpec);
    }

    /**
     * Test #3 for <code>RSAPrivateCrtKeySpec</code> constructor
     * Assertion: Constructs <code>RSAPrivateCrtKeySpec</code>
     * object using valid parameters
     */
    public final void testRSAPrivateCrtKeySpec03() {
        new RSAPrivateCrtKeySpec(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    /**
     * Test for <code>getCrtCoefficient()</code> method<br>
     * Assertion: returns crt coefficient
     */
    public final void testGetCrtCoefficient() {
        RSAPrivateCrtKeySpec ks = new RSAPrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.valueOf(5L));
        assertTrue(BigInteger.valueOf(5L).equals(ks.getCrtCoefficient()));
    }

    /**
     * Test for <code>getPrimeExponentP()</code> method<br>
     * Assertion: returns prime exponent P
     */
    public final void testGetPrimeExponentP() {
        RSAPrivateCrtKeySpec ks = new RSAPrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.valueOf(5L),
                BigInteger.ONE,
                BigInteger.ONE);
        assertTrue(BigInteger.valueOf(5L).equals(ks.getPrimeExponentP()));
    }

    /**
     * Test for <code>getPrimeExponentQ()</code> method<br>
     * Assertion: returns prime exponent Q
     */
    public final void testGetPrimeExponentQ() {
        RSAPrivateCrtKeySpec ks = new RSAPrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.valueOf(5L),
                BigInteger.ONE);
        assertTrue(BigInteger.valueOf(5L).equals(ks.getPrimeExponentQ()));
    }

    /**
     * Test for <code>getPrimeP()</code> method<br>
     * Assertion: returns prime P
     */
    public final void testGetPrimeP() {
        RSAPrivateCrtKeySpec ks = new RSAPrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.valueOf(5L),
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE);
        assertTrue(BigInteger.valueOf(5L).equals(ks.getPrimeP()));
    }

    /**
     * Test for <code>getPrimeQ()</code> method<br>
     * Assertion: returns prime Q
     */
    public final void testGetPrimeQ() {
        RSAPrivateCrtKeySpec ks = new RSAPrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.valueOf(5L),
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE);
        assertTrue(BigInteger.valueOf(5L).equals(ks.getPrimeQ()));
    }

    /**
     * Test for <code>getPublicExponent()</code> method<br>
     * Assertion: returns public exponent
     */
    public final void testGetPublicExponent() {
        RSAPrivateCrtKeySpec ks = new RSAPrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.valueOf(5L),
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE);
        assertTrue(BigInteger.valueOf(5L).equals(ks.getPublicExponent()));
    }

    //
    // Tests for inherited methods
    //

    /**
     * Test for <code>getModulus()</code> method<br>
     * Assertion: returns modulus
     */
    public final void testGetModulus() {
        RSAPrivateCrtKeySpec ks = new RSAPrivateCrtKeySpec(
                BigInteger.valueOf(5L),
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE);
        assertTrue(BigInteger.valueOf(5L).equals(ks.getModulus()));
    }

    /**
     * Test for <code>getPrivateExponent()</code> method<br>
     * Assertion: returns private exponent
     */
    public final void testGetPrivateExponent() {
        RSAPrivateCrtKeySpec ks = new RSAPrivateCrtKeySpec(
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.valueOf(5L),
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE,
                BigInteger.ONE);
        assertTrue(BigInteger.valueOf(5L).equals(ks.getPrivateExponent()));
    }

}
