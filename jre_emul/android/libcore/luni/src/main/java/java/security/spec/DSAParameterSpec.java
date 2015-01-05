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
import java.security.interfaces.DSAParams;

/**
 * The parameter specification used with the Digital Signature Algorithm (DSA).
 */
public class DSAParameterSpec implements AlgorithmParameterSpec, DSAParams {
    // Prime
    private final BigInteger p;
    // Sub-prime
    private final BigInteger q;
    // Base
    private final BigInteger g;

    /**
     * Creates a new {@code DSAParameterSpec} with the specified prime {@code p},
     * sub-prime {@code q} and the base {@code g}.
     *
     * @param p
     *            the prime {@code p}.
     * @param q
     *            the sub-prime {@code q}.
     * @param g
     *            the base {@code g};
     */
    public DSAParameterSpec(BigInteger p, BigInteger q, BigInteger g) {
        this.p = p;
        this.q = q;
        this.g = g;
    }

    /**
     * Returns the base {@code g}.
     *
     * @return the base {@code g}.
     */
    public BigInteger getG() {
        return g;
    }

    /**
     * Returns the prime {@code p}.
     *
     * @return the prime {@code p}.
     */
    public BigInteger getP() {
        return p;
    }

    /**
     * Returns the sub-prime {@code q}.
     *
     * @return the sub-prime {@code q}.
     */
    public BigInteger getQ() {
        return q;
    }
}
