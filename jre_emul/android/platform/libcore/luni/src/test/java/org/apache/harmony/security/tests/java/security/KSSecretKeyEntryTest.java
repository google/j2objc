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
import java.util.HashSet;
import java.util.Set;

import javax.crypto.SecretKey;

import junit.framework.TestCase;

/**
 * Tests for <code>KeyStore.SecretKeyEntry</code> class constructor and methods
 *
 */
public class KSSecretKeyEntryTest extends TestCase {

    /**
     * Test for <code>SecretKeyEntry(SecretKey secretKey)</code> constructor
     * Assertion: throws NullPointerException when secretKey is null
     */
    public void testSecretKeyEntry() {
        SecretKey sk = null;
        try {
            new KeyStore.SecretKeyEntry(sk);
            fail("NullPointerException must be thrown when secretKey is null");
        } catch(NullPointerException e) {
            //expected
        }

        sk = new tmpSecretKey();
        try {
            KeyStore.SecretKeyEntry ske = new KeyStore.SecretKeyEntry(sk);
            assertNotNull(ske);
            assertTrue(ske instanceof KeyStore.SecretKeyEntry);
        } catch(Exception e) {
            fail("Unexpected exception was thrown when secretKey is not null");
        }
    }

    /**
     * Test for
     * <code>SecretKeyEntry(SecretKey secretKey, Set<Attribute> attribute)</code>
     * constructor
     * Assertion: throws NullPointerException when attributes is null
     */
    public void testSecretKeyEntry_nullAttributes() {
        SecretKey sk = new tmpSecretKey();
        try {
            new KeyStore.SecretKeyEntry(sk, null /* attributes */);
            fail("NullPointerException must be thrown when attributes is null");
        } catch(NullPointerException expected) {
        }
    }

    /**
     * Test for <code>getSecretKey()</code> method
     * Assertion: returns SecretKey from the given entry
     */
    public void testGetSecretKey() {
        SecretKey sk = new tmpSecretKey();
        KeyStore.SecretKeyEntry ske = new KeyStore.SecretKeyEntry(sk);
        assertEquals("Incorrect SecretKey", sk, ske.getSecretKey());
    }

    /**
     * Test for <code>getAttributes()</code> method
     * Assertion: returns the attributes specified in the constructor, as an unmodifiable set
     */
    public void testGetAttributes() {
        SecretKey sk = new tmpSecretKey();
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

        KeyStore.SecretKeyEntry ksSKE = new KeyStore.SecretKeyEntry(sk, attributeSet);
        Set<KeyStore.Entry.Attribute> returnedAttributeSet = ksSKE.getAttributes();
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
     * Assertion: returns non null string
     */
    public void testToString() {
        SecretKey sk = new tmpSecretKey();
        KeyStore.SecretKeyEntry ske = new KeyStore.SecretKeyEntry(sk);
        assertNotNull("toString() returns null string", ske.toString());
    }
}

class tmpSecretKey implements SecretKey {
    public String getAlgorithm() {
        return "My algorithm";
    }
    public String getFormat() {
        return "My Format";
    }
    public byte[] getEncoded() {
        return new byte[1];
    }
}
