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
* @author Alexey V. Varlamov
* @version $Revision$
*/

package org.apache.harmony.security.tests.java.security;

import java.security.Guard;
import java.security.GuardedObject;

import junit.framework.TestCase;

/**
 * Tests for <code>GuardedObject</code>
 *
 */
public class GuardedObjectTest extends TestCase {

    /** Null guard imposes no restriction. */
    public void testNoGuard() {
        Object obj = null;
        GuardedObject go = new GuardedObject(obj, null);
        assertNull(go.getObject());

        obj = "ewte rtw3456";
        go = new GuardedObject(obj, null);
        assertEquals(obj, go.getObject());
    }

    /** Test real guard can both allow and deny access. */
    public void testGuard() {
        final String message = "test message";
        final StringBuffer objBuffer = new StringBuffer("235345 t");
        GuardedObject go = new GuardedObject(objBuffer, new Guard() {

            public void checkGuard(Object object) throws SecurityException {
                if (object == objBuffer && objBuffer.length() == 0) {
                    throw new SecurityException(message);
                }
            }
        });
        assertEquals(objBuffer, go.getObject());

        objBuffer.setLength(0);
        try {
            go.getObject();
            fail("SecurityException is not thrown");
        } catch (Exception ok) {
            assertEquals(message, ok.getMessage());
        }
    }
}
