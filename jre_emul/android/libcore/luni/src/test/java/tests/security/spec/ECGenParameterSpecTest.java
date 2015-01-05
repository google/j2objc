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

import java.security.spec.ECGenParameterSpec;

/**
 * Tests for <code>ECGenParameterSpec</code> class fields and methods.
 *
 */
public class ECGenParameterSpecTest extends TestCase {

    //
    // Tests
    //

    /**
     * Test #1 for <code>ECGenParameterSpec</code> constructor<br>
     *
     * Assertion: creates new object of <code>ECGenParameterSpec</code> class
     * using valid <code>name</code>
     */
    public final void testECGenParameterSpec01() {
        new ECGenParameterSpec("someName");
    }

    /**
     * Test #2 for <code>ECGenParameterSpec</code> constructor<br>
     *
     * Assertion: throws NullPointerException
     * if <code>name</code> is <code>null</code>
     */
    public final void testECGenParameterSpec02() {
        try {
            new ECGenParameterSpec(null);
            fail("NPE expected");
        } catch (NullPointerException ok) {}
    }

    /**
     * Test for <code>getName()</code> method<br>
     *
     * Assertion: returns the <code>name</code>
     */
    public final void testGetName() {
        String name = "someName";
        ECGenParameterSpec ps = new ECGenParameterSpec(name);
        assertEquals(name, ps.getName());
    }

}
