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

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;

import org.apache.harmony.security.tests.support.MyKeyPairGenerator1;
import org.apache.harmony.security.tests.support.MyKeyPairGenerator2;
import org.apache.harmony.security.tests.support.SpiEngUtils;

import junit.framework.TestCase;

/**
 * Tests for <code>KeyPairGenerator</code> class constructors and methods.
 *
 */
public class KeyPairGenerator2Test extends TestCase {
    private String KeyPairGeneratorProviderClass = "";

    private static final String KeyPairGeneratorProviderClass1 = "org.apache.harmony.security.tests.support.MyKeyPairGenerator1";
    private static final String KeyPairGeneratorProviderClass2 = "org.apache.harmony.security.tests.support.MyKeyPairGenerator2";
    private static final String KeyPairGeneratorProviderClass3 = "org.apache.harmony.security.tests.support.MyKeyPairGenerator3";
    private static final String KeyPairGeneratorProviderClass4 = "org.apache.harmony.security.tests.support.MyKeyPairGeneratorSpi";

    private static final String defaultAlg = "KPGen";

    private static final String[] invalidValues = SpiEngUtils.invalidValues;

    private static final String[] validValues;

    String post;

    static {
        validValues = new String[4];
        validValues[0] = defaultAlg;
        validValues[1] = defaultAlg.toLowerCase();
        validValues[2] = "kpGEN";
        validValues[3] = "kPGEn";
    }

    Provider mProv;
    String resAlg;

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        Security.removeProvider(mProv.getName());
    }

    protected void setProv() {
        mProv = (new SpiEngUtils()).new MyProvider("MyKPGenProvider".concat(post),
                "Testing provider", KeyPairGenerator1Test.srvKeyPairGenerator.concat(".")
                        .concat(defaultAlg.concat(post)),
                KeyPairGeneratorProviderClass);
        Security.insertProviderAt(mProv, 1);
    }

    private void checkResult(KeyPairGenerator keyPairGen, int mode)
            throws InvalidAlgorithmParameterException {
        AlgorithmParameterSpec pp = null;
        switch (mode) {
        case 1:
            try {
                keyPairGen.initialize(pp, new SecureRandom());
                fail("InvalidAlgorithmParameterException must be thrown");
            } catch (InvalidAlgorithmParameterException e) {
            }
            keyPairGen.initialize(1000, new SecureRandom());
            try {
                keyPairGen.initialize(-1024, new SecureRandom());
                fail("InvalidParameterException must be thrown");
            } catch (InvalidParameterException e) {
                assertEquals("Incorrect exception", e.getMessage(),
                        "Incorrect keysize parameter");
            }
            try {
                keyPairGen.initialize(100, null);
                fail("InvalidParameterException must be thrown");
            } catch (InvalidParameterException e) {
                assertEquals("Incorrect exception", e.getMessage(),
                        "Incorrect random");
            }
            keyPairGen.generateKeyPair();
            keyPairGen.genKeyPair();
            break;
        case 2:
            try {
                keyPairGen.initialize(pp, new SecureRandom());
            } catch (UnsupportedOperationException e) {
                // js2e does not throw this exception
            }
            keyPairGen.initialize(1000, new SecureRandom());
            try {
                keyPairGen.initialize(63, new SecureRandom());
                fail("InvalidParameterException must be thrown");
            } catch (InvalidParameterException e) {
            }
            keyPairGen.initialize(100, null);
            assertNull("Not null KeyPair", keyPairGen.generateKeyPair());
            assertNull("Not null KeyPair", keyPairGen.genKeyPair());
            break;
        case 3:
            keyPairGen.initialize(pp, new SecureRandom());
            keyPairGen.initialize(pp);
            keyPairGen.initialize(1000, new SecureRandom());
            keyPairGen.initialize(100);

            assertNotNull("Null KeyPair", keyPairGen.generateKeyPair());
            assertNotNull("Null KeyPair", keyPairGen.genKeyPair());
            break;
        case 4:
            try {
                keyPairGen.initialize(pp, null);
                fail("UnsupportedOperationException must be thrown");
            } catch (UnsupportedOperationException e) {
            }
            keyPairGen.initialize(pp, new SecureRandom());
            keyPairGen.initialize(101, new SecureRandom());
            keyPairGen.initialize(10000);
            try {
                keyPairGen.initialize(101, null);
                fail("IllegalArgumentException must be thrown for null random");
            } catch (IllegalArgumentException e) {
            }
            try {
                keyPairGen.initialize(99, new SecureRandom());
                fail("InvalidParameterException must be thrown for invalid key");
            } catch (InvalidParameterException e) {
            }
            try {
                keyPairGen.initialize(99);
                fail("InvalidParameterException must be thrown for invalid key");
            } catch (InvalidParameterException e) {
            }
            try {
                keyPairGen.initialize(199, null);
                fail("IllegalArgumentException must be thrown for null random");
            } catch (IllegalArgumentException e) {
            }
            assertNull("Not null KeyPair", keyPairGen.generateKeyPair());
            assertNull("Not null KeyPair", keyPairGen.genKeyPair());
            break;
        }

    }

    /**
     * Test for <code>getInstance(String algorithm)</code> method Assertions:
     * throws NullPointerException when algorithm is null throws
     * NoSuchAlgorithmException when algorithm is incorrect; returns
     * KeyPairGenerator object
     *
     */
    private void GetInstance01(int mode) throws NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {
        try {
            KeyPairGenerator.getInstance(null);
            fail("NullPointerException or KeyStoreException must be thrown");
        } catch (NoSuchAlgorithmException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                KeyPairGenerator.getInstance(invalidValues[i]);
                fail("NoSuchAlgorithmException must be thrown (algorithm: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (NoSuchAlgorithmException e) {
            }
        }
        KeyPairGenerator kpG;
        for (int i = 0; i < validValues.length; i++) {
            String alg = validValues[i].concat(post);
            kpG = KeyPairGenerator.getInstance(alg);
            assertEquals("Incorrect algorithm", kpG.getAlgorithm()
                    .toUpperCase(), (mode <= 2 ? resAlg : alg).toUpperCase());
            assertEquals("Incorrect provider", kpG.getProvider(), mProv);
            checkResult(kpG, mode);
        }
    }

    /**
     * Test for <code>getInstance(String algorithm, String provider)</code>
     * method
     * Assertions:
     * throws NullPointerException  when algorithm is null
     * throws NoSuchAlgorithmException when algorithm is incorrect;
     * throws IllegalArgumentException when provider is null;
     * throws NoSuchProviderException when provider is available;
     * returns
     * KeyPairGenerator object
     */
    public void GetInstance02(int mode) throws NoSuchAlgorithmException,
            NoSuchProviderException, IllegalArgumentException,
            InvalidAlgorithmParameterException {
        try {
            KeyPairGenerator.getInstance(null, mProv.getName());
            fail("NullPointerException or KeyStoreException must be thrown");
        } catch (NoSuchAlgorithmException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                KeyPairGenerator.getInstance(invalidValues[i], mProv.getName());
                fail("NoSuchAlgorithmException must be thrown (algorithm: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (NoSuchAlgorithmException e) {
            }
        }
        String prov = null;
        for (int i = 0; i < validValues.length; i++) {
            String alg = validValues[i].concat(post);
            try {
                KeyPairGenerator.getInstance(alg, prov);
                fail("IllegalArgumentException must be thrown when provider is null (algorithm: "
                        .concat(alg).concat(")"));
            } catch (IllegalArgumentException e) {
            }
        }
        for (int i = 0; i < validValues.length; i++) {
            String alg = validValues[i].concat(post);
            for (int j = 1; j < invalidValues.length; j++) {
                try {
                    KeyPairGenerator.getInstance(alg, invalidValues[j]);
                    fail("NoSuchProviderException must be thrown (algorithm: "
                            .concat(alg).concat(" provider: ").concat(
                                    invalidValues[j]).concat(")"));
                } catch (NoSuchProviderException e) {
                }
            }
        }
        KeyPairGenerator kpG;
        for (int i = 0; i < validValues.length; i++) {
            String alg = validValues[i].concat(post);
            kpG = KeyPairGenerator.getInstance(alg, mProv.getName());
            assertEquals("Incorrect algorithm", kpG.getAlgorithm()
                    .toUpperCase(), (mode <= 2 ? resAlg : alg).toUpperCase());
            assertEquals("Incorrect provider", kpG.getProvider().getName(),
                    mProv.getName());
            checkResult(kpG, mode);
        }
    }

    /**
     * Test for <code>getInstance(String algorithm, Provider provider)</code>
     * method
     * Assertions:
     * throws NullPointerException  when algorithm is null
     * throws NoSuchAlgorithmException when algorithm is incorrect;
     * throws IllegalArgumentException when provider is null;
     * returns KeyPairGenerator object
     */
    private void GetInstance03(int mode) throws NoSuchAlgorithmException,
            IllegalArgumentException, InvalidAlgorithmParameterException {
        try {
            KeyPairGenerator.getInstance(null, mProv);
            fail("NullPointerException or KeyStoreException must be thrown");
        } catch (NoSuchAlgorithmException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                KeyPairGenerator.getInstance(invalidValues[i], mProv);
                fail("NoSuchAlgorithmException must be thrown (algorithm: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (NoSuchAlgorithmException e) {
            }
        }
        Provider prov = null;
        for (int i = 0; i < validValues.length; i++) {
            String alg = validValues[i].concat(post);
            try {
                KeyPairGenerator.getInstance(alg, prov);
                fail("IllegalArgumentException must be thrown when provider is null (algorithm: "
                        .concat(alg).concat(")"));
            } catch (IllegalArgumentException e) {
            }
        }
        KeyPairGenerator kpG;
        for (int i = 0; i < validValues.length; i++) {
            String alg = validValues[i].concat(post);
            kpG = KeyPairGenerator.getInstance(alg, mProv);
            assertEquals("Incorrect algorithm", kpG.getAlgorithm()
                    .toUpperCase(), (mode <= 2 ? resAlg : alg).toUpperCase());
            assertEquals("Incorrect provider", kpG.getProvider(), mProv);
            checkResult(kpG, mode);
        }
    }

    public void testGetInstance01() throws NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {
        KeyPairGeneratorProviderClass = KeyPairGeneratorProviderClass1;
        resAlg = MyKeyPairGenerator1.getResAlgorithm();
        post = "_1";
        setProv();
        GetInstance01(1);
    }

    public void testGetInstance02() throws NoSuchAlgorithmException,
            NoSuchProviderException, IllegalArgumentException,
            InvalidAlgorithmParameterException {
        KeyPairGeneratorProviderClass = KeyPairGeneratorProviderClass1;
        resAlg = MyKeyPairGenerator1.getResAlgorithm();
        post = "_1";
        setProv();
        GetInstance02(1);
    }

    public void testGetInstance03() throws NoSuchAlgorithmException,
            IllegalArgumentException, InvalidAlgorithmParameterException {
        KeyPairGeneratorProviderClass = KeyPairGeneratorProviderClass1;
        resAlg = MyKeyPairGenerator1.getResAlgorithm();
        post = "_1";
        setProv();
        GetInstance03(1);
    }

    public void testGetInstance04() throws NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {
        KeyPairGeneratorProviderClass = KeyPairGeneratorProviderClass2;
        resAlg = MyKeyPairGenerator2.getResAlgorithm();
        post = "_2";
        setProv();
        GetInstance01(2);
    }

    public void testGetInstance05() throws NoSuchAlgorithmException,
            NoSuchProviderException, IllegalArgumentException,
            InvalidAlgorithmParameterException {
        KeyPairGeneratorProviderClass = KeyPairGeneratorProviderClass2;
        resAlg = MyKeyPairGenerator2.getResAlgorithm();
        post = "_2";
        setProv();
        GetInstance02(2);
    }

    public void testGetInstance06() throws NoSuchAlgorithmException,
            IllegalArgumentException, InvalidAlgorithmParameterException {
        KeyPairGeneratorProviderClass = KeyPairGeneratorProviderClass2;
        resAlg = MyKeyPairGenerator2.getResAlgorithm();
        post = "_2";
        setProv();
        GetInstance03(2);
    }

    public void testGetInstance07() throws NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {
        KeyPairGeneratorProviderClass = KeyPairGeneratorProviderClass3;
        resAlg = "";
        post = "_3";
        setProv();
        GetInstance01(3);
    }

    public void testGetInstance08() throws NoSuchAlgorithmException,
            NoSuchProviderException, IllegalArgumentException,
            InvalidAlgorithmParameterException {
        KeyPairGeneratorProviderClass = KeyPairGeneratorProviderClass3;
        resAlg = "";
        post = "_3";
        setProv();
        GetInstance02(3);
    }

    public void testGetInstance09() throws NoSuchAlgorithmException,
            IllegalArgumentException, InvalidAlgorithmParameterException {
        KeyPairGeneratorProviderClass = KeyPairGeneratorProviderClass3;
        resAlg = "";
        post = "_3";
        setProv();
        GetInstance03(3);
    }

    public void testGetInstance10() throws NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {
        KeyPairGeneratorProviderClass = KeyPairGeneratorProviderClass4;
        resAlg = "";
        post = "_4";
        setProv();
        GetInstance01(4);
    }

    public void testGetInstance11() throws NoSuchAlgorithmException,
            NoSuchProviderException, IllegalArgumentException,
            InvalidAlgorithmParameterException {
        KeyPairGeneratorProviderClass = KeyPairGeneratorProviderClass4;
        resAlg = "";
        post = "_4";
        setProv();
        GetInstance02(4);
    }

    public void testGetInstance12() throws NoSuchAlgorithmException,
            IllegalArgumentException, InvalidAlgorithmParameterException {
        KeyPairGeneratorProviderClass = KeyPairGeneratorProviderClass4;
        resAlg = "";
        post = "_4";
        setProv();
        GetInstance03(4);
    }
}
