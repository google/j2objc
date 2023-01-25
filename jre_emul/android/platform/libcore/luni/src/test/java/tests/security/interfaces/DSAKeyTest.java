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

import java.security.KeyPairGenerator;
import java.security.interfaces.DSAKey;
import java.security.interfaces.DSAParams;
import java.security.spec.DSAParameterSpec;

public class DSAKeyTest extends TestCase {

    /**
     * java.security.interfaces.DSAKey
     * #getParams()
     * test covers following use cases
     *   Case 1: check private key
     *   Case 2: check public key
     */
    public void test_getParams() throws Exception {
        DSAParams param = new DSAParameterSpec(Util.P, Util.Q, Util.G);

        KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");
        gen.initialize((DSAParameterSpec) param);
        DSAKey key = null;

        // Case 1: check private key
        key = (DSAKey) gen.generateKeyPair().getPrivate();
        assertDSAParamsEquals(param, key.getParams());

        // Case 2: check public key
        key = (DSAKey) gen.generateKeyPair().getPublic();
        assertDSAParamsEquals(param, key.getParams());
    }

    private void assertDSAParamsEquals(DSAParams expected, DSAParams actual) {
        assertEquals("P differ", expected.getP(), actual.getP());
        assertEquals("Q differ", expected.getQ(), actual.getQ());
        assertEquals("G differ", expected.getG(), actual.getG());
    }
}
