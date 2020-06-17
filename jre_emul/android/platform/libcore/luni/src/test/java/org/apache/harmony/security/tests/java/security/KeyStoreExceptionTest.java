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

import java.security.KeyStoreException;

import junit.framework.TestCase;

/**
 * Tests for <code>KeyStoreException</code> class constructors and methods.
 *
 */
public class KeyStoreExceptionTest extends TestCase {

    private static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };

    private static Throwable tCause = new Throwable("Throwable for exception");

    /**
     * Test for <code>KeyStoreException()</code> constructor Assertion:
     * constructs KeyStoreException with no detail message
     */
    public void testKeyStoreException01() {
        KeyStoreException tE = new KeyStoreException();
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }

    /**
     * Test for <code>KeyStoreException(String)</code> constructor Assertion:
     * constructs KeyStoreException with detail message msg. Parameter
     * <code>msg</code> is not null.
     */
    public void testKeyStoreException02() {
        KeyStoreException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new KeyStoreException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }

    /**
     * Test for <code>KeyStoreException(String)</code> constructor Assertion:
     * constructs KeyStoreException when <code>msg</code> is null
     */
    public void testKeyStoreException03() {
        String msg = null;
        KeyStoreException tE = new KeyStoreException(msg);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }

    /**
     * Test for <code>KeyStoreException(Throwable)</code> constructor
     * Assertion: constructs KeyStoreException when <code>cause</code> is null
     */
    public void testKeyStoreException04() {
        Throwable cause = null;
        KeyStoreException tE = new KeyStoreException(cause);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }

    /**
     * Test for <code>KeyStoreException(Throwable)</code> constructor
     * Assertion: constructs KeyStoreException when <code>cause</code> is not
     * null
     */
    public void testKeyStoreException05() {
        KeyStoreException tE = new KeyStoreException(tCause);
        if (tE.getMessage() != null) {
            String toS = tCause.toString();
            String getM = tE.getMessage();
            assertTrue("getMessage() should contain ".concat(toS), (getM
                    .indexOf(toS) != -1));
        }
        assertNotNull("getCause() must not return null", tE.getCause());
        assertEquals("getCause() must return ".concat(tCause.toString()), tE
                .getCause(), tCause);
    }

    /**
     * Test for <code>KeyStoreException(String, Throwable)</code> constructor
     * Assertion: constructs KeyStoreException when <code>cause</code> is null
     * <code>msg</code> is null
     */
    public void testKeyStoreException06() {
        KeyStoreException tE = new KeyStoreException(null, null);
        assertNull("getMessage() must return null", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }

    /**
     * Test for <code>KeyStoreException(String, Throwable)</code> constructor
     * Assertion: constructs KeyStoreException when <code>cause</code> is null
     * <code>msg</code> is not null
     */
    public void testKeyStoreException07() {
        KeyStoreException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new KeyStoreException(msgs[i], null);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }

    /**
     * Test for <code>KeyStoreException(String, Throwable)</code> constructor
     * Assertion: constructs KeyStoreException when <code>cause</code> is not
     * null <code>msg</code> is null
     */
    public void testKeyStoreException08() {
        KeyStoreException tE = new KeyStoreException(null, tCause);
        if (tE.getMessage() != null) {
            String toS = tCause.toString();
            String getM = tE.getMessage();
            assertTrue("getMessage() must should ".concat(toS), (getM
                    .indexOf(toS) != -1));
        }
        assertNotNull("getCause() must not return null", tE.getCause());
        assertEquals("getCause() must return ".concat(tCause.toString()), tE
                .getCause(), tCause);
    }

    /**
     * Test for <code>KeyStoreException(String, Throwable)</code> constructor
     * Assertion: constructs KeyStoreException when <code>cause</code> is not
     * null <code>msg</code> is not null
     */
    public void testKeyStoreException09() {
        KeyStoreException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new KeyStoreException(msgs[i], tCause);
            String getM = tE.getMessage();
            String toS = tCause.toString();
            if (msgs[i].length() > 0) {
                assertTrue("getMessage() must contain ".concat(msgs[i]), getM
                        .indexOf(msgs[i]) != -1);
                if (!getM.equals(msgs[i])) {
                    assertTrue("getMessage() should contain ".concat(toS), getM
                            .indexOf(toS) != -1);
                }
            }
            assertNotNull("getCause() must not return null", tE.getCause());
            assertEquals("getCause() must return ".concat(tCause.toString()),
                    tE.getCause(), tCause);
        }
    }
}
