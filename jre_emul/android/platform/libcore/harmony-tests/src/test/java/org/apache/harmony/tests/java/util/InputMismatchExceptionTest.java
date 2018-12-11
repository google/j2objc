/* Licensed to the Apache Software Foundation (ASF) under one or more
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
package org.apache.harmony.tests.java.util;

import java.io.Serializable;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;

public class InputMismatchExceptionTest extends TestCase {

    private static final String ERROR_MESSAGE = "for serialization test"; //$NON-NLS-1$

    /**
     * java.util.InputMismatchException#InputMismatchException()
     */
    @SuppressWarnings("cast")
    public void test_Constructor() {
        InputMismatchException exception = new InputMismatchException();
        assertNotNull(exception);
        assertTrue(exception instanceof NoSuchElementException);
        assertTrue(exception instanceof Serializable);
    }

    /**
     * java.util.InputMismatchException#InputMismatchException(String)
     */
    public void test_ConstructorLjava_lang_String() {
        InputMismatchException exception = new InputMismatchException(
                ERROR_MESSAGE);
        assertNotNull(exception);
        assertEquals(ERROR_MESSAGE, exception.getMessage());
    }

    /**
     * serialization/deserialization.
     */
    public void testSerializationSelf() throws Exception {

        SerializationTest.verifySelf(new InputMismatchException(ERROR_MESSAGE));
    }

    /**
     * serialization/deserialization compatibility with RI.
     */
    public void testSerializationCompatibility() throws Exception {

        SerializationTest.verifyGolden(this, new InputMismatchException(
                ERROR_MESSAGE));
    }
}
