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

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.harmony.security.tests.support.cert.MyCertificate;

import junit.framework.TestCase;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for <code>KeyStore.PrivateKeyEntry</code>  class constructor and methods
 *
 */
public class KSPrivateKeyEntryTest extends TestCase {

    private PrivateKey testPrivateKey;
    private Certificate [] testChain;

    private void createParams(boolean diffCerts, boolean diffKeys) {
        byte[] encoded = {(byte)0, (byte)1, (byte)2, (byte)3};
        testChain = new Certificate[5];
        for (int i = 0; i < testChain.length; i++) {
            String s = (diffCerts ? Integer.toString(i) : "NEW");
            testChain[i] = new MyCertificate("MY_TEST_CERTIFICATE_"
                    .concat(s), encoded);
        }
        testPrivateKey = (diffKeys ? (PrivateKey)new tmpPrivateKey() :
            (PrivateKey)new tmpPrivateKey(testChain[0].getPublicKey().getAlgorithm()));
    }

    /**
     * Test for <code>PrivateKeyEntry(PrivateKey privateKey, Certificate[] chain)</code>
     * constructor
     * Assertion: throws NullPointerException when privateKey is null
     */
    public void testPrivateKeyEntry01() {
        Certificate[] certs = new MyCertificate[1];//new Certificate[1];
        PrivateKey pk = null;
        try {
            new KeyStore.PrivateKeyEntry(pk, certs);
            fail("NullPointerException must be thrown when privateKey is null");
        } catch (NullPointerException e) {
        }
    }

    /**
     * Test for <code>PrivateKeyEntry(PrivateKey privateKey, Certificate[] chain)</code>
     * constructor
     * Assertion: throws NullPointerException when chain is null
     * and throws IllegalArgumentException when chain length is 0
     */
    public void testPrivateKeyEntry02() {
        Certificate[] chain = null;
        PrivateKey pk = new tmpPrivateKey();
        try {
            new KeyStore.PrivateKeyEntry(pk, chain);
            fail("NullPointerException must be thrown when chain is null");
        } catch (NullPointerException e) {
        }
        try {
            chain = new Certificate[0];
            new KeyStore.PrivateKeyEntry(pk, chain);
            fail("IllegalArgumentException must be thrown when chain length is 0");
        } catch (IllegalArgumentException e) {
        }
    }
    /**
     * Test for <code>PrivateKeyEntry(PrivateKey privateKey, Certificate[] chain)</code>
     * constructor
     * Assertion: throws IllegalArgumentException when chain contains certificates
     * of different types
     */
    public void testPrivateKeyEntry03() {
        createParams(true, false);
        try {
            new KeyStore.PrivateKeyEntry(testPrivateKey, testChain);
            fail("IllegalArgumentException must be thrown when chain contains certificates of different types");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Test for <code>PrivateKeyEntry(PrivateKey privateKey, Certificate[] chain)</code>
     * constructor
     * Assertion: throws IllegalArgumentException when algorithm of privateKey
     * does not match the algorithm of PublicKey in the end certificate (with 0 index)
     */
    public void testPrivateKeyEntry04() {
        createParams(false, true);
        try {
            new KeyStore.PrivateKeyEntry(testPrivateKey, testChain);
            fail("IllegalArgumentException must be thrown when key algorithms do not match");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Test for
     * <code>PrivateKeyEntry(
     *         PrivateKey privateKey, Certificate[] chain, Set<Attribute> attributes)</code>
     * constructor
     * Assertion: throws NullPointerException when attributes is null
     */
    public void testPrivateKeyEntry05() {
        createParams(false, true);
        try {
            new KeyStore.PrivateKeyEntry(testPrivateKey, testChain, null /* attributes */);
            fail("NullPointerException must be thrown when attributes is null");
        } catch (NullPointerException expected) {
        }
    }

    /**
     * Test for <code>getPrivateKey()</code> method
     * Assertion: returns PrivateKey object
     */
    public void testGetPrivateKey() {
        createParams(false, false);
        KeyStore.PrivateKeyEntry ksPKE = new KeyStore.PrivateKeyEntry(
                testPrivateKey, testChain);
        assertEquals("Incorrect PrivateKey", testPrivateKey, ksPKE
                .getPrivateKey());
    }

    /**
     * Test for <code>getCertificateChain()</code> method Assertion: returns
     * array of the Certificates corresponding to chain
     */
    public void testGetCertificateChain() {
        createParams(false, false);
        KeyStore.PrivateKeyEntry ksPKE = new KeyStore.PrivateKeyEntry(
                testPrivateKey, testChain);
        Certificate[] res = ksPKE.getCertificateChain();
        assertEquals("Incorrect chain length", testChain.length, res.length);
        for (int i = 0; i < res.length; i++) {
            assertEquals("Incorrect chain element: "
                    .concat(Integer.toString(i)), testChain[i], res[i]);
        }
    }

    /**
     * Test for <code>getCertificate()</code> method
     * Assertion: returns end Certificate (with 0 index in chain)
     */
    public void testGetCertificate() {
        createParams(false, false);
        KeyStore.PrivateKeyEntry ksPKE = new KeyStore.PrivateKeyEntry(
                testPrivateKey, testChain);
        Certificate res = ksPKE.getCertificate();
        assertEquals("Incorrect end certificate (number 0)", testChain[0], res);
    }

    /**
     * Test for <code>getAttributes()</code> method
     * Assertion: returns attributes specified in the constructor, as an unmodifiable set.
     */
    public void testGetAttributes() {
        createParams(false, false);
        final String attributeName = "theAttributeName";
        KeyStore.Entry.Attribute myAttribute = new KeyStore.Entry.Attribute() {
            @Override
            public String getName() {
                return attributeName;
            }

            @Override
            public String getValue() {
                return null;
            }
        };
        Set<KeyStore.Entry.Attribute> attributeSet = new HashSet<KeyStore.Entry.Attribute>();
        attributeSet.add(myAttribute);

        KeyStore.PrivateKeyEntry ksPKE = new KeyStore.PrivateKeyEntry(
                testPrivateKey, testChain, attributeSet);
        Set<KeyStore.Entry.Attribute> returnedAttributeSet = ksPKE.getAttributes();
        assertEquals(attributeSet, returnedAttributeSet);
        // Adding an element to the original set is OK.
        attributeSet.add(myAttribute);
        // The returned set is unmodifiabled.
        try {
            returnedAttributeSet.add(myAttribute);
            fail("The returned set of attributed should be unmodifiable");
        } catch (UnsupportedOperationException expected) {
        }
    }

    /**
     * Test for <code>toString()</code> method
     * Assertion: returns non null String
     */
    public void testToString() {
        createParams(false, false);
        KeyStore.PrivateKeyEntry ksPKE = new KeyStore.PrivateKeyEntry(
                testPrivateKey, testChain);
        String res = ksPKE.toString();
        assertNotNull("toString() returns null", res);
    }

    public static Test suite() {
        return new TestSuite(KSPrivateKeyEntryTest.class);
    }

    private static class tmpPrivateKey implements PrivateKey {
        private String alg = "My algorithm";

        public String getAlgorithm() {
            return alg;
        }

        public String getFormat() {
            return "My Format";
        }

        public byte[] getEncoded() {
            return new byte[1];
        }

        public tmpPrivateKey() {
        }

        public tmpPrivateKey(String algorithm) {
            super();
            alg = algorithm;
        }
    }
}
