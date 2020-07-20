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
import java.util.IllegalFormatWidthException;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

public class IllegalFormatWidthExceptionTest extends TestCase {

    /**
     * java.util.IllegalFormatWidthException#IllegalFormatWidthException(int)
     */
    public void test_illegalFormatWidthException() {
        int width = Integer.MAX_VALUE;
        IllegalFormatWidthException illegalFormatWidthException = new IllegalFormatWidthException(
                width);
        assertEquals(width, illegalFormatWidthException.getWidth());

    }

    /**
     * java.util.IllegalFormatWidthException#getWidth()
     */
    public void test_getWidth() {
        int width = 12345;
        IllegalFormatWidthException illegalFormatWidthException = new IllegalFormatWidthException(
                width);
        assertEquals(width, illegalFormatWidthException.getWidth());

    }

    /**
     * java.util.IllegalFormatWidthException#getMessage()
     */
    public void test_getMessage() {
        int width = 12345;
        IllegalFormatWidthException illegalFormatWidthException = new IllegalFormatWidthException(
                width);
        assertTrue(null != illegalFormatWidthException.getMessage());

    }

    // comparator for IllegalFormatWidthException objects
    private static final SerializableAssert exComparator = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {

            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);

            IllegalFormatWidthException initEx = (IllegalFormatWidthException) initial;
            IllegalFormatWidthException desrEx = (IllegalFormatWidthException) deserialized;

            assertEquals("Width", initEx.getWidth(), desrEx.getWidth());
        }
    };

    /**
     * serialization/deserialization.
     */
    public void testSerializationSelf() throws Exception {

        SerializationTest.verifySelf(new IllegalFormatWidthException(12345),
                exComparator);
    }

    /**
     * serialization/deserialization compatibility with RI.
     */
    public void testSerializationCompatibility() throws Exception {

        SerializationTest.verifyGolden(this, new IllegalFormatWidthException(
                12345), exComparator);
    }
}
