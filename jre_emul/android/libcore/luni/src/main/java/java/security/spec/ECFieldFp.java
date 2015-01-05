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
 * The parameters specifying a <i>prime finite field</i> of an
 * elliptic curve.
 */
public class ECFieldFp implements ECField {
    // Prime
    private final BigInteger p;

    /**
     * Creates a new prime finite field of an elliptic curve with the specified
     * prime {@code p}.
     *
     * @param p
     *            the prime value {@code p}.
     * @throws IllegalArgumentException
     *             if {@code p <= zero}.
     */
    public ECFieldFp(BigInteger p) {
        this.p = p;

        if (this.p == null) {
            throw new NullPointerException("p == null");
        }
        if (this.p.signum() != 1) {
            throw new IllegalArgumentException("p <= 0");
        }
    }

    /**
     * Returns the size of the finite field (in bits).
     *
     * @return the size of the finite field (in bits).
     */
    public int getFieldSize() {
        return p.bitLength();
    }

    /**
     * Returns the prime value {@code p} for this finite field.
     *
     * @return the prime value {@code p} for this finite field.
     */
    public BigInteger getP() {
        return p;
    }

    /**
     * Returns whether the specified object is equal to this finite field.
     *
     * @param obj
     *            the object to compare to this finite field.
     * @return {@code true} if the specified object is equal to this finite field,
     *         otherwise {@code false}.
     */
    public boolean equals(Object obj) {
        // object equals itself
        if (this == obj) {
            return true;
        }
        if (obj instanceof ECFieldFp) {
            return (this.p.equals(((ECFieldFp)obj).p));
        }
        return false;
    }

    /**
     * Returns the hashcode value for this finite field.
     *
     * @return the hashcode value for this finite field.
     */
    public int hashCode() {
        return p.hashCode();
    }
}
