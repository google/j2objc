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

package org.apache.harmony.security.tests.java.security;

import java.security.KeyFactorySpi;
import java.security.PrivateKey;
import java.security.spec.KeySpec;
import java.security.PublicKey;
import java.security.Key;

import junit.framework.TestCase;

/**
 * Tests for <code>KeyFactorySpi</code> class constructors
 * and methods.
 *
 */
public class KeyFactorySpiTest extends TestCase {

    /**
     * Test for <code>KeyFactorySpi</code> constructor
     * Assertion: constructs KeyFactorySpi
     */
    public void testKeyFactorySpi() {
        MyKeyFactorySpi keyFSpi = new MyKeyFactorySpi();
        assertTrue(keyFSpi instanceof KeyFactorySpi);

        KeySpec ks = new MyKeySpec();
        KeySpec kss = new MyKeySpec();
        try {
            keyFSpi.engineGeneratePrivate(ks);
            keyFSpi.engineGeneratePublic(ks);
            keyFSpi.engineGetKeySpec(null, java.lang.Class.class);
            keyFSpi.engineTranslateKey(null);
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    public class MyKeyFactorySpi extends KeyFactorySpi {
        protected PrivateKey engineGeneratePrivate(KeySpec keySpec){
            return null;
        }
        protected PublicKey engineGeneratePublic(KeySpec keySpec){
            return null;
        }
        protected KeySpec engineGetKeySpec(Key key, Class keySpec){
            return null;
        }
        protected Key engineTranslateKey(Key key){
            return null;
        }
    }

    class MyKeySpec implements KeySpec {}
}
