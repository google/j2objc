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

import java.io.InterruptedIOException;

import junit.framework.TestCase;

public class InterruptedIOExceptionTest extends TestCase {

    /**
     * @tests java.io.InterruptedIOException#InterruptedIOException()
     */
    public void test_Constructor() {
        try {
            throw new InterruptedIOException();
        } catch (InterruptedIOException e) {
            // Expected
        }
    }

    /**
     * @tests java.io.InterruptedIOException#InterruptedIOException(java.lang.String)
     */
    public void test_ConstructorLjava_lang_String() {
        try {
            throw new InterruptedIOException("Some error message");
        } catch (InterruptedIOException e) {
            // Expected
        }
    }
}
