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
import java.util.IllegalFormatConversionException;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

public class IllegalFormatConversionExceptionTest extends TestCase {

    /**
     * java.util.IllegalFormatConversionException#IllegalFormatConversionException(char,
     *Class)
     */
    public void test_illegalFormatConversionException() {
        try {
            new IllegalFormatConversionException(' ', null);
            fail("should throw NullPointerExcetpion.");
        } catch (NullPointerException e) {
            // desired
        }
    }

    /**
     * java.util.IllegalFormatConversionException#getArgumentClass()
     */
    public void test_getArgumentClass() {
        char c = '*';
        Class<String> argClass = String.class;
        IllegalFormatConversionException illegalFormatConversionException = new IllegalFormatConversionException(
                c, argClass);
        assertEquals(argClass, illegalFormatConversionException
                .getArgumentClass());

    }

    /**
     * java.util.IllegalFormatConversionException#getConversion()
     */
    public void test_getConversion() {
        char c = '*';
        Class<String> argClass = String.class;
        IllegalFormatConversionException illegalFormatConversionException = new IllegalFormatConversionException(
                c, argClass);
        assertEquals(c, illegalFormatConversionException.getConversion());

    }

    /**
     * java.util.IllegalFormatConversionException#getMessage()
     */
    public void test_getMessage() {
        char c = '*';
        Class<String> argClass = String.class;
        IllegalFormatConversionException illegalFormatConversionException = new IllegalFormatConversionException(
                c, argClass);
        assertTrue(null != illegalFormatConversionException.getMessage());

    }

    // comparator for IllegalFormatConversionException objects
    private static final SerializableAssert exComparator = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {

            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);

            IllegalFormatConversionException initEx = (IllegalFormatConversionException) initial;
            IllegalFormatConversionException desrEx = (IllegalFormatConversionException) deserialized;

            assertEquals("ArgumentClass", initEx.getArgumentClass(), desrEx
                    .getArgumentClass());
            assertEquals("Conversion", initEx.getConversion(), desrEx
                    .getConversion());
        }
    };

    /**
     * serialization/deserialization.
     */
    public void testSerializationSelf() throws Exception {

        SerializationTest.verifySelf(new IllegalFormatConversionException('*',
                String.class), exComparator);
    }

    /**
     * serialization/deserialization compatibility with RI.
     */
    public void testSerializationCompatibility() throws Exception {

        SerializationTest.verifyGolden(this,
                new IllegalFormatConversionException('*', String.class),
                exComparator);
    }
}
