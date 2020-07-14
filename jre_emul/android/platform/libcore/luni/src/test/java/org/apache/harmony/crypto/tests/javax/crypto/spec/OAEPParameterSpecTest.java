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

import java.security.spec.MGF1ParameterSpec;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 */
public class OAEPParameterSpecTest extends TestCase {

    /**
     * OAEPParameterSpec(String mdName, String mgfName, AlgorithmParameterSpec
     * mgfSpec, PSource pSrc) method testing. Tests that NullPointerException
     * is thrown in the case of inappropriate constructor parameters and checks
     * the value of DEFAULT field.
     */
    public void testOAEPParameterSpec() {
        // using init values for OAEPParameterSpec.DEFAULT
        String mdName = "SHA-1";
        String mgfName = "MGF1";
        AlgorithmParameterSpec mgfSpec = MGF1ParameterSpec.SHA1;
        PSource pSrc = PSource.PSpecified.DEFAULT;

        try {
            new OAEPParameterSpec(null, mgfName, mgfSpec, pSrc);
            fail("NullPointerException should be thrown in the case of "
                    + "null mdName.");
        } catch (NullPointerException e) {
        }

        try {
            new OAEPParameterSpec(mdName, null, mgfSpec, pSrc);
            fail("NullPointerException should be thrown in the case of "
                    + "null mgfName.");
        } catch (NullPointerException e) {
        }

        try {
            new OAEPParameterSpec(mdName, mgfName, mgfSpec, null);
            fail("NullPointerException should be thrown in the case of "
                    + "null pSrc.");
        } catch (NullPointerException e) {
        }

        assertTrue("The message digest algorithm name of "
                + "OAEPParameterSpec.DEFAULT field should be " + mdName,
                OAEPParameterSpec.DEFAULT.getDigestAlgorithm().equals(mdName));

        assertTrue("The mask generation function algorithm name of "
                + "OAEPParameterSpec.DEFAULT field should be " + mgfName,
                OAEPParameterSpec.DEFAULT.getMGFAlgorithm().equals(mgfName));

        assertTrue("The mask generation function parameters of "
                + "OAEPParameterSpec.DEFAULT field should be the same object "
                + "as MGF1ParameterSpec.SHA1",
                OAEPParameterSpec.DEFAULT.getMGFParameters()
                                                    == MGF1ParameterSpec.SHA1);
        assertTrue("The source of the encoding input P of "
                + "OAEPParameterSpec.DEFAULT field should be the same object "
                + "PSource.PSpecified.DEFAULT",
                OAEPParameterSpec.DEFAULT.getPSource()
                                                == PSource.PSpecified.DEFAULT);
    }

    /**
     * getDigestAlgorithm() method testing.
     */
    public void testGetDigestAlgorithm() {
        String mdName = "SHA-1";
        String mgfName = "MGF1";
        AlgorithmParameterSpec mgfSpec = MGF1ParameterSpec.SHA1;
        PSource pSrc = PSource.PSpecified.DEFAULT;

        OAEPParameterSpec ps = new OAEPParameterSpec(mdName, mgfName,
                                                                mgfSpec, pSrc);
        assertTrue("The returned value does not equal to the "
                + "value specified in the constructor.",
                ps.getDigestAlgorithm().equals(mdName));
    }

    /**
     * getMGFAlgorithm() method testing.
     */
    public void testGetMGFAlgorithm() {
        String mdName = "SHA-1";
        String mgfName = "MGF1";
        AlgorithmParameterSpec mgfSpec = MGF1ParameterSpec.SHA1;
        PSource pSrc = PSource.PSpecified.DEFAULT;

        OAEPParameterSpec ps = new OAEPParameterSpec(mdName, mgfName,
                                                                mgfSpec, pSrc);
        assertTrue("The returned value does not equal to the "
                + "value specified in the constructor.",
                ps.getMGFAlgorithm().equals(mgfName));
    }

    /**
     * getMGFParameters() method testing.
     */
    public void testGetMGFParameters() {
        String mdName = "SHA-1";
        String mgfName = "MGF1";
        AlgorithmParameterSpec mgfSpec = MGF1ParameterSpec.SHA1;
        PSource pSrc = PSource.PSpecified.DEFAULT;

        OAEPParameterSpec ps = new OAEPParameterSpec(mdName, mgfName,
                                                                mgfSpec, pSrc);
        assertTrue("The returned value does not equal to the "
                + "value specified in the constructor.",
                ps.getMGFParameters() == mgfSpec);
    }

    /**
     * getPSource() method testing.
     */
    public void testGetPSource() {
        String mdName = "SHA-1";
        String mgfName = "MGF1";
        AlgorithmParameterSpec mgfSpec = MGF1ParameterSpec.SHA1;
        PSource pSrc = PSource.PSpecified.DEFAULT;

        OAEPParameterSpec ps = new OAEPParameterSpec(mdName, mgfName,
                                                                mgfSpec, pSrc);
        assertTrue("The returned value does not equal to the "
                + "value specified in the constructor.",
                ps.getPSource() == pSrc);
    }

    public static Test suite() {
        return new TestSuite(OAEPParameterSpecTest.class);
    }
}

