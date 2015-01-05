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

/**
* @author Vladimir N. Molotkov
* @version $Revision$
*/

package tests.security.spec;

import junit.framework.TestCase;

import java.math.BigInteger;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;

/**
 * Tests for <code>RSAKeyGenParameterSpec</code> class fields and methods.
 *
 */
public class RSAKeyGenParameterSpecTest extends TestCase {

    /**
     * Test for <code>RSAKeyGenParameterSpec(int,BigInteger)</code> ctor
     * Assertion: constructs <code>RSAKeyGenParameterSpec</code>
     * object using valid parameters
     */
    public final void testRSAKeyGenParameterSpec() {
        AlgorithmParameterSpec aps =
            new RSAKeyGenParameterSpec(512, BigInteger.valueOf(0L));
        assertTrue(aps instanceof RSAKeyGenParameterSpec);
    }

    /**
     * Test for <code>getKeySize()</code> method<br>
     * Assertion: returns key size value
     */
    public final void testGetKeysize() {
        RSAKeyGenParameterSpec rkgps =
            new RSAKeyGenParameterSpec(512, BigInteger.valueOf(0L));
        assertEquals(512, rkgps.getKeysize());
    }

    /**
     * Test for <code>getPublicExponent()</code> method<br>
     * Assertion: returns public exponent value
     */
    public final void testGetPublicExponent() {
        RSAKeyGenParameterSpec rkgps =
            new RSAKeyGenParameterSpec(512, BigInteger.valueOf(0L));
        assertEquals(0, rkgps.getPublicExponent().intValue());
    }

    /**
     * Test for <code>F0</code> field<br>
     * Assertion: the public exponent value F0 = 3
     */
    public final void testF0Value() {
        assertEquals(3, RSAKeyGenParameterSpec.F0.intValue());
    }

    /**
     * Test for <code>F4</code> field<br>
     * Assertion: the public exponent value F0 = 65537
     */
    public final void testF4Value() {
        assertEquals(65537, RSAKeyGenParameterSpec.F4.intValue());
    }

}
