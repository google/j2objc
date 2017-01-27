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
 * The key specification of a RSA private key.
 * <p>
 * Defined in the <a
 * href="http://www.rsa.com/rsalabs/pubs/PKCS/html/pkcs-1.html">PKCS #1 v2.1</a>
 * standard
 */
public class RSAPrivateKeySpec implements KeySpec {
    // Modulus
    private final BigInteger modulus;
    // Private Exponent
    private final BigInteger privateExponent;

    /**
     * Creates a new {@code RSAPrivateKeySpec} with the specified modulus and
     * private exponent.
     *
     * @param modulus
     *            the modulus {@code n}.
     * @param privateExponent
     *            the private exponent {@code e}
     */
    public RSAPrivateKeySpec(BigInteger modulus, BigInteger privateExponent) {
        this.modulus = modulus;
        this.privateExponent = privateExponent;
    }

    /**
     * Returns the modulus {@code n}.
     *
     * @return the modulus {@code n}.
     */
    public BigInteger getModulus() {
        return modulus;
    }

    /**
     * Returns the private exponent {@code e}.
     *
     * @return the private exponent {@code e}.
     */
    public BigInteger getPrivateExponent() {
        return privateExponent;
    }
}
