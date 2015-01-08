/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tests.security.interfaces;

import junit.framework.TestCase;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

public class RSAKeyTest extends TestCase {

    /**
     * java.security.interfaces.RSAKey
     * #getModulus()
     * test covers following use cases
     *   Case 1: check private key
     *   Case 2: check public key
     */
    public void test_getModulus() throws Exception {
        KeyFactory gen = KeyFactory.getInstance("RSA");
        final BigInteger n = BigInteger.valueOf(3233);
        final BigInteger d = BigInteger.valueOf(2753);
        final BigInteger e = BigInteger.valueOf(17);
        RSAKey key = null;

        // Case 1: check private key
        key = (RSAKey) gen.generatePrivate(new RSAPrivateKeySpec(n, d));
        assertEquals("invalid modulus", n, key.getModulus());

        // Case 2: check public key
        key = (RSAKey) gen.generatePublic(new RSAPublicKeySpec(n, e));
        assertEquals("invalid modulus", n, key.getModulus());
    }
}
