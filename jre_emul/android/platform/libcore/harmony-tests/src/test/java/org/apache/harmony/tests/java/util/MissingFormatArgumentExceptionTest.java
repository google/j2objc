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
import java.util.MissingFormatArgumentException;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

public class MissingFormatArgumentExceptionTest extends TestCase {

    /**
     * java.util.MissingFormatArgumentException#MissingFormatArgumentException(String)
     */
    public void test_missingFormatArgumentException() {

        try {
            new MissingFormatArgumentException(null);
            fail("should throw NullPointerExcepiton.");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * java.util.MissingFormatArgumentException#getFormatSpecifier()
     */
    public void test_getFormatSpecifier() {
        String s = "MYTESTSTRING";
        MissingFormatArgumentException missingFormatArgumentException = new MissingFormatArgumentException(
                s);
        assertEquals(s, missingFormatArgumentException.getFormatSpecifier());
    }

    /**
     * java.util.MissingFormatArgumentException#getMessage()
     */
    public void test_getMessage() {
        String s = "MYTESTSTRING";
        MissingFormatArgumentException missingFormatArgumentException = new MissingFormatArgumentException(
                s);
        assertTrue(null != missingFormatArgumentException.getMessage());

    }

    // comparator for comparing MissingFormatArgumentException objects
    private static final SerializableAssert exComparator = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {

            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);

            MissingFormatArgumentException initEx = (MissingFormatArgumentException) initial;
            MissingFormatArgumentException desrEx = (MissingFormatArgumentException) deserialized;

            assertEquals("FormatSpecifier", initEx.getFormatSpecifier(), desrEx
                    .getFormatSpecifier());
        }
    };

    /**
     * serialization/deserialization.
     */
    public void testSerializationSelf() throws Exception {

        SerializationTest.verifySelf(new MissingFormatArgumentException(
                "MYTESTSTRING"), exComparator);
    }

    /**
     * serialization/deserialization compatibility with RI.
     */
    public void testSerializationCompatibility() throws Exception {

        SerializationTest.verifyGolden(this,
                new MissingFormatArgumentException("MYTESTSTRING"),
                exComparator);
    }
}
