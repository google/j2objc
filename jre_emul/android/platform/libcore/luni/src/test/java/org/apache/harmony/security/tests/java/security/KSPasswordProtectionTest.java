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
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.spec.IvParameterSpec;
import javax.security.auth.DestroyFailedException;

import junit.framework.TestCase;

/**
 * Tests for <code>KeyStore.PasswordProtection</code> class constructor and methods
 *
 */
public class KSPasswordProtectionTest extends TestCase {

    /**
     * Test for <code>KeyStore.PasswordProtection(char[] password)</code> constructor
     * and the following methods
     * <code>getPassword()<code>
     * <code>destroy()<code>
     * <code>isDestroyed()<code>
     * Assertions: constructor created new PasswordProtection object
     * getPassword() returns password or throws IllegalArgumentException
     * if PasswordProtection is destroyed
     */
    public void testGetPassword() throws DestroyFailedException {
        char [] pass = {'a', 'b', 'c'};
        KeyStore.PasswordProtection ksPWP = new KeyStore.PasswordProtection(pass);
        char [] rPass = ksPWP.getPassword();
        assertFalse("PasswordProtection Should not be destroyed", ksPWP.isDestroyed());
        assertEquals("Incorrect password length", pass.length, rPass.length);
        for (int i = 0; i < pass.length; i++) {
            assertEquals("Incorrect password (item: ".concat(Integer.toString(i))
                    .concat(")"), pass[i], rPass[i]);
        }
        ksPWP.destroy();
        assertTrue("PasswordProtection must be destroyed", ksPWP.isDestroyed());
        try {
            ksPWP.getPassword();
            fail("IllegalStateException must be thrown because PasswordProtection is destroyed");
        } catch (IllegalStateException e) {
        }

        try {
            ksPWP = new KeyStore.PasswordProtection(null);
        } catch (Exception e) {
            fail("Unexpected exception for NULL parameter");
        }
    }

    /**
     * Test for <code>KeyStore.PasswordProtection(char[] password, String protectionAlgorithm,
     * AlgorithmParameterSpec protectionParameters)</code> constructor
     * and the method <code>getProtectionAlgorithm()</code>

     * Assertions: constructor throws NullPointerException if protectionAlgorithm is null.
     * getProtectionAlgorithm() returns the protection algorithm passed in the constructor.
     */
    public void testGetProtectionAlgorithm() throws DestroyFailedException {
        char [] pass = {'a', 'b', 'c'};
        String protectionAlgorithm = "ThisBeautifulAlgorithm";
        AlgorithmParameterSpec protectionParameters = new IvParameterSpec(new byte[]{});
        KeyStore.PasswordProtection ksPWP;
        try {
            ksPWP = new KeyStore.PasswordProtection(
                    pass, null /* protectionAlgorithm */, protectionParameters);
            fail("Expected null pointer exception");
        } catch (NullPointerException expected) {
        }
        ksPWP = new KeyStore.PasswordProtection(
                pass, protectionAlgorithm, null /* protectionParameters */);
        assertSame(protectionAlgorithm, ksPWP.getProtectionAlgorithm());
    }

    /**
     * Test for <code>KeyStore.PasswordProtection(char[] password, String protectionAlgorithm,
     * AlgorithmParameterSpec protectionParameters)</code> constructor
     * and the method <code>getProtectionParameters()</code>

     * Assertions: constructor creates new PasswordProtection object, even if protectionParameters
     * is null. getProtectionParameterrs() returns the protection algorithm passed in the
     * constructor.
     */
    public void testGetProtectionParameters() throws DestroyFailedException {
        char [] pass = {'a', 'b', 'c'};
        AlgorithmParameterSpec protectionParameters = new IvParameterSpec(new byte[]{});
        KeyStore.PasswordProtection ksPWP =
                new KeyStore.PasswordProtection(
                        pass, "protectionAlgorithm", null /* protectionParameters */);
        assertNull(ksPWP.getProtectionParameters());
        ksPWP = new KeyStore.PasswordProtection(
                pass, "protectionAlgorithm", protectionParameters);
        assertSame(protectionParameters, ksPWP.getProtectionParameters());
    }
}
