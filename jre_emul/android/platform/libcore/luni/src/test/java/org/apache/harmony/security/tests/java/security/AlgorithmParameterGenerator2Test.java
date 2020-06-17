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

package org.apache.harmony.security.tests.java.security;

import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;

import org.apache.harmony.security.tests.support.SpiEngUtils;

import junit.framework.TestCase;

/**
 * Tests for <code>AlgorithmParameterGenerator</code> class constructors and
 * methods.
 */
public class AlgorithmParameterGenerator2Test extends TestCase {

    private static final String AlgorithmParameterGeneratorProviderClass = "org.apache.harmony.security.tests.support.MyAlgorithmParameterGeneratorSpi";

    private static final String defaultAlg = "APG";

    public static final String srvAlgorithmParameterGenerator = "AlgorithmParameterGenerator";

    private static final String[] invalidValues = SpiEngUtils.invalidValues;

    private static final String[] validValues;

    static {
        validValues = new String[4];
        validValues[0] = defaultAlg;
        validValues[1] = defaultAlg.toLowerCase();
        validValues[2] = "apG";
        validValues[3] = "ApG";
    }

    Provider mProv;

    protected void setUp() throws Exception {
        super.setUp();
        mProv = (new SpiEngUtils()).new MyProvider("MyAPGProvider", "Testing provider",
                srvAlgorithmParameterGenerator.concat(".").concat(defaultAlg),
                AlgorithmParameterGeneratorProviderClass);
        Security.insertProviderAt(mProv, 1);
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        Security.removeProvider(mProv.getName());
    }

    private void checkResult(AlgorithmParameterGenerator algParGen)
            throws InvalidAlgorithmParameterException {
        AlgorithmParameters param = algParGen.generateParameters();
        assertNull("Not null parameters", param);

        AlgorithmParameterSpec pp = null;
        algParGen.init(pp, new SecureRandom());
        algParGen.init(pp);
        try {
            algParGen.init(pp, null);
            fail("IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
        }
        pp = new tmpAlgorithmParameterSpec("Proba");
        algParGen.init(pp, new SecureRandom());
        algParGen.init(pp);

        algParGen.init(0, null);
        algParGen.init(0, new SecureRandom());

        try {
            algParGen.init(-10, null);
            fail("IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
        }
        try {
            algParGen.init(-10, new SecureRandom());
            fail("IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
        }
    }
    /**
     * Test for <code>getInstance(String algorithm)</code> method
     * Assertions:
     * throws NullPointerException must be thrown is null
     * throws NoSuchAlgorithmException must be thrown if algorithm is not available
     * returns AlgorithmParameterGenerator object
     */
    public void testGetInstance01() throws NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {
        try {
            AlgorithmParameterGenerator.getInstance(null);
            fail("NullPointerException or NoSuchAlgorithmException should be thrown");
        } catch (NullPointerException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                AlgorithmParameterGenerator.getInstance(invalidValues[i]);
                fail("NoSuchAlgorithmException must be thrown (algorithm: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (NoSuchAlgorithmException e) {
            }
        }
        AlgorithmParameterGenerator apG;
        for (int i = 0; i < validValues.length; i++) {
            apG = AlgorithmParameterGenerator.getInstance(validValues[i]);
            assertEquals("Incorrect algorithm", apG.getAlgorithm(),
                    validValues[i]);
            assertEquals("Incorrect provider", apG.getProvider(), mProv);
            checkResult(apG);
        }
    }

    /**
     * Test for <code>getInstance(String algorithm, String provider)</code>
     * method
     * Assertions:
     * throws NullPointerException must be thrown is null
     * throws NoSuchAlgorithmException must be thrown if algorithm is not available
     * throws IllegalArgumentException when provider is null;
     * throws NoSuchProviderException when provider is available;
     * returns AlgorithmParameterGenerator object
     */
    public void testGetInstance02() throws NoSuchAlgorithmException,
            NoSuchProviderException, IllegalArgumentException,
            InvalidAlgorithmParameterException {
        try {
            AlgorithmParameterGenerator.getInstance(null, mProv.getName());
            fail("NullPointerException or NoSuchAlgorithmException should be thrown");
        } catch (NullPointerException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                AlgorithmParameterGenerator.getInstance(invalidValues[i], mProv
                        .getName());
                fail("NoSuchAlgorithmException must be thrown (algorithm: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (NoSuchAlgorithmException e) {
            }
        }
        String prov = null;
        for (int i = 0; i < validValues.length; i++) {
            try {
                AlgorithmParameterGenerator.getInstance(validValues[i], prov);
                fail("IllegalArgumentException must be thrown when provider is null (algorithm: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (IllegalArgumentException e) {
            }
        }
        for (int i = 0; i < validValues.length; i++) {
            for (int j = 1; j < invalidValues.length; j++) {
                try {
                    AlgorithmParameterGenerator.getInstance(validValues[i],
                            invalidValues[j]);
                    fail("NoSuchProviderException must be thrown (algorithm: "
                            .concat(invalidValues[i]).concat(" provider: ")
                            .concat(invalidValues[j]).concat(")"));
                } catch (NoSuchProviderException e) {
                }
            }
        }
        AlgorithmParameterGenerator apG;
        for (int i = 0; i < validValues.length; i++) {
            apG = AlgorithmParameterGenerator.getInstance(validValues[i], mProv
                    .getName());
            assertEquals("Incorrect algorithm", apG.getAlgorithm(),
                    validValues[i]);
            assertEquals("Incorrect provider", apG.getProvider().getName(),
                    mProv.getName());
            checkResult(apG);
        }
    }

    /**
     * Test for <code>getInstance(String algorithm, Provider provider)</code>
     * method
     * Assertions:
     * throws NullPointerException must be thrown is null
     * throws NoSuchAlgorithmException must be thrown if algorithm is not available
     * throws IllegalArgumentException when provider is null;
     * returns AlgorithmParameterGenerator object
     */
    public void testGetInstance03() throws NoSuchAlgorithmException,
            IllegalArgumentException,
            InvalidAlgorithmParameterException {
        try {
            AlgorithmParameterGenerator.getInstance(null, mProv);
            fail("NullPointerException or NoSuchAlgorithmException should be thrown");
        } catch (NullPointerException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                AlgorithmParameterGenerator.getInstance(invalidValues[i], mProv);
                fail("NoSuchAlgorithmException must be thrown (algorithm: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (NoSuchAlgorithmException e) {
            }
        }
        Provider prov = null;
        for (int i = 0; i < validValues.length; i++) {
            try {
                AlgorithmParameterGenerator.getInstance(validValues[i], prov);
                fail("IllegalArgumentException must be thrown when provider is null (algorithm: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (IllegalArgumentException e) {
            }
        }
        AlgorithmParameterGenerator apG;
        for (int i = 0; i < validValues.length; i++) {
            apG = AlgorithmParameterGenerator.getInstance(validValues[i], mProv);
            assertEquals("Incorrect algorithm", apG.getAlgorithm(),
                    validValues[i]);
            assertEquals("Incorrect provider", apG.getProvider(), mProv);
            checkResult(apG);
        }
    }
    /**
     * Additional class for init(...) methods verification
     */
    class tmpAlgorithmParameterSpec implements AlgorithmParameterSpec {
        private final String type;
        public tmpAlgorithmParameterSpec(String type) {
            this.type = type;
        }
        public String getType() {
            return type;
        }
    }
}
