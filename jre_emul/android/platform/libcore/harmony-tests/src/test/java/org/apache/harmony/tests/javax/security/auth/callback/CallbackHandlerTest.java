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

package org.apache.harmony.tests.javax.security.auth.callback;

import junit.framework.TestCase;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

/**
 * Tests for <code>CallbackHandler</code> class constructors and methods.
 *
 */
public class CallbackHandlerTest extends TestCase {

    /**
     * javax.security.auth.callback.CallbackHandler#handle(Callback[] callbacks)
     */
    public void test_CallbackHandler() {
        CallbackHandlerImpl ch = new CallbackHandlerImpl();
        assertFalse(ch.called);
        ch.handle(null);
        assertTrue(ch.called);
    }

    private class CallbackHandlerImpl implements CallbackHandler {
        boolean called = false;
        public void handle(Callback[] callbacks) {
            called = true;
        }
    }
}
