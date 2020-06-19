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

import java.io.IOException;
import java.security.KeyStore;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.harmony.security.tests.support.tmpCallbackHandler;

import junit.framework.TestCase;

/**
 * Tests for <code>KeyStore.CallbackHandlerProtection> class constructor and methods
 *
 */
public class KSCallbackHandlerProtectionTest extends TestCase {

    /**
     * Test for <code>KeyStore.CallbackHandlerProtection(CallbackHandler handler)</code>
     * constructor
     * Assertion: throws NullPointerException when handler is null
     */
    public void testCallbackHandlerProtection() {
        try {
            new KeyStore.CallbackHandlerProtection(null);
            fail("NullPointerException must be thrown when handler is null");
        } catch (NullPointerException e) {
        }

        class TestCallbackHandler implements CallbackHandler {

            public void handle(Callback[] callbacks) throws IOException,
                    UnsupportedCallbackException {
                // does nothing
            }

        }

        try {
            new KeyStore.CallbackHandlerProtection(new TestCallbackHandler());
        } catch (Exception e) {
            fail("unexpected exception: " + e);
        }

    }

    /**
     * Test for <code>getCallbackHandler()</code> method
     * Assertion: returns CallbackHandler
     */
    public void testGetCallBackHandler() {
        CallbackHandler cbh = new tmpCallbackHandler();
        KeyStore.CallbackHandlerProtection ksCBH = new KeyStore.CallbackHandlerProtection(cbh);
        assertEquals("Incorrect CallbackHandler", cbh,
                ksCBH.getCallbackHandler());
    }
}

