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
import java.security.cert.Certificate;
import java.util.HashSet;
import java.util.Set;

import org.apache.harmony.security.tests.support.cert.MyCertificate;


import junit.framework.TestCase;

/**
 * Tests for <code>KeyStore.TrustedCertificateEntry</code> class constructor and methods
 *
 */

public class KSTrustedCertificateEntryTest extends TestCase {

    /**
     * Test for <codfe>KeyStore.TrustedCertificateEntry(Certificate trustCert)</code>
     * constructor
     * Assertion: throws NullPointerException when trustCert is null
     */
    public void testTrustedCertificateEntry() {
        Certificate cert = null;
        try {
            new KeyStore.TrustedCertificateEntry(cert);
            fail("NullPointerException must be thrown when trustCert is null");
        } catch (NullPointerException e) {
        }

        cert = new MyCertificate("TEST", new byte[10]);
        try {
            KeyStore.TrustedCertificateEntry ksTCE = new KeyStore.TrustedCertificateEntry(cert);
            assertNotNull(ksTCE);
            assertTrue(ksTCE instanceof KeyStore.TrustedCertificateEntry);
        } catch (Exception e) {
            fail("Unexpected exception was thrown when trustCert is not null");
        }
    }

    /**
     * Test for <code>SecretKeyEntry(SecretKey secretKey, Set<Attribute> attributes)</code>
     * constructor
     * Assertion: throws NullPointerException when attributes is null
     */
    public void testSecretKeyEntry_nullAttributes() {
        Certificate cert = new MyCertificate("TEST", new byte[10]);
        try {
            new KeyStore.TrustedCertificateEntry(cert, null /* attributes */);
            fail("NullPointerException must be thrown when attributes is null");
        } catch(NullPointerException expected) {
        }
    }

    /**
     * Test for <codfe>getTrustedCertificate()</code> method
     * Assertion: returns trusted Certificate from goven entry
     */
    public void testGetTrustedCertificate() {
        Certificate cert = new MyCertificate("TEST", new byte[10]);
        KeyStore.TrustedCertificateEntry ksTCE =
                new KeyStore.TrustedCertificateEntry(cert);
        assertEquals("Incorrect certificate", cert, ksTCE.getTrustedCertificate());
    }

    /**
     * Test for <code>getAttributes()</code> method
     * Assertion: returns the attributes specified in the constructor, as an unmodifiable set
     */
    public void testGetAttributes() {
        Certificate cert = new MyCertificate("TEST", new byte[10]);
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

        KeyStore.TrustedCertificateEntry ksTCE =
                new KeyStore.TrustedCertificateEntry(cert, attributeSet);
        Set<KeyStore.Entry.Attribute> returnedAttributeSet = ksTCE.getAttributes();
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
     * Test for <codfe>toString()</code> method
     * Assertion: returns non null string
     */
    public void testToString() {
        Certificate cert = new MyCertificate("TEST", new byte[10]);
        KeyStore.TrustedCertificateEntry ksTCE =
                new KeyStore.TrustedCertificateEntry(cert);
        assertNotNull("toString() returns null string", ksTCE.toString());
    }
}
