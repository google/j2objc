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
import java.util.Arrays;

/**
 * An Elliptic Curve with its necessary values.
 */
public class EllipticCurve {

    // Underlying finite field
    private final ECField field;

    // The first coefficient of the equation defining this elliptic curve
    private final BigInteger a;

    // The second coefficient of the equation defining this elliptic curve
    private final BigInteger b;

    // Bytes used during this elliptic curve generation,
    // if it was generated randomly
    private final byte[] seed;

    // Hash code
    private volatile int hash;

    /**
     * Creates a new {@code EllipticCurve} with the specified field,
     * coefficients and seed.
     *
     * @param field
     *            the finite field of this elliptic curve.
     * @param a
     *            the coefficient {@code a}.
     * @param b
     *            the coefficient {@code b}.
     * @param seed
     *            the seed used for the generation of the curve.
     * @throws IllegalArgumentException
     *             if the specified coefficients are not in the specified field.
     */
    public EllipticCurve(ECField field, BigInteger a, BigInteger b, byte[] seed) {
        this.field = field;
        if (this.field == null) {
            throw new NullPointerException("field == null");
        }
        this.a = a;
        if (this.a == null) {
            throw new NullPointerException("a == null");
        }
        this.b = b;
        if (this.b == null) {
            throw new NullPointerException("b == null");
        }
        // make defensive copy
        if (seed == null) {
            this.seed = null;
        } else {
            this.seed = new byte[seed.length];
            System.arraycopy(seed, 0, this.seed, 0, this.seed.length);
        }
        // check parameters for ECFieldFp and ECFieldF2m.
        // Check invariant: a and b must be in the field.
        // Check conditions for custom ECField are not specified.
        if (this.field instanceof ECFieldFp) {
            BigInteger p = ((ECFieldFp) this.field).getP();
            if (this.a.signum() < 0 || this.a.compareTo(p) >= 0) {
                throw new IllegalArgumentException("the a is not in the field");
            }
            if (this.b.signum() < 0 || this.b.compareTo(p) >= 0) {
                throw new IllegalArgumentException("the b is not in the field");
            }
        } else if (this.field instanceof ECFieldF2m) {
            int fieldSizeInBits = this.field.getFieldSize();
            if (!(this.a.bitLength() <= fieldSizeInBits)) {
                throw new IllegalArgumentException("the a is not in the field");
            }
            if (!(this.b.bitLength() <= fieldSizeInBits)) {
                throw new IllegalArgumentException("the b is not in the field");
            }
        }
    }

    /**
     * Creates a new {@code EllipticCurve} with the specified field and
     * coefficients.
     *
     * @param field
     *            the finite field of this elliptic curve.
     * @param a
     *            the coefficient {@code a}.
     * @param b
     *            the coefficient {@code b}.
     * @throws IllegalArgumentException
     *             if the specified coefficients are not in the specified field.
     */
    public EllipticCurve(ECField field, BigInteger a, BigInteger b) {
        this(field, a, b, null);
    }

    /**
     * Returns the coefficient {@code a} of this elliptic curve.
     *
     * @return the coefficient {@code a} of this elliptic curve.
     */
    public BigInteger getA() {
        return a;
    }

    /**
     * Returns the coefficient {@code b} of this elliptic curve.
     *
     * @return the coefficient {@code b} of this elliptic curve.
     */
    public BigInteger getB() {
        return b;
    }

    /**
     * Returns the finite field of this elliptic curve.
     *
     * @return the finite field of this elliptic curve.
     */
    public ECField getField() {
        return field;
    }

    /**
     * Returns a copy of the seed that was used to generate this elliptic curve.
     *
     * @return a copy of the seed that was used to generate this elliptic curve,
     *         or {@code null} if none specified.
     */
    public byte[] getSeed() {
        if (seed == null) {
            return null;
        } else {
            // return copy
            byte[] ret = new byte[seed.length];
            System.arraycopy(seed, 0, ret, 0, ret.length);
            return ret;
        }
    }

    /**
     * Returns whether the specified object equals to this elliptic curve.
     *
     * @param other
     *            the object to compare.
     * @return {@code true} if the specified object is equal to this elliptic
     *         curve, otherwise {@code false}.
     */
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof EllipticCurve)) {
            return false;
        }
        EllipticCurve otherEc = (EllipticCurve) other;
        return this.field.equals(otherEc.field) && this.a.equals(otherEc.a)
                && this.b.equals(otherEc.b)
                && Arrays.equals(this.seed, otherEc.seed);
    }

    /**
     * Returns the hashcode of this elliptic curve.
     *
     * @return the hashcode of this elliptic curve.
     */
    public int hashCode() {
        // hash init is delayed
        if (hash == 0) {
            int hash0 = 11;
            hash0 = hash0 * 31 + field.hashCode();
            hash0 = hash0 * 31 + a.hashCode();
            hash0 = hash0 * 31 + b.hashCode();
            if (seed != null) {
                for (int i = 0; i < seed.length; i++) {
                    hash0 = hash0 * 31 + seed[i];
                }
            } else {
                hash0 = hash0 * 31;
            }
            hash = hash0;
        }
        return hash;
    }
}
