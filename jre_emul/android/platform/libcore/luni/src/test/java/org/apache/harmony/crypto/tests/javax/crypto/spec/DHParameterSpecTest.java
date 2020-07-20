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
* @author Alexander Y. Kleymenov
* @version $Revision$
*/

package org.apache.harmony.crypto.tests.javax.crypto.spec;

import java.lang.Integer;
import java.math.BigInteger;

import javax.crypto.spec.DHParameterSpec;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 */
public class DHParameterSpecTest extends TestCase {

    /**
     * DHParameterSpec class testing. Tests the equivalence of parameters
     * specified in the constructor with the values returned by getters.
     * The tested object is created by different constructors.
     */
    public void testDHParameterSpec() {
        BigInteger[] ps = {new BigInteger("-1000000000000"), BigInteger.ZERO,
                            BigInteger.ONE, new BigInteger("1000000000000")};
        BigInteger[] gs = {new BigInteger("-1000000000000"), BigInteger.ZERO,
                            BigInteger.ONE, new BigInteger("1000000000000")};
        int[] ls = {Integer.MIN_VALUE, 0, 1, Integer.MAX_VALUE};
        for (int i=0; i<ps.length; i++) {
            DHParameterSpec dhps = new DHParameterSpec(ps[i], gs[i]);
            assertEquals("The value returned by getP() must be "
                        + "equal to the value specified in the constructor",
                        dhps.getP(), ps[i]);
            assertEquals("The value returned by getG() must be "
                        + "equal to the value specified in the constructor",
                        dhps.getG(), gs[i]);
        }
        for (int i=0; i<ps.length; i++) {
            DHParameterSpec dhps = new DHParameterSpec(ps[i], gs[i], ls[i]);
            assertEquals("The value returned by getP() must be "
                        + "equal to the value specified in the constructor",
                        dhps.getP(), ps[i]);
            assertEquals("The value returned by getG() must be "
                        + "equal to the value specified in the constructor",
                        dhps.getG(), gs[i]);
            assertEquals("The value returned by getL() must be "
                        + "equal to the value specified in the constructor",
                        dhps.getL(), ls[i]);
        }
    }

    public static Test suite() {
        return new TestSuite(DHParameterSpecTest.class);
    }
}

