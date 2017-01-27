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
 * The key specification of a RSA private key using Chinese Remainder Theorem
 * (CRT) values.
 * <p>
 * Defined in the <a
 * href="http://www.rsa.com/rsalabs/pubs/PKCS/html/pkcs-1.html">PKCS #1 v2.1</a>
 * standard.
 */
public class RSAPrivateCrtKeySpec extends RSAPrivateKeySpec {
    // Public Exponent
    private final BigInteger publicExponent;
    // Prime P
    private final BigInteger primeP;
    // Prime Q
    private final BigInteger primeQ;
    // Prime Exponent P
    private final BigInteger primeExponentP;
    // Prime Exponent Q
    private final BigInteger primeExponentQ;
    // CRT Coefficient
    private final BigInteger crtCoefficient;

    /**
     * Creates a new {@code RSAMultiPrimePrivateCrtKeySpec} with the specified
     * modulus, public exponent, private exponent, prime factors, prime
     * exponents, crt coefficient, and additional primes.
     *
     * @param modulus
     *            the modulus {@code n}.
     * @param publicExponent
     *            the public exponent {@code e}.
     * @param privateExponent
     *            the private exponent {@code d}.
     * @param primeP
     *            the prime factor {@code p} of {@code n}.
     * @param primeQ
     *            the prime factor {@code q} of {@code n}.
     * @param primeExponentP
     *            the exponent of the prime {@code p}.
     * @param primeExponentQ
     *            the exponent of the prime {@code q}.
     * @param crtCoefficient
     *            the CRT coefficient {@code q^-1 mod p}.
     */
    public RSAPrivateCrtKeySpec(BigInteger modulus,
                                BigInteger publicExponent,
                                BigInteger privateExponent,
                                BigInteger primeP,
                                BigInteger primeQ,
                                BigInteger primeExponentP,
                                BigInteger primeExponentQ,
                                BigInteger crtCoefficient) {

        super(modulus, privateExponent);

        this.publicExponent = publicExponent;
        this.primeP = primeP;
        this.primeQ = primeQ;
        this.primeExponentP = primeExponentP;
        this.primeExponentQ = primeExponentQ;
        this.crtCoefficient = crtCoefficient;
    }

    /**
     * Returns the CRT coefficient, {@code q^-1 mod p}.
     *
     * @return the CRT coefficient, {@code q^-1 mod p}.
     */
    public BigInteger getCrtCoefficient() {
        return crtCoefficient;
    }

    /**
     * Returns the exponent of the prime {@code p}.
     *
     * @return the exponent of the prime {@code p}.
     */
    public BigInteger getPrimeExponentP() {
        return primeExponentP;
    }

    /**
     * Returns the exponent of the prime {@code q}.
     *
     * @return the exponent of the prime {@code q}.
     */
    public BigInteger getPrimeExponentQ() {
        return primeExponentQ;
    }

    /**
     * Returns the prime factor {@code p}.
     *
     * @return the prime factor {@code p}.
     */
    public BigInteger getPrimeP() {
        return primeP;
    }

    /**
     * Returns the prime factor {@code q}.
     *
     * @return the prime factor {@code q}.
     */
    public BigInteger getPrimeQ() {
        return primeQ;
    }

    /**
     * Returns the public exponent {@code e}.
     *
     * @return the public exponent {@code e}.
     */
    public BigInteger getPublicExponent() {
        return publicExponent;
    }
}
