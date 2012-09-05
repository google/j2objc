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

package org.apache.harmony.luni.tests.java.io;

import java.io.IOException;

import junit.framework.TestCase;

public class IOExceptionTest extends TestCase {

    /**
     * @tests java.io.IOException#IOException()
     */
    @SuppressWarnings("unused")
    public void test_Constructor() {
        try {
            if (true) {
                throw new IOException();
            }
            fail("Exception during IOException test");
        } catch (IOException e) {
            // Expected
        }
    }

    /**
     * @tests java.io.IOException#IOException(java.lang.String)
     */
    @SuppressWarnings("unused")
    public void test_ConstructorLjava_lang_String() {
        try {
            if (true) {
                throw new IOException("Some error message");
            }
            fail("Failed to generate exception");
        } catch (IOException e) {
            // Expected
        }
    }
}
