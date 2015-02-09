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

package java.security.interfaces;

import java.math.BigInteger;
import java.security.spec.RSAOtherPrimeInfo;

/**
 * The interface for a Multi-Prime RSA private key. Specified by <a
 * href="http://www.rsa.com/rsalabs/node.asp?id=2125">PKCS #1 v2.0 Amendment 1:
 * Multi-Prime RSA</a>.
 */
public interface RSAMultiPrimePrivateCrtKey extends RSAPrivateKey {

    /**
     * the serial version identifier.
     */
    public static final long serialVersionUID = 618058533534628008L;

    /**
     * Returns the CRT coefficient, {@code q^-1 mod p}.
     *
     * @return the CRT coefficient.
     */
    public BigInteger getCrtCoefficient();

    /**
     * Returns the information for the additional primes.
     *
     * @return the information for the additional primes, or {@code null} if
     *         there are only the two primes ({@code p, q}),
     */
    public RSAOtherPrimeInfo[] getOtherPrimeInfo();

    /**
     * Returns the prime factor {@code p} of {@code n}.
     *
     * @return the prime factor {@code p} of {@code n}.
     */
    public BigInteger getPrimeP();

    /**
     * Returns the prime factor {@code q} of {@code n}.
     *
     * @return the prime factor {@code q} of {@code n}.
     */
    public BigInteger getPrimeQ();

    /**
     * Returns the CRT exponent of the prime {@code p}.
     *
     * @return the CRT exponent of the prime {@code p}.
     */
    public BigInteger getPrimeExponentP();

    /**
     * Returns the CRT exponent of the prime {@code q}.
     *
     * @return the CRT exponent of the prime {@code q}.
     */
    public BigInteger getPrimeExponentQ();

    /**
     * Returns the public exponent {@code e}.
     *
     * @return the public exponent {@code e}.
     */
    public BigInteger getPublicExponent();
}
