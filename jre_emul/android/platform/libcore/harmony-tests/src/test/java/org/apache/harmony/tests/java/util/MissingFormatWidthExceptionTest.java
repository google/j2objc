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
import java.util.MissingFormatWidthException;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

public class MissingFormatWidthExceptionTest extends TestCase {

    /**
     * java.util.MissingFormatWidthException#MissingFormatWidthException(String)
     */
    public void test_missingFormatWidthException() {
        try {
            new MissingFormatWidthException(null);
            fail("should throw NullPointerExcepiton");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * java.util.MissingFormatWidthException#getFormatSpecifier()
     */
    public void test_getFormatSpecifier() {
        String s = "MYTESTSTRING";
        MissingFormatWidthException missingFormatWidthException = new MissingFormatWidthException(
                s);
        assertEquals(s, missingFormatWidthException.getFormatSpecifier());

    }

    /**
     * java.util.MissingFormatWidthException#getMessage()
     */
    public void test_getMessage() {
        String s = "MYTESTSTRING";
        MissingFormatWidthException missingFormatWidthException = new MissingFormatWidthException(
                s);
        assertTrue(null != missingFormatWidthException.getMessage());

    }

    // comparator for comparing MissingFormatWidthException objects
    private static final SerializableAssert exComparator = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {

            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);

            MissingFormatWidthException initEx = (MissingFormatWidthException) initial;
            MissingFormatWidthException desrEx = (MissingFormatWidthException) deserialized;

            assertEquals("FormatSpecifier", initEx.getFormatSpecifier(), desrEx
                    .getFormatSpecifier());
        }
    };

    /**
     * serialization/deserialization.
     */
    public void testSerializationSelf() throws Exception {

        SerializationTest.verifySelf(new MissingFormatWidthException(
                "MYTESTSTRING"), exComparator);
    }

    /**
     * serialization/deserialization compatibility with RI.
     */
    public void testSerializationCompatibility() throws Exception {

        SerializationTest.verifyGolden(this, new MissingFormatWidthException(
                "MYTESTSTRING"), exComparator);
    }
}
