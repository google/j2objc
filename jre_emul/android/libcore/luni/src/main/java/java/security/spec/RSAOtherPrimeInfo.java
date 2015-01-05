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

package java.security.spec;

import java.math.BigInteger;

/**
 * The additional prime information specified as triplet of primes, a prime
 * exponent, and a Chinese Remainder Theorem (CRT) coefficient.
 * <p>
 * Defined in the <a
 * href="http://www.rsa.com/rsalabs/pubs/PKCS/html/pkcs-1.html">PKCS #1 v2.1</a>
 * standard.
 */
public class RSAOtherPrimeInfo {
    // Prime
    private final BigInteger prime;
    // Prime Exponent
    private final BigInteger primeExponent;
    // CRT Coefficient
    private final BigInteger crtCoefficient;

    /**
     * Creates a new {@code RSAOtherPrimeInfo} with the specified prime,
     * exponent, and CRT coefficient.
     *
     * @param prime
     *            the prime factor.
     * @param primeExponent
     *            the prime exponent.
     * @param crtCoefficient
     *            the CRT coefficient.
     */
    public RSAOtherPrimeInfo(BigInteger prime,
            BigInteger primeExponent, BigInteger crtCoefficient) {
        if (prime == null) {
            throw new NullPointerException("prime == null");
        }
        if (primeExponent == null) {
            throw new NullPointerException("primeExponent == null");
        }
        if (crtCoefficient == null) {
            throw new NullPointerException("crtCoefficient == null");
        }
        this.prime = prime;
        this.primeExponent = primeExponent;
        this.crtCoefficient = crtCoefficient;
    }

    /**
     * Returns the CRT coefficient.
     *
     * @return the CRT coefficient.
     */
    public final BigInteger getCrtCoefficient() {
        return crtCoefficient;
    }

    /**
     * Returns the prime factor.
     *
     * @return the prime factor.
     */
    public final BigInteger getPrime() {
        return prime;
    }

    /**
     * Returns the exponent.
     *
     * @return the exponent.
     */
    public final BigInteger getExponent() {
        return primeExponent;
    }
}
