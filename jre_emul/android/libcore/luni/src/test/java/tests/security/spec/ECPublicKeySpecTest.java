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
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;

public class ECPublicKeySpecTest extends TestCase {
    ECPoint w;

    ECParameterSpec params;

    ECPublicKeySpec ecpks;

    protected void setUp() throws Exception {
        super.setUp();
        ECPoint ecpoint = new ECPoint(BigInteger.valueOf(1), BigInteger
                .valueOf(1));
        EllipticCurve curve = new EllipticCurve(new ECFieldF2m(2), BigInteger
                .valueOf(1), BigInteger.valueOf(1));

        w = new ECPoint(BigInteger.valueOf(1), BigInteger.valueOf(1));
        params = new ECParameterSpec(curve, ecpoint, BigInteger.valueOf(1), 1);
        ecpks = new ECPublicKeySpec(w, params);
    }

    protected void tearDown() throws Exception {
        w = null;
        params = null;
        ecpks = null;
        super.tearDown();
    }

    /**
     * test for constructor ECPublicKeySpec(ECPoint, ECParameterSpec)
     * test covers following usecases:
     * case 1: creating object with valid parameters
     * case 2: catch NullPointerException - if w is null.
     * case 3: catch NullPointerException - if params is null.
     */
    public final void test_constructorLjava_security_spec_ECPointLjava_security_spec_ECParameterSpec() {

        // case 1: creating object with valid parameters
        assertEquals("wrong params value", params, ecpks.getParams());
        assertEquals("wrong w value", w, ecpks.getW());

        // case 2: catch NullPointerException - if w is null.
        try {
            new ECPublicKeySpec(null, params);
            fail("NullPointerException has not been thrown");
        } catch (NullPointerException e) {
            // expected
        }

        // case 3: catch NullPointerException - if params is null.
        try {
            new ECPublicKeySpec(w, null);
            fail("NullPointerException has not been thrown");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * test for getW() method
     */
    public final void testGetW() {
        assertEquals("wrong w value", w, ecpks.getW());
    }

    /**
     * test for getParams() meyhod
     */
    public final void testGetParams() {
        assertEquals("wrong params value", params, ecpks.getParams());
    }
}
