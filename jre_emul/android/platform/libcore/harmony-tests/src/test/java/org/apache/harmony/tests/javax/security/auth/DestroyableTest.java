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

package org.apache.harmony.tests.javax.security.auth;

import junit.framework.TestCase;

import javax.security.auth.Destroyable;
import javax.security.auth.DestroyFailedException;


/**
 * Tests for <code>Destroyable</code> class constructors and methods.
 *
 */
public class DestroyableTest extends TestCase {

    /**
     * javax.security.auth.Destroyable#destroy()
     * javax.security.auth.Destroyable#isDestroyed()
     */
    public void test_destroy() {
        myDestroyable md = new myDestroyable();
        try {
            assertFalse(md.isDestroyed());
            md.destroy();
            assertTrue(md.isDestroyed());
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }

    private class myDestroyable implements Destroyable {

        boolean destroyDone = false;

        myDestroyable() {
        }

        public void destroy() throws DestroyFailedException {
            destroyDone = true;
        }

        public boolean isDestroyed() {
            return destroyDone;
        }
    }
}


