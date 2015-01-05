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
 * The parameter specification used with Elliptic Curve Cryptography (ECC).
 */
public class ECParameterSpec implements AlgorithmParameterSpec {
    // Elliptic curve for which this is parameter
    private final EllipticCurve curve;
    // Distinguished point on the elliptic curve called generator or base point
    private final ECPoint generator;
    // Order of the generator
    private final BigInteger order;
    // Cofactor
    private final int cofactor;
    // Name of curve if available.
    private String curveName;

    /**
     * Creates a new {@code ECParameterSpec} with the specified elliptic curve,
     * the base point, the order of the generator (or base point) and the
     * co-factor.
     *
     * @param curve
     *            the elliptic curve.
     * @param generator
     *            the generator (or base point).
     * @param order
     *            the order of the generator.
     * @param cofactor
     *            the co-factor.
     * @throws IllegalArgumentException
     *             if {@code order <= zero} or {@code cofactor <= zero}.
     */
    public ECParameterSpec(EllipticCurve curve, ECPoint generator,
            BigInteger order, int cofactor) {
        this.curve = curve;
        this.generator = generator;
        this.order = order;
        this.cofactor = cofactor;
        // throw NullPointerException if curve, generator or order is null
        if (this.curve == null) {
            throw new NullPointerException("curve == null");
        }
        if (this.generator == null) {
            throw new NullPointerException("generator == null");
        }
        if (this.order == null) {
            throw new NullPointerException("order == null");
        }
        // throw IllegalArgumentException if order or cofactor is not positive
        if (!(this.order.compareTo(BigInteger.ZERO) > 0)) {
            throw new IllegalArgumentException("order <= 0");
        }
        if (!(this.cofactor > 0)) {
            throw new IllegalArgumentException("cofactor <= 0");
        }
    }

    /**
     * Returns the {@code cofactor}.
     *
     * @return the {@code cofactor}.
     */
    public int getCofactor() {
        return cofactor;
    }

    /**
     * Returns the elliptic curve.
     *
     * @return the elliptic curve.
     */
    public EllipticCurve getCurve() {
        return curve;
    }

    /**
     * Returns the generator (or base point).
     *
     * @return the generator (or base point).
     */
    public ECPoint getGenerator() {
        return generator;
    }

    /**
     * Returns the order of the generator.
     *
     * @return the order of the generator.
     */
    public BigInteger getOrder() {
        return order;
    }

    /**
     * Used to set the curve name if available.
     *
     * @hide
     */
    public void setCurveName(String curveName) {
        this.curveName = curveName;
    }

    /**
     * Returns the name of the curve if this is a named curve. Returns
     * {@code null} if this is not known to be a named curve.
     *
     * @hide
     */
    public String getCurveName() {
        return curveName;
    }
}
