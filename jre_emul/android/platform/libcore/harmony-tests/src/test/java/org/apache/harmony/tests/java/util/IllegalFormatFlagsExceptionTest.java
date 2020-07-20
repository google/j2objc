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
import java.util.IllegalFormatFlagsException;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

public class IllegalFormatFlagsExceptionTest extends TestCase {

    /**
     * java.util.IllegalFormatFlagsException#IllegalFormatFlagsException(String)
     */
    public void test_illegalFormatFlagsException() {
        try {
            new IllegalFormatFlagsException(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * java.util.IllegalFormatFlagsException.getFlags()
     */
    public void test_getFlags() {
        String flags = "TESTFLAGS";
        IllegalFormatFlagsException illegalFormatFlagsException = new IllegalFormatFlagsException(
                flags);
        assertEquals(flags, illegalFormatFlagsException.getFlags());
    }

    /**
     * java.util.IllegalFormatFlagsException.getMessage()
     */
    public void test_getMessage() {
        String flags = "TESTFLAGS";
        IllegalFormatFlagsException illegalFormatFlagsException = new IllegalFormatFlagsException(
                flags);
        assertTrue(null != illegalFormatFlagsException.getMessage());

    }

    // comparator for IllegalFormatFlagsException objects
    private static final SerializableAssert exComparator = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {

            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);

            IllegalFormatFlagsException initEx = (IllegalFormatFlagsException) initial;
            IllegalFormatFlagsException desrEx = (IllegalFormatFlagsException) deserialized;

            assertEquals("Flags", initEx.getFlags(), desrEx.getFlags());
        }
    };

    /**
     * serialization/deserialization.
     */
    public void testSerializationSelf() throws Exception {

        SerializationTest.verifySelf(new IllegalFormatFlagsException(
                "TESTFLAGS"), exComparator);
    }

    /**
     * serialization/deserialization compatibility with RI.
     */
    public void testSerializationCompatibility() throws Exception {

        SerializationTest.verifyGolden(this, new IllegalFormatFlagsException(
                "TESTFLAGS"), exComparator);
    }
}
