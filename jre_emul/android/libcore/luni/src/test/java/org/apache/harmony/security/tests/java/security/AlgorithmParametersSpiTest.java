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

import java.security.AlgorithmParametersSpi;
import java.security.spec.AlgorithmParameterSpec;

import junit.framework.TestCase;

/**
 * Tests for <code>AlgorithmParametersSpi</code> class constructors
 * and methods.
 *
 */
public class AlgorithmParametersSpiTest extends TestCase {


    /**
     * Test for <code>AlgorithmParametersSpi</code> constructor
     * Assertion: constructs AlgorithmParametersSpi
     */
    public void testAlgorithmParametersSpi() {
        byte[] bt = new byte[10];
        MyAlgorithmParametersSpi algParSpi = new MyAlgorithmParametersSpi();
        assertTrue(algParSpi instanceof AlgorithmParametersSpi);
        assertNotNull(algParSpi);

        algParSpi.engineInit(new MyAlgorithmParameterSpec());
        algParSpi.engineInit(bt);
        algParSpi.engineInit(bt, "Format");
        algParSpi.engineToString();
        algParSpi.engineGetEncoded();
        algParSpi.engineGetEncoded("Format");
        algParSpi.engineGetParameterSpec(java.lang.Class.class);
    }

    public class MyAlgorithmParametersSpi extends AlgorithmParametersSpi {
        protected void engineInit(AlgorithmParameterSpec paramSpec) {
        }
        protected void engineInit(byte[] params){
        }
        protected void engineInit(byte[] params, String format){
        }
        protected AlgorithmParameterSpec engineGetParameterSpec(Class paramSpec){
            return null;
        }
        protected byte[] engineGetEncoded(){
            return null;
        }
        protected byte[] engineGetEncoded(String format){
            return null;
        }
        protected String engineToString() {
            return null;
        }
    }

    class MyAlgorithmParameterSpec implements AlgorithmParameterSpec {}
}
