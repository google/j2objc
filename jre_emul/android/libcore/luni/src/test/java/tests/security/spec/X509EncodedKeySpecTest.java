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
* @author Vladimir N. Molotkov
* @version $Revision$
*/

package tests.security.spec;

import junit.framework.TestCase;

import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

/**
 * Tests for <code>X509EncodedKeySpec</code> class fields and methods
 *
 */
public class X509EncodedKeySpecTest extends TestCase {

    //
    // Test cases
    //

    /**
     * Test for <code>X509EncodedKeySpec</code> constructor<br>
     * Assertion: constructs new <code>X509EncodedKeySpec</code>
     * object using valid parameter
     */
    public final void testX509EncodedKeySpec() {
        byte[] encodedKey = new byte[] {(byte)1,(byte)2,(byte)3,(byte)4};

        EncodedKeySpec eks = new X509EncodedKeySpec(encodedKey);

        assertTrue(eks instanceof X509EncodedKeySpec);
        try {
            eks = new X509EncodedKeySpec(null);
            fail("expected NullPointerException");
        } catch (NullPointerException e) {
            // ok
        }
    }

    /**
     * Test for <code>getEncoded()</code> method<br>
     * Assertion: returns encoded key
     */
    public final void testGetEncoded() {
        byte[] encodedKey = new byte[] {(byte)1,(byte)2,(byte)3,(byte)4};

        X509EncodedKeySpec eks = new X509EncodedKeySpec(encodedKey);

        byte[] ek = eks.getEncoded();

        assertTrue(Arrays.equals(encodedKey, ek));
    }

    /**
     * Test for <code>getFormat()</code> method
     * Assertion: returns format name (always "X.509")
     */
    public final void testGetFormat() {
        byte[] encodedKey = new byte[] {(byte)1,(byte)2,(byte)3,(byte)4};

        X509EncodedKeySpec meks = new X509EncodedKeySpec(encodedKey);

        assertEquals("X.509", meks.getFormat());
    }

    /**
     * Tests that internal state of the object
     * can not be changed by modifying initial
     * array value
     */
    public final void testIsStatePreserved1() {
        // Reference array
        byte[] encodedKey = new byte[] {(byte)1,(byte)2,(byte)3,(byte)4};
        // Reference array's copy will be used for test
        byte[] encodedKeyCopy = encodedKey.clone();

        X509EncodedKeySpec meks = new X509EncodedKeySpec(encodedKeyCopy);

        // Modify initial array's value
        encodedKeyCopy[3] = (byte)5;

        // Get encoded key
        byte[] ek = meks.getEncoded();

        // Check  using reference array that
        // byte value has not been changed
        assertTrue(Arrays.equals(encodedKey, ek));
    }

    /**
     * Tests that internal state of the object
     * can not be modified using returned value
     * of <code>getEncoded()</code> method
     */
    public final void testIsStatePreserved2() {
        // Reference array
        byte[] encodedKey = new byte[] {(byte)1,(byte)2,(byte)3,(byte)4};
        // Reference array's copy will be used for test
        byte[] encodedKeyCopy = encodedKey.clone();

        X509EncodedKeySpec meks = new X509EncodedKeySpec(encodedKeyCopy);

        byte[] ek = meks.getEncoded();

        // Modify returned array
        ek[3] = (byte)5;

        // Get encoded key again
        byte[] ek1 = meks.getEncoded();

        // Check using reference array that
        // byte value has not been changed
        assertTrue(Arrays.equals(encodedKey, ek1));
    }

}
