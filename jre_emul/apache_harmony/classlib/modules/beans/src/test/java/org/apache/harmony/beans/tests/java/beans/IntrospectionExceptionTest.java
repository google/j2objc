/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.beans.tests.java.beans;

import java.beans.IntrospectionException;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;

/**
 * Unit test of IntrospectionException.
 */

public class IntrospectionExceptionTest extends TestCase {

    public void testConstructor() {
        String message = "IntrospectionExceptionTest";
        IntrospectionException e = new IntrospectionException(message);
        assertSame(message, e.getMessage());
    }

    public void testConstructor_MessageNull() {
        IntrospectionException e = new IntrospectionException(null);
        assertNull(e.getMessage());
    }

    /**
     * @tests serialization/deserialization.
     */
    public void testSerializationSelf() throws Exception {

        SerializationTest.verifySelf(new IntrospectionException(
                "IntrospectionExceptionTest"));
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
     */
    public void testSerializationCompatibility() throws Exception {

        SerializationTest.verifyGolden(this, new IntrospectionException(
                "IntrospectionExceptionTest"));
    }

    public void testIntrospectionExceptionMessage() {
        // Regression for HARMONY-235
        IntrospectionException e = new IntrospectionException("test message");
        assertEquals("test message", e.getMessage());
    }
}
