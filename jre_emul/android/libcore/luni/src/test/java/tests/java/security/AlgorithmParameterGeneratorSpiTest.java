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
 * @author Vera Y. Petrashkova
 * @version $Revision$
 */

package tests.java.security;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import org.apache.harmony.security.tests.support.MyAlgorithmParameterGeneratorSpi;

import junit.framework.TestCase;

/**
 * Tests for <code>AlgorithmParameterGeneratorSpi</code> class constructors
 * and methods.
 *
 */
public class AlgorithmParameterGeneratorSpiTest extends TestCase {

    /**
     * Test for <code>AlgorithmParameterGeneratorSpi</code> constructor
     * Assertion: constructs AlgorithmParameterGeneratorSpi
     */
    public void testAlgorithmParameterGeneratorSpi01()
            throws InvalidAlgorithmParameterException {
        MyAlgorithmParameterGeneratorSpi algParGen = new MyAlgorithmParameterGeneratorSpi();
        AlgorithmParameters param = algParGen.engineGenerateParameters();
        assertNull("Not null parameters", param);
        AlgorithmParameterSpec pp = null;
        algParGen.engineInit(pp, new SecureRandom());
        try {
            algParGen.engineInit(pp, null);
            fail("IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
        }
        algParGen.engineInit(0, null);
        algParGen.engineInit(0, new SecureRandom());

        try {
            algParGen.engineInit(-10, null);
            fail("IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
        }
        try {
            algParGen.engineInit(-10, new SecureRandom());
            fail("IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
        }
    }


}
