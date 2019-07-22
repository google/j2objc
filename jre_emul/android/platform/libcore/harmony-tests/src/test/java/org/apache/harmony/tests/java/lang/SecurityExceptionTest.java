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

package org.apache.harmony.tests.java.lang;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;

public class SecurityExceptionTest extends TestCase {

    /**
     * java.lang.SecurityException#SecurityException()
     */
    public void test_Constructor() {
        SecurityException e = new SecurityException();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }

    /**
     * java.lang.SecurityException#SecurityException(java.lang.String)
     */
    public void test_ConstructorLjava_lang_String() {
        SecurityException e = new SecurityException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }

    /**
     * java.lang.SecurityException#SecurityException(String, Throwable)
     */
    @SuppressWarnings("nls")
    public void test_ConstructorLjava_lang_StringLjava_lang_Throwable() {
        NullPointerException npe = new NullPointerException();
        SecurityException e = new SecurityException("fixture", npe);
        assertSame("fixture", e.getMessage());
        assertSame(npe, e.getCause());
    }

    /**
     * java.lang.SecurityException#SecurityException(Throwable)
     */
    @SuppressWarnings("nls")
    public void test_ConstructorLjava_lang_Throwable() {
        NullPointerException npe = new NullPointerException();
        SecurityException e = new SecurityException(npe);
        assertSame(npe, e.getCause());
    }

    /**
     * serialization/deserialization.
     */
    public void testSerializationSelf() throws Exception {

        SerializationTest.verifySelf(new SecurityException());
    }

    /**
     * serialization/deserialization compatibility with RI.
     */
    public void testSerializationCompatibility() throws Exception {

        SerializationTest.verifyGolden(this, new SecurityException());
    }
}
