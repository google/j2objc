/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.security.spec;

import junit.framework.TestCase;

import java.math.BigInteger;
import java.security.spec.ECFieldF2m;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.EllipticCurve;

public class ECPrivateKeySpecTest extends TestCase {

    BigInteger s;

    ECParameterSpec ecparams;

    ECPrivateKeySpec ecpks;

    protected void setUp() throws Exception {
        super.setUp();

        ECPoint ecpoint = new ECPoint(BigInteger.valueOf(1), BigInteger
                .valueOf(1));
        EllipticCurve curve = new EllipticCurve(new ECFieldF2m(2), BigInteger
                .valueOf(1), BigInteger.valueOf(1));

        s = BigInteger.valueOf(1);
        ecparams = new ECParameterSpec(curve, ecpoint, BigInteger.valueOf(1), 1);
        ecpks = new ECPrivateKeySpec(s, ecparams);
    }

    protected void tearDown() throws Exception {
        s = null;
        ecparams = null;
        ecpks = null;
        super.tearDown();
    }

    /**
     * test for constructor ECPrivateKeySpec(BigInteger, ECParameterSpec)
     * test covers following usecases:
     * case 1: creating object with valid parameters
     * case 2: catch NullPointerException - if s is null.
     * case 3: catch NullPointerException - if params is null.
     */
    public void test_constructorLjava_math_BigIntegerLjava_security_spec_ECParameterSpec() {

        // case 1: creating object with valid parameters
        assertEquals("wrong private value", s, ecpks.getS());
        assertEquals("wrong parameters", ecparams, ecpks.getParams());

        // case 2: catch NullPointerException - if s is null.
        try {
            new ECPrivateKeySpec(null, ecparams);
            fail("NullPointerException has not been thrown");
        } catch (NullPointerException e) {
            // expected
        }

        // case 3: catch NullPointerException - if params is null.
        try {
            new ECPrivateKeySpec(s, null);
            fail("NullPointerException has not been thrown");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * test for getS() method
     */
    public void test_GetS() {
        assertEquals("wrong private value", s, ecpks.getS());
    }

    /**
     * test for getParams() method
     */
    public void test_GetParams() {
        assertEquals("wrong parameters", ecparams, ecpks.getParams());
    }
}
