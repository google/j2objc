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
package org.apache.harmony.tests.java.lang;

import junit.framework.TestCase;

public class UnsupportedClassVersionErrorTest extends TestCase {
    /**
     * Thrown when the Java Virtual Machine attempts to read a class file and
     * determines that the major and minor version numbers in the file are not
     * supported.
     */

    /**
     * java.lang.UnsupportedClassVersionError#UnsupportedClassVersionError()
     */
    public void test_UnsupportedClassVersionError() {
        UnsupportedClassVersionError error = new UnsupportedClassVersionError();
        assertNotNull(error);
        assertNull(error.getMessage());
    }

    /**
     * java.lang.UnsupportedClassVersionError#UnsupportedClassVersionError(java.lang.String)
     */
    public void test_UnsupportedClassVersionError_LString() {
        UnsupportedClassVersionError e = new UnsupportedClassVersionError(
                "Some Error Message");
        assertEquals("Wrong message", "Some Error Message", e.getMessage());
    }

}
