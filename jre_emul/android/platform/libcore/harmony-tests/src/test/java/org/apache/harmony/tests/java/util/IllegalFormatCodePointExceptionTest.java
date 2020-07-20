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
import java.util.IllegalFormatCodePointException;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

public class IllegalFormatCodePointExceptionTest extends TestCase {

    /**
     * java.util.IllegalFormatCodePointException.IllegalFormatCodePointException(int)
     */
    public void test_illegalFormatCodePointException() {
        IllegalFormatCodePointException illegalFormatCodePointException = new IllegalFormatCodePointException(
                -1);
        assertTrue(null != illegalFormatCodePointException);
    }

    /**
     * java.util.IllegalFormatCodePointException.getCodePoint()
     */
    public void test_getCodePoint() {
        int codePoint = 12345;
        IllegalFormatCodePointException illegalFormatCodePointException = new IllegalFormatCodePointException(
                codePoint);
        assertEquals(codePoint, illegalFormatCodePointException.getCodePoint());
    }

    /**
     * java.util.IllegalFormatCodePointException.getMessage()
     */
    public void test_getMessage() {
        int codePoint = 12345;
        IllegalFormatCodePointException illegalFormatCodePointException = new IllegalFormatCodePointException(
                codePoint);
        assertTrue(null != illegalFormatCodePointException.getMessage());
    }

    // comparator for IllegalFormatCodePointException objects
    private static final SerializableAssert exComparator = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {

            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);

            IllegalFormatCodePointException initEx = (IllegalFormatCodePointException) initial;
            IllegalFormatCodePointException desrEx = (IllegalFormatCodePointException) deserialized;

            assertEquals("CodePoint", initEx.getCodePoint(), desrEx
                    .getCodePoint());
        }
    };

    /**
     * serialization/deserialization.
     */
    public void testSerializationSelf() throws Exception {

        SerializationTest.verifySelf(
                new IllegalFormatCodePointException(12345), exComparator);
    }

    /**
     * serialization/deserialization compatibility with RI.
     */
    public void testSerializationCompatibility() throws Exception {

        SerializationTest.verifyGolden(this,
                new IllegalFormatCodePointException(12345), exComparator);
    }
}
