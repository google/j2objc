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
 * The parameters specifying a <i>characteristic 2 finite field</i> of an
 * elliptic curve.
 */
public class ECFieldF2m implements ECField {
    // Mid terms array length for trinomial basis
    private static final int TPB_MID_LEN = 1;
    // Mid terms array length for pentanomial basis
    private static final int PPB_MID_LEN = 3;
    // All terms number for trinomial basis
    private static final int TPB_LEN = TPB_MID_LEN + 2;
    // All terms number for pentanomial basis
    private static final int PPB_LEN = PPB_MID_LEN + 2;
    // m value
    private final int m;
    // Reduction polynomial
    private final BigInteger rp;
    // Mid term(s) of reduction polynomial
    private final int[] ks;

    /**
     * Creates a new {@code ECFieldF2m} with {@code 2^m} elements with a normal
     * basis.
     *
     * @param m
     *            the exponent {@code m} for the number of elements.
     * @throws IllegalArgumentException
     *             if {@code m <= zero}.
     */
    public ECFieldF2m(int m) {
        this.m = m;
        if (this.m <= 0) {
            throw new IllegalArgumentException("m <= 0");
        }
        this.rp = null;
        this.ks = null;
    }

    /**
     * Creates a new {@code ECFieldF2m} with {@code 2^m} elements with a polynomial
     * basis and the reduction polynomial based on {@code rp}.
     * <p>
     * The reduction polynomial must be either <i>trinomial</i> or
     * <i>pentanomial</i>.
     *
     * @param m
     *            the exponent {@code m} for the number of elements.
     * @param rp
     *            the base of the reduction polynomial with the n-th bit
     *            corresponding to the n-th coefficient of the reduction
     *            polynomial.
     * @throws IllegalArgumentException
     *             if {@code m <= zero} or the {@code rp} is invalid.
     */
    public ECFieldF2m(int m, BigInteger rp) {
        this.m = m;
        if (this.m <= 0) {
            throw new IllegalArgumentException("m <= 0");
        }
        this.rp = rp;
        if (this.rp == null) {
            throw new NullPointerException("rp == null");
        }
        // the leftmost bit must be (m+1)-th one,
        // set bits count must be 3 or 5,
        // bits 0 and m must be set
        int rp_bc = this.rp.bitCount();
        if ((this.rp.bitLength() != (m+1)) ||
            (rp_bc != TPB_LEN && rp_bc != PPB_LEN) ||
            (!this.rp.testBit(0) || !this.rp.testBit(m)) ) {
            throw new IllegalArgumentException("rp is invalid");
        }

        // setup ks using rp:
        // allocate for mid terms only
        ks = new int[rp_bc-2];
        // find midterm orders and set ks accordingly
        BigInteger rpTmp = rp.clearBit(0);
        for (int i=ks.length-1; i>=0; i-- ) {
            ks[i] = rpTmp.getLowestSetBit();
            rpTmp = rpTmp.clearBit(ks[i]);
        }
    }

    /**
     * Creates a new {@code ECFieldF2m} with {@code 2^m} elements with
     * a polynomial basis and the reduction polynomial based on {@code ks}.
     * <p>
     * The reduction polynomial must be either <i>trinomial</i> or
     * <i>pentanomial</i>.
     *
     * @param m
     *            the exponent {@code m} for the number of elements.
     * @param ks
     *            the base of the reduction polynomial with coefficients
     *            given in descending order.
     * @throws IllegalArgumentException
     *             if {@code m <= zero} or the reduction polynomial is not
     *             valid.
     */
    public ECFieldF2m(int m, int[] ks) {
        this.m = m;
        if (this.m <= 0) {
            throw new IllegalArgumentException("m <= 0");
        }
        // Defensively copies array parameter
        // to prevent subsequent modification.
        // NPE as specified if ks is null
        this.ks = new int[ks.length];
        System.arraycopy(ks, 0, this.ks, 0, this.ks.length);

        // no need to check for null already
        if (this.ks.length != TPB_MID_LEN && this.ks.length != PPB_MID_LEN) {
            // must be either trinomial or pentanomial basis
            throw new IllegalArgumentException("the length of ks is invalid");
        }
        // trinomial basis:
        // check that m > k >= 1, where k is ks[0]
        // pentanomial basis:
        // check that m > k3 > k2 > k1 >= 1
        // and kx in descending order, where
        // k3 is ks[0], k2 is ks[1], k1 is ks[2]
        boolean checkFailed = false;
        int prev = this.m;
        for (int i=0; i<this.ks.length; i++) {
            if (this.ks[i] < prev) {
                prev = this.ks[i];
                continue;
            }
            checkFailed = true;
            break;
        }
        if (checkFailed || prev < 1) {
            throw new IllegalArgumentException("ks is invalid");
        }

        // Setup rp using ks:
        // bits 0 and m always set
        BigInteger rpTmp = BigInteger.ONE.setBit(this.m);
        // set remaining bits according to ks
        for (int i=0; i<this.ks.length; i++) {
            rpTmp = rpTmp.setBit(this.ks[i]);
        }
        rp = rpTmp;
    }

    /**
     * Returns whether the specified object equals to this finite field.
     *
     * @param obj
     *            the object to compare to this finite field.
     * @return {@code true} if the specified object is equal to this finite field,
     *         otherwise {@code false}.
     */
    public boolean equals(Object obj) {
        // object equals to itself
        if (this == obj) {
            return true;
        }
        if (obj instanceof ECFieldF2m) {
            ECFieldF2m o = (ECFieldF2m)obj;
            // check m
            if (this.m == o.m) {
                // check rp
                if (this.rp == null) {
                    if (o.rp == null) {
                        // fields both with normal basis
                        return true;
                    }
                } else {
                    // at least this field with polynomial basis
                    // check that rp match
                    // return this.rp.equals(o.rp);
                    return Arrays.equals(this.ks, o.ks);
                }
            }
        }
        return false;
    }

    /**
     * Returns the size of this finite field (in bits).
     *
     * @return the size of this finite field (in bits).
     */
    public int getFieldSize() {
        return m;
    }

    /**
     * Returns the exponent {@code m} for this finite field, with {@code 2^m} as
     * the number of elements.
     *
     * @return the exponent {@code m} for this finite field
     */
    public int getM() {
        return m;
    }

    /**
     * Returns a copy of the integer array containing the order of the middle
     * term(s) of the reduction polynomial for a polynomial basis.
     *
     * @return a copy of the integer array containing the order of the middle
     *         term(s) of the reduction polynomial for a polynomial basis or
     *         {@code null} for a normal basis.
     */
    public int[] getMidTermsOfReductionPolynomial() {
        // Defensively copies private array
        // to prevent subsequent modification
        // was: return ks == null ? null : (int[])ks.clone();
        if (ks == null) {
            return null;
        } else {
            int[] ret = new int[ks.length];
            System.arraycopy(ks, 0, ret, 0, ret.length);
            return ret;
        }
    }

    /**
     * Returns the base of the reduction polynomial with the n-th bit
     * corresponding to the n-th coefficient of the reduction polynomial for a
     * polynomial basis.
     *
     * @return the base of the reduction polynomial with the n-th bit
     *         corresponding to the n-th coefficient of the reduction polynomial
     *         for a polynomial basis or {@code null} for a normal basis.
     */
    public BigInteger getReductionPolynomial() {
        return rp;
    }

    /**
     * Returns the hashcode value for this finite field.
     *
     * @return the hashcode value for this finite field.
     */
    public int hashCode() {
        return rp == null ? m : m + rp.hashCode();
    }
}
