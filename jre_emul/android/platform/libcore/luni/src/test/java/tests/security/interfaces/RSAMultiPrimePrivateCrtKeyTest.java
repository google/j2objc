/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.security.interfaces;

import junit.framework.TestCase;

import java.math.BigInteger;
import java.security.interfaces.RSAMultiPrimePrivateCrtKey;
import java.security.spec.RSAOtherPrimeInfo;

import org.apache.harmony.security.tests.support.interfaces.RSAMultiPrimePrivateCrtKeyImpl;

public class RSAMultiPrimePrivateCrtKeyTest extends TestCase {

    /**
     * Reference array of RSAOtherPrimeInfo. DO NOT MODIFY
     */
    private static final RSAOtherPrimeInfo[] opi = new RSAOtherPrimeInfo[] {
            new RSAOtherPrimeInfo(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE),
            new RSAOtherPrimeInfo(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE),
            new RSAOtherPrimeInfo(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE)
    };

    private final BigInteger publicExponent = BigInteger.ONE;
    private final BigInteger primeExponentP = BigInteger.ONE;
    private final BigInteger primeExponentQ = BigInteger.ONE;
    private final BigInteger primeP = BigInteger.ONE;
    private final BigInteger primeQ = BigInteger.ONE;
    private final BigInteger crtCoefficient = BigInteger.ONE;

    class RSAMulti extends RSAMultiPrimePrivateCrtKeyImpl {
        public RSAMulti(BigInteger publicExp,
                        BigInteger primeExpP,
                        BigInteger primeExpQ,
                        BigInteger prP,
                        BigInteger prQ,
                        BigInteger crtCft,
                        RSAOtherPrimeInfo[] otherPrmInfo) {
            super(publicExp, primeExpP, primeExpQ, prP, prQ, crtCft, otherPrmInfo);
        }
    }

    /**
     * java.security.interfaces.RSAMultiPrimePrivateCrtKey#getCrtCoefficient()
     * java.security.interfaces.RSAMultiPrimePrivateCrtKey#getPrimeExponentP()
     * java.security.interfaces.RSAMultiPrimePrivateCrtKey#getPrimeExponentQ()
     * java.security.interfaces.RSAMultiPrimePrivateCrtKey#getPrimeP()
     * java.security.interfaces.RSAMultiPrimePrivateCrtKey#getPrimeQ()
     * java.security.interfaces.RSAMultiPrimePrivateCrtKey#getPublicExponent()
     */
    public void test_RSAMultiPrimePrivateCrtKey() {
        RSAMulti rsam = new RSAMulti(publicExponent, primeExponentP, primeExponentQ,
                                     primeP, primeQ, crtCoefficient, opi);
        try {
            assertEquals(rsam.getCrtCoefficient(), crtCoefficient);
            assertEquals(rsam.getPrimeExponentP(), primeExponentP);
            assertEquals(rsam.getPrimeExponentQ(), primeExponentQ);
            assertEquals(rsam.getPrimeP(), primeP);
            assertEquals(rsam.getPrimeQ(), primeQ);
            assertEquals(rsam.getPublicExponent(), publicExponent);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    /**
     * java.security.interfaces.RSAMultiPrimePrivateCrtKey#getOtherPrimeInfo()
     */
    public void test_getOtherPrimeInfo() {
        RSAMulti rsam = new RSAMulti(publicExponent, primeExponentP, primeExponentQ,
                                     primeP, primeQ, crtCoefficient, null);
        try {
            assertNull("Object RSAOtherPrimeInfo is not NULL", rsam.getOtherPrimeInfo());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        rsam = new RSAMulti(publicExponent, primeExponentP, primeExponentQ,
                            primeP, primeQ, crtCoefficient, opi);

        try {
            assertEquals(rsam.getOtherPrimeInfo(), opi);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
}
